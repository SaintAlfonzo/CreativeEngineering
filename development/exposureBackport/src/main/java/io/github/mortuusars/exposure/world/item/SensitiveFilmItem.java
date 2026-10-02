package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import io.github.mortuusars.exposure.world.camera.film.properties.ColorBalance;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmStyle;
import io.github.mortuusars.exposure.world.camera.film.properties.HSB;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface SensitiveFilmItem extends FilmItem {
   @NotNull
   default FilmProperties getFilmProperties(ItemStack stack) {
      return new FilmProperties(
         this.getType(), Optional.of(this.getFrameSize(stack)), this.getColorPalette(stack), this.getDitherMode(stack), this.getFilmStyle(stack)
      );
   }

   @NotNull
   default FilmStyle getFilmStyle(ItemStack stack) {
      return (FilmStyle)Exposure.DataComponents.FILM_STYLE.getOrDefault(stack, FilmStyle.EMPTY);
   }

   default ResourceKey<ColorPalette> getColorPalette(ItemStack stack) {
      ResourceLocation location = (ResourceLocation)Exposure.DataComponents.FILM_COLOR_PALETTE.get(stack);
      return location != null ? ColorPalettes.createKey(location) : ColorPalettes.DEFAULT;
   }

   @NotNull
   default DitherMode getDitherMode(ItemStack stack) {
      return (DitherMode)Exposure.DataComponents.FILM_DITHER_MODE.getOrDefault(stack, DitherMode.DITHERED);
   }

   default void addFrameSizeToTooltip(ItemStack stack, List<Component> list) {
      int frameSize = this.getFrameSize(stack);
      if (frameSize != this.getDefaultFrameSize(stack)) {
         list.add(
            Component.translatable(
                  "item.exposure.film_roll.tooltip.frame_size", new Object[]{Component.literal(String.format("%.1f", (float)frameSize / 10.0F))}
               )
               .withStyle(ChatFormatting.GRAY)
         );
      }
   }

   default void addPaletteToTooltip(ItemStack stack, List<Component> list) {
      ResourceKey<ColorPalette> palette = this.getColorPalette(stack);
      if (!palette.equals(ColorPalettes.DEFAULT)) {
         list.add(
            Component.translatable("item.exposure.film_roll.tooltip.palette", new Object[]{palette.location().toString()}).withStyle(ChatFormatting.DARK_GRAY)
         );
      }
   }

   default void addStyleToTooltip(ItemStack stack, List<Component> list) {
      FilmStyle style = this.getFilmStyle(stack);
      if (style.sensitivity() != 0.0F) {
         list.add(Component.translatable("item.exposure.film_roll.tooltip.sensitivity", new Object[]{style.sensitivity()}).withStyle(ChatFormatting.DARK_GRAY));
      }

      if (style.contrast() != 0.0F) {
         list.add(Component.translatable("item.exposure.film_roll.tooltip.contrast", new Object[]{style.contrast()}).withStyle(ChatFormatting.DARK_GRAY));
      }

      if (style.levels() != Levels.EMPTY) {
         list.add(Component.translatable("item.exposure.film_roll.tooltip.levels", new Object[]{style.levels().toString()}).withStyle(ChatFormatting.DARK_GRAY));
      }

      if (style.hsb() != HSB.EMPTY) {
         list.add(Component.translatable("item.exposure.film_roll.tooltip.hsb", new Object[]{style.hsb().toString()}).withStyle(ChatFormatting.DARK_GRAY));
      }

      if (style.colorBalance() != ColorBalance.EMPTY) {
         list.add(
            Component.translatable("item.exposure.film_roll.tooltip.color_balance", new Object[]{style.colorBalance().toString()})
               .withStyle(ChatFormatting.DARK_GRAY)
         );
      }

      if (style.noise() != 0.0F) {
         list.add(Component.translatable("item.exposure.film_roll.tooltip.noise", new Object[]{style.noise()}).withStyle(ChatFormatting.DARK_GRAY));
      }
   }
}
