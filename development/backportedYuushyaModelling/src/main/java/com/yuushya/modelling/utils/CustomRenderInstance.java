// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 utils/CustomRenderInstance.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CustomRenderInstance {
   private static final CustomRenderInstance INSTANCE = new CustomRenderInstance();
   private final Map<ChunkPos, Set<BlockPos>> cachedModeData;
   public volatile boolean dirty = false;

   private CustomRenderInstance() {
      this.cachedModeData = new ConcurrentHashMap<>();
   }

   public void addBlockPos(ChunkPos chunkPos, BlockPos blockPos) {
      this.cachedModeData.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet()).add(blockPos);
   }

   public void addAllBlockPos(ChunkPos chunkPos, Set<BlockPos> blockPosSet) {
      this.cachedModeData.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet()).addAll(blockPosSet);
   }

   public void clearChunk(ChunkPos chunkPos) {
      this.cachedModeData.remove(chunkPos);
   }

   public void clearAll() {
      this.cachedModeData.clear();
   }

   public static CustomRenderInstance getINSTANCE() {
      return INSTANCE;
   }

   public Map<ChunkPos, Set<BlockPos>> getCachedModeData() {
      return this.cachedModeData;
   }
}
