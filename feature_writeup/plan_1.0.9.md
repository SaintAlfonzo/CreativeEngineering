# 1.0.9: bug fixes from the old TODO section 3

**DEPLOYED 2026-10-19 15:54 (server + clients)**, update from 1.0.8 (35 files, 5.5 MB; the entry in
[deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md)). Everything built after the 1.0.8 staging (2026-10-19 10:06). The fix
checks still to run in game are TODO `pack/release #4`.

## 0. At a glance

Kind: fix, mods, tooling. Evidence: patcherMod compiles and its reobfuscated jar was checked with `javap` (section 2);
KubeJS files pass `node --check`; NW, BF, QS and EF also ran on scratch servers. Nothing has run in game.

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| FX | Bug fixes from the old [TODO.md](todo/TODO.md) section 3 that had a single fix (section 1) | fix | DEPLOYED (built 2026-10-19, patcherMod 1.0.9); not run in game | per row in 1 |
| RS | Rain Should Extinguish Campfires (`rsec`) removed: Unlit Campfire already puts campfires out in rain | mods | DONE 2026-10-19: jar in `disabledmods/`, `mod_sides.md` row deleted, `server_mods/` and `pack/` regenerated | [feature_porting.md](feature_porting.md) 2.2 |
| DP | `build_deploy.py` preflight error when `mods/` holds two jars with one mod id | tooling | DEPLOYED (built 2026-10-19); checked against the current `mods/` (no error) and a faked clash (error) | [deploy.md](deploy.md) 4 |
| NW | Nether blocks in Overworld generation, first decisions: Scarlet Mountains netherrack -> jasper 90 / crimsite 10, its crimson nylium and roots gone; Moonlight warped nylium -> grass, warped roots and sprouts gone, its blue flower patch generating; Mirage Isles trunks stripped acacia; D&D deep layered rock granite; bunker warts removed; red roof netherrack -> magma; soul fire -> fire and glowstone -> yellow corundum in every structure | worldgen (data) | DEPLOYED (built 2026-10-19: `gen_nether_worldgen.py`, `gen_wild_flowers.py`, swaps regenerated: 1084); verified on scratch servers; new chunks only; NW-01 to NW-11 pending | [nether_worldgen.md](nether_worldgen.md) |
| BF | Rarest Overworld biomes lifted to 0.15 % (37 surface + 4 caves to 1.2 %), the 9 never-spawning surface biomes given `target_pct` (all generate now), paid by the 12 largest land biomes (x0.736); Overworld samples at 20,000 columns + `/dumpclimatecaves`; `/dumpclimate` default 20,000 | worldgen (patcherMod `BiomeLayoutTuner` + data) | DEPLOYED (built 2026-10-19, patcherMod 1.0.9), solved and checked on scratch servers; new worlds only; WG-09 to WG-13 pending | [biome_tuning.md](biome_tuning.md) 2.1 |
| QS | Quark's Myalite viaduct teleport hook skipped outside the End: on every `/tp`, pearl or chorus teleport it read blocks at the target and generated an unexplored target chunk on the server thread (one of the 10-19 watchdog kills) | patcherMod `mixin/QuarkSpiralSpiresTeleportMixin` | DEPLOYED (built 2026-10-19); scratch-tested 2026-10-19 (ST-205: Overworld `/tp` to new terrain 4 ms, no chunk made; End still runs the hook, 38 s stall) | [structure_tuning.md](structure_tuning.md) 7.5 |
| EF | Earth fill footprint water share (`maxWaterSurplus` 0.5), Campsite camp plinths fixed (sunk-start beard, start height -4), `avoidCoasts` switch for metas (off) | worldgen (patcherMod `EarthFill`, `TerrainAdaptation`, `MetaStructure` + data) | DEPLOYED (built 2026-10-19, patcherMod 1.0.9); scratch-tested ST-199 to ST-204; new chunks only | [structure_tuning.md](structure_tuning.md) 7.5 |

## 1. The fixes

