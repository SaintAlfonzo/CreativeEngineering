package net.satisfy.vinery.legacy.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.satisfy.vinery.core.registry.SoundEventRegistry;
import net.satisfy.foundation.util.ShapeUtil;
import net.satisfy.foundation.util.LibUtil;

public class LegacyKitchenSinkBlock extends Block {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty FILLED = BooleanProperty.create("filled");
   public static final BooleanProperty HAS_FAUCET = BooleanProperty.create("has_faucet");
   private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
      VoxelShape shape = Shapes.empty();
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.0, 0.125, 1.0, 0.75, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.75, 0.0, 1.0, 1.0, 0.1875), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.75, 0.75, 1.0, 1.0, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.75, 0.1875, 0.1859375, 1.0, 0.75), BooleanOp.OR);
      return Shapes.joinUnoptimized(shape, Shapes.box(0.8125, 0.75, 0.1875, 1.0, 1.0, 0.75), BooleanOp.OR);
   };
   private static final Supplier<VoxelShape> voxelShapeWithFaucetSupplier = () -> {
      VoxelShape shape = Shapes.empty();
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.0, 0.125, 1.0, 0.75, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.3984375, 1.171875, 0.84375, 0.4296875, 1.234375, 0.90625), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.4296875, 1.375, 0.625, 0.5546875, 1.5, 0.9375), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.4296875, 1.3125, 0.5, 0.5546875, 1.5, 0.625), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.4296875, 1.0625, 0.8125, 0.5546875, 1.375, 0.9375), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.3828125, 1.25, 0.453125, 0.6015625, 1.3125, 0.671875), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.3671875, 1.0, 0.75, 0.6171875, 1.0625, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.3671875, 1.125, 0.78125, 0.3984375, 1.3125, 0.96875), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.75, 0.0, 1.0, 1.0, 0.1875), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.75, 0.75, 1.0, 1.0, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.75, 0.1875, 0.1859375, 1.0, 0.75), BooleanOp.OR);
      return Shapes.joinUnoptimized(shape, Shapes.box(0.8125, 0.75, 0.1875, 1.0, 1.0, 0.75), BooleanOp.OR);
   };
   public static final Map<Direction, VoxelShape> SHAPE = (Map<Direction, VoxelShape>)Util.make(new HashMap(), map -> {
      for (Direction direction : Plane.HORIZONTAL.stream().toList()) {
         map.put(direction, ShapeUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
      }
   });
   public static final Map<Direction, VoxelShape> SHAPE_WITH_FAUCET = (Map<Direction, VoxelShape>)Util.make(new HashMap(), map -> {
      for (Direction direction : Plane.HORIZONTAL.stream().toList()) {
         map.put(direction, ShapeUtil.rotateShape(Direction.NORTH, direction, voxelShapeWithFaucetSupplier.get()));
      }
   });

   public LegacyKitchenSinkBlock(Properties settings) {
      super(settings);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(HAS_FAUCET, true)).setValue(FILLED, false)
      );
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (world.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack itemStack = player.getItemInHand(hand);
         Item item = itemStack.getItem();
         if (itemStack.isEmpty() && (Boolean)state.getValue(HAS_FAUCET) && !(Boolean)state.getValue(FILLED)) {
            world.setBlock(pos, (BlockState)((BlockState)state.setValue(HAS_FAUCET, true)).setValue(FILLED, true), 3);
            world.playSound(
               null,
               (double)pos.getX(),
               (double)pos.getY(),
               (double)pos.getZ(),
               (SoundEvent)SoundEventRegistry.KITCHEN_SINK_FAUCET.get(),
               SoundSource.BLOCKS,
               1.0F,
               1.0F
            );
            world.playSound(null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
         } else if ((item == Items.WATER_BUCKET || item == Items.GLASS_BOTTLE) && !(Boolean)state.getValue(FILLED)) {
            world.setBlock(pos, (BlockState)((BlockState)state.setValue(HAS_FAUCET, (Boolean)state.getValue(HAS_FAUCET))).setValue(FILLED, true), 3);
            world.playSound(null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.isCreative()) {
               if (item == Items.WATER_BUCKET) {
                  itemStack.shrink(1);
                  player.addItem(new ItemStack(Items.BUCKET));
               } else {
                  itemStack.shrink(1);
                  player.addItem(new ItemStack(Items.GLASS_BOTTLE));
               }
            }

            return InteractionResult.SUCCESS;
         } else if ((item == Items.BUCKET || item == Items.GLASS_BOTTLE) && (Boolean)state.getValue(FILLED)) {
            world.setBlock(pos, (BlockState)state.setValue(FILLED, false), 3);
            world.playSound(null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.isCreative()) {
               if (item == Items.BUCKET) {
                  itemStack.shrink(1);
                  player.addItem(new ItemStack(Items.WATER_BUCKET));
               } else {
                  itemStack.shrink(1);
                  player.addItem(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
               }
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return (BlockState)this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return state.getValue(HAS_FAUCET) ? SHAPE_WITH_FAUCET.get(state.getValue(FACING)) : SHAPE.get(state.getValue(FACING));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, FILLED, HAS_FAUCET});
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.deprecated.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
   }
}
