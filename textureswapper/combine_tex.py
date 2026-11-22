# SPDX-License-Identifier: GPL-3.0-only
"""Shim: combine_tex moved to texturetools/cli/combine_tex.py (engine in texturetools/core/, plan_texture_tools_gui.md P0).
Same command line and the same module names as before, so scripts that run or import this file keep working.
"""
import os
import sys

_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if _ROOT not in sys.path:
	sys.path.append(_ROOT)

from texturetools.cli.combine_tex import *  # noqa: E402,F401,F403
from texturetools.cli import combine_tex as _cli  # noqa: E402

if __name__ == "__main__":
	_cli.main()
