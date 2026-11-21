package com.SaintAlfonzo.creativedelight.farm.bucket;

import java.util.LinkedHashMap;
import java.util.Map;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The wooden bucket, a farming tool (older saves' {@code creativeengineering} ids are remapped by
 * swaps/wooden_buckets.json). It carries the farm fluids only: water, Hearth and Harvest sap, cow milk, Hearth and
 * Harvest goat milk, nut milk (a KubeJS fluid, {@code creativedelight:nut_milk}) and Create honey. Other mods' fluids are
 * matched by id, so none of them is a dependency; a missing one simply never fills. Also registers the wooden cauldron
 * ({@link WoodenCauldronBlock}) and the vanilla cauldron and dispenser hooks ({@link WoodenBucketBehaviors}).
 * Writeup: feature_writeup/recipes_engineering.md.
 */
public final class WoodenBuckets {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeDelight.MOD_ID);
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeDelight.MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreativeDelight.MOD_ID);

	public static final ResourceLocation WATER_ID = new ResourceLocation("minecraft", "water");
	public static final ResourceLocation SAP_ID = new ResourceLocation("hearthandharvest", "sap");
	public static final ResourceLocation MILK_ID = new ResourceLocation("minecraft", "milk");
	public static final ResourceLocation HONEY_ID = new ResourceLocation("create", "honey");
	public static final ResourceLocation GOAT_MILK_ID = new ResourceLocation("hearthandharvest", "goat_milk");
	public static final ResourceLocation NUT_MILK_ID = new ResourceLocation("creativedelight", "nut_milk");

	public static final RegistryObject<Item> EMPTY = ITEMS.register("wooden_bucket",
			() -> new WoodenBucketItem(null, new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(16)));
	public static final RegistryObject<Item> WATER = filled("wooden_water_bucket", WATER_ID);
	public static final RegistryObject<Item> SAP = filled("wooden_sap_bucket", SAP_ID);
	public static final RegistryObject<Item> MILK = filled("wooden_milk_bucket", MILK_ID);
	public static final RegistryObject<Item> HONEY = filled("wooden_honey_bucket", HONEY_ID);
	public static final RegistryObject<Item> GOAT_MILK = filled("wooden_goat_milk_bucket", GOAT_MILK_ID);
	public static final RegistryObject<Item> NUT_MILK = filled("wooden_nut_milk_bucket", NUT_MILK_ID);

	// Wooden cauldron: spruce plank stats, not flammable (it holds water).
	public static final RegistryObject<Block> CAULDRON = BLOCKS.register("wooden_cauldron", () -> new WoodenCauldronBlock(
			BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.PODZOL).strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
	public static final RegistryObject<Item> CAULDRON_ITEM = ITEMS.register("wooden_cauldron",
			() -> new BlockItem(CAULDRON.get(), new Item.Properties().tab(CreativeModeTab.TAB_BREWING)));
	@SuppressWarnings("ConstantConditions")
	public static final RegistryObject<BlockEntityType<WoodenCauldronBlockEntity>> CAULDRON_BLOCK_ENTITY =
			BLOCK_ENTITY_TYPES.register("wooden_cauldron",
					() -> BlockEntityType.Builder.of(WoodenCauldronBlockEntity::new, CAULDRON.get()).build(null));

	/** Fluid id to filled bucket, for the fluid handler. */
	static final Map<ResourceLocation, RegistryObject<Item>> BY_FLUID = new LinkedHashMap<>();
	static {
		BY_FLUID.put(WATER_ID, WATER);
		BY_FLUID.put(SAP_ID, SAP);
		BY_FLUID.put(MILK_ID, MILK);
		BY_FLUID.put(HONEY_ID, HONEY);
		BY_FLUID.put(GOAT_MILK_ID, GOAT_MILK);
		BY_FLUID.put(NUT_MILK_ID, NUT_MILK);
	}

	/** Drunk like a milk bucket (clears effects). */
	static boolean drinkable(ResourceLocation fluid) {
		return MILK_ID.equals(fluid) || GOAT_MILK_ID.equals(fluid) || NUT_MILK_ID.equals(fluid);
	}

	private static RegistryObject<Item> filled(String name, ResourceLocation fluid) {
		return ITEMS.register(name, () -> new WoodenBucketItem(fluid,
				new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).craftRemainder(EMPTY.get())));
	}

	private WoodenBuckets() {}

	public static void register(IEventBus modBus) {
		ITEMS.register(modBus);
		BLOCKS.register(modBus);
		BLOCK_ENTITY_TYPES.register(modBus);
		MinecraftForge.EVENT_BUS.register(WoodenBucketMilking.class);
		modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(WoodenBucketBehaviors::register));
	}
}
