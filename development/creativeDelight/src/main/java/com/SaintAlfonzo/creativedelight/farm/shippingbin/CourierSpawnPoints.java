package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;

/**
 * Where the Courier Spawn Points of one dimension are (saved data {@code farmingforblockheads_courier_spawns}), so the bin
 * finds the nearest one in {@code courierSpawnSearchRadius} without scanning blocks. The block adds and removes itself
 * ({@link CourierSpawnBlock}); a listed position whose block is gone is dropped when looked at.
 */
public final class CourierSpawnPoints extends SavedData {
	private static final String NAME = "farmingforblockheads_courier_spawns";
	private final LongSet points = new LongOpenHashSet();

	public static CourierSpawnPoints get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(CourierSpawnPoints::load, CourierSpawnPoints::new, NAME);
	}

	private static CourierSpawnPoints load(CompoundTag tag) {
		CourierSpawnPoints data = new CourierSpawnPoints();
		for (long point : tag.getLongArray("Points")) data.points.add(point);
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.putLongArray("Points", points.toLongArray());
		return tag;
	}

	void add(BlockPos pos) {
		if (points.add(pos.asLong())) setDirty();
	}

	void remove(BlockPos pos) {
		if (points.remove(pos.asLong())) setDirty();
	}

	/** The nearest listed spawn point within {@code radius} (each axis) of {@code center}, or null. */
	@Nullable
	BlockPos nearest(ServerLevel level, BlockPos center, int radius) {
		if (radius <= 0 || points.isEmpty()) return null;
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		LongSet stale = new LongOpenHashSet();
		for (long packed : points) {
			BlockPos pos = BlockPos.of(packed);
			if (Math.abs(pos.getX() - center.getX()) > radius || Math.abs(pos.getY() - center.getY()) > radius
					|| Math.abs(pos.getZ() - center.getZ()) > radius) continue;
			if (level.isLoaded(pos) && !level.getBlockState(pos).is(ShippingBin.COURIER_SPAWN.get())) {
				stale.add(packed);
				continue;
			}
			double distance = pos.distSqr(center);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = pos;
			}
		}
		if (!stale.isEmpty()) {
			points.removeAll(stale);
			setDirty();
		}
		return best;
	}
}
