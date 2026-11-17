#!/usr/bin/env python3
"""Client driver for the CQ-PTH rows (r1115 phase 11): a throwaway client (eval_client.py game dir) joined to an own
scratch server (scratch_server.sh OUT) or in single player. World setup and measurements go through the server console
(scratch_server.sh cmd, probe zpth_probe.js: "zpth watch me ..." on the player tagged "me"); the client only holds keys
and takes screenshots. Every step logs a "say PTHCLI <label>" marker to the server so its log can be cut per step.

    pth_client_drive.py GAME OUT NAME SCENARIO [SHOTDIR]     SCENARIO: one of the names in SCENARIOS below
    pth_client_drive.py GAME sp NAME SCENARIO [SHOTDIR]      single player: commands typed into chat (no console)

Step kinds: ("srv", cmd) console command; ("walk", secs, keys) hold keys (space-separated) together; ("tap", keys)
xdotool key; ("sleep", s); ("shot", name); ("mark", label).
"""
import os
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import qrun  # noqa: E402

INST = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "../.."))
SCRATCH = os.path.join(INST, "tools/creative_labels_test/scratch_server.sh")


def wheat(age, x0=1300, z0=1302):
	return [("srv", f"fill {x0 - 1} -61 {z0 - 1} {x0 + 5} -61 {z0 + 5} minecraft:farmland"),
			("srv", f"fill {x0} -60 {z0} {x0 + 4} -60 {z0 + 4} minecraft:wheat[age={age}]")]


def walk_lane(label, x, y, z, yaw, secs, keys="w", watch=None):
	w = watch or int(secs * 20) + 40
	return [("mark", label), ("srv", f"tp @a[tag=me] {x} {y} {z} {yaw} 0"), ("sleep", 1.5),
			("srv", f"zpth watch me {w} 2"), ("walk", secs, keys), ("sleep", 1)]


