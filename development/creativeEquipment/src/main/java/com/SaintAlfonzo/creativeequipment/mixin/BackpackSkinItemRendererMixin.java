package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkins;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackItemStackRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Held / dropped / inventory look of a skinned backpack: the skin block's own item is drawn instead. Keeps SB's
 * pose handling (it drops the pose ItemRenderer pushed and pushes a fresh one) so the stack stays balanced.
 */
@Mixin(value = BackpackItemStackRenderer.class, remap = false)
public abstract class BackpackSkinItemRendererMixin {

    @Inject(method = "m_108829_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativeengineering$drawSkin(ItemStack stack, TransformType type, PoseStack ps, MultiBufferSource buffer,
            int light, int overlay, CallbackInfo ci) {
        Block skin = BackpackSkins.getSkin(stack);
        if (skin == null) return;
        ps.popPose();
        ps.pushPose();
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(skin.asItem()), type, light, overlay, ps, buffer, 0);
        ci.cancel();
    }
}
