package com.SaintAlfonzo.creativedelight.hearth.nest;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Registration of the nest (plan_hearth_crow_salt.md section 5), built for the crow eval (hearth_and_harvest.md 5.10 N26-2). */
public final class HHNestRegistry {
    private static final String MODID = HearthAndHarvestMod.MODID;

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);

    public static final RegistryObject<Block> NEST = BLOCKS.register("nest", () -> new NestBlock(Properties.copy(Blocks.HAY_BLOCK).noOcclusion()));
    public static final RegistryObject<Item> NEST_ITEM = ITEMS.register("nest", () -> new BlockItem(NEST.get(), ModItems.basicItem()));
    public static final RegistryObject<BlockEntityType<NestBlockEntity>> NEST_ENTITY = BLOCK_ENTITIES.register("nest",
            () -> BlockEntityType.Builder.of(NestBlockEntity::new, NEST.get()).build(null));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NEST_FEATURE = FEATURES.register("nest", () -> new NestFeature(NoneFeatureConfiguration.CODEC));

    private HHNestRegistry() {
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        FEATURES.register(bus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> NestClient.init(bus));
    }
}
