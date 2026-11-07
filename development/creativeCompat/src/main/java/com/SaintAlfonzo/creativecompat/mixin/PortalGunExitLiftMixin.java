package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.portalgun.PortalGunExitLift;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;
import tk.meowmc.portalgun.entities.CustomPortal;

/**
 * Portal Gun 5.0.0 portals lift the player only on floor exits (portalgun/PortalGunExitLift; plan_portal_gun.md 3).
 * Recomputed before each {@code reloadAndSyncToClient()} in {@code updateState} (the one in the branch that sets the
 * destination is what matters) and at the tail of {@code readAdditionalSaveData}, so portals saved before the fix and
 * the client copy agree.
 */
@Mixin(value = CustomPortal.class, remap = false)
public abstract class PortalGunExitLiftMixin {

    @Inject(method = "updateState", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Ltk/meowmc/portalgun/entities/CustomPortal;reloadAndSyncToClient()V"))
    private void creativecompat$exitLiftBeforeSync(CallbackInfo ci) {
        PortalGunExitLift.update((Portal) (Object) this);
    }

    @Inject(method = "m_7378_", remap = false, require = 1, at = @At("TAIL"))
    private void creativecompat$exitLiftOnLoad(CompoundTag tag, CallbackInfo ci) {
        PortalGunExitLift.update((Portal) (Object) this);
    }
}
