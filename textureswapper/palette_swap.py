# SPDX-License-Identifier: GPL-3.0-only
"""Shim: palette_swap moved to texturetools/cli/palette_swap.py (engine in texturetools/core/, plan_texture_tools_gui.md P0).
Same command line and the same module names as before, so scripts that run or import this file keep working.
"""
import os
import sys

_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if _ROOT not in sys.path:
	sys.path.append(_ROOT)

from texturetools.cli.palette_swap import *  # noqa: E402,F401,F403
from texturetools.cli import palette_swap as _cli  # noqa: E402

# Bulk mode folders -- edit these to point at your own source/output folders.
# ALL image files in BULK_SOURCE_DIR are processed when --bulk is used.
BULK_SOURCE_DIR = "./in"
BULK_OUTPUT_DIR = "./out"

if __name__ == "__main__":
	_cli.main(bulk_source_dir=BULK_SOURCE_DIR, bulk_output_dir=BULK_OUTPUT_DIR)
