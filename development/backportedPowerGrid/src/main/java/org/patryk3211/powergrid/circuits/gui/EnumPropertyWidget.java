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

import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.PoseStack;
import org.patryk3211.powergrid.utility.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.components.properties.EnumProperty;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget.PROPERTIES;

public class EnumPropertyWidget<T extends Enum<?>, P extends PropertyEntry<T>> extends PropertyWidget<T, P> {
    private final Class<T> clazz;
    private final SelectionScrollInput widget;
    private final Label widgetLabel;
    private List<T> values;

    // Port fix B24 (power_grid.md 3.5): takes the change callback, so an option change marks the design as changed
    // (upstream never recorded it, and Esc then closed the editor without the unsaved-changes warning)
    public EnumPropertyWidget(Font textRenderer, int x, int y, P property, Class<T> clazz, Runnable changeMadeCallback) {
        super(textRenderer, x, y, property);
        widget = new SelectionScrollInput(x + 4, y + 1, 52, 18) {
            @Override
            protected void writeToLabel() {
                super.writeToLabel();
                if(textRenderer.width(displayLabel.text) > displayLabel.getWidth()) {
                    var string = displayLabel.text.getString();
                    while (textRenderer.width(string) > displayLabel.getWidth()) {
                        string = string.substring(0, string.length() - 1);
                    }
                    displayLabel.text = Component.literal(string);
                }
            }
        };
        widgetLabel = new Label(x + 8, y + 6, CommonComponents.EMPTY);
        widgetLabel.setWidth(46);

        var enumProp = (EnumProperty<? extends T>) property.property;
        this.values = Arrays.asList(enumProp.allValues());
        this.clazz = clazz;

        widget.writingTo(widgetLabel);

        ArrayList<Component> options = new ArrayList<>();
        int current = 0;
        for (int i = 0; i < values.size(); i++) {
            options.add(Component.literal(property.property.toString(values.get(i))));
            if(property.get() == values.get(i))
                current = i;
        }

        widget.forOptions(options);
        widget.setState(current);
        widget.calling(i -> {
            if(property.get() != values.get(i)) {
                property.setValueRaw(values.get(i));
                changeMadeCallback.run();
            }
        });
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        var ctx = new GuiGraphics(poseStack); // Port: 1.19.2 widgets draw with a PoseStack
        int x = this.x;
        int y = this.y;
        ctx.blit(PROPERTIES, x, y, 0, 57, 60, 20);
        widget.render(poseStack, mouseX, mouseY, partialTicks);
        widgetLabel.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return widget.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var clicked = widget.mouseClicked(mouseX, mouseY, button);
        setFocused(clicked);
        return clicked;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return widget.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        widget.tick();
    }

    // Port: 1.19.2's scroll input has no focus (it scrolls under the mouse) and no focus navigation (1.20 nextFocusPath)
    @Override
    public void setFocused(boolean focused) {
    }
}
