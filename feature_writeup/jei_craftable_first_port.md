# JEI "craftable recipes first" - port map

Source: DecompiledMods/jei-1.21.1-neoforge-19.39.0.371 (class files only; no Java source).
`javap -p -c` dumps of every relevant class are in `_PORT_craftable_first/` inside that folder.
Target: JEI 1.19.2-forge-11.8.1.1034 (patcherMod client mixin).

## Verdict

The feature is present in 19.39.0.371. It is not in 11.8.1.1034, where `IRecipeLayoutList.create(menu, player, list)`
ignores both arguments and always returns `UnsortedRecipeLayoutList`.

## How 19.39 does it

Craftable means the transfer button is enabled with nothing missing: `getMissingCountHint() == 0`.

1. `RecipeGuiLogic.getVisibleRecipeLayoutsWithButtons(int,int,menu,bookmarkList,recipesGui)` rebuilds its cached list when
   any of these change: the enabled sorter stages, the selected category, or `menu.containerId`
   (fields `cachedSorterStages`, `cachedRecipeCategory`, `cachedContainerId`). It calls
   `IRecipeLayoutList.create(stages, menu, focusedRecipes, focusGroup, bookmarkList, recipeManager, recipesGui)`. [PORT: hook point A]
2. `IRecipeLayoutList.create` returns `LazyRecipeLayoutList`. [PORT: replaces the Unsorted list]
3. `LazyRecipeLayoutList` ctor: `matchingCraftable = stages.contains(CRAFTABLE) && menu != null &&
   transferManager.getRecipeTransferHandler(menu, category).isPresent()`. This restricts the sort to crafting screens. [PORT: gate]
4. `LazyRecipeLayoutList.calculateNextResult()` builds one layout per call (lazy; also driven by `tick()` and `ensureResults(n)`):
   - `getMissingCountHint() == 0`: appended to `results` immediately.
   - otherwise: parked in `craftMissing`.
   - when the source iterator is exhausted: `craftMissing.sort(RecipeSortUtil.getComparator())` then `results.addAll(craftMissing)`.
   `subList(from,to)` calls `ensureResults(to)`. [PORT: core algorithm]
5. `RecipeSortUtil.createComparator()` orders the not-craftable group by fraction of inputs missing
   (`getMissingCountHint()` over the count of non-empty INPUT slots in `getRecipeSlotsView()`). A hint of -1 sorts as 0. [PORT: optional]
6. `RecipeTransferButtonController.updateState` computes the error via `RecipeTransferUtil.getTransferRecipeError(...)` and
   `getMissingCountHint(err)` is `0` for a null error, otherwise `err.getMissingCountHint()`. `initState` calls `updateState`,
   so the hint exists as soon as the layout is built.
7. `IRecipeTransferError.getMissingCountHint()` is a new default method returning -1. `RecipeLayoutWithButtonsErrored` returns
   `Integer.MAX_VALUE`. [PORT: no equivalent in 11.8.1]
8. UI and config: `RecipeSorterStage {BOOKMARKED, CRAFTABLE}`, config key `recipeSorterStages` (client config),
   toggle button in `RecipeOptionButtons` via `RecipeSortStateButtonController`, lang keys
   `jei.tooltip.recipe.sort.craftable.first.{enabled,disabled}`. [PORT: optional; a plain config toggle is enough]
9. `RecipeSortUtil.sortRecipeCategories` (sorts categories that have a transfer handler for the open menu first) is unused
   by anything in the jar listing I searched. Treat as dead code.

Behaviour note: this is computed when the list is built, not continuously. The order is refreshed when the category,
container id or stage set changes, which includes reopening the recipe screen. It does not re-sort on every inventory
change while the screen stays open. If live re-sorting is wanted, it is an addition beyond 19.39.

## Mapping to 11.8.1.1034

