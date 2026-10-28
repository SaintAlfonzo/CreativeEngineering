#!/usr/bin/env python3
"""Comfort / Nourishment split (feature_writeup/comfort_nourishment.md section 9 and appendix C): resolve the rules
below into kubejs/generation_scripts/food/comfort_nourishment.json.

Creative Delight's creativeengineering/comfort_nourishment.js reads that file on ItemEvents.modification (startup)
and sets Farmer's Delight's Comfort / Nourishment on every food. Other effects, hunger and flags are not touched.

Ruled forms: foods that hand back a bowl, a mason jar or a shulker bowl, and copper cup meals. First match wins:
  4+ inputs, input hunger 8+, output hunger 10+   Nourishment
  3+ inputs, input hunger 5+                      Comfort
  output hunger 10+                               Comfort
  otherwise                                       neither
Length: output hunger under 10 -> 120 s, 10 and up -> 240 s; Comfort at output hunger 7 or less -> 60 s.
  Inputs = ingredient slots of the food's recipe, the container left out. Recipe: the cooking pot recipe, else
  shapeless, else shaped crafting (first id); a feast serving uses its feast block's recipe. A slot counts its lowest
  hunger food member; a slot with no food member takes the cheapest recipe value of its items (two levels deep),
  else 0 (CN-D9: base value 0). A copper cup takes its bowl's inputs (Miner's cup_conversion data) and its own hunger.
  A ruled food with no recipe found keeps its effects (listed under `keep`, hand check: appendix B.2).
Golden foods (food types golden / golden/enchanted) override every rule: Nourishment only, 120 s (enchanted 240 s).
Bottles (glass bottle remainder): both effects removed. Every other food: Nourishment removed, Comfort kept (the
applier does that at runtime for every food not listed here); a kept Comfort at output hunger 7 or less is set to
60 s. Cans are skipped: they copy their source food through
tools/gen_can_types.py, which applies this file to the source's effects.

The rules never look at a food's current Comfort / Nourishment, so the output does not shift after the game has
applied it. Restart the game after a run.

    python3 tools/gen_comfort_nourishment.py            write the JSON, print a summary
    python3 tools/gen_comfort_nourishment.py --list     also print every ruled food with its inputs

Source data (all under kubejs/exported/, from /dumpall): food/foods.json, food/recipes.json, food/food_blocks.json,
tags/item_tags.json; kubejs/generation_scripts/food/food_types.json (golden types); Miner's Delight's cup_conversion from the donor jar in miners_upstream/.
"""
import argparse
import glob
import json
import sys
import zipfile
from collections import OrderedDict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths  # noqa: E402

INSTANCE = pack_paths.INSTANCE
EXPORT = INSTANCE / 'kubejs/exported'
OUT = INSTANCE / 'kubejs/generation_scripts/food/comfort_nourishment.json'
FOOD_TYPES = INSTANCE / 'kubejs/generation_scripts/food/food_types.json'
COMFORT = 'farmersdelight:comfort'
NOURISHMENT = 'farmersdelight:nourishment'

# ── rules (comfort_nourishment.md 9.0, CN-D6 recommended numbers) ──
NOURISH_INPUTS, NOURISH_HUNGER, NOURISH_OUTPUT = 4, 8, 10
COMFORT_INPUTS, COMFORT_HUNGER = 3, 5
FALLBACK_OUTPUT = 10
LONG_FROM, SHORT_SECONDS, LONG_SECONDS = 10, 120, 240
SHORT_COMFORT_UP_TO, SHORT_COMFORT_SECONDS = 7, 60     # Comfort on a food restoring 7 or less
GOLDEN_TYPES = {'golden': 120, 'golden/enchanted': 240}   # food type -> Nourishment seconds, overrides everything
BASE_VALUE = 0          # CN-D9: a non-food input with no recipe value
CRAFT_DEPTH = 2         # crafted non-food inputs: cheapest recipe, two levels deep

BOWL = 'minecraft:bowl'
RULED_CONTAINERS = {BOWL, 'vintagedelight:empty_mason_jar', 'endersdelight:shulker_bowl', 'miners_delight:copper_cup'}
CUP = 'miners_delight:copper_cup'
# cup_conversion bowls this pack has under another id (Farmer's Delight 1.19.2 has no Onion Soup)
CUP_BOWL_IN_PACK = {'farmersdelight:onion_soup': 'brewinandchewin:creamy_onion_soup'}
BOTTLE = 'minecraft:glass_bottle'
CAN_NAMESPACES = ('creativeengineering', 'canned_goods')
RECIPE_ORDER = ('farmersdelight:cooking', 'minecraft:crafting_shapeless', 'minecraft:crafting_shaped')
# recipes a crafted non-food input is valued through
VALUE_TYPES = set(RECIPE_ORDER)


def load(rel):
    with open(EXPORT / rel, encoding='utf-8') as fh:
        return json.load(fh)


