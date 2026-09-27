package com.SaintAlfonzo.creativeengineering.compat;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.SaintAlfonzo.creativeengineering.fan.DragonBreathingRecipe;
import com.SaintAlfonzo.creativeengineering.fan.DragonBreathingRecipeType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * JEI page for {@code creativeengineering:dragon_breathing} fan recipes: the same
 * "processing via fan" layout Create uses for washing / smoking / blasting / haunting,
 * with the Encased Fan + a Dragon Head as catalysts. Adapted from Create: Halitosis'
 * {@code HalitosisJEI} to Create 0.5.1.i / JEI 11.8.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class DragonBreathingJEI implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, "jei_plugin");
    private static final String LANG_KEY = "create.recipe.fan_dragon_breathing";

    private Category category;
    private AutomatedPotionMixingCategory potionMixing;
    private LingerificationCategory lingerification;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        RecipeType<DragonBreathingRecipe> jeiType =
                new RecipeType<>(DragonBreathingRecipeType.ID, DragonBreathingRecipe.class);

        IDrawable background = new EmptyBackground(178, 72);
        IDrawable icon = new DoubleItemIcon(
                () -> new ItemStack(AllItems.PROPELLER.get()),
                () -> new ItemStack(Items.DRAGON_HEAD));

        net.minecraft.world.item.crafting.RecipeType<DragonBreathingRecipe> vanillaType =
                DragonBreathingRecipeType.INSTANCE.getType();
        Supplier<List<DragonBreathingRecipe>> recipes = () -> {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                return List.of();
            }
            return connection.getRecipeManager().getAllRecipesFor(vanillaType);
        };

        List<Supplier<? extends ItemStack>> catalysts = List.of(
                ProcessingViaFanCategory.getFan("fan_dragon_breathing"),
                () -> new ItemStack(Items.DRAGON_HEAD));

        CreateRecipeCategory.Info<DragonBreathingRecipe> info = new CreateRecipeCategory.Info<>(
                jeiType, Component.translatable(LANG_KEY), background, icon, recipes, catalysts);

        category = new Category(info);
        registration.addRecipeCategories(category);

        try {
            potionMixing = new AutomatedPotionMixingCategory(
                    registration.getJeiHelpers().getGuiHelper());
            registration.addRecipeCategories(potionMixing);
            AutomatedPotionMixingCategory.ACTIVE = true;
        } catch (Throwable t) {
            potionMixing = null;
            AutomatedPotionMixingCategory.ACTIVE = false;
            org.apache.logging.log4j.LogManager.getLogger("creativeengineering").error(
                    "[CE] Automated Potion Mixing JEI category disabled - potion recipes stay in the "
                            + "IE Mixer category", t);
        }

        try {
            lingerification = new LingerificationCategory(
                    registration.getJeiHelpers().getGuiHelper());
            registration.addRecipeCategories(lingerification);
            LingerificationCategory.ACTIVE = true;
        } catch (Throwable t) {
            lingerification = null;
            LingerificationCategory.ACTIVE = false;
            org.apache.logging.log4j.LogManager.getLogger("creativeengineering").error(
                    "[CE] Lingerification JEI category disabled - refinery lingering recipes stay in "
                            + "the IE Refinery category", t);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (category != null) {
            category.registerRecipes(registration);
        }

        net.minecraft.world.level.Level level = Minecraft.getInstance().level;
        if (potionMixing != null && level != null) {
            List<MixerRecipe> potions = MixerRecipe.RECIPES.getRecipes(level).stream()
                    .filter(AutomatedPotionMixingCategory::isPotionMixer)
                    .toList();
            registration.addRecipes(AutomatedPotionMixingCategory.TYPE, potions);
        }

        if (lingerification != null && level != null) {
            List<RefineryRecipe> lingering = RefineryRecipe.RECIPES.getRecipes(level).stream()
                    .filter(LingerificationCategory::isLingerification)
                    .toList();
            registration.addRecipes(LingerificationCategory.TYPE, lingering);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (category != null) {
            category.registerCatalysts(registration);
        }
        if (potionMixing != null) {
            registration.addRecipeCatalyst(
                    new ItemStack(IEBlocks.Multiblocks.MIXER), AutomatedPotionMixingCategory.TYPE);
        }
        if (lingerification != null) {
            registration.addRecipeCatalyst(
                    new ItemStack(IEBlocks.Multiblocks.REFINERY), LingerificationCategory.TYPE);
        }
    }

    // MultiOutput (not the single-output ProcessingViaFanCategory base) is what renders per-output
    // chance in JEI: its setRecipe iterates recipe.getRollableResults() and attaches
    // CreateRecipeCategory.addStochasticTooltip to each slot. Create's own splashing / smoking /
    // blasting / haunting fan categories all extend this; the plain base just addItemStack()s
    // getResultItem() with no chance. Our DragonBreathingRecipe extends ProcessingRecipe, so it
    // satisfies MultiOutput's `T extends ProcessingRecipe<?>` bound.
    private static class Category extends ProcessingViaFanCategory.MultiOutput<DragonBreathingRecipe> {

        Category(CreateRecipeCategory.Info<DragonBreathingRecipe> info) {
            super(info);
        }

        @Override
        protected void renderAttachedBlock(PoseStack matrixStack) {
            AnimatedKinetics.defaultBlockElement(Blocks.DRAGON_HEAD.defaultBlockState())
                    .rotateBlock(0.0D, 180.0D, 0.0D)
                    .atLocal(0.0D, 0.0D, 0.0D)
                    .scale(SCALE)
                    .render(matrixStack);
        }
    }
}
