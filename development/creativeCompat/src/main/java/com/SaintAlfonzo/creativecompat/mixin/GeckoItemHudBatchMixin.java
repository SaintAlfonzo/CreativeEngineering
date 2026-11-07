package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.GeckoHudBatch;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

/**
 * GeckoLib 3.1.40 {@code GeoItemRenderer.renderByItem} ends its GUI branch with {@code bufferSource.endBatch()}. With
 * ImmediatelyFast's HUD batching on, that source is ImmediatelyFast's hotbar item batch, so the call drew every slot
 * left of a GeckoLib item (the Portal Gun) early, outside ImmediatelyFast's render state: those slots and the gun came
 * out desaturated. Skip the flush when the source is that batch; ImmediatelyFast draws the whole hotbar once at the end.
 * Any other buffer (inventories, JEI: no screen batching) is flushed as before. plan_portal_gun.md 2, decision D5.
 */
@Mixin(value = GeoItemRenderer.class, remap = false)
public abstract class GeckoItemHudBatchMixin {

    @Redirect(method = "m_108829_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;m_109911_()V"))
    private void creativecompat$keepHudBatch(MultiBufferSource.BufferSource source) {
        if (!GeckoHudBatch.isItemBatch(source)) source.endBatch();
    }
}
