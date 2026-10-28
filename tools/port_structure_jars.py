#!/usr/bin/env python3
"""Port data-only structure mods (MCreator one-building jars and similar) into kubejs/data.

Many 1.19.2-labelled MCreator jars only have a `(FMLJavaModLoadingContext)` constructor, which
Forge 43.3.9 can't call (NoSuchMethodException: <Mod>.<init>()), so the game won't start. Their
only content is a datapack, so this copies it to kubejs/data/<ns>/ and moves the jar to
disabledmods/archive/. Data-only jars that load fine can be ported the same way.

Per jar:
  - refuses a jar with more than one .class file (real code: keep the jar)
  - copies data/** except pack metadata; only the templates a template_pool references
    (unreferenced leftovers are listed, not copied); the 1.21 `data/*/structure/` dir is skipped
  - copies a non-empty assets/*/lang/*.json to kubejs/assets (advancement titles etc.)
  - rewrites every template so 1.19.2 can place it (see BLOCK_REMAP / strip_unknown_items)
  - applies POOL_REPOINT (a pool entry pointing at a template 1.19.2 can't read)

Templates saved in 1.20.1 load in 1.19.2 (DFU never downgrades, the format is the same), but
blocks and items 1.19.2 doesn't have load as air / empty stacks. A template saved in 1.20.5+
(DataVersion >= 3837) has a different item/entity format: the script refuses it unless a
POOL_REPOINT replaces it.

Entities from mods not in the pack are stripped, and ENTITY_STRIP drops listed vanilla ones per template.
Loot tables: item entries naming an item 1.19.2 / the pack doesn't have are dropped; a table of a loot type
1.19.2 doesn't have (SKIP_LOOT_TYPES) is not copied.

A source may also be an unpacked mod directory (DecompiledMods/<mod>); a directory is never moved.

  python3 tools/port_structure_jars.py [--dry-run] [--keep-jar] mods/<jar>|<dir> ...
"""
import argparse, collections, glob, gzip, io, json, os, re, shutil, struct, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KJS_DATA = os.path.join(ROOT, 'kubejs/data')
KJS_ASSETS = os.path.join(ROOT, 'kubejs/assets')
DISABLED = os.path.join(ROOT, 'disabledmods/archive')
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pack_paths  # noqa: E402

VANILLA_JAR = str(pack_paths.vanilla_jar())   # resources only are read (tools/pack_paths.py)
ITEM_FORMAT_CHANGE = 3837  # 1.20.5: item components, lowercase count, new banner/attribute format

# 1.20+ blocks -> a 1.19.2 (or in-pack modded) block. None = air. keep_props: True copies the blockstate
# properties across, False drops them, a tuple keeps only those names, a dict sets exactly those.
# strip_be: drop the block entity nbt.
CONNECTIONS = ('north', 'east', 'south', 'west', 'waterlogged')   # fences and panes without the diagonal ones
BLOCK_REMAP = {
    'minecraft:decorated_pot': ('supplementaries:urn', False, True),
    'minecraft:chiseled_bookshelf': ('minecraft:bookshelf', False, True),
    'minecraft:short_grass': ('minecraft:grass', True, False),
    'minecraft:pink_petals': (None, False, True),
    # Camps. Castles. Carriages. 2.3.4 (2026-10-14): archaeology blocks lose their loot (D6); a hanging sign
    # under a chain becomes a hanging lantern (a wall sign there would have no wall); Diagonal Fences /
    # Diagonal Windows are not in the pack.
    'minecraft:suspicious_gravel': ('minecraft:gravel', False, True),
    'minecraft:suspicious_sand': ('minecraft:sand', False, True),
    'minecraft:mangrove_hanging_sign': ('minecraft:lantern', {'hanging': 'true', 'waterlogged': 'false'}, True),
    'diagonalfences:minecraft/dark_oak_fence': ('minecraft:dark_oak_fence', CONNECTIONS, False),
    'diagonalwindows:minecraft/purple_stained_glass_pane': ('minecraft:purple_stained_glass_pane', CONNECTIONS, False),
}

# Vanilla entities to drop from one template (template id -> entity ids).
ENTITY_STRIP = {
    'eugene:red_roof': {'minecraft:zombie'},   # a castle that is a peaceful settlement holds no hostile mob (7.4.2, D5)
}

