# recipes / materials: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# recipes`, Sub `materials` (rows #1 to #3, all scratchpad items).
There is no feature writeup for this subfamily yet. Proposal: a new `feature_writeup/recipes_materials.md` (prefix
`RS-`, unused so far) in the layout of the other recipe writeups, linked from [recipe_progression.md](../recipe_progression.md)
as the sixth per-folder writeup. The code would be one new server file, `kubejs/server_scripts/other/merges.js`.

Jars read (all in `scratchpad/jars`): Create: Design n' Decor 0.4, 'Dustrial Decor 1.3.3, Wares 1.2.9, Dramatic Doors
3.2.7, Farmer's Delight 1.2.4 (`TomatoVineBlock`, `ModTags` decompiled under `scratchpad/decomp/fd_tomato/`),
Supplementaries 2.4.20 (`ModRegistry`, `RopeArrowEntity` decompiled under `scratchpad/decomp/supp_reg/`), Quark
3.4-418, Twigs 3.0.1, Dave's Building Extended 6.0.1, Caverns & Chasms 1.0.2.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Merge cardboard | Planned | small, KubeJS (startup list + server) |
| #3 | Merge rocky dirts | Planned | small, KubeJS (same pattern as #1) |
| #2 | Merge rope (flax goes) | Planned | medium: KubeJS + 3 config files + loot config; one real bug fixed on the way |

All three are the same job, the "merge" pattern the pack already uses for flour
([cooking.js](../../kubejs/server_scripts/cooking/cooking.js) 76-77):
`R.replaceOutput({ output: old }, old, kept, e)` + `R.replaceInput({ input: old }, old, kept, e)` + `old` in
`global.removedItems` ([removed_items.js](../../kubejs/startup_scripts/_lib/removed_items.js), e.g. 138 `i.out_flour`),
which hides it in JEI, flags its tooltip and strips its tags. Build a small helper once, used by all three rows (and
by `recipes/wood #3`):

```js
// startup_scripts/func/neoCraftHelper.js, next to replaceInput / replaceOutput (~593-605)
// Fold a removed item into the one that stays: every recipe that makes `old` makes `kept`, every recipe that
// takes `old` takes `kept`. `old` must also be in global.removedItems (startup), which hides it and strips its tags.
//   R.merge(i.cardboard_sheet, i.cardboard, e)
merge(old, kept, e) {
  global.NCH._validateAny(old, 'merge old'); global.NCH._validateAny(kept, 'merge kept')
  e.replaceOutput({ output: old }, old, kept)
  e.replaceInput({ input: old }, old, kept)
},
```

Build order: #1 and #3 together (one `/reload` for recipes, plus a restart for the startup list; ride any restart).
Then #2, which changes three mod configs and needs a restart, and fixes the rope pulley bug (#2 Now) in the same
pass.

**Findings:**

- **Bug, rope pulley:** `i.any_rope = '#forge:rope'` (item_dict 681, again at 1578) is singular. The only Forge rope
  tag in the cached jars is `forge:ropes` (Supplementaries, holding `supplementaries:rope`), and no pack script fills
  `forge:rope`. [create_utilities.js](../../kubejs/server_scripts/engineering/create_utilities.js) 55-60 replaces
  the wool in `create:crafting/kinetics/rope_pulley` with `i.any_rope`. If no other mod fills `forge:rope`, the rope
  pulley has no working recipe. Check in game: JEI rope pulley; the rope slot is empty or doesn't cycle. Fixed by
  #2 step 3.
- **#3: Caverns & Chasms is not loaded.** `pack/MODLIST.md` lists `caverns_and_chasms-1.19.2-1.0.2.jar` under
  "Disabled mods (in disabledmods/, not loaded)". The live rocky dirts are Twigs' and Dave's Building Extended's.
  C&C's only matters when `ports/cavesandchasmsport` brings C&C content in.

Related rows: `ports/cavesandchasmsport #4` (C&C block merges, same helper), `loot/tables`
([brief](loot_tables.md), the loot_config.json edits in #2), `farming/crops` (flax is a listed crop in
[crops.md](../crops.md) 92), `cooking/kitchen`, `recipes/wood #3` (same helper), `recipes/progression #2` (every id in
`global.removedItems` costs a recipe pass).

---

## materials #1: Merge cardboard
Status Planned / Work KubeJS / TODO `recipes/materials #1` (scratchpad 2026-10-19)

### Goal
One cardboard item in the pack; the other mod's cardboard recipes and uses point at it.

