// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 client/anvilcraft/rendering/CacheableBERenderingPipeline.java to Forge 1.19.2. Upstream derived this
// render cache from AnvilCraft (LGPL-3.0-or-later, conveyed here under GPL-3.0, see NOTICE-upstream-MIT.txt); Yuushya
// Modelling itself is MIT. Deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.client.anvilcraft.rendering;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Matrix4f;

public class CacheableBERenderingPipeline {
   private static final int MAX_CACHED_REGIONS = 256;
   @Nullable
   private static CacheableBERenderingPipeline instance;
   private final ClientLevel level;
   private final Queue<Runnable> pendingCompiles = new ArrayDeque<>();
   private final Queue<Runnable> pendingUploads = new ArrayDeque<>();
   private final Map<ChunkPos, CachedRegion> regions = new LinkedHashMap<ChunkPos, CachedRegion>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<ChunkPos, CachedRegion> eldest) {
         boolean shouldRemove = this.size() > MAX_CACHED_REGIONS;
         if (shouldRemove) {
            eldest.getValue().releaseBuffers();
         }

         return shouldRemove;
      }
   };
   private boolean valid = true;

   public CachedRegion getRenderRegion(ChunkPos chunkPos) {
      if (this.regions.containsKey(chunkPos)) {
         return this.regions.get(chunkPos);
      } else {
         CachedRegion region = new CachedRegion(chunkPos, this);
         this.regions.put(chunkPos, region);
         return region;
      }
   }

   public CacheableBERenderingPipeline(ClientLevel level) {
      this.level = level;
   }

   public void runTasks() {
      while (!this.pendingCompiles.isEmpty() && this.valid) {
         this.pendingCompiles.poll().run();
      }

      while (!this.pendingUploads.isEmpty() && this.valid) {
         this.pendingUploads.poll().run();
      }
   }

   public static void updateLevel(ClientLevel level) {
      if (instance != null) {
         instance.releaseBuffers();
      }

      instance = new CacheableBERenderingPipeline(level);
      // Port: drop the old level's Item Block positions (upstream kept them across worlds and dimensions).
      CachedModeClient.INSTANCE.clearAll();
      com.yuushya.modelling.utils.CustomRenderInstance.getINSTANCE().clearAll();
   }

   public void blockRemoved(BlockEntity be) {
      BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
      if (renderer != null) {
         ChunkPos chunkPos = new ChunkPos(be.getBlockPos());
         this.getRenderRegion(chunkPos).blockRemoved(be);
      }
   }

   public void updateFromNetwork(ChunkPos chunkPos, Collection<BlockPos> entityPos) {
      this.getRenderRegion(chunkPos).replaceData(entityPos, this.level);
   }

   public void update(BlockEntity be) {
      BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
      if (renderer != null) {
         ChunkPos chunkPos = new ChunkPos(be.getBlockPos());
         this.getRenderRegion(chunkPos).update(be);
      }
   }

   public void submitUploadTask(Runnable task) {
      this.pendingUploads.add(task);
   }

   public void submitCompileTask(Runnable task) {
      this.pendingCompiles.add(task);
   }

   public void releaseBuffers() {
      this.regions.values().forEach(CachedRegion::releaseBuffers);
      this.valid = false;
   }

   public void render(Matrix4f frustumMatrix, Matrix4f projectionMatrix) {
      this.regions.values().forEach(it -> it.render(frustumMatrix, projectionMatrix));
   }

   @Nullable
   public static CacheableBERenderingPipeline getInstance() {
      return instance;
   }

   public void forcedUpdate(BlockPos pos) {
      this.getRenderRegion(new ChunkPos(pos)).forcedUpdate();
   }
}
