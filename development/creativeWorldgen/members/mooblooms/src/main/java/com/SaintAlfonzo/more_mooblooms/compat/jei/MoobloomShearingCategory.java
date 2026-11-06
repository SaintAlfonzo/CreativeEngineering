package com.SaintAlfonzo.more_mooblooms.compat.jei;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.entity.MMEntities;
import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * "Moobloom Shearing": one page per variant. The variant's egg and shears -> each pool item with the expected
 * count per shear (both shears give the same, 4.6), plus the cow egg when shearing it bare turns it back into a
 * cow. The page draws the moobloom with its flowers cycling through the whole pool.
 */
public class MoobloomShearingCategory implements IRecipeCategory<MoobloomShearingCategory.Recipe> {

    /** {@code perSpot}: the chance one flower position drops that item; three positions per shear. */
    public record Recipe(MoobloomVariant variant, ItemStack egg, List<ItemStack> drops, List<Double> perSpot,
                         boolean bareToCow, boolean bareDrops) {}

    public static final RecipeType<Recipe> TYPE = RecipeType.create(MoreMooblooms.ID, "shearing", Recipe.class);

    private static final int WIDTH = 150, HEIGHT = 56;
    private static final int ENTITY_X = 28, ENTITY_Y = 52;
    private static final int EGG_X = 62, OUT_X = 96, COLUMNS = 3;
    /** Ticks (1/20 s) each flower stays before the next pool entry shows. */
    private static final int CYCLE_TICKS = 30;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawable icon;
    /** One display entity per recipe, made again when the client level changes. */
    private final Map<Recipe, Moobloom> entities = new WeakHashMap<>();

    public MoobloomShearingCategory(IGuiHelper gui) {
        background = gui.createBlankDrawable(WIDTH, HEIGHT);
        slot = gui.getSlotDrawable();
        icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SHEARS));
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.more_mooblooms.shearing");
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
        builder.addSlot(RecipeIngredientRole.INPUT, EGG_X, HEIGHT / 2 - 8).addItemStack(recipe.egg()).setBackground(slot, -1, -1);
        int count = recipe.drops().size() + (recipe.bareToCow() ? 1 : 0);
        int rows = (count + COLUMNS - 1) / COLUMNS;
        int y0 = (HEIGHT - rows * 18) / 2 + 1;
        for (int i = 0; i < count; i++) {
            int x = OUT_X + (i % COLUMNS) * 18, y = y0 + (i / COLUMNS) * 18;
            if (i < recipe.drops().size()) {
                double spot = recipe.perSpot().get(i);
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(recipe.drops().get(i)).setBackground(slot, -1, -1)
                        .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.more_mooblooms.shearing.per_shear",
                                amount(spot * MoobloomVariant.Slot.COUNT), MoreMoobloomsJeiPlugin.percent(spot))));
            } else {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(new ItemStack(Items.COW_SPAWN_EGG)).setBackground(slot, -1, -1)
                        .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(recipe.bareDrops()
                                ? "jei.more_mooblooms.shearing.bare_drops" : "jei.more_mooblooms.shearing.bare")));
            }
        }
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
        Minecraft.getInstance().font.draw(pose, "→", EGG_X + 21, HEIGHT / 2 - 4, 0x404040);
        Moobloom moobloom = entity(recipe);
        if (moobloom != null) renderEntity(pose, moobloom, mouseX, ENTITY_X, ENTITY_Y);
    }

    /** The recipe's display moobloom with this frame's flowers: position i shows pool entry (i + step) % size. */
    private Moobloom entity(Recipe recipe) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        Moobloom moobloom = entities.get(recipe);
        if (moobloom == null || moobloom.level != level) {
            moobloom = MMEntities.MOOBLOOM.get().create(level);
            if (moobloom == null) return null;
            moobloom.setVariant(recipe.variant());
            entities.put(recipe, moobloom);
        }
        List<MoobloomVariant.Flower> pool = recipe.variant().pool();
        long step = level.getGameTime() / CYCLE_TICKS;
        List<BlockState> states = new ArrayList<>(MoobloomVariant.Slot.COUNT);
        for (int i = 0; i < MoobloomVariant.Slot.COUNT; i++) states.add(pool.get((int) ((i + step) % pool.size())).state());
        if (!states.equals(moobloom.getFlowers())) moobloom.setDisplayFlowers(states);
        return moobloom;
    }

    /**
     * Draws with JEI's PoseStack (InventoryScreen.renderEntityInInventory ignores it), feet at x, y; turns with the
     * mouse. Also used by {@link MoobloomConversionCategory}.
     */
    static void renderEntity(PoseStack pose, Moobloom moobloom, double mouseX, int x, int y) {
        AABB box = moobloom.getBoundingBox();
        float scale = 34.0F / (float) Math.max(box.getXsize(), Math.max(box.getYsize(), box.getZsize()));
        float yaw = (float) (x - mouseX) / 2.0F + 35.0F; // 0 faces the viewer
        moobloom.yBodyRot = yaw;
        moobloom.yBodyRotO = yaw;
        moobloom.setYRot(yaw);
        moobloom.yHeadRot = yaw;
        moobloom.yHeadRotO = yaw;
        moobloom.setXRot(0.0F);

        pose.pushPose();
        pose.translate(x, y, 50.0);
        pose.scale(scale, scale, scale);
        pose.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Lighting.setupForEntityInInventory();
        dispatcher.overrideCameraOrientation(Quaternion.ONE);
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        dispatcher.render(moobloom, 0.0, 0.0, 0.0, 0.0F, 1.0F, pose, buffers, 15728880);
        buffers.endBatch();
        dispatcher.setRenderShadow(true);
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    /** "1.5", "0.3", "1": up to two decimals. */
    static String amount(double n) {
        String s = String.format(Locale.ROOT, "%.2f", n);
        return s.replaceAll("\\.?0+$", "");
    }
}
