#!/usr/bin/env python3
"""Resource pack stress bench on a throwaway client (feature_writeup/perf_rpack.md section 4).

    python3 rp_bench.py plan  [OPTIONS]                   print the configs and scenes, and the time estimate
    python3 rp_bench.py setup GAME [--world DIR] [OPTIONS] client dir (eval_client.py setup), scan, plan, bench script
    python3 rp_bench.py drive GAME RES [--timeout MIN]     launch, click into the world, wait for the bench, collect
    python3 rp_bench.py run   GAME RES [--world DIR] [OPTIONS]   setup + drive
then
    python3 rp_report.py RES

OPTIONS: --packs a,b (only targets whose id contains one of these) --builtin (mod-provided optional packs too)
         --repeat N (leave-one-out passes, default 1; odd passes run in reverse) --every K (an "all" drift check
         after every K configs, default 6) --quick (shorter samples, one rebuild) --no-scenes (reloads only)

Configs: "all" (the user's options.txt selection) with every scene, "none" (only the packs that are kept: vanilla,
mod_resources, generated and in-tree packs) with every scene, one "-<pack>" per target with that pack's scene and
the control scene, "all" drift checks, and a final "all" with every scene. One client launch runs them all: the
bench script swaps the selection and reloads in game. Refuses to launch while a game JVM is up.
The command line is never printed (eval_client.py builds it from ATLauncher's logged args).
"""
import argparse
import json
import os
import shutil
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
INST = os.path.abspath(os.path.join(HERE, "../../.."))
sys.path.insert(0, os.path.join(INST, "tools/perf/eval"))

# packs that stay selected in every config: required, generated from mod content, or the pack's own
KEEP = ("vanilla", "mod_resources", "Generated Pack", "vinery_upstream_assets", "incubation_additions:")
MAX_BLOCKS = 160
SITE = {"x": 500, "y": 100, "z": 0, "size": 48}
VISTA = {"dim": "minecraft:overworld", "x": 0, "y": 140, "z": 0, "yaw": 45, "pitch": 30}


def opts_list(key):
    for line in open(os.path.join(INST, "options.txt"), errors="replace"):
        if line.startswith(key + ":"):
            return json.loads(line.split(":", 1)[1])
    return []


def short(pid):
    n = pid[5:] if pid.startswith("file/") else pid
    return n[:-4] if n.endswith(".zip") else n


def scan(out):
    subprocess.run([sys.executable, os.path.join(HERE, "rp_scan.py"), "--out", out], check=True,
                   stdout=subprocess.DEVNULL)
    return json.load(open(os.path.join(out, "rpack-scan.json")))


def make_plan(a, scan_json):
    order = opts_list("resourcePacks")
    kept = [p for p in order if any(k in p for k in KEEP)]
    targets = [p for p in order if p not in kept and (p.startswith("file/") or a.builtin)]
    if a.packs:
        want = [w.strip().lower() for w in a.packs.split(",") if w.strip()]
        targets = [p for p in targets if any(w in p.lower() for w in want)]
    extra = json.load(open(os.path.join(HERE, "scene_extra.json")))
    by_id = {r["id"]: r for r in scan_json["packs"]}
    scenes = {"control": {"blocks": ["minecraft:stone", "minecraft:oak_planks", "minecraft:glass"]},
              "vista": {"vista": VISTA}}
    touched = set()
    pack_scenes, notes = {}, {}
    for p in targets:
        r = by_id.get(p, {})
        blocks, ents = list(r.get("blocks", [])), list(r.get("entities", []))
        for key, e in extra.items():
            if not key.startswith("_") and key in p:
                blocks += [b for b in e.get("blocks", []) if b not in blocks]
                ents += [x for x in e.get("entities", []) if x not in ents]
                if e.get("noScene") or e.get("note"):
                    notes[p] = e.get("noScene") or e.get("note")
        if blocks or ents:
            name = "pack:" + short(p)
            scenes[name] = {"blocks": blocks[:MAX_BLOCKS], "entities": ents[:4], "pack": p}
            pack_scenes[p] = name
            touched.update(blocks)
    for b in list(scenes["control"]["blocks"]):
        if b in touched:  # the control must look the same in every config
            scenes["control"]["blocks"].remove(b)
    union = sorted(touched)[:MAX_BLOCKS * 3]
    if union:
        scenes["union"] = {"blocks": union}
    if a.no_scenes:
        scenes = {}
    every = list(scenes)
    sweep = lambda: list(every)
    check = [s for s in ("control", "vista") if s in scenes]
    configs = [{"name": "all", "packs": order, "scenes": sweep()},
               {"name": "none", "packs": kept, "scenes": sweep()}]
    loo = []
    for i in range(a.repeat):
        seq = targets if i % 2 == 0 else list(reversed(targets))
        for p in seq:
            sc = ([pack_scenes[p]] if p in pack_scenes and pack_scenes[p] in scenes else []) + \
                 (["control"] if "control" in scenes else [])
            loo.append({"name": "-" + short(p), "packs": [x for x in order if x != p], "scenes": sc, "target": p})
    for i, c in enumerate(loo):
        configs.append(c)
        if (i + 1) % a.every == 0 and i + 1 < len(loo):
            configs.append({"name": "all", "packs": order, "scenes": check, "drift": True})
    configs.append({"name": "all", "packs": order, "scenes": sweep()})
    t = {"warmTicks": 40, "sampleTicks": 100, "rebuilds": 1} if a.quick else \
        {"warmTicks": 60, "sampleTicks": 200, "rebuilds": 2}
    plan = dict(t, settleTicks=600, postReloadTicks=100, joinTicks=600, statesPerBlock=6, layers=3,
                entityCount=96, floor="minecraft:smooth_stone", camBack=14, camUp=18, camPitch=40,
                site=SITE, scenes=scenes, configs=configs, targets=targets, kept=kept, notes=notes)
    return plan


