# SPDX-License-Identifier: GPL-3.0-only
"""Settings store for the texture tools GUI (plan_texture_tools_gui.md section 8). No Kivy import.

One JSON file in the user config dir: $TEXTURETOOLS_CONFIG_DIR, else $XDG_CONFIG_HOME/texturetools, else
~/.config/texturetools. Holds recent folders, the project folder, dev mode (off by default; shipped builds have no
repo knowledge), provenance_csv (dev mode: the sources.csv to append to; empty = found from the output path, the
project folder or the package location) and small UI state. Presets live next to it in presets/<screen>/ (core/presets.py).

This is app config only: it never writes into a project or texture folder.
"""
import json
import os

DEFAULTS = {
	"recent_folders": [],
	"project_folder": "",
	"dev_mode": False,
	"provenance_csv": "",
	"last_screen": "",
}
MAX_RECENT = 12


def config_dir():
	env = os.environ.get("TEXTURETOOLS_CONFIG_DIR")
	if env:
		return env
	base = os.environ.get("XDG_CONFIG_HOME") or os.path.join(os.path.expanduser("~"), ".config")
	return os.path.join(base, "texturetools")


class Settings:
	"""Dict-like store. set() and add_recent() save at once (atomic replace); get() falls back to DEFAULTS."""

	def __init__(self, directory=None):
		self.directory = directory or config_dir()
		self.path = os.path.join(self.directory, "settings.json")
		self.data = dict(DEFAULTS)
		self._listeners = []
		self.load()

	def load(self):
		try:
			with open(self.path, encoding="utf-8") as f:
				loaded = json.load(f)
			if isinstance(loaded, dict):
				self.data.update(loaded)
		except (OSError, ValueError):
			pass
		return self

	def save(self):
		os.makedirs(self.directory, exist_ok=True)
		tmp = self.path + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(self.data, f, indent="\t", sort_keys=True)
			f.write("\n")
		os.replace(tmp, self.path)

	def get(self, key, default=None):
		if key in self.data:
			return self.data[key]
		return DEFAULTS.get(key, default)

	def set(self, key, value, save=True):
		self.data[key] = value
		if save:
			self.save()
		for fn in list(self._listeners):
			fn(key, value)

	def bind(self, fn):
		"""fn(key, value) after every set()."""
		self._listeners.append(fn)

	@property
	def dev_mode(self):
		return bool(self.get("dev_mode"))

	@property
	def project_folder(self):
		return self.get("project_folder") or ""

	@property
	def presets_dir(self):
		return os.path.join(self.directory, "presets")

	def recent_folders(self):
		return [p for p in self.get("recent_folders") or [] if os.path.isdir(p)]

	def add_recent(self, path):
		"""Remember the folder of path (or path itself when it is a folder), newest first."""
		if not path:
			return
		folder = path if os.path.isdir(path) else os.path.dirname(path)
		folder = os.path.abspath(folder)
		rec = [p for p in self.get("recent_folders") or [] if p != folder]
		rec.insert(0, folder)
		self.set("recent_folders", rec[:MAX_RECENT])

	def start_folder(self):
		"""Where a file browser opens: newest recent folder, else project folder, else home."""
		for p in self.recent_folders():
			return p
		if self.project_folder and os.path.isdir(self.project_folder):
			return self.project_folder
		return os.path.expanduser("~")
