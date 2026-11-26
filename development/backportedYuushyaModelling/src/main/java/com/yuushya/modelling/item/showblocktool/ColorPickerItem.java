// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/ColorPickerItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.client.ColorPickerClient;
import com.yuushya.modelling.item.AbstractToolItem;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.awt.Color;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

public class ColorPickerItem extends AbstractToolItem {
   public ColorPickerItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   // Port: upstream ran this behind `level instanceof ClientLevel` in common code (a dedicated server has no
   // ClientLevel class); the client half lives in client/ColorPickerClient, reached only on the logical client.
   private static void pickColor(Level level, ItemStack handItemStack) {
      if (level.isClientSide) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ColorPickerClient.pickColor(handItemStack));
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
      // Port: canAttackBlock also runs on the server; the clipboard is the client's only.
      if (level.isClientSide && YuushyaDataTags.hasColorData(handItemStack)) {
         int color = YuushyaDataTags.getColorData(handItemStack);
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ColorPickerClient.copyColor(color));
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
