package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code MushroomCow.getWalkTargetValue} (m_5610_) hard-codes mycelium; reads {@code #minecraft:mooshrooms_spawnable_on}
 * instead, for the same reason as {@link AnimalWalkTargetMixin} (mooshrooms in dark forests spawn on grass/dirt).
 */
@Mixin(value = MushroomCow.class, remap = false)
public abstract class MushroomCowWalkTargetMixin {

    @Inject(method = "m_5610_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$spawnableGround(BlockPos pos, LevelReader level, CallbackInfoReturnable<Float> cir) {
        if (level.getBlockState(pos.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON)) cir.setReturnValue(10.0F);
    }
}
