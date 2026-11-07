# Item merges (total replacements)

Status: BUILT 2026-10-22, scratch server PASSED 2026-10-22 (section 4.1), shipped 1.1.01, not tested in game. Cantaloupe (2.10) built 2026-10-28, scratch PASSED (SQ-CM), shipped 1.1.04 / 1.1.05. Food merges 2.13 to 2.15 (2026-11-01 / 11-02): scratch PASSED r1102b, unreleased (deep scan 2026-11-02). KubeJS,
plus patcherMod for 2.6 (fluid tooltips) and 2.7 (worldgen): a full restart is needed.
Still in game: MG-01, MG-02, MG-04, MG-05, MG-07, MG-13 (2.1 to 2.5), MG-16, MG-17, MG-20 (JEI tooltips), the MG-21
names, and the machine runs of MG-18 / MG-19. Coffee (2.8, built, scratch server PASSED 2026-10-22 for the recipe and effect parts: SQ-CO-01, SQ-CO-02, `tools/food_drinks_probe/food_drinks_probe.js` `/fdtest coffee`, after two fixes in 2.8); in game MG-23 (JEI), MG-24 (drinking), MG-25 (inventory swap, real player only), MG-26 (JEI), MG-27 (JEI), MG-28 (an old bush).

Swap lists (section 6, TODO `creativedelight/swappers #2`, `#3`): CD merges moved into `swaps/merges_*.json` and the Java world half built 2026-11-02; patcherMod 1.1.08 installed 2026-11-02 23:20 and the CD jar 2026-11-02 23:16 (again 2026-11-03 00:08), not booted (SQ-SWP-01 to -08, queue section SQ-CDJ6).

One item is purged and the survivor takes over everything it did: recipes, tags, loot, and any copy that still
reaches a player. Open merges are in [todo/TODO.md](todo/TODO.md) family `merges` (plans: section 5), plus `food/drinks #3`.

## 1. How a merge works

