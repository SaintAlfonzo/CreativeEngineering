package com.SaintAlfonzo.creativedelight.mixin.mca;

import forge.net.mca.entity.ai.brain.tasks.chore.AbstractChoreTask;
import forge.net.mca.entity.ai.brain.tasks.chore.HarvestingTask;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCA farmers (the harvesting chore) find farmland with a scan that only matches {@code minecraft:farmland}, so Rich
 * Soil farmland, Fertile farmland and every other modded {@link FarmBlock} were never planted (SQ-FDB-07; MCA replaces
 * the vanilla villagers, so the pack's vanilla sensor mixin never reaches them). In the Overworld the scan now takes any
 * FarmBlock; MCA's own second check ({@code isValidFarmland}: a FarmBlock with air above) is unchanged. Other
 * dimensions keep MCA's plain-farmland rule. The scan predicate is static, so the dimension is read when the scan
 * starts (one server thread runs the chore).
 */
@Mixin(value = HarvestingTask.class, remap = false)
public abstract class McaHarvestAnyFarmlandMixin extends AbstractChoreTask {

    private static boolean creativedelight$overworldScan;

    private McaHarvestAnyFarmlandMixin() {
        super(null);
    }

    @Inject(method = "searchUnusedFarmLand", at = @At("HEAD"), remap = false)
    private void creativedelight$scanStart(int rangeX, int rangeY, CallbackInfo ci) {
        creativedelight$overworldScan = villager != null && villager.level.dimension() == Level.OVERWORLD;
    }

    @Inject(method = "lambda$searchUnusedFarmLand$4", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativedelight$anyFarmland(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (creativedelight$overworldScan && state.getBlock() instanceof FarmBlock) cir.setReturnValue(true);
    }
}
