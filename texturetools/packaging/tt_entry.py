# SPDX-License-Identifier: GPL-3.0-only
"""Entry point of the packaged texture tools (PyInstaller onedir, texturetools.spec).

One executable, sub-commands:
	texturetools [gui] [--screen NAME]      the Kivy app (default)
	texturetools palette_swap ARGS...       texturetools.cli.palette_swap (same for combine_tex, paint_tex,
	                                        scale_tex, upscale_sprites)
	texturetools --version                  version and the bundled library versions
	texturetools selftest                   GUI smoke test (texturetools/tests/test_gui_smoke.py) in the bundle; needs
	                                        a display: run it under xvfb-run. Temp dirs only, nothing else written.
Also runs from a checkout: python3 texturetools/packaging/tt_entry.py ...
"""
import os
import sys

CLI_TOOLS = ("palette_swap", "combine_tex", "paint_tex", "scale_tex", "upscale_sprites")
USAGE = "usage: texturetools [gui [--screen NAME] | {0} ARGS... | selftest | --version]".format(" | ".join(CLI_TOOLS))

if not getattr(sys, "frozen", False):
	_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
	if _root not in sys.path:
		sys.path.insert(0, _root)


def versions():
	import numpy
	import PIL
	from texturetools import __version__
	lines = ["Texture Tools {0} (GPL-3.0-only)".format(__version__),
		"Python {0}".format(sys.version.split()[0]), "numpy {0}".format(numpy.__version__),
		"Pillow {0}".format(PIL.__version__)]
	try:
		from importlib.metadata import version
		lines.append("Kivy {0}".format(version("Kivy")))
	except Exception:
		pass
	return "\n".join(lines)


def run_cli(name, argv):
	import importlib
	mod = importlib.import_module("texturetools.cli." + name)
	sys.argv = [name] + list(argv)
	rc = mod.main(list(argv))
	return rc if isinstance(rc, int) else 0


def selftest():
	import shutil
	import tempfile
	tmp = tempfile.mkdtemp(prefix="tt_selftest_")
	os.environ["TT_SMOKE_TMP"] = tmp
	os.environ["KIVY_HOME"] = os.path.join(tmp, "kivyhome")
	os.environ["TEXTURETOOLS_CONFIG_DIR"] = os.path.join(tmp, "ttcfg")
	os.environ.setdefault("KIVY_NO_CONSOLELOG", "1")
	os.environ.setdefault("SDL_AUDIODRIVER", "dummy")
	try:
		from texturetools.tests import test_gui_smoke as smoke
		smoke.part_a(tmp)
		rc = smoke.child()
		print("texturetools selftest: {0}".format("FAIL" if (rc or smoke.FAILS) else "PASS"))
		return 1 if (rc or smoke.FAILS) else 0
	finally:
		shutil.rmtree(tmp, ignore_errors=True)


def main(argv=None):
	argv = list(sys.argv[1:] if argv is None else argv)
	os.environ.setdefault("KIVY_NO_ARGS", "1")
	if argv and argv[0] in ("-h", "--help", "help"):
		print(USAGE)
		return 0
	if argv and argv[0] in ("--version", "version"):
		print(versions())
		return 0
	if argv and argv[0] in CLI_TOOLS:
		return run_cli(argv[0], argv[1:])
	if argv and argv[0] == "selftest":
		return selftest()
	if argv and argv[0] == "gui":
		argv = argv[1:]
	from texturetools.gui.__main__ import main as gui_main
	return gui_main(argv)


if __name__ == "__main__":
	sys.exit(main())
