// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.TrashCanScreen;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trash Cans 1.0.18: a plain right-click on the Item Trash Can opens {@link TrashCanScreen} (27 slots, deleted on close)
 * instead of the mod's one-slot filter screen; a sneak right-click (empty hand, as vanilla only calls block use then)
 * still opens the filter. The other three cans share TrashCanBlock and are left alone. Target: SuperMartijn642's Core
 * Lib {@code BaseBlock.interact}, which TrashCanBlock overrides (not an SRG name, so {@code remap = false}). Extends
 * BaseBlock only to reach its protected InteractionFeedback enum.
 */
@Pseudo
@Mixin(targets = "com.supermartijn642.trashcans.TrashCanBlock", remap = false)
public abstract class TrashCansItemCanUseMixin extends BaseBlock {

	protected TrashCansItemCanUseMixin(boolean saveTileData, BlockProperties properties) {
		super(saveTileData, properties);
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void creativecompat$trashScreen(BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, Direction side, Vec3 hit, CallbackInfoReturnable<InteractionFeedback> cir) {
		if (player.isShiftKeyDown() || !TrashCanScreen.isItemTrashCan(this)) return;
		if (!level.isClientSide) {
			player.openMenu(TrashCanScreen.menu(pos));
		}
		cir.setReturnValue(InteractionFeedback.SUCCESS);
	}
}
