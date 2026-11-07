package com.SaintAlfonzo.creativecommon.mixin;

// Reserved for Creative Economy: wandering trader trades and spawning move to the future economy mod (trading and loot
// chests; owner, 2026-11-07, creative_split.md 2.3). Stays in Common until that mod is planned.

import com.SaintAlfonzo.creativecommon.trader.WanderingTraderTweaks;

import net.minecraft.world.entity.npc.WanderingTraderSpawner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Sets the wait between Wandering Trader spawn rolls ({@code [wandering_trader] traderIntervalTicks}).
 * Bytecode-confirmed (1.19.2, Forge 43.3.9): the only {@code sipush 24000} in {@code tick}
 * ({@code m_7995_}) is the reset after a roll, and the only one in the constructor is the first wait
 * of a brand new world. Vinery's spawn mixin can swap the spawned entity inside {@code spawn}; the
 * pack sets its chance to 0 and spawns the Winemaker from the Vinery port's own {@code WinemakerSpawner} instead.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false}.
 */
@Mixin(WanderingTraderSpawner.class)
public abstract class WanderingTraderSpawnerMixin {

    @ModifyConstant(method = {"m_7995_", "<init>"}, constant = @Constant(intValue = 24000), remap = false)
    private int creativecommon$spawnDelay(int vanilla) {
        try {
            return WanderingTraderTweaks.spawnDelay(vanilla);
        } catch (Throwable ignored) {
            return vanilla;
        }
    }
}
