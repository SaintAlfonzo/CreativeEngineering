package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Baby horse markings (26.1 *_baby marking textures, through the horse kit's layerTexture swaps). 1.19.2's marking
 * layer draws the parent model itself instead of going through RenderLayer.renderColoredCutoutModel, so the swap is
 * applied to its texture local here. m_6494_ render.
 */
@Mixin(HorseMarkingLayer.class)
public abstract class EquineHorseMarkingMixin {

    @ModifyVariable(method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Horse;FFFFFF)V",
            at = @At("STORE"), ordinal = 0, remap = false)
    private ResourceLocation babyMobs$markingTexture(ResourceLocation texture) {
        return texture == null ? null : BabyRender.layerTexture(texture);
    }
}
