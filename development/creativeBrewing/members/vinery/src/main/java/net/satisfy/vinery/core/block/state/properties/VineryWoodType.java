package net.satisfy.vinery.core.block.state.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.satisfy.foundation.wood.BoatWood;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.ObjectRegistry;

/**
 * The wood of the cherry set (D1: registered as {@code cherry_*}). The wood type name stays 1.5.5's
 * {@code vinery:dark_cherry}, so the sign and boat textures resolve to 1.5.5's art
 * ({@code vinery:entity/signs/dark_cherry}, {@code textures/entity/boat/dark_cherry.png}).
 */
public class VineryWoodType {
    public static final ResourceLocation DARK_CHERRY_ID = new ResourceLocation(Vinery.MOD_ID, "dark_cherry");
    public static final WoodType DARK_CHERRY = WoodType.register(WoodType.create(DARK_CHERRY_ID.toString()));
    public static final BoatWood DARK_CHERRY_BOAT = BoatWood.register(DARK_CHERRY_ID, () -> ObjectRegistry.DARK_CHERRY_BOAT.get(), () -> ObjectRegistry.DARK_CHERRY_CHEST_BOAT.get());
}
