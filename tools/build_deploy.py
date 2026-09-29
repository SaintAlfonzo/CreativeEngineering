#!/usr/bin/env python3
"""
Stages a release of the pack in deploy/latest/ and records it in deploy/DEPLOY_LOG.md.

deploy/
  DEPLOY_LOG.md          one section per version, newest first: what changed and what to delete
  history/<version>.json snapshot of every deployed file (sha1) + mod sides; the next build diffs against it
  latest/                the payload to copy to the other machines (rebuilt from scratch every run)
    README.md            install steps for server and client, this version's changes, the removals
    mods/                every server jar (both + server) -> server mods/  AND  client mods/
    client_mods/         client-only jars                 -> client mods/ only
                         (the client always gets the full server mod set: it also runs single-player
                         worlds on its integrated server, so server-only mods must be there too)
    kubejs/ config/ defaultconfigs/                  -> both
    server/              server-root files: LaunchServer.sh/.bat, perf/ (always-on JFR recording)
                                                     -> the server folder itself (server only)
    resourcepacks/ shaderpacks/                      -> client; only new or changed packs (all with --full)
    removals_server.txt  removals_client.txt         paths (from the target's root) to delete, if any

What goes where:
  mods/*.jar               side from mod_sides.md (same lookup as tools/dump_server_mods.py). Jars in
                           sub-folders (mods/panorama/) are never shipped.
  kubejs/, config/,        every file .gitignore doesn't ignore, minus EXCLUDE (kubejs/archive, Forge's
  defaultconfigs/          *.toml.bak backups, ...), plus RUNTIME_INPUTS (ignored dumps that code reads,
                           e.g. the /dumpclimate samples the biome tuner needs at world creation)
  server/                  the tracked server/ folder, same rules; lands in the server's root folder
  configs_edited/server_config/*   merged into defaultconfigs/ so a new server world starts with the
                           hand-edited serverconfig values (configs_edited wins on a conflict)
  resourcepacks/, shaderpacks/     zips, folder packs (pack.mcmeta) and shader settings files

Removals are what the previous deployed version had at a path on that target and this one doesn't:
an updated jar's old file, a mod removed or moved to client-only (server side), a deleted script.
Copying a folder over never deletes anything, so apply them. From the target's game folder:
  xargs -d '\\n' rm -f < removals_server.txt

Preflight (errors stop the build unless --force; warnings go into the log entry):
  - every jar in mods/ has a mod_sides.md row                                    (error)
  - structure_config.json regenerated from the installed jars + kubejs/data matches the file, and
    gen_structure_groups.py sync would change nothing (both run on a scratch copy)   (error)
  - patcherMod: jar in mods/ equals the gradle build and no source is newer    (warning)
  - pack/manifest.json lists the installed mods                                  (warning)
  - uncommitted changes in shipped folders (recorded with the commit)            (warning)

Building the same version again replaces its log entry and snapshot (fix-ups before distributing);
changes are always counted from the newest *other* version in history/.

Run: python3 tools/build_deploy.py [--version X.Y.Z] [--notes TEXT] [--dry-run] [--full] [--force]
     (no --version: bumps the last version's patch number; the first release is 1.0.0)
"""
import argparse
import contextlib
import datetime
import fnmatch
import io
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from dump_server_mods import MODS, ROOT, SERVER_SIDES, jar_modid, read_doc  # noqa: E402
from gen_pack_manifest import enabled_resource_packs, sha1, version_of  # noqa: E402

DEPLOY = os.path.join(ROOT, 'deploy')
OUT = os.path.join(DEPLOY, 'latest')
HISTORY = os.path.join(DEPLOY, 'history')
LOG = os.path.join(DEPLOY, 'DEPLOY_LOG.md')
WORLDGEN = os.path.join(ROOT, 'kubejs', 'generation_scripts', 'worldgen')
SERVER_CONFIG_EDITS = os.path.join(ROOT, 'configs_edited', 'server_config')
JAR_ARCHIVE = os.path.join(ROOT, 'patcherMod', 'jar_archive')

