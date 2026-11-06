"""Shared helpers for the tools/*.py generators and reports: instance paths, the item_dict parser, a lenient JSON
loader and the vanilla / Forge jar locator. Standard library only, so importing it is cheap.

    import pack_paths as P
    P.INSTANCE, P.MODS, P.ITEM_DICT, P.STARTUP_LIB   # STARTUP_LIB: the dicts' folder in Creative Common
    src, key_val, by_id = P.parse_item_dict()     # key -> value (last wins), item id -> [keys] (no '#tag' values)
    P.item_dict_first_keys()                      # value -> first key that assigns it
    P.loads_lenient(raw)                          # json with // and /* */ comments and trailing commas
    P.vanilla_jar(), P.forge_jar(), P.forge_mapped_jar()
    P.mod_jars('backportedDisplay-*.jar')       # that jar in mods/, or nested in a bundle (META-INF/jarjar/), newest last

Jar locations come from the instance: Minecraft and Forge versions from instance.json, the jars from the launcher's
shared libraries folder (the instance's ../../libraries, as AT Launcher lays it out). Nothing here pins a version.
"""
import glob
import json
import re
from functools import lru_cache
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
MODS = INSTANCE / 'mods'
# The dicts (global.item, food, fluid, blocks, ...) ship in Creative Common's jar since plan_cb_publish.md M1 (2026-11-04).
STARTUP_LIB = INSTANCE / 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib'
ITEM_DICT = STARTUP_LIB / 'item_dict.js'
GRADLE_CACHE = Path.home() / '.gradle/caches/forge_gradle'


NESTED_CACHE = INSTANCE / 'tools/.nested_jars'


def mod_jars(pattern, folder=None):
    """Jars matching a file glob: in mods/ (or `folder`) itself, else nested in a bundle jar there (Creative Delight,
    Creative Brewing: META-INF/jarjar/<name>). A nested jar is extracted once to tools/.nested_jars/ (git-ignored) and
    that path is returned, so callers can open it as any jar. Sorted by name, newest version last."""
    import fnmatch
    import zipfile
    folder = Path(folder) if folder else MODS
    plain = sorted(folder.glob(pattern))
    if plain:
        return plain
    out = []
    for bundle in sorted(folder.glob('*.jar')):
        try:
            with zipfile.ZipFile(bundle) as z:
                for n in z.namelist():
                    name = n.rsplit('/', 1)[-1]
                    if n.startswith('META-INF/jarjar/') and fnmatch.fnmatch(name, pattern):
                        dst = NESTED_CACHE / name
                        info = z.getinfo(n)
                        if not dst.is_file() or dst.stat().st_size != info.file_size or dst.stat().st_mtime < bundle.stat().st_mtime:
                            dst.parent.mkdir(parents=True, exist_ok=True)
                            dst.write_bytes(z.read(n))
                        out.append(dst)
        except (zipfile.BadZipFile, OSError):
            pass
    return sorted(out)


# ── item_dict.js ─────────────────────────────────────────────────────────────

# `i.key = 'value';` at any indent, single or double quotes. Aliases (`i.key = i.other;`) are not values and are skipped.
_ASSIGN = re.compile(r"""^[ \t]*i\.(\w+)[ \t]*=[ \t]*(?:'([^'\n]*)'|"([^"\n]*)")""", re.M)


def item_dict_text(path=None):
    return Path(path or ITEM_DICT).read_text(encoding='utf-8')


def item_dict_assignments(text=None):
    """[(key, value)] for every string assignment in item_dict.js (or the given text), in file order."""
    if text is None:
        text = item_dict_text()
    return [(m.group(1), m.group(2) if m.group(2) is not None else m.group(3)) for m in _ASSIGN.finditer(text)]


def parse_item_dict(path=None):
    """item_dict.js -> (source text, key -> value with the last assignment winning, item id -> [keys]).
    by_id leaves out tag values ('#...')."""
    src = item_dict_text(path)
    key_val = {}
    for k, v in item_dict_assignments(src):
        key_val[k] = v
    by_id = {}
    for k, v in key_val.items():
        if not v.startswith('#'):
            by_id.setdefault(v, []).append(k)
    return src, key_val, by_id


def item_dict_first_keys(text=None, tags=True, skip_keys=()):
    """value -> the first key that assigns it (file order). tags=False leaves out '#tag' values; empty values never count."""
    out = {}
    for k, v in item_dict_assignments(text):
        if v and k not in skip_keys and (tags or not v.startswith('#')):
            out.setdefault(v, k)
    return out


