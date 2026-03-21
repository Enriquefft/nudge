# Nudge Architecture

AI-powered upsell suggestion assistant for Clover POS merchants.

---

## 1. System Overview

```
┌─────────────────────────────────────────┐
│           Android App (Kotlin)           │
│                                          │
│  InventoryConnector ─── loads menu       │
│  OrderConnector     ─── listens/writes   │
│  AiService          ─── builds prompts   │
│  BackendClient      ─── HTTP client      │
│  Room DB            ─── local storage    │
└──────────────┬──────────────────────────┘
               │ HTTPS  (Bearer token)
               ▼
┌─────────────────────────────────────────┐
│         Backend API (Go / Fly.io)        │
│                                          │
│  POST /api/register  (public)            │
│  POST /api/suggest   ─── AI proxy        │
│  POST /api/events    ─── analytics sink  │
│  GET  /api/config    ─── remote config   │
│                                          │
│  middleware: auth → rate-limit → CORS    │
└────────┬─────────────────┬──────────────┘
         │                 │
         ▼                 ▼
┌──────────────┐  ┌─────────────────────┐
│  Neon Postgres│  │  Z.ai  (GLM-4.7)   │
│              │  │                     │
│  merchants   │  │  /chat/completions  │
│  devices     │  │  (OpenAI-compat.)   │
│  events      │  └─────────────────────┘
│  config      │
└──────────────┘
```

The Android app runs on a Clover POS device (AOSP, no GMS). When a cashier adds an item to an order, the app calls the backend, which proxies the request to the Z.ai LLM using a server-held API key. The model returns a single complementary item suggestion from the merchant's menu; the cashier can add it to the order with one tap or dismiss it. All suggestion outcomes are stored locally in Room and periodically batched to the backend for cross-device analytics.

---

## 2. Build Flavors

| Flavor | App ID suffix | IS_DEMO | IS_CLOVER_BUILD | Purpose |
|--------|--------------|---------|-----------------|---------|
| `clover` | _(none)_ | false | true | Production APK deployed to a real Clover device. Uses `InventoryConnector` + `OrderConnector` from the Clover SDK. |
| `pilot` | `.pilot` | false | false | Sideloaded on any Android phone for pre-Clover testing. Menu loaded from a bundled template; cashier taps menu pills to simulate item additions. |
| `demo` | `.demo` | true | false | Scripted walkthrough for investor demos. Uses `DemoDataProvider` scenarios with fixed item sequences and fallback suggestions. Pre-seeds realistic stats. |

**Build commands**

```bash
# Release APK for Clover device
./gradlew assembleCloverRelease

# Debug APK for pilot testing on a regular Android phone
./gradlew assemblePilotDebug

# Demo APK (scripted scenarios)
./gradlew assembleDemoDebug
```

Signing uses V1 only (`enableV1Signing = true`, `enableV2Signing = false`) — a hard Clover requirement. Keystore credentials come from `local.properties`.

---

## 3. Android App Architecture

### Package structure

```
com.aleph.nudge
├── MainActivity.kt            — entry point; mode dispatch; suggestion lifecycle
├── StatsActivity.kt           — daily stats screen (shown / accepted / revenue)
├── NudgeApplication.kt        — Application subclass; service wiring; Sentry init
├── model/
│   ├── MenuItem.kt            — data class: id, name, price (cents), category, isModifier
│   └── Suggestion.kt          — data class: itemId, itemName, price (cents), reason
├── service/
│   ├── AiService.kt           — builds prompts, calls backend proxy or Z.ai directly
│   ├── BackendClient.kt       — HTTP client: register, proxySuggest, sendEvents, fetchConfig
│   ├── OrderObserver.kt       — wraps OrderConnector; detects new line items; adds items
│   ├── InventoryService.kt    — wraps InventoryConnector; fetches and caches menu items
│   ├── CustomerDataService.kt — wraps CustomerConnector; fetches customer order history
│   ├── DemoDataProvider.kt    — hardcoded demo scenarios, fallback suggestions, menu
│   └── PilotMenuProvider.kt   — bundled menu templates for pilot mode
├── data/
│   ├── StatsManager.kt        — reads/writes daily_stats via Room
│   ├── UpsellHistoryManager.kt — reads/writes upsell_pairs; produces history summary string
│   └── db/
│       ├── NudgeDatabase.kt   — Room database singleton (nudge.db)
│       ├── DailyStatsEntity.kt
│       ├── UpsellPairEntity.kt
│       ├── SuggestionEventEntity.kt
│       ├── StatsDao.kt
│       ├── UpsellPairDao.kt
│       └── SuggestionEventDao.kt
└── ui/
    └── SuggestionCardView.kt  — animated card overlay: item name, price, reason, two buttons
```

