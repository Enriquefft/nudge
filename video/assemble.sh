#!/usr/bin/env bash
#
# assemble.sh — Assembles the Nudge hackathon pitch video from raw components.
#
# Usage:
#   ./assemble.sh              Build the final video
#   ./assemble.sh --dry-run    Print what would be done without executing
#   ./assemble.sh --segment N  Build only segment N (1-7), useful for iteration
#   ./assemble.sh --from STEP  Resume from a step: normalize|segments|join|finalize
#
# Prerequisites:
#   - ffmpeg and ffprobe on PATH
#   - Raw files in video/raw/
#   - Card images in video/cards/
#
# Output:
#   - video/output/nudge-final.mp4
#
# The script is structured as four phases, each a function:
#   1. normalize()        — Conform all inputs to 1920x1080 30fps yuv420p
#   2. create_segments()  — Build each video segment with effects and audio
#   3. join_segments()    — Concatenate segments with xfade transitions
#   4. finalize()         — Mix background music, normalize loudness, export

set -euo pipefail

# ---------------------------------------------------------------------------
# CONFIGURATION — Edit these to adjust timings and behavior
# ---------------------------------------------------------------------------

# Directories (relative to this script's location)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAW_DIR="${SCRIPT_DIR}/raw"
CARDS_DIR="${SCRIPT_DIR}/cards"
ASSETS_DIR="${SCRIPT_DIR}/assets"
WORK_DIR="${SCRIPT_DIR}/.work"          # Intermediate files (gitignored)
OUTPUT_DIR="${SCRIPT_DIR}/output"

# Target format
WIDTH=1920
HEIGHT=1080
FPS=30
PIX_FMT=yuv420p

# Pad color for portrait videos (brand dark)
PAD_COLOR="0a1f15"

# Portrait talking head handling:
# Instead of letterboxing (tiny person in dark frame), crop portrait footage
# to 16:9 centered on the upper portion (face area). Phone selfie video at
# 1080x1920 has enough resolution to crop to 1920x1080 from the upper-center.
# Set CROP_Y_OFFSET to shift the crop window down (0 = top of frame).
CROP_TALKING_HEAD=true
CROP_Y_OFFSET=100  # pixels from top to start crop (adjust to center on face)

# Demo trim points (adjust after reviewing raw footage)
TRIM_DEMO_START="00:00:05"   # Skip first 5s of app-demo (setup/unlock)
TRIM_DEMO_DURATION="00:00:45" # Keep 45s of demo footage

# Segment durations (seconds)
DUR_HOOK=3                    # seg01: hook card on screen
DUR_STAT_EACH=2.5             # seg02: each stat card
DUR_PRODUCT=45                # seg03: trimmed demo length
DUR_BUSINESS=30               # seg05: business model
DUR_TECH=20                   # seg06: architecture

# Transitions
XFADE_DURATION=0.5            # Fade between segments (seconds)

# Lower third
LOWER_THIRD_FADE_IN=1.0       # Seconds after segment start
LOWER_THIRD_FADE_OUT_BEFORE=2.0  # Seconds before segment end

# Color grade for talking head (eq filter values)
# Tuned for phone selfie with cool overhead lighting:
# Warmer brightness, moderate contrast boost, slight saturation lift
GRADE_BRIGHTNESS=0.05
GRADE_CONTRAST=1.10
GRADE_SATURATION=1.15

# Vignette on talking head (draws focus to face)
# angle: PI/5 is subtle, PI/3 is heavy. PI/4 is balanced.
VIGNETTE_ANGLE="PI/5"

# Background music
BG_MUSIC_DB="-20"             # Background music volume in dB

# Encoding
CRF=18
AUDIO_BITRATE="192k"
PRESET="slow"                 # Encoding speed/quality tradeoff

# Final output
FINAL_OUTPUT="${OUTPUT_DIR}/nudge-final.mp4"

# ---------------------------------------------------------------------------
# INPUT FILE PATHS
# ---------------------------------------------------------------------------

