package net.mehdinoui.fungidelight.common.world.feature;

import com.mojang.serialization.Codec;
import net.mehdinoui.fungidelight.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class HugeInkyCapFeature extends CustomHugeMushroomFeature {
   protected static int[][][] capLayers = new int[][][]{
      {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}, {2, 0}, {2, 1}, {2, 2}},
      {{0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 0}, {1, 4}, {2, 0}, {2, 4}, {3, 0}, {3, 4}, {4, 0}, {4, 1}, {4, 2}, {4, 3}, {4, 4}},
      {
            {0, 1},
            {0, 2},
            {0, 3},
            {0, 4},
            {0, 5},
            {1, 0},
            {1, 6},
            {2, 0},
            {2, 6},
            {3, 0},
            {3, 6},
            {4, 0},
            {4, 6},
            {5, 0},
            {5, 6},
            {6, 1},
            {6, 2},
            {6, 3},
            {6, 4},
            {6, 5}
      }
   };

   public HugeInkyCapFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      if (!this.isValidSpace(world, pos, 6, 4, 3)) {
         return false;
      } else {
         int stemHeight = 5 + context.random().nextInt(3);
         this.placeStem(world, pos, stemHeight, stemHeight, ((Block)ModBlocks.INKY_CAP_MUSHROOM_STEM.get()).defaultBlockState());
         BlockPos layerOrigin = pos.above(stemHeight);
         this.placeCapLayer(world, layerOrigin.below(0), capLayers[0], ((Block)ModBlocks.INKY_CAP_MUSHROOM_BLOCK.get()).defaultBlockState(), false);
         this.placeCapLayer(world, layerOrigin.below(1), capLayers[1], ((Block)ModBlocks.INKY_CAP_MUSHROOM_BLOCK.get()).defaultBlockState(), false);
         this.placeCapLayer(world, layerOrigin.below(2), capLayers[1], ((Block)ModBlocks.INKY_CAP_MUSHROOM_EDGE.get()).defaultBlockState(), false);
         this.placeCapLayer(world, layerOrigin.below(3), capLayers[2], ((Block)ModBlocks.INKY_CAP_MUSHROOM_EDGE.get()).defaultBlockState(), false);
         this.placeCapLayer(world, layerOrigin.below(4), capLayers[2], ((Block)ModBlocks.INKY_GOO_VEIL.get()).defaultBlockState(), true);
         return true;
      }
   }
}
