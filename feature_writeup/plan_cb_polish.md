# Creative Brewing publish polish plan (2026-11-06, from scratch run r1106cbo)

Follows [plan_cb_publish.md](plan_cb_publish.md) (sections 14 and 15) and the run [scratch_run_2026-11-06.md](testing/scratch_run_2026-11-06.md) 4.
Owner request 2026-11-06: write these notes as a new plan; echo tools and the effects go in Equipment, not Engineering;
fix the absent-mods print; fix the 77 "Empty array?" failures; merge food fluids into Creative Delight; all effects into
Creative Common; scan for other features in the wrong place that belong further down the chain.
Status: BUILT 2026-11-06, every section (Common, Equipment, Brewing, Delight jars in build/libs, NOT installed). Scratch
r1106cbp 2026-11-06 ([scratch_run_2026-11-06.md](testing/scratch_run_2026-11-06.md) 5): PASS 52, FAIL 2 (SQ-CBP-03
horror lasagna, TODO `#14`; SQ-CBP-08 Equipment alone, `#22`, `#23`); client rows CQ-CBP-01, CQ-CDFL-01 open. Fixes 18:29: horror lasagna guard (Common),
felsic transition items and Felsic Scrap assets into Equipment; reruns SQ-CBP-03b, -08b, -11, CQ-CBP-02 queued. Tests: scratch queue SQ-CBP (sections 1 to 3) and SQ-CDFL (section 4). TODO rows: `creativebrewing/publish #11` to `#16` (findings), `#17` to `#21` (this plan).

Chain (verified from each `mods.toml` by the scan): Common <- Compat <- Calendar <- CD <- CB. Engineering, Equipment,
Magic and Worldgen each depend on Common + Compat only, not on CD, CB or each other. No in-tree Java imports a higher
in-tree jar; the only upward links are KubeJS globals and string ids. Rule for this plan: a feature lives in the lowest
jar that has everything it references; content rows live with the jar that owns the item.

## 1. Echo tools and gear effects (Equipment)

**BUILT 2026-11-06.** Every armor and tool effect row (vanilla, Immersive Armors, Everything Copper, felsic, Dragon Loot,
echo tools) is in Equipment's `kubejs_startup/creativeequipment/gear_effects.js`; `vinery:trippy` now has a Vinery guard.
`CopperPoisoningHandler` (`effect/`, registered only with Everything Copper), `CopperPoisoningEffectMixin`,
`EchoArmorEffectProcedureMixin`, `EchoArmorTooltipMixin` are Equipment's (gated in `EquipmentMixinPlugin`;
`create_deep_dark` optional in its `mods.toml`). `[copper_poisoning]` moved to `creativeequipment-common.toml` (carried
over from CB's file, then patcherMod's). The gear JEED block (frameworks plus echo armor) is Equipment's
`equipment/gear_jeed.js`. `lava_vision.js` stays in CB (a potion-machine brew). CB's `mods.toml` lost its now unused
optional dependencies `create_deep_dark`, `everythingcopper`, `farmersrespite`, `dragonloot`.

Cause of the 4 leaks (SQ-CBO-01, -04, -10, -17, -18): the echo items are Equipment's (`creativeequipment/.../items/echo_tools.js`),
but their on-hit rows sit in CB's `kubejs_startup/creativeengineering/tool_effects.js:134-137`.

- Rows to Equipment (after the framework is in Common, section 2): echo pickaxe/axe/shovel/hoe on-hit (`tool_effects.js:134-137`),
  Dragon Loot gear rows (`:111-163`; guard dragonloot, alexsmobs, botania, supplementaries; the `vinery:trippy` row at `:129`
  needs a Vinery guard), Everything Copper set bonus (`armor_effects.js` about `:215`), felsic and dragon armor rows.
