package net.ribs.vintagedelight.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class VineTrunkPlacer extends TrunkPlacer {
   public static final Codec<VineTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> trunkPlacerParts(instance).apply(instance, VineTrunkPlacer::new));

   public VineTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
      super(baseHeight, heightRandA, heightRandB);
   }

   protected TrunkPlacerType<?> type() {
      return (TrunkPlacerType<?>)ModTrunkPlacerTypes.VINE_TRUNK_PLACER.get();
   }

   public List<FoliageAttachment> placeTrunk(
      LevelSimulatedReader level,
      BiConsumer<BlockPos, BlockState> blockSetter,
      RandomSource random,
      int freeTreeHeight,
      BlockPos startPos,
      TreeConfiguration config
   ) {
      List<FoliageAttachment> foliageAttachments = new ArrayList<>();
      MutableBlockPos mutablePos = startPos.mutable();
      int height = this.getTreeHeight(random);
      for (int y = 0; y < height; y++) {
         int thickness = this.calculateThickness(y, height);
         int xOffset = this.calculateOffset(y, height, random);
         int zOffset = this.calculateOffset(y, height, random);
         if (thickness < 1) {
            mutablePos.set(startPos.getX() + xOffset, startPos.getY() + y, startPos.getZ() + zOffset);
            this.placeLog(level, blockSetter, random, mutablePos, config);
         } else {
            for (int x = -thickness; x <= thickness; x++) {
               for (int z = -thickness; z <= thickness; z++) {
                  if (x * x + z * z <= thickness * thickness) {
                     mutablePos.set(startPos.getX() + xOffset + x, startPos.getY() + y, startPos.getZ() + zOffset + z);
                     this.placeLog(level, blockSetter, random, mutablePos, config);
                  }
               }
            }
         }
      }

      // Port: no golden egg on top of the trunk (upstream 80 %); the goose lays the pack's golden eggs

      return foliageAttachments;
   }

   private int calculateThickness(int currentHeight, int maxHeight) {
      double stage1 = 0.4 * (double)maxHeight;
      double stage2 = 0.6 * (double)maxHeight;
      double stage3 = 0.8 * (double)maxHeight;
      if ((double)currentHeight < stage1) {
         return 2;
      } else if ((double)currentHeight < stage2) {
         return 1;
      } else {
         return (double)currentHeight < stage3 ? 1 : 0;
      }
   }

   private int calculateOffset(int currentHeight, int maxHeight, RandomSource random) {
      double windinessFactor = 0.3;
      int maxOffset = 4;
      double frequency = 3.0;
      return (int)(Math.sin((double)currentHeight / (double)maxHeight * Math.PI * frequency) * (double)maxOffset * windinessFactor);
   }
}
