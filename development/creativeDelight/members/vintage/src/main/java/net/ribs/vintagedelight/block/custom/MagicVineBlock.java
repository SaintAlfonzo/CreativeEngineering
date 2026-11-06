package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.common.ToolAction;
import net.ribs.vintagedelight.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

public class MagicVineBlock extends RotatedPillarBlock {
   public MagicVineBlock(Properties pProperties) {
      super(pProperties);
   }

   public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return true;
   }

   public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return 5;
   }

   public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return 5;
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
      if (context.getItemInHand().getItem() instanceof AxeItem && state.is((Block)ModBlocks.MAGIC_VINE.get())) {
         return (BlockState)((Block)ModBlocks.STRIPPED_MAGIC_VINE.get()).defaultBlockState().setValue(AXIS, (Axis)state.getValue(AXIS));
      } else {
         return context.getItemInHand().getItem() instanceof AxeItem && state.is((Block)ModBlocks.MAGIC_VINE_BLOCK.get())
            ? (BlockState)((Block)ModBlocks.STRIPPED_MAGIC_VINE_BLOCK.get()).defaultBlockState().setValue(AXIS, (Axis)state.getValue(AXIS))
            : super.getToolModifiedState(state, context, toolAction, simulate);
      }
   }
}
