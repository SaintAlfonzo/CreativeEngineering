package com.SaintAlfonzo.creativedelight.barbeques.client;

import com.SaintAlfonzo.creativedelight.barbeques.block.StorageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.Random;

/** Basin and tray contents as a small leaning pile, more models for bigger stacks (1.3.0 StorageTileRenderer). */
public class StorageRenderer<T extends StorageBlockEntity> implements BlockEntityRenderer<T> {
    private static final int[] COUNT = {0, 1, 2, 4, 6, 8, 12, 16, 24, 32, 40, 56, 64};
    private static final int[] REV = new int[64];
    private final Random random = new Random(42L);

    static {
        int k = 0;
        for (int i = 0; i < 64; i++) {
            if (i > COUNT[k]) {
                k++;
            }
            REV[i] = k;
        }
    }

    public StorageRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        if (entity.getLevel() == null) {
            return;
        }
        int lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());
        Direction direction = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        for (int i = 0; i < entity.size(); i++) {
            ItemStack stack = entity.getStack(i);
            int seed = stack.isEmpty() ? 187 : stack.getItem().hashCode() + stack.getCount() + i * 64;
            random.setSeed(seed);
            float itemOffset = entity.getOffset(i);
            int models = stack.getCount() < REV.length ? REV[stack.getCount()] : 12;
            for (int j = 0; j < models; j++) {
                float r = (random.nextFloat() * 2.0F - 1.0F) * 0.03F;
                pose.pushPose();
                pose.translate(0.5, 0.2, 0.5);
                pose.mulPose(Vector3f.YP.rotationDegrees(-direction.toYRot()));
                pose.translate(itemOffset + r, 0.0, 0.35 - j * 0.05);
                pose.mulPose(Vector3f.XP.rotationDegrees(20 + j * 4));
                pose.scale(0.375F, 0.375F, 0.375F);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, lightAbove, overlay,
                        pose, buffer, i);
                pose.popPose();
            }
        }
    }
}
