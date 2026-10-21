# mobs / pillagers: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# mobs`, Sub `pillagers`. Writeups: [mob_spawns.md](../mob_spawns.md)
9 "Later" (test prefix `MS-`) for the biome spawns, [structure_tuning.md](../structure_tuning.md) (`ST-`) for the
structures. Sibling briefs: [worldgen_structures_contents.md](worldgen_structures_contents.md) (`structures/contents #3`,
pillager spawners), [mobs_spawns.md](mobs_spawns.md) (same rule file), [worldgen_structures_metas.md](worldgen_structures_metas.md).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Pillager overhaul: lone pillagers + per-structure mix + spawner removal | Planned | large; split into 1a (lone pillagers, data), 1b (per-structure mix, data + templates), 1c (= contents #3) |
| #4 | More vindicators in single-mob spawn pools | Planned | small to medium: structure spawn overrides (28 T&T files) and/or pool weights |
| #3 | Evokers and illusioners in some pillager structures | Planned | medium: template variants + pool overrides |
| #2 | End pillagers in diving gear | Planned | small: one KubeJS server script |

Build order:

1. **`structures/contents #3` first** (pillager spawners out, spawn boxes in; its own brief). Everything below assumes
   spawners are gone, and its spawn boxes are where #4's vindicator share and #2's End pillagers come from.
2. **#1a lone pillagers** with the next `mob_spawns.json` batch (can ride with `mobs/spawns #4` / `#5`).
3. **#4**, then **#3**, then **#1b**: they all edit the same pools and templates; #3 "can go first" (TODO) because the
   caster variants are self-contained. Do the template work in one tool script so each template is copied once.
4. **#2** any time after contents #3 (the End tower loses its 3 spawners there; see #2).

Shared decision (TODO "share" / "the mix per structure"): the numbers in the 1b table. They are the user's call; the
brief gives a recommendation per structure.

---

## The pillager structures today (verified)

Read from every template with `tools/port_structure_jars.py`'s NBT reader (script in the scratchpad; the counts are the
template's entities and spawners). Jars: `Towns-and-Towers-v.1.10`, `ImprovedPillagerOutpost-v5`,
`takesapillage-1.0.3`, `blocky_siege-6.1.91`, `Unwrecked Ships Plus 1.19-1.0.4`; ported data in `kubejs/data/<ns>/`.

| Structure (group / meta) | How illagers get there | Now |
|---|---|---|
| T&T outposts, 23 land + ocean ship + 4 camps (`towns_and_towers:pillager_outpost_*`, `pillager_settlements`; metas `pillager_towers`, `pillager_strongholds`, `pillager_camps`) | jigsaw slots in the base plates -> pools `kaisyn:outpost/pillagers/grunt` (**pillager 1 : vindicator 1**, 1x3x1 entity templates), `kaisyn:outpost/pillagers/outpost_captain` (ominous-banner pillager); plus **structure spawn override** `monster`, `full` box, pillager only 1-2 (all 28 structure jsons) | 269 grunt slots in all: forts 5-13 each, towers 6-11, beach camps 7, jungle/badlands camps 1 captain + 5 grunts in side pieces, ocean ship 18 + 2 captains; 1-2 allays in 26 of 28 |
| Vanilla outpost (`minecraft:pillager_outpost`) | spawn override (pillager), no template mobs | IPO tower variant (1 in 8) adds `ipo:pillager_outpost/entities` = one vindicator per slot |
| Mysticrift towers, 7 Overworld + End (`mysticrift_pillagers_towers_aris:*`, `pillager_settlements`, meta `pillager_large_towers`) | template entities + 1 pillager spawner each (End: 3 spawners, 0 entities) | 4-8 crossbow pillagers (1 with a banner in acacia, cherry, oak), iron golems / villagers / 3 allays (oak) |
| Illager Siege Weapons x6 (`illager_siege_weapons:*`, `vehicles`, meta `siege_weapons`) | template entities + 1-2 pillager spawners | **vindicators**: ballista 3, battering ram 3, catapult 2, reinforced ram 5, trebuchet 3, siege tower 7 (+ 6 pillagers) |
| Takes a Pillage camp + bastille (`takesapillage:*`) | placed features `takesapillage:mob_feature_*` (code) | hard-coded lists (`ModLists`, decompiled): camp illager = pillager 15, skirmisher 12, archer 8, vindicator 5, witch(?) 3, evoker 1; bastille soldier = legioner 15, skirmisher 12, archer 8, pillager 7, vindicator 5, witch(?) 4, evoker 1 |
| C.C.C. (`eugene:pillager_camp`, `pillager_loot_carriage` set) | template entities | camp 1 pillager + 1 vindicator; loot carriage 2 pillagers + ravager; golem van 4 pillagers + ravager; villager chase 5 pillagers |
| Ships: illager warship, viking longship, Blocky Siege pirate ship, Unwrecked pillager ship (`boats`, meta `pillager_boats`) | template entities (+ spawners), Unwrecked: pools `unwrecked_ships:sailors/pillager` (8 slots) / `pillager_captain` (1) | warship 15 pillagers, 3 vindicators, 2 illusioners; longship 3 pillagers, 5 vindicators, 1 evoker; pirate ship 2 spawners only |

Pool weights the pack can change without new files: every pool above is in
`kubejs/generation_scripts/worldgen/pool_config.json` (PoolTuner, `current` / `enabled` per element; it can't add
elements). New elements need a pool JSON override in `kubejs/data/<ns>/worldgen/template_pool/...` (the pack already owns
the Mysticrift, siege-weapon, IPO and `minecraft:pillager_outpost/base_plates` pool files) plus new 1x3x1 entity
templates; T&T's `kaisyn:outpost/pillagers/vindicator.nbt` is the model (size [1,3,1], one bottom jigsaw at [0,0,0]
`minecraft:bottom` -> `minecraft:empty`, the mob at [0,1,0], DataVersion 2975).

