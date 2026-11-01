# Sweep F: pack scripts

Written 2026-11-01 by the Creative Split audit (creative_split.md section 4, sweep F). Read-only: no script was changed.

Covers `kubejs/startup_scripts/`, `kubejs/server_scripts/`, `kubejs/client_scripts/` and the jar startup scripts in `development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/`. One row per file; a file whose rows belong to two or more targets is `mixed? yes` and lists its row ranges under the table (rule 3.7). Targets use the split README vocabulary; `?` marks a doubtful row with a note in the reason column.

Scope applied (brief): `_lib` dicts and func helpers are `Common`; `engineering`, `metal`, `wood` are `Engineering` except the computing and circuit rows (`Computers (Engineering)`); `equipment` is `Equipment`; `magic` is `Magic (Equipment)` minus what CB takes (creative_brewing.md 4.2 phase 1: potions, effects, alcohol, gear effects); `loot` and `worldgen` are `Worldgen`; `cooking` is `CD` or `CB`; `tags`, `other`, `economy` per row; client tooltips, names and JEI order `Common` unless a row shows one content jar's data.

Method: `wc -l`, `grep -n "global\."` for every `global.X =` (defines) and `global.X` (reads), top-level `function/const/var` names, the KubeJS event names each file registers, counts of `global.NCH` / `global.tags` calls, raw `e.shaped/shapeless/custom/add/remove` calls, and whether a `global.<X>.recipes(...)` wrapper is used. The `_lib` dicts (18k lines) were only counted and grepped, never read. Reasons come from each file's header comment and from creative_brewing.md 4.2, creative_split.md 2 and the split README decisions.

## 1. Findings in one place

- **No pack script uses a `global.<X>.recipes(...)` wrapper.** The 134 `global.CD.recipes(` and 2 `global.CB.recipes(` calls are all inside the CD and CB jars; the only pack use of a `.recipes(` member is `global.itemMergeRun.recipes(...)` in `other/item_merges.js`, which is the merge runner, not the id wrapper. Every recipe script moving to a jar needs its body wrapped (rule 3.9), and every recipe id changes with it.
- Every recipe script already goes through `global.NCH` (one file, `magic/enchants.js`, uses six raw `e.*` calls and no NCH; `_lib/sequence_assembly_processes.js` has 11 raw calls; `worldgen/*` use `e.add` on highPriorityData, not recipes). Every tag script goes through `global.tags`; the `t.add(e, ...)` calls are `var t = global.tags` aliases.
- The gear-effects, potion, alcohol and Vinery rows the brief listed for `Magic (Equipment)` are already claimed by CB (creative_brewing.md 4.2 phase 1); only `dragon_breathing`, `enchants`, `progression`, `sculk`, `storage`, `xp` remain for the Magic reservation (164 lines), plus the Botania tooltips and the XP fluid tag rows.
- The circuit rows the brief expected in `engineering/components.js` are not there (components are IE plates and wire coils); the only circuit row is the redstone mechanism block of `mechanisms.js` 31-48. `engineering/redstone.js` (256 lines, redstone components by sequenced assembly) is the real Computers candidate and is marked `Engineering?`.
- Three dead stubs stay in the pack on purpose (`cooking/cooking.js`, `cooking/cookingtags.js`, both 4 lines) so stale server copies are overwritten; `startup_scripts/todo/*.txt` are 1890 lines of stale generated notes, not scripts.
- `client_scripts/namechanges.js` 249-340 still names Delight dishes and lunchboxes although its own header says food names moved to CD on 2026-10-27: a carve CD missed.
- The wandering-trader rows (`func/trade_helper.js`, `economy/wandering_trades.js`, `tags/tradetags.js`, 252 lines) have no jar in creative_split.md 2.1: `trader/TradeListings` is a Common API in 2.4, but the stock and the Winemaker are content. Marked `Common?`; the owner decides Common or Worldgen.

## 2. Startup scripts: `_lib` dicts

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `startup_scripts/_lib/block_dict.js` | 207 | Common | dict, closed decision Q4 (jar kubejs_startup); block ids | blocks ; top: b | item | postInit; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/effect_dict.js` | 55 | Common | dict, closed decision Q4 (jar kubejs_startup); effect ids | effect ; top: ef | - | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/fluid_dict.js` | 239 | Common | dict, closed decision Q4 (jar kubejs_startup); fluid ids | fluid ; top: f | item | postInit; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/food_dict.js` | 1623 | Common | dict, closed decision Q4 (jar kubejs_startup); CD food subdicts | food, foodKey | foodGrammar, item, subdict | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/food_grammar.js` | 59 | Common | dict, closed decision Q4 (jar kubejs_startup); CD subdict grammar check | foodGrammar, subdict | food, item | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/item_dict.js` | 14953 | Common | dict, closed decision Q4 (jar kubejs_startup); item ids | item ; top: i | removedItems | postInit; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/item_merges.js` | 88 | Common | dict, closed decision Q4 (jar kubejs_startup); item-merge groups and the itemMergeRun wrapper | addItemMerges, itemMergeGroups, itemMergeRun, itemMerges ; top: i | NCH, item, removedItems | none; NCH 1, tags 0; no wrapper | no |
| `startup_scripts/_lib/multiblock_dict.js` | 36 | Common | dict, closed decision Q4 (jar kubejs_startup); IE multiblock ids | multiblock ; top: mb | - | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/removed_items.js` | 399 | Common | dict, closed decision Q4 (jar kubejs_startup); removed-item list, drives JEI hide, recipe scan, tag strip | hasScannedRemovedItemRecipes, removedItems ; top: i | item | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/_lib/removed_woods.js` | 358 | Common | dict, closed decision Q4 (jar kubejs_startup); removed wood sets merged into oak | removedWoodTracks, removedWoods ; top: i | addItemMerges, item, removedItems | none; NCH 0, tags 0; no wrapper | no |

