package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * More Mob Variants' pig mud, sheep horns and sheared-wool colour layers are drawn for the adult (or old half-size baby)
 * geometry; on a remodelled baby (variantBabies = NEW_MODELS, pig / sheep remodelled) they are skipped. Optional
 * targets: absent mod = no-op (require = 0). m_6494_ = RenderLayer.render (the bridge every call goes through).
 */
@Pseudo
@Mixin(targets = {
        "com.github.nyuppo.client.renderer.entity.layers.PigMudLayer",
        "com.github.nyuppo.client.renderer.entity.layers.SheepHornsLayer",
        "com.github.nyuppo.client.renderer.entity.layers.ShearedWoolColorLayer"})
public abstract class PetMoreMobVariantsLayersMixin {

    @Inject(method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void babyMobs$skipOnBaby(PoseStack pose, MultiBufferSource buffers, int light, Entity entity, float limbSwing,
                                     float limbAmount, float partialTick, float age, float yaw, float pitch, CallbackInfo ci) {
        if (BabyRender.active()) ci.cancel();
    }
}
