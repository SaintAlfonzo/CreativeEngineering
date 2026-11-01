package net.satisfy.foundation.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

/** Generic renderer for all storage blocks, delegates to the registered {@link StorageTypeRenderer}. */
public class StorageBlockEntityRenderer implements BlockEntityRenderer<StorageBlockEntity> {
    private static final HashMap<ResourceLocation, StorageTypeRenderer> STORAGE_TYPES = new HashMap<>();

    /** Register your renderer client side, the id must match {@link StorageBlock#type()}. */
    public static void registerStorageType(ResourceLocation name, StorageTypeRenderer renderer) {
        STORAGE_TYPES.put(name, renderer);
    }

    public static StorageTypeRenderer getRendererForId(ResourceLocation name) {
        return STORAGE_TYPES.get(name);
    }

    public StorageBlockEntityRenderer() {
    }

    @Override
    public void render(StorageBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (entity.hasLevel()) {
            BlockState state = entity.getBlockState();
            if (state.getBlock() instanceof StorageBlock sB) {
                NonNullList<ItemStack> itemStacks = entity.getInventory();
                matrices.pushPose();
                applyBlockAngle(matrices, state, 180.0F);
                StorageTypeRenderer renderer = getRendererForId(sB.type());
                if (renderer != null) {
                    renderer.render(entity, matrices, vertexConsumers, itemStacks);
                }
                matrices.popPose();
            }
        }
    }

    /** Rotates the pose stack to the block facing. */
    public static void applyBlockAngle(PoseStack matrices, BlockState state, float angleOffset) {
        float angle = state.getValue(StorageBlock.FACING).toYRot();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.mulPose(Vector3f.YP.rotationDegrees(angleOffset - angle));
    }
}
