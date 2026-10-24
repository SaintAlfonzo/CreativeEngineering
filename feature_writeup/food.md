# Food: types, values, Spice of Life and canned parity

Test prefix **FO-** · Last updated **2026-10-21** · Overall status **DEPLOYED** (types, tags, can recipes, servings,
fried eggs shipped from 1.0.2 on, generated cans 1.0.6; FO-01 PASSED, FO-02 to FO-28 not run); the tuning system in
3.5 and the egg migration in 3.8 aren't built. Build plan: [briefs/food_types-and-tuning.md](briefs/food_types-and-tuning.md)

The full per-item reference (hunger, saturation, effects, type, Spice of Life value, canned version, recipe chains,
canned parity) is generated: **[food_catalog.md](food_catalog.md)**. This file explains how the pieces fit, what is
wrong today and how tuning will work.

## 0. At a glance

The pack has **1030 edible items** from 40 mods (plus 139 food blocks: placed cakes, pies, feasts and jelly bottles, and 33
drinks with no food value). Every one of them is sorted into **one of 31 types** (raw vegetable, cooked meat, soup, prepared meal, drink,
golden food and so on), and each type is an item tag, so recipes can target "every cooked fish" or "every raw food"
without listing items. The next step is a tuning layer that sets hunger, saturation, effects and Spice of Life value
per type, with per-item exceptions, and gives every can the value of what went into it.

| Part | Layer | Status |
|---|---|---|
| `/dumpfood` export | KubeJS command, `_lib/dump_food.js` | Works: ran 2026-09-28 19:24 (629 foods), FO-01 PASSED. Re-run 2026-10-02 22:13 after the 1.0.4 mods: 856 foods, 138 food blocks, 26 drinks, 27,499 recipes. Now part of `/dumpall`. Re-run 2026-10-20 23:12 on a scratch server: 1030 foods, 139 food blocks, 33 drinks, 28,031 recipes (server-side names are raw lang keys for KubeJS items; the export keeps the client names from the 10-18 dump) |
| Food types (31) + canned map | [food_types.json](../kubejs/generation_scripts/food/food_types.json) | BUILT: 1016 foods typed, 14 excluded, 242 cans mapped to 252 sources (227 new foods typed 2026-10-02, 3.2; 5 on 2026-10-20) |
| Type tags `#creativeengineering:food_type/...` + `#creativeengineering:canned_source` | [foodtypetags.js](../kubejs/server_scripts/tags/foodtypetags.js) | DEPLOYED with 1.0.2. FO-02 to FO-08 |
| item_dict keys for the 216 foods it lacked + 32 tag keys | generated block at the end of [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) | BUILT, `node --check` clean |
| Classifier, checker and catalog | `tools/food_catalog.py` -> [food_catalog.md](food_catalog.md) | BUILT; `--check` passes |
| Canned Goods recipes removed, new empty-can recipes | [canning.js](../kubejs/server_scripts/cooking/canning.js) | DEPLOYED with 1.0.2. FO-09 to FO-13 |
| Multi-serving cans, chance can return, cans in recipes | patcherMod `food/CanServings` + `ItemStackCanServingsMixin`, CEConfig `[canning]`, [can_servings.json](../kubejs/generation_scripts/food/can_servings.json), [cantags.js](../kubejs/server_scripts/tags/cantags.js), NCH `cans` argument | DEPLOYED (1.0.2 notes "multi-serving cans"; 3.6). FO-14 to FO-22 |
| Food canning: raw veggie, fruit, meat | [canning.js](../kubejs/server_scripts/cooking/canning.js), 36 cans in [cans.js](../kubejs/startup_scripts/items/cans.js) (beetroot can sprite), servings in can_servings.json | DEPLOYED with 1.0.2. Cooked/prepared cans: next row |
| Food canning: cookies, cooked veggie/meat/fish, bread, salad, soup, golden | [tools/gen_can_types.py](../tools/gen_can_types.py) -> [can_types.json](../kubejs/generation_scripts/food/can_types.json); items/overrides in cans.js, recipes via `canSection()` in canning.js | BUILT 2026-10-04, 113 new cans + 13 Canned Goods cans re-valued; DEPLOYED with 1.0.6 (2026-10-14); single player 2026-10-11 logged `[cans] registered 113 generated cans`; recipes not checked in game. Pie, feast, meals, drinks, special not built |
| Tuning system (values, effects, SoL complexity, canned parity) | design in 3.5 | Not built. Decisions D1 to D6 (4.3) first |
| Fried egg furnace/smoker/campfire restored; cooking oil from pumpkin seeds too; own mixer ratio | [cooking.js](../kubejs/server_scripts/cooking/cooking.js), [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js), NCH `smoking` xp/time | DEPLOYED with 1.0.2. FO-23 to FO-27 |
| Vanilla egg -> `#forge:eggs` migration | list in 3.8 | TODO, not built |
| Every raw egg stacks to 64 | now a rule of the stack size policy ([stack_sizes.js](../kubejs/startup_scripts/changes/stack_sizes.js), `tools/gen_stack_sizes.py`); `item_properties.js` is removed in 1.0.5 | DEPLOYED with 1.0.6; single player 2026-10-11 logged `[stack_sizes] set 1052 stack sizes`. FO-28 |
| Glistering Melon Slice edible (Golden Melon Slice values, no effects) | [food_values.js](../kubejs/startup_scripts/cooking/food_values.js), `golden` type in food_types.json | built, scratch server PASSED 2026-10-22 (SQ-OF-12), not run in game (3.10). FO-29 |
| Seasoning JEI page (salting off the Crafting and Mixing pages) | patcherMod `compat/jei/SeasoningJEI` + `SeasoningCategory`, CEConfig `[jei] seasoningCategory` | BUILT 2026-10-21, in the patcherMod 1.1.02 jar installed 2026-10-23 (3.11). FO-30, FO-31 |
| Pack seasonings framework (NBT seasonings, combine rules, bonus on eating; test set cheese / onion / garlic on popcorn) | patcherMod `food/Seasonings`, `SeasoningRecipe`, `SeasoningTooltip`; [seasonings.js](../kubejs/startup_scripts/cooking/seasonings.js), [seasoning.js](../kubejs/server_scripts/cooking/seasoning.js), NCH `season` | BUILT 2026-10-23, patcherMod 1.1.02 jar in `mods/` (23:46, md5 57feef44); logic harness 11/11; scratch test queued (SQ-SN-00, 01) (3.12). FO-45 to FO-48 |
| Spice of Life pack config (10 foods to activate, reset on death, queue 256; tier bonuses still WIP) | `defaultconfigs/solapplepie-server.toml` = `configs_edited/server_config/solapplepie-server.toml` | Shipped with 1.1.01 through the deploy's `defaultconfigs`; the instance copy added 2026-10-22. New-world values: scratch server PASSED 2026-10-22 (SQ-CF-01). FO-32 |
| Bites from placed pies and cakes count for Spice of Life | patcherMod `food/PlacedFoodSol` + `mixin/ServerPlayerGameModePlacedFoodMixin`, [placed_food_slices.json](../kubejs/generation_scripts/food/placed_food_slices.json) | BUILT 2026-10-22, patcherMod 1.1.01 jar in `mods/` (11:06, md5 b5f60533) (4.2 finding 5); scratch server PASSED 2026-10-22 (SQ-SOL-01: load, 14 map entries, mixin); the bite: FO-43 in game |

**Open first:** [food_catalog.md](food_catalog.md) (every food), [food_types.json](../kubejs/generation_scripts/food/food_types.json)
(who is in which type), section 2.3 (Spice of Life), section 4.2 (what is broken today).

**Knobs today:**
- Move a food to another type: move its item_dict key between lists in `food_types.json`, then `/reload`.
- Exclude an edible item from typing: add its key under `excluded` with a reason.
- Mark a food as having a can: `canned` maps each can's key to the keys of what it holds.
- Spice of Life: the pack copy `defaultconfigs/solapplepie-server.toml` (new worlds; keep it equal to
  `configs_edited/server_config/solapplepie-server.toml`), then each world's `saves/<world>/serverconfig/` copy (2.3).
- Placed cakes for Spice of Life: block -> slice item in `kubejs/generation_scripts/food/placed_food_slices.json`
  (restart). Pies need no entry.

**Recipes:** reference the tags through the dict keys, e.g. `i.t_food_cooked_meat` (`#creativeengineering:food_type/cooked/meat`),
`i.t_food_raw` (every raw food), `i.t_canned_source`. The list is at the end of item_dict.js.

**Refreshing:** after adding a food mod, run `/dumpfood` in game, then `python3 tools/food_catalog.py`. New foods get a
type (printed so you can move them) and an item_dict key; nothing you placed moves. Restart for new keys.

---

## 1. Overview: how food works in 1.19.2 with these mods

- **Hunger (nutrition)** is how many half shanks a food fills. **Saturation** is a hidden buffer drained before the
  hunger bar; a food adds `hunger x saturation modifier x 2` points, never beyond the current hunger level. Steak is
  8 hunger and 0.8 modifier: 12.8 saturation points. The modifier is the value mods tune; anything above 1.2 (golden
  apple) is unusual, and past about 1.25 every bite simply tops saturation up to the hunger level.
- **Flags:** meat (wolves eat it), always edible (eat at full hunger), fast (16 ticks instead of 32). Effects come
  with a chance each. Items can also hand something back (bowl, bottle, tankard, stick, empty can).
- **Farmer's Delight effects** carry most of the value of prepared food: *Nourishment* stops hunger from draining
  while it lasts, except when saturation heals you (plated meals); *Comfort* gives natural regeneration however hungry you
  are (warm bowls). Their duration matters more than hunger.
- **Placed food:** cakes and pies are eaten bite by bite from the block, feasts (roast chicken, honey glazed ham,
  shepherd's pie, stuffed pumpkin, rice roll medley, Nether's Delight hoglin) are served into bowls.
  Create: Central Kitchen turns the vanilla pumpkin pie into a placed pie, so `minecraft:pumpkin_pie` isn't edible as an item here.
- **Salt** (`salt` mod): a salted food (NBT on any of the 117 foods in `salt:can_be_salted`) gives +2 hunger and +0.1
  modifier, flat, from `config/salt-common.toml` (`SaltingFoodValues` can set it per item).
