package io.github.mortuusars.exposure.client.util;

import io.github.mortuusars.exposure.Exposure;
import java.io.File;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

public class LevelNameGetter {
   public static String getWorldName() {
      try {
         if (Minecraft.getInstance().hasSingleplayerServer()) {
            if (Minecraft.getInstance().getSingleplayerServer() != null) {
               return Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName().replace('.', '_');
            } else {
               String gameDirectory = Minecraft.getInstance().gameDirectory.getAbsolutePath();
               Path savesDir = Path.of(gameDirectory, "/saves");
               File[] dirs = savesDir.toFile().listFiles((dirx, name) -> new File(dirx, name).isDirectory());
               if (dirs != null && dirs.length != 0) {
                  File lastModified = dirs[0];

                  for (File dir : dirs) {
                     if (dir.lastModified() > lastModified.lastModified()) {
                        lastModified = dir;
                     }
                  }

                  return lastModified.getName();
               } else {
                  return "";
               }
            }
         } else {
            return Minecraft.getInstance().getCurrentServer() != null ? Minecraft.getInstance().getCurrentServer().name : "Unknown";
         }
      } catch (Exception var8) {
         Exposure.LOGGER.error("Failed to get level name: " + var8);
         return "Unknown";
      }
   }
}
