#!/usr/bin/env python3
"""Status as data, docs as generated views (plan_process_overhaul.md P1.4).

Status is written once: feature_writeup/status/features.tsv (one row per feature) and the TODO tables
(feature_writeup/todo/TODO.md). Run results come from the scratch run docs' verdict tables
(feature_writeup/testing/scratch_run_*.md, scratch_queue_run_*.md: `| ID | Result | ...`) and qrun's verdict files
(perf/runs/<run>/**/verdicts/*.json, TX-45; a verdict file wins over the run doc for the same run and row). Jar state
comes from tools/jarstamp.py and jar_archive/installed.tsv.

Writes three blocks between `<!-- generated:status_index begin NAME sha1=... -->` and
`<!-- generated:status_index end NAME -->`:
  readme  feature_writeup/README.md        the feature index table
  todo    feature_writeup/todo/TODO.md     status snapshot: counts by status and test tier
  queue   feature_writeup/testing/scratch_queue.md   queue coverage: rows per code, never run, last run

Usage:
  python3 tools/status_index.py            regenerate the three blocks
  python3 tools/status_index.py --check    fail on: a status word off the legend (TODO or features.tsv), a TODO id in
                                           features.tsv that does not exist, a queue code with no feature, a hand
                                           edit inside a generated block (stored hash), a missing block or writeup;
                                           a block that is only out of date is a WARN (run without --check)

Hand edits go to features.tsv and the TODO rows, never into a generated block.
"""
import hashlib
import json
import os
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent / 'scratch_query'))
import qrows  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
FW = ROOT / 'feature_writeup'
README = FW / 'README.md'
TODO = FW / 'todo' / 'TODO.md'
QUEUE = FW / 'testing' / 'scratch_queue.md'
TSV = FW / 'status' / 'features.tsv'
RUNDOCS = FW / 'testing'
VERDICT_ROOT = ROOT / 'perf' / 'runs'
LEDGER = ROOT / 'jar_archive' / 'installed.tsv'
sys.path.insert(0, str(ROOT / 'tools'))

TSV_COLS = ['key', 'file', 'test prefix', 'status word', 'jars', 'queue codes', 'todo ids', 'note']
FEATURE_LEGEND = ('VERIFIED', 'DEPLOYED', 'BUILT', 'BROKEN', 'reference', 'planned')
TODO_LEGEND = ('Bug', 'Planned', 'Pinned', 'In Progress', 'Testing', 'Complete')
TIERS = 'SCPT'
BLOCKS = {'readme': README, 'todo': TODO, 'queue': QUEUE}
BEGIN = re.compile(r'^<!-- generated:status_index begin (\w+) sha1=([0-9a-f]+) -->$')
CODE_RE = re.compile(r'\b((?:SQ|CQ)-[A-Za-z][A-Za-z0-9]*)-([A-Za-z0-9.]+)')
RESULT_RE = re.compile(r'^[*~ ]*(PASS|FAIL|INCONCLUSIVE|BATCH[- ]FAULT|OPEN|SKIP\w*|CRASH|LOOK|OWNER|BLOCKED|'
                       r'NOT RUN|DEFERRED|N/A|PARTIAL)', re.I)


def sha(lines):
    return hashlib.sha1('\n'.join(lines).encode()).hexdigest()[:12]


def empty_block(name, content=()):
    content = list(content)
    return ['<!-- generated:status_index begin %s sha1=%s -->' % (name, sha(content))] + content + \
        ['<!-- generated:status_index end %s -->' % name]


def cells_of(line):
    return [c.strip() for c in re.split(r'(?<!\\)\|', line.strip())[1:-1]]


def block_spans(lines):
    """{name: (begin index, end index, stored sha)}"""
    out = {}
    for i, l in enumerate(lines):
        m = BEGIN.match(l)
        if m:
            end = next((j for j in range(i + 1, len(lines))
                        if lines[j] == '<!-- generated:status_index end %s -->' % m.group(1)), None)
            out[m.group(1)] = (i, end, m.group(2))
    return out


def strip_blocks(lines, keep=None):
    """Blank every generated block (blocks whose name keep(name) accepts stay)."""
    spans = block_spans(lines)
    skip = set()
    for name, (b, e, _) in spans.items():
        if e is not None and not (keep and keep(name)):
            skip.update(range(b, e + 1))
    return [l if i not in skip else '' for i, l in enumerate(lines)]