### Key class responsibilities

| Class | Responsibility |
|-------|---------------|
| `NudgeApplication` | Initializes all services on startup; determines `AppMode` from build config flags; triggers background device registration; seeds demo data |
| `MainActivity` | Owns the UI lifecycle; dispatches to mode-specific setup methods; orchestrates the suggestion flow including debounce, AI call, card display, and outcome recording |
| `AiService` | Holds the in-memory menu context (`knownItems` map); builds the OpenAI-compatible JSON payload; tries backend proxy first, falls back to direct Z.ai; validates the returned `item_id` against `knownItems` |
| `BackendClient` | Persists `api_token` and `merchant_id` in SharedPreferences after registration; sends all authenticated requests with `Authorization: Bearer <token>`; returns `null` on any failure so callers can fall back |
| `OrderObserver` | Holds a live `OrderConnector`; tracks `knownLineItemIds` to detect net-new items only; dispatches to `onNewItem` callback on the main thread; also exposes `addItemToOrder()` for the accept path |
| `InventoryService` | Connects to `InventoryConnector` on a background thread; returns a flat list of `MenuItem` including modifiers |
| `CustomerDataService` | Optional; disabled by default (`customerPersonalizationEnabled = false`); connects to `CustomerConnector` to fetch customer order history for prompt personalization |
| `StatsManager` | Upserts `DailyStatsEntity` rows keyed by `yyyy-MM-dd`; exposes today's accepted count and revenue total |
| `UpsellHistoryManager` | Upserts `UpsellPairEntity` rows keyed by `"trigger\u001Fsuggested"`; assembles a history summary string injected into the AI prompt |
| `SuggestionCardView` | Custom `FrameLayout`; slide-in/fade-in on `show()`; slide-out/fade-out on dismiss; auto-dismiss timer (15 s); exposes `setOnAddClickListener` and `setOnDismissClickListener` |
| `DemoDataProvider` | Returns scripted `DemoScenario` objects (current order items, new item name, fallback suggestion) and a fixed menu; also returns optional customer context strings per scenario index |
| `PilotMenuProvider` | Returns bundled `MenuTemplate` objects (e.g., "Coffee Shop", "Burger Joint") selectable at first launch |

### Data flow: item added → suggestion displayed

```
Staff taps item on Clover register
         │
         ▼
OrderConnector fires OnOrderUpdated callback
         │
OrderObserver.checkForNewItems()
  - fetches current order via getOrder()
  - diffs against knownLineItemIds
  - calls onNewItem(orderId, itemNames) on main thread
         │
         ▼
MainActivity.onNewItemAdded()
  - cancels any pending suggestionJob
  - starts new coroutine with 500 ms debounce
         │
         ▼  (after debounce)
UpsellHistoryManager.getHistorySummary()  ←── Room query (IO dispatcher)
         │
         ▼
AiService.getSuggestion()
  - BackendClient.proxySuggest()  ──► POST /api/suggest (backend)
      (on failure / null)              backend calls Z.ai, returns verbatim
  - or direct Z.ai call (fallback)
         │
         ▼
AiService.parseResponse()
  - extracts JSON from content field
  - validates item_id against knownItems
  - falls back to name match if ID mismatch
         │
         ▼
MainActivity: check suggestion not already in order
         │
         ▼
SuggestionCardView.show(suggestion)  ──► slide-in animation
         │
    ┌────┴────┐
  Accept    Dismiss
    │          │
    ▼          ▼
OrderObserver  StatsManager.recordDismissed()
.addItemToOrder()  UpsellHistoryManager.recordDismissed()
StatsManager.recordAccepted()
UpsellHistoryManager.recordAccepted()
```

### Local Room database

Database file: `nudge.db` (SQLite, single instance via `NudgeDatabase.getInstance()`).

| Table | Key columns | Purpose |
|-------|-------------|---------|
| `daily_stats` | `date` (PK, yyyy-MM-dd), `shown`, `accepted`, `dismissed`, `revenueCents` | Per-day suggestion outcome counters displayed on the stats screen |
| `upsell_pairs` | `pairKey` (PK, `"trigger\u001Fsuggested"`), `accepted`, `dismissed`, `lastUpdated` | Tracks accept/dismiss counts per item pair; summarized into text injected into the AI prompt |
| `suggestion_events` | `id` (autoincrement), `eventType`, `triggerItems` (JSON), `suggestedItem`, `priceCents`, `synced` | Raw event log; `synced=false` rows are candidates for batch upload to the backend |

