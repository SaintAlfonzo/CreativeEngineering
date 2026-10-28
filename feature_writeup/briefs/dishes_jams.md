# Brief: dishes / jams

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# dishes`, Sub `jams` (rows 1 to 5, all from the scratchpad).
Layout per [README.md](README.md). Writeups: [recipes_cooking.md](../recipes_cooking.md), [food.md](../food.md),
[vintage_delight.md](../vintage_delight.md) (§3.1 overlaps: jams, nut mash). Recipes through `global.NCH` (`R`) in
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js), ids from `global.item` / `global.fluid`; merges in
`kubejs/startup_scripts/_lib/item_merges.js`.

**Jam families today (verified against `item_dict.js` and the jars, 2026-10-24):**
- Fruits Delight (FD) 0.5.11: `fruitsdelight:<fruit>_jelly` (named "Jam", glass bottle container, 1 jam from 2 fruit +
  sugar + lemon slice in the pot), `<fruit>_jelly_block` ("Jam Block", honey-like block), `<fruit>_jello`, `jelly_bread`
  and per-jam `filling/*_jelly_bread` Create recipes.
- Vintage Delight (port): `sweet_berry_jam`, `glow_berry_jam`, `pepper_jam` as `_bottle` items and `_jar` mason-jar blocks
  (`empty_mason_jar`; craft: glass + wooden slab, shaped, `mason_jar.json`); tags `#vintagedelight:sweet_jam_bottles`,
  `savory_jam_bottles`; `forge:jam_jars` / `forge:jam_bottles` are used by its vinegar recipes.
- Delightful: `jelly_bottle` ("Jam Jar"), `glow_jelly_bottle`, `nut_butter_bottle`; tag `#forge:jam` (exports predate Vintage).
- Upgrade Aquatic: `mulberry_jam_bottle`, `mulberry_jam_block`, `mulberry_bread`. Brewin's jams are already gone.

---

## jams #1: Merge all jams into one jam jar
Status **Planned** / Work: KubeJS / patcherMod / resource pack / id `dishes/jams #1`

### Goal
Every jam is Fruits Delight's jam item and Vintage Delight's jam block; every jam jar is a mason jar (mason jars take
glass panes; a new empty mason jar texture); each jam recolours to its fruit (also for the pancake coverings, `dishes/meals #37`).

### Now
Three overlapping jam stacks (above). The only pack jam code is orange jam in `cooking.js` lines 799-804 (pot with
4 slices, mixer with a fluid `f.orange_jam`). The mason jar craft is `vintagedelight:mason_jar` (glass, not panes).

### Build
1. `item_merges.js`: `[i.jelly_bottle, <fd jam>]`-style pairs are not one-to-one (Delightful's is generic); decide first
   (Decisions). Mulberry, sweet berry, glow berry and pepper jams map to FD-style items (new where FD lacks them, row 4).
2. Mason jar craft: change the pane input via `R.remove({id:'vintagedelight:mason_jar'})` + `R.shaped`; or edit the port
   recipe (`development/backportedVintage/.../recipes/mason_jar.json`) and rebuild.
3. Colour: the jam item takes its colour by an item colour hook or per-jam textures; the jar block needs a tinted
   model or per-jam models (the port has one model set per jam). Recolour is a resource-pack and port job.
4. `item_merges` swap the recipes (pb_j: `cooking.js` line 583 uses `#vintagedelight:sweet_jam_bottles`).

### Decisions
- **Which jar item stays.** Options: `vintagedelight:empty_mason_jar` (the row's own wording), Delightful's
  `jelly_bottle`, FD's glass bottle (today's container). Recommend the mason jar; glass bottle remains the drink bottle.
- Whether FD's `*_jelly_block` (storage) stays beside the placeable jar block. Recommend yes (different jobs).

### Gotchas
- Cans: `canned_*_jam`-style cans and `can_parity.md` rows reference FD jams; rerun `/cegen` after merges.

### Tests
- RC-J1 JEI: one jam per fruit; its jar is a mason jar; no Delightful / Mulberry / Vintage duplicate remains.

---

## jams #2: Jam crafting: 4 fruit, sugar, lemon slice for 2 jam
Status **Planned** / Work: KubeJS / id `dishes/jams #2`

### Goal
Cooking pot jam: 4 fruit, 1 sugar, 1 lemon slice (a lemon gives 5 lemon slices) for 2 jam (output doubled).

### Now
FD's own pot recipe is 2 fruit + sugar + lemon slice for 1 jam (`farmersdelight:cooking/apple_jelly`, verified; glass
bottle container). The pack's orange recipe already uses 4 slices (`cooking.js` 799-804, `R.fdCooking(..., i.glass_bottle)`).
`i.any_sugar`, `i.lemon_slice` exist.

