package com.SaintAlfonzo.creativeengineering.block;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;

public class CreativeEngineeringBlocks {

    public static final RegistryObject<Block> DARK_FIRE = CreativeEngineering.BLOCKS.register("dark_fire",
            () -> new DarkFireBlock(BlockBehaviour.Properties.of(Material.FIRE)
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 5)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    // Call this from the mod constructor, same place CreativeEngineeringItems
    // gets registered. Without it, DeferredRegister never hooks into the mod
    // event bus and DARK_FIRE silently never registers - no error, it just
    // won't exist at runtime.
    public static void register(IEventBus modEventBus) {
        CreativeEngineering.BLOCKS.register(modEventBus);
    }
}
