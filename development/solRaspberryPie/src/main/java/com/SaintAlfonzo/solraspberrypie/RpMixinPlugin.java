// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie;

import com.SaintAlfonzo.solraspberrypie.shared.GatedMixinPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.service.MixinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FleetFootedMixin applies only with Alex's Mobs, and only when its target still has the members the fix reads
 * (plan_rp_standalone.md P6): an Alex's Mobs update that renames or drops them only disables the fix, with one warning,
 * instead of failing the mixin config (it is {@code required}). The Food Dictionary mixins only with Spice of Life:
 * Carrot Edition at the tested version (they reach its private members); the AppleSkin tooltip row mixin only with
 * AppleSkin 2.4.2; every other mixin always applies.
 */
public class RpMixinPlugin extends GatedMixinPlugin {
	private static final Logger LOG = LoggerFactory.getLogger("Raspberry Pie");
	private static final String FLEET_FOOTED = "FleetFootedMixin";
	private static final String LIVING_INT = "(Lnet/minecraft/world/entity/LivingEntity;I)V";

	public RpMixinPlugin() {
		super("[solraspberrypie]", Map.of(
				FLEET_FOOTED, List.of("alexsmobs"),
				"CarrotFoodListMixin", List.of("solcarrot"),
				"CarrotFoodDataMixin", List.of("solcarrot"),
				"CarrotBookScreenMixin", List.of("solcarrot"),
				"AppleSkinTooltipRendererMixin", List.of("appleskin")),
				Map.of("solcarrot", "[1.19.2-1.14.0]", "appleskin", "[2.4.2+mc1.19]"));
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (!super.shouldApplyMixin(targetClassName, mixinClassName)) return false;
		if (mixinClassName.endsWith("." + FLEET_FOOTED)) return fleetFootedMatches(targetClassName);
		return true;
	}

	/**
	 * EffectFleetFooted (Alex's Mobs 1.21.1, the version tested) must have the int field {@code lastDuration} (shadowed;
	 * written by its isDurationEffectTick) and applyEffectTick(LivingEntity, int) (SRG {@code m_6742_}, the injection
	 * point). Anything else, including an unreadable class, skips the fix with one warning.
	 */
	private static boolean fleetFootedMatches(String targetClassName) {
		List<String> missing = new ArrayList<>();
		try {
			ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
			if (node.fields == null || node.fields.stream().noneMatch(f -> f.name.equals("lastDuration") && f.desc.equals("I"))) {
				missing.add("int field lastDuration");
			}
			if (node.methods == null || node.methods.stream().noneMatch(m -> (m.name.equals("m_6742_") || m.name.equals("applyEffectTick")) && m.desc.equals(LIVING_INT))) {
				missing.add("applyEffectTick(LivingEntity, int) (m_6742_)");
			}
		} catch (Throwable t) {
			missing.add("the class itself (" + t + ")");
		}
		if (missing.isEmpty()) return true;
		LOG.warn("[solraspberrypie] Alex's Mobs {} lacks {}: the Fleet-Footed fix is off (tested with Alex's Mobs 1.21.1)",
				targetClassName, String.join(", ", missing));
		return false;
	}
}
