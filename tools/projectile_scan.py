#!/usr/bin/env python3
"""Projectile scan for the pass-through web groups (plan_passthrough.md 5.1, phase 4).

Reads every class in mods/*.jar (nested jars included) and the Mojmap Minecraft jar, walks superclasses to
net.minecraft.world.entity.projectile.Projectile, and lists every concrete projectile class with its entity id, the
group its base class gives (same table as Creative Compat surface/PassGroup.byClassChain) and the group its
creativecompat:passthrough/projectile/<group> tag gives.

Entity ids come from the registration code: in each method, a constructor reference (Bullet::new, or a lambda that
does `new Bullet`) is paired with the nearest id-like string constant; the namespace is the jar's mod id. A guess is
kept only when the id is a known entity (lang keys `entity.<ns>.<path>` in the jars, the /dumpnames and /dumptags
exports), else the class is listed as unresolved.

Tag members: from the in-game /dumptags export (kubejs/exported/tags/entity_type_tags.json), or from
tools/passthrough_tag_sim.js output (offline, before the game has loaded the tags):
  python3 tools/projectile_scan.py                          tags from the last /dumptags
  node tools/passthrough_tag_sim.js /tmp/pass.json && python3 tools/projectile_scan.py --tags /tmp/pass.json
  python3 tools/projectile_scan.py --all                    also print every tagged projectile
Report: projectiles with no group tag (they run on the base-class default), tagged ids that are not projectiles,
and ids in two groups. Exit 3 when a projectile has no group tag or an id is in two groups.
Writes tools/projectile_scan.json (class, jar, id, chain, default, tag group) for later diffs.
"""
import glob, io, json, os, re, struct, sys, zipfile, collections

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pack_paths  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'tools', 'projectile_scan.json')
PROJECTILE = 'net/minecraft/world/entity/projectile/Projectile'
GROUPS = ['break', 'settle', 'snag', 'linger', 'hang', 'slow', 'exempt']
TAG = 'creativecompat:passthrough/projectile/'
EXEMPT_ALL = 'creativecompat:passthrough_exempt/all'
# Creative Compat surface/PassGroup.BY_CLASS, most specific first (keep in step with the Java)
BY_CLASS = [
    ('net/minecraft/world/entity/projectile/FishingHook', 'snag'),
    ('net/minecraft/world/entity/projectile/FireworkRocketEntity', 'linger'),
    ('net/minecraft/world/entity/projectile/ThrownPotion', 'settle'),
    ('net/minecraft/world/entity/projectile/ThrownExperienceBottle', 'settle'),
    ('vazkii/quark/content/tools/entity/rang/AbstractPickarang', 'snag'),
    ('vazkii/botania/common/entity/ThornChakramEntity', 'snag'),
    ('net/minecraft/world/entity/projectile/AbstractArrow', 'hang'),
    ('net/minecraft/world/entity/projectile/ThrowableProjectile', 'break'),
    ('net/minecraft/world/entity/projectile/AbstractHurtingProjectile', 'break'),
    ('net/minecraft/world/entity/projectile/LlamaSpit', 'break'),
    ('net/minecraft/world/entity/projectile/ShulkerBullet', 'break'),
]
BY_CLASS_MAP = dict(BY_CLASS)
ID_LIKE = re.compile(r'^[a-z0-9_.]+(/[a-z0-9_.]+)*$')

# ── class files ───────────────────────────────────────────────────────────────
# opcode -> operand bytes (tableswitch 0xaa, lookupswitch 0xab and wide 0xc4 handled in code)
OPLEN = [0] * 256
for op in (0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3a, 0xa9, 0xbc): OPLEN[op] = 1
for op in (0x11, 0x13, 0x14, 0x84, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xbb, 0xbd, 0xc0, 0xc1, 0xc6, 0xc7):
    OPLEN[op] = 2
for op in range(0x99, 0xa9): OPLEN[op] = 2
OPLEN[0xc5] = 3
for op in (0xb9, 0xba, 0xc8, 0xc9): OPLEN[op] = 4


