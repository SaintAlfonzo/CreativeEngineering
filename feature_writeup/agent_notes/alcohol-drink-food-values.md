# alcohol-drink-food-values

> global.alcohol (startup cooking/alcohol_drinks.js) gives all 44 alcoholic drinks hunger 1 / sat 0 / alwaysEat / hidden Intoxication / Nausea; \"Intoxicating Drink\" tooltip; alcohol.md 11

Moved from agent memory 2026-11-16 (last edited there 2026-10-20). Repo status docs win over any status line here.

2026-10-20 (alcohol.md 11, AL-85..90, BUILT): `global.alcohol` in `kubejs/startup_scripts/cooking/alcohol_drinks.js` lists every alcoholic drink (16 Brewin, 26 Vinery, Bellini, Bottle of Wodka) by item_dict key. ItemEvents.modification rebuilds their food with vanilla `FoodProperties$Builder` (KubeJS FoodBuilder can't make hidden effects). FD/FR apple ciders deliberately excluded (not alcoholic, user's list said "alcohol").

- Nausea 5 s / 10 s for `mixed` (Jellie, Mojang Noir); skipped when the food has Tipsy or the key is in `ownDrunkEffect` (wodka: Nausea 60 s in its own code).
- Tooltip rename lives in `client_scripts/alcohol_tooltip.js` (matches the line text starting with the effect name). I read "rename appleskin tooltip" as the Intoxication effect line; keg/JEI tank tooltips still say Intoxication.
- Vinery port `DrinkBlockItem.finishUsingItem` now calls `eat()` (before it only shrank, so food values never applied) and lists food effects in its tooltip. Rebuilt as `-03` (see deploy.md for what shipped).
- Rhino check: `Builder.effect(MobEffectInstance, float)` vs `effect(Supplier, float)` and `setFoodProperties(FoodProperties)` vs `(Consumer)` resolve correctly with Java objects (mock test in the pack's rhino jar).
- Gotcha: Fruits Delight `FDFoodItem.getFoodProperties(stack, entity)` uses its own values only for items with fruit NBT, else the base.

Related: [backported-brewin](backported-brewin.md), [backported-vinery](backported-vinery.md), memory `rhino-fieldandmethods-gotcha`.