## 3. Startup scripts: `changes`, `cooking`, `func`, `items`, `jei`, `magic`, `todo`

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `startup_scripts/changes/block_properties.js` | 25 | Engineering | resets native walk speed of D&D asphalt etc. so only BlockRunner tiers apply (surface/) | - | blocks, item | BlockEvents.modification; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/changes/pnc_tooltips.js` | 30 | Common | Interface: PneumaticCraft info tooltips without Shift (third-party data) | - | item | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/changes/stack_sizes.js` | 41 | Common | stack-size policy for every mod (generated list), pack-wide rule; tuneTools/tuneArmor appear in a comment only (l.6), not an edge | - | item | ItemEvents.modification; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/changes/transition_items.js` | 198 | Engineering | transition items for Create sequenced assembly / IE workbench chains | - | NCH, item | registry; NCH 1, tags 0; no wrapper | no |
| `startup_scripts/cooking/alcohol_drinks.js` | 91 | CB | alcohol drink food values (creative_brewing.md 4.2 phase 1) | alcohol | item | ItemEvents.modification; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/cooking/food_values.js` | 66 | CB | effect swaps and Respite cake slice effects (creative_brewing.md 4.2 phase 1) | foodEffectSwaps ; top: ef | effect, foodValueCopies, item | ItemEvents.modification; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/cooking/potions.js` | 308 | CB | custom potions and brewing mixes (creative_brewing.md 4.2 phase 1) | - ; top: AMPLIFY_REAGENT, AM_POTION_MIXES, CE_POTION_MIXES, CUSTOM_POTIONS, LENGTHEN_REAGENT, LUCK_POTIONS (+4) | item | postInit, registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/func/armor_effects.js` | 269 | CB | gear effects framework, owner 2026-10-31 to CB | armorEffects ; top: dragonEffects, ef, i | effect, item | none; NCH 0, tags 0, raw 1; no wrapper | no |
| `startup_scripts/func/item_physics.js` | 156 | Compat | ItemPhysic fire/lava/float split helper (itemphysic/ is Compat) | itemPhysics | - | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/func/jei_order.js` | 141 | Common | Interface: JEI order helper (PackIngredientOrder is Common) | jeiOrder | item, tags | tags; NCH 0, tags 3; no wrapper | no |
| `startup_scripts/func/potion_area.js` | 187 | CB | potion bucket dome (creative_brewing.md 4.2 phase 1) | potionArea | - | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/func/sample_syringe.js` | 81 | CD | reserved for Creative Cultivation; cultivated meat lives in CD (creative_delight.md 29.5) | sampleSyringe | item | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/func/tool_effects.js` | 155 | CB | gear effects framework, owner 2026-10-31 to CB | toolEffects ; top: discombobulate, echoHit, i | armorEffects, item | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/func/trade_helper.js` | 117 | Common? | wandering trader pool helper; TradeListings API is Common (2.4), but the trader feature itself (trader/, [wandering_trader]) has no jar in 2.1: owner to pick Common or Worldgen | trades | - | none; NCH 0, tags 0; no wrapper | yes |
| `startup_scripts/func/tune_armor.js` | 120 | Equipment | armor caps and enchantability tuning | tuneArmor ; top: i | item | ItemEvents.modification; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/func/tune_tools.js` | 148 | Equipment | tool tuning (tuneTools) | tuneTools ; top: R, i | NCH, item | ItemEvents.modification; NCH 1, tags 0; no wrapper | no |
| `startup_scripts/items/air_brake.js` | 44 | Equipment | Air Brake item | - ; top: $UseAnim, CFG, airBrakeUseDurationTicks | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/coil_blocks.js` | 29 | Engineering | coil blocks | - ; top: COIL_BLOCK_METALS | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/components.js` | 25 | Engineering | IE-style components and wire coils; items only, no circuit rows | - | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/decor_blocks.js` | 31 | Engineering? | layered obsidian, weathered cardboard box: decor with no system; Engineering by default (block/), owner may prefer Compat | - | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/dust_piles.js` | 34 | Engineering | dust pile falling blocks | - ; top: DUST_PILE_METALS | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/echo_tools.js` | 51 | Equipment | echo tier tools (reads toolEffects, CB: cross edge) | - | toolEffects | ItemEvents.toolTierRegistry, registry; NCH 0, tags 0, raw 1; no wrapper | no |
| `startup_scripts/items/end.js` | 3 | Equipment | felsic scrap, End gear material (end_gear.js) | - | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/misc_items.js` | 53 | Engineering | grits, wires, spool, transmitter, rose gold parts, arrow fletching | - | - | registry; NCH 0, tags 0; no wrapper | yes |
| `startup_scripts/items/plate_blocks.js` | 31 | Engineering | plate storage blocks | - ; top: PLATE_BLOCK_METALS | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/potion_buckets.js` | 86 | CB | potion bucket items (creative_brewing.md 4.2 phase 1) | - | potionArea | ItemEvents.modification, registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/sample_syringe.js` | 87 | CD | reserved for Creative Cultivation (see func/sample_syringe.js) | - ; top: SYRINGE_TEXTURE | sampleSyringe | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/stepstools.js` | 23 | Equipment | stepstool items (stepstool/) | - ; top: STEPSTOOL_COLOURS, stepstoolName | item | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/items/wood.js` | 5 | Engineering | wood pellets fuel item | - | - | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/jei/order.js` | 316 | Common | Interface: the pack JEI order definition (all jars' content, so Common) | - ; top: jeiE, jeiF, jeiI, jeiMb, jeiStorageKinds, jeiStorageList, jeiStorageWoods | effect, fluid, item, jeiOrder, multiblock | none; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/magic/magic_resistance.js` | 33 | CB | Magic Resistance effect (creative_brewing.md 4.2 phase 1); not Magic reservation: effects are CB | - | armorEffects | registry; NCH 0, tags 0; no wrapper | no |
| `startup_scripts/todo/create_ie_dict_additions.txt` | 911 | pack | stale generated note (.txt, not loaded); candidate for deletion rather than a jar | - | - | not a script | no |
| `startup_scripts/todo/not_in_dict.txt` | 689 | pack | stale generated note (.txt, not loaded); candidate for deletion rather than a jar | - | - | not a script | no |
| `startup_scripts/todo/recipes_to_remove.txt` | 262 | pack | stale generated note (.txt, not loaded); candidate for deletion rather than a jar | - | - | not a script | no |
| `startup_scripts/todo/remove_list.txt` | 28 | pack | stale generated note (.txt, not loaded); candidate for deletion rather than a jar | - | - | not a script | no |

Row ranges of the mixed files:

- `startup_scripts/func/trade_helper.js`:
  - 1-92: Common? (trader pool helpers)
  - 93-114: CB (Winemaker helpers (creative_brewing.md 4.2))
  - 115-117: Common? (close)
- `startup_scripts/items/misc_items.js`:
  - 1-25,27-46,48-53: Engineering (metal parts; arrow_fletching (l.17) is Equipment data)
  - 26,47: CD (jrftl:prepared_flesh and prepared_flesh_block: cultivated meat, reserved Creative Cultivation)

## 4. Server scripts: `cooking`, `economy`, `engineering`

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `server_scripts/cooking/alcohol.js` | 85 | CB | Brewin' keg and pickling recipe changes (creative_brewing.md 4.2) | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/cooking/cooking.js` | 4 | pack | empty stub kept to overwrite stale server copies; delete once every server has it | - | - | none; NCH 0, tags 0; no wrapper | no |
| `server_scripts/economy/coin.js` | 20 | Compat | Lightman's Currency coin recipe removals | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/economy/trades.js` | 117 | Engineering? | MoreJS villager trades for the Mechanical and Hydraulic Engineer professions (engineering goods); Worldgen if the owner files professions under fauna | - ; top: $MoreJSIntRange, allLevels, f, i, v | fluid, item | none; NCH 0, tags 0; no wrapper | no |
| `server_scripts/economy/wandering_trades.js` | 90 | Common? | wandering trader stock (same owner question as func/trade_helper.js) | - | item, trades | none; NCH 0, tags 0; no wrapper | yes |
| `server_scripts/engineering/blueprints.js` | 256 | Engineering | IE blueprints | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/components.js` | 110 | Engineering | component crafting, sequenced assembly, blueprints, disassembly; no circuit rows found (brief expected some) | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/computing.js` | 19 | Computers (Engineering) | ComputerCraft speaker recipe (2.3) | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/create_utilities.js` | 63 | Engineering | Create Utilities bearings and chain drives | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/crystal.js` | 49 | Engineering | amethyst | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/dreams_desires.js` | 20 | Engineering | painted asphalt | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/dust.js` | 109 | Engineering | dusts per machine | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/end.js` | 15 | Engineering | ender pearl recipes | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/fabric.js` | 106 | Engineering | IE fabric and wool | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/health.js` | 21 | Engineering | bandage and bottling alt | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/laser_cutting.js` | 13 | Engineering | laser cutting bottles | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/mechanisms.js` | 48 | Engineering | precision and vibration mechanisms | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0, raw 1; no wrapper | yes |
| `server_scripts/engineering/mob_resources.js` | 33 | Engineering | leather and rotten flesh | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/music.js` | 34 | Engineering | note block and jukebox | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/oil.js` | 29 | Engineering | bio oil unification | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/pneumatic.js` | 38 | Engineering | PNC compressed iron to steel | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/potion_bottling.js` | 107 | CB | IE potion bottling (creative_brewing.md 4.2) | - ; top: BOTTLES, BUCKETS, POTIONS, R, bucketRecipes, f (+3) | NCH, fluid, item | recipes; NCH 2, tags 0; no wrapper | no |
| `server_scripts/engineering/potion_buckets.js` | 57 | CB | potion bucket right-click (creative_brewing.md 4.2) | - | potionArea | ItemEvents.rightClicked; NCH 0, tags 0; no wrapper | no |
| `server_scripts/engineering/power.js` | 42 | Engineering | Create Additions removals, bulbs, vacuum tube | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/process.js` | 92 | Engineering | blast furnace, press | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/redstone.js` | 256 | Engineering? | redstone components (torch, repeater, comparator, redstone link, randomizer, rose quartz) by sequenced assembly; control-engineering content, candidate for the Computers reservation | - ; top: R, f, i, incComparator, incDaylightSensor, incFastRepeater (+9) | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/resource_blocks.js` | 254 | Engineering | resource stones and extruding | - ; top: R, f, i, u | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/sample_syringe.js` | 68 | CD | reserved for Creative Cultivation; syringe hurt/interact events | - | sampleSyringe | EntityEvents.hurt, ItemEvents.entityInteracted; NCH 0, tags 0; no wrapper | no |
| `server_scripts/engineering/transport.js` | 143 | Engineering | Little Logistics, rails | - ; top: R, i, incLlSpring, incRail | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/engineering/vibrating.js` | 19 | Engineering | vibrating shells | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |

Row ranges of the mixed files:

- `server_scripts/economy/wandering_trades.js`:
  - 1-75: Common? (trader pools)
  - 76-89: CB (Winemaker stock (creative_brewing.md 4.2))
  - 90: Common? (close)
- `server_scripts/engineering/mechanisms.js`:
  - 1-30: Engineering (precision, vibration mechanisms)
  - 31-48: Computers (Engineering)? (redstone mechanism (electron tube, redstone): the circuit row of this file)

## 5. Server scripts: `equipment`, `magic`, `metal`, `wood`

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `server_scripts/equipment/archery.js` | 295 | Equipment | arrows, bows, Nock Enough Arrows recipes | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/equipment/bullets.js` | 12 | Equipment | armor piercing bullets | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/equipment/camera_bag.js` | 23 | Equipment | camera bag smithing | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/equipment/dragon_autorepair.js` | 65 | Equipment | dragon gear passive repair | - | - | PlayerEvents.tick; NCH 0, tags 0; no wrapper | no |
| `server_scripts/equipment/end_gear.js` | 145 | Equipment | echo and felsic gear assembly | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/equipment/shield.js` | 38 | Equipment | shield recipes | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/equipment/stepstool.js` | 12 | Equipment | stepstool recipes | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/equipment/tipped_arrows.js` | 51 | CB | machine tipping of arrows with potions (creative_brewing.md 4.2) | - ; top: POTIONS, R, f, i, it, n | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/magic/armor_effects.js` | 108 | CB | gear effects applier, owner 2026-10-31 to CB | - ; top: $AbstractHorse, $ArmorFxCEConfig, $EquipmentSlot, $MobEffectInstance, ARMOR_SLOTS, HORSE_ARMOR_SLOT (+3) | armorEffects | PlayerEvents.tick, tick; NCH 0, tags 0; no wrapper | no |
| `server_scripts/magic/dragon_breathing.js` | 25 | Magic (Equipment) | dragon breath conversion recipes (recipes_magic.md) | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/magic/enchants.js` | 13 | Magic (Equipment) | enchantment trade filter fallback; 6 raw e.* calls, no NCH | - | - | none; NCH 0, tags 0, raw 6; no wrapper | no |
| `server_scripts/magic/lava_vision.js` | 27 | CB | Lava Vision mixer brew (creative_brewing.md 4.2) | - | NCH, fluid | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/magic/progression.js` | 14 | Magic (Equipment) | Botania terrasteel gating of compasses | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/magic/sculk.js` | 17 | Magic (Equipment) | sculk from XP fluid | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/magic/storage.js` | 42 | Magic (Equipment) | cloud, ender storage, Botania third eye | - ; top: ES_WOOL, R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/magic/tool_effects.js` | 75 | CB | gear effects applier, owner 2026-10-31 to CB | - ; top: $ToolMobEffectInstance, HELD_REFRESH_TICKS | armorEffects, toolEffects | EntityEvents.hurt, PlayerEvents.tick; NCH 0, tags 0; no wrapper | no |
| `server_scripts/magic/xp.js` | 23 | Magic (Equipment) | XP machines (Create Enchantment Industry) | - ; top: R, i | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/metal/metal_base_materials.js` | 762 | Engineering | metal recipes (2.1) | - ; top: COIL_METALS, R, i, incCoilBlockHv, incWirecoilHv | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/metal/metal_building.js` | 75 | Engineering | metal recipes (2.1) | - ; top: R, i, incBrassBars, incCastIronBars, incCopperBars, incGoldBars, incIronBars, incZincBars | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/metal/misc_metal_crafts.js` | 129 | Engineering | metal recipes (2.1) | - ; top: R, f, i, incChain, incSpool | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/ash.js` | 27 | Engineering | wood recipes (2.1) | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/bark.js` | 103 | Engineering | wood recipes (2.1) | - ; top: R, all, compatFd, compatSpecies, fdSpecies, hollowBark (+4) | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/compost.js` | 9 | Engineering | seeds to bone meal on millstone/crushing wheels (machine recipe, not compost framework) | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/removed_woods.js` | 19 | Engineering | removed wood recipes removed before item_merges swaps (reads Common removedWoods) | - | NCH, item, removedWoodTracks, removedWoods | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/sawdust.js` | 89 | Engineering | wood recipes (2.1) | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/smoked_wood.js` | 40 | Engineering | wood recipes (2.1) | - | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/wood/wood_types.js` | 57 | Engineering | wood recipes (2.1) | - ; top: R, doubleForms, i, singleForms, species | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |

## 6. Server scripts: `_lib` dumps

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `server_scripts/_lib/dump_all.js` | 77 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_bee_nests.js` | 29 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_biome.js` | 71 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpBiomes | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_climate.js` | 205 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_compost.js` | 33 | Devtools | /dump* command (2.1 Devtools) | - | compost | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_crafting_audit.js` | 148 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_crop_growth.js` | 131 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpCropGrowth | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_flower_pots.js` | 155 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpFlowerPots | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_fluid.js` | 53 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpFluidTags | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_food.js` | 274 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpFood | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_gear.js` | 395 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpGear | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_items.js` | 59 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpItemsByMod | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_names.js` | 119 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_paintings.js` | 43 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_physics.js` | 147 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpPhysics | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_placeable.js` | 134 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpPlaceable | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_potions.js` | 114 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpPotions | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_recipes.js` | 159 | Devtools | /dump* command (2.1 Devtools) | - ; top: csvSafe, dumpRecipes, getRecipeManager, getRecipeType, writeCsv | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_spawns.js` | 22 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_stacks.js` | 130 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpStacks | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_structuregen.js` | 202 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/dump_tags.js` | 132 | Devtools | /dump* command (2.1 Devtools) | - ; top: dumpRegistryTags, dumpTags | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/export_io.js` | 26 | Devtools | writeExportJson helper for the dumps | - ; top: writeExportJson | - | none; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/flower_world.js` | 288 | Devtools? | Flower Test world builder and /flowerworld; a test tool, but its world preset ships in patcherMod data and the header says it ships with the pack | - | - | commandRegistry, loaded; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/java_guard.js` | 28 | Devtools | guardThrowable helper for the dumps | - ; top: guardThrowable | - | none; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/loot_dump.js` | 190 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/metal_test_recipes.js` | 108 | Devtools? | live mechanical-crafting test recipes, ungated; Devtools if dev-only is accepted, else Engineering | - ; top: R, alphabet, buildGrid, i, seqForms, seqMetals | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/_lib/recipe_scan.js` | 101 | Common | removed-item tag strip is live (CD _core/removed_item_tags.js calls global.stripRemovedItemTags); the scan command is a diagnostic | hasScannedRemovedItemRecipes, stripRemovedItemTags ; top: scanRemovedItemRecipes | NCH, removedItems, tags | commandRegistry, loaded, tags; NCH 1, tags 1; no wrapper | yes |
| `server_scripts/_lib/sequence_assembly_processes.js` | 92 | Devtools? | JEI showcase chain of every sequenced-assembly step, ungated (11 raw e.* calls); Devtools or Engineering | - ; top: R | NCH, fluid, item | recipes; NCH 1, tags 0, raw 11; no wrapper | no |
| `server_scripts/_lib/structure_dump.js` | 118 | Devtools | /dump* command (2.1 Devtools) | - | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/villager_trades_dump.js` | 164 | Devtools | /dump* command (2.1 Devtools) | - ; top: $EntityType, $ForgeRegistries, $HashMap, $RandomSource, $VillagerProfession, $VillagerTrades (+7) | - | commandRegistry; NCH 0, tags 0; no wrapper | no |
| `server_scripts/_lib/wood_test_recipes.js` | 90 | Devtools? | as metal_test_recipes.js | - ; top: R, alphabet, buildGrid, i, seqForms, seqSpecies | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |

Row ranges of the mixed files:

- `server_scripts/_lib/recipe_scan.js`:
  - 1-73,100-101: Common (stripRemovedItemTags, hasScannedRemovedItemRecipes, ServerEvents.tags strip)
  - 74-99: Devtools (/scan command and loaded-hook diagnostic log)

## 7. Server scripts: `loot`, `worldgen`

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `server_scripts/loot/00_lib.js` | 141 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | LOOT | - | LootJS.modifiers; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/05_tuning.js` | 1352 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | lootTuning | item, tags | LootJS.modifiers, PlayerEvents.loggedIn, commandRegistry, genericLootTables, tags; NCH 0, tags 2; no wrapper | no |
| `server_scripts/loot/06_dead_maps.js` | 79 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | - | genericLootTables; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/07_rules.js` | 134 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | item, lootTuning | none; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/08_leaf_apples.js` | 61 | Worldgen | leaf apple drops off; apples come from CD wild crop trees (data edge to CD, no global) | - | item | genericLootTables; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/10_removals.js` | 33 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | LOOT | LootJS.modifiers; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/20_frequency.js` | 32 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | LOOT | LootJS.modifiers; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/30_redistribute.js` | 32 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | LOOT | LootJS.modifiers; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/40_targeted.js` | 26 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | LOOT | LootJS.modifiers; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/95_tag_capture.js` | 11 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | lootTuning | tags; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/99_debug.js` | 10 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | - | LootJS.modifiers; NCH 0, tags 0; no wrapper | no |
| `server_scripts/loot/README.md` | 186 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | - | not a script | no |
| `server_scripts/loot/loot_census.md` | 725 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | - | not a script | no |
| `server_scripts/loot/loot_config.json` | 10976 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | - | not a script | no |
| `server_scripts/loot/loot_index.json` | 14909 | Worldgen | chest/block loot framework (2.1 Worldgen, loot/) | - | - | not a script | no |
| `server_scripts/worldgen/_biome_rules.js` | 257 | Worldgen | shared biome-rule engine (global.biomeRules) read by CD wild crops: cross edge, candidate Common API | biomeRules | tags | none; NCH 0, tags 1, raw 3; no wrapper | no |
| `server_scripts/worldgen/bee_nests.js` | 311 | Worldgen | worldgen (2.1) | - | tags | highPriorityData, loaded, tags; NCH 0, tags 2, raw 3; no wrapper | no |
| `server_scripts/worldgen/biome_overrides.js` | 44 | Worldgen | worldgen (2.1) | - | - | highPriorityData; NCH 0, tags 0, raw 1; no wrapper | no |
| `server_scripts/worldgen/earth_fill.js` | 49 | Worldgen | worldgen (2.1) | - | tags | tags; NCH 0, tags 4; no wrapper | no |
| `server_scripts/worldgen/ground_match.js` | 7 | Worldgen | worldgen (2.1) | - | tags | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/worldgen/mob_spawns.js` | 406 | Worldgen | mob spawn tuning (fauna) | - | tags | highPriorityData, loaded, tags; NCH 0, tags 4, raw 2; no wrapper | no |
| `server_scripts/worldgen/mushroom_spawns.js` | 52 | Worldgen | mushroom worldgen via biomeRules (flora; Fungi Delight mushrooms are worldgen placement, not food) | - | biomeRules | highPriorityData, loaded, tags; NCH 0, tags 0; no wrapper | no |
| `server_scripts/worldgen/structure_overrides.js` | 1448 | Worldgen | worldgen (2.1) | - | tags | highPriorityData, tags; NCH 0, tags 5, raw 2; no wrapper | no |
| `server_scripts/worldgen/terrain_adaptation.js` | 142 | Worldgen | worldgen (2.1) | - | tags | tags; NCH 0, tags 17; no wrapper | no |
| `server_scripts/worldgen/water_fill.js` | 28 | Worldgen | worldgen (2.1) | - | tags | tags; NCH 0, tags 2; no wrapper | no |
| `server_scripts/worldgen/wild_flowers.js` | 667 | Worldgen | worldgen (2.1) | - | tags | highPriorityData, loaded, tags; NCH 0, tags 1, raw 1; no wrapper | no |

## 8. Server scripts: `other`, `tags`

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `server_scripts/other/backpack_skins.js` | 14 | Equipment | backpack skin smithing and tag (backpackskin/ is Equipment in 2.1) | - ; top: BACKPACK_SKIN_TAG | NCH, item, tags | recipes, tags; NCH 2, tags 1; no wrapper | no |
| `server_scripts/other/creative_labels.js` | 40 | pack? | recipes of the in-tree creativeLabels mod and its donor-recipe removals; best owner is a script pack inside that jar, else Compat | - | NCH, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/other/dead_recipe_removal.js` | 34 | Compat | removes dead third-party recipes | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/other/feature_ports.js` | 107 | Compat | ported tier-1 recipe mods (name tag, copper splashing, furnace recycle, AE2 QoL cables); the AE2 cable block (l.?) is Computers-adjacent, note for the reservation doc | - | NCH, item | recipes; NCH 2, tags 0; no wrapper | no |
| `server_scripts/other/flowers.js` | 33 | Compat | Bushier Flowers bush craft, Beautify pot soil (flower pots) | - | NCH, item, removedItems | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/other/harvest.js` | 96 | Equipment? | one harvest tier and tool per block (mineable tags): tool progression, so Equipment; Common if seen as a pack-wide rule | - | item, tags | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/other/item_merges.js` | 41 | Common | pack item-merge group runner (recipes, tags, loot, inventory); the framework CD's own group also uses | - | NCH, item, itemMergeRun | LootJS.modifiers, PlayerEvents.inventoryChanged, recipes; NCH 1, tags 0; wrapper: itemMergeRun | no |
| `server_scripts/other/jeed_providers.js` | 216 | CB | JEED effect providers (creative_brewing.md 4.2; reads armorEffects/toolEffects) | - | NCH, armorEffects, item, toolEffects | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/other/misc_mods.js` | 77 | Compat | Supplementaries and Quark recipe tweaks | - ; top: R, f, i | NCH, fluid, item | recipes; NCH 1, tags 0; no wrapper | no |
| `server_scripts/other/removed_mobs.js` | 13 | Worldgen | removed mobs refused on spawn (fauna, 2.1) | - ; top: REMOVED_MOBS | removedItems | EntityEvents.spawned; NCH 0, tags 0; no wrapper | no |
| `server_scripts/other/starter_kit.js` | 92 | Compat | Starter Kit port (feature_porting.md 1) | - ; top: STARTER_KITS | item | PlayerEvents.loggedIn; NCH 0, tags 0; no wrapper | no |
| `server_scripts/tags/blocktags.js` | 520 | Common | block tags of many systems | - ; top: b, item, t | blocks, item, tags | tags; NCH 0, tags 1; no wrapper | yes |
| `server_scripts/tags/cookingtags.js` | 4 | pack | empty stub kept to overwrite stale copies | - | - | none; NCH 0, tags 0; no wrapper | no |
| `server_scripts/tags/cropseasontags.js` | 68 | Calendar | crop season tags (seasons/CropSeasons) | - | tags | tags; NCH 0, tags 2; no wrapper | no |
| `server_scripts/tags/curiostags.js` | 26 | Compat | curios slot membership | - | item, tags | tags; NCH 0, tags 1; no wrapper | yes |
| `server_scripts/tags/engineeringtags.js` | 38 | Engineering | bulb glass, filament, blueprint shape tags | - | blocks, item, tags | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/tags/entitytags.js` | 14 | Worldgen? | bubble column immune mobs (BubbleColumnImmunityMixin); fauna tweak, else Common | - ; top: t | tags | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/tags/equipmenttags.js` | 88 | Equipment | gear tags | - ; top: i, shaft, t | item, tags | tags; NCH 0, tags 11; no wrapper | yes |
| `server_scripts/tags/feature_port_tags.js` | 18 | Compat | ported mod tags | - | item, tags | tags; NCH 0, tags 2; no wrapper | yes |
| `server_scripts/tags/fluidtags.js` | 116 | Engineering | fluid category tags | - | fluid, tags | tags; NCH 0, tags 1; no wrapper | yes |
| `server_scripts/tags/item_physics_tags.js` | 400 | Compat | ItemPhysic behaviour tags | - ; top: COLORS, COPPER, PHYSICS_TAG, physicsFromTag | item, tags | tags; NCH 0, tags 3; no wrapper | no |
| `server_scripts/tags/jei_order_tags.js` | 7 | Common | JEI order tags from global.jeiOrder | - | jeiOrder | tags; NCH 0, tags 0; no wrapper | no |
| `server_scripts/tags/loottags.js` | 49 | Worldgen | loot meta tags | - ; top: b, i, t | blocks, item, tags | tags; NCH 0, tags 1; no wrapper | yes |
| `server_scripts/tags/metaltags.js` | 102 | Engineering | metal part tags, springs, coil and component families | - | item, tags | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/tags/misctags.js` | 78 | Common | misc tags | - ; top: GLASS_SUB, b, i, rollupGlass, t | blocks, item, tags | tags; NCH 0, tags 1; no wrapper | yes |
| `server_scripts/tags/seasontags.js` | 64 | Calendar | season type and snow biome tags (seasons/SeasonTypes) | - | tags | tags; NCH 0, tags 2; no wrapper | no |
| `server_scripts/tags/storageblocktags.js` | 39 | Engineering | storage block mineable tags | - ; top: doughStorage, i, softStorage, storage, t | item, tags | tags; NCH 0, tags 1; no wrapper | yes |
| `server_scripts/tags/tradetags.js` | 17 | Common? | wandering trade item groups (same owner as trade_helper) | - ; top: i, t | item, tags, trades | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/tags/wood_tags.js` | 63 | Engineering | mulch, millstone seeds, barrel, treated wood, smokable wood tags | - ; top: i, smokeShapes, smokeWoods, t | item, tags | tags; NCH 0, tags 1; no wrapper | no |
| `server_scripts/tags/wthittags.js` | 34 | Common | WTHIT silk touch tag (Interface) | - | tags | tags; NCH 0, tags 2; no wrapper | no |

Row ranges of the mixed files:

- `server_scripts/tags/blocktags.js`:
  - 1-20: Common (D&D ghost axe entries strip (engine fix))
  - 21-31: CD (oven heat sources, stove heat (oven/))
  - 32-35: Worldgen? (monster box non-movable (mob_block_tweaks))
  - 36-38: CD (bakeware hammering on cutting board (cooking/))
  - 39-47: Compat (CfB counters note, MCA villager ladders (Worldgen fauna), Rubidium blendable: 40-43 Worldgen, 44-46 Compat)
  - 48-68: CB (keg heat and freeze sources)
  - 69-74: CD (campfire rain (campfire/))
  - 75-78: Compat (Supplementaries no_world_dye)
  - 79-83: Engineering (vein undo denied (excavation/))
  - 84-91: Common (forge:concrete, bricks, stone_bricks rollups)
  - 92-453: Engineering (BlockRunner surface tiers, sticky, camo, sinking (surface/))
  - 454-500: Worldgen (ground match, earth fill, template trim tags)
  - 501-509: CD (crop parity farmland soils and crops (crops/))
  - 510-521: Worldgen (wild plant soils (vegetation worldgen))
- `server_scripts/tags/curiostags.js`:
  - 1-20: Compat (hands and face slots for Simple Hats, Botania masks, Falconry glove, Cob Pipe)
  - 21-26: Equipment (stepstools feet slot)
- `server_scripts/tags/equipmenttags.js`:
  - 1-33: Equipment (elytra chestplates, dragon gear, bonemeal charms)
  - 34-44: CD (hoe harvest area tags (crops/HoeHarvestArea))
  - 45-88: Equipment (fletching, shafts, camera equipment)
- `server_scripts/tags/feature_port_tags.js`:
  - 1-11: Compat (AE2 QoL water bucket tags)
  - 12-18: Engineering (Bottled Air fire sources (item/BottledAir))
- `server_scripts/tags/fluidtags.js`:
  - 1-12: Common (vanilla forge:fluid subtags)
  - 13-44: CD (food and drink fluid subtags)
  - 45-74: Engineering (plant growth, industrial, creosote, oil chain)
  - 75-84: CB (magic/potion fluids, IE potion tag removal (creative_brewing.md 4.2))
  - 86-90: Magic (Equipment) (XP fluid merge)
  - 91-116: Common (fixes and removals (bad water tags, dead fluids))
- `server_scripts/tags/loottags.js`:
  - 1-18: Worldgen (chest loot groups (chalk, golden food))
  - 19-49: CD (Oceanic Delight fishing net biome and block tags (OceanicDelightFishingNetMixin))
- `server_scripts/tags/misctags.js`:
  - 1-32: Common (glass tag rollup)
  - 33-44: Engineering (Create limestone stone type)
  - 45-57: Compat (flower box and planter box tags)
  - 58-62: Common (forge:seeds completion)
  - 63-78: Compat (chalk dye tags, cbmicroblock tags)
- `server_scripts/tags/storageblocktags.js`:
  - 1-16: Engineering (metal and soft storage blocks)
  - 17-39: CD (dough blocks (shovel))

## 9. Client scripts

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `client_scripts/armor_effects_tooltip.js` | 52 | CB | gear effects tooltip (owner 2026-10-31) | - | armorEffects | ItemEvents.tooltip; NCH 0, tags 0, raw 3; no wrapper | no |
| `client_scripts/fluid_jei.js` | 63 | Common | JEI fluid list | - ; top: DEAD_FLUIDS | jeiOrder | JEIEvents.addFluids; NCH 0, tags 0, raw 2; no wrapper | yes |
| `client_scripts/jei_order.js` | 9 | Common | hands global.jeiOrder to PackIngredientOrder | - | jeiOrder | none; NCH 0, tags 0; no wrapper | no |
| `client_scripts/jeichanges.js` | 58 | Common | JEI hide of removed items and unwanted enchants (Interface) | - ; top: hideRemovedItems, tagRemovedItems, unwantedEnchants | removedItems | ItemEvents.tooltip, JEIEvents.hideItems; NCH 0, tags 0, raw 1; no wrapper | no |
| `client_scripts/name_fixes.js` | 25 | Common | names audit fixes (generated) | - | - | lang; NCH 0, tags 0, raw 1; no wrapper | no |
| `client_scripts/namechanges.js` | 349 | Common | display names | - | item | lang; NCH 0, tags 0, raw 134; no wrapper | yes |
| `client_scripts/potion_buckets.js` | 72 | CB | potion bucket tooltip (creative_brewing.md 4.2) | - ; top: $PB_MobEffectCategory, $PB_PotionUtils, $PB_StringUtil, PB_TOOLTIP_IDS, pbDurationText | potionArea | ItemEvents.tooltip; NCH 0, tags 0; no wrapper | no |
| `client_scripts/texture_tool.js` | 61 | Devtools | console texture probe (dumpIncompleteItemTextures) | dumpIncompleteItemTextures, getItemTextureLocation, printItemTexture ; top: dumpIncompleteItemTextures, getItemTextureLocation, pendingIncompleteItems, printItemTexture | item | none; NCH 0, tags 0; no wrapper | no |
| `client_scripts/tool_effects_tooltip.js` | 26 | CB | gear effects tooltip (owner 2026-10-31) | - | armorEffects, toolEffects | ItemEvents.tooltip; NCH 0, tags 0, raw 2; no wrapper | no |
| `client_scripts/tooltips.js` | 853 | Common | item tooltips | - ; top: NCH_MCRegistry, addTooltipPattern, addTooltips, buildTooltipLine, i, removeTooltipKey, removeTooltips | item | ItemEvents.tooltip; NCH 0, tags 0, raw 3; no wrapper | yes |

Row ranges of the mixed files:

- `client_scripts/fluid_jei.js`:
  - 1-41: Common (register every fluid in JEI)
  - 42-63: CB (dead brewing fluids hidden (creative_brewing.md 4.2))
- `client_scripts/namechanges.js`:
  - 1-7,12-61: Common (lang fixes, JEI info)
  - 8-11: Compat (flower pot names)
  - 62-227: Common (general renames (Quark permafrost etc.))
  - 228-233: CB (Vinery wood and cider names (creative_brewing.md 4.2))
  - 234-248: Common (merge names)
  - 249-333: CD (Delight dish and crop names (Oceanic, Fruits, Veggies, Vintage, Ender's; the header says food names moved to CD on 10-27, these rows are left behind))
  - 334-340: CD (lunchbox tier names)
  - 341-349: Engineering (painted asphalt names)
- `client_scripts/tooltips.js`:
  - 1-91: Common (helpers addTooltips/removeTooltips, colours)
  - 92-389: Common (global, colour, material information)
  - 390-489: Common (tools, crafting materials, blocks, decor, IE, Create, transport: third-party data)
  - 490-515: CD (food and Farmer's Delight lines)
  - 516-521: Equipment (archery (commented out))
  - 522-547: Common (jukebox, Etched)
  - 548-627: Compat (misc mods (chimes...); Brewin' keg temperature and goblet rows inside go CB, line numbers to fix at move)
  - 628-652: CB (Vinery section (creative_brewing.md 4.2))
  - 653-794: Compat (Quark, Supplementaries, Framed; flower box row to CB)
  - 795-807: Compat (flower pots)
  - 808-814: Magic (Equipment) (Botania)
  - 815-853: Equipment (End gear)

## 10. Jar startup scripts (patcherMod `kubejs_startup/creativeengineering/func/`)

| file | lines | target | reason | defines | reads | events; NCH/tags; wrapper | mixed? |
|---|---|---|---|---|---|---|---|
| `kubejs_startup/.../func/compost.js` | 395 | Common | shared compost helper (rule 3.4) | compost, compostListening, compostOriginal ; top: $CompostBlockItem, $CompostComposter, $CompostRegistries, $CompostRegistry, $CompostResourceLocation, $CompostTagKey | - | ForgeEvents.onEvent; NCH 0, tags 0; no wrapper | no |
| `kubejs_startup/.../func/neoCraftHelper.js` | 2518 | Common | NCH recipe helper (rule 3.4) | NCH ; top: $NCH_CEConfigClass, $NCH_ForgeRegistries, $NCH_ResourceLocation | fluid, item, seasoning | registry; NCH 431, tags 0, raw 52; no wrapper | no |
| `kubejs_startup/.../func/tag_helper.js` | 269 | Common | global.tags helper (rule 3.4) | tags | - | tags; NCH 0, tags 4, raw 20; no wrapper | no |

## 11. Global name graph

Definer target and reader targets. `down (Common API)` = defined in Common, read by content jars (allowed, rule 2); `cross` = definer and a reader in different non-Common targets (needs a fix, listed below); `up` = defined in the CD jar, read by the pack. Readers inside the CD and CB jars are counted from `development/creativeDelight/src`, `development/creativeBrewing/src`, `kubejs/data/creative_*` and `kubejs/assets/creative_*` (build and moved_code_backup copies excluded). No CD or CB script reads a pack-defined content global other than `biomeRules`, `lootTuning` and `trades` (all from `kubejs/data/creative_delight/kubejs/`: `_worldgen/wild_crops.js`, `*/loot.js`, `*/trades.js`).

| global | defined in (target) | pack readers (target) | CD/CB jar readers (src and kubejs/data, build copies excluded) | edge |
|---|---|---|---|---|
| `LOOT` | `server_scripts/loot/00_lib.js` (Worldgen) | Worldgen x4 | - | none (same target) |
| `NCH` | `kubejs_startup/.../func/neoCraftHelper.js` (Common) | CB x5, Common x3, Compat x5, Computers (Engineering) x1, Devtools x3, Engineering x32, Equipment x8, Magic (Equipment) x5, pack x1 | CD jar x69 | down (Common API), fine |
| `addItemMerges` | `startup_scripts/_lib/item_merges.js` (Common) | Common x1 | CD jar x6 | down (Common API), fine |
| `alcohol` | `startup_scripts/cooking/alcohol_drinks.js` (CB) | - | - | none (same target) |
| `armorEffects` | `startup_scripts/func/armor_effects.js` (CB) | CB x7 | - | none (same target) |
| `biomeRules` | `server_scripts/worldgen/_biome_rules.js` (Worldgen) | Worldgen x1 | CD jar x2 | cross: Worldgen -> CD jar |
| `blocks` | `startup_scripts/_lib/block_dict.js` (Common) | Common x2, Engineering x2, Worldgen x1 | CD jar x10 | down (Common API), fine |
| `compost` | `kubejs_startup/.../func/compost.js` (Common) | Devtools x1 | CD jar x80 | down (Common API), fine |
| `compostListening` | `kubejs_startup/.../func/compost.js` (Common) | - | - | none (same target) |
| `compostOriginal` | `kubejs_startup/.../func/compost.js` (Common) | - | - | none (same target) |
| `dumpIncompleteItemTextures` | `client_scripts/texture_tool.js` (Devtools) | - | - | none (same target) |
| `effect` | `startup_scripts/_lib/effect_dict.js` (Common) | CB x2, Common x1 | CD jar x1 | down (Common API), fine |
| `fluid` | `startup_scripts/_lib/fluid_dict.js` (Common) | CB x4, Common x2, Compat x3, Devtools x1, Engineering x14, Equipment x3, Magic (Equipment) x3 | CD jar x27 | down (Common API), fine |
| `food` | `startup_scripts/_lib/food_dict.js` (Common) | Common x1 | - | none (same target) |
| `foodEffectSwaps` | `startup_scripts/cooking/food_values.js` (CB) | - | - | none (same target) |
| `foodGrammar` | `startup_scripts/_lib/food_grammar.js` (Common) | Common x1 | - | none (same target) |
| `foodKey` | `startup_scripts/_lib/food_dict.js` (Common) | - | - | none (same target) |
| `foodValueCopies` | CD jar (`kubejs_startup/creativeengineering/food_value_copies.js`) | CB x1 | - | down: CB reads the CD jar (CB depends on CD), fine |
| `getItemTextureLocation` | `client_scripts/texture_tool.js` (Devtools) | - | - | none (same target) |
| `hasScannedRemovedItemRecipes` | `server_scripts/_lib/recipe_scan.js` (Common), `startup_scripts/_lib/removed_items.js` (Common) | - | - | none (same target) |
| `item` | `startup_scripts/_lib/item_dict.js` (Common) | CB x9, CD x1, Common x19, Compat x9, Computers (Engineering) x1, Devtools x4, Engineering x38, Equipment x12, Magic (Equipment) x5, Worldgen x4, pack x1 | CD jar x234 | down (Common API), fine |
| `itemMergeGroups` | `startup_scripts/_lib/item_merges.js` (Common) | - | - | none (same target) |
| `itemMergeRun` | `startup_scripts/_lib/item_merges.js` (Common) | Common x1 | CD jar x3 | down (Common API), fine |
| `itemMerges` | `startup_scripts/_lib/item_merges.js` (Common) | - | - | none (same target) |
| `itemPhysics` | `startup_scripts/func/item_physics.js` (Compat) | - | - | none (same target) |
| `jeiOrder` | `startup_scripts/func/jei_order.js` (Common) | Common x4 | - | none (same target) |
| `lootTuning` | `server_scripts/loot/05_tuning.js` (Worldgen) | Worldgen x2 | CD jar x7 | cross: Worldgen -> CD jar |
| `multiblock` | `startup_scripts/_lib/multiblock_dict.js` (Common) | Common x1 | - | none (same target) |
| `potionArea` | `startup_scripts/func/potion_area.js` (CB) | CB x3 | - | none (same target) |
| `printItemTexture` | `client_scripts/texture_tool.js` (Devtools) | - | - | none (same target) |
| `removedItems` | `startup_scripts/_lib/removed_items.js` (Common) | Common x5, Compat x1, Worldgen x1 | CD jar x11 | down (Common API), fine |
| `removedWoodTracks` | `startup_scripts/_lib/removed_woods.js` (Common) | Engineering x1 | - | down (Common API), fine |
| `removedWoods` | `startup_scripts/_lib/removed_woods.js` (Common) | Engineering x1 | - | down (Common API), fine |
| `sampleSyringe` | `startup_scripts/func/sample_syringe.js` (CD) | CD x2 | - | none (same target) |
| `seasoning` | CD jar (`kubejs_startup/creativeengineering/seasonings.js`) | Common x1 | - | up: CD jar -> Common |
| `stripRemovedItemTags` | `server_scripts/_lib/recipe_scan.js` (Common) | - | CD jar x2 | down (Common API), fine |
| `subdict` | `startup_scripts/_lib/food_grammar.js` (Common) | Common x1 | - | none (same target) |
| `tags` | `kubejs_startup/.../func/tag_helper.js` (Common) | Calendar x2, Common x6, Compat x3, Engineering x5, Equipment x3, Worldgen x12 | CD jar x53 | down (Common API), fine |
| `toolEffects` | `startup_scripts/func/tool_effects.js` (CB) | CB x3, Equipment x1 | - | cross: CB -> Equipment |
| `trades` | `startup_scripts/func/trade_helper.js` (Common?) | Common x2 | CD jar x3 | down (Common API), fine |
| `tuneArmor` | `startup_scripts/func/tune_armor.js` (Equipment) | - | - | none (same target) |
| `tuneTools` | `startup_scripts/func/tune_tools.js` (Equipment) | - | - | none (same target) |

Readers per global (pack files), for the carve docs:

- `LOOT`: `server_scripts/loot/10_removals.js`, `server_scripts/loot/20_frequency.js`, `server_scripts/loot/30_redistribute.js`, `server_scripts/loot/40_targeted.js`
- `NCH`: 63 pack files in every target (Common dict or helper; stays shared, rule 3.4). 
- `addItemMerges`: `startup_scripts/_lib/removed_woods.js`
- `armorEffects`: `client_scripts/armor_effects_tooltip.js`, `client_scripts/tool_effects_tooltip.js`, `server_scripts/magic/armor_effects.js`, `server_scripts/magic/tool_effects.js`, `server_scripts/other/jeed_providers.js`, `startup_scripts/func/tool_effects.js`, `startup_scripts/magic/magic_resistance.js`
- `biomeRules`: `server_scripts/worldgen/mushroom_spawns.js`
- `blocks`: 5 pack files in every target (Common dict or helper; stays shared, rule 3.4). 
- `compost`: `server_scripts/_lib/dump_compost.js`
- `effect`: 3 pack files in every target (Common dict or helper; stays shared, rule 3.4). 
- `fluid`: 30 pack files in every target (Common dict or helper; stays shared, rule 3.4). 
- `food`: `startup_scripts/_lib/food_grammar.js`
- `foodGrammar`: `startup_scripts/_lib/food_dict.js`
- `foodValueCopies`: `startup_scripts/cooking/food_values.js`
- `item`: 103 pack files in every target (Common dict or helper; stays shared, rule 3.4). 
- `itemMergeRun`: `server_scripts/other/item_merges.js`
- `jeiOrder`: `client_scripts/fluid_jei.js`, `client_scripts/jei_order.js`, `server_scripts/tags/jei_order_tags.js`, `startup_scripts/jei/order.js`
- `lootTuning`: `server_scripts/loot/07_rules.js`, `server_scripts/loot/95_tag_capture.js`
- `multiblock`: `startup_scripts/jei/order.js`
- `potionArea`: `client_scripts/potion_buckets.js`, `server_scripts/engineering/potion_buckets.js`, `startup_scripts/items/potion_buckets.js`
- `removedItems`: `client_scripts/jeichanges.js`, `server_scripts/_lib/recipe_scan.js`, `server_scripts/other/flowers.js`, `server_scripts/other/removed_mobs.js`, `startup_scripts/_lib/item_dict.js`, `startup_scripts/_lib/item_merges.js`, `startup_scripts/_lib/removed_woods.js`
- `removedWoodTracks`: `server_scripts/wood/removed_woods.js`
- `removedWoods`: `server_scripts/wood/removed_woods.js`
- `sampleSyringe`: `server_scripts/engineering/sample_syringe.js`, `startup_scripts/items/sample_syringe.js`
- `seasoning`: `kubejs_startup/.../func/neoCraftHelper.js`
- `subdict`: `startup_scripts/_lib/food_dict.js`
- `tags`: 31 pack files in every target (Common dict or helper; stays shared, rule 3.4). 
- `toolEffects`: `client_scripts/tool_effects_tooltip.js`, `server_scripts/magic/tool_effects.js`, `server_scripts/other/jeed_providers.js`, `startup_scripts/items/echo_tools.js`
- `trades`: `server_scripts/economy/wandering_trades.js`, `server_scripts/tags/tradetags.js`

### Cross-target edges that need a decision

- **CB reads Common/Equipment/Worldgen globals, fine (down).** `armorEffects`, `toolEffects`, `potionArea`, `alcohol`, `foodEffectSwaps` are defined and read inside the CB set (plus `jeed_providers.js`, also CB): no edge once the whole set moves.
- **Equipment reads CB:** `items/echo_tools.js` reads `global.toolEffects` (CB) to attach echo tool effects. Sideways edge Equipment -> CB; fix: the effect spec rows move to CB (echo tool effects are gear effects, creative_brewing.md 4.2) or Equipment gets a soft check `if (global.toolEffects)`.
- **Common does not read Equipment:** `changes/stack_sizes.js` names `global.tuneArmor` / `global.tuneTools` in a comment only (line 6); the grep hit is not an edge.
- **CD reads Worldgen:** CD wild crops (`kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.js`) read `global.biomeRules` defined in `worldgen/_biome_rules.js` (Worldgen). Sideways edge CD -> Worldgen; fix: move `_biome_rules.js` into Common as the biome-rule API, or make Worldgen a soft dependency of the wild-crop file.
- **CD reads Worldgen (loot):** CD scripts call `global.lootTuning` (21 uses; `loot/05_tuning.js`, Worldgen). Fix: `00_lib.js` + the `lootTuning` API part of `05_tuning.js` move to Common, or CD depends on Worldgen (not allowed by rule 2 as a hard edge).
- **CD reads the trader helper:** `global.trades` (CD `*/trades.js` for Veggies, Corn and Rustic Delight stock) is defined in `func/trade_helper.js` (`Common?`). Resolves itself if the helper is Common.
- **CD reads Common runtime pieces:** `compost` (80 uses), `removedItems` (11), `blocks` (10), `fluid` (27), `effect`, `addItemMerges` (6), `itemMergeRun` (3), `stripRemovedItemTags` (2, defined in `server_scripts/_lib/recipe_scan.js`, hence that file is Common, not Devtools). All downward once those files are Common.
- **Pack reads CD:** `cooking/food_values.js` reads `global.foodValueCopies` (defined in the CD jar) and `neoCraftHelper.js` (Common) reads `global.seasoning` (CD jar). The NCH one is an upward edge Common -> CD; fix: NCH takes seasoning through a registration hook (`global.NCH.seasonings = ...` set by CD) or a soft check. `food_values.js` goes to CB, which depends on CD already.
- **Load order:** the pack `startup_scripts` load after patcherMod's jar pack and before CD and CB (creative_brewing.md 70). `func/armor_effects.js`, `tool_effects.js`, `tune_*.js` carry `// priority: -100` to load after `item_dict.js`; `jei_order.js` 900, `food_grammar.js` 990, `_lib` dicts 1000 / -10000..-10002. Any file moved into a jar pack keeps its priority but the cross-pack order becomes mod-list order, so `global.item` readers in a jar must only read at event time, not at top level.

## 12. Lines per target

Mixed files are counted by row range. Non-script files (loot json and md, todo txt) are listed separately.

| target | lines (scripts) |
|---|---|
| Common | 23061 |
| Worldgen | 5430 |
| Engineering | 4232 |
| Devtools | 4023 |
| CB | 2191 |
| Compat | 1231 |
| Equipment | 1217 |
| CD | 482 |
| Magic (Equipment) | 146 |
| Calendar | 132 |
| pack | 48 |
| Computers (Engineering) | 37 |
| **total scripts** | 42230 |

| target | lines (non-script data and notes) |
|---|---|
| Worldgen | 26796 |
| pack | 1890 |

Of the Common total, the `_lib` dicts are 18017 lines and the three jar helpers 3182 lines; the rest of Common is 1862 lines of pack scripts (JEI order, names, tooltips, item merges, shared tags).

## 13. Mixed files

16 files, split by row (rule 3.7):

- `startup_scripts/func/trade_helper.js` (117): CB, Common
- `startup_scripts/items/misc_items.js` (53): CD, Engineering
- `server_scripts/economy/wandering_trades.js` (90): CB, Common
- `server_scripts/engineering/mechanisms.js` (48): Computers (Engineering), Engineering
- `server_scripts/_lib/recipe_scan.js` (101): Common, Devtools
- `server_scripts/tags/blocktags.js` (520): CB, CD, Common, Compat, Engineering, Worldgen
- `server_scripts/tags/curiostags.js` (26): Compat, Equipment
- `server_scripts/tags/equipmenttags.js` (88): CD, Equipment
- `server_scripts/tags/feature_port_tags.js` (18): Compat, Engineering
- `server_scripts/tags/fluidtags.js` (116): CB, CD, Common, Engineering, Magic (Equipment)
- `server_scripts/tags/loottags.js` (49): CD, Worldgen
- `server_scripts/tags/misctags.js` (78): Common, Compat, Engineering
- `server_scripts/tags/storageblocktags.js` (39): CD, Engineering
- `client_scripts/fluid_jei.js` (63): CB, Common
- `client_scripts/namechanges.js` (349): CB, CD, Common, Compat, Engineering
- `client_scripts/tooltips.js` (853): CB, CD, Common, Compat, Equipment, Magic (Equipment)

Doubtful rows (`?`), for the owner: `startup_scripts/func/trade_helper.js`, `startup_scripts/items/decor_blocks.js`, `server_scripts/economy/trades.js`, `server_scripts/economy/wandering_trades.js`, `server_scripts/engineering/redstone.js`, `server_scripts/engineering/mechanisms.js`, `server_scripts/_lib/flower_world.js`, `server_scripts/_lib/metal_test_recipes.js`, `server_scripts/_lib/wood_test_recipes.js`, `server_scripts/_lib/sequence_assembly_processes.js`, `server_scripts/other/creative_labels.js`, `server_scripts/other/harvest.js`, `server_scripts/tags/blocktags.js`, `server_scripts/tags/entitytags.js`, `server_scripts/tags/tradetags.js`.
