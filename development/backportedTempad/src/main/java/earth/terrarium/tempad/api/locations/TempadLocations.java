// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/locations/TempadLocations.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.locations;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.context.ItemContext;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TempadLocations {
    /** Upstream {@code LocationProvider = (GameProfile, UpgradeHandler, ChrononHandler) -> LocationHandler}. */
    @FunctionalInterface
    public interface LocationProvider {
        LocationHandler create(GameProfile profile, @Nullable UpgradeHandler upgrades, @Nullable ChrononHandler chronons);
    }

    private static final Map<ResourceLocation, LocationProvider> REGISTRY = new LinkedHashMap<>();
    private static final Set<ResourceLocation> DELETABLE = new LinkedHashSet<>();

    private TempadLocations() {
    }

    public static Map<ResourceLocation, LocationProvider> registry() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    public static Set<ResourceLocation> deletable() {
        return Collections.unmodifiableSet(DELETABLE);
    }

    public static void register(ResourceLocation id, LocationProvider provider) {
        REGISTRY.put(id, provider);
    }

    public static void setDeletable(ResourceLocation id) {
        DELETABLE.add(id);
    }

    @Nullable
    public static LocationProvider get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    @Nullable
    public static LocationHandler get(GameProfile player, @Nullable UpgradeHandler upgrades, @Nullable ChrononHandler chronons, ResourceLocation id) {
        LocationProvider provider = REGISTRY.get(id);
        return provider == null ? null : provider.create(player, upgrades, chronons);
    }

    @Nullable
    public static LocationHandler get(Player player, ItemContext ctx, ResourceLocation id) {
        LocationProvider provider = REGISTRY.get(id);
        return provider == null ? null : provider.create(player.getGameProfile(), UpgradeHandler.of(ctx.getStack()), ChrononHandler.of(ctx.getStack()));
    }

    public static Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> getAll(GameProfile player, @Nullable UpgradeHandler upgrades, @Nullable ChrononHandler chronons) {
        Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> result = new LinkedHashMap<>();
        REGISTRY.forEach((id, provider) -> result.put(id, provider.create(player, upgrades, chronons).getLocations()));
        return result;
    }

    public static Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> getAll(Player player, ItemContext ctx) {
        return getAll(player.getGameProfile(), UpgradeHandler.of(ctx.getStack()), ChrononHandler.of(ctx.getStack()));
    }
}
