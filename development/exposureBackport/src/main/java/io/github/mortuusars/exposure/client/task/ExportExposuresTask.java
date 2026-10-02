package io.github.mortuusars.exposure.client.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.export.ImageExporter;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ExportExposuresTask extends Task<Result<Boolean>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private static ExportExposuresTask currentTask;
   protected final List<String> ids;
   protected final ExportSize size;
   protected final ExportLook look;
   @Nullable
   protected CompletableFuture<Result<Boolean>> future;
   protected boolean stop;

   public ExportExposuresTask(List<String> ids, ExportSize size, ExportLook look) {
      this.ids = ids;
      this.size = size;
      this.look = look;
   }

   public static boolean isRunning() {
      return currentTask != null;
   }

   public static boolean start(List<String> ids, ExportSize size, ExportLook look) {
      if (isRunning()) {
         Minecrft.player().displayClientMessage(Component.translatable("task.exposure.export.already_running"), false);
         return false;
      } else {
         ExposureClient.cycles().addParallelTask(new ExportExposuresTask(ids, size, look));
         return true;
      }
   }

   public static boolean stopCurrentTask() {
      if (currentTask != null) {
         currentTask.stop = true;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public CompletableFuture<Result<Boolean>> execute() {
      currentTask = this;
      if (this.future == null) {
         this.future = CompletableFuture.supplyAsync(() -> {
            Result var1;
            try {
               var1 = this.export();
            } finally {
               currentTask = null;
            }

            return var1;
         });
         this.setStarted();
      }

      return this.future;
   }

   protected Result<Boolean> export() {
      int total = this.ids.size();
      AtomicInteger exported = new AtomicInteger();
      this.print(Component.translatable("task.exposure.export.started", new Object[]{total}));

      for (String id : this.ids) {
         if (this.stop) {
            this.print(Component.translatable("task.exposure.export.stopped"));
            return Result.success(false);
         }

         ExposureData exposure = null;

         for (int attempt = 0; attempt < 50; attempt++) {
            this.updateStatus(Component.translatable("task.exposure.export.status.requesting", new Object[]{id, exported, total}));
            RequestedPalettedExposure request = ExposureClient.exposureStore().getOrRequest(id);
            if (request.getData().isPresent()) {
               exposure = request.getData().get();
               break;
            }

            try {
               Thread.sleep(100L);
            } catch (InterruptedException var9) {
            }
         }

         if (exposure == null) {
            this.print(Component.translatable("task.exposure.export.error.not_received", new Object[]{id}));
         } else {
            this.updateStatus(Component.translatable("task.exposure.export.status.exporting", new Object[]{id, exported, total}));

            try {
               String fileName = id + this.look.getIdSuffix();
               new ImageExporter(exposure, fileName)
                  .modify(ImageEffect.chain(this.look.getModifier(), ImageEffect.Resize.multiplier(this.size.getMultiplier())))
                  .toExposuresFolder()
                  .organizeByWorld((Boolean)Config.Client.EXPORT_ORGANIZE_BY_WORLD.get())
                  .setCreationDate(exposure.getTag().unixTimestamp())
                  .onExport(
                     file -> {
                        exported.getAndIncrement();
                        this.print(
                           Component.translatable("task.exposure.export.exported")
                              .append("'" + fileName + "'")
                              .withStyle(
                                 Style.EMPTY
                                    .withUnderlined(true)
                                    .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.literal("Open")))
                                    .withClickEvent(new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                              )
                        );
                     }
                  )
                  .export();
            } catch (Exception var10) {
               this.print(Component.translatable("task.exposure.export.error", new Object[]{id, var10.getMessage()}));
               LOGGER.error("Failed to export exposure '{}': ", id, var10);
            }
         }
      }

      if (exported.get() > 0) {
         this.print(Component.translatable("task.exposure.export.result", new Object[]{total}));
      } else {
         this.print(Component.translatable("task.exposure.export.none_exported"));
      }

      this.updateStatus(Component.empty());
      return Result.success(true);
   }

   protected void print(MutableComponent message) {
      Minecrft.execute(() -> Minecrft.player().displayClientMessage(message, false));
   }

   protected void updateStatus(MutableComponent status) {
      Minecrft.execute(() -> Minecrft.player().displayClientMessage(status, true));
   }
}
