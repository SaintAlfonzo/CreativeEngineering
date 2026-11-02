#!/usr/bin/env python3
"""Where every crop, fruit tree and wild food plant generates: biomes, rate, height, ground.

    python3 tools/gen_crop_worldgen_table.py

Writes feature_writeup/crops_by_biome.md (generated reference, linked from crops.md section 2.7) and
kubejs/exported/worldgen/crop_worldgen.json (the same data for scripts).

How it works:
- Biome tags are merged from the vanilla jar, the Forge jar, every mod jar and kubejs/data, then resolved.
- Biomes and their base temperatures come from kubejs/exported/biome_data.json (/dumpbiomes); biome areas
  (post-tuning share of each dimension) from kubejs/generation_scripts/worldgen/biome_config.json.
- A crop feature reaches a biome two ways: the biome json lists it (Terralith ships every overworld biome,
  Hearths three Nether biomes), or a Forge biome modifier adds it (forge:add_features and Farmer's
  Delight's add_features_by_filter with its temperature window).
- JSON placed/configured features are walked to find the crop blocks they place (cocoa via the tree
  decorator). Features that mods register in Java (Farmer's Delight, Neapolitan, Fruits Delight, ...)
  have no json, so JAVA_PLACED below restates them from the decompiled jars (feature_writeup/crops.md 2.7).
  Config-driven chances are read from config/*.toml.

- The pack's own placement, Creative Delight's kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json and the
  pack's mushroom_spawns.json (applied in game by the wild_crops.js next to it and kubejs/server_scripts/worldgen/
  mushroom_spawns.js), with the biome traits of biome_traits.json, is evaluated with the same rules:
  the mods' modifiers they switch off are dropped, remove_features run after every add (Forge's order), and each
  rule's features go to the biomes it matches (mushroom rules in cave mode also match ocean and cave biomes). The
  Creative Delight wildCrops / CEConfig mushroomSpawns switches can't be read offline: the tables assume both are on, and the
  summary's "mod default" column shows the numbers with them off.
- The Fungi Delight port (development/backportedFungi) ships only configured features, under the upstream ids. While
  no jar in mods/ has them, they are read from the upstream jar in the ATLauncher downloads folder.

Rates are patch attempts per chunk (1/N = one chunk in N on average). "Per 1000 chunks" multiplies the
rate by the share of the dimension the crop's biomes cover; it is an upper bound, since an attempt still
fails when the ground, water or space test fails.
"""
import io
import json
import re
import zipfile
import sys
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths  # noqa: E402
from pack_paths import loads_lenient  # noqa: E402

INSTANCE = pack_paths.INSTANCE
MODS = INSTANCE / 'mods'
CONFIG = INSTANCE / 'config'
BIOME_DATA = INSTANCE / 'kubejs/exported/biome_data.json'
BIOME_CONFIG = INSTANCE / 'kubejs/generation_scripts/worldgen/biome_config.json'
OUT_MD = INSTANCE / 'feature_writeup/crops_by_biome.md'
OUT_JSON = INSTANCE / 'kubejs/exported/worldgen/crop_worldgen.json'
WILD_CROPS = INSTANCE / 'kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json'
BIOME_TRAITS = INSTANCE / 'kubejs/generation_scripts/worldgen/biome_traits.json'
MUSHROOM_SPAWNS = INSTANCE / 'kubejs/generation_scripts/worldgen/mushroom_spawns.json'
FUNGI_UPSTREAM = Path.home() / '.local/share/atlauncher/downloads/FungiDelight-1.20.1-1.3.jar'
FUNGI_CONFIGURED = 'data/fungidelight/worldgen/configured_feature/'

BASE_JARS = [j for j in (pack_paths.vanilla_jar(), pack_paths.forge_jar('universal')) if j]


# ── config values the Java features read ────────────────────────────────────

def toml_int(file, key, default, section=None):
    """First `key = <int>` in config/<file> (after `[section]` when given). Good enough for flat mod configs."""
    path = CONFIG / file
    if not path.exists():
        return default
    text = path.read_text()
    if section:
        i = text.find(f'[{section}]')
        if i < 0:
            return default
        text = text[i:]
    m = re.search(rf'^\s*"?{re.escape(key)}"?\s*=\s*(-?\d+)', text, re.M)
    return int(m.group(1)) if m else default


def rarity(n):
    return {'type': 'minecraft:rarity_filter', 'chance': n}


def count_extra(base, chance, extra):
    """PlacementUtils.countExtra: `base` per chunk, plus `extra` more with probability `chance`."""
    return {'type': 'minecraft:count', 'count': {'type': 'minecraft:weighted_list', 'distribution': [
        {'data': base, 'weight': round((1 - chance) * 1e6)}, {'data': base + extra, 'weight': round(chance * 1e6)}]}}


