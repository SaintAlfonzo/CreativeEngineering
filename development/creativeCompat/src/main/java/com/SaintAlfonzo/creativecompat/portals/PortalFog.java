package com.SaintAlfonzo.creativecompat.portals;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.mixin.IPPortalRenderingAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalLike;
import qouteall.imm_ptl.core.render.PortalGroup;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;
import qouteall.imm_ptl.core.render.context_management.RenderStates;

import java.util.List;

/**
 * Fog on the far side of a portal (client; feature_writeup/plan_portal_changes.md 2, N3 and D7). Called from
 * mixin/IPPortalFogMixin after vanilla sets up terrain fog. While Immersive Portals draws through a portal, the
 * outermost portal of the chain picks the settings by type ({@code [immersive_portals.fog]} nether / portalGun /
 * mirror): clear up to {@code start} blocks past the portal plane, full fog at {@code end}. Fog is measured from the
 * camera, so both are offset by the camera's distance to that portal; for an unscaled portal the drawing camera is
 * that far from the far-side plane too. Never pushes fog farther out than vanilla set it (water, blindness, the
 * render distance stay). The fog colour is the drawn dimension's own (IP swaps it per dimension).
 */
public final class PortalFog {

    private PortalFog() {}

    public static void apply() {
        if (!CompatConfig.SPEC.isLoaded() || !PortalRendering.isRendering()) return;
        List<PortalLike> layers = IPPortalRenderingAccessor.creativecompat$getPortalLayers();
        if (layers.isEmpty()) return;
        PortalLike outer = layers.get(0);

        CompatConfig.ImmersivePortals c = CompatConfig.IMMERSIVE_PORTALS;
        CompatConfig.ImmersivePortals.Fog fog = switch (PortalKind.of(outer)) {
            case NETHER -> c.nether;
            case GUN -> c.portalGun;
            case MIRROR -> c.mirror;
            default -> null;
        };
        if (fog == null || !fog.enabled.get()) return;

        Portal portal = outer instanceof PortalGroup group ? group.getFirstPortal() : outer instanceof Portal p ? p : null;
        if (portal == null || RenderStates.originalCamera == null) return;
        Vec3 camera = RenderStates.originalCamera.getPosition();
        float offset = (float) portal.getDistanceToNearestPointInPortal(camera);

        float start = offset + fog.start.get();
        float end = offset + Math.max(fog.end.get(), fog.start.get() + 1);
        float vanillaEnd = RenderSystem.getShaderFogEnd();
        if (end >= vanillaEnd) return;
        RenderSystem.setShaderFogStart(Math.min(start, RenderSystem.getShaderFogStart()));
        RenderSystem.setShaderFogEnd(end);
    }
}
