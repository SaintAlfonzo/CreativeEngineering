package com.SaintAlfonzo.creativeengineering.compat.guititle;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.compat.guititle.TitlePlaqueLayout.Blit;
import com.SaintAlfonzo.creativeengineering.compat.guititle.TitlePlaqueLayout.Plaque;
import com.SaintAlfonzo.creativeengineering.compat.guititle.TitlePlaqueLayout.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

/**
 * Stretches a GUI resource pack's title plaque to fit long container titles (renamed chests, Quark wood
 * chests, shulker box colours, modded minecarts, horse names) and logs every title that needed it.
 *
 * <p>Flow, per frame, driven by {@code mixin/ContainerTitlePlaqueMixin}:
 * <ol>
 * <li>Right before {@code renderBg}, {@link #beginCapture} arms {@code RenderSystemShaderTextureMixin}, which
 * records the first texture bound during {@code renderBg}: the screen's background.</li>
 * <li>Right after {@code renderBg}, {@link #afterBackground} finds the plaque under the title in that texture
 * ({@link TitlePlaqueLayout}, cached per texture and row), draws the stretch over the background, and returns
 * the text to draw when the title has to be cut short to stay inside the GUI.</li>
 * <li>The title draw in vanilla {@code renderLabels} then uses that text and x position.</li>
 * </ol>
 *
 * <p>Only screens that keep vanilla's {@code renderLabels} are touched: their title is drawn exactly at
 * {@code titleLabelX/Y}. Screens that draw their own labels (villager trading, creative, beacon, anvil)
 * place the title elsewhere, so a plaque found at {@code titleLabelX} might not be the title's.
 *
 * <p>Overflow log: {@code kubejs/exported/gui_title_overflow.json}, one entry per screen class + texture +
 * title, merged across sessions. Config: {@code [gui_title]} in creativeengineering-common.toml.
 */
