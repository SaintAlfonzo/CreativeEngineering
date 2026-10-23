package net.mehdinoui.fungidelight.common.registry;

import net.mehdinoui.fungidelight.common.recipe.EffectStewRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Port addition (fungi_delight.md 5.1.5): the suspicious stew recipes name their effect by registry id.
public class ModRecipes {
   public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "fungidelight");
   public static final RegistryObject<RecipeSerializer<EffectStewRecipe>> EFFECT_STEW = SERIALIZERS.register("effect_stew", EffectStewRecipe.Serializer::new);

   public static void register(IEventBus eventBus) {
      SERIALIZERS.register(eventBus);
   }
}
