# More Delight 26.05.20 against the installed 25.07.28

**Decided and BUILT 2026-10-25 (owner); Shipped in 1.1.03 (deployed to server and players 2026-10-26); SQ-MRD-02, -03 PASSED (r1101, r1102b); **SQ-MRD-01 FAILED r1102b 2026-11-02: the knives still mine at speed 8** (5.1); MRD- rows not run in game.** Installed: `moredelight-25.07.28-1.19-forge.jar` (`mods/`, `server_mods/`), 1.19.2. Evaluated 2026-10-25
against `devmods/checkNewerVersionForContent/moredelight-26.05.20-1.20-forge.jar` (1.20.1 / Forge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #25` in [todo/TODO.md](todo/TODO.md). No config file.

**Licence:** MIT in both jars. Code and art can be reused with the notice kept.

**Result in one line:** no new item. 26.05.20 is a rewrite onto DelightLib (a new required library) with the same 33
items; the changes worth having are the knife stats (the installed knives are overtuned) and fast-eating toasts.
Owner's decisions: knives retuned, every toast eats fast (a family rule), Carrot Soup keeps Comfort, the tag goes back to
`forge:sliced_potato`, the pack's own meat / fruit / vegetable tags are singular, and a Cheese Toast is added. Built in
section 5; tests in section 4.

## 1. How it was read

- `python3 tools/jar_diff.py` (214 files each side; 7 added, 7 removed, 104 changed; en_us lang **+0 / -1 / ~0**, the
  removed key is the old creative tab title).
- Both jars decompiled with Vineflower. 25.07.28 has `ItemRegistry`, `TierRegistry`, `TagRegistry`,
  `CreativeTabRegistry`; 26.05.20 has only `MoreDelight`, which registers every item, knife and recipe through
  DelightLib builders (`addon.food(...)`, `addon.knife(...)`, `addon.cookingRecipe(...)`). Food values compared line by
  line. DelightLib itself is not on disk, so its knife base damage is unknown.
- Recipes and tags compared semantically (drop `group` / `category`, unwrap `forge:conditional`).
- FD 1.2.4's `CookingPotRecipe` constructor read with `javap`: an empty `container` falls back to the result's crafting
  remainder, so the bowls the 1.20 pot recipes now name change nothing on 1.19.2.
- Pack side: `item_dict.js` (all 33 ids mapped), `item_merges.js` (diced potatoes merged into Rustic Delight's potato
  slices), `exported/gear/tools.json` (live knife stats).

## 2. Changes and proposed decisions

### 2.1 Items

No new item. The same 31 foods and 2 knives; Hamburger with Cheese and Toast with Cheese still register only with
Casualness Delight (not in the pack). Toast with Blueberries (also Casualness only) is gone, and two unregistered models
(Sliced Bread, Toast with Peanut Butter) were deleted. Nothing to carry over.

### 2.2 Knives

| Knife | Installed (live, `tools.json`) | 26.05.20 |
|---|---|---|
| Wooden Knife | 2.2 damage, 2.2 speed, 59 uses, mining speed 8, harvest level 4, enchantability 10 | vanilla Wood tier: 59 uses, mining speed 2, level 0, enchantability 15; attack speed 2.0 |
| Stone Knife | 2.8 damage, 2.2 speed, 131 uses, mining speed 8, harvest level 3, enchantability 10 | vanilla Stone tier: 131 uses, mining speed 4, level 1, enchantability 5; attack speed 2.0 |

For comparison, Flint Knife is 2.5 / 2.0 / 131 / speed 4 / level 1 and Iron Knife 3.5 / 2.0 / 250 / speed 6 / level 2.
The installed Stone Knife beats Flint on every stat and mines faster than Iron; the Wooden Knife has netherite harvest
level. 26.05.20's damage depends on DelightLib's default; with FD's own 0.5 knife base it would be 1.5 (wood) and 2.5
(stone, equal to Flint).

**Decided (built, `func/tune_tools.js`):** carry over through `global.tuneTools` (startup, KnifeItem is a DiggerItem): Wooden Knife 1.5 damage, 2.0
speed, mining speed 2, level 0, enchantability 15; Stone Knife 2.5 damage, 2.0 speed, mining speed 4, level 1,
enchantability 5. Durability unchanged.

### 2.3 Food values

| Item | Installed | 26.05.20 | Proposed |
|---|---|---|---|
| Toast and the 5 toppings (Egg, Honey, Sweet Berries, Glow Berries, Chocolate) | normal eating speed | fast (like dried kelp) | **Decided: every toast eats fast** (also Caviar Toast and the new Cheese Toast), as a family rule (section 5.2) |
| Carrot Soup | Comfort 3:00 | Nourishment 3:00 | **Decided: skip**, Comfort stays on Carrot Soup (and on soups, [farmers_delight.md](farmers_delight.md) 2.5) |

All other hunger, saturation and effects are unchanged.

### 2.4 Recipes, tags, other

| Change | Proposed |
|---|---|
| Pot meals name the bowl container | skip: same behaviour on FD 1.2.4 (remainder fallback) |
| Create cutting: Potato -> 2 Diced Potatoes, Bread -> 4 Bread Slices. The installed jar has both in `data/create/cutting/` (never loaded) with the potato recipe on the crop block | skip: Slice and Dice already turns the cutting-board Bread -> slices recipe into a slicer recipe, and diced potatoes are merged into Rustic's slices |
| Tag `forge:sliced_potatoes` renamed `forge:sliced_potato` | **Decided: follow it** (section 5.3). Correction 2026-11-01: the installed 25.07.28 cooking recipes (mashed potatoes, potato salad, the four diced potatoes with ...) read the plural `forge:sliced_potatoes`; with the diced potatoes merged it was empty, so CD fills it with Rustic's slices too (`dishes/ingredients #26`) |
| Brewin' cheese recipes for Hamburger with Cheese and Toast with Cheese | skip as is (items need Casualness Delight). **Decided:** the Cheese Toast is added (section 5.1), no Casualness Delight items. The cheeseburger exists twice already (Delightful, Vintage) |
| Toast with Honey drops `extraResults` | skip: the bottle comes back through vanilla remainder either way |
| Casualness potato slice -> 1 diced (was 2) | skip: Casualness not installed |
| Knife advancement, access transformer removed, `kk_kz` lang, creative tab via DelightLib | skip |

## 3. Open questions for the owner

None. Cheese Toast values (5 hunger, 0.7 modifier, fast) are the builder's pick between Toast with Egg and the wedge:
change them in `moredelight/toast.js` if wanted.

## 4. Tests

Prefix `MRD-`. Restart needed (startup scripts and a CD jar change).

| ID | Check |
|---|---|
| MRD-01 | Wooden Knife tooltip: 1.5 attack damage, 2.0 speed; mining speed 2, cannot harvest stone-level blocks faster than a wooden tool. Stone Knife 2.5 / 2.0, mining speed 4 |
| MRD-02 | Eat Toast, a topped toast and Caviar Toast: each takes the fast eating time (about 0.8 s) |
| MRD-03 | Cheese Toast: shapeless Toast + Flaxen Cheese Wedge, eats fast, 5 hunger; JEI shows the recipe, the icon is the cheese toast sprite |
| MRD-04 | `/dumptags`: `forge:sliced_potato` and `forge:sliced_potatoes` hold Rustic's potato slices and the Sliced Sweet Potato; More Delight's mashed potatoes, potato salad and four diced potato dishes show the slices in JEI; fries and the Veggies Delight potato dishes still craft |
| MRD-05 | `/dumptags`: `creativeengineering:exotic_fruit`, `fruit_salad_slice`, `roasted_bell_pepper`, `soup_veggie_cooked` filled; Bowl of Exotic Fruits and Fruit Salad still craft from fruit |
| MRD-06 | Carrot Soup still gives Comfort |

## 5. Built

### 5.1 Knives and toasts

Knives: `kubejs/startup_scripts/func/tune_tools.js` (More Delight block). **Bug (r1102b SQ-MRD-01, deep scan 2026-11-02):** attack
damage and speed apply, but `miningSpeed` (lines 134-135) does not reach the knives' tier: both still mine at 8 (the instance
`/dumpgear` agrees). Fast toasts and the Cheese Toast item: Creative
Delight startup script `kubejs_startup/moredelight/toast.js` (list `global.fastToasts`). Item
`creativeengineering:cheese_toast` (`i.cheese_toast`, food type `handheld`), art from More Delight's Toast with Cheese
(`kubejs/assets/creativeengineering/textures/item/cheese_toast.png`, notice in `more_delight_art_notice.txt`). Recipe in
`data/creativedelight/kubejs/moredelight/recipes.js`.

