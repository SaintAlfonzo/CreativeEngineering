package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;
import io.github.mortuusars.exposure.backport.util.Backport;
import io.github.mortuusars.exposure.backport.client.ClientBackport;
import io.github.mortuusars.exposure.backport.gui.GuiRenderTypes;

public class ItemListScreen extends BackportScreen {
   public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/item_list.png");
   protected final Screen parent;
   protected final List<ItemStack> items;
   protected final int rowsCount;
   protected final Animation openingAnimation;
   protected int imageWidth = 176;
   protected int imageHeight = 166;
   protected int titleLabelX = 8;
   protected int titleLabelY = 6;
   protected int leftPos;
   protected int topPos;
   @Nullable
   protected Slot hoveredSlot;
   protected List<Slot> slots = new ArrayList<>();
   protected long openedAt;

   public ItemListScreen(Screen parent, Component title, List<ItemStack> items) {
      super(title);
      this.parent = parent;
      this.items = items;
      List<List<ItemStack>> rows = Lists.partition(items, 9);
      this.rowsCount = rows.size();
      this.openingAnimation = new Animation(200, EasingFunction.EASE_OUT_EXPO);
      this.openedAt = Util.getMillis();
      SimpleContainer container = new SimpleContainer(items.toArray(ItemStack[]::new));
      int rowX = 8;
      int rowY = 18;

      for (int row = 0; row < rows.size(); row++) {
         List<ItemStack> stacks = rows.get(row);
         int rowXToCenterOffset = (162 - stacks.size() * 18) / 2;

         for (int column = 0; column < stacks.size(); column++) {
            int slotIndex = row * 9 + column;
            this.slots.add(new Slot(container, slotIndex, rowX + rowXToCenterOffset + column * 18, rowY) {
               public boolean mayPlace(ItemStack stack) {
                  return false;
               }

               public boolean mayPickup(Player player) {
                  return false;
               }
            });
         }

         rowY += 18;
      }

      Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 1.0F));
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void init() {
      this.imageWidth = 176;
      this.imageHeight = 24 + this.rowsCount * 18;
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      int left = this.leftPos;
      int top = this.topPos;
      this.renderTransparentBackground(guiGraphics);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate((float)this.width / 2.0F, (float)this.height / 2.0F, 0.0F);
      float animProgress = (float)this.openingAnimation.getValue();
      guiGraphics.pose().scale(animProgress, animProgress, animProgress);
      guiGraphics.pose().translate(-((float)this.width / 2.0F), -((float)this.height / 2.0F), 0.0F);
      this.renderBg(guiGraphics, mouseX, mouseY, partialTick);
      RenderSystem.disableDepthTest();
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate((float)left, (float)top, 0.0F);
      this.hoveredSlot = null;

      for (Slot slot : this.slots) {
         if (slot.isActive()) {
            this.renderSlot(guiGraphics, slot);
         }

         if (this.isHovering(slot, (double)mouseX, (double)mouseY) && slot.isActive()) {
            this.hoveredSlot = slot;
            if (this.hoveredSlot.isActive()) {
               renderSlotHighlight(guiGraphics, slot.x, slot.y, 0);
            }
         }
      }

      this.renderLabels(guiGraphics, mouseX, mouseY);
      guiGraphics.pose().popPose();
      RenderSystem.enableDepthTest();
      guiGraphics.pose().popPose();
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   protected void renderBg(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, 17);

      for (int i = 0; i < this.rowsCount; i++) {
         guiGraphics.blit(TEXTURE, this.leftPos, this.topPos + 17 + i * 18, 0, 17, this.imageWidth, 18);
      }

      guiGraphics.blit(TEXTURE, this.leftPos, this.topPos + 17 + this.rowsCount * 18, 0, 35, this.imageWidth, 7);

      for (Slot slot : this.slots) {
         guiGraphics.blit(TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 176, 0, 18, 18);
      }
   }

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }

   protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
      int x = slot.x;
      int y = slot.y;
      ItemStack itemStack = slot.getItem();
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
      guiGraphics.renderItem(itemStack, x, y, slot.x + slot.y * this.imageWidth);
      guiGraphics.renderItemDecorations(this.font, itemStack, x, y, null);
      guiGraphics.pose().popPose();
   }

   public static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset) {
      guiGraphics.fillGradient(GuiRenderTypes.guiOverlay(), x, y, x + 16, y + 16, -2130706433, -2130706433, blitOffset);
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
         ItemStack itemStack = this.hoveredSlot.getItem();
         List<Component> tooltipLines = this.parent instanceof AbstractContainerScreen<?> abstractContainerScreen
            ? abstractContainerScreen.getTooltipFromItem(itemStack)
            : ClientBackport.getTooltipFromItem(Minecrft.get(), itemStack);
         guiGraphics.renderTooltip(this.font, tooltipLines, itemStack.getTooltipImage(), x, y);
      }
   }

   protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
      return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
   }

   protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
      int i = this.leftPos;
      int j = this.topPos;
      double var11;
      double var12;
      return (var11 = mouseX - (double)i) >= (double)(x - 1)
         && var11 < (double)(x + width + 1)
         && (var12 = mouseY - (double)j) >= (double)(y - 1)
         && var12 < (double)(y + height + 1);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (Minecrft.options().keyInventory.matches(keyCode, scanCode)) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else if (!this.isHovering(0, 0, this.imageWidth, this.imageHeight, mouseX, mouseY)) {
         this.onClose();
         return true;
      } else {
         return false;
      }
   }

   public void onClose() {
      Minecrft.get().setScreen(this.parent);
   }
}
