package net.ribs.vintagedelight.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ribs.vintagedelight.item.ModItems;

public class SaltLayerBlock extends SnowLayerBlock {
   public SaltLayerBlock(Properties properties) {
      super(properties);
   }

   public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
      return new ItemStack((ItemLike)ModItems.SALT_DUST.get());
   }

   public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
      super.onPlace(state, world, pos, oldState, isMoving);
      if (!state.is(oldState.getBlock())) {
         world.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   // Place from inside (food/seasoning #7): a layer grown under an entity that already stands in this block would
   // shove it up. While the entity's box overlaps the block and its feet are below the layer's top, the collision is
   // cut at its feet, so it stays where it is. Anything outside the block (walking in from the side, landing from
   // above) gets the full snow-like shape, so thick salt still steps up and holds weight.
   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      VoxelShape full = super.getCollisionShape(state, level, pos, context);
      if (full.isEmpty() || !(context instanceof EntityCollisionContext entityContext)) {
         return full;
      }
      Entity entity = entityContext.getEntity();
      if (entity == null) {
         return full;
      }
      double top = pos.getY() + full.max(Direction.Axis.Y);
      double feet = entity.getY();
      if (feet >= top - 1.0E-5 || feet < pos.getY() - 1.0E-5) {
         return full;
      }
      AABB box = entity.getBoundingBox();
      if (box.maxX <= pos.getX() || box.minX >= pos.getX() + 1 || box.maxZ <= pos.getZ() || box.minZ >= pos.getZ() + 1) {
         return full;
      }
      double height = feet - pos.getY();
      return height <= 1.0E-5 ? Shapes.empty() : Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, height, 1.0D);
   }
}
