// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/LocationDataAttachment.kt (NamedGlobalPosAttachment).
// Translated to Java for Forge 1.19.2: Codec unboundedMap(UUID string -> NamedGlobalVec3) -> CompoundTag
// {"<uuid>": NamedGlobalVec3}. Upstream registers it nowhere (no attachment, no reference); ported for completeness,
// unregistered, so the class name follows the Kotlin class, not the file.
package earth.terrarium.tempad.common.data;

import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record NamedGlobalPosAttachment(Map<UUID, NamedGlobalVec3> locations) {
    public NamedGlobalPosAttachment() {
        this(new LinkedHashMap<>());
    }

    /** Upstream {@code plusAssign}. */
    public void add(NamedGlobalVec3 location) {
        locations.put(UUID.randomUUID(), location);
    }

    /** Upstream {@code minusAssign}. */
    public void remove(UUID locationId) {
        locations.remove(locationId);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        locations.forEach((id, location) -> tag.put(id.toString(), location.save()));
        return tag;
    }

    public static NamedGlobalPosAttachment load(CompoundTag tag) {
        Map<UUID, NamedGlobalVec3> locations = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            if (!tag.contains(key, Tag.TAG_COMPOUND)) continue;
            try {
                locations.put(UUID.fromString(key), NamedGlobalVec3.load(tag.getCompound(key)));
            } catch (IllegalArgumentException ignored) {
                // a malformed key fails the whole upstream codec; here only that entry is skipped
            }
        }
        return new NamedGlobalPosAttachment(locations);
    }
}
