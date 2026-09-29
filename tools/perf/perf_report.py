#!/usr/bin/env python3
"""
Turns a JFR recording from the server (or client) into a per-mod performance report.

  python3 tools/perf/perf_report.py <recording.jfr | JFR repository dir> [options]

Writes perf/reports/<name>.html (open it in a browser) and perf/reports/<name>.summary.json (feed it to a
later run's --baseline to see what changed). Parsed data is cached in perf/parsed/<name>/.

Recordings come from the always-on recording in server/LaunchServer.sh (see server/perf/README.md):
dump with server/perf/dump.sh, copy the .jfr into perf/recordings/ here, run this.

Options:
  --mods DIR          jars to attribute classes to (default deploy/latest/mods, the server set;
                      else mods/). Use the jars the recorded server actually ran.
  --include-startup   keep the boot phase (default: the window starts when the world finished loading)
  --skip S / --last S drop the first S seconds of the window / keep only the last S seconds
  --baseline FILE     an earlier .summary.json: adds "change" columns
  --out FILE          report path (default perf/reports/<recording name>.html)
  --tsrg FILE         SRG->official method names (default patcherMod/build/createMcpToSrg/output.tsrg;
                      without it method names stay m_12345_, tick phases still work)

How samples are attributed (details in the report's "Method and limits" section):
  - every CPU sample is a stack; walking from the running method outward, the first frame that belongs
    to a mod (its class is in a mod jar, or it is a mixin method merged into another class) gets the
    blame ("who does the work"). Library mods (Rhino, Architectury, ...) pass it on to their caller.
    The outermost mod frame is the entry point ("who started it").
  - Server-thread samples are also placed in a tick phase (entity tick, block entities, chunk system, ...)
    by the vanilla frames near the root, and in the Forge event handler they ran under, if any.
"""
import argparse
import collections
import json
import math
import os
import re
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
import modindex  # noqa: E402
import report_html  # noqa: E402

MIXIN_RE = re.compile(r'^(handler|redirect|modify|localvar|constant|args|wrapOperation|wrapWithCondition|'
                      r'modifyExpressionValue|modifyReturnValue|modifyReceiver|modifyVariable)\$[a-z]{3}\d{3}\$(.+?)(\$\d+)?$')
HIDDEN_RE = re.compile(r'\$\$Lambda\$\d+.*$|/0x[0-9a-f]+$|\+0x[0-9a-f]+.*$')
SRG_RE = re.compile(r'm_\d+_')

# Mods whose time rolls up to whoever called them (script engines, shared libraries).
LIBRARY_MODS = {'rhino', 'architectury', 'kotlinforforge', 'cloth_config', 'balm', 'puzzleslib', 'moonlight',
                'bookshelf', 'citadel', 'creativecore', 'placebo', 'lodestone', 'geckolib3', 'geckolib',
                'terrablender', 'resourcefullib', 'framework', 'collective', 'kambrik', 'mixinextras'}
NOT_A_MOD = 'minecraft/forge'


def _m(cls, name, *srg):
    return [cls + '#' + name] + [cls + '#' + s for s in srg]


