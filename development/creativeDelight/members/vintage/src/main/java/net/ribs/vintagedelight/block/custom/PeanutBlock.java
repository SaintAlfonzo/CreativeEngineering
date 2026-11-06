package net.ribs.vintagedelight.block.custom;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.ribs.vintagedelight.item.ModItems;

public class PeanutBlock extends CropBlock {
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

   public PeanutBlock(Properties pProperties) {
      super(pProperties);
   }

   protected ItemLike getBaseSeedId() {
      return (ItemLike)ModItems.PEANUT.get();
   }

   public IntegerProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{AGE});
   }
}