# payload folder -> (targets, folder it goes to on the target; '' = the target's root). Everything the server
# gets, the client gets too: clients also host single-player worlds, so they need the server-only mods and
# server configs. The one exception is 'server': launch scripts and the perf/ recording setup of the
# dedicated server itself, meaningless in a client instance.
CATEGORIES = {
    'mods': (('server', 'client'), 'mods'),
    'client_mods': (('client',), 'mods'),
    'kubejs': (('server', 'client'), 'kubejs'),
    'config': (('server', 'client'), 'config'),
    'defaultconfigs': (('server', 'client'), 'defaultconfigs'),
    'resourcepacks': (('client',), 'resourcepacks'),
    'shaderpacks': (('client',), 'shaderpacks'),
    'server': (('server',), ''),
}
TREES = ('kubejs', 'config', 'defaultconfigs', 'server')
# Big, client-only and rarely changed: shipped only when new or changed (unless --full).
INCREMENTAL = ('resourcepacks', 'shaderpacks')
# Paths from the instance root, on top of what .gitignore already ignores.
EXCLUDE = [
    'kubejs/archive/*',        # old worldgen tuning, never loaded
    'kubejs/quest_working/*',  # notes
    '*.bak',                   # Forge's backups of rewritten configs
    '*.backup[0-9]*',          # Xaero's
    '*/__pycache__/*',
    '*.pyc',
]
# Git-ignored dump output that code reads at runtime, so it ships anyway.
RUNTIME_INPUTS = [
    'kubejs/exported/worldgen/climate/*.json',  # BiomeLayoutTuner's /dumpclimate samples: without them
                                                # biome multipliers are skipped when a world is created
]
LOG_HEADER = """# Deploy log

Every version staged by `tools/build_deploy.py`, newest first. Each entry lists what changed since the
version before it and which files the other machines must delete. The full file list of a version is in
`history/<version>.json`.
"""


class Preflight:
    def __init__(self):
        self.errors, self.warnings = [], []

    def error(self, msg):
        self.errors.append(msg)

    def warn(self, msg):
        self.warnings.append(msg)


# ── collecting the current pack ──────────────────────────────────────────────

def excluded(rel):
    return any(fnmatch.fnmatch(rel, pat) for pat in EXCLUDE)


def git(*args, stdin=None):
    try:
        r = subprocess.run(['git', '-C', ROOT, *args], input=stdin, capture_output=True, text=True)
    except FileNotFoundError:
        return None
    return r.stdout if r.returncode in (0, 1) else None


def walk(folder, root=ROOT):
    """Relative paths (from root) of every file under folder."""
    base = os.path.join(root, folder)
    out = []
    for d, dirs, files in os.walk(base):
        dirs.sort()
        for f in sorted(files):
            out.append(os.path.relpath(os.path.join(d, f), root).replace(os.sep, '/'))
    return out


def not_ignored(paths):
    ignored = git('check-ignore', '--stdin', stdin='\n'.join(paths))
    if ignored is None:
        return paths
    ignored = set(ignored.splitlines())
    return [p for p in paths if p not in ignored or any(fnmatch.fnmatch(p, pat) for pat in RUNTIME_INPUTS)]