### Concurrency

All async work uses Kotlin coroutines. `NudgeApplication` uses a detached `CoroutineScope(Dispatchers.IO + SupervisorJob())` for background registration. `MainActivity` uses `activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())` — cancelled in `onDestroy()`. `OrderObserver` has its own `CoroutineScope(Dispatchers.Main + SupervisorJob())` cancelled in `stopObserving()`. Room DAOs are called with `withContext(Dispatchers.IO)`. Network calls inside `AiService` and `BackendClient` use `withContext(Dispatchers.IO)` or are already blocking (OkHttp) called from an IO coroutine.

---

## 4. Backend API

### Stack

- Language: Go 1.23
- HTTP: standard library `net/http` (no framework)
- Database driver: `github.com/jackc/pgx/v5` (connection pool)
- Database: Neon Postgres (serverless)
- Deployment: Fly.io (`nudge-api`, region `mia`)
- Logging: `log/slog` JSON to stdout

### Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | None | Liveness check; returns `{"status":"ok"}` |
| `POST` | `/api/register` | None | Device registration; creates merchant + device rows; returns `api_token` and `merchant_id` |
| `POST` | `/api/suggest` | Bearer | AI proxy; forwards OpenAI-format request body to Z.ai using per-merchant or global API key; returns raw Z.ai response |
| `POST` | `/api/events` | Bearer | Batch analytics ingestion; validates event types (`shown`/`accepted`/`dismissed`); inserts in a single transaction |
| `GET` | `/api/config` | Bearer | Returns merchant remote config: `ai_model`, `feature_flags` (JSONB), `system_prompt_override` |

Middleware stack applied in order (outermost first): request logger → CORS → auth → rate limiter → mux.

### Database schema

```sql
merchants (
    id TEXT PK,           -- gen_random_uuid()
    name TEXT,
    created_at TIMESTAMPTZ
)

devices (
    id TEXT PK,           -- Clover/Android device ID
    merchant_id TEXT,     -- FK → merchants
    api_token TEXT UNIQUE -- 32 random bytes hex-encoded
    platform TEXT,        -- "android"
    app_version TEXT,
    last_seen_at TIMESTAMPTZ
)

suggestion_events (
    id BIGSERIAL PK,
    device_id TEXT,       -- FK → devices
    merchant_id TEXT,     -- FK → merchants
    event_type TEXT,      -- CHECK IN ('shown','accepted','dismissed')
    trigger_items TEXT[], -- array of item names that triggered the suggestion
    suggested_item TEXT,
    suggested_item_id TEXT,
    price_cents BIGINT,
    ai_model TEXT,
    response_time_ms INT,
    created_at TIMESTAMPTZ
)
-- Indexes: (merchant_id, created_at), (device_id, created_at)

merchant_config (
    merchant_id TEXT PK,  -- FK → merchants
    ai_model TEXT,        -- default 'glm-4.7-flash'
    ai_base_url TEXT,     -- default 'https://api.z.ai/api/paas/v4'
    ai_api_key TEXT,      -- per-merchant key (nullable; falls back to env var)
    feature_flags JSONB,
    system_prompt_override TEXT,
    max_requests_per_minute INT  -- default 30
)
```

### Authentication flow

1. On first launch `NudgeApplication` calls `BackendClient.register()` in a background coroutine (fire-and-forget).
2. `POST /api/register` receives `device_id` (Android `ANDROID_ID`), `merchant_name`, `app_version`, `platform`.
3. Backend looks up the device; if it already exists it returns the existing token (idempotent).
4. Otherwise it upserts a merchant by name, inserts the device row, and returns the auto-generated `api_token` (64-char hex).
5. The token is stored in SharedPreferences (`nudge_backend` prefs). All subsequent requests send `Authorization: Bearer <token>`.
6. Auth middleware validates the token against the `devices` table and injects `device_id` and `merchant_id` into request context. `last_seen_at` is updated asynchronously.
7. If registration fails or the token is not yet available, `BackendClient` returns `null` from `proxySuggest()` and `AiService` falls back to a direct Z.ai call using the API key baked into the APK at build time.

### Rate limiting

