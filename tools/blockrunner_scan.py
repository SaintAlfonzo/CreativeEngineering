#!/usr/bin/env python3
"""Whole-pack block scan for the BlockRunner tiers.

The /dumptags export only lists blocks that carry at least one tag. This adds the blocks that
don't (froglights, rubber blocks, catwalks...) by reading every block id the jars ship:
blockstates in mods/*.jar (and jar-in-jar), the vanilla client jar, and kubejs/assets. A modded
id only counts when an item with the same id exists (kubejs/exported/items_by_mod), because jars
also ship blockstates for optional-compat blocks that never get registered.

  python3 tools/blockrunner_scan.py
      writes tools/blockrunner_untagged_blocks.json (read by blockrunner_tag_sim.js)
  node tools/blockrunner_tag_sim.js /tmp/tiers.json && python3 tools/blockrunner_scan.py /tmp/tiers.json
      also prints every untiered block that could register, grouped by mod, colours and wood
      types collapsed ({c}, {w}); '*' marks blocks with no tags at all
  python3 tools/blockrunner_scan.py --audit kubejs/exported/surface_audit.json
      the watcher, from the in-game /dumpsurface (Creative Devtools): every walkable block (collision
      top >= the dump's registerHeight, has an item) with no tier, no json override and no native speed
      factor, grouped per mod; then the friction / jump / speed factor outliers, tiered or not.
      Exit 3 when the first list is not empty.

Re-run /dumptags and /kubejs export in game after adding mods.
"""
import glob, io, json, os, re, sys, zipfile, collections

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pack_paths  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'tools', 'blockrunner_untagged_blocks.json')
BS = re.compile(r'^assets/([a-z0-9_.-]+)/blockstates/([a-z0-9_/.-]+)\.json$')

def blockstate_ids(z, out):
    for n in z.namelist():
        m = BS.match(n)
        if m: out.add(m.group(1) + ':' + m.group(2))
        elif n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
            try: blockstate_ids(zipfile.ZipFile(io.BytesIO(z.read(n))), out)
            except zipfile.BadZipFile: pass

colors = r'(?<![a-z])(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black)(?![a-z])'
woods = (r'(?<![a-z])(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|crimson|warped|bamboo|azalea|blossom|ancient|twisted'
    r'|dreamwood|livingwood|rose|rubber|smoked|spirit|rubberwood|cherry)(?![a-z])')

def collapse(path):
    return re.sub(woods, '{w}', re.sub(colors, '{c}', path))

def print_groups(groups):
    for ns in sorted(groups, key=lambda n: (-sum(groups[n].values()), n)):
        print('## %s %d' % (ns, sum(groups[ns].values())))
        print(' '.join(p + ('x%d' % n if n > 1 else '') for p, n in groups[ns].items()))

def audit(path):
    dump = json.load(open(path))
    reg = dump.get('registerHeight', 0.2)
    untiered = collections.defaultdict(collections.Counter)
    outliers, errors = [], []
    for row in dump['blocks']:
        if 'error' in row:
            errors.append(row); continue
        ns, p = row['id'].split(':', 1)
        tier = row.get('tier')
        friction, jump, speed = row['friction'], row['jumpFactor'], row['speedFactor']
        if (row['collisionTop'] >= reg - 1e-6 and row['hasItem'] and not tier and not row['override']
                and abs(speed - 1) < 1e-6):
            untiered[ns][collapse(p)] += 1
        if abs(friction - 0.6) > 1e-6 or abs(jump - 1) > 1e-6 or abs(speed - 1) > 1e-6:
            outliers.append(row)
    total = sum(sum(c.values()) for c in untiered.values())
    print('# walkable, no tier: %d blocks in %d mods (dump %s)' % (total, len(untiered), dump.get('generated', '?')))
    print_groups(untiered)
    print('# outliers (friction != 0.6, jump != 1, speed != 1): %d' % len(outliers))
    for r in sorted(outliers, key=lambda r: r['id']):
        print('%s  tier=%s friction=%g jump=%g speed=%g top=%g' % (r['id'], r.get('tier') or (
            'override' if r['override'] else '-'), r['friction'], r['jumpFactor'], r['speedFactor'], r['collisionTop']))
    if errors:
        print('# errors: %d' % len(errors))
        for r in errors: print(r['id'], r['error'])
    sys.exit(3 if total else 0)

if len(sys.argv) > 2 and sys.argv[1] == '--audit':
    audit(sys.argv[2])
elif len(sys.argv) > 1 and sys.argv[1] == '--audit':
    sys.exit('usage: blockrunner_scan.py --audit kubejs/exported/surface_audit.json')

tagged = set()
for mod in json.load(open(os.path.join(ROOT, 'kubejs/exported/tags/block_tags_by_block.json'))).values():
    tagged.update(mod)
items = set()
for f in glob.glob(os.path.join(ROOT, 'kubejs/exported/items_by_mod/*.json')):
    items.update(json.load(open(f))['items'])

modded = set()
for jar in glob.glob(os.path.join(ROOT, 'mods/*.jar')):
    try: blockstate_ids(zipfile.ZipFile(jar), modded)
    except zipfile.BadZipFile: print('skipped unreadable jar', jar)
for f in glob.glob(os.path.join(ROOT, 'kubejs/assets/*/blockstates/*.json')):
    modded.add(f.split('/assets/')[1].split('/')[0] + ':' + os.path.basename(f)[:-5])
vanilla = set()
if pack_paths.vanilla_jar():
    blockstate_ids(zipfile.ZipFile(pack_paths.vanilla_jar()), vanilla)
if not vanilla: print('vanilla client jar not found: untagged vanilla blocks are missing')

untagged = sorted((vanilla | {b for b in modded if b in items}) - tagged)
json.dump(untagged, open(OUT, 'w'), indent=0)
print(len(tagged), 'tagged +', len(untagged), 'untagged registered blocks ->', os.path.relpath(OUT, ROOT))

if len(sys.argv) < 2: sys.exit()
tiered = {b for members in json.load(open(sys.argv[1])).values() for b in members}
# shapes that never register (BlockRunner reads ~0.2 below your feet) or are never walked on
thin = re.compile(r'(button|pressure_plate|_plate$|carpet|rail|torch|lantern|candle|sign|banner|flower|sapling|door|pane|bars'
    r'|ladder|vine|bush|fern|grass$|tall_|crop|seeds|stem$|_head$|skull|pot$|potted_|chain|lever|wire|tripwire|cobweb|fan$'
    r'|coral$|kelp$|seagrass|pickle|lily|roots$|sprouts|fungus|mushroom$|wart$|layer$|leaf_pile|petals|clover|rope|shelf'
    r'|frame$|post$|light|lamp|bulb|track|portal|fire$|air$|button|rod$|_nub$|decal|placard|screen|card_display'
    r'|auction_stand|bookshelf_trader|vending_machine|freezer|coinjar|bogey|wheel|ascii|duck|trophy|chair|stool|table)')
groups = collections.defaultdict(collections.Counter)
for b in sorted(tagged | set(untagged)):
    ns, path = b.split(':', 1)
    if b in tiered or thin.search(path): continue
    groups[ns][collapse(path) + ('*' if b in untagged else '')] += 1
print_groups(groups)
