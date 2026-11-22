# SPDX-License-Identifier: GPL-3.0-only
"""System file dialog and folding sidebar sections (plan_texture_tools_gui.md R9, R10, P11).

Part A (no display): dialog_env (PyInstaller LD_LIBRARY_PATH restore, snap GTK variables dropped, *_VSCODE_SNAP_ORIG
restored), backend order (KDE session, forced), find_backend, argv for every mode on zenity and kdialog, output
parsing, run_dialog end to end against fake zenity scripts on a temp PATH (choose, cancel, failure, none installed),
and the source holds no Kivy file chooser.
Part B (xvfb child, the owner's DISPLAY is never used): every tool screen's sidebar is a SectionPanel; first section
open, the rest folded; a folded section's widgets are out of the widget tree; toggles persist per screen in the
Settings store and come back on a new panel; combine's "Selected layer" starts open and keeps its fold sign when its
text changes; expand / collapse all; open_browser through set_runner calls on_select on the main thread, adds the
recent folder, refuses a second dialog while one is open, reports a failure on the status line; FileSlot.browse sets
its path through the runner. Nothing is written outside the temp dir.

Usage: python3 texturetools/tests/test_gui_dialogs_sections.py [-v]
"""
import os
import shutil
import stat
import subprocess
import sys
import tempfile
import threading
import time

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
if ROOT not in sys.path:
	sys.path.insert(0, ROOT)

VERBOSE = "-v" in sys.argv
CHILD_FLAG = "--gui-child"
FAILS = []
OKS = [0]


def check(cond, what):
	if not cond:
		FAILS.append(what)
		print("FAIL " + what)
	else:
		OKS[0] += 1
		if VERBOSE:
			print("ok   " + what)


# ---------------- part A ----------------

def _fake(bin_dir, name, body):
	p = os.path.join(bin_dir, name)
	with open(p, "w") as f:
		f.write("#!/bin/sh\n" + body + "\n")
	os.chmod(p, os.stat(p).st_mode | stat.S_IXUSR)
	return p


