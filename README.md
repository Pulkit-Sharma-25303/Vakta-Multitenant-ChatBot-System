# Vakta-Chatbot: A Multi-Tenant RAG Chatbot Platform

Vakta-Chatbot is a full-stack application that allows users to create, manage, and deploy their own custom Retrieval-Augmented Generation (RAG) chatbots. Each chatbot can be trained on specific knowledge by ingesting content from website URLs. The platform is powered by a Java Spring Boot backend and local Large Language Models (LLMs) via Ollama, ensuring data privacy and cost-free operation.
<!-- Placeholder: Replace with a screenshot of your admin.html -->
<!-- Placeholder: Replace with a screenshot of your chat.html -->
Core Features
Create Custom Chatbots: Generate unique chatbots, each with a dedicated and isolated knowledge base.
URL-Based Knowledge Ingestion: Simply provide a list of website URLs to teach your chatbot.
Data Isolation: Each chatbot's knowledge is stored in a separate, dedicated Qdrant vector collection, ensuring no data leakage between bots.
Asynchronous Processing: Handles ingestion of large webpages in the background without blocking the user interface.
Local LLM Integration: Powered by local models running on Ollama, allowing for private, offline, and cost-free AI processing.
Simple Web Interface: A two-page frontend for administration (admin.html) and public chatting (chat.html).
Tech Stack
Category
Technologies \& Tools
Backend
Java, Spring Boot, Spring AI
AI
Ollama (llama3:8b, gemma:2b), RAG Architecture
Database
PostgreSQL (for chatbot metadata), Qdrant (for vectors)
Web Scraping
Firecrawl (Self-Hosted)
Frontend
HTML, CSS, JavaScript
Tools
Maven, Git, GitHub, Postman
How It Works
The application follows a modern RAG pipeline:
Creation: An admin uses the web dashboard to create a new chatbot. The backend saves the bot's metadata to PostgreSQL and creates a corresponding unique collection in the Qdrant vector database.
Ingestion: The admin provides URLs. The backend uses a local Firecrawl instance to scrape the content.
Embedding: The scraped text is chunked and converted into vector embeddings by a local Ollama model (e.g., nomic-embed-text).
Storage: These vectors are stored in the chatbot's dedicated Qdrant collection.
Retrieval \& Chat: When a user asks a question on the public chat page, the backend performs a similarity search in the correct Qdrant collection to find the most relevant context.
Generation: The retrieved context, chat history, and the user's question are sent to a local Ollama chat model (e.g., llama3:8b) to generate a final, context-aware answer.
Setup and Installation
Prerequisites
Java 17+
Apache Maven
PostgreSQL database
[Ollama](https://ollama.com/) installed and running.
[Firecrawl](https://github.com/mendableai/firecrawl) installed and running locally.

1. Download Local AI Models
Make sure you have the necessary models downloaded in Ollama:
ollama pull llama3:8b
ollama pull nomic-embed-text
2. Configure the Application
Clone the repository and update the src/main/resources/application.properties file with your specific configuration:

# PostgreSQL Database

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

3. Run the Application
You can run the application using Maven:
mvn spring-boot:run

The server will start on http://localhost:8080.
Usage
Navigate to the admin dashboard at http://localhost:8080/admin.html.
Create a new chatbot by giving it a name.
A shareable link for your chatbot will be generated.
Use the "Ingest URLs" form to teach your new bot.
Open the shareable link in a new tab to start chatting!

can u change its format into readMe.md file

Here’s a properly formatted `README.md` version of your project:

***

# Vakta-Chatbot: A Multi-Tenant RAG Chatbot Platform

Vakta-Chatbot is a full-stack application that allows users to create, manage, and deploy their own custom Retrieval-Augmented Generation (RAG) chatbots. Each chatbot can be trained on specific knowledge by ingesting content from website URLs. The platform is powered by a **Java Spring Boot backend** and **local Large Language Models (LLMs) via Ollama**, ensuring data privacy and cost-free operation.

*Screenshot of `admin.html`*

*Screenshot of `chat.html`*

***

## Core Features

- **Create Custom Chatbots**: Generate unique chatbots, each with an isolated knowledge base.
- **URL-Based Knowledge Ingestion**: Provide website URLs to teach your chatbot.
- **Data Isolation**: Each chatbot has a dedicated Qdrant vector collection to prevent data leakage.
- **Asynchronous Processing**: Handles ingestion of large webpages in the background.
- **Local LLM Integration**: Powered by Ollama for private, offline, and cost-free AI.
- **Simple Web Interface**: Includes a two-page frontend (`admin.html` \& `chat.html`).

***

## Tech Stack

| Category | Technologies \& Tools |
| :-- | :-- |
| Backend | Java, Spring Boot, Spring AI |
| AI | Ollama (llama3:8b, gemma:2b), RAG Architecture |
| Database | PostgreSQL (for metadata), Qdrant (for vectors) |
| Web Scraping | Firecrawl (Self-Hosted) |
| Frontend | HTML, CSS, JavaScript |
| Tools | Maven, Git, GitHub, Postman |


***

## How It Works

The application follows a modern **RAG pipeline**:

1. **Creation**: Admin creates a chatbot via dashboard. Metadata is saved in PostgreSQL, and a unique Qdrant collection is created.
2. **Ingestion**: Admin submits URLs. Firecrawl scrapes website content.
3. **Embedding**: Scraped text is chunked and embedded using Ollama (e.g., `nomic-embed-text`).
4. **Storage**: Vectors are stored in Qdrant for that specific chatbot.
5. **Retrieval \& Chat**: User queries trigger similarity searches in Qdrant. Relevant context is fetched.
6. **Generation**: Context + chat history + question are passed to Ollama (`llama3:8b`) for answer generation.

***

## Setup and Installation

### Prerequisites

- Java 17+
- Apache Maven
- PostgreSQL
- [Ollama](https://ollama.com/) installed \& running
- [Firecrawl](https://github.com/mendableai/firecrawl) installed \& running locally


### 1. Download Local AI Models

```bash
ollama pull llama3:8b
ollama pull nomic-embed-text
```


### 2. Configure the Application

Edit `src/main/resources/application.properties`:

```properties
# PostgreSQL Database
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
```


### 3. Run the Application

```bash
mvn spring-boot:run
```

The server will start on:

```
http://localhost:8080
```


***

## Usage

1. Open **Admin Dashboard**: [http://localhost:8080/admin.html](http://localhost:8080/admin.html)
    - Create a chatbot and give it a name.
    - A shareable public link is generated.
2. **Ingest Knowledge**: Use the "Ingest URLs" form to train your bot with website content.
3. **Chat with Your Bot**: Open the chatbot’s link (e.g., `/chat.html?botId=xyz`) and start chatting.

***

