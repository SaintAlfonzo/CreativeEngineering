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
package org.patryk3211.powergrid.kinetics.punchcard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import org.patryk3211.powergrid.utility.GuiGraphics;

public class PunchCardButton extends AbstractSimiWidget {
    private final byte[] dataRef;
    private final int row, column;
    private final boolean locked;

    public PunchCardButton(int x, int y, int r, int c, byte[] dataRef, boolean locked) {
        super(x, y, 10, 7);
        this.row = r;
        this.column = c;
        this.dataRef = dataRef;
        this.locked = locked;
    }

    public boolean getState() {
        return (dataRef[column] & (1 << row)) != 0;
    }

    public void flipState() {
        dataRef[column] ^= (byte) (1 << row);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Port: 1.19.2 widgets draw with a PoseStack (renderButton; catnip's doRender)
        var graphics = new GuiGraphics(poseStack);
        if(visible) {
            isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            if(locked)
                isHovered = false;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if(!getState()) {
                if (isHovered) {
                    graphics.blit(PunchCardScreen.BACKGROUND, x, y, 240, 20, 10, 7);
                } else {
                    graphics.blit(PunchCardScreen.BACKGROUND, x, y, 228, 20, 10, 7);
                }
            } else {
                if (isHovered) {
                    graphics.blit(PunchCardScreen.BACKGROUND, x, y, 240, 29, 10, 7);
                } else {
                    graphics.blit(PunchCardScreen.BACKGROUND, x, y, 228, 29, 10, 7);
                }
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(locked)
            return;
        flipState();
        if(getState())
            runCallback(mouseX, mouseY);
    }
}
