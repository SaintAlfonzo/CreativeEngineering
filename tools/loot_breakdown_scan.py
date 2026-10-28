#!/usr/bin/env python3
"""Template scan for loot_breakdown.py: one pass over every structure .nbt in the vanilla -extra jar,
mods/*.jar, disabledmods/*.jar (to name the origin of ported structures) and kubejs/data.

For each template it keeps
  containers  every block entity that holds items or a loot table: block, position, LootTable, Items
              (id, count, slot, tag as SNBT) and the block's own state properties
  jigsaw      the pool each jigsaw block points at (for the structure -> template graph)

The result is cached in tools/.cache/template_scan.json, keyed by the jar list and the kubejs/data
file list (name, size, mtime), because the scan reads ~5,600 templates (about 30 s).
"""
import glob
import gzip
import json
import os
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import audit_loot_containers as A  # noqa: E402

ROOT = Path(A.ROOT)
CACHE = ROOT / 'tools/.cache/template_scan.json'
VANILLA_JAR = A.VANILLA_JAR   # tools/pack_paths.py vanilla_jar()


def snbt(v):
    """Plain python NBT (as read_nbt returns it) -> short SNBT. Types are lost (b/s/L), values stay."""
    if isinstance(v, dict):
        return '{' + ','.join(f'{k}:{snbt(x)}' for k, x in v.items()) + '}'
    if isinstance(v, list):
        return '[' + ','.join(snbt(x) for x in v) + ']'
    if isinstance(v, str):
        return json.dumps(v, ensure_ascii=False)
    if isinstance(v, bytes):
        return f'<{len(v)} bytes>'
    return str(v)


def stack(item):
    out = {'id': item.get('id'), 'n': int(item.get('Count', 1))}
    if 'Slot' in item:
        out['slot'] = int(item['Slot'])
    if item.get('tag'):
        out['tag'] = snbt(item['tag'])
    return out


def read_template(raw):
    import io
    import struct
    try:
        return A.read_nbt(gzip.decompress(raw))
    except (OSError, ValueError, struct.error, EOFError):
        try:
            return A.read_nbt(raw)
        except (ValueError, struct.error, EOFError):
            return None


def scan_template(raw, origin, template):
    d = read_template(raw)
    if d is None:
        return None
    palette = d.get('palette') or (d.get('palettes') or [[]])[0]
    containers, jigsaw = [], []
    for b in d.get('blocks', []):
        nb = b.get('nbt')
        if not nb or b['state'] >= len(palette):
            continue
        p = palette[b['state']]
        block = p['Name']
        if block == 'minecraft:jigsaw':
            pool = nb.get('pool')
            if pool:
                jigsaw.append(pool)
            continue
        loot = nb.get('LootTable')
        items = nb.get('Items')
        if not isinstance(items, list):
            items = None
        items = [i for i in (items or []) if isinstance(i, dict)]
        if not (loot or items):
            continue
        row = {'block': block, 'pos': [int(x) for x in b['pos']], 'loot': loot,
               'items': [stack(i) for i in (items or [])]}
        if p.get('Properties'):
            row['props'] = p['Properties']
        containers.append(row)
    return {'origin': origin, 'containers': containers, 'jigsaw': sorted(set(jigsaw))}


def signature():
    jars = sorted(glob.glob(str(ROOT / 'mods/*.jar'))) + sorted(glob.glob(str(ROOT / 'disabledmods/**/*.jar'), recursive=True))
    sig = [[os.path.basename(j), os.path.getsize(j), int(os.path.getmtime(j))] for j in jars]
    kjs = sorted(glob.glob(str(ROOT / 'kubejs/data/*/structures/**/*.nbt'), recursive=True))
    sig += [[os.path.relpath(p, ROOT), os.path.getsize(p), int(os.path.getmtime(p))] for p in kjs]
    return sig


def scan(use_cache=True):
    """{template id: {'origin': jar | 'vanilla' | 'kubejs/data', 'containers': [...], 'jigsaw': [...]}}.
    Templates in disabledmods/ jars are NOT included (those mods are off); kubejs/data ones are."""
    sig = signature()
    if use_cache and CACHE.exists():
        cached = json.loads(CACHE.read_text())
        if cached.get('sig') == sig:
            return cached['templates']
    out = {}
    jars = [(j, os.path.basename(j)) for j in sorted(glob.glob(str(ROOT / 'mods/*.jar')))]
    if VANILLA_JAR and os.path.exists(VANILLA_JAR):
        jars.append((VANILLA_JAR, 'vanilla'))
    for path, label in jars:
        try:
            z = zipfile.ZipFile(path)
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            if not (n.startswith('data/') and '/structures/' in n and n.endswith('.nbt')):
                continue
            ns = n.split('/')[1]
            tid = f"{ns}:{n.split('/structures/', 1)[1][:-4]}"
            if tid.startswith(A.SKIP_TEMPLATES):
                continue
            r = scan_template(z.read(n), label, tid)
            if r is not None:
                out[tid] = r
    for path in sorted(glob.glob(str(ROOT / 'kubejs/data/*/structures/**/*.nbt'), recursive=True)):
        rel = os.path.relpath(path, ROOT / 'kubejs/data').split(os.sep)
        tid = f"{rel[0]}:{'/'.join(rel[2:])[:-4]}"
        r = scan_template(open(path, 'rb').read(), 'kubejs/data', tid)
        if r is not None:
            out[tid] = r
    CACHE.parent.mkdir(parents=True, exist_ok=True)
    CACHE.write_text(json.dumps({'sig': sig, 'templates': out}))
    return out


if __name__ == '__main__':
    t = scan(use_cache='--fresh' not in sys.argv)
    n_cont = sum(len(v['containers']) for v in t.values())
    print(f'{len(t)} templates, {n_cont} containers')
