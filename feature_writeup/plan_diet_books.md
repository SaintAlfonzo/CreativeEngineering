# Plan: Diet Log and Food Dictionary polish (Raspberry Pie, SoL Carrot)

<!-- generated:status_index begin open_work sha1=9c7023d508d9 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 14 open TODO rows (Planned 14), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `food-tuning #2` | Planned | S P | Diet window 30 meals (endDecay 32 -> 30), respec ages out 5 (was 8) |
| `food-tuning #3` | Planned | S C | Diet Log: Apple Pie food history pages removed; last 30 meals list (duplicates shown) |
| `food-tuning #4` | Planned | C | Diversity shown rounded down (4.97 showed 5 before milestone 5) |
| `food-tuning #5` | Planned | C | Diet Log: per-page x, y, scale config; ladder and milestones one line lower |
| `food-tuning #6` | Planned | C | Diet Log: book background stretched by repeating its center strip, configurable width |
| `food-tuning #7` | Planned | C | Diet Log: mouse wheel flips pages |
| `food-tuning #8` | Planned | C | Diet Ladder bar: even spacing, last achieved milestone + next 4 |
| `food-tuning #9` | Planned | C | Diet Log icons: locked unlocks alternate lock/effect; unlocked effect gray with colored frame; slot rungs metal frame, no effect |
| `food-tuning #10` | Planned | C | Gate text: N more unique food(s) to activate diet |
| `food-tuning #11` | Planned | C | Effect screen: smaller Change/Drop label text (buttons unchanged); hover shows how many foods age out |
| `food-tuning #12` | Planned |  | Investigate Sophisticated Backpacks sort-type button for the Food Dictionary toggle |
| `food-tuning #13` | Planned | C | Food Dictionary: uneaten sort configurable (mod+name, name, easiest, hardest) |
| `food-tuning #14` | Planned | S C | Food Dictionary: eaten list 3-state sort button (newest unlock, latest eaten, uneaten order) |
| `food-tuning #15` | Planned |  | Food Dictionary: hand-made type sort mirroring the JEI food family order (after that is done) |
<!-- generated:status_index end open_work -->

Source: owner notes, `todo/scratchpad.md` blocks "raspberry pie" and "sol carrot" (intake 2026-11-18). Phase 1 is
Raspberry Pie (Diet Log, effect screen, diet rules); phase 2 is the Food Dictionary (Spice of Life: Carrot Edition).
Design doc: [raspberry_pie.md](raspberry_pie.md) (sections 5, 6, 6.3, 12.4, 12.5) and
[food_dictionary.md](food_dictionary.md). TODO rows: family Food Tuning, `food-tuning #2` to `#15`, listed per step below.

## 1. Intake

| # | Item (owner's words) | Class | Notes (premise checks) |
|---|---|---|---|
| 1 | food log: move diet ladder and milestones down 1 line length | clear | Pages drawn in `DietLogPages.java` (`ProgressPage`, `ListPage`); title at `frame.y + 4`. |
| 2 | x and y position and size of each page type content configurable | open -> ruled | Client config has one `contentScale` for our pages only (`RpConfig.java:58`). |
| 3 | horizontally extend book texture by copying pixel strip from center and duplicating, configurable | clear | A draw-time stretch of Apple Pie's book background (center column repeated), not a new or edited texture file, so the CLAUDE.md texture rule is not touched. Similar trick: agent note gui-title-plaque-stretch. |
| 4 | change unique food language to 'x more unique food(s) to activate diet' | clear | Two strings: `solraspberrypie.screen.gate_unique` ("Eat %s more different foods to activate diet bonuses") and `solraspberrypie.dietlog.min_unique` ("Need %s more different foods"). |
| 5 | locked 1-time unlocks on diet ladder should alternate between lock icon and effect icon, on diet ladder and milestones | clear | Today an unlock rung shows the `[icons] unlock` item (Monobank lock) on both pages (12.5). |
| 6 | unlocked effects turn to effect icon and remain gray even when diversity is surpassed their level, outline still has color once unlocked | clear | Today a reached unlock is drawn in color with the lock item. |
| 7 | decrease distance between milestones in ladder and show 5 milestones or 4 milestones + last achieved milestone (if achieved) on diet ladder page | open -> ruled | Bar today: proportional to value, window = 35 % lead-in before the last reached + `progressAhead` (3) ahead (`DietLogPages.Window`). |
| 8 | scroll through book pages | clear | Apple Pie's book flips only by its arrow buttons. |
| 9 | shrink 'change' and 'drop' icons on benefit slot | clear | Effect screen (P key) text buttons, 18 px tall (`EffectScreen.java:127-133`). |
| 10 | displayed at 5 diversity but 5 milestone not triggering? is there rounding going on in display or milestone math? | premise -> ruled | Display rounds to one decimal (`DietLogPages.java:297`, `Math.round(d * 10) / 10`), so 4.95-4.99 shows "5"; the server compares the exact value (`DietEngine.java:60`). Cause found, no milestone math bug. |
| 11 | limit effect pool to 30 of the last eaten foods (duplicates count) | open -> ruled | Diversity counts Apple Pie's decay window, `endDecay = 32` meals (`defaultconfigs/solapplepie-server.toml:98`), value fading with age. |
| 12 | respec removes oldest 5 | clear | Server config `respecFoods` default 8 (`RpConfig.java:36`; was 5 before, raspberry_pie.md 5.2). |
| 13 | list how many foods will drop out when respec effect on hover tooltip | clear | Change and Drop costs differ: Change = `respecFoods` (capped at the foods in the window), Drop = foods until the highest active slot is lost (server preview). |
| 14 | do not populate effect slot with currently equiped effect in slot in diet ladder and milestone previews | conflict -> ruled | Reverses G11 `#24` (owner 2026-11-17, raspberry_pie.md 12.5 "Icon rulings"). |
| 15 | history tab is dropped | open -> ruled | Raspberry Pie removes only Apple Pie's benefit pages (`DietLogPages.insert`); the food history pages are still there. |
| 16 | sol carrot: sort food dictionary uneaten order | open -> ruled | Carrot's lists follow `FoodItems.getAllFoods`, sorted by one fixed key; uneaten and eaten split in `FoodData`. |
| 17 | unlocked foods by newest unlock, latest eaten, or same sort order as uneaten order with a single button 3 state toggle | premise | Carrot stores eaten foods as a `HashSet` (`tracking/FoodList`), no order or time: "newest unlock" needs a new stored unlock order. "Latest eaten" can read Apple Pie's memory (meals since last eaten, up to 256 meals). |
| 18 | investigate sophisticated backpacks sort type button | clear | Investigation; result goes into this plan before item 17 is built. |

