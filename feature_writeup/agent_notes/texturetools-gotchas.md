# texturetools-gotchas

> Mechanics and gotchas of texturetools/ (the Kivy texture tool); status and owner checklists in feature_writeup/texture_tools.md

Written 2026-11-22 (P10 of plan_texture_tools_gui.md). Repo status docs win over this note.

- `texturetools/tests/legacy/` holds the frozen pre-move scripts used as parity references (T1, T2). Never edit them;
  the lints (us_spelling, tab_indent) exclude that folder on purpose.
- The extended argparse parsers only switch on when a new flag is used, so a legacy invocation keeps the old parser
  behavior and identical stderr (parity tests compare it). Add new flags without changing the legacy path.
- Batch job kinds (swap, combine, paint, upscale) register on import of their module; a runner that does not import the
  kind sees "unknown kind".
- Never import `kivy.core.window` outside the xvfb child process of the GUI tests; it opens a display and hangs or
  fails headless. Core tests must not import Kivy at all.
- base.py once let a preview or preview-all grid requested before a Run overwrite the Run's status line; fixed
  (commit cd31ade5). Keep status writes keyed to the active job.
- `core/swap.py` and `core/palette.py` have duplicate Lab and HSV helpers; `core/colors.py` is the intended home.
- Defaults changed 2026-11-22 (rulings R5 to R8 in texture_tools.md): `SwapOptions()` is palette alpha 255, alpha
  source, interpolate smaller; `SwapOptions.legacy()` / CLI `--legacy` is the old trio. Stack add and multiply respect
  the overlay alpha unless `Layer.legacy_blend` (CLI `--legacy-blend`). The function-level API stays legacy on purpose
  (`palette_swap()`, `process_one()`, `combine_add`, `combine_multiply`, `combine()`/`combine_dir()` default
  `legacy_blend=True`): gen_jam_art.py imports it. Never change those defaults; compare gen_jam_art.py output in a
  scratch folder (OUT patched) if they are touched.
- Parity tests (T1, T2) pass the legacy options only to the new entry points (`cli_case(..., legacy_argv)`); the frozen
  script gets the plain argv. The old combine parser strips `--legacy-blend` before parsing, so its usage and errors
  stay byte-identical; a new swap flag must default to None and resolve in `options_from_args` (as `--interp`,
  `--palette-alpha`, `--alpha` do) so `--legacy` can fill it.
- Agents never draw or edit textures with the tool (CLAUDE.md texture rule); tests use synthetic arrays.
