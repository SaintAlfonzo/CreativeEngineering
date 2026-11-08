package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonAnswers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.config.ServerConfig;

/**
 * Serene Seasons' dimension whitelist check (a config read and a list scan, on every grass and foliage tint)
 * remembered per dimension in {@link SeasonAnswers} until a tag or config reload (perf.md 19).
 */
@Mixin(value = ServerConfig.class, remap = false)
public class SeasonServerConfigMixin {

	@Inject(method = "isDimensionWhitelisted", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$knownDimension(ResourceKey<Level> dim, CallbackInfoReturnable<Boolean> cir) {
		Boolean known = SeasonAnswers.dimension(dim);
		if (known != null) cir.setReturnValue(known);
	}

	@Inject(method = "isDimensionWhitelisted", at = @At("RETURN"), remap = false)
	private static void creativeengineering$rememberDimension(ResourceKey<Level> dim, CallbackInfoReturnable<Boolean> cir) {
		SeasonAnswers.putDimension(dim, cir.getReturnValueZ());
	}
}
