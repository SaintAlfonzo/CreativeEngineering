package com.SaintAlfonzo.creativeengineering.swap;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * The world half of the swap lists' item merges (feature_writeup/item_merges.md 6, TODO creativedelight/swappers #3).
 * {@link SwapRemapper} only sees ids that are no longer registered; a merge keeps both items registered, so a purged
 * stack that is already in the world is rewritten here: player inventories and ender chests, containers (block entities
 * with a {@link Container} or a modifiable item handler, container entities, any open menu) and dropped items.
 * A rewritten stack keeps its count and everything it carries (NBT, capabilities, nested contents such as a shulker box's
 * items, which are rewritten too). Items beyond the survivor's stack size go to free slots, then to the player or the
 * ground; nothing is deleted. Triggers are the {@link SwapConfig} switches; the merges come from {@link SwapLists#merges()}
 * (active ones whose two items are registered).
 */
public final class ItemMerges {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** Chunk NBT and player persistent data key: the fingerprint of the merge list the chunk or player was last rewritten with. */
	static final String STAMP = "creativeengineering:swap_merges";
	private static final int CHUNKS_PER_TICK = 32;

	private static volatile Map<Item, Item> pairs;
	private static volatile Map<String, String> idPairs = Map.of();
	private static volatile String fingerprint = "";

	/** Loaded server chunks per level (for the command), and those whose stamp is current (for onceWhenUpdated). */
	private static final Map<ResourceKey<Level>, Set<Long>> LOADED = new ConcurrentHashMap<>();
	private static final Map<ResourceKey<Level>, Set<Long>> STAMPED = new ConcurrentHashMap<>();
	private static final ConcurrentLinkedQueue<Pending> QUEUE = new ConcurrentLinkedQueue<>();

	private record Pending(ResourceKey<Level> level, long pos) {}

	private ItemMerges() {}

	// ---------------------------------------------------------------- the merge table

	private static Map<Item, Item> pairs() {
		Map<Item, Item> p = pairs;
		if (p != null) return p;
		synchronized (ItemMerges.class) {
			if (pairs != null) return pairs;
			Map<Item, Item> items = new HashMap<>();
			Map<String, String> ids = new HashMap<>();
			for (SwapLists.Merge m : SwapLists.get().merges()) {
				if (!m.active()) continue;
				Item old = registered(m.old());
				Item survivor = registered(m.survivor());
				if (old == null || survivor == null || old == survivor) continue;
				items.put(old, survivor);
				ids.put(m.old().toString(), m.survivor().toString());
			}
			idPairs = Collections.unmodifiableMap(ids);
			fingerprint = ItemMergeCore.fingerprint(ids);
			pairs = Collections.unmodifiableMap(items);
			LOGGER.info("[swaps] {} item merge(s) live in the world (list {})", items.size(), fingerprint);
			return pairs;
		}
	}

	private static Item registered(ResourceLocation id) {
		return ForgeRegistries.ITEMS.containsKey(id) ? ForgeRegistries.ITEMS.getValue(id) : null;
	}

	private static boolean idle() {
		return pairs().isEmpty();
	}

	// ---------------------------------------------------------------- one stack

	/**
	 * The stack with every purged id in it (itself and nested contents) rewritten, or the same instance when nothing in it
	 * is purged. Same count as the input, which may exceed the survivor's stack size: callers split it.
	 */
	public static ItemStack convert(ItemStack stack) {
		if (stack.isEmpty()) return stack;
		boolean top = pairs().containsKey(stack.getItem());
		if (!top && !stack.hasTag()) return stack;
		CompoundTag saved = stack.save(new CompoundTag());
		if (ItemMergeCore.rewriteNested(saved, idPairs) == 0) return stack;
		ItemStack out = ItemStack.of(saved);
		if (out.isEmpty()) return stack;   // never delete: an id that will not load leaves the stack as it was
		out.setCount(stack.getCount());   // the save writes Count as a byte; keep large modded counts
		return out;
	}

	/** Slot access shared by containers, item handlers and menus. */
	private interface Slots {
		int size();
		ItemStack get(int i);
		void set(int i, ItemStack s);
	}

	/**
	 * Rewrites every slot; overflow beyond the survivor's stack size goes to empty slots, then to {@code sink} (returns
	 * true when it took the stack). What the sink refuses stays in its slot, over-stacked. Returns stacks rewritten.
	 */
	private static int rewrite(Slots slots, Predicate<ItemStack> sink) {
		int n = 0;
		List<ItemStack> overflow = new ArrayList<>();
		for (int i = 0; i < slots.size(); i++) {
			ItemStack s = slots.get(i);
			ItemStack out = convert(s);
			if (out == s) continue;
			int[] parts = ItemMergeCore.split(out.getCount(), out.getMaxStackSize());
			ItemStack first = out.copy();
			first.setCount(parts[0]);
			slots.set(i, first);   // throws before anything moved when the slot cannot be written
			for (int k = 1; k < parts.length; k++) {
				ItemStack extra = out.copy();
				extra.setCount(parts[k]);
				overflow.add(extra);
			}
			n++;
		}
		for (ItemStack extra : overflow) {
			for (int i = 0; i < slots.size() && !extra.isEmpty(); i++) {
				if (!slots.get(i).isEmpty()) continue;
				try {
					slots.set(i, extra);
					extra = ItemStack.EMPTY;
				} catch (RuntimeException ignored) {
					// a slot that refuses writes: try the next
				}
			}
			if (extra.isEmpty() || (sink != null && sink.test(extra))) continue;
			// refused: back onto the first slot holding the same item, over-stacked, rather than lost
			for (int i = 0; i < slots.size(); i++) {
				ItemStack s = slots.get(i);
				if (!ItemStack.isSameItemSameTags(s, extra)) continue;
				ItemStack grown = s.copy();
				grown.grow(extra.getCount());
				try {
					slots.set(i, grown);
					extra = ItemStack.EMPTY;
					break;
				} catch (RuntimeException ignored) {
					// try the next
				}
			}
			if (!extra.isEmpty()) LOGGER.error("[swaps] no slot took {} x{} split off a merged stack", extra.getItem(), extra.getCount());
		}
		return n;
	}

	private static Slots of(Container c) {
		return new Slots() {
			public int size() { return c.getContainerSize(); }
			public ItemStack get(int i) { return c.getItem(i); }
			public void set(int i, ItemStack s) { c.setItem(i, s); }
		};
	}

	private static Slots of(IItemHandlerModifiable h) {
		return new Slots() {
			public int size() { return h.getSlots(); }
			public ItemStack get(int i) { return h.getStackInSlot(i); }
			public void set(int i, ItemStack s) { h.setStackInSlot(i, s); }
		};
	}

	/** A menu's slots, minus crafting and trade results (rewriting those would hand out a different result). */
	private static Slots of(AbstractContainerMenu menu) {
		List<Slot> slots = new ArrayList<>();
		for (Slot s : menu.slots) {
			if (s instanceof ResultSlot || s instanceof FurnaceResultSlot || s instanceof MerchantResultSlot) continue;
			slots.add(s);
		}
		return new Slots() {
			public int size() { return slots.size(); }
			public ItemStack get(int i) { return slots.get(i).getItem(); }
			public void set(int i, ItemStack s) { slots.get(i).set(s); }
		};
	}

	private static Predicate<ItemStack> toPlayer(Player player) {
		return s -> {
			ItemHandlerHelper.giveItemToPlayer(player, s);
			return true;
		};
	}

	private static Predicate<ItemStack> toGround(Level level, BlockPos pos) {
		return s -> {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, s);
			return true;
		};
	}

	// ---------------------------------------------------------------- what gets rewritten

	private static int player(ServerPlayer player) {
		int n = rewrite(of(player.getInventory()), toPlayer(player));
		n += rewrite(of(player.getEnderChestInventory()), toPlayer(player));
		if (n > 0) player.inventoryMenu.broadcastChanges();
		CompoundTag data = player.getPersistentData();
		CompoundTag kept = data.getCompound(Player.PERSISTED_NBT_TAG);
		kept.putString(STAMP, fingerprint);
		data.put(Player.PERSISTED_NBT_TAG, kept);
		return n;
	}

	private static boolean playerStamped(ServerPlayer player) {
		return fingerprint.equals(player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getString(STAMP));
	}

	private static int blockEntity(BlockEntity be) {
		Level level = be.getLevel();
		if (level == null || be.isRemoved()) return 0;
		Predicate<ItemStack> sink = toGround(level, be.getBlockPos());
		int n = 0;
		try {
			if (be instanceof Container c) {
				n = rewrite(of(c), sink);
			} else {
				IItemHandler h = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
				if (h instanceof IItemHandlerModifiable m) n = rewrite(of(m), sink);
			}
		} catch (RuntimeException e) {
			LOGGER.warn("[swaps] skipped {} at {}: {}", be.getType(), be.getBlockPos(), e.toString());
		}
		if (n > 0) be.setChanged();
		return n;
	}

	private static int chunk(LevelChunk chunk) {
		int n = 0;
		for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) n += blockEntity(be);
		return n;
	}

	private static int entity(Entity e) {
		if (e instanceof ItemEntity item) {
			ItemStack s = item.getItem();
			ItemStack out = convert(s);
			if (out == s) return 0;
			item.setItem(out);   // an over-sized dropped stack is fine: pickup splits it
			return 1;
		}
		if (e instanceof Container c && !(e instanceof Player)) {
			try {
				return rewrite(of(c), null);
			} catch (RuntimeException ex) {
				LOGGER.warn("[swaps] skipped {}: {}", e, ex.toString());
			}
		}
		return 0;
	}

	// ---------------------------------------------------------------- triggers

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || idle()) return;
		boolean every = SwapConfig.ON_PLAYER_DATA_LOAD.get();
		if (!every && !(SwapConfig.ONCE_WHEN_UPDATED.get() && !playerStamped(player))) return;
		int n = player(player);
		if (n > 0) LOGGER.info("[swaps] {}: {} merged stack(s) rewritten", player.getGameProfile().getName(), n);
	}

	@SubscribeEvent
	public static void onContainerOpen(PlayerContainerEvent.Open event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !SwapConfig.ON_CONTAINER_OPEN.get() || idle()) return;
		try {
			int n = rewrite(of(event.getContainer()), toPlayer(player));
			if (n > 0) {
				event.getContainer().broadcastChanges();
				LOGGER.info("[swaps] {} opened {}: {} merged stack(s) rewritten", player.getGameProfile().getName(), event.getContainer().getClass().getSimpleName(), n);
			}
		} catch (RuntimeException e) {
			LOGGER.warn("[swaps] menu {} skipped: {}", event.getContainer().getClass().getName(), e.toString());
		}
	}

	@SubscribeEvent
	public static void onEntityJoin(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide()) return;
		if (!SwapConfig.ON_WORLD_LOAD.get() && !SwapConfig.ONCE_WHEN_UPDATED.get()) return;
		Entity e = event.getEntity();
		if (!(e instanceof ItemEntity) && !(e instanceof Container)) return;
		if (idle()) return;
		entity(e);
	}

	@SubscribeEvent
	public static void onChunkData(ChunkDataEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		if (fingerprintNow().equals(event.getData().getString(STAMP)))
			STAMPED.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(event.getChunk().getPos().toLong());
	}

	@SubscribeEvent
	public static void onChunkSave(ChunkDataEvent.Save event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		Set<Long> stamped = STAMPED.get(level.dimension());
		if (stamped != null && stamped.contains(event.getChunk().getPos().toLong())) event.getData().putString(STAMP, fingerprint);
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
		long pos = chunk.getPos().toLong();
		LOADED.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos);
		boolean every = SwapConfig.ON_WORLD_LOAD.get();
		if (!every && !SwapConfig.ONCE_WHEN_UPDATED.get()) return;
		if (!every) {
			Set<Long> stamped = STAMPED.get(level.dimension());
			if (stamped != null && stamped.contains(pos)) return;
		}
		QUEUE.add(new Pending(level.dimension(), pos));   // block entities are read on the next server tick, not mid-load
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		Set<Long> loaded = LOADED.get(level.dimension());
		if (loaded != null) loaded.remove(event.getChunk().getPos().toLong());
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || QUEUE.isEmpty()) return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) return;
		if (idle()) {
			QUEUE.clear();
			return;
		}
		int done = 0;
		int rewritten = 0;
		Pending p;
		while (done < CHUNKS_PER_TICK && (p = QUEUE.poll()) != null) {
			ServerLevel level = server.getLevel(p.level());
			if (level == null) continue;
			ChunkPos cp = new ChunkPos(p.pos());
			LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
			if (chunk == null) continue;   // unloaded again: queued anew on its next load
			rewritten += chunk(chunk);
			if (SwapConfig.ONCE_WHEN_UPDATED.get()) {
				STAMPED.computeIfAbsent(p.level(), k -> ConcurrentHashMap.newKeySet()).add(p.pos());
				chunk.setUnsaved(true);   // so the stamp is written
			}
			done++;
		}
		if (rewritten > 0) LOGGER.info("[swaps] {} chunk(s) checked, {} merged stack(s) rewritten", done, rewritten);
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		LOADED.clear();
		STAMPED.clear();
		QUEUE.clear();
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		register(event.getDispatcher());
	}

	private static String fingerprintNow() {
		pairs();
		return fingerprint;
	}

	// ---------------------------------------------------------------- /ceswap items

	private static void register(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("ceswap").requires(s -> s.hasPermission(2))
				.then(Commands.literal("items").executes(ctx -> {
					CommandSourceStack src = ctx.getSource();
					if (!SwapConfig.ON_COMMAND.get()) {
						src.sendFailure(Component.literal("Item merge rewrite is off (creativeengineering-swaps.toml onCommand)."));
						return 0;
					}
					int[] r = runAll(src.getServer());
					src.sendSuccess(Component.literal(r[0] + " merged stack(s) rewritten: " + r[1] + " player(s), " + r[2]
							+ " loaded chunk(s), " + r[3] + " entities checked (" + pairs().size() + " merges)."), true);
					return r[0];
				})));
	}

	/** Every online player, every loaded chunk's containers and every entity now. Returns {stacks, players, chunks, entities}. */
	static int[] runAll(MinecraftServer server) {
		if (idle()) return new int[] {0, 0, 0, 0};
		int stacks = 0;
		int players = 0;
		int chunks = 0;
		int entities = 0;
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			stacks += player(p);
			players++;
		}
		for (ServerLevel level : server.getAllLevels()) {
			Set<Long> loaded = LOADED.get(level.dimension());
			if (loaded != null) {
				for (Long pos : new ArrayList<>(loaded)) {
					ChunkPos cp = new ChunkPos(pos);
					LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
					if (chunk == null) continue;
					stacks += chunk(chunk);
					chunks++;
				}
			}
			List<Entity> all = new ArrayList<>();
			level.getAllEntities().forEach(all::add);
			for (Entity e : all) {
				if (e instanceof ItemEntity || (e instanceof Container && !(e instanceof Player))) {
					stacks += entity(e);
					entities++;
				}
			}
		}
		LOGGER.info("[swaps] /ceswap items: {} stack(s) rewritten ({} players, {} chunks, {} entities)", stacks, players, chunks, entities);
		return new int[] {stacks, players, chunks, entities};
	}
}
