package com.SaintAlfonzo.creativecompat.surface;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;

/**
 * Arrow despawn (plan_passthrough.md 5.4 and the seventh-pass ruling): every arrow players can pick up ({@code pickup
 * == ALLOWED} with a pickup item; Create Misc and Things' neon tube returns none, so it keeps the minute), stuck in a
 * block or a web, lasts {@code [surface.passthrough] arrowLifeTicks} (12000, 10 minutes) instead of vanilla's 1200.
 * At most {@code arrowLongLifeCap} (128) such arrows per chunk: when one more joins, the oldest there (highest
 * {@code life}) is demoted to vanilla's minute (despawning on its next tick if it is already past it); the demotion
 * is saved ({@link PassSave}).
 *
 * <p>Runs from HEAD of {@code AbstractArrow#tickDespawn} ({@code mixin/ArrowPassMixin}), server side, so it covers
 * exactly the arrows that despawn at all: {@code ThrownTrident}, the Dragon Loot trident and Creative Delight's
 * thrown tools skip {@code super.tickDespawn()} only when pickable with Loyalty, so Loyalty ones never despawn and never
 * count, and every other trident or thrown tool gets the 10 minutes and counts (seventh pass).
 *
 * <p>The per-chunk sets are filled lazily: an arrow joins its chunk's set on its first despawn tick there (also after
 * a reload, which refills a loaded chunk), leaves it on {@code EntityLeaveLevelEvent} (picked up, despawned, killed,
 * unloaded), and a set drops members that fell out or moved away whenever another arrow joins it.
 */
public final class PassArrows {

	private PassArrows() {}

	private static final Map<Level, Long2ObjectMap<LinkedHashSet<AbstractArrow>>> BY_LEVEL = new WeakHashMap<>();

	/**
	 * The despawn limit for this tick, or -1 to run vanilla's code (system off, not pickable). Joins the chunk's set,
	 * which may demote the oldest arrow there (possibly this one).
	 */
	public static int despawnLimit(AbstractArrow arrow) {
		PassValues values = PassValues.local();
		if (!values.enabled) return -1;
		PassState st = PassThrough.state(arrow, true);
		if (st.longLife == 0) {
			boolean pickable = arrow.pickup == AbstractArrow.Pickup.ALLOWED
					&& !((PassArrow) arrow).creativecompat$pickupItem().isEmpty();
			st.longLife = (byte) (pickable ? 2 : 1);
		}
		boolean pickable = st.longLife == 2;
		if (pickable && !st.shortLife) join(arrow, st, values.arrowLongLifeCap);
		return PassMath.despawnLimit(true, pickable, st.shortLife, values.arrowLifeTicks);
	}

	private static void join(AbstractArrow arrow, PassState st, int cap) {
		long chunk = ChunkPos.asLong(arrow.getBlockX() >> 4, arrow.getBlockZ() >> 4);
		if (st.arrowJoined && st.arrowChunk == chunk) return;
		Long2ObjectMap<LinkedHashSet<AbstractArrow>> level = BY_LEVEL.computeIfAbsent(arrow.level, l -> new Long2ObjectOpenHashMap<>());
		if (st.arrowJoined) remove(level, st.arrowChunk, arrow);
		LinkedHashSet<AbstractArrow> set = level.computeIfAbsent(chunk, c -> new LinkedHashSet<>());
		set.add(arrow);
		st.arrowJoined = true;
		st.arrowChunk = chunk;
		if (set.size() <= cap) return;
		prune(set, chunk);
		while (set.size() > Math.max(0, cap)) {
			AbstractArrow[] members = set.toArray(new AbstractArrow[0]);
			int[] lives = new int[members.length];
			for (int i = 0; i < members.length; i++) lives[i] = ((PassArrow) members[i]).creativecompat$life();
			AbstractArrow oldest = members[PassMath.oldestIndex(lives, lives.length)];
			set.remove(oldest);
			PassState os = PassThrough.state(oldest, true);
			os.shortLife = true;
			os.arrowJoined = false;
		}
	}

	/** Drops members that are gone, no longer stuck, demoted, or now in another chunk. */
	private static void prune(LinkedHashSet<AbstractArrow> set, long chunk) {
		for (Iterator<AbstractArrow> it = set.iterator(); it.hasNext(); ) {
			AbstractArrow a = it.next();
			PassState s = PassThrough.state(a, false);
			boolean keep = !a.isRemoved() && ((PassArrow) a).creativecompat$inGround() && s != null && !s.shortLife
					&& ChunkPos.asLong(a.getBlockX() >> 4, a.getBlockZ() >> 4) == chunk;
			if (!keep) {
				it.remove();
				if (s != null) s.arrowJoined = false;
			}
		}
	}

	private static void remove(Long2ObjectMap<LinkedHashSet<AbstractArrow>> level, long chunk, AbstractArrow arrow) {
		LinkedHashSet<AbstractArrow> set = level.get(chunk);
		if (set == null) return;
		set.remove(arrow);
		if (set.isEmpty()) level.remove(chunk);
	}

	/** Forge bus: an arrow leaving its level leaves its chunk's set. */
	public static void onLeave(EntityLeaveLevelEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof AbstractArrow arrow) || e.level.isClientSide) return;
		PassState st = PassThrough.state(arrow, false);
		if (st == null || !st.arrowJoined) return;
		Long2ObjectMap<LinkedHashSet<AbstractArrow>> level = BY_LEVEL.get(event.getLevel());
		if (level != null) remove(level, st.arrowChunk, arrow);
		st.arrowJoined = false;
	}

	/** Forge bus: a level going away drops its sets. */
	public static void onLevelUnload(LevelEvent.Unload event) {
		if (event.getLevel() instanceof Level l) BY_LEVEL.remove(l);
	}

	/** Long-life arrows currently counted in a chunk (tests, debug). */
	public static int count(Level level, long chunk) {
		Long2ObjectMap<LinkedHashSet<AbstractArrow>> m = BY_LEVEL.get(level);
		LinkedHashSet<AbstractArrow> set = m == null ? null : m.get(chunk);
		return set == null ? 0 : set.size();
	}
}
