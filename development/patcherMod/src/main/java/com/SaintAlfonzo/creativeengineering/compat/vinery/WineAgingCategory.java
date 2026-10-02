package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * How Vinery wine aging works (WineYears). Nothing ticks or changes over time: when the Aging
 * Barrel (fermentation barrel) finishes a drink it writes NBT {@code Year} = the current wine
 * year, once. On drinking, the effect level is {@code min(5, (currentYear - Year) / yearsPerEffectLevel)},
 * where a wine year is {@code yearLengthInDays} in-game days (config/vinery/config.json5).
 *
 * <p>Page: barrel -> wine stamped with this year -> the same wine as it reads once fully aged.
 * The stacks carry real {@code Year} tags relative to the client's current year (computed when
 * the page opens), so Vinery's own tooltip shows the matching year and potency.
 */
@ParametersAreNonnullByDefault
class WineAgingCategory extends VineryCategory<WineAgingCategory.Display> {
    static final RecipeType<Display> TYPE =
            new RecipeType<>(new ResourceLocation(CreativeEngineering.MODID, "vinery_wine_aging"), Display.class);

    private static final String YEAR_TAG = "Year";
    private static final String LANG = "creativeengineering.jei.vinery.wine_aging.";

    private static final int WIDTH = 150;
    /** Row is 125 wide (barrel, arrow, wine, arrow, large result); centred in the page. */
    private static final int X0 = (WIDTH - 125) / 2;
    private static final int BARREL_X = X0 + 1, FRESH_X = X0 + 50, RESULT_X = X0 + 104, ROW_Y = 5;
    private static final int STAMP_ARROW_X = X0 + 21, AGE_ARROW_X = X0 + 71, ARROW_Y = 4;
    private static final int TEXT_Y = 31;

    record Display(ItemStack wine) {
    }

    private final IDrawableAnimated stampArrow;
    private final IDrawableAnimated ageArrow;

    WineAgingCategory(IGuiHelper gui) {
        super(gui, TYPE, Component.translatable(LANG + "title"), VineryIds.FERMENTATION_BARREL, WIDTH, 60);
        this.stampArrow = animatedArrow(VineryIds.FERMENTATION_TICKS / 10);
        this.ageArrow = animatedArrow(VineryIds.FERMENTATION_TICKS / 10);
    }

    /** One page per distinct fermentation output: the barrel stamps {@code Year} on all of them. */
    static List<Display> collect(List<FermentationCategory.Display> fermentation) {
        Set<Item> seen = new HashSet<>();
        List<Display> out = new ArrayList<>();
        for (FermentationCategory.Display recipe : fermentation) {
            ItemStack wine = recipe.output().copy();
            wine.setTag(null);
            wine.setCount(1);
            if (seen.add(wine.getItem())) {
                out.add(new Display(wine));
            }
        }
        return out;
    }

    /** WineYears.getYear on the client level. */
    private static int currentYear() {
        Level level = Minecraft.getInstance().level;
        long day = level == null ? 0 : level.getDayTime() / 24000L;
        return (int) (day / VineryIds.yearLengthDays());
    }

    private static int maxAgeYears() {
        return VineryIds.MAX_EFFECT_LEVEL * VineryIds.yearsPerEffectLevel();
    }

    private static ItemStack withYear(ItemStack wine, int year) {
        ItemStack copy = wine.copy();
        copy.getOrCreateTag().putInt(YEAR_TAG, year);
        return copy;
    }

    /** Effect level 0..5 shown as the potion numeral I..VI. */
    private static Component potency(int level) {
        return Component.translatable("enchantment.level." + (level + 1));
    }

    private static Component gray(String key, Object... args) {
        return Component.translatable(LANG + key, args).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        int year = currentYear();
        int maxAge = maxAgeYears();
        ItemStack barrel = VineryIds.stack(VineryIds.FERMENTATION_BARREL);
        if (!barrel.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, BARREL_X, ROW_Y).addItemStack(barrel);
        }
        builder.addSlot(RecipeIngredientRole.INPUT, FRESH_X, ROW_Y)
                .addItemStack(withYear(recipe.wine(), year))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(gray("fresh", year, potency(0))));
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y)
                .addItemStack(withYear(recipe.wine(), year - maxAge))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(
                        gray("aged", maxAge, potency(VineryIds.MAX_EFFECT_LEVEL))));
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView view, PoseStack pose, double mouseX, double mouseY) {
        drawSlot(pose, BARREL_X, ROW_Y);
        drawArrow(pose, stampArrow, STAMP_ARROW_X, ARROW_Y);
        drawSlot(pose, FRESH_X, ROW_Y);
        drawArrow(pose, ageArrow, AGE_ARROW_X, ARROW_Y);
        drawBigSlot(pose, RESULT_X, ROW_Y);

        int center = WIDTH / 2;
        drawCentered(pose, Component.translatable(LANG + "stamp"), center, TEXT_Y);
        drawCentered(pose, Component.translatable(LANG + "rate", VineryIds.yearsPerEffectLevel(),
                potency(VineryIds.MAX_EFFECT_LEVEL)), center, TEXT_Y + 10);
        drawCentered(pose, Component.translatable(LANG + "year_length", VineryIds.yearLengthDays(),
                currentYear()), center, TEXT_Y + 20);
    }

    @Override
    public List<Component> getTooltipStrings(Display recipe, IRecipeSlotsView view, double mouseX, double mouseY) {
        if (over(mouseX, mouseY, STAMP_ARROW_X, ARROW_Y, ARROW_W, ARROW_H)) {
            return List.of(Component.translatable(LANG + "stamp_arrow", currentYear()));
        }
        if (over(mouseX, mouseY, AGE_ARROW_X, ARROW_Y, ARROW_W, ARROW_H)) {
            int maxAge = maxAgeYears();
            return List.of(Component.translatable(LANG + "age_arrow", maxAge, maxAge * VineryIds.yearLengthDays()));
        }
        return new ArrayList<>();
    }
}
