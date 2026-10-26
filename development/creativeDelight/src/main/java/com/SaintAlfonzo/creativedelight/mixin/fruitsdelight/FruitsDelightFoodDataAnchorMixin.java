package com.SaintAlfonzo.creativedelight.mixin.fruitsdelight;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Empty on purpose. Its high priority makes it apply to {@code Player} after Fruits Delight's own PlayerMixin, so that
 * {@code compat.CDMixinPlugin#postApply}, which fires for this class, finds Fruits Delight's merged getFoodData handler
 * and adds the null guard. See the plugin for the why.
 */
@Mixin(value = Player.class, priority = 5000)
public abstract class FruitsDelightFoodDataAnchorMixin {
}
