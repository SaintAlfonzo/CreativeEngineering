// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/PortalSetupApp.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.apps;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record PortalSetupApp(SyncableContext<?> ctx) implements TempadApp<PortalSetupData> {
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ModMenus.PortalSetupMenu(containerId, inventory, createContent((ServerPlayer) player));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("app.tempad.portal_setup");
    }

    @Override
    public PortalSetupData createContent(ServerPlayer player) {
        GameProfile owner = ModComponents.getOwner(ctx.getStack());
        GameProfile profile = owner != null ? owner : player.getGameProfile();
        return new PortalSetupData(TempadLocations.getAll(profile, UpgradeHandler.of(ctx.getStack()), ChrononHandler.of(ctx.getStack())),
                TempadPlayerData.get(player).getPinnedPosition(), ctx.getHolder());
    }

    @Override
    public boolean isEnabled(Player player) {
        return true;
    }
}
