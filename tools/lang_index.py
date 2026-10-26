#!/usr/bin/env python3
"""Offline index of every en_us lang key the client can see, and where it comes from.

Sources, lowest priority first (a later source wins for the same key, like the game's pack stack):
  1. vanilla 1.19.2 client jar (~/.minecraft/versions/1.19.2/1.19.2.jar) and the Forge client jar
  2. the upstream jars the ports read at runtime (<name>_upstream/*.jar), then every mod jar in mods/
     (assets/<ns>/lang/en_us.json) with the jars nested in it (META-INF/jarjar/, Creative Delight's members)
  3. KubeJS: kubejs/assets/<ns>/lang/en_us.json, kubejs/assets/*.zip, and the ClientEvents.lang calls
     in kubejs/client_scripts/*.js (e.add('key', 'Name'), e.renameItem / renameBlock('ns:id' or global.item.<key>, 'Name'))
  4. the resource-pack zips enabled in options.txt (they sit above the KubeJS pack, see
     memory resource-pack-lang-priority)

Not seen offline: lang generated at runtime (Moonlight/Everycomp/Immersive Weathering/Supplementaries
"Generated Pack"s, KubeJS builders' auto names). Callers treat those namespaces as unverified.

Used by tools/missing_names.py and tools/gen_display_delight_names.py.
"""
import io
import json
import re
import zipfile
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
MODS = INSTANCE / 'mods'
VANILLA_JAR = Path.home() / '.minecraft/versions/1.19.2/1.19.2.jar'
FORGE_JAR = Path.home() / '.local/share/atlauncher/libraries/net/minecraftforge/forge/1.19.2-43.3.9/forge-1.19.2-43.3.9-client.jar'
LANG_RE = re.compile(r'^assets/([^/]+)/lang/en_us\.json$')


def parse_lang(raw):
    """Lang json, tolerating a BOM, // comments and trailing commas (several mods ship those)."""
    text = raw.decode('utf-8-sig', errors='replace')
    try:
        return json.loads(text)
    except ValueError:
        text = re.sub(r'^\s*//.*$', '', text, flags=re.M)
        text = re.sub(r',(\s*[}\]])', r'\1', text)
        try:
            return json.loads(text)
        except ValueError:
            return {}


def mod_archives():
    """(label, path or file object) for every archive whose assets the game loads, lowest priority first."""
    for jar in sorted(INSTANCE.glob('*_upstream/*.jar')):
        yield jar.parent.name + '/' + jar.name, jar
    for jar in sorted(MODS.glob('*.jar')):
        yield jar.name, jar
        try:
            with zipfile.ZipFile(jar) as z:
                nested = [n for n in z.namelist() if n.startswith('META-INF/jarjar/') and n.endswith('.jar')]
                for n in nested:
                    yield jar.name + '!' + n.rsplit('/', 1)[1], io.BytesIO(z.read(n))
        except (zipfile.BadZipFile, OSError):
            pass


def zip_langs(path):
    """{key: value} from every assets/*/lang/en_us.json inside a jar or zip (a path or a file object)."""
    out = {}
    try:
        with zipfile.ZipFile(path) as z:
            for name in z.namelist():
                if LANG_RE.match(name):
                    data = parse_lang(z.read(name))
                    if isinstance(data, dict):
                        out.update({k: v for k, v in data.items() if isinstance(v, str)})
    except (zipfile.BadZipFile, OSError):
        pass
    return out


def enabled_pack_zips():
    """Resource-pack zips enabled in options.txt, in load order (last wins)."""
    opts = INSTANCE / 'options.txt'
    if not opts.exists():
        return []
    for line in opts.read_text(errors='replace').splitlines():
        if line.startswith('resourcePacks:'):
            packs = json.loads(line.split(':', 1)[1])
            return [INSTANCE / 'resourcepacks' / p[5:] for p in packs if p.startswith('file/')]
    return []