Mobs from templates keep `PersistenceRequired` 0, so non-patrolling pillagers and vindicators despawn like any monster
once the player is 128+ blocks away (vanilla `PatrollingMonster.removeWhenFarAway` returns true when not patrolling;
from memory). Structures repopulate only through their spawn override.

---

## pillagers #1: Pillager overhaul
Status Planned / Work data / KubeJS / TODO `mobs/pillagers #1` ([mob_spawns.md](../mob_spawns.md) 9 Later)

### Goal
(a) Lone pillagers roam dark forests, Mirage Isles, swamps, extreme ice and badlands. (b) Each pillager structure type
gets its own mix: siege equipment = pillagers + sometimes one vindicator; camps = pillagers + 0-2 vindicators; towers =
pillagers + vindicators + 0-1 evoker / illusioner; large towers and special structures their own. (c) Pillager spawners
removed (that is `worldgen/structures/contents #3`, briefed there).

### Now
- No pillager biome spawner anywhere (spawns_by_biome.md 7 Gaps: "patrols (PatrolSpawner), outposts (structure
  spawns)"). Pillager rule (spawns_by_biome.md 6): placement `ON_GROUND`,
  `PatrollingMonster.checkPatrollingMonsterSpawnRules` = block light 8 or less, **daylight is fine**, not Peaceful.
- mob_spawns.md 3 already shows a `lone_pillager` rule as the format example (test fixture, not in the pack file).
- Per-structure mobs: table above.

