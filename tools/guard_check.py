#!/usr/bin/env python3
"""Guard check (feature_writeup/plan_cb_publish.md M2, 3.3): every script row in Creative Brewing's chain jars that names
an id of an optional mod must be guarded, so the jars boot with an empty pack folder and only their hard dependencies.

A jar's required mods: its own mod ids and nested members', every mandatory dependency in those mods.toml files, and the
same for each Creative jar it depends on. Any other namespace is optional. A row naming an optional id is guarded by:
  G1  a `// requires: <mod>` header of the file (KubeJS and Creative Common's jar loader skip the file without it)
  G2  an enclosing block opened by a line with `global.has(...)` / `Platform.isLoaded(...)` naming that mod, an
      early `if (!global.has(...)) return` in the enclosing block, or the same check on the row's own line
  G3  dict keys (i.x, f.x, global.item.x ...) are resolved through the dicts themselves (Rhino, tools/dict_baseline)
G5  rows inside a call to global.NCH, global.tags or global.tooltips (or their aliases R, t, T, addTooltips ...) need
    no guard: the helpers skip ids of absent mods. A table that a loop later hands to them is marked `// G5` per row
    or `// G5 begin` / `// G5 end` around it.
Tags ('#mod:x') are not flagged: an empty tag of an absent mod is harmless, and NCH / tag_helper skip such rows (G5).
Own namespaces (creativeengineering, kubejs, ...) are not flagged either: several jars register under them, so the
dump cannot tell which jar an id comes from; NCH's absent-mod skip and the minimal-chain boot (SQ-CBS-01) cover them.
Also reports `// requires:` headers naming a mod id that no jar in mods/ provides (a folder that silently never loads).

Usage: python3 tools/guard_check.py [--jar creativebrewing] [--list] [--dicts <dump.json>]
Exit status 1 when anything is found. Reads mods/ (jar mod ids) and the dicts via tools/dict_baseline/run.sh.
"""
import argparse
import json
import os
import re
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths as P  # noqa: E402

ROOT = P.INSTANCE
DEV = ROOT / 'development'

# Creative jar -> (project folder, its script trees). Members' trees are under the project folder.
JARS = {
    'creativecommon': ('creativeCommon', ['kubejs/data/creativecommon/kubejs', 'kubejs/assets/creativecommon/kubejs']),
    'creativecompat': ('creativeCompat', ['kubejs/data/creativecompat/kubejs', 'kubejs/assets/creativecompat/kubejs']),
    'creativecalendar': ('creativeCalendar', ['kubejs/data/creativecalendar/kubejs']),
    'creativedelight': ('creativeDelight', ['kubejs/data/creativedelight/kubejs', 'kubejs/assets/creativedelight/kubejs']),
    'creativebrewing': ('creativeBrewing', ['kubejs/data/creativebrewing/kubejs', 'kubejs/assets/creativebrewing/kubejs']),
}
OWN_NAMESPACES = {'minecraft', 'forge', 'c', 'kubejs', 'creativeengineering', 'creativecommon', 'creativecompat',
                  'creativecalendar', 'creativedelight', 'creativebrewing', 'creativeequipment', 'creativemagic',
                  'creativeworldgen', 'creativedevtools', 'creative_labels'}
DICTS = {'item', 'fluid', 'blocks', 'effect', 'food', 'tool', 'misc'}
HELPERS = {'NCH', 'tags', 'tooltips'}
# `// G5` at the end of a row, or `// G5 begin` ... `// G5 end` around a table: the rows only ever reach the NCH, tags or
# tooltips helpers (a table a loop hands to them), so G5 covers them. Reviewed by hand; the checker trusts the marker.
G5_MARK = re.compile(r'//\s*G5(?:\s+(begin|end))?\b')
TOOLTIP_FUNCS = ['addTooltips', 'addTooltipPattern', 'removeTooltips', 'removeTooltipKey']

ID_RE = re.compile(r"""['"`]([a-z0-9_.\-]+):([a-z0-9_/.\-]+)['"`]""")
REQUIRES_RE = re.compile(r'^//\s*requires:\s*([a-z0-9_\-]+)', re.M)
ALIAS_RE = re.compile(r'\b(?:var|let|const)\s+([A-Za-z_$][\w$]*)\s*=\s*global\.(\w+)\s*[;\n,]')
GUARD_CALL_RE = re.compile(r'(?:global\.has|Platform\.isLoaded|has)\(([^()]*)\)')
EARLY_RETURN_RE = re.compile(r'if\s*\(\s*!\s*(?:global\.has|Platform\.isLoaded)\(([^()]*)\)\s*\)\s*return\b')