class ClassFile:
    __slots__ = ('name', 'sup', 'abstract', 'methods', 'cp', 'bsm')

    def __init__(self, b):
        u2 = lambda i: struct.unpack_from('>H', b, i)[0]
        u4 = lambda i: struct.unpack_from('>I', b, i)[0]
        n = u2(8); i = 10; cp = [None] * n; k = 1
        while k < n:
            t = b[i]
            if t == 1:
                l = u2(i + 1); cp[k] = ('u', b[i + 3:i + 3 + l].decode('utf8', 'replace')); i += 3 + l
            elif t in (7, 8, 16, 19, 20): cp[k] = (t, u2(i + 1)); i += 3
            elif t in (3, 4): i += 5
            elif t in (5, 6): i += 9; k += 1
            elif t in (9, 10, 11, 12, 17, 18): cp[k] = (t, u2(i + 1), u2(i + 3)); i += 5
            elif t == 15: cp[k] = (15, b[i + 1], u2(i + 2)); i += 4
            else: raise ValueError('cp tag %d' % t)
            k += 1
        self.cp = cp
        acc, this, sup = u2(i), u2(i + 2), u2(i + 4)
        self.abstract = bool(acc & 0x0600)  # abstract or interface
        self.name = self.cls(this); self.sup = self.cls(sup) if sup else None
        i += 6; i += 2 + 2 * u2(i)
        nf = u2(i); i += 2
        for _ in range(nf):  # fields
            i += 6; na = u2(i); i += 2
            for _ in range(na): i += 6 + u4(i + 2)
        self.methods = {}
        nm = u2(i); i += 2
        for _ in range(nm):
            mname = self.utf(u2(i + 2)); mdesc = self.utf(u2(i + 4)); na = u2(i + 6); i += 8
            for _ in range(na):
                aname = self.utf(u2(i)); alen = u4(i + 2)
                if aname == 'Code':
                    clen = u4(i + 10); self.methods[mname + mdesc] = b[i + 14:i + 14 + clen]
                i += 6 + alen
        self.bsm = []
        na = u2(i); i += 2
        for _ in range(na):
            aname = self.utf(u2(i)); alen = u4(i + 2)
            if aname == 'BootstrapMethods':
                j = i + 6; cnt = u2(j); j += 2
                for _ in range(cnt):
                    nargs = u2(j + 2); self.bsm.append([u2(j + 4 + 2 * a) for a in range(nargs)]); j += 4 + 2 * nargs
            i += 6 + alen

    def utf(self, x): return self.cp[x][1]
    def cls(self, x): return self.utf(self.cp[x][1])

    def member(self, x):
        """Fieldref / Methodref -> (owner, name, desc)"""
        e = self.cp[x]; nat = self.cp[e[2]]
        return self.cls(e[1]), self.utf(nat[1]), self.utf(nat[2])

    def events(self, code):
        """('str', s) for string constants, ('new', cls) for `new`, ('handle', owner, name, desc) for the method
        handles an invokedynamic passes (lambdas and Foo::new), in code order."""
        out = []; pc = 0; n = len(code)
        while pc < n:
            op = code[pc]
            if op in (0x12, 0x13):
                idx = code[pc + 1] if op == 0x12 else struct.unpack_from('>H', code, pc + 1)[0]
                e = self.cp[idx]
                if e and e[0] == 8: out.append(('str', self.utf(e[1])))
            elif op == 0xbb:
                out.append(('new', self.cls(struct.unpack_from('>H', code, pc + 1)[0])))
            elif op == 0xba:
                e = self.cp[struct.unpack_from('>H', code, pc + 1)[0]]
                if e and e[1] < len(self.bsm):
                    for a in self.bsm[e[1]]:
                        h = self.cp[a]
                        if h and h[0] == 15:
                            out.append(('handle',) + self.member(h[2]))
            if op == 0xaa:
                p = (pc + 4) & ~3; lo, hi = struct.unpack_from('>ii', code, p + 4); pc = p + 12 + 4 * (hi - lo + 1); continue
            if op == 0xab:
                p = (pc + 4) & ~3; cnt = struct.unpack_from('>i', code, p + 4)[0]; pc = p + 8 + 8 * cnt; continue
            if op == 0xc4:
                pc += 6 if code[pc + 1] == 0x84 else 4; continue
            pc += 1 + OPLEN[op]
        return out


def jar_modid(z):
    for n in ('META-INF/mods.toml',):
        if n in z.namelist():
            m = re.search(r'modId\s*=\s*"([^"]+)"', z.read(n).decode('utf8', 'replace'))
            if m: return m.group(1)
    return None


