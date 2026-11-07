// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/TimeTwisterScreen.kt.
// Translated to Java for Forge 1.19.2: 1.21's Tesselator.begin / addVertex / build -> 1.19.2's BufferBuilder begin /
// vertex / end; GuiGraphics.blitSprite -> client/ui/Sprites; setTooltipForNextRenderPass -> a tooltip drawn at the end
// of render; 1.21's Screen.render darkens the world itself, so render calls renderBackground first.
// Deviation: an entry without a marker id (upstream `history.marker!!` would crash) draws no icon and names itself
// "wandering", as the Timeline app does.
package earth.terrarium.tempad.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.client.widgets.TimelineEntry;
import earth.terrarium.tempad.common.data.HistoricalLocation;
import earth.terrarium.tempad.common.network.c2s.BackTrackLocation;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TimeTwisterScreen extends Screen {
    private static final int MAX_ENTRIES = 8;
    private static final int START = 25;
    private static final int END = 80;
    private static final int END_HOVERED = 85;

    private static final float TEXT_LENGTH = (START + END) * 0.4f;
    private static final float TEXT_LENGTH_HOVERED = (START + END + 5) * 0.4f;

    private static final float ICON_LENGTH = (START + END) * 0.575f;
    private static final float ICON_LENGTH_HOVERED = (START + END + 5) * 0.575f;

    private static final int TEXT_COLOR = 0xAAAAAA;
    private static final int TEXT_COLOR_DISABLED = 0xFF5555;

    private static final float COLOR = 0.2f;
    private static final float COLOR_ODD = 0.25f;
    private static final float COLOR_HOVER = 1.2f;
    private static final float COLOR_ODD_HOVER = 1.25f;

    public static long lastSelect = 0;

    public static Vec2 toPoint(float angle, float length) {
        float radians = (float) Math.toRadians(angle);
        float x = Mth.sin(radians) * length;
        float y = -Mth.cos(radians) * length;
        return new Vec2(x, y);
    }

    public static int indexFromAngle(float angle) {
        return Math.round(angle * MAX_ENTRIES / 360f) % MAX_ENTRIES;
    }

    public static float angleFromIndex(int index) {
        return (360f / MAX_ENTRIES) * index;
    }

    private static void drawXYRGBA(BufferBuilder builder, float x, float y, float r, float g, float b, float a) {
        builder.vertex(x, y, 0f).color(r, g, b, a).endVertex();
    }

    private final ContextHolder<?> ctx;
    private final List<Map.Entry<Date, HistoricalLocation>> locations;
    @Nullable
    private List<FormattedCharSequence> tooltip;

    public TimeTwisterScreen(Map<Date, HistoricalLocation> history, ContextHolder<?> ctx) {
        super(CommonComponents.EMPTY);
        this.ctx = ctx;
        List<Map.Entry<Date, HistoricalLocation>> list = new ArrayList<>(new TreeMap<>(history).entrySet());
        Collections.reverse(list);
        this.locations = list;
    }

    private int getIndex() {
        if (minecraft == null) return -1;
        Window window = minecraft.getWindow();
        MouseHandler mouse = minecraft.mouseHandler;
        double mdx;
        double mdy;
        double minRadius;
        if (Minecraft.ON_OSX) {
            mdx = mouse.xpos() - window.getWidth() / 4f;
            mdy = mouse.ypos() - window.getHeight() / 4f;
            minRadius = window.getGuiScale() * START / 2;
        } else {
            mdx = mouse.xpos() - window.getWidth() / 2f;
            mdy = mouse.ypos() - window.getHeight() / 2f;
            minRadius = window.getGuiScale() * START;
        }
        if (mdx * mdx + mdy * mdy < minRadius * minRadius) return -1;
        float angle = (float) Math.toDegrees(Math.atan2(mdx, -mdy));
        if (angle < 0) angle += 360;
        return indexFromAngle(angle);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return button == 0 ? select(getIndex()) : super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return switch (keyCode) {
            case InputConstants.KEY_1 -> select(0);
            case InputConstants.KEY_2 -> select(1);
            case InputConstants.KEY_3 -> select(2);
            case InputConstants.KEY_4 -> select(3);
            case InputConstants.KEY_5 -> select(4);
            case InputConstants.KEY_6 -> select(5);
            case InputConstants.KEY_7 -> select(6);
            case InputConstants.KEY_8 -> select(7);
            default -> false;
        };
    }

    private boolean select(int index) {
        if (index >= 0 && index < locations.size()) {
            ModNetworking.sendToServer(new BackTrackLocation(locations.get(index).getKey(), ctx));
        }
        onClose();
        lastSelect = System.currentTimeMillis();
        return true;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int hoveredIndex = getIndex();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();

        float middleX = scaledWidth / 2f;
        float middleY = scaledHeight / 2f;

        for (int angle = 0; angle < 360; angle++) {
            int index = indexFromAngle(angle);

            boolean hovered = hoveredIndex == index && index < locations.size();

            int length = hovered ? END_HOVERED : END;
            float color;
            if (index % 2 == 0) {
                color = hovered ? COLOR_HOVER : COLOR;
            } else {
                color = hovered ? COLOR_ODD_HOVER : COLOR_ODD;
            }

            double radians1 = Math.toRadians(angle);
            double radians2 = Math.toRadians(angle + 1);

            float x1 = (float) Math.sin(radians1);
            float y1 = (float) -Math.cos(radians1);
            float x2 = (float) Math.sin(radians2);
            float y2 = (float) -Math.cos(radians2);

            drawXYRGBA(builder, middleX + x1 * length, middleY + y1 * length, color, color, color, 0.8f);
            drawXYRGBA(builder, middleX + x2 * length, middleY + y2 * length, color, color, color, 0.8f);
            drawXYRGBA(builder, middleX + x2 * START, middleY + y2 * START, color, color, color, 0.56f);
            drawXYRGBA(builder, middleX + x1 * START, middleY + y1 * START, color, color, color, 0.56f);

            float innerColor = index % 2 == 0 ? 1f : 0.5f;

            int start = length - 2;

            drawXYRGBA(builder, middleX + x1 * length, middleY + y1 * length, innerColor, innerColor, innerColor, 1f);
            drawXYRGBA(builder, middleX + x2 * length, middleY + y2 * length, innerColor, innerColor, innerColor, 1f);
            drawXYRGBA(builder, middleX + x2 * start, middleY + y2 * start, innerColor, innerColor, innerColor, 1f);
            drawXYRGBA(builder, middleX + x1 * start, middleY + y1 * start, innerColor, innerColor, innerColor, 1f);
        }

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableCull();

        tooltip = null;
        for (int index = 0; index < MAX_ENTRIES; index++) {
            boolean hovered = hoveredIndex == index && index < locations.size();
            float angle = angleFromIndex(index);
            Vec2 pos = toPoint(angle, hovered ? TEXT_LENGTH_HOVERED : TEXT_LENGTH);

            drawCenteredString(pose, font, String.valueOf(index + 1), (int) (scaledWidth / 2f + pos.x), (int) (scaledHeight / 2f + pos.y), getTextColor(index));

            pos = toPoint(angle, hovered ? ICON_LENGTH_HOVERED : ICON_LENGTH);

            if (index < locations.size()) {
                Date date = locations.get(index).getKey();
                HistoricalLocation history = locations.get(index).getValue();
                if (history.marker() != null) {
                    RenderSystem.enableBlend();
                    Sprites.blit(pose, TimelineEntry.markerSprite(history.marker()),
                            (int) (scaledWidth / 2f + pos.x - 8), (int) (scaledHeight / 2f + pos.y - 8), 16, 16);
                }
                if (hovered) {
                    MutableComponent marker = history.marker() != null
                            ? Component.translatable(Util.makeDescriptionId("marker", history.marker()))
                            : Component.translatable("misc.tempad.wandering");
                    marker.append(" ");
                    marker.append(TimelineEntry.dimensionText(history.dimension()));
                    tooltip = List.of(
                            marker.getVisualOrderText(),
                            TimelineEntry.positionText(history.pos()).getVisualOrderText(),
                            Component.literal(TimelineEntry.dateFormat.format(date)).getVisualOrderText());
                }
            }
        }

        if (tooltip != null) renderTooltip(pose, tooltip, mouseX, mouseY);
    }

    private int getTextColor(int index) {
        return index >= locations.size() ? TEXT_COLOR_DISABLED : TEXT_COLOR;
    }
}
