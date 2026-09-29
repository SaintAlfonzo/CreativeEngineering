"""Renders perf_report.py's analysis as one self-contained HTML page (inline CSS/SVG/JS, no network)."""
import html
import json
import math
import time

CSS = """
:root{color-scheme:light;--page:#f9f9f7;--surface:#fcfcfb;--ink:#0b0b0b;--ink2:#52514e;--muted:#898781;
--grid:#e1e0d9;--axis:#c3c2b7;--border:rgba(11,11,11,.10);--s1:#2a78d6;--s2:#eb6834;--wash:rgba(42,120,214,.10);
--track:#f0efec;--good:#006300;--bad:#d03b3b}
@media (prefers-color-scheme:dark){:root:where(:not([data-theme="light"])){color-scheme:dark;--page:#0d0d0d;
--surface:#1a1a19;--ink:#fff;--ink2:#c3c2b7;--grid:#2c2c2a;--axis:#383835;--border:rgba(255,255,255,.10);
--s1:#3987e5;--s2:#d95926;--wash:rgba(57,135,229,.12);--track:#262624;--good:#0ca30c;--bad:#e66767}}
:root[data-theme="dark"]{color-scheme:dark;--page:#0d0d0d;--surface:#1a1a19;--ink:#fff;--ink2:#c3c2b7;
--grid:#2c2c2a;--axis:#383835;--border:rgba(255,255,255,.10);--s1:#3987e5;--s2:#d95926;--wash:rgba(57,135,229,.12);
--track:#262624;--good:#0ca30c;--bad:#e66767}
*{box-sizing:border-box}
body{margin:0;background:var(--page);color:var(--ink);font:14px/1.45 system-ui,-apple-system,"Segoe UI",sans-serif}
main{max-width:1100px;margin:0 auto;padding:24px 16px 64px}
h1{font-size:24px;margin:0 0 4px}h2{font-size:18px;margin:36px 0 4px}h3{font-size:15px;margin:20px 0 6px}
.meta,.note{color:var(--ink2)}.note{font-size:13px;margin:4px 0 12px;max-width:80ch}
.card{background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:16px;margin:12px 0}
.tiles{display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:12px;margin:16px 0}
.tile{background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:12px 14px}
.tile .l{color:var(--ink2);font-size:12px}.tile .v{font-size:22px;font-weight:600;margin-top:2px}
.tile .d{color:var(--muted);font-size:12px}
.bars{display:grid;grid-template-columns:minmax(120px,max-content) 1fr 64px;gap:6px 10px;align-items:center}
.bars .lab{white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:320px}
.bars .trk{height:18px;position:relative}
.bars .bar{height:18px;background:var(--s1);border-radius:0 4px 4px 0;min-width:2px}
.bars .ci{position:absolute;top:8px;height:2px;background:var(--ink2);opacity:.45}
.bars .val{text-align:right;font-variant-numeric:tabular-nums;color:var(--ink2)}
table{border-collapse:collapse;width:100%;font-size:13px}
th,td{text-align:left;padding:5px 8px;border-bottom:1px solid var(--grid);vertical-align:top}
th{color:var(--ink2);font-weight:600}td.n,th.n{text-align:right;font-variant-numeric:tabular-nums;white-space:nowrap}
td code,li code{font-size:12px;word-break:break-all}
.up{color:var(--bad)}.down{color:var(--good)}
details{margin:8px 0}summary{cursor:pointer;color:var(--ink2)}
.scroll{overflow-x:auto}
svg text{fill:var(--muted);font-size:11px}
.legend{display:flex;gap:16px;color:var(--ink2);font-size:12px;margin:4px 0}
.legend i{display:inline-block;width:14px;height:2px;vertical-align:middle;margin-right:6px}
#tip{position:fixed;pointer-events:none;background:var(--surface);color:var(--ink);border:1px solid var(--border);
border-radius:6px;padding:6px 8px;font-size:12px;box-shadow:0 2px 8px rgba(0,0,0,.15);display:none;max-width:420px;z-index:9}
.warn{border-left:3px solid var(--bad);padding-left:10px}
@media (max-width:640px){.bars{grid-template-columns:minmax(90px,40%) 1fr 52px}.bars .lab{max-width:40vw}}
"""

