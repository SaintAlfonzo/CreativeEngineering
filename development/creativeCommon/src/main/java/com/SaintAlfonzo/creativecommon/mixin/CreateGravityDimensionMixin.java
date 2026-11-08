package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import de.cadentem.create_gravity.config.ServerConfig;
import de.cadentem.create_gravity.events.ForgeEvents;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
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
 *   limit (LowGravity.dimensionAllowed) gates the non-living low-gravity mixins.
 *   One exception: an entity that still carries the mod's low-gravity modifier (it just left the End)
 *   runs the handler once more, because that run is what removes the modifier. The modifier UUID is
 *   copied from the mod's own constant (it is private).
 *
 * DEDICATED SERVER START (scratch run r1108cge SQ-CGE-02e):
 *   The mod builds its biome list only in reloadConfig(), called on TagsUpdatedEvent (skipped while its server config
 *   is not loaded yet) and on a config file edit. On a dedicated server the first TagsUpdatedEvent fires before the
 *   server config loads, so the list stays null and nothing gets low gravity until someone runs /reload. After the
 *   mod stores the server at ServerStartedEvent (its biome ids resolve through it), build the list once.
 */
@Mixin(value = ForgeEvents.class, remap = false)
public class CreateGravityDimensionMixin {

	private static final UUID LOW_GRAVITY_UUID = UUID.fromString("7871c3e3-1016-4d26-b65d-b154a5399e16");


	@Inject(method = "handleLogic", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativecommon$limitDimensions(LivingTickEvent event, CallbackInfo ci) {
		LivingEntity entity = event.getEntity();
		if (LowGravity.dimensionAllowed(entity.getLevel())) return;
		AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
		if (gravity != null && gravity.getModifier(LOW_GRAVITY_UUID) != null) return;
		ci.cancel();
	}

	@Inject(method = "setServer", at = @At("TAIL"), remap = false, require = 0)
	private static void creativecommon$buildBiomeList(ServerStartedEvent event, CallbackInfo ci) {
		if (ServerConfig.SPEC.isLoaded()) ServerConfig.reloadConfig();
	}

}
