# SPDX-License-Identifier: GPL-3.0-only
"""Shim: upscale_sprites moved to texturetools/cli/upscale_sprites.py (engine in texturetools/core/, plan_texture_tools_gui.md P0).
Same command line and the same module names as before, so scripts that run or import this file keep working.
"""
import os
import sys

_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if _ROOT not in sys.path:
	sys.path.append(_ROOT)

from texturetools.cli.upscale_sprites import *  # noqa: E402,F401,F403
from texturetools.cli import upscale_sprites as _cli  # noqa: E402

if __name__ == "__main__":
	_cli.main()