def scan_jar(z, label, classes, lang_ids, depth=0):
    modid = jar_modid(z)
    for n in z.namelist():
        if n.endswith('.class') and not n.startswith('META-INF'):
            try: c = ClassFile(z.read(n))
            except Exception: continue
            classes[c.name] = (c, label, modid)
        elif re.match(r'^assets/[^/]+/lang/en_us\.json$', n):
            try: lang = json.loads(z.read(n).decode('utf8', 'replace'))
            except Exception: continue
            for k in lang:
                m = re.match(r'^entity\.([a-z0-9_.-]+)\.([a-z0-9_./-]+)$', k)
                if m: lang_ids.add(m.group(1) + ':' + m.group(2))
        elif n.startswith('META-INF/jarjar/') and n.endswith('.jar') and depth < 3:
            try: scan_jar(zipfile.ZipFile(io.BytesIO(z.read(n))), n.rsplit('/', 1)[-1], classes, lang_ids, depth + 1)
            except zipfile.BadZipFile: pass


def chain(name, classes):
    out = []
    while name and len(out) < 40:
        out.append(name)
        e = classes.get(name)
        name = e[0].sup if e else None
    return out


def snake(s):
    return re.sub(r'(?<=[a-z0-9])(?=[A-Z])', '_', s).lower()


