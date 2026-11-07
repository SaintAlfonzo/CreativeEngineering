package com.SaintAlfonzo.creativeequipment.compat;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativeequipment.mixin.ArmorBarRendererAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.api.render.CustomArmorBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * No detailab-compatible "armor overflow" mod exists (checked) - detailab's own
 * {@code ArmorBarRenderer#render} only ever draws one row of 10 armor-point icons (2 points each)
 * and collapses anything past 20 points into tiny 3px tick marks to the left of the bar. This adds
 * real extra rows instead, using whatever textured/recolored icon each armor piece already has
 * registered (via {@link DetailArmorBarCompat} or detailab itself) - stacked above row 0, which is
 * left entirely to detailab.
 *
 * <p>Hooks the exact same event detailab's own {@code PlayerEvents#onArmorRender} does -
 * {@code RenderGuiOverlayEvent.Pre} for {@code VanillaGuiOverlay.ARMOR_LEVEL} - at
 * {@link EventPriority#LOWEST} with {@code receiveCanceled = true}: detailab always cancels that
 * event (to suppress vanilla's own armor icon), and without {@code receiveCanceled} our listener
 * would never run at all once it's canceled.
 *
 * <p>Row contents/order come from {@link ArmorBarRendererAccessor}, a Mixin invoker onto
 * detailab's own private {@code getArmorPoints(Player)} - the exact list its row-0 loop draws
 * from (attribute-modifier bonus points, slot order and all) - rather than re-deriving that
 * algorithm independently. Row 0 itself always shows the LAST (possibly partial) 20-point chunk;
 * this draws the earlier full 20-point chunks as rows above it, topmost row first so that in
 * compact mode each lower row paints over the bottom of the row above it (the "tucked behind"
 * look) - see {@link com.SaintAlfonzo.creativeequipment.CEQConfig.ArmorOverflow#compact}.
 *
 * <p>detailab's own health-row Y anchor ({@code ArmorBarRenderer#render}'s {@code yPos}, based on
 * max health/absorption row count) is private with no accessor, so it's duplicated here from the
 * decompiled 2.6.4 source rather than mixed into - if a future detailab update changes that
 * formula, overflow rows drift a few pixels rather than crashing.
 *
 * <p>The row-0 Y formula and the row drawing loop are adapted from Detail Armor Bar 2.6.4
 * ({@code ArmorBarRenderer#render}), Copyright (c) RedLime, MIT License; see
 * creativecommon-NOTICE-thirdparty.txt.
 */
@Mod.EventBusSubscriber(modid = com.SaintAlfonzo.creativeequipment.CreativeEquipment.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ArmorOverflowRenderer {
    private static final String MODID = "detailab";
    private static final int ROW_SIZE = 20; // armor points per row (10 icons x 2 points)
    private static final int FULL_ROW_SPACING = 9; // icon height, px

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onArmorOverflowRender(RenderGuiOverlayEvent.Pre event) {
        if (!ModList.get().isLoaded(MODID)) return;
        if (event.getOverlay().id() != VanillaGuiOverlay.ARMOR_LEVEL.id()) return;
        if (!com.SaintAlfonzo.creativeequipment.CEQConfig.ARMOR_OVERFLOW.enabled.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        List<Pair<ItemStack, CustomArmorBar>> armorPoints = ArmorBarRendererAccessor.creativecommon$getArmorPoints(player);
        int stackCount = (armorPoints.size() - 1) / ROW_SIZE;
        if (stackCount <= 0) return; // fits in row 0, nothing to do

        int screenWidth = mc.getWindow().getGuiScaledWidth() / 2 - 91;
        int yPos = row0Y(mc, player);
        int spacing = com.SaintAlfonzo.creativeequipment.CEQConfig.ARMOR_OVERFLOW.compact.get() ? com.SaintAlfonzo.creativeequipment.CEQConfig.ARMOR_OVERFLOW.compactRowSpacing.get() : FULL_ROW_SPACING;
        int stackRow = stackCount * ROW_SIZE;
        PoseStack matrices = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        // Topmost row first, so a lower (closer-to-hotbar) row's draw call - later in this loop -
        // paints over the bottom sliver of the row above it when spacing is tucked tight.
        for (int row = stackCount; row >= 1; row--) {
            drawRow(matrices, armorPoints, stackRow - row * ROW_SIZE, screenWidth, yPos - row * spacing);
        }
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
    }

    /** One full 20-point (10-icon) row, starting at armorPoints[offset] - mirrors ArmorBarRenderer#render's own row loop. */
    private static void drawRow(PoseStack matrices, List<Pair<ItemStack, CustomArmorBar>> armorPoints, int offset, int xBase, int y) {
        for (int count = 0; count < 10; count++) {
            int xPos = xBase + count * 8;
            Pair<ItemStack, CustomArmorBar> am1 = armorPoints.get(offset + count * 2);
            Pair<ItemStack, CustomArmorBar> am2 = armorPoints.get(offset + count * 2 + 1);
            if (am1.getSecond() == am2.getSecond()) {
                am1.getSecond().draw(am1.getFirst(), matrices, xPos, y, false, false);
            } else {
                am2.getSecond().draw(am2.getFirst(), matrices, xPos, y, true, true);
                am1.getSecond().draw(am1.getFirst(), matrices, xPos, y, true, false);
            }
        }
    }

    /** Duplicated from ArmorBarRenderer#render (decompiled) - its own yPos/healthRow math is inline, no accessor to invoke. */
    private static int row0Y(Minecraft mc, LocalPlayer player) {
        double playerHealth = Math.ceil(player.getHealth());
        double maxHealth = Math.max(player.getAttributeValue(Attributes.MAX_HEALTH), playerHealth);
        double absorptionHealth = Math.ceil(player.getAbsorptionAmount());
        int healthRow = DetailArmorBar.getConfig().getOptions().toggleCompatibleHeartMod
                ? 1 : (int) Math.ceil((maxHealth + absorptionHealth) / 20.0);
        int screenHeight = mc.getWindow().getGuiScaledHeight() - 39;
        return screenHeight - (healthRow - 1) * Math.max(10 - (healthRow - 2), 3) - 10;
    }
}
