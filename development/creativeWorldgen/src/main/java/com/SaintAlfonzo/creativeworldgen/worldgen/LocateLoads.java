package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The chunk load a structure search makes for a candidate the prediction couldn't rule out (feature_writeup/locate.md
 * 3.2). Vanilla's {@code level.getChunk(x, z, STRUCTURE_STARTS)} from another thread hands the call to the server
 * thread, which then blocks in managedBlock until the chunk's structure starts exist: with Async Locator every miss
 * is still a stalled tick. Here, off the server thread, the server only adds a ticket and starts the chunk future
 * (ServerChunkCache#getChunkFuture), and the search thread waits for it; the server keeps ticking while worldgen
 * threads generate the chunk.
 *
 * The ticket (level 33 + the STRUCTURE_STARTS distance, so no noise or features) keeps the chunk scheduled while the
 * server ticks on; vanilla's own UNKNOWN ticket lasts one tick, which is fine only because vanilla blocks. Removed
 * when the load finishes; it also expires on its own after 5 minutes.
 *
 * Every load is counted for the current search (LocateStats). CWGConfig [worldgen.locate] nonBlockingLoads off, or
 * the server thread itself searching: vanilla's call.
 *
 * A search that has expired (its deadline, /celocate stop, the server stopping; LocateStats#expired) gets an empty
 * chunk with no structure starts instead, at once or while it waits, so it ends as "nothing here" and frees its
 * thread. The wait checks for that every quarter second.
 */
public final class LocateLoads {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/locate");

	private static final TicketType<ChunkPos> TICKET = TicketType.create("creativeengineering_locate", Comparator.comparingLong(ChunkPos::toLong), 6000);
	private static final int LEVEL = 33 + ChunkStatus.getDistance(ChunkStatus.STRUCTURE_STARTS);

	private LocateLoads() {}

	/**
	 * The chunk at STRUCTURE_STARTS (or further). A failed non-blocking load falls back to vanilla's call; an expired
	 * search gets an empty chunk.
	 */
	public static ChunkAccess structureStarts(LevelReader level, int x, int z) {
		if (level instanceof Level l && LocateStats.expired()) return abandoned(l, x, z);
		LocateStats.load();
		if (WorldgenConfig.locateNonBlockingLoads() && level instanceof ServerLevel serverLevel && !serverLevel.getServer().isSameThread()) {
			ChunkAccess chunk = offThread(serverLevel, x, z);
			if (chunk != null) return chunk;
			if (LocateStats.expired()) return abandoned(serverLevel, x, z);
		}
		return level.getChunk(x, z, ChunkStatus.STRUCTURE_STARTS);
	}

	/** A chunk with no structure starts, for a search that has to end: vanilla's caller finds nothing in it. */
	static ChunkAccess abandoned(Level level, int x, int z) {
		return new EmptyLevelChunk(level, new ChunkPos(x, z), level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(Biomes.PLAINS));
	}

	private static ChunkAccess offThread(ServerLevel level, int x, int z) {
		MinecraftServer server = level.getServer();
		ServerChunkCache cache = level.getChunkSource();
		DistanceManager tickets = LocateReflect.tickets(cache);
		ChunkPos pos = new ChunkPos(x, z);
		long deadline = System.nanoTime() + WorldgenConfig.locateLoadTimeoutSeconds() * 1_000_000_000L;
		try {
			// the ticket first, in a server task; then getChunkFuture from this thread. Called on the server thread
			// (inside the task, as before) it waits in managedBlock until the chunk is generated and stalls the tick
			// (05_phase_5.md 3.1); off it, it only hands the load to the server's chunk executor and returns the future
			if (!finished(CompletableFuture.runAsync(() -> tickets.addTicket(TICKET, pos, LEVEL, pos), server), deadline)) return null;
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load = cache.getChunkFuture(x, z, ChunkStatus.STRUCTURE_STARTS, true);
			if (!finished(load, deadline)) return null;
			return load.join().left().orElse(null);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (Exception e) {
			LOG.warn("[locate] chunk {} not loaded for a structure search: {}", pos, e.toString());
			return null;
		} finally {
			server.execute(() -> tickets.removeTicket(TICKET, pos, LEVEL, pos));
		}
	}

	/** Waits for the future: true when it's done, false once the search has expired; throws past the deadline. */
	private static boolean finished(CompletableFuture<?> future, long deadline) throws Exception {
		while (true) {
			try {
				future.get(250, TimeUnit.MILLISECONDS);
				return true;
			} catch (TimeoutException e) {
				if (LocateStats.expired()) return false;
				if (System.nanoTime() > deadline) throw e;
			}
		}
	}
}
