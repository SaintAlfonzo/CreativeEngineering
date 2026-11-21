// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips vanilla's dark gradient over the world behind a screen when CompatConfig
 * {@code [screens] dimWorldBehindScreens} is off. {@code m_96558_} = {@code renderBackground(PoseStack, int)},
 * which the one-argument overload calls. Only in a world; the dirt background outside one stays.
 */
@Mixin(value = Screen.class, remap = false)
public abstract class ScreenBackgroundDimMixin {

	@Inject(method = "m_96558_", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private void creativecompat$noDim(PoseStack pose, int vOffset, CallbackInfo ci) {
		if (!CompatConfig.SCREENS.dimWorldBehindScreens.get() && Minecraft.getInstance().level != null) ci.cancel();
	}
}
