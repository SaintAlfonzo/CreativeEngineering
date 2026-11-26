// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 mixin/LevelRendererMixin.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// the 1.19.2 renderLevel has the same signature with com.mojang.math.Matrix4f and the same compileChunks and
// endLastBatch calls. Deviations from 2.4.1 (null-safe pipeline) are listed in PORT_NOTES.md.
package com.yuushya.modelling.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.client.anvilcraft.rendering.CacheableBERenderingPipeline;
import com.yuushya.modelling.client.anvilcraft.rendering.CachedModeClient;
import com.yuushya.modelling.utils.CustomRenderInstance;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
   @Shadow
   @Nullable
   private ClientLevel level;

   @Inject(
      method = "renderLevel",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;compileChunks(Lnet/minecraft/client/Camera;)V")
   )
   private void yuushya$recompileBlockEntities(
      PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera,
      GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci
   ) {
      CacheableBERenderingPipeline pipeline = CacheableBERenderingPipeline.getInstance();
      if (pipeline != null) {
         pipeline.runTasks();
      }
   }

   @Inject(
      method = "renderLevel",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 0)
   )
   private void yuushya$renderCachedBE(
      PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera,
      GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci
   ) {
      CacheableBERenderingPipeline pipeline = CacheableBERenderingPipeline.getInstance();
      if (pipeline != null) {
         pipeline.render(pPoseStack.last().pose(), pProjectionMatrix);
      }
   }

   @Inject(method = "renderLevel", at = @At("TAIL"))
   private void yuushya$callRebuild(
      PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera,
      GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci
   ) {
      CustomRenderInstance instance = CustomRenderInstance.getINSTANCE();
      ClientLevel clientLevel = this.level;
      if (clientLevel != null && instance.dirty) {
         Map<ChunkPos, Set<BlockPos>> cachedModeData = instance.getCachedModeData();
         cachedModeData.forEach((chunkPos, blockPosSet) -> {
            if (!blockPosSet.isEmpty()) {
               List<BlockPos> list = blockPosSet.stream().filter(blockPos -> clientLevel.getBlockEntity(blockPos) instanceof ItemBlockEntity).toList();
               CachedModeClient.INSTANCE.updateCachedModeData(chunkPos, list);
            }
         });
         instance.dirty = false;
      }
   }
}
