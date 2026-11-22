# SPDX-License-Identifier: GPL-3.0-only
"""ToolScreen: the shared params-dict -> engine call pattern every tool screen uses (plan_texture_tools_gui.md
sections 2 and 8).

Subclass, set tool_name / title / order, decorate with @register_screen, and override:
	default_params(self) -> dict        initial params (JSON-safe values; presets store this dict)
	build_controls(self)                add slots and controls to self.controls via the add_* helpers
	compute_preview(self, params)       WORKER THREAD, pure (never writes a file): returns an RGBA array for
	                                    self.preview, a dict {"preview": arr, "status": str, ...} or None (no preview)
	show_preview(self, result)          main thread; default shows result / result["preview"] in self.preview and
	                                    keeps it as self.result_array (what Save as and Open in Editor use)
	run_job(self, params, progress)     WORKER THREAD, only from the explicit Run button: writes outputs, returns the
	                                    list of written paths; progress(fraction, message). None = no Run button
	validate(self, params) -> str|None  message blocking Run (and preview) when params are incomplete
	cli_argv(self, params) -> list|None argv for Copy as CLI (or presets.register_cli_builder(tool_name, fn))
	handle_drop(self, paths)            files dropped outside any slot (default: first slot that accepts them)
	load_array(self, arr, name)         receive an image from another screen (Open in Editor handoff)
	undo / redo / save_shortcut         Ctrl+Z, Ctrl+Y / Ctrl+Shift+Z, Ctrl+S from the app
Params flow: a control change -> set_param(key, value) -> 150 ms debounce -> compute_preview on the preview worker
(latest wins, params deep-copied) -> show_preview on the main thread. Run uses a second, in-order worker.
No file is written except by Run, Save as and preset save, each an explicit button.
"""
import os

from kivy.app import App
from kivy.logger import Logger
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.screenmanager import Screen
from kivy.uix.scrollview import ScrollView
from kivy.uix.spinner import Spinner
from kivy.uix.textinput import TextInput

from ...core import presets
from ..widgets.controls import ChoiceControl, NumberControl, TextControl, ToggleControl
from ..widgets.fileslot import FileSlot
from ..widgets.filebrowser import open_browser
from ..widgets.preview import PixelPreview
from ..worker import Debouncer, Worker

BAR_H = 36


