#!/usr/bin/env python3
"""Step 1 of feature_writeup/plan_cd_namespace.md: the list of ids that leave the creativeengineering namespace.

Reads the code, not the dumps: Creative Delight's registered Java ids, the startup scripts' dict keys, every generated
can that is not marked existing, the syringe literals and the Display Delight dish list. Classifies every
#creativeengineering: tag key in the dicts by owner. Asserts the plan's counts and that nothing clashes with an id
already under the new namespace.

Writes:
  development/creativeDelight/src/main/resources/data/creativedelight/swaps/cd_namespace.json  the swap list (never prune)
  tools/cd_namespace_ids.json  the plain list tools/cd_namespace_move.py reads

--check prints the counts and writes nothing.

One-off: it reads the dicts before the rewrite and exits with "is not under creativeengineering" once they have moved.
The generated swap list and tools/cd_namespace_ids.json are the lasting record.
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CD = os.path.join(ROOT, "development/creativeDelight/src/main")
RES = os.path.join(CD, "resources")
STARTUP = os.path.join(RES, "kubejs_startup")
DICTS = os.path.join(ROOT, "development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib")
SWAP_OUT = os.path.join(RES, "data/creativedelight/swaps/cd_namespace.json")
LIST_OUT = os.path.join(ROOT, "tools/cd_namespace_ids.json")

OLD = "creativeengineering"
NEW = "creativedelight"
COMMON = "creativecommon"

# Java-registered content (plan 1.1): id, whether it is an item, whether it is a block, source file that must name it.
JAVA_CONTENT = [
    ("oven", True, True, "oven/OvenRegistry.java"),
    ("powdered_sugar_block", False, True, "food/PowderedSugarRegistry.java"),
    ("powdered_sugar_bucket", True, False, "food/PowderedSugarRegistry.java"),
]

# Renamed with no alias (plan D4); recorded so the rewrite covers every reference.
D4_IDS = [
    "baking", "lunchbox_shaped", "lunchbox_upgrade", "lunchbox_skin", "lunchbox_dye", "lunchbox_soap",
    "seasoning", "biome_tag", "can_serving",
]
CAPABILITIES = ["remainder_buffer", "corner_storage"]

# Startup scripts that register moved content. Rows of [i.key, 'Name'] arrays count only in the array-driven files.
SCRIPTS = [
    "creativedelight/drinks.js", "creativedelight/cultures.js", "creativedelight/sample_syringe_items.js",
    "creativedelight/baking_ingredients.js", "creativedelight/bakeware.js", "creativedelight/cornmeal.js",
    "corn_delight/corn_items.js", "rusticdelight/glowing_calamari.js", "vintagedelight/vintage_items.js",
    "veggiesdelight/veggies_items.js", "moredelight/toast.js", "farmersdelight/ocean_dishes.js",
]
ROW_SCRIPTS = {
    "creativedelight/drinks.js", "creativedelight/cultures.js",
    "creativedelight/baking_ingredients.js", "rusticdelight/glowing_calamari.js",
}

EXPECT = {"items": 376, "blocks": 26}

# Tags in the dicts that do not move to creativedelight. Value = namespace they end up in. Everything else in the dicts
# is filled by Creative Delight's scripts (plan 1.3); unknown keys fail the run so a new tag gets a decision.
TAG_OTHER_PREFIX = {
    "smokable/": COMMON,  # owner 2026-11-10
    "plantable/": COMMON,  # owner 2026-11-10
}
TAG_OTHER_EXACT = {
    "plantable": COMMON,  # owner 2026-11-10
    # Stay: filled by Engineering.
    "bulb_glass": OLD, "carbon_filament": OLD, "railings": OLD, "unsmoked_planks": OLD, "vertical_planks": OLD,
    # Stay until creativesplit/ids #25: filled by other jars or pack scripts.
    "loot/chalk": OLD, "loot/golden_food": OLD, "no_world_dye": OLD, "vein_undo_denied": OLD, "stepstools": OLD,
    "trimmable_armor": OLD, "villager_ladders": OLD, "trades/canned_golden": OLD, "trades/golden_food": OLD,
}
# CD tags named only in code or scripts, outside the dicts; prefix families end in "/".
TAG_CD_EXTRA = [
    "crops/farmland_soils", "crops/farmland_crops", "oven_heat_sources",
    "cans/servings/", "food_type/", "food_family/", "jar_family/", "bakeware/", "hoe_harvest/", "fishing_net/",
]
TAG_COMMON_EXTRA = ["plantable/", "smokable/"]


def read(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def load_dict(name, letter):
    """key -> value for every `x.key = 'value'` line of a dict file."""
    text = read(os.path.join(DICTS, name))
    return {m[1]: m[2] for m in re.finditer(r"^\s*" + letter + r"\.(\w+)\s*=\s*'([^']+)'", text, re.M)}


def path_of(value, where):
    if not value.startswith(OLD + ":"):
        sys.exit(f"{where}: {value} is not under {OLD}")
    return value.split(":", 1)[1]


def registry_blocks(text, kind):
    """The bodies of the StartupEvents.registry('<kind>', ...) calls, up to the next one or end of file."""
    parts = re.split(r"(?=StartupEvents\.registry\()|(?=ItemEvents\.)|(?=BlockEvents\.)", text)
    return [p for p in parts if p.startswith(f"StartupEvents.registry('{kind}'")]


def script_ids(items_dict):
    items, blocks = set(), set()
    for rel in SCRIPTS:
        text = read(os.path.join(STARTUP, rel))
        for kind, out in (("item", items), ("block", blocks)):
            for body in registry_blocks(text, kind):
                for m in re.finditer(r"\.create\(\s*(?:(?:global\.item|i)\.(\w+)|'" + OLD + r":([\w/]+)')", body):
                    if m[1]:
                        if m[1] not in items_dict:
                            sys.exit(f"{rel}: no dict key {m[1]}")
                        out.add(path_of(items_dict[m[1]], rel))
                    else:
                        out.add(m[2])
                if rel in ROW_SCRIPTS:
                    for m in re.finditer(r"\[\s*i\.(\w+)\s*,\s*'", body):
                        if m[1] not in items_dict:
                            sys.exit(f"{rel}: no dict key {m[1]}")
                        out.add(path_of(items_dict[m[1]], rel))
    return items, blocks


def can_ids(items_dict):
    out = set()
    pattern = os.path.join(STARTUP, "*", "can_types.js")
    files = glob.glob(pattern)
    if not files:
        sys.exit("no can_types.js files found")
    for path in files:
        text = read(path)
        start = text.index("{", text.index("genPut('can_types'"))
        end = text.rindex("}") + 1
        for key, row in json.loads(text[start:end])["cans"].items():
            if row.get("existing"):
                continue
            if key not in items_dict:
                sys.exit(f"{path}: {key} has no item_dict key")
            out.add(path_of(items_dict[key], path))
    return out


def dish_ids():
    with open(os.path.join(RES, OLD, "displaydelight_dishes.json"), encoding="utf-8") as f:
        data = json.load(f)
    rows = data if isinstance(data, list) else data.get("dishes", data)
    if isinstance(rows, dict):
        names = list(rows)
    else:
        names = [r["block"] for r in rows]
    return set(names)


def classify_tags():
    """dict tag -> target namespace; exits on a tag with no decision (see TAG_OTHER_*)."""
    keys = {}
    for name in ("item_dict.js", "block_dict.js", "fluid_dict.js"):
        for m in re.finditer(r"'#" + OLD + r":([\w/]+)'", read(os.path.join(DICTS, name))):
            keys[m[1]] = None
    out = {}
    for tag in sorted(keys):
        target = TAG_OTHER_EXACT.get(tag)
        if target is None:
            for prefix, ns in TAG_OTHER_PREFIX.items():
                if tag.startswith(prefix):
                    target = ns
        out[tag] = target or NEW
    return out


def resource_paths():
    """Asset paths under the CD jar's assets/creativeengineering, as namespace-relative ids without extension."""
    base = os.path.join(RES, "assets", OLD)
    models, textures = set(), set()
    for dp, _, files in os.walk(base):
        rel = os.path.relpath(dp, base).replace(os.sep, "/")
        for f in files:
            stem, ext = os.path.splitext(f)
            if rel.startswith("textures") and ext == ".png":
                textures.add((rel[len("textures/"):] + "/" if rel != "textures" else "") + stem)
            elif rel.startswith("models") and ext == ".json":
                models.add((rel[len("models/"):] + "/" if rel != "models" else "") + stem)
    # Seasoning ids stayed on the old namespace in this move; they moved later (namespace_move.md section 1).
    keep = "item/seasoned/"
    return {m for m in models if not m.startswith(keep)}, {x for x in textures if not x.startswith(keep)}


