"""The Display Delight jar in mods/ and how its blocks link to foods (shared by the DD generators).

The pack runs its port of Display Delight 1.7.0 (backportedDisplay-*.jar, development/backportedDisplay); a 1.2.0
jar (displaydelight-*.jar) is still read if that is what mods/ has.

  * 1.2.0 links a display block to the food its loot table drops.
  * 1.7.0 and the port link by block name (BlockAssociations.initialize): an optional prefix names the food's mod,
    then plated_ / small_plated_, then the food's path. No prefix: Farmer's Delight, or vanilla for five foods.
    The port ships no loot tables for food blocks and only the blockstates of the blocks it registers.
"""
import re
import zipfile

import lang_index

JAR_GLOBS = ('backportedDisplay-*.jar', 'displaydelight-*.jar')
PLATES = ('food_plate', 'small_food_plate')
PORT_MARKER = 'com/jkvin114/displaydelight/init/DisplayFoods.class'
# Block id prefix -> mod id of the food (1.7.0's COMPAT_NAMESPACES; md_ is the pack's miners_delight port)
PREFIX_NAMESPACES = {
    'cd_': 'corn_delight', 'ed_': 'expandeddelight', 'df_': 'delightful', 'pd_': 'pineapple_delight',
    'od_': 'oceansdelight', 'ad_': 'alexsdelight', 'ctd_': 'culturaldelights', 'lm_': 'largemeals',
    'fd_': 'festive_delight', 'bnc_': 'brewinandchewin', 'acd_': 'aquaculturedelight', 'erd_': 'endersdelight',
    'edd_': 'ends_delight', 'mnd_': 'mynethersdelight', 'nd_': 'nethersdelight', 'crd_': 'crabbersdelight',
    'abd_': 'abnormals_delight', 'fr_': 'farmersrespite', 'md_': 'miners_delight',
}
VANILLA_FOODS = {'mushroom_stew', 'rabbit_stew', 'beetroot_soup', 'pumpkin_pie', 'cookie'}
# 1.2.0 class constants that list its registered blocks (its jar also ships blockstates and loot tables for blocks
# it no longer registers: hamburger, bowl, dog_food...)
LEGACY_BLOCK_CLASSES = ('DisplayBlocks', 'PlatedBlocks', 'SmallPlatedBlocks')


def jar_path():
    for pattern in JAR_GLOBS:
        jars = sorted(lang_index.MODS.glob(pattern))
        if jars:
            return jars[-1]
    raise SystemExit('no %s in mods/' % ' or '.join(JAR_GLOBS))


def is_port(z):
    return PORT_MARKER in z.namelist()


def split_prefix(block):
    """'md_plated_takoyaki' -> ('md_', 'plated_takoyaki'); no known prefix -> ('', block)."""
    for prefix in PREFIX_NAMESPACES:
        if block.startswith(prefix):
            return prefix, block[len(prefix):]
    return '', block


def food_for(block):
    """The food item a 1.7.0 display block links to, by its name."""
    prefix, rest = split_prefix(block)
    for kind in ('small_plated_', 'plated_'):
        if rest.startswith(kind):
            rest = rest[len(kind):]
            break
    ns = PREFIX_NAMESPACES.get(prefix) or ('minecraft' if rest in VANILLA_FOODS else 'farmersdelight')
    return '%s:%s' % (ns, rest)


def registered_blocks(z):
    """Ids of the food display blocks the jar registers (plates left out)."""
    names = z.namelist()
    if is_port(z):
        found = (re.match(r'assets/displaydelight/blockstates/([a-z0-9_-]+)\.json$', n) for n in names)
        return {m.group(1) for m in found if m} - set(PLATES)
    out = set()
    for cls in LEGACY_BLOCK_CLASSES:
        raw = z.read('com/jkvin114/displaydelight/init/%s.class' % cls)
        out.update(s.decode() for s in re.findall(rb'[a-z][a-z0-9_]{2,}', raw))
    blockstates = {m.group(1) for m in (re.match(r'assets/displaydelight/blockstates/([a-z0-9_]+)\.json$', n)
                                        for n in names) if m}
    return (out & blockstates) - set(PLATES)


def links(z):
    """{block id: food item id} for every registered food display block."""
    blocks = registered_blocks(z)
    if is_port(z):
        return {b: food_for(b) for b in blocks}
    out = {}
    for b in blocks:
        name = 'data/displaydelight/loot_tables/blocks/%s.json' % b
        if name in z.namelist():
            drops = re.findall(r'"name"\s*:\s*"([^"]+)"', z.read(name).decode())
            food = [d for d in drops if d not in ('displaydelight:%s' % p for p in PLATES)]
            if food:
                out[b] = food[0]
    return out


def open_jar():
    return zipfile.ZipFile(jar_path())
