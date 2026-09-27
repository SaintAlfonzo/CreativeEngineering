#!/usr/bin/env python3
"""
Builds kubejs/generation_scripts/worldgen/structure_config.json — the prepopulated,
editable source of truth for the structure spawn-frequency/removal framework.

Sources, per structure_set:
  - Modded:  scanned directly out of mods/*.jar (data/<ns>/worldgen/structure_set/*.json +
             data/<ns>/worldgen/structure/*.json for biome info). Fully accurate, and takes
             priority over the vanilla table below when a mod ships its own override of a
             vanilla id (e.g. this pack's Terralith reduces minecraft:villages spacing itself).
             kubejs/data/<ns>/worldgen/... is scanned too, for structures shipped as plain
             datapack data (source "kubejs/data"); a jar's set always wins over it.
  - Modded, registered in Java (AE2 meteorites): CODE_REGISTERED_SETS below, bytecode-checked,
             included only while the mod's jar is in mods/.
  - Vanilla: vanilla bootstraps structure_set/structure definitions in Java, not loose jsons,
             so there is nothing to scan. VANILLA_STRUCTURE_SETS below is a hand-maintained
             table of the 1.19.2 defaults, checked against the bytecode of
             net.minecraft.data.worldgen.StructureSets (javap on the mapped forge jar).
             Biome membership for vanilla structures IS data-driven (as convenience tags) and
             is read straight from data/minecraft/tags/worldgen/biome/has_structure/*.json
             inside the vanilla jar, so that part is exact.

Safe to re-run (e.g. after a mod update): per-structure "enabled" flags are preserved, and
"current" is rebuilt as the new "original" plus every field you had changed (a 3-way merge), so
a corrected or updated baseline flows through to untouched fields without losing your edits.

Every placement field is kept, not just the tunable ones: exclusion_zone, spread_type,
preferred_biomes and mod-specific fields (MES min_distance_from_world_origin, YUNG's
enhanced_exclusion_zone / ring fields) all have to survive into an override, or the override
silently changes behaviour (or fails to load, for required fields).

Run: python3 tools/gen_structure_config.py
Then edit kubejs/generation_scripts/worldgen/structure_config.json and rejoin the world:
kubejs/server_scripts/worldgen/structure_overrides.js applies it at every world load.
"""
import glob
import json
import re
import zipfile
from pathlib import Path
from collections import OrderedDict

INSTANCE = Path(__file__).resolve().parent.parent
MODS_DIR = INSTANCE / 'mods'
KUBEJS_DATA_DIR = INSTANCE / 'kubejs/data'
OUT_PATH = INSTANCE / 'kubejs/generation_scripts/worldgen/structure_config.json'

# The vanilla 1.19.2 jar that actually contains loose data/ jsons (Forge's dev-time split puts
# resources in the "-extra" jar; the plain client/server jars here are launcher stubs). Found by
# globbing the AT Launcher shared library cache; adjust if this instance's libraries live
# elsewhere. Only used for the has_structure/* biome tags -- see load_vanilla_biome_tags().
_vanilla_jar_candidates = glob.glob(
    str(Path.home() / '.local/share/atlauncher/libraries/net/minecraft/client/1.19.2*/*-extra.jar')
)
VANILLA_JAR = Path(_vanilla_jar_candidates[0]) if _vanilla_jar_candidates else None

