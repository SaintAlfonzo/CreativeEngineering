package com.SaintAlfonzo.creativeengineering.fan;

import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * A single-input Create processing recipe consumed by {@link DragonBreathingFanProcessingType}
 * (the dragon-head fan catalyst). Datapack / KubeJS type id: {@code creativeengineering:dragon_breathing}.
 *
 * <p>Modelled on Create's own {@code SplashingRecipe}. Create's {@code ProcessingRecipe}
 * already implements every {@code Recipe} method, but this project compiles against the
 * reobfuscated (SRG) Create jar, so javac cannot see those inherited implementations as
 * satisfying the Mojmap {@code Recipe} interface. The thin overrides below re-declare them
 * under their Mojmap names purely so the class compiles; {@code reobfJar} renames them back
 * onto {@code ProcessingRecipe}'s slots and behaviour is unchanged. Fan processing itself
 * only uses {@code getRollableResults()} (inherited, Create-named, unaffected).
 */
public class DragonBreathingRecipe extends ProcessingRecipe<DragonBreathingRecipe.Wrapper> {

    public DragonBreathingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(DragonBreathingRecipeType.INSTANCE, params);
    }

    @Override
    public boolean matches(@Nonnull Wrapper inv, @Nonnull Level level) {
        return !inv.isEmpty() && ingredients.get(0).test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    // --- Mojmap Recipe methods (compile-time only; see class javadoc) ---

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeType<?> getType() {
        return DragonBreathingRecipeType.INSTANCE.getType();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DragonBreathingRecipeType.INSTANCE.getSerializer();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        // Mirror ProcessingRecipe's own behaviour (first rollable result) without
        // naming its reobfuscated m_8043_(); getRollableResultsAsItemStacks() is
        // Create's own, un-obfuscated method.
        List<ItemStack> results = getRollableResultsAsItemStacks();
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
    }

    @Override
    public ItemStack assemble(@Nonnull Wrapper inv) {
        return getResultItem();
    }

    public static class Wrapper extends RecipeWrapper {
        public Wrapper() {
            super(new ItemStackHandler(1));
        }
    }
}
