# TELYVO provider setup

## Prerequisites
- JDK 21 and Maven 3.9+
- Node.js 22+ and npm
- Git

## Configure secrets
Copy `.env.example` to `.env` and populate the provider values. Never commit `.env`.

### SerpApi
Set `SERPAPI_KEY`. The backend calls `https://serpapi.com/search` using the Google engine. Results are cached for five minutes to reduce duplicate calls.

### You.com Research API
Set `YOU_API_KEY`. The backend uses `POST https://api.you.com/v1/research`, sends the key in the `X-API-Key` header and requests `research_effort=lite` by default. The request includes a decision-brief instruction asking for a conditional recommendation, comparison criteria, trade-offs, assumptions and a decision checklist. Increase effort only when latency and credit budget permit. The response answer and provider-returned source URLs are normalized for the UI. The implementation follows the official Research API reference: https://you.com/docs/api-reference/research/v1-research.

### Nexconn Chat SDK
1. Create a Nexconn application in the Developer Console.
2. Use the development App Key.
3. Set `NEXCONN_APP_SECRET` on the backend only. Never put the App Secret in React, Vite variables or the browser bundle.
4. Set `NEXCONN_API_BASE_URL` to the regional API host shown in your Nexconn Console (the example default is Singapore).
5. Set `NEXCONN_USER_ID` and `NEXCONN_PEER_USER_ID` to two distinct stable user IDs; set their display names if desired.
6. Create a group channel using Nexconn's console or SDK, add both users, and set its ID as `NEXCONN_GROUP_ID`.

The backend now calls Nexconn's signed `POST /v4/auth/access-token/issue` endpoint at runtime and returns the resulting browser-use access token with the App Key and room details. It signs each request server-side using the App Secret; the secret never leaves the backend. The demo supports two fixed identities: the default browser session uses the primary ID; open the peer session link to connect as the second user. The backend only issues tokens for these configured IDs. Production must bind identity selection to an authenticated application session and enforce group membership.

## Run locally
Terminal 1:
```bash
cd backend
mvn spring-boot:run
```

Terminal 2:
```bash
cd frontend
npm install
npm run dev
```

Open the Vite URL, normally http://localhost:5173.

## API endpoints
- `GET /api/v1/health` — backend health
- `GET /api/v1/discovery?query=...` — SerpApi live search
- `POST /api/v1/research` — You.com cited research; JSON body `{"query":"..." }`
- `GET /api/v1/chat/config` — browser-safe Nexconn SDK bootstrap config

## Verification checklist

Run these checks before the live demo; they require valid credentials and a configured Nexconn group.

### Backend tests
```bash
cd backend
mvn test
```

### Frontend production build
```bash
cd frontend
npm ci
npm run build
```

### Live provider smoke tests
1. Start Spring Boot and Vite using the commands above.
2. Open the workspace and confirm provider pills reflect server-side credential configuration (this is configuration presence, not a successful live health check).
3. Search a narrow technical question. Confirm results open the original SerpApi URLs.
4. Run You.com research. Confirm the answer and returned sources render; test a deliberately invalid API key and confirm the UI shows an error without fabricated output.
5. Open the peer session in a second browser profile/tab. Confirm both users join the pre-created Nexconn group and messages appear in both sessions.
6. Remove one provider key temporarily and confirm its endpoint returns a clear not-configured response.
7. Test provider rate limits or use mocked tests; do not burn live credits just to force a 429 response.

### Known demo constraints
- The Nexconn chat bootstrap endpoint issues tokens for two fixed demo identities selected by a query parameter. This is for a controlled demo only; do not expose it as a production identity system. Production must authenticate the caller, derive the Nexconn user ID from the session, authorize room membership, and rate-limit token issuance.
- Provider status pills indicate whether required environment variables are present. They do not prove that the external provider is reachable or that the credentials are valid.
- No automated deployment or build workflow is required to continue development. Build and test locally with the commands above.

## Demo path
1. Enter a technology decision such as “Spring Boot vs Go for microservices”.
2. Discover current web resources through SerpApi.
3. Synthesize the decision with You.com Research and inspect its returned sources.
4. Connect to the configured Nexconn group and exchange messages with a second member.

## Important constraints
- No provider credentials are hardcoded or exposed in frontend build-time variables.
- Research and discovery show provider-returned sources; TELYVO does not claim independent fact verification.
- Nexconn group must exist and include the connected user. A second participant needs their own valid token/user identity.
- External API calls require valid provider credentials and network access; they are not simulated.
