package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseRender;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.CorpseRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Corpse mod's renderer draws through {@link CorpseRender}: live equipment and the withered look
 * (feature_writeup/plan_corpses.md 3.1, rulings 4 and 20). The mod's {@code super.render} (name tag) runs first, then
 * the rest of its body draw is skipped. Applied only with the Corpse mod (CompatMixinPlugin). remap = false.
 */
@Mixin(value = CorpseRenderer.class, remap = false)
public abstract class CorpseRendererMixin {

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void creativecompat$init(EntityRendererProvider.Context context, CallbackInfo ci) {
		CorpseRender.init(context);
	}

	@Inject(method = "render(Lde/maxhenkel/corpse/entities/CorpseEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", shift = At.Shift.AFTER,
					target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;m_7392_(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
			cancellable = true, remap = false)
	private void creativecompat$render(CorpseEntity corpse, float entityYaw, float partialTicks, PoseStack pose,
			MultiBufferSource buffer, int light, CallbackInfo ci) {
		CorpseRender.render(corpse, entityYaw, pose, buffer, light);
		ci.cancel();
	}
}
