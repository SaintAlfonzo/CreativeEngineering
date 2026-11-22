# SPDX-License-Identifier: GPL-3.0-only
"""Editor color panel (plan_texture_tools_gui.md section 6, P7). Every color it shows comes from core/colors.py.

ColorPanel(on_color=None, settings=None, on_status=None)
	color                 current (r, g, b, a) ints; set_color(c, source=None) updates every control and calls
	                      on_color(c) (source = the control that changed it, so it is not rewritten)
	swatch, hex_input     big swatch and '#rrggbb[aa]' field (Enter applies)
	sliders               {'r','g','b','a': Slider 0-255}; hue slider + SVBox (saturation x, value y) for HSV
	nudge(how)            core.colors.nudge(color, how, step_percent / 100); nudge_buttons {how: Button}
	step_percent          nudge step knob (default 5)
	recent                last 16 used colors, newest first; add_recent(c) (the editor calls it on every stroke)
	slots                 32 entries [hex or '', name]; click a filled slot = use it, click an empty slot or
	                      shift-click any = store the current color; slot_buttons; store_slot(i, color, name);
	                      rename_slot(i, name); clear_slot(i); select_slot(i)
	add_image_colors(colors)   fill empty slots with colors not already saved; returns (added, left out)
	load_slots(entries)   replace all slots from [(color, name)] (palette import / load palette from image)
	slot_entries()        [(rgba, name)] of the filled slots, for export
	delta                 autosuggest delta knob (default 24); suggest_buttons {axis: [Button]} for axes
	                      r, g, b, white, black and 'harmony' (complementary, triadic, analogous); click sets the
	                      color, shift-click saves it to the first empty slot. Button.tt_color holds each color.
Persisted in the Settings store: editor_slots, editor_recent, editor_nudge_step, editor_suggest_delta.
Shift is read from Window.modifiers at the click.
"""
import numpy as np
from kivy.core.window import Window
from kivy.graphics import Color, Line, Rectangle
from kivy.graphics.texture import Texture
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.slider import Slider
from kivy.uix.textinput import TextInput
from kivy.uix.widget import Widget

from ...core import colors as C
from .swatches import ColorBox

N_SLOTS = 32
N_RECENT = 16
SUGGEST_ROWS = (("r", "R"), ("g", "G"), ("b", "B"), ("white", "W"), ("black", "K"))
NUDGE_LABELS = (("lighter", "Lighter"), ("darker", "Darker"), ("saturate", "Sat +"), ("desaturate", "Sat -"),
	("hue_left", "Hue <"), ("hue_right", "Hue >"))
ROW = 28


def shift_held():
	return "shift" in (Window.modifiers or [])


def _rgba(c):
	c = tuple(int(v) for v in c)
	return c + (255,) if len(c) == 3 else c


def _bg(c):
	c = _rgba(c)
	return [c[0] / 255.0, c[1] / 255.0, c[2] / 255.0, 1]


def color_button(color, on_press, text=""):
	b = Button(text=text, background_normal="", background_down="", font_size="10sp")
	b.tt_color = None
	set_button_color(b, color)
	b.bind(on_release=lambda inst: on_press(inst))
	return b


def set_button_color(b, color):
	if color is None:
		b.tt_color = None
		b.background_color = [0.22, 0.22, 0.24, 1]
	else:
		b.tt_color = _rgba(color)
		b.background_color = _bg(color)
		lum = C.brightness(b.tt_color[:3])
		b.color = [0, 0, 0, 1] if lum > 140 else [1, 1, 1, 1]


def _label(text, width=None, **kw):
	lbl = Label(text=text, size_hint_x=None if width else 1, width=width or 100, halign="left", valign="middle",
		font_size="12sp", **kw)
	lbl.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
	return lbl


