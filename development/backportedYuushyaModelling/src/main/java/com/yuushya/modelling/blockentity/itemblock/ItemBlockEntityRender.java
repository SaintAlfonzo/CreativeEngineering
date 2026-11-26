// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/itemblock/ItemBlockEntityRender.java (MIT, see NOTICE-upstream-MIT.txt) to
// Forge 1.19.2; deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.itemblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yuushya.modelling.blockentity.AbstractTransformBlockEntityRender;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class ItemBlockEntityRender extends AbstractTransformBlockEntityRender<ItemBlockEntity> {
   public ItemBlockEntityRender(Context context) {
      super(context);
   }

   @Override
   protected void renderSpecific(
      ItemBlockEntity blockEntity, float tickDelta, PoseStack matrixStack, MultiBufferSource multiBufferSource, int light, int overlay
   ) {
      TransformItemData transformData = blockEntity.getTransFormDataNow();
      if (transformData.isShown && (blockEntity.showPosAxis() || blockEntity.showRotAxis())) {
         this.renderAxes(blockEntity, matrixStack, multiBufferSource, transformData);
      }

      if (blockEntity.showText()) {
         this.renderTextInfo(blockEntity, transformData, matrixStack, multiBufferSource, light);
      }
   }

   private void renderTextInfo(
      ItemBlockEntity blockEntity, TransformItemData transformData, PoseStack matrixStack, MultiBufferSource multiBufferSource, int light
   ) {
      matrixStack.pushPose();
      Camera camera = this.blockEntityRenderDispatcher.camera;
      renderText(
         this.font,
         Component.translatable("block.yuushya.itemblock.pos_text")
            .append(
               Component.translatable("block.yuushya.itemblock.x", new Object[]{String.format("%05.1f", transformData.pos.x)}).withStyle(ChatFormatting.DARK_RED)
            )
            .append(
               Component.translatable("block.yuushya.itemblock.y", new Object[]{String.format("%05.1f", transformData.pos.y)}).withStyle(ChatFormatting.GREEN)
            )
            .append(
               Component.translatable("block.yuushya.itemblock.z", new Object[]{String.format("%05.1f", transformData.pos.z)}).withStyle(ChatFormatting.BLUE)
            ),
         0.8F,
         matrixStack,
         multiBufferSource,
         light,
         camera
      );
      renderText(
         this.font,
         Component.translatable("block.yuushya.itemblock.rot_text")
            .append(
               Component.translatable("block.yuushya.itemblock.x", new Object[]{String.format("%05.1f", transformData.rot.x())})
                  .withStyle(ChatFormatting.DARK_RED)
            )
            .append(
               Component.translatable("block.yuushya.itemblock.y", new Object[]{String.format("%05.1f", transformData.rot.y())}).withStyle(ChatFormatting.GREEN)
            )
            .append(
               Component.translatable("block.yuushya.itemblock.z", new Object[]{String.format("%05.1f", transformData.rot.z())}).withStyle(ChatFormatting.BLUE)
            ),
         0.55F,
         matrixStack,
         multiBufferSource,
         light,
         camera
      );
      renderText(
         this.font,
         Component.translatable("block.yuushya.itemblock.scale_text", new Object[]{transformData.scales.x()}),
         0.3F,
         matrixStack,
         multiBufferSource,
         light,
         camera
      );
      float high = 0.3F;

      for (TransformItemData everyTransformData : blockEntity.getTransformData()) {
         int slot = blockEntity.getTransformData().indexOf(everyTransformData);
         Style style = blockEntity.getSlot() == slot
            ? Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)
            : (everyTransformData.isShown ? Style.EMPTY.withColor(ChatFormatting.WHITE) : Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
         ItemStack itemStack = everyTransformData.itemStack;
         MutableComponent displayName = itemStack.isEmpty() ? Component.literal("Air") : (MutableComponent)itemStack.getDisplayName();
         Component component = Component.translatable("block.yuushya.itemblock.slot_text", new Object[]{String.format("%2d", slot)})
            .append(displayName.withStyle(style));
         renderText(this.font, component, high -= 0.25F, matrixStack, multiBufferSource, light, camera);
      }

      matrixStack.popPose();
   }
}