def estimate(plan, reload_s=75):
    per_scene = 3 + 2 + plan["rebuilds"] * 2.5 + (plan["warmTicks"] + plan["sampleTicks"]) / 20
    n_sc = sum(len(c["scenes"]) for c in plan["configs"])
    return (len(plan["configs"]) * (reload_s + plan["postReloadTicks"] / 20) + n_sc * per_scene) / 60


def print_plan(plan):
    print(f"targets {len(plan['targets'])}, kept {len(plan['kept'])}, scenes {len(plan['scenes'])}, "
          f"configs {len(plan['configs'])}")
    for n, s in plan["scenes"].items():
        if "vista" in s:
            print(f"  scene {n}: overworld view")
        else:
            print(f"  scene {n}: {len(s.get('blocks', []))} blocks, {len(s.get('entities', []))} entity types")
    for p in plan["targets"]:
        if p in plan["notes"]:
            print(f"  note {short(p)}: {plan['notes'][p]}")
    print("  configs: " + ", ".join(c["name"] + (f"[{len(c['scenes'])}]" if c["scenes"] else "") for c in plan["configs"]))
    print(f"estimate: about {estimate(plan):.0f} min at 75 s per reload (the first config's reload sets the real pace)")


def setup(a):
    import eval_client
    game = os.path.abspath(a.game)
    world = a.world or default_world()
    print("world:", world)
    eval_client.setup(game, world)
    cs = os.path.join(game, "kubejs/client_scripts")
    for f in ("zz_perfeval_fps.js",):
        if os.path.exists(os.path.join(cs, f)):
            os.remove(os.path.join(cs, f))
    drive_js = os.path.join(game, "kubejs/server_scripts/zz_perfeval_drive.js")  # it teleports the player
    if os.path.exists(drive_js):
        os.remove(drive_js)
    res = os.path.join(game, "rpbench")
    os.makedirs(res, exist_ok=True)
    plan = make_plan(a, scan(res))
    open(os.path.join(cs, "zz_rpbench_plan.js"), "w").write(
        "// Scratch-only, written by tools/perf/rpack/rp_bench.py\nglobal.rpbenchPlan = " + json.dumps(plan) + "\n")
    json.dump(plan, open(os.path.join(res, "plan.json"), "w"), indent=1)
    shutil.copy(os.path.join(HERE, "rp_bench.js"), os.path.join(cs, "zz_rpbench.js"))
    # fixed view: render distance 6, FOV 70, no vsync, uncapped (260), GUI hidden by the script
    path = os.path.join(game, "options.txt")
    fix = {"renderDistance": "6", "simulationDistance": "6", "fov": "0.0", "enableVsync": "false", "maxFps": "260",
           "pauseOnLostFocus": "false", "fullscreen": "false"}
    lines = [(lambda k: f"{k}:{fix.pop(k)}\n" if k in fix else l)(l.split(":", 1)[0]) for l in open(path)]
    lines += [f"{k}:{v}\n" for k, v in fix.items()]
    open(path, "w").writelines(lines)
    print_plan(plan)


def default_world():
    saves = os.path.join(INST, "saves")
    worlds = [os.path.join(saves, w) for w in os.listdir(saves) if os.path.isfile(os.path.join(saves, w, "level.dat"))
              and w != "Workspace"]
    size = lambda d: sum(os.path.getsize(os.path.join(r, f)) for r, _, fs in os.walk(d) for f in fs)
    return min(worlds, key=size)


def game_pid(game):
    for line in subprocess.run(["bash", os.path.join(INST, "tools/scratch_query/game_jvms.sh")],
                               capture_output=True, text=True).stdout.splitlines():
        pid, _, cwd = line.partition(" ")
        if cwd.startswith(game):
            return pid
    return None


def grep(path, needle):
    try:
        for line in open(path, errors="replace"):
            if needle in line:
                return line.rstrip()
    except OSError:
        pass
    return None