- **Survivality** `more_snacks`: cookies, sweet berries and glow berries are eaten faster.
- **AppleSkin** shows hunger and saturation in tooltips and on the HUD, from the same food properties, so tuned
  values show up there automatically.
- **Spice of Life** (the jar is "Apple Pie Edition", the code is *Sweet Potato*, `com.tarinoita.solsweetpotato`):
  rewards a varied diet with permanent bonuses. Mechanics in 2.3.

Where the 856 foods come from (2026-10-02 export): Fruits Delight 107, Farmer's Delight 74, Delightful 46, Golden
Food 45, Canned Goods 39, vanilla 39, Neapolitan 38, Vinery 38, Create Confectionery 37, the pack's own cans 36,
Rustic Delight 34, More Delight 31, Farmer's Respite 30, Brewin' and Chewin' 29, Abnormals Delight 28, Alex's Delight
23, Create: Central Kitchen 22, Alex's Mobs 20, Ender's Delight 20, Corn Delight 19, Create: Factory 19, Nether's
Delight 14, Upgrade Aquatic 11, Quark Delight 9, 16 more mods with 1 to 7 each (Untitled Duck Mod 4).

## 2. Reference

### 2.1 Food types

One type per food. A `/` nests a type under its parent, and the tag of a nested type is also in every parent:
`raw/meat/sliced` is in `raw/meat`, `raw` and the root `#creativeengineering:food_type`. Counts are from the
2026-10-02 export; values are hunger and modifier medians (the full spread is in the catalog summary).

| Type | Covers | Foods | Median hunger / mod | Examples |
|---|---|---:|---|---|
| `raw/veggie` | raw vegetables, herbs, fungi, grain | 14 | 1.5 / 0.3 | carrot, cabbage, onion, bell peppers, gongylidia |
| `raw/veggie/sliced` | cut raw vegetables | 4 | 1.5 / 0.35 | cabbage leaf, potato slices, pumpkin slice |
| `raw/fruit` | fruit, berries, nuts | 41 | 2 / 0.3 | apple, sweet berries, strawberries, grapes, acorn, Fruits Delight's fruits and slices |
| `raw/meat` | raw meat, whole | 15 | 3 / 0.3 | beef, porkchop, ham, raw bison, hoglin loin |
| `raw/meat/sliced` | cuts, chops, mince, bacon, fillets | 15 | 2 / 0.3 | minced beef, bacon, chicken cuts, duck fillet |
| `raw/fish` | fish and seafood, whole | 13 | 2 / 0.3 | cod, salmon, pike, crab leg, calamari |
| `raw/fish/sliced` | fish slices | 7 | 1 / 0.1 | cod slice, pike slice, shulker filet |
| `cooked/veggie` | baked, roasted, dried, pickled vegetables, cooked rice | 17 | 4 / 0.4 | baked potato, dried kelp, roasted peppers, kimchi |
| `cooked/veggie/sliced` | cooked cut vegetables | 1 | 3 / 0.6 | baked potato slices |
| `cooked/fruit` | cooked or dried fruit | 4 | 4 / 0.3 | dried banana |
| `cooked/meat` | cooked meat, whole; patties, jerky, glazed cuts | 20 | 6.5 / 0.8 | steak, smoked ham, beef patty, glazed porkchop |
| `cooked/meat/sliced` | cooked cuts | 10 | 3.5 / 0.7 | cooked bacon, cooked mutton chops |
| `cooked/fish` | cooked fish, whole | 10 | 6 / 0.68 | cooked cod, cooked pike, kippers |
| `cooked/fish/sliced` | cooked slices | 6 | 3 / 0.65 | cooked salmon slice |
| `egg` | egg on its own | 4 | 4 / 0.6 | fried egg, scrambled eggs, boiled emu egg |
| `baked` | bread and buns | 12 | 5 / 0.4 | bread, sourdough bread, banana bread, adzuki bun |
| `dairy` | cheese | 2 | 4 / 1 | flaxen and scarlet cheese wedges |
| `sweet` | cake/pie slices, cookies, candy, chocolate, ice cream | 185 | 4 / 0.35 | cake slice, cookie, candies, waffles, ice cream |
| `handheld` | sandwiches, burgers, wraps, rolls, skewers, stuffed veg | 52 | 10 / 0.8 | hamburger, kelp roll, barbecue stick, pizza slice |
| `salad` | salads | 11 | 6 / 0.6 | mixed salad, fruit salad, potato salad |
| `soup` | soups, stews, broths | 31 | 10 / 0.7 | beef stew, chicken soup, bone broth, rabbit stew |
| `meal` | plated meals | 65 | 12 / 0.8 | steak and potatoes, fried rice, pasta, curry |
| `meal/feast` | servings from a placed feast | 10 | 14 / 0.75 | roast chicken, shepherd's pie, stuffed hoglin |
| `drink` | non-alcoholic drinks | 62 | 0 / 0 | coffee, milkshakes, tea, honey bottle, soda |
| `drink/alcohol` | wine, beer, spirits | 41 | 1 / 0 | Vinery wines, Brewin' and Chewin' boozes |
| `golden` | golden apple tier | 25 | 4 / 1.2 | golden apple, golden carrot, Golden Food's golden_* |
| `golden/enchanted` | notch apple tier | 25 | 8 / 8 | enchanted golden apple, Golden Food's enchanted_* |
| `ingredient` | edible intermediates | 42 | 4 / 0.3 | dough, raw pasta, tomato sauce, cooking oil, jam |
| `hazard` | eaten for the downside or in a pinch | 11 | 1 / 0.2 | rotten flesh, spider eye, pufferfish, soap |
| `special` | magic, joke or utility food | 15 | 1 / 0.2 | chorus fruit, mana cookie, icicle, dog food |
| `canned` | Canned Goods cans and the pack's own food cans | 72 | 5 / 0.8 | canned steak, canned rabbit stew |
| (excluded) | edible in code, not food | 14 | | Central Kitchen's `incomplete_*` assembly items, the Spice of Life lunchboxes |

"Sliced" is a form, not a food group: it only exists under raw and cooked, where a cut changes the numbers (a cod
slice is 1 hunger, a cod is 2). How the classifier decides is in 3.2.

### 2.2 Recipe chains of base ingredients