class SVBox(Widget):
	"""Saturation (x) by value (y) square at a hue; on_pick(s, v) on press and drag."""

	def __init__(self, on_pick=None, **kw):
		kw.setdefault("size_hint_y", None)
		kw.setdefault("height", 150)
		super().__init__(**kw)
		self.on_pick = on_pick
		self.hue = 0.0
		self.sv = (0.0, 0.0)
		self._tex = Texture.create(size=(32, 32), colorfmt="rgba")
		with self.canvas:
			Color(1, 1, 1, 1)
			self._rect = Rectangle(texture=self._tex)
			Color(1, 1, 1, 1)
			self._mark = Line(rectangle=(0, 0, 6, 6), width=1.2)
			Color(0, 0, 0, 1)
			self._mark2 = Line(rectangle=(0, 0, 8, 8), width=1)
		self.bind(pos=self._redraw, size=self._redraw)
		self.set_hue(0.0)

	def set_hue(self, h):
		self.hue = float(h)
		s = np.linspace(0, 1, 32)
		v = np.linspace(0, 1, 32)
		ss, vv = np.meshgrid(s, v)  # row 0 = value 0 (Kivy textures are bottom-up)
		hsv = np.stack([np.full_like(ss, self.hue), ss, vv], axis=-1)
		rgb = np.clip(np.round(C.hsv_to_rgb_arr(hsv)), 0, 255).astype(np.uint8)
		buf = np.concatenate([rgb, np.full(rgb.shape[:2] + (1,), 255, np.uint8)], axis=-1)
		self._tex.blit_buffer(np.ascontiguousarray(buf).tobytes(), colorfmt="rgba", bufferfmt="ubyte")
		self._rect.texture = self._tex
		self.canvas.ask_update()

	def set_sv(self, s, v):
		self.sv = (float(s), float(v))
		self._redraw()

	def _redraw(self, *a):
		self._rect.pos = self.pos
		self._rect.size = self.size
		x = self.x + self.sv[0] * self.width
		y = self.y + self.sv[1] * self.height
		self._mark.rectangle = (x - 3, y - 3, 6, 6)
		self._mark2.rectangle = (x - 4, y - 4, 8, 8)

	def _pick(self, touch):
		s = min(1.0, max(0.0, (touch.x - self.x) / max(1.0, self.width)))
		v = min(1.0, max(0.0, (touch.y - self.y) / max(1.0, self.height)))
		self.set_sv(s, v)
		if self.on_pick is not None:
			self.on_pick(s, v)

	def on_touch_down(self, touch):
		if not self.collide_point(*touch.pos) or getattr(touch, "button", "left") != "left":
			return super().on_touch_down(touch)
		touch.grab(self)
		self._pick(touch)
		return True

	def on_touch_move(self, touch):
		if touch.grab_current is self:
			self._pick(touch)
			return True
		return super().on_touch_move(touch)

	def on_touch_up(self, touch):
		if touch.grab_current is self:
			touch.ungrab(self)
			return True
		return super().on_touch_up(touch)


