package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.DhDimensions;
import com.seibel.distanthorizons.core.level.AbstractDhServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Distant Horizons 3.0.2 has one generator switch for every dimension (enableDistantGeneration). Here the levels in
 * {@link DhDimensions#NO_GENERATION} (the Nether) answer "no world gen": no background generation of chunks the player
 * never loaded. Chunks the player does load still become LODs, as with PRE_EXISTING_ONLY. Covers singleplayer
 * (DhClientServerLevel) and the dedicated server (DhServerLevel calls this through super). perf.md 19.
 * Pseudo: DH may be removed.
 */
@Pseudo
@Mixin(value = AbstractDhServerLevel.class, remap = false)
public abstract class DhNoGenerationMixin {

	@Unique
	private Boolean creativeengineering$noGeneration;

	@Inject(method = "shouldDoWorldGen", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativeengineering$noGenerationHere(CallbackInfoReturnable<Boolean> cir) {
		Boolean skip = creativeengineering$noGeneration;
		if (skip == null) {
			String dim = ((AbstractDhServerLevel) (Object) this).getServerLevelWrapper().getDimensionName();
			skip = DhDimensions.NO_GENERATION.contains(dim);
			creativeengineering$noGeneration = skip;
		}
		if (skip) cir.setReturnValue(false);
	}
}
