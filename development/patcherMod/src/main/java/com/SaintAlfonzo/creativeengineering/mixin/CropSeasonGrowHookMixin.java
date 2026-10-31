package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Plants whose growth never fires Forge's crop-grow event, so crop seasons ({@code seasons/CropSeasons}) could not
 * reach them (crops.md 2.12 F-SS2): IE hemp, The Dirty Stuff's tobacco and Nether's Delight's fungus colonies all
 * grow in {@code tick} (m_213897_), which their random tick calls. Fire the event first; a refused check skips the
 * tick (these ticks only grow the plant). Pseudo: each mod may be removed.
 */
@Pseudo
@Mixin(targets = {
        "blusunrize.immersiveengineering.common.blocks.plant.HempBlock",
        "net.mcreator.thedirtystuff.block.TobaccoSeedsBlock",
        "umpaz.nethersdelight.common.block.FungusColonyBlock"
}, remap = false)
public abstract class CropSeasonGrowHookMixin {

    // m_213897_ = Block.tick(BlockState, ServerLevel, BlockPos, RandomSource)
    @Inject(method = "m_213897_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void creativeengineering$cropGrowEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                                   CallbackInfo ci) {
        if (!ForgeHooks.onCropsGrowPre(level, pos, state, true)) ci.cancel();
    }
}
