# Texture Tools

A standalone pixel-art texture toolbox for the owner and other developers: palette swap, layer combine, shape-keeping
paint, a sprite editor and scale/upscale, as a Kivy GUI over headless engines, plus command-line wrappers with the old
flags. Built 2026-11-22 per [plan_texture_tools_gui.md](plan_texture_tools_gui.md) (TODO `texture-tooling #1`; the swap
engine fixes are `textures/food #10`). Run it with `python3 -m texturetools.gui`. Dev tool: no jar, no scratch queue,
no changelog bullet. Review state: the owner checklists G1 to G4 below are pending.

## 1. Rulings (owner, 2026-11-21)

| Id | Ruling |
|---|---|
| R1 | Top-level `texturetools/`, GPL-3.0-only (SPDX in every file), ships to other developers |
| R2 | Packaged for Linux only (PyInstaller onedir, tarball); Windows and macOS not planned |
| R3 | Palette swap has selectable mapping modes; the rank map stays the default and its output stays byte-identical |
| R4 | The CLAUDE.md texture rule binds agents, not the tool: a user-driven editor is allowed. No agent draws, edits or regenerates a texture while building or testing it; tests use synthetic arrays and existing sprites read-only |
| R5 | (owner, 2026-11-22) Swap palette alpha threshold default 255: only fully opaque palette pixels count (was 1) |
| R6 | (owner, 2026-11-22) Swap alpha source default: keep the source alpha (was palette) |
| R7 | (owner, 2026-11-22) Swap interpolation default `smaller` (blend only when the palette is smaller); `--interpolate` still means `always` |
| R8 | (owner, 2026-11-22) Combine add and multiply respect the overlay's alpha by default (a transparent overlay pixel leaves the base unchanged), in the engine, the stack, the CLI and the GUI |
| R9 | (owner, 2026-11-22) File dialogs are the system default file browser only: zenity (GTK desktops) or kdialog (KDE); the Kivy browser is gone, no built-in fallback |
| R10 | (owner, 2026-11-22) Each set of config options folds under a dropdown in the left sidebar |

R5 to R8 replace the byte-identical legacy defaults (R3 keeps the rank map as the default mode). The old output is one
switch away: swap `--legacy` (or `--palette-alpha 1 --alpha palette --interp off`; in code `SwapOptions.legacy()`),
combine `--legacy-blend` (stack JSON layer key `"legacy_blend": true`; GUI layer toggle "Ignore alpha (legacy)").
The function-level API keeps the legacy behavior: `palette_swap()`, `process_one()`, `combine_add`, `combine_multiply`,
`combine()` and `combine_dir()` (default `legacy_blend=True`), so generators that import them (gen_jam_art.py) do not
change; gen_jam_art.py output was checked byte-identical before and after (2026-11-22, scratch folder). The GUI follows
the new engine defaults; a preset saved earlier keeps its stored swap values, but a combine preset or stack JSON saved
before 2026-11-22 has no `legacy_blend` key, so its add and multiply layers now respect alpha. The `textureswapper/`
shims run the CLI, so a plain shim run now uses the new defaults (add `--legacy` / `--legacy-blend` for the old output).

## 2. Architecture

```
texturetools/
  core/      numpy + Pillow only, no Kivy: image_io, colors, palette, swap, combine, paint, edit, scale, batch,
             presets, undo, provenance
  cli/       palette_swap, combine_tex, paint_tex, scale_tex, upscale_sprites: thin wrappers
  gui/       Kivy app (app, registry, settings, worker, provenance_ui), screens/{swap,combine,paint,editor,scale,
             settings,base,bulkui}, widgets/
  tests/     headless tests (test_*.py) plus tests/legacy/ (frozen pre-move scripts)
  packaging/ PyInstaller spec, build script, user README: see packaging/README.md (written by P9)
textureswapper/{palette_swap,combine_tex,scale_tex,upscale_sprites}.py   shims
```

Principles: every function takes and returns arrays; a screen builds a params object and the live preview calls the
same engine function the batch runs; nothing is written without Save or Run; output is raw RGBA PNG (no gAMA or sRGB
chunk, no metadata); previews are nearest-neighbor with a 150 ms debounce on a worker thread; the editor and result
screens share a snapshot undo stack (`core/undo.py`).

