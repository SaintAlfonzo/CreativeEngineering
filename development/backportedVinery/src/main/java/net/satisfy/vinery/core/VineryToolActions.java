package net.satisfy.vinery.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import net.satisfy.vinery.core.registry.ObjectRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Port: Architectury's {@code AxeItemHooks.addStrippable} / {@code ShovelItemHooks.addFlattenable} as Forge's
 * {@link BlockEvent.BlockToolModificationEvent}. Fuel values are in {@code VineryFuel}.
 */
public final class VineryToolActions {
    private static final Map<Block, Block> STRIPPABLES = new HashMap<>();

    private VineryToolActions() {
    }

    public static void register(Block log, Block stripped) {
        STRIPPABLES.put(log, stripped);
    }

    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        ToolAction action = event.getToolAction();
        BlockState state = event.getState();
        if (action == ToolActions.AXE_STRIP) {
            Block stripped = STRIPPABLES.get(state.getBlock());
            if (stripped != null) {
                BlockState result = stripped.defaultBlockState();
                if (state.hasProperty(RotatedPillarBlock.AXIS) && result.hasProperty(RotatedPillarBlock.AXIS)) {
                    result = result.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                }
                event.setFinalState(result);
            }
        } else if (action == ToolActions.SHOVEL_FLATTEN && state.is(ObjectRegistry.GRASS_SLAB.get())) {
            event.setFinalState(Blocks.DIRT_PATH.defaultBlockState());
        }
    }
}
