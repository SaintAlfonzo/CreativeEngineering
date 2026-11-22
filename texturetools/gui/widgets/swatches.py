# SPDX-License-Identifier: GPL-3.0-only
"""Color swatch widgets for the tool screens (plan_texture_tools_gui.md section 3, P6).

ColorBox(rgba)                    a flat color square (0-255 values) with a thin border
SwatchList(title, on_change)      every color of an image with a checkbox; set_colors(colors), set_checked(hexes),
                                  checked() -> list of '#rrggbb'; on_change(checked list) on a user click. Used for
                                  the palette exclusion list and the source keep list.
SwatchStrip(on_change)            a hand-built palette: '#rrggbb' field + Add, a click on a swatch removes it;
                                  set_colors(hexes), colors; on_change(colors) on every user edit.
hex_of(color) -> '#rrggbb'
"""
from kivy.graphics import Color, Line, Rectangle
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.checkbox import CheckBox
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.widget import Widget

MAX_SWATCHES = 256
ROW_H = 26


def hex_of(color):
	if isinstance(color, str):
		from ...core.palette import parse_color
		color = parse_color(color)
	return "#{0:02x}{1:02x}{2:02x}".format(int(color[0]), int(color[1]), int(color[2]))


class ColorBox(Widget):
	def __init__(self, rgba=(0, 0, 0, 255), **kw):
		kw.setdefault("size_hint", (None, None))
		kw.setdefault("size", (20, 20))
		super().__init__(**kw)
		with self.canvas:
			Color(0.5, 0.5, 0.5, 1)
			self._border = Line(rectangle=(0, 0, 1, 1), width=1)
			self._color = Color(1, 1, 1, 1)
			self._rect = Rectangle()
		self.set_color(rgba)
		self.bind(pos=self._redraw, size=self._redraw)
		self._redraw()

	def set_color(self, rgba):
		c = tuple(int(v) for v in rgba) + (255,) * (4 - len(rgba))
		self._color.rgba = (c[0] / 255.0, c[1] / 255.0, c[2] / 255.0, max(c[3], 40) / 255.0)

	def _redraw(self, *a):
		self._rect.pos = (self.x + 1, self.y + 1)
		self._rect.size = (max(0, self.width - 2), max(0, self.height - 2))
		self._border.rectangle = (self.x, self.y, self.width, self.height)


def _vcenter(widget):
	box = BoxLayout(size_hint=(None, 1), width=widget.width + 4, padding=(2, 3))
	box.add_widget(widget)
	return box


class SwatchList(BoxLayout):
	"""Colors with checkboxes (checked = on the list)."""

	def __init__(self, title="", on_change=None, cols=3, **kw):
		kw.setdefault("size_hint_y", None)
		super().__init__(orientation="vertical", spacing=2, **kw)
		self.on_change = on_change
		self._title = Label(text=title, size_hint_y=None, height=36, halign="left", valign="middle", font_size="12sp")
		self._title.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		self.add_widget(self._title)
		self.grid = GridLayout(cols=cols, size_hint_y=None, height=0, spacing=2, row_default_height=ROW_H,
			row_force_default=True)
		self.grid.bind(minimum_height=self.grid.setter("height"))
		self.add_widget(self.grid)
		self.grid.bind(height=self._resize)
		self.colors = []
		self.boxes = {}
		self._checked = set()
		self._quiet = False
		self._title_text = title
		self._resize()

	def _resize(self, *a):
		self.height = self._title.height + self.grid.height + 2

	def set_colors(self, colors):
		"""colors: iterable of RGB(A) tuples or hex strings (darkest first is fine; order is kept)."""
		hexes, seen = [], set()
		for c in colors or ():
			h = hex_of(c)
			if h not in seen:
				seen.add(h)
				hexes.append((h, c if not isinstance(c, str) else None))
		over = len(hexes) > MAX_SWATCHES
		hexes = hexes[:MAX_SWATCHES]
		self.grid.clear_widgets()
		self.boxes = {}
		self.colors = [h for h, _ in hexes]
		for h, c in hexes:
			rgba = c if c is not None else tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))
			cell = BoxLayout(spacing=2)
			box = CheckBox(active=h in self._checked, size_hint_x=None, width=24)
			box.bind(active=lambda inst, v, h=h: self._clicked(h, v))
			cell.add_widget(box)
			cell.add_widget(_vcenter(ColorBox(rgba)))
			lbl = Label(text=h, font_size="11sp", halign="left", valign="middle")
			lbl.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
			cell.add_widget(lbl)
			self.grid.add_widget(cell)
			self.boxes[h] = box
		self._title.text = "{0} ({1}{2} colors)".format(self._title_text, len(hexes), "+" if over else "")

	def set_checked(self, hexes):
		self._checked = set(hex_of(h) for h in hexes or ())
		self._quiet = True
		try:
			for h, box in self.boxes.items():
				box.active = h in self._checked
		finally:
			self._quiet = False

	def checked(self):
		"""Checked colors: the shown ones in list order, then checked colors not in the current image."""
		shown = [h for h in self.colors if h in self._checked]
		return shown + sorted(self._checked - set(self.colors))

	def toggle(self, color):
		"""Eyedropper: flip one color (also when it is not shown) and report the change."""
		h = hex_of(color)
		if h in self._checked:
			self._checked.discard(h)
		else:
			self._checked.add(h)
		self.set_checked(self._checked)
		if self.on_change is not None:
			self.on_change(self.checked())

	def _clicked(self, h, v):
		if self._quiet:
			return
		if v:
			self._checked.add(h)
		else:
			self._checked.discard(h)
		if self.on_change is not None:
			self.on_change(self.checked())


