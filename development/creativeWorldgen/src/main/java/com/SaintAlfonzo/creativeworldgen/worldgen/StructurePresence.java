package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Which structures and placement types the world being loaded really has, for structure_overrides.js (the structure
 * gate, TODO creativebrewing/publish #5). structure_config.json is generated from the full pack, so on an instance
 * without a mod or data pack it names sets the game cannot decode: a placement type from an absent mod
 * (betterjungletemples:jungle_temple) or a structure whose file is not loaded. Either fails world creation
 * ("Unknown registry key").
 *
 * capture(event) takes the resource manager KubeJS built for this load (DataPackEventJS#wrappedManager: every mod
 * jar, kubejs/data, the donor packs). It has no getter, so it is read by reflection; when that fails (a KubeJS
 * update renamed it) every structure counts as present, which is the old behaviour, and one WARN says so.
 */
public final class StructurePresence {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static ResourceManager manager;

	private StructurePresence() {}

	/** Call first thing in ServerEvents.highPriorityData; returns false when the manager could not be read. */
	public static boolean capture(Object dataPackEvent) {
		manager = null;
		try {
			Field f = dataPackEvent.getClass().getDeclaredField("wrappedManager");
			f.setAccessible(true);
			Object m = f.get(dataPackEvent);
			if (m instanceof ResourceManager rm) manager = rm;
		} catch (ReflectiveOperationException | RuntimeException e) {
			LOGGER.warn("[structures] could not read the data pack event's resource manager ({}); every configured structure is treated as present", e.toString());
		}
		return manager != null;
	}

	/**
	 * True when data/<ns>/worldgen/structure/<path>.json is in a loaded pack, or a mod registered the structure in code
	 * (BuiltinRegistries: AE2's meteorite has no JSON, scratch r1106cbb), or nothing was captured.
	 */
	public static boolean structure(String id) {
		if (manager == null) return true;
		ResourceLocation rl = ResourceLocation.tryParse(id);
		if (rl == null) return false;
		if ("minecraft".equals(rl.getNamespace())) return true;
		if (BuiltinRegistries.STRUCTURES.containsKey(rl)) return true;
		ResourceLocation file = new ResourceLocation(rl.getNamespace(), "worldgen/structure/" + rl.getPath() + ".json");
		return manager.getResource(file).isPresent();
	}

	/** True when the placement type is registered (code, so this works without a capture). */
	public static boolean placementType(String id) {
		ResourceLocation rl = ResourceLocation.tryParse(id == null ? "" : id);
		return rl != null && Registry.STRUCTURE_PLACEMENT_TYPE.containsKey(rl);
	}

	/** Drop the reference once the load is done. */
	public static void release() {
		manager = null;
	}
}
