# Plan: Diet Log round 2, food tooltips, permanent effects column

<!-- generated:status_index begin open_work sha1=c75510cbcb9d -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 13 open TODO rows (Planned 13), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `food-tuning #20` | Planned | S C | Diet window back to 32 meals (endDecay 30 -> 32); meals grid 8 x 4 |
| `food-tuning #21` | Planned | C | Diet Log meals grid: label centered; hover shows food total only when it differs, before the struck-through meal value |
| `food-tuning #22` | Planned | C | Diet Log 50% wider by default; first-page diversity and gate text scaled to fit on one line each |
| `food-tuning #23` | Planned | C | Diet Log freezes briefly on every open: measure and cache |
| `food-tuning #24` | Planned | C | Liquid Diet Log pauses the game in single player |
| `food-tuning #25` | Planned | S P | Effect unlocks (and their messages) wait until the unique-food gate first opens |
| `food-tuning #26` | Planned | C | Diet Log diversity icon: eaten non-drink food closest to the window's average meal value (newest on a tie); else seeds, moss, rotten flesh, spider eye or dandelion leaf |
| `food-tuning #27` | Planned | C | Food Dictionary sorts: highest food, highest saturation, highest diversity (was hardest), custom (hidden until configured) on both lists |
| `food-tuning #28` | Planned | C | Food tooltip line Diversity: X (contributes Y); diversity printed with two decimals, rounded down, everywhere |
| `food-tuning #29` | Planned |  | Merge the Raspberry Pie changelog bullets |
| `interface #24` | Planned | C | Inventory effect list: permanent effects (armor, charm, beacon, diet) in a second column right of the timed ones |
| `tooltips #19` | Planned | C | Food tooltip order (Raspberry Pie pass): AppleSkin hunger and saturation at the top (option: saturation on its own line), Quark shank row removed (config first), diversity line before Quark's saturation word |
| `tooltips #20` | Planned | C | Chopsticks tooltip: double mod name label |
<!-- generated:status_index end open_work -->

Source: owner notes, `todo/scratchpad.md` block "rasp pie" and the four bullets after it (intake 2026-11-19). Follows
[plan_diet_books.md](plan_diet_books.md) (round 1, built 2026-11-19; its look-check, done by the owner 2026-11-19, produced these notes). Design docs:
[raspberry_pie.md](raspberry_pie.md), [food_dictionary.md](food_dictionary.md). TODO rows: listed per step (section 3).

## 1. Intake

