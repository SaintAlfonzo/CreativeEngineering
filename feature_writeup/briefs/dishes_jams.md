# Brief: dishes / jams

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `jams` (rows 1 to 4) and `merges/dishes #7` (was
`dishes/jams #5`); checked and updated 2026-10-31. Layout per [README.md](README.md). Writeups:
[recipes_cooking.md](../recipes_cooking.md) (test prefix `RC-`, jam tests RC-170 to RC-175),
[food.md](../food.md), [vintage_delight.md](../vintage_delight.md) (3.1 overlaps: jams, nut mash),
[item_merges.md](../item_merges.md) 5 (nut butter decision). Recipes through `global.NCH` (`R`) in the Creative Delight
server tree by mod (`kubejs/data/creative_delight/kubejs/<mod>/`), tags through `global.tags`, ids from `global.item`
/ `global.fluid`; food merges through `global.addItemMerges('creative_delight', ...)` in CD startup scripts
(`development/creativeDelight/src/main/resources/kubejs_startup/<mod>/item_merges.js`).

## Scope and order

| Row | Change | Status | Note |
|---|---|---|---|
| `jams #3` | Recipes use jam, never bottled jam or jelly | In Progress | Only the mulberry bread merge is left; do first (KubeJS only) |
| `merges/dishes #7` | Merge nut butter and nut mash | In Progress | KubeJS + lang; owner rules 2026-10-27 settle the survivor |
| `jams #2` | 4 fruit, sugar, lemon slice for 2 jam | In Progress | Pattern built (row 4); applying it to Fruits Delight's 22 jams is open |
| `jams #1` | Merge all jams into one jam jar | In Progress | Fruits Delight and mulberry to mason jars needs Java or a repack: owner |
| `jams #4` | Grape and cherry jam | Testing | Built, shipped 1.1.05; see Built, needs play |

**Jam families today (checked 2026-10-31 against `item_dict.js`, the CD tree and `mods/`):**
- Fruits Delight 0.5.11: 22 `fruitsdelight:<fruit>_jelly` (named "Jam", glass bottle, 2 fruit + sugar + lemon slice in the
  pot -> 1), `<fruit>_jelly_block`, `<fruit>_jello`, `jelly_bread` (renamed **Bread with Jam**, `client_scripts/namechanges.js:301`,
  shipped 1.1.05). All 22 are in `#forge:jams` (`i.t_forge_jams`) through `#fruitsdelight:jelly`
  ([fruitsdelight/tags.js:17](../../kubejs/data/creative_delight/kubejs/fruitsdelight/tags.js)). Orange jam from 4 slices:
  [fruitsdelight/recipes.js:40-46](../../kubejs/data/creative_delight/kubejs/fruitsdelight/recipes.js) (pot -> 1, mixer -> 125 mB `f.orange_jam`).
- Vintage Delight port, `mods/backportedVintage-0.1.6-ce-19.2-09.jar` (installed 2026-10-31 11:27): mason jar jams only.
  The jam and relish bottles and the sweet berry / glow berry jam jars are removed items (CD startup
  `vintagedelight/vintage_items.js:43-48`); their recipes go in
  [vintagedelight/jams.js](../../kubejs/data/creative_delight/kubejs/vintagedelight/jams.js). Kept: Pepper Jam Jar, Relish Jar
  (ingredient, out of the jam tags), new Green Grape / Red Grape / Cherry Jam Jars. Mason jar craft is still
  `vintagedelight:mason_jar` (glass + wooden slab -> 3), not panes.
- Delightful: `jelly_bottle` and `glow_jelly_bottle` are removed items (`_lib/removed_items.js:379-380`).
- Upgrade Aquatic: `mulberry_jam_bottle` (`i.mulberry_jam_bottle`, glass bottle, not removed), `mulberry_jam_block`,
  `mulberry_bread`. Brewin's jams are gone.
