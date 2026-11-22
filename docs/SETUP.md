# TELYVO setup checklist

## Prerequisites
- JDK 21 and Maven 3.9+
- Node.js 22+ and npm
- Git

## Local configuration
1. Copy `.env.example` to `.env`.
2. Set `SERPAPI_KEY` using the key from your SerpApi account.
3. Never commit `.env` or place provider secrets in Vite variables.
4. From `backend`, run `mvn spring-boot:run`.
5. From `frontend`, run `npm install && npm run dev`.
6. Open the Vite URL (normally http://localhost:5173).

## Implemented: live discovery
The React search form calls `GET /api/v1/discovery?query=...`. Spring Boot calls the SerpApi Google Search endpoint server-side and returns normalized title, URL, snippet, source and publication date fields.

- Search query length: 1–240 characters.
- Result limit: configurable, default 8 (maximum 20).
- Provider timeout: configurable, default 8 seconds for connect and read.
- Identical normalized queries are cached in memory for five minutes (up to 200 entries).
- Only HTTP/HTTPS result links are exposed to the browser.
- Missing credentials return `DISCOVERY_NOT_CONFIGURED`; provider failures return a stable error response.
- Search results are provider-returned web references, not independently fact-checked claims.

## Remaining integrations
You.com and Nexconn are not yet connected. Implement them only against the current official API/SDK contract and credentials actually issued to the team. Do not invent endpoints, citation structures or token formats.

## Smoke test
With the backend running and a valid `SERPAPI_KEY` configured:
```bash
curl "http://localhost:8080/api/v1/discovery?query=Spring%20Boot%20vs%20Go"
```
Without a key, expect HTTP 503 with code `DISCOVERY_NOT_CONFIGURED`. No live provider call is required for CI tests.
