# Nudge

AI-powered upsell assistant for Clover POS merchants. Nudge watches the active order and suggests complementary items in real time — one tap to add, one tap to dismiss. It learns from cashier behavior and gets smarter with every transaction.

![Nudge](nudge-cover.png)

## How It Works

1. Cashier adds an item to a Clover order
2. Nudge detects the new line item via Clover's `OrderConnector`
3. AI analyzes the merchant's full menu + current order context
4. A suggestion card appears with the item, price, and reasoning
5. Cashier taps **Add** (item goes into the order) or **Dismiss**
6. Accept/dismiss history feeds back into future suggestions

Latency target: under 2 seconds from item scan to suggestion.

## Architecture

```
┌──────────────────────────────────┐
│     Android App (Kotlin)         │
│  Clover SDK · Room DB · OkHttp   │
└──────────────┬───────────────────┘
               │ HTTPS (Bearer token)
               ▼
┌──────────────────────────────────┐
│     Backend API (Go / Fly.io)    │
│  /register · /suggest · /events  │
│  /config · auth · rate-limit     │
└───────┬──────────────┬───────────┘
        ▼              ▼
  Neon Postgres    Z.ai LLM
  (analytics)    (GLM-4.7-Flash)
```

## Project Structure

```
app/          Android app (Kotlin, 3 build flavors)
backend/      Go API server (Fly.io)
landing/      Astro landing page (nudge.404tf.com)
deck/         Pitch deck (React/Vite)
video/        Video production assets
research/     Market research & competitive analysis
```

## Build Flavors

| Flavor | App ID | Purpose |
|--------|--------|---------|
| `clover` | `com.aleph.nudge` | Production — runs on real Clover POS hardware |
| `pilot` | `com.aleph.nudge.pilot` | Standalone Android app for field testing alongside any POS |
| `demo` | `com.aleph.nudge.demo` | Scripted scenarios for sales demos |

```bash
./gradlew assembleCloverRelease   # Production APK
./gradlew assemblePilotDebug      # Field testing APK
./gradlew assembleDemoDebug       # Demo APK
```

## Tech Stack

- **Android**: Kotlin, Room, Coroutines, Clover SDK v329
- **Backend**: Go, net/http, pgx, Neon Postgres (serverless)
- **AI**: Z.ai GLM-4.7-Flash via OpenAI-compatible API
- **Landing**: Astro, Vercel
- **Deployment**: Fly.io (backend), Clover App Market (Android)
- **CI/CD**: GitHub Actions
- **Crash Reporting**: Sentry

## Development

### Prerequisites

This project uses [Nix](https://nixos.org/) with [direnv](https://direnv.net/) for a reproducible dev environment. After cloning:

```bash
direnv allow    # Loads JDK 17, Go, Gradle, flyctl, etc.
```

Or install manually: JDK 17, Go 1.23+, Gradle, Android SDK (target 29, min 21).

### Configuration

Copy `.env.example` to `.env` and fill in:

```
ZAI_API_KEY=        # Z.ai API key
SENTRY_DSN=         # Sentry DSN
BACKEND_URL=        # Backend endpoint
DATABASE_URL=       # Neon Postgres connection string
```

For Android builds, create `app/Soporte Desarrollo/local.properties` with the same values plus keystore credentials.

### Running

```bash
# Backend
cd backend && go run .

# Landing page
cd landing && npm run dev

# Deck
cd deck && bun dev

# Android — open app/Soporte Desarrollo/ in Android Studio
```

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | — | Liveness check |
| `POST` | `/api/register` | — | Device registration, returns bearer token |
| `POST` | `/api/suggest` | Bearer | AI suggestion proxy |
| `POST` | `/api/events` | Bearer | Batch analytics ingestion |
| `GET` | `/api/config` | Bearer | Remote configuration |

## Traction

- **Approved on the Clover App Market** — production-ready
- **7/10 stores** interested after in-person demos with the working product
- **1 supermarket pilot** agreed: 1-week test targeting 10% cross-sell revenue lift
- Validated across restaurants, markets, bookstores, and supermarkets
- Meetings with Izipay and Culqi (largest POS providers in Peru)

## License

Proprietary. All rights reserved.
