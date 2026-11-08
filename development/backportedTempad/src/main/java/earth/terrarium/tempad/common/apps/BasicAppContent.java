// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/AppContent.kt (BasicAppContent). Translated to Java for
// Forge 1.19.2.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.context.ContextHolder;
import net.minecraft.network.FriendlyByteBuf;

public class BasicAppContent extends AppContent<BasicAppContent> {
    public BasicAppContent(ContextHolder<?> ctx, boolean isStationary) {
        super(ctx, isStationary);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        ctx.encode(buf);
        buf.writeBoolean(isStationary);
    }

    public static BasicAppContent decode(FriendlyByteBuf buf) {
        return new BasicAppContent(ContextHolder.decode(buf), buf.readBoolean());
    }
}
