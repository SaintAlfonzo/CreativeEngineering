"""Audit spawner blocks, structure spawn overrides and spawner-placing code across mods/ jars (+ jar-in-jar) and
kubejs/data (kubejs/data wins on the same path). Structures are mapped to templates by walking their jigsaw pools.
Writes JSON (structures, templates_with_spawners, procs_with_spawner, code_refs); see
feature_writeup/briefs/structures_contents.md #3.
Usage: python3 tools/audit_structure_spawners.py [out.json]   (default: structure_spawners.json in the cwd)"""
import gzip, io, json, os, re, sys, zipfile
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = sys.argv[1] if len(sys.argv) > 1 else 'structure_spawners.json'
from port_structure_jars import nbt_read  # noqa: E402

ILLAGERS = {'minecraft:pillager', 'minecraft:vindicator', 'minecraft:evoker', 'minecraft:illusioner',
            'minecraft:ravager', 'minecraft:witch'}
TPL_RE = re.compile(r'^data/([^/]+)/structures?/(.+)\.nbt$')
STRUCT_RE = re.compile(r'^data/([^/]+)/worldgen/structure/(.+)\.json$')
POOL_RE = re.compile(r'^data/([^/]+)/worldgen/template_pool/(.+)\.json$')
PROC_RE = re.compile(r'^data/([^/]+)/worldgen/processor_list/(.+)\.json$')


def py(t):
    if t.t == 10: return {k: py(v) for k, v in t.v.items()}
    if t.t == 9: return [py(x) for x in t.v]
    if t.t == 7: return None
    return t.v


def ent_ids(nbt):
    ids = []
    e = (nbt.get('SpawnData') or {}).get('entity') or {}
    if 'id' in e: ids.append(e['id'])
    elif 'SpawnData' in nbt and 'id' in nbt['SpawnData']: ids.append(nbt['SpawnData']['id'])  # pre-1.18 shape
    for p in nbt.get('SpawnPotentials') or []:
        d = (p.get('data') or {}).get('entity') or p.get('Entity') or {}
        if 'id' in d and d['id'] not in ids: ids.append(d['id'])
    return ids or ['(empty)']


def scan_template(buf):
    try:
        raw = gzip.decompress(buf)
    except OSError:
        raw = buf
    _, root = nbt_read(raw)
    r = py(root)
    pals = r.get('palettes') or ([r['palette']] if 'palette' in r else [])
    pal = pals[0] if pals else []
    spawners, pools = [], set()
    for b in r.get('blocks', []):
        name = pal[b['state']]['Name'] if b['state'] < len(pal) else '?'
        nbt = b.get('nbt') or {}
        if 'spawner' in name:
            spawners.append({'block': name, 'pos': b['pos'], 'ids': ent_ids(nbt)})
        if name in ('minecraft:jigsaw',) and nbt.get('pool'):
            pools.add(nbt['pool'])
    ents = defaultdict(int)
    for e in r.get('entities', []):
        eid = (e.get('nbt') or {}).get('id')
        if eid: ents[eid] += 1
    return spawners, sorted(pools), dict(ents)


def pool_templates(j, out):
    for el in j.get('elements', []):
        walk_el(el.get('element', {}), out)


def walk_el(e, out):
    if 'location' in e and isinstance(e['location'], str): out.add(e['location'])
    for sub in e.get('elements', []) or []: walk_el(sub, out)


def sources():
    yield 'kubejs/data', None, os.path.join(ROOT, 'kubejs/data')
    for f in sorted(os.listdir(os.path.join(ROOT, 'mods'))):
        if f.endswith('.jar'):
            yield f, os.path.join(ROOT, 'mods', f), None


def iter_files(name, jar, d):
    """yield (src, path, bytes-getter)"""
    if d:
        for dp, _, fs in os.walk(d):
            for f in fs:
                full = os.path.join(dp, f)
                rel = 'data/' + os.path.relpath(full, d)
                yield name, rel, (lambda p=full: open(p, 'rb').read())
        return
    def walk_zip(zname, z):
        for n in z.namelist():
            if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
                try:
                    with zipfile.ZipFile(io.BytesIO(z.read(n))) as zz:
                        yield from walk_zip(zname + '!' + n.rsplit('/', 1)[1], zz)
                except zipfile.BadZipFile:
                    pass
            else:
                yield zname, n, (lambda n=n, z=z: z.read(n))
    with zipfile.ZipFile(jar) as z:
        yield from walk_zip(name, z)