### Build
Loop every jam: `R.remove({id:'farmersdelight:cooking/<fruit>_jelly'})`, then
`R.fdCooking([fruit x4, i.any_sugar, i.lemon_slice], Item.of(jam, 2), e, '<fruit>_jam', 0.1, 200, i.glass_bottle)`.
A 2-output pot recipe needs 2 containers: check the pot's container rule (unverified; orange jam does 1 bottle).
Mirror in Create heated mixing (`f.orange_jam` pattern) so the machine route stays better than hand.

### Decisions
- Container for 2 jams: the pot may need one container per output (unverified; read FD 1.2.4 `CookingPotBlockEntity`
  before building). If so, recommend a 2-jam output with a stack of 2 bottles accepted as the container slot's count.

### Tests
- RC-J2 pot: 4 apples + sugar + lemon slice + bottle -> 2 jam (or the decided container result); JEI shows one recipe.

---

## jams #3: Recipes use jam, never bottled jam or jelly
Status **Planned** / Work: KubeJS / id `dishes/jams #3`

### Goal
Ditch bottled jams (mason-jarred jams are the same in crafting); jam always over jelly; merge mulberry bread into bread with
jam; rename jelly bread to bread with jam.

### Now
Consumers: `jelly_bread` (`fruitsdelight:jelly_bread`, "Jelly Bread", FD filling per jam), `upgrade_aquatic:mulberry_bread`,
`pb_j` (`cooking.js` 583, `#vintagedelight:sweet_jam_bottles`), `delightful:nut_butter_and_jelly_sandwich` ("Nut Butter and
Jam Sandwich"), Vintage's vinegar (`jam_jars` + mushroom). Exact recipe list is unverified; grep `kubejs/exported/recipes/all.csv`
(it predates Vintage) and re-dump.

### Build
1. Merge `upgrade_aquatic:mulberry_bread` into `fruitsdelight:jelly_bread` in `item_merges.js` (`i.mulberry_bread` -> `i.jelly_bread`).
2. Rename: `kubejs/assets/*/lang` entry for `fruitsdelight:jelly_bread` -> "Bread with Jam" (and `namechanges.js`).
3. Inputs: `R.replaceInput` every recipe that takes a bottle-jam or jelly with the jam item or tag `#forge:jam`.

### Tests
- RC-J3 JEI: "Bread with Jam" only; mulberry bread gone; no recipe lists a jam bottle or a jelly.

---

## jams #4: Grape and cherry jam
Status **Planned** / Work: KubeJS / data / id `dishes/jams #4`

### Goal
Add grape and cherry jam (grape jelly / jam, cherry jelly / jam). Cherry pie is `baking/baked-goods #13`.

### Now
Fruits: `i.cherry` (`vinery:cherry`), `i.red_grape`, `i.white_grape` (Vinery backport; the port already has grape juices
that are not foods). FD has no grape or cherry jam; Vintage has no such jar either. No fluid, block or filling set exists.

### Build
Add per jam: the item, the placeable jar block, the fluid (`f.orange_jam` pattern), pot and mixer recipes (row 2's
recipe), bread fillings, cans. The pack adds items in `kubejs/startup_scripts/items/`; follow the chorus juice item
(`drinks #4`). Colours per `jams #1`.

### Decisions
- **Jam or jelly names.** Recommend "Grape Jam" and "Cherry Jam" (FD names its jellies "Jam"; row 3 prefers jam).
- Grape: red, white or both? Recommend one Grape Jam from `#vinery:grapes`-style tag (unverified tag), red tint.

### Tests
- RC-J4 pot: 4 cherries + sugar + lemon slice -> cherry jam; same for grapes; bread with jam takes both.

---

## jams #5: Merge nut butter and nut mash
Status **Planned** / Work: KubeJS / id `merges/dishes #7`

### Goal
Merge nut butter and nut mash into one item across the mods.

### Now
- Delightful `nut_butter_bottle` (`i.nut_butter_bottle`, recipe `food/cooking/nut_butter_bottle`, made from `#forge:nuts`, only acorn today per vintage_delight.md 3.1).
- Vintage `nut_mash_bottle` and `nut_mash_jar` (pot: 5 roasted peanuts + sugar in a mason jar; used by `pb_j`
  and `pad_thai`, `cooking.js` lines 583 and 597; `i.nut_mash_bottle`).
- Both feed a "peanut butter and jam" sandwich (Delightful `nut_butter_and_jelly_sandwich`, Vintage `pb_j`;
  `dishes/meals #31` covers the sandwich).

### Build
`item_merges.js` pair `[i.nut_butter_bottle, i.nut_mash_bottle]` (recommended survivor, below); keep one pot recipe; rename in lang; check `can_types`/can rows for both ids.

### Decisions
- **Which survives.** Recommend Vintage's nut mash (jar form, placeable, already in recipes of the pack), renamed Nut
  Butter in lang, with Delightful's merged into it (`[i.nut_butter_bottle, i.nut_mash_bottle]`).

### Tests
- RC-J5 JEI: one nut butter item; PB and J and pad thai accept it; no Delightful nut butter bottle.

