package com.SaintAlfonzo.creativeengineering.food;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Powdered sugar block {@code creativeengineering:powdered_sugar_block} and its bucket
 * {@code creativeengineering:powdered_sugar_bucket}, the powder snow pair. The plain item
 * {@code creativeengineering:powdered_sugar} and every recipe are KubeJS (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/baking_ingredients.js,
 * kubejs/data/creative_delight/kubejs/creativeengineering/powdered_sugar.js). TODO dishes/ingredients #16.
 */
public final class PowderedSugarRegistry {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeEngineering.MODID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEngineering.MODID);

    // Powder snow's stats (strength 0.25, powder snow sounds, dynamic shape); no block item, the bucket places it.
    public static final RegistryObject<Block> POWDERED_SUGAR_BLOCK = BLOCKS.register("powdered_sugar_block",
            () -> new PowderedSugarBlock(BlockBehaviour.Properties.copy(Blocks.POWDER_SNOW)));

    public static final RegistryObject<Item> POWDERED_SUGAR_BUCKET = ITEMS.register("powdered_sugar_bucket",
            () -> new SolidBucketItem(POWDERED_SUGAR_BLOCK.get(), SoundEvents.BUCKET_EMPTY_POWDER_SNOW,
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET).tab(CreativeModeTab.TAB_FOOD)));

    private PowderedSugarRegistry() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
