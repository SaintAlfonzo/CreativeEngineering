# Item merges (total replacements)

Status: BUILT 2026-10-22, scratch server PASSED 2026-10-22 (section 4.1), not tested in game, not deployed. KubeJS,
plus patcherMod for 2.6 (fluid tooltips) and 2.7 (worldgen): a full restart is needed.
Still in game: MG-01, MG-02, MG-04, MG-05, MG-07, MG-13 (2.1 to 2.5), MG-16, MG-17, MG-20 (JEI tooltips), the MG-21
names, and the machine runs of MG-18 / MG-19. Coffee (2.8, built, scratch server PASSED 2026-10-22 for the recipe and effect parts: SQ-CO-01, SQ-CO-02, `tools/food_drinks_probe/food_drinks_probe.js` `/fdtest coffee`, after two fixes in 2.8); in game MG-23 (JEI), MG-24 (drinking), MG-25 (inventory swap, real player only), MG-26 (JEI), MG-27 (JEI), MG-28 (an old bush).

One item is purged and the survivor takes over everything it did: recipes, tags, loot, and any copy that still
reaches a player. Open merges are in [todo/TODO.md](todo/TODO.md) (`drinks #3`, `materials #1-3`, `tweaks #8`,
`cavesandchasmsport #4`).

## 1. How a merge works

| Piece | File | What it does |
|---|---|---|
| Merge list | `kubejs/startup_scripts/_lib/item_merges.js` | `global.itemMerges`: `[purged, survivor]` pairs (item dict keys). Adds each purged id to `global.removedItems` |
| Hide and strip | `removed_items.js` consumers | JEI hides the purged item (`client_scripts/jeichanges.js`); its item tags are stripped and `recipe_scan.js` (`/recipescan`, off at load by default) logs any recipe that still uses or makes it (`[jeiRemove] <id> -> n recipe(s)`) |
| Recipes | `kubejs/server_scripts/other/item_merges.js` (priority -100) | `NCH.replaceInput` / `replaceOutput` over every recipe, then the per-merge removals of duplicates |
| Tags | same file | The survivor joins the tags recipes or mobs read |
| Loot | same file, LootJS | Every table: purged entry -> survivor, same count (`CE/item_merges`) |
| Inventory | same file, `PlayerEvents.inventoryChanged` | A purged stack in a player inventory becomes the survivor, same count. Catches mod code (Neapolitan's milk cauldron), old chests and old worlds |

To add a merge: add the pair to `global.itemMerges`, then remove any duplicate recipes the swap leaves in
`server_scripts/other/item_merges.js`, add tags, and add a section and tests here.

## 2. Merges

| # | Purged | Survivor | Per-merge work |
|---|---|---|---|
| 2.1 | `alexsmobs:banana` | `neapolitan:banana` | Leaf drops stay off (`config/alexsmobs.toml` `bananasDropFromLeaves = false`, unchanged). Neapolitan's banana joins `#forge:crops/banana`, so `#alexsmobs:bananas` (Alex's Mobs' banana-eating mobs) holds only it. Trader elephant chest: banana -> Neapolitan's (loot swap). Eating Neapolitan's banana gives an Alex's Mobs banana peel, as Alex's did (sopa de macaco keeps its source). Compost row removed |
| 2.2 | `neapolitan:milk_bottle` | `farmersdelight:milk_bottle` | Abnormals Delight turns off Farmer's Delight's bucket and bottle recipes while Neapolitan is loaded, so Neapolitan's stay: bucket + 3 glass bottles -> 3 bottles (output swapped) and bucket + 3 `#forge:bottles/milk` -> milk bucket (FD's bottle joins that tag). Removed: Create spout / drain compat for Neapolitan's bottle and Delightful's `matcha_latte_neapolitan` (FD copies exist). Milk cauldron: taking milk out with a bottle gives Neapolitan's bottle in code; the inventory swap turns it into FD's |
| 2.3 | `neapolitan:chocolate_cake` | `createaddition:chocolate_cake` | Keeps CC&A's texture. Abnormals Delight's 7 slices -> cake makes CC&A's cake. Since the baking overhaul (2026-10-22, [oven.md](oven.md) 2.6) every other recipe is gone (CC&A's own spout, Neapolitan's crafting); baking.js makes CC&A's by hand, in the oven and by a Spout (baked cake base + 500 mB chocolate; ids `chocolate_cake_manual_only`, `chocolate_cake`, `createfilling/chocolate_cake_spout`, [oven.md](oven.md) OV-33). A bite from the placed cake counts for Spice of Life as the Abnormals Delight slice ([food.md](food.md) 4.2 finding 5). Knife on the cutting board: CC&A's cake -> 7 Abnormals Delight chocolate slices (NCH `fdCutting`, id `chocolate_cake`). Knife on the placed cake: CC&A's block joins `abnormals_delight:drops_chocolate_cake_slice`. Compost row moved to CC&A's cake |
| 2.4 | `moredelight:diced_potatoes` | `rusticdelight:potato_slices` | Rustic Delight's potato -> 2 slices stays; More Delight's potato -> 1 diced removed. Rustic slices join `#forge:sliced_potato`, which More Delight's dishes (diced potatoes with beef / chicken / egg and tomato / porkchop, mashed potatoes, potato salad) read |
| 2.5 | `decorative_blocks:rocky_dirt` | `twigs:rocky_dirt` | Decorative Blocks' 4 dirt -> 1 and 1 -> 4 dirt recipes removed. Dave's `davebuildingmod:rocky_dirt` (and slab) is a separate block, renamed Pebble Dirt / Pebble Dirt Slab. Recipes swapped: Pebble Dirt is Twigs' pattern (dirt and pebbles crossed in a 2x2) -> 4, Rocky Dirt is Dave's shapeless 2 dirt + 2 cobblestone -> 4; coarse dirt in place of dirt gives 8 in both. Originals (`davebuildingmod:rec_rocky_dirt`, `twigs:rocky_dirt`) removed |
| 2.8 | Farmer's Respite's coffee: berries, beans, sack, wild bush, three drinks | Rustic Delight's | See 2.8: 7 pairs, 15 recipes removed, Rustic's coffees give Caffeinated |

