# GlosAI API - Agent Guide

## Project

Spring Boot 3.5 / Java 21 app. Translates Portuguese text → **GLOSA** (textual notation for Brazilian Sign Language / LIBRAS) via the Groq API (`llama-3.3-70b-versatile`). Single Maven module, no wrapper - requires system `mvn`.

## Essential commands

```bash
mvn spring-boot:run                 # start on :8080
mvn test                            # single contextLoads test
mvn test -Dtest=GlosaiApplicationTests
```

## Profiles & persistence

| Profile | Behavior |
|---------|----------|
| `withdb` (default) | PostgreSQL (NeonDB), JPA logs translations |
| `nodb` | No database, logs are no-ops |

Override: `--spring.profiles.active=nodb` or `-Dspring-boot.run.profiles=nodb`.

## `.env` - **live credentials in repo**

`.env` contains working Groq API key and NeonDB credentials. It is **gitignored** but currently populated. **Do not commit or expose.** Use `.env.example` as the template.

## Architecture

- **`POST /api/tradução`** - single endpoint (`TraducaoController`). Body `{"texto": "..."}` (max 255 chars, validated).
- **CORS**: currently only `http://localhost:4200`.
- **Cache**: Caffeine (`traduções`, 500 entries). Key = lowercased, trimmed, punctuation-stripped text.
- **Rate limit**: 15 req/min per IP (Bucket4j), returns 429.
- **Persistence**: `LogTranslationService` interface switched by `@ConditionalOnProperty(app.persistence.enabled)`. Logs are saved `@Async` when DB is on.
- **Swagger**: `/swagger-ui.html` (springdoc-openapi).
- **All code in Portuguese**: classes, comments, commit messages, system prompt, error messages.
- **`@EnableAsync`** on main class; `@Async` on `JpaLogTranslationService.salvarLog`.
- **System prompt** is inline in `TraducaoService.systemPrompt()`. If the model returns `"ERRO_ESCOPO"`, the controller returns 422.

## Quirks

- No `mvnw` - Maven must be on `PATH`.
- No CI config found.
- Only one test (context-loads), needs a dummy Groq key (supplied via `@TestPropertySource`).
- `TextoNormalizador` strips trailing `!.,;` from cache keys - be aware when debugging cache misses.
- `@Valid` on request body triggers `MethodArgumentNotValidException` → 400.
- `ForaDeEscopoException` → 422. `RespostaTruncadaException` → 400.
