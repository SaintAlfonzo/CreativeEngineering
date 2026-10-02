package com.SaintAlfonzo.creativeengineering.fan;

import java.util.Optional;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers the {@code creativeengineering:dragon_breathing} Create processing recipe
 * type + serializer, and looks recipes up for {@link DragonBreathingFanProcessingType}.
 *
 * <p>Ported from Create: Halitosis' {@code HalitosisRecipeTypes} (there an enum; a plain
 * singleton is enough for one type).
 */
public final class DragonBreathingRecipeType implements IRecipeTypeInfo {

    public static final DragonBreathingRecipeType INSTANCE = new DragonBreathingRecipeType();

    public static final String NAME = "dragon_breathing";
    public static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, NAME);

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeEngineering.MODID);
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreativeEngineering.MODID);

    private static final RegistryObject<RecipeSerializer<?>> SERIALIZER =
            SERIALIZERS.register(NAME, () -> new ProcessingRecipeSerializer<>(DragonBreathingRecipe::new));
    private static final RegistryObject<RecipeType<?>> TYPE =
            TYPES.register(NAME, () -> RecipeType.simple(ID));

    private DragonBreathingRecipeType() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
        TYPES.register(modEventBus);
    }

    @SuppressWarnings("unchecked")
    public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level level) {
        return level.getRecipeManager().getRecipeFor((RecipeType<T>) getType(), inv, level);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) SERIALIZER.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RecipeType<?>> T getType() {
        return (T) TYPE.get();
    }
}