def collect(pre, root=ROOT, unknown_side=None):
    """(files, mods): files = {category: {path inside the category: source path}},
    mods = {jar: {modId, side, name, version}}.

    root: the dev instance, or an installed game/server folder to record as a past version (then
    configs_edited isn't merged, and unknown_side is the side of jars mod_sides.md no longer lists)."""
    files = {c: {} for c in CATEGORIES}
    mods = {}
    by_modid, by_jar = read_doc()
    inst = json.load(open(os.path.join(ROOT, 'instance.json'), encoding='utf8'))
    by_file = {(m['type'], m['file']): m for m in inst['launcher']['mods']}
    unknown = []
    mods_dir = os.path.join(root, 'mods')
    for jar in sorted(os.listdir(mods_dir), key=str.lower):
        path = os.path.join(mods_dir, jar)
        if not jar.endswith('.jar') or not os.path.isfile(path):
            continue
        modid = jar_modid(path)
        side = (by_modid.get(modid) if modid else None) or by_jar.get(jar) or unknown_side
        if side is None:
            unknown.append(f'{jar} (mod id: {modid or "none"})')
            continue
        entry = by_file.get(('mods', jar))
        # hand-added jars (patcherMod) get an entry named after the file, version "Unknown"
        name = entry['name'] if entry and not entry['name'].endswith('.jar') else modid or os.path.splitext(jar)[0]
        version = version_of(entry) if entry else ''
        mods[jar] = {'modId': modid, 'side': side, 'name': name, 'version': '' if version == 'Unknown' else version}
        files['mods' if side in SERVER_SIDES else 'client_mods'][jar] = path
    if unknown:
        pre.error('jars with no row in mod_sides.md (add a row, see its "Keeping this up to date"):\n'
                  + '\n'.join('    ' + u for u in unknown))

    for tree in TREES:
        for rel in not_ignored(walk(tree, root)):
            if not excluded(rel):
                files[tree][rel[len(tree) + 1:]] = os.path.join(root, rel)
    if root == ROOT and os.path.isdir(SERVER_CONFIG_EDITS):
        for rel in walk(os.path.relpath(SERVER_CONFIG_EDITS, ROOT)):
            name = rel.split('/', 2)[2]
            src = os.path.join(ROOT, rel)
            have = files['defaultconfigs'].get(name)
            if have and sha1(have) != sha1(src):
                pre.warn(f'defaultconfigs/{name} differs from configs_edited/server_config/{name}; shipped the configs_edited copy')
            files['defaultconfigs'][name] = src

    rp = os.path.join(root, 'resourcepacks')
    for name in sorted(os.listdir(rp)) if os.path.isdir(rp) else []:
        path = os.path.join(rp, name)
        if os.path.isfile(path) and name.lower().endswith('.zip'):
            files['resourcepacks'][name] = path
        elif os.path.isfile(os.path.join(path, 'pack.mcmeta')):
            for rel in walk(f'resourcepacks/{name}', root):
                files['resourcepacks'][rel[len('resourcepacks/'):]] = os.path.join(root, rel)
    if os.path.isdir(os.path.join(root, 'shaderpacks')):
        for rel in walk('shaderpacks', root):
            files['shaderpacks'][rel[len('shaderpacks/'):]] = os.path.join(root, rel)
    return files, mods


# ── preflight ────────────────────────────────────────────────────────────────

def check_structures(pre):
    """Regenerate structure_config.json and sync structure_groups.json in a scratch copy of the
    worldgen folder; either one changing means the committed data is stale for the installed mods."""
    with tempfile.TemporaryDirectory() as tmp:
        wg = os.path.join(tmp, 'worldgen')
        shutil.copytree(WORLDGEN, wg)
        import gen_structure_config as gsc
        real = gsc.OUT_PATH
        gsc.OUT_PATH = type(real)(os.path.join(wg, 'structure_config.json'))
        log = io.StringIO()
        try:
            with contextlib.redirect_stdout(log):
                gsc.build()
        finally:
            gsc.OUT_PATH = real
        fresh = json.load(open(os.path.join(wg, 'structure_config.json'), encoding='utf8'))
        current = json.load(open(real, encoding='utf8'))
        if fresh.get('structure_sets') != current.get('structure_sets') or \
                fresh.get('meta_structures') != current.get('meta_structures'):
            new, old = set(fresh['structure_sets']), set(current.get('structure_sets', {}))
            detail = ''.join(f'\n    new set {s}' for s in sorted(new - old)) + \
                ''.join(f'\n    gone set {s}' for s in sorted(old - new))
            pre.error('structure_config.json is stale for the installed mods/kubejs data: run '
                      '`/cegen run worldgen` (or tools/gen_structure_config.py, then the rest of the '
                      'worldgen group)' + (detail or '\n    (same sets, changed members or biomes)'))
            return
        r = subprocess.run([sys.executable, os.path.join(ROOT, 'tools', 'gen_structure_groups.py'),
                            '--worldgen-dir', wg], capture_output=True, text=True, cwd=ROOT)
        if r.returncode != 0:
            pre.error('gen_structure_groups.py failed:\n    ' + (r.stderr or r.stdout).strip().replace('\n', '\n    '))
        elif 'already covers every structure' not in r.stdout:
            changes = [l for l in r.stdout.splitlines() if l.startswith('  ')]
            pre.error('structure_groups.json is out of sync (run `python3 tools/gen_structure_groups.py`, '
                      'then review the new rates):\n' + '\n'.join('  ' + c for c in changes))


