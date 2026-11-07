package com.SaintAlfonzo.creativecompat.portalgun;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The Portal Gun dye recipe serializer, {@code creativecompat:portal_gun_dye} (feature_writeup/plan_portal_gun.md 4).
 * Registered whether or not the Portal Gun is loaded: the recipe names the gun only by id, and its JSON
 * (data/creativecompat/recipes/portal_gun_dye.json) loads only with the mod.
 */
public final class PortalGunRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeCompat.MOD_ID);

    public static final RegistryObject<SimpleRecipeSerializer<PortalGunDyeRecipe>> DYE =
            SERIALIZERS.register("portal_gun_dye", () -> new SimpleRecipeSerializer<>(PortalGunDyeRecipe::new));

    private PortalGunRecipes() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
