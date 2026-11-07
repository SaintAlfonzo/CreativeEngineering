package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.guititle.TitlePlaques;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells {@link TitlePlaques} which texture a container screen's {@code renderBg} binds first (its
 * background), so the title plaque can be found in it. {@code m_157456_} is
 * {@code setShaderTexture(int, ResourceLocation)}. Outside a capture this is one boolean check.
 */
@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemShaderTextureMixin {

    @Inject(method = "m_157456_", remap = false, require = 1, at = @At("HEAD"))
    private static void creativecommon$captureGuiTexture(int unit, ResourceLocation location, CallbackInfo ci) {
        TitlePlaques.onShaderTexture(unit, location);
    }
}
