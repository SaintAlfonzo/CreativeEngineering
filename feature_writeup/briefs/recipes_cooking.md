# recipes / cooking: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# recipes`, Sub `cooking`. Feature writeup:
[recipes_cooking.md](../recipes_cooking.md) (test prefix `RC-`; the writeup ends at RC-18, and the food briefs propose
RC-19 to RC-49, so this brief starts at RC-50: renumber at build time if those are taken). Code:
[cooking.js](../../kubejs/server_scripts/cooking/cooking.js) (live code 1-433; 436-852 is the commented-out
`global.CE` copy).

Jars read for this brief: Quark 3.4-418, Nether's Delight 3.1, Farmer's Delight 1.2.4, Neapolitan 4.1.0, Create 0.5.1.i,
Quark Delight 1.0.0 (all in `scratchpad/jars`).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Smoker recipe | Done 2026-10-22 (comment only) | **probably nothing to build**: Quark makes the vanilla smoker; confirm with RC-01 |
| #2 | Vanilla cake removed | Done 2026-10-22 (nothing built; the baking overhaul covers it) | **nothing to build**: three mod recipes and the oven make `minecraft:cake`; folds into `baking/baked-goods #1` |
| #3 | Comment and id fixes | Planned | tiny, plus a cleanup: four of the "replaced" FD recipes are identical to FD's own |

**Findings that change the rows:**

- **#1: the smoker has recipes.** Quark's Variant Furnaces module is on (`config/quark-common.toml` 127,
  `[building] "Variant Furnaces" = true`), and it ships `quark:building/crafting/furnaces/deepslate_smoker` and
  `.../blackstone_smoker`: a deepslate or blackstone furnace in a plus of 4 `#minecraft:logs`, giving
  `minecraft:smoker`. The pack removes only `minecraft:smoker` (cooking.js 13), so both stay. That matches the
  blast furnace comment in process.js 10 ("require deepslate or blackstone recipe"). Quark ships
  `deepslate_blast_furnace` / `blackstone_blast_furnace` too, so `recipes/engineering #1` and `recipes/progression #1`
  are in the same position (reported to the caller, not edited here).
- **#2: `minecraft:cake` is made four ways.** `farmersdelight:cake_from_milk_bottle` and `neapolitan:cake` (both
  vanilla's grid with `#forge:milk` / `#forge:eggs`), `create:crafting/curiosities/cake` (milk bucket, 2 sugar, egg,
  `#forge:dough`) and the oven's `creativeengineering:baking/cake` (baking.js 96). Vanilla's recipe was a strict subset
  of FD's and Neapolitan's.
- **#3: FD's grilled salmon, bacon and eggs, salmon roll and cod roll are already shapeless,** with the same
  ingredients the pack re-adds. So the "replace shaped with shapeless" blocks only rename the recipe ids.

Build order: #3 in one `/reload`. #1 and #2 need only the in-game checks (RC-01, RC-02) and then closing. `baking/baked-goods
#1` step 3 ([brief](baking_baked-goods.md)) will replace the cake recipes anyway.

---

## cooking #1: Smoker recipe
Status Planned / Work KubeJS / TODO `recipes/cooking #1` ([recipes_cooking.md](../recipes_cooking.md) F1)

### Goal
A player can make a smoker before the Nether, because smoking makes bread blocks, sourdough bread and leather
(prepared flesh, [recipes_engineering.md](../recipes_engineering.md) RE-19).

### Now
- cooking.js 12-13: `//Remove stone smoker recipe` / `R.remove({ id: 'minecraft:smoker' }, e)`. Nothing else in
  `kubejs/server_scripts` or `kubejs/data` touches smoker recipes. The old `global.CE` copy, cooking.js 573-575,
  read "Remove smoker recipes minus blackstone" and also removed `quark:building/crafting/furnaces/deepslate_smoker`.
  The live code dropped that second removal.
