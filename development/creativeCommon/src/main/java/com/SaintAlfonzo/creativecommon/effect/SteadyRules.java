package com.SaintAlfonzo.creativecommon.effect;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Steady, Fruits Delight's Astringent under its pack name ({@code food-tuning #51}, raspberry_pie.md 12.13). Fruits
 * Delight already caps block friction at 0.6 while it lasts (no sliding on ice, soap blocks or slime). The pack adds:
 * <ul>
 *   <li>immunity to the slip effects (Immersive Engineering's Slippery, Neapolitan's Slipping): refused while Steady
 *       lasts and cleared when Steady is added;</li>
 *   <li>a light step: pressure plates (both kinds), tripwires and big dripleaf ignore the holder
 *       ({@code mixin/SteadyBlockTriggersMixin}, {@code SteadyWeightedPlateMixin}, {@code SteadyDripleafMixin}).</li>
 * </ul>
 * Effects are looked up lazily by id; without Fruits Delight nothing changes.
 */
public final class SteadyRules {

	private static final ResourceLocation STEADY_ID = new ResourceLocation("fruitsdelight", "astringent");
	private static final List<ResourceLocation> SLIP_IDS = List.of(
			new ResourceLocation("immersiveengineering", "slippery"),
			new ResourceLocation("neapolitan", "slipping"));

	private static MobEffect steady;
	private static List<MobEffect> slips = List.of();
	private static boolean lookedUp;

	private SteadyRules() {}

	private static void lookUp() {
		if (lookedUp) return;
		steady = ForgeRegistries.MOB_EFFECTS.getValue(STEADY_ID);
		slips = SLIP_IDS.stream().map(ForgeRegistries.MOB_EFFECTS::getValue).filter(e -> e != null).toList();
		lookedUp = true;
	}

	/** True when {@code entity} is a living entity with Steady. */
	public static boolean isSteady(Entity entity) {
		if (!(entity instanceof LivingEntity living)) return false;
		lookUp();
		return steady != null && living.hasEffect(steady);
	}

	@SubscribeEvent
	public static void onApplicable(MobEffectEvent.Applicable event) {
		lookUp();
		if (steady == null || slips.isEmpty()) return;
		if (slips.contains(event.getEffectInstance().getEffect()) && event.getEntity().hasEffect(steady)) {
			event.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public static void onAdded(MobEffectEvent.Added event) {
		lookUp();
		if (steady == null || event.getEffectInstance().getEffect() != steady) return;
		LivingEntity entity = event.getEntity();
		if (entity.level.isClientSide) return;
		for (MobEffect slip : slips) {
			if (entity.hasEffect(slip)) entity.removeEffect(slip);
		}
	}
}
