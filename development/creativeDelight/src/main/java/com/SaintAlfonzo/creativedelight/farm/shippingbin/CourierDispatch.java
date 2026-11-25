package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Where a courier comes from and goes to (rulings D5 to D5c, D9). {@code walk_in_out}: the nearest Courier Spawn Point
 * within {@code courierSpawnSearchRadius}, else a walkable point {@code courierWalkDistance} from the bin, each only when a
 * path to the bin exists; else it appears beside the bin as in {@code at_bin}. {@code walk_out} and {@code at_bin}: beside
 * the bin. On the way out ({@link #exitFor}): back to its spawn point when that is still there and reachable, else the
 * configured distance, else nowhere (it drinks where it stands).
 */
final class CourierDispatch {
	private static final int DIRECTIONS = 8;

	private CourierDispatch() {
	}

	/** Places a courier for the bin's crate; returns it (already in the world). */
	static CourierEntity dispatch(ServerLevel level, BlockPos bin) {
		String mode = CDConfig.SHIPPING_BIN.courierMode();
		long deadline = level.getGameTime() + CDConfig.SHIPPING_BIN.courierWalkTimeoutTicks();
		CourierEntity courier = ShippingBin.COURIER.get().create(level);
		if (courier == null) throw new IllegalStateException("courier entity type gave no entity");
		name(level, courier);
		if (mode.equals("walk_in_out")) {
			courier.setup(bin, null, CourierEntity.PHASE_IN, deadline, false);
			BlockPos spawn = CourierSpawnPoints.get(level).nearest(level, bin, CDConfig.SHIPPING_BIN.courierSpawnSearchRadius());
			if (spawn != null && tryStart(level, courier, bin, spawn.above(), spawn, deadline)) return courier;
			BlockPos start = walkablePointAround(level, courier, bin, null);
			if (start != null) {
				// walkablePointAround checked the path from start to the bin already
				courier.setup(bin, null, CourierEntity.PHASE_IN, deadline, false);
				return add(level, courier, Vec3.atBottomCenterOf(start), false);
			}
		}
		// beside the bin: picks up at once (walk_out walks off afterwards, at_bin drinks there)
		courier.setup(bin, null, CourierEntity.PHASE_PICKUP, deadline, !mode.equals("walk_out"));
		return add(level, courier, besideBin(level, bin), true);
	}

	private static boolean tryStart(ServerLevel level, CourierEntity courier, BlockPos bin, BlockPos start, @Nullable BlockPos origin, long deadline) {
		if (!standable(level, start)) return false;
		courier.setup(bin, origin, CourierEntity.PHASE_IN, deadline, false);
		courier.moveTo(Vec3.atBottomCenterOf(start));
		if (courier.pathToBin() == null) return false;
		add(level, courier, Vec3.atBottomCenterOf(start), false);
		return true;
	}

	private static CourierEntity add(ServerLevel level, CourierEntity courier, Vec3 at, boolean teleported) {
		courier.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360.0F, 0.0F);
		courier.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(at)), MobSpawnType.EVENT, null, null);
		level.addFreshEntity(courier);
		if (teleported) CourierEntity.teleportEffects(level, at);
		return courier;
	}

	private static void name(ServerLevel level, CourierEntity courier) {
		List<? extends String> names = CDConfig.SHIPPING_BIN.courierNames();
		if (!names.isEmpty()) courier.setCustomName(Component.literal(names.get(level.random.nextInt(names.size()))));
	}

	/** Where the courier goes after taking the crate (D5c), null to drink in place. */
	@Nullable
	static BlockPos exitFor(ServerLevel level, CourierEntity courier, @Nullable BlockPos origin) {
		if (origin != null && CDConfig.SHIPPING_BIN.courierSpawnSearchRadius() > 0 && level.isLoaded(origin)
				&& level.getBlockState(origin).is(ShippingBin.COURIER_SPAWN.get())) {
			Path path = courier.getNavigation().createPath(origin.above(), 1);
			if (path != null && path.canReach()) return origin.above();
		}
		return walkablePointAround(level, courier, courier.bin(), courier.blockPosition());
	}

	/**
	 * A standable point {@code courierWalkDistance} from the bin, trying {@value #DIRECTIONS} directions from a random
	 * start. {@code from == null}: a point the courier, placed there, can walk to the bin from; else a point it can walk to
	 * from where it stands.
	 */
	@Nullable
	private static BlockPos walkablePointAround(ServerLevel level, CourierEntity courier, BlockPos bin, @Nullable BlockPos from) {
		int distance = CDConfig.SHIPPING_BIN.courierWalkDistance();
		float start = level.random.nextFloat() * Mth.TWO_PI;
		Vec3 home = courier.position();
		for (int i = 0; i < DIRECTIONS; i++) {
			float angle = start + i * Mth.TWO_PI / DIRECTIONS;
			int x = bin.getX() + Math.round(Mth.cos(angle) * distance);
			int z = bin.getZ() + Math.round(Mth.sin(angle) * distance);
			BlockPos point = groundNear(level, x, bin.getY(), z);
			if (point == null) continue;
			if (from == null) {
				courier.moveTo(Vec3.atBottomCenterOf(point));
				if (courier.pathToBin() != null) return point;
			} else {
				Path path = courier.getNavigation().createPath(point, 1);
				if (path != null && path.canReach()) return point;
			}
		}
		courier.moveTo(home);
		return null;
	}

	@Nullable
	private static BlockPos groundNear(ServerLevel level, int x, int y, int z) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy <= 6; dy++) {
			for (int sign : new int[]{1, -1}) {
				if (dy == 0 && sign < 0) continue;
				pos.set(x, y + dy * sign, z);
				if (level.isLoaded(pos) && standable(level, pos)) return pos.immutable();
			}
		}
		return null;
	}

	static boolean standable(ServerLevel level, BlockPos feet) {
		BlockPos below = feet.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
				&& level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
				&& level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
				&& level.getFluidState(feet).isEmpty();
	}

	/** A free spot next to the bin (sides first, one up or down allowed), else on top of it. */
	static Vec3 besideBin(ServerLevel level, BlockPos bin) {
		for (int dy : new int[]{0, 1, -1}) {
			for (Direction side : Direction.Plane.HORIZONTAL) {
				BlockPos feet = bin.relative(side).above(dy);
				if (standable(level, feet)) return Vec3.atBottomCenterOf(feet);
			}
		}
		return new Vec3(bin.getX() + 0.5D, bin.getY() + 1.0D, bin.getZ() + 0.5D);
	}
}
