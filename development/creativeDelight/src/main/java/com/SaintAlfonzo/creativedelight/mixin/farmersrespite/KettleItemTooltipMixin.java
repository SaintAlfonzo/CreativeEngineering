package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import java.util.List;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.client.KettleTankRender;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.farmersrespite.common.block.KettleBlock;

/**
 * Farmer's Respite Kettle item tooltip (farmers_respite.md K11): under 2.0's stored-drink lines, the tank it kept
 * ("Tank: Black Tea, 3 / 4"). Client only: the method is stripped on a dedicated server.
 */
@Mixin(value = KettleBlock.class, remap = false)
public abstract class KettleItemTooltipMixin {

	@Inject(method = {"m_5871_", "appendHoverText"}, at = @At("TAIL"), require = 1)
	private void ce$tankLine(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
		Component line = KettleTankRender.itemLine(stack);
		if (line != null) tooltip.add(line);
	}
}
