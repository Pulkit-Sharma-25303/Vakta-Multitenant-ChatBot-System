package com.Vakta.Vakta_Chat_Bot.Controller;

import com.Vakta.Vakta_Chat_Bot.Model.VaktaChatBot;
import com.Vakta.Vakta_Chat_Bot.Repository.VaktaRepository;
import com.Vakta.Vakta_Chat_Bot.Service.DataIngestionService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatbotController {

    private final DataIngestionService dataIngestionService;
    private final VaktaRepository vaktaRepository;
    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;

    // In-memory map of session chat memories (UUID per chatbot/session)
    private final Map<UUID, ChatMemory> chatMemories = new ConcurrentHashMap<>();

    @Autowired
    public ChatbotController(ChatModel chatModel,
                             DataIngestionService dataIngestionService,
                             VaktaRepository vaktaRepository,
                             QdrantClient qdrantClient,
                             EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.dataIngestionService = dataIngestionService;
        this.vaktaRepository = vaktaRepository;
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
    }

    @PostMapping("/chatbots")
    public VaktaChatBot createChatbot(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String collectionName = "chatbot_" + UUID.randomUUID().toString().replace("-", "");
        VaktaChatBot newChatbot = new VaktaChatBot(name, collectionName);
        VaktaChatBot savedChatbot = vaktaRepository.save(newChatbot);

        try {
            int dimensions = embeddingModel.dimensions();
            qdrantClient.createCollectionAsync(
                    savedChatbot.getQdrantCollectionName(),
                    VectorParams.newBuilder().setSize(dimensions).setDistance(Distance.Cosine).build()
            ).get();
            System.out.println("Successfully created collection: " + savedChatbot.getQdrantCollectionName());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create chatbot memory store.", e);
        }
        return savedChatbot;
    }

    @GetMapping("/chatbots/{chatbotId}/chat")
    public ResponseEntity<String> chat(@PathVariable UUID chatbotId, @RequestParam String query) {
        Optional<VaktaChatBot> chatbotOptional = vaktaRepository.findById(chatbotId);
        if (chatbotOptional.isEmpty()) {
            return ResponseEntity.status(404).body("This chatbot does not exist.");
        }
        String collectionName = chatbotOptional.get().getQdrantCollectionName();

        // --- 1) Create / get the session ChatMemory using the supported builder API ---
        ChatMemory chatMemory = this.chatMemories.computeIfAbsent(chatbotId,
                id -> MessageWindowChatMemory.builder()
                        .maxMessages(20) // keep last 20 messages in memory
                        .build()
        );

        // --- 2) Perform RAG retrieval from Qdrant ---
        QdrantVectorStore tempVectorStore = QdrantVectorStore.builder(this.qdrantClient, this.embeddingModel)
                .collectionName(collectionName)
                .build();

        List<Document> similarDocuments = tempVectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(5).build()
        );

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // If no context found, return the exact out-of-context message requested
        if (context.isEmpty()) {
            return ResponseEntity.ok("Sorry, the given query is out of context");
        }

        // --- 3) Enhanced, friendly but strictly grounded prompt (RAG) ---
        PromptTemplate promptTemplate = new PromptTemplate(
                """
                        You are Vakta, a helpful and professional AI assistant.\s
                        Your job is to answer user questions strictly based on the provided CONTEXT.\s
                        
                        Guidelines:
                        1. Always prioritize the CONTEXT when answering.
                        2. Never add or invent information that is not in the CONTEXT.
                        3. If the CONTEXT does not contain the answer, respond exactly:\s
                           "Sorry, the given query is out of context."
                        4. Be clear, friendly, and professional.\s
                           - Start with a brief acknowledgment of the question.
                           - Provide a concise and accurate answer.
                           - Encourage the user to ask follow-up questions if needed.
                        5. Keep answers focused, relevant, and easy to read.\s
                           (Avoid unnecessary details unless asked.)
                        
                        ---
                        
                        📘 CONTEXT (knowledge base):
                        {context}
                        
                        💬 CHAT HISTORY:
                        {{history}}
                        
                        ❓ USER QUESTION:
                        {query}
                        
                        ---
                        
                        ✅ FINAL ANSWER:
                        
                """
        );

        // --- 4) Build ChatClient with memory advisor so history is injected automatically ---
        ChatClient memoryAwareChatClient = ChatClient.builder(this.chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();

        // --- 5) Call the model with the prompt ---
        String response = memoryAwareChatClient.prompt(
                        promptTemplate.create(Map.of(
                                "context", context,
                                "query", query
                        ))
                )
                .call()
                .content();

        // Note: We do NOT manually `add(...)` messages to ChatMemory here — the advisor / client pipeline manages message storage.

        return ResponseEntity.ok(response);
    }

    @PostMapping("/chatbots/{chatbotId}/ingest-urls")
    public ResponseEntity<String> ingestUrls(@PathVariable UUID chatbotId, @RequestBody List<String> urls) {
        Optional<VaktaChatBot> chatbotOptional = vaktaRepository.findById(chatbotId);
        if (chatbotOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Chatbot not found.");
        }
        dataIngestionService.ingestUrlsInBackground(urls, chatbotOptional.get().getQdrantCollectionName());
        return ResponseEntity.ok("URL ingestion has started in the background. Your bot will be ready shortly.");
    }

    @GetMapping("/chatbots")
    public List<VaktaChatBot> getAllChatbots() {
        return vaktaRepository.findAll();
    }
}
