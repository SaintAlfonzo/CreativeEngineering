package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.ribs.vintagedelight.block.entity.FermentingJarBlockEntity;
import net.ribs.vintagedelight.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class FermentingJarBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final VoxelShape SHAPE = Block.box(3.25, 0.0, 3.25, 12.75, 13.5, 12.75);

   public FermentingJarBlock(Properties pProperties) {
      super(pProperties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, Boolean.FALSE));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (pState.getBlock() != pNewState.getBlock()) {
         BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
         if (blockEntity instanceof FermentingJarBlockEntity) {
            ((FermentingJarBlockEntity)blockEntity).drops();
         }
      }

      super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (!pLevel.isClientSide()) {
         BlockEntity entity = pLevel.getBlockEntity(pPos);
         if (!(entity instanceof FermentingJarBlockEntity)) {
            throw new IllegalStateException("Our Container provider is missing!");
         }

         NetworkHooks.openScreen((ServerPlayer)pPlayer, (FermentingJarBlockEntity)entity, pPos);
      }

      return InteractionResult.sidedSuccess(pLevel.isClientSide());
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new FermentingJarBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide()
         ? null
         : createTickerHelper(
            pBlockEntityType,
            ModBlockEntities.FERMENTING_JAR_BE.get(),
            (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1)
         );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{WATERLOGGED});
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
   }
}
