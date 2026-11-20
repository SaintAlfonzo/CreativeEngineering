# food-tooltip-rows

> How AppleSkin 2.4.2 and Quark 3.4-418 add food rows to item tooltips, and how Raspberry Pie reorders them (2026-11-20)

Decompiled 2026-11-20 for plan_diet_books_r2.md A11. Status lives in TODO `tooltips #19`.

- **AppleSkin** `squeek.appleskin.client.TooltipOverlayHandler.gatherTooltips(RenderTooltipEvent.GatherComponents)`,
  NORMAL priority: appends one `Either.right(FoodTooltip)` (package-private nested class, no placeholder text line).
  Identify it by class name `squeek.appleskin.client.TooltipOverlayHandler$FoodTooltip`; it can be moved anywhere in
  the list. Its renderer `$FoodTooltipRenderer` is one component of height 20: shanks at `y`, `iinc offsetY 10`, then
  saturation (first `getfield saturationBars` in `m_183452_` comes after the row step). No API for a one-row layout:
  `TooltipOverlayEvent.Pre`/`Render` only cancel the whole thing.
- **Quark** `ImprovedTooltipsModule.makeTooltip(GatherComponents)`, NORMAL: inserts at index 1 the shank row
  (`FoodTooltips$FoodComponent`, only when "Food Tooltips" is on) and at index 2 the word
  `Component.translatable("quark.misc.saturation<N>" | "quark.misc.bad_saturation<N>")` (only when "Show Saturation"
  is on). "Food Tooltips" = false + "Show Saturation" = true keeps the word without the shanks.
- **Raspberry Pie** `client/FoodTooltips.gather` at LOWEST: removes the AppleSkin element and the Quark word (key
  prefix), reinserts after the name: AppleSkin, the diversity line, the word. `AppleSkinTooltipRendererMixin` (gated
  on AppleSkin `[2.4.2+mc1.19]`, SRG names, remap off): height 10, width shanks + 4 + saturation (redirect of
  `Math.max` in `m_142069_`, which also records the shank width), pose translated (+shank width + 4, -10) at the first
  `saturationBars` read, popped at return.
- **Mod Name Tooltip** (`modnametooltip`, ItemTooltipEvent at LOW) adds the mod name unless the last line already is
  it; anything appended after it on a second pass over the same list makes it add the name again (Roll's chopstick
  line, fixed by adding that line once, `tooltips #20`).
