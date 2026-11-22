# SPDX-License-Identifier: GPL-3.0-only
"""Worker threads and debounce for the GUI (plan_texture_tools_gui.md section 8).

Worker: one daemon thread per instance; submit(fn, params, on_done, on_error) runs fn(params) off the main thread and
hands the result to on_done on the main thread (Kivy Clock). latest_only=True (previews): a newer submit supersedes
any job not yet started, and the result of a job that was superseded while running is dropped, so the screen only ever
shows the newest params. latest_only=False (Run jobs): every job runs, in order.

fn receives a frozen deep copy of params. fn may accept a second argument `progress(fraction, message)`; progress
calls are forwarded to on_progress on the main thread.

Debouncer: call() restarts a timer; fn runs once, delay seconds after the last call (default 150 ms).

The main-thread dispatcher defaults to Clock.schedule_once; pass dispatch=lambda f: f() to use without Kivy (tests).
"""
import copy
import inspect
import threading
import traceback

PREVIEW_DEBOUNCE = 0.15


def _clock_dispatch(fn):
	from kivy.clock import Clock
	Clock.schedule_once(lambda dt: fn(), 0)


def _wants_progress(fn):
	try:
		sig = inspect.signature(fn)
	except (TypeError, ValueError):
		return False
	params = [p for p in sig.parameters.values() if p.kind in (p.POSITIONAL_ONLY, p.POSITIONAL_OR_KEYWORD)]
	return len(params) >= 2 or any(p.kind == p.VAR_POSITIONAL for p in sig.parameters.values())


class Worker:
	def __init__(self, latest_only=True, dispatch=None, name="tt-worker"):
		self.latest_only = latest_only
		self._dispatch = dispatch or _clock_dispatch
		self._cond = threading.Condition()
		self._queue = []
		self._generation = 0
		self._busy = False
		self._closed = False
		self._thread = threading.Thread(target=self._loop, name=name, daemon=True)
		self._thread.start()

	@property
	def busy(self):
		with self._cond:
			return self._busy or bool(self._queue)

	def submit(self, fn, params=None, on_done=None, on_error=None, on_progress=None):
		"""Queue fn(params[, progress]). Returns the job's generation number."""
		frozen = copy.deepcopy(params) if params is not None else {}
		with self._cond:
			self._generation += 1
			job = (self._generation, fn, frozen, on_done, on_error, on_progress)
			if self.latest_only:
				self._queue[:] = [job]
			else:
				self._queue.append(job)
			self._cond.notify()
			return self._generation

	def cancel_pending(self):
		"""Drop queued jobs and the result of a running latest_only job."""
		with self._cond:
			self._queue.clear()
			self._generation += 1

	def close(self):
		with self._cond:
			self._closed = True
			self._queue.clear()
			self._cond.notify()

	def wait_idle(self, timeout=None):
		"""Block until the queue is empty and nothing runs (tests). True when idle."""
		with self._cond:
			return self._cond.wait_for(lambda: not self._queue and not self._busy, timeout)

	def _loop(self):
		while True:
			with self._cond:
				self._cond.wait_for(lambda: self._queue or self._closed)
				if self._closed:
					return
				gen, fn, params, on_done, on_error, on_progress = self._queue.pop(0)
				self._busy = True
			try:
				if _wants_progress(fn):
					def progress(frac, msg="", _cb=on_progress):
						if _cb is not None:
							self._dispatch(lambda: _cb(frac, msg))
					result = fn(params, progress)
				else:
					result = fn(params)
				err = None
			except Exception as e:  # handed to on_error on the main thread
				result, err = None, (e, traceback.format_exc())
			with self._cond:
				stale = self.latest_only and gen != self._generation
				self._busy = False
				self._cond.notify_all()
			if stale:
				continue
			if err is not None:
				if on_error is not None:
					self._dispatch(lambda e=err: on_error(*e))
			elif on_done is not None:
				self._dispatch(lambda r=result: on_done(r))


class Debouncer:
	"""fn() runs once, `delay` seconds after the last call() (Kivy Clock, main thread)."""

	def __init__(self, fn, delay=PREVIEW_DEBOUNCE):
		self.fn = fn
		self.delay = delay
		self._ev = None

	def __call__(self, *args):
		from kivy.clock import Clock
		if self._ev is not None:
			self._ev.cancel()
		self._ev = Clock.schedule_once(self._fire, self.delay)

	def _fire(self, dt):
		self._ev = None
		self.fn()

	def cancel(self):
		if self._ev is not None:
			self._ev.cancel()
			self._ev = None

	def flush(self):
		"""Run now if pending."""
		if self._ev is not None:
			self.cancel()
			self.fn()
