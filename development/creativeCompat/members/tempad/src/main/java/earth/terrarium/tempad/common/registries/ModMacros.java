// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModMacros.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.macro.MacroRegistry;
import earth.terrarium.tempad.common.data.FavoriteLocationAttachment;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ModMacros {
    public static final ResourceLocation TELEPORT_TO_PINNED = Tempad.id("default");

    private ModMacros() {
    }

    public static void init() {
        MacroRegistry.register(TELEPORT_TO_PINNED, (player, ctx) -> {
            NamedGlobalVec3 location = FavoriteLocationAttachment.getPinnedLocation(player, ctx);
            if (location == null) return;
            FavoriteLocationAttachment pinned = TempadPlayerData.get(player).getPinnedPosition();
            Component message = TimedoorEntity.openTimedoor(player, ctx, pinned == null ? null : pinned.providerId(),
                    pinned == null ? null : pinned.locationId(), location);
            if (message != null) player.displayClientMessage(message, true);
        });
    }
}
