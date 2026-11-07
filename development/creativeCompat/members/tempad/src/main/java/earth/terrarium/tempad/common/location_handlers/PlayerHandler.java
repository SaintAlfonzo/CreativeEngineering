// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/location_handlers/PlayerHandler.kt. Translated to Java for Forge
// 1.19.2.
package earth.terrarium.tempad.common.location_handlers;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.player_access.PlayerAccess;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHandler implements LocationHandler {
    public static final ResourceLocation ID = Tempad.id("player");

    public final GameProfile player;
    @Nullable
    public final UpgradeHandler upgrades;

    public PlayerHandler(GameProfile player, @Nullable UpgradeHandler upgrades) {
        this.player = player;
        this.upgrades = upgrades;
    }

    @Override
    public Map<UUID, NamedGlobalVec3> getLocations() {
        if (upgrades == null || !upgrades.contains(ModItems.playerKey)) return Map.of();
        MinecraftServer server = Tempad.server();
        if (server == null) return Map.of();
        Map<UUID, NamedGlobalVec3> result = new LinkedHashMap<>();
        for (ServerPlayer it : server.getPlayerList().getPlayers()) {
            if (it.getUUID().equals(player.getId()) || it.isSpectator()) continue;
            boolean visible = ContextRegistry.locate(it, stack -> {
                PlayerAccess access = PlayerAccess.of(stack);
                return access != null && access.canAccess(server.overworld(), it.getGameProfile(), player);
            }) != null;
            if (visible) result.put(it.getUUID(), NamedGlobalVec3.of(it));
        }
        return result;
    }

    @Override
    public void remove(UUID locationId) {
        // NO-OP
    }

    @Nullable
    @Override
    public LocationGetter getSerializable(UUID locationId) {
        MinecraftServer server = Tempad.server();
        ServerPlayer target = server == null ? null : server.getPlayerList().getPlayer(locationId);
        if (target == null) return null;
        GameProfile pos = target.getGameProfile();
        return new IndirectLocation(player, Component.translatable("locations.tempad.player",
                Component.literal(pos.getName()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY), ID, locationId);
    }
}
