"""jar_storage, jar_interaction, knife_drops and grass_drops tabs, generated part.

jar_storage / jar_interaction: every Moonlight soft fluid (data/<ns>/moonlight/soft_fluids/*.json in the jars plus
the pack's own in kubejs/data, id = <ns>:<file>) whose from_mod is loaded, one row per container: the filled item
into a jar, and an empty container out of a full jar. Items that are not registered (not in
kubejs/exported/items_by_mod) or removed (removed_items.js, item_merges.js, removed_woods.js) drop out, as Moonlight
drops unregistered ids. The jar's capacity and the cookie / liquid / mob switches come from
config/supplementaries-common.toml [functional.jar]. Cookies: the pack's jar families plus the rest of
#supplementaries:cookies (feature_writeup/mixed_jars.md). Mobs: #supplementaries:jar_catchable.
Faucet sources, fish buckets: hand-written in jar_*/manual.json (verified in the Supplementaries jar).

knife_drops / grass_drops: the global loot modifiers listed in an active data/forge/loot_modifiers/
global_loot_modifiers.json (merged over every jar and kubejs/data, `replace` honoured). A modifier file present in
several jars resolves to the one from a jar that does not own its namespace (Abnormals Delight overrides Farmer's
Delight's scavenging_feather / scavenging_leather). knife_drops: modifiers that need a knife, machete or other
scavenging tool. grass_drops: the dumped block loot tables (kubejs/exported/loot_tables) of grass-like plants, per
tool (shears, knife, anything else), plus the modifiers that add to those blocks.
"""
import json
import re
import zipfile

from .common import (EXPORTED, KUBEJS_DATA, MODS, ROOT, block, block_tag, entity, fluid, item, items, mod_of,
                     row, tag)

SUPP_CONFIG = ROOT / 'config' / 'supplementaries-common.toml'
DELIGHTFUL_CONFIG = ROOT / 'config' / 'delightful-common.toml'
LIB = ROOT / 'kubejs' / 'startup_scripts' / '_lib'
JAR = 'supplementaries:jar'
UNIT_MB = 250          # one soft fluid unit (a bottle) in mB; a bucket is 4

# Tool tags that make a modifier a knife drop, and how the action slot shows them.
KNIFE_TAGS = {
    'farmersdelight:tools/knives': ('farmersdelight:tools/knives', 'a knife'),
    'forge:tools/knives': ('farmersdelight:tools/knives', 'a knife'),
    'forge:tools/scavenging': ('forge:tools/scavenging', 'a knife'),
    'farmersdelight:straw_harvesters': ('farmersdelight:straw_harvesters', 'a knife'),
    'nethersdelight:tools/machetes': ('nethersdelight:tools/machetes', 'a machete'),
    'forge:tools/machetes': ('nethersdelight:tools/machetes', 'a machete'),
    'nethersdelight:tools/hunting_tools': ('nethersdelight:tools/hunting_tools', 'a knife or machete'),
}
SHEAR_TAGS = {'forge:shears'}

# Grass-like ground plants for grass_drops: block loot tables whose path matches, wild crops excluded (their own tab).
GRASS_RE = re.compile(r'^blocks/((tall_)?(grass|fern|seagrass|beachgrass)|large_fern|dead_bush|sandy_shrub|'
                      r'frosty_(grass|fern)|nether_sprouts)$')

# Pastry slicing: FD PastrySlicingModifier gives 7 - bites for a CakeBlock, 4 - bites for a PieBlock.
CAKE_SLICES = {'minecraft:cake': 7}


# Pack data --------------------------------------------------------------------------------------------------

def _toml_section(path, section):
    """{key: raw value string} of one [section] in a Forge toml (flat, enough for booleans and numbers)."""
    out, inside = {}, False
    if not path.exists():
        return out
    for line in path.read_text(encoding='utf-8').splitlines():
        s = line.strip()
        if s.startswith('['):
            inside = s.strip('[]').strip() == section
            continue
        if inside and '=' in s and not s.startswith('#'):
            k, v = s.split('=', 1)
            out[k.strip()] = v.strip().strip('"')
    return out