SCENARIOS = {
	# CQ-PTH-01: wheat 7 / 3, corn lower 3 / 4, rice paddy wading and swimming beside it, creative walk and fly, rope
	# climb, vines (stand and walk). FOV pairs in "fov".
	"cq01": (
		[("srv", "gamemode survival @a[tag=me]"), ("srv", "effect give @a[tag=me] minecraft:resistance 99999 4 true")]
		+ wheat(7)
		+ walk_lane("cq01-wheat7", 1302.5, -60, 1299.5, 0, 3.0)
		+ wheat(3)
		+ walk_lane("cq01-wheat3", 1302.5, -60, 1299.5, 0, 3.0)
		+ [("srv", "fill 1307 -61 1301 1309 -61 1309 minecraft:farmland"),
		   ("srv", "setblock 1308 -60 1302 corn_delight:corn_crop[age=3,upper=false]"),
		   ("srv", "setblock 1308 -59 1302 corn_delight:corn_crop[age=3,upper=true]"),
		   ("srv", "setblock 1308 -60 1306 corn_delight:corn_crop[age=4,upper=false]"),
		   ("srv", "setblock 1308 -59 1306 corn_delight:corn_crop[age=4,upper=true]")]
		+ walk_lane("cq01-corn", 1308.5, -60, 1299.5, 0, 3.0)
		+ [("srv", "fill 1310 -61 1302 1314 -61 1306 minecraft:water"),
		   ("srv", "fill 1310 -61 1302 1314 -61 1306 farmersdelight:rice[age=3,supporting=true]"),
		   ("srv", "fill 1310 -60 1302 1314 -60 1306 farmersdelight:rice_panicles[age=3]"),
		   ("srv", "fill 1315 -63 1299 1316 -61 1309 minecraft:water")]
		+ walk_lane("cq01-rice-wade", 1312.5, -61, 1299.5, 0, 3.0)
		+ walk_lane("cq01-rice-swim", 1315.5, -62, 1299.5, 0, 3.0)
		+ wheat(7)
		+ [("srv", "gamemode creative @a[tag=me]")]
		+ walk_lane("cq01-creative-walk", 1302.5, -60, 1299.5, 0, 3.0)
		+ [("mark", "cq01-creative-fly"), ("srv", "tp @a[tag=me] 1302.5 -59.8 1299.5 0 0"), ("sleep", 1.5),
		   ("tap", "space"), ("sleep", 0.12), ("tap", "space"), ("sleep", 0.5),
		   ("srv", "zpth watch me 100 2"), ("walk", 3.0, "w"), ("sleep", 1), ("tap", "space"), ("sleep", 0.12),
		   ("tap", "space"), ("srv", "gamemode survival @a[tag=me]"), ("sleep", 1)]
		+ [("srv", "setblock 1320 -54 1302 minecraft:stone"), ("srv", "fill 1320 -60 1302 1320 -55 1302 farmersdelight:rope")]
		+ walk_lane("cq01-rope-walk", 1320.5, -60, 1300.5, 0, 1.2)
		+ [("mark", "cq01-rope-climb"), ("srv", "zpth watch me 80 2"), ("walk", 3.0, "space"), ("sleep", 1)]
		+ [("srv", "setblock 1326 -60 1302 minecraft:stone"), ("srv", "setblock 1325 -60 1302 minecraft:vine[east=true]"),
		   ("mark", "cq01-vine-stand"), ("srv", "tp @a[tag=me] 1325.5 -60 1302.5 0 0"), ("sleep", 1.5),
		   ("srv", "zpth watch me 40 2"), ("sleep", 2.5)]
		+ walk_lane("cq01-vine-walk", 1325.5, -60, 1299.5, 0, 3.0)
	),
	# field of view: screenshots of a fixed view with and without the block under / around the player
	"fov": (
		[("srv", "gamemode survival @a[tag=me]"), ("srv", "fill 1302 -60 1330 1302 -50 1330 minecraft:gold_block")]
		+ wheat(7)
		+ [("mark", "fov-wheat-in"), ("srv", "tp @a[tag=me] 1302.5 -60 1304.5 0 0"), ("sleep", 3), ("shot", "fov_wheat_in"),
		   ("srv", "fill 1300 -60 1302 1304 -60 1306 minecraft:air"), ("sleep", 3), ("shot", "fov_wheat_out"),
		   ("srv", "setblock 1302 -61 1304 immersivepetroleum:asphalt"), ("srv", "tp @a[tag=me] 1302.5 -60 1304.5 0 0"),
		   ("sleep", 3), ("shot", "fov_asphalt_on"), ("srv", "setblock 1302 -61 1304 minecraft:farmland"),
		   ("sleep", 3), ("shot", "fov_asphalt_off")]
	),
	# CQ-PTH-02: the server's dense_crop denseWalk set to 0.2 while the player walks in the wheat field
	"cq02": (
		[("srv", "gamemode survival @a[tag=me]")] + wheat(7)
		+ walk_lane("cq02-before", 1302.5, -60, 1299.5, 0, 3.0)
		+ [("mark", "cq02-edit"),
		   ("shell", "python3 $INST/tools/scratch_query/qrun.py toml $SRV/config/creativecompat-common.toml "
					 "surface.passthrough.dense_crop denseWalk 0.2"), ("sleep", 8)]
		+ walk_lane("cq02-after", 1302.5, -60, 1299.5, 0, 3.0)
		+ [("mark", "cq02-back"),
		   ("shell", "python3 $INST/tools/scratch_query/qrun.py toml $SRV/config/creativecompat-common.toml "
					 "surface.passthrough.dense_crop denseWalk 0.5"), ("sleep", 3)]
	),
	# CQ-PTH-03: FD and Quark rope: ground walk through the bottom block, climb, sneak, step off; 20-block shaft
	"cq03": (
		[("srv", "gamemode survival @a[tag=me]"), ("srv", "effect clear @a[tag=me]"),
		 ("srv", "setblock 1340 -54 1302 minecraft:stone"), ("srv", "fill 1340 -60 1302 1340 -55 1302 farmersdelight:rope"),
		 ("srv", "setblock 1344 -54 1302 minecraft:stone"), ("srv", "fill 1344 -60 1302 1344 -55 1302 quark:rope")]
		+ walk_lane("cq03-fd-walk", 1340.5, -60, 1300.5, 0, 1.0)
		+ [("mark", "cq03-fd-climb"), ("srv", "zpth watch me 60 2"), ("walk", 2.0, "space"),
		   ("mark", "cq03-fd-sneak"), ("srv", "zpth watch me 60 2"), ("walk", 2.0, "Shift_L"), ("sleep", 1)]
		+ walk_lane("cq03-quark-walk", 1344.5, -60, 1300.5, 0, 1.0)
		+ [("mark", "cq03-quark-climb"), ("srv", "zpth watch me 60 2"), ("walk", 2.0, "space"),
		   ("mark", "cq03-quark-sneak"), ("srv", "zpth watch me 60 2"), ("walk", 2.0, "Shift_L"), ("sleep", 1)]
		+ [("srv", "setblock 1348 -41 1302 minecraft:stone"), ("srv", "setblock 1349 -39 1302 minecraft:stone"),
		   ("srv", "fill 1349 -60 1302 1349 -40 1302 farmersdelight:rope"),
		   ("mark", "cq03-shaft"), ("srv", "tp @a[tag=me] 1348.5 -40 1302.5 -90 0"), ("sleep", 2),
		   ("srv", "zpth player @"), ("srv", "zpth watch me 300 5"), ("walk", 0.35, "w"), ("sleep", 14),
		   ("srv", "zpth watch me 2 1")]
	),
}


