// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/location_handlers/AnchorPointsHandler.kt
// (AnchorPointsHandler; AnchorPointsData is its own file). Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.location_handlers;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.LocationHandler;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AnchorPointsHandler implements LocationHandler {
    public static final ResourceLocation ID = Tempad.id("markers");

    public final GameProfile gameProfile;

    public AnchorPointsHandler(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public Map<UUID, NamedGlobalVec3> getLocations() {
        AnchorPointsData points = AnchorPointsData.get();
        return points == null ? new LinkedHashMap<>() : points.getPositions(gameProfile);
    }

    @Override
    public void remove(UUID locationId) {
    }

    @Nullable
    @Override
    public LocationGetter getSerializable(UUID locationId) {
        NamedGlobalVec3 pos = getLocations().get(locationId);
        if (pos == null) return null;
        return new IndirectLocation(gameProfile, Component.translatable("locations.tempad.anchor_point",
                MutableComponent.create(pos.name().getContents()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY), ID, locationId);
    }
}
