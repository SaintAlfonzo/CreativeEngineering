package umpaz.brewinandchewin.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCGuiUtils;
import umpaz.brewinandchewin.client.utility.BnCHudIcons;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * Numbed hearts: while Tipsy, the health held back by TipsyHeartsAttachment is drawn over the hearts it will take (and
 * flickers in the last 80 ticks). Port notes (1.19.2): renderHearts has the same renderHeart calls as 1.21 (0 container,
 * 1 absorption, 2 blinking, 3 health), but no heart-index local is captured; the index is counted on the container call,
 * which runs once per heart, from the highest down. The sprites are separate textures bound around each draw.
 */
@Mixin(Gui.class)
public class GuiMixin {
   @Unique
   private int brewinandchewin$remainingHealth = 0;
   @Unique
   private float brewinandchewin$numbedAlpha = 1.0F;
   @Unique
   private boolean brewinandchewin$increaseNumbedAlpha = false;
   @Unique
   private boolean brewinandchewin$completedAbsorption = false;
   @Unique
   private Player brewinandchewin$player;
   @Unique
   private int brewinandchewin$displayHealth;
   @Unique
   private int brewinandchewin$absorption;
   @Unique
   private int brewinandchewin$heartIndex;

   @Inject(method = "renderHearts", at = @At("HEAD"))
   private void brewinandchewin$captureHearts(
      PoseStack pose, Player player, int x, int y, int height, int regen, float maxHealth, int health, int displayHealth, int absorption, boolean blinking,
      CallbackInfo ci
   ) {
      this.brewinandchewin$player = player;
      this.brewinandchewin$displayHealth = displayHealth;
      this.brewinandchewin$absorption = absorption;
      this.brewinandchewin$heartIndex = Mth.ceil((double)maxHealth / 2.0) + Mth.ceil((double)absorption / 2.0);
   }

