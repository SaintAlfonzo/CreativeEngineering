package io.github.mortuusars.exposure.client.gui.toast;

import java.util.function.Supplier;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BetterTutorialToast implements Toast {
   private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("toast/tutorial");
   public static final int DEFAULT_SHOW_DURATION_MS = 7000;
   public ResourceLocation backgroundSprite = BACKGROUND_SPRITE;
   public Runnable onHide = () -> {
   };
   protected final ToastIcon icon;
   protected final Component title;
   @Nullable
   protected final Component message;
   protected Visibility visibility = Visibility.SHOW;
   protected final long shownAt = System.currentTimeMillis();
   protected final int showDurationMs;
   protected final Supplier<Boolean> hideIf;

   public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, int showDurationMs, Supplier<Boolean> hideIf) {
      this.icon = icon;
      this.title = title;
      this.message = message;
      this.showDurationMs = showDurationMs;
      this.hideIf = hideIf;
   }

   public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, int showDurationMs) {
      this.icon = icon;
      this.title = title;
      this.message = message;
      this.showDurationMs = showDurationMs;
      this.hideIf = () -> false;
   }

   public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, Supplier<Boolean> hideIf) {
      this.icon = icon;
      this.title = title;
      this.message = message;
      this.showDurationMs = -1;
      this.hideIf = hideIf;
   }

   public void hide() {
      this.visibility = Visibility.HIDE;
   }

   /** 1.19.2 toast entry point. */
   @NotNull
   public Visibility render(@NotNull PoseStack poseStack, @NotNull ToastComponent toastComponent, long timeSinceLastVisible) {
      return this.render(new GuiGraphics(poseStack), toastComponent, timeSinceLastVisible);
   }

   @NotNull
   public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
      if (this.visibility == Visibility.SHOW && this.hideIf.get()
         || this.showDurationMs > 0 && System.currentTimeMillis() > this.shownAt + (long)this.showDurationMs) {
         this.hide();
         this.onHide.run();
      }

      guiGraphics.blitSprite(this.backgroundSprite, 0, 0, this.width(), this.height());
      this.icon.render(guiGraphics, 6, 6);
      if (this.message == null) {
         guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 12, -11534256, false);
      } else {
         guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, -11534256, false);
         guiGraphics.drawString(toastComponent.getMinecraft().font, this.message, 30, 18, -16777216, false);
      }

      return this.visibility;
   }
}