# ── lenient JSON ─────────────────────────────────────────────────────────────

_TRAILING_COMMA = re.compile(r',(\s*[}\]])')


def strip_json_comments(text):
    """Drop // and /* */ comments outside strings."""
    out, i, n, in_str = [], 0, len(text), False
    while i < n:
        c = text[i]
        if in_str:
            out.append(c)
            if c == '\\' and i + 1 < n:
                out.append(text[i + 1])
                i += 1
            elif c == '"':
                in_str = False
        elif c == '"':
            in_str = True
            out.append(c)
        elif text.startswith('//', i):
            while i < n and text[i] != '\n':
                i += 1
            continue
        elif text.startswith('/*', i):
            end = text.find('*/', i + 2)
            i = n if end < 0 else end + 2
            continue
        else:
            out.append(c)
        i += 1
    return ''.join(out)


def loads_lenient(raw):
    """json.loads that also accepts a BOM, // and /* */ comments and trailing commas, like Minecraft's lenient GSON
    reader (Beautify, Philip's Ruins and Terralith ship commented worldgen json). bytes or str."""
    text = raw.decode('utf-8-sig', 'replace') if isinstance(raw, bytes) else raw.lstrip('\ufeff')
    try:
        return json.loads(text)
    except ValueError:
        pass
    text = strip_json_comments(text)
    try:
        return json.loads(text)
    except ValueError:
        return json.loads(_TRAILING_COMMA.sub(r'\1', text))


# ── vanilla and Forge jars ───────────────────────────────────────────────────

@lru_cache(maxsize=None)
def instance_versions():
    """(minecraft version, forge version or None) from instance.json."""
    try:
        launcher = json.loads((INSTANCE / 'instance.json').read_text()).get('launcher', {})
    except (OSError, ValueError):
        launcher = {}
    loader = launcher.get('loaderVersion') or {}
    forge = loader.get('version') if loader.get('type', 'Forge') == 'Forge' else None
    return launcher.get('version') or '1.19.2', forge


MC_VERSION, FORGE_VERSION = instance_versions()


@lru_cache(maxsize=None)
def libraries_dir():
    """The launcher's shared libraries folder (AT Launcher: <launcher>/libraries next to instances/)."""
    for d in (INSTANCE.parent.parent / 'libraries', Path.home() / '.local/share/atlauncher/libraries'):
        if d.is_dir():
            return d
    return INSTANCE.parent.parent / 'libraries'


def _first(patterns):
    for p in patterns:
        hits = sorted(glob.glob(str(p)))
        if hits:
            return Path(hits[0])
    return None


@lru_cache(maxsize=None)
def vanilla_jar():
    """A vanilla client jar with the loose assets/ and data/ files, or None. First choice is the Forge installer's
    resources-only '-extra' jar in the libraries (no classes, so it scans fastest); then the full client jar there,
    the vanilla launcher's copy and the ForgeGradle cache copy. All hold the same resources."""
    mc = MC_VERSION
    client = libraries_dir() / 'net/minecraft/client'
    return _first([
        client / f'{mc}-*/client-{mc}-*-extra.jar',
        client / mc / f'client-{mc}.jar',
        Path.home() / f'.minecraft/versions/{mc}/{mc}.jar',
        GRADLE_CACHE / f'minecraft_repo/versions/{mc}/client-extra.jar',
    ])


def forge_jar(kind='universal'):
    """The instance's Forge jar from the libraries ('universal' holds Forge's assets and data, 'client' only patched
    classes), or None."""
    if not FORGE_VERSION:
        return None
    v = f'{MC_VERSION}-{FORGE_VERSION}'
    p = libraries_dir() / f'net/minecraftforge/forge/{v}/forge-{v}-{kind}.jar'
    return p if p.exists() else None


def forge_mapped_jar():
    """ForgeGradle's Mojmap-named Forge + Minecraft jar (classes for javap), or None. The instance's Forge version
    first; any other version ForgeGradle has built as a fallback (newest by name)."""
    mc = MC_VERSION
    base = GRADLE_CACHE / 'minecraft_user_repo/net/minecraftforge/forge'
    if FORGE_VERSION:
        v = f'{mc}-{FORGE_VERSION}_mapped_official_{mc}'
        p = base / v / f'forge-{v}.jar'
        if p.exists():
            return p
    hits = sorted(glob.glob(str(base / f'{mc}-*_mapped_official_{mc}/forge-{mc}-*_mapped_official_{mc}.jar')))
    return Path(hits[-1]) if hits else None
