#!/usr/bin/env python3
"""Item-physics family audit: put every item in the pack in one material family and report, by mod, what
each item does as a dropped item today against what its family says it should do.

    python3 tools/item_physics_audit.py            # writes the report and the family table
    python3 tools/item_physics_audit.py --check    # exit 1 when any item is unclassified (for /cegen warnExit)

Reads (kubejs/exported/, refresh with /dumpall):
  items_by_mod/*.json          every item id, by mod
  tags/item_tags_by_item.json  each item's tags
  tags/item_tags.json          resolved members of the four physics tags
  physics/item_physics.json    /dumpphysics: block material, sound, fire resistance, burn time, live outcome
                               (optional; without it the audit uses tags, gear and names only, and the
                               lava-survivor list misses items that are fire resistant by code)
  gear/tools.json, gear/armor.json   fire resistance and tier for gear (fallback when physics/ is missing)

Writes:
  feature_writeup/item_physics_audit.md                       the report (design: item_physics.md section 9)
  kubejs/generation_scripts/physics/item_physics_families.json  item -> family, rung, current and family behaviour

Every item resolves to exactly one family by the first rung that matches (see RUNGS). An item reaching only
the mod-default rung, or none ('unclassified'), is listed so nothing new slips through when a mod is added.
"""
import argparse
import collections
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EXP = os.path.join(ROOT, 'kubejs', 'exported')
OUT_MD = os.path.join(ROOT, 'feature_writeup', 'item_physics_audit.md')
OUT_JSON = os.path.join(ROOT, 'kubejs', 'generation_scripts', 'physics', 'item_physics_families.json')
P = 'creativeengineering:physics/'

# --------------------------------------------------------------------------------------------------------
# Families. water: skim (rests on the surface), float (dips, rises), sink. fire: burn (fire and lava destroy),
# fireproof (survives fire, lava destroys), lavaproof (survives both; floats on lava under the density tweak).
# Order matters only for the doc; matching order is RUNGS below.
# --------------------------------------------------------------------------------------------------------
FAMILIES = collections.OrderedDict([
    ('explosive',  dict(water='sink',  fire='burn',      note='burn transform: explodes')),
    ('refractory', dict(water='sink',  fire='lavaproof', note='stone, deepslate, nether and end stone, obsidian, diamond, netherite, fire-resistant items')),
    ('metal',      dict(water='sink',  fire='fireproof', note='ingots, plates, metal blocks, metal parts and tools')),
    ('machine',    dict(water='sink',  fire='fireproof', note='composite devices: circuits, storage, computers, electronics')),
    ('masonry',    dict(water='sink',  fire='fireproof', note='fired clay, bricks, terracotta, concrete, tiles, plaster')),
    ('earth',      dict(water='sink',  fire='fireproof', note='sand, gravel, dirt, mud, clay, loose mineral')),
    ('crystal',    dict(water='sink',  fire='fireproof', note='gems and crystals other than diamond')),
    ('glass',      dict(water='sink',  fire='burn',      note='burn transform: dirty glass')),
    ('container',  dict(water='sink',  fire='fireproof', note='bottles, potions, buckets, jars, canned food')),
    ('powder',     dict(water='sink',  fire='fireproof', note='dense powders: redstone, glowstone, metal dusts')),
    ('ice',        dict(water='float', fire='fireproof', note='ice and snow; ice floats in water')),
    ('wood',       dict(water='float', fire='burn',      note='burn transform: ash'),),
    ('nether_wood', dict(water='float', fire='fireproof', note='crimson and warped wood (non-flammable)')),
    ('produce',    dict(water='float', fire='burn',      note='crops, fruit, vegetables, mushrooms, kelp, gourds')),
    ('food',       dict(water='float', fire='burn',      note='prepared food not in a container')),
    ('organic',    dict(water='float', fire='burn',      note='mob drops: bone, flesh, hide, slime, shell, horn')),
    ('foliage',    dict(water='skim',  fire='burn',      note='leaves, flowers, petals, seeds, grass, saplings, vines')),
    ('fibre',      dict(water='skim',  fire='burn',      note='wool, cloth, string, paper, books, feathers, hats')),
    ('light_powder', dict(water='skim', fire='burn',     note='flour, sugar, bone meal, sawdust, pollen')),
    ('technical',  dict(water='sink',  fire='burn',      note='spawn eggs, debug and creative-only items (not audited)')),
    ('unclassified', dict(water='sink', fire='burn',     note='no rung matched: needs a decision')),
])

