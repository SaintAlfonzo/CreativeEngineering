#!/usr/bin/env python3
"""Display-name audit (feature_writeup/names.md, NM-): raw lang keys and capitalization.

    python3 tools/name_audit.py            write kubejs/generation_scripts/names/name_fixes.json + the
                                           generated sections of feature_writeup/names.md
    python3 tools/name_audit.py --print    counts and every proposed change, nothing written
    python3 tools/name_audit.py --check    exit 1 when name_fixes.json is out of date

Input: kubejs/exported/display_names.json from the client dump (/cedumpnames in game, or
[debug.names] dumpAtTitle on a scratch client; Creative Devtools dump/DisplayNameDump). Every entry
carries the name the game shows and the pack + namespace whose lang file set its key.

Rules: kubejs/generation_scripts/names/name_rules.json (hand-written):
  names        {key: name}     exact names; win over everything below
  keep         [key]           leave this name alone (a capitalization the checker would change)
  keepWords    [word]          words whose case is right as written (pH, mB, x2, iPod...)
  minorForce / minorKeep       Title Case small words (see below)
  skip         [regex]         keys never fixed (technical blocks, debug items); still reported

What counts as wrong:
  raw      no lang key, or the shown name is itself a key (block.mod.id) or a bare id (copper_axe).
           Fixed with a name built from the id (Title Case), from the fluid for fluid blocks, from the
           other forms for potions, or from `names`.
  case     Title Case like vanilla: every word capitalized except small words (a, of, and, the...)
           that are not first or last ("Block of Iron", "Flint and Steel"); hyphen parts too
           ("Half-Slab"); spaces trimmed and collapsed. Formatting codes (§) are only reported.

Where fixes go: every fix is a ClientEvents.lang entry (kubejs/client_scripts/name_fixes.js) in the
namespace whose lang file set the key, because the game merges lang per namespace (DisplayNameDump
explains why). The KubeJS pack sits above mod jars and generated packs, so the fix wins. It loses to
the resource-pack zips enabled in options.txt and can't sensibly override our own KubeJS lang:
names those set are listed under "fix at the source" instead.
"""
import argparse
import json
import re
import sys
from datetime import datetime
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
DUMP = INSTANCE / 'kubejs/exported/display_names.json'
RULES = INSTANCE / 'kubejs/generation_scripts/names/name_rules.json'
FIXES = INSTANCE / 'kubejs/generation_scripts/names/name_fixes.json'
WRITEUP = INSTANCE / 'feature_writeup/names.md'

KINDS = ('item', 'block', 'effect', 'enchantment', 'entity', 'fluid', 'potion', 'profession')
KEY_LIKE = re.compile(r'^[a-z0-9_\-]+(\.[a-z0-9_\-/:]+){1,}$')
BARE_ID = re.compile(r'^[a-z0-9]+(_[a-z0-9]+)+$')
FORMAT = re.compile('§.')
POTION_FORMS = {
    'potion': ('Potion of ', ''),
    'splash': ('Splash Potion of ', ''),
    'lingering': ('Lingering Potion of ', ''),
    'arrow': ('Arrow of ', ''),
}


def load_rules():
    rules = json.loads(RULES.read_text())
    rules.setdefault('names', {})
    rules['keep'] = set(rules.get('keep', []))
    rules['keepWords'] = set(rules.get('keepWords', []))
    rules['minorForce'] = set(rules.get('minorForce', []))
    rules['minorKeep'] = set(w.lower() for w in rules.get('minorKeep', []))
    rules['skip'] = [re.compile(p) for p in rules.get('skip', [])]
    return rules


# ---------------------------------------------------------------------------------------------
# Title Case
# ---------------------------------------------------------------------------------------------

def fix_part(part, first, last, rules):
    """One word, hyphen part or slash part. first/last: position in the name (small words keep their
    capital there)."""
    if part.lower() in rules['minorKeep']:
        return part                                  # o', 'n, de...: either case, punctuation and all
    m = re.match(r'^([(\[{"\'“]*)(.*?)([)\]}"\'”.,:;!?]*)$', part)
    pre, core, post = m.group(1), m.group(2), m.group(3)
    if not core or not core[0].isalpha() or core in rules['keepWords']:
        return part
    low = core.lower()
    if low in rules['minorKeep'] or re.match(r"^o'", low):
        return part                                  # foreign particles and o'Lantern: either case
    if low in rules['minorForce'] and not first and not last and not pre:
        if core[0].isupper() and core[1:] == core[1:].lower():
            return pre + low + post                  # "Block Of Iron" -> "Block of Iron"
        return part
    if core[0].islower():
        return pre + core[0].upper() + core[1:] + post
    return part


