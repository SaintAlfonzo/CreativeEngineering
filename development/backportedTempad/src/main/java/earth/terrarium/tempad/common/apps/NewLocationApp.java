// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/NewLocationApp.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record NewLocationApp(SyncableContext<?> ctx) implements TempadApp<NewLocationAppData> {
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ModMenus.NewLocationMenu(containerId, inventory, new NewLocationAppData(CommonConfig.allowLocationSaving(), ctx.getHolder()));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("app.tempad.new_location");
    }

    @Override
    public NewLocationAppData createContent(ServerPlayer player) {
        return new NewLocationAppData(CommonConfig.allowLocationSaving(), ctx.getHolder());
    }
}
