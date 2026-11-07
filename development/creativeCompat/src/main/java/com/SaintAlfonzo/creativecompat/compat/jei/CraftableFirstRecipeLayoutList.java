package com.SaintAlfonzo.creativecompat.compat.jei;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecompat.mixin.JeiMissingSlotsErrorAccessor;
import com.mojang.logging.LogUtils;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Internal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.layouts.IRecipeLayoutList;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Backport of JEI 19's "Show craftable recipes first" ({@code LazyRecipeLayoutList} +
 * {@code RecipeSortUtil}, JEI 1.21.1 19.39.0.371) to JEI 11.8.1. Port notes:
 * feature_writeup/jei_craftable_first_port.md.
 *
 * <p>Built by {@code mixin.JeiRecipeGuiLogicMixin} in place of JEI's {@code UnsortedRecipeLayoutList}
 * whenever the open menu has a recipe transfer handler for the category being shown. Order:
 * <ol>
 *   <li>recipes whose + button is enabled (no transfer error, or one whose type still
 *       {@code allowsTransfer}), in JEI's own order;</li>
 *   <li>the rest, most-complete first: by the share of input slots that are not missing, as JEI 19
 *       ranks them. A recipe whose error doesn't say which slots are missing ranks with 0%.</li>
 * </ol>
 * 11.8.1 already builds every layout up front, so unlike JEI 19 this sorts once, eagerly, instead of
 * lazily. The order is computed when JEI builds the list (a new lookup, another category tab, a different
 * menu, or the config toggled), not re-sorted while the screen stays open.
 */
public final class CraftableFirstRecipeLayoutList implements IRecipeLayoutList {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** A sort slower than this is logged, so a large category's cost shows up in latest.log. */
    private static final long SLOW_SORT_NANOS = 250_000_000L;
    private static boolean loggedHandlerFailure;

    private final List<RecipeLayoutWithButtons<?>> layouts;

    private CraftableFirstRecipeLayoutList(List<RecipeLayoutWithButtons<?>> layouts) {
        this.layouts = layouts;
    }

    /**
     * The craftable-first list for these layouts, or null when the sort doesn't apply (turned off,
     * no menu, or the menu can't take transfers for this category); the caller then uses JEI's own list.
     */
    @Nullable
    public static IRecipeLayoutList sortIfCraftingMenu(@Nullable AbstractContainerMenu menu, @Nullable Player player,
                                                       List<? extends RecipeLayoutWithButtons<?>> layouts) {
        if (menu == null || player == null || layouts.size() < 2 || !com.SaintAlfonzo.creativecompat.CompatConfig.JEI.craftableFirst.get()) {
            return null;
        }
        IRecipeTransferManager transferManager = Internal.getJeiRuntime().getRecipeTransferManager();
        IRecipeCategory<?> category = layouts.get(0).recipeLayout().getRecipeCategory();
        if (transferManager.getRecipeTransferHandler(menu, category).isEmpty()) {
            return null;
        }

        long start = System.nanoTime();
        List<Ranked> ranked = new ArrayList<>(layouts.size());
        for (RecipeLayoutWithButtons<?> layout : layouts) {
            ranked.add(rank(transferManager, menu, player, layout));
        }
        // List.sort is stable, so ties keep JEI's order.
        ranked.sort(Comparator.comparingInt(Ranked::group).thenComparingInt(Ranked::key));

        List<RecipeLayoutWithButtons<?>> sorted = new ArrayList<>(ranked.size());
        for (Ranked r : ranked) {
            sorted.add(r.layout());
        }
        long elapsed = System.nanoTime() - start;
        if (elapsed > SLOW_SORT_NANOS) {
            LOGGER.info("JEI craftable-first sort of {} '{}' recipes took {} ms",
                    layouts.size(), category.getRecipeType().getUid(), elapsed / 1_000_000L);
        }
        return new CraftableFirstRecipeLayoutList(sorted);
    }

    /** group 0 = + button enabled, 1 = not; key orders within group 1 (lower first). */
    private record Ranked(RecipeLayoutWithButtons<?> layout, int group, int key) {}

    private static Ranked rank(IRecipeTransferManager transferManager, AbstractContainerMenu menu, Player player,
                               RecipeLayoutWithButtons<?> layout) {
        IRecipeTransferError error;
        try {
            Optional<IRecipeTransferError> result =
                    RecipeTransferUtil.getTransferRecipeError(transferManager, menu, layout.recipeLayout(), player);
            error = result.orElse(null);
        } catch (RuntimeException e) {
            // The sort asks about recipes JEI would never have checked (off-screen pages), so a broken
            // handler must not take the recipe screen down with it; such recipes just sort last.
            if (!loggedHandlerFailure) {
                loggedHandlerFailure = true;
                LOGGER.warn("JEI craftable-first: transfer check failed, sorting the recipe last", e);
            }
            return new Ranked(layout, 1, 0);
        }
        if (error == null || error.getType().allowsTransfer) {
            return new Ranked(layout, 0, 0);
        }
        return new Ranked(layout, 1, missingKey(layout.recipeLayout(), error));
    }

    /** JEI 19 {@code RecipeSortUtil}: minus the percentage of input slots present; 0 when unknown. */
    private static int missingKey(IRecipeLayoutDrawable<?> recipeLayout, IRecipeTransferError error) {
        if (!(error instanceof RecipeTransferErrorMissingSlots)) {
            return 0;
        }
        int missing = ((JeiMissingSlotsErrorAccessor) error).creativecommon$getSlots().size();
        int inputs = 0;
        for (IRecipeSlotView slot : recipeLayout.getRecipeSlotsView().getSlotViews()) {
            if (slot.getRole() == RecipeIngredientRole.INPUT && !slot.isEmpty()) {
                inputs++;
            }
        }
        if (inputs == 0) {
            return 0;
        }
        return -(100 * (inputs - missing) / inputs);
    }

    @Override
    public int size() {
        return layouts.size();
    }

    @Override
    public List<RecipeLayoutWithButtons<?>> subList(int from, int to) {
        return layouts.subList(from, to);
    }

    @Override
    public Optional<RecipeLayoutWithButtons<?>> findFirst() {
        return layouts.isEmpty() ? Optional.empty() : Optional.of(layouts.get(0));
    }

    @Override
    public void tick() {
        // Same as UnsortedRecipeLayoutList: visible layouts are ticked by RecipeGuiLayouts.
    }
}
