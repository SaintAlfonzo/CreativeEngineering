package net.satisfy.vinery.core.compat.jei.port;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.VintageClock;
import net.satisfy.vinery.core.wine.WineYears;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wine aging on the vintage clock (alcohol.md B4.5): fresh wine -> fully aged wine, with the real time per month and
 * per level, and the cellar speed-up. The stacks carry real WineYear data, so the wine tooltip shows the levels. The
 * text wraps to the page in JEI's dark grey (B7.1); the page is as tall as the wrapped lines need.
 */
public class WineAgingCategory extends PortCategory<WineAgingCategory.Display> {
    public static final RecipeType<Display> TYPE = RecipeType.create(Vinery.MOD_ID, "wine_aging", Display.class);
    private static final String LANG = "jei.vinery_backport.wine_aging";
    private static final int WIDTH = 150;
    private static final int X0 = (WIDTH - 125) / 2;
    private static final int RACK_X = X0 + 1, FRESH_X = X0 + 50, RESULT_X = X0 + 104, ROW_Y = 5;
    private static final int STAMP_ARROW_X = X0 + 21, AGE_ARROW_X = X0 + 71, ARROW_Y = 4;
    private static final int TEXT_Y = 31;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_WIDTH = WIDTH - 4;

    public record Display(ItemStack wine) {
    }

    private final IDrawableAnimated stampArrow;
    private final IDrawableAnimated ageArrow;

    public WineAgingCategory(IGuiHelper gui) {
        super(gui, TYPE, Component.translatable(LANG), new ItemStack(ObjectRegistry.OAK_WINE_RACK_BIG.get()), WIDTH,
                TEXT_Y + wrapped(VintageClock.of(level())).size() * LINE_HEIGHT);
        this.stampArrow = animatedArrow(100);
        this.ageArrow = animatedArrow(400);
    }

    /** One page per aging wine the barrel makes. */
    public static List<Display> collect(List<FermentationBarrelRecipe> recipes) {
        Set<Item> seen = new HashSet<>();
        List<Display> out = new ArrayList<>();
        for (FermentationBarrelRecipe recipe : recipes) {
            ItemStack wine = recipe.getResultItem().copy();
            if (wine.getItem() instanceof DrinkBlockItem && seen.add(wine.getItem())) {
                wine.setCount(1);
                out.add(new Display(wine));
            }
        }
        return out;
    }

    private static Level level() {
        return Minecraft.getInstance().level;
    }

    private static ItemStack aged(ItemStack wine, int days) {
        ItemStack copy = wine.copy();
        Level level = level();
        if (level != null) {
            WineYears.setWineYear(copy, level);
            WineYears.setWineAgeDays(copy, level, days);
        }
        return copy;
    }

    private static int fullAgeDays(VintageClock.Snapshot clock) {
        return clock.maxLevel() * clock.yearsPerEffectLevel() * clock.daysPerYear();
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Display recipe, @NotNull IFocusGroup focuses) {
        VintageClock.Snapshot clock = VintageClock.of(level());
        builder.addSlot(RecipeIngredientRole.CATALYST, RACK_X, ROW_Y).addItemStack(new ItemStack(ObjectRegistry.OAK_WINE_RACK_BIG.get()));
        builder.addSlot(RecipeIngredientRole.INPUT, FRESH_X, ROW_Y).addItemStack(aged(recipe.wine(), 0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).addItemStack(aged(recipe.wine(), fullAgeDays(clock)));
    }

    private static List<Component> lines(VintageClock.Snapshot clock) {
        return List.of(
                Component.translatable(LANG + ".year", clock.daysPerYear(), DrinkBlockItem.realTime(clock.realTicks(clock.ticksPerYear()))),
                Component.translatable(LANG + ".level", clock.yearsPerEffectLevel(), DrinkBlockItem.realTime(clock.realTicks(clock.ticksPerYear() * clock.yearsPerEffectLevel()))),
                Component.translatable(LANG + ".cellar", String.format("%.1f", clock.cellarPercent() / 100.0)));
    }

    private static List<FormattedCharSequence> wrapped(VintageClock.Snapshot clock) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> out = new ArrayList<>();
        for (Component line : lines(clock)) {
            out.addAll(font.split(line, TEXT_WIDTH));
        }
        return out;
    }

    @Override
    public void draw(@NotNull Display recipe, @NotNull IRecipeSlotsView view, @NotNull PoseStack pose, double mouseX, double mouseY) {
        VintageClock.Snapshot clock = VintageClock.of(level());
        drawSlot(pose, RACK_X, ROW_Y);
        drawArrow(pose, stampArrow, STAMP_ARROW_X, ARROW_Y);
        drawSlot(pose, FRESH_X, ROW_Y);
        drawArrow(pose, ageArrow, AGE_ARROW_X, ARROW_Y);
        drawBigSlot(pose, RESULT_X, ROW_Y);
        int y = TEXT_Y;
        for (FormattedCharSequence line : wrapped(clock)) {
            drawCentered(pose, line, WIDTH / 2, y);
            y += LINE_HEIGHT;
        }
    }
}