Per-merchant token bucket, default 30 requests per minute. Implemented in-process with `sync.Map` of buckets. Tokens refill proportionally to elapsed time (not on a fixed interval). Stale buckets (idle > 10 minutes) are cleaned up every 5 minutes. The per-merchant limit is configurable via `merchant_config.max_requests_per_minute`. Requests from unauthenticated paths (register, health) bypass rate limiting. Exceeded requests receive HTTP 429 with `Retry-After: 60`.

### Deployment

```
Fly.io app:    nudge-api
Region:        mia (Miami)
Port:          8080 (internal), HTTPS enforced externally
Health check:  GET /health every 30 s
Auto-scaling:  min 0 machines (spun down when idle, auto-start on request)
```

---

## 5. AI Integration

### Provider

Z.ai, model `glm-4.7-flash`. Z.ai exposes an OpenAI-compatible `/chat/completions` endpoint. The base URL (`https://api.z.ai/api/paas/v4`) and model name are set as `BuildConfig` fields in `defaultConfig` and can be overridden per merchant via `merchant_config`.

### Proxy pattern

```
App (AiService)
  │
  ├─── 1st attempt: POST /api/suggest (backend proxy)
  │         Backend looks up merchant config for api_key / base_url
  │         Backend forwards body verbatim to Z.ai
  │         Backend returns Z.ai response verbatim
  │
  └─── fallback (if backend unavailable or returns null):
            Direct POST to Z.ai /chat/completions
            Uses ZAI_API_KEY from BuildConfig (baked in at build time)
```

The proxy path is preferred because the API key lives only on the server. The direct fallback ensures the app keeps working if the backend is unreachable (Fly.io cold start, network partition, etc.).

### Prompt structure

**System message** (built once at startup from inventory):

```
You are Nudge, an upselling assistant for a POS system.

This merchant's menu:
- <item_id>: <name> $<price> [<category>]
...

Available modifiers:
- <mod_id>: <name> $<price> (<modifier_group>)
...
```

**User message** (built per suggestion):

```
Current order: <item1>, <item2>, ...

[Upsell history — if available:]
Previously accepted: X → Y (N times), ...
Previously dismissed: X → Z (N times), ...

[Customer context — if available:]
This customer has previously ordered: ...

Suggest ONE complementary item or modifier from this merchant's menu.

Return ONLY valid JSON:
{ "item_id": "id", "item_name": "name", "price": "X.XX", "reason": "short phrase" }

Rules:
- Must be an item/modifier from the menu above (use exact item_id)
- Must NOT be already in the order
- Prefer add-ons and modifiers > sides > drinks > desserts > mains
- Reason must be conversational, specific, under 10 words
- Avoid items with low acceptance rates; favor items that customers frequently accept
```

Request parameters: `max_tokens: 256`, `temperature: 0.7`.

### Response validation

`AiService.parseResponse()` extracts the JSON block from the model's content string (substring between first `{` and last `}`). It then checks:

1. `item_id` exists in the `knownItems` map (populated from inventory at startup).
2. If the ID is not found, falls back to a case-insensitive name match against `knownItems`.
3. If neither matches, the suggestion is discarded and `null` is returned — the app shows nothing rather than hallucinating an item.

---

## 6. Data Flow

### Suggestion lifecycle

```
Item added to order
       │
       ▼
500 ms debounce (cancels if another item arrives)
       │
       ▼
UpsellHistoryManager → assemble history summary (Room, IO thread)
       │
       ▼
AiService.getSuggestion()
  → BackendClient.proxySuggest() → backend → Z.ai
  → (fallback) direct Z.ai
       │
       ▼
parseResponse() → validate item_id against knownItems
       │
       ▼ (null = discard; no card shown)
Check: is suggested item already in order? (discard if yes)
       │
       ▼
SuggestionCardView.show()
       │
  ┌────┴────┐
Accept    Dismiss / 15 s timeout
  │          │
  ▼          ▼
OrderConnector    StatsManager.recordDismissed()
.addCustomLineItem()  UpsellHistoryManager.recordDismissed()
StatsManager.recordAccepted(priceCents)
UpsellHistoryManager.recordAccepted()
```

### Analytics sync

Suggestion outcomes are written to `SuggestionEventEntity` in Room with `synced = false`. `BackendClient.sendEvents()` posts unsynced rows as a batch to `POST /api/events`. The backend inserts them into `suggestion_events` in a single transaction. On success the app marks those rows `synced = true`.

Note: the sync trigger (manual, periodic, or on-close) is not yet wired in the current codebase. The Room table and backend endpoint are both implemented and ready; the scheduling call is the remaining integration step.

### Remote config

