package com.SaintAlfonzo.creativeengineering.perf;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Nock Enough Arrows gives every mob a TemptByWhistleGoal whose canUse runs an entity query for whistling arrows
 * within 12 blocks; in the four-player suite run (G04) that was 1.9% of the server thread with no arrow anywhere.
 * This keeps the server's live whistling arrows so {@link com.SaintAlfonzo.creativeengineering.mixin.NeaWhistleGoalSkipMixin}
 * can answer false without the query while there are none (perf.md). Matched by class name: no compile dependency.
 * Weak set: an arrow that never fires a leave event (server stop) cannot keep the check on.
 */
public final class WhistleArrowTracker {

	private static final String WHISTLING_ARROW = "com.itayfeder.nock_enough_arrows.arrows.whistling.WhistlingArrow";
	private static final Set<Entity> ARROWS = Collections.newSetFromMap(new WeakHashMap<>());

	private WhistleArrowTracker() {}

	public static boolean anyArrows() {
		return !ARROWS.isEmpty();
	}

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		Entity e = event.getEntity();
		if (!event.getLevel().isClientSide && e.getClass().getName().equals(WHISTLING_ARROW)) ARROWS.add(e);
	}

	@SubscribeEvent
	public static void onLeave(EntityLeaveLevelEvent event) {
		if (!event.getLevel().isClientSide) ARROWS.remove(event.getEntity());
	}
}
