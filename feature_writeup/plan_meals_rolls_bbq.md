# Plan: Large Meals, Roll Delight and Barbeque's Delight changes (2026-11-04)

Source: the "Writeup plans" block of [todo/scratchpad.md](todo/scratchpad.md) (owner, 2026-11-04). Status: **BUILT 2026-11-04** (CD jar built, not installed; see "Build record" at the end).
Items marked "later" are not in this plan; they became TODO rows (section 5). The hostile delight / seasoning / misc notes in the
scratchpad's "Idea, Notes, Changes" block are not part of this plan (scratchpad rule).

Rules that apply to every step: recipes through `global.NCH` / `F.*` helpers, tags through `global.tags`, ids once in `item_dict.js`,
no emoji, no advancements, no conversions. Hand-edited config is copied to `configs_edited/`. Player-visible changes get a bullet in
`kubejs/assets/creativecommon/changelog/unreleased.md` (then `python3 tools/changelog_lint.py`). Live checks go to
[scratch_queue.md](testing/scratch_queue.md) as rows, not run unless the owner says so. Writeups updated in the same session as each part.

## 0. Order and cost

| Part | Needs | Rebuild | Restart |
|---|---|---|---|
| A. Large Meals | Java edit (pufferfish broth effect), generator and tooltip rows, scripts | `:creativeDelight:build` + install | full |
| B. Roll Delight | scripts, startup food values, rename in lang, one tool edit | `:creativeDelight:build` + install | full |
| C. Barbeque's Delight | member Java (raw item registry, tooltip), NCH/`F` skewer helper, scripts, lang | `:creativeDelight:build` + install (+ patcherMod only if the helper lives in `neoCraftHelper.js`) | full |

Do A, then B, then C (C is the large one and touches the shared skewer helper). One CD build and one install at the end if the game
is closed; check the game is not running first (CLAUDE.md redacted `ps` command). After the build: `/dumpall`, then
`python3 tools/gen_can_types.py`, `/cegen` (food families, placeables, stack sizes), `python3 tools/food_families.py --check`.

## A. Large Meals ([large_meals.md](large_meals.md))

