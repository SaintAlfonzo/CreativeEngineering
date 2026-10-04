#!/usr/bin/env python3
"""Audit every loot container in every structure template (mods/*.jar + vanilla + kubejs/data) against Lootr.

Writes kubejs/generation_scripts/worldgen/loot_container_audit.md: for each container block that
appears with a LootTable, whether Lootr converts it natively (lootr-common.toml), whether
loot_container_swaps.json swaps it to a vanilla container first, or whether it stays per-world
loot. Also lists containers with fixed contents and no loot table, which Lootr can never convert.

Static scan: containers whose loot table is set at placement time by code or a processor (YUNG's,
vanilla mineshafts...) show up as "empty" here, but Lootr converts those in game anyway.

Run after adding/updating a structure mod:  python3 tools/audit_loot_containers.py
"""
import collections, fnmatch, glob, gzip, io, json, os, re, struct, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'kubejs/generation_scripts/worldgen/loot_container_audit.md')
SWAPS = os.path.join(ROOT, 'kubejs/generation_scripts/worldgen/loot_container_swaps.json')
LOOTR = os.path.join(ROOT, 'config/lootr-common.toml')
VANILLA_JAR = os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2/client-extra.jar')
SKIP_TEMPLATES = ('create:gametest/',)
CONTAINER_HINT = re.compile(r'chest|barrel|shulker|crate|cabinet|basket|dropper|dispenser|hopper|monobank|safe|sack')


def read_nbt(buf):
    f = io.BytesIO(buf)
    def r(fmt):
        return struct.unpack(fmt, f.read(struct.calcsize(fmt)))[0]
    def rs():
        return f.read(r('>H')).decode('utf-8', 'replace')
    def payload(t):
        if t == 1: return r('>b')
        if t == 2: return r('>h')
        if t == 3: return r('>i')
        if t == 4: return r('>q')
        if t == 5: return r('>f')
        if t == 6: return r('>d')
        if t == 7: return f.read(r('>i'))
        if t == 8: return rs()
        if t == 9:
            et = r('>b'); n = r('>i'); return [payload(et) for _ in range(n)]
        if t == 10:
            d = {}
            while True:
                tt = r('>b')
                if tt == 0: return d
                name = rs(); d[name] = payload(tt)
        if t == 11: return [r('>i') for _ in range(r('>i'))]
        if t == 12: return [r('>q') for _ in range(r('>i'))]
        raise ValueError(f'bad tag {t}')
    t = r('>b'); rs()
    return payload(t)


def lootr_native():
    """Blocks Lootr converts by itself, from lootr-common.toml."""
    native = ['minecraft:chest', 'minecraft:trapped_chest', 'minecraft:barrel', 'minecraft:*shulker_box']
    try:
        text = open(LOOTR).read()
    except OSError:
        return native, '(lootr-common.toml not found, using Lootr defaults)'
    for key in ('additional_chests', 'additional_trapped_chests'):
        m = re.search(rf'^\s*{key}\s*=\s*\[(.*?)\]', text, re.M)
        if m:
            native += re.findall(r'"([^"]+)"', m.group(1))
    if re.search(r'^\s*convert_quark\s*=\s*true', text, re.M):
        native.append('quark:*_chest')
    note = ''
    if re.search(r'^\s*disable\s*=\s*true', text, re.M):
        note = '**lootr-common.toml has disable = true: Lootr converts nothing.**'
    return native, note


def load_swaps():
    try:
        with open(SWAPS) as fh:
            return json.load(fh).get('swaps', {})
    except (OSError, ValueError):
        return {}


def matches(block, patterns):
    return next((p for p in patterns if fnmatch.fnmatchcase(block, p)), None)


def scan_template(raw, jar, template, rows):
    try:
        d = read_nbt(gzip.decompress(raw))
    except (OSError, ValueError, struct.error, EOFError):
        try:
            d = read_nbt(raw)
        except (ValueError, struct.error, EOFError):
            print(f'  skipped unreadable {jar}:{template}', file=sys.stderr)
            return
    palette = d.get('palette') or (d.get('palettes') or [[]])[0]
    for b in d.get('blocks', []):
        nb = b.get('nbt')
        if not nb or b['state'] >= len(palette):
            continue
        block = palette[b['state']]['Name']
        if not (nb.get('LootTable') or 'Items' in nb):
            continue
        rows.append(dict(jar=jar, template=template, block=block, loot=nb.get('LootTable'),
                         items=len(nb.get('Items') or [])))