# ---------------------------------------------------------------------------
# Vanilla 1.19.2 structure_set defaults, javap-verified against StructureSets.bootstrap
# (forge-1.19.2-43.3.9_mapped_official_1.19.2.jar) on 2026-09-25. Every field matters here:
# an override replaces the whole set, so a missing exclusion_zone or frequency is a real change.
# ---------------------------------------------------------------------------
VANILLA_STRUCTURE_SETS = {
    'minecraft:villages': {
        'type': 'minecraft:random_spread', 'salt': 10387312, 'spacing': 34, 'separation': 8,
        'structures': [
            {'structure': 'minecraft:village_plains', 'weight': 1},
            {'structure': 'minecraft:village_desert', 'weight': 1},
            {'structure': 'minecraft:village_savanna', 'weight': 1},
            {'structure': 'minecraft:village_snowy', 'weight': 1},
            {'structure': 'minecraft:village_taiga', 'weight': 1},
        ],
    },
    'minecraft:desert_pyramids': {
        'type': 'minecraft:random_spread', 'salt': 14357617, 'spacing': 32, 'separation': 8,
        'structures': [{'structure': 'minecraft:desert_pyramid', 'weight': 1}],
    },
    'minecraft:igloos': {
        'type': 'minecraft:random_spread', 'salt': 14357618, 'spacing': 32, 'separation': 8,
        'structures': [{'structure': 'minecraft:igloo', 'weight': 1}],
    },
    'minecraft:jungle_temples': {
        'type': 'minecraft:random_spread', 'salt': 14357619, 'spacing': 32, 'separation': 8,
        'structures': [{'structure': 'minecraft:jungle_pyramid', 'weight': 1}],
    },
    'minecraft:swamp_huts': {
        'type': 'minecraft:random_spread', 'salt': 14357620, 'spacing': 32, 'separation': 8,
        'structures': [{'structure': 'minecraft:swamp_hut', 'weight': 1}],
    },
    'minecraft:pillager_outposts': {
        'type': 'minecraft:random_spread', 'salt': 165745296, 'spacing': 32, 'separation': 8,
        'frequency': 0.2, 'frequency_reduction_method': 'legacy_type_1',
        'exclusion_zone': {'other_set': 'minecraft:villages', 'chunk_count': 10},
        'structures': [{'structure': 'minecraft:pillager_outpost', 'weight': 1}],
    },
    'minecraft:ocean_monuments': {
        'type': 'minecraft:random_spread', 'salt': 10387313, 'spacing': 32, 'separation': 5,
        'spread_type': 'triangular',
        'structures': [{'structure': 'minecraft:monument', 'weight': 1}],
    },
    'minecraft:woodland_mansions': {
        'type': 'minecraft:random_spread', 'salt': 10387319, 'spacing': 80, 'separation': 20,
        'spread_type': 'triangular',
        'structures': [{'structure': 'minecraft:mansion', 'weight': 1}],
    },
    'minecraft:ocean_ruins': {
        'type': 'minecraft:random_spread', 'salt': 14357621, 'spacing': 20, 'separation': 8,
        'structures': [
            {'structure': 'minecraft:ocean_ruin_cold', 'weight': 1},
            {'structure': 'minecraft:ocean_ruin_warm', 'weight': 1},
        ],
    },
    'minecraft:shipwrecks': {
        'type': 'minecraft:random_spread', 'salt': 165745295, 'spacing': 24, 'separation': 4,
        'structures': [
            {'structure': 'minecraft:shipwreck', 'weight': 1},
            {'structure': 'minecraft:shipwreck_beached', 'weight': 1},
        ],
    },
    'minecraft:ruined_portals': {
        'type': 'minecraft:random_spread', 'salt': 34222645, 'spacing': 40, 'separation': 15,
        'structures': [
            {'structure': 'minecraft:ruined_portal', 'weight': 1},
            {'structure': 'minecraft:ruined_portal_desert', 'weight': 1},
            {'structure': 'minecraft:ruined_portal_jungle', 'weight': 1},
            {'structure': 'minecraft:ruined_portal_swamp', 'weight': 1},
            {'structure': 'minecraft:ruined_portal_mountain', 'weight': 1},
            {'structure': 'minecraft:ruined_portal_ocean', 'weight': 1},
            # One set for all 7 in 1.19.2 (BuiltinStructureSets has no separate nether set).
            {'structure': 'minecraft:ruined_portal_nether', 'weight': 1},
        ],
    },
    'minecraft:mineshafts': {
        'type': 'minecraft:random_spread', 'salt': 0, 'spacing': 1, 'separation': 0,
        'frequency': 0.004, 'frequency_reduction_method': 'legacy_type_3',
        'structures': [
            {'structure': 'minecraft:mineshaft', 'weight': 1},
            {'structure': 'minecraft:mineshaft_mesa', 'weight': 1},
        ],
    },
    'minecraft:buried_treasures': {
        'type': 'minecraft:random_spread', 'salt': 0, 'spacing': 1, 'separation': 0,
        'frequency': 0.01, 'frequency_reduction_method': 'legacy_type_2', 'locate_offset': [9, 0, 9],
        'structures': [{'structure': 'minecraft:buried_treasure', 'weight': 1}],
    },
    'minecraft:strongholds': {
        'type': 'minecraft:concentric_rings', 'salt': 0,
        'distance': 32, 'spread': 3, 'count': 128,
        # Required by ConcentricRingsStructurePlacement's codec (fieldOf, not optionalFieldOf).
        'preferred_biomes': '#minecraft:stronghold_biased_to',
        'structures': [{'structure': 'minecraft:stronghold', 'weight': 1}],
    },
    'minecraft:ancient_cities': {
        'type': 'minecraft:random_spread', 'salt': 20083232, 'spacing': 24, 'separation': 8,
        'structures': [{'structure': 'minecraft:ancient_city', 'weight': 1}],
    },
    'minecraft:nether_complexes': {
        'type': 'minecraft:random_spread', 'salt': 30084232, 'spacing': 27, 'separation': 4,
        'structures': [
            {'structure': 'minecraft:fortress', 'weight': 2},
            {'structure': 'minecraft:bastion_remnant', 'weight': 3},
        ],
    },
    'minecraft:nether_fossils': {
        'type': 'minecraft:random_spread', 'salt': 14357921, 'spacing': 2, 'separation': 1,
        'structures': [{'structure': 'minecraft:nether_fossil', 'weight': 1}],
    },
    'minecraft:end_cities': {
        'type': 'minecraft:random_spread', 'salt': 10387313, 'spacing': 20, 'separation': 11,
        'spread_type': 'triangular',
        'structures': [{'structure': 'minecraft:end_city', 'weight': 1}],
    },
}

