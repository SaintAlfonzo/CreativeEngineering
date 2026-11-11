#!/usr/bin/env python3
"""Bake Rustic Delight 1.7.0's art and data into this member's resources, translated to Forge 1.19.2 once.

    python3 development/creativeDelight/members/rustic/tools/bake_upstream.py           # bake (rewrites baked files)
    python3 development/creativeDelight/members/rustic/tools/bake_upstream.py --check   # committed files == fresh bake
    python3 development/creativeDelight/members/rustic/tools/bake_upstream.py --selftest

Rustic Delight is MIT (PhantomWing, confirmed 2026-11-10, feature_writeup/plan_rustic_fold_in.md). The pinned donor jar
sits in <instance>/rustic_upstream/ on the dev machine only; it is never deployed. The rules below were the runtime
translator RusticTranslator.java until the fold-in; each is one function with a case in selftest().

Every baked file is listed in tools/bake_manifest.json (path, sha1 of the upstream file, rules applied). Files under
src/main/resources that are not in the manifest are our own (glowing calamari crate, pancake recolours, pack.mcmeta,
mods.toml) and the bake never touches them; an own file at a baked path wins and is recorded as "own_override".
"""
import copy
import hashlib
import json
import os
import shutil
import sys
import tempfile
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.abspath(os.path.join(MOD, '..', '..', '..', '..'))
RESOURCES = os.path.join(MOD, 'src', 'main', 'resources')
MANIFEST = os.path.join(HERE, 'bake_manifest.json')
OWN_OVERRIDES = os.path.join(HERE, 'own_overrides.json')

DONOR_NAME = 'rusticdelight-forge-1.20.1-1.7.0.jar'
DONOR = os.path.join(INSTANCE, 'rustic_upstream', DONOR_NAME)
DONOR_SHA1 = 'bca9adf60b59951ae5049149185da64a6f7b6f8a'
DONOR_URL = 'https://cdn.modrinth.com/data/foa4fGIH/versions/z29Gd3ec/' + DONOR_NAME
EXPORTED = os.path.join(INSTANCE, 'kubejs', 'exported')

NS = 'rusticdelight'
# Upstream ids the port does not register: Fruit Beignet (owner 2026-10-25), the plain single Pancake and Fried Dough
# (owner 2026-11-01, TODO creativedelight/members #15, #25). Files and data naming one are dropped, after REPLACED.
REMOVED = {'fruit_beignet', 'pancake', 'fried_dough'}
# The plain plate's recipes, cut and loot use Supplementaries' pancake instead (owner 2026-11-01).
REPLACED = {'pancake': 'supplementaries:pancake'}
# Advancements (the pack drops them all), biome modifiers (Creative Delight's wild_crops.json places every wild plant),
# upstream's 1.20 edits of five Farmer's Delight recipes.
DROPPED_FOLDERS = ('/advancements/', 'data/rusticdelight/forge/biome_modifier/', 'data/farmersdelight/recipes/')
# Chest seed and bean swaps: the pack's loot scripts own chest loot.
DROPPED_LOOT_MODIFIERS = {'coffee_beans_from_jungle_temple', 'pale_bell_pepper_seeds_from_jungle_temple',
                          'dark_bell_pepper_seeds_from_jungle_temple', 'cotton_seeds_from_village_taiga',
                          'cotton_seeds_from_village_snowy', 'cotton_seeds_from_village_savanna',
                          'cotton_seeds_from_abandoned_mineshaft', 'cotton_seeds_from_shipwreck_supply'}
# Wild and giant patches keep the three base colours: Pale and Dark seeds only come from chest loot (owner).
RARE_PATCH_BLOCKS = {'wild_pale_bell_peppers', 'wild_dark_bell_peppers', 'bell_pepper_orange_block',
                     'bell_pepper_white_block', 'bell_pepper_pink_block', 'bell_pepper_blue_block',
                     'bell_pepper_purple_block', 'bell_pepper_black_block'}
# 1.19.2 has no cherry blossoms; Vinery cherry leaves or Quark pink blossom leaves stand in (owner).
CHERRY_BLOSSOMS = ['vinery:cherry_leaves', 'quark:pink_blossom_leaves']
# Farmer's Delight 1.20 model ids to their 1.19.2 names (same model).
RENAMED_MODEL_REFS = {'"farmersdelight:block/template_crop_cross"': '"farmersdelight:block/crop_cross"'}
# FD 1.20 names item tooltips tooltip.farmersdelight.<item>; FD 1.19.2 reads farmersdelight.tooltip.<item>.
OLD_TOOLTIP_PREFIX = 'tooltip.farmersdelight.'
NEW_TOOLTIP_PREFIX = 'farmersdelight.tooltip.'
# Recipe types upstream uses that the pack registers but that may have no recipe in the last dump.
KNOWN_SERIALIZERS = {'forge:conditional'}


