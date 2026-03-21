# Nudge Design System

## Brand Identity

**Nudge** is a premium business tool for restaurant merchants. The brand feels confident, clean, and trustworthy — a fintech product, not a toy.

**Brand concept**: A gentle push, a smart suggestion, a helpful prompt.
**Logo mark**: Speech bubble with upward arrow — communicates "suggestion" + "growth/upsell."

---

## Color Palette

### Brand Colors
| Token | Hex | Usage |
|---|---|---|
| `brand_primary` | `#1B6B4A` | Primary surfaces, toolbar, FAB, key UI elements |
| `brand_primary_light` | `#2A8C63` | Hover states, lighter accents |
| `brand_primary_dark` | `#0F4D35` | Status bar, pressed states, gradients |
| `brand_primary_surface` | `#E8F5EE` | Subtle tinted backgrounds, badges |

### Accent (CTA)
| Token | Hex | Usage |
|---|---|---|
| `accent` | `#E8720C` | Primary CTAs only ("Add to Order"), prices, key highlights |
| `accent_light` | `#FF8F2E` | Hover/active states on accent |
| `accent_surface` | `#FFF3E6` | Accent-tinted surface for callouts |

**Rule**: Accent works BECAUSE it's rare. Never use it for more than 10% of any screen.

### Neutrals (Brand-Tinted)
No dead grays. Every neutral has a subtle green undertone toward the brand.

| Token | Hex | Usage |
|---|---|---|
| `surface_primary` | `#FAFBF9` | App background |
| `surface_secondary` | `#F2F4F0` | Secondary sections, alternating rows |
| `surface_card` | `#FEFFFE` | Card backgrounds |
| `surface_elevated` | `#FFFFFF` | Only for truly floating elements |

### Text
Never pure black. All text has green undertone.

| Token | Hex | Usage |
|---|---|---|
| `text_primary` | `#1A2B23` | Headings, primary body text |
| `text_secondary` | `#5A6B63` | Descriptions, secondary info |
| `text_tertiary` | `#8A9B93` | Captions, timestamps, hints |
| `text_on_brand` | `#F5FBF8` | Text on brand_primary backgrounds |
| `text_on_accent` | `#FFFCF8` | Text on accent backgrounds |

### Semantic
| Token | Hex | Usage |
|---|---|---|
| `semantic_success` | `#1B8C5A` | Success states, revenue numbers, confirmations |
| `semantic_success_surface` | `#E6F7EE` | Success background tint |
| `semantic_error` | `#C4382A` | Error states, destructive actions |
| `semantic_error_surface` | `#FDE8E6` | Error background tint |

### Borders & Overlays
| Token | Hex | Usage |
|---|---|---|
| `border_subtle` | `#E2E8E4` | Card borders, dividers, input borders |
| `border_strong` | `#C5CEC8` | Active borders, focused inputs |
| `overlay_scrim` | `#401A2B23` | Behind modals/overlays (25% opacity) |
| `ripple_light` | `#18FFFFFF` | Ripple on dark surfaces |
| `ripple_dark` | `#0D1A2B23` | Ripple on light surfaces |

### Color Rules
1. **No `#000000` or `#FFFFFF`** for backgrounds or surfaces — always tint
2. **60-30-10 rule**: 60% neutral surfaces, 30% text/borders, 10% accent
3. **Same color = same meaning** everywhere (semantic consistency)
4. **No AI palette**: no cyan-on-dark, no purple gradients, no neon accents

---

## Typography

### Scale
5-size system with clear contrast between levels (3:1 minimum between heading and body).

| Token | Size | Weight | Usage |
|---|---|---|---|
| `text_caption` | 12sp | Regular | Labels, badges, timestamps |
| `text_secondary` | 14sp | Regular | Secondary text, descriptions |
| `text_body` | 16sp | Regular | Primary body text (minimum readable) |
| `text_subheading` | 20sp | Medium | Section titles, card titles |
| `text_heading` | 28sp | Medium | Screen headings, key numbers |
| `text_headline` | 36sp | Medium | Hero numbers, primary stats |
| `text_display` | 48sp | Medium | Revenue figures, hero metrics |

