#!/usr/bin/env python3
"""Names for every Display Delight block -> kubejs/assets/displaydelight/lang/en_us.json.

Display Delight 1.2.0 registers ~205 placed-food blocks (121 of them with a block item that JEI lists)
but its lang file only names the two plates, so the rest show raw `block.displaydelight.*` keys in
JEI and WTHIT. Each block's loot table drops the food it holds (plus the plate), so the name comes
from that food's in-game name (tools/lang_index.py, which includes our renames):

    beef_stew                     -> Placed Beef Stew
    plated_hamburger              -> Plated Hamburger
    small_plated_apple_pie_slice  -> Small Plated Apple Pie Slice

When the food's mod isn't installed (corn_delight, expandeddelight, largemeals...), the name is built
from the block id without its mod prefix, which also skips two typos in DD's loot tables
(festive_delight:festive_chiken, pineapple_delight:pineapple_pie_side).

    python3 tools/gen_display_delight_names.py           write the lang file
    python3 tools/gen_display_delight_names.py --check   exit 1 if the file is out of date
"""
import argparse
import json
import re
import sys
import zipfile

import lang_index

OUT = lang_index.INSTANCE / 'kubejs/assets/displaydelight/lang/en_us.json'
JAR_GLOB = 'displaydelight-*.jar'
# Block-id prefixes DD uses for each optional food mod.
PREFIXES = ('ad_', 'cd_', 'ctd_', 'df_', 'ed_', 'fd_', 'lm_', 'od_', 'pd_')
PLATES = {'displaydelight:food_plate', 'displaydelight:small_food_plate'}
# Hand-picked names, where the rule gives something odd.
OVERRIDES = {
    'bowl': 'Placed Bowl',
}


def title(path):
    small = {'and', 'of', 'on', 'a', 'with'}
    words = path.split('_')
    return ' '.join(w if (i and w in small) else w.capitalize() for i, w in enumerate(words))


def strip_prefix(block):
    for p in PREFIXES:
        if block.startswith(p):
            return block[len(p):]
    return block


def read_jar():
    jars = sorted(lang_index.MODS.glob(JAR_GLOB))
    if not jars:
        sys.exit('no %s in mods/' % JAR_GLOB)
    blocks, drops, own = [], {}, {}
    with zipfile.ZipFile(jars[-1]) as z:
        for name in z.namelist():
            m = re.match(r'^assets/displaydelight/blockstates/([a-z0-9_]+)\.json$', name)
            if m:
                blocks.append(m.group(1))
            m = re.match(r'^data/displaydelight/loot_tables/blocks/([a-z0-9_]+)\.json$', name)
            if m:
                drops[m.group(1)] = re.findall(r'"name"\s*:\s*"([^"]+)"', z.read(name).decode())
        own = lang_index.parse_lang(z.read('assets/displaydelight/lang/en_us.json'))
    return jars[-1].name, sorted(blocks), drops, own


def food_name(item, names, namespaces):
    ns, path = item.split(':', 1)
    if ns not in namespaces:
        return None
    for key in ('item.%s.%s' % (ns, path), 'block.%s.%s' % (ns, path)):
        if key in names:
            return names[key]
    return None


def build():
    jar, blocks, drops, own = read_jar()
    names, _ = lang_index.build(skip_kubejs_ns=('displaydelight',))
    namespaces = lang_index.mod_namespaces()
    out, fallback = {}, []
    for b in blocks:
        key = 'block.displaydelight.' + b
        if key in own:
            continue
        if b in OVERRIDES:
            out[key] = OVERRIDES[b]
            continue
        food = [d for d in drops.get(b, []) if d not in PLATES]
        stem = strip_prefix(b)
        if stem.startswith('small_plated_'):
            lead, stem = 'Small Plated ', stem[len('small_plated_'):]
        elif stem.startswith('plated_'):
            lead, stem = 'Plated ', stem[len('plated_'):]
        else:
            lead = 'Placed '
        name = food_name(food[0], names, namespaces) if food else None
        if name is None:
            name = title(stem)
            fallback.append(b)
        out[key] = lead + name
    return jar, out, fallback


def render(out):
    return json.dumps(dict(sorted(out.items())), indent=2, ensure_ascii=False) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    jar, out, fallback = build()
    text = render(out)
    current = OUT.read_text() if OUT.exists() else ''
    if args.check:
        if current != text:
            print('%s is out of date, run python3 tools/gen_display_delight_names.py' % OUT.relative_to(lang_index.INSTANCE))
            return 1
        print('up to date (%d names)' % len(out))
        return 0
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(text)
    print('%s: wrote %d names to %s (%d from the block id, food mod not installed)'
          % (jar, len(out), OUT.relative_to(lang_index.INSTANCE), len(fallback)))
    return 0


if __name__ == '__main__':
    sys.exit(main())
