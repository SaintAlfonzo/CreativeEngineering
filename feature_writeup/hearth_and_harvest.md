# Hearth and Harvest: overlap and integration report

Survey for TODO `largecontent/mod-additions #10`. Written 2026-10-30 as a survey; since then the watering can (4.0) and raspberries (4.1) are built in the CD member `backportedHearth` and shipped (1.1.05 can, 1.1.06 tiers and raspberries; deep scan 2026-11-02); the rest of the port is not built. Jar:
`devmods/toBackport/later_large_content_overlap/hearthandharvest-1.21.1-1.4.1.jar`, unpacked to
`DecompiledMods/hearthandharvest-1.21.1-1.4.1/`, decompiled to `DecompiledMods/hearthandharvest-1.21.1-1.4.1-src/`.
Licence MIT (GPL-compatible: a fork or port is allowed). NeoForge 1.21.1 only, so it needs a **port** to Forge
1.19.2 (skill `port`). Short name in this file: **H&H**. Test prefix: `HNH-`.

Scope of this pass (owner, 2026-10-30): **overlap with the installed mods and the integration process only.** The
item-by-item new-content detail (food values, recipes, balance) is left for the combined writeup with Extra Delight
(`mod-additions #11`), which overlaps H&H heavily; section 3 is a summary only. Section 2.6 holds the placeholders
for the H&H vs Extra Delight pass.

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks by name; upstream bugs are fixed.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 267 item ids (models), 171 blocks, 27 fluids, 6 effects (+ 1 hidden horse boost), 2 potions + Bad Omen brew, 2 enchantments, 1 entity (Crow), 543 recipes, 68 advancements, 2 structures, salt caves, 11 wild plants / flowers, 1 GLM | 1 | read from the jar |
| Overlap: same item in an installed mod | 2.1 | **104** ids |
| Overlap: same concept or block family, different form | 2.1 | **43** ids |
| Blocked: needs 1.20 vanilla or a missing mod (cherry / bamboo woods, Crabber's Delight palm) | 2.4 | **8** ids |
| New to the pack | 3 | **112** ids (summary only; full pass with Extra Delight) |
| System overlaps (salting, crow, nests, litters, milk, alcohol aging, keg, mulch, hoe enchants, worldgen) | 2.3 | 16 systems |
| Upstream issues | 3.4 | B1-B5 (no load errors: never installed) |
| Fold-in | 4 | 24 steps, port first |
| Decisions | 5 | D1-D14 |
| Watering can (six tiers) | 4.0 | shipped 1.1.05 (base), 1.1.06 (tiers, well / cauldron, particles); scratch PASSED (SQ-WC r1031 / r1101, SQ-WCT-01 to -05 r1101, SQ-PSF-09 well drain r1102b); CQ-WC-04, -06, -07 open |
| Raspberries | 4.1 | shipped 1.1.06; scratch PASSED r1101 (SQ-HR-01 to -05) and CQ-HR-06 client PASSED; wild bushes in cold snowy biomes added 2026-11-02 (unreleased, SQ-PSD-07 r1102b raw FAIL, not judged) |
| Tests | 8 | HNH-01..HNH-24 drafted; HNH-25 to HNH-47 for 4.0 / 4.1 |

- **Jar:** `hearthandharvest` 1.4.1, author AlabasterLeking, "A homesteading addon for Farmer's Delight".
- **Licence:** MIT. Port route: in-tree mod under `development/` (like `backportedVinery`), our pack-side food code in
  a Creative Delight tree (memory `creative-delight-routing`).
- **Dependencies:** NeoForge 21.1+, Minecraft 1.21.1, Farmer's Delight 1.3.0+ (required). Optional compat by
  `mod_loaded` condition: Create (54 recipes), Brewin' and Chewin' (41), Botany Pots (14; Botany Pots 9.0.44 in `mods/` since 2026-10-30), Berry Good
  (4), Crabber's Delight (3, not installed). Code compat: Every Compat (wood variants), Thirst Was Taken (not
  installed), JEI.
- **Pack references today:** none in code. Docs: TODO row 10, `devmods_evaluation.md` row (subfolder), a cleaver
  mention in `mineral_delight.md`. Not to be confused with **Hearths** (`hearths-v1.0.0`, a Nether biome datapack,
  installed).
- **Already live:** nothing (never installed). No old worlds hold H&H ids, so anything dropped at port time needs no
  item merge (rule: no old-version conversions).
- **Dumps:** `kubejs/exported/items_by_mod/` is current (2026-10-30); `display_names.json` is from 2026-10-20 and lacks
  the in-tree mods' latest lang (the overlap search also read `development/*/src/**/lang/en_us.json`).

## 1. Overview

H&H is a homesteading content mod: berries, grapes, hops, corn, peanuts and cotton; a keg (fermenting, fluids), a cask
(aging food and drink through Aged / Fine / Reserve tiers, light-dependent), a stomping basin; jams and pickles in jars;
cheese, goat milk, butter, sausages, pies, pizzas; salt (salting any food, salt caves, lickable salt blocks for
animals); manure and mulch; farm tools (watering can, sprinkler, fertilizer bag, seed pouch, cleavers, pitchfork,
horseshoe, scarecrow, trough, nest); kitchen furniture (half cabinets, counter, drawer, sink, bottle racks); storage
crates and bags; mums (8 flowers); a tameable Crow; two structures (Corn Maze, Lilliput Lane).

### Facts

| Fact | Value |
|---|---|
| Mod id, version | `hearthandharvest` 1.4.1, NeoForge 1.21.1 |
| Items | 267 item models (block items included; the Wooden Bucket and Blueberry Bush have lang but no own model) |
| Blocks | 171 (lang), 160 blockstates |
| Fluids | 27: goat milk, cooking oil, brine, sap, syrup, 8 wines, mead, moonshine, root beer, beer, apple cider, hard cider, 8 juices |
| Effects | Pungent (mobs avoid you), Tempting (animals follow you), Drunk (worsens by level), Clarity (immune to Blindness and Darkness), Prickly (contact damage), Pinned (rooted, from the pitchfork); hidden Horse Boost (horseshoe) |
| Potions | Pungent (awkward + poisonous potato, long / strong), Tempting (Pungent + fermented spider eye), Bad Omen (crow feather) |
| Enchantments | Tilling, Harvesting (hoes, max III, data-driven 1.21 format) |
| Entity | Crow (spawns weight 60, groups 3-5, `#hearthandharvest:has_crows`); tameable, shoulder-riding, fetches items, steals shiny items into nests |
| Recipes (543) | 127 shaped, 99 shapeless, 63 `aging` (cask), 37 FD cutting, 28 FD cooking, 26 Create filling, 24 B&C keg pouring, 22 `fermenting` (keg), 20 stonecutting, 15 `stomping`, 15 B&C fermenting, 15 Create mixing, 14 Botany Pots, 12 Create compacting, 9 campfire, 8 smelting, 6 smoking, 1 `salting`, 1 `shapeless_remainder`, 1 smithing |
| Loot | 125 block tables, 9 chest tables (corn maze, 7 Lilliput Lane), crow drops; GLM: 2 % cherry from `minecraft:cherry_leaves` without shears |
| Worldgen | wild red / green grapes, wild cotton, wild peanuts, wild hops, blueberry and raspberry bushes, 28 two-colour mum patches, ground nests, salt caves; village farms get corn and peanuts (`VillageCrops`, 30 % of carrots / potatoes) |
| Structures | Corn Maze (custom structure type), Lilliput Lane (one template) |
| Tags | `c:` (NeoForge common), `farmersdelight:`, `minecraft:` (animal foods, crops, flowers, climbable), `sereneseasons:` (spring / summer / autumn crops), `create:`, `supplementaries:jar_cookies` |
| Advancements | 68; the pack drops all advancements |
| Mixins | horse eating corn / hops, water bottles stack (config), farmland counts mulch as water, sheep leave manure after eating grass, horse hurt; client: hide break progress, chicken-holding arms |
| Code-only behaviour | salted food (+25 % hunger, -10 % saturation), chicken plucking (sneak, hurts without shears), chicken gliding (held chicken slows falls), cow and goat bottle milking, goat milk bucket, pig and rabbit litters (1 in 3 breedings +1 baby), crow stash / shoulder, random armour on structure armour stands, farmer's hat XP on harvest, seed pouch auto-pickup and area planting, hoe enchant effects, villager trades (farmer buys H&H crops, wandering trader sells), cleaver butchering and throwing |
| Config | `Config.java`: crows, litters, plucking, bottle milking, water bottle stacking, salted bonus / penalty, village crops, trades; a per-item toggle map exists but is never read (B2) |

