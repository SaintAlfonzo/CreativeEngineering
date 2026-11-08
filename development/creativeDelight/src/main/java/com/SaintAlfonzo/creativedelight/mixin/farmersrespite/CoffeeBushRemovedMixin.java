package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.CoffeeBushBlock;
import umpaz.farmersrespite.common.block.CoffeeBushTopBlock;
import umpaz.farmersrespite.common.block.CoffeeDoubleStemBlock;
import umpaz.farmersrespite.common.block.CoffeeMiddleStemBlock;
import umpaz.farmersrespite.common.block.CoffeeStemBlock;

/**
 * Coffee merge (item_merges.md 2.8): Farmer's Respite's coffee plant is removed. Its beans are merged into Rustic
 * Delight's, so no new bush can be planted; a bush already in a world (any of the five plant blocks) breaks on its next
 * random tick instead of growing, so it never drains a nearby crop into Wither Roots (crops.md G14). Its drops go
 * through the item merge's loot swap and come out as Rustic Delight's coffee items. Every piece ticks, so a grown
 * stem (which FR stops ticking) is removed too. Applies only with Rustic Delight loaded (CDMixinPlugin
 * NEEDS_ALSO_MOD), the same condition as the coffee merge list. Methods are named twice: SRG for the pack, official
 * for dev runs.
 */
@Mixin(value = {CoffeeBushBlock.class, CoffeeBushTopBlock.class, CoffeeStemBlock.class, CoffeeMiddleStemBlock.class,
		CoffeeDoubleStemBlock.class}, remap = false)
public abstract class CoffeeBushRemovedMixin {

	@Inject(method = {"m_6724_", "isRandomlyTicking"}, at = @At("HEAD"), cancellable = true, require = 1)
	private void creativedelight$alwaysTick(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Inject(method = {"m_213898_", "randomTick"}, at = @At("HEAD"), cancellable = true, require = 1)
	private void creativedelight$breakInstead(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
											   CallbackInfo ci) {
		ci.cancel();
		level.destroyBlock(pos, true);
	}
}
