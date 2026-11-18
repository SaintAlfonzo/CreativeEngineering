// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.campfire;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Campfires cannot burn in the End (plan_post_scratch_1115 G11, TODO {@code #22}; switch {@code [campfire] endUnlightable}).
 * Soul campfires are exempt ("soul" read from the registry path, as {@link CampfireTorch}). Ordinary fire blocks in the
 * End are Creative Common's.
 * <ul>
 * <li>Flint and steel or a fire charge used on an unlit, dry campfire there: the click is refused, the extinguish sound
 * plays, no durability or charge is used. Torches: {@link CampfireTorch} asks {@link #refuses}.</li>
 * <li>Every other route (placed lit, dispenser flint and steel, burning arrows and fireballs, commands, other mods'
 * igniters): the lit campfire's first server tick ({@code CampfireRainMixin}) puts it out with the extinguish sound
 * and the dowse smoke, as the rain-out does. Food on the grill stays.</li>
 * </ul>
 */
public final class CampfireEnd {

	private CampfireEnd() {}

	/** A campfire state that may not burn here: switch on, the End, a campfire, not a soul one. */
	public static boolean refuses(Level level, BlockState state) {
		return CDConfig.CAMPFIRE.endUnlightable.get()
				&& level.dimension() == Level.END
				&& state.getBlock() instanceof CampfireBlock
				&& !CampfireTorch.isSoul(state.getBlock());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Item item = event.getItemStack().getItem();
		if (!(item instanceof FlintAndSteelItem) && !(item instanceof FireChargeItem)) return;
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!CampfireBlock.canLight(state) || !refuses(level, state)) return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
		if (!level.isClientSide()) level.levelEvent(null, 1009, pos, 0);
	}

	/** Server tick of a lit campfire; true when it was put out (the caller skips the rest of its tick work). */
	public static boolean tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide() || !state.getValue(CampfireBlock.LIT) || !refuses(level, state)) return false;
		level.levelEvent(null, 1009, pos, 0);
		CampfireBlock.dowse(null, level, pos, state);
		level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
		return true;
	}
}
