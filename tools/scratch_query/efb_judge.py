#!/usr/bin/env python3
"""Judges earth fill on the placed blocks (scratch queue SQ-EFB, SQ-WGE-03b; run r1102b). Pure reader.

    efb_judge.py under LOG [--box X0 Z0 X1 Z1] [--id REGEX] [--except REGEX] [--fail CHECK]... [--need CHECK]
    efb_judge.py log LOG [--box X0 Z0 X1 Z1] [--id REGEX]
    efb_judge.py vslog LOG [--box X0 Z0 X1 Z1] [--id REGEX]
    efb_judge.py gone LEVEL LOG [--box X0 Z0 X1 Z1] [--id REGEX]
    efb_judge.py starts LEVEL [--box X0 Z0 X1 Z1] [--id REGEX]
    efb_judge.py layouts LEVEL_A LEVEL_B [--box X0 Z0 X1 Z1] [--id REGEX]

LOG is a server log (logs/latest.log); LEVEL the world folder. --box is in block coordinates and filters on the start
chunk. --id filters structure ids (Python regex, searched).
under:  every "[zq] under ..." line of /zq under (zq_probe.js), deduplicated by structure and start chunk; a summary
        per structure id and the offenders of each CHECK:
          over6      a column hangs more than 6 over air (beardReach)
          deepwater  water-below max over 2 (waterReach)
          basewater  water in the bottom 3 layers of the base
          foreign    base earth the native ring does not have
          waterbelow any column with water right under the built bottom (wanted for earth_fill/wet_ok stilts)
        --fail CHECK: exit 1 when a start (not matching --except) fails CHECK. --need CHECK: exit 1 when NO start fails
        CHECK (SQ-EFB-16: with earth fill off the probe must find hard cases).
log:    the [earth-fill] lines (generation only: "(predicted)" lines skipped), deduplicated: kept / CANCELLED by the
        first limit crossed (water, air, mound, too wet) / pruned, per structure id and in total.
vslog:  each probed start paired with its [earth-fill] line (SQ-EFB-11); flags a start the probe finds worse than its
        line (logged 0 over air but over6 > 0; logged 0 over water but water-below max > 2). Exit 1 on a flag.
gone:   CANCELLED starts must be absent from the region files and pruned starts must hold N - K pieces (SQ-EFB-12,
        -13). Only chunks the region files hold count; exit 1 on a contradiction.
starts: the structure starts saved in the region files: id, start chunk, piece count, surface biome of the start chunk.
layouts: starts saved in both worlds (same seed) compared piece by piece (template, x, z, rotation): identical, a subset
        (earth fill pruned pieces in one world) or different (SQ-WGE-06d, canonical pool order). Exit 1 on a difference
        or when no start is in both.
"""
import collections
import glob
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import region_blocks as rb  # noqa: E402  (pure reader helpers: nbt, chunks, biome_at)

UNDER = re.compile(r'\[zq\] under (\S+) chunk (-?\d+) (-?\d+) box (-?\d+) (-?\d+) (-?\d+) to (-?\d+) (-?\d+) (-?\d+) \| pieces (\d+) cols (\d+)'
                   r'.*?\| air-below (\d+) max (\d+) over6 (\d+) \| water-below (\d+) max (\d+) \| base water (\d+) \| base earth: (.*?) \| ring: (.*?) \| foreign: (.*)$')
EF = re.compile(r'\[earth-fill\] (\S+) at chunk \[(-?\d+), (-?\d+)\](?P<pred> \(predicted\))?(?P<rest>.*)$')
NUM = r'(\d+)(\+?)'
EF_VALUES = re.compile(r': ' + NUM + r' earth, ' + NUM + r' over water \(limit (\d+)\), ' + NUM + r' over air after beard \(limit (\d+)\), '
                       + NUM + r' mound columns.*?\(limit (\d+)\)(.*) -> (CANCELLED|kept)')
EF_PRUNE = re.compile(r': pruned (\d+) of (\d+) pieces')


