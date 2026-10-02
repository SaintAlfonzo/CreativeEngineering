package io.github.mortuusars.exposure.backport.gui;

import net.minecraft.resources.ResourceLocation;

/** Backport of 1.20.2+ {@code net.minecraft.client.gui.components.WidgetSprites}. */
public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused) {
    public WidgetSprites(ResourceLocation enabled, ResourceLocation focused) {
        this(enabled, enabled, focused, focused);
    }

    public WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation focused) {
        this(enabled, disabled, focused, disabled);
    }

    public ResourceLocation get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }
}
