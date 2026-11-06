package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A remodelled baby horse draws no horse armour (26.1 has no baby armour model; 1.19.2 would draw the adult armour at
 * full size, since the young flag stays false). Babies cannot be given armour in survival; this covers summoned ones.
 * m_6494_ render.
 */
@Mixin(HorseArmorLayer.class)
public abstract class EquineHorseArmorMixin {

    @Inject(method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Horse;FFFFFF)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void babyMobs$noBabyArmour(PoseStack pose, MultiBufferSource buffers, int light, Horse horse, float limbSwing,
                                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                                       float headPitch, CallbackInfo ci) {
        if (BabyRender.active()) ci.cancel();
    }
}