class ToolScreen(Screen):
	tool_name = ""
	title = ""
	order = 50
	placeholder = False

	def __init__(self, **kw):
		kw.setdefault("name", self.tool_name)
		super().__init__(**kw)
		self.params = dict(self.default_params())
		self.slots = {}
		self.param_controls = {}
		self.result_array = None
		self.result_name = ""
		self._preview_worker = None
		self._run_worker = None
		self._debounce = Debouncer(self._start_preview)
		self._build_layout()
		self.build_controls()
		self._refresh_presets()

	# ---- overridables ----

	def default_params(self):
		return {}

	def build_controls(self):
		pass

	def compute_preview(self, params):
		return None

	run_job = None

	def validate(self, params):
		return None

	def cli_argv(self, params):
		return presets.cli_argv(self.tool_name, params)

	def handle_drop(self, paths):
		for slot in self.slots.values():
			if slot.take_drop(paths):
				return True
		self.set_status("no slot here takes {0}".format(os.path.basename(paths[0])))
		return False

	def load_array(self, arr, name=""):
		self.result_array = arr
		self.result_name = name
		self.preview.set_array(arr, fit=True)
		self.set_status("loaded {0}".format(name or "image"))

	def undo(self):
		pass

	def redo(self):
		pass

	def save_shortcut(self):
		self.save_as()

	# ---- layout ----

	def _build_layout(self):
		root = BoxLayout(orientation="horizontal", spacing=6, padding=6)
		left = BoxLayout(orientation="vertical", size_hint_x=0.36, spacing=4)
		scroll = ScrollView(do_scroll_x=False)
		self.controls = BoxLayout(orientation="vertical", size_hint_y=None, spacing=4)
		self.controls.bind(minimum_height=self.controls.setter("height"))
		scroll.add_widget(self.controls)
		left.add_widget(scroll)
		left.add_widget(self._preset_bar())
		root.add_widget(left)

		right = BoxLayout(orientation="vertical", spacing=4)
		self.preview_area = BoxLayout(orientation="vertical")
		self.preview = PixelPreview()
		self.preview_area.add_widget(self.preview)
		right.add_widget(self.preview_area)
		right.add_widget(self._zoom_bar())
		right.add_widget(self._action_bar())
		self.status = Label(text="", size_hint_y=None, height=24, halign="left", valign="middle")
		self.status.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		right.add_widget(self.status)
		root.add_widget(right)
		self.add_widget(root)

	def _button(self, text, fn, width=None):
		b = Button(text=text, size_hint_x=None if width else 1, width=width or 100)
		b.bind(on_release=lambda *a: fn())
		return b

	def _zoom_bar(self):
		bar = BoxLayout(size_hint_y=None, height=BAR_H, spacing=4)
		bar.add_widget(self._button("Fit", self.preview.fit, 60))
		bar.add_widget(self._button("-", self.preview.zoom_out, 40))
		bar.add_widget(self._button("+", self.preview.zoom_in, 40))
		lbl = Label(text="1x", size_hint_x=None, width=60)
		self.preview.bind(zoom=lambda inst, z: setattr(lbl, "text", "{0}x".format(int(z))))
		bar.add_widget(lbl)
		bar.add_widget(Label())
		return bar

	def _action_bar(self):
		bar = BoxLayout(size_hint_y=None, height=BAR_H, spacing=4)
		self.run_button = self._button("Run", self.run)
		self.run_button.disabled = self.run_job is None
		bar.add_widget(self.run_button)
		bar.add_widget(self._button("Save as", self.save_as))
		if self.tool_name != "editor":
			bar.add_widget(self._button("Open in Editor", self.open_in_editor))
		bar.add_widget(self._button("Copy as CLI", self.copy_cli))
		return bar

	def _preset_bar(self):
		bar = BoxLayout(size_hint_y=None, height=BAR_H, spacing=4)
		self.preset_spinner = Spinner(text="Presets", values=[])
		self.preset_spinner.bind(text=lambda inst, v: v not in ("Presets", "") and self.apply_preset(v))
		self.preset_name = TextInput(multiline=False, hint_text="preset name", size_hint_x=0.8)
		bar.add_widget(self.preset_spinner)
		bar.add_widget(self.preset_name)
		bar.add_widget(self._button("Save preset", lambda: self.save_preset(self.preset_name.text), 100))
		return bar

	# ---- control helpers (each wires to set_param and is restored by presets) ----

	def add_slot(self, key, label, kind="file", filters=("*.png",)):
		slot = FileSlot(label=label, kind=kind, filters=filters, key=key)
		cur = self.params.get(key)
		if cur:
			slot.set_paths(cur if isinstance(cur, list) else [cur])
		slot.bind(paths=lambda inst, paths: self.set_param(key, list(paths) if kind == "files" else inst.path))
		self.slots[key] = slot
		self.controls.add_widget(slot)
		return slot

	def _add_control(self, ctl):
		self.param_controls[ctl.key] = ctl
		self.controls.add_widget(ctl)
		return ctl

	def add_number(self, key, label, lo, hi, step=1):
		return self._add_control(NumberControl(key, label, self.params.get(key, lo), lo, hi, step, self.set_param))

	def add_toggle(self, key, label):
		return self._add_control(ToggleControl(key, label, self.params.get(key, False), self.set_param))

	def add_choice(self, key, label, values):
		return self._add_control(ChoiceControl(key, label, self.params.get(key, values[0]), values, self.set_param))

	def add_text(self, key, label):
		return self._add_control(TextControl(key, label, self.params.get(key, ""), self.set_param))

	def add_heading(self, text):
		lbl = Label(text="[b]{0}[/b]".format(text), markup=True, size_hint_y=None, height=28, halign="left")
		lbl.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		self.controls.add_widget(lbl)
		return lbl

	# ---- params and preview ----

	def set_param(self, key, value):
		if self.params.get(key) == value:
			return
		self.params[key] = value
		self.request_preview()

	def update_params(self, values):
		"""Set several params and refresh controls and slots to match (preset load)."""
		self.params.update(values)
		for key, v in values.items():
			if key in self.slots:
				slot = self.slots[key]
				slot.set_paths(v if isinstance(v, list) else ([v] if v else []))
			elif key in self.param_controls:
				self.param_controls[key].set_value(v)
		self.request_preview()

	def request_preview(self):
		self._debounce()

	def refresh_now(self):
		self._debounce.flush()

	def _start_preview(self):
		if self.validate(self.params) is not None:
			return
		if self._preview_worker is None:
			self._preview_worker = Worker(latest_only=True, name="tt-preview-" + self.tool_name)
		self._preview_worker.submit(self.compute_preview, self.params, on_done=self._preview_done,
			on_error=self._error)

	def _preview_done(self, result):
		if result is None:
			return
		self.show_preview(result)

	def show_preview(self, result):
		arr = result.get("preview") if isinstance(result, dict) else result
		if isinstance(result, dict) and result.get("status"):
			self.set_status(result["status"])
		if arr is not None:
			self.result_array = arr
			self.preview.set_array(arr)

	# ---- actions ----

	def set_status(self, text):
		self.status.text = text

	def _error(self, exc, tb):
		Logger.error("texturetools: %s: %s\n%s", self.tool_name, exc, tb)
		self.set_status("error: {0}".format(exc))

	def run(self):
		if self.run_job is None:
			return
		msg = self.validate(self.params)
		if msg:
			self.set_status(msg)
			return
		if self._run_worker is None:
			self._run_worker = Worker(latest_only=False, name="tt-run-" + self.tool_name)
		self.run_button.disabled = True
		self.set_status("running ...")
		self._run_worker.submit(self.run_job, self.params, on_done=self._run_done, on_error=self._run_error,
			on_progress=lambda f, m: self.set_status("{0:.0f}% {1}".format(f * 100, m)))

	def _run_done(self, outputs):
		self.run_button.disabled = False
		outputs = list(outputs or [])
		self.set_status("wrote {0} file(s)".format(len(outputs)))
		self.after_run(self.params, outputs)

	def _run_error(self, exc, tb):
		self.run_button.disabled = False
		self._error(exc, tb)

	def after_run(self, params, outputs):
		"""Dev mode hook (plan section 8): P6 offers the provenance row here; shipped builds skip it."""
		app = App.get_running_app()
		if outputs and app is not None and getattr(app, "settings", None) is not None and app.settings.dev_mode:
			hook = getattr(app, "provenance_hook", None)
			if hook is not None:
				hook(self, params, outputs)

	def save_as(self):
		if self.result_array is None:
			self.set_status("nothing to save yet")
			return
		arr = self.result_array
		default = self.result_name or "{0}.png".format(self.tool_name)

		def write(paths):
			from ...core.image_io import save_raw
			path = paths[0]
			if not path.lower().endswith(".png"):
				path += ".png"
			save_raw(arr, path)
			self.set_status("saved {0}".format(path))
			self.after_run(self.params, [path])

		open_browser("save", write, title="Save {0} result".format(self.title), default_name=default)

	def open_in_editor(self):
		if self.result_array is None:
			self.set_status("nothing to open yet")
			return
		App.get_running_app().open_in_editor(self.result_array, self.result_name)

	def cli_string(self):
		argv = self.cli_argv(self.params)
		if not argv:
			return None
		import shlex
		return " ".join(shlex.quote(str(a)) for a in argv)

	def copy_cli(self):
		s = self.cli_string()
		if not s:
			self.set_status("no CLI equivalent for this screen yet")
			return None
		try:
			from kivy.core.clipboard import Clipboard
			Clipboard.copy(s)
			self.set_status("copied: " + s)
		except Exception as e:  # clipboard provider missing (headless)
			self.set_status("CLI: {0} (clipboard unavailable: {1})".format(s, e))
		return s

	# ---- presets ----

	def _presets_base(self):
		app = App.get_running_app()
		s = getattr(app, "settings", None)
		return s.presets_dir if s is not None else None

	def _refresh_presets(self):
		base = self._presets_base()
		self.preset_spinner.values = presets.list_presets(base, self.tool_name) if base else []

	def save_preset(self, name):
		base = self._presets_base()
		if not base:
			return None
		path = presets.save_preset(base, self.tool_name, name, self.params)
		self._refresh_presets()
		self.set_status("preset saved: {0}".format(presets.safe_name(name)))
		return path

	def apply_preset(self, name):
		base = self._presets_base()
		try:
			values = presets.load_preset(base, self.tool_name, name)
		except (OSError, ValueError) as e:
			self.set_status("preset not loaded: {0}".format(e))
			return
		self.update_params(values)
		self.set_status("preset loaded: {0}".format(name))

	# ---- lifecycle ----

	def shutdown(self):
		self._debounce.cancel()
		for w in (self._preview_worker, self._run_worker):
			if w is not None:
				w.close()


class PlaceholderScreen(ToolScreen):
	"""P5 stand-in: one source slot shown in the preview. P6 to P8 replace each placeholder module in place."""
	placeholder = True
	phase = ""

	def default_params(self):
		return {"source": ""}

	def build_controls(self):
		self.add_heading(self.title)
		note = Label(text="Placeholder screen; the real one lands in {0}.".format(self.phase), size_hint_y=None,
			height=28)
		self.controls.add_widget(note)
		self.add_slot("source", "Source", kind="file")

	def validate(self, params):
		return None if params.get("source") else "pick a source"

	def compute_preview(self, params):
		from ...core.image_io import load_rgba_array
		arr = load_rgba_array(params["source"])
		return {"preview": arr, "status": "{0}x{1}".format(arr.shape[1], arr.shape[0])}
