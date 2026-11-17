# Small fixes batch (written 2026-11-16)

<!-- generated:status_index begin open_work sha1=d7181d17a00c -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 20 open TODO rows (In Progress 1, Testing 19), 3 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `blocks/flower-pots #10` | In Progress | S C P | Hanging pots place as wall lanterns: remove |
| `blocks/flower-pots #9` | Testing | S P | Exclude melon, moss and pumpkin blocks from the flower pot world |
| `dishes/ingredients #18` | Testing | S C P | Cutting banana bunch |
| `dishes/meals #23` | Testing | S C P | Fried egg and omelette batches |
| `dishes/pickles-and-ferments #6` | Testing | S C P | Vinegar craft |
| `farming/crops #52` | Testing | S | Red jungle grapes cannot be bonemealed |
| `farming/wild-crops #11` | Testing | S | Wild crop table generator misses Rustic Delight's JSON placed features |
| `food/types-and-tuning #27` | Testing | S P | Arc Furnace gilding: at most 4 gold |
| `gameplay/blockrunner #12` | Testing | S C P | Ice boat speed by ice type |
| `items/jars #12` | Testing | S P | Glass bottle and mason jar crafts make 4 |
| `jei/jei-ordering #7` | Testing | C | Remove fishing rods and backpacks from JEI ordering |
| `largecontent/mod-additions #63` | Testing | C | Straw rug: break overlay extends over 4 adjacent blocks |
| `largecontent/mod-additions #89` | Testing | S | A stick replaces an age-1 hops vine (SQ-HOP-04) |
| `mobs/bees #4` | Testing | S | Flower tag removals empty the tags that include them |
| `mobs/tweaks #25` | Testing | S | Quark endermen block pickup nerf enabled |
| `mobs/tweaks #27` | Testing | S P | Iron golem summon cooldown after golem losses |
| `recipes/engineering #3` | Testing | S P | Smaller findings |
| `structures/contents #9` | Testing | S P | Waystone structure glowstone -> yellow corundum |
| `tooling/conventions #7` | Testing | S C | `NCH.ieBlueprint`: repeated inputs never match |
| `ui/misc #7` | Testing | C | Splash text 'Stience!' |
<!-- generated:status_index end open_work -->

Source: owner chat 2026-11-16 (a pick from the "simplest bugs and tweaks" breakdown of [TODO.md](todo/TODO.md)), taken in
with the `intake` skill. Premise checks were read from the code the same day; file:line references are from that read.
Status: **built 2026-11-16, all phases; 8 jars installed (Common, Creative Delight, Brewing, Equipment, Worldgen, Compat,
Engineering, Devtools); nothing tested in game yet.** Build notes and deviations: section "Build notes" below.

Order: by jar, so each jar is rebuilt once. P1 (Common) first because the blueprint fix changes recipes other jars'
scripts call. P2 to P6 in any order. Every Java or startup change needs a full restart; P7 is script and config only.

## Intake

