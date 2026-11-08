package com.SaintAlfonzo.creativecompat.portals;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.mixin.IPPortalRenderingAccessor;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.CHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalLike;

import java.util.List;

/**
 * Which portals Immersive Portals draws through (client; feature_writeup/plan_portal_changes.md 3 and 4, M3 and G5).
 * Called from mixin/IPPortalRenderLimitsMixin at the head of {@code PortalRenderer.shouldSkipRenderingPortal}, IP's one
 * filter for portals to render, and on the return of {@code getPortalRenderDistance}. Teleporting and block
 * interaction through a portal do not pass here.
 *
 * <ul>
 *   <li>Mirrors farther than {@code mirrorRenderDistance} from the camera are skipped (the glass behind shows).</li>
 *   <li>A mirror is skipped once {@code mirrorMaxBounces} mirrors are already being drawn through, when every
 *   portal in that chain is a mirror. IP's {@code maxPortalLayer} still caps everything.</li>
 *   <li>Gun portals are skipped when {@code opaquePortals} is on (the gun's renderer then draws its filled disc,
 *   mixin/PortalGunRendererOpaqueMixin).</li>
 *   <li>Inside a mirror the world is drawn to {@code mirrorViewDistance}, through a gun portal to
 *   {@code portalGunViewDistance}, through a Nether portal to {@code netherViewDistance}, in whole chunks.</li>
 * </ul>
 */
public final class PortalLimits {

    private PortalLimits() {}

    public static boolean skip(Portal portal) {
        if (!CompatConfig.SPEC.isLoaded()) return false;
        PortalKind kind = PortalKind.of(portal);
        if (kind == PortalKind.GUN) return CompatConfig.PORTAL_GUN.opaquePortals.get();
        if (kind != PortalKind.MIRROR) return false;

        CompatConfig.ImmersivePortals c = CompatConfig.IMMERSIVE_PORTALS;
        int range = c.mirrorRenderDistance.get();
        if (range > 0) {
            Vec3 camera = CHelper.getCurrentCameraPos();
            if (portal.getDistanceToNearestPointInPortal(camera) > range) return true;
        }
        List<PortalLike> layers = IPPortalRenderingAccessor.creativecompat$getPortalLayers();
        if (layers.size() >= c.mirrorMaxBounces.get()) {
            for (PortalLike layer : layers) {
                if (PortalKind.of(layer) != PortalKind.MIRROR) return false;
            }
            return true;
        }
        return false;
    }

    /** IP's render distance in chunks for the world seen through {@code portal}, capped per portal type. */
    public static int renderDistance(PortalLike portal, int chunks) {
        if (!CompatConfig.SPEC.isLoaded()) return chunks;
        int blocks = switch (PortalKind.of(portal)) {
            case MIRROR -> CompatConfig.IMMERSIVE_PORTALS.mirrorViewDistance.get();
            case GUN -> CompatConfig.IMMERSIVE_PORTALS.portalGunViewDistance.get();
            case NETHER -> CompatConfig.IMMERSIVE_PORTALS.netherViewDistance.get();
            default -> 0;
        };
        if (blocks <= 0) return chunks;
        return Math.min(chunks, Math.max(1, (blocks + 15) / 16));
    }
}
