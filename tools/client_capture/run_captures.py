#!/usr/bin/env python3
"""Client capture runner (feature_writeup/client_capture.md 2.8): spec files -> capture plan -> throwaway client
running the cdevcapture driver -> crop.py -> test_captures/judgement/<run>/ for the owner to judge.

    run_captures.py --list [GROUP ...] [--check]          tests per group (captures / text readback); --check: specs
                                                           parse, ids unique, item_dict keys resolve, covers in the queue
    run_captures.py GROUP ... [--only ID,ID] [--jar PATH]... [--fresh-config] [--game DIR] [--plan-only]
                    [--display real|xvfb]
                                                           a run; --plan-only writes the run folder and plan, no launch
    run_captures.py --resume RUN                          continue a run after the runner itself stopped
    run_captures.py --recrop RUN                          refresh run.json from the specs and re-crop raw/ (no game)
    run_captures.py serve [--game DIR] [--join HOST:PORT] [--ownsrv PID] [--jar PATH]... [--world NAME]
                          [--drop GLOB]... [--config FILE:SECTION:KEY=VALUE]... [--display real|xvfb]
                                                           --drop: unlink matching jars from GAME/mods; --config: set
                                                           a toml key in GAME (VALUE is raw toml); both per launch
                                                           start a client whose driver serves the file channel
                                                           GAME/cdev_channel (qrun.py client drives it), then return
    run_captures.py stop [--game DIR]                     ask the serving driver to quit the client

Specs: feature_writeup/testing/capture_specs/<GROUP>.json (+ _macros.json). Output: test_captures/judgement/<run>/,
git-ignored and wiped after the owner has judged it; no tool or agent gives a visual verdict there (plan
scratch_testing_expansion 0). Game dir default ~/.cache/ce_capture/game (eval_client.py setup, then the driver jar
from development/creativeDevtools/build/libs swapped in, the perf-eval scripts removed, options.txt for captures).

--display xvfb is EXPERIMENTAL (TX-07, owner D3): an own Xvfb per launch, stopped with the client. Only for tests
that cannot run otherwise (unattended blocks); the real window stays the default and is required for color rows.
run.json records the display, so the owner sees it when judging.

One-JVM rule: refuses to start while any game JVM is up (prints pid and cwd only). Only ever kills the pid it
launched. Never touches the instance's mods/, logs/, saves/ or config/.
"""
import contextlib
import glob
import hashlib
import io
import json
import os
import re
import shutil
import signal
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
INST = os.path.abspath(os.path.join(HERE, "../.."))
SPECS = os.path.join(INST, "feature_writeup/testing/capture_specs")
QUEUE = os.path.join(INST, "feature_writeup/testing/scratch_queue.md")
OUT = os.environ.get("CE_CAPTURE_OUT") or os.path.join(INST, "test_captures/judgement")  # env: offline tests
GAME = os.path.expanduser("~/.cache/ce_capture/game")
DRIVER_JAR = os.path.join(INST, "development/creativeDevtools/build/libs/creativedevtools-1.0.0.jar")
sys.path.insert(0, os.path.join(INST, "tools/perf/eval"))
sys.path.insert(0, os.path.join(INST, "tools"))
sys.path.insert(0, HERE)

PLOT_STEP = 48
ORIGIN_Y = -60  # standing height on the default superflat
BOOT_TIMEOUT_S = 900
STEP_KEYS = {
	"cmd", "look", "wait", "slot", "use", "attack", "screen", "tooltip", "hover", "key", "reload", "macro",
	# TX-03 readback
	"assert_screen", "screen_info", "slot_item", "tooltip_text", "player_pos", "player_motion", "held", "effects",
	"target", "widgets",
	# TX-04 movement
	"walk", "sneak", "jump", "fly",
	# TX-05 widgets
	"click_slot", "click_widget", "type_into", "scroll", "click", "press",
}
OPTIONS = {"guiScale": "2", "renderDistance": "6", "simulationDistance": "6", "pauseOnLostFocus": "false",
		   "fullscreen": "false", "narrator": "0", "tutorialStep": "none", "joinedFirstServer": "true",
		   "skipMultiplayerWarnings": "true", "soundCategory_master": "0.0", "gamma": "0.5", "fov": "0.0",
		   "overrideWidth": "1280", "overrideHeight": "720", "lang": "en_us", "entityShadows": "true",
		   "maxFps": "120"}


