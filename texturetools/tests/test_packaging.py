# SPDX-License-Identifier: GPL-3.0-only
"""Packaging consistency (plan_texture_tools_gui.md P9), no build and no Kivy import.

texturetools/packaging/texturetools.spec must list as hidden imports every module PyInstaller cannot see: each screen in
gui/screens SCREEN_MODULES, each core module that calls batch.register_kind, each cli module, and the Pillow plugins
the tools read and write. tt_entry.py must route every cli module.

Usage: python3 texturetools/tests/test_packaging.py
"""
import ast
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PKG = os.path.dirname(HERE)
FAILS = []


def check(cond, what):
	if not cond:
		FAILS.append(what)
		print("FAIL " + what)


def main():
	spec = open(os.path.join(PKG, "packaging", "texturetools.spec"), encoding="utf-8").read()
	listed = set(re.findall(r'"((?:texturetools|PIL)\.[A-Za-z0-9_.]+)"', spec))
	init = ast.parse(open(os.path.join(PKG, "gui", "screens", "__init__.py"), encoding="utf-8").read())
	screens = []
	for node in ast.walk(init):
		if isinstance(node, ast.Assign) and any(getattr(t, "id", "") == "SCREEN_MODULES" for t in node.targets):
			screens = [e.value for e in node.value.elts]
	check(screens, "SCREEN_MODULES found")
	for s in screens:
		check("texturetools.gui.screens." + s in listed, "spec lists screen " + s)
	core = os.path.join(PKG, "core")
	for f in sorted(os.listdir(core)):
		if f.endswith(".py") and "register_kind(" in open(os.path.join(core, f), encoding="utf-8").read() \
				and f != "batch.py":
			check("texturetools.core." + f[:-3] in listed, "spec lists batch kind module core." + f[:-3])
	entry = open(os.path.join(PKG, "packaging", "tt_entry.py"), encoding="utf-8").read()
	for f in sorted(os.listdir(os.path.join(PKG, "cli"))):
		if f.endswith(".py") and f != "__init__.py":
			check("texturetools.cli." + f[:-3] in listed, "spec lists cli " + f[:-3])
			check('"{0}"'.format(f[:-3]) in entry, "tt_entry routes cli " + f[:-3])
	for p in ("Png", "Gif", "Tga", "WebP", "Bmp", "Jpeg"):
		check("PIL.{0}ImagePlugin".format(p) in listed, "spec lists Pillow plugin " + p)
	print("texturetools packaging: {0} failed".format(len(FAILS)))
	return 1 if FAILS else 0


if __name__ == "__main__":
	sys.exit(main())
