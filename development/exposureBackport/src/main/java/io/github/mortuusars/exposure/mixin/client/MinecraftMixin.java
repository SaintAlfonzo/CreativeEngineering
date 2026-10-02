package io.github.mortuusars.exposure.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.capture.task.BackgroundScreenshotCaptureTask;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.event.ClientEvents;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraReleaseC2SP;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
   @Shadow
   @Nullable
   public LocalPlayer player;
   @Shadow
   @Nullable
   public ClientLevel level;
   @Shadow
   @Nullable
   public Screen screen;

   @Inject(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z"), cancellable = true)
   void onStartUseItem(CallbackInfo ci) {
      if (this.player != null && !this.player.isHandsBusy()) {
         if (!(this.screen instanceof ViewfinderCameraControlsScreen)) {
            if (((CameraOperator)this.player).getActiveExposureCamera() instanceof CameraOnStand cameraOnStand && cameraOnStand.isActive()) {
               cameraOnStand.release();
               Packets.sendToServer(ActiveCameraReleaseC2SP.INSTANCE);
               ci.cancel();
            }
         }
      }
   }

   @Inject(method = "startAttack()Z", at = @At("HEAD"), cancellable = true)
   void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
      if (this.player != null && ((CameraOperator)this.player).getActiveExposureCamera() instanceof CameraOnStand) {
         cir.setReturnValue(false);
      }
   }

   /** 1.19.2 setScreen has no Screen.added(); init(...) runs at the same point. */
   @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V"))
   void onSetScreen(Screen screen, CallbackInfo ci) {
      if (this.player != null && CameraClient.isActive() && !(screen instanceof ViewfinderCameraControlsScreen)) {
         CameraClient.deactivate();
      }
   }

   @Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
   void onLevelUnload(ClientLevel newLevel, CallbackInfo ci) {
      if (this.level != null) {
         ClientEvents.levelUnloaded();
      }
   }

   /** 1.19.2: disconnecting goes through clearLevel(Screen), which resets the game renderer. */
   @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V", shift = At.Shift.AFTER))
   void disconnect(Screen nextScreen, CallbackInfo ci) {
      ClientEvents.disconnect();
   }

   /** Upstream @ModifyReturnValue. */
   @Inject(method = "getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;", at = @At("RETURN"), cancellable = true)
   void onGetMainRenderTarget(CallbackInfoReturnable<RenderTarget> cir) {
      if (BackgroundScreenshotCaptureTask.isCapturing()) {
         cir.setReturnValue(BackgroundScreenshotCaptureTask.getRenderTarget());
      }
   }
}
