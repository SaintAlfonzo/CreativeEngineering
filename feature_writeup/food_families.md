# Food families

Test prefix **FF-** · Last updated **2026-11-02** · Overall status **Produce families (fruit to dairy) DECIDED and applied 2026-10-25; baked families (cookie, cake, pie, muffin, quiche) restructured and applied 2026-10-27 (4.6); both BUILT and shipped (1.1.03, 1.1.04), scratch server PASSED except SQ-FFB-01 (deep scan 2026-11-02), not run in game; every other family: DESIGN draft, decisions open**

## 0. At a glance

Every food in the pack, plus every whole food block (cakes, pies, pizzas, feasts), sorted into **families** by culinary
form (jam, pie slice, juice, burger), so one set of rules covers a whole family: should it be canned, where it can be
put down, and what recipe shape it takes at each station. Families nest like food types do (`pie/slice` is in `pie`).

| Part | Where | Status |
|---|---|---|
| Families (criteria, membership) and their rule rows | [tools/food_families.py](../tools/food_families.py) `FAMILIES`, `classify()`, `RULES` | draft 2026-10-23 |
| 3.1 Family rules (rule / planned / decision per field) | section 3.1 (generated) | stated rules cited; 2026-10-24: the planned rows of the food TODO families added; 2026-10-25: fruit, veggie, meat, fish, egg and dairy decided (4.4); the other Decision rows wait on you (4.2) |
| 3.2 Compliance (members in line, per field) | section 3.2 (generated) | from the 2026-10-24 dumps |
| 3.3 Foods without a family | section 3.3 (generated) | 24 foods, 36 skipped items |
| 3.4 Members by family (every member, every checked row) | section 3.4 (generated) | from the 2026-10-24 dumps |
| Applying the produce rules (4.4): recipes, coaster tag, Display Delight dish blocks, cans | BUILT 2026-10-25, shipped with 1.1.03 (2026-10-26); scratch server PASSED (SQ-FF-01 r1031, SQ-FF-00 and -02 r1102b; deep scan 2026-11-02); not run in game | FF-01 to FF-08 (7), scratch queue SQ-FF |
| Baked families (4.6): pizza, quiche, lasagna and dessert families; cookie, cake, pie, muffin and quiche members on their rules | BUILT 2026-10-27, in the Creative Delight jar since 1.1.04; scratch SQ-FFB-00 PASS r1102b, SQ-FFB-01 no verdict yet (deep scan 2026-11-02); not run in game | FF-11 to FF-14, scratch queue SQ-FFB |
| Pancake family (4.7): `dessert/pancake`, food type `sweet/pancake`, Sweet Potato Pancakes placed | BUILT 2026-11-02, Creative Delight jar not built; not run | FF-15 to FF-17, scratch queue SQ-PCK, CQ-PCK |
| Applying the rules of the other families | not started | after the decisions |

**Counts (2026-10-24 dumps, 2026-10-27 tree):** 1416 items in 33 families and 76 subfamilies, 22 without a family, 64
skipped (sequenced assembly intermediates, lunchboxes, black powder cakes, removed and merged items); none unsorted.
**Open first:** section 3.1, then 4.2 (4.4 for what the produce decision changed). To change a family or a rule, edit `RULES` / `classify()` in the tool; the tables follow.
**Refresh:** `/dumpall` (needs `/dumpfood`, `/dumpplaceable`, `/dumptags item`) → `python3 tools/food_families.py`
(or `/cegen run food_families`). No reload: nothing in game reads it.

## 1. Overview

The pack already sorts food into 31 **food types** ([food.md](food.md) 2.1) for value tuning and Spice of Life. Those
sort by cooking state (raw, cooked, meal, sweet), so they don't carry recipe rules: `sweet` alone holds 190 items
(jellos, pie slices, candy, ice cream, waffles) with no shared recipe shape. Families sort by **what the food is**.
Every food keeps its food type and gets one family (or none, 3.3).

How the file works, in the order of section 3:

1. **3.1 Family rules.** One row per family and subfamily: criteria, parent, and four fields: cannable, placeable,
   recipe shape by station, other. Each entry is one of:
   - **Rule**: already stated in a writeup and built. The source is linked (can parity, oven.md, recipes_cooking.md,
     placeable_food.md, alcohol.md).
   - **Planned**: stated in a TODO row, not built yet.
   - **Decision**: nothing stated. The text is a proposal, mostly what the members already do, for you to accept or
     change, the same way the can parity worksheet's `?` rows worked.
   A subfamily without its own entry for a field takes its parent's ("as fruit"). The Other column also shows each
   family's values now (hunger, saturation, stack, container) and what its members go into.
2. **3.2 Compliance.** Per family, how many members are in line with each field, as passed/checked, split into rule,
   planned and decision counts. Each member is checked against its own subfamily's entries, so a parent row adds up its
   subfamilies. The last two columns show where members place and which stations make them now.
3. **3.3 Foods without a family.** Magic and one-off foods (food type `special`) and single odd fits, with their
   closest family. They keep their own recipes. Also the items skipped on purpose.
4. **3.4 Members by family.** Every member, with ok / **no** / - for each checked entry (R1, R2... listed above each
   table), its can state, placement and stations.

## 2. Reference

- **Cannable** is checked against the can parity worksheet, not against a family-wide guess: each food's `Want` cell
  in [can_parity.md](can_parity.md) section 2. A cut form with a row marked "alt input of" follows its whole item's
  Alt input cell (`Raw Chicken Cuts: x`); a feast serving that "shares Canned X" is n/a (the feast block fills the
  can). "Has a can" = a can source in `food_types.json` "canned", or the food slot (first input group) of any can
  recipe in `can_types.json`; additives (sugar, broth, vinegar) don't count. A food with no row passes only when it
  has no can; a whole block with no row is n/a.
- **Placeable** methods: *own block* (cakes, pies, pizza, feasts, popcorn and nachos boxes), *own block, sneak*
  (Fruits Delight jams, Vinery bottles), *DD placed* (Display Delight display block), *Food Plate*, *Small Food Plate*,
  *coaster* (`#brewinandchewin:placeable_on_coaster`), *Supp. jar* (`#supplementaries:cookies`, or a soft fluid
  container: soups, drinks and jams from the Supplementaries jar, other mods' jars and `kubejs/data/*/moonlight/soft_fluids`).
  Data: [placeable_food.md](placeable_food.md)'s tags and the live Display Delight maps.
- **Stations** are recipe types from the recipe dump. Not counted: Garden Cloche (growing), and unpacking a crate,
  sack or storage block. Some stations repeat another's recipes for automation, so the rules name the first one:
  - Slice and Dice = Cutting Board (87 of 87 foods).
  - Mixer ⊇ Cooking Pot (176 of 180, plus 56 more): Slice & Dice turns pot recipes into heated mixing and skips ids
    ending `_manual_only` ([recipes_cooking.md](recipes_cooking.md) 3).
  - Spout ⊇ Keg pouring (all 17).
  - Smoker ⊇ Furnace, Smoker ≈ Campfire / Skillet: vanilla's cooking trio, 68 foods in all three.
- **Checks** look at station plus ingredient: "Oven with pie crust" passes when some Oven recipe for the item has
  `pie_crust` among its inputs (tags are resolved). "No other recipe" passes when every station that makes the item is
  in the list. A check that doesn't apply to a member shows `-`.

## 3. Families

Family tree (subfamilies in brackets):

- Produce and protein: `fruit` (whole, berry, cut, cooked), `veggie` (raw, raw_cut, cooked, cooked_cut), `meat` (raw,
  raw_cut, cooked, cooked_cut), `fish` (same four), `egg` (cooked, dish), `dairy`
- Bread and kitchen: `bread` (loaf, slice, flat), `ingredient` (dough, sauce, other), `preserve` (jam, spread, pickle)
- Sweets: `jello`, `pie` (whole, slice, small), `cake` (whole, slice, muffin), `cookie`, `dessert` (brownie, pudding,
  pastry, waffle, roll, other: the catchall for any sweet or baked food with no other family), `candy` (bar, piece,
  coated, marshmallow), `frozen` (ice_cream, popsicle, shaved_ice)
- Savory bakes: `pizza` (whole, slice), `quiche` (whole, slice), `lasagna` (whole, serving)
- Hand food: `sandwich` (square, roll, burger, wrap, toast: by the bread its sprite shows), `skewer`, `roll`, `stuffed`, `snack`
- Bowl and plate: `salad`, `soup` (soup, stew), `meal` (rice, pasta, fried, plate), `feast` (whole, serving)
- Drinks: `drink` (juice, tea, coffee, milk, soda, other), `alcohol` (wine, brew, spirit, mixed)
- Overlays: `golden` (enchanted), `can`, `hazard`. Their members wrap a base food; the rules are about the wrapper.

<!-- FAMILIES:BEGIN (generated by tools/food_families.py, do not edit by hand) -->

1423 items in 33 families and 78 subfamilies; 21 without a family (3.3); 91 skipped (3.3); unsorted 94.
Rule rows: 105 stated, 80 planned, 80 decisions.

### 3.1 Family rules

One row per family and subfamily. A field a subfamily does not set is its parent's ("as parent").
**Rule** = stated and built, **Planned** = stated in a TODO row, **Decision** = no stated rule yet: the text is a proposal, mostly what the members do now, to accept or change.

| Family | Parent | Criteria | Cannable | Placeable | Recipe shape by station | Other (values, uses) |
|---|---|---|---|---|---|---|
| **Fruit** `fruit` | - | A fruit, berry or nut eaten as harvested or cut, or only dried/baked. | **Rule:** Per item, can parity 2.2 (raw: Fruit Meal 5/3 x 3 servings, + sugar, honey or vinegar; pumpkin, melon, cantaloupe, hamimelon and pineapple canned from slices) and 2.6 (dried / baked) ([can_parity.md](can_parity.md)) | **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1)) | **Rule:** none: gathered (owner 2026-10-25 (4.2 D1)) | Now: hunger 2 to 5 (median 3); saturation 0.3 to 6 (median 1.2); stack 64 x45<br>Used in: can 34; salad 31; preserve 26; soup 26; meal 21; drink 18; unsorted 15; candy 13; feast 11; fruit 11; 14 more |
| **Whole fruit** `fruit/whole` | fruit | One whole tree or bush fruit (apple, orange, mango, banana). | as fruit | as fruit | as fruit | Now: hunger 2 to 4 (median 3); saturation 0.8 to 2.4 (median 1.8); stack 64 x11<br>Used in: can 11; soup 11; preserve 10; salad 10; drink 8; fruit 6; pie 3; cake 2; candy 2; frozen 2; 9 more |
| **Berry** `fruit/berry` | fruit | Small fruit picked by the bunch: berries, grapes, cherries. Nuts are not berries. | as fruit | as fruit | as fruit | Now: hunger 2 to 5 (median 2); saturation 0.3 to 6 (median 0.55); stack 64 x20<br>Used in: can 12; salad 12; soup 11; candy 10; meal 10; feast 9; unsorted 9; drink 8; preserve 8; cake 5; 10 more |
| **Nut** `fruit/nut` | fruit | A nut or seed eaten as harvested (acorn, walnut, peanut). Canned with salt, not sugar. | **Rule:** Per item, can parity 2.2, but the additive is salt (or bone broth), not sugar, honey or vinegar ([can_parity.md](can_parity.md)) | as fruit | as fruit | Now: hunger 2; saturation 0.4 to 0.8 (median 0.6); stack 64 x2<br>Used in: preserve 2; salad 2; unsorted 2; drink 1; feast 1; fruit 1; meal 1; pizza 1; roll 1; soup 1 |
| **Fruit slice** `fruit/slice` | fruit | A slice or the flesh of a larger fruit (pumpkin, melon, cantaloupe, hamimelon, pineapple, citrus slices, durian flesh). | as fruit | as fruit | **Rule:** Cutting Board: whole fruit -> slices (owner 2026-10-25 (4.2 D1))<br>**Planned:** A banana bunch cuts into bananas ([TODO](todo/TODO.md) dishes/ingredients #18) | Now: hunger 2 to 4 (median 2); saturation 1.2 to 2.4 (median 1.2); stack 64 x7<br>Used in: can 7; salad 7; meal 6; preserve 6; pie 3; soup 3; frozen 2; unsorted 2; dessert 1; drink 1; 3 more |
| **Dried or baked fruit** `fruit/cooked` | fruit | A fruit only dried or baked, nothing added. | as fruit | as fruit | **Rule:** Smoker + Furnace + Campfire: whole fruit (owner 2026-10-25 (4.2 D1)) | Now: hunger 4; saturation 1.6 to 2.4 (median 2.4); stack 64 x5<br>Used in: can 4; meal 2; candy 1; cookie 1; feast 1; ingredient 1; unsorted 1 |
| **Vegetable** `veggie` | - | A vegetable, herb or fungus eaten on its own, raw or plainly cooked. | **Rule:** Per item, can parity 2.1 (raw: Baked Potato meal 5/6 x 2, + salt, broth or vinegar) and 2.6 (cooked: x2 x 4 servings, 6 or 5 + salt) ([can_parity.md](can_parity.md)) | **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1)) | **Rule:** none: harvested (owner 2026-10-25 (4.2 D1)) | Now: hunger 1 to 6 (median 2); saturation 0 to 9.6 (median 1.5); stack 64 x59<br>Used in: meal 51; soup 46; can 32; unsorted 32; roll 31; feast 29; salad 27; stuffed 26; pizza 25; preserve 23; 22 more |
| **Raw vegetable** `veggie/raw` | veggie | Whole, as harvested. | as veggie | as veggie | as veggie | Now: hunger 1 to 6 (median 2); saturation 0.1 to 7.2 (median 1.35); stack 64 x32<br>Used in: meal 26; soup 24; roll 23; unsorted 23; feast 22; salad 21; pizza 20; preserve 19; stuffed 16; veggie 16; 21 more |
| **Cut vegetable** `veggie/raw_cut` | veggie | Cut on a board from a raw vegetable (slices, leaves, florets, diced). | as veggie | as veggie | **Rule:** Cutting Board: whole vegetable -> pieces (owner 2026-10-25 (4.2 D1))<br>**Planned:** Cut sweet potato gives 3 sweet potato slices; cabbage gives 3 leaves with a chance of a 4th ([TODO](todo/TODO.md) dishes/ingredients #20, dishes/ingredients #8) | Now: hunger 1 to 3 (median 2); saturation 0 to 1.2 (median 0.6); stack 64 x7<br>Used in: meal 6; roll 6; salad 5; soup 5; unsorted 5; feast 4; pizza 4; preserve 4; veggie 4; ingredient 3; 9 more |
| **Cooked vegetable** `veggie/cooked` | veggie | A whole vegetable roasted, baked, boiled or grilled, nothing added. | as veggie | as veggie | **Rule:** Smoker + Furnace + Campfire: the raw vegetable; boiled ones in the Cooking Pot (owner 2026-10-25 (4.2 D1)) | Now: hunger 1 to 6 (median 5); saturation 0 to 9.6 (median 4); stack 64 x16<br>Used in: meal 15; soup 14; can 13; sandwich 7; stuffed 6; egg 5; unsorted 4; cake 2; feast 2; golden 2; 13 more |
| **Cooked cut vegetable** `veggie/cooked_cut` | veggie | A cut vegetable cooked, nothing added. | as veggie | as veggie | **Rule:** Smoker + Furnace + Campfire: the cut piece, or Cutting Board: the cooked whole (owner 2026-10-25 (4.2 D1)) | Now: hunger 2 to 3 (median 2.5); saturation 0.8 to 3.6 (median 1.05); stack 64 x4<br>Used in: meal 4; soup 3; can 2; egg 2; bread 1; feast 1; sandwich 1; stuffed 1 |
| **Meat** `meat` | - | Land animal meat, nothing added. | **Rule:** Per item, can parity 2.3 (raw: Meat Meal 5/3 x 4) and 2.7 (cooked: x2 x 3, 5 or 4 + broth; patties, bacon and exotic meats not canned) ([can_parity.md](can_parity.md)) | **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1)) | **Rule:** none: dropped (owner 2026-10-25 (4.2 D1))<br>**Planned:** Patties become their own food type (recipes, which foods open) ([TODO](todo/TODO.md) dishes/meals #14)<br>**Rule:** Unused meats (venison, goat) are removed items; Abnormals Delight's duck line takes Untitled Duck Mod's raw duck ([TODO](todo/TODO.md) food/types-and-tuning #10) | Now: hunger 0 to 15 (median 4); saturation 0 to 20 (median 3.6); stack 64 x82<br>Used in: nofamily 61; roll 61; meal 52; meat 47; soup 28; sandwich 23; skewer 21; pizza 18; feast 17; can 15; 14 more |
| **Raw meat** `meat/raw` | meat | A whole raw cut as dropped by the animal. | as meat | as meat | as meat | Now: hunger 0 to 8 (median 3); saturation 0 to 9.6 (median 2.1); stack 64 x26<br>Used in: nofamily 26; roll 26; meat 24; meal 12; soup 11; can 5; feast 5; sandwich 5; ingredient 4; lasagna 4; 7 more |
| **Raw cut meat** `meat/raw_cut` | meat | Cut or minced from raw meat (chops, bacon, mince). | as meat | as meat | **Rule:** Cutting Board: raw cut -> pieces; a seasoned cut is crafted from its raw meat (mint chops); a mob drop is dropped (hoglin ear, strider slice) (owner 2026-10-25 (4.2 D1)) | Now: hunger 1 to 4 (median 2); saturation 0.4 to 5.6 (median 1); stack 64 x16<br>Used in: nofamily 16; roll 16; meat 14; meal 9; soup 6; feast 5; lasagna 4; stuffed 4; unsorted 4; skewer 2; 3 more |
| **Cooked meat** `meat/cooked` | meat | A whole cut cooked, glazed, smoked or dried. | as meat | as meat | **Rule:** Smoker + Furnace + Campfire: the raw cut; cured, smoked, dried or glazed cuts keep their own station (Keg, Smoker, Spout, a craft) (owner 2026-10-25 (4.2 D1)) | Now: hunger 3 to 15 (median 7); saturation 0.9 to 20 (median 9.6); stack 64 x27<br>Used in: meal 20; nofamily 11; roll 11; pizza 10; sandwich 10; skewer 10; can 8; meat 8; soup 7; feast 6; 9 more |
| **Cooked cut meat** `meat/cooked_cut` | meat | A cut, minced or patty piece cooked. | as meat | as meat | **Rule:** Smoker + Furnace + Campfire: the raw piece (owner 2026-10-25 (4.2 D1)) | Now: hunger 3 to 7 (median 4); saturation 2.4 to 14 (median 4.8); stack 64 x13<br>Used in: meal 11; nofamily 8; pizza 8; roll 8; skewer 8; sandwich 7; quiche 4; snack 4; soup 4; stuffed 3; 8 more |
| **Fish and seafood** `fish` | - | Fish, shellfish and squid, nothing added. | **Rule:** Per item, can parity 2.4 (raw: Fish Meal 4/2 x 4; slices share the whole fish's count) and 2.8 (cooked: x2 x 3, from the cut) ([can_parity.md](can_parity.md)) | **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1)) | **Rule:** none: caught (owner 2026-10-25 (4.2 D1))<br>**Planned:** Fish eggs accepted in the fish egg roll; salmon eggs renamed caviar (caviar in the sea pickle roll); any tentacle cuts to calamari ([TODO](todo/TODO.md) dishes/ocean #2, dishes/ocean #5, dishes/ocean #4) | Now: hunger 1 to 10 (median 3); saturation 0.2 to 42 (median 2.1); stack 64 x52<br>Used in: roll 40; meal 37; nofamily 32; can 31; soup 30; fish 29; pizza 16; unsorted 15; stuffed 14; skewer 8; 6 more |
| **Raw fish** `fish/raw` | fish | A whole raw catch. | as fish | as fish | as fish | Now: hunger 1 to 10 (median 2.5); saturation 0.2 to 42 (median 1.05); stack 64 x20<br>Used in: roll 20; fish 15; meal 13; can 12; nofamily 11; soup 11; stuffed 9; unsorted 9; feast 2; hazard 1; 2 more |
| **Raw cut fish** `fish/raw_cut` | fish | Cut from a raw catch (slices, fillets). | as fish | as fish | **Rule:** Cutting Board: whole fish -> slices (salmon, cod, catfish 6); a mob drop is dropped (guardian fillet, squid tentacles) ([recipes_cooking.md](recipes_cooking.md) 2.1; owner 2026-10-25 (4.2 D1)) | Now: hunger 1 to 4 (median 1); saturation 0.2 to 4 (median 0.4); stack 64 x11<br>Used in: roll 11; fish 8; meal 8; soup 7; can 6; nofamily 5; stuffed 5; unsorted 5; cookie 1; sandwich 1; 1 more |
| **Cooked fish** `fish/cooked` | fish | A whole catch cooked or smoked. | as fish | as fish | **Rule:** Smoker + Furnace + Campfire: the raw fish; cured or fermented fish keeps its own station (Keg, Fermenting Jar) (owner 2026-10-25 (4.2 D1)) | Now: hunger 5 to 9 (median 6); saturation 4.8 to 15.3 (median 7); stack 64 x13<br>Used in: meal 10; nofamily 10; pizza 10; can 7; soup 7; fish 6; roll 5; skewer 5; golden 2; feast 1; 2 more |
| **Cooked cut fish** `fish/cooked_cut` | fish | A cut piece cooked. | as fish | as fish | **Rule:** Smoker + Furnace + Campfire: the raw slice (owner 2026-10-25 (4.2 D1)) | Now: hunger 2 to 8 (median 3); saturation 1.8 to 11.2 (median 3.3); stack 64 x8<br>Used in: can 6; meal 6; nofamily 6; pizza 6; soup 5; roll 4; skewer 3; sandwich 1 |
| **Egg** `egg` | - | Eggs as the main part of the food. | **Rule:** Never canned (precedent); per item, can parity 2.20 ([can_parity.md](can_parity.md)) | **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1)) | **Rule:** Campfire / Skillet + Smoker + Furnace: one egg; scrambled eggs in the Cooking Pot; a cured egg (century egg) its own station (owner 2026-10-25 (4.2 D1))<br>**Planned:** Cooking oil + 3 eggs -> 3 fried eggs; Cooking Pot 4 fried eggs -> 4 omelettes ([TODO](todo/TODO.md) dishes/meals #23)<br>**Planned:** Any egg (`#forge:eggs`) in egg recipes via `R.replaceInput`; turtle and parrot eggs open ([TODO](todo/TODO.md) food/types-and-tuning #6) | Now: hunger 4 to 20 (median 10); saturation 3.2 to 20 (median 14.6); stack 32 x9, 64 x6, 1 x1; returns bowl 8<br>Used in: egg 3; sandwich 2; meal 1; roll 1; soup 1 |
| **Cooked egg** `egg/cooked` | egg | One egg fried, boiled or scrambled, nothing else. | as egg | as egg | as egg | Now: hunger 4 to 20 (median 5.5); saturation 3.2 to 20 (median 7.6); stack 64 x5, 1 x1; returns bowl 1<br>Used in: egg 2; sandwich 2; meal 1; roll 1; soup 1 |
| **Egg dish** `egg/dish` | egg | Eggs with other parts on a plate or bowl (omelet, shakshouka). | as egg | **Rule:** Display Delight placed (patcherMod adds a placed block for each dish Display Delight has none for) (owner 2026-10-25 (4.2 D1)) | **Rule:** Pot meal rule: Cooking Pot, plus a heatless `_manual_only` shapeless craft from cooked forms (one more main ingredient) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)<br>**Planned:** Veggie omelette: the omelette ingredient swapped for tomato, beetroot, any bell pepper or ghost pepper (exact recipe open) ([TODO](todo/TODO.md) dishes/meals #24) | Now: hunger 6 to 12 (median 10); saturation 7.2 to 19.2 (median 16); stack 32 x9, 64 x1; returns bowl 7<br>Used in: egg 1 |
| **Dairy** `dairy` | - | Cheese and yogurt eaten on their own. | **Rule:** Cheese wedges, 4 per can in the Cooking Pot, x1.25 x 4 servings (can parity 2.20) ([can_parity.md](can_parity.md)) | **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1)) | **Rule:** none: cut from a cheese wheel (owner 2026-10-25 (4.2 D1))<br>**Planned:** 9 cheese wedges craft back to the cheese block, for each cheese; more cheese types ([TODO](todo/TODO.md) dishes/ingredients #9) | **Planned:** Stack: cheese wheel 32 ([TODO](todo/TODO.md) food/stack-sizes #1)<br>Now: hunger 4; saturation 6.4 to 8 (median 8); stack 64 x3<br>Used in: egg 3; feast 3; pizza 3; quiche 3; roll 3; sandwich 3; soup 3; can 2; lasagna 2; meal 2; 3 more |
| **Bread** `bread` | - | Baked plain or lightly flavoured dough with no filling. | **Rule:** x2 x 4 servings, 6 bread; slices, toast, tortilla and scones not canned (can parity 2.9) ([can_parity.md](can_parity.md)) | **Decision:** none required | **Rule:** Bread only: dough block smoked into a bread block, sawed or knifed into 16 bread ([recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Decision:** Smoker + Furnace + Campfire: the dough<br>**Planned:** Bread block cut to a crustless bread block (standard slab recipe) ([TODO](todo/TODO.md) baking/baked-goods #10) | Now: hunger 2 to 7 (median 4.5); saturation 1.6 to 14 (median 4); stack 64 x10<br>Used in: sandwich 9; bread 8; dessert 8; feast 8; golden 8; meal 8; salad 8; soup 8; can 7; snack 1; 2 more |
| **Loaf** `bread/loaf` | bread | A whole loaf or round bread. | as bread | as bread | as bread | Now: hunger 4 to 7 (median 5); saturation 1.6 to 14 (median 6); stack 64 x7<br>Used in: can 7; bread 6; dessert 6; feast 6; golden 6; meal 6; salad 6; sandwich 6; soup 6 |
| **Bread slice** `bread/slice` | bread | A slice of a loaf, toasted or not. | as bread | as bread | **Decision:** Cutting Board: the loaf -> slices; toast = slice in the trio<br>**Planned:** Honeycomb spreads on toast ([TODO](todo/TODO.md) dishes/ingredients #22) | **Rule:** Every toast eats fast (16 ticks, like dried kelp); CD moredelight/toast.js ([more_delight.md](more_delight.md) 2.3)<br>Now: hunger 2 to 3 (median 2.5); saturation 1.6 to 2.4 (median 2); stack 64 x2<br>Used in: bread 2; dessert 2; feast 2; golden 2; meal 2; salad 2; sandwich 2; soup 2; unsorted 1 |
| **Flatbread** `bread/flat` | bread | Tortillas and flatbreads baked as one piece (sweet buns are desserts). | as bread | as bread | as bread | Now: hunger 3; saturation 2.4; stack 64 x1<br>Used in: sandwich 1; snack 1; stuffed 1 |
| **Kitchen ingredient** `ingredient` | - | Edible but made to go into other food, not to be eaten as a dish. | **Rule:** Per item, can parity 2.26 (Canned Dough: 8 dough, not edible; tomato sauce in the preserve section) ([can_parity.md](can_parity.md)) | **Decision:** none required | **Rule:** One flour and one dough (Create's) in every recipe that takes them ([recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Rule:** Pie crust: 3 flour + egg + milk, or 3 dough + milk, manual craft -> 3 ([oven.md](oven.md) 2.6)<br>**Planned:** One dough system: Create dough, `#forge:dough`, sweet potato dough, sourdough and cookie dough count as dough; a texture for each dough and a recoloured dough block for each ([TODO](todo/TODO.md) baking/baked-goods #17, baking/baked-goods #27)<br>**Planned:** Sourdough chain (starter -> growing -> fermenting -> sourdough, timed) and a yeast recipe from mushrooms ([TODO](todo/TODO.md) baking/baked-goods #7, baking/baked-goods #8)<br>**Planned:** Batter from wheat flour or cornmeal, used in more fried foods and as a cake base route ([TODO](todo/TODO.md) dishes/ingredients #7)<br>**Planned:** Fats: animal fat (cows, pigs, nether cows) cooks into cooking oil, which takes animal oil's crafts; blubber and whale oil work the same; fish oil counts as cooking oil ([TODO](todo/TODO.md) dishes/ingredients #10)<br>**Planned:** Butter: a new item churned from milk or cream (open) ([TODO](todo/TODO.md) dishes/ingredients #15)<br>**Planned:** Vinegar: 3 apple mash + an enzyme (crushed fungus); foods with vinegar added carry Foul Taste ([TODO](todo/TODO.md) dishes/pickles-and-ferments #6, dishes/pickles-and-ferments #7) | **Planned:** Stack: bowl and bottle ingredients 64 ([TODO](todo/TODO.md) food/stack-sizes #1)<br>Now: hunger 0 to 14 (median 2.5); saturation 0 to 14 (median 1.2); stack 64 x36; returns empty_mason_jar 4, bowl 3, glass_bottle 2; block items 7<br>Used in: meal 14; ingredient 10; dessert 9; unsorted 9; cake 7; lasagna 7; soup 7; bread 6; roll 5; cookie 4; 17 more |
| **Dough and batter** `ingredient/dough` | ingredient | Unbaked dough, batter and crust, edible or not. | as ingredient | as ingredient | as ingredient | as ingredient<br>Now: hunger 1 to 3 (median 2); saturation 0.4 to 1.2 (median 0.8); stack 64 x7; returns bowl 1; block items 7<br>Used in: bread 6; cake 5; dessert 5; ingredient 5; meal 4; unsorted 4; lasagna 3; pizza 3; roll 3; snack 3; 5 more |
| **Sauce and fat** `ingredient/sauce` | ingredient | Sauces, oils and fats. | as ingredient | as ingredient | as ingredient | as ingredient<br>Now: hunger 1 to 8 (median 3); saturation 1.2 to 11.2 (median 3); stack 64 x7; returns bowl 2, empty_mason_jar 1, glass_bottle 1<br>Used in: meal 4; dessert 2; egg 2; unsorted 2; can 1; candy 1; feast 1; hazard 1; ingredient 1; lasagna 1; 8 more |
| **Other ingredient** `ingredient/other` | ingredient | Leaves, beans, pods, mash, mince, raw pasta and noodles for other recipes. | as ingredient | as ingredient | as ingredient | as ingredient<br>Now: hunger 0 to 14 (median 2.5); saturation 0 to 14 (median 1.4); stack 64 x22; returns empty_mason_jar 3, glass_bottle 1<br>Used in: meal 6; soup 6; drink 4; ingredient 4; lasagna 3; unsorted 3; cake 2; cookie 2; dessert 2; egg 2; 8 more |
| **Preserve** `preserve` | - | Fruit or vegetables kept in sugar, salt or brine. | **Rule:** Jams and sauces not canned (precedent); preserve section x1 x 4 (tomato sauce); per item, can parity 2.21 ([can_parity.md](can_parity.md)) | **Decision:** none required | **Decision:** Cooking Pot: fruit + sugar in a jar (Mixer mirrors it) | Now: hunger 2 to 12 (median 4); saturation 1.6 to 12.6 (median 2.4); stack 64 x36, 32 x1; returns glass_bottle 25, empty_mason_jar 1<br>Used in: meal 25; salad 24; sandwich 23; candy 22; soup 7; can 3; preserve 3; dessert 2; ingredient 2; unsorted 1 |
| **Jam** `preserve/jam` | preserve | Fruit boiled with sugar, in a jar or bottle. | as preserve | **Rule:** Own block, while sneaking (Fruits Delight jams) ([placeable_food.md](placeable_food.md) 1) | **Decision:** Cooking Pot: fruit + sugar in a jar (Mixer mirrors it)<br>**Planned:** Pot recipe: 4 fruit + 1 sugar + 1 lemon slice -> 2 jam ([TODO](todo/TODO.md) dishes/jams #2)<br>**Planned:** One jam item (Fruits Delight) and one jam block (Vintage Delight), always a mason jar recoloured to the jam; recipes take jam, never bottled jam or jelly ([TODO](todo/TODO.md) dishes/jams #1, dishes/jams #3) | Now: hunger 4 to 9 (median 4); saturation 1.6 to 12.6 (median 2.4); stack 64 x24; returns glass_bottle 23, empty_mason_jar 1<br>Used in: candy 22; meal 22; salad 22; sandwich 22; dessert 2; ingredient 1 |
| **Spread** `preserve/spread` | preserve | A non-fruit spread in a jar (nut butter). | as preserve | as preserve | **Decision:** Cooking Pot: fruit + sugar in a jar (Mixer mirrors it)<br>**Planned:** Nut butter and nut mash merge into one item ([TODO](todo/TODO.md) dishes/jams #5) | Now: hunger 6; saturation 7.2; stack 64 x1; returns glass_bottle 1<br>Used in: sandwich 1; unsorted 1 |
| **Pickle** `preserve/pickle` | preserve | Vegetables fermented or pickled (kimchi, pickles). | as preserve | as preserve | **Decision:** Keg: vegetable + salt / brine<br>**Planned:** Recipes take real pickles only (not Brewin's pickled pickles or raw sea pickles); a Pickled Sea Pickle without honey ([TODO](todo/TODO.md) dishes/pickles-and-ferments #1, dishes/pickles-and-ferments #2)<br>**Planned:** Enzymes from crushed or milled mushrooms ([TODO](todo/TODO.md) dishes/pickles-and-ferments #5)<br>**Planned:** Kimchi: Vintage Delight's, any pickleable vegetable as the base ([TODO](todo/TODO.md) dishes/pickles-and-ferments #8)<br>**Planned:** Century egg takes a pickled egg ([TODO](todo/TODO.md) dishes/pickles-and-ferments #9) | Now: hunger 2 to 12 (median 6); saturation 2.4 to 9.8 (median 3.8); stack 64 x11, 32 x1; returns glass_bottle 1<br>Used in: soup 7; can 3; meal 3; preserve 3; salad 2; ingredient 1 |
| **Jello** `jello` | - | A set gelatin dessert of one fruit (Fruits Delight jellos and the like). | **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md)) | **Decision:** Display Delight placed | **Decision:** Crafting: jam + gelatin in a bowl (Spout mirrors it) | **Planned:** Stack: bowl foods including jello 32 ([TODO](todo/TODO.md) food/stack-sizes #1)<br>Now: hunger 1 to 8 (median 1); saturation 0.6 to 11.2 (median 0.6); stack 16 x22, 8 x1, 64 x1; returns bowl 22<br>Used in: none |
| **Pie** `pie` | - | A crust filled and baked; whole pies are blocks cut into slices. | **Rule:** 4 slices -> 1 filling can (4 slice-sized servings); crust + full can -> the whole pie; tart cans are pie fillings (can parity 2.13) ([can_parity.md](can_parity.md)) | **Decision:** none required | **Decision:** see subfamilies | Now: hunger 3 to 5 (median 3); saturation 0.6 to 4.8 (median 1.8); stack 64 x25, 16 x1; block items 10<br>Used in: pie 20; can 12; golden 1 |
| **Whole pie** `pie/whole` | pie | The placed block, eaten or cut in slices. | as pie | **Rule:** Own block ([placeable_food.md](placeable_food.md) 1) | **Rule:** Oven (pie tin): pie crust + 2 fruit + 2 sugar (honey allowed) ([oven.md](oven.md) 2.6)<br>**Rule:** Oven or Crafting: pie crust + a full can of its filling (all but durian, pineapple, sponge and End's chorus fruit pie: Ender's chorus pie owns the chorus filling can) ([oven.md](oven.md) 2.6)<br>**Rule:** No other recipe (every other mod's pie recipe removed) ([oven.md](oven.md) 2.6)<br>**Planned:** Sequenced assembly for every pie ([TODO](todo/TODO.md) baked-goods #4)<br>**Planned:** No shapeless recipe turns a pie into a crust (none found 2026-10-24; check in JEI) ([TODO](todo/TODO.md) baking/baked-goods #24)<br>**Planned:** Durian and pineapple pies: a 4-slice placed pie, or a non-pie item with its own name, recipe and can ([TODO](todo/TODO.md) baking/baked-goods #12)<br>**Planned:** Cherry pie: an item placing `create_central_kitchen:cherry_pie`; crust + 2 cherries + 2 sugar, knife to 4 slices, a filling can ([TODO](todo/TODO.md) baking/baked-goods #13) | Now: block items only<br>Used in: pie 10; golden 1 |
| **Pie slice** `pie/slice` | pie | One slice of a whole pie. | as pie | **Decision:** Small Food Plate | **Decision:** Cutting Board: whole pie -> slices<br>**Planned:** Slices whose whole pie is from a mod not in the pack: add the pie or hide the slice, per slice ([TODO](todo/TODO.md) baking/baked-goods #14) | Now: hunger 3 to 5 (median 3); saturation 0.6 to 4.8 (median 1.8); stack 64 x19<br>Used in: pie 10; can 8 |
| **Tart and hand pie** `pie/small` | pie | A one-serving pie or tart sold as an item. | as pie | as pie | **Rule:** Tarts: Oven (baking sheet): fruit + pie crust + egg + sugar + milk -> 2 ([oven.md](oven.md) 2.6)<br>**Rule:** Hand pies: Oven only (baking sheet): pie crust + the filling -> 1 (CD handPieBake) ([oven.md](oven.md) 2.6)<br>**Planned:** Every tart becomes a pie (renamed, retextured) ([TODO](todo/TODO.md) baked-goods #6) | Now: hunger 4 to 5 (median 5); saturation 2.4 to 4.8 (median 4); stack 64 x6, 16 x1<br>Used in: can 4 |
| **Cake** `cake` | - | Sweet sponge with frosting; whole cakes are blocks cut into slices. | **Rule:** 1 cake, or 4 slices + sugar or honey -> 4 fast servings of 1.75 slices (can parity 2.23) ([can_parity.md](can_parity.md)) | **Decision:** none required | **Decision:** see subfamilies | Now: hunger 1 to 9 (median 2); saturation 0.2 to 10.8 (median 0.4); stack 64 x23, 16 x1; returns bowl 1; block items 14<br>Used in: cake 29; can 3; golden 1 |
| **Whole cake** `cake/whole` | cake | The placed block, eaten or cut in slices. | as cake | **Rule:** Own block ([placeable_food.md](placeable_food.md) 1) | **Rule:** Cakes: Crafting (manual): baked cake base + frosting + 2 flavour ([oven.md](oven.md) 2.6)<br>**Rule:** Cakes: Oven (cake pan): raw cake base + frosting + 2 flavour ([oven.md](oven.md) 2.6)<br>**Rule:** Cakes: no other recipe (every other mod's cake recipe removed; `_from_slices` reassembly kept; plain and chocolate cake also spout onto a baked base) ([oven.md](oven.md) 2.6)<br>**Planned:** Every cake becomes a sequenced assembly (baked base, frosting, flavour), not only a grid or oven recipe; the existing spouts (plain, chocolate) need more steps ([TODO](todo/TODO.md) baking/baked-goods #28)<br>**Rule:** Honey cake: 1 honey bottle + 1 honeycomb as its 2 flavour ([TODO](todo/TODO.md) baking/baked-goods #26)<br>**Planned:** Mangosteen cake renamed (not a cake); a dragon egg cake ([TODO](todo/TODO.md) baking/baked-goods #5)<br>**Planned:** Cake paste goes into the Cooking Pot ([TODO](todo/TODO.md) baking/baked-goods #6) | Now: hunger 9; saturation 10.8; stack 16 x1; returns bowl 1; block items 14<br>Used in: cake 14; can 1; golden 1 |
| **Cake slice** `cake/slice` | cake | One slice of a whole cake. | as cake | **Planned:** All slices placeable (method not stated; proposal: Small Food Plate) ([TODO](todo/TODO.md) baked-goods #5) | **Decision:** Cutting Board: whole cake -> slices<br>**Planned:** Unobtainable slices (7 Central Kitchen, its pumpkin pie slice, 6 Delightful) audited and removed ([TODO](todo/TODO.md) baking/baked-goods #5) | Now: hunger 1 to 3 (median 1); saturation 0.2 to 1.8 (median 0.2); stack 64 x20<br>Used in: cake 15; can 2 |
| **Muffin and cupcake** `cake/muffin` | cake | A one-serving cake baked in a muffin tin: muffins, cupcakes (brownies are desserts). | as cake | as cake | **Rule:** Oven (muffin tin, on paper): 2 fruit + wheat flour + milk + egg + sugar -> 4; Cooking Pot: 2 fruit + dough + milk + egg + sugar -> 2 (CD muffinBake) ([oven.md](oven.md) 2.6)<br>**Planned:** Brownie and cupcake baking types: the muffin stays unfrosted, the cupcake is frosted ([TODO](todo/TODO.md) baking/baked-goods #18)<br>**Planned:** Muffin variants: fruit + milk + egg + sugar + flour or cornmeal, served on paper, sequenced assembly ([TODO](todo/TODO.md) baking/baked-goods #2) | Now: hunger 5 to 6 (median 5); saturation 4 to 6 (median 4); stack 64 x3<br>Used in: none |
| **Cookie** `cookie` | - | A small flat baked biscuit. | **Rule:** x2 x 5 fast servings, 8 cookies or 6 + sugar (can parity 2.5) ([can_parity.md](can_parity.md)) | **Decision:** Supplementaries jar (`#supplementaries:cookies`) | **Rule:** Oven only (baking sheet): cookie dough + 2 flavour -> 12 ([oven.md](oven.md) 2.6)<br>**Rule:** No other recipe ([oven.md](oven.md) 2.6)<br>**Planned:** Oatmeal cookie takes oat dough; oatmeal raisin cookie: oat dough + 1 raw oat + 1 grape ([TODO](todo/TODO.md) baking/baked-goods #23)<br>**Planned:** Chorus cookie made from the uncanny cookie; a maple cookie next to the cherry cookie ([TODO](todo/TODO.md) baking/baked-goods #22, baking/baked-goods #15) | Now: hunger 1 to 5 (median 2); saturation 0.1 to 6 (median 0.6); stack 64 x27<br>Used in: can 12; candy 1; cookie 1; dessert 1; golden 1 |
| **Dessert** `dessert` | - | Sweet bakes and set desserts that are not cake, pie, cookie, candy, jello or frozen; also the catchall for any sweet or baked food with no other family. | **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md)) | **Decision:** Food Plate | **Decision:** Oven for the plain bake; toppings by Spout<br>**Planned:** More waffle recipes and uses ([TODO](todo/TODO.md) baking/baked-goods #6)<br>**Decision:** A dessert can get its own cookware or recipe treatment (a mould, a griddle, a press), not the cookie, cake or pie shape; Gingerbread is the first (Create Confectionery's gingerdough: furnace, smoker); the Gingerbread Man stays a cookie | Now: hunger 0 to 12 (median 6); saturation 0 to 19.2 (median 7.2); stack 64 x33, 32 x4, 1 x1, 16 x1; returns bowl 3, glass_bottle 2; block items 2<br>Used in: dessert 5; can 4 |
| **Brownie** `dessert/brownie` | dessert | A brownie, or the tray of brownies it is cut from. | as dessert | **Decision:** Tray: own block (cut into brownies) | **Decision:** Oven: the tray (cake pan); Cutting Board: tray -> brownies | Now: hunger 6; saturation 6 to 7.2 (median 6); stack 64 x3; block items 1<br>Used in: dessert 2 |
| **Pudding** `dessert/pudding` | dessert | Soft set desserts in a bowl (custard, pudding, yogurt desserts) and steamed puddings cut in slices. | **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md)) | **Decision:** Display Delight placed | **Decision:** Bowl puddings: Cooking Pot (Mixer mirrors it); fig pudding: Oven, cut on the board | Now: hunger 3 to 10 (median 6); saturation 2.4 to 14.4 (median 9.2); stack 32 x4, 1 x1, 64 x1; returns bowl 2, glass_bottle 2; block items 1<br>Used in: dessert 2 |
| **Pancake** `dessert/pancake` | dessert | Pancakes from every mod: single pancakes, pancake dishes and the pancake stack blocks. No base item: each pancake keeps its own recipes (owner 2026-11-02). | as dessert | **Rule:** Every pancake can be put down: Rustic's plates and Supplementaries' Pancake are their own stack blocks (a single Rustic pancake goes back on its plate with sneak-use), Sweet Potato Pancakes a Display Delight placed dish (owner 2026-11-02 ([TODO](todo/TODO.md) merges/baking #2, dishes/meals #12; 4.7)) | **Rule:** No base item: every pancake keeps its own recipes (Rustic plates: Cooking Pot, cut into single pancakes; Supplementaries' Pancake: by hand and the heated Mixer; Sweet Potato Pancakes: Cooking Pot) (owner 2026-11-02 ([TODO](todo/TODO.md) merges/baking #2, dishes/meals #12; 4.7))<br>**Rule:** Food type `sweet/pancake` for every pancake eaten as an item (food.md 2.1) (owner 2026-11-02 ([TODO](todo/TODO.md) merges/baking #2, dishes/meals #12; 4.7))<br>**Planned:** Cooking Pot for the Supplementaries Pancake (3 batter + 2 sugar + 1 egg -> 4) ([TODO](todo/TODO.md) merges/baking #1)<br>**Planned:** Coverings recoloured to the jam colour ([TODO](todo/TODO.md) dishes/meals #37) | Now: hunger 4 to 12 (median 4); saturation 4.8 to 19.2 (median 4.8); stack 64 x7, 16 x1; returns bowl 1<br>Used in: none |
| **Pastry** `dessert/pastry` | dessert | Scones, eclairs, beignets and sweet buns. | as dessert | as dessert | as dessert | Now: hunger 5; saturation 0.5 to 6 (median 4); stack 64 x3<br>Used in: can 1 |
| **Waffle** `dessert/waffle` | dessert | A waffle, plain or topped. | as dessert | as dessert | as dessert | Now: hunger 4 to 11 (median 9); saturation 3.2 to 15.4 (median 12.6); stack 64 x7<br>Used in: dessert 1 |
| **Sweet roll** `dessert/roll` | dessert | A rolled sweet dough (sweet roll, chocolate roll). | as dessert | as dessert | as dessert | Now: hunger 6 to 8 (median 7); saturation 6 to 11.2 (median 8.4); stack 64 x7<br>Used in: can 1 |
| **Other dessert** `dessert/other` | dessert | A sweet or baked food that fits no other family. | as dessert | as dessert | as dessert | Now: hunger 0 to 9 (median 6); saturation 0 to 9.6 (median 7.2); stack 64 x5<br>Used in: can 2 |
| **Candy** `candy` | - | Sugar or chocolate confections. | **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md)) | **Decision:** Supplementaries jar | **Decision:** Spout: coating or filling (chocolate, caramel, honey) over the base<br>**Planned:** Gummies for most fruits (slime balls and kelp allowed): a manual craft plus a more efficient pot / Create craft ([TODO](todo/TODO.md) baking/sweets #1)<br>**Planned:** 4 or 5 more rock candy variants from corundum; rose quartz kept ([TODO](todo/TODO.md) baking/sweets #2) | Now: hunger 1 to 10 (median 6); saturation 0 to 12.8 (median 6.4); stack 64 x65, 16 x4; returns stick 4, bowl 1<br>Used in: candy 12; can 3; drink 3; frozen 3; cake 2; pie 2; dessert 1; ingredient 1; soup 1 |
| **Chocolate bar** `candy/bar` | candy | A bar of chocolate or caramel. | as candy | as candy | **Decision:** Press + Basin: the fluid into a bar | Now: hunger 4 to 6 (median 6); saturation 2 to 6.6 (median 3.6); stack 64 x8<br>Used in: candy 6; cake 2; can 2; drink 2; frozen 2; pie 2 |
| **Candy piece** `candy/piece` | candy | A bite of candy, fudge, gummy, coin or leather. | as candy | as candy | as candy | Now: hunger 1 to 8 (median 5); saturation 0 to 12.8 (median 6.4); stack 64 x36, 16 x1; returns stick 2<br>Used in: candy 2; dessert 1; frozen 1; ingredient 1; soup 1 |
| **Coated fruit** `candy/coated` | candy | A fruit or nut dipped in chocolate, caramel, honey or sugar. | as candy | as candy | as candy | Now: hunger 4 to 10 (median 7); saturation 0.8 to 12.8 (median 11); stack 64 x13, 16 x1; returns bowl 1<br>Used in: can 1 |
| **Marshmallow** `candy/marshmallow` | candy | Marshmallows, on a stick or in a s'more. | as candy | as candy | as candy | Now: hunger 2 to 8 (median 5); saturation 1.6 to 9.6 (median 6); stack 64 x8, 16 x2; returns stick 2<br>Used in: candy 4; drink 1 |
| **Frozen dessert** `frozen` | - | Served frozen. | **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md)) | **Decision:** Display Delight placed (ice cream), Food Plate (popsicles) | **Rule:** Every ice food takes `#forge:ice_cubes` ([recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Planned:** Ice cream: unified recipes, a fluid per flavour with mixing recipes, bowl spouting, an ice cream block per flavour (BlockRunner: sticky, sinks in); Truffle Ice Cream joins as a flavour ([TODO](todo/TODO.md) baking/sweets #4)<br>**Planned:** More custard, shaved ice and popsicle variants ([TODO](todo/TODO.md) baking/sweets #3) | Now: hunger 1 to 12 (median 6); saturation 0.4 to 12.8 (median 3.6); stack 32 x10, 64 x7, 16 x3; returns bowl 10, stick 2, glass_bottle 1<br>Used in: drink 5; unsorted 2; frozen 1 |
| **Ice cream** `frozen/ice_cream` | frozen | Churned ice cream in a bowl. | as frozen | as frozen | as frozen | Now: hunger 6 to 12 (median 6); saturation 3.6 to 12.8 (median 4.8); stack 32 x10, 16 x1; returns bowl 10<br>Used in: drink 5; unsorted 2; frozen 1 |
| **Popsicle** `frozen/popsicle` | frozen | Frozen juice, fruit or flavour on a stick (every popsicle, melon and cantaloupe to corn and ender). | as frozen | as frozen | as frozen | Now: hunger 3 to 4 (median 3.5); saturation 1.2 to 3.2 (median 1.4); stack 64 x7, 16 x1; returns stick 2<br>Used in: none |
| **Shaved ice** `frozen/shaved_ice` | frozen | Shaved ice with a fruit flavour. | as frozen | as frozen | as frozen | Now: hunger 1; saturation 0.4; stack 16 x1; returns glass_bottle 1<br>Used in: none |
| **Sandwich** `sandwich` | - | Fillings held in bread or a wrap, eaten by hand. | **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** Food Plate | **Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers | Now: hunger 3 to 15 (median 10); saturation 1.2 to 28.5 (median 14.4); stack 64 x48<br>Used in: none |
| **Square sandwich** `sandwich/square` | sandwich | Square bread slices around a filling (the sprite shows slices). | as sandwich | as sandwich | **Planned:** Nut butter sandwich: more jam options, square bread only ([TODO](todo/TODO.md) dishes/meals #31)<br>**Planned:** 2 bread slices (`#forge:bread_slices`, toast counts) in place of bread ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))<br>**Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers | Now: hunger 7 to 11 (median 10); saturation 8.8 to 20 (median 15); stack 64 x8<br>Used in: none |
| **Long roll sandwich** `sandwich/roll` | sandwich | A long roll (whole bread) around a filling. | as sandwich | as sandwich | **Planned:** Whole bread (`#forge:bread`), kept ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))<br>**Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers | Now: hunger 3 to 11 (median 10); saturation 1.2 to 17.6 (median 11.2); stack 64 x11<br>Used in: none |
| **Burger** `sandwich/burger` | sandwich | A bun around a patty. | as sandwich | as sandwich | **Rule:** Shaped only: toppings top, patty middle, bread bottom ([recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Rule:** Takes any `#forge:bread` ([recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Rule:** One sequenced assembly each ([recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Planned:** A bun (new item `burger_bun`, oven / smoker / knife on bread) in place of bread; Kangaroo Burger shows a long roll and keeps bread ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))<br>**Planned:** Fermented garlic and onion accepted in place of the raw ones in burgers ([TODO](todo/TODO.md) dishes/meals #29) | Now: hunger 7 to 15 (median 11.5); saturation 11.2 to 28.5 (median 18.95); stack 64 x14<br>Used in: none |
| **Wrap** `sandwich/wrap` | sandwich | A tortilla or leaf rolled or folded around a filling (wraps, tacos). | as sandwich | as sandwich | **Planned:** A tortilla in place of bread (mutton wrap: TODO dishes/meals #2) ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))<br>**Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers | Now: hunger 8 to 12 (median 10); saturation 13.2 to 19.2 (median 15.2); stack 64 x7<br>Used in: none |
| **Open toast** `sandwich/toast` | sandwich | One slice of bread or toast with a topping. | as sandwich | as sandwich | **Decision:** Crafting: bread slice or toast + topping<br>**Planned:** Bruschetta: 2 toast in place of bread ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md)) | **Rule:** Every toast eats fast (16 ticks, like dried kelp); CD moredelight/toast.js ([more_delight.md](more_delight.md) 2.3)<br>Now: hunger 4 to 10 (median 5); saturation 6 to 20 (median 6); stack 64 x8<br>Used in: none |
| **Skewer** `skewer` | - | Pieces on a stick, eaten by hand; the stick is kept. | **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** Food Plate | **Decision:** Shaped only + one sequenced assembly (as Crispy Skewer, [recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Planned:** Skewers become their own food type (recipes, which foods open) ([TODO](todo/TODO.md) dishes/meals #13) | Now: hunger 2 to 11 (median 7.5); saturation 0.8 to 18 (median 12.1); stack 64 x8; returns stick 3, blaze_rod 1<br>Used in: skewer 1 |
| **Roll and dumpling** `roll` | - | A filling rolled or folded in a wrapper: sushi rolls, cabbage rolls, dumplings. | **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** Food Plate (whole roll), Small Food Plate (slice) | **Decision:** Crafting: wrapper + filling | Now: hunger 4 to 15 (median 7); saturation 2.4 to 30 (median 8.4); stack 64 x22, 32 x3; returns bowl 2<br>Used in: feast 4; roll 3; can 1; unsorted 1 |
| **Stuffed vegetable** `stuffed` | - | A vegetable hollowed and filled (stuffed potato, peppers). | **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** Food Plate | **Decision:** Crafting + one sequenced assembly on the vegetable (as Stuffed Potato)<br>**Planned:** Stuffed pepper plate: red, yellow and green peppers + rice, meat and onion; sequenced assembly on a bowl plus a less efficient manual craft (or drop the dish) ([TODO](todo/TODO.md) dishes/meals #16) | Now: hunger 3 to 14 (median 10); saturation 1.2 to 25.2 (median 14); stack 64 x7, 32 x4; returns bowl 2<br>Used in: feast 3; stuffed 1 |
| **Pizza** `pizza` | - | Dough topped with sauce, cheese and toppings, baked whole and cut. | **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** see subfamilies | **Decision:** see subfamilies | Now: hunger 4 to 5 (median 5); saturation 5 to 10 (median 6.75); stack 64 x4; block items 4<br>Used in: pizza 8 |
| **Whole pizza** `pizza/whole` | pizza | The placed block, cut in slices. | as pizza | **Rule:** Own block ([placeable_food.md](placeable_food.md) 1) | **Decision:** One shaped recipe + one sequenced assembly (as Brewin' pizza, [recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Planned:** 8 slices per pizza, dearer dough and cheese; Brewin' pizzas merge into Deluxe Pizza, plus a Tropical Pizza (pineapple, bacon) ([TODO](todo/TODO.md) dishes/meals #5, dishes/meals #26, dishes/meals #27, baking/baked-goods #20) | Now: block items only<br>Used in: pizza 4 |
| **Pizza slice** `pizza/slice` | pizza | One slice of a whole pizza. | as pizza | **Decision:** Small Food Plate | **Decision:** Cutting Board: whole pizza -> slices | Now: hunger 4 to 5 (median 5); saturation 5 to 10 (median 6.75); stack 64 x4<br>Used in: pizza 4 |
| **Quiche** `quiche` | - | A savory egg and cheese custard baked in a pie crust. | **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** see subfamilies | **Decision:** see subfamilies | Now: hunger 4 to 6 (median 5); saturation 6 to 6.4 (median 6.2); stack 64 x2; block items 2<br>Used in: quiche 4 |
| **Whole quiche** `quiche/whole` | quiche | The placed block, cut in slices. | as quiche | **Rule:** Own block ([placeable_food.md](placeable_food.md) 1) | **Decision:** Shaped recipe on a pie crust + one sequenced assembly on a pie crust (as Brewin' quiche, [recipes_cooking.md](recipes_cooking.md) 2.1)<br>**Planned:** Quiche: a pie variant for vegetables ([TODO](todo/TODO.md) baking/baked-goods #19) | Now: block items only<br>Used in: quiche 2 |
| **Quiche slice** `quiche/slice` | quiche | One slice of a whole quiche. | as quiche | **Decision:** Small Food Plate | **Decision:** Cutting Board: whole quiche -> slices | Now: hunger 4 to 6 (median 5); saturation 6 to 6.4 (median 6.2); stack 64 x2<br>Used in: quiche 2 |
| **Lasagna** `lasagna` | - | Layered pasta, sauce and cheese baked in a pan. | **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** see subfamilies | **Decision:** see subfamilies | Now: hunger 14 to 16 (median 15); saturation 17.6 to 21 (median 19.3); stack 32 x2; returns bowl 2; block items 1<br>Used in: none |
| **Lasagna pan** `lasagna/whole` | lasagna | The placed pan, served in portions. | as lasagna | **Rule:** Own block ([placeable_food.md](placeable_food.md) 1) | **Decision:** Cooking Pot (Mixer mirrors it)<br>**Planned:** Lasagna needs a baking dish item ([TODO](todo/TODO.md) baking/baked-goods #21) | Now: block items only<br>Used in: none |
| **Lasagna serving** `lasagna/serving` | lasagna | One portion of lasagna, from the pan or made on its own. | as lasagna | as lasagna | **Decision:** Taken from the pan, or Cooking Pot + heatless grid route | Now: hunger 14 to 16 (median 15); saturation 17.6 to 21 (median 19.3); stack 32 x2; returns bowl 2<br>Used in: none |
| **Snack** `snack` | - | Fried or popped finger food: fries, chips, popcorn, nachos, crab sticks. | **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md)) | **Decision:** none required | **Decision:** none shared (fried, popped or boxed)<br>**Planned:** Nachos take cheese ([TODO](todo/TODO.md) dishes/meals #18) | Now: hunger 1 to 12 (median 5.5); saturation 0.2 to 24 (median 6.15); stack 64 x6, 16 x1, 32 x1; returns bowl 1, shulker_bowl 1; block items 2<br>Used in: snack 4; can 3; golden 1; meal 1; sandwich 1; soup 1 |
| **Salad** `salad` | - | Raw or cold parts mixed in a bowl. | **Rule:** x1.25 x 8 fast servings, effects x0.75, 6 salads or 5 + vinegar; potato and maggot salads not canned (can parity 2.10) ([can_parity.md](can_parity.md)) | **Decision:** Display Delight placed | **Decision:** Crafting: raw parts + bowl<br>**Planned:** Salad becomes its own food type; field salad accepts more ingredient options (no fermented vegetables); the cucumber salad takes pickled options ([TODO](todo/TODO.md) dishes/meals #15, dishes/meals #29, dishes/pickles-and-ferments #2) | Now: hunger 2 to 14 (median 7.5); saturation 1.6 to 14 (median 8.8); stack 32 x24; returns bowl 16, shulker_shell 1<br>Used in: can 12; unsorted 2; salad 1 |
| **Soup** `soup` | - | Liquid food in a bowl. | **Rule:** x1.25 x 8 servings, effects x0.75, 6 soups or 3 + salt or broth; 4 skipped (can parity 2.11) ([can_parity.md](can_parity.md)) | **Rule:** Supplementaries jar: every soup and stew is a jar fluid ([TODO](todo/TODO.md) dishes/ingredients #13)<br>**Decision:** Display Delight placed | **Rule:** Cooking Pot (Mixer mirrors it) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)<br>**Rule:** Heatless: shapeless `_manual_only` craft with bone broth, one more main ingredient than the pot ([recipes_cooking.md](recipes_cooking.md) heatless meal rule) | **Planned:** Base values adjusted from the inputs; outliers marked first ([TODO](todo/TODO.md) food/types-and-tuning #8)<br>Now: hunger 1 to 16 (median 8); saturation 0.4 to 27 (median 9.7); stack 32 x65, 64 x1; returns bowl 38, copper_cup 15, shulker_bowl 2, glass_bottle 1<br>Used in: can 32; unsorted 5; golden 3; soup 2; lasagna 1; meal 1 |
| **Soup** `soup/soup` | soup | Thin or blended soups and broths. | as soup | as soup | as soup | as soup<br>Now: hunger 1 to 16 (median 8); saturation 0.4 to 22.1 (median 10.4); stack 32 x35, 64 x1; returns bowl 21, copper_cup 9, glass_bottle 1<br>Used in: can 16; unsorted 4; golden 1; lasagna 1; meal 1; soup 1 |
| **Stew** `soup/stew` | soup | Chunky stews, chilis and braises. | as soup | as soup | as soup | as soup<br>Now: hunger 3 to 15 (median 8.5); saturation 1.8 to 27 (median 9.7); stack 32 x30; returns bowl 17, copper_cup 6, shulker_bowl 2<br>Used in: can 16; golden 2; soup 1; unsorted 1 |
| **Bowl meal** `meal` | - | A cooked dish of several parts in a bowl or on a plate. | **Rule:** Pastas only: 2 -> 4 servings of 0.75 (placeholder); per item, can parity 2.12 ([can_parity.md](can_parity.md)) | **Decision:** Display Delight placed | **Decision:** Cooking Pot (Mixer mirrors it)<br>**Rule:** Heatless: a pot meal is also a shapeless `_manual_only` craft from cooked forms (one more main) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)<br>**Planned:** Meals that take only vanilla or one specific meat, vegetable or fruit open up to any, unless the dish is that food (glazed salmon keeps cooked salmon) ([TODO](todo/TODO.md) dishes/meals #21)<br>**Planned:** Pot recipes that suit the oven gain or move to an Oven recipe (smoking in the oven open) ([TODO](todo/TODO.md) cooking/oven #7)<br>**Planned:** Fries from any potato slice; potato recipes also take sweet potato and its cooked and sliced forms ([TODO](todo/TODO.md) dishes/meals #34, dishes/ingredients #21)<br>**Planned:** Steak and broccoli takes rice, mashed potatoes, any baked potato + bowl, or other baked vegetables + bowl ([TODO](todo/TODO.md) dishes/meals #35) | **Planned:** Salt on every valid food (every meat dish too), never on intermediates (bone broth, cooked rice, tomato sauce); sugar or powdered sugar as a seasoning on some foods ([TODO](todo/TODO.md) food/seasoning #1, food/seasoning #4, food/seasoning #5)<br>Now: hunger 4 to 18 (median 12); saturation 0.8 to 33 (median 19.2); stack 32 x96, 64 x7; returns bowl 73, shulker_bowl 2<br>Used in: can 18; unsorted 4; meal 3; sandwich 2; feast 1; nofamily 1; roll 1; skewer 1; soup 1; stuffed 1 |
| **Rice dish** `meal/rice` | meal | Rice as the base. | as meal | as meal | as meal | as meal<br>Now: hunger 4 to 15 (median 14); saturation 3.2 to 27 (median 21); stack 32 x12, 64 x2; returns bowl 8, shulker_bowl 1<br>Used in: meal 2; can 1; feast 1; nofamily 1; roll 1; sandwich 1; soup 1; stuffed 1; unsorted 1 |
| **Pasta and noodles** `meal/pasta` | meal | Pasta or noodles as the base. | as meal | as meal | as meal | as meal<br>Now: hunger 4 to 15 (median 12); saturation 3.2 to 33 (median 19.2); stack 32 x21; returns bowl 17, shulker_bowl 1<br>Used in: can 14; unsorted 2 |
| **Fried dish** `meal/fried` | meal | Battered or deep-fried main. | as meal | as meal | as meal | as meal<br>Now: hunger 4 to 12 (median 10); saturation 0.8 to 24 (median 14); stack 32 x8, 64 x2; returns bowl 5<br>Used in: skewer 1 |
| **Plated meal** `meal/plate` | meal | A main with sides (steak and potatoes, roasted vegetables). | as meal | as meal | as meal | as meal<br>Now: hunger 4 to 18 (median 10.5); saturation 1.5 to 27 (median 18); stack 32 x55, 64 x3; returns bowl 43<br>Used in: can 3; meal 1; sandwich 1; unsorted 1 |
| **Feast** `feast` | - | A placed block that serves several portions. | **Rule:** 1 placed feast block -> 6 servings (placeholder; can parity 2.14) ([can_parity.md](can_parity.md))<br>**Planned:** Every feast cannable ([TODO](todo/TODO.md) cans/canning #17) | **Decision:** see subfamilies | **Decision:** see subfamilies | Now: hunger 8 to 15 (median 12); saturation 9.6 to 25.2 (median 19.2); stack 32 x15; returns bowl 15; block items 28<br>Used in: can 5; dairy 1; unsorted 1 |
| **Feast block** `feast/whole` | feast | The placed block. | as feast | **Rule:** Own block ([placeable_food.md](placeable_food.md) 1) | **Planned:** Sequenced assembly (roast chicken, shepherd's pie, honey glazed ham...) ([TODO](todo/TODO.md) dishes/meals #5)<br>**Planned:** Cooked in the Oven (hoglin feast first; all feasts in the pot and oven?) ([TODO](todo/TODO.md) dishes/meals #20) | Now: block items only<br>Used in: can 5; dairy 1; unsorted 1 |
| **Feast serving** `feast/serving` | feast | One portion taken from a feast block. | as feast | **Decision:** Display Delight placed | **Decision:** none: taken from the feast block with a bowl | Now: hunger 8 to 15 (median 12); saturation 9.6 to 25.2 (median 19.2); stack 32 x15; returns bowl 15<br>Used in: none |
| **Drink** `drink` | - | Non-alcoholic drinks. | **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md)) | **Decision:** Display Delight placed | **Decision:** Crafting + Cooking Pot (Mixer mirrors it) + Spout from the fluid<br>**Planned:** Kettle with 6 ingredient slots brews the pot drinks via `NCH.frBrewing` (5 Rustic Delight coffees, Apple Cider, Hot Cocoa, Creamy Corn Drink, Ender Nectar, Pickerelweed Juice, Cloud Latte, Mango Milkshake, teas, Chorus Juice); their pot recipes go, Bellini stays in the pot; grid crafts stay ([TODO](todo/TODO.md) food/drinks #7) | **Planned:** Stack: drinks and bottles (tea, juice, custard, shaved ice) 32 ([TODO](todo/TODO.md) food/stack-sizes #1)<br>Now: hunger 0 to 10 (median 0); saturation 0 to 10.4 (median 0); stack 32 x70, 64 x4; returns glass_bottle 58, empty_mason_jar 1<br>Used in: drink 7; meal 5; unsorted 4; ingredient 3; cake 2; candy 2; dessert 2; frozen 2; snack 2; soup 2; 18 more |
| **Juice** `drink/juice` | drink | Pressed fruit or vegetable juice, smoothies, lemonade. | as drink | as drink | **Planned:** Inputs per bottle by hand, Kettle, Create mixing and IE Mixer; machines cheaper than hand ([TODO](todo/TODO.md) food/drinks #8)<br>**Planned:** Every drinkable juice gets a Kettle recipe more efficient than its manual one (Hamimelon, Kiwi, Lemon, Orange, Pear, Prickly Pear, Chorus); "more efficient" read as output per input ([TODO](todo/TODO.md) food/drinks #7, food/drinks #8) | as drink<br>Now: hunger 0 to 4 (median 0.5); saturation 0 to 0.4 (median 0); stack 32 x11, 64 x1; returns glass_bottle 9<br>Used in: meal 1 |
| **Tea** `drink/tea` | drink | Leaves or flowers steeped in hot water. | as drink | **Planned:** Placeable with Display Delight ([TODO](todo/TODO.md) food/drinks #2, placeable #3) | **Planned:** Inputs per bottle by hand, Kettle, Create mixing and IE Mixer; machines cheaper than hand ([TODO](todo/TODO.md) food/drinks #8)<br>**Planned:** Teas in both Kettle and pot: the Kettle recipe is 2x as efficient (output per input), the pot tea takes 2 fruit per tea, sugar / honey / honeycomb in the pot recipe only; Create mixing dearer than manual ([TODO](todo/TODO.md) food/drinks #8, food/drinks #7, food/drinks #2)<br>**Planned:** Farmer's Respite long and strong teas get their own names and textures ([TODO](todo/TODO.md) food/drinks #2) | as drink<br>Now: hunger 0 to 8 (median 0); saturation 0 to 10.4 (median 0); stack 32 x30; returns glass_bottle 29<br>Used in: drink 1 |
| **Coffee** `drink/coffee` | drink | Coffee drinks. | as drink | as drink | as drink | as drink<br>Now: hunger 0 to 6 (median 0); saturation 0 to 1.2 (median 0); stack 32 x6; returns glass_bottle 5<br>Used in: drink 2; meal 2; unsorted 2 |
| **Milk drink** `drink/milk` | drink | Milkshakes and hot chocolate. | as drink | as drink | as drink | as drink<br>Now: hunger 1 to 6 (median 2); saturation 0.4 to 6 (median 6); stack 32 x9; returns glass_bottle 7<br>Used in: drink 1 |
| **Soda and cider** `drink/soda` | drink | Fizzy or fermented-sweet soft drinks, sweet cider. | as drink | as drink | **Rule:** Ciders: a potion-like chain, base -> long (milk) -> strong (honey), each a fluid ([item_merges.md](item_merges.md) 2.x) | as drink<br>Now: hunger 0 to 2 (median 0); saturation 0 to 1.2 (median 0); stack 32 x5, 64 x2; returns glass_bottle 4<br>Used in: none |
| **Other drink** `drink/other` | drink | Honey, water and drinks that fit no other group. | as drink | as drink | as drink | as drink<br>Now: hunger 0 to 10 (median 3.5); saturation 0 to 5.2 (median 2.2); stack 32 x9, 64 x1; returns glass_bottle 4, empty_mason_jar 1<br>Used in: drink 3; ingredient 3; cake 2; candy 2; dessert 2; frozen 2; meal 2; snack 2; soup 2; unsorted 2; 18 more |
| **Alcohol** `alcohol` | - | Drinks that make you tipsy. | **Rule:** Per item, can parity 2.16 (sheet only, no can section) ([can_parity.md](can_parity.md)) | **Decision:** Coaster | **Decision:** see subfamilies | **Rule:** Half a rotten shank (1 hunger), no saturation, always drinkable, Intoxication and Nausea ([alcohol.md](alcohol.md) 11)<br>Now: hunger 0 to 1 (median 1); saturation 0; stack 32 x45; returns tankard 16, glass_bottle 2<br>Used in: alcohol 4 |
| **Wine** `alcohol/wine` | alcohol | Vinery barrel drinks: grape and fruit wines, ciders. | as alcohol | **Rule:** Own block, while sneaking (Vinery bottles) ([placeable_food.md](placeable_food.md) 1) | **Decision:** Fermentation Barrel (grapes: 12 grapes, 4 stomps -> 4 bottles, decided in [TODO](todo/TODO.md) alcohol) | as alcohol<br>Now: hunger 0 to 1 (median 1); saturation 0; stack 32 x26; returns glass_bottle 1<br>Used in: alcohol 4 |
| **Brew** `alcohol/brew` | alcohol | Keg-fermented beer, ale, stout, mead, cider, rice wine. | as alcohol | as alcohol | **Decision:** Keg, poured into a tankard (Spout mirrors the pour) | as alcohol<br>Now: hunger 1; saturation 0; stack 32 x7; returns tankard 7<br>Used in: none |
| **Spirit** `alcohol/spirit` | alcohol | Distilled liquor (vodka, rum). | as alcohol | as alcohol | **Decision:** Keg | as alcohol<br>Now: hunger 1; saturation 0; stack 32 x4; returns tankard 3<br>Used in: none |
| **Mixed drink** `alcohol/mixed` | alcohol | Cocktails and nogs made from other drinks. | as alcohol | as alcohol | **Decision:** Keg, from other drinks | as alcohol<br>Now: hunger 1; saturation 0; stack 32 x8; returns tankard 6, glass_bottle 1<br>Used in: none |
| **Golden food** `golden` | - | A food wrapped in gold: same recipe shape (food + gold) for every base. | **Rule:** 2 golden + honey -> 10 servings at x0.25 (effects x0.333 decided, not built; can parity 2.17) ([can_parity.md](can_parity.md)) | **Decision:** none required | **Rule:** Crafting: the food in 8 gold (real gold only, rose gold counts); every golden food but the apple takes 8 gold nuggets (owner 2026-11-02, [food.md](food.md) 3.16; was 8 gold ingots) ([recipes_cooking.md](recipes_cooking.md) 2.4)<br>**Decision:** Heated Mixer: the food + 6 gold, and Arc Furnace: the food + 5 gold, for every golden food (stated only for carrot and apple, [recipes_cooking.md](recipes_cooking.md) 2.4; TODO dishes/meals #9 built it on every gold tag) | **Rule:** Hunger and saturation are the input food's times 2 (golden) or 4 (gilded), hunger capped at 20; effects kept; the cakes keep their own slices (owner 2026-11-02 ([food.md](food.md) 3.16, [golden_food.md](golden_food.md) 3.6))<br>Now: hunger 4 to 10 (median 5); saturation 4.8 to 128 (median 9.6); stack 16 x50, 8 x2; returns bowl 6, bucket 2, glass_bottle 2; block items 2<br>Used in: can 51; feast 1; golden 1 |
| **Enchanted golden food** `golden/enchanted` | golden | The enchanted tier of a golden food. | as golden | as golden | **Rule:** Gilded (enchanted golden) apple: sequenced assembly on a golden apple, 8 loops of 1 gold ingot, 125 mB liquid XP, pressing (owner 2026-11-03; was 4 loops of a gold block and 250 mB) ([recipes_cooking.md](recipes_cooking.md) 2.4)<br>**Rule:** Every other enchanted golden food but the cake: the same assembly on its golden form; the anvil and the Golden Food Enchantment are removed (owner 2026-11-02 ([golden_food.md](golden_food.md) 3.4))<br>**Rule:** Enchanted golden cake: no recipe (upstream had none) ([golden_food.md](golden_food.md) 3.4) | as golden<br>Now: hunger 6 to 8 (median 8); saturation 9.6 to 128 (median 128); stack 16 x24, 8 x1; returns bowl 3, bucket 1, glass_bottle 1; block items 1<br>Used in: can 25; golden 1 |
| **Can** `can` | - | A food sealed in a can (multi-serving). | **Rule:** n/a: these are the cans | **Decision:** none required | **Rule:** Recipes that use a can are manual only: `_manual_only` id (NCH `cans` argument; Create and auto-crafters skip them) ([can_parity.md](can_parity.md) 8, 2026-10-21)<br>**Planned:** Canning automation: food effects carry over, bone broth returns its bowl ([TODO](todo/TODO.md) cans/canning #8) | Now: hunger 1 to 24 (median 5); saturation 0 to 38.4 (median 6); stack 64 x282, 8 x2<br>Used in: pie 10; cake 1 |
| **Hazard** `hazard` | - | Edible but harmful: rotten, poisonous, inedible things. | **Rule:** Meat Meal x 4, no additive (placeholder); per item, can parity 2.19 ([can_parity.md](can_parity.md)) | **Decision:** none required | **Decision:** none: dropped or found | Now: hunger 0 to 4 (median 2); saturation 0 to 3.2 (median 0.8); stack 64 x10, 16 x1<br>Used in: can 5; skewer 2; alcohol 1; candy 1; drink 1; fish 1; hazard 1; meat 1; nofamily 1; salad 1; 1 more |

### 3.2 Compliance

Members in line with the family's rows (inherited rows included), as passed/checked. A member passes a field when it passes every checked row of that kind. Rule and planned rows are the real gaps; decision rows count members that already match the proposal. Cannable is checked against the member's own Want cell in the can parity worksheet (a cut form follows its whole item's Alt input cell); a member with no row counts as a pass only when it has no can.

| Family | Members | Cannable | Placeable | Recipe shape | Other | Placeable now | Stations now |
|---|---|---|---|---|---|---|---|
| Fruit `fruit` | 45 | rule 45/45 | rule 24/45 | rule 12/12 | - | coaster 5; Supp. jar 22; none 21 | Cutting Board 8 (7 slice, 1 nut); Slice and Dice 8 (7 slice, 1 nut); Campfire / Skillet 5 (5 cooked); Furnace 5 (5 cooked); Smoker 5 (5 cooked); Mechanical Saw 4 (4 slice); Crafting 3 (2 slice, 1 berry); Mana Pool 3 (2 berry, 1 whole); Fan (haunting) 2 (2 berry); Metal Press 2 (2 slice); no recipe 27 |
| Whole fruit `fruit/whole` | 11 | rule 11/11 | rule 1/11 | - | - | coaster 1; Supp. jar 1; none 10 | Mana Pool 1; no recipe 10 |
| Berry `fruit/berry` | 20 | rule 20/20 | rule 19/20 | - | - | coaster 2; Supp. jar 19; none 1 | Fan (haunting) 2; Mana Pool 2; Crafting 1; no recipe 16 |
| Nut `fruit/nut` | 2 | rule 2/2 | rule 0/2 | - | - | none 2 | Cutting Board 1; Slice and Dice 1; no recipe 1 |
| Fruit slice `fruit/slice` | 7 | rule 7/7 | rule 2/7 | rule 7/7 | - | coaster 2; none 5 | Cutting Board 7; Slice and Dice 7; Mechanical Saw 4; Crafting 2; Metal Press 2 |
| Dried or baked fruit `fruit/cooked` | 5 | rule 5/5 | rule 2/5 | rule 5/5 | - | Supp. jar 2; none 3 | Campfire / Skillet 5; Furnace 5; Smoker 5 |
| Vegetable `veggie` | 59 | rule 49/49 | rule 7/59 | rule 27/27 | - | Food Plate 1; coaster 7; none 52 | Cutting Board 25 (16 raw, 7 raw_cut, 2 cooked_cut); Slice and Dice 25 (16 raw, 7 raw_cut, 2 cooked_cut); Campfire / Skillet 18 (15 cooked, 3 cooked_cut); Furnace 18 (15 cooked, 3 cooked_cut); Smoker 18 (15 cooked, 3 cooked_cut); Mixer 8 (4 raw, 2 cooked, 2 raw_cut); Crafting 5 (4 raw, 1 raw_cut); Millstone 4 (4 raw); IE Crusher 3 (3 raw); Crushing Wheels 2 (1 cooked, 1 raw); Mana Pool 2 (2 raw); Cooking Pot 1 (1 cooked); no recipe 14 |
| Raw vegetable `veggie/raw` | 32 | rule 26/26 | rule 6/32 | - | - | coaster 6; none 26 | Cutting Board 16; Slice and Dice 16; Crafting 4; Millstone 4; Mixer 4; IE Crusher 3; Mana Pool 2; Crushing Wheels 1; no recipe 14 |
| Cut vegetable `veggie/raw_cut` | 7 | rule 5/5 | rule 0/7 | rule 7/7 | - | none 7 | Cutting Board 7; Slice and Dice 7; Mixer 2; Crafting 1 |
| Cooked vegetable `veggie/cooked` | 16 | rule 15/15 | rule 1/16 | rule 16/16 | - | Food Plate 1; coaster 1; none 15 | Campfire / Skillet 15; Furnace 15; Smoker 15; Mixer 2; Cooking Pot 1; Crushing Wheels 1 |
| Cooked cut vegetable `veggie/cooked_cut` | 4 | rule 3/3 | rule 0/4 | rule 4/4 | - | none 4 | Campfire / Skillet 3; Furnace 3; Smoker 3; Cutting Board 2; Slice and Dice 2 |
| Meat `meat` | 82 | rule 50/52 | rule 10/82 | rule 56/56 | - | coaster 10; none 72 | Smoker 37 (24 cooked, 13 cooked_cut); Campfire / Skillet 35 (22 cooked, 13 cooked_cut); Furnace 35 (22 cooked, 13 cooked_cut); Cutting Board 27 (13 raw_cut, 7 raw, 4 cooked_cut, 3 cooked); Slice and Dice 27 (13 raw_cut, 7 raw, 4 cooked_cut, 3 cooked); Mixer 10 (6 cooked, 4 cooked_cut); Crafting 4 (2 cooked, 1 raw, 1 raw_cut); Keg 1 (1 cooked); Spout 1 (1 cooked); no recipe 20 |
| Raw meat `meat/raw` | 26 | rule 14/15 | rule 5/26 | - | - | coaster 5; none 21 | Cutting Board 7; Slice and Dice 7; Crafting 1; no recipe 18 |
| Raw cut meat `meat/raw_cut` | 16 | rule 9/10 | rule 0/16 | rule 16/16 | - | none 16 | Cutting Board 13; Slice and Dice 13; Crafting 1; no recipe 2 |
| Cooked meat `meat/cooked` | 27 | rule 17/17 | rule 5/27 | rule 27/27 | - | coaster 5; none 22 | Smoker 24; Campfire / Skillet 22; Furnace 22; Mixer 6; Cutting Board 3; Slice and Dice 3; Crafting 2; Keg 1; Spout 1 |
| Cooked cut meat `meat/cooked_cut` | 13 | rule 10/10 | rule 0/13 | rule 13/13 | - | none 13 | Campfire / Skillet 13; Furnace 13; Smoker 13; Cutting Board 4; Mixer 4; Slice and Dice 4 |
| Fish and seafood `fish` | 52 | rule 35/35 | rule 5/52 | rule 32/32 | - | coaster 5; none 47 | Campfire / Skillet 19 (11 cooked, 8 cooked_cut); Furnace 19 (11 cooked, 8 cooked_cut); Smoker 19 (11 cooked, 8 cooked_cut); Cutting Board 18 (10 raw_cut, 5 cooked_cut, 2 raw, 1 cooked); Slice and Dice 18 (10 raw_cut, 5 cooked_cut, 2 raw, 1 cooked); Mixer 4 (2 cooked, 2 cooked_cut); Mana Pool 3 (3 raw); Keg 1 (1 cooked); vintagedelight:fermenting 1 (1 cooked); no recipe 16 |
| Raw fish `fish/raw` | 20 | rule 12/12 | rule 3/20 | - | - | coaster 3; none 17 | Mana Pool 3; Cutting Board 2; Slice and Dice 2; no recipe 15 |
| Raw cut fish `fish/raw_cut` | 11 | rule 6/6 | rule 0/11 | rule 11/11 | - | none 11 | Cutting Board 10; Slice and Dice 10; no recipe 1 |
| Cooked fish `fish/cooked` | 13 | rule 11/11 | rule 2/13 | rule 13/13 | - | coaster 2; none 11 | Campfire / Skillet 11; Furnace 11; Smoker 11; Mixer 2; Cutting Board 1; Keg 1; Slice and Dice 1; vintagedelight:fermenting 1 |
| Cooked cut fish `fish/cooked_cut` | 8 | rule 6/6 | rule 0/8 | rule 8/8 | - | none 8 | Campfire / Skillet 8; Furnace 8; Smoker 8; Cutting Board 5; Slice and Dice 5; Mixer 2 |
| Egg `egg` | 16 | rule 11/11 | rule 11/16 | rule 10/10 | - | DD placed 10; coaster 3; none 5 | Crafting 9 (8 dish, 1 cooked); Mixer 7 (5 dish, 2 cooked); Campfire / Skillet 6 (4 cooked, 2 dish); Cooking Pot 6 (4 dish, 2 cooked); Furnace 6 (4 cooked, 2 dish); Smoker 6 (4 cooked, 2 dish); Sequenced assembly 1 (1 dish); vintagedelight:fermenting 1 (1 cooked) |
| Cooked egg `egg/cooked` | 6 | rule 4/4 | rule 1/6 | rule 6/6 | - | coaster 1; none 5 | Campfire / Skillet 4; Furnace 4; Smoker 4; Cooking Pot 2; Mixer 2; Crafting 1; vintagedelight:fermenting 1 |
| Egg dish `egg/dish` | 10 | rule 7/7 | rule 10/10 | rule 4/4 | - | DD placed 10; coaster 2 | Crafting 8; Mixer 5; Cooking Pot 4; Campfire / Skillet 2; Furnace 2; Smoker 2; Sequenced assembly 1 |
| Dairy `dairy` | 3 | rule 3/3 | rule 2/3 | - | - | coaster 2; none 1 | Cutting Board 3; Slice and Dice 3 |
| Bread `bread` | 10 | rule 10/10 | - | rule 1/1; decision 4/9 | rule 1/1 | coaster 1; Supp. jar 1; none 8 | Smoker 5 (3 loaf, 1 flat, 1 slice); Campfire / Skillet 4 (2 loaf, 1 flat, 1 slice); Crafting 4 (4 loaf); Furnace 4 (2 loaf, 1 flat, 1 slice); Oven 3 (3 loaf); Cutting Board 2 (1 loaf, 1 slice); Slice and Dice 2 (1 loaf, 1 slice); Mechanical Saw 1 (1 loaf); Mixer 1 (1 loaf) |
| Loaf `bread/loaf` | 7 | rule 7/7 | - | rule 1/1; decision 1/6 | - | coaster 1; none 6 | Crafting 4; Oven 3; Smoker 3; Campfire / Skillet 2; Furnace 2; Cutting Board 1; Mechanical Saw 1; Mixer 1; Slice and Dice 1 |
| Bread slice `bread/slice` | 2 | rule 2/2 | - | decision 2/2 | rule 1/1 | Supp. jar 1; none 1 | Campfire / Skillet 1; Cutting Board 1; Furnace 1; Slice and Dice 1; Smoker 1 |
| Flatbread `bread/flat` | 1 | rule 1/1 | - | decision 1/1 | - | none 1 | Campfire / Skillet 1; Furnace 1; Smoker 1 |
| Kitchen ingredient `ingredient` | 43 | rule 27/27 | - | rule 1/1 | - | own block 4; coaster 5; Supp. jar 1; none 38 | Crafting 22 (10 dough, 10 other, 2 sauce); Mixer 16 (6 dough, 6 sauce, 4 other); Cooking Pot 11 (6 sauce, 4 other, 1 dough); Bottling Machine 4 (3 dough, 1 sauce); Cutting Board 4 (4 other); Slice and Dice 4 (4 other); Smoker 3 (3 other); Campfire / Skillet 2 (2 other); Fan (washing) 2 (2 dough); Furnace 2 (2 other); Apple Press 1 (1 other); Metal Press 1 (1 other); Press 1 (1 dough); Spout 1 (1 sauce); vintagedelight:fermenting 1 (1 other); no recipe 3 |
| Dough and batter `ingredient/dough` | 14 | rule 8/8 | - | rule 1/1 | - | none 14 | Crafting 10; Mixer 6; Bottling Machine 3; Fan (washing) 2; Cooking Pot 1; Press 1 |
| Sauce and fat `ingredient/sauce` | 7 | rule 4/4 | - | - | - | own block 1; coaster 2; Supp. jar 1; none 5 | Cooking Pot 6; Mixer 6; Crafting 2; Bottling Machine 1; Spout 1; no recipe 1 |
| Other ingredient `ingredient/other` | 22 | rule 15/15 | - | - | - | own block 3; coaster 3; none 19 | Crafting 10; Cooking Pot 4; Cutting Board 4; Mixer 4; Slice and Dice 4; Smoker 3; Campfire / Skillet 2; Furnace 2; Apple Press 1; Metal Press 1; vintagedelight:fermenting 1; no recipe 2 |
| Preserve `preserve` | 37 | rule 33/34 | rule 22/22 | decision 24/37 | - | own block 1; own block, sneak 22; coaster 1; Supp. jar 2; none 12 | Crafting 26 (23 jam, 3 pickle); Cooking Pot 24 (23 jam, 1 spread); Mixer 24 (23 jam, 1 spread); Spout 23 (23 jam); vintagedelight:fermenting 9 (9 pickle) |
| Jam `preserve/jam` | 24 | rule 24/24 | rule 22/22 | decision 23/24 | - | own block 1; own block, sneak 22; coaster 1; Supp. jar 1 | Cooking Pot 23; Crafting 23; Mixer 23; Spout 23 |
| Spread `preserve/spread` | 1 | rule 1/1 | - | decision 1/1 | - | none 1 | Cooking Pot 1; Mixer 1 |
| Pickle `preserve/pickle` | 12 | rule 8/9 | - | decision 0/12 | - | Supp. jar 1; none 11 | vintagedelight:fermenting 9; Crafting 3 |
| Jello `jello` | 24 | rule 23/23 | decision 0/24 | decision 23/24 | - | none 24 | Crafting 23; Spout 22; Cooking Pot 1; Mixer 1 |
| Pie `pie` | 36 | rule 14/20 | rule 10/10; decision 8/19 | rule 15/15; planned 0/12; decision 10/19 | - | own block 10; Food Plate 2; Small Food Plate 8; coaster 9; none 17 | Oven 17 (10 whole, 7 small); Crafting 10 (10 whole); Cutting Board 10 (10 slice); Slice and Dice 10 (10 slice); Cooking Pot 2 (2 small); Metal Press 2 (2 slice); Mixer 2 (2 small); no recipe 9 |
| Whole pie `pie/whole` | 10 | - | rule 10/10 | rule 10/10; planned 0/10 | - | own block 10; Food Plate 1; coaster 1 | Crafting 10; Oven 10 |
| Pie slice `pie/slice` | 19 | rule 10/16 | decision 8/19 | decision 10/19 | - | Small Food Plate 8; coaster 7; none 11 | Cutting Board 10; Slice and Dice 10; Metal Press 2; no recipe 9 |
| Tart and hand pie `pie/small` | 7 | rule 4/4 | - | rule 5/5; planned 0/2 | - | Food Plate 1; coaster 1; none 6 | Oven 7; Cooking Pot 2; Mixer 2 |
| Cake `cake` | 38 | rule 20/21 | rule 15/15; planned 9/20 | rule 14/18; decision 15/20 | - | own block 14; own block, sneak 1; Small Food Plate 9; coaster 9; none 14 | Oven 18 (15 whole, 3 muffin); Crafting 16 (15 whole, 1 muffin); Cutting Board 15 (15 slice); Slice and Dice 15 (15 slice); Cooking Pot 3 (3 muffin); Mixer 3 (3 muffin); Metal Press 2 (2 slice); Spout 2 (2 whole); no recipe 5 |
| Whole cake `cake/whole` | 15 | rule 1/1 | rule 15/15 | rule 11/15 | - | own block 14; own block, sneak 1 | Crafting 15; Oven 15; Spout 2 |
| Cake slice `cake/slice` | 20 | rule 16/17 | planned 9/20 | decision 15/20 | - | Small Food Plate 9; coaster 9; none 11 | Cutting Board 15; Slice and Dice 15; Metal Press 2; no recipe 5 |
| Muffin and cupcake `cake/muffin` | 3 | rule 3/3 | - | rule 3/3 | - | none 3 | Cooking Pot 3; Mixer 3; Oven 3; Crafting 1 |
| Cookie `cookie` | 27 | rule 14/14 | decision 26/27 | rule 26/27 | - | Food Plate 8; coaster 8; Supp. jar 26; none 1 | Oven 26; Crafting 1 |
| Dessert `dessert` | 41 | rule 28/28 | rule 0/1; decision 3/30 | decision 18/33 | - | own block 2; DD placed 1; Food Plate 1; coaster 2; Supp. jar 1; none 36 | Crafting 20 (7 pancake, 4 pudding, 3 brownie, 3 other, 3 pastry); Spout 14 (7 roll, 6 waffle, 1 other); Cutting Board 9 (7 pancake, 1 brownie, 1 pudding); Slice and Dice 9 (7 pancake, 1 brownie, 1 pudding); Mixer 6 (3 pudding, 2 pastry, 1 pancake); Cooking Pot 3 (2 pudding, 1 pancake); Smoker 2 (1 other, 1 waffle); Furnace 1 (1 other); Mana Pool 1 (1 other) |
| Brownie `dessert/brownie` | 4 | rule 1/1 | decision 1/1 | decision 1/4 | - | own block 1; none 3 | Crafting 3; Cutting Board 1; Slice and Dice 1 |
| Pudding `dessert/pudding` | 7 | rule 5/5 | decision 1/7 | decision 3/7 | - | own block 1; DD placed 1; coaster 1; none 5 | Crafting 4; Mixer 3; Cooking Pot 2; Cutting Board 1; Slice and Dice 1 |
| Pancake `dessert/pancake` | 8 | rule 1/1 | rule 0/1 | - | - | none 8 | Crafting 7; Cutting Board 7; Slice and Dice 7; Cooking Pot 1; Mixer 1 |
| Pastry `dessert/pastry` | 3 | rule 3/3 | decision 1/3 | decision 0/3 | - | Food Plate 1; coaster 1; none 2 | Crafting 3; Mixer 2 |
| Waffle `dessert/waffle` | 7 | rule 7/7 | decision 0/7 | decision 6/7 | - | none 7 | Spout 6; Smoker 1 |
| Sweet roll `dessert/roll` | 7 | rule 7/7 | decision 0/7 | decision 7/7 | - | none 7 | Spout 7 |
| Other dessert `dessert/other` | 5 | rule 4/4 | decision 0/5 | decision 1/5 | - | Supp. jar 1; none 4 | Crafting 3; Furnace 1; Mana Pool 1; Smoker 1; Spout 1 |
| Candy `candy` | 69 | rule 66/66 | decision 51/69 | decision 40/69 | - | Food Plate 2; coaster 2; Supp. jar 51; none 16 | Spout 37 (18 piece, 13 coated, 4 marshmallow, 2 bar); Crafting 22 (16 piece, 3 marshmallow, 2 bar, 1 coated); Mixer 7 (5 piece, 1 coated, 1 marshmallow); Press + Basin 6 (5 bar, 1 piece); Cooking Pot 3 (2 piece, 1 coated); Campfire / Skillet 2 (2 marshmallow); Keg 1 (1 piece); Smoker 1 (1 marshmallow); no recipe 1 |
| Chocolate bar `candy/bar` | 8 | rule 8/8 | decision 8/8 | decision 5/8 | - | Supp. jar 8 | Press + Basin 5; Crafting 2; Spout 2; no recipe 1 |
| Candy piece `candy/piece` | 37 | rule 34/34 | decision 25/37 | decision 18/37 | - | Supp. jar 25; none 12 | Spout 18; Crafting 16; Mixer 5; Cooking Pot 2; Keg 1; Press + Basin 1 |
| Coated fruit `candy/coated` | 14 | rule 14/14 | decision 13/14 | decision 13/14 | - | Supp. jar 13; none 1 | Spout 13; Cooking Pot 1; Crafting 1; Mixer 1 |
| Marshmallow `candy/marshmallow` | 10 | rule 10/10 | decision 5/10 | decision 4/10 | - | Food Plate 2; coaster 2; Supp. jar 5; none 3 | Spout 4; Crafting 3; Campfire / Skillet 2; Mixer 1; Smoker 1 |
| Frozen dessert `frozen` | 20 | rule 13/13 | decision 6/20 | rule 15/20 | - | DD placed 3; Food Plate 3; coaster 4; none 14 | Crafting 20 (11 ice_cream, 8 popsicle, 1 shaved_ice); Spout 6 (6 ice_cream); Sequenced assembly 1 (1 ice_cream) |
| Ice cream `frozen/ice_cream` | 11 | rule 9/9 | decision 2/11 | rule 9/11 | - | DD placed 2; coaster 2; none 9 | Crafting 11; Spout 6; Sequenced assembly 1 |
| Popsicle `frozen/popsicle` | 8 | rule 3/3 | decision 4/8 | rule 5/8 | - | DD placed 1; Food Plate 3; coaster 2; none 4 | Crafting 8 |
| Shaved ice `frozen/shaved_ice` | 1 | rule 1/1 | decision 0/1 | rule 1/1 | - | none 1 | Crafting 1 |
| Sandwich `sandwich` | 48 | rule 36/36 | decision 19/48 | rule 7/14; planned 23/40; decision 9/34 | rule 6/6 | Food Plate 19; coaster 18; none 29 | Crafting 47 (14 burger, 11 roll, 8 toast, 7 square, 7 wrap); Sequenced assembly 18 (10 burger, 3 roll, 3 wrap, 2 square); Mixer 5 (2 roll, 1 burger, 1 square, 1 wrap); Spout 1 (1 toast); no recipe 1 |
| Square sandwich `sandwich/square` | 8 | rule 8/8 | decision 4/8 | planned 7/8; decision 0/8 | - | Food Plate 4; coaster 4; none 4 | Crafting 7; Sequenced assembly 2; Mixer 1; no recipe 1 |
| Long roll sandwich `sandwich/roll` | 11 | rule 6/6 | decision 4/11 | planned 11/11; decision 1/11 | - | Food Plate 4; coaster 4; none 7 | Crafting 11; Sequenced assembly 3; Mixer 2 |
| Burger `sandwich/burger` | 14 | rule 11/11 | decision 6/14 | rule 7/14; planned 0/13 | - | Food Plate 6; coaster 6; none 8 | Crafting 14; Sequenced assembly 10; Mixer 1 |
| Wrap `sandwich/wrap` | 7 | rule 4/4 | decision 4/7 | planned 4/7; decision 0/7 | - | Food Plate 4; coaster 3; none 3 | Crafting 7; Sequenced assembly 3; Mixer 1 |
| Open toast `sandwich/toast` | 8 | rule 7/7 | decision 1/8 | planned 1/1; decision 8/8 | rule 6/6 | Food Plate 1; coaster 1; none 7 | Crafting 8; Spout 1 |
| Skewer `skewer` | 8 | rule 6/6 | decision 4/8 | decision 1/8 | - | Food Plate 4; coaster 4; none 4 | Crafting 7; Mixer 3; Cooking Pot 2; Sequenced assembly 2 |
| Roll and dumpling `roll` | 25 | rule 10/10 | decision 9/25 | decision 15/25 | - | DD placed 2; Food Plate 9; Small Food Plate 6; coaster 11; none 14 | Crafting 15; Mixer 11; Cooking Pot 7; Cutting Board 3; Sequenced assembly 3; Slice and Dice 3; no recipe 2 |
| Stuffed vegetable `stuffed` | 11 | rule 8/8 | decision 2/11 | decision 2/11 | - | Food Plate 2; Small Food Plate 1; coaster 2; none 9 | Crafting 7; Mixer 7; Cooking Pot 6; Sequenced assembly 2; Campfire / Skillet 1; Furnace 1; Smoker 1 |
| Pizza `pizza` | 8 | rule 4/4 | rule 4/4; decision 0/4 | decision 4/8 | - | own block 4; none 4 | Crafting 4 (4 whole); Cutting Board 4 (4 slice); Slice and Dice 4 (4 slice) |
| Whole pizza `pizza/whole` | 4 | - | rule 4/4 | decision 0/4 | - | own block 4 | Crafting 4 |
| Pizza slice `pizza/slice` | 4 | rule 4/4 | decision 0/4 | decision 4/4 | - | none 4 | Cutting Board 4; Slice and Dice 4 |
| Quiche `quiche` | 4 | rule 2/2 | rule 2/2; decision 1/2 | decision 3/4 | - | own block 2; Small Food Plate 1; coaster 1; none 1 | Crafting 2 (2 whole); Cutting Board 2 (2 slice); Slice and Dice 2 (2 slice); Sequenced assembly 1 (1 whole) |
| Whole quiche `quiche/whole` | 2 | - | rule 2/2 | decision 1/2 | - | own block 2 | Crafting 2; Sequenced assembly 1 |
| Quiche slice `quiche/slice` | 2 | rule 2/2 | decision 1/2 | decision 2/2 | - | Small Food Plate 1; coaster 1; none 1 | Cutting Board 2; Slice and Dice 2 |
| Lasagna `lasagna` | 3 | rule 2/2 | rule 1/1 | decision 2/3 | - | own block 1; DD placed 1; coaster 1; none 1 | Cooking Pot 2 (1 serving, 1 whole); Mixer 2 (1 serving, 1 whole); Crafting 1 (1 serving); Sequenced assembly 1 (1 serving); no recipe 1 |
| Lasagna pan `lasagna/whole` | 1 | - | rule 1/1 | decision 1/1 | - | own block 1 | Cooking Pot 1; Mixer 1 |
| Lasagna serving `lasagna/serving` | 2 | rule 2/2 | - | decision 1/2 | - | DD placed 1; coaster 1; none 1 | Cooking Pot 1; Crafting 1; Mixer 1; Sequenced assembly 1; no recipe 1 |
| Snack `snack` | 10 | rule 4/4 | - | - | - | own block 2; DD placed 2; coaster 1; none 6 | Crafting 5; Cooking Pot 4; Mixer 4; Campfire / Skillet 1; Cutting Board 1; Furnace 1; Slice and Dice 1; Smoker 1; no recipe 1 |
| Salad `salad` | 24 | rule 16/17 | decision 8/24 | decision 23/24 | - | DD placed 8; coaster 8; none 16 | Crafting 23; Mixer 4; Cooking Pot 1; Sequenced assembly 1 |
| Soup `soup` | 66 | rule 36/41 | rule 51/66; decision 24/66 | rule 22/66 | - | DD placed 24; coaster 25; Supp. jar 51; none 15 | Cooking Pot 48 (26 soup, 22 stew); Mixer 48 (26 soup, 22 stew); Crafting 35 (19 soup, 16 stew); Spout 2 (2 soup); fungidelight:effect_stew 1 (1 stew); no recipe 15 |
| Soup `soup/soup` | 36 | rule 18/21 | rule 27/36; decision 12/36 | rule 14/36 | - | DD placed 12; coaster 12; Supp. jar 27; none 9 | Cooking Pot 26; Mixer 26; Crafting 19; Spout 2; no recipe 9 |
| Stew `soup/stew` | 30 | rule 18/20 | rule 24/30; decision 12/30 | rule 8/30 | - | DD placed 12; coaster 13; Supp. jar 24; none 6 | Cooking Pot 22; Mixer 22; Crafting 16; fungidelight:effect_stew 1; no recipe 6 |
| Bowl meal `meal` | 103 | rule 65/69 | decision 38/103 | rule 35/74; decision 74/103 | - | DD placed 38; Food Plate 4; coaster 42; none 61 | Mixer 77 (35 plate, 21 pasta, 12 rice, 9 fried); Cooking Pot 74 (32 plate, 21 pasta, 12 rice, 9 fried); Crafting 55 (30 plate, 17 pasta, 7 rice, 1 fried); Sequenced assembly 3 (3 plate); vintagedelight:fermenting 1 (1 plate); no recipe 9 |
| Rice dish `meal/rice` | 14 | rule 10/10 | decision 4/14 | rule 6/12; decision 12/14 | - | DD placed 4; Food Plate 1; coaster 5; none 9 | Cooking Pot 12; Mixer 12; Crafting 7; no recipe 1 |
| Pasta and noodles `meal/pasta` | 21 | rule 15/16 | decision 12/21 | rule 17/21; decision 21/21 | - | DD placed 12; coaster 12; none 9 | Cooking Pot 21; Mixer 21; Crafting 17 |
| Fried dish `meal/fried` | 10 | rule 6/6 | decision 2/10 | rule 1/9; decision 9/10 | - | DD placed 2; Food Plate 1; coaster 3; none 7 | Cooking Pot 9; Mixer 9; Crafting 1; no recipe 1 |
| Plated meal `meal/plate` | 58 | rule 34/37 | decision 20/58 | rule 11/32; decision 32/58 | - | DD placed 20; Food Plate 2; coaster 22; none 36 | Mixer 35; Cooking Pot 32; Crafting 30; Sequenced assembly 3; vintagedelight:fermenting 1; no recipe 7 |
| Feast `feast` | 43 | rule 6/9 | rule 28/28; decision 14/15 | planned 0/28 | - | own block 28; DD placed 14; coaster 14; none 1 | Crafting 15 (15 whole); Mixer 13 (9 whole, 4 serving); Cooking Pot 9 (9 whole); Sequenced assembly 2 (2 whole); Oven 1 (1 whole); no recipe 17 |
| Feast block `feast/whole` | 28 | rule 0/1 | rule 28/28 | planned 0/28 | - | own block 28 | Crafting 15; Cooking Pot 9; Mixer 9; Sequenced assembly 2; Oven 1; no recipe 6 |
| Feast serving `feast/serving` | 15 | rule 6/8 | decision 14/15 | - | - | DD placed 14; coaster 14; none 1 | Mixer 4; no recipe 11 |
| Drink `drink` | 74 | rule 61/61 | planned 11/30; decision 5/44 | planned 0/42; decision 7/25 | - | own block 1; DD placed 16; coaster 31; Supp. jar 17; none 33 | Spout 47 (25 tea, 8 juice, 7 milk, 4 soda, 3 other); Kettle 36 (28 tea, 5 coffee, 3 soda); Cooking Pot 29 (9 tea, 7 other, 6 coffee, 5 juice, 1 milk, 1 soda); Mixer 25 (9 tea, 6 coffee, 6 other, 2 juice, 1 milk, 1 soda); Crafting 24 (8 juice, 7 milk, 3 coffee, 3 soda, 2 other, 1 tea); Bottling Machine 7 (3 juice, 3 soda, 1 other); minecraft:blasting 2 (1 other, 1 soda); Apple Press 1 (1 other); Keg (pouring) 1 (1 other); no recipe 1 |
| Juice `drink/juice` | 12 | rule 10/10 | decision 2/12 | planned 0/12 | - | DD placed 2; coaster 2; Supp. jar 2; none 8 | Crafting 8; Spout 8; Cooking Pot 5; Bottling Machine 3; Mixer 2 |
| Tea `drink/tea` | 30 | rule 26/26 | planned 11/30 | planned 0/30 | - | DD placed 11; coaster 22; Supp. jar 6; none 8 | Kettle 28; Spout 25; Cooking Pot 9; Mixer 9; Crafting 1; no recipe 1 |
| Coffee `drink/coffee` | 6 | rule 6/6 | decision 0/6 | decision 0/6 | - | none 6 | Cooking Pot 6; Mixer 6; Kettle 5; Crafting 3 |
| Milk drink `drink/milk` | 9 | rule 9/9 | decision 0/9 | decision 6/9 | - | Supp. jar 6; none 3 | Crafting 7; Spout 7; Cooking Pot 1; Mixer 1 |
| Soda and cider `drink/soda` | 7 | rule 5/5 | decision 2/7 | - | - | DD placed 2; coaster 4; none 3 | Spout 4; Bottling Machine 3; Crafting 3; Kettle 3; Cooking Pot 1; Mixer 1; minecraft:blasting 1 |
| Other drink `drink/other` | 10 | rule 5/5 | decision 1/10 | decision 1/10 | - | own block 1; DD placed 1; coaster 3; Supp. jar 3; none 5 | Cooking Pot 7; Mixer 6; Spout 3; Crafting 2; Apple Press 1; Bottling Machine 1; Keg (pouring) 1; minecraft:blasting 1 |
| Alcohol `alcohol` | 45 | rule 44/44 | rule 25/25; decision 17/19 | decision 41/45 | rule 44/45 | own block, sneak 26; DD placed 1; coaster 43; none 2 | Fermentation Barrel 26 (25 wine, 1 mixed); Spout 18 (7 brew, 7 mixed, 3 spirit, 1 wine); Keg 16 (7 brew, 6 mixed, 3 spirit); Keg (pouring) 16 (7 brew, 6 mixed, 3 spirit); Cooking Pot 2 (1 mixed, 1 wine); Mixer 2 (1 mixed, 1 wine); Bottling Machine 1 (1 spirit) |
| Wine `alcohol/wine` | 26 | rule 25/25 | rule 25/25 | decision 25/26 | rule 25/26 | own block, sneak 25; DD placed 1; coaster 26 | Fermentation Barrel 25; Cooking Pot 1; Mixer 1; Spout 1 |
| Brew `alcohol/brew` | 7 | rule 7/7 | decision 7/7 | decision 7/7 | rule 7/7 | coaster 7 | Keg 7; Keg (pouring) 7; Spout 7 |
| Spirit `alcohol/spirit` | 4 | rule 4/4 | decision 3/4 | decision 3/4 | rule 4/4 | coaster 3; none 1 | Keg 3; Keg (pouring) 3; Spout 3; Bottling Machine 1 |
| Mixed drink `alcohol/mixed` | 8 | rule 8/8 | decision 7/8 | decision 6/8 | rule 8/8 | own block, sneak 1; coaster 7; none 1 | Spout 7; Keg 6; Keg (pouring) 6; Cooking Pot 1; Fermentation Barrel 1; Mixer 1 |
| Golden food `golden` | 54 | rule 50/52 | - | rule 30/54; decision 2/28 | - | own block 2; coaster 5; Supp. jar 14; none 35 | Crafting 28; Arc Furnace 2; Mixer 2; Disenchanter 1; Sequenced assembly 1 (1 enchanted); no recipe 25 |
| Enchanted golden food `golden/enchanted` | 26 | rule 25/25 | - | rule 2/26 | - | own block 1; coaster 2; Supp. jar 7; none 17 | Sequenced assembly 1; no recipe 25 |
| Can `can` | 284 | - | - | rule 9/9 | - | none 284 | Crafting 269; Cooking Pot 13; Mixer 13; no recipe 13 |
| Hazard `hazard` | 11 | rule 10/10 | - | - | - | coaster 2; none 9 | Crafting 2; Mixer 2; Crushing Wheels 1; Fan (haunting) 1; Mana Pool 1; no recipe 6 |

### 3.3 Foods without a family

Foods no family rule should cover: magic and one-off foods (food type `special`) and single odd fits. They keep their own recipes; their can decision is still the can parity row.

| Food | Id | Food type | Closest family | Stations | Placeable | Can (want / has) |
|---|---|---|---|---|---|---|
| Ancient Fruit | `quark:ancient_fruit` | special | - | - | - | yes / yes |
| Azalea Flowers | `immersive_weathering:azalea_flowers` | special | - | - | - | no / no |
| Blazing Apple | `hostiledelight:blazingapple` | special | - | Crafting | - | no row / no |
| Chorus Fruit | `minecraft:chorus_fruit` | special | - | Dragon breath | coaster | yes / yes |
| Copper Carrot | `miners_delight:copper_carrot` | special | - | Crafting | - | no row / no |
| Cosmic Cod | `alexsmobs:cosmic_cod` | special | - | - | - | no / no |
| Crab Bars | `quarkdelight:crab_bars` | handheld | snack | Cooking Pot, Crafting, Mixer, Sequenced assembly | - | no / no |
| Dog Food | `farmersdelight:dog_food` | special | - | Cooking Pot, Crafting, Mixer | coaster | no / no |
| Ender Substance | `hostiledelight:endersubstance` | special | - | Crafting | - | no row / no |
| Fish Oil | `alexsmobs:fish_oil` | special | - | Crafting | Supp. jar | no / no |
| Glowing Apple | `hostiledelight:glowapple` | special | - | Crafting | - | no row / no |
| Ice Cubes | `neapolitan:ice_cubes` | special | - | Crafting, Crushing Wheels, Cutting Board, IE Crusher, Slice and Dice | - | no / no |
| Ice Sickle | `immersive_weathering:ice_sickle` | special | - | Crafting | - | no / no |
| Icicle | `immersive_weathering:icicle` | special | - | - | - | no / no |
| Magma Gelatin | `nethersdelight:magma_gelatin` | special | - | Cooking Pot, Mixer | - | no / no |
| Mana in a Bottle | `botania:mana_bottle` | special | - | Mana Pool | - | no / no |
| Moss Clump | `immersive_weathering:moss_clump` | special | - | - | - | no / no |
| Propelpearl | `nethersdelight:propelpearl` | special | - | - | - | no / no |
| Rainbow Jelly | `alexsmobs:rainbow_jelly` | special | - | - | - | no / no |
| Sweet Berry Meatballs | `berry_good:sweet_berry_meatballs` | handheld | skewer | Campfire / Skillet, Furnace, Smoker | - | no / no |
| Twisted Cereal | `endersdelight:twisted_cereal` | meal | meal/plate | Crafting | DD placed, coaster | no / no |

Skipped on purpose (not food for these rules):

| Item | Id | Why |
|---|---|---|
| Apple Sauce Bottle | `vintagedelight:apple_sauce_bottle` | removed or merged |
| Baked Tentacle on a Stick | `oceansdelight:baked_tentacle_on_a_stick` | removed or merged |
| Baked Tentacles | `miners_delight:baked_tentacles` | removed or merged |
| Banana | `alexsmobs:banana` | removed or merged |
| Caesar Salad | `oceanic_delight:caesar_salad` | removed or merged |
| Canned Food | `canned_goods:canned_food` | removed or merged |
| Canned Hamimelon | `creativeengineering:canned_hamimelon` | removed or merged |
| Chips | `pneumaticcraft:chips` | removed or merged |
| Chocolate Cake | `neapolitan:chocolate_cake` | removed or merged |
| Chocolate Copper Coin | `lightmanscurrency:coin_chocolate_copper` | removed or merged |
| Chocolate Diamond Coin | `lightmanscurrency:coin_chocolate_diamond` | removed or merged |
| Chocolate Emerald Coin | `lightmanscurrency:coin_chocolate_emerald` | removed or merged |
| Chocolate Gold Coin | `lightmanscurrency:coin_chocolate_gold` | removed or merged |
| Chocolate Iron Coin | `lightmanscurrency:coin_chocolate_iron` | removed or merged |
| Chocolate Netherite Coin | `lightmanscurrency:coin_chocolate_netherite` | removed or merged |
| Chorus Stew | `endersdelight:chorus_stew_wood` | removed or merged |
| Cod 'n' Chips | `pneumaticcraft:cod_n_chips` | removed or merged |
| Coffee | `farmersrespite:coffee` | removed or merged |
| Coffee | `farmersrespite:long_coffee` | removed or merged |
| Coffee | `farmersrespite:strong_coffee` | removed or merged |
| Coffee Berries | `farmersrespite:coffee_berries` | removed or merged |
| Cooked Chevon | `delightful:cooked_goat` | removed or merged |
| Cooked Venison Chops | `delightful:cooked_venison_chops` | removed or merged |
| Cooked Venison Shanks | `abnormals_delight:cooked_venison_shanks` | removed or merged |
| Copper Lunchbox | `solapplepie:lunchbox` | removed or merged |
| Copper Nugget | `everythingcopper:copper_nugget` | removed or merged |
| Cut Tentacles | `oceansdelight:cut_tentacles` | removed or merged |
| Diced Potatoes | `moredelight:diced_potatoes` | removed or merged |
| Dune Platter | `abnormals_delight:dune_platter` | removed or merged |
| Ender Paella | `endersdelight:ender_paella_wood` | removed or merged |
| Endermite Stew | `endersdelight:endermite_stew_wood` | removed or merged |
| Fried Egg | `incubation:fried_egg` | removed or merged |
| Glow Berries Jam Bottle | `vintagedelight:glow_berry_jam_bottle` | removed or merged |
| Glow Berries Jam Jar | `vintagedelight:glow_berry_jam_jar` | removed or merged |
| Glow Jam Jar | `delightful:glow_jelly_bottle` | removed or merged |
| Grilled Squid Tentacles | `oceanic_delight:grilled_squid_tentacles` | removed or merged |
| Hamimelon Popsicle | `fruitsdelight:hamimelon_popsicle` | removed or merged |
| Hamimelon Slice | `fruitsdelight:hamimelon_slice` | removed or merged |
| High-Grade Black Powder Cake | `oldguns:high_grade_black_powder_cake` | not food (black powder) |
| Honey Apple | `buzzier_bees:honey_apple` | removed or merged |
| Honey Glazed Walnut | `delightful:honey_glazed_walnut` | removed or merged |
| Hot Hot Hotpot | `canned_goods:hot_hot_hotpot` | removed or merged |
| Incomplete Apple Pie | `create_central_kitchen:incomplete_apple_pie` | sequenced assembly intermediate |
| Incomplete Bacon Sandwich | `create_central_kitchen:incomplete_bacon_sandwich` | sequenced assembly intermediate |
| Incomplete Cherry Pie | `create_central_kitchen:incomplete_cherry_pie` | sequenced assembly intermediate |
| Incomplete Chicken Sandwich | `create_central_kitchen:incomplete_chicken_sandwich` | sequenced assembly intermediate |
| Incomplete Chorus Flower Pie | `create_central_kitchen:incomplete_chorus_flower_pie` | sequenced assembly intermediate |
| Incomplete Chorus Fruit Pie | `create_central_kitchen:incomplete_chorus_fruit_pie` | sequenced assembly intermediate |
| Incomplete Egg Sandwich | `create_central_kitchen:incomplete_egg_sandwich` | sequenced assembly intermediate |
| Incomplete Hamburger | `create_central_kitchen:incomplete_hamburger` | sequenced assembly intermediate |
| Incomplete Mulberry Pie | `create_central_kitchen:incomplete_mulberry_pie` | sequenced assembly intermediate |
| Incomplete Mutton Wrap | `create_central_kitchen:incomplete_mutton_wrap` | sequenced assembly intermediate |
| Incomplete Pumpkin Pie | `create_central_kitchen:incomplete_pumpkin_pie` | sequenced assembly intermediate |
| Incomplete Sweet Berry Cheesecake | `create_central_kitchen:incomplete_sweet_berry_cheesecake` | sequenced assembly intermediate |
| Incomplete Truffle Pie | `create_central_kitchen:incomplete_truffle_pie` | sequenced assembly intermediate |
| Iron Lunchbox | `solapplepie:golden_lunchbox` | removed or merged |
| Jam Jar | `delightful:jelly_bottle` | removed or merged |
| Lunchbag | `solapplepie:lunchbag` | removed or merged |
| Mhadjeb | `veggiesdelight:mhadjeb` | removed or merged |
| Passion Fruit Glazed Duck | `abnormals_delight:passion_fruit_glazed_duck` | removed or merged |
| Pearl Pasta | `endersdelight:pearl_pasta_wood` | removed or merged |
| Pepper Jam Bottle | `vintagedelight:pepper_jam_bottle` | removed or merged |
| Pickled Pickles | `brewinandchewin:pickled_pickles` | removed or merged |
| Prickly Pear Juice | `delightful:prickly_pear_juice` | removed or merged |
| Raw Chevon | `delightful:raw_goat` | removed or merged |
| Raw Venison Chops | `delightful:venison_chops` | removed or merged |
| Raw Venison Shanks | `abnormals_delight:venison_shanks` | removed or merged |
| Relish Bottle | `vintagedelight:relish_bottle` | removed or merged |
| Salted Cod | `vintagedelight:salted_cod` | removed or merged |
| Salted Salmon | `vintagedelight:salted_salmon` | removed or merged |
| Seared Venison | `abnormals_delight:seared_venison` | removed or merged |
| Shulker Filet | `endersdelight:shulker_filet` | removed or merged |
| Shulker Mollusk | `endersdelight:shulker_mollusk` | removed or merged |
| Slice of Pumpkin Pie | `create_central_kitchen:pumpkin_pie_slice` | removed or merged |
| Slice of Yucca Gateau | `abnormals_delight:yucca_gateau_slice` | removed or merged |
| Squid Tentacles | `oceanic_delight:glow_squid_tentacles` | removed or merged |
| Squid Tentacles | `oceanic_delight:squid_tentacles` | removed or merged |
| Strawberry Banana Smoothie | `neapolitan:strawberry_banana_smoothie` | removed or merged |
| Stuffed Bell Pepper | `veggiesdelight:stuffed_bellpepper` | removed or merged |
| Sweet Berries Jam Bottle | `vintagedelight:sweet_berry_jam_bottle` | removed or merged |
| Sweet Berries Jam Jar | `vintagedelight:sweet_berry_jam_jar` | removed or merged |
| Tentacle on a Stick | `oceansdelight:tentacle_on_a_stick` | removed or merged |
| Tentacles | `miners_delight:tentacles` | removed or merged |
| Tentacles | `oceansdelight:tentacles` | removed or merged |
| Tentacles On A Stick | `miners_delight:tentacles_on_a_stick` | removed or merged |
| Twisted Cereal | `endersdelight:twisted_cereal_wood` | removed or merged |
| Uncooked Mhadjeb | `veggiesdelight:uncooked_mhadjeb` | removed or merged |
| Venison with Bamboo Shoots | `abnormals_delight:venison_with_bamboo_shoots` | removed or merged |
| Vinegar Bottle | `vintagedelight:vinegar_bottle` | removed or merged |
| Wet High-Grade Black Powder Cake | `oldguns:wet_high_grade_black_powder_cake` | not food (black powder) |
| Wheat Dough | `farmersdelight:wheat_dough` | removed or merged |

**Unsorted** (no rule took them; fix `classify()`):

| Name | Id | Food type |
|---|---|---|
| Canned Cheese Pasta | `creativeengineering:canned_cheese_pasta` | - |
| Canned Cheese Slices | `creativeengineering:canned_cheese_slice` | - |
| Canned Cooked Inky Cap | `creativeengineering:canned_cooked_inky_cap` | - |
| Canned Creamy Morel Soup | `creativeengineering:canned_creamy_morel_soup` | - |
| Canned Cucumber | `creativeengineering:canned_cucumber` | - |
| Canned Cucumber Salad | `creativeengineering:canned_cucumber_salad` | - |
| Canned Ghost Pepper | `creativeengineering:canned_ghost_pepper` | - |
| Canned Ghostly Chili | `creativeengineering:canned_ghostly_chili` | - |
| Canned Inky Caps Salad | `creativeengineering:canned_inky_caps_salad` | - |
| Canned Oatmeal | `creativeengineering:canned_oatmeal` | - |
| Canned Pad Thai | `creativeengineering:canned_pad_thai` | - |
| Canned Pickle Soup | `creativeengineering:canned_pickle_soup` | - |
| Canned Roasted Peanuts | `creativeengineering:canned_roasted_peanut` | - |
| Canned Surströmming | `creativeengineering:canned_surstromming` | - |
| Canned Truffle | `creativeengineering:canned_truffle` | - |
| Canned Truffle Pasta | `creativeengineering:canned_truffle_pasta` | - |
| Cheese Toast | `creativeengineering:cheese_toast` | - |
| Glowing Calamari | `creativeengineering:glowing_calamari` | - |
| Glowing Calamari Roll | `creativeengineering:glowing_calamari_roll` | - |
| Glowing Calamari Slice | `creativeengineering:glowing_calamari_slice` | - |
| Slice of Baklava | `delightful:baklava_slice` | - |
| Cactus Chili | `delightful:cactus_chili` | - |
| Cactus Soup | `delightful:cactus_soup` | - |
| Cactus Soup Cup | `delightful:cactus_soup_cup` | - |
| Cantaloupe Bread | `delightful:cantaloupe_bread` | - |
| Cantaloupe Gummy | `delightful:cantaloupe_gummy` | - |
| Matcha Gummy | `delightful:matcha_gummy` | - |
| Matcha Milkshake | `delightful:matcha_milkshake` | - |
| Nut Dough | `delightful:nut_dough` | - |
| Roasted Acorn | `delightful:roasted_acorn` | - |
| Salmonberry Milkshake | `delightful:salmonberry_milkshake` | - |
| Bowl of Stuffed Cantaloupe | `delightful:stuffed_cantaloupe` | - |
| item.endersdelight.amberveil_stew | `endersdelight:amberveil_stew` | - |
| item.endersdelight.amberveiled_curry | `endersdelight:amberveiled_curry` | - |
| item.endersdelight.chicken_curry | `endersdelight:chicken_curry` | - |
| item.endersdelight.veil_of_flames_risotto | `endersdelight:veil_of_flames_risotto` | - |
| Bowl of Gleaming Salad | `farmersdelight:gleaming_salad` | - |
| Black Bell Pepper | `rusticdelight:bell_pepper_black` | - |
| Blue Bell Pepper | `rusticdelight:bell_pepper_blue` | - |
| Orange Bell Pepper | `rusticdelight:bell_pepper_orange` | - |
| Pink Bell Pepper | `rusticdelight:bell_pepper_pink` | - |
| Purple Bell Pepper | `rusticdelight:bell_pepper_purple` | - |
| Black Bell Pepper Roll | `rusticdelight:bell_pepper_roll_black` | - |
| Blue Bell Pepper Roll | `rusticdelight:bell_pepper_roll_blue` | - |
| Green Bell Pepper Roll | `rusticdelight:bell_pepper_roll_green` | - |
| Orange Bell Pepper Roll | `rusticdelight:bell_pepper_roll_orange` | - |
| Pink Bell Pepper Roll | `rusticdelight:bell_pepper_roll_pink` | - |
| Purple Bell Pepper Roll | `rusticdelight:bell_pepper_roll_purple` | - |
| Red Bell Pepper Roll | `rusticdelight:bell_pepper_roll_red` | - |
| White Bell Pepper Roll | `rusticdelight:bell_pepper_roll_white` | - |
| Yellow Bell Pepper Roll | `rusticdelight:bell_pepper_roll_yellow` | - |
| Black Bell Pepper Slice | `rusticdelight:bell_pepper_slice_black` | - |
| Blue Bell Pepper Slice | `rusticdelight:bell_pepper_slice_blue` | - |
| Green Bell Pepper Slice | `rusticdelight:bell_pepper_slice_green` | - |
| Orange Bell Pepper Slice | `rusticdelight:bell_pepper_slice_orange` | - |
| Pink Bell Pepper Slice | `rusticdelight:bell_pepper_slice_pink` | - |
| Purple Bell Pepper Slice | `rusticdelight:bell_pepper_slice_purple` | - |
| Red Bell Pepper Slice | `rusticdelight:bell_pepper_slice_red` | - |
| White Bell Pepper Slice | `rusticdelight:bell_pepper_slice_white` | - |
| Yellow Bell Pepper Slice | `rusticdelight:bell_pepper_slice_yellow` | - |
| White Bell Pepper | `rusticdelight:bell_pepper_white` | - |
| Calamari Soup | `rusticdelight:calamari_soup` | - |
| Cherry Blossom Coffee | `rusticdelight:cherry_blossom_coffee` | - |
| Cherry Blossom Roll | `rusticdelight:cherry_blossom_roll` | - |
| Fried Dough | `rusticdelight:fried_dough` | - |
| Fried Dumplings | `rusticdelight:fried_dumplings` | - |
| Fried Fish | `rusticdelight:fried_fish` | - |
| Pumpkin Coffee | `rusticdelight:pumpkin_coffee` | - |
| Roasted Black Bell Pepper | `rusticdelight:roasted_bell_pepper_black` | - |
| Roasted Blue Bell Pepper | `rusticdelight:roasted_bell_pepper_blue` | - |
| Roasted Orange Bell Pepper | `rusticdelight:roasted_bell_pepper_orange` | - |
| Roasted Pink Bell Pepper | `rusticdelight:roasted_bell_pepper_pink` | - |
| Roasted Purple Bell Pepper | `rusticdelight:roasted_bell_pepper_purple` | - |
| Roasted Black Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_black` | - |
| Roasted Blue Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_blue` | - |
| Roasted Green Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_green` | - |
| Roasted Orange Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_orange` | - |
| Roasted Pink Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_pink` | - |
| Roasted Purple Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_purple` | - |
| Roasted Red Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_red` | - |
| Roasted White Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_white` | - |
| Roasted Yellow Bell Pepper Slice | `rusticdelight:roasted_bell_pepper_slice_yellow` | - |
| Roasted White Bell Pepper | `rusticdelight:roasted_bell_pepper_white` | - |
| Stuffed Black Bell Pepper | `rusticdelight:stuffed_bell_pepper_black` | - |
| Stuffed Blue Bell Pepper | `rusticdelight:stuffed_bell_pepper_blue` | - |
| Stuffed Orange Bell Pepper | `rusticdelight:stuffed_bell_pepper_orange` | - |
| Stuffed Pink Bell Pepper | `rusticdelight:stuffed_bell_pepper_pink` | - |
| Stuffed Purple Bell Pepper | `rusticdelight:stuffed_bell_pepper_purple` | - |
| Stuffed White Bell Pepper | `rusticdelight:stuffed_bell_pepper_white` | - |
| Sweet Salad | `rusticdelight:sweet_salad` | - |
| Syrup | `rusticdelight:syrup` | - |
| Syrup Coffee | `rusticdelight:syrup_coffee` | - |
| Netherite Lunchbox | `solapplepie:netherite_lunchbox` | - |
| Ghost Charcoal | `vintagedelight:ghost_charcoal` | - |

### 3.4 Members by family

Every member and how it complies. Columns R1, R2... are the family's checked rows listed above each table (ok, **no**, or - when the row does not apply). Can = Want cell / has a can.

#### Fruit (`fruit`)

No direct members; see the subfamilies.

#### Whole fruit (`fruit/whole`)

- R1 (Cannable): **Rule:** Per item, can parity 2.2 (raw: Fruit Meal 5/3 x 3 servings, + sugar, honey or vinegar; pumpkin, melon, cantaloupe, hamimelon and pineapple canned from slices) and 2.6 (dried / baked) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: gathered (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Apple | `minecraft:apple` | ok | ok | - | yes / yes | coaster, Supp. jar | Mana Pool |
| Banana | `neapolitan:banana` | ok | **no** | - | yes / yes | - | - |
| Fig | `fruitsdelight:fig` | ok | **no** | - | yes / yes | - | - |
| Kiwi | `fruitsdelight:kiwi` | ok | **no** | - | yes / yes | - | - |
| Lemon | `fruitsdelight:lemon` | ok | **no** | - | yes / yes | - | - |
| Mango | `fruitsdelight:mango` | ok | **no** | - | yes / yes | - | - |
| Mangosteen | `fruitsdelight:mangosteen` | ok | **no** | - | yes / yes | - | - |
| Orange | `fruitsdelight:orange` | ok | **no** | - | yes / yes | - | - |
| Peach | `fruitsdelight:peach` | ok | **no** | - | yes / yes | - | - |
| Pear | `fruitsdelight:pear` | ok | **no** | - | yes / yes | - | - |
| Persimmon | `fruitsdelight:persimmon` | ok | **no** | - | yes / yes | - | - |

#### Berry (`fruit/berry`)

- R1 (Cannable): **Rule:** Per item, can parity 2.2 (raw: Fruit Meal 5/3 x 3 servings, + sugar, honey or vinegar; pumpkin, melon, cantaloupe, hamimelon and pineapple canned from slices) and 2.6 (dried / baked) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: gathered (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bayberry | `fruitsdelight:bayberry` | ok | ok | - | yes / yes | Supp. jar | - |
| Blueberry | `fruitsdelight:blueberry` | ok | ok | - | yes / yes | Supp. jar | - |
| Cherry | `vinery:cherry` | ok | ok | - | yes / yes | Supp. jar | - |
| Cranberry | `fruitsdelight:cranberry` | ok | ok | - | yes / yes | Supp. jar | - |
| Glow Berries | `minecraft:glow_berries` | ok | ok | - | yes / yes | coaster, Supp. jar | Fan (haunting), Mana Pool |
| Hawberry | `fruitsdelight:hawberry` | ok | ok | - | yes / yes | Supp. jar | - |
| Lychee | `fruitsdelight:lychee` | ok | **no** | - | yes / yes | - | - |
| Mulberry | `upgrade_aquatic:mulberry` | ok | ok | - | yes / yes | Supp. jar | Crafting |
| Red Grape | `vinery:red_grape` | ok | ok | - | no / no | Supp. jar | - |
| Red Jungle Grapes | `vinery:jungle_grapes_red` | ok | ok | - | no / no | Supp. jar | - |
| Red Savanna Grapes | `vinery:savanna_grapes_red` | ok | ok | - | no / no | Supp. jar | - |
| Red Taiga Grapes | `vinery:taiga_grapes_red` | ok | ok | - | no / no | Supp. jar | - |
| Salmonberries | `delightful:salmonberries` | ok | ok | - | yes / yes | Supp. jar | - |
| Strawberries | `neapolitan:strawberries` | ok | ok | - | yes / yes | Supp. jar | - |
| Sweet Berries | `minecraft:sweet_berries` | ok | ok | - | yes / yes | coaster, Supp. jar | Mana Pool |
| White Grape | `vinery:white_grape` | ok | ok | - | no / no | Supp. jar | - |
| White Jungle Grapes | `vinery:jungle_grapes_white` | ok | ok | - | no / no | Supp. jar | - |
| White Savanna Grapes | `vinery:savanna_grapes_white` | ok | ok | - | no / no | Supp. jar | - |
| White Strawberries | `neapolitan:white_strawberries` | ok | ok | - | yes / yes | Supp. jar | Fan (haunting) |
| White Taiga Grapes | `vinery:taiga_grapes_white` | ok | ok | - | no / no | Supp. jar | - |

#### Nut (`fruit/nut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.2, but the additive is salt (or bone broth), not sugar, honey or vinegar ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: gathered (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Acorn | `delightful:acorn` | ok | **no** | - | no / no | - | - |
| Peanut | `vintagedelight:peanut` | ok | **no** | - | no / no | - | Cutting Board, Slice and Dice |

#### Fruit slice (`fruit/slice`)

- R1 (Cannable): **Rule:** Per item, can parity 2.2 (raw: Fruit Meal 5/3 x 3 servings, + sugar, honey or vinegar; pumpkin, melon, cantaloupe, hamimelon and pineapple canned from slices) and 2.6 (dried / baked) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Cutting Board: whole fruit -> slices (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cantaloupe Slice | `delightful:cantaloupe_slice` | ok | **no** | ok | yes / yes | - | Crafting, Cutting Board, Mechanical Saw, Slice and Dice |
| Durian Flesh | `fruitsdelight:durian_flesh` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Lemon Slice | `fruitsdelight:lemon_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Mechanical Saw, Slice and Dice |
| Melon Slice | `minecraft:melon_slice` | ok | ok | ok | yes / yes | coaster | Crafting, Cutting Board, Metal Press, Slice and Dice |
| Orange Slice | `fruitsdelight:orange_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Mechanical Saw, Slice and Dice |
| Pineapple Slice | `fruitsdelight:pineapple_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Mechanical Saw, Slice and Dice |
| Pumpkin Slice | `farmersdelight:pumpkin_slice` | ok | ok | ok | yes / yes | coaster | Cutting Board, Metal Press, Slice and Dice |

#### Dried or baked fruit (`fruit/cooked`)

- R1 (Cannable): **Rule:** Per item, can parity 2.2 (raw: Fruit Meal 5/3 x 3 servings, + sugar, honey or vinegar; pumpkin, melon, cantaloupe, hamimelon and pineapple canned from slices) and 2.6 (dried / baked) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: whole fruit (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Baked Durian | `fruitsdelight:baked_durian` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Baked Pear | `fruitsdelight:baked_pear` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Dried Banana | `neapolitan:dried_banana` | ok | ok | ok | yes / yes | Supp. jar | Campfire / Skillet, Furnace, Smoker |
| Dried Persimmon | `fruitsdelight:dried_persimmon` | ok | ok | ok | yes / yes | Supp. jar | Campfire / Skillet, Furnace, Smoker |
| Roasted Peanut | `vintagedelight:roasted_peanut` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |

#### Vegetable (`veggie`)

No direct members; see the subfamilies.

#### Raw vegetable (`veggie/raw`)

- R1 (Cannable): **Rule:** Per item, can parity 2.1 (raw: Baked Potato meal 5/6 x 2, + salt, broth or vinegar) and 2.6 (cooked: x2 x 4 servings, 6 or 5 + salt) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: harvested (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Beetroot | `minecraft:beetroot` | ok | ok | - | yes / yes | coaster | - |
| Broccoli | `veggiesdelight:broccoli` | ok | **no** | - | yes / yes | - | - |
| Cabbage | `farmersdelight:cabbage` | ok | ok | - | yes / yes | coaster | Crafting, Mixer |
| Cactus Chunk | `delightful:cactus_flesh` | ok | **no** | - | yes / yes | - | Cutting Board, Slice and Dice |
| Carrot | `minecraft:carrot` | ok | ok | - | yes / yes | coaster | Cutting Board, Mana Pool, Slice and Dice |
| Cauliflower | `veggiesdelight:cauliflower` | ok | **no** | - | yes / yes | - | - |
| Cave Carrot | `miners_delight:cave_carrot` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Chorus Fruit Grain | `ends_delight:chorus_fruit_grain` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Chorus Succulent | `ends_delight:chorus_succulent` | - | **no** | - | no row / no | - | - |
| Cleaned Inky Cap | `fungidelight:cleaned_cap` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Corn Kernel | `corn_delight:corn_seeds` | ok | **no** | - | no / no | - | Crafting, Crushing Wheels, Cutting Board, IE Crusher, Millstone, Slice and Dice |
| Cucumber | `vintagedelight:cucumber` | ok | **no** | - | yes / yes | - | - |
| Dandelion Leaf | `veggiesdelight:dandelion_leaf` | ok | **no** | - | no / no | - | Cutting Board, Millstone, Slice and Dice |
| Garlic Clove | `veggiesdelight:garlic_clove` | ok | **no** | - | no / no | - | Crafting, Cutting Board, IE Crusher, Millstone, Mixer, Slice and Dice |
| Ghost Pepper | `vintagedelight:ghost_pepper` | ok | **no** | - | yes / yes | - | - |
| Gongylidia | `alexsmobs:gongylidia` | ok | **no** | - | yes / yes | - | - |
| Green Bell Pepper | `rusticdelight:bell_pepper_green` | ok | **no** | - | yes / yes | - | - |
| Maize | `corn_delight:corn` | ok | **no** | - | yes / yes | - | - |
| Mint Leaves | `neapolitan:mint_leaves` | ok | **no** | - | no / no | - | - |
| Moss | `miners_delight:moss` | - | **no** | - | no row / no | - | - |
| Onion | `farmersdelight:onion` | ok | ok | - | yes / yes | coaster | Cutting Board, Mixer, Slice and Dice |
| Potato | `minecraft:potato` | ok | ok | - | yes / yes | coaster | Cutting Board, Mana Pool, Slice and Dice |
| Raw Oats | `vintagedelight:raw_oats` | ok | **no** | - | no / no | - | Cutting Board, IE Crusher, Millstone, Slice and Dice |
| Raw Vegetarian Patty | `veggiesdelight:raw_vegetarian_patty` | ok | **no** | - | no / no | - | Crafting |
| Red Bell Pepper | `rusticdelight:bell_pepper_red` | ok | **no** | - | yes / yes | - | Cutting Board, Slice and Dice |
| Sea Grape | `oceanic_delight:sea_grape` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Sweet Potato | `veggiesdelight:sweet_potato` | ok | **no** | - | yes / yes | - | Cutting Board, Slice and Dice |
| Tomato | `farmersdelight:tomato` | ok | ok | - | yes / yes | coaster | Cutting Board, Mixer, Slice and Dice |
| Truffle | `fungidelight:truffle` | ok | **no** | - | yes / yes | - | - |
| Turnip | `veggiesdelight:turnip` | ok | **no** | - | yes / yes | - | - |
| Yellow Bell Pepper | `rusticdelight:bell_pepper_yellow` | ok | **no** | - | yes / yes | - | - |
| Zucchini | `veggiesdelight:zucchini` | ok | **no** | - | yes / yes | - | Cutting Board, Slice and Dice |

#### Cut vegetable (`veggie/raw_cut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.1 (raw: Baked Potato meal 5/6 x 2, + salt, broth or vinegar) and 2.6 (cooked: x2 x 4 servings, 6 or 5 + salt) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Cutting Board: whole vegetable -> pieces (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cabbage Leaf | `farmersdelight:cabbage_leaf` | ok | **no** | ok | yes / yes | - | Cutting Board, Mixer, Slice and Dice |
| Cauliflower Floret | `veggiesdelight:cauliflower_floret` | ok | **no** | ok | yes / yes | - | Crafting, Cutting Board, Mixer, Slice and Dice |
| Potato Slices | `rusticdelight:potato_slices` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Prepared Sea Pickle | `hostiledelight:preparedseapickle` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Sea Pickle Slices | `oceanic_delight:sea_pickle_slices` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Truffle Slice | `fungidelight:truffle_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Zucchini Slice | `veggiesdelight:zucchini_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |

#### Cooked vegetable (`veggie/cooked`)

- R1 (Cannable): **Rule:** Per item, can parity 2.1 (raw: Baked Potato meal 5/6 x 2, + salt, broth or vinegar) and 2.6 (cooked: x2 x 4 servings, 6 or 5 + salt) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: the raw vegetable; boiled ones in the Cooking Pot (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Baked Cave Carrot | `miners_delight:baked_cave_carrot` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Baked Potato | `minecraft:baked_potato` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Mixer, Smoker |
| Baked Sweet Potato | `veggiesdelight:baked_sweet_potato` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Boiled Blue Pickerelweed | `upgrade_aquatic:boiled_blue_pickerelweed` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Boiled Corn | `corn_delight:boiled_corn` | ok | **no** | ok | yes / yes | - | Cooking Pot, Mixer |
| Boiled Purple Pickerelweed | `upgrade_aquatic:boiled_purple_pickerelweed` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cactus Steak | `delightful:cactus_steak` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Inky Cap | `fungidelight:cooked_cleaned_cap` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Vegetarian Patty | `veggiesdelight:cooked_vegetarian_patty` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Dried Kelp | `minecraft:dried_kelp` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Crushing Wheels, Furnace, Smoker |
| Grilled Corn | `corn_delight:grilled_corn` | ok | ok | ok | yes / yes | Food Plate, coaster | Campfire / Skillet, Furnace, Smoker |
| Roasted Adzuki Beans | `neapolitan:roasted_adzuki_beans` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Roasted Green Bell Pepper | `rusticdelight:roasted_bell_pepper_green` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Roasted Red Bell Pepper | `rusticdelight:roasted_bell_pepper_red` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Roasted Yellow Bell Pepper | `rusticdelight:roasted_bell_pepper_yellow` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Roasted Zucchini | `veggiesdelight:roasted_zucchini` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |

#### Cooked cut vegetable (`veggie/cooked_cut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.1 (raw: Baked Potato meal 5/6 x 2, + salt, broth or vinegar) and 2.6 (cooked: x2 x 4 servings, 6 or 5 + salt) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: the cut piece, or Cutting Board: the cooked whole (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Baked Potato Slices | `rusticdelight:baked_potato_slices` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Roasted Cauliflower Floret | `veggiesdelight:roasted_cauliflower_floret` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Roasted Garlic Clove | `veggiesdelight:roasted_garlic_clove` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Vegan Patty | `miners_delight:vegan_patty` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |

#### Meat (`meat`)

No direct members; see the subfamilies.

#### Raw meat (`meat/raw`)

- R1 (Cannable): **Rule:** Per item, can parity 2.3 (raw: Meat Meal 5/3 x 4) and 2.7 (cooked: x2 x 3, 5 or 4 + broth; patties, bacon and exotic meats not canned) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: dropped (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Arthropod | `miners_delight:arthropod` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Bat Wing | `miners_delight:bat_wing` | - | **no** | - | no row / no | - | - |
| Dragon Leg | `ends_delight:dragon_leg` | - | **no** | - | no row / no | - | - |
| Enderman Gristle | `ends_delight:enderman_gristle` | - | **no** | - | no row / no | - | - |
| Ghast Tentacle | `hostiledelight:ghasttentacle` | - | **no** | - | no row / no | - | - |
| Ham | `farmersdelight:ham` | ok | **no** | - | no / no | - | - |
| Hoglin Loin | `nethersdelight:hoglin_loin` | ok | **no** | - | no / no | - | - |
| Kangaroo Meat | `alexsmobs:kangaroo_meat` | ok | **no** | - | no / no | - | - |
| Mite Crust | `endersdelight:mite_crust` | ok | **no** | - | no / no | - | - |
| Raw Beef | `minecraft:beef` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |
| Raw Bison | `alexsdelight:raw_bison` | ok | **no** | - | no / no | - | - |
| Raw Bunfungus | `alexsdelight:raw_bunfungus` | ok | **no** | - | no / no | - | - |
| Raw Chicken | `minecraft:chicken` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |
| Raw Dragon Meat | `ends_delight:raw_dragon_meat` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Raw Duck | `untitledduckmod:raw_duck` | ok | **no** | - | no / no | - | - |
| Raw Ender Sausage | `ends_delight:raw_ender_sausage` | - | **no** | - | no row / no | - | Crafting |
| Raw Endermite | `hostiledelight:rawendermite` | - | **no** | - | no row / no | - | - |
| Raw Endermite Meat | `ends_delight:raw_ender_mite_meat` | - | **no** | - | no row / no | - | - |
| Raw Goose | `untitledduckmod:raw_goose` | ok | **no** | - | no / no | - | - |
| Raw Moose Ribs | `alexsmobs:moose_ribs` | ok | **no** | - | no / no | - | - |
| Raw Mutton | `minecraft:mutton` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |
| Raw Porkchop | `minecraft:porkchop` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |
| Raw Rabbit | `minecraft:rabbit` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |
| Shulker Meat | `ends_delight:shulker_meat` | **no** | **no** | - | no row / yes | - | - |
| Silverfish Eggs | `miners_delight:silverfish_eggs` | - | **no** | - | no row / no | - | - |
| Spider Leg | `miners_delight:spider_leg` | - | **no** | - | no row / no | - | - |

#### Raw cut meat (`meat/raw_cut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.3 (raw: Meat Meal 5/3 x 4) and 2.7 (cooked: x2 x 3, 5 or 4 + broth; patties, bacon and exotic meats not canned) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Cutting Board: raw cut -> pieces; a seasoned cut is crafted from its raw meat (mint chops); a mob drop is dropped (hoglin ear, strider slice) (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bison Mince | `alexsdelight:bison_mince` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Ground Strider | `nethersdelight:ground_strider` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Hoglin Ear | `nethersdelight:hoglin_ear` | ok | **no** | ok | no / no | - | - |
| Kangaroo Shank | `alexsdelight:kangaroo_shank` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Loose Moose Rib | `alexsdelight:loose_moose_rib` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Minced Beef | `farmersdelight:minced_beef` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Bacon | `farmersdelight:bacon` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Bunfungus Drumstick | `alexsdelight:raw_bunfungus_drumstick` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Raw Chicken Cuts | `farmersdelight:chicken_cuts` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Dragon Meat Cuts | `ends_delight:raw_dragon_meat_cuts` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Raw Duck Fillet | `abnormals_delight:duck_fillet` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Raw Mint Chops | `neapolitan:mint_chops` | ok | **no** | ok | no / no | - | Crafting |
| Raw Mutton Chops | `farmersdelight:mutton_chops` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Rotten Ham | `hostiledelight:rottenham` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Shulker Meat Slice | `ends_delight:shulker_meat_slice` | **no** | **no** | ok | no row / yes | - | Cutting Board, Slice and Dice |
| Strider Slice | `nethersdelight:strider_slice` | ok | **no** | ok | no / no | - | - |

#### Cooked meat (`meat/cooked`)

- R1 (Cannable): **Rule:** Per item, can parity 2.3 (raw: Meat Meal 5/3 x 4) and 2.7 (cooked: x2 x 3, 5 or 4 + broth; patties, bacon and exotic meats not canned) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: the raw cut; cured, smoked, dried or glazed cuts keep their own station (Keg, Smoker, Spout, a craft) (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Baked Spider Leg | `miners_delight:baked_spider_leg` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Arthropod | `miners_delight:cooked_arthropod` | - | **no** | ok | no row / no | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Cooked Bison | `alexsdelight:cooked_bison` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Bunfungus | `alexsdelight:cooked_bunfungus` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Centipede Leg | `alexsdelight:cooked_centipede_leg` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Chicken | `minecraft:cooked_chicken` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Furnace, Mixer, Smoker |
| Cooked Duck | `untitledduckmod:cooked_duck` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Endermite | `hostiledelight:cookendermite` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Goose | `untitledduckmod:cooked_goose` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Kangaroo Meat | `alexsmobs:cooked_kangaroo_meat` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Moose Ribs | `alexsmobs:cooked_moose_ribs` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Mutton | `minecraft:cooked_mutton` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Furnace, Mixer, Smoker |
| Cooked Porkchop | `minecraft:cooked_porkchop` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Cutting Board, Furnace, Mixer, Slice and Dice, Smoker |
| Cooked Rabbit | `minecraft:cooked_rabbit` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Furnace, Mixer, Smoker |
| Dried Endermite Meat | `ends_delight:dried_endermite_meat` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Ender Sausage | `ends_delight:ender_sausage` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Grilled Ghast | `hostiledelight:grilledghast` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Hoglin Sirloin | `nethersdelight:hoglin_sirloin` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Honey Glazed Porkchop | `buzzier_bees:glazed_porkchop` | ok | **no** | ok | yes / yes | - | Crafting, Spout |
| Jerky | `brewinandchewin:jerky` | ok | **no** | ok | yes / yes | - | Keg |
| Maple Glazed Bacon | `abnormals_delight:maple_glazed_bacon` | ok | **no** | ok | yes / yes | - | Crafting |
| Roasted Dragon Meat | `ends_delight:roasted_dragon_meat` | - | **no** | ok | no row / no | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Roasted Shulker Meat | `ends_delight:roasted_shulker_meat` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Smoked Bat Wing | `miners_delight:smoked_bat_wing` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Smoked Dragon Leg | `ends_delight:smoked_dragon_leg` | - | **no** | ok | no row / no | - | Smoker |
| Smoked Ham | `farmersdelight:smoked_ham` | ok | **no** | ok | no / no | - | Mixer, Smoker |
| Steak | `minecraft:cooked_beef` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Furnace, Mixer, Smoker |

#### Cooked cut meat (`meat/cooked_cut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.3 (raw: Meat Meal 5/3 x 4) and 2.7 (cooked: x2 x 3, 5 or 4 + broth; patties, bacon and exotic meats not canned) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: the raw piece (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Beef Patty | `farmersdelight:beef_patty` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Mixer, Smoker |
| Bison Patty | `alexsdelight:bison_patty` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Bacon | `farmersdelight:cooked_bacon` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Mixer, Smoker |
| Cooked Bunfungus Drumstick | `alexsdelight:cooked_bunfungus_drumstick` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Chicken Cuts | `farmersdelight:cooked_chicken_cuts` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Mixer, Slice and Dice, Smoker |
| Cooked Duck Fillet | `abnormals_delight:cooked_duck_fillet` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Kangaroo Shank | `alexsdelight:cooked_kangaroo_shank` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Loose Moose Rib | `alexsdelight:cooked_loose_moose_rib` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Mint Chops | `neapolitan:cooked_mint_chops` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Mutton Chops | `farmersdelight:cooked_mutton_chops` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Mixer, Slice and Dice, Smoker |
| Cooked Rotten Ham | `hostiledelight:cookedrottenham` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Roasted Dragon Meat Cuts | `ends_delight:roasted_dragon_meat_cuts` | - | **no** | ok | no row / no | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Roasted Shulker Meat Slice | `ends_delight:roasted_shulker_meat_slice` | - | **no** | ok | no row / no | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |

#### Fish and seafood (`fish`)

No direct members; see the subfamilies.

#### Raw fish (`fish/raw`)

- R1 (Cannable): **Rule:** Per item, can parity 2.4 (raw: Fish Meal 4/2 x 4; slices share the whole fish's count) and 2.8 (cooked: x2 x 3, from the cut) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: caught (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Ancient Fish Eggs | `oceanic_delight:ancient_fish_eggs` | - | **no** | - | no row / no | - | - |
| Blobfish | `alexsmobs:blobfish` | ok | **no** | - | yes / yes | - | - |
| Caviar | `oceanic_delight:salmon_eggs` | - | **no** | - | no row / no | - | - |
| Elder Guardian Eye | `oceanic_delight:elder_guardian_eye` | - | **no** | - | no row / no | - | - |
| Flying Fish | `alexsmobs:flying_fish` | ok | **no** | - | yes / yes | - | - |
| Glow Squid | `miners_delight:glow_squid` | - | **no** | - | no row / no | - | - |
| Guardian Tail | `oceansdelight:guardian_tail` | - | **no** | - | no row / no | - | Cutting Board, Slice and Dice |
| Lobster Tail | `alexsmobs:lobster_tail` | ok | **no** | - | yes / yes | - | - |
| Raw Calamari | `rusticdelight:calamari` | ok | **no** | - | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Catfish | `alexsmobs:raw_catfish` | ok | **no** | - | yes / yes | - | - |
| Raw Cod | `minecraft:cod` | ok | ok | - | yes / yes | coaster | Mana Pool |
| Raw Crab Leg | `quark:crab_leg` | ok | **no** | - | yes / yes | - | - |
| Raw Lionfish | `upgrade_aquatic:lionfish` | ok | **no** | - | yes / yes | - | - |
| Raw Perch | `upgrade_aquatic:perch` | ok | **no** | - | yes / yes | - | - |
| Raw Pike | `upgrade_aquatic:pike` | ok | **no** | - | yes / yes | - | - |
| Raw Salmon | `minecraft:salmon` | ok | ok | - | yes / yes | coaster | Mana Pool |
| Shrimp | `oceanic_delight:shrimp` | - | **no** | - | no row / no | - | - |
| Slab of Elder Guardian | `oceansdelight:elder_guardian_slab` | - | **no** | - | no row / no | - | - |
| Squid | `miners_delight:squid` | - | **no** | - | no row / no | - | - |
| Tropical Fish | `minecraft:tropical_fish` | ok | ok | - | yes / yes | coaster | Mana Pool |

#### Raw cut fish (`fish/raw_cut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.4 (raw: Fish Meal 4/2 x 4; slices share the whole fish's count) and 2.8 (cooked: x2 x 3, from the cut) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Cutting Board: whole fish -> slices (salmon, cod, catfish 6); a mob drop is dropped (guardian fillet, squid tentacles) ([recipes_cooking.md](recipes_cooking.md) 2.1; owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Fugu Slice | `oceansdelight:fugu_slice` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Guardian Fillet | `hostiledelight:guardianfillet` | - | **no** | ok | no row / no | - | - |
| Guardian Ratty | `hostiledelight:guardianpatty` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Raw Calamari Slice | `rusticdelight:calamari_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Catfish Slice | `alexsdelight:raw_catfish_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Cod Slice | `farmersdelight:cod_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Perch Slice | `abnormals_delight:perch_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Pike Slice | `abnormals_delight:pike_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Raw Salmon Slice | `farmersdelight:salmon_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Shrimp Slices | `oceanic_delight:shrimp_slices` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Slice of Elder Guardian | `oceansdelight:elder_guardian_slice` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |

#### Cooked fish (`fish/cooked`)

- R1 (Cannable): **Rule:** Per item, can parity 2.4 (raw: Fish Meal 4/2 x 4; slices share the whole fish's count) and 2.8 (cooked: x2 x 3, from the cut) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: the raw fish; cured or fermented fish keeps its own station (Keg, Fermenting Jar) (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Baked Squid | `miners_delight:baked_squid` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Calamari | `rusticdelight:cooked_calamari` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Cooked Catfish | `alexsmobs:cooked_catfish` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Cod | `minecraft:cooked_cod` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Furnace, Mixer, Smoker |
| Cooked Crab Leg | `quark:cooked_crab_leg` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Guardian Tail | `oceansdelight:cooked_guardian_tail` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Lionfish | `upgrade_aquatic:cooked_lionfish` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Lobster Tail | `alexsmobs:cooked_lobster_tail` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Perch | `upgrade_aquatic:cooked_perch` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Pike | `upgrade_aquatic:cooked_pike` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Salmon | `minecraft:cooked_salmon` | ok | ok | ok | yes / yes | coaster | Campfire / Skillet, Furnace, Mixer, Smoker |
| Kippers | `brewinandchewin:kippers` | ok | **no** | ok | no / no | - | Keg |
| Surströmming | `vintagedelight:surstromming` | ok | **no** | ok | yes / yes | - | vintagedelight:fermenting |

#### Cooked cut fish (`fish/cooked_cut`)

- R1 (Cannable): **Rule:** Per item, can parity 2.4 (raw: Fish Meal 4/2 x 4; slices share the whole fish's count) and 2.8 (cooked: x2 x 3, from the cut) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Smoker + Furnace + Campfire: the raw slice (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cooked Calamari Slice | `rusticdelight:cooked_calamari_slice` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Cooked Catfish Slice | `alexsdelight:cooked_catfish_slice` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Cod Slice | `farmersdelight:cooked_cod_slice` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Mixer, Slice and Dice, Smoker |
| Cooked Guardian Fillet | `hostiledelight:cookedguardianfillet` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Cooked Perch Slice | `abnormals_delight:cooked_perch_slice` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Cooked Pike Slice | `abnormals_delight:cooked_pike_slice` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Slice and Dice, Smoker |
| Cooked Salmon Slice | `farmersdelight:cooked_salmon_slice` | ok | **no** | ok | yes / yes | - | Campfire / Skillet, Cutting Board, Furnace, Mixer, Slice and Dice, Smoker |
| Cooked Slice of Elder Guardian | `oceansdelight:cooked_elder_guardian_slice` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |

#### Egg (`egg`)

No direct members; see the subfamilies.

#### Cooked egg (`egg/cooked`)

- R1 (Cannable): **Rule:** Never canned (precedent); per item, can parity 2.20 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Campfire / Skillet + Smoker + Furnace: one egg; scrambled eggs in the Cooking Pot; a cured egg (century egg) its own station (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Boiled Emu Egg | `alexsmobs:boiled_emu_egg` | ok | **no** | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Century Egg | `vintagedelight:century_egg` | ok | **no** | ok | no / no | - | vintagedelight:fermenting |
| Fried Dragon Egg | `ends_delight:fried_dragon_egg` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Fried Egg | `farmersdelight:fried_egg` | ok | ok | ok | no / no | coaster | Campfire / Skillet, Cooking Pot, Furnace, Mixer, Smoker |
| Fried Turtle Egg | `hostiledelight:friedturtleegg` | - | **no** | ok | no row / no | - | Campfire / Skillet, Furnace, Smoker |
| Scrambled Eggs | `incubation:scrambled_eggs` | ok | **no** | ok | no / no | - | Cooking Pot, Crafting, Mixer |

#### Egg dish (`egg/dish`)

- R1 (Cannable): **Rule:** Never canned (precedent); per item, can parity 2.20 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Display Delight placed (patcherMod adds a placed block for each dish Display Delight has none for) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** Pot meal rule: Cooking Pot, plus a heatless `_manual_only` shapeless craft from cooked forms (one more main ingredient) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bacon and Eggs | `farmersdelight:bacon_and_eggs` | ok | ok | - | no / no | DD placed, coaster | Crafting, Mixer, Sequenced assembly |
| Cauliflower Kuku | `veggiesdelight:cauliflower_kuku` | ok | ok | ok | no / no | DD placed | Cooking Pot, Crafting, Mixer |
| Dandelion Greens And Eggs | `veggiesdelight:dandelion_and_eggs` | ok | ok | - | no / no | DD placed | Crafting |
| Diced Potatoes with Egg and Tomato | `moredelight:diced_potatoes_with_egg_and_tomato` | ok | ok | ok | no / no | DD placed | Cooking Pot, Crafting, Mixer |
| Inky Cap Scrambled Eggs | `fungidelight:inky_cap_scrambled_eggs` | - | ok | - | no row / no | DD placed | Crafting |
| Omelet | `moredelight:omelette` | ok | ok | - | no / no | DD placed | Campfire / Skillet, Furnace, Smoker |
| Shakshouka | `veggiesdelight:shakshouka` | ok | ok | ok | no / no | DD placed | Cooking Pot, Crafting, Mixer |
| Shulker Omelette | `ends_delight:shulker_omelette` | - | ok | - | no row / no | DD placed | Campfire / Skillet, Furnace, Smoker |
| Suspicious Bacon And Eggs | `hostiledelight:suspiciousbaconandaggs` | - | ok | - | no row / no | DD placed | Crafting |
| Vegetable Omelet | `brewinandchewin:vegetable_omelet` | ok | ok | ok | no / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |

#### Dairy (`dairy`)

- R1 (Cannable): **Rule:** Cheese wedges, 4 per can in the Cooking Pot, x1.25 x 4 servings (can parity 2.20) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Placeable: at least on a coaster (every raw or cooked produce food, egg and cheese is in the coaster tag; one without its own coaster model is drawn flat) (owner 2026-10-25 (4.2 D1))
- R3 (Recipe shape by station): **Rule:** none: cut from a cheese wheel (owner 2026-10-25 (4.2 D1))

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cheese Slice | `vintagedelight:cheese_slice` | ok | **no** | - | yes / yes | - | Cutting Board, Slice and Dice |
| Flaxen Cheese Wedge | `brewinandchewin:flaxen_cheese_wedge` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |
| Scarlet Cheese Wedge | `brewinandchewin:scarlet_cheese_wedge` | ok | ok | - | yes / yes | coaster | Cutting Board, Slice and Dice |

#### Bread (`bread`)

No direct members; see the subfamilies.

#### Loaf (`bread/loaf`)

- R1 (Cannable): **Rule:** x2 x 4 servings, 6 bread; slices, toast, tortilla and scones not canned (can parity 2.9) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Bread only: dough block smoked into a bread block, sawed or knifed into 16 bread ([recipes_cooking.md](recipes_cooking.md) 2.1)
- R3 (Recipe shape by station): **Decision:** Smoker + Furnace + Campfire: the dough

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Banana Bread | `neapolitan:banana_bread` | ok | - | **no** | yes / yes | - | Crafting |
| Bread | `minecraft:bread` | ok | ok | - | yes / yes | coaster | Campfire / Skillet, Crafting, Cutting Board, Furnace, Mechanical Saw, Mixer, Oven, Slice and Dice, Smoker |
| Cauliflower Bread | `veggiesdelight:cauliflower_bread` | ok | - | **no** | yes / yes | - | Oven |
| Cornbread | `corn_delight:cornbread` | ok | - | ok | yes / yes | - | Campfire / Skillet, Furnace, Smoker |
| Garlic Bread | `veggiesdelight:garlic_bread` | ok | - | **no** | yes / yes | - | Crafting |
| Nether Wart Sourdough | `farmersrespite:nether_wart_sourdough` | ok | - | **no** | yes / yes | - | Crafting |
| Sourdough Bread | `pneumaticcraft:sourdough_bread` | ok | - | **no** | yes / yes | - | Oven, Smoker |

#### Bread slice (`bread/slice`)

- R1 (Cannable): **Rule:** x2 x 4 servings, 6 bread; slices, toast, tortilla and scones not canned (can parity 2.9) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Decision:** Cutting Board: the loaf -> slices; toast = slice in the trio
- R3 (Other (values, uses)): **Rule:** Every toast eats fast (16 ticks, like dried kelp); CD moredelight/toast.js ([more_delight.md](more_delight.md) 2.3)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bread Slice | `moredelight:bread_slice` | ok | ok | - | no / no | - | Cutting Board, Slice and Dice |
| Toast | `moredelight:toast` | ok | ok | ok | no / no | Supp. jar | Campfire / Skillet, Furnace, Smoker |

#### Flatbread (`bread/flat`)

- R1 (Cannable): **Rule:** x2 x 4 servings, 6 bread; slices, toast, tortilla and scones not canned (can parity 2.9) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Bread only: dough block smoked into a bread block, sawed or knifed into 16 bread ([recipes_cooking.md](recipes_cooking.md) 2.1)
- R3 (Recipe shape by station): **Decision:** Smoker + Furnace + Campfire: the dough

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Tortilla | `corn_delight:tortilla` | ok | - | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |

#### Kitchen ingredient (`ingredient`)

No direct members; see the subfamilies.

#### Dough and batter (`ingredient/dough`)

- R1 (Cannable): **Rule:** Per item, can parity 2.26 (Canned Dough: 8 dough, not edible; tomato sauce in the preserve section) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Pie crust: 3 flour + egg + milk, or 3 dough + milk, manual craft -> 3 ([oven.md](oven.md) 2.6)

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Batter | `rusticdelight:batter` | ok | - | no / no | - | Cooking Pot, Mixer |
| Cake Base | `createaddition:cake_base` | - | - | no row / no | - | Crafting |
| Cake Paste | `create_factory:cake_paste` | - | - | no row / no | - | Mixer |
| Cornbread Batter | `corn_delight:cornbread_batter` | ok | - | no / no | - | Crafting, Mixer |
| Cucumber Dough | `vintagedelight:cucumber_dough` | ok | - | no / no | - | Crafting |
| Dough | `create:dough` | ok | - | yes / yes | - | Bottling Machine, Crafting, Fan (washing), Mixer |
| Gingerdough | `create_confectionery:gingerdough` | - | - | no row / no | - | Mixer |
| Oat Dough | `vintagedelight:oat_dough` | ok | - | no / no | - | Crafting |
| Pie Crust | `farmersdelight:pie_crust` | ok | ok | no / no | - | Crafting |
| Raw Tortilla | `corn_delight:tortilla_raw` | ok | - | no / no | - | Bottling Machine, Crafting, Fan (washing) |
| Sourdough | `pneumaticcraft:sourdough` | - | - | no row / no | - | Bottling Machine, Crafting, Mixer |
| Sweet Potato Dough | `veggiesdelight:sweet_potato_dough` | ok | - | no / no | - | Crafting |
| Uncooked Waffle | `create_factory:uncooked_waffle` | - | - | no row / no | - | Press |
| creativeengineering:cookie_dough | `creativeengineering:cookie_dough` | - | - | no row / no | - | Crafting |

#### Sauce and fat (`ingredient/sauce`)

- R1 (Cannable): **Rule:** Per item, can parity 2.26 (Canned Dough: 8 dough, not edible; tomato sauce in the preserve section) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Pie crust: 3 flour + egg + milk, or 3 dough + milk, manual craft -> 3 ([oven.md](oven.md) 2.6)

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Animal Fat | `delightful:animal_fat` | ok | - | no / no | - | - |
| Apple Sauce Jar | `vintagedelight:apple_sauce_jar` | ok | - | no / no | own block, coaster | Cooking Pot, Crafting, Mixer |
| Ardent Sauce | `hostiledelight:ardentsauce` | - | - | no row / no | - | Cooking Pot, Mixer |
| Chorus Sauce | `ends_delight:chorus_sauce` | - | - | no row / no | - | Cooking Pot, Mixer |
| Cooking Oil | `rusticdelight:cooking_oil` | ok | - | no / no | - | Cooking Pot, Mixer |
| Glowing Sauce | `hostiledelight:glowingsauce` | - | - | no row / no | - | Cooking Pot, Mixer |
| Tomato Sauce | `farmersdelight:tomato_sauce` | ok | - | yes / yes | coaster, Supp. jar | Bottling Machine, Cooking Pot, Crafting, Mixer, Spout |

#### Other ingredient (`ingredient/other`)

- R1 (Cannable): **Rule:** Per item, can parity 2.26 (Canned Dough: 8 dough, not edible; tomato sauce in the preserve section) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Pie crust: 3 flour + egg + milk, or 3 dough + milk, manual craft -> 3 ([oven.md](oven.md) 2.6)

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Apple Mash | `vinery:apple_mash` | ok | - | no / no | - | Apple Press |
| Bone Gelatin | `hostiledelight:bonegelatin` | - | - | no row / no | - | Crafting |
| Cheese Curds | `vintagedelight:cheese_curds` | ok | - | no / no | - | Cooking Pot, Mixer |
| Chopped Clover | `delightful:chopped_clover` | ok | - | no / no | - | - |
| Dried Vanilla Pods | `neapolitan:dried_vanilla_pods` | ok | - | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Ghast Blood | `hostiledelight:ghastblood` | - | - | no row / no | - | Crafting |
| Gnocchi | `veggiesdelight:potato_noodle` | ok | - | no / no | - | Cooking Pot, Cutting Board, Mixer, Slice and Dice |
| Golden Coffee Beans | `rusticdelight:golden_coffee_beans` | ok | - | no / no | - | Crafting |
| Green Tea Leaves | `delightful:green_tea_leaf` | ok | - | no / no | - | - |
| Liquid Dragon Egg | `ends_delight:liquid_dragon_egg` | - | - | no row / no | - | Cutting Board, Slice and Dice |
| Magical Butter | `hostiledelight:magicalbutter` | - | - | no row / no | - | Crafting |
| Nut Mash Bottle | `vintagedelight:nut_mash_bottle` | ok | - | no / no | - | Crafting |
| Nut Mash Jar | `vintagedelight:nut_mash_jar` | ok | - | no / no | own block, coaster | Cooking Pot, Crafting, Mixer |
| Raw Pasta | `farmersdelight:raw_pasta` | ok | - | no / no | - | Cutting Board, Metal Press, Slice and Dice |
| Refined Gelatin | `hostiledelight:refinedgelatin` | - | - | no row / no | - | Smoker |
| Relish Jar | `vintagedelight:relish_jar` | ok | - | no / no | own block, coaster | Cooking Pot, Mixer |
| Roasted Coffee Beans | `rusticdelight:roasted_coffee_beans` | ok | - | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Shulker Omelette Mixture | `ends_delight:shulker_omelette_mixture` | - | - | no row / no | - | Crafting |
| Silme Syrup | `hostiledelight:silmesyrup` | - | - | no row / no | - | Crafting |
| Sweet Berry Mince | `berry_good:sweet_berry_mince` | ok | - | no / no | - | Crafting |
| Veggie Noodles | `vintagedelight:cucumber_noodles` | ok | - | no / no | - | Cutting Board, Slice and Dice |
| Vinegar Jar | `vintagedelight:vinegar_jar` | ok | - | no / no | own block, coaster | Crafting, vintagedelight:fermenting |

#### Preserve (`preserve`)

No direct members; see the subfamilies.

#### Jam (`preserve/jam`)

- R1 (Cannable): **Rule:** Jams and sauces not canned (precedent); preserve section x1 x 4 (tomato sauce); per item, can parity 2.21 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block, while sneaking (Fruits Delight jams) ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Decision:** Cooking Pot: fruit + sugar in a jar (Mixer mirrors it)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Apple Jam | `fruitsdelight:apple_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Bayberry Jam | `fruitsdelight:bayberry_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Blueberry Jam | `fruitsdelight:blueberry_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Cantaloupe Jam | `fruitsdelight:hamimelon_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Chorus Jam | `fruitsdelight:chorus_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Cranberry Jam | `fruitsdelight:cranberry_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Durian Jam | `fruitsdelight:durian_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Fig Jam | `fruitsdelight:fig_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Glowberry Jam | `fruitsdelight:glowberry_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Hawberry Jam | `fruitsdelight:hawberry_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Kiwi Jam | `fruitsdelight:kiwi_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Lemon Jam | `fruitsdelight:lemon_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Lychee Jam | `fruitsdelight:lychee_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Mango Jam | `fruitsdelight:mango_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Mangosteen Jam | `fruitsdelight:mangosteen_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Melon Jam | `fruitsdelight:melon_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Mulberry Jam Bottle | `upgrade_aquatic:mulberry_jam_bottle` | ok | - | **no** | no / no | Supp. jar | Crafting, Spout |
| Orange Jam | `fruitsdelight:orange_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Peach Jam | `fruitsdelight:peach_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Pear Jam | `fruitsdelight:pear_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Pepper Jam Jar | `vintagedelight:pepper_jam_jar` | ok | - | ok | no / no | own block, coaster | Cooking Pot, Mixer |
| Persimmon Jam | `fruitsdelight:persimmon_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Pineapple Jam | `fruitsdelight:pineapple_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |
| Sweetberry Jam | `fruitsdelight:sweetberry_jelly` | ok | ok | ok | no / no | own block, sneak | Cooking Pot, Crafting, Mixer, Spout |

#### Spread (`preserve/spread`)

- R1 (Cannable): **Rule:** Jams and sauces not canned (precedent); preserve section x1 x 4 (tomato sauce); per item, can parity 2.21 ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Decision:** Cooking Pot: fruit + sugar in a jar (Mixer mirrors it)

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Nut Butter Bottle | `delightful:nut_butter_bottle` | ok | ok | no / no | - | Cooking Pot, Mixer |

#### Pickle (`preserve/pickle`)

- R1 (Cannable): **Rule:** Jams and sauces not canned (precedent); preserve section x1 x 4 (tomato sauce); per item, can parity 2.21 ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Decision:** Keg: vegetable + salt / brine

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Canned Pickled Beetroot | `creativeengineering:canned_pickled_beetroot` | - | **no** | no row / no | - | Crafting |
| Canned Pickled Onions | `creativeengineering:canned_pickled_onion` | - | **no** | no row / no | - | Crafting |
| Canned Pickled Peppers | `creativeengineering:canned_pickled_pepper` | - | **no** | no row / no | - | Crafting |
| Fermented Garlic Honey | `veggiesdelight:fermented_garlic_honey` | ok | **no** | no / no | Supp. jar | vintagedelight:fermenting |
| Kimchi | `brewinandchewin:kimchi` | ok | **no** | yes / yes | - | vintagedelight:fermenting |
| Kimchi | `vintagedelight:kimchi` | ok | **no** | yes / yes | - | vintagedelight:fermenting |
| Pickle | `vintagedelight:pickle` | ok | **no** | yes / yes | - | vintagedelight:fermenting |
| Pickled Beetroot | `vintagedelight:pickled_beetroot` | ok | **no** | yes / yes | - | vintagedelight:fermenting |
| Pickled Egg | `vintagedelight:pickled_egg` | ok | **no** | no / no | - | vintagedelight:fermenting |
| Pickled Onion | `vintagedelight:pickled_onion` | ok | **no** | yes / yes | - | vintagedelight:fermenting |
| Pickled Pepper | `vintagedelight:pickled_pepper` | ok | **no** | yes / yes | - | vintagedelight:fermenting |
| Pickled Sea Pickle | `creativeengineering:pickled_sea_pickle` | **no** | **no** | no row / yes | - | vintagedelight:fermenting |

#### Jello (`jello`)

- R1 (Cannable): **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Crafting: jam + gelatin in a bowl (Spout mirrors it)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Apple Jello | `fruitsdelight:apple_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Bayberry Jello | `fruitsdelight:bayberry_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Blueberry Jello | `fruitsdelight:blueberry_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Bucket of Sweet Gelatine | `quarkdelight:bucket_of_sweet_gelatine` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Cantaloupe Jello | `fruitsdelight:hamimelon_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Chorus Jello | `fruitsdelight:chorus_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Cranberry Jello | `fruitsdelight:cranberry_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Durian Jello | `fruitsdelight:durian_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Fig Jello | `fruitsdelight:fig_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Glowberry Jello | `fruitsdelight:glowberry_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Hawberry Jello | `fruitsdelight:hawberry_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Kiwi Jello | `fruitsdelight:kiwi_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Lemon Jello | `fruitsdelight:lemon_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Lychee Jello | `fruitsdelight:lychee_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Magma Jelly | `hostiledelight:magmajelly` | - | **no** | ok | no row / no | - | Crafting |
| Mango Jello | `fruitsdelight:mango_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Mangosteen Jello | `fruitsdelight:mangosteen_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Melon Jello | `fruitsdelight:melon_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Orange Jello | `fruitsdelight:orange_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Peach Jello | `fruitsdelight:peach_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Pear Jello | `fruitsdelight:pear_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Persimmon Jello | `fruitsdelight:persimmon_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Pineapple Jello | `fruitsdelight:pineapple_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Sweetberry Jello | `fruitsdelight:sweetberry_jello` | ok | **no** | ok | no / no | - | Crafting, Spout |

#### Pie (`pie`)

No direct members; see the subfamilies.

#### Whole pie (`pie/whole`)

- R1 (Cannable): **Rule:** 4 slices -> 1 filling can (4 slice-sized servings); crust + full can -> the whole pie; tart cans are pie fillings (can parity 2.13) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Rule:** Oven (pie tin): pie crust + 2 fruit + 2 sugar (honey allowed) ([oven.md](oven.md) 2.6)
- R4 (Recipe shape by station): **Rule:** Oven or Crafting: pie crust + a full can of its filling (all but durian, pineapple, sponge and End's chorus fruit pie: Ender's chorus pie owns the chorus filling can) ([oven.md](oven.md) 2.6)
- R5 (Recipe shape by station): **Rule:** No other recipe (every other mod's pie recipe removed) ([oven.md](oven.md) 2.6)
- R6 (Recipe shape by station): **Planned:** Sequenced assembly for every pie ([TODO](todo/TODO.md) baked-goods #4)

| Member | Id | R1 | R2 | R3 | R4 | R5 | R6 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|---|---|
| Apple Pie | `farmersdelight:apple_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |
| Chocolate Pie | `farmersdelight:chocolate_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |
| Chorus Fruit Pie | `ends_delight:chorus_fruit_pie` | - | ok | ok | - | ok | **no** | no row / no | own block | Crafting, Oven |
| Chorus Pie | `endersdelight:chorus_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |
| Mulberry Pie | `upgrade_aquatic:mulberry_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |
| Pumpkin Pie | `minecraft:pumpkin_pie` | - | ok | ok | ok | ok | **no** | no row / yes | own block, Food Plate, coaster | Crafting, Oven |
| Rose Hip Pie | `farmersrespite:rose_hip_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |
| Salmonberry Pie | `delightful:salmonberry_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |
| Sponge Cake | `oceanic_delight:sponge_cake` | - | ok | ok | - | ok | **no** | no row / no | own block | Crafting, Oven |
| Sweet Potato Pie | `veggiesdelight:sweet_potato_pie` | - | ok | ok | ok | ok | **no** | no row / no | own block | Crafting, Oven |

#### Pie slice (`pie/slice`)

- R1 (Cannable): **Rule:** 4 slices -> 1 filling can (4 slice-sized servings); crust + full can -> the whole pie; tart cans are pie fillings (can parity 2.13) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Small Food Plate
- R3 (Recipe shape by station): **Decision:** Cutting Board: whole pie -> slices

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Chorus Pie Slice | `endersdelight:chorus_pie_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Rose Hip Pie Slice | `farmersrespite:rose_hip_pie_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Apple Pie | `farmersdelight:apple_pie_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Metal Press, Slice and Dice |
| Slice of Blueberry Pie | `delightful:blueberry_pie_slice` | **no** | **no** | **no** | yes / no | - | - |
| Slice of Cherry Pie | `create_central_kitchen:cherry_pie_slice` | **no** | **no** | **no** | yes / no | - | - |
| Slice of Chocolate Pie | `farmersdelight:chocolate_pie_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Metal Press, Slice and Dice |
| Slice of Chorus Fruit Pie | `ends_delight:chorus_fruit_pie_slice` | - | ok | ok | no row / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Crimson Berry Pie | `delightful:crimson_berry_pie_slice` | **no** | **no** | **no** | yes / no | - | - |
| Slice of Gloomgourd Pie | `delightful:gloomgourd_pie_slice` | ok | **no** | **no** | no / no | - | - |
| Slice of Green Apple Pie | `delightful:green_apple_pie_slice` | **no** | **no** | **no** | yes / no | - | - |
| Slice of Mulberry Pie | `create_central_kitchen:mulberry_pie_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |
| Slice of Nightshade Berry Pie | `delightful:nightshade_berry_pie_slice` | **no** | **no** | **no** | yes / no | - | - |
| Slice of Pumpkin Pie | `delightful:pumpkin_pie_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Pumpkin Pie | `farmersdelight:pumpkin_pie_slice` | - | ok | **no** | no row / no | Small Food Plate | - |
| Slice of Salmonberry Pie | `delightful:salmonberry_pie_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Source Berry Pie | `delightful:source_berry_pie_slice` | ok | **no** | **no** | no / no | - | - |
| Slice of Truffle Pie | `create_central_kitchen:truffle_pie_slice` | **no** | **no** | **no** | yes / no | - | - |
| Sponge Cake Slice | `oceanic_delight:sponge_cake_slice` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Sweet Potato Pie Slice | `veggiesdelight:sweet_potato_pie_slice` | ok | **no** | ok | yes / yes | - | Cutting Board, Slice and Dice |

#### Tart and hand pie (`pie/small`)

- R1 (Cannable): **Rule:** 4 slices -> 1 filling can (4 slice-sized servings); crust + full can -> the whole pie; tart cans are pie fillings (can parity 2.13) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Tarts: Oven (baking sheet): fruit + pie crust + egg + sugar + milk -> 2 ([oven.md](oven.md) 2.6)
- R3 (Recipe shape by station): **Rule:** Hand pies: Oven only (baking sheet): pie crust + the filling -> 1 (CD handPieBake) ([oven.md](oven.md) 2.6)
- R4 (Recipe shape by station): **Planned:** Every tart becomes a pie (renamed, retextured) ([TODO](todo/TODO.md) baked-goods #6)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Chorus Flower Pie | `ends_delight:chorus_flower_pie` | - | - | ok | - | no row / no | Food Plate, coaster | Oven |
| Durian Pie | `fruitsdelight:durian_pie` | ok | - | - | - | yes / yes | - | Oven |
| Fig Tart | `fruitsdelight:fig_tart` | ok | ok | - | **no** | yes / yes | - | Cooking Pot, Mixer, Oven |
| Green Pie | `hostiledelight:greenpie` | - | - | ok | - | no row / no | - | Oven |
| Lemon Tart | `fruitsdelight:lemon_tart` | ok | ok | - | **no** | yes / yes | - | Cooking Pot, Mixer, Oven |
| Pineapple Pie | `fruitsdelight:pineapple_pie` | ok | - | - | - | yes / yes | - | Oven |
| Rotten Pie | `hostiledelight:rottenpie` | - | - | ok | - | no row / no | - | Oven |

#### Cake (`cake`)

No direct members; see the subfamilies.

#### Whole cake (`cake/whole`)

- R1 (Cannable): **Rule:** 1 cake, or 4 slices + sugar or honey -> 4 fast servings of 1.75 slices (can parity 2.23) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Rule:** Cakes: Crafting (manual): baked cake base + frosting + 2 flavour ([oven.md](oven.md) 2.6)
- R4 (Recipe shape by station): **Rule:** Cakes: Oven (cake pan): raw cake base + frosting + 2 flavour ([oven.md](oven.md) 2.6)
- R5 (Recipe shape by station): **Rule:** Cakes: no other recipe (every other mod's cake recipe removed; `_from_slices` reassembly kept; plain and chocolate cake also spout onto a baked base) ([oven.md](oven.md) 2.6)

| Member | Id | R1 | R2 | R3 | R4 | R5 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|---|
| Adzuki Cake | `neapolitan:adzuki_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Banana Cake | `neapolitan:banana_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Cake | `minecraft:cake` | - | ok | ok | ok | ok | no row / yes | own block | Crafting, Oven, Spout |
| Carrot Cake | `veggiesdelight:carrot_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Cherry Blossom Cheesecake | `rusticdelight:cherry_blossom_cheesecake` | - | ok | **no** | **no** | ok | no row / no | own block | Crafting, Oven |
| Chocolate Cake | `createaddition:chocolate_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven, Spout |
| Coffee Cake | `farmersrespite:coffee_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Coffee Cheesecake | `rusticdelight:coffee_cheesecake` | - | ok | **no** | **no** | ok | no row / no | own block | Crafting, Oven |
| Honey Cake | `createaddition:honey_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Mangosteen Cake | `fruitsdelight:mangosteen_cake` | ok | ok | ok | ok | ok | no / no | own block, sneak | Crafting, Oven |
| Mint Cake | `neapolitan:mint_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Strawberry Cake | `neapolitan:strawberry_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |
| Sweet Berry Cheesecake | `farmersdelight:sweet_berry_cheesecake` | - | ok | **no** | **no** | ok | no row / no | own block | Crafting, Oven |
| Syrup Cheesecake | `rusticdelight:syrup_cheesecake` | - | ok | **no** | **no** | ok | no row / no | own block | Crafting, Oven |
| Vanilla Cake | `neapolitan:vanilla_cake` | - | ok | ok | ok | ok | no row / no | own block | Crafting, Oven |

#### Cake slice (`cake/slice`)

- R1 (Cannable): **Rule:** 1 cake, or 4 slices + sugar or honey -> 4 fast servings of 1.75 slices (can parity 2.23) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Planned:** All slices placeable (method not stated; proposal: Small Food Plate) ([TODO](todo/TODO.md) baked-goods #5)
- R3 (Recipe shape by station): **Decision:** Cutting Board: whole cake -> slices

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Carrot Cake Slice | `veggiesdelight:carrot_cake_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Slice of Adzuki Cake | `abnormals_delight:adzuki_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Aloe Cake | `create_central_kitchen:aloe_cake_slice` | ok | **no** | **no** | no / no | - | - |
| Slice of Banana Cake | `abnormals_delight:banana_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Cake | `farmersdelight:cake_slice` | **no** | ok | ok | no / yes | Small Food Plate, coaster | Cutting Board, Metal Press, Slice and Dice |
| Slice of Cherry Blossom Cheesecake | `rusticdelight:cherry_blossom_cheesecake_slice` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Slice of Chocolate Cake | `abnormals_delight:chocolate_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Chocolate Cake | `create_central_kitchen:chocolate_cake_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Slice of Coffee Cake | `farmersrespite:coffee_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Coffee Cheesecake | `rusticdelight:coffee_cheesecake_slice` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Slice of Honey Cake | `create_central_kitchen:honey_cake_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Slice of Mint Cake | `abnormals_delight:mint_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Passionfruit Cake | `create_central_kitchen:passionfruit_cake_slice` | ok | **no** | **no** | no / no | - | - |
| Slice of Pumpkin Cake | `create_central_kitchen:pumpkin_cake_slice` | ok | **no** | **no** | no / no | - | - |
| Slice of Strawberry Cake | `abnormals_delight:strawberry_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Sweet Berry Cake | `create_central_kitchen:sweet_berry_cake_slice` | ok | **no** | **no** | no / no | - | - |
| Slice of Sweet Berry Cheesecake | `farmersdelight:sweet_berry_cheesecake_slice` | ok | ok | ok | yes / yes | Small Food Plate, coaster | Cutting Board, Metal Press, Slice and Dice |
| Slice of Syrup Cheesecake | `rusticdelight:syrup_cheesecake_slice` | - | **no** | ok | no row / no | - | Cutting Board, Slice and Dice |
| Slice of Vanilla Cake | `abnormals_delight:vanilla_cake_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Slice of Yucca Cake | `create_central_kitchen:yucca_cake_slice` | ok | **no** | **no** | no / no | - | - |

#### Muffin and cupcake (`cake/muffin`)

- R1 (Cannable): **Rule:** 1 cake, or 4 slices + sugar or honey -> 4 fast servings of 1.75 slices (can parity 2.23) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Oven (muffin tin, on paper): 2 fruit + wheat flour + milk + egg + sugar -> 4; Cooking Pot: 2 fruit + dough + milk + egg + sugar -> 2 (CD muffinBake) ([oven.md](oven.md) 2.6)

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Blueberry Muffin | `fruitsdelight:blueberry_muffin` | ok | ok | no / no | - | Cooking Pot, Mixer, Oven |
| Cranberry Muffin | `fruitsdelight:cranberry_muffin` | ok | ok | no / no | - | Cooking Pot, Mixer, Oven |
| Sweet Potato Cupcake | `veggiesdelight:sweet_potato_cupcake` | ok | ok | no / no | - | Cooking Pot, Crafting, Mixer, Oven |

#### Cookie (`cookie`)

- R1 (Cannable): **Rule:** x2 x 5 fast servings, 8 cookies or 6 + sugar (can parity 2.5) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Supplementaries jar (`#supplementaries:cookies`)
- R3 (Recipe shape by station): **Rule:** Oven only (baking sheet): cookie dough + 2 flavour -> 12 ([oven.md](oven.md) 2.6)
- R4 (Recipe shape by station): **Rule:** No other recipe ([oven.md](oven.md) 2.6)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Bat Cookie | `miners_delight:bat_cookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Bayberry Cookie | `fruitsdelight:bayberry_cookie` | ok | ok | ok | ok | yes / yes | Supp. jar | Oven |
| Canned Oatmeal Cookies | `creativeengineering:canned_oatmeal_cookie` | - | **no** | **no** | **no** | no row / no | - | Crafting |
| Cherry Blossom Cookie | `rusticdelight:cherry_blossom_cookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Cherry Cookie | `abnormals_delight:cherry_cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Chorus Cookie | `ends_delight:chorus_cookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Coffee Cookie | `rusticdelight:coffee_cookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Cookie | `minecraft:cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Cranberry Cookie | `fruitsdelight:cranberry_cookie` | ok | ok | ok | ok | yes / yes | Supp. jar | Oven |
| Crawling Cookie | `hostiledelight:mitecookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Emitting Cookie | `hostiledelight:blazecookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Gazing cookie | `hostiledelight:guardiancookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Gingerbread Man | `create_confectionery:gingerbread_man` | ok | ok | ok | ok | no / no | Supp. jar | Oven |
| Green Tea Cookie | `farmersrespite:green_tea_cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Honey Cookie | `farmersdelight:honey_cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Lemon Cookie | `fruitsdelight:lemon_cookie` | ok | ok | ok | ok | yes / yes | Supp. jar | Oven |
| Magical Butter Cookie | `hostiledelight:magicbuttercookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Maple Cookie | `abnormals_delight:maple_cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Mulberry Cookie | `abnormals_delight:mulberry_cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Oatmeal Cookie | `vintagedelight:oatmeal_cookie` | ok | ok | ok | ok | yes / yes | Supp. jar | Oven |
| Persimmon Cookie | `fruitsdelight:persimmon_cookie` | ok | ok | ok | ok | yes / yes | Supp. jar | Oven |
| Sweet Berry Cookie | `farmersdelight:sweet_berry_cookie` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Swelling cookie | `hostiledelight:ghastcookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Syrup Cookie | `rusticdelight:syrup_cookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Trembling cookie | `hostiledelight:pearlcookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |
| Uncanny Cookie | `endersdelight:uncanny_cookies` | ok | ok | ok | ok | yes / yes | Food Plate, coaster, Supp. jar | Oven |
| Vein cookie | `hostiledelight:magmacookie` | - | ok | ok | ok | no row / no | Supp. jar | Oven |

#### Dessert (`dessert`)

No direct members; see the subfamilies.

#### Brownie (`dessert/brownie`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Tray: own block (cut into brownies)
- R3 (Recipe shape by station): **Decision:** Oven: the tray (cake pan); Cutting Board: tray -> brownies

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Beetroot Brownie | `veggiesdelight:beetroot_brownie` | ok | - | ok | no / no | - | Cutting Board, Slice and Dice |
| Beetroot Brownie Tray | `veggiesdelight:beetroot_brownie_tray` | - | ok | **no** | no row / no | own block | Crafting |
| Magma Brownie | `hostiledelight:magmabrownie` | - | - | **no** | no row / no | - | Crafting |
| Slime Brownie | `hostiledelight:slimebrownie` | - | - | **no** | no row / no | - | Crafting |

#### Pudding (`dessert/pudding`)

- R1 (Cannable): **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Bowl puddings: Cooking Pot (Mixer mirrors it); fig pudding: Oven, cut on the board

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Blueberry Custard | `fruitsdelight:blueberry_custard` | ok | **no** | ok | no / no | - | Cooking Pot, Mixer |
| Fig Pudding | `fruitsdelight:fig_pudding` | - | **no** | **no** | no row / no | own block | Crafting |
| Fig Pudding Slice | `fruitsdelight:fig_pudding_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Ghast Pudding | `hostiledelight:ghastpudding` | - | **no** | **no** | no row / no | - | Crafting |
| Glow Berry Custard | `farmersdelight:glow_berry_custard` | ok | ok | ok | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Glowgurt | `berry_good:glowgurt` | ok | **no** | **no** | no / no | - | Crafting |
| Vanilla Pudding | `neapolitan:vanilla_pudding` | ok | **no** | **no** | no / no | - | Crafting, Mixer |

#### Pancake (`dessert/pancake`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Every pancake can be put down: Rustic's plates and Supplementaries' Pancake are their own stack blocks (a single Rustic pancake goes back on its plate with sneak-use), Sweet Potato Pancakes a Display Delight placed dish (owner 2026-11-02 ([TODO](todo/TODO.md) merges/baking #2, dishes/meals #12; 4.7))

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Cherry Blossom Pancake | `rusticdelight:cherry_blossom_pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |
| Chocolate Pancake | `rusticdelight:chocolate_pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |
| Coffee Pancake | `rusticdelight:coffee_pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |
| Honey Pancake | `rusticdelight:honey_pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |
| Pancake | `rusticdelight:pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |
| Pumpkin Pancake | `rusticdelight:pumpkin_pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |
| Sweet Potato Pancakes | `veggiesdelight:sweet_potato_pancakes` | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Vegetable Pancake | `rusticdelight:vegetable_pancake` | - | - | no row / no | - | Crafting, Cutting Board, Slice and Dice |

#### Pastry (`dessert/pastry`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Oven for the plain bake; toppings by Spout

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Adzuki Bun | `neapolitan:adzuki_bun` | ok | **no** | **no** | yes / yes | - | Crafting, Mixer |
| Strange Eclair | `endersdelight:strange_eclair` | ok | ok | **no** | no / no | Food Plate, coaster | Crafting |
| Strawberry Scones | `neapolitan:strawberry_scones` | ok | **no** | **no** | no / no | - | Crafting, Mixer |

#### Waffle (`dessert/waffle`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Oven for the plain bake; toppings by Spout

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Caramel Waffle | `create_factory:caramel_waffle` | ok | **no** | ok | no / no | - | Spout |
| Chocolate Waffle | `create_factory:chocolate_waffle` | ok | **no** | ok | no / no | - | Spout |
| Dark Chocolate Waffle | `create_factory:dark_chocolate_waffle` | ok | **no** | ok | no / no | - | Spout |
| Honey Waffle | `create_factory:honey_waffle` | ok | **no** | ok | no / no | - | Spout |
| Ruby Chocolate Waffle | `create_factory:ruby_chocolate_waffle` | ok | **no** | ok | no / no | - | Spout |
| Waffle | `create_factory:waffle` | ok | **no** | **no** | no / no | - | Smoker |
| White Chocolate Waffle | `create_factory:white_chocolate_waffle` | ok | **no** | ok | no / no | - | Spout |

#### Sweet roll (`dessert/roll`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Oven for the plain bake; toppings by Spout

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Caramel Roll | `create_factory:caramel_roll` | ok | **no** | ok | no / no | - | Spout |
| Chocolate Roll | `create_factory:chocolate_roll` | ok | **no** | ok | no / no | - | Spout |
| Dark Chocolate Roll | `create_factory:dark_chocolate_roll` | ok | **no** | ok | no / no | - | Spout |
| Honey Roll | `create_factory:honey_roll` | ok | **no** | ok | no / no | - | Spout |
| Ruby Chocolate Roll | `create_factory:ruby_chocolate_roll` | ok | **no** | ok | no / no | - | Spout |
| Sweet Roll | `create:sweet_roll` | ok | **no** | ok | yes / yes | - | Spout |
| White Chocolate Roll | `create_factory:white_chocolate_roll` | ok | **no** | ok | no / no | - | Spout |

#### Other dessert (`dessert/other`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Oven for the plain bake; toppings by Spout

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Biscuit of Totality | `botania:mana_cookie` | ok | **no** | **no** | no / no | Supp. jar | Mana Pool |
| Gingerbread | `create_confectionery:gingerbread` | ok | **no** | **no** | no / no | - | Furnace, Smoker |
| Glowing Bread | `hostiledelight:glowbread` | - | **no** | **no** | no row / no | - | Crafting |
| Honey Bread | `buzzier_bees:honey_bread` | ok | **no** | ok | yes / yes | - | Crafting, Spout |
| Mulberry Bread | `upgrade_aquatic:mulberry_bread` | ok | **no** | **no** | yes / yes | - | Crafting |

#### Candy (`candy`)

No direct members; see the subfamilies.

#### Chocolate bar (`candy/bar`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Supplementaries jar
- R3 (Recipe shape by station): **Decision:** Press + Basin: the fluid into a bar

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bar of Caramel | `create_confectionery:bar_of_caramel` | ok | ok | ok | no / no | Supp. jar | Press + Basin |
| Bar of Chocolate | `create:bar_of_chocolate` | ok | ok | **no** | no / no | Supp. jar | - |
| Bar of Dark Chocolate | `create_confectionery:bar_of_black_chocolate` | ok | ok | ok | no / no | Supp. jar | Press + Basin |
| Bar of Ruby Chocolate | `create_confectionery:bar_of_ruby_chocolate` | ok | ok | ok | no / no | Supp. jar | Press + Basin |
| Bar of White Chocolate | `create_confectionery:bar_of_white_chocolate` | ok | ok | ok | no / no | Supp. jar | Press + Basin |
| Chocolate Bar | `neapolitan:chocolate_bar` | ok | ok | ok | no / no | Supp. jar | Press + Basin |
| Mint Chocolate | `neapolitan:mint_chocolate` | ok | ok | **no** | no / no | Supp. jar | Crafting, Spout |
| Vanilla Chocolate Fingers | `neapolitan:vanilla_chocolate_fingers` | ok | ok | **no** | no / no | Supp. jar | Crafting, Spout |

#### Candy piece (`candy/piece`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Supplementaries jar
- R3 (Recipe shape by station): **Decision:** Spout: coating or filling (chocolate, caramel, honey) over the base

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Candied Slime | `hostiledelight:candiedslime` | - | **no** | **no** | no row / no | - | Crafting |
| Candy | `supplementaries:candy` | ok | ok | **no** | no / no | Supp. jar | Crafting |
| Candy Cane | `create_confectionery:candy_cane` | ok | ok | **no** | no / no | Supp. jar | Mixer |
| Chocolate Candy | `create_confectionery:chocolate_candy` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Candy | `create_confectionery:chocolate_candy_1` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Candy | `create_confectionery:chocolate_candy_2` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Candy | `create_confectionery:chocolate_candy_3` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Nut Granola Bar | `vintagedelight:chocolate_nut_granola_bar` | ok | **no** | **no** | no / no | - | Crafting |
| Chocolate Spider Eye | `neapolitan:chocolate_spider_eye` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Cocoa Fudge | `brewinandchewin:cocoa_fudge` | ok | ok | **no** | no / no | Supp. jar | Keg |
| Cotton Candy | `cloudstorage:cotton_candy` | ok | **no** | **no** | no / no | - | Crafting |
| Dark Chocolate Candy | `create_confectionery:black_chocolate_candy` | ok | ok | ok | no / no | Supp. jar | Spout |
| Dark Chocolate Candy | `create_confectionery:black_chocolate_candy_1` | ok | ok | ok | no / no | Supp. jar | Spout |
| Dark Chocolate Candy | `create_confectionery:black_chocolate_candy_2` | ok | ok | ok | no / no | Supp. jar | Spout |
| Dark Chocolate Candy | `create_confectionery:black_chocolate_candy_3` | ok | ok | ok | no / no | Supp. jar | Spout |
| Deluxe Granola Bar | `vintagedelight:deluxe_granola_bar` | ok | **no** | **no** | no / no | - | Crafting |
| Fruity Granola Bar | `vintagedelight:fruity_granola_bar` | ok | **no** | **no** | no / no | - | Crafting |
| Glow Gummy | `hostiledelight:glowgummy` | - | **no** | **no** | no row / no | - | Crafting |
| Glow Wax Shuffle | `hostiledelight:glowwaxshuffle` | - | **no** | **no** | no row / no | - | Crafting |
| Hawberry Roll | `fruitsdelight:hawberry_roll` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Hawberry Sheet | `fruitsdelight:hawberry_sheet` | ok | **no** | **no** | no / no | - | Crafting, Press + Basin |
| Hawberry Stick | `fruitsdelight:hawberry_stick` | ok | **no** | **no** | no / no | - | Crafting |
| Honey Candy | `create_confectionery:honey_candy` | ok | ok | **no** | no / no | Supp. jar | Mixer |
| Honey Roasted Peanut | `vintagedelight:honey_roasted_peanut` | ok | **no** | **no** | no / no | - | Crafting |
| Mint Candies | `neapolitan:mint_candies` | ok | ok | **no** | no / no | Supp. jar | Crafting |
| Rock Candy | `delightful:rock_candy` | ok | ok | **no** | no / no | Supp. jar | Cooking Pot, Mixer |
| Ruby Chocolate Candy | `create_confectionery:ruby_chocolate_candy` | ok | ok | ok | no / no | Supp. jar | Spout |
| Ruby Chocolate Candy | `create_confectionery:ruby_chocolate_candy_1` | ok | ok | ok | no / no | Supp. jar | Spout |
| Ruby Chocolate Candy | `create_confectionery:ruby_chocolate_candy_2` | ok | ok | ok | no / no | Supp. jar | Spout |
| Ruby Chocolate Candy | `create_confectionery:ruby_chocolate_candy_3` | ok | ok | ok | no / no | Supp. jar | Spout |
| Salmonberry Gummy | `delightful:salmonberry_gummy` | ok | ok | **no** | no / no | Supp. jar | Cooking Pot, Mixer |
| Strawberry Bean Bonbons | `neapolitan:strawberry_bean_bonbons` | ok | ok | **no** | no / no | Supp. jar | Crafting |
| Vanilla Fudge | `neapolitan:vanilla_fudge` | ok | ok | **no** | no / no | Supp. jar | Crafting, Mixer |
| White Chocolate Candy | `create_confectionery:white_chocolate_candy` | ok | ok | ok | no / no | Supp. jar | Spout |
| White Chocolate Candy | `create_confectionery:white_chocolate_candy_1` | ok | ok | ok | no / no | Supp. jar | Spout |
| White Chocolate Candy | `create_confectionery:white_chocolate_candy_2` | ok | ok | ok | no / no | Supp. jar | Spout |
| White Chocolate Candy | `create_confectionery:white_chocolate_candy_3` | ok | ok | ok | no / no | Supp. jar | Spout |

#### Coated fruit (`candy/coated`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Supplementaries jar
- R3 (Recipe shape by station): **Decision:** Spout: coating or filling (chocolate, caramel, honey) over the base

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Caramel Apple | `create_factory:caramel_apple` | ok | ok | ok | no / no | Supp. jar | Spout |
| Caramel Glazed Berries | `create_confectionery:caramel_glazed_berries` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Apple | `create_factory:chocolate_apple` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Glazed Berries | `create:chocolate_glazed_berries` | ok | ok | ok | no / no | Supp. jar | Spout |
| Chocolate Strawberries | `neapolitan:chocolate_strawberries` | ok | ok | ok | no / no | Supp. jar | Crafting, Spout |
| Dark Chocolate Apple | `create_factory:dark_chocolate_apple` | ok | ok | ok | no / no | Supp. jar | Spout |
| Dark Chocolate Glazed Berries | `create_confectionery:black_chocolate_glazed_berries` | ok | ok | ok | no / no | Supp. jar | Spout |
| Honey Glazed Berries | `create_factory:honey_glazed_berries` | ok | ok | ok | no / no | Supp. jar | Spout |
| Honeyed Apple | `create:honeyed_apple` | ok | ok | ok | no / no | Supp. jar | Spout |
| Pear with Rock Sugar | `fruitsdelight:pear_with_rock_sugar` | ok | **no** | **no** | yes / yes | - | Cooking Pot, Mixer |
| Ruby Chocolate Apple | `create_factory:ruby_chocolate_apple` | ok | ok | ok | no / no | Supp. jar | Spout |
| Ruby Chocolate Glazed Berries | `create_confectionery:ruby_chocolate_glazed_berries` | ok | ok | ok | no / no | Supp. jar | Spout |
| White Chocolate Apple | `create_factory:white_chocolate_apple` | ok | ok | ok | no / no | Supp. jar | Spout |
| White Chocolate Glazed Berries | `create_confectionery:white_chocolate_glazed_berries` | ok | ok | ok | no / no | Supp. jar | Spout |

#### Marshmallow (`candy/marshmallow`)

- R1 (Cannable): **Rule:** Per item, can parity 2.24 (sweets) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Supplementaries jar
- R3 (Recipe shape by station): **Decision:** Spout: coating or filling (chocolate, caramel, honey) over the base

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Caramelized Marshmallows on a Stick | `create_confectionery:caramelized_marshmellow_on_a_stick` | ok | **no** | **no** | no / no | - | Campfire / Skillet, Smoker |
| Chocolate Glazed Marshmallow | `create_confectionery:chocolate_glazed_marshmallow` | ok | ok | ok | no / no | Supp. jar | Spout |
| Dark Chocolate Glazed Marshmallow | `create_confectionery:black_chocolate_glazed_marshmallow` | ok | ok | ok | no / no | Supp. jar | Spout |
| Marshmallow | `create_confectionery:marshmallow` | ok | ok | **no** | no / no | Supp. jar | Mixer |
| Marshmallow on a Stick | `delightful:marshmallow_stick` | ok | **no** | **no** | no / no | - | Crafting |
| Marshmallows on a Stick | `create_confectionery:marshmallow_on_a_stick` | ok | **no** | **no** | no / no | - | Crafting |
| Roasted Marshmallow on a Stick | `delightful:cooked_marshmallow_stick` | ok | **no** | **no** | no / no | Food Plate, coaster | Campfire / Skillet |
| Ruby Chocolate Glazed Marshmallow | `create_confectionery:ruby_chocolate_glazed_marshmallow` | ok | ok | ok | no / no | Supp. jar | Spout |
| S'more | `delightful:smore` | ok | **no** | **no** | no / no | Food Plate, coaster | Crafting |
| White Chocolate Glazed Marshmallow | `create_confectionery:white_chocolate_glazed_marshmallow` | ok | ok | ok | no / no | Supp. jar | Spout |

#### Frozen dessert (`frozen`)

No direct members; see the subfamilies.

#### Ice cream (`frozen/ice_cream`)

- R1 (Cannable): **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed (ice cream), Food Plate (popsicles)
- R3 (Recipe shape by station): **Rule:** Every ice food takes `#forge:ice_cubes` ([recipes_cooking.md](recipes_cooking.md) 2.1)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Adzuki Ice Cream | `neapolitan:adzuki_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Banana Ice Cream | `neapolitan:banana_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Chocolate Ice Cream | `neapolitan:chocolate_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Ghastly Ice cream | `hostiledelight:ghastbloodicecream` | - | **no** | **no** | no row / no | - | Crafting |
| Matcha Ice Cream | `delightful:matcha_ice_cream` | ok | ok | ok | no / no | DD placed, coaster | Crafting |
| Mint Ice Cream | `neapolitan:mint_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Neapolitan Ice Cream | `neapolitan:neapolitan_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Sequenced assembly |
| Salmonberry Ice Cream | `delightful:salmonberry_ice_cream` | ok | ok | ok | no / no | DD placed, coaster | Crafting |
| Strawberry Ice Cream | `neapolitan:strawberry_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Spout |
| Truffle Ice Cream | `fungidelight:truffle_ice_cream` | - | **no** | **no** | no row / no | - | Crafting |
| Vanilla Ice Cream | `neapolitan:vanilla_ice_cream` | ok | **no** | ok | no / no | - | Crafting, Spout |

#### Popsicle (`frozen/popsicle`)

- R1 (Cannable): **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed (ice cream), Food Plate (popsicles)
- R3 (Recipe shape by station): **Rule:** Every ice food takes `#forge:ice_cubes` ([recipes_cooking.md](recipes_cooking.md) 2.1)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cantaloupe Popsicle | `delightful:cantaloupe_popsicle` | - | ok | ok | no row / no | Food Plate | Crafting |
| Chocolate Popsicle | `moredelight:chocolate_popsicle` | ok | **no** | ok | no / no | - | Crafting |
| Chorus Fruit Popsicle | `ends_delight:chorus_fruit_popsicle` | - | ok | **no** | no row / no | Food Plate, coaster | Crafting |
| Corn Popsicle | `creativeengineering:corn_popsicle` | - | ok | ok | no row / no | DD placed | Crafting |
| Ender Pearlsicle | `hostiledelight:enderpop` | - | **no** | **no** | no row / no | - | Crafting |
| Kiwi Popsicle | `fruitsdelight:kiwi_popsicle` | ok | **no** | ok | no / no | - | Crafting |
| Melon Popsicle | `farmersdelight:melon_popsicle` | ok | ok | ok | no / no | Food Plate, coaster | Crafting |
| Popsiclime | `hostiledelight:popsiclime` | - | **no** | **no** | no row / no | - | Crafting |

#### Shaved ice (`frozen/shaved_ice`)

- R1 (Cannable): **Rule:** Desserts section x1.25 x 8, + sugar; per item, can parity 2.22 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed (ice cream), Food Plate (popsicles)
- R3 (Recipe shape by station): **Rule:** Every ice food takes `#forge:ice_cubes` ([recipes_cooking.md](recipes_cooking.md) 2.1)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cantaloupe Shaved Ice | `fruitsdelight:hamimelon_shaved_ice` | ok | **no** | ok | no / no | - | Crafting |

#### Sandwich (`sandwich`)

No direct members; see the subfamilies.

#### Square sandwich (`sandwich/square`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Planned:** 2 bread slices (`#forge:bread_slices`, toast counts) in place of bread ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))
- R4 (Recipe shape by station): **Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Bacon Sandwich | `farmersdelight:bacon_sandwich` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Bunfungus Sandwich | `alexsdelight:bunfungus_sandwich` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting |
| Chunkwich | `delightful:chunkwich` | ok | **no** | **no** | **no** | no / no | - | - |
| Egg with Bacon Sandwich | `moredelight:egg_with_bacon_sandwich` | ok | **no** | ok | **no** | no / no | - | Crafting |
| Ham and Cheese Sandwich | `brewinandchewin:ham_and_cheese_sandwich` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Sequenced assembly |
| Nut Butter and Jam Sandwich | `delightful:nut_butter_and_jelly_sandwich` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting |
| Tomato Sandwich | `moredelight:tomato_sandwich` | ok | **no** | ok | **no** | no / no | - | Crafting |
| Zucchini Sandwich | `veggiesdelight:zucchini_sandwich` | ok | **no** | ok | **no** | no / no | - | Crafting |

#### Long roll sandwich (`sandwich/roll`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Planned:** Whole bread (`#forge:bread`), kept ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))
- R4 (Recipe shape by station): **Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Chicken Sandwich | `farmersdelight:chicken_sandwich` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Chicken Sandwich with Egg and Tomato | `moredelight:chicken_sandwich_with_egg_and_tomato` | ok | **no** | ok | **no** | no / no | - | Crafting |
| Crawling Sandwich | `endersdelight:crawling_sandwich` | ok | ok | ok | ok | no / no | Food Plate, coaster | Crafting, Sequenced assembly |
| Egg Sandwich | `farmersdelight:egg_sandwich` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Guardian Sandwich | `hostiledelight:guardiansandwich` | - | **no** | ok | **no** | no row / no | - | Crafting |
| Insect Sandwich | `miners_delight:insect_sandwich` | - | **no** | ok | **no** | no row / no | - | Crafting |
| Porkchop Sandwich | `moredelight:porkchop_sandwich` | ok | **no** | ok | **no** | no / no | - | Crafting |
| Squid Sandwich | `miners_delight:squid_sandwich` | - | ok | ok | **no** | no row / no | Food Plate, coaster | Crafting |
| Steak Sandwich | `moredelight:steak_sandwich` | ok | **no** | ok | **no** | no / no | - | Crafting |
| Syrup Sandwich | `rusticdelight:syrup_sandwich` | - | **no** | ok | **no** | no row / no | - | Crafting |
| Turtle Egg Sandwich | `hostiledelight:turtleeggsandwich` | - | **no** | ok | **no** | no row / no | - | Crafting |

#### Burger (`sandwich/burger`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Rule:** Shaped only: toppings top, patty middle, bread bottom ([recipes_cooking.md](recipes_cooking.md) 2.1)
- R4 (Recipe shape by station): **Rule:** Takes any `#forge:bread` ([recipes_cooking.md](recipes_cooking.md) 2.1)
- R5 (Recipe shape by station): **Rule:** One sequenced assembly each ([recipes_cooking.md](recipes_cooking.md) 2.1)
- R6 (Recipe shape by station): **Planned:** A bun (new item `burger_bun`, oven / smoker / knife on bread) in place of bread; Kangaroo Burger shows a long roll and keeps bread ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))

| Member | Id | R1 | R2 | R3 | R4 | R5 | R6 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|---|---|
| Bison Burger | `alexsdelight:bison_burger` | ok | ok | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Sequenced assembly |
| Cave Hamburger | `miners_delight:cave_hamburger` | - | ok | **no** | ok | **no** | **no** | no row / no | Food Plate, coaster | Crafting |
| Cheese Burger | `vintagedelight:cheese_burger` | ok | **no** | ok | ok | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| Cheeseburger | `delightful:cheeseburger` | ok | ok | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Sequenced assembly |
| Deluxe Cheeseburger | `delightful:deluxe_cheeseburger` | ok | ok | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Sequenced assembly |
| Guardian Burger | `hostiledelight:guardianburger` | - | **no** | ok | ok | **no** | **no** | no row / no | - | Crafting |
| Hamburger | `farmersdelight:hamburger` | ok | ok | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Hamburger with Egg | `moredelight:hamburger_with_egg` | ok | **no** | **no** | ok | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| Kangaroo Burger | `alexsmobs:kangaroo_burger` | ok | **no** | ok | ok | ok | - | no / no | - | Crafting, Sequenced assembly |
| Loaded Hamburger | `moredelight:loaded_hamburger` | ok | **no** | **no** | ok | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| Simple Hamburger | `moredelight:simple_hamburger` | ok | **no** | **no** | ok | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| The Burger | `vintagedelight:deluxe_burger` | ok | **no** | ok | ok | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| Vegan Hamburger | `miners_delight:vegan_hamburger` | - | ok | **no** | ok | **no** | **no** | no row / no | Food Plate, coaster | Crafting |
| Vegetarian Burger | `veggiesdelight:vegetarian_burger` | ok | **no** | **no** | ok | **no** | **no** | no / no | - | Crafting |

#### Wrap (`sandwich/wrap`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Planned:** A tortilla in place of bread (mutton wrap: TODO dishes/meals #2) ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))
- R4 (Recipe shape by station): **Decision:** Shaped crafting (no shapeless) + one sequenced assembly, as the burgers

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Chicken Fajitas Wrap | `veggiesdelight:chicken_fajitas_wrap` | ok | **no** | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| Insect Wrap | `miners_delight:insect_wrap` | - | **no** | **no** | **no** | no row / no | - | Crafting |
| Mutton Wrap | `farmersdelight:mutton_wrap` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Taco | `corn_delight:taco` | ok | ok | ok | **no** | no / no | Food Plate, coaster | Crafting |
| Vegan Wrap | `miners_delight:vegan_wrap` | - | ok | **no** | **no** | no row / no | Food Plate, coaster | Crafting |
| Vegetables Wrap | `veggiesdelight:vegetables_wrap` | ok | **no** | ok | **no** | no / no | - | Crafting, Sequenced assembly |
| Wrapped Cantaloupe | `delightful:wrapped_cantaloupe` | - | ok | **no** | **no** | no row / no | Food Plate | Crafting |

#### Open toast (`sandwich/toast`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Crafting: bread slice or toast + topping
- R4 (Recipe shape by station): **Planned:** Bruschetta: 2 toast in place of bread ([TODO](todo/TODO.md) baking/baked-goods #9, [brief](briefs/baking_baked-goods.md))
- R5 (Other (values, uses)): **Rule:** Every toast eats fast (16 ticks, like dried kelp); CD moredelight/toast.js ([more_delight.md](more_delight.md) 2.3)

| Member | Id | R1 | R2 | R3 | R4 | R5 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|---|
| Caviar Toast | `oceanic_delight:caviar_toast` | - | **no** | ok | - | ok | no row / no | - | Crafting |
| Gongylidia Bruschetta | `alexsdelight:gongylidia_bruschetta` | ok | ok | ok | ok | - | no / no | Food Plate, coaster | Crafting |
| Jelly Bread | `fruitsdelight:jelly_bread` | ok | **no** | ok | - | - | no / no | - | Crafting, Spout |
| Toast with Chocolate | `moredelight:toast_with_chocolate` | ok | **no** | ok | - | ok | no / no | - | Crafting |
| Toast with Egg | `moredelight:toast_with_egg` | ok | **no** | ok | - | ok | no / no | - | Crafting |
| Toast with Glow Berries | `moredelight:toast_with_glow_berries` | ok | **no** | ok | - | ok | no / no | - | Crafting |
| Toast with Honey | `moredelight:toast_with_honey` | ok | **no** | ok | - | ok | no / no | - | Crafting |
| Toast with Sweet Berries | `moredelight:toast_with_sweet_berries` | ok | **no** | ok | - | ok | no / no | - | Crafting |

#### Skewer (`skewer`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Shaped only + one sequenced assembly (as Crispy Skewer, [recipes_cooking.md](recipes_cooking.md) 2.1)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Barbecue on a Stick | `farmersdelight:barbecue_stick` | ok | ok | **no** | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Cave Skewer | `hostiledelight:caveskewer` | - | **no** | **no** | no row / no | - | Crafting |
| Classic Corn Dog | `corn_delight:classic_corn_dog` | ok | ok | **no** | no / no | Food Plate, coaster | Cooking Pot, Crafting, Mixer |
| Corn Dog | `corn_delight:corn_dog` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Crispy Skewer | `endersdelight:crispy_skewer` | ok | ok | ok | no / no | Food Plate, coaster | Crafting, Sequenced assembly |
| Nether Skewer | `nethersdelight:nether_skewer` | ok | ok | **no** | no / no | Food Plate, coaster | Crafting |
| Seafood Skewer | `oceanic_delight:seafood_skewer` | - | **no** | **no** | no row / no | - | Crafting |
| Turnip Mutton Skewer | `veggiesdelight:turnip_mutton_skewer` | ok | **no** | **no** | no / no | - | Crafting |

#### Roll and dumpling (`roll`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate (whole roll), Small Food Plate (slice)
- R3 (Recipe shape by station): **Decision:** Crafting: wrapper + filling

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bat Rolls | `miners_delight:bat_rolls` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Cabbage Rolls | `farmersdelight:cabbage_rolls` | ok | ok | ok | no / no | Food Plate, Small Food Plate, coaster | Cooking Pot, Crafting, Mixer |
| Cabbage Wrapped Elder Guardian | `oceansdelight:cabbage_wrapped_elder_guardian` | - | **no** | ok | no row / no | - | Crafting |
| Calamari Roll | `rusticdelight:calamari_roll` | ok | **no** | ok | no / no | - | Crafting |
| Caviar Roll | `oceanic_delight:fish_egg_roll` | - | **no** | ok | no row / no | - | Crafting |
| Caviar Roll Slice | `oceanic_delight:fish_egg_roll_slice` | - | **no** | **no** | no row / no | - | Cutting Board, Slice and Dice |
| Cod Roll | `farmersdelight:cod_roll` | ok | ok | ok | no / no | Food Plate, Small Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Crab Rangoon | `delightful:crab_rangoon` | ok | **no** | ok | no / no | - | Crafting |
| Dumplings | `farmersdelight:dumplings` | ok | ok | ok | yes / yes | Food Plate, coaster | Cooking Pot, Crafting, Mixer |
| Egg Roll | `oceanic_delight:egg_roll` | - | **no** | ok | no row / no | - | Crafting |
| Fugu Roll | `oceansdelight:fugu_roll` | - | ok | ok | no row / no | Food Plate, Small Food Plate, coaster | Crafting |
| Ghast Roll | `hostiledelight:ghastroll` | - | **no** | **no** | no row / no | - | - |
| Guardian Roll | `hostiledelight:guardianroll` | - | **no** | **no** | no row / no | - | - |
| Kelp Roll | `farmersdelight:kelp_roll` | ok | ok | ok | no / no | Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Kelp Roll Slice | `farmersdelight:kelp_roll_slice` | ok | ok | **no** | no / no | Food Plate, Small Food Plate, coaster | Cutting Board, Mixer, Slice and Dice |
| Roll of Elder Guardian | `oceansdelight:elder_guardian_roll` | - | ok | ok | no row / no | Food Plate, Small Food Plate, coaster | Crafting |
| Salmon Roll | `farmersdelight:salmon_roll` | ok | ok | ok | no / no | Food Plate, Small Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Scarlet Pierogi | `brewinandchewin:scarlet_pierogi` | ok | **no** | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Sea Pickle Roll | `oceanic_delight:sea_pickle_roll` | - | **no** | ok | no row / no | - | Crafting |
| Sea Pickle Roll Slice | `oceanic_delight:sea_pickle_roll_slice` | - | **no** | **no** | no row / no | - | Cutting Board, Slice and Dice |
| Shrimp Roll | `oceanic_delight:shrimp_roll` | - | **no** | ok | no row / no | - | Crafting |
| Spring Rolls | `rusticdelight:spring_rolls` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Stuffed Rice Cake | `ends_delight:stuffed_rice_cake` | - | ok | ok | no row / no | Food Plate, coaster | Crafting |
| Takoyaki | `miners_delight:takoyaki` | - | **no** | **no** | no row / no | DD placed, coaster | Cooking Pot, Mixer |
| Takoyaki | `oceanic_delight:takoyaki` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |

#### Stuffed vegetable (`stuffed`)

- R1 (Cannable): **Rule:** Handhelds never canned (precedent); per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Food Plate
- R3 (Recipe shape by station): **Decision:** Crafting + one sequenced assembly on the vegetable (as Stuffed Potato)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cooked Stuffed Cod | `oceansdelight:cooked_stuffed_cod` | - | ok | **no** | no row / no | Food Plate, coaster | Campfire / Skillet, Furnace, Smoker |
| Garlic Stuffed Mushrooms | `veggiesdelight:garlic_stuffed_mushrooms` | ok | **no** | **no** | no / no | - | Cooking Pot, Crafting, Mixer |
| Stuffed Burrito | `vintagedelight:stuffed_burrito` | ok | **no** | ok | no / no | - | Crafting, Sequenced assembly |
| Stuffed Cod | `oceansdelight:stuffed_cod` | - | **no** | **no** | no row / no | - | Crafting |
| Stuffed Green Bell Pepper | `rusticdelight:stuffed_bell_pepper_green` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Stuffed Morels | `fungidelight:stuffed_morels` | - | **no** | **no** | no row / no | - | Cooking Pot, Crafting, Mixer |
| Stuffed Potato | `farmersdelight:stuffed_potato` | ok | ok | ok | no / no | Food Plate, Small Food Plate, coaster | Crafting, Mixer, Sequenced assembly |
| Stuffed Red Bell Pepper | `rusticdelight:stuffed_bell_pepper_red` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Stuffed Yellow Bell Pepper | `rusticdelight:stuffed_bell_pepper_yellow` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Stuffed Zucchini Boat | `veggiesdelight:stuffed_zucchini_boat` | ok | **no** | **no** | no / no | - | Crafting |
| Stuffed Zucchinis | `veggiesdelight:stuffed_zucchinis` | ok | **no** | **no** | no / no | - | Cooking Pot, Crafting, Mixer |

#### Pizza (`pizza`)

No direct members; see the subfamilies.

#### Whole pizza (`pizza/whole`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Decision:** One shaped recipe + one sequenced assembly (as Brewin' pizza, [recipes_cooking.md](recipes_cooking.md) 2.1)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cheese Pizza | `vintagedelight:cheese_pizza` | - | ok | **no** | no row / no | own block | Crafting |
| Meat Pizza | `vintagedelight:meat_pizza` | - | ok | **no** | no row / no | own block | Crafting |
| Pizza | `brewinandchewin:pizza` | - | ok | **no** | no row / no | own block | Crafting |
| Vegan Pizza | `veggiesdelight:vegan_pizza` | - | ok | **no** | no row / no | own block | Crafting |

#### Pizza slice (`pizza/slice`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Small Food Plate
- R3 (Recipe shape by station): **Decision:** Cutting Board: whole pizza -> slices

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cheese Pizza Slice | `vintagedelight:cheese_pizza_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Meat Pizza Slice | `vintagedelight:meat_pizza_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Pizza Slice | `brewinandchewin:pizza_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |
| Vegan Pizza Slice | `veggiesdelight:vegan_pizza_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |

#### Quiche (`quiche`)

No direct members; see the subfamilies.

#### Whole quiche (`quiche/whole`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Decision:** Shaped recipe on a pie crust + one sequenced assembly on a pie crust (as Brewin' quiche, [recipes_cooking.md](recipes_cooking.md) 2.1)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Quiche | `brewinandchewin:quiche` | - | ok | ok | no row / no | own block | Crafting, Sequenced assembly |
| Zucchini Quiche | `veggiesdelight:zucchini_quiche` | - | ok | **no** | no row / no | own block | Crafting |

#### Quiche slice (`quiche/slice`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Small Food Plate
- R3 (Recipe shape by station): **Decision:** Cutting Board: whole quiche -> slices

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Quiche Slice | `brewinandchewin:quiche_slice` | ok | ok | ok | no / no | Small Food Plate, coaster | Cutting Board, Slice and Dice |
| Zucchini Quiche Slice | `veggiesdelight:zucchini_quiche_slice` | ok | **no** | ok | no / no | - | Cutting Board, Slice and Dice |

#### Lasagna (`lasagna`)

No direct members; see the subfamilies.

#### Lasagna pan (`lasagna/whole`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Decision:** Cooking Pot (Mixer mirrors it)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Lasagna Pan | `veggiesdelight:lasagna_pan` | - | ok | ok | no row / no | own block | Cooking Pot, Mixer |

#### Lasagna serving (`lasagna/serving`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Decision:** Taken from the pan, or Cooking Pot + heatless grid route

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Horror Lasagna | `brewinandchewin:horror_lasagna` | ok | ok | no / no | DD placed, coaster | Cooking Pot, Crafting, Mixer, Sequenced assembly |
| Lasagna Slice | `veggiesdelight:lasagna_slice` | ok | **no** | no / no | - | - |

#### Snack (`snack`)

- R1 (Cannable): **Rule:** Per item, can parity 2.25 ([can_parity.md](can_parity.md))

| Member | Id | R1 | Can | Placeable | Stations |
|---|---|---|---|---|---|
| Caramel Popcorn | `corn_delight:caramel_popcorn` | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Crunchy Bar | `miners_delight:crunchy_bar` | - | no row / no | - | Crafting |
| Nachos | `corn_delight:nachos_bowl` | ok | yes / yes | DD placed, coaster | - |
| Nachos | `corn_delight:nachos_block` | - | no row / yes | own block | Cooking Pot, Mixer |
| Nutritional Bar | `miners_delight:nutritional_bar` | - | no row / no | - | Crafting |
| Popcorn | `corn_delight:popcorn` | ok | no / no | - | Campfire / Skillet, Furnace, Smoker |
| Popcorn Bucket | `corn_delight:popcorn_box` | - | no row / yes | own block | Crafting |
| Shrimp Chips | `oceanic_delight:shrimp_chips` | - | no row / no | - | Cooking Pot, Mixer |
| Tortilla Chip | `corn_delight:tortilla_chip` | ok | yes / yes | - | Cutting Board, Slice and Dice |
| item.endersdelight.steak_fries | `endersdelight:steak_fries` | - | no row / no | DD placed | Cooking Pot, Crafting, Mixer |

#### Salad (`salad`)

- R1 (Cannable): **Rule:** x1.25 x 8 fast servings, effects x0.75, 6 salads or 5 + vinegar; potato and maggot salads not canned (can parity 2.10) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Crafting: raw parts + bowl

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Assorted Salad | `ends_delight:assorted_salad` | - | ok | ok | no row / no | DD placed, coaster | Crafting |
| Bowl of Exotic Fruits | `quarkdelight:bowl_of_exotic_fruits` | ok | **no** | ok | yes / yes | - | Crafting |
| Broccoli Salad | `veggiesdelight:broccoli_salad` | ok | **no** | ok | yes / yes | - | Crafting |
| Candied Salad | `hostiledelight:candiedsalad` | - | **no** | ok | no row / no | - | Crafting |
| Cesar Salad | `veggiesdelight:cesar_salad` | ok | **no** | ok | yes / yes | - | Crafting |
| Chicken Salad | `moredelight:chicken_salad` | ok | **no** | ok | yes / yes | - | Crafting |
| Coleslaw | `veggiesdelight:coleslaw` | ok | **no** | ok | yes / yes | - | Crafting |
| Cucumber Salad | `vintagedelight:cucumber_salad` | ok | **no** | ok | yes / yes | - | Crafting |
| End Mixed Salad | `ends_delight:end_mixed_salad` | - | ok | ok | no row / no | DD placed, coaster | Crafting |
| Field Salad | `delightful:field_salad` | ok | ok | ok | yes / yes | DD placed, coaster | Crafting |
| Fruit Salad | `farmersdelight:fruit_salad` | ok | ok | ok | yes / yes | DD placed, coaster | Crafting, Sequenced assembly |
| Inky Caps Salad | `fungidelight:inky_caps_salad` | **no** | **no** | ok | no row / yes | - | Crafting |
| Lush Moss Salad | `quarkdelight:lush_moss_salad` | ok | **no** | ok | yes / yes | - | Crafting |
| Maggot Salad | `alexsdelight:maggot_salad` | ok | ok | ok | no / no | DD placed, coaster | Crafting |
| Mango Salad | `fruitsdelight:mango_salad` | ok | **no** | ok | yes / yes | - | Crafting |
| Mixed Salad | `farmersdelight:mixed_salad` | ok | ok | ok | yes / yes | DD placed, coaster | Crafting, Mixer |
| Nether Salad | `farmersdelight:nether_salad` | ok | ok | ok | yes / yes | DD placed, coaster | Crafting, Mixer |
| Potato Salad | `moredelight:potato_salad` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Potato Salad | `oceanic_delight:potato_salad` | - | **no** | ok | no row / no | - | Crafting |
| Potato Salad | `rusticdelight:potato_salad` | ok | **no** | ok | no / no | - | Crafting, Mixer |
| Sea Salad | `oceanic_delight:sea_salad` | - | **no** | ok | no row / no | - | Crafting |
| Seagrass Salad | `oceansdelight:seagrass_salad` | - | ok | ok | no row / no | DD placed, coaster | Crafting |
| Squid Salad | `oceanic_delight:squid_salad` | - | **no** | ok | no row / no | - | Crafting |
| Turnip Salad | `veggiesdelight:turnip_salad` | ok | **no** | ok | yes / yes | - | Crafting |

#### Soup (`soup`)

No direct members; see the subfamilies.

#### Soup (`soup/soup`)

- R1 (Cannable): **Rule:** x1.25 x 8 servings, effects x0.75, 6 soups or 3 + salt or broth; 4 skipped (can parity 2.11) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Supplementaries jar: every soup and stew is a jar fluid ([TODO](todo/TODO.md) dishes/ingredients #13)
- R3 (Placeable): **Decision:** Display Delight placed
- R4 (Recipe shape by station): **Rule:** Cooking Pot (Mixer mirrors it) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)
- R5 (Recipe shape by station): **Rule:** Heatless: shapeless `_manual_only` craft with bone broth, one more main ingredient than the pot ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | R4 | R5 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|---|
| Acacia Blossom Soup | `alexsdelight:acacia_blossom_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Aurora soup | `hostiledelight:aurorasoup` | - | ok | **no** | ok | **no** | no row / no | Supp. jar | Cooking Pot, Mixer |
| Bat Soup | `miners_delight:bat_soup` | **no** | ok | **no** | ok | **no** | no row / yes | Supp. jar | Cooking Pot, Mixer |
| Bat Soup Cup | `miners_delight:bat_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Bayberry Juice | `fruitsdelight:bayberry_soup` | ok | ok | **no** | ok | **no** | yes / yes | Supp. jar | Cooking Pot, Mixer, Spout |
| Beetroot Soup | `minecraft:beetroot_soup` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Beetroot Soup Cup | `miners_delight:beetroot_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Bell Pepper Soup | `rusticdelight:bell_pepper_soup` | ok | ok | **no** | ok | **no** | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Bone Broth | `farmersdelight:bone_broth` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Bone Broth Cup | `miners_delight:bone_broth_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Broccoli Soup | `veggiesdelight:broccoli_soup` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Carrot Soup | `moredelight:carrot_soup` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Cauliflower Soup | `veggiesdelight:cauliflower_soup` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Cave Soup | `miners_delight:cave_soup` | **no** | ok | ok | ok | **no** | no row / yes | DD placed, coaster, Supp. jar | Cooking Pot, Mixer |
| Cave Soup Cup | `miners_delight:cave_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Caviar Noodle Soup | `oceanic_delight:fish_egg_noodle_soup` | - | ok | **no** | ok | **no** | no row / no | Supp. jar | Cooking Pot, Mixer |
| Chicken Soup | `farmersdelight:chicken_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Chicken Soup Cup | `miners_delight:chicken_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Corn Soup | `corn_delight:corn_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer, Spout |
| Creamy Morel Soup | `fungidelight:creamy_morel_soup` | **no** | ok | **no** | ok | ok | no row / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Creamy Onion Soup | `brewinandchewin:creamy_onion_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Dragon's Breath and Chorus Soup | `ends_delight:dragon_breath_and_chorus_soup` | - | ok | ok | ok | **no** | no row / no | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Glimmering Chowder | `hostiledelight:glimmeringchowder` | - | ok | **no** | ok | **no** | no row / no | Supp. jar | Cooking Pot, Mixer |
| Guardian Soup | `hostiledelight:guardiansoup` | - | ok | **no** | ok | **no** | no row / no | Supp. jar | Cooking Pot, Mixer |
| Miso with Bamboo Sprouts | `quarkdelight:miso_with_bamboo_sprouts` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Noodle Soup | `farmersdelight:noodle_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Noodle Soup Cup | `miners_delight:noodle_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Oatmeal | `vintagedelight:oatmeal` | ok | ok | **no** | ok | **no** | yes / yes | Supp. jar | Cooking Pot, Mixer |
| Onion Soup Cup | `miners_delight:onion_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Pickle Soup | `vintagedelight:pickle_soup` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Pumpkin Soup | `farmersdelight:pumpkin_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Pumpkin Soup Cup | `miners_delight:pumpkin_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Shulker Soup | `ends_delight:shulker_soup` | - | ok | ok | ok | ok | no row / no | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Sopa De Macaco | `alexsmobs:sopa_de_macaco` | ok | ok | **no** | **no** | - | yes / yes | Supp. jar | Crafting |
| Vegetable Soup | `farmersdelight:vegetable_soup` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Vegetable Soup Cup | `miners_delight:vegetable_soup_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |

#### Stew (`soup/stew`)

- R1 (Cannable): **Rule:** x1.25 x 8 servings, effects x0.75, 6 soups or 3 + salt or broth; 4 skipped (can parity 2.11) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Supplementaries jar: every soup and stew is a jar fluid ([TODO](todo/TODO.md) dishes/ingredients #13)
- R3 (Placeable): **Decision:** Display Delight placed
- R4 (Recipe shape by station): **Rule:** Cooking Pot (Mixer mirrors it) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)
- R5 (Recipe shape by station): **Rule:** Heatless: shapeless `_manual_only` craft with bone broth, one more main ingredient than the pot ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | R4 | R5 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|---|
| Adzuki Stew | `neapolitan:adzuki_stew` | ok | ok | **no** | ok | **no** | yes / yes | Supp. jar | Cooking Pot, Mixer |
| Baked Cod Stew | `farmersdelight:baked_cod_stew` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Baked Cod Stew Cup | `miners_delight:baked_cod_stew_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Beef Stew | `farmersdelight:beef_stew` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Beef Stew Cup | `miners_delight:beef_stew_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Blazing Chili | `farmersrespite:blazing_chili` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Mixer |
| Cacciatore | `veggiesdelight:cacciatore` | ok | ok | **no** | ok | **no** | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Cavern Explorer Stew | `quarkdelight:cavern_explorer_stew` | ok | ok | **no** | ok | ok | no / no | Supp. jar | Cooking Pot, Crafting, Mixer |
| Chorus Stew | `endersdelight:chorus_stew` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Enderman Gristle Stew | `ends_delight:enderman_gristle_stew` | - | ok | ok | ok | ok | no row / no | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Endermite Stew | `endersdelight:endermite_stew` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Fig Chicken Stew | `fruitsdelight:fig_chicken_stew` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Fish Stew | `farmersdelight:fish_stew` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Fish Stew Cup | `miners_delight:fish_stew_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Garlic Chicken Stew | `veggiesdelight:garlic_chicken_stew` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |
| Ghostly Chili | `vintagedelight:ghostly_chili` | ok | ok | **no** | ok | **no** | yes / yes | Supp. jar | Cooking Pot, Mixer |
| Glow Squid Stew | `oceanic_delight:glow_squid_stew` | - | ok | **no** | ok | **no** | no row / no | Supp. jar | Cooking Pot, Mixer |
| Insect Stew | `miners_delight:insect_stew` | **no** | ok | **no** | ok | **no** | no row / yes | Supp. jar | Cooking Pot, Mixer |
| Insect Stew Cup | `miners_delight:insect_stew_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Kangaroo Stew | `alexsdelight:kangaroo_stew` | ok | ok | ok | ok | ok | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Mushroom Stew | `minecraft:mushroom_stew` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Mushroom Stew Cup | `miners_delight:mushroom_stew_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Rabbit Stew | `minecraft:rabbit_stew` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Rabbit Stew Cup | `miners_delight:rabbit_stew_cup` | - | **no** | **no** | **no** | - | no row / no | - | - |
| Shrimp Stew | `oceanic_delight:shrimp_stew` | - | ok | **no** | ok | **no** | no row / no | Supp. jar | Cooking Pot, Mixer |
| Sinigang | `delightful:sinigang` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Crafting, Mixer |
| Slime Stew | `hostiledelight:slimestew` | - | ok | **no** | **no** | - | no row / no | Supp. jar | Crafting |
| Strider Moss Stew | `nethersdelight:strider_moss_stew` | ok | ok | ok | ok | **no** | yes / yes | DD placed, coaster, Supp. jar | Cooking Pot, Mixer |
| Suspicious Stew | `minecraft:suspicious_stew` | **no** | ok | **no** | **no** | - | no / yes | coaster, Supp. jar | fungidelight:effect_stew |
| Turnip Beef Stew | `veggiesdelight:turnip_beef_stew` | ok | ok | **no** | ok | ok | yes / yes | Supp. jar | Cooking Pot, Crafting, Mixer |

#### Bowl meal (`meal`)

No direct members; see the subfamilies.

#### Rice dish (`meal/rice`)

- R1 (Cannable): **Rule:** Pastas only: 2 -> 4 servings of 0.75 (placeholder); per item, can parity 2.12 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Cooking Pot (Mixer mirrors it)
- R4 (Recipe shape by station): **Rule:** Heatless: a pot meal is also a shapeless `_manual_only` craft from cooked forms (one more main) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Bowl Of Paella | `oceanic_delight:bowl_of_paella` | - | **no** | **no** | - | no row / no | - | - |
| Cooked Rice | `farmersdelight:cooked_rice` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Cooked Rice with Beef | `moredelight:cooked_rice_with_beef` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Cooked Rice with Chicken Cuts | `moredelight:cooked_rice_with_chicken_cuts` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Cooked Rice with Porkchop | `moredelight:cooked_rice_with_porkchop` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Ender Bamboo Rice | `ends_delight:ender_bamboo_rice` | - | **no** | ok | ok | no row / no | Food Plate, coaster | Cooking Pot, Crafting, Mixer |
| Ender Paella | `endersdelight:ender_paella` | ok | ok | ok | ok | no / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Fried Rice | `farmersdelight:fried_rice` | ok | ok | ok | ok | no / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Garlic Rice With Cauliflower | `veggiesdelight:garlic_rice_with_cauliflower` | ok | **no** | **no** | - | no / no | - | Crafting |
| Globular Rice | `oceanic_delight:globular_rice` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Mushroom Rice | `farmersdelight:mushroom_rice` | ok | ok | ok | ok | no / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Rice And Vegetables | `veggiesdelight:rice_and_vegetables` | ok | **no** | ok | ok | no / no | - | Cooking Pot, Crafting, Mixer |
| Shrimp Rice | `oceanic_delight:shrimp_rice` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Shrimp-Fried Rice | `alexsmobs:shrimp_fried_rice` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |

#### Pasta and noodles (`meal/pasta`)

- R1 (Cannable): **Rule:** Pastas only: 2 -> 4 servings of 0.75 (placeholder); per item, can parity 2.12 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Cooking Pot (Mixer mirrors it)
- R4 (Recipe shape by station): **Rule:** Heatless: a pot meal is also a shapeless `_manual_only` craft from cooked forms (one more main) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Bell Pepper Pasta | `rusticdelight:bell_pepper_pasta` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Cheese Pasta | `vintagedelight:cheese_pasta` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Crab Pasta | `quarkdelight:crab_pasta` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Creamy Pasta with Chicken Cuts | `moredelight:creamy_pasta_with_chicken_cuts` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Creamy Pasta with Ham | `moredelight:creamy_pasta_with_ham` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Duck Noodles | `abnormals_delight:duck_noodles` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Ender Noodle | `ends_delight:ender_noodle` | - | ok | ok | ok | no row / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Glow Ink Pasta | `miners_delight:glow_ink_pasta` | - | ok | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Mixer |
| Glowing Ink Pasta | `hostiledelight:glowpasta` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Kangaroo Pasta | `alexsdelight:kangaroo_pasta` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Lobster Pasta | `alexsdelight:lobster_pasta` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Pasta With Broccoli | `veggiesdelight:pasta_with_broccoli` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Pasta With Eyeball | `oceanic_delight:pasta_with_eyeball` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Pasta With Veggieballs | `miners_delight:pasta_with_veggieballs` | - | ok | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Mixer |
| Pasta with Cheese | `brewinandchewin:cheesy_pasta` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Pasta with Meatballs | `farmersdelight:pasta_with_meatballs` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Pasta with Mutton Chop | `farmersdelight:pasta_with_mutton_chop` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Pearl Pasta | `endersdelight:pearl_pasta` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Squid Ink Pasta | `farmersdelight:squid_ink_pasta` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Truffle Pasta | `fungidelight:truffle_pasta` | **no** | **no** | ok | ok | no row / yes | - | Cooking Pot, Crafting, Mixer |
| Vegetable Noodles | `farmersdelight:vegetable_noodles` | ok | ok | ok | ok | yes / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |

#### Fried dish (`meal/fried`)

- R1 (Cannable): **Rule:** Pastas only: 2 -> 4 servings of 0.75 (placeholder); per item, can parity 2.12 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Cooking Pot (Mixer mirrors it)
- R4 (Recipe shape by station): **Rule:** Heatless: a pot meal is also a shapeless `_manual_only` craft from cooked forms (one more main) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Chunk Nugget | `delightful:chunk_nugget` | ok | **no** | **no** | - | no / no | - | - |
| Fish And Chips | `veggiesdelight:fish_and_chips` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Fried Calamari | `rusticdelight:fried_calamari` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Fried Chicken | `rusticdelight:fried_chicken` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Fried Mushrooms | `rusticdelight:fried_mushrooms` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Fried Shrimp | `oceanic_delight:fried_shrimp` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Honey Fried Kelp | `oceansdelight:honey_fried_kelp` | - | **no** | ok | **no** | no row / no | Food Plate, coaster | Cooking Pot, Mixer |
| Salmon Tempura | `pneumaticcraft:salmon_tempura` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Squid Rings | `oceansdelight:squid_rings` | - | ok | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Mixer |
| Stir Fried Shulker Meat | `ends_delight:stir_fried_shulker_meat` | - | ok | ok | ok | no row / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |

#### Plated meal (`meal/plate`)

- R1 (Cannable): **Rule:** Pastas only: 2 -> 4 servings of 0.75 (placeholder); per item, can parity 2.12 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Cooking Pot (Mixer mirrors it)
- R4 (Recipe shape by station): **Rule:** Heatless: a pot meal is also a shapeless `_manual_only` craft from cooked forms (one more main) ([recipes_cooking.md](recipes_cooking.md) heatless meal rule)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Adzuki Curry | `neapolitan:adzuki_curry` | ok | **no** | ok | ok | no / no | - | Cooking Pot, Crafting, Mixer |
| Black Cod | `farmersrespite:black_cod` | ok | ok | **no** | - | no / no | DD placed, coaster | Crafting |
| Bowl of Stuffed Shulker | `endersdelight:stuffed_shulker_bowl` | ok | ok | **no** | - | yes / yes | DD placed, coaster | - |
| Braised Sea Pickle | `oceansdelight:braised_sea_pickle` | - | ok | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Mixer |
| Carrotmite | `hostiledelight:carrotmite` | - | **no** | **no** | - | no row / no | - | Crafting |
| Coconut Curry | `delightful:coconut_curry` | ok | **no** | **no** | - | no / no | - | - |
| Coffee-Braised Beef | `rusticdelight:coffee_braised_beef` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Cooked Whole Crab | `quarkdelight:cooked_whole_crab` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Cornbread Stuffing | `corn_delight:cornbread_stuffing` | ok | ok | ok | **no** | yes / yes | DD placed, coaster | Cooking Pot, Mixer |
| Creamed Corn | `corn_delight:creamed_corn` | ok | ok | ok | **no** | yes / yes | DD placed, coaster | Cooking Pot, Mixer |
| Creamy Gnocchi | `veggiesdelight:potato_noodles` | ok | **no** | ok | **no** | yes / yes | - | Cooking Pot, Mixer |
| Diced Potatoes with Beef | `moredelight:diced_potatoes_with_beef` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Diced Potatoes with Chicken Cuts | `moredelight:diced_potatoes_with_chicken_cuts` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Diced Potatoes with Porkchop | `moredelight:diced_potatoes_with_porkchop` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| End Barbecue Stick | `ends_delight:end_barbecue_stick` | - | **no** | **no** | - | no row / no | Food Plate, coaster | Crafting |
| Ender Congee | `ends_delight:ender_congee` | - | ok | ok | ok | no row / no | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Escargot | `abnormals_delight:escargot` | ok | **no** | **no** | - | no / no | Food Plate, coaster | - |
| Fiery Fondue | `brewinandchewin:fiery_fondue` | ok | ok | **no** | - | no / no | DD placed, coaster | - |
| Garlic Baked Cod | `veggiesdelight:garlic_baked_cod` | ok | **no** | **no** | - | no / no | - | Crafting |
| Grilled Salmon | `farmersdelight:grilled_salmon` | ok | ok | **no** | - | no / no | DD placed, coaster | Crafting, Mixer, Sequenced assembly |
| Grilled Strider | `nethersdelight:grilled_strider` | ok | ok | ok | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Guardian Ross | `hostiledelight:guardianross` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Guardian Sashimi | `hostiledelight:guardiansashimi` | - | **no** | **no** | - | no row / no | - | Crafting |
| Improvised Barbecue Stick | `miners_delight:improvised_barbecue_stick` | - | **no** | **no** | - | no row / no | - | Crafting |
| Lychee Chicken | `fruitsdelight:lychee_chicken` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Mashed Potatoes | `moredelight:mashed_potatoes` | **no** | **no** | ok | **no** | no / yes | - | Cooking Pot, Mixer |
| Mashed Potatoes | `veggiesdelight:mashed_potatoes` | **no** | **no** | ok | ok | no / yes | - | Cooking Pot, Crafting, Mixer |
| Mitey Meal | `hostiledelight:miteymeal` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Mutton Chops with Truffle | `fungidelight:mutton_chops_with_truffle` | - | **no** | ok | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Orange Chicken | `fruitsdelight:orange_chicken` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Orange Marinated Pork | `fruitsdelight:orange_marinated_pork` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Overnight Oats | `vintagedelight:overnight_oats` | ok | **no** | **no** | - | no / no | - | Crafting, vintagedelight:fermenting |
| PB & J | `vintagedelight:pb_j` | ok | **no** | **no** | - | no / no | - | Crafting |
| Pad Thai | `vintagedelight:pad_thai` | ok | **no** | ok | ok | yes / yes | - | Cooking Pot, Crafting, Mixer |
| Perch with Mushrooms | `abnormals_delight:perch_with_mushrooms` | ok | ok | ok | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Pike with Beetroot | `abnormals_delight:pike_with_beetroot` | ok | ok | ok | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Pineapple Marinated Pork | `fruitsdelight:pineapple_marinated_pork` | ok | **no** | ok | **no** | no / no | - | Cooking Pot, Mixer |
| Plate Of Stuffed Pufferfish | `oceanic_delight:plate_of_stuffed_pufferfish` | - | **no** | **no** | - | no row / no | - | - |
| Plate of Dragon Leg with Sauce | `ends_delight:dragon_leg_with_sauce` | - | ok | **no** | - | no row / no | DD placed, coaster | - |
| Pork Marsala With Mushrooms | `fungidelight:pork_marsala_with_mushrooms` | - | **no** | ok | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Rabbit with Morels | `fungidelight:rabbit_with_morels` | - | **no** | ok | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Ratatouille | `farmersdelight:ratatouille` | **no** | ok | ok | ok | no / yes | DD placed, coaster | Cooking Pot, Crafting, Mixer |
| Roasted Dragon Steak | `ends_delight:roasted_dragon_steak` | - | ok | **no** | - | no row / no | DD placed, coaster | Crafting |
| Roasted Mutton Chops | `farmersdelight:roasted_mutton_chops` | ok | ok | **no** | - | no / no | DD placed, coaster | Crafting, Mixer, Sequenced assembly |
| Roasted Vegetables | `veggiesdelight:roasted_vegetables` | ok | **no** | ok | ok | no / no | - | Cooking Pot, Crafting, Mixer |
| Seasoned Arthropods | `miners_delight:seasoned_arthropods` | - | **no** | **no** | - | no row / no | - | Crafting |
| Slabdish | `abnormals_delight:slabdish` | ok | ok | **no** | - | no / no | DD placed, coaster | - |
| Spiked Tartare | `hostiledelight:guardiantartar` | - | **no** | **no** | - | no row / no | - | Crafting |
| Squid And Pickles | `oceanic_delight:squid_and_pickles` | - | **no** | **no** | - | no row / no | - | Crafting |
| Steak And Broccoli | `veggiesdelight:steak_and_broccoli` | ok | **no** | **no** | - | no / no | - | Crafting |
| Steak With Mushrooms | `fungidelight:steak_with_mushrooms` | - | **no** | ok | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Steak and Potatoes | `farmersdelight:steak_and_potatoes` | ok | ok | **no** | - | no / no | DD placed, coaster | Crafting, Mixer, Sequenced assembly |
| Steamed Beef | `oceanic_delight:steamed_beef` | - | **no** | ok | **no** | no row / no | - | Cooking Pot, Mixer |
| Tea Curry | `farmersrespite:tea_curry` | ok | ok | ok | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Turnip Cake | `veggiesdelight:turnip_cake` | ok | **no** | ok | ok | no / no | - | Cooking Pot, Crafting, Mixer |
| Vegan Steak And Potatoes | `miners_delight:vegan_steak_and_potatoes` | - | ok | **no** | - | no row / no | DD placed, coaster | Crafting |
| Warped Moldy Meat | `nethersdelight:warped_moldy_meat` | ok | ok | **no** | - | no / no | DD placed, coaster | Crafting |
| Weird Caviar | `miners_delight:weird_caviar` | - | **no** | **no** | - | no row / no | - | Crafting |

#### Feast (`feast`)

No direct members; see the subfamilies.

#### Feast block (`feast/whole`)

- R1 (Cannable): **Rule:** 1 placed feast block -> 6 servings (placeholder; can parity 2.14) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Planned:** Sequenced assembly (roast chicken, shepherd's pie, honey glazed ham...) ([TODO](todo/TODO.md) dishes/meals #5)
- R4 (Recipe shape by station): **Planned:** Cooked in the Oven (hoglin feast first; all feasts in the pot and oven?) ([TODO](todo/TODO.md) dishes/meals #20)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Baklava | `delightful:baklava` | - | ok | **no** | ok | no row / no | own block | Crafting, Oven |
| Bell Pepper Medley | `rusticdelight:bell_pepper_medley` | **no** | ok | **no** | **no** | yes / no | own block | Crafting |
| Cheese Wheel | `vintagedelight:cheese_wheel` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Dark Bell Pepper Medley | `rusticdelight:dark_bell_pepper_medley` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Dragon Meat Stew | `ends_delight:dragon_meat_stew_block` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Crafting, Mixer |
| Fake Meatloaf | `miners_delight:fake_meatloaf` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Mixer |
| Glazed Arachnid Limbs | `miners_delight:glazed_arachnid_limbs` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Gleaming Salad | `farmersdelight:gleaming_salad_block` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Grilled Shulker | `ends_delight:grilled_shulker_block` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Guardian Soup | `oceansdelight:guardian_soup` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Mixer |
| Honey Glazed Ham | `farmersdelight:honey_glazed_ham_block` | - | ok | **no** | **no** | no row / yes | own block | Crafting |
| Pale Bell Pepper Medley | `rusticdelight:pale_bell_pepper_medley` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Pineapple Fried Rice | `fruitsdelight:pineapple_fried_rice` | - | ok | **no** | **no** | no row / yes | own block | Cooking Pot, Mixer |
| Rice Roll Medley | `farmersdelight:rice_roll_medley_block` | - | ok | ok | **no** | no row / no | own block | Crafting, Sequenced assembly |
| Rice Roll Royale | `rusticdelight:rice_roll_royale` | - | ok | **no** | **no** | no row / no | own block | Crafting |
| Roast Chicken | `farmersdelight:roast_chicken_block` | - | ok | **no** | **no** | no row / yes | own block | Crafting |
| Royal Food 1 | `fantasyfurniture:decorations/royal_food_0` | - | ok | **no** | **no** | no row / no | own block | - |
| Royal Food 2 | `fantasyfurniture:decorations/royal_food_1` | - | ok | **no** | **no** | no row / no | own block | - |
| Royal Platter | `fantasyfurniture:decorations/royal_platter` | - | ok | **no** | **no** | no row / no | own block | - |
| Shepherd's Pie | `farmersdelight:shepherds_pie_block` | - | ok | ok | **no** | no row / yes | own block | Crafting, Sequenced assembly |
| Steamed Dragon Egg | `ends_delight:steamed_dragon_egg_block` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Crafting, Mixer |
| Stuffed Bell Peppers | `veggiesdelight:stuffed_bellpeppers_block` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Mixer |
| Stuffed Cantaloupe | `delightful:stuffed_cantaloupe_block` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Mixer |
| Stuffed Pumpkin | `farmersdelight:stuffed_pumpkin_block` | - | ok | **no** | **no** | no row / yes | own block | Cooking Pot, Mixer |
| Stuffed Squid | `miners_delight:stuffed_squid` | - | ok | **no** | **no** | no row / no | own block | Cooking Pot, Mixer |
| Venthyr Food 1 | `fantasyfurniture:decorations/venthyr_food_0` | - | ok | **no** | **no** | no row / no | own block | - |
| Venthyr Food 2 | `fantasyfurniture:decorations/venthyr_food_1` | - | ok | **no** | **no** | no row / no | own block | - |
| Venthyr Platter | `fantasyfurniture:decorations/venthyr_platter` | - | ok | **no** | **no** | no row / no | own block | - |

#### Feast serving (`feast/serving`)

- R1 (Cannable): **Rule:** 1 placed feast block -> 6 servings (placeholder; can parity 2.14) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** none: taken from the feast block with a bowl

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Bowl Of Stuffed Squid | `miners_delight:bowl_of_stuffed_squid` | - | ok | - | no row / no | DD placed, coaster | - |
| Bowl of Dragon Meat Stew | `ends_delight:dragon_meat_stew` | - | ok | - | no row / no | DD placed, coaster | - |
| Bowl of Guardian Soup | `oceansdelight:bowl_of_guardian_soup` | - | ok | - | no row / no | DD placed, coaster | - |
| Bowl of Pineapple Fried Rice | `fruitsdelight:bowl_of_pineapple_fried_rice` | ok | **no** | - | yes / yes | - | - |
| Bowl of Steamed Dragon Egg | `ends_delight:steamed_dragon_egg` | - | ok | - | no row / no | DD placed, coaster | - |
| Bowl of Stuffed Pumpkin | `farmersdelight:stuffed_pumpkin` | ok | ok | - | yes / yes | DD placed, coaster | Mixer |
| Plate Of Fake Meatloaf | `miners_delight:plate_of_fake_meatloaf` | - | ok | - | no row / no | DD placed, coaster | - |
| Plate Of Glazed Arachnid Limbs | `miners_delight:plate_of_glazed_arachnid_limbs` | - | ok | - | no row / no | DD placed, coaster | - |
| Plate of Grilled Shulker | `ends_delight:grilled_shulker` | - | ok | - | no row / no | DD placed, coaster | - |
| Plate of Honey Glazed Ham | `farmersdelight:honey_glazed_ham` | ok | ok | - | yes / yes | DD placed, coaster | Mixer |
| Plate of Roast Chicken | `farmersdelight:roast_chicken` | ok | ok | - | yes / yes | DD placed, coaster | Mixer |
| Plate of Shepherd's Pie | `farmersdelight:shepherds_pie` | ok | ok | - | yes / yes | DD placed, coaster | Mixer |
| Plate of Stuffed Hoglin Ham | `nethersdelight:plate_of_stuffed_hoglin_ham` | ok | ok | - | yes / yes | DD placed, coaster | - |
| Plate of Stuffed Hoglin Roast | `nethersdelight:plate_of_stuffed_hoglin_roast` | **no** | ok | - | yes / no | DD placed, coaster | - |
| Plate of Stuffed Hoglin Snout | `nethersdelight:plate_of_stuffed_hoglin_snout` | **no** | ok | - | yes / no | DD placed, coaster | - |

#### Drink (`drink`)

No direct members; see the subfamilies.

#### Juice (`drink/juice`)

- R1 (Cannable): **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Planned:** Inputs per bottle by hand, Kettle, Create mixing and IE Mixer; machines cheaper than hand ([TODO](todo/TODO.md) food/drinks #8)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Cantaloupe Juice | `fruitsdelight:hamimelon_juice` | ok | **no** | **no** | no / no | - | Crafting, Spout |
| Carrot Juice | `veggiesdelight:carrot_juice` | ok | **no** | **no** | no / no | Supp. jar | Bottling Machine, Cooking Pot, Spout |
| Chorus Juice | `endersdelight:chorus_juice` | ok | ok | **no** | no / no | DD placed, coaster | Bottling Machine, Cooking Pot, Crafting, Spout |
| Dandelion Juice | `veggiesdelight:dandelion_juice` | ok | **no** | **no** | no / no | Supp. jar | Bottling Machine, Cooking Pot, Spout |
| Ender Nectar | `delightful:ender_nectar` | ok | ok | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer |
| Kiwi Juice | `fruitsdelight:kiwi_juice` | ok | **no** | **no** | no / no | - | Crafting, Spout |
| Lemon Juice | `fruitsdelight:lemon_juice` | ok | **no** | **no** | no / no | - | Crafting, Spout |
| Nautilus Juice | `oceanic_delight:nautilus_juice` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Oculemonade | `canned_goods:oculemonade` | ok | **no** | **no** | no / no | - | Crafting |
| Orange Juice | `fruitsdelight:orange_juice` | ok | **no** | **no** | no / no | - | Crafting, Spout |
| Pear Juice | `fruitsdelight:pear_juice` | ok | **no** | **no** | no / no | - | Crafting, Spout |
| Sea Grape Juice | `oceanic_delight:sea_grape_juice` | - | **no** | **no** | no row / no | - | Crafting |

#### Tea (`drink/tea`)

- R1 (Cannable): **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Planned:** Placeable with Display Delight ([TODO](todo/TODO.md) food/drinks #2, placeable #3)
- R3 (Recipe shape by station): **Planned:** Inputs per bottle by hand, Kettle, Create mixing and IE Mixer; machines cheaper than hand ([TODO](todo/TODO.md) food/drinks #8)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Azalea Tea | `delightful:azalea_tea` | ok | **no** | **no** | no / no | - | - |
| Berry Matcha Latte | `delightful:berry_matcha_latte` | ok | ok | **no** | no / no | DD placed, coaster | Crafting, Kettle |
| Black Tea | `farmersrespite:black_tea` | ok | ok | **no** | no / no | DD placed, coaster, Supp. jar | Kettle, Spout |
| Black Tea | `farmersrespite:long_black_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Black Tea | `farmersrespite:strong_black_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Bubble Tea | `ends_delight:bubble_tea` | - | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Kettle, Mixer, Spout |
| Chorus Flower Tea | `ends_delight:chorus_flower_tea` | - | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Kettle, Mixer, Spout |
| Chorus Fruit Milk Tea | `ends_delight:chorus_fruit_milk_tea` | - | ok | **no** | no row / no | DD placed, coaster | Cooking Pot, Kettle, Mixer, Spout |
| Dandelion Tea | `farmersrespite:dandelion_tea` | ok | ok | **no** | no / no | DD placed, coaster, Supp. jar | Kettle, Spout |
| Dandelion Tea | `farmersrespite:long_dandelion_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Green Tea | `farmersrespite:green_tea` | ok | ok | **no** | no / no | DD placed, coaster, Supp. jar | Kettle, Spout |
| Green Tea | `farmersrespite:long_green_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Green Tea | `farmersrespite:strong_green_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Hawberry Tea | `fruitsdelight:hawberry_tea` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer, Spout |
| Lavender Tea | `delightful:lavender_tea` | ok | **no** | **no** | no / no | - | Kettle |
| Lychee Cherry Tea | `fruitsdelight:lychee_cherry_tea` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer, Spout |
| Mango Tea | `fruitsdelight:mango_tea` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer, Spout |
| Mangosteen Tea | `fruitsdelight:mangosteen_tea` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer, Spout |
| Matcha Latte | `delightful:matcha_latte` | ok | ok | **no** | no / no | DD placed, coaster | Kettle |
| Peach Tea | `fruitsdelight:peach_tea` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer, Spout |
| Purulent Tea | `farmersrespite:long_purulent_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Purulent Tea | `farmersrespite:purulent_tea` | ok | ok | **no** | no / no | DD placed, coaster, Supp. jar | Kettle, Spout |
| Purulent Tea | `farmersrespite:strong_purulent_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Rose Hip Tea | `farmersrespite:long_rose_hip_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Rose Hip Tea | `farmersrespite:rose_hip_tea` | ok | ok | **no** | no / no | DD placed, coaster, Supp. jar | Kettle, Spout |
| Rose Hip Tea | `farmersrespite:strong_rose_hip_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Sea Pickle Tea | `hostiledelight:seacucumbertea` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Yellow Tea | `farmersrespite:long_yellow_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Yellow Tea | `farmersrespite:strong_yellow_tea` | ok | **no** | **no** | no / no | coaster | Kettle, Spout |
| Yellow Tea | `farmersrespite:yellow_tea` | ok | ok | **no** | no / no | DD placed, coaster, Supp. jar | Kettle, Spout |

#### Coffee (`drink/coffee`)

- R1 (Cannable): **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Crafting + Cooking Pot (Mixer mirrors it) + Spout from the fluid

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Chocolate Coffee | `rusticdelight:chocolate_coffee` | ok | **no** | **no** | no / no | - | Cooking Pot, Crafting, Kettle, Mixer |
| Cloud Latte | `quarkdelight:cloud_latte` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Coffee | `rusticdelight:coffee` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer |
| Dark Coffee | `rusticdelight:dark_coffee` | ok | **no** | **no** | no / no | - | Cooking Pot, Kettle, Mixer |
| Honey Coffee | `rusticdelight:honey_coffee` | ok | **no** | **no** | no / no | - | Cooking Pot, Crafting, Kettle, Mixer |
| Milk Coffee | `rusticdelight:milk_coffee` | ok | **no** | **no** | no / no | - | Cooking Pot, Crafting, Kettle, Mixer |

#### Milk drink (`drink/milk`)

- R1 (Cannable): **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Crafting + Cooking Pot (Mixer mirrors it) + Spout from the fluid

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Adzuki Milkshake | `neapolitan:adzuki_milkshake` | ok | **no** | ok | no / no | Supp. jar | Crafting, Spout |
| Banana Milkshake | `neapolitan:banana_milkshake` | ok | **no** | ok | no / no | Supp. jar | Crafting, Spout |
| Chocolate Milkshake | `neapolitan:chocolate_milkshake` | ok | **no** | ok | no / no | Supp. jar | Crafting, Spout |
| Hot Chocolate | `create_confectionery:hot_chocolate_bottle` | ok | **no** | **no** | no / no | - | Spout |
| Mango Milkshake | `fruitsdelight:mango_milkshake` | ok | **no** | **no** | no / no | - | Cooking Pot, Mixer |
| Mint Milkshake | `neapolitan:mint_milkshake` | ok | **no** | ok | no / no | Supp. jar | Crafting, Spout |
| Soothing Hot Chocolate | `create_confectionery:soothing_hot_chocolate` | ok | **no** | **no** | no / no | - | Crafting |
| Strawberry Milkshake | `neapolitan:strawberry_milkshake` | ok | **no** | ok | no / no | Supp. jar | Crafting, Spout |
| Vanilla Milkshake | `neapolitan:vanilla_milkshake` | ok | **no** | ok | no / no | Supp. jar | Crafting, Spout |

#### Soda and cider (`drink/soda`)

- R1 (Cannable): **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Apple Cider | `farmersdelight:apple_cider` | ok | ok | no / no | DD placed, coaster | Bottling Machine, Cooking Pot, Kettle, Mixer, Spout |
| Apple Cider | `farmersrespite:long_apple_cider` | ok | **no** | no / no | coaster | Bottling Machine, Kettle, Spout |
| Apple Cider | `farmersrespite:strong_apple_cider` | ok | **no** | no / no | coaster | Bottling Machine, Kettle, Spout |
| Dragon's Breath Soda | `ends_delight:dragon_breath_soda` | - | ok | no row / no | DD placed, coaster | Crafting, Spout |
| Reef-Cola | `canned_goods:reef_cola` | ok | **no** | no / no | - | Crafting |
| Soda | `canned_goods:soda` | ok | **no** | no / no | - | Crafting |
| Soda | `hostiledelight:soda` | - | **no** | no row / no | - | minecraft:blasting |

#### Other drink (`drink/other`)

- R1 (Cannable): **Rule:** 4 drinks -> 4 servings (placeholder); per item, can parity 2.15 ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Display Delight placed
- R3 (Recipe shape by station): **Decision:** Crafting + Cooking Pot (Mixer mirrors it) + Spout from the fluid

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Blazing Ale | `hostiledelight:blazingale` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Creamy Corn Drink | `corn_delight:creamy_corn_drink` | ok | ok | **no** | no / no | DD placed, coaster | Cooking Pot, Mixer, Spout |
| Dragon Tonic | `hostiledelight:dragontonic` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Ghast Brew | `hostiledelight:ghastbrew` | - | **no** | **no** | no row / no | - | minecraft:blasting |
| Honey Bottle | `minecraft:honey_bottle` | ok | **no** | ok | yes / yes | coaster, Supp. jar | Apple Press, Crafting, Keg (pouring), Spout |
| Honey Jar | `vintagedelight:honey_jar` | ok | **no** | **no** | no / no | own block, coaster | Crafting |
| Marine Cocktale | `hostiledelight:marinecocktale` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Nut Milk Bottle | `vintagedelight:nut_milk_bottle` | ok | **no** | **no** | no / no | Supp. jar | Cooking Pot, Mixer |
| Sunset Ade | `hostiledelight:sunsetade` | - | **no** | **no** | no row / no | - | Cooking Pot, Mixer |
| Turnip Water | `veggiesdelight:turnip_water` | ok | **no** | **no** | no / no | Supp. jar | Bottling Machine, Cooking Pot, Spout |

#### Alcohol (`alcohol`)

No direct members; see the subfamilies.

#### Wine (`alcohol/wine`)

- R1 (Cannable): **Rule:** Per item, can parity 2.16 (sheet only, no can section) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Rule:** Own block, while sneaking (Vinery bottles) ([placeable_food.md](placeable_food.md) 1)
- R3 (Recipe shape by station): **Decision:** Fermentation Barrel (grapes: 12 grapes, 4 stomps -> 4 bottles, decided in [TODO](todo/TODO.md) alcohol)
- R4 (Other (values, uses)): **Rule:** Half a rotten shank (1 hunger), no saturation, always drinkable, Intoxication and Nausea ([alcohol.md](alcohol.md) 11)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| A Bottle of 'Creepers Crush' | `vinery:creepers_crush` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| A Bottle of 'Mojang Noir' | `vinery:bottle_mojang_noir` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| A Bottle of 'Villagers Fright' | `vinery:villagers_fright` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Aegis Wine | `vinery:aegis_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Apple Wine | `vinery:apple_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Bolvar Wine | `vinery:bolvar_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Chenet Wine | `vinery:chenet_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Cherry Wine | `vinery:cherry_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Chorus Fruit Wine | `ends_delight:chorus_fruit_wine` | - | - | **no** | **no** | no row / no | DD placed, coaster | Cooking Pot, Mixer, Spout |
| Chorus Wine | `vinery:chorus_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Clark Wine | `vinery:clark_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Cristel Wine | `vinery:cristel_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Eiswein | `vinery:eiswein` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Hard Cider | `vinery:apple_cider` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Jellie Wine | `vinery:jellie_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Kelp Cider | `vinery:kelp_cider` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Magnetic Wine | `vinery:magnetic_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Mellohi Wine | `vinery:mellohi_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Miss Lilitus Wine | `vinery:lilitu_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Noir Wine | `vinery:noir_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Praetorian Wine | `vinery:praetorian_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Red Wine | `vinery:red_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Solaris Wine | `vinery:solaris_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Stal Wine | `vinery:stal_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Strad Wine | `vinery:strad_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Sun-Kissed Wine | `vinery:glowing_wine` | ok | ok | ok | ok | no / no | own block, sneak, coaster | Fermentation Barrel |

#### Brew (`alcohol/brew`)

- R1 (Cannable): **Rule:** Per item, can parity 2.16 (sheet only, no can section) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Coaster
- R3 (Recipe shape by station): **Decision:** Keg, poured into a tankard (Spout mirrors the pour)
- R4 (Other (values, uses)): **Rule:** Half a rotten shank (1 hunger), no saturation, always drinkable, Intoxication and Nausea ([alcohol.md](alcohol.md) 11)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Beer | `brewinandchewin:beer` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Kombucha | `brewinandchewin:kombucha` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Mead | `brewinandchewin:mead` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Pale Jane | `brewinandchewin:pale_jane` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Rice Wine | `brewinandchewin:rice_wine` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Steel-Toe Stout | `brewinandchewin:steel_toe_stout` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Strongroot Ale | `brewinandchewin:strongroot_ale` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |

#### Spirit (`alcohol/spirit`)

- R1 (Cannable): **Rule:** Per item, can parity 2.16 (sheet only, no can section) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Coaster
- R3 (Recipe shape by station): **Decision:** Keg
- R4 (Other (values, uses)): **Rule:** Half a rotten shank (1 hunger), no saturation, always drinkable, Intoxication and Nausea ([alcohol.md](alcohol.md) 11)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Bottle of Wodka | `tmted:bottle_of_wodka` | ok | **no** | **no** | ok | no / no | - | Bottling Machine |
| Red Rum | `brewinandchewin:red_rum` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Saccharine Rum | `brewinandchewin:saccharine_rum` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Vodka | `brewinandchewin:vodka` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |

#### Mixed drink (`alcohol/mixed`)

- R1 (Cannable): **Rule:** Per item, can parity 2.16 (sheet only, no can section) ([can_parity.md](can_parity.md))
- R2 (Placeable): **Decision:** Coaster
- R3 (Recipe shape by station): **Decision:** Keg, from other drinks
- R4 (Other (values, uses)): **Rule:** Half a rotten shank (1 hunger), no saturation, always drinkable, Intoxication and Nausea ([alcohol.md](alcohol.md) 11)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Bellini Cocktail | `fruitsdelight:bellini_cocktail` | ok | **no** | **no** | ok | no / no | - | Cooking Pot, Mixer, Spout |
| Bloody Mary | `brewinandchewin:bloody_mary` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Dread Nog | `brewinandchewin:dread_nog` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Egg Grog | `brewinandchewin:egg_grog` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Glittering Grenadine | `brewinandchewin:glittering_grenadine` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Jo's Special Mixture | `vinery:jo_special_mixture` | ok | ok | **no** | ok | no / no | own block, sneak, coaster | Fermentation Barrel |
| Salty Folly | `brewinandchewin:salty_folly` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |
| Withering Dross | `brewinandchewin:withering_dross` | ok | ok | ok | ok | no / no | coaster | Keg, Keg (pouring), Spout |

#### Golden food (`golden`)

- R1 (Cannable): **Rule:** 2 golden + honey -> 10 servings at x0.25 (effects x0.333 decided, not built; can parity 2.17) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Crafting: the food in 8 gold (real gold only, rose gold counts); every golden food but the apple takes 8 gold nuggets (owner 2026-11-02, [food.md](food.md) 3.16; was 8 gold ingots) ([recipes_cooking.md](recipes_cooking.md) 2.4)
- R3 (Recipe shape by station): **Decision:** Heated Mixer: the food + 6 gold, and Arc Furnace: the food + 5 gold, for every golden food (stated only for carrot and apple, [recipes_cooking.md](recipes_cooking.md) 2.4; TODO dishes/meals #9 built it on every gold tag)

| Member | Id | R1 | R2 | R3 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|
| Glistering Melon Slice | `minecraft:glistering_melon_slice` | **no** | ok | **no** | no row / yes | coaster | Crafting |
| Golden Apple | `minecraft:golden_apple` | ok | ok | ok | yes / yes | coaster, Supp. jar | Arc Furnace, Crafting, Disenchanter, Mixer |
| Golden Baked Potato | `goldenfood:golden_baked_potato` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Beetroot | `goldenfood:golden_beetroot` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Beetroot Soup | `goldenfood:golden_beetroot_soup` | ok | ok | **no** | yes / yes | Supp. jar | Crafting |
| Golden Bread | `goldenfood:golden_bread` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cake | `goldenfood:golden_cake` | - | ok | **no** | no row / no | own block | Crafting |
| Golden Carrot | `minecraft:golden_carrot` | ok | ok | ok | yes / yes | coaster | Arc Furnace, Crafting, Mixer |
| Golden Chorus Fruit | `goldenfood:golden_chorus_fruit` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Beef | `goldenfood:golden_cooked_beef` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Chicken | `goldenfood:golden_cooked_chicken` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Cod | `goldenfood:golden_cooked_cod` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Mutton | `goldenfood:golden_cooked_mutton` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Porkchop | `goldenfood:golden_cooked_porkchop` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Rabbit | `goldenfood:golden_cooked_rabbit` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cooked Salmon | `goldenfood:golden_cooked_salmon` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Cookie | `goldenfood:golden_cookie` | ok | ok | **no** | yes / yes | Supp. jar | Crafting |
| Golden Dried Kelp | `goldenfood:golden_dried_kelp` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Glow Berries | `goldenfood:golden_glow_berries` | ok | ok | **no** | yes / yes | Supp. jar | Crafting |
| Golden Honey Bottle | `goldenfood:golden_honey_bottle` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Melon Slice | `goldenfood:golden_melon_slice` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Milk Bucket | `goldenfood:golden_milk_bucket` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Moss Clump | `immersive_weathering:golden_moss_clump` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Mushroom Stew | `goldenfood:golden_mushroom_stew` | ok | ok | **no** | yes / yes | Supp. jar | Crafting |
| Golden Nutritional Bar | `miners_delight:golden_nutritional_bar` | **no** | ok | **no** | no row / yes | - | Crafting |
| Golden Pumpkin Pie | `goldenfood:golden_pumpkin_pie` | ok | ok | **no** | yes / yes | - | Crafting |
| Golden Rabbit Stew | `goldenfood:golden_rabbit_stew` | ok | ok | **no** | yes / yes | Supp. jar | Crafting |
| Golden Sweet Berries | `goldenfood:golden_sweet_berries` | ok | ok | **no** | yes / yes | Supp. jar | Crafting |

#### Enchanted golden food (`golden/enchanted`)

- R1 (Cannable): **Rule:** 2 golden + honey -> 10 servings at x0.25 (effects x0.333 decided, not built; can parity 2.17) ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Rule:** Gilded (enchanted golden) apple: sequenced assembly on a golden apple, 8 loops of 1 gold ingot, 125 mB liquid XP, pressing (owner 2026-11-03; was 4 loops of a gold block and 250 mB) ([recipes_cooking.md](recipes_cooking.md) 2.4)
- R3 (Recipe shape by station): **Rule:** Every other enchanted golden food but the cake: the same assembly on its golden form; the anvil and the Golden Food Enchantment are removed (owner 2026-11-02 ([golden_food.md](golden_food.md) 3.4))
- R4 (Recipe shape by station): **Rule:** Enchanted golden cake: no recipe (upstream had none) ([golden_food.md](golden_food.md) 3.4)

| Member | Id | R1 | R2 | R3 | R4 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|---|---|
| Enchanted Golden Apple | `minecraft:enchanted_golden_apple` | ok | ok | - | - | yes / yes | coaster, Supp. jar | Sequenced assembly |
| Enchanted Golden Baked Potato | `goldenfood:enchanted_golden_baked_potato` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Beetroot | `goldenfood:enchanted_golden_beetroot` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Beetroot Soup | `goldenfood:enchanted_golden_beetroot_soup` | ok | - | **no** | - | yes / yes | Supp. jar | - |
| Enchanted Golden Bread | `goldenfood:enchanted_golden_bread` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cake | `goldenfood:enchanted_golden_cake` | - | - | - | ok | no row / no | own block | - |
| Enchanted Golden Carrot | `goldenfood:enchanted_golden_carrot` | ok | - | **no** | - | yes / yes | coaster | - |
| Enchanted Golden Chorus Fruit | `goldenfood:enchanted_golden_chorus_fruit` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Beef | `goldenfood:enchanted_golden_cooked_beef` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Chicken | `goldenfood:enchanted_golden_cooked_chicken` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Cod | `goldenfood:enchanted_golden_cooked_cod` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Mutton | `goldenfood:enchanted_golden_cooked_mutton` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Porkchop | `goldenfood:enchanted_golden_cooked_porkchop` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Rabbit | `goldenfood:enchanted_golden_cooked_rabbit` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cooked Salmon | `goldenfood:enchanted_golden_cooked_salmon` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Cookie | `goldenfood:enchanted_golden_cookie` | ok | - | **no** | - | yes / yes | Supp. jar | - |
| Enchanted Golden Dried Kelp | `goldenfood:enchanted_golden_dried_kelp` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Glow Berries | `goldenfood:enchanted_golden_glow_berries` | ok | - | **no** | - | yes / yes | Supp. jar | - |
| Enchanted Golden Honey Bottle | `goldenfood:enchanted_golden_honey_bottle` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Melon Slice | `goldenfood:enchanted_golden_melon_slice` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Milk Bucket | `goldenfood:enchanted_golden_milk_bucket` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Moss Clump | `immersive_weathering:enchanted_golden_moss_clump` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Mushroom Stew | `goldenfood:enchanted_golden_mushroom_stew` | ok | - | **no** | - | yes / yes | Supp. jar | - |
| Enchanted Golden Pumpkin Pie | `goldenfood:enchanted_golden_pumpkin_pie` | ok | - | **no** | - | yes / yes | - | - |
| Enchanted Golden Rabbit Stew | `goldenfood:enchanted_golden_rabbit_stew` | ok | - | **no** | - | yes / yes | Supp. jar | - |
| Enchanted Golden Sweet Berries | `goldenfood:enchanted_golden_sweet_berries` | ok | - | **no** | - | yes / yes | Supp. jar | - |

#### Can (`can`)

- R1 (Recipe shape by station): **Rule:** Recipes that use a can are manual only: `_manual_only` id (NCH `cans` argument; Create and auto-crafters skip them) ([can_parity.md](can_parity.md) 8, 2026-10-21)

| Member | Id | R1 | Can | Placeable | Stations |
|---|---|---|---|---|---|
| Canned Acacia Blossom Soup | `creativeengineering:canned_acacia_blossom_soup` | - | no row / no | - | Crafting |
| Canned Adzuki Bun | `creativeengineering:canned_adzuki_bun` | - | no row / no | - | Crafting |
| Canned Adzuki Stew | `creativeengineering:canned_adzuki_stew` | - | no row / no | - | Crafting |
| Canned Ancient Fruit | `creativeengineering:canned_ancient_fruit` | - | no row / no | - | Crafting |
| Canned Apple | `creativeengineering:canned_apple` | - | no row / no | - | Crafting |
| Canned Apple Pie Filling | `creativeengineering:canned_apple_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Apples | `canned_goods:canned_apples` | - | no row / no | - | - |
| Canned Baked Cod Stew | `creativeengineering:canned_baked_cod_stew` | - | no row / no | - | Crafting |
| Canned Baked Durian | `creativeengineering:canned_baked_durian` | - | no row / no | - | Crafting |
| Canned Baked Pear | `creativeengineering:canned_baked_pear` | - | no row / no | - | Crafting |
| Canned Baked Potato | `canned_goods:canned_baked_potato` | - | no row / no | - | Crafting |
| Canned Baked Sweet Potato | `creativeengineering:canned_baked_sweet_potato` | - | no row / no | - | Crafting |
| Canned Banana | `creativeengineering:canned_banana` | - | no row / no | - | Crafting |
| Canned Banana Bread | `creativeengineering:canned_banana_bread` | - | no row / no | - | Crafting |
| Canned Bayberries | `creativeengineering:canned_bayberry` | - | no row / no | - | Crafting |
| Canned Bayberry Cookies | `creativeengineering:canned_bayberry_cookie` | - | no row / no | - | Crafting |
| Canned Bayberry Juice | `creativeengineering:canned_bayberry_soup` | - | no row / no | - | Crafting |
| Canned Beef Stew | `creativeengineering:canned_beef_stew` | - | no row / no | - | Crafting |
| Canned Beetroot | `creativeengineering:canned_beetroot` | - | no row / no | - | Crafting |
| Canned Beetroot Soup | `creativeengineering:canned_ce_beetroot_soup` | - | no row / no | - | Crafting |
| Canned Beetroots | `canned_goods:canned_beets` | - | no row / no | - | - |
| Canned Bell Pepper | `creativeengineering:canned_bell_pepper` | - | no row / no | - | Crafting |
| Canned Bell Pepper Pasta | `creativeengineering:canned_bell_pepper_pasta` | - | no row / no | - | Crafting |
| Canned Bell Pepper Soup | `creativeengineering:canned_bell_pepper_soup` | - | no row / no | - | Crafting |
| Canned Blazing Chili | `creativeengineering:canned_blazing_chili` | - | no row / no | - | Crafting |
| Canned Blobfish | `creativeengineering:canned_blobfish` | - | no row / no | - | Crafting |
| Canned Blueberries | `creativeengineering:canned_blueberry` | - | no row / no | - | Crafting |
| Canned Boiled Blue Pickerelweed | `creativeengineering:canned_boiled_blue_pickerelweed` | - | no row / no | - | Crafting |
| Canned Boiled Corn | `creativeengineering:canned_boiled_corn` | - | no row / no | - | Crafting |
| Canned Boiled Purple Pickerelweed | `creativeengineering:canned_boiled_purple_pickerelweed` | - | no row / no | - | Crafting |
| Canned Bone Broth | `creativeengineering:canned_bone_broth` | - | no row / no | - | Crafting |
| Canned Bread | `canned_goods:canned_beetroot_soup` | - | no row / no | - | - |
| Canned Bread | `canned_goods:canned_bread` | - | no row / no | - | Crafting |
| Canned Broccoli | `creativeengineering:canned_broccoli` | - | no row / no | - | Crafting |
| Canned Broccoli Salad | `creativeengineering:canned_broccoli_salad` | - | no row / no | - | Crafting |
| Canned Broccoli Soup | `creativeengineering:canned_broccoli_soup` | - | no row / no | - | Crafting |
| Canned Cabbage | `creativeengineering:canned_cabbage` | - | no row / no | - | Crafting |
| Canned Cacciatore | `creativeengineering:canned_cacciatore` | - | no row / no | - | Crafting |
| Canned Cactus Flesh | `creativeengineering:canned_cactus_flesh` | - | no row / no | - | Crafting |
| Canned Cactus Steak | `creativeengineering:canned_cooked_cactus_steak` | - | no row / no | - | Crafting |
| Canned Cake | `canned_goods:canned_cake` | - | no row / no | - | Crafting |
| Canned Cantaloupe | `creativeengineering:canned_cantaloupe` | - | no row / no | - | Crafting |
| Canned Caramel Popcorn | `creativeengineering:canned_caramel_popcorn` | - | no row / no | - | Crafting |
| Canned Carrot | `creativeengineering:canned_carrot` | - | no row / no | - | Crafting |
| Canned Carrot Soup | `creativeengineering:canned_carrot_soup` | - | no row / no | - | Crafting |
| Canned Cauliflower | `creativeengineering:canned_cauliflower` | - | no row / no | - | Crafting |
| Canned Cauliflower Bread | `creativeengineering:canned_cauliflower_bread` | - | no row / no | - | Crafting |
| Canned Cauliflower Soup | `creativeengineering:canned_cauliflower_soup` | - | no row / no | - | Crafting |
| Canned Cesar Salad | `creativeengineering:canned_cesar_salad` | - | no row / no | - | Crafting |
| Canned Cheesy Pasta | `creativeengineering:canned_cheesy_pasta` | - | no row / no | - | Crafting |
| Canned Cherries | `creativeengineering:canned_cherry` | - | no row / no | - | Crafting |
| Canned Cherry Cookies | `creativeengineering:canned_cherry_cookie` | - | no row / no | - | Crafting |
| Canned Chicken Salad | `creativeengineering:canned_chicken_salad` | - | no row / no | - | Crafting |
| Canned Chicken Soup | `creativeengineering:canned_chicken_soup` | - | no row / no | - | Crafting |
| Canned Chips | `creativeengineering:canned_chips` | - | no row / no | - | Crafting |
| Canned Chocolate Pie Filling | `creativeengineering:canned_chocolate_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Chorus Fruit | `canned_goods:canned_chorus_fruits` | - | no row / no | - | Crafting |
| Canned Chorus Pie Filling | `creativeengineering:canned_chorus_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Chorus Stew | `creativeengineering:canned_chorus_stew` | - | no row / no | - | Crafting |
| Canned Coffee Berries | `creativeengineering:canned_coffee_berries` | - | no row / no | - | Crafting |
| Canned Coleslaw | `creativeengineering:canned_coleslaw` | - | no row / no | - | Crafting |
| Canned Cooked Calamari | `creativeengineering:canned_cooked_calamari` | - | no row / no | - | Crafting |
| Canned Cooked Catfish | `creativeengineering:canned_cooked_catfish` | - | no row / no | - | Crafting |
| Canned Cooked Chicken | `canned_goods:canned_chicken` | - | no row / no | - | Crafting |
| Canned Cooked Cod | `canned_goods:canned_fish` | - | no row / no | - | Crafting |
| Canned Cooked Lionfish | `creativeengineering:canned_cooked_lionfish` | - | no row / no | - | Crafting |
| Canned Cooked Mutten | `canned_goods:canned_cooked_mutten` | - | no row / no | - | Crafting |
| Canned Cooked Perch | `creativeengineering:canned_cooked_perch` | - | no row / no | - | Crafting |
| Canned Cooked Pike | `creativeengineering:canned_cooked_pike` | - | no row / no | - | Crafting |
| Canned Cooked Pork | `canned_goods:canned_pork` | - | no row / no | - | Crafting |
| Canned Cooked Rabbit | `canned_goods:canned_cooked_rabbit` | - | no row / no | - | Crafting |
| Canned Cooked Rice | `creativeengineering:canned_cooked_rice` | - | no row / no | - | Crafting |
| Canned Cooked Salmon | `canned_goods:canned_cooked_salmon` | - | no row / no | - | Crafting |
| Canned Cookies | `canned_goods:canned_cookies` | - | no row / no | - | Crafting |
| Canned Corn | `creativeengineering:canned_corn` | - | no row / no | - | Crafting |
| Canned Corn Soup | `creativeengineering:canned_corn_soup` | - | no row / no | - | Crafting |
| Canned Cornbread | `creativeengineering:canned_cornbread` | - | no row / no | - | Crafting |
| Canned Cornbread Stuffing | `creativeengineering:canned_cornbread_stuffing` | - | no row / no | - | Crafting |
| Canned Crab Pasta | `creativeengineering:canned_crab_pasta` | - | no row / no | - | Crafting |
| Canned Cranberries | `creativeengineering:canned_cranberry` | - | no row / no | - | Crafting |
| Canned Cranberry Cookies | `creativeengineering:canned_cranberry_cookie` | - | no row / no | - | Crafting |
| Canned Creamed Corn | `creativeengineering:canned_creamed_corn` | - | no row / no | - | Crafting |
| Canned Creamy Gnocchi | `creativeengineering:canned_potato_noodles` | - | no row / no | - | Crafting |
| Canned Creamy Onion Soup | `creativeengineering:canned_creamy_onion_soup` | - | no row / no | - | Crafting |
| Canned Creamy Pasta with Chicken | `creativeengineering:canned_creamy_chicken_pasta` | - | no row / no | - | Crafting |
| Canned Creamy Pasta with Ham | `creativeengineering:canned_creamy_ham_pasta` | - | no row / no | - | Crafting |
| Canned Dried Banana | `creativeengineering:canned_dried_banana` | - | no row / no | - | Crafting |
| Canned Dried Kelp | `canned_goods:canned_dried_kelp` | - | no row / no | - | Crafting |
| Canned Dried Persimmon | `creativeengineering:canned_dried_persimmon` | - | no row / no | - | Crafting |
| Canned Dumplings | `creativeengineering:canned_dumplings` | - | no row / no | - | Crafting |
| Canned Durian | `creativeengineering:canned_durian` | - | no row / no | - | Crafting |
| Canned Durian Pie Filling | `creativeengineering:canned_durian_pie_filling` | - | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Enchanted Golden Apple | `creativeengineering:canned_enchanted_golden_apple` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Baked Potato | `creativeengineering:canned_enchanted_golden_baked_potato` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Beetroot | `creativeengineering:canned_enchanted_golden_beetroot` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Beetroot Soup | `creativeengineering:canned_enchanted_golden_beetroot_soup` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Bread | `creativeengineering:canned_enchanted_golden_bread` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Carrot | `creativeengineering:canned_enchanted_golden_carrot` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Chorus Fruit | `creativeengineering:canned_enchanted_golden_chorus_fruit` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Beef | `creativeengineering:canned_enchanted_golden_cooked_beef` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Chicken | `creativeengineering:canned_enchanted_golden_cooked_chicken` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Cod | `creativeengineering:canned_enchanted_golden_cooked_cod` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Mutton | `creativeengineering:canned_enchanted_golden_cooked_mutton` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Porkchop | `creativeengineering:canned_enchanted_golden_cooked_porkchop` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Rabbit | `creativeengineering:canned_enchanted_golden_cooked_rabbit` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cooked Salmon | `creativeengineering:canned_enchanted_golden_cooked_salmon` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Cookie | `creativeengineering:canned_enchanted_golden_cookie` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Dried Kelp | `creativeengineering:canned_enchanted_golden_dried_kelp` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Glow Berries | `creativeengineering:canned_enchanted_golden_glow_berries` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Honey Bottle | `creativeengineering:canned_enchanted_golden_honey_bottle` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Melon Slice | `creativeengineering:canned_enchanted_golden_melon_slice` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Milk Bucket | `creativeengineering:canned_enchanted_golden_milk_bucket` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Moss Clump | `creativeengineering:canned_enchanted_golden_moss_clump` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Mushroom Stew | `creativeengineering:canned_enchanted_golden_mushroom_stew` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Pumpkin Pie | `creativeengineering:canned_enchanted_golden_pumpkin_pie` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Rabbit Stew | `creativeengineering:canned_enchanted_golden_rabbit_stew` | - | no row / no | - | Crafting |
| Canned Enchanted Golden Sweet Berries | `creativeengineering:canned_enchanted_golden_sweet_berries` | - | no row / no | - | Crafting |
| Canned Endermite Stew | `creativeengineering:canned_endermite_stew` | - | no row / no | - | Crafting |
| Canned Exotic Fruits | `creativeengineering:canned_bowl_of_exotic_fruits` | - | no row / no | - | Crafting |
| Canned Field Salad | `creativeengineering:canned_field_salad` | - | no row / no | - | Crafting |
| Canned Fig Chicken Stew | `creativeengineering:canned_fig_chicken_stew` | - | no row / no | - | Crafting |
| Canned Fig Pie Filling | `creativeengineering:canned_fig_pie_filling` | - | no row / no | - | Crafting |
| Canned Figs | `creativeengineering:canned_fig` | - | no row / no | - | Crafting |
| Canned Fish Stew | `creativeengineering:canned_fish_stew` | - | no row / no | - | Crafting |
| Canned Flaxen Cheese | `creativeengineering:canned_flaxen_cheese` | - | no row / no | - | Cooking Pot, Mixer |
| Canned Flying Fish | `creativeengineering:canned_flying_fish` | - | no row / no | - | Crafting |
| Canned Fruit Salad | `creativeengineering:canned_fruit_salad` | - | no row / no | - | Crafting |
| Canned Garlic Bread | `creativeengineering:canned_garlic_bread` | - | no row / no | - | Crafting |
| Canned Garlic Chicken Stew | `creativeengineering:canned_garlic_chicken_stew` | - | no row / no | - | Crafting |
| Canned Glistering Melon Slice | `creativeengineering:canned_glistering_melon_slice` | - | no row / no | - | Crafting |
| Canned Glow Berries | `canned_goods:canned_glow_berries` | - | no row / no | - | - |
| Canned Glow Berries | `creativeengineering:canned_glow_berry` | - | no row / no | - | Crafting |
| Canned Golden Apple | `creativeengineering:canned_golden_apple` | - | no row / no | - | Crafting |
| Canned Golden Apples | `canned_goods:canned_golden_apples` | - | no row / no | - | - |
| Canned Golden Baked Potato | `creativeengineering:canned_golden_baked_potato` | - | no row / no | - | Crafting |
| Canned Golden Beetroot | `creativeengineering:canned_golden_beetroot` | - | no row / no | - | Crafting |
| Canned Golden Beetroot Soup | `creativeengineering:canned_golden_beetroot_soup` | - | no row / no | - | Crafting |
| Canned Golden Bread | `creativeengineering:canned_golden_bread` | - | no row / no | - | Crafting |
| Canned Golden Carrot | `creativeengineering:canned_ce_golden_carrot` | - | no row / no | - | Crafting |
| Canned Golden Carrots | `canned_goods:canned_golden_carrot` | - | no row / no | - | - |
| Canned Golden Carrots | `canned_goods:canned_pumpkin_pie` | - | no row / no | - | - |
| Canned Golden Chorus Fruit | `creativeengineering:canned_golden_chorus_fruit` | - | no row / no | - | Crafting |
| Canned Golden Cooked Beef | `creativeengineering:canned_golden_cooked_beef` | - | no row / no | - | Crafting |
| Canned Golden Cooked Chicken | `creativeengineering:canned_golden_cooked_chicken` | - | no row / no | - | Crafting |
| Canned Golden Cooked Cod | `creativeengineering:canned_golden_cooked_cod` | - | no row / no | - | Crafting |
| Canned Golden Cooked Mutton | `creativeengineering:canned_golden_cooked_mutton` | - | no row / no | - | Crafting |
| Canned Golden Cooked Porkchop | `creativeengineering:canned_golden_cooked_porkchop` | - | no row / no | - | Crafting |
| Canned Golden Cooked Rabbit | `creativeengineering:canned_golden_cooked_rabbit` | - | no row / no | - | Crafting |
| Canned Golden Cooked Salmon | `creativeengineering:canned_golden_cooked_salmon` | - | no row / no | - | Crafting |
| Canned Golden Cookie | `creativeengineering:canned_golden_cookie` | - | no row / no | - | Crafting |
| Canned Golden Dried Kelp | `creativeengineering:canned_golden_dried_kelp` | - | no row / no | - | Crafting |
| Canned Golden Glow Berries | `creativeengineering:canned_golden_glow_berries` | - | no row / no | - | Crafting |
| Canned Golden Honey Bottle | `creativeengineering:canned_golden_honey_bottle` | - | no row / no | - | Crafting |
| Canned Golden Melon Slice | `creativeengineering:canned_golden_melon_slice` | - | no row / no | - | Crafting |
| Canned Golden Milk Bucket | `creativeengineering:canned_golden_milk_bucket` | - | no row / no | - | Crafting |
| Canned Golden Moss Clump | `creativeengineering:canned_golden_moss_clump` | - | no row / no | - | Crafting |
| Canned Golden Mushroom Stew | `creativeengineering:canned_golden_mushroom_stew` | - | no row / no | - | Crafting |
| Canned Golden Pumpkin Pie | `creativeengineering:canned_golden_pumpkin_pie` | - | no row / no | - | Crafting |
| Canned Golden Rabbit Stew | `creativeengineering:canned_golden_rabbit_stew` | - | no row / no | - | Crafting |
| Canned Golden Sweet Berries | `creativeengineering:canned_golden_sweet_berries` | - | no row / no | - | Crafting |
| Canned Gongylidia | `creativeengineering:canned_gongylidia` | - | no row / no | - | Crafting |
| Canned Green Tea Cookies | `creativeengineering:canned_green_tea_cookie` | - | no row / no | - | Crafting |
| Canned Grilled Corn | `creativeengineering:canned_grilled_corn` | - | no row / no | - | Crafting |
| Canned Hawberries | `creativeengineering:canned_hawberry` | - | no row / no | - | Crafting |
| Canned Hoglin Feast | `creativeengineering:canned_hoglin_feast` | - | no row / no | - | Crafting |
| Canned Honey Bottles | `canned_goods:canned_honey_bottles` | - | no row / no | - | Crafting |
| Canned Honey Bread | `creativeengineering:canned_honey_bread` | - | no row / no | - | Crafting |
| Canned Honey Cookies | `creativeengineering:canned_honey_cookie` | - | no row / no | - | Crafting |
| Canned Honey Glazed Ham | `creativeengineering:canned_honey_glazed_ham` | - | no row / no | - | Crafting |
| Canned Honey Glazed Porkchop | `creativeengineering:canned_glazed_porkchop` | - | no row / no | - | Crafting |
| Canned Jerky | `creativeengineering:canned_jerky` | - | no row / no | - | Crafting |
| Canned Kangaroo Pasta | `creativeengineering:canned_kangaroo_pasta` | - | no row / no | - | Crafting |
| Canned Kangaroo Stew | `creativeengineering:canned_kangaroo_stew` | - | no row / no | - | Crafting |
| Canned Kimchi | `creativeengineering:canned_kimchi` | - | no row / no | - | Crafting |
| Canned Kiwi | `creativeengineering:canned_kiwi` | - | no row / no | - | Crafting |
| Canned Lemon | `creativeengineering:canned_lemon` | - | no row / no | - | Crafting |
| Canned Lemon Cookies | `creativeengineering:canned_lemon_cookie` | - | no row / no | - | Crafting |
| Canned Lemon Pie Filling | `creativeengineering:canned_lemon_pie_filling` | - | no row / no | - | Crafting |
| Canned Lobster Pasta | `creativeengineering:canned_lobster_pasta` | - | no row / no | - | Crafting |
| Canned Lobster Tail | `creativeengineering:canned_raw_lobster` | - | no row / no | - | Crafting |
| Canned Lush Moss Salad | `creativeengineering:canned_lush_moss_salad` | - | no row / no | - | Crafting |
| Canned Lychee | `creativeengineering:canned_lychee` | - | no row / no | - | Crafting |
| Canned Maggots | `creativeengineering:canned_maggots` | - | no row / no | - | Crafting |
| Canned Mango | `creativeengineering:canned_mango` | - | no row / no | - | Crafting |
| Canned Mango Salad | `creativeengineering:canned_mango_salad` | - | no row / no | - | Crafting |
| Canned Mangosteen | `creativeengineering:canned_mangosteen` | - | no row / no | - | Crafting |
| Canned Maple Cookies | `creativeengineering:canned_maple_cookie` | - | no row / no | - | Crafting |
| Canned Maple Glazed Bacon | `creativeengineering:canned_maple_glazed_bacon` | - | no row / no | - | Crafting |
| Canned Melon | `creativeengineering:canned_melon` | - | no row / no | - | Crafting |
| Canned Melon Slice | `canned_goods:canned_melon_slice` | - | no row / no | - | - |
| Canned Mhadjeb | `creativeengineering:canned_mhadjeb` | - | no row / no | - | Crafting |
| Canned Miso with Bamboo Sprouts | `creativeengineering:canned_miso_with_bamboo_sprouts` | - | no row / no | - | Crafting |
| Canned Mixed Salad | `creativeengineering:canned_mixed_salad` | - | no row / no | - | Crafting |
| Canned Mulberries | `creativeengineering:canned_mulberry` | - | no row / no | - | Crafting |
| Canned Mulberry Bread | `creativeengineering:canned_mulberry_bread` | - | no row / no | - | Crafting |
| Canned Mulberry Cookies | `creativeengineering:canned_mulberry_cookie` | - | no row / no | - | Crafting |
| Canned Mulberry Pie Filling | `creativeengineering:canned_mulberry_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Mushroom Stew | `canned_goods:canned_mushrooms` | - | no row / no | - | Crafting |
| Canned Nachos | `creativeengineering:canned_nachos` | - | no row / no | - | Crafting |
| Canned Nether Salad | `creativeengineering:canned_nether_salad` | - | no row / no | - | Crafting |
| Canned Nether Wart Sourdough | `creativeengineering:canned_nether_wart_sourdough` | - | no row / no | - | Crafting |
| Canned Noodle Soup | `creativeengineering:canned_noodle_soup` | - | no row / no | - | Crafting |
| Canned Notch Apples | `canned_goods:canned_enchanted_apples` | - | no row / no | - | - |
| Canned Onion | `creativeengineering:canned_onion` | - | no row / no | - | Crafting |
| Canned Orange | `creativeengineering:canned_orange` | - | no row / no | - | Crafting |
| Canned Pasta with Broccoli | `creativeengineering:canned_pasta_with_broccoli` | - | no row / no | - | Crafting |
| Canned Pasta with Duck | `creativeengineering:canned_duck_pasta` | - | no row / no | - | Crafting |
| Canned Pasta with Meatballs | `creativeengineering:canned_pasta_with_meatballs` | - | no row / no | - | Crafting |
| Canned Pasta with Mutton Chop | `creativeengineering:canned_pasta_with_mutton` | - | no row / no | - | Crafting |
| Canned Peach | `creativeengineering:canned_peach` | - | no row / no | - | Crafting |
| Canned Pear | `creativeengineering:canned_pear` | - | no row / no | - | Crafting |
| Canned Pearl Pasta | `creativeengineering:canned_pearl_pasta` | - | no row / no | - | Crafting |
| Canned Pears in Rock Sugar | `creativeengineering:canned_pear_with_rock_sugar` | - | no row / no | - | Crafting |
| Canned Persimmon | `creativeengineering:canned_persimmon` | - | no row / no | - | Crafting |
| Canned Persimmon Cookies | `creativeengineering:canned_persimmon_cookie` | - | no row / no | - | Crafting |
| Canned Pickles | `creativeengineering:canned_pickled_pickles` | - | no row / no | - | Crafting |
| Canned Pineapple | `creativeengineering:canned_pineapple` | - | no row / no | - | Crafting |
| Canned Pineapple Fried Rice | `creativeengineering:canned_pineapple_fried_rice` | - | no row / no | - | Crafting |
| Canned Pineapple Pie Filling | `creativeengineering:canned_pineapple_pie_filling` | - | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Poisonous Potatos | `canned_goods:canned_posionous_potatos` | - | no row / no | - | Crafting |
| Canned Potato | `creativeengineering:canned_potato` | - | no row / no | - | Crafting |
| Canned Potatos | `canned_goods:canned_potatos` | - | no row / no | - | - |
| Canned Powdered Milk | `creativeengineering:canned_powdered_milk` | - | no row / no | - | Crafting |
| Canned Pufferfishs | `canned_goods:canned_pufferfishs` | - | no row / no | - | Crafting |
| Canned Pumpkin | `creativeengineering:canned_pumpkin` | - | no row / no | - | Crafting |
| Canned Pumpkin Pie Filling | `creativeengineering:canned_pumpkin_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Pumpkin Pies | `canned_goods:canned_carrots` | - | no row / no | - | - |
| Canned Pumpkin Soup | `creativeengineering:canned_pumpkin_soup` | - | no row / no | - | Crafting |
| Canned Rabbit Stew | `canned_goods:canned_rabbit_stew` | - | no row / no | - | Crafting |
| Canned Raw Beef | `creativeengineering:canned_raw_beef` | - | no row / no | - | Crafting |
| Canned Raw Calamari | `creativeengineering:canned_raw_calamari` | - | no row / no | - | Crafting |
| Canned Raw Catfish | `creativeengineering:canned_raw_catfish` | - | no row / no | - | Crafting |
| Canned Raw Chicken | `creativeengineering:canned_raw_chicken` | - | no row / no | - | Crafting |
| Canned Raw Cod | `creativeengineering:canned_raw_cod` | - | no row / no | - | Crafting |
| Canned Raw Crab Leg | `creativeengineering:canned_raw_crab` | - | no row / no | - | Crafting |
| Canned Raw Lionfish | `creativeengineering:canned_raw_lionfish` | - | no row / no | - | Crafting |
| Canned Raw Mutton | `creativeengineering:canned_raw_mutton` | - | no row / no | - | Crafting |
| Canned Raw Perch | `creativeengineering:canned_raw_perch` | - | no row / no | - | Crafting |
| Canned Raw Pike | `creativeengineering:canned_raw_pike` | - | no row / no | - | Crafting |
| Canned Raw Pork | `creativeengineering:canned_raw_pork` | - | no row / no | - | Crafting |
| Canned Raw Rabbit | `creativeengineering:canned_raw_rabbit` | - | no row / no | - | Crafting |
| Canned Raw Salmon | `creativeengineering:canned_raw_salmon` | - | no row / no | - | Crafting |
| Canned Roast Chicken | `creativeengineering:canned_roast_chicken` | - | no row / no | - | Crafting |
| Canned Roasted Adzuki Beans | `creativeengineering:canned_roasted_adzuki_beans` | - | no row / no | - | Crafting |
| Canned Roasted Bell Pepper | `creativeengineering:canned_roasted_bell_pepper` | - | no row / no | - | Crafting |
| Canned Roasted Cauliflower | `creativeengineering:canned_roasted_cauliflower` | - | no row / no | - | Crafting |
| Canned Roasted Zucchini | `creativeengineering:canned_roasted_zucchini` | - | no row / no | - | Crafting |
| Canned Rose Hip Pie Filling | `creativeengineering:canned_rose_hip_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Rotten Flesh | `canned_goods:canned_rotten_flesh` | - | no row / no | - | Crafting |
| Canned Salmonberries | `creativeengineering:canned_salmonberry` | - | no row / no | - | Crafting |
| Canned Salmonberry Pie Filling | `creativeengineering:canned_salmonberry_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Scarlet Cheese | `creativeengineering:canned_scarlet_cheese` | - | no row / no | - | Cooking Pot, Mixer |
| Canned Shepherd's Pie | `creativeengineering:canned_shepherds_pie` | - | no row / no | - | Crafting |
| Canned Shulker Flesh | `creativeengineering:canned_shulker` | - | no row / no | - | Crafting |
| Canned Sinigang | `creativeengineering:canned_sinigang` | - | no row / no | - | Crafting |
| Canned Sopa De Macaco | `creativeengineering:canned_sopa_de_macaco` | - | no row / no | - | Crafting |
| Canned Sourdough Bread | `creativeengineering:canned_sourdough_bread` | - | no row / no | - | Crafting |
| Canned Spider Eyes | `canned_goods:canned_spider_eyes` | - | no row / no | - | Crafting |
| Canned Squid Ink Pasta | `creativeengineering:canned_squid_ink_pasta` | - | no row / no | - | Crafting |
| Canned Steak | `canned_goods:canned_beef` | - | no row / no | - | Crafting |
| Canned Strawberries | `creativeengineering:canned_strawberry` | - | no row / no | - | Crafting |
| Canned Strider Moss Stew | `creativeengineering:canned_strider_moss_stew` | - | no row / no | - | Crafting |
| Canned Stuffed Pumpkin | `creativeengineering:canned_stuffed_pumpkin` | - | no row / no | - | Crafting |
| Canned Stuffed Shulker | `creativeengineering:canned_stuffed_shulker` | - | no row / no | - | Crafting |
| Canned Suspicious Stew | `canned_goods:canned_suspicious_stew` | - | no row / no | - | - |
| Canned Sweet Berries | `canned_goods:canned_sweet_berries` | - | no row / no | - | - |
| Canned Sweet Berries | `creativeengineering:canned_sweet_berry` | - | no row / no | - | Crafting |
| Canned Sweet Berry Cookies | `creativeengineering:canned_sweet_berry_cookie` | - | no row / no | - | Crafting |
| Canned Sweet Berry Pie Filling | `creativeengineering:canned_sweet_berry_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Sweet Potato | `creativeengineering:canned_sweet_potato` | - | no row / no | - | Crafting |
| Canned Sweet Potato Pie Filling | `creativeengineering:canned_sweet_potato_pie_filling` | ok | no row / no | - | Cooking Pot, Crafting, Mixer |
| Canned Sweet Rolls | `creativeengineering:canned_sweet_roll` | - | no row / no | - | Crafting |
| Canned Tomato | `creativeengineering:canned_tomato` | - | no row / no | - | Crafting |
| Canned Tomato Sauce | `creativeengineering:canned_tomato_sauce` | - | no row / no | - | Crafting |
| Canned Tortilla Chips | `creativeengineering:canned_tortilla_chip` | - | no row / no | - | Crafting |
| Canned Tropical Fish | `canned_goods:canned_tropical_fish` | - | no row / no | - | Crafting |
| Canned Turnip | `creativeengineering:canned_turnip` | - | no row / no | - | Crafting |
| Canned Turnip Beef Stew | `creativeengineering:canned_turnip_beef_stew` | - | no row / no | - | Crafting |
| Canned Turnip Salad | `creativeengineering:canned_turnip_salad` | - | no row / no | - | Crafting |
| Canned Uncanny Cookies | `creativeengineering:canned_uncanny_cookie` | - | no row / no | - | Crafting |
| Canned Vegetable Pasta | `creativeengineering:canned_vegetable_pasta` | - | no row / no | - | Crafting |
| Canned Vegetable Soup | `creativeengineering:canned_vegetable_soup` | - | no row / no | - | Crafting |
| Canned White Strawberries | `creativeengineering:canned_white_strawberry` | - | no row / no | - | Crafting |
| Canned Zucchini | `creativeengineering:canned_zucchini` | - | no row / no | - | Crafting |

#### Hazard (`hazard`)

- R1 (Cannable): **Rule:** Meat Meal x 4, no additive (placeholder); per item, can parity 2.19 ([can_parity.md](can_parity.md))
- R2 (Recipe shape by station): **Decision:** none: dropped or found

| Member | Id | R1 | R2 | Can | Placeable | Stations |
|---|---|---|---|---|---|---|
| Frozen Flesh | `hostiledelight:frozen_flesh` | - | - | no row / no | - | Crafting |
| Maggot | `alexsmobs:maggot` | ok | - | yes / yes | - | - |
| Poisonous Potato | `minecraft:poisonous_potato` | ok | - | yes / yes | coaster | Fan (haunting) |
| Pond Water | `immersive_weathering:pond_water` | ok | - | no / no | - | - |
| Pufferfish | `minecraft:pufferfish` | ok | - | yes / yes | coaster | Mana Pool |
| Rotten Apple | `domesticationinnovation:rotten_apple` | ok | - | no / no | - | - |
| Rotten Cherry | `vinery:rotten_cherry` | ok | - | no / no | - | - |
| Rotten Flesh | `minecraft:rotten_flesh` | ok | - | yes / yes | - | Crushing Wheels, Mixer |
| Sinister Carrot | `domesticationinnovation:sinister_carrot` | ok | - | no / no | - | - |
| Soap | `supplementaries:soap` | ok | - | no / no | - | Crafting, Mixer |
| Spider Eye | `minecraft:spider_eye` | ok | - | yes / yes | - | - |

<!-- FAMILIES:END -->

## 4. Limits, findings and decisions

### 4.1 Limits

- Membership is matched by English name and food type, so a renamed item can change family. Anything no rule takes
  lands in "Unsorted" (none today) and `--check` exits 1.
- Everything is read from the last dumps (2026-10-24). Work built since (the 2026-10-25 produce recipes, the Display
  Delight dish blocks, the Vintage and Fungi Delight cans) shows up after the next `/dumpall`; until then 3.2 still
  counts those members as gaps (4.4).
- A check sees a station and an ingredient, not counts or slot positions: "crust + 2 fruit + 2 sugar" is checked as
  "an Oven recipe with pie crust".
- "Used in" counts existing recipes; it shows where members go now, not where they should.

### 4.2 Decisions for you

- **D1. Every Decision entry in 3.1.** Decided 2026-10-25 for `fruit`, `veggie`, `meat`, `fish`, `egg` and `dairy`
  (4.4); open for the rest. Accept the proposal, change it, or turn it into "no rule" (the family keeps
  per-item recipes). 3.2 shows how many members already match each proposal, which is the cost of accepting it.
  The biggest are: placement for meals, drinks, sandwiches and pastries (Display Delight placed / Food Plate, few
  members today); recipe shape for sandwiches, skewers and stuffed vegetables (shaped + one assembly, like the
  burgers); the cooking trio for cooked produce; Mixer and Arc Furnace routes for every golden food (stated only for
  carrot and apple); the enchanted assembly for every enchanted golden food (stated only for the apple).
- **D2. The family list.** Merge, split or rename. Unsure: `meal/plate` (36) is the catch-all for mains (curries,
  roasts, sides); drinks sort by kind (juice, tea, coffee), not by base; sweet and savory pies share `pie`; overlays as
  families or as flags.
- **D3. Sandwich bread shapes.** `sandwich` splits by the bread its sprite shows, from the baked-goods #9 audit
  ([brief](briefs/baking_baked-goods.md)): square slices, long roll, round bun, wrap, open toast. Chunkwich and Zucchini
  Sandwich were not in that audit and sit in `square` by default; Kangaroo Burger shows a long roll but stays a burger
  (the planned bun row skips it). Confirm or move them.
- **D4. Foods without a family (3.3).** Keep them out, or give any of them a family.

### 4.3 Findings: stated rules the members break (2026-10-22 dumps)

From the rule and planned rows in 3.2 (detail per member in 3.4):

| # | Family | Finding |
|---|---|---|
| F1 | `soup`, `meal` | Heatless rule: 14 of 32 pot soups and 26 of 49 pot meals have no `_manual_only` shapeless craft (Sinigang, Fish Stew, Beetroot Soup, Rabbit Stew, Mushroom Stew, the rice dishes, the marinated porks...). Some predate the rule, some are the pack's own older crafts under other ids |
| F2 | `soup` | Chorus Stew and Endermite Stew (the wooden-bowl ones) have no Cooking Pot recipe and no jar fluid; Sopa De Macaco and Suspicious Stew have no pot recipe |
| F3 | `cake/whole` | Chocolate Cake (both) and Cake still have a recipe outside Oven / Crafting |
| F4 | `cookie` | Maple Cookie, Gingerbread and Gingerbread Man aren't oven-only with cookie dough (since 2026-10-27 Maple and the Man are; Gingerbread is `dessert/other`) |
| F5 | `sandwich/burger` | Hamburger with Egg, Loaded Hamburger, Simple Hamburger and Vegetarian Burger still have a shapeless recipe; Vegetarian Burger has no assembly |
| F6 | `pie/whole` | Sweet Potato Pie has no crust + full can route |
| F7 | `pie/slice` | 6 slices want a can and have none (Blueberry, Cherry, Crimson Berry, Green Apple, Nightshade Berry, Truffle): the known slices with no source in the pack |
| F8 | cans | Slice of Cake (`/` but the `_slices` recipe takes it), Suspicious Stew (`/` but Canned Goods' values-only can), Glistering Melon Slice (canned, no worksheet row), Nachos and Popcorn Bucket (fill a feast can, no row) |
| F9 | planned | 0 of 9 pies and 2 of 13 feasts have an assembly; 0 juices or teas follow the drinks #8 shape; 2 of 26 teas place; 1 of 17 cake slices places |

### 4.4 Produce families applied (2026-10-25)

Owner, 2026-10-25: "accept the recipe rules, assume every food item in the range should be placeable", and Display
Delight placement for the egg dishes. The produce Decision rows became Rules (3.1), with these refinements where a
member's own criteria made the proposal wrong:

- **Cured forms keep their own station.** Cooked meat, cooked fish and cooked egg: the cooking trio is for plainly
  cooked ones; jerky, kippers, smoked ham, smoked dragon leg, glazed porkchop, century egg and surstromming keep the
  Keg, Smoker, Spout, a craft or the Fermenting Jar. Boiled vegetables come from the Cooking Pot, scrambled eggs too.
- **Dropped cuts.** Hoglin ear, strider slice, guardian fillet and the Oceanic squid tentacles are mob drops, not board
  cuts. A seasoned raw cut (mint chops) is crafted from its raw meat. A cooked cut vegetable can be cut from the
  cooked whole (vegan patty from a baked cave carrot).
- **Placeable:** egg dishes get a Display Delight placed block (patcherMod `compat/DisplayDelightDishes`, 9 new
  blocks). The produce briefly joined the coaster tag and lay flat without a model; owner 2026-10-25: cut every coaster
  item without its own model (done, placeable_food.md 3), or add a model for each (open).

Membership changes: the 233 foods of Hostile, End's, Miner's, Ocean's and Oceanic Delight got food types (food.md);
Vintage's Pickle moved to `preserve/pickle`, Inky Cap Scrambled Eggs to `egg/dish`, fish eggs (Salmon Eggs, Ancient Fish
Eggs) to `fish/raw`, Silverfish Eggs to `meat/raw`, Shulker Omelette Mixture to `ingredient/other`, Fried Shrimp to
`meal/fried`. Out of the families as removed items: Incubation's fried egg, More Delight's diced potatoes (merged into
potato slices), venison (6) and chevon (2), passion fruit glazed duck, Canned Food and Hot Hot Hotpot.

Changes made to bring members in line (Creative Delight script tree, so they load once its jar is installed):

| Member | Rule | Change |
|---|---|---|
| Baked Durian, Baked Pear, Dried Persimmon | `fruit/cooked` trio | furnace recipe (`fruitsdelight/recipes.js`) |
| Grilled Ghast, Cooked Rotten Ham, Fried Turtle Egg | trio | campfire recipe (`hostiledelight/recipes.js`) |
| Prepared Sea Pickle | `veggie/raw_cut` board | Hostile's broken knife recipe (B2) replaced (`hostiledelight/recipes.js`) |
| Cooked Arthropod | trio | smoker and campfire (`miners_delight/recipes.js`) |
| Raw / Cooked Duck Fillet, Duck Noodles | board, trio | wired to Untitled Duck Mod's raw duck (`abnormals_delight/recipes.js`; types-and-tuning #10) |
| Diced Potatoes with Egg and Tomato | heatless meal rule | `_manual_only` craft (`moredelight/recipes.js`) |
| Venison, chevon | meat planned row | removed items, Delightful venison off in its config (types-and-tuning #10) |
| Truffle, Truffle Slice, Cooked Inky Cap, the Vintage cans | cannable | can parity rows added for Fungi; `gen_can_types.py` run (20 Vintage and Fungi cans) |
| 9 egg dishes | DD placed | Display Delight blocks (patcherMod) |

**Still open after the change:** Maple Glazed Bacon and Maple Cookie: owner 2026-10-25, they stay and take Rustic
Delight's Syrup (cooked bacon + syrup; oven cookie dough + syrup + honey), [rustic_delight.md](rustic_delight.md) 5.2.
Since 2026-11-01 (owner: 50/50 maple syrup and honey) both take syrup + honey: bacon is cooked bacon + syrup + honey
bottle ([rustic_delight.md](rustic_delight.md) 5.10, TODO `creativedelight/members #16`).
Live once the Creative Delight jar is installed. The new foods
of Hostile, End's, Miner's, Ocean's and Oceanic Delight get no cans until each mod's "Cans" row is decided
(`gen_can_types.py` `UNDECIDED_MODS`).

### 4.5 Family recipe helpers (2026-10-25)

Owner, 2026-10-25: each family's recipe shape is generated by one call per recipe, so a member script says only input
and output and a rule change is made once. Since 2026-10-27 the one helper is `global.CD.food(e)` (Creative Delight
startup, `kubejs_startup/creativedelight/cd_food.js`, [creative_delight.md](creative_delight.md) 30); the rule values are
in `global.CD.FOOD` at the top of that file. The table below is the first helper, `global.foodFamily`
(`creativeengineering/food_family_recipes.js`), deleted 2026-10-27; kept as history.

| Call | Station and rule | Default |
|---|---|---|
| `fruitSlice`, `veggieSlice`, `fishSlice`, `meatCut`, `pieSlice`, `cakeSlice` `(in, out, countOverride, id)` | Cutting Board, any knife | 4, 4, 6, 2, 4, 4 |
| `fruitCook`, `veggieCook`, `meatCook`, `fishCook`, `eggCook` `(in, out, id, only, xpOverride)` | Furnace 200, Smoker 100, Campfire 600 ticks; `only` lists the stations a mod does not already ship | xp 0.1 fruit, 0.35 others |
| `pieBake(flavour, pieKey, fillingKey, id)` | Oven, pie tin: crust + 2 flavour + 2 sweet; with a filling can: pot recipe and crust + can | |
| `cakeBake(flavour, cakeKey, id)` | baked base + frosting + 2 flavour by hand; raw base in the cake pan | |
| `cookieBake(flavour, cookieKey, count, id)` | Oven, baking sheet: dough + 2 flavour (or an array of slots) | 12 |
| `muffinBake(berry, key, count, id)`, `tartBake(fruit, key, count, id)` | muffin tin with paper; baking sheet | 4, 2 |

Result keys are `global.item` keys or full ids; recipe ids are unchanged from the hand-written rows. Moved onto the
helpers: `creativeengineering/baking.js` (cake, pie, cookie, muffin and tart tables), `creativeengineering/cultivated_meat.js`,
and the cut and cook rows of the Alex's, Delightful, Hostile, Miner's, Abnormals, Farmer's, Fruits and Rustic Delight
files. Outliers kept as before through overrides: Roasted Acorn xp 0.35 (rule fruit 0.1), Hostile's campfire cooks xp 1
(rule 0.35). Left hand-written: Baklava (not the standard pie shape), bread and sourdough (bread family undecided),
Create, Cooking Pot and sequenced-assembly rows. Next families get a call here when their rule is decided.

**Successor (2026-10-25, adopted 2026-10-27):** `global.CD.food` takes the input first, adds the heatless meal, hand food
(burger, skewer, sandwich, wrap), golden and culture (`cultureCut`: a meat culture to its raw meat or fish, 4 unless given,
no bone meal) families, makes its own ids (`<output>_from_<input>` on the board and in the cooking trio, the output's path
in the oven) and sweeps the old recipes of the output. Owner 2026-10-27: its values are the rules (fruit cook xp 0.35, veggie
slice 2; the old 0.1 and 4 are gone); outliers such as Roasted Acorn and Hostile's xp 1 follow the rule when their rows move
(TODO `creativedelight/framework #12`, a changelog bullet then). Callers today: `creativeengineering/baking.js`,
`creativeengineering/cultivated_meat.js`, `rusticdelight/recipes.js`.

### 4.6 Baked families restructured and applied (2026-10-27)

Owner, 2026-10-27: bread, lasagna, pizza, quiche and dessert are families; dessert is the catchall (brownies, pudding,
scones, adzuki bun, waffles and rolls, any sweet or baked food with no family); every cookie, cake, pie, muffin / cupcake
and quiche is folded in and follows its family rules.

Tree (tool `FAMILIES` / `classify()`): `bake` split into `pizza`, `quiche` and `lasagna` (Horror Lasagna is a lasagna
serving); `pastry` and `pudding` folded into `dessert` with Beetroot Brownie (tray and piece), Hostile's two brownies, Fig
Pudding (block and slice), Adzuki Bun (was `bread/flat`) and Biscuit of Totality (was no family), Gingerbread (was `cookie`; desserts may get their own cookware or recipe treatment, TODO baked-goods #36); `cake/small` is
`cake/muffin` (muffins, Sweet Potato Cupcake). Moved out: Cake Base, Cake Paste and cookie dough to `ingredient/dough`
(oven bases, not foods to eat), Spiked Tartare to `meal/plate` (a fish dish, matched by "tart"). Merged items
(`item_merges.js`, Neapolitan's chocolate cake) are skipped like removed ones. Sponge Cake is renamed Sponge Pie and
sits in `pie` (`FAMILY_OVERRIDE` until the next dump carries the new name). Rules: whole cakes may also spout onto a
baked base (plain and chocolate cake); hand pies get an oven rule; Sponge Pie and End's Chorus Fruit Pie have no filling
can (R4 exception); honey cake takes 1 honey + 1 honeycomb (TODO baked-goods #26).

Applied (Creative Delight `creativeengineering/baking.js` tables, so each family call sweeps the mods' own recipes):

| Family | Members brought onto the rule | Recipe now |
|---|---|---|
| `cookie` | Bat, Chorus, Maple (had no recipe), Hostile's seven (Emitting, Swelling, Gazing, Magical Butter, Vein, Crawling, Trembling) | Oven, baking sheet: cookie dough + 2 flavour -> 12 (smoked bat wing, chorus fruit grain, Rustic syrup, blaze rod, ghast tentacle, guardian fillet, magical butter, magma cream, raw endermite, ender pearl). All in the Supplementaries jar tag. Gingerbread Man: gingerdough + sugar (was a press recipe). Gingerbread left the family for `dessert/other` and keeps its furnace / smoker recipes |
| `cake/whole` | Honey Cake | baked base + frosting + honey bottle + honeycomb (oven: raw base) |
| `pie/whole` | Chorus Fruit Pie, Sponge Pie | Oven, pie tin: crust + 2 chorus fruit grain + 2 sugar; Sponge Pie: crust + sponge + prismarine shard + 2 sugar |
| `pie/small` | Rotten Pie, Green Pie, Chorus Flower Pie | `handPieBake` (new, CD jar): Oven, baking sheet, crust + the filling -> 1 |
| `cake/muffin` | Sweet Potato Cupcake (renamed Sweet Potato Muffin 2026-10-29) | `muffinBake`: oven 4 (2 baked sweet potato); the pot route was dropped 2026-10-29 (baked-goods #32); Veggies' sweet potato dough craft kept |
| `quiche/whole` | Zucchini Quiche | sequenced assembly on a pie crust (3 roasted zucchini, cooked meat, 250 mB milk) beside its shaped recipes. Does not load (Bug, TODO baked-goods #37): `F.assemble` reads the cooked-meat array as a filling step |

Open notes (owner, 2026-10-27): every cake should become a sequenced assembly and the existing spouts need more steps (baked-goods #34); investigate whether the brownie tray and Adzuki Bun are dessert or bread (baked-goods #35).

Not applied (decision or planned rows, not asked): pie and cake slice placement (Small Food Plate needs a Display Delight
model per slice), pizza recipes, lasagna dish, brownie / pudding / pastry oven routes, the planned pie sequenced assembly
(baked-goods #4). Chorus cookie from the uncanny cookie (baked-goods #22) not done: it takes chorus fruit grain.

### 4.7 Pancake family (2026-11-02)

Owner, 2026-11-02 (TODO `merges/baking #2`): the Stackable Pancake is not the base of the pancake family; every pancake
keeps its own recipes. Every pancake in the pack survives. Built under TODO `dishes/meals #12` and Veggies R7
(`creativedelight/members #30`). In the CD jar installed 2026-11-02 23:16 (again 2026-11-03 00:08), not booted. Tests
FF-15 to FF-17; scratch rows SQ-PCK-01 to -05, CQ-PCK-01, -02 (queue section SQ-CDJ6). After the first boot also
`/cegen food_families` (plan_creative_delight.md K).

Survey (dicts, the 2026-10-30 dumps, Creative Delight scripts and the Rustic port, 2026-11-02):

| Pancake | Id | Kind | Placeable | Recipes (unchanged) |
|---|---|---|---|---|
| Pancake (Supplementaries, named "Pancake" since 11-01) | `supplementaries:pancake` (`i.pancakes`) | block item, eaten placed (not a food item) | own stack block, up to 8 | grid 3, heated Mixer 5 (milk or egg solution) (CD `supplementaries/recipes.js`); the plain Rustic plate cuts into it |
| Rustic plates: Pancakes, Honey, Chocolate, Cherry Blossom, Vegetable, Pumpkin, Coffee | `rusticdelight:pancakes`, `<flavour>_pancakes` (`i.pancake_plate`, `i.<flavour>_pancake_plate`) | block items (PancakeStackBlock, 6 per plate, up to 12) | own block | Cooking Pot (Rustic port data) |
| Rustic single pancakes, 6 flavours | `rusticdelight:<flavour>_pancake` (`i.<flavour>_pancake`) | food, 4 / 0.6 | back onto their plate (sneak-use) | taken from the plate; plate from 6 singles |
| Sweet Potato Pancakes (Veggies) | `veggiesdelight:sweet_potato_pancakes` | food, 12 / 0.8, bowl, stack 32 | **new:** Display Delight placed dish | Cooking Pot (sweet potato dough, honey, milk, bowl) |

Rustic's plain single `rusticdelight:pancake` is not registered (TODO `creativedelight/members #15`); the 2026-10-30
dumps still list it, so the next `/dumpall` drops it from 3.4. Autumnity is not in the pack (no jar, no port, no ids), so
`autumnityport #5` "remove or integrate pancakes" has nothing to act on. No undecided pancake was found.

The rule (tool `RULES['dessert/pancake']`, 3.1):

- **Family:** `dessert/pancake`, new subfamily of `dessert` (pancakes left `dessert/pastry`). `classify()` puts any
  food of type `sweet/pancake` or named "pancake" here, and a whole block named "pancake" (the Rustic plates, once the
  food block dump lists them).
- **No base item:** each pancake keeps its own recipes; nothing is folded into the Supplementaries Pancake.
- **Food type:** `sweet/pancake`, nested in `sweet` (so every `sweet` reader still sees them): Sweet Potato Pancakes and
  the six Rustic singles ([food.md](food.md) 2.1, `tools/food_catalog.py` TYPES and its name rule;
  `#creativeengineering:food_type/sweet/pancake`, `i.t_food_sweet_pancake`). Block items stay out of the food types.
- **Placeable:** every pancake can be put down. Sweet Potato Pancakes got a Display Delight placed dish,
  `creativeengineering:placed_sweet_potato_pancakes` (bowl dish, food surface cut from the top of the sprite, crop
  4,3 to 12,8), in Creative Delight's `displaydelight_dishes.json`, written by `tools/gen_dd_dishes.py`. Sneak + right
  click the top of a block puts it down, an empty hand takes it back.
- **Cannable:** as `dessert` (per item, can parity 2.24); no pancake has a can row today.
- **Still planned:** Cooking Pot recipe for the Supplementaries Pancake (`merges/baking #1`), coverings recoloured to the
  jam (`dishes/meals #37`).

Data changed: `item_dict.js` (13 Rustic keys, `i.t_food_sweet_pancake`), `food_types.json` and the three member
`food_types.js` (`tools/food_catalog.py --members`), `food_families.json` (hand-synced, as 2026-10-28), `food_dict.js`
(`f.pastry_<flavour>_pancake`, `tools/food_dict.py` FAMILY_KEY), Creative Delight `displaydelight_dishes.json` and the
generated blockstate, model, loot table, `displayable` tag entry and name. Sections 3.1 and 3.4 here were hand-synced
for the pancake rows; the rest of the generated block was not regenerated (it is already stale against the 10-30 dumps,
94 unsorted).

`tools/gen_dd_dishes.py` now reads item sprites from jars nested in a bundle (`META-INF/jarjar/`); before, every
Creative Delight member food (Veggies, End's) stopped it. Found while doing this: a full run rewrites the nine older dish
models onto the Display Delight 1.7.0 port's bowl (`salad_bowl` texture, new geometry); they were left as they were
(only the new dish uses the port's bowl). Rerun the tool and look at the nine dishes before accepting that.

## 5. File map

| File | Role |
|---|---|
| [tools/food_families.py](../tools/food_families.py) | families, membership, rule rows and checks, section 3 |
| [kubejs/exported/food/](../kubejs/exported/food/) | `catalog.json`, `foods.json`, `food_blocks.json`, `recipes.json`, `placeable.json` (inputs) |
| [kubejs/exported/tags/item_tags.json](../kubejs/exported/tags/item_tags.json) | tag resolution, Supplementaries cookies tag |
| [kubejs/generation_scripts/food/food_types.json](../kubejs/generation_scripts/food/food_types.json), [can_types.json](../kubejs/generation_scripts/food/can_types.json) | food types, removed items, can sources and can recipe food slots |
| [kubejs/generation_scripts/food/placeable.json](../kubejs/generation_scripts/food/placeable.json) | placement tags |
| [cd_food.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_food.js) | the recipe helpers per family, `global.CD.food` (4.5) |
| [can_parity.md](can_parity.md) | the Want cell per food (cannable compliance) |
| [oven.md](oven.md), [recipes_cooking.md](recipes_cooking.md), [placeable_food.md](placeable_food.md), [alcohol.md](alcohol.md), [TODO](todo/TODO.md) | the sources of the Rule and Planned rows |

## 6. Offline checks

- `python3 tools/food_families.py --check`: exit 1 if section 3 is out of date or a food is unsorted.
- `python3 tools/food_families.py --print`: section 3 to stdout, without writing.

## 7. In-game test suite

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-FF-00, -02, SQ-FFB-00, SQ-RM-02, -03.

Needs the patcherMod and Creative Delight jars from this change installed and a restart. Then `/dumpall` and
`python3 tools/food_families.py`: the produce rows of 3.2 read full, except Maple Glazed Bacon (4.4).

- [ ] **FF-01 Furnace fruit:** a pear, a persimmon and durian flesh in a furnace -> Baked Pear, Dried Persimmon, Baked Durian
- [ ] **FF-02 Campfire trio:** a ghast tentacle, a rotten ham and a turtle egg on a campfire -> Grilled Ghast, Cooked Rotten Ham, Fried Turtle Egg; an arthropod on a campfire and in a smoker -> Cooked Arthropod
- [ ] **FF-03 Duck line:** Untitled Duck Mod raw duck on a cutting board with a knife -> 2 Raw Duck Fillets; a fillet in furnace, smoker and campfire -> Cooked Duck Fillet; JEI shows Duck Noodles from the Cooking Pot
- [ ] **FF-04 Prepared Sea Pickle:** a sea pickle on a cutting board with a knife -> Prepared Sea Pickle (no error for `ppicklerecp` in `logs/latest.log`)
- [ ] **FF-05 Removed meats:** JEI search "venison", "chevon" -> nothing (or only REMOVED entries); Diced Potatoes with Egg and Tomato has a crafting-grid recipe from 2 baked potatoes, a cooked egg, tomato sauce and an onion
- [ ] **FF-06 Coaster, models only (changed 2026-10-25):** right click an empty coaster with an apple, a carrot and raw beef (they have coaster models) -> each goes on, with "Placeable on a coaster"; a Shrimp (no model) -> not taken, no coaster line
- [ ] **FF-07 Egg dishes placed:** sneak + right click the top of a block with Shakshouka, an Omelet (More Delight) and a Shulker Omelette -> a placed dish (bowl or plate with the food), facing you; empty hand right click takes it back; breaking it drops the food; WTHIT names it "Placed ..."
- [ ] **FF-08 Dish on a counter:** a placed egg dish on a Cooking for Blockheads counter sits on the worktop and breaks when the counter is removed
- [ ] **FF-09 Helper parity:** no `Error occurred while handling event` in `logs/latest.log` after load; JEI shows the same recipes as before for Apple Pie (oven, filling can, crust + can), Carrot Cake, Cookie (12), Blueberry Muffin (4), Lemon Tart (2), Cooked Duck Fillet (furnace, smoker, campfire), Salmon on a board (6 slices + bone meal), Catfish (6)
- [ ] **FF-10 CD.food callers (2026-10-27):** CD jar built 2026-10-27 installed, restart. No `Error occurred while handling event` and no `cd_food.js` error in `logs/latest.log`. JEI: Blueberry Muffin has one Cooking Pot recipe (2, from any dough) and the oven (4, on paper); a Culture of Beef on a board with a knife -> 4 raw beef, a Culture of Cod -> 8 Cod Slices and no bone meal; Maple Cookie and Syrup Cookie only in the oven (12); Syrup Cheesecake in the oven, and its 4 slices craft back; a Glowing Calamari in furnace, smoker and campfire -> Rustic's Cooked Calamari
- [ ] **FF-11 Cookies (2026-10-27):** CD jar of this change installed, restart. JEI, Oven: Bat, Chorus, Maple, the seven Hostile cookies and the Gingerbread Man each show one oven recipe (cookie dough + 2 flavour, 12) and no crafting, furnace or press recipe; Gingerbread keeps its furnace / smoker recipes; a Hostile cookie goes into a Supplementaries jar
- [ ] **FF-12 Hand pies and cupcake:** JEI: Rotten Pie, Green Pie and Chorus Flower Pie only in the Oven (pie crust + filling, baking sheet); Sweet Potato Cupcake in the Oven (muffin tin, 4) and the Cooking Pot (2)
- [ ] **FF-13 Pies and cakes:** Sponge Cake shows as Sponge Pie (slice: Sponge Pie Slice) and bakes in the pie tin from crust + sponge + prismarine shard + 2 sugar; Chorus Fruit Pie bakes from crust + 2 chorus fruit grain + 2 sugar; Honey Cake: baked cake base + frosting + honey bottle + honeycomb by hand, raw base in the oven; no other recipe for any of them except slices -> whole
- [ ] **FF-14 Zucchini quiche assembly:** JEI shows a sequenced assembly on a pie crust: 3 roasted zucchini, cooked meat, 250 mB milk -> Zucchini Quiche; the incomplete item has a grey quiche sprite
- [ ] **FF-15 Pancake food type (2026-11-02):** after `/dumpall`, `/dumpfood` and `/tags` show `#creativeengineering:food_type/sweet/pancake` with Sweet Potato Pancakes and the six Rustic single pancakes; each is still in `#creativeengineering:food_type/sweet`; `#creativeengineering:food_family/dessert/pancake` holds the same seven
- [ ] **FF-16 Sweet Potato Pancakes placed:** sneak + right click the top of a block with Sweet Potato Pancakes -> a bowl of pancakes, facing you, named Placed Sweet Potato Pancakes in WTHIT; empty hand takes it back; breaking it drops the pancakes, no bowl dupe; then `/dumpall` and `python3 tools/placeable_catalog.py` give it the "Placeable" tooltip line
- [ ] **FF-17 Every pancake kept:** JEI: Pancake (Supplementaries), the seven Rustic plates, the six Rustic singles and Sweet Potato Pancakes each keep their recipes (no recipe removed or added by this change)
- [ ] **FFH-03 (offline):** `tools/test_cd_food.sh` runs; the only ids not in the last recipe dump are `truffle_pie` (dead, `fungiport #3`), the 7 culture cuts and the 6 glowing calamari cooks


## 8. History

- 2026-11-02: pancake family `dessert/pancake` and food type `sweet/pancake` (4.7): no base item, every pancake kept, Sweet Potato Pancakes a Display Delight placed dish; Veggies R7 folded; FF-15 to FF-17, SQ-PCK, CQ-PCK.
- 2026-10-25: family recipe helpers (4.5): cut, cook and bake rows of the Creative Delight scripts go through `global.foodFamily`; FF-09.
- 2026-10-27: `global.foodFamily` replaced by `global.CD.food` (4.5), culture family added; FFH-03, SQ-FFH-02.
- 2026-10-27: baked families restructured (pizza, quiche, lasagna, dessert; muffin / cupcake) and cookie, cake, pie,
  muffin and quiche members applied (4.6); Sponge Pie rename, honey cake split; FF-11 to FF-14.

- 2026-10-25: produce families (fruit to dairy) decided by the owner and applied (4.4). The 233 new Delight foods
  typed and sorted; `classify()` fixes for fish eggs, popsicles without the word, rice cakes, takoyaki, cabbage-wrapped
  rolls, squid rings and Vintage's pickle. Refreshed from the 2026-10-24 dumps.

- 2026-10-24: scanned the TODO rows of `food`, `cans`, `cooking`, `baking` and `dishes` and added every family-level
  recipe, definition or value they state as a Planned row in `RULES` (drinks Kettle / tea rules, jam, pickle, vinegar,
  dough, fats, cake, pie, cookie, pizza, quiche, feast, salad, golden values, stack sizes, seasoning). The rows they came
  from are In Progress in the TODO. Pancakes, patties and skewers as their own food types are noted on `pastry`, `meat`
  and `skewer`, not yet split into families (decision D2).

- 2026-10-23: written (families, stations, placement, cans, uses). Same day, restructured at the user's request: 3.1
  family rules (rule / planned / decision, with sources), 3.2 compliance, 3.3 foods without a family, 3.4 every member
  checked. `sandwich/bread` split into `square` and `roll` by the baked-goods #9 audit ("sandwich/square"), with
  its planned bread rows. `special` and four odd fits left the families (3.3); Bellini moved to mixed drinks (it is alcoholic,
  alcohol.md 11); golden cakes moved to golden food. Findings F1 to F9.
- 2026-10-28: Popsicle is its own family (`frozen/popsicle`: every popsicle, Cantaloupe and Corn Popsicle included); Shaved ice split out as `frozen/shaved_ice`. Members hand-synced in `food_families.md` and `food_families.json` because the food dump (2026-10-24) predates the Cantaloupe and Corn Popsicles: after the next `/dumpall`, run `python3 tools/food_families.py` and `food_catalog.py`.

## Owner ideas (scratchpad 2026-10-26)

- Coaster tag: cut every coaster item without its own model, or add a model for each (all raw, cooked, egg and dairy foods are in the tag now and those without a model lie flat; section 4.4): `cooking/kitchen #21`.
