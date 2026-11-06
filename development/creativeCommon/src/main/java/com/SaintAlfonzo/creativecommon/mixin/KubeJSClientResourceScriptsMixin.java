package com.SaintAlfonzo.creativecommon.mixin;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.client.GeneratedClientResourcePack;
import dev.latvian.mods.kubejs.script.ScriptManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Client scripts from resources ({@code assets/<ns>/kubejs/**}: Creative Delight's tooltips, names, lang, JEI info) at
 * game start. KubeJS 1902.6.2 loads client scripts first in the mod constructor with no resource manager
 * ({@code loadFromResources} is skipped), and again only on F3+T ({@code KeyboardHandlerMixin}), so until an F3+T the
 * resource scripts were never loaded. This loads them once, on the first client pack load, from the packs being opened
 * plus the {@code kubejs/} folder (the {@code kubejs/assets/} copies win over the jar's), before KubeJS's generated
 * pack exists. Its lang ({@code ClientEvents.lang}) is generated lazily later, so resource scripts' lang lines land.
 *
 * Only the first call: F3+T reloads scripts through KubeJS itself. A failure logs and leaves KubeJS's own load in place.
 */
@Mixin(value = GeneratedClientResourcePack.class, remap = false)
public class KubeJSClientResourceScriptsMixin {

	@Unique
	private static boolean creativecommon$loaded;

	@Inject(method = "inject", at = @At("HEAD"), require = 1, remap = false)
	private static void creativecommon$loadResourceClientScripts(Minecraft mc, List<PackResources> packs,
			CallbackInfoReturnable<List<PackResources>> cir) {
		if (creativecommon$loaded || KubeJS.instance == null) return;
		creativecommon$loaded = true;
		ScriptManager manager = KubeJS.getClientScriptManager();
		if (manager == null) return;
		FolderPackResources folder = new FolderPackResources(KubeJSPaths.DIRECTORY.toFile());
		try {
			List<PackResources> all = new ArrayList<>(packs);
			all.add(folder);
			// Not closed: closing the manager would close the game's packs; only the folder pack is ours.
			manager.reload(new MultiPackResourceManager(PackType.CLIENT_RESOURCES, all));
		} catch (Throwable t) {
			org.slf4j.LoggerFactory.getLogger("creativecommon").error(
					"[kubejs] loading client scripts from resources at startup failed; they load on F3+T", t);
		} finally {
			folder.close();
		}
	}
}
