#!/usr/bin/env python3
"""Launches the throwaway repro client from ATLauncher's last logged launch args.

    python3 launch_client.py GAME_DIR HOST PORT

The logged args have the account details stripped; this swaps in an offline account
(username Tester) and the scratch game dir, and joins HOST:PORT on start. The command line
is never printed (see memory: patchermod-headless-smoke-test).
"""
import glob
import os
import subprocess
import sys
import uuid

game_dir, host, port = sys.argv[1], sys.argv[2], sys.argv[3]
users = os.path.expanduser("~/.local/share/atlauncher")
marker = "Launching Minecraft with the following arguments (user related stuff has been removed): ["
line = None
for log in sorted(glob.glob(users + "/logs/old/*.log"), key=os.path.getmtime, reverse=True):
    for l in open(log, errors="replace"):
        if marker in l and "CreativeEngineering2" in l:
            line = l
    if line:
        break
args = line[line.index(marker) + len(marker):line.rindex("]")].split(", ")
args = [a.replace("USERSDIR", users) for a in args]

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
out += ["--gameDir", game_dir, "--username", "Tester", "--uuid", str(uuid.uuid4()),
        "--accessToken", "0", "--userType", "legacy", "--server", host, "--port", port]

env = dict(os.environ, DISPLAY=os.environ.get("DISPLAY", ":1"))
with open(os.path.join(game_dir, "launch.out"), "w") as f:
    p = subprocess.Popen(out, cwd=game_dir, stdout=f, stderr=subprocess.STDOUT, env=env,
                         start_new_session=True)
print("client pid", p.pid)
