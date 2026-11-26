// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation;

import com.SaintAlfonzo.creativeoxidation.shared.GatedMixinPlugin;
import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativeoxidation.mixins.json, the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin): the Supplementaries soap mixin applies only when Supplementaries is loaded, at a
 * tested version (it targets a private method). Every other mixin always applies.
 */
public class OxidationMixinPlugin extends GatedMixinPlugin {
	private static final Map<String, List<String>> NEEDS_MOD = Map.of(
			"SoapWashableHelperMixin", List.of("supplementaries"));

	private static final Map<String, String> MOD_RANGE = Map.of(
			"supplementaries", "[1.19.2-2.4.20,1.19.2-2.5)");

	public OxidationMixinPlugin() {
		super("[creativeoxidation]", NEEDS_MOD, MOD_RANGE);
	}
}
