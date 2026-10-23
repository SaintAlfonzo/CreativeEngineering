#!/usr/bin/env python3
"""Hunger / saturation audit of every can (feature_writeup/can_parity.md, TODO cans/canning #3).

For each can in kubejs/generation_scripts/food/can_types.json (what is built) it compares three totals, each as
hunger / saturation points (saturation points = hunger x modifier x 2, what AppleSkin shows doubled):
  input   the summed food of a recipe's ingredients. "basic" is the plain recipe; "additive" the cheapest additive
          recipe (salt / sugar count 0, honey bottle and bone broth their catalog value)
  output  servings x one serving (the can's food values)
  target  servings x the family goal: the section's meal (Baked Potato, Fruit Meal, Meat Meal, Fish Meal) for meal
          sections, the source x the section multiplier (unrounded) for the others
and prints the ratios as percents, 100 = equal: in -> out = output / input, out vs target = output / target.
Feast and cake inputs are blocks: valued as their servings x the serving (FEAST_SERVINGS, food_blocks.json).

Writes into can_parity.md: the Food h/s, Craft sum h/s, Canned h/s and Food audit cells of every sheet row with a can,
the two percent columns at the end of each row, and section 4.3 between the CAN AUDIT markers.

    python3 tools/can_audit.py           write can_parity.md
    python3 tools/can_audit.py --check   print the section summary, write nothing

Needs kubejs/exported/food/catalog.json and food_blocks.json (/dumpfood). Rerun after gen_can_types.py.
"""
import argparse
import json
import re
import sys
from collections import OrderedDict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_can_types as G  # noqa: E402

SHEET = G.INSTANCE / 'feature_writeup/can_parity.md'
BEGIN = '<!-- BEGIN CAN AUDIT (tools/can_audit.py, do not edit) -->'
END = '<!-- END CAN AUDIT -->'
COLS = ['In -> out', 'Out vs target']
# placed feast blocks the food_blocks.json export doesn't list: servings read from the mods' block classes
FEAST_SERVINGS = {
    'nethersdelight:stuffed_hoglin': 11,   # StuffedHoglinBlock SERVINGS 0..11 (plates)
    'endersdelight:stuffed_shulker': 4,    # StuffedShulkerBlock.getMaxServings
    'minecraft:cake': 7,                   # CakeBlock bites; valued as Farmer's Delight cake slices
}


def hs(h, s):
    return '%s / %s' % (fmt(h), fmt(s))


def fmt(x):
    return ('%.1f' % x).rstrip('0').rstrip('.') if x != int(x) else str(int(x))


def pct(a, b):
    """a as a percent of b, per component: 'h% / s%'."""
    parts = []
    for x, y in zip(a, b):
        parts.append('-' if not y else '%d%%' % round(100.0 * x / y))
    return ' / '.join(parts)


