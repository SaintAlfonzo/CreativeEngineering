package umpaz.brewinandchewin.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * Mouse sway from Tipsy III: the turn is smoothed like the cinematic camera, more strongly at higher levels. Port:
 * 1.19.2's turnPlayer() has no movement-time argument and no named locals, so the frame time is read at HEAD and the
 * smoothing wraps the final LocalPlayer#turn call (same formula as upstream's two ModifyVariables).
 */
@Mixin(MouseHandler.class)
public class TipsyMouseHandlerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private double lastMouseEventTime;
   @Unique
   private final SmoothDouble brewinandchewin$smoothTurnX = new SmoothDouble();
   @Unique
   private final SmoothDouble brewinandchewin$smoothTurnY = new SmoothDouble();
   @Unique
   private double brewinandchewin$frameTime;

   @Inject(method = "turnPlayer", at = @At("HEAD"))
   private void brewinandchewin$captureFrameTime(CallbackInfo ci) {
      this.brewinandchewin$frameTime = Blaze3D.getTime() - this.lastMouseEventTime;
   }

   @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
   private void brewinandchewin$smoothTipsyTurn(LocalPlayer player, double x, double y, Operation<Void> original) {
      if (!player.isSpectator()
         && !this.minecraft.options.smoothCamera
         && player.containerMenu == player.inventoryMenu
         && player.hasEffect(BnCEffects.TIPSY)
         && player.getEffect(BnCEffects.TIPSY).getAmplifier() > 1) {
         double sensitivity = this.minecraft.options.sensitivity().get() * 0.6F + 0.2F;
         double cubed = sensitivity * sensitivity * sensitivity;
         double scale = this.minecraft.options.getCameraType().isFirstPerson() && player.isScoping() ? cubed : cubed * 8.0;
         double tipsyDelta = (double)Math.min(1 + player.getEffect(BnCEffects.TIPSY).getAmplifier(), 10) / 10.0;
         double time = Mth.lerp(tipsyDelta, this.brewinandchewin$frameTime * 10.0, this.brewinandchewin$frameTime) * scale;
         double smoothX = this.brewinandchewin$smoothTurnX.getNewDeltaValue(x, time * Math.max(1.0, tipsyDelta * 2.0));
         double smoothY = this.brewinandchewin$smoothTurnY.getNewDeltaValue(y, time * Math.max(1.2, tipsyDelta * 2.0));
         original.call(player, smoothX, smoothY);
      } else {
         this.brewinandchewin$smoothTurnX.reset();
         this.brewinandchewin$smoothTurnY.reset();
         original.call(player, x, y);
      }
   }
}