# Loot tables of a type 1.19.2 can't parse (the whole file would fail to load): not copied.
SKIP_LOOT_TYPES = {'minecraft:archaeology'}
for _w in ('log', 'wood', 'planks', 'slab', 'stairs', 'fence', 'fence_gate', 'trapdoor', 'door',
           'button', 'pressure_plate', 'sign', 'wall_sign', 'leaves', 'sapling'):
    BLOCK_REMAP[f'minecraft:cherry_{_w}'] = (f'vinery:cherry_{_w}', True, False)
    BLOCK_REMAP[f'minecraft:stripped_cherry_{_w}'] = (f'vinery:stripped_cherry_{_w}', True, False)

# Pool entries whose template 1.19.2 can't read, pointed at a readable variant in the same jar.
POOL_REPOINT = {
    # Mysticrift's pools use templates re-saved in 1.21 (DataVersion 3955); the jar still carries
    # the 1.20.1 saves of the same towers (~90% the same blocks, with pillagers placed as entities).
    'mysticrift_pillagers_towers_aris:acacia': 'mysticrift_pillagers_towers_aris:acacia_pillager_tower_new',
    'mysticrift_pillagers_towers_aris:birch': 'mysticrift_pillagers_towers_aris:birch_pillager_tower_new',
    'mysticrift_pillagers_towers_aris:cherry': 'mysticrift_pillagers_towers_aris:cherry_pillager_tower_new',
    'mysticrift_pillagers_towers_aris:jungle': 'mysticrift_pillagers_towers_aris:jungle_pillager_tower',
    'mysticrift_pillagers_towers_aris:mangrove': 'mysticrift_pillagers_towers_aris:mangrove_pillager_tower_new',
    'mysticrift_pillagers_towers_aris:oak': 'mysticrift_pillagers_towers_aris:oak_wood_pillager_tower_new',
    'mysticrift_pillagers_towers_aris:sand': 'mysticrift_pillagers_towers_aris:desert_pillager_tower_new',
}

SKIP_TOP = ('META-INF/', 'pack.mcmeta', 'pack.png', 'fabric.mod.json', 'quilt.mod.json')


# ---- typed NBT (round-trips byte for byte) ----
class Tag:
    __slots__ = ('t', 'v', 'et')
    def __init__(self, t, v, et=None):
        self.t, self.v, self.et = t, v, et


def nbt_read(buf):
    f = io.BytesIO(buf)
    def r(fmt):
        return struct.unpack(fmt, f.read(struct.calcsize(fmt)))[0]
    def rs():
        return f.read(r('>H')).decode('utf-8')
    fmts = {1: '>b', 2: '>h', 3: '>i', 4: '>q', 5: '>f', 6: '>d'}
    def payload(t):
        if t in fmts: return Tag(t, r(fmts[t]))
        if t == 7: return Tag(t, f.read(r('>i')))
        if t == 8: return Tag(t, rs())
        if t == 9:
            et = r('>b'); n = r('>i'); return Tag(t, [payload(et) for _ in range(n)], et)
        if t == 10:
            d = {}
            while True:
                tt = r('>b')
                if tt == 0: return Tag(t, d)
                name = rs(); d[name] = payload(tt)
        if t == 11: return Tag(t, [r('>i') for _ in range(r('>i'))])
        if t == 12: return Tag(t, [r('>q') for _ in range(r('>i'))])
        raise ValueError(f'bad tag {t}')
    t = r('>b'); name = rs()
    return name, payload(t)


def nbt_write(name, tag):
    out = io.BytesIO()
    def w(fmt, v): out.write(struct.pack(fmt, v))
    def ws(s):
        b = s.encode('utf-8'); w('>H', len(b)); out.write(b)
    fmts = {1: '>b', 2: '>h', 3: '>i', 4: '>q', 5: '>f', 6: '>d'}
    def payload(tg):
        t = tg.t
        if t in fmts: w(fmts[t], tg.v)
        elif t == 7: w('>i', len(tg.v)); out.write(tg.v)
        elif t == 8: ws(tg.v)
        elif t == 9:
            w('>b', tg.v[0].t if tg.v else (tg.et or 0)); w('>i', len(tg.v))
            for x in tg.v: payload(x)
        elif t == 10:
            for k, x in tg.v.items():
                w('>b', x.t); ws(k); payload(x)
            w('>b', 0)
        elif t == 11: w('>i', len(tg.v)); [w('>i', x) for x in tg.v]
        elif t == 12: w('>i', len(tg.v)); [w('>q', x) for x in tg.v]
    w('>b', tag.t); ws(name); payload(tag)
    return out.getvalue()


