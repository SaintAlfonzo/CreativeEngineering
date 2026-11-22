# SPDX-License-Identifier: GPL-3.0-only
"""Texture tools Kivy app: nav bar + ScreenManager over the registered screens (plan_texture_tools_gui.md P5).

TextureToolsApp(settings=None, start_screen=None)
	settings          gui.settings.Settings (recent folders, project folder, dev mode)
	screens           {tool_name: screen instance}
	switch_to(name)   show a screen; current_tool() -> the visible screen
	open_in_editor(arr, name="")   hand an RGBA array to the editor screen and switch to it
	provenance_hook   None in P5; P6 sets fn(screen, params, outputs), called after Run / Save as in dev mode
Keys: Ctrl+Z undo, Ctrl+Y or Ctrl+Shift+Z redo, Ctrl+S save shortcut, Ctrl+1..9 switch screen (all to the current
screen). Files dropped outside any slot go to the current screen's handle_drop(paths).

Import this module before any other kivy.core import: it sets KIVY_NO_ARGS and the mouse config first.
"""
import os

os.environ.setdefault("KIVY_NO_ARGS", "1")

from kivy.config import Config  # noqa: E402

Config.set("input", "mouse", "mouse,multitouch_on_demand")
Config.set("kivy", "exit_on_escape", "0")

from kivy.app import App  # noqa: E402
from kivy.core.window import Window  # noqa: E402
from kivy.uix.boxlayout import BoxLayout  # noqa: E402
from kivy.uix.screenmanager import NoTransition, ScreenManager  # noqa: E402
from kivy.uix.togglebutton import ToggleButton  # noqa: E402

from .. import __version__  # noqa: E402
from . import registry, screens as screen_pkg  # noqa: E402
from .settings import Settings  # noqa: E402
from .widgets.fileslot import DropRouter  # noqa: E402


class TextureToolsApp(App):
	title = "Texture Tools " + __version__

	def __init__(self, settings=None, start_screen=None, **kw):
		super().__init__(**kw)
		self.settings = settings if settings is not None else Settings()
		self.start_screen = start_screen
		self.screens = {}
		self.nav_buttons = {}
		self.provenance_hook = None

	def build(self):
		screen_pkg.load_all()
		root = BoxLayout(orientation="vertical")
		nav = BoxLayout(size_hint_y=None, height=40, spacing=2, padding=2)
		self.manager = ScreenManager(transition=NoTransition())
		for cls in registry.ordered():
			scr = cls()
			self.screens[cls.tool_name] = scr
			self.manager.add_widget(scr)
			b = ToggleButton(text=cls.title, group="tt-nav", allow_no_selection=False)
			b.bind(on_release=lambda inst, n=cls.tool_name: self.switch_to(n))
			self.nav_buttons[cls.tool_name] = b
			nav.add_widget(b)
		root.add_widget(nav)
		root.add_widget(self.manager)
		start = self.start_screen or self.settings.get("last_screen")
		self.switch_to(start if start in self.screens else registry.ordered()[0].tool_name)
		DropRouter.instance().on_unslotted_drop = self._unslotted_drop
		Window.bind(on_key_down=self._on_key)
		return root

	def current_tool(self):
		return self.screens.get(self.manager.current)

	def switch_to(self, name):
		if name not in self.screens:
			raise KeyError(name)
		self.manager.current = name
		for n, b in self.nav_buttons.items():
			b.state = "down" if n == name else "normal"
		return self.screens[name]

	def open_in_editor(self, arr, name=""):
		ed = self.switch_to("editor")
		ed.load_array(arr, name)
		return ed

	def _unslotted_drop(self, paths, pos):
		scr = self.current_tool()
		if scr is not None and hasattr(scr, "handle_drop"):
			scr.handle_drop(paths)

	def _on_key(self, win, key, scancode, codepoint, modifiers):
		if "ctrl" not in modifiers:
			return False
		scr = self.current_tool()
		if codepoint == "z" and "shift" in modifiers or codepoint == "y":
			getattr(scr, "redo", lambda: None)()
		elif codepoint == "z":
			getattr(scr, "undo", lambda: None)()
		elif codepoint == "s":
			getattr(scr, "save_shortcut", lambda: None)()
		elif codepoint and codepoint.isdigit() and codepoint != "0":
			order = registry.ordered()
			i = int(codepoint) - 1
			if i < len(order):
				self.switch_to(order[i].tool_name)
		else:
			return False
		return True

	def on_stop(self):
		for scr in self.screens.values():
			getattr(scr, "shutdown", lambda: None)()
		try:
			if self.settings.get("last_screen") != self.manager.current:
				self.settings.set("last_screen", self.manager.current)
		except OSError:
			pass
