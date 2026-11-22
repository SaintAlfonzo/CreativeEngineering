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