# ---------------------------------------------------------------- inputs

def read_features():
    rows = []
    if not TSV.exists():
        return rows
    lines = TSV.read_text().rstrip('\n').split('\n')
    for n, l in enumerate(lines[1:], 2):
        c = l.split('\t')
        c += [''] * (len(TSV_COLS) - len(c))
        rows.append(dict(zip(TSV_COLS, c), line=n))
    return rows


def split_list(s):
    return [x.strip() for x in s.split(',') if x.strip()]


def read_todo():
    lines = strip_blocks(TODO.read_text().split('\n'))
    arch = next((i for i, l in enumerate(lines) if l.strip() == '# archive'), len(lines))
    open_rows, arch_rows, odd_arch, rows_text = [], [], [], []
    ids = set()
    status_table = False
    for i, l in enumerate(lines):
        if l.startswith('| Id |'):
            status_table = l.startswith('| Id | Change | Status | Tests |')
            continue
        if not l.startswith('|'):
            status_table = False
            continue
        m = re.match(r'\| `([^`]+ #\d+)`', l)
        if not m:
            continue
        c = cells_of(l)
        if i > arch:
            arch_rows.append(m.group(1))
            ids.add(m.group(1))
            if len(c) >= 8 and c[2] in TODO_LEGEND and c[2] != 'Complete':
                odd_arch.append((m.group(1), c[2]))
            continue
        if not status_table or len(c) < 4:
            continue
        ids.add(m.group(1))
        rows_text.append(l)
        open_rows.append({'id': m.group(1), 'status': c[2], 'tests': c[3], 'line': i + 1})
    return {'open': open_rows, 'archive': arch_rows, 'odd_archive': odd_arch, 'ids': ids, 'rows_text': rows_text}


def read_queue():
    # rows blocks are the queue sections rendered from feature_writeup/testing/rows/*.qb (qrows.py): their row lines
    # count like hand-written ones
    lines = strip_blocks(QUEUE.read_text().split('\n'), keep=lambda n: n.startswith('rows_'))
    rows, codes = [], Counter()
    code_sections = defaultdict(set)
    section_text, sec = [[]], 0
    for l in lines:
        if l.startswith('## '):
            sec += 1
            section_text.append([])
        section_text[sec].append(l)
        m = re.match(r'\|\s*((?:SQ|CQ)-[A-Za-z][A-Za-z0-9]*)-(\S+?)\s*\|', l)
        if m:
            rid = '%s-%s' % (m.group(1), m.group(2))
            inline = bool(re.search(r'\(r\d{4}\w*[^)]*?\b(PASS|FAIL)', l))
            rows.append({'id': rid, 'code': m.group(1), 'inline_run': inline})
            codes[m.group(1)] += 1
            code_sections[m.group(1)].add(sec)
    return {'rows': rows, 'codes': codes, 'code_sections': code_sections,
            'section_text': ['\n'.join(s) for s in section_text]}


def expand_ids(cell):
    """'SQ-PWG-14, -16, -26 to -34' -> ids. Plain ids anywhere in the cell, '-NN' continuations use the last code."""
    out = []
    code = None
    for part in re.split(r',|/|;| and ', cell.replace('**', '').replace('~~', '')):
        part = part.strip()
        m = re.match(r'((?:SQ|CQ)-[A-Za-z][A-Za-z0-9]*)-([A-Za-z0-9.]+)(?:\s+to\s+-?([A-Za-z0-9.]+))?', part)
        if m:
            code = m.group(1)
            out.extend(_range(code, m.group(2), m.group(3)))
            continue
        m = re.match(r'-([A-Za-z0-9.]+)(?:\s+to\s+-?([A-Za-z0-9.]+))?$', part)
        if m and code:
            out.extend(_range(code, m.group(1), m.group(2)))
    return out


def _range(code, a, b):
    if b and a.isdigit() and b.isdigit() and int(b) >= int(a) and int(b) - int(a) < 200:
        w = len(a)
        return ['%s-%0*d' % (code, w, n) for n in range(int(a), int(b) + 1)]
    return ['%s-%s' % (code, a)] + (['%s-%s' % (code, b)] if b else [])


def result_word(cell):
    m = RESULT_RE.match(cell)
    if not m:
        return 'OTHER'
    w = m.group(1).upper().replace(' ', '-')
    return 'BATCH-FAULT' if w.startswith('BATCH') else w