| # | Item (owner's words) | TODO row | Class | Finding (2026-11-16) |
|---|---|---|---|---|
| 1 | "mark the loot tuning sim as a component that must be migrated when the creative loot split occurs" | `tooling/generators-and-tests #9` | clear | Done 2026-11-16: [plan_rules_fixes.md](plan_rules_fixes.md) section 5b step 3 names it. The sim runs `kubejs/server_scripts/loot/05_tuning.js`, `07_rules.js`, `95_tag_capture.js` (`tools/loot_tuning_sim.js:73-76`), gone since wave 5 |
| 2 | "add the blueprint fix and test it" | `tooling/conventions #7` | clear | `neoCraftHelper.js:2879-2902` maps inputs one by one, no merge. Brief: [gear_archery.md](briefs/gear_archery.md) "Now" / "Build". No headless test runs the real NCH today |
| 3 | "fix the rustic wild crops" | `farming/wild-crops #11` | clear | `tools/gen_crop_worldgen_table.py:171-178` (dead `JAVA_PLACED` rows), `CROP_BLOCKS` at :235 lacks the Rustic blocks |
| 4 | "remove the unneeded flowers" | `mobs/bees #4` | clear | `kubejs/server_scripts/tags/animal_tags.js:28-39`; no narrower breeding tag exists (vanilla bee and hummingbird hardcode `#minecraft:flowers`, breeding_audit.md:22, :74, :112), so the dependent tags get the entries directly |
| 5 | "fix dragon hoe filter" | `gear/tuning #15` | premise | Already matches the rule: `BonemealCharmMixin.java:39-70` (Creative Equipment) refunds only via `BoneMealItem#useOn`; tree fertilizer, manure, IE / Botania fertilizer, organic mash are always consumed. Brief `gear_tuning.md` cites stale ids (`creativeengineering:bonemeal_charms`) |
| 6 | "allow jungle grapes to be bonemealed" | `farming/crops #52` | premise | Wall vine `GrapeVineBlock.java:87-100` takes bone meal; red and white share the code. The grapevine lattice (`LegacyLatticeStemBlock` via `StemBlock.java:107-110`) needs a lattice below (trunk rule) |
| 7 | "fix hops growth stage and bonemealing" | `largecontent/mod-additions #89` | premise | Stick-in-vine already fixed (C2, [plan_post_scratch_1115.md](plan_post_scratch_1115.md) section C). `HopsVineBlock.java:54-83`: any item at age 2 harvests 0-1 hops; only `Items.BONE_MEAL` (:68) counts as fertilizer, so manure / tree fertilizer on age 2 harvest |
| 8 | "fix the oversided breaking overlay on the straw rug and check sister blocks" | `largecontent/mod-additions #63` | clear | `models/block/straw_rug.json` has 4 extrude planes 8 px outside the block; the crack overlay draws every quad. Sisters: `hearthandharvest:hay_rug`, `farmersdelight:canvas_rug` (same model) |
| 9 | food/types-and-tuning #27 | same | clear | `cd_food.js:56` `gold: { mixer: 6, arc: 5 }` |
| 10 | items/jars #12 | same | clear | Mason jar: Vintage member `data/vintagedelight/recipes/mason_jar.json` count 3; glass bottle: vanilla, no override |
| 11 | dishes/ingredients #38 | same | premise | Stack 64 already built (`tools/gen_stack_sizes.py:59`, owner 11-02). Sap recipe pinned by `creativedelight/members #16` |
| 12 | mobs/tweaks #25 | same | premise | Endermen already in Quark's "Non Griefing Entities" (`quark-common.toml:1744`) but the "Game Nerfs" module is off (:1646) |
| 13 | ui/misc #7 | same | clear | `kubejs/assets/minecraft/texts/splashes.txt`, no "Stience!" yet |
| 14 | structures/contents #9 | same | clear | The glowstone is in Waystones' own `waystones:village/common/waystone` and `village/desert/waystone` (1 block each; Kaisyn's patch copies have none); Worldgen's `waystone_ruin_part` pool reuses the common one |
| 15 | blocks/flower-pots #14 | same | premise | Already built: `not_in_flower_boxes` (creativecompat `misctags.js:11`) and `SuppFlowerBoxPlantsMixin`; POT-12 tests it |
| 16 | blocks/flower-pots #9 | same | premise | The Flower Test world already excludes them (`flower_world.js:67-68`); the comment at :66 says some box still accepts them |
| 17 | blocks/flower-pots #10 | same | open | `beautify:hanging_pot` extends `LanternBlock`, so the Supplementaries wall-lantern tweak places it on walls; blacklisting Beautify there also hits its 4 lamps |
| 18 | jei/jei-ordering #7 | same | open | `kubejs/startup_scripts/jei/order.js`: rods :73-74, :80, :86, :93, :193; backpacks :64, :74, :80, :86, :94, :167, :198, :202; an `equipment` group exists (:47) |
| 19 | "dishes/ingredients #1 (6 bananas/ bunch)" | `dishes/ingredients #18` | clear | #1 is Cornmeal; the banana row is #18. `neapolitan:banana_bunch` (`i.banana_bunch`), `neapolitan:banana` (`i.neapolitan_banana`); no recipe uses the bunch |
| 20 | dishes/pickles-and-ferments #6 | same | open | Enzyme (crushed fungus) does not exist yet (`pickles-and-ferments #5`). Vinegar today: jam jar + mushroom pickling, `jams.js:117-120` |
| 21 | dishes/meals #23 | same | open | Rustic's 1:1 pot recipe `fried_egg_from_cooking_oil` (and a Slice and Dice mirror); More Delight smelts `#forge:cooked_eggs` to omelette (also smoking, campfire); CD `fried_egg_from_egg` smelting / smoking / campfire |
| 22 | recipes/engineering #3 | same | clear | `crystal.js:36` (F4), `dust.js:28/34/40` (F5), `redstone.js:228` (F9), `process.js:18, :55`, `transport.js:71-75` (F8); brief [recipes_engineering.md](briefs/recipes_engineering.md) lines drifted by about 2 |

## Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | Quark endermen nerf | premise | Turn Quark "Game Nerfs" on. On: Selective Mob Griefing with **endermen only** (creeper off the list), villager discount reset, no wool drops, falling block dupe, piston exploits, mushroom block removal, tripwire hook dupe. Off: iron farms, ice roads, Nerf Mending, Mending II, dimension-locked Elytra | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R2 | (new) golem farms | new | No Quark iron farm nerf. Own rule: when a village loses **5 iron golems within 10 min**, villagers summon none there for **1 in-game day (24000 ticks)**; all three in config | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R3 | (new) ice boats | new | No Quark ice road nerf. Own rule, boats only: blue ice 0.98 (today's ice speed), packed ice 0.96, ice 0.94; players and items keep vanilla. Other slippery blocks later (`gameplay/blockrunner #4`) | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R4 | straw rug overlay | clear | Clip the breaking overlay to the block, keep the fringe; straw, hay and canvas rugs | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R5 | hops | premise | Harvest only at age 3 (1-2 hops); every bone-meal-like item grows the vine at ages 0-2 | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R6 | jungle grapes | premise | Drop the trunk rule on the grapevine lattice only (age > 0, not mature); the grapevine stem keeps it | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R7 | dragon hoe | premise | Add an explicit "is bone meal" guard to the charm refund, fix the stale brief, close the row | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R8 | syrup / sap | premise | Stack size done; sap recipe stays pinned with `creativedelight/members #16` | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R9 | vinegar | open | Build now with any mushroom (`#forge:mushrooms`) as the enzyme: 3 apple mash + mushroom in the pickling jar; swap to crushed fungus when `pickles-and-ferments #5` lands. Jam route: agent default, unconfirmed: removed (the row says "change the vinegar craft") | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R10 | JEI ordering | open | Fishing rods get their own group under equipment, backpacks their own group under equipment; wardrobe backpacks and Simple Hats leave the JEI ordering for now (a cosmetic family later) | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R11 | hanging pots | open | Put the lantern-type hanging pots (with plants) in the Flower Test world next to the other hanging pots; keep or remove after looking at the potted plants. No placement change now | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R12 | flower pot world exclusions | premise | The real holders refuse them too: melon, pumpkin, carved pumpkin, jack o'lantern, moss block, moss carpet join `not_in_flower_boxes` | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R13 | fried egg batch | open | Replace Rustic's 1:1 oil recipe (and its Slice and Dice mirror) with oil + 3 eggs -> 3 fried eggs; 4 fried eggs -> 4 omelettes in the cooking pot. Every furnace (smelting) egg recipe becomes smoking + campfire (so the skillet cooks it) | 2026-11-16 | AskUserQuestion 2026-11-16 |
| R14 | blocks/flower-pots #14 | premise | Already built; close the row (agent default, unconfirmed: not asked, the ban tag and mixin exist) | 2026-11-16 | agent default, unconfirmed |
| R15 | banana bunch | clear | Knife cutting, 1 bunch -> 6 bananas | 2026-11-16 | chat 2026-11-16 |
| R16 | waystone glowstone | clear | Through Worldgen's template block swaps (no edited upstream nbt) | 2026-11-16 | agent default, unconfirmed |

