package net.satisfy.vinery.core.compat.jei.port;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.GrapevinePotBlock;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.GrapeType;
import net.satisfy.vinery.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Grape stomping in the Grapevine Pot (code, not a recipe type): a full pot of grapes of one type (12), a number of
 * stomps (setting {@code grapevinePotStomps}, 10% more progress per stomp with 15+ armor), then each wine bottle takes
 * 3 grapes' worth of juice.
 */
public class GrapeStompingCategory extends PortCategory<GrapeStompingCategory.Display> {
    public static final RecipeType<Display> TYPE = RecipeType.create(Vinery.MOD_ID, "grape_stomping", Display.class);
    private static final String LANG = "jei.vinery_backport.grape_stomping";
    private static final int WIDTH = 120;
    private static final int X0 = (WIDTH - 101) / 2;
    private static final int GRAPE_X = X0 + 1, BOTTLE_X = X0 + 19, ROW_Y = 5;
    private static final int ARROW_X = X0 + 42, ARROW_Y = 4;
    private static final int RESULT_X = X0 + 80;
    private static final int TEXT_Y = 31;

    public record Display(ItemStack grape, ItemStack juice) {
    }

    private final IDrawableAnimated arrow;

    public GrapeStompingCategory(IGuiHelper gui) {
        super(gui, TYPE, Component.translatable(LANG), new ItemStack(ObjectRegistry.GRAPEVINE_POT.get()), WIDTH, 50);
        this.arrow = animatedArrow(200);
    }

    public static List<Display> collect() {
        List<Display> out = new ArrayList<>();
        for (GrapeType type : GrapeTypeRegistry.GRAPE_TYPE_TYPES) {
            if (type == GrapeTypeRegistry.NONE || type.getFruit() == null) {
                continue;
            }
            ItemStack grape = new ItemStack(type.getFruit());
            ItemStack juice = new ItemStack(type.getBottle());
            if (!grape.isEmpty() && !juice.isEmpty()) {
                out.add(new Display(grape, juice));
            }
        }
        return out;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Display recipe, @NotNull IFocusGroup focuses) {
        int bottles = GrapevinePotBlock.MAX_GRAPES / GrapevinePotBlock.GRAPES_PER_BOTTLE;
        builder.addSlot(RecipeIngredientRole.INPUT, GRAPE_X, ROW_Y)
                .addItemStack(withCount(recipe.grape(), GrapevinePotBlock.MAX_GRAPES))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable(LANG + ".one_type").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.INPUT, BOTTLE_X, ROW_Y)
                .addItemStack(new ItemStack(ObjectRegistry.WINE_BOTTLE.get(), bottles))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable(LANG + ".per_bottle", GrapevinePotBlock.GRAPES_PER_BOTTLE).withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).addItemStack(withCount(recipe.juice(), bottles));
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    @Override
    public void draw(@NotNull Display recipe, @NotNull IRecipeSlotsView view, @NotNull PoseStack pose, double mouseX, double mouseY) {
        drawSlot(pose, GRAPE_X, ROW_Y);
        drawSlot(pose, BOTTLE_X, ROW_Y);
        drawArrow(pose, arrow, ARROW_X, ARROW_Y);
        drawBigSlot(pose, RESULT_X, ROW_Y);
        drawCentered(pose, Component.translatable(LANG + ".stomps", PlatformHelper.getGrapevinePotStomps()), WIDTH / 2, TEXT_Y);
        drawCentered(pose, Component.translatable(LANG + ".armor", PlatformHelper.getGrapevinePotHeavyArmorBonus()), WIDTH / 2, TEXT_Y + 10);
    }
}
