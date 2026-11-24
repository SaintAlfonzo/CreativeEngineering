package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.perf.GravityDimensions;
import de.cadentem.create_gravity.events.ForgeEvents;
import java.util.UUID;
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
 *   Cancel the handler outside the dimensions in CommonConfig perf.gravityDimensions (default the End); the same
 *   limit (perf.GravityDimensions) gates Creative Compat's low gravity for non-living entities.
 *   One exception: an entity that still carries the mod's low-gravity modifier (it just left the End)
 *   runs the handler once more, because that run is what removes the modifier. The modifier UUID is
 *   copied from the mod's own constant (it is private).
 *
 * The dedicated server start fix (the biome list built at ServerStartedEvent) is Creative Compat's
 * CreateGravityServerStartMixin (plan_wave2_publish.md W2-4).
 */
@Mixin(value = ForgeEvents.class, remap = false)
public class CreateGravityDimensionMixin {

	private static final UUID LOW_GRAVITY_UUID = UUID.fromString("7871c3e3-1016-4d26-b65d-b154a5399e16");

	@Inject(method = "handleLogic", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativecommon$limitDimensions(LivingTickEvent event, CallbackInfo ci) {
		LivingEntity entity = event.getEntity();
		if (GravityDimensions.allowed(entity.getLevel())) return;
		AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
		if (gravity != null && gravity.getModifier(LOW_GRAVITY_UUID) != null) return;
		ci.cancel();
	}
}