### 5.2 Family rule

`tools/food_families.py`: a `values` rule on `bread/slice` (toast only) and `sandwich/toast`: every toast eats fast
(checked against the food dump's `fast` flag). The doc regenerates with `/cegen` after `/dumpall`.

### 5.3 Tag singulars

`forge:sliced_potatoes` -> `forge:sliced_potato` (`i.t_sliced_potato`), and the pack's own tags
`creativeengineering:exotic_fruits` / `fruit_salad_slices` / `roasted_bell_peppers` / `soup_veggies_cooked` ->
`exotic_fruit` / `fruit_salad_slice` / `roasted_bell_pepper` / `soup_veggie_cooked`. Kept plural on purpose: Forge's
shared `forge:fruits/*`, `forge:berries`, `forge:mushrooms/*` (other mods read them) and the `jar_family/*` tags, which
follow the plural family names (cookies, candies, apples). Veggies Delight's port no longer remaps the tag.
Exception (2026-11-01, `dishes/ingredients #26`): More Delight's own six potato cooking recipes read the plural
`forge:sliced_potatoes` (`i.t_sliced_potatoes`), which CD's `moredelight/item_merges.js` fills with Rustic's slices; the
recipes stay on that tag. Since 2026-11-01 the Sliced Sweet Potato (veggies_delight.md 5.6) is in both sliced tags too
(CD `veggiesdelight/tags.js`), so More Delight's six dishes also take it.

### 5.4 Not touched

Nothing else from the 26.05.20 diff.
