# SPDX-License-Identifier: GPL-3.0-only
"""Dev-mode provenance offer after Run / Save as (plan_texture_tools_gui.md section 8, P6).

offer_provenance(app, screen, params, outputs) opens a popup with the suggested sources.csv row (pattern, source, tool,
used for; all editable). "Append row" is the only path that writes (core/provenance.append_row); "Not now" closes
without writing. After an append the popup shows the reminder to run tools/texture_provenance.py --write.
The csv is settings "provenance_csv", else the sources.csv found above the first output, the project folder or this
package; outside the pack repo there is none and only a status line says so.
"""
import os

from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.popup import Popup
from kivy.uix.textinput import TextInput

from ..core import provenance

FIELDS = (("pattern", "Pattern (output)"), ("source", "Source"), ("tool", "Tool (command)"), ("used_for", "Used for"))


def sources_csv_for(settings, outputs):
	explicit = settings.get("provenance_csv") if settings is not None else ""
	if explicit:
		return explicit if os.path.isfile(explicit) else None
	pkg = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
	project = settings.project_folder if settings is not None else ""
	return provenance.find_sources_csv(*(list(outputs[:1]) + [project, pkg]))


class ProvenancePopup(Popup):
	def __init__(self, csv_path, row, on_status=None, **kw):
		self.csv_path = csv_path
		self.row = dict(row)
		self.on_status = on_status
		self.appended = None
		box = BoxLayout(orientation="vertical", spacing=4, padding=6)
		box.add_widget(Label(text="Dev mode: add a provenance row to\n{0}".format(csv_path), size_hint_y=None,
			height=44))
		self.inputs = {}
		for key, label in FIELDS:
			row_box = BoxLayout(size_hint_y=None, height=34, spacing=4)
			row_box.add_widget(Label(text=label, size_hint_x=None, width=130))
			ti = TextInput(text=self.row.get(key, ""), multiline=False)
			self.inputs[key] = ti
			row_box.add_widget(ti)
			box.add_widget(row_box)
		box.add_widget(Label(text="origin owned-tool, status confirm (look-check pending)", size_hint_y=None,
			height=24, font_size="12sp"))
		self.message = Label(text="", size_hint_y=None, height=44)
		box.add_widget(self.message)
		bar = BoxLayout(size_hint_y=None, height=36, spacing=4)
		self.append_button = Button(text="Append row")
		self.append_button.bind(on_release=lambda *a: self.confirm())
		self.close_button = Button(text="Not now")
		self.close_button.bind(on_release=lambda *a: self.dismiss())
		bar.add_widget(self.append_button)
		bar.add_widget(self.close_button)
		box.add_widget(bar)
		kw.setdefault("title", "Texture provenance")
		kw.setdefault("size_hint", (0.8, None))
		kw.setdefault("height", 420)
		kw.setdefault("auto_dismiss", False)
		super().__init__(content=box, **kw)

	def current_row(self):
		row = dict(self.row)
		for key, ti in self.inputs.items():
			row[key] = ti.text.strip()
		return row

	def confirm(self):
		"""The explicit user confirm: append the row, then show the reminder."""
		try:
			self.appended = provenance.append_row(self.csv_path, self.current_row())
		except (OSError, ValueError) as e:
			self.message.text = "not added: {0}".format(e)
			return False
		self.append_button.disabled = True
		self.close_button.text = "Close"
		self.message.text = "Row added.\n" + provenance.REMINDER
		if self.on_status is not None:
			self.on_status("provenance row added. " + provenance.REMINDER)
		return True


def offer_provenance(app, screen, params, outputs):
	settings = getattr(app, "settings", None)
	if settings is None or not settings.dev_mode or not outputs:
		return None
	csv_path = sources_csv_for(settings, outputs)
	if not csv_path:
		screen.set_status("{0} (dev mode: no sources.csv found; set the project folder to the pack repo)".format(
			screen.status.text))
		return None
	root = provenance.find_repo_root(csv_path)
	try:
		tool = screen.cli_string() or screen.title
	except Exception:  # a CLI builder failing must not block the offer
		tool = screen.title
	row = provenance.suggest_row(outputs, screen.provenance_inputs(params), tool, root=root)
	popup = ProvenancePopup(csv_path, row, on_status=screen.set_status)
	app.provenance_popup = popup
	popup.open()
	return popup
