#!/usr/bin/env python3
"""Which mobs spawn in which biomes, how often, and under which conditions; plus the pack's spawn tuning.

    python3 tools/gen_spawn_table.py                  # writes feature_writeup/spawns_by_biome.md
    python3 tools/gen_spawn_table.py --mob minecraft:cow   # one mob's table on stdout (for other writeups)
    python3 tools/gen_spawn_table.py --refresh-vanilla-rules   # rebuild tools/spawn_rules_vanilla.json

Inputs:
- kubejs/exported/spawns.json (/dumpspawns): every biome's spawners as the running game had them, after every
  biome modifier, the pack's tune_spawns record (each changed biome's spawners before the rules), each mob's spawn
  placement, and each biome's tags.
- kubejs/generation_scripts/worldgen/mob_spawns.json: the pack's tuning rules. They are applied here to the
  untuned spawners with the same semantics as patcherMod worldgen/SpawnTuning, so the tables show the file as it
  is now, before a restart. When that differs from what the dump recorded, a WARNING says so (rejoin + /dumpspawns).
- kubejs/generation_scripts/worldgen/biome_traits.json: the biome trait table the rules' 'where' clauses use.
- kubejs/generation_scripts/worldgen/biome_config.json: post-tuning biome areas (share of each dimension).
- tools/spawn_rules_vanilla.json: vanilla's spawn rule method per mob, read from SpawnPlacements' bytecode in the
  ForgeGradle mapped jar (--refresh-vanilla-rules). Mods' rules can only be named from the dump.

"Share" = the mob's weight / the total weight of its category in that biome: the chance that a spawn attempt of
that category in that biome picks this mob. "World share" weights it by biome area: the share of that category's
spawn picks across the dimension that go to this mob, assuming equal attempts per area. It is not spawns per hour:
light, ground blocks, mob caps and the rule checks (section 6) decide whether a picked mob appears.

Exit code 1 when mob_spawns.json has rule errors, 2 when an input is missing.
"""
import argparse
import json
import math
import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths  # noqa: E402

INSTANCE = Path(__file__).resolve().parent.parent
SPAWNS = INSTANCE / 'kubejs/exported/spawns.json'
RULES = INSTANCE / 'kubejs/generation_scripts/worldgen/mob_spawns.json'
TRAITS = INSTANCE / 'kubejs/generation_scripts/worldgen/biome_traits.json'
BIOME_CONFIG = INSTANCE / 'kubejs/generation_scripts/worldgen/biome_config.json'
VANILLA_RULES = INSTANCE / 'tools/spawn_rules_vanilla.json'
OUT_MD = INSTANCE / 'feature_writeup/spawns_by_biome.md'

TAG_PREFIX = 'creativeengineering:mob_spawns/'
ACTIONS = ('set', 'scale', 'remove')
COND_PREFIX = 'creativeengineering:spawn_conditions/'
CONDITIONS = ('light_only', 'water_only')
CATEGORIES = ['monster', 'creature', 'ambient', 'axolotls', 'underground_water_creature', 'water_creature',
              'water_ambient', 'misc']
CAT_LABEL = {'monster': 'Monsters', 'creature': 'Creatures', 'ambient': 'Ambient', 'axolotls': 'Axolotls',
             'underground_water_creature': 'Underground water creatures', 'water_creature': 'Water creatures',
             'water_ambient': 'Water ambient (fish)', 'misc': 'Misc'}

# World-share columns: (label, dimension, layers). Layers come from biome_config.json.
REGIONS = [('Overworld', 'minecraft:overworld', ('surface',)),
           ('Caves', 'minecraft:overworld', ('underground',)),
           ('Nether', 'minecraft:the_nether', ('surface', 'underground')),
           ('End', 'minecraft:the_end', ('surface', 'underground'))]

# Biome-type columns of section 3: (label, trait clause as in mob_spawns.json 'where').
TYPE_COLUMNS = [('plains', 'plains'), ('forest', 'forest'), ('swamp', 'swamp'), ('jungle', 'jungle'),
                ('savanna', 'savanna'), ('desert', 'desert'), ('badlands', 'badlands'), ('mountain', 'mountain'),
                ('peak', 'peak'), ('snowy', 'snowy'), ('beach', 'beach'), ('river', 'river'), ('sky', 'sky'),
                ('flower', 'flower'), ('shrubland', 'shrubland')]