- **Shims.** The four scripts in `textureswapper/` re-export `texturetools.cli.*` and keep the same command lines and
  module names, so every `gen_*.py` that imports them still works (gen_jam_art.py output was byte-identical before and
  after P0).
- **Batch.** `core/batch.py` is the one job runner (dry run, overwrite skip/rename/replace, include/exclude globs,
  recursion, name patterns, skip report, never writes outside the output folder, cartesian count). Job kinds (swap,
  combine, paint, upscale) register on import.
- **Dev-mode provenance.** A setting, off in shipped builds. After a Run, a popup offers a row for
  [texture_sources/sources.csv](texture_sources/sources.csv) (source, tool, use) via `core/provenance.py`; the row is
  appended only on confirm, then the popup reminds to run `python3 tools/texture_provenance.py --write`. See
  [texture_sources.md](texture_sources.md).

## 3. Screens

All tool screens have a live preview, Run, preview-all grid (click to enlarge, untick to skip), presets (JSON), Open
in Editor and Copy as CLI. Zoom 1x to 64x with hard pixels. The per-line behavior is in the checklists (section 7).

| Screen | Controls |
|---|---|
| Palette Swap | source and palette from file, folder or a hand-built swatch strip; mapping mode (rank, even, role, nearest, ramp); interpolate off/smaller/always with amount and RGB/HSV/Lab space; invert; drop lightest/darkest N and classes; palette alpha threshold; palette and source (keep) exclusion lists with tolerance and eyedropper on the thumbnails; hue/saturation/value shift; preserve slider; contrast stretch; alpha source; ordered dither; bulk both directions with name pattern, globs, overwrite policy, dry run |
| Combine | layer list (add, duplicate, remove, reorder, eye); blend mode (alpha, add, multiply, screen, overlay, subtract, min, max, paint); opacity; offset with nudge arrows and drag on the preview; edge clip/wrap/grow; anchor; nearest resize, LCM fit, trim; mask and invert; paint strength/inset/clip; canvas base/largest/fixed and background; varying slots with count and in-UI cartesian confirm; stack JSON export and import |
| Painter | inplace and projected modes (stretch, contain, cover); offset, scale, tile, flip, rotate, inset; strength; outline shade/keep; mask and invert; paint-only and keep color lists with tolerance; material alpha; clip; bulk both directions |
| Editor | canvas with grid, checkerboard, hover cursor; pencil, eraser, line, rectangle, ellipse, bucket with tolerance, global fill, picker (alt-click from any tool), rectangle select, wand, mirror draw; floating copy/cut/paste; flip, rotate, resize canvas, scale, shift, trim; undo and redo with a history strip; color panel (RGB/hex/HSV/alpha, nudge, suggest, harmonies, last 16, 32 saved slots, palette import and export, image palette strip); autosave and restore in the config folder |
| Scale | single image (factor or target size, trim, canvas, anchor) and folder upscale (factors, recursive, flat) |
| Settings | dev mode, project folder, recent folders |

## 4. Command line

Every wrapper runs as `python3 -m texturetools.cli.<name>`; `--help` is the reference. The old flags are unchanged;
the defaults of R5 to R8 changed on 2026-11-22.

- `palette_swap`: `--invert --drop-lightest N --drop-darkest N --drop CLASS --interpolate --even --preview`; new:
  `--mode {rank,even,role,nearest,ramp} --interp {off,smaller,always} --interp-amount --interp-space {rgb,hsv,lab}
  --palette-alpha N --palette-exclude --palette-tol --keep-source --keep-tol --hue-shift --sat-shift --val-shift
  --preserve --stretch --alpha {palette,source} --dither {none,ordered}`, plus the bulk flags. Defaults
  `--palette-alpha 255 --alpha source --interp smaller`; `--legacy` sets `--palette-alpha 1 --alpha palette --interp
  off` for those not given (the old output). `--interpolate` turns `off` or an unset `--interp` into `always`.
