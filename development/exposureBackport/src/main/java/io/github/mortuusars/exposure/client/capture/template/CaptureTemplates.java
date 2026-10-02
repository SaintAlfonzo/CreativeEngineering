package io.github.mortuusars.exposure.client.capture.template;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CaptureTemplates {
   private static final Map<ResourceLocation, CaptureTemplate> TEMPLATES = new HashMap<>();

   public static void register(ResourceLocation id, CaptureTemplate template) {
      Preconditions.checkState(!TEMPLATES.containsKey(id), "Template with id '%s' is already registered.", id);
      TEMPLATES.put(id, template);
   }

   @Nullable
   public static CaptureTemplate get(ResourceLocation id) {
      return TEMPLATES.get(id);
   }

   public static CaptureTemplate getOrThrow(ResourceLocation id) {
      CaptureTemplate template = TEMPLATES.get(id);
      Preconditions.checkNotNull(template, "No template for id '%s' is registered.", id);
      return template;
   }
}
