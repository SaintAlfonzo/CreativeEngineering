#!/usr/bin/env python3
"""Copy Display Delight 1.7.0's assets and data into the port, in 1.19.2 formats, and write the block table.

    python3 tools/port_resources.py            (run from anywhere; rewrites src/main/resources/{assets,data}
                                                and src/main/java/.../init/DisplayFoods.java)

Source: the upstream jar (LGPLv3, models partly MIT), read directly; the path and sha1 are in gradle.properties.
The block list is tools/display_blocks.json: every food display block 1.7.0 registers, transcribed from its
DisplayItems / DisplayBlocks / PlatedBlocks / SmallPlatedBlocks classes, in its item (creative tab) order.

What changes on the way (feature_writeup/display_delight.md section 5):

  * left out: every block whose prefix names a mod the pack does not have (EXCLUDED_PREFIXES), and every block whose
    food the pack removes (kubejs/startup_scripts/_lib/removed_items.js, read through item_dict.js), with its
    blockstate, item model, lang keys and the block models and textures only it uses; the tag entries of the left-out
    mods; the two unregistered showcase blocks
  * Miner's Delight: 1.7.0's tags name `minersdelight:` items, the pack's port is `miners_delight` (the Java side maps
    the `md_` prefix the same way)
  * block models: a face on the undefined texture '#missing' takes its element's top face texture
  * three 1.2.0 textures 1.7.0 dropped ship again from tools/legacy_1.2.0/: block/bowl (still the particle of
    1.7.0's parent_bowl) and minecraft:farmers3d/tray_top and tray_bottom, which the pack's patcherMod dish models
    (tools/gen_dd_dishes.py) copied from 1.2.0's bowl and plate
  * item models: a registered block item with no item model gets one parented on its default-state block model
  * `displaydelight:support_exceptions` also holds the pack's Cooking for Blockheads counters
    (#creativeengineering:counter_supports, optional), so a plate on a counter does not grow a wooden leg
  * recipes lose the 1.20 'category' and 'show_notification' fields, loot tables 'random_sequence'; lang gets the
    1.19.2 creative tab key
"""
import hashlib
import json
import os
import re
import shutil
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.dirname(os.path.dirname(MOD))
sys.path.insert(0, os.path.join(INSTANCE, 'tools'))
import display_delight_jar as ddj  # noqa: E402  (1.7.0's block name -> food rule)
OUT = os.path.join(MOD, 'src', 'main', 'resources')
REMOVED_ITEMS = os.path.join(INSTANCE, 'kubejs', 'startup_scripts', '_lib', 'removed_items.js')
ITEM_DICT = os.path.join(INSTANCE, 'kubejs', 'startup_scripts', '_lib', 'item_dict.js')
JAVA_OUT = os.path.join(MOD, 'src', 'main', 'java', 'com', 'jkvin114', 'displaydelight', 'init', 'DisplayFoods.java')
NS = 'displaydelight'
A = 'assets/%s/' % NS

# Block id prefix -> source mod 1.7.0 supports but the pack does not have. Blocks with these prefixes are not ported.
EXCLUDED_PREFIXES = {
    'ed_': 'expandeddelight',
    'pd_': 'pineapple_delight',
    'ctd_': 'culturaldelights',
    'lm_': 'largemeals',
    'fd_': 'festive_delight',
    'acd_': 'aquaculturedelight',
    'mnd_': 'mynethersdelight',
    'crd_': 'crabbersdelight',
}
TAG_NAMESPACE_MAP = {'minersdelight': 'miners_delight'}
PLATES = ['food_plate', 'small_food_plate']
# The gui transform most plated block models use, for a generated item model whose parent chain has none
PLATED_GUI = {'rotation': [37, 44, 0], 'translation': [0, 4, 0], 'scale': [0.8, 0.8, 0.8]}
LEGACY = os.path.join(HERE, 'legacy_1.2.0')
COUNTER_SUPPORTS = {'id': '#creativeengineering:counter_supports', 'required': False}