def cup_conversions():
    """cup id -> bowl id, from Miner's Delight's own data (the donor jar the backport reads)."""
    out = {}
    for jar in sorted(glob.glob(str(INSTANCE / 'miners_upstream/*.jar'))):
        with zipfile.ZipFile(jar) as z:
            for name in z.namelist():
                if '/cup_conversion/' in name and name.endswith('.json'):
                    for e in json.loads(z.read(name)).get('entries', []):
                        out[e['cup']] = e['bowl']
    return out


class Values:
    def __init__(self, foods, recipes, tags):
        self.hunger = {f['id']: f['nutrition'] for f in foods}
        self.tags = tags
        self.makes = {}
        for r in recipes:
            if r['type'] in VALUE_TYPES and len(r['out']) == 1:
                self.makes.setdefault(r['out'][0]['id'], []).append(r)
        self.memo = {}

    def members(self, raw):
        """Item ids an ingredient slot accepts."""
        ing = json.loads(raw) if isinstance(raw, str) else raw
        if isinstance(ing, list):
            return [m for part in ing for m in self.members(part)]
        if 'item' in ing:
            return [ing['item']]
        if 'tag' in ing:
            return list(self.tags.get(ing['tag'], []))
        for k in ('children', 'ingredients'):
            if k in ing:
                return [m for part in ing[k] for m in self.members(part)]
        if 'base' in ing:
            return self.members(ing['base'])
        return []

    def crafted(self, item, depth):
        if depth <= 0:
            return None
        key = (item, depth)
        if key not in self.memo:
            self.memo[key] = None   # cycle guard
            best = None
            for r in self.makes.get(item, []):
                n = r['out'][0].get('n') or 1
                total = sum(self.slot(s, depth - 1, drop=r.get('container')) for s in r['in'])
                v = total / n
                best = v if best is None else min(best, v)
            self.memo[key] = best
        return self.memo[key]

    def slot(self, raw, depth=CRAFT_DEPTH, drop=None):
        ms = [m for m in self.members(raw) if m != drop]
        food = [self.hunger[m] for m in ms if m in self.hunger]
        if food:
            return min(food)
        made = [v for v in (self.crafted(m, depth) for m in ms) if v is not None]
        return min(made) if made else BASE_VALUE


def is_slot(raw, container):
    ing = json.loads(raw) if isinstance(raw, str) else raw
    return not (isinstance(ing, dict) and ing.get('item') in (container, BOWL))


def pick_recipe(item, recipes_by_out):
    rs = recipes_by_out.get(item, [])
    for t in RECIPE_ORDER:
        hits = sorted((r for r in rs if r['type'] == t), key=lambda r: r['id'])
        if hits:
            return hits[0]
    return None


def decide(inputs, in_hunger, out_hunger):
    if inputs >= NOURISH_INPUTS and in_hunger >= NOURISH_HUNGER and out_hunger >= NOURISH_OUTPUT:
        effect = 'nourishment'
    elif inputs >= COMFORT_INPUTS and in_hunger >= COMFORT_HUNGER:
        effect = 'comfort'
    elif out_hunger >= FALLBACK_OUTPUT:
        effect = 'comfort'
    else:
        return None, 0
    if effect == 'comfort' and out_hunger <= SHORT_COMFORT_UP_TO:
        return effect, SHORT_COMFORT_SECONDS
    return effect, LONG_SECONDS if out_hunger >= LONG_FROM else SHORT_SECONDS


def golden_foods():
    """food id -> Nourishment seconds for the golden food types (item_dict keys in food_types.json)."""
    _, key_val, _ = pack_paths.parse_item_dict()
    with open(FOOD_TYPES, encoding='utf-8') as fh:
        types = json.load(fh)['types']
    out = {}
    for t, seconds in GOLDEN_TYPES.items():
        for k in types.get(t, []):
            fid = k if ':' in k else key_val.get(k)
            if fid:
                out[fid] = seconds
    return out


def current(f):
    ids = {e['effect'] for e in f.get('effects') or []}
    c, n = COMFORT in ids, NOURISHMENT in ids
    return 'both' if c and n else 'comfort' if c else 'nourishment' if n else None