# What vanilla's spawn rule methods check (1.19.2 bytecode). Keys match spawn_rules_vanilla.json 'rule'.
RULE_NOTES = {
    'Animal.checkAnimalSpawnRules': 'block below in `#animals_spawnable_on` (grass), light above 8',
    'Monster.checkMonsterSpawnRules': 'not Peaceful, dark enough (block light 0), spawnable block below',
    'Monster.checkAnyLightMonsterSpawnRules': 'not Peaceful, spawnable block below, any light',
    'Mob.checkMobSpawnRules': 'spawnable block below, any light',
    'WaterAnimal.checkSurfaceWaterAnimalSpawnRules': 'in water from 13 below sea level up to sea level, water above',
    'PatrollingMonster.checkPatrollingMonsterSpawnRules': 'block light 8 or less (daylight is fine), not Peaceful, '
                                                          'spawnable block below',
    'MushroomCow.checkMushroomSpawnRules': 'block below in `#mooshrooms_spawnable_on` (mycelium), light above 8',
    'Wolf.checkWolfSpawnRules': 'block below in `#wolves_spawnable_on`, light above 8',
    'Rabbit.checkRabbitSpawnRules': 'block below in `#rabbits_spawnable_on`, light above 8',
    'Fox.checkFoxSpawnRules': 'block below in `#foxes_spawnable_on`, light above 8',
    'Goat.checkGoatSpawnRules': 'block below in `#goats_spawnable_on`, light above 8',
    'Parrot.checkParrotSpawnRules': 'block below in `#parrots_spawnable_on`, light above 8',
    'Frog.checkFrogSpawnRules': 'block below in `#frogs_spawnable_on`, light above 8',
    'Ocelot.checkOcelotSpawnRules': '2 in 3 attempts; its spawn obstruction check wants grass or leaves below, '
                                    'at or above sea level',
    'PolarBear.checkPolarBearSpawnRules': 'in `#polar_bears_spawn_on_alternate_blocks` biomes also on '
                                          '`#polar_bears_spawnable_on_alternate` (ice); otherwise as Animal',
    'Turtle.checkTurtleSpawnRules': 'on sand, below sea level + 4, light above 8',
    'Husk.checkHuskSpawnRules': 'as Monster, and sees the sky',
    'Stray.checkStraySpawnRules': 'as Monster, and sees the sky (powder snow above counts as open)',
    'Drowned.checkDrownedSpawnRules': 'in water, not Peaceful, dark; 1 in 15 in `#more_frequent_drowned_spawns` '
                                      '(rivers), else 1 in 40 and 5+ below sea level',
    'Guardian.checkGuardianSpawnRules': 'in water, not Peaceful; 1 in 20 where it sees the sky',
    'Axolotl.checkAxolotlSpawnRules': 'block below in `#axolotls_spawnable_on` (clay)',
    'TropicalFish.checkTropicalFishSpawnRules': 'as surface water animals; any height in '
                                                '`#allows_tropical_fish_spawns_at_any_height`',
    'GlowSquid.checkGlowSquideSpawnRules': 'in water 33+ blocks below sea level, total darkness',
    'Bat.checkBatSpawnRules': 'below sea level, light 4 or less (7 around Halloween)',
    'Endermite.checkEndermiteSpawnRules': 'as any-light monster, no player within 5 blocks',
    'Silverfish.checkSilverfishSpawnRules': 'as any-light monster, no player within 5 blocks',
    'Slime.checkSlimeSpawnRules': 'in `#allows_surface_slime_spawns` biomes y 51-69, more at full moon, dark; '
                                  'anywhere in slime chunks below y 40',
    'MagmaCube.checkMagmaCubeSpawnRules': 'not Peaceful, any light',
    'Ghast.checkGhastSpawnRules': 'not Peaceful, 1 in 20, spawnable block below',
    'Hoglin.checkHoglinSpawnRules': 'block below is not a nether wart block',
    'Piglin.checkPiglinSpawnRules': 'block below is not a nether wart block',
    'ZombifiedPiglin.checkZombifiedPiglinSpawnRules': 'not Peaceful, block below is not a nether wart block',
    'Strider.checkStriderSpawnRules': 'in lava, air above the lava',
}

# Mobs that come from somewhere other than a biome's spawner list (section 7 doesn't call them gaps).
OTHER_SOURCES = {
    'minecraft:phantom': 'insomnia spawner (players who haven\'t slept)',
    'minecraft:pillager': 'patrols (PatrolSpawner), outposts (structure spawns)',
    'minecraft:vindicator': 'raids, woodland mansions',
    'minecraft:evoker': 'raids, woodland mansions',
    'minecraft:ravager': 'raids',
    'minecraft:illusioner': 'none in survival',
    'minecraft:wandering_trader': 'WanderingTraderSpawner (pack: wandering_trader.md)',
    'minecraft:trader_llama': 'with the Wandering Trader',
    'minecraft:iron_golem': 'villages',
    'minecraft:villager': 'villages',
    'minecraft:zombie_villager': 'zombie villages, infection; 5% of zombie spawns',
    'minecraft:cat': 'villages, swamp huts (CatSpawner)',
    'minecraft:skeleton_horse': 'skeleton trap horses in thunderstorms',
    'minecraft:warden': 'sculk shriekers',
    'minecraft:guardian': 'ocean monuments (structure spawns)',
    'minecraft:elder_guardian': 'ocean monuments (placed)',
    'minecraft:blaze': 'Nether fortresses (structure spawns), spawners',
    'minecraft:wither_skeleton': 'Nether fortresses (structure spawns)',
    'minecraft:witch': 'swamp huts (structure spawns), raids',
    'minecraft:shulker': 'End cities (placed)',
    'minecraft:cave_spider': 'mineshaft spawners',
    'minecraft:silverfish': 'infested blocks, stronghold spawners',
    'minecraft:endermite': 'ender pearls',
    'minecraft:zombie_horse': 'none in survival',
    'minecraft:mule': 'breeding',
    'minecraft:allay': 'pillager outposts, woodland mansions (placed)',
    'minecraft:snow_golem': 'built by players',
    'minecraft:ender_dragon': 'the End',
    'minecraft:wither': 'built by players',
    'minecraft:giant': 'none in survival',
    'minecraft:tadpole': 'frogspawn',
    'mca:male_villager': 'MCA villages and villager replacement',
    'mca:female_villager': 'MCA villages and villager replacement',
    'vinery:wandering_winemaker': 'its own tick spawner (wandering_trader.md)',
}


# ── loading ─────────────────────────────────────────────────────────────────

def rel(path):
    try:
        return path.relative_to(INSTANCE)
    except ValueError:
        return path


def load_json(path, what):
    try:
        return json.loads(path.read_text())
    except FileNotFoundError:
        print(f'{rel(path)} not found: {what}', file=sys.stderr)
        sys.exit(2)


