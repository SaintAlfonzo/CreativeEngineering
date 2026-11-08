// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/app/AppRegistry.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.app;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AppRegistry {
    @FunctionalInterface
    public interface AppProvider {
        @Nullable
        TempadApp<?> create(SyncableContext<?> ctx, boolean isStationary);
    }

    public record AppHolder(ResourceLocation id, ContextHolder<?> ctx, boolean isStationary) {
        @Nullable
        public TempadApp<?> getApp(Player player) {
            return AppRegistry.get(id, ctx.getCtx(player), isStationary);
        }

        public SyncableContext<?> getCtx(Player player) {
            return ctx.getCtx(player);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeResourceLocation(id);
            ctx.encode(buf);
            buf.writeBoolean(isStationary);
        }

        public static AppHolder read(FriendlyByteBuf buf) {
            return new AppHolder(buf.readResourceLocation(), ContextHolder.decode(buf), buf.readBoolean());
        }
    }

    private static final Map<ResourceLocation, AppProvider> APPS = new LinkedHashMap<>();

    private AppRegistry() {
    }

    public static void register(ResourceLocation id, AppProvider provider) {
        APPS.put(id, provider);
    }

    @Nullable
    public static TempadApp<?> get(ResourceLocation id, SyncableContext<?> ctx, boolean isStationary) {
        AppProvider provider = APPS.get(id);
        return provider == null ? null : provider.create(ctx, isStationary);
    }

    public static Map<ResourceLocation, TempadApp<?>> getAll(SyncableContext<?> ctx, boolean isStationary) {
        Map<ResourceLocation, TempadApp<?>> result = new LinkedHashMap<>();
        APPS.forEach((id, provider) -> {
            TempadApp<?> app = provider.create(ctx, isStationary);
            if (app != null) result.put(id, app);
        });
        return result;
    }

    public static Set<ResourceLocation> getIds() {
        return Collections.unmodifiableSet(APPS.keySet());
    }
}
