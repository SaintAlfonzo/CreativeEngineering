// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/WalletScreen.kt.
// Translated to Java for Forge 1.19.2: Resourceful Lib's AbstractContainerCursorScreen -> AbstractContainerScreen (no
// cursor shapes, PORT_NOTES 6), 1.21 GUI sprites through client/ui/Sprites, the Olympus close button through client/ui.
package earth.terrarium.tempad.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.common.menu.WalletMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {
    public static final ResourceLocation sprite = Tempad.id("screen/wallet");
    public static final ResourceLocation slot = Tempad.id("screen/wallet_slot");

    public WalletScreen(WalletMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 154;
        this.inventoryLabelY = 58;
    }

    @Override
    protected void init() {
        super.init();
        // Olympus MinecraftColors GRAY / BLACK / WHITE: disabled, normal, hovered.
        Button close = new Button().withTexture(null)
                .withRenderer(Renderers.withColors(Renderers.icon(TempadUI.xIcon), 0xFFAAAAAA, 0xFF000000, 0xFFFFFFFF))
                .withCallback(this::onClose);
        close.withSize(7).withPosition(leftPos + imageWidth - 13, topPos + 6);
        addRenderableWidget(close);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        Sprites.blit(pose, sprite, leftPos, topPos, imageWidth, imageHeight);
        int x = leftPos + 7;
        int y = topPos + 18;
        for (int row = 0; row <= 1; row++) {
            for (int column = 0; column <= 8; column++) {
                Sprites.blit(pose, slot, x + column * 18, y + row * 18, 18, 18);
            }
        }
        for (int row = 0; row <= 2; row++) {
            for (int column = 0; column <= 8; column++) {
                Sprites.blit(pose, slot, x + column * 18, y + row * 18 + 52, 18, 18);
            }
        }
        for (int k = 0; k <= 8; k++) {
            Sprites.blit(pose, slot, x + k * 18, y + 18 * 3 + 56, 18, 18);
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        // 1.21's Screen.render draws the darkened background itself; 1.19.2 screens call it.
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        Gfx.drawString(pose, font, title, titleLabelX, titleLabelY, 0xFF000000, false);
        Gfx.drawString(pose, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF000000, false);
    }
}
