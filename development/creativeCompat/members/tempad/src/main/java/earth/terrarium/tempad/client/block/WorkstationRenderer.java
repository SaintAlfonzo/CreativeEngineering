// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/block/WorkstationRenderer.kt.
// Translated to Java for Forge 1.19.2 (Axis -> Vector3f axes, ItemDisplayContext.FIXED -> TransformType.FIXED).
package earth.terrarium.tempad.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import earth.terrarium.tempad.common.block.WorkstationBE;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Draws the docked stack flat on the workstation pad. */
public class WorkstationRenderer implements BlockEntityRenderer<WorkstationBE> {
    private final ItemRenderer itemRenderer;

    public WorkstationRenderer(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    private static float px(float value) {
        return value / 16f;
    }

    @Override
    public void render(WorkstationBE block, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack item = block.inventory.getStackInSlot(0);
        if (item.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90f));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 90f));
        poseStack.translate(px(-1.5f), px(-1f), px(-2.5f));
        poseStack.mulPose(Vector3f.ZN.rotationDegrees(90f));
        poseStack.scale(px(12f), px(12f), px(12f));
        itemRenderer.renderStatic(item, ItemTransforms.TransformType.FIXED, packedLight, packedOverlay, poseStack, bufferSource, 0);
        poseStack.popPose();
    }
}