- **TODO (texture):** real sprites for the three new jam jars (item and interior texture) to replace the wool
  placeholders (`development/backportedVintage/.../textures/item/{green_grape,red_grape,cherry}_jam_jar.png`,
  `textures/block/*_jam_interior_texture.png`).

---

## jams #1: Merge all jams into one jam jar
Status **In Progress** / Work: KubeJS / patcherMod / resource pack / id `dishes/jams #1`

### Goal
Every jam is a mason jar (mason jars take glass panes; a new empty mason jar texture); each jam recolours to its fruit
(also for the pancake coverings, `dishes/meals #37`).

### Now
Done 2026-10-29 (shipped 1.1.05, changelog "Jams"): Vintage's bottles and duplicate berry jars removed, Delightful's jellies
removed, relish out of the jam tags. Open: Fruits Delight's 22 jams and Upgrade Aquatic's mulberry jam are still glass
bottle items (eating returns the bottle; set in their Java); the mason jar craft still takes glass; no recolour.

### Build
1. Container swap for the 23 bottle jams (decision D1). Repack route: Fruits Delight and Upgrade Aquatic are not CD
   members, so either a new CD repack member per mod (memory `creative-delight-routing`) or a patcherMod mixin on
   their item's `getCraftingRemainingItem` / finish-using to return `i.empty_mason_jar`. Then the pot recipes take
   `i.empty_mason_jar` as the container: `R.replaceInput({ type: 'farmersdelight:cooking', output: i.t_fruitsdelight_jelly }, ...)`
   does not touch the container field, so remove and re-add each pot recipe (row 2 does this anyway).
2. Mason jar from panes: in CD `vintagedelight/recipes.js`, `R.remove({ id: 'vintagedelight:mason_jar' }, e)` then
   `R.shaped(Item.of(i.empty_mason_jar, 3), [' s ', 'p p', 'ppp'], { s: <wooden slab tag key>, p: <glass pane tag key> }, e, 'mason_jar')`
   (add dict keys if missing). New empty jar texture: port resource, needs a port build.
3. Colour per fruit: per-jam textures in the repack, or a tint layer; the jar blocks need per-jam models (the port has one
   model set per jam). Resource-pack and port work.

### Decisions
- **D1 Which jar item stays:** the mason jar (`vintagedelight:empty_mason_jar`); the glass bottle stays the drink bottle.
  Owner still to choose the route: recommend a patcherMod remainder mixin (no repacks of two non-member mods).
- FD's `*_jelly_block` stays beside the placeable jar block (storage vs placed food). Recommend yes.

### Gotchas
- Cans: `canned_*_jam` rows in `can_parity.md` name FD jams; rerun `/cegen` after any id change.
- RC-172 note: a JEI search "jam bottle" still finds Upgrade Aquatic's mulberry jam bottle (scratch r1031).

### Tests
- RC-new1 (proposed, numbered when queued): eating any Fruits Delight jam or the mulberry jam returns an empty mason jar;
  mason jar crafts from panes.

---

## jams #2: Jam crafting: 4 fruit, sugar, lemon slice for 2 jam
Status **In Progress** / Work: KubeJS / id `dishes/jams #2`

### Goal
Cooking pot jam: 4 fruit, 1 sugar, 1 lemon slice for 2 jam (output doubled).

### Now
- The pattern is built for the three new jars (row 4): [vintagedelight/jams.js](../../kubejs/data/creative_delight/kubejs/vintagedelight/jams.js)
  `R.fdCooking([fruit x4, i.any_sugar, i.lemon_slice], Item.of(jar, 2), e, id, 1.0, 400, i.empty_mason_jar)`; Slice & Dice
  copies it to the heated mixer. The container question is settled: the pot output of 2 takes one container per serving
  (shipped 1.1.05, recipe ids scratch PASS r1031 SQ-BJ-01).
- Fruits Delight's 22 jams keep their own 2 fruit -> 1 recipes (`farmersdelight:cooking` type, ids under `fruitsdelight:`),
  Vintage's pepper jam its own; orange keeps the 4-slice recipe (1 jam).

