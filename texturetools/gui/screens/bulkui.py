# SPDX-License-Identifier: GPL-3.0-only
"""Shared bulk UI for tool screens: the "Preview all" grid over the preview area (plan_texture_tools_gui.md
sections 3 and 4, P6).

BulkPreviewMixin (mix into a ToolScreen before it):
	bulk_jobs(params) -> list of batch.Job      WORKER-SAFE, pure; the jobs Run would make for these params
	install_bulk_view()                         call at the end of build_controls: a bar over the preview with
	                                            "Preview all" and "Grid" / "Single" view switching
	preview_all()                               computes every job's result (no file written) on a worker and
	                                            shows the grid; a click on a tile shows it full size
	run_params()                                params + "_skip": labels of unticked tiles (kept out of presets)
	filter_skipped(jobs, params)                jobs without the unticked ones
"""
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label

from ..widgets.previewgrid import MAX_TILES, PreviewGrid, compute_job_previews
from ..worker import Worker


class BulkPreviewMixin:
	grid_items = None

	def bulk_jobs(self, params):
		return []

	def install_bulk_view(self):
		self.grid = PreviewGrid(on_open=self._open_tile)
		self._grid_worker = None
		self._grid_shown = False
		self._grid_params = None
		bar = BoxLayout(size_hint_y=None, height=32, spacing=4)
		self.preview_all_button = Button(text="Preview all", size_hint_x=None, width=110)
		self.preview_all_button.bind(on_release=lambda *a: self.preview_all())
		self.view_button = Button(text="Grid", size_hint_x=None, width=80, disabled=True)
		self.view_button.bind(on_release=lambda *a: self.show_grid(not self._grid_shown))
		self.grid_note = Label(text="", halign="left", valign="middle", font_size="12sp")
		self.grid_note.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		for w in (self.preview_all_button, self.view_button, self.grid_note):
			bar.add_widget(w)
		self.bulk_bar = bar
		self.preview_area.add_widget(bar, index=len(self.preview_area.children))

	def _view_holder(self):
		return self.preview_area

	def show_grid(self, on):
		holder = self._view_holder()
		if on == self._grid_shown:
			return
		if on:
			holder.remove_widget(self.preview)
			holder.add_widget(self.grid)
		else:
			holder.remove_widget(self.grid)
			holder.add_widget(self.preview)
		self._grid_shown = on
		self.view_button.text = "Single" if on else "Grid"

	def preview_all(self):
		msg = self.validate(self.params)
		if msg:
			self.set_status(msg)
			return
		if self._grid_worker is None:
			self._grid_worker = Worker(latest_only=True, name="tt-grid-" + self.tool_name)
		self.preview_all_button.disabled = True
		self.set_status("computing every result (nothing is written) ...")
		self._grid_params = dict(self.params)
		self._grid_worker.submit(self._compute_all, self.params, on_done=self._all_done, on_error=self._all_error,
			on_progress=lambda f, m: self.set_status("preview all {0:.0f}% {1}".format(f * 100, m)))

	def _compute_all(self, params, progress):
		jobs = self.bulk_jobs(params)
		return {"items": compute_job_previews(jobs, MAX_TILES, progress), "total": len(jobs)}

	def _all_done(self, result):
		self.preview_all_button.disabled = False
		self.grid_items = result["items"]
		self.grid.set_items(result["items"], result["total"])
		self.view_button.disabled = False
		self.grid_note.text = ""
		self.show_grid(True)
		self.set_status(self.grid.info.text)

	def _all_error(self, exc, tb):
		self.preview_all_button.disabled = False
		self._error(exc, tb)

	def _open_tile(self, item):
		self.show_grid(False)
		self.preview.set_array(item["array"], fit=True)
		self.result_array = item["array"]
		self.result_name = item.get("name") or ""
		self.set_status("{0} (Grid returns to every result)".format(item.get("name") or item["label"]))

	def note_params_changed(self):
		if self._grid_params is not None and self._grid_params != self.params:
			self.grid_note.text = "settings changed since Preview all"

	def run_params(self):
		p = dict(self.params)
		p["_skip"] = sorted(self.grid.unticked())
		return p

	@staticmethod
	def filter_skipped(jobs, params):
		skip = set(params.get("_skip") or ())
		return [j for j in jobs if j.label not in skip]

	def shutdown_bulk(self):
		if self._grid_worker is not None:
			self._grid_worker.close()
