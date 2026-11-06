package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.SaintAlfonzo.creativeworldgen.worldgen.WorldgenConfig;
import it.unimi.dsi.fastutil.longs.Long2BooleanFunction;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMaps;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * StructureCheck's caches made safe for several structure searches at once (feature_writeup/locate.md 3.3).
 *
 * loadedChunks (fastutil open hash map) and featureChecks (HashMap of fastutil maps) are read and written by
 * checkStart on every search thread (Async Locator's pool) and written by the server thread (onStructureLoad,
 * incrementReference). Two threads in an open hash map at once can loop forever or lose entries. When CWGConfig
 * [worldgen.locate] threadSafeStructureCheck is on as the dimension loads, both maps are swapped for synchronized /
 * concurrent ones, and checkStart's cached prediction runs outside the lock (the prediction is the expensive part;
 * two threads predicting the same chunk get the same answer, and the first one is kept).
 */
@Mixin(value = StructureCheck.class, remap = false)
public class StructureCheckThreadSafeMixin {

	@Shadow(remap = false) @Final @Mutable
	private Long2ObjectMap<Object2IntMap<Structure>> f_197247_;

	@Shadow(remap = false) @Final @Mutable
	private Map<Structure, Long2BooleanMap> f_197248_;

	@Inject(method = "<init>", at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$threadSafeMaps(CallbackInfo ci) {
		if (!WorldgenConfig.locateThreadSafeStructureCheck()) return;
		f_197247_ = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
		f_197248_ = new ConcurrentHashMap<>();
	}

	@Redirect(method = "m_226729_(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Z)"
			+ "Lnet/minecraft/world/level/levelgen/structure/StructureCheckResult;",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"),
			require = 1, remap = false)
	private Object creativeengineering$perStructureMap(Map<Object, Object> map, Object structure, Function<Object, Object> create) {
		if (map instanceof ConcurrentHashMap) return map.computeIfAbsent(structure, k -> Long2BooleanMaps.synchronize(new Long2BooleanOpenHashMap()));
		return map.computeIfAbsent(structure, create);
	}

	@Redirect(method = "m_226729_(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Z)"
			+ "Lnet/minecraft/world/level/levelgen/structure/StructureCheckResult;",
			at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2BooleanMap;computeIfAbsent(JLit/unimi/dsi/fastutil/longs/Long2BooleanFunction;)Z"),
			require = 1, remap = false)
	private boolean creativeengineering$predictOutsideLock(Long2BooleanMap map, long chunk, Long2BooleanFunction predict) {
		if (!(map instanceof Long2BooleanMaps.SynchronizedMap)) return map.computeIfAbsent(chunk, predict);
		synchronized (map) {
			if (map.containsKey(chunk)) return map.get(chunk);
		}
		boolean value = predict.get(chunk);
		synchronized (map) {
			if (!map.containsKey(chunk)) map.put(chunk, value);
			return map.get(chunk);
		}
	}
}
