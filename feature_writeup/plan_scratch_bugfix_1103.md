# Bugfix plan from the 2026-11-02/03 scratch runs (r1102b, r1103, r1103w)

Owner decisions of 2026-11-03 applied to every unresolved FAIL. Sources: [scratch_run_2026-11-03.md](testing/scratch_run_2026-11-03.md),
`perf/runs/r1103/FINAL.md`, `perf/runs/r1103/{sA,sB,p2}/judged.md`, `perf/runs/r1102b/s*/judged.md`, `perf/runs/r1103w/w/results.md`.
Status: BUILT and INSTALLED 2026-11-03 19:45-19:52 (every item marked below); nothing has booted or run in game. Installed: creativedelight-1.0.0 (19:46, Vintage -17, Golden -02, Farmer's -04),
creativebrewing-1.0.0 (19:45), creativecalendar-1.0.0 (19:51), creativeworldgen-1.0.0 (19:51), creativeLabels-1.0.1 (19:52), creativeengineering-1.1.08 (patcherMod, 19:52).
Left unproven: 4.3 (why only the Vinery hook applied in r1103; hooks are now require = 1 and fail the boot if missed), 4.5 (fix aimed at FirstTickWater, unconfirmed), SQ-GLD-03 (bundle source not found; row reruns on a fresh bot).

Rebuild map: A = KubeJS data only (`/reload`); B = Creative Delight rebuild + install; C = Creative Brewing; D = Creative
Calendar; E = Creative Worldgen; F = Creative Labels (1.0.1 is still not in `mods/`); G = patcherMod. Everything marked B to G needs a
restart. One jar at a time, never while the game runs.

## 0. Already fixed in the working tree (verify, do not redo)

- SQ-LEM-02 lemon cutting: `fruitsdelight/recipes.js` now removes and remakes both recipes with 8 slices (the old `replaceOutput`
  kept the count).
- SQ-LEM-01 lemon pick yield: `LemonTreePickYieldMixin` now a `@ModifyArg` on the stack constructor, 2 to 5 lemons. The installed
  CD jar (00:08) does not load because of the earlier handler signature; the fixed build (00:48+) is not installed.
- SQ-PSU-06 jam from slices: the separate from-slices recipes are gone, the slice is a second accepted input of the pot recipes, so the
  mason-jar swap reaches them.
- SQ-VTD6-01 wild ghost pepper: `GhostPepperWildCropBlock.mayPlaceOn` names crimson and warped nylium, not `#minecraft:nylium`.
- SQ-FDB-04: `comfort_nourishment.json` now has a `gleaming_salad` row (nourishment 240 s); see 5.4 for the rest.
Action: rebuild CD (B), install, rerun SQ-LEM-01, -02, SQ-PSU-06, SQ-VTD6-01. Nothing else for these.

## 1. Vintage and jam jars (B/A)

### 1.1 Pickling takes the mason jar of vinegar (SQ-VNG-01)
- **BUILT 2026-11-03 (fork A):** `i.vinegar` = alias of `i.vinegar_jar`, `i.vinegar_bottle` = the dead id (jams.js removal only); leather and froglight recipes take `i.vinegar_jar` with no secondary (the jar's crafting remainder, the empty mason jar, comes back through the leftover slots, so a `secondary` jar would duplicate it); cans resolve through the alias, no regeneration. Restart (item_dict.js is a startup script); no jar rebuild. SQ-VNG-01 row refreshed.
- **Fix 2026-11-03:** the alias broke startup (`food dict: ingredient_vinegar_jar: vintagedelight:vinegar_jar already named ingredient_vinegar`): `food_dict.check` rejects two keys for one item. `f.ingredient_vinegar` removed from `food_dict.js`; `f.ingredient_vinegar_jar` is the one entry. `food_dict_index.json` still lists the bottle until the next `/cegen` run.
- Cause: `i.vinegar` (`item_dict.js` 14836) is `vintagedelight:vinegar_bottle`, which the 11-02 jar merge removed. The two other readers
  are `vintagedelight/recipes.js` 26 (leather) and 90 (the froglight loop). `food_dict.js` 520 `f.ingredient_vinegar` also uses it.
- Change: point the three callers at `i.vinegar_jar`; give the pickling recipes `secondary: i.empty_mason_jar` where they now
  return a glass bottle (leather and froglights use `secondary: i.glass_bottle` today, which would hand back a bottle for a jar).
  Then retire `i.vinegar` (or make it an alias of the jar key) so no script can name the bottle again; `jams.js` 85 removes recipes by
  output `i.vinegar`, repoint it to the id string via `R.remove({ id })` or keep the key as a documented dead alias.
- Verify: SQ-VNG-01 plus a JEI look at the four recipes: input jar, output item plus an empty mason jar.

### 1.2 SQ-VP-06 in detail (jam jars are not soft-fluid items)
- **BUILT 2026-11-03 (fork B):** better than option 1: the 9 data soft fluids name their Forge fluid in `equivalent_fluids`; Moonlight fills its fluid map from them before auto-creating one per Forge fluid, so no duplicate key and one soft fluid per jam (`src/own`, `src/main/resources`, `tools/gen_pack_jams.py`). Vintage `-17`, CD jar 19:43, not installed. Rows SQ-VP-06.
- Symptom: `/vtdtest fluids` 24 pass, 2 fail (`pepper_jam_jar: not a soft fluid item`, Supplementaries jar takes 0). Boot log:
  `Adding duplicate key 'ResourceKey[moonlight:soft_fluids / vintagedelight:<n>_jam]'` for ancient_fruit, banana, cherry, green_grape,
  pepper (pepper_jam), raspberry, red_grape, salmonberry, strawberry: 9 ids.
- Cause: `PackJams.java` 122-124 registers a Forge fluid `vintagedelight:<n>_jam` for every jam line with `ownJamFluid()`. Moonlight
  auto-creates a soft fluid with the same id for every Forge fluid, and the Vintage member also ships a data soft fluid
  `data/vintagedelight/moonlight/soft_fluids/<n>_jam.json` (container = the jar). The duplicate key makes one of the two lose, and the
  surviving auto soft fluid has no jar container. Other jars (apple_sauce, honey_vd, nut_mash, relish, vinegar, glow_berry, sweet_berry,
  mulberry) have no own Forge fluid and pass.
- Fix options, recommended first: (1) rename the 9 data files and their ids to `<n>_jam_jar` (soft fluid id and the file name), keep the
  Forge fluids for the cauldron/world fluid; (2) delete the 9 data files and attach the jar to the auto soft fluid in code
  (Moonlight `SoftFluid.Builder` on the fluid's id); (3) drop `ownJamFluid` for these 9 (loses the Forge fluid; not wanted). Check which
  of `kubejs/data/vintagedelight/...` and the member resources hold the live copy first (r1102b found a stale kubejs copy).
- Verify: `/vtdtest fluids` 26 of 26, jar fills from a tank, no duplicate-key line in boot.log. Rows SQ-VP-06 and SQ-VP-04.

### 1.3 Every jam and jar uses the mason jar (audit)
- **DONE 2026-11-03 (fork A):** the 10-30 recipe dumps hold no ingredients, so the audit scanned every jar's recipe JSON (mods/ plus nested jars) for jam / jelly recipes with a glass bottle: 98 hits, all covered by `jams.js` (Fruits Delight crafting, pot, filling, emptying; Upgrade Aquatic and Central Kitchen mulberry removed; Vintage deconstructs removed) except Handcrafted's Berry Jam Jar, now on the mason jar. A live check still needs a fresh `/dumpall` (SQ-PSU-06).
- Re-run an audit instead of trusting the earlier passes: dump all recipes whose output or input is a jam / jelly / jar item
  (`#forge:jam_jars`, `#forge:jams`, `fruitsdelight:*_jelly`, `*_jam_jar`, `mulberry_jam_bottle`) and list any that still take or return
  `minecraft:glass_bottle`. Use `/dumpall` recipe dump plus a grep, or a new `/cegen` check beside `gen_compost_values.py --check`.
- Known gaps to confirm closed: Fruits Delight pot and mixing recipes (done by `jams.js` 100-105), Slice & Dice copies of the pot recipes
  (copied after, so they inherit), Create spout and item-drain pairs, Upgrade Aquatic mulberry jam, Item Drain emptying returns the jar.
- Verify: audit prints 0 lines; SQ-PSU-06, SQ-VP-06.

## 2. Recipe and loot bugs (A, B)

### 2.1 Duck fillet dupe loop (SQ-CDFW-07)
- **BUILT 2026-11-03 (fork A):** `F.meatCut(i.raw_duck, i.duck_fillet)`. `/reload`.
- Cause: `abnormals_delight/recipes.js` 20 `F.meatCut(i.t_raw_duck, i.duck_fillet)`. `#forge:raw_duck` holds `untitledduckmod:raw_duck`
  and `abnormals_delight:duck_fillet` (exported tag list), so a fillet cuts into 2 fillets, in the board recipe and its S&D twin.
- Change: cut from the item only, `F.meatCut(i.raw_duck, i.duck_fillet)`. Keep `i.t_raw_duck` in the noodle recipes (a fillet there is
  fine). Do not remove the fillet from the tag.
- Verify: SQ-CDFW-07 (no recipe whose input is the fillet and output the fillet).

### 2.2 Water kettle never brews (SQ-RSP-06, -15)
- **DONE 2026-11-03 (fork A):** batch fault confirmed from bytecode: `KettleRecipe.matches` collects up to 2 non-empty slots and calls `RecipeMatcher.findMatches`, one item per slot. Rows SQ-RSP-06 / -15 now use one leaf in each slot. No code.
- Finding: probably a batch fault, not a pack bug. The row puts `black_tea_leaves Count:2b` in slot 0 only (`p2_R.qb` 209). The recipe
  (`kettle.js` 29) has two ingredient entries, and Farmer's Respite matches one item per slot, so it needs one leaf in each of two slots.
  The judge's "water base does not match" was a guess: `ce$tankFluid()` returns water for a pure `water_level` tank and
  `KettleBases.get` is water for this recipe, and RSP-05 (honey on a tea tank) brewed.
- Change: first reproduce by hand: two leaves in slots 0 and 1, then one stack of 2 in slot 0. If only the stack fails, fix the batch
  (SQ-RSP-06 and -15 rows); if both fail, log `ce$baseMatches` inputs for the first failing call and fix the base match.
- Verify: SQ-RSP-06, -15 (BrewTimeTotal 200).

### 2.3 Cooking-station overflow (SQ-RB-21b) [decided: five stations]
- **BUILT 2026-11-03** ([remainder_buffer.md](remainder_buffer.md) 3.8): shared `development/shared/stations` (`LeftoverPlan`,
  `StationContainers`) on all five; also containers only in the container slot, one automation face layout, Pickling Jar
  progress per recipe, Kettle time `[cooking] kettleBrewTicks`. CD jar 19:13 (Miner's `-06`, Vintage `-16`) and patcherMod
  1.1.08 18:36 built, not installed. Which stations lack a container slot: none (all five have one).
- Cause (judge, matches the code): `CopperPotBlockEntity.leftoversFit` 334-364. With the container slot (5) already holding the served bowl, `recycleRoom` is 0, so the batter's
  bowl leftover has to use the one leftover slot (7), which the oil's bottle took first. Two leftover kinds and one slot stall the pot (`stallWhenFull`).
- Rule: a leftover goes to the container slot when the recipe's result can be plated with that container (the leftover equals the served container item), up to the slot's
  stack size; any other leftover goes to the overflow (leftover) slot; only when both are full does the station stall.
- Stations (each has its own remainder code today): FD cooking pot (`CookingPotRemainderMixin`, `CookingPotMenuRemainderMixin`), Miner's copper pot
  (`CopperPotBlockEntity`), Farmer's Respite kettle (`KettleRemainderMixin`, `KettleMenuRemainderMixin`, `KettleTankMixin`), CD oven (`OvenBlockEntity`), Vintage pickling
  jar (`FermentingJarBlockEntity`). Not included: Brewin' keg, Create Central Kitchen.
- Change: one shared helper in `cooking/RemainderBuffer` (container-match room, then overflow slot, then stall) called by all five instead of five copies of the
  logic; the old "spares never go there (automation cannot empty that slot)" comment is dropped, so check hopper extraction from the container slot of each station and that
  automation cannot pull plating containers mid-craft. Stations without a container slot (oven, pickling jar, kettle) get only the overflow-slot half unless they have a
  matching slot; list which in the writeup.
- Verify: SQ-RB-21b original text (3 meals, bowl in slot 5, two leftover kinds), the bowl-only rerun, the same two-leftover case on the FD pot, and one case each on the
  kettle, oven and pickling jar; SQ-RB-21 family.

### 2.4 Potted gossypium and potted hemp (SQ-POT-04v)
- **BUILT 2026-11-03 (fork B):** the two pack loot tables (pot + gossypium / IE hemp seed); the probe data shows no other potted block losing its plant. Row SQ-POT-04v added.
- Causes: Miner's `MDBlocks.POTTED_GOSSYPIUM` (line 39) has no loot table; Immersive Engineering 9.2.4 `potted_hemp.json` drops
  `minecraft:air`. Both drop nothing, so pot and plant are lost.
- Change: pack loot tables `kubejs/data/miners_delight/loot_tables/blocks/potted_gossypium.json` and
  `kubejs/data/immersiveengineering/loot_tables/blocks/potted_hemp.json`: `minecraft:flower_pot` plus the plant item
  (gossypium; IE hemp seed or `immersiveengineering:hemp_fiber` source: read what the player put in, `i.seed_hemp`). Mirror in the
  member resources if the Miner's jar should be self-contained. Check the other pack-made potted blocks for the same gap
  (`kubejs/exported/blocks/flower_pots.json`).
- Verify: SQ-POT-04v survival break drops pot and plant.

### 2.5 Hanging roots throws (SQ-POT-03)
- **BUILT 2026-11-03 (fork B):** the full message was in `pot_probe.json`: Immersive Weathering's `WallRootsBlock.getStateForPlacement` sets `facing` to the clicked face (UP throws, for players too). CD mixin `immersive_weathering/WallRootsVerticalFaceMixin` returns no state for a vertical face; `pot_probe.js` records `stack` frames; F1 already struck, F12 added. CD jar 19:43, not installed.
- Known: `FAIL PP-03 ... minecraft:hanging_roots (IllegalArgumentException: Cannot set property DirectionProperty)` in 6 holders (flower_box,
  beautify hanging_pot, planter box, vinery pot, both Supplementaries planters). The message is Minecraft's `BlockState.setValue` on a
  state that lacks the property, truncated by the probe at 50 characters. `HangingRootsBlock` has only `waterlogged`, no facing, so
  a holder is copying a `facing` or `face` property onto the plant's state (it works for roots-like plants that have none because
  they are not tried).
- Cannot be named from the log; the stack is missing. Change: make `pot_probe.js` print the full stack (`e.printStackTrace` through
  `Java.loadClass('java.io.StringWriter')`, or log the exception class and the first 6 frames), run `/pottest` for hanging_roots on
  one holder, then fix at that frame (guard the `setValue` with `hasProperty`, via a mixin in patcherMod or CD).
- Also strike F1 in `flower_pots.md` 3 (did not reproduce).
- Verify: SQ-POT-03 with 0 `hanging_roots` exceptions.

### 2.6 Beautify hanging pot drops its plant (SQ-POT-04)
- **DONE 2026-11-03 (fork B): not a bug.** The probe clicked plant, take back, plant, take back and broke an empty pot; upstream's loot table drops the plant. `pot_probe.js` replants once on such a toggle. flower_pots.md F14.
- Finding: `beautify:hanging_pot` drops only the pot; the plant is lost (item loss). The pot stores the plant in its block entity
  (`HangingPot.validFlowers`, 25 items, bone meal and shears act on it).
- Change: open the class in the jar (`javap -p`), then add a loot table `kubejs/data/beautify/loot_tables/blocks/hanging_pot.json` that
  drops the pot and the stored plant if the plant is a plain block-state or a copyable NBT field; if it is an arbitrary item in the
  block entity, a small mixin on `onRemove` / `playerWillDestroy` is needed instead. Pick after reading the class.
- Verify: SQ-POT-04 (`PP-04 beautify:hanging_pot returns the plant on break`).

## 3. Potions, brewing, member switch (C, B)

### 3.1 Levitation reagent with and without Ender's Delight (SQ-ENM-03)
- **BUILT 2026-11-03 (fork B):** reagent `['ends_delight:shulker_meat_slice', 'minecraft:shulker_shell']` (first registered), `mix()` skips an unknown or empty reagent with a warning. Creative Brewing jar 19:45, not installed. Rows SQ-ENM-03, -05b.
- Identified: reagent `endersdelight:shulker_filet` (`potions.js` line 47, `CUSTOM_POTIONS.levitation`). With no Ender's Delight jar the
  Ingredient is empty and `registerPotionBrewing` throws (startup crash). With the jar, the filet is merged away (`merges_endersdelight.json`:
  `endersdelight:shulker_filet` into `ends_delight:shulker_meat_slice`, both stay registered, the old one is purged), so even with
  the jar the reagent should be `ends_delight:shulker_meat_slice`.
- Shulker shell is used elsewhere: dragon breathing drop (`dragon_breathing.js` 8), Shulker Bowl craft, stuffed shulker, Quark shell
  tooltip, item physics. It is not a reagent in any brewing mix, vanilla or pack (`potions.js` reagent list checked), so it does not clash.
- Change: `reagent: Platform.isLoaded('endersdelight') ? 'ends_delight:shulker_meat_slice' : 'minecraft:shulker_shell'`. Confirm the
  End's Delight id and whether the Creative Brewing jar can name it (it is a CD member, always present). Also guard `mix()` so one
  empty ingredient logs a warning instead of throwing for the whole event.
- Verify: SQ-ENM-03 (boot without the jar), SQ-ENM-05b, then the levitation brew in game with and without Ender's.

### 3.2 enders_additions member switch (SQ-ENM-04b), more info
- **BUILT 2026-11-03 (fork B):** option (a): `bundle_extra_mods=enders_additions` in `members/ends/gradle.properties`, read by `writeMembersList` in both bundle `build.gradle`s; the built members file lists it. `CD.on` resolved through `isMember` (so it was always on, not skipped). CD jar 19:43, not installed. Row SQ-ENM-04b.
- Facts: the End's Delight member jar holds two mods: `ends_delight` (the repack) and `enders_additions` (Ender's 1.1.4 plants and
  dishes, folded in 11-02). `creativedelight_members.txt` is generated from `bundle_members` as one `mod_id` per nested jar, so it lists
  `ends_delight` only. `CDConfig` builds `[members]` switches from that list, so `enders_additions` has no switch. Boot log: "Incorrect
  key members.enders_additions was corrected from true to its default, null".
- Consequence is worse than "always on": `CDConfig.member()` returns false for an id with no switch, and `additions.js` starts with
  `if (!global.CD.on('enders_additions')) return`. Check how `global.CD.on` resolves (it may also test `ModList`); if it calls
  `member()` the file is skipped. SQ-ENM-01/-02 passed with its recipes, so it probably resolves another way. Read the `CD.on` definition
  before choosing.
- Options: (a) write secondary mod ids into the members file (a `bundle_extra_mods` property per member in `build.gradle`, or read each
  nested `mods.toml`), so `enders_additions` gets its switch back and `ends_delight.md` 5.x stays true; (b) drop the switch from the docs and
  the row. Recommended: (a), it is two lines in `writeMembersList`.
- Verify: SQ-ENM-04b (`members.enders_additions = false` plus `/reload` removes the dishes).

## 4. Worldgen, seasons, labels (E, D, F)

### 4.1 Punch on the struck (meteorite) pyramid (SQ-CWG-06, SQ-WGE-12)
- **BUILT 2026-11-03:** `punch` in `META_PART_KEYS`, checked boolean and "structure parts with overlap only"; summary list prints it; Worldgen jar 19:45 (scripts ship in it), not installed.
- Cause: `structure_overrides.js` line 110 `META_PART_KEYS` lacks `punch`; `MetaStructure.Part` has `punch` (BOOL, only valid with
  `overlap`, line 214) and the definition for `meta/meteorite_desert_pyramid` is dropped with `unknown field "punch"`.
- Change: add `'punch'` to `META_PART_KEYS`, and in the part validation around line 660-672 add `punch` boolean check and the
  same rule as Java ("only for structure parts with overlap"). Check the staged JSON writer passes the key through.
- Verify: `place structure creativeengineering:meta/meteorite_desert_pyramid`, no `[meta-structures] ... not a loaded structure` line,
  SQ-CWG-06, SQ-WGE-12.

### 4.2 Wild melons in jungles, and mini melons beside them (SQ-WCD-02)
- **BUILT 2026-11-03 (data):** `kubejs/data/creativedelight/worldgen/placed_feature/wild/melon.json` heightmap `OCEAN_FLOOR_WG`; ships in the Creative Delight jar, so it needs the CD rebuild. SQ-WCD-02 counts melons and mini melons.
- Finding: 0 melons in 98 jungle chunks (r1102b and r1103). The rule `wild_crops.json` `melon`: `warm wet !cliff`, feature
  `creativedelight:wild/melon`, removes `terralith:jungle/patch_melon`, `minecraft:patch_melon_sparse`, `minecraft:patch_melon`.
  Leading hypothesis: the placed feature `wild/melon.json` uses `heightmap: MOTION_BLOCKING`, which lands on the jungle canopy, while the
  patch (`patch_melon.json`, 64 tries, y spread 3) needs grass within 3 blocks below; vanilla's placement uses `OCEAN_FLOOR_WG`
  (`HEIGHTMAP_TOP_SOLID`). Hamimelon uses the same MOTION_BLOCKING but sits in open deserts, so it works.
- Mini melons: the `mini_melon` rule has the same `where` (`warm wet !cliff`), feature `delightful:patch_mini_melon`, so they spawn in
  the same biomes. Whether they spawn beside the melons depends on that feature's own placement, which this repo does not hold; they
  would land by their own rarity, not beside the melon patches. Count them in the same chunk box in the rerun.
- Change: switch `wild/melon.json` heightmap to `OCEAN_FLOOR_WG` (or `WORLD_SURFACE_WG`), keep rarity 6. If still 0, check the
  `remove_features` step: it removes `minecraft:patch_melon`, make sure it does not match the new placed feature through the
  configured-feature name.
- Verify: SQ-WCD-02 in a jungle box (melon count, mini melon count, cocoa count for reference).

### 4.3 Modded grass seasons (SQ-PST-08, -10, -11, -11b) [owner rule 2026-11-03, replaces the winter rule]
- **BUILT 2026-11-03:** `GRASS` `{N,O,N,N}` / `{O,O}`, `[crop_seasons] grassOffPenalty` 0.5 (live config and `configs_edited`); four per-target mixins (`IWGrassSpreadSeasonMixin`, `BotaniaGrassSeasonMixin` HEAD, `VineryGrassSlabSeasonMixin`, `VintageLushGrassSeasonMixin` HEAD), require = 1, via `seasons/GrassSeasonHook` (logs `grass season hook live`). javap: all four targets declare the hooked method and call `ServerLevel.m_46803_`, so the r1103 non-application is not explained offline; the boot will now fail loudly instead. Calendar jar 19:35, not installed. Arid biomes share the wet/dry slots, so grass is slow there too.
- Rule: grass spreads normally in winter, spring and autumn, and slowly in summer and in any tropical biome (the tropical wet and dry seasons).
  This reverses today's `GRASS` type, which is off in winter only (`CropSeasons`: temperate `{N,N,N,O}` for spring, summer, autumn, winter; wet/dry `{N,N}`).
  New arrays: temperate `{N,O,N,N}` (summer off), tropical (wet/dry) `{O,O}` or the family's two slots both off. "Slowly" is half speed for every grass: a new Creative Calendar key
  `[crop_seasons] grassOffPenalty = 0.5` (half the spread attempts are refused), because the shared `offPenalty` stays 0.25 for crops; `refuseSpread` reads the grass
  value when the type is `GRASS`. Check how `typeOf` picks the family so the tropical biome lists (`Family.WARM_WET`, `WARM_DRY`)
  also apply to the grass type; the grass type may need to be tropical-aware rather than a single array.
- Scope: this applies to every grass the mixins touch (vanilla `GrassSpreadSeasonMixin`, IW, Botania, Vinery slab, Vintage lush grass), so it replaces the
  winter slowdown the SQ-PST rows test. Rewrite SQ-PST-08, -10, -11, -11b (and the vanilla grass row) as summer-slow / winter-normal. Open: confirm the vanilla
  grass block follows the same rule (it is the same `GRASS` type today).
- Why the modded ones do nothing today: `ModdedGrassSpreadSeasonMixin` hooks `m_213898_` / `m_213897_` at `getMaxLocalRawBrightness` on `ServerLevel` in four target
  classes with `require = 0`. Mixin only injects methods the target class itself declares; if a target inherits `randomTick` or calls the light check through
  `LevelReader`, nothing applies and `require = 0` hides it.
- Steps: (1) temporarily set `require = 1`, boot, read the `InjectionError` per target, or `javap -p -c` each target for the real method and call owner;
  (2) fix per target, one mixin per target so a missing mod cannot fail the others; (3) change the arrays/family; (4) never leave `require = 0` on the hook.
- Applies to vanilla, IW (including its mycelium / rooted-dirt second roll), Botania, Vinery and Vintage grasses. Add `grassOffPenalty` to `creativecalendar-common.toml`
  and its `configs_edited` copy.
- Verify: summer vs spring spread counts on vanilla and one modded grass (about half in summer), a tropical biome in any season, winter normal.

### 4.4 Sticky note on a jar top, not a sack (SQ-LBS-03) [decided: jar and Pickling Jar]
- **BUILT 2026-11-03:** tag `creative_labels:paper/note_on_top` (the existing namespace, not `creativelabels:`), jar and Pickling Jar tops use `centreNoteFit` (the Pickling Jar lid is 2.5 px down, past the shape test's 2 px), other centre-note tops and all bottoms none. Labels 1.0.1 jar 19:43 (1.0.1 never left build/libs, so no bump), not installed.
- Cause: `Spots.fit` 298-300: centre-note hosts take notes on side faces only through `centreNoteFit`; top and bottom use `shapeFit`,
  which lets the jar lid and the sack neck carry spot C.
- Change: tops (UP) of `supplementaries:jar` and the Pickling Jar take a note; the sack top and bottom stay refused. Add a tag `creativelabels:note_on_top`
  (the two jars; other jars stay out); in `fit`, for a centre-note host with `face` UP or DOWN: tag member -> `shapeFit` (UP only; DOWN refused for all);
  otherwise `Fit.NONE`. Check the Pickling Jar's block id (Vintage Delight member) and its shape has a flat top at spot C. Update `sticky_notes.md` 3.3a and
  the row (SQ-LBS-03 currently expects the Pickling Jar top refused: flip it).
- Verify: SQ-LBS-03 (jar and Pickling Jar tops accepted, sack top and bottom refused, trash can and botany pot tops as before).

### 4.5 Labels drop when the support block goes to air (SQ-LBS-04)
- **BUILT 2026-11-03 (partly a guess):** the probe already used `setBlockAndUpdate` (flag 3), and the Labels path (`neighborChanged` / `updateShape` -> `hostChanged` -> `popResource`) is sound. Suspected cause: Creative Worldgen `FirstTickWater.ticking` left true after a fluid tick that threw or was cancelled, which skips every `popResource`. Now cleared at every level tick end and checked per thread (Worldgen jar 19:45). `zlbs_probe.js` gained a wall-torch control (tells global drop loss from a Labels bug) and a piston case. Not verified in game.
- Finding: code path exists (`FacePaperBlock.updateShape` / `neighborChanged` -> `hostChanged` -> `Block.popResource` 447). The probe
  `zlbs refit` sets the host to air with a plain `/setblock`-style call; a flag-2 `setBlock` fires no `updateShape` or `neighborChanged`, so
  nothing could drop. Likely a probe fault.
- Change: reproduce by hand with the survival break of the host and with `/setblock ... air destroy` (flag 3); check
  `zlbs_probe.js` for the `setBlock` flags. If a real break drops nothing, trace `updateShape` (the `level instanceof ServerLevel`
  guard, and `be.sync()` before the drops are computed). Fix the probe to use flag 3 either way, and add a piston-push case.
- Verify: SQ-LBS-04 20 of 20 with drops: blank labels, written notes with Text.

### 4.6 Waystone frequency (SQ-WGE-05a, -05b)
- **DONE 2026-11-03:** SQ-WGE-05b row text 8 of 10.
- Accepted as is. Change the rows only: -05b "waystone in at least 8 of 10 villages" (r1103w: 8 of 10, none at -240 1120 and 2496 -1488),
  -05a keeps the 1 per 60 chunks look. No code.

## 5. Tuning and tags (A, B)

### 5.1 More Delight knives (SQ-MRD-01)
- **BUILT 2026-11-03 (fork B):** the 10-30 dump already had levels 0 / 1 (KubeJS per-item tier copy); its speed column is the tier speed, which `tuneTools` never set. `applySpec` now sets `t.setSpeed` with the dig speed (startup script, restart). Real dig speed was likely already 2 / 4 (item field), so no changelog bullet.
- Finding: `tune_tools.js` 134-135 sets wooden knife `miningSpeed 2, miningLevel 0` and stone knife `4, 1`; the live dump still shows speed
  8 (and harvest levels 3 to 4). `tuneTools` applies `setDigSpeed` (Digger items only) and `t.setLevel` on the tier; the knives either are not
  `DiggerItem`s, or share one `Tier` object across both (a level set on a shared tier would be overwritten), or the keys name a different
  jar than the installed 25.07.28 (`item_dict` 13807-13808 point at `moredelight:*`).
- Change: dump the two items' class, tier and tier identity (`/dumpgear` plus a one-off probe), then apply the right path: per-item
  tier copy before `setLevel`, or set the attribute through the tool's `getTier` hook. Mining level 0 for wood, 1 for stone (matches flint
  knife 4 / 1, iron 6 / 2).
- Verify: SQ-MRD-01 (`miningSpeed=2` and `4`), and `can_harvest` for the tier tags.

### 5.2 Chicken curry id (SQ-LGM-10)
- **BUILT 2026-11-03 (fork A):** Ender's ids renamed; no other reference to the old ids outside test snapshots. `/reload`.
- Cause: `endersdelight/additions.js` 29-30 and 42-43 reuse `chicken_curry` and `chicken_curry_cooked_manual_only`, the ids Large Meals' curry already
  uses (`largemeals/recipes.js` 33 and 63-64); Ender's wins and Large Meals' curry has no recipe.
- Change: rename Ender's ids to `ender_chicken_curry` and `ender_chicken_curry_cooked_manual_only` (the item key `i.chicken_curry` stays).
  Check nothing refers to the old ids (JEI order lists, `removeById` calls, scratch rows, the SQ-LGM-10 text).
- Verify: SQ-LGM-10 (both curries have a recipe, no duplicate-id line).

### 5.3 Ghost pepper soil and the nylium tag (SQ-VTD6-01) [investigated: do not touch the tag]
- **DONE 2026-11-03**: no code; SQ-VTD6-01 extended with the seed-planting check (three soul soils only) and the nylium wild case.
- Owner aim: planted ghost peppers only on soul soil, rich soul soil and nulch (IW fertile nether soil); the wild ghost pepper still generates on nylium.
- Already true in code, no tag edit needed: planted crop `GhostPepperBlock.mayPlaceOn` and `canSurvive` use `#vintagedelight:ghost_pepper_soil` only (filled by
  `vintagedelight/tags.js` 56 with `soul_soil`, `rich_soul_soil`, `iw_nulch`); the wild block `GhostPepperWildCropBlock` accepts the two named nylium blocks plus
  that tag, and no longer reads `#minecraft:nylium`.
- Investigation of removing `grass_block` from `#minecraft:nylium` (Terralith adds grass, mycelium, moss): the tag has no effect on brown/red mushrooms
  (vanilla `MushroomBlock` uses `#mushroom_grow_block`: mycelium, podzol, nylium blocks by name). What it does touch: vanilla crimson/warped fungus, roots and
  nether sprouts (their soil check is `#nylium`, soul soil or the dirt tag), so today they can be planted on grass, mycelium and moss. No pack script, Terralith
  feature or other mod datapack reads the tag for worldgen except Hearths' Crimson Forest decorations (Nether, nylium floor, grass never there), PresenceFootsteps
  (sound map) and FramedBlocks (`camo_sustain_plant`). I found no mod class naming the tag besides WorldEdit and KubeJS. Removing grass would only stop planting
  those nether plants on grass, which Terralith presumably wanted; not needed for the ghost pepper.
- Decision applied: leave the tag as Terralith ships it. Rerun SQ-VTD6-01 after the Creative Delight rebuild (section 0); add a row that a ghost pepper seed
  cannot be planted on grass, dirt, farmland or nylium, only on the three soils, and a wild one still spawns on nylium.

### 5.4 Gleaming salad (SQ-FDB-04) [decided: in line with the rule]
- **BUILT 2026-11-03 (fork A):** `GOLDEN_EXTRA` in `gen_comfort_nourishment.py` (run offline: 2 lines changed, salad 120 s and the strip key `vinegar` -> `vinegar_bottle`); `food_catalog.py` OVERRIDES salad, and `gleaming_salad` added by hand to `food_types.json` and the farmersdelight member script (a full `food_catalog.py` run on the 10-30 dumps would reclassify 103 foods; `--check` still lists 2 other member scripts out of sync, not touched). Golden tuning multipliers do not reach it (it is a salad, not a golden type). Restart.
- State: `comfort_nourishment.json` has `gleaming_salad` as nourishment 240 s by hand; it is in no `food_types.json` list, so the type rules never see it.
- Change: classify it so the generator's own rules decide: (1) `tools/food_catalog.py` `OVERRIDES`: `'farmersdelight:gleaming_salad': 'salad'`; (2) mark it for golden
  tuning with a `GOLDEN_EXTRA` set in `gen_comfort_nourishment.py` read next to `golden_foods()`, so the golden rule applies: Nourishment 120 s (the 240 s hand row goes);
  (3) `/dumpall`, `/cegen run food_catalog`, `/cegen run comfort_nourishment`; check the golden food tuning (`food.md` 3.5 `golden`/`gilded` multipliers) lists it too.
- Verify: SQ-FDB-04 (Nourishment 120 s, 4 servings, bowl back), SQ-CN salad rows.

### 5.5 Vinegar gives Nausea only when drunk (SQ-PSU-09) [decided: 10 s]
- **BUILT 2026-11-03 (fork A):** `ItemEvents.modification` in CD jar startup `vintagedelight/vintage_items.js` adds Nausea 200 t to the vinegar jar; the comfort applier touches only Comfort / Nourishment, so it stays. Needs the CD rebuild and a restart. `pickles-and-ferments #7` Dropped.
- State: the jar (`ModFoods.VINEGAR_JAR`) has Vintage's Nourishment 3600 t; the pack rule `comfort_nourishment.json` `vinegar_jar: none` removes it, so the jar
  currently has no effect. The "Foul Taste for foods with vinegar" idea (`pickles-and-ferments #7`) was never built.
- Change: the vinegar jar, when drunk, applies Nausea for 10 s (200 ticks); vinegar used as an ingredient adds nothing to any dish; no Foul Taste effect is made.
  Do it through the pack's food effect path (the one that strips the jar's Nourishment: `comfort_nourishment.js` and the food tuning), not in the upstream jar.
  Close `pickles-and-ferments #7` as dropped; the SQ-PSU-09 row becomes "Nausea 10 s, no Nourishment". Note the can generator uses the vinegar as an additive
  (`VINEGAR` in `gen_can_types.py`): its key follows 1.1 once `i.vinegar` is repointed to the jar, and cans must not carry the Nausea.
- Verify: drink the jar (Nausea 10 s), eat a vinegar dish and a vinegar can (none).

### 5.6 Bales and salt blocks stay soft at 0.88x (SQ-VTD7-02)
- **DONE 2026-11-03 (fork A):** `salt:raw_rock_salt_block` is already out of soft (natural_rough, `blocktags.js` comment); row text fixed. No code.
- Decision: keep. `blocktags.js` 341-346 already has bales, bags and the salt blocks in `runner.soft`, and the changelog (unreleased.md 183) says
  0.88x. The row text (1.05x, `natural_smooth`) is stale: fix the row.
- Check: `salt:salt_block` is crafted from salt (confirm the recipe in the Salt mod jar) and `vintagedelight:salt_block` merges into it; the
  third entry `salt:raw_rock_salt_block` is the rock-salt block, which should not be soft: remove it from the list if its recipe is rock salt,
  unless the owner wants it soft (its comment says "raw salt, like raw stone"). Smooth salt, salt bricks and pillars stay 1.25x.

## 6. Other results, no change decided
- **DONE 2026-11-03 (fork B):** SQ-FDB-05 config comment and docs (restart needed). SQ-GLD-03: the bundle is not from the drinks (golden milk has no food effects yet showed it freshly; refreshed at 279 t across drinks 8 s apart): environmental, row reruns on a fresh bot. Found instead: golden / enchanted milk cleared nothing (Forge cures by stack); `ShinyItems.GoldenMilk` cures as a vanilla milk bucket. Golden `-02`, CD jar 19:43, not installed.

- SQ-FDB-05: config `placeAs` edits are not expected to reload live; restart required. Change: docs and row only (say so in
  `farmers_delight_backport` writeup, the config comment in `FDBackportConfig`, row SQ-FDB-05 becomes "restart, then check"). SQ-FDB-07 follows.
- SQ-GLD-03: golden honey and milk drop the unknown 280 t bundle (Haste, Speed II, Strength, Luck, Regeneration). Decision: drop it.
  Find the `LivingEntityUseItemEvent.Finish` handler (grep the pack for `Finish` listeners and `effectsToApply` on the Golden member's
  `GoldenItems`), remove the bundle for both items, keep the items' own food effects. Verify SQ-GLD-03.

## Decisions

All answered (2026-11-03): grass rule and 0.5x speed (4.3), jar tops = jar + Pickling Jar (4.4), nylium (5.3: tag stays), gleaming salad in line with the golden rule (5.4),
vinegar Nausea 10 s (5.5), five-station overflow (2.3), golden and gilded foods 16 with milk buckets and cakes staying 8 (7.1), carrot canned source accepted (7.8), cans
unchanged (7.9). No open decisions; the remaining unknowns are the investigation steps inside each item.

## Order

1. Rebuild and install CD (0, 1, 2.1, 5.2, 5.5), Creative Brewing (3.1), Calendar (4.3), Worldgen (4.1, 4.2), Labels 1.0.1 plus 4.4 and 4.5: one jar at a
   time with the game stopped. 2. Scratch queue: add the rows below to `scratch_queue.md` in the same session as each fix.
3. Hand-checks first: 2.2 (slot split), 4.5 (flag 3 break), 2.5 (stack trace), 5.1 (class dump).

Scratch rows to add or refresh: SQ-VNG-01, SQ-VP-06, SQ-PSU-06, SQ-CDFW-07, SQ-RSP-06, -15 (two slots), SQ-RB-21b, SQ-POT-03, -04, -04v,
SQ-ENM-03, -04b, -05b, SQ-CWG-06, SQ-WGE-12, SQ-WCD-02 (jungle melon and mini melon counts), SQ-PST-08, -10, -11, -11b (plus summer and tropical),
SQ-LBS-03, -04, SQ-MRD-01, SQ-LGM-10, SQ-VTD6-01, SQ-FDB-04, SQ-PSU-09, SQ-GLD-03, SQ-LEM-01, -02. Row text fixes: SQ-VTD7-02, SQ-WGE-05b, SQ-FDB-05,
SQ-PSU-09, SQ-RSP-06 (two slots).

Player changelog bullets (when each lands, family names in brackets): [Fixes] duck fillet loop closed, Large Meals chicken curry craftable,
lemon yields, copper pot, potted gossypium and hemp, hanging pot, labels drop, sticky note on jars; [Grass] spread slowed by season; [Vinegar] Nausea when
drunk, golden honey and milk no longer grant the bundle; [Potions] levitation reagent; [Worldgen] melons in jungles, meteorite pyramid.

## 7. Additions (owner message, 2026-11-03 later)

### 7.1 Golden and gilded food all stack to 16 [decided]
- **DONE 2026-11-03 (fork A):** already true in `stack_sizes.json` (golden types 16, milk buckets and cakes 8 via the bucket and placed-cake rules); no regeneration. Found: the 6 golden stews are vanilla `BowlFoodItem`s (one bowl back for the whole stack), so they were added to FD `soupItemList` (`config/farmersdelight-common.toml` + `configs_edited`), new row SQ-GLD-15. Restart.
- Rule: every golden and gilded (enchanted) food stacks to 16: Golden Food's foods, stews and honeys (was 8 / 4 upstream), plus the vanilla golden apple (64), enchanted golden
  apple (1) and golden carrot (64). The golden and enchanted milk buckets and the golden and enchanted cakes are not raised: they stay 8 (bucket / placed-cake policy).
  Edit the rule in `tools/gen_stack_sizes.py` (the `golden` and `golden/enchanted` food types to 16, with the milk bucket and cake ids kept in the 8 group), regenerate
  `stack_sizes.json`, and check the file.
- Docs: `golden_food.md` 2 (Stack column: milk and cake 8), SQ-GLD-02 / -03, the stack-size-policy memory note. Changelog: [Stack sizes] golden and gilded foods stack to 16 [golden apple and
  golden carrot 64, enchanted golden apple 1, golden honey 8, enchanted honey 4, others 1].

### 7.2 Golden cake takes no candle until the cake candle feature (SQ-GLD-05, TODO `baking/baked-goods #38`)
- **BUILT 2026-11-03 (fork B):** `GoldenCakeBlock.use` returns PASS for any candle. CD jar 19:43, not installed.
- Change: `GoldenCakeBlock.use` (line 62-67) no longer converts on a candle: return `InteractionResult.PASS` for `ItemTags.CANDLES` on both golden and enchanted
  golden cake (remove the `CandleCakeBlock.byCandle` branch and its imports), so the candle is not consumed and the cake is not replaced.
  Update `golden_food.md` (quirk paragraph near line 98 and Q1), the SQ-GLD-05 row ("a candle does nothing"), and TODO #38 (golden cakes join the
  candle integration when it lands).