def main(argv):
    tags_path = os.path.join(ROOT, 'kubejs/exported/tags/entity_type_tags.json')
    if '--tags' in argv: tags_path = argv[argv.index('--tags') + 1]
    show_all = '--all' in argv

    classes, lang_ids = {}, set()
    mc = pack_paths.forge_mapped_jar()
    if not mc: sys.exit('Mojmap Minecraft jar not found (pack_paths.forge_mapped_jar)')
    scan_jar(zipfile.ZipFile(mc), 'minecraft', classes, lang_ids)
    for n in [k for k, v in classes.items() if v[1] == 'minecraft']:
        c, label, _ = classes[n]; classes[n] = (c, label, 'minecraft')
    if pack_paths.vanilla_jar(): scan_jar(zipfile.ZipFile(pack_paths.vanilla_jar()), 'minecraft-assets', {}, lang_ids)
    for jar in sorted(glob.glob(os.path.join(ROOT, 'mods/*.jar'))):
        try: scan_jar(zipfile.ZipFile(jar), os.path.basename(jar), classes, lang_ids)
        except zipfile.BadZipFile: print('skipped unreadable jar', jar)

    known = set(lang_ids)
    try:
        for e in json.load(open(os.path.join(ROOT, 'kubejs/exported/display_names.json')))['entries'].get('entity', []):
            known.add(e['id'])
    except (OSError, ValueError, KeyError): pass
    try:
        for mod in json.load(open(os.path.join(ROOT, 'kubejs/exported/tags/entity_type_tags_by_entity_type.json'))).values():
            known.update(mod)
    except (OSError, ValueError): pass

    proj = {}
    for name, (c, label, modid) in classes.items():
        ch = chain(name, classes)
        if PROJECTILE in ch[1:] and not c.abstract and not re.search(r'\$\d+$', name):  # anonymous: never registered
            proj[name] = ch

    # ctor references -> candidate ids, per method: the nearest id-like string before the reference (vanilla and
    # register("x", Builder.of(X::new)) style), else the nearest after it (a supplier lambda that ends in build("x")).
    # An id that is not a known entity is kept as "unverified", used only when no reference gives a known one.
    cand, loose = collections.defaultdict(set), collections.defaultdict(set)
    def ctor_in(c, body):
        try: ev = c.events(body)
        except Exception: return None
        hit = {x[1] for x in ev if x[0] == 'new' and x[1] in proj} | {
            x[1] for x in ev if x[0] == 'handle' and x[2] == '<init>' and x[1] in proj}
        return hit.pop() if len(hit) == 1 else None
    for name, (c, label, modid) in classes.items():
        for mkey, code in c.methods.items():
            try: ev = c.events(code)
            except Exception: continue
            hits = []
            for pos, e in enumerate(ev):
                target = None
                if e[0] == 'handle' and e[2] == '<init>' and e[1] in proj: target = e[1]
                elif e[0] == 'handle' and e[1] == name and e[2].startswith('lambda$'):
                    body = c.methods.get(e[2] + e[3])
                    if body: target = ctor_in(c, body)
                if target: hits.append((pos, target))
            if not hits: continue
            strs = [(pos, e[1]) for pos, e in enumerate(ev) if e[0] == 'str' and ID_LIKE.match(e[1].split(':')[-1])]
            ns = modid or 'minecraft'
            for pos, target in hits:
                before = [s for p2, s in sorted(strs, key=lambda x: -x[0]) if p2 < pos]
                after = [s for p2, s in sorted(strs) if p2 > pos]
                pick = (before or after)[:1]
                for s in pick:
                    rid = s if ':' in s else ns + ':' + s
                    (cand if rid in known else loose)[target].add(rid)

    tags = {}
    if os.path.isfile(tags_path):
        raw = json.load(open(tags_path))
        for k, v in raw.items():
            tags[k.lstrip('#')] = set(v)
    else:
        print('no tag file %s: every projectile shows as untagged' % os.path.relpath(tags_path, ROOT))
    member = collections.defaultdict(list)
    for g in GROUPS:
        for i in tags.get(TAG + g, ()): member[i].append(g)
    exempt_all = tags.get(EXEMPT_ALL, set())

    rows, untagged, unresolved = [], [], []
    for name in sorted(proj):
        ch = proj[name]; c, label, modid = classes[name]
        default = next((BY_CLASS_MAP[x] for x in ch if x in BY_CLASS_MAP), 'slow')
        ids = sorted(cand.get(name, ()))
        unverified = False
        if not ids and loose.get(name):
            ids = sorted(loose[name]); unverified = True
        if not ids:
            guess = (modid or 'minecraft') + ':' + snake(name.rsplit('/', 1)[-1].split('$')[-1]).replace('_entity', '')
            if guess in known: ids = [guess]
        groups = sorted({g for i in ids for g in member.get(i, [])}) + (['exempt(all)'] if any(i in exempt_all for i in ids) else [])
        row = {'class': name.replace('/', '.'), 'jar': label, 'ids': ids, 'default': default, 'tag': groups,
               'chain': [x.replace('/', '.') for x in ch[1:5]], 'unverified': unverified}
        rows.append(row)
        if not ids: unresolved.append(row)
        elif not groups: untagged.append(row)
    json.dump(rows, open(OUT, 'w'), indent=1)

    proj_ids = {i for r in rows for i in r['ids']}
    stray = sorted(i for i in member if i not in proj_ids)
    doubled = sorted(i for i, g in member.items() if len(g) > 1)
    differs = [r for r in rows if r['tag'] and r['tag'][0] != r['default'] and r['tag'][0] != 'exempt(all)']
    jars = collections.Counter(r['jar'] for r in rows)
    print('# projectiles: %d concrete classes in %d jars, %d with an entity id; tags from %s' % (
        len(rows), len(jars), len(rows) - len(unresolved), tags_path if os.path.relpath(tags_path, ROOT).startswith('..') else os.path.relpath(tags_path, ROOT)))
    print('# no group tag (base-class default applies): %d' % len(untagged))
    for r in untagged: print('  %s%s  %s  default=%s  (%s)' % (','.join(r['ids']), ' (id unverified)' if r['unverified'] else '',
                                                             r['class'], r['default'], r['jar']))
    print('# no entity id found (not registered, or registered by name the scan cannot pair): %d' % len(unresolved))
    for r in unresolved: print('  %s  default=%s  (%s)' % (r['class'], r['default'], r['jar']))
    print('# tagged group differs from the base-class default: %d' % len(differs))
    for r in differs: print('  %s  %s -> %s' % (','.join(r['ids']), r['default'], '+'.join(r['tag'])))
    if stray: print('# tagged ids that are not scanned projectiles: %d\n  %s' % (len(stray), ' '.join(stray)))
    if doubled: print('# ids in two groups: %d\n  %s' % (len(doubled), ' '.join('%s(%s)' % (i, '+'.join(member[i])) for i in doubled)))
    if show_all:
        for r in rows: print('%-50s %-8s %-14s %s' % (','.join(r['ids']) + ('?' if r['unverified'] else '') or '?',
                                                       r['default'], '+'.join(r['tag']) or '-', r['class']))
    print('wrote', os.path.relpath(OUT, ROOT))
    sys.exit(3 if untagged or doubled else 0)


if __name__ == '__main__':
    main(sys.argv[1:])