# Modded structure_sets registered in Java instead of shipped as json, so no jar scan finds them.
# Same format as the vanilla table, plus the class that proves the mod is installed ('marker', the
# set is skipped without it: an override naming an unregistered structure breaks world creation) and
# the structure's biomes (read-only info). Find new ones with tools/check_structure_live.py (section 1).
CODE_REGISTERED_SETS = {
    # appeng.init.worldgen.InitStructures (AE2 12.9.12): new RandomSpreadStructurePlacement(32, 8,
    # LINEAR, 124895654), only registered while config/ae2/common.json generateMeteorites = true.
    'ae2:meteorite': {
        'marker': 'appeng/init/worldgen/InitStructures.class', 'biomes': '#ae2:has_meteorites',
        'type': 'minecraft:random_spread', 'salt': 124895654, 'spacing': 32, 'separation': 8,
        'structures': [{'structure': 'ae2:meteorite', 'weight': 1}],
    },
}

# Structures another mod stops in code, so tuning their set does nothing while the block is on.
# Written into each structure entry as a read-only "blocked" note (and shown in the report).
# (structure, jar glob, config file under config/, key, value that blocks it or None = always, who)
# All javap-checked 2026-09-27: YUNG's Disable*Mixin cancel ChunkGenerator.tryGenerateStructure by
# StructureType; SmallNetherDungeonStructure.findGenerationPoint returns empty when disabled.
CODE_BLOCKS = [
    ('minecraft:desert_pyramid', 'YungsBetterDesertTemples-*.jar', 'betterdeserttemples-forge-1_19.toml',
     'Disable Vanilla Pyramids', 'true', "YUNG's Better Desert Temples"),
    ('minecraft:jungle_pyramid', 'YungsBetterJungleTemples-*.jar', 'betterjungletemples-forge-1_19_2.toml',
     'Disable Vanilla Jungle Temples', 'true', "YUNG's Better Jungle Temples"),
    ('minecraft:swamp_hut', 'YungsBetterWitchHuts-*.jar', 'betterwitchhuts-forge-1_19.toml',
     'Disable Vanilla Witch Huts', 'true', "YUNG's Better Witch Huts"),
    ('minecraft:fortress', 'YungsBetterNetherFortresses-*.jar', 'betterfortresses-forge-1_19.toml',
     'Disable Vanilla Nether Fortresses', 'true', "YUNG's Better Nether Fortresses"),
    ('minecraft:stronghold', 'YungsBetterStrongholds-*.jar', None, None, None, "YUNG's Better Strongholds"),
    ('betterdungeons:small_nether_dungeon', 'YungsBetterDungeons-*.jar', 'betterdungeons-forge-1_19.toml',
     'Enable Small Nether Dungeons', 'false', "YUNG's Better Dungeons"),
]


