# Known Issues and Improvement Opportunities: @enriquefft/prez

This document catalogs issues discovered while building the Nudge pitch deck with the
`@enriquefft/prez` slide framework (v1.0.x). Each entry includes a description of the
problem, its impact, root cause analysis, and the workaround or fix applied.

---

## 1. HTML entities render as literal text in JSX

**Description:**
Named HTML entities such as `&check;`, `&cross;`, `&rarr;`, and `&mdash;` are rendered
as their literal escaped text (e.g., the string `&check;` appears on screen) instead of
the corresponding glyphs.

**Impact:**
Slides that use named entities for symbols (checkmarks, arrows, dashes) display broken
text instead of the intended characters.

**Root cause:**
This is a React/JSX limitation, not a prez bug per se. JSX does not interpret HTML named
entities the way a raw HTML parser does. In JSX, `&check;` is not processed into a
Unicode character -- it is treated as a string literal. Only numeric character references
(`&#x2713;`) or actual Unicode characters work reliably in JSX string content.

The prez README and AI-generated slide examples do not warn about this, which makes it
a documentation gap in the framework.

**Workaround:**
Use Unicode characters directly in the source code instead of named HTML entities:

| Instead of   | Use  | Character name       |
|--------------|------|----------------------|
| `&check;`    | `\u2713` or `\u2714` | Check mark           |
| `&cross;`    | `\u2717` or `\u2718` | Ballot X             |
| `&rarr;`     | `\u2192`             | Rightwards arrow     |
| `&mdash;`    | `\u2014`             | Em dash              |

In practice, pasting the actual character into the `.tsx` file works fine since the
files are UTF-8.

---

## 2. `&cross;` is not a valid HTML entity

**Description:**
`&cross;` is not a standard named HTML entity. Even in plain HTML (outside JSX), it
would not render correctly in all browsers.

**Impact:**
Using `&cross;` produces either literal text or an error, depending on the parser. This
compounds issue #1 because even if prez rendered HTML entities, `&cross;` would still
fail.

**Root cause:**
The correct named entity for a multiplication sign is `&times;` (Unicode U+00D7). For a
ballot/cross mark, there is no standard named entity -- the correct approach is to use
the Unicode character directly: `\u2717` (ballot X) or `\u2718` (heavy ballot X). The
invalid entity likely originated from AI code generation or incorrect documentation
examples that assumed `&cross;` was valid.

**Workaround:**
Use the Unicode character directly: `\u2717` (ballot X) or `\u2718` (heavy ballot X).

---

## 3. PDF export produces only one page

**Description:**
Running `bun run export:pdf` generates a PDF file that contains all slides collapsed
into a single page, regardless of how many slides the deck has (e.g., 12 slides produce
a 1-page PDF).

**Impact:**
The PDF export is unusable for sharing or printing a multi-slide deck. Recipients see
a single very tall page or cropped content instead of individual slides.

**Root cause:**
The prez print mode injects a `<style>` block with:

```css
@page { size: 1280px 720px; margin: 0; }
```

Chrome's `--print-to-pdf` does not reliably honor `@page` size declarations that use
pixel units. The CSS properties `page-break-after: always` and `break-after: page` are
applied to each slide container, but without a valid `@page` size recognized by the
print engine, Chrome treats the entire document as one continuous page.

The export script (`src/export-pdf.ts`) invokes:

```
chrome --headless=new --print-to-pdf="deck.pdf" --no-pdf-header-footer \
  --virtual-time-budget=10000 --run-all-compositor-stages-before-draw \
  "<url>?print=true"
```

There is no `--print-to-pdf-no-header` or paper-size override flag that would fix the
page-size issue.

**Workaround:**
Use the PPTX export instead (`bun run export:pptx`), which works correctly. The PPTX
exporter takes individual screenshots of each slide at 1280x720 and assembles them into
a PowerPoint file using `pptxgenjs`. This produces a proper multi-slide file.

