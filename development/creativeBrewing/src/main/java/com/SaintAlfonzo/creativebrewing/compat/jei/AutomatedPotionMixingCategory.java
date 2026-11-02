package com.SaintAlfonzo.creativebrewing.compat.jei;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.mixer.MixerRecipeCategory;
import com.simibubi.create.AllFluids;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;

/**
 * "Automated Potion Mixing" — a JEI category that reuses IE's Mixer layout / rendering (same
 * slots, same tank + arrow art, same {@code MixerRecipeCategoryMixin} potion tooltip) but under
 * its own {@link RecipeType}, so the ~400 pack-generated potion brews
 * ({@code PotionRecipeGeneratorsMixin}) get their own page instead of drowning the real IE Mixer
 * category. {@code JEIHelperMixin} removes the same recipes from IE's Mixer page.
 *
 * <p>{@code IERecipeCategory.getRecipeType()} is {@code final} and the {@code type} field is
 * {@code private final}, so we swap it by reflection right after {@code super(...)} — a
 * non-static final instance field is writable via {@code setAccessible(true)} on Java 17.
 */
public class AutomatedPotionMixingCategory extends MixerRecipeCategory {

    public static final RecipeType<MixerRecipe> TYPE = new RecipeType<>(
            new ResourceLocation("creativeengineering", "automated_potion_mixing"), MixerRecipe.class);

    /**
     * True once the category is built + registered. {@code JEIHelperMixin} only strips potion
     * recipes out of IE's Mixer page while this holds — if the category failed to construct
     * (reflection blocked, IE internals changed), the recipes stay where they were.
     */
    public static volatile boolean ACTIVE = false;

    public AutomatedPotionMixingCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        try {
            Field typeField = IERecipeCategory.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(this, TYPE);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "[CE] could not rebind IE mixer category type for Automated Potion Mixing", e);
        }
        this.title = Component.translatable("creativeengineering.jei.automated_potion_mixing");
    }

    /** A mixer recipe belongs on this page iff it outputs Create's {@code create:potion} fluid. */
    public static boolean isPotionMixer(MixerRecipe recipe) {
        FluidStack out = recipe.fluidOutput;
        return out != null && !out.isEmpty() && out.getFluid().isSame(AllFluids.POTION.get());
    }
}