# Java factory per block type (DisplayBlocks)
FACTORY = {
    'food': 'food', 'wide': 'wide', 'drink': 'drink', 'hot_drink': 'hotDrink', 'fiery_food': 'fieryFood',
    'fiery_wide': 'fieryWide', 'plated': 'plated', 'stackable': 'stackable', 'fiery_stackable': 'fieryStackable',
    'small': 'small', 'fiery_small': 'fierySmall',
}


def read_props():
    props = {}
    with open(os.path.join(MOD, 'gradle.properties')) as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                k, v = line.split('=', 1)
                props[k.strip()] = v.strip()
    return props


def open_jar():
    props = read_props()
    path = os.path.join(INSTANCE, props['upstream_jar'])
    if not os.path.exists(path):
        sys.exit('upstream jar missing: %s' % path)
    with open(path, 'rb') as f:
        sha = hashlib.sha1(f.read()).hexdigest()
    if sha != props['upstream_sha1']:
        sys.exit('%s has sha1 %s, gradle.properties pins %s' % (path, sha, props['upstream_sha1']))
    return zipfile.ZipFile(path)


def removed_foods():
    """Item ids in the pack's removed-items list (global.item keys resolved through item_dict.js, or literal ids)."""
    with open(ITEM_DICT) as f:
        keys = dict(re.findall(r"^i\.(\w+)\s*=\s*'([^']+)'", f.read(), re.M))
    with open(REMOVED_ITEMS) as f:
        src = re.sub(r'//[^\n]*', '', f.read())
    return {keys[k] for k in re.findall(r'\bi\.(\w+)', src) if k in keys} | set(re.findall(r"'([a-z0-9_]+:[a-z0-9_/]+)'", src))


def load_blocks():
    """(kept rows, rows left out for a missing mod, rows left out for a removed food)."""
    with open(os.path.join(HERE, 'display_blocks.json')) as f:
        rows = json.load(f)
    removed = removed_foods()
    missing = [r for r in rows if any(r['id'].startswith(p) for p in EXCLUDED_PREFIXES)]
    gone = [r for r in rows if r not in missing and ddj.food_for(r['id']) in removed]
    return [r for r in rows if r not in missing and r not in gone], missing, gone


def write_java(rows):
    lines = []
    for r in rows:
        args = '"%s"' % r['id'] + (', %d' % r['stacks'] if 'stacks' in r else '')
        lines.append('        DisplayItems.food(DisplayBlocks.%s(%s));' % (FACTORY[r['type']], args))
    src = '''package com.jkvin114.displaydelight.init;

// Generated by tools/port_resources.py from tools/display_blocks.json; do not edit by hand.

/**
 * Every food display block and its item, in Display Delight 1.7.0's creative tab order. Blocks for mods the pack does
 * not have are left out (port_resources.py, EXCLUDED_PREFIXES).
 */
final class DisplayFoods {
    private DisplayFoods() {}

    static void register() {
%s
    }
}
''' % '\n'.join(lines)
    os.makedirs(os.path.dirname(JAVA_OUT), exist_ok=True)
    with open(JAVA_OUT, 'w') as f:
        f.write(src)


def jload(z, name):
    return json.loads(z.read(name))


def model_path(ref):
    """'displaydelight:block/x' -> 'assets/displaydelight/models/block/x.json'; other namespaces -> None."""
    if ':' not in ref:
        return None
    ns, path = ref.split(':', 1)
    return 'assets/%s/models/%s.json' % (ns, path) if ns == NS else None


def blockstate_models(bs):
    out = []
    for v in bs.get('variants', {}).values():
        out += [x['model'] for x in (v if isinstance(v, list) else [v])]
    for part in bs.get('multipart', []):
        a = part['apply']
        out += [x['model'] for x in (a if isinstance(a, list) else [a])]
    return out


