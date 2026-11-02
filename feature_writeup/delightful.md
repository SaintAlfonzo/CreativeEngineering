# Delightful 3.8.1 against the installed 3.4.1

**Built 2026-10-25: the value changes, the knife loot, the jam switch-off and the new items, as the Creative Delight
member `repackedDelightful` (3). Shipped in 1.1.03 (deployed to server and players 2026-10-26) with the Creative Delight jar (installed 2026-10-26); DF-01 to DF-11, SQ-DF not
run.** The 3.4.1 jar left `mods/` with that install. Evaluated 2026-10-25 against
`devmods/checkNewerVersionForContent/Delightful-1.20.1-3.8.1.jar` (1.20.1 / Forge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #23` in [todo/TODO.md](todo/TODO.md). Config: `config/delightful-common.toml`.

**Licence:** MIT in both jars. Code and art from 3.8.1 can be reused with the notice kept.

**Result in one line:** about 37 new foods and 30 new knives, but most need mods the pack does not have. About 18 items
would be enabled here, and 25 existing foods have new values or effects.

## 1. How it was read

- `python3 tools/jar_diff.py` (1685 files added, 285 removed, 576 changed; en_us lang **+147 / -34 / ~4**).
- Classes decompiled with Vineflower and diffed. Delightful registers every item, then turns it on or off at runtime
  (`IConfigured.enabled()`: config, required mods, conflicting mods, a tag that must be filled or empty). Each new item
  was checked that way against the pack's mod list.
- Food values from `Nutrition` compared old against new. Effect names come from SRG field names, checked against the
  1.19.2 `client_mappings.txt` (for example `f_19600_` Strength, `f_19601_` Instant Health, `f_19591_` Slow Falling).

## 2. Changes and decisions

Owner decisions of 2026-10-25 are marked **Decided**. Status per row: Built / Waiting (needs a delivery decision, 3).

### 2.1 New items brought in

**Decided:** all of these come in. **Built 2026-10-25** in the repack's overlay (3), recipes in Creative Delight's
`delightful/recipes.js` (3.2), except the Mulberry Pie (dropped, below). Cantaloupe and Hami melon (Mini Melon) will be
merged eventually, so the cantaloupe line should not grow more than the rows below. **Merged 2026-10-28**
([item_merges.md](item_merges.md) 2.10): this Cantaloupe is the Mini Cantaloupe (seeds: Mini Cantaloupe Seeds), the
slice stays, the popsicle is Fruits Delight's, the wild patch grows in the hamimelon biomes.

| Item | Needs | Note |
|---|---|---|
| Cantaloupe Bread, Cantaloupe Gummy, Cantaloupe Popsicle, Wrapped Cantaloupe, Bowl of Stuffed Cantaloupe (+ block) | cantaloupe (on here) | Cantaloupe Seeds and a Cantaloupe Plant crop that grows on sand are new too. All the cantaloupe foods |
| Cactus Chili, Cactus Soup, Cactus Soup Cup | none (Chili also takes corn); the cup needs Miner's Delight's Copper Cup, which the port has (2.2) | cactus dishes |
| Baklava (block) + slice | nuts, honey, dough | citrus optional (two recipes) |
| Matcha Milkshake, Salmonberry Milkshake (+ milkshake cauldrons) | ice cream | cauldrons too |
| Matcha Gummy (Glow Jam Cookie is not wanted, 2.3) | matcha | 3.8.1 has no matcha cookie (its new cookies are Glow Jam and Source Berry, both off), so only the gummy |
| Roasted Acorn, Nut Dough | acorn, nut butter | acorn and nut dough |
| Mulberry Pie (block) + slice | Upgrade Aquatic (installed) | **Dropped 2026-10-25:** in 3.8.1 the pie item is Upgrade Aquatic's `mulberry_pie` and Delightful adds only its placed block and slice. The pack already has both from Create Central Kitchen (`create_central_kitchen:mulberry_pie` placed, `mulberry_pie_slice`), with the oven recipe and the Canned Mulberry Pie Filling; a second set would define the item twice |
| Long Prickly Pear Juice | Farmer's Respite + `forge:fruits/prickly_pear` | **Dropped** with Prickly Pear Juice (2.6): no prickly pear source |

### 2.2 New items that stay off in this pack

| Items | Off because |
|---|---|
| Roe Blini, Salmon and Roe Blini, Roe Roll, Aged Roe, Caviar, Sturgeon Roe | need Let Fish Love (and Tide) |
| Source Berry cookie, gummy, ice cream, milkshake, cookie tiles | need Ars Nouveau |
| Chorus Muffin, Chorus Pie | need Unusual End |
| Mutton Pie | needs Windswept |
| Passion Fruit Tart, Yucca and other fruit crates | need Atmospheric or 1.20 mods |
| Venison Stew (+ cup) | needs Environmental / Naturalist / Twilight venison; venison is a removed item here |
| Sinigang Cup, Venison Stew Cup | Sinigang Cup: could come on with the Copper Cup (the port has it, 2.6); owner to say. Venison Stew Cup stays off with venison |
| Glow Jam Cookie, Glow Jam Cookie Tiles | Glow Jam is gone with the Delightful jams (2.3); Cookielicious tiles not installed |
| Knives for Aether, Twilight, Undergarden and other mods | mods not installed |

**Knives. Decided:** add the **Certus Quartz and Fluix knives** (AE2 installed) and the **Aluminum Knife**
(`forge:ingots/aluminum`, Immersive Engineering installed), because no other mod in the pack adds them (checked in
the item dictionary 2026-10-25). **Built 2026-10-25:** Farmer's Delight `KnifeItem`s with 3.8.1's tiers (Aluminum 2 /
220 uses / 6.0 / 2.0 / 22; Certus Quartz = iron; Fluix = iron with 3x uses, 1.2x speed and damage), in `forge:tools/knives`.
The Fluix Knife always has at least Looting I. Recipes: ingot or crystal over a stick; Fluix by 1.19.2 smithing (Certus
Quartz Knife + Fluix Block; 1.20's smithing template is not in AE2 1.19.2). Tier tuning, if any, against
[gear_tuning.md](gear_tuning.md) through `tuneTools`.

### 2.3 Renames and removals

| Change | Decision |
|---|---|
| Jelly Bottle / Glow Jelly Bottle renamed Jam Jar / Glow Jam Jar; the plain Jam Jar turns itself off when Brewin' or Fruits Delight is loaded | **Built 2026-10-25:** Fruits Delight is installed, so `jelly_bottle` and `glow_jelly_bottle` are off in `delightful-common.toml` (copy in `configs_edited/`) and both are in `removed_items.js`. Nut Butter and Jam Sandwiches take `forge:jams`, so Fruits Delight's jams cover them. Input for `dishes/jams #1` (the recipes, cans and food types that name the two bottles are still to clean up) |
| Nut Butter and Jelly Sandwich renamed Nut Butter and Jam Sandwich (new id) | **Decided:** take the rename. **Id switch not done (2026-10-25), owner to confirm:** the old id is wired into the food dict (`sandwich_square_nut_butter_and_jelly`), the generated food types and placeables, a Brewin' coaster model and the Display Delight plate (`df_plated_nut_butter_and_jelly_sandwich`, its `plate_displayable` tag). The item already shows as "Nut Butter and Jam Sandwich" in game, so the id switch changes nothing a player sees; registering the new id beside the old one would define the item twice. If wanted: register `nut_butter_and_jam_sandwich` in the overlay, make the old one a removed item, move every reference above, rerun the generators |
| Berry Matcha Latte -> "Iced Berry Matcha Latte", Nut Butter Bottle -> "Nut Butter", Rock Candy -> "Rose Rock Candy" | **Built 2026-10-25** (`client_scripts/namechanges.js`). Note: the pack's kettle Berry Matcha Latte has no ice (only the grid recipe takes ice cubes, recipes_cooking.md 2.2), so "Iced" fits only that one |
| Pumpkin Pie and slice removed (FD 1.3 has one) | skipped (the pack's placeable pumpkin pie is this one). **Texture to review:** the placed Pumpkin Pie texture, see [unused_textures.md](unused_textures.md) / the texture review list |
| Crimson and Nightshade Berry pies and sacks, Joshua Fruit Crate, Chunk Nugget, 6 knives removed | skip (compat items, none on here) |

### 2.4 Food values on existing items

**Decided:** carry over every hunger / saturation change (**Built 2026-10-25**, Creative Delight startup script
`delightful/delightful_foods.js`, restart to apply). **Effects are part of the larger effects review** and are not
changed ([effects.md](effects.md)): the effect columns below are the 3.8.1 values for that review, including the
Comfort -> Nourishment rows, which stay Comfort for now (FD decision, [farmers_delight.md](farmers_delight.md) 2.5).

Hunger / saturation (built), then effects (for the review).

| Food | 3.4.1 | 3.8.1 |
|---|---|---|
| Cheeseburger | 12 / 0.8, Strength 30 s | 11 / 0.85, Nourishment 60 s, Strength 20 s |
| Deluxe Cheeseburger | 13 / 0.9, Strength 45 s, Nourishment 180 s | 13 / 1.0, Nourishment 240 s, Strength 30 s |
| Sinigang | 15 / 0.9, Comfort 300 s, Fire Resistance 30 s | 12 / 0.7, Nourishment 180 s, Fire Resistance 30 s |
| Coconut Curry | Comfort 300 s | Nourishment 180 s (effect only) |
| Lavender Tea | Comfort 60 s, Caffeinated 10 s | Nourishment 30 s, Caffeinated 30 s (effect only) |
| S'more | 9 / 0.5, Nourishment 180 s | 8 / 0.4, Nourishment 120 s, Sugar Rush II 30 s (Neapolitan), Slow Falling 10 s |
| Marshmallow Stick; Cooked Marshmallow Stick | 1 / 0.3; 2 / 0.4 | 3 / 0.35; 5 / 0.35, Sugar Rush 30 s, Slow Falling 15 s |
| Crab Rangoon | 9 / 0.4 | 10 / 0.5, Resistance II 20 s (Corrosion with Collector's Reap, not installed) |
| Field Salad | 10 / 0.55, Regeneration 5 s | 10 / 0.6, Regeneration 10 s |
| Matcha / Salmonberry Ice Cream | 6 / 0.5 or 0.4, Slowness III 5 s | 6 / 0.3, no effect |
| Salmonberry Gummy | Water Breathing 20 s | 45 s (effect only) |
| Salmonberry Pie slice | Speed 30 s, Water Breathing 30 s | Water Breathing 30 s (effect only) |
| Rock Candy | 4 / 0.2, Speed 6 s | 4 / 0.5 |
| Honey Glazed Walnut; Nut Butter | 6 / 0.5; 5 / 0.3 | 6 / 1.0; 6 / 0.6 |
| Acorn | 2 / 0.1 | 2 / 0.1, Hunger 10 s (effect only) |
| Matcha Latte, Berry Matcha Latte | Respite green tea effect II 60 s, Caffeinated 60 s | Instant Health, green tea effect II 20 s, Caffeinated II 180 s (effect only) |
| Ender Nectar | 4 / 0.1, Blindness 3 s | 4 / 0.0, Instant Health, Caffeinated III 180 s, Tipsy 120 s, Intoxication 90 s, Blindness 3 s |
| Azalea Tea | Nourishment 60 s, Caffeinated 10 s | Blindness 3 s, Levitation II 3 s, Respite yellow tea effect II 240 s (effect only; no recipe here, Ecologics missing) |
| Prickly Pear Juice | Saturation 20 s | removed 2026-10-25 (2.6) |
| Chunkwich; Green Tea Leaf; Chopped Clover | 11 / 0.3; 1 / 0.2; 2 / 0.3 | 11 / 0.4; 1 / 0.1; 1 / 0.35 (read from the class constants of both jars) |

The Cantaloupe Slice keeps the pack's pumpkin-slice values (`food_values.js`), not Delightful's. After a restart rerun
the food catalog and the can generators (`/cegen`) so the new values reach the catalogs and the can parity.

### 2.5 Loot, worldgen, other

| Change | Decision |
|---|---|
| Knife on a Cantaloupe or Mini Melon block gives 4 slices | **Built 2026-10-25:** Creative Delight `delightful/loot.js` (LootJS; a Cantaloupe gives 4 Cantaloupe Slices, a Mini Melon 4 Melon Slices, no Silk Touch). Needs the CD jar installed |
| Goats killed while burning drop Cooked Goat | **Skip.** Goat meat is a removed item here (`raw_goat`, `cooked_goat` off in `delightful-common.toml`, in `removed_items.js`); vanilla goats drop no meat. Goats should not drop meat |
| Animal fat and ham scavenging with a knife reworked | owner's call; compare with [loot.md](loot.md) |
| Acorns: rare drop from any leaves (1%) | skip unless acorns are scarce |
| Worldgen patches as datapack JSON | skip: same three plants (salmonberries, mini melon, cantaloupe) |
| Jade plugin, 1.20 tags (`forge:fruits`, `forge:nuts`, many more), Let Fish Love food tags | skip (Jade not used, 1.20 format) |

### 2.6 Questions answered 2026-10-25

- **Prickly pear source:** none in the pack. The tag `forge:fruits/prickly_pear` holds only `ecologics:prickly_pear`
  (optional), and Ecologics is not installed; Delightful's Prickly Pear Juice recipe needs the Ecologics cooked-pear
  tag, so the juice cannot be made in survival today. The Long Prickly Pear Juice needs the same tag. **Decided 2026-10-25:** Prickly Pear Juice is removed
  (`prickly_pear_juice = false` in `delightful-common.toml`, in `removed_items.js`); the Long Prickly Pear Juice is not added.
- **Miner's Delight Copper Cup:** it was **not** removed. The port dropped only the **Hot Cocoa Cup** (in upstream 1.1.2
  but not the 1.4.5 donor; licence is all rights reserved and the donor jar has no art for it,
  [miners_delight.md](miners_delight.md) D2/D3). The earlier line saying the cup items needed a cup the port dropped
  was wrong. The empty Copper Cup is registered with a stack size of 16 (filled water, milk and powder snow cups 1,
  soup cups 16), from the port's code in `MDItems.java`; no pack stack-size row overrides it.
  **Decided 2026-10-25:** the empty, water, milk and powder snow cups stack to 64 (**built**, `backportedMiners`
  installed; soup cups stay 16). Placing a powder snow cup or emptying a water cup now gives back one empty cup
  and keeps the rest of the stack. The craft: Copper Cup was never removed. The upstream recipe (3 copper ingots,
  `X X` over ` X `) is read from the donor jar and nothing in the pack removes it; it was only missing from the
  earlier notes. No change needed.
- **Delivery. Decided 2026-10-25:** a small in-tree mod (3).
- **Goats:** see 2.5; no meat, by design.

## 3. Delivery (built 2026-10-25: a Creative Delight member)

Everything in 2.1 and the 2.2 knives is Java-side: placed feast and pie blocks with slices, milkshake cauldrons, a
sand crop and `KnifeItem` tools. KubeJS alone cannot register those. **Decided 2026-10-25: a small in-tree mod**,
bundled in Creative Delight. **Built 2026-10-25** as the Farmer's Delight member was (creative_delight.md 21, 26): the
upstream 3.4.1 jar (MIT) repacked unchanged, plus an overlay that registers the 3.8.1 items in Delightful's own
namespace, so the ids are 3.8.1's (`delightful:cantaloupe_seeds`, ...). A separate mod id would have given the items
another namespace and still needed 3.4.1 beside it; the repack replaces the 3.4.1 jar instead.

### 3.1 The member

| Piece | Where |
|---|---|
| Project `repackedDelightful` | `development/creativeDelight/members/delightful`, root `settings.gradle`, sixth in CD's `bundle_members`; `Delightful-*.jar` and `repackedDelightful-*.jar` in CD's `replaces_jars` |
| Upstream | `development/creativeDelight/upstream/Delightful-1.19.2-3.4.1.jar` (sha1 `531b4031...`, git-ignored); version `3.4.1-ce-19.2-01` (newer than `3.4.1` with the loader's comparator; the only dependent in `mods/`, More Mooblooms, takes `*`) |
| Donor | `upstream/donors/Delightful-1.20.1-3.8.1.jar` (sha1 `4fff8803...`), record only (`donor_jar` / `donor_sha1`) |
| Hook | `DelightfulInitMixin` (end of Delightful's constructor) -> `DelightfulBackports.init()`: the registers below on Delightful's mod bus, and at common setup the milkshake cauldron interactions when Neapolitan is loaded. Mixin config `delightful.ce.mixins.json` (`mixin_configs`; the upstream manifest has none) |
| Registry | `DelightfulBackportRegistry`: blocks `cantaloupe_plant`, `stuffed_cantaloupe_block` (FD `FeastBlock`, 4 servings, no leftovers), `baklava` (`BaklavaBlock`, a flat FD `PieBlock`), `matcha_milkshake_cauldron`, `salmonberry_milkshake_cauldron`; 20 items. All in Farmer's Delight's creative tab, like 3.4.1's. Nothing reads `delightful-common.toml`: every backported item is always on |
| Foods | `DelightfulBackportFoods`: 3.8.1's `Nutrition` values. Green tea effect = Farmer's Respite Vitality, caffeinated = FR Caffeinated (3.8.1's own lookups) |
| Classes | `CantaloupePlantBlock` (sand next to water, light 8, ages 0 to 3; when grown, using it picks a Cantaloupe and resets it; bone meal), `MilkshakeCauldronBlock`, `ShakeItem` (2 s drink), `GummyItem` (14 ticks), `CupItem` (gives back Miner's Copper Cup, looked up at use), `FluixKnifeItem`, `DelightfulBackportTiers` |
| Neapolitan compartment | `DelightfulMilkshakes`: Blueprint cauldron maps and `NeapolitanCauldronInteractions.addMilkshakeInteractions` (milk cauldron + ice cream -> milkshake cauldron; bottles fill and empty it). Only class that names Neapolitan or Blueprint, loaded only when Neapolitan is |
| Resources | `tools/port_resources.py` copies from the donor: blockstates, models, textures, block loot (1.20's `random_sequence` dropped), the names (3.4.1's `en_us.json` plus the new keys, `replaced_resources` sha1-pinned), and `data/delightful/cup_conversion/cactus_soup.json` (Miner's Copper Pot serves Cactus Soup as the cup). 3.4.1's own art is kept where both jars have a file: 3.8.1 redrew the cantaloupe block textures, and the Stuffed Cantaloupe uses 3.4.1's. Pie models take Farmer's Delight's `template_pie*` from the FD member's overlay |
| Notice | `NOTICE-upstream-MIT.txt` (brnbrd, both jars) |

Ids that collide with 3.4.1: none (checked against 3.4.1's registry classes and the live item list).

### 3.2 Pack side

- Ids: `item_dict.js`, after the Rustic ids (only CD's scripts use them).
- Recipes: Creative Delight `kubejs/data/creativedelight/kubejs/delightful/recipes.js` (NCH, ids `creativedelight:<type>/<name>`).
  Both scripts return early unless the repack is the loaded Delightful (`Item.exists(i.cantaloupe_seeds)`), so a stale
  3.4.1 jar beside CD cannot break them.

| Item | Recipe here | Note against 3.8.1 |
|---|---|---|
| Cantaloupe Seeds | 1 Cantaloupe Slice | same |
| Cantaloupe Bread | dough, Cantaloupe Slice, sugar | same |
| Wrapped Cantaloupe | Cantaloupe Slice, Cooked Bacon | same |
| Cantaloupe Popsicle | ` cc / icc / si ` or `im / si` (Cantaloupe) | the ice cube versions only (the pack has ice cubes) |
| Stuffed Cantaloupe | pot: rice, cooked mutton, onion, berries, seeds; container Cantaloupe; 400 ticks | same |
| Cantaloupe / Matcha Gummy | pot: fruit or Matcha, sugar, honey bottle, kelp; heated mixer with a honey block: 9 | same |
| Cactus Chili | pot: raw pork, cactus flesh, tomato sauce, hot spice, corn; bowl | the corn version (the pack has corn) |
| Cactus Soup | pot: cactus flesh, rice, tomato, onion; bowl. The cup: Copper Pot, cup_conversion | same |
| Baklava | **oven, pie tin: pie crust, 2 cooked nuts, honey bottle, sugar or honey** | upstream's grid recipe is not added: Baklava is a pie here (oven.md 2.6 baking rule). Cutting 4 slices and 4 slices -> Baklava kept |
| Matcha / Salmonberry Milkshake | 3 bottles, the ice cream, milk -> 3 | same |
| Roasted Acorn | furnace, smoker, campfire | same |
| Nut Dough | nut butter, egg -> 3 | the nut flour recipe is not added (no nut flour in the pack) |
| Aluminum / Certus Quartz Knife | material over a stick | same |
| Fluix Knife | smithing: Certus Quartz Knife + Fluix Block | 1.19.2 smithing (no template) |
| Field Salad (3.4.1 item) | 2 salad ingredients or 2 flowers, a green, a root, a berry, a nut, bowl (2026-10-29, slot tags in `tags.js`; [brief](briefs/dishes_meals.md) meals #39, RC-160) | not a 3.8.1 change: the pack's revamp |

- Tags: `delightful/tags.js` adds what 3.4.1 lacks, with 3.8.1's contents limited to the pack: `forge:tools/knives` (the 3
  knives), `forge:seeds`, `forge:matcha`, `forge:cooked_nuts` (Roasted Acorn, Vintage Delight's Roasted Peanut),
  `forge:nut_butter` (Nut Butter, Vintage Delight's Nut Mash), `forge:crops/cactus` (Cactus Flesh), `delightful:hot_spices`
  (blaze powder and `#forge:chilipepper`, the ghost pepper; 3.8.1's `forge:vegetables/spicy` is empty here).
- WTHIT growth line: `delightful:cantaloupe_plant` in Creative Delight's `crop_growth.json`.
- Display Delight already has placed forms for Cactus Chili, Cactus Soup, both milkshakes and Stuffed Cantaloupe; they
  had no item to place until now.
- Not done: IE cloche and squeezer rows (no NCH cloche helper), compost values, the per-type cans and food catalog
  (after `/dumpall` and the generators), a whole-pie can for Baklava.

## 4. Tests

Live tests are queued as SQ-DF in [testing/scratch_queue.md](testing/scratch_queue.md). In game after the install:

| ID | Check | Pass |
|---|---|---|
| DF-01 | Boot with the CD jar holding the repack, `Delightful-1.19.2-3.4.1.jar` moved out | `latest.log`: `delightful` version `3.4.1-ce-19.2-01`, the mixin `delightful.ce.mixins.json` applied, no `Error occurred while handling event`; old world loads with its Delightful blocks |
| DF-02 | JEI: search `@delightful` | the 20 new items show with names and art; recipes as 3.2 |
| DF-03 | Plant Cantaloupe Seeds on sand next to water, bone meal to grown, use it | a Cantaloupe drops, the plant is back to age 0; on sand without water it cannot be placed |
| DF-04 | Break a grown and a young plant | grown: seeds + Cantaloupe; young: seeds |
| DF-05 | Cook Stuffed Cantaloupe, place it, use a bowl 4 times | 4 Bowls of Stuffed Cantaloupe, the block is gone after the 4th; picking it up whole drops the block |
| DF-06 | Bake Baklava in the oven, place it, eat and cut it | 4 slices, Haste 30 s without particles |
| DF-07 | Milk cauldron (Neapolitan) + Matcha Ice Cream, then 3 glass bottles | the cauldron turns into a Matcha Milkshake cauldron; 3 Matcha Milkshakes, empty cauldron |
| DF-08 | Copper Pot: Cactus Soup recipe | serves Cactus Soup Cups; eating one gives back a Copper Cup |
| DF-09 | Fluix Knife on a mob | tooltip "Always has at least Looting I"; kills drop as with Looting I |
| DF-10 | The 3 knives on a cutting board | they cut (in `forge:tools/knives`) |
| DF-11 | Names | Iced Berry Matcha Latte, Nut Butter, Rose Rock Candy |
