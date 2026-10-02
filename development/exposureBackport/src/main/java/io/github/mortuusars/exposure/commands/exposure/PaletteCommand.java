package io.github.mortuusars.exposure.commands.exposure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.commands.argument.ColorPaletteArgument;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.color.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public class PaletteCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("palette")
            .then(
               Commands.literal("export")
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("palette", new ColorPaletteArgument())
                           .then(
                              Commands.literal("json")
                                 .then(
                                    Commands.argument("file_path", StringArgumentType.string())
                                       .executes(
                                          context -> exportAsJson(
                                                (CommandSourceStack)context.getSource(),
                                                ColorPaletteArgument.getId(context, "palette"),
                                                StringArgumentType.getString(context, "file_path")
                                             )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("png")
                              .then(
                                 Commands.argument("file_path", StringArgumentType.string())
                                    .executes(
                                       context -> exportAsPng(
                                             (CommandSourceStack)context.getSource(),
                                             ColorPaletteArgument.getId(context, "palette"),
                                             StringArgumentType.getString(context, "file_path")
                                          )
                                    )
                              )
                        )
                  )
            ))
         .then(
            ((LiteralArgumentBuilder)Commands.literal("convert")
                  .then(
                     Commands.literal("json_to_png")
                        .then(
                           Commands.argument("file_path", StringArgumentType.string())
                              .executes(
                                 context -> convertJsonToPng((CommandSourceStack)context.getSource(), StringArgumentType.getString(context, "file_path"))
                              )
                        )
                  ))
               .then(
                  Commands.literal("png_to_json")
                     .then(
                        Commands.argument("file_path", StringArgumentType.string())
                           .executes(context -> convertPngToJson((CommandSourceStack)context.getSource(), StringArgumentType.getString(context, "file_path")))
                     )
               )
         );
   }

   private static int exportAsJson(CommandSourceStack source, ResourceLocation paletteId, String filePath) {
      ColorPalette palette = (ColorPalette)source.registryAccess().registryOrThrow(Exposure.Registries.COLOR_PALETTE).get(paletteId);
      if (palette == null) {
         source.sendFailure(Component.literal(paletteId + " is not found."));
         return 0;
      } else {
         try {
            savePaletteAsJson(palette, new File(filePath));
         } catch (Exception var5) {
            Exposure.LOGGER.error("Exporting palette '{}' failed: ", paletteId, var5);
            source.sendFailure(Component.literal("Exporting palette '" + paletteId + "' failed: " + var5.getMessage()));
            return 0;
         }

         source.sendSuccess(Component.literal("Exported palette '" + paletteId + "' to file ")
                  .append(Component.literal(filePath).withStyle(Style.EMPTY.withUnderlined(true))),
            true
         );
         return 0;
      }
   }

   private static int exportAsPng(CommandSourceStack source, ResourceLocation paletteId, String filePath) {
      ColorPalette palette = (ColorPalette)source.registryAccess().registryOrThrow(Exposure.Registries.COLOR_PALETTE).get(paletteId);
      if (palette == null) {
         source.sendFailure(Component.literal(paletteId + " is not found."));
         return 0;
      } else {
         try {
            savePaletteAsPng(palette, new File(filePath));
            source.sendSuccess(Component.literal("Exported palette '" + paletteId + "' to file ")
                     .append(Component.literal(filePath).withStyle(Style.EMPTY.withUnderlined(true))),
               true
            );
         } catch (Exception var5) {
            Exposure.LOGGER.error("Exporting palette '{}' failed: ", paletteId, var5);
            source.sendFailure(Component.literal("Exporting palette '" + paletteId + "' failed: " + var5.getMessage()));
         }

         return 0;
      }
   }

   private static int convertJsonToPng(CommandSourceStack source, String filePath) {
      try {
         File file = new File(filePath);
         ColorPalette palette = loadPaletteFromJson(file);
         savePaletteAsPng(palette, replaceExtension(file, "png"));
         source.sendSuccess(Component.literal("Converted palette '" + filePath + "' to png."), true);
      } catch (Exception var4) {
         Exposure.LOGGER.error("Converting palette '{}' failed: ", filePath, var4);
         source.sendFailure(Component.literal("Converting palette '" + filePath + "' failed: " + var4.getMessage()));
      }

      return 0;
   }

   private static int convertPngToJson(CommandSourceStack source, String filePath) {
      try {
         File file = new File(filePath);
         BufferedImage image = ImageIO.read(file);
         Integer[] colors = new Integer[256];
         int addedCount = 0;

         for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth() && addedCount <= 255; x++) {
               int color = image.getRGB(x, y);
               if (Color.alpha(color) == 0) {
                  color = Color.TRANSPARENT.getARGB();
               }

               boolean alreadyAdded = false;
               Integer[] var10 = colors;
               int var11 = colors.length;
               int var12 = 0;

               while (true) {
                  if (var12 < var11) {
                     Integer addedColor = var10[var12];
                     if (addedColor == null || !addedColor.equals(color)) {
                        var12++;
                        continue;
                     }

                     alreadyAdded = true;
                  }

                  if (!alreadyAdded) {
                     colors[addedCount] = color;
                     addedCount++;
                  }
                  break;
               }
            }
         }

         for (int i = addedCount; i < colors.length - 1; i++) {
            colors[i] = Color.BLACK.getARGB();
         }

         if (colors[255] == null) {
            colors[255] = Color.TRANSPARENT.getARGB();
         }

         if (!colors[255].equals(Color.TRANSPARENT.getARGB())) {
            colors[255] = Color.TRANSPARENT.getARGB();
            source.sendFailure(Component.literal("Corrected last color to be transparent in '" + filePath + "' palette."));
         }

         ColorPalette palette = new ColorPalette(Arrays.stream(colors).mapToInt(Integer::intValue).toArray());

         try {
            savePaletteAsJson(palette, replaceExtension(file, "json"));
         } catch (Exception var14) {
            Exposure.LOGGER.error("Cannot save converted palette '{}' failed: ", filePath, var14);
            source.sendFailure(Component.literal("Cannot save converted palette '" + filePath + "' failed: " + var14.getMessage()));
            return 0;
         }
      } catch (Exception var15) {
         Exposure.LOGGER.error("Converting palette '{}' failed: ", filePath, var15);
         source.sendFailure(Component.literal("Converting palette '" + filePath + "' failed: " + var15.getMessage()));
         return 0;
      }

      source.sendSuccess(Component.literal("Converted palette '" + filePath + "' to json."), true);
      return 0;
   }

   private static void savePaletteAsJson(ColorPalette palette, File file) throws IOException {
      JsonElement jsonElement = (JsonElement)BackportCodecs.getOrThrow(ColorPalette.CODEC.encodeStart(JsonOps.INSTANCE, palette));
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(jsonElement);

      try (FileWriter writer = new FileWriter(file)) {
         writer.write(json);
      }
   }

   private static void savePaletteAsPng(ColorPalette palette, File file) throws IOException {
      BufferedImage image = new BufferedImage(16, 16, 2);
      int[] colors = palette.colors();

      for (int y = 0; y < 16; y++) {
         for (int x = 0; x < 16; x++) {
            image.setRGB(x, y, colors[y * 16 + x]);
         }
      }

      ImageIO.write(image, "png", file);
   }

   private static ColorPalette loadPaletteFromJson(File file) throws IOException {
      ColorPalette var3;
      try (FileReader reader = new FileReader(file)) {
         JsonObject jsonObject = (JsonObject)new Gson().fromJson(reader, JsonObject.class);
         var3 = (ColorPalette)BackportCodecs.getOrThrow(ColorPalette.CODEC.parse(JsonOps.INSTANCE, jsonObject));
      }

      return var3;
   }

   private static File replaceExtension(File originalFile, String newExtension) {
      String originalPath = originalFile.getPath();
      int lastDotIndex = originalPath.lastIndexOf(46);
      String newPath = lastDotIndex == -1 ? originalPath + "." + newExtension : originalPath.substring(0, lastDotIndex) + "." + newExtension;
      return new File(newPath);
   }
}
