// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Names Raspberry Pie gives to other mods' keys (plan_rp_standalone.md P4, D5): Apple Pie's Food Book is the Diet Log,
 * Carrot Edition's Food Book is the Food Dictionary, Apple Pie's open-book key is "Open Diet Log". A lang key of
 * another namespace in our own lang file is not enough: the client merges the namespaces in HashMap order, so Apple Pie's
 * file can load after ours and win (it does with few mods). Our lang files instead carry
 * {@code solraspberrypie.override.<key>} entries, and this copies each onto {@code <key>} once every file is merged
 * (ClientLanguageMixin). Per language and resource-pack overridable like any other key.
 */
public final class LangOverrides {
	public static final String PREFIX = "solraspberrypie.override.";

	public static void apply(Map<String, String> translations) {
		Map<String, String> found = new HashMap<>();
		for (Map.Entry<String, String> e : translations.entrySet()) {
			if (e.getKey().startsWith(PREFIX) && e.getKey().length() > PREFIX.length()) {
				found.put(e.getKey().substring(PREFIX.length()), e.getValue());
			}
		}
		translations.putAll(found);
	}

	private LangOverrides() {}
}