- Verify: SQ-GLD-05 (right-click with a white candle: cake unchanged, candle kept, bites still work).

### 7.3 Botania and CC:Tweaked out of village loot (SQ-GLD-09)
- **BUILT 2026-11-03:** golden_food.md 3.5 says the village tables are no longer overridden, so Forge injects both natively; the two `restoreInjection` calls are gone and `10_removals.js` removes `@botania` / `@computercraft` from `minecraft:chests/village/*` after the roll (LootJS, `CE/loot/removals/village_botania_cc`). Worldgen jar 19:45.
- State: `creativeworldgen/.../loot/07_rules.js` 86-110 restores three mod injections into the 16 village chest tables after reverting Golden Food's
  copies: Chalk (4 tables), Botania `botania:inject/village_chest` (temple, toolsmith, weaponsmith, 0 to 1 rolls) and CC:Tweaked `computercraft:treasure_disk`
  (cartographer). r1103 found only Chalk in 40 rolls, so the other two did not show (they are pulled in at a low chance, and the two tables sampled
  were plains house and cartographer; Botania is not in either).
- Change: keep Chalk, delete the two `L.restoreInjection` calls for `botania` and `computercraft`, and make sure the originals are suppressed too, not only
  not restored: `useVanilla(..., { winner: /goldenfood/ })` returns vanilla's tables, and Forge's own injection of those two mods still runs on
  top of vanilla ones. If they still appear, remove them with a loot-table modifier removing the two nested tables (LootJS / the loot framework's `L.remove`
  by entry name, `loot_census.md` lists the hooks). Check `loot_index.json` / `loot_config.json` and rebuild Creative Worldgen (its jar is stale, see FINAL).