| # | Change | How | Files |
|---|---|---|---|
| A1 | Placeable tooltips | Tooltip lines ("placeable", "on plate") come from the `#creativeengineering:placeable*` tags via `/dumpplaceable` and `tools/placeable_catalog.py`. The 13 Display Delight `lm_` blocks exist, so after `/dumpall` + `/cegen` the catalog should list them; check that each Large Meals food whose DD block exists gets its line. If a food is missing, its DD block or `displaydelight:*` tag link is the cause, fix that, not the tooltip script | `generation_scripts/food/placeable.json` (generated), `backportedDisplay` only if a link is missing |
| A2 | Mushroom Pot Pie into `feast/whole` | `food_families.py` classes any "whole" block with "pie" in the name as `pie/whole` (line ~280), so the pot pie block lands in Pie. Add a `FAMILY_OVERRIDE` entry for the pot pie block id (and keep the serving item in `feast/serving`), same mechanism as the sponge cake override | `tools/food_families.py` |
| A3 | Cans | `AWAIT_DUMP` still holds `largemeals` (soups, meals wait on the dump). After `/dumpall`: remove `largemeals` from `AWAIT_DUMP`. Raw and cooked mutton racks: no can (confirm the generator's meat rule skips them; add explicit exclusions if not). Feasts (omurice, mushroom pot pie, roasted rack of mutton) get a `feast` can row each, the same pattern as `canned_shepherds_pie` (`dict(input=['<block>'])`, value source = the serving). The roasted rack feast follows the pattern too. Soups (4) and meals (4) stay on the per-type rule | `tools/gen_can_types.py`, then `/cegen` can step |
| A4 | Pufferfish Broth nausea | Add a short Nausea effect to the item in the Java food table (`MealItems.java`, `registry/`). Nausea 5 s (owner), amplifier 0, chance 1.0. Keep the Comfort 1 min effect. Decision D3 of the writeup (broth gives no poison) is unchanged. JEED text: `effects` description exists for Nausea, so no new row; check the food effect tooltip shows it | `members/largemeals/.../registry/MealItems.java` |
| A5 | Cod Deluxe and Hearty Lunch are plate meals | Handled as the pack handles every other plate meal (owner): family `meal/plate` through `FAMILY_OVERRIDE` rows in `food_families.py`, food type `meal`, and whatever else the `meal/plate` family rules give its members (Food Plate placement, recipe shape, container). Before building, read the `meal/plate` row of the family rules ([food_families.md](food_families.md)) and apply each line to both foods; the container is changed only if that rule says so | `tools/food_families.py` |
| A6 | Omurice feast, Tomato Egg Soup soup | Already so in `largemeals/food_types.js` (omurice in `meal/feast`, tomato egg soup in `soup`). Verify the family output after `/cegen` (omurice block `feast/whole`, serving `feast/serving`, soup `soup`) and fix any name-based misclassification (the word "egg" or "rice" must not pull them into `egg/dish` or `meal/rice`) with `FAMILY_OVERRIDE`. Also check the can type for Tomato Egg Soup is `soup` | `tools/food_families.py` |
| A7 | Writeup | Update steps 12 (stack sizes), 13 (cans and jars) and section 8 (tests) of `large_meals.md`: feasts now cannable, racks not, pot pie family, broth nausea, plate meals; add tests LGM-21 (placeable tooltips on all 13 `lm_` foods), LGM-22 (broth gives nausea ~10 s), LGM-23 (feast cans craft and uncan), LGM-24 (no can recipe for either rack) | `large_meals.md`, `scratch_queue.md` |
| A8 | Changelog | Bullets under "Large Meals": Pufferfish Broth gives Nausea; the three feasts can be canned. Families and tooltips are not player-noticeable beyond the tooltip lines (one bullet if the tooltips are new for these foods) | `unreleased.md` |

## B. Roll Delight ([roll_delight.md](roll_delight.md))

| # | Change | How | Files |
|---|---|---|---|
| B1 | Cooked Nether Wart is not a cooked vegetable | Remove it from the `cooked/veggie` type: change `roll_delight/food_types.js` (and the generated source `food_catalog`/`food_types.json` override) to a type of its own that no cooked-veggie recipe, tag or rule reads (for example type `ingredient`, family none or `ingredient/other`; check what bowl-returning non-veggie ingredients use). Remove it from every tag the cooked-veggie type feeds (cooked veggie recipe inputs, compost group, cooked-veggie can type). **The can stays** (owner): the generated row `canned_cooked_nether_wart` (`gen_can_types.py` line 286) is kept. It names the can type `cooked_veggie` for the can's values and recipe only; check that the row does not read the food type tag, and if it does, give the row an explicit source so the can still generates Nether's Delight integration is a later row | `roll_delight/food_types.js`, `tools/gen_can_types.py` (check only), `tools/food_families.py`, `roll_delight/tags.js`, `roll_delight/compost.js` if the group changes |
| B2 | Salmon Maki into the slice family | Rename the display name "Salmon Maki" (the slice) to "Salmon Maki Roll Slice" to match "Rice Roll Slice", "Mushroom Roll Slice" etc. Ids unchanged (`amazing_salmon_roll`, `amazing_salmon_roll_slice`). Lang goes under `assets/roll_delight/` of the member, plus `names.md` 3.3 and `display_delight` placed name if one exists. In `food_families.py`, make sure the slice classifies as `roll/slice` like the other slices (a name check on "slice"; add `FAMILY_OVERRIDE` if the old name kept it out). Check the Display Delight small-plate entry and the Legendary Roll Medley tooltip text use the new name | member `en_us.json`, `RollTranslator` lang patch, `names.md` |
| B3 | Golden Roll values from the golden rule | Drop the Golden Dried Kelp copy in `kubejs_startup/roll_delight/food_values.js` (4 / 1.2 whole, 2 / 1.2 slice, always edible, stack 16) and its effects (Regeneration II, Absorption I, Nourishment on the whole roll). Whole roll: the golden rule of [food.md](food.md) 3.16 on its base, the Rice Roll (14 / 0.6): x2 hunger capped at 20, modifier unchanged, so **20 / 0.6**. Add the pair Rice Roll to Golden Roll to the pair list in CD `goldenfood/golden_values.js` so it is applied like the other golden foods. Slice: static **10 / 0.5** (half the whole roll's hunger, the Rice Roll's own whole-to-slice ratio, so three slices are 1.5x the roll as for every roll). No effects on either (golden buffs by family are the later row `food/types-and-tuning #24`). The always-edible flag and the stack 16 go with the copy: the roll takes the golden-tier defaults of `golden_values.js`, the slice stack 64 | `kubejs_startup/roll_delight/food_values.js`, CD `goldenfood/golden_values.js` |
| B4 | Remove the kelp reference | Nothing in the roll files refers to Golden Dried Kelp any more after B3 (grep `golden_dried_kelp` in `roll_delight/` and `goldenfood/golden.js` next to the roll). Golden Dried Kelp itself is untouched | `roll_delight/*`, `goldenfood/golden.js` |
| B5 | Golden Roll Slice cannot be gilded | The slice is already produced only by cutting. Make sure the `goldenGild` / `goldenEnchant` sources do not accept it: the golden food tag (`golden`) must not also be the gilding-input tag for the slice, and the gilding sources tag (slice family) excludes `golden_roll_slice`. Check JEI shows no gilding recipe with the slice as base or result | `goldenfood/golden.js` or tags, `roll_delight/tags.js` |
| B6 | Rolls and medleys not cannable (golden too) | `gen_can_types.py` line 434 lists `golden_roll(_slice)` in a golden set that makes golden cans. Remove them. Rolls are already "never canned" by family rule; verify no generated can for any Roll Delight roll, slice or medley (grep `can_types.json`, also check the `canned` map in `roll_delight/food_types.js` is empty). Legendary and Mythical Medley are feast blocks: they are excluded from the Large Meals feast-can pattern (A3 applies to Large Meals only) | `tools/gen_can_types.py`, generated `can_types.json` |
| B7 | Writeup | `roll_delight.md`: 2.3 (golden rule values), 3.1.1 table row for Golden Roll, 4.1 (Cooked Nether Wart row: no longer `veggie/cooked`, no can), name change, section 8 tests: RD-/ROLL- ids for nether wart not in cooked-veggie recipes, slice name, golden roll values, slice not gildable, no roll can | `roll_delight.md`, `scratch_queue.md` |
| B8 | Changelog | Bullets under "Roll Delight": Salmon Maki renamed Salmon Maki Roll Slice; Golden Roll and slice values changed (old values in brackets); Cooked Nether Wart can no longer be canned and is not used by cooked-vegetable recipes | `unreleased.md` |

## C. Barbeque's Delight ([barbeques_delight.md](barbeques_delight.md))

Scope (owner): all 10 BBQ lines are part of every change below (helper, raw items, family, no canning, names), together with the pack skewers.

Current state: the 10 BBQ lines have raw (not edible) and cooked skewers, a Basin skewering recipe, Grill / furnace / smoker /
campfire cooking, and a shaped `_manual_only` cooked-ingredient craft (4.5). The pack's skewers (Barbecue on a Stick, End
Barbecue Stick, Improvised Barbecue Stick, Nether Skewer, Seafood Skewer, Turnip Mutton Skewer, Cave Skewer, Crispy Skewer) have only
a cooked item and are crafted shaped plus one sequenced assembly with cooked inputs (`F.skewerAssemble`).