- **Verified (jar + config), Quark 3.4-418:** `quark:building/crafting/furnaces/deepslate_smoker` is
  `[" # ", "#X#", " # "]` with `#` = `#minecraft:logs` and `X` = `quark:deepslate_furnace`, giving `minecraft:smoker`.
  `blackstone_smoker` is the same with `quark:blackstone_furnace`. Both are conditional on `quark:flag
  variant_furnaces`, which `[building] "Variant Furnaces" = true` turns on (config/quark-common.toml 127; same in
  configs_edited/root_config/quark-common.toml). `quark:deepslate_furnace` = 8 `minecraft:cobbled_deepslate` in a ring
  (`.../deepslate_furnace`). Dict keys exist: `i.deepslate_furnace` (item_dict 3009), `i.blackstone_furnace` (2898),
  `i.smoker` (2683).
- **Verified (jar), Nether's Delight 3.1:** `nethersdelight:nether_brick_smoker` (its own block) = 4 nether bricks
  around `nethersdelight:blackstone_furnace` (8 blackstone). Nether-gated.
- Campfires cook single items (`minecraft:campfire_cooking`), but bread blocks and sourdough are `minecraft:smoking`
  recipes only.

### Build
1. Nothing to add if RC-01 shows the two Quark recipes. Fix the comment at cooking.js 12 to say where the smoker comes
   from:
   ```js
   // Vanilla's cobblestone smoker removed: the smoker comes from Quark's deepslate / blackstone furnace + 4 logs
   // (quark:building/crafting/furnaces/{deepslate,blackstone}_smoker, Variant Furnaces on). Same rule as the
   // blast furnace (engineering/process.js).
   R.remove({ id: 'minecraft:smoker' }, e)
   ```
2. Docs: after RC-01, mark recipes_cooking.md F1 resolved (its text already names the Quark recipes).
3. If you want the progression to say so in game, add a tooltip line on `i.smoker` in
   [tooltips.js](../../kubejs/client_scripts/tooltips.js) ("Crafted from a deepslate or blackstone furnace").
   Optional.

### Ids and data
`quark:building/crafting/furnaces/deepslate_smoker`, `quark:building/crafting/furnaces/blackstone_smoker` (verified).

### Decisions
- **Keep the deepslate smoker, or blackstone only as the old code had it.** Recommendation: keep both. Deepslate
  is the "go underground first" gate. Blackstone only would put bread blocks and leather behind the Nether, too late
  for food basics.
- **Should a Quark config change ever turn off Variant Furnaces,** both the smoker and the blast furnace lose their only
  recipe. Recommendation: note it in the comment (above). No pack fallback needed.

### Gotchas
- The recipes are in Quark's jar, not the pack, so a grep of `kubejs/` finds nothing. That is how F1 came to be.
- `recipes/progression #1` lists the smoker among items with no recipe. Once RC-01 passes, drop it there.

### Tests
- **RC-01** (existing, now with an expected answer): JEI `minecraft:smoker` -> two Quark recipes (deepslate furnace,
  blackstone furnace, 4 logs each) and no cobblestone one.
- **RC-50 Smoker by hand:** craft 8 cobbled deepslate -> deepslate furnace; with 4 oak logs -> smoker; smoke a dough
  block -> bread block.

---

## cooking #2: Vanilla cake removed
Status Planned / Work KubeJS / TODO `recipes/cooking #2` ([recipes_cooking.md](../recipes_cooking.md) F2)

### Goal
Confirm that `minecraft:cake` stays craftable with vanilla's recipe gone, and close F2.

### Now
- cooking.js 355-357: `//Cake` / `// Redundant cake recipe` / `R.remove({ id: 'minecraft:cake' }, e)`.
- **Verified (jar)**, recipes that make `minecraft:cake`:
  - `farmersdelight:cake_from_milk_bottle`: `mmm / ses / www`, `m` = `#forge:milk`, `s` = sugar, `e` =
    `#forge:eggs`, `w` = wheat;
  - `neapolitan:cake`: the same grid (`AAA / BEB / CCC`, `#forge:milk`, sugar, `#forge:eggs`, wheat);
  - `create:crafting/curiosities/cake`: `" M " / "SES" / " P "`, milk bucket, 2 sugar, `#forge:eggs`, `#forge:dough`;
  - `creativeengineering:baking/cake`: oven, 3 milk + cake base, cake pan (baking.js 96, [oven.md](../oven.md)).
  - Vanilla's own was 3 milk buckets, 2 sugar, egg, 3 wheat: covered by the first two, which take bottles as well.
