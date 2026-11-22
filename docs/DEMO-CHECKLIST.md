# TELYVO MVP demo checklist

## Before the demo

- [ ] JDK 21, Maven 3.9+, Node.js 22+ and npm are available (or Docker Compose v2).
- [ ] `.env` exists locally and is not committed.
- [ ] SerpApi key is active and remaining credits are sufficient.
- [ ] You.com key has Research API access and billing/credits are enabled.
- [ ] Nexconn App Key and App Secret are valid for the selected regional API host.
- [ ] Two distinct Nexconn user IDs are configured.
- [ ] A Nexconn group exists and contains both users.
- [ ] `NEXCONN_DEMO_MODE=true` is set only for this controlled demo.
- [ ] Backend and frontend start without errors.
- [ ] `mvn clean test` passes.
- [ ] `npm install && npm run build` passes.
- [ ] If demonstrating containers, `docker compose ps` reports backend healthy and frontend available on port 8088.

## Live demo sequence

1. Ask: **Should we choose Java Spring Boot or Go for a new microservices platform?**
2. Discover current engineering resources through SerpApi.
3. Open one or two original source links to show provenance.
4. Run You.com Research and show the recommendation, comparison, trade-offs and source list.
5. Click a numbered citation and verify it jumps to the matching original source index.
6. Save a decision record, choose a status and enter the team's rationale.
7. Export the record as Markdown and show that the answer and original source numbering are preserved.
8. Open the peer chat session in a second browser profile/tab.
9. Send a message from the primary session and show it arriving in the peer session.
10. Discuss one trade-off and state the team's decision.

## Failure-path rehearsal

- [ ] Missing SerpApi key produces a clear unavailable state.
- [ ] SerpApi 429 produces a rate-limit message.
- [ ] Missing You.com key produces a clear unavailable state.
- [ ] You.com 429 produces a rate-limit message.
- [ ] Missing Nexconn credentials/group prevents connection with a clear status.
- [ ] `NEXCONN_DEMO_MODE=false` disables the fixed-identity token endpoint.
- [ ] Disconnected chat disables message composition.
- [ ] Research limit (3/min/IP), discovery limit (20/min/IP) and chat-token limit (10/min/IP) return clear 429 responses.
- [ ] No provider failure is replaced by fabricated results or citations.

## Demo safety

Do not display `.env`, API keys, App Secret, access tokens or raw provider request headers during screen sharing. The fixed primary/peer identities are demo-only and must not be used as production authentication. Provider limits are in-memory and are not a substitute for a distributed production rate limiter. Decision history is browser-local; it is not shared with other users.
