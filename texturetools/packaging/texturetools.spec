# -*- mode: python -*-
# SPDX-License-Identifier: GPL-3.0-only
# PyInstaller onedir spec for the texture tools (plan_texture_tools_gui.md P9). Run by build.sh:
#	pyinstaller --noconfirm --distpath packaging/dist --workpath packaging/build/pyi texturetools.spec
# One executable (tt_entry.py) serves the GUI and every CLI as sub-commands. Code only: no pack art, no generated
# textures, no donor jars (build.sh checks the tarball listing).
import os

from kivy.tools.packaging.pyinstaller_hooks import get_deps_minimal, hookspath, runtime_hooks

HERE = os.path.abspath(SPECPATH)  # noqa: F821 (PyInstaller global)
ROOT = os.path.dirname(os.path.dirname(HERE))

# Kivy: window, text, image, clipboard only. video, audio, camera, spelling and gstreamer excluded (size).
kivy_deps = get_deps_minimal(video=None, audio=None, camera=None, spelling=None)

hidden = list(kivy_deps["hiddenimports"])
hidden += [
	# clipboard fallbacks beyond what get_deps_minimal found on the build machine
	"kivy.core.clipboard.clipboard_sdl2", "kivy.core.clipboard.clipboard_xsel", "kivy.core.clipboard.clipboard_dummy",
	# screens are loaded by importlib from SCREEN_MODULES (gui/screens/__init__.py)
	"texturetools.gui.screens.swap", "texturetools.gui.screens.combine", "texturetools.gui.screens.paint",
	"texturetools.gui.screens.editor", "texturetools.gui.screens.scale", "texturetools.gui.screens.settings",
	# batch kinds register themselves on import (core/batch.py register_kind)
	"texturetools.core.swap", "texturetools.core.combine", "texturetools.core.paint", "texturetools.core.scale",
	# CLIs, reached through tt_entry.py sub-commands
	"texturetools.cli.palette_swap", "texturetools.cli.combine_tex", "texturetools.cli.paint_tex",
	"texturetools.cli.scale_tex", "texturetools.cli.upscale_sprites",
	# `texturetools selftest` runs the GUI smoke test in the bundle
	"texturetools.tests.test_gui_smoke",
	# Pillow formats the tools read and write
	"PIL.PngImagePlugin", "PIL.GifImagePlugin", "PIL.TgaImagePlugin", "PIL.WebPImagePlugin", "PIL.BmpImagePlugin",
	"PIL.JpegImagePlugin",
]

excludes = list(kivy_deps["excludes"]) + [
	"kivy.core.video", "kivy.core.audio", "kivy.core.camera", "kivy.core.spelling", "kivy.lib.gstplayer",
	"gi", "ffpyplayer", "cv2", "enchant", "pygame",
	"texturetools.tests.legacy", "textureswapper",
	"matplotlib", "scipy", "pandas", "IPython", "pytest", "docutils", "pygments", "PIL.ImageQt", "PyQt5", "PySide2",
	"PIL.ImageTk",
]

a = Analysis(  # noqa: F821
	[os.path.join(HERE, "tt_entry.py")],
	pathex=[ROOT],
	binaries=[],
	datas=[],
	hiddenimports=hidden,
	hookspath=hookspath(),
	runtime_hooks=runtime_hooks(),
	excludes=excludes,
	noarchive=False,
)
pyz = PYZ(a.pure)  # noqa: F821
exe = EXE(  # noqa: F821
	pyz,
	a.scripts,
	[],
	exclude_binaries=True,
	name="texturetools",
	debug=False,
	strip=False,
	upx=False,
	console=True,
)
coll = COLLECT(  # noqa: F821
	exe,
	a.binaries,
	a.datas,
	strip=False,
	upx=False,
	name="texturetools",
)
