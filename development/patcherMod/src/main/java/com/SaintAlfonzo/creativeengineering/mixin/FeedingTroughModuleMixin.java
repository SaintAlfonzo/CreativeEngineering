package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.compat.TroughRetryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.content.automation.module.FeedingTroughModule;

/**
 * Stops hungry animals re-searching for a Quark feeding trough every few ticks.
 *
 * THE COST:
 *   Quark's TemptGoalMixin calls {@code FeedingTroughModule#temptWithTroughs} from every TemptGoal#canUse
 *   once its schedule has passed - its {@code nextScheduledStart} is only moved forward by {@code start()},
 *   so in practice every canUse. An animal that is ready to breed and has no trough in reach (its cached
 *   trough is gone, or never existed) then runs a POI search (range 10) and a ray cast each time.
 *
 * THE FIX:
 *   When the call comes back without a trough (the result is not Quark's FakePlayer food holder) and no
 *   real player is tempting the animal, stamp the goal with "do not search before now + the cooldown"
 *   and answer the next calls with {@code found} unchanged. Hits are never stamped, so a cached trough
 *   keeps working at full speed. Cooldown is CEConfig perf.troughMissCooldownTicks (600 = 30 s, 0 = off).
 *   An ineligible animal (baby, in love, module off) also comes back without a trough and is stamped
 *   too, which only delays its first search by the cooldown once it becomes eligible.
 */
@Mixin(value = FeedingTroughModule.class, remap = false)
public class FeedingTroughModuleMixin {

	@Inject(method = "temptWithTroughs", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$skipAfterMiss(TemptGoal goal, Player found, ServerLevel level,
			CallbackInfoReturnable<Player> cir) {
		if (CEConfig.PERF.troughMissCooldownTicks.get() <= 0) return;
		if (level.getGameTime() < ((TroughRetryAccess) goal).ce$getTroughRetryAt()) {
			cir.setReturnValue(found);
		}
	}

	@Inject(method = "temptWithTroughs", at = @At("RETURN"), remap = false)
	private static void creativeengineering$recordMiss(TemptGoal goal, Player found, ServerLevel level,
			CallbackInfoReturnable<Player> cir) {
		int cooldown = CEConfig.PERF.troughMissCooldownTicks.get();
		if (cooldown <= 0 || found != null || cir.getReturnValue() instanceof FakePlayer) return;
		((TroughRetryAccess) goal).ce$setTroughRetryAt(level.getGameTime() + cooldown);
	}
}
