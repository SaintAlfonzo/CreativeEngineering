// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/tooltip/UpgradesTooltip.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.common.data.InstalledUpgradesComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class UpgradesTooltip implements ClientTooltipComponent {
    private final InstalledUpgradesComponent upgrades;
    private final List<Component> text;
    private final List<ResourceLocation> images;

    public UpgradesTooltip(InstalledUpgradesComponent upgrades) {
        this.upgrades = upgrades;
        this.text = upgrades.upgrades().stream().map(id -> (Component) Component.translatable(Util.makeDescriptionId("upgrade", id))).toList();
        this.images = upgrades.upgrades().stream().map(id -> new ResourceLocation(id.getNamespace(), "upgrade/" + id.getPath())).toList();
    }

    @Override
    public int getHeight() {
        return upgrades.upgrades().size() * 12 + 14;
    }

    @Override
    public int getWidth(Font font) {
        return text.stream().mapToInt(font::width).max().orElse(0) + 13;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        font.drawInBatch(Component.translatable("misc.tempad.upgrades"), x, y, -1, true, matrix, buffer, false, 0, 15728880);
        Integer gray = ChatFormatting.GRAY.getColor();
        for (int i = 0; i < text.size(); i++) {
            font.drawInBatch(text.get(i), x + 11, y + 1 + (i + 1) * 12f, gray == null ? -1 : gray, true, matrix, buffer, false, 0, 15728880);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, PoseStack pose, ItemRenderer itemRenderer, int blitOffset) {
        for (int i = 0; i < images.size(); i++) {
            Sprites.blit(pose, images.get(i), x, y + 1 + (i + 1) * 12, 8, 7);
        }
    }
}
