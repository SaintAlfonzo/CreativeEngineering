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

import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.PoseStack;
import org.patryk3211.powergrid.utility.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;

import static org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget.PROPERTIES;

public class TextFieldPropertyWidget<T, P extends PropertyEntry<T>> extends PropertyWidget<T, P> {
    private final EditBox widget;
    private final Runnable changeMadeCallback;

    public TextFieldPropertyWidget(Font textRenderer, int x, int y, P property, Runnable changeMadeCallback) {
        super(textRenderer, x, y, property);

        this.changeMadeCallback = changeMadeCallback;

        widget = new EditBox(textRenderer, x + 8, y + 6, 46, 12, Component.empty());
        widget.setValue(property.stringValue());
        widget.setTextColor(-1);
        widget.setTextColorUneditable(-1);
        widget.setBordered(false);
        widget.setMaxLength(20);
        widget.setEditable(true);
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        var ctx = new GuiGraphics(poseStack); // Port: 1.19.2 widgets draw with a PoseStack
        int x = this.x;
        int y = this.y;

        ctx.blit(PROPERTIES, x, y, 0, 57, 60, 20);
        widget.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public void acceptInput() {
        if (!property.get().toString().equals(widget.getValue())) {
            changeMadeCallback.run();
        }
        property.setValue(widget.getValue());
        widget.setFocus(false); // Port: 1.19.2 EditBox.setFocus (setFocused is protected)
        widget.setValue(property.stringValue());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return widget.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Port: 1.19.2's EditBox.mouseClicked drops its own focus on a click outside (1.20 leaves focus to the screen),
        // so the focus is read before the click, or a value typed and then clicked away from was never applied
        var wasFocused = widget.isFocused();
        var clicked = widget.mouseClicked(mouseX, mouseY, button);
        if(wasFocused && !clicked) {
            acceptInput();
        }
        setFocused(clicked);
        return clicked;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            acceptInput();
            return true;
        }
        return widget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        widget.tick();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return widget.charTyped(chr, modifiers);
    }

    @Override
    public void setFocused(boolean newFocused) {
        if (widget.isFocused() && !newFocused) {
            acceptInput();
        }
        widget.setFocus(newFocused);
    }
    // Port: 1.20 nextFocusPath (focus navigation) dropped; 1.19.2 has none
}
