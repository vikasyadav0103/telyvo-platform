# TELYVO

**Evidence-based technology decision intelligence.**

TELYVO helps developers discover current technical information, research engineering choices with cited live sources, and discuss findings with other developers.

## Hackathon MVP

The five-hour MVP focuses on one end-to-end journey:

1. **Discover — SerpApi:** find current articles, news and technical resources for a technology or engineering question.
2. **Research — You.com:** produce a sourced analysis or comparison using live web information.
3. **Collaborate — Nexconn:** share the research in a real-time discussion room.

The three integrations must perform real operations in the final demo. Never fabricate search results, citations, chat events or provider status.

## Planned stack

- Backend: Java 21, Spring Boot 3, Maven
- Frontend: React, Vite, Material UI
- Persistence: PostgreSQL (optional for the five-hour demo if time is constrained)
- Integrations: SerpApi, You.com, Nexconn
- Testing: JUnit 5, Mockito, WireMock; frontend smoke tests where time permits
- Runtime: Docker Compose when practical

## Repository layout

```text
backend/       Spring Boot API
frontend/      React application
docs/          Architecture, setup, testing and demo notes
infra/         Local infrastructure and Docker assets
.env.example   Names of required environment variables (no secrets)
```

## Quick start

The application code is being added in stages. Start by copying `.env.example` to `.env` and filling in credentials locally. **Never commit `.env`, API keys, access tokens, or SDK secrets.**

Provider setup instructions and the five-hour build sequence are in [docs/SETUP.md](docs/SETUP.md). Credit allocation is in [docs/SERPAPI-CREDITS.md](docs/SERPAPI-CREDITS.md).

## Integration rules

- All provider calls happen server-side; never expose provider credentials to the browser.
- Validate and normalize provider responses before returning them to the UI.
- Set connection/read timeouts and handle 401, 403, 429, 5xx, timeouts and malformed payloads.
- Retry only transient failures; respect provider retry instructions and avoid retrying authentication or validation failures.
- Display source links only when returned by the provider and validated. Never invent citations.
- Cache identical discovery queries to conserve SerpApi credits.

## Current status

- [x] Repository and initial project documentation
- [ ] SerpApi credentials and credit redemption verified
- [ ] You.com API access verified
- [ ] Nexconn SDK credentials and minimal chat flow verified
- [ ] Backend discovery and research endpoints
- [ ] Frontend end-to-end demo
- [ ] Negative-path checks and final rehearsal

## Demo scenario

"Should we choose Java Spring Boot or Go for a new microservices platform?"

TELYVO discovers current resources, generates a cited comparison, then opens a Nexconn discussion for the team to review the evidence.

## Live integrations

- **Discover:** SerpApi Google Search, server-side API key, normalized source cards and five-minute in-memory cache.
- **Research:** You.com Research API with provider-returned answer and citations.
- **Discuss:** Nexconn Web Chat SDK group channel for real-time team messages.

See [docs/SETUP.md](docs/SETUP.md) for provider credentials, local setup and the demo path. All provider credentials are supplied through backend environment variables; the Nexconn App Secret is never exposed to the browser.
