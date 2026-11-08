package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonAnswers;
import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.api.season.ISeasonColorProvider;
import sereneseasons.api.season.Season;
import sereneseasons.util.SeasonColorUtil;

import javax.annotation.Nullable;

/**
 * Arid, frozen and permanent winter biomes have no seasons, so Serene Seasons' grass and foliage tint leaves
 * them alone (before, deserts and badlands took the tropical tint and frozen biomes the winter one). Permanent
 * spring, summer and autumn take their own sub-season's tint (Early/Mid/Late Spring, Mid Summer, ...) instead
 * of the world's: the call is repeated with that provider, and the repeat passes through unchanged.
 * See {@link SeasonTypes}.
 */
@Mixin(value = SeasonColorUtil.class, remap = false)
public class SeasonColorUtilMixin {

	@Inject(method = "applySeasonalGrassColouring", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$grass(ISeasonColorProvider provider, Holder<Biome> biome, int originalColour,
												  CallbackInfoReturnable<Integer> cir) {
		if (SeasonTypes.yearRound(biome)) {
			cir.setReturnValue(originalColour);
			return;
		}
		Season.SubSeason own = ownTint(biome);
		if (own != null && provider != own) cir.setReturnValue(SeasonColorUtil.applySeasonalGrassColouring(own, biome, originalColour));
	}

	@Inject(method = "applySeasonalFoliageColouring", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$foliage(ISeasonColorProvider provider, Holder<Biome> biome, int originalColour,
													CallbackInfoReturnable<Integer> cir) {
		if (SeasonTypes.yearRound(biome)) {
			cir.setReturnValue(originalColour);
			return;
		}
		Season.SubSeason own = ownTint(biome);
		if (own != null && provider != own) cir.setReturnValue(SeasonColorUtil.applySeasonalFoliageColouring(own, biome, originalColour));
	}

	/** changeGrassColor / changeFoliageColor read once per config reload, not per tinted block (perf.md 19). */
	@Redirect(method = {"applySeasonalGrassColouring", "applySeasonalFoliageColouring"}, remap = false, require = 2,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;get()Ljava/lang/Object;"))
	private static Object creativeengineering$knownToggle(ForgeConfigSpec.BooleanValue value) {
		return SeasonAnswers.config(value, value::get);
	}

	@Nullable
	private static Season.SubSeason ownTint(Holder<Biome> biome) {
		Level level = Minecraft.getInstance().level;
		return level != null ? SeasonTypes.permanentTint(SeasonTypes.tagged(biome), level) : null;
	}
}
