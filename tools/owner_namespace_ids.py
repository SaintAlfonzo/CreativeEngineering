#!/usr/bin/env python3
"""Step 0 of feature_writeup/namespace_move.md: the ids, tags and resource paths that leave a namespace they do not own.

The owner table below is the decision record (section 2.1, 2.2 and 4 of the plan). The script checks it against the code:
every moved item id must be in the Creative Common dicts or registered in its owner jar's sources, every
creativeengineering item the game dump lists must be Engineering's own, Creative Delight's (tools/cd_namespace_ids.json) or
in this table, and nothing may clash with an id already under the new namespace. Worldgen, Calendar, Devtools, Labels and
Exposure extras are moved by their own changes (their swap lists live in their jars); this file covers the rest.

Writes:
  <owner jar>/src/main/resources/data/<ns>/swaps/owner_namespace.json   swap list per owner (generated; never prune)
  tools/owner_namespace_ids.json                                         the map tools/owner_namespace_move.py reads

--check prints the counts and writes nothing.
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEV = os.path.join(ROOT, "development")
DICTS = os.path.join(DEV, "creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib")
DUMP = os.path.join(ROOT, "kubejs/exported/items_by_mod/creativeengineering.json")
OUT = os.path.join(ROOT, "tools/owner_namespace_ids.json")
OLD = "creativeengineering"
COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
           "blue", "brown", "green", "red", "black"]

# Registered content, saved in worlds: old id -> (new namespace, registry kinds). Kinds are for the record and the checks.
OWNERS = {
    "creativeequipment": {
        "jar": "creativeEquipment",
        "ids": {
            **{f"{OLD}:{c}_stepstool": "item" for c in COLOURS},
            **{f"{OLD}:{t}_shield": "item" for t in ("copper", "gold", "diamond", "netherite", "dragon", "echo", "felsic")},
            **{f"{OLD}:echo_{t}": "item" for t in ("pickaxe", "axe", "shovel", "hoe")},
            f"{OLD}:air_brake": "item",
            f"{OLD}:felsic_scrap": "item",
            f"{OLD}:arrow_fletching": "item",
            f"{OLD}:backpack_skin": "recipe_serializer",
            f"{OLD}:backpack_skin_clear": "recipe_serializer",
            f"{OLD}:elytra_duplication": "recipe_serializer",
            f"{OLD}:copper": "tier",
        },
        "tags": ["stepstools", "backpack_skins", "bonemeal_charms", "elytra_chestplates", "needs_copper_tool",
                 "dragon_gear"],
        "tag_prefixes": ["bottled_air/"],
        "data": ["felsic_ore", "felsic_ore_"],  # felsic ore features and biome modifiers (packs/felsic_gear*)
    },
    "creativemagic": {
        "jar": "creativeMagic",
        "ids": {f"{OLD}:dark_fire": "block", f"{OLD}:flint_and_terrasteel": "item"},
        "tags": [], "tag_prefixes": [], "data": [],
    },
    "creativecompat": {
        "jar": "creativeCompat",
        "ids": {
            f"{OLD}:weathered_cardboard_box": "block+item",
            f"{OLD}:shrapnel": "entity_type",
            f"{OLD}:botanist_pot": "block_entity_type",
        },
        "capabilities": [f"{OLD}:jar_fluid_pipes", f"{OLD}:nightstand_storage"],
        "tags": ["blockrunner_face_up", "camo_speed", "sinking", "vein_undo_denied", "not_in_flower_boxes",
                 "flower_box_plants", "no_world_dye"],
        "tag_prefixes": [],
        "data": ["misc/monster_box_reward"],
    },
    "creativedelight": {
        "jar": "creativeDelight",
        "ids": {"solapplepie:netherite_lunchbox": "item"},
        "tags": [], "tag_prefixes": [], "data": [],
    },
    "creativecommon": {
        "jar": "creativeCommon",
        "ids": {},
        "tags": ["trimmable_armor", "trades/golden_food", "trades/canned_golden"],
        "tag_prefixes": [], "data": [],
    },
    "creativeworldgen": {
        # Only the references outside Worldgen's own tree (dicts, Delight's bird nests); Worldgen's change moves the rest.
        "jar": None,
        "ids": {},
        "tags": ["villager_ladders", "loot/golden_food", "loot/chalk"],
        "tag_prefixes": ["mob_spawns/"],
        "data": [],
    },
}
# Engineering keeps these tags (filled and used by its own recipes); listed so the classification is complete.
ENGINEERING_TAGS = {"railings", "vertical_planks", "unsmoked_planks", "bulb_glass", "carbon_filament",
                    "fan_processing_catalysts/dragon_breathing"}
# Engineering's own items the plan's scan had put elsewhere (layered obsidian: made and used by Engineering's machines).
ENGINEERING_IDS = {"layered_obsidian"}
# Families Engineering registers in a loop (items/dust_piles.js, items/plate_blocks.js).
ENGINEERING_SUFFIXES = ("_dust_pile", "_plate_block")
# Lang keys whose namespace follows the id: item.<ns>.<path>, block.<ns>.<path>, entity.<ns>.<path>.
LANG_KINDS = ("item", "block", "entity")


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def jar_text(jar):
    out = []
    for p in glob.glob(os.path.join(DEV, jar, "**/src/main/**/*.*"), recursive=True):
        if "/build/" in p or p.endswith((".png", ".ogg", ".nbt")):
            continue
        out.append(read(p))
    return "\n".join(out)


def assets(jar, ns_dir):
    """Model and texture paths under a jar's assets/<ns_dir>/ (old or already moved location)."""
    models, textures = set(), set()
    for res in glob.glob(os.path.join(DEV, jar, "**/src/main/resources"), recursive=True):
        base = os.path.join(res, "assets", ns_dir)
        for p in glob.glob(os.path.join(base, "**/*"), recursive=True):
            rel = os.path.relpath(p, base).replace(os.sep, "/")
            if rel.startswith("textures/") and rel.endswith(".png"):
                textures.add(rel[len("textures/"):-4])
            elif rel.startswith("models/") and rel.endswith(".json"):
                models.add(rel[len("models/"):-5])
    return models, textures


