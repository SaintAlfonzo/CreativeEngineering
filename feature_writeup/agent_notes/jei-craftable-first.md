# jei-craftable-first

> JEI 19 \"craftable recipes first\" backported to JEI 11.8.1 via Creative Common (CraftableFirstRecipeLayoutList + 2 mixins, CEConfig [jei]); status in feature_writeup/jei_craftable_first_port.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-05 into Creative Common 1.0.5 jar (no version bump).
Port notes + JEI-01..06 checklist: feature_writeup/jei_craftable_first_port.md. Reference JEI 1.21.1 19.39.0.371
is extracted (class files only) in DecompiledMods/, javap dumps in its `_PORT_craftable_first/`.

- Hook: 11.8.1 `RecipeGuiLogic.createRecipeLayoutsWithButtons` calls `IRecipeLayoutList.create(menu, player, list)`
  which ignored menu/player; `JeiRecipeGuiLogicMixin` redirects it. HEAD inject on `getVisibleRecipeLayoutsWithButtons`
  drops the cache on menu/config change (11.8.1 only reset it in `setState` and on category change).
- Craftable = + button enabled (error null or `allowsTransfer`); rest ranked by % inputs present via
  `JeiMissingSlotsErrorAccessor` (`RecipeTransferErrorMissingSlots.slots`).
- Not live: order fixed per list build, same as JEI 19.

**Why:** user remembered this from newer JEI and wanted it on the pinned 1.19.2 JEI.
**How to apply:** if the pack's JEI is updated past 11.8.1, check whether the native feature exists and drop these mixins. See [creative-common-coremod](creative-common-coremod.md).
