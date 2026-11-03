package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.family.UndeadBabies;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1 {@code ItemInHandLayer.useBabyOffset} for the humanoid babies: the item sits (0, 1, -4.5)/16 from the hand
 * instead of the adult (+-1, 2, -10)/16, full size (the young flag is off, so 1.19.2's half-size shrink no longer
 * applies, as in 26.1). Applied as the difference, just before the item is drawn. SRG, remap = false:
 * m_117184_ renderArmWithItem, m_109322_ ItemInHandRenderer.renderItem.
 */
@Mixin(ItemInHandLayer.class)
public abstract class UndeadItemInHandMixin {

    @Inject(method = "m_117184_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;m_109322_(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
            remap = false)
    private void babyMobs$babyOffset(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transform, HumanoidArm arm,
                                     PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (!UndeadBabies.active()) return;
        float side = arm == HumanoidArm.LEFT ? -1.0F : 1.0F;
        pose.translate(-side / 16.0F, -1.0F / 16.0F, 5.5F / 16.0F);
    }
}
