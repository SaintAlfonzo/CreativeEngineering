package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry objects of the layer blocks (plan_post_scratch_1115.md G7.5, G7.9): the falling layer entity and the full
 * bamboo leaves block (no item of its own: it drops and picks as Twigs bamboo leaves).
 */
public final class FallingLayerEntities {

	private FallingLayerEntities() {}

	public static final DeferredRegister<EntityType<?>> ENTITIES =
			DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CreativeCompat.NS);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeCompat.NS);

	public static final RegistryObject<EntityType<FallingLayerEntity>> FALLING_LAYER = ENTITIES.register("falling_layer",
			() -> EntityType.Builder.<FallingLayerEntity>of(FallingLayerEntity::new, MobCategory.MISC)
					.sized(0.98F, 0.98F)
					.clientTrackingRange(10)
					.updateInterval(20)
					.build("creativecompat:falling_layer"));

	public static final RegistryObject<Block> BAMBOO_LEAVES_FULL = BLOCKS.register("bamboo_leaves_full",
			FullBambooLeavesBlock::new);

	public static void register(IEventBus modEventBus) {
		ENTITIES.register(modEventBus);
		BLOCKS.register(modEventBus);
	}
}
