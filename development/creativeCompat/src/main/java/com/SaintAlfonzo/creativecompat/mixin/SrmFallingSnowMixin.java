// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Snow! Real Magic's falling snow ({@code FallingSnowEntity.tick}, {@code m_8119_}) landing in water turns it to ice
 * only {@link SnowRealMagic#ICE_CHANCE} of the time (ruling R13); otherwise it splashes away as SRM does for any other
 * fluid. The roll replaces the read of SRM's {@code snowMakingIce} key, so that key still switches it off. Gated on
 * {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.entity.FallingSnowEntity", remap = false)
public abstract class SrmFallingSnowMixin {

	@Redirect(method = "m_8119_", remap = false, require = 0, at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
			target = "Lsnownee/snow/SnowCommonConfig;snowMakingIce:Z", remap = false))
	private boolean creativecompat$iceChance() {
		return snownee.snow.SnowCommonConfig.snowMakingIce
				&& ((Entity) (Object) this).level.random.nextDouble() < SnowRealMagic.ICE_CHANCE;
	}
}
