package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.compat.StringEntry;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Create's Configs screen (reachable from the pause menu, used for every mod's ForgeConfigSpec
 * including {@code CommonConfig}) only has a widget for Boolean/Enum/Number values -
 * {@code SubMenuConfigScreen#init()}'s per-value dispatch falls through to an
 * "Impl missing - ..." placeholder row for anything else, a raw {@code ConfigValue<String>}
 * included. This adds the missing branch: a text-field row via {@link StringEntry}.
 *
 * <p>The dispatch lives in a private lambda body (the {@code configGroup.valueMap().forEach}
 * in {@code init()}), compiled as its own synthetic method - {@code lambda$init$12} in
 * create-1.19.2-0.5.1.i.jar. This injects right before that method's fallback
 * {@code ConfigScreenList.LabeledEntry} construction and, for a String value, builds a
 * {@link StringEntry} and cancels the rest of the lambda instead. Captured locals
 * ({@code humanKey}/{@code configValue}/{@code valueSpec}/{@code value}) are exactly the ones
 * live at that point per the decompiled bytecode - if a future Create update reorders or
 * renames this lambda, the mixin fails to apply (loudly, at launch) rather than silently
 * doing nothing.
 */
@Mixin(value = SubMenuConfigScreen.class, remap = false)
public class SubMenuConfigScreenMixin {

    @Shadow
    protected ConfigScreenList list;

    @Inject(
            method = "lambda$init$12",
            at = @At(
                    value = "NEW",
                    target = "Lcom/simibubi/create/foundation/config/ui/ConfigScreenList$LabeledEntry;"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void creativecommon$stringEntry(
            String key, Object obj, CallbackInfo ci,
            String humanKey, ForgeConfigSpec.ConfigValue<?> configValue, ForgeConfigSpec.ValueSpec valueSpec, Object value) {

        if (!(value instanceof String)) {
            return;
        }

        @SuppressWarnings("unchecked")
        ForgeConfigSpec.ConfigValue<String> stringValue = (ForgeConfigSpec.ConfigValue<String>) configValue;
        this.list.children().add(new StringEntry(humanKey, stringValue, valueSpec));
        ci.cancel();
    }
}
