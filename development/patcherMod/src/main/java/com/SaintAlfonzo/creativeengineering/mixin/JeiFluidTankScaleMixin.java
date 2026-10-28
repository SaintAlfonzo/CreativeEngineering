package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.compat.jei.FluidTankScale;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.library.render.FluidTankRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * JEI's one fluid tank renderer (every {@code setFluidRenderer} slot, any mod's category): the fill height is
 * {@code amount * height / capacity}, so 10 mB in a 1000 mB tank is one pixel and 8000 mB fills a 1000 mB tank like 1000
 * does. This swaps the linear share for {@link FluidTankScale} per {@code [jei] fluidAmountScale} (TODO jei/jei-coverage #7).
 *
 * JEI 11.8.1 {@code drawFluid} hands the height off to the lambda {@code lambda$drawFluid$0(ingredient, height,
 * poseStack, width, sprite)}: long local 7 is the amount, long local 9 the scaled height (first store), which JEI then
 * clamps to 1..height itself.
 */
@Mixin(value = FluidTankRenderer.class, remap = false)
public abstract class JeiFluidTankScaleMixin<T> {

    @Shadow @Final private IPlatformFluidHelperInternal<T> fluidHelper;
    @Shadow @Final private long capacity;

    @ModifyVariable(method = "lambda$drawFluid$0", remap = false, require = 0,
            at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private long creativeengineering$scaleFill(long linear, Object ingredient, int height, PoseStack pose, int width,
                                              TextureAtlasSprite sprite) {
        CEConfig.Jei.FluidScale mode = CEConfig.JEI.fluidAmountScale.get();
        if (mode == CEConfig.Jei.FluidScale.LINEAR) return linear;
        @SuppressWarnings("unchecked")
        long amount = fluidHelper.getAmount((T) ingredient);
        return FluidTankScale.height(mode, amount, capacity, height);
    }
}
