package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class PathCaptureTask extends Task<Result<Image>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final TranslatableError ERROR_PATH_EMPTY = new TranslatableError("error.exposure.capture.path.path_empty", "ERR_PATH_EMPTY");
   public static final TranslatableError ERROR_PATH_INVALID = new TranslatableError("error.exposure.capture.path.path_invalid", "ERR_PATH_INVALID");
   protected final String path;

   public PathCaptureTask(String path) {
      this.path = path;
   }

   @Override
   public CompletableFuture<Result<Image>> execute() {
      if (org.apache.commons.lang3.StringUtils.isBlank(this.path)) {
         return CompletableFuture.completedFuture(Result.error(ERROR_PATH_EMPTY));
      } else if (hasHttpPrefix(this.path)) {
         try {
            URI uri = new URI(this.path);
            return new UrlCaptureTask(uri).execute();
         } catch (Exception var2) {
            LOGGER.error("Path '{}' has http/s prefix, but is not valid. Error: {}", this.path, var2.getMessage());
            return CompletableFuture.completedFuture(Result.error(ERROR_PATH_INVALID));
         }
      } else if (looksLikeURL(this.path)) {
         LOGGER.error("Path '{}' looks like a URL, but does not have http/s prefix.", this.path);
         return CompletableFuture.completedFuture(Result.error(UrlCaptureTask.ERROR_NO_HTTP_PREFIX));
      } else if (isValidFilePath(this.path)) {
         return new FileCaptureTask(new File(this.path)).execute();
      } else {
         LOGGER.error("Path '{}' cannot be handled properly.", this.path);
         return CompletableFuture.completedFuture(Result.error(ERROR_PATH_INVALID));
      }
   }

   private static boolean hasHttpPrefix(String path) {
      return path.startsWith("https://") || path.startsWith("http://");
   }

   private static boolean looksLikeURL(String path) {
      if (hasHttpPrefix(path)) {
         return true;
      } else {
         for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == ':' || c == '/' || c == '\\') {
               return false;
            }

            if (c == '.') {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isValidFilePath(String filePath) {
      try {
         Paths.get(filePath);
         return true;
      } catch (NullPointerException | InvalidPathException var2) {
         return false;
      }
   }
}