def title_case(name, rules):
    words = name.split()
    out = []
    for i, w in enumerate(words):
        first = i == 0 or out[-1].endswith((':', '-', '(', '/'))
        last = i == len(words) - 1
        if w in rules['keepWords']:
            out.append(w)
            continue
        # hyphen and slash parts are words of their own ("High-Grade", "Head/Foot")
        parts = re.split(r'([-/])', w)
        fixed = [p if p in ('', '-', '/') else fix_part(p, first and j == 0, last and j == len(parts) - 1, rules)
                 for j, p in enumerate(parts)]
        out.append(''.join(fixed))
    return ' '.join(out)


def name_from_id(path, rules):
    words = re.split(r'[_/]+', path)
    return title_case(' '.join(w for w in words if w), rules)


# ---------------------------------------------------------------------------------------------
# Audit
# ---------------------------------------------------------------------------------------------

def is_raw(row):
    """The text the game shows is a lang key or a bare id. A missing key alone isn't enough: IE cartridges,
    armor trim templates and Everything Copper tools build their names in code."""
    name = row['name'].strip()
    return name == row['key'] or bool(KEY_LIKE.match(name)) or bool(BARE_ID.match(name))


def classify_source(row):
    """'ours' = a KubeJS lang (namechanges.js or kubejs/assets), 'zip' = a resource-pack zip, else None."""
    pack = row.get('pack') or ''
    if pack.startswith('file/') or pack.endswith('.zip'):
        return 'zip'
    if pack.startswith('KubeJS'):            # 'KubeJS Resource Pack [assets]': kubejs/assets + ClientEvents.lang
        return 'ours'
    return None


def derived_name(kind, ns, path, named):
    """A raw entry's name taken from a related entry that has one, or None."""
    def first(table, ids):
        for i in ids:
            if i in table:
                return table[i]
        return None
    if kind == 'block':
        base = re.sub(r'_(fluid_)?block$', '', path)
        hit = first(named['fluid'], [ns + ':' + p for p in (path, base, base + '_fluid', base + '_still')])
        if hit:
            return hit                                  # fluid blocks: Black Water, Crude Oil
        unwalled = re.sub(r'_wall(?=_|$)', '', path)
        if unwalled != path:
            hit = first(named['block'], [ns + ':' + unwalled, 'minecraft:' + unwalled])
            if hit:
                return hit                              # wall variants: Acan Coral Fan, Ender Sconce
    if kind == 'item' and path.endswith('_item'):
        return first(named['item'], [ns + ':' + path[:-5]])
    if kind == 'entity':
        bare = re.sub(r'^projectile_|_projectile$', '', path)
        return first(named['item'], [ns + ':' + p for p in (path, bare, path + '_item')])
    return None