def build():
    foods = load('food/foods.json')['foods']
    recipes = load('food/recipes.json')['recipes']
    blocks = load('food/food_blocks.json')['blocks']
    tags = load('tags/item_tags.json')
    values = Values(foods, recipes, tags)
    by_out = {}
    for r in recipes:
        for o in r['out']:
            by_out.setdefault(o['id'], []).append(r)
    feast_of = {b['serving']: b['id'] for b in blocks if b.get('serving')}
    cups = cup_conversions()
    by_id = {f['id']: f for f in foods}

    def inputs_of(fid, container):
        src = feast_of.get(fid, fid)
        r = pick_recipe(src, by_out)
        if r is None and src != fid:
            r = pick_recipe(fid, by_out)
        if r is None:
            return None
        slots = [s for s in r['in'] if is_slot(s, container)]
        drop = r.get('container')
        return len(slots), sum(values.slot(s, drop=drop) for s in slots), r, src != fid

    golden = golden_foods()
    ruled, keep, bottles = OrderedDict(), [], []
    for f in sorted(foods, key=lambda f: f['id']):
        fid, rem = f['id'], f.get('remainder')
        if fid.split(':')[0] in CAN_NAMESPACES:
            continue
        if fid in golden:
            ruled[fid] = dict(effect='nourishment', seconds=golden[fid], inputs=0, input_hunger=0,
                              output=f['nutrition'], now=current(f), recipe='golden', feast=False, name=f.get('name'))
            continue
        if rem == BOTTLE:
            bottles.append(fid)
            continue
        if rem not in RULED_CONTAINERS:
            # every other food keeps only its Comfort; a light one gets the short Comfort
            if current(f) in ('comfort', 'both') and f['nutrition'] <= SHORT_COMFORT_UP_TO:
                ruled[fid] = dict(effect='comfort', seconds=SHORT_COMFORT_SECONDS, inputs=0, input_hunger=0,
                                  output=f['nutrition'], now=current(f), recipe='kept Comfort', feast=False,
                                  name=f.get('name'))
            continue
        if rem == CUP:
            bowl = cups.get(fid)
            bowl = CUP_BOWL_IN_PACK.get(bowl, bowl)
            got = inputs_of(bowl, BOWL) if bowl in by_id else None
        else:
            got = inputs_of(fid, rem)
        if got is None:
            keep.append(fid)
            continue
        n, hunger, r, feast = got
        effect, seconds = decide(n, hunger, f['nutrition'])
        ruled[fid] = dict(effect=effect, seconds=seconds, inputs=n, input_hunger=hunger, output=f['nutrition'],
                          now=current(f), recipe=r['id'], feast=feast, name=f.get('name'))
    return ruled, keep, bottles, by_id


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--list', action='store_true', help='print every ruled food with its inputs')
    args = ap.parse_args()

    ruled, keep, bottles, by_id = build()
    first_key = pack_paths.item_dict_first_keys(pack_paths.item_dict_text(), tags=False, skip_keys=('na', 'none'))
    key = lambda fid: first_key.get(fid, fid)

    out = OrderedDict()
    out['_readme'] = ('Generated by python3 tools/gen_comfort_nourishment.py; edit the rules there, not this file. '
                      'set: food -> Comfort / Nourishment and seconds ("none" = neither); keep: ruled foods with no '
                      'recipe found, left as they are; strip: both effects removed (bottles). Every other food loses '
                      'Nourishment at runtime. Keys are item_dict keys, raw ids where the item has no key. Read by '
                      'Creative Delight creativeengineering/comfort_nourishment.js and tools/gen_can_types.py; restart '
                      'to apply.')
    out['set'] = OrderedDict((key(fid), OrderedDict([('effect', d['effect'] or 'none'), ('seconds', d['seconds'])]))
                             for fid, d in ruled.items())
    out['keep'] = [key(fid) for fid in keep]
    out['strip'] = [key(fid) for fid in bottles]
    OUT.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(out, indent='\t', ensure_ascii=False) + '\n'
    old = OUT.read_text(encoding='utf-8') if OUT.exists() else None
    OUT.write_text(text, encoding='utf-8')

    counts = OrderedDict()
    for d in ruled.values():
        k = (d['now'] or '-', d['effect'] or '-')
        counts[k] = counts.get(k, 0) + 1
    print('%d ruled foods (%d unchanged), %d kept (no recipe found), %d bottles stripped'
          % (len(ruled), sum(1 for d in ruled.values() if d['now'] == d['effect']), len(keep), len(bottles)))
    for (now, rule), n in sorted(counts.items()):
        print('  %-11s -> %-11s %3d' % (now, rule, n))
    others = [f for f in by_id.values() if f['id'] not in ruled and f['id'] not in keep and f.get('remainder') != BOTTLE
              and f['id'].split(':')[0] not in CAN_NAMESPACES and current(f) in ('nourishment', 'both')]
    print('%d other foods lose Nourishment at runtime' % len(others))
    if keep:
        print('kept, hand check (no recipe found): ' + ', '.join(keep))
    if args.list:
        for fid, d in ruled.items():
            print('  %-11s %-11s %3d s  in %d / %2d  out %2d  %s  (%s%s)' % (
                d['now'] or '-', d['effect'] or '-', d['seconds'], d['inputs'], d['input_hunger'], d['output'],
                fid, d['recipe'], ', feast' if d['feast'] else ''))
    print('wrote %s%s' % (OUT.relative_to(INSTANCE), '' if old != text else ' (unchanged)'))


if __name__ == '__main__':
    main()