def check_patchermod(pre):
    deployed = [j for j in os.listdir(MODS) if re.match(r'creativeengineering-.*\.jar$', j)]
    if len(deployed) != 1:
        pre.warn(f'expected one creativeengineering-*.jar in mods/, found {deployed or "none"}')
        return
    jar = os.path.join(MODS, deployed[0])
    built = os.path.join(ROOT, 'patcherMod', 'build', 'libs', deployed[0])
    if os.path.isfile(built) and sha1(built) != sha1(jar):
        pre.warn(f'mods/{deployed[0]} differs from patcherMod/build/libs/{deployed[0]} (built but not copied to mods/?)')
    src = os.path.join(ROOT, 'patcherMod', 'src')
    newest = max((os.path.getmtime(os.path.join(d, f)) for d, _, fs in os.walk(src) for f in fs), default=0)
    if newest > os.path.getmtime(jar):
        pre.warn(f'patcherMod/src has changes newer than mods/{deployed[0]} (rebuild + copy the jar?)')


def check_manifest(pre, files):
    try:
        manifest = json.load(open(os.path.join(ROOT, 'pack', 'manifest.json'), encoding='utf8'))
    except (OSError, ValueError):
        pre.warn('pack/manifest.json missing: run tools/gen_pack_manifest.py')
        return
    listed = {m['file']: m['sha1'] for m in manifest.get('mods', [])}
    jars = {**files['mods'], **files['client_mods']}
    if set(listed) != set(jars) or any(sha1(p) != listed[j] for j, p in jars.items()):
        pre.warn('pack/manifest.json is out of date with mods/: run tools/gen_pack_manifest.py and commit pack/')


def git_state(pre):
    head = (git('rev-parse', '--short', 'HEAD') or '').strip() or None
    dirty = (git('status', '--porcelain', '--', 'kubejs', 'config', 'defaultconfigs',
                 'configs_edited/server_config', 'mod_sides.md') or '').splitlines()
    dirty = sorted({l[3:].strip('"') for l in dirty})
    if dirty:
        pre.warn(f'{len(dirty)} uncommitted change(s) in shipped folders (commit {head} + working tree)')
    return head, dirty


# ── diffing against the last deployed version ────────────────────────────────

def parse_version(v):
    return tuple(int(x) for x in re.findall(r'\d+', v)) or (0,)


def load_history():
    out = []
    if os.path.isdir(HISTORY):
        for f in os.listdir(HISTORY):
            if f.endswith('.json'):
                out.append(json.load(open(os.path.join(HISTORY, f), encoding='utf8')))
    return sorted(out, key=lambda s: (s['date'], parse_version(s['version'])))


def installed(snapshot, target):
    """Every path (from the target's game folder) this snapshot puts on that target."""
    out = set()
    for cat, paths in snapshot['files'].items():
        targets, dest = CATEGORIES[cat]
        if target in targets:
            out.update(f'{dest}/{p}' if dest else p for p in paths)
    return out


def mod_key(jar, meta):
    return meta['modId'] or 'jar:' + jar


def diff(prev, cur):
    d = {'mods': {'added': [], 'removed': [], 'updated': [], 'sides': []}, 'files': {}, 'removals': {}}
    old = {mod_key(j, m): (j, m) for j, m in (prev or {}).get('mods', {}).items()}
    new = {mod_key(j, m): (j, m) for j, m in cur['mods'].items()}
    old_sha = {**(prev or {}).get('files', {}).get('mods', {}), **(prev or {}).get('files', {}).get('client_mods', {})}
    new_sha = {**cur['files']['mods'], **cur['files']['client_mods']}
    by_name = lambda k: ((new.get(k) or old[k])[1]['name'].lstrip("'[(").lower(), k)
    for k in sorted(new.keys() - old.keys(), key=by_name):
        d['mods']['added'].append(new[k])
    for k in sorted(old.keys() - new.keys(), key=by_name):
        d['mods']['removed'].append(old[k])
    for k in sorted(new.keys() & old.keys(), key=by_name):
        (oj, om), (nj, nm) = old[k], new[k]
        if oj != nj or old_sha.get(oj) != new_sha.get(nj):
            d['mods']['updated'].append((oj, om, nj, nm))
        if om['side'] != nm['side']:
            d['mods']['sides'].append((nj, om['side'], nm['side'], nm))
    for cat in CATEGORIES:
        if cat in ('mods', 'client_mods'):
            continue
        a = (prev or {}).get('files', {}).get(cat, {})
        b = cur['files'][cat]
        d['files'][cat] = {
            'added': sorted(b.keys() - a.keys()),
            'changed': sorted(p for p in b.keys() & a.keys() if a[p] != b[p]),
            'removed': sorted(a.keys() - b.keys()),
        }
    for target in ('server', 'client'):
        d['removals'][target] = sorted(installed(prev, target) - installed(cur, target)) if prev else []
    return d


