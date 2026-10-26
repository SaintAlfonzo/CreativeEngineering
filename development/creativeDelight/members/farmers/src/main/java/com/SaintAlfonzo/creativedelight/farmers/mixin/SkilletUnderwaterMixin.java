package com.SaintAlfonzo.creativedelight.farmers.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.utility.TextUtils;

/**
 * A held Skillet will not start cooking underwater and says so (1.3.4 SkilletItem, "Can't cook underwater...").
 * The placed skillet's "flooded" line is not ported: the 1.2.4 skillet block cannot be waterlogged.
 */
@Mixin(value = SkilletItem.class, remap = false)
public abstract class SkilletUnderwaterMixin {

	@Shadow(remap = false)
	private static boolean isPlayerNearHeatSource(Player player, LevelReader level) {
		throw new AssertionError();
	}

	@Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$underwater(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		if (!player.isUnderWater()) return;
		ItemStack skillet = player.getItemInHand(hand);
		if (skillet.getOrCreateTag().contains("Cooking") || !isPlayerNearHeatSource(player, level)) return;
		ItemStack food = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		if (SkilletItem.getCookingRecipe(food, level).isEmpty()) return;
		player.displayClientMessage(TextUtils.getTranslation("item.skillet.underwater"), true);
		cir.setReturnValue(InteractionResultHolder.pass(skillet));
	}
}
