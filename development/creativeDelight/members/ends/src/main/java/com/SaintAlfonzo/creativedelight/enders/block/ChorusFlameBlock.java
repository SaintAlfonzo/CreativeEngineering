package com.SaintAlfonzo.creativedelight.enders.block;

import com.SaintAlfonzo.creativedelight.enders.EAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

/** Chorusflame (1.1.4 ChorusFlameBlock): three stages, one stage per random tick that passes the soil tier's chance. */
public class ChorusFlameBlock extends EndPlantBlock {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final VoxelShape[] SHAPES = {
            Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0)};

    public ChorusFlameBlock(Properties properties) {
        super(properties, SHAPES[0]);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) return;
        BlockState soil = level.getBlockState(pos.below());
        if (!EndSoil.isSoil(soil)) return;
        double chance = EndSoil.isFast(soil) ? EAConfig.CHORUSFLAME_GROWTH_FAST.get() : EAConfig.CHORUSFLAME_GROWTH_SLOW.get();
        // fire Forge's crop-grow event so the pack's crop seasons apply (patcherMod seasons/CropSeasons)
        if (random.nextDouble() < chance && ForgeHooks.onCropsGrowPre(level, pos, state, true)) {
            BlockState grown = state.setValue(AGE, age + 1);
            level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[Math.min(state.getValue(AGE), MAX_AGE)];
    }
}