JS = """
const tip=document.getElementById('tip');
function showTip(e,h){tip.innerHTML=h;tip.style.display='block';
 const r=tip.getBoundingClientRect();let x=e.clientX+14,y=e.clientY+14;
 if(x+r.width>innerWidth-8)x=e.clientX-r.width-14;if(y+r.height>innerHeight-8)y=e.clientY-r.height-14;
 tip.style.left=x+'px';tip.style.top=y+'px'}
document.addEventListener('mousemove',e=>{
 const t=e.target.closest('[data-tip]');
 if(t){showTip(e,t.dataset.tip);return}
 const s=e.target.closest('svg[data-pts]');
 if(s){const d=JSON.parse(s.dataset.pts),r=s.getBoundingClientRect(),vx=(e.clientX-r.left)/r.width*d.w;
  let best=0,bd=1e9;d.x.forEach((x,i)=>{const dd=Math.abs(x-vx);if(dd<bd){bd=dd;best=i}});
  const cx=d.x[best],ch=s.querySelector('.xh');ch.setAttribute('x1',cx);ch.setAttribute('x2',cx);ch.style.display='';
  s.querySelectorAll('.dot').forEach((c,k)=>{c.setAttribute('cx',cx);c.setAttribute('cy',d.y[k][best]);c.style.display=''});
  showTip(e,d.t[best]);return}
 tip.style.display='none';
 document.querySelectorAll('svg[data-pts] .xh,svg[data-pts] .dot').forEach(n=>n.style.display='none')});
"""


def esc(s):
    return html.escape(str(s), quote=True)


def fnum(v, d=1):
    if v is None:
        return '-'
    return ('{:,.%df}' % d).format(v)


def short(cls):
    """a.b.c.Foo$Bar#m -> Foo$Bar#m, keeping enough to recognise the mod's class."""
    c, _, m = cls.partition('#')
    base = c.rsplit('.', 1)[-1]
    return base + ('#' + m if m else '')


def tile(label, value, detail=''):
    return '<div class="tile"><div class="l">%s</div><div class="v">%s</div><div class="d">%s</div></div>' % (
        esc(label), esc(value), esc(detail))


def hbars(rows, label, value, fmt, tip=None, ci=None, limit=20):
    rows = rows[:limit]
    if not rows:
        return '<p class="note">No samples in this window.</p>'
    top = max(value(r) + (ci(r) if ci else 0) for r in rows) or 1
    out = ['<div class="bars">']
    for r in rows:
        v = value(r)
        w = 100.0 * v / top
        t = esc(tip(r)) if tip else esc('%s: %s' % (label(r), fmt(v)))
        cib = ''
        if ci:
            c = ci(r)
            lo, hi = max(0.0, v - c), v + c
            cib = '<div class="ci" style="left:%.2f%%;width:%.2f%%"></div>' % (100.0 * lo / top, 100.0 * (hi - lo) / top)
        out.append('<div class="lab" title="%s">%s</div><div class="trk" data-tip="%s"><div class="bar" style="width:%.2f%%"></div>%s</div>'
                   '<div class="val">%s</div>' % (esc(label(r)), esc(label(r)), t, w, cib, esc(fmt(v))))
    out.append('</div>')
    return ''.join(out)


def nice_ticks(vmax, n=4):
    if vmax <= 0:
        return [0, 1]
    raw = vmax / n
    mag = 10 ** math.floor(math.log10(raw))
    step = next(s * mag for s in (1, 2, 2.5, 5, 10) if s * mag >= raw)
    return [i * step for i in range(int(math.ceil(vmax / step)) + 1)]