def load_traits():
    biomes = load_json(TRAITS, 'the biome trait table')['biomes']
    return {b: set(str(v).split()) for b, v in biomes.items()}


def load_areas():
    """biome -> (dimension, layer, pct of that dimension)."""
    cfg = load_json(BIOME_CONFIG, 'biome areas')
    out = {}
    for dim, dd in cfg['dimensions'].items():
        for b, info in dd['biomes'].items():
            out[b] = (dim, info.get('layer', 'surface'), info.get('result_pct', info.get('share_pct')) or 0.0)
    return out


def refresh_vanilla_rules():
    """Parse SpawnPlacements.<clinit> in the mapped Forge jar: EntityType.X, Type.Y, Heightmap.Z, then an
    invokedynamic whose bootstrap names the rule method, then register(...)."""
    jar = pack_paths.forge_mapped_jar()
    if not jar:
        sys.exit('no ForgeGradle mapped Forge jar under ~/.gradle/caches/forge_gradle (build patcherMod once)')
    text = subprocess.run(['javap', '-cp', str(jar), '-p', '-c', '-v', 'net.minecraft.world.entity.SpawnPlacements'],
                          capture_output=True, text=True, check=True).stdout
    clinit = text[text.index('  static {};'):]
    bsm_text = text[text.index('BootstrapMethods:'):]
    bsm = {}
    for m in re.finditer(r'^\s+(\d+): #\d+ REF_invokeStatic java/lang/invoke/LambdaMetafactory.*?\n(.*?)(?=^\s+\d+: #|\Z)',
                         bsm_text, re.M | re.S):
        t = re.search(r'REF_invokeStatic ([\w/$]+)\.([\w$]+):', m.group(2))
        if t:
            bsm[int(m.group(1))] = (t.group(1).replace('/', '.'), t.group(2))
    rules, cur = {}, []
    for line in clinit.splitlines():
        if m := re.search(r'Field net/minecraft/world/entity/EntityType\.(\w+):', line):
            cur = [m.group(1).lower()]
        elif (m := re.search(r'SpawnPlacements\$Type\.(\w+):', line)) and len(cur) == 1:
            cur.append(m.group(1))
        elif (m := re.search(r'Heightmap\$Types\.(\w+):', line)) and len(cur) == 2:
            cur.append(m.group(1))
        elif (m := re.search(r'InvokeDynamic #(\d+):test', line)) and len(cur) == 3:
            cls, meth = bsm[int(m.group(1))]
            rules['minecraft:' + cur[0]] = {'placement': cur[1], 'heightmap': cur[2],
                                            'rule': cls.rsplit('.', 1)[1] + '.' + meth, 'class': cls}
            cur = []
    VANILLA_RULES.write_text(json.dumps({'_source': jar.name + ' SpawnPlacements.<clinit>',
                                         'mobs': dict(sorted(rules.items()))}, indent=2) + '\n')
    print(f'{len(rules)} vanilla spawn rules -> {VANILLA_RULES.relative_to(INSTANCE)}')


# ── rules (same semantics as SpawnTuning.java / mob_spawns.js) ─────────────

def matches(traits, clause):
    for tok in str(clause).split():
        want = not tok.startswith('!')
        if (tok.lstrip('!') in traits) != want:
            return False
    return True


def resolve_rule(rule, traits, biome_tags):
    """(sorted biome ids from traits/ids, ['#tag', ...], ids incl. tag members)."""
    out = {b for b, tr in traits.items() if any(matches(tr, c) for c in rule.get('where', []))}
    tags = []
    for b in rule.get('add', []):
        if str(b).startswith('#'):
            if b not in tags:
                tags.append(b)
        else:
            out.add(b)
    for b in rule.get('except', []):
        out.discard(b)
    ids = sorted(out)
    full = set(ids)
    for t in tags:
        full |= {b for b, ts in biome_tags.items() if t[1:] in ts}
    return ids, tags, sorted(full)


def rule_error(rule, seen):
    rid = rule.get('id')
    if not rid or not re.fullmatch(r'[a-z0-9_]+', str(rid)):
        return 'id must be lowercase a-z 0-9 _'
    if rid in seen:
        return 'duplicate id'
    if ':' not in str(rule.get('mob', '')):
        return 'mob must be an entity id'
    a = rule.get('action')
    if a not in ACTIONS:
        return 'action must be set, scale or remove'
    num = lambda v: isinstance(v, (int, float)) and not isinstance(v, bool)
    if a == 'set' and not (num(rule.get('weight')) and rule['weight'] >= 0):
        return 'set needs a weight >= 0'
    if a == 'scale' and not (num(rule.get('factor')) and rule['factor'] >= 0):
        return 'scale needs a factor >= 0'
    if rule.get('category') is not None and rule['category'] not in CATEGORIES:
        return 'unknown category ' + str(rule['category'])
    if rule.get('min') is not None and not rule['min'] >= 1:
        return 'min must be 1 or more'
    if rule.get('max') is not None and not rule['max'] >= (rule.get('min') or 1):
        return 'max must be >= min'
    if rule.get('brown_chance') is not None and not (num(rule['brown_chance']) and 0 <= rule['brown_chance'] <= 1):
        return 'brown_chance must be 0 to 1'
    if rule.get('brown_chance') is not None and rule.get('mob') != 'minecraft:mooshroom':
        return 'brown_chance is for minecraft:mooshroom only'
    return None


def describe(e):
    return f"{e['weight']} ({e['min']}-{e['max']})"


