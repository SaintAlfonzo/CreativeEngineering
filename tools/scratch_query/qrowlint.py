#!/usr/bin/env python3
"""Queue row lint (plan_process_overhaul.md P2.3, TX-46): stale rows and dead ids in feature_writeup/testing/rows/.

Stale: a row's `sources` (row @sources, else the section's @@sources) changed after the row was last matched to them.
That date is the row's @reviewed, else the section's @@reviewed, else its @@date. Source change times come from git
(last commit touching the path; a directory counts every file under it) and, for uncommitted edits, the file mtime.
A commit whose subject contains "[no-stale]" (a mechanical sweep: date form, spelling, renamed ids) is skipped.
The agent that changes a source rewrites the rows written from it in the same session and sets @reviewed (or
@@reviewed for the whole section) to the day.

Dead ids: every `namespace:path` in a row's runnable steps is checked against the latest dumps (kubejs/exported/:
display_names.json registries, tags/*.json, structures and biomes). In a known position (setblock / fill / execute if
block -> block, give / clear / item id -> item, summon -> entity, effect give -> effect, #tag -> tag) an unknown id is an
error; elsewhere an unknown id in a namespace the dumps know is a warning. Ids inside quotes of a shell step, regexes
and comments are skipped.

    qrowlint.py [FILE.qb ...] [--changed PATH ...] [--quiet] [--gate] [--write-baseline]
        no files: every rows file. --changed: the session's changed files; a stale row whose source is among them and
        whose .qb is not is an error (the rewrite was forgotten); other stale rows are warnings.
Library: lint_file(path, changed=None) -> [(severity, line, row id, message)].
"""
import csv
import json
import os
import re
import subprocess
import sys
import time
from functools import lru_cache
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import qrows  # noqa: E402

ROOT = qrows.ROOT
EXPORTED = ROOT / 'kubejs/exported'
ID_RE = re.compile(r'(#?)\b([a-z0-9_.\-]+):([a-z0-9_.\-/]+)')
POS = [  # (regex on the command, registry) the first id after the match is checked against the registry
    (re.compile(r'^(?:execute .*?\b(?:if|unless) block|setblock) +\S+ +\S+ +\S+ +'), 'block'),
    (re.compile(r'^fill +\S+ +\S+ +\S+ +\S+ +\S+ +\S+ +'), 'block'),
    (re.compile(r'^(?:give|clear) +\S+ +'), 'item'),
    (re.compile(r'^summon +'), 'entity'),
    (re.compile(r'^effect give +\S+ +'), 'effect'),
]
SKIP_NS = {'http', 'https', 'file', 'jar'}


CACHE = ROOT / 'jar_archive' / 'queue_id_index.json'   # not tracked; rebuilt when a dump is newer


def _dump_files():
    out = [EXPORTED / n for n in ('display_names.json', 'mob_effects.json', 'fluids.json', 'potions.json',
                                  'spawns.json', 'structures.json', 'biome_data.json', 'paintings.json')]
    for d in ('items_by_mod', 'loot_tables', 'tags'):
        out += sorted((EXPORTED / d).glob('*.json'))
    out.append(EXPORTED / 'recipes' / 'recipe_types.json')
    return [p for p in out if p.exists()]