def scan():
    rows = []
    jars = sorted(glob.glob(os.path.join(ROOT, 'mods/*.jar')))
    if os.path.exists(VANILLA_JAR):
        jars.append(VANILLA_JAR)
    for j in jars:
        try:
            z = zipfile.ZipFile(j)
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            if not (n.startswith('data/') and '/structures/' in n and n.endswith('.nbt')):
                continue
            ns = n.split('/')[1]
            template = f"{ns}:{n.split('/structures/', 1)[1][:-4]}"
            if template.startswith(SKIP_TEMPLATES):
                continue
            scan_template(z.read(n), 'vanilla' if j == VANILLA_JAR else os.path.basename(j), template, rows)
    # structures ported out of jars into the KubeJS datapack (tools/port_structure_jars.py)
    for path in sorted(glob.glob(os.path.join(ROOT, 'kubejs/data/*/structures/**/*.nbt'), recursive=True)):
        rel = os.path.relpath(path, os.path.join(ROOT, 'kubejs/data')).split(os.sep)
        with open(path, 'rb') as fh:
            scan_template(fh.read(), 'kubejs/data', f"{rel[0]}:{'/'.join(rel[2:])[:-4]}", rows)
    return rows


def main():
    native, note = lootr_native()
    swaps = load_swaps()
    rows = scan()
    looted = [r for r in rows if r['loot']]
    fixed = [r for r in rows if not r['loot'] and r['items']]

    by_block = collections.defaultdict(list)
    for r in looted:
        by_block[r['block']].append(r)

    def status(block):
        if matches(block, native):
            return 'Lootr (native)'
        p = matches(block, list(swaps))
        if p:
            return f'swapped -> {swaps[p]} -> Lootr'
        return '**NOT converted**'

    L = ['# Loot container audit', '',
         'Generated by `python3 tools/audit_loot_containers.py`. Every container placed WITH a loot table by a structure '
         'template in `mods/*.jar` + vanilla, and what happens to it. Static scan: loot tables set at placement time by '
         'code or processors (YUNG\'s, mineshaft minecarts...) are not visible here; Lootr converts those in game anyway.', '']
    if note:
        L += [note, '']
    L += ['Lootr converts natively: ' + ', '.join(f'`{p}`' for p in native) + '. Swaps: `loot_container_swaps.json`.', '',
          '## Containers with a loot table', '',
          '| block | containers | templates | status | from |', '|---|---:|---:|---|---|']
    for block in sorted(by_block, key=lambda b: (status(b).startswith('Lootr'), -len(by_block[b]))):
        rs = by_block[block]
        jars = sorted({r['jar'] for r in rs})
        L.append(f"| `{block}` | {len(rs)} | {len({r['template'] for r in rs})} | {status(block)} | {', '.join(jars)} |")

    unconverted = [b for b in by_block if status(b) == '**NOT converted**']
    if unconverted:
        L += ['', '## Not converted: where they are', '',
              'Add a block to `loot_container_swaps.json` to have Lootr handle it (swapped to a vanilla chest/barrel first).', '']
        for block in sorted(unconverted):
            L.append(f'### `{block}`')
            L.append('')
            for t, n in sorted(collections.Counter(r['template'] for r in by_block[block]).items()):
                tables = sorted({r['loot'] for r in by_block[block] if r['template'] == t})
                L.append(f"- `{t}` x{n}: {', '.join(f'`{x}`' for x in tables)}")
            L.append('')

    fixed_by = collections.Counter((r['jar'], r['block']) for r in fixed)
    L += ['', '## Fixed contents, no loot table', '',
          'These hold hand-placed items instead of a loot table, so there is nothing for Lootr to make per-player. '
          'Mostly furnaces/dispensers/decor; the chests and barrels are the ones that act like loot.', '',
          '| from | block | containers |', '|---|---|---:|']
    for (jar, block), n in sorted(fixed_by.items(), key=lambda kv: (kv[0][0], -kv[1])):
        L.append(f'| {jar} | `{block}` | {n} |')

    with open(OUT, 'w') as fh:
        fh.write('\n'.join(L) + '\n')
    print(f'{len(looted)} loot containers ({len(unconverted)} unconverted block types), {len(fixed)} fixed-content containers -> {os.path.relpath(OUT, ROOT)}')


if __name__ == '__main__':
    main()