def _item_dict():
    vals = {}
    src = (LIB / 'item_dict.js').read_text(encoding='utf-8')
    for m in re.finditer(r"^\s*i\.(\w+)\s*=\s*'([^']*)'", src, re.M):
        vals[m.group(1)] = m.group(2)
    return vals


def removed_and_merges():
    """(removed item ids, {old id: new id}) from removed_items.js, removed_woods.js and every item_merges.js."""
    vals = _item_dict()
    removed, merges = set(), {}
    src = (LIB / 'removed_items.js').read_text(encoding='utf-8')
    body = src.split('global.removedItems = [', 1)[1].split('];', 1)[0]
    for key in re.findall(r'i\.(\w+)', body):
        if key in vals:
            removed.add(vals[key])
    # the pack's merges, then Creative Delight's food lists (<mod>/item_merges.js, item_merges.md 1.1)
    cd = ROOT / 'development/creativeDelight/src/main/resources/kubejs_startup'
    for p in [LIB / 'item_merges.js', LIB / 'removed_woods.js', *sorted(cd.glob('*/item_merges.js'))]:
        if not p.exists():
            continue
        for a, b in re.findall(r'\[\s*i\.(\w+)\s*,\s*i\.(\w+)\s*\]', p.read_text(encoding='utf-8')):
            if a in vals and b in vals:
                removed.add(vals[a])
                merges[vals[a]] = vals[b]
    return removed, merges


def known_items():
    out = set()
    for f in (EXPORTED / 'items_by_mod').glob('*.json'):
        try:
            out.update(json.loads(f.read_text(encoding='utf-8')).get('items', []))
        except ValueError:
            pass
    return out


def loaded_mods():
    out = {'minecraft', 'forge'}
    for f in (EXPORTED / 'items_by_mod').glob('*.json'):
        out.add(f.stem)
    for f in MODS.glob('*.jar'):
        try:
            with zipfile.ZipFile(f) as z:
                if 'META-INF/mods.toml' in z.namelist():
                    out.update(re.findall(r'modId\s*=\s*"([^"]+)"', z.read('META-INF/mods.toml').decode('utf-8', 'replace')))
        except zipfile.BadZipFile:
            pass
    return out


class Pack:
    def __init__(self, ctx):
        self.ctx = ctx
        self.items = known_items()
        self.removed, self.merges = removed_and_merges()
        self.mods = loaded_mods()
        self.item_tags = ctx.exported('tags/item_tags.json') or {}
        self.block_tags = ctx.exported('tags/block_tags.json') or {}
        self.entity_tags = ctx.exported('tags/entity_type_tags.json') or {}
        self.fluids = set(ctx.exported('fluids.json') or {})

    def ok(self, id_):
        """Registered, not removed. Unknown when the dump is missing: accept."""
        if id_ in self.removed:
            return False
        return not self.items or id_ in self.items

    def out(self, id_):
        """An output id after the pack's loot merges, or None when it is gone."""
        id_ = self.merges.get(id_, id_)
        return id_ if self.ok(id_) else None

    def entities(self, ref):
        ref = ref.lstrip()
        if ref.startswith('#'):
            return list(self.entity_tags.get(ref[1:], []))
        return [ref]


# Jar storage and emptying --------------------------------------------------------------------------------------

def soft_fluids(ctx, pack):
    """[(id, json)] of every active soft fluid; later entries never win over earlier ones for one filled item."""
    found = {}
    for path, data in ctx.data_files('moonlight/soft_fluids').items():
        ns, rest = path.split('/', 1)
        name = rest[len('moonlight/soft_fluids/'):-len('.json')]
        from_mod = data.get('from_mod', 'minecraft')
        if from_mod not in pack.mods:
            continue
        found[f'{ns}:{name}'] = data
    return sorted(found.items())


def jar_color(data):
    """The soft fluid's colour as a signed ARGB int. The jar item renderer reads it from CachedColor (missing: 0,
    drawn black); no colour or no_tint means untinted (-1)."""
    color = data.get('color')
    if not color or data.get('tint_method') == 'no_tint':
        return -1
    v = int(color.lstrip('#'), 16)
    if len(color.lstrip('#')) <= 6:
        v |= 0xFF000000
    return v - (1 << 32) if v >= 1 << 31 else v


