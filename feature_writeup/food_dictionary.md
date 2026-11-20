# Food Dictionary and Diet Tracker (Spice of Life: Carrot Edition)

Owner request 2026-11-10 (TODO `food/types-and-tuning #16`): add Spice of Life: Carrot Edition only for its book, and
rename the two books.

| Book | Mod | Shown as | Does |
|---|---|---|---|
| `solapplepie:food_book` (`global.item.diet_tracker`) | Apple Pie Edition 2.2.0 | **Diet Tracker** | Food diversity, benefits, food queue. Unchanged gameplay. |
| `solcarrot:food_book` (`global.item.food_dictionary`) | Carrot Edition 1.14.0, LGPL-2.1 | **Food Dictionary** | Compendium of every food, eaten (unlocked) or not yet tried. |

## Facts

- Carrot Edition adds one item (the book), a tracker capability, a max-health modifier and a tooltip line. Mod id
  `solcarrot`, so no clash with `solapplepie` (separate registries, configs and capabilities). Decompiled copy:
  `DecompiledMods/SoL-Carrot-1.19.2-1.14.0/`.
- Recipes: Diet Tracker = book + potato (Apple Pie's), Food Dictionary = book + carrot (Carrot's). No overlap. Both are
  the jars' own shapeless recipes, left as shipped.
- Carrot's own gameplay is switched off so Diet Tracker is the only system that changes the player:
  `heartsPerMilestone = 0`, one unreachable milestone (100000), `baseHearts = 10`, `minimumFoodValue = 0` (every food
  unlocks), no reset on death; client: tooltip, hotbar progress, sounds and particles off.
  Files: `defaultconfigs/solcarrot-server.toml`, `config/solcarrot-client.toml` (copies under `configs_edited/`).
- Names: `kubejs/assets/creativedelight/kubejs/creativedelight/names.js` (food code lives in the Creative Delight tree).
  No resource-pack zip sets either mod's lang.
- Jar: `~/.local/share/atlauncher/downloads/SoL-Carrot-1.19.2-1.14.0.jar` -> `mods/`: copied 2026-11-13 (it was never copied on 2026-11-10, which caused the "carrot book not found" report, TODO `food/types-and-tuning #34`).
  `mod_sides.md` row added (both).
- Placed pie and cake bites unlock their slice in both books (plan P1, `PlacedFoodSol$CarrotBridge`). Unlock ignores
  Apple Pie's complexity config.

## Sorting (Raspberry Pie, built 2026-11-19)

Plan: [plan_diet_books.md](plan_diet_books.md) phase 2 (TODO `food-tuning #12` to `#15`). Code in Raspberry Pie
(`development/solRaspberryPie`), mixins gated on `solcarrot` 1.19.2-1.14.0. Carrot itself sorts both lists by one
fixed key (its own name lookup) and stores eaten foods without order.

- **Uneaten foods:** client config `[foodDictionary] uneatenSort` in `solraspberrypie-client.toml`: `MOD_NAME`
  (default: mod id, then name), `NAME` (A-Z), `EASIEST` (lowest Apple Pie complexity first, then lower nutrition),
  `HIGHEST_FOOD` (nutrition), `HIGHEST_SATURATION` (saturation points: nutrition x modifier x 2), `HIGHEST_DIVERSITY`
  (complexity; was `HARDEST`), `CUSTOM` (since 2026-11-20, plan_diet_books_r2.md A8). A config still on a removed value
  resets to the default.
- **Custom order:** `[foodDictionary] customOrder`, item ids listed first in their order, the rest after (uneaten
  order; mod and name when that is `CUSTOM` too). Empty (default) = `CUSTOM` hidden: the button skips it and a config
  on it sorts as the uneaten order. To be filled with the JEI food family order (`food-tuning #15`).
