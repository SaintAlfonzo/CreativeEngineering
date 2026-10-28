# Brief: engineering / dreams-and-desires

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# engineering`, Sub `dreams-and-desires`. There is no feature
writeup for Create: Dreams n' Desires (D&D, `create_dd`, `Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar`) yet; the
pack touches it in [blockrunner.md](../blockrunner.md) 3.5 (asphalt friction), [fixes_compat.md](../fixes_compat.md)
2.3 / 2.6 (drill and saw crash, axe ghosts) and [nether_worldgen.md](../nether_worldgen.md) F7 (deep layer).

All six rows cite "scratchpad 2026-10-19"; the source (git `2e8687f`, `feature_writeup/todo/change_scratchpad.md`
lines 86-95) is the whole spec: "smoked wood recipe; rubber: integrate with rubber tree mod, latex process chain to
rubber, dreams and desires sap machine on rubber logs, less efficient, rubber to belts, more efficient; rose quartz
sand blasting: 80rough/20smooth recursive split; remove extra woods? keep smoked/merge with treated?; remove
glowstone geodes, review geode spawns". Row #6 (painted asphalt) was written straight into TODO.md in `c82dd31`.

**Verified against jars** (decompiled or unzipped in the scratchpad, 2026-10-21): D&D 1.2c (recipes, tags, lang,
`DesiresOreFeaturesEntries`, `DesiresLayerPatterns`, `SandingRecipe`, `HydraulicCompactingRecipe`), Myrtrees
1.2.0 build 32 (all classes), Create 0.5.1.i (`FanProcessing`, `TransportedItemStack`, `RecipeApplier`, belt and
rose quartz recipes), Immersive Petroleum 4.2.0-29b (asphalt recipes), Supplementaries 2.4.20 (soap recipes),
Geode+ 1.1.8 (biome tags, placed features, loot tables), Terralith 2.3.12 (geode feature file names only).

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| #4 extra woods | Planned | decide which D&D woods stay, then hide / remove the rest | **first**: #1 and #2 depend on which woods survive |
| #1 smoked wood recipe | Planned | 4 smoking recipes (logs -> smoked logs) | **second**, with #4 (same file) |
| #2 rubber chain | Planned | Myrtrees latex -> D&D rubber; sap only from rubber logs; rubber belts; one new NCH helper | **third** |
| #3 rose quartz sand blasting | Planned | one D&D sanding recipe with chance outputs; one new NCH helper | any time (shares the helper work with #2) |
| #6 painted asphalt | Planned | 16 recipes out, 16 in, a soap recipe, 16 names | any time |
| #5 glowstone geodes | Planned | the glowstone half is **done** (verified below); only a geode spawn review is left, mostly decisions | last; no code until decided |