def jar_nbt(fluid_id, count, color):
    return '{BlockEntityTag:{FluidHolder:{Fluid:"%s",Count:%d,CachedColor:%d}}}' % (fluid_id, count, color)


POTION_BOTTLES = ('minecraft:potion', 'minecraft:splash_potion', 'minecraft:lingering_potion')


def filled_ing(ids, fluid_id):
    """The filled container slot; water and potions are minecraft:potion told apart by NBT."""
    if ids == ['minecraft:potion'] and fluid_id.endswith(':water'):
        return item('minecraft:potion', nbt='{Potion:"minecraft:water"}')
    return items(ids) if len(ids) > 1 else item(ids[0])


def is_potion_row(ids, fluid_id):
    """Potion bottles into a jar: the row is written once and marked per_potion; patcherMod's PotionRows makes one row
    per potion (bottles with the potion, jars with its NBT and colour)."""
    return not fluid_id.endswith(':water') and any(i in POTION_BOTTLES for i in ids)


def jar_rows(ctx, pack):
    cfg = _toml_section(SUPP_CONFIG, 'functional.jar')
    cap = int(cfg.get('capacity', '12'))
    storage, emptying = [], []
    claimed = {}
    if cfg.get('jar_liquids', 'true') == 'true':
        for fid, data in soft_fluids(ctx, pack):
            equiv = next((f for f in data.get('equivalent_fluids', []) if f in pack.fluids), None)
            req = {mod_of(fid), data.get('from_mod', 'minecraft')} - {'minecraft', 'moonlight', 'supplementaries'}
            for cat in data.get('containers', []):
                empty = cat.get('empty', 'minecraft:air')
                size = int(cat.get('capacity', 1))
                if empty != 'minecraft:air' and not pack.ok(empty):
                    continue
                filled = [f for f in cat.get('filled', []) if pack.ok(f)]
                if fid.endswith(':water'):
                    filled = [f for f in filled if f != 'minecraft:potion'] + (['minecraft:potion'] if 'minecraft:potion' in cat.get('filled', []) else [])
                fresh = []
                for f in filled:
                    key = (f, empty, 'water' if fid.endswith(':water') and f == 'minecraft:potion' else '')
                    if key in claimed:      # two fluids list it (Moonlight and Supplementaries ghast tear): first wins
                        continue
                    claimed[key] = fid
                    fresh.append(f)
                if not fresh or size <= 0 or size > cap:
                    continue
                r = set(req) | {mod_of(f) for f in fresh} - {'minecraft'}
                if empty != 'minecraft:air':
                    r.add(mod_of(empty))
                r.discard('minecraft')
                inputs = [filled_ing(fresh, fid), item(JAR)]
                outs = [item(JAR, nbt=jar_nbt(fid, size, jar_color(data)), label=f'{size} of {cap} filled')]
                if empty != 'minecraft:air':
                    outs.append(item(empty))
                if equiv:
                    outs.append(fluid(equiv, size * UNIT_MB, label='Same fluid as this'))
                each = cap // size
                notes = [f'Each adds {size} of {cap}; {each} fill a jar.']
                if data.get('preserved_tags_from_item'):
                    notes.append('One potion kind per jar.')
                if empty == 'minecraft:air':
                    notes.append('Cannot be poured back out.')
                storage.append(row(inputs, outs, notes=notes, requires=r))
                if is_potion_row(fresh, fid):
                    storage[-1]['per_potion'] = True
                if empty == 'minecraft:air':
                    continue
                first = fresh[0]
                out = filled_ing([first], fid)
                emptying.append(row([item(JAR, nbt=jar_nbt(fid, cap, jar_color(data)), label=f'Holding {cap}'), item(empty)], [out],
                                    notes=[f'Takes {size} of {cap}.'], requires=r))
                if is_potion_row([first], fid):
                    emptying[-1]['per_potion'] = True
    if cfg.get('jar_cookies', 'true') == 'true':
        storage.extend(cookie_rows(pack, cap))
    if cfg.get('jar_capture', 'true') == 'true':
        storage.extend(mob_rows(pack))
    return storage, emptying


