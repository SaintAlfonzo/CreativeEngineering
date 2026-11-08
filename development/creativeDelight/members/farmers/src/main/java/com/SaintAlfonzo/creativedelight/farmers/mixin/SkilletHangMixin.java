package com.SaintAlfonzo.creativedelight.farmers.mixin;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportRegistry;
import com.SaintAlfonzo.creativedelight.farmers.block.HangingSkilletBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.item.SkilletItem;

/**
 * Hung skillet (TODO cooking/kitchen #39): the skillet's placement (a sneak right-click, as upstream) on the side of a
 * block hangs it on that wall ({@link HangingSkilletBlock}) when the wall is sturdy and the space above the hanging
 * spot is clear; anything else places the skillet as upstream. The stack (name, enchantments, damage) goes into the
 * skillet block entity, as upstream's {@code updateCustomBlockEntityTag} does for the standing skillet.
 */
@Mixin(value = SkilletItem.class, remap = false)
public abstract class SkilletHangMixin {

	@Inject(method = "m_40576_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$hang(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Player player = context.getPlayer();
		Direction face = context.getClickedFace();
		if (player == null || !player.isShiftKeyDown() || !face.getAxis().isHorizontal() || !context.canPlace()) return;
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (!HangingSkilletBlock.canHang(level, pos, face)) return;
		BlockState state = FDBackportRegistry.HANGING_SKILLET.get().defaultBlockState().setValue(HangingSkilletBlock.FACING, face);
		if (!level.setBlock(pos, state, 11)) return;
		ItemStack stack = context.getItemInHand();
		if (level.getBlockEntity(pos) instanceof SkilletBlockEntity be) be.setSkilletItem(stack);
		level.playSound(player, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
		level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
		if (!player.getAbilities().instabuild) stack.shrink(1);
		cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
	}
}
