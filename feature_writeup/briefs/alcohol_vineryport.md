# Brief: alcohol / vineryport

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# alcohol`, Sub `vineryport` (row 26); extended 2026-10-31 to every
open row (3, 4, 5, 8, 15, 22, 24, 25, 26, 28) and checked against the pack. Layout per [README.md](README.md). Writeups:
[alcohol.md](../alcohol.md) Part B (test prefix `VB-`, last id VB-98), [vinery.md](../vinery.md) (1.3.10 reference, `VN-`),
[jei_world_tabs.md](../jei_world_tabs.md) (Grape Planting page, `JW-`, last JW-27), [vinery_jei.md](../vinery_jei.md)
(history only since 2026-10-27), [creative_brewing.md](../creative_brewing.md) (the bundle).

**Where the port lives now (2026-10-31):** `development/creativeBrewing/members/vinery` (Gradle project `:backportedVinery`,
version `1.5.5-ce-19.2-08`), nested in `creativebrewing-1.0.0.jar`. Build and install only through
`./gradlew :creativeBrewing:build --offline` / `:creativeBrewing:installToMods` (game closed). `mods/` holds that jar since
2026-10-31 16:55 and `logs/latest.log` shows it booted 2026-10-31 17:15 with no Vinery errors. Paths below that start
`members/vinery/` mean `development/creativeBrewing/members/vinery/src/main/`. Pack-side Vinery scripts go to Creative
Brewing's tree `kubejs/data/creativebrewing/kubejs/vinery/` (creative_brewing.md 2.4); `kubejs/server_scripts/cooking/alcohol.js`
still holds the older Vinery rows until `creativebrewing/move #1`.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `#24` stem shearing duplicates seeds | new jar | Confirmed in code (below); a three-line fix. Batch with 2 to 5 in one CB build |
| 2 | `#28` Vinery trees stack on themselves | new jar / data | Look in game first; the likely cause is the 1.5.5 tree shape the port converts |
| 3 | `#25` grapes only on soil | new jar + tag | Decide the soil tag, then change `mayPlaceOn` and the planting text |
| 4 | `#26` shorter JEI texts | new jar / lang | After #25 (same strings) |
| 5 | `#5` `#missing` models | new jar | 14 WARN lines now, not 4; I25 belongs to `pack/release #5` |
| 6 | `#15` ingredient audit | KubeJS | Candidate list below from the 2026-10-30 recipe dump |
| 7 | `#22` cellar audit and player docs | docs / maybe lang | Most player text exists already (JEI Wine Aging page, rack tooltips, WTHIT) |
| 8 | `#8` 1.3.10 reference tests | docs | Recommend retiring VN- and VJ-, carrying 6 behaviours into VB- |
| 9 | `#4` upstream jars on publish | tool | Only when the pack is published |
| - | `#3` wine effect reshuffle | Pinned | Waits for `alcohol/decisions #2`; the multi-effect format is already built |
| - | `#1`, `#2`, `#6`, `#7`, `#9`, `#10`, `#13`, `#14`, `#16`, `#23`, `#27` | play | Built, needs play (table at the end) |

---

## vineryport #24: Ripe stem shearing may duplicate seeds
Status **Planned** / Work: new jar / id `alcohol/vineryport #24`

### Goal
Shearing a planted grapevine stem should give the seed back once and empty the stem, as 1.3.10 did.

### Now (code read 2026-10-31)
- `members/vinery/java/net/satisfy/vinery/core/block/PaleStemBlock.java` lines 87-98: with shears on a stem with `age > 0`,
  it drops grapes when `age > 2`, **always** drops one seed (`dropGrapeSeeds`), then sets `age - 1`. Stem age runs 0-4
  (`StemBlock.java` line 37, `AGE_4`). So a ripe stem sheared four times gives 4 seeds and 2 grape drops, and the seed
  that was planted is still "in" it until age 0: confirmed duplication, no game check needed to know it happens.
- The JEI Grape Planting page says "Shears give the seed back; breaking the block loses it"
  (`members/vinery/resources/assets/vinery_backport/lang/en_us.json` line 49), which is the intended rule.

### Build
1. In `PaleStemBlock#useItemOn`, the shears branch: drop grapes if `age > 2`, drop one seed, then set the stem to age 0
   with `LEAVES_PENDING` false (the existing `sheared.getValue(AGE) == 0` branch); keep the sound and the client success.