| # | Change | How | Files |
|---|---|---|---|
| C1 | Seasoning tooltip matches the applied-to-food tooltip | The seasoning item's own tooltip should read like the line the food shows once seasoned (same wording, same colour style, effect numbers as `Seasonings.foodProperties` shows them). Find the two producers: item tooltip (`SeasoningItem` / `BBQClient`, and the CD seasoning framework's seasoning-item tooltip) and the applied-line builder in CD `food/Seasonings`. Make the item tooltip call the same formatter for the same seasoning data in `seasonings.js`, so the six BBQ seasonings and salt show consistently | CD `food/Seasonings`, member `item/SeasoningItem.java`, client tooltip, lang |
| C2 | Skewer helper: 2 or 3 input ingredients | Wrap the repeated pattern into one helper in the skewer family style: `F.skewer({ name, raw, cooked, inputs: [a, b] or [a, b, c], ... })`. Inputs 2: the diagonal `[' AB','ABA','S  ']`-style shapes exist for 3A+2B; the helper takes 2 or 3 distinct ingredient groups and picks the shape. It emits: (1) the raw skewer shaped craft from raw inputs plus stick (swap order and shape to match the Barbecue on a Stick craft), (2) the grill / furnace / smoker / campfire cooking recipes raw to cooked (the cooked item keeps seasoning NBT as the Grill does for BBQ lines), (3) the shaped `_manual_only` cooked-ingredient craft and the sequenced assembly (the later row covers assembly of raw skewers; the helper keeps the existing cooked assembly). The 10 BBQ lines move onto it as a refactor with no change to their results (check the generated recipe ids stay the same, or a diff of the dump shows only intended ids). Located in CD `kubejs_startup/creativedelight/cd_food.js` next to `skewerAssemble`; no patcherMod rebuild needed unless NCH itself changes | `cd_food.js`, `barbequesdelight/recipes.js` |
| C3 | New raw skewers for the pack skewers | A raw item per cooked pack skewer, namespace of the original mod via a CD member (4.3 #2 default; for ids the member must register; tell me in the build if a donor mod forbids it: Cooking for Blockheads precedent does not apply here), stack 64, not edible, grill time from the closest BBQ line. Texture: reuse the raw cod skewer texture for all for now (no recolors). Raw names "Raw TYPE" (O34). Items listed in the plan: Nether Skewer, Turnip Mutton Skewer, Barbecue on a Stick, Crispy Skewer, Seafood Skewer, Cave Skewer. End Barbecue Stick and Improvised Barbecue Stick are included (owner), so all eight pack skewers get a raw item. **End Barbecue Stick (owner):** the craft makes **1** skewer (upstream: 3 skewers from 3 sticks, shapeless `ends_delight:food/end_barbecue_stick` with roasted shulker meat, roasted dragon meat, chorus fruit, dried endermite meat, 3 sticks). New craft: shapeless, dried endermite meat dropped, 1 stick, shulker meat swapped to **Ender's Delight's Shulker Mollusk** (`endersdelight:shulker_mollusk`, dict key `i.shulker_mollusk`), used exclusively: no `ends_delight:` shulker meat and no `#forge:shulker_meat` tag in this craft. Raw craft: Shulker Mollusk + raw dragon meat + chorus fruit + stick, giving 1 raw End Skewer; the heatless `_manual_only` cooked craft uses the cooked form of the mollusk (check what Ender's Delight cooks it into; if it has none, the cooked craft takes the mollusk too and the cooking step is the skewer's) plus roasted dragon meat, same shape, 1 result. **Conflict to resolve first:** the 2026-11-02 merge (`dishes/ingredients #32`, Java item merges in CD) turns the Shulker Mollusk into End's Delight's shulker meat and its filet into the meat slice, so the mollusk cannot be obtained. For this craft to work the merge must be undone for the mollusk (and filet if the cooked craft needs it): remove those two entries from the merge, restore the Ender's knife drop and cutting recipe that `#32` turned off, and update the `#32` row and `plan_food_cooking_batch.md`. That reverses part of an earlier owner decision, so it is listed as a question below Remove upstream `ends_delight:food/end_barbecue_stick` with `R.remove`; check the raw dragon meat key exists in `item_dict.js` or add it once | new raw ids in `item_dict.js`, `food_types.js` (raw skewers are not food), `tags.js` (`#barbequesdelight:raw_skewers` gets the new raws so powders and the Grill accept them), member item registry |
| C4 | Existing pack skewer recipes onto the helper | **Nether Skewer** (Nether's Delight) and **Turnip Mutton Skewer** (Veggies): raw skewer recipe from existing ingredients (nether: its current cooked inputs become raw/uncooked equivalents where they exist; turnip mutton: `i.cooked_mutton` becomes raw mutton, turnip stays), grill raw to cooked recipe. **Barbecue on a Stick, Crispy Skewer, Seafood Skewer, Cave Skewer**: the existing shapeless/shaped craft is swapped to produce the raw item with uncooked ingredients, using the Barbecue on a Stick craft shape and ingredient order; raw is cooked by Grill / furnace / smoker / campfire. The cooked-ingredient `_manual_only` craft stays as the heatless route. Remove or replace the old crafts via `R.remove` (as `veggiesdelight/recipes.js` line 107) | `veggiesdelight/recipes.js`, `endersdelight/recipes.js`, `oceansdelight/recipes.js`, `nethersdelight/*`, `farmersdelight/*`, `hostiledelight/*` recipe files |
| C5 | Skewers are their own family | `food_families.py`: cooked pack skewers and BBQ cooked skewers all `skewer`; raw skewers get `skewer/raw` (or `skewer` with the raw flag) and are not edible; the Skewer row in the family rules (shaped + one assembly, stick kept, Food Plate) updated; `NO_FAMILY` removals. The Kebab Wrap / Sandwich stay `sandwich/*`; Bibimbap `meal/rice` | `tools/food_families.py`, `food_families.md` |
| C6 | No canning | Raw and cooked skewers, kebab items: confirm `gen_can_types.py` has no skewer rows and the family `skewer` has `can=` none (the family table says "never canned"). Add an explicit exclusion if any skewer item is picked up by a type rule | `tools/gen_can_types.py` |
| C7 | Kebab Sandwich is a bread roll | Family `sandwich/roll` (already by name) and the combine recipe's bread input is the bread-roll tag (`forge:bread/roll` or the pack key; check the tag name in the tag dump). Kebab Wrap stays on tortilla (done) | `barbequesdelight/recipes.js`, `tags.js` |
| C8 | Combos: no combine recipe, no carried skewer effects, static values | Kebab Wrap, Kebab Sandwich and Bibimbap become ordinary shaped / bowl recipes through NCH and the sandwich / meal assemblies (`dishes/meals #48` rules), taking cooked skewers as ingredient items. The `combine` recipe type and the `content` NBT go unused for these three, and `BBQSandwichItem` no longer applies stored skewer effects; they keep their own base effects (Wrap Nourishment 1:00 at 50%, Bibimbap Nourishment 2:00). They get normal JEI recipe pages. Items already in worlds with `content` NBT stay valid (no conversion). **Static values** (hunger / saturation modifier), from the average of the ten cooked skewers (hunger 7.6, saturation 11.2 points, the sum of the 3.1 table over ten) plus the base foods, times 0.8, capped at the 16 hunger ceiling of upstream's biggest combo: Wrap (tortilla 3 + skewer 7.6 + cabbage 2 = 12.6 hunger, 15.2 points) **10 / 0.6** (upstream 8 / 0.7); Sandwich (roll + 2 skewers + cabbage + onion = about 23 hunger, 30 points; capped) **16 / 0.6** (upstream 14 / 0.7); Bibimbap (rice 6 + onion 2 + carrot 3 + 3 skewers 22.8 = 33.8 hunger, 43.6 points; capped) **16 / 0.8** (upstream 16 / 0.7). Written into the startup food-values file as fixed numbers; no runtime averaging | `barbequesdelight/recipes.js`, member `CombineItemRecipe` / `BBQSandwichItem`, startup food values, `food_types.js` |
| C9 | No placed models | Not in this plan (later; section 5) | |
| C10 | Writeup and tests | `barbeques_delight.md` 3.1, 3.10, 4.5 (what is built), 4.3 #2 (answered), section 8: BBQ-30 (seasoning tooltip equals food tooltip), BBQ-31 (every pack skewer: craft raw, grill, smelt, smoke, campfire to the cooked item), BBQ-32 (raw pack skewers take powders, refuse sauces), BBQ-33 (all skewers one family in the family dump, none cannable), BBQ-34 (kebab wrap / sandwich / bibimbap craft, no skewer effects, values), BBQ-35 (recipe ids of the 10 BBQ lines unchanged after the refactor) | `barbeques_delight.md`, `scratch_queue.md` |
| C11a | Rename "Barbecue Stick" items to "Skewer" | Lang only, ids unchanged: Barbecue on a Stick to **Barbecue Skewer**, End Barbecue Stick to **End Skewer**, Improvised Barbecue Stick to **Improvised Skewer**, with raws "Raw Barbecue Skewer", "Raw End Skewer", "Raw Improvised Skewer". Also the Display Delight placed names (`displaydelight/lang/en_us.json` "Plated Barbecue on a Stick", "Plated End Barbecue Stick", and the Improvised one), `names.md`, and the family-rule text; the `food_families.py` name rule keys on "skewer" so these three then classify as `skewer` by name | `kubejs/assets/.../names.js`, `displaydelight/lang/en_us.json`, `names.md` |
| C11 | Changelog | Bullets under "Barbeque's Delight" / "Skewers": raw versions of the six (eight) skewers added, crafted from raw ingredients and cooked on a grill, furnace, smoker or campfire; Kebab Wrap, Sandwich and Bibimbap no longer carry skewer effects, values changed (old in brackets); Kebab Sandwich on a bread roll; seasoning item tooltip matches | `unreleased.md` |

## D. Testing and hand-over

- Offline: `./gradlew :creativeDelight:build --offline`; `python3 tools/food_families.py --check`; `python3 tools/changelog_lint.py`;
  KubeJS recipe load is only proven in game (scratch queue rows written, run when the owner says so).
- Scratch rows (S tier): SQ-LGM-11..14 (A), SQ-ROLL-xx (B), SQ-BBQ2-xx (C, recipes, family and can dumps). Client rows (C tier):
  tooltips (A1, C1), names (B2, C3), JEI pages for the new raw skewers and combos.
- Hand-over lists the test ids and the CD jar if the game was open.

## E. Owner answers (2026-11-04) and remaining assumptions

Answered: all eight pack skewers get raws; End Barbecue Stick makes 1 skewer, endermite meat dropped, shulker meat to End's Delight's; "Barbecue Stick" names become "Skewer"; plate meals follow the pack's `meal/plate` handling; Cooked Nether Wart can kept; Golden Dried Kelp value and effect copy dropped; kebab values static (C8); Pufferfish Broth Nausea 5 s.

Assumptions I made, tell me if wrong:
1. "All 10 skewers in the merge" is read as the 10 Barbeque's Delight lines joining every change in part C.
2. Shulker meat in the End Skewer craft is Ender's Delight's Shulker Mollusk only (updated 2026-11-04). This needs the 11-02 shulker merge undone for the mollusk. Scope assumed: only the mollusk (and filet if needed) leave the merge, and every other recipe keeps End's Delight meat. If you want the whole merge reversed or the mollusk used in other recipes too, say so; that is a separate change to `dishes/ingredients #32`.
3. Golden Roll slice is 10 / 0.5 and the roll loses its always-edible flag with the kelp copy.
4. Kebab ceiling of 16 hunger is mine (upstream's largest); change if you want the full 0.8 x input.

## F. Not in this plan (TODO rows)

See section 5 below the table; ids added to [todo/TODO.md](todo/TODO.md) 2026-11-04.

## 5. Later items

| TODO id | Item |
|---|---|
| `dishes/meals #57` | Sequenced assembly for raw skewers (extends `dishes/meals #46`) |
| `dishes/meals #58` | Cave Skewer: integrate with Miner's Delight (merge); also put in the skewer family |
| `food/placeable #5` (existing) | Placed models for skewers (raw and cooked), added to the note |
| `dishes/meals #59` | Cooked Nether Wart: integrate with Nether's Delight |

## Build record (2026-11-04)

Built in one session; `./gradlew :creativeDelight:build --offline` passed (20:33). **Not installed:** a parallel session
is mid-way through the patcherMod split (`creativeCommon` / `creativeEngineering`, 884 changed paths), and a CD jar built
against it should go to `mods/` together with those jars. Install with `:creativeDelight:installToMods` once that work
is installed.

Deviations from the table above:
- **B6:** the `golden_roll(_slice)` entry in `gen_can_types.py` is `SKIP['golden']`, which keeps them out of golden cans;
  removing it would have made them cannable, so it stays. No roll can exists (checked).
- **B3:** the always-edible flag, the stack of 16 and the effects lived in the member's Java registration, not only in the
  kelp copy: both rolls now register 20 / 0.6 and 10 / 0.5, stack 64; the copy file `roll_delight/food_values.js` is deleted.
- **B8 / B1:** the can stays (owner, B1), so there is no "can no longer be canned" bullet.
- **C2 / C4:** all 18 lines are in a new `barbequesdelight/skewers.js` (not spread over the mod files); the old pack
  skewer recipes are gone from the FD, Veggies, Ender's and Ocean's files. Pack raw crafts are shaped one-of-each
  diagonals (the End Skewer shapeless); Nether and Cave Skewers have no heatless craft (their inputs have no cooked form,
  and a same-input craft would clash with the raw one).
- **C3:** Ender's Delight has no cooked mollusk, so the heatless End Skewer takes the mollusk. Assumption 2 kept: the
  stuffed shulker moved to End's meat so only the End Skewer uses the mollusk.
- **C7:** no bread-roll tag exists; the long-roll sandwich family is whole bread (`#forge:bread`), which the Kebab
  Sandwich already takes. No change.
- **C8:** the static values are set in `BBQItems.java` (there is no startup food-value file for the member).
- **C1:** salt is not a pack seasoning (The Salt's "Salted"); only the six BBQ seasonings changed.
- **C5:** raw skewers are not foods, so they have no family (no `skewer/raw`).
- **A1, A3 (AWAIT_DUMP), A6:** wait on `/dumpall` and `/cegen` (dumps predate the three mods).
- **Display Delight names:** set by hand; `gen_display_delight_names.py` currently loses 12 unrelated names (sources
  moved by the split), so it was not rerun.
