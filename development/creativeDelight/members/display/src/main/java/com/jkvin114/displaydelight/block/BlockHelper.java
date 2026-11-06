package com.jkvin114.displaydelight.block;

import com.jkvin114.displaydelight.init.DisplayTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockHelper {
   protected static boolean needSupport(LevelAccessor level, BlockPos current) {
      BlockState stateBelow = level.getBlockState(current.below());
      if (stateBelow.is(DisplayTags.SUPPORT_EXCEPTIONS)) {
         return false;
      } else if (stateBelow.getBlock() instanceof TrapDoorBlock && (Boolean)stateBelow.getValue(TrapDoorBlock.OPEN)) {
         return false;
      } else {
         return stateBelow.getBlock() instanceof AbstractItemBlock ? true : !stateBelow.isFaceSturdy(level, current.below(), Direction.UP, SupportType.CENTER);
      }
   }

   protected static boolean needSupport(LevelAccessor level, BlockPos below, BlockState stateBelow) {
      if (stateBelow.is(DisplayTags.SUPPORT_EXCEPTIONS)) {
         return false;
      } else if (stateBelow.getBlock() instanceof TrapDoorBlock && (Boolean)stateBelow.getValue(TrapDoorBlock.OPEN)) {
         return false;
      } else {
         return stateBelow.getBlock() instanceof AbstractItemBlock ? true : !stateBelow.isFaceSturdy(level, below, Direction.UP, SupportType.CENTER);
      }
   }
}