def S(v): return Tag(8, v)


# ---- what 1.19.2 + the pack has ----
def known_ids():
    vj = zipfile.ZipFile(VANILLA_JAR)
    items = set()
    for n in vj.namelist():
        m = re.match(r'assets/minecraft/(blockstates|models/item)/([^/]+)\.json$', n)
        if m: items.add('minecraft:' + m.group(2))
    mods = set(['minecraft'])
    for j in glob.glob(os.path.join(ROOT, 'mods/*.jar')):
        try:
            for n in zipfile.ZipFile(j).namelist():
                m = re.match(r'(?:assets|data)/([^/]+)/', n)
                if m: mods.add(m.group(1))
        except zipfile.BadZipFile:
            pass
    return items, mods


def item_ok(iid, vitems, mods):
    ns = iid.split(':')[0] if ':' in iid else 'minecraft'
    if ns == 'minecraft': return iid in vitems
    return ns in mods


def strip_unknown_items(tag, vitems, mods, dropped):
    """Unknown item stacks load as empty in 1.19.2; drop them so containers/frames stay clean."""
    if tag.t == 10:
        d = tag.v
        for k in list(d):
            x = d[k]
            if x.t == 10 and 'id' in x.v and x.v['id'].t == 8 and 'Count' in x.v and not item_ok(x.v['id'].v, vitems, mods):
                dropped[x.v['id'].v] += 1
                del d[k]
                continue
            if x.t == 9 and x.v and x.v[0].t == 10:
                keep = []
                for e in x.v:
                    if 'id' in e.v and e.v['id'].t == 8 and 'Count' in e.v and not item_ok(e.v['id'].v, vitems, mods):
                        dropped[e.v['id'].v] += 1
                        if k in ('HandItems', 'ArmorItems'):
                            keep.append(Tag(10, {}))  # positional slots
                        continue
                    keep.append(e)
                x.v = keep
            strip_unknown_items(x, vitems, mods, dropped)
    elif tag.t == 9:
        for e in tag.v: strip_unknown_items(e, vitems, mods, dropped)


def entity_ok(eid, mods):
    return ':' not in eid or eid.split(':')[0] in mods


def convert_template(buf, vitems, mods, stats, loc=None):
    raw = gzip.decompress(buf)
    name, root = nbt_read(raw)
    assert nbt_write(name, root) == raw, 'NBT round-trip mismatch'
    d = root.v
    dv = d['DataVersion'].v if 'DataVersion' in d else 0
    if dv >= ITEM_FORMAT_CHANGE:
        raise ValueError(f'DataVersion {dv} (1.20.5+ item format): add a POOL_REPOINT or port it by hand')
    palettes = d['palettes'].v if 'palettes' in d else [d['palette']]
    strip_states = set()
    for pal in palettes:
        for i, st in enumerate(pal.v):
            bname = st.v['Name'].v
            if bname not in BLOCK_REMAP:
                continue
            new, keep_props, strip_be = BLOCK_REMAP[bname]
            stats['blocks'][f'{bname} -> {new or "air"}'] += 1
            if strip_be: strip_states.add(i)
            if new is None:
                st.v = {'Name': S('minecraft:air')}
                continue
            st.v['Name'] = S(new)
            if isinstance(keep_props, dict):
                st.v['Properties'] = Tag(10, {k: S(v) for k, v in keep_props.items()})
            elif isinstance(keep_props, tuple):
                if 'Properties' in st.v:
                    st.v['Properties'].v = {k: v for k, v in st.v['Properties'].v.items() if k in keep_props}
            elif not keep_props:
                st.v.pop('Properties', None)
    for b in d['blocks'].v:
        if b.v['state'].v in strip_states: b.v.pop('nbt', None)
    strip_unknown_items(d['blocks'], vitems, mods, stats['items'])
    if 'entities' in d:
        keep = []
        for e in d['entities'].v:
            eid = e.v['nbt'].v['id'].v if 'nbt' in e.v and 'id' in e.v['nbt'].v else ''
            if not entity_ok(eid, mods) or eid in ENTITY_STRIP.get(loc, ()):
                stats['entities'][f'{loc}: {eid}'] += 1
                continue
            keep.append(e)
        d['entities'].v = keep
        strip_unknown_items(d['entities'], vitems, mods, stats['items'])
    out = nbt_write(name, root)
    return gzip.compress(out, mtime=0)