@Mod.EventBusSubscriber(modid = "creativeengineering", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TitlePlaques {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ELLIPSIS = "...";
    /** Columns kept clear between a stretched plaque's cap and the GUI's left/right edge. */
    private static final int MARGIN = 2;
    private static final String LOG_FILE = "kubejs/exported/gui_title_overflow.json";

    private static final Field TITLE_X =
            ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97728_");
    private static final Field TITLE_Y =
            ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97729_");

    /** Title text and x to draw instead of the screen's own, when the title was cut short. */
    public record Label(Component text, float x) {}

    private TitlePlaques() {}

    // ---- texture capture (render thread only) -------------------------------------------------

    private static boolean capturing;
    private static ResourceLocation captured;

    public static void beginCapture() {
        capturing = true;
        captured = null;
    }

    /** Called from RenderSystem.setShaderTexture(int, ResourceLocation). */
    public static void onShaderTexture(int unit, ResourceLocation location) {
        if (capturing && unit == 0 && captured == null) captured = location;
    }

    private static ResourceLocation endCapture() {
        capturing = false;
        ResourceLocation location = captured;
        captured = null;
        return location;
    }

    // ---- per-frame work -----------------------------------------------------------------------

    /**
     * Draws the stretched plaque for {@code screen}'s title, if it needs one. Returns the replacement title
     * label when the title had to be truncated, else null (draw the title as usual).
     */
    public static Label afterBackground(AbstractContainerScreen<?> screen, PoseStack pose) {
        ResourceLocation location = endCapture();
        if (location == null || !CEConfig.GUI_TITLE.stretchPlaque.get()) return null;
        if (!USES_VANILLA_LABELS.get(screen.getClass())) return null;
        Texture tex = texture(location);
        if (tex == null) return null;

        int titleX, titleY;
        try {
            titleX = TITLE_X.getInt(screen);
            titleY = TITLE_Y.getInt(screen);
        } catch (IllegalAccessException e) {
            return null;
        }
        Component title = screen.getTitle();
        Font font = Minecraft.getInstance().font;
        int width = font.width(title);
        int imageWidth = screen.getXSize();

        List<Plaque> plaques = tex.plaquesAt(titleY + 3);
        Result result = TitlePlaqueLayout.layout(plaques, titleX, width, imageWidth, MARGIN);
        if (result.plaque() == null) return null;

        Label label = null;
        if (result.maxTextWidth() >= 0) {
            Component cut = truncate(font, title, result.maxTextWidth());
            int cutWidth = font.width(cut);
            int x = result.centered() ? (imageWidth - cutWidth) / 2 : titleX;
            result = TitlePlaqueLayout.layout(plaques, x, cutWidth, imageWidth, MARGIN);
            label = new Label(cut, x);
        }
        if (result.stretched()) {
            draw(pose, screen.getGuiLeft(), screen.getGuiTop(), location, tex, result.blits());
            if (CEConfig.GUI_TITLE.logOverflow.get()) {
                OverflowLog.record(screen, location, title, width, result.plaque(), label != null);
            }
        }
        return label;
    }

    private static void draw(PoseStack pose, int left, int top, ResourceLocation location, Texture tex, List<Blit> blits) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, location);
        int s = tex.scale;
        for (Blit b : blits) {
            GuiComponent.blit(pose, left + b.dstX(), top + b.dstY(), b.w(), b.h(),
                    b.srcX() * s, b.srcY() * s, b.w() * s, b.h() * s, tex.width, tex.height);
        }
    }

    /** The longest prefix of {@code title} that fits in {@code maxWidth} together with "...", styles kept. */
    private static Component truncate(Font font, Component title, int maxWidth) {
        FormattedText head = font.substrByWidth(title, Math.max(0, maxWidth - font.width(ELLIPSIS)));
        MutableComponent out = Component.empty();
        head.visit((style, text) -> {
            out.append(Component.literal(text).withStyle(style));
            return Optional.empty();
        }, title.getStyle());
        return out.append(Component.literal(ELLIPSIS).withStyle(title.getStyle()));
    }

    // ---- screen filter ------------------------------------------------------------------------

    /** True when no class between the screen and AbstractContainerScreen overrides renderLabels. */
    private static final ClassValue<Boolean> USES_VANILLA_LABELS = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                for (Class<?> c = type; c != null && c != AbstractContainerScreen.class; c = c.getSuperclass()) {
                    for (var m : c.getDeclaredMethods()) {
                        String name = m.getName();
                        if ((name.equals("m_7027_") || name.equals("renderLabels")) && m.getParameterCount() == 3
                                && m.getParameterTypes()[0] == PoseStack.class) {
                            return false;
                        }
                    }
                }
                return true;
            } catch (Throwable t) { // a modded screen whose methods reference a missing optional class
                return false;
            }
        }
    };

    // ---- texture cache ------------------------------------------------------------------------

    private static final Map<ResourceLocation, Optional<Texture>> TEXTURES = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> TEXTURES.clear());
    }

    private static Texture texture(ResourceLocation location) {
        return TEXTURES.computeIfAbsent(location, TitlePlaques::load).orElse(null);
    }

    private static Optional<Texture> load(ResourceLocation location) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) return Optional.empty();
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            int w = image.getWidth(), h = image.getHeight();
            int[] argb = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int abgr = image.getPixelRGBA(x, y); // NativeImage stores ABGR despite the name
                    argb[y * w + x] = (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >> 16) & 0xFF);
                }
            }
            return Optional.of(new Texture(w, h, argb));
        } catch (Exception e) {
            LOGGER.debug("Title plaque: could not read {}", location, e);
            return Optional.empty();
        }
    }

    private static final class Texture {
        final int width, height;
        /** Texture pixels per GUI pixel; container backgrounds are 256 GUI pixels tall. */
        final int scale;
        final int[] argb;
        final Map<Integer, List<Plaque>> rows = new HashMap<>();

        Texture(int width, int height, int[] argb) {
            this.width = width;
            this.height = height;
            this.argb = argb;
            this.scale = height >= 256 && height % 256 == 0 ? height / 256 : 1;
        }

        /** Plaques crossing GUI row {@code guiRow}, in GUI pixels. */
        List<Plaque> plaquesAt(int guiRow) {
            return rows.computeIfAbsent(guiRow, row -> {
                List<Plaque> found = TitlePlaqueLayout.findPlaques(
                        (x, y) -> argb[y * width + x], width, height, row * scale + scale / 2);
                List<Plaque> out = new ArrayList<>(found.size());
                for (Plaque p : found) out.add(p.scaledDown(scale));
                return out;
            });
        }
    }

    // ---- overflow log -------------------------------------------------------------------------

    /** Every title that needed a stretch, for finding the screens involved without opening each one. */
    private static final class OverflowLog {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        private static Map<String, JsonObject> entries;

        static void record(AbstractContainerScreen<?> screen, ResourceLocation texture, Component title,
                           int width, Plaque plaque, boolean truncated) {
            if (entries == null) entries = read();
            String text = title.getString();
            String key = screen.getClass().getName() + "|" + texture + "|" + text;
            JsonObject existing = entries.get(key);
            if (existing != null && existing.get("truncated").getAsBoolean() == truncated) return;

            JsonObject e = new JsonObject();
            e.addProperty("title", text);
            e.addProperty("screen", screen.getClass().getName());
            e.addProperty("texture", texture.toString());
            e.addProperty("titleWidth", width);
            e.addProperty("plaqueWidth", plaque.x1() - plaque.x0());
            e.addProperty("truncated", truncated);
            e.addProperty("firstSeen", existing != null ? existing.get("firstSeen").getAsString() : LocalDate.now().toString());
            entries.put(key, e);
            LOGGER.info("Title plaque: stretched {} for \"{}\" ({}px on a {}px plaque{})", texture, text, width,
                    plaque.x1() - plaque.x0(), truncated ? ", truncated" : "");
            write();
        }

        private static Path path() {
            return FMLPaths.GAMEDIR.get().resolve(LOG_FILE);
        }

        private static Map<String, JsonObject> read() {
            Map<String, JsonObject> out = new LinkedHashMap<>();
            Path path = path();
            if (!Files.isRegularFile(path)) return out;
            try (Reader reader = Files.newBufferedReader(path)) {
                for (JsonElement el : GSON.fromJson(reader, JsonArray.class)) {
                    JsonObject o = el.getAsJsonObject();
                    out.put(o.get("screen").getAsString() + "|" + o.get("texture").getAsString() + "|"
                            + o.get("title").getAsString(), o);
                }
            } catch (Exception e) {
                LOGGER.warn("Title plaque: could not read {}, starting a new log", path, e);
            }
            return out;
        }

        private static void write() {
            Path path = path();
            JsonArray array = new JsonArray();
            entries.values().stream()
                    .sorted((a, b) -> Integer.compare(b.get("titleWidth").getAsInt(), a.get("titleWidth").getAsInt()))
                    .forEach(array::add);
            try {
                Files.createDirectories(path.getParent());
                try (Writer writer = Files.newBufferedWriter(path)) {
                    GSON.toJson(array, writer);
                }
            } catch (Exception e) {
                LOGGER.warn("Title plaque: could not write {}", path, e);
            }
        }
    }
}
