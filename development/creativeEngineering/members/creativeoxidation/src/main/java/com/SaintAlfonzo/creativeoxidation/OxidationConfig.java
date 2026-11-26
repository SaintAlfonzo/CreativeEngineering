// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import net.minecraftforge.common.ForgeConfigSpec;

/** config/creativeoxidation-common.toml */
public final class OxidationConfig {
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.BooleanValue ENABLED;
	public static final ForgeConfigSpec.DoubleValue SPEED_FACTOR;
	public static final ForgeConfigSpec.BooleanValue KEEP_STAGE_ON_BREAK;

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.push("oxidation");
		ENABLED = b.comment("Copper machine aging, waxing, scraping and lightning cleaning on Create, Create Deco and Design n' Decor copper blocks.")
				.translation("creativeoxidation.config.oxidationEnabled")
				.define("enabled", true);
		SPEED_FACTOR = b.comment("Multiplier on vanilla copper's aging attempt rate. 0 stops aging; 1 is vanilla speed.")
				.translation("creativeoxidation.config.oxidationSpeedFactor")
				.defineInRange("speedFactor", 1.0, 0.0, 100.0);
		b.pop();
		// R12 (plan_creative_oxidation.md): replaces upstream's clutterReducer3000, inverted
		KEEP_STAGE_ON_BREAK = b.comment("Broken aged or waxed blocks drop an item that keeps the stage and wax, and placing it restores them. Off: they drop as the plain item.")
				.translation("creativeoxidation.config.keepStageOnBreak")
				.define("keepStageOnBreak", true);
		SPEC = b.build();
	}

	private OxidationConfig() {
	}

	public static boolean enabled() {
		return !SPEC.isLoaded() || ENABLED.get();
	}

	public static double speedFactor() {
		return SPEC.isLoaded() ? Math.max(0.0, SPEED_FACTOR.get()) : 1.0;
	}

	public static boolean keepStageOnBreak() {
		return !SPEC.isLoaded() || KEEP_STAGE_ON_BREAK.get();
	}
}
