package com.SaintAlfonzo.creativecommon.effect;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Botania's Emptiness holds Better Beacons' Patrol Nullifier and Phantom Bane as parts ({@code food-tuning #57},
 * raspberry_pie.md 12.13; listed in {@link CompoundEffects}). Adding Emptiness adds both, hidden, for the same time,
 * as Caffeinated does with Phantom Bane, so patrols and phantoms skip the holder too. When Emptiness is removed early
 * (milk, a cure), the hidden parts it added go with it. Effects are looked up lazily by id; without Botania or Better
 * Beacons nothing changes.
 */
public final class EmptinessParts {

	private static final ResourceLocation EMPTINESS_ID = new ResourceLocation("botania", "emptiness");
	private static final List<ResourceLocation> PART_IDS = List.of(
			new ResourceLocation("better_beacons", "patrol_nullifier"),
			new ResourceLocation("better_beacons", "phantom_bane"));

	private static MobEffect emptiness;
	private static List<MobEffect> parts = List.of();
	private static boolean lookedUp;

	private EmptinessParts() {}

	private static void lookUp() {
		if (lookedUp) return;
		emptiness = ForgeRegistries.MOB_EFFECTS.getValue(EMPTINESS_ID);
		parts = PART_IDS.stream().map(ForgeRegistries.MOB_EFFECTS::getValue).filter(e -> e != null).toList();
		lookedUp = true;
	}

	@SubscribeEvent
	public static void onAdded(MobEffectEvent.Added event) {
		lookUp();
		MobEffectInstance added = event.getEffectInstance();
		LivingEntity entity = event.getEntity();
		if (emptiness == null || entity.level.isClientSide || added.getEffect() != emptiness) return;
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
		if (emptiness == null || entity.level.isClientSide || event.getEffect() != emptiness) return;
		for (MobEffect part : parts) {
			MobEffectInstance held = entity.getEffect(part);
			// only the hidden copy Emptiness added; a beacon's or the diet ladder's own Phantom Bane stays
			if (held != null && !held.isVisible() && !held.showIcon()) entity.removeEffect(part);
		}
	}
}
