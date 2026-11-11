# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Course project (SE2032 / HSF302): a single-page chat web app with two separate modes, **AI thường** (plain chat) and **AI RAG** (answers grounded in uploaded `.txt` files). Built with Spring Boot 4.1 (Spring MVC + Thymeleaf, Java 17) and LangChain4j 1.11. UI text, prompts and comments are in Vietnamese.

## Commands

Two API keys must be set as env vars, or the app fails at startup (`AiConfig.requireKey`): `GROQ_API_KEY` (chat) and `GEMINI_API_KEY` (embeddings).

```bash
GROQ_API_KEY=... GEMINI_API_KEY=... ./mvnw spring-boot:run   # then open http://localhost:8080/chat
./mvnw clean package                                          # build jar
```

There is no `src/test` yet, and no `spring-boot-starter-test` dependency in `pom.xml`. Add it before writing tests; a single test then runs with `./mvnw test -Dtest=ClassName#method`.

Manual test data lives in `sample-docs/`. The README lists the expected questions, including one that must return "not found" (`laptop`).

## Architecture

The layering is `controller -> service interface -> service/impl`, the same pattern as the course's `shop-dieuao` project. Keep it.

- `config/AiConfig` defines the three shared beans: `ChatModel` (Groq through `OpenAiChatModel` with Groq's OpenAI-compatible `base-url`), `EmbeddingModel` (Gemini), and `EmbeddingStore<TextSegment>` (`InMemoryEmbeddingStore`, so all data is lost on restart).
- `ChatController` only routes requests. The `mode` request param (`ChatMode.NORMAL`/`RAG`, bound by enum name) picks the service. Each mode keeps its own chat history in `HttpSession` under the key `history_<MODE>`, which is why `ChatMessage` is `Serializable`. Every POST ends in a PRG redirect to `/chat?mode=X#end`. There is no JavaScript: each message is a full page reload.
- `NormalChatServiceImpl` sends the question straight to `ChatModel`.
- `DocumentServiceImpl` splits uploaded text with `DocumentSplitters.recursive(chunkSize, overlap)`, embeds the chunks with Gemini, and stores them with metadata `source=<fileName>`. Re-uploading the same file name first removes the old chunks by that metadata key. The document list is a `synchronized` in-memory map shared by **all users**, not per session.
- `RagChatServiceImpl` embeds the question, searches the store (`top-k`, `min-score`), builds a `<context>` prompt telling the model to answer only from that context, and returns a `RagAnswer` with the answer plus formatted source previews (`[score] file: preview`).
- Every model, chunking and retrieval setting lives under `app.ai.*` / `app.rag.*` in `application.properties` and is injected with `@Value`.

Conventions: field `@Autowired` injection, hand-written DTOs with getters (no Lombok), and service errors caught in the controller and shown to the user as error messages.

## Known limits (from README)

- Only `.txt` uploads (5MB max); the vector store lives in RAM.
- Each question is independent: no conversation memory is sent to the model.
- Common errors: 429 means the free quota is used up; 404 "model not found" means the model name in `application.properties` needs changing; if dependencies fail to resolve, bump `langchain4j.version` (all three LangChain4j modules share it).
