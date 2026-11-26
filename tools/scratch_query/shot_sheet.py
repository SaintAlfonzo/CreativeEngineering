#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Owner review sheets: one labeled contact sheet per test row from qrun client shots.

    shot_sheet.py OUT_DIR DIR [DIR ...] [--rows ID,ID] [--width 640] [--cols 2]

Each DIR is a test_captures/judgement/qrun_* folder. Shots are grouped by the row id prefix of their file name
(`CQ-CMP-18_curio_hover.png` -> CQ-CMP-18); a later DIR replaces an earlier DIR's shots for the same row (pass a
rerun folder last). Writes OUT_DIR/<ROW>.png, each shot scaled to --width and captioned with its shot name.
"""
import argparse
import re
from pathlib import Path

from PIL import Image, ImageDraw

ROW = re.compile(r"^((?:CQ|SQ)-[A-Z0-9]+-\d+[a-z]?)_(.+)\.png$")


def main():
	ap = argparse.ArgumentParser()
	ap.add_argument("out")
	ap.add_argument("dirs", nargs="+")
	ap.add_argument("--rows", default="")
	ap.add_argument("--width", type=int, default=640)
	ap.add_argument("--cols", type=int, default=2)
	a = ap.parse_args()
	want = {r for r in a.rows.split(",") if r}
	rows = {}
	for d in a.dirs:
		found = {}
		for p in sorted(Path(d).glob("*.png")):
			m = ROW.match(p.name)
			if m and (not want or m.group(1) in want):
				found.setdefault(m.group(1), []).append((m.group(2), p))
		rows.update(found)
	out = Path(a.out)
	out.mkdir(parents=True, exist_ok=True)
	cap = 22
	for row, shots in sorted(rows.items()):
		tiles = []
		for name, p in shots:
			im = Image.open(p).convert("RGB")
			h = round(im.height * a.width / im.width)
			tiles.append((name, im.resize((a.width, h), Image.LANCZOS)))
		cols = min(a.cols, len(tiles))
		th = max(t.height for _, t in tiles) + cap
		nrows = (len(tiles) + cols - 1) // cols
		sheet = Image.new("RGB", (cols * a.width + (cols - 1) * 6, nrows * th + (nrows - 1) * 6), (32, 32, 32))
		dr = ImageDraw.Draw(sheet)
		for i, (name, t) in enumerate(tiles):
			x, y = (i % cols) * (a.width + 6), (i // cols) * (th + 6)
			dr.text((x + 6, y + 5), f"{row}  {name}", fill=(235, 235, 235))
			sheet.paste(t, (x, y + cap))
		sheet.save(out / f"{row}.png", optimize=True)
		print(f"{row}: {len(tiles)} shots -> {out / (row + '.png')}")


if __name__ == "__main__":
	main()
