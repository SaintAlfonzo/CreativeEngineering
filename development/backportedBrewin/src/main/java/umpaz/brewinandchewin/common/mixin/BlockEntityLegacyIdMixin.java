package umpaz.brewinandchewin.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Port-only (alcohol.md 3.3): a 2.0 item coaster's block entity ("brewinandchewin:item_coaster") loads as the coaster. */
@Mixin(BlockEntity.class)
public class BlockEntityLegacyIdMixin {
   @Inject(method = "loadStatic", at = @At("HEAD"))
   private static void brewinandchewin$renameLegacyCoaster(BlockPos pos, BlockState state, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir) {
      if ("brewinandchewin:item_coaster".equals(tag.getString("id"))) {
         tag.putString("id", "brewinandchewin:coaster");
      }
   }
}
