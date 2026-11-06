package com.SaintAlfonzo.creativecalendar.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Cool Rain 1.0.2 (coolrain) cancels LevelRenderer.tickRain at HEAD with its own copy, so Serene Seasons'
 * hooks and {@link SeasonRainColumnMixin} never run, and its copy plays the per-block rain sounds whenever the
 * biome's declared precipitation is RAIN: rain sounds in deserts and in winter snow.
 *
 * Marker only: the edit is bytecode in CalendarMixinPlugin.postApply (see compat.CoolRainHooks). Cool Rain's copy is a
 * handler merged into LevelRenderer under a generated name ({@code handler$<id>$tickRain}), and Mixin 0.8.5 skips
 * merged methods for any regex or wildcard selector, so an injector here cannot reach it (it crashed the client at
 * start). Priority 1500 applies this after Cool Rain (1000), so its postApply sees the merged handler. Applied only
 * with Cool Rain loaded (CalendarMixinPlugin).
 */
@Mixin(value = LevelRenderer.class, priority = 1500)
public abstract class CoolRainSeasonMixin {
}
