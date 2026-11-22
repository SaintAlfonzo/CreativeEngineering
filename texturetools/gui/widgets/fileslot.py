# SPDX-License-Identifier: GPL-3.0-only
"""File slot: a labeled path field with Browse and Clear that also takes files dropped from the desktop
(plan_texture_tools_gui.md section 8).

FileSlot(label, kind='file', filters=('*.png',), key=None)
	kind 'file' | 'files' | 'folder' | 'save' (save: picks an output path; nothing is written by the slot)
	paths    ListProperty of absolute paths (one entry except kind 'files'); path = first or ''
	set_paths(list) / clear(); on_paths(paths) event fires on every change
	accepts(path) -> bool (extension filter, folder kind takes folders or the folder of a dropped file)

Drop: DropRouter binds Window.on_drop_begin / on_drop_file / on_drop_end once. Files dropped together are routed as one
batch to the visible FileSlot under the cursor (a 'files' slot takes the whole batch, others the first accepted path);
when no slot is under the cursor, the batch goes to on_unslotted_drop(paths, (x, y)), which the app forwards to the
current screen's handle_drop(paths).
"""
import fnmatch
import os

from kivy.core.window import Window
from kivy.properties import ListProperty, StringProperty
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput

from .filebrowser import open_browser


class FileSlot(BoxLayout):
	paths = ListProperty([])
	label = StringProperty("")
	kind = StringProperty("file")

	def __init__(self, label="", kind="file", filters=("*.png",), key=None, settings=None, **kw):
		kw.setdefault("size_hint_y", None)
		kw.setdefault("height", 34)
		super().__init__(orientation="horizontal", spacing=4, **kw)
		self.label = label
		self.kind = kind
		self.filters = list(filters or [])
		self.key = key
		self.settings = settings
		self._lbl = Label(text=label, size_hint_x=None, width=110, halign="right", valign="middle")
		self._lbl.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		self.field = TextInput(multiline=False, hint_text="drop {0} here or Browse".format(
			"files" if kind == "files" else "a folder" if kind == "folder" else "a file"))
		self.field.bind(on_text_validate=lambda *a: self._typed())
		self.field.bind(focus=lambda inst, f: (not f) and self._typed())
		browse = Button(text="Browse", size_hint_x=None, width=80)
		browse.bind(on_release=lambda *a: self.browse())
		clear = Button(text="x", size_hint_x=None, width=30)
		clear.bind(on_release=lambda *a: self.clear())
		for w in (self._lbl, self.field, browse, clear):
			self.add_widget(w)
		self.bind(paths=self._show)
		DropRouter.instance().register(self)

	@property
	def path(self):
		return self.paths[0] if self.paths else ""

	def _show(self, *a):
		self.field.text = "; ".join(self.paths) if self.kind == "files" else self.path

	def _typed(self):
		text = self.field.text.strip()
		new = [p.strip() for p in text.split(";") if p.strip()] if self.kind == "files" else ([text] if text else [])
		new = [os.path.abspath(os.path.expanduser(p)) for p in new]
		if new != list(self.paths):
			self.paths = new

	def set_paths(self, paths):
		paths = [os.path.abspath(p) for p in paths if p]
		if self.kind != "files":
			paths = paths[:1]
		self.paths = paths

	def clear(self):
		self.paths = []

	def accepts(self, path):
		if self.kind == "folder":
			return True
		if os.path.isdir(path):
			return False
		if not self.filters or self.kind == "save":
			return True
		name = os.path.basename(path).lower()
		return any(fnmatch.fnmatch(name, f.lower()) for f in self.filters)

	def take_drop(self, paths):
		"""Paths dropped on this slot; returns True when any was taken."""
		ok = [p for p in paths if self.accepts(p)]
		if not ok:
			return False
		if self.kind == "folder":
			ok = [p if os.path.isdir(p) else os.path.dirname(p) for p in ok]
		self.set_paths(ok)
		s = self._settings()
		if s is not None:
			s.add_recent(ok[0])
		return True

	def _settings(self):
		if self.settings is not None:
			return self.settings
		from kivy.app import App
		return getattr(App.get_running_app(), "settings", None)

	def browse(self):
		start = self.path or None
		open_browser(self.kind, self.set_paths, settings=self._settings(), filters=self.filters,
			title=self.label or None, start=start,
			default_name=os.path.basename(self.path) if self.kind == "save" else "")

	def on_paths(self, inst, paths):
		pass


class DropRouter:
	"""Routes desktop file drops to the FileSlot under the cursor (one Window binding for the whole app)."""
	_inst = None

	@classmethod
	def instance(cls):
		if cls._inst is None:
			cls._inst = cls()
		return cls._inst

	def __init__(self):
		import weakref
		self._slots = weakref.WeakSet()
		self._batch = None
		self._pos = (0, 0)
		self.on_unslotted_drop = None
		Window.bind(on_drop_begin=self._begin, on_drop_file=self._file, on_drop_end=self._end)

	def register(self, slot):
		self._slots.add(slot)

	def _begin(self, win, x, y, *a):
		self._batch = []
		self._pos = (x, y)

	def _file(self, win, filename, x, y, *a):
		path = filename.decode("utf-8", "replace") if isinstance(filename, bytes) else str(filename)
		self._pos = (x, y)
		if self._batch is None:  # backend without drop_begin: route each file on its own
			self.route([path], (x, y))
		else:
			self._batch.append(path)

	def _end(self, win, x, y, *a):
		batch, self._batch = self._batch, None
		if batch:
			self.route(batch, self._pos)

	def slot_at(self, x, y):
		"""Visible slot under window position (x, y from the top, as SDL reports it)."""
		wy = Window.height - y
		for slot in list(self._slots):
			if slot.get_root_window() is None:
				continue
			if slot.collide_point(*slot.to_widget(x, wy)):
				return slot
		return None

	def route(self, paths, pos):
		slot = self.slot_at(*pos)
		if slot is not None and slot.take_drop(paths):
			return slot
		if self.on_unslotted_drop is not None:
			self.on_unslotted_drop(paths, pos)
		return None