### Build
1. In CD `fruitsdelight/recipes.js` (new block "Jams, 4 fruit for 2"): for each Fruits Delight jam read its fruit from the
   upstream recipe list (or a table `[fruit key, jam key]` written once), `R.remove({ type: 'farmersdelight:cooking', output: jam }, e)`,
   then the call above with `Item.of(jam, 2)` and `i.glass_bottle` (or the mason jar once row 1 lands).
2. Orange: 4 slices = 1 orange's worth today; decide whether it becomes 8 slices -> 2 (consistent) or stays.
3. Upgrade Aquatic mulberry jam: same rule, if it is kept as a jam.

### Decisions
- **Apply to Fruits Delight's jams:** TODO says "the existing jams keep their own recipes" (2026-10-29). Recommend asking the
  owner whether that was final; if so, close the row as done for the new jams.

### Tests
- RC-new2 (proposed, numbered when queued): pot 4 apples + sugar + lemon slice + 2 bottles -> 2 Apple Jam; JEI one pot recipe.

---

## jams #3: Recipes use jam, never bottled jam or jelly
Status **In Progress** / Work: KubeJS / id `dishes/jams #3`

### Goal
Ditch bottled jams; jam always over jelly; merge mulberry bread into bread with jam; rename jelly bread to bread with jam.

### Now
Done: Vintage bottles gone and PB and J takes `#forge:jams` (2026-10-29,
[vintagedelight/cooking.js:21](../../kubejs/data/creative_delight/kubejs/vintagedelight/cooking.js)); Jelly Bread renamed
Bread with Jam (2026-10-30, shipped 1.1.05). Open: `upgrade_aquatic:mulberry_bread` (`i.mulberry_bread`) is still its own
item (compost row in CD startup `upgrade_aquatic/compost_values.js:37`).

### Build
1. CD startup `upgrade_aquatic/item_merges.js` (new, `// requires: upgrade_aquatic`, `// requires: fruitsdelight`):
   `global.addItemMerges('creative_delight', [[i.mulberry_bread, i.jelly_bread]])`. The CD `_core/item_merges.js` run swaps
   recipes, loot and inventories.
2. Drop the mulberry bread compost row (the purged id), and its can if any (`/cegen`).
3. If the merged bread should still be makeable from mulberries: `#forge:jams` holds only the 22 Fruits Delight jams
   (tag dump 2026-10-30, before the new jars), so add `i.mulberry_jam_bottle` with `global.tags` and check that the Bread
   with Jam fillings read the tag rather than per-jam ids.

### Tests
- RC-new3 (proposed, numbered when queued): JEI shows no Mulberry Bread; Bread with Jam from mulberry jam; old stacks swap.

---

## merges/dishes #7: Merge nut butter and nut mash
Status **In Progress** / Work: KubeJS / id `merges/dishes #7` (was `dishes/jams #5`). Also briefed in [merges_dishes.md](merges_dishes.md)
(general merge steps).

### Goal (owner, scratchpad 2026-10-27, [item_merges.md](../item_merges.md) 5)
Remove nut dough. Only the nut mash jar remains: 5 nuts (acorn or peanut) + sugar -> 3 nut mash, needs a mason jar. Nut
butter sandwich: Vintage's texture, Delightful's name; any bread + any jam + a nut mash jar.

### Now (checked 2026-10-31)
- Delightful `nut_butter_bottle` (`i.nut_butter_bottle`; pot `delightful:food/cooking/nut_butter_bottle` from `#forge:nuts`),
  `nut_dough` (`i.nut_dough`, [delightful/recipes.js:69](../../kubejs/data/creative_delight/kubejs/delightful/recipes.js): nut butter + egg -> 3),
  `nut_butter_and_jelly_sandwich` (two upstream crafts).