| # | Item (owner's words) | Class | Notes (premise checks) |
|---|---|---|---|
| 1 | merge raspberry pie changelog lines | clear | `unreleased.md` holds about 20 Raspberry Pie bullets (lines 71-97), several rewriting each other (Diet Log width, meals list). |
| 2 | expand food log horizontally by 50% | clear | "Food log" = the Diet Log book. Width is `[dietLog] extraWidth` (`RpConfig.java:75`, default 0, round 1 ruling 3). |
| 3 | shrink text size of 'current diversity' and 'need x foods' text on first page, keep both on their own line w/ no overflow | clear | Apple Pie's diversity page, transformed by `DiversityPageMixin`. |
| 4 | diet log freezes for short time on each open, carrot dict does on first open but not after. caching needed? other issue? | premise | Suspect: `DietLogPages.band` (`DietLogPages.java:674`) scans every registered item and computes its complexity on each open for the diversity icon; the meals grid also copies the food memory. Carrot's first-open freeze is its own food list build, cached after. To be measured before fixing. |
| 5 | liquid diet log should also pause game in sp | clear | `LiquidDietLogScreen.isPauseScreen` returns false (`LiquidDietLogScreen.java:54`). |
| 6 | food total shouldnt show up on 30 meal hover if its the same as this meal | clear | Hover always lists "This meal" and "Food total" (`DietLogPages.java:567-574`). |
| 7 | hover order is food total (only present if different from hovered food meal value), this meal (crossed out if food total is present) | clear | Today: this meal first, then food total. |
| 8 | show food diversity tag on food items between shanks and meal description (hearty meal, snack) (also a top-level bullet: "move food diversity tag ...") | open -> ruled | No diversity line exists on food tooltips (Apple Pie only has "does not contribute"). "Hearty Meal" / "Light Snack" is Quark's saturation word (agent note quark-saturation-word). |
| 9 | queue unlock messages fired when food eaten threshold is not passed | open -> ruled | Unlocks follow `maxDiversity`, which climbs before the `minFoodsToActivate` gate (5) is passed (`DietEngine.java:109-133`), so unlocks and their messages happen before the diet is active. |
| 10 | diversity value food icon ONLY pulls from carrot food dict history, otherwise a pufferfish | open -> ruled | Today: a Carrot-eaten food in a complexity band of all foods, else the `[dietLog] diversityIcons` pool (`DietLogPages.java:631`, `RpConfig.java:77`). |
| 11 | pick food closest in value to current diversity scaled to food values (many low value foods -> still a low value food; mid meals -> mid) | open -> ruled | See ruling 10. |
| 12 | never pick drinks | clear | `logic/Drinks.isDrink` (tag `solraspberrypie:drinks`). |
| 13 | last 30 meals label should be centered | clear | Drawn at the grid's left edge (`DietLogPages.java:589`). |
| 14 | change last meals to 8 columns by 4 rows, 32 last meals count | conflict -> ruled | Window 30 was ruled 2026-11-18 (plan_diet_books.md ruling 11, 12); grid `MEAL_COLS = 6`, `MEAL_ROWS = 5`. |
| 15 | include pancakes (treat as single pancake) and all other in world eaten foods in diet calcs | premise -> skipped | Placed pies, cakes, candle cakes and Supplementaries cakes already count (Creative Delight `PlacedFoodSol`). Rustic Delight's pancake stack hands out a pancake item (`PancakeStackBlock.java:93`), counted when eaten. |
| 16 | item tags in game show diversity value and contribution when present | clear (with 8) | Same line as item 8. |
| 17 | sort carrot options for highest food, saturation, diversity | open -> ruled | Uneaten config: mod_name, name, easiest, hardest; eaten button: newest unlock, latest eaten, uneaten order (`FoodDictionarySort.java:44-74`). "Hardest" = highest complexity = highest diversity. |
| 18 | applied to uneaten, old 'same as uneaten' renamed to custom and applied to uneaten too, not shown if not configured (mark note to configure) | open -> ruled | The hand-made order is `food-tuning #15` (Planned). |
| 19 | move permanent effects (from armor, charm, beacon, etc) to the right of the timed effects in a new column. dont push over if only permanent effects are applied | open -> ruled | Raspberry Pie already filters the inventory list (`EffectRenderingInventoryScreenMixin`). Armor and tool effects are 40-tick reapplied instances (agent note armor-tool-effects); beacons are ambient. |
| 20 | appleskin: a hunger shank shows below food name, hunger shank and saturation shown at bottom of item | premise | Both Quark `Food Tooltips` (`config/quark-common.toml:1551`, shanks and the saturation word under the name) and AppleSkin (`showFoodValuesInTooltip`, shanks and saturation at the bottom) are on. |
| 21 | move hunger+saturation to top, remove duplicate | clear | AppleSkin's row moves to the top; Quark's shank row goes, its saturation word stays (item 8 places the diversity line before it). |
| 22 | double mod label on item when eaten w/ chopsticks tooltip is shown? | premise | Roll's `RollClient.onTooltip` appends the chopsticks line at the end of the tooltip (`RollClient.java:28`), after the mod name line; the second mod label is not explained by the code. Investigate in game. |

