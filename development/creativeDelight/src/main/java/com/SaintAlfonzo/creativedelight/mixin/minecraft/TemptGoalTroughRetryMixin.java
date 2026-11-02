package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.compat.TroughRetryAccess;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Carries the feeding-trough retry stamp for {@link FeedingTroughModuleMixin}. */
@Mixin(TemptGoal.class)
public class TemptGoalTroughRetryMixin implements TroughRetryAccess {

	@Unique
	private long ce$troughRetryAt;

	@Override
	public long ce$getTroughRetryAt() {
		return ce$troughRetryAt;
	}

	@Override
	public void ce$setTroughRetryAt(long gameTime) {
		ce$troughRetryAt = gameTime;
	}
}
