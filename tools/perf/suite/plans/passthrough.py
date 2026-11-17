"""Pass-through blocks, PT- (feature_writeup/plan_passthrough.md 7.2; perf_suite.md 5.1). Flat lab, one site per
scene and per switch state: each scene runs twice, `a` with [surface.passthrough] enabled = true and `b` with false
(set over RCON with /cdevbench passthrough enabled, Creative Devtools), on two identical sites so nothing carries over.
Compare a against b in summary.md (same group, same window). Budget (plan 7.2): under 0.5 ms added mean tick time at
500 affected entities.

    setsid nohup python3 tools/perf/suite/perf_suite.py run passthrough pt1 > /dev/null 2>&1 &

Needs the Creative Compat jar with the pass-through tags (the Compat build of 2026-11-15 or later) and Creative
Devtools with /cdevbench in the scratch server's mods. Counts scale with --scale.
"""
from builders import FLAT_Y, walls, summon_scatter, gamerules_fixed

CORN_LOW = "corn_delight:corn_crop[age=7,upper=false]"
CORN_TOP = "corn_delight:corn_crop[age=7,upper=true]"
FARMLAND = "minecraft:farmland[moisture=7]"
WHEAT = "minecraft:wheat[age=7]"


def switch(on):
	return [f"cdevbench passthrough enabled {'true' if on else 'false'}"]


def bot_at(x, z, dz=-24):
	return [f"pbot spawn bot1 {x} {FLAT_Y} {z + dz}"]


def field(x0, z0, x1, z1, low, top=None):
	"""Farmland at the surface, the crop on it (and its top block for a two-block crop)."""
	out = [f"fill {x0} {FLAT_Y - 1} {z0} {x1} {FLAT_Y - 1} {z1} {FARMLAND}",
		   f"fill {x0} {FLAT_Y} {z0} {x1} {FLAT_Y} {z1} {low}"]
	if top:
		out.append(f"fill {x0} {FLAT_Y + 1} {z0} {x1} {FLAT_Y + 1} {z1} {top}")
	return out


# --- scenes; k is the site index, so a and b get different sites ---

def pt01(on, k):
	"""200 cows penned in a 32x32 corn field."""
	def build(ctx):
		x, z = ctx.site(k)
		h = 16 * ctx.scale
		return (switch(on) + bot_at(x, z, -h - 8) + field(x - h, z - h, x + h - 1, z + h - 1, CORN_LOW, CORN_TOP)
				+ walls(x - h - 1, z - h - 1, x + h, z + h, FLAT_Y, "minecraft:oak_fence", 2)
				+ summon_scatter(["minecraft:cow"], 200 * ctx.scale, x - h, z - h, x + h - 1, z + h - 1, FLAT_Y))
	return build


def pt02(on, k):
	"""50 farmer villagers working a ripe wheat field with composters (their job site)."""
	def build(ctx):
		x, z = ctx.site(k)
		h = 16 * ctx.scale
		n = 50 * ctx.scale
		nbt = '{PersistenceRequired:1b,VillagerData:{profession:"minecraft:farmer",level:2,type:"minecraft:plains"}}'
		cmds = switch(on) + bot_at(x, z, -h - 8) + field(x - h, z - h, x + h - 1, z + h - 1, WHEAT)
		cmds += walls(x - h - 2, z - h - 2, x + h + 1, z + h + 1, FLAT_Y, "minecraft:oak_fence", 2)
		# composters (their job site) in rows just inside the north and south fences, one per villager
		for i in range(n):
			row, off = divmod(i, 2 * h)
			pz = z - h - 1 if row % 2 == 0 else z + h
			cmds.append(f"setblock {x - h + off} {FLAT_Y} {pz - row // 2 if row % 2 == 0 else pz + row // 2} "
						f"minecraft:composter")
		return cmds + summon_scatter(["minecraft:villager"], n, x - h, z - h, x + h - 1, z + h - 1, FLAT_Y, nbt=nbt)
	return build


def pt03(on, k):
	"""100 zombies inside a 9x9x5 cobweb block, roofed (no sun)."""
	def build(ctx):
		x, z = ctx.site(k)
		y = FLAT_Y
		return (switch(on) + bot_at(x, z)
				+ [f"fill {x - 5} {y - 1} {z - 5} {x + 5} {y + 5} {z + 5} minecraft:stone hollow",
				   f"fill {x - 4} {y} {z - 4} {x + 4} {y + 4} {z + 4} minecraft:cobweb"]
				+ summon_scatter(["minecraft:zombie"], 100 * ctx.scale, x - 4, z - 4, x + 4, z + 4, y + 1))
	return build


