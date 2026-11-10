package com.SaintAlfonzo.creativedelight.hearth.nest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client side of the nest: its contents lie in the four corners. Reached through DistExecutor. */
public final class NestClient {
    private static final float CORNER = 0.12F;
    private static final float[][] CORNERS = {{CORNER, CORNER}, {CORNER, -CORNER}, {-CORNER, -CORNER}, {-CORNER, CORNER}};

    private NestClient() {
    }

    public static void init(IEventBus bus) {
        bus.addListener(NestClient::renderers);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(HHNestRegistry.NEST_ENTITY.get(), context -> new Renderer());
    }

    private static final class Renderer implements BlockEntityRenderer<NestBlockEntity> {
        @Override
        public void render(NestBlockEntity nest, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
            for (int slot = 0; slot < NestBlockEntity.SLOTS; slot++) {
                ItemStack stack = nest.getInventory().getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                float[] corner = CORNERS[slot];
                pose.pushPose();
                pose.translate(0.5F + corner[0], 0.0625F, 0.5F + corner[1]);
                pose.mulPose(Vector3f.YP.rotationDegrees(15.0F + 90.0F * slot));
                pose.mulPose(Vector3f.XP.rotationDegrees(75.0F));
                pose.scale(0.5F, 0.5F, 0.5F);
                pose.translate(-0.2F, 0.15F, -0.2F);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, light, overlay, pose, buffer, slot * 1013);
                pose.popPose();
            }
        }
    }
}
