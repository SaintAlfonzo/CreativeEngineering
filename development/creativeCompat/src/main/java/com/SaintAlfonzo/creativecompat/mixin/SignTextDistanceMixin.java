package com.SaintAlfonzo.creativecompat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sign text is not drawn more than {@link #TEXT_DISTANCE} blocks from the camera; the sign itself still is. Vanilla
 * measures and draws every line of every sign in block entity range on every frame: 1.8% of the render thread on
 * 2026-11-07 (perf.md 19.3), mostly line widths and glyphs, for text too small to read. Looking through a spyglass
 * keeps all text, as vanilla does for the glowing-text outline.
 *
 * SRG names, remap off (render = m_6922_, getRenderMessages = m_155717_). Cancels just before the text part of render(): by then the model's inner pose is popped and only the outer pose
 * (pushed at the start, popped at the end) is open, so it is popped here.
 */
@Mixin(value = SignRenderer.class, remap = false)
public class SignTextDistanceMixin {

	@Unique
	private static final double TEXT_DISTANCE = 24.0;

	@Inject(method = "m_6922_(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
			cancellable = true, require = 1, remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;m_155717_(ZLjava/util/function/Function;)[Lnet/minecraft/util/FormattedCharSequence;"))
	private void creativeengineering$skipFarText(SignBlockEntity sign, float partialTick, PoseStack poseStack,
												 MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.isScoping()) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		BlockPos pos = sign.getBlockPos();
		double dx = pos.getX() + 0.5 - camera.x, dy = pos.getY() + 0.5 - camera.y, dz = pos.getZ() + 0.5 - camera.z;
		if (dx * dx + dy * dy + dz * dz > TEXT_DISTANCE * TEXT_DISTANCE) {
			poseStack.popPose();
			ci.cancel();
		}
	}
}