def wait_for(path, needle, secs, game=None):
    end = time.time() + secs
    while time.time() < end:
        hit = grep(path, needle)
        if hit:
            return hit
        if game and not game_pid(game):
            return None
        time.sleep(2)
    return None


def drive(a):
    import eval_client
    game, res = os.path.abspath(a.game), os.path.abspath(a.res)
    os.makedirs(res, exist_ok=True)
    log, klog = os.path.join(game, "logs/latest.log"), os.path.join(game, "logs/kubejs/client.log")
    tl = open(os.path.join(res, "timeline.txt"), "a")

    def mark(s):
        line = f"{time.strftime('%H:%M:%S')} {s}"
        print(line, flush=True)
        tl.write(line + "\n")
        tl.flush()
    if a.attach:  # a client from an earlier drive is already up (at the title screen or in the world)
        if not game_pid(game):
            return mark("--attach: no client running in " + game)
        mark("attached")
    else:
        for p in (log, klog):
            if os.path.exists(p):
                os.remove(p)  # a wait on the old log would match the previous run
        # the whole run in the recording (launch + about an hour); no debug log config (it skews client timings)
        eval_client.launch(game, nodebuglog=True, maxage="240m", maxsize="4g")
        mark("launched")
    if not wait_for(log, "Sound engine started", 900, game):
        return mark("no title screen (client died or timed out)")
    # KubeJS posts ClientEvents.tick only with a player, so the script cannot open the world; the title screen is
    # usable about 35 s after "Sound engine started" ("Resource reload finished" is debug-level, gone with nodebuglog)
    for attempt in (1, 2):
        if grep(log, "Starting integrated minecraft server") or grep(klog, "[rpbench] start"):
            break
        time.sleep(60)
        w = subprocess.run(["xdotool", "search", "--onlyvisible", "--name", "^Minecraft"], capture_output=True,
                           text=True).stdout.split()
        if not w:
            return mark("no window")
        subprocess.run(["xdotool", "windowactivate", "--sync", w[0]], capture_output=True)
        for x, y, pause in ((425, 235, 3), (340, 133, 1), (267, 395, 1)):  # Singleplayer, first world, Play (854x480)
            subprocess.run(["xdotool", "mousemove", "--window", w[0], str(x), str(y), "click", "1"])
            time.sleep(pause)
        mark(f"play clicked (attempt {attempt})")
        if wait_for(log, "Starting integrated minecraft server", 60, game):
            break
    if not wait_for(klog, "[rpbench] start", 900, game):
        mark("bench did not start (see logs/kubejs/client.log)")
    else:
        mark("bench started")
        end, seen = time.time() + a.timeout * 60, 0
        while time.time() < end and game_pid(game):
            text = open(klog, errors="replace").read()
            n = text.count("[rpbench] config_end")
            if n != seen:
                seen = n
                mark(f"configs done {n}")
            if "[rpbench] done" in text:
                mark("bench done")
                break
            time.sleep(10)
    pid = game_pid(game)
    if pid:
        subprocess.run(["jcmd", pid, "JFR.dump", "name=ce", "filename=" + os.path.join(res, "client.jfr")],
                       capture_output=True)
    for src, dst in ((log, "client_latest.log"), (klog, "client_kubejs.log"),
                     (os.path.join(game, "rpbench/plan.json"), "plan.json"),
                     (os.path.join(game, "rpbench/rpack-scan.json"), "rpack-scan.json")):
        if os.path.exists(src):
            shutil.copy(src, os.path.join(res, dst))
    if pid:
        subprocess.run(["kill", pid])
        for _ in range(30):
            if not game_pid(game):
                break
            time.sleep(1)
        if game_pid(game):
            subprocess.run(["kill", "-9", pid])
    mark("client stopped; report: python3 tools/perf/rpack/rp_report.py " + res)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("what", choices=("plan", "setup", "drive", "run"))
    ap.add_argument("game", nargs="?")
    ap.add_argument("res", nargs="?")
    ap.add_argument("--world")
    ap.add_argument("--attach", action="store_true", help="drive: take over a client that is already up")
    ap.add_argument("--packs")
    ap.add_argument("--builtin", action="store_true")
    ap.add_argument("--repeat", type=int, default=1)
    ap.add_argument("--every", type=int, default=6)
    ap.add_argument("--quick", action="store_true")
    ap.add_argument("--no-scenes", action="store_true")
    ap.add_argument("--timeout", type=int, default=240, help="minutes to wait for the bench (drive)")
    a = ap.parse_args()
    if a.what == "plan":
        out = os.path.join(INST, "perf/reports")
        print_plan(make_plan(a, scan(out)))
        return
    if not a.game:
        sys.exit("GAME dir needed")
    if a.what in ("setup", "run"):
        setup(a)
    if a.what in ("drive", "run"):
        if not a.res:
            sys.exit("RES dir needed")
        drive(a)


if __name__ == "__main__":
    main()
