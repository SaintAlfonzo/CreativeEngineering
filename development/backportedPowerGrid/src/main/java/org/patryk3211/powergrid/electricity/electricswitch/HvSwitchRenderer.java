/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.electricity.electricswitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

public class HvSwitchRenderer extends KineticBlockEntityRenderer<HvSwitchBlockEntity> {
    public HvSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HvSwitchBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Port: upstream skips this renderer when Flywheel 1.0 draws HvSwitchVisual. The visual is not ported (Flywheel
        // 0.6 here), so the renderer always draws the shaft and the rod. The shaft is drawn directly: the 0.5.1 super
        // renderSafe returns early under Flywheel instancing, expecting an instance this block does not have.
        var state = be.getBlockState();
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(getRenderType(be, state)), light);

        var facing = state.getValue(HvSwitchBlock.HORIZONTAL_FACING);
        var rod = CachedBufferer.partialFacing(ModdedPartialModels.HV_SWITCH_ROD, state, facing);
        float angle = (1.0f - be.rod.getValue(partialTicks)) * (float) Math.PI * 0.5f;
        rod
                .rotateCentered(facing.getClockWise(), angle)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(HvSwitchBlockEntity be, BlockState state) {
        return CachedBufferer.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, state, state.getValue(HvSwitchBlock.HORIZONTAL_FACING).getClockWise());
    }
}
