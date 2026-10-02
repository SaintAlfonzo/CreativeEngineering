package io.github.mortuusars.exposure.client.gui;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class Widgets {
   public static final WidgetSprites PREVIOUS_BUTTON_SPRITES = threeStateSprites(Exposure.resource("widgets/previous_button"));
   public static final WidgetSprites NEXT_BUTTON_SPRITES = threeStateSprites(Exposure.resource("widgets/next_button"));
   public static final WidgetSprites CONFIRM_BUTTON_SPRITES = threeStateSprites(Exposure.resource("widgets/confirm_button"));
   public static final WidgetSprites CANCEL_BUTTON_SPRITES = threeStateSprites(Exposure.resource("widgets/cancel_button"));

   public static ResourceLocation empty() {
      return Exposure.resource("empty");
   }

   public static WidgetSprites normalAndHighlighted(ResourceLocation base) {
      return new WidgetSprites(base, base, new ResourceLocation(base.getNamespace(), base.getPath() + "_highlighted"));
   }

   public static WidgetSprites normalAndHighlighted(ResourceLocation normal, ResourceLocation highlighted) {
      return new WidgetSprites(normal, normal, highlighted);
   }

   public static WidgetSprites threeStateSprites(ResourceLocation base) {
      return new WidgetSprites(
         base,
         new ResourceLocation(base.getNamespace(), base.getPath() + "_disabled"),
         new ResourceLocation(base.getNamespace(), base.getPath() + "_highlighted")
      );
   }

   public static <T> Map<T, WidgetSprites> createMap(List<T> values, Function<T, WidgetSprites> convertFunc) {
      Preconditions.checkArgument(!values.isEmpty(), "values list must not be empty.");
      Map<T, WidgetSprites> map = new HashMap<>();

      for (T value : values) {
         map.put(value, convertFunc.apply(value));
      }

      return map;
   }
}