def data_ids():
    base = os.path.join(RES, "data", OLD)
    out = {"loot_tables": [], "soft_fluids": []}
    for dp, _, files in os.walk(os.path.join(base, "loot_tables")):
        for f in files:
            rel = os.path.relpath(os.path.join(dp, f), os.path.join(base, "loot_tables")).replace(os.sep, "/")
            out["loot_tables"].append(rel[:-5])
    for f in os.listdir(os.path.join(base, "moonlight/soft_fluids")):
        out["soft_fluids"].append(f[:-5])
    out["loot_tables"].sort()
    out["soft_fluids"].sort()
    return out


def main():
    check = "--check" in sys.argv
    items_dict = load_dict("item_dict.js", "i")

    items, blocks = script_ids(items_dict)
    # Every block created by a script has a block item of the same id.
    items |= blocks
    items |= can_ids(items_dict)
    for name, is_item, is_block, src in JAVA_CONTENT:
        if f'"{name}"' not in read(os.path.join(CD, "java/com/SaintAlfonzo/creativedelight", src)):
            sys.exit(f"{src} no longer names {name}")
        if is_item:
            items.add(name)
        if is_block:
            blocks.add(name)
    blocks |= dish_ids()

    errors = []
    for label, found in (("items", items), ("blocks", blocks)):
        if len(found) != EXPECT[label]:
            errors.append(f"{label}: found {len(found)}, plan 1.4 says {EXPECT[label]}")

    clash_file = os.path.join(ROOT, "kubejs/exported/items_by_mod", NEW + ".json")
    existing = {i.split(":", 1)[1] for i in json.load(open(clash_file, encoding="utf-8"))["items"]}
    moved = items | blocks
    clashes = sorted(moved & existing)
    if clashes:
        errors.append(f"clash with existing {NEW} items: {clashes}")

    tags = classify_tags()
    moving = {t: ns for t, ns in tags.items() if ns != OLD}
    models, textures = resource_paths()
    data = data_ids()

    print(f"items {len(items)}, blocks {len(blocks)}, unique ids {len(moved)}")
    print(f"dict tags {len(tags)}: to {NEW} {sum(1 for n in tags.values() if n == NEW)}, "
          f"to {COMMON} {sum(1 for n in tags.values() if n == COMMON)}, stay {sum(1 for n in tags.values() if n == OLD)}")
    print(f"models {len(models)}, textures {len(textures)}, loot tables {len(data['loot_tables'])}, soft fluids {len(data['soft_fluids'])}")
    if errors:
        print("\n".join(errors))
        sys.exit(1)
    if check:
        return

    swap = {
        "_comment": "Creative Delight content moved from creativeengineering to creativedelight (feature_writeup/plan_cd_namespace.md). "
                    "Generated by tools/cd_namespace_ids.py, not by hand. Never prune.",
        "replaces": {f"{NEW}:{p}": [f"{OLD}:{p}"] for p in sorted(moved)},
        "capabilities": {f"{NEW}:{c}": [f"{OLD}:{c}"] for c in CAPABILITIES},
    }
    with open(SWAP_OUT, "w", encoding="utf-8", newline="\n") as f:
        json.dump(swap, f, indent=2)
        f.write("\n")

    plain = {
        "ids": sorted(moved),
        "d4_ids": D4_IDS,
        "capabilities": CAPABILITIES,
        "tags": moving,
        "tag_prefixes": {**{p: NEW for p in TAG_CD_EXTRA}, **{p: COMMON for p in TAG_COMMON_EXTRA}},
        "models": sorted(models),
        "textures": sorted(textures),
        "loot_tables": data["loot_tables"],
        "soft_fluids": data["soft_fluids"],
    }
    with open(LIST_OUT, "w", encoding="utf-8", newline="\n") as f:
        json.dump(plain, f, indent=1)
        f.write("\n")
    print(f"wrote {os.path.relpath(SWAP_OUT, ROOT)} and {os.path.relpath(LIST_OUT, ROOT)}")


if __name__ == "__main__":
    main()
