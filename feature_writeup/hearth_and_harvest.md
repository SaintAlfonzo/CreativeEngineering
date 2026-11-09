# Hearth and Harvest: overlap and integration report

Overlap, content and integration plan for TODO `largecontent/mod-additions #10`. Organised around five questions, each answered **in the row it concerns, with the current decision**: overlapping items and blocks (2.1), overlapping systems (2.2), new items and blocks (3.1), new systems (3.2), integration required (4). Owner decisions (D1 to D14, N1 to N22) and scan defaults (D15 to D26) are merged into those rows; the newest owner decision wins over an older row text.

Jar: `devmods/toBackport/later_large_content_overlap/hearthandharvest-1.21.1-1.4.1.jar`, unpacked to `DecompiledMods/hearthandharvest-1.21.1-1.4.1/`, decompiled to `DecompiledMods/hearthandharvest-1.21.1-1.4.1-src/`. Licence MIT (GPL-compatible: fork or port allowed). NeoForge 1.21.1 only, so a **port** to Forge 1.19.2 is needed (skill `port`). Short name: **H&H**. Test prefix: `HNH-`.

Built, in the Creative Delight member `backportedHearth`: the watering can with six tiers (4.0) and raspberries (4.1). Everything else is decided or open but **not built**. **Build principle (owner 2026-11-06, N22): H&H goes in first and complete for the kept content, including every item Extra Delight would also add (butter, mac and cheese, glazed carrots, sunflower seeds and the rest); each is evaluated against the pack's state afterwards, and Extra Delight merges into the pack and the existing mods later.**

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner asks by name; upstream bugs are fixed.

**How to read a decision cell.** `Drop` = not registered by the port (H&H was never installed, so there is no merge and no old id; recipes that used it point at the pack survivor). `Keep` = ported. Then the source: `D#` / `N#` (owner question or directive id, index in section 5), `owner` (a direct owner statement, date), `scan` (a recommendation from the 2026-11-06 scan the owner has not answered; the default applies), `Built` (in the member now). Texture review (owner, 2026-11-04): side-by-side sheets of every overlap are in `feature_writeup/hearth_and_harvest_{crops,drinks_jams,dishes,gear_blocks}_overlap.png`; the only keeper was the Rope Coil block; **owner 11-06 (N20) reopened the comparison** for syrup, cheese, cotton candy, waffle, hat, storage and others (see the rows).

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Overlap, same item in an installed mod | 2.1 | 104 ids on 10-30; 11-06 refresh: 57 of 95 rows unchanged, 38 changed (mostly closed by an owner decision), 4 wrong ids fixed |
| Overlap, same concept or block family | 2.1 | 43 ids |
| Overlap, systems | 2.2 | 23 rows (6 unchanged, 17 changed by decisions or the Creative split) plus 3 new rows |
| Blocked: needs 1.20 vanilla or a missing mod | 2.3 | 8 ids, all dropped (N17) |
| New items and blocks | 3.1 | 112 ids on 10-30; 7 turned out already covered (D19); 56 food / drink ids priced |
| New systems and mechanics | 3.2 | 24 rows |
| Upstream issues | 3.3 | B1 to B12 (no load errors: never installed) |
| Integration | 4 | 4.0 and 4.1 built; 4.2 lists 24 steps, port first |
| Decisions | 5 | D1 to D14 and N1 to N22 answered or recorded; D15 to D26 raised 11-06; D15, D16, D19 to D24 and D26 answered 11-08 (N26, section 5.10); a few sub-calls open |
| Tests | 8 | HNH-01 to HNH-90 drafted; 4.0 / 4.1 pieces mostly PASSED on scratch (r1031, r1101, r1102b, r1103); CQ-WC-04, -06, -07 open |

- **Jar:** `hearthandharvest` 1.4.1, author AlabasterLeking, "A homesteading addon for Farmer's Delight".
- **Route:** in-tree CD member `backportedHearth` (`development/creativeDelight/members/hearth/`, mod id `hearthandharvest`), our pack-side food code in a CD tree (memory `creative-delight-routing`).
- **Dependencies:** NeoForge 21.1+, MC 1.21.1, Farmer's Delight 1.3.0+. Optional by `mod_loaded`: Create (54 recipes), Brewin' and Chewin' (41), Botany Pots (14; installed since 2026-10-30), Berry Good (4), Crabber's Delight (3, not installed). Code compat: Every Compat, Thirst Was Taken (not installed), JEI.
- **Not to be confused with** **Hearths** (`hearths-v1.0.0`, a Nether biome datapack, installed).
- **Dumps:** `kubejs/exported/items_by_mod/` was refreshed 2026-11-05 (after the Creative split, 20 489 ids); `display_names.json` is older than the in-tree lang, so the overlap search also read `development/*/src/**/lang/en_us.json`.

## 1. Overview

H&H is a homesteading content mod: berries, grapes, hops, corn, peanuts, cotton; a keg, a cask (aging), a stomping basin; jams and pickles in jars; cheese, goat milk, butter, sausages, pies, pizzas; salt (salting any food, salt caves, lickable salt blocks); manure and mulch; farm tools (watering can, sprinkler, fertilizer bag, seed pouch, cleavers, pitchfork, horseshoe, scarecrow, trough, nest); kitchen furniture; storage crates and bags; mums; a tameable Crow; two structures (Corn Maze, Lilliput Lane).

| Fact | Value |
|---|---|
| Items / blocks / fluids | 267 item models; 171 blocks (160 blockstates); 27 fluids (goat milk, cooking oil, brine, sap, syrup, 8 wines, mead, moonshine, root beer, beer, apple cider, hard cider, 8 juices) |
| Effects | Pungent, Tempting, Drunk, Clarity, Prickly, Pinned; hidden Horse Boost (from Sugar Cubes, not the horseshoe) |
| Potions | Pungent (awkward + poisonous potato), Tempting (Pungent + fermented spider eye), Bad Omen (awkward + crow feather) |
| Enchantments | Tilling, Harvesting (hoes, max III, data-driven 1.21, all behaviour in code) |
| Entity | Crow (weight 60, groups 3-5): tameable, shoulder, fetches, steals shiny items into nests |
| Recipes (543) | 127 shaped, 99 shapeless, 63 `aging` (cask), 37 FD cutting, 28 FD cooking, 26 Create filling, 24 B&C keg pouring, 22 `fermenting`, 20 stonecutting, 15 `stomping`, 15 B&C fermenting, 15 Create mixing, 14 Botany Pots, 12 Create compacting, 9 campfire, 8 smelting, 6 smoking, 1 `salting`, 1 `shapeless_remainder`, 1 smithing |
| Loot / worldgen / structures | 125 block tables, 9 chest tables, crow drops, 1 GLM (2 % cherry from cherry leaves); wild grapes, cotton, peanuts, hops, blueberry and raspberry, 28 mum patches, ground nests, salt caves, village crops; Corn Maze, Lilliput Lane |
| Tags / advancements / lang | `c:`, `farmersdelight:`, `minecraft:`, `sereneseasons:`, `create:`, `supplementaries:jar_cookies`; 68 advancements (the pack drops all); 316 lang keys |
| Mixins | horse eating corn / hops, water bottles stack, farmland counts mulch as water, sheep leave manure, horse hurt; client: hide break progress, chicken-holding arms |
| Config | `Config.java` (crows, litters, plucking, bottle milking, water bottle stacking, salted bonus, village crops, trades); a per-item toggle map is never read (B1) |

## 2. Overlap

Searched: `item_dict.js`, `items_by_mod/*.json`, `display_names.json`, in-tree lang, [item_merges.md](item_merges.md), and the overlap tables of [vintage_delight.md](vintage_delight.md), [alcohol.md](alcohol.md), [fruits_delight.md](fruits_delight.md), [corn_delight.md](corn_delight.md), [rustic_delight.md](rustic_delight.md), [kitchen.md](kitchen.md), [nests.md](nests.md), [crops.md](crops.md); re-checked 2026-11-06 (N18) against the pack after the Creative split. Namespaces did not change in the split: Vinery and Brewin' are Creative Brewing members; Vintage, Veggies, Delightful, Rustic, Fungi, Miner's, Display, The Salt (`salt:`), Barbeque's, Large Meals, Roll Delight are Creative Delight members.

### 2.1 Items and blocks

**Crops and produce**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Red / Green Grapes, Wild Red / Green Grapes | Vinery red and white grapes, `#vinery:red_grape` / `white_grape`, Vinery grapevine worldgen | **Drop** (D1). Recipes take the Vinery tags (`i.t_red_grapes`, `i.t_white_grapes`); no H&H grape worldgen |
| Cherry | `vinery:cherry` | **Drop**; recipes take Vinery's cherry. The cherry-leaf GLM targets vanilla cherry leaves (absent, 2.3) |
| Blueberries, Blueberry Bush | `fruitsdelight:blueberry`, its bush and worldgen | **Drop** the fruit and bush; FD's blueberry everywhere. **Blueberry Pips: Keep (D3, owner 2026-11-04, overrides the drop recommendation)**: FD's blueberry bush becomes plantable from pips. Not built; check against FD's bush |
| Peanut, Wild Peanuts, crop | `vintagedelight:peanut` and crop | **Drop**; Vintage's |
| Cotton Seeds, Cotton, Wild Cotton, Cotton Plant | Rustic Delight cotton | **Drop**; Rustic's |
| Corn, Corn Kernels, Corn Stalk | `corn_delight:corn`, `corn_seeds`, wild corn | **Drop**; Corn Delight's. Corn Husk is new (3.1) **Corn Husk: implement (owner 2026-11-06, N21)** (source: Corn Delight corn on the cutting board, see 3.1). |
| Corn Meal | `creativeengineering:cornmeal` | **Drop**; pack cornmeal |
| Flour | `create:wheat_flour`, `#forge:flour/wheat` | **Drop**; tag |
| Salt | `vintagedelight:salt_dust` (the pack's one salt, item_merges 2.9) | **Drop**; salt dust / `#forge:salts` |
| Sap Bucket | `create_dd:sap_bucket` (same fluid name); `myrtrees:latex` / Tree Tap (missed on 10-30) | **Keep H&H Sap** (D4, owner 11-04). **N19 (decided 11-05): rename the D&D sap to "Latex"** (fluid, bucket, lang; textures need a recolour; real-life latex chain investigated for the rubber plan). **D25 open (scan)**: `myrtrees:latex` already exists, so the rename makes two Latex items; recommended one latex line (Myrtrees' item, D&D's fluid renamed). Rename on hold, not built **Keep confirmed (owner 2026-11-06, N20).** **Owner 2026-11-07 (N24-1): rename the D&D sap to "Latex" and mark D&D's latex and `myrtrees:latex` for a merge / integration** (integration report first; no id change until it reports). |
| Syrup Bottle | `rusticdelight:syrup` | **Drop H&H syrup item (D4)**: one syrup, Rustic's (`i.syrup`); the tapper / sap cauldron outputs `i.syrup` as a second source **(owner 2026-11-06, N20): review the syrup bottle texture again** against Rustic's before the drop stands; no item is registered until the review is done. |

**Drinks**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Beer, Mead | `brewinandchewin:beer`, `mead` | **Drop** |
| Hard Cider | `vinery:apple_cider` | **Drop** |
| Cherry Wine | `vinery:cherry_wine` | **Drop** |
| Red / Green Grape Wine and Juice | Vinery wines and grape juice bottles | **Drop** |
| Apple Cider fluid | `farmersdelight:apple_cider`, apple cider chain (item_merges 2.6) | **Drop** the item; map the fluid to the pack's apple cider fluid (Central Kitchen) **Owner 11-06 (N20) overrides "map the fluid": implement a cider fluid as the fluid version of `farmersdelight:apple_cider`** (bucket, fluid block, bottle fill / empty give FD's bottle); the H&H item stays dropped. Not built. |
| Chocolate Milk Bottle | Neapolitan / D&D chocolate milkshake | **Keep (owner 2026-11-06, N20)** the bottle. Compare its recipe and values with the milkshake when building. Not built. |
| Goat Milk Bottle / Bucket | none (vanilla goats give cow milk; `delightful:raw_goat` is meat) | not an overlap: **new**, see 3.1 **Keep (owner 2026-11-06, N20)** (bottle and bucket). Not built. |

**Jams, jars, pickles, spreads**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Jar (empty) | `vintagedelight:empty_mason_jar` | **Drop**; mason jar (jams plan `dishes/jams #1`) |
| Apple, Blueberry, Glow Berry, Melon, Sweet Berry Jam | Fruits Delight jellies in mason jars (`jams #8`, 22 FD jams); Vintage's own jam jars are removed items | **Drop**; survivor is FD's jam in its mason jar form |
| Raspberry, Cherry, Grape Jam | **Built**: `vintagedelight:raspberry_jam_jar` (from `hearthandharvest:raspberry`, `jams #7`), `cherry_jam_jar`, `red_grape_jam_jar`, `green_grape_jam_jar` (`jams #4`, shipped 1.1.05) | **Drop**; already in the pack (was "add through jams #1") |
| Pickled Beetroots, Pickled Onions | `vintagedelight:pickled_beetroot`, `pickled_onion` | **Drop**; Vintage's |
| Pickled Cabbage | `vintagedelight:kimchi` (merge 2.19 survivor, built 11-03) | **Drop** (D19, scan: near-duplicate under the D6 rule; accepted 11-08, N26-3) |
| Pickled Carrots, Potatoes | none (Vintage pickles 5 others) | not an overlap: **new**, made in Vintage's pickling jar (3.1) **Add (owner 2026-11-06, N20)**: the new pickles, made in Vintage's pickling jar. Not built. |
| Peanut Butter | `vintagedelight:nut_mash_jar` ("Nut Mash"; Delightful nut butter merged in, item_merges 2.18); `#forge:nut_butter` | **Drop**. H&H recipes take Nut Mash; names should follow |
| Peanut Butter & Jelly | `vintagedelight:pb_j` "Nut Mash and Jam Sandwich" (built) | **Drop** |
| Roasted Peanuts | `vintagedelight:roasted_peanut` | **Drop** |

