package io.github.mortuusars.exposure.client.capture.task;

import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.capture.task.file.ImageFileLoader;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class FileCaptureTask extends Task<Result<Image>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final TranslatableError ERROR_PATH_EMPTY = new TranslatableError("error.exposure.capture.file.path_empty", "ERR_PATH_EMPTY");
   public static final TranslatableError ERROR_PATH_INVALID = new TranslatableError("error.exposure.capture.file.path_invalid", "ERR_PATH_INVALID");
   public static final TranslatableError ERROR_NO_EXTENSION = new TranslatableError("error.exposure.capture.file.no_extension", "ERR_NO_EXTENSION");
   public static final TranslatableError ERROR_PATH_IS_DIRECTORY = new TranslatableError(
      "error.exposure.capture.file.path_is_directory", "ERR_PATH_IS_DIRECTORY"
   );
   public static final TranslatableError ERROR_FILE_DOES_NOT_EXIST = new TranslatableError(
      "error.exposure.capture.file.file_does_not_exist", "ERR_FILE_DOES_NOT_EXIST"
   );
   public static final TranslatableError ERROR_CANNOT_READ = new TranslatableError("error.exposure.capture.file.cannot_read", "ERR_CANNOT_READ");
   public static final TranslatableError ERROR_NOT_SUPPORTED = new TranslatableError("error.exposure.capture.file.not_supported", "ERR_NOT_SUPPORTED");
   public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.capture.file.timed_out", "ERR_TIMED_OUT");
   static final Path BASE_DIR = Minecrft.get().gameDirectory.toPath().toAbsolutePath().normalize();
   protected final File file;
   protected final CompletableFuture<Result<Image>> future = new CompletableFuture<>();

   public FileCaptureTask(File file) {
      this.file = file;
   }

   public File getFile() {
      return this.file;
   }

   @Override
   public CompletableFuture<Result<Image>> execute() {
      return this.future.completeAsync(() -> {
         LOGGER.info("Attempting to load image from file: '{}'", this.file.toString());
         Result<File> result = findFileWithExtension(this.file);
         if (result.isError()) {
            return result.remapError();
         } else {
            result = validateFilepath(result.getValue());
            if (result.isError()) {
               return result.remapError();
            } else {
               File file = result.getValue();
               LOGGER.info("Reading image from file: '{}'", file);
               return ImageFileLoader.chooseFitting(file).load(file);
            }
         }
      }).completeOnTimeout(Result.error(ERROR_TIMED_OUT), (long)((Integer)Config.Server.PROJECT_TIMEOUT_TICKS.get() * 50), TimeUnit.MILLISECONDS);
   }

   private static Result<File> validateFilepath(File file) {
      String filepath = file.getPath();
      if (StringUtil.isNullOrEmpty(filepath)) {
         return Result.error(ERROR_PATH_EMPTY);
      } else {
         if (!Minecrft.get().hasSingleplayerServer() && (Boolean)Config.Client.FILE_LOADING_ONLY_RELATIVE_TO_EXPOSURES_DIR.get()) {
            if (file.isAbsolute()) {
               LOGGER.error("Absolute paths are not supported. Path: {}", filepath);
               LOGGER.info("Place image into a game directory and use a relative path.");
               Minecrft.player()
                  .displayClientMessage(
                     Component.literal("Absolute path is not allowed for image projecting. Loading is allowed only from a game directory.")
                        .withStyle(ChatFormatting.RED),
                     false
                  );
               return Result.error(ERROR_PATH_INVALID);
            }

            Path path = BASE_DIR.resolve(filepath).normalize();
            if (!path.startsWith(BASE_DIR)) {
               return Result.error(ERROR_PATH_INVALID);
            }

            file = path.toFile();
         }

         if (file.isDirectory()) {
            return Result.error(ERROR_PATH_IS_DIRECTORY);
         } else {
            String extension = Files.getFileExtension(file.getAbsolutePath());
            if (StringUtil.isNullOrEmpty(extension)) {
               return Result.error(ERROR_NO_EXTENSION);
            } else {
               return !file.exists() ? Result.error(ERROR_FILE_DOES_NOT_EXIST) : Result.success(file);
            }
         }
      }
   }

   private static Result<File> findFileWithExtension(File file) {
      String extension = Files.getFileExtension(file.getAbsolutePath());
      if (!StringUtil.isNullOrEmpty(extension)) {
         return Result.success(file);
      } else {
         File parentFile = file.getParentFile();
         if (parentFile == null) {
            return Result.error(ERROR_PATH_INVALID);
         } else {
            File[] files = parentFile.listFiles();
            if (files == null) {
               return Result.error(ERROR_CANNOT_READ);
            } else {
               String name = file.getName();

               for (File fileInDirectory : files) {
                  if (!fileInDirectory.isDirectory()) {
                     String fileName = Files.getNameWithoutExtension(fileInDirectory.getName());
                     if (fileName.equals(name)) {
                        return Result.success(fileInDirectory);
                     }
                  }
               }

               return Result.error(ERROR_FILE_DOES_NOT_EXIST);
            }
         }
      }
   }
}
