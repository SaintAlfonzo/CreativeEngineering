// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModRecipes.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.recipe.TempadUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Tempad.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Tempad.MOD_ID);

    public static final RegistryObject<RecipeSerializer<TempadUpgradeRecipe>> upgradeSerializer = SERIALIZERS.register("upgrade", TempadUpgradeRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<TempadUpgradeRecipe>> upgradeRecipe = RECIPE_TYPES.register("upgrade",
            () -> RecipeType.simple(Tempad.id("upgrade")));

    private ModRecipes() {
    }
}
