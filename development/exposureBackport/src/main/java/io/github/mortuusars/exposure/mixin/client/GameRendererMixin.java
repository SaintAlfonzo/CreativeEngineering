package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.FovModifier;
import io.github.mortuusars.exposure.client.util.Shader;
import io.github.mortuusars.exposure.event.ClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 500)
public abstract class GameRendererMixin {
   /** After the level (and its post effect) rendered: same spot as upstream's first getMainRenderTarget() call. */
   @Inject(method = "render(FJZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;", ordinal = 0))
   void onRender(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
      Shader.processForGameRenderer();
      ExposureClient.cycles().tick();
   }

   @Inject(method = "resize(II)V", at = @At("HEAD"))
   void onResize(int width, int height, CallbackInfo ci) {
      Shader.resize(width, height);
   }

   /** Upstream @ModifyReturnValue on the regular (second) return. */
   @Inject(method = "getFov(Lnet/minecraft/client/Camera;FZ)D", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
   private void modifyFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
      if (useFOVSetting) {
         cir.setReturnValue(FovModifier.modify(cir.getReturnValue()));
      }
   }

   @Inject(method = "getFov(Lnet/minecraft/client/Camera;FZ)D", at = @At("RETURN"), cancellable = true)
   void getFov(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
      if (useFOVSetting && FovModifier.shouldOverride()) {
         cir.setReturnValue(FovModifier.modify(cir.getReturnValue()));
      }
   }

   @Inject(method = "resetData()V", at = @At("RETURN"))
   void onResetData(CallbackInfo ci) {
      ClientEvents.resetRenderData();
   }
}
