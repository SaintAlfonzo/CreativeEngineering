# legendary-tooltips-separator-fix

> Legendary Tooltips name underline drawn through the 3D model on armor/tools; IcebergTitleBreakMixin fix; status in feature_writeup/tooltips.md (TT-)

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Legendary Tooltips 1.4.0 (`render_item_model = "EQUIPMENT"`) draws the name underline 9px too high on model tooltips when its LOWEST GatherComponents handler runs before Iceberg 1.1.4's (also LOWEST). The order is listener registration order. Iceberg's TitleBreakComponent then lands before LT's " " spacer line and `TooltipDecor`'s `titleLines--` leaves 0 title lines. EclecticTrove resource pack only supplies `tooltip_borders.png`.

Fix (2026-10-05): Creative Common client mixin `IcebergTitleBreakMixin` (TAIL of `IcebergClient.onGatherComponentsEventEnd`) moves the break from index 2 to 3 when index 0 is `ItemModelComponent`. Iceberg + LegendaryTooltips jars added to creativeCommon/libs. Built into creativeengineering-1.0.5.jar; NOT copied to mods/.

**Why:** the user reported the underline crossing the spinning model.
**How to apply:** after installing the jar, check armor/tool tooltips across a few launches. Client-only, see [patchermod-coremod](patchermod-coremod.md).