# --- specs --------------------------------------------------------------------------------------------------

def spec_path(group):
	return os.path.join(SPECS, group + ".json")


def all_groups():
	return sorted(os.path.basename(p)[:-5] for p in glob.glob(os.path.join(SPECS, "*.json"))
				  if not os.path.basename(p).startswith("_"))


def load_group(group):
	with open(spec_path(group)) as f:
		g = json.load(f)
	if g.get("group", group) != group:
		raise ValueError(f"{spec_path(group)}: group {g.get('group')!r} does not match the file name")
	return g


def macros():
	p = os.path.join(SPECS, "_macros.json")
	if not os.path.exists(p):
		return {}
	with open(p) as f:
		return {k: v for k, v in json.load(f).items() if not k.startswith("_")}


_ITEM_DICT = None


def item_dict():
	global _ITEM_DICT
	if _ITEM_DICT is None:
		import pack_paths
		_ITEM_DICT = pack_paths.parse_item_dict()[1]
	return _ITEM_DICT


def resolve_value(v, origin, errors):
	if isinstance(v, str):
		def xyz(m):
			base = origin["XYZ".index(m.group(1))]
			return str(base + int(m.group(2) or 0))
		v = re.sub(r"\$([XYZ])([+-]\d+)?", xyz, v)

		def key(m):
			k = m.group(1)
			got = item_dict().get(k)
			if not got:
				errors.append(f"item_dict:{k} not in item_dict")
				return m.group(0)
			return got
		return re.sub(r"item_dict:([A-Za-z0-9_]+)", key, v)
	if isinstance(v, list):
		return [resolve_value(x, origin, errors) for x in v]
	if isinstance(v, dict):
		return {k: resolve_value(x, origin, errors) for k, x in v.items()}
	return v


def expand_macros(steps, mac, errors, depth=0):
	out = []
	for s in steps:
		if isinstance(s, dict) and "macro" in s:
			if depth > 5 or s["macro"] not in mac:
				errors.append(f"macro {s['macro']!r} unknown or nested too deep")
				continue
			out += expand_macros(mac[s["macro"]], mac, errors, depth + 1)
		else:
			out.append(s)
	return out


def resolve(groups, only=None):
	"""Spec tests -> resolved test dicts (plots allocated in run order) and a list of errors."""
	mac, tests, errors, seen = macros(), [], [], set()
	world = None
	for group in groups:
		g = load_group(group)
		w = g.get("world", "flat_creative")
		if world is None:
			world = {"name": w, "biome": g.get("biome", "minecraft:plains"), "seed": g.get("seed", 1)}
		elif world["name"] != w:
			errors.append(f"{group}: world {w!r} differs from {world['name']!r}; run the groups separately")
		for t in g.get("tests", []):
			if only and t["id"] not in only:
				continue
			if t["id"] in seen:
				errors.append(f"{group}: duplicate id {t['id']}")
			seen.add(t["id"])
			if not t.get("expect"):
				errors.append(f"{t['id']}: expect is required")
			origin = [len(tests) * PLOT_STEP, g.get("origin_y", ORIGIN_Y), 0]
			errs = []
			steps = resolve_value(expand_macros(t.get("steps", []), mac, errs), origin, errs)
			for s in steps:
				if not isinstance(s, dict) or not (set(s) & STEP_KEYS):
					errs.append(f"unknown step {s}")
			errors += [f"{t['id']}: {e}" for e in errs]
			tests.append({"id": t["id"], "covers": t.get("covers", []), "title": t.get("title", ""),
						  "expect": t.get("expect", ""), "group": group, "spec": os.path.relpath(spec_path(group), INST),
						  "origin": origin, "steps": steps, "capture": t.get("capture", []),
						  "client_config": g.get("client_config", {})})
	return world or {"name": "flat_creative"}, tests, errors


