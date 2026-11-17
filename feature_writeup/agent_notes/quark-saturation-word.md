# quark-saturation-word

> Quark's food tooltip word = 6 saturation buckets (sat x nutrition x 2) + bad set on a HARMFUL effect; no food value gives a unique word, so patcherMod food.IntoxicatingDrinkTooltip swaps it for "Intoxicating Drink" on Intoxication foods

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-20: Quark 3.4 `FoodTooltips.makeTooltip` (RenderTooltipEvent.GatherComponents) adds `Either.left(translatable
"quark.misc.[bad_]saturation<N>")`: s = min(20, satMod*nutrition*2); s>=19 -> 5 Full-Course, 10..19 -> 0 Fulfilling,
8 -> 1 Hearty, 6 -> 2 Meal, 2 -> 3 Light Meal, <2 -> 4 Light Snack; `bad_` when any food effect is HARMFUL
("Disgusting Snack" for 4). Half-shank sat-0 alcohol shares bucket 4 with rotten flesh / raw chicken, so neither a
saturation tweak nor a lang override can single it out. Fix: `food/IntoxicatingDrinkTooltip` (client, LOWEST
priority) replaces that element on any food whose effects hold `brewinandchewin:intoxication`; CEConfig
`[alcohol] quarkLine`; lang `tooltip.creativeengineering.intoxicating_drink`. The KubeJS effect-line rename
(`client_scripts/alcohol_tooltip.js`) was deleted the same day so the two lines don't repeat (AL-88 rewritten).

Related: [alcohol-drink-food-values](alcohol-drink-food-values.md), [feature-ports-2026-10-20](feature-ports-2026-10-20.md).