## 2. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 14 | 8 columns by 4 rows, 32 last meals count | conflict (plan_diet_books.md ruling 11, 12, 2026-11-18) | Diet window back to 32: `endDecay` 30 -> 32; the 8 x 4 grid shows the whole window, label "Last 32 Meals". `respecFoods` stays 5. The grid is built after the widening (A3) and keeps 16 px cells; its page scale config shrinks it if it still overflows at the look-check. | 2026-11-19 | AskUserQuestion 2026-11-19, 2026-11-20 (grid fit) |
| 9 | queue unlock messages fired when food eaten threshold is not passed | open | The unlocks wait too: no unlock counts until the gate is passed; unlocks and their messages happen when it opens. Only the first opening holds them back: once the gate has opened, highest diversity keeps climbing even if the gate later closes again (Apple Pie counter reset). | 2026-11-19 | AskUserQuestion 2026-11-19, 2026-11-20 (gate reset) |
| 8, 16 | food diversity tag ... diversity value and contribution | open | A new Raspberry Pie tooltip line on every countable food: "Diversity: 1.40", plus "(contributes 0.85)" while the food is in the diet window (no colon after "contributes"). Placed between the shanks and Quark's saturation word. History: 2026-11-19 text was "Diversity 1.40"; colon added 2026-11-20. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| 4 (round 1 ruling 10) | decimals | conflict (plan_diet_books.md ruling 10, one decimal rounded down) | Two decimals everywhere Raspberry Pie prints diversity (Diet Log, effect screen, previews, meals hover, tooltip line), still rounded down (4.999 -> 4.99); label "Diversity:" with a colon. Supersedes round 1's one decimal. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| 19 | permanent effects ... new column | open | The inventory effect list only (not the HUD icons): timed effects in the first column, permanent ones in a second column to its right; with only permanent effects they stay in the first column. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 19 | what counts as permanent | open | Beacon and conduit (vanilla ambient flag), effects held by Creative Equipment's armor, tool and charm specs, Raspberry Pie flat buffs and slot effects. Long-duration effects from other sources stay timed. The server marks the gear and diet effects for the client (small sync); ambient is read from the instance. History: agent default 2026-11-19, confirmed 2026-11-20. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| 10, 11 | diversity icon ... closest in value ... otherwise a pufferfish | open | The average base diversity value of the meals in the diet window (repeats count); the icon is the eaten (Food Dictionary) non-drink food whose base value is closest; on a tie the most recently eaten. Nothing eaten: one of the owner's list (replaces the pufferfish and the `diversityIcons` pool). | 2026-11-20 | AskUserQuestion 2026-11-19, 2026-11-20 |
| 10 | nothing-eaten list | open | "seeds, moss, rotten flesh, spider eye, dandelion leaf only". Read as: `minecraft:pumpkin_seeds`, `vintagedelight:mixed_seeds`, `vintagedelight:oat_seeds`, `corn_delight:corn_seeds`, `hearthandharvest:sunflower_seeds`, `miners_delight:moss`, `immersive_weathering:moss_clump`, `minecraft:rotten_flesh`, `minecraft:spider_eye`, `veggiesdelight:dandelion_leaf` (every edible seed, plain moss only; golden, canned and stew variants left out, owner 2026-11-20). The candidate CSV is kept only as the record of the 0.15 search. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| 17, 18 | sort carrot options ... custom | open | Eaten button cycles newest unlock, latest eaten, highest food, highest saturation, highest diversity, custom. The uneaten config gets the same three plus custom. "Hardest" becomes "highest diversity" (one mode); "easiest" stays. Highest saturation = saturation points restored (hunger x modifier x 2). Custom = the hand-made order (`food-tuning #15`), hidden until it is configured; a TODO note says to configure it. | 2026-11-20 | AskUserQuestion 2026-11-19, 2026-11-20 (saturation) |
| 15 | include pancakes ... in world eaten foods | premise | Skipped. | 2026-11-19 | AskUserQuestion 2026-11-19 |
| 2 | expand food log horizontally by 50% | clear | `extraWidth` default = half of Apple Pie's book background width. | 2026-11-20 | AskUserQuestion 2026-11-20 (accepted) |
| 3 | shrink 'current diversity' / 'need x foods' | clear | Both lines drawn at the largest scale (not above 0.85) that fits the page width, each on its own line. | 2026-11-20 | AskUserQuestion 2026-11-20 (accepted) |
| 6, 7 | meal hover | clear | Hover: "N. Name"; then "Food total: X" only when it differs from this meal, then "This meal: Y" (struck through when the total line is there). | 2026-11-20 | AskUserQuestion 2026-11-20 (accepted) |
| 20, 21 | AppleSkin / Quark duplicate | clear | Tooltip order: name, AppleSkin row (shanks and saturation), diversity line, Quark's saturation word, the rest. A client config option puts AppleSkin's saturation on its own line under the shanks. Raspberry Pie owns the whole ordering pass (works only with Raspberry Pie installed). Quark's shank row: config first (`Show Saturation` and siblings in `quark-common.toml`, backed up in `configs_edited/`) if one keeps the word and drops the row; code removal only if not. History: 2026-11-19 agent default had Creative Delight own the pass. | 2026-11-20 | AskUserQuestion 2026-11-20 |
| 4 | diet log freezes on each open | premise | Ship the likely fix (A7 removes the all-items scan) with a timing line in `latest.log` per open; the owner opens the book a few times and the log decides any further caching. | 2026-11-20 | AskUserQuestion 2026-11-20 |

