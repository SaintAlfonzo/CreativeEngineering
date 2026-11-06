package com.SaintAlfonzo.creativedelight.compat;

/**
 * Per-goal "do not search for a feeding trough before this game tick" stamp, added to
 * {@code TemptGoal} by {@code mixin/TemptGoalTroughRetryMixin} and used by
 * {@code mixin/FeedingTroughModuleMixin}. Lives outside the mixin package because mixin classes
 * cannot be referenced directly.
 */
public interface TroughRetryAccess {
    long ce$getTroughRetryAt();

    void ce$setTroughRetryAt(long gameTime);
}
