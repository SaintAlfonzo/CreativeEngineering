package com.SaintAlfonzo.creativecompat.portalgun;

import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalExtension;

/**
 * The post-teleport lift for Portal Gun portals (feature_writeup/plan_portal_gun.md 3, decision D3). After a client
 * teleport Immersive Portals lifts the player onto the top of every block collision in the bottom half of their box
 * ({@code ClientTeleportationManager.adjustPlayerPosition}) when the portal's {@code adjustPositionAfterTeleport} is on.
 * Walking out of a wall portal the box still overlaps the wall behind the exit, so the player was lifted a block. The
 * lift now stays on only when the player leaves the exit portal upward (a floor exit, where it clears the floor block).
 * Set by mixin/PortalGunExitLiftMixin on the server before each sync and on every NBT read; the flag is saved and
 * synced in the portal's NBT, so the client reads the server's value.
 */
public final class PortalGunExitLift {

    private PortalGunExitLift() {}

    public static void update(Portal portal) {
        boolean floorExit = portal.transformLocalVecNonScale(portal.getContentDirection()).y > 0.5;
        PortalExtension.get(portal).adjustPositionAfterTeleport = floorExit;
    }
}
