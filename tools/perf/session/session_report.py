#!/usr/bin/env python3
"""Perf session report (feature_writeup/perf.md 14).

  session_report.py process DIR            per boot: perf_report.py HTML (boots/NN/report.html) and ticks.tsv
  session_report.py report DIR [--baseline OTHER_DIR] [--prune]
                                           process, then DIR/summary.md + summary.json

Segments: a boot runs from its process start to its end; "startup" lasts until `Done (`; after that each timeline
`mark` starts a segment named by its text (before the first mark: "(unmarked)"). Ticks are the pack's
creativecommon.ServerTick events. --prune deletes the raw recordings and the parse cache after processing
(the HTML, ticks.tsv and summary stay). Run it with no server up: parsing loads the CPU.
"""
import argparse, collections, datetime, glob, json, os, re, shutil, subprocess, sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
REPORT = os.path.join(ROOT, 'tools', 'perf', 'perf_report.py')
MD_WORST = 50
LOGT = re.compile(r'^\[(\d{2}\w{3}\d{4} \d{2}:\d{2}:\d{2}\.\d{3})\]')


def meta(b):
    m = {}
    for line in open(os.path.join(b, 'meta.env'), encoding='utf8'):
        k, _, v = line.rstrip('\n').partition('=')
        m[k] = v.strip("'")
    return m


def log_time(line):
    m = LOGT.match(line)
    return datetime.datetime.strptime(m.group(1), '%d%b%Y %H:%M:%S.%f').timestamp() if m else None


def log_facts(b):
    f = {}
    p = os.path.join(b, 'latest.log')
    if not os.path.exists(p):
        return f
    for line in open(p, encoding='utf8', errors='replace'):
        if 'Done (' in line and 'done' not in f:
            f['done'], f['done_s'] = log_time(line), float(re.search(r'Done \(([0-9.]+)s\)', line).group(1))
        elif 'Found' in line and 'recipes in' in line and 'recipes_a' not in f:
            f['recipes_a'] = log_time(line)
        elif line.find('Added ') > 0 and ' recipes, removed' in line and 'recipes_z' not in f:
            f['recipes_z'] = log_time(line)
        elif 'All dimensions are saved' in line:
            f['saved'] = log_time(line)
        elif 'Crash report saved' in line or 'Watchdog' in line and 'single server tick' in line:
            f['crash'] = line.strip()[:200]
    return f


def process(d, prune):
    name = os.path.basename(os.path.normpath(d))
    for b in sorted(glob.glob(os.path.join(d, 'boots', '*'))):
        if os.path.exists(os.path.join(b, 'ticks.tsv')):
            continue
        recs = glob.glob(os.path.join(b, '*.jfr'))
        src = recs[0] if recs else (os.path.join(b, 'repo') if os.path.isdir(os.path.join(b, 'repo')) else None)
        if not src:
            print('%s: no recording' % os.path.basename(b))
            continue
        pname = 'sess_%s_%s' % (name, os.path.basename(b))
        r = subprocess.run([sys.executable, REPORT, src, '--include-startup', '--name', pname, '--mods', os.path.join(ROOT, 'mods'),
                            '--out', os.path.join(b, 'report.html')], capture_output=True, text=True)
        print('%s: %s' % (os.path.basename(b), (r.stdout.strip().splitlines() or ['?'])[-1]))
        if r.returncode:
            sys.stderr.write(r.stderr[-2000:])
            continue
        parsed = os.path.join(ROOT, 'perf', 'parsed', pname)
        with open(os.path.join(parsed, 'events.jsonl'), encoding='utf8') as fh, \
                open(os.path.join(b, 'ticks.tsv'), 'w', encoding='utf8') as out:
            out.write('t_ms\tkind\tms\tinfo\n')
            for line in fh:
                if '"creativecommon.' not in line:
                    continue
                e = json.loads(line)
                k = e['type'][len('creativecommon.'):]
                if k == 'ServerTick':
                    out.write('%d\ttick\t%.3f\t\n' % (e['t'] + e['dur'], e['dur']))
                elif k == 'ChunkWait':
                    out.write('%d\tchunkwait\t%.3f\t%s\n' % (e['t'], e.get('waitTime') or 0, e.get('dimension') or ''))
                elif k == 'KubeJSHandler':
                    out.write('%d\tkubejs\t%.3f\t%s:%s %s\n' % (e['t'], e['dur'], e.get('source'), e.get('line'), e.get('event')))
        if prune:
            shutil.rmtree(parsed, ignore_errors=True)
            for f in recs:
                os.remove(f)
            shutil.rmtree(os.path.join(b, 'repo'), ignore_errors=True)


