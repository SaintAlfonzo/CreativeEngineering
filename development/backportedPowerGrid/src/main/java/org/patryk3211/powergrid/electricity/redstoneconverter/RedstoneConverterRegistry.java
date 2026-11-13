// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.electricity.redstoneconverter;

import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedBlocks;

public class RedstoneConverterRegistry {
    // Port: Create 6's SimpleRegistry does not exist on Create 0.5.1; a plain concurrent map does the same job here.
    public static final java.util.Map<Block, IRedstoneConverterBehaviour> REGISTRY = new java.util.concurrent.ConcurrentHashMap<>();

    public static void init() {
        REGISTRY.put(Blocks.CHEST, ContainerRedstoneConverterBehaviour.chest((ChestBlock) Blocks.CHEST));
        REGISTRY.put(Blocks.TRAPPED_CHEST, ContainerRedstoneConverterBehaviour.chest((ChestBlock) Blocks.TRAPPED_CHEST));

        var createGauge = new CreateGaugeBehaviour();
        REGISTRY.put(AllBlocks.STRESSOMETER.get(), createGauge);
        REGISTRY.put(AllBlocks.SPEEDOMETER.get(), createGauge);
        var pgGauge = new PowerGridGaugeBehaviour();
        REGISTRY.put(ModdedBlocks.VOLTAGE_METER.get(), pgGauge);
        REGISTRY.put(ModdedBlocks.CURRENT_METER.get(), pgGauge);
        REGISTRY.put(ModdedBlocks.POWER_METER.get(), pgGauge);

        registerUnifiedBlock(ModdedBlocks.BATTERY.get());
        registerUnifiedBlock(ModdedBlocks.GENERATOR_CLUTCH.get());

        REGISTRY.put(Blocks.DAYLIGHT_DETECTOR, new DaylightSensorBehaviour());
    }

    public static <T extends Block&IRedstoneConverterBehaviour> void registerUnifiedBlock(T block) {
        REGISTRY.put(block, block);
    }

    public static float get(Level level, BlockState state, BlockPos pos, Direction face) {
        var impl = REGISTRY.get(state.getBlock());
        if(impl != null)
            return impl.getSignal(level, state, pos, face);
        // Fallback handling.
        if(level.getBlockEntity(pos) instanceof Container) {
            impl = ContainerRedstoneConverterBehaviour.containerBE();
            REGISTRY.put(state.getBlock(), impl);
            return impl.getSignal(level, state, pos, face);
        }
        return DefaultRedstoneConverterBehaviour.INSTANCE.getSignal(level, state, pos, face);
    }
}
