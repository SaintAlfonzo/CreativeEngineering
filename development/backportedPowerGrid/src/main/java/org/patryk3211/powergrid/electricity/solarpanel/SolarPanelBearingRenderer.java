// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.electricity.solarpanel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class SolarPanelBearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity> extends KineticBlockEntityRenderer<T> {
    public SolarPanelBearingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final Direction facing = be.getBlockState()
                .getValue(SolarPanelBearingBlock.FACING);
        PartialModel top = ModdedPartialModels.SOLAR_PANEL_BEARING_ROTOR;
        SuperByteBuffer superBuffer = CachedBufferer.partial(top, be.getBlockState());

        float interpolatedAngle = be.getInterpolatedAngle(partialTicks - 1);
        kineticRotationTransform(superBuffer, be, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

        if (facing.getAxis()
                .isHorizontal())
            superBuffer.rotateCentered(Direction.UP,
                    AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
        if (be.getBlockState().getValue(SolarPanelBearingBlock.FACING).getAxis().isVertical()){
            if (facing == Direction.UP){
                superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad( -90 + AngleHelper.verticalAngle(facing)));
            } else {
                superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad( 90 + AngleHelper.verticalAngle(facing)));
            }
        } else {
            superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(AngleHelper.verticalAngle(facing)));
        }
        superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(AngleHelper.verticalAngle(facing)));
        superBuffer.renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticBlockEntity be, BlockState state) {
        return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(SolarPanelBearingBlock.FACING)
                .getOpposite());
    }
}
