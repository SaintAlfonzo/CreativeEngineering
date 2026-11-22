# SPDX-License-Identifier: GPL-3.0-only
"""One job runner for every screen (plan_texture_tools_gui.md sections 2, 4, 8): a list of jobs (inputs, params, an
output name pattern) -> outputs in one output directory, with a progress callback, dry run, an overwrite policy
(skip, rename, replace), include and exclude globs for folder scans, a varying slot and cartesian products (counted
before the run), and a skip report.

Engine-neutral: a job kind is a worker function registered under a name (register_kind); combine registers "combine",
the swap and paint engines register theirs. A worker takes a Job and returns an RGBA array, or (array, info text),
or raises Skip(reason). The array may be a no-argument callable (deferred compute): the runner formats the output
name first and calls it only when it writes, so a dry run checks inputs without computing. The runner never writes outside its output directory: a name that resolves outside is
skipped and reported.
"""

import fnmatch
import itertools
import os
from dataclasses import dataclass, field
from typing import Any, Callable, Dict, List, Optional, Sequence

from texturetools.core.image_io import save_raw

IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".bmp", ".tga", ".gif", ".webp"}
OVERWRITE_POLICIES = ("replace", "skip", "rename")


class Skip(Exception):
	"""Raised by a worker to skip one job; str(e) is the reason shown in the skip report."""


@dataclass
class Job:
	"""inputs: slot -> value (usually a file path); params: worker parameters; pattern and fields build the output
	name (formatted after the worker succeeds, so a bad placeholder fails where the old scripts failed); label: a
	short text for progress and reports (default: the varying inputs)."""
	inputs: Dict[Any, Any]
	params: Dict[str, Any] = field(default_factory=dict)
	pattern: str = "{name}.png"
	fields: Dict[str, Any] = field(default_factory=dict)
	kind: Optional[str] = None
	label: str = ""

	def output_name(self):
		return self.pattern.format(**self.fields)


@dataclass
class JobResult:
	job: Job
	status: str               # written | skipped | dry | exists | error
	output: Optional[str] = None   # path as joined (output_dir + name), as printed by the old scripts
	reason: str = ""
	info: Optional[str] = None


@dataclass
class BatchReport:
	results: List[JobResult] = field(default_factory=list)

	def by_status(self, status):
		return [r for r in self.results if r.status == status]

	@property
	def written(self):
		return self.by_status("written")

	@property
	def skipped(self):
		"""Every job not written for a reason (worker Skip, outside the output dir, existing file under 'skip')."""
		return [r for r in self.results if r.status in ("skipped", "exists")]

	@property
	def errors(self):
		return self.by_status("error")

	def counts(self):
		out = {}
		for r in self.results:
			out[r.status] = out.get(r.status, 0) + 1
		return out

	def skip_report(self):
		"""One line per skipped job: '<label>: <reason>'."""
		return ["{0}: {1}".format(r.job.label, r.reason) for r in self.skipped + self.errors]


# ---------------------------------------------------------------- job kinds (engines register here)

_KINDS: Dict[str, Callable] = {}


def register_kind(name, worker):
	"""Register worker(job) -> array | (array, info) under name; jobs with job.kind == name use it."""
	_KINDS[name] = worker


def get_worker(name):
	return _KINDS[name]


def kinds():
	return sorted(_KINDS)


# ---------------------------------------------------------------- inputs and names

def scan_folder(folder, include=None, exclude=None, recursive=False, exts=IMAGE_EXTS):
	"""Sorted image files in folder (non-recursive by default; files only, extension in exts, case-insensitive).
	include / exclude: glob or list of globs matched against the path relative to folder (with '/'); a file must
	match one include (when given) and no exclude."""
	inc = [include] if isinstance(include, str) else list(include or [])
	exc = [exclude] if isinstance(exclude, str) else list(exclude or [])
	if recursive:
		rels = []
		for dp, dns, fns in os.walk(folder):
			dns.sort()
			for f in fns:
				rels.append(os.path.relpath(os.path.join(dp, f), folder).replace(os.sep, "/"))
		rels.sort()
	else:
		rels = sorted(os.listdir(folder))
	out = []
	for rel in rels:
		path = os.path.join(folder, rel)
		if not os.path.isfile(path) or os.path.splitext(rel)[1].lower() not in exts:
			continue
		if inc and not any(fnmatch.fnmatch(rel, g) for g in inc):
			continue
		if any(fnmatch.fnmatch(rel, g) for g in exc):
			continue
		out.append(path)
	return out


def name_fields(path):
	"""{name} stem and {ext} extension without the dot of one input path."""
	stem, ext = os.path.splitext(os.path.basename(path))
	return {"name": stem, "ext": ext[1:]}


def count_product(slots):
	"""Number of jobs a cartesian run over slots (slot -> list of values) makes; 0 when any list is empty."""
	n = 1
	for vals in slots.values():
		n *= len(vals)
	return n if slots else 0


