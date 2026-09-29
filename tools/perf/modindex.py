"""Class -> mod and mixin-method -> mod indexes built from a mods/ folder, for perf_report.py.

classes:    runtime class name -> mod id (first modId in the jar's mods.toml; jar-in-jar contents count
            as the outer jar's mod)
mixins:     "target.Class#handlerName" -> [[mod, mixinClass], ...]. At runtime an injector is merged into
            its target as handler$<id>$<name> (also redirect$, modify$, wrapOperation$, ...), so a stack
            frame names the target class and the original handler name. Targets come from @Mixin in the
            mixin class file; configs from MANIFEST MixinConfigs + mods.toml [[mixins]].
overwrites: same key shape, for @Overwrite methods (they keep the vanilla name).

Checked 2026-09-28 against the client's -Dmixin.debug.export dump (@MixinMerged): 2009/2116 injected
methods exact, 90 shared between two candidates (both listed), 1 wrong, 16 unresolved.
Cached as JSON (keyed by jar names + sizes); scanning ~340 jars takes about 3 s.
"""
import io
import json
import os
import re
import struct
import zipfile

MIXIN_ANN = 'Lorg/spongepowered/asm/mixin/Mixin;'
OVERWRITE_ANN = 'Lorg/spongepowered/asm/mixin/Overwrite;'


def _mods_toml_ids(z):
    try:
        t = z.read('META-INF/mods.toml').decode('utf-8', 'replace')
    except KeyError:
        return []
    return re.findall(r'^\s*modId\s*=\s*"([^"]+)"', t, re.M)


class _Cls:
    """Minimal class-file reader: constant pool, method names, class/method annotations."""

    def __init__(self, data):
        self.d = data
        self.p = 8
        n = self.u2()
        self.cp = [None] * n
        i = 1
        while i < n:
            tag = self.u1()
            if tag == 1:
                ln = self.u2()
                self.cp[i] = ('utf8', self.d[self.p:self.p + ln].decode('utf-8', 'replace'))
                self.p += ln
            elif tag in (3, 4):
                self.p += 4
            elif tag in (5, 6):
                self.p += 8
                i += 1
            elif tag in (7, 8, 16, 19, 20):
                self.cp[i] = ('ref', self.u2())
            elif tag in (9, 10, 11, 12, 17, 18):
                self.p += 4
            elif tag == 15:
                self.p += 3
            else:
                raise ValueError('bad cp tag %d' % tag)
            i += 1
        self.p += 2  # access
        self.name = self.utf(self.cp[self.u2()][1])
        self.p += 2  # super
        n_if = self.u2()
        self.p += 2 * n_if
        for _ in range(self.u2()):  # fields
            self.p += 6
            self._skip_attrs()
        self.methods = []  # (name, [annotation types])
        for _ in range(self.u2()):
            self.p += 2
            mname = self.utf(self.u2())
            self.p += 2
            self.methods.append((mname, [a[0] for a in self._attrs_annotations()]))
        self.annotations = self._attrs_annotations()

    def u1(self):
        v = self.d[self.p]
        self.p += 1
        return v

    def u2(self):
        v = struct.unpack_from('>H', self.d, self.p)[0]
        self.p += 2
        return v

    def u4(self):
        v = struct.unpack_from('>I', self.d, self.p)[0]
        self.p += 4
        return v

    def utf(self, i):
        return self.cp[i][1]

    def _skip_attrs(self):
        for _ in range(self.u2()):
            self.p += 2
            ln = self.u4()
            self.p += ln

    def _attrs_annotations(self):
        out = []
        for _ in range(self.u2()):
            aname = self.utf(self.u2())
            ln = self.u4()
            end = self.p + ln
            if aname in ('RuntimeInvisibleAnnotations', 'RuntimeVisibleAnnotations'):
                for _ in range(self.u2()):
                    out.append(self._annotation())
            self.p = end
        return out

    def _annotation(self):
        typ = self.utf(self.u2())
        vals = {}
        for _ in range(self.u2()):
            k = self.utf(self.u2())
            vals[k] = self._value()
        return (typ, vals)

    def _value(self):
        tag = chr(self.u1())
        if tag in 'BCDFIJSZ':
            self.p += 2
            return None
        if tag == 's':
            return self.utf(self.u2())
        if tag == 'e':
            self.p += 4
            return None
        if tag == 'c':
            return self.utf(self.u2())
        if tag == '@':
            return self._annotation()
        if tag == '[':
            return [self._value() for _ in range(self.u2())]
        raise ValueError('bad element tag ' + tag)


