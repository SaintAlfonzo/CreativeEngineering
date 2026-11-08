// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/KnowledgeRepositoryApp.kt. Translated to Java for Forge
// 1.19.2: the Tempad app tempad:knowledge_repository (menu tempad:guide), offered by ModApps while the Tempad has the
// knowledge_repository upgrade.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.registries.ModApps;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record KnowledgeRepositoryApp(SyncableContext<?> ctx, boolean isStationary) implements TempadApp<BasicAppContent> {
    @Override
    public BasicAppContent createContent(ServerPlayer player) {
        return new BasicAppContent(ctx.getHolder(), isStationary);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("app." + ModApps.GUIDE.getNamespace() + "." + ModApps.GUIDE.getPath());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ModMenus.KnowledgeMenu(containerId, playerInventory, new BasicAppContent(ctx.getHolder(), isStationary));
    }
}
