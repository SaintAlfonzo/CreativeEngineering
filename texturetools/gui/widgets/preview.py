# SPDX-License-Identifier: GPL-3.0-only
"""Pixel preview: a numpy RGBA array shown at an integer zoom over a checkerboard (plan_texture_tools_gui.md section 8).

PixelPreview(Widget)
	set_array(arr, fit=None)  HxWx4 (or HxWx3) uint8 or float 0-255 (engine convention); None clears. Rows top-down.
	                          fit=True refits the zoom; default refits only when the image size changed.
	array                     the last array given (uint8 copy), or None (plain attribute)
	revision                  NumericProperty, +1 on every set_array (bind it to react to new content)
	zoom                      NumericProperty, integer 1-64; set_zoom(z, anchor=None) keeps the pixel under anchor
	pan                       ListProperty [x, y] in widget px, offset of the image from centered
	fit()                     largest integer zoom that fits (at least 1), pan reset
	pixel_at(x, y)            window coords -> (col, row) with row 0 at the top, or None outside the image
	on_pixel(col, row, button, kind)   event; kind 'down' | 'move' | 'up' for presses on the image with
	                          `pick_buttons` (default left); screens bind it for pickers and the editor canvas.
Input: wheel zooms about the cursor; drag with `pan_buttons` (default middle and right, plus left when left_pans) pans;
double click with a pan button fits. Textures use mag_filter and min_filter 'nearest', so pixels stay crisp at every zoom.
"""
import numpy as np
from kivy.graphics import Color, Rectangle
from kivy.graphics.texture import Texture
from kivy.properties import BooleanProperty, ListProperty, NumericProperty
from kivy.uix.stencilview import StencilView

ZOOM_MIN = 1
ZOOM_MAX = 64
CHECKER_PX = 8


def to_rgba_u8(arr):
	"""Engine array -> contiguous HxWx4 uint8 (clip, round down like save_raw)."""
	a = np.asarray(arr)
	if a.ndim == 2:
		a = np.stack([a, a, a, np.full_like(a, 255)], axis=-1)
	if a.shape[-1] == 3:
		a = np.concatenate([a, np.full(a.shape[:2] + (1,), 255, dtype=a.dtype)], axis=-1)
	if a.dtype != np.uint8:
		a = np.clip(a, 0, 255).astype(np.uint8)
	return np.ascontiguousarray(a)


def _checker_texture():
	tex = Texture.create(size=(2, 2), colorfmt="rgba")
	light, dark = (204, 204, 204, 255), (153, 153, 153, 255)
	buf = np.array([[light, dark], [dark, light]], dtype=np.uint8)
	tex.blit_buffer(buf.tobytes(), colorfmt="rgba", bufferfmt="ubyte")
	tex.mag_filter = "nearest"
	tex.min_filter = "nearest"
	tex.wrap = "repeat"
	return tex