def default_model(bs):
    """Model of the facing=north, stacks=1 state, else the first food model of a multipart."""
    for key, variant in bs.get('variants', {}).items():
        props = dict(p.split('=') for p in key.split(',') if p)
        if props.get('facing', 'north') == 'north' and props.get('stacks', '1') == '1':
            return (variant[0] if isinstance(variant, list) else variant)['model']
    for part in bs.get('multipart', []):
        when = json.dumps(part.get('when', {}))
        if '"support"' in when:
            continue
        a = part['apply']
        return (a[0] if isinstance(a, list) else a)['model']
    return None


def has_gui(models, ref):
    while ref and ref.startswith(NS + ':'):
        m = models.get(model_path(ref))
        if m is None:
            return False
        if 'gui' in m.get('display', {}):
            return True
        ref = m.get('parent')
    return ref is not None


def fix_missing_faces(model):
    fixed, changed = json.loads(json.dumps(model)), False
    for element in fixed.get('elements', []):
        faces = element.get('faces', {})
        donor = faces.get('up', {}).get('texture')
        if donor in (None, '#missing'):
            donor = next((f['texture'] for f in faces.values() if f.get('texture') not in (None, '#missing')), None)
        for face in faces.values():
            if face.get('texture') == '#missing' and donor:
                face['texture'] = donor
                changed = True
    return fixed if changed else None


def write_json(rel, data, indent=2):
    path = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=indent, ensure_ascii=False)
        f.write('\n')


def copy_raw(z, name, rel=None):
    path = os.path.join(OUT, rel or name)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(z.read(name))


