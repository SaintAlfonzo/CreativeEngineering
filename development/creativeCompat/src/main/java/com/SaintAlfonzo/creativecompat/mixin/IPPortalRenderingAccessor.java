package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import qouteall.imm_ptl.core.portal.PortalLike;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

import java.util.Stack;

/** The stack of portals Immersive Portals is drawing through, outermost first (portals/PortalFog, PortalLimits). */
@Mixin(value = PortalRendering.class, remap = false)
public interface IPPortalRenderingAccessor {

    @Accessor(value = "portalLayers", remap = false)
    static Stack<PortalLike> creativecompat$getPortalLayers() {
        throw new AssertionError();
    }
}