def resolve_each(groups):
	"""Each group on its own (as --list shows them; plots restart per group)."""
	tests, errors = [], []
	for g in groups:
		_, t, e = resolve([g])
		tests += t
		errors += e
	return tests, errors


def check(groups):
	tests, errors = resolve_each(groups)
	queue = open(QUEUE).read()
	warn = []
	for t in tests:
		for c in t["covers"]:
			if f"| {c} |" not in queue:
				errors.append(f"{t['id']}: covers {c}, which is not a queue row")
	for t in tests:
		hit = subprocess.run(["grep", "-rqw", "--include=*.md", t["id"], os.path.join(INST, "feature_writeup")])
		if hit.returncode:
			warn.append(f"{t['id']}: not found in any writeup")
	return tests, errors, warn


# --- game dir -----------------------------------------------------------------------------------------------

def game_jvms():
	return subprocess.run(["bash", os.path.join(INST, "tools/scratch_query/game_jvms.sh")],
						  capture_output=True, text=True).stdout.strip()


def preflight(ownsrv=None):
	live = "\n".join(l for l in game_jvms().splitlines() if not (ownsrv and l.split()[0] == str(ownsrv)))
	if live:
		sys.exit("refusing to start: a game JVM is up (pid cwd):\n" + live)
	if not os.path.exists(DRIVER_JAR):
		sys.exit(f"driver jar missing: {DRIVER_JAR} (./gradlew :creativeDevtools:build --offline)")


def md5(p):
	h = hashlib.md5()
	with open(p, "rb") as f:
		for b in iter(lambda: f.read(1 << 20), b""):
			h.update(b)
	return h.hexdigest()


def swap_jar(game, src):
	"""Replace a hard-linked jar in GAME/mods by a copy of SRC: unlink first, never write through the link."""
	mods = os.path.join(game, "mods")
	name = os.path.basename(src)
	stem = re.split(r"-\d", name, 1)[0]
	for j in os.listdir(mods):
		if j == name or re.split(r"-\d", j, 1)[0] == stem:
			os.unlink(os.path.join(mods, j))
	shutil.copy2(src, os.path.join(mods, name))


def toml_set(path, section, key, value):
	lines = open(path).read().splitlines() if os.path.exists(path) else []
	out, sec, done = [], None, False
	for l in lines:
		m = re.match(r"\s*\[([^\]]+)\]\s*$", l)
		if m:
			if sec == section and not done:
				out.append(f"\t{key} = {value}")
				done = True
			sec = m.group(1).strip()
		elif sec == section and not done and re.match(rf"\s*{re.escape(key)}\s*=", l):
			out.append(re.sub(r"=.*", "= " + value, l, count=1))
			done = True
			continue
		out.append(l)
	if not done:
		if sec != section:
			out.append(f"\n[{section}]")
		out.append(f"\t{key} = {value}")
	open(path, "w").write("\n".join(out) + "\n")


def toml_value(v):
	if isinstance(v, bool):
		return "true" if v else "false"
	if isinstance(v, (int, float)):
		return str(v)
	if isinstance(v, list):
		return "[" + ", ".join(toml_value(x) for x in v) + "]"
	return json.dumps(v)