def cookie_rows(pack, cap):
    rows = []
    families = sorted(t for t in pack.item_tags if t.startswith('creativeengineering:jar_family/'))
    in_family = set()
    for t in families:
        members = [m for m in pack.item_tags[t] if pack.ok(m)]
        if not members:
            continue
        in_family.update(members)
        rows.append(row([tag(t), item(JAR)], [item(JAR)],
                        notes=[f'Up to {cap}, one per layer.', 'Any mix of the group shares a jar.']))
    alone = [m for m in pack.item_tags.get('supplementaries:cookies', []) if m not in in_family and pack.ok(m)]
    if alone:
        rows.append(row([items(alone), item(JAR)], [item(JAR)],
                        notes=[f'Up to {cap}, one per layer.', 'Only one kind per jar.']))
    return rows


def mob_rows(pack):
    rows = []
    mobs = [e for e in pack.entity_tags.get('supplementaries:jar_catchable', [])]
    if mobs:
        rows.append(row([{'entities': mobs}], [item(JAR, label='Holding the mob')], action=item(JAR),
                        notes=['Use an empty jar on the mob.', 'Place it to let the mob out.']))
    babies = pack.entity_tags.get('supplementaries:jar_baby_catchable', [])
    if babies:
        rows.append(row([{'entities': babies}], [item(JAR, label='Holding the mob')], action=item(JAR),
                        notes=['Babies only.', 'Use an empty jar on the mob.']))
    return rows


# Global loot modifiers --------------------------------------------------------------------------------------

def active_modifiers(ctx):
    """[(id, json)] of every modifier an active global_loot_modifiers.json lists."""
    lists = []          # (source, data) in load order: jars by name, kubejs/data last
    files = {}          # data path -> [(jar name or None, text)]
    want = re.compile(r'^data/([^/]+)/loot_modifiers/(.+)\.json$')
    for jar in sorted(MODS.glob('*.jar')):
        try:
            with zipfile.ZipFile(jar) as z:
                for n in z.namelist():
                    if n == 'data/forge/loot_modifiers/global_loot_modifiers.json':
                        lists.append((jar.name, json.loads(z.read(n))))
                    elif want.match(n):
                        files.setdefault(n[5:], []).append((jar.name, z.read(n).decode('utf-8')))
        except (zipfile.BadZipFile, ValueError) as e:
            ctx.warn(f'{jar.name}: {e}')
    kd = KUBEJS_DATA / 'forge' / 'loot_modifiers' / 'global_loot_modifiers.json'
    if kd.exists():
        lists.append(('kubejs', json.loads(kd.read_text(encoding='utf-8'))))
    for f in KUBEJS_DATA.glob('*/loot_modifiers/**/*.json'):
        rel = f.relative_to(KUBEJS_DATA).as_posix()
        if rel != 'forge/loot_modifiers/global_loot_modifiers.json':
            files[rel] = [(None, f.read_text(encoding='utf-8'))]
    entries = []
    for _, data in lists:
        if data.get('replace'):
            entries = []
        for e in data.get('entries', []):
            if e not in entries:
                entries.append(e)
    out = []
    for e in entries:
        ns, p = e.split(':', 1)
        cands = files.get(f'{ns}/loot_modifiers/{p}.json')
        if not cands:
            ctx.warn(f'loot modifier {e} listed but has no file')
            continue
        pick = cands[0]
        if len(cands) > 1:
            owner = ns.replace('_', '').lower()
            pick = next((c for c in cands if c[0] is None), None) or \
                next((c for c in cands if owner not in c[0].replace('_', '').replace(' ', '').lower()), cands[-1])
        try:
            out.append((e, json.loads(pick[1])))
        except ValueError as err:
            ctx.warn(f'loot modifier {e}: {err}')
    return out


def delightful_enabled(key):
    text = DELIGHTFUL_CONFIG.read_text(encoding='utf-8') if DELIGHTFUL_CONFIG.exists() else ''
    m = re.search(r'^\s*%s\s*=\s*(true|false)' % re.escape(key), text, re.M)
    return m is None or m.group(1) == 'true'


