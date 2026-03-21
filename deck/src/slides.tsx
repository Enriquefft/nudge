import { Slide, Notes } from '@enriquefft/prez'
import { useTranslations, type Translations } from './i18n'

function createSlides(t: Translations) {
  return (
    <>
      {/* 1. TITLE */}
      <Slide>
        <div className="slide-bg-dark flex flex-col items-center justify-center h-full relative overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-br from-[#0a1f15] via-[#0f2e1f] to-[#061210]" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full bg-[#1B6B4A]/10 blur-[120px]" />
          <div className="relative z-10 flex flex-col items-center">
            <img src="/nudge-icon.png" alt="Nudge" className="w-20 h-20 rounded-2xl mb-8 shadow-2xl" />
            <h1 className="text-8xl font-bold text-white tracking-tight mb-4">{t.title.name}</h1>
            <div className="w-16 h-1 bg-[#E8720C] rounded-full mb-6" />
            <p className="text-2xl text-white/60 font-light tracking-wide">{t.title.tagline}</p>
            <p className="mt-8 text-sm text-white/30 tracking-widest uppercase">{t.title.event}</p>
          </div>
        </div>
        <Notes>{t.notes.title}</Notes>
      </Slide>

      {/* 2. THE PROBLEM */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.problem.label}</p>
          <h2 className="text-6xl font-bold mb-16 leading-tight max-w-[800px]">
            {t.problem.heading1}<br />
            <span className="text-[#E8720C]">{t.problem.heading2}</span>
          </h2>
          <div className="grid grid-cols-3 gap-8">
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03]">
              <p className="text-5xl font-bold text-[#4ADE80] mb-3">{t.problem.stat1}</p>
              <p className="text-white/50 text-base leading-relaxed">{t.problem.stat1desc} <span className="text-white/80 font-medium">{t.problem.stat1bold}</span> {t.problem.stat1rest}</p>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03]">
              <p className="text-5xl font-bold text-[#4ADE80] mb-3">{t.problem.stat2}</p>
              <p className="text-white/50 text-base leading-relaxed">{t.problem.stat2desc} <span className="text-white/80 font-medium">{t.problem.stat2bold}</span></p>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03]">
              <p className="text-5xl font-bold text-[#4ADE80] mb-3">{t.problem.stat3}</p>
              <p className="text-white/50 text-base leading-relaxed">{t.problem.stat3desc} <span className="text-white/80 font-medium">{t.problem.stat3bold}</span> {t.problem.stat3rest}</p>
            </div>
          </div>
          <p className="mt-10 text-white/30 text-sm">{t.problem.sources}</p>
        </div>
        <Notes>{t.notes.problem}</Notes>
      </Slide>

      {/* 3. THE SOLUTION */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.solution.label}</p>
          <h2 className="text-6xl font-bold mb-16 leading-tight">{t.solution.heading}</h2>
          <div className="flex items-center gap-6 max-w-[900px]">
            <div className="flex-1 text-center">
              <div className="w-16 h-16 rounded-2xl bg-[#1B6B4A] flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">1</span>
              </div>
              <p className="text-lg font-semibold mb-2">{t.solution.step1}</p>
              <p className="text-white/40 text-sm">{t.solution.step1desc}</p>
            </div>
            <div className="text-[#4ADE80]/40 text-3xl">→</div>
            <div className="flex-1 text-center">
              <div className="w-16 h-16 rounded-2xl bg-[#1B6B4A] flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">2</span>
              </div>
              <p className="text-lg font-semibold mb-2">{t.solution.step2}</p>
              <p className="text-white/40 text-sm">{t.solution.step2desc}</p>
            </div>
            <div className="text-[#4ADE80]/40 text-3xl">→</div>
            <div className="flex-1 text-center">
              <div className="w-16 h-16 rounded-2xl bg-[#1B6B4A] flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">3</span>
              </div>
              <p className="text-lg font-semibold mb-2">{t.solution.step3}</p>
              <p className="text-white/40 text-sm">{t.solution.step3desc}</p>
            </div>
            <div className="text-[#4ADE80]/40 text-3xl">→</div>
            <div className="flex-1 text-center">
              <div className="w-16 h-16 rounded-2xl bg-[#E8720C] flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">4</span>
              </div>
              <p className="text-lg font-semibold mb-2">{t.solution.step4}</p>
              <p className="text-white/40 text-sm">{t.solution.step4desc}</p>
            </div>
          </div>
        </div>
        <Notes>{t.notes.solution}</Notes>
      </Slide>

      {/* 4. PRODUCT */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white flex items-center justify-center p-16">
          <div className="flex items-center gap-16 max-w-[1100px]">
            <div className="flex-1">
              <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.product.label}</p>
              <h2 className="text-5xl font-bold mb-8 leading-tight">{t.product.heading1}<br />{t.product.heading2}</h2>
              <div className="space-y-5">
                <div className="flex items-start gap-4">
                  <div className="w-2 h-2 rounded-full bg-[#4ADE80] mt-2 flex-shrink-0" />
                  <p className="text-white/70 text-lg"><span className="text-white font-medium">{t.product.f1bold}</span> — {t.product.f1}</p>
                </div>
                <div className="flex items-start gap-4">
                  <div className="w-2 h-2 rounded-full bg-[#4ADE80] mt-2 flex-shrink-0" />
                  <p className="text-white/70 text-lg"><span className="text-white font-medium">{t.product.f2bold}</span> — {t.product.f2}</p>
                </div>
                <div className="flex items-start gap-4">
                  <div className="w-2 h-2 rounded-full bg-[#4ADE80] mt-2 flex-shrink-0" />
                  <p className="text-white/70 text-lg"><span className="text-white font-medium">{t.product.f3bold}</span> — {t.product.f3}</p>
                </div>
                <div className="flex items-start gap-4">
                  <div className="w-2 h-2 rounded-full bg-[#4ADE80] mt-2 flex-shrink-0" />
                  <p className="text-white/70 text-lg"><span className="text-white font-medium">{t.product.f4bold}</span> — {t.product.f4}</p>
                </div>
              </div>
            </div>
            <div className="flex-1 flex justify-center">
              <img src="/nudge-cover.png" alt={t.product.imgAlt} className="w-full max-w-[480px] rounded-2xl shadow-2xl border border-white/10" />
            </div>
          </div>
        </div>
        <Notes>{t.notes.product}</Notes>
      </Slide>

      {/* 5. FIELD VALIDATION */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.validation.label}</p>
          <h2 className="text-6xl font-bold mb-6 leading-tight">
            {t.validation.heading1}<br />
            <span className="text-[#4ADE80]">{t.validation.heading2}</span>
          </h2>
          <p className="text-xl text-white/50 mb-12 max-w-[700px]">{t.validation.sub}</p>
          <div className="grid grid-cols-4 gap-6 mb-12">
            <div className="border border-white/10 rounded-2xl p-6 bg-white/[0.03] text-center">
              <p className="text-4xl font-bold text-[#4ADE80] mb-2">{t.validation.stat1}</p>
              <p className="text-white/40 text-sm">{t.validation.stat1label}</p>
            </div>
            <div className="border border-white/10 rounded-2xl p-6 bg-white/[0.03] text-center">
              <p className="text-4xl font-bold text-white mb-2">{t.validation.stat2}</p>
              <p className="text-white/40 text-sm">{t.validation.stat2label}</p>
            </div>
            <div className="border border-[#E8720C]/30 rounded-2xl p-6 bg-[#E8720C]/[0.06] text-center">
              <p className="text-4xl font-bold text-[#E8720C] mb-2">{t.validation.stat3}</p>
              <p className="text-white/40 text-sm">{t.validation.stat3label}</p>
            </div>
            <div className="border border-[#E8720C]/30 rounded-2xl p-6 bg-[#E8720C]/[0.06] text-center">
              <p className="text-4xl font-bold text-[#E8720C] mb-2">{t.validation.stat4}</p>
              <p className="text-white/40 text-sm">{t.validation.stat4label}</p>
            </div>
          </div>
          <div className="border-l-2 border-[#4ADE80]/40 pl-6 max-w-[600px]">
            <p className="text-2xl text-white/80 italic leading-relaxed">{t.validation.quote}</p>
            <p className="text-white/30 mt-2 text-sm">{t.validation.quoteAttr}</p>
          </div>
        </div>
        <Notes>{t.notes.validation}</Notes>
      </Slide>

      {/* 6. MARKET INSIGHT */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.market.label}</p>
          <h2 className="text-6xl font-bold mb-6 leading-tight">
            {t.market.heading1}<br />
            <span className="text-[#4ADE80]">{t.market.heading2}</span>
          </h2>
          <p className="text-xl text-white/50 mb-12 max-w-[700px]">{t.market.sub}</p>
          <div className="grid grid-cols-3 gap-8">
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03]">
              <p className="text-3xl mb-3">{t.market.v1icon}</p>
              <p className="text-xl font-semibold mb-2">{t.market.v1}</p>
              <p className="text-white/40 text-sm leading-relaxed">{t.market.v1desc}</p>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03]">
              <p className="text-3xl mb-3">{t.market.v2icon}</p>
              <p className="text-xl font-semibold mb-2">{t.market.v2}</p>
              <p className="text-white/40 text-sm leading-relaxed">{t.market.v2desc}</p>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03]">
              <p className="text-3xl mb-3">{t.market.v3icon}</p>
              <p className="text-xl font-semibold mb-2">{t.market.v3}</p>
              <p className="text-white/40 text-sm leading-relaxed">{t.market.v3desc}</p>
            </div>
          </div>
          <div className="mt-10 border border-[#4ADE80]/20 rounded-xl p-5 bg-[#4ADE80]/[0.04] max-w-[700px]">
            <p className="text-white/70 text-base"><span className="text-[#4ADE80] font-semibold">{t.market.advantage}</span> {t.market.advantageDesc}</p>
          </div>
        </div>
        <Notes>{t.notes.market}</Notes>
      </Slide>

      {/* 7. MARKET DATA */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.data.label}</p>
          <h2 className="text-5xl font-bold mb-14 leading-tight">{t.data.heading}</h2>
          <div className="grid grid-cols-2 gap-8 max-w-[900px]">
            <div className="relative border border-white/10 rounded-2xl p-10 bg-white/[0.03] overflow-hidden">
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#E8720C] to-[#FF8F2E]" />
              <p className="text-6xl font-bold text-[#E8720C] mb-1">{t.data.s1}</p>
              <p className="text-white/70 text-lg">{t.data.s1desc}</p>
              <div className="mt-4 flex items-center gap-2">
                <p className="text-3xl font-bold text-white/30">{t.data.s1sub}</p>
                <p className="text-white/30 text-sm">{t.data.s1subdesc}</p>
              </div>
            </div>
            <div className="relative border border-white/10 rounded-2xl p-10 bg-white/[0.03] overflow-hidden">
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#4ADE80] to-[#22C55E]" />
              <p className="text-6xl font-bold text-[#4ADE80] mb-1">{t.data.s2}</p>
              <p className="text-white/70 text-lg">{t.data.s2desc}</p>
              <p className="mt-4 text-white/30 text-sm">{t.data.s2sub}</p>
            </div>
            <div className="relative border border-white/10 rounded-2xl p-10 bg-white/[0.03] overflow-hidden">
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#4ADE80] to-[#22C55E]" />
              <p className="text-6xl font-bold text-[#4ADE80] mb-1">{t.data.s3}</p>
              <p className="text-white/70 text-lg">{t.data.s3desc}</p>
              <p className="mt-4 text-white/30 text-sm">{t.data.s3sub}</p>
            </div>
            <div className="relative border border-white/10 rounded-2xl p-10 bg-white/[0.03] overflow-hidden">
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#E8720C] to-[#FF8F2E]" />
              <p className="text-6xl font-bold text-[#E8720C] mb-1">{t.data.s4}</p>
              <p className="text-white/70 text-lg">{t.data.s4desc}</p>
              <p className="mt-4 text-white/30 text-sm">{t.data.s4sub}</p>
            </div>
          </div>
        </div>
        <Notes>{t.notes.data}</Notes>
      </Slide>

      {/* 8. COMPETITIVE LANDSCAPE */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.competitive.label}</p>
          <h2 className="text-5xl font-bold mb-14 leading-tight">
            {t.competitive.heading1} <span className="text-[#E8720C]">{t.competitive.heading2}</span> {t.competitive.heading3}
          </h2>
          <div className="grid grid-cols-4 gap-5">
            <div className="border border-[#4ADE80]/30 rounded-2xl p-6 bg-[#4ADE80]/[0.06]">
              <p className="text-lg font-bold text-[#4ADE80] mb-1">{t.competitive.nudge}</p>
              <p className="text-xs text-white/30 mb-4">{t.competitive.nudgeSub}</p>
              <div className="space-y-2 text-sm">
                <p className="text-white/70">✓ {t.competitive.n1}</p>
                <p className="text-white/70">✓ {t.competitive.n2}</p>
                <p className="text-white/70">✓ {t.competitive.n3}</p>
                <p className="text-white/70">✓ {t.competitive.n4}</p>
              </div>
            </div>
            <div className="border border-white/10 rounded-2xl p-6 bg-white/[0.03]">
              <p className="text-lg font-bold text-white/60 mb-1">{t.competitive.toast}</p>
              <p className="text-xs text-white/30 mb-4">{t.competitive.toastSub}</p>
              <div className="space-y-2 text-sm">
                <p className="text-white/40">✓ {t.competitive.t1}</p>
                <p className="text-white/40">✗ {t.competitive.t2}</p>
                <p className="text-white/40">✗ {t.competitive.t3}</p>
                <p className="text-white/40">✗ {t.competitive.t4}</p>
              </div>
            </div>
            <div className="border border-white/10 rounded-2xl p-6 bg-white/[0.03]">
              <p className="text-lg font-bold text-white/60 mb-1">{t.competitive.lightspeed}</p>
              <p className="text-xs text-white/30 mb-4">{t.competitive.lightspeedSub}</p>
              <div className="space-y-2 text-sm">
                <p className="text-white/40">✗ {t.competitive.l1}</p>
                <p className="text-white/40">✗ {t.competitive.l2}</p>
                <p className="text-white/40">✗ {t.competitive.l3}</p>
                <p className="text-white/40">✗ {t.competitive.l4}</p>
              </div>
            </div>
            <div className="border border-white/10 rounded-2xl p-6 bg-white/[0.03]">
              <p className="text-lg font-bold text-white/60 mb-1">{t.competitive.clover}</p>
              <p className="text-xs text-white/30 mb-4">{t.competitive.cloverSub}</p>
              <div className="space-y-2 text-sm">
                <p className="text-white/40">✗ {t.competitive.c1}</p>
                <p className="text-white/40">✗ {t.competitive.c2}</p>
                <p className="text-white/40">✗ {t.competitive.c3}</p>
                <p className="text-white/40">✗ {t.competitive.c4}</p>
              </div>
            </div>
          </div>
        </div>
        <Notes>{t.notes.competitive}</Notes>
      </Slide>

      {/* 9. PRODUCT STATUS */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.status.label}</p>
          <h2 className="text-6xl font-bold mb-14 leading-tight">{t.status.heading1} <span className="text-[#4ADE80]">{t.status.heading2}</span></h2>
          <div className="grid grid-cols-2 gap-8 max-w-[900px]">
            <div className="border border-[#4ADE80]/30 rounded-2xl p-8 bg-[#4ADE80]/[0.04]">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-3 h-3 rounded-full bg-[#4ADE80]" />
                <p className="text-sm font-semibold text-[#4ADE80] uppercase tracking-wider">{t.status.prod}</p>
              </div>
              <p className="text-2xl font-bold mb-2">{t.status.prodTitle}</p>
              <p className="text-white/50 text-base leading-relaxed">{t.status.prodDesc}</p>
            </div>
            <div className="border border-[#E8720C]/30 rounded-2xl p-8 bg-[#E8720C]/[0.04]">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-3 h-3 rounded-full bg-[#E8720C]" />
                <p className="text-sm font-semibold text-[#E8720C] uppercase tracking-wider">{t.status.code}</p>
              </div>
              <p className="text-2xl font-bold mb-2">{t.status.codeTitle}</p>
              <p className="text-white/50 text-base leading-relaxed">{t.status.codeDesc}</p>
            </div>
          </div>
          <div className="mt-10 grid grid-cols-4 gap-4 max-w-[900px]">
            <div className="text-center border border-white/10 rounded-xl p-4 bg-white/[0.02]">
              <p className="text-sm text-white/40 mb-1">{t.status.arch1label}</p>
              <p className="text-base font-semibold">{t.status.arch1}</p>
            </div>
            <div className="text-center border border-white/10 rounded-xl p-4 bg-white/[0.02]">
              <p className="text-sm text-white/40 mb-1">{t.status.arch2label}</p>
              <p className="text-base font-semibold">{t.status.arch2}</p>
            </div>
            <div className="text-center border border-white/10 rounded-xl p-4 bg-white/[0.02]">
              <p className="text-sm text-white/40 mb-1">{t.status.arch3label}</p>
              <p className="text-base font-semibold">{t.status.arch3}</p>
            </div>
            <div className="text-center border border-white/10 rounded-xl p-4 bg-white/[0.02]">
              <p className="text-sm text-white/40 mb-1">{t.status.arch4label}</p>
              <p className="text-base font-semibold">{t.status.arch4}</p>
            </div>
          </div>
        </div>
        <Notes>{t.notes.status}</Notes>
      </Slide>

      {/* 10. BUSINESS MODEL */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.business.label}</p>
          <h2 className="text-6xl font-bold mb-14 leading-tight">
            {t.business.heading1}<br />
            <span className="text-[#4ADE80]">{t.business.heading2}</span>
          </h2>
          <div className="grid grid-cols-2 gap-8 max-w-[900px]">
            <div className="border border-white/10 rounded-2xl p-10 bg-white/[0.03]">
              <p className="text-3xl font-bold text-white mb-4">{t.business.saas}</p>
              <p className="text-white/50 text-lg leading-relaxed mb-6">{t.business.saasDesc}</p>
              <div className="border-t border-white/10 pt-4">
                <p className="text-white/30 text-sm">{t.business.saasSub}</p>
              </div>
            </div>
            <div className="border border-[#4ADE80]/20 rounded-2xl p-10 bg-[#4ADE80]/[0.03]">
              <p className="text-3xl font-bold text-[#4ADE80] mb-4">{t.business.fiserv}</p>
              <p className="text-white/50 text-lg leading-relaxed mb-6">{t.business.fiservDesc}</p>
              <div className="border-t border-white/10 pt-4">
                <p className="text-white/30 text-sm">{t.business.fiservSub}</p>
              </div>
            </div>
          </div>
          <div className="mt-10 max-w-[900px] bg-white/[0.03] border border-white/10 rounded-xl p-6 flex items-center gap-6">
            <div className="text-center flex-1">
              <p className="text-3xl font-bold text-white">{t.business.roi1}</p>
              <p className="text-white/40 text-sm">{t.business.roi1label}</p>
            </div>
            <div className="w-px h-12 bg-white/10" />
            <div className="text-center flex-1">
              <p className="text-3xl font-bold text-white">{t.business.roi2}</p>
              <p className="text-white/40 text-sm">{t.business.roi2label}</p>
            </div>
            <div className="w-px h-12 bg-white/10" />
            <div className="text-center flex-1">
              <p className="text-3xl font-bold text-[#4ADE80]">{t.business.roi3}</p>
              <p className="text-white/40 text-sm">{t.business.roi3label}</p>
            </div>
          </div>
        </div>
        <Notes>{t.notes.business}</Notes>
      </Slide>

      {/* 11. THE ASK */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] to-[#061210] text-white p-20 flex flex-col justify-center">
          <p className="text-sm tracking-widest uppercase text-[#E8720C] mb-4 font-semibold">{t.ask.label}</p>
          <h2 className="text-6xl font-bold mb-14 leading-tight">{t.ask.heading1}<br />{t.ask.heading2}</h2>
          <div className="grid grid-cols-2 gap-6 max-w-[800px]">
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03] flex gap-5">
              <span className="text-3xl font-bold text-[#E8720C]">1</span>
              <div>
                <p className="text-lg font-semibold mb-2">{t.ask.a1}</p>
                <p className="text-white/40 text-sm leading-relaxed">{t.ask.a1desc}</p>
              </div>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03] flex gap-5">
              <span className="text-3xl font-bold text-[#E8720C]">2</span>
              <div>
                <p className="text-lg font-semibold mb-2">{t.ask.a2}</p>
                <p className="text-white/40 text-sm leading-relaxed">{t.ask.a2desc}</p>
              </div>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03] flex gap-5">
              <span className="text-3xl font-bold text-[#E8720C]">3</span>
              <div>
                <p className="text-lg font-semibold mb-2">{t.ask.a3}</p>
                <p className="text-white/40 text-sm leading-relaxed">{t.ask.a3desc}</p>
              </div>
            </div>
            <div className="border border-white/10 rounded-2xl p-8 bg-white/[0.03] flex gap-5">
              <span className="text-3xl font-bold text-[#E8720C]">4</span>
              <div>
                <p className="text-lg font-semibold mb-2">{t.ask.a4}</p>
                <p className="text-white/40 text-sm leading-relaxed">{t.ask.a4desc}</p>
              </div>
            </div>
          </div>
        </div>
        <Notes>{t.notes.ask}</Notes>
      </Slide>

      {/* 12. CLOSING */}
      <Slide>
        <div className="h-full bg-gradient-to-br from-[#0a1f15] via-[#0f2e1f] to-[#061210] flex flex-col items-center justify-center relative overflow-hidden">
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] rounded-full bg-[#1B6B4A]/8 blur-[150px]" />
          <div className="relative z-10 flex flex-col items-center text-center">
            <img src="/nudge-icon.png" alt="Nudge" className="w-24 h-24 rounded-2xl mb-10 shadow-2xl" />
            <h2 className="text-7xl font-bold text-white mb-6 leading-tight">
              {t.closing.heading1}<br />
              <span className="text-[#4ADE80]">{t.closing.heading2}</span>
            </h2>
            <div className="w-16 h-1 bg-[#E8720C] rounded-full mb-8" />
            <p className="text-xl text-white/40 mb-2">{t.closing.author}</p>
            <p className="text-base text-white/25">{t.closing.url}</p>
          </div>
        </div>
        <Notes>{t.notes.closing}</Notes>
      </Slide>
    </>
  )
}

export { createSlides }
