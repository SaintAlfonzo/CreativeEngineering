#!/usr/bin/env python3
"""Cropper, manifest and index for a client capture run (feature_writeup/client_capture.md 2.3, 2.9, 2.10).

    python3 tools/client_capture/crop.py RUN_DIR          crop raw/ by the run's specs, write manifest.json + index.md
    python3 tools/client_capture/crop.py --selftest       crop math on a generated frame (no game, no repo files)

RUN_DIR is test_captures/judgement/<run>/ as run_captures.py made it: run.json (resolved tests with crop rules),
results.jsonl (driver), runner.json (crashed / skipped tests), raw/ (frames + sidecars), client_*.log.

Owner rule 2026-11-15: this tool gives no visual verdict. A test with captures gets judge "owner" and an empty
verdict for the owner to fill; only a capture-free test whose read steps carry expect / reject gets PASS / FAIL
(text readback). Crops are rerun from raw/ without the game after a spec crop edit (rerun run_captures.py
--recrop RUN, which refreshes run.json from the specs, then calls this).
"""
import glob
import json
import os
import re
import sys

try:
	from PIL import Image
except ImportError:  # the self-test and the manifest still need PIL for crops; say so plainly
	Image = None

HERE = os.path.dirname(os.path.abspath(__file__))
STAGE_BG = (0x20, 0x20, 0x20)
LOG_BAD = re.compile(r"ERROR|Exception|Error occurred while handling event")
EXPR_OK = re.compile(r"^[0-9WH+\-*/(). ]+$")


def load_regions():
	with open(os.path.join(HERE, "crop_regions.json")) as f:
		return {k: v for k, v in json.load(f).items() if not k.startswith("_")}


def expr(v, gw, gh):
	if isinstance(v, (int, float)):
		return float(v)
	if not EXPR_OK.match(v):
		raise ValueError(f"bad region expression {v!r}")
	return float(eval(v, {"__builtins__": {}}, {"W": gw, "H": gh}))  # digits, W, H and + - * / ( ) only


