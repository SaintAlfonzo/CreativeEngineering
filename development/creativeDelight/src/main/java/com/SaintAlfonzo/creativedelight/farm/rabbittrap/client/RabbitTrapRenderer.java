// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.rabbittrap.client;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.farm.rabbittrap.RabbitTrap;
import com.SaintAlfonzo.creativedelight.farm.rabbittrap.RabbitTrapBlock;
import com.SaintAlfonzo.creativedelight.farm.rabbittrap.RabbitTrapBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The trap as drawn (our own animation, from the behavior: farming_for_blockheads.md 2.3): the donor's box model, tipped
 * up at the front on a stick with a carrot under it while set; it rises over 10 ticks after placing, falls flat in 4
 * ticks when it snaps, and while an animal is inside it wiggles for 8 ticks every 3 seconds.
 */
public class RabbitTrapRenderer implements BlockEntityRenderer<RabbitTrapBlockEntity> {
	private static final float PROP_DEGREES = 24.0F;
	private static final int SETUP_TICKS = 10;
	private static final int FALL_TICKS = 4;
	private static final int WIGGLE_EVERY = 60;
	private static final int WIGGLE_TICKS = 8;
	private static final float WIGGLE_DEGREES = 4.0F;
	// the box's back bottom edge in model units (block model: 1.5 to 14.5, bottom at 0.25)
	private static final float HINGE_Z = 14.5F / 16.0F;
	private static final float HINGE_Y = 0.25F / 16.0F;

	private final BlockRenderDispatcher blocks;
	private final ItemRenderer items;
	private final ItemStack stick = new ItemStack(Items.STICK);
	private final ItemStack carrot = new ItemStack(Items.CARROT);

	public RabbitTrapRenderer(BlockEntityRendererProvider.Context context) {
		this.blocks = context.getBlockRenderDispatcher();
		this.items = context.getItemRenderer();
	}

	@Override
	public void render(RabbitTrapBlockEntity trap, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		if (trap.getLevel() == null) return;
		BlockState state = trap.getBlockState();
		if (!state.hasProperty(RabbitTrapBlock.FACING)) return;
		float now = trap.getLevel().getGameTime() + partialTick;
		boolean triggered = state.getValue(RabbitTrapBlock.TRIGGERED);
		float tilt;
		if (triggered) {
			float t = trap.caughtTime() == Long.MIN_VALUE ? 1.0F : (now - trap.caughtTime()) / FALL_TICKS;
			tilt = PROP_DEGREES * (1.0F - clamp01(t) * clamp01(t));
		} else {
			float t = trap.setupTime() == Long.MIN_VALUE ? 1.0F : (now - trap.setupTime()) / SETUP_TICKS;
			tilt = PROP_DEGREES * easeOut(clamp01(t));
		}
		float wiggle = 0.0F;
		if (triggered && trap.hasAnimal()) {
			float phase = (now + (trap.getBlockPos().hashCode() & 63)) % WIGGLE_EVERY;
			if (phase < WIGGLE_TICKS) wiggle = (float) Math.sin(phase / WIGGLE_TICKS * Math.PI * 4.0D) * WIGGLE_DEGREES;
		}

		Direction facing = state.getValue(RabbitTrapBlock.FACING);
		pose.pushPose();
		// face the model's north (its front texture) toward FACING
		pose.translate(0.5D, 0.0D, 0.5D);
		pose.mulPose(Vector3f.YP.rotationDegrees(180.0F - facing.toYRot() + wiggle));
		pose.translate(-0.5D, 0.0D, -0.5D);

		if (!triggered) {
			renderItem(stick, pose, buffers, light, 0.5F, 0.0F, 1.5F / 16.0F + 0.06F, 0.5F, 90.0F);
			renderItem(carrot, pose, buffers, light, 0.5F, 0.02F, 0.5F, 0.4F, 0.0F);
		}

		pose.pushPose();
		pose.translate(0.0D, HINGE_Y, HINGE_Z);
		pose.mulPose(Vector3f.XP.rotationDegrees(tilt));
		pose.translate(0.0D, -HINGE_Y, -HINGE_Z);
		BlockState north = state.setValue(RabbitTrapBlock.FACING, Direction.NORTH);
		blocks.getModelRenderer().renderModel(pose.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), north,
				blocks.getBlockModel(north), 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
		pose.popPose();
		pose.popPose();
	}

	/** An item standing (stick, {@code pitch} 90: upright prop) or lying flat (carrot) at x, y, z in block units. */
	private void renderItem(ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, float x, float y, float z,
							float scale, float pitch) {
		pose.pushPose();
		pose.translate(x, y, z);
		if (pitch == 0.0F) {
			pose.translate(0.0D, 0.02D, 0.0D);
			pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
		} else {
			pose.translate(0.0D, scale * 0.5D, 0.0D);
			pose.mulPose(Vector3f.ZP.rotationDegrees(45.0F));
		}
		pose.scale(scale, scale, scale);
		items.renderStatic(stack, ItemTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, 0);
		pose.popPose();
	}

	private static float clamp01(float v) {
		return v < 0.0F ? 0.0F : Math.min(1.0F, v);
	}

	private static float easeOut(float t) {
		return 1.0F - (1.0F - t) * (1.0F - t);
	}

	/** Client registration of the renderer. */
	@Mod.EventBusSubscriber(modid = CreativeDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static final class Registration {
		private Registration() {
		}

		@SubscribeEvent
		public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
			event.registerBlockEntityRenderer(RabbitTrap.RABBIT_TRAP_BLOCK_ENTITY.get(), RabbitTrapRenderer::new);
		}
	}
}