MC = 'net.minecraft.'
# Tick phases, checked from the root of the stack inward; the first marker found names the phase.
# Both the official and the SRG name are listed so this works without the tsrg file.
PHASES = [
    ('autosave', _m(MC + 'server.MinecraftServer', 'saveEverything', 'm_195514_')
     + _m(MC + 'server.MinecraftServer', 'saveAllChunks', 'm_129885_')),
    ('entity tick', _m(MC + 'server.level.ServerLevel', 'tickNonPassenger', 'm_8647_')
     + _m(MC + 'server.level.ServerLevel', 'tickPassenger', 'm_8662_')),
    ('block entity tick', _m(MC + 'world.level.chunk.LevelChunk$BoundTickingBlockEntity', 'tick', 'm_142224_')),
    ('mob spawning', _m(MC + 'world.level.NaturalSpawner', 'spawnForChunk', 'm_47029_')
     + _m(MC + 'world.level.NaturalSpawner', 'createState', 'm_186524_')
     + _m(MC + 'server.level.ServerLevel', 'tickCustomSpawners', 'm_8799_')),
    ('random ticks, weather', _m(MC + 'server.level.ServerLevel', 'tickChunk', 'm_8714_')),
    ('scheduled block/fluid ticks', _m(MC + 'world.ticks.LevelTicks', 'tick', 'm_193225_')),
    ('block events', _m(MC + 'server.level.ServerLevel', 'runBlockEvents', 'm_8807_')),
    ('raids', _m(MC + 'world.entity.raid.Raids', 'tick', 'm_37957_')),
    ('player packets', _m(MC + 'network.protocol.PacketUtils', 'lambda$ensureRunningOnSameThread$0', 'lambda$m_131359_$0',
                          'm_131356_') + _m(MC + 'server.network.ServerConnectionListener', 'tick', 'm_9721_')),
    ('chunk system', _m(MC + 'server.level.ChunkMap', 'tick', 'm_140280_', 'm_140421_')
     + _m(MC + 'server.level.ServerChunkCache', 'runDistanceManagerUpdates', 'm_8489_')
     + _m(MC + 'server.level.ChunkMap', 'processUnloads', 'm_140353_')
     + _m(MC + 'server.level.ServerChunkCache$MainThreadExecutor', 'pollTask', 'm_7245_')
     + _m(MC + 'server.level.ChunkHolder', 'broadcastChanges', 'm_140054_')
     + _m(MC + 'server.level.ServerChunkCache', 'tickChunks', 'm_8490_')),
    ('Forge tick events', _m('net.minecraftforge.event.ForgeEventFactory', 'onPreServerTick')
     + _m('net.minecraftforge.event.ForgeEventFactory', 'onPostServerTick')
     + _m('net.minecraftforge.event.ForgeEventFactory', 'onPreLevelTick')
     + _m('net.minecraftforge.event.ForgeEventFactory', 'onPostLevelTick')),
    ('players (tick, list)', _m(MC + 'server.players.PlayerList', 'tick', 'm_11288_')),
    ('commands, functions', _m(MC + 'server.ServerFunctionManager', 'tick', 'm_136128_')),
    ('tasks between ticks', _m(MC + 'server.MinecraftServer', 'waitUntilNextTick', 'm_130012_')
     + _m(MC + 'server.MinecraftServer', 'pollTask', 'm_7245_')),
    ('level tick, other', _m(MC + 'server.level.ServerLevel', 'tick', 'm_8793_')),
    ('server tick, other', _m(MC + 'server.MinecraftServer', 'tickServer', 'm_5705_')),
]
PHASE_OF = {}
PHASE_RANK = {name: i for i, (name, _) in enumerate(PHASES)}
for _name, _markers in PHASES:
    for _mk in _markers:
        PHASE_OF.setdefault(_mk, _name)
TICKED_MARKERS = {
    'entity tick': set(PHASES[1][1]),
    'block entity tick': set(PHASES[2][1]),
}
STOP_FRAMES = set(_m(MC + 'server.MinecraftServer', 'stopServer', 'm_7041_')
                  + _m(MC + 'server.dedicated.DedicatedServer', 'stopServer', 'm_7041_')
                  + _m('net.minecraftforge.server.ServerLifecycleHooks', 'handleServerStopping'))
EVENTBUS_POST = 'net.minecraftforge.eventbus.EventBus#post'


def thread_group(name):
    if name == 'Server thread':
        return name
    prev = None
    while prev != name:
        prev = name
        name = re.sub(r'[\s#\-]*(\[\d+\]|\d+)$', '', name)
    return name or prev


