// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/TimelineScreen.kt.
// Translated to Java for Forge 1.19.2 on the client/ui widget kit (TP-D4). 1.21's
// InventoryScreen.renderEntityInInventoryFollowsMouse(box, scale 50, yOffset 0.6) -> 1.19.2's renderEntityInInventory with
// the same centre, feet offset and mouse-relative rotation, clipped to the same box.
package earth.terrarium.tempad.client.screen.tempad;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.widgets.TimelineEntry;
import earth.terrarium.tempad.client.widgets.TimelineWidget;
import earth.terrarium.tempad.common.data.HistoricalLocation;
import earth.terrarium.tempad.common.network.c2s.BackTrackLocation;
import earth.terrarium.tempad.common.registries.ModMenus;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class TimelineScreen extends AbstractTempadScreen<ModMenus.TimelineMenu> {
    public static final Component trackingText = Component.translatable("app.tempad.timeline.tracking");
    public static final Component teleportButton = Component.translatable("app.tempad.timeline.teleport");
    public static final ResourceLocation sprite = Tempad.id("screen/timeline");

    public TimelineScreen(ModMenus.TimelineMenu menu, Inventory inv, Component title) {
        super(sprite, menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();

        TimelineWidget timeline = new TimelineWidget(125, 72);
        timeline.setPosition(localLeft + 5, localTop + 21);

        for (Map.Entry<Date, HistoricalLocation> entry : new TreeMap<>(menu.appContent.history).entrySet()) {
            timeline.add(new TimelineEntry(timeline.getWidth(), font, entry.getKey(), entry.getValue()));
        }

        timeline.scrollToBottom();
        addRenderableWidget(timeline);

        Button teleportBtn = new Button().withTexture(TempadUI.button)
                .withRenderer(Renderers.text(teleportButton).withColor(Gfx.BLACK))
                .withCallback(() -> {
                    if (timeline.current instanceof TimelineEntry entry) {
                        ModNetworking.sendToServer(new BackTrackLocation(entry.date, menu.ctxHolder));
                        onClose();
                    }
                });
        teleportBtn.withSize(font.width(teleportButton) + 12, 16);
        teleportBtn.setPosition(localLeft + 66 - teleportBtn.getWidth() / 2, localTop + 94);
        addRenderableWidget(teleportBtn);

        addRenderableWidget(new Label(localLeft + 133, localTop + 23, 61, 12, trackingText, font).setColor(Tempad.ORANGE.getValue()));
        addRenderableWidget(new Label(localLeft + 133, localTop + 98, 61, 17, inv.player.getName(), font).setColor(Tempad.ORANGE.getValue()));
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        super.renderBg(pose, partialTick, mouseX, mouseY);
        if (minecraft != null && minecraft.player != null) {
            renderEntityFollowsMouse(localLeft + 134, localTop + 37, localLeft + 192, localTop + 94, 50, 0.6f, mouseX, mouseY);
        }
    }

    /** 1.21 InventoryScreen.renderEntityInInventoryFollowsMouse for the local player (entity scale 1 on 1.19.2). */
    private void renderEntityFollowsMouse(int x1, int y1, int x2, int y2, int scale, float yOffset, float mouseX, float mouseY) {
        float centerX = (x1 + x2) / 2f;
        float centerY = (y1 + y2) / 2f;
        int feetY = (int) (centerY + (minecraft.player.getBbHeight() / 2f + yOffset) * scale);
        Gfx.enableScissor(x1, y1, x2, y2);
        InventoryScreen.renderEntityInInventory((int) centerX, feetY, scale, centerX - mouseX, centerY - mouseY, minecraft.player);
        Gfx.disableScissor();
    }
}