- Verify: SQ-GLD-09 with 200 rolls across temple, toolsmith, weaponsmith, cartographer; 0 `botania:` / `computercraft:` stacks; Chalk present; row text updated.

### 7.4 A squid drops exactly one squid item (SQ-RD-08)
- **DONE 2026-11-03 (fork A):** code check confirms (no count, Looting or killer condition); SQ-RD-08 and `rustic_delight.md` updated.
- State: already so since `dishes/ocean #9` (r1103: 10 of 10 player kills gave one `miners_delight:squid`, nothing else; LootJS strips every other drop).
- Remaining: confirm Looting and non-player kills also give exactly 1 (no `set_count` range, no `looting_enchant`); then fix SQ-RD-08 row text (squid, not Calamari),
  `rustic_delight.md`, and the r_B.qb expectation. No code change expected.

### 7.5 MCA villagers target all overworld farmland (SQ-FDB-07)
- **BUILT 2026-11-03 (fork B):** MCA's `HarvestingTask.isValidFarmland` already takes any FarmBlock; the land scan (`lambda$searchUnusedFarmLand$4`) matched `Blocks.FARMLAND` only. CD mixin `mca/McaHarvestAnyFarmlandMixin` makes it any FarmBlock in the Overworld (dimension read at scan start). CD jar 19:43, not installed.
- Cause (from the run and memory mca-villager-pathing): MCA replaces villagers (`overwriteOriginalVillagers` true in `config/mca.json`), and its
  `VillagerEntityMCA` runs its own brain. `VillagersTargetFarmlandMixin` is on vanilla `SecondaryPoiSensor` and only adds the toml list `farmlandBlocks`
  for vanilla `Villager`s, so it never reaches MCA farmers (their memories never held `secondary_job_site`). MCA farmers farm through
  `net.mca.entity.ai.brain.tasks.chore.HarvestingTask` (assigned chore) and its own farmland scan.