def opts(a):
    o = {'box': None, 'id': None, 'except': None, 'fail': [], 'need': None}
    pos, i = [], 0
    while i < len(a):
        k = a[i]
        if k == '--box':
            o['box'] = [int(v) for v in a[i + 1:i + 5]]; i += 5
        elif k in ('--id', '--except', '--need'):
            o[k[2:]] = a[i + 1]; i += 2
        elif k == '--fail':
            o['fail'].append(a[i + 1]); i += 2
        else:
            pos.append(k); i += 1
    return pos, o


def in_box(cx, cz, box):
    return rb.in_box(cx, cz, box) if box else True


def keep_id(sid, o):
    return not o['id'] or re.search(o['id'], sid)


def read_under(log, o):
    seen = {}
    for line in open(log, errors='replace'):
        m = UNDER.search(line.rstrip('\n'))
        if not m: continue
        sid, cx, cz = m.group(1), int(m.group(2)), int(m.group(3))
        if not keep_id(sid, o) or not in_box(cx, cz, o['box']): continue
        g = m.groups()
        seen[(sid, cx, cz)] = {
            'id': sid, 'cx': cx, 'cz': cz, 'minY': int(g[4]), 'pieces': int(g[9]), 'cols': int(g[10]),
            'air': int(g[11]), 'gap': int(g[12]), 'over6': int(g[13]), 'wat': int(g[14]), 'watmax': int(g[15]),
            'basewater': int(g[16]), 'earth': g[17], 'ring': g[18], 'foreign': g[19].strip()}
    return seen


def checks(s):
    return {'over6': s['over6'] > 0, 'deepwater': s['watmax'] > 2, 'basewater': s['basewater'] > 0,
            'foreign': s['foreign'] != 'none', 'waterbelow': s['wat'] > 0}


def read_log(log, o):
    out = {}
    for line in open(log, errors='replace'):
        m = EF.search(line.rstrip('\n'))
        if not m or m.group('pred'): continue
        sid, cx, cz = m.group(1), int(m.group(2)), int(m.group(3))
        if not keep_id(sid, o) or not in_box(cx, cz, o['box']): continue
        rest = m.group('rest')
        e = out.setdefault((sid, cx, cz), {'id': sid, 'cx': cx, 'cz': cz})
        p = EF_PRUNE.search(rest)
        if p:
            e['pruned'] = (int(p.group(1)), int(p.group(2)))
            continue
        v = EF_VALUES.search(rest)
        if not v: continue
        g = v.groups()
        e['water'], e['water_lim'] = int(g[2]), int(g[4])
        e['air'], e['air_lim'] = int(g[5]), int(g[7])
        e['mound'], e['mound_lim'] = int(g[8]), int(g[10])
        e['outcome'] = g[12]
        why = []
        if 'too wet' in g[11]: why.append('too wet')
        if e['water'] > e['water_lim']: why.append('water')
        if e['air'] > e['air_lim']: why.append('air')
        if e['mound'] > e['mound_lim']: why.append('mound')
        e['why'] = why or ['?']
    return out


