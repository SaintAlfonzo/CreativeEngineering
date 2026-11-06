# creativedelight / framework: implementation brief

Written 2026-10-25 from [creative_delight.md](../creative_delight.md); swept 2026-10-31 against
[TODO](../todo/TODO.md) `# Creative Delight`, Sub `framework`. Test prefixes: `CDT-` (creative_delight.md 19.2, last
CDT-13), `FFH-` / `FF-` ([food_families.md](../food_families.md), last FFH-03, FF-14).

## Scope and order

| Step | Row | Status | Work | Notes |
|---|---|---|---|---|
| 1 | `framework #6` move-diff harness, dump half | In Progress | tool | Gate for member moves and for `#4`'s pilot; also serves SQ-FFH-02 (recipe dump diff) |
| 2 | `framework #4` food dict remainder | In Progress | KubeJS / tool | `tool_dict.js`, `misc_dict.js`, Veggies pilot (needs step 1) |
| 3 | `framework #12` hand-written family rows onto `global.CD.food` | Planned | KubeJS | Changes xp and stations: changelog bullet. After `baking/baked-goods #37` (the `assemble` array bug) if assembly rows move |
| 4 | `framework #7` generators into the CD tree | In Progress | tool | Mechanism built; blocked on the JSON-by-path decision |
| 5 | `framework #3` MIT notices | In Progress | docs / owner | Only Oceanic left (owner decision) |
| - | `framework #9` donor download | Pinned | tool | Nothing until the owner lifts the pin |

Done and archived: `#2` install (before the 1.1.03 build, 2026-10-26). Testing rows are in the table at the end.

---

## framework #6: Move-diff harness: no id, recipe or tag lost
Status In Progress / Work tool / TODO `creativedelight/framework #6`

### Goal
Prove a move lost nothing: registry names, recipes (type, result, ingredients), tag contents and loot table ids are the
same before and after, apart from the expected `creativeengineering:` -> `creativedelight:` recipe id namespace (Q24).

### Now
- **Offline half built 2026-10-25:** [tools/cd_move_diff.py](../../tools/cd_move_diff.py) (184 lines), `snapshot`
  / `diff`: code lines of the scanned script trees as a multiset, data files by content hash (creative_delight.md 27).
  Snapshot default `tools/creative_delight_test/move_diff_snapshot.json`. The script header lists what it does not
  cover: registry names, recipe ids and counts, tags, loot ids.
- Dumps the dump half can read (all in `kubejs/exported/`, refreshed by `/dumpall`; last 2026-10-30):
  `recipes/all.csv` (id, type, mod; 30096 lines), `recipes/_summary.json` (counts per mod and type),
  `food/recipes.json` (`{count, unreadable, recipes}`: id, type, `in`, `out` for food recipes), `tags/*_tags.json`,
  `loot_tables/<mod>.json` (216 files), `items_by_mod/<mod>.json`.
- No recipe dump diff tool exists; SQ-FFH-02 ("diff the recipe dump against the 10-26 one") waits on one.

### Build
1. `cd_move_diff.py snapshot --dumps` adds a `dumps` block: item and block ids from `items_by_mod/`, recipe id -> type
   from `recipes/all.csv`, recipe id -> sorted `in` / `out` from `food/recipes.json`, tag -> sorted members from
   `tags/item_tags.json` / `block_tags.json` / `fluid_tags.json`, loot ids from `loot_tables/`.
2. `diff` on the dump block: LOST for a removed id, tag member or loot id; CHANGED for a recipe whose type, result or
   inputs differ. Map ids across the namespace change by `(type, out, in)` before calling one lost.
3. A `--allow FILE` list for intended removals (a move that also drops a duplicate), so a real move can pass.
4. creative_delight.md 27 gains the dump half; the script header drops its "Not covered" paragraph.

### Gotchas
- Dumps are large: parse, never print. Each snapshot needs a `/dumpall` in game; the "before" one must be taken before
  the move it guards.
- `food/recipes.json` covers food recipes only; non-food recipes are compared by id and type from `all.csv`.

### Tests
- CDT-new1 (proposed, numbered when queued): the harness on two copies of the same dump passes; with one recipe removed, one tag member dropped
  and one recipe renamed `creativeengineering:` -> `creativedelight:`, it reports exactly the first two.

---

## framework #4: Build the food dict (category, form, state keys with subdict masks)
Status In Progress / Work KubeJS / tool / TODO `creativedelight/framework #4` (Tests S~)

