package com.SaintAlfonzo.creativecompat.mixin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.surface.PassChunkHolder;
import com.SaintAlfonzo.creativecompat.surface.PassCountData;
import com.SaintAlfonzo.creativecompat.surface.PassCounts;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Baked pass-through neighbor counts (surface/PassCounts, plan_post_scratch_1115b.md C2): the chunk holds its count
 * data, and every block change in it ({@code LevelChunk#setBlockState}, both sides; it returns the old state, or null
 * when nothing changed) tells {@link PassCounts#blockChanged}, which drops the counts around a block whose tier changed.
 */
@Mixin(value = LevelChunk.class, remap = false)
public abstract class LevelChunkPassMixin implements PassChunkHolder {

	@Unique
	@Nullable
	private PassCountData creativecompat$passCounts;

	@Override
	public PassCountData creativecompat$passCounts() {
		return creativecompat$passCounts;
	}

	@Override
	public void creativecompat$setPassCounts(PassCountData data) {
		creativecompat$passCounts = data;
	}

	@Inject(method = "m_6978_", at = @At("RETURN"))
	private void creativecompat$blockChanged(BlockPos pos, BlockState state, boolean moving,
											 CallbackInfoReturnable<BlockState> cir) {
		BlockState old = cir.getReturnValue();
		if (old == null) return;
		try {
			PassCounts.blockChanged((LevelChunk) (Object) this, pos, old, state);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("LevelChunkPassMixin", t);
			// a failed invalidation must never stop a block change; the count is fixed by the next load
		}
	}
}
