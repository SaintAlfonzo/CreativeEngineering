package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Dropped items and XP orbs stick in every web (plan_passthrough.md 4.2): every tick an item or orb's path crosses a
 * cell of a tier with {@code items = stick}, it rolls {@code itemStickChance}; a hit stops it at a random point along
 * the path inside the webs, a miss on a tick that would carry it out stops it just inside the exit, so every item that
 * enters a web stays in it ({@link PassMath#stickAlong}). One that starts a tick in a web with no motion sticks where it
 * is. The path is the item's position from the start of the tick to now, clipped per cell, so a fast item cannot cross
 * a web unseen.
 *
 * <p><b>Stuck:</b> NoGravity on (vanilla gravity and ItemPhysic's {@code ItemPhysicServer.updatePre} both read the
 * real {@code isNoGravity()}, phase 0 brief 4 and implication 20), motion zero and the position held every tick
 * (fluid pushes and orb homing cannot move it), until the web cell is no longer a stick web; then NoGravity goes back
 * to what it was and the item falls. Stuck items still age and despawn on their normal timer. The state is saved with
 * the entity ({@link PassSave}), so stuck items stay stuck after a reload. A stuck item's own move is skipped
 * ({@link #heldMove}, from the HEAD of {@code Entity#move}): per tick it costs one block-state read of its cell.
 *
 * <p><b>Sides:</b> the server decides (the random stop point is server only, plan 1.4) and sets the synced NoGravity
 * flag with an impulse, so the client gets the position at once. The client only zeroes the motion of an item inside a
 * stick web so it does not run ahead before that arrives.
 *
 * <p>Runs from the TAIL of {@code Entity#checkInsideBlocks} ({@link PassThrough#afterInsideBlocks}), which
 * {@code ItemEntity#tick} and {@code ExperienceOrb#tick} reach through {@code move} every tick while not on the ground.
 */
public final class PassItems {

    private PassItems() {}

    /** How far inside the cell faces a stop point stays. */
    private static final double INSET = 0.01;
    /** A path longer than this (blocks) is a teleport, not a move: only its end counts. */
    private static final double MAX_PATH = 8.0;
    private static final int MAX_CELLS = 64;

    private static final ThreadLocal<double[][]> CELLS = ThreadLocal.withInitial(() -> new double[MAX_CELLS][3]);

    public static void afterInsideBlocks(Entity e) {
        Level level = e.level;
        boolean client = level.isClientSide;
        PassValues values = PassValues.of(client);
        PassState st = PassThrough.state(e, false);
        if (st != null && st.itemStuck) {
            hold(e, st, values);
            return;
        }
        if (!values.enabled || e.noPhysics || !anyStickTier(values)) return;

        double tx = e.getX(), ty = e.getY(), tz = e.getZ();
        double fx = e.xo, fy = e.yo, fz = e.zo;
        double dx = tx - fx, dy = ty - fy, dz = tz - fz;
        if (dx * dx + dy * dy + dz * dz > MAX_PATH * MAX_PATH) {
            fx = tx;
            fy = ty;
            fz = tz;
        }

        if (client) {
            // The server picks the point. Until its NoGravity and position arrive, keep the client copy from running
            // ahead; once NoGravity is on, hold it where the tick started (orb homing would drag it otherwise) so the
            // server's position updates are what moves it.
            BlockPos at = new BlockPos(tx, ty, tz);
            if (stickCell(e, level, at, level.getBlockState(at), values) != null) {
                if (e.isNoGravity() && (e.xo != tx || e.yo != ty || e.zo != tz)) e.setPos(e.xo, e.yo, e.zo);
                if (e.getDeltaMovement().lengthSqr() > 0.0) e.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        double[] p = pathStop(e, level, values, fx, fy, fz, tx, ty, tz);
        if (p == null) return;
        stick(e, PassThrough.state(e, true), p);
    }

    /**
     * One server tick's stick test for an item or orb moving from {@code f} to {@code t}: the stick cells its path box
     * covers, one roll, and the stop point along the path ({@link PassMath#stickAlong}), or null (no web on the path,
     * or no stop this tick). Changes nothing; also the item case of {@code /cdevbench passthrough} (Creative Devtools).
     */
    public static double[] pathStop(Entity e, Level level, PassValues values, double fx, double fy, double fz,
                                    double tx, double ty, double tz) {
        int x0 = Mth.floor(Math.min(fx, tx)), x1 = Mth.floor(Math.max(fx, tx));
        int y0 = Mth.floor(Math.min(fy, ty)), y1 = Mth.floor(Math.max(fy, ty));
        int z0 = Mth.floor(Math.min(fz, tz)), z1 = Mth.floor(Math.max(fz, tz));
        double[][] cells = CELLS.get();
        int n = 0;
        double chance = 0.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x <= x1 && n < MAX_CELLS; x++) {
            for (int y = y0; y <= y1 && n < MAX_CELLS; y++) {
                for (int z = z0; z <= z1 && n < MAX_CELLS; z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    PassTier.Values v = stickCell(e, level, pos, state, values);
                    if (v == null) continue;
                    cells[n][0] = x;
                    cells[n][1] = y;
                    cells[n][2] = z;
                    n++;
                    chance = Math.max(chance, v.itemStickChance());
                }
            }
        }
        if (n == 0) return null;
        double roll = level.random.nextDouble() < chance ? level.random.nextDouble() : -1.0;
        return PassMath.stickAlong(new double[]{fx, fy, fz}, new double[]{tx, ty, tz}, cells, n, roll, INSET);
    }

    /** The tier values when {@code state} at {@code pos} is a stick web for this entity, else null. */
    private static PassTier.Values stickCell(Entity e, Level level, BlockPos pos, BlockState state, PassValues values) {
        PassTier tier = PassThrough.tierOf(state);
        if (tier == null) return null;
        PassTier.Values v = values.tier(tier);
        if (v.items() != PassTier.ItemRule.STICK) return null;
        if (PassThrough.exempt(e, tier, v) || !PassThrough.grown(level, pos, state, tier, v)) return null;
        return v;
    }

    private static boolean anyStickTier(PassValues values) {
        for (PassTier t : PassTier.values()) {
            if (values.tier(t).items() == PassTier.ItemRule.STICK) return true;
        }
        return false;
    }

    private static void stick(Entity e, PassState st, double[] p) {
        st.itemStuck = true;
        st.stuckState = null;
        st.stuckCell = BlockPos.asLong(Mth.floor(p[0]), Mth.floor(p[1]), Mth.floor(p[2]));
        st.stuckX = p[0];
        st.stuckY = p[1];
        st.stuckZ = p[2];
        st.priorNoGravity = e.isNoGravity();
        e.setNoGravity(true);
        e.setPos(p[0], p[1], p[2]);
        e.setDeltaMovement(Vec3.ZERO);
        e.hasImpulse = true;
    }

    /** The state holds a stuck item or orb ({@code mixin/InsideBlocksMixin} reads it at the HEAD of every move). */
    public static boolean stuckState(PassState st) {
        return st.itemStuck;
    }

    /**
     * HEAD of {@code Entity#move} for a stuck item or orb (scratch fixes 2026-11-15, SQ-PTH-28 PT04: the zero-length
     * moves of 500 held items cost about 3 ms a tick in entity collision queries). Checks the web and holds the item;
     * true = skip the move (still stuck), false = it was let go and moves as usual this tick.
     */
    public static boolean heldMove(Entity e, PassState st) {
        hold(e, st, PassValues.of(e.level.isClientSide));
        return st.itemStuck;
    }

    /**
     * A stuck item: held while its cell is a stick web, let go otherwise. Server only holds state. The full web test
     * runs only when the cell's block state or the config snapshot changed since the last one.
     */
    private static void hold(Entity e, PassState st, PassValues values) {
        Level level = e.level;
        BlockPos cell = BlockPos.of(st.stuckCell);
        // the held point is inside the cell, so the cell is loaded whenever the item ticks; check only when moved off
        if (e.blockPosition().asLong() != st.stuckCell && !level.isLoaded(cell)) return;
        BlockState state = level.getBlockState(cell);
        if (state != st.stuckState || values != st.stuckValues) {
            if (!values.enabled || stickCell(e, level, cell, state, values) == null) {
                st.stuckState = null;
                st.stuckValues = null;
                release(e, st);
                return;
            }
            st.stuckState = state;
            st.stuckValues = values;
        }
        if (e.getX() != st.stuckX || e.getY() != st.stuckY || e.getZ() != st.stuckZ) {
            e.setPos(st.stuckX, st.stuckY, st.stuckZ);
        }
        if (e.getDeltaMovement().lengthSqr() > 0.0) e.setDeltaMovement(Vec3.ZERO);
        if (!e.isNoGravity()) e.setNoGravity(true);
    }

    private static void release(Entity e, PassState st) {
        st.itemStuck = false;
        e.setNoGravity(st.priorNoGravity);
        e.hasImpulse = true;
    }

    /** Headless and WTHIT use: the item is held in a web on this side. */
    public static boolean stuck(Entity e) {
        PassState st = PassThrough.state(e, false);
        return st != null && st.itemStuck;
    }
}
