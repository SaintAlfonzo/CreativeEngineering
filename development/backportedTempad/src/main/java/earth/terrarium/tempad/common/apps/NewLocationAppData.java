// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/NewLocationApp.kt (NewLocationAppData). Translated to Java
// for Forge 1.19.2.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.context.ContextHolder;
import net.minecraft.network.FriendlyByteBuf;

public class NewLocationAppData extends AppContent<NewLocationAppData> {
    public final boolean allowLocationSaving;

    public NewLocationAppData(boolean allowLocationSaving, ContextHolder<?> ctx) {
        super(ctx, false);
        this.allowLocationSaving = allowLocationSaving;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(allowLocationSaving);
        ctx.encode(buf);
    }

    public static NewLocationAppData decode(FriendlyByteBuf buf) {
        return new NewLocationAppData(buf.readBoolean(), ContextHolder.decode(buf));
    }
}
