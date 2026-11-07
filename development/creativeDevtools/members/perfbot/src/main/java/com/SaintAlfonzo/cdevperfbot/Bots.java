package com.SaintAlfonzo.cdevperfbot;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The bots and their straight-line paths (one teleport step per tick, at a fixed height). */
final class Bots {

    private final Map<String, BotPlayer> bots = new LinkedHashMap<>();
    private final List<Path> paths = new ArrayList<>();
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
        server.getPlayerList().placeNewPlayer(new BotConnection(), bot);
        bot.setInvulnerable(true);
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

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