def read_runs():
    """{run id: {'date': 'YYYY-MM-DD', 'results': {row id: word}, 'src': ...}}"""
    runs = {}
    for p in sorted(RUNDOCS.glob('scratch*_run_*.md')):
        m = re.search(r'_run_(\d{4})-(\d{2})-(\d{2})(\w*)\.md$', p.name)
        if not m:
            continue
        rid = 'r%s%s%s' % (m.group(2), m.group(3), m.group(4))
        run = runs.setdefault(rid, {'date': '%s-%s-%s' % m.group(1, 2, 3), 'results': {}, 'src': p.name})
        in_table = None
        for l in p.read_text(errors='replace').split('\n'):
            if l.startswith('|'):
                c = cells_of(l)
                if len(c) >= 2 and c[0] in ('ID', 'Id', 'Row') and c[1].startswith('Result'):
                    in_table = 'rows'          # | ID | Result | Evidence |
                    continue
                if len(c) >= 2 and c[0] == 'Verdict' and c[1] == 'Rows':
                    in_table = 'verdicts'      # | Verdict | Rows | Note | (r1031)
                    continue
                if not in_table or len(c) < 2 or set(c[0]) <= set('-: '):
                    continue
                idcell, word = (c[0], result_word(c[1])) if in_table == 'rows' else (c[1], result_word(c[0]))
                for i in expand_ids(idcell):
                    run['results'][i] = word
            else:
                in_table = None
    for dirpath, dirnames, filenames in os.walk(VERDICT_ROOT):
        if os.path.basename(dirpath) != 'verdicts':
            continue
        rel = Path(dirpath).relative_to(VERDICT_ROOT).parts
        rid = rel[0]
        for fn in filenames:
            if not fn.endswith('.json'):
                continue
            try:
                d = json.load(open(os.path.join(dirpath, fn)))
            except (OSError, ValueError):
                continue
            date = (d.get('started') or d.get('ended') or '')[:10]
            run = runs.setdefault(rid, {'date': date, 'results': {}, 'src': 'perf/runs/%s' % rid})
            if date and (not run['date'] or date > run['date']):
                run['date'] = date
            row = d.get('id') or fn[:-5]
            run['results'][row] = (d.get('verdict') or d.get('result') or 'OTHER').upper()
    return runs


def run_index(runs):
    """code -> [(date, run id, Counter of words)] newest first; set of row ids with any result."""
    per = defaultdict(dict)
    seen = set()
    for rid, run in runs.items():
        for row, word in run['results'].items():
            m = CODE_RE.match(row)
            if not m:
                continue
            seen.add(row)
            per[m.group(1)].setdefault(rid, Counter())[word] += 1
    out = {}
    for code, d in per.items():
        out[code] = sorted(((runs[r]['date'], r, c) for r, c in d.items()), reverse=True)
    return out, seen


def ran(row_id, seen):
    return row_id in seen or row_id + 'r' in seen


def counts_text(c):
    parts = ['%d PASS' % c['PASS']] if c['PASS'] else []
    if c['FAIL']:
        parts.append('%d FAIL' % c['FAIL'])
    for k in sorted(c):
        if k not in ('PASS', 'FAIL') and c[k]:
            parts.append('%d %s' % (c[k], k))
    return ', '.join(parts) or '0'


def jar_states():
    """{mod_id: 'pending' | 'installed YYYY-MM-DD' | 'not built' ...}"""
    import jarstamp
    out = {}
    pend = {p for p, *_ in jarstamp.pending()}
    names = {}
    for gp in sorted(jarstamp.DEV.glob('*/gradle.properties')):
        pr = jarstamp._props(gp)
        if 'mod_id' in pr and not pr.get('bundled_in'):
            names[pr['mod_id']] = (gp.parent.name, pr.get('jar_name', pr['mod_id']))
    ledger = {}
    if LEDGER.exists():
        for l in LEDGER.read_text().split('\n')[1:]:
            c = l.split('\t')
            if len(c) >= 6 and c[5].startswith('installed'):
                ledger[re.sub(r'-[0-9][^-]*\.jar$', '', c[1])] = c[0][:10]
    for mod, (proj, jar_name) in names.items():
        if proj in pend:
            out[mod] = 'pending'
        elif jar_name in ledger:
            out[mod] = 'installed %s' % ledger[jar_name]
        elif jarstamp.installed_by_name(jar_name):
            out[mod] = 'in mods/'
        else:
            out[mod] = 'not in mods/'
    return out


