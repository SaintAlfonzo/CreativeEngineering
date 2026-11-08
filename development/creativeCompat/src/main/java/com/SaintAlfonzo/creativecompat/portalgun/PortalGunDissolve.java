package com.SaintAlfonzo.creativecompat.portalgun;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.mixin.PortalGunPortalInvoker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.entities.CustomPortal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gun portals dissolve when their owner is gone (feature_writeup/plan_portal_changes.md 3.1, G6, D8). Server side.
 *
 * <ul>
 *   <li>Owner message: on logout or dimension change (Immersive Portals fires the Forge event for its own
 *   teleports too) every portal of the owner dissolves at once.</li>
 *   <li>Heartbeat: every {@code [portalgun] heartbeatSeconds} each portal (mixin/PortalGunHeartbeatMixin, staggered by
 *   its uuid) looks for its owner; it dissolves when the owner is offline, in another dimension (a missed message,
 *   e.g. a respawn), or farther than {@code despawnDistance}.</li>
 * </ul>
 *
 * Dissolving removes the portal's {@code PortalGunRecord} entry (so the gun fires a fresh pair) and discards the entity
 * with Portal Gun's closing sound. A portal whose chunk is not loaded only loses its entry; Portal Gun's own
 * {@code updateState} discards a portal with no entry when it next ticks. The other portal of a pair stays and shows
 * as unlinked, as when its partner is shot over.
 */
public final class PortalGunDissolve {

    private PortalGunDissolve() {}

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        dissolveAll(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        dissolveAll(event.getEntity());
    }

    /** Every portal the player owns, loaded or not. */
    public static void dissolveAll(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = serverPlayer.server;
        UUID owner = player.getUUID();
        PortalGunRecord record = PortalGunRecord.get();
        List<PortalGunRecord.PortalInfo> removed = new ArrayList<>();
        record.data.entrySet().removeIf(entry -> {
            if (!owner.equals(entry.getKey().playerId())) return false;
            removed.add(entry.getValue());
            return true;
        });
        if (removed.isEmpty()) return;
        record.setDirty();
        for (PortalGunRecord.PortalInfo info : removed) {
            ServerLevel level = server.getLevel(info.portalDim());
            Entity entity = level == null ? null : level.getEntity(info.portalId());
            if (entity instanceof CustomPortal portal) close(portal);
        }
    }

    /** The heartbeat, called every server tick of a gun portal; does nothing off its beat. */
    public static void heartbeat(CustomPortal portal) {
        Entity entity = portal; // vanilla-typed, so the Entity calls reobfuscate
        if (entity.level.isClientSide || entity.isRemoved() || portal.descriptor == null || !CompatConfig.SPEC.isLoaded()) return;
        int interval = CompatConfig.PORTAL_GUN.heartbeatSeconds.get() * 20;
        int phase = (entity.getUUID().hashCode() & Integer.MAX_VALUE) % interval;
        if (entity.tickCount % interval != phase) return;

        MinecraftServer server = entity.getServer();
        if (server == null) return;
        ServerPlayer owner = server.getPlayerList().getPlayer(portal.descriptor.playerId());
        boolean gone = owner == null || owner.level != entity.level;
        if (!gone) {
            int range = CompatConfig.PORTAL_GUN.despawnDistance.get();
            gone = range > 0 && owner.distanceToSqr(entity) > (double) range * range;
        }
        if (gone) dissolve(portal);
    }

    /** This one portal: its record entry (when it still points here) and the entity. */
    public static void dissolve(CustomPortal portal) {
        Entity entity = portal;
        PortalGunRecord record = PortalGunRecord.get();
        Map<PortalGunRecord.PortalDescriptor, PortalGunRecord.PortalInfo> data = record.data;
        PortalGunRecord.PortalInfo info = data.get(portal.descriptor);
        if (info != null && info.portalId().equals(entity.getUUID())) {
            data.remove(portal.descriptor);
            record.setDirty();
        }
        close(portal);
    }

    private static void close(CustomPortal portal) {
        ((PortalGunPortalInvoker) portal).creativecompat$playClosingSound();
        ((Entity) portal).discard();
    }
}
