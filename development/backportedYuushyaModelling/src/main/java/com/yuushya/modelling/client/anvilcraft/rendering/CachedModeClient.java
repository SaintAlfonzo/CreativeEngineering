// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 client/anvilcraft/rendering/CachedModeClient.java to Forge 1.19.2. Upstream derived this
// render cache from AnvilCraft (LGPL-3.0-or-later, conveyed here under GPL-3.0, see NOTICE-upstream-MIT.txt); Yuushya
// Modelling itself is MIT. Deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.client.anvilcraft.rendering;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CachedModeClient {
   public static final CachedModeClient INSTANCE = new CachedModeClient();
   private final Map<ChunkPos, Set<BlockPos>> cachedModeData = new ConcurrentHashMap<>();

   public boolean isCachedModeEnabledOn(BlockEntity be) {
      BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
      return renderer == null ? false : this.isCachedModeEnabledOn(be.getBlockPos());
   }

   public boolean isCachedModeEnabledOn(BlockPos pos) {
      ChunkPos chunkPos = new ChunkPos(pos);
      Set<BlockPos> blockPosSet = this.cachedModeData.get(chunkPos);
      return blockPosSet != null && blockPosSet.contains(pos);
   }

   public void updateCachedModeData(ChunkPos chunkPos, List<BlockPos> blockPosList) {
      Set<BlockPos> blockPosSet = ConcurrentHashMap.newKeySet();
      blockPosSet.addAll(blockPosList);
      this.cachedModeData.put(chunkPos, blockPosSet);
      CacheableBERenderingPipeline pipeline = CacheableBERenderingPipeline.getInstance();
      if (pipeline != null) {
         pipeline.updateFromNetwork(chunkPos, blockPosList);
      }
   }

   public void clearChunk(ChunkPos chunkPos) {
      this.cachedModeData.remove(chunkPos);
   }

   public void clearAll() {
      this.cachedModeData.clear();
   }
}