| Bug | Fix | Where | Writeup |
|---|---|---|---|
| Oven JEI [+] fails ("cannot pickup from" slot 2) | Ingredient slots answer `mayPickup = true`. `SlotItemHandler.mayPickup` is a simulated extract, false for any empty slot, and JEI 11.8.1 checks every crafting slot with it | patcherMod `oven/OvenMenu` | [oven.md](oven.md), 1.0.8 2.2 |
| NEA's three removed enchantments still roll from loot | `isDiscoverable` renamed to its SRG name `m_6592_` (the jar ships no refmap). Confirmed first: the 1.0.8 jar had the Mojmap name, which nothing calls at runtime | patcherMod `Cycle/Stockpile/RecoveryEnchantmentMixin` | [archery.md](archery.md) F6 |
| Felsic shield has no config section | `CRAFTING.FELSIC_SHIELD` added (250 mB, 1 loop, 8 blows, as hardcoded before). Forge adds `[crafting.felsic_shield]` to the toml on the next start | patcherMod `CEConfig` | [gear_tuning.md](gear_tuning.md) F1 |
| Air brake `useDurationTicks` never applied | Read on each use instead of at script load. Confirmed first: Forge 43's `ConfigValue.get()` returns the default in production before configs load, and startup scripts run before that | `startup_scripts/items/air_brake.js`, CEConfig comment | [gear_tuning.md](gear_tuning.md) F7 |
| Shield `NoClassDefFoundError` on `DamageFlag` | `DamageFlag`, `DamageTags`, `DamageTagSnapshot` pre-loaded in `commonSetup` with the other `forceLoad` classes | patcherMod `CreativeEngineering` | [shields.md](shields.md) |
| `ItemPhysicSplit$1` `NoClassDefFoundError` | `burnDecision` uses an if-chain; the jar no longer has the synthetic `$1` class | patcherMod `itemphysic/ItemPhysicSplit` | [item_physics.md](item_physics.md) F1 |
| Potion bucket tooltip durations ignore `effectMult` | Tooltip shows the applied duration (x 1.1 splash, x 1.2 lingering) through `global.potionArea.scaled`, the same function the rings use | `client_scripts/potion_buckets.js`, `startup_scripts/func/potion_area.js` | [potion_buckets.md](potion_buckets.md) F2 |
| Tipped arrow comment claims machine cost = grid cost | Comment rewritten: the different costs are intended (grid 31.25, Spout 25, Bottler 20 mB an arrow) | `server_scripts/equipment/tipped_arrows.js` | [potion_buckets.md](potion_buckets.md) F1 |
| Iron armor set registered twice | One `set()` call deleted | `startup_scripts/func/armor_effects.js` | [armor_effects.md](armor_effects.md) F1 |
| `elytra_chestplates` hand-written `e.add` | `global.tags.add(e, ..., [i.upgraded_dragon_chestplate])` | `server_scripts/tags/equipmenttags.js` | [gear_tuning.md](gear_tuning.md) F3 |
| item_dict `i.catfish_slice` / `i.bacon` | `i.catfish_slice` = `alexsdelight:raw_catfish_slice`; `i.bacon` = raw `farmersdelight:bacon`. `cooking.js` loses its two local overrides and its bacon-and-eggs recipes use `i.cooked_bacon` | `startup_scripts/_lib/item_dict.js`, `server_scripts/cooking/cooking.js` | [food.md](food.md) 4.2 finding 6 |
| Zinc still hammer-crushable | IE's `hammercrushing_zinc` and `raw_hammercrushing_zinc` removed with the other metals' | `server_scripts/metal/metal_base_materials.js` | [recipes_metal.md](recipes_metal.md) F3 |
| 9 copper plates -> copper plate block missing | Added with the other metals' plate-block recipes | same | [recipes_metal.md](recipes_metal.md) F2 |
| `console.log('EVENT:', e)` every reload | Deleted from both base metal files | `metal_base_materials.js`, `misc_metal_crafts.js` | [recipes_metal.md](recipes_metal.md) F7 |
| Planks have no IE crusher recipe | `sawdust.js` crushes `i.any_planks` (was a second `i.any_log`) | `server_scripts/wood/sawdust.js` | [recipes_wood.md](recipes_wood.md) F1 |

Checked and left alone, because the bug is no longer in the ports:

- **Brewin' keg extra bottle:** the port's `processFermenting` gives one remainder per ingredient extracted, and its
  fluid in/out slots give one container per item poured. The 2.0 code that grew the stack by the result count is gone.
- **Vinery JEI 1800 ticks:** the number is only in patcherMod's own Vinery pages, which stand down when the port is
  loaded. The port's page shows no tick count; its barrel runs `totalFermentationTime` (6000 in the pack config).

## 2. Checks

- `./gradlew :patcherMod:build` compiles; `installToMods` put `creativeengineering-1.0.9.jar` in `mods/`.
- `javap` on the 1.0.9 jar: `CycleEnchantmentMixin` (and the other two) declare `m_6592_`, `OvenMenu$1` and `$3`
  override `m_8010_` (mayPickup), and there is no `ItemPhysicSplit$1.class`.
- `node --check` passes on every edited KubeJS file.

Tests to run in game: OV-11 (JEI [+] on the baking page, no log spam), AR (an `enchant_randomly` chest never gives
Cycle, Stockpile or Recovery), GR-28 (change `useDurationTicks`, reload), PB tooltip on a splash and a lingering
bucket, RW-03 (planks in the IE crusher), RM (zinc ore with the hammer gives nothing; 9 copper plates -> block).
