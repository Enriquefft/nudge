# FFmpeg Video Editing Guide for Hackathon Pitch Videos

> System: ffmpeg 8.0.1, libx264, libfreetype, DejaVu/JetBrains fonts.
> Target: 1920x1080, ~3 minutes, h264+AAC.

This guide covers every technique used in `assemble.sh` and additional
tricks for making phone-shot footage look like a startup launch video.
Every command is tested against ffmpeg 8.0.1 on NixOS.

---

## Table of Contents

1. [Color Grading for Phone Footage](#1-color-grading-for-phone-footage)
2. [Smooth Transitions (xfade)](#2-smooth-transitions-xfade)
3. [Text Card Animations](#3-text-card-animations)
4. [Audio Mixing](#4-audio-mixing)
5. [Letterboxing / Aspect Ratio](#5-letterboxing--aspect-ratio)
6. [Lower Thirds](#6-lower-thirds)
7. [Vignette and Subtle Effects](#7-vignette-and-subtle-effects)
8. [Export Settings](#8-export-settings)
9. [Complete Example Pipeline](#9-complete-example-pipeline)

---

## 1. Color Grading for Phone Footage

Phone cameras produce flat, auto-exposed footage. The goal is to add
contrast, warmth, and a consistent "look" without over-processing.
Layer these filters from coarse to fine.

### 1a. The `eq` filter — Brightness, Contrast, Saturation

The fastest single-filter improvement. This is what `assemble.sh` uses.

```bash
# Subtle cinematic bump: slightly brighter, more contrast, warmer saturation
ffmpeg -i talking_head.mp4 \
  -vf "eq=brightness=0.04:contrast=1.08:saturation=1.12" \
  -c:v libx264 -crf 18 -preset slow -c:a copy \
  output.mp4
```

| Parameter    | Default | Cinematic range   | Notes                                |
|--------------|---------|-------------------|--------------------------------------|
| `brightness` | 0.0     | 0.02 to 0.06      | Negative values darken               |
| `contrast`   | 1.0     | 1.05 to 1.15      | Above 1.2 looks harsh                |
| `saturation` | 1.0     | 1.05 to 1.20      | Above 1.3 looks like a phone filter  |
| `gamma`      | 1.0     | 0.9 to 1.1        | <1 brightens midtones, >1 darkens    |

### 1b. The `curves` filter — S-curve and Presets

Curves give finer control than eq. The built-in presets are useful
starting points.

```bash
# Built-in preset: increase_contrast adds a gentle S-curve
ffmpeg -i talking_head.mp4 \
  -vf "curves=preset=increase_contrast" \
  -c:v libx264 -crf 18 -c:a copy output.mp4
```

Available presets (best ones for pitch videos marked with **):

| Preset               | Effect                                           |
|----------------------|--------------------------------------------------|
| **`increase_contrast`** | Gentle S-curve, the safest "make it look better" |
| **`medium_contrast`**   | Slightly stronger S-curve                        |
| `strong_contrast`    | Heavy — usually too much for talking head         |
| **`linear_contrast`**   | Subtle lift, cleaner than increase_contrast      |
| `vintage`            | Faded/muted look — too stylized for a pitch      |
| `cross_process`      | Color shift — do not use for professional video   |
| `lighter`            | Lifts everything — can rescue underexposed shots  |
| `darker`             | Crushes shadows — use sparingly                   |

Custom S-curve for manual control:

```bash
# Custom S-curve: darken shadows, brighten highlights, warm the reds
ffmpeg -i talking_head.mp4 \
  -vf "curves=m='0/0 0.25/0.20 0.5/0.5 0.75/0.83 1/1':r='0/0 0.5/0.55 1/1'" \
  -c:v libx264 -crf 18 -c:a copy output.mp4
```

The `m` (master) curve sets overall contrast. Points are `input/output`
pairs normalized 0-1. The S shape `0.25/0.20` (darken shadows)
and `0.75/0.83` (brighten highlights) is the classic cinematic curve.

### 1c. The `colorbalance` filter — Warm/Cool Shift

Push shadows cool and highlights warm for that "orange and teal" cinema look.

```bash
# Warm highlights, cool shadows (subtle orange-and-teal)
ffmpeg -i talking_head.mp4 \
  -vf "colorbalance=rs=-0.05:gs=-0.02:bs=0.08:rh=0.06:gh=-0.01:bh=-0.05" \
  -c:v libx264 -crf 18 -c:a copy output.mp4
```

| Parameter | Meaning              | Range  |
|-----------|----------------------|--------|
| `rs`      | Red in shadows       | -1 to 1 |
| `gs`      | Green in shadows     | -1 to 1 |
| `bs`      | Blue in shadows      | -1 to 1 |
| `rm`      | Red in midtones      | -1 to 1 |
| `gm`      | Green in midtones    | -1 to 1 |
| `bm`      | Blue in midtones     | -1 to 1 |
| `rh`      | Red in highlights    | -1 to 1 |
| `gh`      | Green in highlights  | -1 to 1 |
| `bh`      | Blue in highlights   | -1 to 1 |

Keep values in the -0.1 to 0.1 range. Anything larger looks tinted.

### 1d. The `lut3d` filter — Professional LUTs

If you have a `.cube` LUT file (free ones available everywhere), this
gives instant "film look" grading.

```bash
ffmpeg -i talking_head.mp4 \
  -vf "lut3d=file=cinematic.cube" \
  -c:v libx264 -crf 18 -c:a copy output.mp4
```

LUTs are designed for LOG footage, so phone video (already in Rec.709)
can look over-processed. To reduce the effect, mix the LUT with the
original using a split/blend approach. Or just stick to the eq+curves
approach above.

### 1e. Recommended Combined Grade

This is the filter chain that gives the best results for phone talking-head
footage. Order matters — apply corrections coarse-to-fine.

```bash
# Full grade stack: eq base -> S-curve -> warm shift -> gentle sharpen
GRADE="eq=brightness=0.03:contrast=1.06:saturation=1.10:gamma=0.97,\
curves=preset=increase_contrast,\
colorbalance=rs=-0.03:bs=0.05:rh=0.04:bh=-0.03"

ffmpeg -i talking_head.mp4 \
  -vf "$GRADE" \
  -c:v libx264 -crf 18 -preset slow -c:a copy \
  graded.mp4
```

---

## 2. Smooth Transitions (xfade)

The `xfade` filter joins two video streams with a transition effect.
This is ffmpeg's built-in way to do dissolves, wipes, and slides without
external tools.

### 2a. How xfade Works

```
Input A: [======= dur_a seconds =======]
Input B:                    [======= dur_b seconds =======]
                            ^--- offset = dur_a - xfade_duration
```

The `offset` parameter is relative to the START of the first input. It
specifies when the transition begins. The combined output duration is:
`dur_a + dur_b - xfade_duration`.

### 2b. Basic Syntax

```bash
# Join two clips with a 0.5s fade transition
ffmpeg -i clip_a.mp4 -i clip_b.mp4 \
  -filter_complex \
    "[0:v][1:v]xfade=transition=fade:duration=0.5:offset=9.5[vout]; \
     [0:a][1:a]acrossfade=d=0.5[aout]" \
  -map "[vout]" -map "[aout]" \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  output.mp4
```

Where `offset=9.5` means clip_a is 10s long, so the fade starts at 9.5s
(= 10.0 - 0.5).

**Both clips must have identical resolution, fps, and pixel format.**
Normalize first (see Section 5).

### 2c. Transition Tier List

**Premium (use these):**

| Transition    | Look                                         | Best for              |
|---------------|----------------------------------------------|-----------------------|
| `fade`        | Classic dissolve                             | Any transition        |
| `fadeblack`   | Fade through black                           | Scene changes         |
| `fadewhite`   | Fade through white                           | Upbeat/clean moments  |
| `dissolve`    | Film-grain dissolve                          | Cinematic feel        |
| `fadeslow`    | Slower dissolve curve, feels more deliberate | Emotional beats       |
| `smoothleft`  | Smooth directional wipe                      | Moving to next topic  |
| `smoothright` | Smooth directional wipe                      | Moving to next topic  |
| `coverleft`   | B slides over A (like a deck of slides)      | Slide deck feel       |
| `revealleft`  | A slides away revealing B                    | Revealing content     |
| `circleopen`  | Circle iris open                             | Dramatic reveal       |

**Acceptable (use sparingly):**

| Transition  | Look                     | Verdict                       |
|-------------|--------------------------|-------------------------------|
| `wipeleft`  | Hard-edge wipe           | OK for tech demos             |
| `slideleft` | Both frames slide        | Can work, a bit "PowerPoint"  |
| `radial`    | Clock wipe               | Acceptable for time-lapse     |
| `hblur`     | Blur transition          | Interesting but niche         |

**Avoid (look cheap):**

| Transition   | Why it looks bad                          |
|--------------|-------------------------------------------|
| `pixelize`   | Looks like a 2005 YouTube transition      |
| `diagtl/tr`  | Diagonal wipes scream "amateur"           |
| `squeezeh/v` | Distortion effect, never looks intentional|
| `hlwind`     | Venetian blind effect — PowerPoint 2003   |
| `zoomin`     | Zoom-in warp looks like a glitch          |

### 2d. Chaining Multiple Xfades

For N clips, you need N-1 xfade filters. Each subsequent xfade feeds
from the previous one's output.

```bash
# Three clips: A (10s) -> B (15s) -> C (5s), each with 0.5s fade
ffmpeg -i a.mp4 -i b.mp4 -i c.mp4 \
  -filter_complex \
    "[0:v][1:v]xfade=transition=fade:duration=0.5:offset=9.5[v1]; \
     [v1][2:v]xfade=transition=fadeblack:duration=0.5:offset=24.0[vout]; \
     [0:a][1:a]acrossfade=d=0.5[a1]; \
     [a1][2:a]acrossfade=d=0.5[aout]" \
  -map "[vout]" -map "[aout]" \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  output.mp4
```

Offset calculation for the second xfade:
- After first xfade: combined duration = 10 + 15 - 0.5 = 24.5s
- Second xfade offset = 24.5 - 0.5 = 24.0s

General formula for offset of xfade `i` (0-indexed):
```
offset[i] = sum(durations[0..i]) - (i + 1) * xfade_duration
```

### 2e. Mixed Transition Types

Use different transitions for different section types:

```bash
# Card -> talking head: dissolve
# Talking head -> screen recording: fadeblack
# Screen recording -> card: smoothleft
```

This creates visual variety without looking inconsistent. The rule:
use at most 2-3 different transition types in the whole video.

---

## 3. Text Card Animations

### 3a. Ken Burns Effect (zoompan)

The most important technique for making static images look alive.
A slow zoom-in (or zoom-out) on a 1920x1080 PNG creates subtle motion
that feels polished.

```bash
# Slow zoom in from 100% to 105% over 4 seconds
ffmpeg -loop 1 -framerate 30 -i card.png \
  -vf "zoompan=z='min(zoom+0.0005,1.05)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=120:s=1920x1080:fps=30,format=yuv420p" \
  -t 4 \
  -c:v libx264 -crf 18 \
  card_animated.mp4
```

Breakdown:
- `z='min(zoom+0.0005,1.05)'` — Zoom increases by 0.0005 per frame, caps at 1.05x
- `x='iw/2-(iw/zoom/2)'` — Keep zoom centered horizontally
- `y='ih/2-(ih/zoom/2)'` — Keep zoom centered vertically
- `d=120` — Total frames (4s at 30fps)
- `s=1920x1080` — Output size

Zoom speed reference (at 30fps):

| Increment | Zoom over 3s | Zoom over 5s | Feel                     |
|-----------|-------------|-------------|--------------------------|
| 0.0003    | ~2.7%       | ~4.5%       | Very subtle, almost still|
| 0.0005    | ~4.5%       | ~7.5%       | Sweet spot for cards     |
| 0.0008    | ~7.2%       | ~12%        | Noticeable, energetic    |
| 0.001     | ~9%         | ~15%        | Fast, only for short cuts|

### 3b. Zoom Out (pull back)

Reverse the zoom to start zoomed in and pull out. Good for "reveal" moments.

```bash
# Slow zoom out from 108% to 100% over 4 seconds
ffmpeg -loop 1 -framerate 30 -i card.png \
  -vf "zoompan=z='if(eq(on,0),1.08,max(zoom-0.0007,1.0))':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=120:s=1920x1080:fps=30,format=yuv420p" \
  -t 4 \
  -c:v libx264 -crf 18 \
  card_zoomout.mp4
```

### 3c. Pan (drift) with Zoom

Slow drift from left to right while zooming. Requires the source image
to be larger than 1920x1080 (e.g., a 2400x1350 image).

```bash
# Slow drift right while zooming in (source must be > 1920x1080)
ffmpeg -loop 1 -framerate 30 -i large_card.png \
  -vf "zoompan=z='min(zoom+0.0004,1.04)':x='if(eq(on,0),0,min(x+1,iw-iw/zoom))':y='ih/2-(ih/zoom/2)':d=150:s=1920x1080:fps=30,format=yuv420p" \
  -t 5 \
  -c:v libx264 -crf 18 \
  card_pan.mp4
```

### 3d. Fade In / Fade Out on Cards

Layer fade on top of zoompan for polished card entrances.

```bash
# Zoom + fade in from black (0.5s) + fade out to black (0.5s) on a 4s card
ffmpeg -loop 1 -framerate 30 -i card.png \
  -vf "zoompan=z='min(zoom+0.0005,1.05)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=120:s=1920x1080:fps=30,\
format=yuv420p,\
fade=t=in:st=0:d=0.5,\
fade=t=out:st=3.5:d=0.5" \
  -t 4 \
  -c:v libx264 -crf 18 \
  card_faded.mp4
```

### 3e. Adding Silent Audio to Card Videos

Card segments need a silent audio track so they can be concatenated or
xfaded with clips that have audio.

```bash
# Card with silent audio track (required for concat/xfade with audio clips)
ffmpeg -loop 1 -framerate 30 -i card.png \
  -f lavfi -i "anullsrc=channel_layout=stereo:sample_rate=48000" \
  -vf "zoompan=z='min(zoom+0.0005,1.05)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=90:s=1920x1080:fps=30,format=yuv420p" \
  -map 0:v -map 1:a \
  -c:v libx264 -crf 18 \
  -c:a aac -b:a 192k \
  -t 3 \
  card_with_silence.mp4
```

---

## 4. Audio Mixing

### 4a. Loudness Normalization (loudnorm)

The EBU R128 loudnorm filter is the single most important audio
processing step. It brings all audio to a consistent perceived volume.

```bash
# Normalize voiceover to broadcast standard
ffmpeg -i voiceover.m4a \
  -af "loudnorm=I=-16:TP=-1.5:LRA=11" \
  -c:a aac -b:a 192k -ar 48000 \
  voiceover_normalized.m4a
```

| Parameter | Meaning                    | Recommended value |
|-----------|----------------------------|-------------------|
| `I`       | Integrated loudness (LUFS) | -16 (YouTube/web) |
| `TP`      | True peak (dBTP)           | -1.5 (headroom)   |
| `LRA`     | Loudness range (LU)        | 11 (natural)      |

For a two-pass approach (more accurate, especially for short clips):

```bash
# Pass 1: Measure
ffmpeg -i voiceover.m4a \
  -af "loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json" \
  -f null /dev/null 2>&1 | tail -12

# Pass 2: Apply with measured values
ffmpeg -i voiceover.m4a \
  -af "loudnorm=I=-16:TP=-1.5:LRA=11:\
measured_I=-22.5:measured_LRA=8.2:measured_TP=-3.1:measured_thresh=-33.0:\
linear=true" \
  -c:a aac -b:a 192k -ar 48000 \
  voiceover_normalized.m4a
```

### 4b. Background Music at Low Volume

```bash
# Mix voiceover with background music at -20dB (barely audible)
ffmpeg -i video_with_voiceover.mp4 -i bg_music.mp3 \
  -filter_complex \
    "[1:a]volume=-20dB,afade=t=in:st=0:d=2,afade=t=out:st=170:d=3[bg]; \
     [0:a][bg]amix=inputs=2:duration=first:dropout_transition=3[aout]" \
  -map 0:v -map "[aout]" \
  -c:v copy -c:a aac -b:a 192k \
  output.mp4
```

Breakdown:
- `volume=-20dB` — Music at ~10% perceived volume
- `afade=t=in:st=0:d=2` — Music fades in over first 2 seconds
- `afade=t=out:st=170:d=3` — Music fades out starting at 170s over 3s
  (set `st` to `total_duration - 3`)
- `amix=inputs=2:duration=first` — Output length matches the main audio
- `dropout_transition=3` — Prevents volume dip when one input ends

Volume level guidelines:

| Level  | When to use                              |
|--------|------------------------------------------|
| -24 dB | Background texture, barely there         |
| -20 dB | Subtle but present (recommended)         |
| -16 dB | Noticeable, can compete with voiceover   |
| -12 dB | Too loud for talking-head sections        |

### 4c. Audio Crossfade Between Sections

When joining two clips that each have their own audio:

```bash
# Crossfade audio between two clips over 0.5 seconds
ffmpeg -i clip_a.mp4 -i clip_b.mp4 \
  -filter_complex \
    "[0:a][1:a]acrossfade=d=0.5:c1=tri:c2=tri[aout]" \
  -map 0:v -map "[aout]" \
  -c:v copy -c:a aac -b:a 192k \
  output.mp4
```

Curve types for acrossfade (best ones marked):

| Curve   | Behavior                        | Verdict                    |
|---------|---------------------------------|----------------------------|
| **`tri`**   | Linear fade (default)       | Clean, predictable         |
| **`qsin`**  | Quarter sine wave           | Smooth, professional       |
| **`hsin`**  | Half sine wave              | Slightly slower start/end  |
| `log`   | Logarithmic                     | Fast start, slow end       |
| `exp`   | Exponential                     | Slow start, fast end       |

### 4d. Reducing Phone Recording Noise

Phone microphones pick up room noise. A simple highpass filter removes
low-frequency rumble.

```bash
# Remove low-frequency rumble (<80Hz) and reduce sibilance
ffmpeg -i voiceover.m4a \
  -af "highpass=f=80,lowpass=f=12000,loudnorm=I=-16:TP=-1.5:LRA=11" \
  -c:a aac -b:a 192k \
  voiceover_clean.m4a
```

### 4e. Suppressing App Demo Audio

When showing an app demo with voiceover, the demo's own audio
(system sounds, taps) should be very quiet:

```bash
# Demo video audio at 15%, voiceover at full volume
ffmpeg -i demo.mp4 -i voiceover.m4a \
  -filter_complex \
    "[0:a]volume=0.15[demo]; \
     [1:a]volume=1.0[vo]; \
     [demo][vo]amix=inputs=2:duration=first:dropout_transition=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v copy -c:a aac -b:a 192k \
  output.mp4
```

---

## 5. Letterboxing / Aspect Ratio

### 5a. Portrait Video in 16:9 Frame (Dark Background)

The simplest and cleanest approach. Centers the portrait video and fills
the remaining space with a solid color.

```bash
# Portrait (1080x1920 or 1080x2340) centered in 1920x1080 with dark fill
ffmpeg -i portrait_recording.mp4 \
  -vf "scale=-2:1080:force_original_aspect_ratio=decrease,\
pad=1920:1080:(ow-iw)/2:(oh-ih)/2:color=#0a1f15,\
setsar=1" \
  -c:v libx264 -crf 18 -c:a copy \
  letterboxed.mp4
```

Use a brand color for the padding (not pure black) — it looks more
intentional. `#0a1f15` is the Nudge brand dark.

### 5b. Portrait Video with Blurred Background

More visually interesting: the portrait video is centered, and a
blurred, zoomed version fills the background.

```bash
# Portrait with blurred background fill (premium look)
ffmpeg -i portrait_recording.mp4 \
  -filter_complex \
    "[0:v]scale=1920:1080:force_original_aspect_ratio=increase,crop=1920:1080,boxblur=20:5[bg]; \
     [0:v]scale=-2:1080:force_original_aspect_ratio=decrease[fg]; \
     [bg][fg]overlay=(W-w)/2:(H-h)/2[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  output.mp4
```

This creates two versions of the same video:
1. `[bg]`: Scaled up to fill 1920x1080, then heavily blurred
2. `[fg]`: Scaled to fit within 1920x1080
3. Overlays the sharp foreground on the blurred background

### 5c. Portrait with Branded Side Panels

Instead of blur, show branded graphics on the sides using PNG overlays.

```bash
# Portrait video with brand panels on sides
ffmpeg -i portrait_recording.mp4 \
  -i left_panel.png -i right_panel.png \
  -filter_complex \
    "[0:v]scale=-2:1080:force_original_aspect_ratio=decrease[phone]; \
     [1:v]scale=420:1080[lp]; \
     [2:v]scale=420:1080[rp]; \
     color=c=#0a1f15:s=1920x1080:d=999[base]; \
     [base][lp]overlay=0:0[tmp1]; \
     [tmp1][rp]overlay=1500:0[tmp2]; \
     [tmp2][phone]overlay=(W-w)/2:(H-h)/2[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a aac -b:a 192k -shortest \
  output.mp4
```

### 5d. Phone Mockup Frame

Place the screen recording inside a phone frame image (a PNG with
transparent center). This is the most polished option.

```bash
# Step 1: Create phone frame PNG in ImageMagick (or use a downloaded one)
# The frame should be 1920x1080 with a transparent center where the screen goes

# Step 2: Scale recording to fit inside the frame, overlay both
ffmpeg -i portrait_recording.mp4 \
  -loop 1 -i phone_frame.png \
  -filter_complex \
    "[0:v]scale=340:720[screen]; \
     color=c=#0a1f15:s=1920x1080:d=999[base]; \
     [base][screen]overlay=790:180[with_screen]; \
     [with_screen][1:v]overlay=0:0:shortest=1[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a aac -b:a 192k -shortest \
  output.mp4
```

Adjust the `scale` and `overlay` coordinates to match your phone frame
image. The coordinates above are approximate for a centered phone mockup.

### 5e. Normalizing All Inputs to Same Format

Before using xfade, ALL inputs must match exactly. This is the conforming
step used in `assemble.sh`:

```bash
# Normalize any video to 1920x1080 30fps yuv420p
ffmpeg -i input.mp4 \
  -vf "scale=1920:1080:force_original_aspect_ratio=decrease,\
pad=1920:1080:(ow-iw)/2:(oh-ih)/2:color=#0a1f15,\
setsar=1,\
fps=30,\
format=yuv420p" \
  -c:v libx264 -crf 18 -preset fast \
  -c:a aac -b:a 192k -ar 48000 -ac 2 \
  -movflags +faststart \
  normalized.mp4
```

---

## 6. Lower Thirds

### 6a. Using drawtext (Text Only)

The `drawtext` filter renders text directly onto video. Good for simple
name/title bars without needing a separate image.

```bash
# Name + title lower third with semi-transparent background box
ffmpeg -i talking_head.mp4 \
  -vf "\
drawtext=text='Emil Hernandez':fontsize=36:fontcolor=white:\
font='DejaVu Sans':x=80:y=h-140:\
box=1:boxcolor=black@0.6:boxborderw=15:\
enable='between(t,2,8)',\
drawtext=text='CEO \& Co-founder, Nudge':fontsize=24:fontcolor=white@0.9:\
font='DejaVu Sans':x=80:y=h-95:\
box=1:boxcolor=black@0.6:boxborderw=10:\
enable='between(t,2,8)'" \
  -c:v libx264 -crf 18 -c:a copy \
  output.mp4
```

Key parameters:
- `x=80:y=h-140` — Position from bottom-left
- `box=1:boxcolor=black@0.6` — Semi-transparent background
- `boxborderw=15` — Padding around text
- `enable='between(t,2,8)'` — Show only between 2s and 8s
- `font='DejaVu Sans'` — Available system font (run `fc-list` to see options)

### 6b. Animated Lower Third (Fade In/Out with drawtext)

```bash
# Lower third that fades in at 1s and fades out at 7s
ffmpeg -i talking_head.mp4 \
  -vf "\
drawtext=text='Emil Hernandez':fontsize=36:fontcolor=white:\
font='DejaVu Sans':x=80:y=h-140:\
box=1:boxcolor=black@0.6:boxborderw=15:\
alpha='if(lt(t,1),0,if(lt(t,1.5),(t-1)/0.5,if(lt(t,7),1,if(lt(t,7.5),(7.5-t)/0.5,0))))'" \
  -c:v libx264 -crf 18 -c:a copy \
  output.mp4
```

The alpha expression creates:
- 0-1s: invisible
- 1-1.5s: fade in
- 1.5-7s: fully visible
- 7-7.5s: fade out
- 7.5s+: invisible

### 6c. PNG Overlay Lower Third (Best Quality)

For a designed lower third (made in Figma, ImageMagick, etc.), overlay
a PNG with transparency. This is what `assemble.sh` uses.

```bash
# Overlay a designed lower-third PNG with fade in/out
ffmpeg -i talking_head.mp4 \
  -loop 1 -framerate 30 -i lower_third.png \
  -filter_complex \
    "[1:v]scale=1920:1080,format=rgba,\
     fade=t=in:st=1.0:d=0.5:alpha=1,\
     fade=t=out:st=8.0:d=0.5:alpha=1[lt]; \
     [0:v][lt]overlay=0:0:shortest=1[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  output.mp4
```

Key details:
- The PNG must be 1920x1080 with transparency (the lower third graphic
  at the bottom, rest transparent)
- `format=rgba` is required for alpha-channel fading
- `fade ... :alpha=1` fades only the alpha channel (not to/from black)
- `overlay=0:0` positions at top-left (the PNG itself defines placement)

### 6d. Creating a Lower Third PNG with ImageMagick

```bash
# Create a lower-third bar PNG (1920x1080, transparent except bottom bar)
magick -size 1920x1080 xc:none \
  -fill 'rgba(10,31,21,0.85)' -draw 'roundrectangle 60,920 700,1040 8,8' \
  -font DejaVu-Sans -pointsize 32 -fill white \
  -annotate +80+965 'Emil Hernandez' \
  -font DejaVu-Sans -pointsize 22 -fill 'rgba(255,255,255,0.8)' \
  -annotate +80+1005 'CEO & Co-founder' \
  lower_third.png
```

---

## 7. Vignette and Subtle Effects

### 7a. Vignette

Darkens the edges of the frame, drawing attention to the center.
Essential for talking-head footage.

```bash
# Subtle vignette
ffmpeg -i talking_head.mp4 \
  -vf "vignette=PI/5" \
  -c:v libx264 -crf 18 -c:a copy \
  output.mp4
```

Vignette angle reference:

| Expression  | Angle (radians) | Effect                          |
|-------------|------------------|---------------------------------|
| `PI/6`      | 0.524            | Very subtle, barely noticeable  |
| **`PI/5`**  | **0.628**        | **Sweet spot for pitch videos** |
| `PI/4`      | 0.785            | Noticeable, cinematic           |
| `PI/3`      | 1.047            | Heavy, dramatic                 |

### 7b. Sharpening (unsharp)

Phone footage benefits from slight sharpening to add perceived detail.

```bash
# Gentle sharpen — luma only, small kernel
ffmpeg -i talking_head.mp4 \
  -vf "unsharp=5:5:0.8:5:5:0.0" \
  -c:v libx264 -crf 18 -c:a copy \
  output.mp4
```

Parameters: `luma_x:luma_y:luma_amount:chroma_x:chroma_y:chroma_amount`

| luma_amount | Effect                              |
|-------------|-------------------------------------|
| 0.3 - 0.5   | Very subtle, just adds clarity     |
| **0.6 - 1.0** | **Good for phone footage**      |
| 1.0 - 1.5   | Noticeable sharpening              |
| > 2.0       | Over-sharpened, creates halos       |

Always leave `chroma_amount` at 0.0 — sharpening chroma creates color
noise artifacts.

### 7c. Combined Grade + Effects Stack

The full talking-head treatment in one filter chain:

```bash
# Full treatment: grade + sharpen + vignette
ffmpeg -i talking_head.mp4 \
  -vf "\
eq=brightness=0.03:contrast=1.06:saturation=1.10:gamma=0.97,\
curves=preset=increase_contrast,\
colorbalance=rs=-0.03:bs=0.05:rh=0.04:bh=-0.03,\
unsharp=5:5:0.8:5:5:0.0,\
vignette=PI/5" \
  -c:v libx264 -crf 18 -preset slow -c:a copy \
  graded.mp4
```

### 7d. Slight Glow (Soft Light Look)

A cheap "bloom" effect using a blurred overlay:

```bash
# Soft glow effect (careful — can look cheesy if overdone)
ffmpeg -i talking_head.mp4 \
  -filter_complex \
    "[0:v]split[main][blur]; \
     [blur]boxblur=10:3,format=rgba,colorchannelmixer=aa=0.15[glow]; \
     [main][glow]overlay=0:0[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -pix_fmt yuv420p \
  -c:a copy \
  output.mp4
```

Keep the alpha (`aa`) at 0.15 or lower. Above 0.25 it looks like
vaseline on the lens.

---

## 8. Export Settings

### 8a. Recommended h264 Settings

```bash
ffmpeg -i input.mp4 \
  -c:v libx264 \
  -crf 18 \
  -preset slow \
  -profile:v high \
  -level 4.1 \
  -pix_fmt yuv420p \
  -movflags +faststart \
  -c:a aac -b:a 192k -ar 48000 \
  output.mp4
```

### 8b. CRF Value Guide

| CRF | Quality      | File size (3 min 1080p) | Use case          |
|-----|-------------|-------------------------|-------------------|
| 14  | Near lossless | ~200-300 MB            | Archive master    |
| 16  | Excellent    | ~120-180 MB             | High-quality copy |
| **18** | **Great** | **~80-120 MB**          | **Pitch video**   |
| 20  | Good         | ~50-80 MB               | Quick draft       |
| 23  | Decent       | ~30-50 MB               | Default ffmpeg    |
| 28  | Noticeable loss | ~15-25 MB            | Low bandwidth     |

CRF 18 is the sweet spot: visually indistinguishable from lossless
for most content, reasonable file size for upload.

### 8c. Preset Guide

| Preset     | Encode time  | File size   | When to use                |
|------------|-------------|-------------|----------------------------|
| ultrafast  | ~1x          | Largest     | Preview/testing only       |
| fast       | ~3x          | 10-15% larger | Intermediate passes      |
| medium     | ~5x          | Baseline    | Default                    |
| **slow**   | **~8x**      | **5-10% smaller** | **Final export**     |
| slower     | ~15x         | ~2% smaller | Diminishing returns        |
| veryslow   | ~30x         | ~1% smaller | Not worth it               |

Use `fast` for intermediate/work files, `slow` for the final export.

### 8d. Faststart

Always include `-movflags +faststart`. This moves the MP4 metadata to
the beginning of the file, allowing web players and upload tools to
start playback before the full file is downloaded.

### 8e. Audio Settings

```
-c:a aac -b:a 192k -ar 48000 -ac 2
```

- `aac`: Universal codec, supported everywhere
- `192k`: High quality for voice + music
- `48000`: Standard sample rate for video (not 44100)
- `-ac 2`: Stereo (mono voiceover gets upmixed)

---

## 9. Complete Example Pipeline

This assembles a full pitch video from four components:

1. `intro_card.png` (3s) — Title card with Ken Burns zoom
2. `talking_head.mp4` — Color graded with lower third overlay
3. `screen_recording.mp4` — Portrait app demo centered in 16:9
4. `outro_card.png` (3s) — Closing card

With 0.5s xfade transitions between each, voiceover audio, and
background music.

### Step 0: Preparation

```bash
mkdir -p .work output
```

### Step 1: Normalize All Video Inputs

```bash
# Normalize talking head (landscape)
ffmpeg -y -i raw/talking_head.mp4 \
  -vf "scale=1920:1080:force_original_aspect_ratio=decrease,\
pad=1920:1080:(ow-iw)/2:(oh-ih)/2:color=#0a1f15,\
setsar=1,fps=30,format=yuv420p" \
  -c:v libx264 -crf 18 -preset fast \
  -c:a aac -b:a 192k -ar 48000 -ac 2 \
  -movflags +faststart \
  .work/norm-head.mp4

# Normalize screen recording (portrait -> 16:9 with blurred background)
ffmpeg -y -i raw/screen_recording.mp4 \
  -filter_complex \
    "[0:v]scale=1920:1080:force_original_aspect_ratio=increase,\
     crop=1920:1080,boxblur=20:5[bg]; \
     [0:v]scale=-2:1080:force_original_aspect_ratio=decrease[fg]; \
     [bg][fg]overlay=(W-w)/2:(H-h)/2,\
     fps=30,format=yuv420p[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -preset fast \
  -c:a aac -b:a 192k -ar 48000 -ac 2 \
  -movflags +faststart \
  .work/norm-demo.mp4
```

### Step 2: Build Each Segment

```bash
# Segment A: Intro card (3s, zoom-in, fade from black, silent audio)
ffmpeg -y \
  -loop 1 -framerate 30 -i cards/intro_card.png \
  -f lavfi -i "anullsrc=channel_layout=stereo:sample_rate=48000" \
  -vf "zoompan=z='min(zoom+0.0005,1.05)':\
x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':\
d=90:s=1920x1080:fps=30,\
format=yuv420p,\
fade=t=in:st=0:d=0.5" \
  -map 0:v -map 1:a \
  -c:v libx264 -crf 18 -preset fast \
  -c:a aac -b:a 192k \
  -t 3 -movflags +faststart \
  .work/seg-intro.mp4

# Segment B: Talking head (graded + lower third + vignette)
HEAD_DUR=$(ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 .work/norm-head.mp4)
LT_OUT=$(echo "$HEAD_DUR - 2.0" | bc)

ffmpeg -y \
  -i .work/norm-head.mp4 \
  -loop 1 -framerate 30 -i cards/lower_third.png \
  -filter_complex \
    "[0:v]eq=brightness=0.03:contrast=1.06:saturation=1.10,\
     curves=preset=increase_contrast,\
     unsharp=5:5:0.8:5:5:0.0,\
     vignette=PI/5[graded]; \
     [1:v]scale=1920:1080,format=rgba,\
     fade=t=in:st=1.0:d=0.5:alpha=1,\
     fade=t=out:st=${LT_OUT}:d=0.5:alpha=1[lt]; \
     [graded][lt]overlay=0:0:shortest=1[vout]" \
  -map "[vout]" -map 0:a \
  -c:v libx264 -crf 18 -preset fast -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  -movflags +faststart \
  .work/seg-head.mp4

# Segment C: App demo with voiceover
ffmpeg -y \
  -i .work/norm-demo.mp4 -i raw/voiceover.m4a \
  -filter_complex \
    "[0:a]volume=0.15[demo_a]; \
     [1:a]volume=1.0,highpass=f=80[vo_a]; \
     [demo_a][vo_a]amix=inputs=2:duration=first:dropout_transition=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v copy -c:a aac -b:a 192k \
  -movflags +faststart \
  .work/seg-demo.mp4

# Segment D: Outro card (3s, zoom-in, fade to black, silent audio)
ffmpeg -y \
  -loop 1 -framerate 30 -i cards/outro_card.png \
  -f lavfi -i "anullsrc=channel_layout=stereo:sample_rate=48000" \
  -vf "zoompan=z='min(zoom+0.0005,1.05)':\
x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':\
d=90:s=1920x1080:fps=30,\
format=yuv420p,\
fade=t=out:st=2.5:d=0.5" \
  -map 0:v -map 1:a \
  -c:v libx264 -crf 18 -preset fast \
  -c:a aac -b:a 192k \
  -t 3 -movflags +faststart \
  .work/seg-outro.mp4
```

### Step 3: Get Durations and Calculate Offsets

```bash
# Get durations
DUR_A=$(ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 .work/seg-intro.mp4)
DUR_B=$(ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 .work/seg-head.mp4)
DUR_C=$(ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 .work/seg-demo.mp4)
DUR_D=$(ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 .work/seg-outro.mp4)

XFADE=0.5

# Calculate xfade offsets
# offset[0] = DUR_A - XFADE
OFF1=$(echo "$DUR_A - $XFADE" | bc)
# offset[1] = DUR_A + DUR_B - 2*XFADE
OFF2=$(echo "$DUR_A + $DUR_B - 2 * $XFADE" | bc)
# offset[2] = DUR_A + DUR_B + DUR_C - 3*XFADE
OFF3=$(echo "$DUR_A + $DUR_B + $DUR_C - 3 * $XFADE" | bc)

echo "Offsets: $OFF1 $OFF2 $OFF3"
```

### Step 4: Join with Transitions

```bash
ffmpeg -y \
  -i .work/seg-intro.mp4 \
  -i .work/seg-head.mp4 \
  -i .work/seg-demo.mp4 \
  -i .work/seg-outro.mp4 \
  -filter_complex \
    "[0:v][1:v]xfade=transition=fadeblack:duration=${XFADE}:offset=${OFF1}[v1]; \
     [v1][2:v]xfade=transition=fade:duration=${XFADE}:offset=${OFF2}[v2]; \
     [v2][3:v]xfade=transition=fadeblack:duration=${XFADE}:offset=${OFF3}[vout]; \
     [0:a][1:a]acrossfade=d=${XFADE}:c1=tri:c2=tri[a1]; \
     [a1][2:a]acrossfade=d=${XFADE}:c1=tri:c2=tri[a2]; \
     [a2][3:a]acrossfade=d=${XFADE}:c1=tri:c2=tri[aout]" \
  -map "[vout]" -map "[aout]" \
  -c:v libx264 -crf 18 -preset slow -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  -movflags +faststart \
  .work/joined.mp4
```

### Step 5: Add Background Music + Final Loudness Normalization

```bash
# Get total duration for music fade-out timing
TOTAL_DUR=$(ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 .work/joined.mp4)
FADE_OUT_START=$(echo "$TOTAL_DUR - 3" | bc)

ffmpeg -y \
  -i .work/joined.mp4 \
  -i assets/bg-music.mp3 \
  -filter_complex \
    "[1:a]volume=-20dB,\
     afade=t=in:st=0:d=2,\
     afade=t=out:st=${FADE_OUT_START}:d=3[bg]; \
     [0:a][bg]amix=inputs=2:duration=first:dropout_transition=3[mixed]; \
     [mixed]loudnorm=I=-16:TP=-1.5:LRA=11[aout]" \
  -map 0:v -map "[aout]" \
  -c:v copy -c:a aac -b:a 192k \
  -movflags +faststart \
  output/nudge-final.mp4
```

### Step 6: Verify

```bash
# Check final output
ffprobe -v error -show_entries format=duration,size \
  -show_entries stream=codec_name,width,height,r_frame_rate,bit_rate \
  -of default output/nudge-final.mp4
```

---

## Quick Reference: Filter Chain Copy-Paste

### Color grade (talking head)
```
eq=brightness=0.03:contrast=1.06:saturation=1.10,curves=preset=increase_contrast,colorbalance=rs=-0.03:bs=0.05:rh=0.04:bh=-0.03
```

### Full talking-head treatment (grade + sharpen + vignette)
```
eq=brightness=0.03:contrast=1.06:saturation=1.10:gamma=0.97,curves=preset=increase_contrast,colorbalance=rs=-0.03:bs=0.05:rh=0.04:bh=-0.03,unsharp=5:5:0.8:5:5:0.0,vignette=PI/5
```

### Ken Burns zoom-in (3s card)
```
zoompan=z='min(zoom+0.0005,1.05)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=90:s=1920x1080:fps=30
```

### Portrait -> 16:9 with blur background
```
[0:v]scale=1920:1080:force_original_aspect_ratio=increase,crop=1920:1080,boxblur=20:5[bg];[0:v]scale=-2:1080:force_original_aspect_ratio=decrease[fg];[bg][fg]overlay=(W-w)/2:(H-h)/2[vout]
```

### Normalize to 1920x1080 30fps
```
scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2:color=#0a1f15,setsar=1,fps=30,format=yuv420p
```

### Audio: loudnorm for web
```
loudnorm=I=-16:TP=-1.5:LRA=11
```

### Export flags
```
-c:v libx264 -crf 18 -preset slow -profile:v high -pix_fmt yuv420p -movflags +faststart -c:a aac -b:a 192k -ar 48000
```
