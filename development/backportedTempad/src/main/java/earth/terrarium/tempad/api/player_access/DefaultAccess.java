// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/player_access/DefaultAccess.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.player_access;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.Level;

public enum DefaultAccess implements PlayerAccess {
    Public {
        @Override
        public boolean canAccess(Level level, GameProfile owner, GameProfile accessor) {
            return true;
        }
    }
}
