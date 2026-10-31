package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.mobs.MonsterBoxVault;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create's drills, saws and their contraption forms break blocks through {@code BlockHelper.destroyBlockAs} with no
 * player, so no {@code BreakEvent} is posted and {@link MonsterBoxVault#onBreak} never sees them. All of them ask
 * this static check first; a vault monster box is unbreakable to them.
 */
@Mixin(value = BlockBreakingKineticBlockEntity.class, remap = false)
public abstract class CreateBreakerMonsterBoxMixin {

    @Inject(method = "isBreakable", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void creativeengineering$skipMonsterBox(BlockState state, float hardness, CallbackInfoReturnable<Boolean> cir) {
        if (MonsterBoxVault.isProtected(state)) cir.setReturnValue(false);
    }
}
