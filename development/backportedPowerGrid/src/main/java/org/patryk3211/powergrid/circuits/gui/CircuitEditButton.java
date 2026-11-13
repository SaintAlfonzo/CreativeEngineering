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
package org.patryk3211.powergrid.circuits.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import org.patryk3211.powergrid.utility.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class CircuitEditButton extends AbstractSimiWidget {
    public CircuitEditButton(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Port: 1.19.2 widgets draw with a PoseStack (renderButton; catnip's doRender)
        var ctx = new GuiGraphics(poseStack);
        if(visible) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if(isMouseOver(mouseX, mouseY)) {
                ctx.hLine(x - 1, x + width, y - 1, 0xFFFFFFFF);
                ctx.vLine(x - 1, y - 1, y + height + 1, 0xFFFFFFFF);
                ctx.hLine(x, x + width, y + height, 0xFF888888);
                ctx.vLine(x + width, y - 1, y + height + 1, 0xFF888888);
//                ctx.drawBorder(x, y, width + 2, height + 2, 0xFF888888);
            }
        }
    }
}
