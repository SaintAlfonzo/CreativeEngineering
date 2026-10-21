package com.SaintAlfonzo.baby_mobs.mixin;

import com.SaintAlfonzo.baby_mobs.age.BabyAgeHolder;
import com.SaintAlfonzo.baby_mobs.age.BabyAges;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Age storage and the spawn roll for the mobs {@link BabyAges} handles. Mob-level hooks because Squid doesn't
 * override these itself; every other mob falls through the {@code handles} check. SRG names, remap = false (no refmap):
 * m_8097_ defineSynchedData, m_7380_ addAdditionalSaveData, m_7378_ readAdditionalSaveData, m_6518_ finalizeSpawn.
 */
@Mixin(Mob.class)
public abstract class MobBabyMixin implements BabyAgeHolder {

    @Unique
    private int babyMobs$age;

    @Override
    public int babyMobs$getAge() {
        return babyMobs$age;
    }

    @Override
    public void babyMobs$setAgeRaw(int age) {
        babyMobs$age = age;
    }

    @Inject(method = "m_8097_", at = @At("TAIL"), remap = false)
    private void babyMobs$define(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (BabyAges.handles(self)) BabyAges.define(self);
    }

    @Inject(method = "m_7380_", at = @At("TAIL"), remap = false)
    private void babyMobs$save(CompoundTag tag, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (BabyAges.handles(self)) tag.putInt("Age", BabyAges.getAge(self));
    }

    @Inject(method = "m_7378_", at = @At("TAIL"), remap = false)
    private void babyMobs$load(CompoundTag tag, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (BabyAges.handles(self)) BabyAges.setAge(self, tag.getInt("Age"));
    }

    @ModifyVariable(method = "m_6518_", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private SpawnGroupData babyMobs$groupData(SpawnGroupData data) {
        Mob self = (Mob) (Object) this;
        return BabyAges.handles(self) ? BabyAges.groupData(self, data) : data;
    }

    @Inject(method = "m_6518_", at = @At("RETURN"), remap = false)
    private void babyMobs$roll(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                               SpawnGroupData data, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        if (BabyAges.handles(self)) BabyAges.rollBaby(self, data);
    }
}
