package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.portals.PortalLimits;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalLike;
import qouteall.imm_ptl.core.render.PortalRenderer;

import java.util.function.Supplier;

/**
 * Mirror limits and opaque gun portals (portals/PortalLimits; feature_writeup/plan_portal_changes.md 3, 4).
 * {@code shouldSkipRenderingPortal} is IP 2.3.7's one filter in {@code getPortalsToRender}; {@code getPortalRenderDistance}
 * sets the chunk radius drawn through a portal.
 */
@Mixin(value = PortalRenderer.class, remap = false)
public abstract class IPPortalRenderLimitsMixin {

    @Inject(method = "shouldSkipRenderingPortal", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void creativecompat$skipPortal(Portal portal, Supplier<Frustum> frustum,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (PortalLimits.skip(portal)) cir.setReturnValue(true);
    }

    @Inject(method = "getPortalRenderDistance", at = @At("RETURN"), cancellable = true, remap = false, require = 1)
    private static void creativecompat$mirrorViewDistance(PortalLike portal, CallbackInfoReturnable<Integer> cir) {
        int capped = PortalLimits.renderDistance(portal, cir.getReturnValueI());
        if (capped != cir.getReturnValueI()) cir.setReturnValue(capped);
    }
}
