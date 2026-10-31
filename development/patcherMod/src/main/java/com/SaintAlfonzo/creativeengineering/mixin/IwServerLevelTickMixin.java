package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Empty on purpose: a marker that applies to ServerLevel after Immersive Weathering's {@code ServerLevelMixin}
 * (priority 1500 > 1000), so {@code perf.CEMixinPlugin#postApply} sees IW's merged {@code callTick} handler and
 * rewrites it (perf.md 15, candidate C).
 */
@Mixin(value = ServerLevel.class, priority = 1500, remap = false)
public class IwServerLevelTickMixin {
}
