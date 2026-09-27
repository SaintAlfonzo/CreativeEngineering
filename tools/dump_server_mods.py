#!/usr/bin/env python3
"""
Copies every server-side mod jar out of mods/ into server_mods/ (instance root), ready to drop
into the dedicated server's mods/ folder.

Which jars count as server-side comes from the table in mod_sides.md (instance root):
  side "both" or "server"  -> copied
  side "client"            -> skipped

Each jar is matched to its row by the first mod id in its META-INF/mods.toml (or fabric.mod.json),
so a mod updated to a new version needs no doc edit. Jars with no mod id (datapack-style jars,
kotlinforforge) are matched by exact file name instead.

Refuses to write anything if a jar in mods/ has no row in mod_sides.md, so a newly added mod
can't be silently left off the server - add a row and re-run. Rows whose mod is no longer
installed only produce a warning.

The output folder is cleared of *.jar before copying, so it always mirrors the current pack.

Run: python3 tools/dump_server_mods.py [--dry-run] [--out DIR]
"""
import argparse
import json
import os
import re
import shutil
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODS = os.path.join(ROOT, 'mods')
DOC = os.path.join(ROOT, 'mod_sides.md')
SERVER_SIDES = {'both', 'server'}
VALID_SIDES = SERVER_SIDES | {'client'}


def read_doc():
    """Returns (by_modid, by_jar) lookups of side, parsed from mod_sides.md's mod table."""
    by_modid, by_jar = {}, {}
    cols = None
    with open(DOC, encoding='utf8') as f:
        for line in f:
            line = line.strip()
            if not line.startswith('|'):
                cols = None
                continue
            cells = [c.strip() for c in line.strip('|').split('|')]
            if cols is None:
                # Header row of a table: only the one with Side/Mod id/Jar columns is the mod list.
                names = [c.lower() for c in cells]
                if {'side', 'mod id', 'jar'} <= set(names):
                    cols = {n: i for i, n in enumerate(names)}
                continue
            if set(line) <= set('|-: '):
                continue
            side = cells[cols['side']].lower()
            if side not in VALID_SIDES:
                sys.exit(f"mod_sides.md: unknown side '{side}' in row: {line}")
            modid = cells[cols['mod id']].strip('`')
            jar = cells[cols['jar']].strip('`')
            if modid and modid != '-':
                by_modid[modid] = side
            else:
                by_jar[jar] = side
    if not by_modid and not by_jar:
        sys.exit('mod_sides.md: no mod table found (needs Side, Mod id and Jar columns)')
    return by_modid, by_jar


def jar_modid(path):
    """First mod id declared by the jar, or '' for jars that declare none."""
    with zipfile.ZipFile(path) as z:
        names = z.namelist()
        if 'META-INF/mods.toml' in names:
            text = z.read('META-INF/mods.toml').decode('utf8', 'replace')
            # Covers both [[mods]] tables and the inline `mods = [{ modId = '...' }]` form
            # (lowcodefml datapack jars); dependency tables name other mods, so skip those.
            for block in re.split(r'\[\[\s*', text):
                if block.startswith('dependencies'):
                    continue
                m = re.search(r'''modId\s*=\s*["']([^"']+)["']''', block)
                if m:
                    return m.group(1)
        if 'fabric.mod.json' in names:
            try:
                return json.loads(z.read('fabric.mod.json'), strict=False).get('id', '')
            except ValueError:
                pass
    return ''


def main():
    ap = argparse.ArgumentParser(description='Copy server-side mod jars into an output folder.')
    ap.add_argument('--out', default=os.path.join(ROOT, 'server_mods'), help='output folder (default: server_mods/ in the instance root)')
    ap.add_argument('--dry-run', action='store_true', help='list what would be copied without writing anything')
    args = ap.parse_args()

    by_modid, by_jar = read_doc()
    copy, skip, unknown, seen_ids, seen_jars = [], [], [], set(), set()
    for jar in sorted(os.listdir(MODS), key=str.lower):
        path = os.path.join(MODS, jar)
        if not jar.endswith('.jar') or not os.path.isfile(path):
            continue
        modid = jar_modid(path)
        side = by_modid.get(modid) if modid else None
        if side is None:
            side = by_jar.get(jar)
        if side is None:
            unknown.append(f'{jar}  (mod id: {modid or "none"})')
            continue
        (seen_ids if modid in by_modid else seen_jars).add(modid if modid in by_modid else jar)
        (copy if side in SERVER_SIDES else skip).append(jar)

    if unknown:
        print('These jars have no row in mod_sides.md - add them, then re-run:')
        for u in unknown:
            print('  ' + u)
        sys.exit(1)

    stale = sorted(set(by_modid) - seen_ids) + sorted(set(by_jar) - seen_jars)
    if stale:
        print('Warning: mod_sides.md rows with no matching jar in mods/ (removed mods?): ' + ', '.join(stale))

    if args.dry_run:
        for jar in copy:
            print('copy  ' + jar)
        print(f'\n{len(copy)} server jars would be copied to {args.out}; {len(skip)} client-only skipped.')
        return

    os.makedirs(args.out, exist_ok=True)
    for old in os.listdir(args.out):
        if old.endswith('.jar'):
            os.remove(os.path.join(args.out, old))
    for jar in copy:
        shutil.copy2(os.path.join(MODS, jar), os.path.join(args.out, jar))
    print(f'Copied {len(copy)} server jars to {args.out}; skipped {len(skip)} client-only:')
    print('  ' + ', '.join(skip))


if __name__ == '__main__':
    main()
