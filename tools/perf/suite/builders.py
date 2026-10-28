"""Command builders for perf suite scenarios: each returns a list of console commands (sent over RCON, so every
command stays under rcon.MAX_COMMAND). Positions are block coordinates; the flat lab's surface is y = -60."""
import random

FLAT_Y = -60


def walls(x0, z0, x1, z1, y, block, height=1):
    top = y + height - 1
    return [f"fill {x0} {y} {z0} {x1} {top} {z0} {block}", f"fill {x0} {y} {z1} {x1} {top} {z1} {block}",
            f"fill {x0} {y} {z0} {x0} {top} {z1} {block}", f"fill {x1} {y} {z0} {x1} {top} {z1} {block}"]


def clear(x0, y0, z0, x1, y1, z1):
    return [f"fill {x0} {y0} {z0} {x1} {y1} {z1} air"]


def summon_scatter(types, n, x0, z0, x1, z1, y, nbt="{PersistenceRequired:1b}", seed=1):
    """n mobs spread evenly over `types`, at seeded random spots inside the box."""
    rnd = random.Random(seed)
    out = []
    for i in range(n):
        t = types[i % len(types)]
        out.append(f"summon {t} {rnd.uniform(x0 + 1, x1 - 1):.1f} {y} {rnd.uniform(z0 + 1, z1 - 1):.1f} {nbt}")
    return out


def item_grid(items, n, x0, z0, y, spacing=1.5, cols=25):
    """n item entities that never despawn and can't be picked up, far enough apart not to merge."""
    out = []
    for i in range(n):
        item = items[i % len(items)]
        x, z = x0 + (i % cols) * spacing, z0 + (i // cols) * spacing
        out.append(f'summon item {x:.1f} {y} {z:.1f} {{Item:{{id:"{item}",Count:1b}},Age:-32768s,PickupDelay:32767s}}')
    return out


def stacks_nbt(item, slots, count=64):
    return "{Items:[" + ",".join(f'{{Slot:{s}b,id:"{item}",Count:{count}b}}' for s in range(slots)) + "]}"


def gamerules_fixed(time=6000):
    """Reproducible world state: no day or weather cycle, fixed time."""
    return ["gamerule doDaylightCycle false", "gamerule doWeatherCycle false", f"time set {time}", "weather clear"]
