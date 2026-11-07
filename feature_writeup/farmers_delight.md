# Farmer's Delight 1.3.4 against the installed 1.2.4

**BUILT 2026-10-25, Shipped in 1.1.03 (deployed to server and players 2026-10-26); scratch r1102b 2026-11-02: SQ-FDB-01 to -03, -10, -13 to -15 PASSED, -04 FAILED (Gleaming Salad has no Nourishment), -05 to -07, -09, -11 open (probe faults), -08 needs a player, -12 client** (section 4: repack `-02` in the Creative Delight jar, CD-tree scripts; tests SQ-FDB-01 to 15; deep scan 2026-11-02). Onion Soup left out (owner 2026-11-02). Installed: `FarmersDelight-1.19.2-1.2.4.jar` (`mods/`). Evaluated 2026-10-25 against
`devmods/checkNewerVersionForContent/FarmersDelight-1.20.1-1.3.4.jar` (1.20.1 / Forge 47), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #18` in [todo/TODO.md](todo/TODO.md).

1.3.4 cannot replace 1.2.4 (1.20.1 only), and many installed add-ons need FD's 1.2.x ids, so anything kept arrives by
KubeJS, data or, since 2026-10-25, the overlay of FD's Creative Delight repack (below). **Owner decisions 2026-10-25** are in section 3; the tables keep the original proposal text.

**Licence:** MIT (Forge build, owner-supplied 2026-10-25). Code and art can be reused with the notice kept.


## 1. How it was read

- `python3 tools/jar_diff.py` (669 files added, 275 removed, 784 changed; lang +258 / -96 / ~1).
- Recipes compared semantically (wrappers, groups, `show_notification`, `category` ignored): 91 shared recipes differ, most
  only in the new `tool` list format. Loot tables compared the same way (pool names, number types ignored).
- Food values and effects read from `FoodValues` bytecode (`javap -c`), effects resolved through the lambda bodies.
- New mixins and loot modifiers read by `javap` and the json.

Most of the 1.3 delta is 1.20 porting: hanging signs, cherry and bamboo woods, torchflower, smithing templates,
`forge:conditional` recipes, tool-action matching in loot and cutting recipes, EMI support, recipe advancement regroup
(the pack drops advancements), and about 40 lang keys renamed (`farmersdelight.tooltip.x` -> `tooltip.farmersdelight.x`).

## 2. Changes and proposed decisions

### 2.1 Food values and effects

| Change in 1.3.4 | Decision (proposed) |
|---|---|
| **Comfort is gone from every FD food.** The 10 foods that gave Comfort give Nourishment for the same time: Baked Cod Stew 6000, Beef Stew 3600, Bone Broth 1200, Chicken Soup, Cooked Rice 600, Fish Stew 3600, Fried Rice, Noodle Soup 6000, Pumpkin Soup 6000, Vegetable Soup 3600 (ticks). The Comfort effect itself still exists | **owner's call**, see 2.5. Default: keep Comfort |
| Chicken Soup 14 / 0.75, 6000 [1.2.4] -> 12 / 0.8, 3600 | follows 2.5 (only worth doing together with it) |
| Fried Rice 14 / 0.75, 6000 [1.2.4] -> 12 / 0.8, 3600 | follows 2.5 |
| New `NourishmentAlwaysEatMixin`: food can be eaten at full hunger while Nourishment is active | skip (code; Nourishment already stops the drain) |
| Every other FD food: no value change | none |

### 2.2 New content