def load_code_blocks():
    """structure id -> why it can't generate right now (from CODE_BLOCKS and the current configs)."""
    out = {}
    for sid, jar_glob, cfg, key, blocking, who in CODE_BLOCKS:
        if not list(MODS_DIR.glob(jar_glob)):
            continue
        if cfg is None:
            out[sid] = who + ' always stops it (no config option). Tuning this set has no effect.'
            continue
        path = INSTANCE / 'config' / cfg
        m = re.search(r'"%s"\s*=\s*(\w+)' % re.escape(key), path.read_text()) if path.exists() else None
        if not m:
            print(f'  WARNING: {cfg} "{key}" not found; not marking {sid} as blocked')
        elif m.group(1) == blocking:
            out[sid] = f'{who} stops it in code (config/{cfg}: "{key}" = {blocking}). Tuning this set has no effect.'
    return out


# structure id -> the has_structure/<name> tag file (only where the tag name differs from the
# structure's own path — most match automatically below).
VANILLA_TAG_OVERRIDES = {
    'minecraft:bastion_remnant': 'bastion_remnant',
    'minecraft:fortress': 'nether_fortress',
    'minecraft:ruined_portal': 'ruined_portal_standard',
    'minecraft:jungle_pyramid': 'jungle_temple',
    'minecraft:monument': 'ocean_monument',
    'minecraft:mansion': 'woodland_mansion',
}


def load_vanilla_biome_tags():
    """namespace:path (structure id) -> sorted [biome ids], from the vanilla jar's
    data/minecraft/tags/worldgen/biome/has_structure/*.json convenience tags."""
    if not VANILLA_JAR:
        print('WARNING: vanilla -extra.jar not found; vanilla structures will have no biome info')
        return {}
    z = zipfile.ZipFile(VANILLA_JAR)
    out = {}
    for name in z.namelist():
        m = re.match(r'^data/minecraft/tags/worldgen/biome/has_structure/(.+)\.json$', name)
        if not m:
            continue
        tag_name = m.group(1)
        data = loads_lenient(z.read(name))
        biomes = [v for v in data.get('values', []) if isinstance(v, str) and not v.startswith('#')]
        out[tag_name] = sorted(biomes)
    return out


def loads_lenient(raw):
    """json.loads that also accepts // and /* */ comments, like Minecraft's lenient GSON reader.
    Some mods (e.g. Beautify) ship commented worldgen jsons that plain json.loads rejects."""
    text = raw.decode('utf-8') if isinstance(raw, bytes) else raw
    out, i, n, in_str = [], 0, len(text), False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == '\\' and i + 1 < n:
                out.append(text[i + 1])
                i += 1
            elif c == '"':
                in_str = False
        elif c == '"':
            in_str = True
            out.append(c)
        elif text.startswith('//', i):
            while i < n and text[i] != '\n':
                i += 1
            continue
        elif text.startswith('/*', i):
            end = text.find('*/', i + 2)
            i = n if end < 0 else end + 2
            continue
        else:
            out.append(c)
        i += 1
    return json.loads(''.join(out))


