package net.satisfy.vinery.legacy.block;

import net.satisfy.vinery.core.block.StemBlock;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.satisfy.vinery.core.item.GrapeBushSeedItem;
import net.satisfy.vinery.core.wine.GrapeType;
import net.minecraftforge.common.ForgeHooks;

public class LegacyLatticeStemBlock extends StemBlock {
   private static final VoxelShape LATTICE_SHAPE_N = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
   private static final VoxelShape LATTICE_SHAPE_E = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
   private static final VoxelShape LATTICE_SHAPE_S = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
   private static final VoxelShape LATTICE_SHAPE_W = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

   public LegacyLatticeStemBlock(Properties settings) {
      super(settings);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return switch ((Direction)state.getValue(FACING)) {
         case EAST -> LATTICE_SHAPE_E;
         case SOUTH -> LATTICE_SHAPE_S;
         case WEST -> LATTICE_SHAPE_W;
         default -> LATTICE_SHAPE_N;
      };
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

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (hand == InteractionHand.OFF_HAND) {
         return super.use(state, world, pos, player, hand, hit);
      } else {
         ItemStack stack = player.getItemInHand(hand);
         int age = (Integer)state.getValue(AGE);
         if (age > 0 && stack.getItem() == Items.SHEARS) {
            stack.hurtAndBreak(1, player, player2 -> player2.broadcastBreakEvent(player.getUsedItemHand()));
            if (age > 2) {
               this.dropGrapes(world, state, pos, null);
            }

            this.dropGrapeSeeds(world, state, pos, null);
            world.setBlock(pos, (BlockState)state.setValue(AGE, 0), 3);
            world.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.AMBIENT, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
         } else {
            if (stack.getItem() instanceof GrapeBushSeedItem seed && seed.getType().isLattice() && age == 0) {
               world.setBlock(pos, this.withAge(state, 1, seed.getType()), 3);
               if (!player.isCreative()) {
                  stack.shrink(1);
               }

               world.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.AMBIENT, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            }

            if (age > 2) {
               stack.hurtAndBreak(1, player, player2 -> player2.broadcastBreakEvent(player.getUsedItemHand()));
               this.dropGrapes(world, state, pos, null);
               world.setBlock(pos, (BlockState)state.setValue(AGE, 1), 3);
               world.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.AMBIENT, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            } else {
               return super.use(state, world, pos, player, hand, hit);
            }
         }
      }
   }

   public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      int i;
      // Port: fire Forge's crop-grow event so the pack's crop seasons apply (crops.md 2.12.1)
      if (!this.isMature(state) && (Integer)state.getValue(AGE) > 0 && world.getRawBrightness(pos, 0) >= 9 && (i = (Integer)state.getValue(AGE)) < 4
              && ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
         world.setBlock(pos, this.withAge(state, i + 1, (GrapeType)state.getValue(GRAPE)), 2);
      }

      super.randomTick(state, world, pos, random);
   }

   public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(world, pos)) {
         if ((Integer)state.getValue(AGE) > 0) {
            this.dropGrapeSeeds(world, state, pos, null);
         }

         if ((Integer)state.getValue(AGE) > 2) {
            this.dropGrapes(world, state, pos, null);
         }

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

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.lattice.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
      if (Screen.hasShiftDown()) {
         tooltip.add(Component.translatable("item.vinery.lattice2.tooltip"));
      } else {
         tooltip.add(Component.translatable("item.vinery.faucet.tooltip"));
      }
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{FACING});
   }
}
