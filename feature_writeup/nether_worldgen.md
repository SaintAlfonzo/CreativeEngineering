# Nether blocks in Overworld generation

Every Nether block the Overworld still generates by itself: biome surface rules, biome features, carvers, and
structures after the existing swaps ([nether_swaps.md](nether_swaps.md)). Section 2 is the decision sheet: one row
per thing that can be changed, with a **Decision** column (blank = still open).

## 0. At a glance

| Part | Status |
|---|---|
| Scan (surface rules, features, code-added layers and carvers, structures after swaps) | BUILT 2026-10-19: `tools/nether_worldgen_scan.py` -> [nether_worldgen_scan.md](nether_worldgen_scan.md) |
| Decisions (section 2) | round 1 answered 2026-10-19 (B1, B8, B9 changed; blackstone, basalt, magma kept; bunker and red roof fixed); round 2 the same day (moonlight flower patch generates, sprouts out; D&D deep layer granite; soul fire to fire and glowstone to yellow corundum in every structure). 2026-10-29: K19 kept, K25 and K32 lantern / torch except ancient cities and sculk structures. Still open: B12, K28 to K31, K33, K35, K36 |
| Soul lanterns and torches (K25, K32) | BUILT 2026-10-30, not deployed: rules 23 to 25 in `nether_swap_rules.json` (new `except_holding: ["@sculk"]`), generator keeps lantern `hanging` and wall torch `facing` (family carry-over), 323 swaps added (1407 in 101 templates); YUNG's `Enable Nether Blocks in Dungeons` off. Not run in game (NW-13 to NW-17, SQ-WG30-B1 to B4) |
| Biome changes (B1, B8, B9, F7) | BUILT 2026-10-19, DEPLOYED with 1.0.9 (2026-10-19 15:54): `nether_worldgen_rules.json` -> `tools/gen_nether_worldgen.py` -> 7 `kubejs/data` files, then `gen_wild_flowers.py` (flower snapshot). Round 2 scratch world: moonlight grove has blue orchids and cornflowers with no sprouts or roots; no breccia or glowstone deep down. Scratch server: loads clean; a new world's Scarlet Mountains has jasper + 9.9 % crimsite and no netherrack, nylium or roots; Mirage Isles trunks are stripped acacia. Not run in the user's game (NW-01 to NW-05) |
| Structure changes (S4, S7, K27, K34) | BUILT 2026-10-19, DEPLOYED with 1.0.9: rules 3, 20 and 21 in `nether_swap_rules.json`, swap file regenerated (1084 swaps). Not run in game (NW-06, NW-07, NW-10, NW-11) |
| No glowstone in the Overworld (`biomes/netherblocks #4`) | CHECKED 2026-11-02, no change needed: the static scan finds no glowstone in any source (surface rules, biome features, code-added layers, live structures after the swaps, and the new processor pass over 150 processor lists). Census tool `tools/block_census.py` built; the real-world count waits for a fresh world (NW-21) |
| Rarest biomes more common, never-spawning biomes spawning | DEPLOYED with 1.0.9 (built 2026-10-19) in [biome_tuning.md](biome_tuning.md) (rarity floor, `target_pct`, 20,000-column samples, `/dumpclimatecaves`) |

- **Files to open:** this file, [nether_worldgen_scan.md](nether_worldgen_scan.md) (raw scan), [nether_swaps.md](nether_swaps.md) (structure swaps in place), [nether_worldgen_rules.json](../kubejs/generation_scripts/worldgen/nether_worldgen_rules.json) (biome rules).
- **Knobs:** `nether_worldgen_rules.json` (surface rules, features, feature removals; rerun `gen_nether_worldgen.py`), `nether_swap_rules.json` (structures; rerun `gen_nether_swaps.py`), `biome_config.json` (drop a biome).
- **Measure:** `python3 tools/nether_worldgen_scan.py` (about 1 minute).
- **New worlds or new chunks only:** surface rules and features apply to chunks generated after the change; the biome layout to worlds created after it.

**How to reply:** give a row id and a choice, for example "B1: netherrack -> jasper + crimsite, nylium -> coarse
dirt", "F1: drop", "K25: lantern everywhere", "S8: drop". A K row decides a block in every structure; an S row
overrides it for one structure. Choices:
- **swap X**: one block for another. Shape is kept: stairs, slabs, walls and axis carry over, as in the nether brick rule.
- **swap X + Y**: several blocks, mixed by noise in surface rules, by weight in features, by position in templates.
- **drop**: remove the feature or structure, or replace the biome (`biome_config.json` `replace_with`).
- **keep**: leave it as it is.

## 1. Overview

The scan looks at four sources:

1. **Surface rules.** These come from Terralith's `minecraft:overworld` noise settings. Tectonic only adds Terratonic
   density functions when Terralith is present, so Terralith's surface rule is the live one. The scan records every
   block rule that results in a Nether block, with the biome conditions on its path.
