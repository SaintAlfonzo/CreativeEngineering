// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/entity/TimedoorRenderer.kt.
// Translated to Java for Forge 1.19.2: vertices are written in each format's element order with endVertex() (1.21's
// builder takes them in any order), com.mojang.math matrices instead of JOML.
package earth.terrarium.tempad.client.entity;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.sizing.TimedoorPlacementSettings;
import earth.terrarium.tempad.client.ShaderModBridge;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimedoorRenderer extends EntityRenderer<TimedoorEntity> {
    private record FaceKey(TimedoorPlacementSettings sizing, BoxFace face) {
    }

    private static final Map<FaceKey, ResourceLocation> faceTextures = new HashMap<>();

    public TimedoorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(TimedoorEntity entity) {
        return Tempad.id("");
    }

    @Override
    public void render(TimedoorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int tickCount = entity.tickCount + entity.getAnimationOffset();
        if (tickCount < TimedoorEntity.IDLE_BEFORE_START) return;
        int tickLength = TimedoorEntity.ANIMATION_LENGTH;
        float animation;
        float ticks = tickCount + partialTick;

        if (entity.getClosingTime() < TimedoorEntity.ANIMATION_LENGTH) {
            if (entity.beganClosing == 0) entity.beganClosing = tickCount;
            animation = Mth.clamp(1 - (ticks - entity.beganClosing) / (float) tickLength, 0f, 1f);
        } else {
            animation = Mth.clamp((ticks - TimedoorEntity.IDLE_BEFORE_START) / (float) tickLength, 0f, 1f);
        }

        TimedoorPlacementSettings sizing = entity.getSizing();
        float width = sizing.widthAtPercent(animation);
        float height = sizing.heightAtPercent(animation);
        float depth = sizing.depthAtPercent(animation);
        float finalHeight = sizing.getDimensions().height;

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YN.rotationDegrees(entity.getYRot() + 180));
        poseStack.translate(width / -2.0, finalHeight / 2.0 - height / 2.0 + 0.01, depth / -2.0);
        // Upstream jitters with the entity's own random; 1.19.2 Entity does not expose it, the level's is used.
        if (entity.isGlitching() && ticks % 65 > 60) {
            float rx = Mth.randomBetween(entity.level.getRandom(), -0.05f, 0.05f);
            float ry = Mth.randomBetween(entity.level.getRandom(), -0.05f, 0.05f);
            float rz = Mth.randomBetween(entity.level.getRandom(), -0.05f, 0.05f);
            poseStack.translate(rx, ry, rz);
        }

        if (width > 0) {
            renderTimedoor(sizing, poseStack, buffer, width, height, depth, entity.getColor().getValue(), tickCount, sizing.showLineAnimation());
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static NativeImage texture(TimedoorPlacementSettings sizing, BoxFace face) {
        int[] dims = face.getDimensions(sizing);
        int textureWidth = dims[0], textureHeight = dims[1];
        NativeImage image = new NativeImage(textureWidth, textureHeight, true);
        for (int x = 0; x < textureWidth; x++) {
            for (int y = 0; y < textureHeight; y++) {
                float x2 = x * 2f / textureWidth - 1;
                float y2 = y * 2f / textureHeight - 1;
                float alpha = x2 * x2 / 3 + y2 * y2 / 3;
                float modAlpha = alpha - (alpha % 0.07f);
                image.setPixelRGBA(x, y, (((int) (modAlpha * 255) + 26) << 24) | 0xFFFFFF);
            }
        }
        return image;
    }

    @Nullable
    private static ResourceLocation registerFaceTexture(TimedoorPlacementSettings sizing, BoxFace face) {
        if (!ShaderModBridge.shadersEnabled()) return null;
        return faceTextures.computeIfAbsent(new FaceKey(sizing, face), key -> Minecraft.getInstance().getTextureManager()
                .register(sizing.getType().id().getPath() + face.name().toLowerCase(Locale.ROOT), new DynamicTexture(texture(sizing, face))));
    }

    /** One quad vertex in the buffer's element order: lightmap-format (shader) or position_tex_color (shader pack). */
    private static void quadVertex(VertexConsumer buffer, Matrix4f model, boolean textured, float x, float y, float z, float r, float g, float b, float u, float v, float sizeU, float sizeV) {
        buffer.vertex(model, x, y, z);
        if (textured) {
            buffer.uv(u, v).color(r, g, b, 1f);
        } else {
            buffer.color(r, g, b, 1f).uv(u, v).uv2((int) (sizeU * 16), (int) (sizeV * 16));
        }
        buffer.endVertex();
    }

    private static void lineVertex(VertexConsumer buffer, Matrix4f model, Matrix3f normal, float x, float y, float z, int color, float nx, float ny, float nz) {
        buffer.vertex(model, x, y, z).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF).normal(normal, nx, ny, nz).endVertex();
    }

    public void renderTimedoor(TimedoorPlacementSettings sizing, PoseStack poseStack, MultiBufferSource source, float width, float height, float depth, int color, int age, boolean animate) {
        float maxX = width, maxY = height, maxZ = depth;
        float minX = 0f, minY = 0f, minZ = 0f;
        Matrix4f model = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        int lineColor = 0xFF000000 | (color & 0xFFFFFF);
        int white = 0xFFFFFFFF;
        boolean shaders = ShaderModBridge.shadersEnabled();

        ResourceLocation tex = registerFaceTexture(sizing, BoxFace.FrontBack);
        VertexConsumer buffer = source.getBuffer(TimedoorRenderTypes.renderType(tex));
        boolean textured = tex != null;
        // Front
        quadVertex(buffer, model, textured, minX, maxY, minZ, red, green, blue, 0f, 1f, minX, maxY);
        quadVertex(buffer, model, textured, maxX, maxY, minZ, red, green, blue, 1f, 1f, maxX, maxY);
        quadVertex(buffer, model, textured, maxX, minY, minZ, red, green, blue, 1f, 0f, maxX, minY);
        quadVertex(buffer, model, textured, minX, minY, minZ, red, green, blue, 0f, 0f, minX, minY);
        // Back
        quadVertex(buffer, model, textured, maxX, maxY, maxZ, red, green, blue, 1f, 1f, maxX, maxY);
        quadVertex(buffer, model, textured, minX, maxY, maxZ, red, green, blue, 0f, 1f, minX, maxY);
        quadVertex(buffer, model, textured, minX, minY, maxZ, red, green, blue, 0f, 0f, minX, minY);
        quadVertex(buffer, model, textured, maxX, minY, maxZ, red, green, blue, 1f, 0f, maxX, minY);

        if (shaders) {
            tex = registerFaceTexture(sizing, BoxFace.TopBottom);
            buffer = source.getBuffer(TimedoorRenderTypes.renderType(tex));
            textured = tex != null;
        }
        // Top
        quadVertex(buffer, model, textured, minX, maxY, maxZ, red, green, blue, 0f, 1f, minX, maxZ);
        quadVertex(buffer, model, textured, maxX, maxY, maxZ, red, green, blue, 1f, 1f, maxX, maxZ);
        quadVertex(buffer, model, textured, maxX, maxY, minZ, red, green, blue, 1f, 0f, maxX, minZ);
        quadVertex(buffer, model, textured, minX, maxY, minZ, red, green, blue, 0f, 0f, minX, minZ);
        // Bottom
        quadVertex(buffer, model, textured, minX, minY, minZ, red, green, blue, 0f, 0f, minX, minZ);
        quadVertex(buffer, model, textured, maxX, minY, minZ, red, green, blue, 1f, 0f, maxX, minZ);
        quadVertex(buffer, model, textured, maxX, minY, maxZ, red, green, blue, 1f, 1f, maxX, maxZ);
        quadVertex(buffer, model, textured, minX, minY, maxZ, red, green, blue, 0f, 1f, minX, maxZ);

        if (shaders) {
            tex = registerFaceTexture(sizing, BoxFace.LeftRight);
            buffer = source.getBuffer(TimedoorRenderTypes.renderType(tex));
            textured = tex != null;
        }
        // Left
        quadVertex(buffer, model, textured, minX, maxY, maxZ, red, green, blue, 1f, 1f, maxZ, maxY);
        quadVertex(buffer, model, textured, minX, maxY, minZ, red, green, blue, 0f, 1f, minZ, maxY);
        quadVertex(buffer, model, textured, minX, minY, minZ, red, green, blue, 0f, 0f, minZ, minY);
        quadVertex(buffer, model, textured, minX, minY, maxZ, red, green, blue, 1f, 0f, maxZ, minY);
        // Right
        quadVertex(buffer, model, textured, maxX, maxY, minZ, red, green, blue, 0f, 1f, minZ, maxY);
        quadVertex(buffer, model, textured, maxX, maxY, maxZ, red, green, blue, 1f, 1f, maxZ, maxY);
        quadVertex(buffer, model, textured, maxX, minY, maxZ, red, green, blue, 1f, 0f, maxZ, minY);
        quadVertex(buffer, model, textured, maxX, minY, minZ, red, green, blue, 0f, 0f, minZ, minY);

        VertexConsumer lines = source.getBuffer(RenderType.lines());

        float widthPart = width * 20f;
        float heightPart = height * 20f;
        float total = widthPart * 2 + heightPart * 2;
        float widthLine = (total / widthPart) * .0625f;
        float heightLine = (total / heightPart) * .0625f;
        float topPercent, rightPercent, bottomPercent, leftPercent;
        if (widthPart > heightPart) {
            topPercent = 1 - (((age + widthPart) % total) - widthPart) / (widthPart - (widthPart * widthLine * 1.5f)) + widthLine;
            rightPercent = 1 - (age % total - widthPart) / heightPart;
            bottomPercent = 1 - (age % total - widthPart - heightPart) / (widthPart - (widthPart * widthLine * 1.5f)) + widthLine;
            leftPercent = 1 - ((age - widthPart) % total - widthPart - heightPart) / heightPart;
        } else {
            topPercent = 1 - (((age + widthPart) % total) - widthPart) / widthPart;
            rightPercent = 1 - (((age + widthPart) % total) - widthPart * 2) / (heightPart - (heightPart * heightLine * 1.5f)) + heightLine;
            bottomPercent = 1 - (age % total - widthPart - heightPart) / widthPart;
            leftPercent = 1 - ((age - widthPart) % total - widthPart - heightPart) / (heightPart - (heightPart * heightLine * 1.5f)) + heightLine;
        }

        // front: top edge
        lineVertex(lines, model, normal, minX, maxY, minZ, lineColor, -1f, 0f, 0f);
        if (animate && topPercent > -widthLine && topPercent < (1 + widthLine)) {
            float start = Mth.clamp(topPercent - widthLine, 0f, 1f);
            float end = Mth.clamp(topPercent + widthLine, 0f, 1f);
            float middle = Mth.clamp(topPercent, 0f, 1f);
            lineVertex(lines, model, normal, Mth.lerp(start, 0f, maxX), maxY, minZ, lineColor, -1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(start, 0f, maxX), maxY, minZ, lineColor, -1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(middle, 0f, maxX), maxY, minZ, white, -1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(middle, 0f, maxX), maxY, minZ, white, -1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(end, 0f, maxX), maxY, minZ, lineColor, -1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(end, 0f, maxX), maxY, minZ, lineColor, -1f, 0f, 0f);
        }
        lineVertex(lines, model, normal, maxX, maxY, minZ, lineColor, -1f, 0f, 0f);

        // front: left edge
        lineVertex(lines, model, normal, minX, minY, minZ, lineColor, 0f, 1f, 0f);
        if (animate && rightPercent > -heightLine && rightPercent < (1 + heightLine)) {
            float start = Mth.clamp(rightPercent - heightLine, 0f, 1f);
            float end = Mth.clamp(rightPercent + heightLine, 0f, 1f);
            float middle = Mth.clamp(rightPercent, 0f, 1f);
            lineVertex(lines, model, normal, minX, Mth.lerp(start, 0f, maxY), minZ, lineColor, 0f, 1f, 0f);
            lineVertex(lines, model, normal, minX, Mth.lerp(start, 0f, maxY), minZ, lineColor, 0f, 1f, 0f);
            lineVertex(lines, model, normal, minX, Mth.lerp(middle, 0f, maxY), minZ, white, 0f, 1f, 0f);
            lineVertex(lines, model, normal, minX, Mth.lerp(middle, 0f, maxY), minZ, white, 0f, 1f, 0f);
            lineVertex(lines, model, normal, minX, Mth.lerp(end, 0f, maxY), minZ, lineColor, 0f, 1f, 0f);
            lineVertex(lines, model, normal, minX, Mth.lerp(end, 0f, maxY), minZ, lineColor, 0f, 1f, 0f);
        }
        lineVertex(lines, model, normal, minX, maxY, minZ, lineColor, 0f, 1f, 0f);

        // front: bottom edge
        lineVertex(lines, model, normal, minX, minY, minZ, lineColor, 1f, 0f, 0f);
        if (animate && bottomPercent > -widthLine && bottomPercent < (1 + widthLine)) {
            float start = Mth.clamp(bottomPercent + widthLine, 0f, 1f);
            float end = Mth.clamp(bottomPercent - widthLine, 0f, 1f);
            float middle = Mth.clamp(bottomPercent, 0f, 1f);
            lineVertex(lines, model, normal, Mth.lerp(1 - start, 0f, maxX), minY, minZ, lineColor, 1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(1 - start, 0f, maxX), minY, minZ, lineColor, 1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(1 - middle, 0f, maxX), minY, minZ, white, 1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(1 - middle, 0f, maxX), minY, minZ, white, 1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(1 - end, 0f, maxX), minY, minZ, lineColor, 1f, 0f, 0f);
            lineVertex(lines, model, normal, Mth.lerp(1 - end, 0f, maxX), minY, minZ, lineColor, 1f, 0f, 0f);
        }
        lineVertex(lines, model, normal, maxX, minY, minZ, lineColor, 1f, 0f, 0f);

        // front: right edge
        lineVertex(lines, model, normal, maxX, maxY, minZ, lineColor, 0f, 1f, 0f);
        if (animate && leftPercent > -heightLine && leftPercent < (1 + heightLine)) {
            float start = Mth.clamp(leftPercent - heightLine, 0f, 1f);
            float end = Mth.clamp(leftPercent + heightLine, 0f, 1f);
            float middle = Mth.clamp(leftPercent, 0f, 1f);
            lineVertex(lines, model, normal, maxX, Mth.lerp(start, maxY, 0f), minZ, lineColor, 0f, 1f, 0f);
            lineVertex(lines, model, normal, maxX, Mth.lerp(start, maxY, 0f), minZ, lineColor, 0f, 1f, 0f);
            lineVertex(lines, model, normal, maxX, Mth.lerp(middle, maxY, 0f), minZ, white, 0f, 1f, 0f);
            lineVertex(lines, model, normal, maxX, Mth.lerp(middle, maxY, 0f), minZ, white, 0f, 1f, 0f);
            lineVertex(lines, model, normal, maxX, Mth.lerp(end, maxY, 0f), minZ, lineColor, 0f, 1f, 0f);
            lineVertex(lines, model, normal, maxX, Mth.lerp(end, maxY, 0f), minZ, lineColor, 0f, 1f, 0f);
        }
        lineVertex(lines, model, normal, maxX, minY, minZ, lineColor, 0f, 1f, 0f);

        // connecting front and back
        lineVertex(lines, model, normal, minX, minY, minZ, lineColor, 0f, 0f, 1f);
        lineVertex(lines, model, normal, minX, minY, maxZ, lineColor, 0f, 0f, 1f);
        lineVertex(lines, model, normal, maxX, minY, minZ, lineColor, 0f, 0f, -1f);
        lineVertex(lines, model, normal, maxX, minY, maxZ, lineColor, 0f, 0f, -1f);
        lineVertex(lines, model, normal, minX, maxY, minZ, lineColor, 0f, 0f, 1f);
        lineVertex(lines, model, normal, minX, maxY, maxZ, lineColor, 0f, 0f, 1f);
        lineVertex(lines, model, normal, maxX, maxY, minZ, lineColor, 0f, 0f, 1f);
        lineVertex(lines, model, normal, maxX, maxY, maxZ, lineColor, 0f, 0f, 1f);

        // back
        lineVertex(lines, model, normal, minX, maxY, maxZ, lineColor, 0f, -1f, 0f);
        lineVertex(lines, model, normal, minX, minY, maxZ, lineColor, 0f, -1f, 0f);
        lineVertex(lines, model, normal, minX, minY, maxZ, lineColor, 1f, 0f, 0f);
        lineVertex(lines, model, normal, maxX, minY, maxZ, lineColor, 1f, 0f, 0f);
        lineVertex(lines, model, normal, maxX, minY, maxZ, lineColor, 0f, 1f, 0f);
        lineVertex(lines, model, normal, maxX, maxY, maxZ, lineColor, 0f, 1f, 0f);
        lineVertex(lines, model, normal, maxX, maxY, maxZ, lineColor, 1f, 0f, 0f);
        lineVertex(lines, model, normal, minX, maxY, maxZ, lineColor, 1f, 0f, 0f);
    }

    enum BoxFace {
        FrontBack, TopBottom, LeftRight;

        int[] getDimensions(TimedoorPlacementSettings sizing) {
            return switch (this) {
                case FrontBack -> new int[]{(int) (sizing.widthAtPercent(1f) * 16), (int) (sizing.heightAtPercent(1f) * 16)};
                case TopBottom -> new int[]{(int) (sizing.widthAtPercent(1f) * 16), (int) (sizing.depthAtPercent(1f) * 16)};
                case LeftRight -> new int[]{(int) (sizing.depthAtPercent(1f) * 16), (int) (sizing.heightAtPercent(1f) * 16)};
            };
        }
    }
}
