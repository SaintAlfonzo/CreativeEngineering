package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalExtension;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;
import qouteall.imm_ptl.core.teleportation.TeleportationUtil;

/**
 * The post-teleport lift, decided by geometry for every Immersive Portals portal (feature_writeup/plan_portal_gun.md 3,
 * D3; plan_bugfix_1114.md 4.1). After a client teleport IP lifts the player onto the top of every block collision in the
 * bottom half of their box ({@code adjustPlayerPosition}) when the portal's saved {@code adjustPositionAfterTeleport}
 * flag is on. Walking out of a wall exit the box still overlaps the wall behind it, so the player was lifted a block.
 * Now the lift runs only when the player leaves the exit upward: the content direction (the exit's outgoing direction
 * in destination space) points up, i.e. the exit portal lies on a floor. Applies to nether frames, Portal Gun portals,
 * End portals and IP's own. The saved per-portal flag (IP default on) is ignored, so the wrong values saved on Portal
 * Gun portals since 11-07 are harmless. Client only (the lift is a client-side position fix); applied only with
 * Immersive Portals (CompatMixinPlugin).
 */
@Mixin(value = ClientTeleportationManager.class, remap = false)
public abstract class IPExitLiftMixin {

    @Unique
    private Portal creativecompat$liftPortal;

    @Inject(method = "teleportPlayer", remap = false, require = 1, at = @At("HEAD"))
    private void creativecompat$rememberPortal(TeleportationUtil.Teleportation teleportation, CallbackInfo ci) {
        creativecompat$liftPortal = teleportation.portal();
    }

    @Redirect(method = "teleportPlayer", remap = false, require = 1,
            at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.GETFIELD, remap = false,
                    target = "Lqouteall/imm_ptl/core/portal/PortalExtension;adjustPositionAfterTeleport:Z"))
    private boolean creativecompat$liftOnFloorExitOnly(PortalExtension extension) {
        Portal portal = creativecompat$liftPortal;
        creativecompat$liftPortal = null;
        return portal != null && portal.getContentDirection().y > 0.5;
    }
}
