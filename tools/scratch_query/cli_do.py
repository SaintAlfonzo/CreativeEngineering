#!/usr/bin/env python3
"""One-off steps on a throwaway client window (eval_client.py game dir), for hand rows that need iteration before
they go into a batch (r1115 phase 10).

    cli_do.py GAME STEP [STEP ...]
      /cmd args          chat command (typed like qrun.py client does)
      walk:SECS[:KEY]    hold KEY (default w) for SECS seconds
      key:KEYS           xdotool key (e.g. key:Escape, key:F3+t)
      shot:PATH          window screenshot to PATH
      sleep:SECS
      mouse:X:Y          move the pointer to window coordinates (hover)
      click:X:Y[:BTN]    click at window coordinates (BTN 1 left, 3 right)
      look:DX:DY         relative mouse move (turns the camera when no screen is open)
      type:TEXT          type TEXT into the focused field (JEI search box after a click on it)
      grep:REGEX         count lines of GAME/logs/latest.log matching REGEX (printed)

Steps run in order; each prints one line.
"""
import os
import re
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import qrun  # noqa: E402


def main():
	if len(sys.argv) < 3:
		sys.exit(__doc__)
	game = os.path.abspath(sys.argv[1])
	c = qrun.Client(game)
	for st in sys.argv[2:]:
		if st.startswith("/"):
			c.cmd(st)
			print("cmd", st)
			continue
		what, _, rest = st.partition(":")
		a = rest.split(":") if rest else []
		if what == "walk":
			k = a[1] if len(a) > 1 else "w"
			subprocess.run(["xdotool", "windowactivate", "--sync", c.w])
			subprocess.run(["xdotool", "keydown", "--window", c.w, k])
			time.sleep(float(a[0]))
			subprocess.run(["xdotool", "keyup", "--window", c.w, k])
			print("walk", a[0], k)
		elif what == "key":
			c.key(rest)
			print("key", rest)
		elif what == "shot":
			c.shot(rest)
			print("shot", rest)
		elif what == "sleep":
			time.sleep(float(rest))
		elif what == "mouse":
			subprocess.run(["xdotool", "mousemove", "--window", c.w, a[0], a[1]])
			print("mouse", a[0], a[1])
		elif what == "click":
			subprocess.run(["xdotool", "mousemove", "--window", c.w, a[0], a[1], "click", a[2] if len(a) > 2 else "1"])
			print("click", rest)
		elif what == "look":
			subprocess.run(["xdotool", "mousemove_relative", "--", a[0], a[1]])
			print("look", rest)
		elif what == "type":
			subprocess.run(["xdotool", "type", "--window", c.w, "--delay", "40", "--", rest])
			print("type", rest)
		elif what == "grep":
			n = sum(1 for line in open(os.path.join(game, "logs/latest.log"), errors="replace") if re.search(rest, line))
			print("grep", rest, n)
		else:
			sys.exit("unknown step " + st)


if __name__ == "__main__":
	main()
