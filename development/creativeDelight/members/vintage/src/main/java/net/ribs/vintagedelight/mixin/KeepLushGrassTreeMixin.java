package net.ribs.vintagedelight.mixin;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.ribs.vintagedelight.block.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TrunkPlacer.class})
public class KeepLushGrassTreeMixin {
   @Inject(
      at = {@At("HEAD")},
      method = {"setDirtAt"},
      cancellable = true
   )
   private static void cancelSetDirtIfRichSoil(
      LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, TreeConfiguration config, CallbackInfo ci
   ) {
      if (level.isStateAtPosition(pos, state -> state.is((Block)ModBlocks.LUSH_GRASS_BLOCK.get()))) {
         ci.cancel();
      }
   }
}