templates = {}       # id -> {src, spawners, pools, ents, overridden_by}
structs = {}         # id -> {src, json}
pool_files = {}      # id -> set(templates), set(fallback/pool refs)
procs = {}
code_refs = defaultdict(set)
CODE_PATTERNS = {b'net/minecraft/world/level/block/entity/SpawnerBlockEntity': 'SpawnerBlockEntity',
                 b'f_50085_': 'Blocks.SPAWNER(srg)'}

for name, jar, d in sources():
    for src, path, get in iter_files(name, jar, d):
        m = TPL_RE.match(path)
        if m:
            tid = f'{m.group(1)}:{m.group(2)}'
            if tid in templates:  # kubejs/data scanned first = wins
                templates[tid].setdefault('shadowed', []).append(src); continue
            try:
                sp, pl, en = scan_template(get())
            except Exception as ex:
                templates[tid] = {'src': src, 'error': str(ex)}; continue
            templates[tid] = {'src': src, 'spawners': sp, 'pools': pl, 'ents': en}
            continue
        for rx, store in ((STRUCT_RE, structs), (POOL_RE, pool_files), (PROC_RE, procs)):
            m = rx.match(path)
            if m:
                rid = f'{m.group(1)}:{m.group(2)}'
                if rid in store: break
                try:
                    j = json.loads(get().decode('utf-8', 'replace'))
                except Exception:
                    j = None
                store[rid] = {'src': src, 'json': j}
                break
        else:
            if path.endswith('.class') and jar:
                b = get()
                for pat, lab in CODE_PATTERNS.items():
                    if pat in b:
                        code_refs[src].add(path + ' [' + lab + ']')

# pool graph
pool_tpl = {}
pool_next = {}
for pid, rec in pool_files.items():
    j = rec['json'] or {}
    out = set(); pool_templates(j, out)
    pool_tpl[pid] = out
    fb = j.get('fallback')
    pool_next[pid] = {fb} if fb and fb != 'minecraft:empty' else set()


def reach(start_pool):
    seen_p, seen_t, todo = set(), set(), [start_pool]
    while todo:
        p = todo.pop()
        if p in seen_p: continue
        seen_p.add(p)
        for t in pool_tpl.get(p, ()):
            if t in seen_t: continue
            seen_t.add(t)
            for np in (templates.get(t) or {}).get('pools', []):
                todo.append(np)
        todo.extend(pool_next.get(p, ()))
    return seen_t


result = {'structures': {}, 'templates_with_spawners': {}, 'procs_with_spawner': {}, 'code_refs': {}}
for sid, rec in sorted(structs.items()):
    j = rec['json'] or {}
    so = j.get('spawn_overrides') or {}
    tps = reach(j['start_pool']) if isinstance(j.get('start_pool'), str) else set()
    sp, ill = [], defaultdict(int)
    for t in sorted(tps):
        tr = templates.get(t) or {}
        for s in tr.get('spawners', []): sp.append({'template': t, **s})
        for e, n in (tr.get('ents') or {}).items():
            if e in ILLAGERS: ill[e] += n
    if so or sp or ill or 'pillager' in sid or 'illager' in sid:
        result['structures'][sid] = {'src': rec['src'], 'type': j.get('type'), 'biomes': j.get('biomes'),
                                     'spawn_overrides': so, 'spawners': sp, 'illager_template_ents': dict(ill),
                                     'n_templates': len(tps)}
for tid, tr in sorted(templates.items()):
    if tr.get('spawners'):
        result['templates_with_spawners'][tid] = {'src': tr['src'], 'spawners': tr['spawners'],
                                                  'ents': tr.get('ents'), 'shadowed': tr.get('shadowed')}
for pid, rec in procs.items():
    if rec['json'] and 'spawner' in json.dumps(rec['json']):
        result['procs_with_spawner'][pid] = {'src': rec['src'], 'json': rec['json']}
result['code_refs'] = {k: sorted(v) for k, v in code_refs.items()}
result['counts'] = {'templates': len(templates), 'structures': len(structs), 'pools': len(pool_files)}
json.dump(result, open(OUT, 'w'), indent=1, default=list)
print(result['counts'])