# Raw video/audio (support both .m4a and .mp3 for voiceovers)
TALKING_HEAD_VALIDATION="${RAW_DIR}/talking-head-validation.mp4"
TALKING_HEAD_CLOSE="${RAW_DIR}/talking-head-close.mp4"
APP_DEMO="${RAW_DIR}/app-demo.mp4"
BG_MUSIC="${ASSETS_DIR}/bg-music.mp3"

# Card images
CARD_HOOK="${CARDS_DIR}/01-hook.png"
CARD_STAT_REVENUE="${CARDS_DIR}/02-stat-revenue.png"
CARD_STAT_STAFF="${CARDS_DIR}/03-stat-staff.png"
CARD_GAP="${CARDS_DIR}/04-gap.png"
CARD_TITLE="${CARDS_DIR}/05-title.png"
CARD_APPROVED="${CARDS_DIR}/06-approved.png"
CARD_CLOSE="${CARDS_DIR}/07-close.png"

# Segment output files
SEG01="${WORK_DIR}/seg01-hook.mp4"
SEG02="${WORK_DIR}/seg02-stats.mp4"
SEG03="${WORK_DIR}/seg03-product.mp4"
SEG04="${WORK_DIR}/seg04-validation.mp4"
SEG05="${WORK_DIR}/seg05-business.mp4"
SEG06="${WORK_DIR}/seg06-tech.mp4"
SEG07="${WORK_DIR}/seg07-close.mp4"

# ---------------------------------------------------------------------------
# FLAGS
# ---------------------------------------------------------------------------

DRY_RUN=false
ONLY_SEGMENT=""
FROM_STEP=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --dry-run)  DRY_RUN=true; shift ;;
        --segment)  ONLY_SEGMENT="$2"; shift 2 ;;
        --from)     FROM_STEP="$2"; shift 2 ;;
        -h|--help)
            head -20 "$0" | tail -18
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            exit 1
            ;;
    esac
done

# ---------------------------------------------------------------------------
# HELPERS
# ---------------------------------------------------------------------------

log()  { echo -e "\033[1;32m>>>\033[0m $*"; }
warn() { echo -e "\033[1;33mWARN:\033[0m $*" >&2; }
err()  { echo -e "\033[1;31mERROR:\033[0m $*" >&2; }
die()  { err "$@"; exit 1; }

# Run or print command depending on --dry-run
run() {
    if $DRY_RUN; then
        echo "  [dry-run] $*"
    else
        "$@"
    fi
}

# Find a voiceover file, trying .m4a then .mp3
find_vo() {
    local base="${RAW_DIR}/$1"
    if [[ -f "${base}.m4a" ]]; then
        echo "${base}.m4a"
    elif [[ -f "${base}.mp3" ]]; then
        echo "${base}.mp3"
    else
        echo ""
    fi
}

# Get duration of a media file in seconds (float)
get_duration() {
    ffprobe -v error -show_entries format=duration \
        -of default=noprint_wrappers=1:nokey=1 "$1"
}

# Check if a video is portrait (height > width)
is_portrait() {
    local w h
    w=$(ffprobe -v error -select_streams v:0 \
        -show_entries stream=width -of default=noprint_wrappers=1:nokey=1 "$1")
    h=$(ffprobe -v error -select_streams v:0 \
        -show_entries stream=height -of default=noprint_wrappers=1:nokey=1 "$1")
    [[ "$h" -gt "$w" ]]
}

# ---------------------------------------------------------------------------
# PREFLIGHT — Validate all required inputs exist
# ---------------------------------------------------------------------------

