// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.shared.BuiltInPacks;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The Shipping Bin port (feature_writeup/farming_for_blockheads.md, TODO mod-integration #7): our own code written from
 * the behavior of Farming for Blockheads 26.3.0.2 (All Rights Reserved), registered under the upstream namespace
 * {@code farmingforblockheads} so the donor jar's art (blockstates, models, textures, read at runtime from
 * {@code blockheads_upstream/}, {@link UpstreamJars}) matches by id. Farming for Blockheads itself is not installed.
 * Registers the bin (block, item, block entity, menu), the {@code farmingforblockheads:shipping_bin} recipe type, the
 * payout loot number provider and conditions ({@link ShipmentLoot}), the carriers (balloon, crate, falling crate,
 * courier) and the Courier Spawn Point (ruling D5b; its id is ours).
 */
public final class ShippingBin {
	public static final String NAMESPACE = "farmingforblockheads";

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NAMESPACE);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NAMESPACE);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NAMESPACE);
	private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, NAMESPACE);
	private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, NAMESPACE);
	private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, NAMESPACE);
	private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NAMESPACE);
	private static final DeferredRegister<LootItemConditionType> CONDITIONS =
			DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, NAMESPACE);
	private static final DeferredRegister<LootNumberProviderType> NUMBER_PROVIDERS =
			DeferredRegister.create(Registry.LOOT_NUMBER_PROVIDER_REGISTRY, NAMESPACE);

	public static final ResourceLocation SHIPPING_BIN_ID = id("shipping_bin");
	/** The payout table (ruling D2: emeralds now, Lightman's Currency prices later, always in this table). */
	public static final ResourceLocation PAYOUT_TABLE = id("gameplay/shipping_bin");

	public static final RegistryObject<Block> SHIPPING_BIN = BLOCKS.register("shipping_bin", () -> new ShippingBinBlock(
			BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
	public static final RegistryObject<Item> SHIPPING_BIN_ITEM = ITEMS.register("shipping_bin",
			() -> new ShippingBinItem(SHIPPING_BIN.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
	public static final RegistryObject<Block> COURIER_SPAWN = BLOCKS.register("courier_spawn", () -> new CourierSpawnBlock(
			BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
	public static final RegistryObject<Item> COURIER_SPAWN_ITEM = ITEMS.register("courier_spawn",
			() -> new CourierSpawnItem(COURIER_SPAWN.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

	@SuppressWarnings("ConstantConditions")
	public static final RegistryObject<BlockEntityType<ShippingBinBlockEntity>> SHIPPING_BIN_BLOCK_ENTITY = BLOCK_ENTITIES.register(
			"shipping_bin", () -> BlockEntityType.Builder.of(ShippingBinBlockEntity::new, SHIPPING_BIN.get()).build(null));
	public static final RegistryObject<MenuType<ShippingBinMenu>> SHIPPING_BIN_MENU =
			MENUS.register("shipping_bin", () -> IForgeMenuType.create(ShippingBinMenu::new));

	public static final RegistryObject<RecipeSerializer<ShippingBinRecipe>> SHIPPING_BIN_SERIALIZER =
			SERIALIZERS.register("shipping_bin", ShippingBinRecipe.Serializer::new);
	public static final RegistryObject<RecipeType<ShippingBinRecipe>> SHIPPING_BIN_RECIPE =
			RECIPE_TYPES.register("shipping_bin", () -> RecipeType.simple(SHIPPING_BIN_ID));

	public static final RegistryObject<EntityType<ShippingBalloonEntity>> SHIPPING_BALLOON = ENTITIES.register("shipping_balloon",
			() -> EntityType.Builder.<ShippingBalloonEntity>of(ShippingBalloonEntity::new, MobCategory.MISC)
					.sized(1.3F, 2.8F).clientTrackingRange(16).updateInterval(2).fireImmune().build(NAMESPACE + ":shipping_balloon"));
	public static final RegistryObject<EntityType<ShippingCrateEntity>> SHIPPING_CRATE = ENTITIES.register("shipping_crate",
			() -> EntityType.Builder.<ShippingCrateEntity>of(ShippingCrateEntity::new, MobCategory.MISC)
					.sized(0.8F, 0.8F).clientTrackingRange(10).updateInterval(2).fireImmune().build(NAMESPACE + ":shipping_crate"));
	public static final RegistryObject<EntityType<FallingShippingCrateEntity>> FALLING_SHIPPING_CRATE = ENTITIES.register(
			"falling_shipping_crate", () -> EntityType.Builder.<FallingShippingCrateEntity>of(FallingShippingCrateEntity::new, MobCategory.MISC)
					.sized(0.8F, 0.8F).clientTrackingRange(16).updateInterval(2).build(NAMESPACE + ":falling_shipping_crate"));
	public static final RegistryObject<EntityType<CourierEntity>> COURIER = ENTITIES.register("courier",
			() -> EntityType.Builder.<CourierEntity>of(CourierEntity::new, MobCategory.MISC)
					.sized(0.6F, 1.95F).clientTrackingRange(10).fireImmune().build(NAMESPACE + ":courier"));

	public static final RegistryObject<LootNumberProviderType> SHIPMENT_VALUE_PROVIDER = NUMBER_PROVIDERS.register("shipment_value",
			() -> new LootNumberProviderType(new ShipmentLoot.ValueProvider.Serializer()));
	public static final RegistryObject<LootItemConditionType> SHIPMENT_VALUE_CONDITION = CONDITIONS.register("shipment_value",
			() -> new LootItemConditionType(new ShipmentLoot.ValueCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> SHIPMENT_CONTAINS_CONDITION = CONDITIONS.register("shipment_contains",
			() -> new LootItemConditionType(new ShipmentLoot.ContainsCondition.Serializer()));

	private ShippingBin() {
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(NAMESPACE, path);
	}

	public static void register(IEventBus modBus) {
		UpstreamJars.verifyOrFail();
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
		BLOCK_ENTITIES.register(modBus);
		MENUS.register(modBus);
		SERIALIZERS.register(modBus);
		RECIPE_TYPES.register(modBus);
		ENTITIES.register(modBus);
		CONDITIONS.register(modBus);
		NUMBER_PROVIDERS.register(modBus);
		modBus.addListener((EntityAttributeCreationEvent event) -> event.put(COURIER.get(), CourierEntity.createAttributes().build()));
		// The donor's art as a built-in resource pack at the bottom (only the bin's files, UpstreamJars.assets()).
		modBus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addMemory(event, net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
				"blockheads_upstream_assets", "Shipping Bin: art read from the Farming for Blockheads jar",
				net.minecraft.server.packs.repository.Pack.Position.BOTTOM, UpstreamJars::assets));
		CreativeDelight.LOGGER.debug("[creativedelight] Shipping Bin registered under {}", NAMESPACE);
	}
}
