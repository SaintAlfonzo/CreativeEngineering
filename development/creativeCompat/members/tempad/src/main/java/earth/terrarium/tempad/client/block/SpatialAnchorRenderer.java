// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/block/SpatialAnchorRenderer.kt.
// Translated to Java for Forge 1.19.2 (same Forge API: BakedModel.getRenderTypes, RenderTypeHelper.getEntityRenderType,
// ModelBlockRenderer.renderModel with ModelData and RenderType). Draws the timedoor marker and chronomark blocks (render
// shape INVISIBLE) tinted with the block entity's colour.
package earth.terrarium.tempad.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.common.block.timedoor_marker.AbstractMarkerBe;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;

public class SpatialAnchorRenderer<T extends AbstractMarkerBe> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

    public SpatialAnchorRenderer(BlockRenderDispatcher blockRenderer) {
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        BakedModel bakedmodel = blockRenderer.getBlockModel(state);
        Color color = blockEntity.getColor();
        float red = color.getIntRed() / 255f;
        float green = color.getIntGreen() / 255f;
        float blue = color.getIntBlue() / 255f;

        for (RenderType rt : bakedmodel.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY)) {
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(),
                    bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)),
                    state,
                    bakedmodel,
                    red,
                    green,
                    blue,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    rt
            );
        }
    }
}
