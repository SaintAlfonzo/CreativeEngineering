package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * The positional half of the Tipsy camera (alcohol.md 10.4). Upstream turns the level pose around a point half a block
 * off the eye (0.5, 0.5, 0.5 in view space) and adds a slow sub-block slide, so the view drifts as it spins. The spin
 * itself is Forge's camera-angle event (BnCClientForgeEvents); this moves the camera by the translation upstream's
 * pose ends up with: R1 R2 (d - p) + p, R1 the yaw turn, R2 the turn about (1, 0, 1), d the slide, p the pivot.
 * Same clock and strength as the spin (player ticks, Tipsy level capped at 11, Distortion Effects scale).
 */
@Mixin(Camera.class)
public abstract class TipsyCameraDriftMixin {

   @Shadow
   protected abstract void move(double forward, double up, double left);

   @Inject(method = "setup", at = @At("TAIL"))
   private void brewinandchewin$tipsyDrift(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (player == null || entity != player || !player.hasEffect(BnCEffects.TIPSY)) {
         return;
      }
      float distortion = minecraft.options.screenEffectScale().get().floatValue();
      if (distortion <= 0.0F) {
         return;
      }
      float ticks = (float)player.tickCount + partialTick;
      float strength = (float)Math.min(player.getEffect(BnCEffects.TIPSY).getAmplifier(), 11) * distortion;

      double[] v = {
         Mth.sin(ticks * 0.00253F) * (strength / 100.0F) - 0.5,
         -0.5,
         Mth.cos(ticks * 0.00784F) * (strength / 100.0F) - 0.5
      };
      double s = Math.sqrt(0.5);
      v = rotate(v, s, 0, s, Math.toRadians(Mth.sin(27.0F + ticks * 0.0132F) * strength));
      v = rotate(v, 0, 1, 0, Math.toRadians(Mth.cos(3.0F + ticks * 0.0295F) * strength));
      double x = v[0] + 0.5, y = v[1] + 0.5, z = v[2] + 0.5;
      // the world moved by (x, y, z) in view space = the camera moved by its negative (view looks down -z)
      this.move(z, -y, x);
   }

   /** Rodrigues: v turned by `angle` about the unit axis (ax, ay, az). */
   private static double[] rotate(double[] v, double ax, double ay, double az, double angle) {
      double cos = Math.cos(angle), sin = Math.sin(angle);
      double dot = ax * v[0] + ay * v[1] + az * v[2];
      return new double[]{
         v[0] * cos + (ay * v[2] - az * v[1]) * sin + ax * dot * (1 - cos),
         v[1] * cos + (az * v[0] - ax * v[2]) * sin + ay * dot * (1 - cos),
         v[2] * cos + (ax * v[1] - ay * v[0]) * sin + az * dot * (1 - cos)
      };
   }
}
