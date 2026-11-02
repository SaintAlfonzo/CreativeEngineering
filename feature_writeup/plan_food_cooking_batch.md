# Plan: food and cooking batch (2026-11-02)

Owner-selected rows from the Food Rules, Drinks, Kitchen, Baking and Dishes categories of [TODO.md](todo/TODO.md). Status stays
**Planned** until built. Every row needs its scratch rows queued in [scratch_queue.md](testing/scratch_queue.md) and a changelog bullet in the
same session as the build (CLAUDE.md "Done means"). Recipes go through `global.NCH`, tags through `global.tags`, ids through `item_dict.js`.

Row corrections: `dishes/ingredients #26` is already Testing (fixed 2026-11-01, run SQ-MRD-04), so it is not in this batch.
"merges/baking #5" does not exist; read as `merges/baking #4` (rename stackable pancakes).

## Wave A: script-only (needs /reload; scratch S tier first)

| Order | Row | Decision / approach |
|---|---|---|
| A1 | `dishes/ingredients #25` | Cabbage cutting yields 4 leaves. |
| A2 | `dishes/ingredients #29` | Glycerol removed as a sugar source in every recipe; remove its sugar tooltip (`ui/tooltips #6`). |
| A3 | `dishes/ingredients #32` | One shulker meat: End's Delight's. Every recipe and tag that used another shulker meat is merged to it. End's Delight textures updated to the merged item. Follows the ARR route in memory `arr-mod-overrides-route` if new ids are needed. |
| A4 | `dishes/meals #51` | One taco recipe taking any roasted pepper and any roasted onion (tags). |
| A5 | `dishes/meals #55` | Vegan patty added to the meat patty tag/ingredient in every recipe. |
| A6 | `dishes/meals #47` | Kebab Wrap combine recipe on `forge:tortilla`, keep skewer-effect NBT (matches `dishes/meals #15`). |
| A7 | `dishes/ocean #9` | Squid drops exactly one full squid (loot table). |
| A8 | `dishes/jams #5` | Unify: grape and cherry jam give 1 jam each (check `#4`). |
| A9 | `dishes/jams #6` | Lemon jam from 5 lemon slices. |
| A10 | `dishes/pickles-and-ferments #11` | Drop the vinegar bottle recipe; vinegar accepts any jam. First check which jams CD `vintagedelight/jams.js` already ferments through jam tags. |
| A11 | `dishes/pickles-and-ferments #12` | Remove prepared sea pickles (recipes, JEI, loot). |
| A12 | `dishes/meals #53` | Remove every copper-cup food. |
| A13 | `merges/dishes #6` | Caesar salad merge: spelling "Caesar", Veggies Delight texture, lettuce + tomato + cheese + bread; Vintage `cesar_salad` merges into Oceanic `caesar_salad` (or the reverse; pick the survivor by id stability). |
| A14 | `merges/dishes #11` | Drop Fish Tempura, Raw Fish Tempura and the tempura fish and chips; keep fried fish. |
| A15 | `merges/dishes #12` | One cornbread stuffing recipe, any baked potato. |
| A16 | `merges/baking #3` | Rustic Delight pancake merged into the Supplementaries pancake (Supplementaries texture). |
| A17 | `merges/baking #4` | Stackable pancakes renamed "Pancakes" plus a stacking tooltip. |
| A18 | `cooking/kitchen #24` | Remove old Blockheads spice rack, tool rack and baskets; replacements stay usable by Blockheads. |
| A19 | `food/seasoning #16` | Seasoning tooltips show the seasoning names. |
| A20 | `cans/canning #18` | Canned ratatouille through the per-type can generator. |

## Wave B: new content (restart needed; client check C)

| Row | Approach |
|---|---|
| `dishes/jams #7` | Salmonberry and raspberry jam, new ids in `item_dict.js`, jam tags, textures. |
| `food/drinks #15` | Build the drink rules exactly as decided in [creative_delight_drinks.md](creative_delight_drinks.md): new teas and juices on reserved ids, kettle flavoured coffees, heated mixing, renames, matcha +1 powder, cloud latte; folds `food/drinks #8`, `#10`, `merges/drinks #1`, `flowers #5`. Largest row; do last in the wave. |
| `cooking/kitchen #25` | Stone cutting boards: granite, andesite, diorite. Textures are the polished granite/andesite/diorite textures painted onto the cutting board model (not swapped in as the whole texture), without the block edge texture. Recipes via `global.NCH`. |

## Wave C: patcherMod (one rebuild, install, restart; never while the game runs)

| Row | Approach |
|---|---|
| `food/seasoning #14` (Bug) | Powdered Sugar block named Snow Block in WTHIT: override the name and mod in the WTHIT plugin or the block's name. |
| `food/seasoning #15` | Seasoned icon moved to the top right (overlay position; resource pack plus patcherMod). |
| `cooking/kitchen #27` | Sneak right-click picks up cooking pot, copper pot, kettle, cooking pan. |
| `food/drinks #17` | Bottling returns go to the plating slot automatically. Scope set 2026-11-02: ensure overflow (extra output spills to the player/world, nothing lost), a plate queue, and every other Farmer's Delight workstation tweak, applied to the cooking pot and oven, and extended to the copper pot, kettle and fermenting jar. |

## Tools

| Row | Approach |
|---|---|
| `food/stack-sizes #2` | Rerun `/dumpstacks`, then `tools/stack_sizes.py` and `gen_stack_sizes.py`; refresh [stack_sizes.md](stack_sizes.md). |

## Added 2026-11-02 (owner confirmed)

- `merges/baking #4` confirmed as the intended row (the "#5" in the request was a typo).
- `dishes/ocean #7` (Kelp Encrusted Cod), execute as written: new pack-namespace item (key in `item_dict.js`), pot recipe cod slice + egg + kelp through `global.NCH` (xp 0.35), 5 / 0.6 to start then food tuning, texture from the Ocean's Delight 1.0.4 jar (MIT). See [ocean_delight.md](ocean_delight.md) 3.1. Wave A (recipe, item key) plus the texture asset; C and T tiers follow.
- `dishes/meals #40` (sushi roll medleys), execute: hostile, egg roll, bell pepper rolls, and the shrimp and sea pickle roll. The sea pickle roll merges with the raw and sliced sea pickle (prepared sea pickles go in A11, so the roll takes raw or sliced sea pickle only). Related `creativedelight/members #14`. Wave B (new ids and textures).

## Order and checks

1. Wave A in the order listed (A3 and A13/A14 touch shared item merges; run one scratch pass after each cluster).
2. Wave B after A (jam #7 depends on the jam unification A8).
3. Wave C as one build. Test IDs and scratch rows added per row in the same session.
4. After each wave: `python3 tools/changelog_lint.py`, update the feature writeups, set rows to Testing.