def toml_mods(path, props):
    """(mod ids, mandatory dependency ids) of one mods.toml, ${...} expanded from gradle.properties."""
    text = path.read_text(encoding='utf-8')
    text = re.sub(r'\$\{(\w+)\}', lambda m: props.get(m.group(1), m.group(0)), text)
    mods, deps = set(), set()
    section = None
    dep_id, mandatory = None, None
    for line in text.splitlines() + ['[[end]]']:
        s = line.split('#', 1)[0].strip()
        if s.startswith('[['):
            if section == 'dep' and dep_id and mandatory:
                deps.add(dep_id)
            section = 'mods' if s.startswith('[[mods]]') else ('dep' if s.startswith('[[dependencies') else None)
            dep_id, mandatory = None, None
            continue
        m = re.match(r'(modId|mandatory)\s*=\s*"?([^"\s]+)"?', s)
        if not m:
            continue
        if section == 'mods' and m.group(1) == 'modId':
            mods.add(m.group(2))
        elif section == 'dep':
            if m.group(1) == 'modId':
                dep_id = m.group(2)
            else:
                mandatory = m.group(2) == 'true'
    return mods, deps


def props_of(folder):
    out = {}
    f = folder / 'gradle.properties'
    if f.exists():
        for line in f.read_text(encoding='utf-8').splitlines():
            if '=' in line and not line.lstrip().startswith('#'):
                k, v = line.split('=', 1)
                out[k.strip()] = v.strip()
    return out


def project_mods(project):
    """(mod ids, mandatory deps) of a Creative project and its nested members."""
    base = DEV / project
    mods, deps = set(), set()
    folders = [base] + sorted(p for p in (base / 'members').glob('*') if p.is_dir()) if (base / 'members').is_dir() else [base]
    for folder in folders:
        toml = folder / 'src/main/resources/META-INF/mods.toml'
        if toml.exists():
            m, d = toml_mods(toml, props_of(folder))
            mods |= m
            deps |= d
    return mods, deps


def required_sets():
    info = {jar: project_mods(proj) for jar, (proj, _) in JARS.items()}
    req = {}

    def resolve(jar, seen):
        if jar in req:
            return req[jar]
        mods, deps = info[jar]
        out = set(mods) | set(deps)
        for d in deps:
            if d in info and d not in seen:
                out |= resolve(d, seen | {jar})
        req[jar] = out
        return out

    for jar in JARS:
        resolve(jar, set())
    return req


def all_mod_ids():
    """Every mod id in mods/ jars (nested Jar-in-Jar members included)."""
    ids = set()

    def scan(z):
        try:
            text = z.read('META-INF/mods.toml').decode('utf-8', 'replace')
            ids.update(re.findall(r'^\s*modId\s*=\s*"([^"]+)"', text, re.M))
        except KeyError:
            pass

    for jar in P.MODS.glob('*.jar'):
        try:
            with zipfile.ZipFile(jar) as z:
                scan(z)
                for name in z.namelist():
                    if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
                        import io
                        with zipfile.ZipFile(io.BytesIO(z.read(name))) as inner:
                            scan(inner)
        except zipfile.BadZipFile:
            continue
    return ids


def load_dicts(path):
    if path:
        return json.loads(Path(path).read_text())
    with tempfile.TemporaryDirectory() as tmp:
        out = os.path.join(tmp, 'dicts.json')
        subprocess.run([str(ROOT / 'tools/dict_baseline/run.sh'), '--dump=' + out, str(P.STARTUP_LIB.parent)],
                       check=True, stdout=subprocess.DEVNULL, env=dict(os.environ, TMPDIR=tmp))
        return json.loads(Path(out).read_text())


def strip_comment(line):
    """The line without a // comment (strings kept; a // inside a string is rare in these scripts)."""
    out, quote, k = [], None, 0
    while k < len(line):
        ch = line[k]
        if quote:
            out.append(ch)
            if ch == '\\' and k + 1 < len(line):
                out.append(line[k + 1])
                k += 1
            elif ch == quote:
                quote = None
        elif ch in '\'"`':
            quote = ch
            out.append(ch)
        elif line.startswith('//', k):
            break
        else:
            out.append(ch)
        k += 1
    return ''.join(out)


def namespaces_in(text, aliases, dicts):
    """Namespaces named on one line: literal ids and dict references (unresolved keys are skipped)."""
    found = set()
    for m in ID_RE.finditer(text):
        start = m.start()
        if start > 0 and text[start - 1] == '#':
            continue
        if start + 1 < len(text) and text[start + 1] == '#':
            continue
        found.add(m.group(1))
    for m in re.finditer(r'\b(?:global\.(\w+)|([A-Za-z_$][\w$]*))\.([A-Za-z_$][\w$]*)', text):
        dname = m.group(1) or aliases.get(m.group(2))
        if dname not in DICTS:
            continue
        val = (dicts.get(dname) or {}).get(m.group(3))
        if isinstance(val, str) and ':' in val and not val.startswith('#'):
            found.add(val.split(':', 1)[0])
    return found