def part_a(tmp):
	from texturetools.gui.widgets import filebrowser as fb

	# dialog_env
	env = fb.dialog_env({"LD_LIBRARY_PATH": "/bundle/_internal", "LD_LIBRARY_PATH_ORIG": "/usr/lib/x"}, frozen=True)
	check(env.get("LD_LIBRARY_PATH") == "/usr/lib/x" and "LD_LIBRARY_PATH_ORIG" not in env,
		"env: PyInstaller LD_LIBRARY_PATH_ORIG restored")
	env = fb.dialog_env({"LD_LIBRARY_PATH": "/bundle/_internal"}, frozen=True)
	check("LD_LIBRARY_PATH" not in env, "env: frozen without _ORIG drops the bundle path")
	env = fb.dialog_env({"LD_LIBRARY_PATH": "/opt/lib"}, frozen=False)
	check(env.get("LD_LIBRARY_PATH") == "/opt/lib", "env: dev run keeps LD_LIBRARY_PATH")
	env = fb.dialog_env({"SNAP": "/snap/codium/1", "GTK_PATH": "/snap/codium/1/usr/lib/gtk-3.0",
		"LOCPATH": "/snap/codium/1/usr/lib/locale", "GDK_PIXBUF_MODULE_FILE": "/home/u/snap/codium/common/c",
		"GTK_IM_MODULE": "ibus", "XDG_DATA_DIRS": "/home/u/snap/codium/1/share",
		"XDG_DATA_DIRS_VSCODE_SNAP_ORIG": "/usr/share", "GDK_BACKEND_VSCODE_SNAP_ORIG": ""}, frozen=False)
	check(not any(k in env for k in ("GTK_PATH", "LOCPATH", "GDK_PIXBUF_MODULE_FILE")), "env: snap GTK vars dropped")
	check(env.get("GTK_IM_MODULE") == "ibus", "env: unrelated GTK var kept")
	check(env.get("XDG_DATA_DIRS") == "/usr/share" and "GDK_BACKEND" not in env
		and not any(k.endswith("_VSCODE_SNAP_ORIG") for k in env), "env: *_VSCODE_SNAP_ORIG restored")
	env = fb.dialog_env({"GTK_PATH": "/snap/x/gtk"}, frozen=False)
	check(env.get("GTK_PATH") == "/snap/x/gtk", "env: no SNAP parent, GTK vars untouched")

	# backend order
	check(fb.backend_order({"XDG_CURRENT_DESKTOP": "Unity"}) == ["zenity", "kdialog"], "order: GTK desktop")
	check(fb.backend_order({"XDG_CURRENT_DESKTOP": "KDE"})[0] == "kdialog", "order: KDE session")
	check(fb.backend_order({"TEXTURETOOLS_FILE_DIALOG": "kdialog"}) == ["kdialog"], "order: forced")
	found = {"kdialog": "/usr/bin/kdialog"}
	check(fb.find_backend({"XDG_CURRENT_DESKTOP": "GNOME"}, which=found.get) == ("kdialog", "/usr/bin/kdialog"),
		"find: falls through to kdialog")
	check(fb.find_backend({}, which=lambda n: None) == (None, None), "find: none installed")
	check(fb.find_backend({"TEXTURETOOLS_FILE_DIALOG": "nautilus"}, which=lambda n: "/x") == (None, None),
		"find: unknown forced backend ignored")

	# argv
	req = {"mode": "file", "title": "Pick", "start": "/data", "default_name": "", "filters": ("*.png",)}
	a = fb.build_argv("zenity", "zenity", req)
	check(a[:4] == ["zenity", "--file-selection", "--title=Pick", "--filename=/data/"]
		and "--file-filter=PNG files | *.png" in a and "--file-filter=All files | *" in a, "zenity file argv")
	a = fb.build_argv("zenity", "zenity", dict(req, mode="files"))
	check("--multiple" in a and "--separator=\n" in a, "zenity files argv")
	a = fb.build_argv("zenity", "zenity", dict(req, mode="folder", filters=()))
	check("--directory" in a and not any(x.startswith("--file-filter") for x in a), "zenity folder argv")
	a = fb.build_argv("zenity", "zenity", dict(req, mode="save", default_name="out.png"))
	check("--save" in a and "--confirm-overwrite" in a and "--filename=/data/out.png" in a, "zenity save argv")
	a = fb.build_argv("kdialog", "kdialog", dict(req, filters=("*.gpl", "*.hex")))
	check(a == ["kdialog", "--title", "Pick", "--getopenfilename", "/data/", "*.gpl *.hex|GPL, HEX files"],
		"kdialog file argv")
	a = fb.build_argv("kdialog", "kdialog", dict(req, mode="files"))
	check(a[-2:] == ["--multiple", "--separate-output"], "kdialog files argv")
	a = fb.build_argv("kdialog", "kdialog", dict(req, mode="folder", filters=()))
	check(a == ["kdialog", "--title", "Pick", "--getexistingdirectory", "/data/"], "kdialog folder argv")
	a = fb.build_argv("kdialog", "kdialog", dict(req, mode="save", default_name="p.gpl"))
	check(a[3:5] == ["--getsavefilename", "/data/p.gpl"], "kdialog save argv")
	check(fb.parse_output("files", "/a/x.png\n/a/y.png\n") == ["/a/x.png", "/a/y.png"], "parse: several")
	check(fb.parse_output("file", "/a/x.png\n/a/y.png\n") == ["/a/x.png"], "parse: one")
	check(fb.parse_output("file", "\n") == [], "parse: empty")

	# run_dialog against fake dialogs
	bin_dir = os.path.join(tmp, "bin")
	os.makedirs(bin_dir)
	args_log = os.path.join(tmp, "args.txt")
	base = {"PATH": bin_dir, "XDG_CURRENT_DESKTOP": "GNOME"}
	_fake(bin_dir, "zenity", 'printf "%s\\n" "$@" > "{0}"\necho /picked/one.png'.format(args_log))
	got = fb.run_dialog(dict(req), environ=base)
	check(got == ["/picked/one.png"], "run: chosen path returned ({0})".format(got))
	with open(args_log) as f:
		logged = f.read().splitlines()
	check("--file-selection" in logged and "--title=Pick" in logged, "run: argv reached the dialog")
	_fake(bin_dir, "zenity", "exit 1")
	check(fb.run_dialog(dict(req), environ=base) == [], "run: cancel gives []")
	_fake(bin_dir, "zenity", "echo 'Gtk-WARNING: cannot open display' >&2\nexit 255")
	try:
		fb.run_dialog(dict(req), environ=base)
		check(False, "run: failure raises")
	except RuntimeError as e:
		check("exit 255" in str(e) and "cannot open display" in str(e), "run: failure message ({0})".format(e))
	os.remove(os.path.join(bin_dir, "zenity"))
	try:
		fb.run_dialog(dict(req), environ=base)
		check(False, "run: none installed raises")
	except fb.DialogUnavailable as e:
		check("zenity" in str(e) and "kdialog" in str(e), "run: none installed names both tools")
	check(fb.start_folder(os.path.join(tmp, "gone", "nope.png"), None) == os.path.expanduser("~"),
		"start: missing folder falls back to home")
	check(fb.start_folder(os.path.join(tmp, "nope.png"), None) == tmp, "start: missing file opens its folder")
	check(fb.start_folder(args_log, None) == tmp, "start: a file path opens its folder")

	with open(fb.__file__) as f:
		src = f.read()
	check("FileChooser" not in src and "Popup" not in src, "no Kivy file chooser or popup left")


