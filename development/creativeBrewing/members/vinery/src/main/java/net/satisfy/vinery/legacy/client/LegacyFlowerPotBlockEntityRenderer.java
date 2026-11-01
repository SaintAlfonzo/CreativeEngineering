package net.satisfy.vinery.legacy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.satisfy.vinery.legacy.block.LegacyFlowerPotBlock;
import net.satisfy.vinery.legacy.entity.LegacyFlowerPotBlockEntity;

@OnlyIn(Dist.CLIENT)
public class LegacyFlowerPotBlockEntityRenderer implements BlockEntityRenderer<LegacyFlowerPotBlockEntity> {
   public LegacyFlowerPotBlockEntityRenderer(Context ctx) {
   }

   public void render(LegacyFlowerPotBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
      if (entity.hasLevel()) {
         BlockState selfState = entity.getBlockState();
         if (selfState.getBlock() instanceof LegacyFlowerPotBlock) {
            Item item = entity.getFlower();
            matrices.pushPose();
            if (item instanceof BlockItem blockItem) {
               matrices.translate(0.0, 0.4F, 0.0);
               PlantRenderer.render(blockItem, matrices, vertexConsumers, entity);
            }
            matrices.popPose();
         }
      }
   }
}
