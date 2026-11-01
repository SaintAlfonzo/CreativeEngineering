package net.satisfy.vinery.legacy.block;

import net.satisfy.foundation.storage.StorageBlock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.satisfy.vinery.legacy.LegacyStorageTypes;
import net.satisfy.vinery.core.registry.TagRegistry;
import net.satisfy.foundation.util.ShapeUtil;

public class LegacyShelfBlock extends StorageBlock {
   private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
      VoxelShape shape = Shapes.empty();
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.25, 0.5, 1.0, 0.3125, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.125, 0.1875, 0.625, 0.1875, 0.25, 0.9375), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.8125, 0.1875, 0.625, 0.875, 0.25, 0.9375), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.125, 0.0625, 0.9375, 0.1875, 0.25, 1.0), BooleanOp.OR);
      return Shapes.joinUnoptimized(shape, Shapes.box(0.8125, 0.0625, 0.9375, 0.875, 0.25, 1.0), BooleanOp.OR);
   };
   public static final Map<Direction, VoxelShape> SHAPE = (Map<Direction, VoxelShape>)Util.make(new HashMap(), map -> {
      for (Direction direction : Plane.HORIZONTAL) {
         map.put(direction, ShapeUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
      }
   });

   public LegacyShelfBlock(Properties settings) {
      super(settings);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      Direction side = ctx.getClickedFace();
      BlockState blockState;
      if (side != Direction.DOWN && side != Direction.UP) {
         blockState = (BlockState)this.defaultBlockState().setValue(FACING, ctx.getClickedFace());
      } else {
         blockState = (BlockState)this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
      }

      return blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? blockState : null;
   }

   public Direction[] unAllowedDirections() {
      return new Direction[]{Direction.DOWN};
   }

   public boolean canInsertStack(ItemStack stack) {
      return !(stack.getItem() instanceof BlockItem) || stack.is(TagRegistry.IGNORE_BLOCK_ITEM);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPE.get(state.getValue(FACING));
   }

   public int size() {
      return 9;
   }

   public ResourceLocation type() {
      return LegacyStorageTypes.SHELF;
   }

   public int getSection(Float f, Float y) {
      float oneS = 0.11111111F;
      int nSection;
      if (f < oneS) {
         nSection = 0;
      } else if (f < oneS * 2.0F) {
         nSection = 1;
      } else if (f < oneS * 3.0F) {
         nSection = 2;
      } else if (f < oneS * 4.0F) {
         nSection = 3;
      } else if (f < oneS * 5.0F) {
         nSection = 4;
      } else if (f < oneS * 6.0F) {
         nSection = 5;
      } else if (f < oneS * 7.0F) {
         nSection = 6;
      } else if (f < oneS * 8.0F) {
         nSection = 7;
      } else {
         nSection = 8;
      }

      return 8 - nSection;
   }

   public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(world, pos)) {
         world.destroyBlock(pos, true);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      VoxelShape shape;
      Direction direction;
      switch (((Direction)state.getValue(FACING)).getOpposite()) {
         case EAST:
            shape = world.getBlockState(pos.east()).getShape(world, pos.east());
            direction = Direction.WEST;
            break;
         case SOUTH:
            shape = world.getBlockState(pos.south()).getShape(world, pos.south());
            direction = Direction.NORTH;
            break;
         case WEST:
            shape = world.getBlockState(pos.west()).getShape(world, pos.west());
            direction = Direction.EAST;
            break;
         default:
            shape = world.getBlockState(pos.north()).getShape(world, pos.north());
            direction = Direction.SOUTH;
      }

      return Block.isFaceFull(shape, direction);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canSurvive(world, pos)) {
         world.scheduleTick(pos, this, 1);
      }

      return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
   }

   /** Port: 1.3.10 used doapi:storage; the shared storage block entity is vinery:storage (ids rewritten on load, W2). */
   @Override
   public net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType() {
      return net.satisfy.vinery.core.registry.EntityTypeRegistry.STORAGE_ENTITY.get();
   }
}
