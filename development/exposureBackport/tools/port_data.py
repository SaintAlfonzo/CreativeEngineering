#!/usr/bin/env python3
"""Copy an upstream mod's (1.21.1 NeoForge) assets and data into a source set's resources, converting data to 1.19.2
Forge formats. Like tools/port_upstream.py, files that already exist are only overwritten with --refresh, and never
when listed in tools/hand_ported_resources.txt.

    tools/port_data.py <extracted-upstream-jar> <dest-resources-root> [--exclude GLOB ...] [--refresh] [--dry-run]

--exclude skips upstream files whose path (relative to the jar root, e.g. assets/minecraft/shaders/post/blur.json)
matches a glob; the README's port commands list the ones used.

Conversions (everything else is copied as is):
  folders     data/<ns>/recipe -> recipes, loot_table -> loot_tables, advancement -> advancements,
              tags/item|block|entity_type|fluid -> tags/items|blocks|entity_types|fluids, data/neoforge -> data/forge
  item stacks {"id", "count", "components"} -> {"item", "count", "nbt"} (component data -> NBT, see components_to_nbt)
  ingredients "neoforge:components" -> "forge:partial_nbt"; c: conventional tags -> forge:
  loot        set_components -> set_nbt; neoforge:loot_table_id -> forge:loot_table_id
  create      sequenced assembly / filling recipes -> Create 0.5.1 (transitionalItem, fluid ingredients with nbt)
  advancements icons -> {"item", "nbt"}; item predicates "items": "x" -> ["x"]; criteria naming 1.19.3+ entities dropped
  1.19.3+ ids items/entities that don't exist in 1.19.2 (tools/vanilla_1_19_2_ids.json) are dropped from recipes,
              tags (made optional) and advancements, and reported
"""
import argparse
import fnmatch
import json
import os
import re
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
IDS = json.load(open(os.path.join(HERE, 'vanilla_1_19_2_ids.json')))
VANILLA_ITEMS = set(IDS['items'])
VANILLA_ENTITIES = set(IDS['entity_types'])

DIR_RENAMES = [
    ('recipe', 'recipes'), ('loot_table', 'loot_tables'), ('advancement', 'advancements'),
    ('tags/item', 'tags/items'), ('tags/block', 'tags/blocks'), ('tags/entity_type', 'tags/entity_types'),
    ('tags/fluid', 'tags/fluids'),
]

# 1.21 conventional (c:) tags -> Forge 1.19.2 tags
C_TAGS = {
    'c:glass_panes/colorless': 'forge:glass_panes/colorless',
}

# 1.19.3+ items in shaped-recipe keys (dropping one would break the pattern) -> closest 1.19.2 item, reported
ITEM_SUBSTITUTES = {
    'minecraft:crafter': 'minecraft:dropper',  # 1.21 crafter: the dropper is the block it's built around
}

warnings = []


def warn(path, msg):
    warnings.append(f'{path}: {msg}')


def vanilla_item_missing(item_id):
    ns, _, path = item_id.partition(':') if ':' in item_id else ('minecraft', '', item_id)
    return ns == 'minecraft' and path not in VANILLA_ITEMS


def vanilla_entity_missing(entity_id):
    ns, _, path = entity_id.partition(':') if ':' in entity_id else ('minecraft', '', entity_id)
    return ns == 'minecraft' and path not in VANILLA_ENTITIES


# ---------------------------------------------------------------- NBT

def snbt(value):
    """JSON value -> SNBT text (strings quoted, bools as bytes, ints/doubles by JSON type)."""
    if isinstance(value, bool):
        return '1b' if value else '0b'
    if isinstance(value, int):
        return str(value)
    if isinstance(value, float):
        return repr(value) + 'd'
    if isinstance(value, str):
        return json.dumps(value)
    if isinstance(value, list):
        return '[' + ','.join(snbt(v) for v in value) + ']'
    if isinstance(value, dict):
        return '{' + ','.join(f'{json.dumps(k)}:{snbt(v)}' for k, v in value.items()) + '}'
    raise TypeError(value)


