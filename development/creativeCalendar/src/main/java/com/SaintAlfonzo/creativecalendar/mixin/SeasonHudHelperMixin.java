package com.SaintAlfonzo.creativecalendar.mixin;

import club.iananderson.seasonhud.config.SeasonHudClient;
import club.iananderson.seasonhud.forge.platform.ForgeSeasonHelper;
import club.iananderson.seasonhud.impl.accessory.mods.Calendar;
import club.iananderson.seasonhud.impl.season.components.Seasons;
import club.iananderson.seasonhud.impl.season.components.SubSeasons;
import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
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
 *   year (day N of 12 sub-seasons) whatever the HUD's day setting;
 * - a permanent season shows its own season with SeasonHUD's normal name: permanent summer and winter as
 *   "Summer" / "Winter" with the day of the year; permanent spring and autumn as Early/Mid/Late Spring or
 *   Autumn (when the HUD shows sub-seasons) with the day within that part (Early and Late 1/4 of the year,
 *   Mid 1/2), or the day of their own year otherwise.
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
		SeasonTypes.Type type = SeasonTypes.hudTypeAt(player.level, player.getOnPos());
		if (type == null) return;
		cir.setReturnValue(switch (type) {
			case ARID -> Seasons.DRY;
			case PERMANENT_SPRING -> Seasons.SPRING;
			case PERMANENT_SUMMER -> Seasons.SUMMER;
			case PERMANENT_AUTUMN -> Seasons.AUTUMN;
			default -> Seasons.WINTER; // frozen, permanent winter
		});
	}

	@Inject(method = "getCurrentSereneSubSeason", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$yearRoundSubSeason(Player player, CallbackInfoReturnable<SubSeasons> cir) {
		SeasonTypes.Type type = SeasonTypes.hudTypeAt(player.level, player.getOnPos());
		if (type == null) return;
		if (splitYear(type)) {
			cir.setReturnValue(switch (SeasonTypes.permanentPart(type, player.level)) {
				case EARLY -> SubSeasons.EARLY;
				case MID -> SubSeasons.MID;
				case LATE -> SubSeasons.LATE;
			});
		} else {
			cir.setReturnValue(SubSeasons.NONE);
		}
	}

	@Inject(method = "getSereneDate", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$dayOfYear(Player player, CallbackInfoReturnable<Long> cir) {
		SeasonTypes.Type type = SeasonTypes.hudTypeAt(player.level, player.getOnPos());
		if (type == null) return;
		cir.setReturnValue(splitYear(type)
				? (long) SeasonTypes.permanentDate(type, player.level, byPart(player))[0]
				: (long) SeasonTypes.dayOfYear(player.level));
	}

	@Inject(method = "sereneSeasonDurationDays", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$yearDays(Player player, CallbackInfoReturnable<Integer> cir) {
		SeasonTypes.Type type = SeasonTypes.hudTypeAt(player.level, player.getOnPos());
		if (type == null) return;
		cir.setReturnValue(splitYear(type)
				? SeasonTypes.permanentDate(type, player.level, byPart(player))[1]
				: SeasonTypes.yearDays(player.level));
	}

	private static boolean splitYear(SeasonTypes.Type type) {
		return type == SeasonTypes.Type.PERMANENT_SPRING || type == SeasonTypes.Type.PERMANENT_AUTUMN;
	}

	/** SeasonHUD's own rule for counting days within the sub-season rather than the season. */
	private static boolean byPart(Player player) {
		return SeasonHudClient.getShowSubSeason() && Calendar.validDetailedMode(player);
	}
}
