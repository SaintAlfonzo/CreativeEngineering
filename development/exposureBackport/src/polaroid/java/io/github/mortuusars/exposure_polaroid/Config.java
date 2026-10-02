package io.github.mortuusars.exposure_polaroid;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config {
   public static int getColor(ConfigValue<String> config) {
      String value = (String)config.get();

      try {
         return Color.fromHex(value).getARGB();
      } catch (Exception var3) {
         Exposure.LOGGER.error("{} is not valid ARGB color. {}", value, String.join("/", config.getPath()));
         return 0;
      }
   }

   public static class Client {
      public static final ForgeConfigSpec SPEC;
      public static final BooleanValue INSTANT_CAMERA_SHOW_TOOLTIP_DETAILS;
      public static final BooleanValue INSTANT_CAMERA_SHOW_SLIDES_COUNT_IN_TOOLTIP;
      public static final BooleanValue INSTANT_CAMERA_SHOW_FULLNESS_BAR_ON_ITEM;
      public static final ConfigValue<String> VIEWFINDER_BACKGROUND_COLOR;
      public static final ConfigValue<String> VIEWFINDER_FONT_MAIN_COLOR;
      public static final ConfigValue<String> VIEWFINDER_FONT_SECONDARY_COLOR;

      static {
         Builder builder = new Builder();
         builder.push("ui");
         INSTANT_CAMERA_SHOW_TOOLTIP_DETAILS = builder.comment("Details about Camera configuring will be shown in Instant Camera item tooltip.")
            .define("instant_camera_details_tooltip", true);
         INSTANT_CAMERA_SHOW_SLIDES_COUNT_IN_TOOLTIP = builder.comment(
               new String[]{"Slides count will be shown in the Instant Camera item tooltip.", "Default: true"}
            )
            .define("instant_camera_slides_tooltip", true);
         INSTANT_CAMERA_SHOW_FULLNESS_BAR_ON_ITEM = builder.comment(
               new String[]{"Slide fullness bar will be shown on the Instant Camera item.", "Default: false"}
            )
            .define("instant_camera_shows_slides_bar", false);
         builder.pop();
         builder.push("viewfinder");
         VIEWFINDER_BACKGROUND_COLOR = builder.comment("Color in hex format. AARRGGBB.").define("background_color", "FA1F1D1B");
         VIEWFINDER_FONT_MAIN_COLOR = builder.comment("Color in hex format. AARRGGBB.").define("font_main_color", "FF2B2622");
         VIEWFINDER_FONT_SECONDARY_COLOR = builder.comment("Color in hex format. AARRGGBB.").define("font_secondary_color", "FF7A736C");
         builder.pop();
         SPEC = builder.build();
      }
   }

   public static class Server {
      public static final ForgeConfigSpec SPEC;
      public static final ConfigValue<String> INSTANT_CAMERA_FOCAL_RANGE;
      public static final IntValue INSTANT_CAMERA_FRAME_SIZE;
      public static final IntValue INSTANT_CAMERA_SLIDE_CAPACITY;

      static {
         Builder builder = new Builder();
         builder.push("instant_camera");
         INSTANT_CAMERA_FOCAL_RANGE = builder.comment(new String[]{"Min/max focal ranges of an Instant Camera.", "Allowed range: 10-300", "Default: 20-40"})
            .define("focal_range", "20-40");
         INSTANT_CAMERA_FRAME_SIZE = builder.comment(
               "Default size of an exposure image taken with Instant Camera. High values take more disk space and can cause lag. Default: 240"
            )
            .defineInRange("frame_size", 240, 1, 2048);
         INSTANT_CAMERA_SLIDE_CAPACITY = builder.comment("How many Slides can Instant Camera hold at once. Default: 12")
            .defineInRange("slide_capacity", 12, 1, 64);
         builder.pop();
         SPEC = builder.build();
      }
   }
}