- `combine_tex`: `--batch --pattern --mode {alpha,add,multiply,paint} --center --lcm --mask --strength --inset --clip`;
  new: more modes, bulk flags, inline `--stack` JSON. `add` and `multiply` respect the overlay's alpha;
  `--legacy-blend` (every form; the old parser stays byte-identical, the flag is taken out before it parses) gives
  the old blend that ignores it.
- `paint_tex`: `--mode {inplace,projected} --fit --offset-x/-y --scale --tile --flip-h/-v --rotate --inset --strength
  --outline --mask --mask-invert --only --only-tol --keep --keep-tol --material-alpha --clip` and bulk
  `--bulk --in-dir --out-dir --reverse --pattern --overwrite --include --exclude --recursive --dry-run`.
- `scale_tex`: `--factor | --size, --trim, --canvas, --anchor {topleft,center}`.
- `upscale_sprites`: `--factors N... --recursive --flat`.

## 5. Tests

Run by `tools/test_texturetools.sh` (every `texturetools/tests/test_*.py`, each must exit 0), wired into
`tools/check_all.py` as check `test_texturetools`. Synthetic arrays only; the `textureswapper/` sprites are read only.

| Id | File | Covers |
|---|---|---|
| T1 | test_parity.py | swap parity: the frozen old script vs the new CLI and shim run with `--legacy` or `--palette-alpha 1 --alpha palette --interp off`; engine `palette_swap()` array-equal |
| T2, T2b | test_parity.py | combine parity (alpha, add, multiply, paint, `--lcm`, `--center`, batch naming) with `--legacy-blend`, shim parity |
| T7 | test_swap_modes.py S6, test_combine_stack.py alpha_blend | the 2026-11-22 defaults (R5 to R8) on synthetic arrays: opaque-only palette, source alpha kept, smaller-palette blend, alpha-aware add and multiply, CLI default vs legacy flags, stored preset values |
| T3 | test_batch.py | bulk never writes outside the output folder; skip report |
| T4 | test_edit.py | undo/redo identity, bucket tolerance, global fill count, rotate x4, nudge in gamut |
| T5 | test_colors.py | suggest differs by exactly the delta on one axis |
| T6 | test_gui_smoke.py | every screen constructs under xvfb |
| T8 | test_gui_dialogs_sections.py | R9 and R10: dialog child env (PyInstaller and snap leaks), backend order, zenity / kdialog argv per mode, fake-dialog runs (choose, cancel, failure, none installed); every sidebar sectioned, first open and the rest folded, state kept per screen, dialog result on the main thread, one dialog at a time, failure on the status line |
| | test_swap_modes, test_combine_stack, test_paint, test_gui_swap_combine, test_gui_editor, test_gui_paint_scale | engine and GUI checks per phase |

Gotchas are in [agent_notes/texturetools-gotchas.md](agent_notes/texturetools-gotchas.md).

## 6. Packaging

Linux onedir tarball and AppImage: [texturetools/packaging/README.md](../texturetools/packaging/README.md). Shipped
builds carry no pack art, generated textures or donor jars. `build.sh` makes the AppImage when `appimagetool` is on
PATH (installed at `~/.local/bin/appimagetool`, 1.9.0, 2026-11-22); first built 2026-11-22
(`texturetools-0.1.0-linux-x86_64.AppImage`, 43 MB, selftest PASS under xvfb). Run it with
`./texturetools-<version>-linux-x86_64.AppImage` (add `APPIMAGE_EXTRACT_AND_RUN=1` without FUSE).

File dialogs (R9) need zenity or kdialog on the user's system; neither is bundled.

## 7. Owner checklists

Checked by the owner at a screen (no game, no scratch queue). Use your own sources and palettes; agents do not make
textures. Mark each line with the date when it passes.

### G1: Palette Swap and Combine screens (P6)

Palette Swap:
- [ ] G1.1 Pick a source and a palette: the preview appears within a moment; Save as writes a file that looks exactly
  like the preview (open both in an image viewer at the same zoom).
- [ ] G1.2 Zoom with the wheel and the + / - buttons up to 64x: pixels stay hard-edged squares, never blurred; Fit
  returns to the largest whole zoom; right or middle drag pans.