def apply_rule(spawners, rule, mob_category):
    """Mutates spawners {cat: [entry]}; returns a change line or None. Mirrors SpawnTuning.set/scale/remove."""
    mob = rule['mob']

    def remove():
        gone = []
        for cat in CATEGORIES:
            lst = spawners.get(cat, [])
            hit = [e for e in lst if e['type'] == mob]
            if hit:
                gone.append(cat + ' ' + ' + '.join(describe(e) for e in hit))
                spawners[cat] = [e for e in lst if e['type'] != mob]
        return 'removed ' + ', '.join(gone) if gone else None

    if rule['action'] == 'remove' or (rule['action'] == 'set' and rule['weight'] <= 0) \
            or (rule['action'] == 'scale' and rule['factor'] <= 0):
        return remove()
    if rule['action'] == 'scale':
        parts = []
        for cat in CATEGORIES:
            for i, e in enumerate(spawners.get(cat, [])):
                if e['type'] != mob:
                    continue
                nw = max(1, math.floor(e['weight'] * rule['factor'] + 0.5))
                if nw != e['weight']:
                    parts.append(f"{e['weight']} -> {nw}")
                    spawners[cat][i] = dict(e, weight=nw)
        return f"scaled x{float(rule['factor'])}: " + ', '.join(parts) if parts else None
    # set
    first, home, at, old = None, None, -1, []
    for cat in CATEGORIES:
        for i, e in enumerate(spawners.get(cat, [])):
            if e['type'] == mob:
                if first is None:
                    first, home, at = e, cat, i
                old.append(describe(e))
    mn = rule.get('min') if rule.get('min') is not None else (first['min'] if first else 1)
    mx = max(mn, rule.get('max') if rule.get('max') is not None else (first['max'] if first else mn))
    new = {'type': mob, 'weight': int(rule['weight']), 'min': int(mn), 'max': int(mx)}
    if first is None:
        cat = rule.get('category') or mob_category or 'creature'
        spawners.setdefault(cat, []).append(new)
        return f'added {describe(new)} to {cat}'
    for cat in CATEGORIES:
        if cat in spawners:
            spawners[cat] = [e for e in spawners[cat] if e['type'] != mob]
    lst = spawners.setdefault(home, [])
    lst.insert(min(at, len(lst)), new)
    if len(old) == 1 and old[0] == describe(new):
        return None
    return 'set ' + ' + '.join(old) + ' -> ' + describe(new)


def strip(sp):
    return {c: [{k: e[k] for k in ('type', 'weight', 'min', 'max')} for e in lst] for c, lst in sp.items() if lst}


def build(dump, cfg, traits):
    """Apply mob_spawns.json to the untuned spawners. Returns (tuned, untuned, rule_rows, problems, warnings, stale)."""
    biome_tags = {b: set(v.get('tags', [])) for b, v in dump['biomes'].items()}
    rec = dump.get('tuning', {}).get('biomes', {})
    untuned = {}
    for b, v in dump['biomes'].items():
        if b in rec:
            sp = defaultdict(list)
            for e in rec[b]['before']:
                sp[e['category']].append({k: e[k] for k in ('type', 'weight', 'min', 'max')})
            untuned[b] = strip(sp)
        else:
            untuned[b] = strip(v.get('spawners', {}))
    tuned = {b: {c: [dict(e) for e in lst] for c, lst in sp.items()} for b, sp in untuned.items()}
    entities = dump.get('entities', {})
    problems, warnings, rows, seen, vocab = [], [], [], set(), set().union(*traits.values())
    namespaces = {'minecraft', 'creativeengineering'} | {m.split(':')[0] for m in entities}
    applied = []      # rule ids mob_spawns.js puts in the modifier
    changes = defaultdict(list)
    for n, rule in enumerate(cfg.get('rules', []), 1):
        err = rule_error(rule, seen)
        if err:
            problems.append(f"rule {n} ({rule.get('id', 'no id')}): {err}")
            continue
        seen.add(rule['id'])
        for clause in rule.get('where', []):
            for tok in str(clause).split():
                if tok.lstrip('!') not in vocab:
                    warnings.append(f"{rule['id']}: trait \"{tok.lstrip('!')}\" is on no biome (typo?)")
        for b in rule.get('except', []):
            if str(b).startswith('#'):
                warnings.append(f"{rule['id']}: except takes biome ids, not tags ({b} ignored)")
        ids, tags, full = resolve_rule(rule, traits, biome_tags)
        for b in ids:
            if b not in dump['biomes']:
                warnings.append(f"{rule['id']}: no such biome {b}")
        for t in tags:
            if not any(t[1:] in ts for ts in biome_tags.values()):
                warnings.append(f"{rule['id']}: tag {t} has no biomes in the dump")
        if rule['mob'].split(':')[0] not in namespaces:
            warnings.append(f"{rule['id']}: mod not loaded, skipped {rule['mob']}")
            rows.append((rule, [], 0))
            continue
        if not ids and not tags:
            warnings.append(f"{rule['id']}: matches no biome, skipped")
            rows.append((rule, [], 0))
            continue
        applied.append(rule['id'])
        if rule['mob'] not in entities:
            warnings.append(f"{rule['id']}: {rule['mob']} is not in the dump (typo, or a mob with no spawn data); "
                            'the game skips it if it doesn\'t exist')
        mob_cat = entities.get(rule['mob'], {}).get('category')
        changed = 0
        hit = [b for b in full if b in tuned]
        for b in hit:
            line = apply_rule(tuned[b], rule, mob_cat)
            if line:
                changes[b].append(rule['id'] + ': ' + line)
                changed += 1
        rows.append((rule, hit, changed))
    tuned = {b: strip(sp) for b, sp in tuned.items()}
    # does the dump show what the file says?
    live = {b: strip(v.get('spawners', {})) for b, v in dump['biomes'].items()}
    differ = sorted(b for b in tuned if tuned[b] != live.get(b))
    live_ids = dump.get('tuning', {}).get('rules', [])
    want_ids = applied
    stale = None
    if differ or (live_ids != want_ids and (live_ids or want_ids)):
        stale = (f'the dump ({dump.get("generated", "?")}) ran {len(live_ids)} rules, mob_spawns.json has '
                 f'{len(want_ids)}; {len(differ)} biomes differ from the dump. Rejoin the world and /dumpspawns to '
                 'confirm. The tables below show mob_spawns.json as it is now.')
    return tuned, untuned, rows, problems, warnings, stale, changes


