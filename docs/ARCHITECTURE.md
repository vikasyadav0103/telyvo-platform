# TELYVO architecture

## Product workflow

**Discover → Research → Decide → Discuss**

- SerpApi discovers current web resources.
- You.com Research synthesizes a cited answer.
- The decision log captures status and team rationale, with Markdown export.
- Nexconn provides real-time team discussion.

## Runtime architecture

The MVP is a modular monolith. It intentionally avoids microservices, Kubernetes, Redis and a database dependency for the hackathon.

```text
Browser
  |
  | React + Vite (development) / Nginx (container)
  |   - discovery and research UI
  |   - sanitized Markdown + citation links
  |   - browser-local decision log
  |   - Nexconn Web Chat SDK
  |
  +---- /api/* ----> Spring Boot REST API
                         |
                         +--> Discovery module ---> SerpApi
                         |      - input validation
                         |      - five-minute Caffeine cache
                         |
                         +--> Research module ----> You.com Research API
                         |      - decision brief prompt
                         |      - safe source URL filtering
                         |      - original citation index preservation
                         |
                         +--> Collaboration module -> Nexconn token API
                                - signed server-side token request
                                - fixed demo identities only
                                - per-IP issuance limit
```

Docker Compose builds the Spring Boot API and React production frontend. Nginx serves the SPA and proxies API traffic over a private Compose network. The backend container has an Actuator health check.

## Backend module boundaries

- `discovery`: validates search queries, calls SerpApi, filters non-HTTP(S) URLs and normalizes search results. Identical queries are cached in memory for five minutes.
- `research`: sends an evidence-first research prompt to You.com, normalizes the Markdown answer and preserves source indexes so citations continue pointing at the correct source even when an unsafe URL is filtered.
- `collaboration`: mints Nexconn browser access tokens server-side for two configured demo users. Nexconn owns message delivery and group membership.
- `common`: maps provider, validation and HTTP status errors to a stable JSON error envelope.
- `config`: configures provider-specific RestClients, CORS and in-memory per-IP request limits.

## Decision record storage

Decision records are currently stored in browser localStorage, capped at 50 records per browser. A record contains the question, research answer, source references, status and team rationale. Users can update status, delete records and export Markdown.

This is a single-browser MVP capability, not shared team persistence. A production implementation should add authenticated users, PostgreSQL-backed decision records, authorization, audit history and a migration strategy.

## Security boundaries

- Provider API keys and the Nexconn App Secret remain server-side.
- The browser receives only the Nexconn App Key, configured group/user IDs and a short-lived user token.
- The Nexconn bootstrap endpoint is enabled only when `NEXCONN_DEMO_MODE=true`; it exposes two fixed demo identities and is not production authentication.
- Discovery is limited to 20 requests/minute/IP, Research to 3 requests/minute/IP, and demo token issuance to 10 requests/minute/IP. These counters are in-memory. Behind the bundled Nginx proxy, API callers share the backend-visible proxy IP.
- Treat provider answers and retrieved web content as untrusted input. Research Markdown is rendered through a sanitizer, and source URLs are restricted to HTTP(S).
- Do not expose secrets in browser bundles, logs, error messages or screenshots.

## Failure and operational policy

- Configure connection and read timeouts per provider. The synchronous You.com timeout is sized for the supported effort tiers; Frontier is not enabled because it requires background polling.
- Map authentication, quota, provider and network failures to stable API errors. Do not return fake search results or citations.
- Provider rate limits are surfaced; automatic retries are not enabled because they could consume credits or duplicate long-running research requests.
- Provider status indicates credential presence only, not a successful live connectivity check.
- Maven tests, frontend production build, live credentials and two-user Nexconn messaging must be verified in a local runtime before release.
