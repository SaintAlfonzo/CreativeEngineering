// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/FavoriteLocationAttachment.kt. Translated to Java for
// Forge 1.19.2: attachment -> part of the TempadPlayerData capability; Codec -> NBT {providerId, locationId}.
package earth.terrarium.tempad.common.data;

import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.common.location_handlers.DefaultLocationHandler;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record FavoriteLocationAttachment(ResourceLocation providerId, UUID locationId) {
    public static final FavoriteLocationAttachment EMPTY = new FavoriteLocationAttachment(DefaultLocationHandler.ID, Util.NIL_UUID);

    public boolean matches(@Nullable ResourceLocation providerId, @Nullable UUID locationId) {
        return Objects.equals(this.providerId, providerId) && Objects.equals(this.locationId, locationId);
    }

    /** Upstream {@code Player.getPinnedLocation(ctx)}. */
    @Nullable
    public static NamedGlobalVec3 getPinnedLocation(Player player, SyncableContext<?> ctx) {
        FavoriteLocationAttachment pinned = TempadPlayerData.get(player).getPinnedPosition();
        if (pinned == null) return null;
        LocationHandler handler = TempadLocations.get(player, ctx, pinned.providerId());
        return handler == null ? null : handler.get(pinned.locationId());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("providerId", providerId.toString());
        tag.put("locationId", NbtUtils.createUUID(locationId));
        return tag;
    }

    @Nullable
    public static FavoriteLocationAttachment load(CompoundTag tag) {
        ResourceLocation provider = ResourceLocation.tryParse(tag.getString("providerId"));
        if (provider == null || !tag.contains("locationId")) return null;
        return new FavoriteLocationAttachment(provider, NbtUtils.loadUUID(tag.get("locationId")));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(providerId);
        buf.writeUUID(locationId);
    }

    public static FavoriteLocationAttachment read(FriendlyByteBuf buf) {
        return new FavoriteLocationAttachment(buf.readResourceLocation(), buf.readUUID());
    }
}
