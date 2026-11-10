package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.CrateBlock;
import com.SaintAlfonzo.creativedelight.hearth.machine.CrateBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Bottle Crate contents: each item's 3D display model ({@code <namespace>:display/<path>}) stands on the 3x3 grid; a
 * pack bottle without such a model lies flat as its own icon (D10: flat models for pack items).
 */
public class CrateRenderer implements BlockEntityRenderer<CrateBlockEntity> {
    private final ItemRenderer itemRenderer;

    public CrateRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CrateBlockEntity crate, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = crate.getBlockState();
        SlabType type = state.getValue(CrateBlock.TYPE);
        Direction facing = state.getValue(CrateBlock.FACING);
        float yRot = switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Vector3f.YP.rotationDegrees(yRot));
        pose.translate(-0.5, 0.0, -0.5);
        if (type == SlabType.BOTTOM || type == SlabType.DOUBLE) half(crate, 0, 0.5, pose, buffer, light, overlay);
        if (type == SlabType.DOUBLE) half(crate, CrateBlockEntity.PER_HALF, 1.0, pose, buffer, light, overlay);
        if (type == SlabType.TOP) half(crate, 0, 1.0, pose, buffer, light, overlay);
        pose.popPose();
    }

    private void half(CrateBlockEntity crate, int offset, double surfaceY, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) {
            ItemStack stack = crate.getItem(offset + i);
            if (stack.isEmpty()) continue;
            double x = 0.0625 + i % 3 * 0.3125 + 0.125;
            double z = 0.0625 + i / 3 * 0.3125 + 0.125;
            pose.pushPose();
            BakedModel model = displayModel(stack);
            if (model != null) {
                pose.translate(x, surfaceY + 0.0625, z);
                itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, false, pose, buffer, light, overlay, model);
            } else {
                pose.translate(x, surfaceY - 0.235, z);
                pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                pose.scale(0.34F, 0.34F, 0.34F);
                itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, light, overlay, pose, buffer, i);
            }
            pose.popPose();
        }
    }

    @Nullable
    private static BakedModel displayModel(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return null;
        ModelManager models = Minecraft.getInstance().getModelManager();
        BakedModel model = models.getModel(new ResourceLocation(id.getNamespace(), "display/" + id.getPath()));
        return model == models.getMissingModel() ? null : model;
    }
}
