// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.fats;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers {@code creativedelight:whale_oil_lamp} ({@link WhaleOilLampBlock}) and its item. Vanilla lantern
 * properties (strength, sound, no occlusion), light by the LIT state. The model and texture are the vanilla lantern's
 * (a copy) until the owner picks a palette for the recolor. The oil tank is {@link WhaleOilLampBlockEntity}.
 */
public final class WhaleOilLamp {

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeDelight.MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeDelight.MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreativeDelight.MOD_ID);

	public static final RegistryObject<Block> BLOCK = BLOCKS.register("whale_oil_lamp",
			() -> new WhaleOilLampBlock(Block.Properties.copy(Blocks.LANTERN).lightLevel(WhaleOilLampBlock::light)));
	public static final RegistryObject<Item> ITEM = ITEMS.register("whale_oil_lamp",
			() -> new WhaleOilLampItem(BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
	@SuppressWarnings("ConstantConditions")
	public static final RegistryObject<BlockEntityType<WhaleOilLampBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("whale_oil_lamp",
			() -> BlockEntityType.Builder.of(WhaleOilLampBlockEntity::new, BLOCK.get()).build(null));

	private WhaleOilLamp() {}

	public static void register(IEventBus modBus) {
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
		BLOCK_ENTITY_TYPES.register(modBus);
	}
}
