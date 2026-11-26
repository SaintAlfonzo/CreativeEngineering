// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 event/GuiOverlayEvent.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.event;

import com.mojang.blaze3d.platform.Window;
import com.yuushya.modelling.registries.ItemRegistry;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.awt.Color;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber({Dist.CLIENT})
public class GuiOverlayEvent {
   @SubscribeEvent
   public static void registerOverlays(Pre event) {
      if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
         GuiGraphics guiGraphics = new GuiGraphics(event.getPoseStack());
         Minecraft minecraft = Minecraft.getInstance();
         ClientLevel world = minecraft.level;
         Options options = minecraft.options;
         Player player = minecraft.player;
         if (player == null || world == null || options.hideGui) {
            return;
         }

         if (player.isSpectator()) {
            return;
         }

         ItemStack mainHandItem = player.getMainHandItem();
         int guiWidth = guiGraphics.guiWidth() / 2;
         int guiHeight = guiGraphics.guiHeight() / 2;
         if (mainHandItem.is((Item)ItemRegistry.COLOR_PICKER_ITEM.get())) {
            Window window = Minecraft.getInstance().getWindow();
            int width = window.getScreenWidth();
            int height = window.getScreenHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            ByteBuffer buffer = BufferUtils.createByteBuffer(4);
            GL11.glReadPixels(centerX, centerY, 1, 1, 6408, 5121, buffer);
            int r = buffer.get(0) & 255;
            int g = buffer.get(1) & 255;
            int b = buffer.get(2) & 255;
            int color = 0xFF000000 | r << 16 | g << 8 | b;
            guiGraphics.fill(guiWidth - 35, guiHeight - 35, guiWidth - 10, guiHeight - 10, Color.LIGHT_GRAY.getRGB());
            guiGraphics.fill(guiWidth - 33, guiHeight - 33, guiWidth - 12, guiHeight - 12, color);
            if (YuushyaDataTags.hasColorData(mainHandItem)) {
               int colorData = YuushyaDataTags.getColorData(mainHandItem);
               guiGraphics.fill(guiWidth + 10, guiHeight - 35, guiWidth + 35, guiHeight - 10, Color.LIGHT_GRAY.getRGB());
               guiGraphics.fill(guiWidth + 12, guiHeight - 33, guiWidth + 33, guiHeight - 12, colorData | 0xFF000000);
            }
         }
      }
   }
}
