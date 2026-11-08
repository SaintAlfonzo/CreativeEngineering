// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium): earth.terrarium.olympus.client.components.Widgets.colorPicker /
// colorInput, components.color.ColorPickerOverlay, ColorPresetType and color.type (HsbColor, HsbState,
// SaturationBrightnessSelector, HueSelector, PresetsSelector, EyedropperOverlay, RecentColorStorage). The overlay and the
// eyedropper are Popup layers instead of Screens.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ColorPicker {
    private ColorPicker() {
    }

    public enum PresetType {
        RECENTS, DEFAULTS, MC_COLORS;

        public Component title() {
            return Component.translatable("olympus.color.preset." + name().toLowerCase(Locale.ROOT));
        }
    }

    public record HsbColor(float hue, float saturation, float brightness, int alpha) {
        public static HsbColor fromRgb(int rgba) {
            int r = (rgba >> 16) & 0xFF, g = (rgba >> 8) & 0xFF, b = rgba & 0xFF, a = (rgba >>> 24) & 0xFF;
            int cmax = Math.max(Math.max(r, g), b);
            int cmin = Math.min(Math.min(r, g), b);
            float brightness = cmax / 255f;
            float saturation = cmax != 0 ? (float) (cmax - cmin) / cmax : 0f;
            float hue;
            if (saturation == 0f) {
                hue = 0f;
            } else {
                float redc = (float) (cmax - r) / (cmax - cmin);
                float greenc = (float) (cmax - g) / (cmax - cmin);
                float bluec = (float) (cmax - b) / (cmax - cmin);
                if (r == cmax) hue = bluec - greenc;
                else if (g == cmax) hue = 2f + redc - bluec;
                else hue = 4f + greenc - redc;
                hue /= 6f;
                if (hue < 0f) hue++;
            }
            return new HsbColor(hue, saturation, brightness, a);
        }

        public int toRgba(boolean hasAlpha) {
            return ((hasAlpha ? alpha : 0) << 24) | (Mth.hsvToRgb(hue, saturation, brightness) & 0xFFFFFF);
        }

        public HsbColor withHue(float hue) {
            return new HsbColor(hue, saturation, brightness, alpha);
        }
    }

    static final class HsbState implements State<HsbColor> {
        private final Consumer<HsbColor> onChange;
        private HsbColor color;
        boolean hasChanged;

        HsbState(HsbColor color, Consumer<HsbColor> onChange) {
            this.color = color;
            this.onChange = onChange;
        }

        @Override
        public HsbColor get() {
            return color;
        }

        @Override
        public void set(HsbColor value) {
            this.color = value;
            this.hasChanged = true;
            onChange.accept(value);
        }
    }

    static final class RecentColors {
        private static final Set<Color> COLORS = new LinkedHashSet<>();

        static void add(Color color) {
            COLORS.remove(color);
            COLORS.add(color);
        }

        static Collection<Color> get(boolean withAlpha) {
            List<Color> list = new ArrayList<>(COLORS);
            Collections.reverse(list);
            if (!withAlpha) list = list.stream().map(c -> new Color(c.getValue() & 0xFFFFFF)).toList();
            return list;
        }
    }

    /** Widgets.colorPicker(state, hasAlpha, factory, overlayFactory). */
    public static Button button(State<Color> state, boolean hasAlpha, Consumer<Button> factory, Consumer<Overlay> overlayFactory) {
        Button button = new Button();
        button.withRenderer(state.withRenderer(color -> {
            Renderers.SolidRenderer solid = Renderers.solid().withColor(color.getValue());
            if (!hasAlpha) solid.withoutAlpha();
            return solid.withPadding(2, 2, 4, 2);
        }));
        button.withSize(16);
        button.withCallback(() -> {
            Overlay overlay = new Overlay(button, state, hasAlpha);
            overlayFactory.accept(overlay);
            overlay.init();
            Popup.open(overlay);
        });
        factory.accept(button);
        return button;
    }

    /**
     * Widgets.colorInput: a text box over a String state that parses into the colour; it keeps its own text (the
     * picker does not write back into it, as in Olympus).
     */
    public static TextBox input(State<Color> state, Consumer<TextBox> factory) {
        TextBox box = new TextBox(new State<>() {
            String temp = state.get().toString();

            @Override
            public String get() {
                return temp;
            }

            @Override
            public void set(String value) {
                state.set(Color.parse(value));
                temp = value;
            }
        });
        factory.accept(box);
        return box;
    }

    public static final class Overlay extends PopupBase {
        private final Button anchor;
        private final State<Color> color;
        private final boolean hasAlpha;
        private final HsbState state;
        private final DropdownState<PresetType> type = DropdownState.of(PresetType.MC_COLORS);
        private ResourceLocation background = Button.olympus("modal/modal");
        private ResourceLocation inset = Button.olympus("modal/modal_inset");
        private Consumer<Button> eyedropperSettings = b -> {
        };
        private BiConsumer<Button, DropdownState<PresetType>> dropdownBtnSettings = (b, s) -> {
        };
        private BiConsumer<Dropdown<PresetType>, DropdownState<PresetType>> dropdownSettings = (d, s) -> {
        };
        private int x, y, width, height, selectX, selectY;

        Overlay(Button anchor, State<Color> color, boolean hasAlpha) {
            this.anchor = anchor;
            this.color = color;
            this.hasAlpha = hasAlpha;
            this.state = new HsbState(HsbColor.fromRgb(color.get().getValue()), c -> color.set(new Color(c.toRgba(hasAlpha))));
        }

        public Overlay withBackground(ResourceLocation background) {
            this.background = background;
            return this;
        }

        public Overlay withInset(ResourceLocation inset) {
            this.inset = inset;
            return this;
        }

        public Overlay withEyedropperSettings(Consumer<Button> settings) {
            this.eyedropperSettings = settings;
            return this;
        }

        public Overlay withDropdownButtonSettings(BiConsumer<Button, DropdownState<PresetType>> settings) {
            this.dropdownBtnSettings = settings;
            return this;
        }

        public Overlay withDropdownSettings(BiConsumer<Dropdown<PresetType>, DropdownState<PresetType>> settings) {
            this.dropdownSettings = settings;
            return this;
        }

        void init() {
            // Grid (spacing 2): [SB 100x50 / hue 100x10, padding 1], [eyedropper + presets dropdown, spacing 4], [presets].
            width = 110;
            Button eyedropper = new Button();
            eyedropper.withRenderer(Renderers.icon(Renderers.EYE_DROPPER).withCentered(12, 12).withPaddingBottom(2));
            eyedropper.withSize(14, 16);
            eyedropper.withCallback(() -> Eyedropper.open(state));
            eyedropperSettings.accept(eyedropper);
            List<PresetType> types = List.of(PresetType.RECENTS, PresetType.MC_COLORS);
            Button presetsButton = Dropdown.create(type, types, PresetType::title, b -> {
                b.withSize(84, 16);
                b.withTexture(null);
                b.withRenderer(type.withRenderer((value, open) -> Renderers.textWithChevron(value.title(), open).withColor(Gfx.GRAY)));
                dropdownBtnSettings.accept(b, type);
            }, d -> dropdownSettings.accept(d, type));
            PresetsSelector presets = new PresetsSelector(102, type, color, hasAlpha, inset);
            int row1 = Math.max(eyedropper.getHeight(), presetsButton.getHeight());
            int layoutHeight = 62 + 2 + row1 + 2 + presets.getHeight();
            height = layoutHeight + 8;
            x = anchor.x;
            y = anchor.y + anchor.getHeight();
            selectX = x + 5;
            selectY = y + 5;
            add(new SaturationBrightness(100, 50, state)).setPosition(selectX, selectY);
            add(new HueSelector(100, 10, state)).setPosition(selectX, selectY + 50);
            eyedropper.setPosition(x + 4, y + 4 + 62 + 2 + (row1 - eyedropper.getHeight()) / 2);
            add(eyedropper);
            presetsButton.setPosition(x + 4 + eyedropper.getWidth() + 4, y + 4 + 62 + 2 + (row1 - presetsButton.getHeight()) / 2);
            add(presetsButton);
            presets.setPosition(x + 4, y + 4 + 62 + 2 + row1 + 2);
            add(presets);
        }

        @Override
        public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            Sprites.blit(pose, background, x, y, width, height);
            Sprites.blit(pose, inset, selectX - 1, selectY - 1, 102, 62);
            renderWidgets(pose, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && !(mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)) {
                close();
                return false;
            }
            return clickWidgets(mouseX, mouseY, button);
        }

        @Override
        public void onClosed() {
            if (state.hasChanged) RecentColors.add(color.get());
        }
    }

    private static void gradient(PoseStack pose, int x, int y, int w, int h, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        Matrix4f m = pose.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertex(bb, m, x, y + h, bottomLeft);
        vertex(bb, m, x + w, y + h, bottomRight);
        vertex(bb, m, x + w, y, topRight);
        vertex(bb, m, x, y, topLeft);
        Tesselator.getInstance().end();
        RenderSystem.enableTexture();
    }

    private static void vertex(BufferBuilder bb, Matrix4f m, float x, float y, int argb) {
        bb.vertex(m, x, y, 0f).color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF).endVertex();
    }

    static final class SaturationBrightness extends KitWidget {
        private final HsbState state;

        SaturationBrightness(int width, int height, HsbState state) {
            super(width, height);
            this.state = state;
        }

        @Override
        protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            HsbColor color = state.get();
            int posX = Math.round(color.saturation() * width);
            int posY = height - Math.round(color.brightness() * height);
            int tileW = Math.round(width / 10f);
            int tileH = Math.round(height / 10f);
            for (int dy = 0; dy < 10; dy++) {
                float minB = dy / 10f, maxB = (dy + 1) / 10f;
                for (int dx = 0; dx < 10; dx++) {
                    float minS = dx / 10f, maxS = (dx + 1) / 10f;
                    gradient(pose, x + dx * tileW, y + (10 - dy - 1) * tileH, tileW, tileH,
                            new HsbColor(color.hue(), minS, maxB, 255).toRgba(true), new HsbColor(color.hue(), maxS, maxB, 255).toRgba(true),
                            new HsbColor(color.hue(), minS, minB, 255).toRgba(true), new HsbColor(color.hue(), maxS, minB, 255).toRgba(true));
                }
            }
            Gfx.outline(pose, x + posX - 1, y + posY - 1, 3, 3, 0xFF000000);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !isMouseOver(mouseX, mouseY)) return false;
            int dx = (int) mouseX - x, dy = (int) mouseY - y;
            if (dx >= 0 && dx < width && dy >= 0 && dy < height) {
                state.set(new HsbColor(state.get().hue(), Mth.clamp((float) dx / width, 0f, 1f), 1f - Mth.clamp((float) dy / height, 0f, 1f), 255));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return mouseClicked(mouseX, mouseY, button);
        }
    }

    static final class HueSelector extends KitWidget {
        private final HsbState state;

        HueSelector(int width, int height, HsbState state) {
            super(width, height);
            this.state = state;
        }

        @Override
        protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            for (int i = 0; i < width; i++) {
                Gfx.fill(pose, x + i, y, x + i + 1, y + height, 0xFF000000 | Mth.hsvToRgb((float) i / width, 1f, 1f));
            }
            int posX = Mth.floor(state.get().hue() * width);
            Gfx.outline(pose, x + posX - 1, y, 3, height, 0xFF000000);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !isMouseOver(mouseX, mouseY)) return false;
            state.set(state.get().withHue(Mth.clamp((float) (mouseX - x) / width, 0f, 1f)));
            return true;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return mouseClicked(mouseX, mouseY, button);
        }
    }

    static final class PresetsSelector extends KitWidget {
        private final State<PresetType> type;
        private final State<Color> state;
        private final boolean withAlpha;
        private final ResourceLocation background;
        private Collection<Color> colors = new ArrayList<>();
        private PresetType lastType;

        PresetsSelector(int width, State<PresetType> type, State<Color> state, boolean withAlpha, ResourceLocation background) {
            super(width, width / 8 * 2 + 6);
            this.type = type;
            this.state = state;
            this.withAlpha = withAlpha;
            this.background = background;
        }

        private Collection<Color> colors() {
            if (lastType != type.get()) {
                colors = switch (type.get()) {
                    case DEFAULTS -> List.of();
                    case RECENTS -> RecentColors.get(withAlpha);
                    case MC_COLORS -> {
                        List<Color> list = new ArrayList<>();
                        for (int c : Gfx.MC_COLORS) list.add(new Color(c));
                        yield list;
                    }
                };
                lastType = type.get();
            }
            return colors;
        }

        @Override
        protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            Sprites.blit(pose, background, x, y, width, height);
            int size = (width - 18) / 8;
            int i = 0;
            for (Color color : colors()) {
                if (i >= 16) break;
                int j = i % 8, k = i / 8;
                int cx = x + 4 + j * size + 2 * j;
                int cy = y + 4 + k * size + 2 * k;
                int rgba = color.getValue();
                if (!withAlpha) rgba |= 0xFF000000;
                Gfx.fill(pose, cx, cy, cx + size, cy + size, rgba);
                Gfx.outline(pose, cx, cy, size, size, 0xFFDDDDDD);
                if (mouseX >= cx && mouseX <= cx + size && mouseY >= cy && mouseY <= cy + size) {
                    Gfx.outline(pose, cx, cy, size, size, 0xFF000000);
                    Popup.Host host = Popup.host();
                    if (host != null) {
                        if (!withAlpha) rgba &= 0xFFFFFF;
                        TextColor tc = TextColor.fromRgb(rgba & 0xFFFFFF);
                        MutableComponent text = Component.literal("[").withStyle(s -> s.withColor(tc))
                                .append(Component.literal(String.format(Locale.ROOT, "#%06X", rgba)).withStyle(s -> s.withColor(TextColor.fromRgb(0xFFFFFF))))
                                .append(Component.literal("]").withStyle(s -> s.withColor(tc)));
                        host.setTooltipForNextFrame(text);
                    }
                }
                i++;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            int size = (width - 18) / 8;
            int i = 0;
            for (Color color : colors()) {
                if (i >= 16) break;
                int j = i % 8, k = i / 8;
                int cx = x + 3 + j * size + 2 * j;
                int cy = y + 3 + k * size + 2 * k;
                if (mouseX >= cx && mouseX <= cx + size && mouseY >= cy && mouseY <= cy + size) {
                    RecentColors.add(state.get());
                    lastType = null;
                    state.set(new Color(color.getValue()));
                    return true;
                }
                i++;
            }
            return false;
        }
    }

    /** Olympus EyedropperOverlay: a still of the screen; the colour under the cursor is picked on click. */
    static final class Eyedropper implements Popup {
        private static final ResourceLocation SCREEN_TEXTURE = new ResourceLocation("tempad", "eyedropper_screen");
        private final NativeImage image;
        private final HsbState state;

        private Eyedropper(NativeImage image, HsbState state) {
            this.image = image;
            this.state = state;
        }

        static void open(HsbState state) {
            Minecraft mc = Minecraft.getInstance();
            DynamicTexture texture = new DynamicTexture(Screenshot.takeScreenshot(mc.getMainRenderTarget()));
            mc.getTextureManager().register(SCREEN_TEXTURE, texture);
            Popup.open(new Eyedropper(texture.getPixels(), state));
        }

        /** The ARGB pixel under the cursor, or empty outside the image. */
        private java.util.OptionalInt pixelAt(double mouseX, double mouseY) {
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            int px = (int) (mouseX * guiScale), py = (int) (mouseY * guiScale);
            if (image == null || px < 0 || py < 0 || px >= image.getWidth() || py >= image.getHeight()) return java.util.OptionalInt.empty();
            int abgr = image.getPixelRGBA(px, py);
            return java.util.OptionalInt.of((abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr & 0xFF0000) >> 16));
        }

        @Override
        public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            var screen = Minecraft.getInstance().screen;
            if (screen == null) return;
            int w = screen.width, h = screen.height;
            RenderSystem.setShaderTexture(0, SCREEN_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            quad(pose, 0, 0, w, h, 0f, 0f, 1f, 1f);
            int x = mouseX - 5, y = mouseY - 5;
            quad(pose, x - 5, y - 5, x + 15, y + 15, (float) x / w, (float) y / h, (float) (x + 10) / w, (float) (y + 10) / h);
            Gfx.outline(pose, x - 5, y - 5, 20, 20, -1);
            java.util.OptionalInt pixel = pixelAt(mouseX, mouseY);
            if (pixel.isPresent()) {
                int rgb = pixel.getAsInt();
                Popup.Host host = Popup.host();
                if (host != null) host.setTooltipForNextFrame(Component.literal(String.format("#%08X", rgb)).withStyle(s -> s.withColor(TextColor.fromRgb(rgb & 0xFFFFFF))));
            }
        }

        private static void quad(PoseStack pose, float x1, float y1, float x2, float y2, float u0, float v0, float u1, float v1) {
            Matrix4f m = pose.last().pose();
            BufferBuilder bb = Tesselator.getInstance().getBuilder();
            bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bb.vertex(m, x1, y2, 0f).uv(u0, v1).endVertex();
            bb.vertex(m, x2, y2, 0f).uv(u1, v1).endVertex();
            bb.vertex(m, x2, y1, 0f).uv(u1, v0).endVertex();
            bb.vertex(m, x1, y1, 0f).uv(u0, v0).endVertex();
            Tesselator.getInstance().end();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return true;
            java.util.OptionalInt pixel = pixelAt(mouseX, mouseY);
            if (pixel.isPresent()) {
                state.set(HsbColor.fromRgb(pixel.getAsInt()));
                close();
            }
            return true;
        }

        @Override
        public void onClosed() {
            Minecraft.getInstance().getTextureManager().release(SCREEN_TEXTURE);
        }
    }
}
