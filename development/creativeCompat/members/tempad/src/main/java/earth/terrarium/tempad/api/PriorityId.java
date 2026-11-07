// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/PriorityId.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api;

import net.minecraft.resources.ResourceLocation;

/** Compared by priority only, as upstream (two locators with the same priority share one sorted-map slot). */
public record PriorityId(ResourceLocation id, Priority priority) implements Comparable<PriorityId> {
    @Override
    public int compareTo(PriorityId other) {
        return Integer.compare(priority.priority, other.priority.priority);
    }
}
