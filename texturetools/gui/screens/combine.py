# SPDX-License-Identifier: GPL-3.0-only
"""Combine screen: P5 placeholder (plan_texture_tools_gui.md section 4); P6 replaces this module in place."""
from ..registry import register_screen
from .base import PlaceholderScreen


@register_screen
class CombineScreen(PlaceholderScreen):
	tool_name = "combine"
	title = "Combine"
	order = 20
	phase = "P6"
