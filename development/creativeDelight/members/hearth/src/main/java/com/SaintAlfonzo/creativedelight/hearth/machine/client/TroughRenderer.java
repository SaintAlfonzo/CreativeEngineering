package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.TroughBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Water Trough: the water surface, or the food stack scattered on the floor. */
public class TroughRenderer implements BlockEntityRenderer<TroughBlockEntity> {
    private static final float INNER_MIN = 0.125F;
    private static final float INNER_MAX = 0.875F;
    private static final float FLOOR_Y = 0.1895F;
    private static final float SCATTER_MIN = 0.4375F;
    private static final float SCATTER_SIZE = 0.125F;
    private static final float MAX_PILE = 0.375F;

    public TroughRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TroughBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        FluidStack fluid = be.tank().getFluid();
        ItemStack food = be.food();
        if (!fluid.isEmpty()) {
            float fill = (float) be.tank().getFluidAmount() / be.tank().getCapacity();
            if (fill > 0.0F) FluidQuads.surface(fluid, pose.last().pose(), buffer, INNER_MIN, INNER_MAX, INNER_MIN, INNER_MAX, FLOOR_Y + fill * 0.273F, light, 0.75F);
        } else if (!food.isEmpty()) {
            MultiBufferSource solid = FluidQuads.solidOnly(buffer);
            long seed = be.getBlockPos().asLong();
            int count = Math.min(food.getCount(), 64);
            float fillFrac = Math.min(count / 64.0F, 1.0F);
            for (int i = 0; i < count; i++) {
                float[] s = FluidQuads.scatter(seed, i);
                pose.pushPose();
                pose.translate(SCATTER_MIN + s[0] * SCATTER_SIZE, FLOOR_Y + s[3] * fillFrac * MAX_PILE, SCATTER_MIN + s[1] * SCATTER_SIZE);
                pose.mulPose(Vector3f.YP.rotationDegrees(s[2] * 360.0F));
                pose.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                Minecraft.getInstance().getItemRenderer().renderStatic(food, ItemTransforms.TransformType.GROUND, light, overlay, pose, solid, i);
                pose.popPose();
            }
        }
    }
}
