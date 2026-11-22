# SPDX-License-Identifier: GPL-3.0-only
"""Texture provenance rows for the pack repo, dev mode only (plan_texture_tools_gui.md section 8, P6).

No Kivy import. The GUI offers a row after Run or Save as when dev mode is on; nothing is written unless the user
confirms it (append_row is only called from that confirm button). Shipped builds have dev mode off and no repo
knowledge: find_sources_csv() returns None outside a checkout of the pack.

Row columns (feature_writeup/texture_sources/sources.csv): pattern, origin, source, tool, used_for, status, note.
	pattern   repo-relative path of the output, or a glob over several outputs (common prefix + '*' + extension)
	origin    owned-tool (the owner's tool run on a source the owner picked)
	source    the inputs, repo-relative where they lie in the repo
	tool      the Copy as CLI command of the run
	used_for  typed by the user
	status    confirm (the owner's look-check turns it into ok)
After appending, tools/texture_provenance.py --write must be rerun (REMINDER).
"""
import csv
import datetime
import io
import os

CSV_REL = os.path.join("feature_writeup", "texture_sources", "sources.csv")
COLUMNS = ("pattern", "origin", "source", "tool", "used_for", "status", "note")
REMINDER = "Now run: python3 tools/texture_provenance.py --write"


def find_repo_root(*starts):
	"""First folder at or above any start path that holds feature_writeup/texture_sources/sources.csv, else None."""
	for start in starts:
		if not start:
			continue
		d = os.path.abspath(start)
		if os.path.isfile(d):
			d = os.path.dirname(d)
		while True:
			if os.path.isfile(os.path.join(d, CSV_REL)):
				return d
			parent = os.path.dirname(d)
			if parent == d:
				break
			d = parent
	return None


def find_sources_csv(*starts):
	root = find_repo_root(*starts)
	return os.path.join(root, CSV_REL) if root else None


def rel(path, root):
	"""Repo-relative path with '/' when path lies in root, else the absolute path."""
	if not path:
		return ""
	if isinstance(path, str) and path.startswith("colors:"):
		return path
	p = os.path.abspath(path)
	if root:
		r = os.path.abspath(root)
		if p == r or p.startswith(r + os.sep):
			return os.path.relpath(p, r).replace(os.sep, "/")
	return p


def output_pattern(outputs, root=None):
	"""One output -> its path; several -> common prefix + '*' + their shared extension (or '*')."""
	paths = [rel(o, root) for o in outputs if o]
	if not paths:
		return ""
	if len(paths) == 1:
		return paths[0]
	prefix = os.path.commonprefix(paths)
	exts = {os.path.splitext(p)[1] for p in paths}
	return prefix + "*" + (exts.pop() if len(exts) == 1 else "")


def suggest_row(outputs, inputs=(), tool="", used_for="", root=None, today=None):
	"""Row dict for sources.csv (the GUI shows it for editing before append_row)."""
	day = (today or datetime.date.today()).isoformat()
	seen, srcs = set(), []
	for i in inputs or ():
		r = rel(i, root)
		if r and r not in seen:
			seen.add(r)
			srcs.append(r)
	return {
		"pattern": output_pattern(outputs, root),
		"origin": "owned-tool",
		"source": "; ".join(srcs),
		"tool": tool or "",
		"used_for": used_for or "",
		"status": "confirm",
		"note": "Added by Texture Tools dev mode {0}; look-check pending".format(day),
	}


def format_row(row):
	"""The CSV line (with newline) append_row writes."""
	buf = io.StringIO()
	csv.writer(buf, lineterminator="\n").writerow([row.get(c, "") for c in COLUMNS])
	return buf.getvalue()


def append_row(csv_path, row):
	"""Append one row to csv_path (explicit user confirm only). Checks the header, keeps the file's comment lines and
	adds a missing final newline first. Returns the line written. Raises ValueError for a file without the header or
	a row without a pattern."""
	if not row.get("pattern"):
		raise ValueError("row has no pattern (output path)")
	with open(csv_path, encoding="utf-8", newline="") as f:
		text = f.read()
	header = next((ln for ln in text.splitlines() if ln and not ln.startswith("#")), "")
	if [h.strip() for h in header.split(",")] != list(COLUMNS):
		raise ValueError("{0} has no {1} header".format(csv_path, ",".join(COLUMNS)))
	line = format_row(row)
	with open(csv_path, "a", encoding="utf-8", newline="") as f:
		if text and not text.endswith("\n"):
			f.write("\n")
		f.write(line)
	return line
