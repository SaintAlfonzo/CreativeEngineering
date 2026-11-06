# mobs / spawns: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# mobs`, Sub `spawns`. Feature writeup: [mob_spawns.md](../mob_spawns.md)
(test prefix `MS-`), generated report [spawns_by_biome.md](../spawns_by_biome.md). Related briefs:
[mobs_pillagers.md](mobs_pillagers.md) (lone pillagers use the same rule file), [mobs_bees.md](mobs_bees.md),
[worldgen_bee-nests.md](worldgen_bee-nests.md), [structures_contents.md](structures_contents.md).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | Plans after the first block | Planned | **nothing left**: its Details are the first block, built as #1 (see #2 below) |
| #0 | Fresh `/dumpspawns` with the 12 rules (prerequisite, = MS-13/MS-14 of #1) | - | one game session |
| #3 | Alex's Mobs and duck / goose parity | Planned | medium: analysis + 5-15 rules in `mob_spawns.json`, no code |
| #4 | Lone villagers | Planned | small: 1 rule + 1 condition, no code |
| #5 | Allay spawns | Planned | small to medium: 1 rule + 1 condition (biomes), pool weight edits (structures) |
| #6 | Fireflies | Planned | medium: KubeJS block + server interaction; the Fireflies mod is not loaded |

Build order:

1. **Dump first.** Every number in spawns_by_biome.md is from the 2026-10-10 02:58 dump, which ran **0 rules** (the
   report's own WARNING, line 11). Rejoin, `/dumpspawns`, `python3 tools/gen_spawn_table.py`; that also ticks MS-13 and
   MS-14 of #1. #3 needs the real post-tuning shares; #4 and #5 need the per-biome creature totals.
2. **#4 and #5 together** (one rule-file edit, one rejoin): both are "add a mob that has no biome spawner, with
   `category: creature` and a `light_only` condition".
3. **#3** after a play session with the first block, so the targets are judged against what players see.
4. **#6** on its own (KubeJS startup + server scripts, a restart for the new block; assets).

Rules for lone pillagers (`mobs/pillagers #1`) and vindicators (`#4`) go into the same `mob_spawns.json`; batch them
with #4/#5 if that row is ready. Depends on nothing outside this family except `mobs/tweaks #4` (entity dictionary,
see Gotchas).

---

## spawns #2: Plans after the first block
Status Planned / Work data / TODO `mobs/spawns #2` ([mob_spawns.md](../mob_spawns.md) 9)

### Goal
The row's Details (mooshrooms in dark forests and Mirage Isles, every dirt spawnable, farm animals by biome type, shiba
off, birds in light, land animals light + pathfind check, ducks and geese on beaches / lakes / ponds / swamps) are the
**first block** of mob_spawns.md 9, word for word.

### Now
All of it is built and live (1.0.6+, DEPLOYED 1.0.9): the 12 rules in
[mob_spawns.json](../../kubejs/generation_scripts/worldgen/mob_spawns.json) 47-200 (`mooshroom_dark_forests` 49 ...
`goose_ponds` 189), the 3 conditions 202-245 (`flying_birds`, `chicken_shores`, `waterfowl_ponds`), `spawn_ground`
247 (`#minecraft:dirt` into the three animal ground tags), the walk-value mixins `AnimalWalkTargetMixin` /
`MushroomCowWalkTargetMixin`, Quark `Shiba = false`. Tests MS-13 to MS-23 cover them; that is row #1 (Testing).
The old TODO (before 2026-10-19) had one row "Mob spawn plans beyond the first block (Alex's Mobs / duck parity,
pillager overhaul, lone villagers, allay, skylands mooblooms)"; the rework split those into spawns #3-#5,
pillagers #1-#2 and mooblooms #4, and #2 kept the first-block text by mistake.