## P1. Creative Common (rebuild, restart)

- **Blueprint repeats** (`tooling/conventions #7`): in `NCH.ieBlueprint`, compute the recipe id first, then merge
  identical inputs (same item or tag and NBT) into one counted ingredient. Comment: two different tags that overlap can
  still starve each other. Affects 9 recipes (archery, `redstone.js` 55 / 64 / 69 / 229, `music.js` 12 / 18).
- **Headless test:** new `tools/test_nch_blueprint.sh`, modelled on `tools/test_cd_food.sh` (pack Rhino, prelude):
  load the real `neoCraftHelper.js` and dicts, stub `e.custom` to record the JSON; assert `[x, x, y]` gives one input
  with count 2 and that the id is unchanged. `check_all.py` picks up `tools/test_*.sh` itself.
- Live: recipe dump before and after (only the 9 recipes change); one blueprint craft in the IE workbench.

## P2. Creative Delight (rebuild, restart)

- **Hops** (R5): `HopsVineBlock` harvests only at age 3; a "bone-meal-like" check replaces `Items.BONE_MEAL` at :68:
  a `BoneMealItem`, or in `#hearthandharvest:bonemeal_substitutes`, or in a new tag holding `create:tree_fertilizer`,
  `botania:fertilizer`, `immersiveengineering:fertilizer`, `hearthandharvest:fertilizer_bag`, `vintagedelight:organic_mash`. Ages 0-2: fertilizer
  grows +1; any other item passes. Check `hops.md` / the H&H writeup and the C-section notes of plan_post_scratch_1115.md.
- **Rug overlay** (R4): client mixin on the breaking-overlay render (`BlockRenderDispatcher#renderBreakingTexture`
  path) that skips quads outside the 0-16 box for blocks in a new block tag `creativedelight:clip_break_overlay`
  (`straw_rug`, `hay_rug`, `farmersdelight:canvas_rug`). Fringe still renders.
