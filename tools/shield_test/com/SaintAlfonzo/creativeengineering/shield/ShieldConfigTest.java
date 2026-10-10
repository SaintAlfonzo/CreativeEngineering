package com.SaintAlfonzo.creativeengineering.shield;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.electronwill.nightconfig.core.CommentedConfig;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Headless checks of the shield config (feature_writeup/shields.md section 6): the [shields] spec builds,
 * tick-gate defaults follow the ladder (burning: netherite+, thorns: diamond+) and are live, the disable cooldown is 20,
 * and EffectTickContext only answers for the entity whose effect is ticking and restores nesting.
 */
public class ShieldConfigTest {
    static int fails = 0, checks = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) fails++;
        System.out.println((ok ? "ok   " : "FAIL ") + what);
    }

    public static void main(String[] args) throws Exception {
        CommentedConfig cfg = CommentedConfig.inMemory();
        CEConfig.COMMON_SPEC.correct(cfg);
        CEConfig.COMMON_SPEC.setConfig(cfg);

        for (ShieldTier t : ShieldTier.values()) {
            boolean burn = t.ordinal() >= ShieldTier.NETHERITE.ordinal();
            boolean contact = t.ordinal() >= ShieldTier.DIAMOND.ordinal();
            check(ShieldConfig.blocksTicks(t, ShieldTickGate.BURN) == burn, t.id() + " blocksBurnTicks default = " + burn);
            check(ShieldConfig.blocksTicks(t, ShieldTickGate.CONTACT) == contact, t.id() + " blocksContactTicks default = " + contact);
        }
        check(ShieldTier.GOLD.ordinal() < ShieldTier.NETHERITE.ordinal()
                && ShieldTier.DIAMOND.ordinal() < ShieldTier.NETHERITE.ordinal(), "gold and diamond rank below netherite");

        cfg.set(List.of("shields", "copper", "blocksBurnTicks"), true);
        CEConfig.COMMON_SPEC.afterReload(); // what Forge's file watcher does after an edit
        check(ShieldConfig.blocksTicks(ShieldTier.COPPER, ShieldTickGate.BURN), "blocksBurnTicks is live (copper set to true)");
        cfg.set(List.of("shields", "copper", "blocksBurnTicks"), false);

        check(ShieldConfig.disableCooldownTicks() == 20, "disableCooldownTicks default 20");
        String fx = CEConfig.SHIELDS.unblockableEffectTicks.get();
        for (String id : new String[] {"minecraft:poison", "minecraft:wither", "everythingcopper:copper_poisoning",
                "alexsmobs:debilitating_sting", "alexsmobs:exsanguination", "alexsmobs:ender_flu", "endersdelight:phasing"}) {
            check(fx.contains(id), "unblockableEffectTicks default lists " + id);
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        sun.misc.Unsafe u = (sun.misc.Unsafe) f.get(null);
        LivingEntity a = (LivingEntity) u.allocateInstance(Zombie.class);
        LivingEntity b = (LivingEntity) u.allocateInstance(Zombie.class);

        check(EffectTickContext.tickingOn(a) == null, "no effect ticking outside a tick");
        EffectTickContext.set(a, MobEffects.POISON);
        check(EffectTickContext.tickingOn(a) == MobEffects.POISON, "poison ticking on its own entity");
        check(EffectTickContext.tickingOn(b) == null, "other entity hurt during the tick is not an effect tick");
        LivingEntity outerE = EffectTickContext.entity();
        var outerF = EffectTickContext.effect();
        EffectTickContext.set(b, MobEffects.WITHER);
        check(EffectTickContext.tickingOn(b) == MobEffects.WITHER, "nested tick visible");
        EffectTickContext.set(outerE, outerF);
        check(EffectTickContext.tickingOn(a) == MobEffects.POISON, "outer tick restored after nested one");
        EffectTickContext.set(null, null);
        check(EffectTickContext.tickingOn(a) == null, "cleared after the tick");

        System.out.println(fails == 0 ? "ALL " + checks + " OK" : fails + " FAILED of " + checks);
        System.exit(fails == 0 ? 0 : 1);
    }
}
