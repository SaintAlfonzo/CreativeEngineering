package com.SaintAlfonzo.creativecompat.mixin;

import com.seibel.distanthorizons.core.wrapperInterfaces.world.ILevelWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Distant Horizons 3.0.2 draws its own LOD clouds ({@code CloudRenderHandler}) and switches vanilla clouds off.
 * It places them at the level's max build height + 200 (y520 in the overworld) on every frame, ignoring the
 * vanilla cloud height. Here they follow the vanilla cloud height instead ({@code DimensionSpecialEffects.getCloudHeight},
 * which Rubidium Extra's {@code cloud_height} sets, 350 in this pack), so both cloud layers and Pretty Rain's
 * {@code cloudHeight} agree. The same value drives DH's culling check, which reads the same local.
 * Dimensions without vanilla clouds (NaN) keep DH's own height. feature_writeup/weather.md 3.4.
 * Pseudo: client-only mod, may be removed.
 */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.core.render.renderer.CloudRenderHandler", remap = false)
public class DhCloudHeightMixin {

	/** DH adds this to getMaxHeight() for the cloud y. */
	private static final int DH_CLOUD_OFFSET = 200;

	@Redirect(method = "preRender", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/world/ILevelWrapper;getMaxHeight()I"))
	private int creativeengineering$vanillaCloudHeight(ILevelWrapper wrapper) {
		ClientLevel level = Minecraft.getInstance().level;
		float vanilla = level != null ? level.effects().getCloudHeight() : Float.NaN;
		return Float.isNaN(vanilla) ? wrapper.getMaxHeight() : Math.round(vanilla) - DH_CLOUD_OFFSET;
	}
}
