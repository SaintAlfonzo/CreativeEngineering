package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.kubejs.JarStartupScripts;
import dev.latvian.mods.kubejs.script.ScriptManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Startup scripts from mod jars (creative_delight.md 10.5 item 1). KubeJS 1902.6.2 reads server and client scripts
 * from every mod's resources, but startup scripts only from {@code kubejs/startup_scripts/}: {@code KubeJS} calls
 * {@code reload(null)}, which skips {@code loadFromResources}. After the folder pack is built, this adds one pack per
 * mod jar that has a {@code kubejs_startup/} folder; see {@link JarStartupScripts} for the order.
 *
 * Runs again on {@code /kubejs reload startup_scripts} (same method). require = 1: if a KubeJS update renames the
 * method, boot fails here instead of the helpers silently going missing.
 */
@Mixin(value = ScriptManager.class, remap = false)
public class KubeJSStartupJarScriptsMixin {

	@Inject(method = "loadFromDirectory", at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$addJarStartupScripts(CallbackInfo ci) {
		JarStartupScripts.addPacks((ScriptManager) (Object) this);
	}
}
