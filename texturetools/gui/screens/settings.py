# SPDX-License-Identifier: GPL-3.0-only
"""Settings screen: project folder, dev mode, recent folders, About (plan_texture_tools_gui.md section 8).

show_about() opens the About popup (version, license, library versions) and returns it.
"""
import sys

from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.checkbox import CheckBox
from kivy.uix.label import Label
from kivy.uix.popup import Popup
from kivy.uix.screenmanager import Screen

from ... import __version__
from ..registry import register_screen
from ..widgets.fileslot import FileSlot


@register_screen
class SettingsScreen(Screen):
	tool_name = "settings"
	title = "Settings"
	order = 100

	def __init__(self, **kw):
		kw.setdefault("name", self.tool_name)
		super().__init__(**kw)
		app = App.get_running_app()
		self.settings = getattr(app, "settings", None)
		box = BoxLayout(orientation="vertical", spacing=6, padding=12)
		self.project_slot = FileSlot(label="Project folder", kind="folder", settings=self.settings)
		if self.settings is not None and self.settings.project_folder:
			self.project_slot.set_paths([self.settings.project_folder])
		self.project_slot.bind(paths=lambda inst, p: self._set("project_folder", inst.path))
		box.add_widget(self.project_slot)

		row = BoxLayout(size_hint_y=None, height=34, spacing=4)
		self.dev_box = CheckBox(active=bool(self.settings and self.settings.dev_mode), size_hint_x=None, width=40)
		self.dev_box.bind(active=lambda inst, v: self._set("dev_mode", bool(v)))
		row.add_widget(self.dev_box)
		row.add_widget(Label(text="Dev mode (after Run, offer a texture provenance row; pack repo only)", halign="left",
			valign="middle", text_size=(600, None)))
		box.add_widget(row)

		self.recent_label = Label(size_hint_y=None, height=140, halign="left", valign="top")
		self.recent_label.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		box.add_widget(self.recent_label)
		clear = Button(text="Clear recent folders", size_hint=(None, None), size=(200, 34))
		clear.bind(on_release=lambda *a: self._set("recent_folders", []))
		box.add_widget(clear)
		about = BoxLayout(size_hint_y=None, height=34, spacing=8)
		about.add_widget(Label(text="Texture Tools {0}  -  GPL-3.0-only".format(__version__)))
		about_btn = Button(text="About", size_hint_x=None, width=100)
		about_btn.bind(on_release=lambda *a: self.show_about())
		about.add_widget(about_btn)
		box.add_widget(about)
		box.add_widget(Label())
		self.add_widget(box)

	def show_about(self):
		lines = ["Texture Tools {0}".format(__version__), "License: GPL-3.0-only", "",
			"Python {0}".format(sys.version.split()[0])]
		for mod, label in (("numpy", "numpy"), ("PIL", "Pillow"), ("kivy", "Kivy")):
			m = sys.modules.get(mod)
			if m is None:
				try:
					m = __import__(mod)
				except ImportError:
					continue
			lines.append("{0} {1}".format(label, getattr(m, "__version__", "?")))
		body = BoxLayout(orientation="vertical", spacing=6, padding=8)
		body.add_widget(Label(text="\n".join(lines), halign="center"))
		close = Button(text="Close", size_hint_y=None, height=34)
		body.add_widget(close)
		popup = Popup(title="About Texture Tools", content=body, size_hint=(None, None), size=(360, 300))
		close.bind(on_release=lambda *a: popup.dismiss())
		popup.open()
		self.about_popup = popup
		return popup

	def on_pre_enter(self, *a):
		rec = self.settings.recent_folders() if self.settings is not None else []
		self.recent_label.text = "Recent folders:\n" + ("\n".join(rec) if rec else "(none)")

	def _set(self, key, value):
		if self.settings is not None and self.settings.get(key) != value:
			self.settings.set(key, value)
			if key == "recent_folders":
				self.on_pre_enter()

	def shutdown(self):
		pass
