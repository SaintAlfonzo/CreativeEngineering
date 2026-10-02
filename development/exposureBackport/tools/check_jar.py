#!/usr/bin/env python3
"""Offline checks on the built exposureBackport jar (feature_writeup/exposure.md section 6).

    tools/check_jar.py [path/to/exposureBackport-*.jar]     (default: the newest jar in build/libs/)

Checks, each printed as PASS / FAIL with details:
  duplicates   no path appears twice in the jar
  merged       every addon tag / global_loot_modifiers.json that shares a path with src/main holds the union of both
               (build.gradle's processResources merge)
  models       every item registered in source has models/item/<id>.json and an en_us name (unused lang names: INFO)
  loot         every global loot modifier entry has its JSON, and every add_table target table exists
  shaders      every filter's shader file exists, in the jar or (minecraft: namespace) in the 1.19.2 client jar
Exit code 1 if any check fails.
"""
import glob
import json
import os
import re
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT = os.path.dirname(HERE)
NAMESPACES = ('exposure', 'exposure_polaroid', 'exposure_expanded', 'exposure_catalog')
ADDONS = ('polaroid', 'catalog', 'expanded')
CLIENT_JAR = os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2/client-extra.jar')

failed = False


def report(name, problems, ok_detail=''):
    global failed
    if problems:
        failed = True
        print(f'FAIL {name}: {len(problems)} problem(s)')
        for p in problems[:30]:
            print(f'     {p}')
    else:
        print(f'PASS {name}{": " + ok_detail if ok_detail else ""}')


def main():
    if len(sys.argv) > 1:
        jar_path = sys.argv[1]
    else:
        jars = sorted(glob.glob(os.path.join(PROJECT, 'build', 'libs', 'exposureBackport-*.jar')), key=os.path.getmtime)
        if not jars:
            sys.exit('no jar in build/libs; run ./gradlew :exposureBackport:build first')
        jar_path = jars[-1]
    print(f'jar: {os.path.relpath(jar_path)}')
    jar = zipfile.ZipFile(jar_path)
    names = [i.filename for i in jar.infolist()]
    files = set(n for n in names if not n.endswith('/'))

    def read_json(path):
        return json.loads(jar.read(path).decode('utf-8'))

    # duplicates
    seen, dupes = set(), []
    for n in names:
        if n in seen:
            dupes.append(n)
        seen.add(n)
    report('duplicates', dupes, f'{len(files)} files')

    # merged
    problems, merged = [], 0
    for addon in ADDONS:
        root = os.path.join(PROJECT, 'src', addon, 'resources')
        for path in glob.glob(os.path.join(root, '**', '*.json'), recursive=True):
            rel = os.path.relpath(path, root).replace(os.sep, '/')
            main_file = os.path.join(PROJECT, 'src', 'main', 'resources', rel)
            if not os.path.isfile(main_file):
                continue
            merged += 1
            in_jar = read_json(rel)
            for source in (main_file, path):
                src = json.load(open(source, encoding='utf-8'))
                for key in ('values', 'entries'):
                    missing = [v for v in src.get(key, []) if v not in in_jar.get(key, [])]
                    if missing:
                        problems.append(f'{rel}: {len(missing)} {key} from {os.path.relpath(source, PROJECT)} missing, e.g. {missing[0]}')
    report('merged', problems, f'{merged} shared files hold both sides')

    # models: every item registered in source (Register.item("id") / ITEMS.register("id")) has a model and a name
    problems, registered = [], []
    item_call = re.compile(r'(?:Register\.item|ITEMS\.register)\(\s*"([a-z0-9_]+)"')
    for ss, ns in (('main', 'exposure'), ('polaroid', 'exposure_polaroid'), ('expanded', 'exposure_expanded'), ('catalog', 'exposure_catalog')):
        for src in glob.glob(os.path.join(PROJECT, 'src', ss, 'java', '**', '*.java'), recursive=True):
            registered += [(ns, i) for i in item_call.findall(open(src, encoding='utf-8').read())]
    lang = {}
    for ns in NAMESPACES:
        if f'assets/{ns}/lang/en_us.json' in files:
            lang.update(read_json(f'assets/{ns}/lang/en_us.json'))
    for ns, item in registered:
        if f'assets/{ns}/models/item/{item}.json' not in files:
            problems.append(f'{ns}:{item} has no item model')
        if f'item.{ns}.{item}' not in lang and f'block.{ns}.{item}' not in lang:
            problems.append(f'{ns}:{item} has no en_us name')
    report('models', problems, f'{len(registered)} registered items')
    unused = sorted(k for k in lang if k.count('.') == 2 and k.startswith('item.')
                    and (k.split('.')[1], k.split('.')[2]) not in set(registered) and k.split('.')[1] in NAMESPACES)
    if unused:
        print(f'INFO {len(unused)} lang names for items that aren\'t registered (upstream leftovers): {", ".join(u.split(".", 1)[1] for u in unused)}')

    # loot
    problems = []
    glm = read_json('data/forge/loot_modifiers/global_loot_modifiers.json')
    for entry in glm['entries']:
        ns, _, path = entry.partition(':')
        mod_file = f'data/{ns}/loot_modifiers/{path}.json'
        if mod_file not in files:
            problems.append(f'{entry}: no {mod_file}')
            continue
        table = read_json(mod_file).get('table')
        if table:
            tns, _, tpath = table.partition(':')
            if f'data/{tns}/loot_tables/{tpath}.json' not in files:
                problems.append(f'{entry}: table {table} missing')
    report('loot', problems, f'{len(glm["entries"])} modifiers')

    # shaders
    problems, checked = [], 0
    client = zipfile.ZipFile(CLIENT_JAR) if os.path.exists(CLIENT_JAR) else None
    client_files = set(client.namelist()) if client else set()
    for name in sorted(files):
        if '/exposure/filter/' in name and name.startswith('data/') and name.endswith('.json'):
            shader = read_json(name).get('shader')
            if not shader:
                continue
            checked += 1
            sns, _, spath = shader.partition(':')
            target = f'assets/{sns}/{spath}'
            if target in files or target in client_files:
                continue
            if sns == 'minecraft' and client is None:
                problems.append(f'{name}: {shader} not in the jar and no client jar at {CLIENT_JAR} to check')
            else:
                problems.append(f'{name}: shader {shader} not found')
    report('shaders', problems, f'{checked} filters')

    sys.exit(1 if failed else 0)


if __name__ == '__main__':
    main()
