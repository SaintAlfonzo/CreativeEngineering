#!/usr/bin/env python3
"""Names for every Display Delight block -> kubejs/assets/displaydelight/lang/en_us.json.

Display Delight 1.2.0 named only the two plates; the pack's 1.7.0 port names every block after its id
("Beef Stew Block"). The pack names each food block after the food it holds, in game (tools/lang_index.py, which
includes our renames); the plates keep the jar's names. The food comes from tools/display_delight_jar.py (1.2.0: the
block's loot table drop; the port: the block name):

    beef_stew                     -> Placed Beef Stew
    plated_hamburger              -> Plated Hamburger
    small_plated_apple_pie_slice  -> Small Plated Apple Pie Slice

When the food isn't installed (a 1.20-only food, or a 1.2.0 block for a mod the pack does not have), the name is
built from the block id without its mod prefix.

    python3 tools/gen_display_delight_names.py           write the lang file
    python3 tools/gen_display_delight_names.py --check   exit 1 if the file is out of date
"""
import argparse
import json
import re
import sys
import display_delight_jar as ddj
import lang_index

OUT = lang_index.INSTANCE / 'kubejs/assets/displaydelight/lang/en_us.json'
# Hand-picked names, where the rule gives something odd.
OVERRIDES = {
    'bowl': 'Placed Bowl',
}


def title(path):
    small = {'and', 'of', 'on', 'a', 'with'}
    words = path.split('_')
    return ' '.join(w if (i and w in small) else w.capitalize() for i, w in enumerate(words))


def read_jar():
    """(jar name, every food block id the jar has a blockstate for, {block: food} links of registered ones)."""
    path = ddj.jar_path()
    with ddj.open_jar() as z:
        blocks = [m.group(1) for m in (re.match(r'^assets/displaydelight/blockstates/([a-z0-9_-]+)\.json$', n)
                                       for n in z.namelist()) if m and m.group(1) not in ddj.PLATES]
        links = ddj.links(z)
    return path.name, sorted(blocks), links


def food_name(item, names, namespaces):
    ns, path = item.split(':', 1)
    if ns not in namespaces:
        return None
    for key in ('item.%s.%s' % (ns, path), 'block.%s.%s' % (ns, path)):
        if key in names:
            return names[key]
    return None


def build():
    jar, blocks, links = read_jar()
    names, _ = lang_index.build(skip_kubejs_ns=('displaydelight',))
    namespaces = lang_index.mod_namespaces()
    out, fallback = {}, []
    for b in blocks:
        key = 'block.displaydelight.' + b
        if b in OVERRIDES:
            out[key] = OVERRIDES[b]
            continue
        food = [links[b]] if b in links else []
        stem = ddj.split_prefix(b)[1]
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
