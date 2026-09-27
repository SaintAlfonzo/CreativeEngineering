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

Re-run /dumptags and /kubejs export in game after adding mods.
"""
import glob, io, json, os, re, sys, zipfile, collections

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
for jar in glob.glob(os.path.join(ROOT, '../../libraries/net/minecraft/client/1.19.2/client-1.19.2.jar')):
    blockstate_ids(zipfile.ZipFile(jar), vanilla)
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
colors = r'(?<![a-z])(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black)(?![a-z])'
woods = (r'(?<![a-z])(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|crimson|warped|bamboo|azalea|blossom|ancient|twisted'
    r'|dreamwood|livingwood|rose|rubber|smoked|spirit|rubberwood|cherry)(?![a-z])')
groups = collections.defaultdict(collections.Counter)
for b in sorted(tagged | set(untagged)):
    ns, path = b.split(':', 1)
    if b in tiered or thin.search(path): continue
    groups[ns][re.sub(woods, '{w}', re.sub(colors, '{c}', path)) + ('*' if b in untagged else '')] += 1
for ns in sorted(groups, key=lambda n: -sum(groups[n].values())):
    print('## %s %d' % (ns, sum(groups[ns].values())))
    print(' '.join(p + ('x%d' % n if n > 1 else '') for p, n in groups[ns].items()))
