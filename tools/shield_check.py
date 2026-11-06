#!/usr/bin/env python3
"""Shields: offline checks for the tier shields and the damage tags they read.

Reads the live [shields] section of config/creativeequipment-common.toml, the code defaults in
Creative Equipment's ShieldTier.java and the damage tag files in patcherMod, then prints:

  1. the effective ladder per tier (what the game uses right now, from the toml)
  2. where the toml differs from the ShieldTier.java defaults (a fresh toml would get the code values)
  3. problems: unknown repair items, bad effect entries, dangling #includes, tiers with no recipe
  4. how every message id in the five category tags classifies (first match wins, same order as
     ShieldCategory), listing only ids that sit in more than one of them

Static only: tags that come from live DamageSource flags (melee from the attacker, magic/fire flags
on modded sources) are not visible here. Use [damage_tags] logSeenSources in game for those.

    python3 tools/shield_check.py        exit code 1 when section 3 found a problem
"""
import json
import re
import sys
from pathlib import Path

import toml

INSTANCE = Path(__file__).resolve().parent.parent
# [shields] moved to Creative Equipment's file on 2026-11-04; its first boot carries the section over, so read the old
# file only until then.
TOML = INSTANCE / 'config/creativeequipment-common.toml'
if not TOML.exists():
    TOML = INSTANCE / 'config/creativeengineering-common.toml'
PM = INSTANCE / 'development/creativeCommon/src/main'
TIER_JAVA = INSTANCE / 'development/creativeEquipment/src/main/java/com/SaintAlfonzo/creativeequipment/shield/ShieldTier.java'
TAGS = PM / 'resources/data/creativeengineering/damage_tags'
ITEM_DICT = INSTANCE / 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js'
RECIPES = [INSTANCE / 'kubejs/data/creativeequipment/kubejs/equipment/shield.js',
           INSTANCE / 'kubejs/data/creativeequipment/kubejs/equipment/end_gear.js']

GROUPS = ('physical', 'elemental', 'magic')
ELEMENTS = ('fire', 'electric', 'cold', 'chemical')            # ElementalKind declaration order
CATEGORIES = ('explosion', 'projectile', 'magic', 'elemental', 'melee')  # ShieldCategory order, OTHER last
ITEM_KEY = {'iron': 'shield'}                                  # item_dict key per tier, else ce_<tier>_shield

problems = []


def code_defaults():
    """Parse the enum constants in ShieldTier.java (id, durability, repair, phys, elem, magic, ..., kb, effects)."""
    src = TIER_JAVA.read_text()
    out = {}
    pat = re.compile(r'^\s*[A-Z]+\("(\w+)",\s*(\d+),\s*"([^"]*)",\s*(\d+),\s*(new int\[\]\{[^}]*\}|\d+),\s*(\d+),'
                     r'\s*(true|false),\s*Rarity\.\w+,\s*(\d+)(?:,\s*"([^"]*)",\s*"([^"]*)")?', re.M)
    for m in pat.finditer(src):
        tid, dur, rep, phys, elem, magic, _fp, kb, shielder, attacker = m.groups()
        if elem.startswith('new'):
            breakdown = [int(x) for x in re.findall(r'-?\d+', elem)]
            blanket = 0
        else:
            breakdown = [-1] * 4
            blanket = int(elem)
        out[tid] = {
            'durability': int(dur), 'repairItem': rep, 'knockbackThreshold': int(kb),
            'reduction': {'physical': int(phys), 'elemental': blanket, 'magic': int(magic)},
            'elemental_breakdown': dict(zip(ELEMENTS, breakdown)),
            'shielderEffects': shielder or '', 'attackerEffects': attacker or '',
        }
    return out


def effective_reductions(cfg):
    bd = cfg.get('elemental_breakdown', {})
    red = cfg.get('reduction', {})
    if any(bd.get(k, -1) >= 0 for k in ELEMENTS):
        elem = '/'.join(f'{k[:4]} {max(bd.get(k, -1), 0)}' for k in ELEMENTS)
    else:
        elem = f"{red.get('elemental', 0)} (blanket)"
    return red.get('physical', 0), elem, red.get('magic', 0)


def check_effects(tier, field, raw):
    for entry in filter(None, (e.strip() for e in raw.split(','))):
        f = entry.split(';')
        ok = len(f) >= 2 and re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_/.-]+', f[0].strip()) and f[1].strip().isdigit()
        if ok and len(f) > 3:
            names = set(CATEGORIES) | set(GROUPS) | {'other'}
            ok = all(n.strip() in names for n in f[3].split('|') if n.strip())
        if not ok:
            problems.append(f'[shields.{tier}].{field}: bad entry "{entry}"')


def load_tags():
    tags = {}
    for p in sorted(TAGS.glob('*.json')):
        tags[p.stem] = json.loads(p.read_text()).get('values', [])
    return tags