# ----------------------------------------------------------------------------------------------------------- rules

# Files named like a removed id that other kept files need: the coaster pancake model is the parent of the six flavoured
# pancake coaster models, and its texture is their particle (the runtime translator dropped both; missing model fixed
# 2026-11-10).
KEPT_SHARED = {'assets/rusticdelight/models/coaster/pancake.json', 'assets/rusticdelight/textures/block/coaster/pancake.png'}


def names_removed(path):
    if path in KEPT_SHARED:
        return False
    f = path.rsplit('/', 1)[-1]
    return any(f in (i + '.json', i + '.png', i + '.png.mcmeta') for i in REMOVED)


def dropped_folder(path):
    return any(d in path for d in DROPPED_FOLDERS)


def dropped_loot_modifier(path):
    return path.startswith('data/rusticdelight/loot_modifiers/') and path.rsplit('/', 1)[-1][:-5] in DROPPED_LOOT_MODIFIERS


def replace_ids(text):
    for old, new in REPLACED.items():
        text = text.replace('"%s:%s"' % (NS, old), '"%s"' % new)
    return text


def mentions_removed(text):
    return any('"%s:%s"' % (NS, i) in text for i in REMOVED)


def is_removed(id_):
    bare = id_[1:] if id_.startswith('#') else id_
    return bare.startswith(NS + ':') and bare[len(NS) + 1:] in REMOVED


def optional(id_):
    return {'id': id_, 'required': False}


def tag_values(tag):
    """Drop removed ids; entries from other namespaces become optional (1.19.2 rejects a tag for one unknown entry)."""
    values = tag.get('values')
    if values is None:
        return tag
    out = []
    for v in values:
        id_ = v['id'] if isinstance(v, dict) else v
        if is_removed(id_):
            continue
        if not isinstance(v, dict) and not id_.startswith(NS + ':') and not id_.startswith('#' + NS + ':'):
            v = optional(id_)
        out.append(v)
    tag['values'] = out
    return tag


def not_(term):
    return {'condition': 'minecraft:inverted', 'term': term}


def loot_conditions(e):
    """1.20 any_of -> alternative; all_of -> not(alternative(not each term))."""
    if isinstance(e, list):
        return [loot_conditions(x) for x in e]
    if not isinstance(e, dict):
        return e
    e = {k: loot_conditions(v) for k, v in e.items()}
    name = e.get('condition') if isinstance(e.get('condition'), str) else ''
    if name == 'minecraft:any_of':
        e['condition'] = 'minecraft:alternative'
    elif name == 'minecraft:all_of':
        return not_({'condition': 'minecraft:alternative', 'terms': [not_(t) for t in e['terms']]})
    return e


def cherry_blossoms(tag):
    tag['values'] = [optional(i) for i in CHERRY_BLOSSOMS]
    return tag


def base_patch(e):
    """Keep the base-colour entries of a patch's weighted state provider."""
    if isinstance(e, list):
        out = []
        for x in e:
            if isinstance(x, dict) and 'weight' in x and 'data' in x:
                if x['data']['Name'].split(':', 1)[-1] in RARE_PATCH_BLOCKS:
                    continue
            out.append(base_patch(x))
        return out
    if isinstance(e, dict):
        return {k: base_patch(v) for k, v in e.items()}
    return e


PATCH_DATA = {
    'data/rusticdelight/tags/items/cherry_blossom_ingredients.json': ('cherry_blossoms', cherry_blossoms),
    'data/rusticdelight/worldgen/configured_feature/wild_bell_peppers.json': ('base_patch', base_patch),
    'data/rusticdelight/worldgen/configured_feature/bell_pepper_block_patch.json': ('base_patch', base_patch),
}


def lang(obj):
    out = {}
    for k, v in obj.items():
        if any(k.endswith('.%s.%s' % (NS, i)) for i in REMOVED):
            continue
        if k.startswith(OLD_TOOLTIP_PREFIX):
            k = NEW_TOOLTIP_PREFIX + k[len(OLD_TOOLTIP_PREFIX):]
        out[k] = v
    return out


