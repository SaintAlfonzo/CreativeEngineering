package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.portals.PortalFog;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fog on the far side of a portal (portals/PortalFog; feature_writeup/plan_portal_changes.md 2). After vanilla's
 * {@code FogRenderer.setupFog} (m_234172_) sets terrain fog; sky fog is left alone. Applied only with Immersive Portals.
 */
@Mixin(value = FogRenderer.class, remap = false)
public abstract class IPPortalFogMixin {

    @Inject(method = "m_234172_", at = @At("TAIL"), remap = false, require = 1)
    private static void creativecompat$portalFog(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                                 boolean thickFog, float partialTick, CallbackInfo ci) {
        if (mode == FogRenderer.FogMode.FOG_TERRAIN) PortalFog.apply();
    }
}
