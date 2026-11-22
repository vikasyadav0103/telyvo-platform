# SerpApi credit plan

The event coupon advertises **500 extra SerpApi credits** for Mumbai participants. Redeem it in the SerpApi account before running development searches:

https://serpapi.com/beyond9to5mumbai500

The coupon must be redeemed by the account holder. After redemption, confirm the balance in the SerpApi dashboard; do not assume the credits were applied.

## Planned budget (500-credit planning envelope)

| Purpose | Planned allocation |
|---|---:|
| Initial integration smoke tests | 20 |
| Feature development and response-shape checks | 120 |
| UI, filter and edge-case QA | 100 |
| Final demo rehearsal and live demonstration | 100 |
| Reserve for unexpected failures | 160 |
| **Total** | **500** |

This is a planning envelope, not a guarantee that every request costs one credit. Actual usage depends on the selected SerpApi engine, parameters and current account billing rules. Check the account dashboard and current SerpApi pricing/usage documentation before high-volume testing.

## Credit-saving controls

1. Use one focused query per user action; do not fan out into multiple engines by default.
2. Cache normalized results by a key derived from engine + query + location + filters.
3. Use a short hackathon TTL (for example, 10–15 minutes) for repeat searches.
4. Debounce search input and require submit; do not call the API on every keystroke.
5. Add backend rate limits and a configurable daily/request budget.
6. Do not automatically retry 400, 401, 403 or quota-related 429 responses.
7. Retry only transient failures with a small bounded retry policy; avoid retries that consume additional credits without user value.
8. Use saved fixtures/WireMock for automated tests. Never burn live credits in unit tests.
9. Add an admin/debug usage counter and log engine, timestamp, status and latency—but never log the API key.
10. Keep a reserve for the final judge-facing demonstration.

## Required usage evidence

Record before/after account balance for:
- One initial smoke test.
- A small batch of representative feature searches.
- Final demo rehearsal.

At the end, report actual usage from the SerpApi dashboard rather than estimating it from the number of UI clicks.