# ---- rung 1: tags (first family whose tag the item carries) --------------------------------------------
TAG_RULES = [
    ('explosive',  ['forge:gunpowder', 'forge:dusts/gunpowder', 'forge:explosives']),
    ('refractory', ['forge:obsidian', 'forge:end_stones', 'forge:netherrack', 'forge:gems/diamond',
                    'forge:storage_blocks/diamond', 'forge:ingots/netherite', 'forge:storage_blocks/netherite',
                    'forge:nuggets/netherite', 'minecraft:shulker_boxes', 'minecraft:soul_fire_base_blocks',
                    'forge:ores', 'forge:stone', 'forge:cobblestone', 'create:stone_types', 'forge:ores_in_ground/stone',
                    'forge:ores_in_ground/deepslate', 'forge:ores_in_ground/netherrack']),
    ('nether_wood', ['minecraft:non_flammable_wood']),
    ('wood',       ['minecraft:planks', 'minecraft:logs', 'minecraft:wooden_slabs', 'minecraft:wooden_stairs',
                    'minecraft:wooden_fences', 'minecraft:wooden_doors', 'minecraft:wooden_trapdoors',
                    'minecraft:wooden_buttons', 'minecraft:wooden_pressure_plates', 'forge:fence_gates/wooden',
                    'minecraft:signs', 'minecraft:boats', 'forge:chests/wooden', 'forge:rods/wooden',
                    'forge:barrels/wooden', 'farmersdelight:cabinets', 'forge:treated_wood', 'create:scrap_wood_small']),
    ('glass',      ['forge:glass', 'forge:glass_panes', 'quark:shards']),
    ('masonry',    ['minecraft:terracotta', 'forge:concrete', 'forge:bricks']),
    ('earth',      ['forge:sand', 'forge:gravel', 'minecraft:dirt', 'forge:concrete_powders', 'forge:clay']),
    ('ice',        ['minecraft:ice', 'minecraft:snow']),
    ('container',  ['forge:buckets', 'forge:bottles', 'creativeengineering:food_type/canned',
                    'creativeengineering:food_type/drink']),
    ('metal',      ['forge:ingots', 'forge:nuggets', 'forge:plates', 'forge:gears', 'forge:wires', 'forge:raw_materials',
                    'forge:storage_blocks/raw', 'minecraft:rails', 'minecraft:anvil', 'forge:sheetmetals',
                    'forge:rods/all_metal', 'forge:scaffoldings', 'create:casing']),
    ('crystal',    ['forge:gems', 'forge:storage_blocks/amethyst', 'forge:storage_blocks/emerald']),
    ('powder',     ['forge:dusts']),
    ('metal',      ['minecraft:music_discs']),
    ('light_powder', ['forge:dyes', 'forge:flour', 'forge:sugar', 'forge:dusts/sugar', 'forge:salt']),
    ('foliage',    ['minecraft:leaves', 'minecraft:saplings', 'minecraft:flowers', 'forge:seeds', 'minecraft:small_flowers',
                    'minecraft:tall_flowers']),
    ('fibre',      ['minecraft:wool', 'forge:wool', 'minecraft:wool_carpets', 'minecraft:beds', 'forge:string',
                    'minecraft:banners', 'forge:feathers', 'forge:paper', 'forge:books', 'minecraft:candles',
                    'forge:fabric_hemp', 'forge:cloth']),
    ('organic',    ['forge:leather', 'forge:bones', 'forge:slimeballs', 'forge:eggs', 'minecraft:fishes',
                    'forge:raw_fishes', 'forge:rope', 'forge:hides']),
    ('produce',    ['forge:crops', 'forge:fruits', 'forge:vegetables', 'forge:mushrooms', 'forge:berries', 'forge:grain',
                    'forge:nuts']),
    ('food',       ['creativeengineering:food_type']),
    ('technical',  ['forge:spawn_eggs']),
]
# tag prefixes: an item tag equal to or under one of these matches (forge:ingots/iron matches forge:ingots)