- Change: (1) `javap` / decompile `HarvestingTask` and the MCA farmer behaviour for the farmland test (likely `FarmBlock` / `Blocks.FARMLAND` or a block tag);
  (2) mixin that test to accept every `FarmBlock` subclass in the overworld (Rich Soil, mulch, fertile soil, modded farmland), not only the listed
  blocks; (3) keep the vanilla-villager mixin; (4) decide whether `farmlandBlocks = []` still disables it (recommended: the list stays for vanilla, MCA
  takes all `FarmBlock`s). Dimension limit: overworld only (check `level.dimension() == Level.OVERWORLD`).
- Verify: SQ-FDB-07 on a world with MCA villagers kept (no need for `overwriteOriginalVillagers` false any more): an MCA farmer plants and harvests on
  Rich Soil and mulch farmland; rewrite the row.

### 7.6 Area hoe harvest costs 1 durability (SQ-PSF-02)
- **BUILT 2026-11-03 (fork B):** cost 1 when an area hoe (more than 1x1) harvested anything; a 1x1 hoe keeps Quark's flag. CD jar 19:43, not installed.
- Cause: `QuarkHoeAreaHarvestMixin` line 64: `hurtAndBreak(1, ...)` only when `SimpleHarvestModule.harvestingCostsDurability`, which the pack turns off
  in Quark's config, so the area use costs 0.
