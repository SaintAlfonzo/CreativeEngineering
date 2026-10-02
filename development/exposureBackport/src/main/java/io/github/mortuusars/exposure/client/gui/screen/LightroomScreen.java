package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.gui.toast.BetterTutorialToast;
import io.github.mortuusars.exposure.client.gui.toast.ToastIcon;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.gui.BackportContainerScreen;

public class LightroomScreen extends BackportContainerScreen<LightroomMenu> {
   public static final ResourceLocation MAIN_TEXTURE = Exposure.resource("textures/gui/lightroom.png");
   public static final ResourceLocation FILM_OVERLAYS_TEXTURE = Exposure.resource("textures/gui/lightroom_film_overlays.png");
   public static final WidgetSprites PRINT_BUTTON_SPRITES = new WidgetSprites(
      Exposure.resource("lightroom/print_button"),
      Exposure.resource("lightroom/print_button_disabled"),
      Exposure.resource("lightroom/print_button_highlighted")
   );
   public static final WidgetSprites PRINTING_MODE_TOGGLE_REGULAR_SPRITES = new WidgetSprites(
      Exposure.resource("lightroom/printing_mode_regular"), Exposure.resource("lightroom/printing_mode_regular_highlighted")
   );
   public static final WidgetSprites PRINTING_MODE_TOGGLE_CHROMATIC_SPRITES = new WidgetSprites(
      Exposure.resource("lightroom/printing_mode_chromatic"), Exposure.resource("lightroom/printing_mode_chromatic_highlighted")
   );
   public static final int FRAME_SIZE = 54;
   protected final KeyBindings keyBindings = KeyBindings.of(
      Key.press(334).or(Key.press(61)).executes(this::enterFrameInspectMode),
      Key.press(263).or(Key.press(65)).executes(() -> this.changeFrame(PagingDirection.PREVIOUS)),
      Key.press(262).or(Key.press(68)).executes(() -> this.changeFrame(PagingDirection.NEXT))
   );
   protected Player player;
   protected Button printButton;
   protected PrintingMode mode;
   protected CycleButton<PrintingMode> printingModeToggleButton;
   protected Map<Integer, Rect2i> slotPlaceholders = Collections.emptyMap();
   protected boolean hasShownDevelopingToast;

   public LightroomScreen(LightroomMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
      this.player = playerInventory.player;
      this.mode = ((LightroomMenu)this.getMenu()).getBlockEntity().getActualPrintingMode();
   }

   protected void init() {
      this.imageWidth = 176;
      this.imageHeight = 209;
      super.init();
      this.inventoryLabelY = 116;
      this.slotPlaceholders = Map.of(
         0,
         new Rect2i(238, 0, 18, 18),
         1,
         new Rect2i(238, 18, 18, 18),
         2,
         new Rect2i(238, 36, 18, 18),
         3,
         new Rect2i(238, 54, 18, 18),
         4,
         new Rect2i(238, 72, 18, 18),
         5,
         new Rect2i(238, 90, 18, 18)
      );
      this.printButton = new ImageButton(this.leftPos + 117, this.topPos + 89, 22, 22, PRINT_BUTTON_SPRITES, button -> {
         int buttonId = Screen.hasShiftDown() && this.player.isCreative() ? 1 : 0;
         this.clickButton(buttonId);
      }, Component.translatable("gui.exposure.lightroom.print"));
      this.updatePrintButtonTooltip();
      this.addRenderableWidget(this.printButton);
      this.printingModeToggleButton = this.createPrintingModeToggleButton();
      this.addRenderableWidget(this.printingModeToggleButton);
      this.updateButtons();
   }

   protected void updatePrintButtonTooltip() {
      MutableComponent tooltip = Component.translatable("gui.exposure.lightroom.print");
      if (!((LightroomMenu)this.getMenu()).getBlockEntity().hasSufficientLightLevel()) {
         tooltip.append("\n").append(Component.translatable("gui.exposure.lightroom.print.not_enough_light_tooltip").withStyle(ChatFormatting.RED));
      }

      if (this.player.isCreative()) {
         tooltip.append("\n").append(Component.translatable("gui.exposure.lightroom.print.creative_tooltip"));
      }

      this.printButton.setTooltip(Tooltip.create(tooltip));
   }