- Vintage `nut_mash_jar` (pot 5 roasted peanuts + sugar + empty mason jar -> 1, `vintagedelight:cooking/nut_mash_jar`) and
  `nut_mash_bottle` (`jam/nut_mash_jar_deconstruct`: jar + 3 bottles -> 3). `pb_j` (bread + nut mash bottle + any jam,
  [vintagedelight/cooking.js:21](../../kubejs/data/creative_delight/kubejs/vintagedelight/cooking.js)), pad thai (line 35) take the bottle.
- `#forge:nut_butter` (`i.t_nut_butter`) = both bottles ([delightful/tags.js:12](../../kubejs/data/creative_delight/kubejs/delightful/tags.js)).
- `#forge:nuts` holds only `delightful:acorn`; the peanut is not in it. Food dict: `f.sauce_nut_butter`,
  `f.ingredient_nut_mash`, `f.ingredient_nut_mash_jar` (food_dict.js 456, 472-473).

### Build
1. Merges (CD startup `delightful/item_merges.js`, new): `[i.nut_butter_bottle, i.nut_mash_jar]`,
   `[i.nut_mash_bottle, i.nut_mash_jar]`, `[i.nut_butter_and_jelly_sandwich, i.pb_j]` (Vintage's item keeps its texture);
   `global.removedItems.push(i.nut_dough)` and remove its recipe (delightful/recipes.js:69).
2. Recipe (CD `vintagedelight/recipes.js`): remove `vintagedelight:cooking/nut_mash_jar` and the deconstruct craft, then
   `R.fdCooking([n x5, i.any_sugar], Item.of(i.nut_mash_jar, 3), e, 'nut_mash_jar', 1.0, 400, i.empty_mason_jar)` with a nut
   slot of `[i.t_nuts, i.roasted_peanut]` (or add the peanut forms to `#forge:nuts` via `global.tags`: decision D2).
3. Sandwich: `pb_j` becomes any bread + any jam + nut mash jar (`R.shapeless(i.pb_j, [i.t_bread, i.nut_mash_jar, i.t_forge_jams], ...)`,
   jar returned as a remainder or consumed: D3); pad thai takes the jar. Rename `pb_j` to "Nut Butter and Jam Sandwich" in
   `namechanges.js`. `#forge:nut_butter` then holds the jar only.
4. Food dict: drop the purged keys; rerun `/cegen` (cans) and the food catalog.

### Decisions
- **D1 Survivor:** settled by the owner (nut mash jar). Recommend the sandwich survivor be `vintagedelight:pb_j` (texture
  wanted), named as Delightful's.
- **D2 Peanuts:** raw `vintagedelight:peanut` or `roasted_peanut`? Today's recipe uses roasted. Recommend roasted peanut
  or acorn (raw or roasted), by a slot array, not by adding peanuts to `#forge:nuts` (that tag feeds Delightful's other nut recipes).
- **D3 Jar in the sandwich:** a whole jar per sandwich is costly at 3 per pot; recommend the jar is a crafting remainder
  that goes down one serving (needs Java) or simply 1 jar -> 3 sandwiches (`Item.of(i.pb_j, 3)`). Recommend the latter.

### Gotchas
- The jar is a placeable block item; check stack size and the coaster / placeable tags after the merge.
- A merged pair cannot be in two groups: these go in `creative_delight` only.

### Tests
- RC-new4 (proposed, numbered when queued): JEI one nut butter (the jar); pot 5 acorns + sugar + jar -> 3 jars; nut butter and
  jam sandwich from any bread and jam; no nut dough.

---

## Built, needs play

| Row | What | Writeup | Open tiers |
|---|---|---|---|
| `jams #4` | Green Grape, Red Grape, Cherry Jam Jars (Vintage port `-09`, installed in `mods/` 2026-10-31), pot 4 fruit + sugar + lemon slice -> 2, shipped 1.1.05; recipe ids scratch PASS r1031 (SQ-BJ-01) | [recipes_cooking.md](../recipes_cooking.md) RC-173, RC-175 | C P (cooking, placing, eating in game); coaster entries after `/dumpall` + `gen_coaster_models.py` |
