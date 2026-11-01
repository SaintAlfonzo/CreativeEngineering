package net.satisfy.vinery.legacy.block;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.IPlantable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.satisfy.vinery.legacy.entity.LegacyFlowerPotBlockEntity;

public class LegacyFlowerPotBlock extends Block implements EntityBlock {
   private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
      VoxelShape shape = Shapes.empty();
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.78125, 0.4375, 0.21875, 0.90625, 0.625, 0.78125), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.09375, 0.4375, 0.21875, 0.21875, 0.625, 0.78125), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.21875, 0.0, 0.21875, 0.78125, 0.4375, 0.78125), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.09375, 0.4375, 0.09375, 0.90625, 0.625, 0.21875), BooleanOp.OR);
      return Shapes.joinUnoptimized(shape, Shapes.box(0.09375, 0.4375, 0.78125, 0.90625, 0.625, 0.90625), BooleanOp.OR);
   };
   // Must follow the supplier: static fields initialise in source order (the decompile had them swapped).
   private static final VoxelShape SHAPE = voxelShapeSupplier.get();

   public LegacyFlowerPotBlock(Properties settings) {
      super(settings);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (hand == InteractionHand.OFF_HAND) {
         return InteractionResult.PASS;
      } else {
         LegacyFlowerPotBlockEntity be = (LegacyFlowerPotBlockEntity)world.getBlockEntity(pos);
         if (be != null && !player.isShiftKeyDown()) {
            ItemStack handStack = player.getItemInHand(hand);
            Item flower = be.getFlower();
            if (handStack.isEmpty()) {
               if (flower != null) {
                  if (!world.isClientSide) {
                     player.addItem(flower.getDefaultInstance());
                     be.setFlower(null);
                  }

                  return InteractionResult.sidedSuccess(world.isClientSide);
               }
            } else if (this.fitInPot(handStack) && flower == null) {
               if (!world.isClientSide) {
                  be.setFlower(handStack.getItem());
                  if (!player.isCreative()) {
                     handStack.shrink(1);
                  }
               }

               return InteractionResult.sidedSuccess(world.isClientSide);
            }

            return super.use(state, world, pos, player, hand, hit);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   /**
    * Pack change: takes what a Supplementaries planter can grow (any plantable block: flowers, saplings, crops, cactus,
    * sugar cane, nether wart...) plus the tall flowers it took before; azaleas stay out.
    */
   public boolean fitInPot(ItemStack item) {
      if (item.is(Items.AZALEA) || item.is(Items.FLOWERING_AZALEA)) {
         return false;
      }
      return item.getItem() instanceof BlockItem blockItem
            && (item.is(ItemTags.TALL_FLOWERS) || blockItem.getBlock() instanceof IPlantable);
   }

   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         if (world.getBlockEntity(pos) instanceof LegacyFlowerPotBlockEntity be) {
            Item flower = be.getFlower();
            if (flower != null) {
               Containers.dropItemStack(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), flower.getDefaultInstance());
            }

            world.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, world, pos, newState, moved);
      }
   }

   public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
      return false;
   }

   public PushReaction getPistonPushReaction(BlockState state) {
      return PushReaction.IGNORE;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new LegacyFlowerPotBlockEntity(pos, state);
   }

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.canbeplaced.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
   }
}
