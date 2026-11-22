# SPDX-License-Identifier: GPL-3.0-only
"""Shared undo stack (plan_texture_tools_gui.md sections 2, 6, 8): snapshots of the whole state (usually an RGBA
array) with undo, redo, a labeled history for the history strip, jump to any entry, and a memory limit (oldest
entries are dropped first; the current state is always kept).

Snapshots are copied on push and on the way out, so the caller may keep editing the array it pushed or got back.
Any state works: numpy arrays (size from nbytes), or other objects (copied with copy.deepcopy, sized with
sys.getsizeof unless a size is given).
"""

import copy
import sys

import numpy as np

DEFAULT_LIMIT = 256 * 1024 * 1024


def _copy(state):
	return state.copy() if isinstance(state, np.ndarray) else copy.deepcopy(state)


def _size(state):
	if isinstance(state, np.ndarray):
		return state.nbytes
	if isinstance(state, (tuple, list)):
		return sum(_size(s) for s in state)
	return sys.getsizeof(state)


class UndoStack:
	"""entries[0..index] are the past and current state, entries[index+1..] the redo branch.

	s = UndoStack(img, label='open'); s.push(new_img, 'pencil'); s.undo() -> previous copy; s.redo();
	s.current (a copy); s.history() -> [(label, is_current)]; s.jump(i); s.clear(state)."""

	def __init__(self, initial=None, label="start", limit_bytes=DEFAULT_LIMIT, max_steps=None):
		self.limit_bytes = limit_bytes
		self.max_steps = max_steps
		self._entries = []    # [label, state, size]
		self._index = -1
		if initial is not None:
			self.clear(initial, label)

	# -- state

	def clear(self, state, label="start"):
		"""Forget all history; state becomes the only entry."""
		self._entries = [[label, _copy(state), _size(state)]]
		self._index = 0

	def push(self, state, label="edit"):
		"""Record state as the new current entry; the redo branch is dropped."""
		del self._entries[self._index + 1:]
		self._entries.append([label, _copy(state), _size(state)])
		self._index = len(self._entries) - 1
		self._trim()

	@property
	def current(self):
		if self._index < 0:
			return None
		return _copy(self._entries[self._index][1])

	@property
	def can_undo(self):
		return self._index > 0

	@property
	def can_redo(self):
		return 0 <= self._index < len(self._entries) - 1

	def undo(self):
		"""Step back; returns the now-current state (a copy), or None when there is nothing to undo."""
		if not self.can_undo:
			return None
		self._index -= 1
		return self.current

	def redo(self):
		if not self.can_redo:
			return None
		self._index += 1
		return self.current

	def jump(self, i):
		"""Make history entry i current (history strip click); returns it."""
		if not 0 <= i < len(self._entries):
			raise IndexError(i)
		self._index = i
		return self.current

	def history(self):
		"""[(label, is_current)] oldest first."""
		return [(e[0], k == self._index) for k, e in enumerate(self._entries)]

	@property
	def index(self):
		return self._index

	def __len__(self):
		return len(self._entries)

	@property
	def bytes_used(self):
		return sum(e[2] for e in self._entries)

	def _trim(self):
		while len(self._entries) > 1 and self._index > 0 and (
				self.bytes_used > self.limit_bytes or (self.max_steps and len(self._entries) > self.max_steps)):
			self._entries.pop(0)
			self._index -= 1
