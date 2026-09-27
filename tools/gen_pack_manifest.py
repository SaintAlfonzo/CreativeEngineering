#!/usr/bin/env python3
"""
Writes pack/manifest.json and pack/MODLIST.md: everything needed to rebuild this instance's
mods/, resourcepacks/ and shaderpacks/ from their download sources, without committing the jars.

Sources of truth:
  mods/, resourcepacks/, shaderpacks/, disabledmods/  -> what is actually installed
  instance.json (ATLauncher)                          -> CurseForge / Modrinth ids, versions, loader
  mod_sides.md                                        -> client / server / both (same lookup as
                                                         tools/dump_server_mods.py)
  options.txt                                         -> enabled resource packs and their order

Every file gets its local sha1 so a rebuilt pack can be checked byte-for-byte. Files with no
CurseForge/Modrinth entry are marked source "local" (the in-tree patcherMod jar, hand-made packs).

Re-run after adding, removing or updating anything, then commit pack/.

Run: python3 tools/gen_pack_manifest.py
"""
import hashlib
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from dump_server_mods import ROOT, jar_modid, read_doc  # noqa: E402

OUT = os.path.join(ROOT, 'pack')
KINDS = {'mods': 'mods', 'resourcepack': 'resourcepacks', 'shaderpack': 'shaderpacks'}


def sha1(path):
    h = hashlib.sha1()
    with open(path, 'rb') as f:
        for chunk in iter(lambda: f.read(1 << 20), b''):
            h.update(chunk)
    return h.hexdigest()


def source_of(entry):
    """CurseForge/Modrinth coordinates + links for one instance.json mod entry."""
    src = {}
    cf_proj, cf_file = entry.get('curseForgeProject'), entry.get('curseForgeFile')
    if cf_proj and cf_file:
        page = (cf_proj.get('links') or {}).get('websiteUrl', '')
        src['curseforge'] = {
            'projectId': cf_proj['id'],
            'fileId': cf_file['id'],
            'slug': cf_proj.get('slug'),
            'url': f"{page}/files/{cf_file['id']}" if page else None,
        }
    mr_proj, mr_ver = entry.get('modrinthProject'), entry.get('modrinthVersion')
    if mr_proj and mr_ver:
        files = mr_ver.get('files') or []
        match = next((f for f in files if f.get('filename') == entry['file']), files[0] if files else {})
        src['modrinth'] = {
            'projectId': mr_proj['id'],
            'versionId': mr_ver['id'],
            'slug': mr_proj.get('slug'),
            'url': f"https://modrinth.com/{mr_proj.get('project_type', 'mod')}/{mr_proj.get('slug')}/version/{mr_ver['id']}",
            'download': match.get('url'),
        }
    return src


def version_of(entry):
    mr = entry.get('modrinthVersion') or {}
    cf = entry.get('curseForgeFile') or {}
    return mr.get('version_number') or cf.get('displayName') or entry.get('version') or ''


def list_files(folder, exts):
    path = os.path.join(ROOT, folder)
    if not os.path.isdir(path):
        return []
    return sorted(f for f in os.listdir(path)
                  if os.path.isfile(os.path.join(path, f)) and f.lower().endswith(exts))


def enabled_resource_packs():
    """(enabled list in load order, flagged-incompatible set) from options.txt."""
    enabled, incompatible = [], set()
    try:
        with open(os.path.join(ROOT, 'options.txt'), encoding='utf8') as f:
            for line in f:
                key, _, value = line.strip().partition(':')
                if key == 'resourcePacks':
                    enabled = json.loads(value)
                elif key == 'incompatibleResourcePacks':
                    incompatible = set(json.loads(value))
    except FileNotFoundError:
        pass
    return enabled, incompatible