def mmss(s):
    s = int(round(s))
    return '%d:%02d' % (s // 60, s % 60) if s < 3600 else '%d:%02d:%02d' % (s // 3600, s % 3600 // 60, s % 60)


def line_chart(series, unit, ref=None, ymin_top=None, fmt=lambda v: fnum(v, 1)):
    """series: [(name, [[x_seconds, y], ...], css var)] sharing one x axis and one y axis."""
    pts_all = [p for _, pts, _ in series for p in pts]
    if not pts_all:
        return '<p class="note">No data in this window.</p>'
    if len({p[0] for p in pts_all}) < 2:
        return '<p class="note">One reading in this window: %s.</p>' % esc(', '.join(
            '%s %s %s at %s' % (name, fmt(pts[0][1]), unit, mmss(pts[0][0])) for name, pts, _ in series if pts))
    W, H, L, R, T, B = 720, 220, 44, 12, 10, 24
    x0 = min(p[0] for p in pts_all)
    x1 = max(p[0] for p in pts_all) or 1
    if x1 <= x0:
        x1 = x0 + 1
    ymax = max(max(p[1] for p in pts_all) * 1.05, (ref or 0) * 1.1, ymin_top or 0) or 1
    ticks = nice_ticks(ymax)
    ymax = ticks[-1]

    def sx(x):
        return L + (x - x0) / (x1 - x0) * (W - L - R)

    def sy(y):
        return T + (1 - y / ymax) * (H - T - B)

    g = []
    for tv in ticks:
        y = sy(tv)
        g.append('<line x1="%d" x2="%d" y1="%.1f" y2="%.1f" stroke="var(--grid)" stroke-width="1"/>' % (L, W - R, y, y))
        g.append('<text x="%d" y="%.1f" text-anchor="end">%s</text>' % (L - 6, y + 4, esc(fnum(tv, 0 if ymax >= 10 else 1))))
    for i in range(6):
        xv = x0 + (x1 - x0) * i / 5
        g.append('<text x="%.1f" y="%d" text-anchor="middle">%s</text>' % (sx(xv), H - 6, mmss(xv)))
    g.append('<line x1="%d" x2="%d" y1="%.1f" y2="%.1f" stroke="var(--axis)" stroke-width="1"/>' % (L, W - R, sy(0), sy(0)))
    if ref is not None:
        g.append('<line x1="%d" x2="%d" y1="%.1f" y2="%.1f" stroke="var(--ink2)" stroke-width="1" opacity=".6"/>'
                 '<text x="%d" y="%.1f" text-anchor="end">%s</text>' % (L, W - R, sy(ref), sy(ref), W - R, sy(ref) - 4,
                                                                      esc('%s %s budget' % (fnum(ref, 0), unit))))
    # hover data: one shared x list (first series' points), y per series at those x
    base = series[0][1]
    hx = [round(sx(p[0]), 1) for p in base]
    hy, tips = [], []
    lookup = [dict((round(p[0], 1), p[1]) for p in pts) for _, pts, _ in series]
    for _, pts, _ in series:
        d = dict((round(p[0], 1), p[1]) for p in pts)
        hy.append([round(sy(d.get(round(p[0], 1), 0)), 1) for p in base])
    for p in base:
        parts = ['<b>%s</b>' % mmss(p[0])]
        for (name, _, _), d in zip(series, lookup):
            v = d.get(round(p[0], 1))
            parts.append('%s: %s %s' % (esc(name), fmt(v) if v is not None else '-', esc(unit)))
        tips.append('<br>'.join(parts))
    lines = []
    for name, pts, var in series:
        path = ' '.join('%.1f,%.1f' % (sx(x), sy(y)) for x, y in pts)
        lines.append('<polyline points="%s" fill="none" stroke="var(%s)" stroke-width="2" stroke-linejoin="round" '
                     'stroke-linecap="round"/>' % (path, var))
    dots = ''.join('<circle class="dot" r="4" fill="var(%s)" stroke="var(--surface)" stroke-width="2" style="display:none"/>' % var
                   for _, _, var in series)
    data = json.dumps({'w': W, 'x': hx, 'y': hy, 't': tips}, separators=(',', ':'))
    legend = ''
    if len(series) > 1:
        legend = '<div class="legend">%s</div>' % ''.join(
            '<span><i style="background:var(%s)"></i>%s</span>' % (var, esc(name)) for name, _, var in series)
    return ('%s<svg viewBox="0 0 %d %d" width="100%%" role="img" data-pts="%s">%s%s'
            '<line class="xh" y1="%d" y2="%d" stroke="var(--axis)" stroke-width="1" style="display:none"/>%s</svg>'
            % (legend, W, H, esc(data), ''.join(g), ''.join(lines), T, H - B, dots))


def table(headers, rows, numeric=()):
    h = ''.join('<th class="n">%s</th>' % esc(x) if i in numeric else '<th>%s</th>' % esc(x) for i, x in enumerate(headers))
    body = []
    for r in rows:
        body.append('<tr>%s</tr>' % ''.join(
            '<td class="n">%s</td>' % c if i in numeric else '<td>%s</td>' % c for i, c in enumerate(r)))
    return '<div class="scroll"><table><thead><tr>%s</tr></thead><tbody>%s</tbody></table></div>' % (h, ''.join(body))


def delta(cur, base):
    if base is None:
        return '<span class="note">new</span>'
    d = cur - base
    if abs(d) < 0.05:
        return '0.0'
    return '<span class="%s">%+.1f</span>' % ('up' if d > 0 else 'down', d)


def render(d, baseline=None):
    w, s, info = d['window'], d['server'], d['info']
    n = s['samples']
    start = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(w['start_ms'] / 1000))
    out = ['<!doctype html><html lang="en"><head><meta charset="utf-8">'
           '<meta name="viewport" content="width=device-width,initial-scale=1">'
           '<title>Server performance report</title><style>%s</style></head><body><main>' % CSS]
    out.append('<h1>Server performance report</h1>')
    out.append('<p class="meta">%s &middot; window from %s, %s long%s &middot; %d jars from <code>%s</code>%s</p>' % (
        esc(d['recording']), esc(start), mmss(w['seconds']),
        ' (boot excluded)' if w['startup_cut'] else '', d['jar_count'], esc(d['mods_dir']),
        (' &middot; compared with <code>%s</code>' % esc(d.get('baseline_name'))) if baseline else ''))
    bits = [info.get('cpu'), info.get('os'), info.get('jvm'),
            ('heap max %s MB' % fnum(info['heap_max_mb'], 0)) if info.get('heap_max_mb') else None,
            ('RAM %s GB' % fnum(info['ram_gb'], 1)) if info.get('ram_gb') else None,
            'CPU sample every %s ms' % fnum(w['period_ms'], 0)]
    out.append('<p class="note">%s</p>' % esc(' | '.join(b for b in bits if b)))

    t = d['tick']
    g = d['gc']
    srv_share = next((x['pct'] for x in d['groups'] if x['group'] == 'Server thread'), 0)
    busy = [b for _, b in d['busy_series']]
    out.append('<div class="tiles">')
    out.append(tile('Tick time, median', '%s ms' % fnum(t['median'], 1), '100-tick average, sampled each second'))
    out.append(tile('Tick time, p95 / max', '%s / %s ms' % (fnum(t['p95'], 1), fnum(t['max'], 1)),
                    '%d s over the 50 ms budget' % t['over50']))
    out.append(tile('Server thread busy', '%s%%' % fnum(sum(busy) / len(busy) if busy else 0, 0),
                    'estimated from samples; %d s at 50%% or more' % (s['spike']['seconds'] if s['spike'] else 0)))
    out.append(tile('Server-thread samples', fnum(n, 0), '%s%% of all CPU samples' % fnum(srv_share, 0)))
    out.append(tile('GC pauses', '%s ms' % fnum(g['pause_ms'], 0), '%d collections, longest %s ms' % (g['count'], fnum(g['longest_ms'], 0))))
    out.append(tile('Allocation', '%s MB/s' % fnum(d['alloc']['total_mb_s'], 0), 'all threads, sampled'))
    out.append('</div>')
    if n < 400:
        out.append('<p class="note warn">Only %d server-thread samples: each share below is accurate to a few '
                   'percentage points at best (the thin line on each bar is its 95%% range). Record longer, '
                   'or under load, for a sharper picture.</p>' % n)

    # ── timelines ──
    out.append('<h2>Tick time</h2><p class="note">Minecraft\'s own 100-tick rolling average, one point per second. '
               'A short spike is smeared over about five seconds; the busy-share chart below reacts faster.</p>')
    out.append('<div class="card">%s</div>' % line_chart([('tick time', d['mspt_series'], '--s1')], 'ms', ref=50, ymin_top=10))
    out.append('<h3>Server thread busy share, per second (estimate)</h3>'
               '<p class="note">Server-thread CPU samples in each second times the sample period. It reads low '
               'when many threads are busy at once (JFR samples a few threads per period), and time spent waiting '
               '(for a chunk, a lock) does not count as busy.</p>')
    out.append('<div class="card">%s</div>' % line_chart([('busy', d['busy_series'], '--s1')], '%', ymin_top=100,
                                                          fmt=lambda v: fnum(v, 0)))
    if d['cpu_series']:
        out.append('<h3>CPU use</h3><p class="note">Share of the whole machine. The gap between the lines is other '
                   'programs; a machine near 100% starves the server thread even if the game itself is fine.</p>')
        out.append('<div class="card">%s</div>' % line_chart(
            [('this JVM', [[x, a] for x, a, _ in d['cpu_series']], '--s1'),
             ('whole machine', [[x, b] for x, _, b in d['cpu_series']], '--s2')], '%', ymin_top=100, fmt=lambda v: fnum(v, 0)))
    if d['heap_series']:
        out.append('<h3>Heap in use after each GC</h3><p class="note">A floor that keeps rising across a long '
                   'recording suggests something is holding on to memory.</p>')
        out.append('<div class="card">%s</div>' % line_chart([('heap after GC', d['heap_series'], '--s1')], 'MB',
                                                              fmt=lambda v: fnum(v, 0)))

    # ── server thread by mod ──
    base_mods = (baseline or {}).get('server_mods', {})
    out.append('<h2>Server thread: who does the work</h2><p class="note">Share of the server thread\'s CPU samples, '
               'blamed on the innermost mod on the stack (the code actually running; library mods such as Rhino pass '
               'the blame to their caller). <b>minecraft/forge</b> is time with no mod frame on the stack at all. '
               'Thin line: 95% range from sampling.</p>')
    out.append('<div class="card">%s</div>' % hbars(
        s['mods'], lambda r: r['mod'], lambda r: r['pct'], lambda v: '%s%%' % fnum(v, 1),
        tip=lambda r: '%s: %.1f%% +/- %.1f (%d samples)' % (r['mod'], r['pct'], r['ci'], r['samples']),
        ci=lambda r: r['ci']))
    rows = []
    for r in s['mods'][:40]:
        hot = '<br>'.join('<code>%s</code> %s%%' % (esc(h), fnum(p, 0)) for h, p in r.get('hot', []))
        entry = ', '.join('%s %s%%' % (esc(m), fnum(p, 0)) for m, p in r.get('entry', []))
        row = [esc(r['mod']), fnum(r['pct'], 1), '&plusmn;' + fnum(r['ci'], 1), fnum(r['samples'], 0)]
        if baseline:
            row.append(delta(r['pct'], base_mods.get(r['mod'])))
        rows.append(row + [hot, entry or '-'])
    hdr = ['mod', '%', '95%', 'samples'] + (['change (pts)'] if baseline else []) + ['where (its first frame)', 'called from']
    out.append('<details open><summary>Table: top methods per mod</summary>%s</details>' % table(
        hdr, rows, numeric=(1, 2, 3, 4) if baseline else (1, 2, 3)))
    if s['entry']:
        out.append('<details><summary>Entry points: outermost mod on the stack</summary><p class="note">Who started '
                   'the work: the first mod frame from the root. KubeJS scripts appear here as kubejs even when the time '
                   'is spent in vanilla calls they make.</p>%s</details>' % table(
                       ['mod', '%'], [[esc(r['mod']), fnum(r['pct'], 1)] for r in s['entry']], numeric=(1,)))

    # ── phases ──
    base_ph = (baseline or {}).get('server_phases', {})
    out.append('<h2>Server thread by tick phase</h2><p class="note">Which part of the tick loop each sample was in, '
               'from the vanilla frames near the root of the stack. "Tasks between ticks" is work the server runs '
               'while waiting for the next tick (chunk loads and promotions, packets queued to the main thread).</p>')
    out.append('<div class="card">%s</div>' % hbars(
        s['phases'], lambda r: r['phase'], lambda r: r['pct'], lambda v: '%s%%' % fnum(v, 1),
        tip=lambda r: '%s: %.1f%% (%s)' % (r['phase'], r['pct'], ', '.join('%s %.0f%%' % (m, p) for m, p in r['mods'])),
        ci=lambda r: r['ci']))
    rows = [[esc(r['phase']), fnum(r['pct'], 1)] + ([delta(r['pct'], base_ph.get(r['phase']))] if baseline else [])
            + [', '.join('%s %s%%' % (esc(m), fnum(p, 0)) for m, p in r['mods'])] for r in s['phases']]
    out.append('<details><summary>Table: phases and the mods inside them</summary>%s</details>' % table(
        ['phase', '%'] + (['change (pts)'] if baseline else []) + ['top mods within the phase'], rows,
        numeric=(1, 2) if baseline else (1,)))

    # ── handlers, entities, block entities ──
    out.append('<h2>Event handlers</h2><p class="note">Forge event handlers the server thread was inside (the outermost '
               'one when events nest), with the Forge hook that fired them. KubeJS shows up as its dispatcher, e.g. '
               '<code>KubeJSWorldEventHandler#levelPostTick</code> for <code>LevelEvents.tick</code> scripts; which script is '
               'not visible to sampling.</p>')
    if s['handlers']:
        out.append('<div class="card">%s</div>' % hbars(
            s['handlers'], lambda r: short(r['handler']), lambda r: r['pct'], lambda v: '%s%%' % fnum(v, 1),
            tip=lambda r: '%s (%s), fired by %s: %.1f%%' % (r['handler'], r['mod'], short(r['source']), r['pct']), limit=15))
        out.append('<details><summary>Table</summary>%s</details>' % table(
            ['handler', 'mod', 'fired by', '% of server thread'],
            [['<code>%s</code>' % esc(r['handler']), esc(r['mod']), '<code>%s</code>' % esc(short(r['source'])),
              fnum(r['pct'], 1)] for r in s['handlers']], numeric=(3,)))
    else:
        out.append('<p class="note">No samples inside Forge events.</p>')
    for ph, title in (('entity tick', 'Entities'), ('block entity tick', 'Block entities')):
        rows = s['ticked'].get(ph) or []
        out.append('<h2>%s</h2><p class="note">The class whose tick method was running, as seen on the stack: the class '
                   'that <i>declares</i> the method, so mobs without their own override show as a vanilla parent '
                   '(e.g. <code>Mob</code>). For "which one, where", use Observable or spark on the live server.</p>' % title)
        if rows:
            out.append('<div class="card">%s</div>' % hbars(
                rows, lambda r: '%s (%s)' % (short(r['cls']), r['mod']), lambda r: r['pct'], lambda v: '%s%%' % fnum(v, 1),
                tip=lambda r: '%s (%s): %.1f%% of the server thread' % (r['cls'], r['mod'], r['pct']), limit=15))
            out.append('<details><summary>Table</summary>%s</details>' % table(
                ['class', 'mod', '% of server thread'],
                [['<code>%s</code>' % esc(r['cls']), esc(r['mod']), fnum(r['pct'], 2)] for r in rows], numeric=(2,)))
        else:
            out.append('<p class="note">No samples in this phase.</p>')

    # ── busy seconds ──
    out.append('<h2>Busy seconds</h2>')
    sp = s['spike']
    if sp:
        out.append('<p class="note">The %d seconds where the server thread was at least 50%% busy (%d samples), '
                   'against the whole window. A mod whose share jumps here is a lag-spike suspect.</p>' % (sp['seconds'], sp['samples']))
        out.append(table(['mod', '% in busy seconds', '95%', '% overall', 'difference (pts)'],
                         [[esc(r['mod']), fnum(r['pct'], 1), '&plusmn;' + fnum(r['ci'], 1), fnum(r['overall'], 1),
                           delta(r['pct'], r['overall'])] for r in sp['rows']], numeric=(1, 2, 3, 4)))
    else:
        out.append('<p class="note">No second reached 50% busy in this window.</p>')

    # ── blocking ──
    if d['blocked']:
        out.append('<h2>Server thread waiting</h2><p class="note">Blocking events recorded on the server thread '
                   '(parks over 50 ms, lock contention over 20 ms). Waiting in 0.1 ms slices, as the server does when '
                   'it waits for a chunk, is not captured.</p>')
        out.append(table(['kind', 'mod', 'where', 'count', 'total ms'],
                         [[esc(r['type']), esc(r['mod']), '<code>%s</code>' % esc(r['site']), fnum(r['count'], 0),
                           fnum(r['total_ms'], 0)] for r in d['blocked']], numeric=(3, 4)))

    # ── other threads ──
    base_g = (baseline or {}).get('groups', {})
    out.append('<h2>All threads</h2><p class="note">Share of every CPU sample, by thread (numbered pools merged). '
               'Work off the server thread only hurts ticks indirectly: through CPU the server thread can\'t get, and '
               'through garbage that ends in GC pauses. Worldgen runs on <b>Worker-Main</b>.</p>')
    out.append('<div class="card">%s</div>' % hbars(
        d['groups'], lambda r: r['group'], lambda r: r['pct'], lambda v: '%s%%' % fnum(v, 1),
        tip=lambda r: '%s: %.1f%% of all samples (%d)' % (r['group'], r['pct'], r['samples']), limit=15))
    for gr in d['groups'][:8]:
        if gr['group'] == 'Server thread':
            continue
        rows = [[esc(m['mod']), fnum(m['pct'], 1), '<br>'.join('<code>%s</code> %s%%' % (esc(h), fnum(p, 0)) for h, p in m.get('hot', [])[:2])]
                for m in gr['mods']]
        extra = (' &middot; change %s pts' % delta(gr['pct'], base_g.get(gr['group']))) if baseline else ''
        trunc = (' &middot; %d truncated stacks' % gr['truncated']) if gr['truncated'] else ''
        out.append('<details><summary>%s: %s%% of all samples%s%s</summary>%s</details>' % (
            esc(gr['group']), fnum(gr['pct'], 1), extra, trunc, table(['mod', '% of this thread', 'where'], rows, numeric=(1,))))

    # ── memory ──
    a = d['alloc']
    base_a = (baseline or {}).get('alloc_mb_s', {})
    out.append('<h2>Allocation and GC</h2><p class="note">Garbage created per second, blamed like CPU samples '
               '(JFR samples allocations by size). More allocation means more frequent GC pauses, which stop every thread.</p>')
    out.append('<div class="card">%s</div>' % hbars(
        a['mods'], lambda r: r['mod'], lambda r: r['mb_s'], lambda v: '%s MB/s' % fnum(v, 1),
        tip=lambda r: '%s: %.1f MB/s (%.1f%%)' % (r['mod'], r['mb_s'], r['pct']), limit=15))
    if baseline:
        out.append(table(['mod', 'MB/s', 'change (MB/s)'], [[esc(r['mod']), fnum(r['mb_s'], 1), delta(r['mb_s'], base_a.get(r['mod']))]
                                                            for r in a['mods']], numeric=(1, 2)))
    out.append('<details><summary>By thread and by object type</summary>%s%s</details>' % (
        table(['thread', 'MB/s'], [[esc(r['group']), fnum(r['mb_s'], 1)] for r in a['groups']], numeric=(1,)),
        table(['object class', 'MB/s'], [['<code>%s</code>' % esc(r['cls']), fnum(r['mb_s'], 1)] for r in a['classes']], numeric=(1,))))
    if g['rows']:
        out.append('<h3>Garbage collections</h3>' + table(
            ['collector', 'cause', 'count', 'total pause ms', 'longest ms'],
            [[esc(r['name']), esc(r['cause']), fnum(r['count'], 0), fnum(r['pause_ms'], 0), fnum(r['longest_ms'], 0)]
             for r in g['rows']], numeric=(2, 3, 4)))
    if d['vmops']:
        out.append('<details><summary>Stop-the-world JVM operations</summary><p class="note">Every thread, the server '
                   'thread included, stops for these.</p>%s</details>' % table(
                       ['operation', 'count', 'total ms', 'longest ms'],
                       [[esc(r['op']), fnum(r['count'], 0), fnum(r['total_ms'], 0), fnum(r['longest_ms'], 1)] for r in d['vmops']],
                       numeric=(1, 2, 3)))

    # ── chunk generation ──
    if d['chunkgen']:
        out.append('<h2>Chunk generation</h2><p class="note">Minecraft\'s own per-stage timing (minecraft.ChunkGeneration). '
                   'For which mods cost worldgen time, open <b>Worker-Main</b> under All threads.</p>')
        out.append(table(['stage', 'chunks', 'total s', 'mean ms'],
                         [[esc(r['status']), fnum(r['count'], 0), fnum(r['total_s'], 1), fnum(r['mean_ms'], 2)] for r in d['chunkgen']],
                         numeric=(1, 2, 3)))

    # ── hot methods ──
    out.append('<h2>Hottest methods on the server thread</h2><p class="note">The method at the top of the stack '
               '(self time). Useful to spot one expensive call, e.g. a lookup done every tick.</p>')
    out.append(table(['method', 'mod', '%'], [['<code>%s</code>' % esc(r['method']), esc(r['mod']), fnum(r['pct'], 1)]
                                              for r in s['leaf']], numeric=(2,)))

    # ── method ──
    unres = ''.join('<li><code>%s</code> (%d)</li>' % (esc(k), v) for k, v in d['unresolved_mixins'])
    out.append('''<h2>Method and limits</h2><div class="note"><ul>
<li><b>Sampling.</b> JFR records the stack of running threads every %s ms. A share is a count of samples, so it is
exact only on average: the 95%% range is shown where it matters. Time a thread spends waiting (parked, sleeping, blocked
on a chunk) is not sampled.</li>
<li><b>Blame.</b> From the running method outward, the first frame owned by a mod gets the sample. A class belongs to a
mod when it is in that mod's jar (<code>%s</code>). Mixin code merged into vanilla or another mod
(<code>handler$...</code>, <code>redirect$...</code>) is traced to the mod that injected it; a name shared by two mods'
mixins lists both. Library mods pass the blame to their caller.</li>
<li><b>KubeJS</b> runs interpreted, so time is visible per event (the handler table) but not per script file.</li>
<li><b>Not visible:</b> which dimension a sample belongs to, which individual entity/block (positions), and the client.
Observable or spark answer "which one, where" on the live server.</li>
<li><b>Tick time</b> is Minecraft's 100-tick average, so it trails and smooths spikes.</li>
</ul>%s</div>''' % (fnum(w['period_ms'], 0), esc(d['mods_dir']),
                      ('<details><summary>Mixin frames that could not be traced to a mod</summary><ul>%s</ul></details>' % unres)
                      if unres else ''))
    out.append('<p class="note">Generated %s by tools/perf/perf_report.py.</p>' % esc(d['generated']))
    out.append('</main><div id="tip"></div><script>%s</script></body></html>' % JS)
    return ''.join(out)
