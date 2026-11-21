// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.storage;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Sneak pickup (TODO items/storage-and-item-loss #12; feature_writeup/storage_audit.md 7): sneak right-click with an
 * empty main hand on a block in {@code #creativecommon:sneak_pickup} (Supplementaries' sack and Supplementaries
 * Squared's dyed sacks, filled by {@code kubejs/data/creativecommon/kubejs/tags/sneak_pickup_tags.js}) puts it in the
 * hand as its normal player-break drop, like a placed Sophisticated Backpack or Creative Delight's cookware pickup.
 * <p>
 * The block keeps its contents through its own loot table: Supplementaries' sack tables already copy the name and
 * {@code set_contents} the items into {@code BlockEntityTag.Items}, and placing the item restores them (vanilla
 * {@code BlockItem}). An unopened sack from a structure rolls its loot table first, as a break does. Respects break
 * protection (a {@link BlockEvent.BreakEvent} is posted); other drops of the table go to the inventory.
 */
public final class SneakPickup {

	public static final TagKey<Block> SNEAK_PICKUP = TagKey.create(Registry.BLOCK_REGISTRY,
			new ResourceLocation("creativecommon", "sneak_pickup"));

	private SneakPickup() {}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		if (!player.isShiftKeyDown() || player.isSpectator() || !player.getMainHandItem().isEmpty()) return;
		BlockState state = level.getBlockState(pos);
		if (!state.is(SNEAK_PICKUP)) return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (event.getHand() != InteractionHand.MAIN_HAND || !(level instanceof ServerLevel server)) return;
		if (!player.mayBuild() || !level.mayInteract(player, pos)) return;
		if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) return;

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof RandomizableContainerBlockEntity container) container.unpackLootTable(player);
		List<ItemStack> drops = Block.getDrops(state, server, pos, be, player, ItemStack.EMPTY);
		ItemStack held = ItemStack.EMPTY;
		for (ItemStack drop : drops) {
			if (held.isEmpty() && drop.is(state.getBlock().asItem())) held = drop;
		}
		if (held.isEmpty()) return; // a table that does not give the block back: leave the block alone
		// The contents now live in the item; clear the block entity so nothing else can drop them a second time
		if (be instanceof net.minecraft.world.Container c) c.clearContent();
		if (!level.removeBlock(pos, false)) return;
		level.levelEvent(2001, pos, Block.getId(state));
		SoundType sound = state.getSoundType(level, pos, player);
		level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		for (ItemStack drop : drops) {
			if (drop != held && !drop.isEmpty()) player.getInventory().placeItemBackInInventory(drop);
		}
	}
}
