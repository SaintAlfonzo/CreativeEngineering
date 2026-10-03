package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.DyeableCannedGolem;
import net.mcreator.cannedgoods.client.renderer.CannedGolemRenderer;
import net.mcreator.cannedgoods.entity.CannedGolemEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps the canned golem's texture for its dyed colour (CannedGolemColorMixin). Red keeps the
 * mod's own big_canned_boy.png. The typed getTextureLocation keeps its plain name in the jar;
 * m_5478_ is only the bridge that calls it.
 */
@Mixin(value = CannedGolemRenderer.class, remap = false)
public abstract class CannedGolemRendererMixin {

    @Inject(method = "getTextureLocation(Lnet/mcreator/cannedgoods/entity/CannedGolemEntity;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"), cancellable = true)
    private void ce$dyedTexture(CannedGolemEntity entity, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation texture = DyeableCannedGolem.texture(((DyeableCannedGolem) entity).ce$getColor());
        if (texture != null) {
            cir.setReturnValue(texture);
        }
    }
}