# ---------------------------------------------------------------- views

def md_links(files, base):
    out = []
    for f in split_list(files):
        rel = os.path.relpath(FW / f, base).replace(os.sep, '/')
        out.append('[%s](%s)' % (os.path.basename(f) if not f.endswith('README.md') else f, rel))
    return ', '.join(out)


def view_readme(feats, rindex, jars):
    out = ['| Feature | File | Test prefix | Status | Last run (per queue code) | Jars | Note |',
           '|---|---|---|---|---|---|---|']
    for f in feats:
        runs = []
        for code in split_list(f['queue codes']):
            if code in rindex:
                d, r, c = rindex[code][0]
                runs.append((d, r, code, c))
        runs.sort(reverse=True)
        cell = '; '.join('%s %s %s: %s' % (code, r, d, counts_text(c)) for d, r, code, c in runs[:3])
        if len(runs) > 3:
            cell += '; +%d codes' % (len(runs) - 3)
        jcell = ', '.join(('%s %s' % (j, jars.get(j, ''))).strip() for j in split_list(f['jars']))
        prefix = ', '.join('`%s`' % p for p in split_list(f['test prefix']))
        out.append('| %s | %s | %s | %s | %s | %s | %s |' % (f['key'], md_links(f['file'], FW), prefix or 'none',
                                                          f['status word'], cell or 'none', jcell, f['note']))
    return out


def view_todo(todo):
    st = Counter(r['status'] for r in todo['open'])
    out = ['| Status | Rows |', '|---|---|']
    for s in TODO_LEGEND:
        out.append('| %s | %d |' % (s, st.get(s, 0)))
    for s in sorted(set(st) - set(TODO_LEGEND)):
        out.append('| %s (off the legend) | %d |' % (s, st[s]))
    out.append('| **All rows in the family tables** | %d |' % len(todo['open']))
    tiers = {t: Counter() for t in TIERS}
    none = 0
    for r in todo['open']:
        if r['status'] == 'Complete':
            continue
        toks = re.findall(r'\b([SCPT])([+~]?)(?=\s|$)', r['tests'])
        if not toks:
            none += 1
        for t, mark in toks:
            tiers[t][mark or 'open'] += 1
    out += ['', 'Test tiers over the rows that are not Complete (`+` passed, `~` partly passed):', '',
            '| Tier | Passed | Partly | Open |', '|---|---|---|---|']
    for t in TIERS:
        out.append('| %s | %d | %d | %d |' % (t, tiers[t]['+'], tiers[t]['~'], tiers[t]['open']))
    out += ['', '%d rows need no tier (`none` or empty). Archive: %d rows.' % (none, len(todo['archive']))]
    if todo['odd_archive']:
        c = Counter(s for _, s in todo['odd_archive'])
        out.append('%d archive rows still carry an open status (%s): %s.' % (
            len(todo['odd_archive']), ', '.join('%s %d' % kv for kv in sorted(c.items())),
            ', '.join('`%s`' % i for i, _ in todo['odd_archive'])))
    return out


def view_queue(queue, rindex, seen):
    never = Counter()
    for r in queue['rows']:
        if not r['inline_run'] and not ran(r['id'], seen):
            never[r['code']] += 1
    allruns = [x for v in rindex.values() for x in v]
    last = max(allruns)[:2] if allruns else None
    total = sum(queue['codes'].values())
    out = ['**Queue coverage** (generated by `python3 tools/status_index.py` from the rows below, the run docs\' '
           'verdict tables and qrun verdict files; never hand-edited): %d rows in %d codes, %d never run; latest run '
           '%s. The old "added at the end" and run banners are in [../status/history.md](../status/history.md).' % (
               total, len(queue['codes']), sum(never.values()), '%s (%s)' % (last[1], last[0]) if last else 'none'),
           '', '<details><summary>Rows per code</summary>', '',
           '| Code | Rows | Never run | Last run |', '|---|---|---|---|']
    for code in sorted(queue['codes']):
        lr = rindex.get(code)
        cell = '%s %s: %s' % (lr[0][1], lr[0][0], counts_text(lr[0][2])) if lr else 'never'
        out.append('| %s | %d | %d | %s |' % (code, queue['codes'][code], never[code], cell))
    out += ['', '</details>']
    return out


