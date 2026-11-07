// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium): earth.terrarium.olympus.client.ui.modals.Modals.delete and
// ActionModal (header with title and close button, wrapped description, footer with Cancel / Delete), as a Popup layer.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DeleteModal extends PopupBase {
    private static final ResourceLocation MODAL = Button.olympus("modal/modal");
    private static final ResourceLocation MODAL_HEADER = Button.olympus("modal/modal_header");
    private static final ResourceLocation MODAL_FOOTER = Button.olympus("modal/modal_footer");
    private static final WidgetSprites MODAL_CLOSE = new WidgetSprites(Button.olympus("modal/buttons/close/normal"), Button.olympus("modal/buttons/close/normal"), Button.olympus("modal/buttons/close/hovered"));
    private static final Component BACK = Component.translatable("olympus.ui.back");
    private static final Component CANCEL = Component.translatable("olympus.ui.cancel");
    private static final Component DELETE = Component.translatable("olympus.ui.delete");
    private static final int MIN_WIDTH = 150;
    private static final int MIN_HEIGHT = 100;

    private final Component title;
    private final Component description;
    private final Runnable action;
    private int left, top, modalWidth, layoutHeight, headerHeight = 21, footerY, footerHeight;
    private Label text;

    private DeleteModal(Component title, Component description, Runnable action) {
        this.title = title;
        this.description = description;
        this.action = action;
    }

    /** Modals.delete(title, description, action).open(). */
    public static void open(Component title, Component description, Runnable action) {
        DeleteModal modal = new DeleteModal(title, description, action);
        modal.init();
        Popup.open(modal);
    }

    private void init() {
        var screen = Minecraft.getInstance().screen;
        int sw = screen == null ? 0 : screen.width;
        int sh = screen == null ? 0 : screen.height;
        Button cancel = add(new Button().withRenderer(Renderers.text(CANCEL)).withCallback(this::close));
        cancel.withSize(80, 24);
        Button delete = add(new Button().withTexture(Button.OLYMPUS_DANGER).withRenderer(Renderers.text(DELETE).withColor(Gfx.WHITE)).withCallback(() -> {
            action.run();
            close();
        }));
        delete.withSize(80, 24);
        int actionsHeight = 24;
        int actionsWidth = 160 + 2 * 5;
        text = add(Label.multiline(actionsWidth, description).setColor(0xFFFFFF).alignLeft());
        int minContentHeight = MIN_HEIGHT - 11 - actionsHeight - 20;
        int contentHeight = text.getHeight() + 5;
        int contentWidth = text.getWidth();
        modalWidth = Math.max(MIN_WIDTH, Math.max(contentWidth, actionsWidth)) + 10;
        int contentBlock = text.getHeight();
        if (contentHeight < minContentHeight) contentBlock += 5 + (minContentHeight - contentHeight - 5);
        footerHeight = actionsHeight + 10;
        layoutHeight = headerHeight + 5 + contentBlock + 5 + footerHeight;
        left = (sw - modalWidth) / 2;
        top = (sh - layoutHeight) / 2;
        Button close = add(new Button().withTexture(null).withRenderer(Renderers.sprite(MODAL_CLOSE)).withCallback(this::close));
        close.withSize(11, 11).withTooltip(BACK);
        close.setPosition(left + modalWidth - 5 - 11, top + 5);
        text.setPosition(left + 5, top + headerHeight + 5);
        footerY = top + layoutHeight - footerHeight;
        int spacing = (modalWidth - 10 - 160);
        cancel.setPosition(left + 5, footerY + 5);
        delete.setPosition(left + 5 + 80 + spacing, footerY + 5);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        var screen = Minecraft.getInstance().screen;
        if (screen != null) {
            // 1.21 blurs and darkens the screen below; 1.19.2 has no blur, so only the darkening.
            GuiComponent.fill(pose, 0, 0, screen.width, screen.height, 0xC0101010);
        }
        RenderSystem.enableBlend();
        Sprites.blit(pose, MODAL, left - 1, top - 1, modalWidth + 2, layoutHeight + 2);
        Sprites.blit(pose, MODAL_HEADER, left, top, modalWidth, headerHeight);
        Sprites.blit(pose, MODAL_FOOTER, left, footerY, modalWidth, footerHeight);
        var font = Minecraft.getInstance().font;
        Gfx.drawString(pose, font, title, left + 5, top + 5 + (11 - 9 + 1) / 2, 0xFFFFFFFF, true);
        renderWidgets(pose, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        clickWidgets(mouseX, mouseY, button);
        return true;
    }
}