def build_conditions(dump, cfg, traits, rows):
    """mob_spawns.json 'conditions' as mob_spawns.js resolves them.
    Returns ([(cond, mobs, biomes or None = everywhere)], problems, warnings)."""
    biome_tags = {b: set(v.get('tags', [])) for b, v in dump['biomes'].items()}
    namespaces = {'minecraft', 'creativeengineering'} | {m.split(':')[0] for m in dump.get('entities', {})}
    usable = {r['id']: r for r, hit, _ in rows if hit}
    out, problems, warnings, seen = [], [], [], set()
    for n, c in enumerate(cfg.get('conditions', []), 1):
        cid = c.get('id')
        mobs = c.get('mobs')
        err = None
        if not cid or not re.fullmatch(r'[a-z0-9_]+', str(cid)):
            err = 'id must be lowercase a-z 0-9 _'
        elif cid in seen:
            err = 'duplicate id'
        elif c.get('condition') not in CONDITIONS:
            err = 'condition must be light_only or water_only'
        elif not isinstance(mobs, list) or not mobs or any(':' not in str(m) for m in mobs):
            err = 'mobs must be a list of entity ids'
        else:
            missing = [r for r in c.get('rules', []) if r not in usable]
            if missing:
                err = 'rules: no usable rule ' + ', '.join(missing)
        if err:
            problems.append(f"condition {n} ({cid or 'no id'}): {err}")
            continue
        seen.add(cid)
        loaded = [m for m in mobs if m.split(':')[0] in namespaces]
        for m in mobs:
            if m not in loaded:
                warnings.append(f'{cid}: mod not loaded, left out {m}')
            elif m not in dump.get('entities', {}):
                warnings.append(f'{cid}: {m} is not in the dump (typo, or a mob with no spawn data)')
        if not loaded:
            continue
        biomes = None
        if c.get('where') or c.get('add') or c.get('rules'):
            _, _, full = resolve_rule({k: c.get(k, []) for k in ('where', 'add')}, traits, biome_tags)
            full = set(full)
            for rid in c.get('rules', []):
                full |= set(resolve_rule(usable[rid], traits, biome_tags)[2])
            full -= set(c.get('except', []))
            biomes = sorted(full)
            if not biomes:
                warnings.append(f'{cid}: matches no biome, skipped')
                continue
        out.append((c, loaded, biomes))
    live = dump.get('tuning', {}).get('conditions', [])
    want = [c['id'] for c, _, _ in out]
    if live != want and (live or want):
        warnings.append(f'the dump ran conditions {live or "none"}, mob_spawns.json has {want or "none"}: rejoin and /dumpspawns')
    return out, problems, warnings


# ── numbers ─────────────────────────────────────────────────────────────────

def shares(spawners):
    """{cat: {mob: (share, weight, min, max)}}; several entries of one mob are summed."""
    out = {}
    for cat, lst in spawners.items():
        total = sum(e['weight'] for e in lst)
        if not total:
            continue
        d = {}
        for e in lst:
            s, w, mn, mx = d.get(e['type'], (0, 0, e['min'], e['max']))
            d[e['type']] = (s + e['weight'] / total, w + e['weight'], min(mn, e['min']), max(mx, e['max']))
        out[cat] = d
    return out


def region_of(b, areas):
    if b not in areas:
        return None, 0.0
    dim, layer, pct = areas[b]
    for label, d, layers in REGIONS:
        if d == dim and layer in layers:
            return label, pct
    return None, 0.0


def fmt_pct(x, digits=1):
    if x == 0:
        return '0'
    if x * 100 < 0.05:
        return '<0.1'
    return f'{x * 100:.{digits}f}'


def fmt_area(p):
    return '' if p is None else ('-' if p == 0 else ('<0.03' if p < 0.03 else f'{p:.2f}'))


def short(b):
    return b[len('minecraft:'):] if b.startswith('minecraft:') else b


def group(mn, mx):
    return str(mn) if mn == mx else f'{mn}-{mx}'


# ── report ──────────────────────────────────────────────────────────────────

REGION_ORDER = {r[0]: n for n, r in enumerate(REGIONS)}


def mob_rows(mob, sh, areas, dump):
    """[(biome, cat, share, weight, min, max, (region, area))] for one mob: by region (Overworld surface first),
    then biggest area."""
    rows = []
    for b, cats in sh.items():
        for cat, d in cats.items():
            if mob in d:
                s, w, mn, mx = d[mob]
                rows.append((b, cat, s, w, mn, mx, region_of(b, areas) if b in areas else (None, None)))
    rows.sort(key=lambda r: (REGION_ORDER.get(r[6][0], len(REGIONS)), -(r[6][1] or 0), -r[2], r[0]))
    return rows


