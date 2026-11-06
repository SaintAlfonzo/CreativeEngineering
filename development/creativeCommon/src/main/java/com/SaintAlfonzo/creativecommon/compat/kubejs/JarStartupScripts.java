package com.SaintAlfonzo.creativecommon.compat.kubejs;

import com.SaintAlfonzo.creativecommon.mixin.KubeJSScriptManagerInvoker;
import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptPackInfo;
import dev.latvian.mods.kubejs.script.ScriptSource;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KubeJS startup scripts shipped in mod jars (creative_delight.md 10.5, 10.6). A jar puts them under
 * {@code kubejs_startup/} at its root (not a resource path, so no resource or data pack sees them); each such jar
 * becomes one script pack named after its first mod id. Files inside a pack sort by their {@code // priority:}
 * header and honour {@code // requires:} and {@code // packmode:}, exactly as in the pack folder (the files go
 * through KubeJS's own {@code loadFile}).
 *
 * Pack order (the map KubeJS runs in insertion order):
 * <ol>
 *   <li>patcherMod's own pack (the shared helpers: global.NCH, global.tags, global.compost), so every later
 *       script can call them at top level;</li>
 *   <li>the pack folder {@code kubejs/startup_scripts/} (the dicts, items, rules);</li>
 *   <li>every other mod jar's pack, in mod-list order (Creative Delight, ...), so they can read the dicts.</li>
 * </ol>
 * One {@code global} object is shared by all packs, but each pack has its own scope: a top-level {@code var} in one
 * pack is not visible in another, so shared code goes through {@code global}.
 */
public final class JarStartupScripts {

	private static final Logger LOGGER = LogUtils.getLogger();
	/** Folder at a jar's root that holds its startup scripts. */
	public static final String DIRECTORY = "kubejs_startup";
	/** Creative Common's mod id (patcherMod's until the 2026-11-04 swap): its pack goes first. */
	private static final String HELPERS_MOD_ID = "creativecommon";

	private JarStartupScripts() {}

	public static void addPacks(ScriptManager manager) {
		if (manager.scriptType != ScriptType.STARTUP || ModList.get() == null) return;

		List<ScriptPack> head = new ArrayList<>();
		List<ScriptPack> tail = new ArrayList<>();
		for (IModFileInfo fileInfo : ModList.get().getModFiles()) {
			if (fileInfo.getMods().isEmpty()) continue;
			Path root = fileInfo.getFile().findResource(DIRECTORY);
			if (!Files.isDirectory(root)) continue;
			String namespace = fileInfo.getMods().get(0).getModId();
			if (manager.packs.containsKey(namespace)) {
				manager.scriptType.console.error("[jar-startup] skipped " + namespace + ": a script pack of that name is already loaded");
				continue;
			}
			try {
				ScriptPack pack = load(manager, namespace, root);
				(HELPERS_MOD_ID.equals(namespace) ? head : tail).add(pack);
			} catch (Throwable t) {
				// One broken jar must not stop the pack's own startup scripts.
				manager.scriptType.console.error("[jar-startup] failed to read " + DIRECTORY + "/ of " + namespace + ": " + t);
				LOGGER.error("[jar-startup] failed to read {}/ of {}", DIRECTORY, namespace, t);
			}
		}
		if (head.isEmpty() && tail.isEmpty()) return;

		Map<String, ScriptPack> folderPacks = new LinkedHashMap<>(manager.packs);
		manager.packs.clear();
		head.forEach(p -> manager.packs.put(p.info.namespace, p));
		manager.packs.putAll(folderPacks);
		tail.forEach(p -> manager.packs.put(p.info.namespace, p));

		String order = String.join(", ", manager.packs.keySet());
		manager.scriptType.console.info("[jar-startup] startup script packs in order: " + order);
		LOGGER.info("[jar-startup] startup script packs in order: {}", order);
	}

	private static ScriptPack load(ScriptManager manager, String namespace, Path root) {
		ScriptPack pack = new ScriptPack(manager, new ScriptPackInfo(namespace, ""));
		KubeJS.loadScripts(pack, root, "");
		ScriptSource.FromPath source = info -> root.resolve(info.file);
		for (ScriptFileInfo info : pack.info.scripts) {
			((KubeJSScriptManagerInvoker) manager).creativecommon$loadFile(pack, info, source);
		}
		pack.scripts.sort(null);
		manager.scriptType.console.info("[jar-startup] " + namespace + ": " + pack.scripts.size() + " of "
				+ pack.info.scripts.size() + " files loaded from its jar");
		return pack;
	}
}