### Goal
`global.food` (`f`), `global.tool` (`t`), `global.misc` (`m`): masks over `global.item`, keys `<category>[_<form>][_<state>]_<name>`.

### Now
- Food dict built 2026-10-25 and unchanged since: `kubejs/startup_scripts/_lib/food_grammar.js` (grammar,
  `global.subdict.parse / check`), `_lib/food_dict.js` (1542 `f.<key> = i.<key>` lines, checked 2026-10-31),
  `tools/food_dict.py` (lint, coverage, reverse index, `/cegen run food_dict`), `tools/test_food_dict.sh`. As built:
  creative_delight.md 13.5.
- CDT-08, CDT-10 PASSED offline; SQ-FD-01 log half PASSED (2026-10-25 late run); the `/kubejs eval` value check not run.
- **No script reads `global.food` yet** (grep of `kubejs/` and the CD tree, 2026-10-31). `tool_dict.js` and
  `misc_dict.js` do not exist.

### Build (left)
1. `_lib/tool_dict.js` and `_lib/misc_dict.js` with the same check (`global.subdict.check(t, 'tool')`); key rules first
   (creative_delight.md 13.3 examples).
2. Pilot Veggies: rewrite the cooking calls in `kubejs/data/creativedelight/kubejs/veggiesdelight/recipes.js` to `f.*`
   (CD scripts alias `f`; pack-folder scripts `fd`), then diff recipes with `#6`'s dump half (CDT-09).

### Decisions
- Juice keys: **`fruit_juice_`** (owner 2026-11-01; was the default `drink_juice_`, 13.5). Tag values in `f`: **no** (default).

### Gotchas
`f` is the fluid alias in the pack's startup and server folders; `food_dict.js` keeps its `f` inside a function. Kept by
hand: never re-run `--seed`; new foods come from `tools/food_dict.py` proposals (`--append`). A food with no general key
goes in `item_dict.js` first.

### Tests
CDT-08 (PASSED offline), SQ-FD-01 (log half PASSED, value check open), CDT-09 pilot recipes identical (open), CDT-10
coverage (PASSED: 0 missing).

---

## framework #12: Hand-written family rows onto `global.CD.food`
Status Planned / Work KubeJS / TODO `creativedelight/framework #12` (Tests S C)

### Goal
The member files' cut, cook, bake, heatless meal, sequenced assembly and golden rows go through the family calls of
`global.CD.food`, so each family's rule (`global.CD.FOOD` values, owner 2026-10-27) holds everywhere.

### Now
Helper: [cd_food.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_food.js)
(Creative Delight jar; values in `global.CD.FOOD` from line 38: slices 40, cook 44). Callers since 2026-10-27 (`#11`):
`creativeengineering/baking.js`, `rusticdelight/recipes.js`, `creativeengineering/cultivated_meat.js`. Hand rows left
(all under `kubejs/data/creativedelight/kubejs/`, checked 2026-10-31):

| File:line | Row | Family call |
|---|---|---|
| `fruitsdelight/recipes.js` 16-18 | baked durian, baked pear, dried persimmon (furnace only, xp 0.1) | `fruitCook` (furnace + smoker + campfire, xp 0.35) |
| `hostiledelight/recipes.js` 26-27 | grilled ghast, cooked rotten ham campfire (xp 1, 600 t) | `meatCook` |
| `miners_delight/recipes.js` 15 | cooked arthropod (smoking only) | `meatCook` (or `fishCook`, decision) |
| `abnormals_delight/recipes.js` 19-21 | duck fillet cut, cooked duck fillet (furnace + smoker) | `meatCut`, `meatCook` |
| `abnormals_delight/upgrade_aquatic.js` 19-20 | cooked perch / pike slice (furnace only) | `fishCook` |
| `delightful/recipes.js` 66-67 | roasted acorn (furnace + smoker) | `fruitCook` (nut) |
| `alexsdelight/recipes.js` 25 | catfish -> 6 slices | `fishSlice` (adds the bone meal) |
| `farmersdelight/recipes.js` 367-368, 376-377 | fried egg (furnace + smoker); salmon, cod slices | `eggCook`; `fishSlice` |
| `rusticdelight/recipes.js` 57 | glowing calamari -> 2 slices + bone meal | `fishSlice` |
| `farmersdelight/recipes.js` 102-112, 186-189, 240, 271-273 | heatless soups and meals (`_cooked_manual_only`) | `soupCraft` / `mealCraft` / `saladCraft` |
| `farmersdelight/recipes.js` 179-250 | bbq stick, grilled salmon, bacon and eggs, rolls, rice medley, mutton chops assemblies | `skewerAssemble` / `assemble` |
| `fruitsdelight/recipes.js` 35, `rusticdelight/recipes.js` 67 | fig chicken stew, bell pepper pasta (heatless) | `soupCraft` / `mealCraft` |
| `minecraft/golden.js` 17-18, `create/golden.js` 19-32, `immersiveengineering/golden.js` 16-19 | golden carrot / apple: ring, heated mixer, arc furnace; enchanted apple assembly | `goldenGild`, `goldenEnchant` in `minecraft/golden.js`; the `create/` and `immersiveengineering/` rows fold into it |

