package io.github.mortuusars.exposure.client.render.photograph;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class PhotographStyles {
   private static final Map<PhotographType, PhotographStyle> STYLES = new HashMap<>();

   public static void register(PhotographType photographType, PhotographStyle style) {
      Preconditions.checkState(!STYLES.containsKey(photographType), "PhotographStyle for type '%s' is already registered.", photographType);
      STYLES.put(photographType, style);
   }

   @NotNull
   public static PhotographStyle get(PhotographType type) {
      PhotographStyle style = STYLES.get(type);
      Preconditions.checkNotNull(style, "Type '%s' does not have a registered style.");
      return style;
   }
}