class Attributor:
    def __init__(self, idx, srg):
        self.classes = idx['classes']
        self.mixins = idx['mixins']
        self.overwrites = idx['overwrites']
        self.srg = srg
        self.owner_cache = {}
        self.pretty_cache = {}
        self.unresolved_mixins = collections.Counter()

    def pretty(self, frame):
        p = self.pretty_cache.get(frame)
        if p is None:
            cls, _, m = frame.partition('#')
            cls = HIDDEN_RE.sub('', cls)
            if self.srg:
                m = SRG_RE.sub(lambda x: self.srg.get(x.group(0), x.group(0)), m)
            p = self.pretty_cache[frame] = cls + '#' + m
        return p

    def owner(self, frame):
        """(owner, kind) of a 'class#method' frame; kind is mod | mixin | vanilla | forge | jdk | lib."""
        o = self.owner_cache.get(frame)
        if o is not None:
            return o
        cls, _, meth = frame.partition('#')
        cls = HIDDEN_RE.sub('', cls)
        mm = MIXIN_RE.match(meth)
        if mm:
            hits = self.mixins.get(cls + '#' + mm.group(2))
            if hits:
                o = ('+'.join(sorted({h[0] for h in hits})), 'mixin')
            else:
                self.unresolved_mixins[cls + '#' + meth] += 1
                o = ('unresolved mixin', 'mixin')
        elif cls + '#' + meth in self.overwrites:
            o = (self.overwrites[cls + '#' + meth][0][0], 'mixin')
        elif cls in self.classes:
            o = (self.classes[cls], 'mod')
        elif cls.split('$')[0] in self.classes:
            o = (self.classes[cls.split('$')[0]], 'mod')
        elif cls.startswith(('net.minecraft.', 'com.mojang.')):
            o = ('minecraft', 'vanilla')
        elif cls.startswith(('net.minecraftforge.', 'cpw.mods.')):
            o = ('forge', 'forge')
        elif cls.startswith(('java.', 'jdk.', 'sun.', 'javax.')):
            o = ('jdk', 'jdk')
        else:
            o = (cls.rsplit('.', 1)[0] if '.' in cls else cls, 'lib')
        self.owner_cache[frame] = o
        return o

    def blame(self, frames):
        """frames are leaf-first. (worker, entry, index of the worker's first frame)."""
        inner = entry = None
        at = 0
        for i, f in enumerate(frames):
            oid, kind = self.owner(f)
            if kind in ('mod', 'mixin'):
                if inner is None and oid not in LIBRARY_MODS:
                    inner, at = oid, i
                if oid not in LIBRARY_MODS or entry is None:
                    entry = oid
        return inner or NOT_A_MOD, entry or NOT_A_MOD, at

    def phase(self, frames):
        """The most specific phase marker on the stack wins (PHASES order), so an entity tick inside
        the level tick inside the server tick counts as 'entity tick'."""
        best = None
        for f in frames:
            ph = PHASE_OF.get(f) or PHASE_OF.get(self.pretty(f))
            if ph and (best is None or PHASE_RANK[ph] < PHASE_RANK[best]):
                best = ph
        return best or 'other'

    def ticked(self, frames, markers):
        """The class whose tick the phase marker called: the entity or block entity being ticked."""
        for i in range(len(frames) - 1, 0, -1):
            if frames[i] in markers or self.pretty(frames[i]) in markers:
                for j in range(i - 1, max(i - 5, -1), -1):
                    cls, _, meth = self.pretty(frames[j]).partition('#')
                    if cls.startswith(('java.', 'net.minecraft.world.level.chunk.LevelChunk')) or 'Ticker' in cls \
                            or meth.startswith('lambda$') and j > i - 4:
                        continue
                    return cls
                return None
        return None

    def handler(self, frames):
        """(event source, handler frame) for the outermost Forge event this sample ran under."""
        for i in range(len(frames) - 1, -1, -1):
            if frames[i] == EVENTBUS_POST:
                source = self.pretty(frames[i + 1]) if i + 1 < len(frames) else '?'
                for j in range(i - 1, -1, -1):
                    # skip the bus, generated dispatchers (__Owner_method_Event), lambdas and library
                    # forwarders (Architectury re-fires Forge events to KubeJS through its own bus)
                    cls = frames[j].split('#')[0]
                    if cls.startswith(('net.minecraftforge.eventbus', 'java.')) or '$$Lambda' in cls \
                            or 'ASMEventHandler' in cls or cls.rsplit('.', 1)[-1].startswith('__') \
                            or self.owner(frames[j])[0] in LIBRARY_MODS:
                        continue
                    return source, self.pretty(frames[j])
                return source, '(handler not identifiable)'
        return None


