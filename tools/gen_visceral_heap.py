#!/usr/bin/env python3
"""Surface rule for the Visceral Heap: vanilla's Nether noise settings with the Heap's rule spliced in.

Biomes O' Plenty adds this rule through TerraBlender, which the pack does not run, and vanilla builds its rules before mod
blocks exist, so a mixin could not name flesh. The pack overrides the whole file instead (feature_writeup/visceral_heap.md).

Input:  kubejs/generation_scripts/worldgen/vanilla_nether_noise_settings.json (vanilla 1.19.2, from the data generator reports)
Output: the Creative Worldgen packs/biomesoplenty data pack, loaded only while Biomes O' Plenty is present.

Run: python3 tools/gen_visceral_heap.py    (or /cdevgen run visceral_heap)
Then rebuild Creative Worldgen. New chunks only.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / 'kubejs/generation_scripts/worldgen/vanilla_nether_noise_settings.json'
OUT = ROOT / 'development/creativeWorldgen/src/main/resources/packs/biomesoplenty/data/minecraft/worldgen/noise_settings/nether.json'

# After the two bedrock rules and the belowTop(5) netherrack rule, where BOP inserts its rules.
INSERT_AT = 3
FLESH = {'type': 'minecraft:block', 'result_state': {'Name': 'biomesoplenty:flesh'}}


def surface(kind):
    return {
        'type': 'minecraft:condition',
        'if_true': {'type': 'minecraft:stone_depth', 'offset': 0, 'add_surface_depth': True,
                    'secondary_depth_range': 0, 'surface_type': kind},
        'then_run': FLESH,
    }


HEAP_RULE = {
    'type': 'minecraft:condition',
    'if_true': {'type': 'minecraft:biome', 'biome_is': ['biomesoplenty:visceral_heap']},
    'then_run': {'type': 'minecraft:sequence', 'sequence': [surface('ceiling'), surface('floor')]},
}


def main():
    settings = json.loads(SOURCE.read_text())
    sequence = settings['surface_rule']['sequence']
    if sequence[INSERT_AT]['if_true'].get('biome_is') != ['minecraft:basalt_deltas']:
        raise SystemExit('vanilla Nether surface rule has an unexpected shape at index %d' % INSERT_AT)
    sequence.insert(INSERT_AT, HEAP_RULE)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(settings, indent=2) + '\n')
    print('wrote %s (%d surface rules)' % (OUT.relative_to(ROOT), len(sequence)))


if __name__ == '__main__':
    main()