preflight() {
    log "Preflight check..."
    local missing=0

    # Required video files
    for f in "$TALKING_HEAD_VALIDATION" "$TALKING_HEAD_CLOSE" "$APP_DEMO"; do
        if [[ ! -f "$f" ]]; then
            err "Missing: $f"
            ((missing++))
        fi
    done

    # Required card images
    for f in "$CARD_HOOK" "$CARD_STAT_REVENUE" "$CARD_STAT_STAFF" \
             "$CARD_GAP" "$CARD_TITLE" "$CARD_APPROVED" "$CARD_CLOSE"; do
        if [[ ! -f "$f" ]]; then
            err "Missing: $f"
            ((missing++))
        fi
    done

    # Voiceover files (required)
    local vo_names=("vo-hook" "vo-product" "vo-business" "vo-tech")
    for name in "${vo_names[@]}"; do
        local found
        found=$(find_vo "$name")
        if [[ -z "$found" ]]; then
            err "Missing: ${RAW_DIR}/${name}.m4a (or .mp3)"
            ((missing++))
        fi
    done

    # Optional: background music
    if [[ ! -f "$BG_MUSIC" ]]; then
        warn "No background music at ${BG_MUSIC} — will skip music layer"
    fi

    # Check ffmpeg
    if ! command -v ffmpeg &>/dev/null; then
        err "ffmpeg not found on PATH"
        ((missing++))
    fi
    if ! command -v ffprobe &>/dev/null; then
        err "ffprobe not found on PATH"
        ((missing++))
    fi

    if [[ $missing -gt 0 ]]; then
        die "$missing required file(s) missing. See errors above."
    fi

    log "All inputs found."
}

# ---------------------------------------------------------------------------
# PHASE 1: NORMALIZE
# ---------------------------------------------------------------------------
# Conform all video inputs to 1920x1080, 30fps, yuv420p.
# Portrait videos are scaled to fit the height and padded with the brand
# dark color to fill 16:9.

normalize() {
    log "Phase 1: Normalizing inputs to ${WIDTH}x${HEIGHT} @ ${FPS}fps..."

    mkdir -p "$WORK_DIR"

    local videos=(
        "$TALKING_HEAD_VALIDATION:norm-validation.mp4"
        "$TALKING_HEAD_CLOSE:norm-close.mp4"
        "$APP_DEMO:norm-demo.mp4"
    )

    for entry in "${videos[@]}"; do
        local src="${entry%%:*}"
        local dst="${WORK_DIR}/${entry##*:}"

        if [[ -f "$dst" ]] && ! $DRY_RUN; then
            log "  Skipping $dst (already exists, delete to re-normalize)"
            continue
        fi

        local vf_chain

        if is_portrait "$src" 2>/dev/null; then
            local basename_src
            basename_src="$(basename "$src")"
            # Check if this is a talking head video — crop to face instead of letterbox
            if $CROP_TALKING_HEAD && [[ "$basename_src" == talking-head-* ]]; then
                log "  Portrait talking head: $src — cropping to 16:9 (face-centered)"
                # Scale width to 1920, then crop height to 1080 from upper portion
                vf_chain="scale=${WIDTH}:-2,crop=${WIDTH}:${HEIGHT}:0:${CROP_Y_OFFSET},setsar=1"
            else
                log "  Portrait detected: $src — will pad to 16:9"
                # Non-talking-head portrait (app demo): pad with brand dark
                vf_chain="scale=-2:${HEIGHT}:force_original_aspect_ratio=decrease,pad=${WIDTH}:${HEIGHT}:(ow-iw)/2:(oh-ih)/2:color=#${PAD_COLOR},setsar=1"
            fi
        else
            # Landscape: scale to exactly target resolution
            vf_chain="scale=${WIDTH}:${HEIGHT}:force_original_aspect_ratio=decrease,pad=${WIDTH}:${HEIGHT}:(ow-iw)/2:(oh-ih)/2:color=#${PAD_COLOR},setsar=1"
        fi

        log "  Normalizing: $(basename "$src") -> $(basename "$dst")"
        run ffmpeg -y -i "$src" \
            -vf "${vf_chain},fps=${FPS},format=${PIX_FMT}" \
            -c:v libx264 -crf "$CRF" -preset fast \
            -c:a aac -b:a "$AUDIO_BITRATE" -ar 48000 -ac 2 \
            -movflags +faststart \
            "$dst"
    done

    # Normalize voiceover audio files to 48kHz stereo
    local vo_names=("vo-hook" "vo-product" "vo-business" "vo-tech")
    for name in "${vo_names[@]}"; do
        local src dst
        src=$(find_vo "$name")
        dst="${WORK_DIR}/norm-${name}.m4a"

        if [[ -f "$dst" ]] && ! $DRY_RUN; then
            log "  Skipping $dst (already exists)"
            continue
        fi

        log "  Normalizing audio: $(basename "$src") -> $(basename "$dst")"
        run ffmpeg -y -i "$src" \
            -c:a aac -b:a "$AUDIO_BITRATE" -ar 48000 -ac 2 \
            "$dst"
    done

    log "Phase 1 complete."
}

