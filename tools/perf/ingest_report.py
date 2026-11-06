#!/usr/bin/env python3
"""
Opens a server report made by server/perf/report.sh and turns it into one page to start from.

  python3 tools/perf/ingest_report.py <report.zip | report.tar.gz | unpacked folder> [--baseline FILE]

Unpacks into perf/incoming/<name>/ and writes perf/incoming/<name>/index.html with:
  - which pack version the server runs (its mods/ hashes matched against deploy/history/*.json) and the
    config/kubejs files that differ from that version
  - "Can't keep up" lines and crashes placed on the recording's tick-time timeline
  - log summary: KubeJS "Error occurred while handling event", exceptions by type, ERROR lines by logger
  - crash report heads, JVM crash files, the Server thread in each live thread dump, the machine snapshot
  - links to perf.html (the live recording) and previous-<run>.html (the run before: its dump on exit, which
    covers watchdog crashes, or the folder a hard-killed JVM left behind), made
    by perf_report.py with the jars that server actually ran, found by hash in mods/, deploy/latest/mods,
    server_mods/ and development/<mod>/jar_archive + build/libs

Options:
  --baseline FILE   an earlier .summary.json, passed on to perf_report.py ("change" columns)
  --no-perf         skip the perf_report.py runs (log summary only)
"""
import argparse
import collections
import datetime
import glob
import hashlib
import html
import json
import os
import re
import shutil
import subprocess
import sys
import tarfile
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
import report_html as R  # noqa: E402

INCOMING = os.path.join(ROOT, 'perf', 'incoming')
JAR_DIRS = [os.path.join(ROOT, d) for d in ('mods', 'deploy/latest/mods', 'server_mods', 'development/creativeCommon/jar_archive',
                                            'development/creativeCommon/build/libs',
                                            'development/exposureBackport/jar_archive',
                                            'development/exposureBackport/build/libs')]
JAR_CACHE = os.path.join(ROOT, 'perf', '.cache', 'jar_sha1.json')

LOG_TS = re.compile(r'^\[(\d\d)([A-Z][a-z]{2})(\d{4}) (\d\d):(\d\d):(\d\d)\.(\d{3})\] \[([^\]/]+)/(\w+)\] \[([^\]]*)\]: (.*)')
MONTHS = {m: i + 1 for i, m in enumerate('Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec'.split())}
BEHIND = re.compile(r"Running (\d+)ms or (\d+) ticks behind")
EXC = re.compile(r'(?:^|Caused by: |Exception in thread "[^"]*" |\s)((?:[a-z_$][\w$]*\.)+[A-Z][\w$]*(?:Exception|Error|Throwable))(?=:|\s*$)')


# ---------------------------------------------------------------- unpacking

def unpack(src):
    """Returns the report folder (the archive holds one top folder named like the archive)."""
    if os.path.isdir(src):
        return os.path.abspath(src)
    base = os.path.basename(src)
    name = re.sub(r'(\.zip|\.tar\.gz|\.tgz)$', '', base)
    dest = os.path.join(INCOMING, name)
    if os.path.isdir(dest):
        print('already unpacked: ' + os.path.relpath(dest, ROOT))
        return dest
    os.makedirs(INCOMING, exist_ok=True)
    tmp = dest + '.tmp'
    shutil.rmtree(tmp, ignore_errors=True)
    if base.endswith('.zip'):
        with zipfile.ZipFile(src) as z:  # ZipFile strips '..' and absolute paths itself
            z.extractall(tmp)
    else:
        with tarfile.open(src) as t:
            for m in t.getmembers():
                p = os.path.normpath(m.name)
                if p.startswith('..') or os.path.isabs(p) or not (m.isfile() or m.isdir()):
                    sys.exit('refusing unsafe archive member: ' + m.name)
            t.extractall(tmp)
    inner = [d for d in os.listdir(tmp) if os.path.isdir(os.path.join(tmp, d))]
    os.rename(os.path.join(tmp, inner[0]) if len(inner) == 1 and not os.path.exists(os.path.join(tmp, 'REPORT.md'))
              else tmp, dest)
    shutil.rmtree(tmp, ignore_errors=True)
    print('unpacked to ' + os.path.relpath(dest, ROOT))
    return dest