def check_file(path, required, dicts, known_ids):
    src = path.read_text(encoding='utf-8', errors='replace')
    head = '\n'.join(src.splitlines()[:15])
    requires = set(REQUIRES_RE.findall(head))
    aliases = {m.group(1): m.group(2) for m in ALIAS_RE.finditer(src)}
    findings, bad_headers = [], []
    for r in requires:
        if known_ids and r not in known_ids:
            bad_headers.append(r)
    stack = [set()]  # guard mods per open brace
    # G5: rows passed to NCH, global.tags or global.tooltips need no guard (the helpers skip absent mods' ids).
    helper_names = [k for k, v in aliases.items() if v in HELPERS]
    helper_call = re.compile(r'(?:\b(?:%s)\.\w+|global\.(?:NCH|tags|tooltips)\.\w+|\b(?:%s))\s*$' % (
        '|'.join(map(re.escape, helper_names)) or '(?!x)x', '|'.join(TOOLTIP_FUNCS)))
    parens = []  # True per open paren that belongs to a helper call
    in_block_comment = False
    g5_region = False
    for n, raw in enumerate(src.splitlines(), 1):
        line = raw
        marker = G5_MARK.search(raw)
        if marker and marker.group(1) == 'begin':
            g5_region = True
        elif marker and marker.group(1) == 'end':
            g5_region = False
        g5_row = g5_region or bool(marker and not marker.group(1))
        if in_block_comment:
            if '*/' not in line:
                continue
            line = line.split('*/', 1)[1]
            in_block_comment = False
        if '/*' in line and '*/' not in line.split('/*', 1)[1]:
            line = line.split('/*', 1)[0]
            in_block_comment = True
        line = strip_comment(line)
        if not line.strip():
            continue
        line_guards = set()
        for g in GUARD_CALL_RE.finditer(line):
            line_guards |= namespaces_in(g.group(1), aliases, dicts) | set(re.findall(r"""['"]([a-z0-9_\-]+)['"]""", g.group(1)))
        early = EARLY_RETURN_RE.search(line)
        if early:
            stack[-1] |= line_guards
        active = set(requires).union(*stack) | line_guards
        in_helper = any(parens)
        quote = None
        for k, ch in enumerate(line):
            if quote:
                if ch == quote:
                    quote = None
                continue
            if ch in '\'"`':
                quote = ch
            elif ch == '(':
                is_helper = bool(helper_call.search(line[:k]))
                parens.append(is_helper)
                in_helper = in_helper or is_helper
            elif ch == ')' and parens:
                parens.pop()
        if in_helper or g5_row:
            continue
        for ns in sorted(namespaces_in(line, aliases, dicts)):
            if ns in OWN_NAMESPACES or ns in required or ns in active:
                continue
            findings.append((n, ns, raw.strip()[:140]))
        for ch in line:
            if ch == '{':
                stack.append(set(line_guards) if not early else set())
            elif ch == '}' and len(stack) > 1:
                stack.pop()
    return findings, bad_headers


# Data files that only list ids as strings (resolved by nothing at load): the dicts and the removed / merge lists.
DATA_ONLY = re.compile(r'kubejs_startup/creativecommon/_lib/')


def script_files(jar):
    project, trees = JARS[jar]
    roots = [ROOT / t for t in trees]
    base = DEV / project
    roots += [p for p in base.glob('src/main/resources/kubejs_startup')]
    roots += [p for p in base.glob('members/*/src/main/resources/kubejs_startup')]
    for r in roots:
        if r.is_dir():
            for f in sorted(r.rglob('*.js')):
                if not DATA_ONLY.search(str(f)):
                    yield f


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--jar', choices=sorted(JARS), action='append', help='only these jars (default: all five)')
    ap.add_argument('--list', action='store_true', help='print every finding, not only the per-file counts')
    ap.add_argument('--dicts', help='resolved dicts JSON (tools/dict_baseline/run.sh --dump=...); default: build it now')
    args = ap.parse_args()

    dicts = load_dicts(args.dicts)
    req = required_sets()
    known = all_mod_ids()
    total = 0
    for jar in args.jar or list(JARS):
        per_file = []
        for f in script_files(jar):
            findings, bad = check_file(f, req[jar], dicts, known)
            if findings or bad:
                per_file.append((f, findings, bad))
        n = sum(len(x[1]) + len(x[2]) for x in per_file)
        total += n
        print('%s: %d unguarded rows in %d files (required: %d mods)' % (jar, n, len(per_file), len(req[jar])))
        for f, findings, bad in per_file:
            mods = sorted({ns for _, ns, _ in findings})
            rel = f.relative_to(ROOT)
            print('  %s: %d (%s)%s' % (rel, len(findings), ', '.join(mods),
                                       ('; requires names no loaded jar: ' + ', '.join(bad)) if bad else ''))
            if args.list:
                for line, ns, text in findings:
                    print('    %d [%s] %s' % (line, ns, text))
    print('total: %d' % total)
    return 1 if total else 0


if __name__ == '__main__':
    sys.exit(main())