   protected CycleButton<PrintingMode> createPrintingModeToggleButton() {
      Map<PrintingMode, WidgetSprites> spritesMap = Map.of(
         PrintingMode.REGULAR, PRINTING_MODE_TOGGLE_REGULAR_SPRITES, PrintingMode.CHROMATIC, PRINTING_MODE_TOGGLE_CHROMATIC_SPRITES
      );
      Map<PrintingMode, Tooltip> tooltipMap = Map.of(
         PrintingMode.REGULAR,
         Tooltip.create(
            Component.translatable("gui.exposure.lightroom.printing_mode.regular")
               .append(CommonComponents.NEW_LINE)
               .append(Component.translatable("gui.exposure.lightroom.printing_mode.regular.info").withStyle(ChatFormatting.GRAY))
         ),
         PrintingMode.CHROMATIC,
         Tooltip.create(
            Component.translatable("gui.exposure.lightroom.printing_mode.chromatic")
               .append(CommonComponents.NEW_LINE)
               .append(Component.translatable("gui.exposure.lightroom.printing_mode.chromatic.info").withStyle(ChatFormatting.GRAY))
         )
      );
      return new CycleButton<>(
            this.leftPos - 17,
            this.topPos + 91,
            18,
            18,
            Arrays.asList(PrintingMode.values()),
            ((LightroomMenu)this.getMenu()).getBlockEntity().getActualPrintingMode(),
            spritesMap,
            (button, newMode) -> this.clickButton(4)
         )
         .setClickSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK)
         .setTooltips(tooltipMap);
   }

   protected void clickButton(int buttonId) {
      ((LightroomMenu)this.getMenu()).clickMenuButton(this.player, buttonId);
      Minecrft.gameMode().handleInventoryButtonClick(((LightroomMenu)this.getMenu()).containerId, buttonId);
   }

   protected void containerTick() {
      PrintingMode currentMode = ((LightroomMenu)this.getMenu()).getBlockEntity().getActualPrintingMode();
      if (currentMode != this.printingModeToggleButton.getCurrentValue()) {
         this.printingModeToggleButton.setCurrentValue(currentMode);
      }
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.updateButtons();
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void updateButtons() {
      this.printButton.active = ((LightroomMenu)this.getMenu()).getBlockEntity().canPrint()
         || this.player.isCreative() && Screen.hasShiftDown() && ((LightroomMenu)this.getMenu()).getBlockEntity().canPrintInCreativeMode();
      this.printButton.visible = !((LightroomMenu)this.getMenu()).isPrinting();
      this.updatePrintButtonTooltip();
      this.printingModeToggleButton.active = true;
      this.printingModeToggleButton.visible = ((LightroomMenu)this.getMenu()).canChangeProcess();
   }

   protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(MAIN_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.blit(MAIN_TEXTURE, this.leftPos - 27, this.topPos + 35, 0, 209, 28, 31);
      this.renderSlotPlaceholders(guiGraphics, mouseX, mouseY, partialTick);
      if (((LightroomMenu)this.getMenu()).isPrinting()) {
         int progress = ((LightroomMenu)this.getMenu()).getData().get(0);
         int time = ((LightroomMenu)this.getMenu()).getData().get(1);
         int width = progress != 0 && time != 0 ? progress * 24 / time : 0;
         guiGraphics.blit(MAIN_TEXTURE, this.leftPos + 116, this.topPos + 91, 176, 0, width, 17);
      }

      List<Frame> frames = ((LightroomMenu)this.getMenu()).getExposedFrames();
      if (frames.isEmpty()) {
         guiGraphics.blit(FILM_OVERLAYS_TEXTURE, this.leftPos + 4, this.topPos + 15, 0, 136, 168, 68);
      } else {
         ItemStack filmStack = ((LightroomMenu)this.getMenu()).getSlot(0).getItem();
         if (filmStack.getItem() instanceof DevelopedFilmItem film) {
            ExposureType exposureType = film.getType();
            FilmColor filmColor = exposureType.getFilmColor();
            int selectedFrame = ((LightroomMenu)this.getMenu()).getSelectedFrame();
            Frame leftFrame = ((LightroomMenu)this.getMenu()).getFrameByIndex(selectedFrame - 1);
            Frame centerFrame = ((LightroomMenu)this.getMenu()).getFrameByIndex(selectedFrame);
            Frame rightFrame = ((LightroomMenu)this.getMenu()).getFrameByIndex(selectedFrame + 1);
            RenderSystem.setShaderColor(filmColor.r(), filmColor.g(), filmColor.b(), filmColor.a());
            guiGraphics.blit(FILM_OVERLAYS_TEXTURE, this.leftPos + 1, this.topPos + 15, 0, leftFrame != null ? 68 : 0, 54, 68);
            guiGraphics.blit(FILM_OVERLAYS_TEXTURE, this.leftPos + 55, this.topPos + 15, 55, rightFrame != null ? 0 : 68, 64, 68);
            if (rightFrame != null) {
               boolean hasMoreFrames = selectedFrame + 2 < frames.size();
               guiGraphics.blit(FILM_OVERLAYS_TEXTURE, this.leftPos + 119, this.topPos + 15, 120, hasMoreFrames ? 68 : 0, 56, 68);
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            PoseStack poseStack = guiGraphics.pose();
            if (leftFrame != null) {
               this.renderFrame(
                  leftFrame,
                  poseStack,
                  (float)(this.leftPos + 6),
                  (float)(this.topPos + 22),
                  54.0F,
                  this.isOverLeftFrame(mouseX, mouseY) ? 0.8F : 0.25F,
                  exposureType
               );
            }

            if (centerFrame != null) {
               this.renderFrame(centerFrame, poseStack, (float)(this.leftPos + 61), (float)(this.topPos + 22), 54.0F, 0.9F, exposureType);
            }

            if (rightFrame != null) {
               this.renderFrame(
                  rightFrame,
                  poseStack,
                  (float)(this.leftPos + 116),
                  (float)(this.topPos + 22),
                  54.0F,
                  this.isOverRightFrame(mouseX, mouseY) ? 0.8F : 0.25F,
                  exposureType
               );
            }

            RenderSystem.setShaderColor(filmColor.r(), filmColor.g(), filmColor.b(), filmColor.a());
            if (((LightroomMenu)this.getMenu()).getBlockEntity().isAdvancingFrameOnPrint()) {
               poseStack.pushPose();
               poseStack.translate(0.0F, 0.0F, 800.0F);
               if (selectedFrame < ((LightroomMenu)this.getMenu()).getTotalFramesCount() - 1) {
                  guiGraphics.blit(MAIN_TEXTURE, this.leftPos + 111, this.topPos + 44, 200, 0, 10, 10);
               } else {
                  guiGraphics.blit(MAIN_TEXTURE, this.leftPos + 111, this.topPos + 44, 210, 0, 10, 10);
               }

               poseStack.popPose();
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   private void renderSlotPlaceholders(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      for (int slotIndex : this.slotPlaceholders.keySet()) {
         Slot slot = ((LightroomMenu)this.getMenu()).getSlot(slotIndex);
         if (!slot.hasItem()) {
            Rect2i placeholder = this.slotPlaceholders.get(slotIndex);
            guiGraphics.blit(
               MAIN_TEXTURE,
               this.leftPos + slot.x - 1,
               this.topPos + slot.y - 1,
               placeholder.getX(),
               placeholder.getY(),
               placeholder.getWidth(),
               placeholder.getHeight()
            );
         }
      }
   }

   protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderTooltip(guiGraphics, mouseX, mouseY);
      boolean advancedTooltips = Minecraft.getInstance().options.advancedItemTooltips;
      int selectedFrame = ((LightroomMenu)this.getMenu()).getSelectedFrame();
      List<Component> tooltipLines = new ArrayList<>();
      int hoveredFrameIndex = -1;
      if (this.isOverLeftFrame(mouseX, mouseY)) {
         hoveredFrameIndex = selectedFrame - 1;
         tooltipLines.add(Component.translatable("gui.exposure.lightroom.previous_frame"));
      } else if (this.isOverCenterFrame(mouseX, mouseY)) {
         hoveredFrameIndex = selectedFrame;
         tooltipLines.add(
            Component.translatable(
               "gui.exposure.lightroom.current_frame", new Object[]{Integer.toString(((LightroomMenu)this.getMenu()).getSelectedFrame() + 1)}
            )
         );
      } else if (this.isOverRightFrame(mouseX, mouseY)) {
         hoveredFrameIndex = selectedFrame + 1;
         tooltipLines.add(Component.translatable("gui.exposure.lightroom.next_frame"));
      }

      if (hoveredFrameIndex != -1) {
         this.addFrameInfoTooltipLines(tooltipLines, hoveredFrameIndex, advancedTooltips);
      }

      if (this.isOverCenterFrame(mouseX, mouseY)) {
         tooltipLines.add(Component.translatable("gui.exposure.lightroom.zoom_in.tooltip"));
      }

      guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipLines, Optional.empty(), mouseX, mouseY);
   }

   private void addFrameInfoTooltipLines(List<Component> tooltipLines, int frameIndex, boolean isAdvancedTooltips) {
      Frame frame = ((LightroomMenu)this.getMenu()).getFrameByIndex(frameIndex);
      if (frame != null) {
         if (((LightroomMenu)this.getMenu()).getBlockEntity().isRefracted()) {
            frame.getColorChannel()
               .ifPresent(
                  c -> tooltipLines.add(
                        Component.translatable("gui.exposure.channel." + c.getSerializedName()).withStyle(Style.EMPTY.withColor(c.getRepresentationColor()))
                     )
               );
         }

         if (isAdvancedTooltips) {
            Component component = frame.identifier()
               .map(
                  id -> !id.isEmpty()
                        ? Component.translatable("gui.exposure.frame.id", new Object[]{Component.literal(id).withStyle(ChatFormatting.GRAY)})
                        : Component.empty(),
                  texture -> Component.translatable(
                        "gui.exposure.frame.texture", new Object[]{Component.literal(texture.toString()).withStyle(ChatFormatting.GRAY)}
                     )
               )
               .withStyle(ChatFormatting.DARK_GRAY);
            tooltipLines.add(component);
         }
      }
   }

   private boolean isOverLeftFrame(int mouseX, int mouseY) {
      List<Frame> frames = ((LightroomMenu)this.getMenu()).getExposedFrames();
      int selectedFrame = ((LightroomMenu)this.getMenu()).getSelectedFrame();
      return selectedFrame - 1 >= 0 && selectedFrame - 1 < frames.size() && this.isHovering(6, 22, 54, 54, (double)mouseX, (double)mouseY);
   }

   private boolean isOverCenterFrame(int mouseX, int mouseY) {
      List<Frame> frames = ((LightroomMenu)this.getMenu()).getExposedFrames();
      int selectedFrame = ((LightroomMenu)this.getMenu()).getSelectedFrame();
      return selectedFrame >= 0 && selectedFrame < frames.size() && this.isHovering(61, 22, 54, 54, (double)mouseX, (double)mouseY);
   }

   private boolean isOverRightFrame(int mouseX, int mouseY) {
      List<Frame> frames = ((LightroomMenu)this.getMenu()).getExposedFrames();
      int selectedFrame = ((LightroomMenu)this.getMenu()).getSelectedFrame();
      return selectedFrame + 1 >= 0 && selectedFrame + 1 < frames.size() && this.isHovering(116, 22, 54, 54, (double)mouseX, (double)mouseY);
   }

   public void renderFrame(@NotNull Frame frame, PoseStack poseStack, float x, float y, float size, float alpha, ExposureType exposureType) {
      RenderableImage image = ExposureClient.renderedExposures().getOrCreate(frame).modifyWith(ImageEffect.NEGATIVE_FILM);
      BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      ExposureClient.imageRenderer()
         .render(image, poseStack, bufferSource, new RenderCoordinates(x, y, size, size), exposureType.getImageColor().withAlpha((int)(alpha * 255.0F)));
      bufferSource.endBatch();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      boolean handled = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      if (!handled && scrollY >= 0.0 && this.isOverCenterFrame((int)mouseX, (int)mouseY)) {
         this.enterFrameInspectMode();
      }

      return handled;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         Slot filmSlot = ((LightroomMenu)this.getMenu()).getSlot(0);
         if (!this.hasShownDevelopingToast
            && !filmSlot.hasItem()
            && this.isHovering(filmSlot.x, filmSlot.y, 16, 16, mouseX, mouseY)
            && ((LightroomMenu)this.getMenu()).getCarried().getItem() instanceof FilmRollItem) {
            Minecrft.get()
               .getToasts()
               .addToast(new BetterTutorialToast(ToastIcon.HEADS_UP, Component.translatable("gui.exposure.lightroom.toast.develop_film.title"), null, 7000));
            this.hasShownDevelopingToast = true;
         }

         if (this.isOverCenterFrame((int)mouseX, (int)mouseY)) {
            this.enterFrameInspectMode();
            return true;
         }

         if (this.isOverLeftFrame((int)mouseX, (int)mouseY)) {
            this.changeFrame(PagingDirection.PREVIOUS);
            return true;
         }

         if (this.isOverRightFrame((int)mouseX, (int)mouseY)) {
            this.changeFrame(PagingDirection.NEXT);
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public void changeFrame(PagingDirection navigation) {
      if ((navigation != PagingDirection.PREVIOUS || ((LightroomMenu)this.getMenu()).getSelectedFrame() != 0)
         && (
            navigation != PagingDirection.NEXT
               || ((LightroomMenu)this.getMenu()).getSelectedFrame() != ((LightroomMenu)this.getMenu()).getTotalFramesCount() - 1
         )) {
         Preconditions.checkState(this.minecraft != null);
         Preconditions.checkState(this.minecraft.player != null);
         Preconditions.checkState(this.minecraft.gameMode != null);
         int buttonId = navigation == PagingDirection.NEXT ? 3 : 2;
         this.clickButton(buttonId);
         Minecrft.get()
            .getSoundManager()
            .play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1.0F, ThreadLocalRandom.current().nextFloat() * 0.4F + 0.8F));
         ((LightroomMenu)this.getMenu())
            .getBlockEntity()
            .setSelectedFrameIndex(((LightroomMenu)this.getMenu()).getBlockEntity().getSelectedFrameIndex() + (navigation == PagingDirection.NEXT ? 1 : -1));
      }
   }

   private void enterFrameInspectMode() {
      Minecraft.getInstance().setScreen(new LightroomFrameInspectScreen(this));
      this.player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1.0F, 1.3F);
   }

   protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
      return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton) && this.hoveredSlot == null;
   }
}
