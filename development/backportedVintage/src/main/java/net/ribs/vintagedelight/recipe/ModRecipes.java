package net.ribs.vintagedelight.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.VintageDelight;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VintageDelight.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, VintageDelight.MODID);
    public static final RegistryObject<RecipeSerializer<FermentingRecipe>> FERMENTING_SERIALIZER = SERIALIZERS.register("fermenting", () -> FermentingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeType<FermentingRecipe>> FERMENTING_TYPE = TYPES.register("fermenting", () -> FermentingRecipe.Type.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