# ---- rung 3: block Material (from /dumpphysics) ---------------------------------------------------------
MATERIAL_RULES = {
    'EXPLOSIVE': 'explosive', 'STONE': 'refractory', 'METAL': 'metal', 'HEAVY_METAL': 'metal', 'PISTON': 'metal',
    'GLASS': 'glass', 'BUILDABLE_GLASS': 'glass', 'AMETHYST': 'crystal', 'SHULKER_SHELL': 'refractory',
    'SAND': 'earth', 'DIRT': 'earth', 'CLAY': 'earth', 'GRASS': 'earth',
    'ICE': 'ice', 'ICE_SOLID': 'ice', 'SNOW': 'ice', 'TOP_SNOW': 'ice', 'POWDER_SNOW': 'ice',
    'WOOD': 'wood', 'BAMBOO': 'wood', 'NETHER_WOOD': 'nether_wood',
    'WOOL': 'fibre', 'CLOTH_DECORATION': 'fibre', 'WEB': 'fibre',
    'PLANT': 'foliage', 'REPLACEABLE_PLANT': 'foliage', 'REPLACEABLE_FIREPROOF_PLANT': 'foliage',
    'WATER_PLANT': 'foliage', 'REPLACEABLE_WATER_PLANT': 'foliage', 'LEAVES': 'foliage', 'MOSS': 'foliage',
    'BAMBOO_SAPLING': 'foliage', 'VEGETABLE': 'produce', 'CACTUS': 'produce', 'SPONGE': 'organic', 'EGG': 'organic',
    'FROGSPAWN': 'organic', 'FROGLIGHT': 'organic', 'CAKE': 'food', 'SCULK': 'organic',
}
# SoundType refines a STONE / DECORATION / modded material (machines on stone material, metal-sounding blocks)
SOUND_RULES = {
    'METAL': 'metal', 'COPPER': 'metal', 'ANVIL': 'metal', 'CHAIN': 'metal', 'LANTERN': 'metal',
    'NETHERITE_BLOCK': 'refractory', 'ANCIENT_DEBRIS': 'refractory', 'LODESTONE': 'refractory',
    'GLASS': 'glass', 'WOOD': 'wood', 'LADDER': 'wood', 'SCAFFOLDING': 'wood', 'BAMBOO': 'wood',
    'WOOL': 'fibre', 'CANDLE': 'fibre', 'GRASS': 'foliage', 'CROP': 'produce', 'SAND': 'earth', 'GRAVEL': 'earth',
    'MUD': 'earth', 'ROOTED_DIRT': 'earth', 'SOUL_SAND': 'earth', 'SOUL_SOIL': 'earth', 'SNOW': 'ice',
    'AMETHYST': 'crystal', 'MUD_BRICKS': 'masonry', 'PACKED_MUD': 'masonry',
}

# ---- rung 4: gear (tier / armor material names) -----------------------------------------------------------
GEAR_RULES = [
    (r'netherite|dragon|echo|felsic|obsidian', 'refractory'),
    (r'diamond', 'refractory'),
    (r'wood|leather|turtle|bamboo|cloth|wool|straw', 'wood'),
    (r'iron|gold|copper|steel|chain|bronze|tin|silver|lead|nickel|alumin|brass|zinc|electrum|constantan|rose_gold|'
     r'cast_iron|manasteel|terrasteel|elementium|uranium|invar|metal|osmium', 'metal'),
    (r'stone|flint|andesite|granite|diorite|blackstone|deepslate', 'refractory'),
    (r'emerald|amethyst|quartz|ruby|sapphire|crystal|certus|fluix|gem', 'crystal'),
]

