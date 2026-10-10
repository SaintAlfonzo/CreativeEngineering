package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code Animal.getWalkTargetValue} (m_5610_) hard-codes grass_block as good ground (10) and falls back to a light
 * value that goes negative at night. Spawning checks it after the spawn rule (PathfinderMob.checkSpawnRules), so
 * with every dirt in {@code #minecraft:animals_spawnable_on} (mob_spawns.json spawn_ground) an animal on podzol
 * would pass the rule and then fail at night. Reading the tag makes the two agree: ground + light, any time.
 * Vanilla's tag is grass_block alone, so with the mob spawn system off nothing changes. Wandering AI uses the same
 * value, so animals favour every spawnable dirt the way they favour grass.
 */
@Mixin(value = Animal.class, remap = false)
public abstract class AnimalWalkTargetMixin {

    @Inject(method = "m_5610_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$spawnableGround(BlockPos pos, LevelReader level, CallbackInfoReturnable<Float> cir) {
        if (level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)) cir.setReturnValue(10.0F);
    }
}
