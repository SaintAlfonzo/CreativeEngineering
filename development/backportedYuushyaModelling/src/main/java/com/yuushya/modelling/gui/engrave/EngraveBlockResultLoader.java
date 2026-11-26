// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/engrave/EngraveBlockResultLoader.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.engrave;

import com.yuushya.modelling.Yuushya;
import com.yuushya.modelling.network.TransformDataListPacket;
import com.yuushya.modelling.utils.ClientMethod;
import com.yuushya.modelling.utils.ShareUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class EngraveBlockResultLoader {
   public static final Map<String, EngraveBlockResult> SHOWBLOCK_ITEM_MAP = new HashMap<>();

   public static void load() {
      if (Files.exists(ClientMethod.BLOCK_PATH)) {
         try {
            load(ClientMethod.BLOCK_PATH);
         } catch (IOException var1) {
            Yuushya.LOGGER.error(var1);
         }
      }
   }

   public static boolean isZip(Path basePath) {
      return basePath.toString().endsWith(".zip");
   }

   private static void loadZip(Path path) {
      try (FileSystem fileSystem = FileSystems.newFileSystem(path)) {
         load(fileSystem.getPath("."));
      } catch (IOException var6) {
         Yuushya.LOGGER.error(var6);
      }
   }

   private static void load(final Path path) throws IOException {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (EngraveBlockResultLoader.isZip(file)) {
               EngraveBlockResultLoader.loadZip(file);
            } else if (file.getFileName().toString().endsWith(".json")) {
               String name = path.relativize(file).toString().replaceAll(".json", "");
               String fileString = Files.readString(file);

               try {
                  ShareUtils.ShareBlockInformation information = ShareUtils.from(fileString);
                  EngraveBlockResultLoader.SHOWBLOCK_ITEM_MAP.put(name, new EngraveBlockResult(name, information));
               } catch (Exception var6) {
                  Yuushya.LOGGER.error(var6);
               }
            }

            return FileVisitResult.CONTINUE;
         }
      });
   }

   public static void saveBlock(String string, String name) throws IOException {
      ShareUtils.ShareBlockInformation information = ShareUtils.from(string);
      SHOWBLOCK_ITEM_MAP.put(name, new EngraveBlockResult(name, information));
      TransformDataListPacket.updateSendingCache(name);
      Path out = ClientMethod.BLOCK_PATH.resolve(name + ".json");
      if (!Files.exists(out)) {
         if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
         }

         Files.createFile(out);
      }

      if (Files.exists(out)) {
         Files.writeString(out, string, StandardCharsets.UTF_8);
      }
   }
}