- Change: charge exactly 1 per area use (one click, however many crops) whenever anything was harvested, independent of the Quark flag:
  drop the flag from the condition (or read a CD config key `crops.areaHarvestDurability = 1`). Climbing tomato columns count as one use as today. Update
  `crops.md` 2.3, the row (SQ-PSF-02 "Damage 1") and the p2_R2 expectation.
- Verify: SQ-PSF-02 and SQ-PSF-04 (a 1x1 hoe on one vine: unchanged), Damage 1 after a 3x3 sweep.

### 7.7 Creative Calendar config in its own file only
- **BUILT 2026-11-03:** 29 old values diffed: all equal; the 3 sections and 3 strays deleted from `config/creativeengineering-common.toml` (127 lines; pre-edit copy in the fork scratchpad; `configs_edited` copy already clean). `CalendarConfigMigration` replaces `warnOldKeys`: strips at construction, writes edited values where the new key still holds its default on first load. Docs: creative_calendar.md 4.3. No patcherMod reader left. Calendar jar 19:35.
- State: the move is done in code. `CalendarConfig` writes `config/creativecalendar-common.toml`; `CEConfig` no longer defines `[seasons]`, `[time]`,
  `[crop_seasons]` (comments at CEConfig lines 88, 514, 717, 738). What remains: `config/creativeengineering-common.toml` still holds those three sections
  (lines about 1831 to 1916 and 2161 to 2183), and Creative Calendar only warns ("still has keys that moved") on every boot; edited values are not carried
  over. The pack's `configs_edited/root_config/creativeengineering-common.toml` backup has them too.
