package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tk.meowmc.portalgun.client.renderer.CustomPortalEntityRenderer;
import tk.meowmc.portalgun.entities.CustomPortal;

/**
 * Opaque gun portals ({@code [portalgun] opaquePortals}; feature_writeup/plan_portal_changes.md 3, G5). Portal Gun 5.0.0
 * draws its filled disc, tinted in the portal colour, for a portal that is not linked yet; with the option on every gun
 * portal draws it, while portals/PortalLimits keeps Immersive Portals from rendering through.
 */
@Mixin(value = CustomPortalEntityRenderer.class, remap = false)
public abstract class PortalGunRendererOpaqueMixin {

    @Shadow(remap = false) @Final private static ResourceLocation OVERLAY_FILLED;

    @Inject(method = "getTextureLocation(Ltk/meowmc/portalgun/entities/CustomPortal;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativecompat$opaqueTexture(CustomPortal portal, CallbackInfoReturnable<ResourceLocation> cir) {
        if (CompatConfig.SPEC.isLoaded() && CompatConfig.PORTAL_GUN.opaquePortals.get()) cir.setReturnValue(OVERLAY_FILLED);
    }
}