def cmd_under(log, o):
    s = read_under(log, o)
    by = collections.defaultdict(lambda: collections.Counter())
    bad = collections.defaultdict(list)
    for st in s.values():
        c = by[st['id']]; c['starts'] += 1
        for k, v in checks(st).items():
            if v:
                c[k] += 1
                bad[k].append(st)
    print('under: %d starts, %d structures' % (len(s), len(by)))
    for sid in sorted(by):
        c = by[sid]
        print('  %-60s starts %3d  over6 %3d  deepwater %3d  basewater %3d  foreign %3d  waterbelow %3d'
              % (sid, c['starts'], c['over6'], c['deepwater'], c['basewater'], c['foreign'], c['waterbelow']))
    rc = 0
    for k in ('over6', 'deepwater', 'basewater', 'foreign', 'waterbelow'):
        lst = bad.get(k, [])
        exc = [b for b in lst if o['except'] and re.search(o['except'], b['id'])]
        hard = [b for b in lst if b not in exc]
        print('%s: %d start(s)%s' % (k, len(hard), (' (+%d excepted)' % len(exc)) if exc else ''))
        for b in hard[:25]:
            print('    %s chunk %d %d: over6 %d gap %d, water-below max %d, base water %d, foreign %s, pieces %d'
                  % (b['id'], b['cx'], b['cz'], b['over6'], b['gap'], b['watmax'], b['basewater'], b['foreign'], b['pieces']))
        if k in o['fail'] and hard:
            rc = 1
    if o['need']:
        if not bad.get(o['need']):
            print('need %s: no start shows it' % o['need']); rc = 1
        else:
            print('need %s: %d start(s) show it' % (o['need'], len(bad[o['need']])))
    return rc


def cmd_log(log, o):
    e = read_log(log, o)
    by = collections.defaultdict(collections.Counter)
    tot = collections.Counter()
    for v in e.values():
        c = by[v['id']]
        if 'pruned' in v:
            c['pruned'] += 1; tot['pruned'] += 1
        if 'outcome' in v:
            c[v['outcome']] += 1; tot[v['outcome']] += 1
            if v['outcome'] == 'CANCELLED':
                for w in v['why']:
                    c['cancel ' + w] += 1; tot['cancel ' + w] += 1
    print('earth-fill lines (generation): %d starts; %s' % (len(e), ', '.join('%s %d' % kv for kv in sorted(tot.items()))))
    for sid in sorted(by):
        print('  %-60s %s' % (sid, ', '.join('%s %d' % kv for kv in sorted(by[sid].items()))))
    return 0


def cmd_vslog(log, o):
    s = read_under(log, o)
    e = read_log(log, o)
    rows, flagged = 0, 0
    print('| structure | chunk | logged water / air | probe water-below max / over6 | flag |')
    print('|---|---|---|---|---|')
    for k, st in sorted(s.items()):
        ln = e.get(k)
        if not ln or 'outcome' not in ln: continue
        rows += 1
        flag = []
        if ln['air'] == 0 and st['over6'] > 0: flag.append('air worse')
        if ln['water'] == 0 and st['watmax'] > 2: flag.append('water worse')
        flagged += 1 if flag else 0
        print('| %s | %d %d | %d / %d | %d / %d | %s |' % (st['id'], st['cx'], st['cz'], ln['water'], ln['air'], st['watmax'],
                                                         st['over6'], ', '.join(flag) or '-'))
    print('pairs: %d, flagged %d' % (rows, flagged))
    return 1 if flagged or rows == 0 else 0


def region_starts(level, o):
    out = {}
    for f in sorted(glob.glob(os.path.join(level, 'region', '*.mca'))):
        for d in rb.chunks(f, o['box']):
            cx, cz = d.get('xPos', 0), d.get('zPos', 0)
            if not in_box(cx, cz, o['box']): continue
            st = ((d.get('structures') or {}).get('starts')) or {}
            secs = {s.get('Y'): s for s in d.get('sections') or []}
            hm = rb.surface(d) if rb.full(d) else None
            top = (hm[8 * 16 + 8] + rb.MIN_Y - 1) if hm else None
            for sid, v in st.items():
                if not isinstance(v, dict) or v.get('id') == 'INVALID': continue
                if not keep_id(sid, o): continue
                kids = v.get('Children') or []
                sig = [((c.get('pool_element') or {}).get('location') if isinstance(c.get('pool_element'), dict) else c.get('id'),
                        c.get('PosX'), c.get('PosZ'), c.get('rotation')) for c in kids]
                out[(sid, cx, cz)] = {'pieces': len(kids), 'sig': sig,
                                      'biome': rb.biome_at(secs, 8, top, 8) if top is not None else '(not full)',
                                      'status': str(d.get('Status', '')).replace('minecraft:', '')}
            out[('__chunk__', cx, cz)] = {'status': str(d.get('Status', '')).replace('minecraft:', '')}
    return out


