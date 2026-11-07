// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModTags.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.dimension.DimensionType;

public final class ModTags {
    public static final TagKey<Item> chargeBlacklist = TagKey.create(Registry.ITEM_REGISTRY, Tempad.id("auto_charge_blacklist"));
    public static final TagKey<Item> chrononGens = TagKey.create(Registry.ITEM_REGISTRY, Tempad.id("chronon_generators"));
    public static final TagKey<Item> batteries = TagKey.create(Registry.ITEM_REGISTRY, Tempad.id("batteries"));
    public static final TagKey<DimensionType> enteringNotSupported = TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, Tempad.id("entering_not_supported"));
    public static final TagKey<DimensionType> leavingNotSupported = TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, Tempad.id("leaving_not_supported"));
    public static final TagKey<DimensionType> intradimensionalTravelNotSupported = TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, Tempad.id("intradimensional_travel_not_supported"));
    public static final TagKey<EntityType<?>> teleportingNotSupport = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, Tempad.id("teleporting_not_supported"));

    private ModTags() {
    }
}
