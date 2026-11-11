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

## Built (2026-11-02)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-FCB-00, -01, -02, -03, -04, -04b, -04c, -04d, -04e, -05, -06, -08; rerun pending (batch fault) SQ-FCB-07, -08b.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-FCB-07, -08b.

Waves A and B built in one session (Wave C and `food/drinks #15` by two forks of it: SQ-RB2, SQ-DRK). Nothing booted.
Scratch rows: [scratch_queue.md](testing/scratch_queue.md) "SQ-FCB" (SQ-FCB-00 to -07, CQ-FCB-01 to -03). Changelog: families
Jams, Gameplay, Cans, Cooking for Blockheads, Miner's Delight, Veggies Delight, Barbeque's Delight. Needs the Creative
Delight jar rebuilt (members repackedFarmers -03, backportedVintage -13, backportedMiners -05) and a full restart: pack
startup dicts changed (`item_dict.js`, `fluid_dict.js`, `block_dict.js`).

Scratch (r1102b, 2026-11-02 evening, on the 14:01 Creative Delight jar; deep scan 2026-11-02): SQ-FCB-00 to -06 and
-08 PASS (s5 judged.md), SQ-FCB-07 and -08b batch faults (-08b real PASS; -07: block entity, placing, reload PASS, drops
and knife cut unproven). SQ-PSU-06 FAIL: the lemon (A9) and orange jam-from-slices pot recipes take a glass bottle, not
the mason jar (`dishes/jams #1`). The 21:10 Creative Delight build (copper cup removal, Miner's -05, Delightful -01) is not
in `mods/`.

| Row | Status | Where |
|---|---|---|
| A1 cabbage | Testing | CD `farmersdelight/recipes.js`: 4 leaves by board and mixer; the 2-leaf grid craft back to a cabbage now takes 4 (else a dupe) |
| A2 glycerol | Testing | CD `pneumaticcraft/tags.js` (`t.remove` from `#forge:sugar`); lang override deleted from `kubejs/assets/pneumaticcraft/lang/en_us.json` |
| A3 shulker meat | Testing | CD startup `endersdelight/item_merges.js`; `endersdelight/recipes.js`; GLM override `kubejs/data/endersdelight/loot_modifiers/scavenging_shulker_mollusk_from_shulker.json` (chance 0); `gen_can_types.py` shulker can on End's meat. End's own art kept; the owner updates the shulker textures later (2026-11-02). Partly undone 2026-11-04 (plan_meals_rolls_bbq.md C3): mollusk and filet out of the merge, GLM override deleted, filet cut back; stuffed shulker on End's meat |
| A4 taco | Testing | CD `corn_delight/recipes.js`. No roasted onion exists, so the slot is onion or any roasted bell pepper |
| A5 patty | Testing | CD `creativedelight/patties.js` (priority -90), `i.t_patty` |
| A6 kebab wrap | Testing | CD `barbequesdelight/recipes.js` |
| A7 squid | Testing | CD `miners_delight/loot.js`: `removeLoot(Ingredient.all)` then one squid; no Rustic gate |
| A8, A9 jams | Testing | CD `vintagedelight/jams.js` (1 jar), `fruitsdelight/recipes.js` (lemon slices; `f.lemon_jam` added) |
| A10 vinegar | Testing | CD `vintagedelight/jams.js`: jar recipe re-made through `R.vdPickling` on jam jars or `#forge:jams` |
| A11 prepared sea pickle | Testing | CD startup `hostiledelight/hostile_items.js`, `hostiledelight/recipes.js` |
| A12 copper cups | Testing | Superseded 2026-11-02 by the full removal (owner: the Copper Cup and every cup go): Miner's port and Delightful register no cup, details in [miners_delight.md](miners_delight.md) last section; SQ-CUP-01 to -05, CQ-CUP-01. |
| A13 Caesar | Testing | CD startup `oceanic_delight/item_merges.js` (survivor Veggies' `cesar_salad`), `veggiesdelight/cooking.js`, `oceansdelight/recipes.js` |
| A14 tempura | Testing | CD startup `pneumaticcraft/pneumatic_items.js`, `pneumaticcraft/recipes.js`, client renames dropped |
| A15 cornbread stuffing | Testing | CD `corn_delight/recipes.js` |
| A16, A17 pancakes | Testing | Already built 2026-11-01 (`creativedelight/members #15`); no change |
| A18 CfB racks | Testing | CD startup `cookingforblockheads/item_merges.js` (oak survivors), `cookingforblockheads/kitchen.js` |
| A19 seasoning names | Testing | CD Java `food/Seasonings` (name field), `food/SeasoningTooltip` (literal name when the lang key is missing), startup `seasonings.js` passes `name`. Root cause of the missing key not reproduced offline: check CQ-FCB-01 |
| A20 canned ratatouille | Testing | `gen_can_types.py` soup row; Large Meals added to `AWAIT_DUMP` (its 4 cans blocked every run: foods not in the 10-30 dump) |
| `dishes/jams #7` | Testing | Vintage port -13 `src/own/` (two jars, placeholder recolours of the cherry jar, tags, lang, soft fluids), Java `ModItems`/`ModBlocks`/`ClientSetup`, `port_resources.py` rerun; CD `vintagedelight/jams.js` |
| `cooking/kitchen #25` | Testing | repackedFarmers -03 `FDBackportRegistry` (three `CuttingBoardBlock`s, added to the cutting board BE type in common setup), assets/loot/lang; art `members/farmers/tools/stone_cutting_boards.py`; CD `farmersdelight/stone_cutting_boards.js` |
| `dishes/ocean #7` | Testing | CD startup `farmersdelight/ocean_dishes.js`, `farmersdelight/recipes.js`, sprite + notice in `kubejs/assets/creativeengineering/textures/item/` |
| `dishes/meals #40` | Pinned | Owner 2026-11-02: no sushi medleys for now |
| `food/stack-sizes #2` | Planned | Not run: needs a fresh `/dumpstacks` (the 10-30 dump predates the 11-01 stack rules) |

**`dishes/meals #40` proposal (pinned 2026-11-02, owner: no medleys for now; kept for later).** Four feast blocks in the Rustic member, built like Rice Roll Royale
(FD feast, 8 servings, FD's rice roll medley models with a recoloured `food` texture):
Hostile Roll Medley (Hostile Delight's guardian rolls and sashimi, Elder Guardian Roll), Egg Roll Medley (Oceanic's egg
roll x 8), Bell Pepper Roll Medley (Rustic's pepper rolls, 8 of the 9 colours or 9 servings), Shrimp and Sea Pickle Roll
Medley (Oceanic's shrimp roll and sea pickle roll slices; the sea pickle roll recipe takes raw or sliced sea pickle).
Needed: the roll list and serving count per medley, and whether placeholder recolours are fine for the art.

### Follow-up 2026-11-02 (owner answers)

- `cooking/kitchen #27` BUILT: sneak right-click with an empty main hand picks up the cooking pot, copper pot, kettle or
  skillet into the hand (block loot to the hand, the meal kept in the item, ingredients drop as on breaking, break
  protection respected); the handle toggles on sneak left-click with an empty hand, on the first hit, without breaking.
  CD Java `cooking/CookwareHandling` (QoL-7 `[qol] cookwarePickup`), tag `#creativedelight:cookware_pickup` (CD
  `creativedelight/kitchen_tags.js`). SQ-FCB-08, CQ-FCB-04. Jar built 13:52, not installed (a game was running).
- Plain matcha = the Matcha Latte (input of Ice Berry Matcha); nothing to build (creative_delight_drinks.md 8).
- `dishes/meals #40` pinned; shulker textures left to the owner.
