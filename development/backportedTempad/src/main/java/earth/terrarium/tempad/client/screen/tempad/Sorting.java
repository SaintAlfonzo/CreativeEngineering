// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/TeleportScreen.kt
// (enum Sorting). Translated to Java for Forge 1.19.2. Kotlin's groupBy keeps first-seen key order: LinkedHashMap.
package earth.terrarium.tempad.client.screen.tempad;

import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public enum Sorting {
    Dimension, Alphabetical, Type;

    public record Entry(ResourceLocation provider, UUID id, NamedGlobalVec3 pos) {
    }

    public String getTranslationKey() {
        return "button.tempad." + toString().toLowerCase(Locale.ROOT);
    }

    public Component toComponent() {
        return Component.translatable(getTranslationKey());
    }

    private static List<Entry> flatten(Map<ResourceLocation, ? extends Map<UUID, NamedGlobalVec3>> values) {
        List<Entry> list = new ArrayList<>();
        values.forEach((provider, locations) -> locations.forEach((id, pos) -> list.add(new Entry(provider, id, pos))));
        return list;
    }

    private static Map<Component, List<Entry>> group(List<Entry> entries, Function<Entry, Component> key) {
        // Components compare by content, as Kotlin's groupBy does through equals.
        Map<Component, List<Entry>> map = new LinkedHashMap<>();
        for (Entry e : entries) map.computeIfAbsent(key.apply(e), k -> new ArrayList<>()).add(e);
        return map;
    }

    public Map<Component, List<Entry>> reorganize(Map<ResourceLocation, ? extends Map<UUID, NamedGlobalVec3>> values) {
        Map<Component, List<Entry>> result;
        switch (this) {
            case Dimension -> {
                result = group(flatten(values), e -> Component.translatable(Util.makeDescriptionId("dimension", e.pos().dimension().location())));
                result.values().forEach(list -> list.sort(Comparator.comparing(e -> e.pos().toString())));
            }
            case Alphabetical -> {
                List<Entry> sorted = flatten(values);
                sorted.sort(Comparator.comparing(e -> e.pos().name().getString()));
                result = group(sorted, e -> e.pos().name().getString().isEmpty() ? CommonComponents.EMPTY
                        : Component.literal(String.valueOf(Character.toUpperCase(e.pos().name().getString().charAt(0)))));
                result.values().forEach(list -> list.sort(Comparator.comparing(e -> e.pos().name().getString())));
            }
            default -> {
                result = group(flatten(values), e -> Component.translatable(Util.makeDescriptionId("provider", e.provider())));
                result.values().forEach(list -> list.sort(Comparator.comparing(e -> e.pos().toString())));
            }
        }
        return result;
    }
}