def mob_table(mob, rows, untuned_sh, changed_biomes):
    L = ['| Biome | Category | Weight | Share % | Group | Area % |', '|---|---|---|---|---|---|']
    for b, cat, s, w, mn, mx, (region, a) in rows:
        mark = ''
        if b in changed_biomes:
            old = untuned_sh.get(b, {}).get(cat, {}).get(mob)
            mark = f' (was {old[1]})' if old and old[1] != w else (' (added)' if not old else '')
        where = '' if region in (None, 'Overworld') else ' ' + region.lower()
        L.append(f'| `{short(b)}` | {cat} | {w}{mark} | {fmt_pct(s)} | {group(mn, mx)} | {fmt_area(a)}{where} |')
    return L


def world_shares(mob, cat, sh, areas):
    out = {}
    for label, dim, layers in REGIONS:
        tot = 0.0
        acc = 0.0
        for b, (d, layer, pct) in areas.items():
            if d != dim or layer not in layers or not pct:
                continue
            tot += pct
            acc += pct * sh.get(b, {}).get(cat, {}).get(mob, (0,))[0]
        out[label] = acc / tot if tot else 0.0
    return out


def rule_text(mob, ent, vanilla):
    v = vanilla.get(mob)
    if v:
        note = RULE_NOTES.get(v['rule'], '')
        return f"`{v['rule']}`" + (f': {note}' if note else '')
    if not ent:
        return ''
    parts = []
    src = ent.get('predicate_source')
    if src:
        parts.append('registered by `' + src.rsplit('.', 1)[-1] + '`'
                     + (' (changed through Forge\'s SpawnPlacementRegisterEvent)' if 'SpawnPlacementRegisterEvent' in src else ''))
    if ent.get('rule_methods'):
        parts.append('rule ' + ', '.join('`' + m.rsplit('.', 2)[-2] + '.' + m.rsplit('.', 1)[-1] + '`'
                                         for m in ent['rule_methods']))
    if not src:
        parts.append('no spawn placement registered (any block, any light)')
    return '; '.join(parts)


