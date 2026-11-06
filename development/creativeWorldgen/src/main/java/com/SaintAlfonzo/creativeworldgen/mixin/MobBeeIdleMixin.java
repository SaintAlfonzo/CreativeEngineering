package com.SaintAlfonzo.creativeworldgen.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bees below y 32 with no player within 32 blocks skip their AI step (goals, sensing, navigation, move and look
 * controls). Bees ticked underground in the four-player suite run (G04), where bees were 5.9% of the server thread,
 * most of it this step (wander goal flight pathing). Movement, gravity and damage still tick; the bee resumes
 * as soon as a player comes near (perf.md).
 */
@Mixin(Mob.class)
public abstract class MobBeeIdleMixin {

	private static final double MAX_Y = 32.0;
	private static final double PLAYER_RANGE = 32.0;

	// m_6140_ = Mob.serverAiStep() (no refmap: SRG name, remap = false)
	@Inject(method = "m_6140_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$idleDeepBee(CallbackInfo ci) {
		if (!((Object) this instanceof Bee bee) || bee.getY() >= MAX_Y) return;
		if (!bee.level.hasNearbyAlivePlayer(bee.getX(), bee.getY(), bee.getZ(), PLAYER_RANGE)) ci.cancel();
	}
}