| 19.39 | 11.8.1 equivalent |
|---|---|
| `IRecipeLayoutList.create(stages, menu, focused, ...)` returning Lazy list | `IRecipeLayoutList.create(menu, player, List<RecipeLayoutWithButtons>)` returning Unsorted; call site is `RecipeGuiLogic.createRecipeLayoutsWithButtons`, bytecode offset 109 |
| `IRecipeLayoutWithButtons` (interface) | `RecipeLayoutWithButtons` (final record: `recipeLayout`, `transferButton`) |
| `RecipeTransferButtonController` | `RecipeTransferButton`; error held in private field `recipeTransferError`, set in `update(menu, player)` |
| `getMissingCountHint() == 0` | `recipeTransferError == null` (or `getType().allowsTransfer`, which is what the old button uses to enable itself) |
| lazy build from `IFocusedRecipes.getRecipes()` | 11.8.1 already builds every layout eagerly into a list (`mapMulti` + `toList`), so sort that list; no lazy iterator needed |
| `tick()` builds the next layout | 11.8.1 `UnsortedRecipeLayoutList.tick()` is where layouts tick; the old button's `update` runs from `RecipeLayoutWithButtons.tick(menu, player)` |
| cache invalidation on stages/category/containerId | 11.8.1 `RecipeGuiLogic` caches `cachedRecipeLayoutsWithButtons` and `cachedRecipeCategory`; add the container id check or the list will be stale across menus |

## Implementation (built 2026-10-05; in the patcherMod 1.0.5 jar, STAGED for pack 1.0.6 2026-10-11, not deployed; the jar booted in single player without mixin errors; JEI-01..06 not run)

- `compat/jei/CraftableFirstRecipeLayoutList.java`: the sorted list. Applies only when the menu has a
  transfer handler for the category. Group 0 = + button enabled (`error == null || getType().allowsTransfer`,
  the old button's own test, so COSMETIC errors count as craftable; JEI 19 uses missing count == 0).
  Group 1 = the rest, by JEI 19's key `-(100 * present / inputs)`; unknown missing count = 0. Stable sort.
  A transfer check that throws is logged once and sorts last. A sort slower than 250 ms is logged with
  the count and category uid.
- `mixin/JeiRecipeGuiLogicMixin.java`: `@Redirect` on `IRecipeLayoutList.create` in
  `createRecipeLayoutsWithButtons`, plus a HEAD inject on `getVisibleRecipeLayoutsWithButtons` that drops the
  cached list when the menu (by identity) or the config value changes (JEI 19 uses containerId + stages).
- `mixin/JeiMissingSlotsErrorAccessor.java`: `@Accessor("slots")` on `RecipeTransferErrorMissingSlots`,
  standing in for JEI 19's `getMissingCountHint()`.
- Config: `creativeengineering-common.toml` `[jei] craftableFirst = true`.
- Not ported: the toggle button / `RecipeSorterStage` enum / BOOKMARKED stage, lazy building.

## Test checklist (JEI-01..)

- JEI-01: crafting table, hold some planks, U on planks: recipes you can make now are on page 1, rest after.
- JEI-02: same lookup from the 2x2 inventory: only 2x2-fitting recipes have an enabled +, and they lead.
- JEI-03: open JEI from a non-crafting screen (chest, or no screen): order is unchanged from stock JEI.
- JEI-04: any modded GUI that shows a JEI + button sorts its category the same way.
- JEI-05: set `craftableFirst = false` in Create's config screen, reopen a lookup: stock order.
- JEI-06: U on a common item (cobblestone, stick) with many uses: no visible hitch; check latest.log for
  "craftable-first sort ... took".

## Not verified

Class files only were inspected. Behaviour above is read from bytecode, not run. 19.39 targets MC 1.21.1 / NeoForge, so
every Mojang/NeoForge name differs from the 1.19.2 mappings the mixin will use.

## History

- 2026-10-05: port built into patcherMod 1.0.5 (`JeiRecipeGuiLogicMixin`, `JeiMissingSlotsErrorAccessor`,
  `CraftableFirstRecipeLayoutList`, `[jei] craftableFirst`).
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed). The shipped config has `craftableFirst = true`.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
