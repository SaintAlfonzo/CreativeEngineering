// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/showblock/ShowBlockEntityRender.java (MIT, see NOTICE-upstream-MIT.txt) to
// Forge 1.19.2; deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.showblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuushya.modelling.blockentity.AbstractTransformBlockEntityRender;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.utils.YuushyaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ShowBlockEntityRender extends AbstractTransformBlockEntityRender<ShowBlockEntity> {
   public ShowBlockEntityRender(Context context) {
      super(context);
   }

   @Override
   protected void renderSpecific(
      ShowBlockEntity blockEntity, float tickDelta, PoseStack matrixStack, MultiBufferSource multiBufferSource, int light, int overlay
   ) {
      TransformBlockData transformData = blockEntity.getTransFormDataNow();
      if (transformData.isShown && (blockEntity.showPosAxis() || blockEntity.showRotAxis())) {
         this.renderAxes(blockEntity, matrixStack, multiBufferSource, transformData);
      }

      if (blockEntity.showText()) {
         this.renderTextInfo(blockEntity, transformData, matrixStack, multiBufferSource, light);
      }
   }

   private void renderTextInfo(
      ShowBlockEntity blockEntity, TransformBlockData transformData, PoseStack matrixStack, MultiBufferSource multiBufferSource, int light
   ) {
      matrixStack.pushPose();
      Camera camera = this.blockEntityRenderDispatcher.camera;
      renderText(
         this.font,
         Component.translatable("block.yuushya.showblock.pos_text")
            .append(
               Component.translatable("block.yuushya.showblock.x", new Object[]{String.format("%05.1f", transformData.pos.x)}).withStyle(ChatFormatting.DARK_RED)
            )
            .append(
               Component.translatable("block.yuushya.showblock.y", new Object[]{String.format("%05.1f", transformData.pos.y)}).withStyle(ChatFormatting.GREEN)
            )
            .append(
               Component.translatable("block.yuushya.showblock.z", new Object[]{String.format("%05.1f", transformData.pos.z)}).withStyle(ChatFormatting.BLUE)
            ),
         0.8F,
         matrixStack,
         multiBufferSource,
         light,
         camera
      );
      renderText(
         this.font,
         Component.translatable("block.yuushya.showblock.rot_text")
            .append(
               Component.translatable("block.yuushya.showblock.x", new Object[]{String.format("%05.1f", transformData.rot.x())})
                  .withStyle(ChatFormatting.DARK_RED)
            )
            .append(
               Component.translatable("block.yuushya.showblock.y", new Object[]{String.format("%05.1f", transformData.rot.y())}).withStyle(ChatFormatting.GREEN)
            )
            .append(
               Component.translatable("block.yuushya.showblock.z", new Object[]{String.format("%05.1f", transformData.rot.z())}).withStyle(ChatFormatting.BLUE)
            ),
         0.55F,
         matrixStack,
         multiBufferSource,
         light,
         camera
      );
      renderText(
         this.font,
         Component.translatable("block.yuushya.showblock.scale_text", new Object[]{transformData.scales.x()}),
         0.3F,
         matrixStack,
         multiBufferSource,
         light,
         camera
      );
      float high = 0.3F;

      for (TransformBlockData everyTransformData : blockEntity.getTransformData()) {
         int slot = blockEntity.getTransformData().indexOf(everyTransformData);
         Style style = blockEntity.getSlot() == slot
            ? Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)
            : (everyTransformData.isShown ? Style.EMPTY.withColor(ChatFormatting.WHITE) : Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
         Block block = everyTransformData.blockState.getBlock();
         Item item = block.asItem();
         MutableComponent displayName = item == Items.AIR ? block.getName() : (MutableComponent)item.getName(item.getDefaultInstance());
         Component component = Component.translatable("block.yuushya.showblock.slot_text", new Object[]{String.format("%2d", slot)})
            .append(displayName.append(Component.literal(YuushyaUtils.getBlockStateProperties(everyTransformData.blockState))).withStyle(style));
         renderText(this.font, component, high -= 0.25F, matrixStack, multiBufferSource, light, camera);
      }

      matrixStack.popPose();
   }
}
