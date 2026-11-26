// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 client/anvilcraft/rendering/CachedRegion.java to Forge 1.19.2. Upstream derived this
// render cache from AnvilCraft (LGPL-3.0-or-later, conveyed here under GPL-3.0, see NOTICE-upstream-MIT.txt); Yuushya
// Modelling itself is MIT. Deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.client.anvilcraft.rendering;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Vector3f;
import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import com.yuushya.modelling.client.NeoItemBlockModel;
import com.yuushya.modelling.utils.YuushyaDataTags;
import com.yuushya.modelling.utils.YuushyaUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer.AmbientOcclusionFace;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IQuadTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Matrix4f;

public class CachedRegion {
   public static final RenderType TRANSLUCENT_MAIN = RenderType.create(
      "translucent_main", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 786432, true, true, translucentState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
   );
   private static final Direction[] DIRECTIONS = Direction.values();
   private final ChunkPos chunkPos;
   private final Set<BlockEntity> blockEntities = new HashSet<>();
   private final CacheableBERenderingPipeline pipeline;
   private final Minecraft minecraft = Minecraft.getInstance();
   private final RandomSource random = RandomSource.create();
   private Map<RenderType, VertexBuffer> buffers = new HashMap<>();
   private Map<RenderType, SortState> meshSortings = new HashMap<>();
   private Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap();
   private AmbientOcclusionFace aoFace = new AmbientOcclusionFace();
   @Nullable
   private CachedRegion.RebuildTask lastRebuildTask;
   private boolean isEmpty = true;

   public CachedRegion(ChunkPos chunkPos, CacheableBERenderingPipeline pipeline) {
      this.chunkPos = chunkPos;
      this.pipeline = pipeline;
   }

   private static CompositeState translucentState(ShaderStateShard state) {
      return CompositeState.builder()
         .setLightmapState(RenderStateShard.LIGHTMAP)
         .setShaderState(state)
         .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .setOutputState(RenderStateShard.MAIN_TARGET)
         .createCompositeState(true);
   }

   @NotNull
   private static float[] getColorComponents(TransformItemData transformData, BakedModel model, BakedQuad bakedQuad) {
      Color color = new Color(transformData.color);
      if (model instanceof NeoItemBlockModel) {
         int[] vertices = bakedQuad.getVertices();
         color = new Color(NeoItemBlockModel.toABGR(vertices[IQuadTransformer.STRIDE + IQuadTransformer.COLOR]));
      }

      float[] colorComponents = new float[3];
      color.getColorComponents(colorComponents);
      return colorComponents;
   }

   public void update(BlockEntity be) {
      if (this.lastRebuildTask != null) {
         this.lastRebuildTask.cancel();
      }

      boolean shouldRecompile = this.blockEntities.removeIf(BlockEntity::isRemoved);
      if (be.isRemoved()) {
         shouldRecompile |= this.blockEntities.remove(be);
         if (shouldRecompile) {
            this.pipeline.submitCompileTask(new CachedRegion.RebuildTask());
         }
      } else {
         shouldRecompile |= this.blockEntities.add(be);
         if (shouldRecompile) {
            this.pipeline.submitCompileTask(new CachedRegion.RebuildTask());
         }
      }
   }

   public void blockRemoved(BlockEntity be) {
      if (this.lastRebuildTask != null) {
         this.lastRebuildTask.cancel();
      }

      boolean removedAny = this.blockEntities.removeIf(BlockEntity::isRemoved) || this.blockEntities.remove(be);
      if (removedAny) {
         this.pipeline.submitCompileTask(new CachedRegion.RebuildTask());
      }
   }

   public void render(Matrix4f frustumMatrix, Matrix4f projectionMatrix) {
      this.renderInternal(frustumMatrix, projectionMatrix, this.buffers.keySet());
   }

   public VertexBuffer getBuffer(RenderType renderType) {
      if (this.buffers.containsKey(renderType)) {
         return this.buffers.get(renderType);
      } else {
         VertexBuffer vb = new VertexBuffer();
         this.buffers.put(renderType, vb);
         return vb;
      }
   }

