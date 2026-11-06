package com.SaintAlfonzo.creativecommon.mixin;

import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips WorldEdit 7.2.12's block state id table build on {@code ServerStartedEvent}.
 *
 * <p>{@code Capability.WORLD_EDITING.ready} walks {@code getAllStates()} of every block type, which builds
 * WorldEdit's own copy of every state (770,959 in this pack, each with a property map and a neighbour table),
 * then maps each one back to the vanilla state to read the id vanilla already assigned
 * ({@code Block.getId}). 13 s per dedicated boot, 15 s per single player world open (perf.md 11.2 #1).
 *
 * <p>{@link WorldEditBlockStateIdAccessMixin} fills the same table on first use from vanilla's ids, so only
 * states WorldEdit actually touches get built.
 */
@Mixin(targets = "com.sk89q.worldedit.extension.platform.Capability$4", remap = false)
public abstract class WorldEditSkipStateIdInitMixin {

    @Inject(method = "ready", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativecommon$skipStateIdInit(PlatformManager platformManager, Platform platform, CallbackInfo ci) {
        ci.cancel();
    }
}