### Now
- **Verified (jars):** two cardboard *materials*:
  - `dustrial_decor:cardboard` (`i.cardboard`, item_dict 11615): `dustrial_decor:cardboard` = 4 paper (2x2) -> 2.
    It's the base of a whole set: `smooth_cardboard` (8 cardboard in a ring -> 12), `folded_cardboard`, `cardboard_box`
    (decor), slab / stairs / vertical slab, `cardboard_door` (x3), `cardboard_trapdoor`, cardboard armor (helmet,
    chestplate, leggings, boots), `cardboard_scrap` (from `#dustrial_decor:cardboard_blocks`). It is in the dungeon
    chest loot (`loot_config.json`, `dustrial_decor:chests/dungeon` 29.8 %).
  - `design_decor:cardboard_sheet` (`i.cardboard_sheet`, 9004): `design_decor:crafting/cardboard_sheet` = 2 paper
    (vertical) -> 1, used only by `design_decor:crafting/cardboard_box` (4 sheets, 2x2 -> 1 `design_decor:cardboard_box`).
  - Same paper rate (2 paper per cardboard) in both.
- Boxes are separate blocks with different jobs and stay: `wares:cardboard_box` (Wares' delivery storage, 4 paper ->
  2, chest loot in 5 warehouse tables), `design_decor:cardboard_box`, `dustrial_decor:cardboard_box` (decor).
  Dramatic Doors' `short_cardboard_door` / `tall_cardboard_door` (item_dict 11378, 11434) are made from Dustrial's
  door (recipes generated in code; not in the jar's data, **unverified** shape).
- No pack script mentions cardboard except a blockrunner tag ([blocktags.js](../../kubejs/server_scripts/tags/blocktags.js)
  347, Dustrial cardboard blocks).

### Build
1. removed_items.js: add `i.cardboard_sheet` (comment `// merged into dustrial_decor:cardboard (recipes/materials #1)`).
2. merges.js: `R.remove({ id: 'design_decor:crafting/cardboard_sheet' }, e)` (the same 2:1 paper rate as Dustrial's;
   keeping it would just duplicate), then `R.merge(i.cardboard_sheet, i.cardboard, e)`. The merge turns
   `design_decor:crafting/cardboard_box` into 4 Dustrial cardboard -> 1 Design n' Decor box.
3. Optional, the "tags through `global.tags`" half of the row: a `#forge:cardboard` tag holding `i.cardboard`
   (`tags/misctags.js`, `t.add(e, 'forge:cardboard', i.cardboard)`) and a dict key `i.any_cardboard = '#forge:cardboard'`,
   for recipes that should take any future cardboard. Recommendation: add it; it's two lines and other mods (Create
   0.6-era add-ons) use that tag.
