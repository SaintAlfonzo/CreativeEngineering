# Item merges (total replacements)

Status: BUILT 2026-10-22, not tested, not deployed. KubeJS, plus patcherMod for 2.6 (fluid tooltips) and 2.7
(worldgen): a full restart is needed.
2.1 to 2.5: built, scratch test queued ([testing/scratch_queue.md](testing/scratch_queue.md) SQ-MG-10 to SQ-MG-15,
probe `tools/item_merges_probe/merge_core_probe.js`); MG-01, MG-02, MG-04, MG-05, MG-07, MG-13 stay in-game checks.

One item is purged and the survivor takes over everything it did: recipes, tags, loot, and any copy that still
reaches a player. Open merges are in [todo/TODO.md](todo/TODO.md) (`drinks #3`, `materials #1-3`, `tweaks #8`,
`cavesandchasmsport #4`).

## 1. How a merge works

| Piece | File | What it does |
|---|---|---|
| Merge list | `kubejs/startup_scripts/_lib/item_merges.js` | `global.itemMerges`: `[purged, survivor]` pairs (item dict keys). Adds each purged id to `global.removedItems` |
| Hide and strip | `removed_items.js` consumers | JEI hides the purged item (`client_scripts/jeichanges.js`); its item tags are stripped and `recipe_scan.js` logs any recipe that still names it (`[jeiRemove] <id> -> n recipe(s)`) |
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
| 2.3 | `neapolitan:chocolate_cake` | `createaddition:chocolate_cake` | Keeps CC&A's texture. Abnormals Delight's 7 slices -> cake makes CC&A's cake. Since the baking overhaul (2026-10-22, [oven.md](oven.md) 2.6) every other recipe is gone (spout, Neapolitan's crafting); baking.js makes CC&A's by hand and in the oven (ids `chocolate_cake_manual_only`, `chocolate_cake`). Knife on the cutting board: CC&A's cake -> 7 Abnormals Delight chocolate slices (NCH `fdCutting`, id `chocolate_cake`). Knife on the placed cake: CC&A's block joins `abnormals_delight:drops_chocolate_cake_slice`. Compost row moved to CC&A's cake |
| 2.4 | `moredelight:diced_potatoes` | `rusticdelight:potato_slices` | Rustic Delight's potato -> 2 slices stays; More Delight's potato -> 1 diced removed. Rustic slices join `#forge:sliced_potatoes`, which More Delight's dishes (diced potatoes with beef / chicken / egg and tomato / porkchop, mashed potatoes, potato salad) read |
| 2.5 | `decorative_blocks:rocky_dirt` | `twigs:rocky_dirt` | Decorative Blocks' 4 dirt -> 1 and 1 -> 4 dirt recipes removed. Dave's `davebuildingmod:rocky_dirt` (and slab) is a separate block, renamed Pebble Dirt / Pebble Dirt Slab. Recipes swapped: Pebble Dirt is Twigs' pattern (dirt and pebbles crossed in a 2x2) -> 4, Rocky Dirt is Dave's shapeless 2 dirt + 2 cobblestone -> 4; coarse dirt in place of dirt gives 8 in both. Originals (`davebuildingmod:rec_rocky_dirt`, `twigs:rocky_dirt`) removed |

Related rename (not a merge): Vinery's `vinery:apple_cider` is "Hard Cider" (`client_scripts/namechanges.js`), and
the apple juice tooltip says "Apple Wine and Hard Cider". FD's and Farmer's Respite's ciders keep "Apple Cider".

### 2.6 Apple cider chain (no purge, built, scratch test queued: SQ-MG-00 to SQ-MG-03)

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

### 2.7 Permafrost -> Froststone (no purge, built, scratch test queued: SQ-MG-04, SQ-MG-06, SQ-MG-07)

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

## 3. Known limits

- A Neapolitan milk bottle can't be poured back into a milk cauldron (only Neapolitan's own bottle fits, and it no
  longer exists in inventories). A bucket still fills it.
- The inventory swap covers the player inventory, not other containers. A purged item in a chest placed before the
  merge turns over when a player picks it up.
- `kubejs/generation_scripts/food/food_types.json` still lists `diced_potatoes`; its tags are stripped at load.
  `tools/gen_stack_sizes.py` still lists `neapolitan:chocolate_cake`. Both are harmless; clean up on the next run.

## 4. Tests

After a full restart, `logs/latest.log` first: `[itemMerges] 5 merges`, no `Error occurred while handling event`
from `item_merges.js` or `cooking.js`, no `ieMixer` warning, no mixin apply error for `JeiFluidHelperTooltipMixin` or
`QuarkPermafrostStyleMixin`, and no `[jeiRemove]` lines for the five purged ids.

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
| MG-09 | JEI chocolate cake | CC&A cake with: baked base + frosting + 2 chocolate bars by hand, 7 slices, oven; no spout |
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
