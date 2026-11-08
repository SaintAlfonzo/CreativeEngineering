// Port glue (ours, GPLv3), TP-D4: 1.21's GuiGraphics.blitSprite for Forge 1.19.2, which has no GUI sprite atlas.
package earth.terrarium.tempad.client.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A 1.21 GUI sprite id {@code ns:path} is the texture {@code ns:textures/gui/sprites/path.png} (served by the in-memory
 * upstream pack). 1.21 reads the sprite's scaling from {@code path.png.mcmeta} ({@code gui.scaling}): "stretch" (the
 * default), "tile" or "nine_slice" (border, edges and centre tiled). This class reads the same metadata and draws the
 * same way with plain texture blits. Cleared on every resource reload.
 */
public final class Sprites {
    private record Info(int texWidth, int texHeight, String type, int width, int height, int left, int top, int right, int bottom) {
    }

    private static final MetadataSectionSerializer<JsonObject> GUI = new MetadataSectionSerializer<>() {
        @Override
        public String getMetadataSectionName() {
            return "gui";
        }

        @Override
        public JsonObject fromJson(JsonObject json) {
            return json;
        }
    };

    private static final Map<ResourceLocation, Info> CACHE = new ConcurrentHashMap<>();

    private Sprites() {
    }

    public static void clear() {
        CACHE.clear();
    }

    public static ResourceLocation texture(ResourceLocation sprite) {
        return new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
    }

    private static Info info(ResourceLocation sprite) {
        return CACHE.computeIfAbsent(sprite, Sprites::load);
    }

    private static Info load(ResourceLocation sprite) {
        ResourceLocation tex = texture(sprite);
        int w = 16, h = 16;
        String type = "stretch";
        int sw = -1, sh = -1, l = 0, t = 0, r = 0, b = 0;
        try {
            Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(tex);
            if (res.isEmpty()) {
                Tempad.LOGGER.warn("Missing GUI sprite {}", tex);
                return new Info(16, 16, "stretch", 16, 16, 0, 0, 0, 0);
            }
            try (InputStream in = res.get().open()) {
                byte[] head = in.readNBytes(24);
                if (head.length == 24) {
                    w = ((head[16] & 0xFF) << 24) | ((head[17] & 0xFF) << 16) | ((head[18] & 0xFF) << 8) | (head[19] & 0xFF);
                    h = ((head[20] & 0xFF) << 24) | ((head[21] & 0xFF) << 16) | ((head[22] & 0xFF) << 8) | (head[23] & 0xFF);
                }
            }
            Optional<JsonObject> gui = res.get().metadata().getSection(GUI);
            if (gui.isPresent() && gui.get().has("scaling")) {
                JsonObject scaling = gui.get().getAsJsonObject("scaling");
                type = scaling.has("type") ? scaling.get("type").getAsString() : "stretch";
                if (scaling.has("width")) sw = scaling.get("width").getAsInt();
                if (scaling.has("height")) sh = scaling.get("height").getAsInt();
                if (scaling.has("border")) {
                    JsonElement border = scaling.get("border");
                    if (border.isJsonObject()) {
                        JsonObject o = border.getAsJsonObject();
                        l = o.get("left").getAsInt();
                        t = o.get("top").getAsInt();
                        r = o.get("right").getAsInt();
                        b = o.get("bottom").getAsInt();
                    } else {
                        l = t = r = b = border.getAsInt();
                    }
                }
            }
        } catch (Exception e) {
            Tempad.LOGGER.warn("Could not read GUI sprite {}: {}", tex, e.toString());
        }
        return new Info(w, h, type, sw < 0 ? w : sw, sh < 0 ? h : sh, l, t, r, b);
    }