# ---------------------------------------------------------------------------
# PHASE 2: CREATE SEGMENTS
# ---------------------------------------------------------------------------

# --- Segment helpers ---

# Create a video from a static image with slow zoom (Ken Burns) effect.
#   $1 = input image
#   $2 = output video
#   $3 = duration (seconds)
#   $4 = (optional) audio file to mix in
#   $5 = (optional) "fadein" to add fade from black
make_card_segment() {
    local img="$1" out="$2" dur="$3"
    local audio="${4:-}"
    local fade="${5:-}"

    # zoompan: slow zoom from 100% to 105% over duration
    local total_frames=$((dur * FPS))
    local zp="zoompan=z='min(zoom+0.0005,1.05)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=${total_frames}:s=${WIDTH}x${HEIGHT}:fps=${FPS}"

    local vf="${zp},format=${PIX_FMT}"

    # Optional fade in from black (first 0.5s)
    if [[ "$fade" == "fadein" ]]; then
        vf="${vf},fade=t=in:st=0:d=0.5"
    fi

    local audio_args=()
    if [[ -n "$audio" && -f "$audio" ]]; then
        audio_args=(-i "$audio" -map 0:v -map 1:a -shortest)
    else
        # Generate silent audio track so concat works
        audio_args=(-f lavfi -i "anullsrc=channel_layout=stereo:sample_rate=48000" -map 0:v -map 1:a -t "$dur")
    fi

    log "  Card segment: $(basename "$img") -> $(basename "$out") (${dur}s)"
    run ffmpeg -y \
        -loop 1 -framerate "$FPS" -i "$img" \
        "${audio_args[@]}" \
        -vf "$vf" \
        -c:v libx264 -crf "$CRF" -preset fast \
        -c:a aac -b:a "$AUDIO_BITRATE" \
        -t "$dur" \
        -movflags +faststart \
        "$out"
}

# Create a multi-card segment with quick cuts and subtle zoom on each.
#   $1 = output video
#   $2 = per-card duration
#   $3 = audio file (optional)
#   $4... = card image files
make_multicard_segment() {
    local out="$1"; shift
    local per_dur="$1"; shift
    local audio="$1"; shift
    local cards=("$@")

    local total_dur
    total_dur=$(echo "${#cards[@]} * $per_dur" | bc)
    local frames_per_card
    frames_per_card=$(echo "$per_dur * $FPS" | bc | cut -d. -f1)

    log "  Multi-card segment: ${#cards[@]} cards x ${per_dur}s -> $(basename "$out")"

    # Build each card as a tiny clip, then concat
    local concat_list="${WORK_DIR}/multicard-concat.txt"
    : > "$concat_list"

    local i=0
    for card in "${cards[@]}"; do
        local tmp="${WORK_DIR}/multicard-${i}.mp4"
        local zp="zoompan=z='min(zoom+0.0008,1.06)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=${frames_per_card}:s=${WIDTH}x${HEIGHT}:fps=${FPS}"

        run ffmpeg -y \
            -loop 1 -framerate "$FPS" -i "$card" \
            -f lavfi -i "anullsrc=channel_layout=stereo:sample_rate=48000" \
            -vf "${zp},format=${PIX_FMT}" \
            -map 0:v -map 1:a \
            -c:v libx264 -crf "$CRF" -preset fast \
            -c:a aac -b:a "$AUDIO_BITRATE" \
            -t "$per_dur" \
            -movflags +faststart \
            "$tmp"

        echo "file '${tmp}'" >> "$concat_list"
        ((i++))
    done

    # Concatenate card clips
    local merged="${WORK_DIR}/multicard-merged.mp4"
    run ffmpeg -y -f concat -safe 0 -i "$concat_list" \
        -c copy "$merged"

    # Mix in voiceover audio if provided
    if [[ -n "$audio" && -f "$audio" ]]; then
        run ffmpeg -y -i "$merged" -i "$audio" \
            -filter_complex "[0:a][1:a]amix=inputs=2:duration=first:dropout_transition=2[aout]" \
            -map 0:v -map "[aout]" \
            -c:v copy -c:a aac -b:a "$AUDIO_BITRATE" \
            -movflags +faststart \
            "$out"
    else
        run cp "$merged" "$out"
    fi
}

