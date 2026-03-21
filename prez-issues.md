# Prez Issues & Workarounds

## Issue 1: GitHub install missing `dist/` folder
- **Problem**: `prez` package installed via `github:Enriquefft/prez` does not include built artifacts (`dist/`). The `package.json` points `main`/`module`/`exports` at `./dist/index.js` which doesn't exist in the source checkout.
- **Error**: `Failed to resolve entry for package "prez". The package may have incorrect main/module/exports specified in its package.json.`
- **Root Cause**: The `files` field in package.json specifies `["dist", "template"]`, but `dist/` is a build artifact not committed to git. The setup script installs from GitHub source, not a published npm package.
- **Workaround**: Added a Vite alias in `vite.config.ts` to resolve `prez` directly from source:
  ```ts
  resolve: {
    alias: {
      prez: path.resolve(__dirname, 'node_modules/prez/src/index.ts'),
    },
  },
  ```
- **Suggested Fix**: Either publish to npm, or add an `exports` field with a `source` condition, or include a `postinstall` script that runs `tsup` to build.

## Issue 2: `setup.sh` pins `prez@^0.1.0` (not published on npm)
- **Problem**: The setup script creates a `package.json` with `"prez": "^0.1.0"` but the npm `prez` package is a completely different project (starts at v1.0.1, currently v4.7.0).
- **Workaround**: Manually changed the dependency to `"prez": "github:Enriquefft/prez"`.
- **Suggested Fix**: Publish the package to npm under a scoped name (e.g., `@prez-dev/prez`) or update setup.sh to use the GitHub reference.

## Issue 3: `Children.toArray` doesn't flatten component wrappers
- **Problem**: The SKILL.md says to "Export a default component that returns `<>` with `<Slide>` children" and `main.tsx` uses `<Deck><Slides /></Deck>`. But `Deck` uses `Children.toArray(children)` to count slides. Since `<Slides />` is a component (not a Fragment literal), `Children.toArray` sees it as 1 child. Result: `totalSlides = 1`, navigation doesn't work.
- **Root Cause**: React 18's `Children.toArray` flattens Fragment *elements* that are direct JSX children, but NOT components that *return* Fragments. `<Slides />` is a React element of type `Slides` (a function), not of type `Fragment`.
- **Workaround**: Changed `slides.tsx` to export a function returning an **array** (not a Fragment), and called it as `{slides()}` in main.tsx:
  ```tsx
  // slides.tsx
  export default function slides() { return [ <Slide>...</Slide>, <Slide>...</Slide> ] }
  // main.tsx
  <Deck>{slides()}</Deck>
  ```
- **Suggested Fix**: Either document that slides must be passed as an array (not a component), or have `Deck` recursively render and count children after the React tree is mounted.

## Issue 4: JSX comments not valid in array context
- **Problem**: When slides are returned as an array `[<Slide/>, <Slide/>]`, JSX-style comments `{/* comment */}` between elements cause parse errors ("Unexpected token, expected ','"). Inside JSX (Fragment or element children), `{/* */}` is valid. In a JS array literal, it's parsed as `{ /* block comment */ }` which is an empty object expression.
- **Workaround**: Use `// comment` (JS line comments) between array elements, keep `{/* comment */}` inside JSX element children.
- **Suggested Fix**: Document this caveat in SKILL.md, or switch to Fragment-based approach with proper Deck child detection.

## Issue 5: Headless Chrome cannot navigate slides via hash
- **Problem**: When taking screenshots with headless Chrome (no Puppeteer), navigating to `http://localhost:5173/#/3` always renders slide 0. The `initialSlide()` function in `useNavigation` reads the hash on mount, but headless Chrome's single-pass mode takes the screenshot before React hydrates.
- **Workaround**: Use Puppeteer with `waitUntil: 'domcontentloaded'` + 2-second delay, opening a **fresh page for each slide** (in-page navigation via keyboard or hash change doesn't work reliably in headless).
- **Note**: Vite's HMR websocket causes `waitUntil: 'networkidle0'` to timeout — use `'domcontentloaded'` instead.

## Issue 6: `export:pdf` renders portrait/letter instead of 16:9 landscape
- **Problem**: The default `export-pdf.ts` uses Chrome's `--print-to-pdf` CLI flag, which outputs letter-size portrait pages. Slides are cropped on the right and stacked vertically with wrong aspect ratio.
- **Root Cause**: Chrome CLI `--print-to-pdf` doesn't support custom paper dimensions. It uses the OS default print settings (letter, portrait).
- **Workaround**: Rewrote `export-pdf.ts` to use `puppeteer-core` with `page.pdf()` which supports custom `width`/`height` and zero margins:
  ```ts
  await page.pdf({ path: output, width: '1280px', height: '720px', printBackground: true, margin: { top: 0, right: 0, bottom: 0, left: 0 } })
  ```
  Added `puppeteer-core` as a dev dependency (uses system Chrome, no download).
- **Suggested Fix**: Update prez's default `export-pdf.ts` template to use puppeteer-core instead of raw Chrome CLI.
