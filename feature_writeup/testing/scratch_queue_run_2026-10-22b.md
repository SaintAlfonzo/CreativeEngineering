# Scratch queue entries run on 2026-10-22 (second bulk run)

Verbatim copy of the queue as it stood before the run (commands, pass criteria, failure notes). Results: the
"2026-10-22 second bulk run" section of [scratch_queue.md](scratch_queue.md). Probe paths below are still valid; the
probes were fixed during the run.

Refilled 2026-10-22 (doc sweep): everything built after or outside the 2026-10-22 bulk run (57 checks: SQ-DR to SQ-GE),
plus the 2026-10-19 to 2026-10-21 builds that were never scratch-tested (32 checks: SQ-OF, SQ-OM). Five probes with their
own commands, so all can be copied in at once: `/fdtest`, `/gmprobe`, `/zfw`, `/ofprobe`, `/omprobe`. Suggested order:
install jars; one group A world (sqA3) with all five probes, after boot `forceload add 384 384 655 527` (zfw) and
`forceload add 0 80 239 111` (omprobe), wait 30 s; then the config-flip restarts (SQ-GM-05, SQ-OM-CFG); a second group A world (sqOM2)
for the serverconfig payload (SQ-OM-18); SQ-OM-17 is an offline build check; then group B: one normal world for SQ-MB-04
and SQ-GE-01 to SQ-GE-03, a second for SQ-GE-04.

Before the run (game closed, `pgrep -c java` is 0):

1. patcherMod: `./gradlew :patcherMod:installToMods --offline` so `mods/` holds the build with extra stem fruit (build/libs
   md5 4daad5bb at the sweep; `mods/` had b5f60533 without it). Rebuild first if the source changed since.
2. More Mooblooms: `./gradlew :moreMooblooms:installToMods --offline` (1.1.0, md5 c2df0ab6, replaces 1.0.0) for SQ-GM-09;
   then mod_sides.md row 1.0.0 -> 1.1.0.
3. Copy the five probes into `OUT/srv/kubejs/server_scripts/` (paths in each block) and the instance configs the blocks name.

None of the three probes has run under Rhino yet; expect a probe fix or two (the 2026-10-22 run fixed six).

### Food and drinks probe (SQ-DR, SQ-FL, SQ-IN, SQ-MJ, SQ-SOL, SQ-CO)

One probe for all six groups: [tools/food_drinks_probe/food_drinks_probe.js](../../tools/food_drinks_probe/food_drinks_probe.js),
copied before `boot` to `OUT/srv/kubejs/server_scripts/zz_fd_probe.js`. Run `/fdtest <part>` or `/fdtest all`; results are
`[fdtest] PASS|FAIL <part> ...` lines in `logs/latest.log`, ending `[fdtest] done <part>: N pass, M fail`. Written
2026-10-22, never run yet (`node --check` only). Target: patcherMod `creativeengineering-1.1.01.jar` (holds `PlacedFoodSol`
since md5 b5f60533), KubeJS working tree of 2026-10-22 after 11:24 (coffee merge). New fluids: restart, not `/reload`.

#### SQ-DR: drinks (chorus juice, berry matcha latte, milk jars)

Feature: [../recipes_cooking.md](../recipes_cooking.md) 2.2 (RC-71 to RC-74), TODO `food/drinks #4` to `#6`. Client-only: JEI,
kettle screen, sounds, creative (scratchpad).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-DR-00 | A | boot | `Done (`; no `Error occurred while handling event` from `cooking.js`, `baking.js`, `milk_jar.js`, `item_merges.js`; no `[NCH]` warning for `frBrewing`, `chorus_juice`, `*_egg_solution`, `frosting`; startup.log registers the three fluids | Startup: `fluids.js` builder chain (`noBucket().noBlock()`), dict keys in `fluid_dict.js` ("Pack fluids") |
| SQ-DR-01 | A | `/fdtest recipes` | PASS for `farmersdelightcooking/chorus_juice_manual_only`, `createmixing/chorus_juice`, `createfilling/chorus_juice`, `createemptying/chorus_juice` and one `iebottling/*` making `endersdelight:chorus_juice` | NCH ids in `cooking.js` "Chorus juice"; IE bottling needs the `#forge:fluid/chorus_juice` tag (`fluidtags.js`) |
| SQ-DR-02 | A | same | Delightful's `food/berry_matcha_latte_from_matcha_latte_neapolitan` gone; `farmersrespitebrewing/berry_matcha_latte` and `shapeless/berry_matcha_latte` loaded; grid takes ice and ice cubes; matcha latte's crafting remainder is a glass bottle; exactly 2 berry latte recipes | Kettle id missing: `frBrewing` JSON (grep `farmersrespite` in latest.log). Remainder not a bottle: add a remainder in NCH `shapeless` |
| SQ-DR-03 | A | `/fdtest milkjar` | For both `milk_jar` and `cow_jar`: empty jar refuses a bottle; with 1000 mB a glass bottle leaves 750 mB and gives 1 FD milk bottle; a milk bottle puts it back to 1000 mB and returns a glass bottle | Check `Error occurred while handling event` first (the `stack.is` bug was fixed 2026-10-22); no error and no change = KubeJS skips fake players, the check moves to RC-74 in game |

#### SQ-FL: cooking fluids (egg solution, frosting)

Feature: [../recipes_cooking.md](../recipes_cooking.md) 2.2 (RC-77, RC-78), TODO `dishes/ingredients #2`, [../oven.md](../oven.md) 2.6.
Machine runs and JEI: in game.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-FL-01 | A | `/fdtest fluids`, `/fdtest recipes` | `chorus_juice`, `egg_solution`, `frosting` registered under `creativeengineering:`, each `#forge:fluid/<name>` tag holds it; the 11 `createmixing/*egg_solution` ids and `createsequencedassembly/quiche_egg_solution_sequenced_assembly` loaded | Fluid missing: `fluids.js` (restart). Tag empty: `fluidtags.js`. Assembly missing: `R.incomplete('quiche')` |
| SQ-FL-02 | A | same | `createmixing/frosting`, `createcompacting/frosting`, `createfilling/cake_frosting_spout`, `shapeless/frosting_manual_only` loaded | `baking.js` "Frosting fluid"; the REMOVED_OUTPUTS sweep for `i.cake` must run before the spout recipe is added |

#### SQ-IN: bone broth routes and ice cubes

