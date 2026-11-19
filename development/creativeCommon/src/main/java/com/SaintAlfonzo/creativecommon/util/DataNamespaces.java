// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.util;

import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Set;
import java.util.TreeSet;

/**
 * Namespaces that ship server data in the loaded packs (mod jars, datapacks, the KubeJS data folder), taken from the
 * server resource manager at the start of every data reload ({@code mixin/ReloadableServerResourcesMixin}). Scripts ask
 * {@code global.has.datapack(ns)}: the tag helper keeps an entry whose namespace is no mod but ships data, such as the
 * structure datapacks ported into {@code kubejs/data/<ns>} (plan_bugfix_r1118.md B2, TODO terrain-adaptation #1).
 */
public final class DataNamespaces {
	private static volatile Set<String> namespaces = Set.of();
	private static volatile int generation;

	private DataNamespaces() {}

	/** Called at the head of {@code ReloadableServerResources.loadResources}, before any listener (tags included) runs. */
	public static void capture(ResourceManager resources) {
		Set<String> found = new TreeSet<>();
		try {
			found.addAll(resources.getNamespaces());
		} catch (RuntimeException e) {
			com.mojang.logging.LogUtils.getLogger().warn("[data-namespaces] could not read the resource manager's namespaces: {}", e.toString());
		}
		namespaces = Set.copyOf(found);
		generation++;
	}

	/** Whether a loaded pack ships server data under this namespace (false before the first reload). */
	public static boolean has(String ns) {
		return ns != null && namespaces.contains(ns);
	}

	/** Count of namespaces seen on the last reload (0 before the first). */
	public static int count() {
		return namespaces.size();
	}

	/** Bumped on every reload, so scripts can tell a new reload's set from the last one's. */
	public static int generation() {
		return generation;
	}
}
