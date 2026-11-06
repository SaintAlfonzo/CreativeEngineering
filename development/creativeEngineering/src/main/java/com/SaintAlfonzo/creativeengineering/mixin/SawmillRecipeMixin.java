package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillRecipeExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Carries the optional {@code "blade"} profile string (parsed by
 * {@link SawmillRecipeSerializerMixin}) on each {@link SawmillRecipe}, so
 * {@link com.SaintAlfonzo.creativeengineering.sawmill.SawbladeRouting} can prefer the
 * recipe matching the installed precision sawblade's {@code {Profile}} NBT.
 *
 * <p>Not synced over the network — client render parity is handled by persisting the
 * resolved recipe id on the {@code SawmillProcess} instead (see {@link SawmillProcessMixin}).
 */
@Mixin(value = SawmillRecipe.class, remap = false)
public class SawmillRecipeMixin implements SawmillRecipeExt {

    @Unique
    private String creativeengineering$blade;

    @Override
    public String creativeengineering$getBlade() {
        return creativeengineering$blade == null ? "" : creativeengineering$blade;
    }

    @Override
    public void creativeengineering$setBlade(String blade) {
        this.creativeengineering$blade = blade == null ? "" : blade;
    }
}
