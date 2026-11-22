# TELYVO MVP demo checklist

## Before the demo
- [ ] JDK 21, Maven 3.9+, Node.js 22+ and npm are available.
- [ ] `.env` exists locally and is not committed.
- [ ] SerpApi key is active and remaining credits are sufficient.
- [ ] You.com key has Research API scope and billing/credits are enabled.
- [ ] Nexconn App Key and App Secret are valid for the selected regional API host.
- [ ] Two distinct Nexconn user IDs are configured.
- [ ] A Nexconn group exists and contains both users.
- [ ] Backend and frontend start without errors.
- [ ] Docker Compose stack reports backend healthy and frontend available on port 8088 (if demonstrating containers).
- [ ] `mvn test` and `npm run build` pass.

## Live demo sequence
1. Ask: **Should we choose Java Spring Boot or Go for a new microservices platform?**
2. Discover current engineering resources through SerpApi.
3. Run You.com Research and show its conditional recommendation, comparison, trade-offs and source list.
4. Open the peer chat session in a second browser tab/profile.
5. Send a message from the primary session and show it arriving in the peer session.
6. Discuss one trade-off and state the team's decision.

## Failure-path rehearsal
- [ ] Missing SerpApi key produces a clear unavailable state.
- [ ] SerpApi 429 produces a rate-limit message.
- [ ] Missing You.com key produces a clear unavailable state.
- [ ] You.com 429 produces a rate-limit message.
- [ ] Missing Nexconn credentials/group prevents connection with a clear status.
- [ ] Disconnected chat disables message composition.
- [ ] No provider failure is replaced by fabricated results or citations.
- [ ] Research rate limit (3/min/IP) and discovery limit (20/min/IP) return clear 429 responses.

## Demo safety
Do not display `.env`, API keys, App Secret, access tokens, or raw provider request headers during screen sharing. The fixed primary/peer identities are demo-only and must not be used as production authentication. Provider limits are in-memory and are not a substitute for a distributed production rate limiter.
