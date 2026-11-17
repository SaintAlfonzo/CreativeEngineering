package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassTierSlot;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Gives every block state the pass-through tier slot ({@link PassTierSlot}, plan_passthrough.md 7.2 fallback, scratch
 * fixes). Fields only, no injection. Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = BlockBehaviour.BlockStateBase.class, remap = false)
public abstract class BlockStatePassMixin implements PassTierSlot {

	@Unique
	private int creativecompat$passTier;

	@Override
	public int creativecompat$passTier() {
		return creativecompat$passTier;
	}

	@Override
	public void creativecompat$setPassTier(int packed) {
		creativecompat$passTier = packed;
	}
}
