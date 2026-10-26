#!/usr/bin/env python3
"""Generates the repackedStorage overlay: assets and data of the added Storage Delight blocks, the axe tag, en_us.json.

Reads the unpacked 25.12.09 jar (the pinned upstream) and 26.10.05 jar (art donor, MIT) from DecompiledMods/.
Run from the repo root: python3 development/repackedStorage/tools/gen_overlay.py
The registries (Java) are hand-written in src/main/java; the block list here must match them.
"""
import json, os, re, shutil

ROOT = os.getcwd()
OLD = os.path.join(ROOT, 'DecompiledMods/storagedelight-25.12.09-1.19-forge')
NEW = os.path.join(ROOT, 'DecompiledMods/storagedelight-26.10.05-26.3-neoforge')
OUT = os.path.join(ROOT, 'development/repackedStorage/src/main/resources')
A = 'assets/storagedelight'
D = 'data/storagedelight'

WOODS = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped']
STONES = {  # countertop key -> (slab ingredient, 1.19.2 top texture)
    'polished_blackstone': ('minecraft:polished_blackstone_slab', None),
    'polished_diorite': ('minecraft:polished_diorite_slab', None),
    'stone_bricks': ('minecraft:stone_brick_slab', None),
    'polished_tuff': ('quark:polished_tuff_slab', 'quark:block/polished_tuff'),
}
OLD_STONES = ['polished_deepslate', 'polished_andesite']
BAMBOO_SLAB, BAMBOO_TRAPDOOR = 'quark:bamboo_planks_slab', 'quark:bamboo_trapdoor'


def types(w):
    return [f'{w}_drawer', f'{w}_drawer_with_door', f'{w}_drawer_with_books', f'small_{w}_drawers',
            f'{w}_bookshelf_with_door', f'glass_{w}_cabinet', f'{w}_cabinet_with_glass_doors',
            f'{w}_single_door_cabinet']


def new_blocks():
    out = [f'{w}_cabinet_with_{s}_countertop' for w in WOODS for s in STONES]
    out += types('bamboo')
    out += [f'bamboo_cabinet_with_{s}_countertop' for s in OLD_STONES + list(STONES)]
    return out


def old_blocks():
    return [n for w in WOODS for n in types(w) + [f'{w}_cabinet_with_{s}_countertop' for s in OLD_STONES]]


def rd(path):
    with open(path, encoding='utf-8-sig') as f:
        return json.load(f)


def wr(rel, obj, text=None):
    p = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w') as f:
        f.write(text if text is not None else json.dumps(obj, indent=2) + '\n')


def has_old(rel):
    return os.path.exists(os.path.join(OLD, rel))


def fix_tex(v):
    for key, (_, tex) in STONES.items():
        if tex and v == f'minecraft:block/{key}':
            return tex
    return v


def copy_model(name, seen):
    """models/block/<name>.json from the new jar, recursively for storagedelight parents."""
    if name in seen:
        return
    seen.add(name)
    rel = f'{A}/models/block/{name}.json'
    m = rd(os.path.join(NEW, rel))
    if 'textures' in m:
        m['textures'] = {k: fix_tex(v) for k, v in m['textures'].items()}
        for v in m['textures'].values():
            mm = re.fullmatch(r'storagedelight:block/(.+)', v)
            if mm:
                copy_texture(mm.group(1))
    if m.get('parent', '').startswith('storagedelight:block/'):
        copy_model(m['parent'].split('/', 1)[1], seen)
    if not has_old(rel):
        wr(rel, m)


def copy_texture(t):
    for ext in ('.png', '.png.mcmeta'):
        rel = f'{A}/textures/block/{t}{ext}'
        if not has_old(rel) and os.path.exists(os.path.join(NEW, rel)):
            dst = os.path.join(OUT, rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy(os.path.join(NEW, rel), dst)


def recipe(name, kind):
    ing = lambda i: {'item': i} if not i.startswith('#') else {'tag': i[1:]}
    if name.startswith('bamboo_') or name.startswith('small_bamboo') or name.startswith('glass_bamboo'):
        slab, trap, wood = BAMBOO_SLAB, BAMBOO_TRAPDOOR, 'bamboo'
    else:
        wood = next(w for w in sorted(WOODS, key=len, reverse=True) if re.search(rf'(^|_|small_|glass_){w}(_|$)', name))
        slab, trap = f'minecraft:{wood}_slab', f'minecraft:{wood}_trapdoor'
    # take the pattern and key letters from the new jar's 1.21 recipe, then rewrite the ingredients
    r = rd(os.path.join(NEW, f'{D}/recipe/{name}.json'))
    key = {}
    for k, v in r['key'].items():
        v = v if isinstance(v, str) else v.get('item') or '#' + v['tag']
        if v.endswith('_slab') and (v.startswith('minecraft:polished_') or v == 'minecraft:stone_brick_slab'):
            if v == 'minecraft:polished_tuff_slab':
                v = STONES['polished_tuff'][0]
        elif v.endswith('_slab'):
            v = slab
        elif v.endswith('_trapdoor'):
            v = trap
        elif v == '#c:glass_panes':
            v = '#forge:glass_panes'
        key[k] = ing(v)
    return {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': r['pattern'], 'key': key,
            'result': {'item': f'storagedelight:{name}'}}


def loot(name):
    return {'type': 'minecraft:block', 'pools': [{'name': 'pool1', 'rolls': 1,
            'entries': [{'type': 'minecraft:item', 'name': f'storagedelight:{name}'}],
            'conditions': [{'condition': 'minecraft:survives_explosion'}]}]}


def main():
    shutil.rmtree(OUT + '/assets', ignore_errors=True)
    shutil.rmtree(OUT + '/data', ignore_errors=True)
    blocks = new_blocks()
    assert len(blocks) == 50 and not set(blocks) & set(old_blocks())
    for n in blocks:
        bs = rd(os.path.join(NEW, f'{A}/blockstates/{n}.json'))
        wr(f'{A}/blockstates/{n}.json', bs)
        seen = set()
        for var in bs['variants'].values():
            copy_model(var['model'].split('/', 1)[1], seen)
        wr(f'{A}/models/item/{n}.json', {'parent': f'storagedelight:block/{n}'})
        wr(f'{D}/recipes/{n}.json', recipe(n, None))
        wr(f'{D}/loot_tables/blocks/{n}.json', loot(n))
    # axe tag: every block, old and new
    wr('data/minecraft/tags/blocks/mineable/axe.json', {'replace': False,
       'values': [f'storagedelight:{n}' for n in old_blocks() + blocks]})
    # en_us.json: the upstream file plus the new names
    lang = rd(os.path.join(OLD, f'{A}/lang/en_us.json'))
    new_lang = rd(os.path.join(NEW, f'{A}/lang/en_us.json'))
    for n in blocks:
        lang[f'block.storagedelight.{n}'] = new_lang[f'block.storagedelight.{n}']
    wr(f'{A}/lang/en_us.json', lang)
    print(len(blocks), 'blocks')


main()
