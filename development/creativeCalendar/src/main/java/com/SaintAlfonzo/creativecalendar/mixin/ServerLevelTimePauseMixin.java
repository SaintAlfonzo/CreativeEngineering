package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.time.TimePause;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code [time] pauseWhenEmpty}: vanilla's +1 dayTime in {@code ServerLevel.tickTime} (m_8809_) is skipped while
 * {@link TimePause#paused}. Only the setDayTime (m_8615_) call: game time and the scheduled events keep running.
 */
@Mixin(ServerLevel.class)
public class ServerLevelTimePauseMixin {

	@Redirect(method = "m_8809_", remap = false,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/server/level/ServerLevel;m_8615_(J)V"))
	private void creativeengineering$pauseWhenEmpty(ServerLevel level, long time) {
		if (!TimePause.paused(level.getServer())) {
			level.setDayTime(time);
		}
	}
}
