package com.SaintAlfonzo.creativedelight.delightful;

import com.SaintAlfonzo.creativedelight.delightful.block.BaklavaBlock;
import com.SaintAlfonzo.creativedelight.delightful.block.CantaloupePlantBlock;
import com.SaintAlfonzo.creativedelight.delightful.block.MilkshakeCauldronBlock;
import com.SaintAlfonzo.creativedelight.delightful.item.CupItem;
import com.SaintAlfonzo.creativedelight.delightful.item.FluixKnifeItem;
import com.SaintAlfonzo.creativedelight.delightful.item.GummyItem;
import com.SaintAlfonzo.creativedelight.delightful.item.ShakeItem;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.KnifeItem;
import vectorwing.farmersdelight.common.registry.ModItems;

/**
 * The backported blocks and items, in Delightful's namespace ({@code delightful:<id>}). Ids, block properties, food
 * values and stack sizes are 3.8.1's, rebuilt with 1.19.2 calls (Material for MapColor; Blocks.CAKE's properties for
 * the baklava, as Farmer's Delight's own pies). Items sit in Farmer's Delight's creative tab, as 3.4.1's do.
 * Delivery and the list: feature_writeup/delightful.md 2.1, 2.2, 3.
 */
public final class DelightfulBackportRegistry {

