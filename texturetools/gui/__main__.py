# SPDX-License-Identifier: GPL-3.0-only
"""Launcher: python3 -m texturetools.gui [--screen NAME]"""
import argparse
import os
import sys


def main(argv=None):
	ap = argparse.ArgumentParser(prog="python3 -m texturetools.gui", description="Texture tools GUI")
	ap.add_argument("--screen", help="screen to open first (swap, combine, paint, editor, scale, settings)")
	args = ap.parse_args(argv)
	os.environ.setdefault("KIVY_NO_ARGS", "1")
	from .app import TextureToolsApp
	TextureToolsApp(start_screen=args.screen).run()
	return 0


if __name__ == "__main__":
	sys.exit(main())