def scan_mod_jars():
    """-> (structure_sets: {id: raw json}, structure_biomes: {id: raw 'biomes' value}, source: {id: jar name})"""
    structure_sets = {}
    structure_biomes = {}
    source = {}
    markers = {cs['marker']: sid for sid, cs in CODE_REGISTERED_SETS.items()}
    for jar in sorted(MODS_DIR.glob('*.jar')):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        names = z.namelist()
        for name in names:
            if name in markers and markers[name] not in structure_sets:
                sid = markers[name]
                cs = CODE_REGISTERED_SETS[sid]
                structure_sets[sid] = {'placement': {k: v for k, v in cs.items() if k not in ('marker', 'biomes', 'structures')},
                                       'structures': cs['structures']}
                source[sid] = jar.name + ' (registered in code)'
                for entry in cs['structures']:
                    structure_biomes[entry['structure']] = cs['biomes']
                continue
            m = re.match(r'^data/([^/]+)/worldgen/structure_set/(.+)\.json$', name)
            if m:
                sid = f'{m.group(1)}:{m.group(2)}'
                try:
                    structure_sets[sid] = loads_lenient(z.read(name))
                    source[sid] = jar.name
                except (json.JSONDecodeError, UnicodeDecodeError):
                    pass
                continue
            m = re.match(r'^data/([^/]+)/worldgen/structure/(.+)\.json$', name)
            if m:
                sid = f'{m.group(1)}:{m.group(2)}'
                try:
                    data = loads_lenient(z.read(name))
                    structure_biomes[sid] = data.get('biomes')
                except (json.JSONDecodeError, UnicodeDecodeError):
                    pass
    # Structures shipped as plain datapack data in kubejs/data (e.g. MCreator house mods whose
    # jars can't load on Forge 43.3.9). Only ids no jar provides, so a kubejs/data override of a
    # mod's set can never become that set's baseline.
    for path in sorted(KUBEJS_DATA_DIR.glob('*/worldgen/*/**/*.json')):
        rel = path.relative_to(KUBEJS_DATA_DIR).as_posix()
        m = re.match(r'^([^/]+)/worldgen/(structure_set|structure)/(.+)\.json$', rel)
        if not m:
            continue
        sid = f'{m.group(1)}:{m.group(3)}'
        try:
            data = loads_lenient(path.read_bytes())
        except (json.JSONDecodeError, UnicodeDecodeError):
            continue
        if m.group(2) == 'structure_set':
            if sid not in structure_sets:
                structure_sets[sid] = data
                source[sid] = 'kubejs/data'
        elif sid not in structure_biomes:
            structure_biomes[sid] = data.get('biomes')
    return structure_sets, structure_biomes, source


def placement_block(raw_placement):
    """Extract just the editable/informational placement fields we care about."""
    out = OrderedDict()
    out['type'] = raw_placement.get('type', 'minecraft:random_spread')
    if 'spacing' in raw_placement:
        out['spacing'] = raw_placement['spacing']
    if 'separation' in raw_placement:
        out['separation'] = raw_placement['separation']
    if 'distance' in raw_placement:
        out['distance'] = raw_placement['distance']
    if 'spread' in raw_placement:
        out['spread'] = raw_placement['spread']
    if 'count' in raw_placement:
        out['count'] = raw_placement['count']
    out['salt'] = raw_placement.get('salt', 0)
    out['frequency'] = raw_placement.get('frequency', 1.0)
    if 'frequency_reduction_method' in raw_placement:
        out['frequency_reduction_method'] = raw_placement['frequency_reduction_method']
    out['locate_offset'] = raw_placement.get('locate_offset', [0, 0, 0])
    # Everything else (exclusion_zone, spread_type, preferred_biomes, mod-specific fields) is
    # carried verbatim so an override can reproduce the set exactly.
    for key, value in raw_placement.items():
        if key not in out and key != 'structures':
            out[key] = value
    return out


def merge_current(prior, new_original):
    """3-way merge: the new baseline plus every field the user changed relative to the OLD
    baseline. Untouched fields follow corrections/mod updates; edited ones stay as edited."""
    old_original = prior.get('original', {})
    edits = {k: v for k, v in prior['current'].items() if old_original.get(k) != v}
    merged = OrderedDict(new_original)
    merged.update(edits)
    return merged, sorted(edits)


