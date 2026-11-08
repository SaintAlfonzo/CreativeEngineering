// Port glue (ours, GPLv3), TP-D4: Olympus opens its context menus, colour picker and modals as separate Screens that draw
// the screen below as their background. On 1.19.2 leaving a container screen for another screen would rebuild it on the
// way back, so the kit draws them as layers inside the host screen instead (same look, same click-outside-to-close).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public interface Popup {
    void render(PoseStack pose, int mouseX, int mouseY, float partialTick);

    /** Every click goes to the top popup; returning false still does not reach the screen below. */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    default boolean keyPressed(int key, int scan, int modifiers) {
        return false;
    }

    default boolean charTyped(char c, int modifiers) {
        return false;
    }

    default void tick() {
    }

    /** Called once when the popup leaves the stack. */
    default void onClosed() {
    }

    interface Host {
        void openPopup(Popup popup);

        void closePopup(Popup popup);

        /** Olympus ScreenUtils.setTooltip / setTooltipForNextRenderPass. */
        void setTooltipForNextFrame(net.minecraft.network.chat.Component tooltip);
    }

    static Host host() {
        return Minecraft.getInstance().screen instanceof Host host ? host : null;
    }

    static void open(Popup popup) {
        Host host = host();
        if (host != null) host.openPopup(popup);
    }

    default void close() {
        Host host = host();
        if (host != null) host.closePopup(this);
    }
}
