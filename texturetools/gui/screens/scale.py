# SPDX-License-Identifier: GPL-3.0-only
"""Scale screen: P5 placeholder (plan_texture_tools_gui.md section 7); P8 replaces this module in place."""
from ..registry import register_screen
from .base import PlaceholderScreen


@register_screen
class ScaleScreen(PlaceholderScreen):
	tool_name = "scale"
	title = "Scale"
	order = 50
	phase = "P8"
