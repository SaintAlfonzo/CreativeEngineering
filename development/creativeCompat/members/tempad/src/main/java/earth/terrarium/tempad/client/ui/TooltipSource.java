// Port glue (ours, GPLv3): 1.19.2 widgets have no tooltip slot; screens ask their widgets for one after rendering.
package earth.terrarium.tempad.client.ui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface TooltipSource {
    @Nullable
    Component tooltipAt(double mouseX, double mouseY);
}
