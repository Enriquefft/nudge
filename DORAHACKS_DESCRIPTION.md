## Nudge — AI Upselling at the Point of Sale for Clover Merchants

**55% of orders get zero upsell attempt.** Staff forget, feel awkward, or don't know what to suggest. That's up to $6,000/month per location in missed revenue.

Nudge fixes this. When a cashier adds an item to an order, the app reads the store's real inventory and shows a suggestion card in under 2 seconds — with the exact words to say. One tap adds it to the order.

### How It Works

1. Staff adds an item to the order
2. AI analyzes the merchant's real catalog + current order
3. Suggestion card appears: item name, price, and a conversational script
4. **Add to Order** — item goes into the Clover order. **Dismiss** — card disappears
5. AI learns from every accept/dismiss, getting smarter for each store

### Field Validation

- **7/10 stores** want to pilot after seeing the working product
- **1 supermarket** agreed to a 1-week pilot targeting 10% cross-sell lift
- **Izipay & Culqi** (largest POS providers in Peru) took formal meetings
- **Approved on the Clover App Market** — ready to ship

### Market Finding

<5% of POS in Peru have inventory data — they just track money. Most POS platforms *can't* do AI upselling. Clover can, because merchants already have their catalog loaded via InventoryConnector. That infrastructure is the moat.

### Tech

- **Android:** Kotlin, Clover SDK v329, Room DB, Coroutines
- **Backend:** Go on Fly.io, Neon Postgres
- **AI:** Z.ai GLM-4.7-Flash (proxied through backend, direct fallback)
- **One codebase, three build flavors:** Clover (native POS), Pilot (standalone for any store), Demo (sales meetings)
- Sentry crash reporting, GitHub Actions CI/CD

### Business Model

- **$29–49/month** per location
- Merchants potentially gain **$72K+/year** in extra upsell revenue — 100x+ ROI
- Every upsell flows through Clover's payment rails — more GMV for Fiserv
- AI cost per merchant: ~$0.003/day — commercially viable at any price point

**Live:** [nudge.404tf.com](https://nudge.404tf.com)
