# Brief: food / stack-sizes

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `stack-sizes`; checked 2026-10-31 (brief sweep:
line numbers, the 2026-10-30 `/dumpstacks`, the Brewin' port path). Reference writeup:
[stack_sizes.md](../stack_sizes.md) (generated, no test prefix). The policy lives in
[tools/gen_stack_sizes.py](../../tools/gen_stack_sizes.py). Its output,
[stack_sizes.json](../../kubejs/generation_scripts/stacks/stack_sizes.json), is applied at startup by
[stack_sizes.js](../../kubejs/startup_scripts/changes/stack_sizes.js). Egg stacks are a policy rule too
([food.md](../food.md) 3.9, FO-28).

## Scope and order

| Row | Status | Size |
|---|---|---|
| `food/stack-sizes #1` Consistent cooking stack sizes | Testing: BUILT 2026-11-01, Build steps 1-7 done as written (plus pear with rock sugar pinned to 32 in `FOOD_OVERRIDES`, owner); Decisions taken as recommended; restart and SQ-STK-01 to -05 left | small: rules in one tool, then a game restart |
| `food/stack-sizes #2` Stack sizes reference out of date | Planned; also in TODO "Blocked on Dumps" (dump done 2026-10-30, CQ-DUMP-04) | trivial: offline tool run |

Build #1, restart, then do #2: `/dumpstacks` + `tools/stack_sizes.py`. The fresh reference is also how #1 gets
checked (#2's policy-check step). #2 can also run now, offline, on the 2026-10-30 dump; it would then list the gaps
below as they are. Related rows that touch the same rules:
- `dishes/ingredients #2` asks "bucket stack size 16?"; the policy gives filled buckets 8 today.
- `ports/cavesandchasmsport #5` asks about gold bucket stacking and whether milk keeps 64.
- `food/drinks #3` merges the two milk bottles; the milk rule below covers both, so either result works.
- `dishes/ingredients #9` plans a stackable cheese ripening shelf.

None of them blocks #1.

---

## stack-sizes #1: Consistent cooking stack sizes
Status **In Progress** · Work: tool / data · id `food/stack-sizes #1`

### Goal
Policy from the scratchpad (2026-10-19):
- Bowl ingredients and bottle ingredients (milk, honey): 64.
- Bowl foods, including jello: 32.
- Drinks and bottles (tea, juice, custard, "pitcher drinks", shaved ice): 32.
- Dog food: 32. Cheese wheel: 32.

### Now (2026-10-31)
How [gen_stack_sizes.py](../../tools/gen_stack_sizes.py) (251 lines, last changed 2026-10-28) assigns sizes. The first
rule to claim an item wins:
1. `FOOD_LEAVE_ALONE` (line 48): 64.
2. Filled buckets `BUCKET_RE`: 8.
3. `PLACED_FOOD_8` (line 67): 8.
4. `BOTTLES_16`: 16.
5. `FOOD_OVERRIDES` (line 53): bone broth, cooked rice and honey bottle at 64; Rustic Delight's three pancakes 32;
   fig pudding 8.
6. The food-type loop (line 178), keyed only on `food_types.json` types:
   - golden: 16
   - `BOWL_TYPES` (meal, meal/feast, soup, salad): 32
   - `DRINK_TYPES`: 32
   - handheld: 64
   - ingredient: 64
   - `sweet` matching `SWEET_32 = (ice_cream|pudding|custard)$` (line 63): 32
7. Hats, stations, misc, eggs, signs and the rest.

The docstring (line 16) lists `food/foods.json, food/drinks.json` as inputs, but **the code reads neither**: no rule
looks at an item's remainder (bowl / bottle), and non-edible drinks are never seen. Tags are read (line 142) only for
`forge:eggs`.

Current output (generated 2026-10-27): 1273 entries (8: 492, 16: 57, 32: 364, 64: 360).

What already matches the policy: every `drink` and `drink/alcohol` (32), FD and Fruits Delight custards (32), honey
bottle (64), jams, batter, cooking oil, tomato sauce, bone broth and cooked rice (64, `ingredient` type or overrides).

Gaps, **confirmed in the 2026-10-30 `/dumpstacks`** (`kubejs/exported/stacks/stack_sizes.json`, every one still at its
mod's size and absent from stack_sizes.json):

| Policy | Items | Size now -> policy | Why the rules miss it |
|---|---|---|---|
| Bowl food incl. jello | 22 `fruitsdelight:*_jello` (family `jello`) | 16 -> 32 | type `sweet`, no regex match |
| Bowl food | `berry_good:glowgurt` | 1 -> 32 | `sweet` |
| Bowl food | `fruitsdelight:pear_with_rock_sugar` | 16 -> 32 | `sweet` |
| Bowl food | `fruitsdelight:mangosteen_cake` | 16 -> 32 (see Decisions) | `sweet`, bowl remainder, also a sneak-placed block |
| Bowl food | `incubation:scrambled_eggs` | 1 -> 32 | type `egg` |
| Dog food | `farmersdelight:dog_food` | 16 -> 32 | type `special` |
| Shaved ice | `fruitsdelight:hamimelon_shaved_ice` (family `frozen/shaved_ice`) | 16 -> 32 | `sweet`, bottle remainder |
| Drinks with no food value | `farmersdelight:hot_cocoa`, `melon_juice`; `farmersrespite:gamblers_tea`, `long_gamblers_tea`, `strong_gamblers_tea`, `strong_hot_cocoa`, `strong_melon_juice`; `abnormals_delight:cherry_cream_soda`, `passion_aloe_nectar`, `pickerelweed_juice`; `create:builders_tea` | 16 -> 32 | not edible, so not in the food catalog or a food type. Builder's Tea has **no remainder** in the dump |
| Bottle ingredient (milk) | `farmersdelight:milk_bottle`, `neapolitan:milk_bottle` | 16 -> 64 | not edible. `#forge:milk/milk_bottle` (`i.t_milk_bottle`, item_dict.js 1896) holds **only FD's** in the 2026-10-30 tag dump |
| Cheese wheel | `brewinandchewin:flaxen_cheese_wheel`, `scarlet_cheese_wheel`, `unripe_flaxen_cheese_wheel`, `unripe_scarlet_cheese_wheel` | 16 -> 32 | not food. The port sets 16: `development/creativeBrewing/members/brewin/src/main/java/umpaz/brewinandchewin/common/registry/BnCItems.java:56-60` |
| "Pitcher drinks" | none found | | no item id or English name with pitcher / jug / carafe (jars checked 2026-10-21) |

The `/dumpstacks` rows now carry `edible`, `useAnim`, `remainder`, `classes` and `tags` (including the
`creativeengineering:food_family/*` tags) for every listed item, so one file gives everything the rules need.

### Build
All in [tools/gen_stack_sizes.py](../../tools/gen_stack_sizes.py):
1. Load the stack dump's rows (the simplest source; it lists exactly the items not at 64):
   ```python
   rows = json.load(open(EXPORT / 'stacks/stack_sizes.json'))['items']       # id -> row
   remainder = {i: r.get('remainder') for i, r in rows.items()}
   ```
   Fall back to `food/foods.json` / `drinks.json` only if an item is missing there. Fix the docstring.
2. `SWEET_32` (line 63) -> `r'(ice_cream|pudding|custard|jello|shaved_ice)$'`.
3. **Milk bottles 64, before any drink rule**, next to the `FOOD_OVERRIDES` loop:
   `require(['farmersdelight:milk_bottle', 'neapolitan:milk_bottle'], 64, 'bottle ingredient (milk)')` plus every
   member of `tags['forge:milk/milk_bottle']` (Neapolitan's is not in the tag).
4. **Bowl / bottle foods, after the type loop (line 178):**
   ```python
   for i, t in sorted(food_type.items()):
       if i in universe and t not in INGREDIENT_TYPES | GOLDEN_TYPES and t != 'canned':
           if remainder.get(i) == 'minecraft:bowl':
               give(i, 32, 'bowl food')
           elif remainder.get(i) == 'minecraft:glass_bottle':
               give(i, 32, 'bottled food')
   ```
   `give` never overwrites, so golden (16), placed cakes (8) and ingredients (64) keep their earlier claim.
   `alexsmobs:fish_oil` (`special`, bottle, at 64 today, so not in the dump) would drop to 32: put it in
   `FOOD_LEAVE_ALONE`.
5. **Non-edible drinks:** each dump row with `edible` false, `useAnim` `DRINK`, no `PotionItem` in `classes`, not a
   bucket: `give(id, 32, 'drink (no food value)')`. On the 2026-10-30 dump that is the 11 drinks above, the two milk
   bottles (claimed at 64 first by step 3) and `botania:brew_flask` / `brew_vial`: **exclude Botania's brews** (they
   are potions in all but class). Potions stay out.
6. `CHEESE_WHEELS_32 = [...]` (the four ids above), `require(CHEESE_WHEELS_32, 32, 'cheese wheel')`, after
   `PLACED_FOOD_8`.
7. `python3 tools/gen_stack_sizes.py --list | grep -E "bowl food|bottled|no food value|milk|cheese"`; expect about
   46 new entries.
8. The JSON is read at startup: **restart** (not `/reload`). The log should show `[stack_sizes] set N stack sizes` with
   N = the JSON's entry count and no `no item_dict key` warning. Raw ids are allowed as entries.

### Ids and data
- Every id above checked against the 2026-10-30 `/dumpstacks` and tag dump (2026-10-31). The inputs live under
  `kubejs/exported/` (git-ignored); the tool runs on the game instance.

### Decisions
- **Mangosteen Cake: 32 (bowl food) or 8 (placed cake)?** Recommend 32: it is served in a bowl, and
  `baking/baked-goods #5` says it is "not a cake". Add it to `PLACED_FOOD_8` instead if it counts as a placed cake.
- **Unripe cheese wheels:** recommend 32 like the ripe ones; ageing happens on the placed block.
- **"Pitcher drinks":** no such item. Recommend reading it as "every drink" (the non-edible drink rule covers later mods).
- **Brewin' tankard drinks:** already 32 through `drink/alcohol`; leave them.
- **Key on families instead of types?** The family tags are in the dump now (`jello`, `frozen/shaved_ice`). Recommend
  staying on types plus remainder for this row; moving the whole policy to families belongs to
  `food/types-and-tuning #13` (applying family rules).

### Gotchas
- "Rules never look at an item's current stack size" (docstring): keep it that way. The milk rule uses the tag and
  ids, not "items stacking to 16".
- `ItemEvents.modification` sets the item's max stack. FD's `enableStackableSoupItems` is overridden for any item the
  JSON lists.
- Stacks that don't merge because of NBT (`cooking/kitchen #5`, returned bowls and paper) are a separate bug.

### Tests
stack_sizes.md has no test prefix. Add to [food.md](../food.md) section 7 (FO-29 to FO-32 were proposed here on
2026-10-21; those numbers have since gone to other tests, so renumber when queued):
- **FO-new1 Bowl and bottle foods:** in survival, stack Apple Jello, Scrambled Eggs, Dog Food and Cantaloupe Shaved
  Ice -> 32 each.
- **FO-new2 Drinks without food value:** Hot Cocoa, Gambler's Tea, Builder's Tea -> 32; FD and Neapolitan Milk Bottle
  -> 64; a Botania Brew Flask unchanged.
- **FO-new3 Cheese wheels:** Flaxen Cheese Wheel and Unripe Scarlet Cheese Wheel -> 32.
- **FO-new4 Potions unchanged:** a Potion of Swiftness still stacks to 1.

---

## stack-sizes #2: Stack sizes reference out of date
Status **Planned** · Work: tool · id `food/stack-sizes #2`

### Goal
Regenerate [stack_sizes.md](../stack_sizes.md) from a fresh `/dumpstacks`. The checked-in file is from the
2026-10-02 dump (2741 of 18412 items), before the stack size policy.

### Now
- A fresh `/dumpstacks` exists: 2026-10-30 (19547 items, 2894 listed). The markdown has not been regenerated since
  (last commit 1b1d45f).
- [tools/stack_sizes.py](../../tools/stack_sizes.py) is up to date for the policy (writes the "Most stack sizes are
  set by stack_sizes.js ..." line, lines 205-208). The checked-in markdown still has the old `item_properties.js` line
  (line 14), which goes away on the rerun.
- Inputs: `kubejs/exported/stacks/stack_sizes.json` ([dump_stacks.js](../../kubejs/server_scripts/_lib/dump_stacks.js)),
  `kubejs/exported/food/{foods,drinks}.json` (2026-10-30) and `catalog.json` (2026-10-25: run
  `tools/food_catalog.py` first so the Food type column is current).

### Build
1. Now (offline) or after #1 and a restart: `python3 tools/food_catalog.py`, then `python3 tools/stack_sizes.py`.
   After #1 it needs a new `/dumpstacks` (sizes are read after KubeJS edits).
2. **Add a policy check** to `stack_sizes.py` (recommended): read `kubejs/generation_scripts/stacks/stack_sizes.json`,
   resolve keys through item_dict (as `gen_stack_sizes.load_dict_keys`, line 114, does), and list every item whose
   dumped stack differs from the policy size. Items the policy sets to 64 are absent from `/dumpstacks`; count that as
   a match.
3. Check the summary table: after #1, "Cooking: Drinks" has no 16s left; the jellos move from 16 to 32.
4. Update the [README](../README.md) index row (line 54) with the new date and count.

### Decisions
- Run now or after #1: recommend now (the reference is a month old) and again after #1 with the policy check.

### Gotchas
- `/dumpstacks` reports sizes after KubeJS edits: run it after the restart that applied #1, not after a `/reload`.

### Tests
- The generated "policy check" section lists no mismatches (after #1).

---

## Built, needs play

- `stack-sizes #2` (TODO "Blocked on Dumps" entry, dump done 2026-10-30): `tools/stack_sizes.py` (and
  `gen_stack_sizes.py` for #1) on the 2026-10-30 dump; [stack_sizes.md](../stack_sizes.md); CQ-DUMP-04. Offline, no
  game needed.