class PixelPreview(StencilView):
	zoom = NumericProperty(1)
	pan = ListProperty([0, 0])
	left_pans = BooleanProperty(False)
	pan_buttons = ListProperty(["middle", "right"])
	pick_buttons = ListProperty(["left"])
	background = ListProperty([0.16, 0.16, 0.18, 1])
	revision = NumericProperty(0)

	__events__ = ("on_pixel",)

	def __init__(self, **kw):
		super().__init__(**kw)
		self._tex = None
		self._checker = _checker_texture()
		self._drag = None
		self.array = None
		with self.canvas:
			self._bg_color = Color(*self.background)
			self._bg = Rectangle(pos=self.pos, size=self.size)
			Color(1, 1, 1, 1)
			self._check_rect = Rectangle(texture=self._checker, size=(0, 0))
			self._img_rect = Rectangle(size=(0, 0))
		self.bind(pos=self._redraw, size=self._redraw, zoom=self._redraw, pan=self._redraw,
			background=lambda *a: setattr(self._bg_color, "rgba", self.background))

	# ---- data ----

	def set_array(self, arr, fit=None):
		if arr is None:
			self.array = None
			self._tex = None
			self._img_rect.texture = None
			self.revision += 1
			self._redraw()
			return
		a = to_rgba_u8(arr)
		h, w = a.shape[:2]
		old = None if self.array is None else self.array.shape[:2]
		if self._tex is None or self._tex.size != (w, h):
			self._tex = Texture.create(size=(w, h), colorfmt="rgba")
			self._tex.mag_filter = "nearest"
			self._tex.min_filter = "nearest"
		# Kivy textures are bottom-up; engine arrays are top-down.
		self._tex.blit_buffer(np.flipud(a).tobytes(), colorfmt="rgba", bufferfmt="ubyte")
		self._img_rect.texture = self._tex
		self.array = a
		self.revision += 1
		if fit or (fit is None and old != (h, w)):
			self.fit()
		else:
			self._redraw()

	@property
	def image_size(self):
		return (0, 0) if self.array is None else (self.array.shape[1], self.array.shape[0])

	# ---- view ----

	def fit(self):
		w, h = self.image_size
		if w and h and self.width > 0 and self.height > 0:
			z = int(min(self.width / w, self.height / h))
		else:
			z = 1
		self.pan = [0, 0]
		self.zoom = max(ZOOM_MIN, min(ZOOM_MAX, z))
		self._redraw()

	def set_zoom(self, z, anchor=None):
		"""Integer zoom, clamped; anchor (window coords) stays over the same image point."""
		z = int(max(ZOOM_MIN, min(ZOOM_MAX, round(z))))
		if z == self.zoom:
			return
		if anchor is not None and self.array is not None:
			ox, oy = self._origin()
			ax, ay = self.to_widget(*anchor)
			ix, iy = (ax - ox) / self.zoom, (ay - oy) / self.zoom
			w, h = self.image_size
			# new origin so (ix, iy) lands on anchor
			nox, noy = ax - ix * z, ay - iy * z
			cx = self.x + (self.width - w * z) / 2.0
			cy = self.y + (self.height - h * z) / 2.0
			self.pan = [nox - cx, noy - cy]
		self.zoom = z

	def zoom_in(self, anchor=None):
		self.set_zoom(self.zoom * 2 if self.zoom >= 2 else 2, anchor)

	def zoom_out(self, anchor=None):
		self.set_zoom(self.zoom // 2, anchor)

	def _origin(self):
		w, h = self.image_size
		z = self.zoom
		return (int(self.x + (self.width - w * z) / 2.0 + self.pan[0]),
			int(self.y + (self.height - h * z) / 2.0 + self.pan[1]))

	def _redraw(self, *a):
		self._bg.pos = self.pos
		self._bg.size = self.size
		w, h = self.image_size
		if not (w and h):
			self._check_rect.size = (0, 0)
			self._img_rect.size = (0, 0)
			return
		ox, oy = self._origin()
		sw, sh = w * self.zoom, h * self.zoom
		self._check_rect.pos = (ox, oy)
		self._check_rect.size = (sw, sh)
		self._check_rect.tex_coords = (0, 0, sw / (2.0 * CHECKER_PX), 0, sw / (2.0 * CHECKER_PX),
			sh / (2.0 * CHECKER_PX), 0, sh / (2.0 * CHECKER_PX))
		self._img_rect.pos = (ox, oy)
		self._img_rect.size = (sw, sh)

	def pixel_at(self, x, y):
		"""Window coords -> (col, row), row 0 at the top; None outside the image."""
		if self.array is None:
			return None
		wx, wy = self.to_widget(x, y)
		ox, oy = self._origin()
		w, h = self.image_size
		col = int((wx - ox) // self.zoom)
		row_from_bottom = int((wy - oy) // self.zoom)
		if 0 <= col < w and 0 <= row_from_bottom < h:
			return col, h - 1 - row_from_bottom
		return None

	# ---- input ----

	def _pans(self, button):
		return button in self.pan_buttons or (self.left_pans and button == "left")

	def on_touch_down(self, touch):
		if not self.collide_point(*touch.pos):
			return super().on_touch_down(touch)
		button = getattr(touch, "button", "left")
		if button in ("scrollup", "scrolldown"):
			# Kivy: scrollup = wheel toward the user on most mice; treat scrolldown as zoom in
			if button == "scrolldown":
				self.zoom_in(touch.pos)
			else:
				self.zoom_out(touch.pos)
			return True
		if touch.is_double_tap and self._pans(button):
			self.fit()
			return True
		if self._pans(button):
			touch.grab(self)
			self._drag = ("pan", touch.pos, list(self.pan))
			return True
		if button in self.pick_buttons:
			p = self.pixel_at(*touch.pos)
			touch.grab(self)
			self._drag = ("pick", button)
			if p is not None:
				self.dispatch("on_pixel", p[0], p[1], button, "down")
			return True
		return super().on_touch_down(touch)

	def on_touch_move(self, touch):
		if touch.grab_current is not self or self._drag is None:
			return super().on_touch_move(touch)
		if self._drag[0] == "pan":
			_, start, pan0 = self._drag
			self.pan = [pan0[0] + touch.pos[0] - start[0], pan0[1] + touch.pos[1] - start[1]]
		else:
			p = self.pixel_at(*touch.pos)
			if p is not None:
				self.dispatch("on_pixel", p[0], p[1], self._drag[1], "move")
		return True

	def on_touch_up(self, touch):
		if touch.grab_current is not self:
			return super().on_touch_up(touch)
		touch.ungrab(self)
		if self._drag is not None and self._drag[0] == "pick":
			p = self.pixel_at(*touch.pos)
			if p is not None:
				self.dispatch("on_pixel", p[0], p[1], self._drag[1], "up")
		self._drag = None
		return True

	def on_pixel(self, col, row, button, kind):
		pass
