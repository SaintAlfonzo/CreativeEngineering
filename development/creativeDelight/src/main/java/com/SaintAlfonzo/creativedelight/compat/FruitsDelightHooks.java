package com.SaintAlfonzo.creativedelight.compat;

import dev.xkmc.fruitsdelight.events.FoodDataAccessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

/**
 * Replacements for calls in Fruits Delight's merged mixin handlers; {@link CDMixinPlugin} rewrites the handlers to
 * call these instead. Only reached when Fruits Delight is loaded (the
 * call lives in its handler), so the Fruits Delight type here is never resolved without it.
 */
public final class FruitsDelightHooks {
	private FruitsDelightHooks() {}

	/** {@code accessor.fruitsdelight$setPlayer(player)}, skipped for a null FoodData (Fruits Delight 1.1.3's guard). */
	public static void setPlayer(Object accessor, Player player) {
		if (accessor != null) ((FoodDataAccessor) accessor).fruitsdelight$setPlayer(player);
	}

	/**
	 * Replaces the {@code this.is(BlockTags.LEAVES)} test at the top of Fruits Delight's collision-shape handler: true
	 * for a leaf block queried by a projectile, the only case the handler goes on to examine. Same result as the
	 * original two tests together, with the cheap test first.
	 */
	public static boolean isLeafForProjectile(BlockStateBase state, CollisionContext ctx) {
		return ctx instanceof EntityCollisionContext ectx
				&& ectx.getEntity() instanceof Projectile
				&& state.is(BlockTags.LEAVES);
	}
}
