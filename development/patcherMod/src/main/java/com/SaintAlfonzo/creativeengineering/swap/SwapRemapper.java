package com.SaintAlfonzo.creativeengineering.swap;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegistryManager;
import org.slf4j.Logger;

/**
 * The world half of the swap lists ({@link SwapLists}): when a world (or a server's registry sync) still holds an id that
 * no longer exists, Forge fires {@link MissingMappingsEvent} for each registry, and every id a list names is remapped to
 * its survivor. Forge then aliases the old id, so blocks in saved chunks, items in every container, inventory and dropped
 * stack, block entities and entities all load as the survivor, and the world saves under the new id from then on.
 * Covers every Forge registry (blocks, items, fluids, block entity types, entity types, ...). Vanilla-only registries
 * (structure processors) have no remap hook; the jar that moved such an id keeps a load-only alias for it.
 */
public final class SwapRemapper {
	private static final Logger LOGGER = LogUtils.getLogger();

	private SwapRemapper() {}

	@SubscribeEvent
	public static void onMissingMappings(MissingMappingsEvent event) {
		SwapLists lists = SwapLists.get();
		if (lists.isEmpty()) return;
		int remapped = 0;
		int unresolved = 0;
		for (MissingMappingsEvent.Mapping<?> mapping : all(event)) {
			ResourceLocation target = lists.target(mapping.getKey());
			if (target == null) continue;
			if (remap(mapping, target)) {
				remapped++;
			} else {
				unresolved++;
				LOGGER.warn("[swaps] {} {} -> {}: the survivor is not registered, left missing", event.getKey().location(), mapping.getKey(), target);
			}
		}
		if (remapped > 0) LOGGER.info("[swaps] {}: {} id(s) remapped to their survivors", event.getKey().location(), remapped);
		if (unresolved > 0) LOGGER.warn("[swaps] {}: {} listed id(s) had no survivor", event.getKey().location(), unresolved);
	}

	/**
	 * The survivor is looked up in the ACTIVE registry. On a world load Forge fires the event from its STAGING copy
	 * (GameData.injectSnapshot), which holds only the snapshot's ids at that point, so {@code mapping.getRegistry()}
	 * never contains an id that is new in this version (found 2026-11-01, scratch_run_2026-11-01.md 7).
	 */
	private static <T> boolean remap(MissingMappingsEvent.Mapping<T> mapping, ResourceLocation target) {
		IForgeRegistry<T> registry = RegistryManager.ACTIVE.getRegistry(mapping.getRegistry().getRegistryKey());
		if (registry == null || !registry.containsKey(target)) return false;
		T value = registry.getValue(target);
		if (value == null) return false;
		mapping.remap(value);
		return true;
	}

	/** The event is fired once per registry; its key's type is only known as a wildcard. */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static java.util.List<MissingMappingsEvent.Mapping<?>> all(MissingMappingsEvent event) {
		return (java.util.List) event.getAllMappings((net.minecraft.resources.ResourceKey) event.getKey());
	}
}
