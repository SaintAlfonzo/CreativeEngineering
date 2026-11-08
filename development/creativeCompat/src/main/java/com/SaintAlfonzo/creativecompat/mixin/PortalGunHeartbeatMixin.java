package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.portalgun.PortalGunDissolve;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tk.meowmc.portalgun.entities.CustomPortal;

/** A gun portal's despawn heartbeat (portalgun/PortalGunDissolve; feature_writeup/plan_portal_changes.md 3.1). */
@Mixin(value = CustomPortal.class, remap = false)
public abstract class PortalGunHeartbeatMixin {

    @Inject(method = "m_8119_", at = @At("TAIL"), remap = false, require = 1)
    private void creativecompat$heartbeat(CallbackInfo ci) {
        PortalGunDissolve.heartbeat((CustomPortal) (Object) this);
    }
}
