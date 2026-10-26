# devmods: evaluating newer jars for content the pack lacks

Written 2026-10-25. Versions below are read from jar file names, not opened, unless the row says evaluated.
Evaluated: Veggies 1.9.3, Vintage KOTS 0.3.1, Farmer's Delight 1.3.4, Rustic Delight 1.7.0, Fruits Delight 1.1.3, Farmer's Respite 2.1.2, Display Delight 1.7.0, Delightful 3.8.1, Corn Delight 1.2.11, More Delight 26.05.20, Storage Delight 26.10.05, Farmer's Structures 1.0.6, Ender's Delight 1.1.4, End's Delight 2.6.1 (2026-10-25).

## 1. Method (same for every jar)

1. **Baseline:** the version the pack runs. Installed third-party jar in `mods/`, or for our ports the upstream jar the port was
   made from (see the port's writeup, "Upstream, ported version").
2. **Unpack both** into `DecompiledMods/<jar-name>/` (jar name as folder; skip if already there).
3. **Inventory diff** by registry id and data path: items and blocks (from `lang/en_us.json` keys and `models/item`), recipes,
   tags, loot tables, worldgen (configured / placed / biome modifiers), entities, effects, sounds. `python3 tools/jar_diff.py OLD NEW`
   (zip listing, changed files, lang-key diff, `--show-json` for json diffs; no decompile) does most of it. Class changes:
   `javap -c -p` on the changed classes, diffed.
4. **Classify each difference:** new content, rename, removal, value change, code-only. Note which need Java (new block
   entities, effects, mixins) versus data-only.
5. **Compatibility class:** 1.20.1 Forge -> 1.19.2 is data-format tweaks (tag keys, recipe fields, `Material`, creative tab).
   1.21.1 NeoForge is further: recipe result / ingredient format, data components, registry and loot changes.
6. **Pack overlap:** check each new item against `item_dict.js`, `item_merges.md`, the overlap tables in the mod's writeup.
7. **Output per jar:** a section in the mod's existing writeup (model: [nethers_delight.md](nethers_delight.md) section 2) with
   decisions: skip, data-only carry-over, extend the port (`-NN` build), or port new code.
8. **Delivery path:** an installed third-party 1.19.2 jar cannot be swapped for a 1.20.1 build, so new content arrives by
   KubeJS / data / patcherMod, or by a new port. Ports get a new `-NN` build and a `mod_sides.md` check (mod id unchanged = no edit).

## 2. Where each jar belongs

### checkNewerVersionForContent (installed 1.19.2 jar, or our port's source, has a newer upstream)

| New jar | Baseline in pack | Gap / note |
|---|---|---|
| FarmersDelight-1.20.1-1.3.4 | FarmersDelight-1.19.2-1.2.4 | **evaluated 2026-10-25**, [farmers_delight.md](farmers_delight.md), `mod-additions #18`: mostly 1.20 porting; Comfort kept on the 10 soups (decided); salvaging at 75%, Gleaming Salad, rope fence, onion and pumpkin loot, rich soil changes **built 2026-10-25** in the FD repack `-02` (Creative Delight member, not installed); Onion Soup open |
| rusticdelight-forge-1.20.1-1.7.0 | 1.19.2-1.3.0 | **evaluated 2026-10-25**, [rustic_delight.md](rustic_delight.md), `mod-additions #19`: about 100 new items (6 pepper colours, slices, rolls, medleys, pancake rework, syrup, coffee and fried foods), no id removed; ARR, so new items need a donor-jar backport or own art; small recipe carry-overs proposed |
| fruitsdelight-1.1.3 | 0.5.11 | **evaluated 2026-10-25**, [fruits_delight.md](fruits_delight.md), `mod-additions #20`: a 1.20 port, no new content; placed Mangosteen Cake effect bug to fix, Fig Pudding any egg, `forge:jams` and crate tags to carry over |
| displaydelight-1.7.0 | 1.2.0 | **ported 2026-10-25** (`development/backportedDisplay`, installed, not deployed), [display_delight.md](display_delight.md), `mod-additions #22`: 101 new display blocks, 95 for foods the pack has; links by block name; axe hides plates, take-all, silk-touch items; no trader sales; blocks for 8 missing mods left out |
| Delightful-1.20.1-3.8.1 | 3.4.1 | **evaluated 2026-10-25**, [delightful.md](delightful.md), `mod-additions #23`: about 18 new items would be on here (cantaloupe and cactus lines, baklava, milkshakes, mulberry pie), the rest need absent mods; 25 food value changes; jam jar turns off beside Fruits Delight; MIT |
| farmersrespite-1.20.1-2.1.2 | 2.0 | **evaluated 2026-10-25**, [farmers_respite.md](farmers_respite.md), `mod-additions #21`: no new item; fluid Kettle rewrite (not ported as is, parts go to `food/drinks #7`); potted Wild Tea Bush fix and death-message lang fix proposed |
| corn_delight-1.2.11-1.20.1 | 1.0.3 | **evaluated 2026-10-25**, [corn_delight.md](corn_delight.md), `mod-additions #24`: Corncob (replaces straw from corn), Corn Popsicle, Cob Pipe (head / Curios cosmetic); village corn farms and farmer trades (code); straw from knife-harvested corn; Comfort -> Nourishment on 3 foods (kept Comfort); MIT |
| moredelight-26.05.20-1.20 | 25.07.28 (1.19) | **evaluated 2026-10-25**, [more_delight.md](more_delight.md), `mod-additions #25`: no new item; rewrite onto DelightLib; knives move to vanilla Wood / Stone tiers (installed ones are overtuned), toasts eat fast, Carrot Soup Comfort -> Nourishment (skip); MIT |
| storagedelight-26.10.05-26.3 (NeoForge) | 25.12.09 (1.19) | **evaluated 2026-10-25**, [storage_delight.md](storage_delight.md), `mod-additions #26`: 94 new blocks, no new block type or behaviour; 64 fit the pack (Blackstone / Diorite / Stone Bricks / Tuff countertops on the 9 woods, bamboo on Quark, cherry on Vinery's dark cherry), pale oak and poplar have no wood; needs Java (rebuilt jar or patcherMod); MIT |
| abnormals_delight-1.21.1-6.0.3 | 1.19.2-4.1.2 | **evaluated 2026-10-25**, [abnormals_delight.md](abnormals_delight.md), `mod-additions #26`: 9 new items (8 copper knives, Plum Cabinet) all need Caverns and Chasms or Environmental (not installed); food values unchanged; Boatload and Upgrade Aquatic salvaging to fold into the FD salvaging carry-over; Adzuki Curry craft and slice XP small carry-overs |
| FarmersStructures-1.0.6 (1.21.1 NeoForge) | 1.0.0 | **evaluated 2026-10-25**, [farmers_structures.md](farmers_structures.md), `mod-additions #27`: data only; cod pond for the carp pond (installed one spawns fishless), 5 new Overworld structures, cook house pools 33 -> 78 templates, 5 dimension-only structures skipped; 83 of 91 templates are 1.21 saves, so `port_structure_jars.py` needs an item / entity downgrade first; ARR |
| VeggiesDelight-1.20.1-1.9.3 and 1.21.1-1.9.3 | port `backportedVeggies` from 1.9.2 | **evaluated 2026-10-25**, [veggies_delight.md](veggies_delight.md) 2.7, `ports/veggiesport #3`: mostly FD 1.3 follow-ups; carry the carrot cake cut face; Comfort -> Nourishment on 9 foods waits for the FD survey |
| vintagedelight-kots-0.3.1 | port `backportedVintage` (Ribs 0.1.6 + KOTS 0.3.1 ids) | **evaluated 2026-10-25**, [vintage_delight.md](vintage_delight.md) 2.10, `ports/vintageport #18`: 3 blocks drop nothing in the port (KOTS has the tables); the rest purged or skipped |
| ends_delight-2.6.1 (1.20.1) | ends_delight-1.19.2-2.1 | **evaluated 2026-10-25**, [ends_delight.md](ends_delight.md) 3.1 and 3.3, `mod-additions #29`: no new item; dragon drops without a tool plus knife extras, smithed Dragon Tooth Knife, succulent worldgen, chorus crate storage tag; Dragon Tooth Knife repair fix and faster End knives; pot teas already live in 2.1; 13 retextures; MIT |
| endersdelight-1.1.4 (1.20.1) | endersdelight-1.19-1.2.2 | **evaluated 2026-10-25**, [ends_delight.md](ends_delight.md) 3.1 and 3.2, `mod-additions #28`: 4 End plants, 5 dishes, Crushed Void Pepper, End House; food values unchanged; wooden-bowl variants gone (pack removes them already); 12 retextures; **1.1.4 declares MIT** while installed 1.2.2 is ARR (route question for Creative Delight); still in `createiveDelight/` |
| oceanic_delight-1.0.4 (1.20.1) | 1.0.1 | currently in `createiveDelight/` |
| oceansdelight-neoforge-1.0.4 (1.21.1) | 1.0.2 | currently in `createiveDelight/` |
| nethersdelight-1.20.1-4.0 | NethersDelight-1.19-3.1 | already surveyed in [nethers_delight.md](nethers_delight.md); execution plan documented 2026-10-25 (section 4, not built); Fungus Colony found to be a behaviour change |

### createiveDelight (members of the Creative Delight merge, [creative_delight.md](creative_delight.md))

Merge members are our four ports plus the installed 1.19.2 End's / Ender's / Ocean's / Oceanic jars. The jars now in the
folder are mostly not those:

| Jar | Verdict |
|---|---|
| FungiDelight-1.21.1-1.3, minersdelight-1.21.1-1.4.5 | donor / reference for ports that are already built (Fungi from 1.20.1 1.3, Miner's from the 1.20.1-1.4.5-backport donor). Keep as reference; no new content to diff unless 1.21.1 differs from 1.20.1 |
| ends_delight, endersdelight, oceanic_delight, oceansdelight | newer upstream of the installed members: move to checkNewer |
| nethersdelight 4.0 | not a Creative Delight member (stays third-party): move to checkNewer |
| MyNethersDelight-1.20.1-1.10.4-backport.1 | decided 2026-10-25: not ported, not added. Reference only |
| unusual_delight-5.0 | not installed, no older build: this is [TODO "Investigate Unusual Delight"](todo/TODO.md), belongs in toBackport |

### toBackport (not installed; survey, then port decisions)

| Jar | Plan row |
|---|---|
| Roll Delight 1.3.1, barbeques delight 1.3.0, largemeals 1.3.0, carnivore 1.5.0, mineraldelight 2.9 | largecontent TODO row 12: overlap survey of the five first, then one port decision per mod |
| later_large_content_overlap/extradelight 2.6.6 and hearthandharvest 1.4.1 | TODO rows 10 and 11: examined against each other, so the subfolder is right |
| unusual_delight 5.0 (move here) | TODO row 8 |

## 3. Suggested order

1. Ports with a known small delta: Veggies 1.9.2 -> 1.9.3, Vintage vs KOTS 0.3.1 (cheapest, feed the next `-NN` builds).
2. Core: Farmer's Delight 1.2.4 -> 1.3.4 (built 2026-10-25, awaiting install and tests), since a change there ripples into every add-on and into `item_dict`.
3. Installed add-ons by size of gap: Rustic, Fruits and Farmer's Respite (done 2026-10-25), Display, then the rest of checkNewer.
4. toBackport survey (TODO rows 8, 10, 11, 12) after the above, because overlap depends on what the installed set gains.

## 3a. Results so far (2026-10-25)

Only the Farmer's Delight rows are built (2026-10-25, not installed); the others are not. "Proposed" means awaiting the owner's decision. Detail lives in the mod's writeup.

### Veggies Delight 1.9.2 -> 1.9.3 ([veggies_delight.md](veggies_delight.md) 2.7, `ports/veggiesport #3`)

| Change | Class | Decision |
|---|---|---|
| Carrot cake slices 1 to 6 show `carrot_cake_inner` on the cut face | data (`port_resources.py`) | **carry over**, BUILT 2026-10-25 (port `1.9.2-ce-19.2-03`, VD-41) |
| Nourishment instead of Comfort on 9 foods (sweet potato pancakes, coleslaw, broccoli soup, cauliflower soup, garlic chicken stew, turnip beef stew, cacciatore, mashed potatoes, potato noodles) | code (food effects) | **decided 2026-10-25: Comfort kept**, no change (FD survey, farmers_delight.md 2.5) |
| Own item icons for the 8 wild plants and the mature dandelion (1.21.1 build only) | assets | optional, owner's call |
| Sweet potato dough as a custom recipe class | code | skip (same inputs and output) |
| New `farmersdelight:feasts`, `pies`, `snacks`, `sweets` tags; pies and feasts to `mineable/axe` | data | skip (FD 1.2.4 has none of them) |
| Create milling `processing_time` rename | data | skip (pack's Create reads `processingTime`) |
| 24 crop stage models on `template_crop_cross`; lasagna pan on `block/tray` | assets | skip (FD 1.3 only) |
| Depot houses place 2 to 3 horses instead of a pool jigsaw | structure | skip |
| Requires Farmer's Delight `[1.3.1,)` | dependency | informs step 2 of the order above |

### Vintage Delight KOTS 0.3.1 vs the port ([vintage_delight.md](vintage_delight.md) 2.10, `ports/vintageport #18`)

| Change | Class | Decision |
|---|---|---|
| Magic Peanut, Smooth Salt Block, Vine Tile have no loot table in the port and drop nothing | data, **bug** | **carry over** the three self-drops, BUILT 2026-10-25 (port `-04`, VTD-54) |
| Cheese Wheel, Cheese Pizza, Meat Pizza have no loot table | data | open: owner decides slices (KOTS) or nothing (FD pies) |
| Gearo berry line, Dehydrated, Pickled Pitcher Pod | content | stays purged (S1, S3, S8) |
| `MixedSeedsItem` | code | empty class, never registered; section 2.3a stands |
| Chef hats as armor layers | code | skip |
| Wild plant cutting into 2 crops | data | skip (FD seed and dye rule kept) |
| `c:` tags, Diet tags, Botany Pots recipes, NeoForge biome modifiers | data | skip (format or mods not in the pack) |

### Farmer's Delight 1.2.4 -> 1.3.4 ([farmers_delight.md](farmers_delight.md), `mod-additions #18`)

**State: BUILT 2026-10-25, not installed, not run in game.** FD 1.2.4 is now a Creative Delight member (`repackedFarmers`,
repack `1.19.2-1.2.4-ce-19.2-02`, [creative_delight.md](creative_delight.md) 21); the 1.3.4 jar is the MIT donor in
`development/creativeDelight/upstream/donors/`. The Java of the backports lives in the repack overlay, recipes, tags and loot in the
CD script tree (`kubejs/data/creative_delight/kubejs/farmersdelight/`). Live checks SQ-CDJ-07 / -08 and SQ-FDB-01 to 12 are queued
([testing/scratch_queue.md](testing/scratch_queue.md)); they need the CD jar installed first (`creativedelight/framework #2`).

| Change | Class | Decision | State |
|---|---|---|---|
| Comfort gone from every FD food: 10 soups and stews give Nourishment for the same time; Chicken Soup and Fried Rice also drop to 12 / 0.8, 3600 (from 14 / 0.75, 6000) | code (food values) | **keep Comfort** (owner, writeup 2.5); Veggies' 9 foods stay Comfort | decided, nothing to build |
| Salvaging: wider furniture inputs per wood, chest boat and minecart splits, 75% plank | data | carry over via `NCH.fdCutting` at 75% | BUILT (27 old cutting recipes removed; per wood one axe recipe; 7 chest boats, 4 minecarts). Boatload furnace boats and the driftwood set (abnormals_delight.md 2.3) not in |
| Gleaming Salad feast | content | add | BUILT (Java feast block in the overlay, 4 servings, sparkle particle). Cans and food-type tags wait for `/dumpall` and the generators |
| Rope Fence and Gate | content | add | BUILT (overlay blocks, `RopeFenceConnectionMixin`, recipes on `#forge:rope`, `forge:fences` tags). Rope merge `materials #2` still open |
| Onion drops from zombies and illagers; pumpkin broken with a knife gives 4 slices | data (loot) | add | BUILT (`farmersdelight/loot.js`, LootJS, runs without CD) |
| Rich soil farmland not trampled | code | add | BUILT as a `FarmlandTrampleEvent` cancel (1.2.4 rich farmland inherits vanilla `fallOn`) |
| Farmer villagers work rich soil | code | add | BUILT (`VillagersTargetFarmlandMixin`, config `[villagers] farmlandBlocks`, default rich soil farmland and mulch); 1.20's tag has no 1.19.2 reader, not added |
| Vegetable Noodles take any mushroom; Dog Food takes any raw meat | data | carry over | BUILT: noodles `#forge:mushrooms`; dog food `#creativeengineering:food_type/raw/meat` (1.3.4's `#forge:raw_meat` holds only 2 Miner's items here). The pack's manual noodles craft still names the brown mushroom |
| Rabbit Stew takes any potato; Cabbage Rolls filling | data | added by owner | BUILT: stew `#forge:crops/potato`; filling tag gains raw meat, raw fish, vegetables, mushrooms and loses eggs |
| Placeable Pumpkin Pie + slice | content | first "skip (pack already has them)", then owner asked to compare | BUILT: third pie `farmersdelight:pumpkin_pie` with `placeAs` switch (FARMERSDELIGHT / DELIGHTFUL / CREATE_CENTRAL_KITCHEN / OFF). Pick one after SQ-FDB-05, then merge or remove the other slices |
| Feedback lines | lang | owner | BUILT: rice shallow water line, skillet underwater (mixin); the rest were key renames |
| Onion Soup | content | evaluate adding | **OPEN**; overlaps Creamy Onion Soup and the Onion Soup cup |
| Bamboo / cherry cabinets, hanging canvas signs, bamboo basket, torchflower, smithing template, tag generalisation, tool-action matching, worldgen as data, new tags, 55 retextured items | 1.20 / format | skip | the 55 retextures are a still-open row (writeup 2.4); preview page not kept |

### Rustic Delight 1.3.0 -> 1.7.0 ([rustic_delight.md](rustic_delight.md), `mod-additions #19`)

| Change | Class | Decision |
|---|---|---|
| Six more pepper colours (Pale and Dark crops), slices, roasted slices, rolls, 9 stuffed peppers, 3 medley feasts, Rice Roll Royale, giant pepper blocks and jungle patch | content (code: crops, feasts) | proposed: owner picks delivery A / B / C (writeup 3) |
| Pancakes: stacks hand out Pancake items, stack to 12; plain, Pumpkin, Coffee pancakes | code | only with a backport (B) |
| Syrup line, coffee cookie / cheesecake / pancakes, Pumpkin and Syrup Coffee, Fried Dough, Fried Dumplings, Calamari Soup | content | proposed: B or own art (C) |
| Fried Fish; cherry blossom foods | content | skip (Fish Tempura chain; no cherry blocks in 1.19.2) |
| Pasta any 3 peppers, Golden Coffee Beans from roasted, Coffee-Braised Beef any coffee, Calamari Roll any raw calamari | data (recipes) | proposed: **carry over** |
| Soup 3 peppers, Honey Coffee without sugar, bell pepper 2 / 0.2, golden beans 1.2, beignet 7, Cotton renames | data / food values | owner's call |
| Pot coffees at 3 beans, raw or roasted | data | skip (coffees move to the Kettle, `food/drinks #7`) |
| Soup Nourishment instead of Comfort | code | skip (Comfort kept, FD decision) |
| Chest loot swaps (coffee, cotton), Rustic crops in village farms | data | owner's call |
| Advancements, Thirst compat, biome tag rename, Respite kettle coffee | 1.20 / not installed | skip |

### Fruits Delight 0.5.11 -> 1.1.3 ([fruits_delight.md](fruits_delight.md), `mod-additions #20`)

| Change | Class | Decision |
|---|---|---|
| Placed Mangosteen Cake bites give no effects (duration and amplifier swapped in 0.5.11) | code, **bug** | proposed: **fix** with a patcherMod mixin |
| Fig Pudding takes any egg | data (recipe) | proposed: **carry over** |
| `forge:jams` gets the 22 Fruits Delight jams; 16 fruit crates in `forge:storage_blocks` | data (tags) | proposed: carry over (jams with `dishes/jams #1`); crates owner's call |
| Comfort -> Nourishment on Mangosteen Cake, Pear with Rock Sugar, Pineapple Fried Rice | code (food effects) | skip (Comfort kept, FD decision) |
| Lychee Cherry Tea on Cherry Leaves; Sniffer durian sapling; seasons unbreakable tag; 1.20 tags; l2harvester and Thirst hooks | 1.20 / not installed / no effect | skip |
| Worldgen, loot tables, other foods and effects | 1.20 format | unchanged in content |

### Farmer's Respite 2.0 -> 2.1.2 ([farmers_respite.md](farmers_respite.md), `mod-additions #21`)

| Change | Class | Decision |
|---|---|---|
| Kettle becomes a 1000 mB fluid tank: 30 drink fluids, pipes, Create filling / emptying, whole tank brews at once (4 bottles per brew, one honey bottle makes 4 strong teas) | code (block entity, recipe format) | proposed: **not ported as is** (replaces the 4 Kettle mixins, `NCH.frBrewing` and the drinks #7 / #8 design); owner decides which parts drinks #7 borrows |
| Wild Tea Bush cannot be potted in 2.0 (potted block registered, `addPlant` never called) | code, **bug** | proposed: **fix** in patcherMod (tea only; FR coffee purged) |
| Wither Roots death message typo; `.player` variant missing in 2.0 (raw key shows when the victim has a kill credit) | lang | proposed: **carry over** as a lang override |
| Coffee Cake Slice and Rose Hip Pie Slice lose Speed 20 s; Coffee Cake Slice Caffeinated 20 s -> 30 s | code (food values) | proposed: skip (owner's call) |
| Wild tea `#forge:is_swamp` at 1/25; coffee worldgen as data; damage type, creative tab, advancements | 1.20 / pack-controlled | skip |
| Recipes outside the Kettle, loot tables, other food values | - | unchanged |

### Display Delight 1.2.0 -> 1.7.0 ([display_delight.md](display_delight.md), `mod-additions #22`)

| Change | Class | Decision |
|---|---|---|
| 101 new display blocks for foods of installed mods, 95 link to a pack food (Abnormals 15, End's 24, Ender's 12, Respite 14, Miner's 12, Brewin' 8, Nether's 7, FD 3) | content (code: blocks) | **ported** 2026-10-25; `md_` maps to `miners_delight` |
| Links by block name and mod prefix instead of loot drop + tag | code | ported; placeable, name and model generators read it through `tools/display_delight_jar.py` |
| Axe hides the plate, plate puts it back, sneak empty hand takes all, silk touch drops the display item | code | ported; silk-touch items on (upstream default), owner's call |
| Wandering trader sells display items for missing mods | code (config) | not ported |
| Fire particles on spicy foods, steam on hot drinks | client | ported |
| 163 blocks for 8 mods not in the pack (list in display_delight.md 4); 3 such ids removed; unregistered showcase models | content | left out of the port |

### Delightful 3.4.1 -> 3.8.1 ([delightful.md](delightful.md), `mod-additions #23`)

| Change | Class | Decision |
|---|---|---|
| Cantaloupe line (bread, gummy, popsicle, wrapped, stuffed, seeds and sand crop), Cactus Chili and Soup, Baklava, Matcha and Salmonberry Milkshakes, Matcha Gummy, Roasted Acorn, Nut Dough, Mulberry Pie, Long Prickly Pear Juice | content | proposed: owner picks; KubeJS + MIT art or a port |
| Roe and caviar, Source Berry, chorus, mutton, passion fruit, venison stew, cups, most knives | content | skip (need mods not in the pack; cups need the dropped copper cup) |
| Certus Quartz, Fluix, Aluminum knives | content | owner's call (AE2 and aluminium are in the pack) |
| Jelly Bottles renamed Jam Jars; plain jam turns off beside Brewin' or Fruits Delight | rename / code | input for `dishes/jams #1` |
| 25 food value changes; Comfort -> Nourishment on Sinigang, Coconut Curry, Lavender Tea | code (food values) | Comfort rows skip (FD decision); rest owner's call |
| Knife on Cantaloupe / Mini Melon gives 4 slices | data (loot modifier) | proposed: carry over |
| Pumpkin pie removed (FD 1.3) | removal | skip: the pack's placeable pie is this one |

### Corn Delight 1.0.3 -> 1.2.11 ([corn_delight.md](corn_delight.md), `mod-additions #24`)

| Change | Class | Decision |
|---|---|---|
| Corncob: corn processing gives it in place of straw; paper, organic compost, fuel, IE ethanol | content (data + item) | proposed: **carry over** as a KubeJS item with MIT art on the pack's knife recipe; other routes owner's call |
| Corn Popsicle, Cob Pipe (worn on the head or in Curios) | content (pipe needs Java) | owner's call |
| Plains and taiga village corn farms (4 templates) | code (pool insert) + data | owner's call; patcherMod insert, PoolTuner weights, re-save templates at 1.19.2 DataVersion |
| Farmer buys corn, sells Cornbread and Tortillas; wandering trader corn kernels and Popcorn Box | code | farmer buy trades: carry over in `trades.js`; wanderer skip (pack sells kernels) |
| Straw from mature corn broken with a knife | data (loot modifier) | owner's call; default skip (pack: corn gives no straw) |
| Classic Corn Dog from Corn Dog + Tomato Sauce in the grid | data | proposed: carry over |
| Comfort -> Nourishment on Creamed Corn, Corn Soup, Creamy Corn Drink; wild corn 1/30 -> 1/70; Mekanism recipes; 1.20 tags | code / data | skip |

### More Delight 25.07.28 -> 26.05.20 ([more_delight.md](more_delight.md), `mod-additions #25`)

| Change | Class | Decision |
|---|---|---|
| Same 33 items, registered through DelightLib (new required library) | code-only | skip |
| Knives on vanilla Wood / Stone tiers, attack speed 2.0 (installed Stone Knife beats Flint, mines faster than Iron) | value change | proposed: carry over with `tuneTools` |
| Toast and its 5 toppings eat fast | value change | owner's call (needs a `fast` option in `food_values.js`) |
| Carrot Soup Comfort -> Nourishment | value change | skip (FD decision) |
| Create cutting moved to the right folder; bowl containers named; `forge:sliced_potato` tag rename | data | skip (Slice and Dice covers bread; same pot behaviour; merge uses the old tag) |
| Brewin' cheese recipes for Casualness-only items | data | skip; a Cheese Toast is the owner's call |

### Storage Delight 25.12.09 -> 26.10.05 ([storage_delight.md](storage_delight.md), `mod-additions #26`)

| Change | Class | Decision |
|---|---|---|
| Polished Blackstone, Polished Diorite, Stone Bricks countertops on the 9 current woods (27) | content (Java: blocks with block entities) | proposed: **carry over** |
| Polished Tuff countertops (9), recipe on Quark's polished tuff slab | content (Java) | owner's call |
| Bamboo set (14) on Quark bamboo; art already in the installed jar | content (Java) | proposed: carry over |
| Cherry set (14); vanilla pink art, the pack's cherry is Vinery's dark cherry | content (Java) | owner's call (skip or retexture) |
| Pale oak and poplar sets (30) | content | skip: no wood in the pack |
| Glass cabinet recipe tag `forge:` -> `c:glass_panes`; 1.21 folder renames; NeoForge fuel data map (same 300 ticks) | data | skip |

### Abnormals Delight 4.1.2 -> 6.0.3 ([abnormals_delight.md](abnormals_delight.md), `mod-additions #26`)

| Change | Class | Decision |
|---|---|---|
| 8 Copper Knives (weathering, waxable), Plum Cabinet; Cherry Cabinet dropped | content (code) | skip: need Caverns and Chasms / Environmental, not installed |
| Salvaging for Boatload furnace boats and the driftwood / river furniture and boats | data | proposed: **carry over** with the FD salvaging row (river wood is removed, skip it) |
| Adzuki Curry grid craft takes Dried Banana and Roasted Adzuki Beans | data | owner's call |
| Perch / Pike slice cooking XP 1.0 -> 0.35 (FD's own slices are 0.35) | value change | proposed: carry over |
| Food values | none | unchanged; the two drinks move to FD's drink item with the same 15 s effect |
| 1.21 formats, FD 1.3 tags, recipes for absent hosts, merged pickerelweed, 4 milk bottles per bucket | data | skip |

### Farmer's Structures 1.0.0 -> 1.0.6 ([farmers_structures.md](farmers_structures.md), `mod-additions #27`)

| Change | Class | Decision |
|---|---|---|
| Cod pond replaces the carp pond (same salt); the installed carp pond's fish need Aquaculture, so it spawns empty | structure (data) | proposed: **carry over** |
| Cocoa farm (jungle), fish stew hut (snowy), ratatouille cellar (swamp), squid tank (taiga), shepherd farm | structure (data) | owner's call; would join group `remote` |
| Afterdark, Deeper and Darker, Bumblezone, `ihfr_fo` and Undergarden farms | structure (data) | skip: host mods not installed |
| Cook house pools 33 -> 78 templates; `random_crates` gains moss and ore-placeholder rules | structure (data) | owner's call |
| 1.21 saves (83 of 91): 394 item stacks in lowercase `count` load empty; `attributes` / `variant` entity keys; 16 block ids absent | format | needed for any carry-over: downgrade pass and remaps in `port_structure_jars.py` |
| Set spacings, `liquid_settings`, `#c:` biome tags, unused `less_chest` processor | data | skip (pack rates come from groups; 1.20.6+ / Fabric fields) |

### Ender's Delight 1.2.2 -> 1.1.4 ([ends_delight.md](ends_delight.md) 3.1 and 3.2, `mod-additions #28`)

| Change | Class | Decision |
|---|---|---|
| Amberveils, Ethereal Saffron, Chorusflame, Void Pepper Sprout (wild End plants; the pepper spreads) | content (Java for Void Pepper and Chorusflame) | decided in 3.1: `wild_crops.json` rules, end stone / tilled end stone only, in-tree mod for the pepper |
| 5 Shulker Bowl dishes, Crushed Void Pepper (knife, Create milling) | content | decided in 3.1: through the dict, `global.NCH`, food tuning |
| End House | structure (data; type is jigsaw plus a surface height >= 50 check) | decided in 3.1: `type/end_constructed`; plain jigsaw plus a pack-side void check; ships after the plants (template places them) |
| Chorus Crate and End Stone Stove loot tables; mining tags | data | decided in 3.1: add self drops (they drop nothing today) |
| JEI info for the 3 knife drops; Shulker Bowl lang fix | data / lang | decided in 3.1 |
| 12 retextures (Stuffed Shulker bowl item now a Shulker Bowl) | art | proposed: carry over the Stuffed Shulker one; rest owner's call |
| Licence: 1.1.4 MIT, 1.2.2 ARR | licence | owner's call: possible MIT route for Creative Delight, confirm co-author status upstream first |
| Wooden-bowl variants removed; 13 food definitions unchanged; Uncanny Cookies recipe (kept); 1.20 damage types, empty mixin config | removal / code-only | skip |

### End's Delight 2.1 -> 2.6.1 ([ends_delight.md](ends_delight.md) 3.1 and 3.3, `mod-additions #29`)

| Change | Class | Decision |
|---|---|---|
| Dragon meat, leg and tooth drop with no tool; knife kill adds extra meat and tooth | data (loot modifiers) | decided in 3.1: adopt, tooltip and JEI page |
| 7 `nd_add_*` machete modifiers removed | data | decided in 3.1: keep them (2.1), machete review later |
| Dragon Tooth Knife smithed from the Egg Shell Knife | data | decided in 3.1: two-input 1.19.2 smithing via `global.NCH`, then `gear #12` |
| Chorus Succulent configured / placed feature and biome modifier (2.1's file is in an unread folder) | worldgen (data) | decided in 3.1: `wild_crops.json` rule; check whether 2.1 generates any (EN-10) |
| `forge:storage_blocks/chorus_fruit` on the chorus crate | data | decided in 3.1: tag both crates |
| Respite brewing of the 3 teas removed; the pot recipes were already in 2.1, unchanged | data | 3.1 stands (Respite keeps them); pricier pot recipes are a change to live recipes |
| Dragon Tooth Knife repair Bone Block -> Dragon Tooth; Dragon Tooth and Egg Shell knives faster, higher enchantability, Dragon Tooth harvest level 2 -> 3 | value change (Java tier) | proposed: carry the repair fix; tier values to `gear #12` |
| Dragon Breath and Chorus Soup no longer clears a debuff (tooltip still says it does) | behaviour | skip: keep 2.1 |
| 13 retextures (shulker meats, sausages, dragon legs, omelette, stir-fry, wine, 2 feast details) | art | owner's call (MIT) |
| Food values, stack sizes, remainders, config; stove on FD 1.3 base class; 1.20 model parent, damage type, tooltip key renames | none / code-only | skip |

### Tooling

- New `tools/jar_diff.py`: file listing, changed files, lang-key diff, `--show-json`. Method step 3 now points to it.
- For a large jar (FD: 784 changed files) `--show-json` is too noisy: compare recipes and loot tables semantically
  (drop `group`, `category`, `show_notification`, pool names; unwrap `forge:conditional`) and read food values from the
  `FoodValues` bytecode (`javap -c -p -constants`, effects via the lambda bodies).
- Enum-heavy classes (registrate mods like Fruits Delight) read better decompiled: `java -jar
  ~/.cache/ce-tools/vineflower-1.10.1.jar -only=<package path> OLD.jar out/` for both jars, normalise repackaged library
  names, then `diff -u -w`.

## 4. Notes

- Do not swap any jar in `mods/` while the game runs; none of these are meant to be installed as is.
- Each evaluated jar produces a bullet only if a player-visible change ships later; the evaluation itself gets none.
- Add a TODO row per checkNewer entry once its survey starts (one row, like `mod-additions #16`).
