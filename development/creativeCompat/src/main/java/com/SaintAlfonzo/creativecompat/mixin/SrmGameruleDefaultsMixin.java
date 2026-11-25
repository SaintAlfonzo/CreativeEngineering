// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The pack's defaults for Snow! Real Magic's blizzard gamerules (ruling R13): {@code snowrealmagic:blizzardStrength} 2
 * (SRM: 0, off) and {@code snowrealmagic:blizzardFrequency} 3500 (SRM: 10000), set where {@code CoreModule} registers
 * them, so a new world starts with them and {@code /gamerule} still changes them. Blizzards come only in thunder
 * ({@code SrmWorldTickMixin}). Gated on {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.CoreModule", remap = false)
public abstract class SrmGameruleDefaultsMixin {

	private static final String CREATE =
			"Lsnownee/snow/mixin/IntegerValueAccess;callCreate(I)Lnet/minecraft/world/level/GameRules$Type;";

	@ModifyArg(method = "<clinit>", remap = false, require = 0, index = 0,
			at = @At(value = "INVOKE", target = CREATE, ordinal = 0, remap = false))
	private static int creativecompat$blizzardStrength(int value) {
		return SnowRealMagic.BLIZZARD_STRENGTH;
	}

	@ModifyArg(method = "<clinit>", remap = false, require = 0, index = 0,
			at = @At(value = "INVOKE", target = CREATE, ordinal = 1, remap = false))
	private static int creativecompat$blizzardFrequency(int value) {
		return SnowRealMagic.BLIZZARD_FREQUENCY;
	}
}
