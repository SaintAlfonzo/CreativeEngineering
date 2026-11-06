package net.ribs.vintagedelight.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.ribs.vintagedelight.block.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Feature.class})
public class KeepLushGrassGiantTreeMixin {
   @Inject(
      at = {@At("HEAD")},
      method = {"isGrassOrDirt"},
      cancellable = true
   )
   private static void keepLushGrass(LevelSimulatedReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if (world.isStateAtPosition(pos, state -> state.is((Block)ModBlocks.LUSH_GRASS_BLOCK.get()))) {
         cir.setReturnValue(false);
         cir.cancel();
      }
   }
}
