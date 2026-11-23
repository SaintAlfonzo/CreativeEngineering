// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.food.popcorn;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Placeable popcorn boxes (food/seasoning #3, owner ruling): one for plain popcorn and one per popcorn flavor, ids
 * {@code creativedelight:<variant>_popcorn_box}. Registered only with Corn Delight. Blockstates and item models point
 * at Corn Delight's popcorn box models (and so its texture) unchanged; distinct art per flavor is the owner's later.
 * Recipes: data creativedelight/kubejs/corn_delight/recipes.js (4 popcorn of the flavor + paper).
 */
public final class PopcornBoxes {

	/** Variant -> flavor id (null: plain popcorn). The flavors that go on popcorn (flavors.js). */
	public static final Map<String, ResourceLocation> VARIANTS = new LinkedHashMap<>();

	static {
		VARIANTS.put("plain", null);
		for (String f : new String[] { "cheese", "onion", "garlic", "ghost_pepper", "void_pepper", "cream_and_onion" }) {
			VARIANTS.put(f, new ResourceLocation(CreativeDelight.MOD_ID, f));
		}
	}

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeDelight.MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeDelight.MOD_ID);
	public static final Map<String, RegistryObject<Block>> BOXES = new LinkedHashMap<>();

	private PopcornBoxes() {}

	public static void register(IEventBus modBus) {
		if (!ModList.get().isLoaded("corn_delight")) return;
		VARIANTS.forEach((variant, flavor) -> {
			String id = variant + "_popcorn_box";
			RegistryObject<Block> block = BLOCKS.register(id,
					() -> new PopcornBoxBlock(Block.Properties.copy(Blocks.WHITE_WOOL), flavor));
			ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_FOOD)));
			BOXES.put(variant, block);
		});
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
	}
}