def prepare_game(game, jars=(), fresh_config=False, client_config=None, world=None):
	"""eval_client setup + capture specifics. Returns {jar name: md5} of our jars in GAME/mods."""
	import eval_client
	with contextlib.redirect_stdout(io.StringIO()):
		eval_client.setup(game)
	for s in ("kubejs/client_scripts/zz_perfeval_fps.js", "kubejs/server_scripts/zz_perfeval_drive.js"):
		p = os.path.join(game, s)
		if os.path.exists(p):
			os.remove(p)  # the perf drive script switches to spectator and teleports: never in a capture run
	for j in [DRIVER_JAR, *jars]:
		swap_jar(game, j)
	if fresh_config:
		shutil.rmtree(os.path.join(game, "config"), ignore_errors=True)
		os.makedirs(os.path.join(game, "config"))
	for rel, sections in (client_config or {}).items():
		for sec, kv in sections.items():
			for k, v in kv.items():
				toml_set(os.path.join(game, rel), sec, k, toml_value(v))
	opts_p = os.path.join(game, "options.txt")
	lines, seen = [], set()
	for line in open(opts_p):
		k = line.split(":", 1)[0]
		if k in OPTIONS:
			line = f"{k}:{OPTIONS[k]}\n"
			seen.add(k)
		lines.append(line)
	lines += [f"{k}:{v}\n" for k, v in OPTIONS.items() if k not in seen]
	open(opts_p, "w").writelines(lines)
	if world:
		shutil.rmtree(os.path.join(game, "saves", world), ignore_errors=True)
		tpl = os.path.join(HERE, "worlds", world)
		if os.path.isdir(tpl):
			shutil.copytree(tpl, os.path.join(game, "saves", world), ignore=shutil.ignore_patterns("session.lock"))
	return {j: md5(os.path.join(game, "mods", j)) for j in sorted(os.listdir(os.path.join(game, "mods")))
			if j.startswith("creative")}


DISPLAY_MODE = "real"  # --display


def launch(game, plan_path, server=None, ownsrv=None):
	"""eval_client launch with -Dcdevcapture.plan through JAVA_TOOL_OPTIONS; returns the client pid."""
	import eval_client
	old = os.environ.get("JAVA_TOOL_OPTIONS")
	os.environ["JAVA_TOOL_OPTIONS"] = ((old + " ") if old else "") + "-Dcdevcapture.plan=" + plan_path
	buf = io.StringIO()
	try:
		with contextlib.redirect_stdout(buf):
			eval_client.launch(game, server=server, ownsrv=ownsrv, display=DISPLAY_MODE)
	except SystemExit as e:
		sys.exit(f"launch refused: {e}")
	finally:
		if old is None:
			os.environ.pop("JAVA_TOOL_OPTIONS", None)
		else:
			os.environ["JAVA_TOOL_OPTIONS"] = old
	m = re.search(r"client pid (\d+)", buf.getvalue())
	if not m:
		sys.exit("launch printed no pid")
	return int(m.group(1))


def alive(pid):
	try:
		with open(f"/proc/{pid}/stat") as f:
			state = f.read().rsplit(")", 1)[1].split()[0]
	except OSError:
		return False
	if state == "Z":
		with contextlib.suppress(ChildProcessError):
			os.waitpid(pid, os.WNOHANG)
		return False
	return True


def kill_own(pid):
	"""Only the pid this runner launched (its own session)."""
	for sig, wait in ((signal.SIGTERM, 30), (signal.SIGKILL, 10)):
		with contextlib.suppress(ProcessLookupError):
			os.kill(pid, sig)
		end = time.time() + wait
		while alive(pid) and time.time() < end:
			time.sleep(1)
		if not alive(pid):
			return


# --- run ----------------------------------------------------------------------------------------------------

def jread(p, default=None):
	try:
		with open(p) as f:
			return json.load(f)
	except (OSError, ValueError):
		return default


def done_ids(run):
	out = set()
	p = os.path.join(run, "results.jsonl")
	if os.path.exists(p):
		for line in open(p):
			if line.strip():
				out.add(json.loads(line)["id"])
	return out


def write_plan(run, meta, remaining):
	plan = {"world": meta["world"], "raw_dir": os.path.join(run, "raw"), "results": os.path.join(run, "results.jsonl"),
			"progress": os.path.join(run, "progress.json"), "step_timeout_ticks": 600, "test_timeout_ticks": 2400,
			"setup": [], "tests": [{"id": t["id"], "origin": t["origin"], "steps": t["steps"], "capture": t["capture"]}
								   for t in remaining]}
	p = os.path.join(run, "capture_plan.json")
	with open(p, "w") as f:
		json.dump(plan, f, indent=1)
	return p


