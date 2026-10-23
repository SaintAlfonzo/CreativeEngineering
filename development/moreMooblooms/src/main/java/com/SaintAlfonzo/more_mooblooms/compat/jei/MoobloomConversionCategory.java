package com.SaintAlfonzo.more_mooblooms.compat.jei;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.entity.MMEntities;
import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * "Moobloom Conversion": one page per variant. A cow (any age, D6) fed one of the variant's foods turns into it
 * with {@code feedChance} per food item ({@link com.SaintAlfonzo.more_mooblooms.entity.CowFeeding}). A food several
 * variants share picks one of them by {@code feed_weight}, so its tooltip shows this variant's share and the chance
 * per item it works out to. The page draws the variant's moobloom.
 */
public class MoobloomConversionCategory implements IRecipeCategory<MoobloomConversionCategory.Recipe> {

    /** {@code feedChance}: config chance one food item converts the cow at all. */
    public record Recipe(MoobloomVariant variant, ItemStack egg, List<ItemStack> food, double feedChance) {}

    public static final RecipeType<Recipe> TYPE = RecipeType.create(MoreMooblooms.ID, "conversion", Recipe.class);

    private static final int WIDTH = 150, HEIGHT = 56;
    private static final int COW_X = 4, FOOD_X = 30, OUT_X = 76, SLOT_Y = HEIGHT / 2 - 8;
    private static final int ENTITY_X = 124, ENTITY_Y = 52;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawable icon;
    private final Map<Recipe, Moobloom> entities = new WeakHashMap<>();

    public MoobloomConversionCategory(IGuiHelper gui) {
        background = gui.createBlankDrawable(WIDTH, HEIGHT);
        slot = gui.getSlotDrawable();
        icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.COW_SPAWN_EGG));
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.more_mooblooms.conversion");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, COW_X, SLOT_Y).addItemStack(new ItemStack(Items.COW_SPAWN_EGG))
                .setBackground(slot, -1, -1)
                .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.more_mooblooms.conversion.cow")
                        .withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.INPUT, FOOD_X, SLOT_Y).addItemStacks(recipe.food()).setBackground(slot, -1, -1)
                .addTooltipCallback((view, tooltip) -> view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack -> {
                    double share = share(recipe.variant(), stack);
                    tooltip.add(Component.translatable("jei.more_mooblooms.conversion.per_item",
                            MoreMoobloomsJeiPlugin.percent(recipe.feedChance() * share)).withStyle(ChatFormatting.GOLD));
                    if (share < 1.0) {
                        tooltip.add(Component.translatable("jei.more_mooblooms.conversion.shared",
                                MoreMoobloomsJeiPlugin.percent(share)).withStyle(ChatFormatting.GRAY));
                    }
                }));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X, SLOT_Y).addItemStack(recipe.egg()).setBackground(slot, -1, -1)
                .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.more_mooblooms.conversion.chance",
                        MoreMoobloomsJeiPlugin.percent(recipe.feedChance())).withStyle(ChatFormatting.GRAY)));
    }

    /** This variant's part of the pick when {@code stack} converts a cow: its feed weight over all matches'. */
    static double share(MoobloomVariant variant, ItemStack stack) {
        int total = 0;
        for (MoobloomVariant v : MoobloomVariants.byFlower(true, stack)) total += v.feedWeight(stack);
        int own = variant.feedWeight(stack);
        return total <= 0 ? 0.0 : (double) own / total;
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        font.draw(pose, "+", COW_X + 18, SLOT_Y + 4, 0x404040);
        font.draw(pose, "→", FOOD_X + 25, SLOT_Y + 4, 0x404040);
        Moobloom moobloom = entity(recipe);
        if (moobloom != null) MoobloomShearingCategory.renderEntity(pose, moobloom, mouseX, ENTITY_X, ENTITY_Y);
    }

    private Moobloom entity(Recipe recipe) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        Moobloom moobloom = entities.get(recipe);
        if (moobloom == null || moobloom.level != level) {
            moobloom = MMEntities.MOOBLOOM.get().create(level);
            if (moobloom == null) return null;
            moobloom.setVariant(recipe.variant());
            List<MoobloomVariant.Flower> pool = recipe.variant().pool();
            List<BlockState> states = new ArrayList<>(MoobloomVariant.Slot.COUNT);
            for (int i = 0; i < MoobloomVariant.Slot.COUNT; i++) states.add(pool.get(i % pool.size()).state());
            moobloom.setDisplayFlowers(states);
            entities.put(recipe, moobloom);
        }
        return moobloom;
    }
}