class Glm:
    """What one modifier needs and adds, from its conditions. `unknown` lists conditions it could not read."""

    def __init__(self, pack, gid, data):
        self.id = gid
        self.type = data.get('type', '')
        self.chance = 1.0
        self.looting = 0.0
        self.tool = None            # (tag, words) a knife-like tool the modifier needs
        self.no_shears = False
        self.no_knife = False
        self.no_silk = False
        self.entities = []
        self.blocks = []
        self.block_tags = []
        self.block_props = {}
        self.player_kill = False
        self.on_fire = None
        self.table = None
        self.enabled = True
        self.unknown = []
        self.optional_knife = False
        for c in data.get('conditions', []):
            self._cond(pack, c)
        self.out = self._output(data)

    def _cond(self, pack, c, inverted=False):
        kind = c.get('condition', '').replace('minecraft:', '')
        if kind == 'random_chance':
            self.chance *= float(c['chance'])
        elif kind == 'random_chance_with_looting':
            self.chance *= float(c['chance'])
            self.looting = float(c.get('looting_multiplier', 0))
        elif kind == 'killed_by_player':
            self.player_kill = True
        elif kind == 'match_tool':
            pred = c.get('predicate', {})
            t = pred.get('tag')
            if pred.get('enchantments') and inverted:
                self.no_silk = True
            elif t in SHEAR_TAGS and inverted:
                self.no_shears = True
            elif t in KNIFE_TAGS and inverted:
                self.no_knife = True
            elif t in KNIFE_TAGS:
                self.tool = KNIFE_TAGS[t]
            else:
                self.unknown.append(c)
        elif kind == 'inverted':
            self._cond(pack, c['term'], inverted=True)
        elif kind == 'entity_properties':
            pred = c.get('predicate', {})
            who = c.get('entity')
            if who in ('killer', 'killer_player') and 'equipment' in pred:
                t = pred['equipment'].get('mainhand', {}).get('tag')
                if t in KNIFE_TAGS:
                    self.tool = KNIFE_TAGS[t]
                else:
                    self.unknown.append(c)
                if who == 'killer_player':
                    self.player_kill = True
            elif who == 'this' and 'type' in pred:
                self.entities.extend(pack.entities(pred['type']))
            elif who == 'this' and 'flags' in pred and 'is_on_fire' in pred['flags']:
                self.on_fire = bool(pred['flags']['is_on_fire'])
            else:
                self.unknown.append(c)
        elif kind == 'alternative':
            terms = c.get('terms', [])
            kinds = {t.get('condition', '').replace('minecraft:', '') for t in terms}
            if kinds == {'entity_properties'}:
                for t in terms:
                    self._cond(pack, t)
            elif kinds == {'block_state_property'}:
                for t in terms:
                    self._cond(pack, t)
            elif kinds == {'block_state_property', 'match_tool'}:
                # Brewin' cheese wheel: a whole wheel, or any wheel with a knife.
                self.optional_knife = True
                for t in terms:
                    if t['condition'].endswith('match_tool'):
                        self.tool = KNIFE_TAGS.get(t['predicate'].get('tag'))
            else:
                self.unknown.append(c)
        elif kind == 'block_state_property':
            if c['block'] not in self.blocks:
                self.blocks.append(c['block'])
            if c.get('properties'):
                self.block_props[c['block']] = c['properties']
        elif kind == 'delightful:is_tag':
            self.block_tags.append(c['tag'])
        elif kind == 'delightful:enabled':
            self.enabled = delightful_enabled(c['item'])
        elif kind == 'forge:loot_table_id':
            self.table = c['loot_table_id']
        elif kind == 'moonlight:iconditions':
            pass        # Supplementaries feature flags (ash, flax): on in this pack
        else:
            self.unknown.append(c)

    def _output(self, d):
        t = self.type
        if t in ('farmersdelight:add_item',):
            return d['item'], int(d.get('count', 1)), int(d.get('count', 1))
        if t == 'delightful:add_item':
            return d['item'], int(d.get('minAmount', 1)), int(d.get('maxAmount', 1))
        if t == 'moonlight:add_item':
            it = d['item']
            return (it['id'], int(it.get('Count', 1)), int(it.get('Count', 1))) if isinstance(it, dict) else (it, 1, 1)
        if t == 'farmersdelight:pastry_slicing':
            n = 7 if any(b in CAKE_SLICES for b in self.blocks) else 4
            return d['slice'], n, n
        if t == 'brewinandchewin:slicing':
            return d['slice'], 4, 4
        if t == 'immersiveengineering:hemp_seed_drops':
            return 'immersiveengineering:seed', 1, 1
        return None

    def block_ids(self, pack):
        out = list(self.blocks)
        for t in self.block_tags:
            out.extend(pack.block_tags.get(t, []))
        return out

    def output_ing(self, pack):
        if not self.out:
            return None
        oid, lo, hi = self.out
        oid = pack.out(oid)
        if not oid:
            return None
        extra = {}
        if self.chance < 1:
            extra['chance'] = round(self.chance, 4)
        if self.type in ('farmersdelight:pastry_slicing', 'brewinandchewin:slicing'):
            return item(oid, count=hi, label=f'{hi} from a whole one, fewer once eaten', **extra)
        if lo != hi:
            return item(oid, count=hi, label=f'{lo} to {hi}', **extra)
        return item(oid, count=lo, **extra) if lo > 1 else item(oid, **extra)

    def notes(self, kill=True):
        n = []
        if self.tool:
            what = self.tool[1]
            if self.optional_knife:
                n.append(f'Cut with {what}; drops itself otherwise.')
            elif self.entities:
                n.append(f'Killed with {what}' + (' by a player.' if self.player_kill else '.'))
            else:
                n.append(f'Cut with {what}.')
        elif self.player_kill and kill:
            n.append('Killed by a player.')
        if self.on_fire is True:
            n.append('Only if burning.')
        elif self.on_fire is False:
            n.append('Not if burning.')
        if self.looting and self.chance + self.looting >= 1:
            n.append('Looting makes it certain.')
        elif self.looting:
            n.append(f'Looting: +{self.looting * 100:g}% per level.')
        if self.no_silk:
            n.append('Not with Silk Touch.')
        if self.no_shears and not self.tool:
            n.append('Not with shears.')
        return n