    private static void bind(ResourceLocation sprite) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture(sprite));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** GuiGraphics.blitSprite(sprite, x, y, width, height). */
    public static void blit(PoseStack pose, ResourceLocation sprite, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Info info = info(sprite);
        bind(sprite);
        switch (info.type) {
            case "nine_slice" -> nineSlice(pose, info, x, y, width, height);
            case "tile" -> tiled(pose, info, x, y, width, height, 0, 0, info.width, info.height);
            default -> GuiComponent.blit(pose, x, y, width, height, 0, 0, info.texWidth, info.texHeight, info.texWidth, info.texHeight);
        }
    }

    /**
     * GuiGraphics.blitSprite(sprite, spriteWidth, spriteHeight, u, v, x, y, width, height): a width x height part of the
     * sprite at (u, v), in the sprite's own pixel units.
     */
    public static void blit(PoseStack pose, ResourceLocation sprite, int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Info info = info(sprite);
        bind(sprite);
        float sx = info.texWidth / (float) spriteWidth;
        float sy = info.texHeight / (float) spriteHeight;
        GuiComponent.blit(pose, x, y, width, height, u * sx, v * sy, Math.round(width * sx), Math.round(height * sy), info.texWidth, info.texHeight);
    }

    /** Scale from the sprite's logical size (mcmeta width/height) to texture pixels. */
    private static void region(PoseStack pose, Info info, int x, int y, int w, int h, int u, int v, int uw, int vh) {
        if (w <= 0 || h <= 0) return;
        float sx = info.texWidth / (float) info.width;
        float sy = info.texHeight / (float) info.height;
        GuiComponent.blit(pose, x, y, w, h, u * sx, v * sy, Math.round(uw * sx), Math.round(vh * sy), info.texWidth, info.texHeight);
    }

    /** Repeats the (u, v, tileW, tileH) part over the area; the last row and column are cut, as 1.21's blitTiledSprite. */
    private static void tiled(PoseStack pose, Info info, int x, int y, int w, int h, int u, int v, int tileW, int tileH) {
        if (w <= 0 || h <= 0 || tileW <= 0 || tileH <= 0) return;
        for (int dx = 0; dx < w; dx += tileW) {
            int cw = Math.min(tileW, w - dx);
            for (int dy = 0; dy < h; dy += tileH) {
                int ch = Math.min(tileH, h - dy);
                region(pose, info, x + dx, y + dy, cw, ch, u, v, cw, ch);
            }
        }
    }

    /** 1.21 GuiGraphics.blitNineSlicedSprite: corners as they are, edges and centre tiled. */
    private static void nineSlice(PoseStack pose, Info s, int x, int y, int w, int h) {
        int left = Math.min(s.left, w / 2);
        int right = Math.min(s.right, w / 2);
        int top = Math.min(s.top, h / 2);
        int bottom = Math.min(s.bottom, h / 2);
        if (w == s.width && h == s.height) {
            region(pose, s, x, y, w, h, 0, 0, w, h);
            return;
        }
        if (h == s.height) {
            region(pose, s, x, y, left, h, 0, 0, left, h);
            tiled(pose, s, x + left, y, w - right - left, h, left, 0, s.width - right - left, s.height);
            region(pose, s, x + w - right, y, right, h, s.width - right, 0, right, h);
            return;
        }
        if (w == s.width) {
            region(pose, s, x, y, w, top, 0, 0, w, top);
            tiled(pose, s, x, y + top, w, h - bottom - top, 0, top, s.width, s.height - bottom - top);
            region(pose, s, x, y + h - bottom, w, bottom, 0, s.height - bottom, w, bottom);
            return;
        }
        int innerW = s.width - left - right;
        int innerH = s.height - top - bottom;
        region(pose, s, x, y, left, top, 0, 0, left, top);
        tiled(pose, s, x + left, y, w - right - left, top, left, 0, innerW, top);
        region(pose, s, x + w - right, y, right, top, s.width - right, 0, right, top);
        region(pose, s, x, y + h - bottom, left, bottom, 0, s.height - bottom, left, bottom);
        tiled(pose, s, x + left, y + h - bottom, w - right - left, bottom, left, s.height - bottom, innerW, bottom);
        region(pose, s, x + w - right, y + h - bottom, right, bottom, s.width - right, s.height - bottom, right, bottom);
        tiled(pose, s, x, y + top, left, h - bottom - top, 0, top, left, innerH);
        tiled(pose, s, x + left, y + top, w - right - left, h - bottom - top, left, top, innerW, innerH);
        tiled(pose, s, x + w - right, y + top, right, h - bottom - top, s.width - right, top, right, innerH);
    }
}
