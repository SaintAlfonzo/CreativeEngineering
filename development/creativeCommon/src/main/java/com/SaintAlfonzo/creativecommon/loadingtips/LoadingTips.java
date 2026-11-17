package com.SaintAlfonzo.creativecommon.loadingtips;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the tip list from every resource pack: {@code assets/<namespace>/loading_tips/<name>.json}
 * (feature_writeup/loading_tips.md section 2). The format grew from Loading Screen Tips by UltrusBot (MIT):
 * <pre>{"override": false, "priority": 0, "replace": false, "remove": ["ns:id"],
 *  "tips": [{"id": "ns:slug", "text": "text or lang key"}, "a plain string"]}</pre>
 * Files are applied in ascending {@code priority}, then by file id. Tips are kept in an ordered id -> text map.
 * A file with {@code "override": true} replaces same-id tips in place, adds new ids at the end and drops the ids in
 * its {@code remove} list; any other file never replaces (a repeated id keeps the first tip and logs a clash). A
 * plain-string tip gets a generated id from its file and text. {@code "replace": true} drops every tip read before
 * the file. A tip is shown as the translation when it is a lang key, else as written.
 */
public final class LoadingTips extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static volatile List<String> tips = List.of();

	private LoadingTips() {
		super(new Gson(), "loading_tips");
	}

	public static List<String> tips() {
		return tips;
	}

	private record TipFile(ResourceLocation id, int priority, JsonObject json) {}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
		List<TipFile> sorted = new ArrayList<>();
		for (Map.Entry<ResourceLocation, JsonElement> e : files.entrySet()) {
			try {
				JsonObject o = GsonHelper.convertToJsonObject(e.getValue(), "loading tips");
				sorted.add(new TipFile(e.getKey(), GsonHelper.getAsInt(o, "priority", 0), o));
			} catch (RuntimeException ex) {
				LOGGER.warn("Loading tips: skipped {}: {}", e.getKey(), ex.getMessage());
			}
		}
		sorted.sort(Comparator.comparingInt(TipFile::priority).thenComparing(TipFile::id));

		Map<String, String> out = new LinkedHashMap<>();
		int replaced = 0, removed = 0, clashes = 0, unmatched = 0;
		for (TipFile f : sorted) {
			try {
				JsonObject o = f.json();
				if (GsonHelper.getAsBoolean(o, "replace", false)) out.clear();
				boolean override = GsonHelper.getAsBoolean(o, "override", false);
				if (override && o.has("remove")) {
					for (JsonElement r : GsonHelper.getAsJsonArray(o, "remove")) {
						String id = GsonHelper.convertToString(r, "remove id");
						if (out.remove(id) != null) removed++;
						else {
							unmatched++;
							LOGGER.warn("Loading tips: {} removes {}, which no earlier file has", f.id(), id);
						}
					}
				}
				for (JsonElement t : GsonHelper.getAsJsonArray(o, "tips")) {
					String id, text;
					if (t.isJsonObject()) {
						JsonObject to = t.getAsJsonObject();
						id = GsonHelper.getAsString(to, "id");
						text = GsonHelper.getAsString(to, "text").strip();
					} else {
						text = GsonHelper.convertToString(t, "tip").strip();
						id = f.id() + "/" + text;
					}
					if (text.isEmpty()) continue;
					if (!out.containsKey(id)) {
						// An override naming another pack's id that nothing defines is a typo or an upstream change.
						if (override && !id.startsWith(f.id().getNamespace() + ":")) {
							unmatched++;
							LOGGER.warn("Loading tips: {} overrides {}, which no earlier file has", f.id(), id);
						}
						out.put(id, text);
					} else if (override) {
						out.put(id, text);
						replaced++;
					} else {
						clashes++;
						LOGGER.warn("Loading tips: {} repeats id {} without override; the first tip stays", f.id(), id);
					}
				}
			} catch (RuntimeException ex) {
				LOGGER.warn("Loading tips: skipped {}: {}", f.id(), ex.getMessage());
			}
		}
		tips = List.copyOf(out.values());
		LoadingTipBar.reset();
		LOGGER.info("Loading tips: {} tips from {} files ({} replaced, {} removed, {} id clashes, {} unmatched ids)",
				out.size(), files.size(), replaced, removed, clashes, unmatched);
	}

	@Mod.EventBusSubscriber(modid = CreativeCommon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static final class Reload {
		private Reload() {}

		@SubscribeEvent
		public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(new LoadingTips());
		}
	}
}
