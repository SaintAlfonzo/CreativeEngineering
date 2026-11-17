package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassThrough;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels {@code Entity#makeStuckInBlock} ({@code m_7601_}) at HEAD when the block is in a pass-through tier
 * (plan_passthrough.md 1.2): {@code surface.PassThrough} applies that block's speed instead. One hook covers every
 * caller (vanilla cobwebs and berry bushes, SecurityCraft, Fantasy Furniture, Create Crafts & Additions barbed wire,
 * IW leaf piles, Twigs bamboo leaves, Nether's Delight propelplant); their other {@code entityInside} effects (berry,
 * thorn, wire damage) stay. Spider's override calls this only outside {@code minecraft:cobweb}, so a spider still
 * passes cobwebs; the tier-wide spider exemption is the {@code creativecompat:passthrough_exempt/cobweb} tag.
 * SecurityCraft's owner pass-through moved to {@code surface.PassOwner}. No other mod mixes into this method
 * (phase 0 brief 1.2).
 */
@Mixin(value = Entity.class, remap = false)
public abstract class MakeStuckInBlockMixin {

	@Inject(method = "m_7601_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$passThroughStuck(BlockState state, Vec3 multiplier, CallbackInfo ci) {
		boolean cancel;
		try {
			cancel = PassThrough.cancelsStuck((Entity) (Object) this, state);
		} catch (Throwable t) {
			cancel = false; // keep vanilla's slowdown if the lookup breaks
		}
		if (cancel) {
			ci.cancel();
		}
	}
}