`BackendClient.fetchConfig()` calls `GET /api/config` and receives `ai_model`, `feature_flags`, and an optional `system_prompt_override`. This is intended to be fetched on startup and cached locally. The endpoint is implemented; consumption of the config response in the app startup path is a remaining integration step.

---

## 7. Security

| Concern | Mechanism |
|---------|-----------|
| AI API key exposure | Key stored server-side only; not in the APK for the proxy path. Direct-fallback APK key is a secondary key with low quota, present only in the `clover` and `pilot` release builds. |
| Code obfuscation | R8/ProGuard enabled in all release build types (`isMinifyEnabled = true`, `isShrinkResources = true`) |
| Transport | HTTPS enforced by Fly.io (`force_https = true`). Backend has 10 s read timeout, 30 s write timeout. |
| Authentication | Bearer token per device, validated on every authenticated request. No token = 401. |
| Rate limiting | 30 req/min per merchant (configurable); per-merchant token bucket in-process. Exceeding returns 429. |
| APK signing | V1 signing only (Clover requirement). Keystore credentials in `local.properties` (not checked in). |
| Crash reporting | Sentry Android SDK (`sentry-android:7.3.0`); 20% trace sample rate; DSN from `local.properties`. |
| CORS | Backend allows all origins (`*`) — acceptable for a mobile → backend API; no browser-based credential flows. |

---

## 8. Development Setup

### Prerequisites

| Tool | Version / Notes |
|------|----------------|
| Go | 1.23+ |
| Android Studio | Ladybug or newer; SDK 29 + 34 installed |
| Android SDK | compileSdk 34, targetSdk 29 |
| JDK | 17 (for toolchain); code targets Java 1.8 |
| Nix + direnv | Optional; `flake.nix` in project root for reproducible env |
| Fly CLI | For backend deployment (`flyctl`) |

### Running the backend locally

```bash
cd /home/hybridz/Projects/nudge/backend

export DATABASE_URL="postgres://user:pass@host/nudge"
export DEFAULT_AI_API_KEY="<your-z.ai-key>"
export PORT=8080

go run ./...
# Server starts on :8080
```

### Running the Android app

1. Copy `local.properties.example` to `local.properties` and fill in:

```
ZAI_API_KEY=<your-z.ai-key>
SENTRY_DSN=<your-sentry-dsn>        # optional
BACKEND_URL=https://nudge-api.fly.dev  # or http://10.0.2.2:8080 for local backend
RELEASE_STORE_PASSWORD=<keystore-password>
RELEASE_KEY_PASSWORD=<key-password>
```

2. Open `app/Soporte Desarrollo/` in Android Studio.
3. Select a build variant (e.g., `demoDebug` to run without a Clover device).
4. Run on the Clover emulator or a physical Clover device for `cloverRelease`.

Clover emulator setup: https://docs.clover.com/dev/docs/setting-up-an-android-emulator

### Deploying the backend to Fly.io

```bash
cd /home/hybridz/Projects/nudge/backend

# First deploy
flyctl launch

# Set secrets (one-time)
flyctl secrets set DATABASE_URL="postgres://..."
flyctl secrets set DEFAULT_AI_API_KEY="..."

# Deploy updates
flyctl deploy
```

The `fly.toml` targets app name `nudge-api` in region `mia`. Health check runs every 30 s at `GET /health`. Machines auto-stop when idle and auto-start on incoming requests (min 0 machines running).

### Database setup

```bash
# Apply schema to Neon (or any Postgres instance)
psql "$DATABASE_URL" -f backend/schema.sql
```

### Environment variables reference

| Variable | Where set | Required | Description |
|----------|-----------|----------|-------------|
| `DATABASE_URL` | Fly.io secret / shell | Yes (backend) | Postgres connection string |
| `DEFAULT_AI_API_KEY` | Fly.io secret / shell | Yes (backend) | Z.ai API key used when no per-merchant key is configured |
| `PORT` | `fly.toml` env | No | HTTP port; defaults to 8080 |
| `ZAI_API_KEY` | `local.properties` | Yes (app, direct fallback) | Z.ai API key baked into APK for direct fallback path |
| `BACKEND_URL` | `local.properties` | No | Backend base URL; defaults to `https://nudge-api.fly.dev` |
| `SENTRY_DSN` | `local.properties` | No | Sentry project DSN for crash reporting |
| `RELEASE_STORE_PASSWORD` | `local.properties` | Yes (release builds) | Keystore password for APK signing |
| `RELEASE_KEY_PASSWORD` | `local.properties` | Yes (release builds) | Key password for APK signing |
