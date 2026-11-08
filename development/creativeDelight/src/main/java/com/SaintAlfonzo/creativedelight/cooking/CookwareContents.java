package com.SaintAlfonzo.creativedelight.cooking;

import com.SaintAlfonzo.creativedelight.CDConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Cookware keeps its contents (TODO cooking/kitchen #40, QoL-8 {@code cookwareKeepContents}, kitchen.md 2.12): a
 * cooking pot, copper pot or kettle broken by a survival player, or picked up with QoL-7, keeps its ingredients, meal,
 * container slot and output in the item ({@code BlockEntityTag}); the kettle also keeps its tank (KettleTankMixin,
 * already so). The leftover slots (remainder buffer, the kettle's bottle boxes, the copper pot's leftover slot) drop.
 * Placing the item restores them (BlockItem's {@code BlockEntityTag} load).
 * <p>
 * Only a break that will produce the loot item keeps: the break is marked on the block entity for the current game
 * tick ({@link #markKeep}, in the block entity's Forge persistent data so the Miner's member reads it without this
 * class); the station's {@code writeMeal} then writes everything and its {@code getDroppableInventory} leaves the kept
 * slots out (FD / Respite mixins {@code *KeepContentsMixin}, the copper pot in its own code). Unmarked removals
 * (creative breaks, explosions, Create drills, /setblock) drop as before, so nothing is lost when no item follows.
 * <p>
 * The item: glint while it holds anything (mixin.minecraft.ItemStackCookwareGlintMixin) and a "Holds:" tooltip. All
 * three items already stack to 1.
 */
public final class CookwareContents {

	/** Persistent-data key; same string in the Miner's member's CopperPotBlockEntity. */
	public static final String KEEP_KEY = "creativedelight:keep_contents";

	/** The meal slot of each station's stored inventory: shown by the mods' own "Serves" lines, not by "Holds". */
	private static final Map<ResourceLocation, Integer> MEAL_SLOT = Map.of(
			new ResourceLocation("farmersdelight", "cooking_pot"), 6,
			new ResourceLocation("farmersrespite", "kettle"), 2,
			new ResourceLocation("miners_delight", "copper_pot"), 4);

	private CookwareContents() {}

	public static boolean enabled() {
		return CDConfig.qol(CDConfig.COOKWARE_KEEP_CONTENTS);
	}

	/** Marks the coming removal of this station as one that yields its item: its contents go into the item. */
	public static void markKeep(@Nullable BlockEntity be) {
		if (be == null || be.getLevel() == null || !enabled()) return;
		be.getPersistentData().putLong(KEEP_KEY, be.getLevel().getGameTime());
	}

	/** Whether this station is being removed by a marked break this tick. */
	public static boolean keeping(BlockEntity be) {
		Level level = be.getLevel();
		CompoundTag data = be.getPersistentData();
		return level != null && data.contains(KEEP_KEY) && data.getLong(KEEP_KEY) == level.getGameTime();
	}

	/**
	 * A survival player's break of a station that drops its item: mark it. Lowest priority and not for cancelled
	 * events, so claims and protection have had their say.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBreak(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
		BlockState state = event.getState();
		if (player == null || player.isCreative() || !state.is(CookwareHandling.COOKWARE)) return;
		if (!(event.getLevel() instanceof Level level) || level.isClientSide) return;
		BlockPos pos = event.getPos();
		if (!state.canHarvestBlock(level, pos, player)) return;
		markKeep(level.getBlockEntity(pos));
	}

	// ---------------------------------------------------------------------------------------------------------
	// The item

	@Nullable
	private static ResourceLocation stationId(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem item)) return null;
		ResourceLocation id = Registry.ITEM.getKey(item);
		return MEAL_SLOT.containsKey(id) ? id : null;
	}

	/** Items of the stored inventory, the meal slot left out when {@code withMeal} is false. */
	private static List<ItemStack> storedItems(ItemStack stack, boolean withMeal) {
		List<ItemStack> out = new ArrayList<>();
		CompoundTag be = stack.getTagElement("BlockEntityTag");
		if (be == null) return out;
		ResourceLocation id = stationId(stack);
		if (id == null) return out;
		int meal = MEAL_SLOT.get(id);
		ListTag items = be.getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < items.size(); i++) {
			CompoundTag entry = items.getCompound(i);
			if (!withMeal && entry.getInt("Slot") == meal) continue;
			ItemStack held = ItemStack.of(entry);
			if (!held.isEmpty()) out.add(held);
		}
		return out;
	}

	/** True if this station item holds any item or kettle fluid (the glint). Cheap exit for every other stack. */
	public static boolean holdsContents(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("BlockEntityTag", Tag.TAG_COMPOUND) || stationId(stack) == null) return false;
		if (!enabled()) return false;
		CompoundTag be = tag.getCompound("BlockEntityTag");
		if (be.getInt("CETankLevel") > 0) return true;
		return !storedItems(stack, true).isEmpty();
	}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stationId(stack) == null || !enabled()) return;
		List<ItemStack> held = storedItems(stack, false);
		if (held.isEmpty()) return;
		List<Component> tip = event.getToolTip();
		tip.add(Component.translatable("creativedelight.cookware.holds").withStyle(ChatFormatting.GRAY));
		for (ItemStack s : held) {
			tip.add(Component.translatable("creativedelight.cookware.holds_entry", s.getCount(), s.getHoverName())
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