# ---------------- part B ----------------

def _child_env(tmp, driver):
	env = dict(os.environ)
	env.pop("WAYLAND_DISPLAY", None)
	env["TT_SMOKE_PARENT_DISPLAY"] = os.environ.get("DISPLAY", "")
	env["KIVY_HOME"] = os.path.join(tmp, "kivyhome")
	env["TEXTURETOOLS_CONFIG_DIR"] = os.path.join(tmp, "ttcfg")
	env["TT_SMOKE_TMP"] = tmp
	env["KIVY_NO_ARGS"] = "1"
	env["KIVY_NO_CONSOLELOG"] = "0" if VERBOSE else "1"
	env["KIVY_WINDOW"] = "sdl2"
	env["SDL_VIDEODRIVER"] = driver
	env["SDL_AUDIODRIVER"] = "dummy"
	return env


def part_b(tmp):
	me = os.path.abspath(__file__)
	attempts = []
	if shutil.which("xvfb-run"):
		attempts.append(("xvfb-run", ["xvfb-run", "-a", "-s", "-screen 0 1280x800x24", sys.executable, me, CHILD_FLAG],
			"x11"))
	attempts.append(("sdl-offscreen", [sys.executable, me, CHILD_FLAG], "offscreen"))
	for label, cmd, driver in attempts:
		env = _child_env(tmp, driver)
		if driver == "offscreen":
			env.pop("DISPLAY", None)
		try:
			r = subprocess.run(cmd + (["-v"] if VERBOSE else []), env=env, capture_output=True, text=True, timeout=120)
		except subprocess.TimeoutExpired:
			print("{0}: timeout".format(label))
			continue
		out = (r.stdout or "") + (r.stderr or "")
		if "GUI_P11_RESULT" in out:
			res = [ln for ln in out.splitlines() if ln.startswith(("FAIL", "GUI_P11_RESULT"))]
			if VERBOSE:
				print(out[-4000:])
			for ln in res:
				if ln.startswith("FAIL"):
					FAILS.append(ln)
			print("part B backend: {0} -> {1}".format(label, res[-1] if res else "?"))
			if r.returncode != 0 and not any(ln.startswith("FAIL") for ln in res):
				FAILS.append("child exit {0}".format(r.returncode))
			return True
		print("{0}: no display backend (exit {1}): {2}".format(label, r.returncode, out.strip().splitlines()[-1:]))
	print("SKIP part B: no display backend available")
	return False


