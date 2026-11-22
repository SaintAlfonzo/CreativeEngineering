# SPDX-License-Identifier: GPL-3.0-only
"""Editor screen: P5 placeholder (plan_texture_tools_gui.md section 6); P7 replaces this module in place."""
from ..registry import register_screen
from .base import PlaceholderScreen


@register_screen
class EditorScreen(PlaceholderScreen):
	tool_name = "editor"
	title = "Editor"
	order = 40
	phase = "P7"
