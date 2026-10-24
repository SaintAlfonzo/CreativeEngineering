package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.time.TimeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Xaero's minimap time line reads the pack clock, so it agrees with SeasonHUD's line just above it
 * (feature_writeup/time.md 3.10, TODO time #5): "Day 57, 19:30" next to "Mid Spring (Day 3/24) 7:30 pm", and both day
 * numbers step at sunrise. Xaero's own math is vanilla's (midnight at dayTime 18000, day change there too). Client
 * only: Xaero has no server classes. The lambda name is compiler assigned (25.3.10); after a Xaero update re-check
 * with {@code javap -p}. {@code require = 0} leaves Xaero's text if it moved.
 */
@Pseudo
@Mixin(targets = "xaero.hud.minimap.info.BuiltInInfoDisplays", remap = false)
public class XaeroTimeInfoMixin {
	private static final String ADD_WORDS = "Lxaero/hud/minimap/info/render/compile/InfoDisplayCompiler;addWords(Ljava/lang/String;)V";

	// the TIME display's four addWords calls, in order: "Day N, HH:MM", "HH:MM", "Day N, HH:MM AM", "HH:MM AM"

	@ModifyArg(method = "lambda$static$14", require = 0, remap = false,
			at = @At(value = "INVOKE", target = ADD_WORDS, ordinal = 0, remap = false))
	private static String creativeengineering$day24(String original) {
		return creativeengineering$packText(original, true, false);
	}

	@ModifyArg(method = "lambda$static$14", require = 0, remap = false,
			at = @At(value = "INVOKE", target = ADD_WORDS, ordinal = 1, remap = false))
	private static String creativeengineering$time24(String original) {
		return creativeengineering$packText(original, false, false);
	}

	@ModifyArg(method = "lambda$static$14", require = 0, remap = false,
			at = @At(value = "INVOKE", target = ADD_WORDS, ordinal = 2, remap = false))
	private static String creativeengineering$day12(String original) {
		return creativeengineering$packText(original, true, true);
	}

	@ModifyArg(method = "lambda$static$14", require = 0, remap = false,
			at = @At(value = "INVOKE", target = ADD_WORDS, ordinal = 3, remap = false))
	private static String creativeengineering$time12(String original) {
		return creativeengineering$packText(original, false, true);
	}

	private static String creativeengineering$packText(String original, boolean withDay, boolean twelveHour) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || !TimeSettings.active() || level.dimensionType().hasFixedTime()) return original;
		long dayTime = level.getDayTime();
		String time = twelveHour ? TimeSettings.clock(dayTime) : TimeSettings.clock24(dayTime);
		return withDay ? I18n.get("gui.xaero_day") + " " + TimeSettings.dayNumber(dayTime) + ", " + time : time;
	}
}
