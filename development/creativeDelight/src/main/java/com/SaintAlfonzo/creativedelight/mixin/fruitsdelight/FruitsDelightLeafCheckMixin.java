package com.SaintAlfonzo.creativedelight.mixin.fruitsdelight;

import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Empty on purpose. Its high priority makes it apply to {@code BlockStateBase} after Fruits Delight's own mixin, so
 * that {@code compat.CDMixinPlugin#postApply}, which fires for this class, finds Fruits Delight's merged handler and
 * swaps its leaf-tag test for a projectile-first one. See the plugin for the why. (In patcherMod until 2026-10-27.)
 */
@Mixin(value = BlockStateBase.class, priority = 5000)
public abstract class FruitsDelightLeafCheckMixin {
}