- **Eaten foods:** a sort button at the top right of the eaten-foods pages (after Sophisticated Backpacks' sort-type
  button, own code: a click steps to the next state, the tooltip names it, an item icon per state): newest unlock
  first (golden carrot), latest eaten first (clock; Apple Pie's meal memory, foods it no longer remembers last),
  highest food (cooked beef), highest saturation (rabbit stew), highest diversity (suspicious stew), custom (book and
  quill, only while configured); ties in the uneaten order. The old "uneaten order" state is gone (2026-11-20). The state is `[foodDictionary] eatenSort`. Unlock order is stored from the first eat after
  2026-11-19 (NBT `solrp_unlockOrder` on Carrot's list, synced and kept through death); older unlocks sort after the
  ordered ones, in the uneaten order.
- **Mouse wheel** flips one page per notch.
- Later: a hand-made type sort mirroring the JEI food family order (`food-tuning #15`).
- Tests: CQ-FDI-01 to -04 (drafts until the owner's look-check).

## Tests (prefix FDI)

Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): SQ-FDI-03, -03b PASS (bitten vanilla and double cakes count as cake slices in both books), SQ-FDI-04 PASS (a bite from each lunchbox tastes the food); found: a lunchbox slot accepts vanilla pumpkin pie, not a food in this pack (owner TODO).

Scratch run r1114 (2026-11-14, [scratch_run_2026-11-14.md](testing/scratch_run_2026-11-14.md) 8): SQ-FDI-01, -02 PASS (no Carrot hearts; max health 22 is the Raspberry Pie rung; the batch's 20th food, pumpkin pie, is not edible in this pack: swap it); SQ-FDI-03 FAIL: bitten cakes become Supplementaries directional cakes and count in neither book (TODO `food/types-and-tuning #39`).

- [ ] **FDI-01:** Boot with the jar; `grep -n "solcarrot|Failed to parse" logs/latest.log` -> mod listed, no errors
- [ ] **FDI-02:** Craft book + potato and book + carrot -> Diet Tracker and Food Dictionary; names right in JEI, tooltip and the keybind screen ("Open Diet Tracker")
- [ ] **FDI-03:** Open the Food Dictionary, eat a new food -> it moves to Already Tasted; no chat message, particles, sound or tooltip line; max health stays 10 hearts after 20 foods
- [ ] **FDI-04:** Food Dictionary lists modded foods (Creative Delight, Rustic Delight) and not lunchboxes or cans' containers -> listed as expected; note foods that should not be there
- [ ] **FDI-05:** Diet Tracker still works and its benefits are unchanged -> as before

## Plan 2026-11-10: world slices, lunchboxes (BUILT 2026-11-10, not installed, not run)

Built 2026-11-10 with the defaults D1 (Cake Slice) and D2 (blacklist only). P1: Creative Delight jar 23:24 in
`development/creativeDelight/build/libs/` (game was running, not installed); compiled against the Carrot jar, now also in
`development/creativeCommon/libs/`. P2: blacklist set in `defaultconfigs/`, `configs_edited/server_config/` and the
Apple Pie copies of New World, Structures and Workspace (no world has a Carrot copy yet; it is copied from
`defaultconfigs/` on first load). P3: no code, as read below. Tests SQ-FDI-03 to -05.

Owner request: update the Food Dictionary when a slice is eaten from the world, drop the lunchboxes from both books,
and make food eaten out of a lunchbox register as the food.

### P1. Placed slices also unlock the Food Dictionary

- Today `PlacedFoodSol` (creativeDelight `food/`, fed by `ServerPlayerGameModePlacedFoodMixin`) resolves a bitten
  pie or cake to its slice item and calls Apple Pie's `FoodTracker.updateFoodList(slice, player)` in `SolBridge`
  (guarded by `isLoaded("solapplepie")`). Carrot never sees the bite: its tracker only listens to
  `LivingEntityUseItemEvent.Finish` (and vanilla cake bites are not covered either).
- Change: add a second bridge class `CarrotBridge` (own inner class, so the outer class still loads without the mod),
  guarded by `isLoaded("solcarrot")`, doing what Carrot's `FoodTracker.onFoodEaten` does after its item checks:
  `SOLCarrotConfig.shouldCount(item)` and survival-mode rule (`limitProgressionToSurvival`), then
  `FoodList.get(player).addFood(item)`, `MaxHealthHandler.updateFoodHPModifier(player)` (0 hearts here, harmless),
  `CapabilityHandler.syncFoodList(player)`. Skip the celebration (sounds, particles, messages): they are configured off.
  `afterUse` calls both bridges, so the existing block map (`placed_food_slices.json`) serves both books.
- Vanilla cake: `afterUse` returns early for it because Apple Pie records it itself. Carrot does not, so the early
  return becomes "record for Carrot only" (the cake item maps to `farmersdelight:cake_slice` per the Apple Pie rule;
  decision D1 below: which item the vanilla cake unlocks in the Food Dictionary).
- Knife cuts record nothing (unchanged); the slice unlocks when eaten.
- Files: `PlacedFoodSol.java`, `ServerPlayerGameModePlacedFoodMixin.java` (no change expected), this doc, Carrot's
  config untouched. Needs a `:creativeDelight:` build, install, restart.

### P2. Lunchboxes dropped from both books

- Facts: the lunchboxes (`solapplepie:lunchbox`, `lunchbag`, `golden_lunchbox`, `creativedelight:netherite_lunchbox`)
  are `isEdible()` in code, so both mods list them as foods. Apple Pie already skips them when they finish being eaten
  (`FoodTracker.onFoodEaten` checks `FoodContainerItem`); Carrot does not, so eating from a lunchbox would also
  unlock the lunchbox itself.
- Change, config only: both mods have a filtering blacklist that hides items from the book and from counting
  (`*` globs allowed). Set in `solcarrot-server.toml` and `solapplepie-server.toml`:
  `blacklist = ["solapplepie:lunchbox", "solapplepie:lunchbag", "solapplepie:golden_lunchbox", "creativedelight:netherite_lunchbox"]`
  (defaultconfigs, `configs_edited/server_config/` copies, and per world `serverconfig/`). A later lunchbox needs a row.
- Checked in the sources: the lunchbox slot rule (`FoodContainerCapabilityProvider.isItemValid`) ignores the blacklist,
  so contents are unaffected. Carrot's book lists `FoodItems.getAllFoods()`, which drops blacklisted items. Apple Pie's
  book lists only the eaten-food queue (never a lunchbox), so there the blacklist only changes the lunchbox tooltip to
  "This food does not contribute to food diversity." SQ-FDI-05 confirms in game.

### P3. Food eaten out of a lunchbox registers as the food

- Reading of the bytecode: `FoodContainerItem.finishUsingItem` picks the best food slot, calls the food's own
  `finishUsingItem`, then `ForgeEventFactory.onItemUseFinish(player, foodStack, ...)`. That posts a real
  `LivingEntityUseItemEvent.Finish` carrying the inner food, so both Apple Pie and Carrot already see the food. With P2
  the outer event (the lunchbox) is ignored by both.
- So P3 is expected to need no code. If SQ-FDI-04 shows the food missing, fallback: a handler on the same event in
  Creative Delight (`lunchbox/`) that calls the P1 bridges for the contained food.
- Open point: Apple Pie's diversity queue gets the inner food once from the nested event; confirm it does not also get
  a second entry from the outer event (it should not, the `FoodContainerItem` check).

### Decisions

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | What does a bitten vanilla cake unlock in the Food Dictionary? | the Cake Slice item, like Apple Pie | Cake Slice |
| D2 | Hide lunchboxes by blacklist, or also remove the Carrot recipe path for them? | blacklist only | blacklist |

### Plan tests

- [ ] **FDI-06:** Bite a placed Farmer's Delight pie, a vanilla cake, a Creative Delight cake -> each unlocks its slice (cake: D1) in the Food Dictionary; Diet Tracker still records it
- [ ] **FDI-07:** Cut a pie with a knife -> nothing unlocks until the slice is eaten
- [ ] **FDI-08:** Open both books -> no lunchbox, lunchbag or other lunchbox tier listed
- [ ] **FDI-09:** Eat a food out of a lunchbox -> the food, not the lunchbox, is tasted in both books; Diet Tracker queue gets one entry
- [ ] **FDI-10:** Put each food kind into a lunchbox -> the blacklist did not change what the lunchbox accepts

## History

- 2026-11-10: names, item_dict keys, configs, sides row, this file. Jar not yet in `mods/`; nothing run.
- 2026-11-10: plan for world slices, lunchboxes and lunchbox eating (P1-P3, FDI-06..10); not built.
- 2026-11-10: plan built with D1 and D2 defaults (P1 Java, P2 config, P3 no code). Creative Delight jar not installed.
- 2026-11-14: bitten cakes (plan_bugfix_1114 4.3, TODO `food/types-and-tuning #39`): Supplementaries' `directional_cake` and `double_cake` map to the cake slice in `placed_food_slices.json`, and `PlacedFoodSol` records the first bite, when the cake turns into a directional cake. Built and installed; tests SQ-FDI-03 rerun, SQ-FDI-03b.
