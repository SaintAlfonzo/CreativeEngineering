# Vintage Delight: backport and integration plan

> **Merge note (2026-10-25):** this port is to be merged with the other Farmer's Delight add-on backports into a single jar, **Creative Delight**. The inventory summary, cross-port interactions and the merge process are in [creative_delight.md](creative_delight.md). This file stays the detailed record for Vintage Delight; its registry namespace and test prefix do not change.

Plan written 2026-10-22, rewritten 2026-10-23 with the owner's decisions; **built 2026-10-23** (port jar, goose egg and
pack integration in `mods/` and scripts; the dump-driven steps are open, §4.0). Vintage Delight (Ribs) has no
1.19.2 build: Modrinth lists 1.20.1 to 1.20.6 only. Two jars are on disk: the original 0.1.6 for Forge 1.20.1 and the
community "KOTS" 0.3.1 for NeoForge 1.21.1, with the same content (KOTS renames the jar items and adds one item).
Getting it into the pack means our own port, like Backported Vinery, Backported Brewin' ([alcohol.md](alcohol.md)) and
the Veggies Delight port ([veggies_delight.md](veggies_delight.md)), and then the same integration every other crop and
food has had.

"Vintage" in this file is Vintage Delight; "Veggies" is Veggies Delight. Test prefix `VTD-` (`VD-` is Veggies').

**Scope rule:** the port adds no recipe and no mechanic that is not in Vintage Delight or defined by a food rule
([recipes_cooking.md](recipes_cooking.md), the `food/` briefs). The only exceptions are the ones the owner asked for by
name: the goose golden egg (§2.5), the ghost pepper soil rule (§2.2) and the Mixed Seeds investigation (§2.3a). Where an earlier
draft proposed a new route (a keg vinegar, a trimmed golden egg table), it is gone.

**Overlap marker:** every Vintage item or block that duplicates something the pack already has carries the text
`*overlaps w/ <what>*` in the tables below. Those duplicates all ship; which side stays is decided after they have been
seen in game (§3.3, `VTD-40`).

This file has three parts:

- **Part 1 (section 2):** everything that is ported, the purges and the mod's machines and mechanics.
- **Part 2 (section 3):** the overlaps with the pack and the Veggies port, marked, and how they get settled.
- **Part 3 (section 4):** the pack systems the crops, foods and machines go through, in build order.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Decisions taken 2026-10-23 (purges, keep-all duplicates, wild placement, soils, pickling, no Dehydrated, vinegar, loot unwired, geese) | §1 | SETTLED |
| Mod inventory after the purges (3 crops: oats, peanuts, cucumbers; ghost peppers; 4 wild plants; pickling jar, salt line, cheese mold, mason jars, about 127 items, about 232 recipe files) | §2 | reference, from `vintagedelight-0.1.6.jar` and `vintagedelight-kots-0.3.1.jar` |
| Overlap list, all marked `*overlaps w/` | §3 | analysis; every duplicate ships, review after play |
| Integration list in build order | §4 | BUILT 2026-10-23 except the dump-driven steps (§4.0, TODO `ports/vintageport #2`); food types, cans (configured), jar fluids, compost and cooking rules built the same day; deployed with 1.1.02 (port `-03`), 1.1.03 shipped `-07` (still in `server_mods/`), `-08` in the 1.1.04 client. **Deep scan 2026-11-02:** `-13` is a nested Creative Delight member (`development/creativeDelight/members/vintage`), in the installed `mods/creativedelight-1.0.0.jar` (2026-11-02 14:01); not deployed |
| Open questions | §5 | vine tree set is on test (S1); Mixed Seeds BUILT 2026-11-01 (§2.3a) |
| Tests | §8 | VTD-01 to VTD-76 drafted, none ticked; server side by `/vtdtest all` 373+ PASS (2026-10-26); group B (SQ-VP-14 / -15) and the CQ-VP client rows open. Deep scan 2026-11-02: r1102 SQ-VTD4-04, -06, SQ-MXS-01, -02 PASS, SQ-VTD8-01 FAIL (jar stall, fixed in `-12`), SQ-VTD5-01 blast drops closed as built; r1102b s1 SQ-VTD4-03, -05, -07, -08 PASS, **SQ-VTD6-01 FAIL** (the planted crop on farmland is fixed by the `canSurvive` override, installed in `-13` and still to be re-seen in game; wild ghost peppers stay on grass: Terralith puts grass in `#minecraft:nylium`; TODO `ports/vintageport #7`), s3 SQ-VTD7-01 PASS, **SQ-VTD7-02 FAIL** (bales and salt blocks soft 0.88x, not 1.05x; resolved 2026-11-02: soft is intended, changelog and test now say 0.88x), s5 SQ-VTD8-02 (b) PASS; VTD-68 server check queued as SQ-VTD9-01; 2026-11-03 run: VTD-80 to VTD-86, SQ-VTDA-10 to -13, CQ-VTDA-01 (SQ-CDJ7), port `-14` installed 2026-11-03 00:08, not booted; `-15` built 2026-11-03 (pack jam line, recipes_cooking.md RC-180 to RC-185, SQ-PJL), not in `mods/` |

- **Source to port from:** `~/.local/share/atlauncher/downloads/vintagedelight-0.1.6.jar` (Forge 1.20.1, closest API,
  109 classes). Decompile with `python3 tools/decompile_mod.py <jar> <out dir>` (94 sources). The KOTS jar
  (`DecompiledMods/vintagedelight-kots-0.3.1.jar`, NeoForge 1.21.1) is the second reference for its fixes (§2.9).
- **Licence:** GPL (owner-supplied 2026-10-25). 0.1.6 `mods.toml` says MIT; KOTS and the current Modrinth page say GPL-3.0. Both fit a GPLv3 port; keep
  the MIT notice for 0.1.6 code and assets.
- **Dependencies:** Forge and Farmer's Delight only (`farmersdelight [1.2.4,)`). FD 1.19.2-1.2.4 has every type it
  uses: `WildCropBlock`, the `wild_crop` feature, `add_features_by_filter`, `add_loot_table` / `add_item` loot
  modifiers, cooking and cutting recipes, `RichSoilFarmland`, Comfort and Nourishment. Optional data for Create, IE,
  Create Crafts & Additions, Serene Seasons, Diet, Moonlight; the Galosphere and Spelunkery recipes stay out (mods not
  in the pack). Supplementaries / Moonlight soft fluids are used for the jars (§4 step 15).
- **Before the port (2026-10-23) nothing in the pack referenced it**, apart from the TODO rows that wait on it (`dishes/ingredients #6`, `#9`,
  `#12`, `food/seasoning #1` step 6) and 22 recipes inside the Veggies jar (§3.4).

## 1. Decisions taken (2026-10-23)

| # | Decision |
|---|---|
| S1 | **Purged:** gearo berries (bush, berries, berry bag, gearo jam jar and bottle, the wild bush and its dark forest modifier, the gearo berry as an ingredient; the `goblins_piss` brew stays with a swap, below). **Restored for testing (2026-10-23):** the whole Magic Peanut line as the mod ships it: Magic Peanut (0.05 % from ripe peanuts since 2026-11-03, was 1 % as shipped; sapling block, advancement, potato cannon type), the magic vine tree (feature, `VineTrunkPlacer`, trunk vines, 80 % Golden Egg on top, ground turned to Lush Grass), magic vine logs / blocks / stripped forms with their recipes and cuts, Vine Net, Vine Tile / Slab / Stairs, Lush Grass with its hoe route and its two mixins (`Feature.isGrassOrDirt`, `TrunkPlacer.setDirtAt`), and KOTS's lush grass config switch. It sits beside the goose golden egg (S14). Marked as a test set: after play it is kept or purged again (`VTD-08`, `VTD-47`) |
| S2 | **Kept for evaluation, all marked `*overlaps w/*`:** every salt item and block, every cheese item, mason jars including the jams, kimchi, both burgers, PB & J and nut mash, honey jar, pizzas, doughs. Nothing is dropped for overlap; nothing is removed from the other side either (§3) |
| S3 | **Not ported:** the pickled pitcher pod and the pitcher plant cutting recipe (1.20-only plant), the Galosphere / Spelunkery recipes |
| S4 | **Machines and mechanics:** all of them are in (§2.3), including the salt layer growth, evaporator, cheese mold, mason jar stacking, the pickling jar, the magic vine tree, Lush Grass and its hoe route, the golden egg block and Ghost Charcoal as shipped. **Mixed Seeds** was investigated (not a registered item upstream; decision postponed until after the build, §2.3a); Organic Mash as shipped |
| S5 | **Wild placement** folds into the existing systems (§2.2): oats, peanuts, cucumbers into `wild_crops.json`; wild ghost peppers into `nether_worldgen_rules.json` (crimson and warped biomes, on nylium). Vintage's own modifiers are switched off in the same way as every other mod's |
| S6 | **Soils:** wild oats, peanuts and cucumbers spawn on, and are placed on, every dirt soil (`#minecraft:dirt`, which holds Rich Soil and Fertile Soil, plus the Immersive Weathering soils through `gen_soil_compat.py`). **Ghost pepper seeds** can only be planted on Soul Soil, Rich Soul Soil or Nulch; the wild ghost pepper still generates on nylium |
| S7 | **Stations:** the Vintage fermenting jar is renamed **Pickling Jar** (display name, lang only) and is used for all pickling (including the Veggies fermented garlic honey and the Brewin' pickled foods, §3.2). The Brewin' **keg** is for beer fluids. The **cask** (lang name of the Vinery port's fermentation barrel) is for wine fluids that generally age |
| S8 | **No Dehydrated, anywhere:** the effect, its three potions and their brewing mixes (and the reflection hook and the three `IBrewingRecipe`s that register them), the salt dust + awkward potion step, the 0:30 on pickled foods and the century egg, the water deletion, JEED text. Nothing in the built jar may register it (§7). The six jar brews are a separate thing and stay (§2.7a). Everything else on those foods stays |
| S9 | **Vinegar** is the pack's vinegar (`i.vinegar` is an alias of `i.vinegar_jar` since the 11-02 jar merge; the bottle id is `i.vinegar_bottle`, recipe removal only; plan_scratch_bugfix_1103 1.1. Drinking the jar gives Nausea 10 s, 5.5) and is used widely as an ingredient: as the canning additive (`dishes/ingredients #6`) and in cooking where a food rule says so |
| S10 | **All fluids are Supplementaries-jarable:** every liquid the port adds has a soft fluid file (§4 step 15) |
| S11 | **Loot tables are added and not wired** (§2.7). Every new Veggies and Vintage crop is marked as needing entries in the crops loot tables, single and crate (§4 step 16) |
| S12 | **Mob food changes are accepted** (chickens, parrots, pigs, villagers); the audit against the pack's mob tweaks and baby mobs is noted as due (§4 step 18) |
| S13 | **Advancements** stay in the jar and are dropped at load with all the others |
| S14 | **Goose golden egg:** 1 in 1000 when a goose drops its egg on the ground; rolled before the walk-to-nest check, never laid in a nest, and laid away from walls when possible (§2.5) |
| S15 | **Chef hats** stay and replace Simple Hats' Chef's Hat (§3.1) |
| S16 | **Seasons:** Vintage's shipped Serene Seasons tags ship as they are; the season overhaul is still due (`farming/crops #5`) |

## 2. Part 1: what is ported

### 2.1 Crops

| Plant | Planted from | Block, max age | Harvest (ripe) | Soil | Seasons shipped (overhaul due) | Replant today |
|---|---|---|---|---|---|---|
| Oats | Oat Seeds | `oat_crop`, 7 | oat + 1 to 2 seeds; a knife (`#farmersdelight:straw_harvesters`) adds straw | farmland | autumn, winter | none of its own: Quark replant and hoe area (§4 step 3) |
| Peanuts | Peanut (no seeds) | `peanut_crop`, 7 | peanuts; 0.05 % a Magic Peanut (pack, was 1 %; tree test set, S1) | farmland | autumn, winter | none of its own: Quark replant and hoe area |
| Cucumbers | Cucumber Seeds | `cucumber_crop`, 7 | break: cucumber + seeds (binomial); **right-click at 7: 1 to 2 cucumbers, back to age 5** | farmland | spring, summer | **own pick**: test `VTD-02` |
| Ghost peppers | Ghost Pepper Seeds | `ghost_pepper_crop`, 7 | as cucumber (right-click 7 -> 5) | **Soul Soil, Rich Soul Soil, Fertile Soul Soil (Nulch)** (was nylium only) | spring, summer | **own pick**: test `VTD-03` |

- Oats, peanuts, cucumbers and ghost peppers `extend CropBlock`; oats and peanuts override only age and shape.
  Cucumber and ghost pepper add their own `use()` (the 7 -> 5 pick); ghost pepper adds the soil check.
- Ghost pepper soil: the hard-coded nylium `mayPlaceOn` becomes a block tag the port reads
  (`#vintagedelight:ghost_pepper_soil`); the pack fills it with `minecraft:soul_soil`, Nether's Delight Rich Soul Soil
  (`nethersdelight:rich_soul_soil`) and `immersive_weathering:nulch_block` through `global.tags`. Nylium is not in
  it, so a planted pepper never sits on nylium. KOTS's planting-soil tooltip on the seeds is kept and lists these three.
- Produce tags: `#forge:oat`, `#forge:peanut`, `#forge:cucumber`, `#forge:chilipepper` (ghost pepper),
  `#forge:vegetables/{cucumber,ghost_pepper,peanut}`. No `#forge:crops/*` or `#forge:seeds/*` tags (the pack adds them
  with the Veggies pattern, §4 step 2).
- **Replant marks (existing replant mechanics, each with a test to run):**

| Crop | Mechanic | Where it is | Test |
|---|---|---|---|
| Cucumbers | own right-click pick, 7 -> 5 | Vintage | `VTD-02` |
| Ghost peppers | own right-click pick, 7 -> 5 | Vintage | `VTD-03` |
| Zucchini | own right-click pick, a ripe plant pops 1 or 2 and drops back 3 ages | Veggies, not yet in [crops.md](crops.md) §2.1 | `VD-25` (existing) plus `VTD-04` for the hoe |
| Oats, peanuts | plain `CropBlock`: Quark break-free replant and hoe area | folded in, rows in crops.md §2.1 | `VTD-01` |

  The other Veggies crops (bell pepper, broccoli, cauliflower, garlic, sweet potato, turnip) are plain `CropBlock`s and
  stay under `VD-02`.

### 2.2 Wild plants and worldgen

| Feature | Vintage placement (switched off) | Pack placement | Ground |
|---|---|---|---|
| Wild Oats (FD `wild_crop`) | FD filter: overworld, temperature 0.4 to 0.9 | a rule in `wild_crops.json`: `cold grassy`, `temperate grassy` (59 biomes; owner, 2026-10-23) | every dirt soil |
| Wild Peanuts (FD `wild_crop`) | same as oats | `wild_crops.json`: `warm dry grassy` (12 biomes: savannas, wooded badlands, Terralith shrublands; owner, 2026-10-23) | every dirt soil |
| Wild Cucumbers (FD `wild_crop`) | `#forge:is_wet` | `wild_crops.json`: `wet grassy`, `wet muddy`, `swamp`, plus `cold grassy`, `temperate grassy` (70 biomes; owner, 2026-10-23) | every dirt soil (the old list of dirt, grass, coarse dirt, sand and red sand is replaced; sand is out) |
| Wild Ghost Peppers (`GhostPepperWildCropBlock`, `random_patch`, 50 tries) | every Nether biome | `nether_worldgen_rules.json` feature entry for **crimson forest and warped forest**, run through `tools/gen_nether_worldgen.py` | nylium (crimson and warped) |
| Salt patch (FD `wild_crop` of salt layers) | `#vintagedelight:is_salt_spawn` = beach, snowy beach, stony shore | kept as shipped (S2/S4), *overlaps w/ The Salt* | dirt, grass, sand, gravel, stone |

- All at `vegetal_decoration`. Rates stay the mod's; only the biomes and the ground change, as for every other crop
  ([crops.md](crops.md) §2.8). Vintage's biome modifiers for oats, peanuts and cucumbers go into the `forge:none`
  replacement list. No gearo bush feature exists.
- The wild plants' own survival check (`mayPlaceOn` / `canSurvive`) also uses the dirt-soil rule so the block can be
  placed by hand and by `/cdevplace` on any dirt soil. The wild ghost pepper is the exception: its block keeps nylium and
  the three seed soils, so it grows where it is generated and where the seeds go.
- **1.1.02 boot failure (fixed in port build -03, 2026-10-23):** the four placed features using FD's
  `farmersdelight:biome_tag` filter (oats, peanuts, cucumbers, salt) wrote the key as `tag`; FD 1.2.4 reads
  `biome_tag`. Every world failed at datapack load with `Not a JSON object: ["vintagedelight:patch_wild_peanuts"]`
  (DFU's either codec reports only its last branch, the inline object, so the message hides the real field error;
  peanuts was simply the first modifier read). Test `SQ-VP-00`.
- `tools/gen_soil_compat.py` runs over the new features; [crops_by_biome.md](crops_by_biome.md) is regenerated;
  `tools/test_wild_crops.sh` covers the new crop tags.

### 2.3 Machines and mechanics (all kept)

- **Pickling jar** (the mod's Fermenting Jar, renamed in lang: glass, wooden slab, water bucket). Block entity with a screen: 6
  ingredient slots, 1 container slot, 2 output slots; no heat, no fuel. Recipe type `vintagedelight:fermenting`
  (ingredients, optional container, output with NBT, optional `secondaryOutput`, `processingTime`). Hoppers: bottom face
  extracts outputs, other faces insert by slot rules. Own JEI category with a click area. It is the station for all
  pickling (S7).
- **Salt growth.** `SaltBlock` random-ticks: with water beside it and air above, it grows a Salt Layer (snow-like, 1 to
  8 layers) on top. Salt Block grows at 10 % per tick, the **Evaporator** (4 logs + salt block) at 50 %. Layers drop
  salt dust. Salt dust right-clicked on the ground places or thickens a layer. Water bucket smelts into a Salt Bucket ->
  3 salt dust. *overlaps w/ The Salt* (its cauldron evaporation and crystal growth).
- **Cheese Mold** (iron ingots + heavy pressure plate). Fill with 4 Cheese Curds (milk bucket + salt + vinegar in the
  pot, 4 curds); the next random tick turns it into a Cheese Wheel, taken by right-click. Wheel -> 4 slices (knife).
  *overlaps w/ Brewin' cheese (keg -> wheel -> wedges)*.
- **Mason jars.** Empty jars and the filled kinds (apple sauce, sweet berry jam, glow berry jam, nut mash, pepper jam,
  relish, honey, vinegar; the gearo jam is purged) are placeable, 1 to 4 per block, waterlogged. A jar holds 3 bottles'
  worth: jar + 3 glass bottles <-> 3 bottles by hand. Filled jars are pot meals with the jar as container.
- **Golden Egg** (block). Breaking it drops 8 to 32 gold nuggets, 1 to 15 gold ingots, 0 to 7 golden apples and 2 to 16
  golden carrots; table unchanged. Falls like sand when unsupported (port change 2026-10-25, `GoldenEggBlock` extends
  `FallingBlock`). Sources: the magic vine tree top (80 %, test set, S1) and the goose (§2.5).
- **Organic Mash** (bone meal x3 per use): secondary output of the vinegar pickling recipes, input to the organic compost
  recipes (§2.3a); ported as shipped.
- **Lush Grass** (block). Bone meal places flower forest, meadow and grass features; a hoe turns it straight into FD Rich
  Soil Farmland. Two mixins (`KeepLushGrassTreeMixin`, `KeepLushGrassGiantTreeMixin`: `Feature.isGrassOrDirt`,
  `TrunkPlacer.setDirtAt`) stop trees turning it to dirt. All kept, with KOTS's config switch for the tree's grass
  conversion.
- **Vine Net** (top, middle or bottom slab-like net; 4 vanilla vines) and **Vine Tile / Slab / Stairs** (tile from the
  Create or cutting-board cut of a stripped magic vine; slab and stairs from tiles). Kept; the tiles come from the magic vine logs of the tree.
- **Chef hats** (16 colours, head slot, swap on use), **Ghost Charcoal** (fuel, smelted from ghost peppers).
- **Mob food** (static edits, access transformer): chickens eat the 3 seeds, parrots tame with them, pigs eat cucumber,
  ghost pepper, raw oats, peanut; villagers pick up ghost pepper, cucumber, peanut, oat. Accepted; audit due (S12).
- **Purged mechanics, for the record:** the gearo bush pick and the Dehydrated
  effect and potions (S1, S8).

#### 2.3a Mixed Seeds: BUILT 2026-11-01 (port `-10`)

The owner postponed the decision until the port is built and seen in game (2026-10-23), then decided a real seed item
(2026-11-01). Built as described at the end of this section; the findings below are the 2026-10-23 investigation.

Findings (2026-10-23, from both jars and the web):

- **It is not a registered item** in 0.1.6 or KOTS 0.3.1. No class in either jar contains `mixed_seeds`; `ModItems`
  registers only `oat_seeds`, `cucumber_seeds` and `ghost_pepper_seeds` (peanuts plant from the peanut). There is no
  lang key, item model (0.1.6), recipe, tag or advancement for it. An earlier draft of this plan called it a registered
  item; that was wrong.
- What exists: the texture `textures/item/mixed_seeds.png` (both jars) and `loot_tables/chests/mixed_seeds.json`
  (byte-identical in both). The table is a copied vanilla chest template: 2 to 10 rolls of book, paper, map, compass
  and a level-30 enchanted book, plus one guaranteed eye armor trim template (1.20 only). Nothing in it is a seed and
  nothing references the table.
- Web: the Modrinth description and the changelogs of 0.0.12 and 0.1.2 to 0.1.6 never mention mixed seeds; the GitHub
  issues on seeds (#18, #9, PR #24) are about seed tags and Botany Pots. No author post was found. CurseForge and the
  KOTS fork's source were not reachable. Source repo: github.com/ribs498/VintageDelight-master.
- **Conclusion:** no evidence of an intended function; it looks like an abandoned idea (a texture and a stray table).
- **What the port does:** registers no item; the texture ships unused; the table ships with the 1.20-only trim entry
  stripped, unwired like the other chest tables (§2.7).
- **DECIDED 2026-11-01 (owner):** a real seed item, found in chests and dropped from grass; planted, it randomly plants
  beetroot, wheat, cabbage, melon or pumpkin seeds (TODO `creativedelight/members #32`). The owner first asked for a mark
  only, then the same day for the build (second batch); the builder's calls are below.
- **Built 2026-11-01** (port `0.1.6-ce-19.2-10`, built, not installed; Creative Delight scripts):
  - Item `vintagedelight:mixed_seeds` ("Mixed Seeds", tooltip "Plants a random crop"), port class `MixedSeedsItem`, the
    shipped texture, in the Vintage tab beside the other seeds; `i.mixed_seeds`. A pack addition, the one exception to the
    port's scope rule.
  - Use on a block: the seed kinds in item tag `#vintagedelight:mixed_seeds_plants` are tried in a random order and the
    first that plants there is planted, as if that seed had been used; one Mixed Seed is spent (none in creative).
    **The pick is made per planting** (each use), not per crop block: the planted block is an ordinary wheat, beetroot,
    cabbage, melon stem or pumpkin stem and drops its own seeds. Nothing plants where none of the five would.
  - Tag (`i.t_mixed_seeds_plants`, filled in CD `vintagedelight/tags.js`; the jar ships it empty): wheat, beetroot,
    melon and pumpkin seeds and **`farmersdelight:cabbage_seeds`** (`i.cabbage_seeds`, the only cabbage seed in the
    pack). Mixed Seeds are in `#forge:seeds`; compost 0.3 through the seeds rule.
  - Chests (CD `vintagedelight/loot.js`, `L.add`, share 0.04, 1 to 4): the five village house tables (plains, taiga,
    savanna, snowy, desert) and the pillager outpost, the tables that already carry farm seeds. Upstream's own
    `chests/mixed_seeds` table (books and paper) stays unwired.
  - Grass (same file, LootJS): 2 % per grass or tall grass broken without shears, on top of the vanilla wheat seed drop
    (12.5 %).
  - JEI information page (CD client `vintagedelight/jei_info.js`).
  - Tests VTD-46, VTD-79; scratch SQ-MXS-01 to -03.

Organic Mash, checked at the same time: it is **not** a dead item. It is the secondary output of both vinegar pickling
recipes (`fermenting/vinegar_from_jam`, `vinegar_from_jam_bottles`) and an input to Vintage's organic compost crafting
recipes (`organic_compost_from_tree_bark_mash`, `_from_rotten_flesh_mash`, `organic_compost_tree_fertilizer`); used on a
block it works as bone meal x3. All ported as shipped.

### 2.4 Items and blocks (about 127 after the purges and S3)

| Group | Items |
|---|---|
| Produce and seeds | oat, raw oats, oat seeds, peanut, roasted peanut, honey roasted peanut, cucumber, cucumber seeds, ghost pepper, ghost pepper seeds, (KOTS) cucumber dough |
| Ingredients | oat dough, cheese curds, cheese slice, cheese wheel, cucumber noodles, salt dust, salt bucket, vinegar bottle, organic mash |
| Jar foods | 8 mason jar blocks (empty + 7 filled) and their 6 bottle forms (apple sauce, sweet berry jam, glow berry jam, nut mash, pepper jam, relish, vinegar; honey is the vanilla bottle) plus nut milk bottle |
| Fermented | pickle, pickled beetroot, pickled egg, pickled onion, pickled pepper, kimchi, surströmming, century egg (any egg, §2.7), overnight oats |
| Meals | oatmeal, pickle soup, ghostly chili, pad thai, cheese pasta, cucumber salad, cheese burger, "The Burger" (deluxe), stuffed burrito, PB & J, salted cod, salted salmon, 3 granola bars, oatmeal cookie |
| Placeable food | cheese pizza, meat pizza (`PizzaBlock`, 4 slices by knife) and their slices |
| Blocks (decor) | salt block, smooth salt block, salt bricks / slab / stairs / wall, mixed salt bricks / slab / stairs / wall, salt pillar, 17 salt lamps (light 15), oat bag, oat bale, cucumber / peanut / ghost pepper crates |
| Blocks (working) | pickling jar, evaporator, salt layer, cheese mold, golden egg, 3 crops + ghost pepper crop, 4 wild plants |
| Other | 16 chef hats, ghost charcoal; Mixed Seeds (not registered upstream; a pack addition since port `-10`, §2.3a) |

### 2.5 Goose golden egg (the one new mechanic, requested)

Untitled Duck Mod geese are Incubation `EggLayer`s through `incubation_additions` (`GooseEntityEggLayerMixin`; the
timer is `eggLayTime`, reset to 6000 + rand(6000)). Incubation's `LayEggInNestGoal` makes a bird walk to an empty nest
when it is about to lay.

- When a goose is due to lay (its egg timer runs out), a **very small chance** (config value, default **1 in 1000**)
  swaps the lay for a Golden Egg placed as a block on the ground (since 2026-10-25; was an item). The timer resets as after a normal lay.
- **The roll happens before the goose starts the walk-to-nest check**, so a goose that wins never goes to a nest; and
  **the golden egg is never laid in a nest** (it is always a ground drop, nests never see it).
- A goose that loses the roll behaves exactly as today (walks to a nest if one is in range, otherwise drops the egg).
- Lives in `development/incubationAdditions` beside the goose mixin; active only when both Untitled Duck Mod and Vintage
  are loaded. Where exactly the egg timer ends and the nest goal starts is read from Incubation's code at build time.
- **Away from walls.** On a winning roll the goose looks for a drop spot before laying: candidate ground spots within
  3 blocks of it, scored by solid blocks around the spot at foot and head height (the 8 neighbours). It lays at the first
  spot with none, else the spot with the fewest; if no candidate is better than where it stands, it lays where it stands
  (so "avoid walls when possible"). Since 2026-10-25 the egg is placed as a block at the best spot other than the
  goose's own (never on the goose, never over a torch, flower or rail); only with no such spot (a closed 1x1 pen) is it
  dropped as an item, at the spot above; the goose is not made to walk
  (no new goal, which also keeps it from touching the nest goal).
- Sound and particles as the goose's normal lay.

### 2.6 Food values (nutrition / saturation modifier, effects)

Raw produce 2 to 3 / 0.2; roasted peanuts 4 to 5 / 0.2 to 0.3 (fast). Pickles 5 to 8 / 0.2 to 0.65 (fast) and Nourishment
0:30 at 60 %, **without** the Dehydrated 0:30 they carry in the mod. Jars 7 to 10 / 0.1 to 0.7 with 5 min Comfort or
Nourishment, bottles 2 to 3 with 1 min. Meals 8 to 14 / 0.7 to 0.85 (Comfort or Nourishment 3 to 8 min); The Burger 15 /
0.95; century egg 11 / 0.8 with Absorption III, Resistance III, Regeneration III and Nausea (no Dehydrated). Granola bars
are "always eat" snacks with Haste and / or Speed. The pack's food types retune these in §4 step 7. Exact values:
`item/ModFoods.java` in the decompile.

### 2.7 Recipes, loot, tags, fluids, advancements

Recipe files after the purges: about 232 of 237 (about 5 gone: gearo recipes, pickled pitcher pod and
plant, Dehydrated brewing; `goblins_piss` is kept with a changed ingredient).

| Set | Notes |
|---|---|
| Crafting | shaped and shapeless; salted cod and salted salmon (2 fish + `#forge:salt` -> 2 salted fish, shapeless); 32 chef hat and 34 salt lamp recipes (craft and re-dye) |
| Pickling jar (`vintagedelight:fermenting`) | 5 pickles (5 of a kind + salt), kimchi (4 cabbage leaves + salt + chili), surströmming (5 raw fish + salt), century egg (**any egg**, `#forge:eggs`, + coal + salt, 3 hours; the sniffer egg input is replaced), overnight oats, 2 vinegar (`#forge:jam_jars` / `#forge:jam_bottles` + `#forge:mushrooms`), leather (rotten flesh, tree bark, salt, vinegar), slime block (plants + salt), 3 froglights, fermented spider eye, 6 named brews (§2.7a; none involves Dehydrated) |
| FD cooking pot | jars (7 filled kinds), curds, cheese pasta, ghostly chili, oatmeal, pad thai, pickle soup, nut milk |
| FD cutting board | oats -> raw oats + straw, wild plants -> seeds + dye, cucumber noodles, cheese and pizza slices |
| Smelting / smoking / campfire | roasted peanut, oat dough -> bread, ghost charcoal, salt bucket |
| Stonecutting | salt decor |
| Create | milling (oats -> raw oats + straw + seeds, wild plants -> seeds + dye) |
| IE | cloche 4, crusher 5, fermenter 3 (ethanol), squeezer 3 (plant oil), less the purged plants |

#### 2.7a What "the brews" are (six)

The pickling jar has 6 recipes in 0.1.6 that output a vanilla `minecraft:potion` carrying a custom name and custom
effects in its NBT (`CustomPotionEffects`, numeric ids of **vanilla** effects). They are not the Dehydrated potions: no
brewing stand step is involved, no Vintage effect appears in any of them, and they are made from a glass bottle plus 5
ingredients in the jar. One (Goblin's Piss) used a gearo berry; **the gearo berry is replaced by a fermented spider eye** (decided 2026-10-23), so all six stay:

| Brew | Jar ingredients | Effects (duration) |
|---|---|---|
| Goblin's Piss | brown mushroom, honey bottle, **fermented spider eye** (was gearo berry), sugar, bone meal; 30 s | Speed III, Weakness II (0:30 each) |
| Berserker's Brew | blaze powder, salt dust, crimson fungus, fermented spider eye, ghast tear | Strength III, Resistance IV (0:30 each), Wither I (0:20) |
| Brashing Tonic | red mushroom, spider eye, sweet berries, sugar, tomato seeds | Regeneration II, Slowness II (0:20) |
| Dragon's Belch | dragon's breath, rabbit's foot, sugar, fire coral, raw oats | Jump Boost III, Levitation I, Nausea I (0:20) |
| Frost Fermenta | ghost pepper seeds, rotten tomato, milk bottle, snowball, kelp | Fire Resistance I, Slowness I (3:00) |
| Oily Oaf | sugar, golden carrot, redstone, pickled onion, glow berries | Haste II, Hunger I (1:00) |

Port notes: the numeric effect ids are read from the recipe NBT; check each against the 1.19.2 vanilla ids in the
port, and confirm none of the six references `vintagedelight:dehydrated`. Dehydrated potions are a different thing
and are not ported (S8); `VTD-27` checks the registry.

- **Chest loot:** the 7 chest tables (`vd_bastion_hoglin_stable`, `vd_ruined_portal`, `vd_village_{desert,plains,
  savanna,snowy,taiga}_house`) and the `mixed_seeds` placeholder are **added to the game and wired to nothing**: the
  jar's 7 `farmersdelight:add_loot_table` modifiers are not shipped and the pack's loot pipeline gets no rule for them.
  Block loot tables ship as 0.1.6 has them, which is not in full: six blocks have none (§2.10). The `add_item` straw modifier on ripe oats stays (it is a crop drop, FD's own rule for
  wheat).
- **Compost:** the mod sets 14 chances at common setup, but the pack's compost engine rebuilds the table, so they are
  rows in Creative Delight's `kubejs_startup/vintagedelight/compost.js` (was `compost_values.js`, moved 2026-10-25, QoL-4; `other`, as Brewin's), with the wild plants at 0.65 and the crates and oat bag at 1.0
  (§4.0, step 7a).
- **Tags for other mods:** Serene Seasons (S16), Create (`blaze_burner_fuel`), C&A, forge nutrients. Diet is not in the
  pack.
- **Moonlight soft fluids:** one per jar content (8 after the gearo purge), so Supplementaries jars take the bottles;
  S10 extends this to every liquid the port adds.
- **Create potato cannon:** projectile types for produce and pickles, less the gearo berry.
- **Advancements:** 0.1.6 ships a set, KOTS 11. They stay in the jar; the pack drops all of them at load (S13).
- **No trades** and **no village houses.**

### 2.8 1.20-only content

Absent in 1.19.2: sniffer egg (century egg input, now any egg), pitcher pod and pitcher plant (pickled pitcher pod and a cutting
recipe, both left out, S3), eye armor trim (the dead `mixed_seeds` table). The pack has no backport of any of them.

### 2.9 KOTS 0.3.1 vs 0.1.6

Same blocks, crops, worldgen and recipes (197 recipe files after its datagen merge). Taken from KOTS: `*_mason_jar` ids
renamed `*_jar` (the separate `mason_jar` item is gone; `empty_mason_jar` stays), Cucumber Dough, mason jar block classes
merged into one `JamBlock`, a separate `FermentingJarItemHandler`, the planting-soil tooltip on ghost pepper seeds. Not
taken: see §2.10; KOTS's lush grass switch (`ConfigurableAlterGroundDecorator`) is taken with the tree (S1).

### 2.10 KOTS 0.3.1 content past the port (evaluated 2026-10-25)

`devmods/checkNewerVersionForContent/vintagedelight-kots-0.3.1.jar` is byte-identical to the KOTS jar read for §2.9.
Compared against the installed port jar `backportedVintage-0.1.6-ce-19.2-03` by item models, blockstates, textures,
lang keys, loot, worldgen, tags, recipes (by type and result) and class names (`tools/jar_diff.py` plus a scratch
inventory script).

| Kind | In KOTS, not in the port | Decision (proposed) |
|---|---|---|
| **Block loot (bug in the port)** | 0.1.6 ships no loot table for 6 blocks, so they drop nothing when broken; KOTS adds them. **Magic Peanut, Smooth Salt Block, Vine Tile**: drop themselves. **Cheese Wheel, Cheese Pizza, Meat Pizza**: drop the slices left (4 to 1 by `bites`) | **carry over** the three self-drops (data; `vintageport #18`). The wheel and pizzas are `PieBlock`s, and FD's own pies drop nothing when broken (FD 1.2.4 and 1.3.4); **owner 2026-10-25: KOTS's slice tables too** (port `-05`) |
| Purged content | Gearo berries (bush, worldgen, bag, jam jar and bottle, 31 textures), Dehydrated (effect, potions, lang), Pickled Pitcher Pod and the pitcher plant cutting | stays purged (S1, S3, S8) |
| Mixed Seeds | `MixedSeedsItem` exists but is an empty class nothing registers | no change to §2.3a |
| Chef hats | worn as armor with 16 `models/armor/*_chef_hat_layer_1` textures; 0.1.6 (the port) wears the item model on the head | skip (code change, same look in play) |
| Wild plant cutting | Wild oats, peanuts, cucumbers, ghost peppers cut into 2 crops; 0.1.6 (the port) gives seeds and dye like FD's wild crops. Stripped magic vine cuts into an oat and tree bark | skip (FD's rule kept; the oat looks like an upstream mistake) |
| Other mods' data | `c:` tags (NeoForge convention), Diet tags, Botany Pots crop recipes, `neoforge:` biome modifiers | skip: Diet and Botany Pots are not in the pack; the rest is format |
| Lang | `tooltip.farmersdelight.nut_milk_bottle` | skip: the port has the same text under FD 1.2.4's key |

Nothing else differs in content: same crops, wild patches, salt patch, machines and recipes once the `*_mason_jar` /
`*_jar` renames of §2.9 are matched.

## 3. Part 2: overlap with what the pack already has

Every row ships. The marker is the text to put in this file, in the JEI / names audit notes and in the review test
`VTD-40`.

### 3.1 Item overlaps

| Vintage item(s) | Already in the pack | Marker |
|---|---|---|
| Salt dust, salt block, salt bucket, 17 salt lamps | The Salt: `salt:salt` (`#forge:salt`, `#forge:salts`), salt block, salt lamp, rock salt ore, clusters, cauldron evaporation, Seasoning JEI tab ([briefs/food_seasoning.md](briefs/food_seasoning.md)) | `*overlaps w/ The Salt*` |
| Salt layer growth, evaporator, beach salt patches | The Salt's cauldron evaporation and crystal growth | `*overlaps w/ The Salt*` |
| Salt bricks, smooth salt, pillar, mixed salt bricks | none | not an overlap |
| Cheese curds, cheese mold, cheese wheel, cheese slice | Brewin' flaxen and scarlet cheese (keg -> unripe wheel -> ripens -> 4 wedges, `#forge:cheese`) | `*overlaps w/ Brewin' cheese*` |
| Kimchi | Brewin' kimchi (keg), already canned (`canned_kimchi`) | `*overlaps w/ Brewin' kimchi*` |
| Cheese Burger, The Burger | Delightful Cheeseburger, Deluxe Cheeseburger | `*overlaps w/ Delightful burgers*` |
| PB & J, Nut Mash (jar, bottle) | Delightful Nut Butter and Jelly Sandwich, Nut Butter Bottle (`#forge:nuts` = acorn today) | `*overlaps w/ Delightful nut butter*` |
| Sweet Berry Jam, Glow Berry Jam (jar, bottle) | Delightful Jelly Bottle (`#forge:jam`), Glow Jelly Bottle; Fruits Delight jellies | `*overlaps w/ Delightful and Fruits Delight jellies*` |
| Honey Jar | pack honey fluid, honey bottle, honey blocks | `*overlaps w/ honey bottle and blocks*` |
| Vinegar (bottle, jar) | none; `dishes/ingredients #6` planned a KubeJS vinegar | not an overlap: it becomes the pack vinegar (S9) |
| Pickle, pickled beetroot / egg / onion / pepper | Brewin' Pickled Pickles (sea pickles, canned) | different foods; "Pickle" vs "Pickled Pickles" is a names check |
| Oat dough, cucumber dough | Create dough | `*overlaps w/ Create dough*`. **Oat dough is a `#forge:dough`** (owner, 2026-10-23), so it works in every dough recipe; cucumber dough stays a named ingredient. 2026-10-28 (TODO `baking/baked-goods #29`, #31): no furnace, smoker or campfire bread; oat dough bakes to bread in the oven with any dough (1 / 3 / 6 on a baking sheet). Overnight oats also craft: 3 raw oats, a fruit slice, a berry, milk, bowl |
| Cheese pizza, meat pizza | Brewin' Pizza (placeable, sliced) | **CLOSED 2026-11-02 (owner): all pizzas are in the pack, none overlap** (also covered by the pizza rule, veggies_delight.md 3.3 R5) |
| Cheese pasta, cucumber noodles | FD pasta dishes, `#forge:pasta` | `*overlaps w/ FD pasta*` (check the noodles in the tag cannot stand in for cooked pasta) |
| Salted Cod, Salted Salmon | The Salt's salting (`Salted` tag on any food, Create mixing pack-wide planned) | `*overlaps w/ The Salt's salting*` |
| Chef hats (16) | Simple Hats `simplehats:chefshat` (cosmetic, from hat bags) | `*overlaps w/ Simple Hats chef's hat*` **settled: Vintage's replaces it** (remove Simple Hats' hat from hat bag loot, recipes and the item dict / stack-size rows) |
| Organic mash | FD Organic Compost | not an overlap: it is an input to FD Organic Compost (§2.3a) |
| Mixed seeds | none | not an item upstream; decision postponed (§2.3a) |
| Crates and bags (cucumber, peanut, ghost pepper, oat bag, oat bale) | FD / Rustic crates, hay bale | `*overlaps w/ FD crates*` for the crate pattern only (new produce) |
| Ghost charcoal | charcoal, Create blaze burner fuels | not an overlap |

No overlap for oats, peanuts, cucumbers, ghost peppers, the pickling jar, pickles or the golden egg.

### 3.2 System overlaps

| Pack system | What Vintage brings | Handling |
|---|---|---|
| Wild crop placement ([crops.md](crops.md) §2.8, `wild_crops.json`) | Own modifiers for oats, peanuts, cucumbers | Rules in `wild_crops.json`; Vintage's modifiers switched off (S5) |
| Nether worldgen ([nether_worldgen.md](nether_worldgen.md)) | Wild ghost peppers in every Nether biome | Crimson and warped forest only, nylium (S5) |
| IW soils ([crops.md](crops.md) §2.8.1) | Oat and peanut patches test `#minecraft:dirt`; cucumber and salt list exact blocks | All dirt soils (S6); `tools/gen_soil_compat.py` |
| Crop parity ([crops.md](crops.md) §2.6) | Own 7 -> 5 pick on cucumber and ghost pepper | Kept; marked with tests `VTD-02`, `VTD-03` (§2.1) |
| Seasons (`farming/crops #5`) | Ships tags | Shipped as is; overhaul due (S16) |
| Fermenting stations: Brewin' keg, Vinery barrel, IE fermenter | A fourth: solid-in, solid-out | **Pickling jar for all pickling, keg for beer fluids, cask for wine fluids that age** (S7). Brewin' keg recipes that pickle (pickled pickles, kimchi) move to the jar; this is a retarget of existing recipes, not new ones. The wider keg / cask fluid audit goes into [alcohol.md](alcohol.md) |
| Potions ([potions.md](potions.md)) | Dehydrated mixes, 6 custom-effect brews | Dehydrated not ported (S8). The 6 brews (§2.7a) ship as the mod makes them; no pack potion fluid or mixer understands their NBT, which `VTD-23` records |
| Cheese (`dishes/ingredients #9`) | Second cheese line | Ships; marked (§3.1) |
| Salt (`food/seasoning #1`) | Second salt line | Ships; marked. Step 6 of that row (merge) waits on the review |
| Vinegar (`dishes/ingredients #6`) | The vinegar that row planned | Source = Vintage's (S9) |
| Jar fluids ([mixed_jars.md](mixed_jars.md)) | Soft fluids with their own colours | All jarable (S10) |
| Chest loot ([loot.md](loot.md)) | 7 GLM injections | Not shipped; tables unwired (§2.7) |
| Mob food | Static edits to chicken, pig, parrot, villager | Accepted; audit due |
| Advancements | A set | Dropped at load |

### 3.3 How the duplicates get settled

**Review closed 2026-11-02 (owner, `ports/vintageport #6`).** Pizzas: no overlap. Salt: merge verified complete (item_merges.md 2.9; one pink salt, The Salt's block, bucket removed). Salted Cod and Salted Salmon: removed (`vintageport #11`, verified in `removedItems` and the recipe removals). Cheese, jams, nut mash, pickles, kimchi and chef hats were settled by the 2026-11-01 and earlier decisions (section 5 and the table above). Only the magic vine tree set (`VTD-47`) still needs play. The text below is the original method, kept for the record.

Nothing is decided in this plan. After the first session with the port in game, each marked pair in §3.1 is looked at
once (`VTD-40`): both versions in JEI, in the recipes that take the shared tag (`#forge:salt`, `#forge:cheese`,
`#forge:jam`), in cans and in the food types. The outcome per pair is "keep both", "keep one" or "merge" and is written
back to this table; removal of a side is a separate change at that point.

### 3.4 Overlap with the Veggies Delight port

- **22 Veggies recipes wait on Vintage** (`veggiesdelight/recipes/compat/vintagedelight/`, each gated by
  `forge:mod_loaded vintagedelight`). With Vintage ported they become live:
  - 12 Veggies dishes given a `#forge:cheese` version;
  - 3 dishes (+ the mhadjeb) that add a ghost pepper;
  - 6 Vintage dishes given Veggies ingredients (pad thai and ghostly chili with garlic, pickle soup with turnip, cheese
    burger, deluxe burger and stuffed burrito with the vegetarian patty or smoked bell pepper). All six Vintage dishes
     exist now (S2). Smoked bell pepper is dropped by Veggies D2; every recipe that took it takes **any Rustic roasted
     bell pepper** (any colour, as the Veggies plan decided), through a tag, not a Vintage item;
  - fermented garlic honey (`vintagedelight:fermenting`: garlic cloves + honey bottle). **It is made in the pickling
    jar**, which replaces Veggies D6.
- The Veggies crops (bell pepper, broccoli, cauliflower, garlic, sweet potato, turnip, zucchini) are marked for the
  crops loot tables with the Vintage ones (§4 step 16).
- Nothing in Vintage needs a Veggies item; no Vintage item has a Veggies name.

## 4. Part 3: what Vintage still needs to match the other crops and foods

### 4.0 Build status (2026-10-23)

| Step | State | Where / note |
|---|---|---|
| 1 Port | BUILT | `development/creativeDelight/members/vintage`, jar `backportedVintage-0.1.6-ce-19.2-01.jar` in `mods/`. 134 items, 68 blocks. Java from the 0.1.6 decompile, resources from the jar through `tools/port_resources.py` (MIT notice kept). §7 string check: no `dehydrated`, `gearo`, `pitcher` or `sniffer` in the jar. Port notes below |
| 2 Ids, tags | BUILT | 134 keys in `item_dict.js` (`vintagedelight_kimchi`, `vintagedelight_salt` prefixed); `i.vinegar` = vinegar bottle. `cookingtags.js`: `#forge:crops/*`, `#forge:seeds/*`, `#forge:storage_blocks/*` for the 4 crops; oat dough in `#forge:dough` (owner, 2026-10-23; was taken out), cucumber dough not. Cucumber noodles stay in `#forge:pasta` for `VTD-40` |
| 3 Crop parity | soil tag BUILT; crops.md rows OPEN | `#vintagedelight:ghost_pepper_soil` = soul soil, rich soul soil, nulch (`blocktags.js`). Quark replant interplay is `VTD-02` / `VTD-03` |
| 4 Wild crops | BUILT | 3 rules in `wild_crops.json` (oats cold and temperate grassy, peanuts warm dry grassy, cucumbers wet / swamp and cold and temperate grassy; rules changed 2026-10-23); ghost peppers: `literal` biome modifier in `nether_worldgen_rules.json` (crimson, warped forest). `test_wild_crops.sh` passes; `crops_by_biome.md` regenerated; `gen_soil_compat.py` adds the salt patch |
| 5 Crop docs, WTHIT | DONE 2026-11-01 | 10-30 dump: the four crops are WTHIT-native, Magic Peanut skipped (sapling), 0 Vintage UNKNOWN; crops.md §2.1 rows written (oats, peanuts; picked crops cucumber, ghost pepper, zucchini) |
| 6 Seasons | BUILT | the jar's tags, as shipped |
| 7 Food types | BUILT; catalog rerun 2026-11-01: Ghost Charcoal (edible since vintageport #13) typed `special`; the three new jam jars show as not installed only because the 10-30 dump predates port `-09` | 54 foods typed by hand in `food_types.json` (existing types only): produce raw veggie / fruit, pickles and kimchi cooked veggie (Brewin's precedent), roasted peanut cooked fruit, salted fish and surströmming cooked fish, the two eggs egg, cheese slice dairy, cookies / granola / honey roasted peanuts sweet, burgers / PB & J / burrito / pizza slices handheld, cucumber salad salad, pickle soup / ghostly chili / oatmeal soup (soup band and Comfort), cheese pasta / pad thai / overnight oats meal, nut milk and honey jar drink, doughs / noodles / curds / vinegar / every jam, sauce, relish and nut mash jar and bottle ingredient. `food_catalog.py` flags all 54 as not installed until a dump has them. No Dehydrated on any food (in the jar). Veggies: all 64 foods were already typed |
| 7a Compost | BUILT | CD `kubejs_startup/vintagedelight/compost.js` (was `compost_values.js`, QoL-4): the port's 14 chances (`other`), wild oats / peanuts / cucumbers / ghost peppers 0.65 (`crops`), 3 crates + oat bag 1.0 (`crates`); the Veggies wild crops and crates the same. Startup reload + `/reload` |
| 8 Cans | BUILT; rerun 2026-11-01 on the 10-30 dump: no change | `gen_can_types.py`: 16 Vintage cans (cucumber, ghost pepper, pickled beetroot / onion / pepper, roasted peanuts, Surströmming, cheese slices, oatmeal cookies, cheese pasta, pad thai; cucumber salad and the 3 soups by type) and 4 shared sources (Vintage kimchi and pickle in Brewin's cans, salted cod and salmon in the cooked fish cans), skipped through `AWAIT_DUMP` until a dump has them. The vinegar additive ran (66 cans). Veggies: 24 cans generated. Rows in [can_parity.md](can_parity.md), CP-27 to CP-31 |
| 9 Cooking rules | BUILT | (Since 2026-10-25 the grid-meal moves, burrito wrap and heatless routes are Creative Delight QoL-5: `kubejs/data/creativedelight/kubejs/vintagedelight/cooking.js`; the burger rule stays in `cooking.js`.) `cooking.js`: burger rule for both burgers (shaped on the cheese slice so Delightful's wedge cheeseburgers keep their grids, beef or vegetarian patty; assemblies start on the slice, the deluxe on the patty), stuffed burrito on `#forge:tortilla` + assembly (ghost pepper or roasted bell pepper first), PB & J and cucumber salad `_manual_only`, heatless cheese pasta (pasta + 3 cheese + bowl), pad thai (2 cooked chicken cuts, roasted peanut) and pickle soup (bone broth, 2 pickles, baked potato), oat dough's furnace bread removed (3 raw oats -> bread stays); the Veggies compat grid recipes folded in (§3.4). `baking.js`: oatmeal cookie oven only (cookie dough + 2 raw oats -> 12). No rule applies: pizzas (one `#forge:dough` each already; oven.md has no pizza rule), granola bars, oatmeal (no cooked oat), ghostly chili (Nether, as blazing chili), jams, curds, nut milk |
| 10, 11 Salt, cheese | BUILT (both lines ship; salt merged 2026-10-26: Vintage's dust and The Salt's block stay, salt bucket removed, [item_merges.md](item_merges.md) 2.9) | `#forge:salt` holds salt dust from the jar; JEI info / WTHIT slices line OPEN |
| 12 Pickling jar | BUILT; remainder buffer OPEN | NCH `R.vdPickling`. `alcohol.js`: Brewin' kimchi and pickled pickles moved from the keg (honey 250 mB -> honey bottle). Fermented garlic honey: the Veggies port ships Vintage's jar recipe (Veggies `-02`), its pot route is gone. The jar keeps no leftover bottles (a honey bottle input loses its glass bottle); a remainder-buffer slot is a TODO row |
| 13 Potions | BUILT | no Dehydrated class, registry entry or brewing mix in the port; the 6 brews use vanilla effect ids only (checked); `VTD-23` records the mixers |
| 14 Placeables, stacks | DONE 2026-11-01 | placeables: the 9 Vintage placeables already in `placeable.json` (the file is stale for other mods only); stacks: Vintage foods by type, plus new rules in `gen_stack_sizes.py`: the 16 chef hats 8 (the simple hat rule), Cheese Pizza, Meat Pizza and Cheese Wheel 8 (placed cake / feast) |
| 15 Jar fluids | BUILT | Pack jams with their own Forge fluid (9: ancient fruit, banana, cherry, green grape, pepper, raspberry, red grape, salmonberry, strawberry): the soft fluid file names that fluid in `equivalent_fluids`, so Moonlight does not auto-create a second soft fluid with the same id (the duplicate key that dropped the jar container, SQ-VP-06, fixed 2026-11-03; `src/own`, `src/main/resources`, `tools/gen_pack_jams.py`); jar and tank share one soft fluid. 8 soft fluids in the jar (1.19.2 texture paths, `*_jar` ids); nut milk: `kubejs/data/creativeengineering/moonlight/soft_fluids/nut_milk.json`; pickle soup, ghostly chili, oatmeal from `gen_soup_soft_fluids.py` (which now keeps the hand-written drink fluids). Veggies: carrot juice, dandelion juice, turnip water (with their tank fluids as equivalents) and fermented garlic honey |
| 16 Loot | BUILT (unwired) | 8 chest tables ship, no GLM; straw modifier kept. Crops loot tables row in TODO |
| 17 Machines | BUILT | Create milling, IE recipes as shipped (IE gated) |
| 18 Mob audit | BUILT | Vintage and Veggies both edit `Chicken.FOOD_ITEMS`, `Pig.FOOD_ITEMS`, `Villager.WANTED_ITEMS` (and Vintage `Parrot.TAME_FOOD`); both combine with the current value, so load order does not matter. The pack has no other edit of these fields; Quark troughs read the chicken / pig temptation, so they take the new foods too. Baby mobs: no food hook. Result: no conflict |
| 19 Goose golden egg | BUILT | `incubationAdditions-1.1.0.jar` in `mods/`; [nests.md](nests.md) 3.11, NE-15, NE-16. Deviations: the roll is at the start of the 400-tick nest window (the only point before the nest walk), drop spots must be in the goose's line of sight, the roll is not saved across a reload |
| 19a Mixed Seeds | BUILT 2026-11-01 (port `-10`, not installed) | §2.3a |
| 20 Chef hats | BUILT | Simple Hats' hat: bag weight 0 (`config/simplehats.json`, copy in `configs_edited/`), in `removed_items.js`, stack row gone. The 16 Vintage hats' stack rows come with step 14 |
| 21 UI | PARTLY 2026-11-01 | JEI coverage: no Vintage gap (the fermenting type has its category; 4 info pages; better screens are `jei #15`); BlockRunner: 18 blocks tiered in `blocktags.js` (crates wood, oat bag and bale natural smooth, salt bricks constructed brick, smooth salt and pillar constructed smooth, the salt blocks natural smooth, pizzas confectionery; sim 0 overlaps), the 18 left are jars, machines and plants; missing names: none. **Needs the game:** the name audit and WTHIT icons read client dumps of 2026-10-20 (no Vintage): `/dumpnames` client side and the WTHIT icon dump |
| 22 Release | changelog BUILT | `unreleased.md` bullets; `build_deploy.py` `IN_TREE_MODS` now lists the Veggies and Vintage ports |

Port notes (what the jar does differently from 0.1.6, all in `development/creativeDelight/members/vintage/README.md`):

- Jars: one item per jar, the block's id (`sweet_berry_jam_jar`, `glow_berry_jam_jar`, `apple_sauce_jar`, ...,
  `empty_mason_jar`); KOTS 0.3.1 still registered `*_mason_jar` items beside them. A plain click with a filled jar
  eats it; the empty jar places without sneaking.
- Pickling Jar: KOTS's item handler (inputs top, container sides, outputs bottom); the container slot is used only
  when the recipe names one (upstream always took one); the recipe type is now registered.
- Cucumber Dough as KOTS: cucumber + `#forge:dough` -> 2, knife -> cucumber noodles (the direct cut is gone).
- Wild ghost pepper: nylium or the soil tag (upstream also took dirt). Wild oats, peanuts, cucumbers: any
  `#minecraft:dirt` (`DirtWildCropBlock`); the cucumber patch tests `#minecraft:dirt` (sand out).
- Upstream id bugs fixed: Frost Fermenta's `minecraft:rotten_tomato` -> FD's; the pickled pepper potato-cannon type;
  `#forge:protein` named an undefined tag (now optional); the jam advancement's tag.
- Mixed Seeds: not registered upstream; registered by the port since `-10` as a pack addition (§2.3a). The table ships unwired.

### 4.0b Built 2026-10-25 (TODO ports/vintageport #5, #9 to #18)

Port `0.1.6-ce-19.2-04` (built, not in `mods/`): no golden egg on the Magic Peanut tree (`VineTrunkPlacer`); JEI jar
time in seconds under a minute and "1min 30s" for part minutes (`FermentingCategory`); a take-only leftover slot (9)
on the Pickling Jar for ingredient leftovers (empty bottles, bowls, a potion's bottle), pulled from the bottom, kept in
saves (older 9-slot saves are widened) and drops, port config `[pickling_jar] remainderBuffer` / `stallWhenFull` (both
on); `port_resources.py` writes the honey_vd soft fluid for the honey jar, the KOTS self-drop loot of Magic Peanut,
Smooth Salt Block and Vine Tile, and now also the two fixes that had been hand-made in the resources (`biome_tag` key,
"Fertile Soul Soil" lang). Port `-05` (2026-10-25): the same script copies KOTS's Cheese Wheel, Cheese Pizza and
Meat Pizza tables (the slices left: 4 at `bites` 0 down to 1 at 3); no other port block lacks a table.

Creative Delight scripts (the first member rows, [creative_delight.md](creative_delight.md) 10.3): server
`kubejs/data/creativedelight/kubejs/vintagedelight/recipes.js` (slime block pickling removed; Berserker's Brew and
leather rebuilt on `#forge:salt` with `R.vdPickling`; salted cod / salmon crafts removed; wild ghost pepper 2 seeds by
Create milling and crushing, 3 by the IE Crusher), `vintagedelight/loot.js` (Vintage crop crates and bags through
`L.pack`, the crops of the port's 7 chest tables joined to the vanilla tables at a small share), `veggiesdelight/loot.js`
(Rustic bell pepper crates and seed bag), `create/slime.js` (`cooking/kitchen #12`); startup
`development/creativeDelight/src/main/resources/kubejs_startup/vintagedelight/vintage_items.js` (salted cod / salmon
into `global.removedItems`; ghost charcoal edible: Wither I 0:05, Fire Resistance 3:00, no hunger, always edible).
These load only with the Creative Delight jar and its patcherMod installed (they return early without `global.CD`).
`gen_can_types.py` and `food_types.json` no longer name the salted fish. Tests VTD-54, VTD-65 to VTD-74.

Port `-06` (2026-10-25, vintageport #7; carries `-05`): ghost pepper seeds and the wild ghost pepper stay only on their
soils. Forge's `BushBlock.canSurvive` asked the soil's `canSustainPlant`, and farmland sustains every crop (grass and
dirt every plains plant), so `mayPlaceOn` alone never kept them off farmland; both blocks now check `mayPlaceOn` in
`canSurvive` (crop: `#vintagedelight:ghost_pepper_soil` plus the light check; wild: nylium or that tag). Peppers
already on farmland pop off at the next block update. Test VTD-75.

Right-click harvest (vintageport #8, investigated 2026-10-25): Quark's harvesting search takes every modded
`CropBlock`, and its `RightClickBlock` handler runs before the block's `use`, so a ripe cucumber, ghost pepper or
zucchini was broken for its loot and replanted at stage 0: cucumber / pepper 1 fruit + 0 to 3 seeds (one seed kept
back as the replant), zucchini 0 to 3 zucchini; the own pick (1 to 2 cucumbers, 1 pepper, 1 to 2 zucchini; back to 5,
5 and 4) never ran. Kept the own pick (it regrows in 2 or 3 stages, a berry-bush crop): patcherMod
`QuarkOwnPickMixin` on `SimpleHarvestModule.getAction` gives blocks in `#creativeengineering:crops/own_pick` Quark's
berry bush treatment (empty hand or 1x1 hoe: Quark steps aside and the block's pick runs; hoe area: Quark clicks each).
Tag filled in the CD trees (`vintagedelight/tags.js`, `veggiesdelight/tags.js`). Quark's villager harvest still
replants them at stage 0 (villagers only farm farmland, so not ghost peppers). Test VTD-76.

### 4.0a Systems not yet updated, both ports (audit 2026-10-23)

Read from the files each system uses, not from the plans. The game dumps (`kubejs/exported/`) were last taken
2026-10-22 21:28 with Veggies loaded and **never with Vintage loaded**. So the Veggies gaps below can be closed offline
(the generators just have not been rerun on that dump); every Vintage gap needs a `/dumpall` first.

| System | Veggies | Vintage | Closes with |
|---|---|---|---|
| Food types (`food_types.json`) | 64 foods typed by hand | 54 typed by hand (2026-10-23) | done |
| Food catalog (`food_catalog.py`, `exported/food/catalog.json`) | rerun 2026-10-23 from the 10-22 dump | not in the dump | dump + rerun (Vintage) |
| Food values (type values, Spice of Life) | follow the catalog | follow the types | after the two rows above |
| Cans (`gen_can_types.py`) | 24 generated 2026-10-23 | 16 cans + 4 shared sources configured, waiting (`AWAIT_DUMP`); vinegar additive ran | dump, empty `AWAIT_DUMP`, rerun, restart |
| Jar fluids (soft fluids) | 5 soups, 3 juices and garlic honey done (2026-10-23) | 8 jar contents, nut milk and the 3 soups done (2026-10-23) | done |
| Jar families (solid foods, `jartags.js`) | none | none (candidates: oatmeal cookie into `cookies`, granola bars, roasted and honey roasted peanuts) | hand list |
| Stack sizes (`gen_stack_sizes.py`) | in the dump, generator not rerun (0 Veggies rows in `stack_sizes.json`) | not in the dump (16 chef hats, jars, pizzas) | rerun / dump + rerun |
| Placeables (`placeable_catalog.py`) | in the dump; `--check` says OUT OF DATE | not in the dump (jars, pizzas, cheese mold, golden egg) | rerun / dump + rerun; then placeable tooltips, Display Delight icons |
| Compost | rules cover produce, seeds, cooked veggies; wild crops and crates added as rows (2026-10-23) | the port's 14 chances, wild crops and crates added as rows (2026-10-23) | `/dumpcompost` to confirm |
| Crop growth (WTHIT) | covered (`crop_growth_coverage.py`: 0 UNKNOWN) | not in the audit | `/dumpcropgrowth` + rerun |
| crops.md §2.1 rows (replant, hoe area, soils) | open | open (oats, peanuts, cucumbers, ghost peppers; zucchini row) | hand edit |
| Seasons | shipped tags, overhaul due | shipped tags, overhaul due | `farming/crops #5` |
| Cooking rules (heatless, `_manual_only`, mixer) | BUILT; its Vintage compat recipes folded in 2026-10-23 | BUILT 2026-10-23 (step 9) | done |
| Baking / oven | BUILT; sweet potato pie filling can added 2026-10-23 | oatmeal cookie in the oven, oat dough furnace bread removed; pizzas and granola have no rule | done |
| Crops loot tables (single, crates) | open | open | `vintageport #5` |
| Chest loot, trades, houses | BUILT | none by design (tables unwired, S11; no trades upstream) | n/a |
| Names audit, JEI coverage, BlockRunner tiers, WTHIT icons | names dump has Veggies; audits not rerun; `wthit_icons.json` is from 10-20 | not in any dump | `/dumpall` + the UI generators |
| Overlap and merge review | pinned (`veggiesport #2`) | pinned (`vintageport #6`, `VTD-40`) | after play |
| Pickling Jar remainders | n/a | open: honey bottles lose their glass bottle (`vintageport #4`) | port or mixin |
| Potion handling of the 6 brews | n/a | no mixer reads their NBT (`VTD-23`) | record only |

**Vintage column, 2026-11-01 (`vintageport #2`, on the 2026-10-30 dumps):** food catalog, cans, stack sizes, placeables,
crop growth, crops.md rows and BlockRunner tiers done (§4.0 steps 5, 7, 8, 14, 21). Compost confirmed by `/dumpcompost`:
produce, seeds, crates and wild crops are in; dishes, jams, cheese and salt are not (as other mods' dishes); every dough
(oat, cucumber, Create's and the rest) composts at 0.85 since 2026-11-01 (owner; `doughs` rule, [composting.md](composting.md));
the cucumber (Veggie) noodles are not a dough and stay out. Still open: the name audit and WTHIT icons (client dumps of 2026-10-20 predate Vintage),
jar families (hand list).

Steps that are the same as the Veggies port are only named; see [veggies_delight.md](veggies_delight.md) §4 for the
detail.

1. **Port (new jar).** `development/creativeDelight/members/vintage`, mod id `vintagedelight`, root `settings.gradle`, `installToMods`,
   `mod_sides.md` row (both sides). 1.20.1 -> 1.19.2: block `Material`, `CreativeModeTab` subclass, recipe JSON without
   `category`, `getResultItem()` without `RegistryAccess`, JEI 11 API, remove the purged classes (the gearo bush, the
   Dehydrated effect and its brewing hooks) and the 1.20-only items; keep the magic vine tree line
   (Magic Peanut, `VineTrunkPlacer`, magic vine wood, Vine Net and Tiles, Lush Grass, its hoe route and both mixins). Rename the
   jar to Pickling Jar in lang. Take KOTS's jar ids and fixes. Keep the mod's advancements.
2. **Ids in one place.** Every kept item in `item_dict.js`; blocks in the block dict; `i.vinegar` points at Vintage's.
   Seed / crop / vegetable tags for the new crops as in the Veggies port.
3. **Crop parity.** Oats and peanuts: rows in crops.md §2.1, folded into Quark replant and hoe area (`VTD-01`).
   Cucumber and ghost pepper: keep the 7 -> 5 pick, keep them off Quark's list, and record in `VTD-02` / `VTD-03`
   whether Quark's generic `CropBlock` harvest fires first. Zucchini gets its row. Ghost pepper soil tag (§2.1).
4. **Wild crops.** Oats, peanuts, cucumbers in `wild_crops.json`; wild ghost peppers in `nether_worldgen_rules.json`
   (crimson, warped forest); salt patches as shipped; `gen_soil_compat.py`; regenerate
   [crops_by_biome.md](crops_by_biome.md); `tools/test_wild_crops.sh`.
5. **Crop docs, audit, WTHIT.** Harvest / replant audit, `tools/crop_growth_coverage.py` after `/dumpcropgrowth`
   (crops age 7).
6. **Seasons.** Ship the tags; the overhaul stays open in `farming/crops #5`.
7. **Food types and values.** `/dumpfood`, `python3 tools/food_catalog.py`; pickles likely need a "preserved" or
   "fermented" type decision; tune against type bands, not Vintage's numbers. Remove Dehydrated from every food.
8. **Cans.** `tools/gen_can_types.py`: new veggies, pickles; **vinegar becomes the additive** planned in
   `dishes/ingredients #6` step 2 (raw veggie, fruit, meat, fish and salad cans).
9. **Cooking rules** ([recipes_cooking.md](recipes_cooking.md)): vinegar in cooking where a food rule defines it; oat
   dough and bread by the pack's bread chain; pizzas to the oven ([oven.md](oven.md)); heatless grid routes and
   `_manual_only` ids for pot meals; granola bars and cookies by the sweets rules; everything through `global.NCH`.
   No recipe is added that Vintage or a rule does not define.
10. **Salt.** Both lines ship (S2). `#forge:salt` holds both salts; by-id recipes keep their ids. The merge step of
    `food/seasoning #1` waits on `VTD-40`.
11. **Cheese.** Both lines ship; JEI info and the WTHIT slices line for the wheel and mold. `dishes/ingredients #9` stays
    open until `VTD-40`.
12. **Pickling jar.** JEI category is the mod's; add the jar to the pack's remainder-buffer check (it has a container
    slot); hopper sides documented; retarget the Brewin' pickling recipes to it (§3.2); fermented garlic honey from the
    Veggies port. Keg / cask split recorded in [alcohol.md](alcohol.md).
13. **Potions and effects.** Dehydrated is not ported: nothing to add to [potions.md](potions.md), JEED or the effect
    export. Confirm no Dehydrated class, registry entry, `PotionBrewing` mix or `IBrewingRecipe` survives in the port. Check the 6 brews (§2.7a) against `PotionMixDedup` and the mixers and note the result (`VTD-23`).
14. **Placeables and stack sizes.** `/dumpplaceable` + `placeable_catalog.py` for mason jars (1 to 4 per block),
    pizzas, cheese mold, golden egg; `gen_stack_sizes.py` rules for bottles, jars and pizzas per the stack size policy.
15. **Jar fluids.** Every liquid the port adds (the 8 jar contents and the nut milk bottle) gets a file under
    `kubejs/data/creativeengineering/moonlight/soft_fluids/`, colours by the pack's jar rules ([mixed_jars.md](mixed_jars.md)),
    WTHIT contents line. `VTD-26` checks each one in a Supplementaries jar.
16. **Loot.** The 8 loot tables ship unwired. **Crops loot tables TODO:** add, as a single item and as a crate drop,
    the new crops: oats (oat bag / bale), peanuts, cucumbers, ghost peppers (Vintage) and bell pepper, broccoli,
    cauliflower, garlic, sweet potato, turnip, zucchini (Veggies); the crate pairs come from each mod's 3x3 recipes.
    Straw modifier kept.
17. **Machines.** Keep Vintage's Create milling and IE cloche / crusher / fermenter / squeezer recipes, less the
    purged plants. No new grinding or oil routes.
18. **Mob audit.** Check the accepted mob food (chicken, parrot, pig, villager) against
    [mob_block_tweaks.md](mob_block_tweaks.md) and [baby_mobs.md](baby_mobs.md); note the result.
19. **Goose golden egg** (§2.5) in `incubationAdditions`; chance 1 in 1000 as a config value; wall-avoiding drop spot;
    Golden Egg block in the dict.
19a. **Mixed Seeds:** BUILT 2026-11-01 (§2.3a).
20. **Chef hats.** Remove `simplehats:chefshat` from hat bags, recipes, `item_dict.js` and `stack_sizes.json`; Vintage's
    16 hats take the stack size rows and a names check.
21. **UI.** Names audit ("Pickle" vs "Pickled Pickles", "Salt Layer", the Pickling Jar name, the Cask name used in alcohol.md), JEI coverage,
    BlockRunner tiers for salt decor (`tools/blockrunner_untagged_blocks.json`), WTHIT icons.
22. **Release.** `/dumpall` and the generators in order, changelog bullets under the farming and food headings, a
    `plan_*.md` row; flip the Veggies plan's 22 compat recipes on (§3.4).

### 4.0c Oat crop look (2026-10-31)

Oats match the pack's wheat, which the enabled resource pack `Fancy Crops v1.2.zip` reshapes: stages 0 to 6 keep the
vanilla crop model with bushier textures, stage 7 is a two-block-tall six-plane model (`tall_wheat_crop`) picked at
random per position from three textures (7, 7a, 7b), so a ripe field has three heights and shapes.

- Overrides in `kubejs/assets/vintagedelight/` (pack level, not the port jar: the shapes are Fancy Crops' work, whose
  licence is not stated, so the derived files stay beside the pack that uses the resource pack):
  `blockstates/oat_crop.json` (age 7 = a weighted-equal list of `oat_stage7`, `oat_stage7a`, `oat_stage7b`),
  `models/block/tall_oat_crop.json` (Fancy Crops' tall model, unchanged), `models/block/oat_stage7{,a,b}.json`,
  `textures/block/oat_stage0..6, 7, 7a, 7b.png`. Stages 0 to 6 keep the port's own models (vanilla `block/crop`).
- Textures: Fancy Crops' `wheat_stageN` shapes recoloured with the port's `oat_stageN` colours, stage by stage (both
  have ages 0 to 7). Mapping by luminance rank, pixel-weighted: the wheat texture's colours sorted by brightness take
  the oat colour at the same brightness quantile, alpha kept. The three ripe variants all take `oat_stage7`'s colours.
  Redo with a short PIL script by this rule if either texture set changes (none is kept in tools/).
- Unused now: the port's own `oat_stage7.png` (16x20) and `tall_crop` model stay in the jar, shadowed.
- Test VTD-78.

## 5. Open questions

| # | Question | Default if not answered |
|---|---|---|
| Q1 | _(closed 2026-10-23: the tree is back for testing, so the tiles and Lush Grass have a source)_ | - |
| Q2 | _(closed 2026-10-23: the Magic Peanut grows the tree again, S1)_ | - |

Also confirm the golden egg replaces the normal egg on a winning roll (it does in §2.5) rather than being laid in
addition.

### Planned from the scratchpad (2026-10-24)

Briefs: [dishes_jams](briefs/dishes_jams.md), [dishes_pickles-and-ferments](briefs/dishes_pickles-and-ferments.md),
[ports_vintageport](briefs/ports_vintageport.md), [alcohol_pack](briefs/alcohol_pack.md). Nothing is built.

- **Jams** ([TODO](todo/TODO.md) `dishes/jams #1`): every jam becomes Fruits Delight's jam item with Vintage's jam block, in
  mason jars; mason jars take glass panes; a new empty-jar texture; jams recolour per fruit. Replaces S2's "keep both" for
  the Vintage jams (§3.1). Open: which jar item stays (recommend the mason jar).
  Hearth and Harvest (survey 2026-10-30, [hearth_and_harvest.md](hearth_and_harvest.md) 2.1) adds raspberry, cherry and
  grape jams and pickled cabbage, carrots and potatoes to these families; its jar, five other jams, two pickles,
  peanut butter, PB & J and roasted peanuts duplicate Vintage's and are not ported.
- **Nut mash** (`merges/dishes #7`): merged with Delightful's nut butter. Decided 2026-11-02 (owner): nut mash survives (acorn or peanut, mason jar only), nut butter dropped; the sandwich becomes Nut Mash and Jam Sandwich.
- **Pickled sea pickle** (`dishes/pickles-and-ferments #1`): Brewin's Pickled Pickles is removed; a recoloured pickle is the
  new Pickled Sea Pickle, crafted without honey. Open: the craft (recommend 5 sea pickles + salt).
- **Real pickles only** (`pickles-and-ferments #2`): pickle recipes (squid and pickles, sea pickle roll, others) take
  `vintagedelight:pickle`, never pickled pickles or raw sea pickles; squid and pickles swaps the onion for a pickle.
- **Pickle soup** (`pickles-and-ferments #3`): the soup takes a pickle or another pickled veggie, plus a veggie variant.
  Open: the exact ingredients.
- **Enzyme and vinegar** (`pickles-and-ferments #5`, `#6`): mushrooms crushed or milled into an enzyme; vinegar becomes
  3 apple mash + an enzyme (replaces the jam + mushroom recipes of §2.7; organic mash needs a new source). Open: which
  mushrooms and machines.
- **Foul Taste** (`pickles-and-ferments #7`): foods with vinegar added carry a Foul Taste effect. Open: the effect (none
  exists yet) and the foods.
- **Kimchi** (`pickles-and-ferments #8`): Vintage's kimchi wins over Brewin's (the jar copy of the Brewin' craft goes), and any
  pickleable veggie works as the base. Replaces the "keep both, `VTD-40`" state for kimchi.
  2026-11-02 (owner): Brewin's recipes are not migrated; new Vintage kimchi recipes, one per pickleable in place of the ghost
  pepper: cucumber or zucchini, onion, egg, sea pickle, beetroot, surstromming input fish (all whole raw fish).
- **Century egg** (`pickles-and-ferments #9`): takes a pickled egg instead of any egg (changes §2.7 and VTD-35).
- **Fermented garlic honey** (`pickles-and-ferments #10`): Regeneration II 5 s and Health Boost 30 s. Open: "3 hearts" is not
  a Health Boost level (I = 2 hearts, II = 4).
- **Fermented veg in uncooked crafts** (`dishes/meals #29`): allow fermented garlic, onion and the like where the raw one is
  used (burgers, the listed salads). Open: which crafts.
- **Vintage brews** (`alcohol/pack #3`): the six jar brews (§2.7a) all have vanilla effects; they get their own category and a
  redesign. Open: the category and the effects ([alcohol.md](alcohol.md) §11).
- **Salted fish out** (BUILT 2026-10-25, §4.0b; `ports/vintageport #11`): Salted Cod and Salted Salmon are removed again
  (restored 2026-10-23); Surstromming stays. Vintage's items only; The Salt's Salted cooked fish stays.
- **Wild ghost pepper seeds** (BUILT 2026-10-25; `ports/vintageport #12`): 2 seeds from milling and crushing, 3 in the IE
  Crusher (confirmed by the owner).
- **Ghost charcoal** (BUILT 2026-10-25; `ports/vintageport #13`): edible, Wither I 0:05 and Fire Resistance 3:00 (owner).
- **Golden egg off the tree** (BUILT 2026-10-25, port `-04`; `ports/vintageport #14`): the Magic Peanut tree no longer carries the 80 % golden egg
  (§2.4, S1, VTD-08); the goose egg (§2.5) stays. Needs a port change.
- **Jar fixes** (BUILT 2026-10-25, §4.0b; `ports/vintageport #15`, `#16`, `#17`): JEI shows seconds under a minute; the jar gets a bottle output buffer
  (the keg's pattern, port-native); the slime block pickling recipe is removed.
- **JEI pages** (`jei/jei-coverage #15`): Vintage's machines and jar recipes get a category or description per recipe type,
  as part of the dump-driven steps (`ports/vintageport #2`).

### Built 2026-10-25: pickled sea pickle, pickle soup (TODO `dishes/pickles-and-ferments #1`, `#3`)

- **Pickled Sea Pickle** (`creativeengineering:pickled_sea_pickle`, CD `kubejs_startup/vintagedelight/vintage_items.js`):
  4 hunger, modifier 0.3, eaten fast. Pickling Jar: 5 sea pickles + salt -> 5, 2400 ticks (CD `vintagedelight/recipes.js`).
  Art: `textureswapper/palette_swap.py` on Vintage's pickle (GPL) with the vanilla sea pickle's palette,
  `kubejs/assets/creativeengineering/textures/item/pickled_sea_pickle.png`. Brewin's Pickled Pickles merge into it
  (`global.itemMerges`, pushed from the same startup file); the pack's honey jar recipe for them is gone (`alcohol.js`);
  compost 0.5 and the cooked soup veggie tag moved to it. `canned_pickled_pickles` keeps its id as Canned Pickles
  (pickle + pickled sea pickle; `gen_can_types.py`, applied at the next `/cdevgen` after `/dumpfood`).
- **Pickle soup:** Vintage's pot recipe replaced: `#creativeengineering:pickled_veg` (pickle, pickled sea pickle, pickled
  beetroot, onion and pepper, both kimchis; CD `vintagedelight/tags.js`), 2 `#forge:vegetables` (was carrot + potato),
  beetroot, `#forge:salad_ingredients`; 200 ticks, 1.0 xp as before.
- Tests: VTD-55, VTD-57 below; SQ-CDB-08, -09.

## 6. File map (planned)

| File | Role |
|---|---|
| `development/creativeDelight/members/vintage/` | The port (new) |
| `development/incubationAdditions/` | Goose golden egg roll |
| `kubejs/startup_scripts/_lib/item_dict.js` | Vintage keys, `i.vinegar`; Simple Hats chef hat removed |
| `kubejs/generation_scripts/worldgen/wild_crops.json`, `nether_worldgen_rules.json` | Wild placement |
| `kubejs/server_scripts/tags/` (via `global.tags`) | `ghost_pepper_soil` tag |
| `kubejs/server_scripts/cooking/` (`cooking.js`, `baking.js`) | Recipes |
| `kubejs/generation_scripts/food/food_types.json`, `can_types.json`, `stacks/stack_sizes.json` | Generated food data |
| `kubejs/data/creativeengineering/moonlight/soft_fluids/` | Jar fluids |
| `mod_sides.md` | Jar row |

## 7. Offline checks (once built)

- `./gradlew :backportedVintage:build --offline` passes.
- `tools/test_wild_crops.sh` passes with the new crop tags and modifiers.
- `python3 tools/food_catalog.py --check` lists no unclassified Vintage food.
- `python3 tools/crop_growth_coverage.py` lists no Vintage crop.
- No `dehydrated` or `gearo` string in the built jar's assets or data.
- Headless harness: a pickling recipe with `#forge:salt` matches `salt:salt` and Vintage's salt dust.

## 8. In-game test suite (drafted)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-VP-03, -05, -05b, -09, -10, -11, -16, SQ-VTD4-03, -05, -05o, -07, -08, SQ-VTD6-02a, SQ-VTD7-01; FAIL SQ-VP-06: pepper jam and relish lose the `_bottle` container: stale `kubejs/data/vintagedelight/moonlight/soft_fluids/pepper_jam.json`, `relish.json` shadow the member files; FAIL SQ-VTD6-01: wild ghost peppers stay on grass: `GhostPepperWildCropBlock.mayPlaceOn` takes `#minecraft:nylium`, which Terralith fills with grass, mycelium, moss; FAIL SQ-VTD7-02: bales and Salt Blocks are in `blockrunner:surface/soft` 0.88x (`blocktags.js` 341-346), the changelog says 1.05x (owner decides); OPEN (no verdict, rerun row written) SQ-VP-15, SQ-VTD8-02c; rerun pending (batch fault) SQ-VP-04, -07, SQ-VTD6-02, SQ-VTD8-02, -02b.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-VTD9-01, SQ-VTDA-11, -13, -10, SQ-VP-04, -07, SQ-VTD8-02, -02b, -02c; FAIL SQ-VP-06: the 9 PackJams Forge fluids share ids with the data soft fluids, so pepper jam jars are not soft-fluid items (`jam/PackJams.java` 122-124); SQ-VTD6-02 OPEN: own-pick crops reset age, but the fruit was seen neither dropped nor in the bot; hand-check in the client.

Crops and worldgen

- [ ] VTD-01 Oats and peanuts plant on farmland, Rich Farmland and Fertile Soil, grow to age 7 and drop produce (and
  seeds where they have them); a knife on ripe oats gives straw; right-click and the hoe area harvest and replant them.
- [ ] VTD-02 **Cucumber replant mark:** right-click at age 7 gives 1 to 2 and drops to age 5; note whether Quark's
  generic replant fires instead; with a hoe over an area, note what each plant does.
- [ ] VTD-03 **Ghost pepper replant mark:** same as VTD-02. Seeds plant on Soul Soil, Rich Soul Soil and Fertile Soul Soil; the seed tooltip names those three; they
  refuse nylium, farmland, dirt and the rest.
- [ ] VTD-04 Zucchini (Veggies, `VD-25`) with a hoe over an area: picks and drops back 3 ages without double-firing.
- [ ] VTD-05 WTHIT shows a growth line on every Vintage crop.
- [ ] VTD-06 Wild oats, peanuts and cucumbers appear only where their `wild_crops.json` rules name, on any dirt soil
  (Grass, Podzol, Rich Soil, Fertile Soil, IW soils); a wild plant can be placed by hand on each of them.
- [ ] VTD-07 Wild ghost peppers appear in crimson and warped forests on nylium and nowhere else in the Nether.
- [ ] VTD-08 No gearo bush exists. The Magic Peanut drops from ripe peanuts at about 1 %; planted, it grows a magic vine tree
  with Lush Grass under it and an 80 % Golden Egg on top; magic vine logs cut to Vine Tiles; Vine Net and Lush Grass exist.
- [ ] VTD-08b Vine tiles come only from cutting a stripped magic vine (cutting board and Create); slab and stairs from tiles.
- [ ] VTD-47 The test set verdict: after play, write keep / purge for the magic vine tree line into S1.
- [ ] VTD-08a Lush Grass: bone meal places flower forest / meadow / grass features; a hoe turns it into Rich Soil
  Farmland; a tree grown beside it leaves it as grass.
- [ ] VTD-09 A Golden Egg block drops the reward table.

Machines

- [ ] VTD-10 Pickling jar: 5 cucumbers + salt -> pickle; hopper in from the top, out from the bottom.
- [ ] VTD-11 The jar takes salt dust in every salt recipe (since the salt merge, item_merges.md 2.9, The Salt's salt
  is salt dust in any inventory; no recipe names `salt:salt`).
- [ ] VTD-12 Evaporator next to water grows salt layers; salt dust places and thickens a layer; salt bucket -> 3 dust.
- [ ] VTD-13 Cheese mold: 4 curds -> wheel after a random tick -> slices; JEI explains it.
- [ ] VTD-14 Mason jars stack 1 to 4 on a block; jar + 3 bottles <-> 3 bottles.
- [ ] VTD-15 Fermented garlic honey is made in the pickling jar.
- [ ] VTD-16 The Brewin' pickling recipes (pickled pickles, kimchi) are made in the jar and no longer in the keg; the
  keg still makes beer.

Food and recipes

- [ ] VTD-20 Every Vintage food has a food type tag and the type's values.
- [ ] VTD-20a Oat dough works in the `#forge:dough` recipes (cake base, pie crust, cookie dough, dumplings, cucumber
  dough); JEI lists it in those recipes. Cucumber dough does not.
- [ ] VTD-21 Pizzas bake in the oven; oat bread follows the pack's bread chain.
- [ ] VTD-22 Each Vintage pot meal has a heatless grid route where the rule says so.
- [ ] VTD-23 The 6 custom-effect brews come out of the jar (Goblin's Piss with a fermented spider eye, not a gearo berry); record how the potion mixers and fluid handle them.
- [ ] VTD-24 Vinegar cans: salad + vinegar + empty can -> a can; vinegar appears as a cooking ingredient only where a
  food rule says so.
- [ ] VTD-25 Placed jars, pizzas and the cheese mold show the placeable tooltip.
- [ ] VTD-26 Every liquid the port adds (the 8 jar contents, nut milk) can be put in a Supplementaries jar.
- [ ] VTD-27 No Vintage food or potion gives Dehydrated; no Dehydrated effect, potion, brewing mix or salt dust awkward
  potion step exists (check JEI, `/effect give`, the brewing stand with salt dust).

Loot, mobs, geese, hats

- [ ] VTD-30 None of the Vintage chest tables is reachable from any vanilla or pack chest; `/loot` can still roll them.
- [ ] VTD-31 Mob food works (chickens, parrots, pigs, villagers) and the audit note is written.
- [ ] VTD-32 Geese: with the chance raised for the test (the default is 1 in 1000), a goose sometimes drops a Golden Egg
  on the ground instead of an egg; it never walks to a nest for that lay; a golden egg is never found in a goose nest.
- [ ] VTD-32a Golden egg and walls: a goose standing against a wall or in a corner with open ground within 3 blocks drops
  the golden egg on the open ground, not against the wall; in a closed 1x1 pen it drops where it stands.
- [ ] VTD-35 Century egg: the jar takes any egg (hen, duck, goose, emu) + coal + salt and gives a century egg after 3
  hours.
- [ ] VTD-33 Vintage chef hats exist in 16 colours; Simple Hats' chef's hat is gone from hat bags and recipes.
- [ ] VTD-34 Advancement files are present in the jar and the pack still drops all advancements at load.

Overlap review and UI

- [ ] VTD-40 **Overlap review:** for each `*overlaps w/*` row in §3.1, compare both versions in JEI, in the shared tag
  recipes, in cans and in food types; write keep both / keep one / merge back into §3.1.
- [ ] VTD-45 Names audit fixes; every Vintage item in JEI has a recipe or a use.
- [ ] VTD-46 Mixed Seeds (§2.3a, port `-10`): use a stack of 20 on tilled farmland; each use plants wheat, beetroot, cabbage, a
  melon stem or a pumpkin stem (all five seen over 20), one seed spent per planting, none in creative; used on grass or
  stone it does nothing and is kept. JEI shows its info page; tooltip "Plants a random crop".
- [ ] VTD-79 Mixed Seeds loot: break about 100 grass with an empty hand and see a few Mixed Seeds (2 %); with shears none.
  `/loot` the village plains house table 20 times: Mixed Seeds turn up now and then.
- [ ] VTD-48 **Burgers:** Cheese Burger is shaped (salad, tomato, onion / cheese slice, patty / bread) with a beef or
  vegetarian patty; a flaxen wedge in the cheese slot gives Delightful's Cheeseburger instead. The Deluxe Burger is
  shaped (salad, tomato, onion / bacon, patty, fried egg / cheese slice, bread). JEI shows one sequenced assembly
  each (cheese burger on bread, deluxe on a beef patty), none in the Create mixer or mechanical crafter.
- [ ] VTD-49 **Stuffed burrito:** a tortilla, beef patty, salad, tomato, cheese and a ghost pepper or roasted bell
  pepper; no bread recipe; an assembly on a tortilla starting with the pepper.
- [ ] VTD-50 **Manual only and heatless:** PB & J and Cucumber Salad craft by hand only; Cheese Pasta (pasta, 3
  cheese, bowl), Pad Thai (pasta, ghost pepper, 2 cooked chicken cuts, nut mash bottle, roasted peanut, bowl) and
  Pickle Soup (bone broth, 2 pickles, carrot, baked potato, beetroot, salad) craft in the grid and not in the
  unheated mixer.
- [ ] VTD-51 **Dough and cookies:** Oat Dough does not smelt or smoke into bread; 3 Raw Oats still craft Bread.
  Cookie dough + 2 Raw Oats bakes 12 Oatmeal Cookies on the baking sheet; no oatmeal cookie grid recipe.
- [ ] VTD-52 **Veggies compat folded:** Cesar Salad takes garlic or cheese, the Stuffed Zucchini Boat milk or cheese,
  both by hand only; the Chicken Fajitas Wrap has no dough recipe.
- [ ] VTD-53 **Types, fluids, compost:** `/dumpfood` then `python3 tools/food_catalog.py --check` lists no new
  Vintage food; Pickle Soup, Ghostly Chili and Oatmeal go in a Supplementaries jar in their own colour (VD: Carrot
  Juice, Dandelion Juice, Turnip Water, Fermented Garlic Honey too, and a jar of carrot juice fills from a tank);
  Pickled Egg and Century Egg compost (0.6, 0.9), a Cucumber Crate composts at 1.0.
- [ ] VTD-54 **Block drops (after `vintageport #18`):** in survival, break a placed Magic Peanut, Smooth Salt Block and
  Vine Tile by hand: each drops itself. Place a Cheese Wheel, a Cheese Pizza and a Meat Pizza; break one whole (4
  slices), take one slice from another and break it (3 slices); a Creeper or TNT blast may drop some slices (the tables use `survives_explosion`; owner 2026-11-02: fine, scratch r1102 got 4).
- [ ] VTD-65 **Salted fish out:** JEI has no Salted Cod or Salted Salmon and no recipe for them; Surstromming still
  pickles; the canned fish and canned cooked salmon still craft from cooked cod / salmon.
- [ ] VTD-66 **Wild ghost pepper seeds:** millstone and crushing wheels give 2 Ghost Pepper Seeds + 1 cyan dye, the IE
  Crusher 3 seeds + 1 cyan dye.
- [ ] VTD-67 **Ghost charcoal:** eat it at full hunger: Wither I 0:05 and Fire Resistance 3:00, no hunger restored; it
  still burns in a furnace.
- [ ] VTD-68 **No golden egg on the tree:** grow a Magic Peanut tree: nothing on top of the trunk; a goose can still lay
  a golden egg. Scratch: SQ-VTD9-01 (server).
- [ ] VTD-69 **Jar time:** JEI Pickling Jar page: Berserker's Brew (400 ticks) shows 20s, a pickle (2400) 2min, leather
  (4800) 4min.
- [ ] VTD-70 **Jar leftover slot:** fermented garlic honey (garlic + honey bottle) leaves the glass bottle in the new slot
  right of the outputs; a hopper under the jar pulls it; breaking the jar drops it; a jar from an older save keeps its
  items; with the slot full of another item the jar waits.
- [ ] VTD-71 **No slime from the jar:** Slime Block has no Pickling Jar recipe in JEI.
- [ ] VTD-72 **Any salt:** Berserker's Brew and leather (rotten flesh, tree bark, vinegar) pickle with Create: The Salt's
  salt as well as Vintage's salt dust.
- [ ] VTD-73 **Honey jar fluid:** a Honey Jar empties into a Supplementaries jar as honey (honey colour, "Honey"), and
  fills back into an empty mason jar; it no longer counts as apple sauce.
- [ ] VTD-75 **Ghost pepper soil:** ghost pepper seeds plant on Soul Soil, Rich Soul Soil and Nulch only; farmland, Rich
  Farmland, Fertile Soil, dirt, grass and nylium refuse them. A wild ghost pepper places on nylium and those three, not on
  grass or dirt. A ghost pepper already on farmland (old save) breaks when a neighbour updates.
- [ ] VTD-77 **Salt layer icon** (port `-07`): the Salt layer item in JEI, the inventory and the hand is a thin pink-white
  slab like the snow layer's item, not the salt dust pile. Placing it still places one layer.
- [ ] VTD-76 **Own pick, not Quark:** ripe cucumber, ghost pepper and zucchini with an empty hand: 1 to 2 cucumbers (1
  pepper, 1 to 2 zucchini), no seeds, back to stage 5 (zucchini 4). Same with a wooden hoe. With an iron hoe over a 3x3 of
  ripe and unripe ones: every ripe one picks once, unripe ones stay, no double drops. Wheat in the same area still
  replants to stage 0.
- [ ] VTD-74 **Crop loot:** `/loottuning` (or the log) lists the Vintage crop packs (oat bale, oat bag, peanut, cucumber,
  ghost pepper crates) and the Rustic bell pepper crates and seed bag; village house, ruined portal and bastion hoglin
  stable chests can hold the Vintage crops.

Oat look (2026-10-31)

- [ ] VTD-78 Plant oats beside wheat on farmland and grow them with bone meal: every stage has the bushy wheat shape in
  oat colours; ripe oats are two blocks tall and vary between three shapes by position, like the ripe wheat next to
  them. Break a ripe oat: same drops as before. F3+T with Fancy Crops off: oats still use these textures.

Pickles (2026-10-25)

- [ ] VTD-55 Pickling Jar: 5 sea pickles + salt -> 5 Pickled Sea Pickles; Pickled Pickles are not in JEI, creative or
  loot, and an old stack turns into Pickled Sea Pickles.
- [ ] VTD-57 Cooking pot: each pickled veggie in the pickle slot makes pickle soup, with any 2 vegetables.

Vintage work run (2026-11-03)

Port `-14` and the CD scripts are in the CD jar installed 2026-11-03 00:08 (Creative Brewing jar the same time, kimchi removal); not booted. Scratch rows SQ-VTDA-10 to -13 and client row CQ-VTDA-01 (queue section SQ-CDJ7) cover VTD-80 to VTD-86 server-side.

- [ ] VTD-80 **Wild ghost pepper soil** (port `-14`, `vintageport #7`): with Terralith, a wild ghost pepper does not stay on
  grass, mycelium or moss (it breaks on the next neighbour update); it places on crimson and warped nylium, Soul Soil,
  Rich Soul Soil and Nulch.
- [ ] VTD-81 **Salt layer from inside** (`food/seasoning #7`): stand in a 2-layer salt patch and use Salt Dust on it up to 8
  layers: the layer grows, you are not pushed up. Walk onto a 6-layer salt block from the side: you still step up.
- [ ] VTD-82 **JEI pages** (`jei/jei-coverage #15`): Organic Mash's page reads "Fermented organic mash, used as an enhanced
  fertilizer or to make compost."; Salt Layer, Cheese Mold and every mason jar (empty and filled) have an information page.
- [ ] VTD-83 **Kimchi** (`merges/dishes #9`): JEI shows one kimchi (Vintage's); Pickling Jar: 4 cabbage leaves + salt +
  one of cucumber or zucchini, onion, any egg, sea pickle, beetroot, any whole raw fish (cod, salmon, tropical fish,
  lionfish, perch, pike, blobfish, flying fish, catfish) -> 4 kimchi; the ghost pepper recipe stays. No Brewin' kimchi recipe.
- [ ] VTD-84 **Nut mash** (`merges/dishes #7`): Cooking Pot: 5 acorns, roasted acorns, peanuts or roasted peanuts (mixed is
  fine) + sugar -> 3 Nut Mash, each served into an empty mason jar; no nut mash bottle, nut butter or nut dough in JEI; a
  Nut Butter bottle in an old chest turns into Nut Mash.
- [ ] VTD-85 **Sandwich** (`merges/dishes #7`): "Nut Mash and Jam Sandwich" (Vintage's art) from any bread + a Nut Mash +
  any jam in the grid, both jars come back empty; Delightful's sandwich is gone from JEI. Pad Thai takes a Nut Mash.
- [ ] VTD-86 **Quiche** (`merges/dishes #8`): the Oven with a pie tin bakes Quiche from pie crust + cheese wedge + milk +
  any egg + any cooked meat, or any mushroom, or a salad green (cabbage, cabbage leaf, dandelion leaf); Zucchini Quiche
  from pie crust + cheese wedge + milk + egg + 2 roasted zucchini. No grid or Create assembly recipe for either.

## 9. History

- 2026-10-22: plan written from the 0.1.6 Forge 1.20.1 jar (decompiled, 94 sources, 237 recipe files) and the KOTS
  0.3.1 NeoForge 1.21.1 jar, checked against the pack's display-name and tag exports and the Veggies 1.20.1 jar's
  Vintage compat recipes. Nothing built.
- 2026-10-23: rewritten with the owner's decisions (§1): gearo and magic vine tree purged, duplicates kept and marked,
  salted fish later restored (2026-10-23), pickling jar / keg / cask split, no Dehydrated, loot unwired, goose golden egg, chef hats replace
  Simple Hats'. Earlier recommendations D1 to D16 are replaced by §1 and §5.
- 2026-10-23 (second pass): century egg takes any egg; the jar is named Pickling Jar in lang; the cask is the Vinery
  port's fermentation barrel (lang); Vine Net, Vine Tiles and Lush Grass with its hoe route are kept (source open, §5
  Q1) while the magic vine tree stays purged; Mixed Seeds investigation and implementation marked (§2.3a); golden egg
  chance 1 in 1000 with wall avoidance; "the 5 brews" defined (§2.7a) and Dehydrated re-confirmed out.
- 2026-10-23 (third pass): Goblin's Piss kept with a fermented spider eye in place of the gearo berry (six brews); Magic Peanut kept for now (Q2).
- 2026-10-23 (fourth pass): the magic vine tree line is back for testing (Magic Peanut grows the tree, 80 % Golden Egg on top, magic vine wood, tiles, Lush Grass); Q1 and Q2 closed; recipes that took Veggies' smoked bell pepper take any Rustic roasted bell pepper; golden egg replace and no-walk decisions confirmed.
- 2026-10-23 (fifth pass): Salted Cod and Salted Salmon restored (2 fish + salt -> 2 salted fish), marked `*overlaps w/ The Salt's salting*` for the `VTD-40` review.
- 2026-10-23: built (§4.0). Port jar `backportedVintage-0.1.6-ce-19.2-01`, goose golden egg in incubationAdditions
  1.1.0, Veggies port `-02` with the 22 Vintage compat recipes, pack integration. Mixed Seeds investigated: not a
  registered item upstream (§2.3a); Organic Mash is a live item. Open: the dump-driven steps and the cooking rules.
- 2026-10-23 (after the build): oat dough joins `#forge:dough`; Nulch renamed Fertile Soul Soil (lang; recipe review in
  TODO `farming/composting #2`); wild oats and cucumbers also in cold and temperate grassy biomes, wild peanuts moved to
  warm dry grassy; `gen_soil_compat.py` reads the Vinery donor jar so the grape bush soil overrides survive its runs
  (they had come from 1.3.10 and would have been deleted by the next run). Systems still not updated for either port:
  §4.0a.
- 2026-10-23 (food integration, both ports): Vintage foods typed (54, existing types); cans configured for Vintage
  (waiting on a dump) and generated for Veggies (24) after the food catalog was rerun from the 10-22 dump; the vinegar
  additive ran; jar fluids for the Vintage soups and the Veggies juices and garlic honey (the soup tool would have
  deleted the hand-written ones; it now keeps them); compost rows; cooking rules (step 9), including the Veggies
  compat recipes that went live with this port. The old §2.7 note that the compost table needed no action was
  wrong: the engine clears the mod's own values.
- 2026-10-24: planned from the scratchpad (section 5, "Planned from the scratchpad"): jam and nut mash merges, pickled sea pickle, real-pickle recipes, pickle soup, enzyme and vinegar, Foul Taste, kimchi merge, century egg and garlic honey changes, salted fish and slime block pickling removed, ghost pepper and charcoal changes, golden egg off the tree, jar fixes (seconds, bottle buffer). Nothing built.
- 2026-10-25: KOTS 0.3.1 checked for content past the port (§2.10). Found: Magic Peanut, Smooth Salt Block and Vine Tile
  have no loot table in 0.1.6 or the port and drop nothing (`vintageport #18`, VTD-54); the rest is purged, format or
  skipped. The §2.7 "block loot tables ship in full" note was wrong. No code changed.
- 2026-10-25: vintageport #5, #9 to #18 built (§4.0b): port `-04`, first Creative Delight member scripts.
- 2026-10-25: Cheese Wheel and pizza loot taken from KOTS (owner: "fix the block tables"): port `-05`, VTD-54, SQ-VTD5-01.
  Veggies' cakes, pies and tray have no table either, as upstream and as FD's pies: not a bug.
- 2026-10-25: vintageport #4 found already built as #16 (leftover slot, VTD-70); #7 ghost pepper soil in `canSurvive`
  (port `-06`, VTD-75); #8 Quark vs own pick investigated, own pick kept via `crops/own_pick` (§4.0b, VTD-76).
- 2026-10-26: the salt layer item shows its one-layer block model instead of salt dust's icon (`food/seasoning #7`, part):
  port `-07` (`tools/port_resources.py` writes the item model), VTD-77.
- 2026-10-28: port `-08`: shears on lush grass clear its new `fertile` state and stop it spreading (pack addition,
  [wthit_lines.md](wthit_lines.md) 2.10, WL-34 to WL-36). Installed in `mods/`.
- 2026-10-29: cucumber noodles renamed Veggie Noodles (namechanges.js), sprite recoloured to the zucchini's palette
  (`kubejs/assets/vintagedelight/textures/item/cucumber_noodles.png`); the cucumber dough takes cucumber, broccoli,
  zucchini or its slice, cactus flesh or dandelion leaves (CD `vintagedelight/recipes.js`; briefs/dishes_meals.md
  meals #43, RC-162). The dough and its block keep their Cucumber names.
- 2026-10-29: port `-09` (pack addition, not upstream): Green Grape, Red Grape and Cherry Jam Jars, mason jar blocks
  like the sweet berry jam jar (same food values, 1 to 4 per block, loot, soft fluids, `#forge:jam_jars`,
  `#vintagedelight:sweet_jam`, Create upright tags); placeholder sprites from the green, purple and red wool. Pack side
  (CD `vintagedelight/jams.js`): pot 4 fruit + sugar + lemon slice -> 2 jars; the jam and relish bottles and the
  sweet berry and glow berry jams are removed items (CD startup `vintage_items.js`, recipes removed); relish leaves
  the jam tags; the pepper jam and relish soft fluids are jar-only (overrides in `kubejs/data/vintagedelight/moonlight/
  soft_fluids/`); PB and J takes `#forge:jams`. Coaster files and placeable entries for the removed jars dropped
  (`tools/placeable_catalog.py` EXCLUDE). RC-170 to RC-175 in [recipes_cooking.md](recipes_cooking.md). Needs the
  `-09` build + install.
- 2026-11-01: `vintageport #2` dump-driven steps on the 2026-10-30 dumps (4.0 steps 5, 7, 8, 14, 21): Ghost Charcoal typed,
  cans unchanged, chef hats and whole pizzas / cheese wheel get stack rules, BlockRunner tiers for 18 blocks, crops.md
  §2.1 rows; the name audit and WTHIT icons wait for client dumps. Mixed Seeds built (§2.3a, port `-10`).
- **2026-11-02:** nested in Creative Delight (`creativedelight/members #1`): `bundled_in=creativeDelight`, jar `backportedVintage-0.1.6-ce-19.2-11` inside the CD jar, "Vintage Delight (Creative Delight)"; folder stays `development/creativeDelight/members/vintage`. The port's three settings (`worldgen.magicVineConvertsDirtToLushGrass`, `pickling_jar.remainderBuffer`, `pickling_jar.stallWhenFull`) are now Creative Delight's `[vintage]` section of `creativedelight-common.toml` (same sub-tables and keys, read through the shared `CdSettings`); the old `vintagedelight-common.toml` is read once by CD's `MemberConfigMove` and renamed `.moved`. Scratch test queued: SQ-VTD8-01 (config move, bottles thrown with the buffer off); the Vintage reruns are listed in scratch_queue.md SQ-CDJ5.
- **2026-11-02 (afternoon):** folder moved to `development/creativeDelight/members/vintage` (plan_creative_delight.md I8),
  port `-12`. Pickling Jar fix (I2, `creativedelight/members #37`): `FermentingRecipe.assignSlots` assigns one input slot
  per ingredient entry (distinct slots first, a slot shared only when there are fewer slots than entries); matching,
  consuming and the leftover check all use it, so split ingredients no longer drain the first slot and stall the next
  batch. Thrown leftovers (buffer off or full) wait CD `[vintage.pickling_jar] overflowEjectDelayTicks` ticks (default 20)
  in a queue saved with the jar and dropped if it breaks. Rows SQ-VTD8-01 (rewritten), SQ-VTD8-02.
- **2026-11-04:** port `-18`, strict one item per slot (owner report: the jar started batches from stacked slots and
  took several items from one slot). `assignSlots` now needs exactly as many filled input slots as ingredient entries,
  each slot feeding one entry (perfect matching, backtracking); the shared-slot fallback of I2 is gone. Every pickling
  recipe has 2 to 6 entries (NCH `pickling` enforces 1 to 6), so none becomes unmakeable. Row SQ-VTD8-02 (b) flipped,
  SQ-VTD8-03 added.

- **2026-11-03 (stations, port `-16`, in the CD jar built 19:13, not installed):** Pickling Jar on the five-station rule
  ([remainder_buffer.md](remainder_buffer.md) 3.8, TODO `creativedelight/members #37`, `cooking/kitchen #29`): leftovers through
  the shared `LeftoverPlan` (the recipe's container back into slot 6 up to a stack, then slot 9, then the delayed throw);
  containers only in slot 6 (recipe containers + `#vintagedelight:container_items`), by hand, shift-click and automation;
  automation faces as the FD stations (every face inserts, routed; top pulls ingredients; sides and bottom pull slots
  7, 8, 9; slot 6 never pulled); progress restarts when the matched recipe changes (the instant kimchi of the
  2026-11-02 scratchpad: a new batch inherited the old one's count). SQ-STN-01 to -03, RB-16 to -19.
- **2026-11-03 (Vintage work run):** port `-14` (nested in the CD jar installed 2026-11-03 00:08, not booted): `GhostPepperWildCropBlock.mayPlaceOn` names
  crimson and warped nylium instead of `#minecraft:nylium` (Terralith fills that tag with grass, mycelium, moss;
  `vintageport #7`). `SaltLayerBlock.getCollisionShape` cuts the collision at the feet of an entity whose box overlaps the
  block and whose feet are below the layer's top, so a layer grown under you does not push you up; outside the block the
  full snow-like shape stays (`food/seasoning #7`; side effect: while already standing in salt you can walk into a thicker
  neighbouring layer instead of stepping up). JEI (`jei-coverage #15`): Organic Mash text fixed, new pages on the Salt
  Layer (grown by the Evaporator; Salt Blocks no longer grow salt since the salt merge), the Cheese Mold and jar stacking
  (every `MasonJarBlock`); keys in `src/own` lang. The stale pack overrides `kubejs/data/vintagedelight/moonlight/
  soft_fluids/pepper_jam.json`, `relish.json` (they dropped the `_bottle` container, SQ-VP-06) moved to
  `development/creativeDelight/moved_code_backup/2026-11-02-vintage/`; the member's own files apply. Kimchi merge
  (`merges/dishes #9`): Brewin's kimchi is a removed item (CD `vintage_items.js`; Creative Brewing `swaps/removed.json`),
  the pack's `brewin_kimchi` jar recipe is gone (Creative Brewing `alcohol.js`), six new jar recipes in CD
  `vintagedelight/recipes.js` (`kimchi_from_*`), new tag `#creativeengineering:whole_raw_fish`; Vintage kimchi takes
  Brewin's place in `#creativeengineering:soup_veggie_cooked` and alone fills Canned Kimchi (`gen_can_types.py`; the can
  now takes Vintage's values). Kimchi, nut mash (`merges/dishes #7`) and quiche (`merges/dishes #8`): item_merges.md 2.18 to 2.20.

### Owner decisions 2026-11-02

- VTD-47: keep the magic vine tree set. Magic Peanut: very rare peanut drop (replaces ~1 %) and very rare wandering trader trade, as rare as the pale/dark seeds.
- Kimchi: Vintage's wins; Brewin's kimchi removed, its recipes NOT carried over (owner later 2026-11-02, replaces "carried over"); new Vintage kimchi recipes per pickleable (see TODO `merges/dishes #9`; built 2026-11-03, History).
- Pickles craftable with cucumbers or zucchini. Fermented garlic honey: 4 hearts already, no change. Golden egg placement observed OK. Both lasagnas kept.