| Piece | File | What it does |
|---|---|---|
| Merge lists | pack: `kubejs/startup_scripts/_lib/item_merges.js`, `_lib/removed_woods.js`; Creative Delight: its swap lists `development/creativeDelight/src/main/resources/data/creativedelight/swaps/merges_*.json` (section 6; were `kubejs_startup/<mod>/item_merges.js` until 2026-11-02) | `global.addItemMerges(group, pairs)`: `[purged, survivor]` pairs (pack: item dict keys; CD: ids from the lists, fed in by CD's `creativedelight/swap_merges.js`) into one group. Adds each purged id to `global.removedItems`; `global.itemMerges` is every pair (probes, tools) |
| Hide and strip | `removed_items.js` consumers | JEI hides the purged item (`client_scripts/jeichanges.js`); its item tags are stripped and `recipe_scan.js` (`/recipescan`, off at load by default) logs any recipe that still uses or makes it (`[jeiRemove] <id> -> n recipe(s)`) |
| Recipes | pack: `kubejs/server_scripts/other/item_merges.js` (priority -100); CD: `kubejs/data/creativedelight/kubejs/_core/item_merges.js` (priority -100, last in the CD tree) | `global.itemMergeRun.recipes(e, group)` -> `NCH.replaceItems`: every input and output of every recipe, data and script-added. Per-merge removals of duplicates: the pack file (general), CD `<mod>/item_merges.js` (food) |
| Tags | same files as the per-merge removals | The survivor joins the tags recipes or mobs read |
| Loot | the two run files, LootJS | Every table: purged entry -> survivor, same count (`CE/item_merges/<group>`) |
| Inventory | the two run files, `PlayerEvents.inventoryChanged` | A purged stack in a player inventory becomes the survivor, same count. Catches mod code (Neapolitan's milk cauldron), old chests and old worlds |
| World (CD group) | Common `swap/ItemMerges` (patcherMod) | Stacks already in the world: player inventories and ender chests, containers, dropped items; NBT and count kept (section 6) |

To add a merge: pack: add the pair with `global.addItemMerges` in the pack's list; food: add `"survivor": ["purged"]` to
the CD swap list `merges_<purged mod>.json` (section 6; its `requires` names the mods both items need). Then remove any duplicate recipes the swap leaves and add tags in the matching server file, and add a
section and tests here. `tools/food_dict.py`, `tools/food_families.py` and `tools/jei_world/tab_drops.py` read `[i.a, i.b]` pairs from the pack's
`_lib` files and the ids from CD's `swaps/merges_*.json`.

### 1.1 Groups: who runs a merge (2026-10-27, perf.md 11.2 #4, TODO `server #20`)

Each merge belongs to one group, and each group's swaps (recipes, loot, inventory) run from that group's own server
script. The recipe swap tool runs twice per recipe event (once per group); no pair is swapped twice.

| Group | Pairs | Run from | Sees |
|---|---|---|---|
| `pack` | general: rocky dirt (2.5), the removed woods ([recipes_wood.md](recipes_wood.md) 2.6), 93 pairs. Later split out by mod | `server_scripts/other/item_merges.js`, priority -100 | data recipes, and recipes added by the pack's scripts before it. A CD script that adds a recipe naming a pack-purged id is not swapped: name the survivor there |
| `creativedelight` | food: banana, milk bottle, chocolate cake, diced potatoes, the 7 coffee pairs, pickled pickles, the 2 salt pairs, 14 pairs | CD `_core/item_merges.js`, priority -100 (CD loads after every pack script) | data recipes and every added recipe (pack and CD) |

- `global.addItemMerges` refuses a purged id already in any group (logs `already merged`), and warns when a pair
  chains with another (a survivor that is purged elsewhere).
- `global.itemMergeRun.recipes` runs a group once per recipe event (keyed on the event's identity hash; a repeat logs
  `already ran`) and logs `[itemMerges] <group>: <n> merges, <m> recipes swapped`.
- `NCH.replaceItems(pairs, e)` (patcherMod 1.1.03): one `forEachRecipe` pass over the data recipes with an
  or-filter of a single match for every purged id, then one walk of the added recipes prefiltered by the same match;
  a matched recipe gets every pair's `replaceInput` / `replaceOutput` in list order. Before: two KubeJS data scans and
  two Rhino walks of the added recipes per pair, and Creative Delight repeated the whole pass for every pair. No
  fallback: the pack scripts and patcherMod ship together, so the run needs a patcherMod with `replaceItems`.
- Recipe ids that moved with the food work: the chocolate cake cutting recipe and Gambler's Tea are now
  `creativedelight:` ids (were `creativeengineering:`).

## 2. Merges

| # | Purged | Survivor | Per-merge work |
|---|---|---|---|
| 2.1 | `alexsmobs:banana` | `neapolitan:banana` | Leaf drops stay off (`config/alexsmobs.toml` `bananasDropFromLeaves = false`, unchanged). Neapolitan's banana joins `#forge:crops/banana`, so `#alexsmobs:bananas` (Alex's Mobs' banana-eating mobs) holds only it. Trader elephant chest: banana -> Neapolitan's (loot swap). Eating Neapolitan's banana gives an Alex's Mobs banana peel, as Alex's did (sopa de macaco keeps its source). Compost row removed. Monkeys (2026-10-30, TODO `merges/mobs #1`): gorilla and sugar glider leaf foraging build Alex's banana in code; Creative Delight mixins `mixin/alexsmobs/GorillaForageBananaMixin` and `SugarGliderForageBananaMixin` hand them Neapolitan's (`compat/BananaMergeHooks`, Alex's banana if Neapolitan is absent). Capuchin, gorilla and elephant feeding already go through `#alexsmobs:bananas`. Neapolitan's chimpanzee is off: `neapolitan-common.toml` group chance 0.0 (was 0.25), its only spawn is with a generated banana plant; spawn egg and head hidden (`removed_items.js`). The Hullabaloo disc stays (bunker loot); the chimpanzee banner pattern stays (crafted) |
| 2.2 | `neapolitan:milk_bottle` | `farmersdelight:milk_bottle` | Abnormals Delight turns off Farmer's Delight's bucket and bottle recipes while Neapolitan is loaded, so Neapolitan's ran (3 per bucket); since 2026-10-25 Creative Delight replaces them with 4 per bucket ([abnormals_delight.md](abnormals_delight.md) 3.4): bucket + 4 glass bottles -> 4 FD bottles and bucket + 4 `#forge:bottles/milk` -> milk bucket (FD's bottle joins that tag). Removed: Create spout / drain compat for Neapolitan's bottle and Delightful's `matcha_latte_neapolitan` (FD copies exist). Milk cauldron: taking milk out with a bottle gives Neapolitan's bottle in code; the inventory swap turns it into FD's |
| 2.3 | `neapolitan:chocolate_cake` | `createaddition:chocolate_cake` | Keeps CC&A's texture. Abnormals Delight's 7 slices -> cake makes CC&A's cake. Since the baking overhaul (2026-10-22, [oven.md](oven.md) 2.6) every other recipe is gone (CC&A's own spout, Neapolitan's crafting); baking.js makes CC&A's by hand, in the oven and by a Spout (baked cake base + 500 mB chocolate; ids `chocolate_cake_manual_only`, `chocolate_cake`, `createfilling/chocolate_cake_spout`, [oven.md](oven.md) OV-33). A bite from the placed cake counts for Spice of Life as the Abnormals Delight slice ([food.md](food.md) 4.2 finding 5). Knife on the cutting board: CC&A's cake -> 7 Abnormals Delight chocolate slices (NCH `fdCutting`, id `chocolate_cake`). Knife on the placed cake: CC&A's block joins `abnormals_delight:drops_chocolate_cake_slice`. Compost row moved to CC&A's cake |
| 2.4 | `moredelight:diced_potatoes` | `rusticdelight:potato_slices` | Rustic Delight's potato -> slices stays (3 since 2026-11-01 [2], rustic_delight.md 5.2); More Delight's potato -> 1 diced removed. Rustic slices join `#forge:sliced_potato` (the pack's fries) and `#forge:sliced_potatoes`, which More Delight 25.07.28's dishes (diced potatoes with beef / chicken / egg and tomato / porkchop, mashed potatoes, potato salad) read. The plural tag held only the diced potatoes, and a merged item is stripped from every tag, so it was empty until 2026-11-01 (`dishes/ingredients #26`; CD `moredelight/item_merges.js`). No sliced sweet potato item exists in the pack |
| 2.5 | `decorative_blocks:rocky_dirt` | `twigs:rocky_dirt` | Decorative Blocks' 4 dirt -> 1 and 1 -> 4 dirt recipes removed. Dave's `davebuildingmod:rocky_dirt` (and slab) is a separate block, renamed Pebble Dirt / Pebble Dirt Slab. Recipes swapped: Pebble Dirt is Twigs' pattern (dirt and pebbles crossed in a 2x2) -> 4, Rocky Dirt is Dave's shapeless 2 dirt + 2 cobblestone -> 4; coarse dirt in place of dirt gives 8 in both. Originals (`davebuildingmod:rec_rocky_dirt`, `twigs:rocky_dirt`) removed |
| 2.8 | Farmer's Respite's coffee: berries, beans, sack, wild bush, three drinks | Rustic Delight's | See 2.8: 7 pairs, 15 recipes removed, Rustic's coffees give Caffeinated |
| 2.9 | `salt:salt`, `vintagedelight:salt_block` (and the salt bucket, removed) | `vintagedelight:salt_dust`, `salt:salt_block` | See 2.9: one pink salt |
| 2.10 | `fruitsdelight:hamimelon_slice`, `fruitsdelight:hamimelon_popsicle`, `creativeengineering:canned_hamimelon` | `delightful:cantaloupe_slice`, `delightful:cantaloupe_popsicle`, `creativeengineering:canned_cantaloupe` | See 2.10: hamimelon line renamed Cantaloupe, Delightful's cantaloupe renamed Mini Cantaloupe |

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
- Removed in `kubejs/data/creativedelight/kubejs/rusticdelight/coffee_merge.js` (in the pack's
  `server_scripts/other/item_merges.js` until 2026-10-27; the swap would leave them as loops, duplicates or a kettle
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

### 2.9 Salt (VERIFIED COMPLETE 2026-11-02: code in CD `vintage_items.js` and `vintagedelight/recipes.js`, scratch r1031 pass; only play is open; built 2026-10-26, scratch server PASSED r1031 2026-10-31 after the 2026-10-28 fix; TODO `food/seasoning #6`)

Scratch run 2026-10-28 ([run log](testing/scratch_queue_run_2026-10-28.md)): SQ-SALT-01 FAILED on two leftovers, fixed
2026-10-28 in CD `vintagedelight/recipes.js` (rebuilt, CD jar installed, retest SQ-SALT-01): the 3 froglight jar recipes
(`vintagedelight:fermenting/*_froglight_from_fermenting`) are rebuilt with The Salt's Salt Block, and Tactical Fishing's
`fishing_vintagedelight_salt_bucket` is removed, with `fishing_creativeengineering_powdered_sugar_bucket` (a defensive
removal; whether that recipe exists was not in the dumps). Tags, the give swap and the rock-salt patch (SQ-SALT-02 to -04)
PASS. Retest r1031 (2026-10-31, SQ-CM-10): the froglight recipes take the Salt Block, sourdough cuts to 3 Bread: PASS.


All salt is Vintage Delight's pink salt (owner, scratchpad 2026-10-26). Pairs in Creative Delight's
`vintagedelight/vintage_items.js` (jar startup): The Salt's salt -> Vintage's salt dust; Vintage's Salt Block -> The
Salt's Salt Block (owner: The Salt's block stays, its shape in Vintage's block colours). Vintage's salt bucket is
removed with both its recipes (water bucket smelt, bucket -> 3 dust). Vintage's bricks, smooth block, pillar, mixed
bricks and the 17 lamps stay.

- Recipes (CD `vintagedelight/recipes.js`): the swap leaves two 9 <-> 1 pairs; Vintage's packing (any `#forge:salt`,
  id `vintagedelight:salt_block`, now makes The Salt's block) and The Salt's `salt:salt_unpacking` stay,
  `salt:salt_packing` and `vintagedelight:salt_block_deconstruct` go. The Evaporator and the salt decor recipes take
  The Salt's block (swap). CD `creativeengineering/salt.js` makes salt dust directly (raw rock salt -> 4, heated basin).
- Tags: salt dust joins `#forge:salts` (The Salt's Salting craft), `#forge:dusts`, `#supplementaries:hourglass_dusts`.
- Loot: cauldron evaporation (`kubejs/data/salt/loot_tables/cauldron_evaporation/salt_{full,level_1,level_2}.json`)
  gives salt dust, same chances. Everything else (butcher trades, mod code) through the loot and inventory swaps.
- Worldgen: Vintage's beach salt patch lays The Salt's raw rock salt block under its layers (owner 2026-10-26; was
  The Salt's Salt Block, which dissolved by the sea) (`tools/gen_soil_compat.py` `STATE_SWAPS`,
  `kubejs/data/vintagedelight/worldgen/configured_feature/patch_salt.json`).
- Large Salt Lamp: The Salt's lamp, renamed (`namechanges.js`). Recipe `large_salt_lamp` (CD `salt.js`): Vintage's
  default lamp outline without its salt on top, every other slot a Salt Block: `SSS / SGS / SDS`, S Salt Block, G
  glowstone, D deepslate tiles (Vintage's lamp uses tiles). Was raw rock salt block, torch, wooden slab.
- Art: `textureswapper/salt/make_salt.py --install` writes The Salt's 19 textures into `kubejs/assets/salt/textures/`:
  Salt Block in Vintage's salt block colours (`textureswapper/salt.png`); both ores (salt pixels only), raw rock salt
  and its block, buds, cluster, cauldron fill (salt pixels only), lamp top, the salt item and the salted overlay in
  Vintage's salt dust colours; lamp side: the salt part recoloured, the wood base (2 rows) kept
  (`src/lamp/salt_lamp_side_{salt,wood}.png`). Sources copied to `textureswapper/salt/src/`. The new Salt Block
  texture also goes over Vintage's `block/salt.png` (`kubejs/assets/vintagedelight/`), so the Salt Layer matches it.
- Seasoning JEI page icon: salt dust (patcherMod `SeasoningJEI`, falls back to The Salt's salt).
- Block behaviour (owner, 2026-10-26; Creative Delight `salt/SaltBlockTicks.java` + mixins `SaltBlockRandomTickMixin`,
  `SaltLayerRandomTickMixin`; the rock salt exemption was `SaltRockNoDissolveMixin`, since 2026-11-02 an edit in the
  Salt member's own `Dissolving`, [salt.md](salt.md)). All through The Salt's own `Melting` / `Dissolving`, so
  `salt-common.toml` chances apply:
  - Every salt block melts touching ice and snow (`#salt:meltables`, 40 % per random tick, one block per tick). The
    Salt's blocks did already; Vintage's whole salt family now does too: Salt Layer, Evaporator, smooth salt, salt
    bricks and mixed salt bricks (with stairs, slabs, walls), salt pillar, the 17 salt lamps. Their random ticks are
    switched on at common setup (`BlockBehaviour.isRandomlyTicking`, set by reflection).
  - Dissolve in water (35 %, not from below; a water source beside leaves water), never in rain (`SaltDissolvingInRain = false` in `salt-common.toml`, 2026-10-28): The Salt's Salt
    Block, ores and Large Salt Lamp (The Salt's own), and the Salt Layer (patcherMod; the snow-layer melt by light is
    replaced). Not the bricks, smooth salt, pillar, Vintage lamps or the Evaporator.
  - The raw rock salt block never dissolves (the beach patch is built on it). It still melts and grows clusters.
  - Salt Blocks grow no salt layers and no clusters (only raw rock salt block is `#salt:salt_cluster_growables`).
    Vintage Salt Blocks left in old worlds act like The Salt's (melt, dissolve, no layer growth).
  - The Evaporator keeps Vintage's growth: 50 % per random tick, the first layer needs water beside and air above,
    later layers grow without water, up to 8.
- Performance (analysis 2026-10-26, no profile yet):
  - Random ticks are spent per chunk section: 3 picks per tick (`randomTickSpeed`) in each section holding any
    randomly ticking block. A salt block is hit about once every 23 min of game time on average (4096 / 3 ticks),
    and a hit costs 6 neighbour reads and tag checks: well under a microsecond.
    10 000 salt bricks give about 150 hits a second, much less than 0.1 ms per second of server time.
  - The only new cost per section: a section with no random tickers before (an underground salt-brick build; surface
    sections already have grass and leaves) now gets its 3 picks a tick. Same as any section with one grass block.
  - Mixin overhead: one identity-set lookup at the head of `BlockBehaviour.randomTick` (blocks that do not override
    it) and of `SnowLayerBlock.randomTick` (all snow layers).
    Each one costs a few nanoseconds and runs only on blocks that already random tick.
  - Block updates happen only when something melts or dissolves (setBlock plus The Salt's sound and particles).
    A big salt build in a snowy biome clears its touching snow once, then has nothing left to melt.
  - Rain does not dissolve salt (config off), so an outdoor Evaporator keeps its layers in rain.

### 2.10 Cantaloupe (built 2026-10-28, scratch server PASSED 2026-10-28: SQ-CM-01 to -03, -09; shipped to the client in 1.1.04, server in 1.1.05; MG-39 to MG-44 in game; TODO `merges/fruit #1`)

Owner plan 5.4 plus answers of 2026-10-28. Fruits Delight's hamimelon line is the **Cantaloupe** (melon block on a stem,
seeds, jam, jello, juice, shaved ice, wild patch); Delightful's cantaloupe (the repack's sand crop, a small
fruit block) is the **Mini Cantaloupe**. Ids unchanged, names in `client_scripts/namechanges.js` (items, the stems,
cauldrons, the three fluids, the Delightful plant and sliced block, the JEI seed note and the biome note).

| Purged | Survivor | Why |
|---|---|---|
| `fruitsdelight:hamimelon_slice` | `delightful:cantaloupe_slice` | one slice; Delightful's keeps the Pumpkin Slice values (`delightful_foods.js`) |
| `fruitsdelight:hamimelon_popsicle` | `delightful:cantaloupe_popsicle` | owner 2026-10-28: Delightful's slice art and popsicle (and its art) stay; the earlier note to keep the hami popsicle art is reversed. The survivor takes the hami popsicle's `frozen/popsicle` family and sweet food type (`food_types.json`, `food_families.json`, `food_dict.js`) |
| `creativeengineering:canned_hamimelon` | `creativeengineering:canned_cantaloupe` | same slice now; `canning.js` skips generated cans whose item is purged |

Pairs: Creative Delight `kubejs_startup/fruitsdelight/item_merges.js`; server half `data/.../fruitsdelight/item_merges.js`.

- Recipes. The merge swaps Fruits Delight's data recipes onto the cantaloupe slice: 9 slices -> Cantaloupe (block),
  knife on the block -> 9 slices, slice -> Cantaloupe Seeds, jam,
  jello, juice. Removed: Delightful's slice -> Mini Cantaloupe Seeds and Fruits Delight's slice popsicle (Delightful's two
  popsicle recipes stay). New: 4 slices (2x2) -> Mini Cantaloupe (`delightful/recipes.js`, `mini_cantaloupe`); 1 Cantaloupe Seeds
  <-> 1 Mini Cantaloupe Seeds (`mini_cantaloupe_seeds`, `cantaloupe_seeds_from_mini`); a Mini Cantaloupe + 2 ice cubes +
  stick -> the popsicle (Delightful's `cantaloupe_popsicle_from_cantaloupe`, now with the Mini Cantaloupe). Bread, gummy, wrapped and
  stuffed cantaloupe keep `#forge:fruits/cantaloupe` / the Mini Cantaloupe as before.
- Tags. The slice joins `#creativeengineering:exotic_fruit` (the hami slice's); it was already in the fruit salad
  slices. Cantaloupe Seeds join `#forge:seeds`. The survivor popsicle keeps its Display Delight plated form.
- Compost: the hami slice row removed; food_dict: `fruit_slice_hamimelon`, `can_hamimelon` removed.
- Worldgen (`_worldgen/wild_crops.json`): the hamimelon rule (warm desert, warm badlands; sand, red sand, terracotta,
  grass-like soils) is labelled Cantaloupe. The Mini Cantaloupe rule moved there too (owner: same spawn locations):
  new pack feature `creativedelight:wild/mini_cantaloupe`, a copy of `wild/hamimelon` placing `delightful:cantaloupe`.
  Was warm dry grassy with Delightful's `patch_cantaloupe`.
- Open: the Mini Cantaloupe Plant still needs sand next to water to be planted (Delightful's rule); the wild patch
  places the fruit block, not the plant. `crops_by_biome.md` needs `python3 tools/gen_crop_worldgen_table.py`.

### 2.11 Apple tree (built 2026-10-30, not run; MG-47; TODO `farming/crops #5`)

Owner 2026-10-30: Fruits Delight's apple tree is removed, Vinery's apple tree stays (crop seasons type `autumn_fruit`,
[crops.md](crops.md) §2.12.1).

| Purged | Survivor | Why |
|---|---|---|
| `fruitsdelight:apple_sapling` | `vinery:apple_tree_sapling` | one apple tree |
| `fruitsdelight:apple_leaves` | `vinery:apple_leaves` | leaves item (Silk Touch, creative) |

Pairs: Creative Delight `kubejs_startup/fruitsdelight/item_merges.js` (now also `// requires: vinery`). Worldgen: the
mod's `forge/biome_modifier/apple.json` is overridden with `forge:none` in `kubejs/data/fruitsdelight/`, so new chunks
get no Fruits Delight apple trees (forests, flower forests and the Terralith Blooming / Lavender / Moonlight biomes lose
their apple trees; Vinery's grow in plains, sunflower plains, meadow and 9 Terralith biomes). Trees already generated
stay and keep fruiting; their leaves are typed `autumn_fruit` like Vinery's. The apple itself (`minecraft:apple`) is
unchanged and its tooltip shows the Vinery apple tree's seasons. Apple jelly, jello and crates stay (made from apples).
The Botany Pots apple recipe goes through the recipe swap onto the Vinery sapling.

### 2.12 Ancient fish (built 2026-11-01, not run; SQ-CAT-02, SQ-CAT-04; [cozy_cats.md](cozy_cats.md) 10.2)

Owner 2026-11-01: Cozy's Improved Cats' ancient fish go; cats tame with vanilla cod and salmon.

| Purged | Survivor | Why |
|---|---|---|
| `cozys-improved-cats:ancient_cod` | `minecraft:cod` | taming fish is vanilla cod |
| `cozys-improved-cats:ancient_salmon` | `minecraft:salmon` | taming fish is vanilla salmon |
| `cozys-improved-cats:ancient_cooked_cod` | `minecraft:cooked_cod` | same food |
| `cozys-improved-cats:ancient_cooked_salmon` | `minecraft:cooked_salmon` | same food |

Pairs: Creative Delight `kubejs_startup/cozys_improved_cats/item_merges.js`; the server half
(`data/creativedelight/kubejs/cozys_improved_cats/item_merges.js`) removes the 8 smelting / blasting recipes, which
the swap would turn into copies of vanilla's. The Training String recipe goes through the swap (cod + salmon +
string). Sources are off at the mod too: fishing swap and mob drops by config, fisherman trades by Creative Compat
(`cats.CozyCats.onTrades`). The cooked fish's random effect on eating is lost with them.

### 2.13 Fish and chips (built 2026-11-01, scratch PASSED r1102b: SQ-MG-21, SQ-VDF-07; not played; TODO `merges/dishes #1`, Veggies Delight M1)

Owner 2026-11-01: Veggies Delight's Fish and Chips stays, PneumaticCraft's goes.

| Purged | Survivor | Why |
|---|---|---|
| `pneumaticcraft:cod_n_chips` | `veggiesdelight:fish_and_chips` | same dish; both were shown as Fish and Chips |

Pair: Creative Delight `kubejs_startup/pneumaticcraft/item_merges.js`. Server half in
`data/creativedelight/kubejs/pneumaticcraft/recipes.js`: one frying route (Cooking Pot, cooking oil + any raw fish + 2
sliced potatoes, in a bowl; was on a paper plate); the grid route (Fries + cooked fish + bowl) left with the Fries. Veggies Delight's two pot recipes (raw fish, potato, egg; its More Delight diced-potato version) are removed as
duplicates. The survivor's food is Veggies Delight's (10, 12 saturation points, returns the bowl), not PneumaticCraft's
(12, 24, no container). The "chips are fries" tooltip moved to the survivor. Fries (`pneumaticcraft:chips`) are
removed outright (owner 2026-11-01, not a merge: no survivor): CD startup `pneumaticcraft/pneumatic_items.js`
(removedItems), the pack's frying recipe gone, Canned Chips dropped from `gen_can_types.py`; `dishes/meals #34` closed.

Tests: SQ-MG-21, SQ-VDF-07 (scratch); MG-48 (in game: JEI shows one Fish and Chips with the frying route, an old Cod 'n' Chips in an
inventory turns into Fish and Chips).

### 2.14 Shulker meat (built 2026-11-02, scratch PASSED r1102b: SQ-FCB-04e, -05; not played; TODO `dishes/ingredients #32`; UNDONE 2026-11-04)

**Undone 2026-11-04** (plan_meals_rolls_bbq.md C3, owner: the End Skewer takes Ender's Shulker Mollusk only): both pairs
removed from `merges_endersdelight.json`, the chance-0 knife loot override deleted (Ender's knife drop is upstream's again)
and the mollusk-to-filet cut restored. Every other recipe keeps End's meat: the stuffed shulker (pot and heatless) takes End's
shulker meat. Canned Shulker Flesh stays on End's meat and slice. Stacks a world merged between 11-02 and 11-04 stay End's
meat (no conversion back). Text below is the 11-02 state.

| Purged | Survivor |
|---|---|
| `endersdelight:shulker_mollusk` | `ends_delight:shulker_meat` |
| `endersdelight:shulker_filet` | `ends_delight:shulker_meat_slice` |

Pairs: CD startup `endersdelight/item_merges.js`. Server half: Ender's mollusk-to-filet cut removed
(`endersdelight/recipes.js`, it duplicated End's meat-to-slice cut); Ender's knife loot modifier switched off by a pack
datapack override (`kubejs/data/endersdelight/loot_modifiers/scavenging_shulker_mollusk_from_shulker.json`, chance 0),
so a shulker killed with a knife drops End's meat once. Canned Shulker Flesh is filled from End's meat and slice. End's
textures unchanged; the owner updates them later (2026-11-02).

### 2.15 Caesar salad (built 2026-11-02, scratch PASSED r1102b: SQ-FCB-01, -05; not played; TODO `merges/dishes #6`)

`oceanic_delight:caesar_salad` -> `veggiesdelight:cesar_salad` (Veggies' texture, shown as "Caesar Salad"). Pair: CD
startup `oceanic_delight/item_merges.js`. Oceanic's recipe removed; the one recipe (CD `veggiesdelight/cooking.js`):
cabbage, tomato, any cheese, any bread, bowl (by hand, `_manual_only`).

### 2.16 Cooking for Blockheads racks and basket (built 2026-11-02, not run; SQ-FCB-05; TODO `cooking/kitchen #24`)

CfB's own `spice_rack`, `tool_rack`, `fruit_basket` -> the blockheads_additions `oak_` versions (same CfB blocks and block
entities, so kitchens use them). Pairs: CD startup `cookingforblockheads/item_merges.js` (only while the member is on).
CfB's three recipes removed (`cookingforblockheads/kitchen.js`); woods with no rack of their own no longer make one.

### 2.17 Potato salad (built 2026-11-03, in the CD jar installed 2026-11-03 00:08, not booted; SQ-CDM2-04; TODO `merges/dishes #4`, `textures/merges #1`)

Owner 2026-11-02: one Potato Salad with Rustic Delight's texture.

| Purged | Survivor |
|---|---|
| `moredelight:potato_salad` | `rusticdelight:potato_salad` |
| `oceanic_delight:potato_salad` | `rusticdelight:potato_salad` |

Pairs: CD swap lists `merges_moredelight.json` (requires moredelight, rusticdelight) and the new
`merges_oceanic_delight_potato.json` (requires oceanic_delight, rusticdelight; a file of its own because the Caesar list
requires veggiesdelight). Merges, not conversions. The survivor keeps its own art and food (8 hunger, Regeneration 0:05;
Comfort by the Comfort / Nourishment rule). Recipes (CD `rusticdelight/recipes.js`): the three mods' recipes removed;
Cooking Pot: 2 potatoes of any kind (potato, sweet potato or their slices), egg, milk, onion or garlic, bowl; by hand
the same plus 1 more potato (`_manual_only`); the egg solution mixing takes 2 potatoes of any kind, onion or garlic and
250 mB egg solution. The pickle input of the 2026-10-27 plan is dropped. Caesar salad texture (`textures/merges #2`):
the survivor is Veggies Delight's own item, so it already has Veggies' texture (2.15); nothing to build.

### 2.18 Nut mash (built 2026-11-03, in the CD jar installed 2026-11-03 00:08, not booted; VTD-84, VTD-85, SQ-VTDA-11, -12; TODO `merges/dishes #7`, `textures/merges #3`)

Owner 2026-11-02: only nut mash, from acorn or peanut, mason jar only; nut butter dropped. Pairs (swap lists):
`vintagedelight:nut_mash_bottle` -> `vintagedelight:nut_mash_jar` (`merges_vintagedelight.json`);
`delightful:nut_butter_bottle` -> `vintagedelight:nut_mash_jar` and `delightful:nut_butter_and_jelly_sandwich` ->
`vintagedelight:pb_j` (new `merges_delightful.json`, requires both mods). `delightful:nut_dough` is a removed item (CD
`vintage_items.js`, swaps `removed.json`) with its recipe gone. Names (port lang, `src/own`): the jar is "Nut Mash" (was
Nut Mash Jar), `pb_j` "Nut Mash and Jam Sandwich" (was PB & J); Vintage's item, so Vintage's texture (`textures/merges #3`
needs nothing more). Recipes removed first, so the merge carries none over: Delightful's nut butter pot and both sandwich
crafts (CD `delightful/recipes.js`), Vintage's nut mash pot and the bottle <-> jar crafts (`vintagedelight/recipes.js`).
New pot (`vintagedelight/recipes.js`): 5 of acorn / roasted acorn / peanut / roasted peanut + any sugar -> 3 Nut Mash,
each in an empty mason jar (was 5 roasted peanuts -> 1). Sandwich (`vintagedelight/cooking.js`): any bread + Nut Mash +
any jam -> 1 (the jars are crafting remainders); Pad Thai's grid recipe takes the jar. `#forge:nut_butter` = the jar.
Open: 1 sandwich per jar (the brief's D3 suggested 3); the generated food types still list the purged ids until the next
food catalog run.

### 2.19 Kimchi (built 2026-11-03, in the CD and Creative Brewing jars installed 2026-11-03 00:08, not booted; VTD-83, SQ-VTDA-11; TODO `merges/dishes #9`, `textures/merges #4`)

Owner 2026-11-02: Vintage's kimchi wins; Brewin's is removed, not merged, and its recipes are not carried over. Brewin's
kimchi: removed item (CD `vintage_items.js`, only while Brewin' is loaded), id in Creative Brewing's new
`swaps/removed.json`; the pack's `brewin_kimchi` jar recipe is gone (Creative Brewing `brewinandchewin/alcohol.js`), the
keg recipe was already removed. Tags: Brewin's kimchi left `#creativeengineering:pickled_veg` and the heatless soup veggie
tag, Vintage's kimchi joins the latter. Canned Kimchi holds only Vintage's kimchi (`tools/gen_can_types.py`; values now
from Vintage's kimchi: 8 hunger, 0.3, fast). New jar recipes (CD `vintagedelight/recipes.js`, `kimchi_from_<base>`), the
upstream ghost pepper recipe kept: 4 cabbage leaves + salt + one of cucumber or zucchini (one recipe), onion, `#forge:eggs`,
sea pickle, beetroot, `#creativeengineering:whole_raw_fish` -> 4, upstream time, no pickled pepper side output. The fish
tag (CD `vintagedelight/tags.js`): cod, salmon, tropical fish, lionfish, perch, pike, blobfish, flying fish, catfish; no
slices, squid or shellfish, no pufferfish. Texture: Vintage's own item (`textures/merges #4` needs nothing more).

### 2.20 Quiche (built 2026-11-03, in the CD jar installed 2026-11-03 00:08, not booted; VTD-86, SQ-VTDA-13; TODO `merges/dishes #8`, `baking/baked-goods #19`)

The quiche family (food_families.md `quiche`) has two items: Brewin's `brewinandchewin:quiche` and Veggies'
`veggiesdelight:zucchini_quiche` (with slices). Owner 2026-11-02: baking type, pie tin, oven only, no plating
(`R.ceBaking`, `{ exp: 0.1, time: 200, tool: #creativeengineering:bakeware/pie_tin }`, no container: a quiche has no
crafting remainder). Brewin's (CD `brewinandchewin/recipes.js`, `quiche_meat`, `quiche_fungus`, `quiche_greens`): pie crust
+ cheese wedge + milk + `#forge:eggs` + one of `#creativeengineering:food_type/cooked/meat`, `#forge:mushrooms`,
`#forge:salad_ingredients`. Zucchini (CD `veggiesdelight/recipes.js`): pie crust + cheese wedge + milk + `#forge:eggs` + 2
roasted zucchini. Removed: the grid recipes and the three sequenced assemblies (milk, egg solution, zucchini). Open: the
pack has no lettuce tag; the lettuce version uses `#forge:salad_ingredients` (cabbage, cabbage leaf, dandelion leaf,
chopped clover); the zucchini quiche's "egg" is read as any egg.

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
- Salt: Vintage Salt Blocks already placed stay in the world and drop The Salt's block. The Salt's own melting /
  dissolving of a dropped salt item does not apply to salt dust.
- Salt merge gaps (audit 2026-10-26):
  - Open, owner to decide: using salt on ice or snow, or dispensing it, no longer melts it. The Salt binds right-click
    melting (`SaltItem.useOn`) and the dispenser behaviour to `salt:salt` itself, not the tag, so salt dust does
    neither. The two world JEI rows that promised it are removed. Fix if wanted: a patcherMod use-on / dispenser hook
    for `#forge:salts`.
  - Open, minor: the Salt Layer has a block item ("Salt Layer", Vintage's creative tab). It drops salt dust
    (1 per layer), not itself.
  - Closed 2026-10-26: layer and Vintage salt blocks melt / dissolve, old Vintage Salt Blocks stop growing layers,
    the beach patch no longer washes out (raw rock salt), JEI (world rows for melting, dissolving and the
    Evaporator; Evaporator info text in `namechanges.js`). Vintage's info on its own Salt Block is unreachable: that
    item is merged away.

## 4. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): rerun pending (batch fault) SQ-SALT-06, -08.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-SWP-06o, -01, -02, -03, -04, -05, -06, -07, -08, SQ-VTDA-12, SQ-SALT-06, -08.

After a full restart, `logs/latest.log` first: `[itemMerges] 1 pack merges registered` and the two
recipe-event lines of 1.1 (SQ-IMS-01), `[food values] 5 effect swaps` with no
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
| MG-06 | JEI uses of milk bucket; craft bucket + 4 glass bottles | 4 Farmer's Delight milk bottles; 4 FD bottles + bucket -> milk bucket (3 until 2026-10-25, abnormals_delight.md 3.4) |
| MG-07 | Milk cauldron (bucket in), take with a glass bottle | FD milk bottle |
| MG-08 | Spout milk into a glass bottle; matcha latte in the kettle with an FD bottle | FD bottle; latte brews, one recipe only in JEI |
| MG-09 | JEI chocolate cake | CC&A cake with: baked base + frosting + 2 chocolate bars by hand, 7 slices, oven, spout (baked base + 500 mB chocolate, the pack's recipe only) |
| MG-10 | Knife on CC&A cake on a cutting board; knife on the placed cake | 7 chocolate slices; one slice per use |
| MG-11 | Knife on a potato on a cutting board | 3 Rustic Delight potato slices (2 before 2026-11-01), no diced potatoes recipe |
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
| MG-29 | JEI search salt | One salt item (pink Salt Dust), one Salt Block (The Salt's, pink), no Salt Bucket; Large Salt Lamp plus Vintage's 17 lamps; ores, raw rock salt, buds and cluster pink |
| MG-30 | Craft 9 salt dust; craft the block back; JEI uses of Salt Block | 1 Salt Block; 9 Salt Dust (one recipe each way); Evaporator, salt bricks / pillar / smooth recipes take it |
| MG-31 | Craft Large Salt Lamp (7 Salt Block, glowstone centre, deepslate tiles bottom middle); place it | Large Salt Lamp, lights up; no raw rock salt block + torch recipe |
| MG-32 | `/give @s salt:salt 3`, `vintagedelight:salt_block 1`; salt a food with salt dust in the grid | 3 Salt Dust, 1 Salt Block; Salted food |
| MG-33 | Cauldron of water over a fire until it evaporates; smelt a water bucket | Salt dust drops; no salt bucket recipe |
| MG-34 | New world, beach: find a salt patch (`/place feature vintagedelight:patch_salt`) | Salt layers on raw rock salt blocks; the rock salt next to the sea stays |
| MG-35 | Place an Evaporator with water beside it; wait for layers; place a Salt Block with water beside it | Salt Layers grow on the Evaporator, textured like the pink Salt Block; the Salt Block grows nothing and dissolves |
| MG-36 | `/gamerule randomTickSpeed 300`; put snow / ice against salt bricks, a salt brick stair, a Vintage salt lamp, a Salt Layer and a raw rock salt block | Every touching snow and ice block melts |
| MG-37 | Same tick speed: Salt Layer beside water, Salt Layer and Salt Block in rain, Salt Layer next to a torch; raw rock salt block beside water and in rain | Layers and Salt Blocks by water dissolve; nothing dissolves in rain; the torch-lit layer stays; raw rock salt stays |
| MG-38 | JEI: uses of Evaporator, of Salt Layer, of `#salt:meltables` | Evaporator page (layers on top, up to 8); Salt Layer in the dissolve rows; no "use salt on ice" or dispenser rows; Evaporator info reads "Salt Blocks do not grow salt" |
| MG-39 | JEI: search hamimelon, cantaloupe | Cantaloupe (block), Cantaloupe Seeds, Cantaloupe Slice (Delightful's art), Cantaloupe Popsicle (Delightful's art), Jam, Jello, Juice, Shaved Ice; Mini Cantaloupe, Mini Cantaloupe Seeds; no Hamimelon Slice, no second popsicle, no Canned Hamimelon |
| MG-40 | `/give @s fruitsdelight:hamimelon_slice 3`, `fruitsdelight:hamimelon_popsicle`, `creativeengineering:canned_hamimelon` | 3 Cantaloupe Slices, a Cantaloupe Popsicle, a Canned Cantaloupe |
| MG-41 | Craft: 1 slice; 9 slices; 4 slices (2x2); 1 Cantaloupe Seeds; 1 Mini Cantaloupe Seeds; Mini Cantaloupe + 2 ice cubes + stick | Cantaloupe Seeds; Cantaloupe; Mini Cantaloupe; Mini Cantaloupe Seeds; Cantaloupe Seeds; Cantaloupe Popsicle |
| MG-42 | Knife on a Cantaloupe block on the cutting board; knife on a placed Mini Cantaloupe | 9 Cantaloupe Slices; 4 Cantaloupe Slices |
| MG-43 | JEI uses of the Cantaloupe Slice: jam, juice, jello, popsicle, exotic fruits bowl, fruit salad, Cantaloupe Bread | All accept it |
| MG-44 | New world, desert and badlands: find wild Cantaloupes and Mini Cantaloupes (`/place feature creativedelight:wild/mini_cantaloupe` to see one); WTHIT on the stem | Both on sand, red sand or terracotta; stem named Cantaloupe Stem |
| MG-45 | Wares package and cardboard box (block and item): look; place an Old and a Weathered Cardboard Box; wash a Dustrial Decor box, then the result, in a Create fan; JEI for other recipes of Old/Weathered | Wares boxes brown; Weathered grey-brown; only the washing recipes exist; boxes walk and sink like Dustrial's (server half: scratch queue SQ-MG-16) |
| MG-46 | Jungle: gorillas and sugar gliders foraging leaves; new world near banana plants | Neapolitan bananas only; no chimpanzees; JEI shows no chimpanzee spawn egg or head (server half: scratch queue SQ-MG-17 to -20) |
- [ ] **MG-47 Apple tree [game]:** JEI has no Fruits Delight apple sapling or leaves; a Fruits Delight apple sapling
  given by `/give` turns into the Vinery apple tree sapling in the inventory; a new world's forest has no Fruits Delight
  apple trees; an apple's tooltip reads "Fertile: Autumn" / "Offseason: Winter, Dry".
- [ ] **MG-48 Fish and chips [game]:** JEI shows one Fish and Chips (Veggies Delight's art) with the Cooking Pot
  frying route, no Cod 'n' Chips, Fries or Canned Chips entry; a Cod 'n' Chips given by `/give`
  turns into Fish and Chips in the inventory; eating it returns a bowl (2.13).

Server half of MG-35 to MG-37 without a player: `tools/salt_probe/salt_probe.js` (`/salttest`, scratch queue SQ-SALT-05
to -07) calls each block's random tick itself and times a salt-brick tick against a stone one (added 2026-10-27).

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

## 5. Planned merges (scratchpad 2026-10-27)

Owner list, moved out of the scratchpad on 2026-10-27. Tracked in [todo/TODO.md](todo/TODO.md) family `merges`
(subfamilies `baking`, `dishes`, `drinks`, `fruit`, `materials`, `mobs`); plans in [briefs/](briefs/README.md)
`merges_*.md`. Nothing here is built yet except where marked. "Survivor" means the item that stays; the purged ids go
through `global.itemMerges` (section 1). Abbreviations: VD = Veggies Delight, MD = More Delight, PC = PneumaticCraft.

Built or confirmed already: salt (2.9, TODO `food/seasoning #6`); coffee (2.8) confirmed working in game 2026-10-27;
the chef's hat (Wardrobe / Simple Hats hat removed) confirmed working in game 2026-10-27.

### 5.1 Dishes (`merges/dishes`)

- Fish and chips (BUILT 2026-11-01, 2.13): keep Veggies Delight's. Merge VD's duplicate recipes. Remove PC's fish and chips, fries and every
  recipe or item tied to them. (Also: merge fish and cod and chips, from the 2026-10-24 note.)
- Mashed potatoes (BUILT 2026-11-01, `merges/dishes #2`; scratch PASSED r1102b): split instead of merge. More Delight's is Mashed Potatoes, VD's is Mashed Sweet Potatoes.
  Cooking pot: 2 potatoes or potato slices + 250 mB milk -> mashed potatoes (sweet potato input -> mashed sweet
  potatoes). Manual craft: 3 cooked potatoes. Both cannable and saltable (with salt).
- Stuffed peppers (BUILT 2026-11-01, `merges/dishes #3`; scratch PASSED r1102b): remove VD's stuffed peppers (item and feast); Rustic's stay. Also removed from the veggie tweaks.
- Potato salad (BUILT 2026-11-03, 2.17): one item. Texture: Rustic Delight's (decided 2026-11-02, was More Delight's). Cooking pot: 2 potato of
  any kind, 1 egg, 1 milk, 1 onion or garlic; the shapeless version adds 1 more potato. (Replaces the 2026-10-27 inputs
  with sliced potatoes and pickle.)
- Cheesy pasta: rename Brewin's to Cheesy Fish Pasta (BUILT 2026-10-30: `namechanges.js`, with its can and placed
  block; shipped 1.1.05). Add bone broth to the cheesy pasta heatless craft.
  2026-11-02 (owner): no cheesy pasta found in game, so nothing is left to merge; the rename stands.
- Caesar salad (BUILT 2026-11-02, 2.15): use the correct spelling; Veggies Delight's texture; lettuce, tomato, cheese, bread.
- Nut butter (BUILT 2026-11-03, 2.18): remove nut dough. Only the nut mash jar remains: 5 nuts (acorn or peanut) + sugar -> 3 nut mash, needs
  a mason jar. Nut butter sandwich: Vintage's texture; any bread + any jam + a nut mash jar.
  DECIDED 2026-11-02 (owner): nut butter is dropped, only nut mash (acorn or peanut, mason jar only); rename to Nut Mash and
  the sandwich to Nut Mash and Jam Sandwich, using nut mash in place of nut butter.
- Pizza and quiche (quiche recipes BUILT 2026-11-03, 2.20): no overlap. Rename Brewin's pizza to Supreme Pizza and rebuild its craft as dough, sauce, dough on
  the bottom row. Make sure the quiche food family exists.
  Quiche recipes DECIDED 2026-11-02 (owner), baking type, pie tin, oven only, no plating: pie crust + cheese wedge + milk +
  any egg + (any cooked meat, or any fungus: two recipes); a lettuce version (any lettuce tag, dandelion leaf included);
  Zucchini Quiche: pie crust + cheese wedge + milk + egg + 2 roasted zucchini.
- Zucchini stuffing: no overlap, nothing to merge.
- Skewers: fold into the skewer family with consistent rules (`dishes/meals #13`). Patties: every patty type is always
  interchangeable (`dishes/meals #14`).
- Burgers: mark all burgers for an ingredient-variant audit and balancing. Drop More Delight's loaded burger in favour
  of "The Burger", renamed Loaded Burger, accepting any cheese. (Rename BUILT 2026-10-30, `namechanges.js`, shipped 1.1.05;
  the drop and the any-cheese recipe are open.)
- Kimchi (BUILT 2026-11-03, 2.19): Vintage Delight's texture. DECIDED 2026-11-02 (owner): Brewin's kimchi removed, its recipes not migrated; new
  Vintage kimchi recipes, one per pickleable in place of the ghost pepper: cucumber or zucchini, onion, egg, sea pickle,
  beetroot, surstromming input fish (all whole raw fish).
- Fish tempura (BUILT 2026-11-02 as removed items, `merges/dishes #11`; scratch PASSED r1102b SQ-FCB-01): drop Fish Tempura and Raw Fish Tempura, keep fried fish. The fish and chips that used tempura go too.

### 5.2 Baking (`merges/baking`)

- Pancakes: recolour the individual pancake items to the Supplementaries pancake shape (one colour per pancake
  type, put on the Supplementaries pancake item). Rename Supplementaries' pancake to Stackable Pancake (BUILT
  2026-10-30, `namechanges.js`, unreleased). Cooking pot:
  3 batter + 2 sugar + 1 egg -> 4 stackable pancakes (check food values against the craft cost). Syrup can go on
  stackable pancakes. Related: Rustic pancake rework (`creativedelight/members #15`), pancake food type
  (`dishes/meals #12`).

### 5.3 Drinks (`merges/drinks`)

- Dandelion juice and tea: keep both, but make sure a juice family and a tea family exist and mark both for folding in.
- Long and strong teas: fine as they are. Dandelion blocks: keep all.

### 5.4 Fruit (`merges/fruit`)

- Cantaloupe and hami melon: rename the survivors to Cantaloupe. Hami melon popsicle texture (make sure a popsicle
  family exists), cantaloupe slice, hami melon seeds, stem, block and spawn behaviour. Implement all other foods of
  both fruits as cantaloupe. Rename Delightful's cantaloupes to Small Cantaloupes, crafted from 4 slices.
  BUILT 2026-10-28 as 2.10 (owner: Mini Cantaloupe, not Small, to match Mini Cantaloupe Seeds and Mini Melon).
- Apple (to define). Also identify the green apple source and decide whether to port it.

### 5.5 Materials (`merges/materials`)

- Crates and bags: chorus: keep End's Delight's crate and bag. Blueberry: drop the sack (not enabled anyway). Coffee: drop
  Farmer's Respite's sack. Apples: drop the bag when Fruits Delight is installed. Sweet berries: drop the Berry Good
  berry basket. (Also `alcohol/decisions #7`, Vinery crates and bags.)
- Cardboard: PARTLY BUILT 2026-10-28 (scratch test queued SQ-MG-16; shipped to the client in 1.1.04). Design and Decor's cardboard box is renamed Old
  Cardboard Box; the new `creativeengineering:weathered_cardboard_box` is the last step. Both are made only by washing
  (Create splashing in `misc_mods.js`): Dustrial Decor box -> Old -> Weathered; their other recipes are removed.
  The Wares boxes and packages are recoloured brown (`textureswapper/cardboard/`). Still open: remove Design and
  Decor's cardboard sheet, one cardboard item with recipes and tags through NCH / `global.tags`, a longer crafting chain.
- Rope: four ropes stay, with distinct jobs.
  - Farmer's Delight: tooltip "Climbable, supports the growth of tomato vines. Use again to extend rope lower".
    Recipe unchanged for now.
  - Quark: disabled.
  - Supplementaries: rename to Rope Netting; tooltip "Climbable, nets and tightwires can be walked across. Use again
    to extend rope lower. Requires support like scaffolding"; recipe 3 Farmer's Delight ropes vertically.
  - Beautify: rename to Twine; tooltip "Climbable, acts like chain"; rope extension behaviour (like Create shafts);
    keep the existing recipe.
- Rotten flesh: Architect's Palette's block becomes the pack's own prepared block with a prepared flesh item. BUILT
  (`merges/materials #3`): the block is `creativeengineering:prepared_flesh_block` (KubeJS `misc_items.js`, hand-authored
  cube_all model, texture `kubejs/assets/creativeengineering/textures/block/prepared_flesh_block.png`); the item keeps
  the id `jrftl:prepared_flesh`, registered by `misc_items.js` since JRFTL was removed 2026-10-31. Recipes in
  `server_scripts/engineering/mob_resources.js` (creosote filling and bottling of rotten flesh and Architect's Palette's
  rotten flesh block, smoking to leather, 9 to 1 block). Why no custom texture showed: the pack sprite sat at
  `kubejs/assets/prepared_flesh.png` (no namespace, never loaded) while JRFTL's own model drew its own sprite; since
  10-31 it is at `kubejs/assets/jrftl/textures/item/prepared_flesh.png` (`textures/merges #9`), and the stray root copy
  was deleted 2026-11-02. JRFTL question closed (jar removed). Unchecked in game: both textures after a full restart.

### 5.6 Mobs (`merges/mobs`)

- Monkeys: built 2026-10-30, see 2.1 (chimpanzee off, gorilla and sugar glider foraging give Neapolitan's banana).
  Scratch r1031 (2026-10-31): mixins PASSED (SQ-MG-17); no chimpanzee in a jungle; the foraging drops are unproven
  (SQ-MG-18, -19: the checks logged nothing), client check pending with CQ-MG-20 / MG-46.

## 6. Swap lists: merges from data, and the world half (TODO `creativedelight/swappers #2`, `#3`; built 2026-11-02; patcherMod 1.1.08 and the CD jar installed 2026-11-02 23:16 to 23:20, not booted; SQ-SWP-01 to -08, queue section SQ-CDJ6)

Creative Delight's merges moved from startup scripts into its swap lists (the same files Common's `swap/SwapLists` reads
for the registry remap, [creative_delight.md](creative_delight.md) 11), one place per item. The 38 pairs of 2.1 to 2.4
and 2.8 to 2.16 are unchanged (40 with 2.17's two, 2026-11-03); only where they live changed.

**Format.** `data/creativedelight/swaps/merges_<purged mod>.json` in the CD jar:
`{ "requires": ["modA", "modB"], "merges": { "survivor:id": ["purged:id", ...] } }`. `merges` has the shape of `replaces`
but names two items that are both still registered. `requires` (mod ids, the old `// requires:` headers) gates the
file's merges: they apply only while every listed mod is loaded. A purged id may appear once across all lists (a
second entry logs `merged twice` and is skipped). Like `replaces`, a merge also remaps the purged id in an old world
once that item is no longer registered, with no `requires` check (the survivor must be registered). Ids are literal
(namespace kept, as in every swap list), not dict keys, since the lists outlive the dict entries. Never pruned.

| Piece | Where | What it does |
|---|---|---|
| Reader | Common `swap/SwapLists` (`merges()`, `scriptMerges(modId)`, record `Merge`) | Reads `merges` and `requires` with the other keys; logs `[swaps] n list file(s): ... k item merge(s)` |
| Script half (#2) | CD `kubejs_startup/creativedelight/swap_merges.js` | Startup: `SwapLists.scriptMerges('creativedelight')` -> `global.addItemMerges('creativedelight', pairs)`; logs `[creativedelight] n item merge(s) from the swap lists`. Startup, not server, because `addItemMerges` feeds `global.removedItems` (JEI hide, tag strip) at load. The group's server half (`_core/item_merges.js`) then swaps recipes and loot as before; per-merge recipe and tag work stays in the server tree's mod folders |
| World half (#3) | Common `swap/ItemMerges`, pure part `swap/ItemMergeCore`, switches `swap/SwapConfig` | Rewrites purged stacks already in the world (below) |

**World half.** Live merges are the active list merges whose two items are registered (logged once:
`[swaps] n item merge(s) live in the world (list <fingerprint>)`). A stack is saved to NBT, every item id in it is
rewritten (the stack itself and any nested saved stack: shulker box, bundle, backpack contents; entity and block entity
`id`s without a `Count` are left alone), and it is loaded back with its original count, so NBT, Forge capabilities and
large modded counts are kept. A stack over the survivor's stack size (e.g. 64 bottles into 16-stack jars) is split:
the rest goes to empty slots, then to the player (inventory or dropped at their feet) or to the ground above the
container; a slot that refuses writes keeps the old stack. Never deletes. Covered: player inventory and ender chest;
block entities that are a `Container` or expose a modifiable item handler; container entities (chest minecarts and
boats); any menu a player opens (crafting, furnace and trade result slots skipped); dropped item entities (an
over-sized dropped stack stays whole, pickup splits it). Not covered: item frames, armor stands, Curios slots,
read-only item handlers, containers in chunks that never load.

**Triggers** (`config/creativeengineering-swaps.toml`, `[swappers]`, COMMON; a file of its own beside
`creativeengineering-common.toml`):

| Key | Default | When |
|---|---|---|
| `onWorldLoad` | false | Every chunk load: its containers are rewritten on the next server tick (32 chunks a tick); every item and container entity as it enters a level |
| `onPlayerDataLoad` | true | Every login: inventory and ender chest |
| `onContainerOpen` | true | Every menu a player opens |
| `onCommand` | true | `/ccswap items` (permission 2): online players, every loaded chunk, every item and container entity now; reports the counts |
| `onceWhenUpdated` | true | Each chunk and player once per list version: the list fingerprint is stamped in the chunk NBT and the player's persisted data (`creativeengineering:swap_merges`); a chunk or player with an older or no stamp is rewritten on its next load. Item and container entities are rewritten as they enter a level while this is on |

The KubeJS inventory swap of 1.1 still runs for both groups (it drops NBT; the Java login and open triggers keep it).

### 6.1 Tests (SWP-)

Offline (2026-11-02): `./gradlew :creativeCommon:build --offline` compiles; harness `MergeCoreTest` (scratchpad, plain
javac/java on the patcherMod classes) 23 checks pass: top-level and nested rewrite, NBT and caps kept, entity ids
untouched, int counts, split sums, fingerprint order-free. JSON: 38 pairs, no duplicates, no chains. Live rows
SQ-SWP-01 to -08 in the scratch queue (section SQ-CDJ6). 2026-11-03: CD `swaps/merges_*.json` hold 40 pairs after
2.17 to 2.20; the counts in SWP-01 (43) predate them, so recount at the first boot (plan_creative_delight.md L).

| ID | Test | Expected |
|---|---|---|
| SWP-01 | Restart, `logs/latest.log` | `[swaps] ... 43 item merge(s)`, `[creativedelight] 43 item merge(s) from the swap lists` (fewer only when a `requires` mod is absent), `[swaps] n item merge(s) live in the world`; no `merged twice`, no `Error occurred while handling event` |
| SWP-02 | JEI: search banana, coffee berries, diced potatoes, Caesar salad | The purged items are hidden, as before the move (recipes and loot follow the survivors) |
| SWP-03 | Put 5 `alexsmobs:banana` with a custom name in a chest (`/data merge`), leave, `/ccswap items` | 5 Neapolitan bananas, name kept |
| SWP-04 | 64 `vintagedelight:vinegar_bottle` in a chest, open it | Jars in 16-stacks (or the survivor's stack size) in the free slots, total 64; none lost |
| SWP-05 | Shulker box holding Farmer's Respite coffee in a chest, open the chest | The box's contents read Rustic Delight coffee |
| SWP-06 | Log out with a purged item in the inventory and ender chest, set `onPlayerDataLoad=false`, rejoin | Rewritten once (`onceWhenUpdated`); a second rejoin logs nothing |
| SWP-07 | Drop a purged stack from an old save (or `/summon item ... {Item:{id:"alexsmobs:banana",Count:3b}}`) | The item entity holds 3 Neapolitan bananas |
| SWP-08 | `onCommand=false`, `/ccswap items` | Refused with the config key named |
