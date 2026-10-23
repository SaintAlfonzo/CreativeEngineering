package net.mehdinoui.veggiesdelight.common.block;

import net.mehdinoui.veggiesdelight.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

// Mature Dandelion: a flower (Luck in suspicious stew) that pops another Mature Dandelion item on every bone meal.
public class DandelionBlock extends FlowerBlock implements BonemealableBlock {
    public DandelionBlock(Properties properties) {
        super(() -> MobEffects.LUCK, 5, properties);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(ModBlocks.MATURE_DANDELION.get()));
    }
}