**Suggested fix for prez:**
Change the `@page` size to use recognized units (`mm` or `in`):
```css
@page { size: 338.67mm 190.5mm; margin: 0; }
```
(1280px / 96dpi * 25.4mm = 338.67mm; 720px / 96dpi * 25.4mm = 190.5mm)

Alternatively, the export script could pass `--print-to-pdf` with an explicit paper size
via DevTools Protocol instead of relying on CSS `@page`.

---

## 4. PDF export does not render images

**Description:**
Images in the exported PDF appear as broken image icons (showing alt text) instead of
the actual image content.

**Impact:**
Any slide containing images (logos, product screenshots, etc.) is incomplete in the PDF
export, making it unprofessional for distribution.

**Root cause:**
The deck uses a Vite `base` path (`base: '/deck/'` in `vite.config.ts`), but image
`src` attributes in the slides use root-relative paths like `src="/nudge-icon.png"`.
When Chrome headless loads the print URL (e.g., `http://localhost:5173?print=true`),
the images should resolve to `http://localhost:5173/deck/nudge-icon.png` due to the
base path, but the raw `/nudge-icon.png` path does not include the base prefix.

Additionally, Chrome headless with `--virtual-time-budget` may not wait for images to
fully load before capturing the page.

**Workaround:**
Use the PPTX export, which captures live screenshots of each slide (including rendered
images) rather than relying on print mode. For PDF specifically, one could adjust image
paths to include the base path explicitly or use Vite's asset import system.

---

## 5. No fullscreen button for mobile devices

**Description:**
Fullscreen mode is only accessible via the `F` keyboard shortcut. Mobile devices (phones
and tablets) have no physical keyboard, so users on mobile have no way to enter
fullscreen mode through the framework's built-in controls.

**Impact:**
On mobile browsers (Chrome Android, Firefox Android, Safari iOS), the deck displays with
full browser chrome (address bar, navigation buttons), which takes up significant screen
space and undermines the presentation experience.

**Root cause:**
The prez navigation handler in `use-navigation.ts` binds fullscreen toggling exclusively
to the `keydown` event for the `f`/`F` key:

```js
case "f":
case "F":
  if (!e.metaKey && !e.ctrlKey) {
    e.preventDefault();
    if (document.fullscreenElement) {
      document.exitFullscreen();
    } else {
      containerRef.current?.requestFullscreen();
    }
  }
  break;
```

There is no touch-accessible UI element (button, gesture, etc.) to trigger the same
action. The framework does handle touch swipe for slide navigation, but not for
fullscreen. The Fullscreen API is supported on Chrome Android and Firefox Android, so
the capability exists -- prez simply does not expose it.

**Workaround applied:**
A `<FullscreenButton>` component was added to `src/main.tsx` that renders a fixed-
position floating button in the bottom-right corner. It calls
`document.documentElement.requestFullscreen()` on tap and only renders when the
Fullscreen API is available. The button uses a semi-transparent backdrop-blur style
so it does not obstruct slide content.

**Suggested fix for prez:**
Add an optional built-in fullscreen toggle button, or expose a prop on `<Deck>` like
`showFullscreenButton={true}` that renders one. The button should be touch-friendly
(minimum 44x44px tap target) and auto-hide after a few seconds of inactivity.

---

## 6. No PWA / web app manifest support

**Description:**
The prez framework does not include a web app manifest (`manifest.json`) or the
associated meta tags needed for Progressive Web App behavior. Without these, using
"Add to Home Screen" on mobile opens the deck in a regular browser tab with full
browser chrome.

**Impact:**
For presentations delivered via a hosted URL (e.g., on Vercel or GitHub Pages), the
ideal mobile experience is a standalone/fullscreen app launched from the home screen.
Without a manifest, users always see browser UI, which degrades the presentation
experience on mobile and tablets.

**Root cause:**
The prez scaffold (`setup.sh`) generates an `index.html` with minimal meta tags. It
does not include:
- A `<link rel="manifest" href="...">` tag
- `<meta name="apple-mobile-web-app-capable" content="yes">`
- `<meta name="mobile-web-app-capable" content="yes">`
- `<meta name="theme-color" content="...">`
- `<meta name="apple-mobile-web-app-status-bar-style" content="...">`

