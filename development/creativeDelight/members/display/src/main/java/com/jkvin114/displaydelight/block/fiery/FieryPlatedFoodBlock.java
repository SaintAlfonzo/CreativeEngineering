package com.jkvin114.displaydelight.block.fiery;

import com.jkvin114.displaydelight.block.StackablePlateFoodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FieryPlatedFoodBlock extends StackablePlateFoodBlock {
   public FieryPlatedFoodBlock(Properties props, int stacks) {
      super(props, stacks);
   }

   public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
      super.animateTick(stateIn, level, pos, rand);
      RandomSource random = level.random;
      if (random.nextFloat() < 0.3F) {
         double x = (double)pos.getX() + 0.5 + (random.nextDouble() * 0.7 - 0.35);
         double y = (double)pos.getY() + 0.45;
         double z = (double)pos.getZ() + 0.5 + (random.nextDouble() * 0.7 - 0.35);
         level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
      }
   }
}