2. Leave `GrapeVineBlock.java` lines 48-51 alone: jungle vine shears only toggle `STERILIZED` and drop nothing (VN-17).
3. Rebuild `:creativeBrewing`, install with the game closed.

### Decisions
- Ripe stem: grapes and the seed together on one shear (recommend yes, same as harvesting then shearing).

### Tests
- **VB-99 (proposed) Stem shearing:** shear a ripe red grape stem: grapes and one seed drop, the stem is empty (age 0, no
  grapes). Shear again: nothing. An unripe stem: one seed, empty.

---

## vineryport #28: Vinery trees grow upward on themselves?
Status **Planned** / Work: new jar / id `alcohol/vineryport #28`

### Goal
Vinery apple and cherry trees seem to grow on top of themselves; check it and stop it.

### Now
- The port reads its tree features from the 1.5.5 donor jar (`vinery_upstream/letsdo-vinery-fabric-1.5.5.jar`,
  `data/vinery/worldgen/configured_feature/{apple, apple_variant, dark_cherry, dark_cherry_variant}.json`), converted in memory
  by `UpstreamTranslator.convertWorldgen` (`members/vinery/java/net/satisfy/vinery/forge/upstream/UpstreamTranslator.java`
  lines 688-713): it drops `sapling_provider` and turns 1.20's `cherry_foliage_placer` into `fancy_foliage_placer`.
- 1.5.5's apple tree is a `fancy_trunk_placer` with `base_height` 13 (+1 to 6), dark cherry `base_height` 7 (+3 to 5), both with
  the cherry foliage numbers (radius 4, height 4, offset 2). A fancy (big oak) trunk that tall with fancy foliage puts
  several leaf blobs up one trunk, which reads as a tree standing on a tree. 1.3.10's apple was a 5-7 block
  `straight_trunk_placer` with `blob_foliage_placer` (radius 2).
- The same features grow from the saplings (`VineryTreeGrower`, keys `apple_variant` / `dark_cherry_variant` in
  `core/world/VineryWorldGen.java` lines 11-13; saplings `ObjectRegistry.java` lines 104-105) and generate wild
  (`placed_feature/tree_apple`, `tree_cherry`). No pack override of these four files exists (`kubejs/data/vinery/worldgen/`
  only overrides the grape bushes).
