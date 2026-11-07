// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/app/TempadApp.kt. Translated to Java for Forge 1.19.2:
// Resourceful's ContentMenuProvider -> MenuProvider + createContent + openMenu (NetworkHooks.openScreen, the content
// written to the open-screen buffer).
package earth.terrarium.tempad.api.app;

import earth.terrarium.tempad.common.apps.AppContent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

public interface TempadApp<T extends AppContent<T>> extends MenuProvider {
    T createContent(ServerPlayer player);

    default boolean isEnabled(Player player) {
        return true;
    }

    default void openMenu(ServerPlayer player) {
        T content = createContent(player);
        NetworkHooks.openScreen(player, this, content::encode);
    }
}
