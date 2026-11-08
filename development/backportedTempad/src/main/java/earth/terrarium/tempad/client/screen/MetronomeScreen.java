// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/MetronomeScreen.kt.
// Translated to Java for Forge 1.19.2: Resourceful Lib's AbstractContainerCursorScreen -> AbstractContainerScreen (no
// cursor shapes), 1.21 GUI sprites through client/ui/Sprites, the Olympus buttons through client/ui, ScreenUtils.setTooltip
// -> a tooltip drawn after the frame.
package earth.terrarium.tempad.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.common.menu.MetronomeMenu;
import earth.terrarium.tempad.common.network.c2s.UpdateMetronomePacket;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class MetronomeScreen extends AbstractContainerScreen<MetronomeMenu> {
    public static final ResourceLocation sprite = Tempad.id("screen/metronome");

    public final MutableState<Boolean> locked;
    @Nullable
    private Component frameTooltip;

    public MetronomeScreen(MetronomeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.locked = MutableState.of(menu.data != null && menu.data.locked());
        this.imageWidth = 211;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = 25;
        this.titleLabelY = 22;
        this.inventoryLabelX = 25;

        Button lock = new Button().withTexture(TempadUI.steelButton)
                .withRenderer(locked.withRenderer(isLocked -> Renderers.icon(isLocked ? TempadUI.lockIcon : TempadUI.unlockIcon).withColor(Gfx.BLACK).withCentered(7, 7)))
                .withCallback(() -> locked.setValue(!locked.getValue()));
        lock.withSize(11).withPosition(leftPos + 161, topPos + 19);
        addRenderableWidget(lock);

        Button close = new Button().withTexture(TempadUI.steelButton)
                .withRenderer(Renderers.icon(TempadUI.xIcon).withColor(Gfx.BLACK).withCentered(7, 7))
                .withCallback(this::onClose);
        close.withSize(11).withPosition(leftPos + 174, topPos + 19);
        addRenderableWidget(close);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        Sprites.blit(pose, sprite, leftPos, topPos, imageWidth, imageHeight);

        int power = menu.energy.get(0);
        int capacity = menu.energy.get(1);
        int height = (int) (Math.max(0f, Math.min(1f, (float) power / capacity)) * 54);
        Sprites.blit(pose, TempadUI.powerVert, 6, 54, 0, 54 - height, leftPos + 102, topPos + 20 + 54 - height, 6, height);

        if (mouseX >= leftPos + 102 && mouseX <= leftPos + 106 && mouseY >= topPos + 20 && mouseY <= topPos + 74) {
            frameTooltip = Component.literal(power + "/" + capacity);
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        // 1.21's Screen.render draws the darkened background itself; 1.19.2 screens call it.
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        renderTooltip(pose, mouseX, mouseY);
        if (frameTooltip != null) {
            renderTooltip(pose, frameTooltip, mouseX, mouseY);
            frameTooltip = null;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        // Upstream menu.data!! (the server always sends the data); a missing one sends nothing.
        if (menu.data != null) ModNetworking.sendToServer(new UpdateMetronomePacket(menu.data.pos().pos(), locked.getValue()));
    }
}
