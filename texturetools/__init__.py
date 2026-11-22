# SPDX-License-Identifier: GPL-3.0-only
"""Texture tools: palette swap, combine, paint, edit and scale for pack art (plan_texture_tools_gui.md).

core/  pure numpy + Pillow engines (no Kivy), headless-testable
cli/   command-line wrappers, same flags as the old textureswapper/ scripts (those are now shims onto cli/)
gui/   Kivy app (P5 onward)
tests/ headless parity and property tests, run by tools/test_texturetools.sh (tools/check_all.py)
"""

__version__ = "0.1.0"