def rename_model_refs(text):
    for old, new in RENAMED_MODEL_REFS.items():
        text = text.replace(old, new)
    return text


class Registry:
    """The pack's items, mods and recipe serializers from the last /dumpall (the static stand-in for the old runtime
    check against ForgeRegistries)."""

    def __init__(self, items, mods, serializers):
        self.items, self.mods, self.serializers = items, mods, serializers

    @staticmethod
    def from_dumps():
        items, mods = set(), set()
        d = os.path.join(EXPORTED, 'items_by_mod')
        for f in os.listdir(d):
            if f.endswith('.json'):
                mods.add(f[:-5])
                items.update(json.load(open(os.path.join(d, f))).get('items', []))
        types = json.load(open(os.path.join(EXPORTED, 'recipes', 'recipe_types.json')))
        serializers = set(KNOWN_SERIALIZERS)
        for t in types.values():
            serializers.update(t.get('serializers', {}))
        mods.update(s.split(':', 1)[0] for s in serializers)
        mods.add('minecraft')
        mods.add('forge')
        return Registry(items, mods, serializers)


def missing_item(e, reg):
    if isinstance(e, list):
        for x in e:
            m = missing_item(x, reg)
            if m:
                return m
    elif isinstance(e, dict):
        for k, v in e.items():
            if k == 'item' and isinstance(v, str):
                if v not in reg.items:
                    return v
            elif k != 'conditions':
                m = missing_item(v, reg)
                if m:
                    return m
    return None


def missing_recipe_id(recipe, reg):
    t = recipe.get('type')
    if t and t != 'forge:conditional' and t not in reg.serializers:
        return t
    return missing_item(recipe, reg)


def recipe_rule(recipe, reg):
    """Returns (recipe or None, rule name or None). A recipe naming an id this pack lacks is dropped when the id's mod
    is in the pack (a 1.20-only item); when the mod is absent it gets a forge:mod_loaded condition instead."""
    missing = missing_recipe_id(recipe, reg)
    if missing is None:
        return recipe, None
    mod = missing.split(':', 1)[0]
    if mod in reg.mods:
        return None, 'drop_missing:' + missing
    conds = list(recipe.get('conditions', []))
    conds.append({'type': 'forge:mod_loaded', 'modid': mod})
    out = dict(recipe)
    out['conditions'] = conds
    return out, 'mod_loaded:' + mod


def drop_dangling_loot_modifiers(files):
    path = 'data/forge/loot_modifiers/global_loot_modifiers.json'
    if path not in files:
        return
    lst = json.loads(files[path][0])
    kept = []
    for entry in lst['entries']:
        ns, p = entry.split(':', 1)
        if 'data/%s/loot_modifiers/%s.json' % (ns, p) in files:
            kept.append(entry)
    if kept != lst['entries']:
        lst['entries'] = kept
        files[path] = (dump(lst), files[path][1], files[path][2] + ['dangling_loot_modifiers'])


def dump(obj):
    return (json.dumps(obj, indent=2, ensure_ascii=False) + '\n').encode('utf-8')


def translate(path, raw, reg):
    """Returns (bytes, rules) or (None, rules) to drop."""
    rules = []
    if path.startswith('assets/'):
        if '/lang/' in path:
            obj = json.loads(raw.decode('utf-8'))
            new = lang(obj)
            if new != obj:
                return dump(new), ['lang']
            return raw, rules
        if '/models/' in path:
            text = raw.decode('utf-8')
            new = rename_model_refs(text)
            if new != text:
                return new.encode('utf-8'), ['model_refs']
        return raw, rules
    # data
    if not path.endswith('.json'):
        return raw, rules
    if dropped_loot_modifier(path):
        return None, ['dropped_loot_modifier']
    text = raw.decode('utf-8')
    new_text = replace_ids(text)
    if new_text != text:
        rules.append('replaced_ids')
    if mentions_removed(new_text) and '/tags/' not in path:
        return None, rules + ['mentions_removed']
    obj = json.loads(new_text)
    orig = copy.deepcopy(obj)
    if '/tags/' in path:
        obj = tag_values(obj)
        if obj != orig:
            rules.append('tag_values')
    elif '/recipes/' in path:
        obj, rule = recipe_rule(obj, reg)
        if rule:
            rules.append(rule)
        if obj is None:
            return None, rules
    elif '/loot_modifiers/' in path or '/loot_tables/' in path:
        obj = loot_conditions(obj)
        if obj != orig:
            rules.append('loot_conditions')
    patch = PATCH_DATA.get(path)
    if patch:
        before = copy.deepcopy(obj)
        obj = patch[1](obj)
        if obj != before:
            rules.append(patch[0])
    if not rules:
        return raw, rules
    return dump(obj), rules