def product_jobs(varying, fixed=None, pattern="{name}.png", params=None, kind=None, extra_fields=None):
	"""Jobs for every combination of the varying slots (slot -> list of paths), in slot order then file order.
	One varying slot = the plain folder batch; two or more = cartesian (call count_product first to confirm).
	fixed: slot -> value shared by every job. Name fields: {name} and {ext} of each varying slot as
	{name<slot>} / {ext<slot>}; {name} = the varying stems joined with '_', {ext} = the first varying slot's;
	{n} = 1-based job number; plus extra_fields (e.g. {base})."""
	keys = list(varying)
	jobs = []
	for n, combo in enumerate(itertools.product(*(varying[k] for k in keys)), 1):
		inputs = dict(fixed or {})
		f = dict(extra_fields or {})
		stems = []
		for k, path in zip(keys, combo):
			inputs[k] = path
			nf = name_fields(path)
			f["name{0}".format(k)] = nf["name"]
			f["ext{0}".format(k)] = nf["ext"]
			stems.append(nf["name"])
		first = name_fields(combo[0]) if combo else {"name": "", "ext": ""}
		f.setdefault("name", "_".join(stems))
		f.setdefault("ext", first["ext"])
		f["n"] = n
		jobs.append(Job(inputs=inputs, params=dict(params or {}), pattern=pattern, fields=f, kind=kind,
						label=" + ".join(str(c) for c in combo)))
	return jobs


# ---------------------------------------------------------------- the runner

def _inside(root, path):
	root = os.path.realpath(root)
	p = os.path.realpath(path)
	return p == root or p.startswith(root + os.sep)


def _renamed(path, taken):
	stem, ext = os.path.splitext(path)
	i = 1
	while True:
		cand = "{0}_{1}{2}".format(stem, i, ext)
		if not os.path.exists(cand) and cand not in taken:
			return cand
		i += 1


def run_batch(jobs: Sequence[Job], output_dir, worker=None, *, dry_run=False, overwrite="replace", progress=None,
			  on_result=None, on_error="skip", compute_in_dry_run=True, saver=save_raw, make_dirs=True):
	"""Run every job; returns a BatchReport.

	worker: function(job) -> array | (array, info), array may be a callable returning it; default: the registered worker of each job's kind.
	dry_run: write nothing (and create no directory); with compute_in_dry_run the worker still runs so its skips
	show, results come back as status 'dry'.
	overwrite: 'replace' (old scripts' behavior), 'skip' (existing file -> status 'exists'), 'rename' (name_1.png ...).
	progress(done, total, result): after each job. on_result(result): after each job (the old scripts' printing).
	on_error: 'skip' records an exception as status 'error'; 'raise' lets it propagate (old scripts' behavior).
	Never writes outside output_dir: such a name is skipped with reason 'outside the output directory'."""
	if overwrite not in OVERWRITE_POLICIES:
		raise ValueError("overwrite must be one of {0}".format(", ".join(OVERWRITE_POLICIES)))
	report = BatchReport()
	if make_dirs and not dry_run:
		os.makedirs(output_dir, exist_ok=True)
	taken = set()
	total = len(jobs)
	for i, job in enumerate(jobs, 1):
		res = _run_one(job, output_dir, worker, dry_run, overwrite, on_error, compute_in_dry_run, saver, taken)
		report.results.append(res)
		if on_result:
			on_result(res)
		if progress:
			progress(i, total, res)
	return report


def _run_one(job, output_dir, worker, dry_run, overwrite, on_error, compute, saver, taken):
	fn = worker or (get_worker(job.kind) if job.kind else None)
	if fn is None:
		raise ValueError("job has no worker and no registered kind: {0!r}".format(job.kind))
	arr, info = None, None
	try:
		if not dry_run or compute:
			got = fn(job)
			arr, info = got if isinstance(got, tuple) else (got, None)
		out = os.path.join(output_dir, job.output_name())
		if callable(arr) and not dry_run:
			arr = arr()
	except Skip as e:
		return JobResult(job, "skipped", reason=str(e))
	except Exception as e:
		if on_error == "raise":
			raise
		return JobResult(job, "error", reason="{0}: {1}".format(type(e).__name__, e))
	if not _inside(output_dir, out) or os.path.realpath(out) == os.path.realpath(output_dir):
		return JobResult(job, "skipped", out, reason="outside the output directory: {0}".format(job.output_name()))
	if os.path.exists(out) or out in taken:
		if overwrite == "skip":
			return JobResult(job, "exists", out, reason="exists: {0}".format(out), info=info)
		if overwrite == "rename":
			out = _renamed(out, taken)
	taken.add(out)
	if dry_run:
		return JobResult(job, "dry", out, info=info)
	try:
		parent = os.path.dirname(out)
		if parent and not os.path.isdir(parent):
			os.makedirs(parent, exist_ok=True)
		saver(arr, out)
	except Exception as e:
		if on_error == "raise":
			raise
		return JobResult(job, "error", out, reason="{0}: {1}".format(type(e).__name__, e))
	return JobResult(job, "written", out, info=info)