# --- Individual segment builders ---

build_seg01() {
    log "Building seg01-hook: Hook card with fade-in and zoom..."
    local vo
    vo=$(find_vo "vo-hook")
    # The hook card gets the first portion of vo-hook; stats get the rest.
    # For simplicity, this segment is just the card with a portion of silence.
    # The voiceover will span seg01+seg02 combined.
    make_card_segment "$CARD_HOOK" "$SEG01" "$DUR_HOOK" "" "fadein"
}

build_seg02() {
    log "Building seg02-stats: Quick-cut stat cards with voiceover..."
    local vo
    vo=$(find_vo "vo-hook")
    local vo_norm="${WORK_DIR}/norm-vo-hook.m4a"
    [[ -f "$vo_norm" ]] && vo="$vo_norm"

    # Stat cards: revenue, staff, gap — with the hook voiceover audio
    make_multicard_segment "$SEG02" "$DUR_STAT_EACH" "$vo" \
        "$CARD_STAT_REVENUE" "$CARD_STAT_STAFF" "$CARD_GAP"
}

build_seg03() {
    log "Building seg03-product: App demo with voiceover..."
    local norm_demo="${WORK_DIR}/norm-demo.mp4"
    local vo
    vo=$(find_vo "vo-product")
    local vo_norm="${WORK_DIR}/norm-vo-product.m4a"
    [[ -f "$vo_norm" ]] && vo="$vo_norm"

    # Trim normalized demo footage
    local trimmed="${WORK_DIR}/demo-trimmed.mp4"
    log "  Trimming demo: start=${TRIM_DEMO_START} duration=${TRIM_DEMO_DURATION}"
    run ffmpeg -y -ss "$TRIM_DEMO_START" -i "$norm_demo" \
        -t "$TRIM_DEMO_DURATION" \
        -c:v libx264 -crf "$CRF" -preset fast \
        -c:a aac -b:a "$AUDIO_BITRATE" \
        -movflags +faststart \
        "$trimmed"

    # Mix voiceover over demo audio (demo audio at lower volume)
    if [[ -f "$vo" ]]; then
        log "  Mixing voiceover into demo..."
        run ffmpeg -y -i "$trimmed" -i "$vo" \
            -filter_complex \
            "[0:a]volume=0.15[demo_a];[1:a]volume=1.0[vo_a];[demo_a][vo_a]amix=inputs=2:duration=first:dropout_transition=2[aout]" \
            -map 0:v -map "[aout]" \
            -c:v copy -c:a aac -b:a "$AUDIO_BITRATE" \
            -movflags +faststart \
            "$SEG03"
    else
        run cp "$trimmed" "$SEG03"
    fi
}

