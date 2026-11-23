// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ComputeFovModifierEvent;

import java.util.UUID;

/**
 * Fleet-Footed's field-of-view shift (food-tuning #55): Alex's Mobs' Fleet-Footed adds a +0.2 movement speed modifier
 * while the player sprint-jumps, and vanilla widens the view with every speed change. Only the part of the widening
 * that modifier causes is scaled by {@code fleetFootedFov}; the rest of the speed (Speed, sprinting) widens it as usual.
 * Reads the synced attribute only, so it works with or without the Fleet-Footed fix (FleetFootedMixin uses the same id).
 */
public final class FleetFootedFov {
	private static final UUID BONUS = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF29A");

	public static void onFov(ComputeFovModifierEvent e) {
		double share = RpConfig.CLIENT.fleetFootedFov.get();
		if (share >= 1.0) return;
		Player p = e.getPlayer();
		AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null || speed.getModifier(BONUS) == null) return;
		float walk = p.getAbilities().getWalkingSpeed();
		if (walk == 0) return;
		double with = speed.getValue();
		AttributeInstance copy = new AttributeInstance(Attributes.MOVEMENT_SPEED, i -> {});
		copy.replaceFrom(speed);
		copy.removeModifier(BONUS);
		double without = copy.getValue();
		// Vanilla: modifier *= (speed / walk + 1) / 2. The ratio of the two factors is the Fleet-Footed part.
		double fWith = (with / walk + 1) / 2, fWithout = (without / walk + 1) / 2;
		if (fWith <= 0) return;
		double full = e.getNewFovModifier();
		double base = full * fWithout / fWith;
		e.setNewFovModifier((float) (base + (full - base) * share));
	}

	private FleetFootedFov() {}
}
