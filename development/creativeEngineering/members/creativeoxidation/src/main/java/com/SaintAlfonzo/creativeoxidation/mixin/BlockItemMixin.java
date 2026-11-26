// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationItemData;
import com.SaintAlfonzo.creativeoxidation.OxidationManager;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placing a staged or waxed item restores its stage (P3), both halves of a door included. The position is taken from
 * the context BlockItem.place really places with (after updatePlacementContext, which moves it for scaffolding). One
 * frame per call, so nested places (Create's fluid tank item places more tanks from inside its own place) each restore
 * their own block.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Unique
	private static final ThreadLocal<Deque<Frame>> creativeoxidation$frames = ThreadLocal.withInitial(ArrayDeque::new);

	@Inject(method = "place", at = @At("HEAD"))
	private void creativeoxidation$capture(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		creativeoxidation$frames.get().push(new Frame(OxidationItemData.read(context.getItemInHand()).orElse(null)));
	}

	@ModifyArg(method = "place", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"),
			index = 0)
	private BlockPlaceContext creativeoxidation$placedAt(BlockPlaceContext context) {
		Frame frame = creativeoxidation$frames.get().peek();
		if (frame != null) frame.pos = context.getClickedPos();
		return context;
	}

	@Inject(method = "place", at = @At("RETURN"))
	private void creativeoxidation$restore(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Frame frame = creativeoxidation$frames.get().poll();
		if (frame == null || frame.stored == null || !cir.getReturnValue().consumesAction()) return;
		OxidationManager.restorePlaced(context.getLevel(), frame.pos != null ? frame.pos : context.getClickedPos(), frame.stored);
	}

	@Unique
	private static final class Frame {
		final OxidationItemData.StoredState stored;
		BlockPos pos;

		Frame(OxidationItemData.StoredState stored) {
			this.stored = stored;
		}
	}
}
