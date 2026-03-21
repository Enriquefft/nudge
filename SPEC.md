# Nudge — Product Specification

AI-powered upsell suggestions at the point of sale for Clover merchants.

## Core Flow

```
Staff adds item to order
        ↓
App detects new line item (Clover SDK listener)
        ↓
AI analyzes: merchant's menu + current order items
        ↓
Suggestion card appears on merchant screen
        ↓
Staff taps "Add" → item added to order | or "Dismiss" → card disappears
```

## Features (ranked by importance)

### 1. Real-Time Suggestion Trigger ← CRITICAL

The moment between "item added" and "suggestion visible" IS the product. If this is slow, laggy, or unreliable, nothing else matters.

- Detect line item additions to the active order via `OrderConnector` listener
- Trigger AI request immediately on detection
- Display suggestion within 1-2 seconds
- Must work every time, on every item addition
- If AI is still processing a previous suggestion, queue — don't stack or flicker

### 2. Suggestion Quality ← CRITICAL

A bad suggestion kills trust instantly. "Add a steak to go with your steak" = uninstall.

- AI must suggest a COMPLEMENTARY item, not a similar one
- Suggestions must come from the merchant's actual inventory — never hallucinate items
- Must not suggest items already in the order
- Prioritize: modifiers/add-ons > sides > drinks > desserts > mains
- Each suggestion includes a short conversational reason staff can say out loud
- Prompt must be tuned until suggestions feel natural and smart

### 3. One-Tap Add to Order ← CRITICAL

This closes the loop. Without it, the app is just a display widget. With it, it directly drives revenue.

- "Add to Order" button adds the suggested item to the current Clover order via `OrderConnector.addLineItem()`
- Item appears in the order immediately — staff sees it, receipt reflects it
- No extra confirmation dialogs, no extra taps
- After adding, card dismisses and waits for next item

### 4. Suggestion Card Design ← CRITICAL

This is the only UI judges will see. It must look like it belongs on a Clover device. A polished app with basic features beats an ugly app with many features.

- Card overlay on the main screen — not a modal, not a full-screen takeover
- Shows: item name, price, reason (one line)
- Two buttons: "Add to Order" (primary, prominent) and "Dismiss" (secondary, subtle)
- Large tap targets — staff are busy, hands might be dirty/wet
- Auto-dismiss after 15 seconds if no action
- **Polished animations:** smooth slide-in/fade-in on appear, smooth slide-out/fade-out on dismiss. No jarring pops. This must feel professional and native.
- Match Clover's Material Design aesthetic (colors, typography, spacing)
- High contrast, readable from arm's length

### 5. Adaptive Intelligence (Learning from Outcomes) ← HIGH

The AI gets smarter over time by learning what works for this specific merchant.

- **Upsell outcome tracking:** Every accept/dismiss is recorded per trigger-item → suggested-item pair. The AI prompt includes historical win/loss data so it avoids suggestions that get dismissed and doubles down on ones that convert.
- **Customer-aware suggestions (when available):** If the merchant has Clover customer data (via CustomerConnector), Nudge pulls the current customer's order history and preferences. The AI uses this to personalize: "This customer orders a milkshake 80% of the time" → suggest milkshake first.
- Graceful degradation: customer data is optional. Works without it, better with it.
- All learning data stays local to the merchant's device (SharedPreferences).

### 6. Acceptance Tracking + Stats Screen ← MEDIUM

Proof that the app works. Essential for the demo. Merchants check this daily.

- Track locally: suggestions shown, accepted, dismissed
- Stats screen shows:
  - Suggestions today: N
  - Accepted: N (X%)
  - Estimated additional revenue: $X (sum of accepted item prices)
- Simple, clean, one screen
- Resets daily or shows selectable date range

## Architecture

```
┌──────────────────┐
│  Clover Device    │
│  (Kotlin)         │
│                   │
│  InventoryConnector → reads menu (cached locally)
│  OrderConnector   → listens for item additions
│  UI               → suggestion card overlay
│  OrderConnector   → adds accepted items to order
│                   │
└────────┬──────────┘
         │ HTTPS
         ▼
┌──────────────────┐
│  AI Service       │
│  (Claude Haiku)   │
│                   │
│  Input: menu context + current items
│  Output: { item, price, reason }
└──────────────────┘
```

## Tech Stack

