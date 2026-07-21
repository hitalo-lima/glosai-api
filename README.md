# GlosAI API

Backend MVP para tradução de português para **GLOSA** - notação textual da Língua Brasileira de Sinais (LIBRAS).

## Pré-requisitos

- Java 21
- Maven 3.9+ (sem wrapper - `mvn` deve estar no `PATH`)

# Instruções

### 1. Variáveis de ambiente

Copie o arquivo de exemplo e preencha as credenciais:

```bash
cp .env.example .env
```

| Variável | Descrição |
|----------|-----------|
| `GROQ_API_KEY` | Chave da [Groq API](https://console.groq.com/keys) |
| `NEON_DB_URL` | URL de conexão do PostgreSQL (NeonDB) |
| `NEON_DB_USER` | Usuário do banco |
| `NEON_DB_PASSWORD` | Senha do banco |

### 2. Executar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Uso

### `POST /api/traducao`

Traduz um texto em português para GLOSA.

```json
{ "texto": "Você pode me ajudar a encontrar a farmácia mais próxima?" }
```

**Resposta** (200):
```json
{ "glosa": "FARMÁCIA PERTO VOCÊ AJUDAR PODER EU ENCONTRAR (PERGUNTA)" }
```

**Erros**:
| Status | Significado |
|--------|-------------|
| 400 | Texto inválido, vazio, >255 caracteres, ou resposta truncada |
| 422 | Texto fora do escopo de tradução (símbolos, código, outro idioma) |
| 429 | Muitas requisições (limite: 15/min por IP) |
| 500 | Erro interno ou falha na Groq API |

## Perfis

| Perfil | Comportamento |
|--------|---------------|
| `withdb` (padrão) | Persiste logs de tradução no PostgreSQL via JPA |
| `nodb` | Sem banco de dados - logs são descartados |

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=nodb
```

## Documentação

Swagger UI: [`/swagger-ui.html`](http://localhost:8080/swagger-ui.html)

## Stack

- Spring Boot 3.5 + Java 21
- Groq API (`llama-3.3-70b-versatile`)
- PostgreSQL / NeonDB (opcional)
- Caffeine (cache)
- Bucket4j (rate limiting)
- Springdoc OpenAPI
