package com.Vakta.Vakta_Chat_Bot.Service;

import io.qdrant.client.QdrantClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies; // Import this
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DataIngestionService {

    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final WebClient webClient;

    @Autowired
    public DataIngestionService(QdrantClient qdrantClient,
                                EmbeddingModel embeddingModel,
                                WebClient.Builder webClientBuilder,
                                @Value("${firecrawl.local.url}") String firecrawlUrl) {
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;

        // --- THIS IS THE FINAL FIX ---
        // We configure the WebClient to accept a much larger amount of data (16MB).
        // This solves the DataBufferLimitException for large webpages.
        final int bufferSize = 16 * 1024 * 1024; // 16 MB
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(bufferSize))
                .build();

        this.webClient = webClientBuilder
                .exchangeStrategies(strategies) // Apply the new buffer size
                .baseUrl(firecrawlUrl)
                .build();
        // --- END OF FIX ---
    }

    @Async
    public void ingestUrlsInBackground(List<String> urls, String collectionName) {
        System.out.println("Starting background ingestion for " + urls.size() + " URL(s) into collection: " + collectionName);
        for (String url : urls) {
            try {
                String markdownContent = scrapeUrlWithFirecrawl(url);

                if (markdownContent == null || markdownContent.isEmpty()) {
                    System.out.println("Skipping empty content for URL: " + url);
                    continue;
                }

                List<String> chunks = chunkText(markdownContent, 250);
                List<Document> documents = chunks.stream()
                        .map(chunk -> new Document(chunk, Map.of("source", url)))
                        .collect(Collectors.toList());

                // Gentle batching for local Ollama
                int batchSize = 10;
                for (int i = 0; i < documents.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, documents.size());
                    List<Document> batch = documents.subList(i, end);

                    QdrantVectorStore tempVectorStore = QdrantVectorStore.builder(this.qdrantClient, this.embeddingModel)
                            .collectionName(collectionName)
                            .build();

                    tempVectorStore.add(batch);

                    System.out.println("Ingested batch " + (i / batchSize + 1) + " for URL: " + url);

                    TimeUnit.MILLISECONDS.sleep(500);
                }
                System.out.println("Successfully finished ingestion for URL: " + url);

            } catch (Exception e) {
                System.err.println("Failed to ingest URL in background: " + url);
                e.printStackTrace();
            }
        }
        System.out.println("Background ingestion task completed for collection: " + collectionName);
    }

    private String scrapeUrlWithFirecrawl(String url) {
        return webClient.post().uri("/v0/scrape").bodyValue(Map.of("url", url, "pageOptions", Map.of("onlyMainContent", true))).retrieve().bodyToMono(Map.class)
                .map(response -> (String) ((Map<?, ?>) response.get("data")).get("markdown")).block();
    }

    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int overlap = 50; // --- NEW: Each chunk will overlap by 50 characters ---

        for (int i = 0; i < text.length(); i += (chunkSize - overlap)) {
            int end = Math.min(text.length(), i + chunkSize);
            chunks.add(text.substring(i, end));
            if (end == text.length()) break;
        }
        return chunks;
    }
}