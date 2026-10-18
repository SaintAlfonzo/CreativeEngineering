# TODO: what is left, pointer by pointer

Started 2026-10-18. One place to see what still needs doing across the pack. It points at the writeup or file
that holds the detail and does not repeat it. Detail stays in the linked writeups
([feature_writeup/README.md](../README.md) is the feature index). The older
[todo.txt](todo.txt) (2026-09-24) and its two source audits, [kubejs_todo.txt](kubejs_todo.txt) and
[mod_todo.txt](mod_todo.txt), are folded in below; their items were checked against the 2026-10-18 tree.

Sections: 1 not started · 2 partly done · 3 open bugs · 4 decisions not made or not carried out · 5 parity with
newer upstream content · 6 KubeJS projects and leftovers · 7 TODOs written in files · 8 docs out of date ·
9 test backlog.

---

## 1. Not started (or barely started)

| Feature | Where it's written up | State |
|---|---|---|
| Vinery backport (1.5.5 -> 1.19.2, "Backported Vinery") | [alcohol.md](../alcohol.md) Part B | analysis and plan only; `development/backportedVinery` doesn't exist. Blocked on D13 (license), see 4 |
| JEI coverage parts 2 and 3 (recipe types with no category; world interactions) | [jei_coverage.md](../jei_coverage.md) 0 | not started. Part 1 is catalogued, but none of its stand-in or info fixes are built (5) |
| Food tuning system (values per type, SoL complexity, canned parity) | [food.md](../food.md) 3.5, 4.3 | designed, not built; waits on D1 to D6 |
| Vanilla egg -> `#forge:eggs` migration | [food.md](../food.md) 3.8 | list written, not built (rescan first, the dump can't read IE recipes) |
| Exposure Catalog port | [exposure.md](../exposure.md) 4 | on hold (user, 2026-10-01); the source set and mods.toml entry are empty |
| Cooking plans (seasoning, sourdough, vinegar, tools, kitchen, smoking, misc) | [cooking.md](../cooking.md) | outline only, nothing built. Two bugs in it are listed in 3 |
| Remainder buffer (bottles/bowls/buckets kept in a slot on the pot, kettle, keg port and oven instead of thrown) | [remainder_buffer.md](../remainder_buffer.md) | feasibility done 2026-10-18 (feasible, ~7 mixins); D1 to D5 open; no code |
| Canning automation | `TODO` in [canning.js](../kubejs/server_scripts/cooking/canning.js) line 68 | not started |
| Mob spawn plans beyond the first block (Alex's Mobs / duck parity, pillager overhaul, lone villagers, allay, skylands mooblooms) | [mob_spawns.md](../mob_spawns.md) 9 | first block done; "Investigate" and "Later" not started |
| Structures: Nether, End and underground grouping pass; cave-structure tuning; move-to-Nether set | [structure_tuning.md](../structure_tuning.md) 7 Todo | not built. Three structures never generate until it's done (3) |
| Structures: vehicle metas (caravans), more raft and wreckage variants | [structure_tuning.md](../structure_tuning.md) 7 Todo, 7.4.9 | marked only; new templates needed |
| Structures: tuning pass (shipwreck 40%, jungle temple spacing, strip unexplained weights, sky village rate, fresh watchtower `--fit-rates`) | [structure_tuning.md](../structure_tuning.md) 7 Todo | waits on the end of the scratch test run |
| Swap Overworld netherrack / crimson / warped ground (jasper, crimsite) | [structure_tuning.md](../structure_tuning.md) 7 Todo "Biomes and terrain" | not built |
| River tree / driftwood cleanup (oak logs, drop river log/wood) | [structure_tuning.md](../structure_tuning.md) 7 Todo "Structure contents" | not built |
| Absorbing small mods into the pack (tiers 1 to 4) | [feature_porting.md](../feature_porting.md) | candidate list only; no mod picked |
| In-game guidebook (Patchouli installed, no book) | [todo.txt](todo.txt) "Features not yet started" | not started (instance-only folder, not in this repo) |
| Writeups for the rest of the pack (item physics, biome tuning, fixes and compat, cans, composting, camera bags, tnt shrapnel, recipe progression) | [feature_writeup/README.md](../README.md) "Backlog" rows 7, 9, 10, 13, 14, 15, 17, 20 | not written; 13 of 21 rows done |
| Immersive Paintings datapack (475 paintings) | none (header of `tools/gen_immersive_paintings.py`) | shipped in 1.0.6 with no writeup; painting screen untested |

## 2. Partly done: built vs still to do

| Feature | Writeup | Done | Still to do |
|---|---|---|---|
| Structure tuning 7.1 to 7.4 (ships INCOMPLETE in 1.0.6) | [structure_tuning.md](../structure_tuning.md), [plan_1.0.6.md](../plan_1.0.6.md) 2.1, [testing/structures/](../testing/structures/00_plan.md) | built; scratch M1, M2, Phases 3 to 6 run | Phase 7 started 2026-10-14 17:43 with no results ([07_phase_7.scratch.md](../testing/structures/07_phase_7.scratch.md)); Phases 8 to 10 and the client list 8.0.11 not run; open bugs in 3; fixes go to 1.0.7 |
| Locate (folding, non-blocking loads, census, DH clip) | [locate.md](../locate.md) | built and in 1.0.6; LO-02, 06, 08, 12, 15 passed | LO-05 failed (loads still block), LO-03/LO-14 failed (census bug), see 3; LO-01, LO-04 partly |
| Alcohol Part A: Brewin' and Chewin' 4.5.0 port | [alcohol.md](../alcohol.md) Part A | analysis, plan and decisions 4.1/4.5. **Port begun 2026-10-17** in `development/backportedBrewin` (in `settings.gradle`): ~45 upstream classes copied (blocks, keg block entity, recipes, items, utilities, Forge containers) | registries, config (`BnCConfiguration`), fluids, effects/attachments, network, menu, client, JEI, assets/data, loot, remaps, old-world conversion: 14 imported classes don't exist yet, so it doesn't compile. Heat/cold tag audit 2.7 not applied. Pack-side changes 3.4 not started. The writeup still says "not started" |
| Food | [food.md](../food.md) | `/dumpfood`, 31 types + tags, catalog, multi-serving cans, raw and generated cans, fried eggs, egg stacks | tuning system and egg migration (1); cooked/prepared can sections pie, feast, meals, drinks, special |
| Oven | [oven.md](../oven.md) | block, GUI, baking recipe type, JEI (patcherMod), bakeware, 7 baking recipes, muffin dough fix (2026-10-18; compiles, 8 GameTests pass) | patcherMod jar rebuild + OV-01 to OV-14; recipes don't use bakeware or plating yet, pot recipes not moved |
| Remainder buffer | [remainder_buffer.md](../remainder_buffer.md) | pot + kettle (mixins), oven (native), `[cooking]` config, GameTest harness (2026-10-18; compiles, 10 GameTests pass) | patcherMod jar into `mods/` + RB-01 to RB-11; keg in the Brewin port once it compiles (3.6); optional WTHIT line |
| Can parity | [can_parity.md](../can_parity.md) | every `x` row built, removals done | `?` Want cells; hunger/saturation columns postponed; no can value-audited; meal/pie/feast/drink/hazard values are placeholders (also flagged in [canning.js](../kubejs/server_scripts/cooking/canning.js) lines 504, 542, 557); hand-written raw cans don't carry effects |
| Names | [names.md](../names.md) | Display Delight + Rustic Delight names; catalog parts 1 and 2 | catalog worked down: 191 partial blocks, 65 items, 53 entities, 20 professions, effects/potions/fluids; no fixes yet |
| Effect descriptions (JEED) | [todo.txt](todo.txt) "Literal TODO comments" | generator `tools/gen_effect_descriptions.py` | 47 `TODO: write a description` rows left in [lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json) |
| Tooltips | [tooltips.md](../tooltips.md) 8 | ~455 lines; pack framework lines | "Tooltips To Add" list (straw, knives, food type, can AppleSkin bars, chalk, Pretty Pipes; Botania, Vinery, Cloud Storage, Brewin); AppleSkin bars missing in Let's Do recipe book, FTB Quests, Wares, AE2, Patchouli |
| Vinery tooltips | [vinery.md](../vinery.md) 0 | flavour lines removed | block lines are commented-out `TODO` placeholders ([tooltips.js](../kubejs/client_scripts/tooltips.js) lines 623-643) |
| More Mooblooms 1.1 | [mooblooms.md](../mooblooms.md) 5 | 1.0 shipped in 1.0.6 | JEI breeding/shearing outputs, better info text |
| Loot: LootJS-only layer | [loot.md](../loot.md), [loot/README.md](../kubejs/server_scripts/loot/README.md) | pre-roll framework and catalog live | `10`-`40` scripts are still commented examples; `global.LOOT` groups/tiers in [00_lib.js](../kubejs/server_scripts/loot/00_lib.js) are empty; finding 8 (legacy dump, 40 MB) not deleted |
| Crops | [crops.md](../crops.md) 4 | G1-G3, G5 fixed; tobacco parity; wild placement | G4, G6-G15 and V-findings open (each has a suggested fix to approve or reject); 13 land biomes with no wild crop (V19) |
| Terrain adaptation leftovers | [structure_tuning.md](../structure_tuning.md) 7 Todo, [terrain_adaptation_scan.md](../terrain_adaptation_scan.md) | soft beard, template trim | batch 4 (spire, fish ponds, Philip's ruins, villages); earth-fill limits for modded structures; Beardifier estimate |
| Cultivated meat | [todo.txt](todo.txt) "Built, but with acknowledged gaps" | all 7 cultures wired; syringe has its own texture | culture food items have no nutrition ([cultures.js](../kubejs/startup_scripts/items/cultures.js) line 6) |
| Fridge sort item loss | [testing/fridge_sort/plan.md](../testing/fridge_sort/plan.md) | `QuarkSafeSort` fix built in patcherMod 1.0.7 (2026-10-14) | not in `mods/`, not shipped; FS-01 to FS-05 not run. [cooking.md](../cooking.md) still lists it as a bug |
| Exposure `-04` (viewfinder ate clicks inside screens and after respawn/rejoin) | [exposure.md](../exposure.md) | fixed in source, `mod_version` -04 (2026-10-17) | not built/shipped; no History line or test in the writeup |

## 3. Open bugs (not fixed)

**Structures and locate** (from [plan_1.0.6.md](../plan_1.0.6.md) 2.1 and the phase writeups):
- Locate loads block the server thread: `LocateLoads.java` lines 80-82 (LO-05). [05_phase_5.md](../testing/structures/05_phase_5.md) 3.1. Candidate for 1.0.7, your call.
- `/celocate census` counts most slots as "known" (LO-03, LO-14). [06_phase_6.md](../testing/structures/06_phase_6.md) 3.1. Fix together with the one above.
- `/celocate stop` labels queued rows `CancellationException`, not `stopped` (cosmetic). [05_phase_5.md](../testing/structures/05_phase_5.md) 3.4.
- Cloud Storage and Domestication Innovation village pieces dropped on chunk reload (third party; possible PoolTuner fix). [05_phase_5.md](../testing/structures/05_phase_5.md) 3.2.
- Desert and savanna village layouts differ between JVM runs (cause unknown). [05_phase_5.md](../testing/structures/05_phase_5.md) 3.3.
- Ground match gives up silently over deep structures. [03_phase_3.md](../testing/structures/03_phase_3.md).
- Beach, river and swamp driftwood water versions can never place (since 1.0.4). [03_phase_3.md](../testing/structures/03_phase_3.md).
- Skeleton dungeon, bone dungeon and lost soul city never generate (`land/any` has no cave biomes). [structure_tuning.md](../structure_tuning.md) 7.3.3.
- Earth fill counts earth bases only, not stone bases (drum tower, piglin camp). [plan_1.0.6.md](../plan_1.0.6.md) 2.1.

**Other features:**
- Brewin' 2.0 keg gives back an extra glass bottle per `pickled_pickles` brew (fluid-item leftover slot grows by the result count). Goes away with the 4.5.0 port. [remainder_buffer.md](../remainder_buffer.md) 1.3.
- Vinery JEI fermentation arrow shows 1800 ticks; the real time is about 50. `VineryIds.FERMENTATION_TICKS` is still 1800. [vinery_jei.md](../vinery_jei.md) 4.
- Canned Goods data: saturation values far too high, crossed ids, wrong effects. [food.md](../food.md) 4.2 findings 1-2 (the tuning system is the planned fix).
- item_dict: `i.catfish_slice` points at a nonexistent id; `i.bacon` is the cooked item. [food.md](../food.md) 4.2 finding 6.
- Spice of Life default benefit `attribute,generic.speed,2` isn't a real attribute. [food.md](../food.md) 4.2 finding 4.
- Muffins may not be cookable. [cooking.md](../cooking.md) "errors/bugs". Pot recipes moved to `#forge:dough` (2026-10-18) but the cause isn't confirmed: Create already puts its dough in `#forge:dough/wheat`. OV-13 in [oven.md](../oven.md) 3.8.
- Bumpy filter (Exposure Expanded) has no source in 1.19.2 (trial chambers only). [exposure.md](../exposure.md) 4.
- Felsic shield assembly asks for `CRAFTING.FELSIC_SHIELD`, which `CEConfig.Crafting` doesn't define: hardcoded values and an `[NCH.cfg]` warning every `/reload`. [gear_tuning.md](../gear_tuning.md) 4 F1.
- Felsic `potionMb` feeds both fills, not just the last as its toml comment says. [gear_tuning.md](../gear_tuning.md) 4 F2.
- Iron armor set bonus registered twice (harmless). [armor_effects.md](../armor_effects.md) 4 F1.
- `#creativeengineering:elytra_chestplates` is a hand-written `e.add` in `equipmenttags.js` line 10, against the tag-helper rule. [gear_tuning.md](../gear_tuning.md) 4 F3.
- Dragon scale armor-piercing bullets use blueprint category `projectiles`, which nothing makes: unreachable. [archery.md](../archery.md) 4 F1.
- Tipped arrows on the Bottler (20 mB) and Spout (25 mB) cost less than the crafting grid (31.25 mB an arrow); the file comment says they're equal. [potion_buckets.md](../potion_buckets.md) 4 F1.
- Potion bucket tooltips show the potion's durations, not × the bucket's 1.1 / 1.2 effect multiplier. [potion_buckets.md](../potion_buckets.md) 4 F2.
- Probable, unverified: NEA's three removed enchantments can still roll from `enchant_randomly` loot (the `isDiscoverable` mixin method likely isn't remapped). [archery.md](../archery.md) 4 F6.
- Engineering: component and coil disassembly comments promise a screwdriver / wirecutter the recipes do not take; iron and diamond components use a different centre on the blueprint. [engineering.md](../engineering.md) 4 F4, F6.
- Probable, unverified: the air brake's `useDurationTicks` is read before Forge loads the config, so edits never apply (GR-28); the dragon set's 36 armor is over vanilla's 30 cap (GR-02). [gear_tuning.md](../gear_tuning.md) 4 F7, F13.
- Cool Rain may ignore the season types (WE-25). [weather.md](../weather.md), [plan_1.0.5.md](../plan_1.0.5.md) 2.
- 5 Terralith cave biomes have no wild crops (single-player warning). [plan_1.0.5.md](../plan_1.0.5.md) 2.

**Carried from [todo.txt](todo.txt), code unchanged since:**
- Shield `NoClassDefFoundError` on `damage.DamageFlag` (crash 2026-09-24): `DamageFlag` / `DamageTags` are still not in the `forceLoad` list in `CreativeEngineering.java` (lines 242-252). Recurrence not checked.
- `ItemPhysicSplit$1` `NoClassDefFoundError`: `burnDecision` still switches on the enum (`ItemPhysicSplit.java` line 128), so the synthetic class is still generated. Recurrence not checked.
- A 0-byte crash report from 2026-09-11 was never looked into.
- `createdeco:polished_iron_bars` stonecutting removed with no replacement ([metal_building.js](../kubejs/server_scripts/metal/metal_building.js) line 11): confirm that's intended.
- `misc_mods.js` dead block has a stray double comma and "all fucked up, fix later" (line 208); it can't be re-enabled as is.

## 4. Decisions not made, or made but not carried out

**Waiting on you:**
- Alcohol Part B: D3, D4, D5, D7 to D10, D12, and **D13 (license) first**. [alcohol.md](../alcohol.md) B5. Note: [cooking.md](../cooking.md) "vinery backport license" says the author refused redistribution, and sketches a plan (MIT code, upstream assets loaded from the old jar). That answer and plan aren't in alcohol.md B3.6 / B5 D13 yet.
- Remainder buffer D1 to D5 (stall or throw when full, recycle into the container slot, slot count, automation, keg timing). [remainder_buffer.md](../remainder_buffer.md) 5.
- Food tuning D1 to D6. [food.md](../food.md) 4.3.
- Can parity `Want` = `?` cells. [can_parity.md](../can_parity.md).
- Crops G4, G6 to G15, V findings: approve or reject each fix. [crops.md](../crops.md) 4.
- Structures: undead castle vs earth fill (narrow its fits or relax the mound limit); ST-135 fortified metas against the 120 s timeout; geode drop tuning (ST-133). [plan_1.0.6.md](../plan_1.0.6.md) 2.1.
- Locate-load and census fixes for 1.0.7 (3); fix or leave the third-party village pieces (3).
- Wood mismatch: 35 structures marked "Restrict". [wood_mismatch.md](../wood_mismatch.md).
- Bee nest starter rules: your review. [bee_nests.md](../bee_nests.md) 9.
- More Mooblooms D47 per-biome shares: first pass, open to retuning. [mooblooms.md](../mooblooms.md) 4.13.
- Exposure bumpy filter: add a recipe or a loot entry. [exposure.md](../exposure.md) 4.
- Metal standardization: plate / rod / dust-recycle gaps and an open question at the top of [metal_base_materials.js](../kubejs/server_scripts/metal/metal_base_materials.js).
- Component tier names for aluminum (`Light?`) and uranium (`Dense?`). [components.js](../../kubejs/server_scripts/engineering/components.js) lines 18 and 22. The item names already say "Light" and "Heavy" ([engineering.md](../engineering.md) F5).
- Whether `kubejs/archive/` and the commented-out old-helper blocks can be deleted (6).
- Armor/tool effects: dragon bow and crossbow effects look reversed (F4); Magic Resistance on vanilla `isMagic()` or the pack's `magic` damage tag (F6); horse armor Regeneration I or II (F9). [armor_effects.md](../armor_effects.md) 4.
- Portal: Flint and Terrasteel places Dark Fire anywhere (F1); Dark Fire lights portals in any dimension (F2). [nether_portal.md](../nether_portal.md) 4.
- Tooling: delete the unused `global.config` framework (F3); keep the metal/wood/sequenced-assembly test recipes that ship in `_lib/` (F4). [pack_tooling.md](../pack_tooling.md) 4.
- Potions: one batch size across machines or not (F1); keep the Refinery's regular → lingering shortcut (F2). [potions.md](../potions.md) 4.
- Potion buckets: machine tipping price (F1); keep the glass-bottle remainder on every vanilla potion (F3). [potion_buckets.md](../potion_buckets.md) 4.
- Archery: fix the bullet blueprint category (F1); split arrow 6 or 4 arrows (F2); the archery blueprint needs a trader-only Ethereal Arrow (F3). [archery.md](../archery.md) 4.
- Engineering: show the sawmill blade in JEI (F2); one centre per component (F4); keep the bucket-eating shulker shell fan recipe (F7). [engineering.md](../engineering.md) 4.
- Gear: dragon mining level 5 vs 4 (F4); carry Armor Trims' `Trim` and shield banners through assemblies (F5); tune the dragon bow, crossbow, trident and horse armor (F10); dragon set over the 30 armor cap (F13); felsic sword durability 2502 vs 2501 (F9). [gear_tuning.md](../gear_tuning.md) 4.

**Decided, not carried out:**
- Brewin' and Chewin' heat / cold tag additions (2.7) are proposed but not applied. [alcohol.md](../alcohol.md) Part A.
- Incubation Additions license: repaint the 4 hay-nest PNGs and rewrite or reference the models before the jar is published anywhere. [nests.md](../nests.md) 4.
- Shield knockback thresholds and a per-tier disable cooldown. [shields.md](../shields.md) 4, `TODO` in `ShieldTier.java` line 46.
- Sawmill recipes off the manual pipeline into an NCH helper. `TODO` in [sawmill.js](../kubejs/generation_scripts/sawmill.js) line 1.
- Magic Resistance and Luck potions use placeholder reagents. [potions.js](../../kubejs/startup_scripts/cooking/potions.js) lines 70-71 and 86; [potions.md](../potions.md) F3.
- Advanced Peripherals structures: off by config; remove the warning or the structures outright. [structure_tuning.md](../structure_tuning.md) 7 Todo.
- Dark Fire portal: `FlintAndTerraSteelItem` is still "TEMPORARY testing behavior" with no frame check and refers to a `TerraFireBlock` that no longer exists. `PortalFailureHandler` keeps a diagnostic log "until confirmed working". [todo.txt](todo.txt), [nether_portal.md](../nether_portal.md) F1, F5.

## 5. Parity with newer upstream content

| Area | Where | Gap |
|---|---|---|
| Brewin' and Chewin' 2.0 -> 4.5.0 | [alcohol.md](../alcohol.md) Part A | port in progress (2) |
| Vinery 1.3.10 -> 1.5.5 | [alcohol.md](../alcohol.md) Part B | not started (1) |
| Exposure 1.9.19 addons | [exposure.md](../exposure.md) | Catalog 1.1.2 not ported |
| JEI 19 "craftable first" | [jei_craftable_first_port.md](../jei_craftable_first_port.md) | backport built, JEI-01 to JEI-06 never run |
| Crop parity layer | [crops.md](../crops.md) 2.6, 4 | only tobacco brought in line; G-list crops (hemp, grapes, weeds, flax, tea, corn, coffee, pineapple) still differ |
| Cans for every food | [can_parity.md](../can_parity.md) | `?` rows; 6 pie slices skipped (no source) |
| Placeable food | [placeable_food.md](../placeable_food.md) | catalog built from fallback data: `/dumpplaceable` never run, so newer mods may be missing |
| Stack sizes reference | [stack_sizes.md](../stack_sizes.md) | from before the stack size policy; rerun `/dumpstacks` |
| Item dictionary | [startup_scripts/todo/](../kubejs/startup_scripts/todo/) | 911 candidate entries (`create_ie_dict_additions.txt`) and 689 unmapped ids (`not_in_dict.txt`) not merged; redstone module still on the old dict variant (item_dict "Merge additions from old dict") |
| Dict validation | `block_dict.js` ~line 45, `fluid_dict.js` ~line 162, `neoCraftHelper.js` ~line 383 | tag entries aren't validated; `item_dict.js` does check tags but probably before they load ([pack_tooling.md](../pack_tooling.md) F1, F2) |
| Sequenced-assembly sprites | [texture_tool.js](../kubejs/client_scripts/texture_tool.js) line 28 | 29 outputs still use the generic incomplete sprite |
| Mob spawns | [mob_spawns.md](../mob_spawns.md) 9 | Alex's Mobs and duck/goose biome parity not investigated |
| Loot census | [loot_census.md](../kubejs/server_scripts/loot/loot_census.md) | still lists Exposure 1.6.0's table ([exposure.md](../exposure.md) 4) |

## 6. KubeJS projects and leftovers

- **Loot overhaul** ([loot/](../kubejs/server_scripts/loot/README.md)): the LootJS-only layer is scaffolding (2).
- **Dictionary worklists** ([startup_scripts/todo/](../kubejs/startup_scripts/todo/)): four `.txt` lists not merged (5). `recipes_to_remove.txt` is fed by the still-live `_lib/recipe_scan.js`; `remove_list.txt` has one item left, `vintageimprovements:rose_gold_rod` (check before removing).
- **Helper migration `global.CE` -> `global.NCH`**: old blocks commented out in place: [misc_mods.js](../kubejs/server_scripts/other/misc_mods.js) from line 60 (and a debug snippet at 280), [resource_blocks.js](../kubejs/server_scripts/engineering/resource_blocks.js) from line 257, [cooking.js](../kubejs/server_scripts/cooking/cooking.js) from line 328. `kubejs/archive/` (9 files and `worldgen/`) has no live references. Keep or delete is your call.
- **`kubejs/recipes/`** (4 JSON files: bonemeal broth, sawmill recycle) is not a KubeJS load path, so they don't load. Port them through NCH or delete them.
- **`kubejs/quest_working/produce.txt`**: already merged into `food_purchasing.snbt` (per todo.txt); safe to delete.
- **Sawmill generator** ([sawmill.js](../kubejs/generation_scripts/sawmill.js)): manual pipeline into 211 jar JSON copies (4).
- **Nulch** recipe is in [sawdust.js](../kubejs/server_scripts/wood/sawdust.js) line 83; the note says it belongs in the compost file.
- **Transition items**: add `R.incomplete(...)` definitions by file to the item dict ([transition_items.js](../kubejs/startup_scripts/changes/transition_items.js) line 24).
- **Bookkeeping**: `minecraft:dispenser`, `smoker` and `cake` are removed but not tracked in `_lib/removed_items.js`.
- **patcherMod leftovers** ([mod_todo.txt](mod_todo.txt)): `CreativeEngineeringItems.javaold`, `build.gradleold`, the MDK `README.txt` and `mods.toml` placeholder text; dead blocks in `CreativeEngineering.java` and `IECompat.java`; the `// TEMP diagnostic` log in `PotionFluidHandlerMixin` (line 62); the per-call log in `IETagUtilsMixin` (line 54); unused `@Overwrite` imports in the Cycle / Stockpile / Recovery enchantment mixins. (`Config.java` and the dead `.en_us.json` are gone.)

## 7. TODOs written in files

| File | Line | Note |
|---|---|---|
| [sequence_assembly_processes.js](../kubejs/server_scripts/_lib/sequence_assembly_processes.js) | 51 | helper should return recipes for every valid process, with a comment per helper |
| [transition_items.js](../kubejs/startup_scripts/changes/transition_items.js) | 24 | `R.incomplete` definitions into the item dict |
| [canning.js](../kubejs/server_scripts/cooking/canning.js) | 56, 68 | "Other Changes TODO"; canning automation; make sure food effects carry over and bone broth returns its bowl |
| [potions.js](../kubejs/startup_scripts/cooking/potions.js) | 86 | better or magic brewing ingredient for Luck |
| [sawmill.js](../kubejs/generation_scripts/sawmill.js) | 1 | move off the manual pipeline |
| [tooltips.js](../kubejs/client_scripts/tooltips.js) | 623-643 | Vinery block lines, all `TODO` |
| [lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json) | (47 rows) | `TODO: write a description` |
| `development/patcherMod/.../shield/ShieldTier.java` | 46 | knockback thresholds, per-shield cooldown |
| [cultures.js](../kubejs/startup_scripts/items/cultures.js) | 6 | nutrition once the cooking recipes exist |

## 8. Docs out of date

- **1.0.6 is deployed** ([deploy/DEPLOYED.json](../deploy/DEPLOYED.json): server and client 1.0.6, 2026-10-14 13:01; commit
  `15c43a0`). But [feature_writeup/README.md](../README.md), [plan_1.0.6.md](../plan_1.0.6.md), [deploy.md](../deploy.md) and most status blocks still say "STAGED for 1.0.6, not deployed". Sweep them to DEPLOYED.
- [alcohol.md](../alcohol.md) Part A says the port is "not started"; it has begun (2). [deploy.md](../deploy.md) `IN_TREE_MODS` will need `backportedBrewin` once it builds.
- [exposure.md](../exposure.md) has no `-04` entry (2).
- [cooking.md](../cooking.md) lists the fridge sort bug, which has a fix built (2).
- Writeups not in the README index: [cooking.md](../cooking.md), [feature_porting.md](../feature_porting.md), [meat_blocks.md](../meat_blocks.md), [nether_swaps.md](../nether_swaps.md), [nether_blocks_scan.md](../nether_blocks_scan.md), [wood_mismatch.md](../wood_mismatch.md), [testing/fridge_sort/](../testing/fridge_sort/plan.md).
- [todo.txt](todo.txt) items found done on 2026-10-18: all 7 cultures wired; syringe texture; dead `.en_us.json`; `Config.java`; dragon-scale chestplate rocket takeoff (`EasyElytraTakeoffHelperMixin`; check whether Alex's Mobs items need adding to `#creativeengineering:elytra_chestplates`). The loot overhaul line is superseded by [loot.md](../loot.md).
- Found while writing [armor_effects.md](../armor_effects.md) and [gear_tuning.md](../gear_tuning.md) (2026-10-18): [magic_resistance.js](../../kubejs/startup_scripts/magic/magic_resistance.js) header says there is no potion (there is); [armor_effects.js](../../kubejs/startup_scripts/func/armor_effects.js) header says 6 effects (cap is 7); the `NCH.cfg` example uses toml keys instead of field names ([neoCraftHelper.js](../../kubejs/startup_scripts/func/neoCraftHelper.js) ~621). (shields.md 2.7 said the felsic shield's values were configurable: corrected the same day.)
- Found while writing [potions.md](../potions.md), [potion_buckets.md](../potion_buckets.md) and [archery.md](../archery.md) (2026-10-18): `DragonBreathBrewing`'s javadoc names a `changes/dragon_breath.js` that doesn't exist (it's `potions.js` part 3); [tipped_arrows.js](../../kubejs/server_scripts/equipment/tipped_arrows.js) header gives amounts the code doesn't use. Raw `e.custom` recipes in `potion_bottling.js` and `tipped_arrows.js`, and 44 raw tag `e.add` lines in `equipmenttags.js`, are against the NCH / tag-helper rule.
- Found while writing [pack_tooling.md](../pack_tooling.md), [nether_portal.md](../nether_portal.md) and [wthit_lines.md](../wthit_lines.md) (2026-10-18): `test_crop_growth.sh` names a `crop_growth.md` (it's crops.md 2.9); `neoCraftHelper.js` line 1 names an old path; `DarkFireBlock#onPlace` still describes the removed `isAir()` override; WTHIT plugin toggles probably show raw `config.waila.plugin_*` keys (no lang lines).

---

## 9. Test backlog

Open = unticked boxes in the file (status words in the file win where they differ). Nothing is VERIFIED yet.

| Writeup | Prefix | Open / passed | Notes |
|---|---|---|---|
| [structure_tuning.md](../structure_tuning.md) | `ST-` | 93 / 98 | Phase 7 onward; ST-65 trim half; client list 8.0.11 |
| [locate.md](../locate.md) | `LO-` | 9 / 7 | LO-03, 05, 14 failed; rerun after the fixes |
| [alcohol.md](../alcohol.md) | `AL-`, `VB-` | 81 / 0 | drafted; run once built |
| [crops.md](../crops.md) | `CR-` | 68 / 0 | none run |
| [mooblooms.md](../mooblooms.md) | `MB-` | 64 / 10 | client-side checks |
| [shields.md](../shields.md) | `SH-` | 42 / 1 | SH-43 first, then SH-14, 19, 33-42 |
| [gear_tuning.md](../gear_tuning.md) | `GR-` | 30 / 0 | new 2026-10-18; GR-02, GR-05, GR-28 settle findings |
| [armor_effects.md](../armor_effects.md) | `AE-` | 24 / 0 | new 2026-10-18; AE-10, AE-19, AE-20 settle findings |
| [potions.md](../potions.md) | `PO-` | 26 / 0 | new 2026-10-18; replaces the never-recorded September potion-fluid checklist |
| [potion_buckets.md](../potion_buckets.md) | `PB-` | 20 / 0 | new 2026-10-18; PB-04 (dome) and PB-11 to PB-17 (recipes, bottle remainder) close the todo.txt gaps |
| [archery.md](../archery.md) | `AR-` | 18 / 0 | new 2026-10-18; AR-13, AR-14, AR-15 settle findings |
| [pack_tooling.md](../pack_tooling.md) | `TL-` | 16 / 0 | new 2026-10-18; TL-01 settles F1 |
| [nether_portal.md](../nether_portal.md) | `NP-` | 12 / 0 | new 2026-10-18; NP-01 to NP-05 let the diagnostic log go |
| [mob_block_tweaks.md](../mob_block_tweaks.md) | `MT-` | 12 / 0 | new 2026-10-18 (BR-28 stays in blockrunner.md) |
| [wthit_lines.md](../wthit_lines.md) | `WL-` | 6 / 0 | new 2026-10-18; line tests stay in BR-, CR-, R4- |
| [engineering.md](../engineering.md) | `EN-` | 18 / 0 | new 2026-10-18; EN-04 settles F2 |
| [blockrunner.md](../blockrunner.md) | `BR-` | 36 / 14 | BR-52 needs a restart |
| [weather.md](../weather.md) | `WE-` | 36 / 0 | client side never run; WE-26 to WE-36 (1.0.6) |
| [exposure.md](../exposure.md) | `EX-` | 34 / 5 | EX-13 (album fix), EX-50 (old world) |
| [food.md](../food.md) | `FO-` | 27 / 1 | FO-02 to FO-28 |
| [loot.md](../loot.md) | `LT-` | 24 / 3 | LT-14 to LT-27 |
| [mob_spawns.md](../mob_spawns.md) | `MS-` | 23 / 0 | |
| [vinery.md](../vinery.md) | `VN-` | 20 / 0 | |
| [tooltips.md](../tooltips.md) | `TT-` | 18 / 0 | TT-16 to TT-18 (1.0.6) |
| [wandering_trader.md](../wandering_trader.md) | `WT-` | 16 / 0 | WT-16 natural spawn |
| [can_parity.md](../can_parity.md) | `CP-` | 14 / 0 | CP-01 to CP-14 |
| [remainder_buffer.md](../remainder_buffer.md) | `RB-` | 11 / 0 | needs the 1.0.7 jar; 10 offline GameTests pass (`tools/test_remainder_buffer.sh`) |
| [flowers.md](../flowers.md) | `FL-` | 14 / 0 | |
| [nests.md](../nests.md) | `NE-` | 14 / 0 | NE-13 client sprites |
| [plan_1.0.4.md](../plan_1.0.4.md) | `R4-` | 14 / 0 | release checks never ticked |
| [perf.md](../perf.md) | `PF-` | 13 / 0 | Windows scripts untested |
| [trashslot.md](../trashslot.md) | `TS-` | 13 / 0 | |
| [bee_nests.md](../bee_nests.md) | `BN-` | 10 / 0 | |
| [vinery_jei.md](../vinery_jei.md) | `VJ-` | 10 / 0 | VJ-04 changes with the fermentation fix |
| [placeable_food.md](../placeable_food.md) | `PL-` | 9 / 0 | run `/dumpplaceable` first |
| [names.md](../names.md) | `NM-` | 6 / 2 | NM-01 to NM-06 |
| [jei_coverage.md](../jei_coverage.md) | `JC-` | 5 / 0 | spot checks |
| [deploy.md](../deploy.md) | `DP-` | 4 / 3 | DP-03 to DP-05, DP-07 |
| [jei_craftable_first_port.md](../jei_craftable_first_port.md) | `JEI-` | 6 / 0 | plain list, no boxes |
| [testing/fridge_sort/plan.md](../testing/fridge_sort/plan.md) | `FS-` | 5 / 0 | needs the 1.0.7 jar |
| [todo.txt](todo.txt) "Built, but with acknowledged gaps" | none | | potion buckets now PB-04, PB-11 to PB-17; potion-fluid chain now PO-01 to PO-26; sawblade routing still never explicitly checked in game |
