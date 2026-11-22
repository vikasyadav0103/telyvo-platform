# TELYVO setup and operations

## 1. Prerequisites

For local development:
- JDK 21
- Maven 3.9+
- Node.js 22+ and npm
- Git

For container deployment:
- Docker Engine 25+ or Docker Desktop
- Docker Compose v2

## 2. Configure provider credentials

Copy `.env.example` to `.env` in the repository root and populate the credentials. Never commit `.env`.

### Environment loading for local development

Spring Boot does not load a plain `.env` file automatically.

**macOS / Linux**
```bash
set -a
source .env
set +a
```
Run the backend in the same shell session.

**Windows PowerShell**
```powershell
Get-Content .env | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object {
  $name, $value = $_.Split('=', 2)
  [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
}
```
Run the backend in the same PowerShell process.

### SerpApi

Set `SERPAPI_KEY`. TELYVO calls the Google Search engine at `https://serpapi.com/search`. Identical query strings are cached in memory for five minutes (up to 200 cache entries) to conserve credits.

### You.com Research API

Set `YOU_API_KEY`. The backend sends `POST https://api.you.com/v1/research` with the `X-API-Key` header and JSON fields `input` and `research_effort`. The default effort is `lite`; supported synchronous efforts are `lite`, `standard`, `deep` and `exhaustive`. `frontier` is intentionally not enabled because it requires background task polling. The configured read timeout defaults to 330 seconds to accommodate the longer synchronous tiers.

The research prompt asks for a conditional recommendation, comparison criteria, trade-offs, assumptions and a decision checklist. Answers are rendered as sanitized Markdown, and citation markers link to the source list returned by You.com. See the official [Research API reference](https://you.com/docs/api-reference/research/v1-research).

### Nexconn Chat SDK

1. Create a Nexconn application and copy its App Key and App Secret.
2. Set `NEXCONN_APP_KEY` and `NEXCONN_APP_SECRET`. The secret stays on the backend.
3. Set `NEXCONN_API_BASE_URL` to the regional API host shown in the Nexconn console. The sample default is Singapore.
4. Configure two distinct stable IDs in `NEXCONN_USER_ID` and `NEXCONN_PEER_USER_ID`.
5. Create a group channel in Nexconn, add both IDs, and set its ID as `NEXCONN_GROUP_ID`.
6. Keep `NEXCONN_DEMO_MODE=true` only for a controlled hackathon demo.

The backend issues short-lived browser-use access tokens through Nexconn's signed token API. The endpoint is rate-limited to ten token requests per minute per client IP and returns only the two configured demo identities. Set `NEXCONN_DEMO_MODE=false` to disable this fixed-identity endpoint. Production must replace it with an authenticated session-to-Nexconn identity mapping, membership authorization and distributed rate limiting.

## 3. Run locally

Start the backend after loading environment variables:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

In a second terminal:

```bash
cd frontend
npm install
npm run build
npm run dev
```

Open `http://localhost:5173`. The Vite development server proxies `/api` to Spring Boot at `http://localhost:8080`.

## 4. Run with Docker Compose

From the repository root, after creating `.env`:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f backend frontend
```

Open `http://localhost:8088` (or the port configured by `APP_PORT`). Nginx serves the React production build and proxies API requests to Spring Boot on the private Compose network. The backend is not published directly to the host.

Stop the stack:

```bash
docker compose down
```

The MVP does not require PostgreSQL or Redis. Decision records are stored in browser localStorage and can be exported as Markdown. They are not synchronized between users or devices.

## 5. API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/health` | Backend health |
| GET | `/api/v1/integrations/status` | Presence of provider configuration |
| GET | `/api/v1/discovery?query=...` | SerpApi live search |
| POST | `/api/v1/research` | You.com cited research; body: `{"query":"..." }` |
| GET | `/api/v1/chat/config?participant=primary` | Demo Nexconn token bootstrap |
| GET | `/api/v1/chat/config?participant=peer` | Peer demo Nexconn token bootstrap |

Provider status indicates whether required environment variables are present; it is not a live connectivity or credential-validity check.

## 6. Verification and demo rehearsal

### Automated checks

```bash
cd backend
mvn clean test

cd ../frontend
npm install
npm run build
```

### Live workflow

1. Enter a question such as “Should we choose Java Spring Boot or Go for microservices?”
2. Discover resources and open the original provider URLs.
3. Run You.com Research. Confirm Markdown formatting and citation markers link to the returned source list.
4. Save a decision record with status and team rationale.
5. Export the record as Markdown and confirm the evidence and sources are included.
6. Open the peer chat session in another browser profile/tab and test two-user messaging.
7. Test missing credentials and rate-limit paths without repeatedly spending live API credits.

### Known MVP boundaries

- Decision records are browser-local. Server-side persistence, team-wide history and audit trails are not implemented.
- The Nexconn fixed-identity endpoint is demo-only. Disable it outside the controlled demo.
- The Nexconn group must already exist and include both configured users.
- Provider calls are live; missing credentials or provider outages produce errors rather than mock results.
- Maven tests and frontend builds must be executed in a local environment before declaring a release. No CI workflow is used as a substitute for local validation.

## 7. Security checklist

- Never commit `.env`, API keys, Nexconn App Secret or access tokens.
- Keep provider credentials on the backend.
- Do not expose the demo chat bootstrap endpoint publicly; set `NEXCONN_DEMO_MODE=false` outside the hackathon environment.
- Terminate TLS at a trusted reverse proxy before exposing the application to the internet.
- Configure allowed origins, authentication, authorization, rate limiting and centralized secret management before production.
