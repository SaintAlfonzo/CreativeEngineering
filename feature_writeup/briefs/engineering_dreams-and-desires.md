# Brief: engineering / dreams-and-desires

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# engineering`, Sub `dreams-and-desires`; rechecked 2026-10-31 (rows #9
and #10 added; the built rows #1, #3, #4 condensed into Built, needs play). There is no feature writeup for Create: Dreams
n' Desires (D&D, `create_dd`, `Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar`, MIT) yet; the pack's D&D work is written up
in [recipes_wood.md](../recipes_wood.md) (2.6 D&D woods, smoked wood RW-23 to RW-28), [engineering.md](../engineering.md) 3
(exclusive outputs, rose quartz sanding, EN-19 to EN-21), [blockrunner.md](../blockrunner.md) 3.5 (asphalt friction),
[fixes_compat.md](../fixes_compat.md) 2.3 / 2.6 (drill and saw crash, axe ghosts) and
[nether_worldgen.md](../nether_worldgen.md) F7 (deep layer). D&D code is in
`kubejs/server_scripts/engineering/dreams_desires.js` (painted asphalt), `wood/smoked_wood.js` + `tags/wood_tags.js`
(smoked wood), `startup_scripts/_lib/removed_woods.js` (removed woods), `engineering/redstone.js` (rose quartz sanding).

Rows #1 to #6 cite "scratchpad 2026-10-19" (git `2e8687f`, `feature_writeup/todo/change_scratchpad.md` lines 86-95):
"smoked wood recipe; rubber: integrate with rubber tree mod, latex process chain to rubber, dreams and desires sap machine
on rubber logs, less efficient, rubber to belts, more efficient; rose quartz sand blasting: 80rough/20smooth recursive
split; remove extra woods? keep smoked/merge with treated?; remove glowstone geodes, review geode spawns". #7, #8 from
scratchpad 2026-10-24, #9 from scratchpad 2026-10-31, #10 from scratch run r1031.

**Verified against jars** (decompiled or unzipped in the scratchpad, 2026-10-21): D&D 1.2c (recipes, tags, lang,
`DesiresOreFeaturesEntries`, `DesiresLayerPatterns`, `SandingRecipe`, `HydraulicCompactingRecipe`), Myrtrees
1.2.0 build 32 (all classes; All Rights Reserved), Create 0.5.1.i, Immersive Petroleum 4.2.0-29b, Supplementaries 2.4.20,
Geode+ 1.1.8, Terralith 2.3.12 (geode feature file names only). Rechecked 2026-10-31: the same jars are in `mods/`.

Shipped: #1 smoked wood, #3 rose quartz sanding, #4 removed woods and #6 painted asphalt are in the 1.1.05 changelog
(`kubejs/assets/creativeengineering/changelog/1.1.05.md` lines 45-61, 114, 169; server on 1.1.05 since 2026-10-31 12:52,
`deploy/DEPLOYED.json`).

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| #10 smokable shape tags too wide | Bug | move the Botania / driftwood exclusions to every shape tag; owner call on crimson and warped | **first**: one line in `wood_tags.js`, `/reload` |
| #9 bark cleanup | Planned | the D&D barks are already merged (2026-10-31); left: a texture for Myrtrees' rubberwood bark | small, resource pack only |
| #2 rubber chain | Planned | Myrtrees latex -> D&D rubber; sap only from Myrtrees rubber logs; rubber belts; one new NCH helper | after #10 |
| #7 milkshake lava generation | Planned | values + machine rows | decisions first |
| #8 sulfuric acid lava generation | Planned | values + machine rows | with #7 |
| #5 geode spawn review | Planned | Geode+ budding drops and rates, mostly decisions | last; no code until decided |
| #6 painted asphalt | Testing | built 2026-10-30, shipped 1.1.05; section kept below until a D&D writeup exists | done |
| #1, #3, #4 | Testing | see Built, needs play | done |

`dreams_desires.js` stays the D&D script for #2, #7 (and the recipe half of #8); tags in
`kubejs/server_scripts/tags/engineeringtags.js` (or `wood_tags.js` for wood).

