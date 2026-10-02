package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.export.ImageExporter;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.SteppedZoom;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.Util;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;

public class PhotographScreen extends BackportScreen {
   protected final Pager pager = new Pager()
      .setCycled(true)
      .setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK))
      .onPageChanged(this::pageChanged);
   protected final SteppedZoom zoom = new SteppedZoom().zoomInSteps(4).zoomOutSteps(4).zoomPerStep(1.4).defaultZoom(1.0);
   protected final KeyBindings keyBindings = KeyBindings.of(
      Key.press(Minecrft.options().keyInventory).executes(this::onClose),
      Key.press(334).or(Key.press(61)).executes(this.zoom::zoomIn),
      Key.press(333).or(Key.press(45)).executes(this.zoom::zoomOut),
      Key.press(2, 73).executes(this::dropAsItem),
      Key.press(2, 67).executes(this::copyIdentifierToClipboard),
      Key.press(3, 67).executes(this::copySavedFilePathToClipboard),
      Key.press(2, 83).executes(this::openSavedFile),
      Key.press(263).or(Key.press(65)).executes(this.pager::previousPage),
      Key.press(262).or(Key.press(68)).executes(this.pager::nextPage),
      Key.release(263).or(Key.press(65)).executes(this.pager::resetCooldown),
      Key.release(262).or(Key.press(68)).executes(this.pager::resetCooldown)
   );
   protected final PhotographScreen.PhotographProvider photographProvider;
   protected float x;
   protected float y;
   protected final Set<String> savedExposureIds = new HashSet<>();
   protected final Map<String, File> savedExposureFiles = new HashMap<>();
   protected ArrayList<ItemAndStack<PhotographItem>> photographs = new ArrayList<>();

   public PhotographScreen(PhotographScreen.PhotographProvider photographProvider) {
      super(Component.empty());
      this.photographProvider = photographProvider;
      this.setPhotographs(photographProvider.get());
      if (this.shouldQueryAllPhotographsImmediately()) {
         this.queryAllPhotographs(this.photographs);
      }
   }

   public PhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
      this(PhotographScreen.PhotographProvider.fixed(photographs));
   }

   protected void setPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
      this.photographs.clear();
      this.photographs.addAll(photographs);
      this.pager.setPagesCount(photographs.size());
      this.pager.setPage(0);
   }

   public void tick() {
      if (this.photographProvider.shouldRefresh()) {
         this.setPhotographs(this.photographProvider.get());
      }
   }

   protected void init() {
      super.init();
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
      this.pager.setPreviousPageButton(previousButton).setNextPageButton(nextButton);
   }

   protected boolean shouldQueryAllPhotographsImmediately() {
      return true;
   }

   protected void queryAllPhotographs(List<ItemAndStack<PhotographItem>> photographs) {
      for (ItemAndStack<PhotographItem> photograph : photographs) {
         photograph.getItem().getFrame(photograph.getItemStack()).identifier().ifId(id -> ExposureClient.exposureStore().getOrRequest(id));
      }
   }

   public ItemAndStack<PhotographItem> getCurrentPhotograph() {
      return (ItemAndStack<PhotographItem>)this.photographs.get(0);
   }

   protected void pageChanged(int oldPage, int newPage) {
      int distance = newPage - oldPage;
      Collections.rotate(this.photographs, -distance);
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      float zoomFactor = (float)this.height * 0.8F;
      float scale = (float)(this.zoom.get() * (double)zoomFactor);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      this.renderTransparentBackground(guiGraphics);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(this.x, this.y, 0.0F);
      guiGraphics.pose().translate((float)this.width / 2.0F, (float)this.height / 2.0F, 50.0F);
      guiGraphics.pose().scale(scale, scale, scale);
      guiGraphics.pose().translate(-0.5, -0.5, 0.0);
      BufferSource bufferSource = Minecrft.get().renderBuffers().bufferSource();
      ExposureClient.photographRenderer().renderStackedPhotographs(this.photographs, guiGraphics.pose(), bufferSource, 15728880, 255, 255, 255, 255);
      bufferSource.endBatch();
      guiGraphics.pose().popPose();
      ItemAndStack<PhotographItem> photograph = this.getCurrentPhotograph();
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderFrameInfoHint(guiGraphics, mouseX, mouseY, photograph);
      guiGraphics.pose().popPose();
      if ((Boolean)Config.Client.EXPORT_PHOTOGRAPH_WHEN_VIEWED.get()) {
         this.trySaveToFile(photograph);
      }
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   private void renderFrameInfoHint(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, ItemAndStack<PhotographItem> photograph) {
      if (Minecrft.get().player != null && Minecrft.get().player.isCreative()) {
         Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
         if (frame != Frame.EMPTY) {
            guiGraphics.drawString(this.font, "?", this.width - this.font.width("?") - 10, 10, -1);
            if (mouseX > this.width - 20 && mouseX < this.width && mouseY < 20) {
               String exposureName = frame.identifier().map(id -> id, ResourceLocation::toString);
               List<Component> lines = new ArrayList<>();
               lines.add(Component.literal(exposureName));
               lines.add(Component.translatable("gui.exposure.photograph_screen.drop_as_item_tooltip", new Object[]{Component.literal("CTRL + I")}));
               lines.add(
                  Component.translatable(
                     "gui.exposure.photograph_screen.copy_" + frame.identifier().map(id -> "id", texture -> "texture_path") + "_tooltip",
                     new Object[]{"CTRL + C"}
                  )
               );
               frame.identifier()
                  .getId()
                  .ifPresent(
                     id -> {
                        if (this.savedExposureFiles.containsKey(id)) {
                           lines.add(
                              Component.translatable(
                                 "gui.exposure.photograph_screen.copy_saved_file_path_tooltip", new Object[]{Component.literal("CTRL + SHIFT + C")}
                              )
                           );
                           lines.add(
                              Component.translatable("gui.exposure.photograph_screen.open_saved_file_tooltip", new Object[]{Component.literal("CTRL + S")})
                           );
                        }
                     }
                  );
               guiGraphics.renderTooltip(this.font, lines, Optional.empty(), mouseX, mouseY + 20);
            }
         }
      }
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

   protected boolean dropAsItem() {
      if (!Minecrft.player().isCreative()) {
         return false;
      } else {
         ItemStack droppedStack = this.getCurrentPhotograph().getItemStack().copy();
         Frame frame = (Frame)Exposure.DataComponents.PHOTOGRAPH_FRAME.getOrDefault(droppedStack, Frame.EMPTY);
         ExposureType type = frame.type();
         Exposure.DataComponents.PHOTOGRAPH_TYPE.set(droppedStack, type);
         Minecrft.gameMode().handleCreativeModeItemDrop(droppedStack);
         Minecrft.player()
            .displayClientMessage(
               Component.translatable("gui.exposure.photograph_screen.item_dropped_message", new Object[]{droppedStack.getDisplayName()}), false
            );
         return true;
      }
   }

   protected boolean copyIdentifierToClipboard() {
      Frame frame = this.getCurrentPhotograph().map(PhotographItem::getFrame);
      if (Minecrft.player().isCreative() && !frame.equals(Frame.EMPTY)) {
         String text = frame.identifier().map(id -> id, ResourceLocation::toString);
         Minecrft.get().keyboardHandler.setClipboard(text);
         Minecrft.player().displayClientMessage(Component.translatable("gui.exposure.photograph_screen.copied_message", new Object[]{text}), false);
         return true;
      } else {
         return false;
      }
   }

   protected boolean copySavedFilePathToClipboard() {
      return this.getCurrentPhotograph()
         .map(PhotographItem::getFrame)
         .identifier()
         .mapId(
            id -> {
               if ((Object)this.savedExposureFiles.get(id) instanceof File file) {
                  Minecrft.get().keyboardHandler.setClipboard(file.getAbsolutePath());
                  Minecrft.player()
                     .displayClientMessage(Component.translatable("gui.exposure.photograph_screen.copied_message", new Object[]{file.getAbsolutePath()}), false);
                  return true;
               } else {
                  return false;
               }
            }
         )
         .orElse(false);
   }

   protected boolean openSavedFile() {
      return this.getCurrentPhotograph().map(PhotographItem::getFrame).identifier().mapId(id -> {
         if ((Object)this.savedExposureFiles.get(id) instanceof File file) {
            Util.getPlatform().openFile(file);
            return true;
         } else {
            return false;
         }
      }).orElse(false);
   }

   protected void trySaveToFile(ItemAndStack<PhotographItem> photograph) {
      Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
      if (frame != Frame.EMPTY && frame.identifier().isId() && frame.isTakenBy(Minecrft.player())) {
         String id = frame.identifier().getId().orElseThrow();
         PhotographType photographType = photograph.getItem().getType(photograph.getItemStack());
         PhotographStyle photographStyle = PhotographStyle.of(photograph.getItemStack());
         String filename = this.getFilename(id, photographType);
         if (!this.savedExposureIds.contains(filename)) {
            ExposureClient.exposureStore()
               .getOrRequest(id)
               .getData()
               .ifPresent(
                  exposure -> {
                     this.savedExposureIds.add(filename);
                     CompletableFuture.runAsync(
                           () -> new ImageExporter(exposure, filename)
                                 .modify(
                                    ImageEffect.chain(
                                       photographStyle.modifier(), ImageEffect.Resize.multiplier((Integer)Config.Client.EXPORT_SIZE_MULTIPLIER.get())
                                    )
                                 )
                                 .toExposuresFolder()
                                 .organizeByWorld((Boolean)Config.Client.EXPORT_ORGANIZE_BY_WORLD.get())
                                 .setCreationDate(exposure.getTag().unixTimestamp())
                                 .onExport(file -> this.savedExposureFiles.put(id, file))
                                 .export()
                        )
                        .handle((unused, throwable) -> {
                           Exposure.LOGGER.error(throwable.getMessage());
                           return null;
                        });
                  }
               );
         }
      }
   }

   @NotNull
   protected String getFilename(String id, PhotographType photographType) {
      String suffix = photographType.getFileSuffix();
      return !StringUtil.isNullOrEmpty(suffix) ? id + "_" + suffix : id;
   }

   public interface PhotographProvider {
      boolean shouldRefresh();

      List<ItemAndStack<PhotographItem>> get();

      static PhotographScreen.PhotographProvider fixed(List<ItemAndStack<PhotographItem>> photographs) {
         Preconditions.checkState(!photographs.isEmpty(), "No photographs to display.");
         return new PhotographScreen.PhotographProvider() {
            private final List<ItemAndStack<PhotographItem>> list = photographs;

            @Override
            public boolean shouldRefresh() {
               return false;
            }

            @Override
            public List<ItemAndStack<PhotographItem>> get() {
               return this.list;
            }
         };
      }

      static PhotographScreen.PhotographProvider fromPhotographItem(int slot) {
         return new PhotographScreen.PhotographProvider.ItemProvider(() -> Minecrft.player().getInventory().getItem(slot));
      }

      public static class ItemProvider implements PhotographScreen.PhotographProvider {
         protected Supplier<ItemStack> itemSupplier;
         protected List<ItemAndStack<PhotographItem>> photographs;

         public ItemProvider(Supplier<ItemStack> itemSupplier) {
            this.itemSupplier = itemSupplier;
            ItemStack stack = itemSupplier.get();
            Preconditions.checkState(
               stack.getItem() instanceof PhotographItem || stack.getItem() instanceof StackedPhotographsItem,
               "itemSupplier should supply valid Photograph or Stacked Photographs item stack at the moment of creation."
            );
            this.photographs = this.fromItemStack(stack);
         }

         protected List<ItemAndStack<PhotographItem>> fromItemStack(ItemStack stack) {
            if (stack.getItem() instanceof PhotographItem) {
               return List.of(new ItemAndStack<>(stack));
            } else {
               return stack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem
                  ? stackedPhotographsItem.getPhotographs(stack).photographsItemAndStacks()
                  : Collections.emptyList();
            }
         }

         @Override
         public boolean shouldRefresh() {
            ItemStack item = this.itemSupplier.get();
            List<ItemAndStack<PhotographItem>> newPhotographs = this.fromItemStack(item);
            if (newPhotographs.isEmpty()) {
               return false;
            } else {
               boolean shouldRefresh = !this.get().equals(newPhotographs);
               if (shouldRefresh) {
                  this.photographs = newPhotographs;
               }

               return shouldRefresh;
            }
         }

         @Override
         public List<ItemAndStack<PhotographItem>> get() {
            return this.photographs;
         }
      }
   }
}
