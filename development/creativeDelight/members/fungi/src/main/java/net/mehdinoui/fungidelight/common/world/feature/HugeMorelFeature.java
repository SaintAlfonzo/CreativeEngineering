package net.mehdinoui.fungidelight.common.world.feature;

import com.mojang.serialization.Codec;
import net.mehdinoui.fungidelight.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class HugeMorelFeature extends CustomHugeMushroomFeature {
   protected static int[][][] capLayers = new int[][][]{
      {{0, 0}},
      {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 2}, {2, 0}, {2, 1}, {2, 2}},
      {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 2}, {2, 0}, {2, 1}, {2, 2}},
      {{0, 1}, {0, 2}, {0, 3}, {1, 0}, {1, 4}, {2, 0}, {2, 4}, {3, 0}, {3, 4}, {4, 1}, {4, 2}, {4, 3}},
      {{0, 1}, {0, 2}, {0, 3}, {1, 0}, {1, 4}, {2, 0}, {2, 4}, {3, 0}, {3, 4}, {4, 1}, {4, 2}, {4, 3}},
      {{0, 2}, {0, 3}, {0, 4}, {1, 1}, {1, 5}, {2, 0}, {2, 6}, {3, 0}, {3, 6}, {4, 0}, {4, 6}, {5, 1}, {5, 5}, {6, 2}, {6, 3}, {6, 4}},
      {{0, 1}, {0, 2}, {0, 3}, {1, 0}, {1, 4}, {2, 0}, {2, 4}, {3, 0}, {3, 4}, {4, 1}, {4, 2}, {4, 3}}
   };

   public HugeMorelFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      if (!this.isValidSpace(world, pos, 6, 6, 2)) {
         return false;
      } else {
         int stemHeight = 9 + context.random().nextInt(4);
         this.placeStem(world, pos, stemHeight, stemHeight, ((Block)ModBlocks.MOREL_MUSHROOM_STEM.get()).defaultBlockState());
         BlockPos layerOrigin = pos.above(stemHeight);

         for (int i = 0; i < capLayers.length; i++) {
            this.placeCapLayer(world, layerOrigin.below(i), capLayers[i], ((Block)ModBlocks.MOREL_MUSHROOM_BLOCK.get()).defaultBlockState(), false);
         }

         return true;
      }
   }
}
