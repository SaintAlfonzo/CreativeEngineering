package com.SaintAlfonzo.creativeengineering.mixin;

import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * KubeJS's private {@code ScriptManager.loadFile}: preload (file headers: priority, requires, packmode), skip
 * check, then add the file to the pack. The startup loader ({@code compat/kubejs/JarStartupScripts}) calls it so
 * jar scripts follow the same header rules as the pack folder's.
 */
@Mixin(value = ScriptManager.class, remap = false)
public interface KubeJSScriptManagerInvoker {

	@Invoker(value = "loadFile", remap = false)
	void creativeengineering$loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);
}