# ---- rung 5: id path tokens -----------------------------------------------------------------------------
# The id path is split on '_' and '/' into tokens. 'words' match a whole token (a trailing s / es is allowed),
# 'parts' match inside a token (compounds such as cobblestone, sandstone, flowerpot). First family wins.
NAME_RULES = [
    ('technical',  dict(words='spawn debug creative test barrier jigsaw command structure',
                        parts='spawn_egg')),
    ('explosive',  dict(words='tnt dynamite gunpowder explosive bomb grenade nitro', parts='')),
    ('container',  dict(words='bottle bottled bucket jar flask vial potion can canned canister thermos kettle '
                              'teapot mug cup tankard', parts='')),
    ('refractory', dict(words='netherite obsidian blackstone basalt deepslate netherrack magma blaze diamond quartz '
                              'debris dragon echo felsic scoria scorchia crimsite asurine veridium ochrum limestone '
                              'marble slate andesite granite diorite calcite tuff dripstone stone rock shingle '
                              'cobble gabbro myonite jasper', parts='stone rock')),
    ('nether_wood', dict(words='crimson warped', parts='')),
    ('glass',      dict(words='glass window lens pane', parts='glass')),
    ('masonry',    dict(words='brick terracotta concrete tile ceramic porcelain plaster stucco pottery pot urn kiln '
                              'adobe mosaic shingles rune pavement azulejo well', parts='brick')),
    ('earth',      dict(words='sand gravel dirt mud soil loam silt peat compost ash clay cinder soot salt', parts='')),
    ('ice',        dict(words='ice snow frost frozen', parts='')),
    ('crystal',    dict(words='emerald amethyst crystal ruby sapphire topaz jade opal gem certus fluix prismarine '
                              'geode celestite corundum myalite prism', parts='')),
    ('light_powder', dict(words='flour sugar sawdust pollen pulp starch cocoa spice yeast', parts='')),
    ('powder',     dict(words='dust powder redstone glowstone grit crushed', parts='')),
    ('metal',      dict(words='iron steel copper brass zinc gold golden alumin aluminum aluminium lead silver nickel '
                              'tin bronze electrum constantan uranium invar cobalt osmium platinum metal locometal '
                              'ingot nugget plate gear cog cogwheel shaft casing pipe tank chain rail wire coil bolt '
                              'screw rivet valve pump gauge coin anvil cannon gun bullet ammo rifle pistol '
                              'cartridge girder scaffolding bars grate vent mesh catwalk lamp lantern bell sheetmetal '
                              'track bogey smithing template trim', parts='metal steel')),
    ('machine',    dict(words='circuit processor chip controller terminal monitor computer drive cell storage '
                              'interface panel cable bus module upgrade card sensor transmitter receiver battery '
                              'capacitor generator engine motor turbine machine assembler press crusher furnace '
                              'turret keycard camera detector reinforced alarm scanner drone robot printer '
                              'filter network', parts='')),
    ('wood',       dict(words='oak spruce birch jungle acacia mangrove cherry aspen maple willow poise driftwood '
                              'azalea plank planks log wood wooden stick barrel crate chest cabinet shelf drawer '
                              'table chair bench stool desk counter cupboard wardrobe fence trapdoor door sign boat '
                              'raft oar bow arrow bowl basket frame easel cart wheel bamboo cork bark timber beam '
                              'rafter lattice trellis ladder crib dresser nightstand sofa couch furniture bookshelf '
                              'dreamwood livingwood blinds campfire framed', parts='wood plank')),
    ('foliage',    dict(words='leaf leaves petal flower blossom bloom sapling seed grass fern vine moss lichen reed '
                              'rush lily tulip rose daisy orchid allium poppy clover bush shrub hedge ivy weed sprout '
                              'roots herb hay straw thatch cattail petals tobacco cigar cigarette', parts='flower')),
    ('produce',    dict(words='apple berry berries fruit melon pumpkin carrot potato beetroot cabbage tomato onion '
                              'corn rice wheat grape banana mango lemon peach pear plum olive bean pea lettuce '
                              'cucumber squash gourd mushroom fungus truffle kelp seaweed cactus cane nut chestnut '
                              'walnut coconut vanilla garlic ginger chili eggplant zucchini leek radish turnip yam '
                              'kale spinach celery broccoli cauliflower asparagus artichoke hop hops barley oat rye',
                         parts='berry')),
    ('food',       dict(words='bread cake pie cookie stew soup salad sandwich burger pizza pasta noodle roast '
                              'cooked fried baked grilled smoked dried jerky sausage ham bacon steak cutlet fillet '
                              'slice pudding tart muffin donut candy chocolate cheese butter cream jam jelly honey '
                              'syrup dumpling sushi taco wrap bun toast waffle pancake omelet meal dish feast '
                              'platter skewer kebab dough batter crust biscuit cracker popsicle', parts='')),
    ('organic',    dict(words='bone flesh meat beef pork porkchop chicken mutton venison duck goose fish cod salmon '
                              'shrimp crab squid tentacle hide leather pelt fur feather slime membrane scale scute '
                              'shell claw tooth teeth fang horn antler egg eye heart gland sac tongue tail wing beak '
                              'hoof wax honeycomb silk web coral', parts='')),
    ('fibre',      dict(words='wool carpet cloth fabric canvas cotton linen string rope thread twine hemp flax jute '
                              'sail banner flag curtain rug blanket quilt pillow cushion hammock tapestry bedspread '
                              'sheet hat cap hood scarf bandana paper book scroll map letter note label ticket '
                              'envelope parchment candle wick tag ribbon bed present sack bag', parts='')),
]


def _name_matcher(spec):
    words = spec['words'].split()
    parts = spec['parts'].split()
    wrx = re.compile(r'^(?:%s)(?:s|es)?$' % '|'.join(map(re.escape, words))) if words else None
    return wrx, parts


NAME_RE = [(fam, _name_matcher(spec)) for fam, spec in NAME_RULES]