### Font Families
- **Headings**: `sans-serif-medium` (system medium weight)
- **Body**: `sans-serif` (system regular)
- **Buttons**: `sans-serif-medium` with letter spacing

### Typography Rules
1. Body text minimum **16sp** — staff read from arm's length
2. Vary **weight AND size** — bold alone isn't enough hierarchy
3. **3:1 ratio minimum** between heading and body sizes
4. On dark backgrounds: reduce font weight slightly, increase spacing
5. **Italic** only for conversational text (suggestion reasons)
6. **ALL CAPS** only for small labels/badges, with `letterSpacing="0.1"`

---

## Spacing

### 4dp Base Grid
All spacing values are multiples of 4dp.

| Token | Value | Usage |
|---|---|---|
| `space_2xs` | 2dp | Hairline gaps, icon padding |
| `space_xs` | 4dp | Tight inline spacing |
| `space_sm` | 8dp | Related element spacing, button gaps |
| `space_md` | 16dp | Standard padding, between components |
| `space_lg` | 24dp | Section padding, card internal padding |
| `space_xl` | 32dp | Between sections |
| `space_2xl` | 48dp | Major section breaks |
| `space_3xl` | 64dp | Screen-level padding |

### Spacing Rules
1. **Never use arbitrary values** (no 13dp, 7dp, 11dp) — stay on the grid
2. **Related items**: tight (4-8dp)
3. **Between groups**: generous (24-32dp)
4. **Between sections**: large (48dp+)
5. Cards provide grouping — don't add spacing AND cards redundantly

---

## Corners

| Token | Value | Usage |
|---|---|---|
| `corner_sm` | 8dp | Small chips, badges |
| `corner_md` | 12dp | Buttons, input fields, stat cards |
| `corner_lg` | 16dp | Main cards, suggestion card |
| `corner_xl` | 20dp | Large containers, bottom sheets |

**Rule**: Corner radius must be consistent per component type across the app.

---

## Elevation

| Token | Value | Usage |
|---|---|---|
| `elevation_subtle` | 2dp | Stat cards, inline cards |
| `elevation_card` | 4dp | Standard cards |
| `elevation_floating` | 8dp | FAB, floating elements |
| `elevation_overlay` | 12dp | Suggestion card overlay, dialogs |

### Elevation Rules
1. Shadows should be **SUBTLE** — if you clearly see it, it's too strong
2. Semantic z-ordering: base < card < FAB < overlay < dialog
3. Never stack multiple elevated elements (no cards in cards)

---

## Components

### Suggestion Card (Star Component)
The most important UI element in the app.

```
+--------------------------------------+
| SUGGESTED ADD-ON          (label)    |  ← caption, brand_primary, all caps
|                                      |
| French Fries              $4.99      |  ← heading + accent price
|                                      |
| "Most people add fries               |  ← body, italic, text_secondary
|  with a burger"                      |
|                                      |
| [  Not Now  ] [   Add to Order    ]  |  ← 1:2 ratio, dismiss:primary
+--------------------------------------+
```

- **Background**: `surface_card`, `corner_lg`, `elevation_overlay`
- **Padding**: `space_lg` (24dp)
- **Auto-dismiss**: 15 seconds
- **Enter**: 400ms slide from bottom (30%) + fade, DecelerateInterpolator
- **Exit**: 250ms slide down (20%) + fade, AccelerateInterpolator
- **Add confirmation**: Button flashes `semantic_success` "Added!" for 600ms before dismiss

### Primary Button ("Add to Order")
- **Background**: `accent` with ripple
- **Text**: `text_on_accent`, `text_body`, medium weight
- **Height**: 52dp
- **Corner**: `corner_md`
- **Min width**: 120dp
- **States**: default / pressed (darker ripple) / disabled (50% alpha)

### Dismiss Button ("Not Now")
- **Background**: transparent, `border_subtle` 1dp stroke
- **Text**: `text_secondary`, `text_body`
- **Height**: 52dp
- **Corner**: `corner_md`
- **States**: default / pressed (dark ripple) / disabled (50% alpha)

