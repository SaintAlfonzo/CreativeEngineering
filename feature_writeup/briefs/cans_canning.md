# Brief: cans / canning

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# cans`, Sub `canning`. Reference writeups:
[cans.md](../cans.md) (hub), [can_parity.md](../can_parity.md) (worksheet, decisions 4, sprites 5.1),
[food.md](../food.md) 3.5 (tuning) and 3.6 (servings). Code: [canning.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/canning.js),
[cans.js](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/cans.js), [gen_can_types.py](../../tools/gen_can_types.py) ->
[can_types.json](../../kubejs/generation_scripts/food/can_types.json), [can_servings.json](../../kubejs/generation_scripts/food/can_servings.json),
[cantags.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/can_tags.js), patcherMod `food/CanServings.java`, `compat/KubeJSCanAction.java`.

Updated 2026-10-31 (brief sweep): sections for the built rows (#3, #4, #5, #6, #9, #12, #14, #15) cut to one line
each under [Built, needs play](#built-needs-play) (can_parity.md and cans.md hold their facts); #2 and #7 closed; the
textures section renamed to its TODO id `textures/food #1`; new test ids are placeholders `CP-new1` to `CP-new4`
(the old proposals CP-20 and CP-23 to CP-26 collided with ids now in can_parity.md).

## Scope and order

Open rows: `textures/food #1` (Planned, was `canning #13`), #1 (In Progress), #8 (In Progress), #10 (Planned), #17
(In Progress). Testing: #3, #4, #5, #6, #9, #11, #12, #14, #15, #16 and the `/cegen` rerun (list at the end).
Complete: #2, #7.

**State on 2026-10-31** (checked against the files):

- [can_types.json](../../kubejs/generation_scripts/food/can_types.json) holds **306 cans: 272 generated pack cans and 34
  Canned Goods cans** reused (`existing`). 25 pack cans have their own sprite
  (`kubejs/assets/creativeengineering/textures/item/canned_*.png`, plus 9 overrides in
  `kubejs/assets/canned_goods/textures/items/`); the other 247 borrow their family's sprite through `GROUP_SPRITE`
  ([gen_can_types.py](../../tools/gen_can_types.py) lines 103-126). None borrows the beetroot can any more.
- can_types.json was last written 2026-10-27 22:22. The `/dumpfood` of 2026-10-30 has not been fed to the generators
  yet: scratch queue `CQ-DUMP-04` (`/cegen` for the can generators) is the TODO "Blocked on Dumps" row for #3 / #14.
- The generator still has no `--sprites` mode (only `--check`, line 698).

**Order:**

1. **`CQ-DUMP-04`:** `/cegen` on the 2026-10-30 dump, restart; then #17's feast rows in the next generator pass.
2. **#1 golden effect length** (D-B, `dur` 0.25 -> 0.333) can go with step 1; the other #1 values wait on
   `food/types-and-tuning #1` (Planned) and use #3's audit findings.
3. **#8:** the bowl remainders and the mixer check (KubeJS, no generator run).
4. **#10:** patcherMod, any time; small.
5. **`textures/food #1`:** ongoing art; `--sprites` lets each batch land without a food export.

Creative Canning (D-A, its own mod) is decided but has no row; do not start it inside these rows. Everything below
stays in KubeJS / gen_can_types.py so it ports as data.

---

## textures/food #1: Can textures (priority)
Status **Planned** / Work resource pack / TODO `textures/food #1` (was `cans/canning #13`)

### Goal
Every pack can has its own 16x16 sprite; none borrows its family's sprite or a Canned Goods sprite.

### Now
- Own sprites: 25 files `kubejs/assets/creativeengineering/textures/item/canned_*.png` and 9 overrides of Canned
  Goods' sprites in `kubejs/assets/canned_goods/textures/items/`. Canned Goods' 34 reused cans keep the mod's sprites
  or those overrides.
- Borrowing pack cans by family (can_types.json `texture`, 2026-10-31; 247 of 272):

| Section | Cans | Borrows |
|---|---:|---|
| soup | 34 | `canned_goods:items/canned_rabit_stew` |
| golden | 26 + 25 enchanted | `canned_golden_apples` / `canned_notch_apples` |
| cooked_veggie | 24 | `canned_baked_potato` |
| meal | 21 | `canned_food` |
| raw_fruit | 18 | `creativeengineering:item/canned_apple` |
| salad | 14 | `creativeengineering:item/canned_cabbage` |
| pie | 13 | `canned_pumpkin_pie` |
| cookies | 12 | `canned_cookies` |
| bread | 11 | `canned_bread` |
| raw_veggie | 10 | `creativeengineering:item/canned_carrot` |
| raw_fish, cooked_meat, feast | 9 each | raw salmon can, `canned_beef`, `canned_food` |
| raw_meat, dairy | 3 each | raw beef can, `canned_food` |
| preserve | 2 | `canned_food` |
| hazard, special, drink, dessert | 1 each | rotten flesh, apple, honey bottles, cake |

- Sprite layout (measured on canned_apple.png vs canned_carrot.png): grey can body and rim shared; the label is rows
  6-11, columns 3-12, a section colour band with a ~5x5 food icon centred. Every sprite is 16x16 RGBA.
- Pipeline: drawn in `textureswapper/canned/newcan` (label layers in `newlabel`, combined by `combine_tex.py --batch`),
  copied in by hand. That folder is not in the repo.
- A can stops borrowing only when `gen_can_types.py` is rerun (it writes `"texture": null` when `<key>.png` exists,
  around line 621), which needs the food export. The hand-written raw cans are generated now (#4), so there are no
  `.texture()` calls left to remove: cans.js line 52 applies `c.texture` from can_types.json.

### Build
1. **`gen_can_types.py --sprites`** (small, do first): load the existing can_types.json, set each non-existing can's
   `texture` to `None` when `SPRITES/<key>.png` exists, else to `GROUP_SPRITE.get(section, CAN_SPRITE_NOTE)`; write
   only can_types.json. No catalog needed, so every sprite batch lands with a copy + `--sprites` + restart. Print the
   number still borrowing per section.
2. **Golden cans by script** (51): `tools/gen_can_sprites.py --golden` (PIL): per golden can, take the base can's
   sprite (golden_cooked_beef -> canned_beef's ...; map in the script), recolour the label band to gold, write
   `canned_golden_<x>.png`; the enchanted twin reuses it (glint from `glow`). Needs the base sprites first.
3. **Draw the rest** in the user's textureswapper flow, most-seen first (recommendation for the open "order"
   decision): raw fruit (18) and raw fish (9) (same can, new icon), cooked meat (9), soup (34), cooked veggie (24),
   cookies (12), bread (11), salad (14), meal (21), pie (13), feast (9), the small sections last. Copy to
   `kubejs/assets/creativeengineering/textures/item/<key>.png`, run `--sprites`, restart.
4. Sheet: Texture cells to `x`, summary column, can_parity 5.1 sprite table.

### Ids and data
- Sprite file name = the can's item_dict key = item path.
- Never override `canned_goods:items/canned_beets` or another borrowed group sprite by accident: every can of the
  family points at it (can_parity 5.1).
- Canned Goods model -> texture names differ from item ids (verified in CannedGoods-1.19.2-3.jar): `canned_pork` uses
  `items/canned_porkchops`, `canned_rabbit_stew` `items/canned_rabit_stew`, `canned_pufferfishs` `items/canned_pufferfish`,
  `canned_spider_eyes` `items/canned_spider_eye`, `canned_carrots` `items/canned_pumpkin_pie`, `canned_pumpkin_pie`
  `items/canned_golden_carrots`. Overrides under `kubejs/assets/canned_goods/` must use the texture name, not the id.

### Decisions
- Order: recommend above. Golden: scripted recolour once the base sprites exist.
- Whether to commit `textureswapper/canned/` (or at least `newlabel/` + `combine_tex.py`) to the repo so a session can
  run the label compositing; recommend yes, as `tools/textures/canned/`.

### Gotchas
- KubeJS items without a texture use `creativeengineering:item/<id>`; a missing png shows purple/black, so do not
  set `texture: null` by hand.
- Resource changes need F3+T or a restart; can_types.json texture changes need a restart (startup script).

### Tests (CP-)
- CP-new1 (proposed, numbered when queued) sprites batch: after a `--sprites` run and restart, JEI "Canned" shows
  every can of the batch with its own sprite; `[cans] registered N generated cans` unchanged.

---

## canning #1: Can values for meal, pie, feast, drink and hazard cans
Status **In Progress** / Work KubeJS / data / TODO `cans/canning #1`

### Goal
Real goals for the placeholder sections, then rerun the generator.

### Now
- Placeholders in gen_can_types.py `SECTIONS` (from line 72): `hazard` line 78 (Meat Meal 5 / 3), `meal` 88 (x0.75,
  2 -> 4), `pie` 89 (x1, 4 -> 4), `feast` 90 (x1, 1 block -> 6), `drink` 91 (x1, 4 -> 4), `golden` 92 (x0.25, 10
  servings, `dur=0.25`). The TODO's "line 86" is now 88 (meal).
- Goal comments in [canning.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/canning.js) (621 lines):
  Hazard 357, Meals 467 (`// TBD` 471), Pie 485, Feasts 509, Drinks 524 (`// TBD` 528), Special / golden 558 (`// TBD`
  562). The TODO's lines 471, 528, 562 are right.
- Value vs goal per can: can_parity 4.3, now generated for every can by `tools/can_audit.py` (#3). Findings for this
  row: golden cans lose hunger to the required honey bottle (71 to 111 %), feasts 55 to 150 %, additive soups about 3x.
- Golden: D-B decided 10 servings, 0.25x nutrition (built), 0.333x effect time (**not built**: `dur=0.25`).

### Build
1. Golden: `SECTIONS['golden']['dur'] = 0.333`; rerun with the `CQ-DUMP-04` generator pass.
2. The rest after `food/types-and-tuning #1` sets per-type values: write each section's goal into its canning.js Goal
   comment and SECTIONS (`mult`, `servings`, `n`), rerun, rerun `tools/can_audit.py` (refreshes 4.3), update 3.1.

### Decisions
- Recommended starting goals (keep the "rationing" design of canning.js: prepared food, many servings, a little
  lower SoL): meal x0.75 with 2 -> 4 stays; pie 4 slices -> 4 at x0.8 (125 % is generous); feast per audit (55 to 150 %:
  size `n` per feast from its servings, not one rule); drink x0.8. Hazard: the low-value ones get their own small meal
  like shulker / maggot. Golden: count the honey bottle into the goal.

### Tests
- CN-02 covers golden effects; add the new numbers to it. Rerun `tools/can_audit.py`: every section within its goal.

---

## canning #8: Canning automation
Status **In Progress** / Work KubeJS / TODO `cans/canning #8`

### Goal
Cans can be made by machines; the canning.js notes "food effects carry over" and "bone broth returns its bowl" are
settled.

### Now
- canning.js line 80 `// Other Changes TODO` (the built servings notes under it), line 94-95 `// TODO` / `// Canning
  automation`, line 99 the two notes (effects, bone broth bowl). Unchanged since the row was written; no automation
  code exists yet.
- Create already automates making cans: the mixer runs shapeless crafting recipes with more than one ingredient, and
  every canning recipe is shapeless with the can plus at least one item. The saved worlds' `create-server.toml` have
  `allowShapelessInMixer = true` (line 27) and `allowRegularCraftingInCrafter = true` (line 33). Recipes that **use**
  a can are `_manual_only` (#9), so machines never eat a whole can.
- Effects carry: every raw can is generated now (#4), so all cans copy their food's effects.
- Bowls: Farmer's Delight bowl foods carry a bowl crafting remainder (bone broth and FD soups hand their bowls back).
  Vanilla mushroom stew, rabbit stew and beetroot soup have none: canning 6 of them loses 6 bowls. Other mods' bowl
  foods unknown. No pack script sets a remainder on food yet (the only `setCraftingRemainder` is
  `kubejs/startup_scripts/items/potion_buckets.js` line 81).

### Build
1. In game: an empty can + 8 beetroot in a basin under a mixer (no heat) -> Canned Beetroot; JEI's Create "shapeless
   mixing" category lists canning recipes. If not, add `R.createMixing` copies in `canSection()` (NCH
   `createMixing(output, input, e, idOverride, heat)`).
2. Bowls: a startup `ItemEvents.modification` in cans.js (or a cooking script): `e.modify(i.mushroom_stew, it =>
   it.setCraftingRemainder(Item.of(i.bowl).item))` (unprefixed call, as potion_buckets.js lines 74-81 note). Find the
   list first: a one-off script logging every soup / salad / meal food whose `getCraftingRemainingItem()` is empty.
3. Delete the canning.js TODO lines 94-99 once settled; say where the answer lives (cans.md).
4. Optional flavour later (Creative Canning): Create sequenced assembly "fill then press the lid".

### Decisions
- Rely on Create's auto-shapeless (recommend) vs explicit mixing recipes.

### Tests (CP-, proposed, numbered when queued)
- CP-new2 mixer canning: empty can + 8 beetroot in a mixer gives Canned Beetroot.
- CP-new3 6 mushroom stews + can -> can + 6 bowls back.

---

## canning #10: Cosmetic can opener
Status **Planned** / Work patcherMod / KubeJS / TODO `cans/canning #10`

### Goal
A can opener item; while it is in the inventory, opening a can plays a sound.

### Now
"Opening" happens in patcherMod `food/CanServings.finishServing` (line 152 on): a partial can elsewhere in the
inventory is eaten first (`findOpenCan`, line 160); the source is a full can when `!isPartial(source)` and
`maxServings(source) > 1`. KubeJS can't tell (the `foodEaten` event sees a copy with no NBT whether or not a can is
opened). Canned Goods has only golem sounds; the pack has no `sounds.json` under `kubejs/assets/creativeengineering/`
or patcherMod yet. No can opener id exists.

### Build
1. KubeJS: item `creativeengineering:can_opener` (cans.js or items/), key `i.can_opener` in item_dict's canning block,
   tag `#creativeengineering:can_openers` (`i.t_can_openers`) added through `global.tags` in
   [can_tags.js](../../kubejs/data/creative_delight/kubejs/creativeengineering/can_tags.js); a recipe (iron nugget +
   iron ingot, NCH `shaped`). Sound: `StartupEvents.registry('sound_event', e => e.create('creativeengineering:can_open'))`,
   `kubejs/assets/creativeengineering/sounds.json` + `sounds/can_open.ogg`.
2. patcherMod `CanServings.finishServing`: before `afterServing` (line 163), `boolean opening = !level.isClientSide &&
   !isPartial(source) && maxServings(source) > 1;` then if `opening` and the inventory holds an item in the tag, play
   the sound at the player (`SoundSource.PLAYERS`, pitch 0.9-1.1). Look the sound up by id
   (`ForgeRegistries.SOUND_EVENTS.getValue`), no Java registration.
3. Optional: the same on a craft-time opening (`craftRemainder`, line 195), skip at first (no player there).

### Tests (CP-, proposed, numbered when queued)
- CP-new4 with an opener in the inventory, the first bite of a full Canned Beef plays the sound; the second bite and an
  opener-less bite are silent.

---

## canning #17: All feasts cannable
Status **In Progress** / Work KubeJS (+ `tools/gen_can_types.py`) / TODO `cans/canning #17` (scratchpad 2026-10-24)

### Goal
Every feast (a placed block with servings) can be canned, as the nine existing feast cans are. Related: `dishes/meals #5`.

### Now
- Feast cans are rows in [gen_can_types.py](../../tools/gen_can_types.py) lines 357-370, section `feast` (line 90:
  `mult=1, servings=6, n=1`, `input=` the feast block item, value source = the serving); output goes to can_types.json;
  recipes come from `canSection('feast')` (canning.js Feasts, line 509). Still nine rows on 2026-10-31: no feast added
  since the row was written.
- Covered: popcorn box, stuffed hoglin (one can for its plates), honey glazed ham, nachos, pineapple fried rice, roast
  chicken, shepherd's pie, stuffed pumpkin, Ender's stuffed shulker.
- **Feast blocks with no can** (`kubejs/exported/food/food_blocks.json` of 2026-10-30, blocks whose classes include a
  `FeastBlock`, `PizzaBlock` or own plate class; 29 such blocks): Brewin' pizza; Delightful stuffed cantaloupe; End's
  Delight dragon meat stew, grilled shulker, steamed dragon egg; Creative Delight / FD gleaming salad and rice roll
  medley; Miner's fake meatloaf, glazed arachnid limbs, stuffed squid; Ocean's Delight guardian soup; Rustic Delight
  three bell pepper medleys and rice roll royale; Veggies Delight lasagna pan, stuffed bell peppers, vegan pizza;
  Vintage cheese pizza and meat pizza. Fantasy Furniture's two platters match the class filter but are decor, not
  feasts. Multi-serving plates on other classes (Brewin' quiche, Oceanic Delight plates) are not in this filter:
  check by hand.

### Build
1. Confirm the list in game or from `food_blocks.json` (`blockClasses`); add item_dict keys for any block item missing.
2. One `('feast', 'canned_<name>', '<Name>', ['<serving key>'], False, dict(input=['<block item key>']))` row per
   missing feast, keys from `global.item`; run `/cegen` (with `CQ-DUMP-04`), restart; sprites per `textures/food #1`.
3. Later feasts (Hostile, Ocean delights, `meals #5` new assemblies, Large Meals if ported) add a row when integrated.

### Decisions
- Servings per can for 8-slice pizzas: recommend keep the section rule (6), so no per-feast exception.
- Pizza block versions with partial servings are not whole feasts: recommend input = the whole pizza item only.
- Dragon egg and dragon stew (End's Delight): recommend canned like the rest (golden-style rarity is not a can rule).

### Gotchas
Generated files are never hand-edited. `canned_hoglin_feast` shares one can across its plates: do the same for any
feast with several plate items.

### Tests
- CN-07 (proposed) JEI shows a can recipe for each feast listed in the generator section.
- CN-08 (proposed) Eat a canned pizza and a canned stuffed pepper can: serving values match the serving item.

---

## Built, needs play

| Row | What | Writeup | Open tiers / note |
|---|---|---|---|
| `cans/canning #3` | Hunger / saturation audit: `tools/can_audit.py` fills the sheet columns and can_parity 4.3 for every can | [can_parity.md](../can_parity.md) 4.3 | T (S+); confirm against the 2026-10-30 `/dumpfood`; findings feed #1 |
| `cans/canning #4` | The 26 hand-written raw cans are generated now and carry their food's effects | [can_parity.md](../can_parity.md) CP-20, [cans.md](../cans.md) F2 | P (S+) |
| `cans/canning #5` | Pumpkin can is fruit, 3 servings | [can_parity.md](../can_parity.md) CP-09 | P T (S+) |
| `cans/canning #6` | Half cans, shulker filet path, opened filling can makes no pie, White Strawberries values | [can_parity.md](../can_parity.md) 4.2, CP-23 | S P |
| `cans/canning #9` | Servings stay manual-craft only (D-C): `_manual_only` ids, no result on machine grids | [cans.md](../cans.md) F4, D-C; CP-24 | S P; decision "whether to extend" open |
| `cans/canning #11` | Canned golem dye, generated cans, parity tests | [cans.md](../cans.md) 7, [can_parity.md](../can_parity.md) | S C P: CN-01 to CN-06, CP-01 to CP-14 (CP-06 superseded by CP-08) |
| `cans/canning #12` | Canned Pears in Rock Sugar and Canned Creamed Corn (built with #14) | [can_parity.md](../can_parity.md) CP-19 | S C P |
| `cans/canning #14` | Can crafts: 19 new cans, sections dairy / preserve / dessert, Canned Dough, Canned Goods values, removals | [can_parity.md](../can_parity.md) 0, CP-15 to CP-22, CP-25 | C P (S+): restart, `/dumpfood`, `food_catalog.py --check` |
| `cans/canning #15` | Canned cake: 1 cake or 4 slices + sugar / honey -> 4 fast servings | [can_parity.md](../can_parity.md) 2.23 | P T (S+); decision "4 or 6 slices" open |
| `cans/canning #16` | Sweet Berry Pie filling can (FD cheesecake renamed) | [oven.md](../oven.md) OV-32, [can_parity.md](../can_parity.md) CP-26 | C P (S+) |
| `cans/canning #3, #14` (Blocked on Dumps) | `/cegen` on the 2026-10-30 `/dumpfood` (glow, pickle and mhadjeb cans) | scratch queue `CQ-DUMP-04` | can_types.json last written 2026-10-27; rerun not done |

Closed: `#2` (`Want = ?` cells, done 2026-10-20, `c82dd31`: no `?` left) and `#7` (removed 1.0.4 cans, D-E: no
remap; if reopened, a Forge `MissingMappingsEvent` remap) are Complete.

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
