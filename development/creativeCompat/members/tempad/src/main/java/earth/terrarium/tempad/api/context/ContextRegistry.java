// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/SyncableContext.kt (ContextRegistry). Translated to Java for
// Forge 1.19.2.
package earth.terrarium.tempad.api.context;

import earth.terrarium.tempad.api.PriorityId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class ContextRegistry {
    /** Upstream {@code ContextProvider<T> = (Player, T) -> SyncableContext<T>}. */
    @FunctionalInterface
    public interface ContextProvider<T> extends BiFunction<Player, T, SyncableContext<T>> {
    }

    /** Upstream {@code ContextLocator = (Player, (ItemStack) -> Boolean) -> SyncableContext<*>?}. */
    @FunctionalInterface
    public interface ContextLocator {
        SyncableContext<?> locate(Player player, Predicate<ItemStack> filter);
    }

    private static final Map<ContextType<?>, ContextProvider<?>> REGISTRY = new LinkedHashMap<>();
    private static final TreeMap<PriorityId, ContextLocator> LOCATORS = new TreeMap<>();

    private ContextRegistry() {
    }

    public static Map<ContextType<?>, ContextProvider<?>> registry() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    public static Map<PriorityId, ContextLocator> locatorRegistry() {
        return Collections.unmodifiableMap(LOCATORS);
    }

    public static <T> void register(ContextType<T> type, ContextProvider<T> provider) {
        REGISTRY.put(type, provider);
    }

    public static void registerLocator(PriorityId id, ContextLocator locator) {
        LOCATORS.put(id, locator);
    }

    @SuppressWarnings("unchecked")
    public static <T> SyncableContext<T> get(ContextType<T> type, Player player, T context) {
        ContextProvider<T> provider = (ContextProvider<T>) REGISTRY.get(type);
        if (provider == null) throw new IllegalStateException("No provider for " + type);
        return provider.apply(player, context);
    }

    public static SyncableContext<?> locate(Player player, Predicate<ItemStack> predicate) {
        for (ContextLocator locator : LOCATORS.values()) {
            SyncableContext<?> context = locator.locate(player, predicate);
            if (context != null) return context;
        }
        return null;
    }
}