def audit(dump, rules, previous=None):
    """previous: the last name_fixes.json 'audit' section. A dump taken with our fixes loaded shows the fixed
    name, set by the KubeJS pack; such a row carries its earlier fix forward instead of being judged again."""
    previous = previous or {}
    entries = dump['entries']
    # kind -> id -> shown name, for entries whose name is fine (a raw entry borrows from these)
    named = {k: {r['id']: r['name'] for r in entries.get(k, []) if not is_raw(r)} for k in KINDS}
    potions_by_id = {}
    for r in entries.get('potion', []):
        potions_by_id.setdefault(r['id'], {})[r['form']] = r

    fixes = {}       # key -> {name, ns, was, why, kind, id}
    at_source = []   # rows whose winner we can't override from the lang event
    reported = []    # skipped or formatting-code rows, listed only
    seen = set()

    def propose(kind, row, new, why):
        key = row['key']
        if key in seen:
            return
        seen.add(key)
        if any(p.search(key) for p in rules['skip']):
            reported.append((kind, row, new, why + ', skipped by rule'))
            return
        src = classify_source(row)
        if src:
            at_source.append((kind, row, new, why, src))
            return
        # a key nobody defines goes in the entry's own namespace
        fixes[key] = {'name': new, 'ns': row.get('ns') or row['id'].partition(':')[0],
                      'was': row['name'], 'why': why, 'kind': kind, 'id': row['id']}

    # Two passes: a raw block whose derived name comes from a raw block (skull_candle_soul_wall from
    # skull_candle_soul) picks up the first pass's fix.
    for pass_no in (1, 2):
        for kind in KINDS:
            for row in entries.get(kind, []):
                key = row['key']
                if key in seen:
                    continue
                old = previous.get(key)
                if old and classify_source(row) == 'ours' and row['name'] == old['name']:
                    seen.add(key)
                    if key in rules['keep']:
                        continue                    # dropped: the mod's own name returns on the next load
                    fixes[key] = dict(old, name=rules['names'].get(key, old['name']))
                    continue
                if key in rules['names']:
                    if rules['names'][key] != row['name']:
                        propose(kind, row, rules['names'][key], 'raw' if is_raw(row) else 'named by rule')
                    else:
                        seen.add(key)
                    continue
                if key in rules['keep']:
                    continue
                if is_raw(row):
                    ns, _, path = row['id'].partition(':')
                    new = derived_name(kind, ns, path, named)
                    if kind == 'potion':
                        for form, r in potions_by_id.get(row['id'], {}).items():
                            prefix = POTION_FORMS[form][0]
                            if not is_raw(r) and r['name'].startswith(prefix):
                                new = POTION_FORMS[row['form']][0] + r['name'][len(prefix):]
                                break
                    if new is None:
                        if pass_no == 1:
                            continue                # maybe derivable once pass 1 named its relative
                        new = name_from_id(re.sub(r'^projectile_|_projectile$|_item$', '', path), rules)
                    propose(kind, row, new, 'raw')
                    if key in fixes:
                        named[kind][row['id']] = new
                    continue
                if pass_no == 2:
                    continue
                name = row['name']
                if FORMAT.search(name):
                    stripped = FORMAT.sub('', name)
                    if title_case(stripped, rules) != stripped.strip():
                        reported.append((kind, row, None, 'case, has formatting codes'))
                    continue
                new = title_case(name, rules)
                if new != name:
                    propose(kind, row, new, 'case')
    return fixes, at_source, reported


# ---------------------------------------------------------------------------------------------
# Output
# ---------------------------------------------------------------------------------------------

def fixes_json(fixes, dump):
    by_ns = {}
    for key in sorted(fixes):
        f = fixes[key]
        by_ns.setdefault(f['ns'], {})[key] = f['name']
    return {
        '_about': 'Generated by tools/name_audit.py from the client name dump of %s; read by '
                  'kubejs/client_scripts/name_fixes.js. Edit name_rules.json, not this file. `audit` keeps what '
                  'each name replaced, so a dump taken with these fixes loaded carries them forward.' % dump.get('generated'),
        'namespaces': {ns: by_ns[ns] for ns in sorted(by_ns)},
        'audit': {key: {k: fixes[key][k] for k in ('name', 'ns', 'was', 'why', 'kind', 'id')} for key in sorted(fixes)},
    }


def md_escape(s):
    return str(s).replace('|', '\\|')


