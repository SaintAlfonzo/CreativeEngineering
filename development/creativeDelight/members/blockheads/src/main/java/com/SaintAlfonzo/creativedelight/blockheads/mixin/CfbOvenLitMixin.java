package com.SaintAlfonzo.creativedelight.blockheads.mixin;

import net.blay09.mods.cookingforblockheads.block.OvenBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cooking for Blockheads oven (13.3.4) gets a {@code lit} block state, so it can be a Farmer's Delight heat source
 * ({@code #farmersdelight:heat_sources}): FD's heat check takes a tagged block's {@code lit} value when it has one, so a
 * pot or skillet on the oven cooks only while the oven burns fuel. CfB's own {@code active} state is never set, and its
 * burn time lives only in the block entity; {@link CfbOvenLitSyncMixin} mirrors it into {@code lit}. The blockstate file
 * has one variant for every state, so the extra property changes no model. Saved ovens load with {@code lit=false}.
 */
@Mixin(value = OvenBlock.class, remap = false)
public abstract class CfbOvenLitMixin {

    @Inject(method = "m_7926_", at = @At("TAIL"))
    private void ce$addLit(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(BlockStateProperties.LIT);
    }

    /** A boolean property's first value is true, so the default state would be lit without this. */
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/blay09/mods/cookingforblockheads/block/OvenBlock;m_49959_(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private BlockState ce$unlitDefault(BlockState state) {
        return state.hasProperty(BlockStateProperties.LIT) ? state.setValue(BlockStateProperties.LIT, false) : state;
    }
}
