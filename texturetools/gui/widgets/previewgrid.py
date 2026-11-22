# SPDX-License-Identifier: GPL-3.0-only
"""Preview-all grid for bulk runs (plan_texture_tools_gui.md section 3 "Preview all", P6).

compute_job_previews(jobs, limit=MAX_TILES, progress=None)   WORKER THREAD, pure: runs each job's registered kind
	worker without saving; returns a list of dicts key (job.label), label, name (output name), array (or None) and
	reason (skip or error text).
PreviewGrid(on_open)   a scrolling grid of tiles: a small nearest-filtered preview, a tick box (ticked = written by
	Run), the output name. A left click on a tile calls on_open(item). set_items(items), unticked() -> set of keys,
	set_unticked(keys).
"""
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.checkbox import CheckBox
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.scrollview import ScrollView

from .preview import PixelPreview

MAX_TILES = 300
TILE_W = 132
TILE_H = 164


def compute_job_previews(jobs, limit=MAX_TILES, progress=None):
	from ...core import batch
	items = []
	todo = list(jobs)[:limit]
	for i, job in enumerate(todo, 1):
		item = {"key": job.label, "label": job.label, "name": "", "array": None, "reason": ""}
		try:
			item["name"] = job.output_name()
		except (KeyError, IndexError, ValueError) as e:
			item["reason"] = "name pattern: {0}".format(e)
		try:
			got = batch.get_worker(job.kind)(job)
			arr = got[0] if isinstance(got, tuple) else got
			if callable(arr):
				arr = arr()
			item["array"] = arr
		except batch.Skip as e:
			item["reason"] = "skip: {0}".format(e)
		except Exception as e:  # shown on the tile; Run would record it as an error
			item["reason"] = "{0}: {1}".format(type(e).__name__, e)
		items.append(item)
		if progress is not None:
			progress(i / float(len(todo)), job.label)
	return items


class TilePreview(PixelPreview):
	"""A grid tile's preview: the wheel scrolls the grid instead of zooming."""

	def on_touch_down(self, touch):
		if getattr(touch, "button", "").startswith("scroll"):
			return False
		return super().on_touch_down(touch)


class PreviewGrid(BoxLayout):
	def __init__(self, on_open=None, **kw):
		super().__init__(orientation="vertical", **kw)
		self.on_open = on_open
		self.items = []
		self.boxes = {}
		self._unticked = set()
		self.info = Label(text="", size_hint_y=None, height=22)
		self.add_widget(self.info)
		self.scroll = ScrollView(do_scroll_x=False)
		self.grid = GridLayout(cols=4, size_hint_y=None, spacing=4, padding=4, col_default_width=TILE_W,
			row_default_height=TILE_H, row_force_default=True)
		self.grid.bind(minimum_height=self.grid.setter("height"))
		self.scroll.add_widget(self.grid)
		self.add_widget(self.scroll)
		self.bind(width=lambda inst, w: setattr(self.grid, "cols", max(1, int(w // (TILE_W + 4)))))

	def set_items(self, items, total=None):
		self.items = list(items)
		self.grid.clear_widgets()
		self.boxes = {}
		for item in self.items:
			tile = BoxLayout(orientation="vertical", spacing=2)
			pv = TilePreview(pick_buttons=["left"], pan_buttons=[])
			if item.get("array") is not None:
				pv.set_array(item["array"], fit=True)
				pv.bind(size=lambda inst, *a: inst.fit())
			pv.bind(on_pixel=lambda inst, c, r, b, kind, it=item: kind == "up" and self._open(it))
			tile.add_widget(pv)
			row = BoxLayout(size_hint_y=None, height=24)
			box = CheckBox(active=item["key"] not in self._unticked and item.get("array") is not None,
				size_hint_x=None, width=24, disabled=item.get("array") is None)
			box.bind(active=lambda inst, v, k=item["key"]: self._tick(k, v))
			row.add_widget(box)
			text = item.get("name") or item["label"]
			if item.get("reason"):
				text = item["reason"]
			lbl = Label(text=text, font_size="10sp", halign="left", valign="middle", shorten=True,
				shorten_from="left")
			lbl.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
			row.add_widget(lbl)
			tile.add_widget(row)
			self.grid.add_widget(tile)
			self.boxes[item["key"]] = box
		n_ok = sum(1 for i in self.items if i.get("array") is not None)
		more = "" if total is None or total <= len(self.items) else ", first {0} of {1} shown".format(
			len(self.items), total)
		self.info.text = "{0} result(s), {1} skipped{2}; untick to leave out, click to enlarge".format(
			n_ok, len(self.items) - n_ok, more)

	def _tick(self, key, v):
		if v:
			self._unticked.discard(key)
		else:
			self._unticked.add(key)

	def _open(self, item):
		if self.on_open is not None and item.get("array") is not None:
			self.on_open(item)

	def unticked(self):
		return set(self._unticked)

	def set_unticked(self, keys):
		self._unticked = set(keys or ())
		for k, box in self.boxes.items():
			box.active = k not in self._unticked and not box.disabled
