package com.SaintAlfonzo.creativecompat.surface;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Projectiles (and every other non-living, non-item entity) in pass-through webs (plan_passthrough.md 5). Only tiers
 * with {@code projectiles = true} count (the cobweb tier by default). Every group but {@code exempt} takes the web
 * slowdown each tick inside: the whole velocity x walk, downward also x fall, dense values when dense. Then, by
 * {@link PassGroup} (entity-type tags {@code creativecompat:passthrough/projectile/<group>}, else the base-class
 * default):
 * <ul>
 *   <li>{@code break}: in a surrounded web, moved to that cell's center and its impact run as a block hit there
 *       (Forge's impact event first), then discarded on the server; stalled in any web (below
 *       {@link #BREAK_STALL_SPEED} for {@code breakStallTicks} ticks in a row, plan_post_scratch_1115b.md B) the same
 *       impact runs where it stopped (an ender pearl teleports there);</li>
 *   <li>{@code settle}: in a surrounded web, no horizontal motion and on the cell's center line; the dense fall drag
 *       makes it sink slowly until it lands below the webs and breaks there;</li>
 *   <li>{@code snag}: in any web, held at the center of the web cell; a fishing bobber or feather is reeled in as
 *       usual, a pickarang or chakram ({@link PassSnagItem}) is picked up by touch and drops as an item when the web
 *       breaks; a bobber just falls again;</li>
 *   <li>{@code linger}: in a surrounded web, held where it entered until the web breaks or its flight ends
 *       (fireworks explode or vanish on their own timer);</li>
 *   <li>{@code hang}: in a surrounded web an arrow goes into vanilla's stuck state with the web as its block
 *       ({@link PassArrow#creativecompat$hangInWeb}); its block-hit code never runs;</li>
 *   <li>{@code slow}: nothing more; {@code exempt}: nothing at all.</li>
 * </ul>
 *
 * <p><b>Hooks</b> (phase 0 brief 5.1, implication 18): the TAIL of {@code Entity#checkInsideBlocks} for arrows,
 * throwables, fireballs and bobbers ({@link #afterInsideBlocks}); fireworks restore their motion after the move, so
 * they only record the result there and act at their tick end ({@link #fireworkTail}); Quark pickarangs never call
 * {@code checkInsideBlocks}, so their tick end runs the whole check ({@link #tick}); the Botania chakram restores its
 * motion after {@code super.tick()}, so its tick end re-holds a snag ({@link #snagTail}). Both sides run it, like the
 * projectiles' own movement; only the server discards, spawns items or gives them.
 */
public final class PassProjectiles {

	private PassProjectiles() {}

	// ---- groups ----

	private static final class Tags {
		static final Map<PassGroup, TagKey<EntityType<?>>> GROUP = new EnumMap<>(PassGroup.class);

		static {
			for (PassGroup g : PassGroup.values()) {
				GROUP.put(g, TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(CreativeCompat.NS, g.tagPath())));
			}
		}
	}

	/** Entity-type tag of a group ({@code creativecompat:passthrough/projectile/<group>}). */
	public static TagKey<EntityType<?>> groupTag(PassGroup g) {
		return Tags.GROUP.get(g);
	}

	private static final Map<EntityType<?>, PassGroup> GROUP_OF = new ConcurrentHashMap<>();

	/** The entity's group: the exempt tags, then the group tags in {@link PassGroup} order, then the class default. */
	public static PassGroup groupOf(Entity e) {
		EntityType<?> type = e.getType();
		PassGroup g = GROUP_OF.get(type);
		if (g == null) {
			g = findGroup(type, e.getClass());
			GROUP_OF.put(type, g);
		}
		return g;
	}

	private static PassGroup findGroup(EntityType<?> type, Class<?> cls) {
		if (type.is(PassThrough.exemptAllTag())) return PassGroup.EXEMPT;
		for (PassGroup g : PassGroup.values()) {
			if (type.is(Tags.GROUP.get(g))) return g;
		}
		return PassGroup.byClassChain(PassGroup.chainOf(cls));
	}

	/** Tag reload (from {@link PassThrough#clearCache}). */
	static void clearCache() {
		GROUP_OF.clear();
	}

	// ---- web cells of a result ----

	/** The counted cells of tiers with {@code projectiles = true}. */
	static final class Web {
		boolean any, surrounded;
		double walk, fall;
		/** The web cell to snag in: the entity's own cell when it is one, else the first. */
		long cell;
		long surroundedCell;

		void reset() {
			any = surrounded = false;
			walk = fall = 1.0;
			cell = surroundedCell = 0L;
		}
	}

	private static final ThreadLocal<Web> WEB = ThreadLocal.withInitial(Web::new);

	static Web web(Entity e, PassResult r, PassValues values, Web w) {
		w.reset();
		long own = e.blockPosition().asLong();
		boolean ownFound = false;
		for (int i = 0; i < r.cells(); i++) {
			PassRule.Cell c = r.cellRule(i);
			if (!values.tier(c.tier()).projectiles()) continue;
			long pos = r.cellPos(i);
			if (!w.any || (!ownFound && pos == own)) {
				w.cell = pos;
				ownFound = pos == own;
			}
			w.any = true;
			w.walk = Math.min(w.walk, c.walk());
			w.fall = Math.min(w.fall, c.fall());
			if (c.surrounded() && !w.surrounded) {
				w.surrounded = true;
				w.surroundedCell = pos;
			}
		}
		return w;
	}

	/** {@code pos} is still a block of a projectile tier (the snag or linger cell). */
	private static boolean webAt(Level level, long pos, PassValues values) {
		BlockPos p = BlockPos.of(pos);
		if (!level.isLoaded(p)) return true;
		PassTier t = PassThrough.tierOf(level.getBlockState(p));
		return t != null && values.tier(t).projectiles();
	}

	// ---- entry points ----

	/** TAIL of {@code Entity#checkInsideBlocks} for a projectile. */
	public static void afterInsideBlocks(Projectile p) {
		if (p instanceof FireworkRocketEntity) {
			prepare(p); // acts at its tick end, after it put its motion back
			return;
		}
		tick(p);
	}

	/** The full check for this tick: evaluate, then act. Quark pickarangs call it from their tick end. */
	public static void tick(Entity p) {
		PassState st = prepare(p);
		if (st != null) act(p, st, PassValues.of(p.level.isClientSide));
	}

	/**
	 * RETURN of {@code FireworkRocketEntity#tick} ({@code mixin/FireworkPassMixin}): the result its move recorded this
	 * tick, applied now that the rocket has put its motion back. Rockets boosting a gliding player are skipped.
	 */
	public static void fireworkTail(FireworkRocketEntity f, boolean attached) {
		if (attached || f.isRemoved()) return;
		PassState st = PassThrough.state(f, false);
		if (st == null) return;
		if (st.resultTick != f.tickCount) st = prepare(f);
		if (st != null) act(f, st, PassValues.of(f.level.isClientSide));
	}

	/** RETURN of the chakram's tick: it put its pre-tick motion back, so hold the snag again. */
	public static void snagTail(Entity p) {
		if (p.isRemoved()) return;
		PassState st = PassThrough.state(p, false);
		if (st != null && st.snagged) holdSnag(p, st);
	}

	/**
	 * Fills the entity's result for this tick; returns its state, or null when it is in no web and has no state (the
	 * common case: nothing allocated).
	 */
	private static PassState prepare(Entity p) {
		PassValues values = PassValues.of(p.level.isClientSide);
		PassState st = PassThrough.state(p, false);
		if (!values.enabled || PassThrough.skipsEntity(p) || groupOf(p) == PassGroup.EXEMPT) {
			if (st != null) {
				st.result.reset();
				st.resultTick = p.tickCount;
			}
			return st;
		}
		if (st == null) {
			PassResult scratch = PassThrough.evaluate(p, values, PassThrough.scratch());
			if (!web(p, scratch, values, WEB.get()).any) return null;
			st = PassThrough.state(p, true);
		}
		PassThrough.evaluate(p, values, st.result);
		st.resultTick = p.tickCount;
		return st;
	}

	private static void act(Entity p, PassState st, PassValues values) {
		if (p.isRemoved() || st.broken) return;
		boolean off = !values.enabled || PassThrough.skipsEntity(p);
		if (st.snagged) {
			if (off || !webAt(p.level, st.snagCell, values)) {
				unsnag(p, st);
				return;
			}
			holdSnag(p, st);
			return;
		}
		if (st.lingering) {
			if (off || !webAt(p.level, st.lingerCell, values)) {
				st.lingering = false;
			} else {
				holdLinger(p, st);
				return;
			}
		}
		if (off) return;
		PassGroup g = groupOf(p);
		if (g == PassGroup.EXEMPT) return;
		Web w = web(p, st.result, values, WEB.get());
		if (!w.any) {
			st.stallTicks = 0;
			return;
		}
		if (g == PassGroup.SNAG) {
			st.snagged = true;
			st.snagCell = w.cell;
			holdSnag(p, st);
			return;
		}
		slow(p, w);
		if (g == PassGroup.BREAK && !w.surrounded) {
			// SQ-PTH-11 (plan_post_scratch_1115b.md B): stopped in any web for breakStallTicks ticks, it breaks there
			if (stallBreaks(st, values.breakStallTicks, p.getDeltaMovement().lengthSqr())) {
				breakIn(p, st, w.cell, false);
				return;
			}
		}
		if (!w.surrounded) return;
		switch (g) {
			case BREAK -> breakIn(p, st, w.surroundedCell, true);
			case SETTLE -> settle(p, w.surroundedCell);
			case LINGER -> {
				st.lingering = true;
				st.lingerCell = w.surroundedCell;
				st.lingerX = p.getX();
				st.lingerY = p.getY();
				st.lingerZ = p.getZ();
				holdLinger(p, st);
			}
			case HANG -> {
				if (p instanceof PassArrow a && !a.creativecompat$inGround()) a.creativecompat$hangInWeb();
			}
			default -> { }
		}
	}

	/** A {@code break} projectile slower than this (blocks per tick) in a web counts as stopped. */
	static final double BREAK_STALL_SPEED = 0.05;

	/**
	 * One web tick of a {@code break} projectile: counts the ticks in a row it has been stalled ({@link #stalled}) and
	 * says when that reaches {@code need} (0 = never). Moving again starts the count over.
	 */
	static boolean stallBreaks(PassState st, int need, double speedSqr) {
		if (need <= 0 || !stalled(speedSqr)) {
			st.stallTicks = 0;
			return false;
		}
		return ++st.stallTicks >= need;
	}

	/** Speed (squared, after this tick's web slowdown) at which a {@code break} projectile counts as stalled. */
	static boolean stalled(double speedSqr) {
		return speedSqr < BREAK_STALL_SPEED * BREAK_STALL_SPEED;
	}

	/** Whole velocity x walk, downward also x fall. */
	private static void slow(Entity p, Web w) {
		if (w.walk >= 1.0 && w.fall >= 1.0) return;
		Vec3 v = p.getDeltaMovement();
		double vy = v.y * w.walk;
		if (vy < 0.0) vy *= w.fall;
		p.setDeltaMovement(v.x * w.walk, vy, v.z * w.walk);
	}

	/**
	 * Runs the projectile's impact as a block hit on the web at {@code cell}: at the cell's center (moved there;
	 * surrounded), or where it is ({@code atCentre} false: stalled), then discards it on the server.
	 */
	private static void breakIn(Entity p, PassState st, long cell, boolean atCentre) {
		st.broken = true;
		BlockPos pos = BlockPos.of(cell);
		Vec3 c = atCentre ? Vec3.atCenterOf(pos) : p.position().add(0.0, p.getBbHeight() / 2.0, 0.0);
		Vec3 v = p.getDeltaMovement();
		if (atCentre) p.setPos(c.x, c.y - p.getBbHeight() / 2.0, c.z);
		if (!(p instanceof Projectile proj)) return;
		Direction face = v.lengthSqr() > 1.0E-12 ? Direction.getNearest(-v.x, -v.y, -v.z) : Direction.UP;
		BlockHitResult hit = new BlockHitResult(c, face, pos, false);
		if (!ForgeEventFactory.onProjectileImpact(proj, hit)) {
			// ProjectilePassMixin switched off ([mixins]): the projectile is only removed
			if (proj instanceof PassImpact impact) impact.creativecompat$impact(hit);
		}
		if (!p.level.isClientSide && !p.isRemoved()) p.discard();
	}

	private static void settle(Entity p, long cell) {
		BlockPos pos = BlockPos.of(cell);
		p.setDeltaMovement(0.0, p.getDeltaMovement().y, 0.0);
		double cx = pos.getX() + 0.5, cz = pos.getZ() + 0.5;
		if (p.getX() != cx || p.getZ() != cz) p.setPos(cx, p.getY(), cz);
	}

	private static void holdSnag(Entity p, PassState st) {
		BlockPos pos = BlockPos.of(st.snagCell);
		double x = pos.getX() + 0.5, y = pos.getY() + 0.5 - p.getBbHeight() / 2.0, z = pos.getZ() + 0.5;
		if (p.getX() != x || p.getY() != y || p.getZ() != z) p.setPos(x, y, z);
		if (p.getDeltaMovement().lengthSqr() > 0.0) p.setDeltaMovement(Vec3.ZERO);
		if (p instanceof PassSnagItem s) s.creativecompat$holdTimers();
	}

	/** The snag web is gone: a bobber falls again; a pickarang or chakram drops as its item (server). */
	private static void unsnag(Entity p, PassState st) {
		st.snagged = false;
		if (p instanceof PassSnagItem s && !p.level.isClientSide) {
			ItemStack stack = s.creativecompat$snagStack();
			if (stack != null && !stack.isEmpty()) p.spawnAtLocation(stack.copy(), 0.0F);
			p.discard();
		}
	}

	private static void holdLinger(Entity p, PassState st) {
		if (p.getX() != st.lingerX || p.getY() != st.lingerY || p.getZ() != st.lingerZ) {
			p.setPos(st.lingerX, st.lingerY, st.lingerZ);
		}
		if (p.getDeltaMovement().lengthSqr() > 0.0) p.setDeltaMovement(Vec3.ZERO);
	}

	/**
	 * HEAD of {@code Entity#playerTouch} ({@code mixin/EntityPassMixin}): a snagged pickarang or chakram is picked up
	 * like an arrow (5.1): given whole to the player's inventory, then gone. Server only.
	 */
	public static void playerTouch(Entity e, Player player) {
		if (!(e instanceof PassSnagItem s) || e.level.isClientSide || e.isRemoved()) return;
		PassState st = PassThrough.state(e, false);
		if (st == null || !st.snagged) return;
		ItemStack stack = s.creativecompat$snagStack();
		if (stack == null || stack.isEmpty()) return;
		ItemStack give = stack.copy();
		if (player.getInventory().add(give)) {
			player.take(e, 1);
			e.discard();
		}
	}

	/**
	 * An arrow hanging in a web (stuck, with a projectile-tier block as its block), for pickup-item fixes
	 * ({@code mixin/QuarkTorchArrowPickupMixin}).
	 */
	public static boolean webHung(Entity e) {
		if (!(e instanceof PassArrow a) || !a.creativecompat$inGround()) return false;
		var last = a.creativecompat$lastState();
		if (last == null) return false;
		PassTier t = PassThrough.tierOf(last);
		return t != null && PassValues.of(e.level.isClientSide).tier(t).projectiles();
	}

	/**
	 * Any other non-living entity (minecarts, boats, falling blocks, primed TNT) lost vanilla's web slowdown to the
	 * stuck cancel: it gets the {@code slow} treatment in projectile tiers. No per-entity state.
	 */
	public static void otherEntity(Entity e) {
		PassValues values = PassValues.of(e.level.isClientSide);
		if (!values.enabled || PassThrough.skipsEntity(e)) return;
		PassResult r = PassThrough.evaluate(e, values, PassThrough.scratch());
		if (!r.any()) return;
		Web w = web(e, r, values, WEB.get());
		if (w.any) slow(e, w);
	}
}
