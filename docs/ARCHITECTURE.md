# TELYVO MVP architecture

## Product workflow

Discover → Research → Discuss.

- SerpApi discovers current web resources.
- You.com produces live research with source references.
- Nexconn provides real-time developer discussion.

## Architecture decision

Use a modular monolith for the five-hour MVP. Do not introduce microservices, Kubernetes, Redis or a complex event bus during the hackathon unless a concrete requirement demands them.

```text
React UI
   |
   v
Spring Boot REST API
   |
   +--> Discovery module ----> SerpApi
   |
   +--> Research module -----> You.com
   |
   +--> Collaboration module -> Nexconn Chat SDK
   |
   +--> Optional persistence -> PostgreSQL
```

## Backend boundaries

- `discovery`: validates queries, calls SerpApi, normalizes results and applies caching/rate limits.
- `research`: submits a user question to You.com, normalizes answer/source data and enforces citation rules.
- `collaboration`: associates an application research session with a Nexconn room; Nexconn owns real-time message delivery.
- `common`: global error response, validation, correlation IDs and safe logging.

## Security boundaries

- Provider credentials remain server-side.
- Browser receives only the minimum client-safe Nexconn configuration or a short-lived user token minted according to Nexconn's documented flow.
- Treat search results and retrieved web content as untrusted input.
- Do not render raw HTML from provider responses.
- Validate citation URLs and escape displayed text.
- Do not claim source verification beyond what the application actually checks.

## Failure policy

- Configure connection and response timeouts.
- Map provider errors to stable application error codes.
- Retry only transient network/5xx failures, with a small bounded policy.
- Respect provider Retry-After instructions.
- Do not retry authentication, validation, permission or quota failures.
- Return partial results only when clearly labelled; never silently substitute fabricated data.
