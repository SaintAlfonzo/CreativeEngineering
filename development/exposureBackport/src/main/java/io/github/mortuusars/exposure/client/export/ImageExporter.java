package io.github.mortuusars.exposure.client.export;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.util.LevelNameGetter;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class ImageExporter {
   protected final Image image;
   protected final String fileName;
   protected String folder = "exposures";
   @Nullable
   protected String worldName = null;
   protected long creationUnixTimestamp = 0L;
   protected Function<Image, Image> imageModifier = Function.identity();
   protected Consumer<File> onExport = f -> {
   };

   public ImageExporter(Image image, String fileName) {
      this.image = image;
      this.fileName = fileName;
   }

   public ImageExporter(ExposureData exposure, String fileName) {
      this(PalettedImage.fromExposure(exposure), fileName);
   }

   public Function<Image, Image> getModifier() {
      return this.imageModifier;
   }

   public String getFileName() {
      return this.fileName;
   }

   public String getFolder() {
      return this.folder;
   }

   @Nullable
   public String getWorldSubfolder() {
      return this.worldName;
   }

   public long getCreationUnixTimestamp() {
      return this.creationUnixTimestamp;
   }

   public ImageExporter modify(Function<Image, Image> imageModifier) {
      this.imageModifier = imageModifier;
      return this;
   }

   public ImageExporter withFolder(String folder) {
      this.folder = folder;
      return this;
   }

   public ImageExporter toExposuresFolder() {
      this.folder = "exposures";
      return this;
   }

   public ImageExporter organizeByWorld(@Nullable String worldName) {
      this.worldName = worldName;
      return this;
   }

   public ImageExporter organizeByWorld(boolean organize) {
      this.worldName = organize ? LevelNameGetter.getWorldName() : null;
      return this;
   }

   public ImageExporter setCreationDate(long unixTimestamp) {
      this.creationUnixTimestamp = unixTimestamp;
      return this;
   }

   public void export() {
      this.save(this.imageModifier.apply(this.image));
   }

   protected boolean save(Image image) {
      try {
         NativeImage nativeImage = this.convertToNativeImage(image);

         boolean var6;
         try {
            String filepath = this.getFolder() + "/" + (this.getWorldSubfolder() != null ? this.getWorldSubfolder() + "/" : "") + this.getFileName() + ".png";
            File outputFile = new File(filepath);
            boolean ignored = outputFile.getParentFile().mkdirs();
            nativeImage.writeToFile(outputFile);
            if (this.creationUnixTimestamp > 0L) {
               this.trySetFileCreationDate(outputFile.getAbsolutePath(), this.creationUnixTimestamp);
            }

            this.onExport.accept(outputFile);
            Exposure.LOGGER.info("Exposure saved: {}", outputFile);
            var6 = true;
         } catch (Throwable var8) {
            if (nativeImage != null) {
               try {
                  nativeImage.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (nativeImage != null) {
            nativeImage.close();
         }

         return var6;
      } catch (Exception var9) {
         Exposure.LOGGER.error("Failed to save exposure to file: {}", var9.toString());
         return false;
      }
   }

   protected NativeImage convertToNativeImage(Image image) {
      NativeImage nativeImage = new NativeImage(image.width(), image.height(), false);

      for (int x = 0; x < nativeImage.getWidth(); x++) {
         for (int y = 0; y < nativeImage.getHeight(); y++) {
            nativeImage.setPixelRGBA(x, y, Color.ABGRtoARGB(image.getPixelARGB(x, y)));
         }
      }

      return nativeImage;
   }

   protected void trySetFileCreationDate(String filePath, long creationTimeUnixSeconds) {
      try {
         Date creationDate = Date.from(Instant.ofEpochSecond(creationTimeUnixSeconds));
         BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(filePath), BasicFileAttributeView.class);
         FileTime creationTime = FileTime.fromMillis(creationDate.getTime());
         FileTime modifyTime = FileTime.fromMillis(System.currentTimeMillis());
         attributes.setTimes(modifyTime, modifyTime, creationTime);
      } catch (Exception var8) {
      }
   }

   public ImageExporter onExport(Consumer<File> onExport) {
      this.onExport = onExport;
      return this;
   }
}