def pct(v, q):
    return v[min(len(v) - 1, int(q * len(v)))] if v else None


def summarize(d):
    tl = []
    for line in open(os.path.join(d, 'timeline.tsv'), encoding='utf8'):
        p = line.rstrip('\n').split('\t')
        if len(p) >= 4:
            tl.append((float(p[0]), p[1], p[2], p[3]))
    boots, segs, worst = [], [], []
    for b in sorted(glob.glob(os.path.join(d, 'boots', '*'))):
        m, f = meta(b), log_facts(b)
        start, end = float(m['START']), float(m.get('END') or os.path.getmtime(os.path.join(b, 'proc.tsv')))
        ticks, waits, kjs = [], [], []
        tp = os.path.join(b, 'ticks.tsv')
        if os.path.exists(tp):
            for row in open(tp, encoding='utf8').readlines()[1:]:
                t, k, ms, info = row.rstrip('\n').split('\t')
                (ticks if k == 'tick' else waits if k == 'chunkwait' else kjs).append((int(t) / 1000.0, float(ms), info))
        proc = []
        for r in open(os.path.join(b, 'proc.tsv'), encoding='utf8').readlines()[1:]:
            try:
                proc.append(tuple(map(float, r.split('\t'))))
            except ValueError:  # a row written while the JVM exited has empty fields
                pass
        if 'saved' in f:
            ended = 'clean stop'
        elif 'crash' in f:
            ended = 'crash: ' + f['crash']
        else:
            ended = 'no clean stop in the log'
        boot = {'boot': os.path.basename(b), 'level': m.get('LEVEL'), 'type': m.get('TYPE'), 'jars': m.get('JARS'), 'ended': ended,
                'wall_to_done_s': round(f['done'] - start, 1) if 'done' in f else None, 'done_s': f.get('done_s'),
                'recipe_event_s': round(f['recipes_z'] - f['recipes_a'], 1) if 'recipes_a' in f and 'recipes_z' in f else None,
                'uptime_min': round((end - start) / 60, 1), 'rss_max_mb': max((r[2] for r in proc), default=None),
                'report': os.path.relpath(os.path.join(b, 'report.html'), d) if os.path.exists(os.path.join(b, 'report.html')) else None}
        boots.append(boot)
        # segment edges inside this boot
        edges = [(start, 'startup')]
        if 'done' in f:
            edges.append((f['done'], '(unmarked)'))
        edges += [(t, txt) for t, k, _, txt in tl if k == 'mark' and start < t < end]
        edges.sort()
        cmds = [(t, txt) for t, k, _, txt in tl if k == 'cmd' and start <= t <= end]
        for i, (a, label) in enumerate(edges):
            z = edges[i + 1][0] if i + 1 < len(edges) else end
            if z - a < 1:
                continue
            d_ms = sorted(ms for t, ms, _ in ticks if a <= t < z)
            pr = [r for r in proc if a <= r[0] <= z]
            cpu = (pr[-1][1] - pr[0][1]) / (pr[-1][0] - pr[0][0]) if len(pr) > 1 and pr[-1][0] > pr[0][0] else None
            segs.append({'boot': boot['boot'], 'segment': label, 'seconds': round(z - a), 'ticks': len(d_ms),
                         'median_ms': pct(d_ms, .5), 'p95_ms': pct(d_ms, .95), 'max_ms': d_ms[-1] if d_ms else None,
                         'over50': sum(1 for x in d_ms if x > 50), 'busy_pct': round(sum(d_ms) / (10 * (z - a)), 1),
                         'cpu_cores': round(cpu, 2) if cpu is not None else None,
                         'rss_max_mb': max((r[2] for r in pr), default=None),
                         'chunk_wait_ms': round(sum(ms for t, ms, _ in waits if a <= t < z)),
                         'slow_kubejs': sum(1 for t, _, _ in kjs if a <= t < z)})
            for t, ms, _ in ticks:
                if a <= t < z and ms > 50:
                    before = [c for ct, c in cmds if t - 60 <= ct <= t]
                    worst.append({'time': datetime.datetime.fromtimestamp(t).strftime('%H:%M:%S'), 'boot': boot['boot'],
                                  'segment': label, 'ms': round(ms, 1), 'last_cmds': before})
    worst.sort(key=lambda w: -w['ms'])
    # every tick over 50 ms is kept (summary.json, worst_ticks.tsv); summary.md shows the top MD_WORST
    return {'session': os.path.basename(os.path.normpath(d)), 'boots': boots, 'segments': segs, 'worst_ticks': worst}