# ---- rung 6: mod defaults (the family an item of this mod most likely is when nothing above matched) ------
MOD_DEFAULTS = {
    'ae2': 'machine', 'refinedstorage': 'machine', 'pneumaticcraft': 'machine', 'computercraft': 'machine',
    'advancedperipherals': 'machine', 'projectred_core': 'machine', 'projectred_transmission': 'machine',
    'projectred_integration': 'machine', 'projectred_fabrication': 'machine', 'projectred_illumination': 'machine',
    'securitycraft': 'machine', 'appbot': 'machine', 'createappliedkinetics': 'machine', 'cccbridge': 'machine',
    'controlengineering': 'machine', 'cloudstorage': 'machine', 'prettypipes': 'machine', 'itemfilters': 'machine',
    'physics_gun_mod': 'machine', 'ricksportalgun': 'machine', 'appliedcooking': 'machine',
    'create': 'metal', 'create_dd': 'metal', 'createaddition': 'metal', 'createdeco': 'metal', 'design_decor': 'metal',
    'railways': 'metal', 'vintageimprovements': 'metal', 'immersiveengineering': 'metal', 'immersivepetroleum': 'metal',
    'immersiveposts': 'metal', 'more_immersive_wires': 'metal', 'oldguns': 'metal', 'everythingcopper': 'metal',
    'metalfences': 'metal', 'littlelogistics': 'metal', 'create_things_and_misc': 'metal', 'create_factory': 'metal',
    'create_power_loader': 'metal', 'create_enchantment_industry': 'metal', 'extendedgears': 'metal',
    'bellsandwhistles': 'metal', 'creategoggles': 'metal', 'compressedcreativity': 'metal', 'dustrial_decor': 'metal',
    'lightmanscurrency': 'metal', 'blocky_siege': 'metal', 'immersive_armors': 'metal', 'iron_fishing_rods': 'metal',
    'create_tweaked_controllers': 'metal', 'create_mechanical_extruder': 'metal', 'littlecontraptions': 'metal',
    'enderstorage': 'refractory', 'dragonloot': 'refractory', 'felsic_gear': 'refractory',
    'simplehats': 'fibre', 'comforts': 'fibre', 'fairylights': 'fibre', 'bedspreads': 'fibre', 'scholar': 'fibre',
    'wardrobe': 'wood', 'fantasyfurniture': 'wood', 'handcrafted': 'wood', 'another_furniture': 'wood',
    'storagedrawers': 'wood', 'everycomp': 'wood', 'dramaticdoors': 'wood', 'boatload': 'wood', 'astikorcarts': 'wood',
    'decorative_blocks': 'wood', 'storagedelight': 'wood', 'interiors': 'wood', 'domum_ornamentum': 'wood',
    'architects_palette': 'refractory', 'davebuildingmod': 'refractory', 'redeco': 'refractory',
    'clayworks': 'masonry', 'mosaic_masonry': 'masonry', 'chalk': 'earth',
    'farmersdelight': 'food', 'fruitsdelight': 'food', 'vintagedelight': 'food', 'delightful': 'food',
    'neapolitan': 'food', 'create_confectionery': 'food', 'veggiesdelight': 'food', 'miners_delight': 'food',
    'hostiledelight': 'food', 'ends_delight': 'food', 'rusticdelight': 'food', 'farmersrespite': 'food',
    'abnormals_delight': 'food', 'oceanic_delight': 'food', 'goldenfood': 'food', 'canned_goods': 'container',
    'brewinandchewin': 'food', 'cookingforblockheads': 'metal', 'moredelight': 'food', 'nethersdelight': 'food',
    'fungidelight': 'food', 'create_central_kitchen': 'food', 'endersdelight': 'food', 'corn_delight': 'food',
    'alexsdelight': 'food', 'oceansdelight': 'food', 'quarkdelight': 'food', 'berry_good': 'food',
    'solapplepie': 'food', 'refinedcooking': 'food', 'framedblocks': 'wood', 'copycats': 'metal', 'the_dirty_stuff': 'foliage', 'vinery': 'food', 'tobacconistmod': 'foliage',
    'colored_water': 'container', 'ppfluids': 'container', 'splash_milk': 'container', 'potionbundles': 'container',
    'floralench': 'foliage', 'bushierflowers': 'foliage', 'buzzier_bees': 'organic', 'alexsmobs': 'organic',
    'alexsabnormalities': 'organic', 'untitledduckmod': 'organic', 'upgrade_aquatic': 'organic',
    'exposure': 'machine', 'exposure_expanded': 'machine', 'exposure_polaroid': 'fibre', 'exposure_extras': 'fibre',
    'geode': 'crystal', 'create_crystal_clear': 'glass', 'glow_ink_plus': 'organic',
}

GEAR_FILES = ('gear/tools.json', 'gear/armor.json')


def load(rel, default=None):
    path = os.path.join(EXP, rel)
    if not os.path.exists(path):
        return default
    with open(path) as fh:
        return json.load(fh)


def tag_family(tags):
    for fam, prefixes in TAG_RULES:
        for t in tags:
            for p in prefixes:
                if t == p or t.startswith(p + '/'):
                    return fam, '#' + t
    return None


def name_family(item_id):
    tokens = [t for t in re.split(r'[_/]', item_id.split(':', 1)[1]) if t]
    for fam, (wrx, parts) in NAME_RE:
        for t in tokens:
            if wrx and wrx.match(t):
                return fam, t
        for t in tokens:
            for p in parts:
                if p in t:
                    return fam, t
    return None