## 2. Overlap (first)

Searched: `item_dict.js`, `items_by_mod/*.json` (2026-10-30), `display_names.json` (2026-10-20), in-tree lang,
[item_merges.md](item_merges.md) (built and planned merges), and the overlap tables of
[vintage_delight.md](vintage_delight.md), [alcohol.md](alcohol.md), [fruits_delight.md](fruits_delight.md),
[corn_delight.md](corn_delight.md), [rustic_delight.md](rustic_delight.md), [kitchen.md](kitchen.md),
[nests.md](nests.md), [crops.md](crops.md). Search script: name and id match plus key-noun match, then curated by hand.

Because H&H was never installed, "Drop" below means **not registered by the port** (no merge, no old ids). The
port's recipes that used a dropped item point at the pack survivor instead.

### 2.1 Items

**Crops and produce**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Red Grapes, Green Grapes, Wild Red / Green Grapes | Vinery red and white grapes (6 biome kinds each), `#vinery:red_grape` / `#vinery:white_grape`, Vinery grapevine worldgen | same fruit, third grape system | Drop. H&H grape recipes take the Vinery tags (`i.t_red_grapes`, `i.t_white_grapes`). No H&H grape worldgen |
| Cherry | `vinery:cherry` (Vinery cherry trees) | same fruit | Drop; recipes take Vinery's cherry. The GLM targets vanilla cherry leaves (absent, 2.4) |
| Blueberries, Blueberry Bush | `fruitsdelight:blueberry`, its bush and worldgen | same fruit and bush | Drop; FD's blueberry everywhere. H&H Blueberry Pips: see D3 |
| Peanut, Wild Peanuts, peanut crop | `vintagedelight:peanut`, wild peanuts, crop | same crop | Drop; Vintage's |
| Cotton Seeds, Cotton, Wild Cotton, Cotton Plant | Rustic Delight cotton (renamed "Cotton", CD member) | same crop | Drop; Rustic's |
| Corn, Corn Kernels (seed), Corn Stalk | `corn_delight:corn`, `corn_delight:corn_seeds`, wild corn | same crop | Drop; Corn Delight's. H&H's Corn Husk is new (3) |
| Corn Meal | `creativeengineering:cornmeal` (CD `creativeengineering/cornmeal.js`) | same ingredient | Drop; pack cornmeal |
| Flour | `create:wheat_flour`, `#forge:flour/wheat` (`i.flour`) | same ingredient | Drop; tag |
| Salt | `vintagedelight:salt_dust` (the pack's one salt, merge 2.9) | fourth salt | Drop; salt dust / `#forge:salts` |
| Sap Bucket | `create_dd:sap_bucket` (D&D sap fluid) | same fluid name; D&D's sap is planned as rubber-log only (`engineering/dreams-and-desires #2`) | D4 |
| Syrup Bottle | `rusticdelight:syrup` (pot: apple or beetroot + sugar) | same topping, different source | D4 (one syrup item; H&H's tapper is a second source) |

**Drinks**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Beer, Mead | `brewinandchewin:beer`, `brewinandchewin:mead` | same drink | Drop; Brewin's |
| Hard Cider | `vinery:apple_cider` (alcoholic, in the 44 drinks of alcohol.md 11) | same drink | Drop |
| Cherry Wine | `vinery:cherry_wine` | same drink | Drop |
| Red / Green Grape Wine | `vinery:red_wine`, Vinery white wines | same drink | Drop |
| Red / Green Grape Juice | Vinery grape juice bottles (red / white, per biome) | same drink | Drop |
| Apple Cider fluid | `farmersdelight:apple_cider` (non-alcoholic), apple cider chain (item_merges 2.6) | fluid for an existing item | Map the fluid to the pack's apple cider fluid (Central Kitchen) |
| Chocolate Milk Bottle | Neapolitan Chocolate Milkshake, D&D chocolate milkshake | similar, not the same item | keep or drop: D5 |
| Goat Milk Bottle / Bucket | none (vanilla goats give cow milk) | new | 3; pack milk fluids rule (alcohol.md 4.1) applies |

**Jams, jars, pickles, spreads**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Jar (empty) | `vintagedelight:empty_mason_jar` (Supplementaries' jar is a container block, not a food jar) | third jar | Drop; mason jar. Plan `dishes/jams #1` already picks the mason jar |
| Apple, Blueberry, Glow Berry, Melon, Sweet Berry Jam | Fruits Delight jellies (`apple_jelly`, `blueberry_jelly`, `glowberry_jelly`, `melon_jelly`, `sweetberry_jelly`); Vintage sweet / glow berry jam jars | same jam | Drop. Plan `dishes/jams #1`: every jam is Fruits Delight's jam item in Vintage's jar block |
| Raspberry, Cherry, Grape Jam | none | new fruits for the jam family | Add through `dishes/jams #1` (FD-style jam item + mason jar), not as H&H jar blocks |
| Pickled Beetroots, Pickled Onions | `vintagedelight:pickled_beetroot`, `pickled_onion` | same food | Drop; Vintage's |
| Pickled Cabbage, Carrots, Potatoes | none (Vintage pickles 5 others) | new pickles | Add in Vintage's pickling jar (`dishes/pickles-and-ferments`), not the H&H keg |
| Peanut Butter | `vintagedelight:nut_mash_jar` / bottle, Delightful nut butter (merge planned, item_merges 5.1) | same spread | Drop; the nut mash survivor |
| Peanut Butter & Jelly | `vintagedelight:pb_j`, Delightful nut butter and jelly sandwich (planned one item) | same sandwich | Drop |
| Roasted Peanuts | `vintagedelight:roasted_peanut` | same food | Drop |

**Dairy, cooking ingredients**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Cheese Wheel, Unripe Cheese Wheel, Cheese Slice | Vintage cheese wheel / slice, Brewin' flaxen and scarlet cheese, `#forge:cheese` (`i.any_cheese`) | third cheese line | Drop; `#forge:cheese` in recipes. H&H's cask aging of Brewin' cheeses: see 2.3 |
| Goat Cheese line | none | new | 3 |
| Butter | none (Create Confectionery cocoa butter is unrelated) | new | 3; `#forge:butter` tag new to the pack |
| Cooking Oil | `rusticdelight:cooking_oil` | same ingredient | Drop |
| Batter | `rusticdelight:batter` | same ingredient | Drop |
| Jerky | `brewinandchewin:jerky` | same food | Drop |

**Baked goods, sweets, dishes**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Pizza / slice | `brewinandchewin:pizza` (to be renamed Supreme Pizza, item_merges 5.1) | same name and concept | Drop |
| Meat Pizza, Cheese Pizza (+ slices) | `vintagedelight:meat_pizza`, `cheese_pizza` | same food | Drop |
| Veggie Pizza / slice | `veggiesdelight:vegan_pizza` | similar | Drop (D6) |
| Blueberry Pie / slice | `delightful:blueberry_pie` | same food | Drop |
| Cherry Pie / slice | `create_central_kitchen:cherry_pie` | same food | Drop |
| Carrot Cake / slice | `veggiesdelight:carrot_cake` | same food | Drop |
| Chocolate Cake / slice | `createaddition:chocolate_cake` (survivor of merge 2.3), Abnormals Delight slice | same food | Drop |
| Blueberry Muffin | `fruitsdelight:blueberry_muffin` | same food | Drop |
| Marshmallow, Roasted Marshmallow | `delightful:marshmallow_stick`, `cooked_marshmallow_stick`; Create Confectionery marshmallow | same food | Drop; H&H's Charred Marshmallow and "hold near campfire" roasting: D7 |
| Smore | `delightful:smore` | same food | Drop |
| Caramel | `create_confectionery:caramel` | same ingredient | Drop |
| Caramel Apple | `create_factory:caramel_apple` | same food | Drop |
| Chocolate Bar | `neapolitan:chocolate_bar`, `create:bar_of_chocolate` | same food | Drop |
| Cotton Candy | `cloudstorage:cotton_candy` | same food | Drop (D6: Cloud Storage's is an odd source) |
| Maple Cookie | `abnormals_delight:maple_cookie` | same food | Drop |
| Mashed Potatoes | `moredelight:mashed_potatoes` (planned split, item_merges 5.1) | same food | Drop |
| Popcorn, Tortilla, Taco | Corn Delight | same food | Drop |
| Corn Bread | `corn_delight:cornbread` | same food | Drop |
| Cooked Corn on the Cob | `corn_delight:grilled_corn` / `boiled_corn` | same concept | Drop; Uncooked Corn on the Cob goes with it |
| Corn Stew | `corn_delight:corn_soup` | similar | Drop (D6) |
| Pancake (block) | Rustic Delight pancakes, Supplementaries pancake (rework planned, item_merges 5.2) | fourth pancake | Drop |
| Waffle (block) | `create_factory:waffle` | same food | Drop |
| Sugar Cubes | `supplementaries:sugar_cube` | same item | Drop |

**Tools, gear, farm blocks**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Crow Spawn Egg (Crow) | `alexsmobs:crow` (spawns in `flying_birds`, mob_spawns.md) | same animal; AM's crow is also tameable, rides the shoulder and gathers items | Drop the entity; D2 |
| Farmer's Hat | `wardrobe:farmers_hat_helmet`, `vinery:straw_hat` | same hat; H&H's grants XP on harvest | D8 |
| Wooden Bucket | `creativeengineering:wooden_bucket`, `myrtrees:wooden_bucket` | third wooden bucket | Drop |
| Seed Pouch | `quark:seed_pouch` | same name; H&H's also plants an area and auto-picks seeds | D8 |
| Sprinkler | `sliceanddice:sprinkler`, `create_things_and_misc:sprinkler` | S&D's is a Create machine; H&H's is passive (water + bone meal in an area) | D9 |
| Watering Can | TODO `farming/crops #8`: built ahead of the port in patcherMod under H&H's id (4.0) | the pack wants one | keep H&H's (Extra Delight has none); the port takes the id over (4.0) |
| Scarecrow | TODO `blocks/new #8` (planned dressable scarecrow) | the pack wants one | keep; H&H's also repels crows (none if D2 drops the crow) |
| Trough | `quark:feeding_trough` (pack retry tweak, mob_block_tweaks.md 2.3) | same job (auto-breed); H&H's also takes water (babies grow faster) | D9 |
| Nest | Incubation nests (`incubation:hay_nest`, Incubation Additions nests): chickens lay into them, nests.md | same job | Drop; crow stash goes with D2 |
| Animal Feed | `farmersdelight:horse_feed` (horses only) | H&H's breeds any animal | keep (3) |
| Sink (`basin`), Counter | Cooking for Blockheads sink and counter (kitchen.md), Handcrafted counters, Redeco counters | fourth counter family | D10 |
| Drawer | Handcrafted, Another Furniture, Storage Delight drawers | many drawers | D10 |
| Empty Crate | `quark:crate`, `immersiveengineering:crate` (name only) | H&H's is a bottle crate (BottleCrateRecipe) | D10 |
| Bottle Racks (9 vanilla woods) | Vinery wine racks (merged into one rack family, alcohol.md 10.11) | second rack family | Drop; Vinery racks |
| Trellis | Vinery grapevine lattice (kept, 10.10), Beautify trellises (decor) | H&H's trellis grows hops and crop vines | keep (hops need it), check against the Vinery lattice: D11 |
| Hay Rug, Straw Rug | FD canvas rug (form only) | new decor | keep |
| Mulch | Immersive Weathering mulch (renamed Fertile Soil in the pack, crops.md 2.4) | same name; H&H's keeps farmland wet | D11 |
| Keg | `brewinandchewin:keg` (Backported Brewin, heat / cold rules, alcohol.md 10.13, 10.16) | second keg | Drop; H&H's 22 keg recipes rebuilt as Brewin' keg recipes (alcohol.md rules) |
| Cask | Vinery Aging Barrel (`fermentation_barrel`), Brewin' Heating Cask | third aging block; H&H adds food and pickle aging tiers | D12 |
| Stomping Basin | Vinery Apple Press (`wine_press`), Create press / millstone | juice from fruit | D12 |

**Storage blocks**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Apple, Golden Apple, Golden Carrot Crates | Quark crates, Fruits Delight apple crate | same block | Drop |
| Blueberry, Peanut, Corn Crates; Corn Kernel Bag | Fruits Delight, Vintage, Corn Delight crates and bag | same block | Drop (crops dropped too) |
| Cherry, Red / Green Grape Crates | Vinery bags (crates removed in favour of bags, alcohol.md 10.10) | same block | Drop |
| Sweet Berry, Glow Berry Crates | Quark berry sacks, Berry Good baskets (item_merges 5.5: Berry Good basket dropped) | same block | Drop |
| Bag of Gunpowder, Bag of Cocoa Beans | Quark sacks | same block | Drop |
| Bag of Feathers | `supplementaries:feather_block` | same block | Drop |
| Bag of Flour | `create_compressed:wheat_flour_pile` | same block | Drop |
| Sugar Cane Bundle | `quark:sugar_cane_block` | same block | Drop |
| Stick Brush | Quark stick block | same block | Drop |
| Charcoal Block | `quark:charcoal_block`, `architects_palette:charcoal_block` | same block | Drop |
| Bag of Salt, Bag of Sugar | The Salt's Salt Block; Supplementaries sugar cube block | similar | Drop (D13) |
| Cotton Bale | Rustic Delight Cotton Crate | similar | Drop |
| Spool, Rope Coil | name clash only: `creativeengineering:spool` (component), Quark / Supplementaries / FD rope (rope plan item_merges 5.5) | string and rope storage blocks | D13 |

**Salt blocks**

| H&H item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Salt Block | `salt:salt_block` (the pack's salt block, melts / dissolves, item_merges 2.9) | same block | Drop |
| Salt Lamp | Large Salt Lamp (The Salt's, renamed), 17 Vintage salt lamps | same block | Drop |
| Polished salt, salt slab / stairs / wall (8) | Vintage smooth salt, salt bricks, mixed salt bricks (with stairs, slabs, walls) | a third salt decor set | D13 |
| Lightly / Well / Heavily Licked Salt Block (+ waxed, 7 more) | none: animals lick it, it wears down | new mechanic on a salt block | keep the mechanic on The Salt's block or drop: D13 |
| Salt Drip | none | new | follows D13 |

### 2.2 Mob drops, dishes, ingredients

- No mob loot is replaced. Crow drops (crow feather) are new only if the crow stays (D2).
- Same food under another id: covered in 2.1 (pizza, pie, cake, jam, pickle, peanut, cheese, jerky). Every one of them is
  a "Drop at port", not a merge.
- Ingredients that need a tag in place of a literal id when the port's recipes are rebuilt: grapes (Vinery tags),
  cherry, blueberry, peanut, cotton, corn, cornmeal, flour, salt, cheese, cooking oil, batter, jam (`#forge:jams`),
  milk (`#forge:milk`), nut mash.

### 2.3 Systems

| H&H system | Pack system | Overlap | Proposal |
|---|---|---|---|
| Salting (crafting: food + salt = Salted, +25 % hunger, -10 % saturation; tooltip "Salted") | The Salt's Salting craft (`#forge:salts`), Seasoning JEI page (patcherMod `SeasoningJEI`), item_merges 2.9 | **same mechanic** | Drop H&H's (`SaltingRecipe`, `SaltedEffectEvents`, `SaltedFoodTooltipHandler`) |
| Crow entity (spawn, tame, shoulder, fetch, steal shiny items into nests, scarecrow repels) | Alex's Mobs crow (spawn table, `flying_birds` light rule) | same animal | D2 (recommend drop) |
| Nests (chickens lay into them, 9 stacks, hoppers) | Incubation nests (nests.md) | same job | Drop |
| Pig / rabbit litters (1 in 3 breedings +1 baby) | Fungi Delight truffle litters (extra piglets, mob_block_tweaks.md, `tweaks #13` mob food overhaul) | pigs: overlap | D14 |
| Cow bottle milking (glass bottle on cow = FD milk bottle) | pack milk: Neapolitan -> FD milk bottle (merge 2.2), pack fluids (alcohol.md 4.1) | new route to an existing item | keep (cheap, uses the merge survivor), D14 |
| Goat milking, goat milk bucket "Clears All Effects" | vanilla goats give cow milk | new | keep with the goat cheese line (3) |
| Chicken plucking and gliding | none | new | keep (3) |
| Alcohol: Drunk effect, wines, Aged / Fine / Reserve tiers (less drunk, longer effects) | Brewin' Tipsy + Intoxication, Vinery wine aging, alcohol.md 11 (every drink: half a rotten shank, Intoxication, Nausea, "Intoxicating Drink" line) | **same system** | Drop Drunk; kept H&H drinks join `global.alcohol` (Intoxication, Nausea rule). Aging tiers: D12 |
| Keg fermenting (juice -> wine, milk -> cheese, water + salt -> brine, pickling) | Brewin' keg (port), Vintage pickling jar (`vintagedelight:fermenting`) | same machines | rebuild kept recipes on Brewin' keg / Vintage jar (step 9) |
| Cask aging (food and drink, light dependent) | Vinery aging barrel, wine racks with storage temperature (alcohol.md 10.13) | partly | D12 |
| Hoe enchants Tilling / Harvesting (area till / harvest) | Hoe harvest areas per hoe tier (patcherMod `crops/HoeHarvestArea`, Quark, memory `hoe-harvest-area`) | **Harvesting conflicts** with the per-hoe areas | Drop Harvesting; Tilling: D11 |
| Mulch counts as water for farmland (mixin); grass features place mulch | IW mulch = Fertile Soil (crops.md soil rules) | name and role | D11 |
| Sheep leave manure; manure fertiliser, manure bricks | compost framework ([composting.md](composting.md)), fertilisers (IE, S&D, Botania) | partly (fertiliser) | keep manure (3), route through compost (step 13) |
| Wild crops worldgen (grapes, cotton, peanuts, hops, berries) | `wild_crops.json`, `tools/gen_crop_worldgen_table.py` (crops.md 2.7, 2.8) | grapes, cotton, peanuts, blueberries already placed by the pack | Drop those features; keep wild hops and raspberry bush through `wild_crops.json` (step 14) |
| Seasons tags (spring / summer / autumn crops) | Serene Seasons, pack fertility by biome season types (memory `weather-by-biome`) | format | carry over for kept crops (hops, raspberry, sunflower?) via `global.tags` (step 3) |
| Mums (28 two-colour patches) | wild flowers system (`wild_flowers.json`, [flowers.md](flowers.md)), flower pots matrix | format | add the 8 mums as flowers through `wild_flowers.json`, not H&H features (step 14) |
| Salt caves | The Salt's rock salt ores, Vintage beach salt patch (gen_soil_compat) | another salt source | D13 |
| Structures (Corn Maze, Lilliput Lane) | structure framework (memory `structure-gen-framework`, groups, spacing) | format | through the framework (step 15), loot through [loot.md](loot.md) |
| Village farm crops (corn, peanuts in vanilla farms) | none in the pack (wild crops only) | new | drop (corn and peanuts are other mods' crops; D14) |
| Villager trades (farmer buys H&H crops; trader sells) | Dynamic Villager Trades, wandering trader catalog ([wandering_trader.md](wandering_trader.md)) | format | drop the code; any trades through the pack trade lists |
| Potions (Pungent, Tempting, Bad Omen) | potions system ([potions.md](potions.md), mixers, potion buckets, tipped arrows) | format | step 10 |
| Water bottles stack (mixin) | stack size policy ([stack_sizes.md](stack_sizes.md)) | policy | disable (config) unless the policy wants it |
| Advancement triggers (salted food, corn maze), structure discovery poll | pack drops all advancements | dead code | drop |

### 2.4 Blocked: 1.20 vanilla content or missing mods

| Items | Needs | Proposal |
|---|---|---|
| Cherry Half Cabinet, Cherry Bottle Rack | vanilla cherry wood (1.20) | Vinery's cherry wood is the pack's cherry: base the half cabinet on Vinery's `cherry_cabinet`; rack dropped with the rack family |
| Bamboo Half Cabinet, Bamboo Bottle Rack, Bamboo Trellis, Stripped Bamboo Trellis | vanilla bamboo block / planks (1.20) | drop (or Every Compat if a bamboo wood mod is present: none) |
| Palm Half Cabinet, Palm Bottle Rack | Crabber's Delight (not installed) | drop |
| Cherry leaf GLM | `minecraft:cherry_leaves` (1.20) | drop with the cherry item |

### 2.5 Overlap with mods in the same batch

- **Extra Delight** (`mod-additions #11`): overlaps H&H heavily (both have jams, pickles, cheese, butter, corn, peanut
  butter, pies, cask-like aging, watering can, salt, sausages, hot dogs, croissants, donuts). Placeholders in 2.6.
- **Barbeque's Delight**, **Carnivore** ([barbeques_delight.md](barbeques_delight.md), [carnivore.md](archive/carnivore.md)):
  sausages, skewers, hot dog, jerky. Check H&H's Raw / Cooked / Skewered Sausage and Hot Dog against those reports'
  survivors before adding (skewer family rule, item_merges 5.1).
- **Roll Delight**, **Large Meals** (not surveyed yet): check croissant, biscuits and gravy, chicken pot pie.
- **Mineral Delight** ([mineral_delight.md](archive/mineral_delight.md)): cleavers mentioned there; no item overlap.
- **Unusual Delight** ([unusual_delight.md](unusual_delight.md)): salt, sweeteners and jars themes; no direct id overlap
  found.

### 2.6 Placeholders for the H&H vs Extra Delight pass

**Settled 2026-10-30** in [extra_delight.md](extra_delight.md) 2.5 (one survivor per row) and 5.1 (D4, D7, D12). To settle in the combined writeup, one survivor per row (all marked "decide with Extra Delight" in section 5):
watering can, sprinkler and fertiliser tools; butter; sausages and hot dogs; croissant, scone, donut, muffin family;
goat milk and goat cheese; cask aging tiers vs any Extra Delight aging; raisins and trail mix; caramel corn, candy
corn, popcorn variants; mac and cheese; biscuits and gravy; chicken pot pie; pickled vegetables; jam fruits.

## 3. New content (summary; full pass with Extra Delight)

112 ids are new to the pack. By line:

- **Crops:** Raspberry (bush, pips, worldgen), Hops (wild hops, trellis-grown, bag), Sunflower Seeds (from sunflowers),
  Corn Husk and its bundle.
- **Drinks:** Blueberry, Raspberry, Cherry, Glow Berry, Sweet Berry Juice; Blueberry, Raspberry, Sweet Berry, Glow
  Berry, Melon Wine; Root Beer; Moonshine; Goat Milk (bottle, bucket); Brine (bottle, bucket).
- **Jams and pickles:** Raspberry, Cherry, Grape Jam; Pickled Cabbage, Carrots, Potatoes (all routed into existing
  families, 2.1).
- **Food:** Butter; Goat Cheese line; Raisins, Raisin Cookie; Trail Mix; Croissant; Raspberry Scone; Cider Donut;
  Baked Apple; Candy Corn; Peanut Butter Cookie; Raspberry, Grape, Peanut Butter, Chicken Pot Pie (+ slices); Raw /
  Cooked / Skewered Sausage, Hot Dog; Macaroni and Cheese; Biscuits and Gravy; Glazed Carrots; Hops Salad; Tamale;
  Street Corn.
- **Farm and tools:** Tree Tapper (and sap cauldron), Fertilizer Bag, Animal Feed, Horseshoe (+ horse boost), Flint /
  Iron / Golden / Diamond / Netherite Cleaver (butchering, throwable), Pitchfork (throwable, Pinned), Crow Feather
  (only with the crow), Jug (8-bucket placeable fluid container).
- **Blocks:** 11 half cabinets (vanilla woods), mushroom / fungus crates (4), Glistering Melon, Poisonous Potato,
  Rotten Tomato Crates, Bag of Manure, Hay / Straw Rug, 8 mums (+ potted), manure family (manure, wet / fired brick,
  block, bricks, polished, slabs, stairs, walls: 13).
- **Effects:** Pungent, Tempting, Clarity, Prickly, Pinned (Drunk dropped, 2.3). JEED text needed for each kept one.
- **Mechanics:** chicken plucking and gliding, goat milking, lickable salt (D13), farmer's hat XP (D8), seed pouch
  area planting (D8).

### 3.4 Upstream issues

| # | Issue | Evidence | Fix |
|---|---|---|---|
| B1 | Per-item enable toggles are defined (`Config.put`, `ITEMS`) but never read | `Config.java` 166-172; no caller outside `Config` | drop the map in the port |
| B2 | Item model `lemon_wine` (and `2d_lemon_wine`) with no item, lang or recipe | `assets/hearthandharvest/models/item/` | leftover; drop |
| B3 | Cherry GLM targets `minecraft:cherry_leaves`, which is absent on 1.19.2 | `loot_modifiers/cherries_from_cherry_leaves.json` | dropped with the cherry (2.4) |
| B4 | `DeprecationTooltipHandler` runs on every tooltip for an empty set | `DeprecationTooltipHandler.java` 21-26 | drop the class |
| B5 | Structure discovery polls every player every 40 ticks for an advancement trigger | `StructureDiscoveryEvents.java` | moot: the pack drops advancements (returns at once); drop the class |

No load errors: the jar was never loaded in the pack.

## 4. Fold-in, in build order

### 4.0 Watering can, ahead of the port (built 2026-10-30; moved into the member with tiers 2026-10-31)

Owner, 2026-10-30: the watering can only, now, through patcherMod; the rest waits for the full port. Settles TODO
`farming/crops #8` (Extra Delight has none, [extra_delight.md](extra_delight.md) 2.5).

- **Route:** MIT, so code and texture are carried over (not the ARR donor-jar route). The texture is copied unchanged
  from the H&H jar; notice in patcherMod `credit.txt` and `META-INF/licenses/hearthandharvest-MIT.txt`, and in the
  item class header.
- **Id:** `hearthandharvest:watering_can` (upstream id, registered from patcherMod under H&H's namespace like the
  Netherite Lunchbox under `solapplepie:`). Creative tab: Tools. `global.item` keys `watering_can`, `t_bonemeal_substitutes`.
- **Moved and tiered (owner, 2026-10-31):** the can now lives in the CD member `backportedHearth` (4.1), version
  `1.4.1-ce-19.2-02`; patcherMod's `wateringcan/`, `assets/hearthandharvest/`, the `credit.txt` line and
  `META-INF/licenses/hearthandharvest-MIT.txt` are gone (the member's `NOTICE-upstream-MIT.txt` lists the texture and
  strings). Code: `item/WateringCanItem`, `item/WateringCanTier`, registered in `item/HHItems`; JEI info page per tier in
  `compat/HHJeiPlugin` (JEI compileOnly). Recipe and fertiliser tag moved to CD `kubejs/data/creativedelight/kubejs/
  hearthandharvest/recipes.js` and `tags.js` (recipe ids `creativedelight:...`); `server_scripts/equipment/watering_can.js`
  deleted. patcherMod and CD must be installed together (old patcherMod + new CD registers the id twice).
- **Tiers (pack addition, owner 2026-10-31):** one class, `WateringCanTier` holds the numbers. Capacity is for water
  and fertiliser each. Fills from a water source stay free; a well or any block entity with a water fluid handler costs
  exactly 1000 mB per fill (the well's roof half routes to the block below); a water cauldron costs the tier's levels.
  Every fill tops the can to full whatever it held.

  | Tier | Id | Area | Capacity | Cauldron levels per fill | Upgrade (smithing, from the tier before) |
  |---|---|---|---|---|---|
  | Copper | `hearthandharvest:watering_can` (1.4.1 id) | 1x1 | 8 | 1 | crafted: 5 copper ingots around a bucket |
  | Iron | `hearthandharvest:iron_watering_can` | 2x2 | 12 | 1 | + iron block |
  | Gold | `hearthandharvest:golden_watering_can` | 3x3 | 16 (1.4.1's can) | 1 | + gold block |
  | Diamond | `hearthandharvest:diamond_watering_can` | 4x4 | 20 | 2 | + diamond |
  | Netherite | `hearthandharvest:netherite_watering_can` | 5x5 | 32 | 3 (all) | + netherite ingot; fire resistant |
  | Dragon | `hearthandharvest:dragon_watering_can` | 7x7 | 64 | 3 (all) | + Dragon Loot dragon scale; fire resistant |

  A cauldron with fewer levels than the tier's fill takes is emptied for a partial fill: capacity x levels / tier levels,
  rounded down, added to what the can holds (Diamond 1 level = 10; Netherite 1 = 10, 2 = 21; Dragon 1 = 21, 2 = 42).
  Owner, 2026-10-31 (first build refused the low cauldron instead).
  Even areas (2x2, 4x4): the extra row on each axis goes to the side of the clicked block the hit point is on. Upgrade
  materials and the smithing route are Claude's picks (owner gave tiers, areas, capacities and
  cauldron costs only); smithing was picked because vanilla smithing copies the base's NBT, so the contents carry over.
  Old cans holding more than a tier's capacity read as the capacity (clamped on read). The copper tier is now
  "Copper Watering Can". All tiers use the copper (1.4.1) texture for now. Not in `#creativeengineering:dragon_gear`
  (no durability).
- **Code (before 2026-10-31):** patcherMod `wateringcan/` (`WateringCanItem`, `WateringCanItems`, `WateringCanJeiPlugin`). Data components
  became stack NBT (`WaterLevel`, `BonemealLevel`, `FertilizerItem`; keys removed at 0, so a used-up can is a plain
  stack again).
- **Mechanics (same as upstream):** 16 water, filled to full by using it on a water source. 16 fertiliser, loaded one
  item per use from the off hand (main hand only), one kind at a time ("Already loaded with X!" otherwise); fertiliser
  is `#hearthandharvest:bonemeal_substitutes`. Use on a bonemealable block: one water and one fertiliser, bone meal on
  the 3x3 at that height (each block rolls its own bone-meal success; nothing spent if nothing grew). Use on fire or
  any block whose `lit` property is on (campfires; upstream's check also catches candles, redstone ore, lamps and torches, kept as is): one water, `lit` set off. Durability bar =
  water + fertiliser out of 32. Tooltip shows both levels.
- **Soil watering (pack addition, owner 2026-10-30):** a use on a plant or on soil also soaks the soil of the 3x3:
  soil = any block with farmland `moisture` (vanilla farmland, FD Rich Soil farmland, IW Fertile Soil, where 7 =
  soaked), taken at the clicked block if it is soil, else the block under it. Every soil block below 7 goes to 7. With
  fertiliser and something grew: one water and one fertiliser as before (soaking is free). No fertiliser, or nothing
  grew: one water if any soil was dry, nothing spent otherwise. Fertile Soil then stays soaked (IW dries it only next
  to a heat source); vanilla farmland dries one step per random tick without water nearby, as after rain. The client
  claims the click only when the server will act (water, and fertiliser on a bonemealable block or dry soil in the 3x3).
- **Splash on plants (pack addition, owner 2026-10-31):** every block bone meal grew also gets water splash particles
  (server `sendParticles`, seen by all nearby players), like soaked soil. A plant whose soil this click soaks gets the
  soil's splash only, so berry bushes on grass, crops on soaked farmland and the like now show water too.
- **Not on trees (pack addition, owner 2026-10-31):** tree growers are skipped. Rule (`WateringCanItem.isTreeGrower`):
  `#minecraft:saplings` (vanilla and modded saplings, azaleas, mangrove propagule), any `SaplingBlock` subclass
  (modded saplings left out of the tag; the propagule is one), `AzaleaBlock`, `FungusBlock` (crimson / warped fungus
  into huge fungi), `MushroomBlock` (huge mushrooms), plus the block tag `#hearthandharvest:watering_can_tree_growers`
  (empty; for growers none of these catch). A click on one does nothing (no water, no soaking); in the area, they
  get no bone meal. Not counted as tree growers: mangrove leaves (grow a propagule, not a tree), moss, grass, bamboo,
  FD / Nether's Delight mushroom colonies (pack check 2026-10-31: no other sapling-like block outside the tag).
- **Upstream bug fixed:** a charged click on a crop fertilised twice on the server (the client's `useOn` passed, so it
  also sent the use packet, whose ray trace hit the same block). The client now claims the click; the `use` bone-meal
  branch is gone (unreachable after the fix).
- **Recipe:** upstream shape (5 copper ingots around a bucket), `server_scripts/equipment/watering_can.js` via NCH.
  The tag (same file) holds bone meal only: upstream's other member, H&H Manure, is not in the pack yet; the full port
  adds it. Upstream's `c:tools` membership not carried (no pack use).
- **Model:** upstream's item model routes every view to the 2D sprite (its 3D model is never shown and has `#missing`
  textures), so ours is a plain generated model.
- **Full port:** done 2026-10-31 (see "Moved and tiered" above).
- Tests HNH-25 to HNH-33, HNH-38 to HNH-47 (section 8; HNH-25 to -33 were written for the 16 / 3x3 can, now the Golden tier). Scratch server r1031 (2026-10-31) PASSED SQ-WC-01 (boot, recipe), -01b, -02,
  -03 (off-hand bone meal load), -05 (soil watering on farmland and mulch); -03 and -05 again on the moved can r1101; tiers
  SQ-WCT-01 to -05 PASSED r1101 (SQ-WCT-03 well note: see SQ-PSF-09, r1102b, the drain does reach the well tank).
  CQ-WC-04, -06, -07 open (hand steps).

### 4.1 Raspberries, first piece of the port (built 2026-10-31)

Owner, 2026-10-31: the raspberry crop (behaviour and textures) only, the way the watering can was done, into the
H&H port as a Creative Delight member. Settles D3 (raspberry pips kept). Built 2026-10-31, shipped 1.1.06 (Raspberries
bullets); scratch PASSED r1101 (SQ-HR-01 to -05, CQ-HR-06).

- **Home:** CD member `backportedHearth` (`development/creativeDelight/members/hearth/`, mod id `hearthandharvest`,
  version `1.4.1-ce-19.2-01`), nested in the CD jar (`bundle_members`, `replaces_jars` also moves any standalone
  `hearthandharvest-*.jar` / `backportedHearth-*.jar` out of `mods/`). Member switch `creativedelight-common.toml
  [members] hearthandharvest` (added by CD from the members list). Later H&H pieces go in the same member; the
  watering can moved here 2026-10-31 (4.0).
- **Route:** MIT (as 4.0). Art and data copied unchanged from the 1.4.1 jar (`NOTICE-upstream-MIT.txt` lists them);
  the loot table loses the 1.21 `random_sequence`. Java is our own, written for 1.19.2.
- **Ids:** `hearthandharvest:raspberry_bush` (block), `hearthandharvest:raspberry` (food), `hearthandharvest:raspberry_pips`
  (planting item). Upstream registers the pips only with Berry Good loaded (then the raspberry is plain food, else the
  raspberry plants the bush); the pack ships Berry Good, so only that form is built. Creative tab: Farmer's Delight's,
  as the other members. `global.item` keys `raspberry`, `raspberry_pips`, `t_fruits_raspberries`, `t_crops_raspberry`,
  `t_seeds_raspberry`, `t_fox_food`.
- **Behaviour (as 1.4.1):** a sweet berry bush (ages 0-3, random-tick growth, bone meal, placed on `#minecraft:dirt` or
  farmland). Differences from sweet berries: picked by hand only at age 3, 2-3 raspberries, back to age 1 (sweet berries
  from age 2); slows walkers but never hurts them (foxes and bees pass freely); pick block gives the pips. Breaking it
  drops 2-3 raspberries at age 3, 1-2 at age 2 (Fortune adds). Raspberry: 2 hunger, 0.4 saturation modifier, eaten fast.
  Foxes get 1.4.1's berry bush goal (priority 10): walk to a ripe bush (age 2+), take one raspberry into the mouth if
  empty, drop the rest (1-2, one more at age 3), bush to age 1; needs mob griefing.
- **Recipe:** 1 raspberry -> 1 pips (shapeless, upstream), CD `kubejs/data/creativedelight/kubejs/hearthandharvest/recipes.js`.
- **Tags** (CD `hearthandharvest/tags.js`, 1.4.1's `c:` tags as `forge:`): raspberry in `#forge:fruits/raspberries`
  (the pack's existing plural tag) and `#forge:fruits`, `#forge:crops/raspberry` and `#forge:crops`, `#forge:berries`,
  `#forge:fruits/berries`, `#minecraft:fox_food`; pips in `#forge:seeds/raspberry` and `#forge:seeds`. Food type
  `raw/fruit` (`food_types.json`), jar family `berries` (`creativeengineering/jar_tags.js`).
- **Seasons:** `temperate` in `crop_seasons.json` (1.4.1: spring and summer crop), not Serene Seasons tags.
- **WTHIT:** growth line rule in CD `crop_growth.json`, harvestable at age 3.
- **Worldgen:** wild crop rule `raspberry` in `wild_crops.json` ("temperate grassy" or "cold grassy snowy", 44 biomes, crops_by_biome.md; the blueberry biomes added 2026-11-02, `farming/wild-crops #7`),
  feature `creativedelight:wild/raspberry_bush` = 1.4.1's patch (Farmer's Delight wild crop: ripe bushes on dirt, with
  grass and coarse dirt; 1 in 62 chunks; `short_grass` -> 1.19.2 `minecraft:grass`). Upstream's biome modifier (every
  overworld biome of temperature 0.4-0.9) is not carried. The pips have no other source.
- **Not in this piece:** raspberry crate, juice, wine, jam, pie, scone (later H&H pieces or the jam family, 2.1);
  compost uses the pack rules (seeds and fruit categories), no own row. Cans, food families, names, JEI coverage and
  stack sizes come from `/dumpall` + `/cegen` after install.
- Tests HNH-03, HNH-04 (raspberry half), HNH-34 to HNH-37; scratch SQ-HR-01 to SQ-HR-05, CQ-HR-06: all PASSED r1101
  (SQ-HR-04: the item id is `hearthandharvest:raspberry`; SQ-HR-05: 26 biomes in `wild_crops/raspberry`).

0. **Decide D1 first** (port all vs port the new content only). The steps below assume the recommendation: an in-tree
   port that registers only the kept content (sections 2 and 3), so nothing needs an item merge.
1. **Port** (skill `port`, [feature_porting.md](feature_porting.md)): the CD member `backportedHearth`
   (`development/creativeDelight/members/hearth/`, mod id `hearthandharvest`, started with 4.1). NeoForge 1.21.1 -> Forge 1.19.2: data
   components (`SALTED`, vintage tier, jug and pouch contents) -> NBT; 1.21 data-driven enchantment -> Java
   `Enchantment` (only if Tilling stays, D11); `DeferredHolder` -> `RegistryObject`; data paths `recipe` -> `recipes`,
   `loot_table` -> `loot_tables`, `tags/item` -> `tags/items`, `advancement` dropped; result format `id` -> `item`;
   `c:` tags -> `forge:`; NeoForge biome modifiers and data maps -> Forge biome modifiers / pack systems; mixins
   re-targeted for 1.19.2. Drop the dropped ids from the registries and their assets, recipes, loot, tags and lang.
   Add a `mod_sides.md` row (both sides).
2. **Dumps and generators** (memory `dumpall-refresh-workflow`): install, `/dumpall`, then `/cegen` (names, food
   catalog, JEI coverage, stack sizes, physics, WTHIT icons, crop table).
3. **Ids and tags:** new keys in `item_dict.js` (and block / fluid dicts) for every kept id; tags through `global.tags`
   in a CD `hearthandharvest/tags.js`: `#forge:crops/raspberry`, `#forge:crops/hops`, `#forge:fruits/raspberry`,
   `#forge:butter`, `#forge:jams` (via the jam family), `#forge:cheese` (goat cheese), `#forge:milk` (goat milk),
   Serene Seasons crop tags for kept crops, `#minecraft:small_flowers` / `flower_pots` for mums, storage block tags
   for kept crates.
4. **Recipe retargets** (in the port's data, or CD `hearthandharvest/recipes.js` via `global.NCH`): every kept recipe
   that named a dropped item takes the pack survivor or tag (2.2 list). Upstream recipe removals: the Botany Pots and
   Crabber's Delight recipes (mods absent), bamboo / palm / cherry recipes (2.4).
5. **Mob loot** ([loot.md](loot.md)): none if the crow is dropped (D2). Otherwise crow feather in the crow table and
   `/dumpall` loot refresh.
6. **Item merges:** none (nothing installed). Only if D1 goes "port everything": every Drop row of 2.1 becomes a
   `global.addItemMerges` pair in the `creativedelight` group.
7. **Potions** ([potions.md](potions.md), [potion_buckets.md](potion_buckets.md)): Pungent and Tempting potions (long /
   strong), tipped arrows, potion buckets, IE / Create mixer routes per pack rules. Bad Omen from crow feather only if
   the crow stays.
8. **Alcohol** ([alcohol.md](alcohol.md) 11): kept drinks (Blueberry, Raspberry, Sweet Berry, Glow Berry, Melon Wine,
   Root Beer?, Moonshine) join `global.alcohol` (hunger 1, saturation 0, Intoxication, Nausea, "Intoxicating Drink").
   Root beer is not alcoholic upstream: confirm. Drunk effect removed.
9. **Machines:** H&H keg recipes -> Brewin' keg fermenting (wines, moonshine, root beer) and pouring; pickling -> Vintage
   pickling jar; brine -> keg or jar. Cask and stomping basin per D12. Create filling / mixing / compacting kept where
   they make kept items.
10. **Recipes by pack rules** through `global.NCH` ([recipes_cooking.md](recipes_cooking.md)): heatless `_manual_only`
    for cooking-pot meals, roll rules, sandwich rules; baking (pies, croissant, scone, donut, cookies) through the
    oven ([oven.md](oven.md)); Slice & Dice and Central Kitchen mirrors; container remainders (bottles, buckets, jars).
11. **Effects** ([effects.md](effects.md), JEED text): Pungent, Tempting, Clarity, Prickly, Pinned descriptions in the
    JEED override; check the effects table generator picks them up.
12. **Food types and values** (`food_types.json`, [food.md](food.md)); food families (`tools/food_families.py --check`:
    jam, pickle, pie, pizza, sausage / skewer, cheese families); stack sizes ([stack_sizes.md](stack_sizes.md)); cans
    and jars ([cans.md](cans.md): cannable kept foods; jams into mason jars).
13. **Compost** ([composting.md](composting.md)): kept crops, pips, husks, manure (the pack framework, not NeoForge data
    maps).
14. **Crops, wild crops, seasons** ([crops.md](crops.md)): raspberry bush and hops trellis soil rules, WTHIT growth
    line, harvest behaviour, wild hops and wild raspberry through `wild_crops.json` + `tools/gen_crop_worldgen_table.py`;
    mums through `wild_flowers.json` and the flower pots matrix ([flowers.md](flowers.md),
    [flower_pots.md](flower_pots.md)). H&H's own biome modifiers removed.
15. **Worldgen and structures:** Corn Maze and Lilliput Lane through the structure framework (groups, spacing,
    placement priority, biome fits), chest loot through `loot.md`; salt caves per D13; ground nests dropped.
16. **Names and lang** ([names.md](names.md)): name clashes ("Spool", "Crate", "Counter", "Drawer", "Sink", "Trellis",
    "Mulch", "Jug"), player-facing spelling (Smore -> S'more if kept: it is dropped).
17. **Mob and block audit:** BlockRunner tier for new floor blocks (salt, manure, mulch), mineable tags, Item Physic
    (sinks / floats), WTHIT ([wthit_lines.md](wthit_lines.md)) for crops, keg-less machines, jug, sprinkler.
18. **Tooltips** ([tooltips.md](tooltips.md)) and **JEI** ([jei_coverage.md](jei_coverage.md)): H&H's info texts
    (tree tapper, watering can, trough, nests...) reworded per the pack style; JEI categories of kept machines.
19. **Placeables / Display Delight:** pies, pizzas, cheese wheels, jugs, hay rugs ([placeable_food.md](placeable_food.md)).
20. **Kitchen furniture** per D10 ([kitchen.md](kitchen.md)).
21. **Trades and chests:** H&H villager code dropped; kept crops into the trader catalog if wanted.
22. **Every Compat:** H&H's module adds half cabinets for other woods; check the pack's Every Compat config.
23. **Config:** `configs_edited/` copy of the ported mod's config (crows off if kept as an option, litters per D14,
    water bottle stacking off).
24. **Release:** `mod_sides.md` row, changelog bullets per family (only when built), TODO row, plan row.

Order: 0 and 1 before everything; 2 after 1 and again after 3-4; 3 before 4, 7-15; 8-9 before 10; 12 after 10;
14-15 after 2 (crop table and worldgen dumps); 24 last.

Routing (memory `creative-delight-routing`): food recipes, tags, food values and cooking rules go in a new CD tree
`development/creativeDelight/src/main/resources/kubejs_startup/hearthandharvest/` (startup) and
`kubejs/data/creativedelight/kubejs/hearthandharvest/` (server); engine code (blocks, entities, mixins) stays in the
port jar; brewing and alcohol stay in the alcohol scripts (reserved for Creative Brewing).

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Port all of H&H, or only what is new (2.1 Drop rows never registered)? | port the new content only; nothing installed means no merges | new content only |
| D2 | H&H crow vs Alex's Mobs crow | drop H&H's crow, crow feather, Bad Omen brew and crow stash; scarecrow keeps working on AM crows only if code is added (not by default) | drop |
| D3 | Berry pips (blueberry, raspberry) | keep raspberry pips (seed for the new bush); blueberry pips follow Fruits Delight's blueberry (none upstream): drop | as recommended |
| D4 | Sap and syrup: H&H tree tapper + sap cauldron vs D&D sap (rubber logs) and Rustic syrup | keep the tapper as a second syrup source (spruce / dark oak), one Syrup item (Rustic's), H&H sap fluid renamed if D&D's stays rubber-only | decide with Extra Delight |
| D5 | Chocolate Milk Bottle | drop (Neapolitan milkshake covers it) | drop |
| D6 | Near-duplicates (veggie pizza, corn stew, cotton candy) | drop all three | drop |
| D7 | Campfire roasting (marshmallow, "hold near a campfire") and the Charred Marshmallow | keep the mechanic on Delightful's marshmallow stick, add Charred | decide with Extra Delight |
| D8 | Farmer's Hat (XP on harvest) and Seed Pouch (area planting, auto-pickup) vs Wardrobe hat / Quark pouch | keep both mechanics, give them to the existing items or rename H&H's ("Planter's Pouch") | keep H&H's, renamed |
| D9 | Sprinkler and trough vs S&D sprinkler and Quark feeding trough | keep H&H's sprinkler as the non-Create farm block (renamed "Field Sprinkler"); drop the trough unless water-trough growth is wanted | as recommended |
| D10 | Kitchen blocks (half cabinets, counter, drawer, sink, bottle crate) | keep half cabinets only; drop counter, drawer, sink (CfB and Handcrafted cover them); bottle crate with the jam / drink storage plan | as recommended |
| D11 | Trellis, Mulch, Tilling enchant | keep trellis (hops); rename H&H mulch or drop it (IW mulch is the pack's Fertile Soil); drop Tilling and Harvesting (hoe areas cover harvest; Tilling is new but needs a Java enchantment port) | as recommended |
| D12 | Cask (aging tiers) and Stomping Basin | keep the stomping basin as a manual juicer if Vinery's press does not cover berries; cask tiers only if Extra Delight has nothing better | decide with Extra Delight |
| D13 | Salt extras: decor set, licked salt blocks, salt drip, salt caves, bags | drop the decor set and bags (Vintage / The Salt cover them); licking: add the mechanic to The Salt's block only if wanted; salt caves: drop (The Salt ores + beach patch) | drop |
| D14 | Small behaviours: pig / rabbit litters, cow bottle milking, village farm crops | keep rabbit litters and bottle milking; pig litters wait for the mob food overhaul (`tweaks #13`, Fungi Delight truffle litters); drop village crops | as recommended |

## 6. File map (planned)

| Path | What |
|---|---|
| `development/creativeDelight/src/main/resources/kubejs_startup/hearthandharvest/` | items dict glue, food values, tags (CD startup) |
| `kubejs/data/creativedelight/kubejs/hearthandharvest/` | recipes (NCH), cooking rules, removals (CD server) |
| `kubejs/startup_scripts/_lib/item_dict.js` | `global.item` keys for kept ids |
| `kubejs/startup_scripts/cooking/alcohol_drinks.js` | kept drinks in `global.alcohol` |
| `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json`, `kubejs/generation_scripts/worldgen/wild_flowers.json` | wild hops, raspberry, mums |
| `mod_sides.md`, `configs_edited/root_config/hearthandharvest-*.toml` | sides row, config backup |
| `development/creativeDelight/members/hearth/` | the port as a CD member (`backportedHearth`): Java, assets, data of kept content; raspberries (4.1), watering cans (4.0) |

## 7. Offline checks (once built)

- `./gradlew :backportedHearth:build --offline` clean; jar has no dropped ids (`unzip -l | grep` per Drop row).
- `python3 tools/food_families.py --check`, `python3 tools/changelog_lint.py`.
- Grep the port's data for `c:` tags, `"id":` results, `loot_table/`, `recipe/` (1.21 leftovers): none.
- Grep the port's data for every dropped id: none.
- `latest.log` after first boot: no `Failed to parse`, no `hearthandharvest` errors.

## 8. In-game test suite (drafted)

- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-SGF-01, -02, SQ-WCT-06, -07, -08.

| ID | Test | Pass |
|---|---|---|
| HNH-01 | Boot with the port: no H&H errors in `latest.log`, JEI shows only kept items | |
| HNH-02 | None of the 2.1 Drop ids exists (`/give` fails) | |
| HNH-03 | Raspberry bush (4.1): pips plant it on dirt, farmland, rich soil (not on sand or stone); it grows; picked by hand at age 3 only (2-3 raspberries, back to age 1); hoe area harvest breaks it | server half PASSED r1101 (SQ-HR-01 to -03) |
| HNH-04 | Wild raspberry and wild hops appear in their biomes (crop table) | raspberry half: server PASSED r1101 (SQ-HR-05, 26 biomes) |
| HNH-05 | Hops climb the trellis and ripen; harvested by hand | |
| HNH-06 | Mums generate as wild flowers and go in flower pots | |
| HNH-07 | Kept wines ferment in the Brewin' keg; each shows "Intoxicating Drink", half a rotten shank, Nausea | |
| HNH-08 | Pickled cabbage / carrots / potatoes made in the Vintage pickling jar | |
| HNH-09 | Raspberry, cherry, grape jams exist in the jam family (mason jar) | |
| HNH-10 | Goat milking with bucket and bottle; goat milk bucket clears all effects | |
| HNH-11 | Goat cheese: unripe wheel ripens; slices from the wheel | |
| HNH-12 | Cow bottle milking gives the Farmer's Delight milk bottle (merge survivor) | |
| HNH-13 | Chicken plucking: bare hand hurts, shears do not; feathers drop | |
| HNH-14 | Butter and its recipes; `#forge:butter` holds it | |
| HNH-15 | Sausage line, hot dog: recipes use pack survivors (bread tag, cooking pot rules) | |
| HNH-16 | Pies: oven baking, slices place and eat | |
| HNH-17 | Tree tapper on spruce collects sap; sap cauldron over fire gives syrup (D4) | |
| HNH-18 | Pungent and Tempting potions brew (long, strong), arrows and buckets exist; JEED text shows | |
| HNH-19 | Cleaver: butchering drops and throwing; Pitchfork: Pinned on hit | |
| HNH-20 | Horseshoe on a horse gives the speed boost | |
| HNH-21 | Manure from sheep after grazing; manure composts; manure bricks smelt | |
| HNH-22 | Corn Maze and Lilliput Lane generate through the structure framework; chest loot has no dropped ids | |
| HNH-23 | Salting a food uses The Salt's craft only (no H&H "Salted" tooltip) | |
| HNH-24 | Half cabinets: cut from FD cabinets, store items; cherry uses Vinery's cabinet | |
| HNH-25 | Watering can (4.0): crafted from 5 copper ingots and a bucket; shows in Tools tab and JEI with its info page | server half PASSED r1031 (SQ-WC-01, -01b: recipe loads) |
| HNH-26 | Use on a water source: water 16 / 16, bucket fill sound; tooltip shows both levels | |
| HNH-27 | Bone meal in the off hand, use in the air: one bone meal taken per use up to 16; full can takes no more | server half PASSED r1031 (SQ-WC-03) |
| HNH-28 | Charged can on a wheat patch: one use spends exactly 1 water and 1 fertiliser and grows the 3x3; grown crops spend nothing | |
| HNH-29 | Lit campfire and fire: one use each puts it out for 1 water; empty can does nothing | server half: SQ-WCT-08 (to queue, deep scan 2026-11-02); CQ-WC-04 |
| HNH-30 | Use the can until water and fertiliser are both 0: bar gone, fertiliser kind cleared (another fertiliser loads again) | |
| HNH-31 | Can with water, no fertiliser, on young wheat over dry farmland (3x3): farmland in the 3x3 turns wet, splash, water -1; again at once spends nothing | server half PASSED r1031 (SQ-WC-05: 3x3 farmland and mulch soaked, water -1 once) |
| HNH-32 | Same on dry Fertile Soil under a crop and on the soil block itself: soaked texture in the 3x3, water -1; stays soaked after a few minutes away from heat | |
| HNH-33 | Charged can on young wheat over dry farmland: crops grow and soil soaks for 1 water and 1 fertiliser total; on grown wheat over dry soil: soaks for 1 water, no fertiliser spent | |
| HNH-34 | Raspberry Pips from 1 raspberry (crafting grid); pick block on the bush gives the pips; both in Farmer's Delight's tab | |
| HNH-35 | Walking through the bush slows, never hurts; a fox walks through freely and picks a ripe bush (raspberry in its mouth, bush to age 1) | fox half: server PASSED r1101 (SQ-HR-04) |
| HNH-36 | WTHIT on the bush shows the growth line, "Mature" at age 3; the pips tooltip shows the temperate season line | client PASSED r1101 (CQ-HR-06) |
| HNH-37 | Raspberry: 2 hunger, eaten fast; in `#forge:fruits/raspberries`, `#forge:berries`, `#minecraft:fox_food`; pips in `#forge:seeds` | tags: server PASSED r1101 (SQ-HR-04) |
| HNH-38 | Watering can tiers (4.0): every tier in the Tools tab with its name, tooltip "Area: NxN" and its capacity; JEI info page shows that tier's numbers | server half PASSED r1101 (SQ-WCT-01: six ids) |
| HNH-39 | Smithing: Copper + Iron Block -> Iron, then Gold Block, Diamond, Netherite Ingot, Dragon Scale up the chain; a can with water and bone meal keeps both | server half PASSED r1101 (SQ-WCT-05) |
| HNH-40 | Area: Copper grows / soaks 1 block, Iron 2x2 (extra row toward the aimed side), Golden 3x3, Diamond 4x4, Netherite 5x5, Dragon 7x7; still 1 water and 1 fertiliser per use | server half PASSED r1101 (SQ-WCT-04: dragon 7x7, copper centre only) |
| HNH-41 | Water cauldron, 3 levels: Copper / Iron / Golden fill to full for 1 level, Diamond for 2, Netherite and Dragon empty it; an empty Diamond can on a 1-level cauldron empties it for 10 water, an empty Netherite can on 2 levels for 21 | server half PASSED r1101 (SQ-WCT-02) |
| HNH-42 | Well (Just a Well) with water: any tier fills to full and the well loses 1 bucket; clicking the roof half works too; an empty well does nothing | server half PASSED r1101 (SQ-WCT-03); well drain r1102b SQ-PSF-09 (real PASS) |
| HNH-43 | An old can holding 16 / 16 (pre-tier) shows 8 / 8 as Copper and works | server half PASSED r1101 (SQ-WCT-05: 16 clamps to 7 after a use) |
| HNH-44 | Netherite and Dragon cans survive lava and fire as items | server: SQ-WCT-06 (to queue, deep scan 2026-11-02) |
| HNH-45 | Fertilised can on a sweet berry bush on grass: bush grows, water splash particles show on it (also seen by a second player) | |
| HNH-46 | Fertilised can on an oak sapling, a modded sapling (Vinery / Fruits Delight), a mangrove propagule, a crimson fungus on nylium, an azalea and a red mushroom: nothing happens, no water or fertiliser spent; a sapling inside the area of a click on a crop does not grow | server half: SQ-WCT-07 (to queue, deep scan 2026-11-02); CQ-WC-07 |
| HNH-47 | Water Spout Gun (Create: Misc and Things) shot at dry farmland, or at a crop on dry farmland: the farmland turns wet with a splash; the lava / honey guns do not wet it | server half: SQ-SGF-01, -02 (to queue, deep scan 2026-11-02); CQ-SG-01 |

## 9. History

- 2026-10-30: investigation written (overlap and integration process only), nothing built. Jar unpacked and
  decompiled to `DecompiledMods/`. New content detail deferred to the combined pass with Extra Delight.
- 2026-10-30: combined pass written in [extra_delight.md](extra_delight.md) 2.5: placeholders settled, D4 / D7 / D12
  answered there (5.1); H&H port first in stage 2 (ED D16).
- 2026-10-30: watering can built ahead of the port in patcherMod 1.1.04 (4.0), installed to `mods/`; tests HNH-25 to
  HNH-30, scratch SQ-WC-01 to CQ-WC-04 queued.
- 2026-10-30: soil watering added (4.0): a use on a plant or soil soaks the 3x3 soil, also without fertiliser;
  tests HNH-31 to HNH-33, scratch SQ-WC-05.
- 2026-10-31: scratchpad notes translated into TODO rows (`gameplay/misc #17`, `#18`, `ui/names #11`). No code changed.
- 2026-10-31: raspberries built as the first piece of the port (4.1), in the new CD member `backportedHearth`; CD jar
  built, not installed (game running). Tests HNH-03, HNH-04, HNH-34 to HNH-37; scratch SQ-HR-01 to CQ-HR-06.
- 2026-10-31: watering can moved from patcherMod into `backportedHearth` (4.0) with six tiers (copper to dragon), well and
  cauldron filling, smithing upgrades. patcherMod and CD built, not installed (game running). Tests HNH-38 to HNH-44;
  scratch SQ-WCT-01 to -05.
- 2026-10-31: splash particles on every plant the can grows; tree growers (saplings, propagules, fungi, azaleas,
  mushrooms) excluded; the water Spout Gun (Create: Misc and Things) soaks farmland it hits (patcherMod
  `compat/SpoutGunFarmland`, `ProjectileImpactEvent`). Tests HNH-45 to HNH-47.