### Build
Nothing. Delete the row (or fold its Decisions "shares" into #3, which is where the share tuning lives).

### Decisions
- **Delete or repurpose.** Recommendation: delete; "retune shares after play" is #3's job.

---

## spawns #0 (prerequisite): a dump with the rules

Not a TODO row; listed so a session doesn't build on stale numbers.

1. Join the world with the current pack (patcherMod 1.0.5+ has `SpawnTuning`), check latest.log:
   `[mob-spawns] 12 of 12 rules and 3 of 3 conditions` (MS-13).
2. `/dumpspawns` (or `/cegen run spawns_by_biome dumps`), then `python3 tools/gen_spawn_table.py`; the WARNING lines at
   the top of spawns_by_biome.md (lines 11 and 30) must be gone (MS-14). Commit the regenerated report.
3. Then `python3 tools/gen_spawn_table.py --mob <id>` gives any one mob's table for the rows below.

---

## spawns #3: Alex's Mobs and duck / goose parity
Status Planned / Work data / TODO `mobs/spawns #3` ([mob_spawns.md](../mob_spawns.md) 9 "Investigate", 10)

### Goal
Make Alex's Mobs (AM) animals and ducks / geese spawn at shares that sit sensibly next to the farm animals in each
biome type: no single wild animal swamping a biome's creature picks, waterfowl as common as the habitat suggests.

### Now
- Analysis (no tuning applied at the time): mob_spawns.md 10.1 findings, 10.3 per-animal rules, 10.4 shares by biome
  type, 10.5 per-biome weights, 10.7 AM biome selectors. Key numbers: AM weights next to the farm block (sheep 12 /
  pig 10 / chicken 10 / cow 8): gazelle 40, elephant 30, tiger 30, capuchin 28, mudskipper 28, kangaroo 25, gorilla 25,
  rhinoceros 24, toucan 23. In savannas gazelle takes 19 %, elephant 15 %, rhino 12 % of creature picks (10.4); in
  badlands kangaroo 34 %, emu 27 %; seal 39 % on beaches.
- Since then the first block changed the farm-animal side (pig x2 forests, cow x2 plains, rabbits in savannas, chickens
  on shores and in the sky) and added ducks / geese: `duck_wetlands` 6 / `goose_wetlands` 4 (swamps, beaches; rules at
  mob_spawns.json 148, 162), `duck_ponds` 4 / `goose_ponds` 3 in 74 grassy land biomes, water only (176, 189;
  condition `waterfowl_ponds` 233). River weights are the mod's own (duck 6, goose 4).
- AM weights live in two places: `config/alexsmobs.toml` (`<mob>SpawnWeight`, `<mob>SpawnRolls`; mirrored in
  `configs_edited/root_config/alexsmobs.toml`, identical today) and its biome lists in `config/alexsmobs/<mob>_spawns.json`.
  The pack's `scale` / `set` rules run after AM's modifiers (`AFTER_EVERYTHING`), so either place works.

### Build
1. Do #0. From the new spawns_by_biome.md section 3 (creatures by biome type) and 5 (per biome), write down for each
   biome type the share of: the farm block, the biggest single AM animal, ducks + geese.
2. Pick targets (see Decisions) and write `scale` rules, one per AM animal or per group, e.g.:
   ```json
   { "id": "am_savanna_herds", "mob": "alexsmobs:gazelle", "action": "scale", "factor": 0.5,
     "where": ["savanna", "shrubland"], "label": "spawns #3", "notes": "gazelle 40 -> 20: was 19 % of savanna picks" },
   { "id": "am_elephant", "mob": "alexsmobs:elephant", "action": "scale", "factor": 0.5, "where": ["savanna"] }
   ```
   `factor` may be fractional; SpawnTuning rounds half up, minimum 1 (mob_spawns.md 3). One rule per mob keeps the
   report readable; rule ids are `[a-z0-9_]`.
3. Waterfowl: compare duck / goose shares on rivers (mod weights 6 / 4) with the wetland (6 / 4) and pond (4 / 3) rules.
   The ratio is 3:2 everywhere except ponds (4:3); weights are integers, so ponds can only become 6 / 4 (more birds) or
   stay. Decide what "parity" means first (Decisions), then adjust `duck_ponds` / `goose_ponds` weights.
4. Preview offline: edit the JSON, run `python3 tools/gen_spawn_table.py` (applies the rules to the last dump), read
   section 1 and 3; `bash tools/test_mob_spawns.sh` (needs `./gradlew :creativeCommon:compileJava` once).
5. Rejoin, `/dumpspawns`, regenerate, commit; add the rules to mob_spawns.md 3 "Rules now" and a History line.

### Ids and data
- Entity ids from the dump (verified in spawns_by_biome.md section 4): `alexsmobs:gazelle`, `alexsmobs:elephant`,
  `alexsmobs:rhinoceros`, `alexsmobs:kangaroo`, `alexsmobs:emu`, `alexsmobs:tiger`, `alexsmobs:capuchin_monkey`,
  `alexsmobs:gorilla`, `alexsmobs:toucan`, `alexsmobs:seal`, `untitledduckmod:duck`, `untitledduckmod:goose`.
- Traits for `where`: wild_crops.json `biomes` table (savanna 11 biomes, shrubland 5, badlands 8, beach 3, river 3,
  swamp 4, jungle 9, plains 14, forest 50).

### Decisions
- **Target.** Recommendation: no single non-farm creature above the biome's largest farm animal share x 1.5, except
  where the biome has no farm animals (badlands, beaches: the AM animals are the fauna there; leave them). In practice:
  halve gazelle, elephant, rhinoceros in savannas; capuchin and gorilla in jungles x0.6; leave predators (their own AM
  rolls already gate them).
- **Tune in `mob_spawns.json` or in `alexsmobs.toml`?** Recommendation: `mob_spawns.json`. One file, shown in the
  report with "(was N)", reversible with CEConfig `mobSpawns = false`; the toml would need both copies kept in sync.
- **What "duck / goose parity" means.** Two readings: (a) duck vs goose ratio (now 3:2 on rivers and wetlands, 4:3 on
  ponds), (b) waterfowl vs the habitat's other animals. Recommendation: (b), and leave the ratio; also check that
  `nests #3` (nests in duck / goose / emu habitats) uses the same biome lists.

### Gotchas
- Shares are of the creature category only, and creatures mostly come from chunk-generation herds (10.1 "Where the
  herds come from"), so changes show in new chunks; old chunks keep their herds.
- AM animals with a `SpawnRolls` above 1 (sunbird 1 in 15) are already rarer than their weight says.
- `scale` never adds a mob; a mob that is missing from a biome needs `set`.
- More Mooblooms converts cows after the pick (mob_spawns.md 3.1 Interplay): cow changes move moobloom numbers.

### Tests
- **MS-24 parity rules load:** rejoin -> `[mob-spawns] N of N rules`, no errors; spawns_by_biome.md section 1 lists the
  new rules with biomes matched > 0.
- **MS-25 savanna mix:** new savanna chunks -> herds of cows/sheep/pigs/rabbits appear alongside gazelles and elephants,
  not mostly gazelles (compare 3 fresh savanna areas).
- **MS-26 waterfowl:** a swamp, a plains lake and a beach each show ducks and geese within a few hundred blocks.

---

## spawns #4: Lone villagers
Status Planned / Work data / TODO `mobs/spawns #4` ([mob_spawns.md](../mob_spawns.md) 9 Later)

### Goal
Now and then a single villager wanders the open countryside: plains, temperate hills, fields and meadows.

### Now
- `minecraft:villager` has no biome spawner (spawns_by_biome.md 7 Gaps: "villages"). Its category is **misc**
  (spawns_by_biome.md 6 line 6664: `misc`, placement `ON_GROUND`, rule `Mob.checkMobSpawnRules`: spawnable block below,
  any light). Misc never spawns naturally (mob_spawns.md 2.1), so a `set` without `category` would add a dead entry.
- MCA: `config/mca.json` `"overwriteOriginalVillagers": true` (line 4): a vanilla villager that joins the world becomes
  an MCA villager (`mca:male_villager` / `mca:female_villager`). So the rule should name `minecraft:villager` and let MCA
  convert it (unverified at which point MCA converts natural spawns; check in game, Tests).
- SpawnTuning `set` adds the mob to `category` with group 1-1 unless given (mob_spawns.md 3 table).

### Build
1. Add to `mob_spawns.json` `rules` (after `goose_ponds`, line 200):
   ```json
   { "id": "lone_villagers", "mob": "minecraft:villager", "action": "set", "weight": 1, "min": 1, "max": 1,
     "category": "creature",
     "where": ["temperate plains", "temperate grassy mountain !forest !rocky", "temperate grassy shrubland"],
     "label": "spawns #4", "notes": "A lone villager in open temperate country. Creature category: misc never spawns." }
   ```
   Checked against wild_crops.json: these clauses match exactly plains, sunflower_plains, meadow, blooming_plateau,
   steppe, valley_clearing (meadow and blooming_plateau are "temperate grassy plains mountain"; the hills clause adds
   nothing today, and `yosemite_cliffs` is excluded by `!rocky`). For more "temperate hills" add ids by hand
   (e.g. `terralith:highlands` is cold, so it needs `add`). Confirm in the generator's section 1.
2. Add a condition so they never spawn in caves or dark spots (the villager's rule allows any light):
   ```json
   { "id": "lone_villagers_daylight", "condition": "light_only", "mobs": ["minecraft:villager"], "rules": ["lone_villagers"] }
   ```
   `light_only` replaces the rule with "light above 8" (still on the ground: placement `ON_GROUND`).
3. Generator preview, `bash tools/test_mob_spawns.sh`, rejoin, `/dumpspawns`, regenerate.
4. mob_spawns.md 3 "Rules now" + 3.1 "Conditions now" rows; strike the line in section 9 Later.

### Ids and data
- `minecraft:villager` (vanilla). MCA ids `mca:male_villager`, `mca:female_villager` (from spawns_by_biome.md 7, i.e. the
  dump). Traits: wild_crops.json.

### Decisions
- **Weight.** A plains creature list totals about 90 after the first block (sheep 12, pig 10, chicken 10, cow 16,
  horse 5, donkey 1, duck 4, goose 3, raccoon 10, crow 10, bison 9). Weight 1 = about 1 % of picks, mostly at chunk
  generation (herd chance 0.1 per roll). Recommendation: 1; raise to 2 after play if nobody meets one.
- **Name `minecraft:villager` or an MCA id?** Recommendation: vanilla id; MCA converts it, and the rule keeps working
  if MCA goes.

### Gotchas
- Villagers count toward the creature cap (10 per player area); a lone villager is persistent once spawned.
- A villager far from a village has no bed or job site and is zombie food at night; that is the flavour, but MCA may
  try to give it a home (it can claim beds in player bases). Check in play.
- MCA's `overwriteOriginalZombieVillagers` is also on; nothing to do here.

### Tests
- **MS-27 lone villager rule:** rejoin -> rule `lone_villagers` matched N biomes; `/dumpspawns` -> plains `creature` lists
  `minecraft:villager` 1 (1-1).
- **MS-28 lone villager in play:** explore new plains / meadow chunks in daylight -> occasionally one MCA villager alone,
  never in a cave; it is an MCA villager (name tag, MCA interaction screen), not a vanilla one.

---

## spawns #5: Allay spawns
Status Planned / Work data / TODO `mobs/spawns #5` ([mob_spawns.md](../mob_spawns.md) 9 Later; scratchpad 2026-10-19
"more allay spawns in pillager cages, mystical biomes and structures")

### Goal
Allays are rare today; make them findable: in pillager cages, in "mystical" biomes, and in more structures.

### Now
Verified by reading every structure template (scratchpad script over `kubejs/data` and the T&T, IPO, Takes a Pillage,
Blocky Siege, Villages & Pillages and Unwrecked Ships jars):
- **Towns & Towers outposts already hold allays**: 1-2 template allay entities in the base plate of 26 of the 28
  outposts (camps beach, snowy_beach; all 9 forts; all 15 towers; both ocean ships). Missing: `camps/wooded_badlands`
  and `camps/sparse_jungle`.
- **Vanilla outpost** (`minecraft:pillager_outpost`): the cage piece `minecraft:pillager_outpost/feature_cage_with_allays`
  (3 allays) is weight 1 of 14 in pool `minecraft:pillager_outpost/features` (6 empty + 8 pieces); IPO ships its own
  copy of that template. 7 of 8 outposts use the vanilla plate (pool `minecraft:pillager_outpost/base_plates`, IPO 1 :
  vanilla 7, [structure_tuning.md](../structure_tuning.md) 3.12).
- **Mysticrift oak tower** (`mysticrift_pillagers_towers_aris:oak_wood_pillager_tower_new`): 3 allays. The other 7
  Mysticrift towers have iron golems or villagers instead.
- Takes a Pillage cages (`takesapillage:pillager_camp/cages`) hold a "prisoner" from a hard-coded list (iron golem 4,
  villager 3, clay golem 3; `ModLists.PRISONER_LIST`, decompiled): no allays without a mixin.
- No biome spawner (spawns_by_biome.md 7: "pillager outposts, woodland mansions (placed)"). The allay is category
  creature; it has no `SpawnPlacements` entry, so placement is `NO_RESTRICTIONS` and the rule always passes (vanilla
  behaviour from memory; the dump's section 6 lists only mobs with a spawner, so check `spawns.json` `entities` after
  adding the rule).

### Build
1. **Mystical biomes (rule + condition):**
   ```json
   { "id": "allay_mystical", "mob": "minecraft:allay", "action": "set", "weight": 3, "min": 1, "max": 2,
     "category": "creature",
     "add": ["terralith:moonlight_grove", "terralith:moonlight_valley", "terralith:mirage_isles",
             "terralith:amethyst_rainforest", "terralith:amethyst_canyon", "terralith:lavender_forest",
             "terralith:lavender_valley"],
     "label": "spawns #5", "notes": "Allays in Terralith's magical biomes." }
   ```
   and a `light_only` condition `allay_daylight` with `"rules": ["allay_mystical"]`, so the `NO_RESTRICTIONS`
   placement can't put one in a dark cave pocket below the surface.
2. **Pillager cages (pool weights, `pool_config.json`):** in
   `kubejs/generation_scripts/worldgen/pool_config.json`, pool `minecraft:pillager_outpost/features`, raise
   `minecraft:pillager_outpost/feature_cage_with_allays` `current` 1 -> 3 (1 in 14 -> 3 in 16 per feature roll). PoolTuner
   (patcherMod `worldgen/PoolTuner`) applies it at world load; no new files.
3. **More structures (template entities):** the two T&T camps without allays, and the 7 Mysticrift towers without
   them, would need edited template copies (same tooling as `mobs/pillagers #1` and `#3`: copy the template NBT under
   `kubejs/data/<ns>/structures/...` and append an allay entity). Do this with the pillagers overhaul so each template
   is copied once. List the edits in mobs_pillagers.md's template table.
4. Generator preview, tests, rejoin, dump; mob_spawns.md 3 rows; structure_tuning.md note for the pool weight.

### Ids and data
- `minecraft:allay`; pool `minecraft:pillager_outpost/features`, piece `minecraft:pillager_outpost/feature_cage_with_allays`
  (verified in pool_config.json). Terralith biome ids above are in wild_crops.json (all seven verified in its
  `biomes` table).
- Template allay counts above: verified (NBT read with `tools/port_structure_jars.py`'s reader).

### Decisions
- **Which biomes are "mystical".** Recommendation: Terralith moonlight, lavender, amethyst and Mirage Isles (above);
  not the skylands (they get mooblooms, `mobs/mooblooms #4`) and not cherry (sakura) groves.
- **Weight.** Recommendation 3, groups 1-2: rare (a few % of creature picks there) but findable.

### Gotchas
- Allays duplicate with amethyst shards and dance to jukeboxes; more allays = more duplication. Fine, but say so in the
  writeup.
- Supplementaries' jar catches allays (`supplementaries:jar_catchable` includes `minecraft:allay`, verified in
  `supplementaries-1.19.2-2.4.20.jar`): players will bottle them, which is a plus.
- Template allays in T&T are saved with `PersistenceRequired` 0; allays are creature category, so they don't
  despawn anyway (unverified for allays specifically; watch in play).

### Tests
- **MS-29 allay rule:** `/dumpspawns` -> moonlight_grove `creature` lists `minecraft:allay` 3 (1-2).
- **MS-30 allays in play:** a moonlight grove in daylight -> allays appear over a few minutes of exploring new chunks.
- **MS-31 outpost cages:** `/locate structure minecraft:pillager_outpost` x3 in new chunks -> at least one allay cage
  (the generated pool_tag_report.md shows the new weight).

---

## spawns #6: Fireflies
Status Planned / Work data (KubeJS in fact) / TODO `mobs/spawns #6` (scratchpad 2026-10-19 "verify firefly mod works,
allow catching fireflies with jar?"; scratchpad 2026-10-21 "jar fireflies when near bush at night")

### Goal
Fireflies the player notices at night near firefly bushes, and a way to catch them in a jar.

### Now
- **The Fireflies mod is not loaded.** `Fireflies Mod-1.0.1 (Forge 1.19.2).jar` (modid `fireflies`) is in
  `disabledmods/` (pack/MODLIST.md "Disabled mods", manifest.json 8532). Decompiled: it is particles only. Its
  `Spawing2Procedure` (a `LevelTickEvent`) puts firefly particles above each player who stands on two block types in
  swamp / mangrove swamp when `getDayTime()` is 13000-23000; `getDayTime()` is the absolute world time, so after the
  first in-game day it never fires again (a bug). Its only entity, `fireflies:firefly_cluser` (Monster category), spawns
  particles and discards itself in `finalizeSpawn`. Nothing to catch.
- **The Firefly Bush port is loaded:** `fireflybushport:firefly_bush` (`fireflybush-1.19.2.jar`), a `FlowerBlock`
  subclass, generated in rivers and swamps (`data/fireflybushport/forge/biome_modifier/firefly_bush_biome_modifier.json`,
  `forge:add_features`, `fireflybushport:firefly_bush`). At night (`!level.isDay()`) its block tick sends 1
  `fireflybushport:fireflie` particle (server-side `sendParticles`) and **reschedules itself every tick**
  (`scheduleTick(pos, block, 0)`), with a random bush sound. Decompiled `FireflyBushUpdateTickProcedure`.
- Jar: `supplementaries:jar` (`global.item.jar`, item_dict.js 3632). Supplementaries catches entities in jars
  (`supplementaries:jar_catchable` tag); a firefly is not an entity here, so a plain catch can't work.
- No item_dict key for the bush (needs `i.firefly_bush = 'fireflybushport:firefly_bush'`).

### Build
1. **Keep the Fireflies mod disabled** (it does nothing after day 1); say so in the row and mod_sides.md.
2. **Firefly jar block** (KubeJS startup), in a new `kubejs/startup_scripts/items/firefly_jar.js`, after
   `items/decor_blocks.js`'s pattern (creativeengineering namespace, hand-authored models under
   `kubejs/assets/creativeengineering/`):
   ```js
   StartupEvents.registry('block', e => {
   	e.create('creativeengineering:firefly_jar')
   		.displayName('Firefly Jar')
   		.material('glass').soundType('glass')
   		.hardness(0.3).lightLevel(0.6)          // KubeJS 6: 0-1 float, 0.6 ~ light 9
   		.renderType('cutout')
   		.box(5, 0, 5, 11, 9, 11)                // jar-sized hitbox (check the Supplementaries jar model's box)
   		.model('creativeengineering:block/firefly_jar')
   })
   ```
   Add `i.firefly_jar = 'creativeengineering:firefly_jar'` to item_dict.js, a lang name if displayName isn't enough,
   a texture (jar with glowing dots; the Supplementaries jar texture is the base), and client particles if wanted
   (KubeJS 6 has no `animateTick` builder; skip, the light is the effect).
3. **Catching** (server, `kubejs/server_scripts/mobs/fireflies.js`, new folder or next to other mob scripts):
   ```js
   // Right-click a firefly bush at night with an empty Supplementaries jar -> a firefly jar.
   const i = global.item
   BlockEvents.rightClicked(i.firefly_bush, e => {
   	const { player, item, level } = e
   	if (item.id != i.jar || item.nbt) return            // an empty jar only (a jar with contents has NBT)
   	if (level.isDay()) { player.statusMessage = Text.gray('The fireflies only come out at night'); return }
   	item.count--
   	player.give(i.firefly_jar)
   	level.playSound(null, e.block.pos, 'minecraft:item.bottle.fill', 'blocks', 1, 1.2)
   	e.cancel()
   })
   ```
   Optional cooldown per bush (block entity-less: keep a `Map` of `pos -> gameTime` in the script, or accept unlimited
   jars, since the bush is the limiter).
4. **Recipe:** none needed (the catch is the source). If a fallback is wanted: `NCH.shapeless` jar + glow berries? Not
   recommended.
5. Writeup: a short section in mob_block_tweaks.md (or a new "fireflies" paragraph in flowers.md, where the bush lives);
   TODO Work column -> KubeJS.

### Ids and data
- `fireflybushport:firefly_bush` (verified, jar blockstate and lang), particle `fireflybushport:fireflie` (verified).
- `supplementaries:jar` (verified in item_dict.js and the jar). `fireflies:firefly_cluser` (verified, disabled mod).
- New: `creativeengineering:firefly_jar` (block + item).

### Decisions
- **Re-enable the Fireflies mod?** Recommendation: no (particle-only and broken after the first night).
- **Firefly jar as a block or an item?** Recommendation: a placeable block with light (a lantern alternative), so the
  catch has a use. Should it release fireflies (right-click -> empty jar back, particles)? Optional, later.
- **Is the bush a bee flower?** It extends `FlowerBlock` but is in no flower tag; see `mobs/bees #1`. Recommendation:
  leave it out (1.21 doesn't count it either).

### Gotchas
- **Perf:** every firefly bush in loaded chunks reschedules a block tick every tick at night and sends a particle packet
  each time. A swamp with dozens of bushes near a player is dozens of ticks + packets per tick. If the server shows it
  (spark profile at night in a swamp), a patcherMod mixin on `FireflyBushBlock#tick` to reschedule every 5-10 ticks would
  fix it; note it in [perf](../perf.md) if seen.
- `BlockEvents.rightClicked` fires for both hands; check `e.hand == 'main_hand'` or the item check covers it.
- An empty Supplementaries jar item may carry an empty `BlockEntityTag`; test what `item.nbt` is for a fresh crafted
  jar and for one emptied from a placed jar.

### Tests
- New prefix in whichever writeup takes it, e.g. mob_block_tweaks.md `MT-`: **MT-13 firefly jar:** at night,
  right-click a firefly bush with an empty jar -> one Firefly Jar, the jar count goes down; by day -> message, nothing
  taken. **MT-14 jar light:** place the Firefly Jar -> light level 9 around it (F3), glass sound, drops itself.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | First spawn block (12 rules, 3 conditions, spawn ground) | Testing | MS-01 to MS-23 open. spawns_by_biome.md is from a dump that ran 0 rules (its own WARNING): MS-13/MS-14 are the first thing to run (#0 above). Writeup fixed: at-a-glance "Tests" said MS-01 to MS-12, section 3 said "the pack's file has no rules yet" |

## Writeup corrections made

- [mob_spawns.md](../mob_spawns.md) 0 At a glance, Tests: "MS-01 to MS-12" -> "MS-01 to MS-23" (the suite has 23).
- [mob_spawns.md](../mob_spawns.md) 0 At a glance, Jar: "ships in pack 1.0.5" -> "1.0.5 was never deployed, so it first
  shipped in pack 1.0.6".
- [mob_spawns.md](../mob_spawns.md) 3: "(Format examples from the test fixture; the pack's file has no rules yet.)" ->
  points at "Rules now" (the file has had 12 rules since 2026-10-09).
