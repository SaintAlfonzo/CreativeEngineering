package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.MultiblockPart;
import com.SaintAlfonzo.creativedelight.hearth.machine.StompingBasinBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.client.model.data.ModelData;

/** Basin contents; while in a 2x2 group each quadrant draws its own part of the big basin (block models off). */
public class StompingBasinRenderer implements BlockEntityRenderer<StompingBasinBlockEntity> {
    public static final ResourceLocation MODEL_NW = new ResourceLocation("hearthandharvest", "block/big_stomping_basin_nw");
    public static final ResourceLocation MODEL_NE = new ResourceLocation("hearthandharvest", "block/big_stomping_basin_ne");
    public static final ResourceLocation MODEL_SW = new ResourceLocation("hearthandharvest", "block/big_stomping_basin_sw");
    public static final ResourceLocation MODEL_SE = new ResourceLocation("hearthandharvest", "block/big_stomping_basin_se");
    private static final float FLOOR_Y = 0.0645F;
    private static final float FLUID_MIN_Y = 0.0725F;
    private static final float FLUID_RANGE = 0.615F;

    public StompingBasinRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StompingBasinBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        MultiblockPart role = be.role();
        if (role == MultiblockPart.MEMBER) {
            drawModel(be, quadrant(be), pose, buffer, light, overlay);
            return;
        }
        boolean combined = role == MultiblockPart.CONTROLLER;
        if (combined) drawModel(be, MODEL_NW, pose, buffer, light, overlay);
        items(be, pose, buffer, light, overlay, combined);
        fluid(be, pose, buffer, light, combined);
    }

    private static ResourceLocation quadrant(StompingBasinBlockEntity be) {
        BlockPos controller = be.controllerPos();
        if (controller == null) return MODEL_NW;
        int dx = be.getBlockPos().getX() - controller.getX();
        int dz = be.getBlockPos().getZ() - controller.getZ();
        if (dx == 1 && dz == 0) return MODEL_NE;
        if (dx == 0 && dz == 1) return MODEL_SW;
        return dx == 1 && dz == 1 ? MODEL_SE : MODEL_NW;
    }

    private static void drawModel(StompingBasinBlockEntity be, ResourceLocation id, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(id);
        Level level = be.getLevel();
        RandomSource random = RandomSource.create();
        var vc = buffer.getBuffer(RenderType.solid());
        if (level != null) {
            BlockPos pos = be.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, model, state, pos, pose, vc, false, random, 42L, overlay);
        } else {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, null, random, ModelData.EMPTY, null)) {
                vc.putBulkData(pose.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, light, overlay, false);
            }
        }
    }

    private static int light(StompingBasinBlockEntity controller, int dx, int dz) {
        Level level = controller.getLevel();
        return level == null ? 0 : LevelRenderer.getLightColor(level, controller.getBlockPos().offset(dx, 0, dz));
    }

    private static void items(StompingBasinBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay, boolean combined) {
        float min = combined ? 0.3125F : 0.4375F;
        float size = combined ? 1.375F : 0.125F;
        long seed = be.getBlockPos().asLong();
        MultiBufferSource solid = FluidQuads.solidOnly(buffer);
        int nw = combined ? light(be, 0, 0) : light;
        int ne = combined ? light(be, 1, 0) : light;
        int sw = combined ? light(be, 0, 1) : light;
        int se = combined ? light(be, 1, 1) : light;
        int index = 0;
        for (int slot = 0; slot < be.items().getSlots(); slot++) {
            ItemStack stack = be.items().getStackInSlot(slot);
            int rendered = stack.isEmpty() ? 0 : Math.min(stack.getCount(), 64);
            for (int i = 0; i < rendered; i++, index++) {
                float[] s = FluidQuads.scatter(seed, index);
                float x = min + s[0] * size;
                float z = min + s[1] * size;
                pose.pushPose();
                pose.translate(x, FLOOR_Y + index * 0.001F, z);
                pose.mulPose(Vector3f.YP.rotationDegrees(s[2] * 360.0F));
                pose.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                int itemLight = z >= 1.0F ? (x >= 1.0F ? se : sw) : (x >= 1.0F ? ne : nw);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, itemLight, overlay, pose, solid, index);
                pose.popPose();
            }
        }
    }

    private static void fluid(StompingBasinBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, boolean combined) {
        FluidStack fluid = be.tank().getFluid();
        if (fluid.isEmpty()) return;
        float fill = (float) be.tank().getFluidAmount() / be.tank().getCapacity();
        if (fill <= 0.0F) return;
        float y = FLUID_MIN_Y + fill * FLUID_RANGE;
        Matrix4f m = pose.last().pose();
        if (combined) {
            float mid = 1.0F;
            FluidQuads.surface(fluid, m, buffer, 0.125F, mid, 0.125F, mid, y, light(be, 0, 0), 0.75F);
            FluidQuads.surface(fluid, m, buffer, mid, 1.875F, 0.125F, mid, y, light(be, 1, 0), 0.75F);
            FluidQuads.surface(fluid, m, buffer, 0.125F, mid, mid, 1.875F, y, light(be, 0, 1), 0.75F);
            FluidQuads.surface(fluid, m, buffer, mid, 1.875F, mid, 1.875F, y, light(be, 1, 1), 0.75F);
        } else {
            FluidQuads.surface(fluid, m, buffer, 0.125F, 0.875F, 0.125F, 0.875F, y, light, 0.75F);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(StompingBasinBlockEntity be) {
        return be.role() != MultiblockPart.NONE;
    }
}