class Audit:
    def __init__(self):
        self.foods = {f['id']: f for f in G.load_json(G.EXPORT / 'catalog.json')['foods']}
        self.keys = G.parse_dict(G.DICT.read_text(encoding='utf-8'))
        self.cans = G.load_json(G.OUT)['cans']
        self.blocks = dict(FEAST_SERVINGS)
        fb = G.EXPORT / 'food_blocks.json'
        if fb.exists():
            for b in G.load_json(fb)['blocks']:
                if b.get('servings'):
                    self.blocks[b['id']] = b['servings']

    def food(self, key):
        f = self.foods.get(self.keys.get(key))
        if not f:
            return None
        pts = f['saturation'] if f.get('saturation') is not None else f['nutrition'] * f['saturationModifier'] * 2
        return (f['nutrition'], pts)

    def source_value(self, can, key):
        """One item of a source slot: a food, a feast / cake block (servings x the serving) or a tag standing for
        the can's first source."""
        v = self.food(key)
        if v:
            return v
        n = self.blocks.get(self.keys.get(key))
        serving = self.food(can['sources'][0]) if can['sources'] else None
        if n and serving:
            return (n * serving[0], n * serving[1])
        return serving

    def recipe_input(self, can, rec):
        h = s = 0.0
        for g, inp in enumerate(rec['inputs']):
            if g == 0:
                v = self.source_value(can, inp['any'][0])
            else:   # additive: the cheapest alternative, non-foods (salt, sugar) count 0
                vals = [self.food(k) or (0, 0) for k in inp['any']]
                v = min(vals, key=lambda x: x[0] + x[1])
            if v is None:
                return None
            h += v[0] * inp['n']
            s += v[1] * inp['n']
        return (h, s)

    def output(self, can):
        if can.get('food') is False:
            return None
        return (can['servings'] * can['hunger'], can['servings'] * can['hunger'] * can['modifier'] * 2)

    def target(self, can):
        spec = G.SECTIONS[can['section']]
        if can.get('food') is False:
            return None
        if 'meal' in spec:
            return (can['servings'] * spec['meal'][0], can['servings'] * spec['meal'][1])
        src = self.food(can['sources'][0]) if can['sources'] else None
        if not src:
            return None
        return (can['servings'] * src[0] * spec['mult'], can['servings'] * src[1] * spec['mult'])

    def row(self, key):
        can = self.cans[key]
        recs = can['recipes']
        basic = next((r for r in recs if not r['suffix']), recs[0] if recs else None)
        adds = [r for r in recs if r is not basic]
        r = OrderedDict(key=key, can=can, basic=basic)
        r['in'] = self.recipe_input(can, basic) if basic else None
        add_in = [(self.recipe_input(can, a), a) for a in adds]
        add_in = [x for x in add_in if x[0]]
        r['add'] = min(add_in, key=lambda x: x[0][0] + x[0][1]) if add_in else None
        r['out'] = self.output(can)
        r['target'] = self.target(can)
        r['io'] = pct(r['out'], r['in']) if r['in'] and r['out'] else 'n/a'
        r['io_add'] = pct(r['out'], r['add'][0]) if r['add'] and r['out'] else '-'
        r['ot'] = pct(r['out'], r['target']) if r['target'] and r['out'] else 'n/a'
        return r


def recipe_text(rec):
    return ' + '.join('%d %s' % (inp['n'], '/'.join(inp['any'])) for inp in rec['inputs'])


def audit_block(a, rows):
    out = [BEGIN, '']
    out.append('Generated by `tools/can_audit.py` from can_types.json (what is built) and the food export. Totals are '
               'hunger / saturation points for the whole can; percents are per component, 100% = equal. **In -> out** = '
               'output / input (above 100% canning adds food), **out vs target** = output / the family goal (servings '
               'x the section meal, or the source x the section multiplier). The additive column uses the cheapest '
               'additive (salt and sugar count 0). Feasts and cake: the block\'s servings x the serving. `n/a`: no '
               'recipe (values-only Canned Goods cans), not a food (Canned Dough), or no value source (Powdered Milk).')
    out.append('')
    out.append('| Section | Cans | In -> out (basic) | In -> out (additive) | Out vs target |')
    out.append('|---|---:|---|---|---|')

    def rng(vals, idx):
        nums = []
        for v in vals:
            parts = v.split(' / ')
            if len(parts) == 2 and parts[idx].endswith('%'):
                nums.append(int(parts[idx][:-1]))
        return (min(nums), max(nums)) if nums else None

    def span(vals):
        h, s = rng(vals, 0), rng(vals, 1)
        if not h:
            return 'n/a'
        f = lambda x: '%d%%' % x[0] if x[0] == x[1] else '%d to %d%%' % x  # noqa: E731
        return 'h %s, s %s' % (f(h), f(s))
    by = OrderedDict()
    for r in rows:
        by.setdefault(r['can']['section'], []).append(r)
    for sec, rs in by.items():
        out.append('| %s | %d | %s | %s | %s |' % (sec, len(rs), span([r['io'] for r in rs]), span([r['io_add'] for r in rs]),
                                                  span([r['ot'] for r in rs])))
    for sec, rs in by.items():
        out += ['', '**%s**' % sec, '',
                '| Can | Basic recipe | Input | Output | In -> out | Additive input | In -> out (additive) | Target | Out vs target |',
                '|---|---|---|---|---|---|---|---|---|']
        for r in rs:
            out.append('| %s | %s | %s | %s | %s | %s | %s | %s | %s |' % (
                r['key'], recipe_text(r['basic']) if r['basic'] else 'no recipe',
                hs(*r['in']) if r['in'] else 'n/a', hs(*r['out']) if r['out'] else 'n/a', r['io'],
                hs(*r['add'][0]) if r['add'] else '-', r['io_add'],
                hs(*r['target']) if r['target'] else 'n/a', r['ot']))
    out += ['', END]
    return '\n'.join(out)


