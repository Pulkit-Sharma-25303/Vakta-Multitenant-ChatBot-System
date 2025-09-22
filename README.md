Vakta-Chatbot: A Multi-Tenant RAG Chatbot PlatformVakta-Chatbot is a full-stack application that allows users to create, manage, and deploy their own custom Retrieval-Augmented Generation (RAG) chatbots. Each chatbot can be trained on specific knowledge by ingesting content from website URLs. The platform is powered by a Java Spring Boot backend and local Large Language Models (LLMs) via Ollama, ensuring data privacy and cost-free operation.<!-- Placeholder: Replace with a screenshot of your admin.html --><!-- Placeholder: Replace with a screenshot of your chat.html -->Core FeaturesCreate Custom Chatbots: Generate unique chatbots, each with a dedicated and isolated knowledge base.URL-Based Knowledge Ingestion: Simply provide a list of website URLs to teach your chatbot.Data Isolation: Each chatbot's knowledge is stored in a separate, dedicated Qdrant vector collection, ensuring no data leakage between bots.Asynchronous Processing: Handles ingestion of large webpages in the background without blocking the user interface.Local LLM Integration: Powered by local models running on Ollama, allowing for private, offline, and cost-free AI processing.Simple Web Interface: A two-page frontend for administration (admin.html) and public chatting (chat.html).Tech StackCategoryTechnologies & ToolsBackendJava, Spring Boot, Spring AIAIOllama (llama3:8b, gemma:2b), RAG ArchitectureDatabasePostgreSQL (for chatbot metadata), Qdrant (for vectors)Web ScrapingFirecrawl (Self-Hosted)FrontendHTML, CSS, JavaScriptToolsMaven, Git, GitHub, PostmanHow It WorksThe application follows a modern RAG pipeline:Creation: An admin uses the web dashboard to create a new chatbot. The backend saves the bot's metadata to PostgreSQL and creates a corresponding unique collection in the Qdrant vector database.Ingestion: The admin provides URLs. The backend uses a local Firecrawl instance to scrape the content.Embedding: The scraped text is chunked and converted into vector embeddings by a local Ollama model (e.g., nomic-embed-text).Storage: These vectors are stored in the chatbot's dedicated Qdrant collection.Retrieval & Chat: When a user asks a question on the public chat page, the backend performs a similarity search in the correct Qdrant collection to find the most relevant context.Generation: The retrieved context, chat history, and the user's question are sent to a local Ollama chat model (e.g., llama3:8b) to generate a final, context-aware answer.Setup and InstallationPrerequisitesJava 17+Apache MavenPostgreSQL databaseOllama installed and running.Firecrawl installed and running locally.1. Download Local AI ModelsMake sure you have the necessary models downloaded in Ollama:ollama pull llama3:8b
ollama pull nomic-embed-text
2. Configure the ApplicationClone the repository and update the src/main/resources/application.properties file with your specific configuration:# PostgreSQL Database
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# Ollama Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3:8b
spring.ai.ollama.embedding.options.model=nomic-embed-text

# Qdrant Vector Database
spring.ai.vectorstore.qdrant.host=your_qdrant_host
spring.ai.vectorstore.qdrant.port=6334
spring.ai.vectorstore.qdrant.api-key=your_qdrant_api_key

# Local Firecrawl Instance
firecrawl.local.url=http://localhost:3002
3. Run the ApplicationYou can run the application using Maven:mvn spring-boot:run
The server will start on http://localhost:8080.UsageNavigate to the admin dashboard at http://localhost:8080/admin.html.Create a new chatbot by giving it a name.A shareable link for your chatbot will be generated.Use the "Ingest URLs" form to teach your new bot.Open the shareable link in a new tab to start chatting!