def components_to_nbt(components, path):
    """1.21 item components -> 1.19.2 NBT dict (same mapping as backport.util.BackportRecipes.componentsToNbt)."""
    nbt = {}
    for key, value in components.items():
        if key.startswith('!'):
            continue
        full = key if ':' in key else 'minecraft:' + key
        if full == 'minecraft:potion_contents':
            potion = value if isinstance(value, str) else value.get('potion')
            if potion:
                nbt['Potion'] = potion
        elif full == 'minecraft:custom_name':
            nbt.setdefault('display', {})['Name'] = value if isinstance(value, str) else json.dumps(value)
        elif full == 'minecraft:custom_data':
            nbt.update(value)
        elif full.startswith('minecraft:') or full.startswith('create:'):
            warn(path, f'component {full} has no 1.19.2 NBT mapping, dropped')
        else:
            nbt[full] = value
    return nbt


# ---------------------------------------------------------------- stacks / ingredients

def convert_stack(stack, path):
    if not isinstance(stack, dict):
        return stack
    out = dict(stack)
    if 'id' in out and 'item' not in out:
        out['item'] = out.pop('id')
    if isinstance(out.get('item'), dict):  # Create 6: {"item": {"id": ...}, "chance"}
        inner = convert_stack(out.pop('item'), path)
        out = {**inner, **out}
    components = out.pop('components', None)
    if components:
        nbt = components_to_nbt(components, path)
        if nbt:
            out['nbt'] = snbt(nbt)
    return out


def convert_tag_ref(value):
    if isinstance(value, str):
        prefix = '#' if value.startswith('#') else ''
        bare = value[1:] if prefix else value
        if bare in C_TAGS:
            return prefix + C_TAGS[bare]
        if bare.startswith('c:'):
            return prefix + 'forge:' + bare[2:]
    return value


def convert_ingredient(ing, path):
    if isinstance(ing, list):
        return [convert_ingredient(i, path) for i in ing]
    if not isinstance(ing, dict):
        return ing
    out = dict(ing)
    if 'tag' in out:
        out['tag'] = convert_tag_ref(out['tag'])
    if isinstance(out.get('items'), str):
        v = out.pop('items')
        if v.startswith('#'):
            out['tag'] = convert_tag_ref(v[1:])
        else:
            out['item'] = v
    if out.get('item') in ITEM_SUBSTITUTES:
        warn(path, f"ingredient {out['item']} does not exist in 1.19.2, replaced with {ITEM_SUBSTITUTES[out['item']]}")
        out['item'] = ITEM_SUBSTITUTES[out['item']]
    elif 'item' in out and vanilla_item_missing(out['item']):
        warn(path, f"ingredient {out['item']} does not exist in 1.19.2 (no substitute in ITEM_SUBSTITUTES)")
    if out.get('type') == 'neoforge:components':
        nbt = components_to_nbt(out.get('components', {}), path)
        new = {'type': 'forge:partial_nbt'}
        if 'item' in out:
            new['item'] = out['item']
        elif 'items' in out:
            new['items'] = out['items']
        new['nbt'] = nbt
        out = new
    return out


def ingredient_items(ing):
    if isinstance(ing, list):
        return [i for x in ing for i in ingredient_items(x)]
    if isinstance(ing, dict):
        if 'item' in ing:
            return [ing['item']]
        if isinstance(ing.get('items'), list):
            return list(ing['items'])
    return []


def ingredient_unavailable(ing):
    items = ingredient_items(ing)
    return bool(items) and all(vanilla_item_missing(i) for i in items) and not (isinstance(ing, dict) and 'tag' in ing)


# ---------------------------------------------------------------- per kind

def convert_recipe(d, path):
    t = d.get('type', '')
    if t.startswith('create:'):
        return convert_create_recipe(d, path)
    for key in ('key',):
        if isinstance(d.get(key), dict):
            d[key] = {k: convert_ingredient(v, path) for k, v in d[key].items()}
    if isinstance(d.get('ingredients'), list):
        kept = []
        for ing in d['ingredients']:
            if ingredient_unavailable(ing):
                warn(path, f'ingredient {ingredient_items(ing)} does not exist in 1.19.2, dropped')
                continue
            kept.append(convert_ingredient(ing, path))
        d['ingredients'] = kept
    for key in ('ingredient', 'film', 'photograph', 'source', 'source_ingredient'):
        if key in d:
            d[key] = convert_ingredient(d[key], path)
    if 'result' in d:
        d['result'] = convert_stack(d['result'], path) if isinstance(d['result'], dict) else d['result']
    if 'neoforge:conditions' in d:
        d['conditions'] = convert_conditions(d.pop('neoforge:conditions'))
    return d


