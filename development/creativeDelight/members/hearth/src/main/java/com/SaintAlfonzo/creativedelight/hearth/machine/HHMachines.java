package com.SaintAlfonzo.creativedelight.hearth.machine;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Block machines of Hearth and Harvest (hearth_and_harvest.md 3.1, 3.2): jug, trough, sprinkler, crate, stomping basin. */
public final class HHMachines {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HearthAndHarvestMod.MODID);

    public static final RegistryObject<Block> JUG = BLOCKS.register("jug",
            () -> new JugBlock(Properties.copy(Blocks.IRON_BARS).strength(2.0F, 3.0F).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<Block> TROUGH = BLOCKS.register("trough",
            () -> new TroughBlock(Properties.copy(Blocks.CAULDRON)));
    public static final RegistryObject<Block> SPRINKLER = BLOCKS.register("sprinkler",
            () -> new SprinklerBlock(Properties.copy(Blocks.BARREL).noOcclusion()));
    public static final RegistryObject<Block> CRATE = BLOCKS.register("crate",
            () -> new CrateBlock(Properties.copy(Blocks.BARREL).noOcclusion()));
    public static final RegistryObject<Block> STOMPING_BASIN = BLOCKS.register("stomping_basin",
            () -> new StompingBasinBlock(Properties.copy(Blocks.BARREL).noOcclusion()));

    public static final RegistryObject<Item> JUG_ITEM = ITEMS.register("jug", () -> new JugBlockItem(JUG.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> TROUGH_ITEM = ITEMS.register("trough", () -> new TroughBlockItem(TROUGH.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> SPRINKLER_ITEM = ITEMS.register("sprinkler",
            () -> new net.minecraft.world.item.BlockItem(SPRINKLER.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> CRATE_ITEM = ITEMS.register("crate", () -> new CrateBlockItem(CRATE.get(), ModItems.basicItem().stacksTo(16)));
    public static final RegistryObject<Item> STOMPING_BASIN_ITEM = ITEMS.register("stomping_basin",
            () -> new net.minecraft.world.item.BlockItem(STOMPING_BASIN.get(), ModItems.basicItem()));

    public static final RegistryObject<BlockEntityType<JugBlockEntity>> JUG_BE = BLOCK_ENTITIES.register("jug",
            () -> BlockEntityType.Builder.of(JugBlockEntity::new, JUG.get()).build(null));
    public static final RegistryObject<BlockEntityType<TroughBlockEntity>> TROUGH_BE = BLOCK_ENTITIES.register("trough",
            () -> BlockEntityType.Builder.of(TroughBlockEntity::new, TROUGH.get()).build(null));
    public static final RegistryObject<BlockEntityType<SprinklerBlockEntity>> SPRINKLER_BE = BLOCK_ENTITIES.register("sprinkler",
            () -> BlockEntityType.Builder.of(SprinklerBlockEntity::new, SPRINKLER.get()).build(null));
    public static final RegistryObject<BlockEntityType<CrateBlockEntity>> CRATE_BE = BLOCK_ENTITIES.register("crate",
            () -> BlockEntityType.Builder.of(CrateBlockEntity::new, CRATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<StompingBasinBlockEntity>> STOMPING_BASIN_BE = BLOCK_ENTITIES.register("stomping_basin",
            () -> BlockEntityType.Builder.of(StompingBasinBlockEntity::new, STOMPING_BASIN.get()).build(null));

    public static final RegistryObject<RecipeType<StompingRecipe>> STOMPING_TYPE = RECIPE_TYPES.register("stomping", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return HearthAndHarvestMod.MODID + ":stomping";
        }
    });
    public static final RegistryObject<RecipeSerializer<StompingRecipe>> STOMPING_SERIALIZER = RECIPE_SERIALIZERS.register("stomping", StompingRecipe.Serializer::new);

    private HHMachines() {
    }

    /** Single hook from the mod constructor. */
    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> CrateBlockItem.registerDispense(CRATE_ITEM.get())));
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStoppedEvent event) -> {
            StompingBasinBlock.clearAirborneTracking();
            TroughFoods.clear();
        });
    }
}
