// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The Item Trash Can's delete-on-close screen (Trash Cans 1.0.18, {@code trashcans:item_trash_can}; plan_ready_now_1123.md
 * R1, creative_compat.md): a plain right-click opens a three-row chest menu over a fresh 27-slot container, and whatever
 * is left in it when the menu closes is deleted, as FTB Essentials' trash can. A sneak right-click keeps the mod's own
 * filter screen; hopper and pipe insertion go through the block entity and are untouched. Wired by
 * {@code TrashCansItemCanUseMixin}.
 */
public final class TrashCanScreen {

	public static final ResourceLocation ITEM_TRASH_CAN = new ResourceLocation("trashcans", "item_trash_can");
	public static final int SIZE = 27;
	private static final double REACH_SQ = 64.0;

	private TrashCanScreen() {}

	/** True for the Item Trash Can block only (the four Trash Cans blocks share one class). */
	public static boolean isItemTrashCan(Block block) {
		return ITEM_TRASH_CAN.equals(ForgeRegistries.BLOCKS.getKey(block));
	}

	/** The chest menu for the can at {@code pos}; its container empties when the menu closes. */
	public static MenuProvider menu(BlockPos pos) {
		return new SimpleMenuProvider((id, inventory, player) -> new ChestMenu(MenuType.GENERIC_9x3, id, inventory,
				new TrashContainer(pos), 3), Component.translatable("container.creativecompat.trash_can"));
	}

	/** A throwaway container: never saved, cleared in stopOpen (called from ChestMenu.removed). */
	static final class TrashContainer extends SimpleContainer {
		private final BlockPos pos;

		TrashContainer(BlockPos pos) {
			super(SIZE);
			this.pos = pos;
		}

		@Override
		public boolean stillValid(Player player) {
			return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= REACH_SQ;
		}

		@Override
		public void stopOpen(Player player) {
			super.stopOpen(player);
			clearContent();
		}
	}
}