**Dairy and cooking ingredients**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Cheese Wheel, Unripe Wheel, Cheese Slice | Vintage cheese, Brewin' flaxen / scarlet cheese, `#forge:cheese` | **Drop**; `#forge:cheese` in recipes **Reversed in part (owner 2026-11-06, N20): consider folding H&H's cheese in as a new cheese type** next to Vintage's and Brewin's (own wheel / slice, in `#forge:cheese`). Open: compare textures and values, then decide. Until decided, recipes still use `#forge:cheese`. |
| Cooking Oil | `rusticdelight:cooking_oil` | **Drop** |
| Batter | `rusticdelight:batter` | **Drop** |
| Jerky | `brewinandchewin:jerky` | **Drop** |
| Goat cheese line, Butter | none | not overlaps: **new** (3.1) **Keep Butter; ensure the goat cheese line is added (owner 2026-11-06, N20).** Not built. **N22: H&H's butter is applied now** as the pack's butter (own item, `#forge:butter`); Extra Delight's butter merges into it later. The goat cheese line is crafted the way the Brewin' cheeses are (Brewin' keg, no cask). |

**Baked goods, sweets, dishes**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Pizza / slice | `brewinandchewin:pizza` (renamed Supreme Pizza, built) | **Drop** |
| Meat Pizza, Cheese Pizza (+ slices) | `vintagedelight:meat_pizza`, `cheese_pizza` | **Drop** |
| Veggie Pizza | `veggiesdelight:vegan_pizza` | **Drop (D6, owner 11-04)** |
| Blueberry Pie / Cherry Pie | `delightful:blueberry_pie_slice`, `create_central_kitchen:cherry_pie_slice` (whole pies are blocks; only the `_slice` ids are items) | **Drop** |
| Carrot Cake / slice | `veggiesdelight:carrot_cake` | **Drop** |
| Chocolate Cake / slice | `createaddition:chocolate_cake`, Abnormals Delight slice | **Drop** |
| Blueberry Muffin | `fruitsdelight:blueberry_muffin` | **Drop** |
| Marshmallow, Roasted Marshmallow | `delightful:marshmallow_stick`, `cooked_marshmallow_stick`; Create Confectionery marshmallow on a stick | **Drop the items.** **D7 (owner 11-04): add the roasting mechanic and the Charred Marshmallow** on Delightful's stick ("hold near a campfire"); see 3.1 / 3.2. Not built **Owner 11-06 (N20) confirms: keep the Charred Marshmallow and the campfire roasting mechanic.** |
| Smore | `delightful:smore` | **Drop** |
| Caramel | `create_confectionery:bar_of_caramel` (the 10-30 row had the wrong id `caramel`) | **Drop** |
| Caramel Apple | `create_factory:caramel_apple` | **Drop** |
| Chocolate Bar | `neapolitan:chocolate_bar`, `create:bar_of_chocolate` | **Drop** |
| Cotton Candy | `cloudstorage:cotton_candy` | **Drop (D6)** **Compare (owner 2026-11-06, N20)** (D6 drop on hold): set against `cloudstorage:cotton_candy` (texture, hunger, effects) and decide after. |
| Maple Cookie | `abnormals_delight:maple_cookie` | **Drop** |
| Mashed Potatoes | `moredelight:mashed_potatoes` (split built 11-01) | **Drop** |
| Popcorn, Tortilla, Taco; Corn Bread | Corn Delight | **Drop** |
| Cooked / Uncooked Corn on the Cob | `corn_delight:grilled_corn` / `boiled_corn` | **Drop** |
| Corn Stew | `corn_delight:corn_soup` | **Drop (D6)** |
| Pancake (block) | Rustic pancakes, Supplementaries "Stackable Pancake" (renamed, built) | **Drop** |
| Waffle (block) | `create_factory:waffle` | **Drop** **Reversed (owner 2026-11-06, N20): add the Waffle and compare** with `create_factory:waffle` (texture, values, stacking) to pick the survivor or keep both. |
| Sugar Cubes | `supplementaries:sugar_cube` | **Drop**; also removes the hidden Horse Boost (3.1 effects) |
| Raw / Cooked Skewered Sausage | `barbequesdelight:raw_pork_sausage_skewer` / `grilled_pork_sausage_skewer` (port 11-01) | **Drop** (scan); recipes needing a skewered sausage take Barbeque's |
| Chicken Pot Pie (+ slice) | `largemeals:mushroom_pot_pie` feast (raw chicken, carrot, cabbage, milk, pie crust), pot pie family (port 11-01) | **Drop** (D19, scan: near-duplicate); accepted 11-08, N26-3 |
| Macaroni and Cheese | `brewinandchewin:cheesy_pasta`, `vintagedelight:cheese_pasta` | **Drop** unless Extra Delight is added (it is ED's survivor, D19) **Reversed (owner 2026-11-06, N21): add Mac and Cheese** and evaluate its overlap with `brewinandchewin:cheesy_pasta` and `vintagedelight:cheese_pasta` (same dish or a distinct one; fold or keep both). Needs butter and cheese (see 3.1). Not built. **N22: added from H&H now** (with H&H's butter), evaluated against the pack's pasta dishes afterwards. |

**Tools, gear, farm blocks**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Crow Spawn Egg (Crow entity) | `alexsmobs:crow` (entity; spawns in `flying_birds`) | **Drop the entity (D2, owner 11-05)**; its mechanics go onto AM's crow (3.2). Not built **Reversed (owner 2026-11-06, N20): add H&H's crow entity to compare with AM's crow** (D2 mechanics-only route on hold). The comparison decides which crow carries the tame / shoulder / fetch / steal mechanics; [plan_hearth_crow_salt.md](plan_hearth_crow_salt.md) needs a revision once decided. |
| Farmer's Hat | `wardrobe:farmers_hat_helmet`, `vinery:straw_hat`, `simplehats:farmerbrim`, `simplehats:summerhat` (Farmer Brim missed on 10-30) | **Drop the item (D8, owner 11-04 / 11-05)**; the XP-on-harvest rule goes on the four pack hats (3.2). Hat list needs owner confirmation **Reversed (owner 2026-11-06, N20): add the H&H Farmer's Hat** (D8 drop withdrawn); the XP-on-harvest rule goes on it and, if kept, the four pack hats. Compare against the four pack hats. **Settled (owner 2026-11-06, N21): keep the Farmer's Hat effect on all four pack hats and on the H&H hat.** The H&H hat is ported. |
| Wooden Bucket | `creativeengineering:wooden_bucket`, `myrtrees:wooden_bucket` | **Drop** |
| Seed Pouch | `quark:seed_pouch` | **Drop (D8, owner note 11-05)**; extend Quark's pouch instead (3.2). Not built |
| Sprinkler | `sliceanddice:sprinkler` (Create machine), `create_things_and_misc:sprinkler` | **Keep, renamed "Field Sprinkler" (D9, owner 11-04)**: passive, non-Create; see 3.1 **Add confirmed (owner 2026-11-06, N20).** |
| Watering Can | TODO `farming/crops #8` | **Built** (4.0): H&H's id, six tiers; shipped |
| Scarecrow | planned dressable scarecrow (`blocks/new #8`) | **Keep the plain block (owner 11-05, D13 answer)**: added to `#alexsmobs:crow_fears` and `#creativedelight:repels_crows`; the dressable one stays its own row. Recipe needs a farmer's hat (swap to Wardrobe's or Vinery's). Not built **Add confirmed (owner 2026-11-06, N20).** **Accepts any farmer's hat (owner 2026-11-06, N21)** (the four pack hats and the H&H hat, through one hat tag), not only the H&H hat. |
| Trough | `quark:feeding_trough` | **Keep (D9, owner 11-04)**: also takes water (babies grow faster) next to Quark's trough. Not built **Implement for evaluation (owner 2026-11-06, N21)** (ported as an eval build next to Quark's trough; keep or drop after in-game testing). **N22 investigation and decision:** water is a mechanic (upstream: a 1000 mB tank that accepts only `minecraft:water` and only while the food slot is empty; fills 4 mB a second in rain; every 80 ticks each baby animal within 10x4x10 blocks ages 16 ticks faster for 10 mB). Restrict the tank to water only (still the check `Fluids.WATER`: no potion, modded or flowing-water variants, no other fluid through pipes or the fluid capability), keep the food mode as is. **Rename to differentiate from Quark's "Feeding Trough": "Water Trough"** (name confirmed by the owner, N22 follow-up). Details in 3.1 Trough. |
| Nest | Incubation nests (egg incubators, take no arbitrary items) | **Keep the 4-slot ground nest as the crow-stash sink only (plan P2; reverses the 10-30 "Drop")**; no chicken laying. Not built **Reversed (owner 2026-11-06, N21): drop nests totally.** No ground nest block, no stash, no nest worldgen, no chicken laying (supersedes plan P2 and the line above). |
| Animal Feed | `farmersdelight:horse_feed` (horses only), `supplementaries:fodder` | not an overlap: **Keep** (3.1) **Not included (owner 2026-11-06, N21).** Breeding foods differ per animal and are documented on the animal items; the per-animal difference matters, so an audit is still needed first (owner). |
| Sink (`basin`), Counter, Drawer | CfB sink / counter, Handcrafted, Redeco, Another Furniture, Storage Delight | **Drop (D10, owner 11-04)** |
| Empty Crate | `quark:crate` ("Empty Crate"), IE crate (name only) | **Keep, renamed "Bottle Crate" (D10, 11-04 investigate, owner note 11-05)**: no overlap found (bottle and egg display crate); see 3.1. Not built **Investigate the Bottle Crate (owner 2026-11-06, N20).** |
| Bottle Racks (9 woods) | Vinery wine racks (one rack family, alcohol.md 10.11) | **Drop** **Investigate (owner 2026-11-06, N20)** (drop on hold): compare with Vinery's wine racks (what each holds, form, woods) before dropping; the Bottle Crate investigation covers the same display need. |
| Trellis (+ bamboo) | `vinery:grapevine_lattice`, Beautify / Decorative Blocks / Redeco lattices (all named "Lattice") | **Keep (D11, owner 11-04)**, possibly merged with Vinery's lattice (check open). Bamboo versions dropped (N17). Name "Trellis" is free **(owner 2026-11-06, N20): add the existing trellis behaviour to hops** (hops climb the trellis as H&H does). **Reversed (owner 2026-11-06, N21): H&H's trellis is not ported.** Hops are ported onto Vinery's trellis (`vinery:grapevine_lattice` family) instead; supersedes the lattice-merge check. |
| Hay Rug, Straw Rug | FD canvas rug (form only) | not an overlap: **Keep** (3.1) **Keep confirmed (owner 2026-11-06, N20).** |
| Mulch | `immersive_weathering:mulch_block` ("Fertile Soil") | **Drop (D11, owner 11-04)** |
| Keg | `brewinandchewin:keg` (Backported Brewin) | **Drop**; the kept recipes are rebuilt as Brewin' keg recipes |
| Cask | Vinery Aging Barrel, Brewin' Heating Cask | **Drop (D12, owner 11-05)**; no Aged / Fine / Reserve tiers, so the 63 `aging` recipes and vintage-scaled values go too |
| Stomping Basin | Vinery Apple Press (apple mash only), Grapevine Pot (grape to wine juice only), Create / IE machines | **Keep as the manual juicer, pending an eval (D12, owner note 11-05)**: berry, cherry, glow berry, melon and oil recipes; apple cider and grape juices dropped; no slime / sponge / sugar or other extra recipes before the eval (owner 2026-11-08, N26-6). Not built **Investigate (owner 2026-11-06, N20)** (the eval above is the investigation); no build until it reports. |
| Jug | IE Jerrycan (10 000 mB, not placeable) | role overlap only (scan): **Keep, renamed "Fluid Jug" (owner 2026-11-07, N24-4)** (3.1) |

**Storage blocks**

Owner 11-06 (N20): compare the textures of the storage rows below once more before the drops stand; the Rope Coil is a new block. Blocked content (2.3): drop all.

| H&H item | Overlaps with | Decision |
|---|---|---|
| Apple, Golden Apple, Golden Carrot Crates | Quark crates, FD apple crate | **Drop** |
| Blueberry, Peanut, Corn Crates; Corn Kernel Bag | FD, Vintage, Corn Delight crates / bag | **Drop** (crops dropped too) |
| Cherry, Red / Green Grape Crates | Vinery bags (crates removed for bags, alcohol.md 10.10) | **Drop** |
| Sweet Berry, Glow Berry Crates | Quark berry sacks, Berry Good baskets | **Drop** |
| Bag of Gunpowder, Bag of Cocoa Beans | Quark sacks | **Drop** |
| Bag of Feathers | `supplementaries:feather_block` | **Drop** |
| Bag of Flour | `create_compressed:wheat_flour_pile` | **Drop** |
| Sugar Cane Bundle, Stick Brush | Quark sugar cane / stick blocks | **Drop** |
| Charcoal Block | `quark:charcoal_block`, `architects_palette:charcoal_block` | **Drop** |
| Bag of Salt, Bag of Sugar | The Salt's Salt Block, Supplementaries sugar cube block | **Drop (D13, owner 11-05)** |
| Cotton Bale | Rustic Cotton Crate (`rusticdelight:cotton_boll_crate`) | **Drop** |
| Brown / Red Mushroom Crate | `fungidelight:brown_mushroom_crate`, `red_mushroom_crate` (missed on 10-30) | **Drop** (D19); crimson / warped fungus crates stay new (3.1) |
| Spool | `creativeengineering:spool` (name), `architects_palette:spool` (decor block; missed on 10-30) | **Drop** (scan, D19; lean drop); owner call **Keep for an evaluation (owner 2026-11-06, N21)** (reverses the drop lean); name clash with `creativeengineering:spool` and `architects_palette:spool` to be resolved in the eval. |
| Rope Coil | Quark / Supplementaries / FD rope (a storage block, not a rope item) | **Keep as its own block, crafted from 9 rope (owner 11-04, `blocks/new #18`)**; not built **Confirmed (owner 2026-11-06, N20): a new block.** |

