# SPDX-License-Identifier: GPL-3.0-only
"""Painter screen: P5 placeholder (plan_texture_tools_gui.md section 5); P8 replaces this module in place."""
from ..registry import register_screen
from .base import PlaceholderScreen


@register_screen
class PaintScreen(PlaceholderScreen):
	tool_name = "paint"
	title = "Painter"
	order = 30
	phase = "P8"
