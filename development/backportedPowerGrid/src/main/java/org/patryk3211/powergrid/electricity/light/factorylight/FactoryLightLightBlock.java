// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.electricity.light.factorylight;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.electricity.light.bulb.ILightBulb;

public class FactoryLightLightBlock extends Block {
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 1);
    // Port: 1.20 Properties.replaceable() / pushReaction(DESTROY) are Material properties on 1.19.2 (ModdedBlocks uses
    // it as the initial properties); noParticlesOnBreak() has no 1.19.2 twin (the block has no shape and cannot be hit).
    public static final Material MATERIAL = new Material(MaterialColor.NONE, false, false, false, false, false, true, PushReaction.DESTROY);

    public FactoryLightLightBlock(Properties properties) {
        super(properties
                .lightLevel(state -> state.getValue(POWER) == 1 ? ILightBulb.LIGHT_LEVEL_FULL_POWER : ILightBulb.LIGHT_LEVEL_LOW_POWER)
                .noCollission()
                .noLootTable()
                .noOcclusion()
                .air());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }
}
