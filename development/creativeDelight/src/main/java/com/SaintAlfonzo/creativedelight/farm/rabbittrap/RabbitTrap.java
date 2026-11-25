// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.rabbittrap;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The rabbit trap port (feature_writeup/farming_for_blockheads.md 2.3, TODO mod-integration #10, ruling D7): our own
 * code written from the behavior of Farming for Blockheads 26.3.0.2 (All Rights Reserved), registered under the upstream
 * namespace so the donor jar's art (blockstate, block model, three textures; {@code UpstreamJars}) matches by id.
 * Registers the block, its item and block entity, the unattended catch tables ({@link RabbitTrapCatches}, data
 * {@code farmingforblockheads:rabbit_trap/biomes}) and the queue that rolls reloaded traps in random order
 * ({@link PassiveCatches}, ruling D12).
 */
public final class RabbitTrap {
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ShippingBin.NAMESPACE);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShippingBin.NAMESPACE);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ShippingBin.NAMESPACE);

	/** Entity types the trap lures and keeps (2.3.1, ruling D11; filled by the pack script rabbit_trap.js). */
	public static final TagKey<EntityType<?>> CAPTURABLE = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, ShippingBin.id("rabbit_trap_capturable"));
	/** Rolled first on an unattended hit: the fox note (2.3), else an animal from the biome table. */
	public static final ResourceLocation LOOT_TABLE = ShippingBin.id("gameplay/rabbit_trap");

	public static final RegistryObject<Block> RABBIT_TRAP = BLOCKS.register("rabbit_trap", () -> new RabbitTrapBlock(
			BlockBehaviour.Properties.of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion()));
	public static final RegistryObject<Item> RABBIT_TRAP_ITEM = ITEMS.register("rabbit_trap",
			() -> new RabbitTrapItem(RABBIT_TRAP.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
	@SuppressWarnings("ConstantConditions")
	public static final RegistryObject<BlockEntityType<RabbitTrapBlockEntity>> RABBIT_TRAP_BLOCK_ENTITY = BLOCK_ENTITIES.register(
			"rabbit_trap", () -> BlockEntityType.Builder.of(RabbitTrapBlockEntity::new, RABBIT_TRAP.get()).build(null));

	private RabbitTrap() {
	}

	public static void register(IEventBus modBus) {
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
		BLOCK_ENTITIES.register(modBus);
		MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(RabbitTrapCatches.INSTANCE));
		MinecraftForge.EVENT_BUS.addListener((TickEvent.LevelTickEvent event) -> {
			if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) PassiveCatches.tick(event.level);
		});
		MinecraftForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> PassiveCatches.forget(event.getLevel()));
		CreativeDelight.LOGGER.debug("[creativedelight] Rabbit trap registered under {}", ShippingBin.NAMESPACE);
	}
}
