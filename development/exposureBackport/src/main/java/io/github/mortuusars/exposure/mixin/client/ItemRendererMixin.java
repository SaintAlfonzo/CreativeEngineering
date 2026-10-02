package io.github.mortuusars.exposure.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.render.model.CameraModel;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 1.19.2: {@code ItemTransforms.TransformType} instead of {@code ItemDisplayContext}. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
   @ModifyVariable(
      method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
      at = @At("HEAD"),
      argsOnly = true
   )
   BakedModel renderItem(BakedModel model, ItemStack stack, ItemTransforms.TransformType displayContext, boolean leftHand,
                         PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
      if (Minecraft.getInstance().level == null) {
         return model;
      } else if (!stack.is(Exposure.Items.CAMERA.get())) {
         return model;
      } else if (displayContext == ItemTransforms.TransformType.GUI) {
         BakedModel guiModel = PlatformHelperClient.getModel(ExposureClient.Models.CAMERA_GUI);
         BakedModel resolved = guiModel.getOverrides().resolve(guiModel, stack, Minecrft.level(), Minecrft.player(), 0);
         return resolved != null ? resolved : guiModel;
      } else {
         return model;
      }
   }

   /** Upstream @ModifyReturnValue. */
   @Inject(method = "getModel(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("RETURN"), cancellable = true)
   private void getModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
      if (stack.is(Exposure.Items.CAMERA.get())) {
         ClientLevel clientLevel = level instanceof ClientLevel lv ? lv : null;
         cir.setReturnValue(CameraModel.modifyCameraModel(stack, clientLevel, entity, seed, cir.getReturnValue()));
      }
   }
}