- `baking/baked-goods #1` step 3 ([brief](baking_baked-goods.md), "Cakes still built from egg + sugar + wheat/dough")
  plans to replace FD's, Neapolitan's and Create's cake recipes with a shaped cake-base recipe.

### Build
Nothing now. Keep the removal, and change the comment to "Vanilla's cake: FD's cake_from_milk_bottle and
neapolitan:cake are the same grid with milk tags; baking/baked-goods #1 replaces all hand cakes". Close the row after RC-02.
FD and Neapolitan's two identical grids conflict with each other, which is harmless (one wins). Leave that to
baked-goods #1, which removes both.

### Ids and data
As above (verified).

### Decisions
- **Keep the removal.** Recommendation: yes (the row's own default).

### Gotchas
- If baked-goods #1 lands first and removes FD's, Neapolitan's and Create's cake, the oven and the new
  `shaped/cake_cake_base` recipe must both exist before the release, or the cake becomes oven-only.

### Tests
- **RC-02** (existing, expected answer): JEI `minecraft:cake` -> FD's, Neapolitan's, Create's crafting recipes and
  the oven's; no `minecraft:cake` recipe id.

---

## cooking #3: Comment and id fixes
Status Planned / Work KubeJS / TODO `recipes/cooking #3` ([recipes_cooking.md](../recipes_cooking.md) F6)

### Goal
Make the comments match the code, and give the crab bar recipe a clean id. While there, drop four re-adds that only
copy FD's own recipes.

### Now
- cooking.js 241: `R.shapeless(Item.of(i.crab_bars, 2), [i.cooked_fish, i.cooked_crab, i.bonemeal], e,
  'creativeengineering/crab_bars_shapeless_manual_only')`, so the id is
  `creativeengineering:shapeless/creativeengineering/crab_bars_shapeless_manual_only`. It is the only idOverride with a
  slash in `kubejs/server_scripts` (grep `e, '[^']*/[^']*'`). Nothing references it (the old copy at 704 is
  commented out). The `_manual_only` suffix matters: Create's mixer skips shapeless recipes whose id path ends with it
  (recipes_cooking.md 3).
- cooking.js 257-260 grilled salmon (`// Replace shaped recipe w/ shaped`), 278-281 bacon and eggs (same comment),
  289-292 salmon roll and 298-301 cod roll (`//Make shapeless, already shapeless?`).
- **Verified (jar, FD 1.2.4):** `farmersdelight:grilled_salmon` is `crafting_shapeless` [`#forge:cooked_fishes/salmon`,
  sweet berries, bowl, `#forge:crops/cabbage`, `#forge:crops/onion`]. `bacon_and_eggs` is shapeless [2 cooked bacon,
  bowl, 2 `#forge:cooked_eggs`]. `salmon_roll` / `cod_roll` are shapeless [2 slices, cooked rice] -> 2. The pack's
  re-adds use the same dict keys (`i.cooked_salmon` = `#forge:cooked_fishes/salmon`, `i.cabbage` =
  `#forge:crops/cabbage`, `i.onion` = `#forge:crops/onion`, `i.cooked_eggs` = `#forge:cooked_eggs`, ...), so each
  pair (`R.remove` + `R.shapeless`) gives the same recipe under the id `creativeengineering:shapeless/<toId>`. The
  sequenced assemblies next to them are new and stay.
- Quark Delight's `quarkdelight:crab_bars` (removed at 240) is shaped: crab leg, raw fish, bone meal -> 4. The
  comment "Remove raw shapeless crab bar recipe" is wrong in the same way.
- [recipes_cooking.md](../recipes_cooking.md) 2.3 said "FD's shaped removed" for grilled salmon and bacon and eggs
  (corrected today).

### Build
1. cooking.js 241: idOverride `'crab_bars_manual_only'`, giving `creativeengineering:shapeless/crab_bars_manual_only`.
   Comment at 239: "Quark Delight's raw crab bars (shaped, x4) replaced by a cooked shapeless x2 (hand only) and a raw
   pot recipe x4".
2. cooking.js 257-260, 278-281, 289-292, 298-301: delete the `R.remove` + `R.shapeless` pairs and keep FD's recipes
   (ids go back to `farmersdelight:grilled_salmon` etc.). Leave a one-line comment over each assembly, e.g.
   `// Grilled salmon: FD's shapeless kept; sequenced assembly added`. If you'd rather keep the pack-owned ids, keep the
   pairs and just fix the comments to "FD's shapeless recipe re-added under a pack id". Recommendation: delete. Less
   code, and FD's ids are the ones other mods and JEI bookmarks know.
3. Optional, same file (recipes_cooking.md F3): line 9 `i.catfish = 'alexsmobs:raw_catfish'` repeats item_dict.js
   47, which already holds the same value. Delete the line.
4. Docs: recipes_cooking.md F6 -> resolved; 2.3 table already corrected.

### Ids and data
No new ids. Renamed: `creativeengineering:shapeless/creativeengineering/crab_bars_shapeless_manual_only` ->
`creativeengineering:shapeless/crab_bars_manual_only`. Back to FD: `farmersdelight:{grilled_salmon,bacon_and_eggs,
salmon_roll,cod_roll}`.

### Decisions
- **Delete the identical re-adds or keep them under pack ids.** Recommendation: delete (above).

### Gotchas
- Keep `_manual_only` on the crab bar id. Without it the mixer gets a crab bar recipe that undercuts the pot's x4.
- Removing the four pairs also removes four `creativeengineering:shapeless/...` ids. Grep `feature_writeup/` and
  `kubejs/` for the generated names before deleting (none found on 2026-10-21).

### Tests
- **RC-51 Crab bars id:** JEI crab bars -> shapeless x2 with id `creativeengineering:shapeless/crab_bars_manual_only`;
  the mixer doesn't offer it.
- **RC-52 FD meals:** JEI grilled salmon, bacon and eggs, salmon roll, cod roll -> one shapeless recipe each under the
  `farmersdelight:` id, plus the pack's sequenced assembly.

---

## cooking #4: 12 shulker bowls from a shell
Status: Planned / Work: KubeJS / TODO `recipes/cooking #4` ([recipes_cooking.md](../recipes_cooking.md))

### Goal
A shulker shell crafts into 12 shulker bowls.

### Now
`endersdelight:shulker_bowl` (`i.shulker_bowl`, item_dict line 141): one shapeless recipe, 1 `minecraft:shulker_shell` -> 1
bowl (verified in the jar, `shulker_bowl.json`). The pack's `chorus_stew` and `endermite_stew` grid recipes take it
(cooking.js lines 448-449); Ender's Delight's stuffed shulker bowl is a separate item.

### Build
`R.remove({ id: 'endersdelight:shulker_bowl' }, e)` and `R.shapeless(Item.of(i.shulker_bowl, 12), [i.shulker_shell], e,
'shulker_bowl')` (confirm the shell key in `item_dict`). Check the wooden-bowl variants of Ender's Delight stews are
untouched.

### Decisions
None; 12 is the user's number. Check balance with `meals #5` feasts (stuffed shulker feast).

### Tests
RC-151 Shell -> 12 shulker bowls; the 1-bowl recipe is gone.

---

## Built, needs play

None in this subfamily.

## Writeup corrections made

- [recipes_cooking.md](../recipes_cooking.md) F1: another agent had already added the Quark recipes on 2026-10-21
  (and the 0 "Removals" row). Its last sentence still said "a player needs another smoker ... or loot", so that
  sentence was changed to say the Quark recipes cover the early game, with a link to this brief, and the F1 heading
  "has no recipe. Your call." became "recipe is removed; Quark's make the smoker".
- [recipes_cooking.md](../recipes_cooking.md) F2: added the four `minecraft:cake` recipes found in the jars.
- [recipes_cooking.md](../recipes_cooking.md) 2.3: "shapeless and assembly (FD's shaped removed)" -> FD's own recipes
  are shapeless and the pack re-adds the same ones; F6 notes it.
- [recipes_cooking.md](../recipes_cooking.md) F6: answered "already shapeless?" (yes, all four FD recipes are
  shapeless with the same ingredients).
