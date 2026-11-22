# SPDX-License-Identifier: GPL-3.0-only
"""File dialogs: the desktop's own file browser, never a Kivy one (owner 2026-11-22, plan_texture_tools_gui.md R9).

open_browser(mode, on_select, settings=None, filters=("*.png",), title=None, start=None, default_name="",
		on_error=None)
	mode 'file' (one file), 'files' (several), 'folder', 'save' (a path to write; the dialog itself writes nothing).
	Runs the system dialog in a background thread so the app keeps drawing; on_select(paths) is called on the Kivy main
	thread with absolute paths (one entry except in 'files' mode). Cancel calls nothing. The dialog opens in `start`,
	else the Settings start folder (newest recent folder, then the project folder, then home); the chosen folder is
	added to recent folders. Overwrite confirmation in 'save' mode is the dialog's own. One dialog at a time: a second
	request while one is open is refused with a status message. Returns the worker thread, or None when refused.
	on_error(text) gets "no dialog found" and dialog failures; default: the current screen's status line.

Backends (first found on PATH wins): zenity (GTK; GNOME, Unity, Xfce, Cinnamon, MATE) and kdialog (KDE). A KDE or
LXQt session tries kdialog first. TEXTURETOOLS_FILE_DIALOG=zenity|kdialog forces one. When neither is installed the
request fails with a message naming both; there is no built-in fallback browser.
The dialog runs with a cleaned environment (dialog_env): PyInstaller's bundle LD_LIBRARY_PATH is swapped back for the
launcher's, and the GTK / GIO variables a snap-confined parent (VS Code or Codium from snap) points into its own
runtime are dropped, since either makes the host's zenity fail to load.
Tests: set_runner(fn) replaces the subprocess step; fn(request) returns a list of paths, [] or None for cancel.
"""
import os
import shutil
import subprocess
import sys
import threading

MODES = ("file", "files", "folder", "save")
BACKENDS = ("zenity", "kdialog")
DEFAULT_TITLES = {"file": "Open file", "files": "Open files", "folder": "Choose folder", "save": "Save as"}
# GTK / GIO / locale variables a snap parent sets to paths inside its own runtime
SNAP_LEAK_VARS = ("GTK_PATH", "GTK_EXE_PREFIX", "GTK_IM_MODULE_FILE", "GIO_MODULE_DIR", "GDK_PIXBUF_MODULEDIR",
	"GDK_PIXBUF_MODULE_FILE", "LOCPATH", "GSETTINGS_SCHEMA_DIR")
SNAP_ORIG_SUFFIX = "_VSCODE_SNAP_ORIG"

_runner = None
_busy = threading.Lock()


class DialogUnavailable(RuntimeError):
	pass


def set_runner(fn):
	"""Replace the dialog step (tests). fn(request) -> list of paths, or [] / None on cancel. None restores the real
	one."""
	global _runner
	_runner = fn


def is_busy():
	return _busy.locked()


# ---------------- pure helpers (no Kivy) ----------------

def dialog_env(environ=None, frozen=None):
	"""Environment for the dialog child process (see the module doc)."""
	env = dict(os.environ if environ is None else environ)
	frozen = getattr(sys, "frozen", False) if frozen is None else frozen
	if "LD_LIBRARY_PATH_ORIG" in env:
		env["LD_LIBRARY_PATH"] = env.pop("LD_LIBRARY_PATH_ORIG")
	elif frozen:
		env.pop("LD_LIBRARY_PATH", None)
	for key in [k for k in env if k.endswith(SNAP_ORIG_SUFFIX)]:
		base = key[:-len(SNAP_ORIG_SUFFIX)]
		orig = env.pop(key)
		if orig:
			env[base] = orig
		else:
			env.pop(base, None)
	if env.get("SNAP"):
		for key in SNAP_LEAK_VARS:
			if "/snap/" in env.get(key, ""):
				env.pop(key)
	return env


def backend_order(environ=None):
	env = os.environ if environ is None else environ
	forced = (env.get("TEXTURETOOLS_FILE_DIALOG") or "").strip().lower()
	if forced:
		return [forced]
	desk = "{0}:{1}".format(env.get("XDG_CURRENT_DESKTOP", ""), env.get("DESKTOP_SESSION", "")).lower()
	if "kde" in desk or "plasma" in desk or "lxqt" in desk:
		return ["kdialog", "zenity"]
	return ["zenity", "kdialog"]


def find_backend(environ=None, which=None):
	"""(name, executable) of the first dialog found on environ's PATH, or (None, None)."""
	env = os.environ if environ is None else environ
	if which is None:
		which = lambda n: shutil.which(n, path=env.get("PATH", os.defpath))  # noqa: E731
	for name in backend_order(environ):
		if name not in BACKENDS:
			continue
		exe = which(name)
		if exe:
			return name, exe
	return None, None