def gui_rect(rule, side, regions):
	"""The crop rule as a GUI-coordinate rect (x, y, w, h), or None for full / auto."""
	gw, gh = side["gui_size"]
	if rule in ("full", "auto"):
		return None
	if rule == "gui":
		if "gui" not in side:
			raise ValueError("crop gui: no GUI bounds in the frame (no container or JEI screen open)")
		return list(side["gui"])
	if rule == "tooltip":
		if "tooltip" not in side:
			raise ValueError("crop tooltip: no tooltip drawn in the captured frame")
		return list(side["tooltip"])
	if rule.startswith("center:"):
		w, h = (int(n) for n in rule[7:].lower().split("x"))
		cx, cy = side.get("crosshair", [gw // 2, gh // 2])
		return [cx - w / 2, cy - h / 2, w, h]
	if rule.startswith("rect:"):
		return [float(n) for n in rule[5:].split(",")]
	if rule.startswith("slot:"):
		n = int(rule[5:])
		for i, x, y in side.get("slots", []):
			if i == n:
				return [x - 1, y - 1, 18, 18]
		raise ValueError(f"crop slot:{n}: no such slot in the frame")
	if rule in regions:
		return [expr(v, gw, gh) for v in regions[rule]]
	raise ValueError(f"unknown crop rule {rule!r}")


def trim_bg(img, bg=STAGE_BG, tol=6):
	"""Bounding box (px) of everything that is not the flat stage background."""
	from PIL import ImageChops
	rgb = img.convert("RGB")
	diff = ImageChops.difference(rgb, Image.new("RGB", rgb.size, bg))
	mask = diff.point(lambda v: 255 if v > tol else 0).convert("L").point(lambda v: 255 if v else 0)
	box = mask.getbbox()
	return list(box) if box else None


def crop_one(raw_png, side, cap, regions):
	"""Returns the cropped PIL image for one capture entry of a spec."""
	img = Image.open(raw_png)
	fw, fh = img.size
	scale = float(side.get("gui_scale", 1))
	rule = cap.get("crop", "full")
	pad = float(cap.get("pad", 4))
	rect = gui_rect(rule, side, regions)
	if rule == "auto":
		box = trim_bg(img)
		if box is None:
			raise ValueError("crop auto: the frame is all stage background")
		p = pad * scale
		box = [box[0] - p, box[1] - p, box[2] + p, box[3] + p]
	elif rect is None:
		box = [0, 0, fw, fh]
	else:
		x, y, w, h = rect
		if rule in ("tooltip", "gui"):
			x, y, w, h = x - pad, y - pad, w + 2 * pad, h + 2 * pad
		box = [x * scale, y * scale, (x + w) * scale, (y + h) * scale]
	box = [max(0, int(round(box[0]))), max(0, int(round(box[1]))),
		   min(fw, int(round(box[2]))), min(fh, int(round(box[3])))]
	if box[2] <= box[0] or box[3] <= box[1]:
		raise ValueError(f"crop {rule}: empty box {box}")
	out = img.crop(box)
	edge = int(cap.get("max_edge", 1024))
	if max(out.size) > edge:
		f = edge / max(out.size)
		out = out.resize((max(1, int(out.width * f)), max(1, int(out.height * f))), Image.BOX)
	return out


def read_jsonl(path):
	out = {}
	if os.path.exists(path):
		for line in open(path):
			line = line.strip()
			if line:
				r = json.loads(line)
				out[r["id"]] = r  # a relaunch reruns a test: the last result wins
	return out


def log_slices(run):
	"""Per test id: the ERROR / Exception lines between its BEGIN and END markers, over every launch's log."""
	errs = {}
	for logf in sorted(glob.glob(os.path.join(run, "client_*.log"))):
		cur = None
		for line in open(logf, errors="replace"):
			m = re.search(r"\[cdevcapture\] (BEGIN|END) (\S+)", line)
			if m:
				cur = m.group(2) if m.group(1) == "BEGIN" else None
				continue
			if cur and LOG_BAD.search(line):
				errs.setdefault(cur, []).append(line.strip()[:300])
	return errs


def text_verdict(test, res):
	"""PASS / FAIL only for a capture-free test whose read steps carry expect / reject; else None (owner judges)."""
	if test.get("capture"):
		return None
	judged = any(isinstance(s, dict) and ("expect" in s or "reject" in s) for s in test.get("steps", []))
	if not judged or res is None or res.get("status") not in ("ok", "fail"):
		return None
	return "PASS" if res["status"] == "ok" else "FAIL"


def build(run):
	with open(os.path.join(run, "run.json")) as f:
		meta = json.load(f)
	runner = {}
	rp = os.path.join(run, "runner.json")
	if os.path.exists(rp):
		with open(rp) as f:
			runner = json.load(f)
	results = read_jsonl(os.path.join(run, "results.jsonl"))
	errs = log_slices(run)
	regions = load_regions()
	raw = os.path.join(run, "raw")
	manifest = {"run": os.path.basename(os.path.normpath(run)), "groups": meta.get("groups", []),
				"judgement_rule": "pixels are judged by the owner only; verdict PASS/FAIL here comes from text readback",
				"jars": meta.get("jars", {}), "git": meta.get("git", ""), "tests": []}
	for t in meta["tests"]:
		res = results.get(t["id"])
		status = res["status"] if res else runner.get("status", {}).get(t["id"], "missing")
		e = {"id": t["id"], "covers": t.get("covers", []), "title": t.get("title", ""), "expect": t.get("expect", ""),
			 "group": t.get("group", ""), "spec": t.get("spec", ""), "status": status, "files": [], "crop_errors": []}
		if res:
			if res.get("error"):
				e["error"] = res["error"]
			e["reads"] = res.get("reads", [])
			e["steps"] = res.get("steps", [])
			e["raw"] = res.get("files", [])
		if t["id"] in runner.get("crash", {}):
			e["crash"] = runner["crash"][t["id"]]
		e["log_errors"] = errs.get(t["id"], [])
		for cap in t.get("capture", []):
			name = cap.get("name", "")
			stem = t["id"] + ("_" + name if name else "")
			frames = sorted(glob.glob(os.path.join(raw, glob.escape(stem) + "_f*.png"))) or \
				[p for p in [os.path.join(raw, stem + ".png")] if os.path.exists(p)]
			for png in frames:
				try:
					with open(png[:-4] + ".json") as f:
						side = json.load(f)
					out = crop_one(png, side, cap, regions)
					dst = os.path.basename(png)
					out.save(os.path.join(run, dst))
					e["files"].append(dst)
				except (OSError, ValueError, KeyError) as ex:
					e["crop_errors"].append(f"{os.path.basename(png)}: {ex}")
		v = text_verdict(t, res)
		e["judge"] = "text" if v else "owner"
		e["verdict"] = v
		e["verdict_by"] = "regex" if v else None
		manifest["tests"].append(e)
	with open(os.path.join(run, "manifest.json"), "w") as f:
		json.dump(manifest, f, indent=1)
	write_index(run, manifest)
	n = {}
	for e in manifest["tests"]:
		k = e["verdict"] or ("for owner" if e["status"] == "ok" else e["status"])
		n[k] = n.get(k, 0) + 1
	return manifest, n


def cell(s):
	return str(s).replace("|", "/").replace("\n", " ")


def write_index(run, m):
	lines = [f"# Capture run {m['run']}", "",
			 "Judgement folder (git-ignored, wiped after the owner has judged it). The tool and agents give no",
			 "visual verdict: fill the Verdict column (pass / fail / unclear + note) for every `owner` row. `text` rows",
			 "were judged by their read steps' regexes.", "",
			 "| ID | Covers | Expect | Captures | Driver | Judge | Verdict |", "|---|---|---|---|---|---|---|"]
	for e in m["tests"]:
		imgs = " ".join(f"![{f}]({f})" for f in e["files"]) or "-"
		drv = e["status"] + (": " + e["error"] if e.get("error") else "")
		if e["crop_errors"]:
			drv += "; crop: " + "; ".join(e["crop_errors"])
		lines.append(f"| {e['id']} | {cell(', '.join(e['covers']))} | {cell(e['expect'])} | {imgs} | {cell(drv)} | "
					 f"{e['judge']} | {e['verdict'] or ''} |")
	reads = [e for e in m["tests"] if e.get("reads")]
	if reads:
		lines += ["", "## Readback", ""]
		for e in reads:
			lines.append(f"- {e['id']}: " + "; ".join(cell(r) for r in e["reads"][:40]))
	with open(os.path.join(run, "index.md"), "w") as f:
		f.write("\n".join(lines) + "\n")


def selftest():
	"""Crop math on a synthetic 1280x720 frame at GUI scale 2; prints OK or raises."""
	import tempfile
	d = tempfile.mkdtemp(prefix="crop_selftest_")
	img = Image.new("RGB", (1280, 720), STAGE_BG)
	for x in range(600, 700):  # a "tooltip" 100x40 px at 600,300
		for y in range(300, 340):
			img.putpixel((x, y), (200, 200, 255))
	png = os.path.join(d, "T.png")
	img.save(png)
	side = {"gui_scale": 2, "frame": [1280, 720], "gui_size": [640, 360], "crosshair": [320, 180],
			"tooltip": [300, 150, 50, 20], "gui": [232, 97, 176, 166], "slots": [[0, 240, 110]]}
	reg = load_regions()
	checks = [
		({"crop": "auto", "pad": 4}, (116, 56)),        # 100+2*8, 40+2*8
		({"crop": "tooltip", "pad": 4}, (116, 56)),     # (50+8)*2, (20+8)*2
		({"crop": "gui", "pad": 0}, (352, 332)),
		({"crop": "center:200x120"}, (400, 240)),
		({"crop": "slot:0"}, (36, 36)),
		({"crop": "rect:10,10,30,20"}, (60, 40)),
		({"crop": "hotbar"}, (364, 46)),
		({"crop": "full"}, (1024, 576)),               # max_edge 1024
	]
	for cap, want in checks:
		got = crop_one(png, side, cap, reg).size
		assert got == want, f"{cap}: {got} != {want}"
		print(f"ok {cap['crop']:<16} {got}")
	print("crop selftest OK", d)


if __name__ == "__main__":
	if Image is None:
		sys.exit("PIL (python3-pil) is needed")
	if sys.argv[1:] == ["--selftest"]:
		selftest()
	elif len(sys.argv) == 2:
		man, counts = build(sys.argv[1])
		print(f"{len(man['tests'])} tests:", ", ".join(f"{k} {v}" for k, v in sorted(counts.items())),
			  "->", os.path.join(sys.argv[1], "index.md"))
	else:
		sys.exit(__doc__)
