package net.satisfy.foundation.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.satisfy.foundation.client.render.WobbleAnimation;
import net.satisfy.foundation.render.ClientUtil;

public class BottleGroupRenderer implements StorageTypeRenderer {
    private static final String FAKE_MODEL = "fake_model";

    @Override
    public void render(StorageBlockEntity entity, PoseStack poseStack, MultiBufferSource buffers, NonNullList<ItemStack> items) {
        poseStack.translate(-0.5, 0, -0.5);
        switch (count(items)) {
            case 1 -> renderOne(entity, poseStack, buffers, items);
            case 2 -> renderTwo(entity, poseStack, buffers, items);
            case 3 -> renderThree(entity, poseStack, buffers, items);
            default -> {
            }
        }
    }

    protected boolean isLying(ItemStack stack) {
        return false;
    }

    protected BlockState stateFor(BlockItem item) {
        BlockState state = item.getBlock().defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty fake && property.getName().equals(FAKE_MODEL)) {
                return state.setValue(fake, false);
            }
        }
        return state;
    }

    protected void renderBottle(ItemStack stack, int index, StorageBlockEntity entity, PoseStack poseStack, MultiBufferSource buffers) {
        if (!(stack.getItem() instanceof BlockItem item)) {
            return;
        }
        poseStack.pushPose();
        WobbleAnimation.apply(poseStack, entity.getLevel(), entity.getWobbleStart(), WobbleAnimation.seed(entity.getBlockPos().asLong() + index));
        ClientUtil.renderBlock(stateFor(item), poseStack, buffers, entity);
        poseStack.popPose();
    }

    private static int count(NonNullList<ItemStack> items) {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void renderOne(StorageBlockEntity entity, PoseStack poseStack, MultiBufferSource buffers, NonNullList<ItemStack> items) {
        renderBottle(items.get(0), 0, entity, poseStack, buffers);
    }

    private void renderTwo(StorageBlockEntity entity, PoseStack poseStack, MultiBufferSource buffers, NonNullList<ItemStack> items) {
        poseStack.translate(-0.15F, 0F, -0.25F);
        renderBottle(items.get(0), 0, entity, poseStack, buffers);
        poseStack.translate(0.1F, 0F, 0.8F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(30));
        renderBottle(items.get(1), 1, entity, poseStack, buffers);
    }

    private void renderThree(StorageBlockEntity entity, PoseStack poseStack, MultiBufferSource buffers, NonNullList<ItemStack> items) {
        poseStack.translate(-0.25F, 0F, -0.25F);
        renderBottle(items.get(0), 0, entity, poseStack, buffers);
        poseStack.translate(0.15F, 0F, 0.5F);
        renderBottle(items.get(1), 1, entity, poseStack, buffers);
        ItemStack third = items.get(2);
        if (isLying(third)) {
            poseStack.translate(0.35F, 0.7F, -0.13F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
        } else {
            poseStack.translate(0.1F, 0F, 0F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(30));
        }
        renderBottle(third, 2, entity, poseStack, buffers);
    }
}
