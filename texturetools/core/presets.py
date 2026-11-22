# SPDX-License-Identifier: GPL-3.0-only
"""JSON presets for every screen and the "Copy as CLI" string builder (plan_texture_tools_gui.md section 8).

No Kivy import. A preset is one JSON file <base>/<screen>/<name>.json:
	{"screen": "swap", "version": 1, "params": {...}}
params is the screen's params dict (the same dict the engine call is built from). Values are made JSON-safe on save
(tuples -> lists, numpy scalars and arrays -> Python values); arrays larger than a palette belong in files, not presets.

Copy as CLI: each screen registers a builder that turns its params dict into an argv list
(register_cli_builder(screen, fn)); cli_string(screen, params) shell-quotes it. argv_from_spec() covers the common
case of positional keys plus a flag map.
"""
import json
import os
import re
import shlex

PRESET_VERSION = 1
_NAME_RE = re.compile(r"[^A-Za-z0-9 _.\-]+")


def _jsonable(v):
	try:
		import numpy as np
		if isinstance(v, np.ndarray):
			return v.tolist()
		if isinstance(v, np.generic):
			return v.item()
	except ImportError:  # pragma: no cover
		pass
	if isinstance(v, dict):
		return {str(k): _jsonable(x) for k, x in v.items()}
	if isinstance(v, (list, tuple, set)):
		return [_jsonable(x) for x in v]
	return v


def safe_name(name):
	"""File-safe preset name: letters, digits, space, _ . -; never empty, never a path."""
	n = _NAME_RE.sub("_", (name or "").strip()).strip(". ")
	return n or "preset"


def preset_dir(base, screen):
	return os.path.join(base, safe_name(screen))


def preset_path(base, screen, name):
	return os.path.join(preset_dir(base, screen), safe_name(name) + ".json")


def save_preset(base, screen, name, params):
	"""Write a preset (explicit user action only). Returns the path."""
	d = preset_dir(base, screen)
	os.makedirs(d, exist_ok=True)
	path = preset_path(base, screen, name)
	doc = {"screen": screen, "version": PRESET_VERSION, "params": _jsonable(params)}
	tmp = path + ".tmp"
	with open(tmp, "w", encoding="utf-8") as f:
		json.dump(doc, f, indent="\t", sort_keys=True)
		f.write("\n")
	os.replace(tmp, path)
	return path


def load_preset(base, screen, name):
	"""params dict of a preset; ValueError when the file is for another screen or malformed."""
	with open(preset_path(base, screen, name), encoding="utf-8") as f:
		doc = json.load(f)
	if not isinstance(doc, dict) or doc.get("screen") != screen or not isinstance(doc.get("params"), dict):
		raise ValueError("not a {0} preset: {1}".format(screen, name))
	return doc["params"]


def list_presets(base, screen):
	d = preset_dir(base, screen)
	if not os.path.isdir(d):
		return []
	return sorted(f[:-5] for f in os.listdir(d) if f.endswith(".json"))


def delete_preset(base, screen, name):
	path = preset_path(base, screen, name)
	if os.path.exists(path):
		os.remove(path)
		return True
	return False


# ---- Copy as CLI ----

_CLI_BUILDERS = {}


def register_cli_builder(screen, fn):
	"""fn(params) -> argv list (strings), e.g. ["python3", "-m", "texturetools.cli.palette_swap", "a.png", ...]."""
	_CLI_BUILDERS[screen] = fn


def has_cli_builder(screen):
	return screen in _CLI_BUILDERS


def cli_argv(screen, params):
	fn = _CLI_BUILDERS.get(screen)
	if fn is None:
		return None
	return [str(a) for a in fn(params)]


def cli_string(screen, params):
	"""Shell-quoted command line for params, or None when the screen has no builder."""
	argv = cli_argv(screen, params)
	return None if argv is None else " ".join(shlex.quote(a) for a in argv)


def argv_from_spec(module, params, positional=(), flags=None, python="python3"):
	"""Common builder: python3 -m <module> <positional values> <flags>.

	positional: params keys in order (list values expand to several args; empty or None values are left out).
	flags: {param key: "--flag"}; True -> bare flag, False/None/"" -> left out, list -> flag followed by each value,
	anything else -> flag value. Flags are emitted in the dict's order.
	"""
	argv = [python, "-m", module]
	for key in positional:
		v = params.get(key)
		if v is None or v == "":
			continue
		if isinstance(v, (list, tuple)):
			argv.extend(str(x) for x in v)
		else:
			argv.append(str(v))
	for key, flag in (flags or {}).items():
		v = params.get(key)
		if v is None or v is False or v == "" or v == []:
			continue
		if v is True:
			argv.append(flag)
		elif isinstance(v, (list, tuple)):
			argv.append(flag)
			argv.extend(str(x) for x in v)
		else:
			argv.extend([flag, str(v)])
	return argv