def can_for_items(a):
    """item id -> can key; a can with recipes wins over a values-only one for the same food."""
    m = {}
    for key, c in a.cans.items():
        ids = [a.keys.get(k) for k in c['sources']]
        for iid in ids:
            if iid and (iid not in m or (not a.cans[m[iid]]['recipes'] and c['recipes'])):
                m[iid] = key
    return m


def fill_sheet(text, a, rows_by_key):
    lines = text.split('\n')
    item_can = can_for_items(a)
    stop = next(n for n, l in enumerate(lines) if l.startswith('## 3.'))
    for n in range(stop):
        l = lines[n]
        if l.startswith('| Want |'):
            cells = [c.strip() for c in l.strip().strip('|').split('|')]
            if cells[-1] != COLS[-1]:
                lines[n] = l.rstrip() + ' %s |' % ' | '.join(COLS)
                lines[n + 1] = lines[n + 1].rstrip() + '---|---|'
            continue
        if not l.startswith('| ') or l.startswith('| ['):
            continue
        cells = [c.strip() for c in l.strip().strip('|').split('|')]
        if len(cells) not in (13, 15):
            continue
        cells = cells[:13]
        m = re.search(r'`([^`]+)`', cells[1])
        want = cells[0].lower().startswith('x')
        iid = m.group(1) if m else None
        if iid and iid.endswith('*'):   # one row for several ids (bell pepper colours, hoglin plates)
            iid = next((x for x in sorted(item_can) if x.startswith(iid[:-1])), iid)
        key = item_can.get(iid) if iid else None
        if want and key and key in rows_by_key:
            r = rows_by_key[key]
            src = a.food(next((k for k in r['can']['sources'] if a.keys.get(k) == iid), r['can']['sources'][0])) \
                if r['can']['sources'] else None
            cells[10] = hs(*src) if src else 'n/a'
            cells[11] = hs(*r['in']) if r['in'] else 'n/a'
            cells[12] = hs(*r['out']) if r['out'] else 'n/a'
            cells[8] = 'x'
            extra = [r['io'], r['ot']]
        elif want:
            extra = ['-', '-']
        else:
            extra = ['n/a', 'n/a']
        lines[n] = '| ' + ' | '.join(cells + extra) + ' |'
    text = '\n'.join(lines)
    return text


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    a = Audit()
    rows = [a.row(k) for k in a.cans]
    rows_by_key = {r['key']: r for r in rows}
    block = audit_block(a, rows)
    for l in block.split('\n'):
        if l.startswith('| ') and not l.startswith('| Section') and not l.startswith('| Can') and l.count('|') == 6:
            print(l)
    if args.check:
        print('check only, nothing written')
        return
    text = SHEET.read_text(encoding='utf-8')
    text = fill_sheet(text, a, rows_by_key)
    if BEGIN in text:
        i, j = text.index(BEGIN), text.index(END) + len(END)
        text = text[:i] + block + text[j:]
    else:
        print('no CAN AUDIT markers in can_parity.md 4.3, add them first')
        sys.exit(1)
    SHEET.write_text(text, encoding='utf-8')
    print('wrote %s (%d cans)' % (SHEET.name, len(rows)))


if __name__ == '__main__':
    main()