build_seg04() {
    log "Building seg04-validation: Talking head with lower third and color grade..."
    local norm_val="${WORK_DIR}/norm-validation.mp4"
    local dur
    if $DRY_RUN; then
        dur=40  # Estimate for dry run
    else
        dur=$(get_duration "$norm_val" | cut -d. -f1)
    fi

    # Calculate lower-third timing
    local lt_end
    lt_end=$(echo "$dur - $LOWER_THIRD_FADE_OUT_BEFORE" | bc)

    # Build filter: color grade + lower third overlay with fade
    # The lower third image is overlaid and fades in/out
    local vf=""
    # Color grade: warm shift with eq filter
    vf+="eq=brightness=${GRADE_BRIGHTNESS}:contrast=${GRADE_CONTRAST}:saturation=${GRADE_SATURATION}"
    # Lower third overlay with alpha fade
    vf+=",format=rgba"

    log "  Color grading + lower third overlay (fade in @${LOWER_THIRD_FADE_IN}s, fade out @${lt_end}s)"

    # We need a filter_complex for the overlay
    run ffmpeg -y \
        -i "$norm_val" \
        -loop 1 -framerate "$FPS" -i "${CARDS_DIR}/05-title.png" \
        -filter_complex \
        "[0:v]eq=brightness=${GRADE_BRIGHTNESS}:contrast=${GRADE_CONTRAST}:saturation=${GRADE_SATURATION},vignette=angle=${VIGNETTE_ANGLE}[graded]; \
         [1:v]scale=${WIDTH}:${HEIGHT},format=rgba, \
         fade=t=in:st=${LOWER_THIRD_FADE_IN}:d=0.5:alpha=1, \
         fade=t=out:st=${lt_end}:d=0.5:alpha=1[lt]; \
         [graded][lt]overlay=0:0:shortest=1[vout]" \
        -map "[vout]" -map 0:a \
        -c:v libx264 -crf "$CRF" -preset fast -pix_fmt "$PIX_FMT" \
        -c:a aac -b:a "$AUDIO_BITRATE" \
        -movflags +faststart \
        "$SEG04"
}

build_seg05() {
    log "Building seg05-business: ROI + Approved cards with voiceover..."
    local vo
    vo=$(find_vo "vo-business")
    local vo_norm="${WORK_DIR}/norm-vo-business.m4a"
    [[ -f "$vo_norm" ]] && vo="$vo_norm"

    # Two cards: title (business/ROI content) and approved, splitting the duration
    local half_dur
    half_dur=$(echo "$DUR_BUSINESS / 2" | bc)

    make_multicard_segment "$SEG05" "$half_dur" "$vo" \
        "$CARD_TITLE" "$CARD_APPROVED"
}

build_seg06() {
    log "Building seg06-tech: Architecture card with voiceover..."
    local vo
    vo=$(find_vo "vo-tech")
    local vo_norm="${WORK_DIR}/norm-vo-tech.m4a"
    [[ -f "$vo_norm" ]] && vo="$vo_norm"

    # Use the title card as architecture placeholder (user can swap in a
    # dedicated architecture card later). Duration matches vo-tech.
    make_card_segment "$CARD_TITLE" "$SEG06" "$DUR_TECH" "$vo"
}

build_seg07() {
    log "Building seg07-close: Talking head with lower third, fade to close card..."
    local norm_close="${WORK_DIR}/norm-close.mp4"
    local dur
    if $DRY_RUN; then
        dur=15
    else
        dur=$(get_duration "$norm_close" | cut -d. -f1)
    fi

    local lt_end
    lt_end=$(echo "$dur - $LOWER_THIRD_FADE_OUT_BEFORE" | bc)

    # Part A: talking head with lower third and color grade
    local part_a="${WORK_DIR}/seg07-head.mp4"
    run ffmpeg -y \
        -i "$norm_close" \
        -loop 1 -framerate "$FPS" -i "${CARDS_DIR}/05-title.png" \
        -filter_complex \
        "[0:v]eq=brightness=${GRADE_BRIGHTNESS}:contrast=${GRADE_CONTRAST}:saturation=${GRADE_SATURATION},vignette=angle=${VIGNETTE_ANGLE}[graded]; \
         [1:v]scale=${WIDTH}:${HEIGHT},format=rgba, \
         fade=t=in:st=${LOWER_THIRD_FADE_IN}:d=0.5:alpha=1, \
         fade=t=out:st=${lt_end}:d=0.5:alpha=1[lt]; \
         [graded][lt]overlay=0:0:shortest=1[vout]" \
        -map "[vout]" -map 0:a \
        -c:v libx264 -crf "$CRF" -preset fast -pix_fmt "$PIX_FMT" \
        -c:a aac -b:a "$AUDIO_BITRATE" \
        -movflags +faststart \
        "$part_a"

    # Part B: close card held for 3 seconds
    local part_b="${WORK_DIR}/seg07-card.mp4"
    make_card_segment "$CARD_CLOSE" "$part_b" 3

    # Combine with crossfade from talking head to close card
    if $DRY_RUN; then
        local dur_a=15
    else
        local dur_a
        dur_a=$(get_duration "$part_a")
    fi

    local xfade_offset
    xfade_offset=$(echo "$dur_a - 1.0" | bc)

    log "  Crossfading talking head -> close card at ${xfade_offset}s"
    run ffmpeg -y \
        -i "$part_a" -i "$part_b" \
        -filter_complex \
        "[0:v][1:v]xfade=transition=fade:duration=1.0:offset=${xfade_offset}[vout]; \
         [0:a][1:a]acrossfade=d=1.0[aout]" \
        -map "[vout]" -map "[aout]" \
        -c:v libx264 -crf "$CRF" -preset fast -pix_fmt "$PIX_FMT" \
        -c:a aac -b:a "$AUDIO_BITRATE" \
        -movflags +faststart \
        "$SEG07"
}

