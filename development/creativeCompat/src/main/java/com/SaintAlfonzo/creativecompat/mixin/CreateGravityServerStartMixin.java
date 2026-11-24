// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import de.cadentem.create_gravity.config.ServerConfig;
import de.cadentem.create_gravity.events.ForgeEvents;
import net.minecraftforge.event.server.ServerStartedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create: Gravity on a dedicated server start (scratch run r1108cge SQ-CGE-02e; moved from Creative Common's
 * CreateGravityDimensionMixin in the wave 2 carve, plan_wave2_publish.md W2-4).
 *
 * The mod builds its biome list only in reloadConfig(), called on TagsUpdatedEvent (skipped while its server config is
 * not loaded yet) and on a config file edit. On a dedicated server the first TagsUpdatedEvent fires before the server
 * config loads, so the list stays null and nothing gets low gravity until someone runs /reload. After the mod stores
 * the server at ServerStartedEvent (its biome ids resolve through it), build the list once.
 */
@Mixin(value = ForgeEvents.class, remap = false)
public class CreateGravityServerStartMixin {

	@Inject(method = "setServer", at = @At("TAIL"), remap = false, require = 0)
	private static void creativecompat$buildBiomeList(ServerStartedEvent event, CallbackInfo ci) {
		if (ServerConfig.SPEC.isLoaded()) ServerConfig.reloadConfig();
	}
}
