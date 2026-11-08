// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/tooltip/ChrononToolip.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.api.tva_device.ChrononData;
import earth.terrarium.tempad.client.TempadUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;

public record ChrononTooltip(ChrononData container) implements ClientTooltipComponent {
    @Override
    public int getHeight() {
        return 17;
    }

    @Override
    public int getWidth(Font font) {
        return 74;
    }

    @Override
    public void renderImage(Font font, int x, int y, PoseStack pose, ItemRenderer itemRenderer, int blitOffset) {
        TempadUI.renderEnergyBar(pose, font, x, y, container.content(), container.capacity());
    }
}