def convert_conditions(conditions):
    out = []
    for c in conditions:
        c = dict(c)
        if c.get('type', '').startswith('neoforge:'):
            c['type'] = 'forge:' + c['type'][len('neoforge:'):]
        out.append(c)
    return out


def convert_create_fluid(ing, path):
    if not isinstance(ing, dict) or 'fluid' not in ing and ing.get('type') != 'fluid_stack' and 'fluid_tag' not in ing:
        return None
    out = {}
    if 'fluid_tag' in ing:
        out['fluidTag'] = convert_tag_ref(ing['fluid_tag'])
    else:
        out['fluid'] = ing['fluid']
    nbt = {}
    for key, value in ing.get('components', {}).items():
        if key == 'create:potion_fluid_bottle_type':
            nbt['Bottle'] = value.upper()
        elif key == 'minecraft:potion_contents':
            potion = value if isinstance(value, str) else value.get('potion')
            if potion:
                nbt['Potion'] = potion
        else:
            warn(path, f'fluid component {key} not converted')
    if nbt:
        out['nbt'] = nbt
    out['amount'] = ing.get('amount', 1000)
    return out


def with_mod_loaded(d, modid):
    """Forge recipe condition so the recipe only loads with the mod (upstream relied on NeoForge skipping it)."""
    conditions = d.get('conditions', [])
    if not any(c.get('type') == 'forge:mod_loaded' and c.get('modid') == modid for c in conditions):
        d['conditions'] = conditions + [{'type': 'forge:mod_loaded', 'modid': modid}]
    return d


def convert_create_recipe(d, path, top=True):
    def ingredients(lst):
        out = []
        for ing in lst:
            fluid = convert_create_fluid(ing, path)
            out.append(fluid if fluid is not None else convert_ingredient(ing, path))
        return out

    if 'ingredients' in d:
        d['ingredients'] = ingredients(d['ingredients'])
    if 'ingredient' in d:
        d['ingredient'] = convert_ingredient(d['ingredient'], path)
    if 'results' in d:
        d['results'] = [convert_stack(r, path) for r in d['results']]
    if 'transitional_item' in d:
        d['transitionalItem'] = convert_stack(d.pop('transitional_item'), path)
    if 'sequence' in d:
        d['sequence'] = [convert_create_recipe(dict(step), path, False) for step in d['sequence']]
    return with_mod_loaded(d, 'create') if top else d


def convert_loot(d, path):
    def walk(node):
        if isinstance(node, dict):
            if node.get('function') == 'minecraft:set_components':
                nbt = components_to_nbt(node.get('components', {}), path)
                node = {k: v for k, v in node.items() if k != 'components'}
                node['function'] = 'minecraft:set_nbt'
                node['tag'] = snbt(nbt)
            if node.get('condition', '').startswith('neoforge:'):
                node['condition'] = 'forge:' + node['condition'][len('neoforge:'):]
            return {k: walk(v) for k, v in node.items()}
        if isinstance(node, list):
            return [walk(v) for v in node]
        return node
    return walk(d)


def convert_item_predicate(p, path):
    if not isinstance(p, dict):
        return p
    p = dict(p)
    items = p.get('items')
    if isinstance(items, str):
        if items.startswith('#'):
            p.pop('items')
            p['tag'] = convert_tag_ref(items[1:])
        else:
            p['items'] = [items]
    if 'tag' in p:
        p['tag'] = convert_tag_ref(p['tag'])
    if isinstance(p.get('items'), list):
        missing = [i for i in p['items'] if vanilla_item_missing(i)]
        if missing:
            warn(path, f'item predicate drops 1.19.3+ items {missing}')
            p['items'] = [i for i in p['items'] if i not in missing]
    if 'components' in p:
        nbt = components_to_nbt(p.pop('components'), path)
        if nbt:
            p['nbt'] = snbt(nbt)
    return p


def entities_named(node):
    found = []
    if isinstance(node, dict):
        t = node.get('type')
        # entity predicates name their type as "minecraft:<entity>" (frame/sub-predicate types have no namespace)
        if isinstance(t, str) and t.startswith('minecraft:') and node.get('condition') is None and vanilla_entity_missing(t):
            found.append(t)
        for v in node.values():
            found += entities_named(v)
    elif isinstance(node, list):
        for v in node:
            found += entities_named(v)
    return found