def render(fixes, at_source, reported, dump):
    counts = {}
    for f in fixes.values():
        counts.setdefault(f['kind'], {'raw': 0, 'case': 0, 'named by rule': 0})[f['why']] += 1
    lines = ['Generated by `python3 tools/name_audit.py` on %s from the client name dump of %s (%s).'
             % (datetime.now().strftime('%Y-%m-%d %H:%M'), dump.get('generated'),
                ', '.join('%s %d' % (k, len(dump['entries'].get(k, []))) for k in KINDS)), '']
    lines += ['| Kind | Raw key fixed | Capitalization fixed | Named by rule |', '|---|---|---|---|']
    for k in KINDS:
        c = counts.get(k, {'raw': 0, 'case': 0, 'named by rule': 0})
        lines.append('| %s | %d | %d | %d |' % (k, c['raw'], c['case'], c['named by rule']))
    lines += ['', '**%d fixes** in `name_fixes.json`, %d to fix at the source, %d reported only.'
              % (len(fixes), len(at_source), len(reported)), '']

    def table(title, rows, cols):
        if not rows:
            return [title, '', 'None.', '']
        out = [title, '', '| ' + ' | '.join(cols) + ' |', '|' + '---|' * len(cols)]
        out += ['| ' + ' | '.join(md_escape(c) for c in r) + ' |' for r in rows]
        return out + ['']

    lines += table('### Fix at the source', [
        (k, '`%s`' % r['key'], r['name'], new or '', why, '%s (%s)' % (r.get('pack'), r.get('ns')))
        for k, r, new, why, src in at_source], ['Kind', 'Key', 'Shown', 'Should be', 'Why', 'Set by'])
    lines += table('### Reported only', [
        (k, '`%s`' % r['key'], r['name'], new or '', why) for k, r, new, why in reported],
        ['Kind', 'Key', 'Shown', 'Proposed', 'Why'])
    for why, title in (('raw', '### Raw keys fixed'), ('named by rule', '### Named by rule'),
                       ('case', '### Capitalization fixed')):
        rows = [(f['kind'], '`%s`' % f['id'], f['was'], f['name'], f['ns'])
                for key, f in sorted(fixes.items(), key=lambda kv: (kv[1]['kind'], kv[1]['id']))
                if f['why'] == why]
        if len(rows) > 60:
            lines += ['<details><summary>%s (%d)</summary>' % (title[4:], len(rows)), '']
            lines += table('', rows, ['Kind', 'Id', 'Was', 'Now', 'Namespace'])[1:]
            lines += ['</details>', '']
        else:
            lines += table('%s (%d)' % (title, len(rows)), rows, ['Kind', 'Id', 'Was', 'Now', 'Namespace'])
    return '\n'.join(lines).rstrip() + '\n'


def replace_block(text, tag, body):
    begin, end = '<!-- BEGIN GENERATED: %s -->' % tag, '<!-- END GENERATED: %s -->' % tag
    if begin not in text:
        raise SystemExit('names.md has no %s block' % begin)
    head, rest = text.split(begin, 1)
    _, tail = rest.split(end, 1)
    return head + begin + '\n' + body + end + tail


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--print', action='store_true')
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    if not DUMP.exists():
        raise SystemExit('no %s: run /cedumpnames in game first' % DUMP.relative_to(INSTANCE))
    dump = json.loads(DUMP.read_text())
    rules = load_rules()
    previous = json.loads(FIXES.read_text()).get('audit', {}) if FIXES.exists() else {}
    fixes, at_source, reported = audit(dump, rules, previous)
    data = fixes_json(fixes, dump)
    text = json.dumps(data, indent=1, ensure_ascii=False, sort_keys=False) + '\n'
    if args.check:
        current = FIXES.read_text() if FIXES.exists() else ''
        ok = json.loads(current or '{}').get('audit') == data['audit']
        print('up to date (%d fixes)' % len(fixes) if ok else 'OUT OF DATE: rerun tools/name_audit.py')
        sys.exit(0 if ok else 1)
    if args.print:
        for key, f in sorted(fixes.items(), key=lambda kv: (kv[1]['why'], kv[1]['kind'], kv[0])):
            print('%-5s %-11s %-60s %r -> %r' % (f['why'][:5], f['kind'], key, f['was'], f['name']))
        for k, r, new, why, src in at_source:
            print('SRC   %-11s %-60s %r -> %r  (%s, %s)' % (k, r['key'], r['name'], new, why, r.get('pack')))
        for k, r, new, why in reported:
            print('REP   %-11s %-60s %r -> %r  (%s)' % (k, r['key'], r['name'], new, why))
        print('%d fixes, %d at source, %d reported' % (len(fixes), len(at_source), len(reported)))
        return
    FIXES.parent.mkdir(parents=True, exist_ok=True)
    FIXES.write_text(text)
    WRITEUP.write_text(replace_block(WRITEUP.read_text(), 'name-audit', render(fixes, at_source, reported, dump)))
    print('%d fixes -> %s; names.md updated' % (len(fixes), FIXES.relative_to(INSTANCE)))


if __name__ == '__main__':
    main()
