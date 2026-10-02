package io.github.mortuusars.exposure.client.gui.screen.album;

import com.google.common.collect.Lists;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import io.github.mortuusars.exposure.backport.gui.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import io.github.mortuusars.exposure.backport.gui.DefaultTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import io.github.mortuusars.exposure.backport.util.Backport;
import io.github.mortuusars.exposure.backport.client.ClientBackport;

public class PhotographSlotWidget extends AbstractWidget {
   public static final WidgetSprites SPRITES = new WidgetSprites(
      Exposure.resource("album/photograph_slot"), Exposure.resource("album/photograph_slot_highlighted")
   );
   public static final WidgetSprites EMPTY_SPRITES = new WidgetSprites(
      Exposure.resource("album/photograph_slot_empty"), Exposure.resource("album/photograph_slot_empty_highlighted")
   );
   private final Screen parent;
   protected final Supplier<ItemStack> photographSupplier;
   protected boolean editable;
   protected Consumer<PhotographSlotWidget> primaryAction = slot -> {
   };
   protected Consumer<PhotographSlotWidget> secondaryAction = slot -> {
   };
   protected boolean hasPhotograph;

   public PhotographSlotWidget(Screen parent, int x, int y, int width, int height, Supplier<ItemStack> photographSupplier) {
      super(x, y, width, height, Component.empty());
      this.parent = parent;
      this.photographSupplier = photographSupplier;
   }

   public PhotographSlotWidget editable(boolean editable) {
      this.editable = editable;
      return this;
   }

   public PhotographSlotWidget primaryAction(Consumer<PhotographSlotWidget> primaryAction) {
      this.primaryAction = primaryAction;
      return this;
   }

   public PhotographSlotWidget secondaryAction(Consumer<PhotographSlotWidget> secondaryAction) {
      this.secondaryAction = secondaryAction;
      return this;
   }

   public boolean isEditable() {
      return this.editable;
   }

   public ItemStack getPhotograph() {
      return this.photographSupplier.get();
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      ItemStack photograph = this.getPhotograph();
      if (photograph.getItem() instanceof PhotographItem) {
         this.hasPhotograph = true;
         PhotographStyle photographStyle = PhotographStyle.of(photograph);
         guiGraphics.blit(photographStyle.albumPaperTexture(), this.getX(), this.getY(), 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
         guiGraphics.pose().pushPose();
         float scale = 96.0F;
         guiGraphics.pose().translate((float)(this.getX() + 6), (float)(this.getY() + 6), 1.0F);
         guiGraphics.pose().scale(scale, scale, scale);
         BufferSource bufferSource = Minecrft.get().renderBuffers().bufferSource();
         ExposureClient.photographRenderer().render(photograph, false, false, guiGraphics.pose(), bufferSource, 15728880);
         bufferSource.endBatch();
         guiGraphics.pose().popPose();
         if (photographStyle.hasAlbumOverlayTexture()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 2.0F);
            guiGraphics.blit(photographStyle.albumOverlayTexture(), this.getX(), this.getY(), 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
            guiGraphics.pose().popPose();
         }
      } else {
         this.hasPhotograph = false;
      }

      WidgetSprites sprites = this.hasPhotograph ? SPRITES : EMPTY_SPRITES;
      ResourceLocation resourceLocation = sprites.get(this.isActive(), this.isHoveredOrFocused());
      if (!this.editable && !this.hasPhotograph) {
         resourceLocation = sprites.get(this.isActive(), false);
      }

      guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
   }

   public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (this.editable && !this.hasPhotograph) {
         guiGraphics.renderTooltip(Minecrft.get().font, Component.translatable("gui.exposure.album.add_photograph"), mouseX, mouseY);
      } else {
         ItemStack photograph = this.getPhotograph();
         if (!photograph.isEmpty()) {
            List<Component> itemTooltip = ClientBackport.getTooltipFromItem(Minecrft.get(), photograph);
            itemTooltip.add(Component.translatable("gui.exposure.album.left_click_or_scroll_up_to_view"));
            if (this.editable) {
               itemTooltip.add(Component.translatable("gui.exposure.album.right_click_to_remove"));
            }

            if (this.isFocused()) {
               guiGraphics.renderTooltip(
                  Minecrft.get().font, Lists.transform(itemTooltip, Component::getVisualOrderText), DefaultTooltipPositioner.INSTANCE, mouseX, mouseY
               );
            } else {
               guiGraphics.renderTooltip(Minecrft.get().font, itemTooltip, Optional.empty(), mouseX, mouseY);
            }
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.active && this.visible && this.clicked(mouseX, mouseY)) {
         if (button == 0) {
            this.primaryAction.accept(this);
         } else {
            if (button != 1) {
               return false;
            }

            this.secondaryAction.accept(this);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (scrollY > 0.0 && this.clicked(mouseX, mouseY) && this.hasPhotograph) {
         this.primaryAction.accept(this);
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.active && this.visible && CommonInputs.selected(keyCode)) {
         if (Screen.hasShiftDown()) {
            this.secondaryAction.accept(this);
         } else {
            this.primaryAction.accept(this);
         }

         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      ItemStack photograph = this.getPhotograph();
      if (!photograph.isEmpty()) {
         narrationElementOutput.add(NarratedElementType.TITLE, photograph.getHoverName());
      }
   }

   public boolean inspectPhotograph() {
      ItemStack photograph = this.getPhotograph();
      if (!(photograph.getItem() instanceof PhotographItem)) {
         return false;
      } else {
         Minecrft.get().setScreen(new ChildPhotographScreen(this.parent, List.of(new ItemAndStack<>(photograph))));
         Minecrft.get()
            .getSoundManager()
            .play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), Minecrft.level().getRandom().nextFloat() * 0.2F + 1.3F, 0.75F));
         return true;
      }
   }
}