def evidence(game, run, pid, t_launch, n):
	out = []
	dst = os.path.join(run, "crash")
	os.makedirs(dst, exist_ok=True)
	for p in glob.glob(os.path.join(game, f"hs_err_pid{pid}.log")) + \
			[p for p in glob.glob(os.path.join(game, "crash-reports/*.txt")) if os.path.getmtime(p) >= t_launch]:
		shutil.copy2(p, os.path.join(dst, f"launch{n}_" + os.path.basename(p)))
		out.append(f"crash/launch{n}_" + os.path.basename(p))
	return out


def log_tail(path, n=30):
	try:
		lines = open(path, errors="replace").read().splitlines()
	except OSError:
		return []
	return [l[:300] for l in lines[-n:] if "accessToken" not in l]


def run_launches(run, meta, game, args):
	runner = jread(os.path.join(run, "runner.json"), {"launches": 0, "crash": {}, "status": {}, "crashes": {}})
	while True:
		done = done_ids(run)
		remaining = [t for t in meta["tests"] if t["id"] not in done and runner["status"].get(t["id"]) != "skipped"]
		if not remaining:
			break
		if runner["launches"] >= len(meta["tests"]) + 3:
			print("too many launches; stopping")
			break
		preflight()
		meta["jars"] = prepare_game(game, args["jars"], args["fresh_config"], remaining[0]["client_config"],
									meta["world"]["name"])
		with open(os.path.join(run, "run.json"), "w") as f:
			json.dump(meta, f, indent=1)
		for p in ("progress.json",):
			with contextlib.suppress(FileNotFoundError):
				os.remove(os.path.join(run, p))
		plan = write_plan(run, meta, remaining)
		runner["launches"] += 1
		n = runner["launches"]
		t0 = time.time()
		pid = launch(game, plan)
		print(f"launch {n}: client pid {pid}, {len(remaining)} tests")
		last_sig, last_change, hung, finished = None, time.time(), False, False
		while True:
			time.sleep(2)
			prog = jread(os.path.join(run, "progress.json"))
			rp = os.path.join(run, "results.jsonl")
			sig = (json.dumps(prog), os.path.getsize(rp) if os.path.exists(rp) else 0)
			if sig != last_sig:
				last_sig, last_change = sig, time.time()
				if prog:
					print(f"  {prog.get('state')} {prog.get('current')} {prog.get('index')}/{prog.get('total')}", flush=True)
			if not alive(pid):
				break
			state = (prog or {}).get("state", "")
			if state == "done" or state.startswith("aborted"):
				finished = True
				end = time.time() + 90
				while alive(pid) and time.time() < end:
					time.sleep(2)
				if alive(pid):
					kill_own(pid)
				break
			limit = BOOT_TIMEOUT_S if state in ("", "loading") else 2400 / 20 + 60
			if time.time() - last_change > limit:
				hung = True
				kill_own(pid)
				break
		shutil.copy2(os.path.join(game, "logs/latest.log"), os.path.join(run, f"client_{n}.log")) \
			if os.path.exists(os.path.join(game, "logs/latest.log")) else None
		prog = jread(os.path.join(run, "progress.json"), {})
		if finished and prog.get("state") == "done":
			break
		if (prog or {}).get("state", "").startswith("aborted"):
			print("driver aborted:", prog["state"])
			break
		cur = prog.get("current") if prog.get("state") == "running" else None
		reason = "hang (no progress)" if hung else "client exited"
		ev = evidence(game, run, pid, t0, n)
		if cur and cur not in done_ids(run):
			runner["crashes"][cur] = runner["crashes"].get(cur, 0) + 1
			runner["crash"][cur] = {"launch": n, "reason": reason, "evidence": ev,
									"log_tail": log_tail(os.path.join(run, f"client_{n}.log"))}
			runner["status"][cur] = "skipped" if runner["crashes"][cur] >= 2 else "crashed"
			print(f"  {cur}: {reason} ({runner['crashes'][cur]}x){' -> skipped' if runner['crashes'][cur] >= 2 else ''}")
		else:
			runner["boot_failures"] = runner.get("boot_failures", 0) + 1
			print(f"  boot failure ({reason}), {runner['boot_failures']}x")
			if runner["boot_failures"] >= 2:
				break
		with open(os.path.join(run, "runner.json"), "w") as f:
			json.dump(runner, f, indent=1)
	with open(os.path.join(run, "runner.json"), "w") as f:
		json.dump(runner, f, indent=1)


