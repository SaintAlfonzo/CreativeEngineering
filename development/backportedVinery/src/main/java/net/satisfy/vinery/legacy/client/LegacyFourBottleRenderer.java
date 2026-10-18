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
public class LegacyFourBottleRenderer implements StorageTypeRenderer {
   public void render(StorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
      matrices.translate(-0.13, 0.335, 0.125);
      matrices.scale(0.9F, 0.9F, 0.9F);

      for (int i = 0; i < itemStacks.size(); i++) {
         ItemStack stack = (ItemStack)itemStacks.get(i);
         if (!stack.isEmpty()) {
            Item var8 = stack.getItem();
            if (var8 instanceof BlockItem) {
               BlockItem blockItem = (BlockItem)var8;
               matrices.pushPose();
               if (i == 0) {
                  matrices.translate(-0.35F, 0.0, 0.0);
               } else if (i == 1) {
                  matrices.translate(0.0, -0.33F, 0.0);
               } else if (i == 2) {
                  matrices.translate(-0.7F, -0.33F, 0.0);
               } else {
                  if (i != 3) {
                     matrices.popPose();
                     continue;
                  }

                  matrices.translate(-0.35F, -0.66F, 0.0);
               }

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
