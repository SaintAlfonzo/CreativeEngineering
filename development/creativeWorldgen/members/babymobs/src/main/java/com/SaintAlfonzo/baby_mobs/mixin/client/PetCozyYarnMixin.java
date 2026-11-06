package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cozy's Improved Cats' yarn ball in a cat's mouth (its CatYarnLayer). Its baby branch (scale 0.75, translate for the
 * old half-size baby) would misplace the ball on the 26.1 baby mesh, whose head is already at the baby position. While a
 * remodelled baby renders: skip that branch, then scale 0.75 after the move to the head (as the fox held-item fix does).
 * Optional target: absent mod = no-op (require = 0).
 */
@Pseudo
@Mixin(targets = "net.cozy.cozysimprovedcats.entity.client.CatYarnLayer")
public abstract class PetCozyYarnMixin {

    private static final String RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Cat;FFFFFF)V";

    @Redirect(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;m_6162_()Z"),
            remap = false, require = 0)
    private boolean babyMobs$oldBabyOffset(Cat cat) {
        return cat.isBaby() && !BabyRender.active();
    }

    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;m_104299_(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            shift = At.Shift.AFTER), remap = false, require = 0)
    private void babyMobs$babyScale(PoseStack pose, MultiBufferSource buffers, int light, Cat cat, float limbSwing, float limbAmount,
                                    float partialTick, float age, float yaw, float pitch, CallbackInfo ci) {
        if (BabyRender.active()) pose.scale(0.75F, 0.75F, 0.75F);
    }
}
