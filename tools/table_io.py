#!/usr/bin/env python3
"""CSV tables of the pack's tools (plan_process_overhaul.md P3.3, owner ruling D14: CSV is the default format).

Format: UTF-8, `\\n` line ends, Python `csv` dialect (comma, double quotes only where needed). Optional leading comment
lines start with `#` and are kept on write; the first non-comment line is the header.

  read(path)                  -> (comments, header, rows as lists, padded to the header length)
  read_dicts(path)            -> [dict] keyed by header, each with '_line' (1-based file line)
  write(path, header, rows, comments=())
  append(path, header, row)   adds one row; writes the header first when the file is new or empty
"""
import csv
import io
from pathlib import Path


def _parse(path):
	p = Path(path)
	if not p.exists():
		return [], [], [], []
	lines = p.read_text(encoding='utf-8').split('\n')
	comments = []
	while lines and lines[0].startswith('#'):
		comments.append(lines.pop(0))
	rdr = csv.reader(io.StringIO('\n'.join(lines)))
	header, rows, nums = [], [], []
	start = 1
	for r in rdr:
		line, start = start + len(comments), rdr.line_num + 1
		if not header:
			header = r
			continue
		if not r or (len(r) == 1 and not r[0]):
			continue
		rows.append(r + [''] * (len(header) - len(r)))
		nums.append(line)
	return comments, header, rows, nums


def read(path):
	comments, header, rows, _ = _parse(path)
	return comments, header, rows


def read_dicts(path):
	_, header, rows, nums = _parse(path)
	out = []
	for r, n in zip(rows, nums):
		d = dict(zip(header, r))
		d['_line'] = n
		out.append(d)
	return out


def dumps(header, rows, comments=()):
	buf = io.StringIO()
	for c in comments:
		buf.write(c.rstrip('\n') + '\n')
	w = csv.writer(buf, lineterminator='\n')
	w.writerow(header)
	for r in rows:
		w.writerow(['' if v is None else v for v in r])
	return buf.getvalue()


def write(path, header, rows, comments=()):
	Path(path).parent.mkdir(parents=True, exist_ok=True)
	Path(path).write_text(dumps(header, rows, comments), encoding='utf-8')


def append(path, header, row):
	p = Path(path)
	new = not p.exists() or p.stat().st_size == 0
	p.parent.mkdir(parents=True, exist_ok=True)
	with open(p, 'a', encoding='utf-8', newline='') as f:
		w = csv.writer(f, lineterminator='\n')
		if new:
			w.writerow(header)
		w.writerow(row)
