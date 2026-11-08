// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/TempadClient.kt (renderType).
// Translated to Java for Forge 1.19.2: a RenderType subclass only to reach the protected render state shards.
package earth.terrarium.tempad.client.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TimedoorRenderTypes extends RenderType {
    @Nullable
    public static ShaderInstance timedoorShader;

    /** Upstream builds a new RenderType per call; the result is the same, so one per texture is kept. */
    private static final Map<ResourceLocation, RenderType> TEXTURED = new ConcurrentHashMap<>();
    private static RenderType shaderType;

    private TimedoorRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }

    /** Null texture: the rendertype_timedoor shader; a texture: position_tex_color (a shader pack is on). */
    public static RenderType renderType(@Nullable ResourceLocation textureId) {
        if (textureId == null) {
            if (shaderType == null) shaderType = build(null);
            return shaderType;
        }
        return TEXTURED.computeIfAbsent(textureId, TimedoorRenderTypes::build);
    }

    private static RenderType build(@Nullable ResourceLocation textureId) {
        CompositeState.CompositeStateBuilder builder = CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLayeringState(NO_LAYERING)
                .setShaderState(new ShaderStateShard(() -> textureId != null ? GameRenderer.getPositionTexColorShader() : timedoorShader))
                .setOutputState(PARTICLES_TARGET);
        if (textureId != null) builder.setTextureState(new TextureStateShard(textureId, false, true));
        return create("timedoor",
                textureId != null ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, false, true, builder.createCompositeState(true));
    }

    /** Shaders are reloaded with the resources; the cached shader-backed type reads the field lazily, nothing to reset. */
    public static void clearTextures() {
        TEXTURED.clear();
    }
}
