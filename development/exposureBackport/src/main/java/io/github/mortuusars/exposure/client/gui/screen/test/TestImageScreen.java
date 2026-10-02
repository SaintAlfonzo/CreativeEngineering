package io.github.mortuusars.exposure.client.gui.screen.test;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.Checkbox;
import io.github.mortuusars.exposure.backport.gui.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Matrix4f;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;
import io.github.mortuusars.exposure.backport.util.Backport;
import io.github.mortuusars.exposure.backport.gui.GuiRenderTypes;

public class TestImageScreen extends BackportScreen {
   protected float scale = 1.0F;
   protected boolean isCapturing;
   @Nullable
   protected Image image;
   @Nullable
   protected RenderableImage renderableImage;
   protected List<AbstractWidget> rightPaneWidgets = new ArrayList<>();
   protected Slider sizeSlider;
   protected ShutterSpeedSlider shutterSpeedSlider;
   protected Slider exposureSlider;
   protected Slider contrastSlider;
   protected Slider shadowsSlider;
   protected Slider midtonesSlider;
   protected Slider highlightsSlider;
   protected Slider blackSlider;
   protected Slider whiteSlider;
   protected Slider balanceRedSlider;
   protected Slider balanceGreenSlider;
   protected Slider balanceBlueSlider;
   protected Slider hueSlider;
   protected Slider saturationSlider;
   protected Slider brightnessSlider;
   protected Slider noiseSlider;
   protected Checkbox bw;
   protected Checkbox aged;
   protected float rightPaneScroll = 0.0F;
   protected long applyEditsAt = -1L;