def pt04(on, k):
	"""500 dropped items thrown down into a 25x20 web slab (3 deep); they stick (a) or fall through (b)."""
	def build(ctx):
		x, z = ctx.site(k)
		y = FLAT_Y
		items = ["minecraft:cobblestone", "minecraft:wheat_seeds", "minecraft:string", "minecraft:bone",
				 "minecraft:arrow", "minecraft:apple", "minecraft:dirt", "minecraft:stick"]
		cmds = switch(on) + bot_at(x, z) + [f"fill {x - 13} {y + 2} {z - 10} {x + 12} {y + 4} {z + 10} minecraft:cobweb"]
		n = 500 * ctx.scale
		for i in range(n):
			ix, iz = x - 12 + (i % 25) * 1.0 + 0.5, z - 10 + (i // 25) % 20 + 0.5
			cmds.append(f'summon item {ix:.1f} {y + 8} {iz:.1f} {{Item:{{id:"{items[i % len(items)]}",Count:1b}},'
						f'Age:-32768s,PickupDelay:32767s,Motion:[0.0d,-0.6d,0.0d]}}')
		return cmds
	return build


def pt05(on, k):
	"""100 arrows a second into a 9x9x9 web block (five repeating command blocks, 20 arrows a second each); in the
    surrounded middle they hang (a), else they cross and stick in the stone behind (b). Pickable arrows, so the
    10-minute life and the 128-per-chunk cap run too."""
	def build(ctx):
		x, z = ctx.site(k)
		y = FLAT_Y
		cmds = switch(on) + bot_at(x, z, -30) + [
			f"fill {x - 4} {y} {z - 4} {x + 4} {y + 8} {z + 4} minecraft:cobweb",
			f"fill {x + 6} {y} {z - 6} {x + 6} {y + 10} {z + 6} minecraft:stone",
		]
		for i in range(5):
			ay, az = y + 2 + i, z - 2 + i
			inner = f"summon arrow {x - 8} {ay + 0.5} {az + 0.5} {{pickup:1b,Motion:[2.0d,0.0d,0.0d]}}"
			cmds.append(f"setblock {x - 10} {ay} {az} minecraft:repeating_command_block[facing=up]"
						f"{{auto:1b,Command:\"{inner}\"}}")
		return cmds

	def stop(ctx):
		x, z = ctx.site(k)
		return [f"fill {x - 10} {FLAT_Y} {z - 6} {x - 10} {FLAT_Y + 10} {z + 6} minecraft:air"]
	return build, stop


def pt06(on, k):
	"""One bot walking a straight line through a 16x96 corn strip at walking speed, about 25 s (the pbot path is one
    target, so a line, not circles)."""
	def build(ctx):
		x, z = ctx.site(k)
		return (switch(on) + [f"pbot spawn bot1 {x} {FLAT_Y} {z - 50}"]
				+ field(x - 8, z - 48, x + 7, z + 47, CORN_LOW, CORN_TOP))

	def start(ctx):
		x, z = ctx.site(k)
		return [f"pbot path bot1 {x} {FLAT_Y} {z + 50} 0.2"]
	return build, start


def pair(sid, title, maker, k, settle=60, measure=180, **extra):
	"""Scene sid at site k (switch on) and site k + 1 (switch off)."""
	rows = []
	for on, suffix, site in ((True, "a", k), (False, "b", k + 1)):
		made = maker(on, site)
		row = dict(id=sid + suffix, title=f"{title} ({'pass-through on' if on else 'pass-through off'})",
				   settle=settle, measure=measure, **extra)
		if isinstance(made, tuple):
			row["setup"] = made[0]
			if maker is pt06:
				row["start"] = made[1]
			else:
				row["teardown"] = made[1]
		else:
			row["setup"] = made
		rows.append(row)
	return rows


P = dict(
	id="P", title="flat lab, pass-through scenes (each on and off)", world="flat", level="pgP",
	# spawn-animals=false would discard every animal each tick (general.py S); monsters off, zombies are summoned
	props={"difficulty": "normal", "spawn-monsters": "false", "spawn-animals": "true", "enable-command-block": "true"},
	init=lambda ctx: gamerules_fixed(6000) + ["gamerule doMobSpawning false", "gamerule randomTickSpeed 0",
											  "gamerule mobGriefing false"],
	scenarios=(
		[dict(id="PT00", title="baseline: one bot on an empty flat site", setup=lambda ctx: bot_at(*ctx.site(0)),
			  settle=60, measure=120)]
		+ pair("PT01", "200 cows in a 32x32 corn field", pt01, 1)
		+ pair("PT02", "50 farmer villagers in a wheat field", pt02, 3)
		+ pair("PT03", "100 zombies in a 9x9x5 web block", pt03, 5)
		+ pair("PT04", "500 items thrown into webs", pt04, 7, settle=30)
		+ pair("PT05", "100 arrows a second into a web block", pt05, 9, settle=20, measure=120)
		+ pair("PT06", "one bot walking through a corn strip", pt06, 11, settle=20, measure=25)
		+ [dict(id="PT99", title="switch back on", setup=lambda ctx: switch(True), settle=5, measure=5,
				jfr=False, census=False)]
	),
)

GROUPS = [P]
