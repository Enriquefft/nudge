# Clover Developer Platform & APIs

## API Families

| API | Purpose |
|---|---|
| **Platform API (v3)** | Core: Merchants, Customers, Orders, Payments, Inventory, Employees, Notifications |
| **Ecommerce Service API** | Card-not-present: Charges, Refunds, Gift Cards |
| **Card Present REST API** | In-person: Payments, Card, Credits, Receipts |
| **Tokenization Service APIs** | Stored payment method tokens |
| **Recurring APIs** | Plans and Subscriptions |

Base URLs:
- Sandbox: `https://apisandbox.dev.clover.com/v3/merchants/{mId}/`
- Production: `https://api.clover.com/v3/merchants/{mId}/`

## Key APIs for Our Use Case

### Orders API (`/v3/merchants/{mId}/orders`)
- CRUD on orders
- **Line items** within orders (item ID, name, price, quantity) — critical for basket analysis
- Discounts, service charges, order state (open/locked/paid)
- Timestamps, associated payments and customer references

### Customers API (`/v3/merchants/{mId}/customers`)
- Customer profiles: name, phone, email, address
- Multi-pay tokens (identify repeat payers by card)
- Customer order history via cross-reference
- EU: special privacy permission controls

### Payments API (`/v3/merchants/{mId}/payments`)
- Amount, tip, tax
- Payment method type (credit, debit, cash, digital wallet)
- Card type metadata
- Transaction timestamps

### Inventory API (`/v3/merchants/{mId}/items`)
- Items: name, price, cost, description
- **Categories and category-item associations**
- **Modifier groups and modifiers** (sizes, add-ons) — upsell goldmine
- Tags, tax rates, discounts
- Bulk import supported

## Authentication

- **OAuth 2.0** (v2 flow mandatory for all apps by Dec 2025)
- Expiring `access_token` + `refresh_token` pairs
- Two flows: standard Authorization Code (high-trust) or + PKCE (low-trust)
- MFA mandatory for global developer accounts
- Merchant-generated API tokens (manual) do NOT expire — useful for testing
- Scoped permissions: apps request only what they need

## Rate Limits

- Enforced **at the application level** — all merchants share the same pool
- Exceeding returns `429 Too Many Requests`
- Specific numeric thresholds not publicly documented
- Pagination required for large datasets (offset/limit)
- Filter parameters supported (e.g., orders by date range)

## Webhooks (Real-Time Events)

Available events:
- **Order** (`O`): fires on order create/update/delete
- **Payment** (`P`): fires on payment create/update
- **Inventory** (`I`, `IC`, `IG`, `IM`): item/category/modifier changes
- **Customer** (`C`): customer record changes
- Also: Employees, App installs/uninstalls, Merchant properties, Service hours, Cash adjustments

Payload: `objectId`, `type` (CREATE/UPDATE/DELETE), `ts` (Unix timestamp ms)

**Key for us:** Line items added to an open order trigger an Order UPDATE webhook — enables real-time "items being added to cart" detection for upsell suggestions.

## Android SDK

### Available SDKs

| SDK | Use Case |
|---|---|
| **clover-android-sdk** | Native apps on Clover hardware |
| **Remote Pay Android SDK** | External Android POS → Clover payment device |
| **Clover Connector Android SDK** | Semi-integrated POS software |
| **Clover Go SDK** | BYOD mobile + Clover Go reader |

### Key Constraints

- **Kotlin recommended**, Java supported. Flutter/React Native **NOT recommended**
- **No Google Mobile Services (GMS)** — Clover runs AOSP Android. No `com.google.android.gms` or Firebase
- Must use Clover's Notifications API instead of Firebase Cloud Messaging
- Standard Android UI (Views, Activities, Fragments)
- Standard networking (OkHttp, Retrofit) — **external API calls unrestricted**
- Standard storage: SQLite, SharedPreferences, files
- Kotlin Coroutines encouraged for background processing
- Min API Level 17

```gradle
dependencies {
    compile 'com.clover.sdk:clover-android-sdk:latest.release'
}
```

## Clover Hardware

| Device | Screen | Connectivity | Best For |
|---|---|---|---|
| **Station Solo** | 14" HD | WiFi, Ethernet | All-in-one countertop |
| **Station Duo 2** | 14" merchant + 8" customer | WiFi, Ethernet | **Ideal for upsell display** |
| **Mini 2** | 8" touch | WiFi, Ethernet | Compact countertop |
| **Mini 3** | 8" LCD | Ethernet, WiFi, 4G | Built-in printer, camera |
| **Flex 3** | 5.99" HD | WiFi, cellular | Handheld |
| **Flex 4** | ~6" touch | WiFi, cellular | Newest handheld |
| **Compact** | Keypad + display | Ethernet, WiFi, 4G | PIN shield, camera |
| **Clover Go** | Phone/tablet | Bluetooth | BYOD |

### Processing Power
- Mini 2: Qualcomm Snapdragon 660 (mid-range mobile SoC)
- **Cannot run local LLMs** — must use cloud AI
- Can handle lightweight on-device inference (small models, embeddings)
- All devices have WiFi; Flex/Mini 3/Compact have cellular fallback

### Gen 1 End-of-Life
- Gen 1 devices (Station C010, Mobile C020/C021, Mini C030/C031, Flex C041/C042) reach End-of-App-Update on **March 30, 2026**
- Target Gen 2+ devices

## Data Available for AI

| Data | Available? | Notes |
|---|---|---|
| Transaction history | Yes | All historical records, filterable by date |
| Line items per order | Yes | Item ID, name, price, qty, modifiers |
| Repeat customers | Yes | Via Customer profiles or card fingerprinting |
| Inventory catalog | Yes | Items, categories, modifiers, prices |
| Real-time order updates | Yes | Webhooks fire on line item additions |
| Stock quantities | No | Clover inventory = catalog, not warehouse |
| Raw card numbers | No | PCI — only tokenized references |

## Architecture for Our App

```
[Clover Device] ──webhook──→ [Our Backend] ──→ [AI Service (Claude/etc)]
      ↑                           │
      └───── REST API ────────────┘
         (suggestions, insights)
```

1. **Webhook** fires when items added to open order
2. **Backend** receives event, fetches full order via REST API
3. **AI service** analyzes basket, returns upsell suggestion
4. **Backend** pushes suggestion to the Clover app (or app polls)
5. **Staff sees suggestion** on merchant-facing screen

**Station Duo 2** is ideal: merchant sees suggestions on 14" screen, customer sees order/promo on 8" screen.

## Sources

- [Clover API Reference](https://docs.clover.com/dev/reference/api-reference-overview)
- [Clover REST API Tutorials](https://docs.clover.com/dev/docs/clover-rest-api-index)
- [Clover Android SDK — GitHub](https://github.com/clover/clover-android-sdk)
- [Custom Android Basics](https://docs.clover.com/dev/docs/clover-development-basics-android)
- [Webhooks](https://docs.clover.com/dev/docs/webhooks)
- [Clover Devices Tech Specs](https://docs.clover.com/dev/docs/clover-devices-tech-specs)
- [OAuth Token](https://docs.clover.com/dev/docs/obtaining-an-oauth-token)
- [Inventory FAQs](https://docs.clover.com/dev/docs/inventory-faqs)
- [What's New 2025](https://docs.clover.com/dev/docs/whats-new-2025)
- [New Clover Devices 2025 — VMS](https://www.getvms.com/new-clover-devices-in-2025/)
