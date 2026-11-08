package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import tk.meowmc.portalgun.entities.CustomPortal;

/** Portal Gun's closing sound, played when a portal dissolves (portalgun/PortalGunDissolve). */
@Mixin(value = CustomPortal.class, remap = false)
public interface PortalGunPortalInvoker {

    @Invoker(value = "playClosingSound", remap = false)
    void creativecompat$playClosingSound();
}
