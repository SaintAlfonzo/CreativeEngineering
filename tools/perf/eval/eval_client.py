#!/usr/bin/env python3
"""Throwaway client for the pack performance evaluation (feature_writeup/perf.md section 10).

    python3 eval_client.py setup GAME_DIR [WORLD_DIR]   mods hard-linked, kubejs/config/resourcepacks copied, the user's
                                                         options.txt windowed / uncapped / no pause on lost focus,
                                                         the two scratch scripts, WORLD_DIR copied to saves/PerfEval
    python3 eval_client.py launch GAME_DIR [nodebuglog] [server=HOST:PORT] [ownsrv=PID]
                                                         starts it with the user's -Xmx and a JFR recording from JVM start
                                                         (ce-server.jfc copied to GAME_DIR/perf/); refuses if a game JVM is up.
                                                         nodebuglog: drop ATLauncher's client-1.19-debug.xml log config
                                                         server=HOST:PORT: join that server at start (--server/--port)
                                                         ownsrv=PID: ignore that game JVM (your own scratch server,
                                                         for rows that need a server plus a client)

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


def start_xvfb(game):
    """EXPERIMENTAL (plan_scratch_testing_expansion TX-07, owner D3): an own Xvfb display for an unattended client.
    Software rendering (llvmpipe): slower, colors may differ; never for color rows. Returns the DISPLAY string.
    The display's pid goes to GAME/xvfb.pid; a detached reaper stops it once the client pid is gone."""
    r, w = os.pipe()
    x = subprocess.Popen(["Xvfb", "-displayfd", str(w), "-screen", "0", "1280x720x24", "-nolisten", "tcp"],
                         pass_fds=(w,), stdout=subprocess.DEVNULL, stderr=open(os.path.join(game, "xvfb.out"), "w"),
                         start_new_session=True)
    os.close(w)
    num = b""
    with os.fdopen(r, "rb") as f:
        num = f.readline().strip()
    if not num:
        x.kill()
        sys.exit("Xvfb did not report a display (see %s)" % os.path.join(game, "xvfb.out"))
    open(os.path.join(game, "xvfb.pid"), "w").write(str(x.pid))
    return x, ":" + num.decode()


def launch(game, nodebuglog=False, maxage="60m", maxsize="1g", server=None, ownsrv=None, display="real"):
    live = subprocess.run(["bash", os.path.join(INST, "tools/scratch_query/game_jvms.sh")],
                          capture_output=True, text=True).stdout.strip()
    # ownsrv=PID: the caller's own scratch server for a row that needs a server and a client (server first)
    live = "\n".join(l for l in live.splitlines() if not (ownsrv and l.split()[0] == ownsrv))
    if live:
        sys.exit("game JVM already up: " + live)
    jfc = os.path.join(game, "perf/ce-server.jfc")
    jvm = ["-XX:FlightRecorderOptions=stackdepth=256,repository=" + os.path.join(game, "perf/jfr-repo"),
           "-XX:StartFlightRecording=name=ce,settings=%s,disk=true,maxage=%s,maxsize=%s,dumponexit=true,filename=%s"
           % (jfc, maxage, maxsize, os.path.join(game, "perf/exit.jfr"))]
    extra = []
    if server:
        host, _, port = server.rpartition(":")
        extra = ["--server", host, "--port", port]
    args = launch_client.build_args(game, extra=extra, jvm_extra=jvm, username="PerfEval")
    args = [a if not a.startswith("-Xmx") else next(x for x in launch_client.logged_args() if x.startswith("-Xmx"))
            for a in args]
    if nodebuglog:
        args = [a for a in args if not (a.startswith("-Dlog4j.configurationFile=") and a.endswith("-debug.xml"))]
    env = dict(os.environ, DISPLAY=os.environ.get("DISPLAY", ":1"))
    xvfb = None
    if display == "xvfb":
        xvfb, env["DISPLAY"] = start_xvfb(game)
    elif display != "real":
        sys.exit("display must be real or xvfb")
    f = open(os.path.join(game, "launch.out"), "w")
    p = subprocess.Popen(args, cwd=game, stdout=f, stderr=subprocess.STDOUT, env=env, start_new_session=True)
    if xvfb:
        # reaper: the Xvfb lives exactly as long as this client (only these two own pids)
        subprocess.Popen(["sh", "-c", "while kill -0 %d 2>/dev/null; do sleep 5; done; kill %d" % (p.pid, xvfb.pid)],
                         stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, start_new_session=True)
        print("display", env["DISPLAY"], "(xvfb, experimental)")
    print("client pid", p.pid)


if __name__ == "__main__":
    what, game = sys.argv[1], os.path.abspath(sys.argv[2])
    if what == "setup":
        setup(game, sys.argv[3] if len(sys.argv) > 3 else None)
    elif what == "launch":
        opts = sys.argv[3:]
        srv = next((o.split("=", 1)[1] for o in opts if o.startswith("server=")), None)
        own = next((o.split("=", 1)[1] for o in opts if o.startswith("ownsrv=")), None)
        disp = next((o.split("=", 1)[1] for o in opts if o.startswith("display=")), "real")
        launch(game, "nodebuglog" in opts, server=srv, ownsrv=own, display=disp)
    else:
        sys.exit(__doc__)
