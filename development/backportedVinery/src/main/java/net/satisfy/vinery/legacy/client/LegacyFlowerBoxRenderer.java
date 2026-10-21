package net.satisfy.vinery.legacy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.satisfy.foundation.storage.StorageTypeRenderer;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.satisfy.foundation.render.ClientUtil;

@OnlyIn(Dist.CLIENT)
public class LegacyFlowerBoxRenderer implements StorageTypeRenderer {
   public void render(StorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
      matrices.translate(-0.25, 0.25, 0.75);
      matrices.mulPose(Vector3f.YP.rotationDegrees(90.0F));

      for (int i = 0; i < itemStacks.size(); i++) {
         ItemStack stack = (ItemStack)itemStacks.get(i);
         if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
            matrices.pushPose();
            matrices.translate(0.0, 0.0, (double)(-0.5F * (float)i));
            ClientUtil.renderBlockFromItem(blockItem, matrices, vertexConsumers, entity);
            matrices.popPose();
         }
      }
   }
}