def read(path, limit=None):
    try:
        with open(path, encoding='utf8', errors='replace') as fh:
            return fh.read(limit) if limit else fh.read()
    except OSError:
        return ''


# ---------------------------------------------------------------- version + drift

def server_hashes(d):
    out = {}
    for line in read(os.path.join(d, 'files.sha1')).splitlines():
        h, _, p = line.partition('  ')
        if p:
            out[p] = h
    return out


def match_version(hashes):
    """Best deploy/history version by mods/ jars, plus per-folder differences against it."""
    srv_mods = {p[5:]: h for p, h in hashes.items() if p.startswith('mods/')}
    best = None
    for path in glob.glob(os.path.join(ROOT, 'deploy', 'history', '*.json')):
        with open(path, encoding='utf8') as fh:
            hist = json.load(fh)
        hm = hist['files'].get('mods', {})
        same = sum(1 for j, h in srv_mods.items() if hm.get(j) == h)
        score = (same - len(set(srv_mods) ^ set(hm)), hist.get('date', ''))
        if best is None or score > best[0]:
            best = (score, hist)
    if not best:
        return None
    hist = best[1]
    res = {'version': hist['version'], 'date': hist.get('date', ''), 'folders': {}}
    for folder, prefix in (('mods', 'mods/'), ('config', 'config/'), ('defaultconfigs', 'defaultconfigs/'),
                           ('kubejs', 'kubejs/')):
        want = hist['files'].get(folder, {})
        have = {p[len(prefix):]: h for p, h in hashes.items() if p.startswith(prefix)}
        res['folders'][folder] = {
            'shipped': len(want),
            'changed': sorted(p for p, h in want.items() if p in have and have[p] != h),
            'missing': sorted(p for p in want if p not in have),
            'extra': sorted(p for p in have if p not in want),
        }
    return res


def jar_index():
    """sha1 -> local jar path over JAR_DIRS, cached by (path, size, mtime)."""
    try:
        with open(JAR_CACHE, encoding='utf8') as fh:
            cache = json.load(fh)
    except (OSError, ValueError):
        cache = {}
    out, fresh = {}, {}
    for d in JAR_DIRS:
        for p in glob.glob(os.path.join(d, '*.jar')):
            st = os.stat(p)
            key = '%s|%d|%d' % (p, st.st_size, int(st.st_mtime))
            h = cache.get(key)
            if not h:
                with open(p, 'rb') as fh:
                    h = hashlib.sha1(fh.read()).hexdigest()
            fresh[key] = h
            out.setdefault(h, p)
    os.makedirs(os.path.dirname(JAR_CACHE), exist_ok=True)
    with open(JAR_CACHE, 'w', encoding='utf8') as fh:
        json.dump(fresh, fh)
    return out


def build_mods_dir(d, hashes):
    """perf/incoming/<name>/mods/: symlinks to local copies of exactly the jars the server ran."""
    idx = jar_index()
    md = os.path.join(d, 'mods')
    shutil.rmtree(md, ignore_errors=True)
    os.makedirs(md)
    missing = []
    for p, h in sorted(hashes.items()):
        if not p.startswith('mods/') or not p.endswith('.jar'):
            continue
        src = idx.get(h)
        if src:
            os.symlink(src, os.path.join(md, os.path.basename(p)))
        else:
            missing.append(p[5:])
    return md, missing


# ---------------------------------------------------------------- logs

class LogLine:
    __slots__ = ('ts', 'thread', 'level', 'logger', 'msg', 'extra')

    def __init__(self, m):
        dd, mon, yy, hh, mi, ss, ms = m.group(1, 2, 3, 4, 5, 6, 7)
        self.ts = datetime.datetime(int(yy), MONTHS.get(mon, 1), int(dd), int(hh), int(mi), int(ss), int(ms) * 1000)
        self.thread, self.level, self.logger, self.msg = m.group(8), m.group(9), m.group(10), m.group(11)
        self.extra = []