def knife_rows(pack, glms):
    rows = []
    for g in glms:
        if not g.tool or not g.enabled or g.unknown or g.table:
            continue
        out = g.output_ing(pack)
        if out is None:
            continue
        req = {mod_of(g.id), mod_of(out['item'])} - {'minecraft'}
        action = tag(g.tool[0])
        if g.entities:
            mobs = [e for e in g.entities if e]
            if not mobs:
                continue
            rows.append(row([{'entities': mobs} if len(mobs) > 1 else entity(mobs[0])], [out], action=action,
                            notes=g.notes(), requires=req))
            continue
        bl = [b for b in g.block_ids(pack)]
        if not bl:
            continue
        notes = g.notes()
        for b, props in g.block_props.items():
            if props.get('age'):
                notes.append('Fully grown only.')
                break
        rows.append(row([blocks_ing(bl)], [out], action=action, notes=notes[:3], requires=req))
    return rows


def blocks_ing(ids):
    return block(ids[0]) if len(ids) == 1 else {'blocks': ids}


# Grass drops ------------------------------------------------------------------------------------------------

def _cond_result(c, scen):
    """(passes, chance) of one loot condition for a scenario: 'shears', 'knife' or 'hand'. None: cannot tell."""
    kind = c.get('condition', '').replace('minecraft:', '')
    if kind == 'random_chance':
        return True, float(c['chance'])
    if kind == 'forge:can_tool_perform_action':
        return (scen == 'shears') if c.get('action') == 'shears_dig' else None, 1.0
    if kind == 'match_tool':
        p = c.get('predicate', {})
        if p.get('tag') in SHEAR_TAGS or p.get('items') == ['minecraft:shears']:
            return scen == 'shears', 1.0
        if p.get('tag') in KNIFE_TAGS:
            return scen == 'knife', 1.0
        if p.get('enchantments'):
            return False, 1.0
        return None, 1.0
    if kind == 'inverted':
        ok, ch = _cond_result(c['term'], scen)
        return (None if ok is None else not ok), 1.0
    if kind in ('survives_explosion', 'block_state_property', 'location_check'):
        return True, 1.0
    return None, 1.0


def _conds(conds, scen):
    chance = 1.0
    for c in conds or []:
        ok, ch = _cond_result(c, scen)
        if ok is None:
            return None, 1.0
        if not ok:
            return False, 1.0
        chance *= ch
    return True, chance


