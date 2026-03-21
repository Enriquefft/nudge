#!/usr/bin/env python3
"""
Render the Nudge app icon as a 480x480 PNG.
Design:
  - Background: solid circle, color #1B6B4A (deep emerald green)
  - Foreground: white speech bubble with upward chevron cutout (evenOdd)
    Viewport: 108x108, scaled to 480x480
"""

from PIL import Image, ImageDraw
import math

SIZE = 480
VIEWPORT = 108.0
SCALE = SIZE / VIEWPORT

# Background color
BG_COLOR = (0x1B, 0x6B, 0x4A, 255)

# Create base image (transparent)
img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))

# --- Draw green circle background ---
bg_layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
bg_draw = ImageDraw.Draw(bg_layer)
padding = int(4 * SCALE / VIEWPORT * SIZE)  # small margin
padding = 0  # full bleed circle
bg_draw.ellipse([0, 0, SIZE - 1, SIZE - 1], fill=BG_COLOR)
img = Image.alpha_composite(img, bg_layer)

# --- Draw foreground: speech bubble with chevron cutout ---
# We'll use a two-layer approach:
# 1. Draw the white speech bubble polygon
# 2. Draw the chevron in the background color (simulating evenOdd cutout)

def scale_pts(pts):
    """Scale a list of (x, y) tuples from 108-viewport to 480px."""
    return [(x * SCALE, y * SCALE) for x, y in pts]

# Speech bubble outer shape (rounded rectangle + tail)
# Corners: rounded at radius 8 (from C28,31.6 31.6,28 which implies r=8 at 108 vp)
# We approximate the rounded rectangle with a polygon + arc approach using Pillow.

# Bubble bounding box in viewport coords: x=28..78, y=28..70
# Corner radius = 8 (from 36-28=8 and 42-28... wait: M42,28 with C28,31.6 means
# left side top-left corner: (28,36) with radius ~8, right (70,28) corner radius ~8)
# Tail: points at (48,70), (38,80), (38,70)

# We'll draw rounded rect manually using ImageDraw.rounded_rectangle if available,
# plus the tail triangle. Then draw the chevron cutout on top.

bubble_layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
bubble_draw = ImageDraw.Draw(bubble_layer)

# Rounded rectangle: x1=28, y1=28, x2=78, y2=70, radius=8
x1, y1, x2, y2 = 28, 28, 78, 70
r = 8  # corner radius in viewport units

def vp(v):
    """Scale single viewport value to pixels."""
    return v * SCALE

# Draw the rounded rect body
bubble_draw.rounded_rectangle(
    [vp(x1), vp(y1), vp(x2), vp(y2)],
    radius=vp(r),
    fill=(255, 255, 255, 255)
)

# Draw the tail triangle: (48,70), (38,80), (38,70)
tail_pts = scale_pts([(48, 70), (38, 80), (38, 70)])
bubble_draw.polygon(tail_pts, fill=(255, 255, 255, 255))

img = Image.alpha_composite(img, bubble_layer)

# --- Draw chevron cutout (paint it green to simulate evenOdd) ---
# Chevron path in viewport: M53,39 L65,54 L57,54 L57,64 L49,64 L49,54 L41,54 Z
# This is an upward-pointing arrow: tip at (53,39), then wide base at y=54,
# then down-legs to y=64.
chevron_layer = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
chevron_draw = ImageDraw.Draw(chevron_layer)

chevron_pts = scale_pts([
    (53, 39),
    (65, 54),
    (57, 54),
    (57, 64),
    (49, 64),
    (49, 54),
    (41, 54),
])
chevron_draw.polygon(chevron_pts, fill=BG_COLOR)

img = Image.alpha_composite(img, chevron_layer)

# Save
out_path = '/home/hybridz/Projects/aleph-fiserv-merchant/nudge-icon-480.png'
img.save(out_path, 'PNG', optimize=True)

# Verify
from PIL import Image as PILImage
verify = PILImage.open(out_path)
print(f"Saved: {out_path}")
print(f"Size: {verify.size}")
print(f"Mode: {verify.mode}")

import os
file_size = os.path.getsize(out_path)
print(f"File size: {file_size} bytes ({file_size / 1024:.1f} KB)")
assert verify.size == (480, 480), f"Expected 480x480, got {verify.size}"
assert file_size < 2 * 1024 * 1024, f"File too large: {file_size}"
print("All checks passed.")
