# SPDX-License-Identifier: GPL-3.0-only
"""Screen registry (plan_texture_tools_gui.md P5). No Kivy import.

A screen module decorates its class with @register_screen; the class carries
	tool_name  unique screen name (also the preset folder and the Copy as CLI key)
	title      nav bar text
	order      nav bar position (lower first)
screens/__init__.py lists the modules the app imports (SCREEN_MODULES); a new screen adds its module name there.
"""
SCREENS = {}


def register_screen(cls):
	name = getattr(cls, "tool_name", None)
	if not name:
		raise ValueError("{0} has no tool_name".format(cls.__name__))
	SCREENS[name] = cls
	return cls


def ordered():
	"""Registered screen classes in nav order."""
	return sorted(SCREENS.values(), key=lambda c: (getattr(c, "order", 50), c.tool_name))
