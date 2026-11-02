package net.mehdinoui.fungidelight.common.block.mushrooms;

import net.mehdinoui.fungidelight.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.tag.ModTags;

// 1.19.2: MushroomBlock takes a Supplier<Holder<ConfiguredFeature>>; the huge form is the data-driven
// configured feature fungidelight:huge_inky_cap_mushroom, looked up in the server registry when bone meal or a random tick grows it.
public class InkyCapMushroomBlock extends MushroomBlock {
   protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);

   public InkyCapMushroomBlock(Properties pProperties) {
      super(pProperties, HugeMushroomFeatures.supplier("huge_inky_cap_mushroom"));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!level.isClientSide()) {
         if (level.getBlockState(pos.below()).is(ModTags.MUSHROOM_COLONY_GROWABLE_ON)) {
            if (random.nextInt(4) == 0) {
               level.setBlockAndUpdate(pos, ((Block)ModBlocks.INKY_CAP_MUSHROOM_COLONY.get()).defaultBlockState());
            }
         } else {
            super.randomTick(state, level, pos, random);
         }
      }
   }
}
