# Brief: food / stack-sizes

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `stack-sizes`. Reference writeup:
[stack_sizes.md](../stack_sizes.md) (generated, no test prefix). The policy lives in
[tools/gen_stack_sizes.py](../../tools/gen_stack_sizes.py). Its output,
[stack_sizes.json](../../kubejs/generation_scripts/stacks/stack_sizes.json), is applied at startup by
[stack_sizes.js](../../kubejs/startup_scripts/changes/stack_sizes.js). Egg stacks are a policy rule too
([food.md](../food.md) 3.9, FO-28).

## Scope and order

| Row | Status | Size |
|---|---|---|
| `food/stack-sizes #1` Consistent cooking stack sizes | Planned | small: rules in one tool, then a game restart |
| `food/stack-sizes #2` Stack sizes reference out of date | Planned | trivial, but needs the game |

Build #1, restart, then do #2 in the same game session: `/dumpstacks` + `tools/stack_sizes.py`. The fresh reference
is also how #1 gets checked (see #2's policy-check step). Related rows that touch the same rules:
- `food/ingredients #2` asks "bucket stack size 16?"; the policy gives filled buckets 8 today.
- `largecontent/cavesandchasmsport #5` asks about gold bucket stacking and whether milk keeps 64.
- `food/drinks #3` merges the two milk bottles; the milk rule below covers both, so either result works.
- `food/ingredients #9` plans a stackable cheese ripening shelf.

None of them blocks #1.

---

## stack-sizes #1: Consistent cooking stack sizes
Status **Planned** · Work: tool / data · id `food/stack-sizes #1`

### Goal
Policy from the scratchpad (2026-10-19):
- Bowl ingredients and bottle ingredients (milk, honey): 64.
- Bowl foods, including jello: 32.
- Drinks and bottles (tea, juice, custard, "pitcher drinks", shaved ice): 32.
- Dog food: 32. Cheese wheel: 32.

### Now
How [gen_stack_sizes.py](../../tools/gen_stack_sizes.py) assigns sizes. The first rule to claim an item wins:
1. `FOOD_LEAVE_ALONE` (line 45): 64.
2. Filled buckets `BUCKET_RE`: 8.
3. `PLACED_FOOD_8` (line 64): 8.
4. `BOTTLES_16`: 16.
5. `FOOD_OVERRIDES` (line 50): bone broth, cooked rice and honey bottle at 64.
6. The food-type loop (line 181), keyed only on `food_types.json` types:
   - golden: 16
   - `BOWL_TYPES` (meal, meal/feast, soup, salad): 32
   - `DRINK_TYPES`: 32
   - handheld: 64
   - ingredient: 64
   - `sweet` matching `SWEET_32 = (ice_cream|pudding|custard)$` (line 60): 32
7. Hats, stations, misc, eggs, signs and the rest.

The tool's docstring lists `food/foods.json, food/drinks.json` as inputs, but **the code reads neither**: no rule looks
at an item's remainder (bowl / bottle), and non-edible drinks are never seen.

Current output: 1056 entries (8: 491, 16: 55, 32: 229, 64: 281). It matches today's `food_types.json`: every
`drink` / `meal` / `soup` / `salad` / `ingredient` item is in it (checked 2026-10-21).

What already matches the policy: every `drink` and `drink/alcohol` (32), FD and Fruits Delight custards (32), honey
bottle (64), jams, batter, cooking oil, tomato sauce, bone broth and cooked rice (64, `ingredient` type or overrides).

Gaps. Old size from the 2026-10-02 `/dumpstacks` ([stack_sizes.md](../stack_sizes.md)); none of these items is in
today's JSON, so they still have their mod's size:

| Policy | Items | Size now → policy | Why the rules miss it |
|---|---|---|---|
| Bowl food incl. jello | 22 `fruitsdelight:*_jello` | 16 → 32 | type `sweet`, no regex match |
| Bowl food | `berry_good:glowgurt` | 1 → 32 | `sweet` |
| Bowl food | `fruitsdelight:pear_with_rock_sugar` | 16 → 32 | `sweet` |
| Bowl food | `fruitsdelight:mangosteen_cake` | 16 → 32 (see Decisions) | `sweet`, bowl remainder, also a sneak-placed block |
| Bowl food | `incubation:scrambled_eggs` | 1 → 32 | type `egg` |
| Dog food | `farmersdelight:dog_food` | 16 → 32 | type `special` |
| Shaved ice | `fruitsdelight:hamimelon_shaved_ice` | 16 → 32 | `sweet`, bottle remainder |
| Drinks with no food value | `farmersdelight:hot_cocoa`, `melon_juice`; `farmersrespite:gamblers_tea`, `long_gamblers_tea`, `strong_gamblers_tea`, `strong_hot_cocoa`, `strong_melon_juice`; `abnormals_delight:cherry_cream_soda`, `passion_aloe_nectar`, `pickerelweed_juice`; `create:builders_tea` | 16 → 32 | not edible, so not in the food catalog or a food type |
| Bottle ingredient (milk) | `farmersdelight:milk_bottle`, `neapolitan:milk_bottle` | 16 → 64 | not edible |
| Cheese wheel | `brewinandchewin:flaxen_cheese_wheel`, `scarlet_cheese_wheel`, `unripe_flaxen_cheese_wheel`, `unripe_scarlet_cheese_wheel` | 16 → 32 | not food. The port sets 16: `development/backportedBrewin/.../registry/BnCItems.java:56-60` |
| "Pitcher drinks" | none found | | no item id or English name with pitcher / jug / carafe in the item_dict, the in-tree ports' lang or the lang of rusticdelight, moredelight, delightful, create_central_kitchen, abnormals_delight, neapolitan, fruitsdelight, farmersrespite (jars checked 2026-10-21) |

The bowl / bottle lists come from [food_catalog.md](../food_catalog.md)'s Leaves column (2026-10-20 `/dumpfood`)
compared with the JSON. The non-edible drinks come from stack_sizes.md 1.2.

### Build
All in [tools/gen_stack_sizes.py](../../tools/gen_stack_sizes.py):
1. Load the remainders and drinks the docstring already promises:
   ```python
   foods = json.load(open(EXPORT / 'food/foods.json'))['foods']        # rows: id, remainder, useAnim, ...
   remainder = {f['id']: f.get('remainder') for f in foods}
   drinks = json.load(open(EXPORT / 'food/drinks.json'))['drinks']      # id, remainder, classes, tags
   ```
   Row fields per `kubejs/server_scripts/_lib/dump_food.js:161-193` (`remainder`, `useAnim`, `classes`, `tags`).
2. `SWEET_32` (line 60) → `r'(ice_cream|pudding|custard|jello|shaved_ice)$'`. This covers the 22 jellos and the shaved
   ice by name. The remainder rule in step 4 would catch them too, but the name rule keeps the summary readable.
3. **Milk bottles 64, before any drink rule.** Members of `#forge:milk/milk_bottle` (already read in
   `tags = json.load(... 'tags/item_tags.json')`, line 145; key `i.t_milk_bottle` exists):
   `give(i, 64, 'bottle ingredient (milk)')`. Put it next to the `FOOD_OVERRIDES` loop. Check the tag's members in
   `item_tags.json`; if Neapolitan's bottle is missing, list both ids.
4. **Bowl / bottle foods, after the type loop (line 181):**
   ```python
   for i, t in sorted(food_type.items()):
       if i in universe and t not in INGREDIENT_TYPES | GOLDEN_TYPES and t != 'canned':
           if remainder.get(i) == 'minecraft:bowl':
               give(i, 32, 'bowl food')
           elif remainder.get(i) == 'minecraft:glass_bottle':
               give(i, 32, 'bottled food')
   ```
   `give` never overwrites, so golden (16), placed cakes (8) and ingredients (64) keep their earlier claim.
   `alexsmobs:fish_oil` (`special`, bottle, 64 today) would drop to 32: put it in `FOOD_LEAVE_ALONE`.
5. **Non-edible drinks:** for each `drinks.json` row with remainder `minecraft:glass_bottle`, no `PotionItem` in its
   `classes`, and not a bucket: `give(id, 32, 'drink (no food value)')`. **Potions must stay out**: `drinks.json` also
   lists vanilla and mod potions, and stacking potions is not part of this policy.
6. `CHEESE_WHEELS_32 = [...]` (the four ids above), `require(CHEESE_WHEELS_32, 32, 'cheese wheel')`, placed after
   `PLACED_FOOD_8`.
7. Fix the docstring if a source is still unused; run `python3 tools/gen_stack_sizes.py --list | grep -E "bowl food|bottled|no food value|milk|cheese"`.
   Expect about 46 new entries.
8. The JSON is read at startup: **restart** (not `/reload`). The log should show `[stack_sizes] set N stack sizes` with
   N = the JSON's entry count (1056 today) and no `no item_dict key` warning. Raw ids are allowed as entries, so new
   items need no item_dict keys.

### Ids and data
- Every id above was checked against the repo: [stack_sizes.md](../stack_sizes.md) (dump 2026-10-02),
  [food_catalog.md](../food_catalog.md) (2026-10-20), item_dict. The cheese wheel stack (16) was checked in the port's source.
