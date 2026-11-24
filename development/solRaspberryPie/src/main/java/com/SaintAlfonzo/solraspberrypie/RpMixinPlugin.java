// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie;

import com.SaintAlfonzo.solraspberrypie.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * The Food Dictionary mixins apply only with Spice of Life: Carrot Edition at the tested version (they reach its private members); the AppleSkin tooltip row mixin only with
 * AppleSkin 2.4.2; every other mixin always applies. The Fleet-Footed fix moved to Creative Compat
 * (plan_wave2_publish.md W2-3).
 */
public class RpMixinPlugin extends GatedMixinPlugin {
	public RpMixinPlugin() {
		super("[solraspberrypie]", Map.of(
				"CarrotFoodListMixin", List.of("solcarrot"),
				"CarrotFoodDataMixin", List.of("solcarrot"),
				"CarrotBookScreenMixin", List.of("solcarrot"),
				"AppleSkinTooltipRendererMixin", List.of("appleskin")),
				Map.of("solcarrot", "[1.19.2-1.14.0]", "appleskin", "[2.4.2+mc1.19]"));
	}
}
