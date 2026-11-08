// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/widgets/TimelineEntry.kt.
// Translated to Java for Forge 1.19.2: Minecraft.locale -> the selected language's Java locale; GuiGraphics.blitSprite
// -> client/ui/Sprites; Vec3.component / ResourceKey.component (upstream Extensions.kt) -> positionText / dimensionText.
package earth.terrarium.tempad.client.widgets;

import com.ibm.icu.text.DateFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.common.data.HistoricalLocation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimelineEntry extends TimelineWidget.Item {
    public static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT,
            Minecraft.getInstance().getLanguageManager().getSelected().getJavaLocale());

    /** Upstream {@code Vec3.component}. */
    public static Component positionText(Vec3 pos) {
        return Component.literal("XYZ: " + (int) pos.x + " / " + (int) pos.y + " / " + (int) pos.z);
    }

    /** Upstream {@code ResourceKey<Level>.component}. */
    public static Component dimensionText(ResourceKey<Level> dimension) {
        return Component.translatable(Util.makeDescriptionId("dimension", dimension.location()));
    }

    /** Upstream {@code ResourceLocation(marker.namespace, "marker/" + marker.path)}, a GUI sprite. */
    public static ResourceLocation markerSprite(ResourceLocation marker) {
        return new ResourceLocation(marker.getNamespace(), "marker/" + marker.getPath());
    }

    private final Font font;
    public final Date date;
    public final HistoricalLocation location;
    private final List<FormattedCharSequence> text = new ArrayList<>();
    @Nullable
    private final ResourceLocation sprite;

    public TimelineEntry(int parentWidth, Font font, Date date, HistoricalLocation location) {
        this.font = font;
        this.date = date;
        this.location = location;
        Component dateDisplay = Component.literal(dateFormat.format(date));
        MutableComponent markerComponent = location.marker() != null
                ? Component.translatable(Util.makeDescriptionId("marker", location.marker()))
                : Component.translatable("misc.tempad.wandering");
        text.addAll(font.split(markerComponent.append(" ").append(dimensionText(location.dimension())), parentWidth - 6));
        text.add(positionText(location.pos()).getVisualOrderText());
        text.add(dateDisplay.getVisualOrderText());
        this.sprite = location.marker() != null ? markerSprite(location.marker()) : null;
    }

    private int parentCenter() {
        return parent != null ? parent.x + parent.getWidth() / 2 : x + width / 2;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        if (current) {
            for (int index = 0; index < text.size(); index++) {
                boolean isLast = index == text.size() - 1;
                FormattedCharSequence line = text.get(index);
                font.drawShadow(pose, line, parentCenter() - font.width(line) / 2, y + 4 + index * 10,
                        isLast ? Tempad.DARK_ORANGE.getValue() : Tempad.ORANGE.getValue());
            }
        }

        if (sprite != null) {
            RenderSystem.enableBlend();
            Sprites.blit(pose, sprite, x + 4, y + height - 22, 12, 12);
        }

        Gfx.fill(pose, x + width / 2 - 1, y + height - 6, x + width / 2 + 1, y + height - 2,
                current ? Tempad.HIGHLIGHTED_ORANGE.getValue() : Tempad.ORANGE.getValue());
    }
}