   @WrapOperation(
      method = "renderHearts",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 0)
   )
   private void brewinandchewin$countHeart(Gui instance, PoseStack pose, Gui.HeartType type, int x, int y, int v, boolean blinking, boolean half, Operation<Void> operation) {
      this.brewinandchewin$heartIndex--;
      operation.call(instance, pose, type, x, y, v, blinking, half);
   }

   @Unique
   private void brewinandchewin$flicker(TipsyHeartsAttachment attachment, int heartIndex, int healthStart, float maxStep, boolean requireNoAbsorption) {
      if (BnCConfiguration.CLIENT_CONFIG.get().numbedHeartFlickering()
         && attachment.getNumbedHealth() > 1.0F
         && attachment.getTicksUntilDamage() < 80
         && heartIndex == healthStart
         && (!requireNoAbsorption || this.brewinandchewin$absorption == 0)) {
         if (!Minecraft.getInstance().isPaused()) {
            float increase = Mth.lerp((float)(80 - attachment.getTicksUntilDamage()) / 80.0F, 0.0F, maxStep);
            this.brewinandchewin$numbedAlpha = Mth.clamp(
               this.brewinandchewin$numbedAlpha + (this.brewinandchewin$increaseNumbedAlpha ? increase : -increase), -0.01F, 1.01F
            );
            if (this.brewinandchewin$numbedAlpha < 0.0F) {
               this.brewinandchewin$increaseNumbedAlpha = true;
            }

            if (this.brewinandchewin$numbedAlpha > 1.0F) {
               this.brewinandchewin$increaseNumbedAlpha = false;
            }
         }
      } else if (heartIndex == healthStart) {
         this.brewinandchewin$numbedAlpha = 1.0F;
         this.brewinandchewin$increaseNumbedAlpha = true;
      }
   }

   @Unique
   private void brewinandchewin$drawNumbed(PoseStack pose, int heartIndex, int healthStart, int healthEnd, float renderHealth, int x, int y, boolean hardcore,
      boolean half, boolean absorbing) {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.brewinandchewin$numbedAlpha);
      if (heartIndex == healthStart && half) {
         BnCGuiUtils.blitWhole(pose, BnCGuiUtils.sprite(BnCHudIcons.getTipsyHalfHeartTexture(absorbing, hardcore)), x, y, 9, 9);
         this.brewinandchewin$remainingHealth--;
      } else if ((heartIndex != healthStart || !(renderHealth % 2.0F < 1.0F) || this.brewinandchewin$remainingHealth != 1)
         && (heartIndex != healthEnd || this.brewinandchewin$remainingHealth != 1)) {
         BnCGuiUtils.blitWhole(pose, BnCGuiUtils.sprite(BnCHudIcons.getTipsyFullHeartTexture(absorbing, hardcore)), x, y, 9, 9);
         this.brewinandchewin$remainingHealth -= 2;
      } else {
         BnCGuiUtils.blitWhole(pose, BnCGuiUtils.sprite(BnCHudIcons.getTipsyRightHeartTexture(absorbing, hardcore)), x, y, 9, 9);
         this.brewinandchewin$remainingHealth--;
      }

      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
   }

   @Unique
   private void brewinandchewin$resetNumbed() {
      this.brewinandchewin$remainingHealth = 0;
      this.brewinandchewin$numbedAlpha = 1.0F;
      this.brewinandchewin$increaseNumbedAlpha = true;
   }

   @WrapOperation(
      method = "renderHearts",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 3)
   )
   private void brewinandchewin$renderTipsyHearts(Gui instance, PoseStack pose, Gui.HeartType type, int x, int y, int v, boolean blinking, boolean half,
      Operation<Void> operation) {
      Player player = this.brewinandchewin$player;
      int absorptionAmount = this.brewinandchewin$absorption;
      int heartIndex = this.brewinandchewin$heartIndex;
      if (absorptionAmount <= 0) {
         this.brewinandchewin$completedAbsorption = false;
      }

      TipsyHeartsAttachment attachment = player == null ? null : BrewinAndChewin.getHelper().getTipsyHeartsAttachment(player);
      if (player != null && player.hasEffect(BnCEffects.TIPSY) && attachment != null && !(attachment.getNumbedHealth() <= 0.0F)
         && (absorptionAmount <= 0 || !this.brewinandchewin$completedAbsorption)) {
         float renderHealth = player.getHealth();
         int healthStart = Mth.ceil(renderHealth / 2.0F) - 1;
         int healthEnd = Math.max(Mth.floor((renderHealth - attachment.getNumbedHealth()) / 2.0F), -1);
         if (heartIndex > healthStart) {
            this.brewinandchewin$resetNumbed();
            operation.call(instance, pose, type, x, y, v, blinking, half);
            return;
         }

         if (heartIndex == healthStart && absorptionAmount <= 0) {
            this.brewinandchewin$remainingHealth = Math.min(
               Mth.ceil(attachment.getNumbedHealth()) - ((float)this.brewinandchewin$displayHealth % 1.0F < attachment.getNumbedHealth() % 1.0F ? 1 : 0),
               Mth.ceil((float)this.brewinandchewin$displayHealth)
            );
         }

         operation.call(instance, pose, type, x, y, v, blinking, half);
         if (this.brewinandchewin$remainingHealth > 0) {
            this.brewinandchewin$flicker(attachment, heartIndex, healthStart, 0.06F, true);
            this.brewinandchewin$drawNumbed(pose, heartIndex, healthStart, healthEnd, renderHealth, x, y, v != 0, half, false);
         }
      } else {
         this.brewinandchewin$resetNumbed();
         operation.call(instance, pose, type, x, y, v, blinking, half);
      }
   }

   @WrapOperation(
      method = "renderHearts",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 1)
   )
   private void brewinandchewin$renderAbsorbingTipsyHearts(Gui instance, PoseStack pose, Gui.HeartType type, int x, int y, int v, boolean blinking, boolean half,
      Operation<Void> operation) {
      Player player = this.brewinandchewin$player;
      int heartIndex = this.brewinandchewin$heartIndex;
      TipsyHeartsAttachment attachment = player == null ? null : BrewinAndChewin.getHelper().getTipsyHeartsAttachment(player);
      if (player != null && player.hasEffect(BnCEffects.TIPSY) && attachment != null && !(attachment.getNumbedHealth() <= 0.0F)) {
         float renderHealth = player.getMaxHealth() + player.getAbsorptionAmount();
         int healthStart = Mth.ceil(renderHealth / 2.0F) - 1;
         int healthEnd = Math.max(Mth.floor((renderHealth - attachment.getNumbedHealth() - 1.0F) / 2.0F), -1);
         if (heartIndex > healthStart) {
            this.brewinandchewin$resetNumbed();
            operation.call(instance, pose, type, x, y, v, blinking, half);
            return;
         }

         if (heartIndex == healthStart) {
            this.brewinandchewin$completedAbsorption = false;
            this.brewinandchewin$remainingHealth = Math.min(
               Mth.ceil(attachment.getNumbedHealth()) - ((float)this.brewinandchewin$displayHealth % 1.0F < attachment.getNumbedHealth() % 1.0F ? 1 : 0),
               Mth.ceil((float)this.brewinandchewin$displayHealth)
            );
         } else if (this.brewinandchewin$remainingHealth <= 0) {
            this.brewinandchewin$completedAbsorption = true;
            operation.call(instance, pose, type, x, y, v, blinking, half);
            return;
         }

         operation.call(instance, pose, type, x, y, v, blinking, half);
         this.brewinandchewin$flicker(attachment, heartIndex, healthStart, 0.08F, false);
         this.brewinandchewin$drawNumbed(pose, heartIndex, healthStart, healthEnd, renderHealth, x, y, v != 0, half, true);
      } else {
         this.brewinandchewin$resetNumbed();
         operation.call(instance, pose, type, x, y, v, blinking, half);
      }
   }
}