- Change: (1) diff the old sections against `creativecalendar-common.toml`, copy any value that differs; (2) delete the three sections (and the two
  strays `seasonsMeltIntervalTicks`, `cropSeasons`, `cropOutOfSeason`) from the live file and the `configs_edited` copy, plus the `.bak` copies are left; (3) make
  `CreativeCalendar.warnOldKeys` migrate once instead of warning (copy each old value into the new spec if the new file still holds the default, then
  strip the lines), logging what it moved; (4) grep scripts and docs for the old path (`kubejs/.../cal_server.js`, `creative_calendar.md` 4.3, README, `mod_sides.md`)
  and `creativeengineering-common.toml [time]` references; (5) confirm no patcherMod code still reads those keys.
- Verify: boot logs no "moved keys" warning; changing a season length in `creativecalendar-common.toml` takes effect; SQ time/season rows.

### 7.8 Carrot has a canned source (SQ-FO-02) [accepted]
- **DONE 2026-11-03 (fork A):** SQ-FO-02 and `food.md` FO-04 text updated.
- Accepted. The generator makes a can for every raw veggie; `food_types.json` has `canned_carrot: [minecraft_carrot]`. Text only: FO-04 and `food.md` line 903 drop the
  "Carrot shows no `canned_source`" clause; FO-02 expects the carrot with `canned_source`.