# ------------------------------------------------------------------------------------------------------------ bake

def sha1(b):
    return hashlib.sha1(b).hexdigest()


def read_donor():
    if not os.path.isfile(DONOR):
        sys.exit('donor jar missing: %s\nget it from %s' % (DONOR, DONOR_URL))
    raw = open(DONOR, 'rb').read()
    if sha1(raw) != DONOR_SHA1:
        sys.exit('donor jar sha1 %s, expected %s' % (sha1(raw), DONOR_SHA1))
    zf = zipfile.ZipFile(DONOR)
    return {n: zf.read(n) for n in zf.namelist()
            if not n.endswith('/') and (n.startswith('assets/') or n.startswith('data/'))}


def bake(reg):
    """Returns ({path: (bytes, upstream sha1, rules)}, {path: (upstream sha1, rules)} dropped)."""
    files, dropped = {}, {}
    for path, raw in sorted(read_donor().items()):
        if names_removed(path):
            dropped[path] = (sha1(raw), ['names_removed'])
            continue
        if path.startswith('data/') and dropped_folder(path):
            dropped[path] = (sha1(raw), ['dropped_folder'])
            continue
        out, rules = translate(path, raw, reg)
        if out is None:
            dropped[path] = (sha1(raw), rules)
        else:
            files[path] = (out, sha1(raw), rules)
    drop_dangling_loot_modifiers(files)
    return files, dropped


def own_overrides():
    return set(json.load(open(OWN_OVERRIDES))['paths']) if os.path.isfile(OWN_OVERRIDES) else set()


def manifest_of(files, dropped, overrides):
    return {
        'donor': DONOR_NAME, 'donor_sha1': DONOR_SHA1, 'donor_url': DONOR_URL, 'licence': 'MIT, Copyright (c) PhantomWing',
        'files': {p: {'upstream_sha1': s, 'rules': r} | ({'own_override': True} if p in overrides else {})
                  for p, (_, s, r) in sorted(files.items())},
        'dropped': {p: {'upstream_sha1': s, 'rules': r} for p, (s, r) in sorted(dropped.items())},
    }


def write(root, files, overrides):
    for path, (data, _, _) in files.items():
        if path in overrides:
            continue
        full = os.path.join(root, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, 'wb') as f:
            f.write(data)


def main_bake():
    reg = Registry.from_dumps()
    files, dropped = bake(reg)
    overrides = own_overrides()
    if os.path.isfile(MANIFEST):
        for p in json.load(open(MANIFEST))['files']:
            if p not in overrides and p not in files and os.path.isfile(os.path.join(RESOURCES, p)):
                os.remove(os.path.join(RESOURCES, p))
    write(RESOURCES, files, overrides)
    with open(MANIFEST, 'w') as f:
        json.dump(manifest_of(files, dropped, overrides), f, indent=1)
        f.write('\n')
    assets = sum(1 for p in files if p.startswith('assets/'))
    print('baked %d asset and %d data files, dropped %d, own overrides %d' % (assets, len(files) - assets, len(dropped), len(overrides)))
    for p, (_, r) in dropped.items():
        if any(x.startswith('drop_missing') or x.startswith('mod_loaded') for x in r):
            print('  ', p, r)
    for p, (_, _, r) in files.items():
        if any(x.startswith('mod_loaded') for x in r):
            print('  ', p, r)


def main_check():
    reg = Registry.from_dumps()
    files, _ = bake(reg)
    overrides = own_overrides()
    bad = 0
    for path, (data, _, _) in files.items():
        if path in overrides:
            continue
        full = os.path.join(RESOURCES, path)
        if not os.path.isfile(full):
            print('missing:', path)
            bad += 1
        elif open(full, 'rb').read() != data:
            print('differs:', path)
            bad += 1
    listed = set(json.load(open(MANIFEST))['files']) if os.path.isfile(MANIFEST) else set()
    for p in sorted(listed - set(files)):
        print('in manifest, not in a fresh bake:', p)
        bad += 1
    print('check: %d problems' % bad)
    sys.exit(1 if bad else 0)