def build_readme():
    return OrderedDict([
        ('workflow', (
            'Edit values under "current" per structure_set, or flip a structure entry\'s '
            '"enabled" to false, then rejoin the world: kubejs/server_scripts/worldgen/'
            'structure_overrides.js applies this file at every world load (new chunks only; '
            'structure_tag_report.md shows what it did). For category-wide changes ("all villages x0.5") '
            'use multipliers in structure_tags.json instead; they apply on top of "current". '
            'Re-running tools/gen_structure_config.py is always safe -- it keeps "enabled" and '
            'every field you changed in "current".'
        )),
        ('top_level_fields', OrderedDict([
            ('source', 'Where this structure_set\'s definition came from: a mod jar filename, or '
                '"vanilla_known_defaults" (see caveat below).'),
            ('structures', 'The structure(s) this set can place. Each has "structure" (its id), '
                '"weight", "enabled" (your on/off switch), and "biomes" (read-only reference).'),
            ('original', 'The values this structure_set actually has without any override -- the '
                'diff baseline, refreshed on every scan. Don\'t edit this; structure_overrides.js '
                'compares "current" against it to decide what to write.'),
            ('current', 'Your edits go here. Identical to "original" until you change something.'),
        ])),
        ('placement_dials (inside original/current)', OrderedDict([
            ('type', 'Which placement algorithm: "minecraft:random_spread" (almost everything -- '
                'an evenly-spaced grid, one random attempt per cell) or '
                '"minecraft:concentric_rings" (strongholds only -- rings expanding out from '
                'world spawn, not a grid).'),
            ('spacing', '[random_spread] Average chunks between grid cells, i.e. between attempts '
                'at this structure. Bigger spacing = rarer structure. This is usually the dial '
                'you want for "make X less/more common".'),
            ('separation', '[random_spread] Minimum chunks kept clear around each grid cell so two '
                'attempts can never land right next to each other. Must stay smaller than '
                '"spacing" (vanilla convention: separation < spacing, often spacing/4).'),
            ('spread_type', '[random_spread, optional] Where within its grid cell an attempt can '
                'land: "linear" (default, anywhere in the cell) or "triangular" (biased toward '
                'the cell center -- vanilla only uses this for ocean monuments). Rarely worth '
                'touching.'),
            ('distance', '[concentric_rings] Chunks between each successive ring.'),
            ('spread', '[concentric_rings] Random jitter added within a ring so structures aren\'t '
                'on a perfect circle.'),
            ('count', '[concentric_rings] Total number of attempts across every ring in the whole '
                'set (128 for vanilla strongholds). Lower = fewer strongholds total, still spread '
                'across the same ring layout.'),
            ('frequency', 'A flat 0.0-1.0 chance multiplier applied on top of spacing/separation '
                'or the ring layout -- "of the grid cells that pass everything else, only spawn '
                'here X% of the time." Vanilla uses this to keep mineshafts/buried treasure rare '
                'despite a 1-chunk grid. Setting it to 0 disables the structure outright -- this '
                'is exactly what structure_overrides.js does automatically when you disable '
                'every structure in a set. Omitted/1.0 means "no extra reduction".'),
            ('frequency_reduction_method', 'Which RNG algorithm applies the "frequency" chance '
                '("legacy_type_1" / "legacy_type_2" / "default"). Purely about matching vanilla\'s '
                'exact RNG behavior bit-for-bit -- doesn\'t change how rare something ends up '
                'feeling. Safe to leave alone.'),
            ('salt', 'Seeds this structure_set\'s own slice of the world seed\'s RNG, so different '
                'structure_sets don\'t land in lockstep with each other. Only affects WHERE on '
                'the grid attempts fall, not how often. Safe to leave alone -- a wrong value just '
                'shifts the grid\'s phase relative to a from-scratch vanilla world.'),
            ('locate_offset', 'The "xyz coords" dial: an integer [x, y, z] nudge applied to every '
                'placement attempt before any other check. x/z shift the whole grid sideways; y '
                'is essentially unused (the structure\'s own logic picks its actual height). '
                'Default [0, 0, 0].'),
        ])),
        ('per_structure_fields (inside a structures[] entry)', OrderedDict([
            ('weight', 'When a set holds multiple structures sharing one grid (e.g. the 5 village '
                'variants in minecraft:villages), weight influences which ONE wins a given grid '
                'cell before that structure\'s own biome check narrows it further. Only meaningful '
                'relative to the other entries in the same set.'),
            ('enabled', 'Your removal switch. false on the only structure in a set forces that '
                'set\'s frequency to 0 (always schema-safe). false on one of several removes just '
                'that entry from the set\'s structure list, leaving the others untouched.'),
            ('biomes', 'Read-only reference -- which biomes this structure can spawn in, from the '
                'mod\'s own structure json or (vanilla) minecraft\'s has_structure/* tags. '
                'Changing WHERE something spawns needs editing the structure\'s own definition, '
                'which this framework deliberately never touches -- see the README.'),
            ('blocked', 'Read-only, only present when another mod stops this structure in code (YUNG\'s '
                '"Disable Vanilla X" options, Better Dungeons\' small nether dungeon toggle). Its set can '
                'still be tuned, but nothing generates while the block is on. Written by the scanner from '
                'the current mod configs; re-run it after changing one of those options.'),
        ])),
        ('caveats', (
            'Vanilla "source":"vanilla_known_defaults" entries come from a table in the scanner '
            '(vanilla bootstraps them in Java, unlike every mod here), javap-checked against the '
            '1.19.2 StructureSets bytecode. /dumpstructuregen in-world shows the live values. Also: this pack\'s minecraft:villages and '
            'minecraft:end_cities are NOT vanilla -- Terralith and Nullscape already override '
            'them, and the scanner picked that up (check "source" per entry).'
        )),
    ])


