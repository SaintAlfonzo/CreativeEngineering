package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DirectScreenshotCaptureTask extends Task<Result<Image>> {
   protected int delay = Math.max(1, (Integer)Config.Client.DIRECT_CAPTURE_DELAY_FRAMES.get());
   @Nullable
   protected CompletableFuture<Result<Image>> future;

   @NotNull
   @Override
   public CompletableFuture<Result<Image>> execute() {
      if (this.future == null) {
         this.future = new CompletableFuture<>();
      }

      return this.future;
   }

   @Override
   public void tick() {
      if (this.future != null && !this.future.isDone()) {
         if (this.delay <= 0) {
            try {
               NativeImage nativeImage = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
               this.future.complete(Result.success(new WrappedNativeImage(nativeImage)));
            } catch (Exception var2) {
               this.future.completeExceptionally(var2);
            }
         }

         this.delay--;
      }
   }
}
