package net.satisfy.vinery.legacy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.satisfy.foundation.storage.StorageTypeRenderer;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.satisfy.vinery.core.block.WineBottleBlock;
import net.satisfy.foundation.render.ClientUtil;

@OnlyIn(Dist.CLIENT)
public class LegacyNineBottleRenderer implements StorageTypeRenderer {
   public void render(StorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
      matrices.translate(-0.13, 0.335, 0.125);
      matrices.scale(0.9F, 0.9F, 0.9F);

      for (int i = 0; i < itemStacks.size(); i++) {
         ItemStack stack = (ItemStack)itemStacks.get(i);
         if (!stack.isEmpty()) {
            Item line = stack.getItem();
            if (line instanceof BlockItem) {
               BlockItem blockItem = (BlockItem)line;
               matrices.pushPose();
               int linex = i >= 6 ? 3 : (i >= 3 ? 2 : 1);
               float x;
               float y;
               if (linex == 1) {
                  x = -0.35F * (float)i;
                  y = 0.0F;
               } else if (linex == 2) {
                  x = -0.35F * (float)(i - 3);
                  y = -0.33F;
               } else {
                  x = -0.35F * (float)(i - 6);
                  y = -0.66F;
               }

               matrices.translate((double)x, (double)y, 0.0);
               matrices.mulPose(Vector3f.XN.rotationDegrees(90.0F));
               ClientUtil.renderBlock(
                  (BlockState)blockItem.getBlock().defaultBlockState().setValue(WineBottleBlock.FAKE_MODEL, false), matrices, vertexConsumers, entity
               );
               matrices.popPose();
            }
         }
      }
   }
}
