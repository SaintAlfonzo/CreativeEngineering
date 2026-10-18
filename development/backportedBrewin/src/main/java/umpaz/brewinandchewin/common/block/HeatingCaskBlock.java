package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;

public class HeatingCaskBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   private static final VoxelShape STEAM_AREA = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);

   public HeatingCaskBlock(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      ItemStack heldStack = player.getItemInHand(hand);
      if (this.isSpaceAbove(level, pos)
         && (heldStack.is(Items.WATER_BUCKET) || heldStack.is(Items.POTION) && PotionUtils.getPotion(heldStack) == Potions.WATER)) {
         for (int i = 0; i < 20; i++) {
            RandomSource randomsource = level.getRandom();
            level.addParticle(
               ParticleTypes.LARGE_SMOKE,
               (double)pos.getX() + 0.5 + randomsource.nextDouble() / 4.0 * (double)(randomsource.nextBoolean() ? 1 : -1),
               (double)pos.getY() + 1.1,
               (double)pos.getZ() + 0.5 + randomsource.nextDouble() / 4.0 * (double)(randomsource.nextBoolean() ? 1 : -1),
               randomsource.nextDouble() / 4.0 * (double)(randomsource.nextBoolean() ? 1 : -1),
               0.15,
               randomsource.nextDouble() / 4.0 * (double)(randomsource.nextBoolean() ? 1 : -1)
            );
            if (i % 3 == 0) {
               level.playSound(player, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
         }

         if (!player.isCreative()) {
            if (heldStack.is(Items.WATER_BUCKET)) {
               player.setItemInHand(hand, BrewinAndChewin.getHelper().getCraftingRemainingItem(heldStack));
            } else {
               player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(5) == 0 && this.isSpaceAbove(level, pos)) {
         level.addParticle(
            ParticleTypes.LARGE_SMOKE,
            (double)pos.getX() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1),
            (double)pos.getY() + 1.1,
            (double)pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1),
            0.0,
            0.005,
            0.0
         );
      }
   }

   public boolean isSpaceAbove(Level level, BlockPos pos) {
      if (level != null) {
         BlockState above = level.getBlockState(pos.above());
         return !Shapes.joinIsNotEmpty(STEAM_AREA, above.getShape(level, pos.above()), BooleanOp.AND);
      } else {
         return true;
      }
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rotation) {
      return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation(state.getValue(FACING)));
   }
}