# ---------------------------------------------------------------- rows sections (plan P2.1, P2.5)

ARCHIVE = FW / 'testing' / 'scratch_queue_archive.md'
ARCHIVE_HEAD = ['# Scratch queue archive', '',
                'Queue sections whose rows all PASSed on the jars still installed (`qrun.py archive`, plan_process_overhaul.md '
                'P2.5), with their verdict history. Generated from `feature_writeup/testing/rows/*.qb` (`@@archived`) by '
                '`python3 tools/status_index.py`; never hand-edited. The rows stay data: `qrun.py plan` plans them again when '
                'a jar or source changes.', '']


def rows_views(runs):
    """({block name: lines}, {block name: QUEUE or ARCHIVE})"""
    hist = defaultdict(list)
    for rid, run in runs.items():
        for row, word in run['results'].items():
            hist[row].append((run['date'], rid, word))
    views, where = {}, {}
    for name, (_, sec) in qrows.sections_by_block().items():
        lines = qrows.render(sec)
        if sec['meta'].get('archived'):
            lines += ['', '**Verdict history** (archived %s):' % sec['meta']['archived'], '']
            for kind, r in sec['items']:
                if kind == 'row':
                    h = sorted(hist.get(r['id'], ()), reverse=True)[:5]
                    lines.append('- %s: %s' % (r['id'], '; '.join('%s %s (%s)' % (w, run, d) for d, run, w in h) or 'none'))
            where[name] = ARCHIVE
        else:
            where[name] = QUEUE
        views[name] = lines
    return views, where


def rows_spans(path):
    if not path.exists():
        return [], {}
    lines = path.read_text().split('\n')
    return lines, {n: v for n, v in block_spans(lines).items() if n.startswith('rows_')}


# ---------------------------------------------------------------- main

def build():
    feats = read_features()
    todo = read_todo()
    queue = read_queue()
    runs = read_runs()
    rindex, seen = run_index(runs)
    views = {'readme': view_readme(feats, rindex, jar_states()), 'todo': view_todo(todo),
             'queue': view_queue(queue, rindex, seen)}
    views['rows'], views['rows_where'] = rows_views(runs)
    return feats, todo, queue, views


def check(feats, todo, queue, views):
    fails, warns = [], []
    if not feats:
        fails.append('features.tsv missing or empty')
    hdr = TSV.read_text().split('\n', 1)[0].split('\t') if TSV.exists() else []
    if hdr and hdr != TSV_COLS:
        fails.append('features.tsv header is not %s' % '\t'.join(TSV_COLS))
    keys = Counter(f['key'] for f in feats)
    for k, n in keys.items():
        if n > 1:
            fails.append('features.tsv: key %r on %d rows' % (k, n))
    for f in feats:
        if f['status word'] not in FEATURE_LEGEND:
            fails.append('features.tsv line %d (%s): status word %r not in the legend' % (
                f['line'], f['key'], f['status word']))
        for t in split_list(f['todo ids']):
            if t not in todo['ids']:
                fails.append('features.tsv line %d (%s): TODO id %r does not exist' % (f['line'], f['key'], t))
        for p in split_list(f['file']):
            if not (FW / p).exists():
                fails.append('features.tsv line %d (%s): file %s does not exist' % (f['line'], f['key'], p))
    for r in todo['open']:
        if r['status'] not in TODO_LEGEND:
            fails.append('TODO.md line %d %s: status %r not in the legend' % (r['line'], r['id'], r['status'][:60]))
    owned = {c for f in feats for c in split_list(f['queue codes'])}
    for code in sorted(set(queue['codes']) - owned):
        fails.append('scratch_queue.md: queue code %s (%d rows) has no feature in features.tsv' % (
            code, queue['codes'][code]))
    for name, path in BLOCKS.items():
        lines = path.read_text().split('\n')
        span = block_spans(lines).get(name)
        rel = path.relative_to(ROOT)
        if not span or span[1] is None:
            fails.append('%s: generated block %s missing or unterminated' % (rel, name))
            continue
        b, e, stored = span
        content = lines[b + 1:e]
        if sha(content) != stored:
            fails.append('%s: hand edit inside generated block %s (lines %d-%d); move the change to features.tsv or '
                         'TODO.md and rerun tools/status_index.py' % (rel, name, b + 2, e))
        elif content != views[name]:
            warns.append('%s: block %s is out of date; run python3 tools/status_index.py' % (rel, name))
    # queue sections rendered from rows/*.qb (plan P2.1); archived ones live in the archive file (P2.5)
    qfails, _ = qrows.check()
    fails += qfails
    present = {}
    for path in (QUEUE, ARCHIVE):
        lines, spans = rows_spans(path)
        rel = path.relative_to(ROOT)
        for name, (b0, e, stored) in sorted(spans.items(), key=lambda kv: kv[1][0]):
            present[name] = path
            if e is None:
                fails.append('%s: generated block %s unterminated' % (rel, name))
                continue
            content = lines[b0 + 1:e]
            if sha(content) != stored:
                fails.append('%s: hand edit inside generated block %s (lines %d-%d); edit the .qb in '
                             'feature_writeup/testing/rows/ and rerun tools/status_index.py' % (rel, name, b0 + 2, e))
            elif name not in views['rows']:
                fails.append('%s: block %s has no section in feature_writeup/testing/rows/' % (rel, name))
            elif content != views['rows'][name] or views['rows_where'][name] != path:
                warns.append('%s: block %s is out of date or in the wrong file; run python3 tools/status_index.py' % (
                    rel, name))
    for name in sorted(set(views['rows']) - set(present)):
        warns.append('rows section %s not in the queue yet; run python3 tools/status_index.py' % name)
    return fails, warns


