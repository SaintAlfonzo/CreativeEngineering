package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Alex's Mobs' giant squid flees cachalot whales from far off: its AIAvoidWhales goal starts on any whale within 50
 * blocks ({@code canUse}, m_8036_, the AABB inflate) and keeps running while that whale is within 60
 * ({@code canContinueToUse}, m_8045_). Both radii come from {@link CompatConfig.GiantSquid} (feature_writeup/
 * mob_block_tweaks.md section 2.10). The goal class is package-private, so it is named by string. SRG names,
 * remap = false.
 */
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityGiantSquid$AIAvoidWhales", remap = false)
public abstract class AmGiantSquidAvoidWhalesMixin {

    @ModifyConstant(method = "m_8036_", constant = @Constant(floatValue = 50.0F), require = 1, remap = false)
    private float creativecompat$fleeStart(float original) {
        return CompatConfig.GiantSquid.start();
    }

    @ModifyConstant(method = "m_8045_", constant = @Constant(floatValue = 60.0F), require = 1, remap = false)
    private float creativecompat$fleeStop(float original) {
        return CompatConfig.GiantSquid.stop();
    }
}