   private void renderInternal(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Collection<RenderType> renderTypes) {
      if (!this.isEmpty) {
         RenderSystem.enableBlend();
         Window window = Minecraft.getInstance().getWindow();
         Vec3 cameraPosition = this.minecraft.gameRenderer.getMainCamera().getPosition();
         int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
         if (!(
            cameraPosition.distanceTo(new Vec3((double)(this.chunkPos.x * 16), cameraPosition.y, (double)(this.chunkPos.z * 16)))
               > (double)renderDistance
         )) {
            for (RenderType renderType : new ArrayList<>(renderTypes)) {
               VertexBuffer vb = this.buffers.get(renderType);
               if (vb != null) {
                  this.renderLayer(renderType, vb, frustumMatrix, projectionMatrix, cameraPosition, window);
               }
            }
         }
      }
   }

   public void releaseBuffers() {
      this.buffers.values().forEach(VertexBuffer::close);
   }

   private void renderLayer(
      RenderType renderType, VertexBuffer vertexBuffer, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Vec3 cameraPosition, Window window
   ) {
      int indexCount = this.indexCountMap.getInt(renderType);
      if (indexCount > 0) {
         renderType.setupRenderState();
         ShaderInstance shader = RenderSystem.getShader();
         Uniform uniform = shader.CHUNK_OFFSET;
         if (uniform != null) {
            uniform.set(
               (float)((double)this.chunkPos.getMinBlockX() - cameraPosition.x),
               (float)(-cameraPosition.y),
               (float)((double)this.chunkPos.getMinBlockZ() - cameraPosition.z)
            );
         }

         vertexBuffer.bind();
         vertexBuffer.drawWithShader(frustumMatrix, projectionMatrix, shader);
         VertexBuffer.unbind();
         if (uniform != null) {
            uniform.set(0.0F, 0.0F, 0.0F);
         }

         renderType.clearRenderState();
      }
   }

   public void replaceData(Collection<BlockPos> entityPos, ClientLevel clientLevel) {
      List<BlockEntity> blockEntities = entityPos.stream().<BlockEntity>map(clientLevel::getBlockEntity).filter(Objects::nonNull).toList();
      this.blockEntities.clear();
      this.blockEntities.addAll(blockEntities);
      this.pipeline.submitCompileTask(new CachedRegion.RebuildTask());
   }

   public void forcedUpdate() {
      this.pipeline.submitCompileTask(new CachedRegion.RebuildTask());
   }

   public <E extends BlockEntity> void addIfPossible(E blockEntity) {
      if (!this.blockEntities.contains(blockEntity)) {
         this.blockEntities.add(blockEntity);
         this.pipeline.submitCompileTask(new CachedRegion.RebuildTask());
      }
   }

