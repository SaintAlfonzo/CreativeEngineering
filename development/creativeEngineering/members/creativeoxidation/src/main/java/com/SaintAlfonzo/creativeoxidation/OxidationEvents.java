// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Hand interactions on staged blocks (P4): honeycomb waxes; an axe (any item that can AXE_SCRAPE) or Immersive
 * Weathering's steel wool (R6) takes the wax off first, then one stage back, and loses 1 durability. Waxed Up support
 * (upstream) is dropped: the mod is not in the pack. Deployers fire the same event, so they wax and scrape too. Also the
 * coin stack's placement restore (its item is not a BlockItem, so BlockItemMixin does not see it).
 */
public final class OxidationEvents {
	private static final ResourceLocation STEEL_WOOL = new ResourceLocation("immersive_weathering", "steel_wool");

	private OxidationEvents() {
	}

	public static void register() {
		MinecraftForge.EVENT_BUS.addListener(OxidationEvents::onRightClickBlock);
		MinecraftForge.EVENT_BUS.addListener(OxidationEvents::onBlockPlaced);
	}

	/** The tool kind of a held stack, or null. */
	static OxidationRules.Tool toolOf(ItemStack stack) {
		if (stack.isEmpty()) return null;
		if (stack.is(Items.HONEYCOMB)) return OxidationRules.Tool.HONEYCOMB;
		if (stack.canPerformAction(ToolActions.AXE_SCRAPE) || STEEL_WOOL.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
			return OxidationRules.Tool.SCRAPER;
		}
		return null;
	}

	private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		OxidationRules.Tool tool = toolOf(stack);
		if (tool == null || !OxidationConfig.enabled()) return;
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!OxidationProperties.isOxidizable(state)
				|| !OxidationRules.toolApplies(tool, state.getValue(OxidationProperties.WAXED), state.getValue(OxidationProperties.STAGE))) {
			return;
		}
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();
		if (!level.isClientSide) {
			boolean changed;
			if (tool == OxidationRules.Tool.HONEYCOMB) {
				changed = OxidationManager.wax(level, pos, state);
				if (changed && !player.getAbilities().instabuild) stack.shrink(1);
			} else {
				changed = OxidationManager.scrape(level, pos, state) != OxidationManager.ScrapeResult.NONE;
				if (changed) stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
			}
			if (!changed) return;
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
			player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
	}

	/** Create Deco's coin stack item places its block itself; restore the held coins' stage (P3). */
	private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player player) || !(event.getLevel() instanceof Level level) || level.isClientSide) return;
		ResourceLocation placedId = ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock());
		if (placedId == null || !OxidationTargets.PLACER_ITEMS.containsValue(placedId.toString())) return;
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			if (held.getItem() instanceof BlockItem) continue;
			ResourceLocation heldId = ForgeRegistries.ITEMS.getKey(held.getItem());
			if (heldId == null || !placedId.toString().equals(OxidationTargets.placedBlockOf(heldId.toString()))) continue;
			OxidationItemData.read(held).ifPresent(stored -> OxidationManager.restorePlaced(level, event.getPos(), stored));
			return;
		}
	}
}
