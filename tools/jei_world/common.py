"""Shared helpers for tools/gen_jei_world.py modules (the world-interaction JEI tabs).

A module is tools/jei_world/tab_<name>.py with a `build(ctx)` that returns {tab_id: [row, ...]}. Rows use the
format documented in patcherMod compat/jei/world/WorldRow.java; build them with the small helpers below so the
JSON stays uniform. Tab ids are the ones in compat/jei/world/WorldTab.java.
"""
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODS = ROOT / 'mods'
KUBEJS_DATA = ROOT / 'kubejs' / 'data'
EXPORTED = ROOT / 'kubejs' / 'exported'
OUT = ROOT / 'kubejs' / 'assets' / 'creativeengineering' / 'jei_world'


class Context:
    """Read-only view of the pack's data: mod jars with kubejs/data on top, and the /dump* exports."""

    def __init__(self):
        self._index = None  # data path -> (jar Path or None for kubejs/data, entry name)
        self.warnings = []

    def warn(self, text):
        self.warnings.append(text)

    def _build_index(self):
        index = {}
        for jar in sorted(MODS.glob('*.jar')):
            try:
                with zipfile.ZipFile(jar) as z:
                    for name in z.namelist():
                        if name.startswith('data/') and name.endswith('.json'):
                            index.setdefault(name[len('data/'):], (jar, name))
            except zipfile.BadZipFile:
                self.warn(f'bad jar {jar.name}')
        if KUBEJS_DATA.is_dir():
            for f in KUBEJS_DATA.rglob('*.json'):
                index[f.relative_to(KUBEJS_DATA).as_posix()] = (None, f)
        self._index = index

    def data_files(self, subpath):
        """{'<ns>/<subpath>/<rest>.json': parsed json} for every data file under data/<ns>/<subpath>/, any namespace.

        kubejs/data wins over a jar with the same path (datapack order); between jars the first by name wins.
        """
        if self._index is None:
            self._build_index()
        out = {}
        for path, (jar, name) in sorted(self._index.items()):
            parts = path.split('/', 1)
            if len(parts) < 2 or not parts[1].startswith(subpath.rstrip('/') + '/'):
                continue
            try:
                if jar is None:
                    text = Path(name).read_text(encoding='utf-8')
                else:
                    with zipfile.ZipFile(jar) as z:
                        text = z.read(name).decode('utf-8')
                out[path] = json.loads(text)
            except (ValueError, KeyError, OSError) as e:
                self.warn(f'{path}: {e}')
        return out

    def exported(self, relpath):
        """Parsed kubejs/exported/<relpath>, or None when missing."""
        p = EXPORTED / relpath
        if not p.exists():
            self.warn(f'missing dump kubejs/exported/{relpath}')
            return None
        return json.loads(p.read_text(encoding='utf-8'))


# Row and ingredient builders -------------------------------------------------------------------------------

def item(id_, **extra):
    return {'item': id_, **extra}


def items(ids, **extra):
    return {'items': list(ids), **extra}


def tag(tag_id, **extra):
    return {'tag': tag_id.lstrip('#'), **extra}


def block(id_, **extra):
    return {'block': id_, **extra}


def blocks(ids, **extra):
    return {'blocks': list(ids), **extra}


def block_tag(tag_id, **extra):
    return {'block_tag': tag_id.lstrip('#'), **extra}


def fluid(id_, amount=1000, **extra):
    return {'fluid': id_, 'amount': amount, **extra}


def entity(id_, **extra):
    return {'entity': id_, **extra}


def row(inputs, outputs, action=None, notes=None, linked=False, requires=None):
    r = {}
    if requires:
        r['requires'] = sorted(set(requires))
    r['inputs'] = inputs
    if action:
        r['action'] = action
    r['outputs'] = outputs
    if notes:
        r['notes'] = notes
    if linked:
        r['linked'] = True
    return r


def mod_of(id_):
    """Namespace of an id or tag ('#ns:path' too); 'minecraft' when there is none."""
    s = id_.lstrip('#')
    return s.split(':', 1)[0] if ':' in s else 'minecraft'
