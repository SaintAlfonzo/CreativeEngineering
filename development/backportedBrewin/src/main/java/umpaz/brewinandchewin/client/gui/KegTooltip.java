package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.item.KegTooltipComponent;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import vectorwing.farmersdelight.common.utility.TextUtils;

/** The keg item's tooltip: servings, then the drink's icon and name. Port: 1.19.2 tooltip component API. */
public class KegTooltip implements ClientTooltipComponent {
   private static final int ITEM_SIZE = 16;
   private static final int MARGIN = 4;
   private final int textSpacing = 9 + 1;
   private final AbstractedFluidStack mealStack;

   public KegTooltip(KegTooltipComponent tooltip) {
      this.mealStack = tooltip.stack();
   }

   private MutableComponent servings() {
      long serving = FluidUnit.MILLIBUCKET.convertToLoader(250L);
      return this.mealStack.amount() == serving
         ? TextUtils.getTranslation("tooltip.cooking_pot.single_serving")
         : TextUtils.getTranslation("tooltip.cooking_pot.many_servings", this.mealStack.amount() / serving);
   }

   @Override
   public int getHeight() {
      return this.mealStack.isEmpty() ? this.textSpacing : this.textSpacing + ITEM_SIZE;
   }

   @Override
   public int getWidth(Font font) {
      if (!this.mealStack.isEmpty()) {
         return Math.max(font.width(this.servings()), font.width(BrewinAndChewin.getHelper().getFluidDisplayName(this.mealStack)) + 20);
      } else {
         return font.width(TextUtils.getTranslation("tooltip.cooking_pot.empty"));
      }
   }

   @Override
   public void renderImage(Font font, int mouseX, int mouseY, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
      if (!this.mealStack.isEmpty()) {
         ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(this.mealStack);
         itemRenderer.renderAndDecorateItem(itemDisplay, mouseX, mouseY + this.textSpacing - 1, blitOffset);
      }
   }

   @Override
   public void renderText(Font font, int x, int y, Matrix4f matrix4f, BufferSource bufferSource) {
      Integer color = ChatFormatting.GRAY.getColor();
      int gray = color == null ? -1 : color;
      if (!this.mealStack.isEmpty()) {
         font.drawInBatch(this.servings(), (float)x, (float)y, gray, true, matrix4f, bufferSource, false, 0, 15728880);
         font.drawInBatch(
            BrewinAndChewin.getHelper().getFluidDisplayName(this.mealStack), (float)(x + ITEM_SIZE + MARGIN), (float)(y + this.textSpacing + MARGIN), -1,
            true, matrix4f, bufferSource, false, 0, 15728880
         );
      } else {
         font.drawInBatch(TextUtils.getTranslation("tooltip.cooking_pot.empty"), (float)x, (float)y, gray, true, matrix4f, bufferSource, false, 0, 15728880);
      }
   }
}