def convert_advancement(d, path):
    display = d.get('display')
    if isinstance(display, dict) and isinstance(display.get('icon'), dict):
        display['icon'] = convert_stack(display['icon'], path)
    criteria = d.get('criteria', {})
    dropped = []
    for name, crit in list(criteria.items()):
        missing = entities_named(crit.get('conditions', {}))
        if missing:
            warn(path, f'criterion {name} needs 1.19.3+ entities {missing}, dropped')
            dropped.append(name)
            del criteria[name]
            continue
        conditions = crit.get('conditions', {})
        if isinstance(conditions.get('items'), list):
            conditions['items'] = [convert_item_predicate(p, path) for p in conditions['items']]
        if isinstance(conditions.get('item'), dict):
            conditions['item'] = convert_item_predicate(conditions['item'], path)
    if dropped and isinstance(d.get('requirements'), list):
        reqs = [[r for r in group if r not in dropped] for group in d['requirements']]
        d['requirements'] = [g for g in reqs if g]
    return d


def convert_tag(d, path):
    values = []
    for v in d.get('values', []):
        ref = v['id'] if isinstance(v, dict) else v
        new_ref = convert_tag_ref(ref)
        if not new_ref.startswith('#') and vanilla_item_missing(new_ref) and '/items/' in path.replace('\\', '/'):
            warn(path, f'{new_ref} does not exist in 1.19.2, made optional')
            values.append({'id': new_ref, 'required': False})
            continue
        values.append({**v, 'id': new_ref} if isinstance(v, dict) else new_ref)
    d['values'] = values
    return d


# ---------------------------------------------------------------- driver

def dest_rel(rel):
    parts = rel.split('/')
    if parts[0] != 'data':
        return rel
    if len(parts) > 1 and parts[1] == 'neoforge':
        parts[1] = 'forge'
    rest = '/'.join(parts[2:])
    for old, new in DIR_RENAMES:
        if rest == old or rest.startswith(old + '/'):
            rest = new + rest[len(old):]
            break
    return '/'.join(parts[:2] + ([rest] if rest else []))


def convert_json(rel_dest, d):
    p = rel_dest
    if '/recipes/' in p:
        return convert_recipe(d, p)
    if '/loot_tables/' in p or '/loot_modifiers/' in p:
        return convert_loot(d, p)
    if '/advancements/' in p:
        return convert_advancement(d, p)
    if '/tags/' in p:
        return convert_tag(d, p)
    return d


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('upstream')
    ap.add_argument('dest')
    ap.add_argument('--refresh', action='store_true')
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--exclude', nargs='*', default=[])
    a = ap.parse_args()

    protected_file = os.path.join(HERE, 'hand_ported_resources.txt')
    protected = set()
    if os.path.exists(protected_file):
        protected = {l.strip() for l in open(protected_file) if l.strip() and not l.startswith('#')}

    written = skipped = 0
    for top in ('assets', 'data'):
        root_dir = os.path.join(a.upstream, top)
        if not os.path.isdir(root_dir):
            continue
        for root, _, files in os.walk(root_dir):
            for f in files:
                src = os.path.join(root, f)
                rel = os.path.relpath(src, a.upstream).replace(os.sep, '/')
                if any(fnmatch.fnmatch(rel, g) for g in a.exclude):
                    continue
                out_rel = dest_rel(rel)
                out = os.path.join(a.dest, out_rel)
                if out_rel in protected or (os.path.exists(out) and not a.refresh):
                    skipped += 1
                    continue
                if not a.dry_run:
                    os.makedirs(os.path.dirname(out), exist_ok=True)
                if top == 'data' and f.endswith('.json'):
                    d = json.load(open(src, encoding='utf-8'))
                    d = convert_json(out_rel, d)
                    if not a.dry_run:
                        with open(out, 'w', encoding='utf-8') as fh:
                            json.dump(d, fh, indent=2, ensure_ascii=False)
                            fh.write('\n')
                elif not a.dry_run:
                    shutil.copyfile(src, out)
                written += 1
    for w in warnings:
        print('WARN', w, file=sys.stderr)
    print(f'written {written}, skipped {skipped}', file=sys.stderr)


if __name__ == '__main__':
    main()