## 3. Steps

Phase A is Raspberry Pie (one code owner, one jar build), phase B is the effect-list and chopsticks work in other jars.
Order inside A: A3 before A1 (the grid needs the width); A7 before A4 (measure after the scan is gone); A10 last.

| Step | Items | Change | TODO |
|---|---|---|---|
| A1 | 14 | `endDecay` 32 in `defaultconfigs/solapplepie-server.toml` (+ `configs_edited/`, local worlds' `serverconfig/` as in raspberry_pie.md 12.4); meal log length follows. Grid `MEAL_COLS` 8, `MEAL_ROWS` 4. | `food-tuning #20` |
| A2 | 6, 7, 13 | Meals grid: label centered over the grid; hover order and the conditional total line. | `food-tuning #21` |
| A3 | 2, 3 | `extraWidth` default 50 % of the book; first-page text scaled to fit, one line each. | `food-tuning #22` |
| A4 | 4 | Debug timing line per Diet Log open (icon, meals grid, pages); caching only for what the owner's log shows slow. | `food-tuning #23` |
| A5 | 5 | Liquid Diet Log pauses the game in single player. | `food-tuning #24` |
| A6 | 9 | Unlocks wait for the first gate opening: `maxDiversity` does not climb until the gate has opened once (new saved flag), then climbs as today. Old saves keep what they have. | `food-tuning #25` |
| A7 | 10, 11, 12 | Diversity icon: average base value of the window's meals -> closest eaten non-drink food, newest on a tie; else the owner's list (config `diversityIcons` becomes that flat list). `band` and its all-items scan go. | `food-tuning #26` |
| A8 | 17, 18 | Food Dictionary sorts: highest food (nutrition), highest saturation (points), highest diversity (was hardest), custom (hidden until configured); an icon item per button state. A client config still on `hardest` resets to the default. | `food-tuning #27`; note on `food-tuning #15` |
| A9 | 8, 16, round 1 #10 | Tooltip line "Diversity: X (contributes Y)"; two-decimal round-down everywhere Raspberry Pie prints diversity. | `food-tuning #28` |
| A11 | 20, 21 | Tooltip ordering pass in Raspberry Pie (client, LOWEST priority): AppleSkin row to the top, the diversity line, Quark's word; saturation-own-line option. Quark config checked first. | `tooltips #19` |
| A10 | 1 | Merge the Raspberry Pie changelog bullets into the final state. "Food diversity counts your last 30 meals [32]" is deleted, not merged (30 never shipped, 32 is the released value). | `food-tuning #29` |
| B2 | 19 | Inventory effect list: permanent effects in a second column (Creative Common client hook; Creative Equipment and Raspberry Pie mark their effects through a small server -> client sync). One mixin owner for `renderEffects`: Raspberry Pie's existing filter moves into Common's hook or stays a redirect checked for order. Narrow screens: vanilla's compact mode keeps one column (checked at the look-check). | `interface #24` |
| B3 | 22 | Investigate the double mod label with the chopsticks line; fix in the Roll member. | `tooltips #20` |

Every step, same session: changelog bullet in `unreleased.md` under the Raspberry Pie family (A) or a broad heading (B),
writeup section (raspberry_pie.md 5.3, 6.3, 9.5, 12; food_dictionary.md; tooltips.md; effects.md; roll_delight.md),
queue rows (RPI-, FDI-, TT-, EF- prefixes from test_prefix_index.md) after the look-check.

Look-check (intake step 5): after A1 to A3, A7, A9, A11 and B2, a client capture of the Diet Log first page, a food
tooltip and the inventory effect list goes to the owner before queue rows are written.

## 4. Waiting on the owner

- Open the Diet Log a few times after the A4 build so the timing lines land in `latest.log`.
- Not waiting: the custom Food Dictionary order stays hidden until `food-tuning #15` (the JEI food family order) is
  complete; nothing to configure before then (owner, 2026-11-20).
- Queue rows: round 1's draft rows (plan_diet_books.md section 6) are refreshed together with this round's rows,
  after this round's look-check.