- The inputs live under `kubejs/exported/` (git-ignored). The tool runs only on the game instance after `/dumpall`
  (or `/dumpfood`, `/dumpitemsbymod` and the tag dump).

### Decisions
- **Mangosteen Cake: 32 (bowl food) or 8 (placed cake)?** Recommend 32. It is served in a bowl, and
  `food/baked-goods #5` already says it is "not a cake" and should be renamed. Add it to `PLACED_FOOD_8` instead if it
  should count as a placed cake.
- **Unripe cheese wheels:** recommend 32 like the ripe ones; ageing happens on the placed block, so stacks don't matter for it.
- **"Pitcher drinks":** no such item in the pack. Recommend reading it as "every drink" (the non-edible drink rule
  covers anything a later mod adds). Ask the owner whether a planned item was meant.
- **Brewin' tankard drinks:** already 32 through `drink/alcohol`. The policy doesn't say otherwise; leave them.

### Gotchas
- "Rules never look at an item's current stack size" (docstring): keep it that way. The milk rule must use the tag,
  not "items stacking to 16".
- `ItemEvents.modification` sets the item's max stack. FD's `enableStackableSoupItems` (config) is overridden for any
  item the JSON lists (stack_sizes.md "What the pack changes").
- Stacks that don't merge because of NBT (`food/kitchen #5`, returned bowls and paper) are a separate bug. A bigger
  max stack doesn't fix it.

### Tests
stack_sizes.md has no test prefix. Add to [food.md](../food.md) section 7, after FO-28 (egg stacks). Take the next free numbers at build time; other briefs may claim FO-29 first:
- **FO-29 Bowl and bottle foods:** in survival, stack Apple Jello, Scrambled Eggs, Dog Food and Hamimelon Shaved Ice → 32 each.
- **FO-30 Drinks without food value:** Hot Cocoa, Gambler's Tea, Builder's Tea → 32; FD and Neapolitan Milk Bottle → 64.
- **FO-31 Cheese wheels:** Flaxen Cheese Wheel and Unripe Scarlet Cheese Wheel → 32.
- **FO-32 Potions unchanged:** a Potion of Swiftness still stacks to 1.

---

## stack-sizes #2: Stack sizes reference out of date
Status **Planned** · Work: tool · id `food/stack-sizes #2`

### Goal
Regenerate [stack_sizes.md](../stack_sizes.md) from a fresh `/dumpstacks`. The current file is from 2026-10-02,
before the stack size policy (DEPLOYED with 1.0.6).

### Now
- [tools/stack_sizes.py](../../tools/stack_sizes.py) is already up to date for the policy. It writes the
  "Most stack sizes are set by stack_sizes.js ..." line (lines 205-208). The checked-in markdown still says
  `item_properties.js` raised the eggs, because it is old output; `item_properties.js` was removed in 1.0.5 (food.md 3.9).
- Inputs: `kubejs/exported/stacks/stack_sizes.json` (`/dumpstacks`, [dump_stacks.js](../../kubejs/server_scripts/_lib/dump_stacks.js)),
  `kubejs/exported/food/{foods,drinks}.json` and `catalog.json` (food types; run `tools/food_catalog.py` after `/dumpfood`).
- The markdown is generated: "Don't edit this file: rerun the dump and the tool."

### Build
1. After #1 is built and the game restarted: `/dumpfood`, `python3 tools/food_catalog.py`, `/dumpstacks`,
   `python3 tools/stack_sizes.py`.
2. **Add a policy check** to `stack_sizes.py` (recommended, small). Read
   `kubejs/generation_scripts/stacks/stack_sizes.json`, resolve keys through item_dict (as `gen_stack_sizes.load_dict_keys`
   does), and list every item whose dumped stack differs from the policy size. Items the policy sets to 64 are absent
   from `/dumpstacks`; that counts as a match. This is the in-game proof for #1, and a startup `[stack_sizes] could not
   set` failure would show up here.
3. Check the summary table: "Cooking: Drinks" should have no 16s left; the jellos should move from 16 to 32 under
   Cooking: Food.
4. Update the README index row (`feature_writeup/README.md` line 40) with the new date and count.

### Decisions
- None. (Whether to add the policy check: recommend yes.)

### Gotchas
- `/dumpstacks` reports sizes after KubeJS edits, so run it after the restart that applied #1, not after a `/reload`.

### Tests
- The generated "policy check" section lists no mismatches.

---

## Built, needs play

None (no Testing or Complete rows in this subfamily).

## Writeup corrections made

None. stack_sizes.md is generated and stays as it is until #2 reruns the tool; its stale `item_properties.js` line
goes away then.
