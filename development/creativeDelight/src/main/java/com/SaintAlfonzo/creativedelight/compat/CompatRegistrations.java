package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativecommon.perf.IdleTickRules;
import com.SaintAlfonzo.creativecompat.block.VineShearing;
import com.SaintAlfonzo.creativecompat.compat.JarMixing;
import com.SaintAlfonzo.creativecompat.compat.jei.world.WorldTab;
import com.SaintAlfonzo.creativecompat.surface.StuckInBlock;
import com.SaintAlfonzo.creativedelight.CreativeDelight;

/**
 * What this jar hands to Creative Common and Creative Compat, so neither names it (design_rules.md DR-C7,
 * publish_readiness.md PR-CP-12): the Hearth and Harvest hops vine keeps its own shears handling, powdered sugar's
 * slowdown for the WTHIT speed line, this namespace's {@code jar_family/} tags as mixed-jar families, the sample
 * syringe as the tool of the Sample Syringe JEI world tab, and the idle random tick rule for treated-stick tomatoes.
 * Called from the constructor: the vine state is decided while blocks are constructed.
 */
public final class CompatRegistrations {

	private CompatRegistrations() {}

	public static void register() {
		// Hearth and Harvest hops: shears toggle its own sterilized state, and it overrides randomTick
		VineShearing.keepOwnHandling("com.SaintAlfonzo.creativedelight.hearth.block.HopsVineBlock");
		StuckInBlock.register("com.SaintAlfonzo.creativedelight.food.PowderedSugarBlock", 0.9);
		JarMixing.addFamilyNamespace(CreativeDelight.MOD_ID);
		WorldTab.addTool("sample_syringe", CreativeDelight.MOD_ID + ":sample_syringe");
		// grown treated-stick tomatoes still climb a stick placed above later (+wake_above)
		IdleTickRules.addModRule(CreativeDelight.MOD_ID + ":treated_stick_tomatoes age=max " + IdleTickRules.WAKE_ABOVE);
	}
}
