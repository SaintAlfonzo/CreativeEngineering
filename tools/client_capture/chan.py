#!/usr/bin/env python3
"""Host side of the capture driver's file channel (feature_writeup/client_capture.md 2.11).

The driver (cdevcapture, launched by run_captures.py serve) polls <dir>/in/ once per client tick and runs one
request at a time; it answers in <dir>/out/<seq>.json and writes a heartbeat to <dir>/state.json every 20 ticks.

    from chan import Channel
    c = Channel(GAME + "/cdev_channel")
    reply = c.request("CQ-PTH-01", [{"walk": 40, "keys": "forward"}])     # blocks until the driver answers

    python3 chan.py DIR state                      print the heartbeat and its age
    python3 chan.py DIR ping | stop                ping the driver / make the client quit
    python3 chan.py DIR run ID 'STEP_JSON' ...     run steps, print the reply

Library only: never launches or kills a game JVM.
"""
import json
import os
import sys
import time

STALE_S = 60.0  # heartbeat older than this: the driver is gone or hung


class ChannelError(RuntimeError):
	pass


class Channel:
	def __init__(self, path):
		self.dir = os.path.abspath(path)
		self.inbox = os.path.join(self.dir, "in")
		self.outbox = os.path.join(self.dir, "out")
		self.raw = os.path.join(self.dir, "raw")
		self._last = 0

	def state(self):
		try:
			with open(os.path.join(self.dir, "state.json")) as f:
				s = json.load(f)
		except (OSError, ValueError):
			return None
		s["age_s"] = time.time() - s.get("time", 0) / 1000.0
		return s

	def serving(self):
		s = self.state()
		return bool(s) and s["age_s"] < STALE_S and s.get("phase") in ("SERVE",)

	def _seq(self):
		# milliseconds, strictly increasing within this process, unique across hosts in practice
		n = max(int(time.time() * 1000), self._last + 1)
		self._last = n
		return n

	def send(self, req):
		os.makedirs(self.inbox, exist_ok=True)
		seq = self._seq()
		req = dict(req, seq=seq)
		tmp = os.path.join(self.inbox, f"{seq}.json.tmp")
		with open(tmp, "w") as f:
			json.dump(req, f)
		os.replace(tmp, os.path.join(self.inbox, f"{seq}.json"))
		return seq

	def wait(self, seq, timeout=300.0, poll=0.05):
		out = os.path.join(self.outbox, f"{seq}.json")
		end = time.time() + timeout
		while True:
			if os.path.exists(out):
				with open(out) as f:
					r = json.load(f)
				os.remove(out)
				return r
			now = time.time()
			if now > end:
				raise ChannelError(f"no reply to request {seq} in {int(timeout)} s")
			s = self.state()
			if s is None or s["age_s"] > STALE_S:
				raise ChannelError("driver heartbeat stale (%s): client dead or hung" %
								   ("missing" if s is None else f"{int(s['age_s'])} s old"))
			time.sleep(poll)

	def request(self, rid, steps, reset=False, timeout=300.0, timeout_ticks=None):
		req = {"op": "run", "id": rid, "steps": steps, "reset": reset}
		if timeout_ticks:
			req["timeout_ticks"] = timeout_ticks
		return self.wait(self.send(req), timeout)

	def op(self, op, timeout=30.0):
		return self.wait(self.send({"op": op}), timeout)


def main(a):
	if len(a) < 2:
		sys.exit(__doc__)
	c = Channel(a[0])
	if a[1] == "state":
		print(json.dumps(c.state()))
	elif a[1] in ("ping", "stop"):
		print(json.dumps(c.op(a[1])))
	elif a[1] == "run" and len(a) >= 3:
		print(json.dumps(c.request(a[2], [json.loads(s) for s in a[3:]]), indent=1))
	else:
		sys.exit(__doc__)


if __name__ == "__main__":
	main(sys.argv[1:])