- **Language:** Kotlin
- **SDK:** Clover Android SDK v329
- **Target SDK:** 29 (Android 10), Min SDK: 17
- **AI:** Claude Haiku API (direct HTTPS call from app, or thin backend proxy)
- **No GMS/Firebase** — Clover runs AOSP
- **Signing:** V1 only (Clover requirement)
- **Local storage:** SharedPreferences or SQLite for stats tracking

## Project Template

Base scaffold is at `clover-template/Soporte Desarrollo/`. It has Gradle config with Clover SDK but empty `app/src/` — build from scratch.

```
clover-template/Soporte Desarrollo/
├── app/build.gradle.kts    ← Clover SDK v329, compileSdk 29, minSdk 17
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
└── app/src/                ← EMPTY — all code goes here
```

## Clover Build Constraints (will waste hours if ignored)

- **AOSP Android, NOT Google Android.** No `com.google.android.gms` or `com.google.firebase` packages. They will not resolve. Use standard Android APIs only.
- **No Firebase Cloud Messaging.** For notifications, use Clover's own Notifications API or local notifications.
- **V1 signing ONLY.** V2 signing must be disabled. The template already has this configured:
  ```kotlin
  signingConfig?.apply {
      enableV1Signing = true
      enableV2Signing = false
  }
  ```
- **Kotlin recommended.** Flutter and React Native are explicitly NOT supported by Clover.
- **Java 1.8 compatibility.** `jvmTarget = "1.8"`, no Java 11+ features.
- **compileSdk/targetSdk = 29.** Do not raise this — Clover devices run Android 8.1-10.
- **Clover SDK dependency:**
  ```kotlin
  implementation("com.clover.sdk:clover-android-sdk:329")
  ```
- **Network calls are unrestricted.** Standard Android networking (OkHttp, Retrofit, HttpURLConnection) works. External AI API calls are allowed.
- **Clover emulator** for testing: https://docs.clover.com/dev/docs/setting-up-an-android-emulator
- **Clover developer docs:** https://docs.clover.com/dev/docs/clover-development-basics-android
- **Clover SDK GitHub:** https://github.com/clover/clover-android-sdk

## Clover SDK Integration

| Function | SDK | Method |
|---|---|---|
| Load inventory | `InventoryConnector` | `getItems()`, `getCategories()`, `getModifierGroups()` |
| Listen for order changes | `OrderConnector` | Register listener on active order |
| Read current order | `OrderConnector` | `getOrder(orderId)` with line items |
| Add item to order | `OrderConnector` | `addLineItem(orderId, item)` |

## AI Prompt

System context (built once on startup from inventory):
```
You are Nudge, an upselling assistant for a POS system.

This merchant's menu:
{items_with_prices_and_categories}

Available modifiers:
{modifier_groups_with_prices}
```

Per-suggestion prompt:
```
Current order: {current_line_items_with_prices}

Suggest ONE complementary item or modifier from this merchant's menu.

Return ONLY valid JSON:
{ "item_id": "id", "item_name": "name", "price": "X.XX", "reason": "short phrase staff says to customer" }

Rules:
- Must be an item/modifier from the menu above (use exact item_id)
- Must NOT be already in the order
- Prefer add-ons and modifiers > sides > drinks > desserts > mains
- Reason must be conversational, specific, under 10 words
- Example reason: "Most people add fries with a burger"
```

## Data Flow

1. **On app launch:** Fetch full inventory via `InventoryConnector`. Cache in memory. Build menu context string for AI prompt.
2. **On order opened:** Register listener on active order.
3. **On line item added:** Read current order items. Send menu context + current items to AI. Display returned suggestion.
4. **On "Add" tap:** Call `addLineItem()` with the suggested item's ID. Log acceptance. Dismiss card.
5. **On "Dismiss" tap:** Log dismissal. Dismiss card.
6. **On order closed/paid:** Unregister listener. Wait for next order.

## Demo Data

For the hackathon demo, preload the Clover emulator with a realistic restaurant menu:
- 5-6 categories (Mains, Sides, Drinks, Desserts, Appetizers, Modifiers)
- 15-20 items with realistic prices
- 3-4 modifier groups (size upgrades, add-ons, extras)

## What This Is NOT

- Not a loyalty program
- Not a CRM
- Not an analytics dashboard
- Not a customer-facing app
- Not a marketing tool
- One feature. Done well.