def current_behaviour(item_id, phys_tags, dump_row):
    """What a dropped stack does today: (water, fire, lava survive) from the live dump, else from the tags."""
    if dump_row is not None and 'survivesLava' in dump_row:
        water = 'float' if dump_row.get('floats') else 'sink'
        if dump_row['survivesLava']:
            fire = 'lavaproof'
        elif dump_row.get('survivesFire'):
            fire = 'fireproof'
        else:
            fire = 'burn'
        return water, fire
    water = 'sink' if item_id in phys_tags['sinks'] else ('float' if item_id in phys_tags['floats'] else 'sink')
    fire = 'lavaproof' if item_id in phys_tags['lavaproof'] else ('fireproof' if item_id in phys_tags['fireproof'] else 'burn')
    return water, fire


def classify(item_id, tags, dump_row, gear_row):
    """(family, rung, evidence). Rungs: 1 tag, 2 fire-resistant flag, 3 block material/sound, 4 gear tier,
    5 name token, 6 mod default, 7 none."""
    mod = item_id.split(':', 1)[0]
    hit = tag_family(tags)
    if hit:
        return hit[0], 'tag', hit[1]
    fire_res = (dump_row or {}).get('fireResistant')
    if fire_res is None and gear_row is not None:
        fire_res = gear_row.get('fireResistant')
    if fire_res:
        return 'refractory', 'fire_resistant', 'Item#isFireResistant'
    if dump_row and dump_row.get('blockItem'):
        mat, snd = dump_row.get('material'), dump_row.get('sound')
        fam = SOUND_RULES.get(snd) if mat in (None, 'STONE', 'DECORATION', 'modded') else None
        if fam is None:
            fam = MATERIAL_RULES.get(mat)
        if fam:
            # a STONE-material block named like a brick or a machine is masonry / machine, not natural stone
            if fam == 'refractory' and mat == 'STONE':
                nh = name_family(item_id)
                if nh and nh[0] in ('masonry', 'machine', 'metal', 'crystal', 'glass'):
                    return nh[0], 'material+name', 'STONE + ' + nh[1]
            return fam, 'material', '%s/%s' % (mat, snd)
    gear_name = ((gear_row or {}).get('material') or '') + ' ' + str((dump_row or {}).get('tier') or '') + ' ' + \
        str((dump_row or {}).get('armorMaterial') or '')
    if gear_name.strip():
        for rx, fam in GEAR_RULES:
            if re.search(rx, gear_name.lower()):
                return fam, 'gear', gear_name.strip()
    if (dump_row or {}).get('edible'):
        nh = name_family(item_id)
        if nh and nh[0] in ('container', 'produce', 'organic'):
            return nh[0], 'edible+name', nh[1]
        return 'food', 'edible', 'Item#isEdible'
    nh = name_family(item_id)
    if nh:
        return nh[0], 'name', nh[1]
    if mod in MOD_DEFAULTS:
        return MOD_DEFAULTS[mod], 'mod_default', mod
    if (dump_row or {}).get('burnTime'):
        return 'wood', 'fuel', 'burn time %s' % dump_row['burnTime']
    return 'unclassified', 'none', ''


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--check', action='store_true', help='exit 1 when any item is unclassified')
    ap.add_argument('--list-limit', type=int, default=25, help='items listed per mod in the gap tables')
    args = ap.parse_args()

    items = {}
    for f in sorted(glob.glob(os.path.join(EXP, 'items_by_mod', '*.json'))):
        for iid in json.load(open(f))['items']:
            items[iid] = os.path.basename(f)[:-5]
    if not items:
        sys.exit('no kubejs/exported/items_by_mod/ - run /dumpitemsbymod (or /dumpall)')
    by_item_nested = load('tags/item_tags_by_item.json', {})
    tags_of = {}
    for mod, rows in by_item_nested.items():
        tags_of.update(rows)
    all_tags = load('tags/item_tags.json', {})
    phys_tags = {k: set(all_tags.get(P + k, [])) for k in ('floats', 'sinks', 'fireproof', 'lavaproof')}
    dump = load('physics/item_physics.json')
    dump_rows = {r['id']: r for r in (dump or {}).get('items', [])}
    gear = {}
    for rel in GEAR_FILES:
        for r in (load(rel, {}) or {}).get('items', []):
            gear[r['id']] = r

    results = {}
    for iid in sorted(items):
        tags = tags_of.get(iid, [])
        fam, rung, ev = classify(iid, tags, dump_rows.get(iid), gear.get(iid))
        cur_w, cur_f = current_behaviour(iid, phys_tags, dump_rows.get(iid))
        spec = FAMILIES[fam]
        manual = sorted(k for k, s in phys_tags.items() if iid in s)
        results[iid] = dict(mod=items[iid], family=fam, rung=rung, evidence=ev, manual=manual,
                            now=dict(water=cur_w, fire=cur_f),
                            family_behaviour=dict(water=spec['water'], fire=spec['fire']))

    # ---- lava survivors today ----
    if dump_rows:
        survivors = {i: dump_rows[i].get('why', []) for i in results if dump_rows.get(i, {}).get('survivesLava')}
        surv_src = '/dumpphysics (live: tags, rules, fire-resistant items, ItemPhysic undestroyable list)'
    else:
        survivors = {i: ['split_lavaproof'] for i in phys_tags['lavaproof'] if i in results}
        for i, g in gear.items():
            if g.get('fireResistant') and i in results:
                survivors.setdefault(i, []).append('fire_resistant_item')
        for i in ('minecraft:nether_star', 'minecraft:bedrock', 'minecraft:obsidian'):
            if i in results:
                survivors.setdefault(i, []).append('itemphysic_undestroyable')
        surv_src = ('tags + gear dump only (no physics/item_physics.json yet: run /dumpphysics for fire-resistant '
                    'non-gear items such as netherite scrap, ancient debris and modded fire-resistant items)')

    write_json(results, survivors, dump is not None)
    write_md(results, survivors, surv_src, dump is not None, args.list_limit)
    n_unc = sum(1 for r in results.values() if r['family'] == 'unclassified')
    n_def = sum(1 for r in results.values() if r['rung'] == 'mod_default')
    print('items %d | unclassified %d | mod-default only %d | lava survivors %d | dump %s' % (
        len(results), n_unc, n_def, len(survivors), 'yes' if dump else 'no (run /dumpphysics)'))
    print('wrote', os.path.relpath(OUT_MD, ROOT), 'and', os.path.relpath(OUT_JSON, ROOT))
    if args.check and n_unc:
        return 1
    return 0


