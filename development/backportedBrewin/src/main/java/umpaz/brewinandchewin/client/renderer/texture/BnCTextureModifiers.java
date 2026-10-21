package umpaz.brewinandchewin.client.renderer.texture;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.client.renderer.texture.modifier.GlintTextureModifier;
import umpaz.brewinandchewin.client.renderer.texture.modifier.GrassTintTextureModifier;
import umpaz.brewinandchewin.client.renderer.texture.modifier.PotionTintTextureModifier;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;

/** Coaster model texture modifiers by id ({"type": "brewinandchewin:glint"} in a coaster JSON). */
public class BnCTextureModifiers {
   private static final Map<ResourceLocation, Supplier<? extends TextureModifier>> FACTORIES = new HashMap<>();

   public static void register(ResourceLocation id, Supplier<? extends TextureModifier> factory) {
      if (FACTORIES.containsKey(id)) {
         throw new UnsupportedOperationException("Attempted to register duplicate ID texture modifiers. " + id);
      }
      FACTORIES.put(id, factory);
   }

   @Nullable
   public static TextureModifier create(ResourceLocation id) {
      Supplier<? extends TextureModifier> factory = FACTORIES.get(id);
      return factory == null ? null : factory.get();
   }

   public static void init() {
      if (FACTORIES.isEmpty()) {
         register(GlintTextureModifier.ID, GlintTextureModifier::new);
         register(GrassTintTextureModifier.ID, GrassTintTextureModifier::new);
         register(PotionTintTextureModifier.ID, PotionTintTextureModifier::new);
      }
   }
}
