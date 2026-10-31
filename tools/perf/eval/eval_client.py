#!/usr/bin/env python3
"""Throwaway client for the pack performance evaluation (feature_writeup/perf.md section 10).

    python3 eval_client.py setup GAME_DIR [WORLD_DIR]   mods hard-linked, kubejs/config/resourcepacks copied, the user's
                                                         options.txt windowed / uncapped / no pause on lost focus,
                                                         the two scratch scripts, WORLD_DIR copied to saves/PerfEval
    python3 eval_client.py launch GAME_DIR [nodebuglog]  starts it with the user's -Xmx and a JFR recording from JVM start
                                                         (ce-server.jfc copied to GAME_DIR/perf/); refuses if a game JVM is up.
                                                         nodebuglog: drop ATLauncher's client-1.19-debug.xml log config

The command line is never printed (it is built from ATLauncher's logged args, see tools/item_repro/launch_client.py).
"""
import os
import shutil
import subprocess
import sys

INST = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../.."))
sys.path.insert(0, os.path.join(INST, "tools/item_repro"))
import launch_client  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))


def setup(game, world=None):
    os.makedirs(game, exist_ok=True)
    mods = os.path.join(game, "mods")
    shutil.rmtree(mods, ignore_errors=True)
    os.makedirs(mods)
    for j in os.listdir(os.path.join(INST, "mods")):
        if j.endswith(".jar"):
            os.link(os.path.join(INST, "mods", j), os.path.join(mods, j))
    for d in ("kubejs", "config", "defaultconfigs", "resourcepacks"):
        shutil.rmtree(os.path.join(game, d), ignore_errors=True)
        shutil.copytree(os.path.join(INST, d), os.path.join(game, d), symlinks=True)
    for d in os.listdir(INST):
        if d.endswith("_upstream"):
            dst = os.path.join(game, d)
            if not os.path.exists(dst):
                os.symlink(os.path.join(INST, d), dst)
    opts = []
    for line in open(os.path.join(INST, "options.txt")):
        k = line.split(":", 1)[0]
        line = {"fullscreen": "fullscreen:false\n", "maxFps": "maxFps:260\n",
                "pauseOnLostFocus": "pauseOnLostFocus:false\n"}.get(k, line)
        opts.append(line)
    open(os.path.join(game, "options.txt"), "w").writelines(opts)
    shutil.copy(os.path.join(HERE, "client_fps_log.js"), os.path.join(game, "kubejs/client_scripts/zz_perfeval_fps.js"))
    shutil.copy(os.path.join(HERE, "sp_drive.js"), os.path.join(game, "kubejs/server_scripts/zz_perfeval_drive.js"))
    os.makedirs(os.path.join(game, "perf"), exist_ok=True)
    shutil.copy(os.path.join(INST, "server/perf/ce-server.jfc"), os.path.join(game, "perf/ce-server.jfc"))
    if world:
        dst = os.path.join(game, "saves/PerfEval")
        shutil.rmtree(dst, ignore_errors=True)
        shutil.copytree(world, dst, ignore=shutil.ignore_patterns("session.lock"))
    print("client dir ready:", len(os.listdir(mods)), "jars")


def launch(game, nodebuglog=False, maxage="60m", maxsize="1g"):
    live = subprocess.run(["bash", os.path.join(INST, "tools/scratch_query/game_jvms.sh")],
                          capture_output=True, text=True).stdout.strip()
    if live:
        sys.exit("game JVM already up: " + live)
    jfc = os.path.join(game, "perf/ce-server.jfc")
    jvm = ["-XX:FlightRecorderOptions=stackdepth=256,repository=" + os.path.join(game, "perf/jfr-repo"),
           "-XX:StartFlightRecording=name=ce,settings=%s,disk=true,maxage=%s,maxsize=%s,dumponexit=true,filename=%s"
           % (jfc, maxage, maxsize, os.path.join(game, "perf/exit.jfr"))]
    args = launch_client.build_args(game, jvm_extra=jvm, username="PerfEval")
    args = [a if not a.startswith("-Xmx") else next(x for x in launch_client.logged_args() if x.startswith("-Xmx"))
            for a in args]
    if nodebuglog:
        args = [a for a in args if not (a.startswith("-Dlog4j.configurationFile=") and a.endswith("-debug.xml"))]
    env = dict(os.environ, DISPLAY=os.environ.get("DISPLAY", ":1"))
    f = open(os.path.join(game, "launch.out"), "w")
    p = subprocess.Popen(args, cwd=game, stdout=f, stderr=subprocess.STDOUT, env=env, start_new_session=True)
    print("client pid", p.pid)


if __name__ == "__main__":
    what, game = sys.argv[1], os.path.abspath(sys.argv[2])
    if what == "setup":
        setup(game, sys.argv[3] if len(sys.argv) > 3 else None)
    elif what == "launch":
        launch(game, len(sys.argv) > 3 and sys.argv[3] == "nodebuglog")
    else:
        sys.exit(__doc__)