def parse_log(path):
    lines, cur = [], None
    with open(path, encoding='utf8', errors='replace') as fh:
        for raw in fh:
            raw = raw.rstrip('\n')
            m = LOG_TS.match(raw)
            if m:
                cur = LogLine(m)
                lines.append(cur)
            elif cur is not None and len(cur.extra) < 400:
                cur.extra.append(raw)
    return lines


def norm(msg):
    """Groups messages that differ only by numbers, coordinates, UUIDs or quoted names."""
    msg = re.sub(r'[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}', '<uuid>', msg)
    msg = re.sub(r"'[^']{1,80}'", "'…'", msg)
    msg = re.sub(r'-?\d+(\.\d+)?', '#', msg)
    return msg[:220]


def summarize_logs(lines):
    lag = [(l.ts, int(BEHIND.search(l.msg).group(1)), int(BEHIND.search(l.msg).group(2)))
           for l in lines if "Can't keep up" in l.msg and BEHIND.search(l.msg)]
    kjs = collections.Counter()
    kjs_first = {}
    errors = collections.Counter()
    err_first = {}
    exc = collections.Counter()
    exc_first = {}
    levels = collections.Counter(l.level for l in lines)
    for l in lines:
        if 'Error occurred while handling event' in l.msg:
            detail = (l.extra[0].strip() if l.extra else '')
            k = norm(l.msg + ' ' + detail)
            kjs[k] += 1
            kjs_first.setdefault(k, (l.ts, l.msg + ('\n' + detail if detail else '')))
        if l.level in ('ERROR', 'FATAL'):
            k = (l.logger, norm(l.msg))
            errors[k] += 1
            err_first.setdefault(k, (l.ts, l.msg + ('\n' + '\n'.join(l.extra[:6]) if l.extra else '')))
        for text in [l.msg] + l.extra[:200]:
            m = EXC.search(text)
            if m:
                exc[m.group(1)] += 1
                exc_first.setdefault(m.group(1), (l.ts, text.strip()[:300]))
    return {'lag': lag, 'kjs': kjs, 'kjs_first': kjs_first, 'errors': errors, 'err_first': err_first,
            'exc': exc, 'exc_first': exc_first, 'levels': levels,
            'first': lines[0].ts if lines else None, 'last': lines[-1].ts if lines else None}


def crash_head(path):
    t = read(path)
    desc = re.search(r'^Description: (.*)$', t, re.M)
    when = re.search(r'^Time: (.*)$', t, re.M)
    # the exception and its first frames, right after the description
    body = t.split('\n\n', 3)
    stack = ''
    m = re.search(r'^Description: [^\n]*\n\n(.*?)(\n\n|\Z)', t, re.M | re.S)
    jars = []
    if m:
        # drop the transformer list Forge appends to each frame ({re:mixin,pl:accesstransformer:B,...})
        stack = '\n'.join(re.sub(r' \{[^{}]*\}$', '', ln) for ln in m.group(1).splitlines()[:16])
        # frames name their jar: "at a.b.C.m(C.java:1) ~[somemod-1.0.jar%23123!/:?]"; vanilla/forge/libs aside
        for j in re.findall(r'~\[([^\]%]+?\.jar)', m.group(1)):
            if j not in jars and not re.match(r'(server-1\.19|forge-1\.19|fmlcore|javafmllanguage|modlauncher|'
                                              r'securejarhandler|brigadier|datafixerupper|eventbus|netty|guava|fastutil)', j):
                jars.append(j)
    srv = re.search(r'"Server thread".*?(?=\n\n)', t, re.S)
    return {'file': os.path.basename(path), 'description': desc.group(1) if desc else '?',
            'time': when.group(1) if when else '', 'stack': stack or '\n'.join(body[:2])[:1500],
            'server_thread': '\n'.join(srv.group(0).splitlines()[:20]) if srv else '', 'jars': jars}