Related rename (not a merge): Vinery's `vinery:apple_cider` is "Hard Cider" (`client_scripts/namechanges.js`), and
the apple juice tooltip says "Apple Wine and Hard Cider". FD's and Farmer's Respite's ciders keep "Apple Cider".

### 2.6 Apple cider chain (no purge, built, scratch server PASSED: SQ-MG-00 to SQ-MG-03)

All three non-alcoholic ciders stay: `farmersdelight:apple_cider` (base), `farmersrespite:long_apple_cider` and
`strong_apple_cider`. They run like a potion: one base, a lengthened tier (milk, the cider's "redstone") and an
amplified tier (honey, its "glowstone"), each with a Central Kitchen fluid (`f.apple_cider`, `f.long_apple_cider`,
`f.strong_apple_cider`; tags `#forge:fluid/apple_cider`, `/long_apple_cider`, `/strong_apple_cider`).

| Machine | Recipe | Amounts | Where |
|---|---|---|---|
| FD cooking pot, FR kettle | unchanged (kettle: cider + `#forge:milk` -> long, cider + honey bottle -> strong) | 1 bottle | the mods |
| Create mixer | base: CCK's 2 apples + sugar, heated. Long: cider fluid + FD milk bottle, heated. Strong: cider fluid + honey bottle, heated | base 250 mB; tiers 1000 mB in and out (Automatic Brewing's batch) | CCK; `cooking.js` "Apple cider" (`createmixing/long_apple_cider`, `strong_apple_cider`) |
| IE Mixer | base: 250 mB water + 2 apples + sugar. Long / strong: cider + the same reagent, 6400 RF/t (IE's potion energy) | base 250 mB; tiers 1500 mB (the IE Mixer potion batch) | `cooking.js` (`iemixer/apple_cider`, `long_apple_cider`, `strong_apple_cider`), new helper `NCH.ieMixer` |
| IE Bottling Machine | each fluid -> its bottle | 250 mB | `cooking.js` |
| Create spout / drain | CCK's filling and emptying for all three | 250 mB | CCK |

The tier recipes are plain `create:mixing` and `immersiveengineering:mixer` recipes, so JEI shows them on Create's
Mixing page and IE's Mixer page, not on Automatic Brewing or Automated Potion Mixing (that page takes only
`create:potion` outputs). Removed: `tmted:mixer/apple_cider` (made the dead `tmted:apple_cider`) and
`tmted:bottling/apple_cider` (read the empty `#forge:apple_cider`).

