package com.SaintAlfonzo.creativemagic.mixin;

import com.SaintAlfonzo.creativemagic.dimension.IPNetherPortalBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.peripheral.portal_generation.IntrinsicPortalGeneration;

/**
 * Ordinary fire no longer builds Immersive Portals Nether portals (feature_writeup/plan_portal_changes.md 1, N1).
 * IP redirects {@code PortalShape.findEmptyPortalShape} in {@code BaseFireBlock#onPlace}; in {@code normal} mode the
 * redirect always hands back an empty shape (after calling {@code onFireLitOnObsidian} next to obsidian), so Forge's
 * PortalSpawnEvent never fires from fire and PortalFailureHandler (crying obsidian, soul fire) never ran. The gate
 * returns false (no IP portal) and posts the event vanilla would have posted, at the moment the fire lands, so the
 * frame fails before Smaller Nether Portals' delayed flint-and-steel task looks at it (that task fills the frame with
 * portal blocks itself and ignores a cancelled event; found 2026-11-07 in latest.log). The crouch-ignite
 * path (off in IP's config) is closed the same way. Dark Fire passes through via IPNetherPortalBridge.
 * Only callers in IP 2.3.7: MixinAbstractFireBlock (fire) and MixinFlintAndSteelItem (crouch).
 */
@Mixin(value = IntrinsicPortalGeneration.class, remap = false)
public abstract class IPFireGateMixin {

    @Inject(method = "onFireLitOnObsidian", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void creativemagic$gateFire(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IPNetherPortalBridge.passing()) return;
        cir.setReturnValue(false);
        IPNetherPortalBridge.postVanillaSpawnEvent(level, pos);
    }

    @Inject(method = "onCrouchingPlayerIgnite", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void creativemagic$gateCrouch(ServerLevel level, ServerPlayer player, BlockPos pos,
                                                 CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
