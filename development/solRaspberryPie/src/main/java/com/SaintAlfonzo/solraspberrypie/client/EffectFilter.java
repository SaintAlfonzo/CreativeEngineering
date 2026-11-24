// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Which effect instances the client hides (9.5). The client cannot see cure lists, so an instance counts as ours when
 * its effect and level match what the server says it applied and it has at most one diet duration left; a longer
 * (merged potion) or stronger potion instance does not match and shows normally.
 */
public final class EffectFilter {
	private static boolean ours(MobEffectInstance i, S2CState s) {
		ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(i.getEffect());
		Integer lvl = id == null ? null : s.applied.get(id);
		return lvl != null && i.getAmplifier() == lvl - 1 && i.getDuration() <= s.duration;
	}

	private static boolean flat(MobEffectInstance i, S2CState s) {
		ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(i.getEffect());
		return id != null && s.flat.containsKey(id);
	}

	public static boolean widgetOn() {
		return RpConfig.CLIENT.widgetEnabled.get();
	}

	/** HUD: flat bonuses never; slot effects not while the widget is on. */
	public static Collection<MobEffectInstance> hud(Collection<MobEffectInstance> all) {
		S2CState s = ClientState.state;
		if (s == null || s.applied.isEmpty()) return all;
		// Per frame: a new list only when something is left out (PR-RP-20).
		List<MobEffectInstance> out = null;
		int n = 0;
		for (MobEffectInstance i : all) {
			boolean hide = ours(i, s) && (flat(i, s) || widgetOn());
			if (hide && out == null) out = new ArrayList<>(firstN(all, n));
			else if (!hide && out != null) out.add(i);
			n++;
		}
		return out == null ? all : out;
	}

	/** Inventory list: diet effects hide while the widget is on (the widget shows them). */
	public static Collection<MobEffectInstance> inventory(Collection<MobEffectInstance> all) {
		S2CState s = ClientState.state;
		if (s == null || s.applied.isEmpty() || !widgetOn()) return all;
		List<MobEffectInstance> out = null;
		int n = 0;
		for (MobEffectInstance i : all) {
			boolean hide = ours(i, s);
			if (hide && out == null) out = new ArrayList<>(firstN(all, n));
			else if (!hide && out != null) out.add(i);
			n++;
		}
		return out == null ? all : out;
	}

	/** The first {@code n} instances of {@code all}, in order. */
	private static List<MobEffectInstance> firstN(Collection<MobEffectInstance> all, int n) {
		List<MobEffectInstance> out = new ArrayList<>(Math.max(n, all.size() - 1));
		for (MobEffectInstance i : all) {
			if (out.size() >= n) break;
			out.add(i);
		}
		return out;
	}

	/**
	 * Diet flat buffs and slot effects count as permanent in Creative Common's two-column inventory effect list
	 * (plan_diet_books_r2.md B2). Registered by reflection only when Creative Common is loaded, so Raspberry Pie has no
	 * dependency on it.
	 */
	public static boolean permanent(MobEffectInstance i) {
		S2CState s = ClientState.state;
		return s != null && !s.applied.isEmpty() && ours(i, s);
	}

	static void registerPermanentSource() {
		if (!ModList.get().isLoaded("creativecommon")) return;
		try {
			Class.forName(PERMANENT_API).getMethod("addSource", Predicate.class)
					.invoke(null, (Predicate<MobEffectInstance>) EffectFilter::permanent);
		} catch (ReflectiveOperationException | LinkageError e) {
			SolRaspberryPie.LOG.warn("Creative Common's permanent effect list not found ({}); diet effects stay in the timed column", e.toString());
		}
	}

	private static final String PERMANENT_API = "com.SaintAlfonzo.creativecommon.client.PermanentEffects";

	private EffectFilter() {}
}
