// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/apps/SettingsApp.kt (SettingsData). Translated to Java for Forge
// 1.19.2.
package earth.terrarium.tempad.common.apps;

import earth.terrarium.tempad.api.context.ContextHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;

public class SettingsData extends AppContent<SettingsData> {
    public final Set<ResourceLocation> providers;

    public SettingsData(Set<ResourceLocation> providers, boolean isStationary, ContextHolder<?> ctx) {
        super(ctx, isStationary);
        this.providers = providers;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(providers, FriendlyByteBuf::writeResourceLocation);
        buf.writeBoolean(isStationary);
        ctx.encode(buf);
    }

    public static SettingsData decode(FriendlyByteBuf buf) {
        Set<ResourceLocation> providers = buf.readCollection(LinkedHashSet::new, FriendlyByteBuf::readResourceLocation);
        boolean isStationary = buf.readBoolean();
        return new SettingsData(providers, isStationary, ContextHolder.decode(buf));
    }
}