   public TestImageScreen() {
      super(Component.empty());
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void init() {
      int sliderWidth = 120;
      int sliderHeight = 15;
      int spacingInGroup = 1;
      int spacingBetweenGroups = 4;
      int screenEdgeMargin = 5;
      int labelXOffset = 3;
      int x = this.width - sliderWidth - screenEdgeMargin;
      int labelX = x + labelXOffset;
      this.rightPaneWidgets.clear();
      this.sizeSlider = new Slider(x, screenEdgeMargin, sliderWidth, sliderHeight, 320.0, 1.0, 2048.0, 0, "Size", v -> this.onChanged());
      this.addRenderableWidget(this.sizeSlider);
      this.rightPaneWidgets.add(this.sizeSlider);
      int y = screenEdgeMargin + this.sizeSlider.getHeight();
      y += spacingBetweenGroups;
      MultiLineTextWidget toneLabel = new MultiLineTextWidget(labelX, y, Component.literal("Tone"), this.font);
      this.addRenderableOnly(toneLabel);
      this.rightPaneWidgets.add(toneLabel);
      y += 9;
      this.shutterSpeedSlider = new ShutterSpeedSlider(
         x, y, sliderWidth, sliderHeight, "Shutter Speed", Exposure.Items.CAMERA.get().getAvailableShutterSpeeds(), ShutterSpeed.DEFAULT, v -> this.onChanged()
      );
      this.shutterSpeedSlider.setHorizontalGradient(1140850688, 1157627903);
      this.addRenderableWidget(this.shutterSpeedSlider);
      this.rightPaneWidgets.add(this.shutterSpeedSlider);
      y += this.shutterSpeedSlider.getHeight();
      y += spacingInGroup;
      this.exposureSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -4.0, 4.0, 2, "Sensitivity", v -> this.onChanged());
      this.exposureSlider.setHorizontalGradient(1140850688, 1157627903);
      this.addRenderableWidget(this.exposureSlider);
      this.rightPaneWidgets.add(this.exposureSlider);
      y += this.exposureSlider.getHeight();
      y += spacingInGroup;
      this.contrastSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Contrast", v -> this.onChanged());
      this.contrastSlider.setHorizontalGradient(1157627903, 1140850688);
      this.addRenderableWidget(this.contrastSlider);
      this.rightPaneWidgets.add(this.contrastSlider);
      y += this.contrastSlider.getHeight();
      y += spacingBetweenGroups;
      MultiLineTextWidget levelsLabel = new MultiLineTextWidget(labelX, y, Component.literal("Levels"), this.font);
      this.addRenderableOnly(levelsLabel);
      this.rightPaneWidgets.add(levelsLabel);
      y += 9;
      this.shadowsSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, 0.0, 255.0, 0, "Shadows", v -> this.onChanged());
      this.shadowsSlider.setHorizontalGradient(1157627903, 1140850688);
      this.addRenderableWidget(this.shadowsSlider);
      this.rightPaneWidgets.add(this.shadowsSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.midtonesSlider = new Slider(x, y, sliderWidth, sliderHeight, 128.0, 0.0, 255.0, 0, "Midtones", v -> this.onChanged());
      this.midtonesSlider.setHorizontalGradient(1157627903, 1140850688);
      this.addRenderableWidget(this.midtonesSlider);
      this.rightPaneWidgets.add(this.midtonesSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.highlightsSlider = new Slider(x, y, sliderWidth, sliderHeight, 255.0, 0.0, 255.0, 0, "Highlights", v -> this.onChanged());
      this.highlightsSlider.setHorizontalGradient(1157627903, 1140850688);
      this.addRenderableWidget(this.highlightsSlider);
      this.rightPaneWidgets.add(this.highlightsSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.blackSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, 0.0, 255.0, 0, "Black", v -> this.onChanged());
      this.blackSlider.setHorizontalGradient(1140850688, 1157627903);
      this.addRenderableWidget(this.blackSlider);
      this.rightPaneWidgets.add(this.blackSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.whiteSlider = new Slider(x, y, sliderWidth, sliderHeight, 255.0, 0.0, 255.0, 0, "White", v -> this.onChanged());
      this.whiteSlider.setHorizontalGradient(1140850688, 1157627903);
      this.addRenderableWidget(this.whiteSlider);
      this.rightPaneWidgets.add(this.whiteSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingBetweenGroups;
      MultiLineTextWidget hsbLabel = new MultiLineTextWidget(labelX, y, Component.literal("HSB"), this.font);
      this.addRenderableOnly(hsbLabel);
      this.rightPaneWidgets.add(hsbLabel);
      y += 9;
      this.hueSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Hue", v -> this.onChanged());
      this.hueSlider.setHorizontalGradient(1157562368, 1157562623);
      this.addRenderableWidget(this.hueSlider);
      this.rightPaneWidgets.add(this.hueSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.saturationSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Saturation", v -> this.onChanged());
      this.saturationSlider.setHorizontalGradient(1148680055, 1157571089);
      this.addRenderableWidget(this.saturationSlider);
      this.rightPaneWidgets.add(this.saturationSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.brightnessSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Brightness", v -> this.onChanged());
      this.brightnessSlider.setHorizontalGradient(1140850688, 1157627903);
      this.addRenderableWidget(this.brightnessSlider);
      this.rightPaneWidgets.add(this.brightnessSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingBetweenGroups;
      MultiLineTextWidget colorBalanceLabel = new MultiLineTextWidget(labelX, y, Component.literal("Color Balance"), this.font);
      this.addRenderableOnly(colorBalanceLabel);
      this.rightPaneWidgets.add(colorBalanceLabel);
      y += 9;
      this.balanceRedSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Red", v -> this.onChanged());
      this.balanceRedSlider.setHorizontalGradient(1140916223, 1157562368);
      this.addRenderableWidget(this.balanceRedSlider);
      this.rightPaneWidgets.add(this.balanceRedSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.balanceGreenSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Green", v -> this.onChanged());
      this.balanceGreenSlider.setHorizontalGradient(1157562623, 1140915968);
      this.addRenderableWidget(this.balanceGreenSlider);
      this.rightPaneWidgets.add(this.balanceGreenSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingInGroup;
      this.balanceBlueSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, -1.0, 1.0, 2, "Blue", v -> this.onChanged());
      this.balanceBlueSlider.setHorizontalGradient(1157627648, 1140850943);
      this.addRenderableWidget(this.balanceBlueSlider);
      this.rightPaneWidgets.add(this.balanceBlueSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingBetweenGroups;
      MultiLineTextWidget effectsLabel = new MultiLineTextWidget(labelX, y, Component.literal("Effects"), this.font);
      this.addRenderableOnly(effectsLabel);
      this.rightPaneWidgets.add(effectsLabel);
      y += 9;
      this.noiseSlider = new Slider(x, y, sliderWidth, sliderHeight, 0.0, 0.0, 1.0, 2, "Noise", v -> this.onChanged());
      this.noiseSlider.setHorizontalGradient(1148680055, 1157627903);
      this.addRenderableWidget(this.noiseSlider);
      this.rightPaneWidgets.add(this.noiseSlider);
      y += this.shadowsSlider.getHeight();
      y += spacingBetweenGroups;
      this.bw = Checkbox.builder(Component.literal("BW"), this.font).pos(x, y).onValueChange((checkbox, bl) -> this.onChanged()).build();
      this.addRenderableWidget(this.bw);
      this.rightPaneWidgets.add(this.bw);
      this.aged = Checkbox.builder(Component.literal("AGED"), this.font)
         .pos(x + this.bw.getWidth() + 8, y)
         .onValueChange((checkbox, bl) -> this.onChanged())
         .build();
      this.addRenderableWidget(this.aged);
      this.rightPaneWidgets.add(this.aged);
      Button capture = Button.builder(Component.literal("CAPTURE"), b -> this.capture())
         .size(60, 20)
         .pos(screenEdgeMargin, this.height - 20 - screenEdgeMargin)
         .build();
      this.addRenderableWidget(capture);
      if (this.image == null) {
         this.capture();
      }
   }

   protected void capture() {
      if (this.isCapturing) {
         Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1.0F));
         Minecrft.player().displayClientMessage(Component.literal("Capture is in progress."), false);
      } else {
         this.isCapturing = true;
         Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_OPEN.get(), 1.0F));
         ExposureClient.cycles()
            .enqueueTask(
               Capture.of(Capture.screenshot(), CaptureAction.hideGui())
                  .handleErrorAndGetResult(
                     err -> Minecrft.execute(() -> Minecrft.player().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false))
                  )
                  .thenAsync(ImageEffect.Crop.SQUARE_CENTER::modify)
                  .onError(err -> Minecrft.execute(() -> Minecrft.player().displayClientMessage(err.casual().withStyle(ChatFormatting.RED), false)))
                  .accept(this::setImage)
            );
      }
   }

   protected void applyEdits() {
      if (this.renderableImage == null) {
         Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1.0F));
         Minecrft.player().displayClientMessage(Component.literal("No image to modify."), false);
      } else {
         try {
            this.renderableImage = RenderableImage.of("test_image", this.image)
               .modifyWith(ImageEffect.Resize.to(this.sizeSlider.getValue().intValue()))
               .modifyWith(
                  ImageEffect.exposure(this.shutterSpeedSlider.getShutterSpeed().getBrightness() * (this.exposureSlider.getValue().floatValue() + 1.0F))
               )
               .modifyWith(ImageEffect.contrast(this.contrastSlider.getValue().floatValue()))
               .modifyWith(
                  ImageEffect.levels(
                     new Levels(
                        this.shadowsSlider.getValue().intValue(),
                        this.midtonesSlider.getValue().intValue(),
                        this.highlightsSlider.getValue().intValue(),
                        this.blackSlider.getValue().intValue(),
                        this.whiteSlider.getValue().intValue()
                     )
                  )
               )
               .modifyWith(
                  ImageEffect.hsb(
                     this.hueSlider.getValue().floatValue(), this.saturationSlider.getValue().floatValue(), this.brightnessSlider.getValue().floatValue()
                  )
               )
               .modifyWith(
                  ImageEffect.colorBalance(
                     this.balanceRedSlider.getValue().floatValue(),
                     this.balanceGreenSlider.getValue().floatValue(),
                     this.balanceBlueSlider.getValue().floatValue()
                  )
               )
               .modifyWith(ImageEffect.noise(this.noiseSlider.getValue().floatValue()))
               .modifyWith(ImageEffect.optional(this.bw.selected(), ImageEffect.BLACK_AND_WHITE))
               .modifyWith(image -> Palettizer.DITHERED.palettize(image, (ColorPalette)ColorPalettes.getDefault(Minecrft.registryAccess()).value()), "palette")
               .modifyWith(ImageEffect.optional(this.aged.selected(), ImageEffect.AGED));
            ExposureClient.imageRenderer().clearCacheOf("test_image");
         } catch (Exception var2) {
            Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1.0F));
            Minecrft.player().displayClientMessage(Component.literal("Failed to apply edits. " + var2.getMessage()), false);
            Exposure.LOGGER.error("Failed to apply edits: ", var2);
         }

         this.applyEditsAt = -1L;
      }
   }

   protected void onChanged() {
      this.applyEditsAt = UnixTimestamp.Milliseconds.now();
   }

   private void setImage(Image image) {
      if (this.image != null) {
         this.image.close();
      }

      if (this.renderableImage != null) {
         this.renderableImage.close();
      }

      this.image = image;
      this.renderableImage = RenderableImage.of("test_image", image);
      this.applyEdits();
      ExposureClient.imageRenderer().clearCacheOf("test_image");
      Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.SHUTTER_CLOSE.get(), 1.0F));
      this.isCapturing = false;
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.fillHorizontalGradient(guiGraphics, this.width - 100, -500, this.width, this.height + 500, 0, -1156509423);
      if (this.isCapturing) {
         String txt = "Capturing...";
         guiGraphics.drawString(this.font, txt, this.width / 2 - this.font.width(txt) / 2, this.height / 2 - 4, -1);
      } else if (this.renderableImage == null) {
         String txt = "<No Image>";
         guiGraphics.drawString(this.font, txt, this.width / 2 - this.font.width(txt) / 2, this.height / 2 - 4, -1);
      } else {
         if (this.applyEditsAt > 0L && UnixTimestamp.Milliseconds.now() >= this.applyEditsAt) {
            this.applyEdits();
         }

         guiGraphics.pose().pushPose();
         float size = (float)this.height * 0.8F * this.scale;
         guiGraphics.pose().translate((float)this.width / 2.0F - size / 2.0F, (float)this.height / 2.0F - size / 2.0F, -100.0F);
         float borderPercent = 0.02F;
         guiGraphics.fill(
            Mth.floor(-size * borderPercent),
            Mth.floor(-size * borderPercent),
            Mth.ceil(size + size * borderPercent),
            Mth.ceil(size + size * borderPercent),
            -1
         );
         guiGraphics.pose().scale(size, size, size);
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         ExposureClient.imageRenderer().render(this.renderableImage, guiGraphics.pose(), bufferSource, RenderCoordinates.DEFAULT, Color.WHITE);
         bufferSource.endBatch();
         guiGraphics.pose().popPose();
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
         return true;
      } else {
         int value = Mth.sign(scrollY);
         if (mouseX > (double)(this.width - 100) && !this.rightPaneWidgets.isEmpty()) {
            int yChange = value * 14;
            if (yChange > 0 && ((AbstractWidget)this.rightPaneWidgets.get(0)).getY() >= 5) {
               return true;
            } else if (yChange < 0
               && ((AbstractWidget)Backport.getLast(this.rightPaneWidgets)).getY() + ((AbstractWidget)Backport.getLast(this.rightPaneWidgets)).getHeight() <= this.height - 5) {
               return true;
            } else {
               for (AbstractWidget widget : this.rightPaneWidgets) {
                  widget.setY(widget.getY() + yChange);
                  if (widget.getY() >= 5 && widget.getY() + widget.getHeight() <= this.height - 5) {
                     widget.visible = true;
                  } else {
                     widget.visible = false;
                  }
               }

               return true;
            }
         } else {
            this.scale = Mth.clamp(this.scale + 0.2F * (float)value, 0.2F, 2.0F);
            return true;
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
         return true;
      } else if (button != 1) {
         return false;
      } else {
         double fov = (double)((Integer)Minecrft.options().fov().get()).intValue();
         int windowWidth = Minecrft.get().getWindow().getHeight();
         int windowHeight = Minecrft.get().getWindow().getHeight();
         double xRot = dragY / (double)windowHeight * fov;
         double yRot = dragX / (double)windowWidth * fov * ((double)windowHeight / (double)windowWidth);
         Minecrft.player().turn(-yRot * 20.0, -xRot * 20.0);
         return true;
      }
   }

   private void fillHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
      // 1.20 builds these vertices in RenderType.gui(); 1.19.2 draws GUI fills immediately
      guiGraphics.fillHorizontalGradient(x1, y1, x2, y2, colorFrom, colorTo);
   }
}
