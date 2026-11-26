// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 client/anvilcraft/rendering/FullyBufferedBufferSource.java to Forge 1.19.2. Upstream derived this
// render cache from AnvilCraft (LGPL-3.0-or-later, conveyed here under GPL-3.0, see NOTICE-upstream-MIT.txt); Yuushya
// Modelling itself is MIT. Deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.client.anvilcraft.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FullyBufferedBufferSource extends BufferSource implements AutoCloseable {
   private final Map<RenderType, BufferBuilder> bufferBuilders = new LinkedHashMap<>();
   private final Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap();
   private final Map<RenderType, SortState> meshSorts = new HashMap<>();
   private final Map<RenderType, RenderType> cachedRenderTypeConvertions = new HashMap<>();

   public FullyBufferedBufferSource() {
      super(null, null);
   }

   @Override
   public VertexConsumer getBuffer(RenderType renderType) {
      RenderType actualType = this.forceUseBlockRenderTypes(renderType);
      return (VertexConsumer)this.bufferBuilders.computeIfAbsent(actualType, it -> {
         BufferBuilder bufferBuilder = new BufferBuilder(786432);
         bufferBuilder.begin(it.mode(), it.format());
         return bufferBuilder;
      });
   }

   private RenderType forceUseBlockRenderTypes(RenderType renderType) {
      return renderType.format() == DefaultVertexFormat.NEW_ENTITY && renderType instanceof CompositeRenderType compositeRenderType
         ? this.cachedRenderTypeConvertions.computeIfAbsent(renderType, it -> this.createBlockRenderType(compositeRenderType))
         : renderType;
   }

   private RenderType createBlockRenderType(CompositeRenderType renderType) {
      CompositeState state = renderType.state();
      return RenderType.create(
         "yuushya_modelling:generated",
         DefaultVertexFormat.BLOCK,
         renderType.mode(),
         786432,
         renderType.affectsCrumbling(),
         renderType.sortOnUpload,
         CompositeState.builder()
            .setCullState(state.cullState)
            .setOutputState(RenderStateShard.MAIN_TARGET)
            .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
            .setTextureState(state.textureState)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setTransparencyState(state.transparencyState)
            .createCompositeState(false)
      );
   }

   public boolean isEmpty() {
      return this.bufferBuilders.isEmpty() ? true : this.bufferBuilders.values().stream().noneMatch(it -> it.vertices > 0);
   }

   @Override
   public void endBatch(RenderType renderType) {
   }

   @Override
   public void endLastBatch() {
   }

   @Override
   public void endBatch() {
   }

   public void upload(Function<RenderType, VertexBuffer> vertexBufferGetter, Consumer<Runnable> runner) {
      for (RenderType renderType : new ArrayList<>(this.bufferBuilders.keySet())) {
         BufferBuilder bufferBuilder = this.bufferBuilders.get(renderType);
         if (bufferBuilder != null) {
            int vertexCount = bufferBuilder.vertices;
            int indexCount = renderType.mode().indexCount(vertexCount);
            runner.accept(() -> {
               if (vertexCount <= 0) {
                  bufferBuilder.end().release();
                  this.bufferBuilders.remove(renderType);
               } else {
                  RenderedBuffer mesh = bufferBuilder.end();
                  this.indexCountMap.put(renderType, indexCount);
                  VertexBuffer vertexBuffer = vertexBufferGetter.apply(renderType);
                  vertexBuffer.bind();
                  vertexBuffer.upload(mesh);
                  VertexBuffer.unbind();
                  this.bufferBuilders.remove(renderType);
               }
            });
         }
      }
   }

   @Override
   public void close() {
      this.bufferBuilders.clear();
   }

   public Reference2IntMap<RenderType> getIndexCountMap() {
      return this.indexCountMap;
   }

   public Map<RenderType, SortState> getMeshSorts() {
      return this.meshSorts;
   }
}