def cmd_gone(level, log, o):
    rs = region_starts(level, o)
    e = read_log(log, o)
    bad, notes, checked_c, checked_p = [], [], 0, 0
    for k, v in e.items():
        ch = rs.get(('__chunk__', k[1], k[2]))
        if not ch or ch['status'] in ('', 'empty'): continue
        if v.get('outcome') == 'CANCELLED':
            checked_c += 1
            if k in rs: bad.append('CANCELLED but in the world: %s chunk %d %d (%d pieces)' % (k[0], k[1], k[2], rs[k]['pieces']))
        if 'pruned' in v and v.get('outcome') != 'CANCELLED':
            checked_p += 1
            kk, n = v['pruned']
            if k not in rs:
                others = [o2[0] for o2 in rs if o2[0] != '__chunk__' and o2[1:] == k[1:]]
                if others:
                    notes.append('pruned start %s chunk %d %d not saved under its id; the chunk holds %s' % (k + (', '.join(others),)))
                else:
                    bad.append('pruned start missing: %s chunk %d %d' % k)
            elif rs[k]['pieces'] != n - kk:
                bad.append('pruned %d of %d but %d pieces saved: %s chunk %d %d' % (kk, n, rs[k]['pieces'], k[0], k[1], k[2]))
    print('gone: %d CANCELLED and %d pruned lines in saved chunks checked' % (checked_c, checked_p))
    for b in bad[:40]:
        print('  ' + b)
    for b in notes[:20]:
        print('  note: ' + b)
    print('contradictions: %d' % len(bad))
    return 1 if bad or checked_c == 0 else 0


def cmd_starts(level, o):
    rs = region_starts(level, o)
    n = 0
    for k, v in sorted(rs.items()):
        if k[0] == '__chunk__': continue
        n += 1
        print('start %s chunk %d %d (block %d %d): %d pieces, biome %s, status %s' % (k[0], k[1], k[2], k[1] * 16, k[2] * 16,
                                                                                    v['pieces'], v['biome'], v['status']))
    print('starts: %d' % n)
    return 0


def cmd_layouts(la, lb, o):
    ra, rb_ = region_starts(la, o), region_starts(lb, o)
    same = sub = diff = 0
    for k, v in sorted(ra.items()):
        if k[0] == '__chunk__' or k not in rb_: continue
        a, b = v['sig'], rb_[k]['sig']
        if a == b:
            same += 1
        elif set(a) <= set(b) or set(b) <= set(a):
            sub += 1
            print('  subset (pruned in one world): %s chunk %d %d: %d vs %d pieces' % (k[0], k[1], k[2], len(a), len(b)))
        else:
            diff += 1
            print('  DIFFERENT: %s chunk %d %d: %d vs %d pieces, first difference at piece %d'
                  % (k[0], k[1], k[2], len(a), len(b), next((i for i, (x, y) in enumerate(zip(a, b)) if x != y), min(len(a), len(b)))))
    print('layouts in both worlds: %d identical, %d pruned subsets, %d different' % (same, sub, diff))
    return 1 if diff or same == 0 else 0


def main():
    a = sys.argv[1:]
    if len(a) < 2:
        sys.exit(__doc__)
    pos, o = opts(a[1:])
    cmd = a[0]
    if cmd == 'under': rc = cmd_under(pos[0], o)
    elif cmd == 'log': rc = cmd_log(pos[0], o)
    elif cmd == 'vslog': rc = cmd_vslog(pos[0], o)
    elif cmd == 'gone': rc = cmd_gone(pos[0], pos[1], o)
    elif cmd == 'starts': rc = cmd_starts(pos[0], o)
    elif cmd == 'layouts': rc = cmd_layouts(pos[0], pos[1], o)
    else: sys.exit(__doc__)
    sys.exit(rc)


if __name__ == '__main__':
    main()
