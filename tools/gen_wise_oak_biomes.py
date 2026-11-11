#!/usr/bin/env python3
"""Biomes where the Wise Oak generates naturally: those whose tree features include a tree with an oak log trunk.

    python3 tools/gen_wise_oak_biomes.py

Writes kubejs/data/creativeworldgen/tags/worldgen/biome/wise_oak_biomes.json and prints what it included and excluded.
feature_writeup/wise_oak.md 4.5 is the reference. It uses the biome and tree walker of tools/gen_bee_nests.py, which
reads the installed jars and kubejs/data, so modded biomes count. A biome not in the tag gets no Wise Oak, which makes
a newly added biome safe until this runs again. Lightning growth ignores the tag.

ALLOW and DENY are the hand exceptions (overworld biome ids).
"""
import json
import sys
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_bee_nests as bn  # noqa: E402
import gen_crop_worldgen_table as g  # noqa: E402
import gen_wild_flowers as gw  # noqa: E402

OUT = g.INSTANCE / 'kubejs/data/creativeworldgen/tags/worldgen/biome/wise_oak_biomes.json'
ALLOW = set()
# Owner decision 2026-11-09: these list an oak tree only on a technicality (island oaks, river banks, rare surface spots).
DENY = {
    'minecraft:cold_ocean', 'minecraft:deep_cold_ocean', 'minecraft:deep_frozen_ocean', 'minecraft:deep_lukewarm_ocean',
    'minecraft:deep_ocean', 'minecraft:frozen_ocean', 'minecraft:lukewarm_ocean', 'minecraft:ocean', 'minecraft:warm_ocean',
    'minecraft:river', 'minecraft:frozen_river', 'terralith:warm_river',
    'minecraft:dripstone_caves', 'terralith:cave/desert_caves', 'terralith:cave/frostfire_caves', 'terralith:cave/mantle_caves',
}

# bn.JAVA_TREES models these for tree counts only, without a trunk. Same placements, pointed at the real tree.
JAVA_FEATURES = {
    'minecraft:trees_meadow': bn.JAVA_TREES['minecraft:trees_meadow'],
    'minecraft:trees_badlands': {**bn.JAVA_TREES['minecraft:trees_badlands'], 'feature': 'minecraft:oak'},
    'minecraft:trees_snowy': {**bn.JAVA_TREES['minecraft:trees_snowy'], 'feature': 'minecraft:spruce'},
}

# Vanilla configured features registered in Java (VegetationFeatures) that JSON placed features point at. Only the
# oak members matter here; the walker needs a selector to find them.
JAVA_CONFIGURED = {
    'minecraft:trees_savanna': [('minecraft:acacia', 0.8)],
    'minecraft:trees_jungle': [('minecraft:fancy_oak', 0.1), ('minecraft:jungle_bush', 0.5), ('minecraft:mega_jungle_tree', 0.33)],
    'minecraft:trees_water': [('minecraft:fancy_oak', 0.1)],
}
JAVA_DEFAULT = {'minecraft:trees_savanna': 'minecraft:oak', 'minecraft:trees_jungle': 'minecraft:jungle_tree',
                'minecraft:trees_water': 'minecraft:oak'}


def has_oak_trunk(cfg):
    return 'minecraft:oak_log' in json.dumps(cfg.get('trunk_provider'))


class OakWalker(bn.Walker):
    """A feature's oak trees: {'oak': trees per placement}. Bushes do not count, as in the bee-nest walker."""

    def walk(self, v, seen):
        cfg = v.get('config') if isinstance(v, dict) else None
        if isinstance(cfg, dict) and 'trunk_placer' in cfg:
            out = defaultdict(float)
            if has_oak_trunk(cfg) and not bn.tree_nest_chance(cfg)[1]:
                out['oak'] += 1.0
            return out
        return super().walk(v, seen)


def main():
    world = gw.load_world()
    for fid, data in JAVA_FEATURES.items():
        world['placed'].setdefault(fid, ('Java (modelled)', data))
    for fid, picks in JAVA_CONFIGURED.items():
        world['configured'].setdefault(fid, ('Java (modelled)', {'type': 'minecraft:random_selector', 'config': {
            'features': [{'chance': c, 'feature': f} for f, c in picks], 'default': JAVA_DEFAULT[fid]}}))
    for ref in ('minecraft:oak', 'minecraft:fancy_oak', 'minecraft:spruce', 'minecraft:fancy_oak_bees'):
        if ref not in world['configured']:
            print(f'WARNING: {ref} is not a configured feature in any jar; its biomes are judged without it')
    walker = OakWalker(world)
    wt = (world['tags'], world['biome_files'], world['placed'], world['configured'], world['modifiers'],
          world['bdata'], world['cache'])
    features, _ = g.feature_map(wt, lambda f: bool(walker.feature(f)), g.load_wild_crops())

    overworld = set(g.resolve_tag('minecraft:is_overworld', world['tags'], world['cache']))
    # feature_map also returns the pack's wild crop features, so keep only the ones the walker found oaks in.
    oak = {b for f, bm in features.items() if sum(walker.feature(f).values()) > 0 for b in bm}
    included = sorted(((oak & overworld) | (ALLOW & overworld)) - DENY)
    excluded = sorted(overworld - set(included))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps({'replace': False, 'values': [{'id': b, 'required': False} for b in included]}, indent=2) + '\n')

    print(f'wise oak biomes: {len(included)} included, {len(excluded)} excluded')
    print('included:', ', '.join(included))
    print('excluded:', ', '.join(excluded))


if __name__ == '__main__':
    main()