def server_thread(dump):
    m = re.search(r'^"Server thread".*?(?=\n\n)', read(dump), re.M | re.S)
    return '\n'.join(m.group(0).splitlines()[:25]) if m else '(no Server thread in this dump)'


# ---------------------------------------------------------------- perf

def run_perf(rec, name, out, mods, baseline):
    """Runs perf_report.py; returns (error or None, note or None)."""
    cmd = [sys.executable, os.path.join(HERE, 'perf_report.py'), rec, '--name', name, '--out', out, '--mods', mods]
    if baseline:
        cmd += ['--baseline', baseline]
    print('perf_report: ' + os.path.relpath(rec, ROOT))
    r = subprocess.run(cmd, capture_output=True, text=True)
    m = re.search(r'^window (\d+) s', r.stdout, re.M)
    # perf_report starts the window at the first non-zero tick average; a server that lagged or crashed right
    # after boot has almost nothing past that point, so keep the boot instead
    if 'empty window' in r.stderr or (m and int(m.group(1)) < 30):
        print('  short window after boot, rerunning with --include-startup')
        r = subprocess.run(cmd + ['--include-startup'], capture_output=True, text=True)
        note = 'Includes the server start: the recording has under 30 s of ticking after boot.'
    else:
        note = None
    sys.stdout.write(r.stdout)
    if r.returncode:
        sys.stderr.write(r.stderr[-3000:])
        return 'perf_report.py failed: ' + (r.stderr.strip().splitlines() or ['?'])[-1], None
    return None, note


def tick_series(name):
    """[(epoch_ms, mspt)] from the parsed recording."""
    out = []
    path = os.path.join(ROOT, 'perf', 'parsed', name, 'events.jsonl')
    if not os.path.exists(path):
        return out
    with open(path, encoding='utf8') as fh:
        for line in fh:
            if '"minecraft.ServerTickTime"' in line:
                e = json.loads(line)
                out.append((e['t'], e.get('averageTickDuration') or 0.0))
    return sorted(out)


def server_tz(d):
    m = re.search(r'^- Created: .* ([+-]\d{4}) on ', read(os.path.join(d, 'REPORT.md')), re.M)
    if not m:
        return datetime.timezone.utc, False
    s = m.group(1)
    off = datetime.timedelta(hours=int(s[1:3]), minutes=int(s[3:5]))
    return datetime.timezone(-off if s[0] == '-' else off), True


def timeline(series, marks, tz):
    """Tick time with markers: series [(epoch_ms, mspt)], marks [(naive local datetime, label, kind)]."""
    if len(series) < 2:
        return ''
    t0 = series[0][0]
    pts = [[(t - t0) / 1000.0, v] for t, v in series]
    chart = R.line_chart([('tick time (100-tick average)', pts, '--s1')], 'ms', ref=50)
    x1 = pts[-1][0] or 1
    W, L, Rr = 720, 44, 12
    ticks = []
    for ts, label, kind in marks:
        x = (ts.replace(tzinfo=tz).timestamp() * 1000 - t0) / 1000.0
        if -5 <= x <= x1 + 5:
            sx = L + max(0, min(x, x1)) / x1 * (W - L - Rr)
            ticks.append('<line x1="%.1f" x2="%.1f" y1="2" y2="22" stroke="var(%s)" stroke-width="2" data-tip="%s"/>'
                         % (sx, sx, '--bad' if kind == 'crash' else '--s2', R.esc('%s %s' % (R.mmss(x), label))))
    strip = ('<svg viewBox="0 0 %d 24" width="100%%" role="img" aria-label="log events on the same time axis">'
             '<line x1="%d" x2="%d" y1="12" y2="12" stroke="var(--grid)"/>%s</svg>' % (W, L, W - Rr, ''.join(ticks)))
    return chart + strip + ('<p class="note">Marks under the chart: orange = "Can\'t keep up", red = crash report. '
                            'Hover for the time. Tick time is Minecraft\'s 100-tick average, so short spikes look smaller, and a '
                            'hung tick never finishes: before a watchdog crash the line stays flat until the red mark.</p>')