class DirSource:
    """An unpacked mod (DecompiledMods/<mod>) read like a zip."""
    def __init__(self, path):
        self.path = path
    def namelist(self):
        return sorted(os.path.relpath(os.path.join(d, f), self.path).replace(os.sep, '/')
                      for d, _, fs in os.walk(self.path) for f in fs)
    def read(self, name):
        with open(os.path.join(self.path, name), 'rb') as fh: return fh.read()


def mod_id(z):
    toml = z.read('META-INF/mods.toml').decode('utf-8', 'replace')
    return re.search(r'modId\s*=\s*["\']([^"\']+)["\']', toml).group(1)   # Modrinth-generated tomls use single quotes


def clean_loot(raw, vitems, mods, stats, name):
    """A loot table without item entries 1.19.2 / the pack doesn't have; None when its type can't load at all."""
    j = json.loads(raw)
    if j.get('type') in SKIP_LOOT_TYPES:
        stats['loot'][f'{name}: not copied (type {j["type"]})'] += 1
        return None
    dropped = []
    def walk(x):
        if isinstance(x, dict):
            for k, v in x.items():
                if k == 'entries' and isinstance(v, list):
                    keep = []
                    for e in v:
                        if isinstance(e, dict) and e.get('type') in ('minecraft:item', 'item') and not item_ok(e.get('name', ''), vitems, mods):
                            dropped.append(e['name'])
                            continue
                        keep.append(e)
                    x[k] = v = keep
                walk(v)
        elif isinstance(x, list):
            for v in x: walk(v)
    walk(j)
    if not dropped:
        return raw
    for d in dropped: stats['loot'][f'{name}: dropped {d}'] += 1
    return (json.dumps(j, indent=2) + '\n').encode()


def port_predicate(j):
    """1.20 all_of / any_of -> 1.19.2: a predicate file that is a JSON array means all of them; any_of is alternative."""
    if isinstance(j, list):
        return [port_predicate(x) for x in j]
    if isinstance(j, dict):
        j = {k: port_predicate(v) for k, v in j.items()}
        if j.get('condition') == 'minecraft:all_of':
            return j['terms']
        if j.get('condition') == 'minecraft:any_of':
            j['condition'] = 'minecraft:alternative'
    return j