## 2. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 2 | x and y position and size of each page type content configurable | open | Every page type gets its own x, y and scale in the client config: Apple Pie's diversity page, the last-30-meals list, Diet Ladder, Milestones. | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 7 | decrease distance between milestones ... show 5 milestones or 4 + last achieved | open | Even spacing: each milestone in the window gets an equal step; the fill moves proportionally between neighbors. Window = last achieved + next 4, or the first 5 when none is achieved. The 35 % lead-in and `progressAhead` go. | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 10 | displayed at 5 diversity but 5 milestone not triggering | premise | Display rounds down (4.97 -> 4.9) everywhere diversity is printed by Raspberry Pie; milestone math unchanged. | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 11, 12 | limit effect pool to 30 of the last eaten foods (duplicates count), respec removes oldest 5 | open | Window 30 with the fade kept: `endDecay` 32 -> 30 in the pack's Apple Pie server config; `respecFoods` 8 -> 5. The Raspberry Pie book shows the last 30 foods eaten, not unique (duplicates listed). | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 14 | do not populate effect slot with currently equiped effect ... in diet ladder and milestone previews | conflict (G11 `#24`, 2026-11-17) | Effects show in slots only on the effect screen and the inventory widget, not in the Diet Log. In the Diet Log a slot rung is the metal frame item inside the kind-colored frame; both grayed when the slot is not active. | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 15 | history tab is dropped | open | Remove Apple Pie's food history pages from the Diet Log (replaced by the last-30-meals list, ruling 11). | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 16 | sort food dictionary uneaten order | open | Configurable, four modes: mod then name, name A-Z, easiest first, hardest first. Future: a hand-made type sort, probably mirroring the JEI food family order once that is complete (own TODO row, low). | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 16, 17 | code home for the Food Dictionary changes | open | Raspberry Pie (Carrot mixins load only when Carrot is present). | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 1 | move diet ladder and milestones down 1 line length | clear | One font line (10 px in page units) is the new default y offset of both pages, set through the item 2 config. | 2026-11-18 | agent default, unconfirmed |
| 3 | horizontally extend book texture ... configurable | clear | Client config `[dietLog] extraWidth` (px, default 0 until the look-check); the background is drawn as left half, a 1 px center column repeated `extraWidth` times, right half; pages and buttons move out by half the extra width. | 2026-11-18 | agent default, unconfirmed |
| 4 | x more unique food(s) to activate diet | clear | Both strings become "%s more unique food(s) to activate diet" (singular "food" when 1; separate keys). | 2026-11-18 | agent default, unconfirmed |
| 5, 6 | locked unlocks alternate lock / effect icon; unlocked = effect icon, gray, colored outline | clear | Locked: the lock item and the effect sprite alternate every 1 s (same clock as the key's slot cycle), gray frame. Unlocked: effect sprite grayed, frame in the unlock color, whatever the current diversity. Both pages. | 2026-11-18 | agent default, unconfirmed |
| 8 | scroll through book pages | clear | Mouse wheel flips one page per notch in the Diet Log (and the Food Dictionary in phase 2). | 2026-11-18 | agent default, unconfirmed |
| 9 | shrink change and drop | clear | Buttons keep their size (18 px); only the label text is slightly smaller (about 0.85 scale). History: agent default 2026-11-18 was buttons 18 -> 14 px and label 0.8, replaced by the owner the same day. | 2026-11-18 | chat 2026-11-18 |
| 13 | how many foods will drop out ... hover tooltip | clear | Hover on Change: "N foods age out" (respecFoods capped at the window). Hover on Drop: only the number of foods that will age out, no preview content (no slots lost or shifted); the count is computed with the drop rule (5.2) and synced with the state, not fetched by a preview request. Clicking Drop still opens the confirm panel (5.4), unchanged. History: agent default 2026-11-18 showed the count from the server's drop preview; replaced by the owner. | 2026-11-19 | chat 2026-11-19 |
| 17 | single button 3 state toggle | premise | Unlock order is stored from the first eat after the update; older unlocks have no order and sort after the ordered ones, in the uneaten order. "Latest eaten" uses Apple Pie's meal memory; foods past it sort last, in the uneaten order. The toggle state is a client config value. | 2026-11-18 | agent default, unconfirmed |

## 3. Phase 1: Raspberry Pie

Order: rules first (server, testable headless), then the Diet Log layout, then icons and screens. One code owner.

| Step | Items | Change | TODO |
|---|---|---|---|
| 1.1 | 11, 12 | `endDecay` 30 in `defaultconfigs/solapplepie-server.toml` (+ `configs_edited/`); local worlds' `serverconfig/` copies replaced as in 12.4; `respecFoods` default 5. Ageing (6.1) uses `endDecay`, so the respec math follows. Writeup 5.2, 5.3 example table redone for 30. | `food-tuning #2` |
| 1.2 | 11, 15 | Meal log: Raspberry Pie stores the last 30 foods eaten in order (NBT key in Apple Pie's capability, like `solrp_gateFoods`; synced). Apple Pie's food history pages are removed; a "Last 30 meals" list page (duplicates shown) takes their place. Length follows `endDecay`. | `food-tuning #3` |
| 1.3 | 10 | Diversity printed rounded down (one decimal) on the Diet Ladder page, effect screen and P-screen previews. | `food-tuning #4` |
| 1.4 | 1, 2 | Client config `[dietLog.pages]`: per page type (diversity, meals, ladder, milestones) `x`, `y`, `scale`; replaces `contentScale`. Ladder and milestones default `y` = one line. Apple Pie's diversity page gets the transform by mixin. | `food-tuning #5` |
| 1.5 | 3 | Book background stretch, `[dietLog] extraWidth`; mouse mapping and the fit rule (`bookScale`) include the extra width. | `food-tuning #6` |
| 1.6 | 8 | Mouse wheel page flip (`mouseScrolled` on the Diet Log). | `food-tuning #7` |
| 1.7 | 7 | Even-spaced ladder bar, window of 5 (last achieved + 4, or first 5); `progressAhead` removed. | `food-tuning #8` |
| 1.8 | 5, 6, 14 | Icons: locked unlock alternates lock / effect; unlocked = grayed effect sprite, colored frame; slot rungs = metal frame item in the colored frame, grayed when inactive, hover "Effect Slot N" (no effect named). Key unchanged except the slot entry stops cycling effects. | `food-tuning #9` |
| 1.9 | 4 | Gate strings. | `food-tuning #10` |
| 1.10 | 9, 13 | Effect screen: smaller Change / Drop label text (buttons unchanged); hover tooltips with the aged-out food count. | `food-tuning #11` |

Look-check (intake step 5): after 1.4, 1.5, 1.7 and 1.8, a client capture of the Diet Ladder, Milestones and meals
pages goes to the owner before test rows are written.

## 4. Phase 2: Food Dictionary (Carrot)

| Step | Items | Change | TODO |
|---|---|---|---|
| 2.1 | 18 | Investigate Sophisticated Backpacks' sort-type button (icon per state, click cycles, tooltip names the mode); record here how far the toggle copies it (own code, no SB dependency). | `food-tuning #12` |
| 2.2 | 16 | Client config `[foodDictionary] uneatenSort` = `mod_name` (default), `name`, `easiest`, `hardest` (complexity from Apple Pie's config, then nutrition). Mixin on Carrot's `FoodData` lists. | `food-tuning #13` |
| 2.3 | 17 | Stored unlock order (Raspberry Pie, keyed to Carrot unlocks); 3-state button on the eaten pages: newest unlock / latest eaten / uneaten order; state in client config. Mouse wheel flip as 1.6. | `food-tuning #14` |
| later | 16 | Hand-made type sort following the JEI food family order, after that work is done. | `food-tuning #15` (low) |

## 5. Tests

Queue rows are written after the look-check (prefix `RPI-` for phase 1, `FDI-` for phase 2; queue `CQ-RPI`,
`SQ-RPI`, `CQ-FDI`). Headless: the 30-meal window and respec 5 math in the existing harness.

## 6. Waiting on the owner

- Unconfirmed agent defaults in section 2 (items 1, 3, 4, 5/6, 8, 17).
- `extraWidth` value: picked at the look-check.