def _build():
    reg = {k: set() for k in ('block', 'item', 'entity', 'effect', 'fluid', 'tag', 'other')}
    p = EXPORTED / 'display_names.json'          # older (client dump), every registry
    if p.exists():
        for kind, entries in json.loads(p.read_text())['entries'].items():
            reg.setdefault(kind, set()).update(e['id'] for e in entries if isinstance(e, dict) and 'id' in e)
    for f in (EXPORTED / 'items_by_mod').glob('*.json'):
        try:
            reg['item'].update(json.loads(f.read_text()).get('items', ()))
        except ValueError:
            pass
    for f in (EXPORTED / 'loot_tables').glob('*.json'):
        try:
            keys = json.loads(f.read_text())
        except ValueError:
            continue
        for k in keys:
            kind, _, path = k.partition('/')
            if kind in ('blocks', 'entities') and path:
                reg['block' if kind == 'blocks' else 'entity'].add('%s:%s' % (f.stem, path))
    for kind, reg_kind in (('block', 'block'), ('item', 'item'), ('entity_type', 'entity'), ('fluid', 'fluid')):
        f = EXPORTED / 'tags' / ('%s_tags_by_%s.json' % (kind, kind))
        if f.exists():
            for v in json.loads(f.read_text()).values():
                reg[reg_kind].update(v if isinstance(v, (list, dict)) else ())
        f = EXPORTED / 'tags' / ('%s_tags.json' % kind)
        if f.exists():
            d = json.loads(f.read_text())
            reg['tag'].update(d)
            for v in d.values():
                if isinstance(v, list):
                    reg[reg_kind].update(x for x in v if isinstance(x, str) and not x.startswith('#'))
    for name, kind in (('mob_effects.json', 'effect'), ('fluids.json', 'fluid'), ('spawns.json', 'entity'),
                       ('structures.json', 'other'), ('biome_data.json', 'other'), ('potions.json', 'other'),
                       ('paintings.json', 'other')):
        f = EXPORTED / name
        if f.exists():
            reg[kind].update(m.group(0) for m in ID_RE.finditer(f.read_text(errors='replace')) if not m.group(1))
    f = EXPORTED / 'recipes' / 'recipe_types.json'     # recipe types and serializers
    if f.exists():
        for k, v in json.loads(f.read_text()).items():
            reg['other'].add(k)
            reg['other'].update((v or {}).get('serializers', {}) if isinstance(v, dict) else ())
    reg['block'] |= reg['item']                  # block items share the block id; tagless blocks are rare
    return reg


@lru_cache(maxsize=1)
def registries():
    """{registry: set(ids)}, 'any': every id, 'ns': known namespaces, 'date': newest dump day. Cached in
    jar_archive/queue_id_index.json until a dump changes."""
    files = _dump_files()
    stamp = max((p.stat().st_mtime for p in files), default=0)
    reg = None
    try:
        c = json.loads(CACHE.read_text())
        if c.get('stamp') == stamp and c.get('n') == len(files):
            reg = {k: set(v) for k, v in c['reg'].items()}
    except (OSError, ValueError, KeyError):
        pass
    if reg is None:
        reg = _build()
        try:
            CACHE.write_text(json.dumps({'stamp': stamp, 'n': len(files), 'reg': {k: sorted(v) for k, v in
                                                                                   reg.items()}}))
        except OSError:
            pass
    reg['any'] = set().union(*reg.values())
    reg['ns'] = {i.split(':', 1)[0] for i in reg['any']}
    reg['date'] = time.strftime('%Y-%m-%d', time.localtime(stamp)) if stamp else ''
    return reg


@lru_cache(maxsize=None)
def swap_aliases():
	"""Old ids the swap lists keep as permanent aliases (`replaces` values in development/**/data/*/swaps/*.json):
	they still resolve in NBT, so an old-id save row may name them."""
	out = set()
	for f in (ROOT / 'development').glob('**/src/**/data/*/swaps/*.json'):
		if '/build/' in str(f):
			continue
		try:
			rep = json.loads(f.read_text()).get('replaces', {})
		except (OSError, ValueError):
			continue
		for olds in rep.values():
			out.update(o for o in (olds if isinstance(olds, list) else [olds]) if isinstance(o, str))
	return frozenset(out)


def _clean(cmd):
    cmd = re.sub(r'"(?:[^"\\]|\\.)*"', lambda m: m.group(0) if 'id:' in cmd else '""', cmd)
    return cmd


def dead_ids(cmd):
    """[(severity, id, registry)] for one console command."""
    reg = registries()
    if not reg.get('any'):
        return []
    out = []
    checked = set()
    for rx, kind in POS:
        m = rx.match(cmd)
        if not m:
            continue
        im = ID_RE.match(cmd, m.end())
        if not im:
            continue
        tag, ident = im.group(1), '%s:%s' % (im.group(2), im.group(3))
        checked.add(im.start())
        if tag:
            if ident not in reg['tag']:
                out.append(('E', '#' + ident, 'tag'))
        elif ident not in reg.get(kind, ()) and ident not in reg.get(kind + '_alias', ()) and ident not in swap_aliases():
            out.append(('E', ident, kind))
    # item stacks in NBT: {id:"x",Count:1b} or {Count:1b,id:"x"} (not enchantment or effect ids)
    for m in re.finditer(r'\bid: *"([a-z0-9_.\-]+:[a-z0-9_.\-/]+)" *, *Count\b|\bCount: *\d+b? *, *id: *"([a-z0-9_.\-]+:'
                         r'[a-z0-9_.\-/]+)"', cmd):
        ident = m.group(1) or m.group(2)
        if ident not in reg.get('item', ()) and ident not in swap_aliases():
            out.append(('E', ident, 'item'))
        checked.add(m.start(1) if m.group(1) else m.start(2))
    for m in ID_RE.finditer(_clean(cmd)):
        if m.start() in checked or m.start(2) in checked:
            continue
        ns, ident = m.group(2), '%s:%s' % (m.group(2), m.group(3))
        if ns in SKIP_NS or ns not in reg['ns'] or '/' in m.group(3) or '.' in m.group(3):
            continue
        if m.group(1):
            if ident not in reg['tag']:
                out.append(('W', '#' + ident, 'tag'))
        elif ident not in reg['any'] and ident not in swap_aliases():
            out.append(('W', ident, 'any'))
    return out