Names and tooltips follow potions: all three items and fluids are "Apple Cider", as every tier of a potion shares
its name; the tier shows in the effect line. Items already list their effect (FD `DrinkableItem`, effect tooltip on):
"Absorption (1:00)", long "Absorption (1:30)", strong "Absorption II (0:30)". Fluids now do too: patcherMod
`JeiFluidHelperTooltipMixin` + `compat/DrinkFluidEffects` put the same lines under the name of every JEI fluid
tooltip (ingredient list and all categories). The fluid's effects are its item's, read from Create's emptying
recipes on the client, so the teas and other drink fluids with an emptying recipe get them as well. A Create potion
fluid is skipped (Create already lists it). The honey bottle comes back from the Create mixer (basin remainder); the
IE Mixer uses it up, as it does every item reagent.

### 2.7 Permafrost -> Froststone (no purge, built, scratch server PASSED: SQ-MG-04, SQ-MG-06, SQ-MG-07)

Immersive Weathering's permafrost is the world's only permafrost. Quark's `quark:permafrost` family is renamed
Froststone (`namechanges.js`: Froststone, Froststone Slab / Stairs / Vertical Slab / Wall, Froststone Bricks,
Froststone Brick Slab / Stairs / Vertical Slab / Wall; dict keys renamed `i.froststone*`).

- Worldgen: Quark's frozen peaks cave style (y 105-140, `PermafrostModule`) now lays IW's permafrost on floors,
  ceilings and the floor pillars; walls stay packed ice. patcherMod `QuarkPermafrostStyleMixin` swaps the style's
  floor / ceiling state at the first fill (Quark sets them while blocks register, before IW's block is sure to
  exist). Switch: `[worldgen.systems] permafrostMerge` (default true; off restores Quark's block). New chunks only.
  IW's own surface patches in icy biomes are unchanged. No structure in any jar uses Quark's block.
- Recipe: Froststone gets IW's permafrost craft with stone in place of dirt, shapeless 2 packed ice + 2 stone -> 2
  (`item_merges.js`, id `froststone`). Quark's bricks, slabs, stairs, walls and stonecutting stay as they were.
- Block Runner: Quark's block stays a "natural smooth" tier, IW's a "loose ground" tier, so running speed in those
  caves changes slightly.

### 2.8 Coffee (Farmer's Respite's purged, built 2026-10-22, scratch server PASSED 2026-10-22: SQ-CO-01, SQ-CO-02)

Rustic Delight's coffee is the only one (user decision 2026-10-22, settles the brief's D2: Farmer's Respite's crop and
drinks go, Rustic's drinks give Caffeinated instead of Speed and Haste, every other coffee craft takes Rustic's beans).

