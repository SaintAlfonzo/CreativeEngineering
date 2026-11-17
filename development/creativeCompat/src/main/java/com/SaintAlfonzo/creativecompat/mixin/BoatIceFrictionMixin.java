package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.BoatFriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Boat-only block friction (R3, plan_small_fixes_1116.md): in {@code Boat#getGroundFriction} (m_38377_) the friction of a
 * block listed in {@code [boat_friction] blocks} replaces the block's own. Players, mobs and items keep vanilla
 * slipperiness. The call is Forge's {@code IForgeBlockState#getFriction(LevelReader, BlockPos, Entity)}.
 */
@Mixin(value = Boat.class, remap = false)
public abstract class BoatIceFrictionMixin {
	@Redirect(method = "m_38377_", remap = false, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
	private float creativecompat$boatFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
		Float f = BoatFriction.of(state);
		return f != null ? f : state.getFriction(level, pos, entity);
	}
}