# ---------------------------------------------------------------- page

def pre(text):
    return '<pre style="white-space:pre-wrap;font-size:12px;margin:6px 0">%s</pre>' % R.esc(text)


def render(d, ctx):
    e = R.esc
    p = []
    p.append('<h1>Server report %s</h1>' % e(ctx['name']))
    p.append('<p class="meta">%s</p>' % e(ctx['created']))
    v = ctx['version']
    lg = ctx['logs']
    p.append('<div class="tiles">')
    p.append(R.tile('Pack version', v['version'] if v else '?', ('mods match' if v and not (
        v['folders']['mods']['changed'] or v['folders']['mods']['missing'] or v['folders']['mods']['extra'])
        else 'mods differ, see below') if v else 'no files.sha1'))
    p.append(R.tile("Can't keep up", str(len(lg['lag'])) if lg else '-', 'in latest.log'))
    p.append(R.tile('Crash reports', str(len(ctx['crashes'])), 'last 24 h + newest'))
    p.append(R.tile('ERROR lines', str(lg['levels'].get('ERROR', 0)) if lg else '-', 'latest.log'))
    p.append(R.tile('KubeJS event errors', str(sum(lg['kjs'].values())) if lg else '-', 'latest.log'))
    p.append('</div>')

    links = []
    for label, f, err, note in ctx['perf']:
        links.append('<li>%s%s</li>' % (('<a href="%s">%s</a>' % (e(f), e(label))) if not err else
                                        '%s: %s' % (e(label), e(err)), (' <span class="meta">(%s)</span>' % e(note)) if note else ''))
    p.append('<div class="card"><b>Reports</b><ul>%s<li><a href="REPORT.md">REPORT.md</a> (from the server)</li></ul>'
             '</div>' % ''.join(links or ['<li>No recording in this report.</li>']))
    if ctx['notes']:
        p.append('<div class="card warn"><b>Notes from the server</b>%s</div>' % pre(ctx['notes']))

    if ctx['timeline']:
        p.append('<h2>Timeline</h2>' + ctx['timeline'])

    p.append('<h2>Lag lines</h2>')
    if lg and lg['lag']:
        rows = [[e(ts.strftime('%F %T')), '%d ms' % ms, str(t)] for ts, ms, t in lg['lag'][-40:]]
        p.append('<p class="note">"Can\'t keep up" is logged when the server falls more than 2 s (40 ticks) behind, '
                 'at most once every 15 s. Last 40 shown.</p>' + R.table(['time (server clock)', 'behind', 'ticks'], rows, (1, 2)))
    else:
        p.append('<p class="note">None in latest.log.</p>')

    p.append('<h2>Crashes</h2>')
    if not ctx['crashes'] and not ctx['hserr']:
        p.append('<p class="note">No crash reports or JVM crash files in the report.</p>')
    for c in ctx['crashes']:
        p.append('<div class="card"><b>%s</b> <span class="meta">%s</span><br>%s%s%s%s</div>' % (
            e(c['description']), e(c['time']), e(c['file']),
            ('<br>Mod jars in the stack: <code>%s</code>' % e(', '.join(c['jars']))) if c['jars'] else '', pre(c['stack']),
            ('<details><summary>Server thread in the crash report</summary>%s</details>' % pre(c['server_thread']))
            if c['server_thread'] else ''))
    for name, head in ctx['hserr']:
        p.append('<div class="card"><b>JVM crash</b> %s%s</div>' % (e(name), pre(head)))

    if lg:
        p.append('<h2>KubeJS event errors</h2>')
        if lg['kjs']:
            p.append(R.table(['count', 'first seen', 'message'], [
                [str(n), e(lg['kjs_first'][k][0].strftime('%T')), pre(lg['kjs_first'][k][1])]
                for k, n in lg['kjs'].most_common(20)], (0,)))
        else:
            p.append('<p class="note">None.</p>')
        p.append('<h2>Exceptions by type</h2><p class="note">Every line in latest.log naming an exception class, '
                 'stack traces included, so one failure can count several times (Caused by: ...).</p>')
        p.append(R.table(['count', 'type', 'first seen', 'first line'], [
            [str(n), '<code>%s</code>' % e(k), e(lg['exc_first'][k][0].strftime('%T')), '<code>%s</code>' % e(lg['exc_first'][k][1])]
            for k, n in lg['exc'].most_common(25)], (0,)) if lg['exc'] else '<p class="note">None.</p>')
        p.append('<h2>ERROR lines by logger</h2>')
        p.append(R.table(['count', 'logger', 'first seen', 'message'], [
            [str(n), '<code>%s</code>' % e(k[0]), e(lg['err_first'][k][0].strftime('%T')),
             '<details><summary>%s</summary>%s</details>' % (e(k[1][:140]), pre(lg['err_first'][k][1]))]
            for k, n in lg['errors'].most_common(30)], (0,)) if lg['errors'] else '<p class="note">None.</p>')

    if ctx['dumps']:
        p.append('<h2>Server thread in the live thread dumps</h2><p class="note">Taken 3 s apart. The same deep '
                 'frames in every dump mean the server thread is stuck there.</p>')
        for name, txt in ctx['dumps']:
            p.append('<details%s><summary>%s</summary>%s</details>' % (' open' if name.endswith('-1.txt') else '',
                                                                        e(name), pre(txt)))

    if v:
        p.append('<h2>Files against %s</h2>' % e(v['version']))
        p.append('<p class="note">Hashes from the server compared with deploy/history/%s.json (shipped %s). Mods '
                 'rewrite their config files on load, so a few "changed" configs are normal; hand edits show here '
                 'too. "Extra" files were not shipped (made by the server or copied by hand).</p>' % (
                     e(v['version']), e(v['date'])))
        rows = []
        for folder, f in v['folders'].items():
            det = ''.join('<details><summary>%s (%d)</summary>%s</details>' % (lab, len(f[k]), pre('\n'.join(f[k][:300])))
                          for lab, k in (('changed', 'changed'), ('missing', 'missing'), ('extra', 'extra')) if f[k])
            rows.append([e(folder), str(f['shipped']), str(len(f['changed'])), str(len(f['missing'])),
                         str(len(f['extra'])), det])
        p.append(R.table(['folder', 'shipped', 'changed', 'missing', 'extra', ''], rows, (1, 2, 3, 4)))
    if ctx['missing_jars']:
        p.append('<p class="note warn">%d server jars have no local copy with the same hash, so their classes are '
                 'unattributed in the perf reports: %s</p>' % (len(ctx['missing_jars']), e(', '.join(ctx['missing_jars']))))

    if ctx['system']:
        p.append('<h2>Machine</h2><details><summary>system.txt</summary>%s</details>' % pre(ctx['system']))
    return ('<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" '
            'content="width=device-width,initial-scale=1"><title>Server report</title><style>%s</style></head>'
            '<body><main>%s</main><div id="tip"></div><script>%s</script></body></html>' % (R.CSS, ''.join(p), R.JS))


