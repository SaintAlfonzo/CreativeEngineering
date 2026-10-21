#!/usr/bin/env python3
"""
Builds kubejs/generation_scripts/worldgen/biome_config.json -- the editable source of truth for
the biome frequency tuning framework -- plus one baseline snapshot per tunable dimension under
kubejs/generation_scripts/worldgen/biome_baselines/.

What's tunable: any dimension whose biome_source is an inline `minecraft:multi_noise` list,
because that list IS the biome layout -- every entry is a box in 7D climate space and the game
picks, per quart-block, the entry nearest to the local climate noise. In this pack:
  - minecraft:overworld  -> Terralith's data/minecraft/dimension/overworld.json (~1700 entries)
  - minecraft:the_end    -> Nullscape's data/minecraft/dimension/the_end.json
  - minecraft:the_nether -> vanilla's hardcoded `minecraft:nether` preset (no json exists to
                            scan; VANILLA_NETHER below is read out of MultiNoiseBiomeSource$Preset
                            bytecode in the 1.19.2 mapped jar, 5 entries)
Anything using a `preset` reference, a fixed/checkerboard source, or a flat generator (the
creativeengineering:* sky dimensions) has no layout to tune and is skipped.

Safe to re-run (e.g. after a mod update): per-biome "multiplier"/"replace_with"/"target_pct" edits are kept,
baselines + entry counts are refreshed, new biomes appear with multiplier 1.0, biomes that
vanished are dropped (with a warning if you had edited them).

Run: python3 tools/gen_biome_config.py
Then edit biome_config.json; kubejs/server_scripts/worldgen/biome_overrides.js (patcherMod's
BiomeLayoutTuner) solves and applies it at every world load, so the next NEW world gets it.
"""
import json
import zipfile
from collections import OrderedDict
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
MODS_DIR = INSTANCE / 'mods'
WORLDGEN_DIR = INSTANCE / 'kubejs/generation_scripts/worldgen'
OUT_PATH = WORLDGEN_DIR / 'biome_config.json'
BASELINE_DIR = WORLDGEN_DIR / 'biome_baselines'


def _p(t=0.0, h=0.0, c=0.0, e=0.0, d=0.0, w=0.0, o=0.0):
    return {'temperature': t, 'humidity': h, 'continentalness': c, 'erosion': e,
            'depth': d, 'weirdness': w, 'offset': o}


# MultiNoiseBiomeSource.Preset.NETHER, 1.19.2 (Climate.parameters(t, h, c, e, d, w, offset)).
VANILLA_NETHER = {
    'type': 'minecraft:the_nether',
    'generator': {
        'type': 'minecraft:noise',
        'settings': 'minecraft:nether',
        'biome_source': {
            'type': 'minecraft:multi_noise',
            'biomes': [
                {'biome': 'minecraft:nether_wastes', 'parameters': _p()},
                {'biome': 'minecraft:soul_sand_valley', 'parameters': _p(h=-0.5)},
                {'biome': 'minecraft:crimson_forest', 'parameters': _p(t=0.4)},
                {'biome': 'minecraft:warped_forest', 'parameters': _p(h=0.5, o=0.375)},
                {'biome': 'minecraft:basalt_deltas', 'parameters': _p(t=-0.5, o=0.175)},
            ],
        },
    },
}

README = OrderedDict([
    ('workflow', 'Set a biome\'s "multiplier" (or "replace_with"), then /reload or rejoin to see the solve '
                 '(result_pct / note below update), and create a NEW world to use it. '
                 'kubejs/server_scripts/worldgen/biome_overrides.js solves the layout at every world load. '
                 'Multipliers other than 0/1 need a climate sample from /dumpclimate first (see README.md).'),
    ('multiplier', '1.0 = untouched. 0 = removed (its climate space goes to whichever biomes border it). '
                   '0.5 = half as common, 2.0 = twice as common, measured against share_pct. The solver '
                   'hits these by raising/lowering one offset penalty per biome, so a boosted biome grows '
                   'outward from its existing patches into its climate neighbours, never appears somewhere '
                   'climatically unrelated.'),
    ('replace_with', 'A biome id, or null. Relabels every entry of this biome to that one -- an exact swap of '
                     'the same climate space. Overrides multiplier.'),
    ('target_pct', 'Optional: an absolute share goal (% of the biome\'s layer) instead of a multiplier; overrides it. '
                   'Works for a biome the samples never hit: its entries win the samples they lose by the smallest '
                   'margins. tools/biome_rarity_floor.py writes these. "target_layer" (optional) names the layer for '
                   'a never-hit biome (default surface).'),
    ('read_only', '"entries" (how many climate boxes the biome owns), "layer" (surface or underground -- which '
                  'sample layer its share is measured on), "share_pct" (original % of that layer), "result_pct" '
                  '(% after the last solve), "note" (solver warnings, e.g. target clamped).'),
    ('caveat', 'Every world stores its biome layout in level.dat at creation time. Changes only affect worlds '
               'created AFTER the override exists; existing saves keep the layout they were made with.'),
])


