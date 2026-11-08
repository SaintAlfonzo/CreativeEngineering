// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/TimelineApp.kt (TimelineApp;
// TimelineData is its own file). Translated to Java for Forge 1.19.2: player.travelHistory -> TempadPlayerData.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.LinkedHashMap;

public record TimelineApp(SyncableContext<?> ctx, boolean isStationary) implements TempadApp<TimelineData> {
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ModMenus.TimelineMenu(containerId, inventory, createContent((ServerPlayer) player));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("app.tempad.timeline");
    }

    @Override
    public TimelineData createContent(ServerPlayer player) {
        return new TimelineData(new LinkedHashMap<>(TempadPlayerData.get(player).getTravelHistory().getHistory()), isStationary, ctx.getHolder());
    }
}
