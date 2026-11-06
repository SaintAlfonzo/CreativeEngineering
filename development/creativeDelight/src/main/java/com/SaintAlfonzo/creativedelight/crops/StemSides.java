package com.SaintAlfonzo.creativedelight.crops;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

/**
 * The {@code north/east/south/west} booleans the mixin adds to every attached stem (farming/crops #20): "a fruit this
 * stem grew is on that side", besides the one {@code facing} points at. Kept apart from {@link StemExtraFruit}
 * because {@code mixin/minecraft/AttachedStemExtraFruitMixin} calls it while vanilla's Blocks class initialises.
 */
public final class StemSides {

    private static Method registerDefaultState;

    private StemSides() {}

    /** The property of a horizontal side. */
    public static BooleanProperty grown(Direction side) {
        return switch (side) {
            case NORTH -> BlockStateProperties.NORTH;
            case EAST -> BlockStateProperties.EAST;
            case SOUTH -> BlockStateProperties.SOUTH;
            default -> BlockStateProperties.WEST;
        };
    }

    /**
     * Re-registers the block's default state with all four sides false: a boolean's first value is true, so
     * {@code StateDefinition.any()} (vanilla's default) would start every attached stem connected on all sides.
     * Block.registerDefaultState (m_49959_) is protected; reached by reflection (an @Invoker on an SRG name does
     * not compile against the dev mappings).
     */
    public static void unconnectedDefault(Block block) {
        BlockState def = block.defaultBlockState();
        if (!def.hasProperty(BlockStateProperties.NORTH)) return;
        def = def.setValue(BlockStateProperties.NORTH, false).setValue(BlockStateProperties.EAST, false)
                .setValue(BlockStateProperties.SOUTH, false).setValue(BlockStateProperties.WEST, false);
        try {
            if (registerDefaultState == null) {
                registerDefaultState = ObfuscationReflectionHelper.findMethod(Block.class, "m_49959_", BlockState.class);
            }
            registerDefaultState.invoke(block, def);
        } catch (Exception e) {
            LogUtils.getLogger().error("[stem_fruit] could not reset the attached stem default state of {}", block, e);
        }
    }
}