**Workaround applied:**
A `manifest.json` was manually created in `deck/public/` with:

```json
{
  "name": "Nudge -- Pitch Deck",
  "short_name": "Nudge Deck",
  "start_url": "/deck/",
  "display": "fullscreen",
  "orientation": "landscape",
  "background_color": "#0a1f15",
  "theme_color": "#0a1f15",
  "icons": [
    {
      "src": "/deck/nudge-icon.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

The following meta tags were added to `index.html`:

```html
<meta name="viewport" content="width=device-width, initial-scale=1.0,
  maximum-scale=1.0, user-scalable=no, viewport-fit=cover" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
<meta name="theme-color" content="#0a1f15" />
<link rel="manifest" href="/deck/manifest.json" />
```

**Suggested fix for prez:**
The `setup.sh` scaffold should generate a default `manifest.json` and include the
required meta tags in `index.html`. Sensible defaults would be:
- `"display": "fullscreen"` (presentations should fill the screen)
- `"orientation": "landscape"` (slides are 16:9)
- Theme color derived from the deck background

---

## 7. Fullscreen API not functional on Firefox Android

**Description:**
The Fullscreen API (`document.documentElement.requestFullscreen()`) does not work on
Firefox for Android. The floating fullscreen button we added (issue #5 workaround) either
does not appear (API reports as unsupported) or fails silently when tapped.

**Impact:**
There is no way to enter fullscreen on Firefox Android -- neither the `F` key shortcut
nor the custom button nor any browser-level UI. The deck always shows with full browser
chrome (address bar, toolbar).

**Root cause:**
Firefox for Android has historically had incomplete or broken Fullscreen API support.
Unlike Chrome Android, which reliably supports `requestFullscreen()`, Firefox Android
either does not expose the API or silently rejects the call. This is a browser limitation,
not a prez bug, but prez offers no alternative for this scenario.

**Workaround:**
Use Chrome on Android for presenting. For Firefox users, the deck is still navigable
via touch swipe but will always show browser UI.

---

## 8. PWA "Add to Home Screen" creates a bookmark, not a standalone app (Firefox Android)

**Description:**
On Firefox for Android, "Add to Home Screen" creates a simple URL shortcut that opens
in a regular Firefox tab with full browser chrome. The `manifest.json` with
`"display": "fullscreen"` is ignored.

**Impact:**
The PWA standalone/fullscreen mode we configured does not work on Firefox Android. The
home screen icon launches a normal browser tab, defeating the purpose of the manifest.

**Root cause:**
Firefox for Android does not support installing Progressive Web Apps. Unlike Chrome
Android (which reads `manifest.json` and launches PWAs in standalone/fullscreen mode),
Firefox only creates a bookmark-style shortcut. The manifest `display` property is
completely ignored. This is a long-standing Firefox limitation.

**Workaround:**
Use Chrome on Android for the "Add to Home Screen" PWA experience. Alternatively,
share the PPTX file for offline mobile viewing.

---

## Summary table

| # | Issue                              | Severity | Prez bug? | Workaround available? |
|---|-------------------------------------|----------|-----------|----------------------|
| 1 | HTML entities literal in JSX        | Medium   | Doc gap   | Yes (use Unicode)    |
| 2 | `&cross;` invalid entity            | Low      | Doc gap   | Yes (use Unicode)    |
| 3 | PDF export: single page             | High     | Yes       | Yes (use PPTX)       |
| 4 | PDF export: broken images           | High     | Yes       | Yes (use PPTX)       |
| 5 | No fullscreen button for mobile     | Medium   | Yes       | Yes (custom button)  |
| 6 | No PWA/manifest support             | Medium   | Yes       | Yes (manual setup)   |
| 7 | Fullscreen API broken on Firefox Android | High | No (browser) | Use Chrome         |
| 8 | PWA install not supported on Firefox Android | High | No (browser) | Use Chrome      |
