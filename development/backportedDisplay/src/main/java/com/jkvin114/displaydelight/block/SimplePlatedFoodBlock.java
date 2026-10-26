package com.jkvin114.displaydelight.block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SimplePlatedFoodBlock extends AbstractStackablePlatedFoodBlock {
   public SimplePlatedFoodBlock(Properties props) {
      super(props);
   }

   @Override
   public int getMaxStackable() {
      return 1;
   }
}