def git_state():
	h = subprocess.run(["git", "-C", INST, "rev-parse", "--short", "HEAD"], capture_output=True, text=True).stdout.strip()
	dirty = subprocess.run(["git", "-C", INST, "status", "--porcelain", "kubejs"], capture_output=True, text=True).stdout
	return h + (f" (+{len(dirty.splitlines())} kubejs changes)" if dirty.strip() else "")


def crop(run):
	import crop as cropper
	man, counts = cropper.build(run)
	print(f"{len(man['tests'])} tests:", ", ".join(f"{k} {v}" for k, v in sorted(counts.items())),
		  "->", os.path.relpath(os.path.join(run, "index.md"), INST))


def opt(a, name, default=None):
	if name in a:
		i = a.index(name)
		v = a[i + 1]
		del a[i:i + 2]
		return v
	return default


def opts(a, name):
	out = []
	while name in a:
		out.append(opt(a, name))
	return out


def flag(a, name):
	if name in a:
		a.remove(name)
		return True
	return False


def serve(a):
	game = os.path.abspath(opt(a, "--game", GAME))
	join, ownsrv, world = opt(a, "--join"), opt(a, "--ownsrv"), opt(a, "--world", "flat_creative")
	jars = opts(a, "--jar")
	drops, configs = opts(a, "--drop"), opts(a, "--config")
	set_display(opt(a, "--display", "real"))
	bad = [c for c in configs if not re.match(r"[^:]+:[^:]+:[^=]+=", c)]
	if bad:
		sys.exit("--config wants FILE:SECTION:KEY=VALUE, got: " + ", ".join(bad))
	preflight(ownsrv)
	from chan import Channel
	chan_dir = os.path.join(game, "cdev_channel")
	shutil.rmtree(chan_dir, ignore_errors=True)
	prepare_game(game, jars, world=None if join else world)
	apply_client_changes(game, drops, configs)
	w = {"name": world}
	if join:
		w["join"] = "server"
	plan = {"world": w, "raw_dir": os.path.join(chan_dir, "raw"), "results": os.path.join(chan_dir, "results.jsonl"),
			"progress": os.path.join(chan_dir, "progress.json"), "setup": [], "tests": [], "channel": chan_dir}
	os.makedirs(chan_dir, exist_ok=True)
	pp = os.path.join(game, "capture_plan.json")
	with open(pp, "w") as f:
		json.dump(plan, f, indent=1)
	pid = launch(game, pp, server=join, ownsrv=ownsrv)
	with open(os.path.join(chan_dir, "pid"), "w") as f:
		f.write(str(pid))
	print(f"client pid {pid}; waiting for the driver to serve {chan_dir}", flush=True)
	c, end = Channel(chan_dir), time.time() + BOOT_TIMEOUT_S
	while time.time() < end:
		if not alive(pid):
			sys.exit(f"client {pid} exited before serving; see {game}/logs/latest.log")
		if c.serving():
			print(f"serving: qrun.py client {game} BATCH RES")
			return
		time.sleep(3)
	sys.exit(f"driver not serving after {BOOT_TIMEOUT_S} s (client pid {pid} left running; stop it with "
			 f"run_captures.py stop, or kill that pid)")


def set_display(d):
	global DISPLAY_MODE
	if d not in ("real", "xvfb"):
		sys.exit("--display wants real or xvfb")
	DISPLAY_MODE = d


