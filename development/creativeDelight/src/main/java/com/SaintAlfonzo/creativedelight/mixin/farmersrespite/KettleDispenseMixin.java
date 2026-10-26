package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.block.entity.dispenser.KettleDispenseBehavior;

/**
 * A dispenser facing a Farmer's Respite Kettle (farmers_respite.md K7): water bucket and bottles follow the tank rules
 * ({@link KettleTank#dispense}): a full tank of four, one fluid at a time. Anything else is left to 2.0.
 */
@Mixin(value = KettleDispenseBehavior.class, remap = false)
public abstract class KettleDispenseMixin {

	@Inject(method = {"m_6115_", "dispense"}, at = @At("HEAD"), cancellable = true, require = 1)
	private void ce$tankDispense(BlockSource source, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
		BlockEntity be = source.getLevel().getBlockEntity(pos);
		if (!(be instanceof KettleBlockEntity)) return;
		ItemStack out = KettleTank.dispense(be, stack);
		if (out != null) cir.setReturnValue(out);
	}
}
