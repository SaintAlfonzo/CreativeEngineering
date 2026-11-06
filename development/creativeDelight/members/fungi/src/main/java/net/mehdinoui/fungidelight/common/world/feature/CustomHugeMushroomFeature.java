package net.mehdinoui.fungidelight.common.world.feature;

import com.mojang.serialization.Codec;
import net.mehdinoui.fungidelight.common.block.mushrooms.InkyGooVeilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import vectorwing.farmersdelight.common.tag.ModTags;

// 1.19.2: BlockState.canBeReplaced() (1.20) is getMaterial().isReplaceable().
public abstract class CustomHugeMushroomFeature extends Feature<NoneFeatureConfiguration> {
   protected CustomHugeMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   protected boolean placeStem(LevelAccessor world, BlockPos pos, int minHeight, int maxHeight, BlockState stemBlock) {
      int stemHeight = minHeight + world.getRandom().nextInt(maxHeight - minHeight + 1);

      for (int y = 0; y < stemHeight; y++) {
         BlockPos stemPos = pos.above(y);
         if (world.getBlockState(stemPos).getMaterial().isReplaceable()) {
            world.setBlock(stemPos, stemBlock, 3);
         }
      }

      return stemHeight > 0;
   }

   protected void placeCapLayer(LevelAccessor world, BlockPos center, int[][] offsets, BlockState block, boolean hanging) {
      int minX = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int minZ = Integer.MAX_VALUE;
      int maxZ = Integer.MIN_VALUE;

      for (int[] offset : offsets) {
         minX = Math.min(minX, offset[0]);
         maxX = Math.max(maxX, offset[0]);
         minZ = Math.min(minZ, offset[1]);
         maxZ = Math.max(maxZ, offset[1]);
      }

      int centerX = (minX + maxX) / 2;
      int centerZ = (minZ + maxZ) / 2;
      if (!hanging) {
         for (int[] offset : offsets) {
            BlockPos blockPos = center.offset(offset[0] - centerX, 0, offset[1] - centerZ);
            if (world.getBlockState(blockPos).getMaterial().isReplaceable()) {
               world.setBlock(blockPos, block, 3);
            }
         }
      } else {
         RandomSource random = world.getRandom();

         for (int[] offsetx : offsets) {
            BlockPos blockPos = center.offset(offsetx[0] - centerX, 0, offsetx[1] - centerZ);
            if (world.getBlockState(blockPos).getMaterial().isReplaceable() && random.nextFloat() < 0.6F) {
               int length = 1 + random.nextInt(2);

               for (int i = 0; i < length; i++) {
                  BlockPos hangingPos = blockPos.below(i);
                  boolean connected = length - i > 1;
                  BlockState finalState = (BlockState)block.setValue(InkyGooVeilBlock.CONNECTED, connected);
                  if (!world.getBlockState(hangingPos).getMaterial().isReplaceable()) {
                     break;
                  }

                  world.setBlock(hangingPos, finalState, 3);
               }
            }
         }
      }
   }

   protected boolean isValidSpace(LevelAccessor world, BlockPos pos, int mushroomHeight, int capHeight, int mushroomRadius) {
      BlockState state = world.getBlockState(pos.below());
      if (state.is(BlockTags.DIRT) && !state.is(ModTags.MUSHROOM_COLONY_GROWABLE_ON)) {
         if (pos.getY() + mushroomHeight >= world.getMaxBuildHeight()) {
            return false;
         } else {
            for (int y = 0; y <= mushroomHeight; y++) {
               if (!world.getBlockState(pos.above(y)).getMaterial().isReplaceable()) {
                  return false;
               }
            }

            for (int yx = mushroomHeight; yx >= capHeight; yx--) {
               for (int x = -mushroomRadius; x <= mushroomRadius; x++) {
                  for (int z = -mushroomRadius; z <= mushroomRadius; z++) {
                     if (!world.getBlockState(pos.offset(x, yx, z)).getMaterial().isReplaceable()) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }
}