2. **Biome features.** These are the features listed in each biome's json (Terralith ships all 143 json Overworld
   biomes), plus `forge:add_features` modifiers, minus `forge:remove_features`. The scan walks placed and configured
   feature json for every block placed. It skips rule tests and block predicates, which check blocks rather than
   place them. It also counts:
   - vanilla feature types that hardcode their block (`underwater_magma` -> magma)
   - template-placing features (YUNG's Extras)
   - features registered in Java, read from the decompiled jars
3. **Code-added sources.** These are carvers and Create-style layered ores that no biome json or modifier names:
   Upgrade Aquatic's underwater canyon and Create: Dreams & Desires' deep layer, from the decompiled jars. The D&D
   layer was found 2026-10-19, when a new world showed glowstone the scan had missed; every mod jar's worldgen classes
   were then searched for Nether block fields, and nothing else turned up in the Overworld.
4. **Structures after the swaps.** This is [nether_blocks_scan.md](nether_blocks_scan.md) with
   `nether_block_swaps.json` applied. It shows what is left, plus the Nether blocks the swaps themselves put in
   (basalt for soul sand, magma for netherrack).

"Nether block" is `nether_blocks_scan.classify`. **Tier 1** is netherrack, soul sand/soil and nether wart. **Tier 2**
is crying obsidian, the blackstone family and basalt. **Tier 3** is everything else: crimson/warped, magma, soul
fire/lanterns, glowstone and the rest. Smooth basalt is an Overworld block and doesn't count. Biome shares are
`result_pct` from `biome_config.json`, the share of the Overworld after biome tuning. A share of 0 % means the
climate samples never hit the biome: it is rare or unreachable.

Candidate blocks named below exist in the pack (checked 2026-10-19):
- Quark `jasper`
- Create `crimsite`, plus the `scorchia` family (near-black volcanic stone) and the `scoria` family (brown volcanic
  stone). Both families have stairs, slabs, walls and a pillar.
- Quark `azalea` wood
- Quark corundum

### 1.1 Findings

Findings of the first scan (2026-10-19, before the changes; shares then were from the 3,000-column samples):

- **Scarlet Mountains is the only tier-1 biome.** Netherrack makes up the whole mountain above the preliminary
  surface. Deepslate and blackstone stripes run through it, and crimson nylium patches and roots cover the top (B1).
- **Yellowstone is the biggest surface case at 4.2 %**, already boosted x1.5. It has a ring of blackstone, blackstone
  rims at sea level and underwater magma (B4).
- **Volcanic Caverns puts Nether blocks under every Overworld biome**: basalt pillars and spikes, magma deltas and
  magma blobs, all between Y -62 and -36 (F1 to F4). It was added 2026-10-13.
- **Upgrade Aquatic's canyons put magma on canyon floors at Y 10 in every ocean** (F5).
- **YUNG's rare desert obelisk is built of blackstone with gilded blackstone** (F6). It is a feature, not a
  structure, so neither structure scan saw it before.
- **Structures:** 58 live structures still hold Nether blocks after the swaps. 4 of them are the `plan/move_to_nether`
  set. Outside that set and the ancient city's kept soul sand, only three tier-1 cases are left:
  - the bunker's nether wart farm (30)
  - the sculk dungeon's soul soil (4, kept)
  - `eugene:red_roof`'s netherrack (3)

  The rest is basalt and polished basalt (2740), the blackstone family (about 1660) and tier-3 trim: soul lanterns
  (441), magma (274) and crimson/warped wood pieces.
- **The swap file was stale.** `eugene:red_roof` arrived with the 7.4 mods (2026-10-14), after `nether_block_swaps.json`
  was generated (2026-10-12). Regenerated 2026-10-19: its 3 netherrack are magma now (rule 16).
- **Found later the same day: D&D glowstone** (F7). Create: Dreams & Desires' deep layered blob puts glowstone
  layers between Y -64 and 0 under every Overworld biome. A new scratch world had 493 glowstone in 25 chunks.
- **Glowstone (2026-11-02, `biomes/netherblocks #4`):** none left in Overworld generation. D&D has no glowstone
  geode (the "D&D geodes" were its F7 layer, granite since 1.0.9); Geode+'s three glowstone geode tags list Nether
  biomes only; Quark's obsidian spikes are Nether-only (config); structure glowstone is yellow corundum (rule 21) or
  gold ore (rule 14); no processor list in any jar or `kubejs/data` puts out glowstone (scan section 4). Every class
  in every jar that names glowstone was read: blocks, recipes, datagen, the D&D layer, Quark's Nether spike, YUNG's
  Nether fortress item frames. The processor pass also lists Overworld processors that put out other Nether blocks
  (basalt on Terralith desert and fortified village roads and a Towns mushroom street, gilded blackstone and stripped
  crimson / warped stem masts on Unwrecked ships): not glowstone, left for the netherblockswap rows.
- **Clean:** Geode+'s Nether geodes are tagged to Nether biomes only. Quark's Nether generators stay in the Nether.
  Twigs, Farmer's Respite and Upgrade Aquatic's prismarine coral only test for Nether blocks, they never place them.
  None of the 13 mods with json-less Overworld features places one: 11 reference no Nether block, and 2 only test
  for one (scan section 2).

## 2. Decision sheet

### 2.1 Biomes: surface rules and the features only that biome has (B)

| Row | Biome | Share | Surface rule | Biome features | Options | Decision |
|---|---|---:|---|---|---|---|
| B1 | `terralith:scarlet_mountains` | 0.255 % | **netherrack**: the whole mountain body above the preliminary surface, where no earlier rule wins. **blackstone**: wide stripes (`scarlet/surface_pattern` +-0.375, around a +-0.1 deepslate core). **crimson nylium**: top layer in patches (`crimson_noise`). Deepslate redstone ore specks in the stripe core | `mountains/scarlet/crimson_spread` (crimson nylium, 1 per chunk), `mountains/scarlet/crimson_patch` (crimson roots, 1 per chunk) | netherrack -> jasper / crimsite / jasper + crimsite (TODO idea); blackstone -> scorchia / deepslate; nylium -> coarse dirt / podzol / rooted dirt; roots -> drop / dead bush; or drop the biome | DONE 2026-10-19: netherrack -> jasper 90 % + crimsite 10 % (per block); the crimson nylium rule and both crimson features removed; blackstone kept |
| B2 | `terralith:volcanic_peaks` | 0.25 % (x1.5) | **basalt**: most of the surface (`tropics/volcano_surface` +-0.75); **blackstone**: core band +-0.11; smooth basalt between them; gravel outside | none | basalt -> scoria / tuff / smooth basalt; blackstone -> scorchia / deepslate; or keep (volcano look, boosted on purpose) | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B3 | `terralith:volcanic_crater` | 0.005 % (x1.5) | as B2, plus **magma** in two noise bands on the floor at Y 61 and up | `volcano/crater_fixer` (basalt, blackstone, magma, 512 per chunk), `volcano/lava_flow` (blackstone, 80), `volcano/magma_barrier` (blackstone, 2048); `volcano/magma_veins` is off in Terralith's json (count 0) | same as B2; magma -> keep / scorchia | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B4 | `terralith:yellowstone` | 4.775 % (x1.5) | **blackstone**: one ring of the hot-spring pattern (`yellowstone/calcite_patch` 0.615 to 0.675, between red sand and smooth basalt) | `yellowstone/acid_black` (blackstone, Y 61 to 63, 228 per chunk: pool rims at sea level), `yellowstone/magma` (underwater magma, 2.5 per chunk; also in `caldera`, 0 %) | blackstone -> scorchia / deepslate / tuff; magma -> keep (bubble columns) / drop | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B5 | `terralith:tropical_jungle` | 0.165 % | **blackstone** band at about Y 63 to 67 and **basalt** at 65 to 70 (shoreline), blackstone patches inland (volcano noise +-0.105) | none | blackstone -> scorchia; basalt -> scoria; or keep | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B6 | `terralith:ashen_savanna` | 0.155 % | **blackstone** patches (volcano noise +-0.105) below about Y 85 to 115 | `savanna/badlands/grass_gravel` (blackstone, 200 per chunk) | blackstone -> scorchia / tuff / gravel | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B7 | `terralith:basalt_cliffs` | 0 % | **basalt**: every cliff block | `cliffs/basalt/basaltifier` (basalt, Y 62 to 164, 700 per chunk), `basalt_column` (Y 75, 200), `basalt_column_upper` (Y 180, 100) | basalt -> scoria (pillar keeps the columns) / smooth basalt; or drop the biome | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B8 | `terralith:moonlight_grove`, `moonlight_valley` | 0.15 % / 0.33 % | none | `flower/blue/nylium_disk` (warped nylium disks, noise count), `flower/blue/flowers` (warped roots, nether sprouts mixed into the blue flowers, 1 per chunk) | nylium -> moss block / podzol; roots and sprouts -> fern / grass / drop | DONE 2026-10-19: nylium -> grass block; warped roots and nether sprouts out of the flower mix; the patch converted to a vanilla flower patch so it generates on grass (round 2) |
| B9 | `terralith:mirage_isles` | 0.175 % | none | `enchanted/trees` (stripped warped stem in Terralith's enchanted oak; the placed feature is the pack's bee-nest override, the stem is Terralith's configured tree) | stem -> stripped birch / stripped azalea / keep | DONE 2026-10-19: stripped warped stem -> stripped acacia log |
| B10 | `terralith:cave/thermal_caves` | 1.32 % (cave) | none | `cave/thermal/surface/floor` + `floor_low` (blackstone floor, noise count), `ceiling` + `ceiling_low` (magma ceiling, 512 per chunk each) | blackstone -> scorchia / deepslate; magma -> keep / scorchia; or drop the biome | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B11 | `terralith:cave/mantle_caves` | 0 % (cave) | none | `cave/mantle/replacer` (blackstone, 250 per chunk, Y bottom to 30), `cave/fuck_art_just_need_performance/blackstone` (blackstone, 256), `cave/mantle/basalt_strip` (blackstone, 140), `cave/mantle/magma_strip` (blackstone + magma, 140) | blackstone -> scorchia / deepslate; magma -> keep | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| B12 | `terralith:cave/frostfire_caves` | 0 % (cave) at the scan; 1.196 % since the 1.0.9 cave floor | none | `cave/frostfire/frostfire_patch` (soul fire, 17 per chunk), `frostfire_ceiling` (soul lanterns, 12) | soul fire -> drop / fire; soul lantern -> lantern | |

### 2.2 Features and carvers in many biomes (F)

| Row | Feature | From | Nether blocks | Where | Biomes | Options | Decision |
|---|---|---|---|---|---|---|---|
| F1 | `volcanic_caverns:basalt_pillar` | Volcanic Caverns (Java) | basalt | 4 per chunk, Y -55 to -40, floor-to-ceiling pillars | all 145 Overworld biomes | drop (remove feature) / swap -> scoria pillar / smooth basalt | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| F2 | `volcanic_caverns:basalt_spike` | Volcanic Caverns (Java) | basalt | 2 to 8 per chunk, Y -62 to -36, dripstone-shaped spikes | all 145 | as F1 | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| F3 | `volcanic_caverns:delta` | Volcanic Caverns (Java) | magma (rim of a lava pool) | 6 per chunk, Y -56 to -46 | all 145 | keep / drop / swap -> scorchia | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| F4 | `volcanic_caverns:underground_magma` | Volcanic Caverns (Java) | magma (ore blobs of 33 in natural stone) | 10 per chunk, Y -56 to -49 | all 145 | keep / drop / swap -> scorchia | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| F5 | `upgrade_aquatic:underwater_canyon` (carver) | Upgrade Aquatic (Java) | magma (25 % of a canyon's floor at Y 10; the rest obsidian) | 1 in 30 ocean chunks starts a canyon (Y 38 to 58) | the 9 ocean biomes (41 % of the Overworld) | keep / drop the carver / obsidian only (needs a mixin) | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| F6 | `yungsextras:desert/obelisks/desert_obelisk_rare` | YUNG's Extras (template `desert/obelisk/obelisk_rare`) | blackstone 13, gilded blackstone 19, polished blackstone 4, polished blackstone bricks 4, polished blackstone brick stairs 12, blackstone stairs 4 | 1 in 3000 chunks, surface | 6 desert biomes (2.3 %) | drop / swap -> scorchia family + gold block or raw gold block for the gilded | keep (2026-10-19: leave the rare obelisk, gilded blackstone included) |
| F7 | `create_dd:striated_eroded_overworld` (Create layered ore) | Create: Dreams & Desires (Java, `DesiresLayerPatterns.SPACE_ROCK`) | glowstone: one layer 1 to 2 thick in a blob of breccia, gabbro and granite layers (weights 2, 1, 1, glowstone 1) | 1 in 48 chunks, blob size 24, Y -64 to 0 | every Overworld biome | keep / drop the glowstone layer / swap -> ochre froglight / yellow corundum | DONE 2026-10-19 (round 2): granite only (two granite layers; `kubejs/data/create_dd/worldgen/configured_feature/striated_eroded_overworld.json`). D&D's separate `gabbro_blob` (granite in stone, gabbro in deepslate, Y -64 to 0) is not layered and was left alone |

Volcanic Caverns' other parts place no Nether block: the carver (lava and air), smooth basalt blobs, extra coal and
diamond ore and lava springs.

### 2.3 Structures: each block across every structure (K)

Counts are the live structures after the swaps, without the four `plan/move_to_nether` structures (S1, S2, S5, S28).
"D twin" = the deepslate family twin (polished deepslate stairs/slab/wall, deepslate bricks, cracked deepslate bricks,
chiseled deepslate), as the nether brick rule already does. "S twin" = the Create scorchia twin (polished cut
scorchia, scorchia bricks, cracked has no twin). Buttons and pressure plates have no stone twin in either family:
stone or polished blackstone kept.

| Row | Block | Tier | Structures | Blocks | Biggest | Options | Decision |
|---|---|---:|---:|---:|---|---|---|
| K1 | `soul_sand` | 1 | 1 | 173 | ancient_city 173 | kept 2026-10-12 (under its soul fire); keep / basalt / coarse dirt | keep (2026-10-12) |
| K2 | `nether_wart` | 1 | 1 | 30 | underground_bunker 30 (a wart farm; its soul sand is already basalt) | drop / beetroots (needs farmland under) | DONE 2026-10-19: removed (rule 3) |
| K3 | `soul_soil` | 1 | 1 | 4 | sculk_dungeon 4 | kept 2026-10-12; keep / basalt | keep (2026-10-12) |
| K4 | `netherrack` | 1 | 1 | 3 | red_roof_castle 3 | rerun the swaps (rule 15 makes it magma) / other | DONE 2026-10-19: magma (rule 16, swap file regenerated) |
| K5 | `polished_basalt` | 2 | 10 | 1650 | ancient_city 705, underground_structures 363, frosted_dungeon 233 ... | polished cut scoria / polished deepslate / keep | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K6 | `basalt` | 2 | 11 | 1090 | fortified_village 480, underground_structures 240 (204 swapped in), plains_mansion 144 ... | scoria pillar / smooth basalt / keep (the 2026-10-12 soul sand target) | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K7 | `polished_blackstone_stairs` | 2 | 3 | 487 | barn 410, stronghold 48, desert_temple 29 | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K8 | `polished_blackstone_slab` | 2 | 3 | 316 | barn 248, stronghold 64, gold_washing_facility 4 | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K9 | `polished_blackstone` | 2 | 5 | 281 | barn 158, desert_temple 44, stronghold 31 ... | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K10 | `polished_blackstone_bricks` | 2 | 1 | 141 | pillager_outpost_swedish 141 | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K11 | `polished_blackstone_button` | 2 | 3 | 101 | pillager_outpost 96 (the cages), desert_temple 3, antique_trading_ship 2 | stone button / keep | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K12 | `polished_blackstone_wall` | 2 | 1 | 54 | pillager_outpost 54 | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K13 | `polished_blackstone_brick_wall` | 2 | 4 | 51 | pillager_outpost_swedish 36, sculk_dungeon 8, spire 4 ... | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K14 | `blackstone` | 2 | 5 | 47 | spire 26, desert_temple 10, taiga_forge 8 ... | cobbled deepslate / scorchia | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K15 | `polished_blackstone_brick_slab` | 2 | 2 | 45 | pillager_outpost_swedish 40, taiga_forge 5 | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K16 | `polished_blackstone_brick_stairs` | 2 | 2 | 43 | pillager_outpost_swedish 41, taiga_forge 2 | D twin / S twin | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K17 | `blackstone_wall` | 2 | 1 | 32 | desert_temple 32 (statues) | cobbled deepslate wall / scorchia wall | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K18 | `cracked_polished_blackstone_bricks` | 2 | 1 | 30 | pillager_outpost_swedish 30 | cracked deepslate bricks / scorchia bricks | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K19 | `crying_obsidian` | 2 | 3 | 27 | stronghold 10 (portal treasure rooms), jungle_pillager_tower 9, old_villager_statue 8 | obsidian / keep | keep (2026-10-29) |
| K20 | `blackstone_slab` | 2 | 3 | 15 | desert_temple 8, village_temple 4, taiga_forge 3 | cobbled deepslate slab / scorchia slab | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K21 | `polished_blackstone_pressure_plate` | 2 | 1 | 6 | barn 6 | stone pressure plate / keep | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K22 | `gilded_blackstone` | 2 | 2 | 6 | desert_temple 4, taiga_forge 2 | deepslate gold ore / raw gold block | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K23 | `blackstone_stairs` | 2 | 1 | 6 | gold_washing_facility 6 | cobbled deepslate stairs / scorchia stairs | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K24 | `chiseled_polished_blackstone` | 2 | 1 | 1 | stronghold 1 | chiseled deepslate | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K25 | `soul_lantern` | 3 | 22 | 441 | spire 221, ancient_city 128, frosted_dungeon 20 ... | lantern / keep | DONE 2026-10-30 (decided 2026-10-29): lantern (state kept) in every Overworld structure except ancient cities and sculk structures (rule 23): 311 swapped. Kept: ancient city 128, Philip's bone dungeon 2 (sculk in its start room), Nether-shared ship layout 7, `plan/move_to_nether` 29 |
| K26 | `magma_block` | 3 | 19 | 274 | jungle_temple 177 (34 swapped in), desert_temple 48, underground_bunker 10 ... | keep (the 2026-10-12 netherrack target) / scorchia | keep (2026-10-19: blackstone, basalt and magma are fine in the Overworld) |
| K27 | `soul_fire` | 3 | 2 | 177 | ancient_city 173, sculk_dungeon 4 | keep (on kept soul sand) / fire / drop | DONE 2026-10-19 (round 2): fire (rule 20); the frosted dungeon's stays put out (rule 6). Fire on the ancient city's kept soul sand turns back into soul fire on any block update next to it (vanilla `BaseFireBlock.getState`) |
| K28 | `crimson_trapdoor` | 3 | 3 | 39 | fortified_desert_village 36, witch_hut 2, taiga_forge 1 | mangrove trapdoor || DONE 2026-11-02 (`netherblockswap #4`): mangrove, state kept (rules 26 to 32) |
| K29 | `crimson_button` | 3 | 3 | 28 | stronghold 24, desert_temple 2, pillager_outpost_jungle 2 | mangrove button || DONE 2026-11-02 (`netherblockswap #4`): mangrove, state kept (rules 26 to 32) |
| K30 | `warped_pressure_plate` | 3 | 1 | 24 | desert_temple 24 (puzzle) | azalea / dark oak pressure plate || DONE 2026-11-02 (`netherblockswap #4`): dark oak, state kept (rules 26 to 32) |
| K31 | `warped_trapdoor` | 3 | 1 | 8 | spire 8 | azalea / dark oak trapdoor || DONE 2026-11-02 (`netherblockswap #4`): dark oak, state kept (rules 26 to 32) |
| K32 | `soul_wall_torch` | 3 | 2 | 8 | jungle_temple 7, spire 1 | wall torch | DONE 2026-10-30 (decided 2026-10-29): wall torch, facing kept, same exceptions as K25 (rule 24): 12 swapped, incl. 4 in Creative Delight's snowy depot house (Veggies Delight village piece, added after the scan); standing soul torch -> torch (rule 25, none in templates) |
| K33 | `crimson_slab` | 3 | 1 | 6 | desert_temple 6 | mangrove slab || DONE 2026-11-02 (`netherblockswap #4`): mangrove, state kept (rules 26 to 32) |
| K34 | `glowstone` | 3 | 2 | 5 | church 4, big_villager_tent 1 | yellow corundum (the sky village choice) / keep | DONE 2026-10-19 (round 2): yellow corundum (rule 21); the Mysticrift mangrove tower's stays gold ore (rule 14) |
| K35 | `warped_stairs` | 3 | 1 | 1 | zombie_dungeon 1 | azalea / dark oak stairs || DONE 2026-11-02 (`netherblockswap #4`): dark oak, state kept (rules 26 to 32) |
| K36 | `stripped_warped_hyphae` | 3 | 1 | 1 | ocean_ruins 1 | stripped azalea / dark oak wood || DONE 2026-11-02 (`netherblockswap #4`): dark oak, state kept (rules 26 to 32) |

### 2.4 Structures one by one (S)

Use an S row to drop a structure, or to give it a choice that differs from the K rows. A blank Decision follows the K
rows. "Swapped in" = Nether blocks the current swaps put there (already counted in the left column).

| Row | Structure | Group | T1 | T2 | T3 | Nether blocks left | Swapped in | Note | Decision |
|---|---|---|---:|---:|---:|---|---|---|---|
| S1 | `philipsruins:lost_soul_city` | standalone | 1172 | 2 | 3 | soul_soil 839, soul_sand 331, ancient_debris 2, nether_wart 2, polished_blackstone_pressure_plate 2, soul_lantern 1 |  | move to Nether (exempt) | |
| S2 | `philipsruins:lost_soul_dungeon` | standalone | 670 | 363 | 18 | soul_soil 336, polished_basalt 335, soul_sand 330, basalt 28, soul_lantern 17, nether_wart 4, soul_campfire 1 |  | move to Nether (exempt) | |
| S3 | `minecraft:ancient_city` | standalone | 173 | 705 | 301 | polished_basalt 705, soul_sand 173, soul_fire 173, soul_lantern 128 |  | soul sand kept (2026-10-12) | |
| S4 | `underground_bunkers:underground_bunker` | standalone |  | 59 | 10 | basalt 52, magma_block 10, polished_basalt 7 | basalt 30 |  | DONE 2026-10-19: warts removed. It had no netherrack: its soul sand was already basalt (rule 17) |
| S5 | `philipsruins:start_nether_ruin` | ruins | 13 | 2287 | 610 | polished_blackstone_bricks 910, polished_blackstone 622, blackstone 334, nether_bricks 313, polished_blackstone_brick_stairs 97, blackstone_slab 90, nether_brick_fence 79, polished_blackstone_brick_slab 70, red_nether_bricks 61, blackstone_stairs 54, gilded_blackstone 49, nether_brick_stairs 43, nether_brick_slab 27, nether_brick_wall 25, crimson_nylium 21, red_nether_brick_stairs 20, nether_wart_block 15, polished_blackstone_slab 14, chiseled_polished_blackstone 13, netherrack 13, polished_blackstone_stairs 13, polished_blackstone_button 11, crying_obsidian 7, crimson_fungus 3, polished_blackstone_brick_wall 3, soul_lantern 3 |  | move to Nether (exempt) | |
| S6 | `philipsruins:sculk_dungeon` | standalone | 4 | 36 | 4 | polished_basalt 17, basalt 10, polished_blackstone_brick_wall 8, soul_soil 4, soul_fire 4, blackstone 1 |  | soul sand/soil kept (2026-10-12) | |
| S7 | `eugene:red_roof_castle` | settlements |  |  | 3 | magma_block 3 | magma_block 3 |  | DONE 2026-10-19: magma |
| S8 | `animal_barn:barn` | remote |  | 822 |  | polished_blackstone_stairs 410, polished_blackstone_slab 248, polished_blackstone 158, polished_blackstone_pressure_plate 6 |  |  | |
| S9 | `philipsruins:underground_structures` | standalone |  | 603 | 17 | polished_basalt 363, basalt 240, soul_lantern 17 | basalt 204 |  | |
| S10 | `terralith:fortified_village` | settlements |  | 480 | 5 | basalt 480, magma_block 4, soul_lantern 1 |  |  | |
| S11 | `simply_houses:plains_mansion` | remote |  | 315 | 1 | polished_basalt 171, basalt 144, soul_lantern 1 |  |  | |
| S12 | `towns_and_towers:exclusives/pillager_outpost_swedish` | pillager_settlements |  | 312 |  | polished_blackstone_bricks 141, polished_blackstone_brick_stairs 41, polished_blackstone_brick_slab 40, polished_blackstone_brick_wall 36, cracked_polished_blackstone_bricks 30, polished_blackstone 24 |  |  | |
| S13 | `terralith:underground/frosted_dungeon` | standalone |  | 233 | 20 | polished_basalt 233, soul_lantern 20 |  |  | |
| S14 | `minecraft:pillager_outpost` | pillager_settlements |  | 174 | 12 | polished_blackstone_button 96, polished_blackstone_wall 54, polished_blackstone 24, soul_lantern 12 |  |  | |
| S15 | `simply_houses:taiga_forge` | remote |  | 168 | 9 | basalt 74, polished_basalt 71, blackstone 8, polished_blackstone_brick_slab 5, magma_block 4, soul_lantern 4, polished_blackstone_brick_wall 3, blackstone_slab 3, gilded_blackstone 2, polished_blackstone_brick_stairs 2, crimson_trapdoor 1 |  |  | |
| S16 | `betterstrongholds:stronghold` | standalone |  | 154 | 25 | polished_blackstone_slab 64, polished_blackstone_stairs 48, polished_blackstone 31, crimson_button 24, crying_obsidian 10, chiseled_polished_blackstone 1, soul_lantern 1 |  |  | |
| S17 | `betterdeserttemples:desert_temple` | surface_dungeons |  | 130 | 80 | magma_block 48, polished_blackstone 44, blackstone_wall 32, polished_blackstone_stairs 29, warped_pressure_plate 24, blackstone 10, blackstone_slab 8, crimson_slab 6, gilded_blackstone 4, polished_blackstone_button 3, crimson_button 2 |  |  | |
| S18 | `simply_houses:plains_house` | remote |  | 97 | 1 | basalt 50, polished_basalt 47, soul_lantern 1 |  |  | |
| S19 | `philipsruins:level_two_ruins_pool` | ruins |  | 37 |  | polished_basalt 34, basalt 3 | basalt 3 |  | |
| S20 | `terralith:spire` | surface_dungeons |  | 30 | 230 | soul_lantern 221, blackstone 26, warped_trapdoor 8, polished_blackstone_brick_wall 4, soul_wall_torch 1 |  |  | |
| S21 | `towns_and_towers:pillager_outpost_ocean` | boats |  | 20 |  | basalt 20 |  |  | |
| S22 | `towns_and_towers:village_mushroom_fields` | settlements |  | 13 | 2 | basalt 11, soul_lantern 2, polished_basalt 2 |  |  | |
| S23 | `villager_gold_washing_facility:villager_gold_washing_facility` | remote |  | 10 |  | blackstone_stairs 6, polished_blackstone_slab 4 |  |  | |
| S24 | `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | pillager_settlements |  | 9 |  | crying_obsidian 9 |  |  | |
| S25 | `oldvillagerstatue:old_villager_statue` | ruins |  | 8 |  | crying_obsidian 8 |  |  | |
| S26 | `philipsruins:ancient_crypt` | standalone |  | 6 |  | basalt 6 | basalt 6 |  | |
| S27 | `desert_village_temple:village_temple` | remote |  | 4 | 3 | blackstone_slab 4, magma_block 3 |  |  | |
| S28 | `philipsruins:rare_ruin` | ruins |  | 3 | 97 | twisting_vines_plant 51, weeping_vines_plant 10, nether_brick_fence 8, twisting_vines 8, soul_lantern 8, nether_wart_block 4, weeping_vines 4, crying_obsidian 3, warped_wart_block 2, crimson_trapdoor 2 |  | move to Nether (exempt) | |
| S29 | `antiquetradingship:antique_trading_ship` | boats |  | 2 |  | polished_blackstone_button 2 |  |  | |
| S30 | `towns_and_towers:village_forest` | settlements |  | 2 |  | blackstone 2 |  |  | |
| S31 | `betterjungletemples:jungle_temple` | surface_dungeons |  |  | 184 | magma_block 177, soul_wall_torch 7 | magma_block 34 |  | |
| S32 | `terralith:fortified_desert_village` | settlements |  |  | 44 | crimson_trapdoor 36, magma_block 8 | magma_block 8 |  | |
| S33 | `simply_houses:taiga_house` | remote |  |  | 9 | soul_lantern 7, magma_block 2 |  |  | |
| S34 | `simply_houses:taiga_inn` | remote |  |  | 9 | soul_lantern 9 |  |  | |
| S35 | `hopo:underwater_ruins` | ocean_ruins |  |  | 5 | magma_block 5 |  |  | |
| S36 | `eugene:witch_wagon` | vehicles |  |  | 4 | soul_lantern 4 |  |  | |
| S37 | `gothic_church:church` | remote |  |  | 4 | glowstone 4 |  |  | |
| S38 | `philipsruins:pumpkin_ruins` | ruins |  |  | 3 | soul_lantern 3 |  |  | |
| S39 | `betterdungeons:zombie_dungeon` | entrance_dungeons |  |  | 2 | warped_stairs 1, soul_lantern 1 |  |  | |
| S40 | `betterwitchhuts:witch_hut` | witch_huts |  |  | 2 | crimson_trapdoor 2 |  |  | |
| S41 | `minecraft:village_savanna` | settlements |  |  | 2 | magma_block 2 |  |  | |
| S42 | `minecraft:village_taiga` | settlements |  |  | 2 | magma_block 2 |  |  | |
| S43 | `philipsruins:bone_dungeon` | standalone |  |  | 2 | soul_lantern 2 |  |  | |
| S44 | `simply_houses:forest_fort` | remote |  |  | 2 | soul_lantern 2 |  |  | |
| S45 | `towns_and_towers:pillager_outpost_jungle` | pillager_settlements |  |  | 2 | crimson_button 2 |  |  | |
| S46 | `towns_and_towers:pillager_outpost_snowy_slopes` | pillager_settlements |  |  | 2 | soul_lantern 2 |  |  | |
| S47 | `towns_and_towers:village_swamp` | settlements |  |  | 2 | soul_lantern 1, magma_block 1 |  |  | |
| S48 | `villager_forge:villager_forge` | remote |  |  | 2 | magma_block 2 |  |  | |
| S49 | `farmers_structures:cooks` | remote |  |  | 1 | magma_block 1 |  |  | |
| S50 | `illager_siege_weapons:catapult` | vehicles |  |  | 1 | magma_block 1 |  |  | |
| S51 | `illager_siege_weapons:trebuchet` | vehicles |  |  | 1 | magma_block 1 |  |  | |
| S52 | `minecraft:village_desert` | settlements |  |  | 1 | magma_block 1 |  |  | |
| S53 | `minecraft:village_plains` | settlements |  |  | 1 | magma_block 1 |  |  | |
| S54 | `philipsruins:ocean_ruins` | ocean_ruins |  |  | 1 | stripped_warped_hyphae 1 |  |  | |
| S55 | `plains_villager_tents:big_villager_tent` | remote |  |  | 1 | glowstone 1 |  |  | |
| S56 | `simply_houses:forest_stonemason` | remote |  |  | 1 | soul_lantern 1 |  |  | |
| S57 | `simply_houses:taiga_woodcutter` | remote |  |  | 1 | soul_lantern 1 |  |  | |
| S58 | `towns_and_towers:village_old_growth_taiga` | settlements |  |  | 1 | magma_block 1 |  |  | |

Not in the sheet: the vanilla ruined portals (off). `unwrecked_ships:ship`'s crimson/warped pieces belong to the
disabled Nether ship: Overworld ships never take them (scan section 3, "Shared").

## 3. What the pack changes

Already in place before the sheet: the 19 structure swap rules of 2026-10-12 ([nether_swaps.md](nether_swaps.md),
[structure_tuning.md](structure_tuning.md) section 7 Done). Their basalt and magma targets show up in section 2 as
"swapped in".

Built 2026-10-19 (first round of decisions):

| Rows | Change | Where |
|---|---|---|
| B1 | Scarlet Mountains: the netherrack rule becomes a per-block mix, jasper 90 % and crimsite 10 %; the crimson nylium rule is dropped (the mix shows on top instead); `crimson_spread` and `crimson_patch` no longer generate there | `nether_worldgen_rules.json` -> copies of Terralith's `overworld` and `large_biomes` noise settings in `kubejs/data/minecraft/worldgen/noise_settings/`; `forge:remove_features` modifier `creativeengineering:nether_worldgen/scarlet_mountains_crimson` |
| B8 | Moonlight grove/valley: the nylium disk places grass; warped roots and (round 2) nether sprouts out of the blue flower mix, and the patch converted from a nylium-only nether vegetation patch to a vanilla flower patch, so it generates on grass | overrides of `terralith:flower/blue/nylium_disk` and `flowers` (configured features); `gen_wild_flowers.py` rerun so `flower_features.json` (which `wild_flowers.js` rebuilds flower features from) has the new patch |
| B9 | Mirage Isles: enchanted oak trunks are stripped acacia logs | override of `terralith:enchanted/enchanted_oak` |
| S4, K2 | Underground bunker: the 30 nether wart removed | rule 3 in `nether_swap_rules.json` |
| S7, K4 | Red roof castle: 3 netherrack -> magma | rule 16 (existing), swap file regenerated |
| F7 | Create: Dreams & Desires' deep layered rock (breccia, gabbro, granite, glowstone layers) is granite only | literal `kubejs/data/create_dd/worldgen/configured_feature/striated_eroded_overworld.json`: Create's `layered_ore` with two granite layers (one layer crashes Create: `LayerPattern.rollNext` never repeats the previous layer, `nextInt(0)`) |
| K27 | Soul fire -> fire in every structure (ancient city 173, Philip's sculk dungeon 4); the frosted dungeon's stays put out | rule 20 |
| K34 | Glowstone -> Quark yellow corundum in every structure (gothic church 4, villager tent 1); the Mysticrift mangrove tower's stays gold ore | rule 21 |
| (biomes) | Rarest Overworld biomes lifted to 0.15 %, paid by the 12 largest land biomes; the 9 never-spawning surface biomes given target shares; deep cave samples | [biome_tuning.md](biome_tuning.md) 2.1 |

Built 2026-10-30 (K25, K32, decided 2026-10-29; TODO `structures/netherblockswap #3`):

| Rows | Change | Where |
|---|---|---|
| K25 | Soul lantern -> lantern (`hanging`, `waterlogged` kept) in every Overworld structure but ancient cities and sculk structures: 311 in 34 templates (spire 221, frosted dungeon 20, IPO outpost tops 12, Simply Houses, witch wagon, T&T, YUNG's zombie dungeon and stronghold 1 each ...) | rule 23 |
| K32 | Soul wall torch -> wall torch (`facing` kept): 12 (YUNG's jungle temple 7, Veggies Delight snowy depot house 4, spire 1); standing soul torch -> torch (none today) | rules 24, 25 |
| (generator) | `gen_nether_swaps.py`: `except_holding` (skip every structure any of whose templates holds a matching block) and the `@sculk` selector (block id path contains `sculk`); target state carry-over falls back by id suffix (`FAMILY_KEEP`: trapdoor, button, pressure plate, slab, stairs, wall, wall torch, lantern, log/wood/hyphae/stem) after the exact-id `KEEP`. The old 1084 swaps came out byte-identical before the rules were added | `tools/gen_nether_swaps.py` |
| (YUNG's dungeons) | `"Enable Nether Blocks in Dungeons" = false`: the dungeon processor no longer places soul lanterns, soul campfires or soul sand (the template scan can't see processor blocks); backup in `configs_edited/root_config/` | `config/betterdungeons-forge-1_19.toml` |

Sculk structures (`@sculk`, from the template palettes, 2026-10-30): `minecraft:ancient_city` (sculk sensors; also
listed by id), `philipsruins:sculk_dungeon` (sculk, sculk bone bricks, catalysts, shriekers; no soul lights) and
`philipsruins:bone_dungeon` (5 sculk in `bone_dungeon_start`; keeps its 2 soul lanterns). No structure tag marks sculk
structures; the list is recomputed on every generator run, so a new sculk structure is excluded without a rule edit.

How the mix works: per 16-block Y band, a `vertical_gradient` condition whose gradient spans 1900 blocks centred
so its chance is 10 % in the middle of the band (about 9.6 % to 10.4 % across it), then jasper. Each choice of a
mix has its own random name, so the picks are independent. Measured in a new scratch world: 4,454 crimsite in 45,066
blocks of the mountain layer (9.9 %).

The nylium disk only turns underwater grass into nylium, so with grass as its block it changes nothing. The blue
flower patch is the vanilla `nether_forest_vegetation` type, which only places when the block under it is nylium:
with the nylium gone, its cornflowers, orchids, ferns and sprouts no longer generate. They only ever came from those
underwater disks.

How each remaining kind of decision would be built:

| Decision | Where it would go |
|---|---|
| Structure block swap (K, S) | a rule in `nether_swap_rules.json`, then `python3 tools/gen_nether_swaps.py`. Several targets ("X + Y") need a new list form of `to`, picked by position |
| Drop a structure (S) | `structure_groups.json` `enabled: false` |
| Surface rule swap (B) | a `surface` rule in `nether_worldgen_rules.json` (block, "remove" or a weighted mix), then `python3 tools/gen_nether_worldgen.py`. Its noise settings copies must be regenerated on a Terralith update |
| Terralith feature swap or drop (B) | a `features` or `remove_features` rule in `nether_worldgen_rules.json`, then `gen_nether_worldgen.py` |
| Java feature drop (F1 to F4) | a `forge:remove_features` modifier, or a `forge:none` override of Volcanic Caverns' modifier |
| Java feature swap (F1 to F4) | a json placed feature of the pack's own replacing it (Volcanic Caverns' pillar and spike types are its own Java features: a swap needs them re-registered in json or a mixin) |
| Carver (F5) | a `forge:none` override of `upgrade_aquatic:add_carver/underwater_canyon` (drops the canyons too) or a mixin on the floor block |
| YUNG's obelisk (F6) | drop with `forge:remove_features`; a swap through `TemplateBlockSwaps` if YUNG loads it through the template manager (unchecked) |
| Drop a biome (B) | `biome_config.json` `replace_with` |

## 4. Limits and decisions

- **Static scan.** The biome side reads jar and `kubejs/data` json, not the running game. Code-added features are
  covered by the tool's hand tables (`JAVA_FEATURES`, `CODE_ADDED`), built from a search of every mod jar's
  worldgen classes for Nether `Blocks` fields (2026-10-19). A mod that builds blocks by id string would slip through.
- **Structure processors** that place Nether blocks are not counted (YUNG's zombie dungeon stairs processor uses
  soul lanterns and warped stairs; its stronghold ore processor names nether gold ore). The same blind spot as
  [nether_blocks_scan.md](nether_blocks_scan.md).
- **Noise settings copies.** The surface change replaces Terralith's whole `overworld` and `large_biomes` noise
  settings with generated copies. A Terralith update leaves the copies on the old version until
  `gen_nether_worldgen.py` runs again; the generator refuses to overwrite files it doesn't own.
- **Fire on the ancient city's soul sand.** Vanilla fire decides its own kind from the block under it
  (`BaseFireBlock.getState`): on soul sand or soul soil, any block update next to it turns it back into soul fire.
  The template places plain fire and jigsaw pieces don't update shapes, so it generates as fire; a player breaking
  or placing a block next to it brings the soul fire back. Swapping the soul sand under those fires (kept since
  2026-10-12) would stop that.
- **Flower snapshot order.** `wild_flowers.js` rebuilds every Terralith flower feature from
  `flower_features.json`, so a change to a flower feature only lands after `gen_wild_flowers.py` reruns
  (`/cegen` runs `nether_worldgen` before `wild_flowers`).
- **Per-block mix, not clumps.** Surface rule noise is sampled in 2D (one value per column), so clumps would be
  vertical streaks through the mountain; the mix is random per block instead. Crimsite veins as clumps would need an
  ore feature instead (not done).
- **Surface rule order is not simulated.** Each Nether result is listed with its conditions. An earlier rule in the
  same sequence can still win, so a hit means "can place", not "always places". Scarlet Mountains' netherrack is the
  last fallback, after its deepslate/blackstone stripes and nylium tops.
- **Template-placing features** are read through any config `location` (YUNG's Extras is the only user). Vanilla
  fossils are clean.
- **0 % biomes** (volcanic crater, basalt cliffs, mantle and frostfire caves, caldera) were in Terralith's biome
  source but never hit in the scan's climate samples. Since the 1.0.9 rarity floor and cave samples
  ([biome_tuning.md](biome_tuning.md) 2.1) `biome_config.json` gives them shares (volcanic crater 0.15 %, basalt
  cliffs 0.295 %, caldera 0.24 %; mantle caves 0.725 % and frostfire caves 1.196 % of the cave layer), so B3, B7, B11
  and B12 should now generate in new worlds (model shares; WG-13 checks the caves in game).
- **Pool-walk over-reach** (an Overworld ship "reaching" the Nether ship's pieces) is split out as "Shared", the same
  safety rule the swap generator uses.

## 5. File map

- [../tools/nether_worldgen_scan.py](../tools/nether_worldgen_scan.py): the scan. `JAVA_FEATURES`, `CODE_ADDED`,
  `TYPE_BLOCKS` and `JAVA_CHECKED` hold what was read from decompiled jars.
- [../tools/gen_nether_worldgen.py](../tools/gen_nether_worldgen.py) (`/cegen run nether_worldgen`),
  [../kubejs/generation_scripts/worldgen/nether_worldgen_rules.json](../kubejs/generation_scripts/worldgen/nether_worldgen_rules.json),
  `nether_worldgen_manifest.json`: the biome changes. Output: `kubejs/data/minecraft/worldgen/noise_settings/{overworld,large_biomes}.json`,
  `kubejs/data/terralith/worldgen/configured_feature/{flower/blue/nylium_disk,flower/blue/flowers,enchanted/enchanted_oak}.json`,
  `kubejs/data/creativeengineering/forge/biome_modifier/nether_worldgen/scarlet_mountains_crimson.json`.
- [../tools/nether_blocks_scan.py](../tools/nether_blocks_scan.py): `scan(swaps)` (shared with the pre-swap report), `load_swaps()`.
- [../tools/gen_nether_swaps.py](../tools/gen_nether_swaps.py), [../kubejs/generation_scripts/worldgen/nether_swap_rules.json](../kubejs/generation_scripts/worldgen/nether_swap_rules.json), `nether_block_swaps.json`: the structure swaps.
- [nether_worldgen_scan.md](nether_worldgen_scan.md) (generated), [nether_blocks_scan.md](nether_blocks_scan.md) (generated, before swaps), [nether_swaps.md](nether_swaps.md) (generated, swaps in place).
- [../kubejs/generation_scripts/worldgen/biome_config.json](../kubejs/generation_scripts/worldgen/biome_config.json): biome shares, `replace_with`.

## 6. Offline checks

- `python3 tools/nether_worldgen_scan.py [--json OUT] [--no-structures]`: rewrites the scan. Check that its
  "Json-less mod features" table has no CHECK BY HAND row after adding mods.
- `python3 tools/nether_worldgen_scan.py` section 4 (2026-11-02): structure processor lists, the Nether blocks each
  puts out (input and location predicates and YUNG's `target_block` skipped).
- `python3 tools/block_census.py <world> <block>... [--dim overworld|nether|end] [--max-files N]`: counts blocks in
  a save's region files from the section palettes (no dependencies; 3,500 chunks in about 5 s). 2026-11-02: no
  local world has chunks generated after 1.0.9 (`WorldG`, 2026-10-12, holds 32,204 glowstone in 191 chunks, 157 of
  them at Y 0 and below: the old D&D layer), so NW-21 needs a fresh world.
- `python3 tools/nether_blocks_scan.py`: the before-swaps report. It was unchanged by the 2026-10-19 refactor
  (identical output).
- `python3 tools/gen_nether_worldgen.py --dry-run`: every rule must match something (it stops with ERROR when a rule
  matches nothing, for example after a Terralith update renames a block result).
- Round 2 scratch runs 2026-10-19 (worlds `nw4` to `nw7`): the first single-layer granite pattern crashed chunk
  generation (`Bound must be positive` in `LayerPattern.rollNext`); with two layers, clean. `nw7`: 610 full chunks
  have no breccia or glowstone at Y 0 and below (gabbro left: D&D's separate blob); moonlight grove at 11520 11296
  (25 chunks, generated only after the flower snapshot was rebuilt): cornflower 432, blue orchid 153, their Flower
  Patch versions 106, no nether sprouts, warped roots or nylium. Chunks decorated before the snapshot rebuild still
  had a few sprouts (spill-over into neighbours), so only fresh chunks prove it.
- Scratch server run 2026-10-19 (session scratchpad, seed 7379340783510277000, world `nw2`): boot and world creation
  clean with the noise settings copies; Scarlet Mountains at -800 608 (25 chunks): jasper 40,612, crimsite 4,454 in
  Y 162 to 217, no netherrack, crimson nylium or roots; Mirage Isles at -2976 -2816 (25 chunks): 137 stripped acacia
  logs, no warped stem.

## 7. In-game test suite

All in a world created after these changes (new chunks only).

- [ ] **NW-01 Scarlet stone:** `/locate biome terralith:scarlet_mountains`, go there → the mountain is jasper with
  scattered crimsite (about 1 block in 10), with blackstone and deepslate stripes; no netherrack anywhere.
- [ ] **NW-02 Scarlet top:** same place → no crimson nylium and no crimson roots on top; the top is the same stone mix
  (or snow up high).
- [ ] **NW-03 Moonlight:** `/locate biome terralith:moonlight_grove` → no warped nylium in its ponds, no warped roots.
- [ ] **NW-04 Mirage Isles:** `/locate biome terralith:mirage_isles` → the enchanted oaks have stripped acacia trunks.
- [ ] **NW-05 Large biomes:** a world of type Large Biomes → NW-01 holds there too (the `large_biomes` copy).
- [ ] **NW-06 Bunker:** find an underground bunker (`/locate structure underground_bunkers:underground_bunker`) → the
  basalt farm room has no nether wart.
- [ ] **NW-07 Red roof castle:** `/locate structure eugene:red_roof_castle` → magma where the netherrack was.
- [ ] **NW-08 Moonlight flowers:** NW-03's grove → patches of cornflowers and blue orchids (with ferns and grass);
  no nether sprouts.
- [ ] **NW-09 Deep layer:** mine around Y -40 for a while (or spectator through caves) → granite blobs, no breccia
  and no glowstone in the stone. (Gabbro blobs in deepslate are D&D's own and stay.)
- [ ] **NW-10 Ancient city fire:** `/locate structure minecraft:ancient_city` → the city centre fires are orange
  fire on soul sand; break a block next to one and note whether it turns blue (expected, see section 4).
- [ ] **NW-11 Gothic church:** `/locate structure gothic_church:church` → yellow corundum lights where the glowstone
  was.
- [ ] **NW-13 Spire lanterns:** `/locate structure terralith:spire` → plain lanterns, no soul lanterns; the hanging
  ones still hang from their chains, the standing ones stand; the wall torch at the base is a plain torch on the wall.
- [ ] **NW-14 Jungle temple torches:** `/locate structure betterjungletemples:jungle_temple`, the water parkour and
  floor 2 parkour rooms → plain wall torches, attached to the wall (not floating, not facing into it).
- [x] **NW-15 Ancient city and sculk structures keep theirs:** `/locate structure minecraft:ancient_city` and
  `philipsruins:bone_dungeon` → soul lanterns still there. (Scratch PASSED 2026-10-30, SQ-WG30-B03.)
- [x] **NW-16 YUNG dungeons:** a few zombie and small dungeons (`/locate structure betterdungeons:zombie_dungeon`,
  `betterdungeons:small_dungeon`) → no soul lantern, soul campfire or soul sand. (Scratch PASSED 2026-10-30, SQ-WG30-B04.)
- [ ] **NW-17 Snowy depot house:** a snowy village with Veggies Delight's depot house → plain wall torches.
- [ ] **NW-21 Glowstone census:** fresh world, Chunky radius 1000 around spawn plus one deep cave region, stop the
  server, `python3 tools/block_census.py <save> minecraft:glowstone minecraft:soul_fire minecraft:soul_lantern` →
  0 glowstone in the Overworld (any hit: read its chunk and y, then find the source; player-placed blocks excluded);
  a gothic church found in that area (NW-11) shows yellow corundum.
- Scratch 2026-10-30 (SQ-WG30-B01, B02 PASSED): load line `1412 block swap(s) in 105 template(s)`; `/place template`
  of the spire layer1 / base_l, the water parkour room and the snowy depot house gave 0 soul blocks, lantern `hanging`
  and wall torch `facing` counts equal to the template's soul ones (4 standing spire lanterns on the template's bottom
  layer dropped only because it was placed in mid-air). NW-13, NW-14, NW-17 still want an in-game look at real starts.

## 8. History

- 2026-10-19: scan built (`nether_worldgen_scan.py`; `nether_blocks_scan.scan()` split out with swaps applied) and
  the decision sheet written: 12 biome rows, 6 feature/carver rows, 36 block rows, 58 structure rows. Found the stale
  swap file (`eugene:red_roof`), YUNG's rare obelisk (a feature, missed by the structure scans) and Volcanic Caverns'
  deep basalt/magma.
- 2026-10-19 (round 2, user): moonlight blue flower patches to generate and nether sprouts removed; D&D's deep
  layered rock to granite; soul fire to fire and glowstone to yellow corundum in every structure. Built:
  `gen_nether_worldgen.py` gained `convert` (nether vegetation -> flower patch) and `literal` (whole files);
  `gen_wild_flowers.py` rerun; swap rules 20, 21 (1084 swaps). Verified on scratch worlds (section 6). Found the
  flower snapshot dependency and the soul-sand fire limit (section 4). The scan skips code-added features the pack
  overrides.
- 2026-10-19: first decisions (user): Scarlet netherrack to jasper 90 / crimsite 10, its crimson nylium and roots
  removed; blackstone, basalt and magma fine in the Overworld (B2 to B7, B10, B11, F1 to F5, K5 to K26 bar K19, K25
  kept); the rare desert obelisk kept; Moonlight warped nylium to grass and warped roots removed; Mirage Isles warped
  stems to acacia (stripped, as the original); bunker warts removed (it had no netherrack: its soul sand was already
  basalt); red roof netherrack to magma; rarest biomes more common ([biome_tuning.md](biome_tuning.md)). Built
  `gen_nether_worldgen.py` + `nether_worldgen_rules.json`; bunker rule 3; swaps regenerated (902). Verified on a
  scratch server (section 6). Found D&D's glowstone layer (F7) in that world; the scan gained `CODE_ADDED`.
- 2026-10-19: both rounds DEPLOYED with pack 1.0.9 (15:54, server + clients); new chunks only. NW-01 to NW-11 pending.
- 2026-10-30: K25, K32 built (decided 2026-10-29, K19 kept): rules 23 to 25, generator `except_holding` / `@sculk` /
  `FAMILY_KEEP`, 1407 swaps (323 new), YUNG's dungeon nether blocks off. Offline: old swaps byte-identical, every
  unswapped soul light accounted for (ancient city, bone dungeon, move-to-Nether, Nether-shared ship). NW-13 to NW-17.
- 2026-11-02 (`biomes/netherblocks #4`): glowstone check. Scan gained the processor pass (section 4 of the scan);
  `tools/block_census.py` built. No glowstone source found, no data change. NW-21 pending (fresh world).
- 2026-11-02 (`structures/contents #1`): Philip's lost soul dungeon, lost soul city, rare ruin and blackstone
  Nether ruin moved to the Nether ([structure_tuning.md](structure_tuning.md) 7); `plan/move_to_nether` removed, so
  nothing is exempt from the swaps any more. Swap file unchanged (1407 swaps); the scan's structure table lost the
  four (47 live Overworld structures).
