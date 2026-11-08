// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/compat/FTBTeamsCompat.kt. Translated to Java for Forge 1.19.2
// against FTB Teams 1902.2 (FTBTeamsAPIImpl.INSTANCE.manager.getTeamForPlayerID -> FTBTeamsAPI.getPlayerTeam, null while the
// team manager is not loaded). Loaded only when FTB Teams is (ModLocations checks ModList first), TP-D8.
package earth.terrarium.tempad.common.compat;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.player_access.PlayerAccess;
import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import net.minecraft.world.level.Level;

public final class FTBTeamsCompat implements PlayerAccess {
    public static final FTBTeamsCompat INSTANCE = new FTBTeamsCompat();

    private FTBTeamsCompat() {
    }

    @Override
    public boolean canAccess(Level level, GameProfile owner, GameProfile accessor) {
        Team team = FTBTeamsAPI.getPlayerTeam(owner.getId());
        return team != null && team.getMembers().contains(accessor.getId());
    }

    /** Upstream {@code initFTBTeamsAccess()}: access id {@code tempad:ftbteams}. */
    public static void init() {
        PlayerAccessApi.register(Tempad.id("ftbteams"), INSTANCE);
    }
}
