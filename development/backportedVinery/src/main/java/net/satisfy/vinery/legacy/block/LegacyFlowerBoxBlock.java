package net.satisfy.vinery.legacy.block;

import net.satisfy.foundation.storage.StorageBlock;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.vinery.legacy.LegacyStorageTypes;
import net.satisfy.foundation.util.ShapeUtil;
import net.satisfy.foundation.util.LibUtil;
import net.satisfy.vinery.core.registry.TagRegistry;

public class LegacyFlowerBoxBlock extends StorageBlock {
   private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
      VoxelShape shape = Shapes.empty();
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.9375, 0.0, 0.5625, 1.0, 0.375, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0, 0.0, 0.5625, 0.0625, 0.375, 1.0), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0625, 0.0, 0.5625, 0.9375, 0.375, 0.625), BooleanOp.OR);
      shape = Shapes.joinUnoptimized(shape, Shapes.box(0.0625, 0.0, 0.9375, 0.9375, 0.375, 1.0), BooleanOp.OR);
      return Shapes.joinUnoptimized(shape, Shapes.box(0.0625, 0.0, 0.625, 0.9375, 0.3125, 0.9375), BooleanOp.OR);
   };
   public static final Map<Direction, VoxelShape> SHAPE = (Map<Direction, VoxelShape>)Util.make(new HashMap(), map -> {
      for (Direction direction : Plane.HORIZONTAL.stream().toList()) {
         map.put(direction, ShapeUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
      }
   });

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPE.get(state.getValue(FACING));
   }

   public LegacyFlowerBoxBlock(Properties settings) {
      super(settings);
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      return player.isShiftKeyDown() ? InteractionResult.PASS : super.use(state, world, pos, player, hand, hit);
   }

   public int size() {
      return 2;
   }

   public ResourceLocation type() {
      return LegacyStorageTypes.FLOWER_BOX;
   }

   public Direction[] unAllowedDirections() {
      return new Direction[]{Direction.DOWN};
   }

   public boolean canInsertStack(ItemStack stack) {
      return stack.is(net.minecraft.tags.ItemTags.create(net.satisfy.vinery.core.Vinery.identifier("small_flower")));
   }

   public int getSection(Float x, Float y) {
      return (double)x.floatValue() < 0.5 ? 0 : 1;
   }

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.canbeplaced.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
   }

   /** Port: 1.3.10 used doapi:storage; the shared storage block entity is vinery:storage (ids rewritten on load, W2). */
   @Override
   public net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType() {
      return net.satisfy.vinery.core.registry.EntityTypeRegistry.STORAGE_ENTITY.get();
   }
}