For every raw base the catalog lists: compressed (9 into a crate, sack, basket or block), cooked (furnace, smoker,
campfire), sliced (cutting board, Create saw), sliced and cooked (either order), its cans, and how many recipes use
it. Examples from [food_catalog.md](food_catalog.md#base-ingredients-recipe-chains):

| Base | Compressed | Cooked | Sliced | Sliced, cooked | Can | Used in |
|---|---|---|---|---|---|---:|
| Raw Beef 3/0.3 | | Steak 8/0.8 | Minced Beef 2/0.3 | Beef Patty 4/0.8 | canned_beef | 28 |
| Raw Cod 2/0.1 | | Cooked Cod 5/0.6 | Cod Slice 1/0.1 | Cooked Cod Slice 3/0.5 | canned_fish | 37 |
| Potato 1/0.3 | potato crate | Baked Potato 5/0.6 | Potato Slices 1/0.3 | Baked Potato Slices 3/0.6 | canned_potatos, canned_baked_potato | 63 |
| Carrot 3/0.6 | carrot crate | | | | (none, see 4.2) | 75 |
| Raw Pike 3/0.3 | | Cooked Pike 8/0.8 | Pike Slice 1/0.3 | Cooked Pike Slice 4/0.8 | | 33 |
| Green Bell Pepper 1/0.1 | pepper crate | Roasted 5/0.8 | | | | 30 |

Machine routes (IE squeezer and bottler, Create mixing to fluids) aren't in the chains: IE's recipe classes don't
expose their inputs to the export (4.1).

### 2.3 Spice of Life: how it rates a meal

Read from the bytecode of `solapplepie-1.19.2-2.2.0.jar` (2026-09-28).

**What gets tracked.** Only a finished eat of an item that has food properties
(`LivingEntityUseItemEvent.Finish` + `isEdible()`), plus one special case: a bite of the vanilla cake block, recorded
as Farmer's Delight's cake slice. Bites from other placed cakes and pies, potions, milk, and the 21 drinks with no
food value never count.
//TODO: Update SOL with placed 

**The queue.** The player remembers the last `queueSize` (32) foods. Each distinct food appears once, with its age
in meals: eating a food resets its age to 0 and ages everything else by 1; a food reaching age 32 drops out.
Eating the same thing twice adds nothing new, it only refreshes that food.

**Diversity** = the sum over remembered foods of `complexity x age penalty`. With the defaults
(`startDecay 0`, `endDecay 32`, `minContribution 0`) the penalty is `1 - age/32`: a food eaten just now counts fully,
one eaten 16 meals ago counts half.

**Complexity** ("meal quality") is what a food is worth:
1. The value in the config's `complexityUnparsed` list, if the item is there (tags not supported).
2. Otherwise from its food properties: `q = (hunger + hunger x modifier) / 2`, then
   `complexity = q / 5` when `q < 5`, else `1 + 4 x log10(q - 4)` (times `defaultContribution`, 1.0).

| Food | Hunger / mod | q | Complexity |
|---|---|---:|---:|
| Carrot | 3 / 0.6 | 2.4 | 0.48 |
| Bread | 5 / 0.6 | 4.0 | 0.80 |
| Cooked Chicken | 6 / 0.6 | 4.8 | 0.96 (just below the curve's break) |
| Steak | 8 / 0.8 | 7.2 | 3.02 by formula, **2** from the default config list |
| Beef Stew | 12 / 0.8 | 10.8 | 4.33 |
| Roast Chicken (feast) | 14 / 0.75 | 12.25 | 4.66 |
| Enchanted golden anything (Golden Food) | 8 / 8 | 36 | 7.02 (notch apple: 10 from config) |
| Canned Suspicious Stew | 26 / 42 | 559 | 11.98 |

Medians by type today: raw 0.1 to 0.4, cooked 0.5 to 1.8, handheld 3.4, soup 3.5, meal 4.3, feast 4.7, golden 0.9,
enchanted golden 7.0, canned 7.8. The formula rewards the saturation modifier, which is why the cans (4.2 finding 1)
and Golden Food's enchanted foods top the list.

**Benefits.** 17 thresholds (2, 5, 7, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100). The defaults give
Speed I at 2, then +1 max health (half a heart) at each later threshold, with Haste, Strength (up to III at 90),
Regeneration, Luck, armor toughness and full knockback resistance (at 40) along the way.

**What diets reach today** (steady state of a repeating rotation of distinct foods; diversity for `k` foods of
average complexity `c` is `c x (k - k(k-1)/64)`):

| Diet | Foods | Diversity now | Tiers now | With the proposed values (3.5) |
|---|---:|---:|---:|---:|
| Steak only | 1 | 2.0 | 1 | 0.5 |
| Vanilla 5 (steak, baked potato, bread, chicken, apple) | 5 | 4.8 | 1 | 2.3 |
| Farmer's Delight kitchen (4 meals, 2 soups, 2 handhelds, 2 cooked, a slice, a drink) | 12 | 34.5 | 8 | 15.1 |
| Chef (12 meals, 6 soups, 6 handhelds, 4 sweets, 4 cooked) | 32 | 48.9 | 11 | 26.9 |
| 16 prepared meals | 16 | 48.8 | 11 | 30.6 |
| 16 enchanted golden foods | 16 | 86.0 | 15 | 12.3 |
| 16 cans | 16 | 101.0 | **17 (all)** | about 8 (cans at their source's value) |

So today the best way to max Spice of Life is to eat 16 different cans, and the second best is Golden Food.

**Config is per world.** The server config lives in each world's `serverconfig/`. Until 2026-10-22 there was no pack
copy in `defaultconfigs/`, so every new world got the mod defaults (since then: `defaultconfigs/solapplepie-server.toml`,
shipped with 1.1.01). WorldC has the defaults; Workspace was hand-edited
(queue 1000, 10 foods before any benefit, reset on death, and 31 benefit entries for 17 thresholds).

### 2.4 Canned Goods

Every can is 2 of a food plus an iron ingot, and eating one gives the empty can back (smelts to an ingot). 34 cans
hold a food; `canned_food` comes from the butcher, and Soda, Reef-Cola, Oculemonade and the Hot Hot Hotpot are drinks
or dishes of their own. Foods with a can are in `#creativeengineering:canned_source` and have the Canned column
filled in the catalog. Values and parity per can: [food_catalog.md](food_catalog.md#canned-goods-can-vs-what-goes-in).
Some cans apply effects in code on top of their food properties (golden apple cans, pufferfish, spider eye,
suspicious stew and others); the catalog lists them as `code:`.

## 3. What the pack changes

### 3.1 `/dumpfood` (works)

`kubejs/server_scripts/_lib/dump_food.js`, op level 2, same pattern as `/dumpgear`. It reads the live registries, so
it sees values after every mod and KubeJS edit. Output in `kubejs/exported/food/`: `foods.json` (every edible item:
hunger, modifier, saturation points, flags, effects with chances, eat/drink animation and time, what it leaves
behind, class chain, tags, Spice of Life complexity and whether it came from the config), `food_blocks.json`,
`drinks.json` (drinkable, no food), `recipes.json` (every recipe with an item or fluid output: raw ingredient JSON so
tags survive, outputs with chances, fluid inputs, heat, cook time, container). It also refreshes the item tag export.
Every call into a mod class goes through `guardThrowable`, so one odd item or recipe can't end the command silently.

### 3.2 Food types (DEPLOYED with 1.0.2)

- **Assignments** live in [food_types.json](../kubejs/generation_scripts/food/food_types.json): type -> item_dict keys,
  plus `excluded` (key -> reason) and `canned` (can key -> source keys). It sits in `generation_scripts/` beside the
  worldgen configs because the tuning config will join it there.
- **Tags:** [foodtypetags.js](../kubejs/server_scripts/tags/foodtypetags.js) reads the file on every tag load and adds
  each type through `global.tags.addTag`, so the parents fill themselves. Tag ids come from the `i.t_food_*` dict
  keys. It warns when a key is in two types, when a key isn't an item id in item_dict (new keys before a restart),
  and logs `[food types] 615 foods in 31 types, 33 canned sources`.
- **item_dict:** 216 foods had no key (Golden Food, Canned Goods, Neapolitan, Rustic Delight, Abnormals Delight,
  Nether's Delight, Upgrade Aquatic, Berry Good, Spice of Life had no entries at all). They now have keys in a
  generated block before `// Validation`, with the two can items the canning recipes will need (`empty_can`,
  `crushed_empty_can`) and the 32 tag keys. Bare path when free, `<ns>_<path>` on a clash
  (`neapolitan_banana`, `abnormals_delight_chocolate_cake_slice`). Only these namespaces' foods were added, not their
  blocks.
- **Classifier** (`tools/food_catalog.py`, only for foods in no type yet): exclusions, then a hand override table,
  then fixed lists (hazard, special), then name and class rules in this order: canned, enchanted golden, golden,
  feast serving (from the feast blocks), alcohol (Booze/DrinkBlock classes, Vinery's wine tag), ingredient, soup,
  drink (drink animation or name), salad, sweet, handheld (not when it comes in a bowl), egg, meal (name or a bowl),
  baked, dairy, and last raw/cooked. Cooked means a cooked-sounding name or being the output of a furnace, smoker or
  campfire recipe; the group is fruit, fish, meat (the meat flag or a meat name) or veggie; sliced needs a cut name.
- **Canned map:** from the recipes, corrected by what each can holds (4.2 finding 2): `canned_carrots` holds pumpkin
  pie, `canned_pumpkin_pie` and `canned_golden_carrot` hold golden carrots, `canned_mushrooms` holds mushroom stew.

### 3.3 Cans (built)

[canning.js](../kubejs/server_scripts/cooking/canning.js) removes all 35 Canned Goods food-can recipes (the 34
"2 x food + iron ingot" cans and the Hot Hot Hotpot), its two can recipes (ingot -> empty can, ingot -> crushed can)
and the can -> iron ingot smelting and blasting (with 4 or more cans per ingot, that would duplicate iron). No can
of food is craftable until food canning is rebuilt on the type tags. Kept: Soda, Reef-Cola, Oculemonade and Mini
Fizz (drinks made in an empty can), canned dirt and the canned golem.

New empty-can recipes, cans per ingot rising with the station:

| Station | Recipe | Cans per ingot |
|---|---|---:|
| Stonecutter | 1 iron ingot -> 4 | 4 |
| Crafting table | 2 iron plates stacked (`#forge:plates/iron`, so Create's iron sheet works) -> 12 | 6 |
| Create deployer | iron plate deployed onto an iron plate -> 12 | 6 |
| IE Engineer's Workbench (components blueprint) | 4 IE iron plates -> 32 | 8 |

Side effect: the crushed can has no recipe now, so canned dirt and the canned golem (both need crushed cans) can't be
made. The crushed can -> iron nugget recipes are still there.

### 3.4 Catalog tool (built)

`python3 tools/food_catalog.py` (classify new foods, write the JSON, the item_dict block and the catalog) and
`--check` (writes nothing, exits 1 if a food has no type or key, a key is in two types, or a typed item vanished).
The catalog also writes `kubejs/exported/food/catalog.json` (per food: type, values, flags, effects, code effects,
current and formula SoL complexity, cans), which the tuning step will start from.

### 3.5 Tuning system (design, not built)

Goal: every food's hunger, saturation, effects and Spice of Life value follow its type, with named exceptions, and
every can is worth what went into it. No recipe changes in this step.

**One config.** `kubejs/generation_scripts/food/food_tuning.json`:
- `types`: per type a hunger band, a modifier band, the Spice of Life complexity, and optional default effects
  (for example Nourishment seconds per hunger point for meals) and salt bonus.
- `items`: per item_dict key, any of `hunger`, `modifier`, `alwaysEat`, `fast`, `meat`, `effects`, `sol`, or
  `exempt: true` (keep the mod's values; the band doesn't apply).
- `canned`: the parity rule (below).
The tool gets a `--report` that lists every food outside its band and seeds `items` with the ones you accept.

**Applying values: startup.** `startup_scripts/food/food_tuning.js` on `ItemEvents.modification`:
`e.modify(id, item => item.setFoodProperties(food => { food.hunger(n); food.saturation(m); ... }))`. KubeJS's
consumer form starts from the item's current properties (bytecode-checked: `new FoodBuilder(existing)`), so only the
named fields change. Needs a restart. Changes show up in AppleSkin, the mods' own tooltips and Spice of Life's
formula at once. Both one-argument overloads of `setFoodProperties` (`FoodProperties` and a consumer) work in this
Rhino build: cans.js uses the consumer form on the Canned Goods cans and food_values.js / alcohol_drinks.js pass a
built `FoodProperties`, and the 2026-10-20 `/dumpfood` shows their values. The consumer form keeps the old meat /
always-edible / fast flags and effects unless the script sets them (`meat(false)` etc.).

**Code-side effects are separate.** Canned Goods' procedures, Brewin' and Chewin's Tipsy, Vinery's age-scaled wines,
Neapolitan's healing items and Farmer's Delight's hot cocoa / milk bottle act in `finishUsingItem`, not through food
properties, so the step above can't change them. Options per mod: an `ItemEvents.foodEaten` handler that corrects the
effect a tick later, or a patcherMod mixin that skips the procedure. Decision D4.

**Spice of Life: server.** `server_scripts/food/sol_complexity.js` fills
`com.tarinoita.solsweetpotato.ConfigHandler.complexityMap` (public, a plain HashMap, rebuilt only at server start)
from the type values and item overrides on `ServerEvents.loaded`, then calls `ConfigHandler.syncConfig(player)` for
anyone online; players who log in later get the map in the mod's own login sync. The dump already loads these
classes, so reflection access is proven. Thresholds and benefits go into a pack
`defaultconfigs/solapplepie-server.toml` (plus the `configs_edited/server_config` copy), and existing worlds' copies
are updated by hand.

**Proposed starting values** (all knobs; "outside" counts today's foods outside the band):

| Type | Hunger | Modifier | SoL | Outside |
|---|---|---|---:|---:|
| raw/veggie, raw/veggie/sliced | 1-3, 1-2 | 0.3-0.6, 0.3-0.4 | 0.25 | 5/12, 1/3 |
| raw/fruit | 2-4 | 0.1-0.3 | 0.25 | 6/21 |
| raw/meat, raw/meat/sliced | 2-4, 1-2 | 0.2-0.4 | 0.25 | 5/13, 5/15 |
| raw/fish, raw/fish/sliced | 1-3, 1 | 0.1-0.3 | 0.25 | 3/13, 0/7 |
| cooked/veggie, sliced | 3-6, 2-3 | 0.5-0.8, 0.5-0.7 | 0.5 | 7/13, 0/1 |
| cooked/fruit | 3-5 | 0.3-0.6 | 0.5 | 0/1 |
| cooked/meat, sliced | 5-8, 3-4 | 0.6-0.8 | 0.5 | 7/18, 5/10 |
| cooked/fish, sliced | 5-6, 2-3 | 0.6-0.8, 0.5-0.8 | 0.5 | 4/10, 2/6 |
| egg | 3-6 | 0.4-0.6 | 0.5 | 1/4 |
| baked | 5-7 | 0.6-0.8 | 0.75 | 4/7 |
| dairy | 3-4 | 0.6-1.0 | 0.5 | 0/2 |
| sweet | 1-6 | 0.1-0.5, fast | 0.5 | 62/136 |
| handheld | 8-12 | 0.6-0.9 | 1.5 | 24/38 |
| salad | 5-8 | 0.5-0.7 | 1.0 | 3/8 |
| soup | 6-14 | 0.6-0.8, Comfort | 1.5 | 6/26 |
| meal | 10-14 | 0.75-0.9, Nourishment | 2.5 | 19/46 |
| meal/feast | 12-16 | 0.75-0.9, Nourishment | 3.0 | 2/7 |
| drink, drink/alcohol | 0-6 | 0-0.6, 0-0.3 | 0.25 | 7/27, 0/41 |
| golden | 4-6 | 1.2 | 0.5 | 1/25 |
| golden/enchanted | 4-8 | 1.2 | 1.0 | 25/25 |
| ingredient | 1-3 | 0.1-0.3 | 0 | 9/18 |
| hazard, special | as they are | as they are | 0, 0.25 | |
| canned | parity | parity | the source's | |

213 of 553 banded foods are outside these bands, most by a little (0.85 against 0.8). The recommendation is to
review, not snap (D1): hard rules everywhere (modifier at most 1.2 outside golden foods, always-edible only for
golden foods, drinks and sweets), bands as a report, exemptions for foods that are deliberately different.

With those complexities the thresholds need rescaling. A ladder of 1, 2, 3, 4, 5, 6.5, 8, 9.5, 11, 12.5, 14, 16, 18,
20, 22, 24, 26 gives: steak only 0 tiers, vanilla 5 foods 2, the Farmer's Delight kitchen 11, the chef's 32 foods
all 17, 16 enchanted golden foods 9 (D3).

**Canned parity rule.** A can of 2 x source gets: hunger = the sum of the sources' hunger; modifier = the sources'
combined saturation points / (2 x hunger), which for two of the same food is the source's own modifier; the source's
effects at the same level (durations doubled, D4); the source's meat flag; the source's Spice of Life value (D5).
Canned steak becomes 16 / 0.8 (25.6 saturation points) instead of 16 / 24 (768). Placed-food sources count the whole
block (2 cakes = 28 hunger, 5.6 points; 2 pumpkin pies = 24 hunger, 14.4 points).

**Salt (optional).** Generate `SaltingFoodValues` per type (for example +1 / +0.05 on raw food, +2 / +0.1 on meals,
0 on sweets and drinks) instead of the flat +2 / +0.1 that triples a 1-hunger raw food.

**Workflow once built:** `/dumpfood` -> `python3 tools/food_catalog.py --report` -> edit `food_tuning.json` ->
restart (values) or `/reload` plus rejoin (SoL values, if the script re-applies on reload) -> `/dumpfood` again and
compare the catalog.

### 3.6 Multi-serving cans (DEPLOYED with 1.0.3)

A can holds several servings. Eating one takes a serving; the last serving leaves an empty can, a crushed can
or nothing. A KubeJS shaped or shapeless recipe can use a can the same way: one serving per craft.

**How many servings.** [can_servings.json](../kubejs/generation_scripts/food/can_servings.json) maps item_dict
keys to a count. [cantags.js](../kubejs/server_scripts/tags/cantags.js) turns it into
`#creativeengineering:cans/servings/<n>`, which chains up to `#creativeengineering:cans/servings` and
`#creativeengineering:cans` (keys `i.t_can_servings`, `i.t_cans`). `/reload` applies a change. A can that isn't
listed keeps the Canned Goods behaviour: one serving, always an empty can back. Today 247 cans are listed, from
1 to 10 servings (spread in [cans.md](cans.md) 2.1): most generated cans hold 3 to 8 (a can of n x source holds n
servings), golden cans 10, `canned_food` and the Hot Hot Hotpot 2; Soda, Reef-Cola and Oculemonade hold 1, so
they only get the return roll. Mini Fizz isn't a can (it hands nothing back) and isn't listed.

**Value per serving.** Each serving gives the can's full food value and effects. Canned Goods sized its values
for 2 foods, so at 2 servings a can is now worth twice as much until the parity tuning (3.5) runs. The parity
rule fits servings well: a can of n x source holds n servings, each worth one source.

**Servings, not durability.** The servings left are an NBT int (`CanServings`), written only on a can that has
been opened. Real durability would force a stack size of 1 (cans stack to 64) and make cans enchantable. A full
can carries no NBT, so new cans stack as before. An opened can draws a bar like a damaged tool's (width and
green-to-red colour from the servings left), and every can with more than one serving has a tooltip line
`Servings: x / y`.

**Eating.** A mixin on `ItemStack#finishUsingItem` runs the can's own `finishUsingItem` on a one-can copy, so
food, effects and Canned Goods' procedures (golden apple cans, Soda and so on) apply to each serving; the empty
can that code hands back is discarded. Then one serving is taken:
- from the can in hand if it is already open;
- otherwise from the most-eaten open can of the same kind in the inventory, so a stack of full cans doesn't
  open a new can per bite;
- otherwise from one can of the held stack, and the opened can goes to the inventory (or drops if it's full).
Creative mode takes nothing. Spice of Life counts each serving as eating that can.

**Return roll.** On the last serving, CEConfig `[canning]`: `emptyCanChance` 0.7 empty can,
`crushedCanChance` 0.25 crushed can, the rest nothing. If the two add up past 1, the crushed chance is cut to
fit. `emptyCanItem` and `crushedCanItem` name the items. `enabled = false` turns everything off. Crushed cans
now have a source again, so canned dirt and the canned golem can be made again, and crushed can -> iron nugget
recycles them.

**Recipes.** `R.shaped(output, pattern, keys, e, id, damage, cans)` and
`R.shapeless(output, input, e, id, damage, replace, cans)`: `cans = true` covers every can slot, or pass a filter
or an array of filters (item, `#tag`, slot index). NCH attaches the KubeJS custom ingredient action
`creativeengineering:can_serving` (Java: `compat/KubeJSCanAction`), which leaves the can with one serving less
in the grid, or rolls the return on the last serving. Example in canning.js:
`R.shapeless(Item.of(i.beef_cooked), [i.canned_beef], e, 'uncan_beef', null, null, true)`.
KubeJS empties its custom action map on every server script load, so NCH registers the callback again on
each call; common setup also registers it for clients of a dedicated server.

**Limits.**
- A grid slot holding a stack of full cans opens a new can per craft, and the opened can goes to the
  inventory (vanilla's remainder rule). To drain one can, put it in the grid on its own.
- Only KubeJS shaped/shapeless recipes made through NCH with `cans` use servings. Other recipes (Create
  mixing, mechanical crafting, other mods' recipes) use up the whole can as before.
- An auto-feeder that calls `Item#finishUsingItem` directly, not through the stack, eats the whole can as one
  serving.
- On a dedicated server the client briefly guesses nothing for a last-serving craft; the server's roll then
  shows.
- Cans in play come from the canning recipes (every food type section, [can_parity.md](can_parity.md)), loot,
  the butcher (`canned_food`) and the drink recipes. (Until the generated cans, 2026-10-04, none of the food
  cans had a canning recipe.)

### 3.7 Cooking oil and fried eggs (DEPLOYED with 1.0.2)

**What was wrong (found 2026-09-29).** Only one recipe made a fried egg: Rustic Delight's egg + cooking oil in the
Cooking Pot (plus its Slice & Dice mixer copy). Abnormals Delight ships its own `farmersdelight:fried_egg`,
`fried_egg_from_smoking` and `fried_egg_from_campfire_cooking` with a `forge:not mod_loaded incubation` condition,
replacing Farmer's Delight's. Incubation is installed, so none of the three load. cooking.js then removes
Incubation's own three fried egg recipes as redundant. Everything on `#forge:cooked_eggs` (egg sandwich, bacon and
eggs, noodle soup, the deploying step in cooking.js) needed a cotton farm first.

**Fix.** cooking.js re-adds Farmer's Delight's three recipes through NCH with its values: furnace 200 ticks, smoker
100, campfire 600, 0.35 xp each, input the vanilla egg (as Farmer's Delight had it; see 3.8). Ids are
`creativeengineering:smelting|smoking|campfirecooking/...`. Incubation's fried egg stays removed. NCH `smoking` now
takes `xp` and `cookingtime` like `smelting` (defaults 0 and 100, the old behaviour).

**Cooking oil.** `rusticdelight:cooking_oil` is a drinkable bottle (stacks to 16, 2 hunger, 50% Nausea 20 s, gives the
bottle back). It feeds fried chicken, fried calamari, fried mushrooms, fruit beignet, spring rolls, the oil fried
egg and an oil-for-egg fried rice.

| Change | Before | Now |
|---|---|---|
| `#rusticdelight:cooking_oil_ingredients` | cotton seeds | cotton seeds, pumpkin seeds (`i.t_cooking_oil_ingredients`) |
| Cooking Pot | 6 seeds + bottles -> 2 oil (`rusticdelight:cooking/cooking_oil`) | same recipe, id `creativeengineering:farmersdelightcooking/cooking_oil_manual_only` |
| Create mixer (heated) | Slice & Dice copy: 6 seeds + 1 bottle -> 2 oil | 3 seeds + 1 bottle -> 1 oil (`creativeengineering:createmixing/cooking_oil_heated_mixing`) |

Slice & Dice builds its mixing copies at the end of KubeJS's recipe event (a mixin on `RecipesEventJS.post`), so
`R.remove` on a `sliceanddice:` id finds nothing. It skips any Cooking Pot recipe whose id ends in `_manual_only`,
so the pot recipe is removed and re-added under that suffix, and the mixer recipe is written by hand. The other
oil recipes keep their Slice & Dice copies.

### 3.8 Vanilla egg -> `#forge:eggs` migration (TODO, not built)

Recipes that take `minecraft:egg` as an item don't accept the other eggs. `#forge:eggs` holds 12 items: vanilla,
turtle (Supplementaries), the Alex's Mobs crocodile, emu and terrapin eggs, 5 Quark parrot eggs (Quark Delight), and
Untitled Duck Mod's duck and goose eggs (since 1.0.2). From the 2026-09-28 dump (it can't read IE machine recipes, so
rescan first; a 2026-10-21 jar rescan, with current ids, is in
[briefs/food_types-and-tuning.md](briefs/food_types-and-tuning.md) #6: the Brewin' 4.5.0 port already uses
`#forge:eggs` for the omelet and egg grog, Dread Nog takes a turtle egg, and its bacon quiche id is `quiche_from_bacon`):

| Recipe | Migrate? |
|---|---|
| `creativeengineering:` smelting, smoking and campfire fried egg (3.7) | yes |
| `rusticdelight:cooking/fried_egg_from_cooking_oil` | yes |
| `brewinandchewin:cooking/vegetable_omelet` | yes |
| `brewinandchewin:quiche_bacon`, `quiche_mushroom` | yes |
| `brewinandchewin:fermenting/egg_grog`, `dread_nog` | yes |
| `create_factory:cake_paste`, `create_deep_dark:compacting_echo_cake_base` | yes |
| `incubation:chicken_egg_crate` | no: storage block, must stay vanilla eggs |
| `mca:potion_of_feminity`, `potion_of_masculinity` | probably no: not food |

Notes for the build:
- Use `R.replaceInput` with a per-recipe id filter, not a pack-wide swap, so the crate keeps its egg.
- Slice & Dice copies (vegetable omelet, oil fried egg) follow on their own: they're built from the source recipe
  after KubeJS runs.
- Decide first whether turtle and parrot eggs belong in `#forge:eggs` at all. Turtle eggs are placeable blocks,
  and parrot eggs hatch parrots, so a fried egg from them may not be wanted.

### 3.9 Egg stack size (DEPLOYED with 1.0.6)

Since 2026-10-05 this is the `egg` rule of the stack size policy: `tools/gen_stack_sizes.py` gives every item in
`#forge:eggs` 64, and [stack_sizes.js](../kubejs/startup_scripts/changes/stack_sizes.js) applies the generated
`stack_sizes.json` (see [stack_sizes.md](stack_sizes.md)). `item_properties.js`, which did it before, is deleted on both
machines by the 1.0.5 removals. The rule covers all 12 raw eggs in
`#forge:eggs`: vanilla egg, turtle egg, the Alex's Mobs crocodile, emu and terrapin eggs, the 5 Quark parrot
eggs, and the Untitled Duck Mod duck and goose eggs. They are listed by item_dict key, because tags aren't loaded yet when the startup event runs: if a mod adds an
egg to `#forge:eggs` later, rerun `tools/gen_stack_sizes.py` after a tag dump. Not changed: cooked eggs (fried eggs, boiled emu egg), the
dragon egg and spawn eggs.

### 3.10 Glistering Melon Slice edible (BUILT 2026-10-21)

Vanilla's Glistering Melon Slice (a brewing ingredient) was not food. [food_values.js](../kubejs/startup_scripts/cooking/food_values.js)
now copies Golden Food's `golden_melon_slice` onto it, the golden food made from the same base item: 4 hunger, 1.2
modifier (4.8 saturation), always edible, like every `golden` food (they are all 4 / 1.2 whatever the base). Effects are
left out for now: the copy entry is `{ from: 'golden_melon_slice', effects: false }`, which rebuilds the properties
without them. It is in the `golden` type in food_types.json, so it joins `#creativeengineering:food_type/golden`.
Brewing is unchanged. Note: in this pack `minecraft:golden_carrot` is not effect-free: the 2026-10-20 `/dumpfood` has it at
4 / 1.2 with Regeneration II 5 s and Absorption 2 min, the same as the golden apple. Until the next `/dumpfood`,
`food_catalog.py --check` reports the slice as "no longer edible" (the export predates the change). FO-29.

### 3.11 Seasoning JEI page (BUILT 2026-10-21)

Salt shows its one `salt:salting` special craft as a Crafting-page extension, and Create: The Salt adds 65
`create:mixing` recipes (food + `#forge:salts` -> the same food with `{Salted:1b}`), one Mixing page each. patcherMod's
`SeasoningJEI` (a `@JeiPlugin`, client only) moves them onto one **Seasoning** page (`creativeengineering:seasoning`,
icon: salt; catalysts: crafting table, mechanical mixer):
- the salting craft: one page, machine slot = crafting table, every `#salt:can_be_salted` food + salt -> the salted
  food. Food and output cycle together (a JEI focus link), so "uses of" / "recipes for" a food shows that food only;
- Create: The Salt's mixes: one page, machine slot = mechanical mixer, the foods those mixes cover. A mix is folded in
  only if its result carries `Salted`, it has exactly two ingredients and one of them matches the result's item;
- any crafting recipe whose id starts `creativeengineering:shapeless/season/` with 2 or 3 ingredients (the hook for
  `food/seasoning #3`: name pack seasoning crafts `R.shapeless(..., 'season/<name>')`), a page each.

`onRuntimeAvailable` hides exactly the recipes it re-showed (by recipe id) from the Crafting and Mixing pages; if
building the page throws, nothing is hidden. The crafts themselves are unchanged (the grid and basin still do them).
Salted items keep Salt's own "Salted: +N nutrition" tooltip, so the page adds none. Off switch: CEConfig
`[jei] seasoningCategory = false` (next JEI start). Compiles against `development/patcherMod/libs/salt-1.19.2-1.2.4.1.jar`
(git-ignored, copied from `mods/`). FO-30, FO-31.

### 3.12 Pack seasonings framework (BUILT 2026-10-23)

`food/seasoning #3`'s mechanism, separate from Salt's `Salted`. A seasoning is NBT on the food,
`Seasonings:["creativeengineering:garlic", ...]`, kept sorted so the order they went on doesn't split stacks.
- **Definitions:** [seasonings.js](../kubejs/startup_scripts/cooking/seasonings.js) (`global.seasoning`, startup, both
  sides) holds each seasoning once: id, display name, grid item, bonus (`nutrition` hunger points, `saturation` points
  capped at the food level, `heal` health points, timed `effects`) and `combines` (keys it may share a food with;
  either side listing the other is enough, unlisted pairs don't combine). It passes each one to patcherMod
  `food/Seasonings.define` as JSON. The client lang hook [seasoning_lang.js](../kubejs/client_scripts/seasoning_lang.js)
  adds the names (`seasoning.<ns>.<path>`).
- **Craft:** recipe type `creativeengineering:seasoning` (`food/SeasoningRecipe`, special shapeless): one food
  matching `food` + one of each listed seasoning's item gives the same food (all NBT kept, so salted food stays salted)
  with those seasonings added, count 1. No match if the food already has one of them or any pair among old + new
  doesn't combine. Emitted by `NCH.season(food, key | [keys], e)`, id `shapeless/season/<keys>_on_<food>`; recipes in
  [seasoning.js](../kubejs/server_scripts/cooking/seasoning.js).
- **Eating:** `Seasonings.onEaten` (`LivingEntityUseItemEvent.Finish`, server, players, edible items) applies each
  seasoning's bonus after the food's own.
- **Tooltip:** `food/SeasoningTooltip`, one line per seasoning under the name: "Garlic: Speed (0:10)", "Onion: +2
  nutrition".
- **JEI:** each seasoning recipe is a page on the Seasoning tab (3.11): food | seasoning item(s) -> seasoned food,
  food and result cycling together.

Test set on popcorn (first values, not tuned): Cheese (any cheese wedge) +2 saturation; Onion (any onion) +2
nutrition; Garlic Speed I 10 s. Onion and garlic combine (garlic popcorn + onion, onion popcorn + garlic, or both at
once give the same stack, both bonuses apply); cheese combines with nothing. `heal` is supported but no seasoning uses
it yet.

AppleSkin: `compat/AppleSkinSeasonings` (registered only if AppleSkin is loaded) listens to AppleSkin's
`FoodValuesEvent` and adds each seasoning's nutrition and absolute saturation to `modifiedFoodValues`, so the tooltip
bar and HUD overlay show the seasoned total (saturation modifier recomputed from the new hunger). Compiles against
`libs/appleskin-forge-mc1.19-2.4.2.jar`. Heal and effects are not shown. FO-49.

Limits: a seasoned food used as a
recipe input loses the seasoning; JEI shows seasoned and plain popcorn as one entry (no subtype interpreter). Not built
from row 3: pepper, dill, cream and onion, ice cubes, popcorn variants. FO-45 to FO-48.

## 4. Limits and decisions

### 4.1 Limits
- The export reads `getIngredients()`; IE machine recipes don't fill it, so IE routes are missing from the chains.
  7 recipes couldn't be read at all.
- Code-side effects aren't in the export; Canned Goods' are listed by hand in the tool (`CANNED_CODE_EFFECTS`),
  other mods' are only flagged by class in `catalog.json`.
- Types are for the recipe and tuning layers only. Nothing in game shows a food's type yet (a tooltip line would go
  in [tooltips.md](tooltips.md)).
- New item_dict keys need a restart; until then the tag script warns and skips those foods.
- Spice of Life counts a can and its source as two different foods; there's no way to merge them in the mod.

### 4.2 Findings (2026-09-28)

1. **Canned Goods saturation is broken** (FIXED for every Canned Goods food can 2026-10-21: the reused cans since 1.0.6,
   the 15 no recipe makes now take their family's values from gen_can_types.py, [can_parity.md](can_parity.md) 8). Its modifiers are 1 to 42 where the game expects about 0.1 to 1.2 (the
   MCreator field was filled with saturation points). Every can refills saturation to the hunger level; canned steak
   is 16 / 24, 30 times the saturation of two steaks. The same numbers make cans the top Spice of Life foods
   (canned suspicious stew 11.98, canned steak 10.17): 16 cans reach every threshold.
2. **Canned Goods ids are crossed.** `canned_carrots` is "Canned Pumpkin Pies" (sized for 2 pies) and the 2-carrot
   recipe makes it too, so carrots have no can of their own; `canned_pumpkin_pie` is "Canned Golden Carrots";
   `canned_golden_carrot` is a second golden carrot can with no recipe; `canned_beetroot_soup` is labelled
   "Canned Bread"; `canned_mushrooms` ("Canned Mushroom Stew") is made from 1 brown + 1 red mushroom but valued as 2
   stews. Canned cookies and the golden and notch apple cans are flagged as meat. Oculemonade runs Soda's effect
   (Speed II). The golden apple cans amplify their source (regeneration III, absorption V).
3. **Golden Food changes vanilla.** Golden carrot is 4 hunger here (vanilla 6), and the enchanted golden apple plus
   all 23 enchanted golden foods are 8 / 8 (vanilla 4 / 1.2), worth 7 Spice of Life each.
4. **Spice of Life config drift.** Per world; a `defaultconfigs` copy since 2026-10-22 (2.3, FO-32), older worlds
   keep their own. Workspace has 31 benefit entries for
   17 thresholds. The default benefits include `attribute,generic.speed,2`, not a real attribute name (probably
   skipped; check the log on the next start). The default complexity list names two mods that weren't installed
   then (`farmersrespite`, `largemeals`): harmless. Farmer's Respite was added 2026-10-02, so its 10 entries (black tea twice;
   6 teas at 3, rose hip pie and coffee cake slices at 4, Blazing Chili at 5) now apply; only the 4 `largemeals` entries are dead (the jar's
   `ComplexityParser` logs `Item is not food: largemeals:...` for each). Unknown benefit attributes are dropped
   silently (`AttributeBenefit.createAttribute` marks them invalid, no log line).
5. **Placed pies and non-vanilla cakes** didn't count for Spice of Life when bitten from the block (only the vanilla
   cake does, as Farmer's Delight's cake slice); their slices do when eaten from the hand. FIXED 2026-10-22
   (`types-and-tuning #5`): patcherMod `food/PlacedFoodSol` + `mixin/ServerPlayerGameModePlacedFoodMixin`
   (`useItemOn` HEAD/RETURN state compare, server side, no fake players, nothing with a knife in hand) records the
   slice through SoL's `FoodTracker.updateFoodList`. Pies (any FD `PieBlock`) count as their slice item; cakes and
   puddings come from `kubejs/generation_scripts/food/placed_food_slices.json` (own slice where one exists:
   Neapolitan -> Abnormals Delight slices, coffee cake, Create C&A's chocolate cake -> Abnormals Delight's chocolate
   slice (what the knife gives since the merge, item_merges.md 2.3; was Central Kitchen's slice until 2026-10-22),
   C&A's honey cake -> Central Kitchen's honey slice, fig pudding; golden
   cakes have none and count as `farmersdelight:cake_slice` like the vanilla cake; mangosteen cake counts as its
   own edible item). Candle cakes count as the cake they turn into (vanilla's included: SoL skips those). The map
   must name food items (SoL drops non-food entries on load). Read at server start. FO-43.
6. **item_dict** (FIXED 2026-10-19, [plan_1.0.9.md](plan_1.0.9.md)): `i.catfish_slice` points at `alexsmobs:raw_catfish_slice`, which
   doesn't exist (it's `alexsdelight:`; `cooking.js` overrides it at runtime); `i.bacon` is
   `farmersdelight:cooked_bacon` (the raw bacon's key is another).

### 4.3 Decisions for you before the tuning build

- **D1** Bands: review-and-exempt (recommended) or snap every food into its band?
- **D2** Golden Food: put the enchanted foods and the notch apple back to vanilla 4 / 1.2, and the golden carrot to 6?
- **D3** Spice of Life: take the per-type complexities and the rescaled ladder? The benefits themselves (+8 hearts,
  Strength III, full knockback resistance at the top) are generous; trim them?
- **D4** Canned code effects: match the source (needs a `foodEaten` correction or a mixin), or keep the stronger ones
  as the reason to can golden food?
- **D5** Spice of Life and cans: accept that a can counts as a separate food from its source?
- **D6** Raw meat: give every raw meat and fish Farmer's Delight's 30% Hunger chance, or leave each mod's choice?

### 4.4 Rejected
- **Types as hand-written tag lists in a script:** 615 foods would make the script the place to edit, and the tool
  couldn't check it. JSON keyed by item_dict keys keeps ids in the dict and lets the tool add and check.
- **Item ids in food_types.json:** would define ids a second time (item_dict is the one place for ids).
- **Editing the per-world Spice of Life toml as the source of truth:** differs per world and resets with new worlds;
  the KubeJS map fill applies pack-wide.
- **Fried eggs as a `kubejs/data/farmersdelight/recipes/` override** (would keep Farmer's Delight's ids): recipes go
  through NCH in this pack; the NCH ids are fine.
- **Removing the Slice & Dice oil copy with `R.remove`:** does nothing, the copy doesn't exist yet when KubeJS
  removes (3.7). Turning off `basin_cooking` in `sliceanddice-common.toml` would drop every copy.
- **Scratch dedicated server for the dump:** the client was using 10 of 14 GB, so the dump ran in your world instead.

### 4.5 Planned (scratchpad 2026-10-21)
- **Fats and oils** ([TODO](todo/TODO.md) `dishes/ingredients #10`): animal fat drops only from cows, pigs and nether
  cows (knives give more meat and fat); whales drop blubber, made into whale oil that works the same; tallow crafted
  from animal fat (no tallow drops); animal oil removed, animal fat cooks into cooking oil, which takes animal oil's
  crafts; more seeds into cooking oil, bottled seed oil; fish oil counts as cooking oil, more fish into fish oil.
- **Sweets:** the single smoothie goes or becomes a type with variants (`baking/sweets #5`); matcha and salmonberry ice
  creams made not gross, ice cream parity (`baking/sweets #4`); every ice food takes ice or ice blocks (`dishes/ingredients #5`).

### 4.6 Planned from the scratchpad (2026-10-24)
- **Merge salts, pink salt** ([TODO](todo/TODO.md) `food/seasoning #6`): the salt items of The Salt and Vintage Delight
  become one, Vintage's pink salt dust (textures, recipes, tags). Open: which salt wins (recommend Vintage's `salt_dust`).
- **Salt layers** (`food/seasoning #7`): place a layer while standing in it without being pushed up, and retexture the
  layer as a palette swap of another layer block. Port change (`SaltLayerBlock`, a snow layer). No open decision beyond
  which layer block is the palette source.
- **AppleSkin** (`food/seasoning #8`): pack seasonings already reach AppleSkin (3.12, FO-49); the gap is Salt's own
  `Salted` bonus, which the tooltip does not show. Confirm which one the note meant.
- **Seasoned icons** (`food/seasoning #9`): every new seasoning gets its own icon overlay on the food (as Salt's salted
  overlay); none exist for the pack seasonings yet.
- **Honeycomb** (`dishes/ingredients #22`): counts as sugar in heated mixing (2 sugar per honeycomb) and as honey in some
  recipes; spreads on toast. Open: which recipes.
- **Jams** (`dishes/jams #1`, `#5`): one jam family (Fruits Delight's jam item, Vintage's jam block, mason jars take glass
  panes, recolour per jam) and nut butter merged with nut mash. Open: which jar item stays, which nut item survives.
- **Seasoning JEI split** (`food/seasoning #1`, future): one JEI category per seasoning type instead of the single
  Seasoning page of 3.11.

## 5. File map

- [dump_food.js](../kubejs/server_scripts/_lib/dump_food.js): `/dumpfood`
- [food_types.json](../kubejs/generation_scripts/food/food_types.json): types, exclusions, canned map
- [foodtypetags.js](../kubejs/server_scripts/tags/foodtypetags.js): the tags
- [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): generated "Food mods" block before `// Validation`
- `tools/food_catalog.py`: classifier, checker, catalog writer
- [cooking.js](../kubejs/server_scripts/cooking/cooking.js) "Protein / Egg": fried egg restore, cooking oil pot and mixer
- [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js): pumpkin seeds in `#rusticdelight:cooking_oil_ingredients`
- [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js): `smoking(output, input, e, id, xp, cookingtime)`
- [food_values.js](../kubejs/startup_scripts/cooking/food_values.js): one food's values copied onto another (cantaloupe slice, glistering melon slice)
- [stack_sizes.js](../kubejs/startup_scripts/changes/stack_sizes.js): egg stack sizes (the `egg` rule; `item_properties.js` removed in 1.0.5)
- patcherMod [compat/jei/SeasoningJEI.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/jei/SeasoningJEI.java)
  and [SeasoningCategory.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/jei/SeasoningCategory.java): the Seasoning JEI page (3.11)
- Pack seasonings (3.12): patcherMod `food/Seasonings.java`, `SeasoningRecipe.java`, `SeasoningTooltip.java`;
  [seasonings.js](../kubejs/startup_scripts/cooking/seasonings.js) (definitions), [seasoning.js](../kubejs/server_scripts/cooking/seasoning.js) (crafts),
  [seasoning_lang.js](../kubejs/client_scripts/seasoning_lang.js) (names), NCH `season`
- [food_catalog.md](food_catalog.md): generated reference; `kubejs/exported/food/catalog.json` (not in git)
- Spice of Life config: `defaultconfigs/solapplepie-server.toml` (pack copy), `saves/<world>/serverconfig/solapplepie-server.toml`, client `config/solapplepie-client.toml`
- Placed food for Spice of Life: patcherMod `food/PlacedFoodSol.java`, `mixin/ServerPlayerGameModePlacedFoodMixin.java`; `kubejs/generation_scripts/food/placed_food_slices.json`; scratch probe `tools/food_drinks_probe/food_drinks_probe.js` (`/fdtest sol`)
- Related configs: `config/salt-common.toml`, `config/farmersdelight-common.toml` (vanilla soups get FD effects,
  stack to 16), `config/survivality-common.toml` (`more_snacks`)

## 6. Offline checks

- `python3 tools/food_catalog.py --check`: exit 0 = every food typed, every key resolves, no key in two types, no
  typed item gone. Passes 2026-09-28.
- `node --check kubejs/startup_scripts/_lib/item_dict.js kubejs/server_scripts/tags/foodtypetags.js`: syntax.
- After a mod update: `/dumpfood`, then the tool without `--check`; it prints every new food and its type.

## 7. In-game test suite

Restart the game first (item_dict gained keys), then load WorldC.

- [x] **FO-01 dump:** `/dumpfood` -> chat `Dumped 629 foods, 132 food blocks, 21 drinks, 26533 recipes`; four files
  in `kubejs/exported/food/`. PASSED 2026-09-28 19:24.
- [ ] **FO-02 tags load:** after the restart, `logs/kubejs/server.log` has
  `[food types] 842 foods in 31 types, 76 canned sources` and no `[food types]` warning (seen on the 2026-10-02
  scratch server; food_types.json now has 1016 typed keys and 252 canned-source keys, so expect about
  `1016 foods in 31 types, 252 canned sources`).
- [ ] **FO-03 nested tags:** hold Raw Bacon (`farmersdelight:bacon`), `/kubejs hand` -> tags include
  `creativeengineering:food_type/raw/meat/sliced`, `.../raw/meat`, `.../raw` and `creativeengineering:food_type`.
- [ ] **FO-04 canned source:** hold a Cooked Porkchop, `/kubejs hand` -> `creativeengineering:food_type/cooked/meat` and
  `creativeengineering:canned_source`. A Carrot shows no `canned_source`.
- [ ] **FO-05 new keys:** hold Neapolitan Strawberries (a food whose key is new) -> `creativeengineering:food_type/raw/fruit`.
- [ ] **FO-06 JEI:** search `$creativeengineering:food_type/meal/feast` -> the 10 feast servings;
  `$creativeengineering:food_type/meal` -> 75 items.
- [ ] **FO-07 startup validation:** `logs/kubejs/startup.log` has no new `[item_dict] Missing/invalid` lines for the
  food block (the pre-existing `catfish_slice`, `jei_bucket_*`, `tmted_*`, `rs_*` lines are old).
- [ ] **FO-08 round trip:** `/dumpfood`, then `python3 tools/food_catalog.py --check` exits 0 and
  `kubejs/exported/tags/item_tags.json` lists 842 members under `creativeengineering:food_type`.

- [ ] **FO-09 food cans gone:** after `/reload`, JEI shows no crafting recipe for any Canned Goods food can (e.g.
  Canned Steak, Canned Apples, Hot Hot Hotpot); Soda and Reef-Cola still have theirs.
- [ ] **FO-10 stonecutter:** an iron ingot in the stonecutter offers 4 Empty Cans; the crushed can option is gone.
- [ ] **FO-11 crafting:** two iron plates stacked in one column give 12 Empty Cans; two Create iron sheets do too.
- [ ] **FO-12 deployer:** a deployer holding an iron plate over an iron plate on a belt or depot makes 12 Empty Cans.
- [ ] **FO-13 workbench and recycling:** the components blueprint in the Engineer's Workbench makes 32 Empty Cans
  from 4 IE iron plates; an Empty Can in a furnace or blast furnace no longer smelts to an iron ingot.

- [ ] **FO-14 restart and tags:** after deploying the jar and restarting, `logs/kubejs/server.log` has
  `[can servings] 247 cans: 3 x 1, 25 x 2, 46 x 3, 67 x 4, 12 x 5, 9 x 6, 35 x 8, 50 x 10` (was `39 cans: 3 x 1,
  36 x 2` before the generated cans) and no `[can servings]` warning. `/kubejs hand` on a Canned Beef lists
  `creativeengineering:cans/servings/3`, `creativeengineering:cans/servings` and `creativeengineering:cans`.
  Canned Beef holds 3 servings since 1.0.6 (a cooked meat can): read FO-15 to FO-22's `2 / 2` and `1 / 2` as 3 / 3
  and 2 / 3, or run them with Canned Carrot (`creativeengineering:canned_carrot`, 2 servings).
- [ ] **FO-15 tooltip:** a Canned Beef shows `Servings: 2 / 2` under its name; a Soda shows no servings line.
- [ ] **FO-16 first serving:** hold one Canned Beef (survival), eat it. You get the food, the can stays in hand
  with a bar at half width (yellow) and `Servings: 1 / 2`; no empty can appears.
- [ ] **FO-17 last serving:** eat it again. The can is replaced by an empty can, a crushed can or nothing.
  Repeat with 20 or so cans (`/give @s canned_goods:canned_beef 20`): roughly 14 empty, 5 crushed, 1 nothing.
- [ ] **FO-18 stack and open cans first:** hold 5 full Canned Beef and eat once: 4 stay in hand, an opened can
  (1 / 2) goes to the inventory. Eat again from the stack: the opened can empties (roll) and the hand still has 4.
- [ ] **FO-19 stacking:** full cans stack with new full cans; two opened cans at 1 / 2 stack together; an opened
  can doesn't stack with a full one.
- [ ] **FO-20 effects per serving:** eat a Canned Golden Apples twice: its effects apply both times.
- [ ] **FO-21 config:** set `[canning] emptyCanChance = 0`, `crushedCanChance = 1` in the Configs screen, empty a
  can: always a crushed can. `enabled = false`: no tooltip line, no bar, eating gives an empty can every time
  (Canned Goods behaviour). Put both back.
- [ ] **FO-22 crafting:** add the `uncan_beef` example from canning.js's header comment as a real line (or any recipe with `cans`),
  `/reload`. One full Canned Beef in the grid -> cooked beef, and the grid keeps the can at 1 / 2; craft again ->
  cooked beef and the rolled return in the grid. No `[NCH] ... KubeJSCanAction not available` warning in the log.

Cooking oil and fried eggs (3.7), after a restart:
- [ ] **FO-23 fried egg:** JEI uses of Egg show Fried Egg in the furnace (10 s), smoker (5 s) and campfire (30 s);
  cook one in each. JEI shows no Incubation fried egg recipe.
- [ ] **FO-24 seeds tag:** hold Pumpkin Seeds, `/kubejs hand` -> tags include `rusticdelight:cooking_oil_ingredients`.
- [ ] **FO-25 cooking pot:** 3 cotton seeds + 3 pumpkin seeds in a heated Cooking Pot -> 2 Cooking Oil, one bottle
  per oil. JEI lists one Cooking Pot recipe for Cooking Oil (`cooking_oil_manual_only`).
- [ ] **FO-26 mixer:** a heated basin with 3 pumpkin seeds + 1 glass bottle -> 1 Cooking Oil. JEI lists one mixing
  recipe for Cooking Oil (3 seeds, 1 bottle), no `sliceanddice:` one.
- [ ] **FO-27 other oil copies kept:** JEI still shows the Slice & Dice mixing recipe for Fried Chicken; the log has
  no NCH warning for `cooking_oil`.
- [ ] **FO-28 egg stacks:** `/give @s minecraft:egg 64`, `/give @s quark:egg_parrot_blue 64` and
  `/give @s alexsmobs:emu_egg 64`: each is one full stack. Chickens' eggs picked up merge past 16.
  `logs/kubejs/startup.log` has `[stack_sizes] set N stack sizes` and no `no item_dict key` warning.
- [ ] **FO-29 glistering melon** (values, no effects, golden type: scratch server PASSED 2026-10-22, SQ-OF-12; eating and brewing: in game): after a restart, hold a Glistering Melon Slice in survival with a full hunger bar and
  eat it: AppleSkin shows 4 hunger / 4.8 saturation (same as a Golden Melon Slice), no effect is applied. It still
  brews Healing in a brewing stand. `logs/kubejs/startup.log` has `[food values] 2 foods copied` and no warning.
- [ ] **FO-30 Seasoning page:** after installing the patcherMod jar and restarting, `logs/latest.log` has
  `[jei] Seasoning page: 2 page(s), moved 1 crafting and 65 mixing recipe(s)` and `[jei] hid 66 seasoning recipe(s)`
  (counts may differ if Create: The Salt recipes are removed). Press U on Salt: a Seasoning tab with two pages
  (crafting table, mechanical mixer); the Crafting tab no longer shows the "food + salt" page. R on Cooked Beef: the
  Mixing tab has no cooked beef + salt mix; the Seasoning pages show only cooked beef -> salted cooked beef.
- [ ] **FO-31 still crafts, off switch:** cooked beef + salt in the grid still gives salted cooked beef, and a basin
  under a mixer still salts it. Set `[jei] seasoningCategory = false` in `config/creativeengineering-common.toml`,
  rejoin: no Seasoning tab, salting back on the Crafting and Mixing tabs.
- [ ] **FO-32 Spice of Life defaults in a new world** (scratch server PASSED 2026-10-22, SQ-CF-01, with the same
  file through the deploy's `defaultconfigs`; left: one single-player world, client): create a new world: its
  `serverconfig/solapplepie-server.toml` has `minFoodsToActivate = 10`, `resetOnDeath = true`, `queueSize = 256`
  (copied from `defaultconfigs/`, added 2026-10-22).
- [ ] **FO-43 Placed pies and cakes count for Spice of Life** (client: needs a real player, fake players are skipped
  on purpose): in survival, hungry, bite a placed Apple Pie, a Chocolate Cake (Create Crafts & Additions'), a
  Strawberry Cake, a Golden Cake and a vanilla candle cake (one bite each); open the food book: Slice of Apple Pie,
  Chocolate Cake Slice (Abnormals Delight's, the one a knife gives), Strawberry Cake Slice and Slice of Cake are
  listed. A knife on a placed pie drops a slice and adds
  nothing until the slice is eaten. A Create deployer biting a cake adds nothing. `logs/latest.log` has no
  `PlacedFoodSol` warning.
- [x] **FO-44 Placed food map loads** (scratch server PASSED 2026-10-22, SQ-SOL-01: 14 entries, chocolate cake -> Abnormals Delight's slice): after a boot with the patcherMod jar,
  `logs/latest.log` has no `PlacedFoodSol` warning or error and no mixin failure for
  `ServerPlayerGameModePlacedFoodMixin`; every block in `placed_food_slices.json` exists and maps to an edible item
  (`/fdtest sol`).
- [ ] **FO-45 Seasoning crafts** (load and recipe ids: scratch SQ-SN-00, 01): Popcorn + any Cheese Wedge, Popcorn +
  Onion, Popcorn + Garlic, Popcorn + Garlic + Onion each craft one seasoned popcorn. The tooltip under the name reads
  "Cheese: +2 saturation", "Onion: +2 nutrition", "Garlic: Speed (0:10)" (both onion and garlic lines on the last).
  Popcorn + Salt still salts it. JEI: U on Garlic shows two Seasoning pages (garlic, garlic + onion).
- [ ] **FO-46 Combine rules:** Garlic Popcorn + Onion and Onion Popcorn + Garlic both give popcorn that stacks with the
  Garlic + Onion craft's. Garlic Popcorn + Garlic, Cheese Popcorn + Onion, Cheese Popcorn + Garlic and Onion Popcorn +
  Cheese give no result.
- [ ] **FO-47 Bonus on eating** (survival, `/effect clear`, hunger at about 10: `/effect give @s hunger 30 100`, then
  clear): AppleSkin's numbers before and after eating each: onion popcorn gives 2 more hunger than plain popcorn, cheese
  popcorn 2 more saturation (never above the hunger bar), garlic popcorn gives Speed I for 10 s, garlic + onion popcorn
  both. Plain popcorn gives none of these.
- [ ] **FO-49 AppleSkin:** hover plain popcorn, then onion popcorn: the onion one's hunger bar is 2 drumsticks longer; cheese
  popcorn's saturation icon is 2 points longer; hold onion popcorn: HUD overlay previews the extra hunger. Eating it
  fills by the amount shown. Plain popcorn unchanged.
- [ ] **FO-48 Stacking:** 2 Garlic + Onion popcorns made in different orders stack in one slot; seasoned and plain
  popcorn don't. `logs/latest.log` has no `Seasonings` error.

## 8. History

- **2026-09-28** First writeup for 1.0.2. `/dumpfood` built and run in WorldC. 31 food types, food_types.json,
  foodtypetags.js, 216 item_dict keys + 32 tag keys, `tools/food_catalog.py` and the generated catalog. Spice of
  Life mechanics read from bytecode; Canned Goods values, crossed ids and code effects read from its jar. Tuning
  system designed (3.5), not built.
- **2026-09-28** canning.js: Canned Goods food-can, can and can-recycling recipes removed; empty cans from the
  stonecutter (4 per ingot), crafting table and deployer (12 per 2 plates) and Engineer's Workbench (32 per 4
  plates). Food canning not built yet. FO-09 to FO-13.
- **2026-09-28** Multi-serving cans (3.6): servings from `can_servings.json` tags, NBT servings with a bar and
  tooltip, open cans eaten first, empty/crushed/nothing roll on the last serving (CEConfig `[canning]`), NCH
  `cans` argument for shaped/shapeless recipes. patcherMod built; the jar isn't in `mods/` yet. FO-14 to FO-22.
- **2026-09-29** Food canning for raw veggie (2 Baked Potato meals, salt), raw fruit (3 meals of 5/3, sugar) and
  raw meat (4 meals of 5/3, bone broth): 36 new `creativeengineering:canned_*` items on the Canned Goods beetroot
  sprite, basic ~80% / additive ~60% of the ingredients a can is worth, grid-capped at 8 / 6. Every canning recipe has an
  explicit id (the auto id only uses the first input, so basic and additive recipes collided).
- **2026-09-29** Fried eggs (3.7): Abnormals Delight's Incubation-conditioned overrides had removed Farmer's
  Delight's furnace, smoker and campfire fried egg; re-added through NCH (NCH `smoking` gained xp and time).
  Cooking oil: pumpkin seeds join cotton seeds; the Slice & Dice mixer copy is replaced by 3 seeds + 1 bottle -> 1
  oil (pot recipe moved to a `_manual_only` id). Vanilla egg -> `#forge:eggs` migration listed as TODO (3.8).
  FO-23 to FO-27.
- **2026-09-29** Egg stack size (3.9): the 10 raw eggs in `#forge:eggs` stack to 64. FO-28.
- **2026-10-02** Refresh for the mods added since 09-28 (Fruits Delight, More Delight, Farmer's Respite, Corn Delight,
  Untitled Duck Mod): `/dumpfood` re-run on a scratch server (856 foods) and added to `/dumpall`. The classifier
  typed 227 new foods; 74 of its picks were moved by hand to match earlier calls: bottled jellies -> `ingredient`
  (like jam bottles), jellos, muffins, tarts, single pies, custard, shaved ice, hawberry candy and sweet toasts ->
  `sweet`, corn dogs, tacos, jelly bread and egg toast -> `handheld`, tortilla and bread slice -> `baked`, baked/dried
  fruit -> `cooked/fruit`, durian flesh and orange slice -> `raw/fruit`, diced potatoes -> `raw/veggie/sliced`,
  tortilla chips -> `cooked/veggie`, raw tortilla -> `ingredient`. Scratch server logs `842 foods in 31 types,
  76 canned sources`. FO-02/06/08 expectations updated.
- **2026-10-11** Ships in pack 1.0.5 (staged, not deployed): the generated per-type cans (`can_types.json`, 113 cans)
  and the egg stack sizes, now from the stack size policy (`stack_sizes.js`; `item_properties.js` is in the 1.0.5
  removals). Single player with the staged files logged 113 generated cans and 1052 stack sizes, no warnings. 3.9,
  FO-28 and the file map updated.
- 2026-10-13: raw cans trimmed (mint, acorn, grapes, mite crust, raw ham/chevon/hoglin/kangaroo/moose/bison/bunfungus
  removed), White Strawberries got `canned_white_strawberry`, `canned_cactus` -> `canned_cactus_flesh` and the item
  Cactus Chunk renamed Cactus Flesh. Per-food can coverage now lives in [can_parity.md](can_parity.md) (`CP-`).
- 2026-10-14: the generated per-type cans and stack sizes (staged for 1.0.5, never deployed) and the 2026-10-13 can
  parity changes ship in pack **1.0.6** (STAGED 2026-10-14, [plan_1.0.6.md](plan_1.0.6.md)).
- 2026-10-19: 4.2 finding 6 fixed: `i.catfish_slice` is the Alex's Delight id, `i.bacon` is raw bacon ([plan_1.0.9.md](plan_1.0.9.md)).
- 2026-10-19: 3.6 numbers updated (247 cans, 1 to 10 servings; canning recipes exist), [cans.md](cans.md) F1.
- 2026-10-20: food audit. Fresh `/dumpfood` (scratch server, every jar in mods/ incl. Brewin -03, Vinery -04): 1030 foods,
  none new to the pack, 5 gone (Brewin's apple jelly, glow berry marmalade and sweet berry jam are no longer food;
  Vinery's King Danis wine and duplicate mead removed by the port). Typed 5: Beer, Vodka, Dread Nog, Miss Lilitus Wine
  (`vinery:lilitu_wine`) -> drink/alcohol (Dread Nog moved by hand from drink), Scarlet Pierogi -> meal (the stale
  `scarlet_pierogies` key dropped). Name scan of every type found no misfiles. `gen_can_types.py` rerun on the new catalog:
  4 cans follow Brewin 4.5.0 values (Kimchi and Pickled Pickles lose their effects, Cheesy Pasta 11 / 0.716 with longer
  Nourishment, Creamy Onion Soup Comfort -> Nourishment). food_catalog.py keeps hand comments in its item_dict block now.
  New potential cans in [can_parity.md](can_parity.md).
- 2026-10-20: `gen_stack_sizes.py` rerun on the fresh food export: `scarlet_pierogies` -> `scarlet_pierogi` (fixes the
  startup `[stack_sizes] no item_dict key` warning), Beer / Vodka / Dread Nog / Miss Lilitus Wine and the two golden
  milk bucket cans added, golden cakes now by dict key.
- 2026-10-21: Glistering Melon Slice edible at Golden Melon Slice values (4 / 1.2, always edible), no effects;
  food_values.js entries can drop effects; added to the `golden` type (3.10). FO-29.
- 2026-10-21: Seasoning JEI page (3.11): Salt's salting craft and Create: The Salt's 65 salting mixes moved off the
  Crafting and Mixing pages onto one Seasoning page; `creativeengineering:shapeless/season/...` crafts join it.
  patcherMod built, jar not installed. FO-30, FO-31.
- 2026-10-22: Spice of Life pack config in `defaultconfigs/` (FO-32, new-world values passed on the scratch server,
  SQ-CF-01). Placed pies and cakes count for Spice of Life (4.2 finding 5, patcherMod `PlacedFoodSol`, jar in
  `mods/`, not run; FO-43 in game, FO-44 / SQ-SOL-01 on a scratch server). Doc audit: Create C&A's chocolate cake
  bite now counts as Abnormals Delight's chocolate slice (the knife's slice after the merge), not Central Kitchen's.
- **2026-10-22** Glistering Melon Slice values (3.10, FO-29: hunger and modifier of the Golden Melon Slice, always
  edible, no effects, golden type) queued as scratch SQ-OF-12 (`tools/older_food_probe.js`); eating it stays in game.
- 2026-10-22 (second bulk run): SQ-SOL-01 and SQ-OF-12 PASS on the scratch server. FO-44 done; FO-29 and FO-43 in game.
- 2026-10-23: Pack seasonings framework (3.12, `food/seasoning #3` mechanism): NBT seasonings with combine rules and a
  bonus on eating; test set cheese / onion / garlic on popcorn. patcherMod 1.1.02 jar installed (also carries the 3.11
  Seasoning page). FO-45 to FO-48; scratch SQ-SN-00, 01 queued.
- 2026-10-24: planned from the scratchpad (4.6): merged pink salt, salt layers placed from inside, seasoning on AppleSkin and per-seasoning icons, honeycomb as sugar, one jam family and merged nut butter. Nothing built.