def apply_client_changes(game, drops=(), configs=()):
	"""serve --drop / --config (TX-31 client side). GAME/mods holds hard links: unlink only, never write through.
    eval_client.setup rebuilds mods/ and config/ on every launch, so nothing carries over to the next launch."""
	mods = os.path.join(game, "mods")
	for g in drops:
		hit = glob.glob(os.path.join(mods, g))
		if not hit:
			sys.exit(f"--drop {g}: no jar in {mods} matches")
		for f in hit:
			os.unlink(f)
			print("dropped", os.path.basename(f))
	for c in configs:
		m = re.match(r"([^:]+):([^:]+):([^=]+)=(.*)", c)
		f, sec, k, v = m.groups()
		path = os.path.normpath(os.path.join(game, f))
		if not path.startswith(game + os.sep):
			sys.exit(f"--config {c}: path leaves the game dir")
		toml_set(path, sec, k.strip(), v.strip())
		print(f"config {f}: [{sec}] {k.strip()} = {v.strip()}")


def main():
	a = sys.argv[1:]
	if not a:
		sys.exit(__doc__)
	if a[0] == "serve":
		return serve(a[1:])
	if a[0] == "stop":
		from chan import Channel
		game = os.path.abspath(opt(a, "--game", GAME))
		print(Channel(os.path.join(game, "cdev_channel")).op("stop"))
		return
	if flag(a, "--list"):
		do_check = flag(a, "--check")
		groups = a or all_groups()
		if do_check:
			tests, errors, warn = check(groups)
		else:
			tests, errors = resolve_each(groups)
			warn = []
		for t in tests:
			kind = f"{len(t['capture'])} capture(s)" if t["capture"] else "text readback"
			print(f"{t['group']:<10} {t['id']:<10} {kind:<14} {','.join(t['covers']):<24} {t['title'][:60]}")
		for w in warn:
			print("warn:", w)
		for e in errors:
			print("ERROR:", e)
		sys.exit(1 if errors else 0)
	recrop = opt(a, "--recrop")
	if recrop:
		run = os.path.abspath(recrop)
		meta = jread(os.path.join(run, "run.json"))
		world, tests, errors = resolve(meta["groups"], set(t["id"] for t in meta["tests"]))
		if errors:
			sys.exit("\n".join(errors))
		old = {t["id"]: t for t in meta["tests"]}
		for t in tests:  # keep the plots the run used; take crop rules and texts from the specs
			t["origin"] = old.get(t["id"], t)["origin"]
		meta["tests"] = tests
		with open(os.path.join(run, "run.json"), "w") as f:
			json.dump(meta, f, indent=1)
		return crop(run)
	resume = opt(a, "--resume")
	game = os.path.abspath(opt(a, "--game", GAME))
	only = opt(a, "--only")
	jars = [os.path.abspath(j) for j in opts(a, "--jar")]
	fresh = flag(a, "--fresh-config")
	plan_only = flag(a, "--plan-only")
	disp = opt(a, "--display")
	if resume:
		run = os.path.abspath(resume)
		meta = jread(os.path.join(run, "run.json"))
		set_display(disp or meta.get("display", "real"))
	else:
		groups = a
		if not groups:
			sys.exit(__doc__)
		world, tests, errors = resolve(groups, set(only.split(",")) if only else None)
		if errors:
			sys.exit("spec errors:\n" + "\n".join(errors))
		if not tests:
			sys.exit("no tests selected")
		configs = {json.dumps(t["client_config"], sort_keys=True) for t in tests}
		if len(configs) > 1:
			sys.exit("groups with different client_config: run them separately")
		run = os.path.join(OUT, time.strftime("%Y-%m-%d_%H%M_") + "+".join(groups))
		os.makedirs(os.path.join(run, "raw"), exist_ok=True)
		set_display(disp or "real")
		meta = {"groups": groups, "world": world, "tests": tests, "git": git_state(), "game": game, "display": DISPLAY_MODE,
				"created": time.strftime("%Y-%m-%d %H:%M:%S")}
		with open(os.path.join(run, "run.json"), "w") as f:
			json.dump(meta, f, indent=1)
		latest = os.path.join(OUT, "latest")
		with contextlib.suppress(FileNotFoundError):
			os.remove(latest)
		os.symlink(os.path.basename(run), latest)
	if plan_only:
		print("plan:", write_plan(run, meta, meta["tests"]))
		return
	run_launches(run, meta, game, {"jars": jars, "fresh_config": fresh})
	crop(run)


if __name__ == "__main__":
	main()