def main():
    z = open_jar()
    names = set(z.namelist())
    kept, missing, gone = load_blocks()
    write_java(kept)
    block_ids = PLATES + [r['id'] for r in kept]
    dropped_ids = {r['id'] for r in missing + gone}

    for sub in ('assets', 'data'):  # assets/minecraft holds only the legacy textures
        shutil.rmtree(os.path.join(OUT, sub), ignore_errors=True)

    # Every block model in the jar, for parent chains and the item-model fallback
    models = {n: jload(z, n) for n in names if n.startswith(A + 'models/') and n.endswith('.json')}

    # Blockstates, and the block models they and the item models reach
    reach, todo, item_models, problems = set(), [], {}, []
    generated_items = []
    for bid in block_ids:
        bs_name = A + 'blockstates/%s.json' % bid
        if bs_name not in names:
            problems.append('%s: no blockstate' % bid)
            continue
        bs = jload(z, bs_name)
        write_json('assets/%s/blockstates/%s.json' % (NS, bid), bs, indent=1)
        todo += blockstate_models(bs)
        im_name = A + 'models/item/%s.json' % bid
        if im_name in names:
            item_models[bid] = models[im_name]
        else:
            ref = default_model(bs)
            if ref is None or model_path(ref) not in models:
                problems.append('%s: no item model and no default block model' % bid)
                continue
            entry = {'parent': ref}
            if not has_gui(models, ref):
                entry['display'] = {'gui': PLATED_GUI}
            item_models[bid] = entry
            generated_items.append(bid)
    for bid, m in item_models.items():
        write_json('assets/%s/models/item/%s.json' % (NS, bid), m, indent=1)
        if m.get('parent'):
            todo.append(m['parent'])
    textures, foreign = set(), set()
    while todo:
        ref = todo.pop()
        path = model_path(ref)
        if path is None or path in reach:
            continue
        if path not in models:
            problems.append('model %s not in the jar' % ref)
            continue
        reach.add(path)
        m = models[path]
        if m.get('parent'):
            todo.append(m['parent'])
        for tex in m.get('textures', {}).values():
            if tex.startswith('#'):
                continue
            ns, p = tex.split(':', 1) if ':' in tex else ('minecraft', tex)
            if ns == NS:
                textures.add('%stextures/%s.png' % (A, p))
            else:
                foreign.add('%s:%s' % (ns, p))
    fixed = 0
    written_items = {A + 'models/item/%s.json' % b for b in item_models}
    for path in sorted(reach):
        if path in written_items:
            continue
        m = models[path]
        if '"#missing"' in json.dumps(m):
            f = fix_missing_faces(m)
            if f is not None:
                m, fixed = f, fixed + 1
        write_json(path, m, indent=1)
    legacy = []
    for root, _, files in os.walk(LEGACY):
        for f in files:
            rel = os.path.relpath(os.path.join(root, f), LEGACY).replace(os.sep, '/')
            legacy.append(rel)
            os.makedirs(os.path.dirname(os.path.join(OUT, rel)), exist_ok=True)
            shutil.copyfile(os.path.join(root, f), os.path.join(OUT, rel))
    # the Food Plate and Small Food Plate items' own icons: no block model reaches them
    textures |= {n for n in names if n.startswith(A + 'textures/item/') and n.endswith('.png')}
    for t in sorted(textures):
        if t in legacy:
            continue
        if t not in names:
            problems.append('texture %s not in the jar' % t)
            continue
        copy_raw(z, t)
        if t + '.mcmeta' in names:
            copy_raw(z, t + '.mcmeta')

    # Lang: drop the keys of blocks that are not ported; 1.19.2 creative tab key
    drop_keys = {'block.%s.%s' % (NS, b) for b in dropped_ids} | {'item.%s.%s' % (NS, b) for b in dropped_ids}
    for n in sorted(names):
        if n.startswith(A + 'lang/') and n.endswith('.json'):
            lang = {k: v for k, v in jload(z, n).items() if k not in drop_keys}
            if n.endswith('/en_us.json'):
                lang['itemGroup.%s' % NS] = 'Display Delight'
            write_json(n, lang)

    # Data
    excluded_ns = set(EXCLUDED_PREFIXES.values())
    for n in sorted(names):
        if not n.startswith('data/') or n.endswith('/'):
            continue
        d = jload(z, n)
        if '/tags/items/' in n:
            values = []
            for v in d['values']:
                vid = v['id'] if isinstance(v, dict) else v
                ns = vid.lstrip('#').split(':', 1)[0]
                if ns in excluded_ns:
                    continue
                if ns in TAG_NAMESPACE_MAP:
                    vid = vid.replace(ns + ':', TAG_NAMESPACE_MAP[ns] + ':', 1)
                values.append(dict(v, id=vid) if isinstance(v, dict) else vid)
            d['values'] = values
        elif n.endswith('/tags/blocks/support_exceptions.json'):
            d['values'].append(COUNTER_SUPPORTS)
        elif '/recipes/' in n:
            d.pop('category', None)
            d.pop('show_notification', None)
        elif '/loot_tables/' in n:
            d.pop('random_sequence', None)
        write_json(n, d)

    copy_raw(z, 'logo.png')
    write_json('pack.mcmeta', {'pack': {
        'description': 'Display Delight (Backport) resources', 'pack_format': 9,
        'forge:resource_pack_format': 9, 'forge:data_pack_format': 10}})

    print('blocks ported: %d food + 2 plates; left out: %d for mods not in the pack (%s), %d for removed foods (%s)' % (
        len(kept), len(missing), ', '.join(sorted(set(EXCLUDED_PREFIXES.values()))), len(gone),
        ' '.join(r['id'] for r in gone)))
    print('block models: %d (%d #missing faces fixed); item models: %d (%d generated); textures: %d' % (
        len([p for p in reach if '/models/block/' in p]), fixed, len(item_models), len(generated_items), len(textures)))
    if generated_items:
        print('generated item models: ' + ' '.join(generated_items))
    if foreign:
        print('textures from other mods: ' + ' '.join(sorted(foreign)))
    for p in problems:
        print('WARNING ' + p)
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
