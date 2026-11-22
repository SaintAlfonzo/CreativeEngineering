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
- File dialogs (R9, P11) shell out to zenity / kdialog from `gui/widgets/filebrowser.py` in a worker thread; the result
  comes back through `Clock.schedule_once`. The child env must be cleaned (`dialog_env`): the PyInstaller bundle sets
  `LD_LIBRARY_PATH` to its `_internal` (restore `LD_LIBRARY_PATH_ORIG`), and a parent started from the VS Code /
  Codium snap carries `GTK_PATH`, `LOCPATH`, `GIO_MODULE_DIR`, `GDK_PIXBUF_*` into `/snap/...`, which makes the host
  zenity die with a libpthread / GLIBCXX symbol error. Agent shells here run under that snap: test dialogs with
  `env -u GTK_PATH -u GTK_EXE_PREFIX -u GIO_MODULE_DIR -u GDK_PIXBUF_MODULEDIR -u GDK_PIXBUF_MODULE_FILE -u LOCPATH
  -u GSETTINGS_SCHEMA_DIR -u GTK_IM_MODULE_FILE zenity ...`. `find_backend` must search the given env's PATH, not
  `shutil.which`'s default (tests run fake dialogs on a temp PATH). Never open a real dialog in a test: `set_runner`.
- Sidebar sections (R10): `ToolScreen.controls` is a `SectionPanel`; `add_heading` starts a section and every later
  `self.controls.add_widget` lands in its body, so screens keep plain build code. A folded body is detached from the
  tree (slots in it are skipped by drop routing via `get_root_window()`); code that reads a widget's window position
  must open its section first. Open state lives in settings key `open_sections` {tool: {title: bool}}, keyed by the
  first title (combine renames "Selected layer" through `Section.text`).
