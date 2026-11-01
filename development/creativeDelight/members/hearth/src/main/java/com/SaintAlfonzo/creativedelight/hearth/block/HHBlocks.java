package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Blocks, ids and base properties as 1.4.1. */
public final class HHBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HearthAndHarvestMod.MODID);

    public static final RegistryObject<Block> RASPBERRY_BUSH = BLOCKS.register("raspberry_bush",
            () -> new RaspberryBushBlock(Properties.copy(Blocks.SWEET_BERRY_BUSH)));

    private HHBlocks() {
    }
}
