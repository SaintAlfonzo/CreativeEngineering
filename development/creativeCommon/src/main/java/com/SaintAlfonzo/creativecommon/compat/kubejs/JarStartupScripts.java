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
 *   <li>Creative Common's own pack (the shared helpers: global.NCH, global.tags, global.compost, the dicts), so every
 *       later script can call them at top level;</li>
 *   <li>helper files of other jars, one pack per jar ({@code <modid>_helpers}), in mod-list order: a file opts in with
 *       the header line {@code // phase: helpers} (Creative Compat's jei_order.js: the pack's
 *       {@code startup_scripts/jei/order.js} calls its global.jeiOrder.define at top level);</li>
 *   <li>the pack folder {@code kubejs/startup_scripts/} (items, rules);</li>
 *   <li>the rest of every other mod jar's pack, in mod-list order (Creative Delight, ...), so they can read the dicts
 *       and whatever the pack's startup scripts define.</li>
 * </ol>
 * A helper file runs in its own pack's scope, apart from the rest of its jar: it shares nothing but {@code global}.
 * Until 2026-11-08 the second step was a hard-coded list naming Creative Compat (scratch run r1108cge).
 * One {@code global} object is shared by all packs, but each pack has its own scope: a top-level {@code var} in one
 * pack is not visible in another, so shared code goes through {@code global}.
 */
public final class JarStartupScripts {

	private static final Logger LOGGER = LogUtils.getLogger();
	/** Folder at a jar's root that holds its startup scripts. */
	public static final String DIRECTORY = "kubejs_startup";
	/** Creative Common's mod id (patcherMod's until the 2026-11-04 swap): its whole pack goes first. */
	private static final String HELPERS_MOD_ID = "creativecommon";
	/** Header that moves a file of another jar ahead of the pack's startup_scripts folder. */
	private static final String PHASE_PROPERTY = "phase";
	private static final String PHASE_HELPERS = "helpers";

	private JarStartupScripts() {}

	public static void addPacks(ScriptManager manager) {
		if (manager.scriptType != ScriptType.STARTUP || ModList.get() == null) return;

		ScriptPack common = null;
		List<ScriptPack> helpers = new ArrayList<>();
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
				if (HELPERS_MOD_ID.equals(namespace)) {
					common = pack;
				} else {
					ScriptPack early = splitHelpers(manager, pack, namespace, root);
					if (early != null) helpers.add(early);
					tail.add(pack);
				}
			} catch (Throwable t) {
				// One broken jar must not stop the pack's own startup scripts.
				manager.scriptType.console.error("[jar-startup] failed to read " + DIRECTORY + "/ of " + namespace + ": " + t);
				LOGGER.error("[jar-startup] failed to read {}/ of {}", DIRECTORY, namespace, t);
			}
		}
		if (common == null && helpers.isEmpty() && tail.isEmpty()) return;

		Map<String, ScriptPack> folderPacks = new LinkedHashMap<>(manager.packs);
		manager.packs.clear();
		if (common != null) manager.packs.put(common.info.namespace, common);
		helpers.forEach(p -> manager.packs.put(p.info.namespace, p));
		manager.packs.putAll(folderPacks);
		tail.forEach(p -> manager.packs.put(p.info.namespace, p));

		String order = String.join(", ", manager.packs.keySet());
		manager.scriptType.console.info("[jar-startup] startup script packs in order: " + order);
		LOGGER.info("[jar-startup] startup script packs in order: {}", order);
	}

	/**
	 * Moves the files of a loaded jar pack whose header says {@code // phase: helpers} into a pack of their own,
	 * {@code <namespace>_helpers}, that runs before the pack folder; null when the jar has none.
	 */
	private static ScriptPack splitHelpers(ScriptManager manager, ScriptPack pack, String namespace, Path root) {
		List<ScriptFileInfo> early = new ArrayList<>();
		pack.scripts.removeIf(file -> {
			if (!PHASE_HELPERS.equals(file.info.getProperty(PHASE_PROPERTY, ""))) return false;
			early.add(file.info);
			return true;
		});
		if (early.isEmpty()) return null;
		ScriptPack helpers = new ScriptPack(manager, new ScriptPackInfo(namespace + "_" + PHASE_HELPERS, ""));
		ScriptSource.FromPath source = info -> root.resolve(info.file);
		for (ScriptFileInfo info : early) {
			((KubeJSScriptManagerInvoker) manager).creativecommon$loadFile(helpers, info, source);
		}
		helpers.scripts.sort(null);
		manager.scriptType.console.info("[jar-startup] " + namespace + ": " + helpers.scripts.size()
				+ " helper file(s) run before startup_scripts");
		return helpers;
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
