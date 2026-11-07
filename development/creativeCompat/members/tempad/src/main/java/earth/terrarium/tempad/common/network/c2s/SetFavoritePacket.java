// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/SetFavoritePacket.kt. Translated to Java for Forge
// 1.19.2 (SimpleChannel message).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.common.data.FavoriteLocationAttachment;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record SetFavoritePacket(@Nullable FavoriteLocationAttachment favorite) {
    public SetFavoritePacket(ResourceLocation providerId, UUID locationId) {
        this(new FavoriteLocationAttachment(providerId, locationId));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeOptional(Optional.ofNullable(favorite), (b, fav) -> fav.write(b));
    }

    public static SetFavoritePacket decode(FriendlyByteBuf buf) {
        return new SetFavoritePacket(buf.readOptional(FavoriteLocationAttachment::read).orElse(null));
    }

    public void handle(ServerPlayer player) {
        TempadPlayerData.get(player).setPinnedPosition(favorite);
    }
}
