package net.satisfy.vinery.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * W2 (alcohol.md B3.3): 1.3.10's racks, shelves, flower boxes, wine boxes and placed wine bottles used doapi's
 * storage block entity. Same NBT (Items + size); only the id moves to the port's. Block entity ids aren't in Forge's
 * registry snapshot, so a missing-mappings remap can't reach them.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityLegacyIdMixin {
    @Inject(method = "loadStatic", at = @At("HEAD"))
    private static void vinery$rewriteLegacyId(BlockPos pos, BlockState state, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir) {
        if ("doapi:storage".equals(tag.getString("id"))) {
            tag.putString("id", "vinery:storage");
        }
    }
}
