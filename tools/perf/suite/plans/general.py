"""General server benchmarks, G- (feature_writeup/perf_suite.md 5). Two worlds:
  N  normal world on the eval seed, structures on, the play area pregenerated in setup (worldgen is not measured
     here beyond the boot), fixed noon and clear weather
  S  flat lab: one site per stress scenario, 2000 blocks apart, so earlier sites unload when the bot leaves
Scenario fields: see perf_suite.py run_scenario. Counts scale with --scale."""
from builders import FLAT_Y, walls, summon_scatter, item_grid, stacks_nbt, gamerules_fixed


def bots_spread(ctx, names, d=600):
    x, y, z = ctx.spawn
    spots = [(x, y, z), (x + d, 200, z), (x - d, 200, z), (x, 200, z + d), (x, 200, z - d)]
    return [f"pbot spawn {n} {sx} {sy} {sz}" for n, (sx, sy, sz) in zip(names, spots)]


def bot_home(ctx):
    x, y, z = ctx.spawn
    return [f"pbot spawn bot1 {x} {y} {z}"]


N = dict(
    id="N", title="normal world, pregenerated play area", world="normal", level="pgN",
    init=lambda ctx: gamerules_fixed(6000),
    # spawn square (bots up to 600 out, view distance 10) and the travel corridor east of spawn
    pregen=lambda ctx: [(ctx.spawn[0], ctx.spawn[2], 800, 800),
                        (ctx.spawn[0] + 1600, ctx.spawn[2], 1700, 160)],
    scenarios=[
        dict(id="G02", title="idle, no player", settle=240, measure=120),
        dict(id="G03a", title="one bot at spawn, day", setup=bot_home, settle=600, measure=300),
        dict(id="G03b", title="one bot at spawn, night (hostile spawning)",
             setup=lambda ctx: bot_home(ctx) + ["time set 18000"], settle=180, measure=300,
             teardown=lambda ctx: ["time set 6000"]),
        dict(id="G04", title="four bots, 600 blocks apart",
             setup=lambda ctx: bots_spread(ctx, ["bot1", "bot2", "bot3", "bot4"]), settle=300, measure=300),
        dict(id="G06", title="save-all flush with four bots' chunks loaded", kind="save",
             setup=lambda ctx: bots_spread(ctx, ["bot1", "bot2", "bot3", "bot4"]), settle=30, measure=60,
             teardown=lambda ctx: ["pbot remove bot2", "pbot remove bot3", "pbot remove bot4"]),
        dict(id="G05a", title="travel 30 blocks/s (elytra) through pregenerated land",
             setup=bot_home, settle=30, measure=110,
             start=lambda ctx: [f"pbot path bot1 {ctx.spawn[0] + 3000} 200 {ctx.spawn[2]} 1.5"],
             teardown=bot_home),
        dict(id="G05b", title="travel 14 blocks/s (horse) through pregenerated land",
             setup=bot_home, settle=60, measure=105,
             start=lambda ctx: [f"pbot path bot1 {ctx.spawn[0] + 1400} 120 {ctx.spawn[2]} 0.7"],
             teardown=bot_home),
        dict(id="G05c", title="travel 10 blocks/s into new land (worldgen; off by default)", enabled=False,
             setup=bot_home, settle=30, measure=210,
             start=lambda ctx: [f"pbot path bot1 {ctx.spawn[0]} 120 {ctx.spawn[2] + 2000} 0.5"],
             teardown=bot_home),
        dict(id="G07", title="/reload with one bot", kind="reload", setup=bot_home, settle=30, measure=90),
        dict(id="G12", title="soak: two bots, 45 minutes (drift, heap floor)",
             setup=lambda ctx: bots_spread(ctx, ["bot1", "bot2"]), settle=60, measure=2700, slices=300,
             teardown=lambda ctx: ["pbot remove bot2"]),
        dict(id="G01b", title="restart of the same world (twice)", kind="restart", repeat=2),
    ],
)


