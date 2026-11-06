package com.SaintAlfonzo.creativeworldgen.compat;

import brightspark.asynclocator.AsyncLocator;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The only class of ours that touches Async Locator's own classes (call {@link #available()} first; nothing here
 * loads when the mod is missing). Used by /celocate (worldgen/LocateCommand): batch searches go through AL's public
 * locate call, and census regions run on AL's thread pool, so both follow its asyncLocatorThreads setting.
 */
public final class AsyncLocatorBridge {

	private AsyncLocatorBridge() {}

	public static boolean available() {
		return ModList.get().isLoaded("asynclocator");
	}

	/** One search; {@code cancel} on the returned handle stops it (AL never completes a search that threw). */
	public static Handle locate(ServerLevel level, HolderSet<Structure> structures, BlockPos origin, int radius, boolean skipKnown) {
		AsyncLocator.LocateTask<Pair<BlockPos, Holder<Structure>>> task = AsyncLocator.locate(level, structures, origin, radius, skipKnown);
		return new Handle(task.completableFuture(), task::cancel);
	}

	/** AL's locating thread pool (a private static field), for work that isn't a single search. */
	public static ExecutorService executor() throws ReflectiveOperationException {
		Field f = AsyncLocator.class.getDeclaredField("LOCATING_EXECUTOR_SERVICE");
		f.setAccessible(true);
		ExecutorService executor = (ExecutorService) f.get(null);
		if (executor == null || executor.isShutdown()) throw new IllegalStateException("Async Locator's thread pool is not running");
		return executor;
	}

	public static Future<?> submit(Runnable work) throws ReflectiveOperationException {
		return executor().submit(work);
	}

	public record Handle(CompletableFuture<Pair<BlockPos, Holder<Structure>>> future, Runnable cancel) {}
}
