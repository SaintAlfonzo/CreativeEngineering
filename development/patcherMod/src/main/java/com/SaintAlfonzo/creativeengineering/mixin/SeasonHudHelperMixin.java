package com.SaintAlfonzo.creativeengineering.mixin;

import club.iananderson.seasonhud.forge.platform.ForgeSeasonHelper;
import club.iananderson.seasonhud.impl.season.components.Seasons;
import club.iananderson.seasonhud.impl.season.components.SubSeasons;
import com.SaintAlfonzo.creativeengineering.seasons.SeasonTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SeasonHUD under the season types ({@link SeasonTypes}):
 * - on a river or beach it shows the season of the land around it;
 * - in an arid or frozen biome there is one season all year: the Dry icon and colour for arid, Winter's for
 *   frozen (the name is swapped in {@link SeasonHudCurrentSeasonMixin}), and the day counts through the whole
 *   year (day N of 12 sub-seasons) whatever the HUD's day setting.
 */
@Mixin(value = ForgeSeasonHelper.class, remap = false)
public class SeasonHudHelperMixin {

	@Redirect(method = "isTropicalSereneSeason", remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/Level;m_204166_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private Holder<Biome> creativeengineering$adaptiveBiome(Level level, BlockPos pos) {
		return SeasonTypes.resolve(level, level.getBiome(pos), pos);
	}

	@Inject(method = "getCurrentSereneSeason", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$yearRoundSeason(Player player, CallbackInfoReturnable<Seasons> cir) {
		SeasonTypes.Type type = SeasonTypes.yearRoundTypeAt(player.level, player.getOnPos());
		if (type != null) cir.setReturnValue(type == SeasonTypes.Type.ARID ? Seasons.DRY : Seasons.WINTER);
	}

	@Inject(method = "getCurrentSereneSubSeason", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$yearRoundSubSeason(Player player, CallbackInfoReturnable<SubSeasons> cir) {
		if (SeasonTypes.yearRoundTypeAt(player.level, player.getOnPos()) != null) cir.setReturnValue(SubSeasons.NONE);
	}

	@Inject(method = "getSereneDate", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$dayOfYear(Player player, CallbackInfoReturnable<Long> cir) {
		if (SeasonTypes.yearRoundTypeAt(player.level, player.getOnPos()) != null) {
			cir.setReturnValue((long) SeasonTypes.dayOfYear(player.level));
		}
	}

	@Inject(method = "sereneSeasonDurationDays", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$yearDays(Player player, CallbackInfoReturnable<Integer> cir) {
		if (SeasonTypes.yearRoundTypeAt(player.level, player.getOnPos()) != null) {
			cir.setReturnValue(SeasonTypes.yearDays(player.level));
		}
	}
}