| Purged | Survivor | Why this survivor |
|---|---|---|
| `farmersrespite:coffee_berries` | `rusticdelight:coffee_beans` | both the raw harvest |
| `farmersrespite:coffee_beans` | `rusticdelight:roasted_coffee_beans` | both the processed bean every coffee food reads (also FR's seed: the FR bush can no longer be planted) |
| `farmersrespite:coffee_beans_sack` | `rusticdelight:coffee_beans_bag` | storage block |
| `farmersrespite:wild_coffee_bush` | `rusticdelight:wild_coffee` | wild plant (FR's Basalt Deltas spawn was already off, `wild_crops.json`) |
| `farmersrespite:coffee`, `long_coffee` | `rusticdelight:coffee` | base drink |
| `farmersrespite:strong_coffee` | `rusticdelight:dark_coffee` | Rustic's level II coffee |

- Recipes after the swap: FR coffee cake (grid, CCK mixing and dough craft, pack baking.js) and blazing chili take
  roasted beans; gambler's tea (kettle) and Canned Coffee Berries take raw beans.
- Removed in `server_scripts/other/item_merges.js` (the swap would leave them as loops, duplicates or a kettle
  route to Rustic's coffee): `farmersrespite:brewing/{coffee,long_coffee,strong_coffee}`,
  `create_central_kitchen:{filling,emptying}/{coffee,long_coffee,strong_coffee}`, `create_central_kitchen:mixing/coffee`,
  `create_central_kitchen:milling/coffee_berries`, `farmersrespite:cutting/coffee_berries`, `farmersrespite:coffee_beans`
  (sack -> 9), `farmersrespite:coffee_beans_sack`, `minecraft:red_dye` (FR's berries -> red dye; no other jar uses
  that id). CCK's coffee fluid stays registered with no recipe.
- Effects: `startup_scripts/cooking/food_values.js` `global.foodEffectSwaps` removes Speed and Haste from Rustic's five
  coffees and adds `farmersrespite:caffeinated` at Speed's duration and level (coffee 1800 t, milk 1200, honey and
  chocolate 900, dark 900 at II). Hunger, saturation, always-edible and the milk / chocolate coffee cures are
  unchanged. Caffeinated: +10 % speed and +20 % attack speed per level, resets the phantom timer.
- Compost: the coffee berries row removed (Rustic's beans already 0.65). Placed FR bushes drop Rustic's items (loot swap).
- Fixed in the scratch run (2026-10-22): gambler's tea, Canned Coffee Berries and a mortar craft still named Respite's
  coffee berries. KubeJS's swap skips script-added recipes, so item merges pass `includeAdded` to NCH `replaceInput` /
  `replaceOutput` (the two can recipes, [pack_tooling.md](pack_tooling.md) 2.2). Respite's kettle has no KubeJS
  schema, so gambler's tea is removed and rebuilt with `R.frBrewing` on Rustic's beans. `mortar.js` skips catalogue
  entries naming a purged item (`[mortar] 6 of 7`).

## 3. Known limits

- A Neapolitan milk bottle can't be poured back into a milk cauldron (only Neapolitan's own bottle fits, and it no
  longer exists in inventories). A bucket still fills it.
- The inventory swap covers the player inventory, not other containers. A purged item in a chest placed before the
  merge turns over when a player picks it up.
- `kubejs/generation_scripts/food/food_types.json` still lists `diced_potatoes`; its tags are stripped at load.
  `tools/gen_stack_sizes.py` still lists `neapolitan:chocolate_cake`. Both are harmless; clean up on the next run.
  Coffee likewise: `food_types.json` (`coffee_berries`), `stack_sizes.json` (the three FR coffees), `mortar_catalog.json`
  (CCK's berry milling), `can_types.json` (Canned Coffee Berries' source key) still name purged items.
- Canned Coffee Berries keeps its name, values and effects (Wither, Caffeinated) but is filled from Rustic's raw beans,
  which are not a food themselves. Rename or drop the can if that reads wrong.
- FR coffee bushes already in a world stay and still grow (CCK harvester compat included); they drop Rustic's items.

## 4. Tests

After a full restart, `logs/latest.log` first: `[itemMerges] 12 merges`, `[food values] 5 effect swaps` with no
`effect swap:` warning, no `Error occurred while handling event`
from `item_merges.js` or `cooking.js`, no `ieMixer` warning, no mixin apply error for `JeiFluidHelperTooltipMixin` or
`QuarkPermafrostStyleMixin`, and no `[jeiRemove]` lines for the purged ids.

| ID | Test | Expected |
|---|---|---|
| MG-01 | JEI: search banana, milk bottle, chocolate cake, diced, rocky dirt | None of the five purged items show |
| MG-02 | `/give @s alexsmobs:banana 5` | Becomes 5 Neapolitan bananas |
| MG-03 | Eat a Neapolitan banana | Banana peel in the inventory |
| MG-04 | Feed / tame a capuchin monkey with a Neapolitan banana | Accepted |
| MG-05 | Break jungle leaves (20+) | No bananas |
| MG-06 | JEI uses of milk bucket; craft bucket + 3 glass bottles | 3 Farmer's Delight milk bottles; 3 FD bottles + bucket -> milk bucket |
| MG-07 | Milk cauldron (bucket in), take with a glass bottle | FD milk bottle |
| MG-08 | Spout milk into a glass bottle; matcha latte in the kettle with an FD bottle | FD bottle; latte brews, one recipe only in JEI |
| MG-09 | JEI chocolate cake | CC&A cake with: baked base + frosting + 2 chocolate bars by hand, 7 slices, oven, spout (baked base + 500 mB chocolate, the pack's recipe only) |
| MG-10 | Knife on CC&A cake on a cutting board; knife on the placed cake | 7 chocolate slices; one slice per use |
| MG-11 | Knife on a potato on a cutting board | 2 Rustic Delight potato slices, no diced potatoes recipe |
| MG-12 | Cooking pot: diced potatoes with beef using potato slices | Cooks |
| MG-13 | Hover Vinery cider and apple juice | "Hard Cider"; tooltip "Apple Wine and Hard Cider" |
| MG-14 | JEI Pebble Dirt and Rocky Dirt | Pebble Dirt (Dave's, slab "Pebble Dirt Slab"): 2 dirt + 2 Twigs pebbles crossed -> 4, coarse dirt -> 8. Rocky Dirt (Twigs'): 2 dirt + 2 cobblestone -> 4, coarse dirt -> 8. One Rocky Dirt only, no rocky dirt -> dirt recipe |
| MG-15 | JEI uses of the Apple Cider fluid | Create Mixing page: + milk bottle -> 1000 mB long, + honey bottle -> 1000 mB strong, both heated. IE Mixer page: same at 1500 mB. None on Automatic Brewing or Automated Potion Mixing |
| MG-16 | Hover the three cider fluids in JEI (ingredient list and a Mixing recipe) | All "Apple Cider"; under the name "Absorption (1:00)", "Absorption (1:30)", "Absorption II (0:30)" in blue |
| MG-17 | Hover the three cider items | All "Apple Cider" with the same three effect lines |
| MG-18 | Heated mixer: 1000 mB cider + 1 milk bottle; then + 1 honey bottle on fresh cider | 1000 mB long cider, glass bottle left in the basin; 1000 mB strong cider |
| MG-19 | IE Mixer: 1500 mB cider + milk bottle; IE Mixer 250 mB water + 2 apples + sugar; Bottling Machine each cider | 1500 mB long cider; 250 mB cider; one bottle per 250 mB, right tier |
| MG-20 | Hover a green tea fluid in JEI | Its effect line under the name (side effect of MG-16's hook) |
| MG-21 | JEI search froststone; craft 2 packed ice + 2 stone | 10 Froststone blocks named as in 2.7; 2 Froststone. No "Permafrost" left on Quark's blocks |
| MG-22 | New frozen peaks chunks: caves at y 105-140 (`/locate biome minecraft:frozen_peaks`, spectator) | IW permafrost floors, ceilings and pillars, packed ice walls, no Froststone |
| MG-23 | JEI: search coffee (recipe removals: scratch server PASSED 2026-10-22, SQ-CO-01) | No Farmer's Respite coffee items or kettle coffee recipes; Rustic's Coffee, Milk / Honey / Chocolate / Dark Coffee, beans, bag, wild coffee all there |
| MG-24 | (effect swap: scratch server PASSED 2026-10-22, SQ-CO-02) Hover Rustic's Coffee, Milk Coffee, Honey Coffee, Chocolate Coffee, Dark Coffee; drink each | Caffeinated 1:30, 1:00, 0:45, 0:45, Caffeinated II 0:45; no Speed or Haste. Milk / chocolate coffee still clear one effect |
| MG-25 | `/give @s farmersrespite:coffee_berries 3`, `coffee_beans 3`, `strong_coffee 1`, `long_coffee 1` | 3 Coffee Beans, 3 Roasted Coffee Beans, 1 Dark Coffee, 1 Coffee (Rustic's) |
| MG-26 | (inputs: scratch server PASSED 2026-10-22, SQ-CO-01, coffee cake takes roasted beans) JEI recipes for Coffee Cake and Blazing Chili; uses of Coffee Beans | Cake and chili take Roasted Coffee Beans; Gambler's Tea (kettle) and Canned Coffee Berries take Coffee Beans; no beans -> red dye |
| MG-27 | JEI uses of Rustic's Coffee and Dark Coffee in a Spout / Item Drain | No Central Kitchen coffee fluid recipe |
| MG-28 | Break a placed FR coffee bush at stage 2 (old world, or `/setblock`) | Rustic's Coffee Beans drop, no FR berries |

### 4.1 Scratch server results (2026-10-22)

Probes `tools/item_merges_probe/merge_probe.js` (`/mgtest`, `/mgpf`) and `merge_core_probe.js` (`/mgcore`), patcherMod
1.1.01 md5 37bb7049, KubeJS working tree of 2026-10-22. Every check passed:

- Load: `[itemMerges] 5 merges`, no mixin apply error, no script error. The `[jeiRemove]` lines the scan printed for
  four purged ids were false positives: `recipe_scan.js` runs in the same recipe event as `item_merges.js`, before
  KubeJS applies `replaceInput` / `replaceOutput`. It now skips merged ids; the live check below covers them.
- MG-03, MG-06, MG-08 to MG-12, MG-14 (recipe and tag parts, 33 of 33): purged ids hidden and without tags, no loaded
  recipe names a purged item, the 9 duplicate recipes removed, eating a banana leaves a peel, elephant chests roll
  only Neapolitan bananas (300 rolls), FD milk bottles in and out of the bucket recipes, knife on the CC&A cake gives
  7 slices and 7 slices give the cake back, Rustic potato slices tagged, Pebble Dirt 4 / 8, Rocky Dirt 4 / 8, slab 6.
- MG-15 and MG-19 (recipe parts): long and strong cider Create mixing, three IE mixer recipes, one bottling recipe per
  cider; tmted's two removed, Central Kitchen's base mixing kept; fluid tags hold one fluid each.
- MG-21 (recipe part): 2 packed ice + 2 stone gives 2 `quark:permafrost` (Froststone).
- MG-22: three frozen peaks sites in a new normal world, 3x3 chunks at y 100-145: Quark permafrost 0 at all three,
  Immersive Weathering permafrost 6335, 1303, 1612. With `permafrostMerge = false` (new world, restart): Quark
  permafrost 1073, Immersive Weathering 0.
- The Winemaker pool has no apple sapling; the other Fruits Delight saplings are still there.
- Coffee (2.8, second bulk run 2026-10-22, `/fdtest coffee`): SQ-CO-01 PASS after the two fixes in 2.8 (`[itemMerges]
  12 merges`, the 7 FR coffee ids purged, no recipe names a purged coffee item, the 9 removed ids gone); SQ-CO-02 PASS
  (Rustic's coffee gives Caffeinated, no Speed or Haste). MG-23 (JEI), MG-24 (drinking), MG-25, MG-26 (JEI), MG-27,
  MG-28 stay in game.