def main():
    inst = json.load(open(os.path.join(ROOT, 'instance.json'), encoding='utf8'))
    launcher = inst['launcher']
    by_file = {(m['type'], m['file']): m for m in launcher['mods']}
    by_modid, by_jar = read_doc()

    def build(kind, folder, filename, extra=None):
        entry = by_file.get((kind, filename))
        rec = {
            'file': filename,
            'name': entry['name'] if entry else os.path.splitext(filename)[0],
            'version': version_of(entry) if entry else '',
            'sha1': sha1(os.path.join(ROOT, folder, filename)),
        }
        rec.update(extra or {})
        src = source_of(entry) if entry else {}
        rec['source'] = src or 'local'
        return rec

    mods, unsided = [], []
    for jar in list_files('mods', ('.jar',)):
        modid = jar_modid(os.path.join(ROOT, 'mods', jar))
        side = by_modid.get(modid) if modid else by_jar.get(jar)
        if side is None:
            unsided.append(jar)
        mods.append(build('mods', 'mods', jar, {'modId': modid, 'side': side}))

    disabled = [build('mods', 'disabledmods', jar, {'modId': jar_modid(os.path.join(ROOT, 'disabledmods', jar))})
                for jar in list_files('disabledmods', ('.jar', '.disabled'))]

    enabled, incompatible = enabled_resource_packs()
    order = {p[len('file/'):]: i for i, p in enumerate(enabled) if p.startswith('file/')}
    rpacks = [build('resourcepack', 'resourcepacks', f,
                    {'enabled': f in order, 'loadOrder': order.get(f),
                     'flaggedIncompatible': f'file/{f}' in incompatible})
              for f in list_files('resourcepacks', ('.zip',))]
    spacks = [build('shaderpack', 'shaderpacks', f) for f in list_files('shaderpacks', ('.zip',))]

    manifest = {
        'name': launcher['name'],
        'minecraft': launcher['version'],
        'loader': {'type': launcher['loaderVersion']['type'], 'version': launcher['loaderVersion']['version']},
        'java': inst.get('javaVersion'),
        'jvmArgs': launcher.get('javaArguments', ''),
        'resourcePackOrder': enabled,
        'mods': mods,
        'disabledMods': disabled,
        'resourcePacks': rpacks,
        'shaderPacks': spacks,
    }
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, 'manifest.json'), 'w', encoding='utf8') as f:
        json.dump(manifest, f, indent=2, ensure_ascii=False)
        f.write('\n')
    with open(os.path.join(OUT, 'MODLIST.md'), 'w', encoding='utf8') as f:
        f.write(render_md(manifest))

    local = [r['file'] for r in mods + rpacks + spacks if r['source'] == 'local']
    print(f"{len(mods)} mods, {len(disabled)} disabled, {len(rpacks)} resource packs, "
          f"{len(spacks)} shader packs -> {os.path.relpath(OUT, ROOT)}/")
    if local:
        print('no download source (source "local"):', ', '.join(local))
    if unsided:
        print('WARNING: no row in mod_sides.md:', ', '.join(unsided))


def link(rec):
    src = rec['source']
    if src == 'local':
        return 'local'
    parts = []
    if 'curseforge' in src and src['curseforge']['url']:
        parts.append(f"[CF]({src['curseforge']['url']})")
    if 'modrinth' in src:
        parts.append(f"[MR]({src['modrinth']['url']})")
    return ' '.join(parts)


def cell(text):
    return str(text or '').replace('|', '\\|')


def render_md(m):
    lines = [
        f"# {m['name']} - mod list",
        '',
        'Generated by `tools/gen_pack_manifest.py` from the installed files; do not edit by hand.',
        'Full data (ids, sha1, download urls) is in `manifest.json`.',
        '',
        f"- Minecraft **{m['minecraft']}**, {m['loader']['type']} **{m['loader']['version']}**",
        f"- Java {(m['java'] or {}).get('majorVersion', '?')}",
        f"- JVM args: `{m['jvmArgs']}`",
        '',
    ]
    counts = {}
    for r in m['mods']:
        side = r['side'] or 'unclassified'
        counts[side] = counts.get(side, 0) + 1
    lines += [f"## Mods ({len(m['mods'])}: " + ', '.join(f'{v} {k}' for k, v in sorted(counts.items())) + ')',
              '', '| Side | Mod | Mod id | Version | File | Source |', '|---|---|---|---|---|---|']
    for r in sorted(m['mods'], key=lambda r: r['name'].lstrip("'[").lower()):
        lines.append(f"| {r['side'] or '?'} | {cell(r['name'])} | `{r['modId'] or '-'}` | {cell(r['version'])} "
                     f"| `{cell(r['file'])}` | {link(r)} |")
    lines += ['', '## Resource packs', '',
              'Load order is bottom-to-top as in the in-game menu (higher number = higher priority).', '',
              '| Order | Pack | File | Source |', '|---|---|---|---|']
    for r in sorted(m['resourcePacks'], key=lambda r: (r['loadOrder'] is None, r['loadOrder'] or 0, r['file'])):
        order = r['loadOrder'] if r['enabled'] else 'off'
        lines.append(f"| {order} | {cell(r['name'])} | `{cell(r['file'])}` | {link(r)} |")
    lines += ['', 'Full enabled list (includes built-in / mod-provided packs):', '', '```']
    lines += m['resourcePackOrder'] + ['```', '', '## Shader packs', '', '| Pack | File | Source |', '|---|---|---|']
    for r in m['shaderPacks']:
        lines.append(f"| {cell(r['name'])} | `{cell(r['file'])}` | {link(r)} |")
    if m['disabledMods']:
        lines += ['', '## Disabled mods (in disabledmods/, not loaded)', '', '| Mod | File | Source |', '|---|---|---|']
        for r in m['disabledMods']:
            lines.append(f"| {cell(r['name'])} | `{cell(r['file'])}` | {link(r)} |")
    return '\n'.join(lines) + '\n'


if __name__ == '__main__':
    main()
