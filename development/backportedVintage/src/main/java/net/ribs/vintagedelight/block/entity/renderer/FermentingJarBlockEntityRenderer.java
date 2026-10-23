package net.ribs.vintagedelight.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.ribs.vintagedelight.block.entity.FermentingJarBlockEntity;

// Upstream's renderer (items stacked inside the jar) with the 1.19.2 PoseStack / ItemRenderer API.
public class FermentingJarBlockEntityRenderer implements BlockEntityRenderer<FermentingJarBlockEntity> {
    private final Random random = new Random();
    private final Map<ItemStack, Float> itemRotations = new HashMap<>();

    public FermentingJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FermentingJarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        List<ItemStack> itemStacks = blockEntity.getRenderStacks();
        float baseHeight = 0.15F;
        for (ItemStack stack : itemStacks) {
            float rotationAngle = this.itemRotations.computeIfAbsent(stack, k -> this.random.nextFloat() * 360.0F);
            poseStack.pushPose();
            poseStack.translate(0.5F, baseHeight, 0.5F);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(270.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotationAngle));
            itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, poseStack, buffer, 1);
            poseStack.popPose();
            baseHeight += 0.06F;
        }
    }

    private static int getLightLevel(Level level, BlockPos pos) {
        return LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));
    }
}
