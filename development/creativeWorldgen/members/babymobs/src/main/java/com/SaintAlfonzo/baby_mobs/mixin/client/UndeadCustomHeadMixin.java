package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Head items (skulls, pumpkins, banners) on remodelled babies: 1.19.2 shrinks them to 0.7 and drops them by a block for
 * any baby, sized for the old half-scale body; 26.1 draws them full size on the baby head, which the head part's own
 * transform already places. SRG, remap = false: m_6494_ render, m_6162_ isBaby.
 */
@Mixin(CustomHeadLayer.class)
public abstract class UndeadCustomHeadMixin {

    @Redirect(method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;m_6162_()Z"), remap = false)
    private boolean babyMobs$fullSizeHead(LivingEntity entity) {
        return !BabyRender.active() && entity.isBaby();
    }
}
