package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.jkvin114.displaydelight.block.FoodBlock;
import com.jkvin114.displaydelight.init.BlockAssociations;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/** The Display Delight half of {@link DisplayDelightDishes}: only loaded when Display Delight is. */
final class DisplayDelightDishBlocks {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeDelight.NS);
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private record Entry(DisplayDelightDishes.Dish dish, RegistryObject<Block> block) {}

    private DisplayDelightDishBlocks() {}

    static void register(IEventBus modEventBus, List<DisplayDelightDishes.Dish> dishes) {
        for (DisplayDelightDishes.Dish d : dishes) {
            // Display Delight's own food blocks: wood material and sound, no occlusion, broken instantly
            ENTRIES.add(new Entry(d, BLOCKS.register(d.block(), () -> new FoodBlock(
                    BlockBehaviour.Properties.of(Material.WOOD).noOcclusion().instabreak().sound(SoundType.WOOD)))));
        }
        BLOCKS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(DisplayDelightDishBlocks::onLevelLoad);
    }

    /**
     * Display Delight fills its item <-> block maps on every server level load, from its own blocks' drops; these join
     * the same maps (put-if-absent, so a later Display Delight block for the same food would not be replaced).
     */
    private static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        for (Entry e : ENTRIES) {
            Block block = e.block().get();
            if (!ForgeRegistries.ITEMS.containsKey(e.dish().item())) {
                LOGGER.warn("[creativeengineering] Display Delight dish {}: item {} not installed", e.dish().block(), e.dish().item());
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(e.dish().item());
            BlockAssociations.addToMap(item, block, false);
        }
    }
}
