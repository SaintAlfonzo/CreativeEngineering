package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.SteppedZoom;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;

public class FilmFrameInspectScreen extends BackportScreen {
   public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
   public static final int BG_SIZE = 78;
   public static final int FRAME_SIZE = 54;
   protected final Pager pager = new Pager().setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)).onPageChanged(this::pageChanged);
   protected final SteppedZoom zoom = new SteppedZoom().zoomInSteps(4).zoomOutSteps(4).zoomPerStep(1.4).defaultZoom(1.0);
   protected final KeyBindings keyBindings = KeyBindings.of(
      Key.press(Minecrft.options().keyInventory).executes(this::onClose),
      Key.press(334).or(Key.press(61)).executes(this.zoom::zoomIn),
      Key.press(333).or(Key.press(45)).executes(this.zoom::zoomOut),
      Key.press(263).or(Key.press(65)).executes(this.pager::previousPage),
      Key.press(262).or(Key.press(68)).executes(this.pager::nextPage),
      Key.release(263).or(Key.press(65)).executes(this.pager::resetCooldown),
      Key.release(262).or(Key.press(68)).executes(this.pager::resetCooldown)
   );
   protected final List<Frame> frames;
   protected float zoomFactor;
   protected float x;
   protected float y;

   public FilmFrameInspectScreen(List<Frame> frames) {
      this(frames, frames.size() - 1);
   }

   public FilmFrameInspectScreen(List<Frame> frames, int startingFrame) {
      super(Component.empty());
      this.frames = new ArrayList<>(frames);
      this.initPager(startingFrame);
   }

   protected void initPager(int startingFrame) {
      this.pager.setPagesCount(this.frames.size());
      this.pager.setPage(startingFrame);
      Collections.rotate(this.frames, -startingFrame);
   }

   protected void init() {
      super.init();
      this.zoomFactor = (float)this.height / 78.0F / (float)this.zoom.getZoomPerStep();
      ImageButton previousButton = new ImageButton(
         0,
         (int)((float)this.height / 2.0F - 8.0F),
         16,
         16,
         Widgets.PREVIOUS_BUTTON_SPRITES,
         button -> this.pager.changePage(PagingDirection.PREVIOUS),
         Component.translatable("gui.exposure.previous_page")
      );
      this.addRenderableWidget(previousButton);
      ImageButton nextButton = new ImageButton(
         this.width - 16,
         (int)((float)this.height / 2.0F - 8.0F),
         16,
         16,
         Widgets.NEXT_BUTTON_SPRITES,
         button -> this.pager.changePage(PagingDirection.NEXT),
         Component.translatable("gui.exposure.next_page")
      );
      this.addRenderableWidget(nextButton);
      this.pager.setPagesCount(this.frames.size()).setPreviousPageButton(previousButton).setNextPageButton(nextButton);
   }

   protected Frame getCurrentFrame() {
      return (Frame)this.frames.get(0);
   }

   protected void pageChanged(int oldPage, int newPage) {
      int distance = newPage - oldPage;
      Collections.rotate(this.frames, -distance);
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      float scale = (float)(this.zoom.get() * (double)this.zoomFactor);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      this.renderTransparentBackground(guiGraphics);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(this.x, this.y, 0.0F);
      guiGraphics.pose().translate((float)this.width / 2.0F, (float)this.height / 2.0F, 50.0F);
      guiGraphics.pose().scale(scale, scale, scale);
      guiGraphics.pose().translate(-39.0F, -39.0F, 0.0F);
      RenderSystem.setShaderTexture(0, TEXTURE);
      GuiUtil.blit(guiGraphics.pose(), 0.0F, 0.0F, 78.0F, 78.0F, 0, 0, 256, 256, 0.0F);
      Frame frame = this.getCurrentFrame();
      ExposureType filmType = frame.type();
      FilmColor filmColor = filmType.getFilmColor();
      RenderSystem.setShaderColor(filmColor.r(), filmColor.g(), filmColor.b(), filmColor.a());
      GuiUtil.blit(guiGraphics.pose(), 0.0F, 0.0F, 78.0F, 78.0F, 0, 78, 256, 256, 0.0F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.pose().translate(12.0F, 12.0F, 0.0F);
      RenderableImage image = ExposureClient.renderedExposures().getOrCreate(frame).modifyWith(ImageEffect.NEGATIVE_FILM);
      BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      ExposureClient.imageRenderer().render(image, guiGraphics.pose(), bufferSource, new RenderCoordinates(0.0F, 0.0F, 54.0F, 54.0F), filmType.getImageColor());
      bufferSource.endBatch();
      guiGraphics.pose().popPose();
      guiGraphics.pose().pushPose();
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.pose().popPose();
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
         return true;
      } else {
         if (scrollY >= 0.0) {
            this.zoom.zoomIn();
         } else {
            this.zoom.zoomOut();
         }

         return true;
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
         return true;
      } else if (button == 0) {
         float centerX = (float)this.width / 2.0F;
         float centerY = (float)this.height / 2.0F;
         this.x = (float)Mth.clamp((double)this.x + dragX, (double)(-centerX), (double)centerX);
         this.y = (float)Mth.clamp((double)this.y + dragY, (double)(-centerY), (double)centerY);
         return true;
      } else {
         return false;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