def port(jar, vitems, mods, dry, keep_jar):
    is_dir = os.path.isdir(jar)
    z = DirSource(jar) if is_dir else zipfile.ZipFile(jar)
    names = z.namelist()
    classes = [n for n in names if n.endswith('.class')]
    if len(classes) > 1:
        print(f'SKIP {os.path.basename(jar)}: {len(classes)} classes (has code)')
        return False
    mid = mod_id(z)
    data = [n for n in names if n.startswith('data/') and not n.endswith('/')]
    pools = [n for n in data if '/worldgen/template_pool/' in n]
    jar_ns = set(n.split('/')[1] for n in data)
    present = set(n.split('/')[1] + ':' + '/'.join(n.split('/')[3:])[:-4]
                  for n in data if n.split('/')[2] == 'structures' and n.endswith('.nbt'))
    referenced, pruned = set(), []
    pool_json = {}
    for p in pools:
        j = json.loads(z.read(p))
        kept = []
        for e in j.get('elements', []):
            el = e['element']
            locs = []
            for sub in ([el] + el.get('elements', [])):
                loc = sub.get('location')
                if isinstance(loc, str):
                    if loc in POOL_REPOINT:
                        sub['location'] = loc = POOL_REPOINT[loc]
                    locs.append(loc)
            # vanilla templates always exist, even when the jar also ships data/minecraft (overrides)
            gone = [l for l in locs if l.split(':')[0] in jar_ns - {'minecraft'} and l not in present]
            if gone:  # the jar's pool names a template the jar doesn't ship: loads as an empty piece
                pruned += gone
                continue
            referenced.update(locs)
            kept.append(e)
        if pruned and len(kept) != len(j.get('elements', [])):
            j['elements'] = kept
        if 'name' not in j:  # 1.20 dropped the pool's own name; 1.19.2's codec requires it (world load fails without)
            parts = p.split('/')
            j = {'name': parts[1] + ':' + '/'.join(parts[4:])[:-5], **j}
        pool_json[p] = j
    stats = {'blocks': collections.Counter(), 'items': collections.Counter(), 'entities': collections.Counter(),
             'loot': collections.Counter()}
    written, unreferenced, copied = [], [], set()
    namespaces = set()
    for n in data:
        parts = n.split('/')
        ns, kind = parts[1], parts[2]
        if kind == 'structure':  # 1.21 layout, not read by 1.19.2
            continue
        dest = os.path.join(KJS_DATA, *parts[1:])
        if os.path.exists(dest):
            raise SystemExit(f'{dest} already exists: {mid} collides with something in kubejs/data')
        namespaces.add(ns)
        if kind == 'structures' and n.endswith('.nbt'):
            loc = ns + ':' + '/'.join(parts[3:])[:-4]
            if loc not in referenced and ns != 'minecraft':   # a minecraft: template overrides vanilla's (its pools use it)
                unreferenced.append(loc)
                continue
            content = convert_template(z.read(n), vitems, mods, stats, loc)
            copied.add(loc)
        elif kind == 'loot_tables' and n.endswith('.json'):
            content = clean_loot(z.read(n), vitems, mods, stats, ns + ':' + '/'.join(parts[3:])[:-5])
            if content is None:
                continue
        elif n in pool_json:
            content = (json.dumps(pool_json[n], indent=2) + '\n').encode()
        elif kind == 'predicates' and n.endswith('.json'):
            content = (json.dumps(port_predicate(json.loads(z.read(n))), indent=2) + '\n').encode()
        else:
            content = z.read(n)
        written.append((dest, content))
    for n in names:
        if re.match(r'assets/[^/]+/lang/[^/]+\.json$', n) and len(json.loads(z.read(n) or b'{}')) > 0:
            written.append((os.path.join(KJS_ASSETS, *n.split('/')[1:]), z.read(n)))
    print(f'{os.path.basename(jar)} -> kubejs/data/{",".join(sorted(namespaces))}  ({mid}, {len(written)} files)')
    for k, v in sorted(stats['blocks'].items()): print(f'    remap {k}  x{v} palette entries')
    if stats['items']: print(f'    dropped unknown items: {dict(stats["items"])}')
    for k, v in sorted(stats['entities'].items()): print(f'    stripped entity {k}  x{v}')
    for k in sorted(stats['loot']): print(f'    loot {k}')
    if unreferenced: print(f'    not copied (no pool uses them): {", ".join(sorted(unreferenced))}')
    if pruned: print(f'    pool entries dropped, template not in the jar ({len(pruned)}): {", ".join(sorted(pruned))}')
    if dry:
        return True
    for dest, content in written:
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        if dest.startswith(KJS_ASSETS) and os.path.exists(dest):
            merged = json.load(open(dest)); merged.update(json.loads(content))
            content = (json.dumps(merged, indent=2) + '\n').encode()
        with open(dest, 'wb') as fh: fh.write(content)
    if not keep_jar and not is_dir:
        os.makedirs(DISABLED, exist_ok=True)
        shutil.move(jar, os.path.join(DISABLED, os.path.basename(jar)))
    return True


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('jars', nargs='+')
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--keep-jar', action='store_true', help='leave the jar in mods/')
    a = ap.parse_args()
    vitems, mods = known_ids()
    for dep in set(v[0].split(':')[0] for v in BLOCK_REMAP.values() if v[0]):
        if dep not in mods: sys.exit(f'BLOCK_REMAP target mod {dep} is not in mods/')
    ok = sum(port(j, vitems, mods, a.dry_run, a.keep_jar) for j in a.jars)
    print(f'{ok}/{len(a.jars)} ported{" (dry run)" if a.dry_run else ""}')


if __name__ == '__main__':
    main()
