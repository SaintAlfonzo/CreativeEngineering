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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import net.ribs.vintagedelight.block.ModBlocks;
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
      // Tell the block below a layer arrived, also for flag-2 placements (salt dust, /setblock) that send no block
      // update: a worn Hearth and Harvest salt lick takes the layers back in (plan_bugfix_1114 3.7). Any other block
      // below sees an ordinary neighbour update.
      if (!world.isClientSide && state != oldState) {
         world.neighborChanged(pos.below(), this, pos);
         settle(world, pos, state);
      }
   }

   // Full block (Creative Compat SurfaceLayers): 8 layers hold 8 salt dust, the Salt Block 9.
   // A layer resting on a full pile (salt dust clicked on top, a dispenser, a falling layer) turns the pile into the
   // Salt Block and gives up one layer; a full pile placed under a layer does the same.
   private static void settle(Level world, BlockPos pos, BlockState state) {
      BlockState below = world.getBlockState(pos.below());
      if (below.is(state.getBlock()) && below.getValue(LAYERS) == 8) {
         world.setBlock(pos.below(), fullBlock(), 3);
         int n = state.getValue(LAYERS) - 1;
         world.setBlock(pos, n > 0 ? state.setValue(LAYERS, n) : Blocks.AIR.defaultBlockState(), 3);
      } else if (state.getValue(LAYERS) == 8) {
         BlockState above = world.getBlockState(pos.above());
         if (above.is(state.getBlock())) settle(world, pos.above(), above);
      }
   }

   /** The Salt Block: The Salt's (Vintage Delight's merges into it), else Vintage Delight's own. */
   public static BlockState fullBlock() {
      Block salt = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("salt", "salt_block"));
      return (salt == null || salt == Blocks.AIR ? ModBlocks.SALT_BLOCK.get() : salt).defaultBlockState();
   }

   // Never replaced by another block; salt dust adds layers through its own click.
   @Override
   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return false;
   }

   // Place from inside (food/seasoning #7): a layer grown under an entity that already stands in this block would
   // shove it up. While the entity's box overlaps the block and its feet are below the layer's top, the collision is
   // cut at its feet, so it stays where it is. Anything outside the block (walking in from the side, landing from
   // above) gets the full shape, so thick salt still steps up and holds weight.
   // Height (Creative Compat SurfaceLayers): the full shape is the visible layer, like every
   // other surface layer (sand, ash, sawdust), not snow's one layer lower; the sink is visual (dust tier).
   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      VoxelShape full = state.getShape(level, pos, context);
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
