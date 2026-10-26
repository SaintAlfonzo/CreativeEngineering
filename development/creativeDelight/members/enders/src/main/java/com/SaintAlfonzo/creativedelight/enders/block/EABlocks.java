package com.SaintAlfonzo.creativedelight.enders.block;

import com.SaintAlfonzo.creativedelight.enders.EndersAdditionsMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The four End plants, under Ender's Delight's namespace (ids and base block properties as 1.1.4). */
public final class EABlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EndersAdditionsMod.NAMESPACE);

    public static final RegistryObject<Block> AMBERVEIL_MUSHROOM = BLOCKS.register("amberveil_mushroom",
            () -> new EndPlantBlock(Properties.copy(Blocks.RED_MUSHROOM), Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0)));
    public static final RegistryObject<Block> ETHEREAL_SAFFRON_BUSH = BLOCKS.register("ethereal_saffron_bush",
            () -> new EndPlantBlock(Properties.copy(Blocks.SWEET_BERRY_BUSH), Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)));
    public static final RegistryObject<Block> CHORUS_FLAME_BLOCK = BLOCKS.register("chorus_flame_block",
            () -> new ChorusFlameBlock(Properties.copy(Blocks.RED_MUSHROOM)));
    public static final RegistryObject<Block> VOIDPEPPER_BLOCK = BLOCKS.register("voidpepper_block",
            () -> new VoidpepperBlock(Properties.copy(Blocks.TWISTING_VINES)));

    private EABlocks() {
    }
}