	private static final String NS = "delightful";

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NS);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NS);

	// Blocks
	public static final RegistryObject<Block> CANTALOUPE_PLANT = BLOCKS.register("cantaloupe_plant",
			() -> new CantaloupePlantBlock(BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.PLANT).noCollission()
					.randomTicks().instabreak().sound(SoundType.CROP)));
	public static final RegistryObject<Block> STUFFED_CANTALOUPE_BLOCK = BLOCKS.register("stuffed_cantaloupe_block",
			() -> new FeastBlock(BlockBehaviour.Properties.copy(Blocks.MELON).noOcclusion(), DelightfulBackportRegistry.STUFFED_CANTALOUPE, false));
	public static final RegistryObject<Block> BAKLAVA = BLOCKS.register("baklava",
			() -> new BaklavaBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), DelightfulBackportRegistry.BAKLAVA_SLICE));
	public static final RegistryObject<Block> MATCHA_MILKSHAKE_CAULDRON = BLOCKS.register("matcha_milkshake_cauldron",
			() -> new MilkshakeCauldronBlock(milkshakeMap("matcha")));
	public static final RegistryObject<Block> SALMONBERRY_MILKSHAKE_CAULDRON = BLOCKS.register("salmonberry_milkshake_cauldron",
			() -> new MilkshakeCauldronBlock(milkshakeMap("salmonberry")));

	// Cantaloupe
	public static final RegistryObject<Item> CANTALOUPE_SEEDS = ITEMS.register("cantaloupe_seeds",
			() -> new ItemNameBlockItem(CANTALOUPE_PLANT.get(), ModItems.basicItem()));
	public static final RegistryObject<Item> CANTALOUPE_BREAD = food("cantaloupe_bread", DelightfulBackportFoods.CANTALOUPE_BREAD);
	public static final RegistryObject<Item> WRAPPED_CANTALOUPE = food("wrapped_cantaloupe", DelightfulBackportFoods.WRAPPED_CANTALOUPE);
	public static final RegistryObject<Item> CANTALOUPE_POPSICLE = ITEMS.register("cantaloupe_popsicle",
			() -> new ConsumableItem(ModItems.foodItem(DelightfulBackportFoods.CANTALOUPE_POPSICLE).craftRemainder(Items.STICK), true));
	public static final RegistryObject<Item> STUFFED_CANTALOUPE_BLOCK_ITEM = ITEMS.register("stuffed_cantaloupe_block",
			() -> new BlockItem(STUFFED_CANTALOUPE_BLOCK.get(), ModItems.basicItem().stacksTo(1)));
	public static final RegistryObject<Item> STUFFED_CANTALOUPE = ITEMS.register("stuffed_cantaloupe",
			() -> new ConsumableItem(ModItems.bowlFoodItem(DelightfulBackportFoods.STUFFED_CANTALOUPE), true));
	public static final RegistryObject<Item> CANTALOUPE_GUMMY = ITEMS.register("cantaloupe_gummy",
			() -> new GummyItem(ModItems.foodItem(DelightfulBackportFoods.CANTALOUPE_GUMMY)));

	// Cactus
	public static final RegistryObject<Item> CACTUS_CHILI = ITEMS.register("cactus_chili",
			() -> new ConsumableItem(ModItems.bowlFoodItem(DelightfulBackportFoods.CACTUS_CHILI), true));
	public static final RegistryObject<Item> CACTUS_SOUP = ITEMS.register("cactus_soup",
			() -> new ConsumableItem(ModItems.bowlFoodItem(DelightfulBackportFoods.CACTUS_SOUP), true));
	public static final RegistryObject<Item> CACTUS_SOUP_CUP = ITEMS.register("cactus_soup_cup",
			() -> new CupItem(ModItems.foodItem(DelightfulBackportFoods.CACTUS_SOUP_CUP).stacksTo(16)));

	// Baklava (3.8.1's Mulberry Pie is not taken: Create Central Kitchen already places Upgrade Aquatic's and slices it)
	public static final RegistryObject<Item> BAKLAVA_ITEM = ITEMS.register("baklava",
			() -> new BlockItem(BAKLAVA.get(), ModItems.basicItem()));
	public static final RegistryObject<Item> BAKLAVA_SLICE = food("baklava_slice", DelightfulBackportFoods.BAKLAVA_SLICE);

	// Milkshakes, gummies
	public static final RegistryObject<Item> MATCHA_MILKSHAKE = ITEMS.register("matcha_milkshake",
			() -> new ShakeItem(ModItems.drinkItem().food(DelightfulBackportFoods.MATCHA_MILKSHAKE)));
	public static final RegistryObject<Item> SALMONBERRY_MILKSHAKE = ITEMS.register("salmonberry_milkshake",
			() -> new ShakeItem(ModItems.drinkItem().food(DelightfulBackportFoods.SALMONBERRY_MILKSHAKE)));
	public static final RegistryObject<Item> MATCHA_GUMMY = ITEMS.register("matcha_gummy",
			() -> new GummyItem(ModItems.foodItem(DelightfulBackportFoods.MATCHA_GUMMY)));

	// Nuts
	public static final RegistryObject<Item> ROASTED_ACORN = ITEMS.register("roasted_acorn",
			() -> new ConsumableItem(ModItems.foodItem(DelightfulBackportFoods.ROASTED_ACORN)));
	public static final RegistryObject<Item> NUT_DOUGH = ITEMS.register("nut_dough",
			() -> new ConsumableItem(ModItems.foodItem(DelightfulBackportFoods.NUT_DOUGH)));

	// Knives (FD's knife attack values, as 3.8.1's DKnifeItem)
	public static final RegistryObject<Item> ALUMINUM_KNIFE = ITEMS.register("aluminum_knife",
			() -> new KnifeItem(DelightfulBackportTiers.ALUMINUM, 0.5F, -2.0F, ModItems.basicItem()));
	public static final RegistryObject<Item> CERTUS_QUARTZ_KNIFE = ITEMS.register("certus_quartz_knife",
			() -> new KnifeItem(DelightfulBackportTiers.CERTUS_QUARTZ, 0.5F, -2.0F, ModItems.basicItem()));
	public static final RegistryObject<Item> FLUIX_KNIFE = ITEMS.register("fluix_knife",
			() -> new FluixKnifeItem(ModItems.basicItem()));

	private DelightfulBackportRegistry() {}

	/** A food with its effects in the tooltip. */
	private static RegistryObject<Item> food(String name, FoodProperties food) {
		return ITEMS.register(name, () -> new ConsumableItem(ModItems.foodItem(food), true));
	}

	private static java.util.Map<Item, CauldronInteraction> milkshakeMap(String flavour) {
		return ModList.get().isLoaded("neapolitan") ? DelightfulMilkshakes.map(flavour) : CauldronInteraction.newInteractionMap();
	}

	static void register(IEventBus modBus) {
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
	}
}
