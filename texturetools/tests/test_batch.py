# SPDX-License-Identifier: GPL-3.0-only
"""T3 batch runner tests (plan_texture_tools_gui.md section 10): a bulk run never writes outside its output
directory, the skip report lists every mismatch, dry run writes nothing, the overwrite policies, include and exclude
globs, progress, registered job kinds and combine_dir's counts. Synthetic arrays only; files in a temp dir.
"""

import contextlib
import io
import os
import shutil
import sys
import tempfile

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)

from texturetools.core import batch  # noqa: E402
from texturetools.core import combine as C  # noqa: E402

results = [0, 0]


def check(ok, what):
	results[0] += 1
	if not ok:
		results[1] += 1
		print("FAIL  " + what)


rng = np.random.default_rng(7)


def arr(h, w):
	return rng.integers(0, 256, (h, w, 4)).astype(np.float32)


def write(d, name, a):
	p = os.path.join(d, name)
	os.makedirs(os.path.dirname(p), exist_ok=True)
	Image.fromarray(a.astype(np.uint8), "RGBA").save(p)
	return p


def snapshot(d):
	out = {}
	for dp, _, fs in os.walk(d):
		for f in fs:
			p = os.path.join(dp, f)
			with open(p, "rb") as fh:
				out[os.path.relpath(p, d)] = fh.read()
	return out


def const_worker(job):
	return np.full((2, 2, 4), job.params.get("v", 255), np.float32)


def outside(tmp):
	"""Hostile names: none may land outside out/, every one is reported."""
	out = os.path.join(tmp, "out")
	os.makedirs(out)
	os.symlink(tmp, os.path.join(out, "link"))
	bad = ["../escape.png", "../../escape2.png", os.path.join(tmp, "abs.png"), "sub/../../escape3.png",
		   "link/escape4.png", "./", "", ".", "sub/.."]
	good = ["ok.png", "sub/deeper/ok2.png", "./ok3.png", "a..b.png"]
	jobs = [batch.Job(inputs={0: n}, pattern=n.replace("{", "{{").replace("}", "}}"), label=n) for n in bad + good]
	before = snapshot(tmp)
	rep = batch.run_batch(jobs, out, const_worker)
	after = snapshot(tmp)
	new = sorted(set(after) - set(before))
	inside = [p for p in new if p.startswith("out" + os.sep) and not p.startswith(os.path.join("out", "link"))]
	check(new == inside and len(inside) == len(good), "only the good names were written, inside out/: %r" % new)
	skipped = {r.job.label for r in rep.results if r.status != "written"}
	check(skipped == set(bad), "every hostile name reported: %r" % sorted(skipped ^ set(bad)))
	check(all("outside the output directory" in r.reason or r.status == "error" for r in rep.skipped + rep.errors),
		  "outside reason")
	# the same with random generated names
	for t in range(200):
		parts = [rng.choice(["..", ".", "a", "b", "link", "", tmp.strip("/")]) for _ in range(rng.integers(1, 5))]
		name = "/".join(parts) + ".png"
		b4 = set(snapshot(tmp))
		batch.run_batch([batch.Job(inputs={}, pattern=name.replace("{", "{{"), label=name)], out, const_worker)
		extra = set(snapshot(tmp)) - b4
		check(all(p.startswith("out" + os.sep) and not p.startswith(os.path.join("out", "link") + os.sep) for p in extra),
			  "random name %s wrote %r" % (name, extra))