# ---------------------------------------------------------------- staleness

NO_STALE = '[no-stale]'


@lru_cache(maxsize=1)
def git_times():
    """{path: last commit time} for every tracked path, and {path: mtime} for uncommitted edits. Commits whose
    subject carries NO_STALE (mechanical sweeps: date form, spelling, id renames) do not count."""
    times = {}
    try:
        r = subprocess.run(['git', 'log', '--format=@%ct %s', '--name-only', '--since=2026-09-01'], cwd=ROOT,
                           capture_output=True, text=True, timeout=60)
        t, skip = 0, False
        for l in r.stdout.split('\n'):
            if l.startswith('@'):
                stamp, _, subj = l[1:].partition(' ')
                t, skip = int(stamp), NO_STALE in subj
            elif l and l not in times and not skip:
                times[l] = t
        r = subprocess.run(['git', 'status', '--porcelain', '-uall'], cwd=ROOT, capture_output=True, text=True,
                           timeout=60)
        for l in r.stdout.split('\n'):
            p = l[3:].strip()
            if p:
                try:
                    times[p] = max(times.get(p, 0), int((ROOT / p).stat().st_mtime))
                except OSError:
                    pass
    except (OSError, subprocess.SubprocessError):
        pass
    return times


GEN_BLOCK_RE = re.compile(r'<!-- generated:status_index begin.*?<!-- generated:status_index end[^>]*-->', re.S)
SECTION0_RE = re.compile(r'^\*\*README index entry, moved here[^\n]*(?:\n(?!\s*\n)[^\n]*)*', re.M)


def _status_free(text):
    """A markdown source without its generated blocks and the D6 section 0 status paragraphs (P3.4 h)."""
    return re.sub(r'\n\s*\n+', '\n\n', SECTION0_RE.sub('', GEN_BLOCK_RE.sub('', text or ''))).strip()


def _git_text(rev, path):
    r = subprocess.run(['git', 'show', '%s:%s' % (rev, path)], cwd=ROOT, capture_output=True, text=True)
    return r.stdout if r.returncode == 0 else None


@lru_cache(maxsize=None)
def content_change(path):
    """Time of the newest change to a markdown file that is not confined to generated blocks or section 0 status
    paragraphs; None when no such change is found in the window git_times() reads."""
    try:
        cur = (ROOT / path).read_text(errors='replace')
    except OSError:
        return git_times().get(path)   # deleted or unreadable: the plain change time
    head = _git_text('HEAD', path)
    if head is None or _status_free(cur) != _status_free(head):
        return git_times().get(path)
    r = subprocess.run(['git', 'log', '--format=%H %ct %s', '--since=2026-09-01', '--', path], cwd=ROOT,
                       capture_output=True, text=True, timeout=60)
    for line in r.stdout.split('\n'):
        if not line.strip():
            continue
        sha, t, subj = (line.split(' ', 2) + [''])[:3]
        if NO_STALE in subj:
            continue
        new, old = _git_text(sha, path), _git_text(sha + '^', path)
        if old is None or _status_free(new) != _status_free(old):
            return int(t)
    return None


@lru_cache(maxsize=None)
def newest_change(src):
    """(time, path) of the newest change to src (file or directory), or None. A markdown file counts only changes
    outside its generated blocks and section 0 status paragraphs, so a status-only edit does not stale a row."""
    src = src.rstrip('/')
    best = None
    for p, t in git_times().items():
        if (p == src or p.startswith(src + '/')) and (best is None or t > best[0]):
            if p.endswith('.md') and p == src:
                t = content_change(p)
                if t is None:
                    continue
            best = (t, p)
    return best


