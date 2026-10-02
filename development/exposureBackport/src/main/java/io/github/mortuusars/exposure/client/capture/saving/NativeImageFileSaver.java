package io.github.mortuusars.exposure.client.capture.saving;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.color.Color;
import java.io.File;
import org.slf4j.Logger;

public class NativeImageFileSaver {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final File file;

   public NativeImageFileSaver(File file) {
      this.file = file;
   }

   public NativeImageFileSaver(String filePath) {
      this.file = new File(filePath);
   }

   public void save(Image image) {
      try {
         NativeImage nativeImage = new NativeImage(image.width(), image.height(), false);

         try {
            for (int y = 0; y < image.height(); y++) {
               for (int x = 0; x < image.width(); x++) {
                  int pixelColor = image.getPixelARGB(x, y);
                  nativeImage.setPixelRGBA(x, y, Color.ARGBtoABGR(pixelColor));
               }
            }

            boolean ignored = this.file.getParentFile().mkdirs();
            nativeImage.writeToFile(this.file);
            LOGGER.info("Saved image: {}", this.file);
         } catch (Throwable var7) {
            try {
               nativeImage.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         nativeImage.close();
      } catch (Exception var8) {
         LOGGER.error("Failed to save image to file: {}", var8.toString());
      }
   }
}
