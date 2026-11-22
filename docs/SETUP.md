# TELYVO setup checklist

## 1. Confirm provider access before coding

Do these checks at the start of the hackathon. Do not wait until the integration phase.

### SerpApi
1. Sign in to SerpApi.
2. For Mumbai participants, open the event coupon URL supplied by the organizers: https://serpapi.com/beyond9to5mumbai500
3. Redeem the 500 extra credits in the account.
4. Confirm the account dashboard reflects the credit grant.
5. Create/copy the API key and store it only in the local `.env` as `SERPAPI_KEY`.
6. Make one minimal Google Search API request and record the remaining-credit count before and after.

### You.com
1. Sign in to the You.com developer platform.
2. Enable the API/product required by the hackathon.
3. Create an API key and check its permissions, rate limits and spending controls.
4. Store it locally as `YOU_API_KEY`; set `YOU_API_BASE_URL` to the documented endpoint for the selected API.
5. Make one minimal live search/research request and preserve the actual source URLs returned.

### Nexconn
1. Register/sign in and obtain the Chat SDK credentials.
2. Read the SDK's current quick-start and authentication instructions.
3. Confirm whether the SDK requires a server-issued user token, app ID, API key, secret or webhook signature.
4. Store credentials locally; never put privileged secrets in React/Vite variables.
5. Test a two-user or two-client message exchange before building the full UI.

**Do not guess provider endpoints, request fields, token formats or SDK method names. Use the current official documentation and the credentials actually issued to the team.**

## 2. Local development

Prerequisites:
- JDK 21
- Maven 3.9+
- Node.js LTS and npm
- Git
- Docker Desktop (only if using local PostgreSQL/Redis)

Create a local secret file:

```bash
cp .env.example .env
```

Fill the values locally. Verify that `.env` is ignored by Git before the first commit.

## 3. Five-hour implementation sequence

| Time | Deliverable | Acceptance check |
|---|---|---|
| 0:00–0:20 | Redeem/check credits; verify all three provider credentials | One minimal request/connection per provider |
| 0:20–0:50 | Spring Boot + React skeleton and health endpoint | Both apps start |
| 0:50–1:35 | SerpApi discovery service and UI | Live search results displayed |
| 1:35–2:25 | You.com research service and citations | Live answer with provider-returned citations |
| 2:25–3:10 | Nexconn minimal group/direct chat | Two clients exchange a message |
| 3:10–4:10 | Connect the user journey and save/share research | Discover → research → discuss works |
| 4:10–4:40 | Negative tests and failure states | Key failure scenarios handled |
| 4:40–5:00 | Rehearse demo and verify usage/credits | Stable 3-minute demo |

## 4. Minimum negative-path checks

- Empty/oversized query returns a validation error.
- SerpApi 401/429/5xx/timeout produces a clear error state.
- You.com 401/429/5xx/timeout is handled without fabricated content.
- Missing or invalid citations are not displayed as verified sources.
- Nexconn disconnect/reconnect and unauthorized room access are handled.
- Duplicate chat events do not appear twice.
- No API key or privileged SDK secret appears in browser bundles, logs or Git history.

## 5. Definition of done

The MVP is complete only when a judge can:
1. Search a technology topic and see live SerpApi results.
2. Generate a cited research response through You.com.
3. Share the research context and exchange a real-time message through Nexconn.
4. Observe a sensible error state when an integration is unavailable.
