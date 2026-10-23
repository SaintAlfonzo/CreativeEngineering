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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.GrapeBushBlock;
import net.satisfy.vinery.core.block.PaleStemBlock;
import net.satisfy.vinery.core.item.GrapeBushSeedItem;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.GrapeType;
import net.satisfy.vinery.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Grape planting (code, not a recipe type; alcohol/vineryport #23): for each grape type and each way to grow it,
 * seeds + what they go on -> grapes when picked and the seeds back. Every number is read from the port's blocks:
 * <ul>
 *   <li>bush ({@link GrapeBushBlock}, red, white, savanna, taiga): seed on any full opaque block, light
 *       {@code minLight()} or more (taiga also needs podzol, coarse dirt or grass within 4); grows on
 *       {@code grapeGrowthChance x grapeGrowthMultiplier} of random ticks, stages 0 to 3; right-click picks 0-1 at
 *       stage 2, 1-2 at stage 3, back to stage 1. Breaking drops seeds (block loot, 1.3.10's tables).</li>
 *   <li>vine ({@code GrapeVineBlock}, jungle): placed like vines; 1 in 5 random ticks with light 9 above; same
 *       picking; shears toggle sterilized (no growth or spread).</li>
 *   <li>stem ({@link PaleStemBlock}, non-jungle): seed on the top of a 2-high stem, light 9, stages 1 to 4; picking at
 *       stage 4 gives 3-4, back to stage 2; shears give the seed back (and grapes from stage 3); a player break loses
 *       the seed. Red and white grow grapevine leaves around the stem (setting).</li>
 *   <li>lattice ({@code LegacyLatticeStemBlock}, jungle): seed on an empty lattice on a wall, light 9, every random
 *       tick; right-click from stage 3 (2 grapes, 3-4 at stage 4), back to stage 1; shears empty it, seed back.</li>
 * </ul>
 */
public class GrapePlantingCategory extends PortCategory<GrapePlantingCategory.Display> {
    public static final RecipeType<Display> TYPE = RecipeType.create(Vinery.MOD_ID, "grape_planting", Display.class);
    private static final String LANG = "jei.vinery_backport.grape_planting";
    private static final int WIDTH = 150;
    private static final int X0 = (WIDTH - 104) / 2;
    private static final int SEED_X = X0 + 1, SUPPORT_X = X0 + 19, ROW_Y = 5;
    private static final int ARROW_X = X0 + 42, ARROW_Y = 4;
    private static final int GRAPE_X = X0 + 72, SEED_OUT_X = X0 + 90;
    private static final int TEXT_Y = 28;
    private static final int LINE_HEIGHT = 10;
    private static final int LINES = 6;
    private static final int TEXT_WIDTH = WIDTH - 4;
    /** Hardcoded light levels in GrapeVineBlock#randomTick and LegacyLatticeStemBlock#randomTick. */
    private static final int VINE_LIGHT = 9, LATTICE_LIGHT = 9;
    /** GrapeVineBlock#randomTick: random.nextInt(5) == 0. */
    private static final double VINE_CHANCE = 0.2;
    /** PaleStemBlock#randomTick: grows unless random.nextDouble() >= 0.98 x multiplier. */
    private static final double STEM_CHANCE = 0.98;
    /** Growth chance per random tick that reads as "quickly": stems and lattices, not bushes (0.5) or vines (0.2). */
    private static final double FAST = 0.75;

    public enum Method { BUSH, VINE, STEM, LATTICE }

    public record Display(GrapeType type, Method method) {
    }

    private final IDrawableAnimated arrow;

    public GrapePlantingCategory(IGuiHelper gui) {
        super(gui, TYPE, Component.translatable(LANG), new ItemStack(ObjectRegistry.RED_GRAPE_SEEDS.get()), WIDTH,
                TEXT_Y + LINES * LINE_HEIGHT);
        this.arrow = animatedArrow(200);
    }

    /** Two pages per grape type: in the wild (bush or vine) and on a support (stem or lattice). */
    public static List<Display> collect() {
        List<Display> out = new ArrayList<>();
        for (GrapeType type : GrapeTypeRegistry.GRAPE_TYPE_TYPES) {
            if (type == GrapeTypeRegistry.NONE || type.getSeeds() == null || type.getSeeds() == Items.AIR
                    || type.getFruit() == null || type.getFruit() == Items.AIR) {
                continue;
            }
            out.add(new Display(type, type.isLattice() ? Method.VINE : Method.BUSH));
            out.add(new Display(type, type.isLattice() ? Method.LATTICE : Method.STEM));
        }
        return out;
    }

    private static Block wildBlock(GrapeType type) {
        return type.getSeeds() instanceof GrapeBushSeedItem seed ? seed.getBlock() : null;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Display recipe, @NotNull IFocusGroup focuses) {
        GrapeType type = recipe.type();
        Method method = recipe.method();
        builder.addSlot(RecipeIngredientRole.INPUT, SEED_X, ROW_Y).addItemStack(new ItemStack(type.getSeeds()));

        List<ItemStack> support = new ArrayList<>();
        String supportTip;
        switch (method) {
            case BUSH -> {
                if (wildBlock(type) instanceof GrapeBushBlock.TaigaGrapeBush) {
                    support.add(new ItemStack(Items.PODZOL));
                    support.add(new ItemStack(Items.COARSE_DIRT));
                    support.add(new ItemStack(Items.GRASS_BLOCK));
                    supportTip = LANG + ".support.taiga";
                } else {
                    support.add(new ItemStack(Items.GRASS_BLOCK));
                    support.add(new ItemStack(Items.DIRT));
                    support.add(new ItemStack(Items.STONE));
                    supportTip = LANG + ".support.ground";
                }
            }
            case VINE -> {
                support.add(new ItemStack(Items.JUNGLE_LOG));
                supportTip = LANG + ".support.vine";
            }
            case STEM -> {
                support.add(new ItemStack(ObjectRegistry.GRAPEVINE_STEM.get(), 2));
                supportTip = LANG + ".support.stem";
            }
            default -> {
                support.add(new ItemStack(ObjectRegistry.GRAPEVINE_LATTICE.get()));
                supportTip = LANG + ".support.lattice";
            }
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, SUPPORT_X, ROW_Y).addItemStacks(support)
                .addTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable(supportTip).withStyle(ChatFormatting.GRAY)));

        String grapeTip = switch (method) {
            case BUSH, VINE -> LANG + ".grapes.bush";
            case STEM -> LANG + ".grapes.stem";
            default -> LANG + ".grapes.lattice";
        };
        builder.addSlot(RecipeIngredientRole.OUTPUT, GRAPE_X, ROW_Y).addItemStack(new ItemStack(type.getFruit()))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable(grapeTip).withStyle(ChatFormatting.GRAY)));

        String seedTip = method == Method.BUSH || method == Method.VINE ? LANG + ".seeds.break" : LANG + ".seeds.shears";
        builder.addSlot(RecipeIngredientRole.OUTPUT, SEED_OUT_X, ROW_Y).addItemStack(new ItemStack(type.getSeeds()))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable(seedTip).withStyle(ChatFormatting.GRAY)));
    }

    /** The page's text, before wrapping: where, light and growth speed, then the method's extra rule. */
    private static List<Component> lines(Display recipe) {
        GrapeType type = recipe.type();
        double multiplier = PlatformHelper.getGrapeGrowthMultiplier();
        List<Component> out = new ArrayList<>();
        switch (recipe.method()) {
            case BUSH -> {
                Block block = wildBlock(type);
                int light = block instanceof GrapeBushBlock bush ? bush.minLight() : 10;
                out.add(Component.translatable(LANG + ".where.bush", speed(PlatformHelper.getGrapeGrowthChance() * multiplier), light));
                if (block instanceof GrapeBushBlock.TaigaGrapeBush) {
                    out.add(Component.translatable(LANG + ".taiga"));
                }
            }
            case VINE -> {
                out.add(Component.translatable(LANG + ".where.vine", speed(VINE_CHANCE), VINE_LIGHT));
                out.add(Component.translatable(LANG + ".vine_shears"));
            }
            case STEM -> {
                out.add(Component.translatable(LANG + ".where.stem", speed(STEM_CHANCE * multiplier), PaleStemBlock.MIN_LIGHT));
                if ((type == GrapeTypeRegistry.RED || type == GrapeTypeRegistry.WHITE) && PlatformHelper.shouldGrapevineLeavesGrow()) {
                    out.add(Component.translatable(LANG + ".leaves"));
                }
            }
            default -> {
                out.add(Component.translatable(LANG + ".where.lattice", speed(1.0), LATTICE_LIGHT));
            }
        }
        return out;
    }

    /** "quickly" from {@link #FAST} of random ticks up, else "slowly". */
    private static Component speed(double chance) {
        return Component.translatable(LANG + (chance >= FAST ? ".fast" : ".slow"));
    }

    @Override
    public void draw(@NotNull Display recipe, @NotNull IRecipeSlotsView view, @NotNull PoseStack pose, double mouseX, double mouseY) {
        drawSlot(pose, SEED_X, ROW_Y);
        drawSlot(pose, SUPPORT_X, ROW_Y);
        drawArrow(pose, arrow, ARROW_X, ARROW_Y);
        drawSlot(pose, GRAPE_X, ROW_Y);
        drawSlot(pose, SEED_OUT_X, ROW_Y);
        var font = Minecraft.getInstance().font;
        int y = TEXT_Y;
        int drawn = 0;
        for (Component line : lines(recipe)) {
            for (FormattedCharSequence part : font.split(line, TEXT_WIDTH)) {
                if (drawn++ == LINES) return;
                drawCentered(pose, part, WIDTH / 2, y);
                y += LINE_HEIGHT;
            }
        }
    }
}
