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
package org.patryk3211.powergrid.kinetics.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedPartialModels;
import org.patryk3211.powergrid.kinetics.variac.VariacBlock;

public class TunedBlockRenderer<T extends TunedBlockEntity> extends KineticBlockEntityRenderer<T> {
    public TunedBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource provider, int light, int overlay) {
        // Port: the shaft is drawn directly (the 0.5.1 super renderSafe skips it under Flywheel instancing; upstream's
        // TunedBlockVisual is Flywheel 1.0 and not ported).
        var state = be.getBlockState();
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, provider.getBuffer(getRenderType(be, state)), light);

        var buffer = CachedBufferer.partialFacing(ModdedPartialModels.VARIAC_ARMATURE, state, state.getValue(VariacBlock.HORIZONTAL_FACING).getOpposite());

        float angle = be.arm.getValue(partialTicks) * (float) Math.PI * 1.75f;
        buffer
                .rotateCentered(Direction.UP, angle)
                .light(light)
                .renderInto(ms, provider.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
        if(state.getValue(TunedBlock.BASE)) {
            return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.UP);
        } else {
            // Port: Create 0.5.1 has no AllPartialModels.SHAFT; the vertical shaft block model is the same model.
            return CachedBufferer.block(KINETIC_BLOCK, shaft(Direction.Axis.Y));
        }
    }
}