- [ ] G1.3 Every control changes the preview where it applies: Mode (rank, even, role, nearest, ramp), Interpolate
  and Amount, Blend space (with interpolation on), Invert, Dither (palette smaller than the source), Alpha from (a
  source with semi-transparent pixels), Min alpha (a palette with semi-transparent pixels), Drop browns / whites,
  Drop darkest / lightest, Hue / Sat / Value shift, Exclude tol, Preserve source, Contrast stretch (nearest, ramp),
  Keep tol. Note any control that changes nothing on a case where it should.
- [ ] G1.4 Click a palette thumbnail pixel: its color is ticked in the exclusion list and the preview changes; click
  again to untick. Click a source thumbnail pixel (an outline): that color stays untouched in the preview.
- [ ] G1.5 Palette from "swatches": type colors into the strip, Add, click a swatch to remove it; "Strip from palette
  colors" copies the palette's colors (minus the excluded ones).
- [ ] G1.6 Bulk (both directions: plain and Reverse roles) with a name pattern, Dry run on: Run reports the count and
  writes nothing. Preview all shows every result; clicking a tile shows it full size, Grid returns; untick one tile,
  Dry run off, Run: that file is not written, the others are; Existing file skip / rename / replace behave as named.
- [ ] G1.7 Copy as CLI, paste in a terminal from the repo root: the command writes the same file as Run.
- [ ] G1.8 Save a preset, change things, load it: every control, the swatch strip and the lists come back.

Combine:
- [ ] G1.9 Three or more layers (Add layer, Duplicate): up / dn reorder, the eye hides a layer, x removes one; each
  change shows in the preview at once; Save as matches the preview.
- [ ] G1.10 Per-layer controls each change the preview: blend mode (all ten), Opacity, Offset X / Y, the Nudge arrows,
  a left drag on the preview (moves the selected layer by whole pixels; with Snap a scaled layer moves by its scale),
  Offset edge (clip, wrap, grow), Anchor, Resize (scale, size), Trim first, Mask and Invert mask, Paint strength /
  inset / clip (paint mode), Ignore alpha (legacy) (add and multiply only: off, a transparent overlay pixel leaves the
  base unchanged; on, the old blend darkens or brightens it).
- [ ] G1.11 Canvas base / largest / fixed WxH, Background color, LCM fit, Size check (any, contain, same): the preview
  follows; a size problem shows as a status line, not a crash.
- [ ] G1.12 Bulk: tick Varies on a layer and give it a folder: the count line shows the number of outputs; Preview all
  shows them; Dry run writes nothing. Two varying layers: the count says cartesian, Run refuses until "Confirm N" is
  pressed; changing a folder or glob asks again.
- [ ] G1.13 Export stack JSON, then `python3 -m texturetools.cli.combine_tex --stack <file> <out>` writes the same
  image; Import stack JSON restores the layers. Copy as CLI pasted in a terminal writes the same output as Run.
- [ ] G1.14 Saved presets reload the whole layer stack.

Dev mode (Settings, pack repo only):
- [ ] G1.15 With dev mode on, Run or Save as opens the provenance popup with the output pattern, sources and command
  filled in; "Not now" writes nothing; "Append row" adds one row (origin owned-tool, status confirm) to
  `feature_writeup/texture_sources/sources.csv` and shows the reminder to run
  `python3 tools/texture_provenance.py --write`. With dev mode off nothing is offered.

### G2: Texture Editor screen (P7)

Canvas and tools:
- [ ] G2.1 Open one of your images: it fits the canvas at a whole zoom; the wheel zooms about the cursor up to 64x with
  hard-edged pixels; right or middle drag pans; Grid shows from 4x; Checkerboard toggles; the hovered pixel has a
  one-pixel outline and its coordinates and color show beside the zoom buttons.
- [ ] G2.2 Pencil and Eraser (B, E): a fast drag leaves no gaps; a drag off the edge clips. Line, Rect, Ellipse (L, R,
  O): the shape previews while dragging and lands on release; Filled shapes on and off.
- [ ] G2.3 Mirror X, Y, both: the guide line shows and the pencil, eraser and shapes draw mirrored.
- [ ] G2.4 Bucket (G) with Tolerance 0 and higher, Diagonal fill on and off; Global fill (F) changes every pixel of the
  clicked color (within tolerance), connected or not.