# -------------------------------------------------------------------------------------------------------- selftest

def selftest():
    reg = Registry({'minecraft:sugar', 'supplementaries:pancake', 'rusticdelight:batter'},
                   {'minecraft', 'forge', 'supplementaries', NS, 'farmersdelight'}, {'minecraft:crafting_shaped', 'farmersdelight:cutting'})
    assert names_removed('assets/rusticdelight/textures/item/pancake.png')
    assert not names_removed('assets/rusticdelight/textures/item/pancake_stack.png')
    assert not names_removed('assets/rusticdelight/models/coaster/pancake.json')
    assert names_removed('assets/rusticdelight/brewinandchewin/coaster/pancake.json')
    assert dropped_folder('data/rusticdelight/advancements/x.json')
    assert dropped_loot_modifier('data/rusticdelight/loot_modifiers/cotton_seeds_from_shipwreck_supply.json')
    assert replace_ids('{"item":"rusticdelight:pancake"}') == '{"item":"supplementaries:pancake"}'
    assert mentions_removed('{"item":"rusticdelight:fried_dough"}')
    assert tag_values({'values': ['rusticdelight:a', '#rusticdelight:b', 'minecraft:c', 'rusticdelight:pancake', {'id': 'x:y', 'required': False}]}) == \
        {'values': ['rusticdelight:a', '#rusticdelight:b', {'id': 'minecraft:c', 'required': False}, {'id': 'x:y', 'required': False}]}
    a, b = {'condition': 'a'}, {'condition': 'b'}
    assert loot_conditions({'condition': 'minecraft:any_of', 'terms': [a]}) == {'condition': 'minecraft:alternative', 'terms': [a]}
    assert loot_conditions({'pools': [{'conditions': [{'condition': 'minecraft:all_of', 'terms': [a, b]}]}]}) == \
        {'pools': [{'conditions': [not_({'condition': 'minecraft:alternative', 'terms': [not_(a), not_(b)]})]}]}
    assert cherry_blossoms({'values': ['minecraft:cherry_leaves']})['values'][0] == optional('vinery:cherry_leaves')
    patch = {'entries': [{'weight': 1, 'data': {'Name': 'rusticdelight:wild_red_bell_peppers'}},
                         {'weight': 1, 'data': {'Name': 'rusticdelight:wild_dark_bell_peppers'}}]}
    assert base_patch(patch) == {'entries': [patch['entries'][0]]}
    assert lang({'item.rusticdelight.pancake': 'P', 'tooltip.farmersdelight.x': 'T', 'k': 'v'}) == {'farmersdelight.tooltip.x': 'T', 'k': 'v'}
    assert rename_model_refs('{"parent": "farmersdelight:block/template_crop_cross"}') == '{"parent": "farmersdelight:block/crop_cross"}'
    r = {'type': 'minecraft:crafting_shaped', 'result': {'item': 'rusticdelight:batter'}, 'key': {'a': {'item': 'minecraft:sugar'}}}
    assert recipe_rule(r, reg) == (r, None)
    assert recipe_rule(dict(r, key={'a': {'item': 'minecraft:cherry_log'}}), reg)[0] is None
    kept, rule = recipe_rule(dict(r, key={'a': {'item': 'absentmod:thing'}}), reg)
    assert rule == 'mod_loaded:absentmod' and kept['conditions'] == [{'type': 'forge:mod_loaded', 'modid': 'absentmod'}]
    assert recipe_rule(dict(r, type='absentmod:press'), reg)[1] == 'mod_loaded:absentmod'
    assert recipe_rule(dict(r, conditions=[{'type': 'x'}], key={'a': {'item': 'minecraft:sugar'}}), reg)[1] is None
    files = {'data/forge/loot_modifiers/global_loot_modifiers.json': (b'{"replace": false, "entries": ["rusticdelight:a", "rusticdelight:b"]}', 's', []),
             'data/rusticdelight/loot_modifiers/a.json': (b'{}', 's', [])}
    drop_dangling_loot_modifiers(files)
    assert json.loads(files['data/forge/loot_modifiers/global_loot_modifiers.json'][0])['entries'] == ['rusticdelight:a']
    print('selftest ok')


if __name__ == '__main__':
    arg = sys.argv[1] if len(sys.argv) > 1 else ''
    {'--check': main_check, '--selftest': selftest}.get(arg, main_bake)()