def write_json(results, survivors, have_dump):
    """One line per item: family, rung, evidence, today's water/fire, the physics tags it is in now.
    The family's own behaviour is in 'families'."""
    os.makedirs(os.path.dirname(OUT_JSON), exist_ok=True)
    head = {'_note': 'Written by tools/item_physics_audit.py. items: id -> [family, rung, evidence, now water/fire, '
                     'physics tags now]. Family behaviour: families.',
            'physicsDump': have_dump,
            'families': {k: v for k, v in FAMILIES.items()},
            'lavaSurvivors': {k: sorted(set(survivors[k])) for k in sorted(survivors)}}
    with open(OUT_JSON, 'w') as fh:
        body = json.dumps(head, indent=1)
        fh.write(body[:-2] + ',\n "items": {\n')
        ids = sorted(results)
        for n, iid in enumerate(ids):
            r = results[iid]
            row = [r['family'], r['rung'], r['evidence'], fmt_beh(r['now']), r['manual']]
            fh.write('  %s: %s%s\n' % (json.dumps(iid), json.dumps(row), ',' if n < len(ids) - 1 else ''))
        fh.write(' }\n}\n')


def fmt_beh(b):
    return '%s/%s' % (b['water'], b['fire'])


def differs(r):
    """Family behaviour is not today's. skim counts as float: the game has no skim yet (section 10)."""
    if r['family'] in ('technical', 'unclassified'):
        return False
    want = dict(r['family_behaviour'], water='float' if r['family_behaviour']['water'] == 'skim' else r['family_behaviour']['water'])
    return fmt_beh(r['now']) != fmt_beh(want)