# ── rendering ────────────────────────────────────────────────────────────────

def details(title, lines, open_limit=15):
    if not lines:
        return []
    if len(lines) <= open_limit:
        return [f'{title}:', ''] + lines + ['']
    return [f'<details><summary>{title}</summary>', ''] + lines + ['', '</details>', '']


def collapse(paths, limit=8):
    """File list, with folders holding more than `limit` of the paths shown as one line."""
    by_dir = {}
    for p in paths:
        by_dir.setdefault(os.path.dirname(p), []).append(p)
    out = []
    for dirname in sorted(by_dir):
        items = by_dir[dirname]
        if len(items) > limit:
            out.append(f'- `{dirname}/` ({len(items)} files)')
        else:
            out.extend(f'- `{p}`' for p in items)
    return out


def mod_label(meta):
    v = meta['version']
    # CurseForge's display name is often just the file name (shown anyway) or repeats the mod name
    if v.lower().startswith(meta['name'].lower()):
        v = v[len(meta['name']):].strip(' -_')
    return meta['name'] + (f' {v}' if v and not v.lower().endswith('.jar') else '')


def render_changes(cur, prev, d):
    m = d['mods']
    lines = []
    lines += details(f"Mods added ({len(m['added'])})",
                     [f"- {mod_label(meta)} `{j}` ({meta['side']})" for j, meta in m['added']])
    lines += details(f"Mods updated ({len(m['updated'])})",
                     [f"- {mod_label(nm)}: `{oj}` -> `{nj}`" for oj, om, nj, nm in m['updated']])
    lines += details(f"Mods removed ({len(m['removed'])})",
                     [f"- {mod_label(meta)} `{j}` (was {meta['side']})" for j, meta in m['removed']])
    lines += details(f"Mods whose side changed ({len(m['sides'])})",
                     [f"- {meta['name']} `{j}`: {a} -> {b}" for j, a, b, meta in m['sides']])
    for cat, ch in d['files'].items():
        n = sum(len(v) for v in ch.values())
        if not n:
            continue
        body = []
        for kind in ('added', 'changed', 'removed'):
            if ch[kind]:
                body += [f'*{kind} ({len(ch[kind])})*', ''] + collapse(ch[kind]) + ['']
        lines += details(f"{cat}/: {len(ch['added'])} added, {len(ch['changed'])} changed, {len(ch['removed'])} removed",
                         body[:-1], open_limit=25)
    if not lines:
        lines = ['No file changes since the previous version.', '']
    return lines


def render_removals(d):
    lines = []
    for target in ('server', 'client'):
        rm = d['removals'][target]
        if rm:
            lines += details(f'Delete on the {target} ({len(rm)}, also in removals_{target}.txt)',
                             [f'- `{p}`' for p in rm], open_limit=30)
    return lines or ['None.', '']


def render_entry(cur, prev, d):
    counts = (f"{len(cur['files']['mods'])} server+client mods, {len(cur['files']['client_mods'])} client-only mods, "
              f"{len(cur['files']['resourcepacks'])} resource pack files, {len(cur['files']['shaderpacks'])} shader pack files")
    commit = cur['commit'] or 'no git'
    if cur['dirty']:
        commit += f" + {len(cur['dirty'])} uncommitted file(s)"
    lines = [f"<!-- deploy:{cur['version']} -->",
             f"## {cur['version']} - {cur['date'][:16].replace('T', ' ')}", '',
             f"- Minecraft {cur['minecraft']}, {cur['loader']}",
             f"- Commit: {commit}",
             f"- Since: {prev['version'] if prev else 'nothing (first deployment: everything is new)'}",
             f"- Contents: {counts}"]
    if cur['notes']:
        lines.append(f"- Notes: {cur['notes']}")
    if cur['warnings']:
        lines.append('- Built with warnings:')
        lines += [f'  - {w.splitlines()[0]}' for w in cur['warnings']]
    lines += ['', '### Changes', ''] + render_changes(cur, prev, d)
    lines += ['### Removals', ''] + render_removals(d)
    return lines


