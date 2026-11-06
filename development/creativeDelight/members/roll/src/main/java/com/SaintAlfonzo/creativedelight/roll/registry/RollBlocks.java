package com.SaintAlfonzo.creativedelight.roll.registry;

import com.SaintAlfonzo.creativedelight.roll.RollDelightMod;
import com.SaintAlfonzo.creativedelight.roll.content.RollMedleyBlock;
import com.SaintAlfonzo.creativedelight.roll.content.WrappedChopsticksBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** The three blocks: two roll medleys (FeastBlocks, roll_delight.md 3.2) and the wrapped chopsticks. */
public final class RollBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RollDelightMod.MODID);

    /** Servings top first: 3 mushroom roll slices, 3 salmon maki, 2 tobiko rolls. */
    public static final RegistryObject<Block> LEGENDARY_ROLL_MEDLEY = BLOCKS.register("legendary_roll_medley", () -> new RollMedleyBlock(medley(),
            List.of(RollItems.TOBIKO_ROLL, RollItems.TOBIKO_ROLL, RollItems.SALMON_MAKI, RollItems.SALMON_MAKI, RollItems.SALMON_MAKI,
                    RollItems.MUSHROOM_ROLL_SLICE, RollItems.MUSHROOM_ROLL_SLICE, RollItems.MUSHROOM_ROLL_SLICE)));
    /** Servings top first: 3 ancient roll slices, 3 golden roll slices, 2 nether rolls. */
    public static final RegistryObject<Block> MYTHICAL_ROLL_MEDLEY = BLOCKS.register("mythical_roll_medley", () -> new RollMedleyBlock(medley(),
            List.of(RollItems.NETHER_ROLL, RollItems.NETHER_ROLL, RollItems.GOLDEN_ROLL_SLICE, RollItems.GOLDEN_ROLL_SLICE, RollItems.GOLDEN_ROLL_SLICE,
                    RollItems.ANCIENT_ROLL_SLICE, RollItems.ANCIENT_ROLL_SLICE, RollItems.ANCIENT_ROLL_SLICE)));
    public static final RegistryObject<Block> WRAPPED_CHOPSTICKS = BLOCKS.register("wrapped_chopsticks", () -> new WrappedChopsticksBlock(
            BlockBehaviour.Properties.of(Material.DECORATION).sound(SoundType.BAMBOO).strength(0.0F, 10.0F).noOcclusion()));

    private RollBlocks() {
    }

    private static BlockBehaviour.Properties medley() {
        return BlockBehaviour.Properties.of(Material.CAKE).sound(SoundType.WOOL).strength(1.0F, 10.0F).noOcclusion();
    }
}