**Salt blocks**

| H&H item | Overlaps with | Decision |
|---|---|---|
| Salt Block | `salt:salt_block` (The Salt's; melts, dissolves) | **Drop** |
| Salt Lamp | Large Salt Lamp (The Salt's), 17 Vintage salt lamps | **Drop** |
| Polished salt, slab / stairs / wall (8) | Vintage smooth salt, salt bricks, mixed salt bricks | **Drop (D13, owner 11-05)** |
| Lightly / Well / Heavily Licked Salt Block (+ waxed, 7 more) | none | **Keep the mechanic on the pack's block (D13, owner 11-05)**: worn stages as `SaltSandBlock`s in The Salt's colour, a waxed flag, animal lick goal; plan S1 to S7. Not built |
| Salt Drip | none | **Drop the block (D13)**: salt-cave drips become The Salt's buds and cluster (3.2) |

### 2.2 Systems

| H&H system | Pack system | Decision |
|---|---|---|
| Salting (food + salt = Salted, +25 % hunger, -10 % saturation) | The Salt's Salting craft (`#forge:salts`), Seasoning JEI page (now in Creative Delight), the seasonings framework (cap 2); item_merges 2.9 | **Same mechanic. Drop H&H's** (`SaltingRecipe`, `SaltedEffectEvents`, `SaltedFoodTooltipHandler`) |
| Crow entity (spawn, tame, shoulder, fetch, steal, scarecrow repel) | Alex's Mobs crow (`flying_birds`, drops a vanilla feather) | **Mechanics on AM's crow (D2, owner 11-05)**; entity not ported. Comparison and per-feature calls in 3.2 |
| Nests (chickens lay into them, hoppers) | Incubation nests | **Drop the laying**; nest block kept as stash sink (2.1) **Nests dropped totally (owner 2026-11-06, N21).** |
| Pig / rabbit litters (1 in 3 breedings +1 baby) | Fungi Delight truffle litters (extra piglets) | **Add both, boost the truffle effect (D14 / N3, owner 11-04 / 11-05)**; pigs no longer wait for `tweaks #13`. No litter code found yet; not built |
| Cow bottle milking | FD milk bottle (Neapolitan milk merged, pack milk fluids alcohol.md 4.1) | **Add (D14 / N4)**: glass bottle on a cow gives `farmersdelight:milk_bottle`, never an H&H bottle. Not built |
| Goat milking, goat milk bucket "Clears All Effects" | vanilla goats give cow milk | not an overlap: **Keep** (3.2) |
| Chicken plucking and gliding | none | not an overlap: **Keep** (3.2) |
| Alcohol: Drunk effect, wines, Aged / Fine / Reserve tiers | Brewin' Tipsy + Intoxication, Vinery wine aging, `global.alcohol` (Creative Brewing `alcohol_drinks.js`, "Intoxicating Drink" line) | **Drop Drunk and tiers (cask dropped); kept drinks join `global.alcohol`**. Per-drink call in 3.1 (D16, scan) **Compare the Drunk effect with Tipsy / Intoxication (owner 2026-11-06, N20)** before it is dropped (drop on hold); tiers stay dropped. |
| Keg fermenting (juice to wine, milk to cheese, brine, pickling) | Brewin' keg, Vintage pickling jar (`vintagedelight:fermenting`) | **Rebuild kept recipes on the Brewin' keg / Vintage jar**; H&H already ships `brewinandchewin:fermenting` copies for its drinks and cheeses **owner 2026-11-08 (N26-1): brine is added for evaluation; pickling routes stay as written until it reports (5.10).** |
| Cask aging | Vinery aging barrel, wine racks | **Drop (D12)** |
| Hoe enchants Tilling / Harvesting | per-hoe harvest areas (`hoe_harvest/NxN` tags, Creative Delight `HoeHarvestArea`, Quark Simple Harvest) | **Drop both enchantments (N8 / D21, scan)**; Harvesting duplicates the pack areas; Tilling becomes area tilling by the hoe's existing area (3.2). Tilling call still the scan's default **Superseded (owner 2026-11-08, N26-8): Harvesting is dropped; Tilling is not dropped before an eval (N23-12 tiers).** |
| Mulch counts as water for farmland | IW mulch = Fertile Soil | **Drop (D11)** |
| Manure (sheep manure, fertiliser, bricks) | compost framework, fertilisers (IE, S&D, Botania, Create) | **Keep and implement (N9, owner 11-04)**; design in 3.2 |
| Wild crops worldgen | `wild_crops.json`, `gen_crop_worldgen_table.py` | **Drop H&H's grape / cotton / peanut / blueberry features; raspberry Built; wild hops to add (N10)** |
| Season tags (spring / summer / autumn crops) | Serene Seasons tags, Creative Calendar (`CropSeasons`, `cropseasontags.js`) | **Add hops and sunflower through Calendar's crop season tags (N11)** **Widened (owner 2026-11-06, N21): every included crop goes through Calendar's crop season tags** (hops, sunflower, raspberry, corn, peanut, cotton and the rest of the kept crops), not only hops and sunflower. |
| Mums (28 two-colour patches) | `wild_flowers.json`, flower pot matrix | **Add 8 mums as wild flowers (N12, owner 11-04) plus a botanist trade**. Not built |
| Salt caves | The Salt rock salt ores, Vintage beach patch | **Implement swapped (D13, owner note 11-05)**; final cave call still open (section 5) |
| Structures (Corn Maze, Lilliput Lane) | structure framework (Creative Worldgen) | **Add the Corn Maze; investigate Lilliput Lane (N14)**; both through the framework |
| Village farm crops | none in the pack | **Drop (D14, confirmed 11-05)**: H&H adds no village crop of its own |
| Villager trades (farmer buys H&H crops; trader sells) | Dynamic Villager Trades, wandering trader catalog | **Integrate through the pack trade lists, not H&H's code (N15)** |
| Potions (Pungent, Tempting, Bad Omen) | potions system in Creative Brewing | **Keep Pungent / Tempting (D22); add Bad Omen from the crow feather (P5)**. No pack effect matches Pungent, Tempting, Clarity, Prickly or Pinned (Pinned is registered, N23-5; thrown pitchfork only, N25 rules) |
| Water bottles stack (mixin) | stack size policy | **Disable (config)** unless the policy wants it |
| Advancement triggers, structure discovery poll | pack drops all advancements | **Drop** (dead code) |
| *New row:* crops in pots | Botany Pots (installed 10-30; `farming/crops #36`) | Kept crops (raspberry pips, blueberry pips, hops, sunflower seeds) have no entries yet; **add with `farming/crops #36`** |
| *New row:* sausage skewers | pack skewer helper and Barbeque's skewers | **Barbeque's survivor** (2.1) |
| *New row:* pie and feast families | `food_families.py` pie / feast families, Large Meals pot pie family | **Add family rows for the kept pies** (step 12) |

### 2.3 Blocked: 1.20 vanilla content or missing mods

All dropped (**N17, owner 11-04**).

| Items | Needs | Decision |
|---|---|---|
| Cherry Half Cabinet, Cherry Bottle Rack | vanilla cherry wood | Half cabinet: **base on Vinery's `cherry_cabinet`**; rack dropped with the rack family |
| Bamboo Half Cabinet, Bottle Rack, Trellis, Stripped Bamboo Trellis | vanilla bamboo block / planks | **Drop** |
| Palm Half Cabinet, Palm Bottle Rack | Crabber's Delight (not installed) | **Drop** |
| Cherry leaf GLM | `minecraft:cherry_leaves` | **Drop** with the cherry item (B3) |

### 2.4 Overlap with mods in the same batch

| Mod | Overlap | Decision |
|---|---|---|
| **Extra Delight** (`mod-additions #11`, not in the pack) | jams, pickles, cheese, butter, corn, peanut butter, pies, aging, watering can, salt, sausages, hot dogs, croissants, donuts | Settled 2026-10-30 in [extra_delight.md](extra_delight.md) 2.5 (one survivor per row; D4 / D7 / D12 answered there). Butter, corn husk / kernels, mac and cheese, glazed carrots, sunflower seeds, raisins and trail mix are ED's survivors that H&H dishes wait on |
| Barbeque's Delight, Carnivore | sausages, skewers, hot dog, jerky | Barbeque's skewers are the survivor (2.1); loose Raw / Cooked Sausage and Hot Dog stay (no loose pork sausage in the pack) |
| Roll Delight, Large Meals | croissant, biscuits and gravy, chicken pot pie | pot pie dropped (2.1); the rest checked in 3.1 |
| Mineral Delight (archived), Unusual Delight | cleavers mention; salt / sweetener / jar themes | no id overlap |

**Close but not the same (keep, noted):** Trail Mix vs Vintage granola bars (loose mix vs bar); Hot Dog vs `corn_delight:corn_dog`; Street Corn vs `corn_delight:grilled_corn` (a topped dish); Animal Feed vs `supplementaries:fodder` / `horse_feed`; Half Cabinets vs full-height cabinets (FD, Storage Delight, Every Compat); Peanut Butter Cookie / Pie stay new but take Nut Mash (rename to match).

## 3. New

Checked and still new (no pack id or concept found): hops (wild hops, bag), sunflower seeds, corn husk and bundle, five fruit juices, five fruit wines, root beer, moonshine, goat milk and goat cheese, brine, butter, raisins, croissant, scone, cider donut, baked apple, candy corn, raspberry / grape / peanut butter pies, biscuits and gravy, glazed carrots, hops salad, tamale, tree tapper and sap cauldron, fertilizer bag, horseshoe, cleavers, pitchfork, jug, half cabinets, crimson / warped fungus crates, glistering melon / poisonous potato / rotten tomato crates, bag of manure, hay / straw rug, 8 mums, manure family (13), crow feather, the effects. Of the 112 ids new on 10-30, seven turned out covered (jams, Skewered Sausage, Chicken Pot Pie, Pickled Cabbage, mushroom crates, Spool; Mac and Cheese is ED's): they moved to 2.1 (D19).

### 3.1 New items and blocks

Values are hunger / saturation modifier; durations in seconds; "II" = amplifier 1; a food is fast-eating unless noted. Input mapping used everywhere: salt `#c:dusts/salt` to `#forge:salts`; milk to `#forge:milk`; flour to `#forge:flour/wheat`; corn flour to `creativeengineering:cornmeal`; jar to `vintagedelight:empty_mason_jar`; cooking oil / batter / syrup to Rustic's; cherry to `vinery:cherry`; grapes to the Vinery tags; blueberry to `fruitsdelight:blueberry`; peanut to Vintage's; peanut butter to Nut Mash; butter to `#forge:butter` (ED's); corn to `corn_delight:corn`, kernels to `corn_delight:corn_seeds`; H&H keg recipes to the Brewin' keg; pickling to the Vintage jar. **Blocked** = an input with no pack survivor yet. Pack norms: food type medians from `kubejs/exported/food/catalog.json` and [food.md](food.md) 2.1. The H&H vintage-scaled values and all aging variants are out with the cask.

**Drinks.** All `alwaysEdible`, glass bottle back, stack 16 (buckets 1). Wines hold 4 glasses upstream (1000 mB, each glass a full dose). Juice and wine fluids: Create compacting 4 fruit to 500 mB juice, stomping basin, keg pouring 250 mB, Create filling.

| Item | Values / effects | Made by | Decision |
|---|---|---|---|
| Blueberry Juice | 0/0, Luck 30 s | 4 blueberry to 500 mB | **Keep** as a plain `drink` (0/0); D16 |
| Raspberry Juice | 0/0, Prickly 30 s | 4 raspberry | **Keep**; D16 |
| Cherry Juice | 0/0, Haste 30 s | 4 cherry | **Keep (N22: all the new juices are added)** as a plain juice; Cherry Wine stays dropped, so the juice is drink-only |
| Sweet Berry Juice | 0/0, Tempting 60 s | 4 sweet berries | **Keep**; D16 |
| Glow Berry Juice | 0/0, Night Vision 30 s | 4 glow berries | **Keep**; D16 |
| Blueberry / Raspberry / Sweet Berry / Glow Berry / Melon Wine (5) | upstream 1/0.2, Drunk I 180 s 75 % plus two effects | Brewin' keg: 3 fruit + 1000 mB water; H&H keg 250 mB juice, 60 s; Create mixing (no heat) | **Keep as Vinery-style wines (D16, scan; default applies); N22 (owner 2026-11-06): add all five wines for evaluation, and port every wine and juice recipe to Vinery's stations** (Apple Press for the juices where a pressing fits, Grapevine Pot and Fermentation Barrel for the wines, in place of the H&H keg, cask and basin recipes; the Brewin' keg recipes listed here stay as the second route only if the owner wants both). Evaluation: taste each against the Vinery wines for effect, hunger and cost, then keep, merge or drop; nothing is dropped before that. Values as follows: `global.alcohol` (1/0, Intoxication 90 s, Nausea 5 s), keep both drink effects and chances, one drink per 250 mB bottle (no 4 glasses). Drunk not ported. Melon juice fluid: Central Kitchen's. Drop or slow the Create mixing recipes (they skip the keg) |
| Glow Berry Wine effect Clarity | Clarity 60 s + Night Vision | | **Clarity not registered (D23, scan)**; the wine gives Fruits Delight's Brightening (same effect) **Reversed (owner 2026-11-08, N26-9): Clarity is added for evaluation; the wine gives Clarity and Night Vision as upstream, compared with Brightening in the eval.** |
| Root Beer | 1/0.2, Drunk I 60 s 50 %, Night Vision 30 s, Haste 30 s | Brewin' keg: 2 hanging roots + sugar + water | **Soft drink (D16)**: type `drink` 0/0, Night Vision and Haste kept, not in `global.alcohol`. **Owner 2026-11-07 (N24-5): added as the first member of a "Soda" family; fold in / integrate with all other sodas in the pack** (integration report; not built) |
| Moonshine | 1/0.2, Drunk II 180 s, Saturation II 180 s, Strength II 60 s, Slowness II 60 s | keg: 2 corn flour + sugar + water | **Keep like Vodka (D16)**: Tipsy I 180 s, Intoxication 150 s, keep Strength II and Slowness II, **drop Saturation II** (refills hunger against Intoxication). Corn flour to cornmeal **Accepted (owner 2026-11-08, N26-3).** |
| Goat Milk Bottle / Bucket (+ fluid) | bottle clears 1 effect, bucket all | adult goat with bottle / bucket; keg pouring 250 / 1000 mB | **Keep (N5, D17 scan)**: goat milk as a pack fluid (alcohol.md 4.1 rules); keep both textures (near-copies of the vanilla bucket / FD bottle, cream recolour); **Owner 2026-11-07 (N24-2): goat milk is in `#forge:milk` and counts as milk everywhere except cow-milk-specific applications (cheese, ice creams, dairy sweets)**; those take `#creativedelight:cow_milk`. Lists in 5.8 |
| Brine Bottle / Bucket (+ fluid) | not edible | water bottle + salt; keg / mixing | **Drop (D15, scan)**: its only use was pickling, which moves to the Vintage jar (takes salt, no fluid) **Reversed (owner 2026-11-08, N26-1): add the bottle, bucket and fluid for evaluation.** Hooks in 5.10 |

**Jams, pickles, dairy.**

| Item | Values / effects | Made by | Decision |
|---|---|---|---|
| Pickled Carrots, Pickled Potatoes | 4/0.5, Nourishment 60 s / Health Boost I 60 s | keg + brine upstream | **Keep, made in the Vintage pickling jar** (5 of the vegetable + salt, no fluid), `dishes/pickles-and-ferments`; values align with `cooked/veggie` (D18). Effects kept **Brine eval (owner 2026-11-08, N26-1) decides whether these keep salt or take brine.** Salt in the Vintage jar until then |
| Butter | not edible | salt + milk to 2 | **N22: H&H's butter is applied now** (supersedes "blocked on ED"): own item in `#forge:butter`, so Croissant, Scone, Mac and Cheese and the pie crust are no longer blocked. Extra Delight's butter merges into it later |
| Unripe Goat Cheese Wheel, Goat Cheese Wheel (blocks) | not edible | Brewin' keg: 1000 mB goat milk + salt, temperature 4; placed, two random-tick stages at 10 % (about 23 min); wheel gives 4 slices (knife click or cutting board) | **Keep, keg only, no cask (D17, scan)**. Unripe slice texture unused (leftover) |
| Goat Cheese Slice | 3/0.3 | wheel | **Keep; values to the `dairy` type 4/0.8 and into `#forge:cheese` (D17, D18)** so the 13 pack dishes (23 recipes) take it. Feeds Tamale and Street Corn. **N22: crafted the same way as the Brewin' cheeses** (the same keg-and-wheel flow and the same wheel-to-slice cutting); the Brewin' recipes are the template |

**Baked goods, sweets.**

| Item | Values | Made by | Decision |
|---|---|---|---|
| Raisins | 2/0.3 | smelt / smoke / campfire grapes (Vinery tags) | **Keep** (ED dried fruit row, D15 of the ED pass) |
| Raisin Cookie | 2/0.2 | 2 wheat + raisins to 8 | **Keep** |
| Peanut Butter Cookie | 3/0.3 | 2 wheat + peanut butter to 8 | **Keep; input is Nut Mash** (jar remainder); rename to match |
| Croissant | 4/0.5 | FD pot: wheat dough + butter | **Keep; blocked on butter** |
| Raspberry Scone | 6/0.3 (upstream copies the Blueberry Muffin) | shaped: butter, 2 raspberry, sugar, batter | **Keep; align to the pack type (B12, D18)**; blocked on butter |
| Cider Donut | 5/0.5, Nourishment 10 s | FD pot: FD apple cider + oil + salt + egg + sugar to 2 | **Keep** |
| Baked Apple | 5/0.5 (upstream copies the donut) | smelt / smoke / campfire apple | **Keep; align to `cooked/fruit` 4/0.3 (B12, D18)** |
| Candy Corn | 4/0.6 | FD pot: corn kernels + orange dye + syrup + sugar to 4 | **Keep; saturation to the `sweet` norm 0.35 (D18)**; Corn Delight's kernels (owner 2026-11-08, N26-5) |
| Raspberry / Grape / Peanut Butter Pie (+ slices) | slice 3/0.3 | shaped pie; FD `PieBlock`, 4 slices | **Keep** as placeable pies; grape pie takes the Vinery tags; the peanut pie takes whole peanuts upstream; pie / feast family rows needed |
| Charred Marshmallow | 1/0.1, Poison 10 s | held near a campfire 10 s more | **Keep (D7)** on Delightful's `cooked_marshmallow_stick`, type `hazard`; stick back |

**Sausages and dishes.**

| Item | Values | Made by | Decision |
|---|---|---|---|
| Raw Sausage | 2/0.3 | cleaver + raw meat (beef 3, pork 2, mutton 2, chicken 1, rabbit 1) | **Keep** (loose sausage; ED's 5 sausage dishes take it); use the pack `food_type/raw/meat` tag, not `forge:raw_meat` |
| Cooked Sausage | 3/0.5 | smelt / smoke / campfire | **Keep** |
| Hot Dog | 10/0.7 | cooked sausage + bread | **Keep**; in line with `handheld` |
| Biscuits and Gravy | 10/0.5, Nourishment II 180 s | FD pot: cooked sausage + salt + milk + flour, bowl | **Keep; saturation to `meal` norm, Nourishment level I (D18)** |
| Hops Salad | 6/0.2, Instant Health I | 2 hops + oil + salt + bowl | **Keep; align to `salad` (0.6, Regeneration 5 s) (D18)** |
| Tamale | 7/0.6, Nourishment II 180 s | FD pot: cooked meat + vegetable + cheese, corn husk container | **Keep; blocked on corn husk (ED's, Corn Delight cutting)**; tags to the pack food types |
| Street Corn | 10/0.8, Nourishment II 60 s, Haste II 180 s | FD pot: corn on the cob + blaze powder + cheese | **Keep; use `corn_delight:grilled_corn`**; shorten Haste II (longest in the pack; Rustic pancakes give Haste I 30 s); stick remainder needs a source or is dropped (D18) |
| Trail Mix | 5/0.5, Speed 180 s, Regeneration I 60 s | roasted peanuts + raisins + cocoa + seeds | **Keep if raisins stay; Regeneration 60 s is far over pack snacks (D18)** |
| Macaroni and Cheese, Glazed Carrots, Sunflower Seeds | 11/0.5; 10/0.5; 1/0.3 | FD pot (pasta + butter + cheese); pot (carrot + syrup); cutting sunflower | **N22 (owner 2026-11-06): added by H&H first** (supersedes "ED's survivors, not in the H&H port"); every other item Extra Delight would also add that H&H carries comes in the same way. Evaluated afterwards against the pack's state (pasta dishes, carrot dishes, seeds and `#forge:seeds` side effects) |

**N22 (owner 2026-11-06): align every H&H food value to the input-to-output rules already established in the pack** (the food type medians and the recipe-value rules of [food.md](food.md) and [food_families.md](food_families.md)): the D18 default is now the rule, not a default, and applies to every new food and drink. Obvious family matches are added to their families (jam, pickle, pie, pizza, sausage, cheese, cookie, juice, wine, donut and pastry rows in `tools/food_families.py`; step 12). Value outliers (D18; align each to its pack food type and family): Moonshine Saturation II, Goat Cheese Slice, Street Corn, Trail Mix, Hops Salad, Biscuits and Gravy and Pot Pie slice saturation, Candy Corn, the copied Baked Apple / Scone values, Nourishment II on four dishes (the pack uses level I), jams (3/0.3 vs FD 4/0.3). In line with the pack: juices, hot dog, fruit pie slices, pickles, cookies, raw sausage, croissant, charred marshmallow.

**Farm tools and gear** (size S under about 150 lines, M 150 to 500, L over 500).

| Item | Behaviour | Decision |
|---|---|---|
| Tree Tapper (block) + Sap Cauldron | Wall block on a log in `#hearthandharvest:tappable`; every 40 ticks 50 % for 10 mB sap, tank 1000 mB (a bucket per about 6.7 min). Sap bucket on a cauldron, heated: one level per 480 ticks, at level 1 a bottle takes one syrup (a bucket of sap = 1 syrup after 48 s). Create: mixing sap to syrup fluid; `sugar_from_syrup` | **Keep (D4, owner 11-04)** as a second syrup source; output Rustic's `i.syrup`; sap stays "Sap" (N19). Only spruce and dark oak are tappable in the pack: extend `tappable` through `global.tags` (birch is real-world syrup). Fix B6 (cauldron stall). Overlap noted: `myrtrees:tree_tap` is a different tapper (latex). S |
| Fertilizer Bag | 8 uses; sets a crop to max age at once, forces trees, harvests or bone-meals other crops | **Keep; recipe 4 bone meal + 4 manure + the bark / husk / straw binder, 8 uses (owner 2026-11-07, N24-3)**, so 1 bone meal + 1 manure per use (was 1 + 1 for all 8). Still bypasses season rules by direct `setBlock`; routing through the bone-meal event stays a build option. S |
| Animal Feed | right-click any adult animal: love mode | **Keep**; works on modded animals, so check against the mob food overhaul (`tweaks #13`). S **Not included (owner 2026-11-06, N21)** (audit of per-animal breeding foods first). |
| Horseshoe | on a tamed horse-type: Fire Resistance, cactus / berry immunity, soul sand / honey slowdown cancelled; **no speed boost**; throwable (ringer on fences) | **Keep**; store the shoe in the horse's NBT. The hidden Horse Boost is Sugar Cubes' (dropped; Supplementaries' sugar cube already gives horses Speed). HNH-20 wording corrected. **N22 (owner 2026-11-06): the horseshoe keeps a speed boost** (upstream has none, so this is an addition: a movement-speed attribute modifier on the shod horse, +25 % like the dropped Horse Boost, confirmed by the owner) **and a Supplementaries sugar cube fed to the horse still stacks on top of it** (separate effect and modifier, never replaced by the shoe). HNH-20 and a new test cover both. M |
| Flint / Iron / Golden / Diamond / Netherite Cleaver | FD `KnifeItem`; butchering (chicken, cow, pig, sheep, rabbit) removes non-meat drops, +50 % first meat; throwable with Loyalty | **Keep**; netherite via 1.19.2 smithing (diamond cleaver + ingot); tag `#forge:tools/knives` for cutting boards; use the `food_type/raw/meat` tag. No cleaver in the pack. M |
| Pitchfork | 250 durability, throwable; breaking grass or crops replaces all drops with 2 to 4 straw; thrown hit gives Pinned 10 s | **Keep; fix B8 (keep ripe produce).** **N23-5 (owner 2026-11-07) reverses N22 / D24: the thrown pitchfork applies Pinned (melee applies nothing); N25 sets 3 s for players and exemptions for bosses, flying and swimming mobs.** No Scared Still. See Effects table: Scared Still is removed from the pitchfork and from the tiger. M |
| Seed Pouch | 512, hotbar pickup, plants 1x1 to 9x9, dispenser | **Drop; extend Quark's pouch (D8, owner 11-04 / owner note 11-05)**: hotbar pickup handler and 5x5 planting (`Shift Range` in `quark-common.toml` line 529 plus its `configs_edited/` copy). Confirm "5x5 area pickup" means the planting area |
| Farmer's Hat | head item, +1 to 2 XP per ripe crop, costs 1 durability | **Drop the item; rule on four pack hats (D8, owner note 11-05)**: hat list through `global.tags`, hook Quark `SimpleHarvestEvent` plus `BlockEvent.BreakEvent`, tooltip line; no durability cost (decide). Owner confirms the list **Settled (owner 2026-11-06, N21): effect on the four pack hats and the H&H hat.** |
| Jug | 8000 mB portable tank, placeable, holds one fluid | **Keep, renamed "Fluid Jug" (owner 2026-11-07, N24-4)**; role overlap with the IE Jerrycan accepted. M |
| Trough | holds water (babies grow about 20 % faster) or one food stack (auto-breeds adults up to 16 in a 20x8x20 box; schedules manure) | **Keep (D9)**; use `Animal.isFood`. S to M **Eval build (owner 2026-11-06, N21).** **N22, investigated against the source (`TroughBlock`, 131 lines; `TroughBlockEntity`, 197):** one block entity holds either a 1000 mB water tank or one 64-item food slot, never both (the tank refuses water while food is inside, the slot refuses food while water is inside). Water: tank validity is `isSame(Fluids.WATER)`; buckets and bottles go through the shared fluid helper; rain adds 4 mB a second while the food slot is empty; every 80 ticks each baby `AgeableMob` in a 10x4x10 box gains 16 ticks of growth for 10 mB (about 20 % faster); a comparator reads the tank. Food: every 80 ticks, when fewer than `troughBreedingCap` (16) animals stand in a 20x8x20 box, each adult in the 10x4x10 box that can breed and likes the food is put in love and eats one item; manure is scheduled for it. **Decision: the water mechanic stays and the tank accepts only plain water** (reject potion, flowing and modded water fluids and every other fluid; keep the empty-slot rule); **rename "Water Trough"** to separate it from Quark's Feeding Trough (food only, no water). Lang key and JEI name change; **the item gets a tooltip line stating that the water increases baby animal growth (owner, N22 follow-up; HNH-74 checks it)**; the block id stays `hearthandharvest:trough`. Compare against Quark's trough in game and keep or drop after the eval. |
| Field Sprinkler | 10 000 mB tank, infinite next to a water source, farmland moisture 7 in radius 7, fires out, fertiliser every 5 s, redstone pauses | **Keep, renamed (D9)**. M; watch farms with many (scans 675 blocks every 2 s) |
| Scarecrow | decorative, not dressable | **Keep (owner 11-05)**; see 2.1 |
| Bottle Crate | slab-type, 9 slots per half, bottles and eggs, hoppers in from sides and top, out from below | **Keep, named "Bottle Crate" (D10, owner note 11-05)**: drop the tall-wine entries, rebuild the bottle tag from pack ids, flat models for pack items (`CrateBlock` 367 lines, `CrateBlockEntity` 212, renderers). The `BottleCrateRecipe` serializer is dead code |
| Rope Coil | storage block | **Keep** (2.1) |

**Crops and plants.**

| Item | Behaviour | Decision |
|---|---|---|
| Hops, Wild Hops, Bag of Hops | trellis crop (age 0-4, spreads up and sideways, needs farmland under the column base); 2 to 4 hops per harvest; horses eating hops heal; FD `WildCropBlock` wild patch, rarity 1 in 120, forest | **Keep**. Wild hops through `wild_crops.json` / `gen_crop_worldgen_table.py` (N10), seasons summer + autumn (N11). Horse eating needs a mixin (1.19.2 `handleEating`). Beer could take hops (food side) **Hops grow on Vinery's trellis (owner 2026-11-06, N21)** (no H&H trellis); seasons per the widened rule. |
| Trellis | connecting frame, climbable, grows hops, vines, rose bushes | **Keep (D11)**; L (`TrellisBlock` 499 + item 486 + ghost renderer); lattice merge check open **Not ported (owner 2026-11-06, N21)**; hops use Vinery's trellis. |
| Sunflower Seeds | 1/0.3; place a **full-grown** sunflower (dupe loop 1 to 4) | **Keep; fix B7** (growing stage or 1 seed); N11 season: summer + autumn. Beware `#forge:seeds` feeding seed milling and pouches |
| Corn Husk, Corn Husk Bundle | 3 to paper, 4 to FD canvas, 9 to bundle | **Keep; needs a source**: FD cutting board `corn_delight:corn` to 2 kernels + 1 husk (ED's route) **Implement (owner 2026-11-06, N21)** (no longer open on the source). |
| Mums (8) + potted | flower, Instant Health stew; dye only by FD cutting board | **Keep (N12)**: `wild_flowers.json`, flower pot matrix, add a 1-dye crafting recipe if the pack's flowers have one; botanist trade |
| Raspberry, Raspberry Pips, Raspberry Bush | | **Built (4.1)** |
| Blueberry Pips | | **Keep (D3)**; not built |

**Blocks.**

| Item | Behaviour | Decision |
|---|---|---|
| Half Cabinets (11) | FD `CabinetBlock`, half depth, 27 slots | **Keep 9** vanilla woods (oak, spruce, birch, jungle, acacia, dark oak, mangrove, crimson, warped); cherry on Vinery's cabinet; bamboo and palm dropped (N17). S |
| Crimson / Warped Fungus Crates | slab blocks, 9 to 1 | **Keep**; data only; check double-slab drops |
| Glistering Melon, Poisonous Potato, Rotten Tomato Crates | full blocks | **Keep**; data only; no pack storage block for any |
| Hay Rug, Straw Rug | FD `CanvasRugBlock` | **Keep**. S |
| Manure family (Bag of Manure, Manure Block, wet / fired brick, Bricks, Polished + stairs, slabs, walls: 13) | see 3.2 manure | **Keep (N9)** |

**Effects.** JEED text needed for each kept one.

| Effect | Behaviour | Decision |
|---|---|---|
| Pungent | goal on every mob: flees the holder within 8 blocks (12 at II); hostile melee mobs flee too; brain mobs ignore it | **Keep with the B10 fix (D22, accepted 11-08, N26-3)**: scan from effect holders. Close to Neapolitan Harmony; no pack effect equals it |
| Tempting | animals within 8 blocks walk to the holder | **Keep (D22)**; new |
| Clarity | refuses and clears Blindness / Darkness | **Not registered (D23)**: Fruits Delight's Brightening **Reversed (owner 2026-11-08, N26-9): registered for evaluation against Brightening.** |
| Prickly | 1 + level thorns damage every 0.5 s to anything touching the holder (raspberry juice and wine) | **Keep** (S); hits pets and players who touch you |
| Pinned | speed and jump -100 % | **Registered (N23-5, reverses D24 / N22)**; applied only by the thrown pitchfork, 10 s (3 s on players); bosses, flying mobs and swimming mobs exempt (N25) |
| Scared Still (`alexsmobs:fear`) | Alex's Mobs effect; applied by the tiger's reveal and the dragon shovel | **N22 (owner 2026-11-06): remove it from the pitchfork and from the tiger, and the effect must not show in game** (no HUD icon, no tooltip colour entry, no JEI / JEED page, no potion or effect list). **Also removed from the dragon shovel (owner, N22 follow-up)**, so no source remains. Remove the tiger hook through the AM config or a mixin; check what an effect that no longer has any source means for the effects table generator |
| Drunk | escalating levels up to blindness and item drops | **Not ported** (2.2) |
| Horse Boost | Sugar Cubes on a horse | **Drop** (sugar cubes dropped) |
| Potions Pungent / Tempting / Bad Omen | long and strong variants, tipped arrows, buckets, mixers | **Keep Pungent / Tempting (D22). Bad Omen: add the crow feather item and the brew (owner 11-05, plan P5)**: not brewable in the pack today (only Vinery's Villager's Fright wine, 90 s); the Luck step (Bad Omen + fermented spider eye) only if Luck routes are wanted, the four-leaf clover already brews it; drop the long variant (raid on demand) |

### 3.2 New systems and mechanics

| System | Behaviour | Decision |
|---|---|---|
| Crow mechanics on Alex's Mobs' crow | H&H crow: tame by corn kernels (1 in 5), shoulder, fetch, steal shiny items into nests, crop pecking, wariness / trust. AM crow already shoulders, picks up items, deposits into item-frame chests, scatters from pumpkins and the scarecrow tag; its peck is animation only (no crop damage) and it drops a vanilla feather | **D2 (owner 11-05): implement on AM's crow, H&H entity not ported.** Per feature: **crop pecking: owner 11-05, crows damage crops, default on** (a goal resetting crop age over a pack tag; mobGriefing and a config gate; about 90 lines); **shiny stealing: as the plan (C2)**, cheap variant if wanted (about 40 lines drop beside an Incubation nest) or the full steal goal with the ported nest (about 150 + 250 lines); tame crows must not steal; **trust system: deep dive first (owner 11-05)**, mixin on `mobInteract` would change every AM taming, config switch default off; **crow feather item and Bad Omen brew: added (owner 11-05, reverses the earlier drop advice)**. Plan: [plan_hearth_crow_salt.md](plan_hearth_crow_salt.md) (P1 to P7, R1, R2, C2, S1 to S7, tests HNH-48 to HNH-68). Hook: `EntityJoinLevelEvent` adding goals (as `FoxBerryGoals`), mixin only for private state. Not built **(owner 2026-11-06, N21): both crows (AM's with the mechanics, and H&H's) need an in-game evaluation** before the crow question is final; nests are dropped, so theft has no stash (see the plan). **owner 2026-11-08 (N26-2): nests are no longer dropped outright; they are evaluated with the crows.** |
| Scarecrow repels crows | tag only | `crow_fears` and the pack's repels tag (2.1). Not built Needs an in-game eval of the crows (owner 2026-11-06, N21). |
| Salt licking | right-click degrades a salt block (5 % per lick), honeycomb waxes, axe unwaxes, salt restores; last lick pops 1 salt; every animal gets a goal to stay within 12 blocks of salt and lick every 8 min | **Implement (D13, owner 11-05)** on the pack's block: three stage blocks plus waxed variants as `SaltSandBlock`s in the pack's peach palette (quantile map from H&H's texture), dispenser rule, lick-block registry instead of a per-animal scan (S5). Side effect: pens beside salt blocks. Not built |
| Salt caves | 33-block carved caves, salt shell with calcite and tuff, 1.2 % pools, drips; 1 in 24 chunks, Y -40 to 30, dry / desert / badlands / savanna / beach | **Implement swapped (D13, owner note 11-05)**: shell of raw rock salt block (a Salt Block shell would dissolve into the pools), drips become The Salt's buds and cluster (removes `SaltDripBlock`, 204 lines), Forge biome tags, config for rarity and height. Port of the 428-line feature plus a biome modifier. Final cave call still open |
| Chicken plucking and gliding | sneak + use gives 1 feather, 30 s cooldown per chicken; shears lose 1 durability; bare hand 25 % for 1 damage; babies pluckable; vanilla chickens only. Held chicken slows falls | **Keep (D20, scan)**: port as upstream (`EntityInteract`, cooldown in chicken NBT, S). **Karma (N6)**: no karma system exists (only planned `largecontent/karma #1` and a comment hook in `NamedMobs.onDeath`); loss only on a damaging pluck, more for a baby, once karma exists; add the action to the TODO karma list. No karma now **Accepted (owner 2026-11-08, N26-3)**: plucking only (gliding out, N23-10). |
| Goat milking | adult goat with bucket / bottle (replaces vanilla's cow-milk bucket from goats) | **Keep** (3.1 goat milk) |
| Cow bottle milking, rabbit and pig litters | 2.2 | **Add (N3, N4, D14)** |
| Manure | fed adult unnamed animals (not chickens or crows) drop 1 manure 30 to 60 s later (player or trough); sheep after grazing; random pooping off; player Shift+P (1 hunger, 15 s). Fuel 200 ticks; throwable (Pungent + Poison 5 s); `bonemeal_substitutes`; not usable as bone meal and not compostable upstream | **Implement (N9, owner 11-04), design per D26 (scan)**: sources as upstream (mixin on `Sheep.ate`, trough), **drop player pooping**; add a by-hand bone-meal use (vanilla `applyBonemeal`, so season rules apply), composter values (manure 0.85, bag 1.0), IE cloche fertiliser recipe, FD organic compost through NCH; throwing kept; 13 block data entries. M; changelog family "Manure" **Accepted (owner 2026-11-08, N26-4), without Botany Pots: the pack removed every pot fertilizer.** |
| Hoe area tilling | Tilling: sneak-use tills a 3x3 / 5x5 / 7x7 ring by a hard-coded map (modded soils ignored, B9) | **Drop both enchantments (D21, scan; default)**; sneak-use tills the hoe's existing `hoe_harvest/NxN` area through `ToolActions.HOE_TILL` (modded soils work), 1 durability per click, in the `HoeHarvestArea` code. Alternative: Tilling adds +1 radius. S **Superseded (N23-12, then owner 2026-11-08, N26-8): the Tilling enchantment stays for an eval with the N23-12 tiers; no enchantment-free area tilling is built.** |
| Campfire roasting | marshmallow / sausage held within 1 block of a fire or while burning for 10 s | **Add on Delightful's stick (D7)** and the Skewered Sausage idea only via Barbeque's. Not built |
| Farmer's hat XP | 2.1 | hat rule, config per hat (D8) |
| Seed pouch changes | 2.1 | extend Quark's pouch (D8) |
| Wild hops / season tags / mums / villager trades | 2.2 | N10, N11, N12, N15 |
| Corn Maze (custom structure type), Lilliput Lane (one template, 7 chest tables) | | **Add Corn Maze; investigate Lilliput Lane (N14)** through the structure framework (groups, spacing, priority, biome fits), loot through [loot.md](loot.md). Village crop and trade code not ported |
| Latex rename | D&D sap to "Latex" | **N19 decided 11-05; rename confirmed 11-07 (N24-1); merge with `myrtrees:latex` marked for an integration report** (2.1 Sap Bucket) |
| Stomping basin | landing after a 0.3-block drop stomps; 2x2 group forms a multiblock (4 slots, 32 B tank); 15 recipes (8 juices, 3 oils, slime, sponge, sugar) | **Keep as the manual juicer pending an eval (D12, owner note 11-05)**; berry juices need new fluids; apple cider and melon juice fluids exist (Central Kitchen), cooking oil is Create Additions' **No extra recipes before the eval (owner 2026-11-08, N26-6).** |
| Every Compat module (half cabinets for other woods) | | check the pack's Every Compat config (step 22) **Not extended to other woods for now (owner 2026-11-08, N26-7).** |

### 3.3 Upstream issues

| # | Issue | Evidence | Fix |
|---|---|---|---|
| B1 | Per-item enable toggles defined (`Config.put`, `ITEMS`) but never read | `Config.java` 166-172 | drop the map |
| B2 | Item model `lemon_wine` with no item, lang or recipe | `models/item/` | drop |
| B3 | Cherry GLM targets `minecraft:cherry_leaves` (absent on 1.19.2) | `loot_modifiers/cherries_from_cherry_leaves.json` | dropped with the cherry |
| B4 | `DeprecationTooltipHandler` runs on every tooltip for an empty set | `DeprecationTooltipHandler.java` 21-26 | drop the class |
| B5 | Structure discovery polls every player every 40 ticks for an advancement trigger | `StructureDiscoveryEvents.java` | drop (advancements dropped) |
| B6 | Sap cauldron stalls for good if not heated at its first scheduled tick | `SapCauldronBlock.onPlace` / `tick` | reschedule while level > 1 |
| B7 | Sunflower seeds place a full-grown sunflower: 1 becomes up to 4 | `SunflowerSeedItem.useOn`, `cutting/sunflower.json` | growing stage, or 1 seed |
| B8 | Pitchfork replaces all crop drops with straw | `PitchforkEventHandler` | straw from grass / mature wheat only, or added to normal drops |
| B9 | Tilling ignores modded soils | `HoeEnchantmentEvents.TILL_MAP` | use the tool action (D21) |
| B10 | Pungent / Tempting goals on every mob scan 17-block boxes every second | `HearthAndHarvest.onEntityJoin` | scan from effect holders |
| B11 | `randomTicks()` on manure block and bricks with no random tick | `HHModBlocks` 456, 459 | drop the flag |
| B12 | Baked Apple uses the Cider Donut values, Raspberry Scone the Blueberry Muffin values | `HHFoodValues` | pack food type values (D18) |

No load errors: the jar was never loaded in the pack.

## 4. Integration required

Order: 4.0 and 4.1 are built. 4.2 lists the remaining fold-in steps with the pack system each touches and the decision that gates it. D1 (owner 11-05): port only the desired content, so nothing needs an item merge; the keep list is sections 2 and 3.

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
  stack sizes come from `/dumpall` + `/cdevgen` after install.
- Tests HNH-03, HNH-04 (raspberry half), HNH-34 to HNH-37; scratch SQ-HR-01 to SQ-HR-05, CQ-HR-06: all PASSED r1101
  (SQ-HR-04: the item id is `hearthandharvest:raspberry`; SQ-HR-05: 26 biomes in `wild_crops/raspberry`).

### 4.2 Remaining fold-in, in build order

Routing (memory `creative-delight-routing`): food recipes, tags, food values and cooking rules go in a CD tree `development/creativeDelight/src/main/resources/kubejs_startup/hearthandharvest/` (startup) and `kubejs/data/creativedelight/kubejs/hearthandharvest/` (server); engine code (blocks, entities, mixins) stays in the port jar; brewing and alcohol scripts belong to Creative Brewing.

| # | Step | Pack system / doc | Gated by | Decision / state |
|---|---|---|---|---|
| 1 | **Port** the kept content into `backportedHearth`: data components to NBT (`SALTED`, jug and pouch contents), `DeferredHolder` to `RegistryObject`, paths `recipe` to `recipes`, `loot_table` to `loot_tables`, `tags/item` to `tags/items`, result `id` to `item`, `c:` tags to `forge:`, NeoForge biome modifiers and data maps to Forge / pack systems, mixins re-targeted; drop the dropped ids from registries, assets, recipes, loot, tags, lang. `mod_sides.md` row (both sides) | skill `port`, [feature_porting.md](feature_porting.md) | D1 | Started (4.0, 4.1). API rework list: `useItemOn` split back to `use`, `hurtAndBreak` consumer form, `ItemAttributeModifiers` to `getAttributeModifiers`, `ItemTags.MEAT` / `*_FOOD` (1.20.5) absent, `Blocks.SHORT_GRASS` to `GRASS`, NeoForge payloads to a `SimpleChannel` |
| 2 | **Dumps and generators**: install, `/dumpall`, then `/cdevgen` (names, food catalog, JEI coverage, stack sizes, physics, WTHIT icons, crop table); again after 3 and 4 | memory `dumpall-refresh-workflow` | 1 | Done for 4.0 / 4.1 |
| 3 | **Ids and tags**: keys in `item_dict.js` and block / fluid dicts for every kept id; tags through `global.tags` in CD `hearthandharvest/tags.js`: `#forge:crops/hops`, `#forge:butter`, `#forge:cheese` (goat cheese, D17), `#forge:milk` (goat milk: owner call), tappable logs, repels crows, crow fears, hat list, bottle tag, Calendar crop-season tags for hops and sunflower (N11), flower / flower pot tags for mums, storage block tags | `global.tags`, [crops.md](crops.md) | 1 | Partly done (raspberry tags) |
| 4 | **Recipe retargets** in the port's data or CD `recipes.js` via `global.NCH`: every kept recipe that named a dropped item takes the pack survivor or tag (grapes, cherry, blueberry, peanut, cotton, corn, cornmeal, flour, salt, cheese, oil, batter, jam `#forge:jams` / `#forge:jam_jars`, milk, nut mash `#forge:nut_butter`, raspberry `i.t_fruits_raspberries`, syrup `i.syrup`). Remove the Botany Pots / Crabber's recipes whose mods are absent and the bamboo / palm / cherry recipes | [recipes_cooking.md](recipes_cooking.md) | 3 | Not built |
| 5 | **Mob loot**: crow feather replaces AM's vanilla feather via a loot modifier on the AM crow; `/dumpall` loot refresh | [loot.md](loot.md) | D2 | Decided (P5); not built |
| 6 | **Item merges**: none (nothing installed) | [item_merges.md](item_merges.md) | D1 | None (decided D1) |
| 7 | **Potions**: Pungent, Tempting (long / strong), tipped arrows, potion buckets, IE / Create mixer routes; Bad Omen from the crow feather | [potions.md](potions.md), [potion_buckets.md](potion_buckets.md) | D22, P5 | Decided; not built |
| 8 | **Alcohol**: kept wines and Moonshine join `global.alcohol` (Intoxication, Nausea, "Intoxicating Drink"); Root Beer a soft drink; Drunk removed; Create mixing recipes dropped or slowed | [alcohol.md](alcohol.md) 11 | D16 | Default applies; owner confirm Root Beer |
| 9 | **Machines**: keg recipes to the Brewin' keg (wines, moonshine, goat cheese, brine for eval); pickles to the Vintage jar; stomping basin per eval; Create filling / mixing / compacting kept where they make kept items | | D12, D17 | Keg / jar decided; basin pending eval |
| 10 | **Recipes by pack rules** (butter is H&H's now, N22) via `global.NCH`: heatless `_manual_only` for cooking-pot meals, roll and sandwich rules, baking (pies, croissant, scone, donut, cookies) through the oven, S&D and Central Kitchen mirrors, container remainders | [recipes_cooking.md](recipes_cooking.md), [oven.md](oven.md) | 4, butter (ED) | Not built; croissant, scone, tamale wait on ED butter / husk |
| 11 | **Effects**: JEED text for Pungent, Tempting, Prickly, Clarity (eval, N26-9); check the effects-table generator; Brightening compared with Clarity; Scared Still hidden (N22) | [effects.md](effects.md) | D22 to D24 | Decided |
| 12 | **Food types and values**, `tools/food_families.py --check` (jam, pickle, pie, pizza, sausage / skewer, cheese families), stack sizes, cans and jars | [food.md](food.md), [stack_sizes.md](stack_sizes.md), [cans.md](cans.md) | 4 | D18 default: align each outlier |
| 13 | **Compost**: kept crops, pips, husks, manure (0.85), bag (1.0) in the pack framework | [composting.md](composting.md) | N9 | Decided |
| 14 | **Crops, wild crops, seasons**: hops trellis soil rules, WTHIT growth line, wild hops through `wild_crops.json` + `gen_crop_worldgen_table.py`, mums through `wild_flowers.json` and the flower pot matrix, Botany Pots entries (`farming/crops #36`); H&H biome modifiers removed | [crops.md](crops.md), [flowers.md](flowers.md), [flower_pots.md](flower_pots.md) | 2, 3 | N10 to N12 decided; raspberry built |
| 15 | **Worldgen and structures**: Corn Maze and Lilliput Lane through the framework, chest loot; salt caves swapped; ground nests per the crow eval (N26-2) | memory `structure-gen-framework` | 2 | N14 decided; caves final call open |
| 16 | **Names and lang**: clashes "Spool", "Crate", "Counter", "Drawer", "Sink", "Trellis", "Mulch", "Jug"; Nut Mash naming for the peanut butter cookie / pie; "Latex" | [names.md](names.md) | | Bottle Crate and Field Sprinkler already renamed in the decisions |
| 17 | **Mob and block audit**: BlockRunner tier (salt stages, manure), mineable tags, Item Physic, WTHIT lines for crops, jug, sprinkler | [wthit_lines.md](wthit_lines.md) | | Not built |
| 18 | **Tooltips and JEI**: H&H info texts reworded to pack style; JEI categories of kept machines; pouch / hat tooltip lines | [tooltips.md](tooltips.md), [jei_coverage.md](jei_coverage.md) | | Not built |
| 19 | **Placeables / Display Delight**: pies, cheese wheels, jugs, hay rugs | [placeable_food.md](placeable_food.md) | | Not built |
| 20 | **Kitchen furniture**: half cabinets only | [kitchen.md](kitchen.md) | D10 | Decided |
| 21 | **Trades and chests**: farmer buys H&H crops, wandering trader, botanist (mums), through the pack trade lists | [wandering_trader.md](wandering_trader.md) | N15 | Decided; not built |
| 22 | **Every Compat**: half cabinets for other woods; check the pack config | | | Closed for now (N26-7): no other woods |
| 23 | **Config**: `configs_edited/` copy of the port's config (water bottle stacking off, litters, crow options, village crops off) | | | Not built |
| 24 | **Release**: `mod_sides.md` row, changelog bullets per family ("Manure", crow, etc.; only when built), TODO row, plan row | skill `release` | | 4.0 / 4.1 bullets shipped; none in `unreleased.md` |

Ordering: 1 before everything; 2 after 1 and again after 3 and 4; 3 before 4 and 7 to 15; 8 and 9 before 10; 12 after 10; 14 and 15 after 2; 24 last.

## 5. Decision index

Each decision is written in full in the row it concerns (section 2 / 3 / 4). This index only locates them. State: **answered** (owner), **scan default** (raised 2026-11-06, the default applies until answered), **open** (needs an owner call).

| ID | Subject | Row | State |
|---|---|---|---|
| D1 | Port only the desired content | 4 | answered 11-05 |
| D2 | Crow mechanics on AM's crow | 3.2 | answered 11-05; trust system deep dive pending |
| D3 | Blueberry pips | 2.1 crops | answered 11-04 (keep) |
| D4 | Tapper, one syrup (Rustic's), sap name | 2.1 crops, 3.1 tools | answered 11-04 / 11-05 |
| D5, D6 | Chocolate milk; veggie pizza, corn stew, cotton candy | 2.1 | answered 11-04 (drop) |
| D7 | Roasting + Charred Marshmallow | 2.1, 3.1, 3.2 | answered 11-04 |
| D8 | Hat XP, seed pouch | 3.1 tools | answered; hat list settled 11-07 (N23-18: five hats, no durability cost) |
| D9 | Field Sprinkler, trough | 3.1 tools | answered 11-04 |
| D10 | Kitchen blocks, Bottle Crate | 2.1, 3.1 | answered |
| D11 | Trellis, mulch, Tilling | 2.1, 3.2 | answered; lattice merge check void (N21: hops use Vinery's trellis) |
| D12 | Cask dropped; stomping basin | 2.1, 3.2 | cask answered 11-05; **basin pending eval** |
| D13 | Salt decor dropped; licking; caves | 2.1, 3.2 | answered 11-05; caves added 11-07 (N24-11), examined in game for a further eval |
| D14 | Litters, bottle milking, village crops | 2.2 | answered 11-04 / 11-05 |
| N1 to N4, N17 | Crow audit, scarecrows, litters, bottle milking, blocked content | 3.2, 2.3 | done / decided |
| N5 to N9, N16, N18 | Goat cheese, karma, alcohol, hoe enchants, manure, Pungent / Tempting / Bad Omen, overlap refresh | 3.1, 3.2, 2 | investigated 11-06; D15 to D26 raised |
| N10 to N15, N19 | Hops worldgen, season tags, mums, salt caves, structures, trades, Latex | 2.2, 3.2 | decided; not built |
| D15 | Drop brine | 3.1 drinks | **reversed 11-08 (N26-1): add for evaluation** |
| D16 | H&H alcohol under `global.alcohol` | 3.1 drinks | answered 11-08 (N26-3): default accepted |
| D17 | Goat cheese line, `#forge:milk` | 3.1 | answered 11-07 (N24-2): keep; goat milk is milk except in cow-specific dairy |
| D18 | Value outliers | 3.1 | scan default (align) |
| D19 | Items no longer new | 2.1 | answered 11-08 (N26-3): default accepted; Spool and Mac and Cheese stay as N21 / N22 |
| D20 | Chicken plucking, karma | 3.2 | answered 11-08 (N26-3): default accepted |
| D21 | Hoe enchants | 3.2 | answered 11-08 (N26-8): Harvesting dropped; Tilling held for an eval |
| D22 | Pungent / Tempting | 3.1 effects | answered 11-08 (N26-3): default accepted |
| D23, D24 | Clarity to Brightening; Pinned registered (N23-5 reverses D24; N25 rules; no Scared Still) | 3.1 effects | D23 reversed 11-08 (N26-9): Clarity added for evaluation; D24 closed by N23-5 and N25 |
| D25 | Latex clash with `myrtrees:latex` | 2.1 crops | answered 11-07 (N24-1): rename, merge / integration report pending |
| N20 | Owner review notes 11-06: reversals (chocolate milk, cheese, cotton candy, waffle, crow, hat, bottle racks, Drunk) and confirmations | 2.1, 2.2 | decided; comparisons open; not built |
| N22 | Owner notes 11-06, third pass: H&H first, ED later (butter, mac and cheese, glazed carrots, sunflower seeds); all juices and wines in, wine and juice recipes on Vinery's stations; goat cheese as Brewin' cheeses; family matches and value alignment; horseshoe speed boost stacking with sugar cube; no Scared Still (pitchfork, tiger); Water Trough (water only); farmer's hat on all hats | 2.1, 3.1 | decided; not built |
| N23 | Owner notes 11-07, fourth pass: corn husk drops and grinding, mum dyes, Prickly spares pets, pinned source, sugar-cube and horseshoe stacking, luck route, salt lick build, no carry-on, hoe enchantments as tiers, Quark pouch behaviour, lemon wine, cherry leaves, sunflower growth, pitchfork to straw, hat XP, port as a CD member | 5.7 | decided / investigated; not built |
| N21 | Owner notes 11-06 on the plan: corn husk, mac and cheese, hats, scarecrow, trough, animal feed, trellis / hops, spool, crows, nests, seasons | 2.1, 2.2, 3.1, 3.2 | decided; evals open; not built |
| D26 | Manure fertiliser use | 3.2 | answered 11-08 (N26-4): default accepted, no Botany Pots |
| N25 | Owner notes 11-07: Pinned rules (3 s on players; bosses, flying and swimming mobs exempt) | 5.9 | decided; not built |
| N24 | Owner answers 11-07: Latex, goat milk, Fertilizer Bag, Fluid Jug, Soda family, crow and salt calls, caves, stale rows | 5.8 | decided; not built |
| N26 | Owner answers 11-08: brine and Clarity for eval, crow nests in the crow eval, D16 / D19 / D20 / D22 / D26, candy corn, basin extras, half cabinets, hoe enchantments | 5.10 | decided; evals open; not built |

Also open, no id: Spool (evaluation); the cleaver check (undefined: no row says what it was); the brine, Clarity and Tilling evals (N26); the Soda family and Latex integration reports (N24-1, N24-5). Closed 11-07 (N24): Root Beer, Fertilizer Bag, Jug. Settled earlier: Cherry Juice (kept, N22), Corn Husk source (N23-1), trellis (N21).

### 5.7 Owner notes N23 (2026-11-07, fourth pass)

Recorded from the owner's list, with what the investigation found. Nothing is built. Where a note repeats N21 / N22 it is marked "restated".

| # | Note | Finding | Decision / action |
|---|---|---|---|
| N23-1 | Corn husk: random drop from harvesting, milling, crushing; higher chance on cutting; husk grinds to 0.2 cornmeal | Source was open (ED's route, cutting board only). Husk to cornmeal is not an upstream recipe | **Add**: Corn Delight corn gives a husk by random chance on harvest (hand / hoe / Create harvester), Create milling and crushing, with a higher chance on the Farmer's Delight cutting board. Grinding a husk (mortar, mill) gives 0.2 cornmeal, which is chance-based output (0.2 = 20 % chance of 1) unless a fractional-output rule exists; check the pack's `cornmeal` rules. Chances to config |
| N23-2 | Dye recipes for each of the 8 mums | Upstream has dye only by cutting board (3.1, N12) | **Add** one crafting recipe per mum to a matching dye (colour map to write, 8 rows) plus the cutting board route kept; through `global.NCH` |
| N23-3 | Clarity vs Brightening overlap | Clarity clears Blindness / Darkness; Fruits Delight's Brightening gives immunity to both | Same effect: **Clarity stays unregistered (D23)**; Glow Berry Wine gives Brightening. Restated **Reversed 11-08 (N26-9): Clarity added for evaluation.** |
| N23-4 | Prickly must not hit any pets, owner or not | Upstream thorns hit anything touching the holder (3.1) | **Change**: Prickly damage skips every tamed animal (any owner, and untamed-owner-less pets). Needs an entity check on `TamableAnimal.isTame()` / `OwnableEntity` plus Quark / AM pets that are not `TamableAnimal` (list in the test). Players and hostile mobs still take it |
| N23-5 | Pinned: determine the source and add it; Scared Still removed | The only source is the **thrown** pitchfork hitting a target (`ThrownPitchfork`, 200 ticks, level 0). Nothing else applies it; a melee hit does not | N22 had "Pinned not registered, nothing replaces it". **Reversed by this note: register Pinned** (speed and jump -100 %, 10 s) and apply it from the thrown pitchfork only. Scared Still stays removed from the pitchfork, the tiger and the effect lists (restated). Update HNH-73 (hit applies nothing) to: throw applies Pinned 10 s, melee does not |
| N23-6 | Drunk overlap | Upstream Drunk escalates to blindness and item drops; pack has Tipsy + Intoxication in `global.alcohol` | **Not ported, drinks use Tipsy / Intoxication (2.2, N20)**; compare result stays: no overlap left to add. Restated |
| N23-7 | Sugar cube boost and horseshoes stack as horse bonuses; can the sugar cube be told apart (sugar + honey, rename "Sugar Horse Treat")? | Horse Boost is Supplementaries' Sugar Cube (hidden effect), the horseshoe gives no speed (3.1). The Supplementaries cube is also the decorative / feeding item | **Add**: both apply and stack (HNH-81). Differentiating: a separate item is the cleaner route: **Sugar Horse Treat** (sugar + honey), horse-only, gives the boost; the Supplementaries cube keeps its own uses. **Owner 2026-11-07: separate item, the Supplementaries sugar cube keeps its boost.** Sugar Horse Treat (sugar + honey) gives the same boost and stacks with the horseshoe and the cube |
| N23-8 | Luck route: Bad Omen + fermented spider eye; where does the Four-Leaf Clover come from | Bad Omen from the crow feather (P5). The pack already brews Luck from Awkward + Four-Leaf Clover (1.1.03). Buzzier Bees' clover is **an item only** (no block, no loot table or worldgen in the jar); the pack places it in chest loot only | **Add** Bad Omen + fermented spider eye = Luck route (long / strong variants). **Clover (owner 2026-11-07): placeable block and a rare worldgen feature.** The item places a clover block (breaking it drops the item, no tool needed). **Biomes, settled:** flower forest, meadow, Terralith Moonlight Grove, Moonlight Valley, Blooming Plateau, Blooming Valley, Lavender Forest, Lavender Valley, Lush Valley, vanilla sunflower plains, and the pack's flowergrove, flowerhills, flowerridge. **Buzzier Bees' clover patch feature also generates rarely in each of these biomes**. Through the pack worldgen tables (`wild_flowers.json` / biome modifiers); chest loot stays. Not added: Sakura, Valley Clearing, plains, forest |
| N23-9 | Salt lick: 4 rock salt in a square, the existing rock salt texture on the lick shape, slurp sound on lick by player or animal | Plan in [plan_hearth_crow_salt.md](plan_hearth_crow_salt.md) | **Add** the crafting recipe (2x2 rock salt) and texture to the plan; a slurp sound (new sound event, existing vanilla sound as fallback, e.g. a pitched drink or honey sound) played on player use and on the animal lick goal. Tests HNH-48 to HNH-68 get a row |
| N23-10 | No chicken pick-up to glide; Carry On not in the pack | Gliding with a held chicken is upstream. **Carry On is not installed** (only `Carry Cats 2.5`, a different mod; the Carry On tag in barbeques is dropped) | **Drop gliding** (keep plucking, 3.2); nothing to remove for Carry On. Verified in `mods/` |
| N23-11 | Player pooping | Upstream: player Shift+P (1 hunger, 15 s) drops manure; animals drop manure after feeding (3.2) | **Owner 2026-11-07: not added.** Animal manure stays |
| N23-12 | Both hoe enchantments, tiered | Upstream: Harvesting = cube flood-fill of ripe crops, Tilling = ring by a hard-coded map (B9). Pack: per-hoe `hoe_harvest/NxN` areas (Creative Delight `HoeHarvestArea`, Quark Simple Harvest) | **Owner 2026-11-07, reverses D21 for both.** **Harvesting I / II / III set the hoe's harvest area to 3x3 / 4x4 / 5x5** through the existing hoe harvest mechanic (`HoeHarvestArea`); the H&H cube harvest is not ported. A hoe whose own area is already that size or larger is unchanged (the enchant never shrinks it). **Tilling I / II / III let the hoe till 3x3 / 4x4 / 5x5** (sneak-use; even sizes reach one block toward the look direction as `HoeHarvestArea` does); **1 durability per block tilled** (upstream costs 1 per use). Tillable set: everything the hoe tool action tills **including modded dirts, Immersive Weathering / Rich Soil, and end stone** (**tilled end stone (owner 2026-11-07) is its own farmland-type block**, used for ender crops (**which crops: left for a later ender crop audit**): a new block (tilled end stone texture, moisture and trample rules as farmland, ender crops may plant on it); check the pack's existing End crop soils first). Enchants are Java enchantments on 1.19.2 (hoes, max III) **Changed 11-08 (N26-8): Harvesting dropped; Tilling tiers stand as an eval build.** |
| N23-13 | Seed pouch: behaviour of the Quark pouch, from the dropped H&H version | H&H: 512, hotbar pickup, plants 1x1 to 9x9, dispenser | **Add to Quark's pouch**: hotbar pickup of seeds into the pouch, planting area from the `Shift Range`, dispenser planting. **Owner 2026-11-07: planting range 5x5** (`Shift Range` in `quark-common.toml` plus its `configs_edited/` copy). Capacity stays Quark's |
| N23-14 | Lemon wine as a mirror of the other ported wines if it has a texture | Upstream has the item texture, the block bottle texture and a model, but no item, lang or recipe (B2) | **Add** lemon wine with the five-wine pattern (lemon from the pack's lemon, Vinery station route, values by N22). It has a texture, so it is kept |
| N23-15 | Vinery cherry or cherry blossom leaves in place of vanilla cherry leaves | Upstream GLM targets `minecraft:cherry_leaves` (B3); the cherry is Vinery's | **Retarget**: loot modifier for cherries (2 %) on Vinery's cherry leaves (or its cherry blossom leaves) |
| N23-16 | Sunflower growing stage: a green recoloured melon stem; at its last stage it becomes a sunflower | Upstream seeds place a full-grown plant (B7: 1 seed to up to 4) | **Build**: sunflower seeds plant a stem block (green recolour of the melon stem textures, stages 0 to 7); at the final stage it places the sunflower (2-high) as the melon stem places its fruit. Replaces B7. Placeholder texture for now |
| N23-17 | Pitchfork: grass, ferns, mature wheat, oats, rice, hemp give straw | B8 (all crop drops replaced) | **Change**: straw only from grass, ferns, and the mature crops above (checked by crop-age); other crops drop normally; mature wheat keeps wheat and seeds (HNH-73) |
| N23-18 | The five farmer hats give XP on crop harvest and take no durability damage | N21: hat effect on four pack hats plus the H&H hat; upstream costs 1 durability | **Settled**: the five hats; the durability cost is removed (HNH to extend: no damage taken) |
| N23-19 | Fix the rest of the upstream issues per the documented fixes | 3.3: B1, B4, B5 dropped; B6, B9 to B12 fixed as listed; B2, B3, B7, B8 handled above | **Apply** all of 3.3 as written |
| N23-20 | Implement as a member of Creative Delight | Member `backportedHearth` already exists (4.0, 4.1) | Confirmed. Build order stays 4.2; N23 items join their steps (recipes 10, effects 11, hat/pouch 17, potions 7) |

No N23 question is open; the ender crop list waits for the ender crop audit.

### 5.8 Owner notes N24 (2026-11-07, answers to the open calls)

Nothing is built. Each answer is also written into the row it concerns.

| # | Decision | Detail |
|---|---|---|
| N24-1 | **Latex**: D&D's sap is renamed "Latex"; D&D's latex and `myrtrees:latex` are **marked for a merge / integration** | Rename: fluid, bucket, lang (texture recolour). Integration report to write (skill `integration-report`): one latex line or two linked ones, recipe interchange, which id survives. Until it reports, no id change is built. Closes D25 (rename answered, merge pending) |
| N24-2 | **Goat milk counts as milk**, except in cow-milk-specific applications | Rule: goat milk is in `#forge:milk`; cow-specific outputs use a new tag `#creativedelight:cow_milk` (cow milk bucket, FD milk bottle, Neapolitan milk) as their input. Lists below |
| N24-3 | **Fertilizer Bag recipe: 4 bone meal + 4 manure** | Uses stay 8, so 1 bone meal + 1 manure per use. The binder item (bark / husk / straw) stays in the recipe. Updates HNH-70 |
| N24-4 | **Jug renamed "Fluid Jug"**, kept | Keeps the 8000 mB placeable single-fluid tank. Overlap with the IE Jerrycan stays accepted |
| N24-5 | **Root Beer joins a "Soda" family**; fold in / integrate with every other soda in the pack | Root Beer is a soft drink (type `drink`, 0/0, Night Vision and Haste, not in `global.alcohol`) and becomes the first member of the Soda family. Integration task: list every soda-like drink in the pack (vanilla-style bottles, Create / Delight sodas, D&D and Vinery juices that are fizzy), put them under one family tag (`#creativedelight:sodas`) with one value rule and one JEI group. Needs an integration report; not built |
| N24-6 | **Crop pecking defaults on** | Closes crow plan call 1; `mobGriefing`, scarecrow range and `crow_fears` protect as planned |
| N24-7 | **Crow food and breeding use the pack's existing corn** | The pack's corn crop and corn kernels (Corn Delight, `#forge:crops/corn` and the kernel item) are in `#creativedelight:crow_food` now, not "once corn is ported"; AM's seeds stay. Closes crow plan call 2 |
| N24-8 | **Theft: one item, hotbar only, survives without a nest** | Rules and the item list below. Closes crow plan call 3, and the "does theft survive without a stash" question: yes |
| N24-9 | **Dressable scarecrows pinned** | Kept as a separate later item (R3). A later eval decides whether H&H's scarecrow can be dressed. Closes crow plan call 4 |
| N24-10 | **Salt lick wear is slower** | Cow licks once a minute on average; each lick has a 1 in 8 chance to wear a stage. Numbers below. Closes crow plan call 5 |
| N24-11 | **Salt caves are added** | Built through the structure framework as in 3.2, then examined in game for a further eval. Closes crow plan call 6 and D13's final cave call |
| N24-12 | **Stale rows updated** | D8 (five hats, no durability cost, N23-18), D11 (lattice check void, N21), Corn Husk source (N23-1) |

#### Goat milk: what takes it and what does not (N24-2)

The rule is: any recipe that asks for "milk" takes goat milk; a recipe whose result is cow-dairy specific asks for cow milk only. The pack-wide recipe scan (every recipe that names `minecraft:milk_bucket`, `farmersdelight:milk_bottle` or `#forge:milk`) is a build step; the lists are by category, from the H&H rows and the food docs.

| Takes goat milk (milk in general) | Cow milk only (excluded) |
|---|---|
| Butter (salt + milk) | Cheese: the Brewin' keg cheeses and their wheels / slices |
| Biscuits and Gravy | Ice creams: Neapolitan family, Matcha, Salmonberry, Truffle, Adzuki, and any other Comfort ice cream |
| Soups, stews, sauces, mashed potatoes, pancakes, bakes, pies, puddings and cakes that only use milk as the liquid | Dairy sweets: milkshakes, chocolate milk, custards, puddings and sweets whose name or family is dairy |
| Tea / coffee / hot chocolate with milk, smoothies | Cow milk bucket and FD milk bottle items themselves (goat milk stays its own fluid) |
| Cake and cookie doughs, dough fluids | Cream, whipped cream, yoghurt-like items if present |
| Create fluid recipes that take `#forge:milk` | Cow-specific: cow bottle milking output |

Goat cheese itself is **goat milk only** (H&H's keg recipe, 1000 mB goat milk + salt), and is not a cow-cheese input.

#### Theft: rules and the stolen-item list (N24-8)

Rules (replace C6 and the shiny list):

- A wild crow takes **one item** (a count of 1, even from a stack), from the **hotbar** only. Ground items are left alone, except an item a player throws for a tame attempt (C4).
- The item is carried in the main hand and the crow flies away from the nearest player. **It must survive when possible, nest or not:** the crow does not despawn while it carries loot; the held item is saved to NBT and drops when the crow dies, is hit hard (config `crow.dropOnHurt`) or after a timeout (`crow.carryTicks`, default 1200); the dropped item entity has no despawn timer and resists fire, lava and explosions. If the crow is tamed by a thrown stolen item (C4), the item returns to the thrower.
- Never while tame; `crow.stealShinyItems` switch stays.

List `#creativedelight:crow_shiny_items` (generated through `global.tags`, not hand-listed ids): 

| Group | Source |
|---|---|
| Gems | `#forge:gems` (diamond, emerald, amethyst shard, lapis, quartz, pack gems) |
| Ingots | `#forge:ingots` except netherite and ingots from the rare-tier tag |
| Nuggets | `#forge:nuggets` |
| Raw ores | `#forge:raw_materials` |
| Glitter | glowstone dust (redstone is not taken) |
| Special | ender pearl, eye of ender |

**Cut by default (add back if wanted):** tools, armour, weapons, enchanted books, netherite ingot and scrap, totems, anything with enchantments or NBT, and any item the player has named. Reason: with a hotbar-only steal a tool or weapon would be the item in the player's hand.

#### Salt lick wear (N24-10)

- Animal lick interval: **1200 ticks average** (randomised 900 to 1500; first lick staggered 600 to 1800), config `salt.saltLickInterval` = 1200. A cow licks once a minute on average.
- Each animal lick has a **1 in 8 chance** to step one stage (`salt.saltAnimalStageChance` = 0.125). The block has 4 steps (3 worn stages and the pop), so about 32 licks per block per animal: about **32 minutes for one cow, about 6 minutes for five**. (Was a stage per lick; 500 ticks.)
- Player lick chance stays 0.05 per lick.

### 5.9 Owner notes N25 (2026-11-07, Pinned rules)

| # | Note | Rule |
|---|---|---|
| N25-1 | Pitchfork: Pinned exemptions and player duration | Pinned (thrown pitchfork only, N23-5) is **3 s on players**, 10 s on other targets. **Exempt:** bosses, flying mobs, swimming mobs. Exempt targets take the hit damage but get no effect. The exact test for each class (boss tag, flying / swimming check or tag) is picked at build time; prefer entity-type tags built through `global.tags` over per-tick checks. Test HNH-90 |

### 5.10 Owner answers N26 (2026-11-08)

Nothing is built. Each answer is also written into the row it concerns.

| # | Decision | Detail |
|---|---|---|
| N26-1 | **Brine is added for evaluation** (reverses D15) | Bottle, bucket and fluid ported: water bottle + salt, keg and Create routes, salt set to the pack's salt. Kept pickles stay on salt in the Vintage jar until the eval reports. Hooks below |
| N26-2 | **Crow nests are evaluated with the crows** (N21's total drop is on hold) | The nest block joins the crow eval. If ported, nests are more common in villages (`mobs/tweaks #23`). If not, N24-8 theft without a nest stands. Plan section 11 |
| N26-3 | **D16, D19, D20 and D22 defaults accepted** | Moonshine like Vodka and the juice and wine rows; Pickled Cabbage and Chicken Pot Pie dropped; chicken plucking ported, no karma until a karma system exists; Pungent and Tempting kept with the B10 fix |
| N26-4 | **D26 manure default accepted, except Botany Pots** | Pots take no fertilizer in the pack (`kubejs/server_scripts/other/botany_pots.js` removes the recipe type). By-hand bone-meal use, composter values, IE cloche and FD organic compost stand |
| N26-5 | **Candy Corn uses Corn Delight's kernels** | Through `global.NCH` at step 4 |
| N26-6 | **No extra stomping recipes before the basin eval** | Slime, sponge, sugar and any other addition wait for D12's eval |
| N26-7 | **Half cabinets do not extend to other woods for now** | Every Compat module not ported; closes step 22 |
| N26-8 | **Harvesting enchantment dropped; Tilling enchantment not dropped before an eval** | Harvesting I to III (N23-12) are not built; hoe areas stay on `HoeHarvestArea`. Tilling I to III as designed in N23-12 (3x3 / 4x4 / 5x5, 1 durability per block, tilled end stone) are built for the eval |
| N26-9 | **Clarity added for evaluation** (reverses D23) | Registered; Glow Berry Wine gives Clarity and Night Vision as upstream; compared with Fruits Delight's Brightening, which covers the same two effects |

#### Features waiting on brine (N26-1)

| Hook | Where | State | For the eval |
|---|---|---|---|
| Brine Jar | `dishes/jams #8`; art in `textureswapper/mason_jar/implement/new_jar/brine_jar.png` and the mason jar interior texture | Waiting only on a brine bottle in the pack | Build the jar once the bottle exists |
| Pickle family recipe rule | `tools/food_families.py` `preserve/pickle`: "Keg: vegetable + salt / brine" | Live rule, already names brine, and disagrees with H&H's move to the Vintage jar | Settle which station and input the family uses |
| H&H pickles | Upstream feeds carrots, cabbage, beetroots, potatoes and onions from brine; carrots and potatoes are kept on salt | Salt until the eval | Whether kept pickles take brine |
| Extra Delight pickle juice | [extra_delight.md](extra_delight.md): brine is the input, pickle juice the byproduct, keep both | Waits for the Extra Delight merge | Keep brine's id stable for it |
| Port plumbing | Upstream Create filling and mixing, bottle fluid map, short bottle tag | Build step | Fluid dict entry at step 3 |

`items/jars #10` (brine colours) uses the word for each pickle's jar tint and does not depend on the fluid.

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
| HNH-19 | Cleaver: butchering drops and throwing; Pitchfork: thrown hit applies Pinned (melee does not) | |
| HNH-20 | Horseshoe on a tamed horse: fire resistance, no cactus or berry bush damage, normal speed on soul sand and honey; a speed boost while shod (N22) | |
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
| HNH-69 | Tree tapper on a spruce log: a bucket of sap in about 6.7 min; sap cauldron over a campfire gives 1 syrup in about 48 s; a cauldron filled before the fire is lit still works (B6 fix) | |
| HNH-70 | Fertilizer bag: crafted from 4 bone meal + 4 manure + binder; 8 uses, each takes a crop to max age (N24-3) | |
| HNH-71 | Animal feed on an Alex's Mobs animal and a vanilla cow | |
| HNH-72 | Cleaver kills a cow: no leather, about +50 % beef; the same by a thrown cleaver; Loyalty returns it | |
| HNH-73 | Pitchfork: thrown hit applies Pinned 10 s (N23-5), melee applies nothing; pitchfork on ripe wheat keeps the wheat and seeds (B8 fix) |
| HNH-74 | Water Trough (N22), tooltip says water increases baby growth: water makes babies grow faster; food breeds nearby animals up to the cap of 16; only plain water goes in (potion, lava, a modded fluid are refused, also from a pipe); no water while food is inside and the reverse | |
| HNH-75 | Sprinkler: farmland moist within radius 7, fertiliser use, redstone pauses it, endless refill from an adjacent water source | |
| HNH-76 | Hops on a trellis with and without farmland; sunflower seeds plant a growing sunflower, not a full one (B7 fix) | |
| HNH-77 | Chicken plucking: sneak-use gives 1 feather, 30 s cooldown; shears lose 1 durability and do no damage; bare hand sometimes hurts the chicken | |
| HNH-78 | Manure: a fed cow drops manure 30 to 60 s later, a sheep after grazing; none from a named or baby animal; bone-meal use and composter (D26); not a Botany Pots fertiliser (N26-4) | |
| HNH-79 | Sneak-use with an iron hoe on grass and on rich soil tills the hoe's harvest area (D21) | |
| HNH-81 | Horseshoe plus a Supplementaries sugar cube on the same horse: both boosts apply and stack; removing one leaves the other (N22) | |
| HNH-82 | Scared Still: a revealing tiger and the pitchfork no longer apply it; the effect has no icon, tooltip entry or JEI / JEED page (N22; the dragon shovel no longer applies it either) | |
| HNH-83 | Juices and wines: each of the five juices and five wines is craftable at Vinery's stations, with pack-aligned values (N22) | |
| HNH-84 | Goat cheese is made and cut the way the Brewin' cheeses are; the slice is in `#forge:cheese` (N22) | |
| HNH-85 | H&H butter, mac and cheese, glazed carrots, sunflower seeds craft and show in JEI; butter in `#forge:butter` (N22) | |
| HNH-86 | Farmer's hat effect on all four pack hats and the H&H hat; scarecrow takes any of them (N21) | |
| HNH-90 | Pinned exemptions (N25): thrown pitchfork on a player gives 3 s; no Pinned on a boss (ender dragon, wither), a flying mob (bat, phantom, parrot) or a swimming mob (fish, squid, dolphin) | | |
| HNH-80 | Pungent potion: a zombie flees, a villager does not; Tempting potion: cows follow; Glow Berry Wine gives Clarity and Night Vision and clears Blindness (eval, N26-9) | |
| HNH-92 | Brine (N26-1): water bottle + salt gives a brine bottle; bucket, keg and Create routes work; JEI shows them; compare pickling on brine and on salt for the eval | |
| HNH-93 | Tilling I / II / III (N26-8): sneak-use tills 3x3 / 4x4 / 5x5 including modded dirts and end stone, 1 durability per block; log for the eval | |
| HNH-94 | Harvesting enchantment (N26-8) does not exist in JEI, the enchanting table or loot; hoe harvest areas are unchanged | |
| HNH-95 | Clarity (N26-9): refuses and clears Blindness and Darkness; compare with Brightening for the eval | |
| HNH-96 | Crow nests (N26-2): nest generates, crows use it alongside both crows in the eval; if kept, nests appear more often in villages | |

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
- 2026-11-05: owner directed the crow mechanics (on AM's crow), nest stash, scarecrow and salt lick to be implemented; plan written in [plan_hearth_crow_salt.md](plan_hearth_crow_salt.md) (tests HNH-48 to HNH-68), nothing built.
- 2026-11-06: full content scan (H&H only, Extra Delight later) (now merged into sections 2 and 3): food and drink values, non-food behaviour, N18 overlap refresh, N5 / N6 / N7 / N8 / N9 / N16 answered (5.6); horse boost corrected (Sugar Cubes, not the horseshoe); B6 to B12, D15 to D26, tests HNH-69 to HNH-80. Nothing built.
- 2026-11-06: report rewritten around five questions (overlap items and blocks, overlap systems, new items and blocks, new systems, integration required) with each decision in its row; sections 5.1 to 5.6 and 10 folded into those rows, section 5 is now an index. Nothing built.

- 2026-11-06: owner review notes recorded as N20 in the rows of 2.1 / 2.2 (keeps, reversals of D5 / D6 / D8 / D2 drops to compare, investigations: bottle crate, bottle racks, stomping basin, Drunk effect; blocked content stays dropped). Nothing built.
- 2026-11-06: owner notes N22 recorded in the rows (H&H first and complete, ED merges later; butter applied; juices and wines for evaluation on Vinery's stations; goat cheese like Brewin' cheeses; value and family alignment; horseshoe speed boost and sugar cube stacking; Scared Still removed from pitchfork, tiger and dragon shovel and hidden; trough investigated and renamed Water Trough, water-only tank; README row rewritten to the current state). Nothing built.
- 2026-11-06: owner notes N21 added to the rows: corn husk implemented; mac and cheese added (overlap eval); hat effect on the four pack hats and the H&H hat; scarecrow takes any farmer's hat; trough as an eval build; animal feed not included (per-animal breeding-food audit first); H&H trellis not ported, hops on Vinery's trellis; spool kept for eval; both crows need an in-game eval; nests dropped totally; all included crops get Calendar season tags. Nothing built.
- 2026-11-07: owner notes N23 recorded as section 5.7 with investigation results (Pinned source is the thrown pitchfork only, lemon wine has a texture, Carry On is not in the pack, Four-Leaf Clover is item-only in Buzzier Bees). Nothing built.
- 2026-11-08: owner answers N26 recorded in section 5.10 and the rows; brine hooks listed; tests HNH-92 to HNH-96 added. Nothing built.

## Owner notes (scratchpad 2026-11-07)

- Manure buildup (owner 2026-11-07): layers build up in cramped or uncleared pens; manure mobs walk to open space to drop it; crammed mobs (no 3x3 clear space, touching other mobs) drop manure layers instead of items with a chance. Row `blocks/new #22`.
