package io.github.mortuusars.exposure.client.gui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.TutorialToast.Icons;
import net.minecraft.resources.ResourceLocation;

public interface ToastIcon {
   ToastIcon MOVEMENT_KEYS = new ToastIcon.TutorialIcon(Icons.MOVEMENT_KEYS);
   ToastIcon MOUSE = new ToastIcon.TutorialIcon(Icons.MOUSE);
   ToastIcon TREE = new ToastIcon.TutorialIcon(Icons.TREE);
   ToastIcon RECIPE_BOOK = new ToastIcon.TutorialIcon(Icons.RECIPE_BOOK);
   ToastIcon WOODEN_PLANKS = new ToastIcon.TutorialIcon(Icons.WOODEN_PLANKS);
   ToastIcon SOCIAL_INTERACTIONS = new ToastIcon.TutorialIcon(Icons.SOCIAL_INTERACTIONS);
   ToastIcon RIGHT_CLICK = new ToastIcon.TutorialIcon(Icons.RIGHT_CLICK);
   ToastIcon HOVER = new ToastIcon.SpriteIcon(Exposure.resource("toast/hover"));
   ToastIcon F1 = new ToastIcon.SpriteIcon(Exposure.resource("toast/f1"));
   ToastIcon HEADS_UP = new ToastIcon.SpriteIcon(Exposure.resource("toast/heads_up"));

   void render(GuiGraphics var1, int var2, int var3);

   public static class SpriteIcon implements ToastIcon {
      protected final ResourceLocation sprite;

      public SpriteIcon(ResourceLocation sprite) {
         this.sprite = sprite;
      }

      @Override
      public void render(GuiGraphics guiGraphics, int x, int y) {
         RenderSystem.enableBlend();
         guiGraphics.blitSprite(this.sprite, x, y, 20, 20);
      }
   }

   public static class TutorialIcon implements ToastIcon {
      protected final Icons icon;

      public TutorialIcon(Icons icon) {
         this.icon = icon;
      }

      @Override
      public void render(GuiGraphics guiGraphics, int x, int y) {
         // 1.19.2 icons draw from the toast sheet, which the caller binds
         RenderSystem.setShaderTexture(0, new net.minecraft.resources.ResourceLocation("textures/gui/toasts.png"));
         this.icon.render(guiGraphics.pose(), net.minecraft.client.Minecraft.getInstance().gui, x, y);
      }
   }
}