def filter_label(filters):
	exts = [p[2:].upper() if p.startswith("*.") else p for p in filters]
	return "{0} files".format(", ".join(exts)) if exts else "All files"


def _start_target(req):
	start = req.get("start") or os.path.expanduser("~")
	if req["mode"] == "save" and req.get("default_name"):
		return os.path.join(start, req["default_name"])
	return start.rstrip(os.sep) + os.sep


def build_argv(name, exe, req):
	"""argv for one dialog request: req has mode, title, start (a folder), default_name, filters."""
	mode, title, filters = req["mode"], req.get("title") or DEFAULT_TITLES[req["mode"]], tuple(req.get("filters") or ())
	target = _start_target(req)
	if name == "zenity":
		argv = [exe, "--file-selection", "--title=" + title, "--filename=" + target]
		if mode == "files":
			argv += ["--multiple", "--separator=\n"]
		elif mode == "folder":
			argv.append("--directory")
		elif mode == "save":
			argv += ["--save", "--confirm-overwrite"]
		if filters and mode != "folder":
			argv += ["--file-filter={0} | {1}".format(filter_label(filters), " ".join(filters)),
				"--file-filter=All files | *"]
		return argv
	if name == "kdialog":
		flt = "{0}|{1}".format(" ".join(filters), filter_label(filters)) if filters else ""
		if mode == "folder":
			return [exe, "--title", title, "--getexistingdirectory", target]
		if mode == "save":
			return [exe, "--title", title, "--getsavefilename", target] + ([flt] if flt else [])
		argv = [exe, "--title", title, "--getopenfilename", target] + ([flt] if flt else [])
		if mode == "files":
			argv += ["--multiple", "--separate-output"]
		return argv
	raise ValueError("unknown dialog backend {0}".format(name))


def parse_output(mode, stdout):
	paths = [ln for ln in (stdout or "").splitlines() if ln.strip()]
	paths = [os.path.abspath(p) for p in paths]
	return paths if mode == "files" else paths[:1]


def run_dialog(req, environ=None):
	"""Blocking: show the system dialog for req. Returns the chosen paths, [] on cancel. Raises DialogUnavailable
	when no dialog program is installed, RuntimeError when the dialog fails."""
	name, exe = find_backend(environ)
	if name is None:
		raise DialogUnavailable("no system file dialog found: install zenity (GNOME, Xfce, MATE, Cinnamon) "
			"or kdialog (KDE)")
	r = subprocess.run(build_argv(name, exe, req), env=dialog_env(environ), capture_output=True, text=True)
	if r.returncode == 1:  # Cancel or window closed (both tools)
		return []
	if r.returncode != 0:
		tail = (r.stderr or "").strip().splitlines()[-1:]
		raise RuntimeError("{0} failed (exit {1}){2}".format(name, r.returncode, ": " + tail[0] if tail else ""))
	return parse_output(req["mode"], r.stdout)


def start_folder(start, settings):
	if start and not os.path.isdir(start):
		start = os.path.dirname(start)
	if start and os.path.isdir(start):
		return os.path.abspath(start)
	if settings is not None:
		return settings.start_folder()
	return os.path.expanduser("~")


# ---------------- Kivy side ----------------

def _settings(settings):
	if settings is not None:
		return settings
	from kivy.app import App
	return getattr(App.get_running_app(), "settings", None)


def _report(text):
	from kivy.app import App
	from kivy.logger import Logger
	Logger.warning("texturetools: file dialog: %s", text)
	app = App.get_running_app()
	scr = None
	if app is not None and hasattr(app, "current_tool"):
		scr = app.current_tool()
	if scr is not None and hasattr(scr, "set_status"):
		scr.set_status(text)


def open_browser(mode, on_select, settings=None, filters=("*.png",), title=None, start=None, default_name="",
		on_error=None):
	if mode not in MODES:
		raise ValueError("mode must be one of {0}".format(MODES))
	from kivy.clock import Clock
	report = on_error or _report
	if not _busy.acquire(blocking=False):
		report("a file dialog is already open")
		return None
	s = _settings(settings)
	req = {"mode": mode, "title": title or DEFAULT_TITLES[mode], "start": start_folder(start, s),
		"default_name": default_name or "", "filters": tuple(filters or ()) if mode != "folder" else ()}

	def finish(paths, error):
		if error:
			report(error)
			return
		if not paths:
			return
		if s is not None:
			s.add_recent(paths[0])
		on_select(paths)

	def work():
		paths, error = None, None
		try:
			paths = (_runner or run_dialog)(req)
		except Exception as e:  # noqa: BLE001 - shown to the user, never raised into the UI thread
			error = str(e)
		finally:
			_busy.release()
		Clock.schedule_once(lambda dt: finish(paths, error), 0)

	t = threading.Thread(target=work, name="tt-file-dialog", daemon=True)
	t.start()
	return t