- **Arc gilding** (`food/types-and-tuning #27`): `cd_food.js:56` `arc: 4`.
- **Mason jar 4** (`items/jars #12`): member `mason_jar.json` count 4.
- **Glass bottle 4** (`items/jars #12`): NCH override of `minecraft:glass_bottle` with the same id, count 4, in
  `kubejs/data/creativedelight/kubejs/minecraft/recipes.js` (or Compat's vanilla recipe file if one exists there).
- **Banana bunch** (R15): `R.fdCutting` bunch -> 6 bananas; check what Neapolitan's bunch already does when used.
- **Vinegar** (R9): `jams.js:117-120`: pickling jar 3 apple mash + `#forge:mushrooms` -> vinegar jar; mark the
  mushroom as the stand-in for the crushed fungus enzyme (`pickles-and-ferments #5`).
- **Eggs** (R13): remove `rusticdelight:fried_egg_from_cooking_oil` and its Slice and Dice mirror; FD cooking pot:
  oil + 3 eggs -> 3 fried eggs; cooking pot 4 fried eggs -> 4 omelettes. Every furnace egg recipe (CD
  `fried_egg_from_egg`, More Delight omelette and any other `minecraft:smelting` egg output, from the recipe dump)
  is removed; its smoking and campfire forms stay or are added.
- Script items are server scripts in the jar tree, but the jar is being rebuilt for Java anyway.

## P3. Creative Brewing (rebuild, restart)

- **Jungle grape lattice** (R6): override `isValidBonemealTarget` in `LegacyLatticeStemBlock`: `AGE > 0 &&
  !isMature`, no lattice-below check. Update [vinery.md](vinery.md) 2.5 (the trunk rule now applies to the stem only).

## P4. Creative Equipment (rebuild, restart)

- **Dragon hoe** (R7): `BonemealCharmMixin:46` adds `context.getItemInHand().is(Items.BONE_MEAL)` before the refund
  roll. Fix [briefs/gear_tuning.md](briefs/gear_tuning.md) (ids `creativeequipment:bonemeal_charms`, jar Equipment;
  reading A retired).

## P5. Creative Worldgen (rebuild, restart)

- **Waystone glowstone** (R16): new `kubejs/generation_scripts/worldgen/misc_block_swaps.json` in the
  `TemplateBlockSwaps` format, with the glowstone position in `waystones:village/common/waystone` and
  `waystones:village/desert/waystone` -> `quark:yellow_corundum`; add it to `TemplateBlockSwaps.CONFIGS`. Positions
  read from the two nbt files at build time. Covers the village pieces and the Worldgen waystone ruin.
- Coordinate with plan_rules_fixes P2b (Creative Loot split rebuilds Worldgen): build either before or after, not during.

## P6. Creative Compat (rebuild, restart)

- **Golem cooldown** (R2): per-village record of iron golem deaths (village = the golem's home or nearest meeting
  point POI); 5 deaths within 12000 ticks block villager golem summoning (`Villager#spawnGolemIfNeeded` path) within
  that village for 24000 ticks. Config `[golems]` in CompatConfig: count, window, cooldown, switch. Player-built golems
  are not affected.
- **Ice boats** (R3): boat-only friction for `minecraft:blue_ice` 0.98, `packed_ice` 0.96, `ice` 0.94 (mixin on the
  boat's ground friction, read from a config map so `gameplay/blockrunner #4` can add slippery blocks later).
  Players, mobs and items keep vanilla slipperiness.
- **Flower box ban** (R12): add melon, pumpkin, carved pumpkin, jack o'lantern, moss block, moss carpet to
  `not_in_flower_boxes` (`misctags.js:11`); check which box the `flower_world.js:66` comment meant.

## P7. Scripts and config (`/reload` or restart, no jar)

- **Quark** (R1): `config/quark-common.toml`: "Game Nerfs" = true; "Disable Iron Farms" = false, "Disable Ice Roads" =
  false, "Non Griefing Entities" = ["minecraft:enderman"]; the rest as ruled. Copy to
  `configs_edited/root_config/quark-common.toml`. Check whether the server config folder needs it as well.
- **Flower tags** (`mobs/bees #4`): keep the breeding removals; add the removed entries straight to
  `creativeworldgen:ground_match/plants`, `template_trim/plants`, `creativecompat:physics/floats`, `flower_box_plants`,
  and check `chalk:chalk_cannot_draw_on` and `another_furniture:planter_box_placeables` (the SQ-RDB-02 list in
  `perf/runs/r1113/verdicts/c4_a.md:46`). Worldgen's and Compat's tags live in their jar trees: they ride on the P5 / P6
  rebuilds.
- **Wild crops generator** (`farming/wild-crops #11`): add wild_cotton, wild_bell_peppers, wild_coffee and
  bell_pepper_{red,yellow,green}_block to `CROP_BLOCKS`, rerun, drop the dead Rustic `JAVA_PLACED` rows and their
  `tools/check_known.csv` entries; `check_all.py --fast` passes. If the table feeds a dict, Common rebuilds.
- **Engineering findings** (`recipes/engineering #3`): F4 `crystal.js:36` 9 -> 8 coal blocks; F5 single cinder flour
  filling recipes at 25 mB beside `dust.js:28/34/40`; F9 `redstone.js:228` NBT gains `Bottle:"REGULAR"`; F8 stale
  comments at `process.js:18, :55`, step ids at `transport.js:71-75`.
- **Splash** (`ui/misc #7`): append `Stience!` to `kubejs/assets/minecraft/texts/splashes.txt`.
- **JEI ordering** (R10): `order.js`: a `fishing_rods` group and a `backpacks` group under `equipment`; wardrobe
  backpacks and Simple Hats entries removed. Startup script: restart.
- **Flower Test world** (R11): add the lantern-type hanging pots with plants beside the other hanging pots in
  `flower_world.js`; owner looks and rules keep or remove.

## Done when

Per phase: build, `check_all.py --fast --changed`, install, writeup and TODO row updated, `features.csv` +
`status_index.py`, changelog bullets (player-visible: hops, grapes, rug, gilding, jars, banana, vinegar, eggs, waystone,
golems, ice boats, Quark nerfs, flower boxes, splash, engineering recipes, JEI ordering, the 9 blueprint recipes now
craftable; none for: generator, loot sim, blueprint test, dragon hoe guard), queue rows with the writeup's prefix, commit and push to
`agent_staging`.

## Build notes (2026-11-16)

- **P1:** `_mergeBlueprintInputs` merges on the emitted JSON; `tools/test_nch_blueprint.sh` (runner `tools/nch_test/`) loads the
  real `neoCraftHelper.js` and `has.js`; 9 checks pass, 4 fail against the old helper.
- **P2 hops:** a real `BoneMealItem` passes to its own `useOn`; the substitutes tag and new `#creativedelight:bonemeal_like`
  (dict `i.t_bonemeal_like`) grow the vine in `use()`; the fertilizer bag loses 1 durability instead of shrinking.
- **P2 rug:** `@ModifyVariable` on the first `BakedModel` local of Forge's `renderBreakingTexture` (Forge method, no remap);
  block tag `#creativedelight:clip_break_overlay` (dict `i.t_clip_break_overlay`).
- **P2 eggs:** the egg cook family has `furnace: 0` (`_cook` skips the smelting recipe); plain `minecraft:egg` for the batch
  (`#forge:eggs` holds crocodile, turtle and parrot eggs). Also removed: `ends_delight` fried dragon egg and shulker omelette
  smelting, `alexsmobs:boiled_emu_egg` (all keep smoking and campfire forms).
- **P3:** the lattice's own right-click harvests at age 3, so bone meal acts at ages 1-2.
- **P4:** the guard gates only the refund roll; charm damage is unchanged.
- **P5:** positions [2, 0, 3] read from the Waystones 11.4.2 nbt; the file is hand-written, no generator.
- **P6 golems:** village = nearest meeting point POI within 64 blocks; state is a per-level SavedData. **Ice boats:** config
  `[boat_friction]` (both sides: the rider's client moves the boat).
- **P6 flower boxes:** no holder tag lists the six blocks, so the box that took them does it in code; the ban tag covers
  all three boxes.
- **P7 flower tags (deviation):** the re-adds are in `animal_tags.js`, not the Worldgen / Compat trees: only the script that
  removes the entries knows them, and jar scripts have their own scope. Planter and flower box tags get back only wild
  plants that were small flowers (no Botania functional flowers).
- **P7 generator:** the port's `ChanceFilter` hard-codes its rarities (reads no config); `RUSTIC_CHANCE` mirrors them.
  `test_mushroom_spawns`' known row (same gap) was removed too.
- **P7 engineering:** these scripts ship in the Engineering jar, so Engineering was rebuilt and installed. The Flower Test
  world script ships in Devtools, likewise.
- **P7 Quark:** no server copy needed (common config, shipped from `config/`).
- **TODO rows:** golems `mobs/tweaks #27`, ice boats `gameplay/blockrunner #12` (added at intake).

## History

- 2026-11-16: written from the owner's pick and 13 AskUserQuestion rulings (R1 to R13); R14 and R16 agent defaults.
- 2026-11-16 night: built, all phases (build notes above); coordinated with the process overhaul Phase 3 session.