Feature: [../recipes_cooking.md](../recipes_cooking.md) 2.3 (RC-75, RC-76), TODO `dishes/ingredients #4`, `#5`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-IN-01 | A | `/fdtest recipes` | the 13 `shapeless/<meal>_cooked_manual_only` ids loaded (6 broth soups, 7 pastas); kangaroo stew takes bone broth | `[NCH]` warning names the undefined dict key (e.g. `i.t_cooked_duck`, `i.t_cooked_kangaroo`) |
| SQ-IN-02 | A | `/fdtest ice` | `neapolitan:ice_cutting` gone; `farmersdelightcutting/ice_cubes_hammering`, `shapeless/ice_cubes_hammering` (4), `iecrushing/ice_cubes` (3) loaded; `#forge:ice_cubes` holds ice; the 6 ice foods (Abnormals Delight's melon popsicle, chocolate / kiwi / hamimelon popsicle, shaved ice, bellini) take ice and cubes; `neapolitan:ice` takes cubes only | Ice food refusing ice: wrong id in `iceFoods`; `neapolitan:ice` taking ice = the cube swap's `not: { id }` filter failed (9 ice -> ice loop) |

#### SQ-MJ: soups and stews in jars

Feature: [../mixed_jars.md](../mixed_jars.md) 9 (MJ-11 to MJ-14), TODO `dishes/ingredients #13`. 24 files in
`kubejs/data/creativeengineering/moonlight/soft_fluids/`. Colours and WTHIT: in game.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MJ-01 | A | `/fdtest soupjar` | `24 soups are soft fluid items` and `every #food_type/soup item is a soft fluid item` PASS | grep `soft_fluid` / `moonlight` errors in latest.log; rerun `/cdevgen run soup_soft_fluids` |
| SQ-MJ-02 | A | same | corn soup in (2, bowl back), six bowls fill and the 7th stays, a bowl takes one out; all 24 in and out as themselves; shulker bowl back, wooden bowl refused; a bottle soup pours 1 | Count 0: container `capacity` / `empty` in the json. Wrong item out: two fluids claim the same item |

#### SQ-SOL: placed pies and cakes for Spice of Life

Feature: [../food.md](../food.md) 4.2 finding 5 (FO-44; the bite itself is FO-43 in game, fake players are skipped on
purpose), TODO `food/types-and-tuning #5`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-SOL-01 | A | boot, then `/fdtest sol` | No `Mixin apply failed` / `InvalidInjectionException` naming `ServerPlayerGameModePlacedFoodMixin`; no `PlacedFoodSol:` warning; class loaded, map read (14 entries), every entry block -> food item, chocolate cake -> `abnormals_delight:chocolate_cake_slice` | Class missing: old jar. Mixin target `ServerPlayerGameMode#useItemOn` (m_7179_). A `not a food item` warning: fix that line of `placed_food_slices.json` |

#### SQ-CO: coffee merge

Feature: [../item_merges.md](../item_merges.md) 2.8 (recipe and effect parts of MG-23, MG-24, MG-26), TODO `food/drinks #3`.
Inventory swap (MG-25), bush drops (MG-28), JEI: in game.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CO-01 | A | boot, then `/fdtest coffee` | `[itemMerges] 12 merges` in startup.log; the 7 FR coffee ids in `global.removedItems`; `no recipe names a purged coffee item`; the 9 removed ids gone; a coffee cake recipe takes roasted coffee beans | A recipe hit: `replaceInput` / `replaceOutput` missed that type; remove it by id in `item_merges.js` |
| SQ-CO-02 | A | same | `Rustic coffee gives Caffeinated, no Speed / Haste` PASS | `foodEffectSwaps` in `food_values.js` didn't apply (startup.log `[food values]`) |

After the run: results into recipes_cooking.md 2.2 / 2.3, mixed_jars.md 9, food.md FO-44, item_merges.md 2.8; TODO rows
`food/drinks #3` to `#6`, `dishes/ingredients #2`, `#4`, `#5`, `#13`, `food/types-and-tuning #5`.

### SQ-GM: gear, mobs, storage and config (shields, dragon bow, camera bags, archery, vein undo, chalk, seagulls, Mooblooms 1.1, sacks)

Probe: [tools/gear_misc_probe.js](../../tools/gear_misc_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_gm_probe.js`. Run `/gmprobe [part]`; results are `[gmprobe] PASS|FAIL|INFO ...` lines in
`logs/latest.log`, ending `[gmprobe] done: N pass, M fail`. Written 2026-10-22, never run yet (`node --check` only). Target:
patcherMod `creativeengineering-1.1.01.jar` (holds every class here since md5 b5f60533), KubeJS working tree of 2026-10-22.
SQ-GM-09 also needs `moreMooblooms-1.1.0.jar` (md5 c2df0ab6, in `development/moreMooblooms/build/libs`) installed into
`mods/` in place of 1.0.0 before boot (`scratch_server.sh` symlinks `mods/`; game closed). Copy the instance
`config/creativeengineering-common.toml`, `alexsmobs.toml` and `chalk-common.toml` into `OUT/srv/config/` if setup did not;
`[vein_undo]` is not in the instance toml yet (Forge adds it with defaults at boot).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-GM-00 | A | boot | `Done (`; no `Mixin apply failed` / `InvalidInjectionException` naming `SupplementariesSackItemMixin`, `OreExcavation*Mixin` or `McaCombEyeColourMixin`; no KubeJS errors from `tool_effects.js`, `equipmenttags.js`, `bullets.js`, `archery.js`, `blocktags.js` | Mixin error: check the OE 1.11.166 / Supplementaries 2.4.20 target names; KubeJS: the named file |
| SQ-GM-01 | A | `/gmprobe shield` ([shields.md](../shields.md) 2.4, SH-44) | INFO lines show the three `shielderEffects`; netherite gives no Resistance; echo amplifier 0, felsic 1, dragon 1 plus Regeneration 0 and attacker Poison; Resistance lasts 40 ticks | Empty INFO: the srv toml lacks the values (copy the instance toml). Values but no effect: `ShieldBlockHandler` / `ShieldEffects.parse` |
| SQ-GM-02 | A | `/gmprobe effects` ([armor_effects.md](../armor_effects.md) F4, on-hit half of AE-19 / AE-20) | Bow held = Invisibility I; crossbow held = Resistance II; fake-player main-hand hit: bow -> slowness 3 + glowing 0, crossbow -> exsanguination 1 + weakness 2; neither gives the target Invisibility or Resistance | Tables: startup `tool_effects.js` (restart). No hit effect: `server_scripts/magic/tool_effects.js` `EntityEvents.hurt` |
| SQ-GM-03 | A | `/gmprobe bag` ([camera_bags.md](../camera_bags.md) F4, CB-11) | Redstone lamp and IE light bulb accepted; stick refused | `equipmenttags.js` ADD list, `i.t_exposure_flashes` |
| SQ-GM-04 | A | `/gmprobe archery` ([archery.md](../archery.md) F1, F5, AR-07 / AR-13) | Dragon scale cartridge recipe in blueprint category `bullet`, none in `projectiles`; inputs match; output 64 armor-piercing; explosive arrow blueprint and fletching recipes accept a blaze rod | Missing recipe: `[NCH]` lines in server.log; counts: `NCH.ieBlueprint` |
| SQ-GM-08 | A | `/gmprobe vein` ([fixes_compat.md](../fixes_compat.md) 2.26, FX-31 / FX-32 / FX-34) | OE Auto Pickup on; stone allowed; iron ore, deepslate coal ore, diamond block, budding amethyst, spawner, ancient debris and chest each refused ("can't be undone"); cooldown message after `startCooldown`; history mixin applied; `tickRestore(server, null)` returns false without a crash | Tag empty: `blocktags.js` `vein_undo_denied`. Chest allowed: `denyBlockEntities`. Throws: the `@Shadow` / reflection names in `VeinUndoAccess` |
| SQ-GM-05 | A | after SQ-GM-08: `stop`; in `OUT/srv/config/creativeengineering-common.toml` set `[vein_undo] cooldownSeconds = 0`, `denyBlockEntities = false` (add the section if absent); boot; `/gmprobe vein` | As SQ-GM-08 except the chest and cooldown lines FAIL (the expected flips: chest allowed, no cooldown) | Config not read: `CEConfig` `[vein_undo]` registration |
| SQ-GM-06 | A | `/gmprobe chalk` ([fixes_compat.md](../fixes_compat.md) 2.27, FX-27) | Every `MarkSymbol.getSpecialSymbols()` symbol unlocked for a new fake player | The srv `chalk-common.toml` lacks the empty `*UnlockAdvancement` values |
| SQ-GM-07 | A | `/gmprobe gull` ([mob_block_tweaks.md](../mob_block_tweaks.md) MT-26) | The seagull blacklist INFO line holds the 3 lunchbox ids and each item exists | `alexsmobs.toml` not copied; id typo |
| SQ-GM-09 | A | boot with `moreMooblooms-1.1.0.jar` in place of 1.0.0 ([mooblooms.md](../mooblooms.md) 5) | `Done (`; no error naming `more_mooblooms` or `compat.jei` | A JEI or client class reached from common code in 1.1.0 |
| SQ-GM-10 | A | `/gmprobe sack` ([storage_audit.md](../storage_audit.md) 6, logic of SB-01 / SB-03 / SB-04) | Rotate down then up restores the order; a one-stack sack never rotates; empty-cursor right-click (`overrideOtherStackedOnMe`) puts 2 cobblestone on the cursor and leaves 2 stacks (dirt next); an emptied sack has no tag; a left click takes nothing | Mixin not applied (SQ-GM-00); `SackBundle.contents` / `store` |

After the run: results into shields.md SH-44, armor_effects.md AE-19 / AE-20, camera_bags.md CB-11, archery.md AR-07 / AR-13,
fixes_compat.md 2.26 / 2.27, mob_block_tweaks.md MT-26, mooblooms.md 5, storage_audit.md 6; TODO rows `gear/shields #5`,
`gear/archery #2`, `#5`, `gear/camera-bags #4`, `potions/effects #2`, `mobs/tweaks #7`, `mobs/mooblooms #2`,
`items/storage-and-item-loss #7`, `gameplay/misc #8`, `#12`.

### Farming and worldgen probe (SQ-SF, SQ-WC, SQ-CD, SQ-MB, SQ-GE)

Probe: [tools/farm_worldgen_probe.js](../../tools/farm_worldgen_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_zfw_probe.js`. Commands `/zfw stem|wild|straw|loot|mulberry|recipes|geode N|count R ids`;
results `grep '\[zfw\]' logs/latest.log`. Written 2026-10-22, never run yet (`node --check` only). Group A: after boot run
`forceload add 384 384 655 527` and wait 30 s before the `zfw` commands.

Jar: only `development/patcherMod/build/libs/creativeengineering-1.1.01.jar` (md5 4daad5bb) holds extra stem fruit; `mods/`
(b5f60533) has everything else here. Either install it first (`:patcherMod:installToMods`, game closed) or after setup:
`ln -sfn "$PWD/development/patcherMod/build/libs/creativeengineering-1.1.01.jar" OUT/srv/mods/creativeengineering-1.1.01.jar`.
Never the `server_mods/` jar (37bb7049, old geode `drop` key, no Mulberry sapling).

#### SQ-SF: extra stem fruit

Feature: [../crops.md](../crops.md) 2.10, CR-71 to CR-75, TODO `farming/crops #11`. Before boot append to
`OUT/srv/config/creativeengineering-common.toml` (section absent there): `[stem_fruit]` `enabled = true`, `maxFruits = 4`,
`secondFruitMinutes = 0`, `thirdFruitMinutes = 0`, `fourthFruitMinutes = 1`. Not covered: seasons (CR-76, bug `crops #12`),
the stem model.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-SF-00 | A | boot | `Done (`; no `Mixin apply failed` / `InvalidInjectionException` naming `AttachedStemExtraFruitMixin`; no `[stem_fruit] CropBlock.getGrowthSpeed not found` | Mixin SRG names `m_6724_`, `m_213898_`, `m_6810_`, `m_7417_` (no refmap) |
| SQ-SF-01 | A | `/zfw stem` | `stem fruits found:` lists melon, pumpkin, hamimelon; PASS `attached stem random-ticks` for each | Not random-ticking: the `m_6724_` override did not apply |
| SQ-SF-02 | A | same run | PASS `2nd and 3rd fruit, 4th held by its wait`: 3 fruits after 3000 random ticks | 1 = wait or light / soil check; 4 = wait ignored; check the printed cfg line |
| SQ-SF-03 | A | wait 70 s, `/zfw stem` | PASS `4th fruit after the wait, no 5th`: 4 | 3 = saved last-fruit time wrong (`StemExtraFruit.Data`) |
| SQ-SF-04 | A | same | PASS `re-points at another fruit`, PASS `all taken -> plain grown stem` | `reattach` / `m_7417_` inject |
| SQ-SF-05 | A | same | PASS `enabled = false: no extra fruit`; `[zfw] stem done N pass, 0 fail` | Config read path |

#### SQ-WC: wild-crops batch 2026-10-22

Feature: [../crops.md](../crops.md) 2.8, V1, V6, V7, V20, CR-83, CR-86 to CR-88, TODO `farming/wild-crops #3`, `#4`. Target:
`wild_crops.json` of 11:09 (offline `tools/test_wild_crops.sh` passes), `config/neapolitan-common.toml` min height 192.
Registry checks only, so superflat is fine.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-WC-00 | A | boot | `[wild-crops] N crop/biome pairs in 53 crop tags` with no `problems`, no `[wild-crops]` ERROR | Rerun `tools/test_wild_crops.sh`; `vinery_upstream/` symlink present |
| SQ-WC-01 | A | `/zfw wild` | PASS tea in 9 biomes (swamp, mangrove_swamp, jungle, dark_forest, lush_valley, sakura_grove, orchid_swamp, tropical_jungle, cloud_forest); PASS no tea in plains | Tea rule / trait `cherry` in `wild_crops.json` |
| SQ-WC-02 | A | same | PASS grapes: lavender_forest bushes, shrubland savanna, shield taiga, tropical_jungle + mangrove_swamp vines; none in snowy_taiga, dark_forest | The four `grape*` rules; Vinery modifier ids |
| SQ-WC-03 | A | same | PASS `no wild flax anywhere` | `flax` rule / `supplementaries:wild_flax` forge:none |
| SQ-WC-04 | A | same | PASS savanna cotton, jungle pepper + coffee generate; PASS bone meal in savanna / jungle / plains grows no `rusticdelight:` patch | `kubejs/data/rusticdelight/worldgen/configured_feature/*` overrides not loaded |
| SQ-WC-05 | A | `/zfw straw` | PASS y 200 white, y 191 red, y 150 red; config min height 192 | `neapolitan-common.toml`; `>` vs `>=` at 192 |

#### SQ-CD: crop drop tables (apples from apple trees only, bell pepper colours)

Feature: [../crops.md](../crops.md) 3, V17, CR-57, CR-69, TODO `farming/crops #6`, `#9`. Built 2026-10-21, not in the
2026-10-22 run. Any 1.1.01 jar.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-CD-00 | A | boot | `[leaf-apples] apples removed from 3 of 3 leaf tables`, `[crop-drops] rewrote 1 of 1 crop tables` | Script ran before the tables / pool match |
| SQ-CD-01 | A | `/zfw loot` | PASS each pepper colour 0.40 to 0.56 per ripe harvest (600 rolls); PASS unripe drops no pepper | `09_crop_drops.js` weights / CEConfig `[crop_drops.bell_peppers]` defaults |
| SQ-CD-02 | A | same | PASS no apples from oak, dark oak, rubberwood leaves (2000 rolls each) | `08_leaf_apples.js` TABLES |

#### SQ-MB: river wood removed, Mulberry tree

Feature: [../recipes_wood.md](../recipes_wood.md) 2.6, RW-15 to RW-20, TODO `recipes/wood #3`. KubeJS tree 2026-10-22 +
patcherMod `MulberrySapling` (in `mods/` and `build/libs`).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-MB-00 | A | boot, `/zfw recipes` | startup.log `[removedWoods] 1 woods merged into oak`, no `[item_dict] Missing` for river / oak ids; PASS `no recipe makes one` (92 purged); PASS one hedge recipe, oak log, x2 | `server_scripts/wood/removed_woods.js` `R.remove` filter; item_dict keys |
| SQ-MB-01 | A | `/zfw mulberry` | PASS river_tree feature: oak_log > 0, river_log 0, mulberry leaves > 0 | Datapack `upgrade_aquatic/worldgen/configured_feature/river_tree.json` not read |
| SQ-MB-02 | A | same | PASS bone-meal sapling gives an oak trunk; PASS with `mulberrySapling = false` river logs, no oak | `MulberrySapling` event not fired (UA's grower may bypass `SaplingGrowTreeEvent`) |
| SQ-MB-03 | A | `/zfw loot` | PASS river_log drops oak_log x5, river_planks drops oak_planks x5 | Item merge loot path |
| SQ-MB-04 | B | `/locate biome minecraft:river` (X Z); `/forceload add X-48 Z-48 X+48 Z+48`; wait 60 s; `/execute positioned X 0 Z run zfw count 3 upgrade_aquatic:river_log upgrade_aquatic:river_leaves minecraft:oak_log` | river_log 0, river_leaves > 0 (0 leaves: try another river) | Worldgen still uses UA's built-in holder |

#### SQ-GE: ocean geodes under the deepest floor

Feature: [../structure_tuning.md](../structure_tuning.md) 2026-10-22 geode note, ST-133, TODO
`structures/rates-and-groups #7`. Group B, a second new world for SQ-GE-04.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-GE-00 | B | boot | `[worldgen.ocean_geodes]` in `OUT/srv/config/creativeengineering-common.toml` has `lowestFloorY = -8`, `clearance = 2`, `spread = 8`, no `drop`; no `GeodeFeatureOceanDepthMixin` errors | Wrong jar linked |
| SQ-GE-01 | B | `/locate biome minecraft:deep_ocean` (X Z); `/forceload add X-112 Z-112 X+112 Z+112`; wait 90 s; `/execute positioned X 0 Z run zfw geode 36` | Log lists geode features; PASS `ocean amethyst geodes moved under y -10` (75%+ of placed); PASS `moved ocean geodes never reach the sea floor` | `OceanGeodes.placeLowered` maths; ocean tag on the floor biome |
| SQ-GE-02 | B | `/forceload remove all`; `/locate biome minecraft:plains`; same forceload / wait; `/execute positioned X 0 Z run zfw geode 36` | PASS `land geodes keep their height` | Ocean check too broad |
| SQ-GE-03 | B | from the SQ-GE-01 run | PASS `prismarine geodes keep their height` (Geode Plus, found by `geode:budding_prismarine`) | `untouched` list |
| SQ-GE-04 | B, 2nd world | new level-name, `[worldgen.systems] oceanGeodes = false` before boot, repeat SQ-GE-01 | The `moved` line FAILs with 0 moved (system off) | Switch not read |

After the run: results into crops.md 2.8 / 2.10 / 3, recipes_wood.md 2.6, structure_tuning.md (ST-133); TODO rows
`farming/crops #6`, `#9`, `#11`, `farming/wild-crops #3`, `#4`, `recipes/wood #3`, `structures/rates-and-groups #7`.


### Older food and kitchen builds (SQ-OF)

Food builds of 2026-10-19 to 2026-10-21 that a server can check and that never ran on one. One new probe:
[tools/older_food_probe.js](../../tools/older_food_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_of_probe.js`. Run `/ofprobe <part>` or `/ofprobe all` (parts: `cans`, `canextra`,
`canremoved`, `cornmeal`, `muffin`, `meals`, `assembly`, `expensive`, `frying`, `dogfood`, `melon`); results are
`[ofprobe] PASS|FAIL|INFO ...` lines in `logs/latest.log`, ending `[ofprobe] done: N pass, M fail`. SQ-OF-06 uses the
existing [tools/kitchen_probe/cfb_oven_smoking_probe.js](../../tools/kitchen_probe/cfb_oven_smoking_probe.js), copied to
`OUT/srv/kubejs/server_scripts/zz_ko_probe.js` (`/kotest`). Written 2026-10-22, never run (`node --check` only). Target:
patcherMod `creativeengineering-1.1.01.jar` from `build/libs` (install step 1 above; it holds `CfbOvenSmokingMixin` and
`CanServings`), KubeJS working tree of 2026-10-22. All group A; can share the superflat world of the other group A probes.
Client-only, not queued: JEI (Seasoning page, Feast Plating tab), WTHIT "Holding" line, fridge models, oven tooltips,
can sprites, names.

#### SQ-OF-00: boot

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OF-00 | A | boot | `Done (`; `logs/kubejs/startup.log` has `[cans] registered 228 generated cans` (the probe's SQ-OF-01 INFO line gives the number from the json; CP-19 still says 227, written before the last can was added); no `[canning] ... skipped`, no `[cans] ... has no item_dict key`, no `[food values]` warning; no `Error occurred while handling event` from `canning.js`, `cans.js`, `cooking.js`, `baking.js`, `food_values.js` | Startup: `cans.js` reads `kubejs/generation_scripts/food/can_types.json` (missing in `OUT/srv/kubejs`?); skipped can recipe: an item_dict key in the json, rerun `tools/gen_can_types.py` |

#### SQ-OF-01 to SQ-OF-03: cans (values, effects, crafts, removals)

Feature: [../can_parity.md](../can_parity.md) 3.1, 4.3, 2.23 and [../cans.md](../cans.md) F2, TODO `cans/canning #3`, `#4`,
`#5`, `#14`, `#15`, `food/types-and-tuning #2` (in game: CP-09, CP-15 to CP-22).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OF-01 | A | `/ofprobe cans` | PASS for "food values as the json", "servings as the json" and "can recipes as the json" (all 262 cans: registered, hunger, saturation modifier, meat / fast / always, effects with ticks / amplifier / chance, `CanServings` count; every `shapeless/<key><suffix>` or `farmersdelightcooking/<key><suffix>` recipe makes 1 can from the json's slot count with the empty can; pie cans turn back into their pie). Per-can FAIL lines name the can and both values | Values off on a Canned Goods can only: `ItemEvents.modification` in `cans.js` (`setFoodProperties`). Slot count off on every multi-count recipe: KubeJS did not expand `Item.of(x, n)`, fix the probe's count. Servings off: `can_servings.json` out of step with `can_types.json` (rerun `gen_can_types.py`) |
| SQ-OF-02 | A | `/ofprobe canextra` | Canned Banana agility 300 t, Glow Berries glowing 300 t, Salmonberries water breathing 100 t, Raw Chicken hunger 600 t at 0.3; Coffee Berries wither 40 t at 0.8 and caffeinated 200 t; Cactus Flesh 4 + can and 3 + salt + can; Canned Pumpkin 5 / 0.3, 3 servings, 3 slices + sugar; cantaloupe slice eats as a pumpkin slice; Canned Cake 4 hunger, fast, speed 400 t, 4 servings, from 1 cake + can and from 4 slices + sugar or honey + can; no can with a saturation modifier above 10; no cookie can is meat | A json value differs from the TODO text: `tools/gen_can_types.py` SECTIONS / EXPLICIT. Cantaloupe: `food_values.js` |
| SQ-OF-03 | A | `/ofprobe canremoved` | No recipe makes a can with NBT (no partly full can); no recipe makes Canned Food or the Hot Hot Hotpot; butcher has no BasicItemListing selling Canned Food and still has other listings | Canned Food trade: `trades.js` butcher `forEachTrades` filter; recipe left: `canning.js` removals |

#### SQ-OF-04: cornmeal (2026-10-19)

Feature: [../recipes_cooking.md](../recipes_cooking.md) 2.1, TODO `dishes/ingredients #1` (RC-15 to RC-17 in game; the
grid tortilla, washing, Bottler and mortar routes passed in SQ-CL-01 and the mortar probe).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OF-04 | A | `/ofprobe cornmeal` | Knife cuts 1 maize -> 4 kernels; IE crusher corn -> 2 cornmeal, crate -> 18; PNC `pcpressurechamber/corn_kernels_cornmeal` present (INFO lists its output: 3 cornmeal); `shapeless/cornbread_batter` makes 3 from cornmeal, no corn; the 10 old Corn Delight ids (cutting, raw tortilla, splashing, batter, IE crusher, milling) gone; no raw tortilla or batter recipe takes corn; cornmeal in `#forge:flour/corn`. INFO lists the pancake recipes and whether each takes cornmeal (RC-17: pancakes accept cornmeal) | Old id back: the `R.remove` list in `cooking.js` "Cornmeal"; tag: `tags/cookingtags.js` |

#### SQ-OF-05, SQ-OF-06: oven muffins and the CfB oven smoking

Feature: [../oven.md](../oven.md) 2.6 (muffins), 2.8 (other ovens), TODO `cooking/oven #5`, `#4` (OV-20, OV-24 in game; OV-23
tooltips are client-only).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OF-05 | A | `/ofprobe muffin` | `baking/blueberry_muffin` and `baking/cranberry_muffin` make 4; `farmersdelightcooking/blueberry_muffin` and `cranberry_muffin` make 2 | `baking.js` "Muffins", `cooking.js` muffin pot recipes |
| SQ-OF-06 | A | `/kotest`, wait 30 s | `[kotest] PASS` for OV-24a (ham -> smoked ham), OV-24b (beef -> cooked beef), OV-24c (cobblestone refused) at once, and OV-24d (ham cooked to smoked ham in the output slots) 30 s later | `mods/` jar older than the build (no `CfbOvenSmokingMixin`; grep `creativeengineering.mixins.json` in the log for a mixin apply error); OV-24d alone: no fuel burn in a forceloaded chunk, `forceload add 0 0` and rerun |

#### SQ-OF-07 to SQ-OF-12: meals

Feature: [../recipes_cooking.md](../recipes_cooking.md) 2.3, TODO `dishes/meals #1`, `#2`, `#3`, `#4`, `#6`, `#7`, `#8`
(RC-55 to RC-64, RC-66 to RC-69 in game); Glistering Melon Slice: [../food.md](../food.md) 3.10, FO-29. These were only
"verified loading" on a scratch server before.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OF-07 | A | `/ofprobe meals` | Grid routes for vegetable soup, vegetable noodles, ratatouille, corn soup; `#creativeengineering:soup_veggies_cooked` not empty; every `creativeengineering:shapeless/*_cooked*` id ends `_manual_only` and each one checked is skipped by Create automation; fried rice takes cooked eggs (no raw egg), mushroom rice cooked rice + baked potato, chicken soup cooked chicken; mutton wrap on a tortilla (grid and assembly base), FD / CCK wraps gone; Incubation's scrambled eggs gone, ours takes 3 eggs; B&C vegetable omelet pot takes fried eggs, omelette grid route; exotic fruits and fruit salad replaced (salad assembly makes 2), both tags filled; 8 burgers: each one shaped grid recipe and one assembly, no milk, the 11 old burger ids gone; quiche: one shaped grid recipe and an assembly, B&C's two gone | "one grid recipe" FAIL lists the extra ids: an `R.remove` missing in `cooking.js` "Meals" |
| SQ-OF-08 | A | `/ofprobe assembly` | Shepherd's pie grid takes a cheese wedge, no milk, FD's gone; assemblies for shepherd's pie, horror lasagna (2), stuffed potato, crispy skewer, crawling sandwich, steak and potatoes; the last three have only their shaped grid recipe (Ender's Delight / FD ids gone). INFO names horror lasagna's first step (pasta) | Assembly missing: `R.createSequencedAssembly` id in `cooking.js` "Assembly meals" |
| SQ-OF-09 | A | `/ofprobe expensive` | Grid routes `_cooked_manual_only` for chorus stew, endermite stew, crab pasta, pumpkin soup, pasta with meatballs, dumplings, cavern explorer stew, squid ink pasta; `createmixing/squid_ink_pasta_mixing` makes 2 from ink + tomato sauce fluids, heated | Mixing heat or fluids: NCH `createMixing` call in `cooking.js` |
| SQ-OF-10 | A | `/ofprobe frying` | The 4 PneumaticCraft frying ids gone; raw fish tempura from cod or salmon + batter; pot fish tempura and fries (2) with `#rusticdelight:cooking_oil`; fish and chips plated on paper, plus its grid route; `#forge:sliced_potatoes` holds the Rustic Delight slices; no salmon tempura / chips / fish and chips recipe with plant oil. INFO lists every fry route (Slice & Dice's heated mixing copies are fine) | Plant-oil mix left: the old heated mixing in `cooking.js` or a PneumaticCraft datapack recipe; remove by id |
| SQ-OF-11 | A | `/ofprobe dogfood` | `shapeless/dog_food_cooked_manual_only`: 5 slots, rotten flesh, bone meal, 2 cooked meat, cooked rice; skipped by Create automation | `cooking.js` "Dog food" |
| SQ-OF-12 | A | `/ofprobe melon` | Glistering Melon Slice is food with the Golden Melon Slice's hunger and modifier, always edible, no effects, in `#creativeengineering:food_type/golden` | `food_values.js` (`effects: false` branch), `food_types.json` "golden" |

After the run: mark SQ-OF-00 to SQ-OF-12 PASS / FAIL here, then record them in [../can_parity.md](../can_parity.md) section 7
(CP-09, CP-15 to CP-22: values and recipes; sprites, JEI, eating stay in game), [../recipes_cooking.md](../recipes_cooking.md)
(RC-15 to RC-17, RC-55 to RC-69 recipe level), [../oven.md](../oven.md) (OV-20, OV-24) and [../food.md](../food.md)
(FO-29), and set TODO `cans/canning #3, #4, #5, #14, #15`, `types-and-tuning #2`, `ingredients #1`, `oven #4, #5`,
`meals #1 to #4, #6 to #8` to "scratch server PASSED" for the server-side part.

### Older alcohol, ports and misc builds (SQ-OM)

Probe: [tools/older_misc_probe.js](../../tools/older_misc_probe.js), copied before `boot` to
`OUT/srv/kubejs/server_scripts/zz_om_probe.js`. Run `/omprobe <part>` or `/omprobe all` (all = keg, coaster, compost,
drinks, cask, press, leafslow, raid, air, tier1, baby, jeed; `setup`, `wine`, `campfire`, `babyxp`, `houses X Z` and
`serverconfig` only by name); results are `[omprobe] PASS|FAIL|INFO ...` lines in `logs/latest.log`, ending
`[omprobe] done: N pass, M fail`. Written 2026-10-22, never run yet (`node --check` only). Its blocks go in a strip at
x 0 to 230, y -60, z 92 to 100: run `forceload add 0 80 239 111` after the boot and wait 30 s before the first part.

Targets: patcherMod `creativeengineering-1.1.01.jar` (every class here is in both the `mods/` build b5f60533 and the
build/libs one 4daad5bb the queue installs first), Brewin' `backportedBrewin-4.5.0-ce-19.2-03.jar` (md5 579cd3fa),
Vinery `backportedVinery-1.5.5-ce-19.2-04.jar` (md5 2aa87561), `babyMobs-1.0.0.jar` (md5 389cf345): all three already
in `mods/`, identical to their `build/libs`, nothing to install for this block. KubeJS working tree of 2026-10-22.
World sqOM1 (group A) for SQ-OM-00 to SQ-OM-16; SQ-OM-17 is offline; SQ-OM-18 needs its own group A world sqOM2.

#### SQ-OM-00: boot lines

Feature: boot log of the alcohol, feature-port, JEED and Vinery village work ([alcohol.md](../alcohol.md) 11, 13,
[feature_porting.md](../feature_porting.md) 1.1, [effects.md](../effects.md) 3); TODO `alcohol/pack #1`,
`alcohol/vineryport #14`, `ports/featureporting #2`, `ui/effects #4`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-00 | A | boot sqOM1 | `Done (`; `[alcohol] food values set on 44 of 44 drinks` and no other `[alcohol]` line (AL-90); `[feature-ports] tier 1: 371 recipes (1 name tag, 33 splashing, 26 recycle smelting, 311 AE2 cable)` and no `[NCH]` error (FP-15); `jeed_providers.js: 83 effect provider recipes` and no `jeed_providers.js:` warning (EF-15); four `[vinery-houses] vinery:vinery_<biome> added to minecraft:village/<biome>/houses` lines, no `[vinery-houses]` warning; no `Mixin apply failed` / `InvalidInjectionException` naming `RaidGlowingMixin`, `LeafPileSlowMixin`, `CampfireRainMixin`, `baby_mobs` or the two ports | 43 of 44: the named drink's `item_dict` key; a lower recipe count: `feature_ports.js` / `resource_blocks.js`; mixin: the named class against its target |

#### SQ-OM-KEG: Brewin' keg slot rules, side input, old kegs

Feature: [alcohol.md](../alcohol.md) 10.16 (logic of AL-81 to AL-84), [remainder_buffer.md](../remainder_buffer.md) 3.6;
TODO `alcohol/brewinport #7`, `#6` (slot half; the leftover overflow and stall, AL-79 / AL-80, stay in game).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-01 | A | `/omprobe keg` | 9 slots; wheat into the grid from side, bottom and top, dirt refused; tankard into the container slot, dirt refused; a honey bottle skips the grid from side and bottom and goes into the fluid slot, not the container slot; output, empty-container and leftover slots take nothing from any face; an 8-slot (`-02`) keg NBT loads wheat 3 / tankard 2 / beer 1 / glass bottle 4 into the same slots of 9; bottom and sides pull output, empties and leftovers, the top only ingredients | Slot counts: `KegItemHandlerForge.readFromNbt`; face rules: `SidedKegWrapper`; slot validity: `KegBlockEntity.isItemValidForSlot` |

#### SQ-OM-COA: coasters take modelled items only

Feature: [alcohol.md](../alcohol.md) 12 (logic of AL-91); TODO `alcohol/brewinport #8`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-02 | A | `/omprobe coaster` | INFO `taggedItemsOnly=true`; `#brewinandchewin:placeable_on_coaster` holds 100+ items (about 152); beer, apple, FD hot cocoa accepted; Vinery red wine, Farmer's Respite green tea, stone refused; a fake-player click with stone leaves the placed coaster empty, with beer puts 1 on | Tag small: `placeabletags.js` / the port's tag file; accepts true for wine: `CoasterBlockEntity.accepts` or the srv `brewinandchewin-common.toml` |

#### SQ-OM-CMP: Brewin' compost chances and BlockRunner tier

Feature: [alcohol.md](../alcohol.md) 10.16 (compost and BlockRunner lines); TODO `alcohol/brewinport #4` (done part).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-03 | A | `/omprobe compost` | Kimchi 0.5, Pickled Pickles 0.5, Quiche 0.85, Quiche Slice 0.85 in the composter map; heating cask and ice crate in `blockrunner:surface/wood`; the coaster in no `blockrunner:surface/*` tag | `compost_values.js` brewinandchewin rows (startup: restart); `blocktags.js` wood list |

#### SQ-OM-ALC: alcoholic drink food values

Feature: [alcohol.md](../alcohol.md) 11 (logic of AL-85 to AL-87; the Quark word AL-88 is client); TODO `alcohol/pack #1`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-04 | A | `/omprobe drinks` | 44 of 44 drinks: hunger 1, saturation 0, alwaysEat, hidden Intoxication, Nausea 100 ticks (200 for Jellie Wine and Mojang Noir, none for Tipsy drinks and Bottle of Wodka); a survival fake player drinking Red Wine: hunger 10 -> 11, saturation stays 0, hidden Intoxication and Nausea 100 | The FAIL line names each wrong drink and value: `alcohol_drinks.js`; Red Wine gives nothing: Vinery `-03`+ `DrinkBlockItem` eat path |

#### SQ-OM-WN: wine storage boosts and campfire rain (one setup, then wait)

Feature: wine storage [alcohol.md](../alcohol.md) 10.13, [time.md](../time.md) 4 T5 (logic of VB-76); campfire rain
(patcherMod `CampfireRain`, memory `campfire-rain`; no writeup section yet). TODO `alcohol/pack #2`, `gameplay/misc #2`
(the built part: coal / charcoal block fuel, soul campfire, Unlit Campfire burns forever).

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-05 | A | `/omprobe setup`, wait 30 s, `/omprobe wine` | Kinds open 0, open + ice 2, open + magma 4, cellar 1, cellar + ice 3, cellar + magma 5; rates 100 / 200 / 75 / 200 / 400 / 150 % (VB-76); INFO shows the VintageClock factors and Brewin' climate -1 / 0 / 1 per spot | Kind 0 everywhere cold / warm: Brewin' `-03` `BnCHeatSources.climate` missing (the INFO line says so); cellar spots kind 0: light not updated yet, rerun `wine` after another 10 s; rates off with right kinds: `[time.wine.boosts]` / vinery-common values |
| SQ-OM-06 | A | after SQ-OM-05 (rain started by `setup`, 30 s or more): `/omprobe campfire` | Plain campfire on stone out; on a coal block and on a Quark charcoal block still lit; soul campfire on stone lit; `unlitcampfire-server.toml campfireLitTime = 0` from the world's serverconfig | Plain still lit: INFO `raining false` (run `weather rain` again) or `[campfire] rainDouses`; fuel ones out: `#creativeengineering:campfire/rain_proof_fuel` (`blocktags.js`) |

#### SQ-OM-VIN: Vinery cask and press hopper rules

Feature: [alcohol.md](../alcohol.md) 10.19 (logic of VB-93 / VB-94); TODO `alcohol/vineryport #7`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-07 | A | `/omprobe cask`, `/omprobe press` | Cask (recipe and items named in INFO): the same ingredient tops up its own slot only, the recipe's second ingredient is accepted, an item no recipe pairs with it and dirt are refused; only the output can be pulled; wine bottles from top and side; the recipe's juice is accepted from the top, another juice type refused, and refused while the first type is in. Press: apple and honeycomb into an empty press from a side, nothing from the top; with apples in, more apples yes, honeycomb waits; the apple recipe's bottle accepted, the honey recipe's other bottle waits; only the output can be pulled | `FermentationBarrelBlockEntity.ingredientKeepsWorking` / `juiceKeepsWorking`; `ApplePressBlockEntity.keepsWorking` |

#### SQ-OM-VH: Vinery houses in placed villages

Feature: [alcohol.md](../alcohol.md) 13 (server half of VB-97; VB-95 / VB-96 passed 2026-10-20); TODO
`alcohol/vineryport #14`. `place structure` ignores the biome, so it works on superflat.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-08 | A | for each X Z in 1000 0 (plains), 1400 0 (taiga), 1800 0 (savanna), 2200 0 (desert), 1000 400 (plains), 1400 400 (taiga), 1800 400 (savanna), 2200 400 (desert): `forceload add <X-96> <Z-96> <X+96> <Z+96>`, wait 10 s, `place structure minecraft:village_<biome> X -60 Z`, `/omprobe houses X Z`, `forceload remove <X-96> <Z-96> <X+96> <Z+96>` | At least 2 of the 8 `[omprobe] VILLAGE X Z winery=true` lines (the writeup expects about one village in two or three); the INFO lists the Vinery block entities found | None: the SQ-OM-00 `[vinery-houses]` lines and `pool_config.json` weights; `place structure` errors: console.log |

#### SQ-OM-LP: leaf pile slow height

Feature: [blockrunner.md](../blockrunner.md) (IW leaf pile row), test MT-25 in [mob_block_tweaks.md](../mob_block_tweaks.md)
(logic only; the WTHIT line stays in game); TODO `gameplay/blockrunner #3`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-09 | A | `/omprobe leafslow` | `slowFromLayers = 4`: 3 layers does not slow a zombie, 4 and 5 do | All slow from 4 even after SQ-OM-14: `LeafPileSlowMixin` not applied (SQ-OM-00); none slow: IW `leaf_piles_slow` off |

#### SQ-OM-FP: tier 2 feature ports (raids, Bottled Air) and tier 1 recipes

Feature: [feature_porting.md](../feature_porting.md) 2.4 (logic of FP-11, FP-13; FP-12 Comfort needs a real player in
range, it stays in game) and 1.1 (FP-15, recipe half of FP-09); TODO `ports/featureporting #3`, `#2`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-10 | A | `/omprobe raid` | A pillager added to a raid wave gets Glowing at `glowingAmplifier`, no particles, no icon | No effect: `RaidGlowingMixin` (`m_37718_`) not applied |
| SQ-OM-11 | A | `/omprobe air` | INFO fake player in water and under water; glass bottle right-click: air 0 -> 150 (`airPerBottle`); with a lava bucket in the other hand the water bottle in slot 10 is gone; a water bottle under water: the right-click is cancelled | "not in water": the fake player's baseTick did not update the water state, check it in game (FP-13); air unchanged: `BottledAir.onRightClickItem` registration |
| SQ-OM-12 | A | `/omprobe tier1` | 311 AE2 cable, 26 recycle smelting, 33 copper splashing recipes; name tag (leather, paper, 2 string), recycle hopper -> iron ingot, rail -> nugget, anvil -> iron block, copper block splashing -> exposed copper; no `createmixing/generator_*` or `create_generators:*` recipe; one `obsidian_generation_heated`; copper ore x10 -> 1 raw copper each, deepslate copper ore -> 1, silk touch -> the ore | Counts / ids: `feature_ports.js` (NCH ids); drops: `kubejs/data/minecraft/loot_tables/blocks/*copper_ore.json` |

#### SQ-OM-BM: baby squid, glow squid and dolphin

Feature: [baby_mobs.md](../baby_mobs.md) (server half of BM-04 to BM-07; looks and sounds stay in game); TODO
`ports/babymobport #1`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-13 | A | `/omprobe baby`, wait 2 s, `/omprobe babyxp` | Adult squid (control) drops items and leaves orbs; baby squid, glow squid and dolphin (isBaby true) drop no items and leave no orbs; a cod fed to a baby dolphin: age -24000 -> -21600, 2 cod left; 400 packs of 4 through `finalizeSpawn` (NATURAL): the first of a pack never a baby, the rest about 5 / 5 / 10 % babies | Drops: `LivingEntityBabyMixin`; feeding: `DolphinFeedMixin`; shares: `MobBabyMixin` / `BabyAges.rollBaby` |

#### SQ-OM-CFG: config flips (two restarts)

Feature: the config halves of MT-25, FP-11, BM-07 and VB-98 ([alcohol.md](../alcohol.md) 13); TODO
`gameplay/blockrunner #3`, `ports/featureporting #3`, `ports/babymobport #1`, `alcohol/vineryport #14`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-14 | A | `stop`; in `OUT/srv/config/`: `creativeengineering-common.toml` `[leaf_piles] slowFromLayers = 6`, `[raids] glowingRaiders = false`; `vinery-common.toml` `[village_houses] weight = 20`; `baby_mobs-common.toml` `[spawns] squidBabyChance = 1.0`; boot; `forceload add 0 80 239 111`, wait 30 s; `/omprobe leafslow`, `/omprobe raid`, `/omprobe baby` | `[vinery-houses]` lines say weight 20 (configured); leafslow: 5 layers no longer slows, 6 and 7 do; raid: no Glowing; baby: squid 100 % of pack members after the first, the first still never | Value not read: the key's section (Forge adds a missing key with its default; grep first) |
| SQ-OM-15 | A | `stop`; `vinery-common.toml` `[village_houses] enabled = false`; boot; `place template vinery:vinery_plains 0 -60 140`; then `stop` and restore the four srv config files from the instance | No `[vinery-houses]` line in `latest.log`; the `place template` command fails (template not found in console.log) | Lines still there: `VillageHouses` ignores `enabled` |

#### SQ-OM-JD: JEED effect providers

Feature: [effects.md](../effects.md) 3 (server half of EF-15; the JEI pages and EF-16 stay in game); TODO `ui/effects #4`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-16 | A | `/omprobe jeed` | No pack `jeed:effect_provider` recipe has an empty ingredient; Health Boost lists the Vinery standard, Solaris and Stal Wine; No Minimap lists the command block; Sunny has none; INFO: whether any recipe makes Cherry Cream Soda (the row's open bug: it can't be made) and the effects with no provider recipe | Empty ingredient: the recipe id in the FAIL line, `jeed_providers.js`; no Vinery wines: `WineEffects.winesByEffect` (the boot `jeed_providers.js: no Vinery wine providers` warning) |

#### SQ-OM-DP: world serverconfig payload

Feature: [deploy.md](../deploy.md) DP-08 (`world_serverconfig/`); TODO `pack/release #6`.

| ID | Group | Commands | Pass | If it fails |
|---|---|---|---|---|
| SQ-OM-17 | offline | Either the next real build (`python3 tools/build_deploy.py --notes "..."`), or a scratch build with `bd.ROOT`'s outputs `bd.OUT` / `bd.HISTORY` / `bd.LOG` / `bd.DEPLOYED` pointed into the scratchpad (pattern: `tools/changelog_test/deploy_test.py`); then `ls <OUT>/world_serverconfig`, `diff` each file against `configs_edited/server_config/` (else `defaultconfigs/`), grep the README | 8 files at 2026-10-22 (brickhopper, create, seasonhud, sereneseasons, solapplepie, sophisticatedbackpacks, stardew_fishing, unlitcampfire), each equal to its `configs_edited/server_config` copy (else `defaultconfigs`); present in an update build too; the README server and client steps each have the Existing worlds line naming the changed files | `build_deploy.py` `payload['world_serverconfig']` (line ~1227) and `render_readme` |
| SQ-OM-18 | A | `scratch_server.sh setup OUT sqOM2`; `rm OUT/srv/defaultconfigs/unlitcampfire-server.toml`; boot; `/omprobe serverconfig` (expect FAIL: mod defaults); `stop`; copy SQ-OM-17's `world_serverconfig/*` into `OUT/srv/sqOM2/serverconfig/`; boot; `/omprobe serverconfig` | Second run: 3 PASS (`campfireLitTime = 0`, `campfireRainUnlitTime = -1`, `soulCampfireLitTime = 0`, each read from `sqOM2/serverconfig`); first run differs (if the mod defaults equal the pack values, say so and drop this row) | Still defaults: the copy went to the wrong folder (`<level>/serverconfig/`, not `config/`) |

After the run: results into alcohol.md AL-81 to AL-84, AL-85 to AL-87, AL-90, AL-91, VB-76, VB-93, VB-94, VB-97, VB-98
(and 10.13, 10.16, 10.19, 11, 12, 13 status lines), remainder_buffer.md 3.6, baby_mobs.md BM-04 to BM-07,
feature_porting.md FP-09, FP-11, FP-13, FP-15, mob_block_tweaks.md MT-25, effects.md EF-15, deploy.md DP-08, memory
`campfire-rain` (no writeup); TODO rows `alcohol/brewinport #4`, `#6`, `#7`, `#8`, `alcohol/pack #1`, `#2`,
`alcohol/vineryport #7`, `#14`, `gameplay/blockrunner #3`, `gameplay/misc #2`, `ports/babymobport #1`,
`ports/featureporting #2`, `#3`, `ui/effects #4`, `pack/release #6`.