def scan_mod_dimensions():
    """-> {dim_id: [(jar_name, json_dict), ...]} for every inline multi_noise dimension json."""
    found = {}
    for jar in sorted(MODS_DIR.glob('*.jar')):
        try:
            zf = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        with zf:
            for name in zf.namelist():
                parts = name.split('/')
                if len(parts) != 4 or parts[0] != 'data' or parts[2] != 'dimension' or not name.endswith('.json'):
                    continue
                try:
                    doc = json.loads(zf.read(name))
                except (json.JSONDecodeError, UnicodeDecodeError):
                    print(f'  ! {jar.name}: {name} is not valid json, skipped')
                    continue
                src = doc.get('generator', {}).get('biome_source', {})
                if src.get('type') != 'minecraft:multi_noise' or 'biomes' not in src:
                    continue
                dim_id = f'{parts[1]}:{parts[3][:-5]}'
                found.setdefault(dim_id, []).append((jar.name, doc))
    return found


def baseline_rel(dim_id):
    ns, path = dim_id.split(':', 1)
    return f'biome_baselines/{ns}__{path.replace("/", "__")}.json'


def main():
    sources = scan_mod_dimensions()
    if 'minecraft:the_nether' not in sources:
        sources['minecraft:the_nether'] = [('vanilla_preset:minecraft:nether', VANILLA_NETHER)]

    old = {}
    if OUT_PATH.exists():
        old = json.loads(OUT_PATH.read_text()).get('dimensions', {})

    BASELINE_DIR.mkdir(parents=True, exist_ok=True)
    dims = OrderedDict()
    for dim_id in sorted(sources):
        cands = sources[dim_id]
        jar_name, doc = cands[-1]
        if len(cands) > 1:
            print(f'  ! {dim_id} is defined by several jars ({", ".join(c[0] for c in cands)}); '
                  f'using {jar_name} -- check which one actually wins in-game (/dumpclimate model check)')

        rel = baseline_rel(dim_id)
        (WORLDGEN_DIR / rel).write_text(json.dumps(doc, separators=(',', ':')))

        counts = {}
        for e in doc['generator']['biome_source']['biomes']:
            counts[e['biome']] = counts.get(e['biome'], 0) + 1

        prev = old.get(dim_id, {})
        prev_biomes = prev.get('biomes', {})
        biomes = OrderedDict()
        for b in sorted(counts):
            p = prev_biomes.get(b, {})
            biomes[b] = OrderedDict([
                ('multiplier', p.get('multiplier', 1.0)),
                ('replace_with', p.get('replace_with')),
            ])
            for k in ('target_pct', 'target_layer'):
                if p.get(k) is not None:
                    biomes[b][k] = p[k]
            biomes[b].update([
                ('entries', counts[b]),
                ('layer', p.get('layer')),
                ('share_pct', p.get('share_pct')),
                ('result_pct', p.get('result_pct')),
            ])
            if p.get('note'):
                biomes[b]['note'] = p['note']
        for b, p in prev_biomes.items():
            if b not in counts and (p.get('multiplier', 1.0) != 1.0 or p.get('replace_with') or p.get('target_pct') is not None):
                print(f'  ! {dim_id}: {b} no longer exists in the source layout -- your edit to it was dropped')

        dims[dim_id] = OrderedDict([
            ('source', jar_name),
            ('baseline', rel),
            ('biome_count', len(counts)),
            ('entry_count', sum(counts.values())),
            ('samples', prev.get('samples')),
            ('model_agreement_pct', prev.get('model_agreement_pct')),
            ('biomes', biomes),
        ])
        print(f'  {dim_id}: {len(counts)} biomes / {sum(counts.values())} entries  <- {jar_name}')

    for f in BASELINE_DIR.glob('*.json'):
        if f'biome_baselines/{f.name}' not in {d['baseline'] for d in dims.values()}:
            f.unlink()

    OUT_PATH.write_text(json.dumps(OrderedDict([('_readme', README), ('dimensions', dims)]), indent=2) + '\n')
    print(f'wrote {OUT_PATH.relative_to(INSTANCE)}')


if __name__ == '__main__':
    main()
