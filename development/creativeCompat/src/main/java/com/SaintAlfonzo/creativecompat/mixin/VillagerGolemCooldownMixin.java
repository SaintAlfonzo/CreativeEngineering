package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.mobs.GolemCooldown;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Golem farm cooldown (R2, plan_small_fixes_1116.md): {@code Villager#spawnGolemIfNeeded} (m_35397_, both the gossip and
 * the panic summon) does nothing while the villager's village is on cooldown ({@code mobs.GolemCooldown}).
 */
@Mixin(value = Villager.class, remap = false)
public abstract class VillagerGolemCooldownMixin {
	@Inject(method = "m_35397_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecompat$golemCooldown(ServerLevel level, long gameTime, int minVillagers, CallbackInfo ci) {
		if (GolemCooldown.blocked(level, ((Villager) (Object) this).blockPosition())) ci.cancel();
	}
}
