#!/usr/bin/env python3
"""Lift the rarest Overworld biomes to a floor share and take the difference from the most common land biomes, by
writing multipliers into kubejs/generation_scripts/worldgen/biome_config.json (feature_writeup/biome_tuning.md 2.1).

    python3 tools/biome_rarity_floor.py [--floor 0.15] [--cave-floor 1.2] [--unsampled 0.3] [--donor-min 1.0] [--dry-run]

Per layer, from each biome's untuned `share_pct`:
- surface: every biome with 0 < share < floor gets multiplier floor / share (USER_BOOSTS keep a larger one).
  Donors: surface biomes with share >= donor-min that are not ocean, river or beach. They all get one multiplier that
  removes the same total share the rare biomes gain.
- surface biomes the samples never hit (share 0; a multiplier has nothing to scale) get `target_pct` --unsampled
  instead (UNSAMPLED_TARGETS overrides it per biome), counted in what the donors pay. The solver then lets their
  entries win the samples they lose by the smallest margins (BiomeLayoutTuner target_pct).
- underground: the same floor with --cave-floor; donors are deep caves and lush caves. Never-sampled cave biomes are
  left alone: /dumpclimatecaves samples them first.
Left alone: the skylands (cut to 0.15 on purpose), yellowstone (boosted on purpose) and deep dark (ancient cities).
The tool owns every other Overworld multiplier: one it doesn't set (no longer rare after a resample) goes back to 1.
Layers are the solver's: a biome with a depth-0 climate entry is surface, the 14 cave biomes underground.

The solve itself runs in game (`/reload`); `result_pct` and the solver's notes then show what was reached.
"""
import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

CONFIG = Path(__file__).resolve().parent.parent / 'kubejs/generation_scripts/worldgen/biome_config.json'
KEEP_EXTRA = {'minecraft:deep_dark', 'terralith:yellowstone'}
# the user's own boosts (2026-09): a floor never lowers them
USER_BOOSTS = {'terralith:yosemite_cliffs', 'terralith:yosemite_lowlands', 'terralith:volcanic_peaks'}
WATER = ('ocean', 'river', 'beach')
CAVE_DONORS = {'terralith:cave/deep_caves', 'minecraft:lush_caves'}
# Never-sampled biomes that only make sense smaller: the crater sits inside volcanic peaks (about 0.25 %), the shield
# clearing inside the shield.
UNSAMPLED_TARGETS = {'terralith:volcanic_crater': 0.15, 'terralith:shield_clearing': 0.15}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--floor', type=float, default=0.15)
    ap.add_argument('--cave-floor', type=float, default=1.2)
    ap.add_argument('--unsampled', type=float, default=0.3)
    ap.add_argument('--donor-min', type=float, default=1.0)
    ap.add_argument('--dry-run', action='store_true')
    args = ap.parse_args()
    doc = json.loads(CONFIG.read_text())
    biomes = doc['dimensions']['minecraft:overworld']['biomes']
    keep = {k for k in biomes if 'skylands' in k} | KEEP_EXTRA
    plan, targets = {}, {}
    unsampled = {k: UNSAMPLED_TARGETS.get(k, args.unsampled) for k, v in biomes.items()
                 if k not in keep and not v.get('share_pct') and v.get('layer') in (None, 'surface')}
    for layer, floor, is_donor in (
            ('surface', args.floor, lambda k, v: v['share_pct'] >= args.donor_min and not any(w in k for w in WATER)),
            ('underground', args.cave_floor, lambda k, v: k in CAVE_DONORS)):
        pool = {k: v for k, v in biomes.items() if v['layer'] == layer and k not in keep}
        rare = {k: v for k, v in pool.items() if 0 < v['share_pct'] < floor}
        donors = {k: v for k, v in pool.items() if is_donor(k, v)}
        need = sum(floor - v['share_pct'] for v in rare.values())
        if layer == 'surface':
            need += sum(unsampled.values())
            targets.update(unsampled)
        cut = round(1 - need / sum(v['share_pct'] for v in donors.values()), 3)
        for k, v in rare.items():
            plan[k] = round(max(v['multiplier'] if k in USER_BOOSTS else 0, floor / v['share_pct']), 2)
        for k in donors:
            plan[k] = cut
        extra = f', {len(unsampled)} never-sampled biomes targeted' if layer == 'surface' and unsampled else ''
        print(f'{layer}: {len(rare)} rare biomes lifted to {floor} %{extra} (+{need:.2f} %), {len(donors)} donors x{cut}')
    for k, v in biomes.items():
        if k not in plan and k not in keep and k not in USER_BOOSTS and v['multiplier'] != 1:
            plan[k] = 1
        if k not in targets and 'target_pct' in v:
            print(f'  {k}: target_pct {v["target_pct"]} dropped')
            v.pop('target_pct')
            v.pop('target_layer', None)
    for k, m in sorted(plan.items()):
        print(f'  {k}: {biomes[k]["multiplier"]} -> {m}')
        biomes[k]['multiplier'] = m
    for k, pct in sorted(targets.items()):
        print(f'  {k}: target_pct {biomes[k].get("target_pct")} -> {pct} (surface)')
        biomes[k]['target_pct'] = pct
        biomes[k]['target_layer'] = 'surface'
    readme = doc.setdefault('_readme', {})
    if 'target_pct' not in readme:
        import gen_biome_config
        readme['target_pct'] = gen_biome_config.README['target_pct']
    if not args.dry_run:
        CONFIG.write_text(json.dumps(doc, indent=2) + '\n')
        print(f'wrote {CONFIG.name}; /reload to solve, then check result_pct and the notes')


if __name__ == '__main__':
    main()
