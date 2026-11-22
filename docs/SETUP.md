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
3. Obtain a user access token through Nexconn's documented server-side flow. For this MVP, supply the resulting demo user's token as `NEXCONN_USER_TOKEN` in the backend environment.
4. Set `NEXCONN_USER_ID` to the exact user ID bound to that token.
5. Create a group channel using Nexconn's console or SDK and add this user (and the second demo user); set its ID as `NEXCONN_GROUP_ID`.
6. Set `NEXCONN_APP_KEY` to the same App Key used to create the token.
7. Never put the App Secret in React, Vite variables or the browser bundle.

The backend exposes `GET /api/v1/chat/config` to return the browser-safe App Key, user access token, user ID and group ID at runtime. This endpoint is a demo bootstrap endpoint, not a production identity system. In production, protect it with application authentication and mint short-lived user tokens through Nexconn's signed server API.

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