   private void calculateShape(
      BlockAndTintGetter level,
      BlockState state,
      BlockPos pos,
      int[] vertices,
      Direction direction,
      @javax.annotation.Nullable float[] shape,
      BitSet shapeFlags
   ) {
      float f = 32.0F;
      float f1 = 32.0F;
      float f2 = 32.0F;
      float f3 = -32.0F;
      float f4 = -32.0F;
      float f5 = -32.0F;

      for (int i = 0; i < 4; i++) {
         float f6 = Float.intBitsToFloat(vertices[i * 8]);
         float f7 = Float.intBitsToFloat(vertices[i * 8 + 1]);
         float f8 = Float.intBitsToFloat(vertices[i * 8 + 2]);
         f = Math.min(f, f6);
         f1 = Math.min(f1, f7);
         f2 = Math.min(f2, f8);
         f3 = Math.max(f3, f6);
         f4 = Math.max(f4, f7);
         f5 = Math.max(f5, f8);
      }

      if (shape != null) {
         shape[Direction.WEST.get3DDataValue()] = f;
         shape[Direction.EAST.get3DDataValue()] = f3;
         shape[Direction.DOWN.get3DDataValue()] = f1;
         shape[Direction.UP.get3DDataValue()] = f4;
         shape[Direction.NORTH.get3DDataValue()] = f2;
         shape[Direction.SOUTH.get3DDataValue()] = f5;
         int j = DIRECTIONS.length;
         shape[Direction.WEST.get3DDataValue() + j] = 1.0F - f;
         shape[Direction.EAST.get3DDataValue() + j] = 1.0F - f3;
         shape[Direction.DOWN.get3DDataValue() + j] = 1.0F - f1;
         shape[Direction.UP.get3DDataValue() + j] = 1.0F - f4;
         shape[Direction.NORTH.get3DDataValue() + j] = 1.0F - f2;
         shape[Direction.SOUTH.get3DDataValue() + j] = 1.0F - f5;
      }

      float f9 = 1.0E-4F;
      float f10 = 0.9999F;
      switch (direction) {
         case DOWN:
            shapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            shapeFlags.set(0, f1 == f4 && (f1 < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos)));
            break;
         case UP:
            shapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            shapeFlags.set(0, f1 == f4 && (f4 > 0.9999F || state.isCollisionShapeFullBlock(level, pos)));
            break;
         case NORTH:
            shapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            shapeFlags.set(0, f2 == f5 && (f2 < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos)));
            break;
         case SOUTH:
            shapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            shapeFlags.set(0, f2 == f5 && (f5 > 0.9999F || state.isCollisionShapeFullBlock(level, pos)));
            break;
         case WEST:
            shapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            shapeFlags.set(0, f == f3 && (f < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos)));
            break;
         case EAST:
            shapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            shapeFlags.set(0, f == f3 && (f3 > 0.9999F || state.isCollisionShapeFullBlock(level, pos)));
      }
   }

   private class RebuildTask implements Runnable {
      private boolean cancelled = false;

      @Override
      public void run() {
         CachedRegion.this.lastRebuildTask = this;
         int vertexSize = YuushyaUtils.vertexSize();
         PoseStack poseStack = new PoseStack();
         CachedRegion.this.isEmpty = true;
         FullyBufferedBufferSource bufferSource = new FullyBufferedBufferSource();
         Minecraft mc = Minecraft.getInstance();

         for (BlockEntity be : new ArrayList<>(CachedRegion.this.blockEntities)) {
            if (be instanceof ItemBlockEntity itemBlockEntity) {
               LocalPlayer localPlayer = mc.player;
               if (be.getLevel() == null || localPlayer == null) {
                  bufferSource.close();
                  return;
               }

               ItemRenderer itemRenderer = mc.getItemRenderer();
               BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
               ArrayList<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
               directions.add(null);
               float f = ((Direction)itemBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)).toYRot();
               List<TransformItemData> transformDatas = itemBlockEntity.getTransformData();
               Level level = be.getLevel();
               BlockPos pos = be.getBlockPos();
               Boolean disableAO = (Boolean)be.getBlockState().getValue(AbstractTransformBlock.ENABLE_AO);

               for (TransformItemData transformData : transformDatas) {
                  if (transformData.isShown) {
                     ItemStack itemStack = transformData.itemStack;
                     // PHASE-B: upstream also skips the Text Block item here (`!itemStack.is(ItemRegistry.TEXT_BLOCK.get())`).
                     if (!itemStack.isEmpty()) {
                        BlockState blockState = YuushyaDataTags.getBlockState(itemStack);
                        if (transformData.enableBlock && blockState != null) {
                           BakedModel blockModel = blockRenderer.getBlockModel(blockState);

                           for (Direction value : directions) {
                              List<BakedQuad> blockModelQuads = blockModel.getQuads(blockState, value, CachedRegion.this.random);
                              float[] afloat = new float[CachedRegion.DIRECTIONS.length * 2];
                              BlockPos offset = pos.offset(
                                 (int)(transformData.pos.x / 16.0), (int)(transformData.pos.y / 16.0), (int)(transformData.pos.z / 16.0)
                              );
                              this.putNormalModel(
                                 poseStack, bufferSource, be, f, level, pos, disableAO, transformData, blockModel, blockModelQuads, afloat, offset
                              );
                           }
                        } else {
                           BakedModel blockModel = itemRenderer.getModel(itemStack, null, null, localPlayer.getId());

                           for (BakedModel model : blockModel.getRenderPasses(itemStack, true)) {
                              BlockPos offset = pos.offset(
                                 (int)(transformData.pos.x / 16.0), (int)(transformData.pos.y / 16.0), (int)(transformData.pos.z / 16.0)
                              );
                              if (model instanceof BuiltInModel) {
                                 poseStack.pushPose();
                                 poseStack.translate((float)(pos.getX() & 15), (float)pos.getY(), (float)(pos.getZ() & 15));
                                 YuushyaUtils.scale(poseStack, transformData.scales);
                                 YuushyaUtils.translate(poseStack, transformData.pos);
                                 YuushyaUtils.rotate(poseStack, transformData.rot);
                                 poseStack.translate(0.5F, 0.5F, 0.5F);
                                 int packedLight = LevelRenderer.getLightColor(level, offset);
                                 itemRenderer.render(
                                    itemStack, TransformType.NONE, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model
                                 );
                                 poseStack.popPose();
                              } else {
                                 for (Direction value : directions) {
                                    float[] afloat = new float[CachedRegion.DIRECTIONS.length * 2];
                                    List<BakedQuad> blockModelQuads = model.getQuads(null, value, CachedRegion.this.random);
                                    this.putNormalModel(
                                       poseStack, bufferSource, be, f, level, pos, disableAO, transformData, model, blockModelQuads, afloat, offset
                                    );
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         CachedRegion.this.isEmpty = bufferSource.isEmpty();
         bufferSource.upload(CachedRegion.this::getBuffer, CachedRegion.this.pipeline::submitUploadTask);
         CachedRegion.this.meshSortings = bufferSource.getMeshSorts();
         CachedRegion.this.indexCountMap = bufferSource.getIndexCountMap();
         CachedRegion.this.lastRebuildTask = null;
      }

      private void putNormalModel(
         PoseStack poseStack,
         FullyBufferedBufferSource bufferSource,
         BlockEntity be,
         float f,
         Level level,
         BlockPos pos,
         Boolean disableAO,
         TransformItemData transformData,
         BakedModel blockModel,
         List<BakedQuad> blockModelQuads,
         float[] afloat,
         BlockPos offset
      ) {
         for (BakedQuad bakedQuad : blockModelQuads) {
            poseStack.pushPose();
            poseStack.translate((float)(pos.getX() & 15), (float)pos.getY(), (float)(pos.getZ() & 15));
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            YuushyaUtils.scale(poseStack, transformData.scales);
            YuushyaUtils.translate(poseStack, transformData.pos);
            YuushyaUtils.rotate(poseStack, transformData.rot);
            float[] colorComponents = CachedRegion.getColorComponents(transformData, blockModel, bakedQuad);
            if (disableAO) {
               BitSet bitset = new BitSet(3);
               CachedRegion.this.calculateShape(level, be.getBlockState(), offset, bakedQuad.getVertices(), bakedQuad.getDirection(), afloat, bitset);
               CachedRegion.this.aoFace.calculate(level, be.getBlockState(), offset, bakedQuad.getDirection(), afloat, bitset, bakedQuad.isShade());
               bufferSource.getBuffer(CachedRegion.TRANSLUCENT_MAIN)
                  .putBulkData(
                     poseStack.last(),
                     bakedQuad,
                     CachedRegion.this.aoFace.brightness,
                     colorComponents[0],
                     colorComponents[1],
                     colorComponents[2],
                     1.0F,
                     CachedRegion.this.aoFace.lightmap,
                     OverlayTexture.NO_OVERLAY,
                     true
                  );
            } else {
               int packedLight = LevelRenderer.getLightColor(
                  level, pos.offset((int)(transformData.pos.x / 16.0), (int)(transformData.pos.y / 16.0), (int)(transformData.pos.z / 16.0))
               );
               bufferSource.getBuffer(CachedRegion.TRANSLUCENT_MAIN)
                  .putBulkData(poseStack.last(), bakedQuad, colorComponents[0], colorComponents[1], colorComponents[2], packedLight, OverlayTexture.NO_OVERLAY);
            }

            poseStack.popPose();
         }
      }

      void cancel() {
         this.cancelled = true;
      }
   }
}