class ColorPanel(BoxLayout):
	def __init__(self, on_color=None, settings=None, on_status=None, **kw):
		kw.setdefault("size_hint_y", None)
		super().__init__(orientation="vertical", spacing=4, **kw)
		self.bind(minimum_height=self.setter("height"))
		self.on_color = on_color
		self.on_status = on_status
		self.settings = settings
		self.color = (0, 0, 0, 255)
		self._hue = 0.0
		self._quiet = False
		self.recent = []
		self.slots = [["", ""] for _ in range(N_SLOTS)]
		self.selected_slot = 0
		self.step_percent = 5.0
		self.delta = 24
		self._load_settings()
		self._build()
		self.set_color(self.color)

	# ---- settings ----

	def _get(self, key, default):
		if self.settings is None:
			return default
		v = self.settings.get(key)
		return default if v is None else v

	def _set(self, key, value):
		if self.settings is not None:
			try:
				self.settings.set(key, value)
			except OSError as e:
				self._status("settings not saved: {0}".format(e))

	def _load_settings(self):
		slots = self._get("editor_slots", [])
		for i, e in enumerate(slots[:N_SLOTS] if isinstance(slots, list) else []):
			if isinstance(e, (list, tuple)) and len(e) == 2:
				try:
					self.slots[i] = [C.to_hex(C.from_hex(e[0])) if e[0] else "", str(e[1] or "")]
				except ValueError:
					pass
		rec = self._get("editor_recent", [])
		self.recent = []
		for h in rec if isinstance(rec, list) else []:
			try:
				self.recent.append(_rgba(C.from_hex(h)))
			except (ValueError, TypeError, AttributeError):
				pass
		self.recent = self.recent[:N_RECENT]
		try:
			self.step_percent = float(self._get("editor_nudge_step", 5.0))
			self.delta = int(self._get("editor_suggest_delta", 24))
		except (TypeError, ValueError):
			pass

	def _save_slots(self):
		self._set("editor_slots", [list(e) for e in self.slots])

	def _status(self, text):
		if self.on_status is not None:
			self.on_status(text)

	# ---- layout ----

	def _heading(self, text):
		lbl = _label("[b]{0}[/b]".format(text), markup=True, size_hint_y=None, height=24)
		self.add_widget(lbl)

	def _build(self):
		self._heading("Color")
		top = BoxLayout(size_hint_y=None, height=56, spacing=6)
		self.swatch = ColorBox(self.color, size=(56, 56))
		top.add_widget(self.swatch)
		col = BoxLayout(orientation="vertical", spacing=2)
		self.hex_input = TextInput(multiline=False, size_hint_y=None, height=ROW, font_size="12sp")
		self.hex_input.bind(on_text_validate=lambda *a: self.apply_hex(self.hex_input.text))
		col.add_widget(self.hex_input)
		self.info = _label("", size_hint_y=None, height=24)
		col.add_widget(self.info)
		top.add_widget(col)
		self.add_widget(top)

		self.sliders = {}
		self.slider_labels = {}
		for ch in ("r", "g", "b", "a"):
			row = BoxLayout(size_hint_y=None, height=ROW, spacing=4)
			row.add_widget(_label(ch.upper(), 18))
			s = Slider(min=0, max=255, step=1, value=0)
			s.bind(value=lambda inst, v, ch=ch: self._slider(ch, v))
			lbl = _label("0", 34)
			row.add_widget(s)
			row.add_widget(lbl)
			self.sliders[ch] = s
			self.slider_labels[ch] = lbl
			self.add_widget(row)

		self.svbox = SVBox(on_pick=self._sv_pick)
		self.add_widget(self.svbox)
		row = BoxLayout(size_hint_y=None, height=ROW, spacing=4)
		row.add_widget(_label("H", 18))
		self.hue_slider = Slider(min=0, max=359, step=1, value=0)
		self.hue_slider.bind(value=lambda inst, v: self._hue_slide(v))
		row.add_widget(self.hue_slider)
		self.add_widget(row)

		self._heading("Nudge")
		grid = GridLayout(cols=3, size_hint_y=None, height=2 * ROW + 2, spacing=2)
		self.nudge_buttons = {}
		for how, text in NUDGE_LABELS:
			b = Button(text=text, font_size="12sp")
			b.bind(on_release=lambda inst, how=how: self.nudge(how))
			self.nudge_buttons[how] = b
			grid.add_widget(b)
		self.add_widget(grid)
		row = BoxLayout(size_hint_y=None, height=ROW, spacing=4)
		row.add_widget(_label("Step %", 50))
		self.step_slider = Slider(min=1, max=50, step=1, value=self.step_percent)
		self.step_label = _label("{0:g}".format(self.step_percent), 34)
		self.step_slider.bind(value=lambda inst, v: self.set_step(v))
		row.add_widget(self.step_slider)
		row.add_widget(self.step_label)
		self.add_widget(row)

		self._heading("Recent")
		self.recent_grid = GridLayout(cols=8, size_hint_y=None, height=2 * 22 + 2, spacing=2, row_default_height=22,
			row_force_default=True)
		self.recent_buttons = []
		for i in range(N_RECENT):
			b = color_button(None, lambda inst: inst.tt_color and self.set_color(inst.tt_color))
			self.recent_buttons.append(b)
			self.recent_grid.add_widget(b)
		self.add_widget(self.recent_grid)

		self._heading("Saved slots")
		self.slot_grid = GridLayout(cols=8, size_hint_y=None, height=4 * 26 + 6, spacing=2, row_default_height=26,
			row_force_default=True)
		self.slot_buttons = []
		for i in range(N_SLOTS):
			b = color_button(None, lambda inst, i=i: self._slot_click(i))
			self.slot_buttons.append(b)
			self.slot_grid.add_widget(b)
		self.add_widget(self.slot_grid)
		row = BoxLayout(size_hint_y=None, height=ROW, spacing=4)
		self.slot_label = _label("slot 1", 52)
		self.slot_name = TextInput(multiline=False, hint_text="slot name", font_size="12sp")
		self.slot_name.bind(on_text_validate=lambda *a: self.rename_slot(self.selected_slot, self.slot_name.text))
		for w in (self.slot_label, self.slot_name):
			row.add_widget(w)
		for text, fn in (("Name", lambda: self.rename_slot(self.selected_slot, self.slot_name.text)),
				("Store", lambda: self.store_slot(self.selected_slot, self.color)),
				("Clear", lambda: self.clear_slot(self.selected_slot))):
			b = Button(text=text, size_hint_x=None, width=48, font_size="12sp")
			b.bind(on_release=lambda inst, fn=fn: fn())
			row.add_widget(b)
		self.add_widget(row)
		# palette file and image buttons are added by the editor (they need the file browser and the image)
		self.slot_actions = GridLayout(cols=2, size_hint_y=None, height=2 * ROW + 2, spacing=2)
		self.add_widget(self.slot_actions)

		self._heading("Suggest")
		row = BoxLayout(size_hint_y=None, height=ROW, spacing=4)
		row.add_widget(_label("Delta", 40))
		self.delta_slider = Slider(min=1, max=128, step=1, value=self.delta)
		self.delta_label = _label(str(self.delta), 34)
		self.delta_slider.bind(value=lambda inst, v: self.set_delta(v))
		row.add_widget(self.delta_slider)
		row.add_widget(self.delta_label)
		self.add_widget(row)
		self.suggest_buttons = {}
		for axis, text in SUGGEST_ROWS + (("harmony", "Hm"),):
			row = BoxLayout(size_hint_y=None, height=24, spacing=2)
			row.add_widget(_label(text, 22))
			btns = []
			for i in range(6 if axis != "harmony" else 5):
				b = color_button(None, lambda inst: self._suggest_click(inst))
				btns.append(b)
				row.add_widget(b)
			if axis == "harmony":
				row.add_widget(Widget())
			self.suggest_buttons[axis] = btns
			self.add_widget(row)
		hint = _label("click sets, shift-click saves to a slot; Hm = complementary, triadic x2, analogous x2",
			size_hint_y=None, height=30)
		hint.font_size = "10sp"
		self.add_widget(hint)
		self._refresh_recent()
		self._refresh_slots()

	def add_slot_action(self, text, fn):
		b = Button(text=text, font_size="12sp")
		b.bind(on_release=lambda inst: fn())
		self.slot_actions.add_widget(b)
		return b

	# ---- color ----

	def set_color(self, c, source=None):
		c = C.clamp(_rgba(c))
		self.color = c
		h, s, v = C.rgb_to_hsv(c[:3])
		if s > 0 and v > 0 and source not in ("hue", "sv"):
			self._hue = h
		self._quiet = True
		try:
			self.swatch.set_color(c)
			if source != "hex":
				self.hex_input.text = C.to_hex(c)
			for ch, k in (("r", 0), ("g", 1), ("b", 2), ("a", 3)):
				if source != ch:
					self.sliders[ch].value = c[k]
				self.slider_labels[ch].text = str(c[k])
			if source != "hue":
				self.hue_slider.value = int(round(self._hue)) % 360
			self.svbox.set_hue(self._hue)
			if source != "sv":
				self.svbox.set_sv(s, v)
		finally:
			self._quiet = False
		hh, ss, ll = C.rgb_to_hsl(c[:3])
		self.info.text = "HSV {0:.0f} {1:.0f}% {2:.0f}%  L {3:.0f}%".format(h, s * 100, v * 100, ll * 100)
		self._refresh_suggest()
		if self.on_color is not None:
			self.on_color(c)
		return c

	def apply_hex(self, text):
		try:
			c = C.from_hex(text)
		except ValueError as e:
			self._status(str(e))
			return None
		return self.set_color(c, source="hex")

	def _slider(self, ch, v):
		if self._quiet:
			return
		c = list(self.color)
		c[{"r": 0, "g": 1, "b": 2, "a": 3}[ch]] = int(v)
		self.set_color(c, source=ch)

	def _hsv_color(self, h, s, v):
		return C.hsv_to_rgb(h, s, v) + (self.color[3],)

	def _sv_pick(self, s, v):
		self.svbox.set_sv(s, v)
		self.set_color(self._hsv_color(self._hue, s, v), source="sv")

	def _hue_slide(self, h):
		if self._quiet:
			return
		self._hue = float(h)
		s, v = self.svbox.sv
		self.set_color(self._hsv_color(self._hue, s, v), source="hue")

	def nudge(self, how):
		return self.set_color(C.nudge(self.color, how, self.step_percent / 100.0))

	def set_step(self, v):
		self.step_percent = float(v)
		self.step_label.text = "{0:g}".format(self.step_percent)
		if self.step_slider.value != v:
			self.step_slider.value = v
		self._set("editor_nudge_step", self.step_percent)

	def set_delta(self, v):
		self.delta = int(v)
		self.delta_label.text = str(self.delta)
		if self.delta_slider.value != v:
			self.delta_slider.value = v
		self._set("editor_suggest_delta", self.delta)
		self._refresh_suggest()

	# ---- recent ----

	def add_recent(self, c):
		c = _rgba(c)
		if self.recent and self.recent[0] == c:
			return
		self.recent = [c] + [r for r in self.recent if r != c]
		self.recent = self.recent[:N_RECENT]
		self._refresh_recent()
		self._set("editor_recent", [C.to_hex(r) for r in self.recent])

	def _refresh_recent(self):
		for i, b in enumerate(self.recent_buttons):
			set_button_color(b, self.recent[i] if i < len(self.recent) else None)

	# ---- slots ----

	def slot_color(self, i):
		h = self.slots[i][0]
		return _rgba(C.from_hex(h)) if h else None

	def _refresh_slots(self):
		for i, b in enumerate(self.slot_buttons):
			c = self.slot_color(i)
			set_button_color(b, c)
			name = self.slots[i][1]
			mark = ">" if i == self.selected_slot else ""
			b.text = (mark + (name[:5] if name else "")) if (mark or name) else ""
		self.slot_label.text = "slot {0}".format(self.selected_slot + 1)

	def select_slot(self, i):
		self.selected_slot = i
		self.slot_name.text = self.slots[i][1]
		self._refresh_slots()

	def _slot_click(self, i):
		self.select_slot(i)
		if shift_held() or not self.slots[i][0]:
			self.store_slot(i, self.color)
		else:
			self.set_color(self.slot_color(i))

	def store_slot(self, i, color, name=None):
		self.slots[i][0] = C.to_hex(_rgba(color))
		if name is not None:
			self.slots[i][1] = name
		self._save_slots()
		self._refresh_slots()
		self._status("saved {0} to slot {1}".format(self.slots[i][0], i + 1))

	def rename_slot(self, i, name):
		self.slots[i][1] = (name or "").strip()
		self._save_slots()
		self._refresh_slots()

	def clear_slot(self, i):
		self.slots[i] = ["", ""]
		self._save_slots()
		self._refresh_slots()

	def first_empty_slot(self):
		for i, (h, _) in enumerate(self.slots):
			if not h:
				return i
		return None

	def save_to_free_slot(self, c):
		i = self.first_empty_slot()
		if i is None:
			self._status("all {0} slots are full".format(N_SLOTS))
			return None
		self.store_slot(i, c)
		return i

	def add_image_colors(self, colors):
		have = set(h for h, _ in self.slots if h)
		added = 0
		left = 0
		for c in colors:
			h = C.to_hex(_rgba(c))
			if h in have:
				continue
			i = self.first_empty_slot()
			if i is None:
				left += 1
				continue
			self.slots[i] = [h, ""]
			have.add(h)
			added += 1
		self._save_slots()
		self._refresh_slots()
		return added, left

	def load_slots(self, entries):
		entries = list(entries)
		self.slots = [["", ""] for _ in range(N_SLOTS)]
		for i, (c, name) in enumerate(entries[:N_SLOTS]):
			self.slots[i] = [C.to_hex(_rgba(c)), name or ""]
		self._save_slots()
		self.select_slot(0)
		return min(len(entries), N_SLOTS), max(0, len(entries) - N_SLOTS)

	def slot_entries(self):
		return [(self.slot_color(i), n) for i, (h, n) in enumerate(self.slots) if h]

	# ---- suggest ----

	def _refresh_suggest(self):
		if not hasattr(self, "suggest_buttons"):
			return
		rows = C.suggest(self.color, self.delta, 6)
		hm = C.harmonies(self.color)
		rows["harmony"] = hm["complementary"] + hm["triadic"] + hm["analogous"]
		for axis, btns in self.suggest_buttons.items():
			prev = self.color
			for b, c in zip(btns, rows[axis]):
				set_button_color(b, c)
				b.opacity = 0.35 if (c == self.color or c == prev) else 1
				prev = c

	def _suggest_click(self, b):
		if b.tt_color is None:
			return
		if shift_held():
			self.save_to_free_slot(b.tt_color)
		else:
			self.set_color(b.tt_color)
