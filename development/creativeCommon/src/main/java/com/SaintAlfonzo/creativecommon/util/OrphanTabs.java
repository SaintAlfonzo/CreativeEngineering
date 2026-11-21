// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.util;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creative tabs for orphaned items (jei/jei-coverage #29). An item whose mod registers it with no creative tab (Abnormals
 * Delight does that for its compat foods when the parent mod is absent) is never in creative mode, its search or JEI,
 * because JEI's item list is the creative tabs' contents. {@code mixin/ItemOrphanTabMixin} asks {@link #tabFor} when
 * {@code Item.getItemCategory()} would return null.
 *
 * <p>Allow rule: only the ids in {@code [creative_menu] orphanTabItems} get a tab. A pack-wide "every orphan" rule would
 * also list technical items mods hide on purpose, and the pack keeps some orphans hidden (escargot; the maple cookie is
 * merged into the Syrup Cookie). The tab is the one most other items of the same mod use, so a compat food lands next to
 * its siblings; a mod with no tabbed item falls back to vanilla Food (edible) or Miscellaneous.
 *
 * <p>The list is read on the first query after the config has loaded and is kept for the session.
 */
public final class OrphanTabs {
	private OrphanTabs() {}

	public static final List<String> DEFAULTS = List.of(
			"abnormals_delight:maple_glazed_bacon",
			"abnormals_delight:cherry_cookie",
			"abnormals_delight:duck_fillet",
			"abnormals_delight:cooked_duck_fillet",
			"abnormals_delight:duck_noodles");

	private static volatile Set<ResourceLocation> allowed;
	private static final Map<Item, CreativeModeTab> CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
	private static final Map<String, CreativeModeTab> BY_MOD = Collections.synchronizedMap(new HashMap<>());

	/** The configured ids, or null while the config is not loaded yet. */
	private static Set<ResourceLocation> allowed() {
		Set<ResourceLocation> a = allowed;
		if (a != null) return a;
		List<? extends String> ids;
		try {
			ids = CommonConfig.CREATIVE_MENU.orphanTabItems.get();
		} catch (IllegalStateException notLoaded) {
			return null;
		}
		Set<ResourceLocation> out = new HashSet<>();
		for (String s : ids) {
			ResourceLocation rl = ResourceLocation.tryParse(s);
			if (rl != null) out.add(rl);
		}
		allowed = out;
		return out;
	}

	/** True when the item is on the allow list (whether or not its mod gave it a tab). */
	public static boolean isListed(Item item) {
		Set<ResourceLocation> a = allowed();
		if (a == null || a.isEmpty()) return false;
		ResourceLocation id = Registry.ITEM.getKey(item);
		return a.contains(id);
	}

	/** The tab for an item whose own category is null, or null to keep it hidden. */
	public static CreativeModeTab tabFor(Item item) {
		CreativeModeTab cached = CACHE.get(item);
		if (cached != null) return cached;
		if (!isListed(item)) return null;
		ResourceLocation id = Registry.ITEM.getKey(item);
		CreativeModeTab tab = BY_MOD.computeIfAbsent(id.getNamespace(), OrphanTabs::majorityTab);
		if (tab == null) tab = item.isEdible() ? CreativeModeTab.TAB_FOOD : CreativeModeTab.TAB_MISC;
		CACHE.put(item, tab);
		return tab;
	}

	/** The tab most items of the namespace use (listed items skipped, so this never recurses), or null. */
	private static CreativeModeTab majorityTab(String namespace) {
		Map<CreativeModeTab, Integer> counts = new IdentityHashMap<>();
		for (Item other : Registry.ITEM) {
			ResourceLocation oid = Registry.ITEM.getKey(other);
			if (!namespace.equals(oid.getNamespace()) || isListed(other)) continue;
			CreativeModeTab t = other.getItemCategory();
			if (t != null) counts.merge(t, 1, Integer::sum);
		}
		CreativeModeTab best = null;
		int bestCount = 0;
		for (Map.Entry<CreativeModeTab, Integer> e : counts.entrySet()) {
			if (e.getValue() > bestCount) {
				best = e.getKey();
				bestCount = e.getValue();
			}
		}
		return best;
	}
}