def child():
	parent = os.environ.get("TT_SMOKE_PARENT_DISPLAY", "")
	if os.environ.get("SDL_VIDEODRIVER") == "x11" and (not os.environ.get("DISPLAY")
			or os.environ.get("DISPLAY") == parent):
		print("refusing to open a window on the parent's DISPLAY")
		return 3
	tmp = os.environ["TT_SMOKE_TMP"]
	img_dir = os.path.join(tmp, "picked")
	os.makedirs(img_dir, exist_ok=True)
	picked = os.path.join(img_dir, "chosen.png")

	from texturetools.gui.app import TextureToolsApp
	from texturetools.gui.screens.base import ToolScreen
	from texturetools.gui.widgets import filebrowser as fb
	from texturetools.gui.widgets.sections import Section, SectionPanel, SettingsSectionState
	from kivy.base import ExceptionHandler, ExceptionManager
	from kivy.clock import Clock

	class Record(ExceptionHandler):
		def handle_exception(self, inst):
			import traceback
			check(False, "exception in loop: {0!r}\n{1}".format(inst, traceback.format_exc()))
			a = TextureToolsApp.get_running_app()
			if a:
				a.stop()
			return ExceptionManager.PASS

	ExceptionManager.add_handler(Record())
	app = TextureToolsApp(start_screen="swap")

	def steps():
		yield 0.3
		tools = [s for s in app.screens.values() if isinstance(s, ToolScreen)]
		check(len(tools) >= 5, "tool screens built ({0})".format(len(tools)))
		for scr in tools:
			p = scr.controls
			check(isinstance(p, SectionPanel), scr.tool_name + ": sidebar is a SectionPanel")
			secs = list(p.sections.values())
			check(len(secs) >= 2, scr.tool_name + ": {0} sections".format(len(secs)))
			if not secs:
				continue
			check(secs[0].is_open and secs[0].body.parent is secs[0], scr.tool_name + ": first section open")
			folded = [s for s in secs[1:] if not s.is_open]
			if scr.tool_name != "combine":
				check(len(folded) == len(secs) - 1, scr.tool_name + ": later sections folded")
			for s in folded:
				check(s.body.parent is None and s.header.text.startswith("+"),
					scr.tool_name + ": folded " + s.title + " detached, shows +")
				for w in s.body.children:
					check(w.get_root_window() is None, scr.tool_name + ": folded widget out of the tree")
					break
			check(all(isinstance(s, Section) for s in secs), scr.tool_name + ": sections are Section widgets")

		# toggles persist per screen
		swap = app.switch_to("swap")
		yield 0.1
		mapping = swap.controls.sections["Mapping"]
		mapping.header.dispatch("on_release")
		yield 0.05
		check(mapping.is_open and mapping.body.parent is mapping and mapping.header.text.startswith("-"),
			"swap: click opens Mapping")
		stored = app.settings.get("open_sections") or {}
		check(stored.get("swap", {}).get("Mapping") is True, "swap: open state stored ({0})".format(stored))
		swap.controls.sections["Inputs"].toggle()
		check((app.settings.get("open_sections") or {}).get("swap", {}).get("Inputs") is False,
			"swap: folded state stored")
		again = SectionPanel(state=SettingsSectionState(app.settings, "swap"))
		check(again.begin_section("Inputs").is_open is False and again.begin_section("Mapping").is_open is True,
			"swap: stored state comes back on a new panel")
		other = SectionPanel(state=SettingsSectionState(app.settings, "paint"))
		other.begin_section("Inputs")
		check(other.sections["Inputs"].is_open, "state is per screen")
		swap.controls.expand_all()
		check(all(s.is_open for s in swap.controls.sections.values()), "expand all")
		swap.controls.collapse_all()
		check(not any(s.is_open for s in swap.controls.sections.values()), "collapse all")
		swap.controls.sections["Inputs"].set_open(True)

		# combine: Selected layer open, text update keeps the fold sign
		comb = app.screens["combine"]
		sl = comb.controls.sections.get("Selected layer")
		check(sl is not None and sl.is_open, "combine: Selected layer starts open")
		if sl is not None:
			sl.text = "[b]Selected layer 2[/b]"
			check(sl.header.text == "-  [b]Selected layer 2[/b]" and sl.title == "Selected layer",
				"combine: renamed header keeps sign and state key")

		# open_browser through the runner
		calls = []
		seen = []

		def runner(req):
			calls.append((req, threading.current_thread().name))
			return [picked]

		fb.set_runner(runner)
		fb.open_browser("file", lambda paths: seen.append((paths, threading.current_thread().name)),
			title="Pick one", start=img_dir)
		t0 = time.time()
		while not seen and time.time() - t0 < 3:
			yield 0.05
		check(seen and seen[0][0] == [picked], "dialog: on_select got the path ({0})".format(seen))
		check(seen and seen[0][1] == "MainThread", "dialog: on_select on the main thread")
		check(calls and calls[0][1] == "tt-file-dialog" and calls[0][0]["title"] == "Pick one"
			and calls[0][0]["start"] == img_dir, "dialog: request ran off the main thread")
		check(app.settings.recent_folders()[:1] == [img_dir], "dialog: recent folder added")

		gate = threading.Event()
		fb.set_runner(lambda req: (gate.wait(5), [])[1])
		first = fb.open_browser("file", lambda p: None)
		swap.set_status("")
		second = fb.open_browser("file", lambda p: None)
		check(first is not None and second is None and "already open" in swap.status.text,
			"dialog: second request refused while one is open")
		gate.set()
		first.join(3)
		yield 0.1
		check(not fb.is_busy(), "dialog: free again after close")

		def failing(req):
			raise fb.DialogUnavailable("no system file dialog found: install zenity or kdialog")

		fb.set_runner(failing)
		fb.open_browser("save", lambda p: check(False, "dialog: on_select not called on failure"))
		yield 0.2
		check("no system file dialog" in swap.status.text, "dialog: failure shown on the status line")

		fb.set_runner(lambda req: [picked] if req["mode"] == "file" else [])
		swap.slots["source"].browse()
		t0 = time.time()
		while swap.slots["source"].path != picked and time.time() - t0 < 3:
			yield 0.05
		check(swap.slots["source"].path == picked and swap.params.get("source") == picked,
			"file slot Browse sets path and param")
		fb.set_runner(None)
		check(not os.listdir(img_dir), "nothing written next to the picked path")

	gen = steps()

	def tick(dt):
		try:
			wait = next(gen)
		except StopIteration:
			app.stop()
			return
		except Exception as e:
			import traceback
			check(False, "step raised {0!r}\n{1}".format(e, traceback.format_exc()))
			app.stop()
			return
		Clock.schedule_once(tick, wait)

	Clock.schedule_once(tick, 0.2)
	Clock.schedule_once(lambda dt: (check(False, "timeout"), app.stop()), 60)
	app.run()
	print("GUI_P11_RESULT {0} ({1} checks ok)".format("FAIL" if FAILS else "PASS", OKS[0]))
	return 1 if FAILS else 0


def main():
	if CHILD_FLAG in sys.argv:
		return child()
	tmp = tempfile.mkdtemp(prefix="tt_gui_p11_")
	try:
		part_a(tmp)
		print("part A: {0} failed ({1} ok)".format(len(FAILS), OKS[0]))
		part_b(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("texturetools gui dialogs/sections: {0} failed".format(len(FAILS)))
	return 1 if FAILS else 0


if __name__ == "__main__":
	sys.exit(main())
