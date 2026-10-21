package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Drops tutorial and advancement toasts per CEConfig {@code [toasts]} before they are queued, so
 * they never take a slot or play a sound. Every toast in the pack goes through
 * {@code ToastComponent.addToast}, vanilla's and the mods' alike.
 *
 * <p>Mod toasts are matched by class name so no mod jar is needed to compile and a missing mod is
 * simply no match. The caller keeps its toast object and may still call {@code hide()} on it,
 * which is harmless on a toast that was never added.
 *
 * <p>Vanilla-class target, matched by SRG name ({@code m_94922_} = {@code addToast(Toast)}) with
 * {@code remap = false} - same idiom as {@link SyncGlDebugOutputMixin}.
 */
@Mixin(value = ToastComponent.class, remap = false)
public class ToastFilterMixin {

    private static final Set<String> MOD_TUTORIAL_TOASTS = Set.of(
            "io.github.mortuusars.exposure.client.gui.toast.BetterTutorialToast",
            "me.paulf.fairylights.client.tutorial.ClippyController$Balloon"
    );
    private static final Set<String> MOD_ADVANCEMENT_TOASTS = Set.of(
            "vazkii.patchouli.client.base.ClientAdvancements$LexiconToast"
    );

    @Inject(method = "m_94922_", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$filterToast(Toast toast, CallbackInfo ci) {
        String name = toast.getClass().getName();
        if (!CEConfig.TOASTS.tutorial.get()
                && (toast instanceof TutorialToast || MOD_TUTORIAL_TOASTS.contains(name))) {
            ci.cancel();
        } else if (!CEConfig.TOASTS.advancement.get()
                && (toast instanceof AdvancementToast || MOD_ADVANCEMENT_TOASTS.contains(name))) {
            ci.cancel();
        }
    }
}