# ---------------------------------------------------------------- main

def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n\n')[1], formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('report')
    ap.add_argument('--baseline')
    ap.add_argument('--no-perf', action='store_true')
    args = ap.parse_args()

    d = unpack(args.report)
    name = os.path.basename(d)
    report_md = read(os.path.join(d, 'REPORT.md'))
    created = re.search(r'^- Created: (.*)$', report_md, re.M)
    window = re.search(r'^- Window: (.*)$', report_md, re.M)
    notes = re.search(r'^## Notes\n\n(.*?)\n\n## ', report_md, re.M | re.S)
    notes = notes.group(1).strip() if notes else ''
    ctx = {'name': name, 'notes': '' if notes == '- none' else notes,
           'created': 'Created %s. Window %s.' % (created.group(1) if created else '?', window.group(1) if window else '?')}

    hashes = server_hashes(d)
    ctx['version'] = match_version(hashes) if hashes else None
    mods, ctx['missing_jars'] = build_mods_dir(d, hashes) if hashes else (None, [])
    if not hashes or not os.listdir(mods):
        dl = os.path.join(ROOT, 'mods')
        mods = dl
        ctx['missing_jars'] = []

    ctx['perf'] = []
    series = []
    if not args.no_perf:
        live = next((os.path.join(d, f) for f in ('recording.jfr', 'recording') if os.path.exists(os.path.join(d, f))), None)
        if live:
            err, note = run_perf(live, 'incoming-' + name, os.path.join(d, 'perf.html'), mods, args.baseline)
            ctx['perf'].append(('Live recording (perf.html)', 'perf.html', err, note))
            series = tick_series('incoming-' + name)
        for prev in sorted(glob.glob(os.path.join(d, 'previous-run', '*'))):
            tag = re.sub(r'\.jfr$', '', os.path.basename(prev))
            how = ('stopped or crashed through the watchdog (dump on exit)' if prev.endswith('.jfr') else
                   'killed hard: its leftover recording folder')
            err, note = run_perf(prev, 'incoming-%s-prev-%s' % (name, tag), os.path.join(d, 'previous-%s.html' % tag),
                                 mods, None)
            ctx['perf'].append(('Previous run, %s: %s' % (how, tag), 'previous-%s.html' % tag, err, note))
            if not series:
                series = tick_series('incoming-%s-prev-%s' % (name, tag))

    logp = os.path.join(d, 'logs', 'latest.log')
    ctx['logs'] = summarize_logs(parse_log(logp)) if os.path.exists(logp) else None
    ctx['crashes'] = [crash_head(f) for f in sorted(glob.glob(os.path.join(d, 'crash-reports', '*.txt')))]
    ctx['hserr'] = [(os.path.basename(f), '\n'.join(read(f, 6000).splitlines()[:30]))
                    for f in sorted(glob.glob(os.path.join(d, 'hs_err_pid*.log')))]
    ctx['dumps'] = [(os.path.basename(f), server_thread(f)) for f in sorted(glob.glob(os.path.join(d, 'jvm', 'threads-*.txt')))]
    ctx['system'] = read(os.path.join(d, 'system.txt'))

    tz, _ = server_tz(d)
    marks = [(ts, "Can't keep up: %d ms behind" % ms, 'lag') for ts, ms, _ in (ctx['logs']['lag'] if ctx['logs'] else [])]
    for c in ctx['crashes']:
        m = re.match(r'crash-(\d{4})-(\d\d)-(\d\d)_(\d\d)\.(\d\d)\.(\d\d)', c['file'])
        if m:
            marks.append((datetime.datetime(*map(int, m.groups())), 'crash: ' + c['description'], 'crash'))
    ctx['timeline'] = timeline(series, marks, tz)

    out = os.path.join(d, 'index.html')
    with open(out, 'w', encoding='utf8') as fh:
        fh.write(render(d, ctx))
    v = ctx['version']
    lg = ctx['logs']
    print('version: %s%s' % (v['version'] if v else '?', '' if not v or not (
        v['folders']['mods']['changed'] or v['folders']['mods']['missing'] or v['folders']['mods']['extra'])
        else ' (mods differ)'))
    if lg:
        print("latest.log: %d can't-keep-up, %d ERROR, %d KubeJS event errors" % (
            len(lg['lag']), lg['levels'].get('ERROR', 0), sum(lg['kjs'].values())))
    print('crash reports: %d' % len(ctx['crashes']))
    print('page: ' + os.path.relpath(out, os.getcwd()))


if __name__ == '__main__':
    main()