### 7.9 Canned foods effect duration [decided: no change]
- **DONE 2026-11-03 (fork A):** SQ-CNS-03 and `cans.md` 2 text updated.
- Keep what `gen_can_types.py` does today (`dur` 1.0 for most sections, 0.75 for salad, soup and meal, 0.25 for golden; Canned Goods' own `code_fx` cans unscaled by the
  generator). No code. Text only: SQ-CNS-03 expects "ticks = source x the section `dur`" (noodle soup 4800 -> 3600, beef stew comfort 4800 -> 3600), and `cans.md` 2 keeps "scaled durations".

### 7.10 Stale rows
- **DONE 2026-11-03**: the 21 FINAL.md row texts applied by `| SQ-ID |` match (SQ-VP-06 by its 1.2 fix, SQ-BBQ-07 rewritten for the retired click seasoning); the section 7 rows by their items. The "Batch regexes for reuse" notes in FINAL.md are batch-file fixes, not row texts, and are left for the next run's batch prep.
- `scratch_queue.md`: apply the 23 stale-text corrections listed in `perf/runs/r1103/FINAL.md` ("Stale row texts") and the ones decided here (SQ-RD-08, SQ-FO-02,
  SQ-CNS-03, SQ-GLD-02 / -03 / -05 / -09, SQ-PSF-02, SQ-FDB-05, SQ-FDB-07, SQ-VTD7-02, SQ-WGE-05b, SQ-PSU-09, SQ-RSP-06). Edit each row in place with a script that
  matches the `| SQ-ID |` prefix, never by line number (the file is 695 KB).
- `todo/TODO.md`: add or refresh a row per fix in this plan (the unbuilt ones as Planned), move rows whose fix is in the working tree (section 0) to Testing, and
  update `creativedelight/members #50`, the Golden row, and the Creative Calendar config row; README index status for `creative_calendar.md`, `golden_food.md`,
  `flower_pots.md`, `ends_delight.md`.

## 8. Added scratch rows and open questions

Rows to add or refresh for section 7: SQ-GLD-05 (candle refused), SQ-GLD-09 (200 rolls, no Botania / CC:Tweaked), SQ-RD-08 (Looting, non-player), SQ-FDB-07 (MCA kept),
SQ-PSF-02 (Damage 1), a calendar config row (single file, migrated values), SQ-FO-02, SQ-CNS-03 (text only).
Open decisions: see the Decisions section.
Changelog bullets: [Grass] spreads at half speed in summer and tropical biomes, normal otherwise [slow in winter]; [Golden food] cakes take no candle, [Villagers] MCA farmers use any farmland, [Tools] area hoe harvest costs 1 durability, [Loot] village chests lose
Botania and CC:Tweaked items; the calendar config move gets none (no player-visible change beyond the file).
