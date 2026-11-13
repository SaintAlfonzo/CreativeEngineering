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
package org.patryk3211.powergrid.ponder.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.element.AnimatedSceneElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;

import java.util.function.Function;

// Create 6 AnimatedSceneElementBase.renderLast(level, buffer, GuiGraphics, fade, pt) -> Create 0.5.1
// AnimatedSceneElement.renderLast(world, buffer, PoseStack, fade, pt).
public class WireElement extends AnimatedSceneElement {
    protected BaseWireEntity wire;
    protected Function<Level, BaseWireEntity> wireFactory;

    public WireElement(Function<Level, BaseWireEntity> factory) {
        this.wireFactory = factory;
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        if(wire != null) {
            wire.discard();
            wire = null;
        }
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if(wire != null) {
            wire.tick();
        }
    }

    @Override
    protected void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if(wire == null && isVisible()) {
            wire = wireFactory.apply(world);
        }
        if(wire == null)
            return;
        ms.pushPose();
        ms.translate(
                Mth.lerp(pt, wire.xo, wire.getX()),
                Mth.lerp(pt, wire.yo, wire.getY()),
                Mth.lerp(pt, wire.zo, wire.getZ())
        );
        dispatcher.render(wire, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
        ms.popPose();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(!visible) {
            if(wire != null) {
                wire.discard();
                wire = null;
            }
        }
    }
}
