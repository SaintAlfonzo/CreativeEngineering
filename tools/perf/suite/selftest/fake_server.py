"""Stand-in for a Minecraft server, for selftest.py: speaks RCON, writes logs/latest.log and a ceperfbot tick
log, and ends the ways a real server does when told to (`fake die9|hang|oom|crashnext`). Run from the srv dir
by a copy of python named `java`, so the runner's pid lookup (pgrep -x java + cwd) finds it."""
import os
import signal
import socket
import struct
import sys
import threading
import time

os.makedirs("logs", exist_ok=True)
os.makedirs("perf/bot", exist_ok=True)
props = dict(l.strip().split("=", 1) for l in open("server.properties") if "=" in l)
LOG = open("logs/latest.log", "a", buffering=1)
state = {"ticking": True, "tick": 0}
lock = threading.Lock()


def log(text):
    LOG.write(time.strftime("[%H:%M:%S] ") + text + "\n")


if os.path.exists("crash_next_boot"):
    os.remove("crash_next_boot")
    os.makedirs("crash-reports", exist_ok=True)
    open("crash-reports/crash-fake-server.txt", "w").write("---- Minecraft Crash Report ----\nfake\n")
    log("Crash report saved to: ./crash-reports/crash-fake-server.txt")
    sys.exit(1)

ticks = open(f"perf/bot/ticks-{int(time.time() * 1000)}.csv", "w")


def write(line):
    with lock:
        ticks.write(line)


def mark(label):
    write(f"m,{int(time.time() * 1000)},{state['tick']},{label}\n")
    with lock:
        ticks.flush()


def ticker():
    while True:
        if state["ticking"]:
            state["tick"] += 1
            t = state["tick"]
            write(f"t,{t},{5000 + (t % 7) * 300}\n")
            if t % 20 == 0:
                write(f"s,{int(time.time() * 1000)},{t},1,123,441,2048\n")
                with lock:
                    ticks.flush()
        time.sleep(0.05)


def handle(cmd):
    if cmd == "pbot status":
        return "ceperfbot ok tick %d bots 0 out perf/bot" % state["tick"]
    if cmd.startswith("pbot spawn"):
        n = cmd.split()[2]
        return f"bot {n} at 10 64 -20 in minecraft:overworld"
    if cmd.startswith("pbot mark "):
        mark(cmd[10:])
        return "marked"
    if cmd.startswith("pbot census "):
        tag = cmd.split()[2]
        open(f"perf/bot/census-{tag}.json", "w").write('{"tick": %d}' % state["tick"])
        return f"census {tag} entities 123 block_entities 0"
    if cmd == "stop":
        log("Stopping server")
        log("All dimensions are saved")
        threading.Timer(0.5, lambda: os._exit(0)).start()
        return "Stopping the server"
    if cmd == "fake die9":
        threading.Timer(0.3, lambda: os.kill(os.getpid(), signal.SIGKILL)).start()
        return "dying"
    if cmd == "fake hang":
        state["ticking"] = False
        return "hanging"
    if cmd == "fake oom":
        log("java.lang.OutOfMemoryError: Java heap space")
        threading.Timer(0.3, lambda: os._exit(1)).start()
        return "oom"
    if cmd == "fake crashnext":
        open("crash_next_boot", "w").write("1")
        return "armed"
    return "ok"


def client(conn):
    def read_exact(n):
        b = b""
        while len(b) < n:
            c = conn.recv(n - len(b))
            if not c:
                raise ConnectionError
            b += c
        return b

    def send(rid, body):
        pkt = struct.pack("<ii", rid, 0) + body.encode() + b"\x00\x00"
        conn.sendall(struct.pack("<i", len(pkt)) + pkt)

    try:
        while True:
            (n,) = struct.unpack("<i", read_exact(4))
            p = read_exact(n)
            rid, kind = struct.unpack("<ii", p[:8])
            body = p[8:-2].decode()
            if kind == 3:
                send(rid if body == props["rcon.password"] else -1, "")
            else:
                send(rid, handle(body))
    except (ConnectionError, OSError):
        conn.close()


time.sleep(1)
threading.Thread(target=ticker, daemon=True).start()
srv = socket.socket()
srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
srv.bind(("127.0.0.1", int(props["rcon.port"])))
srv.listen(4)
log('Done (1.000s)! For help, type "help"')
mark("server_starting")
while True:
    c, _ = srv.accept()
    threading.Thread(target=client, args=(c,), daemon=True).start()