| Item / block | 1.3.4 detail | Pack overlap | Decision (proposed) |
|---|---|---|---|
| Onion Soup | 2 onion + bread + milk in the pot, 12 / 0.8, Nourishment 3600 | Brewin' Creamy Onion Soup (`i.creamy_onion_soup`), Miner's Delight Onion Soup cup | **TODO: evaluate adding** (owner 2026-10-25): compare against Brewin's Creamy Onion Soup and Miner's Delight's cup (effect, value, role) and decide whether it stays alongside, merges, or is dropped |
| Gleaming Salad (feast, 4 bowls) | 2 glow berries, honey bottle, tomato, golden carrot, beetroot, 2 cabbage, bowl; serving 14 / 0.75, Nourishment 6000 | none (Display Delight has placed salads only) | optional: a pack item would need a new feast block (patcherMod or KubeJS block + loot); owner's call |
| Placeable vanilla Pumpkin Pie + Slice of Pumpkin Pie (`PlacePumpkinPieMixin`, knife slicing loot modifier, 4 slices) | | Create Central Kitchen placed pie and slice (`i.pumpkin_pie_slice`), Delightful pie and slice (`i.delightful_pumpkin_pie_slice`) | skip (already twice) |
| Rope Fence (4 rope + 2 sticks -> 3), Rope Fence Gate (2 rope + 4 sticks); fences connect to rope (`RopeFenceConnectionMixin`) | | none | optional; owner's call. Ties to `materials #2` (rope merge). Fence and gate are plain blocks (KubeJS block types); the rope connection needs a patcherMod mixin |
| Tomatoes on Rope (`HangingTomatoBlock`) | split out of the tomato block | 1.2.4 tomatoes already climb rope | skip (same behaviour) |
| Basket renamed `wooden_basket`, new Bamboo Basket | | `farmersdelight:basket` | skip (id rename only; bamboo variant needs 1.20 bamboo) |
| Bamboo and Cherry Cabinets | | Every Compat `i.bamboo_cabinet` (Quark), `i.cherry_cabinet` (Vinery) | skip (covered) |
| Hanging Canvas Signs (17), wall canvas sign keys | 1.20 hanging signs | Supplementaries hanging signs | **BUILT** only the undyed ceiling `farmersdelight:hanging_canvas_sign` (state `rotation` 0-15, `attached`, `waterlogged`, so Farmer's Structures places); no text, block entity, renderer, dyes or wall variant. See 4.2 |

### 2.3 Recipes

| Change | Decision (proposed) |
|---|---|
| Ingredients generalised to tags: `minecraft:carrot / potato / beetroot / wheat` -> `forge:crops/*` in 25 recipes; cooking pot takes `#forge:buckets/water` | skip by default: the pack's tag work already makes most of these interchangeable; revisit only if a modded crop fails a recipe |
| Noodle Soup: `forge:cooked_eggs` -> `forge:eggs` (raw egg) | skip (the pack's own noodle soup craft, `cooking.js`, uses cooked eggs) |
| Rabbit Stew (pot): baked potato -> any potato | skip |
| Kelp Roll: carrot -> any `forge:vegetables` | skip (the pack builds kelp roll by sequenced assembly, `cooking.js`) |
| Vegetable Noodles: brown mushroom -> any `forge:mushrooms`; Cabbage Rolls: the `cabbage_roll_ingredients` tag inlined; Dog Food: `wolf_prey` -> `forge:raw_meat` | skip |
| Wheat dough from water is a custom `farmersdelight:dough` recipe (keeps the bucket); dough from egg same as 1.2.4 | skip (same result) |
| **Salvaging** (cutting board): per-wood door / sign / trapdoor recipes (axe, 1 plank) become one recipe per wood that also takes fence, fence gate, pressure plate, button, boat and cabinet (axe, 75% chance of 1 plank); new: chest boats -> boat + chest, chest / furnace / hopper / TNT minecarts -> minecart + part (hoe, as upstream lists it); the 4 leather armor recipes merged into one; horse armor, saddle, stone, bricks and the rest only moved folders, same results | **carry over** as data through `NCH.fdCutting`: the wider furniture inputs and the boat / minecart splits (vanilla 1.19.2 items only; skip hanging sign, bamboo, cherry). Keep 1.2.4's guaranteed plank on doors, signs and trapdoors unless the owner wants the 75% |
| Torchflower -> 2 orange dye | skip (1.20 plant) |
| Netherite knife: smithing template | skip (1.20 smithing) |

### 2.4 Loot, worldgen, tags, behaviour

| Change | Decision (proposed) |
|---|---|
| New loot modifiers: onion from zombies, husks, zombie villagers and from pillagers, evokers, vindicators (2%, +1% per Looting level, player kill only) | optional, data-only (KubeJS loot rules, `loot/07_rules.js`); owner's call |
| New loot modifier: breaking a pumpkin with a knife (no Silk Touch) gives 4 pumpkin slices | optional, data-only |
| Block and chest loot tables: shears matched by tool action, explosion decay added; no item, count or weight change | skip |
| Wild crop and mushroom colony features moved to data (`patch_*`), biome modifiers on biome tags | skip: the pack's wild crop system (`worldgen/wild_crops.js`) replaces FD's modifiers |
| `KeepRichSoilUntrampledMixin`: rich soil farmland is not trampled | optional, patcherMod mixin; owner's call |
| `VillagersTargetRichSoilMixin`: farmer villagers work rich soil farmland | optional, patcherMod mixin; owner's call |
| New tags (`feasts`, `pies`, `meals`, `drinks`, `snacks`, `sweets`, `serving_containers`, `straw_harvesters`, `mushroom_colony_growable_on`, `unaffected_by_rich_soil`, `planted_from_below`, `cabinets`, storage block tags) | skip: FD 1.2.4 reads none of them. `feasts` / `pies` / `meals` could be filled as pack tags later if a feature needs them |
| Feedback lines: "You need a %s to eat this", skillet "Can't cook underwater", rice "must be planted in shallow water", cooking pot "Served on: %s", cutting board "This doesn't seem cuttable..." | skip (code, UI feedback only) |
| Retextures: 31 item and 24 block textures (soups, stuffed pumpkin, shepherd's pie, rice crops, rope, safety net, canvas signs, straw, raw pasta, cutting board) | optional, visual; a resource-pack override (`kubejs/assets/farmersdelight/`). `mods.toml` declares MIT for both versions; owner's call |

### 2.5 Comfort or Nourishment (decided 2026-10-25: option A)

FD 1.3 and the 1.3-era add-ons (Veggies 1.9.3, Brewin' 4.5) moved soups from Comfort to Nourishment. In the pack's
food dump 57 foods give Comfort: FD 10, Veggies (port) 9, pack cans 19 (copies of the others), Ender's Delight 4,
Corn Delight 3, Delightful 3, Fruits Delight 3, Farmer's Respite 2, four others 1 each. [food.md](food.md) treats Comfort
as the soup effect (types table: soup = Comfort, meal = Nourishment).

- **A. Keep Comfort. CHOSEN.** The 10 FD soups and stews keep Comfort and their 1.2.4 values (Chicken Soup and Fried Rice
  stay 14 / 0.75, 6000). Comfort stays in Creative Delight too. Nothing to build.
- B. Follow FD 1.3 for FD and Veggies only (19 foods). Not chosen.
- C. Retire Comfort from food pack-wide. Not chosen.

The 9 Veggies foods upstream 1.9.3 moved to Nourishment (sweet potato pancakes, coleslaw, broccoli soup, cauliflower soup,
garlic chicken stew, turnip beef stew, cacciatore, mashed potatoes, potato noodles) are Comfort in the port (1.9.2) and in
the 2026-10-25 food dump, so they stay as they are; `veggiesport #3` carries no effect change.

### 2.6 Small recipe and tag changes in detail

**Recipe ingredients made looser (25 shared recipes).** 1.2.4 named vanilla items; 1.3.4 names the Forge tag, so any
modded item in the tag also works. Item -> tag: carrot -> `forge:crops/carrot` (8 recipes: Beef Stew, Chicken Soup, Fried Rice,
Rabbit Stew, Vegetable Noodles, Vegetable Soup, Chicken Sandwich, Roast Chicken), potato -> `forge:crops/potato` (Baked Cod Stew,
Beef Stew, Rabbit Stew [was baked potato], Vegetable Soup, Stuffed Pumpkin), beetroot -> `forge:crops/beetroot` (Beetroot
Soup, Mixed Salad, Roasted Mutton Chops, Ratatouille, Vegetable Soup), wheat -> `forge:crops/wheat` (Apple Pie, Pie Crust,
Honey Cookie, Sweet Berry Cookie, Cake from milk bottle), water bucket -> `forge:buckets/water` (Cooking Pot). In the pack
the tags already hold the modded variants where we merged them, so the gain is small; a pack recipe that names a vanilla
item and is meant to accept a modded one is the only place it would matter. **Decision: skip.**

**Recipes that change meaning, not just wording:**
- Noodle Soup: `forge:cooked_eggs` -> `forge:eggs`. In 1.3.4 a raw egg works; in 1.2.4 it needs a cooked one. The pack's own
  Noodle Soup craft ([cooking.js](kubejs/server_scripts/cooking/cooking.js) line 29) already uses cooked eggs. Skip.
- Kelp Roll: the carrot becomes any `forge:vegetables`. The pack makes Kelp Roll by sequenced assembly, so unused. Skip.
- Vegetable Noodles: the brown mushroom becomes any `forge:mushrooms`. **BUILT** 2026-10-25 (section 4.3).
- Cabbage Rolls: the `farmersdelight:cabbage_roll_ingredients` item tag is inlined as raw meat, raw fish, vegetables or
  mushrooms. Same set; skip.
- Dog Food: `farmersdelight:wolf_prey` becomes `forge:raw_meat`. **BUILT** 2026-10-25 with the pack raw meat food type instead (section 4.3); the last sentence of this bullet gives the tag contents. `forge:raw_meat` lists rabbit and the raw beef, chicken,
  pork and mutton tags. Skip.
- Wheat dough from water: now a custom recipe type (`farmersdelight:dough`) that keeps the bucket; dough from eggs becomes a
  tag recipe with the same output. Same results; skip (the pack's dough rules own this recipe).
- Netherite Knife: `smithing_transform` with a template (1.20 smithing). Skip.

**New tags in 1.3.4 and what each does:**
- Item lists, used by 1.3's tooltips, JEI info and by add-ons: `meals` (30: soups, stews, rice and pasta dishes),
  `snacks` (13: sandwiches, wraps, rolls, dumplings, stuffed potato), `sweets` (12: cake, cookie, pie slices, popsicle,
  custard), `drinks` (4: milk bottle, apple cider, melon juice, hot cocoa), `feasts` (6 feast blocks), `pies` (4 whole pies).
  1.2.4 reads none of them. They can be filled by the pack later; the food-type tags in
  [food.md](food.md) already cover the same ground.
- Block behaviour tags, read by 1.3 code that 1.2.4 does not have: `planted_from_below` (cave vines),
  `mushroom_colonies`, `campfire_signal_smoke` (straw and rice bales make signal smoke), `mushroom_colony_growable_on`,
  `unaffected_by_rich_soil`, `terrain`, `heat_sources` / `tray_heat_sources` / `heat_conductors` (stove and pot heat).
  Item tags `straw_harvesters`, `serving_containers`, `flat_on_cutting_board` and `tools/knives` are likewise read only by
  1.3 code (names from the lang file; contents not read). Without the 1.3 code none of these do anything.
- Vanilla and Forge tag additions: `maintains_farmland` (crop blocks keep farmland wet), `replaceable` (sandy shrub),
  `villager_plantable_seeds` (cabbage seeds, tomato seeds, onion), `combination_step_sound_blocks` (rugs, tatami, cutting
  board), `fences` / `fence_gates` (rope fence), `ceiling_hanging_signs` / `wall_hanging_signs` (1.20), `forge:raw_meat`,
  `forge:buckets/water`, `forge:storage_blocks/*` (crates, bales), and Create's `fan_transparent` (safety net).
  The fence, farmland and villager tags are used by the optional changes in section 3 and are added with them.
- `cabbage_roll_ingredients` and `wolf_prey` tags are removed (inlined in the recipes above).

## 3. Decisions (owner, 2026-10-25) and build plan

| Item | Decision | How it is built |
|---|---|---|
| Comfort vs Nourishment | **A: keep Comfort** on all 10 FD soups and stews, at 1.2.4 values; Comfort stays in Creative Delight | nothing |
| Veggies 9 foods | stay Comfort (they already are) | nothing |
| Salvaging (cutting board recycling) | **BUILT** **carry over, with the 75% chance**: every wood's furniture recipe (door, trapdoor, sign, fence, gate, pressure plate, button, boat, cabinet) returns 1 plank at 75%, replacing the 1.2.4 guaranteed plank on doors, signs and trapdoors; chest boats -> boat + chest; chest / furnace / hopper / TNT minecarts -> minecart + part; 4 leather armor pieces -> leather by shears | `NCH.fdCutting`, vanilla 1.19.2 items and the cabinets the pack has; player-visible: changelog bullet. Also (2026-10-25, decided, not built): Boatload furnace boats and the driftwood set from Abnormals Delight 6.0.3 ([abnormals_delight.md](abnormals_delight.md) 3.1); milk bucket <-> 4 bottles (3.4) |
| Gleaming Salad feast | **BUILT** **add** (recipe in 2.2, 14 / 0.75, Nourishment 6000, 4 servings) | new feast block: patcherMod or KubeJS block, loot, tag, cans and food type tag |
| Rope Fence and Gate | **BUILT** **add** (4 rope + 2 sticks -> 3; 2 rope + 4 sticks) | fence and gate blocks; rope connection by a patcherMod mixin; `fences` / `fence_gates` tags; ties to `materials #2` (rope merge: use the surviving rope) |
| Onion drops | **BUILT** **add** (2%, +1% per Looting level, player kill: zombie, husk, zombie villager, pillager, evoker, vindicator) | KubeJS loot rule in `loot/07_rules.js`; check against the pack's loot config |
| Pumpkin with a knife | **BUILT** **add** (4 pumpkin slices, no Silk Touch) | loot rule |
| Rich soil not trampled | **BUILT** **add** | patcherMod mixin |
| Villagers work rich soil | **BUILT** **add** (and Fertile Soil, configurable) | patcherMod mixin + `villager_plantable_seeds` tag |
| Vegetable Noodles: any mushroom | **BUILT** carry over | change the recipe's mushroom input to `#forge:mushrooms` (`NCH` override of `farmersdelight:vegetable_noodles`) |
| Onion Soup | **LEFT OUT (owner 2026-11-02)** | overlap check with Creamy Onion Soup (now Nourishment in the pack, food.md 2026-10-20) and the Onion Soup cup; then add as a pot recipe plus item, or skip |
| Dog Food: any raw meat | **BUILT** carry over (pack raw meat food type, see 4.3) | change the `wolf_prey` input to `#forge:raw_meat` (override of `farmersdelight:dog_food`) |

**Where the Java goes (staged 2026-10-25, [creative_delight.md](creative_delight.md) 21).** Farmer's Delight 1.2.4 is now a
Creative Delight member, `repackedFarmers` (`development/creativeDelight/members/farmers`): the 1.2.4 jar unchanged plus an
overlay. 1.3.4 is MIT like 1.2.4, and its jar is the donor (`development/creativeDelight/upstream/donors/`, sha1 in the
member's `gradle.properties`), so a 1.3.4 class may be decompiled into the overlay (Q6 hybrid). New blocks and items
(Gleaming Salad, Rope Fence and Gate) and the two rich soil mixins go in the overlay, under the `farmersdelight:` namespace
and FD's own registries pattern, instead of patcherMod; edited FD files (lang, tags) use `replaced_resources`, an edited
class `replaced_classes`. Recipe and loot rows stay KubeJS through the helpers.

Added by the owner 2026-10-25 and built the same day: the placeable vanilla Pumpkin Pie (third pie, with a switch to compare
all three), Rabbit Stew (any potato), Cabbage Rolls (inlined filling), the feedback lines and the Rich Soil tooltip. Only
Onion Soup is left out (owner 2026-11-02). Build detail in section 4.

## 4. Built 2026-10-25 (shipped in 1.1.03, not tested in game)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-FDB-02, -03, -10, -13, -14, -15; FAIL SQ-FDB-04: Gleaming Salad loses Nourishment: no rule in `tools/gen_comfort_nourishment.py` (check after the 19:50 regeneration); OPEN (no verdict, rerun row written) SQ-FDB-05, -06; rerun pending (batch fault) SQ-FDB-07, -09, -11.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-FDB-09, -11; FAIL SQ-FDB-05: live `placeAs` toml edits never reload (no file-watcher reload; `FDBackportConfig.java`, `FDBackportEvents.java`); check by hand first; SQ-FDB-06 OPEN: the vanilla control farmland was not trampled either, so the row proves nothing; SQ-FDB-07 OPEN: MCA replaces summoned villagers and FD's farmland hook does not reach MCA brains; owner decision.

Owner request 2026-10-25: execute the backport; keep Onion Soup for evaluation; add Gleaming Salad, the third placeable
pumpkin pie with a way to compare all three in game, Rope Fence and Gate, the Rabbit Stew / Vegetable Noodles / Cabbage
Rolls / Dog Food changes, salvaging, onion drops, knife pumpkin slicing, untramplable rich soil (tooltip updated),
farmer villagers on rich farmland and Fertile Soil (configurable), and the feedback lines.

### 4.1 Where it is

| Part | Where |
|---|---|
| Java (blocks, items, particle, sounds, config, events) | `development/creativeDelight/members/farmers/src/main/java/com/SaintAlfonzo/creativedelight/farmers/` |
| Mixins (`farmersdelight.ce.mixins.json`, appended to `mixin_configs`) | `.../farmers/mixin/`: `FarmersDelightInitMixin` (calls `FDBackports.init()` at the end of FD's constructor, so registers, config and listeners belong to FD's container), `RopeFenceConnectionMixin`, `VillagersTargetFarmlandMixin`, `SkilletUnderwaterMixin`. SRG names, `remap = false` |
| Assets and data at new paths (47 files from the 1.3.4 donor, MIT) | `members/farmers/src/main/resources/assets/farmersdelight/` (blockstates, models, textures incl. 1.3.4's `block/bowl.png` the salad model needs, `particles/sparkle.json`), `data/farmersdelight/loot_tables/blocks/` (rope fence, gate, salad; `random_sequence` dropped) |
| Edited upstream files (`replaced_resources`, sha1-pinned) | `lang/en_us.json` (6 names, 2 subtitles, rice line, skillet line), `sounds.json` (2 gate sounds, lead-knot events) |
| Recipes (CD tree, `global.CD.recipes`, ids `creativedelight:`) | `kubejs/data/creativedelight/kubejs/farmersdelight/recipes.js` |
| Tags | `.../farmersdelight/tags.js`: cabbage roll filling (runs without CD), `forge:fences` / `forge:fence_gates`, knife-mineable pie (with CD) |
| Loot (LootJS; runs without CD, needs only FD) | `.../farmersdelight/loot.js` |
| Dict keys | `item_dict.js` (end of the farmersdelight section, plus `any_hoe`, `any_shears`, `t_forge_fences`, `t_forge_fence_gates`, `t_wolf_prey`), `block_dict.js` |
| Tooltip | `kubejs/client_scripts/tooltips.js` (`fdVillagerLine`, shown only with `creativedelight` loaded) |
| Version | repack `1.19.2-1.2.4-ce-19.2-02`; `:creativeDelight:build` 2026-10-25 nests it |

### 4.2 Behaviour and choices

- **Config** `config/farmersdelight-ce-common.toml` (registered by the repack, FD's container). A change needs a restart:
  r1103 (SQ-FDB-05) showed a live edit is not picked up; the config comment says so since 2026-11-03.
  - `[pumpkin_pie] placeAs = FARMERSDELIGHT | DELIGHTFUL | CREATE_CENTRAL_KITCHEN | OFF`: the "see all three" switch.
    A `RightClickBlock` handler at HIGHEST (Delightful's runs at NORMAL) places `<mod>:pumpkin_pie` with Delightful's
    rules (not on a Cutting Board, solid block below, room). `OFF` leaves it to the two mods as before (which one won was
    never pinned down). `sneakToPlace` (1.3.4's option) default false. A mod that is missing makes the value act as OFF.
  - `[villagers] farmlandBlocks` (default Rich Soil Farmland and `immersive_weathering:mulch_block`): redirect of the
    `secondaryPoi().contains` call in `SecondaryPoiSensor.doTick` for Farmers only. Non-`FarmBlock` entries are dropped
    with a warning (vanilla `HarvestFarmland` plants only on `FarmBlock`, and already harvests/plants on both soils when
    a villager stands next to them; the sensor is what makes them walk there). Resolved lazily after load/reload. 1.20's
    `villager_plantable_seeds` tag has no 1.19.2 reader, so not added.
  - MCA villagers (the pack replaces vanilla villagers with MCA's) never pass the sensor above: their harvesting chore
    scans for `minecraft:farmland` only. Since 2026-11-03 (SQ-FDB-07) the CD mixin `mca/McaHarvestAnyFarmlandMixin`
    lets that scan take any `FarmBlock` in the Overworld (Rich Soil Farmland, Fertile farmland, modded farmland); other
    dimensions keep MCA's rule. The list above stays for vanilla villagers.
- **Rich soil trample**: a `FarmlandTrampleEvent` cancel for `RichSoilFarmlandBlock` instead of 1.3.4's `turnToDirt`
  mixin; 1.2.4's rich farmland inherits vanilla `fallOn`, so trampling turned it into plain Dirt. Not configurable.
  Tooltip: Rich Soil no longer claims "can't be trampled" (untilled soil is not farmland); Rich Soil Farmland does.
- **Gleaming Salad**: `FeastBlock` subclass, 4 servings, bowl leftovers, light = servings x 3, sparkle particle (1.3.4's,
  ported), food 14 / 0.75 Nourishment 6000, bowl item stacks to 16. 1.20 MapColor/sound become `Material.CAKE`,
  `COLOR_LIGHT_GREEN`, `SoundType.WOOL`. Not yet: cans, food-type tags (the generators pick it up after `/dumpall`).
- **Pumpkin pie**: `farmersdelight:pumpkin_pie` block (no item; pick-block gives the vanilla pie) slicing into the new
  `farmersdelight:pumpkin_pie_slice` (FD `PIE_SLICE` food), 4 slices craft a pumpkin pie. Not added: 1.3.4's cutting-board
  pie -> 4 slices (Delightful already owns that input) and its debug pie item. Once the owner picks one pie, the other
  slices are candidates for `removed_items` / item merges.
- **Rope Fence / Gate**: 1.3.4 classes on 1.19.2. The gate re-implements `use` / `neighborChanged` because 1.19.2's
  `FenceGateBlock` hardcodes the wooden sounds (level events 1008 / 1014). Recipes take `#forge:rope` (any of the three
  ropes; `materials #2`). Fuel 200 ticks.
- **Hanging Canvas Sign** (`HangingCanvasSignBlock`, registry id `farmersdelight:hanging_canvas_sign`, stack 16): a no-collision JSON-model
  block (board and chains from 1.3.4's entity texture `entity/signs/hanging/canvas.png`, copied to `textures/block/`; 8 models,
  `attached` V chains or straight chains, 22.5 degree steps by element rotation plus blockstate y) with 1.3.4's property names. Placed
  under a block that supports the centre (or any non-air block); sneaking while placing sets `attached`, which then pops off when
  the support above goes (unattached never pops). Recipe 6 from 2 Chains, 4 logs, 2 Canvas (1.3.4's, `creativedelight:` id
  `crafting_shaped/hanging_canvas_sign`). Tag `minecraft:mineable/axe`; loot drops itself. Limits: no text, dyes, wall variant
  or hanging-sign tag.
- **Feedback lines**: of 2.4's list only two changed meaning. Rice: "This seed must be planted in shallow water." (lang).
  Held skillet: will not start underwater, "Can't cook underwater..." (mixin). Not ported: "Can't cook while flooded"
  (the 1.2.4 skillet block cannot be waterlogged) and the cutting board's "%s remaining" (1.2.4 boards hold one item).
  The rest are key renames with the same text.

### 4.3 Recipe and tag changes as built

- Salvaging: `farmersdelight:cutting/<wood>_door|sign|trapdoor` (27) removed; per wood one axe recipe (door, trapdoor,
  sign, fence, gate, pressure plate, button, FD cabinet, boat where it exists) -> plank at 75%. Chest boats (7) -> boat +
  chest. Chest / furnace / hopper / TNT minecart -> minecart + block, hoe. Leather armor unchanged (same result as 1.3.4).
  Since 2026-10-25 also Abnormals Delight 6.0.3's set (abnormals_delight.md 3.1): Boatload furnace boats (9, hoe),
  Boatload crimson / warped boat and chest boat, and the driftwood furniture, chest boat and furnace boat.
- Rabbit Stew: baked potato -> `#forge:crops/potato`. Vegetable Noodles: brown mushroom -> `#forge:mushrooms` (FD pot
  recipe only; the pack's `vegetable_noodles_cooked_manual_only` craft in `cooking.js` still names the brown mushroom).
- Dog Food: `#farmersdelight:wolf_prey` -> `#creativeengineering:food_type/raw/meat` (30 meats). 1.3.4's `#forge:raw_meat`
  holds only Miner's Delight's arthropod and spider leg in this pack, so a literal copy would have broken the recipe.
- Cabbage Rolls: instead of inlining, the filling tag gains raw meat, raw fish, `#forge:vegetables`, `#forge:mushrooms`
  and loses `#forge:eggs`, so the pot recipe and the pack's manual craft change together and other mods' entries stay.

### 4.4 Textures preview

A local page (not published) renders every new model in 3D, the item icons, the 47 new textures, and the 55 textures
1.3.4 retextured (not adopted; 2.4 row open: owner picks from the 2026-11-02 side-by-side sheets), plus the three pumpkin pies side by side. Generator in the session
scratchpad (`fd_preview/gen_preview.py`); regenerate there or copy it to `tools/` if it is wanted again.

### 4.5 Open

- Scratch r1102b 2026-11-02 (`perf/runs/r1102b/s1/judged.md`): SQ-FDB-01, -02, -03, -10, -13, -14, -15 PASS. **SQ-FDB-04 FAIL:**
  `tools/gen_comfort_nourishment.py` has no rule for `farmersdelight:gleaming_salad`, so the Comfort / Nourishment pass strips
  its Nourishment (same class as Rustic, TODO `creativedelight/members #43`); fix: add the rule, `/dumpall`, `/cdevgen`. Rerun with
  fixed probes: -05 (pie pick: Delightful / Central Kitchen pies absent, live toml reload unconfirmed), -06, -07 (MCA replaces the
  summoned villager), -09, -11. -08 needs a player, -12 is the client check.
- Onion Soup: **LEFT OUT (owner 2026-11-02)**; the copper onion soup leaves with the copper cups. Not added.
- Pick one pumpkin pie after comparing (SQ-FDB-05), then remove or merge the other slices.
- Gleaming Salad: cans, food-type tags after `/dumpall`.

### 4.6 Retextures adopted (owner, 2026-11-02)

Ten 1.3.4 item textures are copied into `kubejs/assets/farmersdelight/textures/item/` (the KubeJS pack sits above the mod jars; MIT
notice in `farmers_delight_art_notice.txt`): chicken soup, honey glazed ham and its block item, stuffed pumpkin and its block item,
pumpkin soup, vegetable soup, bone broth, fried rice, rice roll medley (block item). The other 45 retextures (blocks, canvas signs,
rice crops, cutting board, tray, shepherd's pie, stuffed pumpkin block faces) are not adopted. Client only, resource reload (F3+T) or restart;
check in game: the ten icons in the inventory and JEI, and any can or Display Delight plate that borrows them. Queue row CQ-FDB-01 (deep scan 2026-11-02).