def write_md(dump, tuned, untuned, rows, problems, warnings, stale, changes, areas, traits, vanilla, conds=(), cfg=None):
    sh = {b: shares(sp) for b, sp in tuned.items()}
    untuned_sh = {b: shares(sp) for b, sp in untuned.items()}
    ents = dump.get('entities', {})
    dims_of = {b: v.get('dimensions', []) for b, v in dump['biomes'].items()}
    live = {b for b, ds in dims_of.items() if ds}
    mobs = defaultdict(dict)      # cat -> mob -> biome count
    for b in live:
        for cat, d in sh.get(b, {}).items():
            for m in d:
                mobs[cat][m] = mobs[cat].get(m, 0) + 1
    L = []
    w = L.append
    w('# Mob spawns by biome (generated)\n')
    w(f'Generated by `tools/gen_spawn_table.py` from `kubejs/exported/spawns.json` (/dumpspawns, '
      f'{dump.get("generated", "?")}) with `mob_spawns.json` applied. Do not edit by hand: rerun after a dump or after '
      'editing `kubejs/generation_scripts/worldgen/mob_spawns.json`. Reference: [mob_spawns.md](mob_spawns.md).\n')
    w('**Reading the numbers:**')
    w('- **Share** = weight / total weight of the category in that biome: the chance a spawn attempt of that category '
      'there picks this mob.')
    w('- **World share** = share weighted by biome area (`biome_config.json`): the part of the category\'s spawn picks '
      'across the region that go to this mob, assuming equal attempts per area. Overworld = surface biomes, Caves = '
      'overworld cave biomes, Nether and End = their whole dimension. Not spawns per hour: light, ground blocks and mob '
      'caps still decide (section 6).')
    w('- Only biome spawner lists. Structure spawns (fortresses, monuments, huts, outposts) and separate spawners '
      '(patrols, phantoms, traders, villages) are not in them; section 7 names the known ones.')
    w('- Biomes in no dimension (unused or dimension-less) are left out. Area `-` = no sampled area (sky dimensions, '
      'pocket dimensions); `<0.03` = under the climate sample\'s resolution.\n')
    if stale:
        w(f'**WARNING:** {stale}\n')

    # 1. tuning
    w('## 1. Pack tuning (`mob_spawns.json`)\n')
    if not rows and not problems:
        w('No rules: every mob spawns as the mods ship it.\n')
    else:
        w('| Rule | Mob | Action | Biomes matched | Biomes changed | Notes |')
        w('|---|---|---|---|---|---|')
        for rule, hit, changed in rows:
            act = {'set': lambda r: f"set {r['weight']}" + (f" ({group(r.get('min') or 1, r.get('max') or r.get('min') or 1)})"
                                                             if r.get('min') or r.get('max') else ''),
                   'scale': lambda r: f"x{r['factor']}", 'remove': lambda r: 'remove'}[rule['action']](rule)
            if rule.get('brown_chance') is not None:
                act += f", brown {rule['brown_chance'] * 100:g} %"
            w(f"| `{rule['id']}` | `{rule['mob']}` | {act} | {len(hit)} | {changed} | "
              f"{(rule.get('label') or '') + (' ' if rule.get('label') and rule.get('notes') else '') + (rule.get('notes') or '')} |")
        w('')
    for p in problems:
        w(f'- **PROBLEM:** {p}')
    for x in warnings:
        w(f'- WARNING: {x}')
    if problems or warnings:
        w('')
    if changes:
        w('<details><summary>Changes per biome</summary>\n')
        for b in sorted(changes):
            w(f'- `{short(b)}`: ' + '; '.join(changes[b]))
        w('\n</details>\n')
    if conds:
        w('**Spawn conditions** (checked at spawn time, not in the weights above; mob_spawns.md 3.1):\n')
        w('| Condition | Kind | Mobs | Biomes | Notes |')
        w('|---|---|---|---|---|')
        for c, cmobs, biomes in conds:
            where = 'everywhere' if biomes is None else (f'{len(biomes)}: ' + ', '.join(short(b) for b in biomes)
                                                        if len(biomes) <= 12 else f'{len(biomes)} biomes')
            w(f"| `{c['id']}` | {c['condition']} | {', '.join(short(m) for m in cmobs)} | {where} | "
              f"{(c.get('label') or '') + (' ' if c.get('label') and c.get('notes') else '') + (c.get('notes') or '')} |")
        w('')
    ground = {t: v for t, v in ((cfg or {}).get('spawn_ground') or {}).items() if not t.startswith('_')}
    if ground:
        w('**Spawn ground** (added to block tags spawn rules read): '
          + '; '.join(f"`#{t}` += {', '.join('`' + e + '`' for e in v)}" for t, v in ground.items()) + '\n')

    # 2. summary per category
    w('## 2. Mobs by category\n')
    caps = dump.get('categories', {})
    for cat in CATEGORIES:
        if not mobs.get(cat):
            continue
        c = caps.get(cat, {})
        w(f'### {CAT_LABEL[cat]} (`{cat}`, cap {c.get("max_per_chunk", "?")} per chunk'
          + (', persistent' if c.get('persistent') else '') + ')\n')
        w('| Mob | Biomes | ' + ' | '.join(f'{r[0]} %' for r in REGIONS) + ' | Groups | Egg |')
        w('|---|---|' + '---|' * len(REGIONS) + '---|---|')
        lines = []
        for m, nb in mobs[cat].items():
            ws = world_shares(m, cat, sh, areas)
            grp = {group(d[cat][m][2], d[cat][m][3]) for b, d in sh.items() if b in live and m in d.get(cat, {})}
            lines.append((-ws['Overworld'], -ws['Caves'], -ws['Nether'], -ws['End'], m, nb, ws, grp))
        for *_, m, nb, ws, grp in sorted(lines):
            egg = 'yes' if ents.get(m, {}).get('spawn_egg') else ''
            w(f'| `{m}` | {nb} | ' + ' | '.join(fmt_pct(ws[r[0]], 2) for r in REGIONS)
              + f" | {', '.join(sorted(grp, key=lambda g: [int(x) for x in g.split('-')]))} | {egg} |")
        w('')

    # 3. creatures by biome type
    w('## 3. Creatures by biome type\n')
    w('Mean creature share over the overworld biomes with that trait (`biome_traits.json` table), weighted by area; '
      'biomes with no area count at a weight of 0.01. Columns are the biome types the tuning plans in mob_spawns.md '
      'name; a mob under 0.5% everywhere is left out.\n')
    cols = []
    for label, clause in TYPE_COLUMNS:
        bs = [b for b, tr in traits.items() if matches(tr, clause) and b in live]
        if bs:
            cols.append((label, bs))
    w('| Mob | ' + ' | '.join(f'{c[0]} ({len(c[1])})' for c in cols) + ' |')
    w('|---|' + '---|' * len(cols))
    trows = []
    for m in mobs.get('creature', {}):
        vals = []
        for label, bs in cols:
            num = den = 0.0
            for b in bs:
                a = region_of(b, areas)[1] or 0.01
                num += a * sh.get(b, {}).get('creature', {}).get(m, (0,))[0]
                den += a
            vals.append(num / den if den else 0)
        if max(vals) >= 0.005:
            trows.append((-sum(vals), m, vals))
    for _, m, vals in sorted(trows):
        w(f'| `{m}` | ' + ' | '.join(fmt_pct(v, 0) if v >= 0.005 else '' for v in vals) + ' |')
    w('')

    # 4. per mob
    w('## 4. Per mob\n')
    w('Every biome each mob spawns in: overworld surface first, then caves, Nether, End, biomes without area; '
      'biggest area first. Area is the share of that region. "(was N)" = weight before `mob_spawns.json`.\n')
    changed_biomes = set(changes)
    for cat in CATEGORIES:
        for m in sorted(mobs.get(cat, {})):
            rws = [r for r in mob_rows(m, {b: sh[b] for b in live if b in sh}, areas, dump) if r[1] == cat]
            if not rws:
                continue
            w(f'### `{m}` ({cat}, {len(rws)} biomes)\n')
            L.extend(mob_table(m, rws, untuned_sh, changed_biomes))
            w('')

    # 5. per biome
    w('## 5. Per biome\n')
    w('Every spawner by category with its share. "Herds" = the chance a new chunk spawns a creature group at '
      'generation (`creature_probability`). Costs = spawn-potential charge / energy budget (soul sand valley style '
      'density limits).\n')
    by_dim = defaultdict(list)
    for b in sorted(live):
        by_dim[dims_of[b][0]].append(b)
    for dim in sorted(by_dim):
        w(f'### {dim}\n')
        for b in by_dim[dim]:
            v = dump['biomes'][b]
            a = areas.get(b, (None, None, None))[2]
            tr = ' '.join(sorted(traits.get(b, []))) or 'no traits (not in biome_traits.json)'
            w(f"**`{short(b)}`** area {fmt_area(a) or '-'}%, herds {fmt_pct(v.get('creature_probability', 0))}%, "
              f"traits: {tr}" + (' (tuned)' if b in changed_biomes else ''))
            w('')
            if not sh.get(b):
                w('- no spawners\n')
                continue
            for cat in CATEGORIES:
                d = sh[b].get(cat)
                if not d:
                    continue
                items = sorted(d.items(), key=lambda kv: -kv[1][0])
                w(f'- {cat}: ' + ', '.join(f'`{short(m)}` {fmt_pct(s, 0)}% ({x} {group(mn, mx)})'
                                           for m, (s, x, mn, mx) in items))
            if v.get('costs'):
                w('- costs: ' + ', '.join(f"`{short(m)}` {c['charge']}/{c['energy_budget']}" for m, c in sorted(v['costs'].items())))
            w('')

    # 6. conditions
    w('## 6. Spawn conditions\n')
    w('The placement and rule a mob must pass after it is picked, for every mob with a spawner, a placement or a spawn '
      'egg. Placement: ON_GROUND, IN_WATER, IN_LAVA or NO_RESTRICTIONS. Vanilla rules are named from the bytecode '
      '(`tools/spawn_rules_vanilla.json`); a mod\'s rule shows the class that registered it and the spawn-rule methods on '
      'its own classes. "Own checks" = the mob overrides `checkSpawnRules` / `checkSpawnObstruction` (run after the '
      'rule). Natural spawns also need the mob cap not to be full, no player within 24 blocks, and space for the mob.\n')
    w('| Mob | Category | Biomes | Placement | Rule | Own checks |')
    w('|---|---|---|---|---|---|')
    for m in sorted(ents):
        e = ents[m]
        nb = sum(1 for b in live if any(m in d for d in sh.get(b, {}).values()))
        if not nb and not e.get('placement'):
            continue
        own = [x.rsplit('.', 1)[-1] + (' (spawn rules)' if k == 'spawn_rules_override' else ' (obstruction)')
               for k in ('spawn_rules_override', 'obstruction_override') if (x := e.get(k))]
        pl = (e.get('placement') or '-') + (f", {e['heightmap']}" if e.get('heightmap') and e['heightmap'] != 'MOTION_BLOCKING_NO_LEAVES' else '')
        w(f"| `{m}` | {e.get('category', '?')} | {nb} | {pl} | {rule_text(m, e, vanilla)} | {', '.join(own)} |")
    w('')

    # 7. gaps
    w('## 7. Gaps\n')
    w('**Spawn eggs with no biome spawner** (other sources named where known):\n')
    natural = {m for b in live for d in sh.get(b, {}).values() for m in d}
    for m in sorted(x for x, e in ents.items() if e.get('spawn_egg') and x not in natural):
        w(f'- `{m}`' + (f': {OTHER_SOURCES[m]}' if m in OTHER_SOURCES else ''))
    w('')
    w('**Biomes with no creature spawners** (overworld surface biomes with area only; caves, oceans and sky biomes often '
      'have none on purpose):\n')
    empty = [b for b in sorted(live) if not sh.get(b, {}).get('creature') and areas.get(b, (0, 0, 0))[2]
             and areas[b][0] == 'minecraft:overworld' and areas[b][1] == 'surface']
    w(', '.join(f'`{short(b)}`' for b in empty) or 'none')
    w('')
    missing = sorted(b for b in live if 'minecraft:overworld' in dims_of[b] and b not in traits)
    w('**Overworld biomes without traits** (a `where` clause can\'t select them; name them in `add`): '
      + (', '.join(f'`{short(b)}`' for b in missing) or 'none') + '\n')
    OUT_MD.write_text('\n'.join(L).rstrip() + '\n')


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--mob', help='print one mob\'s biome table (markdown) and exit')
    ap.add_argument('--refresh-vanilla-rules', action='store_true', help='rebuild tools/spawn_rules_vanilla.json')
    ap.add_argument('--dump', type=Path, help='spawns.json to read (tests)')
    ap.add_argument('--rules', type=Path, help='mob_spawns.json to read (tests)')
    ap.add_argument('--out', type=Path, help='markdown file to write (tests)')
    args = ap.parse_args()
    global SPAWNS, RULES, OUT_MD
    SPAWNS, RULES, OUT_MD = (args.dump or SPAWNS).resolve(), (args.rules or RULES).resolve(), (args.out or OUT_MD).resolve()
    if args.refresh_vanilla_rules:
        refresh_vanilla_rules()
        return 0
    dump = load_json(SPAWNS, 'run /dumpspawns (or /dumpall) in game first')
    cfg = load_json(RULES, 'the tuning rules')
    traits = load_traits()
    areas = load_areas()
    vanilla = json.loads(VANILLA_RULES.read_text())['mobs'] if VANILLA_RULES.exists() else {}
    tuned, untuned, rows, problems, warnings, stale, changes = build(dump, cfg, traits)
    conds, cprob, cwarn = build_conditions(dump, cfg, traits, rows)
    problems += cprob
    warnings += cwarn
    if args.mob:
        live = {b for b, v in dump['biomes'].items() if v.get('dimensions')}
        sh = {b: shares(sp) for b, sp in tuned.items() if b in live}
        rws = mob_rows(args.mob, sh, areas, dump)
        if not rws:
            print(f'{args.mob} spawns in no biome', file=sys.stderr)
            return 1
        print('\n'.join(mob_table(args.mob, rws, {b: shares(sp) for b, sp in untuned.items()}, set(changes))))
        return 0
    write_md(dump, tuned, untuned, rows, problems, warnings, stale, changes, areas, traits, vanilla, conds, cfg)
    n_rules = len(rows)
    print(f'{len(dump["biomes"])} biomes, {len(dump.get("entities", {}))} mobs, {n_rules} tuning rules '
          f'({sum(r[2] for r in rows)} biome changes), {len(conds)} spawn conditions -> {rel(OUT_MD)}')
    for p in problems:
        print('PROBLEM:', p)
    for x in warnings:
        print('WARNING:', x)
    if stale:
        print('WARNING:', stale)
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
