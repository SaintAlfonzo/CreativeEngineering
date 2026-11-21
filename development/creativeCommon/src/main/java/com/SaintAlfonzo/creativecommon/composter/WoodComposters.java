// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.composter;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * A composter per wood species (TODO farming/composting #3, briefs/farming_composting.md "composting #3",
 * feature_writeup/composting.md). Oak stays {@code minecraft:composter}. Each block is a {@link ComposterBlock}
 * subclass, so the chance table, hoppers, comparator output and bone meal are vanilla's. Recipes (that wood's slabs)
 * and tags: Common's server script {@code kubejs/data/creativecommon/kubejs/recipes/composters.js}.
 */
public final class WoodComposters {

	/** species -> its planks block (map color source). The overworld ones burn like the vanilla composter. */
	private static final Map<String, String> OVERWORLD = new LinkedHashMap<>();
	/** Nether woods: not flammable, no fuel value. */
	private static final Map<String, String> NETHER = new LinkedHashMap<>();
	static {
		OVERWORLD.put("spruce", "minecraft:spruce_planks");
		OVERWORLD.put("birch", "minecraft:birch_planks");
		OVERWORLD.put("dark_oak", "minecraft:dark_oak_planks");
		OVERWORLD.put("jungle", "minecraft:jungle_planks");
		OVERWORLD.put("acacia", "minecraft:acacia_planks");
		OVERWORLD.put("mangrove", "minecraft:mangrove_planks");
		OVERWORLD.put("twisted", "architects_palette:twisted_planks");
		OVERWORLD.put("ancient", "quark:ancient_planks");
		OVERWORLD.put("blossom", "quark:blossom_planks");
		OVERWORLD.put("azalea", "quark:azalea_planks");
		NETHER.put("crimson", "minecraft:crimson_planks");
		NETHER.put("warped", "minecraft:warped_planks");
	}

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeCommon.MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeCommon.MODID);
	public static final Map<String, RegistryObject<Block>> BY_SPECIES = new LinkedHashMap<>();

	static {
		OVERWORLD.forEach((name, planks) -> add(name, planks, true));
		NETHER.forEach((name, planks) -> add(name, planks, false));
	}

	private WoodComposters() {}

	private static void add(String name, String planks, boolean flammable) {
		String id = name + "_composter";
		Material material = flammable ? Material.WOOD : Material.NETHER_WOOD;
		ResourceLocation planksId = new ResourceLocation(planks);
		// The planks' map color, looked up when first asked (the planks may register after this jar); wood if absent
		MaterialColor[] color = new MaterialColor[1];
		BlockBehaviour.Properties props = BlockBehaviour.Properties.of(material, state -> {
			if (color[0] == null) {
				Block p = ForgeRegistries.BLOCKS.getValue(planksId);
				color[0] = p == null || p.defaultBlockState().isAir() ? material.getColor() : p.defaultMaterialColor();
			}
			return color[0];
		}).strength(0.6F).sound(SoundType.WOOD);
		RegistryObject<Block> block = BLOCKS.register(id, () -> new WoodComposterBlock(props, flammable));
		ITEMS.register(id, () -> new WoodComposterItem(block.get(),
				new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS), flammable ? 300 : 0));
		BY_SPECIES.put(name, block);
	}

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
	}
}