4. Optional machine route: Create compacting 2 paper -> 1 cardboard
   (`R.createCompacting(Item.of(i.cardboard), [Item.of(i.paper, 2)], e, 'cardboard_compacting')`; pressing takes one
   item, so it can't do 2:1). Recommendation: only if cardboard gets a real use.

### Ids and data
Kept `dustrial_decor:cardboard`, removed `design_decor:cardboard_sheet`, recipe removed
`design_decor:crafting/cardboard_sheet` (all verified). `i.paper` = `minecraft:paper` (item_dict 1359).

### Decisions
- **Which survives.** Recommendation: Dustrial's. It has the whole block, door and armor family, and loot; Design n'
  Decor's sheet feeds one box.

### Gotchas
- `replaceInput({ input: old })` catches every recipe in every mod, including ones not in the cached jars. The JEI
  check below is the real test.
- The removal must stay before the merge, or the sheet recipe becomes a second paper -> Dustrial cardboard recipe
  (harmless, but a duplicate).

### Tests
- **RS-01 One cardboard:** JEI search "cardboard" -> no Cardboard Sheet; the Design n' Decor cardboard box takes 4
  Dustrial cardboard; paper 2x2 -> 2 cardboard still works.

---

## materials #3: Merge rocky dirts
Status Planned / Work KubeJS / data / TODO `recipes/materials #3` (scratchpad 2026-10-21)

### Goal
One rocky dirt block across the mods.

### Now
- **Verified (jars):**
  - `twigs:rocky_dirt` (`i.twigs_rocky_dirt`, item_dict 10512): `twigs:rocky_dirt` = 2 dirt + 2 `twigs:pebble`
    (diagonal) -> 4; drops itself; shovel and pickaxe mineable. No slab or other uses.
  - `davebuildingmod:rocky_dirt` (`i.rocky_dirt`, 7880): `davebuildingmod:rec_rocky_dirt` = shapeless 2 dirt + 2
    cobblestone -> 4; `davebuildingmod:rocky_dirt_slab` (`i.rocky_dirt_slab`, 7881) = 3 rocky dirt in a row -> 6
    (`rec_rocky_dirt_slab`).
  - `caverns_and_chasms:rocky_dirt` (not loaded, see Findings): 2x2 dirt / cobblestone -> 4, plus an overworld ore
    feature `caverns_and_chasms:ore_rocky_dirt` in `#minecraft:is_overworld`. If C&C is ever enabled, its 2x2 matches
    the same grid as DBE's shapeless recipe.
- Neither live one has worldgen. Twigs' pebbles do (`twigs` biome modifier `add_pebbles`), so the Twigs recipe uses a
  world material.
- Pack use: blockrunner path tag only ([blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) 253 lists DBE's
  rocky dirt and slab by regex and `item.twigs_rocky_dirt`). After a merge both may stay in it (removed blocks can
  still be placed in old builds).

### Build
1. removed_items.js: `i.rocky_dirt` (DBE). Keep the DBE slab (see Decisions).
2. merges.js: `R.merge(i.rocky_dirt, i.twigs_rocky_dirt, e)`. That turns DBE's 2 dirt + 2 cobble recipe into a second
   Twigs rocky dirt recipe (a cobble route next to the pebble one) and its slab recipe into 3 Twigs rocky dirt -> 6
   slabs.
3. Optional: a 1:1 shapeless DBE rocky dirt -> Twigs rocky dirt for old inventories
   (`R.shapeless(Item.of(i.twigs_rocky_dirt), [i.rocky_dirt], e, 'rocky_dirt_unify')`). Mind that `merge` would
   rewrite its input; add it after the merge, or skip it.
4. Dict: rename for clarity while there: `i.rocky_dirt` -> keep as is (other code may use it) but add
   `i.dbe_rocky_dirt` as an alias and comment the merge. Don't rename keys in this row (`tooling/conventions`).

### Ids and data
Kept `twigs:rocky_dirt`; removed `davebuildingmod:rocky_dirt`; kept `davebuildingmod:rocky_dirt_slab` (all verified).

### Decisions
- **Which survives.** Recommendation: Twigs'. It has the pebble recipe tied to worldgen and is the better-known block;
  DBE's cobble recipe survives as a second route through the merge.
- **The DBE slab** has DBE's texture, not Twigs'. Options: keep it (slightly off texture next to Twigs blocks), retexture
  it to Twigs' through `kubejs/assets/davebuildingmod/textures/block/` (an asset override, client restart), or remove
  it too. Recommendation: keep it and retexture to match.
- **C&C's rocky dirt** when C&C content arrives: merge it into Twigs' with the same helper, and drop its ore feature
  (or keep it as a Twigs rocky-dirt ore through a datapack placed-feature override). Leave it to
  `ports/cavesandchasmsport #4`.

### Gotchas
- `global.removedItems` strips only *item* tags (recipe_scan.js 50-54). `#minecraft:mineable/*` are block tags, so a
  placed DBE rocky dirt still mines normally.

### Tests
- **RS-02 One rocky dirt:** JEI "rocky dirt" -> only Twigs' (pebble and cobblestone recipes) and DBE's slab (made from
  Twigs' rocky dirt).

---

## materials #2: Merge rope
Status Planned / Work KubeJS / data (+ config) / TODO `recipes/materials #2` (scratchpad 2026-10-19)

### Goal
One rope. Flax goes. Rope arrows use the surviving rope, and tomato vines keep climbing it (Farmer's Delight's
behaviour).

### Now
- **Three ropes (verified, jars):**
  - `farmersdelight:rope` (`i.fd_rope`, item_dict 195): 2 straw (vertical) -> 4. Chef trade sells 9 for an emerald
    ([trades.js](../../kubejs/server_scripts/economy/trades.js) 70). Chest loot (`loot_config.json`): shipwreck supply
    84 %, abandoned mineshaft 70.4 %, simple dungeon 70.4 %.
  - `supplementaries:rope` (`i.supplementaries_rope`, 3675): `supplementaries:rope` = 3 `#forge:crops/flax`
    (vertical) -> 3, conditions `flag rope` AND `flag flax`; `supplementaries:rope_2` = 3 string (diagonal) -> 3,
    condition `NOT flag flax` AND `flag rope`. Mineshaft loot 35 %.
  - `quark:rope` (`i.quark_rope`, 3405): 6 string (2x3) -> 3, `quark:flag rope`; Quark `[building] Rope = true`
    (config/quark-common.toml 120).
- **Tomatoes (verified, decompiled `TomatoVineBlock`):** a vine climbs the block above if
  `enableTomatoVineClimbingTaggedRopes` is on and it is in `#farmersdelight:ropes`, otherwise only
  `farmersdelight:rope`. Mining a rope-logged vine leaves `defaultTomatoVineRope`. Pack config
  (`config/farmersdelight-common.toml` 24-28): `defaultTomatoVineRope = "farmersdelight:rope"`,
  `enableTomatoVineClimbingTaggedRopes = true`. FD's block tag `farmersdelight:ropes` = FD rope + `quark:rope` +
  `supplementaries:rope` (optional entries). So tomatoes already climb all three, and only the left-behind rope is
  FD's.
- **Rope arrows (verified, decompiled `RopeArrowEntity`):** the arrow places `CommonConfigs.getSelectedRope()`, i.e.
  `config/supplementaries-common.toml` 92 `[functional.rope] rope_override = "supplementaries:rope"`. Knots on
  fences/posts only work with Supplementaries' own rope (`ropeBlock == ModRegistry.ROPE.get()`). The arrow recipes
  are special types (`supplementaries:rope_arrow_create` / `rope_arrow_add`). The item tag `supplementaries:ropes`
  holds all three ropes and `#forge:ropes`.
- **Flax (verified):** Supplementaries registers flax, flax seeds, wild flax, potted flax and the flax block whatever
  the config says (`ModRegistry` 1035-1060). `[functional.flax] enabled` (supplementaries-common.toml 174-175) only
  gates the creative tab (`RegUtils.getTab`) and the recipes flagged `flax`: `strings` (flax -> string), `sack`,
  `rope`, `doormat_2`, `flax_block`, `flax_block_uncrafting`. Fallbacks that switch on when flax is off: `rope_2`
  (string rope) and `sack_2` (wheat sack). **There is no flax-free doormat recipe**, so `supplementaries:doormat`
  (`i.doormat`, 1518) would lose its only recipe. Wild flax worldgen is already off (`[functional.flax.wild_flax]
  enabled = false`; [crops.md](../crops.md) 458). Flax seeds still come from 4 chest tables
  (`supplementaries:inject/{dungeon,mineshaft,pillager,shipwreck_storage}_flax`; loot_config.json
  `supplementaries:flax_seeds`: pillager outpost 100 %, shipwreck supply 20 %, dungeon 19 %, mineshaft 14 %). GLM
  `supplementaries:straw_from_flax` (knife on ripe flax -> straw) is flag-gated. The pack already swaps the fodder
  recipe's flax block for a rice bale + straw bale ([misc_mods.js](../../kubejs/server_scripts/other/misc_mods.js)
  25-27). Compost values: flax seeds, flax, wild flax, flax block (compost_values.js 55, 341-342, 527).
- **Bug:** `i.any_rope = '#forge:rope'` (see Findings). The real tags are `#forge:ropes` (item, Supplementaries) and
  `#supplementaries:ropes`.

### Build
1. **Configs:**
   - `config/supplementaries-common.toml`: `[functional.flax] enabled = false` (flax recipes off, `rope_2` and
     `sack_2` on). Keep `rope_override = "supplementaries:rope"`.
   - `config/farmersdelight-common.toml`: `defaultTomatoVineRope = "supplementaries:rope"` (the vine leaves the kept
     rope; climbing tagged ropes is already on).
   - `config/quark-common.toml`: leave `Rope = true` if Quark's rope stays registered for old worlds and is just
     hidden (recommended: the flag also gates Quark's rope recipe, which the merge removes anyway). Turning the module
     off can change Quark behaviour beyond the recipe (**unverified**: whether disabled-module items stay registered
     in Quark 3.4).
   - `config/` is what ships (`tools/build_deploy.py` header: `config/` files, filtered by CONFIG_CLASSES).
     `configs_edited/root_config/` holds hand-edited copies (a quark-common.toml among them). Update the matching copy
     there too, if one exists, so the two don't drift.
2. **removed_items.js:** `i.fd_rope`, `i.quark_rope`, `i.flax`, `i.flax_seeds`, `i.wild_flax`, `i.flax_block`.
3. **Dict:** fix `i.any_rope` to `'#forge:ropes'` (both definitions, 681 and 1578; the second wins at load). Add
   `i.rope = i.supplementaries_rope` as the pack's "the rope" key.
4. **merges.js:**
   ```js
   // Rope: Supplementaries' is the one rope (rope arrows, knots, mineshafts); FD's and Quark's fold into it.
   R.replaceOutput({ id: 'farmersdelight:rope' }, i.fd_rope, i.rope, e)    // keep the straw route: 2 straw -> 4 rope
   R.remove({ id: 'quark:building/crafting/rope' }, e)                      // 6 string -> 3, worse than rope_2
   R.merge(i.fd_rope, i.rope, e)
   R.merge(i.quark_rope, i.rope, e)
   // Doormat: its only recipe needed flax
   R.shaped(Item.of(i.doormat), ['SS'], { S: i.straw }, e, 'doormat')     // 2 straw, the flax recipe's shape
   ```
   The fodder replacement in misc_mods.js stays as is.
5. **Tags** (`tags/misctags.js`, through `global.tags`): put `i.rope` in `forge:ropes` (it's there from
   Supplementaries already; the line documents it). Strip FD's and Quark's rope from `farmersdelight:ropes`? Not
   needed: tags of removed items are item tags. The block tag still lets vines climb old placed ropes, which is fine.
6. **Trades:** trades.js 70 `i.fd_rope` -> `i.rope`.
7. **Loot** (`kubejs/server_scripts/loot/loot_config.json`, then `/reload`; the loot brief's conventions): set
   `"remove": true` on `supplementaries:flax_seeds` and `farmersdelight:rope`. FD's rope is common in shipwreck,
   mineshaft and dungeon chests. To keep rope in them, swap it instead with a LootJS
   `replaceLoot(i.fd_rope, i.rope, true)` in a `loot/` script ([00_lib.js](../../kubejs/server_scripts/loot/00_lib.js)
   9 lists `.replaceLoot`). Recommendation: the swap; rope in those chests fits.
8. **Compost:** leave the flax rows (removed items composting is harmless) or drop them. Recommendation: leave.
9. Docs: the new recipes_materials.md (rope section), [crops.md](../crops.md) (flax removed: the crop table row and
   the wild-crops note), [recipes_engineering.md](../recipes_engineering.md) if it lists the rope pulley.

### Ids and data
Kept `supplementaries:rope`; removed `farmersdelight:rope`, `quark:rope`, `supplementaries:{flax,flax_seeds,wild_flax,
flax_block}`; recipes `farmersdelight:rope` (output swapped), `quark:building/crafting/rope` (removed),
`supplementaries:rope_2` (activated by config), new `creativeengineering:shaped/doormat`. Config keys above (all
verified in the files and jars).

### Decisions
- **Which rope wins.** Recommendation: Supplementaries'. Rope arrows and fence knots are built around it, it slides,
  attaches to block sides and fills mineshafts, and FD's config makes tomatoes use it with one line. FD's rope would
  lose knots on rope arrows. Quark's rope is the odd one out (string-only, block pulling).
- **Rope sources once flax is gone.** Recommendation: straw (FD's recipe, swapped to the kept rope, 2 -> 4) and string
  (Supplementaries' `rope_2`, 3 -> 3). Straw keeps rope a farming by-product.
- **Doormat.** Recommendation: 2 straw (the flax recipe's shape).

### Gotchas
- Placed FD ropes and Quark ropes in old worlds stay as blocks; only the items are hidden. Vines still climb them
  (block tag).
- `enableRopeReeling` (FD) is FD-rope only. Reeling goes away with FD's rope; Supplementaries' rope has its own pull-up
  (right-click with a rope).
- The FD `straw_harvesters` GLM on flax dies with the flag. Straw still comes from wheat and rice on the cutting
  board.
- `recipes/progression #2`: six more ids in `global.removedItems`.

### Tests
- **RS-03 One rope:** JEI "rope" -> only Supplementaries' rope (2 straw -> 4, 3 string -> 3); no flax anywhere; the rope
  pulley takes the rope (the `#forge:ropes` fix).
- **RS-04 Tomatoes:** a tomato vine under a Supplementaries rope climbs it; breaking a rope-logged vine leaves a
  Supplementaries rope.
- **RS-05 Rope arrow:** craft a rope arrow with the rope, shoot it at a fence -> rope knot and rope hanging down.
- **RS-06 Doormat and sack:** 2 straw -> doormat; the wheat sack recipe shows.

---

## Built, needs play

None in this subfamily.

## Writeup corrections made

None. No writeup covers this subfamily yet.