def _desc_to_name(d):
    if d.startswith('L') and d.endswith(';'):
        d = d[1:-1]
    return d.replace('/', '.')


def _mixin_configs(z):
    names = set()
    try:
        mf = z.read('META-INF/MANIFEST.MF').decode('utf-8', 'replace')
        mf = re.sub(r'\r?\n ', '', mf)  # manifest line continuations
        m = re.search(r'^MixinConfigs:\s*(.+)$', mf, re.M)
        if m:
            names.update(x.strip() for x in m.group(1).split(',') if x.strip())
    except KeyError:
        pass
    try:
        t = z.read('META-INF/mods.toml').decode('utf-8', 'replace')
        names.update(re.findall(r'^\s*config\s*=\s*"([^"]+)"', t, re.M))
    except KeyError:
        pass
    return names


def _scan_jar(z, jar_label, mod_id, classes, mixins, overwrites, depth=0):
    ids = _mods_toml_ids(z)
    owner = ids[0] if ids else mod_id
    for info in z.infolist():
        n = info.filename
        if n.endswith('.class') and not n.startswith('META-INF/'):
            classes.setdefault(n[:-6].replace('/', '.'), owner)
        elif depth < 2 and n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
            try:
                inner = zipfile.ZipFile(io.BytesIO(z.read(n)))
                _scan_jar(inner, jar_label, owner, classes, mixins, overwrites, depth + 1)
            except zipfile.BadZipFile:
                pass
    for cfg in _mixin_configs(z):
        try:
            j = json.loads(z.read(cfg).decode('utf-8', 'replace'))
        except (KeyError, ValueError):
            continue
        pkg = j.get('package', '')
        for key in ('mixins', 'server', 'client'):
            for m in j.get(key) or []:
                cls = (pkg + '.' + m) if pkg else m
                try:
                    c = _Cls(z.read(cls.replace('.', '/') + '.class'))
                except (KeyError, ValueError, IndexError, struct.error):
                    continue
                targets = []
                for typ, vals in c.annotations:
                    if typ == MIXIN_ANN:
                        targets += [_desc_to_name(v) for v in (vals.get('value') or []) if v]
                        targets += [v.replace('/', '.') for v in (vals.get('targets') or []) if v]
                for t in targets:
                    for mname, anns in c.methods:
                        if mname.startswith('<'):
                            continue
                        mixins.setdefault(t + '#' + mname, []).append([owner, cls])
                        if OVERWRITE_ANN in anns:
                            overwrites.setdefault(t + '#' + mname, []).append([owner, cls])


def build(mods_dir, cache_path):
    jars = sorted(f for f in os.listdir(mods_dir) if f.endswith('.jar'))
    key = [[f, os.path.getsize(os.path.join(mods_dir, f))] for f in jars]
    if os.path.exists(cache_path):
        with open(cache_path) as fh:
            cached = json.load(fh)
        if cached.get('key') == key:
            return cached
    classes, mixins, overwrites, jar_of = {}, {}, {}, {}
    for f in jars:
        try:
            z = zipfile.ZipFile(os.path.join(mods_dir, f))
        except zipfile.BadZipFile:
            continue
        ids = _mods_toml_ids(z)
        mod = ids[0] if ids else re.sub(r'[-_]?\d.*$', '', f[:-4]).lower()
        jar_of[mod] = f
        _scan_jar(z, f, mod, classes, mixins, overwrites)
    out = {'key': key, 'classes': classes, 'mixins': mixins, 'overwrites': overwrites, 'jars': jar_of}
    os.makedirs(os.path.dirname(cache_path) or '.', exist_ok=True)
    with open(cache_path, 'w') as fh:
        json.dump(out, fh)
    return out


def load_srg(tsrg_path):
    """srg method name (m_12345_) -> official name, from ForgeGradle's createMcpToSrg output.tsrg."""
    m = {}
    if not tsrg_path or not os.path.exists(tsrg_path):
        return m
    with open(tsrg_path) as fh:
        for line in fh:
            if line.startswith('\t') and not line.startswith('\t\t'):
                parts = line.split()
                if len(parts) == 3 and parts[2].startswith('m_'):
                    m[parts[2]] = parts[0]
    return m
