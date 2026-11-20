// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.client;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.mojang.logging.LogUtils;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import org.slf4j.Logger;

/**
 * Client API: which active effects count as permanent, for the inventory effect list's second column
 * (plan_diet_books_r2.md B2, mixin.EffectColumnsMixin). Permanent means any of:
 * <ul>
 *   <li>the instance is ambient (beacon, conduit);</li>
 *   <li>its effect is held by worn armor or a held tool through Creative Equipment's gear specs (the server sends the
 *       set, effect.GearEffectMarks) and the instance is a short refreshed one, not a longer potion of the same
 *       effect;</li>
 *   <li>a source another mod added with {@link #addSource} says so (Raspberry Pie: diet flat buffs and slot effects).</li>
 * </ul>
 * Long-duration effects from anything else stay timed. No client-only types here, so the class is safe to load on a
 * dedicated server (the gear packet handler touches it).
 */
public final class PermanentEffects {

	private static final Logger LOG = LogUtils.getLogger();

	/** Gear instances are refreshed at 40 ticks; anything longer is another source of the same effect. */
	public static final int GEAR_MAX_DURATION = 100;

	private static final List<Predicate<MobEffectInstance>> SOURCES = new CopyOnWriteArrayList<>();
	private static volatile Set<MobEffect> gear = Set.of();
	private static boolean warned;

	private PermanentEffects() {
	}

	/** Adds a test that marks instances as permanent. Called once at client setup; exceptions are caught per call. */
	public static void addSource(Predicate<MobEffectInstance> source) {
		if (source != null) {
			SOURCES.add(source);
		}
	}

	public static boolean isPermanent(MobEffectInstance i) {
		if (i == null) {
			return false;
		}
		if (i.isAmbient()) {
			return true;
		}
		if (i.getDuration() <= GEAR_MAX_DURATION && gear.contains(i.getEffect())) {
			return true;
		}
		for (Predicate<MobEffectInstance> source : SOURCES) {
			try {
				if (source.test(i)) {
					return true;
				}
			} catch (RuntimeException e) {
				if (!warned) {
					warned = true;
					LOG.warn("[permanent effects] a source threw; treating as not permanent", e);
				}
			}
		}
		return false;
	}

	/** The effects the local player's gear holds, from the server (effect.GearEffectMarks). */
	public static void setGear(Set<MobEffect> effects) {
		gear = effects == null ? Set.of() : Set.copyOf(effects);
	}

	public static Set<MobEffect> gear() {
		return gear;
	}
}