SCRIPT_ADD = re.compile(r"""\.add\(\s*['"]([^'"]+)['"]\s*,\s*['"]([^'"]*)['"]""")
SCRIPT_RENAME = re.compile(r"""\.rename(?:Item|Block)\(\s*['"]([a-z0-9_.\-]+):([a-z0-9_./\-]+)['"]\s*,\s*['"]([^'"]*)['"]""")
# renameItem(global.item.<key>, 'Name'): the id comes from item_dict.js
SCRIPT_RENAME_KEY = re.compile(r"""\.rename(?:Item|Block)\(\s*global\.item\.(\w+)\s*,\s*['"]([^'"]*)['"]""")
ITEM_DICT = INSTANCE / 'kubejs/startup_scripts/_lib/item_dict.js'


def script_langs():
    """Keys set by ClientEvents.lang in client scripts. renameItem uses the item's description id,
    which is block.* for a block item, so both forms are recorded."""
    out = {}
    item_ids = None
    for js in sorted((INSTANCE / 'kubejs/client_scripts').rglob('*.js')):
        text = js.read_text(errors='replace')
        if 'ClientEvents.lang' not in text:
            continue
        for key, val in SCRIPT_ADD.findall(text):
            out[key] = val
        renames = SCRIPT_RENAME.findall(text)
        for key, val in SCRIPT_RENAME_KEY.findall(text):
            if item_ids is None:
                item_ids = dict(re.findall(r"^\s*i\.(\w+)\s*=\s*'([a-z0-9_.\-]+:[a-z0-9_./\-]+)'",
                                           ITEM_DICT.read_text(errors='replace'), re.M))
            if key in item_ids:
                renames.append(tuple(item_ids[key].split(':', 1)) + (val,))
        for ns, path, val in renames:
            dotted = path.replace('/', '.')
            out['item.%s.%s' % (ns, dotted)] = val
            out['block.%s.%s' % (ns, dotted)] = val
    return out


def kubejs_asset_langs(skip=()):
    out = {}
    root = INSTANCE / 'kubejs/assets'
    for f in sorted(root.glob('*/lang/en_us.json')):
        if f.parent.parent.name in skip:
            continue
        data = parse_lang(f.read_bytes())
        if isinstance(data, dict):
            out.update({k: v for k, v in data.items() if isinstance(v, str)})
    for z in sorted(root.glob('*.zip')):
        out.update(zip_langs(z))
    return out


def build(skip_kubejs_ns=()):
    """Returns (names, source): names is {key: value} after priority, source is {key: label}.
    skip_kubejs_ns leaves out kubejs/assets/<ns>/lang files (a generator excludes its own output)."""
    names, source = {}, {}

    def put(d, label):
        for k, v in d.items():
            names[k] = v
            source[k] = label

    if VANILLA_JAR.exists():
        put(zip_langs(VANILLA_JAR), 'vanilla')
    if FORGE_JAR.exists():
        put(zip_langs(FORGE_JAR), 'forge')
    for label, jar in mod_archives():
        put(zip_langs(jar), label)
    put(kubejs_asset_langs(skip_kubejs_ns), 'kubejs/assets')
    put(script_langs(), 'kubejs/client_scripts')
    for z in enabled_pack_zips():
        if z.exists():
            put(zip_langs(z), 'resourcepacks/' + z.name)
    return names, source


def mod_namespaces():
    """Namespaces that exist at runtime: every assets/ or data/ top folder in the mod jars, plus
    minecraft/forge, plus the namespaces KubeJS registers into."""
    out = {'minecraft', 'forge', 'kubejs', 'creativeengineering'}
    for _, jar in mod_archives():
        try:
            with zipfile.ZipFile(jar) as z:
                for name in z.namelist():
                    parts = name.split('/')
                    if len(parts) > 2 and parts[0] in ('assets', 'data'):
                        out.add(parts[1])
        except (zipfile.BadZipFile, OSError):
            pass
    return out


if __name__ == '__main__':
    import sys
    names, source = build()
    for key in sys.argv[1:]:
        print('%s = %r  (%s)' % (key, names.get(key), source.get(key, 'missing')))
    if len(sys.argv) == 1:
        print('%d keys' % len(names))
