package com.SaintAlfonzo.creativecommon.swap;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The load report of the swap lists (plan_cd_namespace.md 3.3): in the first 10 minutes after a server starts, counts the
 * stacks and chunk palette entries that still name an id a list renames and did not resolve. Any count is item or block loss.
 * One {@code [swaps] unresolved} line per id, written once a minute while the counts change and again when the server stops.
 */
public final class SwapReport {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final long WINDOW_NANOS = TimeUnit.MINUTES.toNanos(10);
	private static final int FLUSH_TICKS = 1200;

	private static final Map<String, AtomicLong> COUNTS = new ConcurrentHashMap<>();
	private static final AtomicBoolean DIRTY = new AtomicBoolean();
	private static volatile long windowStart;
	private static volatile boolean open;
	private static int ticks;

	private SwapReport() {}

	private static boolean inWindow() {
		if (!open) return false;
		if (System.nanoTime() - windowStart < WINDOW_NANOS) return true;
		open = false;
		return false;
	}

	/** {@code ItemStack.of} came back empty for this tag. */
	public static void noteEmptyStack(CompoundTag tag) {
		if (!inWindow() || tag == null) return;
		ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
		if (id != null && SwapLists.get().target(id) != null) count(id);
	}

	/** A chunk is being read: palette entries that name a listed id with no block behind it would load as air. */
	public static void noteChunk(CompoundTag chunk) {
		if (!inWindow() || chunk == null) return;
		ListTag sections = chunk.getList("sections", Tag.TAG_COMPOUND);
		SwapLists lists = SwapLists.get();
		for (int s = 0; s < sections.size(); s++) {
			ListTag palette = sections.getCompound(s).getCompound("block_states").getList("palette", Tag.TAG_COMPOUND);
			for (int p = 0; p < palette.size(); p++) {
				ResourceLocation id = ResourceLocation.tryParse(palette.getCompound(p).getString("Name"));
				if (id != null && lists.target(id) != null && !ForgeRegistries.BLOCKS.containsKey(id)) count(id);
			}
		}
	}

	private static void count(ResourceLocation id) {
		COUNTS.computeIfAbsent(id.toString(), k -> new AtomicLong()).incrementAndGet();
		DIRTY.set(true);
	}

	@SubscribeEvent
	public static void onAboutToStart(ServerAboutToStartEvent event) {
		COUNTS.clear();
		DIRTY.set(false);
		ticks = 0;
		windowStart = System.nanoTime();
		open = !SwapLists.get().isEmpty();
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || ++ticks < FLUSH_TICKS) return;
		ticks = 0;
		if (DIRTY.getAndSet(false)) log();
	}

	@SubscribeEvent
	public static void onStopping(ServerStoppingEvent event) {
		log();
		if (COUNTS.isEmpty()) LOGGER.info("[swaps] load report: every listed id resolved");
		open = false;
	}

	private static void log() {
		new TreeMap<>(COUNTS).forEach((id, n) -> LOGGER.warn("[swaps] unresolved {}: {}", id, n.get()));
	}
}
