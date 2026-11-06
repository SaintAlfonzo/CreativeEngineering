package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkins;
import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkinsClient;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackLayerRenderer;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.IBackpackModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Worn look (the chest layer and the Curios renderer both end here): after SB moves the pose onto the back, the
 * skin block is drawn in place of the backpack model. No upgrades or display item. Only reachable with the equip switch on.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(value = BackpackLayerRenderer.class, remap = false)
public abstract class BackpackSkinWornMixin {

    @Inject(method = "renderBackpack", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void creativeengineering$wornSkin(EntityModel<?> parentModel, LivingEntity entity, PoseStack ps,
            MultiBufferSource buffer, int light, ItemStack backpack, boolean wearsArmor, IBackpackModel model, CallbackInfo ci) {
        Block skin = BackpackSkins.getSkin(backpack);
        if (skin == null) return;
        // raw call: SB's generic signature cannot be satisfied from a wildcard model
        ((IBackpackModel) model).translateRotateAndScale((EntityModel) parentModel, entity, ps, wearsArmor);
        BackpackSkinsClient.renderWorn(skin, ps, buffer, light);
        ci.cancel();
    }
}