- [ ] G2.5 Picker (I) and Alt + click with any other tool set the current color and draw nothing.
- [ ] G2.6 Select (M) drag and Wand (W, Wand contiguous on and off): the selection shows tinted; Shift adds; Combine
  subtract and intersect work; a click without a drag deselects; drawing and fills stay inside the selection.
- [ ] G2.7 Ctrl+C, Ctrl+V: the piece floats with a yellow outline; drag inside it moves it, arrow keys nudge it, a click
  outside it or Enter commits, Esc drops it; Ctrl+X cuts, Delete clears, Ctrl+A / Ctrl+D select all / none; Paste over
  on draws a semi-transparent piece over the image instead of replacing.
- [ ] G2.8 Image buttons: Flip H / V, Rot 90 / 180 / 270, Resize canvas (every anchor), Scale Up / Down (Down refuses a
  size that does not divide), Shift (Shift wraps on and off), Trim.

Undo and files:
- [ ] G2.9 Ctrl+Z / Ctrl+Y / Ctrl+Shift+Z and the Undo / Redo buttons step through every edit; the history strip shows
  each step, the current one highlighted, and a click jumps to any step (Open, New and Open in Editor are steps too).
- [ ] G2.10 New WxH makes a transparent canvas; Save on an unsaved canvas asks for a path; Save as writes a raw PNG that
  looks exactly like the canvas; Ctrl+S saves to the document path. The title shows `*` while unsaved.
- [ ] G2.11 Make an edit, close the app without saving, start it again: the editor comes back with the unsaved image
  ("restored unsaved work"), still marked unsaved; nothing was written next to the image (the autosave lives in the
  config dir, `~/.config/texturetools/editor_autosave/`). Saving removes the autosave.
- [ ] G2.12 From Palette Swap or Combine, Open in Editor: the result opens as an unsaved document; Ctrl+Z returns to
  the previous document.

