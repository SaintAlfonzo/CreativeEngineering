package com.SaintAlfonzo.creativedelight.hearth.effect;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;

/**
 * Pungent (mobs flee the holder) and Tempting (animals walk to it). Fix B10: upstream gives every mob two goals that each
 * scan a 17-block box every second; here each holder looks for its neighbours once a second, and a mob only gets a goal
 * the first time a holder is near it. With no holder in the world this costs nothing.
 */
public final class EffectAuras {
    private static final Set<LivingEntity> HOLDERS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<Mob, AuraGoal> PUNGENT_GOALS = new WeakHashMap<>();
    private static final Map<Mob, AuraGoal> TEMPTING_GOALS = new WeakHashMap<>();
    private static final double BASE_RADIUS = 8.0D;
    private static int ticks;

    private EffectAuras() {
    }

    private static boolean isAura(MobEffect effect) {
        return effect == HHEffects.PUNGENT.get() || effect == HHEffects.TEMPTING.get();
    }

    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEntity().level.isClientSide && isAura(event.getEffectInstance().getEffect())) HOLDERS.add(event.getEntity());
    }

    /** Effects saved with an entity do not fire Added when it loads. */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living
                && (living.hasEffect(HHEffects.PUNGENT.get()) || living.hasEffect(HHEffects.TEMPTING.get()))) {
            HOLDERS.add(living);
        }
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || HOLDERS.isEmpty() || ++ticks % 20 != 0) return;
        HOLDERS.removeIf(holder -> holder.isRemoved() || !holder.isAlive()
                || !(holder.hasEffect(HHEffects.PUNGENT.get()) || holder.hasEffect(HHEffects.TEMPTING.get())));
        for (LivingEntity holder : HOLDERS) {
            pulse(holder, HHEffects.PUNGENT.get(), PUNGENT_GOALS, true);
            pulse(holder, HHEffects.TEMPTING.get(), TEMPTING_GOALS, false);
        }
    }

    private static void pulse(LivingEntity holder, MobEffect effect, Map<Mob, AuraGoal> goals, boolean flee) {
        MobEffectInstance instance = holder.getEffect(effect);
        if (instance == null) return;
        double radius = BASE_RADIUS * (1.0D + 0.5D * instance.getAmplifier());
        for (Mob mob : holder.level.getEntitiesOfClass(Mob.class, holder.getBoundingBox().inflate(radius),
                m -> m != holder && m.isAlive() && (flee ? m.getBrain().getMemories().isEmpty() : m instanceof Animal))) {
            AuraGoal goal = goals.computeIfAbsent(mob, m -> {
                AuraGoal created = new AuraGoal(m, effect, flee);
                m.goalSelector.addGoal(1, created);
                return created;
            });
            goal.offer(holder, radius);
        }
    }

    static boolean isResting(Mob mob) {
        if (mob.isPassenger() || mob.isSleeping()) return true;
        if (mob instanceof TamableAnimal tamable && (tamable.isOrderedToSit() || tamable.isInSittingPose())) return true;
        return (mob instanceof Fox fox && fox.isSitting()) || (mob instanceof Cat cat && cat.isLying()) || (mob instanceof Panda panda && panda.isSitting());
    }

    /** Walks away from or to the nearest holder a pulse offered. Speeds as 1.4.1: 1.0 far, 1.5 (flee) or 1.25 (follow) within 4 blocks. */
    private static final class AuraGoal extends Goal {
        private final Mob mob;
        private final MobEffect effect;
        private final boolean flee;
        private LivingEntity source;
        private double radius;
        private int repath;

        AuraGoal(Mob mob, MobEffect effect, boolean flee) {
            this.mob = mob;
            this.effect = effect;
            this.flee = flee;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        void offer(LivingEntity holder, double holderRadius) {
            if (source == null || !valid() || mob.distanceToSqr(holder) < mob.distanceToSqr(source)) {
                source = holder;
                radius = holderRadius;
            }
        }

        private boolean valid() {
            return source != null && source.isAlive() && source.hasEffect(effect) && mob.distanceToSqr(source) < radius * radius && !isResting(mob);
        }

        @Override
        public boolean canUse() {
            return valid();
        }

        @Override
        public boolean canContinueToUse() {
            return valid();
        }

        @Override
        public void start() {
            repath = 0;
        }

        @Override
        public void tick() {
            if (source == null || (--repath > 0 && !mob.getNavigation().isDone())) return;
            repath = 10;
            double near = mob.distanceToSqr(source);
            if (flee) {
                double dx = mob.getX() - source.getX();
                double dz = mob.getZ() - source.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                if (length < 1.0E-4D) {
                    double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
                    dx = Math.cos(angle);
                    dz = Math.sin(angle);
                    length = 1.0D;
                }
                mob.getNavigation().moveTo(mob.getX() + dx / length * radius, mob.getY(), mob.getZ() + dz / length * radius, near < 16.0D ? 1.5D : 1.0D);
            } else {
                mob.getLookControl().setLookAt(source, mob.getMaxHeadYRot() + 20.0F, mob.getMaxHeadXRot());
                if (near < 2.25D) {
                    mob.getNavigation().stop();
                } else {
                    mob.getNavigation().moveTo(source, near < 16.0D ? 1.25D : 1.0D);
                }
            }
        }

        @Override
        public void stop() {
            source = null;
            mob.getNavigation().stop();
        }
    }
}
