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
package org.patryk3211.powergrid.kinetics.generator.inductionrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.kinetics.generator.rotor.RotorBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer;

public class CommutatorRenderer extends RotorRenderer {
    public CommutatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RotorBlockEntity rotor, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(rotor, partialTicks, matrixStack, buffer, light, overlay);

        var rotorAngle = getRotorAngle(rotor, partialTicks);
        var brushAngle = rotorAngle * 2;

        var sin = Math.sin(brushAngle);
        var brushOffset = sin * sin * 1 / 16f;

        var state = rotor.getBlockState();
        if(state.getBlock() instanceof VerticalCommutatorBlock) {
            var facing = state.getValue(VerticalCommutatorBlock.HORIZONTAL_FACING);
            if(!state.getValue(VerticalCommutatorBlock.UP))
                facing = facing.getOpposite();

            var brush = CachedBufferer.partial(ModdedPartialModels.VERTICAL_COMMUTATOR_BRUSH, state);
            brush.light(light)
                    .centre()
                    .rotateToFace(facing)
                    .unCentre()
                    .translate(-brushOffset, 0, 0)
                    .renderInto(matrixStack, buffer.getBuffer(RenderType.solid()));
            brush.light(light)
                    .centre()
                    .rotateToFace(facing)
                    .rotateZRadians(Math.PI)
                    .unCentre()
                    .translate(-brushOffset, 0, 0)
                    .renderInto(matrixStack, buffer.getBuffer(RenderType.solid()));
        } else {
            var axis = state.getValue(CommutatorBlock.HORIZONTAL_FACING).getAxis();
            var facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);

            var brush = CachedBufferer.partial(ModdedPartialModels.COMMUTATOR_BRUSH, state);
            brush.light(light)
                    .centre()
                    .rotateToFace(facing)
                    .unCentre()
                    .translate(-brushOffset, 0, 0)
                    .renderInto(matrixStack, buffer.getBuffer(RenderType.solid()));
            brush.light(light)
                    .centre()
                    .rotateToFace(facing.getOpposite())
                    .unCentre()
                    .translate(-brushOffset, 0, 0)
                    .renderInto(matrixStack, buffer.getBuffer(RenderType.solid()));
        }
    }

    @Override
    protected SuperByteBuffer getModelForState(BlockState state) {
        if(state.getBlock() instanceof VerticalCommutatorBlock) {
            return CachedBufferer.partialFacing(ModdedPartialModels.COMMUTATOR_SHAFT, state, Direction.UP);
        } else {
            return CachedBufferer.partialFacing(ModdedPartialModels.COMMUTATOR_SHAFT, state,
                    Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(CommutatorBlock.HORIZONTAL_FACING).getAxis()));
        }
    }
}
