package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTank;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTankHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.KettleBlock;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;

/**
 * Farmer's Respite Kettle block (farmers_respite.md K1, K7): {@code water_level} runs 0-4 (2.0: 0-3; placed Kettles
 * keep their level), right-click with a bottle or bucket goes through the tank rules ({@link KettleTank#use}) before
 * 2.0's own water handling, and a Kettle item that kept a tank puts it back when placed. Methods are named twice: SRG
 * for the pack, official for dev runs.
 */
@Mixin(value = KettleBlock.class, remap = false)
public abstract class KettleBlockMixin {

	@ModifyArg(method = "<clinit>", require = 1,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/properties/IntegerProperty;m_61631_(Ljava/lang/String;II)Lnet/minecraft/world/level/block/state/properties/IntegerProperty;"),
			index = 2)
	private static int ce$fourLevels(int max) {
		return KettleTank.MAX_LEVEL;
	}

	@Inject(method = {"m_6227_", "use"}, at = @At("HEAD"), cancellable = true, require = 1)
	private void ce$tankUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (level.isClientSide || !(level.getBlockEntity(pos) instanceof KettleBlockEntity kettle)) return;
		if (KettleTank.use(kettle, player, hand) != KettleTank.Use.PASS) {
			cir.setReturnValue(InteractionResult.SUCCESS);
		}
	}

	@Inject(method = {"m_6402_", "setPlacedBy"}, at = @At("TAIL"), require = 1)
	private void ce$tankFromItem(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack,
			CallbackInfo ci) {
		if (level.isClientSide) return;
		CompoundTag tag = stack.getTagElement("BlockEntityTag");
		if (tag == null || !tag.contains("CETankLevel") || !tag.contains("CETankFluid")) return;
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof KettleTankHolder tank) {
			tank.ce$setTank(FluidStack.loadFluidStackFromNBT(tag.getCompound("CETankFluid")), tag.getInt("CETankLevel"));
		}
	}
}
