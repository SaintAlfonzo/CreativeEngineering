package com.SaintAlfonzo.solraspberrypie;

import com.SaintAlfonzo.solraspberrypie.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * FleetFootedMixin applies only with Alex's Mobs; the Food Dictionary mixins only with Spice of Life: Carrot Edition at
 * the tested version (they reach its private members); the AppleSkin tooltip row mixin only with AppleSkin 2.4.2;
 * every other mixin always applies.
 */
public class RpMixinPlugin extends GatedMixinPlugin {
	public RpMixinPlugin() {
		super("[solraspberrypie]", Map.of(
				"FleetFootedMixin", List.of("alexsmobs"),
				"CarrotFoodListMixin", List.of("solcarrot"),
				"CarrotFoodDataMixin", List.of("solcarrot"),
				"CarrotBookScreenMixin", List.of("solcarrot"),
				"AppleSkinTooltipRendererMixin", List.of("appleskin")),
				Map.of("solcarrot", "[1.19.2-1.14.0]", "appleskin", "[2.4.2+mc1.19]"));
	}
}
