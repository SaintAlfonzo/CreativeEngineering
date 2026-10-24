#!/usr/bin/env python3
"""Launches the throwaway repro client from ATLauncher's last logged launch args.

    python3 launch_client.py GAME_DIR [HOST PORT]

The logged args have the account details stripped; this swaps in an offline account
(username Tester) and the scratch game dir, and joins HOST:PORT on start. Without HOST PORT it
stops at the title screen (the names audit's dump, feature_writeup/names.md section 11). The command line
is never printed (see memory: patchermod-headless-smoke-test).

Importable: build_args(game_dir, extra) returns the argument list and launch(game_dir, extra) starts it
(tools/client_capture/run_captures.py uses both).
"""
import glob
import os
import subprocess
import sys
import uuid

USERS = os.path.expanduser("~/.local/share/atlauncher")
MARKER = "Launching Minecraft with the following arguments (user related stuff has been removed): ["


def logged_args():
    line = None
    for log in sorted(glob.glob(USERS + "/logs/old/*.log"), key=os.path.getmtime, reverse=True):
        for l in open(log, errors="replace"):
            if MARKER in l and "CreativeEngineering2" in l:
                line = l
        if line:
            break
    if line is None:
        raise SystemExit("no logged CreativeEngineering2 launch in ATLauncher's logs/old/")
    args = line[line.index(MARKER) + len(MARKER):line.rindex("]")].split(", ")
    return [a.replace("USERSDIR", USERS) for a in args]


def build_args(game_dir, extra=(), jvm_extra=(), username="Tester"):
    """Java command line for GAME_DIR. jvm_extra goes before the main class, extra after the game args."""
    args = logged_args()
    out = []
    skip = {"--username", "--uuid", "--accessToken", "--userType", "--gameDir"}
    i = 0
    while i < len(args):
        a = args[i]
        if a == "gamemoderun" or a.startswith("-Dmixin.debug"):
            i += 1
            continue
        if a.startswith("-Xmx"):
            a = "-Xmx5G"
        if a.startswith("-Djava.library.path="):
            natives = os.path.join(game_dir, "natives")
            os.makedirs(natives, exist_ok=True)
            a = "-Djava.library.path=" + natives
        if a in skip:
            i += 2
            continue
        out.append(a)
        i += 1
    if jvm_extra:
        # JVM options go right after the java executable.
        out = out[:1] + list(jvm_extra) + out[1:]
    out += ["--gameDir", game_dir, "--username", username, "--uuid", str(uuid.uuid4()),
            "--accessToken", "0", "--userType", "legacy"]
    return out + list(extra)


def launch(game_dir, extra=(), jvm_extra=(), username="Tester", display=None):
    """Starts the client detached (own session), output to GAME_DIR/launch.out. Returns the Popen."""
    env = dict(os.environ, DISPLAY=display or os.environ.get("DISPLAY", ":1"))
    f = open(os.path.join(game_dir, "launch.out"), "w")
    return subprocess.Popen(build_args(game_dir, extra, jvm_extra, username), cwd=game_dir, stdout=f,
                            stderr=subprocess.STDOUT, env=env, start_new_session=True)


if __name__ == "__main__":
    game_dir = sys.argv[1]
    extra = ["--server", sys.argv[2], "--port", sys.argv[3]] if len(sys.argv) > 3 else []
    p = launch(game_dir, extra)
    print("client pid", p.pid)
