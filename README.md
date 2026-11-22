# TELYVO

**Evidence-based technology decision intelligence.**

TELYVO helps developers discover current technical information, research engineering choices with cited live sources, record decisions with rationale, and discuss findings with other developers.

## Product workflow

TELYVO follows a four-stage engineering decision workflow:

1. **Discover — SerpApi:** find current articles, news and technical resources for an engineering question.
2. **Research — You.com:** produce a sourced analysis or comparison using live web information.
3. **Decide — Decision Log:** record the question, research evidence, decision status and team rationale; export the record as Markdown.
4. **Discuss — Nexconn:** bring teammates into a real-time discussion around the evidence.

The three external integrations must perform real operations in the final demo. Never fabricate search results, citations, chat events or provider status.

## Technology stack

- Backend: Java 21, Spring Boot 3, Maven
- Frontend: React 19, Vite, Material UI
- Decision records: browser localStorage with Markdown export (no server-side persistence yet)
- Integrations: SerpApi, You.com Research API, Nexconn Chat SDK
- Testing: JUnit 5, Mockito and Spring MockRestServiceServer
- Runtime: Docker Compose with Spring Boot API and Nginx-served React build

## Repository layout

```text
backend/       Spring Boot API and provider integration tests
frontend/      React application and UI components
docs/          Architecture, setup, testing and demo notes
.env.example   Names of required environment variables (no secrets)
docker-compose.yml
```

## Quick start

The application source is available on the default `master` branch. Copy `.env.example` to `.env` and fill in credentials locally. **Never commit `.env`, API keys, access tokens, or SDK secrets.**

Provider setup, local development and Docker deployment instructions are in [docs/SETUP.md](docs/SETUP.md). The end-to-end demo sequence is in [docs/DEMO-CHECKLIST.md](docs/DEMO-CHECKLIST.md). SerpApi credit guidance is in [docs/SERPAPI-CREDITS.md](docs/SERPAPI-CREDITS.md).

## Integration and security rules

- All provider calls happen server-side; never expose provider credentials to the browser.
- Validate and normalize provider responses before returning them to the UI.
- Configure connection/read timeouts and handle authentication, quota, server, timeout and malformed-payload failures.
- Automatic retries are intentionally disabled for provider calls to avoid unexpected credit consumption and duplicate long-running research requests.
- Display source links only when returned by the provider and validated. Never invent citations.
- Cache identical discovery queries to conserve SerpApi credits.
- The Nexconn fixed-identity bootstrap is demo-only and must be disabled outside a controlled demo.

## Current implementation status

- [x] SerpApi discovery endpoint, normalization and in-memory query cache
- [x] You.com Research API integration with sanitized Markdown and source citations
- [x] Nexconn token issuance and browser chat SDK integration
- [x] Decision Log with status updates, rationale and Markdown export
- [x] Dockerfiles, Nginx reverse proxy and Docker Compose deployment
- [x] API exception mapping and per-IP in-memory rate limits
- [x] Mocked backend tests for discovery, research and Nexconn token issuance
- [x] TELYVO favicon and application metadata
- [ ] Provider credentials and live connectivity verified
- [ ] Maven tests and frontend production build executed in a network-enabled local environment
- [ ] Two-user Nexconn room smoke test and final demo rehearsal
- [ ] Production authentication, authorization and server-side decision persistence
- [ ] Shared team workspace, audit history and distributed rate limiting
- [ ] Production deployment, HTTPS, backups and monitoring

## Demo scenario

"Should we choose Java Spring Boot or Go for a new microservices platform?"

TELYVO discovers current resources, generates a cited comparison, lets the user record the decision and rationale, then opens a Nexconn discussion for the team to review the evidence.

## Current MVP boundaries

- Decision records are stored in the current browser only. They are not synchronized across users or devices.
- Nexconn group channels must be created in Nexconn and include the configured demo identities.
- Provider status indicates credential presence, not live connectivity or credential validity.
- In-memory rate limits reset on backend restart. Behind the bundled Nginx proxy, requests share the proxy's backend-visible IP.
- Maven tests, frontend builds, live provider credentials and two-user chat must be verified before declaring a release.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for system boundaries and [docs/SETUP.md](docs/SETUP.md) for operating instructions.
