// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/tooltip/ChrononToolip.kt (ChrononData). Translated to Java for
// Forge 1.19.2. Moved out of the client package: it is the common tooltip data the items return; the client side
// registers a ClientTooltipComponent factory for it.
package earth.terrarium.tempad.api.tva_device;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record ChrononData(int content, int capacity) implements TooltipComponent {
    public static final ChrononData INFINITE = new ChrononData(-1, -1);
}
