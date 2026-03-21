import { getChrome } from './find-chrome'

const url = process.argv[2] || 'http://localhost:5173'
const output = process.argv[3] || 'deck.pdf'
const chrome = getChrome()

console.log(`Exporting PDF from ${url}`)

async function exportPdf() {
  const puppeteer = await import('puppeteer-core')

  const browser = await puppeteer.default.launch({
    headless: true,
    executablePath: chrome,
    args: ['--no-sandbox', '--disable-dev-shm-usage'],
  })

  // Detect slide count
  let firstPage = await browser.newPage()
  await firstPage.setViewport({ width: 1280, height: 720 })
  await firstPage.goto(url, { waitUntil: 'domcontentloaded', timeout: 15000 })
  await new Promise(r => setTimeout(r, 2000))
  const totalSlides = await firstPage.evaluate(() => {
    const el = document.querySelector('[data-prez-total]')
    return el ? parseInt(el.getAttribute('data-prez-total') || '1') : 1
  })
  await firstPage.close()
  console.log(`Found ${totalSlides} slides`)

  // Screenshot each slide as base64 PNG
  const screenshots: string[] = []
  for (let i = 0; i < totalSlides; i++) {
    const page = await browser.newPage()
    await page.setViewport({ width: 1280, height: 720 })
    await page.goto(`${url}#/${i}`, { waitUntil: 'domcontentloaded', timeout: 15000 })
    await new Promise(r => setTimeout(r, 1500))
    const b64 = await page.screenshot({ type: 'png', encoding: 'base64' })
    screenshots.push(b64 as string)
    await page.close()
    console.log(`  Slide ${i + 1}/${totalSlides}`)
  }

  // Render all screenshots as one-per-page HTML, then print to PDF
  const html = `<!DOCTYPE html><html><head>
<style>
  @page { size: 1280px 720px; margin: 0; }
  * { margin: 0; padding: 0; }
  .slide { width: 1280px; height: 720px; page-break-after: always; break-after: page; overflow: hidden; }
  .slide img { width: 100%; height: 100%; display: block; }
</style>
</head><body>
${screenshots.map(s => `<div class="slide"><img src="data:image/png;base64,${s}"></div>`).join('\n')}
</body></html>`

  const pdfPage = await browser.newPage()
  await pdfPage.setContent(html, { waitUntil: 'load' })
  await new Promise(r => setTimeout(r, 1000))

  await pdfPage.pdf({
    path: output,
    width: '1280px',
    height: '720px',
    printBackground: true,
    margin: { top: 0, right: 0, bottom: 0, left: 0 },
  })

  await browser.close()
  console.log(`Exported ${totalSlides} slides to ${output}`)
}

exportPdf().catch(e => {
  console.error(e)
  process.exit(1)
})