def fmt(v, nd=1):
    return '' if v is None else ('%.*f' % (nd, v) if isinstance(v, float) else str(v))


def write_md(d, s, base):
    bseg = {x['segment']: x for x in base['segments']} if base else {}
    L = ['# Perf session %s' % s['session'], '']
    if base:
        L += ['Baseline: %s (segments matched by name; change = this run minus baseline).' % base['session'], '']
    L += ['## Boots', '', '| Boot | Type | Jars | Launch to Done (s) | `Done (` (s) | Recipe event (s) | Up (min) | RSS max (MB) | Ended | Report |',
          '|---|---|---|---|---|---|---|---|---|---|']
    for b in s['boots']:
        L.append('| %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |' % (b['boot'], b['type'], b['jars'], fmt(b['wall_to_done_s']), fmt(b['done_s']),
                 fmt(b['recipe_event_s']), fmt(b['uptime_min']), fmt(b['rss_max_mb'], 0), b['ended'], b['report'] or 'not processed'))
    L += ['', '## Segments', '', '| Boot | Segment | s | Ticks | Median ms | p95 ms | Max ms | Over 50 ms | Busy % | CPU cores | RSS max | Chunk wait ms | Slow KubeJS |',
          '|---|---|---|---|---|---|---|---|---|---|---|---|---|']
    for g in s['segments']:
        o = bseg.get(g['segment'])
        def c(k, nd=1):
            v = fmt(g[k], nd)
            if o and g[k] is not None and o.get(k) is not None:
                v += ' (%+.*f)' % (nd, g[k] - o[k])
            return v
        L.append('| %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |' % (g['boot'], g['segment'], g['seconds'], g['ticks'], c('median_ms', 2),
                 c('p95_ms'), c('max_ms', 0), c('over50', 0), c('busy_pct'), c('cpu_cores', 2), fmt(g['rss_max_mb'], 0), c('chunk_wait_ms', 0), g['slow_kubejs']))
    with open(os.path.join(d, 'worst_ticks.tsv'), 'w', encoding='utf8') as fh:
        fh.write('time\tboot\tsegment\tms\tcommands_60s_before\n')
        for w in s['worst_ticks']:
            fh.write('%s\t%s\t%s\t%s\t%s\n' % (w['time'], w['boot'], w['segment'], w['ms'], ' ; '.join(w['last_cmds'])))
    L += ['', '## Worst ticks (over 50 ms)', '', '%d ticks over 50 ms; all of them in worst_ticks.tsv and summary.json, the %d longest here.'
          % (len(s['worst_ticks']), min(MD_WORST, len(s['worst_ticks']))), '',
          '| Time | Boot | Segment | ms | Last commands in the 60 s before |', '|---|---|---|---|---|']
    for w in s['worst_ticks'][:MD_WORST]:
        L.append('| %s | %s | %s | %s | %s |' % (w['time'], w['boot'], w['segment'], w['ms'], '; '.join('`%s`' % x for x in w['last_cmds'][-3:])))
    open(os.path.join(d, 'summary.md'), 'w', encoding='utf8').write('\n'.join(L) + '\n')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('what', choices=['process', 'report'])
    ap.add_argument('dir')
    ap.add_argument('--baseline')
    ap.add_argument('--prune', action='store_true')
    a = ap.parse_args()
    if subprocess.run(['bash', os.path.join(ROOT, 'tools', 'scratch_query', 'game_jvms.sh')], capture_output=True, text=True).stdout.strip():
        print('note: a game JVM is up; parsing now loads the CPU it is using')
    process(a.dir, a.prune)
    if a.what == 'process':
        return
    s = summarize(a.dir)
    base = None
    if a.baseline:
        base = json.load(open(os.path.join(a.baseline, 'summary.json'), encoding='utf8'))
    json.dump(s, open(os.path.join(a.dir, 'summary.json'), 'w', encoding='utf8'), indent=1)
    write_md(a.dir, s, base)
    print('summary: ' + os.path.relpath(os.path.join(a.dir, 'summary.md'), os.getcwd()))


if __name__ == '__main__':
    main()
