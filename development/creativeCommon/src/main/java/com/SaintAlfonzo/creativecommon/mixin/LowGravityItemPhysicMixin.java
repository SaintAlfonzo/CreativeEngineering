package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Low gravity for dropped items when ItemPhysic is installed. ItemPhysic replaces the gravity step of ItemEntity.tick
 * with its own -0.04 in ItemPhysicServer.updatePre, so LowGravityPullMixin's constant is never used for items. Scales
 * that constant the same way, on both sides (the method runs on the server and on the client). The -0.02 branch of the
 * same method is an item in a fluid and stays as it is, as vanilla's swimming path does. Applies only when ItemPhysic
 * and Create Gravity are both loaded (CommonMixinPlugin): LowGravity needs Create Gravity's classes.
 */
@Mixin(targets = "team.creative.itemphysic.server.ItemPhysicServer", remap = false)
public abstract class LowGravityItemPhysicMixin {

	@ModifyConstant(method = "updatePre", constant = @Constant(doubleValue = -0.04D), remap = false, require = 1)
	private static double creativecommon$lowGravity(double pull, ItemEntity item, RandomSource random) {
		return pull * LowGravity.pullScale(item);
	}
}