create_segments() {
    log "Phase 2: Creating segments..."
    mkdir -p "$WORK_DIR"

    if [[ -n "$ONLY_SEGMENT" ]]; then
        log "  Building only segment ${ONLY_SEGMENT}"
        "build_seg0${ONLY_SEGMENT}"
        return
    fi

    build_seg01
    build_seg02
    build_seg03
    build_seg04
    build_seg05
    build_seg06
    build_seg07

    log "Phase 2 complete. All segments built."
}

# ---------------------------------------------------------------------------
# PHASE 3: JOIN SEGMENTS
# ---------------------------------------------------------------------------
# Concatenate all segments with xfade transitions between them.

join_segments() {
    log "Phase 3: Joining segments with ${XFADE_DURATION}s fade transitions..."

    local segments=("$SEG01" "$SEG02" "$SEG03" "$SEG04" "$SEG05" "$SEG06" "$SEG07")
    local n=${#segments[@]}

    # Verify all segments exist
    if ! $DRY_RUN; then
        for seg in "${segments[@]}"; do
            if [[ ! -f "$seg" ]]; then
                die "Segment missing: $seg — run create_segments first"
            fi
        done
    fi

    # Get durations for calculating xfade offsets
    local durations=()
    if $DRY_RUN; then
        # Estimate durations for dry run display
        durations=(3 7.5 45 40 30 20 18)
    else
        for seg in "${segments[@]}"; do
            durations+=("$(get_duration "$seg")")
        done
    fi

    # Build the xfade filter chain.
    # For N segments, we need N-1 xfade filters chained together.
    # Each xfade offset = cumulative_duration - (transition_index * xfade_duration)
    #
    # The offset for xfade i is:
    #   sum(durations[0..i]) - (i * XFADE_DURATION) - XFADE_DURATION

    local inputs=""
    for i in "${!segments[@]}"; do
        inputs+="-i ${segments[$i]} "
    done

    local vfilter=""
    local afilter=""
    local cumulative=0

    for ((i = 0; i < n - 1; i++)); do
        cumulative=$(echo "$cumulative + ${durations[$i]}" | bc)
        local offset
        offset=$(echo "$cumulative - ($i + 1) * $XFADE_DURATION" | bc)

        local vin_a vin_b avin_a avin_b vout aout

        if [[ $i -eq 0 ]]; then
            vin_a="[0:v]"
            avin_a="[0:a]"
        else
            vin_a="[vfade${i}]"
            avin_a="[afade${i}]"
        fi

        vin_b="[$((i + 1)):v]"
        avin_b="[$((i + 1)):a]"

        if [[ $i -eq $((n - 2)) ]]; then
            vout="[vout]"
            aout="[aout]"
        else
            vout="[vfade$((i + 1))]"
            aout="[afade$((i + 1))]"
        fi

        vfilter+="${vin_a}${vin_b}xfade=transition=fade:duration=${XFADE_DURATION}:offset=${offset}${vout};"
        afilter+="${avin_a}${avin_b}acrossfade=d=${XFADE_DURATION}${aout};"
    done

    # Remove trailing semicolon
    local filter_complex="${vfilter}${afilter}"
    filter_complex="${filter_complex%;}"

    local joined="${WORK_DIR}/joined.mp4"

    log "  Filter chain with $((n - 1)) xfade transitions"
    if $DRY_RUN; then
        echo "  [dry-run] ffmpeg -y ${inputs} -filter_complex '${filter_complex}' ... ${joined}"
    else
        # shellcheck disable=SC2086
        ffmpeg -y $inputs \
            -filter_complex "$filter_complex" \
            -map "[vout]" -map "[aout]" \
            -c:v libx264 -crf "$CRF" -preset "$PRESET" -pix_fmt "$PIX_FMT" \
            -c:a aac -b:a "$AUDIO_BITRATE" \
            -movflags +faststart \
            "$joined"
    fi

    log "Phase 3 complete: $(basename "$joined")"
}

# ---------------------------------------------------------------------------
# PHASE 4: FINALIZE
# ---------------------------------------------------------------------------
# Add background music (if present), normalize audio loudness, export.

finalize() {
    log "Phase 4: Finalizing..."

    local joined="${WORK_DIR}/joined.mp4"
    mkdir -p "$OUTPUT_DIR"

    if ! $DRY_RUN && [[ ! -f "$joined" ]]; then
        die "Joined video not found: $joined — run join_segments first"
    fi

    if [[ -f "$BG_MUSIC" ]]; then
        log "  Mixing background music at ${BG_MUSIC_DB}dB..."

        # Mix bg music under the main audio
        run ffmpeg -y -i "$joined" -i "$BG_MUSIC" \
            -filter_complex \
            "[1:a]volume=${BG_MUSIC_DB}dB,afade=t=in:st=0:d=2,afade=t=out:st=999:d=3[bg]; \
             [0:a][bg]amix=inputs=2:duration=first:dropout_transition=3[mixed]; \
             [mixed]loudnorm=I=-16:TP=-1.5:LRA=11[aout]" \
            -map 0:v -map "[aout]" \
            -c:v copy -c:a aac -b:a "$AUDIO_BITRATE" \
            -movflags +faststart \
            "$FINAL_OUTPUT"
    else
        log "  No background music found, normalizing audio only..."

        run ffmpeg -y -i "$joined" \
            -af "loudnorm=I=-16:TP=-1.5:LRA=11" \
            -c:v copy -c:a aac -b:a "$AUDIO_BITRATE" \
            -movflags +faststart \
            "$FINAL_OUTPUT"
    fi

    if ! $DRY_RUN; then
        local final_dur
        final_dur=$(get_duration "$FINAL_OUTPUT")
        local size
        size=$(du -h "$FINAL_OUTPUT" | cut -f1)
        log "Phase 4 complete."
        log ""
        log "============================================"
        log "  OUTPUT: ${FINAL_OUTPUT}"
        log "  Duration: ${final_dur}s"
        log "  Size: ${size}"
        log "============================================"
    else
        log "Phase 4 complete (dry run)."
        log "  Would output: ${FINAL_OUTPUT}"
    fi
}

# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------

main() {
    log "Nudge Pitch Video Assembler"
    log "Working directory: ${SCRIPT_DIR}"
    echo ""

    # Determine which steps to run
    local run_normalize=true
    local run_segments=true
    local run_join=true
    local run_finalize=true

    case "$FROM_STEP" in
        "")          ;; # Run all
        normalize)   ;;
        segments)    run_normalize=false ;;
        join)        run_normalize=false; run_segments=false ;;
        finalize)    run_normalize=false; run_segments=false; run_join=false ;;
        *)           die "Unknown step: $FROM_STEP (use: normalize|segments|join|finalize)" ;;
    esac

    # Preflight always runs
    preflight

    $run_normalize && normalize
    $run_segments  && create_segments
    $run_join      && join_segments
    $run_finalize  && finalize

    log "Done."
}

main