Not family rows (row's Decision): Baklava (`delightful/recipes.js` 57), bread, salvaging cuts (`farmersdelight/recipes.js` 59-90),
the Miner's calamari loop (32), Hostile's prepared sea pickle (20).

### Build
1. One file at a time: replace the hand rows with `F.<call>(input, output, ...)` inside `global.CD.recipes`
   (`var F = global.CD.food(e)` at the top). The call removes the same stations' recipes for that input and output, so
   the mod's own versions go too.
2. After each file: `tools/test_cd_food.sh <file>` (prints every recipe call, lists family ids missing from the last
   dump; add the file to its default list).
3. Golden: `F.goldenGild(i.carrot, i.golden_carrot, i.any_nugget_gold)` and the apple, `F.goldenEnchant(i.golden_apple,
   i.enchanted_golden_apple)` in `minecraft/golden.js`; delete `create/golden.js` and `immersiveengineering/golden.js`
   rows (keep the files only if other rows remain).
4. Record changed ids (the helper's own suffixes) in creative_delight.md 30 "State"; `#6`'s harness with an allow
   list for them.
5. Changelog bullet (values change): e.g. "Baked pear, baked durian and dried persimmon also cook in the smoker and on
   a campfire; xp 0.35 [0.1]", "Grilled ghast ... xp 0.35 [1]".

### Decisions
- Which rows are family rows (row's Decision): **the table above**; Baklava, bread, salvaging cuts, Miner's calamari
  loop stay hand-written.
- Cooked arthropod: **`meatCook`** (a land mob drop; check its `food_type` tag in `food_types.json` first; the family
  adds furnace and campfire).
- Hostile campfire xp 1: **follow `CD.FOOD`** (owner 2026-10-27: the values are the rules).

### Gotchas
- `assemble` turns an array step into a fill step (`Array.prototype.fill`, creative_delight.md 30 open bug,
  `baking/baked-goods #37`): fix it first or keep array steps out of moved assembly rows.
- A family sweep never removes `creativeengineering:` or `creativedelight:` recipes; delete the old hand row itself.
- Ids change to the helper's suffixes: anything that names an old id (JEI hiding, `R.remove` lists, quests) needs a grep.

### Tests
- FFH-new1 (proposed, numbered when queued): `tools/test_cd_food.sh` over every touched file, every family id present after a `/dumpall`.
- FF-new1 (proposed, numbered when queued; in game / client): JEI for baked pear, cooked duck fillet, catfish slice and golden carrot shows each
  family's stations and values; no duplicate recipes.
- Scratch: SQ-FFH-02 extended with the touched files.

---

## framework #7: Generators write into the CD script tree
Status In Progress / Work tool / TODO `creativedelight/framework #7`

### Now
Mechanism built 2026-10-25 (creative_delight.md 28.2): `outRoot` per entry in `tools/generators.json` (50 entries; the
key is documented in the file's header, line 22, and no entry sets it on 2026-10-31), read by
[tools/gen_out.py](../../tools/gen_out.py) (`out_root`, `header`, `--check`); CDT-13 PASSED (`tools/test_gen_out.py`).
Blocker: every candidate writes JSON that CD scripts read by instance path (`generation_scripts/food/can_types.json` x6,
`can_servings.json`, `food_types.json`, `food_families.json`, `placeable.json`, `mortar_catalog.json`,
`comfort_nourishment.json`, `worldgen/crop_seasons.json`, `biome_traits.json`; readers in the CD tree and in CD's startup
`cans.js`, `comfort_nourishment.js`). A file inside the jar has no instance path.

### Build
1. Decide per generator (below). For a moved one: emit a `.js` data file (`global.CD.data.<name> = {...}`) under its
   `outRoot` with `header(id)` first, and change its readers from `JsonIO.readString` to the global.
2. Server data first (mortar catalog, placeable, food types); startup data (cans) last or never.
3. Creative Brewing (`creativebrewing/move #1`) will want the same for `gen_effect_descriptions.py` and
   `gen_coaster_models.py` (creative_brewing.md 4.2): keep `gen_out.py`'s allowed roots open for the CB trees.

### Decisions
- Which generators move: **mortar catalog and placeable first** (server only, small); food types next; **cans stay
  JSON by path** (read at startup by two packs).

### Gotchas
Generated files in the jar's tree are `dev` class in `build_deploy.py`; a run after a jar build makes the jar stale
(the freshness check refuses it).

---

## framework #3: Check the MIT notices
Status In Progress / Work docs / TODO `creativedelight/framework #3`

### Now
- Checked 2026-10-25 (creative_delight.md 28.1): Farmer's, Delightful, End's, Ocean's notices fixed.
- Members since then, checked 2026-10-31: `members/enders/NOTICE-upstream-MIT.txt` (Ender's Delight 1.1.4, mods.toml MIT,
  sha1 named) and `members/hearth/NOTICE-upstream-MIT.txt` (Hearth and Harvest 1.4.1, MIT) are present. Rustic
  (`backportedRustic`) is a donor-jar port (upstream jar read at runtime from `rustic_upstream/`, not shipped), so no
  notice is needed; Blockheads Additions holds only own code (CfB is All Rights Reserved; models name CfB parents by id).
- **Left: Oceanic.** `repackedOceanic-1.0.1-ce-19.2-01.jar` is still nested in `creativedelight-1.0.0.jar`; its
  notice keeps "MIT, DariusZeBaguette" from `mods.toml`, while the author's Modrinth listing for the same file says
  All Rights Reserved.

### Build
Owner decision, then either: (a) a message to the author and the notice updated with the answer, or (b) drop the repack:
Oceanic back to its own upstream jar in `mods/`, the fishing-net overlay as a `@Pseudo` mixin in CD
(`mixin.oceanic_delight.`, `CDMixinPlugin`), the member row out of `creativedelight_members` and `mod_sides.md`.

### Decisions
- **Ask the author first; if no answer by the next release, drop the repack** (option b keeps the overlay and ships
  nothing of theirs).

---

## framework #9: Donor jars downloaded by the build
Status Pinned / Work tool / TODO `creativedelight/framework #9`. Writeup creative_delight.md 15.

Nothing to do until the owner lifts the pin. Pins today: `UpstreamJars.java` in
`development/creativeBrewing/members/vinery/` (moved from `backportedVinery` 2026-10-31),
`development/creativeDelight/members/rustic/`, `development/backportedMiners/`; donor folders `farmers_upstream/`,
`miners_upstream/`, `rustic_upstream/`, `vinery_upstream/`; `UPSTREAM_DONORS` in `tools/build_deploy.py` (line 507).
When it starts: a download step per donor that checks the same sha1 and refuses on mismatch.

---

## Built, needs play

| Row | Change | Open tiers | Tests | Writeup |
|---|---|---|---|---|
| framework #1 | Framework scratch tests | S~ C | SQ-CDT-07, SQ-CDJ-03 (client), SQ-CDJ-04, SQ-FN-02, -03 (group B biomes) | [creative_delight.md](../creative_delight.md) 19 |
| framework #5 | CD config keys and `global.CD` API (`[qol]`) | S~ | SQ-CDQ per-key switch checks | [creative_delight.md](../creative_delight.md) 8.1, 22 |
| framework #8 | Compartment pattern (`CDMixinPlugin`, `mixin.<modid>.`) | S | SQ-CDQ-03, CDT-new2 (proposed, numbered when queued: boot with each optional mod absent) | [creative_delight.md](../creative_delight.md) 12 |
| framework #10 | Stale upstream member jar logs an ERROR | S | SQ-CDJ-02 (rewritten, not rerun) | [creative_delight.md](../creative_delight.md) 28.3 |
| framework #11 | Members moved from `global.foodFamily` to `global.CD.food` | S | SQ-FFH-02, FF-10 | [food_families.md](../food_families.md) 4.5, [creative_delight.md](../creative_delight.md) 30 |
