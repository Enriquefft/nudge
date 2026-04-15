# Pitch Strategy: Nudge 3-Minute Video

---

## The One Thing Judges Must Remember

**"This app is live on the Clover App Market, stores already want it, and nothing like it exists on Clover."**

Not the tech. Not the AI. The fact that this is a real product with real demand filling a gap that Clover's own infrastructure uniquely enables. Every second of the video must reinforce this.

---

## Judges & Criteria

All three judges are **product people** at Fiserv:
- Santiago Perez, Product Manager, Clover
- Sebastian Calens, VP Product, Acquiring
- Patricio Celia, Product Director

**Judging criteria** (equal weight): Technicality, Originality, UI/UX/DX, Practicality, Presentation.

They evaluate technicality through a **product lens** -- they care about *what you built and whether it works*, not your infrastructure choices.

---

## Optimal Narrative Arc

**Structure: GAP --> PRODUCT --> PROOF --> MONEY --> STAMP**

Not "problem --> solution --> traction." Here's why this specific ordering:

1. **GAP** (not "problem"): The judges are Clover product leaders. They already know merchants lose revenue from missed upsells. Don't educate them on the problem -- show them the gap *in their own ecosystem*. "Zero real-time AI upsell tools on Clover App Market" is a gap in THEIR platform. That makes it personal. That's what gets a product person to lean in.

2. **PRODUCT** immediately after the gap. You've earned 6 seconds of curiosity. Pay it off instantly. Show the app working. The longer you talk before showing the product, the more it feels like a pitch deck.

3. **PROOF** after the product. Once they've seen the app, the traction validates what they just watched. "7 out of 10 stores want this" hits different after they've seen it work. Before the demo, it's just a claim.

4. **MONEY** after proof. The business model only matters if they believe the product works and people want it. Now they believe both. Show them how Nudge makes money AND how it makes Fiserv money.

5. **STAMP** as its own beat. "Approved on the Clover App Market" answers the unspoken question: "But is this real?" Yes. It's approved. Ready to ship. This separates you from every other hackathon demo.

---

## Time Allocation

| Section | Time | Current | Recommended | Delta |
|---------|------|---------|-------------|-------|
| HOOK | 0:00-0:06 | 8 sec | 6 sec | -2s. One stat, one line. |
| GAP | 0:06-0:20 | 17 sec | 14 sec | -3s. Cut $6K -- save for biz model. |
| PRODUCT DEMO | 0:20-1:05 | 45 sec | 45 sec | Keep. This is the star. |
| FIELD VALIDATION | 1:05-1:45 | 40 sec | 40 sec | Keep. Strongest section. |
| BUSINESS MODEL | 1:45-2:15 | 30 sec | 30 sec | Add $6K/mo stat here. |
| CLOVER APPROVED | 2:15-2:25 | (buried) | 10 sec standalone | Own beat. Own text card. |
| TECH | 2:25-2:40 | 20 sec | 15 sec | -5s. Replace content, not just trim. |
| CLOSE | 2:40-2:55 | 20 sec | 15 sec | Shorter. Punchier. |
| END CARD | 2:55-3:00 | (in close) | 5 sec | Logo, URL, name. Silence. |

**Total: 3:00**

---

## What to CUT

1. **"$6,000 a month walking out the door" from the gap.** Move to business model as ROI math. The gap section should be about the gap, not math.

2. **Izipay and Culqi mention.** Distracts from Clover focus. Judges don't know these brands. Invites "wait, does this work on competitors?" See Peru finding section.

3. **"Less than 5% of POS have inventory data."** Too specific without context. Replace with "most POS can't do this."

4. **All backend infrastructure names.** Go on Fly.io, Neon Postgres, Z.ai GLM-4.7 -- none of these mean anything to product judges. See tech section.

5. **Room DB / Coroutines / Sentry / GitHub Actions / ProGuard R8.** Standard Android tooling. Like a chef listing "oven, knife, cutting board."

6. **"The pilot version works alongside any POS."** Product detail for investors, not a 3-minute video.

---

## Opening Hook

**The 55% stat targets merchants. The judges aren't merchants -- they're Clover product leaders.**

**Recommended opening:**

> Screen: Black. White text fades in: `Zero.`
> Beat. Then: `Real-time AI upsell tools on the Clover App Market: zero.`
>
> Voiceover: "Zero real-time AI upsell tools on the Clover App Market. Merchants lose 55% of upsell opportunities because staff forget, feel awkward, or don't know what to suggest."

**Why "zero" over "55%":**
1. "Zero" is a challenge to the product people in the room. It says: "Your platform has a gap. I found it. I filled it."
2. The 55% stat still appears -- but as supporting evidence, not the headline.
3. It immediately frames Nudge as Clover-native, not a generic AI project.

**If you want to stay safe:** The 55% hook works fine. It's dramatic and sourced (Red Lobster/SoundHound study). The "zero" hook is higher-risk, higher-reward. Pick based on delivery confidence.

