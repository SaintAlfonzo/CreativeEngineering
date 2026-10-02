package io.github.mortuusars.exposure.client.capture.task.file;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.capture.task.FileCaptureTask;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;

public class BufferedImageFileLoader implements ImageFileLoader {
   private static final Logger LOGGER = LogUtils.getLogger();

   @Override
   public Result<Image> load(File file) {
      try {
         Result var4;
         try (FileInputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image.getWidth() > 10000 || image.getHeight() > 10000) {
               LOGGER.error("Cannot load image from file '{}': image is too large.", file);
               return Result.error(FileCaptureTask.ERROR_CANNOT_READ);
            }

            var4 = Result.success(new WrappedBufferedImage(image));
         }

         return var4;
      } catch (IOException var7) {
         Exposure.LOGGER.error("Loading image from file path '{}' failed:", file, var7);
         return Result.error(FileCaptureTask.ERROR_CANNOT_READ);
      }
   }
}
