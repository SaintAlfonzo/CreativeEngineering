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
package org.patryk3211.powergrid.circuits.editor;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.mojang.blaze3d.vertex.PoseStack;
import org.patryk3211.powergrid.utility.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.gui.CircuitEditButton;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender;
import org.patryk3211.powergrid.collections.ModIcons;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.ChangeScreenC2SPacket;
import org.patryk3211.powergrid.network.packets.SaveSchematicC2SPacket;
import org.patryk3211.powergrid.utility.Lang;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender.COLOR_TRACE_BACK;
import static org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender.COLOR_TRACE_FRONT;

public class CircuitDesignTableScreen extends AbstractSimiContainerScreen<CircuitDesignTableMenu> {
    private static final ResourceLocation BACKGROUND = PowerGrid.texture("gui/circuit_design_table");
    private static final int WIDTH = 180;
    private static final int HEIGHT = 92;
    public static final int SCALE = 4;

    private static final Component TOOLTIP_EDIT = Lang.translateDirect("gui.circuit_designer.edit");

    private IconButton confirmButton;
    private IconButton loadButton;
    private CircuitEditButton editButton;
    private IconButton openButton;

    private final CircuitSchematic schematic;

    public CircuitDesignTableScreen(CircuitDesignTableMenu container, Inventory inv, Component title) {
        super(container, inv, title);

        schematic = container.contentHolder.getSchematic();
    }

    @Override
    protected void init() {
        setWindowSize(WIDTH, HEIGHT + 4 + PLAYER_INVENTORY.height);
        setWindowOffset(11, 0);

        super.init();

        confirmButton = new IconButton(leftPos + 116 - windowXOffset, topPos + 66, AllIcons.I_CONFIRM);
        loadButton = new IconButton(leftPos + 15 - windowXOffset, topPos + 65, ModIcons.I_RIGHT);
        openButton = new IconButton(leftPos + 15 - windowXOffset, topPos + 21, AllIcons.I_OPEN_FOLDER);
        editButton = new CircuitEditButton(leftPos + 42 - windowXOffset, topPos + 18, 68, 68);

        confirmButton.withCallback(() ->
                ModdedPackets.getChannel().sendToServer(new SaveSchematicC2SPacket(menu.contentHolder, false)));
        loadButton.withCallback(() ->
                ModdedPackets.getChannel().sendToServer(new SaveSchematicC2SPacket(menu.contentHolder, true)));

        openButton.withCallback(() ->
                ModdedPackets.getChannel().sendToServer(new ChangeScreenC2SPacket(menu.contentHolder, 2)));
        editButton.withCallback(() ->
                ModdedPackets.getChannel().sendToServer(new ChangeScreenC2SPacket(menu.contentHolder, 1)));
        // Port: 1.19.2 widgets have no Tooltip; Create's AbstractSimiWidget tooltip (drawn by the screen) is used
        editButton.getToolTip().add(TOOLTIP_EDIT);

        addRenderableWidget(confirmButton);
        addRenderableWidget(loadButton);
        addRenderableWidget(editButton);
        addRenderableWidget(openButton);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        var ctx = new GuiGraphics(poseStack); // Port: 1.19.2 screens draw with a PoseStack
        int bgX = getLeftOfCentered(WIDTH);
        int invY = topPos + HEIGHT + 4;
        renderPlayerInventory(poseStack, bgX + 2, invY);

        ctx.blit(BACKGROUND, bgX, topPos, 0, 0, WIDTH, HEIGHT);

        int x = leftPos + 44 - 11;
        int y = topPos + 20;
        CircuitSchematicRender.renderLayer(schematic.front(), ctx, x, y, SCALE, COLOR_TRACE_FRONT);
        CircuitSchematicRender.renderLayer(schematic.back(), ctx, x, y, SCALE, COLOR_TRACE_BACK);
        CircuitSchematicRender.renderComponents(schematic, ctx, leftPos + 44 - 11, topPos + 20, SCALE);

        ctx.drawCenteredString(font, title, leftPos + (WIDTH - 8) / 2, topPos + 3, 0xFFFFFF);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if(!menu.player.isCreative() && !menu.contentHolder.isPowered())
            onClose();
    }
}
