package net.ribs.vintagedelight.block.custom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;

// Upstream's base jar block plus KOTS 0.3.1's merge of the nine per-jar subclasses into one: the block knows its own
// item (the filled or empty jar, registered under the block id) and pops that item on an empty-hand click.
public class MasonJarBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final IntegerProperty JARS = IntegerProperty.create("jars", 1, 4);
   public static final DirectionProperty FACING = DirectionProperty.create("facing", Plane.HORIZONTAL);
   private static final VoxelShape ONE_JAR_AABB = Block.box(5.0, 0.0, 5.0, 11.0, 11.75, 11.0);
   private static final VoxelShape TWO_JAR_AABB = Shapes.or(
      Block.box(9.0, 0.0, 9.0, 15.0, 11.75, 15.0), Block.box(1.0, 0.0, 9.0, 9.0, 11.75, 15.0)
   );
   private static final VoxelShape THREE_JAR_AABB = Shapes.or(TWO_JAR_AABB, Block.box(1.0, 0.0, 1.0, 7.0, 11.75, 7.0));
   private static final VoxelShape FOUR_JAR_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 11.75, 15.0);
   public static final int MAX_JARS = 4;

   private final Supplier<? extends Item> jarItem;

   public MasonJarBlock(Properties properties, Supplier<? extends Item> jarItem) {
      super(properties);
      this.jarItem = jarItem;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(JARS, 1)).setValue(FACING, Direction.NORTH))
            .setValue(WATERLOGGED, Boolean.FALSE)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{JARS, FACING, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
      FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
      boolean isWaterlogged = fluidstate.getType() == Fluids.WATER;
      if (blockstate.is(this)) {
         int jars = (Integer)blockstate.getValue(JARS);
         if (jars < 4) {
            return (BlockState)((BlockState)blockstate.setValue(JARS, jars + 1)).setValue(WATERLOGGED, isWaterlogged);
         }
      }

      Direction facing = context.getHorizontalDirection().getOpposite();
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, facing)).setValue(JARS, 1)).setValue(WATERLOGGED, isWaterlogged);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      Direction direction = (Direction)state.getValue(FACING);

      return this.rotateShape(switch (state.getValue(JARS)) {
         case 1 -> ONE_JAR_AABB;
         case 2 -> TWO_JAR_AABB;
         case 3 -> THREE_JAR_AABB;
         default -> FOUR_JAR_AABB;
      }, direction);
   }

   private VoxelShape rotateShape(VoxelShape shape, Direction direction) {
      if (direction == Direction.NORTH) {
         return shape;
      } else {
         List<VoxelShape> rotatedShapes = new ArrayList<>();
         shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double[] rotated = this.rotate(minX, minZ, maxX, maxZ, direction);
            rotatedShapes.add(Shapes.box(rotated[0], minY, rotated[1], rotated[2], maxY, rotated[3]));
         });
         return rotatedShapes.stream().reduce(Shapes::or).orElse(Shapes.empty());
      }
   }

   private double[] rotate(double minX, double minZ, double maxX, double maxZ, Direction direction) {
      switch (direction) {
         case EAST:
            return new double[]{1.0 - maxZ, minX, 1.0 - minZ, maxX};
         case SOUTH:
            return new double[]{1.0 - maxX, 1.0 - maxZ, 1.0 - minX, 1.0 - minZ};
         case WEST:
            return new double[]{minZ, 1.0 - maxX, maxZ, 1.0 - minX};
         default:
            return new double[]{minX, minZ, maxX, maxZ};
      }
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      return !useContext.isSecondaryUseActive() && useContext.getItemInHand().getItem() == this.asItem() && (Integer)state.getValue(JARS) < 4;
   }

   public boolean tryPlaceJar(Level world, BlockPos pos, Player player, ItemStack itemStack) {
      if (!world.getBlockState(pos).is(this)) {
         pos = pos.relative(player.getDirection().getOpposite());
      }

      BlockState state = world.getBlockState(pos);
      if (state.is(this)) {
         int jars = (Integer)state.getValue(JARS);
         if (jars < 4) {
            world.setBlock(pos, (BlockState)state.setValue(JARS, jars + 1), 3);
            itemStack.shrink(1);
            return true;
         }
      } else if (world.isEmptyBlock(pos)) {
         world.setBlock(pos, this.defaultBlockState(), 3);
         itemStack.shrink(1);
         return true;
      }

      return false;
   }

   public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
      return new ItemStack(this.jarItem.get());
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack itemInHand = player.getItemInHand(hand);
      if (!world.isClientSide && hand == InteractionHand.MAIN_HAND && itemInHand.isEmpty()) {
         int jars = (Integer)state.getValue(JARS);
         if (jars > 1) {
            world.setBlock(pos, (BlockState)state.setValue(JARS, jars - 1), 3);
            popResource(world, pos, new ItemStack(this.jarItem.get()));
            return InteractionResult.SUCCESS;
         }

         if (jars == 1) {
            world.removeBlock(pos, false);
            popResource(world, pos, new ItemStack(this.jarItem.get()));
            return InteractionResult.SUCCESS;
         }
      }

      return super.use(state, world, pos, player, hand, hit);
   }
}
