// Port glue (ours, GPLv3), TP-D4: 1.21's WidgetSprites (normal / disabled / hovered sprite ids) for Forge 1.19.2.
package earth.terrarium.tempad.client.ui;

import net.minecraft.resources.ResourceLocation;

public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused) {
    /** The two-argument 1.21 constructor: the disabled sprite is the normal one. */
    public WidgetSprites(ResourceLocation enabled, ResourceLocation enabledFocused) {
        this(enabled, enabled, enabledFocused);
    }

    public ResourceLocation get(boolean active, boolean hoveredOrFocused) {
        if (!active) return disabled;
        return hoveredOrFocused ? enabledFocused : enabled;
    }
}