def members(tags, name, seen=None):
    """Message ids (and globs) in a tag, #includes resolved."""
    seen = seen if seen is not None else set()
    if name in seen:
        return set()
    seen.add(name)
    out = set()
    for v in tags.get(name, []):
        if v.startswith('#'):
            inc = v[1:].split(':', 1)[-1]
            if inc not in tags:
                problems.append(f'damage_tags/{name}.json: #include {v} has no file')
                continue
            out |= members(tags, inc, seen)
        else:
            out.add(v)
    return out


def main():
    live = toml.load(TOML).get('shields')
    if not live:
        print(f'no [shields] section in {TOML} (the jar has not run yet?)')
        return 1
    defaults = code_defaults()
    tiers = list(defaults)  # ladder order as declared
    item_dict = ITEM_DICT.read_text()
    recipe_src = '\n'.join(p.read_text() for p in RECIPES)

    print('== 1. Effective ladder (live toml)\n')
    print(f"global: enabled={live.get('enabled')} wearThreshold={live.get('wearThreshold')} "
          f"disableCooldownTicks={live.get('disableCooldownTicks')} facing={live.get('facing')}")
    print(f"unblockableEffectTicks={live.get('unblockableEffectTicks', '(not in toml yet: code default, 7 effects)')}\n")
    print(f"{'tier':10} {'dur':>5} {'phys':>4}  {'elemental':32} {'magic':>5} {'kb':>3} {'burn':>4} {'thrn':>4}  repair / effects")
    for t in tiers:
        c = live.get(t, {})
        phys, elem, magic = effective_reductions(c)
        wear = c.get('durabilityCost', {})
        extra = '' if all(wear.get(g, 100) == 100 for g in GROUPS) else f' wear={wear}'
        eff = ' '.join(x for x in (c.get('shielderEffects'), c.get('attackerEffects') and '-> ' + c['attackerEffects']) if x)
        burn = c.get('blocksBurnTicks', tiers.index(t) >= tiers.index('netherite'))  # code defaults when not in toml yet
        thorn = c.get('blocksContactTicks', tiers.index(t) >= tiers.index('diamond'))
        print(f"{t:10} {c.get('durability', '?'):>5} {phys:>4}  {elem:32} {magic:>5} {c.get('knockbackThreshold', '?'):>3} {'yes' if burn else 'no':>4} {'yes' if thorn else 'no':>4}  "
              f"{c.get('repairItem', '')} {eff}{extra}")

    print('\n== 2. Toml vs ShieldTier.java defaults\n')
    drift = 0
    for t in tiers:
        c, d = live.get(t, {}), defaults[t]
        for key in ('durability', 'repairItem', 'knockbackThreshold', 'shielderEffects', 'attackerEffects'):
            if c.get(key) != d[key]:
                print(f'{t}.{key}: toml {c.get(key)!r}, code {d[key]!r}'); drift += 1
        for sect in ('reduction', 'elemental_breakdown'):
            for k, v in d[sect].items():
                if c.get(sect, {}).get(k) != v:
                    print(f'{t}.{sect}.{k}: toml {c.get(sect, {}).get(k)!r}, code {v!r}'); drift += 1
    print(f'{drift} difference(s)' if drift else 'none')

    for t in tiers:
        c = live.get(t, {})
        rep = c.get('repairItem', '')
        if rep and f"'{rep}'" not in item_dict:
            problems.append(f'[shields.{t}].repairItem {rep} is not in item_dict.js')
        check_effects(t, 'shielderEffects', c.get('shielderEffects', ''))
        check_effects(t, 'attackerEffects', c.get('attackerEffects', ''))
        key = ITEM_KEY.get(t, f'ce_{t}_shield')
        if not re.search(rf'Item\.of\(i\.{key}\b|\[i\.{key},', recipe_src):
            problems.append(f'{t}: no recipe producing i.{key} in shield.js / end_gear.js')

    tags = load_tags()
    resolved = {name: members(tags, name) for name in tags}

    print('\n== 3. Problems\n')
    print('\n'.join(problems) if problems else 'none')

    print('\n== 4. Message ids in more than one category tag (static; first match wins)\n')
    by_id = {}
    for cat in CATEGORIES:
        for mid in resolved.get(cat, ()):
            by_id.setdefault(mid, []).append(cat)
    shared = {m: cats for m, cats in by_id.items() if len(cats) > 1}
    for mid in sorted(shared):
        cats = shared[mid]
        win = cats[0]
        note = ''
        if win == 'elemental':
            kind = next((k for k in ELEMENTS if mid in resolved.get(k, ())), None)
            note = f' ({kind})'
        print(f'{mid:40} {" + ".join(cats):35} -> {win}{note}')
    print(f'\n{len(by_id)} ids across the five category tags, {len(shared)} shared')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
