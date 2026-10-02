package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.animation.CameraModelPoses;
import io.github.mortuusars.exposure.client.animation.CameraPoses;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HumanoidModel.class})
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
   @Shadow
   @Final
   public ModelPart leftArm;
   @Shadow
   @Final
   public ModelPart rightArm;
   @Unique
   private float exposure$LeftArmBobbingMultiplier = 1.0F;
   @Unique
   private float exposure$RightArmBobbingMultiplier = 1.0F;

   @Inject(
      method = {"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V",
         ordinal = 0
      )}
   )
   void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
      // Reconstructed from the decompiled upstream method (the decompilers reuse one variable for camera and poses).
      if (!(entity instanceof CameraOperator operator)) {
         this.exposure$LeftArmBobbingMultiplier = 1.0F;
         this.exposure$RightArmBobbingMultiplier = 1.0F;
         return;
      }

      Camera camera = ((CameraOperator)operator).getActiveExposureCamera();
      if (camera != null && camera.getItemStack().getItem() instanceof CameraItem item) {
         CameraPoses poses = CameraModelPoses.get(item);
         HumanoidArm mainArm = Minecraft.getInstance().options.mainHand().get();
         HumanoidArm arm = camera instanceof CameraInHand cameraInHand && cameraInHand.getHand() == InteractionHand.OFF_HAND ? mainArm.getOpposite() : mainArm;
         if (camera instanceof CameraOnStand cameraOnStand) {
            poses.applyStand((HumanoidModel<?>)(Object)this, entity, arm, cameraOnStand.getStand());
            this.exposure$LeftArmBobbingMultiplier = 1.0F;
            this.exposure$RightArmBobbingMultiplier = 1.0F;
            return;
         }

         if (item.isInSelfieMode(camera.getItemStack())) {
            poses.applySelfie((HumanoidModel<?>)(Object)this, entity, arm, false);
            if (arm == HumanoidArm.LEFT) {
               this.exposure$LeftArmBobbingMultiplier = 0.0F;
               this.exposure$RightArmBobbingMultiplier = 1.0F;
            } else {
               this.exposure$LeftArmBobbingMultiplier = 1.0F;
               this.exposure$RightArmBobbingMultiplier = 0.0F;
            }
            return;
         }

         if (Minecrft.player().equals(entity) && PlatformHelper.isModLoaded("realcamera")) {
            return;
         }

         poses.applyHolding((HumanoidModel<?>)(Object)this, entity, arm);
         this.exposure$LeftArmBobbingMultiplier = arm == HumanoidArm.LEFT ? 0.1F : 0.5F;
         this.exposure$RightArmBobbingMultiplier = arm == HumanoidArm.RIGHT ? 0.1F : 0.5F;
         return;
      }

      if (entity instanceof CameraHolder holder) {
         CameraInHand cameraInHand = CameraInHand.find(holder);
         if (cameraInHand != null) {
            cameraInHand.ifPresent((item, stack) -> {
               CameraPoses poses = CameraModelPoses.get(item);
               if (item.isDisassembled(stack)) {
                  HumanoidArm mainArm = Minecraft.getInstance().options.mainHand().get();
                  HumanoidArm arm = cameraInHand.getHand() == InteractionHand.OFF_HAND ? mainArm.getOpposite() : mainArm;
                  poses.applyDisassembled((HumanoidModel<?>)(Object)this, entity, arm);
               }
            });
         }
      }

      this.exposure$LeftArmBobbingMultiplier = 1.0F;
      this.exposure$RightArmBobbingMultiplier = 1.0F;
   }

   @Redirect(
      method = {"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"
      )
   )
   private void removeBobbing(ModelPart modelPart, float ageInTicks, float multiplier) {
      if (this.exposure$LeftArmBobbingMultiplier != 1.0F && modelPart == this.leftArm) {
         multiplier = this.exposure$LeftArmBobbingMultiplier * -1.0F;
      }

      if (this.exposure$RightArmBobbingMultiplier != 1.0F && modelPart == this.rightArm) {
         multiplier = this.exposure$RightArmBobbingMultiplier;
      }

      AnimationUtils.bobModelPart(modelPart, ageInTicks, multiplier);
   }
}
