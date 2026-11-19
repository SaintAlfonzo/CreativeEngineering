package com.SaintAlfonzo.solraspberrypie;

import com.SaintAlfonzo.solraspberrypie.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * FleetFootedMixin applies only with Alex's Mobs; the Food Dictionary mixins only with Spice of Life: Carrot Edition at
 * the tested version (they reach its private members); every other mixin always applies.
 */
public class RpMixinPlugin extends GatedMixinPlugin {
	public RpMixinPlugin() {
		super("[solraspberrypie]", Map.of(
				"FleetFootedMixin", List.of("alexsmobs"),
				"CarrotFoodListMixin", List.of("solcarrot"),
				"CarrotFoodDataMixin", List.of("solcarrot"),
				"CarrotBookScreenMixin", List.of("solcarrot")),
				Map.of("solcarrot", "[1.19.2-1.14.0]"));
	}
}
