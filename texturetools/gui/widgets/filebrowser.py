# SPDX-License-Identifier: GPL-3.0-only
"""Shared file browser popup (plan_texture_tools_gui.md section 8).

open_browser(mode, on_select, settings=None, filters=("*.png",), title=None, start=None, default_name="")
	mode 'file' (one file), 'files' (several), 'folder', 'save' (a path to write; the browser itself writes nothing).
	on_select(paths) gets a list of absolute paths (one entry except in 'files' mode); not called on Cancel.
	The side column lists the project folder and recent folders from the Settings store; the chosen folder is added to
	recent folders. In 'save' mode an existing file needs a second click (button reads "Overwrite?").
"""
import os

from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.filechooser import FileChooserListView
from kivy.uix.label import Label
from kivy.uix.popup import Popup
from kivy.uix.scrollview import ScrollView
from kivy.uix.textinput import TextInput

MODES = ("file", "files", "folder", "save")


def _settings(settings):
	if settings is not None:
		return settings
	app = App.get_running_app()
	return getattr(app, "settings", None)


class FileBrowser(BoxLayout):
	def __init__(self, mode, on_select, settings=None, filters=("*.png",), start=None, default_name="", on_cancel=None,
			**kw):
		super().__init__(orientation="vertical", spacing=4, **kw)
		if mode not in MODES:
			raise ValueError("mode must be one of {0}".format(MODES))
		self.mode = mode
		self.on_select = on_select
		self.on_cancel = on_cancel
		self.settings = _settings(settings)
		self._confirm_overwrite = None
		start = start or (self.settings.start_folder() if self.settings else os.path.expanduser("~"))
		if start and not os.path.isdir(start):
			start = os.path.dirname(start)
		if not start or not os.path.isdir(start):
			start = os.path.expanduser("~")

		body = BoxLayout(orientation="horizontal", spacing=4)
		side = BoxLayout(orientation="vertical", size_hint_y=None, spacing=2)
		side.bind(minimum_height=side.setter("height"))
		side_scroll = ScrollView(size_hint_x=0.3)
		side_scroll.add_widget(side)
		self.chooser = FileChooserListView(path=start, filters=list(filters) if mode != "folder" else [],
			multiselect=(mode == "files"), dirselect=(mode == "folder"))
		self._fill_side(side)
		body.add_widget(side_scroll)
		body.add_widget(self.chooser)
		self.add_widget(body)

		bar = BoxLayout(size_hint_y=None, height=36, spacing=4)
		self.name_input = None
		if mode == "save":
			self.name_input = TextInput(text=default_name, multiline=False, hint_text="file name")
			self.name_input.bind(text=lambda *a: self._reset_confirm())
			bar.add_widget(self.name_input)
			self.chooser.bind(selection=self._selection_to_name)
		self.ok_button = Button(text={"save": "Save here", "folder": "Use folder"}.get(mode, "Open"), size_hint_x=None,
			width=120)
		self.ok_button.bind(on_release=lambda *a: self._ok())
		cancel = Button(text="Cancel", size_hint_x=None, width=90)
		cancel.bind(on_release=lambda *a: self._cancel())
		bar.add_widget(self.ok_button)
		bar.add_widget(cancel)
		self.add_widget(bar)

	def _fill_side(self, side):
		def add(label, path):
			b = Button(text=label, size_hint_y=None, height=30, halign="left", shorten=True, shorten_from="left")
			b.bind(size=lambda inst, *a: setattr(inst, "text_size", (inst.width - 8, None)))
			b.bind(on_release=lambda *a: setattr(self.chooser, "path", path))
			side.add_widget(b)
		s = self.settings
		side.add_widget(Label(text="Places", size_hint_y=None, height=24))
		add("Home", os.path.expanduser("~"))
		if s is not None and s.project_folder and os.path.isdir(s.project_folder):
			add("Project: " + s.project_folder, s.project_folder)
		if s is not None:
			rec = s.recent_folders()
			if rec:
				side.add_widget(Label(text="Recent", size_hint_y=None, height=24))
			for p in rec:
				add(p, p)

	def _selection_to_name(self, chooser, sel):
		if sel and not os.path.isdir(sel[0]):
			self.name_input.text = os.path.basename(sel[0])

	def _reset_confirm(self):
		self._confirm_overwrite = None
		self.ok_button.text = "Save here"

	def result_paths(self):
		if self.mode == "save":
			name = (self.name_input.text or "").strip()
			if not name:
				return []
			return [os.path.abspath(os.path.join(self.chooser.path, name))]
		if self.mode == "folder":
			sel = [p for p in self.chooser.selection if os.path.isdir(p)]
			return [os.path.abspath(sel[0] if sel else self.chooser.path)]
		sel = [os.path.abspath(p) for p in self.chooser.selection if os.path.isfile(p)]
		return sel if self.mode == "files" else sel[:1]

	def _ok(self):
		paths = self.result_paths()
		if not paths:
			return
		if self.mode == "save" and os.path.exists(paths[0]) and self._confirm_overwrite != paths[0]:
			self._confirm_overwrite = paths[0]
			self.ok_button.text = "Overwrite?"
			return
		if self.settings is not None:
			self.settings.add_recent(paths[0])
		self.on_select(paths)

	def _cancel(self):
		if self.on_cancel:
			self.on_cancel()


def open_browser(mode, on_select, settings=None, filters=("*.png",), title=None, start=None, default_name=""):
	"""Popup with a FileBrowser; returns the Popup (already open)."""
	popup = Popup(title=title or {"file": "Open file", "files": "Open files", "folder": "Choose folder",
		"save": "Save as"}[mode], size_hint=(0.9, 0.9), auto_dismiss=True)

	def done(paths):
		popup.dismiss()
		on_select(paths)

	popup.content = FileBrowser(mode, done, settings=settings, filters=filters, start=start, default_name=default_name,
		on_cancel=popup.dismiss)
	popup.open()
	return popup
