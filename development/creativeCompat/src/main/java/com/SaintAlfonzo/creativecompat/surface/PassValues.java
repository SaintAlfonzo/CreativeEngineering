package com.SaintAlfonzo.creativecompat.surface;

import java.util.EnumMap;
import java.util.Map;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * One snapshot of every {@code [surface.passthrough]} value (plan 1.4). Movement reads it every tick, so it is built
 * once from the toml (on load and on every save) instead of reading each config value per entity per tick.
 *
 * <p>Two copies: {@link #local()} from this side's own toml, and the server's, received through {@link PassSync}.
 * The server and anything simulated on it use the local one; the client uses the server's while connected (player
 * movement runs on the client, so both must agree), its own otherwise. {@link #of(boolean)} picks by side.
 */
public final class PassValues {

	public final boolean enabled;
	public final int arrowLifeTicks;
	public final int arrowLongLifeCap;
	/**
	 * Ticks a {@code break} projectile must stay stalled in a web before its impact runs where it stopped
	 * (plan_post_scratch_1115b.md B); 0 = off.
	 */
	public final int breakStallTicks;
	/**
	 * {@code [surface.passthrough.layers] snowWalkThrough} (plan_post_scratch_1115.md G7.4): snow layers below full have
	 * no collision for living entities, except one in leather boots standing on top and not sneaking ({@link SnowLayers}).
	 */
	public final boolean snowWalkThrough;
	/** {@code [surface.passthrough.layers] snowFullBecomesBlock}: a snow layer that reaches 8 becomes a snow block. */
	public final boolean snowFullBecomesBlock;
	private final PassTier.Values[] tiers;

	public PassValues(boolean enabled, int arrowLifeTicks, int arrowLongLifeCap, Map<PassTier, PassTier.Values> tiers) {
		this(enabled, arrowLifeTicks, arrowLongLifeCap, DEFAULT_STALL_TICKS, true, true, tiers);
	}

	public PassValues(boolean enabled, int arrowLifeTicks, int arrowLongLifeCap, int breakStallTicks,
					  boolean snowWalkThrough, boolean snowFullBecomesBlock, Map<PassTier, PassTier.Values> tiers) {
		this.enabled = enabled;
		this.arrowLifeTicks = arrowLifeTicks;
		this.arrowLongLifeCap = arrowLongLifeCap;
		this.breakStallTicks = breakStallTicks;
		this.snowWalkThrough = snowWalkThrough;
		this.snowFullBecomesBlock = snowFullBecomesBlock;
		this.tiers = new PassTier.Values[PassTier.values().length];
		for (PassTier t : PassTier.values()) {
			PassTier.Values v = tiers.get(t);
			this.tiers[t.ordinal()] = v != null ? v : t.defaults();
		}
	}

	public PassTier.Values tier(PassTier t) {
		return tiers[t.ordinal()];
	}

	public static final int DEFAULT_STALL_TICKS = 3;
	/** The built-in defaults (what an unloaded config gives). */
	public static PassValues defaults() {
		Map<PassTier, PassTier.Values> m = new EnumMap<>(PassTier.class);
		for (PassTier t : PassTier.values()) m.put(t, t.defaults());
		return new PassValues(true, 12000, 128, DEFAULT_STALL_TICKS, true, true, m);
	}

	// ---- which copy ----

	private static volatile PassValues local;
	private static volatile PassValues remote;

	/** The values for code running on this side ({@code clientSide} = the entity's level is a client level). */
	public static PassValues of(boolean clientSide) {
		if (clientSide) {
			PassValues r = remote;
			if (r != null) return r;
		}
		return local();
	}

	/** This side's own toml values (defaults until the config has loaded). */
	public static PassValues local() {
		PassValues l = local;
		if (l == null) {
			if (!CompatConfig.SPEC.isLoaded()) return DEFAULTS;
			l = fromConfig();
			local = l;
		}
		return l;
	}

	private static final PassValues DEFAULTS = defaults();

	static PassValues fromConfig() {
		CompatConfig.Surface.Passthrough c = CompatConfig.SURFACE.passthrough;
		Map<PassTier, PassTier.Values> m = new EnumMap<>(PassTier.class);
		c.tiers.forEach((t, cfg) -> m.put(t, cfg.values()));
		return new PassValues(c.enabled.get(), c.arrowLifeTicks.get(), c.arrowLongLifeCap.get(),
				c.breakStallTicks.get(), c.snowWalkThrough.get(), c.snowFullBecomesBlock.get(), m);
	}

	/** Received from the server (client side), or null on logout. */
	static void setRemote(PassValues values) {
		remote = values;
	}

	/** True while the client uses the server's values. */
	public static boolean usingRemote() {
		return remote != null;
	}

	/**
	 * Rebuild the snapshot now and re-send it to every player, after code changed a config value in memory
	 * ({@code /cdevbench passthrough enabled}, Creative Devtools) without waiting for the file watcher.
	 */
	public static void refresh() {
		local = null;
		PassSync.sendToAllAfterSave();
	}

	/** Loading or saving creativecompat-common.toml: rebuild the snapshot (then {@link PassSync} re-sends it). */
	public static void onConfig(ModConfigEvent event) {
		if (event.getConfig().getSpec() != CompatConfig.SPEC) return;
		local = null;
		if (event instanceof ModConfigEvent.Reloading) {
			PassSync.sendToAllAfterSave();
		}
	}

	// ---- wire format (PassSync) ----

	/**
	 * Wire format version, written first (plan_post_scratch_1115b.md C1: the layout changed with the neighbor model
	 * and the layer rule; 3 with plan_post_scratch_1115.md G7.4: snow and leaf max slow, the snow switches instead of
	 * solidBelowFull and its overrides). A client that reads another version uses the system switched off ({@link #mismatch()})
	 * rather than misreading the table.
	 */
	public static final int FORMAT = 3;

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(FORMAT);
		buf.writeBoolean(enabled);
		buf.writeVarInt(arrowLifeTicks);
		buf.writeVarInt(arrowLongLifeCap);
		buf.writeVarInt(breakStallTicks);
		buf.writeBoolean(snowWalkThrough);
		buf.writeBoolean(snowFullBecomesBlock);
		buf.writeVarInt(tiers.length);
		for (PassTier t : PassTier.values()) {
			buf.writeUtf(t.key());
			PassTier.Values v = tiers[t.ordinal()];
			buf.writeDouble(v.walk());
			buf.writeDouble(v.fall());
			buf.writeEnum(v.growth());
			buf.writeEnum(v.neighbours().canonical());
			buf.writeVarInt(v.denseFrom());
			buf.writeDouble(v.denseWalk());
			buf.writeDouble(v.denseFall());
			buf.writeBoolean(v.denseResetsFall());
			buf.writeEnum(v.surrounded());
			buf.writeVarInt(v.surroundedAt());
			buf.writeVarInt(v.minLayers());
			buf.writeBoolean(v.walkOnlyMoving());
			buf.writeEnum(v.items());
			buf.writeDouble(v.itemStickChance());
			buf.writeBoolean(v.projectiles());
			buf.writeBoolean(v.depthStriderPasses());
			buf.writeDouble(v.snowMaxSlow());
			buf.writeDouble(v.leafMaxSlow());
		}
	}

	/**
	 * Reads what {@link #write} wrote; tiers are matched by key, so an unknown or missing tier keeps its default. Another
	 * format version gives {@link #mismatch()} and leaves the rest of the buffer unread.
	 */
	public static PassValues read(FriendlyByteBuf buf) {
		int format = buf.readVarInt();
		if (format != FORMAT) {
			buf.skipBytes(buf.readableBytes());
			return mismatch();
		}
		boolean enabled = buf.readBoolean();
		int life = buf.readVarInt();
		int cap = buf.readVarInt();
		int stall = buf.readVarInt();
		boolean snowWalk = buf.readBoolean();
		boolean snowBlock = buf.readBoolean();
		int n = buf.readVarInt();
		Map<PassTier, PassTier.Values> m = new EnumMap<>(PassTier.class);
		for (int i = 0; i < n; i++) {
			String key = buf.readUtf();
			PassTier.Values v = new PassTier.Values(buf.readDouble(), buf.readDouble(),
					buf.readEnum(PassTier.Growth.class), buf.readEnum(PassTier.Neighbours.class), buf.readVarInt(),
					buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readEnum(PassTier.Surround.class),
					buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readEnum(PassTier.ItemRule.class),
					buf.readDouble(), buf.readBoolean(), buf.readBoolean(), buf.readDouble(), buf.readDouble());
			for (PassTier t : PassTier.values()) {
				if (t.key().equals(key)) m.put(t, v);
			}
		}
		return new PassValues(enabled, life, cap, stall, snowWalk, snowBlock, m);
	}

	/** What a client uses when the server's values came in another format: the system off (vanilla behavior). */
	public static PassValues mismatch() {
		Map<PassTier, PassTier.Values> m = new EnumMap<>(PassTier.class);
		for (PassTier t : PassTier.values()) m.put(t, t.defaults());
		return new PassValues(false, 1200, 0, 0, false, false, m);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PassValues p)) return false;
		return enabled == p.enabled && arrowLifeTicks == p.arrowLifeTicks && arrowLongLifeCap == p.arrowLongLifeCap
				&& breakStallTicks == p.breakStallTicks && snowWalkThrough == p.snowWalkThrough
				&& snowFullBecomesBlock == p.snowFullBecomesBlock
				&& java.util.Arrays.equals(tiers, p.tiers);
	}

	@Override
	public int hashCode() {
		return java.util.Arrays.hashCode(tiers) * 31 + (enabled ? 1 : 0);
	}
}