def build():
    vanilla_tags = load_vanilla_biome_tags()
    mod_sets, mod_structure_biomes, mod_source = scan_mod_jars()
    blocked = load_code_blocks()

    existing = {}
    if OUT_PATH.exists():
        existing = json.loads(OUT_PATH.read_text()).get('structure_sets', {})

    out_sets = OrderedDict()

    def biome_for(structure_id):
        if structure_id in mod_structure_biomes:
            return mod_structure_biomes[structure_id]
        ns, _, struct_path = structure_id.partition(':')
        if ns == 'minecraft':
            tag_name = VANILLA_TAG_OVERRIDES.get(structure_id, struct_path)
            if tag_name in vanilla_tags:
                return vanilla_tags[tag_name]
        return None

    all_ids = sorted(set(VANILLA_STRUCTURE_SETS) | set(mod_sets))
    for sid in all_ids:
        if sid in mod_sets:
            raw = mod_sets[sid]
            original_placement = placement_block(raw.get('placement', {}))
            raw_structures = raw.get('structures', [])
            src = mod_source.get(sid, 'unknown.jar')
        else:
            vd = VANILLA_STRUCTURE_SETS[sid]
            original_placement = placement_block(vd)
            raw_structures = vd['structures']
            src = 'vanilla_known_defaults'

        structures = []
        for entry in raw_structures:
            struct_id = entry.get('structure')
            structures.append(OrderedDict([
                ('structure', struct_id),
                ('weight', entry.get('weight', 1)),
                ('enabled', True),
                ('biomes', biome_for(struct_id)),
            ]))
            if struct_id in blocked:
                structures[-1]['blocked'] = blocked[struct_id]

        prior = existing.get(sid)
        if prior and 'current' in prior:
            current_placement, edited = merge_current(prior, original_placement)
            if edited:
                print(f'  kept your edits to {sid}: {", ".join(edited)}')
            # carry forward per-structure enabled flags the user already set
            prior_enabled = {s['structure']: s.get('enabled', True) for s in prior.get('structures', [])}
            for s in structures:
                if s['structure'] in prior_enabled:
                    s['enabled'] = prior_enabled[s['structure']]
        else:
            current_placement = OrderedDict(original_placement)

        out_sets[sid] = OrderedDict([
            ('source', src),
            ('structures', structures),
            ('original', original_placement),
            ('current', current_placement),
        ])

    # sets whose mod is gone (jar removed/renamed): dropped, since an override naming an
    # unregistered structure breaks world creation ("Unbound values in registry")
    for sid in sorted(set(existing) - set(out_sets)):
        print(f'  dropped {sid} (was from {existing[sid].get("source", "?")}, no longer in mods/)')

    result = OrderedDict([
        ('_readme', build_readme()),
        ('structure_sets', out_sets),
    ])

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUT_PATH.write_text(json.dumps(result, indent=2))
    print(f'wrote {len(out_sets)} structure_sets ({len(mod_sets)} modded + {len(VANILLA_STRUCTURE_SETS)} vanilla) -> {OUT_PATH}')
    total_structures = sum(len(v['structures']) for v in out_sets.values())
    missing_biomes = sum(1 for v in out_sets.values() for s in v['structures'] if s['biomes'] is None)
    print(f'{total_structures} structures total, {missing_biomes} with no biome info resolved')


if __name__ == '__main__':
    build()
