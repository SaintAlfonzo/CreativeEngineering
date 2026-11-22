# Plan: Texture Tools GUI (Kivy)

<!-- generated:status_index begin open_work sha1=73e76fa09019 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 1 open TODO row (Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `texture-tooling #1` | Testing | none | Texture Tools GUI (Kivy): swap, combine, painter, editor, packaged for Linux |
<!-- generated:status_index end open_work -->

Source: owner request (chat 2026-11-21). Supersedes the "three faults only" ruling R5 of
[plan_unblocked_batch_1121.md](plan_unblocked_batch_1121.md) for the GUI build (owner 2026-11-21: selectable modes, the
rank map stays the default). TODO rows: `textures/food #10` (swap engine) plus new rows under `textures/tools` (section 9).

## 1. Rulings (owner, 2026-11-21)

| # | Ruling |
|---|---|
| R1 | New top-level `texturetools/`, GPL-3.0-only (SPDX in every file). Ships to other developers |
| R2 | Packaged for Linux only (PyInstaller onedir, tarball; AppImage optional). Windows and macOS later, not planned |
| R3 | Palette swap gets selectable mapping modes; the current rank map stays the default and its output stays byte-identical |
| R4 | The texture rule in CLAUDE.md binds agents, not the tool: the tool is the owner's, so a user-driven editor is allowed. No agent draws, edits or regenerates a texture while building or testing it; tests use synthetic arrays and existing sprites read-only |

## 2. Architecture

Core (no Kivy import, headless-testable) and GUI separated, so the CLI and the existing generators keep working.

```
texturetools/
  core/            pure numpy + Pillow; every function takes arrays, returns arrays
    image_io.py    load_rgba, save_raw (no gAMA/sRGB), folder scan, name patterns
    colors.py      rgb/hsv/hsl/lab conversions, brightness, nudge, harmonies, suggest
    palette.py     extract palette (opaque only), sort, exclude, drop classes, interpolate
    swap.py        swap engine + modes (section 3)
    combine.py     layer stack + blend modes + resize/offset (section 4)
    paint.py       shape-preserving texture paint (section 5)
    edit.py        pixel editor ops: bucket, global bucket, flip, rotate, cut/paste, resize (section 6)
    scale.py       integer scale, trim, canvas (from scale_tex.py)
    batch.py       one job runner: list of (inputs, params) -> outputs, progress callback, dry run
    presets.py     JSON presets for every screen
    provenance.py  appends rows to feature_writeup/texture_sources/sources.csv (dev-mode only)
  cli/             palette_swap.py, combine_tex.py, scale_tex.py, upscale_sprites.py: thin wrappers, same flags
  gui/             Kivy app: app.py, screens/{swap,combine,paint,editor,scale}.py, widgets/
  tests/           headless: golden parity + property tests
  packaging/       texturetools.spec (PyInstaller), build.sh, LICENSE, README
textureswapper/palette_swap.py etc. -> import from texturetools.cli (kept as shims so gen_*.py scripts do not break)
```

Principles: one engine call per action; the GUI only builds a params dict and a live preview from the same call the batch
runs; no file is written without an explicit Save/Run; outputs are raw RGBA PNG (existing `save_raw` behavior). Undo is
a shared snapshot stack in core (editor and any screen that produces a result).

## 3. Palette Swap screen

Layout: left = source (file or folder), middle = palette (file, folder, or hand-built swatch strip), right = live
preview at integer zoom with nearest filtering; bottom = knobs and Run.

Engine fixes (textures/food #10, inside `swap.py`, legacy mode untouched):
- non-monotonic palettes: mode `role` handles them; legacy skips them as today
- interpolation also when the palette is larger than the texture (resample the palette to n source shades)
- even spacing of ranks (mode `even`)
- ignore semi-transparent palette pixels below an alpha threshold (default 255 = opaque only, knob)

| Control | Kind | Maps to |
|---|---|---|
| Mapping mode | dropdown | `rank` (legacy, default), `even` (even gradient), `role` (hue and lightness role), `nearest` (closest color in Lab), `ramp` (fit source luminance onto the palette sorted as a smooth ramp) |
| Interpolate | toggle + amount | blend between neighbors; `always`, `only when palette smaller` (legacy), off |
| Interpolation space | dropdown | RGB, HSV, Lab (Lab gives the smoothest ramps) |
| Invert | toggle | palette order reversed (existing `--invert`) |
| Exclude colors | swatch list with checkboxes, eyedropper on the palette image, drop classes (brown, white), darkest N, lightest N | palette side; the same exclusion on the source side ("keep these source colors untouched", e.g. outlines, eyes) |
| Exclude by tolerance | slider | colors within N of an excluded swatch are excluded too |
| Preserve source | slider 0-100 | blend result back toward the source per pixel (partial recolor) |
| Hue / saturation / value shift | 3 sliders | applied to the palette before mapping |
| Contrast stretch | toggle | normalize source brightness range before ranking |
| Alpha | toggle | keep source alpha (default), take palette alpha |
| Dither | none / ordered | only for downsized palettes; off by default |
| Reverse roles (bulk) | toggle | existing bulk semantics: one shape, many palettes; or many sources, one palette |
| Bulk | folder in, folder out, name pattern `{name} {shape} {palette}`, include/exclude glob, overwrite policy (skip, rename, replace) | `batch.py` |
| Preview all | grid of results before writing, click one to open it full size, untick to skip | |
| Presets | save, load, "copy as CLI" | `presets.py` |

Parity rule: with default knobs the output equals the current `palette_swap.py` output pixel for pixel on the existing
`textureswapper/` fixtures (test T1).

## 4. Combine screen

Replaces the base + overlay pair with a layer stack (3 or more, reorderable).

| Control | Detail |
|---|---|
| Layers | list with add, remove, drag to reorder, visibility eye, per-layer opacity |
| Blend mode per layer | alpha, add, multiply, plus screen, overlay, subtract, min, max, paint (existing) |
| Offset | X and Y numeric + arrow buttons + drag on the preview; wrap, clip or grow canvas; snap to whole source pixels |
| Resize per layer | scale factor or WxH, nearest only (default), anchor (top left, center), `LCM` fit (existing `--lcm`), trim transparent first |
| Canvas | size = base, largest, or fixed; background transparent or a color |
| Mask | per-layer mask image (existing paint `--mask`) and invert mask |
| Paint options | strength, inset, clip (existing) |
| Bulk | one fixed stack with one slot marked "varies": every file in a folder fills that slot; name pattern; skip size mismatches with a report (existing `combine_dir` behavior, generalized to any slot) |
| Cartesian bulk | two varying slots produce every pair, with a count and confirm before running |
| Presets | stack saved as JSON, so a "can + fruit + lid" recipe reruns |

Engine: a fold over the stack calling the existing per-mode functions; 2 layers must equal today's output (T2).

## 5. Texture Painter screen

Wraps the existing `paint` mode: apply one texture onto another shape while keeping the texture image.
- Inputs: shape (base) and material (texture); live preview.
- The material is sampled in place by default; knobs: offset, scale (integer), tile, flip and rotate the material, inset.
- Shading: strength 0-100 (how much of the shape's luminance shading is kept), outline handling (keep base outline pixels as is, or shade them), mask from the base's alpha or a loaded mask, and a "paint only these colors" picker so a shape's eyes or rim can stay.
- A second mode, "projected": the material is laid in one piece across the shape's bounding box (no tiling), with the shape deciding where it shows, so the material's own image survives.
- Bulk: one shape, many materials (or reverse), same as the other screens.
- Output goes to the Editor for touch-up with one button.

## 6. Texture Editor screen

Zoomed-pixel sprite editor. Canvas with grid, integer zoom (1x to 64x), pan, optional checkerboard, pixel-perfect cursor.

Tools: pencil, eraser, line, rectangle, ellipse (filled and outline), bucket (contiguous, with tolerance), global
bucket (every pixel of the clicked color, tolerance), eyedropper (alt-click from any tool), selection (rect, magic wand),
cut, copy, paste (floating, movable, commits on click away), mirror-draw toggle (X, Y, both).

Image operations: flip H and V, rotate 90/180/270, resize canvas (anchor), scale (integer, nearest), shift/wrap, trim.
Undo and redo, unlimited within memory, with a history strip.

Color panel:
- Current color swatch with RGB sliders, hex entry, HSV wheel/box, alpha slider.
- Nudge buttons beside the swatch: lighter (toward white), darker (toward black), more and less saturated, hue left and right; step size knob (default 5%).
- Color memory: last 16 used colors, auto-filled.
- Saved slots: 32 named slots, persisted in the app config and importable and exportable as a palette file; also "add all colors of this image" and "load palette from image".
- Autosuggest: from the current color, six suggestions evenly stepped in R, in G, in B and toward white and black by the same delta (default 24 per channel, one knob), so contrast between options is consistent; shown as a row per axis; click to set, shift-click to save. A second row offers the harmonies (complementary, triadic) at the same lightness.
- Image palette strip: every color in the image, click to select, click a swatch with bucket held to replace it everywhere.

## 7. Scale and Upscale (smaller screens)

Existing `scale_tex.py` and `upscale_sprites.py` become a Scale screen: factor or target size, trim, canvas and anchor, folder
mode with recursion. Linked from the Editor's resize options so there is one implementation.

## 8. Cross-cutting

- Shared file browser with recent folders, drag and drop of files onto any slot, a project folder setting.
- Every screen: preview, Run, "Open in Editor", "Copy as CLI", preset save and load.
- Pixel art defaults everywhere: nearest-neighbor, never smoothing; no color management, no metadata written.
- Dev mode (a setting, off in shipped builds): after Run, offers a provenance row for `feature_writeup/texture_sources/sources.csv` (source, tool, use) and reminds to rerun `python3 tools/texture_provenance.py --write`. Shipped builds have no repo knowledge.
- Undo per screen, autosave of unsaved editor state, no write without Save.
- Kivy performance: previews are textures with `mag_filter = nearest`; the engine runs on a worker thread; previews debounce 150 ms; images are 16x16 to 512x512 in practice.
- Dependencies pinned: Kivy 2.3.0, numpy 1.26, Pillow (the installed 9.0.1 is old, pin the version tested and note it).

## 9. Phases

Each phase ends with headless tests and a commit on `agent_staging` per CLAUDE.md; the GUI parts need the owner at a
screen, so GUI phases hand over a short checklist instead of a scratch queue row (no game involved).

| Phase | Work | Owner | Done when |
|---|---|---|---|
| P0 | Scaffold `texturetools/`, move the four scripts' logic into `core/` untouched, CLI wrappers, shims in `textureswapper/`. Run every `gen_*.py` that imports them before and after | general-purpose | T1, T2 parity: byte-identical outputs; generators unchanged |
| P1 | Swap engine: the three fixes as modes, alpha threshold, exclusion, tolerance, HSV shift, preserve, color-space interpolation. CLI flags for each | general-purpose | Mode tests; legacy golden unchanged; `textures/food #10` row updated |
| P2 | Combine engine: layer stack, new blend modes, offset, resize, canvas, masks, bulk slot and cartesian | general-purpose | 2-layer parity; N-layer property tests |
| P3 | Painter engine: projected mode, outline handling, color-keep list | general-purpose | Tests on synthetic shapes |
| P4 | Editor engine: bucket, global bucket, selection, transforms, undo stack, suggest and nudge math, palette import and export | general-purpose | Property tests (flip twice = identity, nudge round trip, suggest spacing equal) |
| P5 | Kivy shell: app, screen navigation, file browser, preview widget (zoom, pan, nearest), presets, settings, worker threads | general-purpose | Launches headless-smoke (`KIVY_NO_ARGS`, xvfb) and loads every screen |
| P6 | Swap and Combine screens (batch UI, preview grid, exclusion picker, layer list) | general-purpose | Owner checklist G1 |
| P7 | Editor screen: canvas, tools, color panel | general-purpose | Owner checklist G2 |
| P8 | Painter and Scale screens, Open in Editor handoff, Copy as CLI | general-purpose | Owner checklist G3 |
| P9 | Packaging: PyInstaller spec (onedir), bundle Kivy deps and SDL libs, launcher script, tarball, optional AppImage, README, LICENSE, version in About; build on a clean venv and run on a machine without the dev setup (container or fresh user) | general-purpose | Tarball starts, loads every screen, runs one swap and one combine |
| P10 | Docs: `feature_writeup/texture_tools.md` writeup, agent note, `texture_sources.md` pointer, `tools/generators.json` untouched, `mod_sides.md` not applicable | doc-pass | `status_index.py` clean |

Order: P0 first and alone (everything depends on parity). P1 to P4 are independent engines (different files) and can run
in parallel agents after P0. P5 after P0. P6 to P8 after their engines and P5, one at a time (shared shell files).

## 10. Tests

Headless, in `texturetools/tests/` and wired into `tools/check_all.py` (added in P0):
- T1 swap parity: legacy mode vs the old script on every swap fixture, array-equal
- T2 combine parity: alpha, add, multiply, paint, `--lcm`, `--center`, batch naming
- T3 property: bulk never writes outside its output dir; skip report lists every mismatch
- T4 editor math: undo/redo identity, bucket tolerance 0 equals exact match, global bucket count equals pixel count of that color, rotate x4 = identity, nudge toward white then black stays in gamut
- T5 suggest: the six per-axis suggestions differ from the base by exactly the delta (clamped) in only that axis
- T6 GUI smoke: each screen constructs under xvfb without exception
- G1 to G3 owner checklists in `texture_tools.md` (visual: preview matches output, zoom keeps pixels crisp)

## 11. Risks and open items

- Kivy on Linux packaging: SDL2 and GStreamer pulls make the tarball large; strip unused providers (`KIVY_NO_*`, exclude video/audio) in the spec. Expected 80 to 150 MB.
- Kivy has no native file dialog: the shared file browser (P5) is real work; a `plyer` or `tkinter` fallback is not planned (keeps the bundle small).
- Pillow 9.0.1 is old; confirm the pinned version works with Kivy 2.3.0 and PyInstaller before P0 ends.
- Open (default taken, owner may overturn): the `role` mode's definition (hue family then lightness rank) is a first design; the owner reviews its outputs on the jam and pepper cases from the row text before it is kept.
- Not planned: Windows and macOS builds, animated textures (.mcmeta strips), normal and specular maps, an undo of bulk runs (bulk previews first instead).
- Shipped builds ship no pack art or generated textures, and no donor jars (see the upstream jar distribution note).