Related rows: `worldgen/biomes/netherblocks #4` (no glowstone in the Overworld; [brief](worldgen_biomes_netherblocks.md) #4),
`structures/rates-and-groups #7` (ocean geode depth, [brief](structures_rates-and-groups.md)), `magic #2` (Geode+ shards),
`gameplay/blockrunner #5` (asphalt speed, owns the speed half of #6).

Tests: no writeup prefix exists for D&D. Proposed: a new writeup `feature_writeup/dreams_desires.md` with prefix `DD-`,
written with the next D&D build (fold the #6 section and the decided parts of #2 / #5 into it). Rows already written up use
their writeup's prefix (RW-, EN-).

---

## dreams-and-desires #10: Smokable shape tags too wide

Status Bug / Work KubeJS / `engineering/dreams-and-desires #10` · breaks `#1` (SQ-RW-23 FAIL)

### Goal

Only the woods meant to smoke get smoking recipes: no Botania magic woods and no driftwood in any of the 13 smoked shapes.

### Now

- `kubejs/server_scripts/tags/wood_tags.js` lines 35-52 build the 13 `#creativeengineering:smokable/<shape>` tags with
  `global.tags.addMatching` from the vanilla / forge wood tags. `smokeExcept` (line 39) = `*:*smoked*`, `*:*polished*`,
  `*:*vertical*`. Botania (`botania:*`) and driftwood (`*:driftwood`, `*:driftwood_log`) are excluded only in the log
  (line 40) and wood (41) tags; the stripped, planks, slab, stairs, fence, fence gate, door, trapdoor, button and pressure
  plate tags (42-52) use `smokeExcept` alone.
- `wood/smoked_wood.js` turns each tag into one smoking recipe (200 ticks; smoker, campfire, Create fan with a Smoking Sail).
- Scratch r1031 ([scratch_run_2026-10-31.md](../testing/scratch_run_2026-10-31.md) lines 246, 273, 421): `smokable/planks`
  holds 9 Botania planks and `upgrade_aquatic:driftwood_planks`; RW-23b: 13 recipes, Botania, driftwood, crimson and warped
  inputs in most shapes.
- Crimson and warped are in on purpose (the log tag takes `*_stem`; the wood tag drops stems, keeps hyphae), and RW-23
  ([recipes_wood.md](../recipes_wood.md) line 230) expects crimson to smoke.
- Shipped in 1.1.05 (changelog "Smoked Wood": "from any wood type").

### Build

1. `wood_tags.js` line 39: `var smokeExcept = ['*:*smoked*', '*:*polished*', '*:*vertical*', 'botania:*', '*:driftwood*']`;
   drop `'botania:*'` and the driftwood patterns from the extra lists on lines 40 and 41 (they keep `*stripped*`, `_wood`,
   `_hyphae`, `_log`, `_stem`, `_beam`).
2. Only if the owner excludes the nether woods: add `'#minecraft:non_flammable_wood'` to `smokeExcept` (`except` takes
   `#tags`, tag_helper.js line 37; covers crimson, warped and modded nether woods) and change RW-23's crimson case.
3. `/reload`; rerun SQ-RW-23 and SQ-RW-23b. Player changelog: a **Fixed** bullet under "Smoked Wood" in `unreleased.md`
   (1.1.05 is deployed), past tense, e.g. Botania woods and driftwood no longer smoke into Smoked wood.

### Decisions

- **Crimson and warped** (TODO decision): keep (recommended; RW-23 as written, and smoking a stem is no stranger than a log)
  or exclude through `#minecraft:non_flammable_wood`.
- Other woods to keep out (Twisted, Ancient, Azalea, Bamboo...): recommend none; the row names only Botania and driftwood.

### Gotchas

- `addMatching` with `except` expands nested `#tag` entries to their members as the script runs, so woods added to the
  vanilla tags later (by another tag script) are not filtered; keep `wood_tags.js` late in load order as it is.
- Upgrade Aquatic river wood was merged away (removed_woods.js `river`); only driftwood remains of that mod's woods.

### Tests

- SQ-RW-23, SQ-RW-23b (rerun): no `botania:` or driftwood input in any smoked-shape recipe; crimson per the decision.
- RW-23 in game (recipes_wood.md).

---

## dreams-and-desires #9: Bark cleanup for removed woods

Status Planned / Work KubeJS / data / `engineering/dreams-and-desires #9`

### Goal

No Immersive Weathering bark is left for the D&D woods removed in #4, and no bark item is shown without a texture.

### Now

- **The removal is done** (2026-10-31, with #4): `startup_scripts/_lib/removed_woods.js` merges `i.create_dd_rose_bark`
  (line 188), `i.create_dd_rubber_bark` (268) and `i.create_dd_spirit_bark` (348) into `i.bark_oak` (hidden, tags stripped,
  old items turn into oak bark; 1.1.05 changelog line 114 "... and bark"). Smoked bark (`i.create_dd_smoked_bark`) stays with
  smoked wood.
- **The textureless bark is Myrtrees', not D&D's**: the log scan
  ([log_warnings_lists.md](../log_warnings_lists.md) line 1019, "Texture file not found") lists
  `immersive_weathering:textures/item/myrtrees/rubberwood_bark.png` (item `i.myrtrees_rubberwood_bark`, item_dict.js line
  13288), next to `myrtrees:textures/block/rubberwood_log_side.png` (line 1007). Myrtrees ships `rubberwood_log.png` and
  `_log_top.png` but no `_log_side`, so the generated bark has no source. Removing D&D rubber does not fix it; Myrtrees
  rubberwood stays (it is #2's rubber tree).
- Leftover: CD startup `immersive_weathering/compost_values.js` lines 68, 69, 71 still give the removed rose, rubber and
  spirit barks a compost value (line 70, smoked bark, is right).
- Licences: Myrtrees is All Rights Reserved (no copying its art); D&D is MIT (its
  `textures/block/palettes/wood_types/rubber/rubber_log.png` can be used with a notice).

### Build

1. Texture: make `kubejs/assets/immersive_weathering/textures/item/myrtrees/rubberwood_bark.png` from D&D's rubber log side
   (the TODO's "give it the Dreams and Desires texture"), in the shape of the other IW bark items (`textureswapper/`
   palette swap of an existing IW bark item onto the D&D rubber log palette); MIT notice beside it.
2. Drop the rose, rubber and spirit bark rows (lines 68, 69, 71) from CD `immersive_weathering/compost_values.js`; ships with
   the next CD build.
3. Rerun `tools/log_warnings_scan.py` after the next launch: the rubberwood bark line is gone. Changelog bullet only if the
   texture counts as player-visible (it does: **Fixed:** Rubberwood Bark had no texture).

### Decisions

None open (TODO: none). Optional: also supply `myrtrees:textures/block/rubberwood_log_side.png` from the same D&D source if
the log warning matters (not needed for the bark).

### Tests

- RW-new1 (proposed, numbered when queued): Rubberwood Bark shows a bark texture in JEI and in hand; no
  `rubberwood_bark.png` line in `latest.log`.
- RW-28 (existing) already checks that no rose, rubber or spirit bark is in JEI.

---

## dreams-and-desires #2: Rubber chain

Status Planned / Work KubeJS / `engineering/dreams-and-desires #2` · #4 settled 2026-10-31: D&D rubber wood is merged into oak, so Myrtrees rubberwood is the only rubber log. Nothing built (2026-10-31: no latex, sap or rubber key in item_dict.js, no recipe in `dreams_desires.js`)

### Goal

Rubber comes mainly from Myrtrees' tapped latex; D&D's sap machines still work but only on rubber logs and give less;
rubber makes belts more cheaply than dried kelp.

### Now

- **Myrtrees** (verified, decompiled): one `myrtrees:filled_rubberwood_log` per tree at trunk height +1 (also in the
  pack's own copy `kubejs/data/creative_delight/worldgen/configured_feature/wild/rubber_tree.json`, which uses
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
  `create:belt_connector` (`i.belt_connector`, item_dict.js 224). Not touched by the pack.
- NCH (line numbers 2026-10-31): `createEmptying(outputs, input, e, idOverride)` line 1848, `createCompacting(output, input, e,
  idOverride, heat)` 1649, `createMixing` 1665, `smoking` 1016, `shaped` 798; `ddSanding` 1377 (built for #3) is the
  pattern for a D&D recipe-type helper.

### Build

1. Dicts. `item_dict.js` D&D section: `i.create_dd_raw_rubber = 'create_dd:raw_rubber'`,
   `i.create_dd_rubber = 'create_dd:rubber'`, `i.any_rubber = '#forge:rubbers'` (style of `i.any_dye_white`);
   a Myrtrees block next to `i.rubberwood_sapling` (line 13733): `i.latex = 'myrtrees:latex'`,
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
     // results via _itemJson({countMode:'flat', includeChance:true}) / _fluidJson; see ddSanding (line 1377) and dragonBreathing (1341)
     e.custom({ type: 'create_dd:hydraulic_compacting', ingredients: ..., results: ...,
                heatRequirement: heat || 'none' }).id(`${global.NCH.modId}:ddhydraulic/${idSuffix}`)
   },
   ```
3. Tag (`engineeringtags.js`, through `global.tags.add`): `creativeengineering:sap_logs` = `i.rubberwood_log` (the D&D
   rubber logs are merged into oak since 2026-10-31, so nothing else).
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
- Myrtrees' own worldgen is removed (CD `kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.json` `rubber`, line 338;
  the pack's copy `creative_delight:wild/rubber_tree` places it in jungles); saplings are
  also sold by the wandering trader (`wandering_trades.js` line 28).

### Tests

- **DD-04 Latex:** tap a wild rubber tree (tap on the filled log, wooden bucket under the tap) -> latex after ~2 min;
  compact it -> 2 raw rubber; smoke -> rubber.
- **DD-05 Sap only from rubber logs:** Hydraulic Press on a heated basin with 2 stripped oak logs -> nothing; with a
  rubberwood log -> 250 mB sap. Item Drain with a stripped log -> nothing.
- **DD-06 Belts:** 3 rubber in a row -> 2 belt connectors; the kelp recipe still works.

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
- Those two numbers are in the **server config** `serverconfig/create_dd-server.toml` (per world: `saves/<world>/serverconfig/`
  in New World, Workspace and WorldA to WorldG, 25 / 5 where read; no `defaultconfigs/create_dd-server.toml` exists, checked
  2026-10-31), range 1 to 100 and 0 to 100.
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
   outputs of the ore stones kept lower than the machine rows; ids from `global.item` / `global.fluid` (the five
   fluids `create_dd:{chocolate,vanilla,strawberry,glowberry,pumpkin}_milkshake` have no fluid_dict.js keys yet: add them).
3. Update the JEI manual rows and notes for the new chances.

### Decisions
The new values (the row says "values"); recommend lowering `chanceForOreStone` only if the machine route is as cheap, so the
world trick is not the best ore source. Whether the world interaction stays: it is hardcoded, so only the numbers can change.

### Gotchas
Config read per world; a pack release needs the new numbers in `world_serverconfig/` of the deploy payload (deploy.md).

### Tests (EN-)
- EN-new1 (proposed, numbered when queued; EN-19 is now rose quartz sanding): lava over bedrock beside chocolate milkshake gives scorchia and veridium at the configured share (50 trials).

---

## dreams-and-desires #8: Tune sulfuric acid lava generation
Status **Planned** / Work KubeJS / data / TODO `engineering/dreams-and-desires #8`

### Goal
Tune what lava generates against Vintage Improvements' sulfuric acid.

### Now
- Vintage Improvements 0.2.0.3 (`VintageFluids`): sulfuric acid + lava: flowing lava sets to `create:scoria`, a lava source
  to obsidian (`jei_world/lava_generation/manual.json`, requires `vintageimprovements` and `create`). The class is code; no
  option for it was found in `config/vintageimprovements-common.toml` (grep for lava / acid is empty).
- Acid fluid key `f.sulfuric_acid` (fluid_dict.js line 202), bucket `i.sulfuric_acid_bucket`.

### Build
Same two routes as #7: values (needs a mixin if the numbers are hardcoded: read `VintageFluids` first) and folding the result
into mixing / extruding rows through `global.NCH`. Likely only a recipe route, since no config exists.

### Decisions
Values (the row names none). Recommend recipe route only and a note in the JEI row.

### Tests (EN-)
- EN-new2 (proposed, numbered when queued; EN-20 is taken): the acid + lava rows in JEI match the world result; the machine recipe gives the same block.

---

## dreams-and-desires #5: Geode spawn review

Status Planned / Work data / `engineering/dreams-and-desires #5` (rechecked 2026-10-31: `config/geode.json5` unchanged since the
initial commit, no `kubejs/data/geode/` overrides, the `create_dd` striated override still in place)

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

## dreams-and-desires #6: Painted asphalt

Status Testing (built 2026-10-30, `engineering/dreams_desires.js`, SQ-DD-08 scratch server PASSED r1031 2026-10-31; shipped 1.1.05) / Work KubeJS / resource pack / `engineering/dreams-and-desires #6` · speed half in
`gameplay/blockrunner #5`

Built 2026-10-30 as planned (`dreams_desires.js` lines 7-19, with dict key `i.t_painted_asphalt` for
`#create_dd:asphalt_blocks`; 16 names by a loop in `namechanges.js` line 345; right-clicking with soap still gives white
painted asphalt, Supplementaries' colour set). Fold into a D&D writeup (`dreams_desires.md`, DD-08 to DD-10) when one is
written, then delete this section; the plan below is kept as written.

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

## Built, needs play

- `engineering/dreams-and-desires #1` Smoked wood recipe: [recipes_wood.md](../recipes_wood.md) "Pending" and history 2026-10-30; shipped 1.1.05; open: S (SQ-RW-23 FAILED r1031, fix is `#10`; SQ-RW-24 client), C (CQ-RW-23), P (RW-23 to RW-26).
- `engineering/dreams-and-desires #3` Rose quartz sand blasting: [engineering.md](../engineering.md) 3; shipped 1.1.05; open: S (EN-21 server half PASSED r1031), C P T (EN-19, EN-20, EN-21 JEI look).
- `engineering/dreams-and-desires #4` Extra woods: [recipes_wood.md](../recipes_wood.md) 2.6 "D&D woods"; shipped 1.1.05; open: S (SQ-RW-28 PASSED r1031), C (CQ-RW-28), P (RW-28).
- `engineering/dreams-and-desires #6` Painted asphalt: section above (no writeup yet); shipped 1.1.05; open: S (SQ-DD-08 PASSED r1031), C P (DD-08 to DD-10).

## Writeup corrections made

None (2026-10-21; rechecked 2026-10-31). The linked writeups (nether_worldgen.md F7, blockrunner.md 3.5, mob_block_tweaks.md) match the code and the
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
