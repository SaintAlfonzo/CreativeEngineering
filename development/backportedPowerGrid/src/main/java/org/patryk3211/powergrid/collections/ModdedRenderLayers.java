/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.collections;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

// Port: 1.19.2's render state shards are protected (upstream reads them through access wideners); extending RenderType
// gives access. The class is never instantiated.
public class ModdedRenderLayers extends RenderType {
    private ModdedRenderLayers(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }

    private static final RenderType DEBUG_LINES = RenderType.create(
            "powergrid_debug_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.DEBUG_LINES,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setCullState(NO_CULL)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLineState(new LineStateShard(OptionalDouble.of(1.0f)))
                    .createCompositeState(false)
    );

    private static final RenderType COLOR = RenderType.create(
            "powergrid_color",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setCullState(NO_CULL)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .createCompositeState(false)
    );

    private static final RenderType ADDITIVE_CRT = RenderType.create(
            "powergrid_additive_crt",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setCullState(NO_CULL)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setOutputState(PARTICLES_TARGET)
                    .createCompositeState(false)
    );

    private static final RenderType ADDITIVE_COLOR = RenderType.create(
            "powergrid_additive_color",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setCullState(NO_CULL)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setOutputState(PARTICLES_TARGET)
                    .createCompositeState(false)
    );

    private static final RenderType ADDITIVE = RenderType.create("powergrid_additive", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, RenderType.SMALL_BUFFER_SIZE, true, true, RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(BLOCK_SHEET)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .createCompositeState(true));

    public static RenderType getDebugLines() {
        return DEBUG_LINES;
    }

    public static RenderType getColor() {
        return COLOR;
    }

    public static RenderType getAdditiveCrt() {
        return ADDITIVE_CRT;
    }

    public static RenderType getAdditiveColor() {
        return ADDITIVE_COLOR;
    }

    public static RenderType getAdditive() {
        return ADDITIVE;
    }

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }
}
