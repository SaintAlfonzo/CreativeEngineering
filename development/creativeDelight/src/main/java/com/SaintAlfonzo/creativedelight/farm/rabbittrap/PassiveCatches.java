// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.rabbittrap;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Ruling D12 (farming_for_blockheads.md 2.3.2): unattended catches are shared by nearby traps. While a trap holds an
 * unattended catch, no other trap within {@code rabbitTrapPassiveSpacing} blocks (in each direction) makes one. A trap
 * whose chunk loads again queues its "time away" roll here instead of rolling at once; the queue waits until no trap
 * has joined for 20 ticks (at most 100), then rolls them in random order, so the trap that gets the catch is not
 * always the one that loaded first.
 */
public final class PassiveCatches {
	private static final int QUIET_TICKS = 20;
	private static final int MAX_WAIT_TICKS = 100;

	private static final Map<LevelAccessor, Queue> QUEUES = new WeakHashMap<>();

	private PassiveCatches() {
	}

	private static final class Queue {
		final List<BlockPos> traps = new ArrayList<>();
		long first;
		long last;
	}

	static void enqueue(ServerLevel level, BlockPos pos) {
		Queue q = QUEUES.computeIfAbsent(level, l -> new Queue());
		long now = level.getGameTime();
		if (q.traps.isEmpty()) q.first = now;
		q.last = now;
		if (!q.traps.contains(pos)) q.traps.add(pos.immutable());
	}

	static void tick(LevelAccessor accessor) {
		if (!(accessor instanceof ServerLevel level)) return;
		Queue q = QUEUES.get(level);
		if (q == null || q.traps.isEmpty()) return;
		long now = level.getGameTime();
		if (now - q.last < QUIET_TICKS && now - q.first < MAX_WAIT_TICKS) return;
		List<BlockPos> batch = new ArrayList<>(q.traps);
		q.traps.clear();
		Collections.shuffle(batch, new java.util.Random(level.getRandom().nextLong()));
		for (BlockPos pos : batch) {
			if (!level.isLoaded(pos)) continue;
			if (level.getBlockEntity(pos) instanceof RabbitTrapBlockEntity trap) trap.rollTimeAway(level);
		}
	}

	static void forget(LevelAccessor level) {
		QUEUES.remove(level);
	}

	/** True when a loaded trap other than {@code pos} within the spacing holds an unattended catch. */
	static boolean blocked(ServerLevel level, BlockPos pos) {
		int spacing = CDConfig.RABBIT_TRAP.passiveSpacing();
		if (spacing <= 0) return false;
		int minX = SectionPos.blockToSectionCoord(pos.getX() - spacing), maxX = SectionPos.blockToSectionCoord(pos.getX() + spacing);
		int minZ = SectionPos.blockToSectionCoord(pos.getZ() - spacing), maxZ = SectionPos.blockToSectionCoord(pos.getZ() + spacing);
		for (int cx = minX; cx <= maxX; cx++) {
			for (int cz = minZ; cz <= maxZ; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) continue;
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					if (!(be instanceof RabbitTrapBlockEntity other) || !other.holdsPassiveCatch()) continue;
					BlockPos o = be.getBlockPos();
					if (o.equals(pos)) continue;
					if (Math.abs(o.getX() - pos.getX()) <= spacing && Math.abs(o.getY() - pos.getY()) <= spacing
							&& Math.abs(o.getZ() - pos.getZ()) <= spacing) return true;
				}
			}
		}
		return false;
	}
}