def site_bot(ctx, k):
    x, z = ctx.site(k)
    return [f"pbot spawn bot1 {x} {FLAT_Y} {z - 24}"]


def g08(ctx):
    x, z = ctx.site(1)
    items = ["minecraft:cobblestone", "minecraft:wheat", "minecraft:carrot", "minecraft:iron_ingot",
             "minecraft:oak_log", "minecraft:bone", "minecraft:egg", "minecraft:string", "minecraft:apple",
             "minecraft:dirt"]
    return site_bot(ctx, 1) + item_grid(items, 500 * ctx.scale, x - 18, z - 15, FLAT_Y + 1)


def g09(ctx):
    x, z = ctx.site(2)
    return (site_bot(ctx, 2) + walls(x - 20, z - 20, x + 20, z + 20, FLAT_Y, "minecraft:oak_fence")
            + summon_scatter(["minecraft:cow", "minecraft:sheep", "minecraft:pig", "minecraft:chicken"],
                             200 * ctx.scale, x - 20, z - 20, x + 20, z + 20, FLAT_Y))


def g10(ctx):
    x, z = ctx.site(3)
    return (site_bot(ctx, 3)
            + [f"fill {x - 14} {FLAT_Y} {z - 14} {x + 14} {FLAT_Y + 5} {z + 14} minecraft:stone hollow"]
            + summon_scatter(["minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper"],
                             100 * ctx.scale, x - 13, z - 13, x + 13, z + 13, FLAT_Y + 1))


def g11a(ctx):
    x, z = ctx.site(4)
    side = 32 if ctx.scale == 1 else 45
    x0, z0, x1, z1 = x - side // 2, z - side // 2, x - side // 2 + side - 1, z - side // 2 + side - 1
    return site_bot(ctx, 4) + [
        f"fill {x0} {FLAT_Y} {z0} {x1} {FLAT_Y} {z1} minecraft:barrel",
        f"fill {x0} {FLAT_Y + 1} {z0} {x1} {FLAT_Y + 1} {z1} minecraft:hopper[facing=down]",
        f"fill {x0} {FLAT_Y + 2} {z0} {x1} {FLAT_Y + 2} {z1} minecraft:barrel{stacks_nbt('cobblestone', 27)}",
    ]


def g11b(ctx):
    x, z = ctx.site(5)
    side = 16 * ctx.scale
    nbt = '{Items:[{Slot:0b,id:"raw_iron",Count:64b},{Slot:1b,id:"coal",Count:64b}]}'
    return site_bot(ctx, 5) + [f"fill {x - side // 2} {FLAT_Y} {z - side // 2} {x + side // 2 - 1} {FLAT_Y} "
                               f"{z + side // 2 - 1} minecraft:furnace{nbt}"]


S = dict(
    id="S", title="flat lab, one site per scenario", world="flat", level="pgS",
    # spawn-animals=false would make vanilla discard every animal each tick (G09 measured an empty pen in g1);
    # natural spawning is off through doMobSpawning instead
    props={"difficulty": "normal", "spawn-monsters": "false", "spawn-animals": "true"},
    init=lambda ctx: gamerules_fixed(6000) + ["gamerule doMobSpawning false"],
    scenarios=[
        dict(id="S00", title="baseline: one bot on an empty flat site", setup=lambda ctx: site_bot(ctx, 0),
             settle=60, measure=120),
        dict(id="G08", title="500 item entities on the ground (ItemPhysic)", setup=g08, settle=30, measure=180),
        dict(id="G09", title="200 vanilla farm animals in one pen", setup=g09, settle=60, measure=180),
        dict(id="G10", title="100 hostile mobs in a dark box", setup=g10, settle=60, measure=180),
        dict(id="G11a", title="1024 hoppers moving items barrel to barrel", setup=g11a, settle=30, measure=180),
        dict(id="G11b", title="256 furnaces smelting", setup=g11b, settle=30, measure=180),
    ],
)

GROUPS = [S, N]
