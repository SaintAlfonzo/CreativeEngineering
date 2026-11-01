package umpaz.brewinandchewin.forge.client.gui;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCGuiUtils;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.common.registry.ModEffects;

/**
 * Port of BnCHUDOverlays onto 1.19.2 GUI overlays: the Intoxication food bar (replaces vanilla's and Farmer's Delight's
 * nourishment bar while Intoxicated) and the orange Tipsy screen tint (shown when Distortion Effects is below 100%).
 */
public class BnCHUDOverlays {
   private static final ResourceLocation NOURISHMENT_ICONS_TEXTURE = new ResourceLocation("farmersdelight", "textures/gui/fd_icons.png");
   public static final ResourceLocation FOOD_EMPTY_INTOXICATION_TEXTURE = BnCGuiUtils.sprite(BrewinAndChewin.asResource("hud/food_empty_intoxication"));
   public static final ResourceLocation FOOD_HALF_INTOXICATION_TEXTURE = BnCGuiUtils.sprite(BrewinAndChewin.asResource("hud/food_half_intoxication"));
   public static final ResourceLocation FOOD_FULL_INTOXICATION_TEXTURE = BnCGuiUtils.sprite(BrewinAndChewin.asResource("hud/food_full_intoxication"));
   private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
   private static float tipsyTransparencyModifier = 0.0F;

   public static void init(IEventBus modBus) {
      modBus.addListener(EventPriority.LOW, BnCHUDOverlays::register);
      MinecraftForge.EVENT_BUS.addListener(BnCHUDOverlays::onRenderGuiOverlayPre);
   }

   private static void register(RegisterGuiOverlaysEvent event) {
      event.registerAbove(VanillaGuiOverlay.VIGNETTE.id(), "tipsy", new TipsyOverlay());
      event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "intoxication", new IntoxicationOverlay());
   }

   private static boolean shouldRenderOverlay(Minecraft minecraft) {
      return !minecraft.options.hideGui && minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
   }

   /** Hides the vanilla food bar while the Intoxication bar draws in its place. FD's nourishment bar draws in Post, see below. */
   private static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null
         && event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())
         && BnCConfiguration.CLIENT_CONFIG.get().intoxicationFoodOverlay()
         && mc.player.hasEffect(BnCEffects.INTOXICATION)) {
         event.setCanceled(true);
      }
   }

   public static void renderTipsyOverlay(PoseStack pose, int width, int height, float scalar) {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ONE, DestFactor.ONE);
      RenderSystem.setShaderColor(scalar, 0.55F * scalar, 0.08F * scalar, 1.0F);
      RenderSystem.setShaderTexture(0, NAUSEA_LOCATION);
      GuiComponent.blit(pose, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
   }

   public static void drawIntoxicationOverlay(Player player, Minecraft minecraft, PoseStack pose, int right, int top) {
      int ticks = minecraft.gui.getGuiTicks();
      RenderSystem.enableBlend();
      boolean nourished = player.hasEffect(ModEffects.NOURISHMENT.get());

      for (int i = 0; i < 10; i++) {
         int x = right - i * 8 - 9 + (int)(Mth.cos((float)(ticks + i * 2) * 0.2F) * 2.0F);
         int y = top + (int)(Mth.sin((float)(ticks + i * 2) * 0.25F) * 2.0F);
         float effectiveHungerOfBar = (float)player.getFoodData().getFoodLevel() / 2.0F - (float)i;
         if (nourished) {
            boolean healing = player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)
               && player.isHurt()
               && player.getFoodData().getFoodLevel() >= 18;
            int naturalHealingOffset = healing ? 18 : 0;
            RenderSystem.setShaderTexture(0, NOURISHMENT_ICONS_TEXTURE);
            GuiComponent.blit(pose, x, y, 0, 0.0F, 0.0F, 9, 9, 256, 256);
            if (effectiveHungerOfBar >= 1.0F) {
               GuiComponent.blit(pose, x, y, 0, (float)(18 + naturalHealingOffset), 0.0F, 9, 9, 256, 256);
            } else if (effectiveHungerOfBar >= 0.5F) {
               GuiComponent.blit(pose, x, y, 0, (float)(9 + naturalHealingOffset), 0.0F, 9, 9, 256, 256);
            }
         } else {
            BnCGuiUtils.blitWhole(pose, FOOD_EMPTY_INTOXICATION_TEXTURE, x, y, 9, 9);
            if (effectiveHungerOfBar >= 1.0F) {
               BnCGuiUtils.blitWhole(pose, FOOD_FULL_INTOXICATION_TEXTURE, x, y, 9, 9);
            } else if (effectiveHungerOfBar >= 0.5F) {
               BnCGuiUtils.blitWhole(pose, FOOD_HALF_INTOXICATION_TEXTURE, x, y, 9, 9);
            }
         }
      }

      RenderSystem.disableBlend();
      RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
   }

   public static class IntoxicationOverlay implements IGuiOverlay {
      @Override
      public void render(ForgeGui gui, PoseStack pose, float partialTick, int width, int height) {
         Minecraft minecraft = Minecraft.getInstance();
         Player player = minecraft.player;
         if (BnCConfiguration.CLIENT_CONFIG.get().intoxicationFoodOverlay()
            && shouldRenderOverlay(minecraft)
            && player != null
            && player.hasEffect(BnCEffects.INTOXICATION)
            && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false);
            int top = height - gui.rightHeight;
            int right = width / 2 + 91;
            drawIntoxicationOverlay(player, minecraft, pose, right, top);
            gui.rightHeight += 10;
         }
      }
   }

   public static class TipsyOverlay implements IGuiOverlay {
      @Override
      public void render(ForgeGui gui, PoseStack pose, float partialTick, int width, int height) {
         Minecraft mc = Minecraft.getInstance();
         if (shouldRenderOverlay(mc) && mc.player != null && !mc.player.hasEffect(MobEffects.CONFUSION) && mc.player.hasEffect(BnCEffects.TIPSY)) {
            MobEffectInstance effect = mc.player.getEffect(BnCEffects.TIPSY);
            float distortionScale = mc.options.screenEffectScale().get().floatValue();
            float tipsyScale = Math.min((float)(1 + effect.getAmplifier()) / 10.0F * 0.4F, 0.4F);
            if (distortionScale < 1.0F && tipsyScale > 0.0F) {
               renderTipsyOverlay(pose, width, height, (1.0F - distortionScale) * tipsyScale * tipsyTransparencyModifier);
               float partialTickModifier = mc.getDeltaFrameTime() * (effect.getDuration() <= 60 ? -0.006F : 0.007F);
               tipsyTransparencyModifier = Mth.clamp(tipsyTransparencyModifier + partialTickModifier, 0.0F, 1.0F);
            } else {
               tipsyTransparencyModifier = 0.0F;
            }
         } else {
            tipsyTransparencyModifier = 0.0F;
         }
      }
   }
}
