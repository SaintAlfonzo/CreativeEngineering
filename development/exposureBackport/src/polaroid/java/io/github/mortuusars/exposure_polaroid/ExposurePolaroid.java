package io.github.mortuusars.exposure_polaroid;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.film.properties.ColorBalance;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmStyle;
import io.github.mortuusars.exposure.world.camera.film.properties.HSB;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import io.github.mortuusars.exposure_polaroid.world.item.InstantCameraItem;
import io.github.mortuusars.exposure_polaroid.world.item.InstantSlideItem;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.world.item.Item.Properties;
import org.slf4j.Logger;

public class ExposurePolaroid {
   public static final String ID = "exposure_polaroid";
   public static final Logger LOGGER = LogUtils.getLogger();

   public static void init() {
      ExposurePolaroid.Blocks.init();
      ExposurePolaroid.BlockEntityTypes.init();
      ExposurePolaroid.EntityTypes.init();
      ExposurePolaroid.Items.init();
      ExposurePolaroid.DataComponents.init();
      ExposurePolaroid.CriteriaTriggers.init();
      ExposurePolaroid.ItemSubPredicates.init();
      ExposurePolaroid.MenuTypes.init();
      ExposurePolaroid.RecipeSerializers.init();
      ExposurePolaroid.SoundEvents.init();
      ExposurePolaroid.ArgumentTypes.init();
   }

   public static ResourceLocation resource(String path) {
      return new ResourceLocation("exposure_polaroid", path);
   }

   public static class ArgumentTypes {
      public static void init() {
      }
   }

   public static class BlockEntityTypes {
      static void init() {
      }
   }

   public static class Blocks {
      static void init() {
      }
   }

   public static class CriteriaTriggers {
      public static void init() {
      }
   }

   public static class DataComponents {
      static void init() {
      }
   }

   public static class EntityTypes {
      static void init() {
      }
   }

   public static class ItemSubPredicates {
      public static void init() {
      }
   }

   public static class Items {
      // 1.19.2: Properties.component(...) defaults become DataComponentType.withDefault; the items go into Exposure's tab
      // (upstream adds them in BuildCreativeModeTabContentsEvent).
      public static final Supplier<InstantCameraItem> INSTANT_CAMERA = Register.item(
         "instant_camera",
         () -> DataComponentType.withDefault(
               new InstantCameraItem(new Properties().stacksTo(1).tab(Exposure.CreativeTabs.EXPOSURE.get())),
               Exposure.DataComponents.CAMERA_ACTIVE,
               false
            )
      );
      public static final Supplier<InstantSlideItem> INSTANT_COLOR_SLIDE = Register.item(
         "instant_color_slide", () -> slide(ExposureType.COLOR, FilmStyle.create()
            .withContrast(0.2F)
            .withLevels(new Levels(0, 135, 255, 25, 255))
            .withHSB(new HSB(0.0F, 0.05F, 0.05F))
            .withColorBalance(new ColorBalance(0.03F, 0.01F, -0.01F)))
      );
      public static final Supplier<InstantSlideItem> INSTANT_BLACK_AND_WHITE_SLIDE = Register.item(
         "instant_black_and_white_slide", () -> slide(ExposureType.BLACK_AND_WHITE, FilmStyle.create()
            .withContrast(0.2F)
            .withLevels(new Levels(0, 135, 255, 25, 255))
            .withHSB(new HSB(0.0F, 0.05F, 0.05F))
            .withColorBalance(new ColorBalance(0.03F, 0.01F, -0.01F)))
      );
      public static final Supplier<InstantSlideItem> HIGH_SENSITIVITY_INSTANT_COLOR_SLIDE = Register.item(
         "high_sensitivity_instant_color_slide", () -> slide(ExposureType.COLOR, FilmStyle.create()
            .withSensitivity(2.0F)
            .withContrast(0.2F)
            .withLevels(new Levels(0, 135, 255, 25, 255))
            .withHSB(new HSB(0.0F, 0.05F, 0.05F))
            .withColorBalance(new ColorBalance(0.03F, 0.01F, -0.01F))
            .withNoise(0.05F))
      );
      public static final Supplier<InstantSlideItem> HIGH_SENSITIVITY_INSTANT_BLACK_AND_WHITE_SLIDE = Register.item(
         "high_sensitivity_instant_black_and_white_slide", () -> slide(ExposureType.BLACK_AND_WHITE, FilmStyle.create()
            .withSensitivity(2.0F)
            .withContrast(0.2F)
            .withLevels(new Levels(0, 135, 255, 25, 255))
            .withHSB(new HSB(0.0F, 0.05F, 0.05F))
            .withColorBalance(new ColorBalance(0.03F, 0.01F, -0.01F))
            .withNoise(0.05F))
      );

      private static InstantSlideItem slide(ExposureType type, FilmStyle style) {
         return DataComponentType.withDefault(
            new InstantSlideItem(type, new Properties().tab(Exposure.CreativeTabs.EXPOSURE.get())), Exposure.DataComponents.FILM_STYLE, style
         );
      }

      static void init() {
      }
   }

   public static class LootTables {
   }

   public static class MenuTypes {
      static void init() {
      }
   }

   public static class RecipeSerializers {
      static void init() {
      }
   }

   public static class Registries {
   }

   public static class SoundEvents {
      public static final Supplier<SoundEvent> INSTANT_CAMERA_VIEWFINDER_OPEN = register("item", "instant_camera.viewfinder_open");
      public static final Supplier<SoundEvent> INSTANT_CAMERA_VIEWFINDER_CLOSE = register("item", "instant_camera.viewfinder_close");
      public static final Supplier<SoundEvent> INSTANT_CAMERA_RELEASE = register("item", "instant_camera.release");
      public static final Supplier<SoundEvent> INSTANT_CAMERA_SLIDE_INSERT = register("item", "instant_camera.slide_insert");
      public static final Supplier<SoundEvent> INSTANT_CAMERA_SLIDE_REMOVE = register("item", "instant_camera.slide_remove");

      private static Supplier<SoundEvent> register(String category, String key) {
         Preconditions.checkState(category != null && !category.isEmpty(), "'category' should not be empty.");
         Preconditions.checkState(key != null && !key.isEmpty(), "'key' should not be empty.");
         String path = category + "." + key;
         return Register.soundEvent(path, () -> new SoundEvent(ExposurePolaroid.resource(path)));
      }

      static void init() {
      }
   }

   public static class Stats {
      public static final Map<ResourceLocation, StatFormatter> STATS = new HashMap<>();

      private static ResourceLocation register(ResourceLocation location, StatFormatter formatter) {
         STATS.put(location, formatter);
         return location;
      }

      public static void register() {
         STATS.forEach((location, formatter) -> {
            Registry.register(Registry.CUSTOM_STAT, location, location);
            net.minecraft.stats.Stats.CUSTOM.get(location, formatter);
         });
      }
   }

   public static class Tags {
      public static class Blocks {
      }

      public static class Items {
      }
   }
}
