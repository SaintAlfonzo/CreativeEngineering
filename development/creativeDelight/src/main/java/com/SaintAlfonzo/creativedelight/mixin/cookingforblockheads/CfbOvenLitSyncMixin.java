package com.SaintAlfonzo.creativedelight.mixin.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.tile.OvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors the Cooking for Blockheads oven's burn time into the {@code lit} state {@link CfbOvenLitMixin} adds: lit
 * while fuel burns, unlit once it runs out. The state is set only when it changes. CfB's {@code onRemove} acts only when
 * the block changes, so toggling {@code lit} keeps the oven's contents.
 */
@Mixin(value = OvenBlockEntity.class, remap = false)
public abstract class CfbOvenLitSyncMixin {

    @Inject(method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("RETURN"))
    private void ce$syncLit(Level level, BlockPos pos, BlockState ignored, CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlockStateProperties.LIT)) {
            return;
        }
        boolean burning = ((OvenBlockEntity) (Object) this).isBurning();
        if (state.getValue(BlockStateProperties.LIT) != burning) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, burning), Block.UPDATE_ALL);
        }
    }
}
