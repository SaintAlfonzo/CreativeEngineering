# SPDX-License-Identifier: GPL-3.0-only
"""Screens of the texture tools app. Each module registers its class with gui.registry.register_screen.

swap, combine, paint, editor, scale are P5 placeholders that P6 to P8 replace in place (same module, same tool_name);
settings is the app settings screen. A new screen adds its module name to SCREEN_MODULES.
"""
import importlib

SCREEN_MODULES = ("swap", "combine", "paint", "editor", "scale", "settings")


def load_all():
	for name in SCREEN_MODULES:
		importlib.import_module(__name__ + "." + name)
