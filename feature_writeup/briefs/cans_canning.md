# Brief: cans / canning

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# cans`, Sub `canning`. Reference writeups:
[cans.md](../cans.md) (hub), [can_parity.md](../can_parity.md) (worksheet, decisions 4, sprites 5.1),
[food.md](../food.md) 3.5 (tuning) and 3.6 (servings). Code: [canning.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/canning.js),
[cans.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js), [gen_can_types.py](../../tools/gen_can_types.py) ->
[can_types.json](../../kubejs/generation_scripts/food/can_types.json), [can_servings.json](../../kubejs/generation_scripts/food/can_servings.json),
[cantags.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/can_tags.js), patcherMod `food/CanServings.java`, `compat/KubeJSCanAction.java`.

## Scope and order

Open rows: #1 (In Progress), #2, #3, #4 (Bug), #5 to #10, #12 to #15. #11 is Testing (table at the end).

**State on 2026-10-21 that changes the rows** (checked against the files, details per row):

- The worksheet has **no `?` cells left**: the user decided every row (commit `c82dd31`, 2026-10-20). Now 258 rows
  `Want x`, 493 `/`, 0 `?` (the sheet's summary still said 263 `?`; corrected). Row #2 is done.
- **26 `Want x` rows have no can**: 20 buildable (#14) and 6 pie slices with no source in the pack (blocked on
  `baking/baked-goods`).
- **228 distinct cans are registered** (26 hand-written in cans.js + 202 generated); the sheet's "235" counts rows
  (shared cans, the bell pepper colour rows). **45 have their own sprite, 183 borrow one**: 152 Canned Goods' beetroot
  can, 18 the apple can, 9 the raw salmon can, 3 the raw beef can, 1 the carrot can (#13).
- The 2026-10-20 builds (sliced-input rule 4.2, pumpkin to fruit, Fish Meal, shulker meal, cantaloupe values) are **not
  shipped**: 1.0.9's `deploy/history/1.0.9.json` records canning.js / can_types.json / cans.js hashes from `15c43a0`;
  the files changed in `c82dd31`. They go out with the next deploy.
- Golden cans **already** serve 0.25x nutrition (SECTIONS `golden` mult 0.25); D-B only changes their effect time
  (0.25x -> 0.333x). cans.md F3 said "full value"; corrected.
- `tools/gen_can_types.py` needs `kubejs/exported/food/catalog.json` + `foods.json` (a `/dumpfood` export,
  gitignored, not in the repo). Every row that adds or changes a generated can needs that export first. The sprite
  source folder `textureswapper/canned/` is also outside the repo (the user's machine).

**Build groups and order:**

1. **KubeJS-only, no export needed:** #4 (effects on 4 hand-written cans), #6b (opened filling can no longer makes a
   pie, `modifyResult`), #9/#8 guard (can-consuming recipe ids end in `_manual_only`). One session, one `/reload` +
   restart.
2. **#14 can crafts + #15 canned cake + #12** (same generator run): extend `gen_can_types.py` (new sections and opts),
   run it on a fresh `/dumpfood`, restart. Do #13's generator tweak (`--sprites`) in the same pass.
3. **#13 textures:** ongoing art; the `--sprites` mode lets each batch land without the food export. Golden cans can
   be scripted (recolour), the rest drawn.
4. **#1 values, #3 audit:** after `food/types-and-tuning #1` (D1 to D6). #1's golden effect change (D-B) can go with
   group 2.
5. **#10 can opener:** patcherMod, any time; small.
6. Close by decision: #2 (done), #7 (D-E, no remap), #9 (D-C, manual crafts only). #5 is built (Testing).

Creative Canning (D-A, its own mod) is decided but has no row; do not start it inside these rows. Everything below
stays in KubeJS / gen_can_types.py so it ports as data.

---

## canning #14: Can crafts (priority)
Status Planned / Work KubeJS / tool / `cans/canning #14`

### Goal
A can and its recipes for every `Want x` row that has none, through the generator and the NCH helpers; servings,
additives and the 4.2 sliced-input rule as for the built cans.

### Now
- Recount of [can_parity.md](../can_parity.md) section 2 (script: parse every family table, count the Want / Impl
  cells): 258 `Want x`, 235 rows implemented, 0 `?`. The 26 `Want x` rows without a can:

| Sheet line | Item | Type (food_types.json) | Dict key | Catalog h / mod / sat | Effects |
|---:|---|---|---|---|---|
| 103 | Corn Kernel `corn_delight:corn_seeds` | raw/veggie | `corn_seeds` | 1 / 0.2 / 0.4 | none |
| 104 | Maize `corn_delight:corn` | raw/veggie | `corn` | 2 / 0.2 / 0.8 | none |
| 130 | Coffee Berries `farmersrespite:coffee_berries` | raw/fruit | `coffee_berries` | 2 / 0.4 / 1.6 | wither 5 s @80 %, caffeinated 10 s |
| 248 | Chips `pneumaticcraft:chips` | cooked/veggie | `chips` | 5 / 0.6 / 6 | none |
| 251 | Grilled Corn `corn_delight:grilled_corn` | cooked/veggie | `grilled_corn` | 6 / 0.2 / 2.4 | none |
| 256 | Tortilla Chip `corn_delight:tortilla_chip` | cooked/veggie | `tortilla_chip` | 1 / 0.1 / 0.2 | none |
| 413 | Cornbread Stuffing `corn_delight:cornbread_stuffing` | meal | `cornbread_stuffing` | 12 / 1 / 24 | nourishment 300 s; bowl |
| 414 | Creamed Corn `corn_delight:creamed_corn` | meal | `creamed_corn` | 7 / 0.5 / 7 | comfort 180 s; bowl |
| 415 | Creamy Pasta with Chicken Cuts `moredelight:...` | meal | `creamy_pasta_with_chicken_cuts` | 12 / 0.8 / 19.2 | nourishment 180 s; bowl |
| 416 | Creamy Pasta with Ham `moredelight:...` | meal | `creamy_pasta_with_ham` | 12 / 0.8 / 19.2 | nourishment 180 s; bowl |
| 483 | Fig Tart `fruitsdelight:fig_tart` | sweet | `fig_tart` | 5 / 0.4 / 4 | digesting 80 s |
| 484 | Lemon Tart `fruitsdelight:lemon_tart` (Want cell is `X`) | sweet | `lemon_tart` | 5 / 0.4 / 4 | haste 20 s, refreshing 20 s |
| 731 | Maggot `alexsmobs:maggot` | hazard | `maggot` | 1 / 0.2 / 0.4 | none |
| 748 | Flaxen Cheese Wedge (`x*`: "needs pot cooking recipe") | dairy | `flaxen_cheese_wedge` | 4 / 1 / 8 | none |
| 749 | Scarlet Cheese Wedge (`x*`) | dairy | `scarlet_cheese_wedge` | 4 / 1 / 8 | none |
| 785 | Tomato Sauce `farmersdelight:tomato_sauce` | ingredient | `tomato_sauce` | 4 / 0.4 / 3.2 | none; bowl (craft remainder, verified) |
| 929 | Pear with Rock Sugar `fruitsdelight:pear_with_rock_sugar` | sweet | `pear_with_rock_sugar` | 8 / 0.6 / 9.6 | comfort 60 s, lozenge 30 s; bowl |
| 945 | Sweet Roll `create:sweet_roll` | sweet | `sweet_roll` | 6 / 0.8 / 9.6 | none |
| 989 | Dumplings `farmersdelight:dumplings` | handheld | `dumplings` | 8 / 0.8 / 12.8 | none |
| 1042 | Dough `create:dough` | none (not edible) | `in_dough` (tag `dough` = `#forge:dough/wheat`) | not a food | - |
| 466-482 | 6 pie slices: blueberry, cherry, crimson berry, green apple, nightshade, truffle | sweet | - | - | blocked: no whole pie in the pack (2.13 note) |

  Catalog values from [food_catalog.md](../food_catalog.md) (2026-10-20 dump). Every source key above exists in
  [item_dict.js](../../kubejs/startup_scripts/_lib/item_dict.js) (checked); none of the proposed can keys below collide.
- `farmersdelight:wheat_dough` (the edible dough, `i.out_dough`) is in `_lib/removed_items.js:140`; `create:dough`
  (`i.in_dough`) is the pack's dough but has no food component, so it is not in the catalog. The sheet's Dough row
  is a can of a non-food: see Decisions.
- TODO #12's two foods are already rows here: "Pear with rock salt" is Pear with Rock **Sugar** (line 929), creamed corn
  is line 414.
- Generator mechanics (gen_can_types.py): `EXPLICIT` rows (line 104), type loops for bread/salad/soup/golden (440-443),
  `sized_recipes()` for meal sections (321), mult sections build `n` of the sources plus `extra` additive recipes
  (396-402), honey bread / golden post-processing (446-454), sprite choice (406-410). canning.js `canSection(section)`
  (lines 20-53) turns each recipe into `R.shapeless(out, [empty_can, ...], e, key + suffix)` (id
  `creativeengineering:shapeless/<key><suffix>`), plus `<key>_pie` for cans with `pie`. A section that canning.js
  never calls `canSection()` for gets no recipes (silently).

### Build
1. **Get the export.** In game `/dumpfood`, copy `kubejs/exported/food/` into the checkout, run
   `python3 tools/food_catalog.py --check` then `python3 tools/gen_can_types.py --check` (expect the current 202 cans,
   no problems) before editing.
2. **gen_can_types.py: new sections in `SECTIONS`** (keep the order; canning.js calls them by name):
   ```python
   ('raw_veggie', dict(meal=(5, 6), servings=2, target=0.8, adv=0.7, dur=1.0, fast=False, extra=[('_salt', SALT, 0, 1)])),
   ('dairy',      dict(mult=1.25, servings=4, n=4, dur=1.0, fast=False, extra=[], pot=True)),      # see Decisions
   ('preserve',   dict(mult=1,    servings=4, n=4, dur=1.0, fast=False, extra=[])),
   ('dessert',    dict(mult=1.25, servings=8, n=6, dur=0.75, fast=False, extra=[('_sugar', SWEET, 3, 1)])),
   ```
   `raw_veggie` reproduces the hand-written veggie rule exactly (checked: the formula gives every hand-written veggie,
   fruit and meat count and additive count as written in canning.js), so the new raw veggie cans match the old ones.
   Add `'raw_veggie': 'creativeengineering:item/canned_carrot'` to `GROUP_SPRITE`.
3. **gen_can_types.py: opts** (in `add()`):
   - `input` for meal sections too: `sized_recipes()` takes `opts.get('input', sources)` for the `any` lists (today
     only mult sections honour `input`). Needed for Dough.
   - `fx_drop=[effect ids]`: filter those out of `scaled_effects()` (coffee berries' wither, see Decisions).
   - `pot`: copy `spec.get('pot')` / `opts.get('pot')` into the can as `"pot": true`.
4. **EXPLICIT rows** (section, key, name, sources, existing, opts):
   ```python
   ('raw_veggie', 'canned_corn', 'Canned Corn', ['corn', 'corn_seeds'], False),          # 6 / 4+salt; kernels share (4.2)
   ('raw_fruit', 'canned_coffee_berries', 'Canned Coffee Berries', ['coffee_berries'], False,
    dict(fx_drop=['minecraft:wither'])),                                                   # 5 / 3+sugar
   ('cooked_veggie', 'canned_chips', 'Canned Chips', ['chips'], False),                    # 6 / 5+salt
   ('cooked_veggie', 'canned_grilled_corn', 'Canned Grilled Corn', ['grilled_corn'], False),
   ('cooked_veggie', 'canned_tortilla_chip', 'Canned Tortilla Chips', ['tortilla_chip'], False),
   ('meal', 'canned_cornbread_stuffing', 'Canned Cornbread Stuffing', ['cornbread_stuffing'], False),   # 2 -> 4
   ('meal', 'canned_creamed_corn', 'Canned Creamed Corn', ['creamed_corn'], False),
   ('meal', 'canned_creamy_chicken_pasta', 'Canned Creamy Pasta with Chicken', ['creamy_pasta_with_chicken_cuts'], False),
   ('meal', 'canned_creamy_ham_pasta', 'Canned Creamy Pasta with Ham', ['creamy_pasta_with_ham'], False),
   ('meal', 'canned_dumplings', 'Canned Dumplings', ['dumplings'], False),
   ('pie', 'canned_fig_tart_filling', 'Canned Fig Tart Filling', ['fig_tart'], False),     # 4 tarts, no pie back
   ('pie', 'canned_lemon_tart_filling', 'Canned Lemon Tart Filling', ['lemon_tart'], False),
   ('hazard', 'canned_maggots', 'Canned Maggots', ['maggot'], False, dict(meal=(2, 1))),  # 7, like shulker
   ('dairy', 'canned_flaxen_cheese', 'Canned Flaxen Cheese', ['flaxen_cheese_wedge'], False),
   ('dairy', 'canned_scarlet_cheese', 'Canned Scarlet Cheese', ['scarlet_cheese_wedge'], False),
   ('preserve', 'canned_tomato_sauce', 'Canned Tomato Sauce', ['tomato_sauce'], False),
   ('dessert', 'canned_pear_with_rock_sugar', 'Canned Pears in Rock Sugar', ['pear_with_rock_sugar'], False),
   ('bread', 'canned_sweet_roll', 'Canned Sweet Rolls', ['sweet_roll'], False),           # 6 -> 4
   ('preserve', 'canned_dough', 'Canned Dough', ['in_dough'], False,
    dict(input=['dough'], values=dict(hunger=2, modifier=0.3))),                          # see Decisions
   ```
   Counts in the comments are what the formulas give (meal sections: `n0 = min(8, round(can x 0.8 / value))`;
   additive `round(n0 x adv)`): Canned Corn 6 / 4, Coffee 5 / 3, Maggots 7 (own (2, 1) meal, 100 % share instead of
   44 % at the Meat Meal's 8-item cap).
   The Corn Kernel row folds into Canned Corn as its sliced form: the pack cuts 1 maize into 2 kernels
   (`cooking.js:104`, `fdCutting(i.t_crops_corn, knife, 2 corn_seeds)`), the same 2x boost as Cabbage Leaf.
5. **canning.js:** add a section block per new section with the Goal comment style already used, and call
   `canSection('raw_veggie')` (after the hand-written veggie block), `canSection('dairy')`, `canSection('preserve')`,
   `canSection('dessert')`. In `canSection()`, branch on `can.pot`:
   ```js
   if (can.pot) R.fdCooking(rec.inputs flattened to item ids, Item.of(i[key], 1), e, key + rec.suffix, 1, 200, i.empty_can)
   else R.shapeless(out, input, e, key + rec.suffix)
   ```
   (the Cooking Pot's container slot takes the empty can, NCH `fdCooking(input, output, e, idOverride, exp, time,
   container)`, neoCraftHelper.js:926; at most 6 ingredients, so pot sections keep n <= 6). Keep the manual
   shapeless recipe for cheese too only if the user wants a no-pot route (the `x*` note says pot).
6. Run `python3 tools/gen_can_types.py` (writes can_types.json, the generated item_dict block, can_servings.json,
   food_types.json `canned`), then `python3 tools/food_catalog.py --check`. Restart (new items).
7. **Sheet:** fill Impl / Recipe / Input / Effects for the 20 rows; mark Corn Kernel as the alt input of Maize; update
   the summary counts; history line. cans.md 2.2 section table and counts.
8. **Pie slices (6):** leave `Want x` but note "blocked on `baking/baked-goods #4` / `#5`" or set them `/` (ask). When a
   whole pie becomes obtainable, add the EXPLICIT pie row with `pie=` like the others.

### Ids and data
- New can items, all `creativeengineering:<key>` via the generated item_dict block (keys above).
- Recipe ids: `creativeengineering:shapeless/<key>` and `<key>_salt` / `_sugar` (pot: the fdCooking id NCH builds from
  `key`).
- Verified: FD bowl foods (`bowlFoodItem`) and tomato sauce have a bowl crafting remainder
  (FarmersDelight-1.19.2-1.2.4 `ModItems`, lines 223 and 363-364), so the grid hands the bowls back. corn_delight,
  moredelight and fruitsdelight bowl foods: unverified (see #8 for a check).
- Unverified: whether `pneumaticcraft:chips` keeps its id after `dishes/meals #7` (rename PC chips, remove PC
  deep-frying); build the can on the current id.

### Decisions
- **Dough can.** `create:dough` is not a food. Options: (a) an ingredient can (fixed low food value, and the pack's
  dough recipes take `canned_dough` with the NCH `cans` argument, one serving = one dough); (b) drop the row.
  Recommend (a) with 4 dough -> 4 servings; it is the only "ingredient can" so far and fits real canned dough.
- **Cheese wedges.** Recommend the pot route as the sheet says: 4 wedges + can in the Cooking Pot -> 4 servings at
  1.25x (processed cheese). Needs the `pot` branch above.
- **Coffee berries.** Copying effects would give each serving an 80 % Wither. Recommend dropping wither
  (`fx_drop`), keep caffeinated.
- **Tortilla chip** values come out tiny (2 hunger a serving); acceptable, or move it to `x3`. Recommend leave.
- **Tarts** go in the pie section without a pie-back recipe (like durian / pineapple pies). The sheet note "move all
  tarts to pies" is `baking/baked-goods #6`.
- **Corn Kernel** as its own can vs folded into Canned Corn: recommend folded (4.2 rule).

### Gotchas
- gen_can_types.py refuses an `existing=False` can whose key is already in the hand-written part of item_dict, and a
  meal section source must be in the food catalog unless `values` is given (Dough).
- A section not called from canning.js gets items but no recipes; log line `[canning] <section>: N recipes` shows it.
- Create's mixer auto-processes shapeless crafting recipes with 2+ ingredients (verified in create-1.19.2-0.5.1.i,
  `MechanicalMixerBlockEntity#matchStaticFilters`, `allowShapelessInMixer` default true), so every new canning recipe
  is also a mixer recipe. That is fine for making cans (see #8).

### Tests (CP-)
- CP-15 corn: 6 maize, or 6 corn kernels, or 4 + salt -> Canned Corn (2 servings); no partial can.
- CP-16 cheese: 4 Flaxen Cheese Wedges + an empty can in the container slot of a heated Cooking Pot -> Canned Flaxen
  Cheese.
- CP-17 bowls: 2 Creamed Corn + can -> can and 2 bowls back; 6 Pears with Rock Sugar -> 6 bowls back.
- CP-18 coffee: a Canned Coffee Berries serving gives Caffeinated, never Wither.
- CP-19 every new can shows a sprite (no purple/black) and its JEI recipe; `[canning]` log has no `skipped` line.

---

## canning #13: Can textures (priority)
Status Planned / Work resource pack / `cans/canning #13`

### Goal
Every registered can has its own 16x16 sprite; none borrows its group's or Canned Goods' beetroot can.

### Now
- Sprites: `kubejs/assets/creativeengineering/textures/item/<can key>.png` (25 files: 19 hand-written raw cans plus
  cranberry, raw beef / mutton / pork / cod / salmon) and `kubejs/assets/canned_goods/textures/items/` (9 overrides of
  Canned Goods' own sprites). Canned Goods' 20 reused ("existing") cans keep the mod's sprites or those overrides.
- Borrowing cans, by where the texture comes from (computed from can_types.json `texture` and cans.js `.texture()`):

| Section | Cans without a sprite | Borrows |
|---|---:|---|
| golden | 50 | beetroot can |
| soup | 25 | beetroot can |
| raw_fruit | 16 (+ hand-written White Strawberries) | apple can |
| cooked_veggie | 13 | beetroot can |
| cookies | 11 | beetroot can |
| meal | 11 | beetroot can |
| raw_fish | 9 | raw salmon can |
| pie | 9 | beetroot can |
| feast | 9 | beetroot can |
| cooked_meat | 8 (calamari, catfish, perch, pike, lionfish, glazed porkchop, jerky, maple bacon) | beetroot can |
| salad | 8 | beetroot can |
| bread | 7 | beetroot can |
| special | 1 (ancient fruit) | apple can |
| raw_meat | 1 (shulker) + hand-written Raw Chicken, Raw Rabbit | raw beef can |
| drink | 1 (powdered milk) | beetroot can |
| raw veggie (hand) | 1 (Cactus Flesh) | carrot can |
| **Total** | **183** of 228 | |

  Full key lists: `python3 -I` over can_types.json (cans with a non-null `texture`). #14 adds 20 more (19 new items
  plus Canned Dough), #15 none if it reuses `canned_goods:canned_cake` (it has a sprite).
- Sprite layout (measured on canned_apple.png vs canned_carrot.png): grey can body and rim shared; the label is rows
  6-11, columns 3-12, a section colour band with a ~5x5 food icon centred. Every sprite is 16x16 RGBA.
- Pipeline today: drawn in `textureswapper/canned/newcan` (label layers in `newlabel`, combined by
  `combine_tex.py --batch`), copied in by hand. That folder is not in the repo.
- A can stops borrowing only when `gen_can_types.py` is rerun (it writes `"texture": null` when
  `<key>.png` exists, line 406-410), which needs the food export. Hand-written cans need their `.texture(...)` removed
  in cans.js (lines 39 Cactus Flesh, 52 White Strawberries, 59 Raw Chicken, 62 Raw Rabbit).

### Build
1. **`gen_can_types.py --sprites`** (small, do first): load the existing can_types.json, set each non-existing can's
   `texture` to `None` when `SPRITES/<key>.png` exists, else to `GROUP_SPRITE.get(section, CAN_SPRITE_NOTE)`; write
   only can_types.json. No catalog needed, so every sprite batch lands with a copy + `--sprites` + restart. Print the
   number still borrowing per section.
2. **Golden cans by script** (50 of 183): add `tools/gen_can_sprites.py --golden` (PIL is available): for each golden
   can, take the matching base can's sprite (golden_cooked_beef -> canned_beef's, golden_carrot -> canned_carrot ...;
   map in the script), recolour the label band to the gold palette, write `canned_golden_<x>.png`; the enchanted twin
   reuses the same sprite (they already render with the glint, `glow` in can_types.json). Needs the base sprites to
   exist first, so golden comes after the plain families it copies from. Alternative: one shared golden sprite and one
   enchanted (2 files) via `GROUP_SPRITE['golden']`; recommend that as an interim step (cheap, no beetroot cans).
3. **Interim group sprites** for every section still on the beetroot can: one sprite per section
   (`canned_section_soup.png` ...) and `GROUP_SPRITE` entries, so a soup can at least reads as soup. Drawn once per
   section (10 sprites), then replaced can by can.
4. **Draw the rest** in the user's textureswapper flow, in this order (most-seen first, recommendation for the open
   "order" decision): raw fruit (16) and raw fish (9) (cheap: same can, new icon), cooked meat / fish (8), soup (25),
   cooked veggie (13), cookies (11), bread (7), salad (8), meal (11), pie (9), feast (9), hazard / special / drink
   leftovers, then #14's 20. Copy to `kubejs/assets/creativeengineering/textures/item/<key>.png`, run
   `--sprites`, remove cans.js `.texture()` for the 4 hand-written cans as their sprites land.
5. Sheet: Texture cells to `x`, summary column, 5.1 sprite table.

### Ids and data
- Sprite file name = the can's item_dict key = item path (`canned_ce_beetroot_soup` included).
- Never override `canned_goods:items/canned_beets` (every borrowing can points at it; can_parity 5.1).
- Canned Goods model -> texture names differ from item ids (verified in CannedGoods-1.19.2-3.jar): `canned_pork` uses
  `items/canned_porkchops`, `canned_rabbit_stew` `items/canned_rabit_stew`, `canned_pufferfishs` `items/canned_pufferfish`,
  `canned_spider_eyes` `items/canned_spider_eye`, `canned_carrots` `items/canned_pumpkin_pie`, `canned_pumpkin_pie`
  `items/canned_golden_carrots`. Overrides under `kubejs/assets/canned_goods/` must use the texture name, not the id.

### Decisions
- Order: recommend above. Golden: interim shared sprite now, scripted recolour later.
- Whether to commit `textureswapper/canned/` (or at least `newlabel/` + `combine_tex.py`) to the repo so a session can
  run the label compositing; recommend yes, as `tools/textures/canned/`.

### Gotchas
- KubeJS items without `.texture()` use `creativeengineering:item/<id>`; a missing png shows purple/black, so do not
  set `texture: null` by hand.
- Resource changes need F3+T or a restart; can_types.json texture changes need a restart (startup script).

### Tests (CP-)
- CP-20 sprites batch: after a `--sprites` run and restart, JEI "Canned" shows every can of the batch with its own
  sprite; `[cans] registered N generated cans` unchanged.

---

## canning #4: Hand-written raw cans don't carry effects (Bug)
Status Bug / Work KubeJS / `cans/canning #4`

### Goal
The 26 hand-written cans pass their source's effects per serving, like the generated meal-section cans (duration x
1.0, same chance).

### Now
- cans.js lines 24-63 build the 26 cans with `veggie` / `fruit` / `meat` food builders: no `.effect()`.
- Only 4 of the 26 sources have effects (food_catalog.md): Banana `neapolitan:banana` agility 15 s; Glow Berries
  glowing 15 s; Salmonberries water_breathing 5 s; Raw Chicken hunger 30 s @30 %. The sheet's Effects cells agree,
  except Cantaloupe (`/`, but since 2026-10-20 the slice copies the pumpkin slice and has none; corrected to `none`).
- `global.effect` (effect_dict.js) has `glowing`, `water_breathing`, `hunger`; no Neapolitan section, no `agility`.

### Build
1. effect_dict.js: add a `// Neapolitan` block under the vanilla ones: `ef.agility = 'neapolitan:agility';`.
2. cans.js: per can, chain the effect (ticks, amplifier 0, chance):
   ```js
   var fx = global.effect
   e.create('creativeengineering:canned_banana').displayName('Canned Banana').food(f => fruit(f).effect(fx.agility, 300, 0, 1))
   ... canned_glow_berry: fx.glowing 300; canned_salmonberry: fx.water_breathing 100; canned_raw_chicken: fx.hunger 600, chance 0.3
   ```
   (`fruit` / `meat` return the builder: make them `return f.hunger(5)...` so they chain.)
3. Sheet Effects cells for the 4 rows -> `x`; cans.md F2 closed.
4. Later (D-A, Creative Canning) the hand-written cans move into the generator: add the `raw_veggie` section from #14
   and EXPLICIT rows for all 26 (hand-written veggie / fruit / meat counts are reproduced exactly by the generator's
   formulas, checked); recipe suffixes match (`_salt`, `_sugar`, `_broth`); the hand-written lines in item_dict's
   canning block move into the generated block. Not needed for this bug.

### Decisions
- Raw chicken's Hunger chance on a can: keep (matches generated raw cans, e.g. Canned Shulker carries nausea), or drop
  with `food/types-and-tuning` D6. Recommend keep until D6.

### Gotchas
- Startup script: restart, not `/reload`. Canned Goods' existing cans are untouched.

### Tests
- CP-21 one serving of Canned Banana / Glow Berries / Salmonberries gives agility 15 s / glowing 15 s / water
  breathing 5 s; Canned Raw Chicken sometimes Hunger.

---

## canning #1: Can values for meal, pie, feast, drink and hazard cans (In Progress)
Status In Progress / Work KubeJS / data / `cans/canning #1`

### Goal
Real goals for the placeholder sections, then rerun the generator.

### Now
- Placeholders in gen_can_types.py `SECTIONS`: `hazard` line 73 (Meat Meal, pufferfish Fish Meal), `meal` 83
  (x0.75, 2 -> 4), `pie` 84 (x1, 4 -> 4), `feast` 85 (x1, 1 block -> 6), `drink` 86 (x1, 4 -> 4), `golden` 87.
  Goal comments in canning.js: hazard 373-379, meal 482-498 ("TBD"), pie 500-521, feast 523-536, drink 538-551 ("TBD"),
  golden 553-570. (The TODO's "~504, 542, 557" point at pie, drink and golden.)
- Value vs goal per can: can_parity 4.3 (meal 81-84 %, pie 125 %, drink 125 %, feast n/a).
- Golden: D-B decided 10 servings, 0.25x nutrition, 0.333x effect time. Nutrition is already 0.25x (built); effects
  are 0.25x (`dur=0.25`).

### Build
1. Golden (no tuning dependency): `SECTIONS['golden']['dur'] = 0.333`; rerun with #14's generator pass.
2. The rest after `food/types-and-tuning #1` sets per-type values: write each section's goal into its canning.js Goal
   comment and SECTIONS (`mult`, `servings`, `n`), rerun, update 3.1 and 4.3 (same script that built 4.3).

### Decisions
- Recommended starting goals (keep the "rationing" design of canning.js lines 75-82: prepared food, many servings, a
  little lower SoL): meal x0.75 with 2 -> 4 stays; pie 4 slices -> 4 at x0.8 (125 % is generous); feast keep 6
  servings of one serving; drink x0.8. Hazard: the low-value ones get their own small meal like shulker/maggot.

### Tests
- CN-02 covers golden effects; add the new numbers to it.

---

## canning #2: `Want = ?` cells (done)
Status Planned -> done / Work data / `cans/canning #2`

### Now
No `?` left (258 x, 493 `/`). Decided by the user 2026-10-20 (`c82dd31`): drinks, alcohol, desserts, cakes, most
sweets / handhelds / preserves / ingredients `/`; new `x`: Maggot, both cheese wedges (pot), Tomato Sauce, Pear with
Rock Sugar, Sweet Roll, Dumplings, Dough, both tarts. Summary and at-a-glance recounted in can_parity.md (this brief's
corrections). The remaining work is #14.

---

## canning #3: Hunger / saturation audit of every can
Status Planned / Work data / `cans/canning #3`

### Goal
Fill the Food h/s, Craft sum h/s and Canned h/s columns and mark Food audit per can.

### Now
Postponed on `food/types-and-tuning #1`. 4.3 already has value vs goal per can (2026-10-20), computed from
can_types.json, canning.js and food_catalog.md.

### Build
After tuning: add `--audit` to gen_can_types.py that prints, per can, source h/s (catalog), the least efficient
recipe's summed h/s (from `recipes`, counting additives at their catalog value: salt 0, honey bottle 6 / 1.2) and the
can total (servings x meal), as a markdown table to paste into the sheet (or write the three cells directly by
matching the item id in the row). Hand-written cans need their recipes listed in the script (or migrate them, #4
step 4).

---

## canning #5: Pumpkin can: veggie or fruit (built)
Status Planned -> Testing / Work KubeJS / `cans/canning #5`

Built 2026-10-20 (can_parity 4 "Pumpkin", cans.md D-D): fruit, 3 servings, 4 slices or 3 + sugar / honey (recipe id
`canned_pumpkin_sugar`, canning.js 276-277; cans.js 54-55; can_servings.json `canned_pumpkin: 3`). Not shipped yet
(after 1.0.9). Test: CP-09.

---

## canning #6: Smaller can-parity follow-ups
Status Planned / Work KubeJS / `cans/canning #6`

### Now (per item)
- **White strawberry ratio:** decided (same 3 / 2 + sugar, bigger per-serving value); numbers wait on #3 / tuning.
- **Half cans from raw meat slices (CP-09):** superseded and built 2026-10-20: no recipe makes a partial can (4.2).
- **Shulker filet cheaper path:** answered. The mollusk cuts into 2 filets (verified:
  `endersdelight-1.19-1.2.2.jar` `data/endersdelight/recipes/cutting/shulker_filet.json`, count 2), and filets share the
  mollusk's 8 slots (4.2), so 4 mollusks cut make a full can. Done.
- **An opened filling can still makes a whole pie:** decided, not built. `<key>_pie` (canning.js line 49:
  `R.shapeless(pie, [i[key], i.pie_crust], e, key + '_pie')`) takes any can, opened or not.

### Build (opened filling can)
1. NCH (neoCraftHelper.js, next to `_canServings`, line 765): add `_fullCansOnly(recipe, canIds)`:
   ```js
   _fullCansOnly(recipe, ids) {
     recipe.modifyResult((grid, result) => {
       for (var n = 0; n < ids.length; n++) {
         var s = grid.find(Ingredient.of(ids[n]))
         if (!s.isEmpty() && s.nbt != null && s.nbt.contains('CanServings')) return Item.empty
       }
       return result
     })
   }
   ```
   and an options path to reach it from `shapeless` / `shaped` (e.g. `cans = { fullOnly: [ids] }`, or a new 8th
   argument). Verified in kubejs-forge-1902.6.2-build.73: `RecipeJS#modifyResult(ModifyRecipeResultCallback)` exists,
   the callback gets `ModifyRecipeCraftingGrid` (`find(Ingredient)`, `findAll`) and the result stack, applied in
   `KubeJSCraftingRecipe#kjs$assemble`.
2. canning.js line 49: use it, and give the id the `_manual_only` suffix (`key + '_pie_manual_only'`): the recipe has 2 ingredients, so
   Create's mixer runs it too, its ingredient match ignores NBT, and whether the basin builds the output through
   `assemble` (where `modifyResult` runs) or `getResultItem()` is unverified. Create skips recipe ids ending `_manual_only` (verified, `AllRecipeTypes.CAN_BE_AUTOMATED`).
3. Do not use `.strongNBT()`: the repo has a history of NBT ingredients matching nothing (item_dict.js lines 77-80,
   neoCraftHelper.js 160-170).

### Tests
- CP-22 an opened Canned Apple Pie Filling (2 / 4) + Pie Crust -> no output; a full one -> Apple Pie. A mixer with
  filling + crust does nothing.

---

## canning #7: Removed cans shipped in 1.0.4 (decided: no remap)
Status Planned -> Complete (won't do) / `cans/canning #7`

Decided 2026-10-20: cans.md D-E and can_parity 4 "Removed cans already shipped: No issue, no remap, closed". If
reopened, the pattern is a Forge `MissingMappingsEvent` remap like `development/backportedBrewin/.../BnCRemaps.java`.

---

## canning #8: Canning automation
Status Planned / Work KubeJS / `cans/canning #8`

### Goal
Cans can be made by machines; the canning.js notes "food effects carry over" and "bone broth returns its bowl" are
settled.

### Now
- canning.js line 68-69 `// TODO Canning automation`; line 73 the two notes.
- Create already automates them: the mixer runs any shapeless crafting recipe with more than one ingredient
  (`MechanicalMixerBlockEntity#matchStaticFilters`, create-1.19.2-0.5.1.i, `allowShapelessInMixer` default true;
  the pack's Create server config is per world, value unknown), and every canning recipe is shapeless with the can
  plus at least one item. The mechanical crafter does too (`allowRegularCraftingInCrafter` default true).
- Bowls: FD bowl foods carry a bowl crafting remainder (verified, FD `ModItems.bowlFoodItem`), so bone broth and FD
  soups hand their bowls back in the grid. Vanilla mushroom stew, rabbit stew and beetroot soup have none: canning 6
  of them loses 6 bowls. Other mods' bowl foods unknown.
- Effects carry: generated cans yes; hand-written no (#4).

### Build
1. In game: put an empty can + 8 beetroot in a basin under a mixer (no heat) -> Canned Beetroot; check JEI's Create
   "shapeless mixing" category lists canning recipes. If the world config has `allowShapelessInMixer = false`, decide
   whether to turn it on (affects every shapeless recipe) or add `R.createMixing` copies per can recipe in
   `canSection()` (one line next to `R.shapeless`; NCH `createMixing(output, input, e, idOverride, heat)`, line 1266).
2. Bowls: a startup `ItemEvents.modification` in cans.js (or a cooking script) sets the bowl remainder on bowl foods
   that lack one: `e.modify(i.mushroom_stew, it => { it.craftingRemainder = Item.of(i.bowl).item })` (KubeJS has
   `ItemKJS#kjs$setCraftingRemainder`, verified). Find the list in game: a one-off script logging every item in the
   soup / salad / meal food types whose `getCraftingRemainingItem()` is empty.
3. Can-consuming recipes stay manual (D-C): `_manual_only` ids (#6, #9).
4. Optional flavour later (Creative Canning): Create sequenced assembly "fill then press the lid".

### Decisions
- Rely on Create's auto-shapeless (recommend) vs explicit mixing recipes.

### Tests
- CP-23 mixer canning; CP-24 6 mushroom stews -> can + 6 bowls back.

---

## canning #9: Servings only work in NCH crafting recipes (decided)
Status Planned -> Complete (decided) / `cans/canning #9`

Decided 2026-10-20 (cans.md D-C, can_parity 4 "Crafting uses"): cans are used only in manual shaped / shapeless
crafts. The one guard to build: every recipe that consumes a can gets a `_manual_only` id so Create's mixer (verified) and
mechanical crafter (unverified, check `RecipeGridHandler`) don't eat a whole can (today only `<key>_pie`, done under #6). Any future recipe with the NCH `cans` argument
should do the same; worth a comment next to `_canServings` in neoCraftHelper.js.

---

## canning #10: Cosmetic can opener
Status Planned / Work patcherMod (+ KubeJS item, sound) / `cans/canning #10`

### Goal
A can opener item; while it is in the inventory, opening a can plays a sound.

### Now
"Opening" happens in patcherMod `food/CanServings.finishServing` (lines 152-173): `source` is a full can when
`!isPartial(source)` and `maxServings(source) > 1`. KubeJS can't tell (the `foodEaten` event sees a copy with no NBT
whether or not a can is opened), so the Work column should read patcherMod. Canned Goods has only golem sounds.

### Build
1. KubeJS: item `creativeengineering:can_opener` (cans.js or items/), key `i.can_opener` in item_dict's canning block,
   tag `#creativeengineering:can_openers` (`i.t_can_openers`) added through `global.tags` in cantags.js; a recipe
   (iron nugget + iron ingot, NCH `shaped`). Sound: `StartupEvents.registry('sound_event', e =>
   e.create('creativeengineering:can_open'))`, `kubejs/assets/creativeengineering/sounds.json` + `sounds/can_open.ogg`.
2. patcherMod `CanServings.finishServing`: before `afterServing`, `boolean opening = !level.isClientSide &&
   !isPartial(source) && maxServings(source) > 1;` then if `opening` and the inventory holds an item in the tag, play
   the sound at the player (`SoundSource.PLAYERS`, pitch 0.9-1.1). Look the sound up by id
   (`ForgeRegistries.SOUND_EVENTS.getValue`), no Java registration.
3. Optional: the same on a craft-time opening (`craftRemainder`), skip at first (no player there).

### Tests
- CP-25 with an opener in the inventory, the first bite of a full Canned Beef plays the sound; the second bite and an
  opener-less bite are silent.

---

## canning #12: More cannable foods
Status Planned / `cans/canning #12`

Both foods are already `Want x` rows built under #14 (Pear with Rock **Sugar**, line 929; Creamed Corn, line 414).
Propose folding the row into #14.

---

## canning #15: Canned cake, 4 servings
Status Planned / Work KubeJS / `cans/canning #15`

### Goal
One canned cake: 4 servings, a quarter cake each, fast; 1 cake + can, or 4 slices + sugar / honey + can.

### Now
- `canned_goods:canned_cake` (`i.canned_cake`) exists, has its own sprite, no recipe, and Canned Goods food 28 / 4.0
  always-edible (verified, `CannedCakeItem`; no code effects, hands an empty can back, which the servings mixin
  discards). can_servings.json lists it at 2.
- Cake: `minecraft:cake` (`i.cake`) is a block item, not a food; FD Slice of Cake `farmersdelight:cake_slice`
  (`i.cake_slice`) 2 / 0.1, fast, speed 20 s; a cake is 7 slices.

### Build
1. EXPLICIT row, reuse the Canned Goods can (existing = True), new section `cake` (mult 1.75 = 7/4 slices a serving,
   servings 4, n 1, fast, input `['cake']`, value source `cake_slice`) and a post-processing step like honey bread:
   add `_slices` recipe `4 cake_slice + 1 of [sugar, honey_bottle]`.
   ```python
   ('cake', 'canned_cake', None, ['cake_slice'], True, dict(input=['cake'])),
   ```
2. canning.js: `canSection('cake')` with a Goal comment.
3. Rerun the generator (sets servings to 4 in can_servings.json and overrides the 28 / 4.0 food via cans.js's
   `ItemEvents.modification`).

### Decisions
- Which id: recommend Canned Goods' `canned_cake` (sprite exists, an orphan item gets a use, no new item).
- The slice route gives a full can from 4 of 7 slices (cheaper than the whole cake). Recommend 4 slices + sugar as
  asked but note it is the 1.75x boost route; or ask for 6 slices + sugar.

### Tests
- CP-26 cake + can -> Canned Cake (4 / 4, fast, speed per serving); 4 slices + sugar -> the same.

---

## canning #17: All feasts cannable
Status: Planned / Work: KubeJS (+ `tools/gen_can_types.py`) / TODO `cans/canning #17` (scratchpad 2026-10-24)

### Goal
Every feast (a placed block with servings) can be canned, as the nine existing feast cans are. Related: `dishes/meals #5`.

### Now
- Feast cans are rows in [gen_can_types.py](../../tools/gen_can_types.py) lines 356-370, section `feast`
  (`mult=1, servings=6, n=1`, `input=` the feast block item, value source = the serving); output goes to
  `kubejs/generation_scripts/food/can_types.json`; recipes come from `canSection('feast')`
  ([canning.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/canning.js) line 511).
- Covered: popcorn box, stuffed hoglin (one can for its three plates), honey glazed ham, nachos, pineapple fried
  rice, roast chicken, shepherd's pie, stuffed pumpkin, stuffed shulker.
- No can row found (by absence in gen_can_types.py and `food_types.json` `canned_*`): Brewin' pizza, Vintage
  cheese pizza and meat pizza, Veggies Delight vegan pizza and stuffed bell peppers feast, Brewin' quiche, Oceanic
  Delight paella plate and stuffed pufferfish blocks. Which of these count as feasts is *unverified*: the dump
  `kubejs/exported/food/food_blocks.json` (2026-10-22, before the new delights) lists only the block classes.

### Build
1. `/dumpall`, then list every block whose class is `FeastBlock`, `PizzaBlock` or a mod's own multi-serving plate
   (`food_blocks.json`, `blockClasses`); that list is "all feasts".
2. One `('feast', 'canned_<name>', '<Name>', ['<serving key>'], False, dict(input=['<block item key>']))` row per
   missing feast, keys from `global.item`; run `/cegen`; add can textures per `can_parity.md`.
3. Later feasts (End's, Hostile, Ocean delights, `meals #5` new assemblies) add a row when integrated.

### Decisions
- Servings per can for 8-slice pizzas: recommend keep the section rule (6), so no per-feast exception.
- Pizza block versions with partial servings (SERVINGS 0-3) are not whole feasts: recommend input = the whole
  pizza item only.

### Gotchas
Generated files are never hand-edited. `canned_hoglin_feast` shares one can across three plates: do the same for
any feast with several plate items.

### Tests
- CN-07 JEI shows a can recipe for each feast listed in the generator section.
- CN-08 Eat a canned pizza and a canned stuffed pepper can: serving values match the serving item.

---

## Built, needs play

| Row | What | Note |
|---|---|---|
| `cans/canning #11` | Canned golem dye, generated cans, parity tests: CN-01 to CN-06, CP-01 to CP-14 | CP-06 is superseded by CP-08. CP-09 tests the 2026-10-20 build, which is not deployed yet (after 1.0.9). FO-14 to FO-22 (food.md, `food/types-and-tuning #11`) expected 2 servings on Canned Beef; it holds 3 since 1.0.6 (note added to FO-14). |

## Proposed TODO.md corrections

- `#2` Status Planned -> Complete; Details -> "Done 2026-10-20 (`c82dd31`): no `?` left, 258 x / 493 `/`".
- `#5` Status Planned -> Testing; Details -> "BUILT 2026-10-20: fruit, 3 servings (CP-09), ships next deploy".
- `#6` Details: half cans and shulker filet done (4.2; mollusk cuts into 2, verified); left: white strawberry
  numbers (after #3), opened filling can makes a pie.
- `#7` Status Planned -> Complete (decided D-E: no remap).
- `#9` Status Planned -> Complete (decided D-C: manual crafts only; `_manual_only` guard under #6).
- `#10` Work KubeJS -> patcherMod / KubeJS.
- `#12` fold into #14 ("rock salt" -> "rock sugar"; both already `Want x`).
- `#13` Details "48 of the 235 built cans" -> "45 of the 228 built cans (183 borrow: 152 the beetroot can)".
- `#14` Details "248 `Want x`, 235 built; then the `?` rows once row 2 decides them" -> "258 `Want x` rows, 235
  built: 20 cans to add, 6 pie slices blocked (no source); no `?` rows left".
- `#1` Details: golden cans are already 0.25x nutrition; D-B leaves only effects 0.25x -> 0.333x.

## Writeup corrections made

- [can_parity.md](../can_parity.md) 0: "Potential cans" row (263 `?`) -> decided, 0 `?`, 26 `x` without a can.
- can_parity.md 0 legend and Columns: `yes` / `no` -> `x` / `/` (what the tables use).
- can_parity.md 1: summary recounted (Pie, alcohol, Hazard, Egg and dairy, Preserves, Desserts, Cake, Sweets,
  Handheld, Ingredients rows and the total).
- can_parity.md 2.2: Cantaloupe Effects `/` -> `none` (copies the pumpkin slice since 2026-10-20).
- can_parity.md 2.9, 3.1, 6, 7 CP-01: four "no" that had become "/" ("no additive", "no recipe makes", "no
  `[canning]`", "no results").
- can_parity.md 4.3 "Golden can numbers": golden cans are already 0.25x nutrition.
- [cans.md](../cans.md) 0 Pending: the 2026-10-20 D-D build is unshipped (after 1.0.9).
- cans.md 2.1: servings spread (2 servings 25, 3 servings 46).
- cans.md F3 and D-B: golden cans already serve 0.25x nutrition; only the effect length is left.
- [food.md](../food.md) 7 FO-14: the 247-can log line and `cans/servings/3`; a note that Canned Beef holds 3 servings
  (FO-15 to FO-22 read as 3 / 3, or use Canned Carrot).