HM_MOTION = {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'}        # PlacementUtils.HEIGHTMAP
HM_WORLD = {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'}        # HEIGHTMAP_WORLD_SURFACE
HM_FLOOR = {'type': 'minecraft:heightmap', 'heightmap': 'OCEAN_FLOOR'}             # treePlacement
FULL_RANGE = {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform',
              'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'below_top': 0}}}
CAVE_RANGE = {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform',
              'min_inclusive': {'above_bottom': 0}, 'max_inclusive': {'absolute': 256}}}

FD = 'farmersdelight-common.toml'


def fd(name):
    return toml_int(FD, 'chance', {'wild_cabbages': 30, 'wild_beetroots': 30, 'wild_potatoes': 100, 'wild_carrots': 120,
                                   'wild_onions': 120, 'wild_tomatoes': 100, 'wild_rice': 20,
                                   'brown_mushroom_colonies': 15, 'red_mushroom_colonies': 15}[name], f'world.{name}')


# Fruits Delight trees: (name, spawn N in FDTrees: countExtra(0, 1/N, 1) per chunk)
FRUIT_TREES = [('apple', 20), ('bayberry', 15), ('durian', 200), ('fig', 20), ('hawberry', 20), ('kiwi', 20),
               ('lychee', 4), ('mango', 4), ('mangosteen', 40), ('orange', 20), ('peach', 8), ('pear', 15),
               ('persimmon', 50)]

# Placed features registered in Java (no json). crop: label used in the tables; ground/notes: what the
# decompiled feature needs and does. Keep in step with feature_writeup/crops.md 2.7.
JAVA_PLACED = {
    # vanilla (VegetationPlacements / CavePlacements / TreePlacements, 1.19.2 mojmap)
    'minecraft:patch_berry_common': dict(crop='Sweet Berries', mods=[rarity(32), HM_WORLD],
        ground='grass block', notes='96 tries, bushes at age 3 (ripe)'),
    'minecraft:patch_berry_rare': dict(crop='Sweet Berries', mods=[rarity(384), HM_WORLD],
        ground='grass block', notes='96 tries, bushes at age 3 (ripe)'),
    'minecraft:patch_pumpkin': dict(crop='Pumpkin', mods=[rarity(300), HM_MOTION], ground='grass block', notes='96 tries'),
    'minecraft:patch_melon': dict(crop='Melon', mods=[rarity(6), HM_MOTION], ground='grass block', notes='64 tries'),
    'minecraft:patch_melon_sparse': dict(crop='Melon', mods=[rarity(64), HM_MOTION], ground='grass block', notes='64 tries'),
    'minecraft:patch_sugar_cane': dict(crop='Sugar Cane', mods=[rarity(6), HM_MOTION],
        ground='dirt or sand next to water', notes='20 tries'),
    'minecraft:patch_sugar_cane_swamp': dict(crop='Sugar Cane', mods=[rarity(3), HM_MOTION],
        ground='dirt or sand next to water', notes='20 tries'),
    'minecraft:patch_sugar_cane_desert': dict(crop='Sugar Cane', mods=[HM_MOTION],
        ground='dirt or sand next to water', notes='20 tries, every chunk'),
    'minecraft:patch_sugar_cane_badlands': dict(crop='Sugar Cane', mods=[rarity(5), HM_MOTION],
        ground='dirt or sand next to water', notes='20 tries'),
    'minecraft:cave_vines': dict(crop='Glow Berries', mods=[{'type': 'minecraft:count', 'count': 188}, CAVE_RANGE],
        ground='under a solid ceiling (scans up 12)', notes='vine columns, some segments with berries'),
    'minecraft:jungle_tree': dict(crop='Cocoa', mods=[], ground='jungle tree trunk',
        notes='vanilla jungle tree, CocoaDecorator 0.2'),
    # Farmer's Delight 1.2.4 WildCropGeneration: WildCropFeature 64 tries, crop within 4 xz, companion within 6, y +-3
    'farmersdelight:patch_wild_cabbages': dict(crop='Cabbage', mods=[rarity(fd('wild_cabbages')), HM_MOTION],
        ground='sand (#minecraft:sand)', notes='companion: Sandy Shrub'),
    'farmersdelight:patch_wild_beetroots': dict(crop='Beetroot (Sea Beet)', mods=[rarity(fd('wild_beetroots')), HM_MOTION],
        ground='sand (#minecraft:sand)', notes='companion: Sandy Shrub'),
    'farmersdelight:patch_wild_onions': dict(crop='Onion', mods=[rarity(fd('wild_onions')), HM_MOTION],
        ground='#minecraft:dirt', notes='companion: Allium'),
    'farmersdelight:patch_wild_tomatoes': dict(crop='Tomato', mods=[rarity(fd('wild_tomatoes')), HM_MOTION],
        ground='#farmersdelight:terrain (dirt, grass, sand, red sand, mud...)', notes='companion: Dead Bush'),
    'farmersdelight:patch_wild_carrots': dict(crop='Carrot', mods=[rarity(fd('wild_carrots')), HM_MOTION],
        ground='#minecraft:dirt', notes='companion: Grass; turns the dirt floor of the patch to Coarse Dirt'),
    'farmersdelight:patch_wild_potatoes': dict(crop='Potato', mods=[rarity(fd('wild_potatoes')), HM_MOTION],
        ground='#minecraft:dirt', notes='companion: Fern'),
    'farmersdelight:patch_wild_rice': dict(crop='Rice', mods=[rarity(fd('wild_rice')), HM_MOTION],
        ground='full water source 1 deep, air above, on #minecraft:dirt or sand', notes='96 tries within 7 xz of the sea/lake floor'),
    'farmersdelight:patch_brown_mushroom_colonies': dict(crop='Brown Mushroom Colony', mods=[rarity(fd('brown_mushroom_colonies')), HM_MOTION],
        ground='mycelium', notes='colony age 0-3; companion: Brown Mushroom'),
    'farmersdelight:patch_red_mushroom_colonies': dict(crop='Red Mushroom Colony', mods=[rarity(fd('red_mushroom_colonies')), HM_MOTION],
        ground='mycelium', notes='colony age 0-3; companion: Red Mushroom'),
    # Farmer's Respite 2.0 FRGeneration
    'farmersrespite:patch_wild_tea_bush': dict(crop='Tea', mods=[rarity(toml_int('farmersrespite-common.toml', 'teaBushChance', 1)), HM_MOTION],
        ground='#minecraft:dirt', notes='only 3 tries (crop within 3 xz, Fern within 5): a bush or two'),
    'farmersrespite:patch_coffee_bush': dict(crop='Coffee (Farmer\'s Respite)', mods=[rarity(toml_int('farmersrespite-common.toml', 'coffeeBushChance', 1)), FULL_RANGE],
        ground='basalt', notes='64 tries, y +-6 around a random height; companion: Wither Roots'),
    # Delightful 3.4.1 DelightfulWildCropGeneration
    'delightful:patch_wild_salmonberries': dict(crop='Salmonberries', mods=[rarity(toml_int('delightful-common.toml', 'chance_wild_salmonberries', 35)), HM_MOTION],
        ground='#minecraft:dirt', notes='Farmer\'s Delight wild-crop patch, companion: Grass'),
    'delightful:patch_mini_melon': dict(crop='Mini Melon', mods=[rarity(toml_int('delightful-common.toml', 'chance_mini_melon', 35)), HM_MOTION],
        ground='#minecraft:dirt', notes='64 tries within 4'),
    'delightful:patch_cantaloupe': dict(crop='Cantaloupe', mods=[rarity(toml_int('delightful-common.toml', 'chance_cantaloupe', 55)), HM_MOTION],
        ground='#delightful:cantaloupe_spawns (dirt, grass, sand, red sand, mud...)', notes='64 tries within 3'),
    # Corn Delight 1.0.3 WildCornGeneration (rarity hard-coded)
    'corn_delight:patch_wild_corn': dict(crop='Corn', mods=[rarity(30), HM_MOTION],
        ground='#minecraft:dirt', notes='companion: Grass; Coarse Dirt floor like wild carrots'),
    # Rustic Delight 1.3.0 (Feature.FLOWER patches: bone meal on grass in these biomes can also grow them)
    'rusticdelight:wild_cotton_placed': dict(crop='Cotton', mods=[rarity(toml_int('rusticdelight-common.toml', 'wild_cotton_chance', 96)), HM_MOTION],
        ground='#minecraft:dirt', notes='32 tries within 6; flower-type feature, so bone meal on grass here can grow it'),
    'rusticdelight:wild_bell_peppers_placed': dict(crop='Bell Pepper', mods=[rarity(toml_int('rusticdelight-common.toml', 'wild_bell_peppers_chance', 15)), HM_MOTION],
        ground='#minecraft:dirt', notes='48 tries within 4; flower-type feature (bone meal on grass)'),
    'rusticdelight:wild_coffee_placed': dict(crop='Coffee (Rustic Delight)', mods=[rarity(toml_int('rusticdelight-common.toml', 'wild_coffee_chance', 15)), HM_MOTION],
        ground='#minecraft:dirt', notes='48 tries within 6; flower-type feature (bone meal on grass)'),
    # Rustic Delight 1.7.0 (Creative Delight port; json in the rustic_upstream donor, rarity from the port's ChanceFilter)
    'rusticdelight:bell_pepper_block_patch_placed': dict(crop='Giant Bell Peppers', mods=[rarity(64), HM_MOTION],
        ground='grass block', notes='48 tries within 7; red, yellow, green giant pepper blocks (needs Creative Delight)'),
    # Nether's Delight 3.1 NDGeneration + PropelplantFeature (CD repackedNethers; 4.0 renamed it propelplant_patch, the pack
    # keeps the 3.1 id, nethers_delight.md 4.5)
    'nethersdelight:patch_propelplant': dict(crop='Propelplant', mods=[{'type': 'minecraft:count', 'count': 8}, FULL_RANGE],
        ground='#minecraft:nylium (crimson/warped nylium)', notes='cross-shaped 7x7, y -1..+1 of a random height, 1 in 3 spots'),
    # Neapolitan 4.1.0 NeapolitanFeatures
    'neapolitan:patch_strawberry_bush': dict(crop='Strawberries', mods=[rarity(64), HM_WORLD],
        ground='grass block or coarse dirt', notes='512 tries within 5; 90% red bushes at age 6, 10% unripe on Coarse Dirt; '
                                                   'turns nearby ground to Coarse Dirt'),
    'neapolitan:patch_vanilla_vine': dict(crop='Vanilla', mods=[rarity(64), HM_WORLD],
        ground='side of #neapolitan:vanilla_plantable_on (logs, leaves, dirt)', notes='64 tries per direction within 7'),
    'neapolitan:patch_adzuki_sprouts': dict(crop='Adzuki Beans', mods=[rarity(128), HM_WORLD],
        ground='grass block', notes='256 tries within 3; ripe sprouts (age 6); the grass under each becomes Dirt'),
    'neapolitan:mint_pond': dict(crop='Mint', mods=[rarity(4), HM_WORLD],
        ground='flat grass/dirt', notes='digs a small pond (1-9 water) with grown mint around it and often one spruce tree'),
    'neapolitan:banana_plant_common': dict(crop='Banana', mods=[count_extra(0, 0.25, 3)],
        ground='grass block, sand or gravel', notes='stalk 3-6 tall with fronds, maybe a bundle (chimpanzees spawn with 25%); '
                                                    'grass around turns to Gravel'),
    'neapolitan:banana_plant_uncommon': dict(crop='Banana', mods=[count_extra(0, 0.1, 1)],
        ground='grass block, sand or gravel', notes='see banana_plant_common'),
    'neapolitan:banana_plant_rare': dict(crop='Banana', mods=[count_extra(0, 0.05, 1)],
        ground='grass block, sand or gravel', notes='see banana_plant_common'),
    # Fruits Delight 0.5.11 FDBushes / FDPineapple / FDMelons (trees added below)
    'fruitsdelight:blueberry_bush': dict(crop='Blueberry', mods=[rarity(32), HM_WORLD], ground='grass block', notes='20 tries, ripe (age 4)'),
    'fruitsdelight:cranberry_bush': dict(crop='Cranberry', mods=[rarity(32), HM_WORLD], ground='grass block', notes='20 tries, ripe (age 4)'),
    'fruitsdelight:lemon_tree': dict(crop='Lemon', mods=[rarity(32), HM_WORLD], ground='grass block', notes='20 tries, ripe two-high bush'),
    'fruitsdelight:pineapple': dict(crop='Pineapple', mods=[rarity(32), HM_MOTION], ground='sand', notes='24 tries within 5; Wild Pineapple'),
    'fruitsdelight:hamimelon': dict(crop='Hamimelon', mods=[rarity(64), HM_MOTION], ground='sand', notes='24 tries within 5; melon blocks'),
    # Upgrade Aquatic 5.1.0 RiverTreeFeature
    'upgrade_aquatic:trees_river': dict(crop='Mulberries', mods=[count_extra(1, 0.5, 2), HM_FLOOR], ground='under river tree leaves',
        notes='river trees; each leaf with air below has 1 in 6 to grow a ripe mulberry vine'),
}
for _name, _n in FRUIT_TREES:
    JAVA_PLACED[f'fruitsdelight:tree/{_name}_tree'] = dict(
        crop=_name.capitalize() + ' (tree)', mods=[count_extra(0, 1 / _n, 1), HM_FLOOR],
        ground='sapling soil, no water above', notes=f'1 tree per {_n} chunks; natural leaves flower and fruit')

# Biomes a mod injects into a dimension after the biome source is built, so /dumpbiomes lists no dimension.
RUNTIME_BIOMES = {'quark:glimmering_weald': 'minecraft:overworld'}   # Quark UndergroundBiomeHandler

# Java placed features that only appear nested inside other features (no "reaches no biome" note for them).
NESTED_ONLY = {'minecraft:jungle_tree'}

# Configured features registered in Java that JSON features point at (vanilla ids without a json override).
JAVA_CONFIGURED = {
    'minecraft:jungle_tree': ['cocoa'],
    'minecraft:trees_jungle': ['cocoa'],     # random_selector whose default is jungle_tree_checked (with cocoa)
    # Fungi Delight huge mushrooms: Java features with an empty config
    'fungidelight:huge_inky_cap_mushroom': ['Huge Inky Cap'],
    'fungidelight:huge_morel_mushroom': ['Huge Morel'],
    # Fungi Delight's rooted dirt blob (an ore feature on vanilla rooted dirt: labelled here, not via CROP_BLOCKS,
    # which would also catch azalea trees)
    'fungidelight:rooted_dirt_blob': ['Rooted Dirt (truffle ground)'],
}

# Blocks found in json features that count as a crop, and the label they get.
CROP_BLOCKS = {
    'minecraft:sweet_berry_bush': 'Sweet Berries', 'minecraft:pumpkin': 'Pumpkin', 'minecraft:melon': 'Melon',
    'minecraft:sugar_cane': 'Sugar Cane', 'minecraft:cave_vines': 'Glow Berries', 'minecraft:bamboo': 'Bamboo',
    'minecraft:cactus': 'Cactus', 'minecraft:chorus_flower': 'Chorus Fruit',
    'myrtrees:rubberwood_log': 'Rubber tree',
    'supplementaries:wild_flax': 'Flax',
    'endersdelight:amberveil_mushroom': 'Amberveils', 'endersdelight:chorus_flame_block': 'Chorusflame',
    'endersdelight:ethereal_saffron_bush': 'Ethereal Saffron', 'endersdelight:voidpepper_block': 'Void Pepper',
    'ends_delight:chorus_succulent': 'Chorus Succulent',
    'hearthandharvest:raspberry_bush': 'Raspberry',
    'vinery:red_grape_bush': 'Grapes: red', 'vinery:white_grape_bush': 'Grapes: white',
    'vinery:savanna_grape_bush_red': 'Grapes: savanna red', 'vinery:savanna_grape_bush_white': 'Grapes: savanna white',
    'vinery:taiga_grape_bush_red': 'Grapes: taiga red', 'vinery:taiga_grape_bush_white': 'Grapes: taiga white',
    'vinery:jungle_grape_bush_red': 'Grapes: jungle red', 'vinery:jungle_grape_bush_white': 'Grapes: jungle white',
    'vinery:apple_leaves': 'Apple (Vinery tree)', 'vinery:cherry_leaves': 'Cherry (Vinery tree)',
    'vinery:dark_cherry_leaves': 'Cherry (Vinery tree)',  # 1.5.5 id; the port renames dark_cherry_* to cherry_*
    # the pack's own json versions of Java features (kubejs/data/creativeengineering/worldgen/*/wild/)
    'farmersdelight:wild_cabbages': 'Cabbage', 'farmersdelight:wild_beetroots': 'Beetroot (Sea Beet)',
    'farmersdelight:brown_mushroom_colony': 'Brown Mushroom Colony', 'farmersdelight:red_mushroom_colony': 'Red Mushroom Colony',
    'fruitsdelight:hamimelon': 'Cantaloupe', 'delightful:cantaloupe': 'Mini Cantaloupe',  # item_merges.md 2.10
    # Veggies Delight backport (development/backportedVeggies)
    'veggiesdelight:wild_broccoli': 'Broccoli', 'veggiesdelight:wild_cauliflowers': 'Cauliflower',
    'veggiesdelight:wild_garlic': 'Garlic', 'veggiesdelight:wild_sweet_potatoes': 'Sweet Potato',
    'veggiesdelight:wild_turnips': 'Turnip', 'veggiesdelight:wild_zucchinis': 'Zucchini',
    # Vintage Delight backport (development/backportedVintage); wild ghost peppers are Nether-only
    'vintagedelight:wild_oats': 'Oats', 'vintagedelight:wild_peanuts': 'Peanuts',
    'vintagedelight:wild_cucumbers': 'Cucumbers',
    # Fungi Delight backport (development/backportedFungi); placement in mushroom_spawns.json / wild_crops.json
    'miners_delight:wild_cave_carrots': 'Cave Carrot',
    'fungidelight:inky_cap_mushroom': 'Inky Cap', 'fungidelight:morel_mushroom': 'Morel',
    'fungidelight:inky_cap_mushroom_colony': 'Inky Cap Colony', 'fungidelight:morel_mushroom_colony': 'Morel Colony',
    'fungidelight:truffle_dirt': 'Truffle Dirt',
}
COCOA = 'Cocoa'

# ── loading ──────────────────────────────────────────────────────────────────

def iter_sources():
    """(label, {path: bytes-reader}) in load order: vanilla, forge, mods, kubejs/data."""
    # the Vinery port ships code only and reads its data from the upstream jars at runtime (1.5.5 wins over 1.3.10)
    upstream = [INSTANCE / 'vinery_upstream' / n for n in ('letsdo-vinery-forge-1.3.10.jar', 'letsdo-vinery-fabric-1.5.5.jar')]
    fungi = False
    for jar in BASE_JARS + sorted(MODS.glob('*.jar')) + upstream:
        try:
            z = zipfile.ZipFile(jar)
        except (zipfile.BadZipFile, FileNotFoundError):
            continue
        # a bundle jar (Creative Delight) carries its members under META-INF/jarjar/; Forge loads them as mods
        for label, zz in [(jar.name, z)] + [(f'{jar.name}!{n.rsplit("/", 1)[1]}', zipfile.ZipFile(io.BytesIO(z.read(n))))
                                             for n in z.namelist() if n.startswith('META-INF/jarjar/') and n.endswith('.jar')]:
            names = [n for n in zz.namelist() if n.startswith('data/') and n.endswith('.json')]
            fungi = fungi or any(n.startswith(FUNGI_CONFIGURED) for n in names)
            yield label, [(n, (lambda z=zz, n=n: z.read(n))) for n in names]
    # the Fungi Delight port ships the upstream configured features (only those) under the same ids
    if not fungi and FUNGI_UPSTREAM.exists():
        z = zipfile.ZipFile(FUNGI_UPSTREAM)
        yield FUNGI_UPSTREAM.name, [(n, (lambda z=z, n=n: z.read(n))) for n in z.namelist()
                                    if n.startswith(FUNGI_CONFIGURED) and n.endswith('.json')]
    kd = INSTANCE / 'kubejs'
    yield 'kubejs/data', [(str(p.relative_to(kd)), (lambda p=p: p.read_bytes())) for p in (kd / 'data').rglob('*.json')]


PATTERNS = {
    'tag': re.compile(r'^data/([^/]+)/tags/worldgen/biome/(.+)\.json$'),
    'biome': re.compile(r'^data/([^/]+)/worldgen/biome/(.+)\.json$'),
    'placed': re.compile(r'^data/([^/]+)/worldgen/placed_feature/(.+)\.json$'),
    'configured': re.compile(r'^data/([^/]+)/worldgen/configured_feature/(.+)\.json$'),
    'modifier': re.compile(r'^data/([^/]+)/forge/biome_modifier/(.+)\.json$'),
}


def load_all():
    tags, biomes, placed, configured, modifiers = defaultdict(list), {}, {}, {}, {}
    for label, files in iter_sources():
        for name, read in files:
            for kind, pat in PATTERNS.items():
                m = pat.match(name)
                if not m:
                    continue
                rid = f'{m.group(1)}:{m.group(2)}'
                try:
                    data = loads_lenient(read())
                except (ValueError, UnicodeDecodeError):
                    break
                if kind == 'tag':
                    if data.get('replace'):
                        tags[rid] = []
                    for v in data.get('values', []):
                        tags[rid].append(v['id'] if isinstance(v, dict) else v)
                elif kind == 'biome':
                    biomes[rid] = (label, data)
                elif kind == 'placed':
                    placed[rid] = (label, data)
                elif kind == 'configured':
                    configured[rid] = (label, data)
                else:
                    modifiers[rid] = (label, data)
                break
    return tags, biomes, placed, configured, modifiers


def resolve_tag(tag, tags, cache, stack=frozenset()):
    if tag in cache:
        return cache[tag]
    if tag in stack:
        return frozenset()
    out = set()
    for v in tags.get(tag, []):
        out |= resolve_tag(v[1:], tags, cache, stack | {tag}) if v.startswith('#') else {v}
    cache[tag] = frozenset(out)
    return cache[tag]


def holder_set(spec, tags, cache, all_biomes):
    if spec is None:
        return set()
    if isinstance(spec, dict):
        return set(all_biomes) if spec.get('type') == 'forge:any' else set()
    if isinstance(spec, list):
        out = set()
        for s in spec:
            out |= holder_set(s, tags, cache, all_biomes)
        return out
    return set(resolve_tag(spec[1:], tags, cache)) if spec.startswith('#') else {spec}


# ── feature walking ──────────────────────────────────────────────────────────

def crops_in(fid, placed, configured, kind, seen):
    """Crop labels reachable from a placed ('p') or configured ('c') feature id."""
    key = (kind, fid)
    if key in seen:
        return set()
    seen.add(key)
    if kind == 'p':
        if fid in placed:
            target = placed[fid][1].get('feature')
            return crops_in_value(target, placed, configured, seen, configured_ref=True)
        if fid in JAVA_PLACED:
            return {JAVA_PLACED[fid]['crop']}
        return set()
    out = {COCOA if c == 'cocoa' else c for c in JAVA_CONFIGURED.get(fid, [])}
    if fid in configured:
        out |= crops_in_value(configured[fid][1], placed, configured, seen)
    return out


def crops_in_value(v, placed, configured, seen, configured_ref=False):
    out = set()
    if isinstance(v, str):
        if configured_ref:
            return crops_in(v, placed, configured, 'c', seen)
        if ':' in v:
            if v in placed or v in JAVA_PLACED:
                out |= crops_in(v, placed, configured, 'p', seen)
            if v in configured or v in JAVA_CONFIGURED:
                out |= crops_in(v, placed, configured, 'c', seen)
            if v in CROP_BLOCKS:
                out.add(CROP_BLOCKS[v])
        return out
    if isinstance(v, list):
        for x in v:
            out |= crops_in_value(x, placed, configured, seen)
        return out
    if isinstance(v, dict):
        if v.get('type') == 'minecraft:cocoa':
            out.add(COCOA)
        if v.get('type') == 'minecraft:chorus_plant':
            out.add('Chorus Fruit')
        if v.get('type') == 'ends_delight:chorus_succulent':   # End's Delight's Java feature (count config)
            out.add('Chorus Succulent')
        if 'placement' in v and isinstance(v.get('feature'), str):   # inline placed feature: feature = configured id
            out |= crops_in(v['feature'], placed, configured, 'c', seen)
        for k, x in v.items():
            if k == 'Name' and isinstance(x, str) and x in CROP_BLOCKS:
                out.add(CROP_BLOCKS[x])
            elif not (k == 'feature' and 'placement' in v and isinstance(x, str)):
                out |= crops_in_value(x, placed, configured, seen)
    return out


def mean_count(c):
    if isinstance(c, (int, float)):
        return float(c)
    t = c.get('type')
    if t == 'minecraft:constant':
        return float(c['value'])
    if t == 'minecraft:uniform':
        v = c.get('value', c)
        return (v['min_inclusive'] + v['max_inclusive']) / 2
    if t == 'minecraft:weighted_list':
        d = c['distribution']
        tw = sum(e['weight'] for e in d)
        return sum(mean_count(e['data']) * e['weight'] for e in d) / tw if tw else 0.0
    if t == 'minecraft:biased_to_bottom':
        v = c.get('value', c)
        return (v['min_inclusive'] + v['max_inclusive']) / 2
    return None


def anchor(a):
    (k, v), = a.items()
    return {'absolute': f'{v}', 'above_bottom': f'bottom+{v}' if v else 'bottom', 'below_top': f'top-{v}' if v else 'top'}[k]


def placement_info(mods):
    """(attempts per chunk or None, height text, extra notes) from a placement modifier list."""
    rate, where, notes = 1.0, [], []
    for m in mods:
        t = m.get('type', '')
        if t == 'minecraft:count':
            c = mean_count(m['count'])
            if c is None:
                notes.append('variable count')
                rate = None
            elif rate is not None:
                rate *= c
        elif t == 'minecraft:rarity_filter':
            rate = None if rate is None else rate / m['chance']
        elif t == 'rusticdelight:configurable_rarity_filter':
            notes.append('config rarity')
        elif t in ('minecraft:noise_based_count', 'minecraft:noise_threshold_count', 'minecraft:count_on_every_layer'):
            if 'noise-based count' not in notes:
                notes.append('noise-based count')
            rate = None
        elif t == 'minecraft:heightmap':
            where.append({'MOTION_BLOCKING': 'surface', 'WORLD_SURFACE_WG': 'surface',
                          'OCEAN_FLOOR': 'surface or sea floor', 'OCEAN_FLOOR_WG': 'surface or sea floor',
                          'MOTION_BLOCKING_NO_LEAVES': 'surface'}.get(m['heightmap'], m['heightmap']))
        elif t == 'minecraft:height_range':
            h = m['height']
            if 'min_inclusive' in h:
                where.append(f"Y {anchor(h['min_inclusive'])} to {anchor(h['max_inclusive'])}")
            else:
                where.append('fixed Y')
        elif t == 'minecraft:surface_water_depth_filter':
            notes.append('dry land' if m.get('max_water_depth', 0) == 0 else f"water up to {m['max_water_depth']} deep")
        elif t == 'minecraft:environment_scan':
            notes.append('scans for a ceiling/floor')
    return rate, ' then '.join(where) or 'surface (feature finds it)', notes


def fmt_rate(r):
    if r is None:
        return 'variable'
    if r >= 1:
        return f'{r:.2f}'.rstrip('0').rstrip('.') + ' per chunk'
    n = 1 / r
    return f'1 in {n:.0f} chunks' if n >= 10 else f'1 in {n:.1f} chunks'


# ── wild_crops.json (same rules as Creative Delight's _worldgen/wild_crops.js) ──

NEVER = {'ocean', 'cave'}      # surface features land on water or in another biome there


def load_wild_crops():
    """(config, biome traits). The config is Creative Delight's wild_crops.json with "biomes" from biome_traits.json;
    its crops also carry mushroom_spawns.json's rules, keys prefixed 'mushroom_spawns:' and marked '_mushroom': both
    systems share the biome table and the clause rules."""
    cfg = loads_lenient(WILD_CROPS.read_text())
    cfg['biomes'] = loads_lenient(BIOME_TRAITS.read_text())['biomes']
    if MUSHROOM_SPAWNS.exists():
        for key, rule in loads_lenient(MUSHROOM_SPAWNS.read_text()).get('rules', {}).items():
            cfg['crops']['mushroom_spawns:' + key] = dict(rule, _mushroom=True)
    return cfg, {b: set(t.split()) for b, t in cfg['biomes'].items()}


def rule_never(crop):
    """Traits that keep a biome out of a rule: none for a mushroom rule in cave mode."""
    return set() if crop.get('mode') == 'cave' else NEVER


def clause_matches(traits, clause):
    return all((t[1:] not in traits) if t.startswith('!') else (t in traits) for t in clause.split())


def resolve_crop(traits, crop):
    """Clauses OR'ed, traits in a clause AND'ed ('!x' negated), then add / except."""
    never = rule_never(crop)
    out = {b for b, tr in traits.items() if not tr & never and any(clause_matches(tr, c) for c in crop.get('where', []))}
    return sorted((out | set(crop.get('add', []))) - set(crop.get('except', [])))


# ── main ─────────────────────────────────────────────────────────────────────

def feature_map(world, crop_set, wild):
    """feature -> {biome: how}. Forge's order: biome files, then every add modifier, then every remove."""
    tags, biome_files, placed, configured, modifiers, bdata, cache = world
    all_biomes = sorted(bdata)
    fb = defaultdict(dict)
    for b, (label, data) in biome_files.items():
        for step in data.get('features', []):
            for fid in step if isinstance(step, list) else [step]:
                if isinstance(fid, str) and crop_set(fid):
                    fb[fid][b] = f'biome json ({label.split("_")[0].split("-")[0]})'
    disabled = {m for c in wild[0]['crops'].values() for m in c.get('disable_modifiers', [])} if wild else set()
    removes, empty = [], set()
    for mid, (label, data) in sorted(modifiers.items()):
        t = data.get('type')
        if mid in disabled or t not in ('forge:add_features', 'farmersdelight:add_features_by_filter', 'forge:remove_features'):
            continue
        feats = data.get('features')
        feats = [feats] if isinstance(feats, str) else (feats or [])
        feats = [f for f in feats if isinstance(f, str) and crop_set(f)]
        if not feats:
            continue
        if t == 'forge:remove_features':
            removes.append((data.get('biomes'), feats))
            continue
        if t == 'forge:add_features':
            bs = holder_set(data.get('biomes'), tags, cache, all_biomes)
        else:
            bs = holder_set(data.get('allowed_biomes'), tags, cache, all_biomes)
            bs -= holder_set(data.get('denied_biomes'), tags, cache, all_biomes)
            lo, hi = data.get('min_temperature'), data.get('max_temperature')
            bs = {b for b in bs if b in bdata and (lo is None or bdata[b]['temperature'] >= lo)
                  and (hi is None or bdata[b]['temperature'] <= hi)}
        for f in feats:
            if not bs:
                empty.add(f)
            for b in bs:
                if b in bdata:
                    fb[f][b] = f'modifier {mid}'
    if wild:
        cfg, traits = wild
        for key, crop in cfg['crops'].items():
            bs = [b for b in resolve_crop(traits, crop) if b in bdata]
            for f in crop.get('features', []):
                for b in bs:
                    fb[f][b] = f'wild_crops.json {key}'
        rem = sorted({f for c in cfg['crops'].values() for f in c.get('remove_features', [])})
        removes.append(('#minecraft:is_overworld', rem))
    for spec, feats in removes:
        for b in holder_set(spec, tags, cache, all_biomes):
            for f in feats:
                fb[f].pop(b, None)
    return fb, empty


def build_rows(fb, world, crop_set, area, oceans, dim_of, notes_by_feature):
    placed = world[2]
    rows = []
    for fid, bmap in fb.items():
        if not bmap:
            continue
        if fid in placed:
            src = placed[fid][0]
            rate, where, notes = placement_info(placed[fid][1].get('placement', []))
            ground, extra = '', ''
        elif fid in JAVA_PLACED:
            j = JAVA_PLACED[fid]
            src = 'Java'
            rate, where, notes = placement_info(j['mods'])
            ground, extra = j['ground'], j['notes']
        else:
            continue
        dims = defaultdict(float)
        for b in bmap:
            d, layer, pct = area.get(b, (dim_of(b), '?', 0.0))
            if layer == 'surface' and b not in oceans:
                dims[d] += pct
        for crop in sorted(crop_set(fid)):
            n = ([extra] if extra else []) + notes + ([notes_by_feature[fid]] if fid in notes_by_feature else [])
            if crop == COCOA and fid not in JAVA_PLACED:
                n.append('rate counts trees; cocoa only on vanilla-style jungle trees (CocoaDecorator 0.2)')
            per1000 = {d: (rate or 0) * pct / 100 * 1000 for d, pct in dims.items()}
            rows.append(dict(crop=crop, feature=fid, source=src, rate=rate, where=where, ground=ground,
                             notes='; '.join(n), biomes=sorted(bmap), trees=crop == COCOA,
                             how=sorted(set(bmap.values())), area_pct=dict(dims), per_1000_chunks=per1000))
    rows.sort(key=lambda r: (r['crop'].lower(), r['feature']))
    return rows


def main():
    tags, biome_files, placed, configured, modifiers = load_all()
    cache = {}
    bdata = json.loads(BIOME_DATA.read_text())
    world = (tags, biome_files, placed, configured, modifiers, bdata, cache)
    cfg_b = json.loads(BIOME_CONFIG.read_text())
    area = {}            # biome -> (dimension, layer, pct of that dimension layer)
    for dim, dd in cfg_b['dimensions'].items():
        for b, info in dd['biomes'].items():
            area[b] = (dim, info.get('layer', 'surface'), info.get('result_pct', info.get('share_pct')) or 0.0)
    oceans = set(resolve_tag('minecraft:is_ocean', tags, cache))   # surface there is water: no land crop roots

    def dim_of(b):
        if b in area:
            return area[b][0]
        ds = bdata.get(b, {}).get('dimensions') or []
        return ds[0] if ds else '?'

    feature_crops = {}

    def crop_set(fid):
        if fid not in feature_crops:
            feature_crops[fid] = crops_in(fid, placed, configured, 'p', set())
        return feature_crops[fid]

    wild = load_wild_crops()
    cfg, traits = wild
    notes_by_feature = {f: c['notes'] for c in cfg['crops'].values() if c.get('notes') for f in c.get('features', [])}

    # biomes that are in no dimension's biome source never generate: keep them out of the counts
    dead = {b for b in bdata if not bdata[b].get('dimensions') and b not in area and b not in RUNTIME_BIOMES}
    for b, d in RUNTIME_BIOMES.items():
        area.setdefault(b, (d, 'underground', 0.0))

    def finish(fb):
        dropped = set()
        for bmap in fb.values():
            for b in list(bmap):
                if b in dead:
                    dropped.add(b)
                    del bmap[b]
        return dropped

    fb_default, _ = feature_map(world, crop_set, None)
    finish(fb_default)
    fb, empty_modifier_features = feature_map(world, crop_set, wild)
    dropped = finish(fb)
    rows_default = build_rows(fb_default, world, crop_set, area, oceans, dim_of, {})
    rows = build_rows(fb, world, crop_set, area, oceans, dim_of, notes_by_feature)

    checks = coverage_checks(world, wild, crop_set, area, oceans, rows)
    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.write_text(json.dumps(rows, indent=1) + '\n')
    write_md(rows, rows_default, wild, area, dim_of, sorted(dropped), checks)
    print(f'{len(rows)} crop/feature rows, {len({r["crop"] for r in rows})} crops -> {OUT_MD.relative_to(INSTANCE)}')
    for c in checks['problems']:
        print('  PROBLEM: ' + c)
    if checks['bare']:
        print(f"  {len(checks['bare'])} land biomes with no wild crop: " + ', '.join(short(b) for b in checks['bare']))
    disabled = {m for c in cfg['crops'].values() for m in c.get('disable_modifiers', [])}
    removed = {f for c in cfg['crops'].values() for f in c.get('remove_features', [])}
    for fid in sorted(set(JAVA_PLACED) | empty_modifier_features):
        if fid not in NESTED_ONLY and fid not in removed and not fb.get(fid) and not any(
                modifiers.get(m, (0, {}))[1].get('features') in (fid, [fid]) for m in disabled):
            print(f'  note: {fid} reaches no biome (no biome json lists it and its modifiers match no biome)')


def coverage_checks(world, wild, crop_set, area, oceans, rows):
    """Config problems + which land biomes the rules leave bare."""
    tags, biome_files, placed, configured, modifiers, bdata, cache = world
    cfg, traits = wild
    problems = []
    overworld = sorted(b for b, v in bdata.items() if 'minecraft:overworld' in (v.get('dimensions') or [])) \
        + sorted(b for b, d in RUNTIME_BIOMES.items() if d == 'minecraft:overworld')
    for b in overworld:
        if b not in traits:
            problems.append(f'{b} is an overworld biome with no entry in biome_traits.json (gets no crops)')
    for b in traits:
        if b not in bdata and b not in RUNTIME_BIOMES:
            problems.append(f'biome_traits.json lists {b}, which is not a registered biome')
    vocab = set().union(*traits.values())
    for key, crop in cfg['crops'].items():
        for clause in crop.get('where', []):
            for t in clause.split():
                if t.lstrip('!') not in vocab:
                    problems.append(f'{key}: trait "{t.lstrip("!")}" is on no biome (typo?)')
        for b in crop.get('add', []) + crop.get('except', []):
            # End and Nether biomes have no traits; a rule names them through add (the End plants)
            if b not in traits and b not in bdata:
                problems.append(f'{key}: add/except names {b}, which is neither in "biomes" nor a registered biome')
        for f in crop.get('features', []):
            if f not in placed and f not in JAVA_PLACED:
                problems.append(f'{key}: feature {f} is neither a json placed feature nor in JAVA_PLACED')
            elif not crop_set(f):
                problems.append(f'{key}: feature {f} places no known crop block (add it to CROP_BLOCKS)')
        for m in crop.get('disable_modifiers', []):
            if m not in modifiers:
                problems.append(f'{key}: disable_modifiers names {m}, which no jar ships')
        if crop.get('features') and not resolve_crop(traits, crop):
            problems.append(f'{key}: the rule matches no biome')
    land = [b for b in sorted(traits) if not traits[b] & NEVER and b in bdata
            and area.get(b, ('', 'surface', 0))[1] == 'surface' and b not in oceans]
    covered = {b for r in rows for b in r['biomes']}
    return dict(problems=problems, bare=[b for b in land if b not in covered], land=land)


def short(b):
    return b.replace('minecraft:', '')


def per1000_total(rows, crop):
    return sum(v for r in rows if r['crop'] == crop for d, v in r['per_1000_chunks'].items() if d == 'minecraft:overworld')


def write_md(rows, rows_default, wild, area, dim_of, dropped, checks):
    cfg, traits = wild
    L = []
    L.append('# Crops by biome (generated)\n')
    L.append('Generated by `python3 tools/gen_crop_worldgen_table.py`. Do not edit by hand; the explanations, ground rules and '
             'gaps are in [crops.md](crops.md) sections 2.7 and 2.8. Rerun after adding or updating mods, after `/dumpbiomes`, '
             'after retuning biomes (`biome_config.json`) or after editing `wild_crops.json` or `mushroom_spawns.json`.\n')
    L.append('Every table assumes the pack\'s placement (`kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json` and '
             '`mushroom_spawns.json`, Creative Delight `[worldgen] wildCrops = true`, CEConfig `mushroomSpawns = true`). Section 1 also '
             'shows each crop with the switches off (the mods\' own spawns).\n')
    L.append('- **Rate**: patch attempts per chunk. "1 in 30 chunks" means a chunk tries once with probability 1/30.')
    L.append('- **Share**: the part of the dimension (post-tuning biome areas from `biome_config.json`) covered by the biomes '
             'the feature is added to. Ocean biomes (`#minecraft:is_ocean`, about 41% of the overworld) and underground '
             'biomes are left out: a surface attempt there lands on water or in another biome. "no area data" means the '
             'biomes have no sampled area (skylands, caves).')
    L.append('- **Per 1000 chunks**: rate x share x 1000. An upper bound: an attempt still fails when the ground, water or '
             'space test fails (wild cabbages need sand or dirt at the spot).')
    L.append('- **Height**: where the attempt is placed. "surface" means the top block of the column (heightmap).\n')

    L.append('## 1. Per crop: pack placement against the mods\' defaults\n')
    L.append('Overworld patch attempts per 1000 chunks, all of the crop\'s features together (cocoa: trees).\n')
    L.append('| Crop | Biomes now | Per 1000 now | Mod default | Change |')
    L.append('|---|---|---|---|---|')
    for crop in sorted({r['crop'] for r in rows} | {r['crop'] for r in rows_default}, key=str.lower):
        now, before = per1000_total(rows, crop), per1000_total(rows_default, crop)
        nb = len({b for r in rows if r['crop'] == crop for b in r['biomes']})
        ch = '-' if not before and not now else 'new' if not before else 'gone' if not now else f'x{now / before:.2f}'
        L.append(f'| {crop} | {nb} | {now:.1f} | {before:.1f} | {ch} |')
    L.append('')

    L.append('## 2. Summary per feature\n')
    L.append('| Crop | Feature | Rate | Height | Biomes | Share | Per 1000 chunks |')
    L.append('|---|---|---|---|---|---|---|')
    for r in rows:
        share = ', '.join(f'{short(d)} {p:.1f}%' for d, p in sorted(r['area_pct'].items()) if p) or 'no area data'
        per = ', '.join(f'{v:.1f}' for d, v in sorted(r['per_1000_chunks'].items()) if v) or '-'
        if r['trees']:
            per += ' trees'
        L.append(f"| {r['crop']} | `{r['feature']}` | {fmt_rate(r['rate'])}{' (trees)' if r['trees'] else ''} | {r['where']} | {len(r['biomes'])} | {share} | {per} |")
    L.append('')

    def rule_text(crop):
        rule = ' or '.join(f'`{c}`' for c in crop.get('where', []))
        if crop.get('add'):
            rule += (' + ' if rule else '') + 'add ' + ', '.join(short(b) for b in crop['add'])
        if crop.get('except'):
            rule += ' except ' + ', '.join(short(b) for b in crop['except'])
        return rule

    def biome_text(bs):
        return f"{len(bs)}: {', '.join(short(b) for b in bs)}" if len(bs) <= 40 else f'{len(bs)}: every classified biome but ' + (
            ', '.join(short(b) for b in sorted(set(traits) - set(bs))) or 'none')

    L.append('## 3. wild_crops.json rules\n')
    L.append('| Key | Crop | Rule | Biomes |')
    L.append('|---|---|---|---|')
    for key, crop in cfg['crops'].items():
        if crop.get('_mushroom'):
            continue
        step = f" ({crop['step']})" if crop.get('step') else ''
        L.append(f"| {key} | {crop.get('label', key)}{step} | {rule_text(crop)} | {biome_text(resolve_crop(traits, crop))} |")
    L.append('')
    L.append('### 3.1 mushroom_spawns.json rules\n')
    L.append('Same biome words and clause rules; `surface` mode skips ocean and cave biomes, `cave` mode matches them too.\n')
    L.append('| Key | Mushroom | Mode | Rule | Biomes |')
    L.append('|---|---|---|---|---|')
    for key, crop in cfg['crops'].items():
        if crop.get('_mushroom'):
            L.append(f"| {key.split(':', 1)[1]} | {crop.get('label', key)} | {crop.get('mode', 'surface')} | {rule_text(crop)} | "
                     f"{biome_text(resolve_crop(traits, crop))} |")
    L.append('')

    L.append('## 4. Biomes per feature\n')
    by_feature = {}
    for r in rows:
        by_feature.setdefault(r['feature'], r)
    for fid in sorted(by_feature, key=lambda f: (by_feature[f]['crop'].lower(), f)):
        r = by_feature[fid]
        crops = ', '.join(sorted({x['crop'] for x in rows if x['feature'] == fid}))
        L.append(f'### `{fid}` ({crops})\n')
        L.append(f"- Rate {fmt_rate(r['rate'])}, height: {r['where']}" + (f"; ground: {r['ground']}" if r['ground'] else ''))
        if r['notes']:
            L.append(f"- {r['notes']}")
        L.append(f"- Added by: {', '.join(r['how'])}")
        bl = []
        for b in r['biomes']:
            d, layer, pct = area.get(b, (dim_of(b), '?', 0.0))
            bl.append(f'{short(b)} ({pct:.2f}%)' if layer == 'surface' and pct else f'{short(b)} (not sampled)' if layer == 'surface' else f'{short(b)} ({layer})')
        L.append(f"- {len(bl)} biomes: " + ', '.join(bl) + '\n')

    L.append('## 5. Crops per biome\n')
    L.append('Every overworld land biome, with its traits from `biome_traits.json`, plus other dimensions\' biomes that have a crop.\n')
    L.append('| Biome | Area | Traits | Crops (feature rate) |')
    L.append('|---|---|---|---|')
    per_biome = defaultdict(list)
    for r in rows:
        for b in r['biomes']:
            per_biome[b].append(f"{r['crop']} ({fmt_rate(r['rate'])})")
    for b in sorted(set(per_biome) | set(checks['land']), key=lambda b: (dim_of(b), b)):
        d, layer, pct = area.get(b, (dim_of(b), '?', 0.0))
        a = f'{short(d)} {pct:.2f}%' if layer == 'surface' else f'{short(d)} {layer}'
        tr = ' '.join(sorted(traits.get(b, ()))) or '-'
        L.append(f"| {short(b)} | {a} | {tr} | {', '.join(sorted(set(per_biome[b]))) or '**none**'} |")
    L.append('')

    L.append('## 6. Coverage checks\n')
    L.append(f"- Classified overworld land biomes: {len(checks['land'])}. With no wild crop at all: "
             + (', '.join(short(b) for b in checks['bare']) or 'none') + '.')
    if checks['problems']:
        L.append('- Config problems:')
        L.extend(f'  - {p}' for p in checks['problems'])
    else:
        L.append('- Config problems: none (every overworld biome classified, every rule token, biome, feature and modifier id known).')
    L.append('')
    if dropped:
        L.append('## 7. Biomes left out\n')
        L.append('These biomes are in the tags or temperature windows that crop features use, but no dimension\'s biome '
                 'source contains them (empty `dimensions` in `biome_data.json`), so they never generate: '
                 + ', '.join(short(b) for b in dropped) + '.\n')
    OUT_MD.write_text('\n'.join(L))


if __name__ == '__main__':
    main()