def main():
    check_only = "--check" in sys.argv
    errors = []
    dicts = "".join(read(p) for p in glob.glob(os.path.join(DICTS, "*.js")))
    dump = set(json.load(open(DUMP))["items"]) if os.path.exists(DUMP) else set()
    cd = json.load(open(os.path.join(ROOT, "tools/cd_namespace_ids.json")))
    cd_moved = set(cd["ids"])
    eng = jar_text("creativeEngineering")

    claimed = {}
    for ns, o in OWNERS.items():
        text = jar_text(o["jar"]) if o["jar"] else ""
        for old, kind in o["ids"].items():
            path = old.split(":", 1)[1]
            if old in claimed:
                errors.append(f"{old} claimed by {claimed[old]} and {ns}")
            claimed[old] = ns
            if f"'{old}'" not in dicts and f'"{path}"' not in text and f"'{old}'" not in text and f"{path}" not in text:
                errors.append(f"{old}: not in the dicts nor in {o['jar']}'s sources")
            if f"'{ns}:{path}'" in dicts:
                errors.append(f"{ns}:{path} already in the dicts (clash)")

    # Every creativeengineering item in the dump is Engineering's, Delight's earlier move, or claimed here.
    for item in sorted(dump):
        ns, path = item.split(":", 1)
        if ns != OLD or path in cd_moved or item in claimed or path in ENGINEERING_IDS or path.startswith("wooden_") \
                or path.endswith(ENGINEERING_SUFFIXES):
            continue
        key = re.search(r"(\w+)\s*=\s*'" + re.escape(item) + "'", dicts)
        if path in eng or (key and re.search(r"\b[ib]\." + key[1] + r"\b", eng)):
            continue
        errors.append(f"{item}: in the dump, owner unknown")

    ids, tags, prefixes, models, textures, data, caps = {}, {}, {}, {}, {}, {}, {}
    for ns, o in OWNERS.items():
        for old in o["ids"]:
            ids[old] = ns
        for t in o["tags"]:
            if t in ENGINEERING_TAGS:
                errors.append(f"tag {t} listed for {ns} and Engineering")
            tags[t] = ns
        for p in o["tag_prefixes"]:
            prefixes[p] = ns
        for c in o.get("capabilities", []):
            caps[c] = ns
        for d in o["data"]:
            data[d] = ns
        if o["jar"]:
            m, t = assets(o["jar"], OLD)
            for x in m:
                models[x] = ns
            for x in t:
                textures[x] = ns
    # Resource paths are read from the old folders, which the move empties: keep the ones recorded by an earlier run.
    if os.path.exists(OUT):
        prev = json.load(open(OUT))
        for x, ns in prev.get("models", {}).items():
            models.setdefault(x, ns)
        for x, ns in prev.get("textures", {}).items():
            textures.setdefault(x, ns)
    # Creative Delight leftovers of plan_cd_namespace.md: the canned golem dye textures in the pack's kubejs/assets.
    for p in glob.glob(os.path.join(ROOT, "kubejs/assets/*/textures/entity/canned_golem/*.png")):
        textures["entity/canned_golem/" + os.path.basename(p)[:-4]] = "creativedelight"

    eng_textures = set()
    for p in glob.glob(os.path.join(DEV, "creativeEngineering/src/main/resources/assets/creativeengineering/textures/**/*.png"), recursive=True):
        eng_textures.add(os.path.relpath(p).split("/textures/", 1)[1][:-4])
    for x in set(textures) & eng_textures:
        errors.append(f"texture {x} exists in Engineering too; ambiguous")

    print(f"ids {len(ids)}, tags {len(tags)}, tag prefixes {len(prefixes)}, capabilities {len(caps)}, data {len(data)}, "
          f"models {len(models)}, textures {len(textures)}")
    for ns in OWNERS:
        print(f"  {ns}: {sum(1 for v in ids.values() if v == ns)} ids, {sum(1 for v in tags.values() if v == ns)} tags")
    if errors:
        print("\n".join("ERROR " + e for e in errors))
        sys.exit(1)
    if check_only:
        return

    out = {"ids": ids, "tags": tags, "tag_prefixes": prefixes, "capabilities": caps, "data": data,
           "models": models, "textures": textures, "lang_kinds": list(LANG_KINDS)}
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(out, f, indent=1, sort_keys=True)
        f.write("\n")
    for ns, o in OWNERS.items():
        if not o["jar"] or not (o["ids"] or o.get("capabilities")):
            continue
        swap = {"_comment": f"Content moved to {ns} (feature_writeup/namespace_move.md). Generated by "
                            "tools/owner_namespace_ids.py, not by hand. Never prune."}
        swap["replaces"] = {f"{ns}:{old.split(':', 1)[1]}": [old] for old in sorted(o["ids"]) if o["ids"][old] != "tier"}
        if o.get("capabilities"):
            swap["capabilities"] = {f"{ns}:{c.split(':', 1)[1]}": [c] for c in sorted(o["capabilities"])}
        path = os.path.join(DEV, o["jar"], "src/main/resources/data", ns, "swaps/owner_namespace.json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(swap, f, indent=2)
            f.write("\n")
        print("wrote", os.path.relpath(path, ROOT))
    print("wrote", os.path.relpath(OUT, ROOT))


if __name__ == "__main__":
    main()
