// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationItemData;
import com.SaintAlfonzo.creativeoxidation.OxidationManager;
import com.SaintAlfonzo.creativeoxidation.OxidationProperties;
import com.SaintAlfonzo.creativeoxidation.OxidationRules;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Random ticks for aging states (B2 fix): an aging state answers true, every other state falls through to its block's
 * own answer (upstream replaced the answer on every state that carried the properties). The aging step runs first in
 * randomTick; the block's own randomTick then runs only when the block random ticks by itself and the state did not
 * just change ({@link OxidationRules#runOwnRandomTick}).
 *
 * <p>Drops (P3, R8): every loot-table drop of a staged or waxed target goes through getDrops(LootContext.Builder)
 * (player and tool breaks, Create's drills, saws and wrench pickup, explosions, pistons), so the stage is written there
 * instead of on Block.getDrops' entity overload only (upstream).
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
	private void creativeoxidation$agingTicks(CallbackInfoReturnable<Boolean> cir) {
		BlockState state = (BlockState) (Object) this;
		if (!OxidationProperties.hasProperties(state)) return;
		Boolean forced = OxidationRules.forceRandomTicking(OxidationProperties.isAging(state));
		if (forced != null) cir.setReturnValue(forced);
	}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void creativeoxidation$age(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		BlockState state = (BlockState) (Object) this;
		if (!OxidationProperties.isAging(state)) return;
		boolean aged = OxidationManager.randomTick(level, pos, state, random);
		if (!OxidationRules.runOwnRandomTick(state.getBlock().isRandomlyTicking(state), aged)) ci.cancel();
	}

	@Inject(method = "getDrops", at = @At("RETURN"))
	private void creativeoxidation$keepStageInDrops(LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
		OxidationItemData.keepStageInDrops((BlockState) (Object) this, cir.getReturnValue());
	}
}