class SwatchStrip(BoxLayout):
	"""A hand-built palette strip."""

	def __init__(self, on_change=None, **kw):
		kw.setdefault("size_hint_y", None)
		super().__init__(orientation="vertical", spacing=2, **kw)
		self.on_change = on_change
		self.colors = []
		row = BoxLayout(size_hint_y=None, height=30, spacing=4)
		self.field = TextInput(multiline=False, hint_text="#rrggbb (comma separated for several)")
		self.field.bind(on_text_validate=lambda *a: self.add_text(self.field.text))
		add = Button(text="Add", size_hint_x=None, width=50)
		add.bind(on_release=lambda *a: self.add_text(self.field.text))
		clear = Button(text="Clear", size_hint_x=None, width=56)
		clear.bind(on_release=lambda *a: self._user_set([]))
		for w in (self.field, add, clear):
			row.add_widget(w)
		self.add_widget(row)
		self.grid = GridLayout(cols=8, size_hint_y=None, height=0, spacing=2, row_default_height=28,
			row_force_default=True)
		self.grid.bind(minimum_height=self.grid.setter("height"))
		self.grid.bind(height=lambda *a: self._resize())
		self.add_widget(self.grid)
		self.error = Label(text="", size_hint_y=None, height=18, font_size="11sp")
		self.add_widget(self.error)
		self._resize()

	def _resize(self):
		self.height = 30 + self.grid.height + 18 + 6

	def add_text(self, text):
		from ...core.palette import parse_color
		new = list(self.colors)
		for part in (text or "").replace(";", ",").split(","):
			part = part.strip()
			if not part:
				continue
			try:
				new.append(hex_of(parse_color(part)))
			except ValueError as e:
				self.error.text = str(e)
				return
		self.error.text = ""
		self.field.text = ""
		self._user_set(new)

	def _user_set(self, colors):
		self.set_colors(colors)
		if self.on_change is not None:
			self.on_change(list(self.colors))

	def set_colors(self, colors):
		self.colors = [hex_of(c) for c in colors or ()][:MAX_SWATCHES]
		self.grid.clear_widgets()
		for i, h in enumerate(self.colors):
			rgb = [int(h[k:k + 2], 16) / 255.0 for k in (1, 3, 5)]
			b = Button(text="", background_normal="", background_color=rgb + [1])
			b.bind(on_release=lambda inst, i=i: self._remove(i))
			self.grid.add_widget(b)

	def _remove(self, i):
		new = list(self.colors)
		del new[i]
		self._user_set(new)