def render_readme(cur, prev, d, shipped_incremental):
    rp_order = [p[len('file/'):] for p in cur['resourcePackOrder'] if p.startswith('file/')]
    lines = [f"# {cur['name']} {cur['version']}", '',
             f"Built {cur['date'][:16].replace('T', ' ')} from commit {cur['commit'] or '?'}"
             f"{' (with uncommitted changes)' if cur['dirty'] else ''}. "
             f"Minecraft {cur['minecraft']}, {cur['loader']}.", '',
             '## Server', '',
             '1. Stop the server and back up the world.',
             '2. Delete every path in `removals_server.txt` (from the server folder): '
             "`xargs -d '\\n' rm -f < removals_server.txt`" if d['removals']['server'] else
             '2. Nothing to delete.',
             '3. Copy `mods/` into the server\'s `mods/`. Do not copy `client_mods/`.',
             '4. Copy `kubejs/`, `config/` and `defaultconfigs/` over the server\'s folders.' +
             (' Copy the *contents* of `server/` into the server folder itself (`LaunchServer.sh`/`.bat`, '
              '`perf/`); if you changed MEMORY or JAVAPATH in the server\'s LaunchServer script, set them again.'
              if cur['files'].get('server') else ''),
             '5. Existing world only: `defaultconfigs/` is used only when a world has no serverconfig yet. '
             'Copy any changed `*-server.toml` from it into `<world>/serverconfig/` by hand.',
             '', '## Client', '',
             '1. Close the game.',
             '2. Delete every path in `removals_client.txt` (from the instance folder): '
             "`xargs -d '\\n' rm -f < removals_client.txt`" if d['removals']['client'] else
             '2. Nothing to delete.',
             '3. Copy `mods/` **and** `client_mods/` into the instance\'s `mods/`. `mods/` is the full server '
             'mod set, server-only mods included: the client needs them to host single-player worlds.',
             '4. Copy `kubejs/`, `config/`, `defaultconfigs/`' +
             (', `resourcepacks/`, `shaderpacks/`' if shipped_incremental else '') + ' over the instance\'s folders.',
             ]
    if shipped_incremental:
        lines.append('   `resourcepacks/` and `shaderpacks/` hold only packs that are new or changed in this version.')
    if rp_order:
        lines += ['5. Enable these resource packs, lowest priority first (1 ends up at the bottom of the '
                  "in-game Selected list, the last one at the top); packs not listed stay off:", '']
        lines += [f'   {i + 1}. {p}' for i, p in enumerate(rp_order)]
    lines += ['', '## Changes since ' + (prev['version'] if prev else 'nothing (first deployment)'), '']
    lines += render_changes(cur, prev, d)
    lines += ['## Removals', ''] + render_removals(d)
    return '\n'.join(lines) + '\n'


def write_log(cur, entry):
    text = open(LOG, encoding='utf8').read() if os.path.isfile(LOG) else LOG_HEADER
    parts = re.split(r'(?m)^(?=<!-- deploy:)', text)
    head, entries = parts[0], [p for p in parts[1:] if not p.startswith(f"<!-- deploy:{cur['version']} -->")]
    body = '\n'.join(entry).rstrip() + '\n\n'
    with open(LOG, 'w', encoding='utf8') as f:
        f.write(head.rstrip() + '\n\n' + body + ''.join(entries))


def archive_patchermod(files):
    """Store the patcherMod jar this version ships in patcherMod/jar_archive/ (git-ignored). The build
    only adds versions that aren't archived yet, so this is what keeps the released jar there."""
    for jar, src in files['mods'].items():
        if re.match(r'creativeengineering-.*\.jar$', jar):
            dst = os.path.join(JAR_ARCHIVE, jar)
            if not os.path.isfile(dst) or sha1(dst) != sha1(src):
                os.makedirs(JAR_ARCHIVE, exist_ok=True)
                shutil.copy2(src, dst)
                print(f'archived the shipped {jar} in {os.path.relpath(JAR_ARCHIVE, ROOT)}/')


