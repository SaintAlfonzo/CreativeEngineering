// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/AppContent.kt. Translated to Java for Forge 1.19.2:
// Resourceful's MenuContent + RecordCodecMenuContentSerializer -> encode(FriendlyByteBuf), written into the
// NetworkHooks.openScreen buffer and decoded by each IForgeMenuType factory (ModMenus).
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.context.ContextHolder;
import net.minecraft.network.FriendlyByteBuf;

public abstract class AppContent<T extends AppContent<T>> {
    public final ContextHolder<?> ctx;
    public final boolean isStationary;

    protected AppContent(ContextHolder<?> ctx, boolean isStationary) {
        this.ctx = ctx;
        this.isStationary = isStationary;
    }

    public abstract void encode(FriendlyByteBuf buf);
}