def changed_after(src, day):
    """(newest change time, path) of src when it is after the day (YYYY-MM-DD, end of day), else None."""
    try:
        limit = time.mktime(time.strptime(day, '%Y-%m-%d')) + 86400
    except ValueError:
        return None
    best = newest_change(src)
    return best if best and best[0] > limit else None


def reviewed(sec, row):
    return row['meta'].get('reviewed') or sec['meta'].get('reviewed') or sec['meta'].get('date', '')


def lint_file(path, changed=None):
    out = []
    f = qrows.load(path)
    rel = os.path.relpath(Path(path).resolve(), ROOT)
    changed = {c.rstrip('/') for c in (changed or ())}
    for sec in f['sections']:
        for kind, r in sec['items']:
            if kind != 'row':
                continue
            m = qrows.effective(sec, r)
            day = reviewed(sec, r)
            for src in (m.get('sources') or '').split():
                hit = changed_after(src, day) if day else None
                if hit:
                    in_session = changed and any(hit[1] == c or hit[1].startswith(c + '/') for c in changed)
                    sev = 'E' if in_session and rel not in changed else 'W'
                    out.append((sev, r['line'], r['id'], 'stale: %s changed %s, after the row was reviewed (%s); '
                                'rewrite the row and set @reviewed' % (
                                    hit[1], time.strftime('%Y-%m-%d', time.localtime(hit[0])), day)))
            n = r['line']
            for l in r['steps']:
                n += 1
                s = l.strip()
                if s.startswith('> ') or s.startswith(('srv> ', 'cli> ')):
                    for sev, ident, kind2 in dead_ids(s.split('> ', 1)[1]):
                        out.append((sev, n, r['id'], 'dead id: %s not in the %s dump' % (
                            ident, 'registry' if kind2 == 'any' else kind2)))
    return out


BASELINE = ROOT / 'feature_writeup/testing/rows/_lint_baseline.csv'


def _key(path, rid, msg):
    return (Path(path).name, rid, msg.split(' not in ')[0])


def baseline():
    """{(file, row, finding)} from the baseline CSV (header: file,row,finding)."""
    try:
        with open(BASELINE, newline='') as f:
            return {tuple(r) for r in list(csv.reader(f))[1:] if len(r) == 3}
    except OSError:
        return set()


def write_baseline(found):
    with open(BASELINE, 'w', newline='') as f:
        w = csv.writer(f)
        w.writerow(('file', 'row', 'finding'))
        w.writerows(sorted(found))


def main(argv):
    """--gate: errors already in _lint_baseline.csv (dead ids found at the migration) are warnings; a new one fails.
    --write-baseline: rewrite the baseline from the current errors (only to shrink it after fixes)."""
    quiet = '--quiet' in argv
    gate = '--gate' in argv
    base = baseline() if gate else set()
    found = set()
    changed = None
    files = []
    i = 0
    while i < len(argv):
        a = argv[i]
        if a == '--changed':
            changed = []
            i += 1
            while i < len(argv) and not argv[i].startswith('--'):
                changed.append(argv[i])
                i += 1
            continue
        if not a.startswith('--'):
            files.append(a)
        i += 1
    files = files or [str(p) for p in qrows.all_files()]
    ne = nw = 0
    for p in files:
        for sev, n, rid, msg in lint_file(p, changed):
            if sev == 'E':
                found.add(_key(p, rid, msg))
                if _key(p, rid, msg) in base:
                    sev, msg = 'W', msg + ' (baseline)'
            ne += sev == 'E'
            nw += sev == 'W'
            if sev == 'E' or not quiet:
                print('%s %s:%d %s: %s' % (sev, os.path.relpath(p, ROOT), n, rid, msg))
    if '--write-baseline' in argv:
        write_baseline(found)
        print('baseline: %d entries -> %s' % (len(found), os.path.relpath(BASELINE, ROOT)))
    elif gate and base - found:
        print('baseline: %d entries fixed; shrink it with --write-baseline' % len(base - found))
    print('qrowlint: %d file(s), %d error(s), %d warning(s)' % (len(files), ne, nw))
    return 1 if ne else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
