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

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import org.patryk3211.powergrid.utility.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.components.properties.BooleanProperty;
import org.patryk3211.powergrid.circuits.components.properties.EnumProperty;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.circuits.components.properties.IntProperty;
import org.patryk3211.powergrid.circuits.components.properties.StringProperty;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

import java.util.ArrayList;
import java.util.List;

public class ComponentPropertiesWidget extends AbstractSimiWidget {
    public static final ResourceLocation PROPERTIES = PowerGrid.texture("gui/circuit_design_table_properties");

    private final Font textRenderer;
    private final int right;
    @Nullable
    private PlacedComponent component;
    private final List<PropertyWidget<?, ?>> propertyWidgets = new ArrayList<>();

    private int propertyCount = 0;

    public ComponentPropertiesWidget(Font textRenderer, int right, int y) {
        super(right - 150, y, 150, 150);
        this.right = right;
        this.textRenderer = textRenderer;
    }

    /**
     * Populate this widget with settings for the provided component.
     * @param component the component to create settings widgets for
     */
    public void setComponent(@Nullable PlacedComponent component) {
        setComponent(component, () -> {});
    }

    /**
     * Populate this widget with settings for the provided component and set a callback to trigger when changes are made.
     * @param component the component to create settings widgets for
     * @param changeMadeCallback callback that will be called when the component's properties are modified
     */
    public void setComponent(@Nullable PlacedComponent component, Runnable changeMadeCallback) {
        this.component = component;

        // Accept inputs on all property widgets
        for(var widget : propertyWidgets) {
            widget.setFocused(false);
        }

        propertyWidgets.clear();
        if(component != null) {
            var properties = component.component.getProperties();
            var title = titleOf(component);
            int titleWidth = textRenderer.width(title);
            int maxTextLength = 0;
            for(var property : properties) {
                if(property.isHidden())
                    continue;
                var propertyKey = property.translationKey();
                var length = textRenderer.width(Component.translatable(propertyKey));
                if (length > maxTextLength)
                    maxTextLength = length;
            }

            int requiredWidth = Math.max(maxTextLength + 6 + 60 + 5, titleWidth + 20);
            int width = Math.max(requiredWidth, 120);
            setWidth(width);
            this.x = right - width;

            int x = right - 60, y = this.y + 16;
            for(var property : properties) {
                if(property.isHidden())
                    continue;
                if(property instanceof FloatProperty || property instanceof IntProperty || property instanceof StringProperty) {
                    propertyWidgets.add(new TextFieldPropertyWidget<>(textRenderer, x, y, component.getEntry(property), changeMadeCallback));
                } else if(property instanceof BooleanProperty bProp) {
                    propertyWidgets.add(new BooleanPropertyWidget(textRenderer, x, y, component.getEntry(bProp), changeMadeCallback));
                } else if(property instanceof EnumProperty) {
                    var clazz = property.defaultValue().getClass();
                    propertyWidgets.add(new EnumPropertyWidget(textRenderer, x, y, component.getEntry(property), clazz, changeMadeCallback));
                } else {
                    propertyWidgets.add(new ConstantPropertyWidget<>(textRenderer, x, y, component.getEntry(property)));
                }
                y += 20;
            }
            propertyCount = propertyWidgets.size();
        } else {
            setWidth(120);
            this.x = right - 120;
            propertyCount = 0;
        }
    }

    // Port: component_items is a synced table on 1.19.2; a component with no item entry shows its id instead of a crash
    private static Component titleOf(PlacedComponent placed) {
        var item = ComponentRegistry.getItem(placed.component);
        if(item != null)
            return Component.translatable(item.getDescriptionId());
        var id = ComponentRegistry.getId(placed.component);
        return Component.literal(id == null ? "?" : id.toString());
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Port: 1.19.2 widgets draw with a PoseStack (renderButton inside AbstractSimiWidget's push / pop; catnip's doRender)
        var ctx = new GuiGraphics(poseStack);
        if(component == null)
            return;

        var ms = ctx.pose();
        ms.translate(x, y, 0);

        // Three sliced texture
        int centerSliceSize = Math.max(width - 120, 0);
        ctx.blit(PROPERTIES, 0, 0, 0, 0, 60, 16);
        if(width > 120) {
            ctx.blitRepeating(PROPERTIES, 60, 0, centerSliceSize, 16, 61, 0, 30, 16);
        }
        ctx.blit(PROPERTIES, 60 + centerSliceSize, 0, 92, 0, 60, 16);

        ctx.drawCenteredString(textRenderer, titleOf(component), getWidth() / 2, 5, 0xFFFFFFFF);

        // Draw common background for each property
        ctx.blitRepeating(PROPERTIES, 0, 16, 60, propertyCount * 20, 0, 29, 60, 20);
        if(width > 120) {
            ctx.blitRepeating(PROPERTIES, 60, 16, centerSliceSize, propertyCount * 20, 61, 29, 30, 20);
        }

        ms.pushPose();
        ms.translate(-x, -y, 0);
        int yOffset = 16;
        for(var widget : propertyWidgets) {
            yOffset = widget.y + widget.getHeight() - y;
            var propertyKey = widget.property.property.translationKey();
            int end = ctx.drawString(textRenderer, Component.translatable(propertyKey), x + 6, widget.y + 6, 0xFF606060, false);

            widget.render(poseStack, mouseX, mouseY, partialTicks);

            // Port: 1.19.2 has no Component.translatableWithFallback; an absent key gives no summary, as upstream's ""
            if(I18n.exists(propertyKey + ".summary")) {
                var summary = Component.translatable(propertyKey + ".summary");
                // Hover description.
                int x1 = x + 6;
                int y1 = widget.y + 6;
                var lines = textRenderer.split(summary, getWidth());
                if(mouseX >= x1 && mouseY >= y1 && mouseX < end && mouseY < y1 + 16)
                    ctx.renderTooltip(textRenderer, lines, mouseX, mouseY);
            }
        }
        ms.popPose();

        ms.translate(0, yOffset, 0);
        ctx.blit(PROPERTIES, 0, 0, 0, 50, 60, 6);
        if(width > 120) {
            ctx.blitRepeating(PROPERTIES, 60, 0, centerSliceSize, 6, 61, 50, 30, 6);
        }
        ctx.blit(PROPERTIES, 60 + centerSliceSize, 0, 92, 50, 60, 6);

        var stack = component.footprint().getRenderedStack(component.component);
        if(stack != null) {
            ctx.blit(PROPERTIES, getWidth() / 2 - 8, 4, 240, 0, 16, 10);

            ms.pushPose();
            ms.translate(getWidth() / 2, 14, 0);
            ms.scale(3, 3, 1);
            ms.translate(-8, 0, 0);
            ctx.renderItem(stack, 0, 0);
            ms.popPose();
        }
    }

    @Override
    public void tick() {
        for(var widget : propertyWidgets) {
            widget.tick();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(component == null)
            return false;

        boolean result = false;
        for(var widget : propertyWidgets) {
            result |= widget.mouseClicked(mouseX, mouseY, button);
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(component == null)
            return false;

        boolean result = false;
        for(var widget : propertyWidgets) {
            if(widget.isMouseOver(mouseX, mouseY))
                result |= widget.mouseScrolled(mouseX, mouseY, delta);
        }
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = false;
        for(var widget : propertyWidgets) {
            result |= widget.keyPressed(keyCode, scanCode, modifiers);
        }
        return result;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean result = false;
        for(var widget : propertyWidgets) {
            result |= widget.charTyped(chr, modifiers);
        }
        return result;
    }
}