Color panel:
- [ ] G2.13 RGB and alpha sliders, the hex field (Enter; #rgb, #rrggbb, #rrggbbaa), the saturation / value box and the
  hue slider all move together and set the swatch.
- [ ] G2.14 Nudge buttons (Lighter, Darker, Sat + / -, Hue < / >) at the default 5% step and at a larger Step %.
- [ ] G2.15 Recent row fills with the last 16 colors you drew with, newest first.
- [ ] G2.16 Saved slots: click an empty slot to store, click a filled one to use it, Shift-click to overwrite; Name,
  Store, Clear; slots survive an app restart. Export palette (.gpl, .hex, .txt, .json) and Import palette round trip;
  "Add image colors" fills the empty slots with the image's colors; "Load palette from image" replaces the slots with
  another image's colors.
- [ ] G2.17 Suggest rows R, G, B, W (toward white), K (toward black) step evenly from the current color by the Delta
  knob (default 24); grayed entries are clamped repeats; click sets, Shift-click saves to the first empty slot. The Hm
  row holds complementary, two triadic and two analogous colors at the same lightness.
- [ ] G2.18 Image palette strip under the canvas lists every color of the image; a click selects it; with Bucket or
  Global fill as the tool a click replaces that color everywhere (inside the selection, if any) with the current color.

### G3: Texture Painter and Scale screens, Open in Editor, Copy as CLI (P8)

Painter:
- [ ] G3.1 Pick a shape and a material: the preview paints the material into the shape and keeps its shading; the shape
  and material thumbnails show over the preview. Save as writes a file that looks exactly like the preview.
- [ ] G3.2 Every control changes the preview where it applies: Mode (inplace, projected), Projected fit (stretch,
  contain, cover; only enabled in projected), Offset X / Y, Scale and Tile (only in place), Flip H / V, Rotate (0, 90,
  180, 270), Inset, Strength (0 = the material's own colors, 1 = full shading), Outline (keep leaves the rim as it was),
  Material alpha (skip leaves the shape where the material is transparent), Clip unpainted.
- [ ] G3.3 Load a mask: only its opaque pixels are painted; Invert mask paints the rest of the shape.
- [ ] G3.4 Click the shape thumbnail with "Eyedropper adds to" = keep: the color is ticked in the Keep list and that
  color stays unpainted; click again to untick. With "only": only the ticked colors are painted; Keep tol and Only tol
  widen the match.
- [ ] G3.5 Bulk (one shape, a materials folder) and Reverse roles (one material, a shapes folder) with a name pattern
  ({shape}, {material}): Dry run reports the count and writes nothing; Preview all shows every result, untick one and
  Run leaves it out.
- [ ] G3.6 Copy as CLI, paste in a terminal from the repo root (`python3 -m texturetools.cli.paint_tex ...`): it writes
  the same file as Run, single and bulk.
- [ ] G3.7 Save a preset, change things, load it: every control and both color lists come back.

Scale:
- [ ] G3.8 Single image: Scale by factor, by target size (S or WxH; an uneven or non-multiple size says why and writes
  nothing) and fit canvas; Trim first; Canvas with Anchor topleft and center. The preview is pixel-crisp and Save as / Run
  write what the preview shows; a blank Output writes `<stem>_<size>.png` next to the input.
- [ ] G3.9 Folder mode: Factors (e.g. `2 4 8 16`), Recursive, Flat: the status line counts images and files; Preview all
  shows every output (nothing written); Run writes `x<N>/` subfolders (or `_x<N>` names when Flat) in the output folder
  (blank = `<folder>_upscaled`).
- [ ] G3.10 Copy as CLI gives `scale_tex` (single) or `upscale_sprites` (folder); pasted, it writes the same files.
- [ ] G3.11 In the Texture Editor, "Scale screen (size, canvas, trim)" opens the image on the Scale screen; Open in
  Editor brings the scaled result back as an unsaved document. The editor's own Scale Up / Down and Trim give the same
  pixels as the Scale screen at that factor.

Every screen:
- [ ] G3.12 Open in Editor from Palette Swap, Combine, Painter and Scale opens the current result in the editor as an
  unsaved document (from a bulk grid: the tile last clicked).
- [ ] G3.13 With dev mode on, a Painter or Scale Run offers the provenance row like the other screens.

### G4: System file dialog and folding sidebar (P11)

- [ ] G4.1 Browse on any slot (Source, Palette, Output, a folder slot), Open / Save as in the editor, Import / Export
  stack JSON and palettes: your desktop's own file dialog opens (not a Kivy one), in the last folder used; the app
  keeps drawing while it is open; Cancel changes nothing.
- [ ] G4.2 Save as onto an existing file: the dialog asks to overwrite; a second Browse while a dialog is open is
  refused on the status line.
- [ ] G4.3 Every screen's left sidebar: each heading is a dropdown ("-" open, "+" folded); the first starts open, the
  rest folded (Combine: Selected layer open too); a click folds or opens it; Expand all / Collapse all work.
- [ ] G4.4 Fold and open some sections, restart the app: each screen comes back as you left it.
- [ ] G4.5 Combine: selecting another layer renames the Selected layer header and keeps its fold state. A file dropped
  where a folded slot would be is handled like a drop outside any slot (the screen's first slot that takes it).

## 8. Open items for the owner

Defaults the plan did not specify or that were kept for byte-identical legacy output:

- (a) Closed 2026-11-22 (R5 to R7): the defaults flipped to palette alpha 255, keep source alpha, interpolate
  `smaller`; `--interpolate` still means `always`. Legacy via `--legacy`; gen_*.py unchanged (function-level API).
- (b) The `role` mapping mode (hue family, then lightness rank) is a first design: review its output on the jam and pepper
  cases before it is kept.
- (c) Closed 2026-11-22 (R8): Combine multiply and add respect the overlay's alpha by default; `--legacy-blend` and
  the layer toggle "Ignore alpha (legacy)" keep the old blend. G1.10 now shows the new default.
- (d) `core/swap.py` and `core/palette.py` each carry their own Lab and HSV helpers; merge them into `core/colors.py`.
- (e) `scale_tex.main` can call `core/scale.scale_pipeline` instead of its own path.
- Not planned: Windows and macOS, animated textures, normal and specular maps, undo of bulk runs.
