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
Set `YOU_API_KEY`. The backend uses `POST https://api.you.com/v1/research`, sends the key in the `X-API-Key` header and requests `research_effort=lite` by default. Increase effort only when latency and credit budget permit. The response answer and provider-returned source URLs are normalized for the UI.

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
