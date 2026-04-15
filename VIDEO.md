# Nudge — 3-Minute Video Plan

## Roadmap (10pm - 7am)

| Time | What | Duration |
|------|------|----------|
| 10:00pm | Read this doc, practice script out loud 2x | 20 min |
| 10:20pm | Record talking head (field validation + close) | 20 min |
| 10:40pm | Screen-record app demo on phone (3-5 min raw) | 20 min |
| 11:00pm | Screen-record deck slides for stats/business sections | 15 min |
| 11:15pm | Record voiceover audio (non-talking-head sections) | 25 min |
| 11:40pm | **Sleep** | **~6 hours** |
| 5:30am | Edit everything together (CapCut on phone) | 60 min |
| 6:30am | Review, fix weak spots, re-record if needed | 20 min |
| 6:50am | Export + submit on DoraHacks | 10 min |

Record everything BEFORE sleeping. Editing is mechanical — you can do it groggy. Recording needs energy.

---

## Video Structure (3:00)

### 0:00-0:08 — HOOK (text card + voiceover)

**Screen:** Black. White text fades in: `55% of orders get zero upsell attempt.`

**Say:** "More than half of orders at any store get no upsell at all."

### 0:08-0:25 — THE GAP (deck slides or text cards + voiceover)

**Screen:** Quick cuts between stat cards from your deck (problem slide).

**Say:** "Staff forget, feel awkward, or don't know what to suggest. That's $6,000 a month per location walking out the door. And on the Clover App Market — zero real-time AI upsell tools. Nothing."

### 0:25-1:10 — THE PRODUCT (screen recording of pilot app)

**Screen:** Phone screen recording. Show the actual app.

Flow to record:
1. App is open, order screen visible
2. Add an item — suggestion card slides in (<2s)
3. Read the conversational script out loud
4. Tap "Add to Order" — item added
5. Add a different item — new suggestion appears
6. Tap "Dismiss" — card goes away cleanly
7. Quick flash of the stats screen

**Say:** "This is Nudge. Cashier adds an item to the order. AI reads the store's real inventory and shows a suggestion — with the exact words to say. One tap, it's in the order. Dismiss it, it disappears. It learns from every accept and dismiss. Gets smarter for each store."

### 1:10-1:50 — FIELD VALIDATION (talking head, on camera)

**Screen:** You, looking at camera. Desk lamp on your face from the side. Clean background.

**Say (don't read, just know these beats):**
- "I went to 10 stores today with this app running on my phone."
- "7 want to pilot."
- "One supermarket agreed to a 1-week pilot."
- "I met with Izipay and Culqi — the two biggest POS in Peru."
- "I found out something: less than 5% of POS here even have inventory data. They just track money. Most POS can't do what Nudge does."
- "Clover can. InventoryConnector, the catalog — that infrastructure is the reason this works. That's why I built on Clover."

### 1:50-2:20 — BUSINESS MODEL (deck slides + voiceover)

**Screen:** Business model slide from deck. Then ROI bar.

**Say:** "$29 to $49 a month per location. The merchant potentially gains $72K a year in extra revenue. That's over 100x return. And every upsell runs through Clover's payment rails — more volume per merchant, more processing fees for Fiserv."

**Screen:** Text card or slide: `Approved on the Clover App Market`

**Say:** "As of today, Nudge is approved on the Clover App Market. Not a prototype. Ready to ship."

### 2:20-2:40 — TECH (text cards or deck slide + voiceover)

**Screen:** Architecture slide or simple text cards.

**Say:** "One codebase, three build flavors: Clover native, standalone pilot, and demo. Kotlin, Clover SDK, Go backend on Fly.io. The pilot version works alongside any POS. The Clover version hooks directly into InventoryConnector and OrderConnector."

### 2:40-3:00 — CLOSE (talking head or text card)

**Screen:** You on camera, or Nudge logo + icon on dark background.

**Say:** "The app works. Stores want it. It's approved. Nudge — AI upselling at the point of sale."

**Screen:** Nudge icon. `nudge.404tf.com`. Your name.

---

## Recording Checklist

### Talking head (record first, lighting matters)
- [ ] Phone propped at eye level, not below (no chin shot)
- [ ] Desk lamp on your face from the side, not behind you
- [ ] White or plain wall behind you if possible
- [ ] Look at the camera lens, not the screen
- [ ] Record 3 takes of each segment, pick the best
- [ ] Segment A: field validation (~40 sec)
- [ ] Segment B: close (~15 sec)

### App screen recording
- [ ] Use Android built-in screen recorder
- [ ] Turn on Do Not Disturb (no notifications during recording)
- [ ] Load demo data that looks realistic (not "Test Item 1")
- [ ] Record 3-5 minutes of raw usage, you'll cut to ~45 sec
- [ ] Show at least 2 different item categories
- [ ] Show one dismiss and one accept
- [ ] Show the stats screen

### Deck slides / text cards
- [ ] Screen-record the deck on laptop: problem slide, business model, competitive, architecture
- [ ] OR make simple text cards in CapCut (black bg, white text, one stat per card)
- [ ] The "55%" hook can be a CapCut text card — no need to record the deck for it

### Voiceover
- [ ] Record in a quiet room, phone close to mouth
- [ ] Use Voice Memos or record video of a black screen (strip video later)
- [ ] Record each section separately — easier to edit
- [ ] Speak slower than you think you need to
- [ ] Record 2 takes of each, pick the cleaner one

---

## Editing (CapCut)

Order of assembly:
1. Import all clips (talking head, screen recordings, voiceover audio)
2. Lay down the voiceover as the audio backbone
3. Place screen recordings over the voiceover, trimming to match
4. Insert talking head segments at 1:10 and 2:40
5. Add text cards for the hook and any transitions
6. Add fade transitions between sections (0.3s, nothing fancy)
7. Optional: subtle background music (CapCut stock, low volume)
8. Export at highest quality

---

## Submission

### DoraHacks (Fiserv / Fintech Track)
- [ ] Upload video
- [ ] GitHub link: the repo
- [ ] Description (use this):

> **Nudge** — AI-powered upselling at the point of sale for Clover merchants.
>
> When a cashier adds an item to an order, Nudge reads the store's real inventory and shows a suggestion card in under 2 seconds — with the exact words to say. One tap adds it to the order.
>
> **Traction:** 7/10 stores interested after field validation. Supermarket pilot agreed. Meetings with Izipay and Culqi (largest POS in Peru). Approved on the Clover App Market.
>
> **Key finding:** <5% of POS in Peru have inventory data. Clover's InventoryConnector infrastructure is the moat that makes AI upselling possible.
>
> **Tech:** Kotlin + Clover SDK, Go backend on Fly.io, Neon Postgres, AI via Z.ai GLM-4.7-Flash. One codebase, three build flavors (Clover, Pilot, Demo).
>
> **Business model:** SaaS $29-49/mo per location. 100x+ ROI for merchants. Every upsell increases GMV through Clover's payment rails.
>
> Live: nudge.404tf.com

### PL_Genesis (Crecimiento Track) — deadline March 31
- [ ] Cross-submit same project
- [ ] Qualifies under AI track

### Best Projects Track
- [ ] Cross-submit same project
