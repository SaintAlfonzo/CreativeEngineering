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
package org.patryk3211.powergrid.kinetics.generator.clutch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.patryk3211.powergrid.collections.ModdedPartialModels;

import static org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer.getRotorAngle;

public class GeneratorClutchRenderer extends KineticBlockEntityRenderer<GeneratorClutchBlockEntity> {
    public GeneratorClutchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GeneratorClutchBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Port: upstream returns here when Flywheel 1.0 draws GeneratorClutchVisual. The visual is not ported (Flywheel
        // 0.6 here), so this renderer always draws; the shaft bit is drawn directly (the 0.5.1 super renderSafe skips it
        // under Flywheel instancing, expecting an instance this block does not have).
        var state = be.getBlockState();
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(getRenderType(be, state)), light);

        var facing = state.getValue(BlockStateProperties.FACING);
        var axis = facing.getAxis();

        var rotorModel = CachedBufferer.partialFacing(ModdedPartialModels.CLUTCH_SHAFT, state, facing.getOpposite());
        var rotorAngle = getRotorAngle(be, partialTicks);

        rotorModel.light(light);
        rotorModel.rotateCentered(Direction.get(Direction.AxisDirection.POSITIVE, axis), rotorAngle);
        rotorModel.renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(GeneratorClutchBlockEntity be, BlockState state) {
        var facing = state.getValue(GeneratorClutchBlock.FACING);
        return CachedBufferer.partialFacing(ModdedPartialModels.SHAFT_BIT, state, facing);
    }
}
