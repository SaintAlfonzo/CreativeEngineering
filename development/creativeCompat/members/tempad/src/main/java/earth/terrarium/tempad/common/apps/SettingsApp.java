// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/SettingsApp.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.LinkedHashSet;

public record SettingsApp(SyncableContext<?> ctx, boolean isStationary) implements TempadApp<SettingsData> {
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ModMenus.SettingsMenu(containerId, inventory, new SettingsData(new LinkedHashSet<>(TempadLocations.registry().keySet()), isStationary, ctx.getHolder()));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("app.tempad.settings");
    }

    @Override
    public SettingsData createContent(ServerPlayer player) {
        return new SettingsData(new LinkedHashSet<>(TempadLocations.registry().keySet()), isStationary, ctx.getHolder());
    }

    @Override
    public boolean isEnabled(Player player) {
        return true;
    }
}
