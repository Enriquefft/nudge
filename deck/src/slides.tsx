import { Slide, Notes } from 'prez'

export default function slides() {
  return [
      // ====== SLIDE 1: HOOK + PROBLEM (30s) ======
      <Slide>
        <div className="flex h-full bg-[#0a0a0a] text-white">
          {/* Left: The stat */}
          <div className="flex-1 flex flex-col justify-center px-20">
            <p className="text-lg font-medium text-amber-400 tracking-widest uppercase mb-6">
              Every single day
            </p>
            <h1 className="text-[8rem] font-black leading-[0.85] tracking-tight text-white">
              37%
            </h1>
            <p className="text-2xl font-light text-white/50 mt-6 max-w-lg leading-relaxed">
              of staff <span className="text-white font-medium">never suggest an add-on</span>.
              The ones that do are inconsistent.
            </p>
          </div>
          {/* Right: Supporting data */}
          <div className="w-[420px] flex flex-col justify-center gap-6 pr-16">
            <div className="bg-white/[0.04] border border-white/[0.08] rounded-2xl p-7">
              <p className="text-4xl font-black">$6K</p>
              <p className="text-sm text-white/40 mt-1">lost monthly per merchant from missed upsells</p>
            </div>
            <div className="bg-white/[0.04] border border-white/[0.08] rounded-2xl p-7">
              <p className="text-4xl font-black">78%</p>
              <p className="text-sm text-white/40 mt-1">AI upsell rate vs 45% for humans</p>
            </div>
            <div className="bg-amber-500/10 border border-amber-500/20 rounded-2xl p-7">
              <p className="text-4xl font-black text-amber-400">0</p>
              <p className="text-sm text-amber-300/50 mt-1">
                apps on Clover that do this. Out of 283.
              </p>
            </div>
          </div>
        </div>
        <Notes>
          "37 percent of restaurant staff never suggest an add-on. Not sometimes — never.
          The ones who do? Wildly inconsistent. That gap costs the average merchant $6,000
          a month. AI upsells 78% of the time — humans, 45%. And across 283 apps on the
          Clover App Market? Zero do real-time AI upselling. Zero."
        </Notes>
      </Slide>,

      // ====== SLIDE 2: THE GAP / WHY NOW (20s) ======
      <Slide>
        <div className="flex flex-col justify-center h-full bg-[#0a0a0a] text-white px-20 py-14">
          <h2 className="text-[3.2rem] font-bold leading-tight mb-10">
            Toast built this for their merchants.
            <br />
            <span className="text-amber-400">Clover's 700,000 have nothing.</span>
          </h2>
          <div className="flex gap-6">
            <div className="flex-1 bg-white/[0.04] border border-white/[0.08] rounded-2xl p-7">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-2.5 h-2.5 rounded-full bg-orange-500" />
                <p className="text-lg font-semibold text-orange-400">Toast IQ</p>
              </div>
              <ul className="space-y-2.5 text-[15px] text-white/50">
                <li>AI menu upsells at POS</li>
                <li>Guest history on handhelds</li>
                <li>AI-driven marketing</li>
              </ul>
              <p className="mt-5 text-xs text-white/20">Restaurants only. Locked ecosystem.</p>
            </div>
            <div className="flex-1 bg-white/[0.04] border border-white/[0.08] rounded-2xl p-7">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-2.5 h-2.5 rounded-full bg-green-500" />
                <p className="text-lg font-semibold text-green-400">Clover App Market</p>
              </div>
              <ul className="space-y-2.5 text-[15px] text-white/50">
                <li>Loyalty (points, punch cards)</li>
                <li>CRM (customer data storage)</li>
                <li>BI dashboards (historical reports)</li>
              </ul>
              <p className="mt-5 text-xs text-white/20">No AI. No real-time. No upselling.</p>
            </div>
            <div className="flex-1 bg-amber-500/[0.08] border-2 border-amber-500/30 rounded-2xl p-7">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-2.5 h-2.5 rounded-full bg-amber-400" />
                <p className="text-lg font-semibold text-amber-400">The gap</p>
              </div>
              <p className="text-xl font-semibold text-white leading-relaxed">
                Real-time AI recommendations at the moment of sale.
              </p>
              <p className="mt-5 text-xs text-amber-400/40">
                Cross-vertical. 700K+ merchants. First mover window: 12-24 months.
              </p>
            </div>
          </div>
        </div>
        <Notes>
          "Toast built ToastIQ — AI upsells at the POS. But it's locked to Toast,
          restaurants only. Clover has 700,000 merchants across every vertical — restaurants,
          retail, services. 283 apps. None with real-time AI. The gap is massive. And the
          window to fill it is 12 to 24 months."
        </Notes>
      </Slide>,

      // ====== SLIDE 3: MEET NUDGE (25s) ======
      <Slide>
        <div className="flex flex-col items-center justify-center h-full bg-[#0a0a0a] text-white">
          <div className="flex items-center gap-5 mb-5">
            <div className="w-[72px] h-[72px] rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center shadow-lg shadow-amber-500/20">
              <svg width="38" height="38" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </div>
            <h1 className="text-[5.5rem] font-black tracking-tight">Nudge</h1>
          </div>
          <p className="text-[1.6rem] text-white/40 font-light max-w-2xl text-center mb-14">
            AI upsell assistant that lives on your Clover POS.
            <br />
            <span className="text-white/60">No training. No new hardware. One tap.</span>
          </p>
          {/* 3-step flow */}
          <div className="flex items-center gap-5 max-w-3xl">
            <div className="flex-1 text-center bg-white/[0.04] border border-white/[0.08] rounded-xl py-6 px-4">
              <div className="w-11 h-11 rounded-full bg-amber-500/20 text-amber-400 flex items-center justify-center mx-auto text-lg font-black mb-3">1</div>
              <p className="font-semibold text-[15px]">Detect</p>
              <p className="text-xs text-white/30 mt-1">Item added to order</p>
            </div>
            <span className="text-white/15 text-2xl">&#8594;</span>
            <div className="flex-1 text-center bg-white/[0.04] border border-white/[0.08] rounded-xl py-6 px-4">
              <div className="w-11 h-11 rounded-full bg-orange-500/20 text-orange-400 flex items-center justify-center mx-auto text-lg font-black mb-3">2</div>
              <p className="font-semibold text-[15px]">Suggest</p>
              <p className="text-xs text-white/30 mt-1">AI picks the right add-on</p>
            </div>
            <span className="text-white/15 text-2xl">&#8594;</span>
            <div className="flex-1 text-center bg-white/[0.04] border border-white/[0.08] rounded-xl py-6 px-4">
              <div className="w-11 h-11 rounded-full bg-green-500/20 text-green-400 flex items-center justify-center mx-auto text-lg font-black mb-3">3</div>
              <p className="font-semibold text-[15px]">Add</p>
              <p className="text-xs text-white/30 mt-1">One tap, on the receipt</p>
            </div>
          </div>
        </div>
        <Notes>
          "Meet Nudge. It's an AI assistant that lives on your Clover POS. Three steps,
          under two seconds. Staff adds an item — Nudge detects it through the Clover SDK.
          AI analyzes the menu and current order, picks the best complementary item.
          A card appears with the suggestion and the exact words your staff can say.
          One tap — it's on the receipt. No training. No new hardware."
        </Notes>
      </Slide>,

      // ====== SLIDE 4: DEMO / PRODUCT SHOT (40s) ======
      <Slide>
        <div className="flex items-center justify-center h-full bg-[#0a0a0a] text-white px-14">
          <div className="flex gap-10 items-center max-w-[1100px] w-full">
            {/* Mock POS Screen */}
            <div className="w-[440px] shrink-0">
              <div className="bg-[#141414] rounded-2xl border border-white/[0.08] overflow-hidden shadow-2xl shadow-black/50">
                {/* POS Header */}
                <div className="bg-[#1a1a1a] px-6 py-3 flex items-center justify-between border-b border-white/[0.06]">
                  <span className="text-sm text-white/30 font-medium">Clover POS</span>
                  <span className="text-sm text-green-400/70">Order #1247</span>
                </div>
                {/* Order Items */}
                <div className="px-6 py-5 space-y-3">
                  <div className="flex justify-between text-white/70 text-[15px]">
                    <span>Classic Burger</span>
                    <span>$12.99</span>
                  </div>
                  <div className="flex justify-between text-white/70 text-[15px]">
                    <span>Iced Tea</span>
                    <span>$3.50</span>
                  </div>
                  <div className="border-t border-white/[0.06] pt-3 mt-3" />
                  <div className="flex justify-between text-white font-semibold">
                    <span>Total</span>
                    <span>$16.49</span>
                  </div>
                </div>
                {/* Nudge Card — the star of the show */}
                <div className="mx-4 mb-4 bg-gradient-to-r from-amber-500/[0.15] to-orange-500/[0.08] border border-amber-500/25 rounded-xl p-5">
                  <div className="flex items-center gap-2 mb-3">
                    <div className="w-5 h-5 rounded bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
                      <span className="text-[10px] text-white font-black">N</span>
                    </div>
                    <span className="text-[11px] text-amber-400/80 font-semibold tracking-wider uppercase">Nudge</span>
                  </div>
                  <p className="text-white font-bold text-lg">Loaded Fries</p>
                  <p className="text-amber-200/50 text-sm mt-1.5">
                    "Most people add fries with a burger" &middot; $5.99
                  </p>
                  <div className="flex gap-3 mt-4">
                    <button className="flex-1 bg-gradient-to-r from-amber-500 to-orange-500 text-black font-bold py-2.5 rounded-lg text-sm">
                      Add to Order
                    </button>
                    <button className="px-5 py-2.5 text-white/30 text-sm rounded-lg border border-white/[0.08]">
                      Dismiss
                    </button>
                  </div>
                </div>
              </div>
            </div>
            {/* Key points */}
            <div className="flex-1 space-y-7">
              <h2 className="text-[2.8rem] font-bold leading-tight">
                What the cashier sees
              </h2>
              <p className="text-lg text-white/40 leading-relaxed">
                A non-intrusive card slides in with the item, price, and a conversational
                reason to offer it. One tap adds it to the order.
              </p>
              <div className="space-y-4">
                {[
                  'Suggestions from the merchant\'s actual inventory',
                  'Never suggests items already in the order',
                  'Conversational phrase staff can say out loud',
                  'Large tap targets for busy, wet hands',
                  'Auto-dismisses after 15 seconds',
                ].map((text) => (
                  <div key={text} className="flex items-start gap-3">
                    <div className="w-1.5 h-1.5 rounded-full bg-amber-400 mt-2.5 shrink-0" />
                    <span className="text-[15px] text-white/50">{text}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
        <Notes>
          "Here's what the cashier sees. The card slides in at the bottom — item, price,
          and a phrase to say out loud. 'Most people add fries with a burger.' One tap, it's
          on the order. Dismiss, or wait 15 seconds and it goes away on its own.
          Suggestions always come from the merchant's actual inventory — never hallucinated.
          Never suggests something already in the order. Large tap targets because hands
          are busy, sometimes dirty. This is designed for a real merchant, not a demo."
        </Notes>
      </Slide>,

      // ====== SLIDE 5: HOW IT WORKS / TECH PROOF (20s) ======
      <Slide>
        <div className="flex flex-col justify-center h-full bg-[#0a0a0a] text-white px-20 py-14">
          <h2 className="text-4xl font-bold mb-10">Native Clover. Real AI. Under 2 seconds.</h2>
          <div className="flex gap-5 items-stretch">
            {/* Clover Device */}
            <div className="flex-1 bg-white/[0.04] border border-white/[0.08] rounded-xl p-6">
              <p className="text-[11px] text-green-400 font-semibold tracking-wider uppercase mb-4">
                On Device
              </p>
              <div className="space-y-2">
                {[
                  ['InventoryConnector', 'Loads full menu, caches locally'],
                  ['OrderConnector', 'Listens for every item addition'],
                  ['SuggestionCardView', 'Animated overlay, one-tap add'],
                  ['StatsManager', 'Tracks acceptance rate + revenue'],
                ].map(([title, desc]) => (
                  <div key={title} className="bg-white/[0.04] rounded-lg px-4 py-2.5">
                    <p className="text-sm font-medium text-white/70">{title}</p>
                    <p className="text-xs text-white/25 mt-0.5">{desc}</p>
                  </div>
                ))}
              </div>
              <p className="text-[11px] text-white/15 mt-4">Kotlin &middot; Clover SDK v329 &middot; minSdk 17</p>
            </div>
            {/* Arrow */}
            <div className="flex flex-col items-center justify-center gap-1 px-3">
              <p className="text-[10px] text-white/20 tracking-wider">HTTPS</p>
              <div className="text-white/15 text-xl">&#8644;</div>
              <p className="text-[10px] text-white/20">&lt;2s</p>
            </div>
            {/* AI Engine */}
            <div className="flex-1 bg-white/[0.04] border border-white/[0.08] rounded-xl p-6">
              <p className="text-[11px] text-amber-400 font-semibold tracking-wider uppercase mb-4">
                AI Engine
              </p>
              <div className="space-y-2">
                {[
                  ['Menu context', 'Inventory-aware, category-structured'],
                  ['Order analysis', 'Current items, avoids duplicates'],
                  ['Complementary ranking', 'Add-ons > sides > drinks > mains'],
                  ['Natural language reason', '"Most people add fries with a burger"'],
                ].map(([title, desc]) => (
                  <div key={title} className="bg-white/[0.04] rounded-lg px-4 py-2.5">
                    <p className="text-sm font-medium text-white/70">{title}</p>
                    <p className="text-xs text-white/25 mt-0.5">{desc}</p>
                  </div>
                ))}
              </div>
              <p className="text-[11px] text-white/15 mt-4">LLM &middot; JSON response &middot; $0.003/merchant/day</p>
            </div>
            {/* Arrow */}
            <div className="flex flex-col items-center justify-center gap-1 px-3">
              <div className="text-white/15 text-xl">&#8594;</div>
            </div>
            {/* Stats */}
            <div className="w-[200px] bg-white/[0.04] border border-white/[0.08] rounded-xl p-6">
              <p className="text-[11px] text-blue-400 font-semibold tracking-wider uppercase mb-4">
                Dashboard
              </p>
              <div className="space-y-4">
                <div>
                  <p className="text-2xl font-black">47</p>
                  <p className="text-[11px] text-white/25">suggestions today</p>
                </div>
                <div>
                  <p className="text-2xl font-black text-green-400">66%</p>
                  <p className="text-[11px] text-white/25">acceptance rate</p>
                </div>
                <div>
                  <p className="text-2xl font-black text-amber-400">+$186</p>
                  <p className="text-[11px] text-white/25">added revenue</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <Notes>
          "Under the hood: Nudge hooks directly into Clover's official Android SDK.
          InventoryConnector loads the full menu and caches it. OrderConnector listens
          for every item addition in real time. The AI analyzes the menu context and current
          order, returns a structured suggestion in under 2 seconds. Cost? Three-tenths of a
          cent per merchant per day. And the stats dashboard gives merchants daily proof:
          suggestions shown, acceptance rate, revenue added."
        </Notes>
      </Slide>,

      // ====== SLIDE 6: TRACTION + BUSINESS MODEL (20s) ======
      <Slide>
        <div className="flex h-full bg-[#0a0a0a] text-white">
          {/* Left: Impact numbers */}
          <div className="flex-1 flex flex-col justify-center px-20">
            <h2 className="text-4xl font-bold mb-10">The math works</h2>
            <div className="grid grid-cols-2 gap-5">
              <div className="bg-green-500/[0.06] border border-green-500/15 rounded-xl p-6">
                <p className="text-[3.2rem] font-black text-green-400 leading-none">15-30%</p>
                <p className="text-sm text-green-300/40 mt-2">avg ticket increase with AI recs</p>
              </div>
              <div className="bg-amber-500/[0.06] border border-amber-500/15 rounded-xl p-6">
                <p className="text-[3.2rem] font-black text-amber-400 leading-none">$500+</p>
                <p className="text-sm text-amber-300/40 mt-2">extra daily revenue per merchant</p>
              </div>
              <div className="bg-blue-500/[0.06] border border-blue-500/15 rounded-xl p-6">
                <p className="text-[3.2rem] font-black text-blue-400 leading-none">91%</p>
                <p className="text-sm text-blue-300/40 mt-2">of SMBs with AI say it boosts revenue</p>
              </div>
              <div className="bg-purple-500/[0.06] border border-purple-500/15 rounded-xl p-6">
                <p className="text-[3.2rem] font-black text-purple-400 leading-none">35%</p>
                <p className="text-sm text-purple-300/40 mt-2">of Amazon revenue from AI recs</p>
              </div>
            </div>
          </div>
          {/* Right: Business model */}
          <div className="w-[380px] flex flex-col justify-center pr-16">
            <div className="bg-white/[0.04] border border-white/[0.08] rounded-2xl p-8">
              <p className="text-xs text-white/30 uppercase tracking-wider mb-1">Pricing</p>
              <div className="flex items-baseline gap-1.5">
                <p className="text-5xl font-black">$49</p>
                <p className="text-lg text-white/30">/mo</p>
              </div>
              <p className="text-sm text-white/25 mt-1">30-day free trial</p>
              <div className="border-t border-white/[0.06] my-5" />
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-white/40">Net after Clover 30%</span>
                  <span className="text-white/60">$34.30</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-white/40">AI cost per merchant</span>
                  <span className="text-white/60">$2.70/mo</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-white/40">Gross margin</span>
                  <span className="text-green-400 font-bold">92%</span>
                </div>
              </div>
              <div className="border-t border-white/[0.06] my-5" />
              <p className="text-xs text-white/25">At 1% of Clover merchants</p>
              <p className="text-2xl font-black text-amber-400 mt-1">$4.1M ARR</p>
            </div>
          </div>
        </div>
        <Notes>
          "The math: AI recommendations increase average ticket 15-30%. For a merchant
          doing 100 transactions a day, that's $500+ in extra revenue — daily.
          We charge $49 a month. Clover takes 30%, we net $34. AI costs us under $3.
          That's a 92% gross margin. At just 1% of Clover's 700,000 merchants —
          $4.1 million ARR. And the merchant? The app pays for itself in the first hour."
        </Notes>
      </Slide>,

      // ====== SLIDE 7: VISION + ASK (25s) ======
      <Slide>
        <div className="flex flex-col items-center justify-center h-full bg-[#0a0a0a] text-white relative">
          <div className="absolute inset-0 bg-gradient-to-t from-amber-950/10 via-transparent to-transparent" />
          <div className="relative z-10 text-center px-20">
            <div className="flex items-center justify-center gap-4 mb-6">
              <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center shadow-lg shadow-amber-500/15">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 2L2 7l10 5 10-5-10-5z" />
                  <path d="M2 17l10 5 10-5" />
                  <path d="M2 12l10 5 10-5" />
                </svg>
              </div>
              <h1 className="text-6xl font-black tracking-tight">Nudge</h1>
            </div>
            <p className="text-[1.7rem] text-white/40 font-light max-w-2xl mx-auto leading-relaxed mb-4">
              Merchants leave money on the table every transaction.
            </p>
            <p className="text-[1.7rem] font-medium max-w-2xl mx-auto leading-relaxed">
              <span className="text-amber-400">Nudge fixes that. One tap at a time.</span>
            </p>

            {/* Roadmap teaser */}
            <div className="flex items-center justify-center gap-4 mt-12 mb-10">
              <div className="px-4 py-2 rounded-lg bg-green-500/10 border border-green-500/20 text-sm">
                <span className="text-green-400 font-semibold">Now:</span>
                <span className="text-white/40 ml-1.5">Smart Upsell</span>
              </div>
              <span className="text-white/10">&rarr;</span>
              <div className="px-4 py-2 rounded-lg bg-white/[0.04] border border-white/[0.08] text-sm text-white/30">
                Customer Intelligence
              </div>
              <span className="text-white/10">&rarr;</span>
              <div className="px-4 py-2 rounded-lg bg-white/[0.04] border border-white/[0.08] text-sm text-white/30">
                NL Insights
              </div>
              <span className="text-white/10">&rarr;</span>
              <div className="px-4 py-2 rounded-lg bg-white/[0.04] border border-white/[0.08] text-sm text-white/30">
                Agentic AI
              </div>
            </div>

            {/* Bottom line */}
            <div className="flex items-center justify-center gap-6 text-sm text-white/20">
              <span>Ready for Clover App Market</span>
              <span>&middot;</span>
              <span>$49/mo &middot; 92% margin</span>
              <span>&middot;</span>
              <span>First mover on 700K merchants</span>
            </div>
          </div>
        </div>
        <Notes>
          "Merchants leave money on the table every single transaction. Nudge fixes that.
          One tap at a time. Today: smart upsell, built and working on Clover.
          Next: customer intelligence, natural language insights, and eventually agentic AI
          that doesn't just suggest — but acts. We're ready for the Clover App Market.
          We're looking for a Fiserv product champion to open that door.
          Thank you."
        </Notes>
      </Slide>
  ]
}
