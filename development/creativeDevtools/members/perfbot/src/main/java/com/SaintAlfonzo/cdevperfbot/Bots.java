package com.SaintAlfonzo.cdevperfbot;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The bots, their straight-line paths (one teleport step per tick, at a fixed height), their walks (real movement:
 * forward input and yaw each tick, physics by the server) and their timed item uses. Per-name flags (vulnerable,
 * pickup) survive a respawn. Every bot is exempt from Loading Protection: see {@link #onLogin}.
 */
final class Bots {

	private static final Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

	private final Map<String, BotPlayer> bots = new LinkedHashMap<>();
	private final List<Path> paths = new ArrayList<>();
	private final Map<String, Walk> walks = new HashMap<>();
	private final Map<String, Integer> releaseAt = new HashMap<>();
	private final Set<String> vulnerable = new HashSet<>();
	private final Set<String> noPickup = new HashSet<>();
	/** Where the bot being logged in will be teleported (read by {@link #onLogin}). */
	private Vec3 pendingSpawn;
	private TickLog log;

	void setLog(TickLog log) {
		this.log = log;
	}

	BotPlayer spawn(MinecraftServer server, String name, ServerLevel level, double x, double y, double z, GameType mode) {
		BotPlayer old = bots.get(name);
		if (old != null && !old.isRemoved()) {
			old.teleportTo(level, x, y, z, 0f, 0f);
			old.setGameMode(mode);
			old.getLevel().getChunkSource().move(old);
			return old;
		}
		UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		BotPlayer bot = new BotPlayer(server, level, new GameProfile(id, name), this);
		pendingSpawn = new Vec3(x, y, z);
		try {
			server.getPlayerList().placeNewPlayer(new BotConnection(), bot);
		} finally {
			pendingSpawn = null;
		}
		bot.setInvulnerable(!vulnerable.contains(name));
		bot.setHealth(bot.getMaxHealth());
		bot.setGameMode(mode);
		bot.teleportTo(level, x, y, z, 0f, 0f);
		// the login put its chunk tickets at its saved or spawn position: move them now (see BotPlayer)
		bot.getLevel().getChunkSource().move(bot);
		bots.put(name, bot);
		return bot;
	}

	void respawn(BotPlayer dead) {
		String name = dead.getGameProfile().getName();
		if (bots.get(name) != dead) return;
		MinecraftServer server = dead.getServer();
		ServerLevel level = dead.getLevel();
		double x = dead.getX(), y = dead.getY(), z = dead.getZ();
		GameType mode = dead.gameMode.getGameModeForPlayer();
		remove(name);
		BotPlayer bot = spawn(server, name, level, x, Math.max(y, level.getMinBuildHeight() + 2), z, mode);
		bot.setHealth(bot.getMaxHealth());
		if (log != null) log.mark("bot_respawned:" + name);
	}

	boolean remove(String name) {
		BotPlayer bot = bots.remove(name);
		if (bot == null) return false;
		paths.removeIf(p -> p.bot == bot);
		walks.remove(name);
		releaseAt.remove(name);
		if (!bot.isRemoved()) bot.connection.disconnect(Component.literal("cdevperfbot remove"));
		return true;
	}

	int removeAll() {
		List<String> names = new ArrayList<>(bots.keySet());
		names.forEach(this::remove);
		return names.size();
	}

	BotPlayer get(String name) {
		BotPlayer bot = bots.get(name);
		return bot == null || bot.isRemoved() ? null : bot;
	}

	Map<String, BotPlayer> all() {
		return bots;
	}

	/** Moves the bot toward (toX, toZ) at {@code speed} blocks per tick, at height y; replaces its earlier path. */
	int path(BotPlayer bot, double toX, double y, double toZ, double speed) {
		paths.removeIf(p -> p.bot == bot);
		double dx = toX - bot.getX(), dz = toZ - bot.getZ();
		double len = Math.sqrt(dx * dx + dz * dz);
		int ticks = (int) Math.ceil(len / speed);
		if (ticks <= 0) return 0;
		paths.add(new Path(bot, bot.getX(), y, bot.getZ(), dx / ticks, dz / ticks, ticks));
		return ticks;
	}

	boolean stopPath(BotPlayer bot) {
		return paths.removeIf(p -> p.bot == bot);
	}

	// ---- flags

	void setVulnerable(BotPlayer bot, boolean on) {
		String name = bot.getGameProfile().getName();
		if (on) vulnerable.add(name);
		else vulnerable.remove(name);
		bot.setInvulnerable(!on);
	}

	void setPickup(BotPlayer bot, boolean on) {
		String name = bot.getGameProfile().getName();
		if (on) noPickup.remove(name);
		else noPickup.add(name);
	}

	boolean pickup(String name) {
		return !noPickup.contains(name);
	}

	private BotPlayer asBot(Player p) {
		return p instanceof BotPlayer b && bots.get(b.getGameProfile().getName()) == b ? b : null;
	}

	/**
	 * Loading Protection exemption. That mod protects a player who just logged in (no hits taken or dealt, no mob
	 * targeting) until the player moves 0.1 blocks from the login position or protectionDuration (150 s in the pack)
	 * runs out. A bot logs in at its saved position and is then teleported; when both are the same spot (a bot
	 * respawned or spawned again where it stood) it never "moves" and stays protected (gotchas, SQ-HH-424). This runs
	 * before Loading Protection's own login listener and puts the bot one block above its target, so the teleport in
	 * {@link #spawn} is a move and the protection ends at the end of the next server tick. Nothing reads the mod.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (pendingSpawn != null && event.getEntity() instanceof BotPlayer bot) {
			bot.setPos(pendingSpawn.x, pendingSpawn.y + 1.0, pendingSpawn.z);
		}
	}

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		BotPlayer bot = asBot(event.getEntity());
		if (bot != null && noPickup.contains(bot.getGameProfile().getName())) event.setCanceled(true);
	}

	@SubscribeEvent
	public void onXpPickup(PlayerXpEvent.PickupXp event) {
		BotPlayer bot = asBot(event.getEntity());
		if (bot != null && noPickup.contains(bot.getGameProfile().getName())) event.setCanceled(true);
	}

	// ---- timed item use (eat, drink, bows): the bot's own LivingEntity tick counts the use down

	/** Release after {@code ticks} ticks of use (0 = hold until the item finishes itself). */
	void holdUse(BotPlayer bot, int ticks) {
		String name = bot.getGameProfile().getName();
		if (ticks > 0) releaseAt.put(name, bot.getServer().getTickCount() + ticks);
		else releaseAt.remove(name);
	}

	@SubscribeEvent
	public void onUseFinish(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof BotPlayer bot) || asBot(bot) == null) return;
		releaseAt.remove(bot.getGameProfile().getName());
		LOGGER.info(new BotLine("useitem").kv("bot", bot.getGameProfile().getName()).kv("end", "finish")
				.kv("item", ForgeRegistries.ITEMS.getKey(event.getItem().getItem())).kv("result", ForgeRegistries.ITEMS.getKey(event.getResultStack().getItem()))
				.kv("tick", bot.getServer().getTickCount()).kv("food", bot.getFoodData().getFoodLevel())
				.kv("sat", bot.getFoodData().getSaturationLevel()).kv("health", bot.getHealth()).toString());
		if (log != null) log.mark("use_done:" + bot.getGameProfile().getName());
	}

	@SubscribeEvent
	public void onUseStop(LivingEntityUseItemEvent.Stop event) {
		if (!(event.getEntity() instanceof BotPlayer bot) || asBot(bot) == null) return;
		LOGGER.info(new BotLine("useitem").kv("bot", bot.getGameProfile().getName()).kv("end", "released")
				.kv("item", ForgeRegistries.ITEMS.getKey(event.getItem().getItem())).kv("left", event.getDuration())
				.kv("tick", bot.getServer().getTickCount()).toString());
	}

	private void stepUses(MinecraftServer server) {
		if (releaseAt.isEmpty()) return;
		int now = server.getTickCount();
		for (Iterator<Map.Entry<String, Integer>> it = releaseAt.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Integer> e = it.next();
			if (now < e.getValue()) continue;
			it.remove();
			BotPlayer bot = get(e.getKey());
			if (bot != null && bot.isUsingItem()) bot.releaseUsingItem();
		}
	}

	// ---- walks

	/** Starts a walk to (x, z) with real movement; replaces the bot's teleport path and earlier walk. */
	void walk(BotPlayer bot, double x, double z, boolean sprint, int timeout) {
		paths.removeIf(p -> p.bot == bot);
		walks.put(bot.getGameProfile().getName(), new Walk(bot, x, z, sprint, timeout, bot.getServer().getTickCount(),
				bot.position()));
	}

	boolean stopWalk(BotPlayer bot, String how) {
		Walk w = walks.remove(bot.getGameProfile().getName());
		if (w == null) return false;
		endWalk(w, how);
		return true;
	}

	private void endWalk(Walk w, String how) {
		BotPlayer bot = w.bot;
		bot.zza = 0;
		bot.setSprinting(false);
		bot.setJumping(false);
		int ticks = bot.getServer().getTickCount() - w.startTick;
		double moved = Math.sqrt(sq(bot.getX() - w.from.x) + sq(bot.getZ() - w.from.z));
		LOGGER.info(new BotLine("moveto").kv("bot", bot.getGameProfile().getName()).kv("end", how).kv("ticks", ticks)
				.kv("dist", moved).kv("speed_bpt", ticks > 0 ? moved / ticks : 0).kv("speed_bps", ticks > 0 ? moved * 20 / ticks : 0)
				.kv("x", bot.getX()).kv("y", bot.getY()).kv("z", bot.getZ()).kv("left", Math.sqrt(sq(w.x - bot.getX()) + sq(w.z - bot.getZ())))
				.toString());
		if (log != null) log.mark("walk_done:" + bot.getGameProfile().getName() + ":" + how);
	}

	private static double sq(double d) {
		return d * d;
	}

	/** Sets next tick's input: yaw toward the target, full forward, jump when blocked on the ground. */
	private void stepWalks(MinecraftServer server) {
		if (walks.isEmpty()) return;
		int now = server.getTickCount();
		for (Iterator<Walk> it = walks.values().iterator(); it.hasNext(); ) {
			Walk w = it.next();
			BotPlayer bot = w.bot;
			if (bot.isRemoved()) {
				it.remove();
				continue;
			}
			double dx = w.x - bot.getX(), dz = w.z - bot.getZ();
			double left = Math.sqrt(dx * dx + dz * dz);
			String end = null;
			if (left < Walk.ARRIVE) end = "reached";
			else if (now - w.startTick >= w.timeout) end = "timeout";
			else if (now - w.lastProgressTick > Walk.STUCK_TICKS) end = "stuck";
			if (end != null) {
				it.remove();
				endWalk(w, end);
				continue;
			}
			if (left < w.bestLeft - 0.05) {
				w.bestLeft = left;
				w.lastProgressTick = now;
			}
			float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
			bot.setYRot(yaw);
			bot.setYHeadRot(yaw);
			bot.setYBodyRot(yaw);
			// slow down for the last block so a fast walk does not overshoot the target
			bot.zza = (float) Math.min(1.0, Math.max(0.2, left));
			bot.setSprinting(w.sprint && left > 1.5);
			bot.setJumping(bot.horizontalCollision && bot.isOnGround());
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			stepWalks(server);
			stepUses(server);
		}
		stepPaths();
		// the chunk tracker a client's movement packets would drive (BotPlayer explains why not from its tick)
		for (BotPlayer bot : bots.values()) {
			if (!bot.isRemoved()) bot.getLevel().getChunkSource().move(bot);
		}
	}

	private void stepPaths() {
		for (Iterator<Path> it = paths.iterator(); it.hasNext(); ) {
			Path p = it.next();
			if (p.bot.isRemoved()) {
				it.remove();
				continue;
			}
			p.x += p.dx;
			p.z += p.dz;
			p.bot.teleportTo(p.x, p.y, p.z);
			if (--p.left <= 0) {
				it.remove();
				if (log != null) log.mark("path_done:" + p.bot.getGameProfile().getName());
			}
		}
	}

	private static final class Walk {
		static final double ARRIVE = 0.3;
		static final int STUCK_TICKS = 60;
		final BotPlayer bot;
		final double x, z;
		final boolean sprint;
		final int timeout, startTick;
		final Vec3 from;
		double bestLeft = Double.MAX_VALUE;
		int lastProgressTick;

		Walk(BotPlayer bot, double x, double z, boolean sprint, int timeout, int startTick, Vec3 from) {
			this.bot = bot;
			this.x = x;
			this.z = z;
			this.sprint = sprint;
			this.timeout = timeout;
			this.startTick = startTick;
			this.from = from;
			this.lastProgressTick = startTick;
		}
	}

	private static final class Path {
		final BotPlayer bot;
		double x, z;
		final double y, dx, dz;
		int left;

		Path(BotPlayer bot, double x, double y, double z, double dx, double dz, int left) {
			this.bot = bot;
			this.x = x;
			this.y = y;
			this.z = z;
			this.dx = dx;
			this.dz = dz;
			this.left = left;
		}
	}
}
