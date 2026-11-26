// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.client;

import com.SaintAlfonzo.creativeoxidation.CreativeOxidation;
import com.SaintAlfonzo.creativeoxidation.OxidationProperties;
import com.SaintAlfonzo.creativeoxidation.OxidationStage;
import com.jozufozu.flywheel.core.PartialModel;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Staged copies of the Create partial models that render copper outside the block model (boiler gauge, fluid valve
 * pointer, portable fluid interface). Flywheel 0.6 bakes a PartialModel only when it is created before model loading,
 * so {@link #init} runs from the mod constructor. A part is registered only when its model JSON is in this jar
 * (assets/creativeoxidation/models/block/<stage>/<part>.json, copied in P6); a missing one keeps Create's model.
 * Create 0.5.1 renders the valve handle from its block model, so upstream's valve handle partial is not needed here.
 */
public final class OxidizedPartialModels {
	private static final Map<OxidationStage, Map<Part, PartialModel>> MODELS = new EnumMap<>(OxidationStage.class);

	private OxidizedPartialModels() {
	}

	public static void init() {
		if (!MODELS.isEmpty()) return;
		for (OxidationStage stage : OxidationStage.values()) {
			if (stage == OxidationStage.UNAFFECTED) continue;
			Map<Part, PartialModel> stageModels = new EnumMap<>(Part.class);
			for (Part part : Part.values()) {
				String path = "block/" + stage.getSerializedName() + "/" + part.path;
				if (OxidizedPartialModels.class.getResource("/assets/" + CreativeOxidation.MOD_ID + "/models/" + path + ".json") != null) {
					stageModels.put(part, new PartialModel(new ResourceLocation(CreativeOxidation.MOD_ID, path)));
				}
			}
			MODELS.put(stage, stageModels);
		}
	}

	public static PartialModel forState(Part part, BlockState state, PartialModel original) {
		if (state == null || !OxidationProperties.hasProperties(state)) return original;
		Map<Part, PartialModel> stageModels = MODELS.get(state.getValue(OxidationProperties.STAGE));
		return stageModels == null ? original : stageModels.getOrDefault(part, original);
	}

	public enum Part {
		BOILER_GAUGE("steam_engine/gauge"),
		BOILER_GAUGE_DIAL("steam_engine/gauge_dial"),
		PORTABLE_INTERFACE_TOP("portable_fluid_interface/block_top"),
		PORTABLE_INTERFACE_MIDDLE("portable_fluid_interface/block_middle"),
		PORTABLE_INTERFACE_MIDDLE_POWERED("portable_fluid_interface/block_middle_powered"),
		FLUID_VALVE_POINTER("fluid_valve/pointer");

		private final String path;

		Part(String path) {
			this.path = path;
		}
	}
}