def skip_report(tmp):
	d = os.path.join(tmp, "labels")
	base = write(tmp, "base.png", arr(16, 16))
	sizes = {"a": (16, 16), "b": (8, 8), "c": (16, 16), "d": (5, 7), "e": (16, 8), "f": (32, 32)}
	for n, (h, w) in sizes.items():
		write(d, n + ".png", arr(h, w))
	mism = {n for n, s in sizes.items() if s != (16, 16)}
	jobs = C.stack_jobs([C.Layer(base), C.Layer(None)], {1: d}, {"fit": "same"})
	rep = batch.run_batch(jobs, os.path.join(tmp, "o1"))
	listed = {os.path.splitext(os.path.basename(r.job.label))[0] for r in rep.skipped}
	check(listed == mism and len(rep.skip_report()) == len(mism), "stack skip report lists every mismatch: %r" % listed)
	err = io.StringIO()
	with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(err):
		n = C.combine_dir(base, d, os.path.join(tmp, "o2"))
	lines = [l for l in err.getvalue().splitlines() if l.startswith("Skip:")]
	check(n == len(sizes) - len(mism) and len(lines) == len(mism) and
		  all(any("/%s.png" % m in l for l in lines) for m in mism), "combine_dir skip lines: %r" % lines)
	# dry run: same report, nothing written, no directory made
	before = snapshot(tmp)
	rep = batch.run_batch(jobs, os.path.join(tmp, "o3"), dry_run=True)
	check(snapshot(tmp) == before and not os.path.exists(os.path.join(tmp, "o3")), "dry run writes nothing")
	check(len(rep.by_status("dry")) == len(sizes) - len(mism) and len(rep.skipped) == len(mism), "dry run report")
	with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
		n = C.combine_dir(base, d, os.path.join(tmp, "o4"), dry_run=True)
	check(n == len(sizes) - len(mism) and not os.path.exists(os.path.join(tmp, "o4")), "combine_dir dry run")
	# include / exclude
	names = [os.path.basename(p) for p in batch.scan_folder(d, include=["a*", "c*", "d*"], exclude="d*")]
	check(names == ["a.png", "c.png"], "include and exclude: %r" % names)


def overwrite(tmp):
	out = os.path.join(tmp, "ow")
	jobs = lambda v: [batch.Job(inputs={}, params={"v": v}, pattern="x.png", label="x")]
	batch.run_batch(jobs(10), out, const_worker)
	rep = batch.run_batch(jobs(20), out, const_worker, overwrite="skip")
	check(rep.results[0].status == "exists" and np.array(Image.open(os.path.join(out, "x.png")))[0, 0, 0] == 10,
		  "skip keeps the old file")
	rep = batch.run_batch(jobs(30) + jobs(40), out, const_worker, overwrite="rename")
	check([os.path.basename(r.output) for r in rep.written] == ["x_1.png", "x_2.png"], "rename numbers new files")
	batch.run_batch(jobs(50), out, const_worker, overwrite="replace")
	check(np.array(Image.open(os.path.join(out, "x.png")))[0, 0, 0] == 50, "replace overwrites")
	try:
		batch.run_batch(jobs(1), out, const_worker, overwrite="nope")
		check(False, "bad policy raises")
	except ValueError:
		check(True, "")


def runner_misc(tmp):
	seen = []
	batch.register_kind("test_const", const_worker)
	jobs = [batch.Job(inputs={}, pattern="k%d.png" % i, kind="test_const", label=str(i)) for i in range(5)]
	rep = batch.run_batch(jobs, os.path.join(tmp, "k"), progress=lambda d, t, r: seen.append((d, t, r.status)))
	check(seen == [(i, 5, "written") for i in range(1, 6)], "progress and registered kind")
	check("combine" in batch.kinds() and "test_const" in batch.kinds(), "kinds listed")

	def boom(job):
		raise RuntimeError("bad input")
	rep = batch.run_batch(jobs[:2], os.path.join(tmp, "k2"), boom)
	check(len(rep.errors) == 2 and "bad input" in rep.errors[0].reason, "errors recorded under on_error=skip")
	try:
		batch.run_batch(jobs[:1], os.path.join(tmp, "k3"), boom, on_error="raise")
		check(False, "on_error=raise propagates")
	except RuntimeError:
		check(True, "")
	calls = []

	def deferred(job):
		def compute():
			calls.append(job.label)
			return const_worker(job)
		return compute
	batch.run_batch(jobs, os.path.join(tmp, "k4"), deferred, dry_run=True)
	check(calls == [], "dry run does not call a deferred compute")
	j = batch.product_jobs({0: ["p/a.png", "p/b.png"], 2: ["q/x.png", "q/y.png", "q/z.png"]})
	check(len(j) == 6 == batch.count_product({0: [1, 2], 2: [1, 2, 3]}) and j[0].output_name() == "a_x.png"
		  and j[5].fields["n"] == 6 and j[5].fields["name2"] == "z", "product jobs and fields")
	check(batch.count_product({0: [], 1: [1]}) == 0 and batch.count_product({}) == 0, "empty products")


def main():
	tmp = tempfile.mkdtemp(prefix="tt_batch_")
	try:
		outside(os.path.join(tmp, "o") if os.makedirs(os.path.join(tmp, "o")) is None else None)
		skip_report(tmp)
		overwrite(tmp)
		runner_misc(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("{0}  T3 batch: {1} checks, {2} failed".format("PASS" if not results[1] else "FAIL", *results))
	return 1 if results[1] else 0


if __name__ == "__main__":
	sys.exit(main())
