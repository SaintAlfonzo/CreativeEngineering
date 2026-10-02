package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.Rect2f;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.BrokenInterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.backport.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ViewfinderOverlay {
   public static final ResourceLocation VIEWFINDER_TEXTURE = Exposure.resource("textures/gui/viewfinder/viewfinder.png");
   public static final ResourceLocation NO_FILM_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/no_film.png");
   public static final ResourceLocation REMAINING_FRAMES_ICON_TEXTURE = Exposure.resource("textures/gui/viewfinder/remaining_frames.png");
   public static final ResourceLocation BSOD_SAD_FACE_TEXTURE = Exposure.resource("textures/gui/viewfinder/bsod_sad_face.png");
   public static final ResourceLocation BSOD_QR_CODE_TEXTURE = Exposure.resource("textures/gui/viewfinder/bsod_qr_code.png");
   protected final LocalPlayer player;
   protected final Camera camera;
   protected final Viewfinder viewfinder;
   protected final Rect2f opening;
   protected final Animation scaleAnimation;
   protected final float initialScale;
   protected int backgroundColor;
   protected float scale;
   protected float bobX = 0.0F;
   protected float bobY = 0.0F;
   protected float attackAnim = 0.0F;
   protected float xRot;
   protected float yRot;
   protected float xRot0;
   protected float yRot0;
   protected boolean forceDrawShutterOnNextFrame;
   protected long forceDrawShutterUntil = -1L;

   public ViewfinderOverlay(Camera camera, Viewfinder viewfinder) {
      this.player = Minecrft.player();
      this.camera = camera;
      this.viewfinder = viewfinder;
      this.backgroundColor = Config.getColor(Config.Client.VIEWFINDER_BACKGROUND_COLOR);
      this.opening = new Rect2f(0.0F, 0.0F, 0.0F, 0.0F);
      this.recalculateOpening();
      this.scaleAnimation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
      this.initialScale = 0.5F;
      this.scale = 0.5F;
      this.xRot = Minecrft.get().gameRenderer.getMainCamera().getXRot();
      this.yRot = Minecrft.get().gameRenderer.getMainCamera().getYRot();
      this.xRot0 = this.xRot;
      this.yRot0 = this.yRot;
   }

   public Rect2f getOpening() {
      return this.opening;
   }

   public float getScale() {
      return this.scale;
   }

   public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
      this.recalculateOpening();
      this.scale = Mth.lerp((float)this.scaleAnimation.getValue(), this.initialScale, 1.0F);
      if (this.viewfinder.isLookingThrough() && !Minecrft.options().hideGui && !this.camera.isEmpty()) {
         int width = Minecrft.get().getWindow().getGuiScaledWidth();
         int height = Minecrft.get().getWindow().getGuiScaledHeight();
         guiGraphics.pose().pushPose();
         guiGraphics.pose().translate((float)width / 2.0F, (float)height / 2.0F, 0.0F);
         guiGraphics.pose().scale(this.scale, this.scale, this.scale);
         if ((Boolean)Minecrft.options().bobView().get()) {
            this.bobView(guiGraphics.pose(), deltaTracker);
         }

         this.applyAttackAnimation(guiGraphics.pose(), deltaTracker);
         this.applyMovementDelay(guiGraphics.pose(), deltaTracker);
         guiGraphics.pose().translate((float)(-width) / 2.0F, (float)(-height) / 2.0F, 0.0F);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         GuiUtil.drawRect(guiGraphics, this.opening.x, this.opening.y, -9999.0F, this.opening.height, this.backgroundColor);
         GuiUtil.drawRect(guiGraphics, this.opening.x + this.opening.width, this.opening.y, 9999.0F, this.opening.height, this.backgroundColor);
         GuiUtil.drawRect(guiGraphics, -4999.0F, this.opening.y, 9999.0F, -9999.0F, this.backgroundColor);
         GuiUtil.drawRect(guiGraphics, -4999.0F, this.opening.y + this.opening.height, 9999.0F, 9999.0F, this.backgroundColor);
         boolean drawGuide = true;
         StoredItemStack filter = Attachment.FILTER.get(this.camera.getItemStack());
         if (filter.getForReading().getItem() instanceof BrokenInterplanarProjectorItem brokenInterplanarProjector) {
            drawGuide = false;
            this.renderBSOD(guiGraphics, brokenInterplanarProjector, filter.getForReading());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         this.drawShutter(guiGraphics);
         this.drawViewfinderTexture(guiGraphics);
         if (drawGuide) {
            ResourceLocation guideTexture = CameraSettings.COMPOSITION_GUIDE.getOrDefault(this.camera.getItemStack()).overlayTextureLocation();
            GuiUtil.blit(guideTexture, guiGraphics.pose(), this.opening, 0, 0, (int)this.opening.width, (int)this.opening.height, 0.0F);
         }

         if (!(Minecrft.get().screen instanceof ViewfinderCameraControlsScreen)) {
            this.renderStatusIcons(guiGraphics.pose(), this.camera.getItemStack());
         }

         guiGraphics.pose().popPose();
         RenderSystem.disableDepthTest();
      }
   }

   protected void drawViewfinderTexture(GuiGraphics guiGraphics) {
      GuiUtil.blit(VIEWFINDER_TEXTURE, guiGraphics.pose(), this.opening, 0, 0, (int)this.opening.width, (int)this.opening.height, 0.0F);
   }

   protected void drawShutter(GuiGraphics guiGraphics) {
      if (this.camera.isShutterOpen() || this.forceDrawShutterOnNextFrame || this.forceDrawShutterUntil - System.currentTimeMillis() > 0L) {
         GuiUtil.drawRect(guiGraphics, this.opening, -98624229);
         this.forceDrawShutterOnNextFrame = false;
      }
   }

   public void startDrawingShutter() {
      this.forceDrawShutterOnNextFrame = true;
      this.forceDrawShutterUntil = UnixTimestamp.Milliseconds.now() + 100L;
   }

   protected void renderBSOD(GuiGraphics guiGraphics, BrokenInterplanarProjectorItem item, ItemStack stack) {
      Font font = Minecrft.get().font;
      int yCenter = (int)(this.opening.y + this.opening.height / 2.0F);
      int margin = 9;
      int x = (int)(this.opening.x + this.opening.width * 0.125F);
      int y = yCenter;
      int sadFaceSize = 9 * 5;
      guiGraphics.blit(BSOD_SAD_FACE_TEXTURE, x, yCenter - sadFaceSize - margin, 0.0F, 0.0F, sadFaceSize, sadFaceSize, sadFaceSize, sadFaceSize);
      MutableComponent message = Component.translatable("item.exposure.broken_interplanar_projector.viewfinder.message");

      for (FormattedCharSequence line : font.split(message, (int)(this.opening.width * 0.75F))) {
         guiGraphics.drawString(font, line, x, y, -1, false);
         y += 9;
      }

      y += margin;
      int qrCodeTextureSize = 41;

      int qrCodeSize = switch ((int)Minecrft.get().getWindow().getGuiScale()) {
         case 1 -> qrCodeTextureSize * 3;
         case 2 -> qrCodeTextureSize * 2;
         default -> qrCodeTextureSize;
      };
      guiGraphics.blit(BSOD_QR_CODE_TEXTURE, x, y, 0.0F, 0.0F, qrCodeSize, qrCodeSize, qrCodeSize, qrCodeSize);
      MutableComponent errorCode = Component.translatable("item.exposure.broken_interplanar_projector.viewfinder.error_code");
      guiGraphics.drawString(font, errorCode, x + qrCodeSize + margin, y, -1, false);
      y += 9;
      String code = item.getErrorCode(stack);
      guiGraphics.drawString(font, code, x + qrCodeSize + margin, y, -1, false);
   }

   public void bobView(PoseStack poseStack, DeltaTracker deltaTracker) {
      if (Minecrft.get().getCameraEntity() instanceof Player pl) {
         float walkDist = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), pl.walkDistO, pl.walkDist);
         float strength = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), pl.oBob, pl.bob);
         float x = Mth.sin(walkDist * (float) Math.PI) * strength;
         float y = Math.abs(Mth.cos(walkDist * (float) Math.PI) * strength);
         float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1.0F);
         this.bobX = Mth.lerp(delta, this.bobX, x);
         this.bobY = Mth.lerp(delta, this.bobY, y);
         double guiScale = Minecrft.get().getWindow().getGuiScale();
         poseStack.translate((double)(this.bobX * 100.0F) / guiScale, (double)(this.bobY * 200.0F) / guiScale, 0.0);
         float scale = this.scale - this.bobY * 0.25F;
         poseStack.scale(scale, scale, scale);
      }
   }

   public void applyAttackAnimation(PoseStack poseStack, DeltaTracker deltaTracker) {
      float attack = this.player.attackAnim;
      if (attack > 0.1F) {
         attack = 1.0F - attack;
      }

      float delta = Math.min(deltaTracker.getGameTimeDeltaTicks(), 1.0F);
      this.attackAnim = Mth.lerp(delta, this.attackAnim, attack);
      poseStack.scale(1.0F - this.attackAnim * 0.1F, 1.0F - this.attackAnim * 0.2F, 1.0F - this.attackAnim * 0.1F);
      poseStack.mulPose(Vector3f.ZN.rotationDegrees(Mth.lerp(this.attackAnim, 0.0F, 5.0F)));
      double guiScale = Minecrft.get().getWindow().getGuiScale();
      poseStack.translate(0.0, 60.0 / guiScale * (double)this.attackAnim, 0.0);
   }

   public void applyMovementDelay(PoseStack poseStack, DeltaTracker deltaTracker) {
      float delta = Math.min(deltaTracker.getGameTimeDeltaTicks() * 0.6F, 1.0F);
      this.xRot0 = Mth.lerp(delta, this.xRot0, this.xRot);
      this.yRot0 = Mth.lerp(delta, this.yRot0, this.yRot);
      this.xRot = Minecrft.get().gameRenderer.getMainCamera().getXRot();
      this.yRot = Minecrft.get().gameRenderer.getMainCamera().getYRot();
      double guiScale = Minecrft.get().getWindow().getGuiScale();
      double horizontalDelay = (double)(this.yRot - this.yRot0) / guiScale * 3.0;
      double verticalDelay = (double)(this.xRot - this.xRot0) / guiScale * 3.0;
      poseStack.translate(-horizontalDelay, -verticalDelay, 0.0);
   }

   protected void recalculateOpening() {
      int width = Minecrft.get().getWindow().getGuiScaledWidth();
      int height = Minecrft.get().getWindow().getGuiScaledHeight();
      float openingSize = (float)Math.min(width, height);
      this.opening.x = ((float)width - openingSize) / 2.0F;
      this.opening.y = ((float)height - openingSize) / 2.0F;
      this.opening.width = openingSize;
      this.opening.height = openingSize;
   }

   protected void renderStatusIcons(PoseStack poseStack, ItemStack cameraStack) {
      ItemStack filmStack = Attachment.FILM.get(cameraStack).getForReading();
      if (!filmStack.isEmpty() && filmStack.getItem() instanceof FilmRollItem filmRollItem && filmRollItem.canAddFrame(filmStack)) {
         this.renderRemainingFramesIcon(poseStack, filmRollItem, filmStack);
      } else {
         this.renderNoFilmIcon(poseStack);
      }
   }

   protected void renderNoFilmIcon(PoseStack poseStack) {
      RenderSystem.setShaderTexture(0, NO_FILM_ICON_TEXTURE);
      int x = (int)(this.opening.x + this.opening.width / 2.0F - 12.0F) + (Integer)Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_X.get();
      int y = (int)(this.opening.y + this.opening.height - 18.0F) + (Integer)Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_Y.get();
      GuiUtil.blit(poseStack, (float)x, (float)y, 23.0F, 18.0F, 0, 0, 23, 18, 0.0F);
   }

   protected void renderRemainingFramesIcon(PoseStack poseStack, FilmRollItem filmRollItem, ItemStack filmStack) {
      int maxFrames = filmRollItem.getMaxFrameCount(filmStack);
      int exposedFrames = filmRollItem.getStoredFramesCount(filmStack);
      int remainingFrames = Math.max(0, maxFrames - exposedFrames);
      if (maxFrames > 5 && remainingFrames <= 3) {
         RenderSystem.setShaderTexture(0, REMAINING_FRAMES_ICON_TEXTURE);
         float x = (float)((int)(this.opening.x + this.opening.width / 2.0F - 17.0F) + (Integer)Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_X.get());
         float y = (float)((int)(this.opening.y + this.opening.height - 15.0F) + (Integer)Config.Client.VIEWFINDER_STATUS_ICON_OFFSET_Y.get());
         int vOffset = (remainingFrames - 1) * 15;
         GuiUtil.blit(poseStack, x, y, 33.0F, 15.0F, 0, vOffset, 33, 45, 0.0F);
      }
   }
}