---

## The Tambo Quote

**Use it. Place it in field validation, after "7 want to pilot."**

> "A Tambo employee told me, 'esto es mejor que mi entrenamiento' -- 'this is better than my training.'"

**Why:**
1. Only third-party validation in the entire video. Everyone else is you reporting on your own product.
2. Spanish creates a moment of authenticity in an English video. The judges will understand it.
3. Proves the product was used by a real employee, not just shown to store owners.
4. "Better than my training" means it solves an operational problem, not just a revenue problem.

**Where exactly:** After "7 want to pilot" and before the supermarket pilot. Escalation: stores want it --> an employee loves it --> a supermarket commits.

**Delivery:** Say it naturally in the talking head. Keep the Spanish. Add English subtitle in editing.

---

## The "Approved on Clover App Market" Moment

**Give it its own 10-second standalone beat at 2:15.**

Currently buried inside the business model section. This is the single most differentiating fact in the video. Don't share it with ROI math.

**Visual:** Full screen. Black background. White text: `Approved on the Clover App Market`. Hold 2 seconds. Then voiceover: "As of today, Nudge is approved on the Clover App Market. Not a prototype. Ready to ship."

**Why here, not at the end:** By the close you're wrapping up. The approval needs room to breathe. After the business model, judges think "the math works, but is this real?" The approval answers that at the perfect moment.

---

## Technical Content: What to Show vs Cut

### What scores "Technicality" with product judges

Product judges interpret "advanced engineering" as:

1. **Deep Clover SDK integration (InventoryConnector + OrderConnector)** -- Reading real inventory, modifying real orders. Santiago Perez will know how hard this is. **Currently not mentioned at all.**

2. **Real-time AI in a POS workflow** -- Suggestion in 1-2 seconds while a cashier is mid-transaction. Hard latency constraint.

3. **The learning loop** -- Tracks accept/dismiss per item pair, feeds history back to AI. Gets smarter per merchant. Separates Nudge from static rule-based tools.

4. **Approved on Clover App Market** -- Technical validation from Clover's own review process.

### Tech section: replace content, not just trim

**Current VO 4 (~15 sec):**
> "One codebase, three build flavors. Kotlin, Clover SDK, Go backend on Fly.io."

This is name-dropping. Replace with substance.

**Recommended VO 4 (~10 sec):**
> "One Kotlin codebase, native Clover integration. It reads real inventory, modifies real orders, and learns what works for each store."

This hits: (1) Kotlin + Clover = correct platform, (2) InventoryConnector/OrderConnector = deep SDK work, (3) learning = adaptive intelligence. No name-dropping, all substance.

### Architecture card (if shown)

**Keep:** "1 app, 3 deployment modes" headline. "Kotlin + Clover SDK."
**Add:** "InventoryConnector + OrderConnector." "Adaptive learning per merchant."
**Cut:** Go on Fly.io, Neon Postgres, Z.ai GLM-4.7, Room DB, Coroutines, Sentry, GitHub Actions, ProGuard R8.

### Full detail verdict

| Detail | Verdict | Reason |
|--------|---------|--------|
| Clover App Market approved | **KEEP - lead with it** | Production validation |
| InventoryConnector/OrderConnector | **ADD** | Core technical achievement, currently missing |
| Learning algorithm | **ADD** | Differentiator product people understand |
| 1 codebase, 3 flavors | **TRIM** | Reframe as deployment reach |
| Kotlin + Clover SDK | **KEEP** | Required context, 2 words |
| Go on Fly.io | **CUT** | Infrastructure name-dropping |
| Neon Postgres | **CUT** | Irrelevant |
| Z.ai GLM-4.7 | **CUT** | AI model name means nothing |
| Room DB / Coroutines | **CUT** | Standard Android |
| Sentry | **CUT** | Ops tooling, not a feature |
| GitHub Actions CI/CD | **CUT** | Expected for any shipped product |
| ProGuard R8 | **CUT** | Build optimization, irrelevant |

---

## Peru Finding Framing

### The problem with the current approach

The current script spends ~15 seconds on: Izipay meeting, Culqi meeting, "<5% of POS have inventory data," "most POS can't do what Nudge does."

This creates more questions than it answers. A product judge hears "Peru POS can't do this" and thinks: "So this doesn't work in his home market?"

### Recommendation: CUT the Peru details. KEEP the Clover moat line.

The finding is real and strategically important. But in a 3-minute video, you don't need the full syllogism (Peru POS lack data --> Nudge needs data --> Clover has data --> Clover is the moat). Just state the conclusion:

**"This works because Clover has real inventory data on every device. Most POS don't -- that's the moat."**

This is true, it compliments Clover, and it doesn't invite questions about which markets or countries.

### What was cut and why