def write(views):
    changed = []
    for name, path in BLOCKS.items():
        lines = path.read_text().split('\n')
        span = block_spans(lines).get(name)
        if not span or span[1] is None:
            print('%s: no generated block %s; place the markers first' % (path.relative_to(ROOT), name))
            continue
        b, e, _ = span
        new = empty_block(name, views[name])
        if lines[b:e + 1] != new:
            lines[b:e + 1] = new
            path.write_text('\n'.join(lines))
            changed.append(name)
    # rows sections: regenerate each block in its file (queue, or the archive for @@archived sections); a block in the
    # wrong file is removed there and appended to the right one; a section without a block is appended at the end
    n = 0
    for path in (QUEUE, ARCHIVE):
        lines, spans = rows_spans(path)
        if not lines and path == ARCHIVE:
            if not any(w == ARCHIVE for w in views['rows_where'].values()):
                continue
            lines = list(ARCHIVE_HEAD)
        orig = list(lines)
        for name, (b0, e, _) in sorted(((k, v) for k, v in spans.items() if v[1] is not None), key=lambda kv: -kv[1][0]):
            if views['rows_where'].get(name) != path:
                del lines[b0:e + 2 if e + 1 < len(lines) and not lines[e + 1].strip() else e + 1]
            elif lines[b0:e + 1] != empty_block(name, views['rows'][name]):
                lines[b0:e + 1] = empty_block(name, views['rows'][name])
        have = set(rows_spans(path)[1]) if path.exists() else set()
        for name in sorted((k for k, w in views['rows_where'].items() if w == path and k not in have),
                           key=lambda k: (k.rsplit('_', 1)[0], int(k.rsplit('_', 1)[1]))):
            while lines and not lines[-1].strip():
                lines.pop()
            lines += [''] + empty_block(name, views['rows'][name]) + ['']
        if lines != orig:
            path.write_text('\n'.join(lines))
            n += 1
    if n:
        changed.append('rows sections (%d file%s)' % (n, 's' if n > 1 else ''))
    return changed


def main(argv):
    feats, todo, queue, views = build()
    if '--check' in argv:
        fails, warns = check(feats, todo, queue, views)
        for w in warns:
            print('WARN ' + w)
        for f in fails:
            print('FAIL ' + f)
        print('status_index: %s (%d features, %d TODO rows, %d queue codes)' % (
            'FAIL %d' % len(fails) if fails else 'OK', len(feats), len(todo['open']), len(queue['codes'])))
        return 1 if fails else 0
    changed = write(views)
    if any(c.startswith('rows') for c in changed):
        # new or moved rows sections change the queue's row counts: rebuild once so the coverage block agrees
        changed += [c for c in write(build()[3]) if c not in changed]
    print('status_index: %s' % ('updated ' + ', '.join(changed) if changed else 'no change'))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
