// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/PortalSetupApp.kt (PortalSetupData). Translated to Java
// for Forge 1.19.2: ByteCodec -> encode/decode on FriendlyByteBuf, same field order (locations, optional favourite, ctx).
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.common.data.FavoriteLocationAttachment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PortalSetupData extends AppContent<PortalSetupData> {
    public final Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> locations;
    @Nullable
    public final FavoriteLocationAttachment favoriteLocation;

    public PortalSetupData(Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> locations, @Nullable FavoriteLocationAttachment favoriteLocation, ContextHolder<?> ctx) {
        super(ctx, true);
        this.locations = locations;
        this.favoriteLocation = favoriteLocation;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(locations, FriendlyByteBuf::writeResourceLocation,
                (b, inner) -> b.writeMap(inner, FriendlyByteBuf::writeUUID, (b2, location) -> location.write(b2)));
        buf.writeOptional(Optional.ofNullable(favoriteLocation), (b, fav) -> fav.write(b));
        ctx.encode(buf);
    }

    public static PortalSetupData decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> locations = buf.readMap(LinkedHashMap::new, FriendlyByteBuf::readResourceLocation,
                b -> b.readMap(LinkedHashMap::new, FriendlyByteBuf::readUUID, NamedGlobalVec3::read));
        FavoriteLocationAttachment favorite = buf.readOptional(FavoriteLocationAttachment::read).orElse(null);
        return new PortalSetupData(locations, favorite, ContextHolder.decode(buf));
    }
}
