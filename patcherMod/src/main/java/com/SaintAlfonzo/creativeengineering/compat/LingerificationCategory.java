package com.SaintAlfonzo.creativeengineering.compat;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.refinery.RefineryRecipeCategory;
import com.simibubi.create.AllFluids;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;

/**
 * "Lingerification" — a JEI category that reuses IE's Refinery layout / rendering (same tanks,
 * same arrow art) but under its own {@link RecipeType}, so the pack-generated
 * dragon's-breath-fluid potion-lingering brews ({@link com.SaintAlfonzo.creativeengineering.fluidbrewing.Lingerification})
 * get their own page instead of drowning IE's real Refinery category next to biodiesel / resin.
 * {@code JEIHelperMixin} removes the same recipes from IE's Refinery page.
 *
 * <p>Same trick as {@link AutomatedPotionMixingCategory}: {@code IERecipeCategory.getRecipeType()}
 * is {@code final} and the {@code type} field is {@code private final}, so we swap it by
 * reflection right after {@code super(...)}.
 */
public class LingerificationCategory extends RefineryRecipeCategory {

    public static final RecipeType<RefineryRecipe> TYPE = new RecipeType<>(
            new ResourceLocation("creativeengineering", "lingerification"), RefineryRecipe.class);

    /**
     * True once the category is built + registered. {@code JEIHelperMixin} only strips these
     * recipes out of IE's Refinery page while this holds — if the category failed to construct,
     * they stay where they were.
     */
    public static volatile boolean ACTIVE = false;

    public LingerificationCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        try {
            Field typeField = IERecipeCategory.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(this, TYPE);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "[CE] could not rebind IE refinery category type for Lingerification", e);
        }
        this.title = Component.translatable("creativeengineering.jei.lingerification");
    }

    /**
     * A refinery recipe belongs on this page iff it outputs a LINGERING {@code create:potion}
     * fluid. Within {@code RefineryRecipe.RECIPES} those are only ever ours (IE's lingering
     * mixer-parity recipes are {@code MixerRecipe}, a separate list).
     */
    public static boolean isLingerification(RefineryRecipe recipe) {
        FluidStack out = recipe.output;
        if (out == null || out.isEmpty() || !out.getFluid().isSame(AllFluids.POTION.get())) {
            return false;
        }
        return out.hasTag() && out.getTag() != null
                && "LINGERING".equals(out.getTag().getString("Bottle"));
    }
}