# ── main ─────────────────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser(description='Stage a release of the pack in deploy/latest/ and log it.')
    ap.add_argument('--version', help='version to stage (default: last version with its patch number + 1)')
    ap.add_argument('--notes', default='', help='one line for the log entry')
    ap.add_argument('--dry-run', action='store_true', help='print the log entry; write and copy nothing')
    ap.add_argument('--full', action='store_true', help='ship every resource/shader pack, not just new or changed ones')
    ap.add_argument('--force', action='store_true', help='build even if a preflight check fails (recorded in the log)')
    args = ap.parse_args()

    pre = Preflight()
    files, mods = collect(pre)
    print('checking structure data (regenerating in a scratch folder)...')
    check_structures(pre)
    check_patchermod(pre)
    check_manifest(pre, files)
    commit, dirty = git_state(pre)

    history = load_history()
    last = history[-1]['version'] if history else None
    version = args.version or (re.sub(r'(\d+)(?!.*\d)', lambda m: str(int(m.group(1)) + 1), last) if last else '1.0.0')
    prev = next((s for s in reversed(history) if s['version'] != version), None)

    for w in pre.warnings:
        print('WARNING: ' + w)
    for e in pre.errors:
        print('ERROR: ' + e)
    if pre.errors and not args.force:
        sys.exit(f'{len(pre.errors)} preflight error(s): nothing written (--force to build anyway)')

    print(f'hashing {sum(len(v) for v in files.values())} files...')
    inst = json.load(open(os.path.join(ROOT, 'instance.json'), encoding='utf8'))['launcher']
    enabled, _ = enabled_resource_packs()
    cur = {
        'version': version,
        'date': datetime.datetime.now().isoformat(timespec='seconds'),
        'name': inst['name'],
        'minecraft': inst['version'],
        'loader': f"{inst['loaderVersion']['type']} {inst['loaderVersion']['version']}",
        'commit': commit,
        'dirty': dirty,
        'notes': args.notes,
        'warnings': pre.warnings + [f'FORCED past: {e}' for e in pre.errors],
        'resourcePackOrder': enabled,
        'mods': mods,
        'files': {cat: {p: sha1(src) for p, src in paths.items()} for cat, paths in files.items()},
    }
    d = diff(prev, cur)
    entry = render_entry(cur, prev, d)
    if args.dry_run:
        print('\n'.join(entry))
        print(f'(dry run: nothing written; would stage {version} in {os.path.relpath(OUT, ROOT)}/)')
        return

    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    shipped_incremental = 0
    for cat, paths in files.items():
        for rel, src in paths.items():
            if cat in INCREMENTAL and prev and not args.full and \
                    prev['files'].get(cat, {}).get(rel) == cur['files'][cat][rel]:
                continue
            dst = os.path.join(OUT, cat, rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(src, dst)
            shipped_incremental += cat in INCREMENTAL
    for target in ('server', 'client'):
        if d['removals'][target]:
            with open(os.path.join(OUT, f'removals_{target}.txt'), 'w', encoding='utf8') as f:
                f.write('\n'.join(d['removals'][target]) + '\n')
    with open(os.path.join(OUT, 'README.md'), 'w', encoding='utf8') as f:
        f.write(render_readme(cur, prev, d, shipped_incremental))
    with open(os.path.join(OUT, 'VERSION.txt'), 'w', encoding='utf8') as f:
        f.write(version + '\n')

    os.makedirs(HISTORY, exist_ok=True)
    with open(os.path.join(HISTORY, f'{version}.json'), 'w', encoding='utf8') as f:
        json.dump(cur, f, indent=1, ensure_ascii=False)
        f.write('\n')
    write_log(cur, entry)
    archive_patchermod(files)

    print(f"staged {version} in {os.path.relpath(OUT, ROOT)}/: {len(files['mods'])} mods, "
          f"{len(files['client_mods'])} client mods, {shipped_incremental} resource/shader pack files; "
          f"removals: {len(d['removals']['server'])} server, {len(d['removals']['client'])} client")
    print(f'logged in {os.path.relpath(LOG, ROOT)}')


if __name__ == '__main__':
    main()
