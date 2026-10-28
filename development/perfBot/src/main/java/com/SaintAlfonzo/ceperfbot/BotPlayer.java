package com.SaintAlfonzo.ceperfbot;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * A player with no client. A real client's packets drive {@code doTick()} (movement, base tick) and the chunk
 * tracker; here {@link #tick()} does both. Bots are invulnerable by default; if one dies anyway (/kill, void) it
 * is logged in again at the same spot by {@link Bots}.
 */
final class BotPlayer extends ServerPlayer {

    private final Bots bots;

    BotPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, Bots bots) {
        super(server, level, profile, null);
        this.bots = bots;
    }

    @Override
    public void tick() {
        connection.resetPosition();
        getLevel().getChunkSource().move(this);
        super.tick();
        doTick();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        MinecraftServer server = getServer();
        if (server != null) server.tell(new TickTask(server.getTickCount(), () -> bots.respawn(this)));
    }
}