### FAB (Add Item)
- **Size**: 56x56dp
- **Background**: `brand_primary`, oval, ripple
- **Icon/text**: white "+"
- **Elevation**: `elevation_floating` (8dp)
- **Touch target**: 56dp (exceeds 48dp minimum)

### Toolbar
- **Height**: 56dp
- **Background**: `brand_primary`
- **Text**: `text_on_brand`, 20sp, medium weight
- **Elevation**: 4dp
- **Icon buttons**: 48x48dp touch target

### Stat Card
- **Background**: `surface_card`, `border_subtle` 1dp stroke, `corner_md`
- **Elevation**: `elevation_subtle`
- **Padding**: `space_lg`
- **Value**: `text_headline` or `text_heading`, `brand_primary`
- **Label**: `text_secondary` size, `text_secondary` color

---

## Animation

### Timing
| Duration | Usage |
|---|---|
| 100-150ms | Instant feedback (button press, color change) |
| 200-300ms | State changes (screen transitions, toggles) |
| 300-500ms | Layout changes (card expand) |
| 400ms | Suggestion card entrance |
| 250ms | Suggestion card exit, activity transitions |
| 600ms | Success confirmation hold |
| 2000ms | Idle breathing pulse (loop) |

### Rules
1. **Exit < Enter**: Exits are ~60% of entrance duration
2. **DecelerateInterpolator** for entrances (objects slow as they arrive)
3. **AccelerateInterpolator** for exits (objects speed as they leave)
4. **NEVER** use BounceInterpolator or OvershootInterpolator
5. Animate **transform + alpha** only — avoid animating layout properties
6. Stagger: 100ms per item, cap total stagger at 400ms

### Key Animations
| Animation | Spec |
|---|---|
| Card slide in | translateY 30%→0%, alpha 0→1, 400ms, decelerate |
| Card slide out | translateY 0%→20%, alpha 1→0, 250ms, accelerate |
| Screen transition forward | incoming: translateX 100%→0%, 250ms / outgoing: translateX 0%→-30% |
| Screen transition back | incoming: translateX -30%→0%, 250ms / outgoing: translateX 0%→100% |
| Stats stagger | translateY 20dp→0, alpha 0→1, 250ms each, 100ms delay |
| Idle pulse | alpha 0.3↔0.7, 2000ms, infinite reverse |
| Add confirmation | Button color → semantic_success, text → "Added!", hold 600ms |

---

## Interaction Patterns

### Touch Targets
- **Minimum**: 48x48dp for ALL interactive elements
- Buttons can appear smaller visually but must have padded tap area

### Feedback
1. **Tap**: Ripple effect on all touchable surfaces
2. **Action confirmation**: Visual state change (color, text) before dismiss
3. **Loading**: Subtle spinner (32dp, brand_primary tint) during AI processing
4. **Item added**: Snackbar with brand_primary background (not system Toast)

### States
Every interactive element needs:
- Default
- Pressed (ripple + slight darken)
- Disabled (50% alpha)
- Focused (optional, for accessibility)

---

## Layout Principles

### The Squint Test
Blur your eyes at any screen. You should identify:
1. The most important element
2. The second most important
3. Clear groupings

If everything looks the same weight → hierarchy problem.

### Screen Composition
- **60%** neutral surface space
- **30%** content (text, data, secondary elements)
- **10%** accent/action (CTAs, highlights)

### Card Usage
- Cards create grouping — don't overuse them
- Never nest cards inside cards
- Spacing alone can create visual groups (no card needed)

---

## Asset Specifications

### App Icon
- Adaptive icon: white speech bubble + arrow foreground on `brand_primary` circle
- Market listing: 480x480 PNG
- Launcher: adaptive icon (API 26+) with density fallbacks

### Cover Banner
- 1280x720 PNG
- Left: brand name + tagline + value prop
- Right: card mockup showing the product in action
- Background: brand gradient (primary → dark)