- Java to Equipment: `CopperPoisoningHandler`, `CopperPoisoningEffectMixin` (Equipment already depends on Everything Copper);
  `EchoArmorEffectProcedureMixin`, `EchoArmorTooltipMixin` (Create: Deep Dark echo set bonus; add `create_deep_dark` as an
  optional dependency in Equipment's `mods.toml` and a guard in `EquipmentMixinPlugin`).
- JEED rows for echo armor (`kubejs/data/creativebrewing/kubejs/creativebrewing/jeed_providers.js:137-139`) move with the
  rows; `kubejs/data/creativebrewing/kubejs/alexsmobs/lava_vision.js` needs a look to pick Equipment or CD.
- Echo gear is split four ways today (items in Equipment, rows in CB, mixins in CB, JEED rows in CB): one owner after this.
- Tests: rerun SQ-CBO-01 and the 22 rows that failed only on the leak; add an Equipment-only boot (Common, Compat, Equipment,
  no CB) with the `/cbo effects` probe: 0 leaks, echo rows present.

## 2. All effects into Creative Common

**BUILT 2026-11-06.** Java in Common: `effect/` (`CEPotions`, `EffectRules`, `EffectCategories`, `CompoundEffects`,
`CompoundEffectTooltip`, `MagicResistanceEffect`, `PhantomBaneRules`) and the ten effect mixins in `mixin/` (gate rows and
version ranges in `CEMixinPlugin`; `better_beacons`, `fruitsdelight`, `jeed` optional in Common's `mods.toml`).
`[effects]` and `[magic_resistance]` are in `creativecommon-common.toml` (carry-over from `creativebrewing-common.toml`,
then patcherMod's file). Scripts in Common: startup `effects/` (`armor_effects.js`, `tool_effects.js` frameworks,
`magic_resistance.js`, `potions.js` with `global.creativePotions`), server and client `kubejs/{data,assets}/creativecommon/kubejs/effects/`.
Ids: the 37 potions and the effect are `creativecommon:` (`data/creativecommon/swaps/effects_potions.json`, the
entries moved out of CB's `creative_split.json`); new `PotionByNameRemapMixin` reads any renamed potion id saved in NBT
(potions, arrows, bucket contents, potion fluids), so the old D5 gap is closed too. Brew mixes stay in CB's
`potions.js`. The `effect.creativebrewing.*` lang keys are `effect.creativecommon.*` in Common's lang.
Not done: (a) member ports (Vinery, Oceanic, Miner's, Rustic, Fungi) keep registering their own effects and potions
under their upstream namespaces; "every effect from Common" was read as the Creative-owned ones. (b) Potion JEI split
(section 5) left as it is, still undecided. (c) A player config that still names `creativebrewing:magic_resistance`
in Equipment's `[crafting]` felsic rows gives felsic recipes that ask for the old potion NBT string (the pack's own
`config/` and `configs_edited/` were updated).

Today the effect framework and almost every effect definition are in CB. Common only has `effect_dict.js` (`global.effect`).

| Piece | Now | Notes |
|---|---|---|
| `global.armorEffects`, `global.toolEffects` frameworks, tick applier, tooltip scripts (`armor_effects.js`, `tool_effects.js`, their data script and `*_tooltip.js` asset scripts in the pack's `kubejs/data|assets/creativebrewing/kubejs/creativebrewing/`) | CB | the two files share helpers and move together; priority -100 and `global.item` load order; the applier is gated on `global.CB`, the gate changes to the framework's own flag |
| `EffectRules`, `EffectCategories`, `LivingEntityEffectRemapMixin`, `MobEffectCategoryMixin`, `GuiEffectIconMixin`, `LivingEntityUseItemIconMixin`, `CreateTeaPipeIconMixin`, `FruitsDelightEffectFuncMixin` | CB, driven by `CBConfig [effects]` | `[effects]` config section moves to the Common config; carry-over shim for edited `creativebrewing-*.toml` values (as `CDConfig.java:69` did) |
| `CompoundEffects`, `CompoundEffectTooltip`, `JeedEffectInfoMixin`, `JeedEffectRendererMixin` | CB | JEED and the Vinery wine table stay optional |
| `PhantomBaneRules`, `PhantomSpawnerBaneSkipMixin`, `CaffeinatedEffectMixin` | CB | Common, or the Farmer's Respite half into CD (CD already holds the kettle mixins) |
| `MagicResistanceEffect` + `magic_resistance.js` (`creativebrewing:magic_resistance`) | CB | Common; id becomes `creativecommon:magic_resistance` (decided, remap list for saved effects, potion NBT, recipes) |
| `CEPotions` registrations (`creativebrewing:*` potions) | CB | Equipment `end_gear.js` and Engineering `dust.js` recipe against them (inverted dependencies, section 6); registrations to Common under `creativecommon:` ids (decided), brew mixes stay in CB |
| `effect_swaps.js` (`global.foodEffectSwaps`) | CB | food edits: CD (see section 4) |

Stay in CB (potion machines and alcohol, only CB, Common or CD referenced): alcohol_drinks, potion_area, potion_buckets,
tipped_arrows, potion_bottling, DragonBreathBrewing, Lingerification, PotionMixDedup, WinemakerTrades, the IE and Create
potion-fluid mixins, Intoxication tooltip. Undecided: potion JEI is split between Common (`PotionRows`, `PotionFluidTooltip`)
and CB (`PotionMachinesJEI`, `PotionPagesJEI`, `LingerificationCategory`); settle the line in one pass.

CHANGED (owner, 2026-11-07): the five Better Beacons potions (Long Reach, Phantom Bane) are registered by Creative Brewing
as `creativebrewing:` again, and the Caffeinated rule and mixin are Creative Delight's ([creative_split.md](creative_split.md) 7.19).
DECIDED (owner, 2026-11-06): (a) every potion and every mob effect registered by any in-tree Creative mod is registered from
Common (not only Magic Resistance: also effects registered in CB, CD, Equipment, Magic or Engineering; list them with a grep
of `MobEffect` / `DeferredRegister<MobEffect>` / `Potion` registers in the first pass). Content rows (which item gives which
effect) stay with the jar that owns the item (section 1). (b) Potions and effects use the `creativecommon:` namespace: rename
`creativebrewing:*` potions and `creativebrewing:magic_resistance` with a remap list for old saves (memory
`forge-missing-mappings-staging`: resolve survivors in the ACTIVE registry; test on a fresh old-id save). Update every
recipe, tag, loot and script string that names the old ids (grep `creativebrewing:`). (c) The `[effects]` config lives in the
Common config; carry-over shim for edited `creativebrewing-*.toml` values.
Tests: SQ-CBO-12, -13, -15 rerun on the chain, plus a Common + Compat + Equipment boot; world with old
`creativebrewing:magic_resistance` potions/effects loaded after any rename.

## 3. Scratch-run fixes

**BUILT 2026-11-06.** (1) `absent_mods.js` prints from a `// priority: -100000` `ServerEvents.recipes` handler.
(2) Cause of the 77 `Empty array?`: not absent `mod` clauses. kubejs-immersive-engineering resolves an IE output with no
`item` key through `IEApi.getPreferredStackbyMod`, which throws on an empty tag; IE's metal press rows for zinc and brass
(guard `forge:ingots/zinc`, output `forge:plates/zinc` etc.) have empty output tags in the cooking set, and every
output-filtered remove tests every recipe. NCH `_pruneEmptyIeOutputs` removes such IE recipes by id once per recipe
event before the first filter (one `[NCH] removed N ...` line). (3) Loot tables: new Common `LootTablesOptionalModsMixin`
honours a top-level `forge:conditions` and drops item entries of absent mods (one `[absent mods] loot tables:` line);
no JSON edits needed. Horror lasagna: `createSequencedAssembly` skips (counted) an assembly whose step has an empty
fluid and drops its steps. Compost: values for items of absent mods, or of an absent compat folder
(`immersive_weathering:<mod>/...`), are counted, not warned.

- **`[absent mods]` never prints** (`creativebrewing/publish #11`): KubeJS 1902.6.2-build.73 never posts `ServerEvents.afterRecipes`,
  so `kubejs/data/creativecommon/kubejs/_lib/absent_mods.js` never runs, full pack included. Options: (1) the same print from a
  lowest-priority `ServerEvents.recipes` handler (runs last, counters complete after all recipe scripts; tags load before
  recipes); (2) from Java in Common after the reload (`RecipeManager` apply at lowest priority, reads the NCH/tags counters
  through the KubeJS scope). Recommendation: (1), no Java, same one-line guarantee (`none skipped` when empty). Also fix the
  header comment ("tags load before recipes") and re-check the counters reset between `/reload`s. Tests SQ-CBC-02, -04,
  SQ-CBO-03, -16 (rerun).
- **77 `Empty array?`** (`#15`, SQ-CBC-02; list `perf/runs/r1106cbo/c3d/empty_array_filters.txt`). Cause not yet known; step 1 is a
  diagnosis run, then the fix:
  1. 48 filters contain `not: [..., {mod: creativeengineering}]` (Engineering absent in the cooking set). Check whether KubeJS
     throws for a `mod` clause naming an absent mod; if so `NCH._absentInFilter` strips or short-circuits `not` entries for
     absent mods (an absent mod's `not` clause is always true).
  2. 29 plain output/id filters on items of loaded CD members (Miner's, Oceans, Oceanic tentacles; Vintage jams and bottles;
     Veggies mhadjeb and stuffed peppers; Cooking for Blockheads racks; `tmted:bottling/wheat_dough`; `farmersrespite` cakes):
     boot with `logErroringRecipes=true` in `srv/local/kubejs/dev.properties` and read which id fails to resolve (item
     absent from this jar version, or KubeJS wrapping an empty tag/array).
  3. Fix in the helper (NCH), not in the 77 calls. Target: 0 `Empty array?` on the cooking set, total failures under 40.
  Tests: SQ-CBC-02 rerun; full pack SQ-CBC-04 (removed 1328 unchanged).
- **Loose ends from the run** (`#13`, `#14`, `#16`): loot tables of optional-mod blocks log `Can't find block` on the chain
  (add `forge:conditions` / `mod_loaded`, or skip per present mod); horror lasagna filling gets `minecraft:empty` (NCH skips a
  step whose fluid is absent); Common `compost.js:181` WARN for Immersive Weathering leaf piles of absent mods.

## 4. Food fluids into Creative Delight

**BUILT 2026-11-06 (Common and CD in build/libs, not installed; creative_delight.md 40.4).** Fluid ids renamed to
`creativedelight:` (`fluid_dict.js`; remap `data/creativedelight/swaps/food_fluids.json`, 66 entries with the flowing
variants); CD's startup folder is `kubejs_startup/creativedelight/`; `effect_swaps.js` in CD (Climbing swap also needs
Vinery). Soft fluids: moved into the CD jar but kept under `data/creativeengineering/moonlight/soft_fluids/` with
their ids, since Moonlight has no soft-fluid alias and a rename would empty jars in saves; their `equivalent_fluids`
name the new Forge ids. Tests SQ-CDFL-01 to -04, CQ-CDFL-01.

The scan found the registration already done: fluids are registered in CD (`creativedelight/.../creativeengineering/fluids.js:11`,
`drinks.js:36`) and tagged by CD scripts (`fluid_tags.js`, `drink_tags.js`); Engineering's `tags/fluidtags.js:14` keeps only
water, milk and oil. What is still outside CD:

- 59 Moonlight soft-fluid JSON files (soups, stews, juices) as loose pack data under
  `kubejs/data/creativeengineering/moonlight/soft_fluids/`: into the CD jar (`data/creativedelight/moonlight/soft_fluids`) or
  a namespace rename; check Moonlight's namespace handling first.
- Food fluid ids still use `creativeengineering:` (Common `fluid_dict.js:89-127`). A rename to `creativedelight:` changes fluids
  in tanks and pipes in saves: needs a remap list. DECIDED (owner, 2026-11-06): rename to `creativedelight:` with a fluid remap
  list; fix `fluid_dict.js`, the soft-fluid JSON ids, tags, recipes and scripts together.
- `effect_swaps.js` (food effect swaps) from CB to CD (small, `global.effect` is in Common).
- Common `DrinkFluidEffects.java` and `JeiFluidHelperTooltipMixin.java`: fine in Common once the effect framework is there;
  moving to CD is optional.
- Also rename the duplicate script folder: CB and CD both keep startup scripts under `kubejs_startup/creativeengineering/`.
  Rename to `creativebrewing/` and `creativedelight/` before publishing, or the two jars' files collide on merge
  (memory `forge-shared-asset-path`, `creative-mod-naming`). Check the load order effect first.
- "Merge food fluids into CD" read as the above (the registration half is already done); the id rename is decided.
Tests: SQ-CDJ rows on fluids, a CD-only boot (CEPortTesting-style, no CB) listing the fluids in JEI/`/zq`.

## 5. Other misplaced features (scan result, `Explore` agent, read-only)

**2026-11-06:** CD's Brewin' and Vinery scripts carry `// requires:` headers and `CoasterRendererOffsetMixin` sits in the
`brewinandchewin` compartment: guards fine, nothing changed. Potion JEI split and the effect ids duplicated in the
`[effects]` defaults: not changed (Java config defaults cannot read `global.effect`; the JEI split is undecided).

All findings are in sections 1 to 4 plus the following. Calendar and Compat: none. Engineering Java is machine/IE/Create code
and stays; Magic holds XP fluid tags and enchant mixins and stays. Candidates not placed yet:

- Potion JEI split Common vs CB (section 2).
- Effect ids defined both in `effect_dict.js` and as strings in `CBConfig [effects]`: fold into one place.
- CD's Brewin'/Vinery content (`kubejs/data/creativedelight/kubejs/brewinandchewin/*`, `vinery/*`, `CoasterRendererOffsetMixin`,
  `FeastPlatingCategory`) references third-party mods that are CB members; not an in-tree dependency, but verify the guards,
  since CD's `mods.toml` lists neither mod.
- Compost value scripts are spread over CD (about 35 folders) and CB (vinery, brewinandchewin); engine in Common; no duplicate
  content found.

## 6. Inverted dependencies to remove (a lower or sibling jar needs CB)

**DONE 2026-11-06:** all three. Equipment `end_gear.js` and Engineering `dust.js` name `creativecommon:` potions
(Common registers them); the echo and gear rows are Equipment's.

1. Equipment `end_gear.js:15-24, 69-78, 119-141` uses `creativebrewing:darkness`, `:magic_resistance`, `:long_resistance`; Equipment
   cannot run without CB. Fixed by moving the potion registrations to Common (section 2).
2. Engineering `engineering/dust.js:85-89` uses `creativebrewing:resistance` (powdered obsidian bottling and filling). Same fix.
3. Equipment `echo_tools.js:43` and the whole `tool_effects.js` (section 1).

## 7. Order

1. Section 3 fixes first (small, independent: absent-mods print, Empty array diagnosis and NCH fix, loot-table conditions).
2. Effect framework + rules + mixins + config into Common (section 2), (ids decided: `creativecommon:`).
3. Rows, Java and JEED rows into Equipment (section 1).
4. Food fluid leftovers and `effect_swaps.js` into CD; rename the duplicate startup folder (section 4).
5. Build all jars (Common first), copy to `CEPortTesting/mods/`, rerun r1106cbo's batches (`tools/scratch_query/batches/r1106cbo/`
   with the row corrections from the run log), add boots: Common + Compat + Equipment alone, CD alone.
6. Player changelog bullet only if a player can notice something (an id rename with a remap, or a gear effect change); moves
   alone get none. Update `creative_brewing.md`, `creative_equipment.md`, `creative_delight.md`, `creative_engineering.md`
   writeups and `mod_sides.md` if a jar's side changes.

## 8. What is left in Creative Engineering (scan 2026-11-06 evening, `Explore` agent, read-only; nothing moved yet)

Done the same evening: end gear and air brake transition rows, felsic ore data, echo / air brake / shield textures to
Equipment (TODO `creativebrewing/publish #22`); backpack skin guard (`#23`).

Stays (engineering): sawmill blades (IE), dragon breathing fan and Create `exclusive_results`, IE and Immersive Posts
fixes, Dreams & Desires crash fixes; components, coils, coil blocks, dust piles, plate blocks, grits, wires, wood
pellets; server scripts `engineering/*` (blueprints, components, Create utilities, mechanisms, pneumatic, power,
process, redstone, transport, laser cutting, D&D, computing, resource blocks, dust, end, vibrating, crystal, oil,
mob_resources, music), `metal/*`, `wood/*`, the engineering / metal / fluid / storage block tags.

Not engineering, candidates:

| Content | Where now | Proposed home |
|---|---|---|
| XP fluid bridge (5 mixins + FluidBridge) | Engineering Java | Magic (XP fluid) |
| Walk speeds on blocks: BlockRunner, framed / copycat camo, sinking blocks, WTHIT line (`surface/`, 14 files + mixins) | Engineering Java | Compat (third-party glue; LeafPileSlow already reads Compat's config) |
| Ore Excavation undo and auto-pickup (`excavation/`, 5 mixins) | Engineering Java | Compat (or Equipment) |
| Wooden bucket (`bucket/`) | Engineering Java | Equipment |
| TNT shrapnel (`shrapnel/`) | Engineering Java | unclear (stays, or Compat beside item physics, which calls it) |
| `arrow_fletching` item | `items/misc_items.js` | Equipment (only `archery.js` uses it) |
| Bandage recipes | `engineering/health.js` | Equipment |
| Hemp fabric for wool | `engineering/fabric.js` | unclear (a Common swap) |
| Animal-fat torch | `wood/ash.js` | Delight (minor) |
| Seed milling, mulch tags | `wood/compost.js`, `wood_tags.js` | unclear (Delight) |
| Vinery barrel tag | `wood_tags.js` | Brewing |
| `prepared_flesh` / block | `items/misc_items.js` | unclear (creosote chain; stays) |

Pack-level files in the `creativeengineering` namespace, in no jar: paintings (31, Compat), worldgen sky islands,
waystone ruins, driftwood, deadwood, Scarlet mountains, Moobloom / azalea biome modifiers (Worldgen), wild mushrooms,
cave carrots, ghost peppers (Delight or Worldgen), `seasoned/*` and `culture_*` models (Delight), `arrow_fletching.png`
(Equipment), `entity/canned_golem` (unclear). Engineering's own blocks (dust piles, plate blocks, coil blocks, prepared
flesh block) have their blockstates and models only in the pack's `kubejs/assets/`, not in the jar: the jar alone shows
them untextured. `creativeengineering:layered_obsidian` is registered from `kubejs/startup_scripts/`, not a jar.

## 9. Engineering carve, done 2026-11-06 evening (owner decisions on section 8)

Built 18:51-18:54, not installed (TODO `creativeengineering/carve #1`, tests SQ-ENGM). Owner targets: XP to Magic;
BlockRunner walk speeds, Ore Excavation, TNT shrapnel to Compat; wooden bucket, arrow fletching to Equipment; Vinery tags
to Brewing; animal-fat torch, seed milling, mulch to Delight; prepared flesh and hemp-for-wool stay in Engineering;
paintings to Compat (NOT done: vanilla Mojang art, kept pack-only by the licence audit, owner to confirm); sky islands,
waystone ruins, driftwood, deadwood, Scarlet mountains, Moobloom, azalea, wild mushrooms, cave carrots, ghost peppers to
Worldgen (optional-mod files in `packs/<modid>/`, `compat/ModDataPacks`); seasoned and culture models to Delight; all
Engineering assets into its jar (229 files). Generators repointed: `gen_nether_worldgen.py` writes into the Worldgen
jar's tree; `gen_crop_worldgen_table.py` also reads `packs/<modid>/data/` inside jars.