| Removed | Reason |
|---------|--------|
| "Met with Izipay and Culqi" | Distracts from Clover, invites competitor questions |
| "Less than 5% of POS have inventory data" | Too specific, requires context judges don't have |
| "Two biggest POS in Peru" | Positions Peru as target market, which it currently isn't |

### Where the Peru finding IS useful (not the video)
- Post-hackathon mentorship conversations with Fiserv
- If a judge asks "what about international markets?" in Q&A
- Investor conversations about defensibility
- PRODUCT.md (already documented)

---

## Closing

> Talking head: "The app works. Stores want it. It's live on Clover."
>
> End card: Nudge icon, `nudge.404tf.com`, your name. Hold 4-5 seconds. Silence or subtle music.

**Why "live on Clover" not "AI upselling at the point of sale":** By 2:55 they know what it is. The close should be a call to action ("you can install this right now"), not a category description.

---

## Language: English

1. DoraHacks is international, English is default.
2. Clover is a US product; Santiago Perez works in English.
3. English makes the video reusable (other tracks, landing page, App Market).
4. English signals "global product" not "local Peru hack."

**Exception:** If talking head is more natural in Spanish, do it in Spanish with English subtitles. Authenticity beats polish.

---

## Tone: Scrappy Founder with Product Discipline

| Style | Signal | Risk |
|-------|--------|------|
| Startup energy | "We're gonna disrupt!" | Naive to product VPs |
| Corporate polish | Smooth, rehearsed, safe | Feels like a deck |
| **Scrappy founder** | **"I built this, tested it, it works"** | **None for these judges** |

- Talking head: conversational, direct, eye contact. Like telling a friend what happened today.
- Voiceover: slightly more measured and authoritative. Half-step more composed.
- Say "I" not "we." Solo founder. More honest, more impressive.

---

## Revised Script Beats

### 0:00-0:06 -- HOOK
- Screen: `Zero.` then `Real-time AI upsell tools on Clover: zero.`
- VO: "Zero real-time AI upsell tools on the Clover App Market."

### 0:06-0:20 -- GAP
- Screen: Stat cards (55% of orders, staff barriers)
- VO: "55% of orders get no upsell attempt. Staff forget, feel awkward, or don't know what to suggest. Nothing on Clover solves this."

### 0:20-1:05 -- PRODUCT DEMO
- Screen: Phone recording -- add item, suggestion, accept. Second item, dismiss. Stats flash.
- VO: "This is Nudge. Cashier adds an item. AI reads the store's real inventory and shows a suggestion with the exact words to say. One tap, it's in the order. Dismiss, it's gone. It learns from every accept and dismiss."

### 1:05-1:45 -- FIELD VALIDATION (talking head)
- "I went to 10 stores today with this app on my phone."
- "7 want to pilot."
- "A Tambo employee told me: 'esto es mejor que mi entrenamiento.'"
- "One supermarket agreed to a 1-week pilot."
- "This works because Clover has real inventory data on every device. Most POS don't -- that's the moat."

### 1:45-2:15 -- BUSINESS MODEL
- Screen: Business model slide, ROI bar
- VO: "Merchants pay $29 to $49 a month. A single location gains $6,000 a month in extra revenue -- $72K a year. Over 100x return. Every upsell runs through Clover's payment rails -- more volume, more processing fees for Fiserv."

### 2:15-2:25 -- CLOVER APPROVED (standalone)
- Screen: Full screen `Approved on the Clover App Market`
- VO: "As of today, Nudge is approved on the Clover App Market. Not a prototype. Ready to ship."

### 2:25-2:40 -- TECH (compressed)
- Screen: Architecture slide or text cards
- VO: "One Kotlin codebase, native Clover integration. It reads real inventory, modifies real orders, and learns what works for each store."

### 2:40-2:55 -- CLOSE (talking head)
- "The app works. Stores want it. It's live on Clover."

### 2:55-3:00 -- END CARD
- Nudge icon. `nudge.404tf.com`. Your name. Silence or subtle music.

---

## Summary of All Changes

| Decision | Current | Recommended | Why |
|----------|---------|-------------|-----|
| Opening hook | 55% stat | "Zero tools on Clover" | Targets judges, not merchants |
| $6K/mo stat | In gap section | In business model | Stronger as ROI proof |
| Tambo quote | Not included | In field validation | Only third-party validation |
| Clover approved | Buried in biz model | Standalone 10-sec beat | Too important to share |
| Peru / Izipay / Culqi | Detailed field finding | "Most POS can't" (1 line) | Avoids distraction |
| Tech content | Stack name-dropping | SDK integration + learning | Substance over labels |
| Tech time | 20 sec | 15 sec | Reallocate to stronger sections |
| Close | Category description | "Live on Clover" (CTA) | Action beats description |
| Language | Unspecified | English | Global signal, reusable |
| Tone | Unspecified | Scrappy founder | Authentic + disciplined |
