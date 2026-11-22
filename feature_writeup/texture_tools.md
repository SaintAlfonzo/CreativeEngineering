# Texture Tools

Stub (2026-11-22, P6 of [plan_texture_tools_gui.md](plan_texture_tools_gui.md)); P10 writes the full writeup. The app
is `texturetools/` (core engines, CLI wrappers, Kivy GUI): `python3 -m texturetools.gui`.

## Owner checklists

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
  inset / clip (paint mode).
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
