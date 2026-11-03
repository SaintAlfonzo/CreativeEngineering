package com.SaintAlfonzo.baby_mobs.age;

import com.SaintAlfonzo.baby_mobs.BMConfig;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.animal.Squid;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Age for mobs that aren't {@link AgeableMob}s in 1.19.2 but became ageable later: squid and glow squid (1.21.2's
 * {@code AgeableWaterCreature}) and dolphins. The server keeps {@code age} (negative = baby, counts up to 0 as
 * vanilla's {@link AgeableMob}); clients get a synced baby flag per class. Everything else (sounds pitch, no loot or
 * experience for babies, half-size hitbox through {@code getScale}) is vanilla behaviour keyed off {@code isBaby()}.
 */
public final class BabyAges {
    public static final int BABY_START_AGE = -24000;
    private static EntityDataAccessor<Boolean> SQUID_BABY;
    private static EntityDataAccessor<Boolean> DOLPHIN_BABY;

    private BabyAges() {}

    /** Defines the synced ids before any squid or dolphin exists (mod construction). */
    public static void init() {
        SQUID_BABY = SynchedEntityData.defineId(Squid.class, EntityDataSerializers.BOOLEAN);
        DOLPHIN_BABY = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);
    }

    public static boolean handles(Object entity) {
        return entity instanceof Squid || entity instanceof Dolphin;
    }

    @Nullable
    public static EntityDataAccessor<Boolean> babyKey(Mob mob) {
        if (mob instanceof Squid) return SQUID_BABY;
        if (mob instanceof Dolphin) return DOLPHIN_BABY;
        return null;
    }

    public static boolean isBabyKey(Mob mob, EntityDataAccessor<?> key) {
        EntityDataAccessor<Boolean> own = babyKey(mob);
        return own != null && own.equals(key);
    }

    public static void define(Mob mob) {
        EntityDataAccessor<Boolean> key = babyKey(mob);
        if (key != null) mob.getEntityData().define(key, false);
    }

    public static int getAge(Mob mob) {
        if (mob.level.isClientSide) {
            EntityDataAccessor<Boolean> key = babyKey(mob);
            return key != null && mob.getEntityData().get(key) ? -1 : 1;
        }
        return ((BabyAgeHolder) mob).babyMobs$getAge();
    }

    public static boolean isBaby(Mob mob) {
        return getAge(mob) < 0;
    }

    /** Vanilla {@code AgeableMob.setAge}: stores the age and flips the synced flag when 0 is crossed. */
    public static void setAge(Mob mob, int age) {
        int old = getAge(mob);
        ((BabyAgeHolder) mob).babyMobs$setAgeRaw(age);
        if ((old < 0) != (age < 0)) {
            EntityDataAccessor<Boolean> key = babyKey(mob);
            if (key != null) mob.getEntityData().set(key, age < 0);
            mob.refreshDimensions();
        }
    }

    /** Vanilla {@code AgeableMob.setBaby}: a fresh baby at {@code -growTicks}, or grown up. */
    public static void setBaby(Mob mob, boolean baby) {
        setAge(mob, baby ? -BMConfig.GROW_TICKS.get() : 0);
    }

    /** Vanilla {@code AgeableMob.ageUp(int seconds)}: closer to 0 by {@code seconds * 20}, never past it. */
    public static void ageUp(Mob mob, int seconds) {
        int age = getAge(mob);
        if (age >= 0) return;
        age = Math.min(0, age + seconds * 20);
        setAge(mob, age);
    }

    /** Server tick: a baby's age counts up to 0, where it grows up (vanilla {@code AgeableMob.aiStep}). */
    public static void tick(Mob mob) {
        if (mob.level.isClientSide || !mob.isAlive()) return;
        int age = getAge(mob);
        if (age < 0) setAge(mob, age + 1);
    }

    /** Hitbox scale while young (vanilla's default is 0.5): 26.1 squid 0.5 / 0.8, dolphin 0.65. */
    public static float scale(Mob mob) {
        if (!isBaby(mob)) return 1.0F;
        return mob instanceof Squid ? 0.625F : 0.65F;
    }

    public static float babyChance(Mob mob) {
        if (mob instanceof GlowSquid) return BMConfig.GLOW_SQUID_BABY_CHANCE.get().floatValue();
        if (mob instanceof Squid) return BMConfig.SQUID_BABY_CHANCE.get().floatValue();
        if (mob instanceof Dolphin) return BMConfig.DOLPHIN_BABY_CHANCE.get().floatValue();
        return 0.0F;
    }

    /** {@code finalizeSpawn}: a null group becomes an ageable group with this mob's baby chance (1.21.2 Squid / Dolphin). */
    public static SpawnGroupData groupData(Mob mob, @Nullable SpawnGroupData data) {
        return data != null ? data : new AgeableMob.AgeableMobGroupData(babyChance(mob));
    }

    /** {@code finalizeSpawn}: vanilla AgeableMob's roll. The first member of a group is never a baby. */
    public static void rollBaby(Mob mob, @Nullable SpawnGroupData data) {
        if (!(data instanceof AgeableMob.AgeableMobGroupData group)) return;
        if (group.isShouldSpawnBaby() && group.getGroupSize() > 0 && mob.getRandom().nextFloat() <= group.getBabySpawnChance()) {
            setAge(mob, -BMConfig.GROW_TICKS.get());
        }
        group.increaseGroupSizeByOne();
    }

    /** Baby dolphins are passive ({@code Dolphin.canAttack} in 26.1). */
    public static boolean blocksAttack(LivingEntity attacker) {
        return attacker instanceof Dolphin dolphin && isBaby(dolphin);
    }

    static boolean serverRunning() {
        return ServerLifecycleHooks.getCurrentServer() != null;
    }
}