- Vinery's apple tree is the pack's only apple tree since 2026-10-30 (item_merges.md, Fruits Delight's removed).

### Build
1. In game (P): grow one apple and one cherry sapling and find a wild one; screenshot. If it is the tall multi-blob shape,
   it is the conversion, not a growth bug.
2. Fix in `convertWorldgen` for the four tree configs: either (recommended) replace the trunk with
   `minecraft:straight_trunk_placer` (apple base 5, rand 2; cherry base 6, rand 2) and foliage with `blob_foliage_placer`
   (radius 2-3, height 3), close to 1.3.10; or keep `fancy_trunk_placer` with base 4 to 6 so one crown forms. Write the
   values as port-written JSON overrides under `members/vinery/resources/data/vinery/worldgen/configured_feature/` if the
   translator lets port data win over donor data (check `keep()`), else patch in the translator.
3. If step 1 shows a tree really regrowing on its own top (a sapling-like random tick on leaves or logs), look at
   `AppleLeavesBlock` / `DarkCherryLeavesBlock` random ticks instead.

### Decisions
- Tree shape: 1.3.10-like small trees (recommend) or a lower 1.5.5 fancy tree. Ask the owner with the screenshots.

### Gotchas
- Changing a configured feature changes new chunks only; existing trees stay.
- `fruit leaves` crop seasons (`farming/crops #5`) key on the leaves blocks, not the feature: unaffected.

### Tests
- **VB-100 (proposed) Tree shape:** an apple and a cherry sapling each grow one trunk with one crown; wild trees in new
  chunks match; apples and cherries still grow on the leaves.

---

## vineryport #25: Grapes grow only on soil blocks
Status **Planned** / Work: new jar / id `alcohol/vineryport #25`

### Goal
Ground grape bushes plant only on soil (today any full solid block), and the JEI planting text says so.

### Now
- `members/vinery/java/net/satisfy/vinery/core/block/GrapeBushBlock.java` line 111 `canSurvive` calls `mayPlaceOn` (lines
  122-124): `floor.isSolidRender(level, pos)`. So stone, planks and glass blocks work; farmland and slabs do not (not
  solid-render). Taiga bushes add a 4-block soil check for growth (`TaigaGrapeBush`, `hasTaigaSoil`).
- Stems (`PaleStemBlock.java` line 197) need a redstone-conductor block below, lattices a wall: out of scope (they are built,
  not planted in soil).
- Wild bush worldgen (`kubejs/data/vinery/worldgen/configured_feature/*_grape_bush.json`) places only on `minecraft:grass_block`
  or `#creativeengineering:worldgen/grass_like_soils`, so it is unaffected if the soil tag holds those.
- `#minecraft:dirt` in the 2026-10-30 dump has 28 blocks (vanilla dirt family, mud, moss, rooted dirt, FD rich soil, Nether's
  Delight rich soul soil, the IW soils, Botania grasses, Fungi truffle dirt, Vintage lush grass).
- JEI text: `grape_planting.where.bush` "Plantable on any solid block, ..." and `support.ground` "Any full solid block below"
  (lang lines 31, 40).

### Build
1. Port data: a block tag `vinery:grape_bush_soil` in `members/vinery/resources/data/vinery/tags/blocks/` holding
   `#minecraft:dirt`, `minecraft:farmland`, `farmersdelight:rich_soil_farmland` (optional entries) and
   `#creativeengineering:worldgen/grass_like_soils` (optional).
2. `mayPlaceOn`: `floor.is(BUSH_SOIL)` (a `TagKey<Block>` in the port's tags class). Keep the light check in `canSurvive`.
3. Pack additions, if any, through `global.tags` in [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) with a new
   `b.t_grape_bush_soil` key in `kubejs/startup_scripts/_lib/block_dict.js`.
4. Lang: `where.bush` "Plantable on any soil, grows %s at light level %s or more", `support.ground` "Dirt, grass, farmland or
   another soil block below" (shortened further in #26).

### Decisions
- **Which soil tag** (the row's question): recommend `#minecraft:dirt` plus farmland, as above. Sand and gravel stay out
  (savanna bushes generate on grass, not sand).

### Gotchas
- A bush already standing on stone pops off on its next neighbour update once this ships (it fails `canSurvive`); mention
  it in the changelog bullet.

### Tests
- **VB-101 (proposed) Grape soil:** red grape seeds plant on dirt, grass, farmland, rich soil and moss; not on stone, planks or
  sand; the taiga rule still applies; JEI says "any soil".

---

## vineryport #26: Shorten Backported Vinery JEI descriptions
Status **Planned** / Work: new jar / lang / id `alcohol/vineryport #26`

### Goal
Shorten the JEI description texts of the Vinery backport items (see `jei/jei-coverage #12`, planting text).

### Now (checked 2026-10-31)
- The only JEI texts the port has are its three own pages; there are no JEI item-info pages for Vinery items (no
  `addIngredientInfo` in `VineryJEIPlugin`, none in the pack's lang). The texts are lang keys in
  `members/vinery/resources/assets/vinery_backport/lang/en_us.json` lines 19-49: `jei.vinery_backport.grape_stomping.*`,
  `wine_aging.*` (year, level, cellar, cold, warm) and `grape_planting.*` (where.bush / vine / stem / lattice, slow, fast,
  taiga, vine_shears, leaves, support.*, grapes.*, seeds.*). `support.*`, `grapes.*` and `seeds.*` are slot tooltips, not page
  text (`GrapePlantingCategory.java` lines 114-146).
- `members/vinery/java/net/satisfy/vinery/core/compat/jei/port/GrapePlantingCategory.java`: page `WIDTH` 150 (line 51),
  `LINES` 6 (line 58), the page height is fixed at `TEXT_Y + LINES * LINE_HEIGHT` (lines 77-78), and `draw` stops after six
  wrapped lines (lines 197-201). `WineAgingCategory` already sizes its page to the wrapped text (its class comment, line 34).
- The slow / quick wording `jei/jei-coverage #12` asked for is in (lines 35-36). The "stop it growing and" cut-off that row
  quotes is gone: the vine line is now "Shears stop growth and spreading; again to undo" (line 38). Unverified in game
  whether any page still cuts off.

### Build
1. Rewrite each page line to fit one or two wrapped lines at 146 px: e.g. `where.*` "Any soil; grows %s at light %s+",
   "On a wall, spreads like vines; grows %s at light %s+", "On a 2-high grapevine stem; grows %s at light %s+";
   `wine_aging.cold` / `warm` without the parenthesis ("x%s near more freeze than heat sources"); keep `support.*` as
   tooltips but shorten them the same way.
2. `GrapePlantingCategory`: measure the wrapped lines of the longest page once (as `WineAgingCategory` does) and size the
   background from it, so nothing is dropped; remove the `drawn++ == LINES` cut.
3. Rebuild with #25 in the same CB build; update jei_world_tabs.md 3 (JW-16) with the new strings (vinery_jei.md is history).

### Decisions
- Order with `#25`: change the soil text once, after the soil tag is decided (recommend `#25` first, then this pass).

### Tests
- **JW-28 (proposed):** the Grape Planting, Grape Stomping and Wine Aging pages show all their text with no cut-off at the
  default GUI scale; no line wraps more than twice.

---

## vineryport #5: I25 tool reruns and `#missing` models
Status **Planned** / Work: tool / new jar / id `alcohol/vineryport #5`

### Goal
Rerun the generators after the port (I25), and stop the `#missing` texture faces on the wine models.

### Now
- **I25 is `pack/release #5`'s work.** alcohol.md B3.4 I25 lists `crop_growth_coverage.py`, `placeable_catalog.py`,
  `stack_sizes.py`, `gen_spawn_table.py`, `food_catalog.py` and `/dumpall`. The dumps with the port exist (2026-10-26 scratch,
  2026-10-30 client), and the generated files already carry Vinery keys (37 in food_types.json, 28 in stack_sizes.json). The
  last four tools are in `tools/generators.json` (`/cegen`); `crop_growth_coverage.py` is not.
- **`#missing` is 14 WARN lines now, not 4.** `logs/latest.log` (boot 2026-10-31 17:16): `Unable to resolve texture
  reference: #missing in` the four upstream models `vinery:block/{aegis, glowing, stal, strad}_wine` and ten port-built
  models `vinery_backport:block/wine_box/{aegis_wine, apple_cider, chenet_wine, creepers_crush, eiswein, glowing_wine,
  magnetic_wine, mellohi_wine, stal_wine, strad_wine}`. The wine box models are copies of Strad's model
  (`members/vinery/java/net/satisfy/vinery/forge/upstream/BottleLooks.java`, `BOX_SHAPE = "strad_wine"`), so they inherit
  its `#missing` faces. In Stal / Strad the faces are the zero-thick label's underside (alcohol.md B4.9).

### Build
1. I25: drop it from this row and leave it with `pack/release #5` (CQ-DUMP-04). Run `python3 tools/crop_growth_coverage.py`
   by hand once on the 2026-10-30 dump and check grapes and fruit leaves are listed.
2. Models: in `UpstreamTranslator`, when a block model is copied from the donor, rewrite a face texture `#missing` to the
   texture of the same element's opposite face, falling back to `#particle`. `BottleLooks` builds from the already-fixed
   Strad model, so the ten wine box copies are fixed with it.
3. Rebuild with the other port rows; grep the next `latest.log` for `#missing in vinery`.

### Decisions
- Remap or drop the faces: recommend remap (dropping a face can open a see-through hole in aegis and glowing, whose
  `#missing` faces are not known to be label undersides).

### Tests
- **VB-102 (proposed) No missing faces:** no `#missing in vinery` line in `latest.log`; Stal, Strad, Aegis and Glowing
  wines placed, in the single rack and in the wine box show no black-purple face from below.

---

## vineryport #15: Vinery ingredient audit
Status **Planned** / Work: KubeJS / data / id `alcohol/vineryport #15`

### Goal
Check the port's recipe ingredients and tags against the pack's items and merges.

### Now (2026-10-30 recipe dump, `kubejs/exported/food/recipes.json`, 140 `vinery:` recipes)
Non-Vinery ingredients by family, with the audit question each raises:
- **Sugar** (`minecraft:sugar`, 9 recipes: apple cider, aegis, clark, praetorian...): the pack has `#forge:sugar` (sugar,
  CE powdered sugar, and PneumaticCraft glycerol, which should not count) and the sweetener keys the Vintage audit used
  (`i.any_sweetener_plain`, Creative Delight `vintagedelight/recipes.js` line 31).
- **Apples** (`minecraft:apple`, apple bag, apple mashing): `#forge:fruits/apple` holds only the vanilla apple, so no change.
- **Honey** (`minecraft:honey_bottle` in 5 wines, honeycomb in the apple press): the pack's honey fluid decision for Brewin'
  (alcohol.md 4.1 A) does not touch items; bottles stay.
- **Straw**: the basket takes `#minecraft:straw` (FD straw, wheat, Supplementaries flax); the straw hat takes plain
  `minecraft:wheat`. Make the hat take `#minecraft:straw` too, or leave it.
- **Cherries:** `#forge:fruits/cherry` is empty in the dump, and `vinery:cherry` is only in `#forge:berries` / `#c:berries`.
  Check whether any other mod's cherry exists (none in item_dict); if so, tag and merge.
- **Wool / fabric**: the Winemaker leggings already take white wool or IE hemp fabric (pack `fabric.js`). Other Winemaker
  pieces use dyed wool only.
- **Wood**: racks use per-wood planks and slabs (vanilla seven plus Quark bamboo); `grapevine_pot` and `wine_box` use
  `#minecraft:planks` / `#minecraft:wooden_slabs`. No other mod's woods get racks; that is a design question, not a bug.
- Grapes and juices are Vinery's own; other "grapes" in the pack are Oceanic Delight sea grapes (a different food).

### Build
1. One Creative Brewing file `kubejs/data/creativebrewing/kubejs/vinery/recipes.js` (`// requires: vinery`, early return on
   `!global.CB || !global.CB.on('vinery')`, rows inside `global.CB.recipes`).
2. For each accepted change: `R.remove({ id: 'vinery:<id>' }, e)` and rebuild with the matching `global.NCH` helper
   (`R.shaped`, `R.shapeless`; the wine fermentation and apple press types need a `vineryFermenting` / `vineryPress` helper
   in neoCraftHelper.js if any of those change: patcherMod rebuild). Tags (cherry) through `global.tags`.
3. Record the outcome in alcohol.md B3.4 (pack integration).

### Decisions
- Sugar in wines: `#forge:sugar` minus glycerol, i.e. a pack key for sugar + powdered sugar (recommend), or leave plain sugar.
- Straw hat on `#minecraft:straw`: recommend yes (one line).

### Gotchas
- `replaceInput` may not reach Vinery's own recipe types (it does not reach the Vintage jar's, recipes.js line 29): check
  one, else rebuild those rows.

### Tests
- **VB-103 (proposed) Ingredients:** an Apple Cider ferments with powdered sugar; the straw hat crafts from FD straw; JEI
  shows the changed rows once.

---

## vineryport #22: Cellar mechanic audit and player docs
Status **Planned** / Work: docs / id `alcohol/vineryport #22`

### Goal
Audit how the Vinery cellar works and write it up somewhere players see it.

### Now
- Mechanic (`members/vinery/java/net/satisfy/vinery/core/wine/WineYears.java`): wine ages on the vintage clock everywhere
  (`[time.wine] clock = "SUN"`, `config/creativeengineering-common.toml` line 2055). In a Foundation storage block
  (racks, wine box: `StorageBlockEntity` listener in `core/Vinery.java` lines 28-41) the storage kind is sampled **once at
  insert** (`startStorage`, line 203; `storageKind`, line 106): cellar = no sky light, cold / warm from Brewin's keg climate
  count. Factors multiply (cellar x2.0, cold x2.0, warm x0.75; `[time.wine.boosts]` line 2063).
- Player text already there: JEI Wine Aging page (lang lines 24-29: cellar, cold, warm, "these multiply"), the rack and
  wine box tooltip "Wine aging is sped up by dark and cold environments, slowed by warm ones" (VB-78), the wine tooltip's
  storage line, WTHIT's `cold cellar x4.0` line, `/wine info`.

### Build
1. Audit questions to answer from code, then check one each in game: does a wine re-sample its kind if the light or the
   heat sources change while it sits (code says no: only at insert)? Placed wine bottles (`WineBottleBlock`): storage or
   not? Do chests / barrels / Sophisticated storage count (code says no: base rate)? Does `bonusTicks` survive a
   remove-and-reinsert?
2. Write the answers into alcohol.md 10.13 as a short "Cellar rules" list.
3. Player-facing: extend the JEI Wine Aging page with one line "Only wine racks and the wine box; set when the wine goes
   in" (lang `wine_aging.storage`), and change nothing else. A guide book entry is not needed.

### Decisions
- **Where** (the row's question): recommend the JEI Wine Aging page plus the rack tooltip that already exists; FTB Quests
  only if a quest chapter for wine is written anyway.
- If "sampled once" surprises players: re-sample on a slow block tick (recommend leaving it; say it on the page).

### Tests
- **VB-104 (proposed) Cellar rules:** a wine racked in a lit room then darkened keeps "sunny"; removed and reinserted it
  reads "cellar"; a wine in a chest shows no storage line; the JEI page line is there.

---

## vineryport #8: 1.3.10 reference tests
Status **Planned** / Work: docs / id `alcohol/vineryport #8`

### Goal
Settle VN-01 to VN-20 (1.3.10) and VJ-01 to VJ-10 (patcherMod's deleted Vinery pages).

### Now
- [vinery.md](../vinery.md) 7 still lists VN-01 to VN-20 as open; [vinery_jei.md](../vinery_jei.md) is marked history
  (code removed 2026-10-27) and says VJ- can no longer run.
- Superseded by VB-: VN-01 (VB-10 stomping), VN-05 / VN-08 (VB-93, VB-94), VN-13 (VB-13 lattice), VN-14 (VB-15 taiga bush
  soil), VN-18 (VB-17 fruit leaves), VN-19 (the aging block of VB-). Unchecked against VB-: VN-02, VN-04, VN-06, VN-07
  (pot type lock, press and barrel timing, barrel output). VN-20 (kitchen sink) is gone with the removed block (alcohol.md 10.10).
- Not covered by any VB- test: VN-03 mob stomp, VN-09 / VN-10 stem pair and harvest, VN-15 savanna light, VN-16 bush seed
  drop, VN-17 sterilised vine, VN-12 taiga seed on a stem. VN-11 (stem seed loss) becomes #24's VB-99.

### Build
1. Decide `alcohol/decisions #12` (recommend A, retire).
2. vinery.md 7: mark every VN- row retired with its VB- replacement; vinery_jei.md 7: mark VJ- retired.
3. alcohol.md B8: add VB-105 (proposed) "Port behaviours from 1.3.10" covering mob stomping, stem pair and harvest,
   savanna bush light 14, bush seed drop by stage, vine shears toggle, taiga seeds on a stem (no soil rule).

### Decisions
- `alcohol/decisions #12`: A retire (recommend) or B rewrite.

### Tests
- VB-105 (proposed), as above.

---

## vineryport #4: Upstream jars on publish
Status **Planned** / Work: tool / id `alcohol/vineryport #4`

### Goal
D14 option A: when the pack is published, each machine downloads the two Vinery donor jars instead of getting them in the
payload.

### Now
- Payload today: `tools/build_deploy.py` `UPSTREAM_DONORS` (line 507) ties `vinery_upstream/` to the `creativebrewing` jar
  and checks each jar's sha1 against
  `members/vinery/java/net/satisfy/vinery/forge/upstream/UpstreamJars.java` lines 22-25 (1.5.5 Modrinth URL given; 1.3.10
  CurseForge files page, no direct link). `deploy/latest/` carries `vinery_upstream/` and three more donor folders
  (`miners_upstream`, `rustic_upstream`, `farmers_upstream`), all ARR donors with the same question.
- `deploy/latest/verify.sh` and `apply.sh` exist; neither downloads anything.

### Build (at publish time)
1. A fetch step in `build_deploy.py`'s generated `apply.sh` / `.ps1`: for each pinned jar with a direct URL, download into
   `<folder>/` and check the sha1; for one without (Vinery 1.3.10), stop with the page URL and the expected file name.
2. A `--publish` switch that leaves the donor folders out of the payload and writes the fetch step instead.
3. Do the same for the other three donor folders in one pass (their own TODO rows if they have them).

### Decisions
- None until publish (D14 decided). Whether the 1.3.10 jar can be dropped as a donor by then would remove the manual step.

### Tests
- **DP-11 (proposed, deploy.md):** a `--publish` build has no `vinery_upstream/` jars; `apply.sh` on a clean folder downloads
  the 1.5.5 jar, checks its sha1, and stops with a clear message for 1.3.10.

---

## vineryport #3: Wine effect reshuffle (B7.4)
Status **Pinned** / Work: new jar / tool / id `alcohol/vineryport #3`

### Goal
Two effects per wine, food-mod effects with fallbacks, picked with a `tools/wine_effect_candidates.py` list. Held until
`alcohol/decisions #2` unpins it.

### Now
- **Several effects per wine already work** (built 2026-10-31 for Jellie Wine, `#27`):
  `members/vinery/java/net/satisfy/vinery/core/wine/WineEffects.java` lines 88-116 parse
  `wine|effect[+effect...]|amplifier|ticks`; `config/vinery-common.toml` line 62 holds 26 entries, Jellie Wine with four
  effects, the rest one each.
- **No fallbacks:** if any effect id in an entry is not registered, the whole entry is dropped with a WARN (lines 98-107).
- `tools/wine_effect_candidates.py` does not exist. The effect data it would read: `kubejs/exported/mob_effects.json`
  (2026-10-30) and the JEED texts in `kubejs/assets/creativeengineering/lang/en_us.json`.
- VB-52 (effect fallbacks) is struck out as pinned in alcohol.md B8.

### Build (when unpinned)
1. Fallback syntax: `a|b` is taken, so use `effect?fallback` per slot (`fruitsdelight:x?minecraft:y`); the parser takes the
   first registered id in the chain, and drops only that slot if none is.
2. `tools/wine_effect_candidates.py`: every effect in `mob_effects.json` with its mod, beneficial / harmful and JEED text,
   filtered to food mods; prints a markdown table to pick from.
3. Owner picks two per wine; write them into the port's config defaults and `config/vinery-common.toml` (copy to
   `configs_edited/root_config/`); update the JEED provider list (`kubejs/server_scripts/other/jeed_providers.js`).

### Decisions
- `alcohol/decisions #2`: when to unpin (A after the VB- tests and the effects writeup refresh, recommended).

### Tests
- VB-52 (unpin it) plus **VB-106 (proposed)**: each wine shows both effects in its tooltip and `/vinery effect`; with the
  food mod removed, the fallback applies and the log has no dropped entry.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| vineryport #1 | Vinery 1.5.5 port | Testing | alcohol.md Part B; VB-01 to VB-98; open S~ C~ P. Now `-08` nested in Creative Brewing 1.0.0, booted 2026-10-31 17:15 (the row still says `-06` not booted) |
| vineryport #2 | Decisions built with the recommendation (D4, D5, D7, D8, D9, D10, D12) | Testing | alcohol.md B5; open S~ C~ P |
| vineryport #6 | Grass slab biome tint | Testing | alcohol.md 10.16; `#c:blendable_blocks` in blocktags.js; open C P |
| vineryport #7 | Hopper jams | Testing | vinery.md 4, alcohol.md 10.19; VB-93, VB-94; open S+ C P (SQ-OM-07 PASSED) |
| vineryport #9 | Grape stomping: 12 grapes, 4 stomps, 4 bottles | Testing | alcohol.md B7.14; VB-10; open S C P T |
| vineryport #10 | Wine case and rack fit bugs | Testing | alcohol.md 10.9 to 10.14, 10.18; VB-67, VB-71, VB-72 still unreported; open C~ P |
| vineryport #13 | Duplicate blocks removed, "Cherry" names | Testing | alcohol.md 10.10, 10.11; VB-68 to VB-72; open S C P |
| vineryport #14 | Vinery village houses | Testing | alcohol.md 13; VB-95 to VB-98; open S+ C P (SQ-OM-08, -14, -15 PASSED) |
| vineryport #16 | Vinery tooltips | Testing | alcohol.md 10.15, 10.18, [tooltips.md](../tooltips.md) 3; VB-84 to VB-86, VB-92; open C~ P |
| vineryport #23 | JEI tab for grape planting types | Testing | [jei_world_tabs.md](../jei_world_tabs.md) JW-16; open C P |
| vineryport #27 | Jellie Wine: the enchanted golden apple set | Testing | [effects.md](../effects.md) 2.7; EF-29, SQ-EFX-10 queued; open S C P. In the CB jar booted 2026-10-31 17:15 |
