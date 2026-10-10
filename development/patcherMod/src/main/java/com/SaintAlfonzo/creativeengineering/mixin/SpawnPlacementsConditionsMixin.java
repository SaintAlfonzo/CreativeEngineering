package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.SpawnConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The mob_spawns.json spawn conditions ({@link SpawnConditions}: light_only, water_only) in front of the mob's
 * registered spawn rule. {@code SpawnPlacements.checkSpawnRules} (m_217074_) is the one call both natural spawning
 * and chunk-generation herds make, and it runs for mobs without a registered rule too (Alex's Mobs' crow).
 */
@Mixin(value = SpawnPlacements.class, remap = false)
public abstract class SpawnPlacementsConditionsMixin {

    @Inject(method = "m_217074_", at = @At("HEAD"), cancellable = true, remap = false)
    private static <T extends Entity> void creativeengineering$conditions(EntityType<T> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        Boolean verdict = SpawnConditions.check(type, level, reason, pos);
        if (verdict != null) cir.setReturnValue(verdict);
    }
}
