package com.SaintAlfonzo.creativecommon.effect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Effects that hold other effects as real parts (listed in {@link CompoundEffects}): Botania's Emptiness holds Better
 * Beacons' Patrol Nullifier and Phantom Bane ({@code food-tuning #57}), Alex's Mobs' Fleet-Footed holds Botania's
 * Feather Feet. Adding the holder adds each part, hidden, for the same time, as Caffeinated does
 * with Phantom Bane. When the holder is removed early (milk, a cure, the diet ladder taking it away), the hidden parts
 * it added go with it. Effects are looked up lazily by id; a missing mod's holder or part is skipped.
 */
public final class HeldEffectParts {

	private static final Map<ResourceLocation, List<ResourceLocation>> PART_IDS = new LinkedHashMap<>();

	static {
		PART_IDS.put(new ResourceLocation("botania", "emptiness"), List.of(
				new ResourceLocation("better_beacons", "patrol_nullifier"),
				new ResourceLocation("better_beacons", "phantom_bane")));
		PART_IDS.put(new ResourceLocation("alexsmobs", "fleet_footed"), List.of(
				new ResourceLocation("botania", "feather_feet")));
	}

	private static final Map<MobEffect, List<MobEffect>> PARTS = new LinkedHashMap<>();
	private static boolean lookedUp;

	private HeldEffectParts() {}

	private static void lookUp() {
		if (lookedUp) return;
		PART_IDS.forEach((holder, ids) -> {
			MobEffect h = ForgeRegistries.MOB_EFFECTS.getValue(holder);
			List<MobEffect> parts = ids.stream().map(ForgeRegistries.MOB_EFFECTS::getValue).filter(e -> e != null).toList();
			if (h != null && !parts.isEmpty()) PARTS.put(h, parts);
		});
		lookedUp = true;
	}

	@SubscribeEvent
	public static void onAdded(MobEffectEvent.Added event) {
		lookUp();
		MobEffectInstance added = event.getEffectInstance();
		LivingEntity entity = event.getEntity();
		List<MobEffect> parts = PARTS.get(added.getEffect());
		if (parts == null || entity.level.isClientSide) return;
		for (MobEffect part : parts) {
			// a visible copy from another source (beacon, ladder) that lasts at least as long stays as it is; adding a
			// hidden one over it would hide its icon (MobEffectInstance.update copies visibility)
			MobEffectInstance held = entity.getEffect(part);
			if (held != null && held.isVisible() && held.getDuration() >= added.getDuration()) continue;
			entity.addEffect(new MobEffectInstance(part, added.getDuration(), 0, added.isAmbient(), false, false));
		}
	}

	@SubscribeEvent
	public static void onRemove(MobEffectEvent.Remove event) {
		lookUp();
		LivingEntity entity = event.getEntity();
		List<MobEffect> parts = event.getEffect() == null ? null : PARTS.get(event.getEffect());
		if (parts == null || entity.level.isClientSide) return;
		for (MobEffect part : parts) {
			MobEffectInstance held = entity.getEffect(part);
			// only the hidden copy the holder added; a visible copy from another source stays
			if (held != null && !held.isVisible() && !held.showIcon()) entity.removeEffect(part);
		}
	}
}