def _count_info(entry):
    count, fortune = 1, None
    for f in entry.get('functions', []):
        fn = f.get('function', '').replace('minecraft:', '')
        if fn == 'set_count' and isinstance(f.get('count'), (int, float)):
            count = int(f['count'])
        elif fn == 'apply_bonus' and f.get('enchantment', '').endswith('fortune'):
            fortune = f.get('parameters', {}).get('bonusMultiplier')
    return count, fortune


def table_drops(table, scen, ctx, where):
    """[(item, count, chance, fortune bonus)] of one simple block table for one scenario (first pool that applies)."""
    out = []
    for pool in table.get('pools', [])[:1]:     # double plants: one pool per half, the same drops
        ok, pool_chance = _conds(pool.get('conditions'), scen)
        if ok is None:
            ctx.warn(f'{where}: pool condition not read, {pool.get("conditions")}')
        if not ok:
            continue
        def walk(entries):
            for e in entries:
                t = e.get('type', '').replace('minecraft:', '')
                if t == 'alternatives':
                    for ch in e.get('children', []):
                        ok, chance = _conds(ch.get('conditions'), scen)
                        if ok is None:
                            ctx.warn(f'{where}: condition not read, {ch.get("conditions")}')
                            return
                        if ok:
                            walk([ch])
                            break
                elif t == 'item':
                    ok, chance = _conds(e.get('conditions'), scen)
                    if ok:
                        cnt, fort = _count_info(e)
                        out.append((e['name'], cnt, chance, fort))
                elif t in ('group', 'sequence'):
                    walk(e.get('children', []))
        walk(pool.get('entries', []))
    return out


def grass_rows(ctx, pack, glms):
    rows = []
    for f in sorted(EXPORTED.glob('loot_tables/*.json')):
        ns = f.stem
        try:
            tables = json.loads(f.read_text(encoding='utf-8'))
        except ValueError:
            continue
        for path, table in sorted(tables.items()):
            if not GRASS_RE.match(path):
                continue
            bid = f'{ns}:{path[len("blocks/"):]}'
            if bid in pack.removed:
                continue
            extra = [g for g in glms if g.enabled and not g.unknown and not g.entities and bid in g.block_ids(pack)]
            for scen in ('hand', 'knife', 'shears'):
                drops = table_drops(table, scen, ctx, f'{bid}')
                outs, notes = [], []
                for iid, cnt, chance, fort in drops:
                    iid = pack.out(iid)
                    if not iid:
                        continue
                    ing = item(iid, **({'count': cnt} if cnt > 1 else {}))
                    if chance < 1:
                        ing['chance'] = round(chance, 4)
                    outs.append(ing)
                    if fort:
                        notes.append(f'Fortune: up to +{fort} seeds per level.')
                glm_outs = []
                for g in extra:
                    if g.tool and scen != 'knife':
                        continue
                    if (g.no_shears and scen == 'shears') or (g.no_knife and scen == 'knife'):
                        continue
                    o = g.output_ing(pack)
                    if o:
                        glm_outs.append(o)
                if scen == 'knife' and not any(g.tool for g in extra):
                    continue            # a knife changes nothing here
                outs.extend(glm_outs)
                if not outs:
                    continue
                if scen == 'hand':
                    action = None
                    notes.insert(0, 'Any tool but shears' + (' or a knife.' if any(g.tool for g in extra) else '.'))
                elif scen == 'knife':
                    action = tag('farmersdelight:straw_harvesters')
                    notes.insert(0, 'Cut with a knife.')
                else:
                    action = tag('forge:shears')
                    notes.insert(0, 'Cut with shears.')
                req = {ns} | {mod_of(o['item']) for o in outs if 'item' in o}
                req.discard('minecraft')
                rows.append(row([block(bid)], outs[:6], action=action, notes=notes[:3], requires=req))
    return rows


def build(ctx):
    pack = Pack(ctx)
    storage, emptying = jar_rows(ctx, pack)
    glms = [Glm(pack, gid, data) for gid, data in active_modifiers(ctx)]
    for g in glms:
        if g.tool and g.unknown:
            ctx.warn(f'{g.id}: conditions not read, left out: {g.unknown}')
    return {
        'jar_storage': storage,
        'jar_interaction': emptying,
        'knife_drops': knife_rows(pack, glms),
        'grass_drops': grass_rows(ctx, pack, glms),
    }
