package com.SaintAlfonzo.creativecommon.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativecommon.effect.PhantomBaneRules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.PhantomSpawner;

/**
 * Better Beacons 1.0.2's {@code PhantomSpawnerMixin} injects at {@code Player.isSpectator()} inside the per-player loop of
 * {@code PhantomSpawner.tick} ({@code m_7995_}) and returns 0 from the whole method when that player has Phantom Bane, so
 * one player with Phantom Bane (beacon, echo armor set, Caffeinated's hidden part) cancelled the spawn pass for every
 * player after them in the list ({@code potions/effects #13}, effects.md 2.7). Here the loop's
 * {@code level.players()} ({@code m_6907_}, called once in the method) is handed a copy without the Phantom Bane
 * players, so Better Beacons' check never meets one and only those players are skipped. In the better_beacons
 * compartment: without Better Beacons there is no Phantom Bane and nothing to fix.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerBaneSkipMixin {

    @Redirect(method = "m_7995_", remap = false,
            at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/server/level/ServerLevel;m_6907_()Ljava/util/List;"))
    private List<ServerPlayer> creativeengineering$withoutPhantomBane(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (!PhantomBaneRules.anyHasPhantomBane(players)) return players;
        return players.stream().filter(p -> !PhantomBaneRules.hasPhantomBane(p)).toList();
    }
}