def write_md(results, survivors, surv_src, have_dump, limit):
    mods = collections.defaultdict(list)
    for iid, r in results.items():
        mods[r['mod']].append((iid, r))
    L = []
    w = L.append
    w('# Item physics family audit')
    w('')
    w('Generated by `tools/item_physics_audit.py` (`/cegen run item_physics_audit`). Do not edit by hand.')
    w('Design and the family table: [item_physics.md](item_physics.md) section 9. Block material and fire')
    w('resistance come from `/dumpphysics`: %s.' % ('present' if have_dump else
      '**not dumped yet**, so block items are classified by tag and name only and the lava list is partial'))
    w('')
    w('Rungs, first match wins: `tag` (a vanilla/forge/pack tag) > `fire_resistant` (the item\'s own flag) >')
    w('`material` (block Material, refined by SoundType) > `gear` (tool tier / armor material) > `edible` > `name`')
    w('(a token in the id) > `mod_default` > `none` (unclassified). "Manual" = already in one of the four physics')
    w('tags. "Differs" = the family\'s behaviour is not what the item does now.')
    w('')
    total = len(results)
    rung_c = collections.Counter(r['rung'] for r in results.values())
    fam_c = collections.Counter(r['family'] for r in results.values())
    manual = sum(1 for r in results.values() if r['manual'])
    n_differs = sum(1 for r in results.values() if differs(r))
    w('## 1. Totals')
    w('')
    w('| Items | In a physics tag now | Differs from family | Mod default only | Unclassified |')
    w('|---|---|---|---|---|')
    w('| %d | %d | %d | %d | %d |' % (total, manual, n_differs, rung_c['mod_default'], fam_c['unclassified']))
    w('')
    w('| Rung | Items |')
    w('|---|---|')
    for k, v in rung_c.most_common():
        w('| %s | %d |' % (k, v))
    w('')
    w('## 2. Families')
    w('')
    w('| Family | Water | Fire | Items | Note |')
    w('|---|---|---|---|---|')
    for k, spec in FAMILIES.items():
        w('| %s | %s | %s | %d | %s |' % (k, spec['water'], spec['fire'], fam_c[k], spec['note']))
    w('')
    w('## 3. By mod')
    w('')
    w('Sorted by items needing attention (unclassified + mod default + differs).')
    w('')
    w('| Mod | Items | Manual | Unclassified | Mod default | Differs | Top families |')
    w('|---|---|---|---|---|---|---|')
    rows = []
    for mod, lst in mods.items():
        unc = sum(1 for _, r in lst if r['family'] == 'unclassified')
        dflt = sum(1 for _, r in lst if r['rung'] == 'mod_default')
        dif = sum(1 for _, r in lst if differs(r))
        man = sum(1 for _, r in lst if r['manual'])
        top = ', '.join('%s %d' % kv for kv in collections.Counter(r['family'] for _, r in lst).most_common(3))
        rows.append((unc + dflt + dif, mod, len(lst), man, unc, dflt, dif, top))
    rows.sort(key=lambda x: (-x[0], x[1]))
    for _, mod, n, man, unc, dflt, dif, top in rows:
        w('| %s | %d | %d | %d | %d | %d | %s |' % (mod, n, man, unc, dflt, dif, top))
    w('')
    no_default = sorted(m for m in mods if m not in MOD_DEFAULTS and m != 'minecraft')
    w('Mods with no mod default (an unmatched item from these goes to unclassified): %d.' % len(no_default))
    w('')
    w('## 4. Lava survivors today')
    w('')
    w('Source: %s.' % surv_src)
    w('')
    by_why = collections.defaultdict(list)
    for iid, why in survivors.items():
        by_why[' + '.join(sorted(set(why))) or 'unknown'].append(iid)
    w('| Reason | Items |')
    w('|---|---|')
    for k in sorted(by_why):
        w('| %s | %d |' % (k, len(by_why[k])))
    w('')
    surv_mods = collections.Counter(results[i]['mod'] for i in survivors)
    w('By mod: ' + ', '.join('%s %d' % kv for kv in surv_mods.most_common()) + '.')
    w('')
    outside = sorted(i for i in survivors if results[i]['family'] != 'refractory')
    w('Survivors whose family is not `refractory` (check each: an over-broad lavaproof entry, or a family rule to'
      ' fix): %d.' % len(outside))
    w('')
    for i in outside[:200]:
        w('- `%s` (%s, %s)' % (i, results[i]['family'], results[i]['rung']))
    if len(outside) > 200:
        w('- ... %d more in the JSON' % (len(outside) - 200))
    w('')
    w('## 5. Gaps by mod')
    w('')
    w('Unclassified and mod-default items, at most %d per mod (all of them in `%s`).' % (
        limit, os.path.relpath(OUT_JSON, ROOT)))
    w('')
    for _, mod, n, man, unc, dflt, dif, top in rows:
        gap = [(i, r) for i, r in mods[mod] if r['rung'] in ('none', 'mod_default')]
        if not gap:
            continue
        w('### %s (%d)' % (mod, len(gap)))
        w('')
        w(', '.join('`%s`%s' % (i.split(':', 1)[1], '' if r['rung'] == 'none' else ' (' + r['family'] + ')')
                    for i, r in gap[:limit]) + (' ...' if len(gap) > limit else ''))
        w('')
    w('## 6. Differs from family, by mod')
    w('')
    w('Items whose current behaviour (water/fire) is not their family\'s. Manual tag choices show here too: either')
    w('the family is wrong for the item or the tag is. At most %d per mod.' % limit)
    w('')
    for _, mod, n, man, unc, dflt, dif, top in rows:
        lst = [(i, r) for i, r in mods[mod] if differs(r)]
        if not lst:
            continue
        w('### %s (%d)' % (mod, len(lst)))
        w('')
        w('| Item | Family (rung) | Now | Family says |')
        w('|---|---|---|---|')
        for i, r in lst[:limit]:
            w('| `%s` | %s (%s) | %s | %s |' % (i.split(':', 1)[1], r['family'], r['rung'], fmt_beh(r['now']),
                                               fmt_beh(r['family_behaviour'])))
        if len(lst) > limit:
            w('| ... %d more | | | |' % (len(lst) - limit))
        w('')
    with open(OUT_MD, 'w') as fh:
        fh.write('\n'.join(L).rstrip() + '\n')


if __name__ == '__main__':
    sys.exit(main())