def ci95(k, n):
    if not n:
        return 0.0
    p = k / n
    return 196.0 * math.sqrt(p * (1 - p) / n)


def parse_ms(s, default):
    m = re.match(r'\s*([\d.]+)\s*(ms|s|us|ns)?', s or '')
    if not m:
        return default
    v = float(m.group(1))
    return v * {'s': 1000, 'ms': 1, 'us': 1e-3, 'ns': 1e-6}.get(m.group(2) or 'ms', 1)


def ensure_parsed(src, parsed):
    """Runs PerfParse.java unless the cache is newer than every input."""
    inputs = []
    if os.path.isdir(src):
        for dp, _, fs in os.walk(src):
            inputs += [os.path.join(dp, f) for f in fs if f.endswith('.jfr')]
    else:
        inputs = [src]
    if not inputs:
        sys.exit('no .jfr files in ' + src)
    marker = os.path.join(parsed, 'stacks.tsv')
    if os.path.exists(marker) and os.path.getmtime(marker) > max(os.path.getmtime(p) for p in inputs):
        return
    t = time.time()
    r = subprocess.run(['java', os.path.join(HERE, 'PerfParse.java'), parsed, src], capture_output=True, text=True)
    sys.stdout.write(r.stdout)
    if r.returncode:
        sys.stderr.write(r.stderr)
        sys.exit('PerfParse failed')
    print('parsed in %.1f s' % (time.time() - t))


def tsv(path):
    with open(path, encoding='utf8') as fh:
        for line in fh:
            yield line.rstrip('\n').split('\t')


