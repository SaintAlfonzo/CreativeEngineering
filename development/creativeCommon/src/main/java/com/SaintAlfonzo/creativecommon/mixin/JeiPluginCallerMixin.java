package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.compat.jei.JeiIngredientChanges;
import mezz.jei.api.IModPlugin;
import mezz.jei.library.load.PluginCaller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

/**
 * Runs each JEI plugin call with that plugin's uid as {@link JeiIngredientChanges#currentPlugin()}, so a runtime
 * ingredient add/remove can be put down to the plugin that made it (the pack's {@code kubejs:jei} vs a mod's).
 */
@Mixin(value = PluginCaller.class, remap = false)
public abstract class JeiPluginCallerMixin {

    @Redirect(method = "callOnPlugins",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static void creativecommon$callAs(Consumer<IModPlugin> func, Object plugin) {
        JeiIngredientChanges.runAs(((IModPlugin) plugin).getPluginUid().toString(), () -> func.accept((IModPlugin) plugin));
    }
}
