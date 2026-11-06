package com.SaintAlfonzo.creativecompat.cats;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.github.alexthe668.domesticationinnovation.server.entity.FeatherEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Domestication Innovation's Feather on a Stick as a cat command (feature_writeup/cozy_cats.md section 10).
 *
 * <ul>
 *   <li>The cast feather ({@code FeatherEntity}, a {@code FishingHook}) sticks to a mob it hits, as a fishing hook does
 *       ({@code mixin/DiFeatherEntityMixin}); which mobs is {@link #canHook}. Reeling in lets go without pulling the mob
 *       ({@code mixin/FishingHookFeatherPullMixin}).</li>
 *   <li>While it is stuck, the caster's tamed cats within {@link #RANGE} blocks that can move ({@link #canJoin}) take the
 *       hooked mob as their target, and {@link FeatherAttackGoal} (priority 1, above Cozy's home goal and every vanilla
 *       cat goal) chases and claws it. Unhooked, the goal stops and the cat drops the target.</li>
 *   <li>A cat that lands a hit on a mob its owner's feather holds gets Strength, Resistance and Regeneration
 *       ({@code [cats] buffSeconds}, {@code buffLevel}).</li>
 *   <li>On the ground the feather keeps DI's lure; {@link #chasing} lets Cozy's home goal give way to it
 *       ({@code mixin/CozyHomeGoalFeatherMixin}).</li>
 * </ul>
 *
 * State is two weak maps refreshed by every feather's server tick; a mark older than {@link #STALE_TICKS} counts as
 * gone, so nothing has to be cleared when a feather is reeled in or removed. Server thread only.
 */
public final class CatFeather {

    /** DI's own lure range for the feather. */
    public static final double RANGE = 21.0;
    /** A mark not refreshed for this many ticks is gone (the feather ticks every tick while it exists). */
    private static final long STALE_TICKS = 5;
    /** Cats are re-targeted this often while a feather holds a mob. */
    private static final int TARGET_EVERY = 10;

    private record Hook(UUID owner, long tick) {}

    /** Hooked mob -> the caster and the last tick the hook was seen on it. */
    private static final Map<LivingEntity, Hook> HOOKED = new WeakHashMap<>();
    /** Pet DI's lure is pulling -> last tick. */
    private static final Map<Entity, Long> CHASING = new WeakHashMap<>();
    /** Cat -> the mob this class made its target, so a stale target is dropped even if the goal never ran. */
    private static final Map<Cat, LivingEntity> ASSIGNED = new WeakHashMap<>();

    private CatFeather() {}

    private static boolean enabled() {
        return CompatConfig.SPEC.isLoaded() && CompatConfig.CATS.featherHooksMobs.get();
    }

    // ------------------------------------------------------------------ hooking

    /** Whether the feather may stick to {@code entity}. Runs on both sides (the client predicts the hit). */
    public static boolean canHook(FeatherEntity feather, Entity entity) {
        if (!enabled() || !(entity instanceof LivingEntity living) || !living.isAlive() || entity.isSpectator()
                || !entity.isPickable()) {
            return false;
        }
        Player owner = ((FishingHook) feather).getPlayerOwner();
        if (owner != null) {
            if (entity == owner || owner.isPassengerOfSameVehicle(entity)) return false;
            if (entity instanceof OwnableEntity pet && owner.getUUID().equals(pet.getOwnerUUID())) return false;
        }
        if (entity instanceof Player) {
            return CompatConfig.CATS.hookPlayers.get();
        }
        if (!(entity instanceof Mob)) {
            return false; // armour stands and other non-mob living things
        }
        boolean hostile = entity instanceof Enemy && !(entity instanceof NeutralMob);
        return hostile ? CompatConfig.CATS.hookHostile.get() : CompatConfig.CATS.hookNeutralAndPassive.get();
    }

    /** {@code FishingHook.setHookedEntity} (m_150157_, private): also syncs the hooked id to clients. */
    private static final Method SET_HOOKED =
            ObfuscationReflectionHelper.findMethod(FishingHook.class, "m_150157_", Entity.class);

    /** Sticks the feather to {@code entity} as a fishing hook sticks (server; from DiFeatherEntityMixin). */
    public static void hookInto(FishingHook hook, Entity entity) {
        try {
            SET_HOOKED.invoke(hook, entity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("FishingHook.setHookedEntity", e);
        }
    }

    /** Server tick of a feather (from {@code DiFeatherEntityMixin}, after DI's own tick). */
    public static void tick(FeatherEntity feather) {
        // vanilla members through the vanilla type: the DI jar is SRG-named, so reobf maps calls owned by FishingHook
        FishingHook hook = feather;
        if (hook.level.isClientSide || hook.isRemoved()) return;
        long now = hook.level.getGameTime();
        if (hook.isOnGround() && feather.closestPet != null) {
            CHASING.put(feather.closestPet, now);
        }
        Player owner = hook.getPlayerOwner();
        if (owner == null || !enabled() || !(hook.getHookedIn() instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }
        HOOKED.put(target, new Hook(owner.getUUID(), now));
        if (hook.tickCount % TARGET_EVERY != 0) return;
        for (Cat cat : hook.level.getEntitiesOfClass(Cat.class, owner.getBoundingBox().inflate(RANGE),
                c -> c != target && c.isTame() && c.isOwnedBy(owner) && canJoin(feather, c))) {
            if (cat.getTarget() != target) {
                cat.setTarget(target);
                ASSIGNED.put(cat, target);
            }
        }
    }

    /** A cat that can move takes part: DI's rule (not sitting, not on DI's stay command) plus Cozy's Stay mode. */
    private static boolean canJoin(FeatherEntity feather, Cat cat) {
        return cat.isAlive() && !cat.isPassenger() && feather.isPetAmbulatory(cat);
    }

    /** Whether {@code target} is held right now by a feather cast by {@code cat}'s owner. */
    public static boolean isFeatheredFor(Cat cat, LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        Hook hook = HOOKED.get(target);
        return hook != null && cat.level.getGameTime() - hook.tick() <= STALE_TICKS
                && hook.owner().equals(cat.getOwnerUUID());
    }

    /** Whether DI's lure is pulling {@code pet} this tick. */
    public static boolean chasing(Entity pet) {
        Long tick = CHASING.get(pet);
        return tick != null && pet.level.getGameTime() - tick <= STALE_TICKS;
    }

    /** Drops a target this class gave the cat once the feather no longer holds it. */
    static void dropStaleTarget(Cat cat) {
        LivingEntity assigned = ASSIGNED.get(cat);
        if (assigned == null) return;
        if (!isFeatheredFor(cat, assigned)) {
            ASSIGNED.remove(cat);
            if (cat.getTarget() == assigned) cat.setTarget(null);
        }
    }

    // ------------------------------------------------------------------ events (Forge bus; DI loaded)

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Cat cat) {
            cat.goalSelector.addGoal(1, new FeatherAttackGoal(cat));
            if (ModList.get().isLoaded("cozys_improved_cats")) {
                CozyCats.clearDiCommand(cat);
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Cat cat) || cat.level.isClientSide) return;
        if (!isFeatheredFor(cat, event.getEntity())) return;
        int seconds = CompatConfig.CATS.buffSeconds.get();
        if (seconds <= 0) return;
        int ticks = seconds * 20;
        int amp = CompatConfig.CATS.buffLevel.get() - 1;
        cat.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ticks, amp));
        cat.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, amp));
        cat.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks, amp));
    }
}
