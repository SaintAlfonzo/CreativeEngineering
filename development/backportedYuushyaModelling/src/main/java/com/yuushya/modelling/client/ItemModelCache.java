// SPDX-License-Identifier: GPL-3.0-only
// New in the backport (bug B2 fix, plan_yuushya_backport.md section 3): replaces upstream 2.4.1's
// `Map<ItemStack, ...> itemModelCache` in NeoShowBlockModel / NeoItemBlockModel, which was keyed by ItemStack identity
// (every new stack instance made a new model) and never cleared.
package com.yuushya.modelling.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Item model cache for Entity Block and Item Block items: keyed by the transform-data tag's string form, LRU-bound
 * to {@link #MAX_ENTRIES} entries, thread safe (chunk build threads reach it through nested render passes), cleared
 * on every client resource reload (models and textures change, so the cached models are stale).
 */
public final class ItemModelCache<V> {
   public static final int MAX_ENTRIES = 256;
   private static final List<ItemModelCache<?>> ALL = Collections.synchronizedList(new ArrayList<>());
   private final Map<String, V> map = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, V> eldest) {
         return this.size() > MAX_ENTRIES;
      }
   });

   public ItemModelCache() {
      ALL.add(this);
   }

   public V get(CompoundTag transformDataTag, Supplier<V> factory) {
      String key = transformDataTag.toString();
      synchronized (this.map) {
         V value = this.map.get(key);
         if (value == null) {
            value = factory.get();
            this.map.put(key, value);
         }

         return value;
      }
   }

   public void clear() {
      this.map.clear();
   }

   public int size() {
      return this.map.size();
   }

   public static void clearAll() {
      synchronized (ALL) {
         ALL.forEach(ItemModelCache::clear);
      }
   }

   /** Registered from YuushyaClientNeoForge on RegisterClientReloadListenersEvent. */
   public static final PreparableReloadListener RELOAD_LISTENER = new PreparableReloadListener() {
      @Override
      public CompletableFuture<Void> reload(
         PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
         ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor
      ) {
         return barrier.wait(null).thenRunAsync(ItemModelCache::clearAll, gameExecutor);
      }

      @Override
      public String getName() {
         return "yuushya_modelling:item_model_cache";
      }
   };
}
