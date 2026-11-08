// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModLocations.kt. Translated to Java for Forge 1.19.2.
// Handlers in upstream order (default, anchor points, player, wallet); the FTB Teams access option only when FTB Teams is
// installed (TP-D8). Upstream's Odyssey Allies option is not ported (the pack does not have that mod).
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.api.player_access.DefaultAccess;
import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import earth.terrarium.tempad.common.compat.FTBTeamsCompat;
import earth.terrarium.tempad.common.location_handlers.AnchorPointsHandler;
import earth.terrarium.tempad.common.location_handlers.DefaultLocationHandler;
import earth.terrarium.tempad.common.location_handlers.PlayerHandler;
import earth.terrarium.tempad.common.location_handlers.WalletLocationHandler;
import net.minecraftforge.fml.ModList;

public final class ModLocations {
    private ModLocations() {
    }

    public static void init() {
        TempadLocations.register(DefaultLocationHandler.ID, (player, upgrades, chronons) -> new DefaultLocationHandler(player));
        TempadLocations.register(AnchorPointsHandler.ID, (player, upgrades, chronons) -> new AnchorPointsHandler(player));
        TempadLocations.register(PlayerHandler.ID, (player, upgrades, chronons) -> new PlayerHandler(player, upgrades));
        TempadLocations.register(WalletLocationHandler.ID, WalletLocationHandler::new);
        TempadLocations.setDeletable(DefaultLocationHandler.ID);
        PlayerAccessApi.register(Tempad.id("public"), DefaultAccess.Public);
        if (ModList.get().isLoaded("ftbteams")) FTBTeamsCompat.init();
    }
}
