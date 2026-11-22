# SPDX-License-Identifier: GPL-3.0-only
"""Small param controls for the left panel of a ToolScreen (plan_texture_tools_gui.md section 8).

Each control has a `key`, get_value(), set_value(v) and calls on_change(key, value) when the user changes it.
ToolScreen.add_number / add_toggle / add_choice / add_text create them and wire on_change to set_param.
"""
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.checkbox import CheckBox
from kivy.uix.label import Label
from kivy.uix.slider import Slider
from kivy.uix.spinner import Spinner
from kivy.uix.textinput import TextInput

ROW_H = 34


def _label(text):
	lbl = Label(text=text, size_hint_x=None, width=110, halign="right", valign="middle")
	lbl.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
	return lbl


class _Row(BoxLayout):
	def __init__(self, key, label, on_change, **kw):
		super().__init__(orientation="horizontal", size_hint_y=None, height=ROW_H, spacing=4, **kw)
		self.key = key
		self.on_change = on_change
		self._quiet = False
		self.add_widget(_label(label))

	def _changed(self, value):
		if not self._quiet and self.on_change is not None:
			self.on_change(self.key, value)

	def set_value(self, v):
		self._quiet = True
		try:
			self._set(v)
		finally:
			self._quiet = False


class NumberControl(_Row):
	"""Slider plus a text field; integer when step is an int."""

	def __init__(self, key, label, value, lo, hi, step=1, on_change=None):
		super().__init__(key, label, on_change)
		self.is_int = isinstance(step, int) and isinstance(lo, int) and isinstance(hi, int)
		self.slider = Slider(min=lo, max=hi, step=step, value=value)
		self.text = TextInput(text=self._fmt(value), multiline=False, size_hint_x=None, width=64)
		self.slider.bind(value=lambda inst, v: self._from_slider(v))
		self.text.bind(on_text_validate=lambda *a: self._from_text())
		self.add_widget(self.slider)
		self.add_widget(self.text)

	def _fmt(self, v):
		return str(int(v)) if self.is_int else "{0:g}".format(v)

	def _conv(self, v):
		return int(round(v)) if self.is_int else float(v)

	def _from_slider(self, v):
		self.text.text = self._fmt(v)
		self._changed(self._conv(v))

	def _from_text(self):
		try:
			v = float(self.text.text)
		except ValueError:
			self.text.text = self._fmt(self.slider.value)
			return
		v = max(self.slider.min, min(self.slider.max, v))
		self.slider.value = v

	def get_value(self):
		return self._conv(self.slider.value)

	def _set(self, v):
		self.slider.value = v
		self.text.text = self._fmt(v)


class ToggleControl(_Row):
	def __init__(self, key, label, value=False, on_change=None):
		super().__init__(key, label, on_change)
		self.box = CheckBox(active=bool(value), size_hint_x=None, width=40)
		self.box.bind(active=lambda inst, v: self._changed(bool(v)))
		self.add_widget(self.box)
		self.add_widget(Label())

	def get_value(self):
		return bool(self.box.active)

	def _set(self, v):
		self.box.active = bool(v)


class ChoiceControl(_Row):
	def __init__(self, key, label, value, values, on_change=None):
		super().__init__(key, label, on_change)
		self.spinner = Spinner(text=str(value), values=[str(v) for v in values])
		self.spinner.bind(text=lambda inst, v: self._changed(v))
		self.add_widget(self.spinner)

	def get_value(self):
		return self.spinner.text

	def _set(self, v):
		self.spinner.text = str(v)


class TextControl(_Row):
	def __init__(self, key, label, value="", on_change=None):
		super().__init__(key, label, on_change)
		self.input = TextInput(text=str(value), multiline=False)
		self.input.bind(on_text_validate=lambda *a: self._changed(self.input.text))
		self.input.bind(focus=lambda inst, f: (not f) and self._changed(self.input.text))
		self.add_widget(self.input)

	def get_value(self):
		return self.input.text

	def _set(self, v):
		self.input.text = "" if v is None else str(v)