Build #2 and #3 together: both need D&D recipe types that no NCH helper covers (`create_dd:hydraulic_compacting`,
`create_dd:sanding`). Suggested new script: `kubejs/server_scripts/engineering/dreams_desires.js` for #1, #2, #3, #6
(and the recipe removals of #4); tags in a new block in `kubejs/server_scripts/tags/engineeringtags.js`.

Related rows: `worldgen/biomes/netherblocks #4` (no glowstone in the Overworld; its brief
[worldgen_biomes_netherblocks.md](worldgen_biomes_netherblocks.md) #4 already records the D&D finding),
`structures/rates-and-groups #7` (ocean geode depth, [brief](structures_rates-and-groups.md)),
`magic #2` (uses for the Geode+ shards), `gameplay/blockrunner #5` (asphalt speed, owns the speed half of #6).

Tests: no writeup prefix exists for D&D. Proposed: a new writeup `feature_writeup/dreams_desires.md` with prefix
`DD-`, written when the first row is built (then fold each section here into it and delete it from this brief).

---

## dreams-and-desires #4: Extra woods

Status Planned / Work KubeJS / data / `engineering/dreams-and-desires #4`

### Goal

Decide which of D&D's four wood families stay in play, and take the rest out cleanly (no recipes, hidden in JEI,
no generated compat variants).

### Now

- D&D registers four full wood families: `rose`, `smoked`, `rubber`, `spirit`. Each has `<w>_log`, `<w>_wood`,
  `stripped_<w>_log`, `stripped_<w>_wood`, `<w>_planks`, `_slab`, `_stairs`, `_fence`, `_fence_gate`, `_door`,
  `_trapdoor`, `_button`, `_pressure_plate` (lang `assets/create_dd/lang/en_us.json`; 13 blocks per wood).
- **No recipe produces any D&D log or wood** (verified: none of the 777 jar recipes outputs a `*_log`; no trees, no
  saplings, no worldgen). The planks, stairs etc. craft from the logs (`create_dd:<w>_planks`, `create_dd:<w>_wood`,
  ...; Create cutting `create_dd:cutting/<w>_log` strips them). So all four families are creative-only today, but
  visible in JEI (nothing in `kubejs/client_scripts/jeichanges.js` hides them).
- Generated compat variants exist for all four: Every Compat (`config/everycomp-wood_types.toml` lines 65-69:
  `[types.wood_type.create_dd] rose = true, smoked = true, rubber = true, spirit = true`; modules enabled at line 83 on:
  handcrafted, architects_palette, beautify, another_furniture, quark, decorative_blocks, farmersdelight,
  storagedrawers, twigs, lightmanscurrency, dramaticdoors, create), plus Moonlight-generated ones not controlled by
  that file: `supplementaries:create_dd/{sign_post,hanging_sign}_<w>`, `suppsquared:create_dd/item_shelf_<w>`,
  `immersive_weathering:create_dd/<w>_bark` (dict keys `i.create_dd_<w>_bark`, item_dict.js 12905-12908; composted at
  0.8 in `kubejs/startup_scripts/compost/compost_values.js` 491-494).
- D&D tags: `#create_dd:{rose,smoked,rubber,spirit}_logs`, the stripped logs in `#forge:stripped_logs`, planks etc. in
  the vanilla wooden tags, all in `#create_dd:flammable_wood`.
- Myrtrees (`myrtrees`) is the pack's rubber tree: `myrtrees:rubberwood_log`, `_planks`, `_leaves`, `_sapling`, no
  stripped log, no stairs / doors of its own (Every Compat adds variants: `[types.wood_type.myrtrees] rubberwood`).
  D&D's rubber family duplicates it.
- IE treated wood (`immersiveengineering:treated_wood_{horizontal,vertical,packaged}`, `#forge:treated_wood`) is
  planks only: IE has no treated log.
- Removal machinery: `kubejs/startup_scripts/_lib/removed_items.js` (`global.removedItems`) is hidden in JEI
  (`jeichanges.js` line 5) and stripped of item tags (`server_scripts/_lib/recipe_scan.js` line 51), and the scan at
  the top of `recipe_scan.js` prints every recipe still naming one. It does **not** remove recipes; that is done by
  hand with `R.remove(...)`.

### Build

1. Decide (below). Recommended: keep `smoked` (it gets a recipe in #1), remove `rose`, `spirit` and `rubber`.
2. `item_dict.js`, D&D section (line 1580): add literal keys for every removed id, style
   `i.create_dd_<w>_<form>` (prefixed like `i.create_dd_hazard_block`, since bare `i.rubber_planks` etc. would read as
   generic), e.g. `i.create_dd_rose_log = 'create_dd:rose_log'`. 3 woods x 13 = 39 lines; generate them once with a
   shell loop and paste, the dict has no loops. Add keys for the Moonlight variants too
   (`i.create_dd_<w>_sign_post = 'supplementaries:create_dd/sign_post_<w>'`, `_hanging_sign`, `_item_shelf`).
3. `removed_items.js`: append all of them (and `i.create_dd_<w>_bark`; also take those 3 barks out of
   `compost_values.js`).
4. Recipes: in `dreams_desires.js`, `R.bulk('remove', ...)` over `{output: id}` for every removed id, plus
   `{input: id}` (the Moonlight sign / shelf recipes take the planks). Then run the server once and check that
   `[jeiRemove]` in the log prints no recipe for these ids.
5. Every Compat: set `rose = false`, `spirit = false`, `rubber = false` under `[types.wood_type.create_dd]` in
   `config/everycomp-wood_types.toml` (it is a common config: client and server must match, so this ships through
   the normal deploy). Unverified that `false` drops the type from every module; check after a restart that
   `/dumpitemsbymod everycomp` (`server_scripts/_lib/dump_items.js`) lists no `create_dd/rose` etc.
6. If smoked stays: nothing else; if merged with treated (option B below), see #1 Decisions.

### Ids and data

- D&D: `create_dd:{rose,rubber,spirit}_{log,wood,planks,slab,stairs,fence,fence_gate,door,trapdoor,button,pressure_plate}`,
  `create_dd:stripped_{rose,rubber,spirit}_{log,wood}` (verified, lang + loot tables).
- Moonlight variants (from `stack_sizes.json` and loot ids in the repo, verified for these 12): `supplementaries:create_dd/{sign_post,hanging_sign}_{rose,rubber,smoked,spirit}`,
  `suppsquared:create_dd/item_shelf_{rose,rubber,smoked,spirit}`; Every Compat ids are generated at runtime and not
  in the repo: list them in game.

### Decisions

- **Which woods stay.** Recommendation: smoked stays (theme fits Create; #1 gives it a source), rubber goes (duplicate
  of Myrtrees; the rubber *material* items `create_dd:raw_rubber`, `rubber`, `rubber_block`, `padded_rubber` etc.
  stay, they are not wood), rose and spirit go (no source, no theme in this pack).
- **"Keep smoked or merge with treated".** A: keep smoked as its own decorative family (recommended). B: make smoked
  logs IE's missing "treated log": smoked log -> 4 treated planks, smoked planks into `#forge:treated_wood`, remove
  D&D's smoked planks set. B is worse: any `#forge:treated_wood` recipe would then accept smoked planks, which come
  from a smoker with no creosote, so it bypasses IE's creosote cost. If treated-log flavour is wanted, add it the
  other way round in #1 (a creosote route that also makes smoked logs).
- **Rubber wood alternative**: keep D&D rubber as the building set for Myrtrees rubberwood (stonecut
  `myrtrees:rubberwood_planks` <-> `create_dd:rubber_planks`). Not recommended: Every Compat already gives Myrtrees
  rubberwood its stairs / doors etc.

### Gotchas

- Every Compat and Moonlight register their variants at startup: changing the toml needs a full restart of both
  sides, and blocks of a disabled type already placed in a world turn to air. The logs were never obtainable in
  survival, so only creative builds are at risk.
- Moonlight variants (signs, shelves, bark) cannot be turned off per wood type; they stay registered and are only
  hidden and recipe-less.
- `#forge:stripped_logs` also feeds D&D's sap recipes (see #2); removing the stripped rose / spirit / rubber logs
  from tags happens automatically through `recipe_scan.js`.

### Tests

- **DD-01 Woods gone:** JEI search `@create_dd rose`, `spirit`, `rubber_log` -> nothing; `@everycomp rose` -> nothing;
  `@create_dd smoked` -> the smoked family is still there. Server log `[jeiRemove]` lists no recipe for the removed ids.

---

## dreams-and-desires #1: Smoked wood recipe

Status Planned / Work KubeJS / `engineering/dreams-and-desires #1` · depends on #4 (smoked stays)

### Goal

D&D smoked logs are obtainable: smoke ordinary logs.

### Now

- No recipe makes `create_dd:smoked_log` or any smoked wood (verified, #4). The rest of the family crafts from it:
  `create_dd:smoked_planks` (4 from `#create_dd:smoked_logs`), `create_dd:smoked_wood`, Create cutting
  `create_dd:cutting/smoked_log` -> `stripped_smoked_log`.
- Create's fan "bulk smoking" (fan through fire / campfire) runs vanilla `minecraft:smoking` recipes, so one smoking
  recipe covers the Smoker and the fan. `R.smoking(output, input, e, idOverride, xp, cookingtime)`
  (`neoCraftHelper.js` line 868) builds it; its input goes through `_itemJson`, which takes an array (one recipe for
  several logs).
- Vanilla log keys exist in the dict: `i.log_oak`, `i.wood_oak`, `i.log_oak_stripped`, `i.wood_oak_stripped`
  (item_dict.js 380-383), the same pattern for the other species.

### Build

1. `item_dict.js` D&D section: `i.smoked_log`, `i.stripped_smoked_log`, `i.smoked_wood`, `i.stripped_smoked_wood`
   (`create_dd:` ids; no clash with existing keys, `i.smoked_ham` is the only `smoked_` key).
2. `dreams_desires.js`: four recipes, one per form, each taking the seven Overworld vanilla species:
   ```js
   var sp = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove']
   R.smoking(i.smoked_log,           sp.map(s => i['log_' + s]),            e, 'smoked_log',           0.1, 200)
   R.smoking(i.stripped_smoked_log,  sp.map(s => i['log_' + s + '_stripped']), e, 'stripped_smoked_log', 0.1, 200)
   R.smoking(i.smoked_wood,          sp.map(s => i['wood_' + s]),           e, 'smoked_wood',          0.1, 200)
   R.smoking(i.stripped_smoked_wood, sp.map(s => i['wood_' + s + '_stripped']), e, 'stripped_smoked_wood', 0.1, 200)
   ```
   All 28 keys exist (`i.log_<s>`, `i.wood_<s>`, `i.log_<s>_stripped`, `i.wood_<s>_stripped`, item_dict.js from
   line 380).
3. Optional (Decision 2): a Create filling route, `R.createFilling(i.smoked_log, [sp logs, Fluid.of(f.creosote, 125)], e)`.

### Ids and data

- `create_dd:smoked_log`, `stripped_smoked_log`, `smoked_wood`, `stripped_smoked_wood` (verified).
- Fluid `immersiveengineering:creosote` = `f.creosote` (fluid_dict.js 106).

### Decisions

- **Which logs.** Recommendation: the seven vanilla Overworld species, explicit (not `#minecraft:logs_that_burn`, which
  would include smoked logs themselves, hollow logs, modded stems and the removed D&D woods).
- **Creosote route too?** Recommendation: no. One route keeps JEI simple; smoking is the name.
- **Smoking time**: 200 ticks (twice a food item) so a Smoker is not a free decor machine; the fan ignores the time.

### Gotchas

- Bulk smoking also cooks anything else on the belt; logs are not food, so nothing else on them changes.
- Charcoal is a *smelting* recipe, so the smoker and blasting / smelting do not conflict.

### Tests

- **DD-02 Smoker:** oak log in a Smoker -> smoked log; stripped birch log -> stripped smoked log.
- **DD-03 Fan:** encased fan through a campfire onto a belt of spruce logs -> smoked logs; JEI "Bulk Smoking" shows
  the four recipes.

---

## dreams-and-desires #2: Rubber chain

Status Planned / Work KubeJS / `engineering/dreams-and-desires #2` · depends on #4 (which rubber logs exist)

### Goal

Rubber comes mainly from Myrtrees' tapped latex; D&D's sap machines still work but only on rubber logs and give less;
rubber makes belts more cheaply than dried kelp.

### Now

- **Myrtrees** (verified, decompiled): one `myrtrees:filled_rubberwood_log` per tree at trunk height +1 (also in the
  pack's own copy `kubejs/data/creativeengineering/worldgen/configured_feature/wild/rubber_tree.json`, which uses
  `myrtrees:rubber_trunk_placer`, and in sapling-grown trees). It holds 10 000 latex units and never refills; a
  `myrtrees:tree_tap` on it drips 2 units every 5 ticks into a `myrtrees:wooden_bucket` under the tap (holds 2 000);
  right-clicking the bucket with >= 1 000 pops one `myrtrees:latex` item. So one tree = 10 latex, one latex per about
  2 minutes. Config is hard-coded (`MyrtreesConfig`: no toml). Latex is an item only (no fluid; the jar's `fluid.latex`
  lang key is unused) and is tagged `#forge:slimeballs` by Myrtrees.
- **D&D rubber** (verified, recipe JSON):
  - sap: `create_dd:emptying/sap_from_logs` (`create:emptying`, `#forge:stripped_logs` -> 100 mB `create_dd:sap`),
    `create_dd:emptying/sap_from_wood` (`#forge:stripped_wood`), `create_dd:hydraulic_compacting/sap_from_logs`
    (2 x `#forge:stripped_logs` -> 500 mB, heated), `.../sap_from_wood`;
  - raw rubber: `create_dd:mixing/raw_rubber` (1000 mB sap -> 1), `create_dd:hydraulic_compacting/raw_rubber_from_sap`
    (500 mB -> 2, heated);
  - rubber: `create_dd:smoking/rubber` (`minecraft:smoking`, raw rubber -> `create_dd:rubber`, 600 ticks);
  - uses: rubber blocks, padded rubber decor, and the fan sails (`create_dd:crafting/fan_catalyst/*_sail` take
    `create_dd:rubber_block`). Tags `#forge:rubbers` / `#forge:crude_rubbers` = `create_dd:rubber`,
    `#forge:raw_rubbers` = `create_dd:raw_rubber`.
  - So today any stripped log makes rubber, about one log per rubber, and Myrtrees latex has **no use at all**.
- `create_dd:hydraulic_compacting` is a Create `BasinRecipe` (`HydraulicCompactingRecipe`): the normal basin JSON
  (`ingredients` with items / fluids, `results`, `heatRequirement`). No NCH helper and no KubeJS schema for it.
- Create belt: `create:crafting/kinetics/belt_connector`, 6 `minecraft:dried_kelp` (`DDD/DDD`) -> 1
  `create:belt_connector` (`i.belt_connector`, item_dict.js 175). Not touched by the pack.
- NCH: `createEmptying(outputs, input, e, idOverride)` line 1449, `createCompacting(output, input, e, idOverride, heat)`
  line 1250, `createMixing` 1266, `smoking` 868, `shaped` 698.

### Build

1. Dicts. `item_dict.js` D&D section: `i.create_dd_raw_rubber = 'create_dd:raw_rubber'`,
   `i.create_dd_rubber = 'create_dd:rubber'`, `i.any_rubber = '#forge:rubbers'` (style of `i.any_dye_white`);
   a Myrtrees block next to `i.rubberwood_sapling` (line 13309): `i.latex = 'myrtrees:latex'`,
   `i.rubberwood_log = 'myrtrees:rubberwood_log'`, `i.rubberwood_planks`, `i.tree_tap = 'myrtrees:tree_tap'`,
   `i.myrtrees_wooden_bucket = 'myrtrees:wooden_bucket'`. `fluid_dict.js`: `f.sap = 'create_dd:sap'`.
2. NCH helper (extend `neoCraftHelper.js`, next to the Create section): 
   ```js
   ddHydraulicCompacting(outputs, inputs, e, idOverride, heat) {
     // create_dd:hydraulic_compacting is a Create BasinRecipe: same JSON as create:compacting
     var outArr = Array.isArray(outputs) ? outputs : [outputs]
     var inArr = Array.isArray(inputs) ? inputs : [inputs]
     var idSuffix = idOverride ?? global.NCH.toId(outArr[0], inArr[0], 'ddhydraulic')
     var ctx = 'ddHydraulicCompacting ' + idSuffix
     // items via _itemJson (countMode 'none', one entry per count like Create), fluids via _fluidJson,
     // results via _itemJson({countMode:'flat', includeChance:true}) / _fluidJson; see dragonBreathing (line 1128)
     e.custom({ type: 'create_dd:hydraulic_compacting', ingredients: ..., results: ...,
                heatRequirement: heat || 'none' }).id(`${global.NCH.modId}:ddhydraulic/${idSuffix}`)
   },
   ```
3. Tag (`engineeringtags.js`, through `global.tags.add`): `creativeengineering:sap_logs` = `i.rubberwood_log` (plus
   the D&D rubber logs only if #4 keeps them).
4. `dreams_desires.js`:
   - remove `create_dd:emptying/sap_from_logs`, `sap_from_wood`, `create_dd:hydraulic_compacting/sap_from_logs`,
     `sap_from_wood` (`R.bulk('remove', ...)`);
   - sap, rubber logs only, the "less efficient" route:
     `R.ddHydraulicCompacting(Fluid.of(f.sap, 250), ['#creativeengineering:sap_logs'], e, 'sap_from_rubber_log', 'heated')`;
     no Item Drain route (Decision 1);
   - keep D&D's sap -> raw rubber recipes as they are (1000 mB -> 1 mixing; 500 mB -> 2 hydraulic);
   - latex, the main route: `R.createCompacting(Item.of(i.create_dd_raw_rubber, 2), [i.latex], e, 'raw_rubber_from_latex')`
     and `R.ddHydraulicCompacting(Item.of(i.create_dd_raw_rubber, 3), [i.latex], e, 'raw_rubber_from_latex_hydraulic')`;
     a hand route too: `R.shapeless(i.create_dd_raw_rubber, [i.latex, i.latex], e)` (worse, for before Create);
   - raw rubber -> rubber stays D&D's smoking (fan smoking covers bulk);
   - belts: `R.shaped(Item.of(i.belt_connector, 2), ['RRR'], {R: i.any_rubber}, e, 'belt_connector_from_rubber')`.
5. Per tree that gives: tapping 10 latex -> 20 raw rubber (compacting) or 30 (hydraulic); pressing all ~6 logs of
   the same tree -> 1 500 mB sap -> 6 raw rubber at best. Belts: 3 rubber -> 2 belts (1.5 rubber per belt) vs
   6 kelp per belt.

### Ids and data

- Verified: `myrtrees:latex`, `rubberwood_log`, `rubberwood_planks`, `tree_tap`, `wooden_bucket`,
  `filled_rubberwood_log` (block only); `create_dd:sap` (fluid, `create_dd:sap_bucket`), `create_dd:raw_rubber`,
  `create_dd:rubber`, `create_dd:hydraulic_press` (the machine), recipe ids above.
- Unverified: whether other pack mods consume `#forge:rubbers` (search JEI uses of Rubber in game).

### Decisions

- **Item Drain sap.** Recommendation: drop it (100 mB per log for free is the "any log is rubber" hole). Keep only the
  heated hydraulic press on rubber logs.
- **Latex as a slimeball.** Myrtrees tags latex `#forge:slimeballs`, so it already crafts sticky pistons, leads,
  etc. wherever a recipe takes the tag. Recommendation: keep (fits), but note it in the writeup.
- **Numbers** above are a starting point; the row only fixes the order (latex > sap, rubber belts > kelp belts).

### Gotchas

- Myrtrees logs have no stripped form, so they never matched D&D's `#forge:stripped_logs` sap recipes: rubber trees
  only enter the sap chain through the new tag.
- A filled log broken by hand drops a plain rubberwood log (`myrtrees:blocks/filled_rubberwood_log`) and loses its
  latex; worth a tooltip on the tree tap (`client_scripts/tooltips.js`).
- `create_dd:hydraulic_compacting` needs D&D's Hydraulic Press over a heated basin; JEI shows it under D&D's
  category. Confirm the custom recipe loads (no `[NCH]` warning, JEI entry visible).
- Myrtrees' own worldgen is removed (wild_crops.json `rubber`, pack copy in 1 chunk of 4 in jungles); saplings are
  also sold by the wandering trader (`wandering_trades.js` line 28).

### Tests

- **DD-04 Latex:** tap a wild rubber tree (tap on the filled log, wooden bucket under the tap) -> latex after ~2 min;
  compact it -> 2 raw rubber; smoke -> rubber.
- **DD-05 Sap only from rubber logs:** Hydraulic Press on a heated basin with 2 stripped oak logs -> nothing; with a
  rubberwood log -> 250 mB sap. Item Drain with a stripped log -> nothing.
- **DD-06 Belts:** 3 rubber in a row -> 2 belt connectors; the kelp recipe still works.

---

## dreams-and-desires #3: Rose quartz sand blasting

Status Planned / Work KubeJS / `engineering/dreams-and-desires #3`

### Goal

D&D's sanding fan ("sand blasting") polishes rose quartz only partly per pass: 80 % stays rough, 20 % comes out
polished, so a loop of passes does the full job.

### Now

- `create_dd:sanding/rose_quartz` (`create_dd:sanding`, `create:rose_quartz` -> `create:polished_rose_quartz`, 100 %).
  The sanding fan runs through `#create_dd:fan_processing_catalysts/sanding` (`create_dd:sanding_sail`, sand, red sand).
  `SandingRecipe` is a Create `ProcessingRecipe`, 1 input, up to 12 outputs, so chance outputs work.
- Other polished rose quartz routes: Create sandpaper `create:sandpaper_polishing/rose_quartz` (by hand, 100 %, not
  touched by the pack), charging `polished_rose_quartz_charging` in `kubejs/server_scripts/engineering/redstone.js`
  line 91. Rose quartz itself: redstone.js 80-87.
- No NCH helper for D&D fan types. `dragonBreathing` (line 1128) is the template: a custom fan type with chance results
  through `_itemJson(..., {includeChance: true})`.
- How Create applies it (verified, Create 0.5.1.i): `RecipeApplier` rolls every output once **per item in the stack**,
  each output independently. `FanProcessing`: an item entity is processed once per fan type (its `Time` goes to -1 and
  stays there); extra result stacks spawn as fresh entities; on a belt the results copy `processedBy` and
  `processingTime = -1`, so nothing is processed twice by the same fan type on the same belt.

### Build

1. NCH helper `ddSanding(outputs, input, e, idOverride)`: copy `dragonBreathing`, type `create_dd:sanding`, id
   `${modId}:ddsanding/${idSuffix}`. (If D&D's seething or freezing get recipes later, generalise it to
   `ddFan(type, outputs, input, e, idOverride)`.)
2. `dreams_desires.js`:
   ```js
   R.remove({ id: 'create_dd:sanding/rose_quartz' }, e)
   R.ddSanding([Item.of(i.rose_quartz).withChance(0.8), Item.of(i.polished_rose_quartz).withChance(0.2)],
       i.rose_quartz, e, 'rose_quartz')
   ```
3. Document in the writeup how to make it "recursive": route the belt through a funnel / depot / chute and back
   (re-inserted items are new `TransportedItemStack`s and get processed again), or let thrown items fall back into
   the stream (a new entity each time).

### Ids and data

`create:rose_quartz` = `i.rose_quartz`, `create:polished_rose_quartz` = `i.polished_rose_quartz` (item_dict.js
1541-1542); recipe `create_dd:sanding/rose_quartz` (verified).

### Decisions

- **Exact 80/20 is not possible** with Create chances: the two rolls are independent, so per item 64 % rough only,
  16 % both (2 items), 4 % polished only, 16 % nothing. The expected count is 0.8 rough + 0.2 polished = 1.0 per item,
  i.e. 80/20 on average and no net loss. Recommendation: accept this. The alternative (polished 20 %, nothing else)
  destroys 80 % per pass.
- Keep sandpaper at 100 %: hand work stays the slow but sure route. Recommendation: yes.

### Gotchas

- Whether D&D's sanding type goes through Create's `RecipeApplier` (and so rolls per item) is assumed, not read from
  D&D's fan type class; DD-07 checks it.
- JEI shows the two chances per output; the "recursive" behaviour needs a line in the writeup or a tooltip.

### Tests

- **DD-07 Sanding split:** 64 rose quartz on a belt through a sanding fan -> about 51 rough and 13 polished (each
  pass); feed the rough back through a funnel loop until none is left -> all polished.

---

## dreams-and-desires #6: Painted asphalt

Status Planned / Work KubeJS / resource pack / `engineering/dreams-and-desires #6` · speed half in
`gameplay/blockrunner #5`

### Goal

D&D's 16 coloured asphalt blocks become "Painted Asphalt": made by painting Immersive Petroleum asphalt (8 asphalt +
1 dye -> 8), washed back with soap, and running like IP asphalt.

### Now

- D&D recipes (verified): `create_dd:crafting/decor/<colour>_asphalt_block`, shaped `BSB/SDS/BSB`, slime ball x4 +
  `create:scorchia` x4 + `#forge:dyes/<colour>` -> 4. `create_dd:hazard_block_from_asphalt_blocks_stonecutting`:
  `#create_dd:asphalt_blocks` -> 2 hazard blocks (keep).
- Item ids in the dict: `i.<colour>_asphalt_block` (item_dict.js 1581-1596), IP: `i.asphalt`, `i.asphalt_slab`,
  `i.asphalt_stair` (4144-4146). Dyes: `i.any_dye_<colour>` = `#forge:dyes/<colour>` (from line 1991). Soap:
  `i.soap = 'supplementaries:soap'` (1495).
- "IE asphalt" in the row is **Immersive Petroleum** asphalt (`immersivepetroleum:asphalt`, IP recipe
  `immersivepetroleum:asphalt`, 8 per craft from bitumen, sand, gravel, water). IE itself has none.
- Speed: already done in 1.0.9. `kubejs/startup_scripts/changes/block_properties.js` resets D&D asphalt friction 0.35
  -> 0.6 and speed factor 0.001 -> 1.0 (jump factor 1.25 kept), and `blocktags.js` line 125 puts
  `create_dd:*_asphalt_block` in BlockRunner's `asphalt` tier with IP asphalt (1.50). BR-28 / BR-29 untested.
- Supplementaries soap recipes are plain shapeless crafts (e.g. `supplementaries:soap/piston`: sticky piston + soap ->
  piston); its `supplementaries:soap_clearing` special recipe uses Moonlight's colour sets, which have no uncoloured
  member for D&D asphalt, so soap does nothing to it today (unverified in game).
- Names: D&D lang `block.create_dd.<colour>_asphalt_block` = "<Colour> Asphalt Block".

### Build

1. `dreams_desires.js`:
   ```js
   var colors = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
       'purple', 'blue', 'brown', 'green', 'red', 'black']
   R.bulk('remove', colors.map(c => [{ id: 'create_dd:crafting/decor/' + c + '_asphalt_block' }, e]))
   colors.forEach(c => R.shaped(Item.of(i[c + '_asphalt_block'], 8), ['AAA', 'ADA', 'AAA'],
       { A: [i.asphalt, '#create_dd:asphalt_blocks'], D: i['any_dye_' + c] }, e, c + '_painted_asphalt'))
   R.shaped(Item.of(i.asphalt, 8), ['AAA', 'ASA', 'AAA'], { A: '#create_dd:asphalt_blocks', S: i.soap }, e,
       'asphalt_from_painted_asphalt_soap')
   ```
   (Add `i.any_asphalt_painted = '#create_dd:asphalt_blocks'` to the dict instead of the literal tag.)
2. Names, `kubejs/client_scripts/namechanges.js`: 16 lines in the namespace form
   `e.add('create_dd', 'block.create_dd.<colour>_asphalt_block', '<Colour> Painted Asphalt')` (the 3-argument form,
   see the comment at the top of `name_fixes.js`: the 2-argument one lands in `minecraft` and can lose). A loop over
   `colors` with a name map is fine. That is the whole "resource pack" part unless the textures change.
3. Leave `block_properties.js` and the BlockRunner tier as they are; `gameplay/blockrunner #5` owns speed.

### Ids and data

Verified: the 16 D&D recipe ids, `#create_dd:asphalt_blocks` (16 blocks), IP asphalt recipes, soap recipe shape.

### Decisions

- **Re-dyeing**: the sketch accepts painted asphalt as the "A" ingredient too (8 red + white dye -> 8 white).
  Recommendation: yes, it costs nothing.
- **Soap ratio**: 8 painted + 1 soap -> 8 asphalt (mirrors the paint recipe). Recommendation over 1 + 1 -> 1.
- **Textures**: keep D&D's. A "painted" look (IP asphalt texture with a coloured stripe) would need new textures in
  the resource pack; not in the row's scope.
- **Slabs / stairs**: IP's slab and stair recipes take `#forge:asphalt`; painted asphalt is not in it, so painted
  asphalt has no slabs or stairs. Recommendation: leave it (adding it would turn painted asphalt into plain IP slabs).

### Gotchas

- `create:scorchia` loses its only D&D use; scorchia still has the pack's recipes (`engineering/resource_blocks.js`
  99-105).
- D&D's jump factor 1.25 stays on painted asphalt (blockrunner.md 3.5); IP asphalt jumps normally.

### Tests

- **DD-08 Paint:** 8 IP asphalt + red dye -> 8 Red Painted Asphalt; the old scorchia recipe is gone from JEI.
- **DD-09 Soap:** 8 painted (mixed colours) + soap -> 8 IP asphalt.
- **DD-10 Speed:** BR-28 (blockrunner.md): IP and painted asphalt read the same `M`.

---

## dreams-and-desires #5: Glowstone geodes

Status Planned / Work data / `engineering/dreams-and-desires #5`

### Goal

No D&D glowstone in the Overworld (done), and a deliberate set of geodes.

### Now

- **The glowstone half is done and shipped** (verified 2026-10-21). D&D has **no geode feature**: its jar ships no
  worldgen JSON, and `DesiresOreFeaturesEntries` registers exactly five Create ore features: `gabbro_blob`,
  `dolomite_blob` (standard blobs, Overworld), `striated_eroded_overworld` (layered, `DesiresLayerPatterns.SPACE_ROCK`:
  breccia / gabbro+granite / granite+gabbro / glowstone layers, Y -64 to 0, every Overworld biome),
  `striated_eroded_nether` (same pattern, Nether, Y 0 to 100) and `striated_ores_oceans` (calcite / dolomite /
  weathered limestone, oceans). The "glowstone geodes" seen in a new world were the glowstone layer of
  `striated_eroded_overworld`; it is overridden to granite only by
  `kubejs/data/create_dd/worldgen/configured_feature/striated_eroded_overworld.json` (nether_worldgen.md F7), which is
  in the 1.0.9 deploy manifest (`deploy/history/1.0.9.json` line 3625, same SHA-1 as the file now). The Nether copy
  keeps its glowstone, which is right for the Nether. Knobs: `config/create_dd-common.toml` `[worldgen.v2.*]`.
- **Geodes in the pack** (what "review geode spawns" is about):
  - vanilla amethyst geode (1 in 24 chunks).
  - Geode+ (`geode`, code-registered placed features, rarities in `config/geode.json5`, all at default since the
    initial commit): Overworld: lapis and redstone geodes in stone (Y -4 to 30) and deepslate (Y -58 to 0), 1 in 48
    chunks each; diamond in stone and deepslate, 1 in 72 each, every Overworld biome; emerald (stone + deepslate,
    1 in 72) in `#minecraft:is_mountain`; echo geode (1 in 16) and sculk diamond / emerald / lapis / redstone geodes
    in the deep dark; celestite (1 in 48) in swamps; prismarine (1 in 32, Y 30 to 60) in deep oceans. Nether:
    quartz, gold nugget, ancient debris and glowstone geodes in normal, basalt and blackstone versions (Nether biome
    tags only; nether_worldgen.md 1.1). End: wrappist.
  - Terralith: `terralith:mountains/emerald/emerald_geode`, `cave/generic/mega_geode`, `cave/crystal/*`,
    `meme/meme_geode` (file names only, not read).
- **Geode+ budding blocks drop themselves with silk touch** (loot tables `geode:blocks/budding_diamond`, and so on for
  every budding block), unlike vanilla budding amethyst. Their clusters drop 2 diamonds (fortune bonus) or 2
  netherite scrap (`nether_ancient_debris_cluster`). So a silk-touch pickaxe gives a renewable diamond, emerald, lapis,
  redstone and netherite-scrap farm. Nothing in the pack changes this (`grep geode:` in kubejs: only tags, the
  wrappist horn loot and two potion inputs).

### Build

Only after the decisions:

1. Silk-touch budding: override the budding loot tables in `kubejs/data/geode/loot_tables/blocks/` with an empty
   table (`{"type": "minecraft:block", "pools": []}`), the vanilla budding amethyst behaviour. Files:
   `budding_{diamond,emerald,lapis,redstone}`, `budding_deepslate_*`, `budding_sculk_*`, `budding_nether_*`,
   `budding_basalt_*`, `budding_blackstone_*` as decided (list in the jar's `data/geode/loot_tables/blocks/`). The pack
   already ships datapack loot overrides this way (loot.md "Pack datapack").
2. Rates: edit `config/geode.json5` `Rarity of ...` / `Enable ...`; a restart regenerates nothing in old chunks.
3. Record the geode table above in a writeup (nether_worldgen.md 1.1 has the Nether side; the Overworld side fits
   `structure_tuning.md` near the ocean geode section or the new `dreams_desires.md`).

### Decisions

- **Budding farms** (the real question). Recommendation: no silk-touch drop for budding diamond, emerald and ancient
  debris (renewable diamonds and netherite skip the pack's progression); keep it for lapis, redstone, quartz,
  glowstone, gold nugget, celestite, echo, wrappist and prismarine (cheap or decorative).
- **Rates**: Geode+ Overworld ore geodes add about one geode per 9 chunks on top of amethyst (4 x 1/48 + 2 x 1/72).
  Recommendation: leave the rates; decide again after the budding change.
- **Glowstone geodes in the Nether** (Geode+ normal / basalt / blackstone and the large glowstone crystal): Nether-only,
  keep.

### Gotchas

- Geode+ placements are not reproducible per seed (structure test log `03_phase_3.md`); compare counts, not chunks.
- The ocean depth system (`worldgen/OceanGeodes`, CEConfig `[worldgen.ocean_geodes]`) moves every vanilla-`GeodeFeature`
  geode under oceans, Geode+ included except prismarine; rate changes here change that row's numbers
  (`structures/rates-and-groups #7`).

### Tests

- **DD-11 Budding:** silk-touch a Geode+ budding diamond -> nothing drops (after the change); budding lapis -> drops.
- Glowstone: NW-04 / NW-05 in nether_worldgen.md and the census of `worldgen/biomes/netherblocks #4` already cover it.

---

## dreams-and-desires #7: Tune milkshake lava generation
Status **Planned** / Work KubeJS / data / TODO `engineering/dreams-and-desires #7`

### Goal
Tune what lava touching a milkshake generates, and fold the same results into mixing / extruding (machine recipes).

### Now
- The generation is code in D&D 1.2c (`DesiresFluids.addMilkshakeInteraction`, read with `javap` for the JEI manual rows):
  lava beside a milkshake makes a stone (flowing lava) or obsidian (a lava source) by flavour: chocolate -> scorchia,
  vanilla -> dolomite, strawberry -> cobbled deepslate, glowberry -> gabbro, pumpkin -> dripstone block; over bedrock an
  ore stone (veridium, asurine, crimsite, ochrum, breccia) at `chanceForOreStone` 25 %, over artificial bedrock (beacon,
  netherite block, dragon egg, end portal, reinforced deepslate) at `chanceForArtificialOreStone` 5 %.
- Those two numbers are in the **server config** `serverconfig/create_dd-server.toml` (per world: `saves/WorldB/serverconfig`,
  `WorldC`, `Workspace`; no `defaultconfigs/create_dd-server.toml` exists), range 1 to 100 and 0 to 100.
- The pack's machine route for ore stones exists: `kubejs/server_scripts/engineering/resource_blocks.js` has
  `R.createExtruding(i.veridium, [lava 1000, water 1000], e, 'veridium_extruding', catalyst dust block, 10)` and the same for
  crimsite and the other stones, plus compacting (250 mB lava + 2 gravel + 6 nuggets). Extruding with milkshake instead of
  water for the base stones (scorchia, dolomite, ...) is not defined: unverified (grep `milkshake` in server_scripts finds nothing).
- JEI shows the world rows in the Lava Generation tab (`jei_world/lava_generation/manual.json`, renamed by
  `jei/jei-coverage #21`).

### Build
1. Choose values (Decisions). Config: add `defaultconfigs/create_dd-server.toml` with the new chances (hand-edited, so copy it
   to `configs_edited/server_config/`), and note that existing worlds keep their own serverconfig file.
2. Folding: `R.createExtruding` / `R.createMixing` rows for each milkshake stone through `global.NCH`, with the chance
   outputs of the ore stones kept lower than the machine rows; ids from `global.item` / `global.fluid` (the milkshake fluids
   `create_dd:*_milkshake`, check the keys in fluid_dict).
3. Update the JEI manual rows and notes for the new chances.

### Decisions
The new values (the row says "values"); recommend lowering `chanceForOreStone` only if the machine route is as cheap, so the
world trick is not the best ore source. Whether the world interaction stays: it is hardcoded, so only the numbers can change.

### Gotchas
Config read per world; a pack release needs the new numbers in `world_serverconfig/` of the deploy payload (deploy.md).

### Tests (EN-)
- EN-19 (proposed): lava over bedrock beside chocolate milkshake gives scorchia and veridium at the configured share (50 trials).

---

## dreams-and-desires #8: Tune sulfuric acid lava generation
Status **Planned** / Work KubeJS / data / TODO `engineering/dreams-and-desires #8`

### Goal
Tune what lava generates against Vintage Improvements' sulfuric acid.

### Now
- Vintage Improvements 0.2.0.3 (`VintageFluids`): sulfuric acid + lava: flowing lava sets to `create:scoria`, a lava source
  to obsidian (`jei_world/lava_generation/manual.json`, requires `vintageimprovements` and `create`). The class is code; no
  option for it was found in `config/vintageimprovements-common.toml` (grep for lava / acid is empty).
- Acid fluid key `f.sulfuric_acid` (fluid_dict line 178), bucket `i.sulfuric_acid_bucket`.

### Build
Same two routes as #7: values (needs a mixin if the numbers are hardcoded: read `VintageFluids` first) and folding the result
into mixing / extruding rows through `global.NCH`. Likely only a recipe route, since no config exists.

### Decisions
Values (the row names none). Recommend recipe route only and a note in the JEI row.

### Tests (EN-)
- EN-20 (proposed): the acid + lava rows in JEI match the world result; the machine recipe gives the same block.

---

## Built, needs play

None: every row of this subfamily is Planned.

## Writeup corrections made

None. The linked writeups (nether_worldgen.md F7, blockrunner.md 3.5, mob_block_tweaks.md) match the code and the
1.0.9 deploy.

## Extra woods: overlap survey (scratchpad 2026-10-27)

For `engineering/dreams-and-desires #4`. D&D adds four woods with no tree, sapling or worldgen (logs are creative only).

- Blocks every D&D wood has (13): log, wood, stripped log, stripped wood, planks, slab, stairs, fence, fence gate, door,
  trapdoor, button, pressure plate.
- rose: no overlap by name (not rose quartz, rose gold or rose bush). Needs a visual check against Quark blossom and cherry wood.
- smoked: overlaps Immersive Weathering's charred wood (log, planks, slab, stairs, fence, gate); IE treated wood
  (planks only) is the brief's option B.
- rubber: overlaps myrtrees rubberwood, the pack's real rubber tree (log, planks, leaves, sapling, latex). Related:
  create_dd raw_rubber / rubber / rubber_block / padded rubber are not wood and stay (rubber chain is #2).
- spirit: no overlap by name. Needs a visual check against Quark ancient and the nether woods.
- Generated for each of the 4 woods (removing a wood removes these too): Every Compat af 7 (bench, chair, drawer,
  planter_box, shelf, shutter, table), ap 5 (boards, slab, stairs, wall, railing), bd 3 (blinds, picture_frame,
  trellis), c 2 (window, window_pane), db 4 (beam, palisade, seat, support), dd 2 (short and tall door), fd 1 (cabinet),
  hc 16 (bench, board, chair, corner_trim, couch, counter, cupboard, desk, dining_bench, drawer, fancy_bed, nightstand,
  pillar_trim, shelf, side_table, table), lc 4 (auction_stand, bookshelf_trader, shelf, shelf_2x2), q 8 (hollow_log,
  bookshelf, chest, ladder, post, trapped_chest, stripped_post, vertical_planks), sd 7 (drawers, trim), tw 1 (table).
- Overlap inside Every Compat itself, for every wood in the pack: bench af / hc, chair af / hc, drawer af / hc / sd,
  shelf af / hc / lc, table af / hc / tw, cabinet fd vs cupboard hc, trellis bd vs lattice.
- Other: supplementaries sign_post and hanging_sign, suppsquared item_shelf, IW bark (compost 0.8), railways tracks
  (3 widths + incomplete, 6 each).
- Recommendation (owner list): keep smoked; remove rose, spirit and rubber (rubber duplicates myrtrees).
