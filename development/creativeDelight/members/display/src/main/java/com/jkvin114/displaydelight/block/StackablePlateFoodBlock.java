package com.jkvin114.displaydelight.block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class StackablePlateFoodBlock extends AbstractStackablePlatedFoodBlock {
   private final int maxStacks;

   public StackablePlateFoodBlock(Properties props, int stacks) {
      super(props);
      if (stacks > 0 && stacks <= 10) {
         this.maxStacks = stacks;
      } else {
         throw new IllegalArgumentException("Stack size should be between 1 and 10");
      }
   }

   @Override
   public int getMaxStackable() {
      return this.maxStacks;
   }
}
