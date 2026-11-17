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

# After the two bedrock rules, ahead of vanilla's belowTop(5) netherrack rule (index 2) so the band under the roof is
# flesh too. BOP inserts its own rules one later (after that netherrack rule); the Heap goes first because it is flesh
# at every depth (plan_bugfix_1114.md 6.4, owner).
INSERT_AT = 2
FLESH = {'type': 'minecraft:block', 'result_state': {'Name': 'biomesoplenty:flesh'}}


def surface(kind):
	return {
		'type': 'minecraft:condition',
		'if_true': {'type': 'minecraft:stone_depth', 'offset': 0, 'add_surface_depth': True,
					'secondary_depth_range': 0, 'surface_type': kind},
		'then_run': FLESH,
	}


# Floor and ceiling first (the top layer, as BOP), then flesh unconditionally: every default-block (netherrack) position in
# the Heap becomes flesh at any depth, cave walls included. Porous flesh, tendons, eyebulbs and flesh balls are features
# and still place on top. Carvers cut only #minecraft:nether_carver_replaceables, so flesh joins that tag
# (kubejs/data/creativeworldgen/kubejs/tags/visceral_heap.js); it stays out of #minecraft:base_stone_nether, so Nether ores
# do not place inside the Heap.
HEAP_RULE = {
	'type': 'minecraft:condition',
	'if_true': {'type': 'minecraft:biome', 'biome_is': ['biomesoplenty:visceral_heap']},
	'then_run': {'type': 'minecraft:sequence', 'sequence': [surface('ceiling'), surface('floor'), FLESH]},
}


def main():
	settings = json.loads(SOURCE.read_text())
	sequence = settings['surface_rule']['sequence']
	rule = sequence[INSERT_AT]
	if (rule['if_true'].get('type') != 'minecraft:y_above'
			or rule['then_run'].get('result_state', {}).get('Name') != 'minecraft:netherrack'):
		raise SystemExit('vanilla Nether surface rule has an unexpected shape at index %d' % INSERT_AT)
	sequence.insert(INSERT_AT, HEAP_RULE)
	OUT.parent.mkdir(parents=True, exist_ok=True)
	OUT.write_text(json.dumps(settings, indent=2) + '\n')
	print('wrote %s (%d surface rules)' % (OUT.relative_to(ROOT), len(sequence)))


if __name__ == '__main__':
	main()
