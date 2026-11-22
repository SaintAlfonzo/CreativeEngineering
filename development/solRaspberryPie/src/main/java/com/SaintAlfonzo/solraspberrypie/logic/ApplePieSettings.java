// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-start check of Apple Pie's own settings (plan_rp_standalone.md P5). Raspberry Pie seeds no Apple Pie file (D4):
 * it runs on whatever the world's {@code solapplepie-server.toml} says, Apple Pie's defaults included, and names the
 * settings it cannot work with in one boxed warning. Settings it works with but that change how it behaves get one INFO
 * line each. Apple Pie's defaults (queueSize 32, endDecay 32, minFoodsToActivate 0, minContribution 0) give no warning.
 */
public final class ApplePieSettings {
	public static void report() {
		List<String> problems = new ArrayList<>();
		List<String> notes = new ArrayList<>();
		int size = SOLSweetPotatoConfig.size(), start = SOLSweetPotatoConfig.startDecay(), end = SOLSweetPotatoConfig.endDecay();
		double minContribution = SOLSweetPotatoConfig.minContribution();

		if (minContribution > 0) {
			problems.add("minContribution is " + minContribution + " (needs 0): foods aged out by respecs and death still count a little, so every respec costs less (6.1)");
		}
		if (SOLSweetPotatoConfig.shouldResetOnDeath()) {
			problems.add("resetOnDeath is true (needs false): Apple Pie clears the whole food memory on death, before Raspberry Pie can age the window out");
		}
		if (start > end) {
			problems.add("startDecay " + start + " is above endDecay " + end + " (Apple Pie needs startDecay <= endDecay): diversity values are wrong");
		}
		if (end >= size) {
			notes.add("endDecay " + end + " is not below queueSize " + size + ": foods aged out by respecs and death are forgotten at the next meal "
					+ "instead of staying in the Diet Log history (costs are unchanged); a queueSize above endDecay keeps them");
		}
		if (!SOLSweetPotatoConfig.getBenefitsUnparsed().isEmpty() || !SOLSweetPotatoConfig.getThresholds().isEmpty()) {
			notes.add("Apple Pie's own benefit list is not empty; Raspberry Pie keeps it off (its ladder replaces it)");
		}

		for (String n : notes) SolRaspberryPie.LOG.info("Apple Pie settings: {}", n);
		if (problems.isEmpty()) return;
		SolRaspberryPie.LOG.warn("================================================================");
		SolRaspberryPie.LOG.warn("APPLE PIE SETTINGS RASPBERRY PIE CANNOT WORK WITH (serverconfig/solapplepie-server.toml):");
		for (String p : problems) SolRaspberryPie.LOG.warn("  {}", p);
		SolRaspberryPie.LOG.warn("  Change them in the world's serverconfig (or defaultconfigs for new worlds) and restart.");
		SolRaspberryPie.LOG.warn("================================================================");
	}

	private ApplePieSettings() {}
}
