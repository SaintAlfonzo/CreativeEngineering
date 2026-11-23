# SPDX-License-Identifier: GPL-3.0-only
"""Collapsible sidebar sections (owner, 2026-11-22, plan_texture_tools_gui.md R10).

SectionPanel(state=None)    the left sidebar's control column (ToolScreen.controls). begin_section(title, is_open)
                            starts a Section; every add_widget after it lands in that section's body, so a screen's
                            build_controls code is unchanged: add_heading("Mapping") then its controls. Widgets added
                            before the first section stay above all sections, always shown.
	sections            {title: Section}, in build order
	expand_all() / collapse_all()
	state               object with get_open(title) -> bool|None and set_open(title, bool), or None (nothing kept)
Section(title, is_open)     header button ("-" open, "+" folded) over a body; a click toggles it. Folding detaches the
                            body (its widgets keep their state and params, they are only out of the widget tree, so a
                            desktop drop never lands on a folded slot). Section.text is the header's markup and may be
                            changed (combine's "Selected layer 2"); Section.title stays the state key.
"""
from kivy.properties import BooleanProperty, StringProperty
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button

HEADER_H = 30


class Section(BoxLayout):
	text = StringProperty("")
	is_open = BooleanProperty(True)

	def __init__(self, title, is_open=True, on_toggle=None, **kw):
		super().__init__(orientation="vertical", size_hint_y=None, spacing=2, **kw)
		self.bind(minimum_height=self.setter("height"))
		self.title = title
		self._on_toggle = on_toggle
		self.header = Button(markup=True, size_hint_y=None, height=HEADER_H, halign="left", valign="middle")
		self.header.bind(size=lambda inst, *a: setattr(inst, "text_size", (inst.width - 12, inst.height)))
		self.header.bind(on_release=lambda *a: self.toggle())
		self.body = BoxLayout(orientation="vertical", size_hint_y=None, spacing=4, padding=(8, 2, 0, 6))
		self.body.bind(minimum_height=self.body.setter("height"))
		super().add_widget(self.header)
		self.bind(text=lambda *a: self._label(), is_open=lambda *a: self._apply())
		self.is_open = bool(is_open)
		self.text = "[b]{0}[/b]".format(title)
		self._apply()

	def _label(self):
		self.header.text = "{0}  {1}".format("-" if self.is_open else "+", self.text)

	def _apply(self):
		if self.is_open and self.body.parent is None:
			super().add_widget(self.body)
		elif not self.is_open and self.body.parent is self:
			super().remove_widget(self.body)
		self._label()

	def toggle(self):
		self.set_open(not self.is_open)

	def set_open(self, value):
		value = bool(value)
		if value == self.is_open:
			return
		self.is_open = value
		if self._on_toggle is not None:
			self._on_toggle(self.title, value)

	def add_widget(self, widget, *a, **kw):
		self.body.add_widget(widget, *a, **kw)

	def remove_widget(self, widget, *a, **kw):
		self.body.remove_widget(widget, *a, **kw)


class SectionPanel(BoxLayout):
	def __init__(self, state=None, **kw):
		kw.setdefault("spacing", 4)
		super().__init__(orientation="vertical", size_hint_y=None, **kw)
		self.bind(minimum_height=self.setter("height"))
		self.state = state
		self.sections = {}
		self.current = None

	def begin_section(self, title, is_open=None):
		"""New section; is_open None = the stored state, else open only for the first section."""
		stored = self.state.get_open(title) if self.state is not None else None
		if stored is not None:
			is_open = stored
		elif is_open is None:
			is_open = not self.sections
		sec = Section(title, is_open=is_open, on_toggle=self._toggled)
		self.sections[title] = sec
		super().add_widget(sec)
		self.current = sec
		return sec

	def _toggled(self, title, value):
		if self.state is not None:
			self.state.set_open(title, value)

	def add_widget(self, widget, *a, **kw):
		if self.current is not None:
			self.current.add_widget(widget, *a, **kw)
		else:
			super().add_widget(widget, *a, **kw)

	def expand_all(self):
		for sec in self.sections.values():
			sec.set_open(True)

	def collapse_all(self):
		for sec in self.sections.values():
			sec.set_open(False)


class SettingsSectionState:
	"""Section open state per screen in the Settings store: key 'open_sections' {tool: {title: bool}}."""

	def __init__(self, settings, tool):
		self.settings = settings
		self.tool = tool

	def get_open(self, title):
		if self.settings is None:
			return None
		v = (self.settings.get("open_sections") or {}).get(self.tool, {}).get(title)
		return None if v is None else bool(v)

	def set_open(self, title, value):
		if self.settings is None:
			return
		data = dict(self.settings.get("open_sections") or {})
		tool = dict(data.get(self.tool) or {})
		tool[title] = bool(value)
		data[self.tool] = tool
		try:
			self.settings.set("open_sections", data)
		except OSError:
			pass
