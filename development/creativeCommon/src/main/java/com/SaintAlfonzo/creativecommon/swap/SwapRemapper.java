package com.SaintAlfonzo.creativecommon.swap;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegistryManager;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * The world half of the swap lists ({@link SwapLists}): when a world (or a server's registry sync) still holds an id that
 * no longer exists, Forge fires {@link MissingMappingsEvent} for each registry, and every id a list names is remapped to
 * its survivor. Forge then aliases the old id, so blocks in saved chunks, items in every container, inventory and dropped
 * stack, block entities and entities all load as the survivor, and the world saves under the new id from then on.
 * Covers every Forge registry (blocks, items, fluids, block entity types, entity types, ...). Vanilla-only registries
 * (structure processors) have no remap hook; the jar that moved such an id keeps a load-only alias for it.
 * Ids a list names as removed get Forge's ignore action: no missing-registry warning, the old blocks and items are dropped.
 */
public final class SwapRemapper {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** Forge has no API to remove an alias. The field is Forge's own name, not obfuscated. */
	private static final Field ALIASES = aliasesField();

	private SwapRemapper() {}

	@SubscribeEvent
	public static void onMissingMappings(MissingMappingsEvent event) {
		SwapLists lists = SwapLists.get();
		if (lists.isEmpty()) return;
		int remapped = 0;
		int unresolved = 0;
		int ignored = 0;
		for (MissingMappingsEvent.Mapping<?> mapping : all(event)) {
			ResourceLocation target = lists.target(mapping.getKey());
			if (target == null) {
				if (lists.isRemoved(mapping.getKey())) {
					mapping.ignore();
					ignored++;
				}
				continue;
			}
			if (remap(mapping, target)) {
				remapped++;
			} else {
				unresolved++;
				LOGGER.warn("[swaps] {} {} -> {}: the survivor is not registered, left missing", event.getKey().location(), mapping.getKey(), target);
			}
		}
		if (remapped > 0) LOGGER.info("[swaps] {}: {} id(s) remapped to their survivors", event.getKey().location(), remapped);
		if (ignored > 0) LOGGER.info("[swaps] {}: {} removed id(s) dropped (listed as removed, no replacement)", event.getKey().location(), ignored);
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
		dropAliasFromSurvivor(mapping.getRegistry(), registry.getKey(value));
		return true;
	}

	/**
	 * Removes the alias leading out of a survivor from the registry Forge is loading into. Forge is about to add old -> survivor,
	 * and a world saved before an earlier swap the other way (old -> survivor is the reverse of a saved survivor -> old) would
	 * then hold a cycle. {@code ForgeRegistry.getRaw} follows aliases with no cycle check and spins forever when neither name is
	 * registered yet, which is the case while it copies the staging registry into the active one (hang found 2026-11-08).
	 * The survivor is a registered name, so names win over its alias and the alias is dead anyway. Dropping it unconditionally
	 * also covers cycles closed by two remaps in one event, which a walk of the saved aliases could not see: Forge adds the new
	 * aliases only after the event, and with every survivor a sink no new alias can be part of a cycle.
	 */
	static void dropAliasFromSurvivor(Object registry, ResourceLocation survivor) {
		if (!(registry instanceof ForgeRegistry<?>)) return;
		try {
			@SuppressWarnings("unchecked")
			Map<ResourceLocation, ResourceLocation> aliases = (Map<ResourceLocation, ResourceLocation>) ALIASES.get(registry);
			ResourceLocation stale = aliases.remove(survivor);
			if (stale != null) LOGGER.info("[swaps] dropped stale alias {} -> {}: the id is registered again", survivor, stale);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("[swaps] could not read ForgeRegistry.aliases", e);
		}
	}

	private static Field aliasesField() {
		try {
			Field field = ForgeRegistry.class.getDeclaredField("aliases");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("[swaps] ForgeRegistry.aliases not found", e);
		}
	}

	/** The event is fired once per registry; its key's type is only known as a wildcard. */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static java.util.List<MissingMappingsEvent.Mapping<?>> all(MissingMappingsEvent event) {
		return (java.util.List) event.getAllMappings((net.minecraft.resources.ResourceKey) event.getKey());
	}
}
