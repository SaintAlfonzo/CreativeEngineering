package net.satisfy.vinery.legacy.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.satisfy.foundation.block.FacingBlock;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.satisfy.vinery.legacy.item.LegacyCalendarItem;

public class LegacyCalendarBlock extends FacingBlock {
   private static final Map<Direction, VoxelShape> BOUNDING_SHAPES = Maps.newEnumMap(
      ImmutableMap.of(
         Direction.NORTH,
         Block.box(1.0, 1.0, 15.0, 15.0, 15.0, 16.0),
         Direction.SOUTH,
         Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 1.0),
         Direction.WEST,
         Block.box(15.0, 1.0, 1.0, 16.0, 15.0, 15.0),
         Direction.EAST,
         Block.box(0.0, 1.0, 1.0, 1.0, 15.0, 15.0)
      )
   );

   public LegacyCalendarBlock(Properties settings) {
      super(settings);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return getBoundingShape(state);
   }

   public static VoxelShape getBoundingShape(BlockState state) {
      return BOUNDING_SHAPES.get(state.getValue(FACING));
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      Direction direction = (Direction)state.getValue(FACING);
      BlockPos blockPos = pos.relative(direction.getOpposite());
      BlockState blockState = world.getBlockState(blockPos);
      return blockState.isFaceSturdy(world, blockPos, direction);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState blockState = this.defaultBlockState();
      Level worldView = ctx.getLevel();
      BlockPos blockPos = ctx.getClickedPos();

      for (Direction direction : ctx.getNearestLookingDirections()) {
         if (direction.getAxis().isHorizontal() && (blockState = (BlockState)blockState.setValue(FACING, direction.getOpposite())).canSurvive(worldView, blockPos)
            )
          {
            return blockState;
         }
      }

      return null;
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
      return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : state;
   }

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.decoration.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
      if (world instanceof Level level) {
         tooltip.add(LegacyCalendarItem.getTime(level));
      }
   }
}