def wthit_shots():
	s = [("srv", "gamemode spectator @a[tag=me]")] + wheat(7)
	s += [("mark", "cq04-wheat7"), ("srv", "tp @a[tag=me] 1302.5 -57.5 1304.5 0 90"), ("sleep", 2.5), ("shot", "cq04_wheat7")]
	s += wheat(2)
	s += [("mark", "cq04-wheat2"), ("sleep", 2), ("shot", "cq04_wheat2")]
	s += [("srv", "setblock 1360 -60 1302 minecraft:cobweb"), ("mark", "cq04-lone-web"),
		  ("srv", "tp @a[tag=me] 1360.5 -59.5 1300.0 0 0"), ("sleep", 2.5), ("shot", "cq04_lone_web")]
	s += [("srv", "fill 1364 -60 1302 1366 -58 1304 minecraft:cobweb"), ("mark", "cq04-cube-face"),
		  ("srv", "tp @a[tag=me] 1365.5 -59.5 1300.5 0 0"), ("sleep", 2.5), ("shot", "cq04_cube_face"),
		  ("mark", "cq04-cube-inside"), ("srv", "tp @a[tag=me] 1365.5 -60.4 1302.8 0 0"), ("sleep", 2.5),
		  ("shot", "cq04_cube_inside")]
	s += [("srv", "setblock 1371 -60 1302 minecraft:stone"), ("srv", "setblock 1370 -60 1302 minecraft:vine[east=true]"),
		  ("mark", "cq04-vine"), ("srv", "tp @a[tag=me] 1370.5 -59.5 1300.0 0 0"), ("sleep", 2.5), ("shot", "cq04_vine")]
	s += [("srv", "setblock 1374 -60 1302 immersive_weathering:oak_leaf_pile[layers=3]"),
		  ("srv", "setblock 1378 -60 1302 immersive_weathering:oak_leaf_pile[layers=5]"),
		  ("mark", "cq04-pile3"), ("srv", "tp @a[tag=me] 1374.5 -59 1300.5 0 30"), ("sleep", 2.5), ("shot", "cq04_pile3"),
		  ("mark", "cq04-pile5"), ("srv", "tp @a[tag=me] 1378.5 -59 1300.5 0 30"), ("sleep", 2.5), ("shot", "cq04_pile5"),
		  ("srv", "item replace entity @a[tag=me] armor.feet with minecraft:leather_boots{Enchantments:[{id:\"minecraft:depth_strider\",lvl:3s}]}"),
		  ("mark", "cq04-pile5-ds"), ("sleep", 2.5), ("shot", "cq04_pile5_ds"),
		  ("srv", "item replace entity @a[tag=me] armor.feet with minecraft:air")]
	s += [("srv", "setblock 1382 -60 1302 minecraft:powder_snow"), ("mark", "cq04-powder"),
		  ("srv", "tp @a[tag=me] 1382.5 -59 1300.5 0 30"), ("sleep", 2.5), ("shot", "cq04_powder")]
	s += wheat(7)
	s += [("mark", "cq04-switch-off"),
		  ("shell", "python3 $INST/tools/scratch_query/qrun.py toml $GAME/config/creativecompat-common.toml surface.wthit passThrough false"),
		  ("sleep", 8), ("srv", "tp @a[tag=me] 1302.5 -57.5 1304.5 0 90"), ("sleep", 2.5), ("shot", "cq04_wheat7_off"),
		  ("shell", "python3 $INST/tools/scratch_query/qrun.py toml $GAME/config/creativecompat-common.toml surface.wthit passThrough true"),
		  ("srv", "gamemode survival @a[tag=me]")]
	return s


SCENARIOS["cq04"] = wthit_shots()

# CQ-PTH-01 second pass (r1115): survival wheat 7 / 3 again, wading from inside the paddy, swimming against the rice
# edge, flight set by the probe (a double-tapped space did not start flying), rope climb from inside the rope cell
# CQ-PTH-04 second pass: spectator eye is feet + 1.62, so the side-on shots aim from feet y -61.12 (first pass looked at the sky)
SCENARIOS["cq04b"] = [("srv", "gamemode spectator @a[tag=me]")] + [
	st for lab, x, y in (("lone_web", 1360, -61.12), ("vine", 1370, -61.12), ("pile3", 1374, -61.45),
						 ("pile5", 1378, -61.35), ("powder", 1382, -61.12))
	for st in (("mark", "cq04b-" + lab), ("srv", f"tp @a[tag=me] {x}.5 {y} 1300.3 0 0"), ("sleep", 2.5), ("shot", "cq04b_" + lab))
] + [("srv", "item replace entity @a[tag=me] armor.feet with minecraft:leather_boots{Enchantments:[{id:\"minecraft:depth_strider\",lvl:3s}]}"),
	 ("srv", "tp @a[tag=me] 1378.5 -61.35 1300.3 0 0"), ("sleep", 2.5), ("shot", "cq04b_pile5_ds"),
	 ("srv", "item replace entity @a[tag=me] armor.feet with minecraft:air"), ("srv", "gamemode survival @a[tag=me]")]

