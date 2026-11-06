package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import de.cadentem.create_gravity.events.ForgeEvents;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs Create: Gravity's per-entity tick logic only in the dimensions that can have gravity biomes.
 *
 * THE COST:
 *   {@code ForgeEvents#handleLogic} listens to every LivingTickEvent. For every living entity in every
 *   dimension it looks the biome up (a map get plus a biome-config loop), builds a fresh
 *   AttributeModifier, and updates the gravity attribute, and for players it also runs the oxygen
 *   logic. The pack's biome_configs lists only #minecraft:is_end, so outside the End all of that
 *   concludes "nothing to do".
 *
 * THE FIX:
 *   Cancel the handler outside the dimensions in CommonConfig perf.gravityDimensions (default the End).
 *   One exception: an entity that still carries the mod's low-gravity modifier (it just left the End)
 *   runs the handler once more, because that run is what removes the modifier. The modifier UUID is
 *   copied from the mod's own constant (it is private).
 */
@Mixin(value = ForgeEvents.class, remap = false)
public class CreateGravityDimensionMixin {

	private static final UUID LOW_GRAVITY_UUID = UUID.fromString("7871c3e3-1016-4d26-b65d-b154a5399e16");

	private static List<? extends String> creativecommon$sourceList;
	private static Set<ResourceLocation> creativecommon$allowed = Set.of();

	@Inject(method = "handleLogic", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativecommon$limitDimensions(LivingTickEvent event, CallbackInfo ci) {
		if (!CommonConfig.PERF.gravityLimitDimensions.get()) return;
		LivingEntity entity = event.getEntity();
		if (creativecommon$allowedDimensions().contains(entity.getLevel().dimension().location())) return;
		AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
		if (gravity != null && gravity.getModifier(LOW_GRAVITY_UUID) != null) return;
		ci.cancel();
	}

	private static Set<ResourceLocation> creativecommon$allowedDimensions() {
		List<? extends String> list = CommonConfig.PERF.gravityDimensions.get();
		if (list != creativecommon$sourceList) {
			Set<ResourceLocation> set = new HashSet<>();
			for (String id : list) {
				ResourceLocation rl = ResourceLocation.tryParse(id);
				if (rl != null) set.add(rl);
			}
			creativecommon$allowed = set;
			creativecommon$sourceList = list;
		}
		return creativecommon$allowed;
	}
}
