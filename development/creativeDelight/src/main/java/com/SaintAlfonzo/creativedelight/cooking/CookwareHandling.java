package com.SaintAlfonzo.creativedelight.cooking;

import com.SaintAlfonzo.creativedelight.CDConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Cookware handling (TODO cooking/kitchen #27, QoL-7 {@code cookwarePickup}): for the blocks in
 * {@code #creativedelight:cookware_pickup} (cooking pot, copper pot, kettle, skillet).
 * <ul>
 * <li>Sneak right-click with an empty main hand picks the block up: every slot of its inventory (ingredients, meal,
 * containers, output) goes into the player's inventory (overflow drops at the player), and the hand gets the station
 * as a plain item (the loot drop without {@code BlockEntityTag}, so the skillet keeps its own enchantments and a
 * renamed station its name). Respects break protection (a {@link BlockEvent.BreakEvent} is posted).</li>
 * <li>Left-click (punch) with an empty main hand, sneaking or not, toggles the block's state instead of breaking it:
 * the kettle's {@code lid}, the pots' handle ({@code support=handle} against the tray state upstream would give).
 * The skillet has no toggle and breaks as normal. Holding the button counts as one punch (see {@link #HELD_GAP}).</li>
 * </ul>
 * Upstream's sneak right-click toggles are never reached: the right-click event is cancelled before the block's use.
 * Placing a pot or kettle against the bottom face of a block still gives the handle (upstream placement).
 * WTHIT line: {@code compat/wthit/WthitCookwareToggle}.
 */
public final class CookwareHandling {

	public static final TagKey<Block> COOKWARE = TagKey.create(Registry.BLOCK_REGISTRY,
			new ResourceLocation("creativedelight", "cookware_pickup"));
	private static final TagKey<Block> TRAY_HEAT_SOURCES = TagKey.create(Registry.BLOCK_REGISTRY,
			new ResourceLocation("farmersdelight", "tray_heat_sources"));

	/**
	 * A held attack button re-sends the click every tick in survival and every 6 ticks in creative; clicks closer than
	 * this many ticks to the previous one at the same block are the same punch.
	 */
	private static final int HELD_GAP = 7;
	private static final Map<UUID, long[]> LAST_PUNCH = new ConcurrentHashMap<>();

	public enum Toggle {
		LID("lid"), HANDLE("handle");

		public final String key;

		Toggle(String key) {
			this.key = key;
		}

		public Component hint() {
			return Component.translatable("creativedelight.wthit.punch_toggle." + key);
		}
	}

	private CookwareHandling() {}

	public static boolean enabled(BlockState state) {
		return state.is(COOKWARE) && CDConfig.qol(CDConfig.COOKWARE_PICKUP);
	}

	private static boolean emptyHanded(Player player) {
		return player.getMainHandItem().isEmpty() && !player.isSpectator();
	}

	/** The state a punch toggles on this block, or null (the skillet). */
	@Nullable
	public static Toggle toggleOf(BlockState state) {
		Property<?> lid = state.getBlock().getStateDefinition().getProperty("lid");
		if (lid instanceof BooleanProperty) return Toggle.LID;
		if (handleValue(state) != null) return Toggle.HANDLE;
		return null;
	}

	@Nullable
	private static Enum<?> handleValue(BlockState state) {
		if (!(state.getBlock().getStateDefinition().getProperty("support") instanceof EnumProperty<?> support)) return null;
		for (Object value : support.getPossibleValues()) {
			if (value instanceof Enum<?> e && e.name().equals("HANDLE")) return e;
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static BlockState toggled(BlockState state, Level level, BlockPos pos, Toggle toggle) {
		if (toggle == Toggle.LID) {
			BooleanProperty lid = (BooleanProperty) state.getBlock().getStateDefinition().getProperty("lid");
			return state.setValue(lid, !state.getValue(lid));
		}
		EnumProperty support = (EnumProperty) state.getBlock().getStateDefinition().getProperty("support");
		Enum<?> handle = handleValue(state);
		if (state.getValue(support) != handle) return state.setValue(support, (Comparable) handle);
		String tray = level.getBlockState(pos.below()).is(TRAY_HEAT_SOURCES) ? "TRAY" : "NONE";
		for (Object value : support.getPossibleValues()) {
			if (((Enum<?>) value).name().equals(tray)) return state.setValue(support, (Comparable) value);
		}
		return state;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!player.isShiftKeyDown() || !emptyHanded(player) || !enabled(state)) return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (event.getHand() != InteractionHand.MAIN_HAND || !(level instanceof ServerLevel server)) return;
		if (!player.mayBuild() || !level.mayInteract(player, pos)) return;
		if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) return;

		BlockEntity be = level.getBlockEntity(pos);
		List<ItemStack> contents = takeContents(be);
		List<ItemStack> drops = Block.getDrops(state, server, pos, be, player, ItemStack.EMPTY);
		ItemStack station = drops.isEmpty() ? new ItemStack(state.getBlock()) : drops.get(0).copy();
		CompoundTag tag = station.getTag();
		if (tag != null) {
			tag.remove("BlockEntityTag");
			if (tag.isEmpty()) station.setTag(null);
		}
		// Inventory is empty now, so the block's onRemove throws nothing out
		if (!level.removeBlock(pos, false)) return;
		level.levelEvent(2001, pos, Block.getId(state));
		SoundType sound = state.getSoundType(level, pos, player);
		level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		player.setItemInHand(InteractionHand.MAIN_HAND, station);
		for (ItemStack stack : contents) player.getInventory().placeItemBackInInventory(stack);
	}

	/** Empties the block entity's whole inventory ({@code getInventory()} on all four mods' block entities). */
	private static List<ItemStack> takeContents(@Nullable BlockEntity be) {
		List<ItemStack> out = new ArrayList<>();
		if (be == null) return out;
		IItemHandler inv;
		try {
			Method m = be.getClass().getMethod("getInventory");
			if (!(m.invoke(be) instanceof IItemHandler handler)) return out;
			inv = handler;
		} catch (ReflectiveOperationException e) {
			return out;
		}
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack;
			if (inv instanceof IItemHandlerModifiable mod) {
				stack = mod.getStackInSlot(slot).copy();
				mod.setStackInSlot(slot, ItemStack.EMPTY);
			} else {
				stack = inv.extractItem(slot, Integer.MAX_VALUE, false);
			}
			if (!stack.isEmpty()) out.add(stack);
		}
		be.setChanged();
		return out;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!emptyHanded(player) || !enabled(state)) return;
		Toggle toggle = toggleOf(state);
		if (toggle == null) return;
		// Cancelled on both sides: no break progress on the client, and the server ignores the start of the break
		event.setCanceled(true);
		if (level.isClientSide || !level.mayInteract(player, pos)) return;
		long now = level.getGameTime();
		long[] last = LAST_PUNCH.put(player.getUUID(), new long[] {pos.asLong(), now});
		if (last != null && last[0] == pos.asLong() && now - last[1] < HELD_GAP) return;
		level.setBlockAndUpdate(pos, toggled(state, level, pos, toggle));
		level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
	}
}
