package com.SaintAlfonzo.creativecalendar.mixin;

import club.iananderson.seasonhud.impl.season.CurrentFertility;
import club.iananderson.seasonhud.impl.season.CurrentSeason;
import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.client.Minecraft;
import com.SaintAlfonzo.creativecalendar.CalendarConfig;
import com.SaintAlfonzo.creativecalendar.time.TimeSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SeasonHUD's season name reads "Arid" or "Frozen" in those biomes (lang desc.seasonhud.season.arid /
 * .frozen). The fertility warning, when SeasonHUD shows one in place of the name, still wins.
 * Icon, colour and day count: {@link SeasonHudHelperMixin}. With {@code [time] hudTimeOfDay} the HUD line ends with
 * the time of day, "Mid Spring (Day 3/24) 7:30 pm" (feature_writeup/time.md 3.7, decision T6), where the sky has a
 * day at all.
 */
@Mixin(value = CurrentSeason.class, remap = false)
public class SeasonHudCurrentSeasonMixin {

	@Shadow(remap = false) @Final private Minecraft mc;
	@Shadow(remap = false) @Final private Player player;

	@Inject(method = {"getHudText", "getHudTextNoFormat", "journeymapText"}, at = @At("RETURN"), remap = false)
	private void creativeengineering$timeOfDay(CallbackInfoReturnable<MutableComponent> cir) {
		if (player == null || !TimeSettings.active() || !CalendarConfig.TIME.hudTimeOfDay.get() || player.level.dimensionType().hasFixedTime()) return;
		cir.getReturnValue().append(Component.literal(" " + TimeSettings.clock(player.level.getDayTime())));
	}

	@Inject(method = "getTranslation(Z)Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true, remap = false)
	private void creativeengineering$yearRoundName(boolean showSubSeason, CallbackInfoReturnable<Component> cir) {
		if (player == null || CurrentFertility.getInstance(mc).shouldOverwriteSeason()) return;
		SeasonTypes.Type type = SeasonTypes.yearRoundTypeAt(player.level, player.getOnPos());
		if (type != null) {
			cir.setReturnValue(Component.translatable(type == SeasonTypes.Type.ARID
					? "desc.seasonhud.season.arid" : "desc.seasonhud.season.frozen"));
		}
	}
}
