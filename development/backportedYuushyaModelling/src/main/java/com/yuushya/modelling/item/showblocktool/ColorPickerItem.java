// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/ColorPickerItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.mojang.blaze3d.platform.Window;
import com.yuushya.modelling.item.AbstractToolItem;
import com.yuushya.modelling.utils.ClientMethod;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class ColorPickerItem extends AbstractToolItem {
   public ColorPickerItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   // TODO(A5): side safety. `level instanceof ClientLevel` and ClientMethod.setClipboard (inMainHandLeftClickOnBlock) run
   // in common code; on a dedicated server ClientLevel/Minecraft do not exist (NoClassDefFoundError). Move the client
   // part behind level.isClientSide + a client-only helper class.
   private static void pickColor(Level level, ItemStack handItemStack) {
      if (level instanceof ClientLevel) {
         Window window = Minecraft.getInstance().getWindow();
         int width = window.getScreenWidth();
         int height = window.getScreenHeight();
         int centerX = width / 2;
         int centerY = height / 2;
         ByteBuffer buffer = BufferUtils.createByteBuffer(4);
         GL11.glReadPixels(centerX, centerY, 1, 1, 6408, 5121, buffer);
         int r = 255 - buffer.get(0) & 0xFF;
         int g = 255 - buffer.get(1) & 0xFF;
         int b = 255 - buffer.get(2) & 0xFF;
         int color = 0xFF000000 | r << 16 | g << 8 | b;
         YuushyaDataTags.setColorData(handItemStack, color);
         ClientMethod.setClipboard(String.format("#%06X", color));
      }
   }

   @Override
   public InteractionResult inMainHandRightClickInAir(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      pickColor(level, handItemStack);
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      pickColor(level, handItemStack);
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      if (YuushyaDataTags.hasColorData(handItemStack)) {
         int color = YuushyaDataTags.getColorData(handItemStack);
         ClientMethod.setClipboard(String.format("#%06X", color));
      }

      return InteractionResult.PASS;
   }

   @Override
   public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flags) {
      if (YuushyaDataTags.hasColorData(itemStack)) {
         int color = YuushyaDataTags.getColorData(itemStack);
         String hex = String.format("#%08X", color);
         tooltips.add(Component.literal(hex).withStyle(Style.EMPTY.withColor(color)));
      } else {
         tooltips.add(Component.translatable("item.yuushya.color_picker.none").withStyle(Style.EMPTY.withColor(Color.LIGHT_GRAY.getRGB())));
      }

      super.appendHoverText(itemStack, level, tooltips, flags);
   }
}
