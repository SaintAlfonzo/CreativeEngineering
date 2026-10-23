package com.SaintAlfonzo.creativeengineering.compat.jei.world;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * One world-interaction tab ({@link WorldTab}): inputs | action over the arrow -> outputs, with note lines under.
 * Every tab uses this class; only the type, title and icon differ.
 *
 * <pre>
 *            [action]
 * [in][in][in] ---->  [out][out][out]
 *                     [out][out][out]
 * note line 1
 * note line 2
 * </pre>
 * An output under 100% shows its chance under the slot (single output row) and in its tooltip. Effect and "Nothing"
 * slots are drawn here and get their tooltip from {@link #getTooltipStrings}; so do notes cut off after 3 lines.
 */
@ParametersAreNonnullByDefault
public class WorldInteractionCategory implements IRecipeCategory<WorldRow> {

    /** The same arrow as Feast Plating. */
    private static final ResourceLocation GUI = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
    private static final int WIDTH = 160;
    private static final int ROW_Y = 19;
    private static final int ACTION_X = 62;
    private static final int ARROW_X = 58;
    private static final int OUTPUT_X = 88;
    private static final int NOTES_Y = 50;
    private static final int NOTE_LINES = 3;
    private static final int TEXT = 0xFF404040;

    private final WorldTab tab;
    private final RecipeType<WorldRow> type;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public WorldInteractionCategory(IGuiHelper helper, WorldTab tab, ItemStack iconStack) {
        this.tab = tab;
        this.type = type(tab);
        background = helper.createBlankDrawable(WIDTH, NOTES_Y + NOTE_LINES * 10);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
        slot = helper.getSlotDrawable();
        arrow = helper.createDrawable(GUI, 82, 128, 24, 17);
    }

    public static RecipeType<WorldRow> type(WorldTab tab) {
        return RecipeType.create(CreativeEngineering.MODID, "world/" + tab.id, WorldRow.class);
    }

    @Override
    public RecipeType<WorldRow> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(tab.titleKey());
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
    public void setRecipe(IRecipeLayoutBuilder builder, WorldRow row, IFocusGroup focuses) {
        IRecipeSlotBuilder first = null;
        for (int n = 0; n < row.inputs().size(); n++) {
            IRecipeSlotBuilder input = slot(builder, RecipeIngredientRole.INPUT, n * 18 + 1, ROW_Y + 1, row.inputs().get(n));
            if (n == 0) first = input;
        }
        if (row.action() != null) {
            slot(builder, RecipeIngredientRole.CATALYST, ACTION_X + 1, 1, row.action());
        }
        IRecipeSlotBuilder firstOut = null;
        for (int n = 0; n < row.outputs().size(); n++) {
            IRecipeSlotBuilder output = slot(builder, RecipeIngredientRole.OUTPUT, outputX(n) + 1, outputY(row, n) + 1, row.outputs().get(n));
            if (n == 0) firstOut = output;
        }
        if (row.linked() && first != null && firstOut != null
                && size(row.inputs().get(0)) == size(row.outputs().get(0)) && size(row.inputs().get(0)) > 1) {
            builder.createFocusLink(first, firstOut);
        }
    }

    private static int size(WorldRow.Ing ing) {
        return ing.items().isEmpty() ? ing.fluids().size() : ing.items().size();
    }

    /** Null for a drawn-only slot. */
    private static IRecipeSlotBuilder slot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, WorldRow.Ing ing) {
        if (ing.drawnOnly()) return null;
        IRecipeSlotBuilder slot = builder.addSlot(role, x, y);
        if (!ing.items().isEmpty()) {
            slot.addItemStacks(ing.items());
        } else {
            slot.addIngredients(ForgeTypes.FLUID_STACK, ing.fluids());
            slot.setFluidRenderer(1000, false, 16, 16);
        }
        if (!ing.tooltip().isEmpty()) {
            slot.addTooltipCallback((view, tooltip) -> tooltip.addAll(ing.tooltip()));
        }
        return slot;
    }

    private static int outputX(int n) {
        return OUTPUT_X + (n % 3) * 18;
    }

    private static int outputY(WorldRow row, int n) {
        if (row.outputs().size() <= 3) return ROW_Y;
        return n < 3 ? ROW_Y - 9 : ROW_Y + 9;
    }

    @Override
    public void draw(WorldRow row, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        for (int n = 0; n < row.inputs().size(); n++) slot.draw(poseStack, n * 18, ROW_Y);
        if (row.action() != null) slot.draw(poseStack, ACTION_X, 0);
        arrow.draw(poseStack, ARROW_X, ROW_Y);
        for (int n = 0; n < row.outputs().size(); n++) slot.draw(poseStack, outputX(n), outputY(row, n));
        forEachSlot(row, (ing, x, y) -> {
            if (ing.effect() == null) return;
            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(ing.effect());
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, sprite.atlas().location());
            GuiComponent.blit(poseStack, x + 1, y + 1, 0, 16, 16, sprite);
        });

        Font font = Minecraft.getInstance().font;
        if (row.outputs().size() <= 3) {
            for (int n = 0; n < row.outputs().size(); n++) {
                float chance = row.outputs().get(n).chance();
                if (chance >= 1f) continue;
                String text = WorldRow.percent(chance);
                poseStack.pushPose();
                poseStack.translate(outputX(n) + 9 - font.width(text) * 0.375f, ROW_Y + 19, 0);
                poseStack.scale(0.75f, 0.75f, 1f);
                font.draw(poseStack, text, 0, 0, TEXT);
                poseStack.popPose();
            }
        }
        int line = 0;
        for (String note : row.notes()) {
            List<FormattedCharSequence> wrapped = font.split(FormattedText.of(note), WIDTH);
            for (FormattedCharSequence part : wrapped) {
                if (line == NOTE_LINES) return;
                font.draw(poseStack, part, 0, NOTES_Y + line * 10, TEXT);
                line++;
            }
        }
    }

    @Override
    public List<Component> getTooltipStrings(WorldRow row, IRecipeSlotsView slots, double mouseX, double mouseY) {
        List<Component> out = new ArrayList<>();
        forEachSlot(row, (ing, x, y) -> {
            if (ing.drawnOnly() && mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) out.addAll(ing.tooltip());
        });
        if (out.isEmpty() && mouseY >= NOTES_Y && notesCut(row)) {
            for (String note : row.notes()) out.add(Component.literal(note));
        }
        return out;
    }

    private static boolean notesCut(WorldRow row) {
        Font font = Minecraft.getInstance().font;
        int lines = 0;
        for (String note : row.notes()) lines += font.split(FormattedText.of(note), WIDTH).size();
        return lines > NOTE_LINES;
    }

    private interface SlotVisitor {
        void visit(WorldRow.Ing ing, int x, int y);
    }

    /** Every slot with the top-left corner of its background. */
    private static void forEachSlot(WorldRow row, SlotVisitor visitor) {
        for (int n = 0; n < row.inputs().size(); n++) visitor.visit(row.inputs().get(n), n * 18, ROW_Y);
        if (row.action() != null) visitor.visit(row.action(), ACTION_X, 0);
        for (int n = 0; n < row.outputs().size(); n++) visitor.visit(row.outputs().get(n), outputX(n), outputY(row, n));
    }
}