### Build
**1a. Lone pillagers** (`kubejs/generation_scripts/worldgen/mob_spawns.json`, after the last rule at line 200):
```json
{ "id": "lone_pillagers", "mob": "minecraft:pillager", "action": "set", "weight": 3, "min": 1, "max": 1,
  "category": "monster",
  "where": ["dark_oak", "swamp", "icy", "badlands"], "add": ["terralith:mirage_isles"],
  "label": "pillagers #1", "notes": "Lone pillagers in dark forests, the Mirage Isles, swamps, extreme ice, badlands." }
```
Traits (wild_crops.json): `dark_oak` = dark_forest, temperate_highlands; `swamp` = swamp, mangrove_swamp, ice_marsh,
orchid_swamp; `icy` = ice_spikes, frozen_peaks, frozen_cliffs, glacial_chasm; `badlands` = 8 biomes. Drop
`temperate_highlands` with `"except"` if only real dark forests are wanted.
Then decide daylight (Decisions). If "night only": either
- **(recommended) a `dark_only` spawn condition** in patcherMod `worldgen/SpawnConditions`: add `DARK_ONLY` to `Kind`
  (SpawnConditions.java 49-57, also the error text), a `case DARK_ONLY -> { if (!Monster.isDarkEnoughToSpawn(level,
  pos, level.getRandom())) return Boolean.FALSE; }` in `check` (108-127; returns FALSE or keeps `verdict`, so the
  pillager's own rule still runs), `CONDITIONS` in `mob_spawns.js` 41 and the message at 201, `CONDITIONS` in
  `tools/gen_spawn_table.py` 50 and its message at 422, a case in `tools/mob_spawns_test/rules_fixture.json`; then
  `{ "id": "lone_pillagers_dark", "condition": "dark_only", "mobs": ["minecraft:pillager"], "rules": ["lone_pillagers"] }`.
  Needs a patcherMod build.
- or, KubeJS only, `kubejs/server_scripts/mobs/pillagers.js`:
  `EntityEvents.checkSpawn('minecraft:pillager', e => { if (e.type == 'NATURAL' && e.level.isDay() && e.level.canSeeSky(e.block.pos)) e.cancel() })`
  (`EntityEvents.checkSpawn` and its `type` / `level` / `block` getters verified in `kubejs-forge-1902.6.2-build.73`
  `CheckLivingEntitySpawnEventJS`; it also fires for patrol and structure-box spawns, hence the `NATURAL` check).

**1b. Per-structure mix** (data; one tool script, see "Tooling" below):

| Structure | Target (TODO) | Change | Files |
|---|---|---|---|
| Siege weapons x6 | pillagers, sometimes 1 vindicator | per template two variants: A = every vindicator -> crossbow pillager; B = A + one vindicator kept; pool weight A 1 : B 1 | `kubejs/data/illager_siege_weapons/structures/*.nbt` (edit + add `_v` copies), `.../worldgen/template_pool/*.json` (second element) |
| T&T camps (beach, snowy beach, wooded badlands, sparse jungle), C.C.C. camp, Takes a Pillage camp | pillagers, 0-2 vindicators | T&T: copy the 4 camp base plates (+ the badlands/jungle side pieces) into `kubejs/data/kaisyn/structures/outpost/camps/...` with their grunt jigsaws pointed at a new pool `creativeengineering:pillagers/camp_grunt` (pillager 3 : vindicator 1 -> about 1.75 vindicators in a 7-slot camp); C.C.C. already 1 + 1, leave; TaP is code (leave, or a mixin on `ModLists.setupEntityLists`) | new pool json + template copies |
| T&T towers (15) and forts (9), vanilla / IPO outposts | pillagers, vindicators, 0-1 caster | grunt pool stays 1 : 1 (or #4's weights); caster via the captain pool (#3) | pool override `kubejs/data/kaisyn/worldgen/template_pool/outpost/pillagers/outpost_captain.json` |
| Mysticrift towers ("large towers, specials") | own mix | caster variants (#3); spawner removal leaves their template pillagers; contents #3 adds a spawn box | `kubejs/data/mysticrift_pillagers_towers_aris/...` |
| Ships | not in the TODO list | leave | - |

**Tooling.** Write `tools/pillager_templates.py` (import `port_structure_jars` for `nbt_read` / `nbt_write`; it keeps
byte-exact round trips): a table of edits `{template path: [ops]}`, ops `swap_entity(index or id, new_id, hand_items)`,
`add_entity(id, pos, nbt)`, `retarget_jigsaw(old_pool, new_pool)`, `clone_to(new path)`; reads jar templates from a
`--jar` path (T&T isn't in the repo) and ported ones from `kubejs/data`; `--check` mode for the offline test. Entity
templates for new pools: `--make-entity creativeengineering:pillagers/evoker minecraft:evoker` writes a 1x3x1 copy of
T&T's `vindicator.nbt` with the id swapped and `HandItems` cleared.

**Alternative to template copies (not recommended now):** a patcherMod structure processor
`creativeengineering:entity_swap` (`{from, to: weighted list, chance}`) using Forge's `StructureProcessor#processEntity`
hook (exists in Forge 1.19.2 as far as I know; unverified), added to the pool elements' `processors`. Random per
placement, no NBT editing, but can't do "at most one vindicator" and needs Java.

**1c.** `structures/contents #3`: see its brief.

Then: `python3 tools/check_worldgen_json.py`; rejoin; `/ceplace` the edited templates (structure_tuning.md 3.10) and
look; structure_tuning.md new subsection "3.20 Pillager garrisons" with the final table; mob_spawns.md 3 row for
`lone_pillagers`.

### Ids and data
- `minecraft:pillager`, `minecraft:vindicator`, `minecraft:evoker`, `minecraft:illusioner`; TaP entities
  `takesapillage:archer`, `takesapillage:skirmisher`, `takesapillage:legioner`, `takesapillage:clay_golem`
  (class names verified; ids from the registry names, check in the dump).
- Pools (verified in pool_config.json): `kaisyn:outpost/pillagers/grunt`, `kaisyn:outpost/pillagers/outpost_captain`,
  `kaisyn:outpost/pillagers/captive_jungle`, `.../captive_snow` (villager prisoners), `ipo:pillager_outpost/entities`,
  `takesapillage:pillager_camp/entities/illager`, `unwrecked_ships:sailors/pillager`, `.../pillager_captain`.
- TaP list SRG ids: `f_20513_` pillager, `f_20493_` vindicator, `f_20568_` evoker (all three matched against vanilla's
  `VillagerHostilesSensor` map TaP rebuilds); `f_20495_` read as witch (unverified).
- Item ids: none needed (crossbow for converted pillagers is `minecraft:crossbow`, written into template NBT, not a
  KubeJS literal).

### Decisions
- **Lone pillagers by day?** The pillager's rule allows daylight, and by day it's the only monster that can spawn, so
  in those biomes daytime monster picks that land on it all succeed; weight 3 of ~750 monster weight is about 0.4 % of
  picks. Recommendation: night only via `dark_only` (keeps them a dusk threat in badlands and ice); revisit after play.
- **Weight.** Recommendation 3 (mob_spawns.md example used 2).
- **Camps' vindicator cap.** Independent pool rolls can't cap at 2; a 3 : 1 camp pool gives about 1-2 on average.
  Recommendation: accept the average, or (exact) replace the camp grunt slots with fixed templates.
- **Takes a Pillage lists.** Recommendation: leave (they are that mod's flavour, evoker already 1 in 44).

### Gotchas
- **Bad Omen from lone pillagers:** natural pillagers become patrol captains with an ominous banner 6 % of the time
  (`PatrollingMonster.finalizeSpawn`, any reason but PATROL / EVENT / STRUCTURE; from memory). Killing one gives Bad
  Omen, so lone pillagers in dark forests start raids. Fine if wanted; else strip leaders in a KubeJS
  `EntityEvents.spawned` handler.
- Monster cap: lone pillagers and structure boxes share the 70-per-area monster cap with everything else.
- `tools/port_structure_jars.py` rewrites the ported folders (`kubejs/data/illager_siege_weapons`, Mysticrift) when
  re-run; the template tool must run after it (or the port tool calls it).
- Overriding a jar template by path (T&T `kaisyn:*`) hides later fixes from a T&T update; keep the tool's `--check`
  comparing against the jar.
- Evokers drop a Totem of Undying every time: never put them in a repeating spawn box (spawn overrides), only as
  one-off template mobs.

### Tests (MS- for the biome rule, ST- for structures; next free numbers)
- **MS-32 lone pillagers load:** rejoin -> rule `lone_pillagers` matched about 19 biomes; `/dumpspawns` dark_forest
  `monster` lists pillager 3 (1-1).
- **MS-33 lone pillagers in play:** a night in a dark forest / badlands -> single pillagers appear, never by day (with
  `dark_only`); none in plains.
- **ST-9x siege weapon mix:** `/ceplace` each siege weapon template twice (both variants) -> variant A has no
  vindicator, B exactly one; pillagers carry crossbows.
- **ST-9x camp mix:** 5 new T&T beach camps -> vindicators 0-3, mostly 1-2.

---

## pillagers #4: More vindicators
Status Planned / Work data / TODO `mobs/pillagers #4` (scratchpad 2026-10-19)

### Goal
"Vindicators take the pillager's place in single-mob spawn pools": where a spawn list or pool can only produce a
pillager, let it produce a vindicator some of the time.

### Now
Single-mob spawn sources that only give pillagers (verified):
- **All 28 T&T outpost structure jsons** (`data/towns_and_towers/worldgen/structure/pillager_outpost_*.json` and
  `exclusives/pillager_outpost_*.json` in the T&T jar): `"spawn_overrides": {"monster": {"bounding_box": "full",
  "spawns": [{"type": "minecraft:pillager", "minCount": 1, "maxCount": 2, "weight": 1}]}}`. This is the continuous
  respawn of every T&T outpost.
- Vanilla `minecraft:pillager_outpost` spawn override (pillager only; vanilla data, from memory).
- Pools `unwrecked_ships:sailors/pillager` (8 slots on the Unwrecked pillager ship), and the spawn boxes
  `structures/contents #3` adds (pillager only, per its brief).
- Already mixed: `kaisyn:outpost/pillagers/grunt` (1 : 1), `ipo:pillager_outpost/entities` (vindicator only).
- `lone_pillagers` (#1a) would be one more.

### Build
1. **T&T spawn overrides:** copy the 28 structure jsons from the jar into
   `kubejs/data/towns_and_towers/worldgen/structure/` (same paths, `exclusives/` included) with
   `"spawns": [{"type": "minecraft:pillager", "weight": 3, "minCount": 1, "maxCount": 2},
   {"type": "minecraft:vindicator", "weight": 1, "minCount": 1, "maxCount": 1}]`. Generate them (a 20-line addition to
   `tools/pillager_templates.py`, `--spawn-overrides`), don't hand-edit; nothing else in the pack writes these files
   (structure_overrides.js writes structure *sets*; biome fits and terrain adaptation act at runtime).
2. **Vanilla outpost:** `kubejs/data/minecraft/worldgen/structure/pillager_outpost.json` (copy of vanilla 1.19.2's, from
   the client jar) with the same two-entry list.
3. **Contents #3 boxes:** give its new spawn boxes the same two entries.
4. **Unwrecked sailors:** `pool_config.json` can only reweight; add a vindicator element via a pool override
   `kubejs/data/unwrecked_ships/worldgen/template_pool/sailors/pillager.json` pointing at a new
   `creativeengineering:pillagers/vindicator` entity template (or reuse `kaisyn:outpost/pillagers/vindicator`, which is
   loaded anyway).
5. **Lone pillagers:** a second rule `lone_vindicators` (same biomes, weight 1, category monster) if wanted.
6. `python3 tools/check_worldgen_json.py`; rejoin; structure_tuning.md note.

### Ids and data
- Structure json keys `spawn_overrides.monster.{bounding_box, spawns[type, weight, minCount, maxCount]}` (verified in the
  T&T jar). `minecraft:vindicator` (vanilla).

### Decisions
- **Share** (TODO Decisions). Recommendation: pillager 3 : vindicator 1 in the boxes; grunt pool stays 1 : 1 (already
  half vindicators in every T&T building).
- **Which reading of "single-mob spawn pools".** Recommendation: the spawn overrides and the Unwrecked sailors pool
  (both pillager-only today); the grunt pool is already mixed.

### Gotchas
- **Vindicators follow `Monster.checkMonsterSpawnRules`** (dark, block light 0; spawns_by_biome.md 6, placement
  `NO_RESTRICTIONS`): in a box they spawn only at night or indoors, while pillagers also spawn by day. So the real share
  by day stays all pillagers; at night the 3 : 1 holds. That is a reasonable split; say so in the writeup.
- A spawn override replaces the box's whole monster list (no zombies or creepers inside), as now.

### Tests
- **ST-9x outpost box mix:** at night inside a T&T tower outpost, kill the garrison and wait -> new pillagers and
  occasional vindicators; by day only pillagers.

---

## pillagers #3: Evokers and illusioners in some pillager structures
Status Planned / Work data / TODO `mobs/pillagers #3` (scratchpad 2026-10-19; "part of row 1 but can go first")

### Goal
One spell-caster now and then in the harder, rarer pillager structures.

### Now
Casters today: the illager warship (2 illusioners), the viking longship (1 evoker), Takes a Pillage camp / bastille
(evoker 1 in 44 / 1 in 52 per mob feature). None in T&T outposts or the Mysticrift towers; vanilla mansions have
evokers (vanilla).

### Build
1. **Entity templates:** `creativeengineering:pillagers/evoker` and `creativeengineering:pillagers/illusioner` (1x3x1, from
   T&T's `vindicator.nbt` with the id swapped and `HandItems` empty for the evoker; template mobs are loaded from NBT
   and skip `finalizeSpawn`, so the illusioner needs `HandItems: [{id: "minecraft:bow", Count: 1}, {}]` written in), and
   "captain + caster" templates `creativeengineering:pillagers/captain_evoker`, `captain_illusioner` = T&T's
   `outpost_captain.nbt` plus the caster entity in the same 1x3x1 space (template path
   `kubejs/data/creativeengineering/structures/pillagers/*.nbt`).
2. **T&T towers and forts (every T&T outpost with a captain):** pool override
   `kubejs/data/kaisyn/worldgen/template_pool/outpost/pillagers/outpost_captain.json`:
   captain 3, captain_evoker 1, captain_illusioner 1 (40 % of outposts get one caster; the 3 outposts with two captain
   slots can get two). Camps keep plain captains only if #1b's camp copies point their captain jigsaw at a plain pool;
   otherwise camps get casters too (acceptable for a first pass).
3. **Mysticrift towers ("hard, unique"):** per tower, a variant template `<tower>_caster.nbt` with one plain pillager
   entity swapped to an evoker (towers 1, 3, 5, 7 by list order) or illusioner (the others); pool json (already in
   `kubejs/data/mysticrift_pillagers_towers_aris/worldgen/template_pool/`) gets the variant at weight 1 next to the
   original at weight 1. Keep the `block_ignore` processor on the new element.
4. `/ceplace` the variants; structure_tuning.md table.

### Ids and data
- Pools above verified in pool_config.json; Mysticrift pool format verified
  (`kubejs/data/mysticrift_pillagers_towers_aris/worldgen/template_pool/oak_wood_pillager_tower.json`).
- T&T captain template: one pillager, `PatrolLeader: 1`, ominous banner in the head slot (verified).

### Decisions
- **Which structures** (TODO). Recommendation: the 8 Mysticrift towers (large and rare; the End one is 1 in 232 End
  highland chunks, structures_by_biome.md) and T&T forts; T&T towers via the shared captain pool at the same rate unless the
  user wants forts only (then copy the 9 fort base plates and point one grunt jigsaw at a caster pool).
- **Evoker vs illusioner split.** Recommendation: even; illusioners are otherwise only on the warship.

### Gotchas
- Totems: each evoker is a guaranteed Totem of Undying; at 40 % of 28 T&T outposts per region that is a noticeable
  totem source. Lower the caster weight if totems become common.
- Vexes fly through walls, so a tower with an evoker is a much harder fight from outside; intended, but keep casters
  out of the small camps.

### Tests
- **ST-9x casters:** `/ceplace` the captain pool and a Mysticrift caster variant -> one evoker / illusioner present;
  5 new T&T outposts -> about 2 with a caster.

---

## pillagers #2: End pillagers in diving gear
Status Planned / Work KubeJS / TODO `mobs/pillagers #2` (scratchpad 2026-10-19 "pillager towers: ensure end pillagers
spawn with diving gear")

### Goal
Pillagers in the End (the Mysticrift End tower) wear Create diving gear, as if they came through the void in suits.

### Now
- `mysticrift_pillagers_towers_aris:end_pillager_tower` (template `end_new_pillager_tower`, 15x23x15): **no template
  mobs**, 3 pillager spawners (+ 2 spawners with an empty entity that spawn nothing), item frames. It generates on the
  End's outer islands (structure_tuning.md ST-80; `structures_by_biome.md`: end_highlands 1/232). After
  `structures/contents #3` removes pillager spawners it has **no pillagers at all** unless that brief's spawn box is
  also given to the End tower (its step 3 says "the End one too if wanted"): it must be, for this row.
- No other pillager source in the End (patrols are Overworld only; raids need a village).
- Gear ids in item_dict.js: `i.copper_diving_helmet` (178), `i.copper_backtank` (176), `i.copper_diving_boots` (177);
  netherite set at 4808-4811.
- KubeJS `EntityEvents.spawned` fires when an entity is added to a level, **also when loaded from disk**
  (`EntitySpawnedEventJS` @Info, verified in the KubeJS jar); type filter via the first argument.

### Build
1. Make sure contents #3 gives `kubejs/data/mysticrift_pillagers_towers_aris/worldgen/structure/end_pillager_tower.json`
   a spawn box (`monster`, `full`, pillager; `"spawn_overrides": {}` today).
2. New `kubejs/server_scripts/mobs/end_illagers.js`:
   ```js
   // End illagers wear Create diving gear (TODO mobs/pillagers #2). Once per mob: a persistentData flag.
   const i = global.item
   const END_DIVERS = ['minecraft:pillager', 'minecraft:vindicator', 'minecraft:evoker', 'minecraft:illusioner']
   END_DIVERS.forEach(type => EntityEvents.spawned(type, e => {
   	const mob = e.entity
   	if (e.level.dimension != 'minecraft:the_end' || mob.persistentData.getBoolean('ce_end_gear')) return
   	mob.persistentData.putBoolean('ce_end_gear', true)
   	if (!mob.isPatrolLeader()) mob.setHeadArmorItem(i.copper_diving_helmet)   // keep a captain's banner
   	mob.setChestArmorItem(i.copper_backtank)
   	mob.setFeetArmorItem(i.copper_diving_boots)
   	;['head', 'chest', 'feet'].forEach(s => mob.setDropChance(s, 0))           // see Decisions
   }))
   ```
   `setDropChance` takes an `EquipmentSlot`; Rhino should convert the string, else use
   `Java.loadClass('net.minecraft.world.entity.EquipmentSlot').HEAD` etc. `setHeadArmorItem` and friends are KubeJS
   LivingEntity helpers (verify names in ProbeJS; fall back to `setItemSlot(EquipmentSlot.HEAD, Item.of(...))`).
   Ids from `global.item`, per CLAUDE.md; a pack entity tag (`creativeengineering:end_divers` via `global.tags` in
   `tags/entitytags.js`) instead of the literal list once `mobs/tweaks #4` (entity dictionary) exists.
3. Writeup: a line in mob_spawns.md 9 / structure_tuning.md where the End tower is described.

### Ids and data
- `create:copper_diving_helmet`, `create:copper_backtank`, `create:copper_diving_boots` (item_dict.js, verified).
- Dimension id `minecraft:the_end`.

### Decisions
- **Copper or netherite.** Recommendation: copper (theme; netherite gear as a drop would be loot).
- **Drop chances.** Recommendation: 0 for all three (spawn-box pillagers respawn forever; a free backtank per kill
  breaks Create's progression). Or helmet only at the vanilla 8.5 %.
- **All End illagers or only the tower's.** Recommendation: all in the End (the tower is the only source anyway).

### Gotchas
- The handler also runs for mobs loaded from disk; the flag keeps it to once, and mobs that already exist in the End
  get gear the next time they load (fine).
- A pillager wearing a backtank shows the backtank model; armour-points: Create's diving gear gives 1-2 armour, so End
  pillagers get a little tougher.
- Captains wear the ominous banner in the head slot; overwriting it would remove the raid captain. The sketch skips the
  helmet for captains.

### Tests
- **MS-34 End divers:** `/execute in minecraft:the_end run summon minecraft:pillager ~ ~ ~` -> it has the copper helmet,
  backtank and boots; in the Overworld it doesn't. Kill it -> no gear drops (drop chance 0).
- **MS-35 End tower garrison:** locate the End tower (ST-80 command), wait at night -> pillagers spawn in the box,
  all in diving gear.

---

## Built, needs play

None (no Testing / Complete rows in this subfamily).

## Writeup corrections made

None in this file's scope (the mob_spawns.md fixes are listed in [mobs_spawns.md](mobs_spawns.md)).
