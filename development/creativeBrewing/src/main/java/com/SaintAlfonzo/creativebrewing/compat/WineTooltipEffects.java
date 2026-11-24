package com.SaintAlfonzo.creativebrewing.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;

/**
 * The Vinery port's wine table as an effect source for Creative Compat's compound effect tooltip lines
 * ({@code CompoundEffectTooltip.addEffectSource}, registered by {@code CreativeBrewing}): {@code WineEffects.get(item)}
 * at the wine's base level, or none for other items. Read by reflection: the Vinery port is a separate member jar.
 */
public final class WineTooltipEffects {

	private static Method wineGet;
	private static boolean wineLookedUp;

	private WineTooltipEffects() {}

	public static List<MobEffectInstance> effects(Item item) {
		if (!wineLookedUp) {
			wineLookedUp = true;
			try {
				wineGet = Class.forName("net.satisfy.vinery.core.wine.WineEffects").getMethod("get", Item.class);
			} catch (ReflectiveOperationException | LinkageError absent) {
				wineGet = null;
			}
		}
		if (wineGet == null) return List.of();
		try {
			Object wine = wineGet.invoke(null, item);
			if (wine == null) return List.of();
			List<?> list = (List<?>) wine.getClass().getMethod("effects").invoke(wine);
			int amplifier = (Integer) wine.getClass().getMethod("amplifier").invoke(wine);
			List<MobEffectInstance> out = new ArrayList<>();
			for (Object effect : list) {
				if (effect instanceof MobEffect me) out.add(new MobEffectInstance(me, 1, amplifier));
			}
			return out;
		} catch (ReflectiveOperationException | ClassCastException e) {
			return List.of();
		}
	}
}