def analyze(parsed, idx, srg, args):
    at = Attributor(idx, srg)
    stacks = {}
    for i, tr, fr in tsv(os.path.join(parsed, 'stacks.tsv')):
        stacks[int(i)] = (fr.split(';') if fr else [], tr == '1')
    events = []
    with open(os.path.join(parsed, 'events.jsonl'), encoding='utf8') as fh:
        for line in fh:
            events.append(json.loads(line))

    # ── window ──
    t_first = t_last = None
    for t, *_ in tsv(os.path.join(parsed, 'samples.tsv')):
        t = int(t)
        t_first = t if t_first is None else min(t_first, t)
        t_last = t if t_last is None else max(t_last, t)
    if t_first is None:
        sys.exit('recording has no CPU samples (was jdk.ExecutionSample enabled?)')
    lo, hi = t_first, t_last
    # Shutdown (stopServer: saving, mods' ServerStopping handlers) is not part of a running server.
    if not args.include_startup:
        stop_ids = {i for i, (fr, _) in stacks.items() if any(f in STOP_FRAMES for f in fr)}
        stops = [int(t) for t, th, sid in tsv(os.path.join(parsed, 'samples.tsv'))
                 if th == 'Server thread' and int(sid) in stop_ids]
        if stops:
            hi = min(stops) - 1
    # Boot ends at the first tick: the first non-zero 100-tick average. The world-loaded event alone is too
    # early - ServerStartedEvent handlers (WorldEdit builds its block registry there) run after it.
    startup_cut = None
    if not args.include_startup:
        loads = [e['t'] + e['dur'] for e in events if e['type'] == 'minecraft.LoadWorld']
        ticks = sorted(e['t'] for e in events if e['type'] == 'minecraft.ServerTickTime'
                       and (e.get('averageTickDuration') or 0) > 0 and (not loads or e['t'] >= max(loads)))
        if loads and ticks:
            startup_cut = ticks[0]
            lo = max(lo, int(startup_cut))
    if args.skip:
        lo += int(args.skip * 1000)
    if args.last:
        lo = max(lo, hi - int(args.last * 1000))
    if lo >= hi:
        sys.exit('empty window')
    secs = (hi - lo) / 1000.0
    periods = [parse_ms(e.get('value'), None) for e in events if e['type'] == 'samplePeriod']
    periods = [p for p in periods if p]
    period = min(periods) if periods else 20.0

    # ── CPU samples ──
    groups = collections.Counter()
    g_worker = collections.defaultdict(collections.Counter)
    g_hot = collections.defaultdict(collections.Counter)
    s_entry = collections.Counter()
    s_entry_of = collections.defaultdict(collections.Counter)
    phase = collections.Counter()
    phase_mod = collections.defaultdict(collections.Counter)
    ticked = {k: collections.Counter() for k in TICKED_MARKERS}
    handlers = collections.Counter()
    handler_src = {}
    leaf = collections.Counter()
    per_sec = collections.Counter()
    per_sec_mod = collections.defaultdict(collections.Counter)
    truncated = collections.Counter()
    for t, th, sid in tsv(os.path.join(parsed, 'samples.tsv')):
        t = int(t)
        if not lo <= t <= hi:
            continue
        g = thread_group(th)
        groups[g] += 1
        frames, tr = stacks.get(int(sid), ([], False))
        if tr:
            truncated[g] += 1
        w, e, i = at.blame(frames)
        g_worker[g][w] += 1
        if frames:
            g_hot[(g, w)][at.pretty(frames[i])] += 1
        if g != 'Server thread':
            continue
        s_entry[e] += 1
        s_entry_of[w][e] += 1
        ph = at.phase(frames)
        phase[ph] += 1
        phase_mod[ph][w] += 1
        if ph in ticked:
            c = at.ticked(frames, TICKED_MARKERS[ph])
            if c:
                ticked[ph][c] += 1
        h = at.handler(frames)
        if h:
            handlers[h[1]] += 1
            handler_src.setdefault(h[1], collections.Counter())[h[0]] += 1
        if frames:
            leaf[at.pretty(frames[0])] += 1
        sec = (t - lo) // 1000
        per_sec[sec] += 1
        per_sec_mod[sec][w] += 1

    n_srv = groups['Server thread']
    total = sum(groups.values())

    def mod_rows(counter, n, hot_key=None, limit=40):
        rows = []
        for m, k in counter.most_common(limit):
            r = {'mod': m, 'samples': k, 'pct': 100.0 * k / n if n else 0, 'ci': ci95(k, n)}
            if hot_key is not None:
                hot = g_hot[(hot_key, m)].most_common(3)
                r['hot'] = [[h, 100.0 * c / k] for h, c in hot]
            rows.append(r)
        return rows

    server = mod_rows(g_worker['Server thread'], n_srv, 'Server thread')
    for r in server:
        ents = s_entry_of[r['mod']].most_common(3)
        r['entry'] = [[m, 100.0 * c / r['samples']] for m, c in ents if m != r['mod']][:2]

    # Busy seconds: server-thread samples per second x sample period = the share of that second spent
    # running (an estimate: JFR samples a handful of threads per period, so it reads low when many
    # threads are busy). Seconds at >= 50% busy are compared with the whole window.
    nsec = int(secs) + 1
    busy_series = [[s, min(100.0, per_sec.get(s, 0) * period / 10.0)] for s in range(nsec)]
    hot_secs = [s for s, b in busy_series if b >= 50.0]
    spike = None
    if hot_secs:
        c = collections.Counter()
        for s in hot_secs:
            c.update(per_sec_mod[s])
        k = sum(c.values())
        base = {r['mod']: r['pct'] for r in server}
        spike = {'seconds': len(hot_secs), 'samples': k,
                 'rows': [{'mod': m, 'pct': 100.0 * v / k, 'ci': ci95(v, k),
                           'overall': base.get(m, 100.0 * g_worker['Server thread'][m] / n_srv if n_srv else 0)}
                          for m, v in c.most_common(15)]}

    # ── allocation ──
    alloc_mod = collections.Counter()
    alloc_group = collections.Counter()
    alloc_cls = collections.Counter()
    alloc_total = 0
    for t, th, sid, wgt, ocls in tsv(os.path.join(parsed, 'alloc.tsv')):
        if not lo <= int(t) <= hi:
            continue
        wgt = int(wgt)
        frames, _ = stacks.get(int(sid), ([], False))
        m = at.blame(frames)[0]
        alloc_mod[m] += wgt
        alloc_group[thread_group(th)] += wgt
        alloc_cls[ocls] += wgt
        alloc_total += wgt

    # ── blocking on the server thread ──
    blocked = collections.defaultdict(lambda: [0, 0.0])
    for t, th, typ, dur, sid in tsv(os.path.join(parsed, 'blocked.tsv')):
        if not lo <= int(t) <= hi:
            continue
        frames, _ = stacks.get(int(sid), ([], False))
        w = at.blame(frames)[0]
        site = next((at.pretty(f) for f in frames if not f.startswith(('java.', 'jdk.', 'sun.'))), '?')
        b = blocked[(typ.replace('jdk.', ''), w, site)]
        b[0] += 1
        b[1] += float(dur)

    # ── events ──
    ev = [e for e in events if lo <= e['t'] <= hi]
    mspt = [[(e['t'] - lo) / 1000.0, e.get('averageTickDuration') or 0.0] for e in ev if e['type'] == 'minecraft.ServerTickTime']
    cpu = [[(e['t'] - lo) / 1000.0, 100.0 * ((e.get('jvmUser') or 0) + (e.get('jvmSystem') or 0)), 100.0 * (e.get('machineTotal') or 0)]
           for e in ev if e['type'] == 'jdk.CPULoad']
    heap = [[(e['t'] - lo) / 1000.0, (e.get('heapUsed') or 0) / 2 ** 20] for e in ev
            if e['type'] == 'jdk.GCHeapSummary' and e.get('when') == 'After GC']
    gcs = [e for e in ev if e['type'] == 'jdk.GarbageCollection']
    gc_by = collections.defaultdict(lambda: [0, 0.0, 0.0])
    for e in gcs:
        k = (e.get('name') or '?', e.get('cause') or '?')
        gc_by[k][0] += 1
        gc_by[k][1] += e.get('sumOfPauses') or 0
        gc_by[k][2] = max(gc_by[k][2], e.get('longestPause') or 0)
    chunk = collections.defaultdict(lambda: [0, 0.0])
    for e in ev:
        if e['type'] == 'minecraft.ChunkGeneration':
            c = chunk[e.get('status') or '?']
            c[0] += 1
            c[1] += e['dur']
    vmops = collections.defaultdict(lambda: [0, 0.0, 0.0])
    for e in ev:
        if e['type'] == 'jdk.ExecuteVMOperation' and e.get('safepoint'):
            v = vmops[e.get('operation') or '?']
            v[0] += 1
            v[1] += e['dur']
            v[2] = max(v[2], e['dur'])
    info = {}
    for e in events:
        if e['type'] == 'jdk.JVMInformation':
            info['jvm'] = e.get('jvmVersion', '').split(' for ')[0]
            info['args'] = e.get('jvmArguments', '')
        elif e['type'] == 'jdk.CPUInformation':
            m = re.search(r'Brand: ([^,\n]+)', e.get('description', ''))
            info['cpu'] = (m.group(1).strip() if m else e.get('cpu', '')) + ' (%s hw threads)' % e.get('hwThreads', '?')
        elif e['type'] == 'jdk.GCHeapConfiguration':
            info['heap_max_mb'] = (e.get('maxSize') or 0) / 2 ** 20
        elif e['type'] == 'jdk.PhysicalMemory':
            info['ram_gb'] = (e.get('totalSize') or 0) / 2 ** 30
        elif e['type'] == 'jdk.OSInformation':
            osv = e.get('osVersion') or ''
            m = re.search(r'(?:PRETTY_NAME|DISTRIB_DESCRIPTION)="?([^"\n]+)', osv)
            info['os'] = (m.group(1) if m else osv.split('\n')[0])[:80]

    def pctl(vals, q):
        s = sorted(vals)
        return s[min(len(s) - 1, int(q * len(s)))] if s else None

    mv = [v for _, v in mspt]
    return {
        'recording': os.path.basename(os.path.normpath(args.recording)),
        'generated': time.strftime('%Y-%m-%d %H:%M'),
        'window': {'start_ms': lo, 'end_ms': hi, 'seconds': secs, 'startup_cut': bool(startup_cut and lo >= startup_cut),
                   'period_ms': period},
        'info': info,
        'mods_dir': os.path.relpath(args.mods, ROOT) if args.mods.startswith(ROOT) else args.mods,
        'jar_count': len(idx['key']),
        'tick': {'median': pctl(mv, .5), 'p95': pctl(mv, .95), 'max': max(mv) if mv else None,
                 'over50': sum(1 for v in mv if v > 50), 'samples': len(mv)},
        'mspt_series': mspt, 'cpu_series': cpu, 'heap_series': heap, 'busy_series': busy_series,
        'groups': [{'group': g, 'samples': k, 'pct': 100.0 * k / total, 'truncated': truncated[g],
                    'mods': mod_rows(g_worker[g], k, g, 8)} for g, k in groups.most_common(25)],
        'server': {'samples': n_srv, 'mods': server,
                   'entry': [{'mod': m, 'pct': 100.0 * k / n_srv} for m, k in s_entry.most_common(15)] if n_srv else [],
                   'phases': [{'phase': p, 'samples': k, 'pct': 100.0 * k / n_srv, 'ci': ci95(k, n_srv),
                               'mods': [[m, 100.0 * c / k] for m, c in phase_mod[p].most_common(4)]}
                              for p, k in phase.most_common()],
                   'ticked': {ph: [{'cls': c, 'mod': at.owner(c + '#tick')[0], 'samples': k,
                                    'pct': 100.0 * k / n_srv} for c, k in cnt.most_common(25)]
                              for ph, cnt in ticked.items()},
                   'handlers': [{'handler': h, 'mod': at.owner(h)[0], 'samples': k, 'pct': 100.0 * k / n_srv,
                                 'source': handler_src[h].most_common(1)[0][0]} for h, k in handlers.most_common(25)],
                   'leaf': [{'method': h, 'mod': at.owner(h)[0], 'pct': 100.0 * k / n_srv} for h, k in leaf.most_common(25)],
                   'spike': spike},
        'alloc': {'total_mb_s': alloc_total / secs / 2 ** 20,
                  'mods': [{'mod': m, 'mb_s': w / secs / 2 ** 20, 'pct': 100.0 * w / alloc_total}
                           for m, w in alloc_mod.most_common(20)] if alloc_total else [],
                  'groups': [{'group': g, 'mb_s': w / secs / 2 ** 20} for g, w in alloc_group.most_common(10)],
                  'classes': [{'cls': c, 'mb_s': w / secs / 2 ** 20} for c, w in alloc_cls.most_common(10)]},
        'gc': {'count': len(gcs), 'pause_ms': sum(e.get('sumOfPauses') or 0 for e in gcs),
               'longest_ms': max((e.get('longestPause') or 0 for e in gcs), default=0),
               'rows': [{'name': k[0], 'cause': k[1], 'count': v[0], 'pause_ms': v[1], 'longest_ms': v[2]}
                        for k, v in sorted(gc_by.items(), key=lambda kv: -kv[1][1])]},
        'vmops': [{'op': k, 'count': v[0], 'total_ms': v[1], 'longest_ms': v[2]}
                  for k, v in sorted(vmops.items(), key=lambda kv: -kv[1][1])[:10]],
        'chunkgen': [{'status': s, 'count': v[0], 'total_s': v[1] / 1000, 'mean_ms': v[1] / v[0]}
                     for s, v in sorted(chunk.items(), key=lambda kv: -kv[1][1])],
        'blocked': [{'type': k[0], 'mod': k[1], 'site': k[2], 'count': v[0], 'total_ms': v[1]}
                    for k, v in sorted(blocked.items(), key=lambda kv: -kv[1][1])[:15]],
        'unresolved_mixins': at.unresolved_mixins.most_common(10),
    }


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n\n')[1], formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('recording')
    ap.add_argument('--mods')
    ap.add_argument('--include-startup', action='store_true')
    ap.add_argument('--skip', type=float, default=0)
    ap.add_argument('--last', type=float, default=0)
    ap.add_argument('--baseline')
    ap.add_argument('--out')
    ap.add_argument('--tsrg', default=os.path.join(ROOT, 'patcherMod', 'build', 'createMcpToSrg', 'output.tsrg'))
    args = ap.parse_args()
    if not args.mods:
        dl = os.path.join(ROOT, 'deploy', 'latest', 'mods')
        args.mods = dl if os.path.isdir(dl) else os.path.join(ROOT, 'mods')
    args.mods = os.path.abspath(args.mods)

    name = re.sub(r'\.jfr$', '', os.path.basename(os.path.normpath(args.recording)))
    parsed = os.path.join(ROOT, 'perf', 'parsed', name)
    ensure_parsed(args.recording, parsed)
    idx = modindex.build(args.mods, os.path.join(ROOT, 'perf', '.cache', 'modindex.json'))
    srg = modindex.load_srg(args.tsrg)
    if not srg:
        print('note: no tsrg at %s - method names stay SRG (m_12345_)' % args.tsrg)
    data = analyze(parsed, idx, srg, args)
    baseline = None
    if args.baseline:
        with open(args.baseline, encoding='utf8') as fh:
            baseline = json.load(fh)
        data['baseline_name'] = baseline.get('recording')

    out = args.out or os.path.join(ROOT, 'perf', 'reports', name + '.html')
    os.makedirs(os.path.dirname(os.path.abspath(out)), exist_ok=True)
    with open(out, 'w', encoding='utf8') as fh:
        fh.write(report_html.render(data, baseline))
    summary = re.sub(r'\.html$', '', out) + '.summary.json'
    slim = {k: data[k] for k in ('recording', 'generated', 'window', 'tick', 'mods_dir')}
    slim['server_mods'] = {r['mod']: r['pct'] for r in data['server']['mods']}
    slim['server_phases'] = {r['phase']: r['pct'] for r in data['server']['phases']}
    slim['alloc_mb_s'] = {r['mod']: r['mb_s'] for r in data['alloc']['mods']}
    slim['groups'] = {r['group']: r['pct'] for r in data['groups']}
    with open(summary, 'w', encoding='utf8') as fh:
        json.dump(slim, fh, indent=1)
    s = data['server']
    print('window %.0f s, %d server-thread samples; top: %s' % (
        data['window']['seconds'], s['samples'],
        ', '.join('%s %.1f%%' % (r['mod'], r['pct']) for r in s['mods'][:5])))
    print('report: ' + os.path.relpath(out, os.getcwd()))


if __name__ == '__main__':
    main()