# CQ-PTH-03 second pass: sneak while on the rope off the ground, then a 20-block drop down the rope column
SCENARIOS["cq03b"] = [
	("srv", "gamemode survival @a[tag=me]"), ("srv", "effect clear @a[tag=me]"),
	("mark", "cq03b-fd-sneak"), ("srv", "tp @a[tag=me] 1340.5 -57 1302.5 0 0"), ("sleep", 0.1),
	("srv", "zpth watch me 60 2"), ("walk", 3.0, "Shift_L"), ("sleep", 1),
	("srv", "effect give @a[tag=me] minecraft:instant_health 1 5"), ("sleep", 1), ("mark", "cq03b-shaft"), ("srv", "tp @a[tag=me] 1349.5 -41.5 1302.5 0 0"), ("srv", "zpth player @"),
	("srv", "zpth watch me 400 10"), ("sleep", 18), ("srv", "zpth player @")]

SCENARIOS["cq01b"] = (
	[("srv", "gamemode survival @a[tag=me]")] + wheat(7)
	+ walk_lane("cq01b-wheat7", 1302.5, -60, 1299.5, 0, 3.0)
	+ wheat(3)
	+ walk_lane("cq01b-wheat3", 1302.5, -60, 1299.5, 0, 3.0)
	+ walk_lane("cq01b-rice-wade", 1312.5, -61, 1302.3, 0, 2.0)
	+ walk_lane("cq01b-rice-swim", 1315.25, -62, 1300.5, 0, 3.0)
	+ wheat(7)
	+ [("srv", "gamemode creative @a[tag=me]"), ("mark", "cq01b-creative-fly"),
	   ("srv", "tp @a[tag=me] 1302.5 -59.7 1299.5 0 0"), ("sleep", 1), ("srv", "zpth fly @ME true"), ("sleep", 1),
	   ("srv", "zpth watch me 100 2"), ("walk", 3.0, "w"), ("sleep", 1), ("srv", "zpth fly @ME false"),
	   ("srv", "gamemode survival @a[tag=me]"), ("sleep", 1)]
	+ [("mark", "cq01b-rope-climb"), ("srv", "tp @a[tag=me] 1320.5 -60 1302.5 0 0"), ("sleep", 1.5),
	   ("srv", "zpth watch me 80 2"), ("walk", 2.5, "space"), ("sleep", 1)]
)


def main():
	if len(sys.argv) < 5:
		sys.exit(__doc__)
	game, out, name, scen = os.path.abspath(sys.argv[1]), sys.argv[2], sys.argv[3], sys.argv[4]
	shots = sys.argv[5] if len(sys.argv) > 5 else os.path.join(game, "pth_shots")
	os.makedirs(shots, exist_ok=True)
	c = qrun.Client(game)
	sp = out == "sp"
	srv_dir = "" if sp else os.path.join(os.path.abspath(out), "srv")

	def srv(cmd):
		cmd = cmd.replace("@a[tag=me]", name).replace("@ME", name).replace("zpth player @", "zpth player " + name)
		if sp:
			c.cmd("/" + cmd)
		else:
			subprocess.run(["bash", SCRATCH, "cmd", out, cmd], capture_output=True)
			time.sleep(0.3)

	srv(f"tag {name} add me")
	for st in SCENARIOS[scen]:
		k = st[0]
		t = time.strftime("%H:%M:%S")
		if k == "srv":
			srv(st[1])
		elif k == "mark":
			srv("say PTHCLI " + st[1])
		elif k == "walk":
			keys = st[2].split()
			subprocess.run(["xdotool", "windowactivate", "--sync", c.w])
			for key in keys:
				subprocess.run(["xdotool", "keydown", "--window", c.w, key])
			time.sleep(float(st[1]))
			for key in reversed(keys):
				subprocess.run(["xdotool", "keyup", "--window", c.w, key])
		elif k == "tap":
			subprocess.run(["xdotool", "windowactivate", "--sync", c.w])
			subprocess.run(["xdotool", "key", "--window", c.w, *st[1].split()])
		elif k == "sleep":
			time.sleep(float(st[1]))
		elif k == "shot":
			c.shot(os.path.join(shots, st[1] + ".png"))
		elif k == "shell":
			subprocess.run(["bash", "-c", st[1]], env=dict(os.environ, INST=INST, SRV=srv_dir, GAME=game))
		print(t, *[str(x) for x in st], flush=True)


if __name__ == "__main__":
	main()
