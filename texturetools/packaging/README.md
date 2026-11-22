<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Texture Tools for Linux

Texture Tools swaps palettes, combines layers, paints, edits and scales small pixel-art textures (Minecraft-style
sprites). One program has a desktop app (Kivy) and the command-line tools. License: GPL-3.0-only (see `LICENSE`).
The bundle holds code and libraries only, with no textures. Linux x86_64 only; Windows and macOS builds are not planned.

## Install

1. Download `texturetools-<version>-linux-x86_64.tar.gz`.
2. Extract it anywhere: `tar -xzf texturetools-<version>-linux-x86_64.tar.gz`
3. Optional, to run it from any folder: `ln -s "$PWD/texturetools-<version>-linux-x86_64/texturetools" ~/.local/bin/texturetools`

No Python or other install is needed: Python, numpy, Pillow, Kivy and SDL2 are inside `app/`. The system needs
glibc 2.35 or newer (Ubuntu 22.04 and later, or a similar distribution), X11 or XWayland, and OpenGL 2.0 or newer
(Mesa is fine). To remove it, delete the folder; settings are in `~/.config/texturetools/` (or `$XDG_CONFIG_HOME/texturetools/`) and Kivy's log and config in
`~/.kivy/`.

## Run

| Command | What it does |
|---|---|
| `./texturetools` | Opens the app |
| `./texturetools gui --screen combine` | Opens the app on a screen: `swap`, `combine`, `paint`, `editor`, `scale`, `settings` |
| `./texturetools palette_swap ARGS...` | Palette swap from the command line |
| `./texturetools combine_tex ARGS...` | Layer combine |
| `./texturetools paint_tex ARGS...` | Painter |
| `./texturetools scale_tex ARGS...` | Scale |
| `./texturetools upscale_sprites ARGS...` | Batch sprite upscale |
| `./texturetools --version` | Version and library versions |
| `./texturetools selftest` | Opens the app, visits every screen and checks it (needs a display; `xvfb-run ./texturetools selftest` for a hidden one) |

Every tool prints its options with `--help`, for example `./texturetools palette_swap --help`. Settings > About shows
the version. In the app, Copy as CLI gives the matching `python3 -m texturetools.cli.<tool> ...` line for a checkout;
with the bundle, run the same arguments as `./texturetools <tool> ...`.

The AppImage takes the same commands: `./texturetools-<version>-linux-x86_64.AppImage palette_swap ARGS...`
(make it executable first; without FUSE, set `APPIMAGE_EXTRACT_AND_RUN=1`).

File dialogs are your desktop's own file browser: `zenity` (GNOME, Unity, Xfce, Cinnamon, MATE) or `kdialog` (KDE;
tried first in a KDE or LXQt session). Neither is bundled; install one if Browse reports "no system file dialog
found". `TEXTURETOOLS_FILE_DIALOG=zenity` or `=kdialog` picks one.

Examples:

	./texturetools palette_swap source.png palette.png out.png
	./texturetools combine_tex base.png overlay.png combined.png

## Run from source

	python3 -m pip install -r texturetools/requirements.txt
	python3 -m texturetools.gui
	python3 -m texturetools.cli.palette_swap --help

## Build

	texturetools/packaging/build.sh [--clean] [--no-appimage]

It makes a venv in `packaging/build/venv` (downloads the pinned packages from PyPI on the first run), runs PyInstaller
on `texturetools.spec` (one folder, not one file, for a fast start), writes `packaging/dist/texturetools/` and the
tarball `packaging/dist/texturetools-<version>-linux-x86_64.tar.gz`, and refuses a tarball that holds any image
outside Kivy's own theme data or any jar. An AppImage is made too when `appimagetool` is on `PATH`. Kivy's
PyInstaller hooks open a window while analyzing; the build runs them under `xvfb-run` when it is installed.

The spec keeps Kivy's window, text, image and clipboard providers and drops video, audio, camera, spelling and
GStreamer. It names the modules PyInstaller cannot see: the screens (loaded by name from `SCREEN_MODULES`), the
batch-kind engines, the CLIs and the Pillow plugins for PNG, GIF, TGA, WebP, BMP and JPEG.

## Dependencies and versions tested

| Package | Version | Use |
|---|---|---|
| Python | 3.10.12 | runtime (built on Ubuntu 22.04, glibc 2.35) |
| numpy | 1.26.4 | image math (`texturetools/requirements.txt`) |
| Pillow | 9.0.1 | image read and write |
| Kivy | 2.3.0 | app (bundles SDL2) |
| PyInstaller | 6.10.0 | build only (`packaging/requirements-build.txt`) |
| pyinstaller-hooks-contrib | 2026.10 | build only |

Tested 2026-11-22: the tarball extracted to a new folder, run as a different `HOME` with `PATH` and `PYTHONPATH`
free of any checkout or venv, under `xvfb-run`: `--version`, `selftest` (every screen, preview, zoom, drop, presets,
Open in Editor, About) and one palette swap and one combine on generated images.
