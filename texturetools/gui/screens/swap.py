# SPDX-License-Identifier: GPL-3.0-only
"""Palette Swap screen: P5 placeholder (plan_texture_tools_gui.md section 3); P6 replaces this module in place."""
from ..registry import register_screen
from .base import PlaceholderScreen


@register_screen
class SwapScreen(PlaceholderScreen):
	tool_name = "swap"
	title = "Palette Swap"
	order = 10
	phase = "P6"
